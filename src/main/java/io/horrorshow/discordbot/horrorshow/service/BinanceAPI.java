package io.horrorshow.discordbot.horrorshow.service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import io.horrorshow.discordbot.horrorshow.graph.CandleStickVolumeChart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
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
                        System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                        System.currentTimeMillis());

        return new CandleStickVolumeChart().createChartAsBytes(symbol, candleSticks);
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
