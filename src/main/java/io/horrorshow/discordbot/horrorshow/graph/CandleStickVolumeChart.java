package io.horrorshow.discordbot.horrorshow.graph;

import com.binance.api.client.domain.market.Candlestick;
import lombok.Getter;
import lombok.Setter;
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
@Setter
public class CandleStickVolumeChart {

    private final List<Date> xDataDates = new ArrayList<>();
    private final List<Long> xDataLong = new ArrayList<>();
    private final List<Double> open = new ArrayList<>();
    private final List<Double> high = new ArrayList<>();
    private final List<Double> low = new ArrayList<>();
    private final List<Double> close = new ArrayList<>();
    private final List<Double> volume = new ArrayList<>();
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
    private Point candleStickChartSize = new Point(1000, 440);
    private Point volumeChartSize = new Point(1000, 200);
    private Point decoratorSize = new Point(15, candleStickChartSize.y + volumeChartSize.y);
    private int decoratorArc = 5;
    private Styler.ChartTheme chartTheme = Styler.ChartTheme.Matlab;
    private boolean isLegendVisible = false;
    private boolean antiAlias = true;

    public CandleStickVolumeChart() {

    }

    public byte[] createChartAsBytes(String symbol, List<Candlestick> candlesticks) throws IOException {
        for (Candlestick candle : candlesticks) {
            addToXDataOpenHighLowCloseVolumeLists(candle);
        }

        var ohlcChart = getOhlcChart(symbol, xDataDates, open, high, low, close, volume);
        var volumeChart = getVolumeChart(xDataLong, volume);

        BufferedImage image = drawCandlestickVolumeChart(ohlcChart, volumeChart);

        return getBytes(image);
    }

    private void addToXDataOpenHighLowCloseVolumeLists(Candlestick candle) {
        xDataDates.add(new Date(candle.getOpenTime()));
        xDataLong.add(candle.getOpenTime());
        open.add(Double.parseDouble(candle.getOpen()));
        high.add(Double.parseDouble(candle.getHigh()));
        low.add(Double.parseDouble(candle.getLow()));
        close.add(Double.parseDouble(candle.getClose()));
        volume.add(Double.parseDouble(candle.getVolume()));
    }

    private byte[] getBytes(BufferedImage image) throws IOException {
        var baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        baos.flush();
        return baos.toByteArray();
    }

    @NotNull
    private BufferedImage drawCandlestickVolumeChart(OHLCChart ohlcChart, CategoryChart volumeChart) {
        var candlesticksImg = BitmapEncoder.getBufferedImage(ohlcChart);
        var volumeImg = BitmapEncoder.getBufferedImage(volumeChart);

        var concatImg =
                new BufferedImage(
                        Math.max(candleStickChartSize.x, volumeChartSize.x) + decoratorSize.x,
                        candleStickChartSize.y + volumeChartSize.y,
                        BufferedImage.TYPE_INT_RGB);
        var g = concatImg.createGraphics();
        g.drawImage(candlesticksImg, decoratorSize.x, 0, null);
        g.drawImage(volumeImg, decoratorSize.x, candleStickChartSize.y, null);
        drawDecorator(g);
        g.dispose();
        return concatImg;
    }

    private void drawDecorator(Graphics2D g) {
        g.setColor(leftDecoratorColor);
        g.fillRoundRect(0, 0, decoratorSize.x - decoratorArc, decoratorSize.y, decoratorArc, decoratorArc);
        g.fillRect(decoratorArc, 0, decoratorSize.x - decoratorArc, decoratorSize.y);
    }

    @NotNull
    private CategoryChart getVolumeChart(List<?> xData, List<Double> volume) {
        var chart = new CategoryChartBuilder()
                .width(volumeChartSize.x).height(volumeChartSize.y)
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
                .width(candleStickChartSize.x).height(candleStickChartSize.y)
                .title(symbol)
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
