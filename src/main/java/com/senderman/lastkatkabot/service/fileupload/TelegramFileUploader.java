package com.senderman.lastkatkabot.service.fileupload;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import javax.annotation.Nullable;
import java.io.File;
import java.util.function.Consumer;

@Component
public class TelegramFileUploader {

    private final TelegramFileUploadService uploadService;

    public TelegramFileUploader(TelegramFileUploadService uploadService) {
        this.uploadService = uploadService;
    }

    public void sendDocument(long chatId, @Nullable Integer replyToMessageId, File file, @Nullable Consumer<File> callback) {
        var requestBody = RequestBody.create(file, MediaType.parse("multipart/form-data"));
        var document = MultipartBody.Part.createFormData("document", file.getName(), requestBody);
        uploadService.sendDocument(chatId, replyToMessageId, document).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (callback != null) {
                    callback.accept(file);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (callback != null) {
                    callback.accept(file);
                }
            }
        });
    }
}
