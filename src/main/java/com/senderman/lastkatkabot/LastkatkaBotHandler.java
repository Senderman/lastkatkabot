package com.senderman.lastkatkabot;

import com.annimon.tgbotsmodule.BotHandler;
import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.api.methods.send.SendMessageMethod;
import com.senderman.lastkatkabot.Handlers.*;
import com.senderman.lastkatkabot.TempObjects.BullsAndCowsGame;
import com.senderman.lastkatkabot.TempObjects.RelayGame;
import com.senderman.lastkatkabot.TempObjects.VeganTimer;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class LastkatkaBotHandler extends BotHandler {

    public final Set<Integer> admins;
    public final Set<Integer> blacklist;
    public final Set<Integer> premiumUsers;
    public final Set<Long> allowedChats;
    public final Map<Long, VeganTimer> veganTimers;
    public final Map<Long, BullsAndCowsGame> bullsAndCowsGames;
    public final Map<Long, RelayGame> relayGames;
    private final AdminHandler adminHandler;
    private final UsercommandsHandler usercommandsHandler;
    private final DuelController duelController;
    private CallbackHandler callbackHandler;

    LastkatkaBotHandler() {

        var mainAdmin = Services.botConfig().getMainAdmin();
        sendMessage(mainAdmin, "Initialization...");

        // settings
        Services.setHandler(this);
        Services.setDBService(new MongoDBService());
        Services.setLocalization(new LastResourceBundleLocalizationService("Language", Services.db()));

        admins = Services.db().getTgUsersIds(DBService.COLLECTION_TYPE.ADMINS);
        premiumUsers = Services.db().getTgUsersIds(DBService.COLLECTION_TYPE.PREMIUM);
        blacklist = Services.db().getTgUsersIds(DBService.COLLECTION_TYPE.BLACKLIST);

        allowedChats = Services.db().getAllowedChatsSet();
        allowedChats.add(Services.botConfig().getLastvegan());
        allowedChats.add(Services.botConfig().getTourgroup());

        adminHandler = new AdminHandler(this);
        usercommandsHandler = new UsercommandsHandler(this);
        callbackHandler = new CallbackHandler(this);
        duelController = new DuelController(this);
        veganTimers = new HashMap<>();
        bullsAndCowsGames = Services.db().getBnCGames();
        relayGames = new HashMap<>();

        sendMessage(mainAdmin,
                Services.i18n().getString("botIsReady", Services.db().getUserLocale(mainAdmin)));
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

        if (!allowedChats.contains(chatId) && !message.isUserMessage()) // do not respond in not allowed chats
            return null;

        if (message.getLeftChatMember() != null) {
            Methods.sendDocument()
                    .setChatId(chatId)
                    .setFile(Services.botConfig().getLeavesticker())
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

        // for relay
        if (message.isUserMessage() && text.matches("\\p{L}{4,10}")) {
            RelayGame game = null;
            for (var gameChatId : relayGames.keySet()) {
                if (relayGames.get(gameChatId).players.contains(message.getFrom().getId())) {
                    game = relayGames.get(gameChatId);
                }
            }
            if (game != null) {
                if (game.needToAskLeader && message.getFrom().getId() == game.leaderId) {
                    game.checkLeaderWord(message);
                    return null;
                } else if (game.isGoing && !game.needToAskLeader) {
                    game.checkWord(message);
                    return null;
                }
            }
        }

        if (chatId.equals(-1001339940111L) && !message.isCommand()) {
            var t = message.getFrom().getFirstName() + ": " + text;
            if (message.isReply())
                t += "\n>> " + message.getReplyToMessage().getFrom().getFirstName() + ": " + message.getReplyToMessage().getText();
            sendMessage(Services.botConfig().getMainAdmin(), t);
        }

        if (!message.isCommand())
            return null;

        if (!message.isUserMessage()) { // handle other's bots commands
            if (processVeganCommands(message))
                return null;
        }

        /* bot should only trigger on general commands (like /command) or on commands for this bot (/command@mybot),
         * and NOT on commands for another bots (like /command@notmybot)
         */

        final var command = text.split("\\s+", 2)[0].toLowerCase(Locale.ENGLISH).replace("@" + getBotUsername(), "");

        if (command.contains("@"))
            return null;

        if (isNotInBlacklist(message) && processUserCommand(message, command))
            return null;

        // commands for main admin only
        if (message.getFrom().getId().equals(Services.botConfig().getMainAdmin()) && processMainAdminCommand(message, command))
            return null;

        // commands for all admins
        if (isFromAdmin(message) && processAdminCommand(message, command))
            return null;

        // commands for tournament
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
        }
        return null;
    }

    @Override
    public String getBotUsername() {
        return Services.botConfig().getUsername().split(" ")[Services.botConfig().getPosition()];
    }

    @Override
    public String getBotToken() {
        return Services.botConfig().getToken().split(" ")[Services.botConfig().getPosition()];
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
        } else if (data.startsWith(LastkatkaBot.CALLBACK_DELETE_ADMIN)) {
            callbackHandler.deleteAdmin(query);
            adminHandler.listOwners(query.getMessage());
        } else if (data.startsWith(LastkatkaBot.CALLBACK_SET_LANG)) {
            callbackHandler.setLocale(query);
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
                    duelController.joinDuel(query);
                    return;
                case LastkatkaBot.CALLBACK_VOTE_BNC:
                    bullsAndCowsGames.get(query.getMessage().getChatId()).addVote(query);
            }
        }
    }

    private void processNewMembers(Message message) {
        var chatId = message.getChatId();
        var newMembers = message.getNewChatMembers();

        if (chatId == Services.botConfig().getTourgroup()) { // restrict any user who isn't in tournament
            for (User user : newMembers) {
                if (TournamentHandler.membersIds == null || !TournamentHandler.membersIds.contains(user.getId())) {
                    Methods.Administration.restrictChatMember()
                            .setChatId(Services.botConfig().getTourgroup())
                            .setUserId(user.getId())
                            .setCanSendMessages(false).call(this);
                }
            }

        } else if (!newMembers.get(0).getUserName().equalsIgnoreCase(getBotUsername()) && !newMembers.get(0).getBot()) {
            Methods.sendDocument(chatId)
                    .setFile(Services.botConfig().getHigif())
                    .setReplyToMessageId(message.getMessageId())
                    .call(this); // say hi to new member

        } else {
            var locale = Services.i18n().getLocale(message);
            if (allowedChats.contains(chatId)) {// Say hello to new group if chat is allowed
                sendMessage(chatId, Services.i18n().getString("chatAllowed", locale));
                return;
            }

            sendMessage(chatId, Services.i18n().getString("chatNotAllowed", locale));
            var mainAdminLocale = Services.db().getUserLocale(Services.botConfig().getMainAdmin());
            var row1 = List.of(new InlineKeyboardButton()
                    .setText(Services.i18n().getString("acceptChat", mainAdminLocale))
                    .setCallbackData(LastkatkaBot.CALLBACK_ALLOW_CHAT + chatId + "title=" + message.getChat().getTitle()));
            var row2 = List.of(new InlineKeyboardButton()
                    .setText(Services.i18n().getString("denyChat", mainAdminLocale))
                    .setCallbackData(LastkatkaBot.CALLBACK_DONT_ALLOW_CHAT + chatId));
            var markup = new InlineKeyboardMarkup();
            markup.setKeyboard(List.of(row1, row2));
            sendMessage(Methods.sendMessage((long) Services.botConfig().getMainAdmin(),
                    String.format(Services.i18n().getString("addChat", mainAdminLocale),
                            message.getChat().getTitle(), chatId, message.getFrom().getFirstName()))
                    .setReplyMarkup(markup));
        }
    }

    private boolean processVeganCommands(Message message) {
        var chatId = message.getChatId();
        var text = message.getText();

        if (Services.botConfig().getVeganWarsCommands().contains(text) && !veganTimers.containsKey(chatId)) { // start veganwars timer
            veganTimers.put(chatId, new VeganTimer(chatId));
            return true;

        } else if (text.startsWith("/join") && veganTimers.containsKey(chatId)) {
            veganTimers.get(chatId).addPlayer(message.getFrom().getId(), message);
            return true;

        } else if (text.startsWith("/flee") && veganTimers.containsKey(chatId)) {
            veganTimers.get(chatId).removePlayer(message.getFrom().getId());
            return true;

        } else if (text.startsWith("/fight") && veganTimers.containsKey(chatId)) {
            if (veganTimers.get(chatId).getVegansAmount() > 1) {
                veganTimers.get(chatId).stop();
            }
            return true;
        }
        return false;
    }

    private boolean processUserCommand(Message message, String command) {
        var chatId = message.getChatId();

        switch (command) {
            case "/pinlist":
                usercommandsHandler.pinlist(message);
                return true;
            case "/pair":
                usercommandsHandler.pair(message);
                return true;
            case "/lastpairs":
                usercommandsHandler.lastpairs(message);
                return true;
            case "/action":
                usercommandsHandler.action(message);
                return true;
            case "/f":
                usercommandsHandler.payRespects(message);
                return true;
            case "/dice":
                usercommandsHandler.dice(message);
                return true;
            case "/cake":
                usercommandsHandler.cake(message);
                return true;
            case "/duel":
                duelController.createNewDuel(message);
                return true;
            case "/stats":
                usercommandsHandler.dstats(message);
                return true;
            case "/bnc":
                if (!bullsAndCowsGames.containsKey(chatId))
                    bullsAndCowsGames.put(chatId, new BullsAndCowsGame(message));
                else
                    sendMessage(chatId, Services.i18n().getString("bncIsGoing", Services.db().getChatLocale(chatId)));
                return true;
            case "/bncinfo":
                if (bullsAndCowsGames.containsKey(chatId))
                    bullsAndCowsGames.get(chatId).sendGameInfo(message);
                return true;
            case "/bncstop":
                if (bullsAndCowsGames.containsKey(chatId))
                    bullsAndCowsGames.get(chatId).createPoll(message);
                return true;
            case "/bncruin":
                if (bullsAndCowsGames.containsKey(chatId))
                    bullsAndCowsGames.get(chatId).changeAntiRuin();
                return true;
            case "/bnchelp":
                usercommandsHandler.bnchelp(message);
                return true;
            case "/relay":
                if (message.isUserMessage())
                    return true;
                if (relayGames.containsKey(chatId))
                    sendMessage(chatId, "В этом чате игра уже идет!");
                else
                    relayGames.put(chatId, new RelayGame(message));
                return true;
            case "/joinrelay":
                if (relayGames.containsKey(chatId)) {
                    for (var game : relayGames.values()) {
                        if (game.players.contains(message.getFrom().getId())) {
                            sendMessage(chatId, "Вы уже в игре в одном из чатов!");
                            return true;
                        }
                    }
                    relayGames.get(chatId).addPlayer(message);
                }
                return true;
            case "/leaverelay":
                if (relayGames.containsKey(chatId))
                    relayGames.get(chatId).kickPlayer(message);
                return true;
            case "/startrelay":
                if (relayGames.containsKey(chatId) && !relayGames.get(chatId).isGoing)
                    relayGames.get(chatId).startGame();
                return true;
            case "/relayhelp":
                sendMessage(chatId, RelayGame.relayHelp());
                return true;
            case "/reset":
                if (veganTimers.containsKey(chatId)) {
                    veganTimers.get(chatId).stop();
                    sendMessage(chatId, Services.i18n().getString("vegansRemoved", Services.db().getChatLocale(chatId)));
                }
                return true;
            case "/feedback":
                usercommandsHandler.feedback(message);
                return true;
            case "/help":
                usercommandsHandler.help(message);
                return true;
            case "/setlocale":
                usercommandsHandler.setLocale(message);
                return true;
            case "/getinfo":
                usercommandsHandler.getinfo(message);
                return true;
            case "/regtest":
                usercommandsHandler.testRegex(message);
                return true;
        }
        return false;
    }

    private boolean processMainAdminCommand(Message message, String command) {
        switch (command) {
            case "/owner":
                adminHandler.addOwner(message);
                return true;
            case "/addpremium":
                adminHandler.addPremium(message);
                return true;
            case "/update":
                adminHandler.update(message);
                return true;
            case "/announce":
                adminHandler.announce(message);
                return true;
            case "/chats":
                adminHandler.chats(message);
                return true;
            case "/cc":
                adminHandler.cleanChats(message);
                return true;
        }
        return false;
    }

    private boolean processAdminCommand(Message message, String command) {
        switch (command) {
            case "/badneko":
                adminHandler.badneko(message);
                return true;
            case "/goodneko":
                adminHandler.goodneko(message);
                return true;
            case "/nekos":
                adminHandler.nekos(message);
                return true;
            case "/owners":
                adminHandler.listOwners(message);
                return true;
            case "/critical":
                duelController.critical(message);
                return true;
            case "/setup":
                TournamentHandler.setup(message, this);
                return true;
            case "/go":
                TournamentHandler.startTournament(this);
                return true;
            case "/ct":
                TournamentHandler.cancelSetup(this);
                return true;
            case "/tourhelp":
                adminHandler.setupHelp(message);
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

    private boolean isPremiumUser(Message message) {
        return premiumUsers.contains(message.getFrom().getId());
    }

    private boolean isNotInBlacklist(Message message) {
        var result = blacklist.contains(message.getFrom().getId());
        if (result) {
            Methods.deleteMessage(message.getChatId(), message.getMessageId()).call(this);
        }
        return !result;
    }

    public Message sendMessage(long chatId, String text) {
        return sendMessage(Methods.sendMessage(chatId, text));
    }

    public Message sendMessage(SendMessageMethod sm) {
        return sm
                .enableHtml(true)
                .disableWebPagePreview()
                .call(this);
    }
}
