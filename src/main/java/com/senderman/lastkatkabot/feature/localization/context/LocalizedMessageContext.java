package com.senderman.lastkatkabot.feature.localization.context;

import com.annimon.tgbotsmodule.commands.context.MessageContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.feature.localization.service.LocalizationService;
import io.micronaut.context.annotation.Prototype;
import org.telegram.telegrambots.meta.api.objects.Update;

@Prototype
public class LocalizedMessageContext extends MessageContext {
    private final LocalizationService localizationService;

    public LocalizedMessageContext(CommonAbsSender sender, Update update, String arguments, LocalizationService localizationService) {
        super(sender, update, arguments);
        this.localizationService = localizationService;
    }

    public String getLocale() {
        return localizationService.getLocale(user().getId());
    }

    public String getString(String key) {
        return localizationService.getString(key, getLocale());
    }
}
