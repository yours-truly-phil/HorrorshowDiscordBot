package io.horrorshow.discordbot.horrorshow.service.binance;

import io.horrorshow.discordbot.horrorshow.service.RespondsToDiscordMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Consumer;

@Service
public class BinanceTextTicker implements RespondsToDiscordMessage<BinanceTextTickerResponse> {

    private static final String CMD_AVERAGE_PRICE = "^\\$avgPrice [A-Z0-9-_.]{1,20}$";
    private static final String CMD_PRICE = "^\\$price [A-Z0-9-_.]{1,20}$";

    private static final Set<String> MATCHES = Set.of(CMD_AVERAGE_PRICE, CMD_PRICE);

    private final BinanceApiWrapper binanceApi;

    public BinanceTextTicker(@Autowired BinanceApiWrapper binanceApiWrapper) {
        this.binanceApi = binanceApiWrapper;
    }

    private String[] token(String s) {
        return s.split(" ");
    }

    private BinanceTextTickerResponse binancePriceOf(String rawMsgContent) {
        var t = token(rawMsgContent);
        if (t.length > 1)
            return new BinanceTextTickerResponse(binanceApi.getPrice(t[1]).toString());
        else
            return new BinanceTextTickerResponse("missing symbol parameter: $price <SYMBOL>");
    }

    private BinanceTextTickerResponse binanceAvgPrice(String rawMsgContent) {
        var tokens = rawMsgContent.split(" ");
        if (tokens.length > 1)
            return new BinanceTextTickerResponse(binanceApi.getAveragePrice(tokens[1]));
        else
            return new BinanceTextTickerResponse("missing symbol parameter: $avgPrice <SYMBOL>");
    }

    @Override
    public boolean matches(String message) {
        return MATCHES.stream().anyMatch(message::matches);
    }

    @Override
    public void computeMessage(String message, Consumer<BinanceTextTickerResponse> consumer) {
        if (message.matches(CMD_AVERAGE_PRICE)) {
            consumer.accept(binanceAvgPrice(message));
        } else if (message.matches(CMD_PRICE)) {
            consumer.accept(binancePriceOf(message));
        } else {
            consumer.accept(new BinanceTextTickerResponse("couldn't compute message %s".formatted(message)));
        }
    }
}
