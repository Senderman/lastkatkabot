package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.ApiRequests;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class GetInfo implements CommandExecutor {

    private final CommonAbsSender telegram;

    public GetInfo(CommonAbsSender telegram) {
        this.telegram = telegram;
    }

    @Override
    public String getTrigger() {
        return "/getinfo";
    }

    @Override
    public String getDescription() {
        return "(reply) инфа о сообщении в формате JSON";
    }

    @Override
    public void execute(Message message) {
        var chatId = message.getChatId();

        if (!message.isReply()) {
            ApiRequests.answerMessage(message, "Для использования команды, отправьте ее в ответ на нужное сообщение!")
                    .call(telegram);
            return;
        }

        var replyInfo = message.getReplyToMessage()
                .toString()
                .replaceAll("\\w+=null,?\\s*", "")
                .replaceAll("=([\\w\\d]+)", "=<code>$1</code>");

        Methods.sendMessage(chatId, replyInfo).call(telegram);
    }
}
