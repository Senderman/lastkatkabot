package com.senderman.futurewars;

import com.annimon.tgbotsmodule.api.methods.Methods;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.inlinequery.ChosenInlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.InlineQuery;
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent;
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

class GameController {

    private static Map<Long, Game> games;
    private final FutureWarsHandler handler;

    GameController(FutureWarsHandler handler) {
        this.handler = handler;
        games = new HashMap<>();
    }

    static void stopjoin(long chatId) {
        games.remove(chatId);
    }

    static void endgame(Game game) {
        game.getTurnTimer().shutdown();
        games.remove(game.getChatId());
    }

    void create(Message message) {
        final var chatId = message.getChatId();
        if (games.containsKey(chatId)) {
            handler.sendMessage(chatId, "В этом чате игра уже начата, смотрите выше!");
            return;
        }

        var gameMsg = handler.sendMessage(chatId, "Набор в игру открыт! Отправьте /newteam чтобы создать команду!");
        var timer = new JoinTimer(chatId, handler);
        games.put(chatId, new Game(gameMsg.getChatId(), gameMsg.getMessageId(), timer, handler));
    }

    void newteam(Message message) {
        final var chatId = message.getChatId();
        if (!games.containsKey(chatId)) {
            handler.sendMessage(chatId, "В этом чате игра еще не началась!");
            return;
        }

        var game = games.get(chatId);

        if (game.getTeams().size() == 5) {
            handler.sendMessage(chatId, "Слишком много команд! Доступен только джоин в уже существующие!");
            return;
        }

        if (game.isStarted()) {
            handler.sendMessage(chatId, "Игра уже идет!");
            return;
        }

        var userId = message.getFrom().getId();

        if (isInGame(userId)) {
            handler.sendMessage(Methods.sendMessage(chatId, "Вы уже джойнулись куда-то!")
                    .setReplyToMessageId(message.getMessageId()));
            return;
        }

        try {
            handler.execute(new SendMessage((long) userId, "Вы успешно присоединились!"));
            var teamId = game.getTeams().keySet().size(); // increase teamId if exists
            while (game.getTeams().containsKey(teamId))
                teamId++;
            var player = new Player(userId, message.getFrom().getFirstName(), teamId);
            game.getPlayers().put(userId, player);
            Set<Player> playersSet = new HashSet<>();
            playersSet.add(player);
            game.getTeams().put(teamId, playersSet);
            handler.sendMessage(chatId, player.name + " успешно присоединился!");

            Methods.editMessageText()
                    .setChatId(chatId)
                    .setText(getTextForJoin(game))
                    .setMessageId(game.getMessageId())
                    .setReplyMarkup(getMarkupForJoin(game))
                    .setParseMode(ParseMode.HTML)
                    .call(handler);
        } catch (TelegramApiException e) {
            handler.sendMessage(Methods.sendMessage(chatId, "Сначала напишите боту что нибудь в лс!")
                    .setReplyToMessageId(message.getMessageId()));
        }
    }

    void begin(Message message) {
        final var chatId = message.getChatId();
        if (!games.containsKey(chatId)) {
            handler.sendMessage(chatId, "В этом чате игра еще не началась! Начать набор - /create");
            return;
        }

        var game = games.get(chatId);

        if (game.isStarted()) {
            handler.sendMessage(chatId, "В этом чате игра уже началась!");
            return;
        }
        if (game.getTeams().size() < 2) {
            handler.sendMessage(chatId, "Недостаточно команд!");
            return;
        }

        game.start();
        game.getJoinTimer().stop(false);

        var text = new StringBuilder();
        for (int teamId : game.getTeams().keySet()) {
            text.append(String.format("<b>Команда %1$d:</b>\n", teamId));

            for (Player player : game.getTeams().get(teamId)) {
                text.append("- ").append(player.name).append("\n");
            }
            text.append("\n");
        }
        Methods.editMessageText()
                .setChatId(game.getChatId())
                .setMessageId(game.getMessageId())
                .setText(text.toString())
                .setReplyMarkup(null)
                .setParseMode(ParseMode.HTML)
                .call(handler);

        handler.sendMessage(chatId, "Игра началась!\n" +
                "Вкл/выкл отчеты в личку - /pmreports (вкл по умолчанию)\n" +
                "Вкл/выкл отчеты в чат - /chatreports (вкл по умолчанию)");
    }

    void jointeam(CallbackQuery query) {
        var chatId = query.getMessage().getChatId();

        if (!games.containsKey(chatId))
            return;

        var game = games.get(chatId);
        var team = Integer.parseInt(query.getData().split(" ")[1]);

        if (!game.getTeams().containsKey(team))
            return;

        var userId = query.getFrom().getId();

        if (isInGame(userId)) {
            Methods.answerCallbackQuery()
                    .setShowAlert(true)
                    .setText("Вы уже джойнулись куда-то!")
                    .setCallbackQueryId(query.getId())
                    .call(handler);
            return;
        }

        if (game.getPlayers().size() == 25) {
            Methods.answerCallbackQuery()
                    .setShowAlert(true)
                    .setText("Слишком много игроков! (Макс. 25)")
                    .setCallbackQueryId(query.getId())
                    .call(handler);
            return;
        }

        var userName = query.getFrom().getFirstName();
        var player = new Player(userId, userName, team);

        try {
            handler.execute(new SendMessage((long) userId, "Вы успешно присоединились!"));
            game.getPlayers().put(userId, player);
            game.getTeams().get(team).add(player);
            Methods.answerCallbackQuery()
                    .setShowAlert(false)
                    .setText("Вы успешно присоединились!")
                    .setCallbackQueryId(query.getId())
                    .call(handler);
            handler.sendMessage(chatId, player.name + " успешно присоединился!");
            Methods.editMessageText()
                    .setChatId(chatId)
                    .setText(getTextForJoin(game))
                    .setMessageId(game.getMessageId())
                    .setParseMode(ParseMode.HTML)
                    .setReplyMarkup(getMarkupForJoin(game))
                    .call(handler);

        } catch (TelegramApiException e) {
            Methods.answerCallbackQuery()
                    .setShowAlert(true)
                    .setText("Сначала напишите боту что нибудь в лс!")
                    .setCallbackQueryId(query.getId())
                    .call(handler);
        }

    }

    void escape(Message message) {
        final var chatId = message.getChatId();
        if (!games.containsKey(chatId)) {
            handler.sendMessage(chatId, "В этом чате игра еще не началась!");
            return;
        }

        var game = games.get(chatId);

        if (game.isStarted()) {
            handler.sendMessage(chatId, "Нельзя уйти из начавшейся игры!");
            return;
        }

        var player = game.getPlayers().remove(message.getFrom().getId());
        if (player == null) {
            handler.sendMessage(chatId, "Вас нет в списке!");
            return;
        }
        var team = game.getTeams().get(player.team);
        team.remove(player);
        if (team.size() == 0)
            game.getTeams().remove(player.team);


        handler.sendMessage(chatId, player.name + " убежал!");
        Methods.editMessageText()
                .setChatId(chatId)
                .setText(getTextForJoin(game))
                .setMessageId(game.getMessageId())
                .setParseMode(ParseMode.HTML)
                .setReplyMarkup(getMarkupForJoin(game))
                .call(handler);

    }

    void attack(CallbackQuery query) {

        var params = query.getData().split(" ");
        var chatId = Long.parseLong(params[2]);

        if (!games.containsKey(chatId))
            return;

        var game = games.get(chatId);
        var targetId = Integer.parseInt(params[1]);
        game.setTarget(query.getFrom().getId(), targetId);
        game.doAction(query.getFrom().getId(), Player.ACTION.ATTACK);
    }

    void showTargets(CallbackQuery query) {
        var chatId = Long.parseLong(query.getData().split(" ")[1]);

        if (!games.containsKey(chatId))
            return;

        var game = games.get(chatId);
        game.showTargets(query.getFrom().getId());
    }

    void simpleAction(CallbackQuery query, Player.ACTION action, boolean isFinalAction) {
        var chatId = Long.parseLong(query.getData().split(" ")[1]);

        if (!games.containsKey(chatId))
            return;

        var game = games.get(chatId);

        if (isFinalAction) {
            game.doAction(query.getFrom().getId(), action);
        } else {
            if (action == Player.ACTION.DEFENCE || action == Player.ACTION.DEF_FLAG)
                game.setupShield(query.getFrom().getId(), action);
        }
    }

    void updateShield(CallbackQuery query) {
        var params = query.getData().split(" ");

        var chatId = Long.parseLong(params[2]);
        if (!games.containsKey(chatId))
            return;

        var game = games.get(chatId);
        var player = game.getPlayers().get(query.getFrom().getId());
        var defence = Integer.parseInt(params[1]);
        if (defence > player.shield)
            defence = player.shield;
        player.currentShield += defence;
        if (player.currentShield > 5)
            player.currentShield = 5;
        else if (player.currentShield < 0)
            player.currentShield = 0;
        game.setupShield(query.getFrom().getId(), player.action);
    }

    void showMainMenu(CallbackQuery query) {
        var chatId = Long.parseLong(query.getData().split(" ")[1]);

        if (!games.containsKey(chatId))
            return;

        var game = games.get(chatId);
        game.sendMainMenu(query.getFrom().getId());
    }

    void handleTeamMessage(InlineQuery query) {
        Game game = null;
        for (Game g : games.values()) {
            if (g.getPlayers().containsKey(query.getFrom().getId())) {
                game = g;
                break;
            }
        }
        if (game == null) {
            var content = new InputTextMessageContent()
                    .setMessageText("Я пытался сказать что-то, не играя в игру");
            var result = new InlineQueryResultArticle()
                    .setId("deny_team_message")
                    .setTitle("Вы не в игре!")
                    .setInputMessageContent(content);
            Methods.answerInlineQuery()
                    .setInlineQueryId(query.getId())
                    .setResults(result)
                    .call(handler);
            return;
        }
        var content = new InputTextMessageContent()
                .setMessageText("Я что-то сказал своей команде");
        var result = new InlineQueryResultArticle()
                .setId("send_to_team")
                .setTitle("Отправить команде")
                .setDescription(query.getQuery())
                .setInputMessageContent(content);
        Methods.answerInlineQuery()
                .setInlineQueryId(query.getId())
                .setResults(result)
                .call(handler);
    }

    void sendTeamMessage(ChosenInlineQuery query) {
        Game game = null;
        for (Game g : games.values()) {
            if (g.getPlayers().containsKey(query.getFrom().getId())) {
                game = g;
                break;
            }
        }
        if (game == null)
            return;

        int team = game.getPlayers().get(query.getFrom().getId()).team;
        for (Player teammate : game.getTeams().get(team)) {
            if (teammate.id > 0) {
                handler.sendMessage(teammate.id, "✉️" + query.getFrom().getFirstName() + ": " + query.getQuery());
            }
        }
    }

    void pmReports(Message message) {
        var chatId = message.getChatId();
        if (!games.containsKey(chatId))
            return;

        var game = games.get(chatId);
        var playerId = message.getFrom().getId();
        if (!game.getPlayers().containsKey(playerId))
            return;

        var player = game.getPlayers().get(playerId);
        player.pmReports = !player.pmReports;
        if (player.pmReports)
            handler.sendMessage(chatId, "Режим отчетов в лс для " + player.name + " включен!");
        else
            handler.sendMessage(chatId, "Режим отчетов в лс для " + player.name + " отключен!");
    }

    void chatReports(Message message) {
        var chatId = message.getChatId();
        if (!games.containsKey(chatId))
            return;

        var game = games.get(chatId);
        game.chatReports = !game.chatReports;
        if (game.chatReports)
            handler.sendMessage(chatId, "Отчеты в группу включены!");
        else
            handler.sendMessage(chatId, "Отчеты в группу выключены!");
    }

    private boolean isInGame(int playerId) {
        for (Game game : games.values()) {
            if (game.getPlayers().containsKey(playerId))
                return true;
        }
        return false;
    }

    private String getTextForJoin(Game game) {
        var text = new StringBuilder();
        if (game.getTeams().size() < 2)
            text.append("Набор в игру открыт! Отправьте /newteam чтобы создать команду!\n\n");
        else
            text.append("Набор в игру открыт! Отправьте /newteam чтобы создать команду, или присоединитесь к существующей!\n\n");

        for (int teamId : game.getTeams().keySet()) {
            text.append(String.format("<b>Команда %1$d:</b>\n", teamId));

            for (Player player : game.getTeams().get(teamId)) {
                text.append("- ").append(player.name).append("\n");
            }
            text.append("\n");
        }

        return text.toString();
    }

    private InlineKeyboardMarkup getMarkupForJoin(Game game) {
        if (game.getTeams().size() < 2) {
            return null;
        }

        var markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        for (int team : game.getTeams().keySet()) {
            var row = List.of(new InlineKeyboardButton()
                    .setText("Джоин в команду " + team)
                    .setCallbackData(FutureWarsBot.CALLBACK_JOIN_TEAM + team));
            buttons.add(row);
        }
        markup.setKeyboard(buttons);
        return markup;
    }
}
