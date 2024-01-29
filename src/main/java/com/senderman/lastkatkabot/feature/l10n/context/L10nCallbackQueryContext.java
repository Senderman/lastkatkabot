package com.senderman.lastkatkabot.feature.l10n.context;

import com.annimon.tgbotsmodule.commands.context.CallbackQueryContext;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.feature.l10n.service.L10nService;
import org.telegram.telegrambots.meta.api.objects.Update;

public class L10nCallbackQueryContext extends CallbackQueryContext {
    private final L10nService localizationService;
    private final String locale;

    public L10nCallbackQueryContext(CommonAbsSender sender, Update update, String arguments, L10nService localizationService) {
        super(sender, update, arguments);
        this.localizationService = localizationService;
        this.locale = localizationService.getLocale(user().getId(), user().getFirstName(), user().getLanguageCode());
    }

    public String getString(String key) {
        return localizationService.getString(key, locale);
    }
}
