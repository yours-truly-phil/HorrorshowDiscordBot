package io.horrorshow.discordbot.horrorshow.service;

import org.springframework.scheduling.annotation.Async;

import java.util.function.Consumer;

public interface RespondsToDiscordMessage<T> {

    boolean canCompute(String message);

    @Async
    void computeMessage(String message, Consumer<T> consumer);

    String help();

}
