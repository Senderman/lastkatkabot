package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.Optional;
import java.util.function.Function;

@Command
@Singleton
public class BncTopCommand implements CommandExecutor {

    private final UserStatsService users;

    public BncTopCommand(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String command() {
        return "/bnctop";
    }

    @Override
    public String getDescription() {
        return "топ игроков в Быки и Коровы. /bnctop chat для топа чата";
    }

    @Override
    public void accept(@NotNull MessageContext ctx) {

        boolean chatTop = ctx.argument(0, "").equals("chat");
        String title = (chatTop ? "<b>Топ-10 задротов чата в bnc:</b>" : "<b>Топ-10 задротов в bnc:</b>") + "\n\n";
        var topUsers = chatTop ? users.findTop10BncPlayersByChat(ctx.chatId()) : users.findTop10BncPlayers();
        int counter = 0;
        var top = new StringBuilder(title);
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
