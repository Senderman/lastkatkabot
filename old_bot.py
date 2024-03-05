#!/usr/bin/env python
# reast in peace, lastkatka in python

import json
import os
import sys
import time
import config # config.py in the bot's directory

import telepot
from telepot.loop import MessageLoop

token = config.token

lastkatka = config.lastkatka
lastvegan = config.lastvegan
tourgroup = config.tourgroup
allowed_chats = [lastkatka, lastvegan, tourgroup]
admins = config.admins

# for tournament
tournamentEnabled = False
link = ""
tournamentChannel = config.tournamentChannel
tournamentPassword = config.tournamentPassword
members = []
members_ids = []
# for vegan
veganStartCommands = config.veganStartCommands
joined = []
joinAllowed = False


def reply(chat_id, msg):
    if 'reply_to_message' not in msg:
        bot.sendMessage(chat_id, "Этой командой нужно ответить на сообщение!", reply_to_message_id=msg['message_id'])
        return False
    else:
        return True


def proccessTournament(chat_id, msg, txt): # managing tournament
    global tournamentEnabled
    global members
    global members_ids

    if "/summon" == txt[:7]:
        pinglist = []
        for i in members:
            pinglist.append("@" + i)
        bot.sendMessage(lastvegan, ", ".join(
            pinglist) + ", напишите мне в лс команду /tournament для получения ссылки на группу с турниром")

    elif "/tournament" == txt[:11]:
        if msg['chat']['type'] == "private":
            if msg['from']['username'] in members:
                members_ids.append(msg['from']['id'])  # collect user's ids (will be used in /kickmembers command)
                bot.sendMessage(chat_id, "Заходи сюда -> " + link)
            else:
                bot.sendMessage(chat_id, "Вас пока еще не звали")
        else:
            bot.sendMessage(chat_id, "Команда работает только у бота в лс", reply_to_message_id=msg['message_id'])

    elif "/kickmembers" == txt[:12] and msg['from']['id'] in admins:
        bot.sendMessage(tourgroup, "Раунд завершен, пока!")
        bot.sendMessage(lastvegan,
                        "<b>Раунд завершен. Болельщики, посетите " + tournamentChannel + ",  чтобы узнать результат!</b>",
                        parse_mode="HTML")
        for user_id in members_ids:
            bot.kickChatMember(tourgroup, user_id)
            bot.unbanChatMember(tourgroup, user_id)
        resetTournament()

    elif "/rt" == txt[:3] and msg['from']['id'] in admins:
        bot.sendMessage(lastvegan, "<b>Турнир отменен из-за непредвиденных обстоятельств!</b>", parse_mode="HTML")


def proccessVegan(msg, txt): # counting players
    global joined
    global joinAllowed

    if txt in veganStartCommands:
        if not joinAllowed:
            joinAllowed = True
            joined.clear()

    if joinAllowed:
        if "/join@veganwarsbot" == txt[:18]:
            if msg['from']['username'] not in joined:
                joined.append(msg['from']['username'])
                bot.sendMessage(lastvegan, "Джойнулось " + str(len(joined)) + " игроков")

        elif "/fight@veganwarsbot" == txt[:19]:
            if len(joined) > 1:
                resetVegan()
            else:
                bot.sendMessage(lastvegan, "Веганы, джоин, мясоеды наступают!!!")

        elif "/flee@veganwarsbot" == txt[:18]:
            if msg['from']['username'] in joined:
                joined.remove(msg['from']['username'])
                bot.sendMessage(lastvegan, "Осталось " + str(len(joined)) + " игроков")

    elif "/reset" == txt[:6]:
        bot.sendMessage(lastvegan, "Счетчик обнулен!")
        resetVegan()


def resetVegan():
    global joinAllowed
    joinAllowed = False
    joined.clear()


def resetTournament():
    members.clear()
    members_ids.clear()
    tournamentEnabled = False


def handle(msg):
    global tournamentEnabled
    global members
    global link

    content_type, chat_type, chat_id = telepot.glance(msg)

    if chat_type != "private" and chat_id not in allowed_chats:  # leave from foreign groups
        bot.sendMessage(chat_id, "Какая то левая конфа, ну ее нафиг")
        bot.leaveChat(chat_id)

    txt = ""
    if 'text' in msg:
        txt = txt + msg['text']
    if txt != "" and chat_type != "channel":
        print("Msg: " + msg['from']['first_name'] + ": " + txt)

        if "/pinthis" == txt[:8]:
            if reply(chat_id, msg):
                bot.pinChatMessage(chat_id, msg['reply_to_message']['message_id'], disable_notification=True)
                bot.deleteMessage(telepot.message_identifier(msg))

        elif "/unpin" == txt[:6]:
            bot.unpinChatMessage(chat_id)
            bot.deleteMessage(telepot.message_identifier(msg))

        elif "/nahuy" == txt[:6] and chat_type != "private":
            if reply(chat_id, msg):
                reply_id = msg['reply_to_message']['message_id']
                if msg['reply_to_message']['from']['username'] == "Senderman":
                    bot.sendMessage(chat_id, "Юльку нахуй слать низзя!", reply_to_message_id=msg['message_id'])
                elif msg['reply_to_message']['from']['username'] == "gtfo_uvao_bot":
                    bot.sendMessage(chat_id, "Бота нахуй слать низзя!", reply_to_message_id=msg['message_id'])
                else:
                    bot.sendMessage(chat_id, "Вы были посланы нахуй юзером " + msg['from']['first_name'],
                                    reply_to_message_id=reply_id)
                    bot.deleteMessage(telepot.message_identifier(msg))

        elif "/thx" == txt[:4] and chat_type != "private":
            if reply(chat_id, msg):
                reply_id = msg['reply_to_message']['message_id']
                bot.sendMessage(chat_id, "Вы были поглажены по голове юзером " + msg['from']['first_name'],
                                reply_to_message_id=reply_id)
                bot.deleteMessage(telepot.message_identifier(msg))

        elif "/setup" == txt[:6]:
            params = txt.strip().replace("@", "").split(" ")[1:]
            if len(params) != 4:
                bot.sendMessage(chat_id, "Неверное количество аргументов!", reply_to_message_id=msg['message_id'])
            elif params[3] != tournamentPassword:
                bot.sendMessage(chat_id, "Неверный пароль!")
            else:
                members = params[0:2]
                link = params[2]
                tournamentEnabled = True
                resetVegan()
                bot.sendMessage(lastvegan, "<b>Турнир активирован!</b>\n\n<b>Участники:</b> " + ", ".join(
                    members) + "\nОтправьте /summon чтобы позвать участников на турнир!", parse_mode="HTML")

        elif tournamentEnabled:
            proccessTournament(chat_id, msg, txt)

        elif chat_id == lastvegan:
            proccessVegan(msg, txt)


bot = telepot.Bot(token)

# skip old messages
updates = bot.getUpdates()
if updates:
    last_update_id = updates[-1]['update_id']
    bot.getUpdates(offset=last_update_id+1)

MessageLoop(bot, handle).run_as_thread()
print('Listening ...')
while 1:
    time.sleep(10)
