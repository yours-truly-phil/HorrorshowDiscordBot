package io.horrorshow.discordbot.horrorshow.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.util.Map;

class BinanceAPITest {

    @Mock
    RestTemplateBuilder restTemplateBuilder;

    BinanceAPI binanceAPI;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
        binanceAPI = new BinanceAPI(restTemplateBuilder);
    }

    @Test
    void getAveragePrice() {
        Assertions.assertThat(binanceAPI.urlString(
                "https://api.binance.com/api/v3/avgPrice",
                Map.of("symbol", "ADAUSDT")))
                .isEqualTo("https://api.binance.com/api/v3/avgPrice?symbol=ADAUSDT");
    }
}