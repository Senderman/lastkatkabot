package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class ShortInfo implements CommandExecutor {

    private final CommonAbsSender telegram;

    public ShortInfo(CommonAbsSender telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return "/shortinfo";
    }

    @Override
    public String getDescription() {
        return "краткая инфа о сообщении. Поддерживается реплай";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();
        var userId = message.getFrom().getId();

        String info = String.format("==== Информация ====\n\n" +
                "\uD83D\uDCAC ID чата: <code>%d</code>\n" +
                "\uD83D\uDE4D\u200D♂️ Ваш ID: <code>%d</code>", chatId, userId);

        if (message.isReply()) {
            var reply = message.getReplyToMessage();
            var replyMessageId = reply.getMessageId();
            var replyUserId = reply.getFrom().getId();
            info += String.format("\n\n" +
                    "✉️ ID reply: <code>%d</code>\n" +
                    "\uD83D\uDE4D\u200D♂ ID юзера из reply: <code>%d</code>", replyMessageId, replyUserId);

            var forward = reply.getForwardFromChat();
            if (forward != null && forward.isChannelChat()) {
                info += String.format("\n\uD83D\uDCE2 ID канала: <code>%d</code>", forward.getId());
            }
        }
        Methods.sendMessage(chatId, info).callAsync(telegram);

    }
}
