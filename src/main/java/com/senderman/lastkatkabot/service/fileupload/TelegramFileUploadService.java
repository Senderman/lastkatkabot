package com.senderman.lastkatkabot.service.fileupload;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import javax.annotation.Nullable;

public interface TelegramFileUploadService {

    @POST("sendDocument")
    @Multipart
    Call<Void> sendDocument(
            @Query("chat_id") long chatId,
            @Nullable @Query("reply_to_message_id") Integer replyToMessageId,
            @Part MultipartBody.Part document
    );

}
