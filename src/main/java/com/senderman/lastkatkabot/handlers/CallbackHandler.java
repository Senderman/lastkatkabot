package com.senderman.lastkatkabot.handlers;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.TgUser;
import com.senderman.lastkatkabot.DBService;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.LastkatkaBotHandler;
import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Set;

public class CallbackHandler {

    private final LastkatkaBotHandler handler;

    public CallbackHandler(LastkatkaBotHandler handler) {
        this.handler = handler;
    }

    public void payRespects(CallbackQuery query) {
        if (query.getMessage().getText().contains(query.getFrom().getFirstName())) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText("You've already payed respects! (or you've tried to pay respects to yourself)")
                    .setShowAlert(true)
                    .call(handler);
            return;
        }
        Methods.answerCallbackQuery()
                .setCallbackQueryId(query.getId())
                .setText("You've payed respects")
                .setShowAlert(true)
                .call(handler);
        Methods.editMessageText()
                .setChatId(query.getMessage().getChatId())
                .setMessageId(query.getMessage().getMessageId())
                .setReplyMarkup(UsercommandsHandler.getMarkupForPayingRespects())
                .setText(query.getMessage().getText() + "\n" + query.getFrom().getFirstName() + " has payed respects")
                .call(handler);
    }

    public void cake(CallbackQuery query, CAKE_ACTIONS actions) {
        if (!query.getFrom().getId().equals(query.getMessage().getReplyToMessage().getFrom().getId())) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText("Этот тортик не вам!")
                    .setShowAlert(true)
                    .call(handler);
            return;
        }

        if (query.getMessage().getDate() + 2400 < System.currentTimeMillis() / 1000) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText("Тортик испортился!")
                    .setShowAlert(true)
                    .call(handler);
            Methods.editMessageText()
                    .setChatId(query.getMessage().getChatId())
                    .setText("\uD83E\uDD22 Тортик попытались взять, но он испортился!")
                    .setMessageId(query.getMessage().getMessageId())
                    .setReplyMarkup(null)
                    .call(handler);
            return;
        }

        var acq = Methods.answerCallbackQuery()
                .setCallbackQueryId(query.getId());
        var emt = Methods.editMessageText()
                .setChatId(query.getMessage().getChatId())
                .setMessageId(query.getMessage().getMessageId())
                .setReplyMarkup(null);
        if (actions == CAKE_ACTIONS.CAKE_OK) {
            acq.setText("n p u я m н o r o  a n n e m u m a");
            emt.setText("\uD83C\uDF82 " + query.getFrom().getFirstName() + " принял тортик"
                    + query.getData().replace(LastkatkaBot.CALLBACK_CAKE_OK, ""));
        } else {
            acq.setText("Ну и ладно");
            emt.setText("\uD83D\uDEAB \uD83C\uDF82 " + query.getFrom().getFirstName() + " отказался от тортика"
                    + query.getData().replace(LastkatkaBot.CALLBACK_CAKE_NOT, ""));
        }
        acq.call(handler);
        emt.call(handler);
    }

    public void registerInTournament(CallbackQuery query) {
        var memberId = query.getFrom().getId();
        if (!handler.tournamentHandler.isEnabled) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText("⚠️ На данный момент нет открытых раундов!")
                    .setShowAlert(true)
                    .call(handler);
            return;
        }

        if (handler.tournamentHandler.membersIds.contains(memberId)) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText("⚠️ Вы уже получили разрешение на отправку сообщений!")
                    .setShowAlert(true)
                    .call(handler);
            return;
        }

        if (!handler.tournamentHandler.members.contains(query.getFrom().getUserName())) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText("\uD83D\uDEAB Вы не являетесь участником текущего раунда!")
                    .setShowAlert(true)
                    .call(handler);
            return;
        }

        handler.tournamentHandler.membersIds.add(memberId);
        Methods.Administration.restrictChatMember()
                .setChatId(Services.config().getTourgroup())
                .setUserId(memberId)
                .setCanSendMessages(true)
                .setCanSendMediaMessages(true)
                .setCanSendOtherMessages(true)
                .call(handler);
        Methods.answerCallbackQuery()
                .setCallbackQueryId(query.getId())
                .setText("✅ Вам даны права на отправку сообщений в группе турнира!")
                .setShowAlert(true)
                .call(handler);
        handler.sendMessage(Services.config().getTourgroup(), String.format("✅ %1$s получил доступ к игре!", query.getFrom().getFirstName()));
    }

    public void addChat(CallbackQuery query) {
        var chatId = Long.parseLong(query.getData().replace(LastkatkaBot.CALLBACK_ALLOW_CHAT, ""));
        handler.allowedChats.add(chatId);
        Methods.editMessageText()
                .setChatId(query.getMessage().getChatId())
                .setText("✅ Чат добавлен в разрешенные!")
                .setMessageId(query.getMessage().getMessageId())
                .setReplyMarkup(null)
                .call(handler);
        var message = handler.sendMessage(chatId, "Разработчик принял данный чат. Бот готов к работе здесь!\n" +
                "Для некоторых фичей бота требуются права админа на удаление и закреп сообщений.");
        Services.db().addAllowedChat(chatId, message.getChat().getTitle());
    }

    public void denyChat(CallbackQuery query) {
        var chatId = Long.parseLong(query.getData().replace(LastkatkaBot.CALLBACK_DONT_ALLOW_CHAT, ""));
        Methods.editMessageText()
                .setChatId(query.getMessage().getChatId())
                .setMessageId(query.getMessage().getMessageId())
                .setText("\uD83D\uDEAB Чат отклонен!")
                .setReplyMarkup(null)
                .call(handler);
        handler.sendMessage(chatId, "Разработчик отклонил данный чат. Всем пока!");
        Methods.leaveChat(chatId).call(handler);
    }

    public void deleteChat(CallbackQuery query) {
        var chatId = Long.parseLong(query.getData().split(" ")[1]);
        Services.db().removeAllowedChat(chatId);
        Services.db().deleteBncGame(chatId);
        handler.allowedChats.remove(chatId);
        Methods.answerCallbackQuery()
                .setShowAlert(true)
                .setText("Чат удален!")
                .setCallbackQueryId(query.getId())
                .call(handler);
        handler.sendMessage(chatId, "Разработчик решил удалить бота из данного чата. Всем пока!");
        Methods.leaveChat(chatId).call(handler);
        Methods.deleteMessage(query.getMessage().getChatId(), query.getMessage().getMessageId()).call(handler);
    }

    public void deleteUser(CallbackQuery query, DBService.COLLECTION_TYPE type) {
        Set<Integer> userIds;
        String listName;
        switch (type) {
            case ADMINS:
                userIds = handler.admins;
                listName = "админов";
                break;
            case BLACKLIST:
                userIds = handler.blacklist;
                listName = "плохих кошечек";
                break;
            case PREMIUM:
                userIds = handler.premiumUsers;
                listName = "премиум-пользователей";
                break;
            default:
                return;
        }
        var userId = Integer.parseInt(query.getData().split(" ")[1]);
        Services.db().removeTGUser(userId, type);
        userIds.remove(userId);
        Methods.answerCallbackQuery()
                .setShowAlert(true)
                .setText("Пользователь удален из списка")
                .setCallbackQueryId(query.getId())
                .call(handler);
        handler.sendMessage(userId, "Разработчик удалил вас из " + listName + " бота!");
        Methods.deleteMessage(query.getMessage().getChatId(), query.getMessage().getMessageId()).call(handler);
    }

    public void accept_marriage(CallbackQuery query) {
        var userId = query.getFrom().getId();
        var message = query.getMessage();
        if (!message.getReplyToMessage().getFrom().getId().equals(userId)) {
            Methods.answerCallbackQuery()
                    .setShowAlert(true)
                    .setText("Куда лезете? Это не вам!")
                    .setCallbackQueryId(query.getId())
                    .call(handler);
            return;
        }

        Methods.answerCallbackQuery()
                .setShowAlert(true)
                .setText("Поздравляем! Теперь у вас есть вторая половинка")
                .setCallbackQueryId(query.getId())
                .call(handler);

        Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(handler);

        // user - accepter, couple - inviter
        var user = new TgUser(userId, query.getFrom().getFirstName());
        var coupleId = Integer.parseInt(query.getData().replaceAll(LastkatkaBot.CALLBACK_ACCEPT_MARRIAGE, ""));
        var couple = new TgUser(Methods.getChatMember(message.getChatId(), coupleId).call(handler).getUser());
        Services.db().setLover(user.getId(), couple.getId());
        handler.sendMessage(message.getChatId(), user.getId() + " " + couple.getId());
        handler.sendMessage(couple.getId(), "Поздравляем! Теперь ваша вторая половинка - " + user.getLink());
        var format = "Внимание все! Сегодня великий день свадьбы %s и %s! Так давайте же поздравим их и съедим шавуху в часть такого праздника!";
        var text = String.format(format, user.getLink(), couple.getLink());
        handler.sendMessage(message.getChatId(), text);
    }

    public void deny_marriage(CallbackQuery query) {
        var userId = query.getFrom().getId();
        var message = query.getMessage();
        if (!message.getReplyToMessage().getFrom().getId().equals(userId)) {
            Methods.answerCallbackQuery()
                    .setShowAlert(true)
                    .setText("Куда лезете? Это не вам!")
                    .setCallbackQueryId(query.getId())
                    .call(handler);
            return;
        }

        Methods.answerCallbackQuery()
                .setShowAlert(false)
                .setText("Принято")
                .setCallbackQueryId(query.getId())
                .call(handler);
        Methods.editMessageText()
                .setChatId(message.getChatId())
                .setText("Пользователь " + query.getFrom().getFirstName() + "отказался от брака :(")
                .setReplyMarkup(null)
                .setMessageId(message.getMessageId())
                .call(handler);
    }

    public void closeMenu(CallbackQuery query) {
        Methods.editMessageText()
                .setChatId(query.getMessage().getChatId())
                .setMessageId(query.getMessage().getMessageId())
                .setText("Меню закрыто")
                .setReplyMarkup(null)
                .call(handler);
    }

    public enum CAKE_ACTIONS {CAKE_OK, CAKE_NOT}
}