package discord;

import discord.object.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

public class XPChecker implements Runnable {

    private final IDiscordClient client;
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE hh:mma");

    public XPChecker(IDiscordClient client) {
         this.client = client;
    }
    
    public void run() {
        System.out.println(String.format("[%s] Checking all guild users to add xp",
                LocalDateTime.now().format(formatter)));
        checkGuilds(client.getGuilds());
    }

    private void checkGuilds(List<IGuild> guilds) {
        for (IGuild guild : guilds) {
            checkVoiceChannels(guild);
        }
    }
    
    private void checkVoiceChannels(IGuild guild) {
        List<IVoiceChannel> channels = guild.getVoiceChannels();
        channels.removeIf(channel -> channel.equals(guild.getAFKChannel()));
        for (IVoiceChannel channel : channels) {
            checkUsers(channel.getConnectedUsers(), channel, guild);
        }
    }
    
    private void checkUsers(List<IUser> dUsers, IChannel channel, IGuild guild) {
        dUsers.removeIf(user -> user.isBot() //only count people that are "talking"
                || user.getVoiceStateForGuild(guild).isSelfDeafened()
                || user.getVoiceStateForGuild(guild).isSelfMuted()
                || user.getVoiceStateForGuild(guild).isMuted());
        if (dUsers.size() >= 2) {
            List<String> names = new ArrayList<>();
            int xp = 1 * dUsers.size() + 13; // min 450/hr
            dUsers.removeIf(user -> UserManager.getUserLevel(
                    user.getLongID()) == LevelManager.MAX_LEVEL);
            if (dUsers.size() == 0) return; //if all are max level
            dUsers.forEach(dUser -> names.add(UserManager.getUserName(dUser.getLongID())));
            BotUtils.sendMessage(guild.getChannelsByName("log").get(0),
                    String.format("+%dXP in %s (%s)", xp, 
                            channel.getName(), LocalDateTime.now().format(formatter)),
                            names.toString());
            for (IUser dUser : dUsers) {
                User user = UserManager.getUserFromID(dUser.getLongID());
                user.addXP(xp);
                System.out.println("Gave " + xp + "xp to " + user.getName());
            }                     
            UserManager.saveDatabase();
        }
    }
    
}