package io.horrorshow.discordbot.horrorshow.service.binance;

import com.binance.api.client.exception.BinanceApiException;
import io.horrorshow.discordbot.horrorshow.graph.CandleStickVolumeChart;
import io.horrorshow.discordbot.horrorshow.service.RespondsToDiscordMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.function.Consumer;

@Service
@Slf4j
public class BinanceGraphs implements RespondsToDiscordMessage<BinanceGraphsResponse> {

    private static final String CMD_CANDLESTICKS = "\\$candlesticks [A-Z0-9-_.]{1,20}$";

    private final BinanceApiWrapper binanceApi;

    public BinanceGraphs(@Autowired BinanceApiWrapper binanceApiWrapper) {
        this.binanceApi = binanceApiWrapper;
    }

    public BufferedImage candleSticksVolumeChartImage(String symbol) throws IOException, BinanceApiException {
        var candleSticks = binanceApi.getCandlesticks(symbol);

        String title = binanceApi.get24HrSummaryString(symbol);

        return new CandleStickVolumeChart()
                .setTitle(title)
                .createCandlesticksVolumeChart(symbol, candleSticks);
    }

    @Override
    public boolean matches(String msg) {
        return msg.matches(CMD_CANDLESTICKS);
    }

    @Override
    public void computeMessage(String message, Consumer<BinanceGraphsResponse> consumer) {
        var tokens = message.split(" ");
        if (tokens.length > 1) {
            try {
                var image = candleSticksVolumeChartImage(tokens[1]);
                var response = new BinanceGraphsResponse(image, tokens[1] + ".png");
                consumer.accept(response);
            } catch (IOException e) {
                log.error("error creating candlesticks volume chart image for token {}", tokens[1], e);
            }
        }
    }
}
