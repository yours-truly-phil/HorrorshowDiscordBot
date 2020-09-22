package io.horrorshow.discordbot.horrorshow.service;

import io.horrorshow.discordbot.horrorshow.HorrorshowDiscordBotApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/kill")
    public String killPizzaBot() {
        pizzaBot.getJda().shutdown();
        return getPizzaBotStatus();
    }

    @GetMapping("/restart")
    public void restartHorrorshowDiscordBotApplication() {
        HorrorshowDiscordBotApplication.restart();
    }

}
