package discord.command.info;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.data.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.data.object.Prestige;
import discord.util.ProfileBuilder;
import discord.data.object.Reincarnation;
import discord.data.object.User;
import java.util.List;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class ProfileCommand extends AbstractCommand {
    
    public ProfileCommand() {
        super(new String[] {"profile", "prof"}, 0, CommandCategory.INFO);
    }
    
    //code copy and paste
    @Override
    public void execute(IMessage message, String[] args) {
        User user;
        if (args.length > 0) {
            List<IUser> mentions = message.getMentions();
            if (!mentions.isEmpty()) {
                user = UserManager.getDBUserFromDUser(mentions.get(0));
            } else {
                String name = CommandHandler.combineArgs(0, args);
                user = UserManager.getDBUserFromName(name);
            }
        } else {
            user = UserManager.getDBUserFromMessage(message);
        }
        if (user == null) {
            BotUtils.sendErrorMessage(message.getChannel(),
                    "Specified user was not found in the database.");
            return;
        }
        BotUtils.sendEmbedMessage(message.getChannel(), buildProfileInfo(message.getGuild(), user));
    }
    
    public EmbedObject buildProfileInfo(IGuild guild, User user) {
        ProfileBuilder builder = new ProfileBuilder(guild, user);
        Prestige prestige = user.getProgress().getPrestige();
        Reincarnation reincarnation = user.getProgress().getReincarnation();
        if (!user.getDesc().isEmpty()) {
            builder.addDesc();
        }
      
        builder.addRank();
        builder.addLevel();
        if (prestige.isPrestiged()) {
            builder.addPrestige();
        }
        if (reincarnation.isReincarnated()) {
            builder.addReincarnation();
        }
        if (user.getProgress().isNotMaxLevel()) {
            builder.addXPProgress();
        }
        if (reincarnation.isReincarnated()) {
            builder.addXPBoost();
        }
        builder.addTotalXP();
        if (prestige.isPrestiged() || reincarnation.isReincarnated()) {
            builder.addTotalLevel();
            if (prestige.isPrestiged()) {
                builder.addBadgeCase();
            }
        }
        if (user.getProgress().isNotMaxLevel() && !user.getProgress().getPrestige().isMax()) { 
            builder.addBarProgressToMaxLevel();
        }
        return builder.build();
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "[nickname/@mention]", 
                "View you or another user's detailed profile.");
    }
    
}
