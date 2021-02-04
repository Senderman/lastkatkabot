package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class Divorce implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final UserStatsRepository users;

    public Divorce(CommonAbsSender telegram, UserStatsRepository users) {
        this.telegram = telegram;
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
    public void execute(Message message) {

        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        var userId = message.getFrom().getId();
        var userStats = users.findById(userId).orElseGet(() -> new Userstats(userId));
        var loverId = userStats.getLoverId();

        if (loverId == null) {
            ApiRequests.answerMessage(message, "У вас и так никого нет!").call(telegram);
            return;
        }

        var loverStats = users.findById(loverId).orElseGet(() -> new Userstats(loverId));

        userStats.setLoverId(null);
        loverStats.setLoverId(null);
        users.save(userStats);
        users.save(loverStats);

        ApiRequests.answerMessage(message, "Вы расстались со своей половинкой!").call(telegram);
        Methods.sendMessage(loverId, "Ваша половинка решила с вами расстаться :(").call(telegram);

    }
}
