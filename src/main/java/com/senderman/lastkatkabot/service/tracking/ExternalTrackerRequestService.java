package com.senderman.lastkatkabot.service.tracking;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ExternalTrackerRequestService {

    @POST("update")
    Call<Void> update(
            @Query("chatId") long chatId,
            @Query("userId") int userId,
            @Query("lastMessageDate") int lastMessageDate,
            @Query("token") String token
    );

}
