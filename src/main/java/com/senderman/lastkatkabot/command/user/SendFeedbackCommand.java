package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.config.BotConfig;
import com.senderman.lastkatkabot.dbservice.AdminService;
import com.senderman.lastkatkabot.dbservice.FeedbackService;
import com.senderman.lastkatkabot.model.Feedback;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Singleton;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class SendFeedbackCommand implements CommandExecutor {

    private final FeedbackService feedbackRepo;
    private final AdminService adminRepo;
    private final BotConfig config;

    public SendFeedbackCommand(
            FeedbackService feedbackRepo,
            AdminService adminRepo, BotConfig config
    ) {
        this.feedbackRepo = feedbackRepo;
        this.adminRepo = adminRepo;
        this.config = config;
    }

    @Override
    public String command() {
        return "/feedback";
    }

    @Override
    public String getDescription() {
        return "отправить сообщение разработчику. Например, /feedback бот не работает";
    }

    @Override
    public void accept(MessageContext ctx) {
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage("Неверное кол-во аргументов!").callAsync(ctx.sender);
            return;
        }
        var feedbackText = Html.htmlSafe(ctx.argument(0));
        if (feedbackText.length() > 2000) {
            ctx.replyToMessage("Максимальная длина текста - 2000 символов!").callAsync(ctx.sender);
            return;
        }
        var user = ctx.user();
        var userLink = Html.getUserLink(user);

        var feedback = new Feedback(feedbackText, user.getId(), user.getFirstName(), ctx.chatId(), ctx.message().getMessageId());
        feedback = feedbackRepo.insert(feedback);
        var feedbackId = feedback.getId();
        var adminPings = StreamSupport.stream(adminRepo.findAll().spliterator(), false)
                .map(a -> "<a href=\"tg://user?id=" + a.getUserId() + "\">" + a.getName() + "</a>")
                .collect(Collectors.joining(", "));
        var text = ("""
                🔔 <b>Фидбек #%d</b>

                От: %s

                %s

                Для ответа, введите /fresp %d &lt;ваш ответ&gt;
                                
                🚨 %s""")
                .formatted(feedbackId, userLink, feedbackText, feedbackId, adminPings);
        Methods.sendMessage(config.feedbackChannelId(), text).callAsync(ctx.sender);
        ctx.replyToMessage("✅ Сообщение отправлено разработчикам!").callAsync(ctx.sender);
    }
}
