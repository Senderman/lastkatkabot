package com.senderman.lastkatkabot.handlers;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.TgUser;
import com.senderman.lastkatkabot.DBService;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.LastkatkaBotHandler;
import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminHandler {

    private final LastkatkaBotHandler handler;

    public AdminHandler(LastkatkaBotHandler handler) {
        this.handler = handler;
    }

    public void addUser(Message message, DBService.UserType type) {
        if (!message.isReply())
            return;

        var id = message.getReplyToMessage().getFrom().getId();
        Set<Integer> list = null;
        String format = "";
        switch (type) {
            case ADMINS:
                list = handler.getAdmins();
                format = "✅ %1$s теперь мой хозяин!";
                break;
            case BLACKLIST:
                if (handler.isFromAdmin(message.getReplyToMessage()) || handler.isPremiumUser(message.getReplyToMessage())) {
                    handler.sendMessage(message.getChatId(), "Мы таких в плохие киси не берем!");
                    return;
                }
                list = handler.getBlacklist();
                format = "\uD83D\uDE3E %1$s - плохая киса!";
                break;
            case PREMIUM:
                list = handler.getPremiumUsers();
                format = "\uD83D\uDC51 %1$s теперь премиум пользователь!";
                break;
        }

        var name = message.getReplyToMessage().getFrom().getFirstName();
        var user = new TgUser(id, name);
        list.add(id);
        Services.db.addTgUser(id, type);
        handler.sendMessage(message.getChatId(), String.format(format, user.getName()));
    }

    public void listUsers(Message message, DBService.UserType type) {
        var users = Services.db.getTgUsersByType(type);
        var messageToSend = Methods.sendMessage().setChatId(message.getChatId());

        boolean allAdminsAccess = false;
        String title;
        String callback;
        switch (type) {
            case ADMINS:
                title = "\uD83D\uDE0E <b>Админы бота:</b>\n";
                callback = LastkatkaBot.CALLBACK_DELETE_ADMIN;
                break;
            case BLACKLIST:
                allAdminsAccess = true;
                title = "\uD83D\uDE3E <b>Список плохих кис:</b>\n";
                callback = LastkatkaBot.CALLBACK_DELETE_NEKO;
                break;
            case PREMIUM:
                title = "\uD83D\uDC51 <b>Список премиум-пользователей:</b>\n";
                callback = LastkatkaBot.CALLBACK_DELETE_PREM;
                break;
            default:
                return;
        }

        var showButtons = allAdminsAccess || message.getChatId().equals((long) Services.botConfig.getMainAdmin());

        if (!showButtons || !message.isUserMessage()) {
            var userlist = new StringBuilder(title);
            var dropList = new StringBuilder("\n");
            for (var id : users) {
                try {
                    var name = Methods.getChatMember(id, id).call(handler).getUser().getFirstName();
                    var user = new TgUser(id, name);
                    userlist.append(user.getLink()).append("\n");
                } catch (Exception e) {
                    Services.db.removeTGUser(id, type);
                    dropList.append("Юзер с id ").append(id).append(" удален из бд!\n");
                }
            }
            messageToSend.setText(userlist.append(dropList).toString());
        } else {
            var markup = new InlineKeyboardMarkup();
            ArrayList<List<InlineKeyboardButton>> rows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (var id : users) {
                var name = Methods.getChatMember(id, id).call(handler).getUser().getFirstName();
                var user = new TgUser(id, name);
                row.add(new InlineKeyboardButton()
                        .setText(user.getName())
                        .setCallbackData(callback + " " + user.getId()));
                if (row.size() == 2) {
                    rows.add(row);
                    row = new ArrayList<>();
                }
            }
            if (row.size() == 1) {
                rows.add(row);
            }
            rows.add(List.of(new InlineKeyboardButton()
                    .setText("Закрыть меню")
                    .setCallbackData(LastkatkaBot.CALLBACK_CLOSE_MENU)));
            markup.setKeyboard(rows);
            messageToSend.setText(title + "Для удаления пользователя нажмите на него").setReplyMarkup(markup);
        }
        handler.sendMessage(messageToSend);
    }

    public void goodneko(Message message) {
        if (!message.isReply())
            return;

        var neko = new TgUser(message.getReplyToMessage().getFrom().getId(), message.getReplyToMessage().getFrom().getFirstName());
        Services.db.removeTGUser(neko.getId(), DBService.UserType.BLACKLIST);
        handler.getBlacklist().remove(message.getReplyToMessage().getFrom().getId());
        handler.sendMessage(message.getChatId(),
                String.format("\uD83D\uDE38 %1$s - хорошая киса!", neko.getLink()));
    }

    public void update(Message message) {
        var params = message.getText().split("\n");
        if (params.length < 2) {
            handler.sendMessage(message.getChatId(), "Неверное количество аргументов!");
            return;
        }
        var update = new StringBuilder().append("\uD83D\uDCE3 <b>ВАЖНОЕ ОБНОВЛЕНИЕ:</b> \n\n");
        for (int i = 1; i < params.length; i++) {
            update.append("* ").append(params[i]).append("\n");
        }
        var tempChatSet = new HashSet<>(handler.getAllowedChats());
        tempChatSet.remove(Services.botConfig.getTourgroup());
        for (long chat : tempChatSet) {
            handler.sendMessage(chat, update.toString());
        }
    }

    public void chats(Message message) {
        if (!message.isUserMessage()) {
            handler.sendMessage(message.getChatId(), "Команду можно использовать только в лс бота!");
            return;
        }
        var markup = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> rows = new ArrayList<>();
        var chats = Services.db.getAllowedChatsMap();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (long chatId : chats.keySet()) {
            row.add(new InlineKeyboardButton()
                    .setText(chats.get(chatId))
                    .setCallbackData(LastkatkaBot.CALLBACK_DELETE_CHAT + " " + chatId));
            if (row.size() == 2) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        if (row.size() == 1) {
            rows.add(row);
        }
        rows.add(List.of(new InlineKeyboardButton()
                .setText("Закрыть меню")
                .setCallbackData(LastkatkaBot.CALLBACK_CLOSE_MENU)));
        markup.setKeyboard(rows);
        handler.sendMessage(Methods.sendMessage(message.getChatId(), "Для удаления чата нажите на него")
                .setReplyMarkup(markup));
    }

    public void cleanChats(Message message) {
        var chats = Services.db.getAllowedChatsMap();
        for (long chatId : chats.keySet()) {
            try {
                var chatMsg = handler.execute(new SendMessage(chatId, "Сервисное сообщение, оно будет удалено через пару секунд"));
                var title = chatMsg.getChat().getTitle();
                Methods.deleteMessage(chatId, chatMsg.getMessageId()).call(handler);
                Services.db.updateTitle(chatId, title);
            } catch (TelegramApiException e) {
                Services.db.removeAllowedChat(chatId);
                Services.db.cleanup();
                handler.sendMessage(message.getFrom().getId(), "Чат \"" + chats.get(chatId) + "\" удален из списка!");
                handler.getAllowedChats().remove(chatId);
            }
        }
        handler.sendMessage(message.getFrom().getId(), "Чаты обновлены!");
    }

    public void announce(Message message) {
        handler.sendMessage(message.getChatId(), "Рассылка запущена. На время рассылки бот будет недоступен");
        var text = message.getText();
        text = "\uD83D\uDCE3 <b>Объявление</b>\n\n" + text.split("\\s+", 2)[1];
        var usersIds = Services.db.getAllUsersIds();
        var counter = 0;
        for (var userId : usersIds) {
            try {
                handler.execute(new SendMessage((long) userId, text).enableHtml(true));
                counter++;
            } catch (TelegramApiException e) {
                BotLogger.error("ANNOUNCE", e.toString());
            }
        }
        handler.sendMessage(message.getChatId(),
                String.format("Объявление получили %1$d/%2$d человек", counter, usersIds.size()));
    }

    public void setupHelp(Message message) {
        handler.sendMessage(message.getChatId(), Services.botConfig.getSetupHelp());
    }
}
