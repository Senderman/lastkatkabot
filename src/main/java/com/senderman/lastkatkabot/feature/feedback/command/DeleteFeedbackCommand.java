package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.feature.feedback.service.FeedbackService;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;

import java.util.EnumSet;

@Singleton
@Command
public class DeleteFeedbackCommand implements CommandExecutor {

    private final FeedbackService feedbackRepo;
    private final BotConfig config;

    public DeleteFeedbackCommand(
            FeedbackService feedbackRepo,
            BotConfig config
    ) {
        this.feedbackRepo = feedbackRepo;
        this.config = config;
    }

    @Override
    public String command() {
        return "/fdel";
    }

    @Override
    public String getDescription() {
        return "удалить фидбек по id. /fdel 3";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(MessageContext ctx) {
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage("Неверное количество аргументов!").callAsync(ctx.sender);
            return;
        }
        var arg = ctx.argument(0);
        if (arg.matches("\\d+")) {
            deleteSingleFeedback(ctx, Integer.parseInt(arg));
        } else if (arg.matches("\\d+-\\d+")) {
            var args = arg.split("-");
            int from = Integer.parseInt(args[0]);
            int to = Integer.parseInt(args[1]);
            deleteFeedbackInRange(ctx, from, to);
        } else {
            ctx.replyToMessage("id фидбека - это число либо интервал!").callAsync(ctx.sender);
        }
    }


    private void deleteSingleFeedback(MessageContext ctx, int feedbackId) {
        if (!feedbackRepo.existsById(feedbackId)) {
            notifyNoFeedbacksFound(ctx);
            return;
        }

        feedbackRepo.deleteById(feedbackId);
        notifySuccess(ctx, "Фидбек #" + feedbackId + " удален");
    }

    private void deleteFeedbackInRange(MessageContext ctx, int from, int to) {
        long result = feedbackRepo.deleteByIdBetween(from, to);
        if (result == 0) {
            notifyNoFeedbacksFound(ctx);
            return;
        }
        notifySuccess(ctx, "Удалено " + result + " фидбеков (с " + from + " по " + to + ")");
    }

    private void notifySuccess(MessageContext ctx, String text) {
        ctx.replyToMessage(text).callAsync(ctx.sender);
        if (!ctx.chatId().equals(config.feedbackChannelId()))
            Methods.sendMessage()
                    .setChatId(config.feedbackChannelId())
                    .setText(text + " пользователем " + Html.getUserLink(ctx.user()))
                    .callAsync(ctx.sender);
    }

    private void notifyNoFeedbacksFound(MessageContext ctx) {
        ctx.replyToMessage("Ни одного фидбека не найдено").callAsync(ctx.sender);
    }
}
