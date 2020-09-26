package io.horrorshow.discordbot.horrorshow.helper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    public static final String GET_AVG_PRICE_URL = "https://api.binance.com/api/v3/avgPrice";
    public static String concatUrlParams(String url, Map<String, String> params) {
        return params.entrySet()
                .stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&", url + "?", ""));
    }

    public static byte[] toBytes(BufferedImage image) throws IOException {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        return baos.toByteArray();
    }
}
