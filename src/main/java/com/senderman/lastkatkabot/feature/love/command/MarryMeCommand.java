package com.senderman.lastkatkabot.feature.love.command;

import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.love.model.MarriageRequest;
import com.senderman.lastkatkabot.feature.love.service.MarriageRequestService;
import com.senderman.lastkatkabot.feature.userstats.service.UserStatsService;
import com.senderman.lastkatkabot.util.Html;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Command
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
        return "love.marryme.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        var message = ctx.message();
        if (message.isUserMessage() || !message.isReply()) {
            ctx.replyToMessage(ctx.getString("love.marryme.mustBeReply")).callAsync(ctx.sender);
            return;
        }

        var proposerId = message.getFrom().getId();
        var proposeeId = message.getReplyToMessage().getFrom().getId();

        if (proposerId.equals(proposeeId)) {
            ctx.replyToMessage(ctx.getString("love.marryme.noSelfMarriage")).callAsync(ctx.sender);
            return;
        }

        var proposerStats = users.findById(proposerId);

        if (Objects.equals(proposerStats.getLoverId(), proposeeId)) {
            ctx.replyToMessage(ctx.getString("love.marryme.alreadyMarried")).callAsync(ctx.sender);
            return;
        }

        if (proposerStats.hasLover()) {
            ctx.replyToMessage(ctx.getString("love.marryme.proposerHaveLover")).callAsync(ctx.sender);
            return;
        }
        var proposeeStats = users.findById(proposeeId);

        if (proposeeStats.hasLover()) {
            ctx.replyToMessage(ctx.getString("love.marryme.proposeeHaveLover")).callAsync(ctx.sender);
            return;
        }

        var proposerLink = Html.getUserLink(message.getFrom());
        var text = ctx.getString("love.marryme.message").formatted(proposerLink);

        var request = new MarriageRequest.Builder()
                .setProposerId(proposerId)
                .setProposerName(proposerLink)
                .setProposeeId(proposeeId)
                .setProposeeName(Html.getUserLink(message.getReplyToMessage().getFrom()))
                .setRequestDate(message.getDate())
                .createMarriageRequest();

        request = marriages.insert(request);

        ctx.reply(text)
                .inReplyTo(message.getReplyToMessage())
                .setSingleRowInlineKeyboard(
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("love.marryme.acceptButton"))
                                .payload(MarriageCallback.NAME, "accept", request.getId())
                                .create(),
                        ButtonBuilder.callbackButton()
                                .text(ctx.getString("love.marryme.declineButton"))
                                .payload(MarriageCallback.NAME, "decline", request.getId())
                                .create()
                )
                .callAsync(ctx.sender);
    }

}
