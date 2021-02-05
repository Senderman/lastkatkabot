package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.model.MarriageRequest;
import com.senderman.lastkatkabot.model.Userstats;
import com.senderman.lastkatkabot.repository.MarriageRequestRepository;
import com.senderman.lastkatkabot.repository.UserStatsRepository;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Objects;

@Component
public class MarryMe implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final UserStatsRepository users;
    private final MarriageRequestRepository marriages;

    public MarryMe(CommonAbsSender telegram, UserStatsRepository users, MarriageRequestRepository marriages) {
        this.telegram = telegram;
        this.users = users;
        this.marriages = marriages;
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
        if (message.isUserMessage() || !message.isReply()) {
            ApiRequests.answerMessage(message, "Для использования команды необходимо ответить ей на чье-нибудь сообщение!")
                    .callAsync(telegram);
            return;
        }

        var proposerId = message.getFrom().getId();
        var proposeeId = message.getReplyToMessage().getFrom().getId();

        if (proposerId.equals(proposeeId)) {
            Methods.sendMessage(chatId, "На самом себе нельзя жениться!").callAsync(telegram);
            return;
        }

        var proposerStats = users.findById(proposerId).orElseGet(() -> new Userstats(proposerId));

        if (Objects.equals(proposerStats.getLoverId(), proposeeId)) {
            ApiRequests.answerMessage(message, "Вы уже в браке с этим пользователем!").callAsync(telegram);
            return;
        }

        if (proposerStats.hasLover()) {
            ApiRequests.answerMessage(message, "Вы что, хотите изменить своей половинке?!").callAsync(telegram);
            return;
        }
        var proposeeStats = users.findById(proposeeId).orElseGet(() -> new Userstats(proposeeId));

        if (proposeeStats.hasLover()) {
            ApiRequests.answerMessage(message, "У этого пользователя уже есть своя вторая половинка!").callAsync(telegram);
            return;
        }

        var proposerLink = Html.getUserLink(message.getFrom());
        var text = "Пользователь " + proposerLink + " предлагает вам предлагает вам руку, сердце и шавуху. Вы согласны?";

        var request = new MarriageRequest();
        request.setId(marriages.findFirstByOrderByIdDesc().map(r -> r.getId() + 1).orElse(1));
        request.setProposerId(proposerId);
        request.setProposerName(proposerLink);
        request.setProposeeId(proposeeId);
        request.setProposeeName(Html.getUserLink(message.getReplyToMessage().getFrom()));
        request.setRequestDate(message.getDate());
        marriages.save(request);

        var markup = new MarkupBuilder()
                .addButton(ButtonBuilder.callbackButton()
                        .text("Принять")
                        .payload(Callbacks.MARRIAGE + " " + request.getId() + " accept"))
                .addButton(ButtonBuilder.callbackButton()
                        .text("Отказаться")
                        .payload(Callbacks.MARRIAGE + " " + request.getId() + " decline"))
                .build();

        Methods.sendMessage()
                .setChatId(chatId)
                .setText(text)
                .setReplyToMessageId(message.getReplyToMessage().getMessageId())
                .setReplyMarkup(markup)
                .callAsync(telegram);
    }

}
