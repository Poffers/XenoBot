package discord.commands.hidden;

import discord.BotUtils;
import discord.LevelManager;
import discord.UserManager;
import discord.commands.AbstractCommand;
import discord.commands.CommandCategory;
import discord.objects.User;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

public class PrestigeCommand extends AbstractCommand {
    
    public PrestigeCommand() {
        super(new String[] {"prestige"}, 0, CommandCategory.HIDDEN);
    }
    
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getUserFromID(message.getAuthor().getLongID());
        IChannel channel = message.getChannel();
        if (!(user.getLevel() == LevelManager.MAX_LEVEL)) {
                    BotUtils.sendErrorMessage(channel, "You must be level **80** to prestige."
                            + " You can view your progress with `!lvl`.");
        } else {
            LevelManager.prestigeUser(channel, user);
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Prestige and carry over back to level one."
                + "\n*(Level 80)*");
    }
}