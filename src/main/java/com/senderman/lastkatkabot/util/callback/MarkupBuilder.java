package com.senderman.lastkatkabot.util.callback;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper to ease the creation of InlineKeyboardMarkup instances
 */
public class MarkupBuilder {

    private final List<InlineKeyboardRow> keyboard;
    private InlineKeyboardRow currentRow;

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
        currentRow = new InlineKeyboardRow();
        keyboard.add(currentRow);
        return this;
    }

    public InlineKeyboardMarkup build() {
        return new InlineKeyboardMarkup(keyboard);
    }

}
