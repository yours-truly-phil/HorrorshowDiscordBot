package io.horrorshow.discordbot.horrorshow.service.binance;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class BinanceTextTickerResponse {
    private final String text;
}
