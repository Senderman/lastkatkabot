package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class Stats implements CommandExecutor {

    private final UserStatsService users;


    public Stats(UserStatsService users) {
        this.users = users;
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
        String text = String.format(
                "📊 Статистика %s:\n\n" +
                "👑 Дуэлей выиграно: %d\n" +
                "⚔️ Всего дуэлей: %d\n" +
                "📈 Винрейт: %d\n\n" +
                "🐮 Баллов за быки и коровы: %d",
                name, stats.getDuelWins(), stats.getDuelsTotal(), winRate, stats.getBncScore());

        if (stats.getLoverId() != null) {
            var lover = Methods.getChatMember(stats.getLoverId(), stats.getLoverId()).call(ctx.sender);
            if (lover != null) {
                String loverLink = Html.getUserLink(lover.getUser());
                text += "\n\n❤️ Вторая половинка: " + loverLink;
            }
        }
        ctx.reply(text).callAsync(ctx.sender);
    }
}
