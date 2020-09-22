package io.horrorshow.discordbot.horrorshow.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;

@RestController
@Slf4j
public class BotController {

    private static final String PROP_TOKEN = "${jda.discord.token}";

    private final PizzaBot pizzaBot;

    private final String token;

    public BotController(@Autowired PizzaBot pizzaBot,
                         @Autowired @Value(PROP_TOKEN) String token) {
        Assert.notNull(token, "Token must not be null, did you forget to set ${%s}?".formatted(PROP_TOKEN));
        Assert.notNull(pizzaBot, "%s must not be null".formatted(PizzaBot.class.getName()));
        this.token = token;
        this.pizzaBot = pizzaBot;
    }

    @GetMapping("/pizzabot/startup")
    public String startUpPizzaBot() {
        if (pizzaBot.getJda() == null || pizzaBot.getJda().getStatus() == JDA.Status.DISCONNECTED) {
            try {
                pizzaBot.startUp(token);
            } catch (LoginException e) {
                log.error("Login Exception", e);
                return e.getLocalizedMessage();
            }
            return "Starting up";
        } else {
            return "Status is ${%s}".formatted(pizzaBot.getJda().getStatus().toString());
        }
    }

}
