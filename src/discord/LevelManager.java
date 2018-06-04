package discord;

import discord.objects.User;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;

public class LevelManager {
    
    public static void addUserXPFromID(IGuild guild, long id, int amount) {
        User user = UserManager.getUserFromID(id);
        user.addXP(amount); 
        checkXPUser(guild, user);
    }   
    
    //this needs to be a lot cleaner, fix it sooner
    private static void changeLevelUser(IGuild guild, User user, boolean levelUp) {
        int amount = (levelUp) ? 1 : -1;
        
        if (levelUp) {          
            user.addXP(-user.getXPForLevel()); //carry over xp to next level
        }  
        
        user.addLevels(amount);
        setUserXPForLevel(user);     
        
        if (!levelUp) {           
            user.addXP(user.getXPForLevel() + user.getXP()); //subtract from the max
        }
        
        NameManager.formatNameOfUser(guild, user);         
        int level = user.getLevel();
        BotUtils.sendMessage(guild.getChannelsByName("log").get(0), String.format("%s```http\nLevel up! %d → %d```", 
                guild.getUserByID(user.getID()).mention(), level - 1, level)); 
        
        RankManager.setRankOfUser(guild, user);
        
        if (user.getLevel() == BotUtils.MAX_LEVEL) 
            maxOutUser(guild.getChannelsByName("commmands").get(0), user);
        else 
            checkXPUser(guild, user);
    }
    
    private static void maxOutUser(IChannel channel, User user) {
        user.setXPForLevel(0);
        user.setXP(0);
        BotUtils.sendMessage(channel, "%s```http\nCongratulations! You have reached the max level. "
                + "You will no longer earn any xp until prestiged."
                + "\n\nYou can now prestige and carry over back to level one with \"!prestige\"!"
                + "\n\nAll level perks will be lost, but you will gain the ability to change your name color."
                + "\n\nPrestiging is PERMANENT. Only do so if you are ready.```");
    }
    
    public static void prestigeUser(IChannel channel, User user) {
        user.addPrestige();
        user.setLevel(1);
        user.setXP(0);
        user.setEmoji(0);
        setUserXPForLevel(user);
        IGuild guild = channel.getGuild();
        NameManager.formatNameOfUser(guild, user);
        RankManager.setRankOfUser(guild, user);
        BotUtils.sendMessage(channel, "@here```" + user.getName() + " has prestiged! Praise unto thee.```");
    }
    
    private static void checkXPUser(IGuild guild, User user) {
        int xp = user.getXP();
        if (xp >= user.getXPForLevel())
            changeLevelUser(guild, user, true);
        else if (xp < 0)
            changeLevelUser(guild, user, false);
    }
    
    public static void setUserXPForLevel(User user) {
        user.setXPForLevel(user.getLevel() * 24 + 56);
    }
    
    public static EmbedObject buildInfo(User user) {
        EmbedBuilder builder = new EmbedBuilder();       
        builder.withColor(0, 255, 127);
        builder.withTitle("__" + user.getName() + "__");
        builder.withDesc(user.getRank().getName());
        builder.appendField("Level", "`" + user.getLevel() + "`", true);
        int prestige = user.getPrestige();
        if (prestige > 0) 
            builder.appendField("Prestige", String.format("`%d%c`", 
                    prestige, BotUtils.PRESTIGE_SYMBOLS[prestige - 1]), true);
        builder.appendField("XP", "`" + user.getXP() + "/" + user.getXPForLevel() + "`", true);
        builder.appendField("Progress to Next Level", getProgress(user), false);
        
        return builder.build();
    }
    
    private static String getProgress(User user) {
        StringBuilder builder = new StringBuilder();
        //generates an int 1-10 depicting progress based on xp
        int prog = (int) Math.floor(((double) user.getXP() / user.getXPForLevel()) * 10);
        
        for (int i = 1; i <= 10; i++) {
            if (i <= prog)
                builder.append(":white_large_square: ");
            else
                builder.append(":white_square_button: ");
        }
        return builder.toString();
    }
      
}