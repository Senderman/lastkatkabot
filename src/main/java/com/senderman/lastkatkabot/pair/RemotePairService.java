package com.senderman.lastkatkabot.pair;

import com.senderman.lastkatkabot.pair.entity.ChatPair;
import com.senderman.lastkatkabot.pair.entity.ResponseResult;
import com.senderman.lastkatkabot.pair.entity.UserInChat;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RemotePairService {

    @GET("getPair")
    Call<ResponseResult<ChatPair>> getPair(@Query("chat_id") long chatId);

    @GET("isUserInChat")
    Call<ResponseResult<UserInChat>> isUserInChat(
            @Query("chat_id") long chatId,
            @Query("user_id") int userId
    );

}
