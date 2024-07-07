package com.senderman.lastkatkabot.feature.userstats.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.TelegramUsersHelper;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.Optional;

@Command
public class StatsCommand implements CommandExecutor {

    private final UserStatsService users;
    private final TelegramUsersHelper usersHelper;
    private final BotConfig botConfig;


    public StatsCommand(UserStatsService users, TelegramUsersHelper usersHelper, BotConfig botConfig) {
        this.users = users;
        this.usersHelper = usersHelper;
        this.botConfig = botConfig;
    }

    @Override
    public String command() {
        return "/stats";
    }

    @Override
    public String getDescriptionKey() {
        return "userstats.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        User user = (ctx.message().isReply()) ? ctx.message().getReplyToMessage().getFrom() : ctx.user();

        if (user.getIsBot()) {
            ctx.replyToMessage(ctx.getString("userstats.isBot"))
                    .callAsync(ctx.sender);
            return;
        }

        var stats = users.findById(user.getId());
        String name = Html.htmlSafe(user.getFirstName());
        int winRate = stats.getDuelsTotal() == 0 ? 0 : 100 * stats.getDuelWins() / stats.getDuelsTotal();
        var userLocale = Optional.ofNullable(stats.getLocale())
                .or(() -> Optional.ofNullable(user.getLanguageCode()))
                .orElseGet(() -> botConfig.getLocale().getDefaultLocale());
        String text = ctx.getString("userstats.text")
                .formatted(name,
                        stats.getDuelWins(),
                        stats.getDuelsTotal(),
                        winRate,
                        stats.getBncScore(),
                        userLocale
                );
        var loverId = stats.getLoverId();
        if (loverId == null) {
            ctx.reply(text).callAsync(ctx.sender);
            return;
        }

        User lover = usersHelper.findUserFirstName(loverId, ctx);
        String loverLink = Html.getUserLink(lover);
        text += ctx.getString("userstats.lover").formatted(loverLink);
        ctx.reply(text).callAsync(ctx.sender);
    }

}
