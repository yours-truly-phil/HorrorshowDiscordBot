package io.horrorshow.discordbot.horrorshow.service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.OHLCChartBuilder;
import org.knowm.xchart.OHLCSeries;
import org.knowm.xchart.style.Styler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

        var chart = new OHLCChartBuilder()
                .width(1024).height(480)
                .title(symbol)
                .xAxisTitle("time")
                .yAxisTitle("price")
                .theme(Styler.ChartTheme.Matlab)
                .build();

        var styler = chart.getStyler();
        styler.setLegendVisible(false);
        styler.setAntiAlias(true);

        styler.setPlotBackgroundColor(Color.DARK_GRAY);

        styler.setChartBackgroundColor(Color.DARK_GRAY.darker().darker());
        styler.setChartFontColor(Color.LIGHT_GRAY.brighter());
        styler.setAnnotationsFontColor(Color.RED);
        styler.setCursorFontColor(Color.GREEN);
        styler.setXAxisTitleColor(Color.LIGHT_GRAY)
                .setYAxisTitleColor(Color.LIGHT_GRAY);
        styler.setXAxisTickLabelsColor(Color.LIGHT_GRAY.brighter())
                .setYAxisTickLabelsColor(Color.LIGHT_GRAY.brighter());

        var size = candleSticks.size();
        List<Date> xData = new ArrayList<>(size);
        List<Double> open = new ArrayList<>(size);
        List<Double> high = new ArrayList<>(size);
        List<Double> low = new ArrayList<>(size);
        List<Double> close = new ArrayList<>(size);
        List<Double> volume = new ArrayList<>(size);
        for (Candlestick candle : candleSticks) {
            xData.add(new Date(candle.getOpenTime()));
            open.add(Double.parseDouble(candle.getOpen()));
            high.add(Double.parseDouble(candle.getHigh()));
            low.add(Double.parseDouble(candle.getLow()));
            close.add(Double.parseDouble(candle.getClose()));
            volume.add(Double.parseDouble(candle.getVolume()));
        }
        chart.addSeries(symbol, xData, open, high, low, close, volume)
                .setUpColor(Color.GREEN.brighter())
                .setDownColor(Color.RED.brighter())
                .setOhlcSeriesRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.HiLo);

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
