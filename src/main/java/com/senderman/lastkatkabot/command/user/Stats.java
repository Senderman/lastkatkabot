package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class Stats implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final UserStatsService users;


    public Stats(CommonAbsSender telegram, UserStatsService users) {
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
            ApiRequests.answerMessage(message, "Но это же просто бот, имитация человека! " +
                    "Разве может бот написать симфонию, иметь статистику, играть в BnC, участвовать в дуэлях?")
                    .callAsync(telegram);
            return;
        }

        var stats = users.findById(user.getId());
        String name = Html.htmlSafe(user.getFirstName());
        int winRate = stats.getDuelsTotal() == 0 ? 0 : 100 * stats.getDuelWins() / stats.getDuelsTotal();
        String text = String.format("\uD83D\uDCCA Статистика %s:\n\n" +
                        "\uD83D\uDC51 Дуэлей выиграно: %d\n" +
                        "⚔️ Всего дуэлей: %d\n" +
                        "\uD83D\uDCC8 Винрейт: %d\n\n" +
                        "\uD83D\uDC2E Баллов за быки и коровы: %d",
                name, stats.getDuelWins(), stats.getDuelsTotal(), winRate, stats.getBncScore());

        if (stats.getLoverId() != null) {
            var lover = Methods.getChatMember(stats.getLoverId(), stats.getLoverId()).call(telegram);
            if (lover != null) {
                String loverLink = Html.getUserLink(lover.getUser());
                text += "\n\n❤️ Вторая половинка: " + loverLink;
            }
        }
        Methods.sendMessage(chatId, text).callAsync(telegram);
    }
}
