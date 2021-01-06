package com.senderman.lastkatkabot.util.callback;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public abstract class ButtonBuilder {

    protected final InlineKeyboardButton button;

    protected ButtonBuilder() {
        this.button = new InlineKeyboardButton();
    }

    public static ButtonBuilder callbackButton() {
        return new CallbackButtonBuilder();
    }

    public static ButtonBuilder urlButton() {
        return new UrlButtonBuilder();
    }

    public static ButtonBuilder switchInlineQueryButton() {
        return new SwitchInlineQueryButtonBuilder();
    }

    public static ButtonBuilder switchInlineQueryCurrentChatButton() {
        return new SwitchInlineQueryCurrentChatButtonBuilder();
    }

    public ButtonBuilder text(String text) {
        button.setText(text);
        return this;
    }

    public abstract ButtonBuilder payload(String payload);

    public InlineKeyboardButton create() {
        return button;
    }

    private static class CallbackButtonBuilder extends ButtonBuilder {

        @Override
        public ButtonBuilder payload(String payload) {
            button.setCallbackData(payload);
            return this;
        }
    }

    private static class UrlButtonBuilder extends ButtonBuilder {

        @Override
        public ButtonBuilder payload(String payload) {
            button.setUrl(payload);
            return this;
        }
    }

    private static class SwitchInlineQueryButtonBuilder extends ButtonBuilder {


        @Override
        public ButtonBuilder payload(String payload) {
            button.setSwitchInlineQuery(payload);
            return this;
        }
    }

    private static class SwitchInlineQueryCurrentChatButtonBuilder extends ButtonBuilder {

        @Override
        public ButtonBuilder payload(String payload) {
            button.setSwitchInlineQueryCurrentChat(payload);
            return this;
        }
    }

}
