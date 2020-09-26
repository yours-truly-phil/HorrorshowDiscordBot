package io.horrorshow.discordbot.horrorshow.helper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

class UtilsTest {

    @Test
    void concatUrlParams() {
        Assertions.assertThat(Utils.concatUrlParams(
                "https://api.binance.com/api/v3/avgPrice",
                Map.of("symbol", "ADAUSDT")))
                .isEqualTo("https://api.binance.com/api/v3/avgPrice?symbol=ADAUSDT");
    }
}