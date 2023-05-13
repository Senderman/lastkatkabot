package com.senderman.lastkatkabot.feature.localization.context;

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.feature.localization.service.LocalizationService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class LocalizedCallbackQueryContext extends CallbackQueryContext {
    private final LocalizationService localizationService;

    public LocalizedCallbackQueryContext(CommonAbsSender sender, Update update, String arguments, LocalizationService localizationService) {
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
