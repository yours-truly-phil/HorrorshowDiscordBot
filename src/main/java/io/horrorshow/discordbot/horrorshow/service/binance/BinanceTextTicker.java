package io.horrorshow.discordbot.horrorshow.service.binance;

import io.horrorshow.discordbot.horrorshow.service.RespondsToDiscordMessage;
import io.horrorshow.discordbot.horrorshow.service.response.TextResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BinanceTextTicker implements RespondsToDiscordMessage<TextResponse> {

    private static final String CMD_AVERAGE_PRICE = "^\\$avgPrice [A-Z0-9-_.]{1,20}$";
    private static final String CMD_PRICE = "^\\$price [A-Z0-9-_.]{1,20}$";
    private static final String CMD_ALL_TOKENS = "^\\$allTokens$";
    private static final String CMD_ALL_PRICES = "^\\$allPrices$";

    private static final Set<String> MATCHES = Set.of(CMD_AVERAGE_PRICE, CMD_PRICE, CMD_ALL_TOKENS, CMD_ALL_PRICES);

    private final BinanceApiWrapper binanceApi;

    public BinanceTextTicker(@Autowired BinanceApiWrapper binanceApiWrapper) {
        this.binanceApi = binanceApiWrapper;
    }

    private String[] token(String s) {
        return s.split(" ");
    }

    private TextResponse getPriceOf(String rawMsgContent) {
        var t = token(rawMsgContent);
        if (t.length > 1)
            return new TextResponse(binanceApi.getPrice(t[1]).toString());
        else
            return new TextResponse("missing symbol parameter: $price <SYMBOL>");
    }

    private TextResponse getAvgPriceOf(String rawMsgContent) {
        var tokens = rawMsgContent.split(" ");
        if (tokens.length > 1)
            return new TextResponse(binanceApi.getAveragePrice(tokens[1]));
        else
            return new TextResponse("missing symbol parameter: $avgPrice <SYMBOL>");
    }

    private List<TextResponse> getAllTokens() {
        int max = 2000;
        var tokensString = String.join(", ", binanceApi.getAllTokens());
        return divideStringIntoParts(max, tokensString);
    }

    @NotNull
    private List<TextResponse> divideStringIntoParts(int max, String tokensString) {
        List<String> parts = new ArrayList<>();
        for (int i = 0; i < tokensString.length(); i += max) {
            parts.add(tokensString.substring(i, Math.min(tokensString.length(), i + max)));
        }
        return parts.stream().map(TextResponse::new).collect(Collectors.toList());
    }

    private List<TextResponse> getAllPrices() {
        int max = 2000;
        var allPrices = binanceApi.getAllPrices()
                .stream().map(tickerPrice -> "[" + tickerPrice.getSymbol() + ":" + tickerPrice.getPrice() + "]")
                .collect(Collectors.joining(""));
        return divideStringIntoParts(max, allPrices);
    }

    @Override
    public boolean canCompute(String message) {
        return MATCHES.stream().anyMatch(message::matches);
    }

    @Override
    public void computeMessage(String message, Consumer<TextResponse> consumer) {
        if (message.matches(CMD_AVERAGE_PRICE)) {
            consumer.accept(getAvgPriceOf(message));
        } else if (message.matches(CMD_PRICE)) {
            consumer.accept(getPriceOf(message));
        } else if (message.matches(CMD_ALL_TOKENS)) {
            getAllTokens().forEach(consumer);
        } else if (message.matches(CMD_ALL_PRICES)) {
            getAllPrices().forEach(consumer);
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
