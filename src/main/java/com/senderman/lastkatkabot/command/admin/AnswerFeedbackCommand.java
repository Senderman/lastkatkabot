package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.FeedbackService;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class AnswerFeedbackCommand implements CommandExecutor {

    private final FeedbackService feedbackService;
    private final BotConfig config;

    public AnswerFeedbackCommand(
            FeedbackService feedbackService,
            BotConfig config
    ) {
        this.feedbackService = feedbackService;
        this.config = config;
    }

    @Override
    public String command() {
        return "/fresp";
    }

    @Override
    public String getDescription() {
        return "ответить на фидбек. " + command() + " id ответ. " + command() + " 4 хорошо, починим";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.ADMIN, Role.MAIN_ADMIN);
    }

    @Override
    public void accept(MessageContext ctx) {
        ctx.setArgumentsLimit(2);

        if (ctx.argumentsLength() < 2) {
            ctx.replyToMessage("Неверное количество аргументов!").callAsync(ctx.sender);
            return;
        }

        int feedbackId;
        try {
            feedbackId = Integer.parseInt(ctx.argument(0));
        } catch (NumberFormatException e) {
            ctx.replyToMessage("id фидбека - это число!").callAsync(ctx.sender);
            return;
        }

        var feedbackOptional = feedbackService.findById(feedbackId);
        if (feedbackOptional.isEmpty()) {
            ctx.replyToMessage("Фидбека с таким id не существует!").callAsync(ctx.sender);
            return;
        }
        // feedbackRepo.deleteById(feedbackId);
        var feedback = feedbackOptional.get();
        feedback.setReplied(true);
        feedbackService.update(feedback);

        var answer = ctx.argument(1);
        Methods.sendMessage()
                .setChatId(feedback.getChatId())
                .setText("\uD83D\uDD14 <b>Ответ разработчика</b>\n\n" + answer)
                .setReplyToMessageId(feedback.getMessageId())
                .call(ctx.sender);
        ctx.replyToMessage("Ответ отправлен!").callAsync(ctx.sender);

        // notify others about answer
        if (!ctx.chatId().equals(config.feedbackChannelId())) {
            var replierUsername = ctx.user().getFirstName();
            var answerReport = String.format("%s ответил на фидбек #%d:\n\n%s",
                    replierUsername, feedbackId, answer);
            Methods.sendMessage(config.feedbackChannelId(), answerReport).callAsync(ctx.sender);
        }
    }
}
