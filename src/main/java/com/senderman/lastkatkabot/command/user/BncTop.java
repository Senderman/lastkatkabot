package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.ChatMember;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;
import java.util.function.Function;

@Component
public class BncTop implements CommandExecutor {

    private final UserStatsService users;

    public BncTop(UserStatsService users) {
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
    public void execute(MessageContext ctx) {

        int counter = 0;
        var top = new StringBuilder("<b>Топ-10 задротов в bnc:</b>\n\n");
        var topUsers = users.findTop10BncPlayers();
        for (var user : topUsers) {
            top.append(++counter)
                    .append(": ")
                    .append(formatUser(user.getUserId(), user.getBncScore(), ctx.message().isUserMessage(), ctx.sender))
                    .append("\n");
        }

        ctx.reply(top.toString()).callAsync(ctx.sender);
    }

    private String formatUser(long userId, int score, boolean printLink, CommonAbsSender telegram) {
        Function<User, String> userPrinter = printLink ? Html::getUserLink : u -> Html.htmlSafe(u.getFirstName());

        String user = Optional.ofNullable(Methods.getChatMember(userId, userId).call(telegram))
                .map(ChatMember::getUser)
                .map(userPrinter)
                .orElse("Без имени");

        return user + " (" + score + ")";
    }
}
