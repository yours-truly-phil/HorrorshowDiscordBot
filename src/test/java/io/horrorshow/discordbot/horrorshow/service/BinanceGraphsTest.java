package io.horrorshow.discordbot.horrorshow.service;

import io.horrorshow.discordbot.horrorshow.service.binance.BinanceGraphs;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

class BinanceGraphsTest {

    BinanceGraphs binanceGraphs;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
        binanceGraphs = new BinanceGraphs(null);
    }


}