package com.senderman.lastkatkabot.command.user;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.annimon.tgbotsmodule.services.CommonAbsSender;
import com.senderman.lastkatkabot.command.CommandExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class BncHelp implements CommandExecutor {

    private final CommonAbsSender telegram;
    private final String bncPhotoId;

    public BncHelp(CommonAbsSender telegram, @Value("${bnc-help-picture-id}") String bncPhotoId) {
        this.telegram = telegram;
        this.bncPhotoId = bncPhotoId;
    }

    @Override
    public String getTrigger() {
        return "/bnchelp";
    }

    @Override
    public String getDescription() {
        return "помощь по игре Быки и Коровы";
    }

    @Override
    public void execute(Message message) {
        Methods.sendPhoto()
                .setChatId(message.getChatId())
                .setReplyToMessageId(message.getMessageId())
                .setFile(bncPhotoId)
                .call(telegram);
    }
}
