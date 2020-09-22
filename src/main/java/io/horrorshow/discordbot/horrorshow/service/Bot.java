package io.horrorshow.discordbot.horrorshow.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.security.auth.login.LoginException;

@Service
@Slf4j
public class Bot extends ListenerAdapter {

    private static final String PROP_TOKEN = "jda.discord.token";

    private static JDA jda;

    public Bot(@Autowired @Value("${" + PROP_TOKEN + "}") String TOKEN) {
        Assert.notNull(TOKEN, "Token must not be null, did you forget to set ${" + PROP_TOKEN + "}?");
        try {
            jda = JDABuilder.createDefault(TOKEN).build();
            Assert.notNull(jda, "JDA must not be null, check the logs");

            jda.addEventListener(this);

        } catch (LoginException e) {
            log.error("Login Exception during JDA creation", e);
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            event.getMessage().addReaction("\uD83C\uDF55").queue();
            event.getChannel().sendMessage("Pizza").queue(message -> {
                message.addReaction("\uD83C\uDF55").queue();
            });
        }
    }
}
