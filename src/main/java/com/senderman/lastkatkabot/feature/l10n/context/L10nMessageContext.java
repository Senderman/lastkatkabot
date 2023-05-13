package com.senderman.lastkatkabot.feature.l10n.context;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.feature.l10n.service.L10nService;
import io.micronaut.context.annotation.Prototype;
import org.telegram.telegrambots.meta.api.objects.Update;

@Prototype
public class L10nMessageContext extends MessageContext {

    private final L10nService localizationService;
    private final String locale;

    public L10nMessageContext(CommonAbsSender sender, Update update, String arguments, L10nService localizationService) {
        super(sender, update, arguments);
        this.localizationService = localizationService;
        this.locale = localizationService.getLocale(user().getId());
    }

    public String getLocale() {
        return this.locale;
    }

    public String getString(String key) {
        return localizationService.getString(key, locale);
    }
}
