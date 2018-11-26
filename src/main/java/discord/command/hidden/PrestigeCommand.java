package discord.command.hidden;

import discord.BotUtils;
import discord.UserManager;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import discord.object.Progress;
import discord.object.User;
import sx.blah.discord.handle.obj.IMessage;

public class PrestigeCommand extends AbstractCommand {
    
    public PrestigeCommand() {
        super(new String[] {"prestige"}, 0, CommandCategory.HIDDEN);
    }
    
    public void execute(IMessage message, String[] args) {
        User user = UserManager.getDBUserFromMessage(message);
        if (user.getProgress().getPrestige().isMax()) {
            BotUtils.sendInfoMessage(message.getChannel(), "You have already reached the maximum prestige.");
        } else if (!(user.getProgress().isMaxLevel())) {
            BotUtils.sendErrorMessage(message.getChannel(), "You must be level **"
                    + Progress.MAX_LEVEL + "** to prestige."
                    + " You can view your progress with `!prog`.");
        } else {
            user.getProgress().prestige(message.getGuild());
            BotUtils.sendMessage(message.getChannel(), "Movin' on up", "Welcome to Prestige " 
                    + user.getProgress().getPrestige().getNumber() + ".");
        }
    }
    
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "", "Prestige and carry over back to level one."
                + "\n*(Level " + Progress.MAX_LEVEL + ")*");
    }
}
