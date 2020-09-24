package io.horrorshow.discordbot.horrorshow.service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.OHLCChartBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BinanceAPI {

    private final String URL_BINANCE_AVG_PRICE;

    private final RestTemplate restTemplate;

    private final BinanceApiRestClient client;

    public BinanceAPI(@Autowired RestTemplateBuilder restTemplateBuilder) {
        URL_BINANCE_AVG_PRICE = "https://api.binance.com/api/v3/avgPrice";
        if (restTemplateBuilder != null)
            this.restTemplate = restTemplateBuilder.build();
        else restTemplate = null;

        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance();
        client = factory.newRestClient();
    }

    public byte[] candleSticks(String symbol) throws IOException {
        var candleSticks =
                client.getCandlestickBars(
                        symbol,
                        CandlestickInterval.FIFTEEN_MINUTES,
                        500,
                        System.currentTimeMillis() - 1000 * 60 * 60 * 12,
                        System.currentTimeMillis());
        var chart = new OHLCChartBuilder().width(640).height(480).title(symbol).xAxisTitle("time").yAxisTitle("price").build();
        var xData = candleSticks
                .stream()
                .map(candlestick -> new Date((candlestick.getOpenTime())))
                .collect(Collectors.toList());
        List<Number> open = candleSticks
                .stream()
                .map(candlestick -> Double.parseDouble(candlestick.getOpen()))
                .collect(Collectors.toList());
        List<Number> high = candleSticks
                .stream()
                .map(candlestick -> Double.parseDouble(candlestick.getHigh()))
                .collect(Collectors.toList());
        List<Number> low = candleSticks
                .stream()
                .map(candlestick -> Double.parseDouble(candlestick.getLow()))
                .collect(Collectors.toList());
        List<Number> close = candleSticks
                .stream()
                .map(candlestick -> Double.parseDouble(candlestick.getClose()))
                .collect(Collectors.toList());
        List<Number> volume = candleSticks
                .stream()
                .map(candlestick -> Double.parseDouble(candlestick.getVolume()))
                .collect(Collectors.toList());

        chart.addSeries(symbol, xData, open, high, low, close, volume);
        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
    }

    public TickerPrice getPrice(String symbol) {
        return client.getPrice(symbol);
    }

    public String urlString(String url, Map<String, String> params) {
        return params.entrySet()
                .stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                        + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&", url + "?", ""));
    }

    public String getAveragePrice(String symbol) {
        var params = Map.of("symbol", symbol);
        return restTemplate.getForObject(urlString(URL_BINANCE_AVG_PRICE, params), String.class);
    }
}
