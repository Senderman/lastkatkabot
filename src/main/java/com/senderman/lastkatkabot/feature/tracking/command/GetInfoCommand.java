package com.senderman.lastkatkabot.feature.tracking.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.senderman.lastkatkabot.command.Command;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import com.senderman.lastkatkabot.util.Html;
import jakarta.inject.Named;
import org.jetbrains.annotations.NotNull;

@Command
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
        return "tracking.getinfo.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        var message = ctx.message();
        if (!message.isReply()) {
            ctx.replyToMessage(ctx.getString("tracking.getinfo.mustBeReply")).callAsync(ctx.sender);
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
