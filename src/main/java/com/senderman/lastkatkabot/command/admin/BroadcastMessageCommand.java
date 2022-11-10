package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.util.Threads;
import org.apache.http.HttpStatus;
import org.json.HTTP;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.util.EnumSet;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
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

        var counter = new CounterMessage(total, ctx.sender, counterMessage);

        // no need to use thread pool, since the /broadcast command is used rarely
        new Thread(() -> {
            for (int i = 0; i < total; i++) {
                var m = new SendMessage();
                m.setChatId(chatIds.get(i));
                m.setText(messageToBroadcast);
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
            ctx.replyToMessage("✅ Рассылка завершена!").callAsync(ctx.sender);
        }).start();
    }

    private boolean is429TooManyRequests(TelegramApiException e) {
        if (e instanceof TelegramApiRequestException ex)
            return ex.getErrorCode() == 429;
        return false;
    }

    // A message that holds information about current broadcast status and updates it
    private static class CounterMessage {
        private final int total;
        private final CommonAbsSender sender;
        private final Message messageToEdit;
        private int successful = 0;
        private int done = 0;

        public CounterMessage(int total, CommonAbsSender sender, Message messageToEdit) {
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
