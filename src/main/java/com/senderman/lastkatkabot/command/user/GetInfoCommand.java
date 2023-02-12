package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
public class GetInfoCommand implements CommandExecutor {

    private final ObjectMapper objectMapper;

    public GetInfoCommand(@Named("messageToJsonMapper") ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String command() {
        return "/getinfo";
    }

    @Override
    public String getDescription() {
        return "(reply) инфа о сообщении в формате JSON";
    }

    @Override
    public void accept(MessageContext ctx) {
        var message = ctx.message();
        if (!message.isReply()) {
            ctx.replyToMessage("Для использования команды, отправьте ее в ответ на нужное сообщение!").callAsync(ctx.sender);
            return;
        }

        String serializedMessage;
        try {
            serializedMessage = Html.htmlSafe(objectMapper.writeValueAsString(message.getReplyToMessage()))
                    .replaceAll(":\\s(\"?)([^{\\[\\n]*?)(\",\\n|\"\\n|,\\n|\\n)", ": $1<code>$2</code>$3");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ctx.replyToMessage(serializedMessage).callAsync(ctx.sender);
    }
}
