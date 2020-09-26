package io.horrorshow.discordbot.horrorshow.service.binance;

import io.horrorshow.discordbot.horrorshow.service.RespondsToDiscordMessage;
import io.horrorshow.discordbot.horrorshow.service.response.TextResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class BinanceTextTicker implements RespondsToDiscordMessage<TextResponse> {

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

    private TextResponse binancePriceOf(String rawMsgContent) {
        var t = token(rawMsgContent);
        if (t.length > 1)
            return new TextResponse(binanceApi.getPrice(t[1]).toString());
        else
            return new TextResponse("missing symbol parameter: $price <SYMBOL>");
    }

    private TextResponse binanceAvgPrice(String rawMsgContent) {
        var tokens = rawMsgContent.split(" ");
        if (tokens.length > 1)
            return new TextResponse(binanceApi.getAveragePrice(tokens[1]));
        else
            return new TextResponse("missing symbol parameter: $avgPrice <SYMBOL>");
    }

    @Override
    public boolean canCompute(String message) {
        return MATCHES.stream().anyMatch(message::matches);
    }

    @Override
    public void computeMessage(String message, Consumer<TextResponse> consumer) {
        if (message.matches(CMD_AVERAGE_PRICE)) {
            consumer.accept(binanceAvgPrice(message));
        } else if (message.matches(CMD_PRICE)) {
            consumer.accept(binancePriceOf(message));
        } else {
            consumer.accept(new TextResponse("couldn't compute message %s".formatted(message)));
        }
    }

    @Override
    public String help() {
        return "Available commands for %s\n%s"
                .formatted(this.getClass().getSimpleName(),
                        MATCHES.stream().map(s -> "    " + s)
                                .collect(Collectors.joining("\n")));
    }
}
