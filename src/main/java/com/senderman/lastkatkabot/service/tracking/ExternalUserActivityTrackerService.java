package com.senderman.lastkatkabot.service.tracking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

@Service
public class ExternalUserActivityTrackerService implements UserActivityTrackerService {

    private final ExternalTrackerRequestService requestService;
    private final String externalToken;

    public ExternalUserActivityTrackerService(
            @Value("${externalTrackerToken}") String externalToken,
            @Value("${externalTrackerUrl}") String externalTrackerUrl
    ) {
        this.externalToken = externalToken;
        this.requestService = new Retrofit.Builder()
                .baseUrl(externalTrackerUrl)
                .build()
                .create(ExternalTrackerRequestService.class);
    }

    @Override
    public void updateLastMessageDate(long chatId, int userId, int messageLastDate) {
        requestService.update(chatId, userId, messageLastDate, externalToken).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }
}
