package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod;
import com.senderman.Command;
import com.senderman.lastkatkabot.handlers.*;
import com.senderman.lastkatkabot.tempobjects.BullsAndCowsGame;
import com.senderman.lastkatkabot.tempobjects.Duel;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.lang.reflect.Method;
import java.util.*;

public class LastkatkaBotHandler extends BotHandler {

    public final Set<Integer> admins;
    public final Set<Integer> blacklist;
    public final Set<Integer> premiumUsers;
    public final Set<Long> allowedChats;
    public final Map<Long, BullsAndCowsGame> bullsAndCowsGames;
    public final Map<Long, Map<Integer, Duel>> duels;
    public final CommandListener commandListener;
    private final Map<String, Method> commands;
    private final AdminHandler adminHandler;
    private final CallbackHandler callbackHandler;

    LastkatkaBotHandler() {

        var mainAdmin = Services.config().getMainAdmin();
        sendMessage(mainAdmin, "Initialization...");

        // settings
        Services.setHandler(this);
        Services.setDBService(new MongoDBService());
        Services.db().cleanup();

        admins = Services.db().getTgUsersIds(DBService.COLLECTION_TYPE.ADMINS);
        premiumUsers = Services.db().getTgUsersIds(DBService.COLLECTION_TYPE.PREMIUM);
        blacklist = Services.db().getTgUsersIds(DBService.COLLECTION_TYPE.BLACKLIST);

        allowedChats = Services.db().getAllowedChatsSet();
        allowedChats.add(Services.config().getLastvegan());
        allowedChats.add(Services.config().getTourgroup());

        commandListener = new CommandListener(this);
        commands = new HashMap<>();
        adminHandler = new AdminHandler(this);
        callbackHandler = new CallbackHandler(this);
        bullsAndCowsGames = Services.db().getBnCGames();
        duels = new HashMap<>();

        // init command-method map
        for (var m : commandListener.getClass().getDeclaredMethods()) {
            if (m.isAnnotationPresent(Command.class))
                commands.put(m.getAnnotation(Command.class).name(), m);
        }

        sendMessage(mainAdmin, "Бот готов к работе!");
    }

    @Override
    public BotApiMethod onUpdate(@NotNull Update update) {

        // first we will handle callbacks
        if (update.hasCallbackQuery()) {
            processCallbackQuery(update.getCallbackQuery());
            return null;
        }

        if (!update.hasMessage())
            return null;

        final var message = update.getMessage();

        // don't handle old messages
        if (message.getDate() + 120 < System.currentTimeMillis() / 1000)
            return null;

        var newMembers = message.getNewChatMembers();

        if (newMembers != null && newMembers.size() != 0) {
            processNewMembers(message);
            return null;
        }

        final var chatId = message.getChatId();

        if (message.getMigrateFromChatId() != null && allowedChats.contains(message.getMigrateFromChatId())) {
            migrateChat(message.getMigrateFromChatId(), chatId);
            sendMessage(message.getMigrateFromChatId(), "Id чата обновлен!");
        }

        if (!allowedChats.contains(chatId) && !message.isUserMessage()) // do not respond in not allowed chats
            return null;

        if (message.getMigrateFromChatId() != null) {
            migrateChat(message.getMigrateFromChatId(), chatId);
            return null;
        }

        if (message.getLeftChatMember() != null && !message.getLeftChatMember().getUserName().equals(getBotUsername()) && !message.getChatId().equals(Services.config().getTourgroup())) {
            Methods.sendDocument()
                    .setChatId(chatId)
                    .setFile(Services.config().getLeavesticker())
                    .setReplyToMessageId(message.getMessageId())
                    .call(this);
            Services.db().removeUserFromChatDB(message.getLeftChatMember().getId(), chatId);
        }

        if (!message.hasText())
            return null;

        if (message.isGroupMessage() || message.isSuperGroupMessage()) // add user to DB
            Services.db().addUserToChatDB(message);

        var text = message.getText();

        // for bulls and cows
        if (text.matches("\\d{4,10}") && bullsAndCowsGames.containsKey(chatId) && isNotInBlacklist(message)) {
            bullsAndCowsGames.get(chatId).check(message);
            return null;
        }

        if (!message.isCommand())
            return null;

        /* bot should only trigger on general commands (like /command) or on commands for this bot (/command@mybot),
         * and NOT on commands for another bots (like /command@notmybot)
         */

        final var command = text.split("\\s+", 2)[0]
                .toLowerCase(Locale.ENGLISH)
                .replace("@" + getBotUsername(), "");

        if (command.contains("@"))
            return null;

        try {
            var m = commands.get(command);
            var annotation = m.getAnnotation(Command.class);
            if (isFromAdmin(message) && annotation.forAllAdmins()
                    || message.getFrom().getId().equals(Services.config().getMainAdmin()) && annotation.forMainAdmin()
                    || isNotInBlacklist(message)
            )
                m.invoke(commandListener, message);
        } catch (Exception e) {
            return null;
        }

        /* TODO implement commands for tournament
        if (TournamentHandler.isEnabled && isFromAdmin(message)) {
            switch (command) {
                case "/score":
                    TournamentHandler.score(message, this);
                    return null;
                case "/win":
                    TournamentHandler.win(message, this);
                    return null;
                case "/rt":
                    TournamentHandler.rt(this);
            }
        }*/
        return null;
    }

    @Override
    public String getBotUsername() {
        return Services.config().getUsername().split(" ")[Services.config().getPosition()];
    }

    @Override
    public String getBotToken() {
        return Services.config().getToken().split(" ")[Services.config().getPosition()];
    }

    private void processCallbackQuery(CallbackQuery query) {
        String data = query.getData();

        if (data.startsWith(LastkatkaBot.CALLBACK_CAKE_OK)) {
            callbackHandler.cake(query, CallbackHandler.CAKE_ACTIONS.CAKE_OK);
        } else if (data.startsWith(LastkatkaBot.CALLBACK_CAKE_NOT)) {
            callbackHandler.cake(query, CallbackHandler.CAKE_ACTIONS.CAKE_NOT);
        } else if (data.startsWith(LastkatkaBot.CALLBACK_ALLOW_CHAT)) {
            callbackHandler.addChat(query);
        } else if (data.startsWith(LastkatkaBot.CALLBACK_DONT_ALLOW_CHAT)) {
            callbackHandler.denyChat(query);
        } else if (data.startsWith(LastkatkaBot.CALLBACK_DELETE_CHAT)) {
            callbackHandler.deleteChat(query);
            adminHandler.chats(query.getMessage());
        } else if (data.startsWith("deleteuser_")) {
            DBService.COLLECTION_TYPE type;
            switch (query.getData().split(" ")[0]) {
                case LastkatkaBot.CALLBACK_DELETE_ADMIN:
                    type = DBService.COLLECTION_TYPE.ADMINS;
                    break;
                case LastkatkaBot.CALLBACK_DELETE_NEKO:
                    type = DBService.COLLECTION_TYPE.BLACKLIST;
                    break;
                case LastkatkaBot.CALLBACK_DELETE_PREM:
                    type = DBService.COLLECTION_TYPE.PREMIUM;
                    break;
                default:
                    return;
            }
            callbackHandler.deleteUser(query, type);
            adminHandler.listUsers(query.getMessage(), type);
        } else {
            switch (data) {
                case LastkatkaBot.CALLBACK_REGISTER_IN_TOURNAMENT:
                    callbackHandler.registerInTournament(query);
                    return;
                case LastkatkaBot.CALLBACK_PAY_RESPECTS:
                    callbackHandler.payRespects(query);
                    return;
                case LastkatkaBot.CALLBACK_CLOSE_MENU:
                    callbackHandler.closeMenu(query);
                    return;
                case LastkatkaBot.CALLBACK_JOIN_DUEL:
                    var chat = duels.get(query.getMessage().getChatId());
                    if (chat == null) {
                        Duel.answerCallbackQuery(query, "⏰ Дуэль устарела!", true);
                        return;
                    }
                    var duel = chat.get(query.getMessage().getMessageId());
                    if (duel == null) {
                        Duel.answerCallbackQuery(query, "⏰ Дуэль устарела!", true);
                        return;
                    }
                    duel.join(query);
                    return;
                case LastkatkaBot.CALLBACK_VOTE_BNC:
                    bullsAndCowsGames.get(query.getMessage().getChatId()).addVote(query);
            }
        }
    }

    private void processNewMembers(Message message) {
        var chatId = message.getChatId();
        var newMembers = message.getNewChatMembers();

        if (chatId == Services.config().getTourgroup()) { // restrict any user who isn't in tournament
            for (User user : newMembers) {
                if (TournamentHandler.membersIds == null || !TournamentHandler.membersIds.contains(user.getId())) {
                    Methods.Administration.restrictChatMember()
                            .setChatId(Services.config().getTourgroup())
                            .setUserId(user.getId())
                            .setCanSendMessages(false).call(this);
                }
            }

        } else if (!newMembers.get(0).getBot()) {
            Methods.sendDocument(chatId)
                    .setFile(Services.config().getHigif())
                    .setReplyToMessageId(message.getMessageId())
                    .call(this); // say hi to new member

        } else if (newMembers.get(0).getUserName().equals(getBotUsername())) {
            if (allowedChats.contains(chatId)) {// Say hello to new group if chat is allowed
                sendMessage(chatId, "Этот чат находится в списке разрешенных. Бот готов к работе здесь");
                return;
            }

            sendMessage(chatId, "Чата нет в списке разрешенных. Дождитесь решения разработчика");
            var row1 = List.of(new InlineKeyboardButton()
                    .setText("Добавить")
                    .setCallbackData(LastkatkaBot.CALLBACK_ALLOW_CHAT + chatId));
            var row2 = List.of(new InlineKeyboardButton()
                    .setText("Отклонить")
                    .setCallbackData(LastkatkaBot.CALLBACK_DONT_ALLOW_CHAT + chatId));
            var markup = new InlineKeyboardMarkup();
            markup.setKeyboard(List.of(row1, row2));
            sendMessage(Methods.sendMessage(Services.config().getMainAdmin(),
                    String.format("Добавить чат %1$s (%2$d) в список разрешенных? - %3$s",
                            message.getChat().getTitle(), chatId, message.getFrom().getFirstName()))
                    .setReplyMarkup(markup));
        }
    }

    private boolean processAdminCommand(Message message, String command) {
        switch (command) {
            case "/setup":
                TournamentHandler.setup(message, this);
                return true;
            case "/go":
                TournamentHandler.startTournament(this);
                return true;
            case "/ct":
                TournamentHandler.cancelSetup(this);
                return true;
            case "/tourmessage":
                TournamentHandler.tourmessage(this, message);
                return true;
        }
        return false;
    }

    private boolean isFromAdmin(Message message) {
        return admins.contains(message.getFrom().getId());
    }

    // TODO uncomment when needed
    /*private boolean isPremiumUser(Message message) {
        return premiumUsers.contains(message.getFrom().getId());
    }*/

    private boolean isNotInBlacklist(Message message) {
        var result = blacklist.contains(message.getFrom().getId());
        if (result) {
            Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(this);
        }
        return !result;
    }

    private void migrateChat(long oldChatId, long newChatId) {
        allowedChats.remove(oldChatId);
        allowedChats.add(newChatId);
        Services.db().updateChatId(oldChatId, newChatId);
    }

    private boolean isAbleToMigrateChat(long oldChatId, TelegramApiException e) {
        if (!(e instanceof TelegramApiRequestException))
            return false;

        var ex = (TelegramApiRequestException) e;
        if (ex.getParameters() == null)
            return false;
        if (ex.getParameters().getMigrateToChatId() == null)
            return false;

        migrateChat(oldChatId, ex.getParameters().getMigrateToChatId());
        return true;
    }

    public Message sendMessage(long chatId, String text) {
        return sendMessage(Methods.sendMessage(chatId, text));
    }

    public Message sendMessage(SendMessageMethod sm) {
        var sendMessage = new SendMessage(sm.getChatId(), sm.getText())
                .enableHtml(true)
                .disableWebPagePreview()
                .setReplyMarkup(sm.getReplyMarkup())
                .setReplyToMessageId(sm.getReplyToMessageId());
        try {
            return execute(sendMessage);
        } catch (TelegramApiException e) {
            if (!isAbleToMigrateChat(Long.parseLong(sm.getChatId()), e))
                return null;
            sm.setChatId(((TelegramApiRequestException) e).getParameters().getMigrateToChatId());
            return sm.call(this);
        }
    }
}
