package com.senderman.lastkatkabot.service.fileupload;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class was introduced due to the memory leak in TelegramBots which caused bot to reboot on free heroku account
 */
@Component
public class TelegramFileUploader {

    private final TelegramFileUploadService uploadService;

    public TelegramFileUploader(TelegramFileUploadService uploadService) {
        this.uploadService = uploadService;
    }

    public void sendDocument(long chatId, @Nullable Integer replyToMessageId, InputStream file, String filename) {
        RequestBody requestBody;
        try (file) {
            requestBody = RequestBody.create(file.readAllBytes(), MediaType.parse("multipart/form-data"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var document = MultipartBody.Part.createFormData("document", filename, requestBody);
        try {
            uploadService.sendDocument(chatId, replyToMessageId, document).execute();
        } catch (IOException ignored) {
        }
    }

    public void sendPhoto(long chatId, @Nullable Integer replyToMessageId, String caption, InputStream file, String filename) {
        RequestBody requestBody;
        try (file) {
            requestBody = RequestBody.create(file.readAllBytes(), MediaType.parse("multipart/form-data"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var document = MultipartBody.Part.createFormData("photo", filename, requestBody);
        try {
            uploadService.sendPhoto(chatId, replyToMessageId, caption, "HTML", document).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
