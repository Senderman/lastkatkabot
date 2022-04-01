package com.senderman.lastkatkabot.service.fileupload;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import javax.annotation.Nullable;

public interface TelegramFileUploadService {

    @POST("sendPhoto")
    @Multipart
    Call<Void> sendPhoto(
            @Query("chat_id") long chatId,
            @Nullable @Query("reply_to_message_id") Integer replyToMessageId,
            @Nullable @Query("caption") String caption,
            @Query("parse_mode") String parseMode,
            @Part MultipartBody.Part document
    );

    @POST("sendDocument")
    @Multipart
    Call<Void> sendDocument(
            @Query("chat_id") long chatId,
            @Nullable @Query("reply_to_message_id") Integer replyToMessageId,
            @Part MultipartBody.Part document
    );

}
