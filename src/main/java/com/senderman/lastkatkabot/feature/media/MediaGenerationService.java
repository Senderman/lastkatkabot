package com.senderman.lastkatkabot.feature.media;

import com.senderman.lastkatkabot.feature.members.exception.TooWideNicknameException;
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

    /**
     * Generate sticker with greeting
     *
     * @param nickname nickname of the user
     * @return File object which references generated sticker with webp format
     * @throws IOException              if it can't read original template or write a new sticker
     * @throws TooWideNicknameException if the given nickname is too wide to attach to sticker
     */
    public InputStream generateGreetingSticker(String nickname) throws IOException, TooWideNicknameException {
        var orig = getClass().getResourceAsStream("/media/greeting.png");
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
