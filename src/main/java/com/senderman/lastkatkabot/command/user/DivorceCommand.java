package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import org.springframework.stereotype.Component;

@Component
public class DivorceCommand implements CommandExecutor {

    private final UserStatsService users;

    public DivorceCommand(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String command() {
        return "/divorce";
    }

    @Override
    public String getDescription() {
        return "подать на развод";
    }

    @Override
    public void accept(MessageContext ctx) {

        var userId = ctx.user().getId();
        var userStats = users.findById(userId);
        var loverId = userStats.getLoverId();

        if (loverId == null) {
            ctx.replyToMessage("У вас и так никого нет!").callAsync(ctx.sender);
            return;
        }

        var loverStats = users.findById(loverId);

        userStats.setLoverId(null);
        loverStats.setLoverId(null);
        users.save(userStats);
        users.save(loverStats);

        ctx.replyToMessage("Вы расстались со своей половинкой!").callAsync(ctx.sender);
        Methods.sendMessage(loverId, "Ваша половинка решила с вами расстаться :(").callAsync(ctx.sender);

    }
}
