package com.senderman.lastkatkabot.feature.tracking.command;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.command.CommandExecutor;
import com.senderman.lastkatkabot.feature.l10n.context.L10nMessageContext;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.NotNull;

@Singleton
public class PrivacyPolicyCommand implements CommandExecutor {

    private final String privacyPolicyLink;

    public PrivacyPolicyCommand(@Value("${bot.privacyPolicyLink}") String privacyPolicyLink) {
        this.privacyPolicyLink = privacyPolicyLink;
    }

    @Override
    public String command() {
        return "/privacy";
    }

    @Override
    public String getDescriptionKey() {
        return "privacy.description";
    }

    @Override
    public void accept(@NotNull L10nMessageContext ctx) {
        var message = ctx.replyToMessage().setText(privacyPolicyLink).call(ctx.sender);

        // since there's a method preprocessor that disables webPagePreview on SendMessage method,
        // we use EditMessage to re-enable it
        if (message != null)
            Methods.editMessageText(message.getChatId(), message.getMessageId(), message.getText())
                    .enableWebPagePreview()
                    .callAsync(ctx.sender);
    }


}
