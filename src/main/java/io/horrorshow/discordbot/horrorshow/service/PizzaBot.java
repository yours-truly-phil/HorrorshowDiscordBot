package io.horrorshow.discordbot.horrorshow.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PizzaBot extends ListenerAdapter {

    private static final String PROP_TOKEN = "${jda.discord.token}";

    private static final String EMOJI_PIZZA = "\uD83C\uDF55";
    private static final String CMD_PRINT_MESSAGES = "\\$messages";
    private static final String CMD_AVERAGE_PRICE = "\\$avgPrice .+";
    private static final String CMD_PRICE = "\\$price \\w+$";
    private static final String CMD_CANDLESTICKS = "\\$candlesticks \\w+$";

    private final Map<String, Message> messages = new HashMap<>();

    @Getter
    private final JDA jda;
    private final BinanceAPI binanceAPI;

    public PizzaBot(@Autowired @Value(PROP_TOKEN) String token,
                    @Autowired BinanceAPI binanceAPI) throws LoginException {
        this.binanceAPI = binanceAPI;
        Assert.notNull(token, "Token must not be null, did you forget to set ${%s}?".formatted(PROP_TOKEN));
        jda = JDABuilder.createDefault(token).build();
        jda.setAutoReconnect(true);
        jda.addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            var rawMsgContent = event.getMessage().getContentRaw();
            if (rawMsgContent.matches(CMD_PRINT_MESSAGES)) {
                log.info("Print messages command by {}", event.getAuthor());
                printMessages(event.getChannel());
            } else if (rawMsgContent.matches(CMD_AVERAGE_PRICE)) {
                var avgPrice = binanceAvgPrice(rawMsgContent);
                sendMessage(event.getChannel().getId(), avgPrice);
            } else if (rawMsgContent.matches(CMD_PRICE)) {
                var price = binancePriceOf(rawMsgContent);
                sendMessage(event.getChannel().getId(), price);
            } else if (rawMsgContent.matches(CMD_CANDLESTICKS)) {
                try {
                    var symbol = token(rawMsgContent)[1];
                    sendFile(event.getChannel(), binanceAPI.candleSticks(symbol), symbol + ".png");
                } catch (IOException e) {
                    log.error("Exception creating candle sticks graph", e);
                }
            }
        }
    }

    private void sendFile(MessageChannel channel, byte[] bytes, String filename) {
        channel.sendFile(bytes, filename).queue();

    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
    }

    private String[] token(String s) {
        return s.split(" ");
    }

    private String binancePriceOf(String rawMsgContent) {
        var t = token(rawMsgContent);
        if (t.length > 1) return binanceAPI.getPrice(t[1]).toString();
        else return "missing symbol parameter: $price <SYMBOL>";
    }

    private String binanceAvgPrice(String rawMsgContent) {
        var tokens = rawMsgContent.split(" ");
        if (tokens.length > 1) return binanceAPI.getAveragePrice(tokens[1]);
        else return "missing symbol parameter: $avgPrice <SYMBOL>";
    }

    public List<TextChannel> getTextChannels() {
        return jda.getTextChannels();
    }

    public void sendMessage(String channelId, String message) {
        var channel = jda.getTextChannelById(channelId);
        if (channel != null && channel.canTalk()) {
            queueMessage(Objects.requireNonNull(jda.getTextChannelById(channelId)), message);
        }
    }

    private void printMessages(MessageChannel channel) {
        var message = messages.entrySet().stream()
                .map(entry -> "Msg " + entry.getKey() +
                        " by " + entry.getValue().getAuthor() +
                        "\n" + entry.getValue().getContentRaw())
                .collect(Collectors.joining("\n"));
        channel.sendMessage((!message.isEmpty()) ? message : "no messages stored").queue();
    }

    private void queueMessage(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        var emoji = event.getReaction().getReactionEmote().getEmoji();
        if (EMOJI_PIZZA.equals(emoji)) {
            var channel = event.getChannel();
            var messageId = event.getMessageId();
            event.getChannel()
                    .retrieveMessageById(messageId)
                    .queue(message -> {
                        messages.put(message.getId(), message);
                        var msg = String.format("Added message: %s", message);
                        log.info(msg);
                        queueMessage(channel, msg);
                    });
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        var emoji = event.getReaction().getReactionEmote().getEmoji();
        if (EMOJI_PIZZA.equals(emoji)) {
            var messageId = event.getMessageId();
            var channel = event.getChannel();
            channel.retrieveMessageById(messageId)
                    .queue(message -> {
                        if (message.getReactions().stream()
                                .noneMatch(reaction -> reaction.getReactionEmote().getEmoji().equals(EMOJI_PIZZA))) {
                            messages.remove(message.getId());
                            var msg = String.format("Removed message due to no more pizza %s", message);
                            log.info(msg);
                            queueMessage(channel, msg);
                        }
                    });
        }
    }
}
