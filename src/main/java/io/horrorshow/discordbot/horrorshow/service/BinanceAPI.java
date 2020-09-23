package io.horrorshow.discordbot.horrorshow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BinanceAPI {

    private final String URL_BINANCE_AVG_PRICE;

    private final RestTemplate restTemplate;

    public BinanceAPI(@Autowired RestTemplateBuilder restTemplateBuilder) {
        URL_BINANCE_AVG_PRICE = "https://api.binance.com/api/v3/avgPrice";

        this.restTemplate = restTemplateBuilder.build();
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
