package com.senderman.lastkatkabot.handler;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.callback.Callbacks;
import com.senderman.lastkatkabot.dbservice.BlacklistedChatService;
import com.senderman.lastkatkabot.dbservice.ChatInfoService;
import com.senderman.lastkatkabot.exception.TooWideNicknameException;
import com.senderman.lastkatkabot.service.ImageService;
import com.senderman.lastkatkabot.util.callback.ButtonBuilder;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.Consumer;

@Singleton
public class NewMemberHandler implements Consumer<MessageContext> {

    private final BlacklistedChatService blacklistedChatService;
    private final ChatInfoService chatInfoService;
    private final ImageService imageService;

    public NewMemberHandler(
            BlacklistedChatService blacklistedChatService,
            ChatInfoService chatInfoService,
            ImageService imageService
    ) {
        this.blacklistedChatService = blacklistedChatService;
        this.chatInfoService = chatInfoService;
        this.imageService = imageService;
    }

    @Override
    public void accept(MessageContext ctx) {
        long chatId = ctx.chatId();
        // if bot is added to the blacklisted chat, leave
        if (blacklistedChatService.existsById(chatId)) {
            Methods.sendMessage(chatId, "📛 Ваш чат в списке спамеров! Бот не хочет здесь работать!").callAsync(ctx.sender);
            Methods.leaveChat(chatId).callAsync(ctx.sender);
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

        ctx.replyToMessageWithSticker()
                .setFile(stickerId)
                .setInlineKeyboard(ButtonBuilder.callbackButton()
                        .text("Привет, " + nickname + "!")
                        .payload(Callbacks.GREETING)
                        .create())
                .callAsync(ctx.sender);
    }

    private void fallbackWithGreetingGif(MessageContext ctx) {
        ctx.replyToMessageWithDocument()
                .setFile(imageService.getHelloGifId())
                .callAsync(ctx.sender);
    }
}
