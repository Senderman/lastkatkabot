package com.senderman.lastkatkabot.service;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Client
@Requires(property = "offload.enabled", value = "true")
public interface UpdateOffloaderClient extends UpdateOffloader {

    @Override
    @Post("${offload.path}")
    @Header(name = "Authorization", value = "Bearer ${offload.token}")
    CompletableFuture<Void> offloadUpdates(@Body Collection<Update> updates);

}
