package io.horrorshow.discordbot.horrorshow.service;

import io.horrorshow.discordbot.horrorshow.HorrorshowDiscordBotApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/pizzabot")
@Slf4j
public class PizzaBotController {

    private final PizzaBot pizzaBot;

    public PizzaBotController(@Autowired PizzaBot pizzaBot) {
        Assert.notNull(pizzaBot, "%s must not be null".formatted(PizzaBot.class.getName()));
        this.pizzaBot = pizzaBot;
    }

    @GetMapping("/status")
    public String getPizzaBotStatus() {
        return pizzaBot.getJda().getStatus().toString();
    }

    @GetMapping("/channels")
    public String getTextChannels() {
        return pizzaBot.getTextChannels().stream()
                .map(textChannel -> "Name: " + textChannel.getName()
                        + " Id: " + textChannel.getId()
                        + " Users: " + textChannel.getMembers().size()
                        + " canTalk: " + textChannel.canTalk())
                .collect(Collectors.joining("\n"));
    }

    @PostMapping("/send/{channel}/{message}")
    public void sendMessage(@PathVariable String channel, @PathVariable String message) {
        pizzaBot.sendMessage(channel, message);
    }

    @PostMapping("/kill")
    public String killPizzaBot() {
        pizzaBot.getJda().shutdown();
        return getPizzaBotStatus();
    }

    @PostMapping("/restart")
    public void restartHorrorshowDiscordBotApplication() {
        HorrorshowDiscordBotApplication.restart();
    }

}
