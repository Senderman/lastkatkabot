package com.senderman.lastkatkabot.feature.media;

import com.senderman.lastkatkabot.feature.l10n.service.L10nService;
import com.senderman.lastkatkabot.feature.members.exception.TooWideNicknameException;
import io.micrometer.core.annotation.Counted;
import jakarta.inject.Singleton;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Singleton
public class MediaGenerationService {

    private final L10nService l10n;

    public MediaGenerationService(L10nService l10n) {
        this.l10n = l10n;
    }

    /**
     * Generate sticker with greeting
     *
     * @param nickname nickname of the user
     * @return File object which references generated sticker with webp format
     * @throws IOException              if it can't read original template or write a new sticker
     * @throws TooWideNicknameException if the given nickname is too wide to attach to sticker
     */
    @Counted("greeting_sticker_generation")
    public InputStream generateGreetingSticker(String nickname, String locale) throws IOException, TooWideNicknameException {
        var orig = getClass().getResourceAsStream("/media/greeting.png");
        var img = ImageIO.read(Objects.requireNonNull(orig));
        orig.close();
        var g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        var font = new Font(Font.SANS_SERIF, Font.BOLD, 45);
        var imageWidth = img.getWidth();

        // create title text
        var title = font.createGlyphVector(g.getFontRenderContext(), l10n.getString("greeting.title", locale));
        var titleWidth = title.getOutline().getBounds().width;
        // align title horizontally to the center
        var titleOutline = title.getOutline((imageWidth - titleWidth) / 2f, 50f);
        // border of the text will be black
        var stroke = new BasicStroke(3.5f);
        g.setColor(Color.black);
        g.setStroke(stroke);
        g.draw(titleOutline);
        // and the text itself - white
        g.setColor(Color.white);
        g.fill(titleOutline);

        // create text which will be attached to the bottom of the image
        var text = font.createGlyphVector(g.getFontRenderContext(), nickname + "!");
        var textWidth = text.getOutline().getBounds().width;
        // if text is too wide, fail
        if (imageWidth < textWidth) throw new TooWideNicknameException(nickname);
        // align nickname horizontally to the center
        var textOutline = text.getOutline((imageWidth - textWidth) / 2f, 480f);
        // border of the text will be black
        g.setColor(Color.black);
        g.setStroke(stroke);
        g.draw(textOutline);
        // and the text itself - white
        g.setColor(Color.white);
        g.fill(textOutline);
        g.dispose();
        try (var out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            out.flush();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public InputStream generateWeatherImage(String[] input) throws IOException {
        int width = 1600;
        int height = 800;
        var img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var g = img.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
        g.setColor(Color.WHITE);
        int fontHeight = g.getFontMetrics().getHeight();
        for (int i = 0; i < input.length; i++) {
            int yPos = 50 + i * fontHeight;
            String line = input[i];
            g.drawString(line, 50, yPos);
        }
        g.dispose();
        try (var out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            out.flush();
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
