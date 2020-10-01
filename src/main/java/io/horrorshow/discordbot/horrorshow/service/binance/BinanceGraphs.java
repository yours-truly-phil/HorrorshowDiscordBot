package io.horrorshow.discordbot.horrorshow.service.binance;

import io.horrorshow.discordbot.horrorshow.graph.CandleStickVolumeChart;
import io.horrorshow.discordbot.horrorshow.service.RespondsToDiscordMessage;
import io.horrorshow.discordbot.horrorshow.service.response.ImageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Service
@Slf4j
public class BinanceGraphs implements RespondsToDiscordMessage<ImageResponse> {

    private static final Pattern CMD_CANDLESTICKS = Pattern.compile("^\\$candlesticks [A-Z0-9-_.]{1,20}$");

    private final BinanceApiWrapper binanceApi;

    public BinanceGraphs(@Autowired BinanceApiWrapper binanceApiWrapper) {
        this.binanceApi = binanceApiWrapper;
    }

    public BufferedImage candleSticksVolumeChartImage(String symbol) throws ExecutionException, InterruptedException {
        return candleSticksVolumeChartImageAsync(symbol).get();
    }

    public CompletableFuture<BufferedImage> candleSticksVolumeChartImageAsync(String symbol) {
        var candlesticks =
                CompletableFuture.supplyAsync(() -> binanceApi.getCandlesticks(symbol));

        var title =
                CompletableFuture.supplyAsync(() -> binanceApi.get24HrSummaryString(symbol));

        return candlesticks.thenCombine(title, (c, t) ->
                new CandleStickVolumeChart().setTitle(t).createCandlesticksVolumeChart(symbol, c));
    }

    @Override
    public boolean canCompute(String msg) {
        return CMD_CANDLESTICKS.matcher(msg).matches();
    }

    @Override
    public void computeMessage(String message, Consumer<ImageResponse> consumer) {
        var tokens = message.split(" ");
        if (tokens.length > 1) {
            try {
                var image = candleSticksVolumeChartImage(tokens[1]);
                var response = new ImageResponse(image, tokens[1] + ".png");
                consumer.accept(response);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Problem creating candlesticks volume chart image", e);
            }
        }
    }

    @Override
    public String help() {
        return "Available commands for %s\n    %s"
                .formatted(BinanceGraphs.class.getSimpleName(), CMD_CANDLESTICKS);
    }
}
