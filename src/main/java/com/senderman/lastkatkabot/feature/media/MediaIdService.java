package com.senderman.lastkatkabot.feature.media;

import com.annimon.tgbotsmodule.api.methods.interfaces.InputFileMethod;
import com.senderman.lastkatkabot.config.model.Settings;
import com.senderman.lastkatkabot.config.service.SettingsService;
import jakarta.inject.Singleton;

/**
 * Class to store telegram fileId in db, provide them if exists, or provide the media itself when fileId is missing
 */
@Singleton
public class MediaIdService {

    private final SettingsService repo;

    public MediaIdService(SettingsService repo) {
        this.repo = repo;
    }

    /**
     * Set media for telegram media method.
     *
     * @param method  method to modify
     * @param media media
     */
    public void setMedia(InputFileMethod<?, ?> method, Media media) {
        repo.findById(media.getKey()).ifPresentOrElse(
                fileId -> method.setFile(fileId.getValue()),
                () -> method.setFile(media.getName(), getClass().getResourceAsStream(media.getPath()))
        );
    }

    /**
     * Store fileId for media
     *
     * @param media id of the media
     * @param fileId  telegram fileId
     */
    public void setFileId(Media media, String fileId) {
        repo.save(new Settings(media.getKey(), fileId));
    }

}
