package io.horrorshow.discordbot.horrorshow.graph;

import com.binance.api.client.domain.market.Candlestick;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.knowm.xchart.*;
import org.knowm.xchart.style.CategoryStyler;
import org.knowm.xchart.style.OHLCStyler;
import org.knowm.xchart.style.Styler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
public class CandleStickVolumeChart {

    private Color leftDecoratorColor = new Color(247, 44, 123);
    private Color upColor = new Color(67, 181, 129);
    private Color downColor = new Color(240, 71, 71);
    private Color volumeFillColor = new Color(32, 34, 37);
    private Color xAxisTickLabelsColor = new Color(220, 221, 222);
    private Color yAxisTickLabelsColor = new Color(220, 221, 222);
    private Color plotBackgroundColor = new Color(54, 57, 63);
    private Color chartBackgroundColor = new Color(47, 49, 54);
    private Color chartFontColor = new Color(220, 221, 222);
    private Color xAxisTitleColor = new Color(142, 146, 151);
    private Color yAxisTitleColor = new Color(142, 146, 151);

    private int width = 1000;
    private int candleSticksChartHeight = 450;
    private int volumeChartHeight = 180;
    private int decoratorWidth = 15;
    private int decoratorArc = 5;

    private Styler.ChartTheme chartTheme = Styler.ChartTheme.Matlab;
    private boolean isLegendVisible = false;
    private boolean antiAlias = true;
    private String title;

    public CandleStickVolumeChart setTitle(String title) {
        this.title = title;
        return this;
    }

    public CandleStickVolumeChart setAntiAlias(boolean antiAlias) {
        this.antiAlias = antiAlias;
        return this;
    }

    public CandleStickVolumeChart setIsLegendVisible(boolean isLegendVisible) {
        this.isLegendVisible = isLegendVisible;
        return this;
    }

    public CandleStickVolumeChart setChartTheme(Styler.ChartTheme chartTheme) {
        this.chartTheme = chartTheme;
        return this;
    }

    public CandleStickVolumeChart setDecoratorArc(int decoratorArc) {
        this.decoratorArc = decoratorArc;
        return this;
    }

    public CandleStickVolumeChart setDecoratorWidth(int decoratorWidth) {
        this.decoratorWidth = decoratorWidth;
        return this;
    }

    public CandleStickVolumeChart setVolumeChartHeight(int height) {
        this.volumeChartHeight = height;
        return this;
    }

    public CandleStickVolumeChart setCandleSticksChartHeight(int height) {
        this.candleSticksChartHeight = height;
        return this;
    }

    public CandleStickVolumeChart setWidth(int width) {
        this.width = width;
        return this;
    }

    public CandleStickVolumeChart setYAxisTitleColor(Color color) {
        yAxisTitleColor = color;
        return this;
    }

    public CandleStickVolumeChart setXAxisTitleColor(Color color) {
        xAxisTitleColor = color;
        return this;
    }

    public CandleStickVolumeChart setChartFontColor(Color color) {
        chartFontColor = color;
        return this;
    }

    public CandleStickVolumeChart setChartBackgroundColor(Color color) {
        chartBackgroundColor = color;
        return this;
    }

    public CandleStickVolumeChart setPlotBackgroundColor(Color color) {
        plotBackgroundColor = color;
        return this;
    }

    public CandleStickVolumeChart setYAxisTickLabelsColor(Color color) {
        yAxisTickLabelsColor = color;
        return this;
    }

    public CandleStickVolumeChart setXAxisTickLabelsColor(Color color) {
        xAxisTickLabelsColor = color;
        return this;
    }

    public CandleStickVolumeChart setVolumeChartFillColor(Color color) {
        volumeFillColor = color;
        return this;
    }

    public CandleStickVolumeChart setDownColor(Color color) {
        downColor = color;
        return this;
    }

    public CandleStickVolumeChart setUpColor(Color color) {
        upColor = color;
        return this;
    }

    public CandleStickVolumeChart setLeftDecoratorColor(Color color) {
        leftDecoratorColor = color;
        return this;
    }

    public BufferedImage createCandlesticksVolumeChart(String symbol, List<Candlestick> candlesticks) throws IOException {

        var size = candlesticks.size();

        List<Date> xDataDates = new ArrayList<>(size);
        List<Long> xDataLong = new ArrayList<>(size);
        List<Double> open = new ArrayList<>(size);
        List<Double> high = new ArrayList<>(size);
        List<Double> low = new ArrayList<>(size);
        List<Double> close = new ArrayList<>(size);
        List<Double> volume = new ArrayList<>(size);

        for (Candlestick candle : candlesticks) {
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

        return drawCandlestickVolumeChart(ohlcChart, volumeChart);
    }

    @NotNull
    private BufferedImage drawCandlestickVolumeChart(OHLCChart ohlcChart, CategoryChart volumeChart) {
        var candlesticksImg = BitmapEncoder.getBufferedImage(ohlcChart);
        var volumeImg = BitmapEncoder.getBufferedImage(volumeChart);

        var totalHeight = volumeChartHeight + candleSticksChartHeight;
        var totalWidth = width + decoratorWidth;
        var concatImg =
                new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        var g = concatImg.createGraphics();
        g.drawImage(candlesticksImg, decoratorWidth, 0, null);
        g.drawImage(volumeImg, decoratorWidth, candleSticksChartHeight, null);
        drawDecorator(g);
        g.dispose();
        return concatImg;
    }

    private void drawDecorator(Graphics2D g) {
        g.setColor(leftDecoratorColor);
        var height = volumeChartHeight + candleSticksChartHeight;
        g.fillRoundRect(0, 0, decoratorWidth - decoratorArc, height, decoratorArc, decoratorArc);
        g.fillRect(decoratorArc, 0, decoratorWidth - decoratorArc, height);
    }

    @NotNull
    private CategoryChart getVolumeChart(List<?> xData, List<Double> volume) {
        var chart = new CategoryChartBuilder()
                .width(width).height(volumeChartHeight)
                .title("Volume")
                .xAxisTitle("time")
                .yAxisTitle("volume")
                .theme(chartTheme)
                .build();

        var style = chart.getStyler();
        styleCategory(style);

        chart.addSeries("volume", xData, volume)
                .setFillColor(volumeFillColor);

        return chart;
    }

    @NotNull
    private OHLCChart getOhlcChart(String symbol, List<Date> xData,
                                   List<Double> open, List<Double> high,
                                   List<Double> low, List<Double> close,
                                   List<Double> volume) {

        var chart = new OHLCChartBuilder()
                .width(width).height(candleSticksChartHeight)
                .title((title != null) ? title : symbol)
                .xAxisTitle("time")
                .yAxisTitle("price")
                .theme(chartTheme)
                .build();

        var style = chart.getStyler();
        styleOHLC(style);

        chart.addSeries(symbol, xData, open, high, low, close, volume)
                .setUpColor(upColor)
                .setDownColor(downColor)
                .setOhlcSeriesRenderStyle(OHLCSeries.OHLCSeriesRenderStyle.HiLo);

        return chart;
    }

    private void styleCategory(CategoryStyler styler) {
        styler.setXAxisTickLabelsColor(xAxisTickLabelsColor)
                .setYAxisTickLabelsColor(yAxisTickLabelsColor);
        styler.setXAxisTicksVisible(false);
        styler.setAxisTitlesVisible(false);

        style(styler);
    }

    private void style(Styler styler) {
        styler.setLegendVisible(isLegendVisible);
        styler.setAntiAlias(antiAlias);

        styler.setPlotBackgroundColor(plotBackgroundColor);

        styler.setChartBackgroundColor(chartBackgroundColor);
        styler.setChartFontColor(chartFontColor);
        styler.setXAxisTitleColor(xAxisTitleColor)
                .setYAxisTitleColor(yAxisTitleColor);
    }

    private void styleOHLC(OHLCStyler styler) {
        styler.setXAxisTickLabelsColor(xAxisTickLabelsColor)
                .setYAxisTickLabelsColor(yAxisTickLabelsColor);
        styler.setAxisTitlesVisible(false);

        style(styler);
    }
}
