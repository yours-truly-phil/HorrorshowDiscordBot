package io.horrorshow.discordbot.horrorshow.service;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.*;
import org.knowm.xchart.style.OHLCStyler;
import org.knowm.xchart.style.Styler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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

        var size = candleSticks.size();
        List<Date> xDataDates = new ArrayList<>(size);
        List<Long> xDataLong = new ArrayList<>(size);
        List<Double> open = new ArrayList<>(size);
        List<Double> high = new ArrayList<>(size);
        List<Double> low = new ArrayList<>(size);
        List<Double> close = new ArrayList<>(size);
        List<Double> volume = new ArrayList<>(size);
        for (Candlestick candle : candleSticks) {
            xDataDates.add(new Date(candle.getOpenTime()));
            xDataLong.add(candle.getOpenTime());
            open.add(Double.parseDouble(candle.getOpen()));
            high.add(Double.parseDouble(candle.getHigh()));
            low.add(Double.parseDouble(candle.getLow()));
            close.add(Double.parseDouble(candle.getClose()));
            volume.add(Double.parseDouble(candle.getVolume()));
        }

        var ohlcChart = getOhlcChart(symbol, xDataDates, open, high, low, close, volume);
        var volumeChart = getVolumeChart(xDataLong, volume);


        var candlesticksImg = BitmapEncoder.getBufferedImage(ohlcChart);
        var volumeImg = BitmapEncoder.getBufferedImage(volumeChart);

        var concatImg = new BufferedImage(1024, 640, BufferedImage.TYPE_INT_RGB);
        var graphics = concatImg.createGraphics();
        graphics.drawImage(candlesticksImg, 0, 0, null);
        graphics.drawImage(volumeImg, 0, candlesticksImg.getHeight(), null);
        graphics.dispose();
        var baos = new ByteArrayOutputStream();
        ImageIO.write(concatImg, "png", baos);
        baos.flush();
        return baos.toByteArray();
    }

    @NotNull
    private CategoryChart getVolumeChart(List<?> xData, List<Double> volume) {
        var chart = new CategoryChartBuilder()
                .width(1024).height(200)
                .title("Volume")
                .xAxisTitle("time")
                .yAxisTitle("volume")
                .theme(Styler.ChartTheme.Matlab)
                .build();

        var style = chart.getStyler();
        style(style);

        chart.addSeries("volume", xData, volume)
                .setFillColor(Color.DARK_GRAY.darker().darker());

        return chart;
    }

    @NotNull
    private OHLCChart getOhlcChart(String symbol, List<Date> xData,
                                   List<Double> open, List<Double> high,
                                   List<Double> low, List<Double> close,
                                   List<Double> volume) {

        var chart = new OHLCChartBuilder()
                .width(1024).height(480)
                .title(symbol)
                .xAxisTitle("time")
                .yAxisTitle("price")
                .theme(Styler.ChartTheme.Matlab)
                .build();

        var style = chart.getStyler();
        styleOHLC(style);

        chart.addSeries(symbol, xData, open, high, low, close, volume)
                .setUpColor(Color.GREEN.brighter())
                .setDownColor(Color.RED.brighter())
                .setOhlcSeriesRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.HiLo);
        return chart;
    }

    private void style(Styler styler) {
        styler.setLegendVisible(false);
        styler.setAntiAlias(true);

        styler.setPlotBackgroundColor(Color.DARK_GRAY);

        styler.setChartBackgroundColor(Color.DARK_GRAY.darker().darker());
        styler.setChartFontColor(Color.LIGHT_GRAY.brighter());
        styler.setXAxisTitleColor(Color.LIGHT_GRAY)
                .setYAxisTitleColor(Color.LIGHT_GRAY);
    }

    private void styleOHLC(OHLCStyler styler) {
        styler.setXAxisTickLabelsColor(Color.LIGHT_GRAY.brighter())
                .setYAxisTickLabelsColor(Color.LIGHT_GRAY.brighter());

        style(styler);
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
