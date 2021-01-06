package com.senderman.lastkatkabot.command.user;

import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class Divorce implements CommandExecutor {

    private final ApiRequests telegram;
    private final UserStatsRepository users;

    public Divorce(ApiRequests telegram, UserStatsRepository users) {
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
        var userStats = users.findById(userId).orElse(new Userstats(userId));
        var loverId = userStats.getLoverId();

        if (loverId == null) {
            telegram.sendMessage(chatId, "У вас и так никого нет!", messageId);
            return;
        }

        var loverStats = users.findById(loverId).orElseThrow();

        userStats.setLoverId(null);
        loverStats.setLoverId(null);
        users.save(userStats);
        users.save(loverStats);

        telegram.sendMessage(chatId, "Вы расстались со своей половинкой!", messageId);
        telegram.sendMessage(loverId, "Ваша половинка решила с вами расстаться :(");

    }
}
