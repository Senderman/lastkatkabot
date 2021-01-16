package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MarryMe implements CommandExecutor {

    private final ApiRequests telegram;
    private final UserStatsRepository users;

    @Autowired
    public MarryMe(ApiRequests telegram, UserStatsRepository users) {
        this.telegram = telegram;
        this.users = users;
    }

    @Override
    public String getTrigger() {
        return "/marryme";
    }

    @Override
    public String getDescription() {
        return "(reply) пожениться на ком-нибудь";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();
        var messageId = message.getMessageId();
        if (message.isUserMessage() || !message.isReply()) {
            telegram.sendMessage(chatId, "Для использования команды необходимо ответить ей на чье-нибудь сообщение!", messageId);
            return;
        }

        var userId = message.getFrom().getId();
        var loverId = message.getReplyToMessage().getFrom().getId();

        if (userId.equals(loverId)) {
            telegram.sendMessage(chatId, "На самом себе нельзя жениться!");
            return;
        }

        var userStats = users.findById(userId).orElse(new Userstats(userId));

        if (userStats.getLoverId() != null) {
            telegram.sendMessage(chatId, "Вы что, хотите изменить своей половинке?!", messageId);
            return;
        }

        var loverStats = users.findById(loverId).orElse(new Userstats(loverId));

        if (loverStats.getLoverId() != null) {
            telegram.sendMessage(chatId, "У этого пользователя уже есть своя вторая половинка!", messageId);
            return;
        }

        var userLink = Html.getUserLink(message.getFrom());
        var text = "Пользователь " + userLink + " предлагает вам предлагает вам руку, сердце и шавуху. Вы согласны?";

        var markup = new MarkupBuilder()
                .addButton(ButtonBuilder.callbackButton()
                        .text("Принять")
                        .payload(Callbacks.MARRIAGE + " accept " + userId))
                .addButton(ButtonBuilder.callbackButton()
                        .text("Отказаться")
                        .payload(Callbacks.MARRIAGE + " decline"))
                .build();

        telegram.sendMessage(Methods.sendMessage()
                .setChatId(chatId)
                .setText(text)
                .setReplyToMessageId(message.getReplyToMessage().getMessageId())
                .setReplyMarkup(markup)
        );
    }

}
