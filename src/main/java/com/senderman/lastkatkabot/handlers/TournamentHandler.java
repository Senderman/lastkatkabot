package com.senderman.lastkatkabot.handlers;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.LastkatkaBotHandler;
import com.senderman.lastkatkabot.Services;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TournamentHandler {
    public boolean isEnabled = false;
    final public Set<Integer> membersIds;
    Set<String> members;
    private Set<String> teams;
    private boolean isTeamMode;
    private String roundName;
    private final LastkatkaBotHandler handler;

    public TournamentHandler(LastkatkaBotHandler handler) {
        this.handler = handler;
        membersIds = new HashSet<>();
        teams = new HashSet<>();
    }

    public void setup(Message message) {
        if (isEnabled)
            return;

        var params = message.getText().split("\n");
        if (params.length != 4) {
            handler.sendMessage(message.getChatId(), "Неверное количество аргументов!");
            return;
        }

        isTeamMode = !params[1].startsWith("@"); // Name of commands should not start with @

        roundName = params[3].strip();

        var checkText = new StringBuilder()
                .append("⚠️ Проверьте правильность введенных данных")
                .append("\n\n")
                .append("<b>Раунд: </b>")
                .append(roundName);

        if (isTeamMode) {
            teams = new HashSet<>();
            for (var i = 1; i < 3; i++) {
                var paramString = params[i].strip().split("\\s+");
                teams.add(paramString[0].replace("_", " "));
                for (var j = 1; j < paramString.length; j++) {
                    members.add(paramString[j].replace("@", ""));
                }
            }
            checkText.append("\n<b>Команды: </b> ").append(getTeamsAsString());

        } else {
            members.add(params[1].replace("@", "").strip());
            members.add(params[2].replace("@", "").strip());
        }
        checkText.append("\n<b>Участники: </b>")
                .append(getMembersAsString())
                .append("\n\n/go - подтвердить, /ct - отменить");

        handler.sendMessage(Methods.sendMessage()
                .setChatId(Services.botConfig.getLastvegan())
                .setText(checkText.toString())
                .setReplyToMessageId(message.getMessageId()));
    }

    public void startTournament() {
        if (members.isEmpty() || isEnabled)
            return;

        var markup = new InlineKeyboardMarkup();
        var row1 = List.of(new InlineKeyboardButton()
                .setText("Снять ограничения")
                .setCallbackData(LastkatkaBot.CALLBACK_REGISTER_IN_TOURNAMENT)
        );
        var row2 = List.of(new InlineKeyboardButton()
                .setText("Группа турнира")
                .setUrl("https://t.me/" + Objects.requireNonNull(Services.botConfig.getTourgroupname()).replace("@", "")));
        var row3 = List.of(new InlineKeyboardButton()
                .setText("Канал турнира")
                .setUrl("https://t.me/" + Objects.requireNonNull(Services.botConfig.getTourchannel()).replace("@", "")));
        markup.setKeyboard(List.of(row1, row2, row3));

        var toVegans = Methods.sendMessage()
                .setChatId(Services.botConfig.getLastvegan())
                .setText(String.format("\uD83D\uDCE3 <b>Турнир активирован!</b>\n\n" +
                        "%1$s, нажмите на кнопку ниже для снятия ограничений в группе турнира", getMembersAsString()))
                .setReplyMarkup(markup);

        Methods.Administration.pinChatMessage()
                .setChatId(Services.botConfig.getLastvegan())
                .setMessageId(handler.sendMessage(toVegans).getMessageId())
                .setNotificationEnabled(false)
                .call(handler);

        var toChannel = Methods.sendMessage()
                .setChatId(Services.botConfig.getTourchannel());

        if (isTeamMode)
            toChannel.setText("<b>" + roundName + "</b>\n\n"
                    + teams.toArray()[0] + " vs " + teams.toArray()[1]);
        else
            toChannel.setText("<b>" + roundName + "</b>\n\n@"
                    + members.toArray()[0] + " vs @" + members.toArray()[1]);

        handler.sendMessage(toChannel);
        isEnabled = true;
    }

    public void cancelSetup() {
        if (isEnabled)
            return;
        members.clear();
        if (isTeamMode)
            teams.clear();
        handler.sendMessage(Services.botConfig.getLastvegan(), "\uD83D\uDEAB Действие отменено");
    }

    private String getScore(String[] params) {
        String player1;
        String player2;
        if (isTeamMode) {
            player1 = params[1].replace("_", " ");
            player2 = params[3].replace("_", " ");
        } else {
            player1 = params[1];
            player2 = params[3];
        }
        return String.format("%1$s - %2$s\n%3$s:%4$s", player1, player2, params[2], params[4]);
    }

    private void restrictMembers() {
        isEnabled = false;
        for (var memberId : membersIds) {
            Methods.Administration.restrictChatMember(Services.botConfig.getTourgroup(), memberId).call(handler);
        }
        members.clear();
        membersIds.clear();
        if (isTeamMode)
            teams.clear();
        Methods.Administration.unpinChatMessage(Services.botConfig.getLastvegan()).call(handler);
        var tournamentMessageId = Services.db.getTournamentMessageId();
        if (tournamentMessageId != 0) {
            Methods.Administration.pinChatMessage()
                    .setChatId(Services.botConfig.getLastvegan())
                    .setMessageId(tournamentMessageId)
                    .setNotificationEnabled(false)
                    .call(handler);
        }
    }

    public void score(Message message) {
        var params = message.getText().split("\\s+");
        if (params.length != 5) {
            handler.sendMessage(message.getChatId(), "Неверное количество аргументов!");
            return;
        }
        var score = getScore(params);
        handler.sendMessage(Methods.sendMessage(Objects.requireNonNull(Services.botConfig.getTourchannel()), score));
    }

    public void win(Message message) {
        var params = message.getText().split("\\s+");
        if (params.length != 6) {
            handler.sendMessage(message.getChatId(), "Неверное количество аргументов!");
            return;
        }
        var score = getScore(params);
        restrictMembers();

        String goingTo;
        if (isTeamMode)
            goingTo = (params[5].equals("over")) ?
                    " выиграли турнир" :
                    " выходят в " + params[5].replace("_", " ");
        else
            goingTo = (params[5].equals("over")) ?
                    " выиграл турнир" :
                    " выходит в " + params[5].replace("_", " ");

        handler.sendMessage(Methods.sendMessage()
                .setChatId(Objects.requireNonNull(Services.botConfig.getTourchannel()))
                .setText(score + "\n\n" + params[1].replace("_", " ") + "<b>" + goingTo + "!</b>"));

        handler.sendMessage(Methods.sendMessage()
                .setChatId(Services.botConfig.getLastvegan())
                .setText(String.format("\uD83D\uDCE3 <b>Раунд завершен. Победитель:</b> %1$s\n" +
                        "Болельщики, посетите %2$s, чтобы узнать подробности", params[1], Services.botConfig.getTourchannel())));
    }

    public void resetTournament() {
        restrictMembers();
        handler.sendMessage(Services.botConfig.getLastvegan(), "\uD83D\uDEAB <b>Раунд отменен из-за непредвиденных обстоятельств!</b>");
    }

    public void tourmessage(Message message) {
        if (!message.getChatId().equals(Services.botConfig.getLastvegan()) || !message.isReply())
            return;
        Services.db.setTournamentMessage(message.getReplyToMessage().getMessageId());
        handler.sendMessage(message.getChatId(), "✅ Главное сообщение турнира установлено!");
    }

    private String getMembersAsString() {
        var memberList = new StringBuilder();
        for (var member : members) {
            memberList.append("@").append(member).append(", ");
        }
        memberList.delete(memberList.length() - 2, memberList.length() - 1); //remove trailing ", "
        return memberList.toString();
    }

    private String getTeamsAsString() {
        var teamList = new StringBuilder();
        for (var team : teams) {
            teamList.append(team).append(", ");
        }
        teamList.delete(teamList.length() - 2, teamList.length() - 1);
        return teamList.toString();
    }
}