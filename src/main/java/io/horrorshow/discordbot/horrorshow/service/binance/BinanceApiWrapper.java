package io.horrorshow.discordbot.horrorshow.service.binance;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import io.horrorshow.discordbot.horrorshow.helper.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BinanceApiWrapper {

    private final RestTemplate restTemplate;
    private final BinanceApiRestClient binanceApiRestClient;

    public BinanceApiWrapper(@Autowired RestTemplateBuilder restTemplateBuilder,
                             @Autowired BinanceApiRestClient binanceApiRestClient) {
        if (restTemplateBuilder != null)
            this.restTemplate = restTemplateBuilder.build();
        else restTemplate = null;

        this.binanceApiRestClient = binanceApiRestClient;
    }

    public List<Candlestick> getCandlesticks(String symbol) {
        return binanceApiRestClient.getCandlestickBars(
                symbol,
                CandlestickInterval.FIFTEEN_MINUTES,
                500,
                System.currentTimeMillis() - 1000 * 60 * 60 * 24,
                System.currentTimeMillis());
    }

    public String get24HrSummaryString(String symbol) {
        var dayStats = binanceApiRestClient.get24HrPriceStatistics(symbol);
        var priceChangePercent = Double.parseDouble(dayStats.getPriceChangePercent());
        var priceChange = Double.parseDouble(dayStats.getPriceChange());
        return "%s Last: %s 24hr: %.2f%% (%f) volume: %s"
                .formatted(symbol, dayStats.getLastPrice(),
                        priceChangePercent, priceChange, dayStats.getVolume());
    }

    public TickerPrice getPrice(String symbol) {
        return binanceApiRestClient.getPrice(symbol);
    }

    public String getAveragePrice(String symbol) {
        var params = Map.of("symbol", symbol);
        return restTemplate.getForObject(Utils.concatUrlParams(Utils.GET_AVG_PRICE_URL, params), String.class);
    }
}
