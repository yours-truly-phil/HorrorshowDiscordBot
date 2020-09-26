package io.horrorshow.discordbot.horrorshow.service.binance;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.awt.image.BufferedImage;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class BinanceGraphsResponse {
    private final BufferedImage bufferedImage;
    private final String title;
    private final String error;
    private final boolean hasErrors;

    public BinanceGraphsResponse(BufferedImage image, String title) {
        bufferedImage = image;
        this.title = title;
        error = null;
        hasErrors = false;
    }
}
