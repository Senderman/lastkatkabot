package com.senderman.lastkatkabot.handler;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import com.senderman.lastkatkabot.exception.TooWideNicknameException;
import com.senderman.lastkatkabot.service.ImageService;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import com.senderman.lastkatkabot.util.callback.MarkupBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.function.Consumer;

@Component
public class NewMemberHandler {

    private final BlacklistedChatService blacklistedChatService;
    private final Consumer<Long> chatPolicyViolationConsumer;
    private final ChatInfoService chatInfoService;
    private final ImageService imageService;

    public NewMemberHandler(
            BlacklistedChatService blacklistedChatService,
            @Qualifier("chatPolicyViolationConsumer") Consumer<Long> chatPolicyViolationConsumer,
            ChatInfoService chatInfoService,
            ImageService imageService
    ) {
        this.blacklistedChatService = blacklistedChatService;
        this.chatPolicyViolationConsumer = chatPolicyViolationConsumer;
        this.chatInfoService = chatInfoService;
        this.imageService = imageService;
    }

    public void accept(MessageContext ctx) {
        var chatId = ctx.chatId();
        // if bot is added to the blacklisted chat, leave
        if (blacklistedChatService.existsById(chatId)) {
            chatPolicyViolationConsumer.accept(chatId);
            return;
        }
        ctx.message().getNewChatMembers()
                .stream()
                .filter(m -> !m.getIsBot())
                .forEach(m -> sendGreetingSticker(ctx, m.getFirstName()));
    }

    private void sendGreetingSticker(MessageContext ctx, String nickname) {
        var stickerId = chatInfoService.findById(ctx.chatId()).getGreetingStickerId();
        try {
            if (stickerId == null)
                sendDefaultGreetingSticker(ctx, nickname);
            else sendCustomGreetingSticker(ctx, nickname, stickerId);
        } catch (TooWideNicknameException e) {
            fallbackWithGreetingGif(ctx);
        }
    }

    private void sendDefaultGreetingSticker(MessageContext ctx, String nickname) throws TooWideNicknameException {
        try (var stickerStream = imageService.generateGreetingSticker(nickname)) {
            // if we send a png file with the webp extension, telegram will show it as sticker
            ctx.replyToMessageWithDocument()
                    .setFile("sticker.webp", stickerStream)
                    .callAsync(ctx.sender);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendCustomGreetingSticker(MessageContext ctx, String nickname, @NotNull String stickerId) throws TooWideNicknameException {
        // telegram limitation (max callback size
        if (nickname.length() > 32)
            throw new TooWideNicknameException(nickname);

        var markup = new MarkupBuilder()
                .addButton(ButtonBuilder.callbackButton()
                        .text("Привет, " + nickname + "!")
                        .payload(Callbacks.GREETING)
                        .create())
                .build();

        ctx.replyToMessageWithSticker()
                .setFile(stickerId)
                .setReplyMarkup(markup)
                .callAsync(ctx.sender);
    }

    private void fallbackWithGreetingGif(MessageContext ctx) {
        ctx.replyToMessageWithDocument()
                .setFile(imageService.getHelloGifId())
                .callAsync(ctx.sender);
    }
}
