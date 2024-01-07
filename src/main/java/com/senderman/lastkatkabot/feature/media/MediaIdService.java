package com.senderman.lastkatkabot.feature.media;

import com.annimon.tgbotsmodule.api.methods.interfaces.InputFileMethod;
import com.senderman.lastkatkabot.config.model.Settings;
import com.senderman.lastkatkabot.config.service.SettingsService;
import jakarta.inject.Singleton;

import java.util.Map;

/**
 * Class to store telegram fileId in db, provide them if exists, or provide the media itself when fileId is missing
 */
@Singleton
public class MediaIdService {

    private final SettingsService repo;

    private final Map<MediaId, String> mediaPaths = Map.of(
            MediaId.BNCHELP, "/media/bnchelp.jpg",
            MediaId.GREETING_GIF, "/media/greeting_gif.mp4",
            MediaId.LEAVE_STICKER, "/media/leave_sticker.webp"
    );

    public MediaIdService(SettingsService repo) {
        this.repo = repo;
    }

    /**
     * Set media for telegram media method.
     *
     * @param method  method to modify
     * @param mediaId id of the media
     */
    public void setMedia(InputFileMethod<?, ?> method, MediaId mediaId) {
        repo.findById(mediaId.getKey()).ifPresentOrElse(
                fileId -> method.setFile(fileId.getValue()),
                () -> method.setFile(mediaId.getKey(), getClass().getResourceAsStream(mediaPaths.get(mediaId)))
        );
    }

    /**
     * Store fileId for media
     *
     * @param mediaId id of the media
     * @param fileId  telegram fileId
     */
    public void setFileId(MediaId mediaId, String fileId) {
        repo.save(new Settings(mediaId.getKey(), fileId));
    }

}
