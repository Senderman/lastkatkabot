package com.senderman.lastkatkabot.Handlers;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.DBService;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.LastkatkaBotHandler;
import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.ArrayList;

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
        var userLocale = Services.i18n().getLocale(query);
        var chatLocale = Services.db().getChatLocale(query.getMessage().getChatId());
        if (!query.getFrom().getId().equals(query.getMessage().getReplyToMessage().getFrom().getId())) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText(Services.i18n().getString("notForU", userLocale))
                    .setShowAlert(true)
                    .call(handler);
            return;
        }

        if (query.getMessage().getDate() + 2400 < System.currentTimeMillis() / 1000) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText(Services.i18n().getString("ripCake", userLocale))
                    .setShowAlert(true)
                    .call(handler);
            Methods.editMessageText()
                    .setChatId(query.getMessage().getChatId())
                    .setText("\uD83E\uDD22 " + Services.i18n().getString("ripCake", chatLocale))
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
            emt.setText("\uD83C\uDF82 " + query.getFrom().getFirstName() + " " + Services.i18n().getString("gotCake", chatLocale)
                    + query.getData().replace(LastkatkaBot.CALLBACK_CAKE_OK, ""));
        } else {
            acq.setText("Ну и ладно");
            emt.setText("\uD83D\uDEAB \uD83C\uDF82 " + query.getFrom().getFirstName() + " " + Services.i18n().getString("fuckTheCake", chatLocale)
                    + query.getData().replace(LastkatkaBot.CALLBACK_CAKE_NOT, ""));
        }
        acq.call(handler);
        emt.call(handler);
    }

    public void registerInTournament(CallbackQuery query) {
        var memberId = query.getFrom().getId();
        var userLocale = Services.db().getUserLocale(memberId);
        var chatLocale = Services.db().getChatLocale(Services.botConfig().getTourgroup());
        if (!TournamentHandler.isEnabled) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText("⚠️ " + Services.i18n().getString("noOpenRounds", userLocale))
                    .setShowAlert(true)
                    .call(handler);
            return;
        }

        if (TournamentHandler.membersIds.contains(memberId)) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText("⚠️ " + Services.i18n().getString("uHavePermission", userLocale))
                    .setShowAlert(true)
                    .call(handler);
            return;
        }

        if (!TournamentHandler.members.contains(query.getFrom().getUserName())) {
            Methods.answerCallbackQuery()
                    .setCallbackQueryId(query.getId())
                    .setText(Services.i18n().getString("notAPart", userLocale))
                    .setShowAlert(true)
                    .call(handler);
            return;
        }

        TournamentHandler.membersIds.add(memberId);
        Methods.Administration.restrictChatMember()
                .setChatId(Services.botConfig().getTourgroup())
                .setUserId(memberId)
                .setCanSendMessages(true)
                .setCanSendMediaMessages(true)
                .setCanSendOtherMessages(true)
                .call(handler);
        Methods.answerCallbackQuery()
                .setCallbackQueryId(query.getId())
                .setText("✅ " + Services.i18n().getString("permissionGiven", userLocale))
                .setShowAlert(true)
                .call(handler);
        handler.sendMessage(Services.botConfig().getTourgroup(),
                "✅ " + String.format(Services.i18n().getString("accessAllowed", chatLocale), query.getFrom().getFirstName()));
    }

    public void addChat(CallbackQuery query) {
        var chatId = Long.parseLong(query.getData()
                .replace(LastkatkaBot.CALLBACK_ALLOW_CHAT, "")
                .replaceAll("title=.*$", ""));
        var title = query.getData().replaceAll("^.*?title=", "");
        Services.db().addAllowedChat(chatId, title);
        handler.allowedChats.add(chatId);
        Methods.editMessageText()
                .setChatId(query.getMessage().getChatId())
                .setText(Services.i18n().getString("chatAccepted", query.getFrom()))
                .setMessageId(query.getMessage().getMessageId())
                .setReplyMarkup(null)
                .call(handler);
        handler.sendMessage(chatId, Services.i18n().getString("helloMessage", Services.db().getChatLocale(chatId)));
    }

    public void denyChat(CallbackQuery query) {
        var chatId = Long.parseLong(query.getData().replace(LastkatkaBot.CALLBACK_DONT_ALLOW_CHAT, ""));
        Methods.editMessageText()
                .setChatId(query.getMessage().getChatId())
                .setMessageId(query.getMessage().getMessageId())
                .setText(Services.i18n().getString("chatDenied", query.getFrom()))
                .setReplyMarkup(null)
                .call(handler);
        handler.sendMessage(chatId, Services.i18n().getString("goodbyeMessage", Services.db().getChatLocale(chatId)));
        Methods.leaveChat(chatId).call(handler);
    }

    public void deleteChat(CallbackQuery query) {
        var chatId = Long.parseLong(query.getData().split(" ")[1]);
        Services.db().removeAllowedChat(chatId);
        Services.db().deleteBncGame(chatId);
        handler.allowedChats.remove(chatId);
        Methods.answerCallbackQuery()
                .setShowAlert(true)
                .setText(Services.i18n().getString("chatDeleted", Services.i18n().getLocale(query)))
                .setCallbackQueryId(query.getId())
                .call(handler);
        handler.sendMessage(chatId, Services.i18n().getString("chatDeletedMessage", Services.db().getChatLocale(chatId)));
        Methods.leaveChat(chatId).call(handler);
        Methods.deleteMessage(query.getMessage().getChatId(), query.getMessage().getMessageId()).call(handler);
    }

    public void deleteAdmin(CallbackQuery query) {
        var adminId = Integer.parseInt(query.getData().split(" ")[1]);
        Services.db().removeTGUser(adminId, DBService.COLLECTION_TYPE.ADMINS);
        handler.admins.remove(adminId);
        Methods.answerCallbackQuery()
                .setShowAlert(true)
                .setText(Services.i18n().getString("adminDeleted", Services.i18n().getLocale(query)))
                .setCallbackQueryId(query.getId())
                .call(handler);
        handler.sendMessage(adminId, Services.i18n().getString("adminDeletedMessage", Services.db().getUserLocale(adminId)));
        Methods.deleteMessage(query.getMessage().getChatId(), query.getMessage().getMessageId()).call(handler);
    }

    public void setLocale(CallbackQuery query) {
        var chatId = query.getMessage().getChatId();
        var locale = query.getData().split(" ")[1];
        if (query.getMessage().isUserMessage()) {
            Services.db().setUserLocale(query.getFrom().getId(), locale);
        } else {
            var admins = Methods.getChatAdministrators(chatId).call(handler);
            var adminsIds = new ArrayList<Integer>();
            for (var member : admins) {
                adminsIds.add(member.getUser().getId());
            }
            if (!adminsIds.contains(query.getFrom().getId())) {
                Methods.answerCallbackQuery()
                        .setText(Services.i18n().getString("notChatAdmin", locale))
                        .setShowAlert(true)
                        .setCallbackQueryId(query.getId())
                        .call(handler);
                return;
            }
            Services.db().setChatLocale(chatId, locale);
            Methods.deleteMessage(chatId, query.getMessage().getMessageId()).call(handler);
        }
        Methods.answerCallbackQuery()
                .setText(Services.i18n().getString("langSet", locale))
                .setCallbackQueryId(query.getId())
                .call(handler);
    }

    public void closeMenu(CallbackQuery query) {
        Methods.editMessageText()
                .setChatId(query.getMessage().getChatId())
                .setMessageId(query.getMessage().getMessageId())
                .setText(Services.i18n().getString("menuClosed", Services.i18n().getLocale(query)))
                .setReplyMarkup(null)
                .call(handler);
    }

    public enum CAKE_ACTIONS {CAKE_OK, CAKE_NOT}
}