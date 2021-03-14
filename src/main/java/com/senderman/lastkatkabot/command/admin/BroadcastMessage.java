package com.senderman.lastkatkabot.command.admin;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.senderman.lastkatkabot.Role;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.dbservice.ChatUserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.EnumSet;
import java.util.function.Consumer;

@Component
public class BroadcastMessage implements CommandExecutor {

    private final ChatUserService chatUsers;

    public BroadcastMessage(ChatUserService chatUsers) {
        this.chatUsers = chatUsers;
    }

    @Override
    public String getTrigger() {
        return "/broadcast";
    }

    @Override
    public String getDescription() {
        return "разослать всем сообщение";
    }

    @Override
    public EnumSet<Role> getRoles() {
        return EnumSet.of(Role.MAIN_ADMIN);
    }

    @Override
    public void execute(MessageContext ctx) {
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
        var counter = new CounterWithCallback(total,
                i -> ctx.replyToMessage("Сообщение получили %d/%d чатов".formatted(i, total)).callAsync(ctx.sender)
        );

        for (var chat : chatIds) {
            var m = new SendMessage(Long.toString(chat), messageToBroadcast);
            ctx.sender.callAsync(m, msg -> counter.incSuccessful(), e -> counter.incDone());
        }
    }

    private static class CounterWithCallback {
        private final int total;
        private final Consumer<Integer> callback;
        private int successful = 0;
        private int done = 0;

        public CounterWithCallback(int total, Consumer<Integer> callback) {
            this.total = total;

            this.callback = callback;
        }

        synchronized void incSuccessful() {
            successful++;
            incDone();
        }

        synchronized void incDone() {
            if (++done == total) executeCallback();
        }

        private void executeCallback() {
            callback.accept(successful);
        }
    }
}
