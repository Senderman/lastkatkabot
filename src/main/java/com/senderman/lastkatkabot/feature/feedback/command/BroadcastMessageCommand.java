package com.senderman.lastkatkabot.feature.feedback.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.feature.tracking.service.ChatUserService;
import com.senderman.lastkatkabot.util.Threads;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.EnumSet;
import java.util.stream.StreamSupport;

import static java.util.concurrent.TimeUnit.SECONDS;

@Command
public class BroadcastMessageCommand implements CommandExecutor {

    private final ChatUserService chatUsers;

    public BroadcastMessageCommand(ChatUserService chatUsers) {
        this.chatUsers = chatUsers;
    }

    @Override
    public String command() {
        return "/broadcast";
    }

    @Override
    public String getDescriptionKey() {
        return "feedback.broadcast.description";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.MAIN_ADMIN);
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        var chatId = ctx.chatId();
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage(ctx.getString("common.invalidArgumentsNumber")).callAsync(ctx.sender);
            return;
        }

        ctx.replyToMessage(ctx.getString("feedback.broadcast.start")).call(ctx.sender);

        var messageToBroadcast = "🔔 %s\n\n"
                .formatted(ctx.getString("feedback.broadcast.messageFromDevelopers")) + ctx.argument(0);
        var chatIds = StreamSupport.stream(chatUsers.getChatIds().spliterator(), false).toList();
        long total = chatIds.size();
        var counterMessage = ctx
                .replyToMessage(ctx.getString("feedback.broadcast.status").formatted(0, 0, 0, total))
                .call(ctx.sender);

        var counter = new CounterMessage(total, counterMessage, ctx);

        // no need to use thread pool, since the /broadcast command is used rarely
        new Thread(() -> {
            for (int i = 0; i < total; i++) {
                var m = new SendMessage(chatIds.get(i).toString(), messageToBroadcast);
                try {
                    ctx.sender.execute(m);
                    // on success, increase successful counter
                    counter.incSuccessful();
                } catch (TelegramApiException e) {
                    // if we hit telegram's limits, just wait and try again
                    if (is429TooManyRequests(e)) {
                        i--;
                        Threads.sleep(SECONDS.toMillis(30));
                    } else // failed to send message my some other reason, do not retry
                        counter.incDone();
                }

                if (i % 20 == 0) {
                    Threads.sleep(SECONDS.toMillis(5));
                }
            }
            ctx.replyToMessage(ctx.getString("feedback.broadcast.finished")).callAsync(ctx.sender);
        }).start();
    }

    private boolean is429TooManyRequests(TelegramApiException e) {
        if (e instanceof TelegramApiRequestException ex)
            return ex.getErrorCode() == 429;
        return false;
    }

    // A message that holds information about current broadcast status and updates it
    private static class CounterMessage {
        private final long total;
        private final CommonAbsSender sender;
        private final Message messageToEdit;
        private final L10nMessageContext ctx;
        private int successful = 0;
        private int done = 0;

        public CounterMessage(long total, Message messageToEdit, L10nMessageContext ctx) {
            this.total = total;
            this.sender = ctx.sender;
            this.messageToEdit = messageToEdit;
            this.ctx = ctx;
        }

        synchronized void incSuccessful() {
            successful++;
            incDone();
        }

        synchronized void incDone() {
            done++;
            if (done % 10 == 0 || done == total)
                updateStatus();
        }

        synchronized private void updateStatus() {
            String text = ctx.getString("feedback.broadcast.status")
                    .formatted(successful, done - successful, done, total);
            Methods.editMessageText(messageToEdit.getChatId(), messageToEdit.getMessageId(), text).callAsync(sender);
        }

    }
}
