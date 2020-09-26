package io.horrorshow.discordbot.horrorshow.configuration;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BinanceApiConfiguration {

    @Bean
    public BinanceApiRestClient binanceApiRestClient() {
        return BinanceApiClientFactory.newInstance().newRestClient();
    }

}
