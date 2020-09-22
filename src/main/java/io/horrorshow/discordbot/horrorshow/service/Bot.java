package io.horrorshow.discordbot.horrorshow.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDABuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.security.auth.login.LoginException;

@Service
@Slf4j
public class Bot {

    private static final String PROP_TOKEN = "jda.discord.token";

    public Bot(@Autowired @Value("${" + PROP_TOKEN + "}") String TOKEN) {
        Assert.notNull(TOKEN, "Token must not be null, did you forget to set ${" + PROP_TOKEN + "}?");
        try {
            JDABuilder.createDefault(TOKEN).build();
        } catch (LoginException e) {
            log.error("Login Exception during JDA creation", e);
        }
    }
}
