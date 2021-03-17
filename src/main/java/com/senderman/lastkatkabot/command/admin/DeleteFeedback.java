package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.FeedbackService;
import com.senderman.lastkatkabot.util.Html;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class DeleteFeedback implements CommandExecutor {

    private final FeedbackService feedbackRepo;
    ;
    private final BotConfig config;

    public DeleteFeedback(
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
        return "удалить фидбек по id. " + command() + " 3";
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

        int feedbackId;
        try {
            feedbackId = Integer.parseInt(ctx.argument(0));
        } catch (NumberFormatException e) {
            ctx.replyToMessage("id фидбека - это число!").callAsync(ctx.sender);
            return;
        }

        if (!feedbackRepo.existsById(feedbackId)) {
            ctx.replyToMessage("Фидбека с таким id не существует!").callAsync(ctx.sender);
            return;
        }

        feedbackRepo.deleteById(feedbackId);
        var text = "Фидбек #" + feedbackId + " удален";
        ctx.replyToMessage(text).callAsync(ctx.sender);
        if (!ctx.chatId().equals(config.feedbackChannelId()))
            Methods.sendMessage()
                    .setChatId(config.feedbackChannelId())
                    .setText(text + " пользователем " + Html.getUserLink(ctx.user()))
                    .callAsync(ctx.sender);
    }
}
