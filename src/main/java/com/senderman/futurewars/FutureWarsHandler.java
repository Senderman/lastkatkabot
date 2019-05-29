package com.senderman.futurewars;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Locale;

public class FutureWarsHandler extends BotHandler {

    private final GameController controller;
    private final BotConfig botConfig;

    FutureWarsHandler(BotConfig botConfig) {
        controller = new GameController(this);
        this.botConfig = botConfig;
    }

    @Override
    protected BotApiMethod onUpdate(@NotNull Update update) {

        if (update.hasCallbackQuery()) {
            var query = update.getCallbackQuery();
            var dataStart = query.getData().split(" ")[0] + " ";
            switch (dataStart) {
                case FutureWarsBot.CALLBACK_ATTACK:
                    controller.attack(query);
                    break;
                case FutureWarsBot.CALLBACK_DEFENCE:
                    controller.simpleAction(query, Player.ACTION.DEFENCE, false);
                    break;
                case FutureWarsBot.CALLBACK_FLAG_DEFENCE:
                    controller.simpleAction(query, Player.ACTION.DEF_FLAG, false);
                    break;
                case FutureWarsBot.CALLBACK_CHARGE_LASER:
                    controller.simpleAction(query, Player.ACTION.CHARGE_LASER, true);
                    break;
                case FutureWarsBot.CALLBACK_CHARGE_SHIELD:
                    controller.simpleAction(query, Player.ACTION.CHARGE_SHIELD, true);
                    break;
                case FutureWarsBot.CALLBACK_CONFIRM_DEFENCE:
                    controller.simpleAction(query, Player.ACTION.DEFENCE, true);
                    break;
                case FutureWarsBot.CALLBACK_CONFIRM_FLAG_DEFENCE:
                    controller.simpleAction(query, Player.ACTION.DEF_FLAG, true);
                    break;
                case FutureWarsBot.CALLBACK_SUMMON_CLONE:
                    controller.simpleAction(query, Player.ACTION.SUMMON_CLONE, true);
                    break;
                case FutureWarsBot.CALLBACK_ROLL:
                    controller.simpleAction(query, Player.ACTION.ROLL, true);
                    break;
                case FutureWarsBot.CALLBACK_SELECT_TARGET:
                    controller.showTargets(query);
                    break;
                case FutureWarsBot.CALLBACK_MAIN_MENU:
                    controller.showMainMenu(query);
                    break;
                case FutureWarsBot.CALLBACK_DEFENCE_VALUE:
                    controller.updateShield(query);
                    break;
                case FutureWarsBot.CALLBACK_JOIN_TEAM:
                    controller.jointeam(query);
                    break;
            }
            return null;
        }

        if (update.hasInlineQuery()) {
            var query = update.getInlineQuery();
            controller.handleTeamMessage(query);
            return null;
        }

        if (update.hasChosenInlineQuery()) {
            var query = update.getChosenInlineQuery();
            if (query.getResultId().equals("send_to_team"))
                controller.sendTeamMessage(query);
            return null;
        }

        if (!update.hasMessage())
            return null;

        final var message = update.getMessage();

        if (!message.hasText())
            return null;

        if (message.getDate() + 120 < System.currentTimeMillis() / 1000)
            return null;

        final var chatId = message.getChatId();

        final var command = message.getText().split("\\s+", 2)[0]
                .toLowerCase(Locale.ENGLISH).replace("@" + getBotUsername(), "");

        if (command.contains("@")) // check if the command is for bot or for all bots
            return null;

        if (command.equals("/help")) {
            try {
                execute(new SendMessage((long) message.getFrom().getId(), botConfig.getHelp()).setParseMode(ParseMode.HTML));
                if (!message.isUserMessage())
                    sendMessage(Methods.sendMessage(chatId, "Помощь отправлена вам в лс!")
                            .setReplyToMessageId(message.getMessageId()));
            } catch (TelegramApiException e) {
                sendMessage(Methods.sendMessage(chatId, "Сначала напишите боту в лс!")
                        .setReplyToMessageId(message.getMessageId()));
            }
        }

        if (message.isUserMessage())
            return null;

        switch (command) {
            case "/create":
                controller.create(message);
                break;
            case "/newteam":
                controller.newteam(message);
                break;
            case "/escape":
                controller.escape(message);
                break;
            case "/begin":
                controller.begin(message);
                break;
            case "/pmreports":
                controller.pmReports(message);
                break;
            case "/chatreports":
                controller.chatReports(message);
                break;
        }

        return null;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername().split(" ")[botConfig.getPosition()];
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken().split(" ")[botConfig.getPosition()];
    }

    Message sendMessage(long chatId, String text) {
        return sendMessage(Methods.sendMessage(chatId, text));
    }

    Message sendMessage(SendMessageMethod sm) {
        return sm
                .disableWebPagePreview()
                .enableHtml(true)
                .call(this);
    }
}
