package com.senderman.lastkatkabot.bnc;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;
import java.util.function.Function;

@Component
public class BncTop implements CommandExecutor {

    private final ApiRequests telegram;
    private final UserStatsRepository users;

    public BncTop(ApiRequests telegram, UserStatsRepository users) {
        this.telegram = telegram;
        this.users = users;
    }

    @Override
    public String getTrigger() {
        return "/bnctop";
    }

    @Override
    public String getDescription() {
        return "топ игроков в Быки и Коровы";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();

        int counter = 0;
        var top = new StringBuilder("<b>Топ-10 задротов в bnc:</b>\n\n");
        for (var user : users.findTop10OrderByBncScore()) {
            top.append(++counter)
                    .append(": ")
                    .append(formatUser(user.getUserId(), user.getBncScore(), message.isUserMessage()))
                    .append("\n");
        }

        telegram.sendMessage(chatId, top.toString());
    }

    private String formatUser(int userId, int score, boolean printLink) {
        Function<User, String> userPrinter = printLink ? Html::getUserLink : u -> Html.htmlSafe(u.getFirstName());

        String user = Optional.ofNullable(telegram.execute(Methods.getChatMember(userId, userId)))
                .map(ChatMember::getUser)
                .map(userPrinter)
                .orElse("Без имени");

        return user + " (" + score + ")";
    }
}
