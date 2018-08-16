package discord.command.info;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord.BotUtils;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

public class CryptoCommand extends AbstractCommand {
    
    public CryptoCommand() {
        super(new String[] {"crypto", "cryptocurrency", "cc", "price"}, 1, CommandCategory.INFO);
    }
    
    public void execute(IMessage message, String[] args) {
        IChannel channel = message.getChannel();
        StringBuilder sb = new StringBuilder();
        EmbedBuilder builder = new EmbedBuilder();
        try {
            URL url = new URL("https://api.coinmarketcap.com/v1/ticker/" + args[0]);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            sb.deleteCharAt(0).deleteCharAt(sb.length() - 1); //terrible way to do this id imagine
            String json = sb.toString().replace("null", "\"Unknown\""); //also bad but hey            
            JsonNode node = new ObjectMapper().readTree(json);
            builder.withColor(0, 255, 127);
            builder.withThumbnail(String.format(
                    "https://files.coinmarketcap.com/static/img/coins/32x32/%s.png", node.path("id").asText()));
            builder.withTimestamp(node.path("last_updated").asLong() * 1000);
            builder.withTitle(String.format("__%s (%s)__",
                    node.path("name").asText(), node.path("symbol").asText()));
            builder.withDesc("Rank " + node.path("rank").asText());
            builder.appendField("Price (USD)", "$" + node.path("price_usd").asText(), true);
            builder.appendField("Price (BTC)", node.path("price_btc").asText(), true);
            builder.appendField("Market Cap", "$" + node.path("market_cap_usd").asText(), true);
            builder.appendField("Volume (Past Day)", "$" + node.path("24h_volume_usd").asText(), true);
            builder.appendField("Change (Past Hour)", node.path("percent_change_1h").asText() + "%", true);
            builder.appendField("Change (Past Day)", node.path("percent_change_24h").asText() + "%", true);
            builder.appendField("Change (Past Week)", node.path("percent_change_7d").asText() + "%", true);
            builder.appendField("Max Supply", node.path("max_supply").asText(), true);
            builder.appendField("Total Supply", node.path("total_supply").asText(), true);
            builder.appendField("Circulating Supply", node.path("available_supply").asText(), true);
            builder.withFooterText("coinmarketcap.com");
            BotUtils.sendEmbedMessage(channel, builder.build());
        } catch (IOException e) {
            e.printStackTrace();
            BotUtils.sendErrorMessage(channel, "Specified coin was not found in the database.");
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[full name]", "View price and other info on a crpytocurrency.");
    }
    
}