package io.horrorshow.discordbot.horrorshow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;

@Controller
public class BotController {

    private static final String PROP_TOKEN = "${jda.discord.token}";

    private final PizzaBot pizzaBot;

    public BotController(@Autowired PizzaBot pizzaBot,
                         @Autowired @Value(PROP_TOKEN) String token) {
        Assert.notNull(token, "Token must not be null, did you forget to set ${%s}?".formatted(PROP_TOKEN));
        Assert.notNull(pizzaBot, "%s must not be null".formatted(PizzaBot.class.getName()));
        this.pizzaBot = pizzaBot;

        pizzaBot.startUp(token);
    }

}
