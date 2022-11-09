package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class BroadcastMessageCommand implements CommandExecutor {

    private final ChatUserService chatUsers;
    private final ExecutorService threadPool;

    public BroadcastMessageCommand(
            ChatUserService chatUsers,
            @Qualifier("generalNeedsPool") ExecutorService threadPool
    ) {
        this.chatUsers = chatUsers;
        this.threadPool = threadPool;
    }

    @Override
    public String command() {
        return "/broadcast";
    }

    @Override
    public String getDescription() {
        return "разослать всем сообщение";
    }

    @Override
    public EnumSet<Role> authority() {
        return EnumSet.of(Role.MAIN_ADMIN);
    }

    @Override
    public void accept(MessageContext ctx) {
        var chatId = ctx.chatId();
        ctx.setArgumentsLimit(1);
        if (ctx.argumentsLength() < 1) {
            ctx.replyToMessage("Неверное количество аргументов!").callAsync(ctx.sender);
            return;
        }

        Methods.sendMessage(chatId, "Начало рассылки сообщений...").call(ctx.sender);

        var messageToBroadcast = "🔔 <b>Сообщение от разработчиков</b>\n\n" + ctx.argument(0);
        var chatIds = chatUsers.getChatIds();
        int total = chatIds.size();
        var counterMessage = ctx.replyToMessage("Статус рассылки: %d успешно, %d неуспешно, всего %d из %d"
                .formatted(0, 0, 0, total)
        ).call(ctx.sender);

        var counter = new CounterWithCallback(total, ctx.sender, counterMessage);

        threadPool.execute(() -> {
            for (int i = 0; i < total; i++) {
                var m = new SendMessage(Long.toString(chatIds.get(i)), messageToBroadcast);
                ctx.sender.callAsync(m, msg -> counter.incSuccessful(), e -> counter.incDone());

                if (i % 20 == 0) {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private static class CounterWithCallback {
        private final int total;
        private final CommonAbsSender sender;
        private final Message messageToEdit;
        private int successful = 0;
        private int done = 0;

        public CounterWithCallback(int total, CommonAbsSender sender, Message messageToEdit) {
            this.total = total;
            this.sender = sender;
            this.messageToEdit = messageToEdit;
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
            String text = "Статус рассылки: %d успешно, %d неуспешно, всего %d из %d"
                    .formatted(successful, done - successful, done, total);
            Methods.editMessageText(messageToEdit.getChatId(), messageToEdit.getMessageId(), text).callAsync(sender);
        }

    }
}
