package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.MethodExecutor;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import com.senderman.lastkatkabot.util.TelegramHtmlUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class Stats implements CommandExecutor {

    private final MethodExecutor telegram;
    private final UserStatsRepository users;


    public Stats(MethodExecutor telegram, UserStatsRepository users) {
        this.telegram = telegram;
        this.users = users;
    }

    @Override
    public String getTrigger() {
        return "/stats";
    }

    @Override
    public String getDescription() {
        return "статистика. Реплаем можно узнать статистику реплайнутого";
    }

    @Override
    public void execute(Message message) {
        long chatId = message.getChatId();
        User user = (message.isReply()) ? message.getReplyToMessage().getFrom() : message.getFrom();

        if (user.getIsBot()) {
            telegram.sendMessage(chatId, "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, играть в BnC, участвовать в дуэлях?");
            return;
        }

        var stats = users.findById(user.getId()).orElse(new Userstats(user.getId()));
        String name = TelegramHtmlUtils.htmlSafe(user.getFirstName());
        int winRate = stats.getDuelsTotal() == 0 ? 0 : 100 / stats.getDuelWins() / stats.getDuelsTotal();
        String text = String.format("\uD83D\uDCCA Статистика %s:\n\n" +
                        "\uD83D\uDC51 Дуэлей выиграно: %d\n" +
                        "⚔️ Всего дуэлей: %d\n" +
                        "\uD83D\uDCC8 Винрейт: %d\n\n" +
                        "\uD83D\uDC2E Баллов за быки и коровы: %d",
                name, stats.getDuelWins(), stats.getDuelsTotal(), winRate, stats.getBncScore());

        if (stats.getLoverId() != null) {
            var lover = telegram.execute(Methods.getChatMember(stats.getLoverId(), stats.getLoverId()));
            if (lover != null) {
                String loverLink = TelegramHtmlUtils.getUserLink(lover.getUser());
                text += "\n\n❤️ Вторая половинка: " + loverLink;
            }
        }
        telegram.sendMessage(chatId, text);
    }
}
