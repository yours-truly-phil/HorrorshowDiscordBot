package io.horrorshow.discordbot.horrorshow.service;

import io.horrorshow.discordbot.horrorshow.HorrorshowDiscordBotApplication;
import io.horrorshow.discordbot.horrorshow.controller.DiscordBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/bot")
@Slf4j
public class BotController {

    private final DiscordBot discordBot;

    public BotController(@Autowired DiscordBot discordBot) {
        Assert.notNull(discordBot, "%s must not be null".formatted(DiscordBot.class.getName()));
        this.discordBot = discordBot;
    }

    @GetMapping("/status")
    public String getPizzaBotStatus() {
        return discordBot.getJda().getStatus().toString();
    }

    @GetMapping("/channels")
    public String getTextChannels() {
        return discordBot.getTextChannels().stream()
                .map(textChannel -> "Name: " + textChannel.getName()
                        + " Id: " + textChannel.getId()
                        + " Users: " + textChannel.getMembers().size()
                        + " canTalk: " + textChannel.canTalk())
                .collect(Collectors.joining("\n"));
    }

    @PostMapping("/send/{channel}/{message}")
    public void sendMessage(@PathVariable String channel, @PathVariable String message) {
        discordBot.sendMessage(channel, message);
    }

    @PostMapping("/kill")
    public String killPizzaBot() {
        discordBot.getJda().shutdown();
        return getPizzaBotStatus();
    }

    @PostMapping("/restart")
    public void restartHorrorshowDiscordBotApplication() {
        HorrorshowDiscordBotApplication.restart();
    }

}
