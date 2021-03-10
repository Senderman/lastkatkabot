package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class Divorce implements CommandExecutor {

    private final UserStatsService users;

    public Divorce(UserStatsService users) {
        this.users = users;
    }

    @Override
    public String getTrigger() {
        return "/divorce";
    }

    @Override
    public String getDescription() {
        return "подать на развод";
    }

    @Override
    public void execute(Message message, CommonAbsSender telegram) {

        var userId = message.getFrom().getId();
        var userStats = users.findById(userId);
        var loverId = userStats.getLoverId();

        if (loverId == null) {
            ApiRequests.answerMessage(message, "У вас и так никого нет!").callAsync(telegram);
            return;
        }

        var loverStats = users.findById(loverId);

        userStats.setLoverId(null);
        loverStats.setLoverId(null);
        users.save(userStats);
        users.save(loverStats);

        ApiRequests.answerMessage(message, "Вы расстались со своей половинкой!").callAsync(telegram);
        Methods.sendMessage(loverId, "Ваша половинка решила с вами расстаться :(").callAsync(telegram);

    }
}
