package com.senderman.lastkatkabot.service;

import com.senderman.lastkatkabot.config.BotConfig;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class ImageService {

    private final BotConfig config;

    public ImageService(BotConfig config) {
        this.config = config;
    }

    /**
     * Generate sticker with greeting
     *
     * @param nickname nickname of the user
     * @return File object which references generated sticker with webp format
     * @throws IOException              if it can't read original template or write a new sticker
     * @throws TooWideNicknameException if the given nickname is too wide to attach to sticker
     */
    public File generateGreetingSticker(String nickname) throws IOException, TooWideNicknameException {
        var orig = getClass().getResourceAsStream("/greeting.png");
        var img = ImageIO.read(Objects.requireNonNull(orig));
        orig.close();
        var g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        var font = new Font(Font.SANS_SERIF, Font.BOLD, 45);
        // create text which will be attached to the bottom of the image
        var text = font.createGlyphVector(g.getFontRenderContext(), nickname + "!");
        var imageWidth = img.getWidth();
        var textWidth = text.getOutline().getBounds().width;
        // if text is too wide, fail
        if (imageWidth < textWidth) throw new TooWideNicknameException(nickname);
        // align nickname horizontally to the center
        var x = (imageWidth - textWidth) / 2;
        var textOutline = text.getOutline(x, 480f);
        // border of the text will be black
        g.setColor(Color.black);
        g.setStroke(new BasicStroke(3.5f));
        g.draw(textOutline);
        // and the text itself - white
        g.setColor(Color.white);
        g.fill(textOutline);
        g.dispose();
        var out = new File(UUID.randomUUID() + ".webp");
        ImageIO.write(img, "webp", out);
        return out;
    }

    public String getHelloGifId() {
        return config.helloGifId();
    }

    public String getLeaveStickerId() {
        return config.leaveStickerId();
    }

    public static class TooWideNicknameException extends Exception {
        public TooWideNicknameException(String nickname) {
            super("Nickname " + nickname + " is too wide!");
        }
    }
}
