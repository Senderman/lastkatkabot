package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.MarriageRequestService;
import com.senderman.lastkatkabot.dbservice.UserStatsService;
import com.senderman.lastkatkabot.model.MarriageRequest;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import jakarta.inject.Singleton;

import java.util.Objects;

@Singleton
public class MarryMeCommand implements CommandExecutor {

    private final UserStatsService users;
    private final MarriageRequestService marriages;

    public MarryMeCommand(UserStatsService users, MarriageRequestService marriages) {
        this.users = users;
        this.marriages = marriages;
    }

    @Override
    public String command() {
        return "/marryme";
    }

    @Override
    public String getDescription() {
        return "(reply) пожениться на ком-нибудь";
    }

    @Override
    public void accept(MessageContext ctx) {
        var message = ctx.message();
        if (message.isUserMessage() || !message.isReply()) {
            ctx.replyToMessage("Для использования команды необходимо ответить ей на чье-нибудь сообщение!").callAsync(ctx.sender);
            return;
        }

        var proposerId = message.getFrom().getId();
        var proposeeId = message.getReplyToMessage().getFrom().getId();

        if (proposerId.equals(proposeeId)) {
            ctx.replyToMessage("На самом себе нельзя жениться!").callAsync(ctx.sender);
            return;
        }

        var proposerStats = users.findById(proposerId);

        if (Objects.equals(proposerStats.getLoverId(), proposeeId)) {
            ctx.replyToMessage("Вы уже в браке с этим пользователем!").callAsync(ctx.sender);
            return;
        }

        if (proposerStats.hasLover()) {
            ctx.replyToMessage("Вы что, хотите изменить своей половинке?!").callAsync(ctx.sender);
            return;
        }
        var proposeeStats = users.findById(proposeeId);

        if (proposeeStats.hasLover()) {
            ctx.replyToMessage("У этого пользователя уже есть своя вторая половинка!").callAsync(ctx.sender);
            return;
        }

        var proposerLink = Html.getUserLink(message.getFrom());
        var text = "Пользователь " + proposerLink + " предлагает вам предлагает вам руку, сердце и шавуху. Вы согласны?";

        var request = new MarriageRequest.Builder()
                .setProposerId(proposerId)
                .setProposerName(proposerLink)
                .setProposeeId(proposeeId)
                .setProposeeName(Html.getUserLink(message.getReplyToMessage().getFrom()))
                .setRequestDate(message.getDate())
                .createMarriageRequest();

        request = marriages.insert(request);

        var markup = new MarkupBuilder()
                .addButton(ButtonBuilder.callbackButton()
                        .text("Принять")
                        .payload(Callbacks.MARRIAGE + " accept " + request.getId()))
                .addButton(ButtonBuilder.callbackButton()
                        .text("Отказаться")
                        .payload(Callbacks.MARRIAGE + " decline " + request.getId()))
                .build();

        ctx.reply(text)
                .setReplyToMessageId(message.getReplyToMessage().getMessageId())
                .setReplyMarkup(markup)
                .callAsync(ctx.sender);
    }

}
