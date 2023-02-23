package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.annotation.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.util.Optional;

@Singleton
@Command
public class StatsCommand implements CommandExecutor {

    private final UserStatsService users;
    private final ChatUserService chatUsers;


    public StatsCommand(UserStatsService users, ChatUserService chatUsers) {
        this.users = users;
        this.chatUsers = chatUsers;
    }

    @Override
    public String command() {
        return "/stats";
    }

    @Override
    public String getDescription() {
        return "статистика. Реплаем можно узнать статистику реплайнутого";
    }

    @Override
    public void accept(MessageContext ctx) {
        User user = (ctx.message().isReply()) ? ctx.message().getReplyToMessage().getFrom() : ctx.user();

        if (user.getIsBot()) {
            ctx.replyToMessage("Но это же просто бот, имитация человека! " +
                            "Разве может бот написать симфонию, иметь статистику, играть в BnC, участвовать в дуэлях?")
                    .callAsync(ctx.sender);
            return;
        }

        var stats = users.findById(user.getId());
        String name = Html.htmlSafe(user.getFirstName());
        int winRate = stats.getDuelsTotal() == 0 ? 0 : 100 * stats.getDuelWins() / stats.getDuelsTotal();
        String text = """
                📊 Статистика %s:

                👑 Дуэлей выиграно: %d
                ⚔️ Всего дуэлей: %d
                📈 Винрейт: %d%%

                🐮 Баллов за быки и коровы: %d"""
                .formatted(name, stats.getDuelWins(), stats.getDuelsTotal(), winRate, stats.getBncScore());

        var loverId = stats.getLoverId();
        if (loverId == null) {
            ctx.reply(text).callAsync(ctx.sender);
            return;
        }

        User lover = chatUsers.findNewestUserData(loverId)
                .map(l -> new User(l.getUserId(), l.getName(), false)) // get actual username from chatUsers table
                .or(() -> getUserDataFromTelegram(loverId, ctx.sender)) // fallback to request it from telegram
                .orElseGet(() -> new User(loverId, "Unknown User", false)); // give up and set the name to "Unknown user"
        String loverLink = Html.getUserLink(lover);
        text += "\n\n❤️ Вторая половинка: " + loverLink;
        ctx.reply(text).callAsync(ctx.sender);
    }

    private Optional<User> getUserDataFromTelegram(long userId, CommonAbsSender sender) {
        var member = Methods.getChatMember(userId, userId).call(sender);
        return Optional.ofNullable(member).map(ChatMember::getUser);
    }
}
