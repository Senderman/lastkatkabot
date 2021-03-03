package com.senderman.lastkatkabot.pair;

import com.senderman.lastkatkabot.dbservice.ChatUserService;
import com.senderman.lastkatkabot.model.ChatUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PairService implements ChatUserService {

    private final RemotePairService remotePairService;

    public PairService(@Value("${remotePairServiceUrl}") String remotePairServiceUrl) {
        this.remotePairService = new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .baseUrl(remotePairServiceUrl)
                .build()
                .create(RemotePairService.class);
    }

    @Override
    public Stream<ChatUser> findAll() {
        return Stream.empty();
    }

    @Override
    public void deleteByChatIdAndUserId(long chatId, int userId) {
    }

    @Override
    public List<ChatUser> getTwoOrLessUsersOfChat(long chatId) {
        try {
            var response = remotePairService.getPair(chatId).execute();
            if (!response.isSuccessful()) {
                return List.of();
            }
            var listResult = response.body();
            if (!listResult.isOk()) {
                System.err.println(listResult.getReason());
                return List.of();
            }
            return listResult.getResult().getPair().stream()
                    .map(u -> new ChatUser(chatId, u))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public boolean chatHasUser(long chatId, int userId) {
        try {
            var response = remotePairService.isUserInChat(chatId, userId).execute();
            if (!response.isSuccessful())
                return false;
            return response.body().getResult().getResult();
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void deleteInactiveChatUsers(long chatId) {
    }

    @Override
    public void delete(ChatUser chatUser) {

    }

    @Override
    public Iterable<ChatUser> saveAll(Iterable<ChatUser> chatUsers) {
        return null;
    }

    @Override
    public long getTotalUsers() {
        return -1;
    }

    @Override
    public long getTotalChats() {
        return -1;
    }
}
