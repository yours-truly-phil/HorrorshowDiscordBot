package io.horrorshow.discordbot.horrorshow.service;

import java.util.function.Consumer;

public interface RespondsToDiscordMessage<T> {

    boolean canCompute(String message);

    void computeMessage(String message, Consumer<T> consumer);

    String help();

}
