package com.senderman.lastkatkabot.util.callback;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс для упрощения создания InlineKeyboardMarkup'ов
 */
public class MarkupBuilder {

    private final List<List<InlineKeyboardButton>> keyboard;
    private List<InlineKeyboardButton> currentRow;

    public MarkupBuilder() {
        keyboard = new ArrayList<>();
        newRow();
    }

    public MarkupBuilder addButton(InlineKeyboardButton button) {
        currentRow.add(button);
        return this;
    }

    public MarkupBuilder addButton(ButtonBuilder buttonBuilder) {
        addButton(buttonBuilder.create());
        return this;
    }


    // Создать новый ряд и переключиться на него
    public MarkupBuilder newRow() {
        currentRow = new ArrayList<>();
        keyboard.add(currentRow);
        return this;
    }

    public InlineKeyboardMarkup build() {
        var markup = new InlineKeyboardMarkup();
        markup.setKeyboard(keyboard);
        return markup;
    }

}
