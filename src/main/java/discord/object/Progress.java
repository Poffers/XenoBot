package discord.object;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import discord.BotUtils;
import discord.ColorManager;
import discord.RankManager;
import sx.blah.discord.handle.obj.IGuild;
import java.awt.Color;
import sx.blah.discord.handle.obj.IChannel;

public class Progress {
    
    @JsonIgnore
    private User user;
    
    private int level;
    private int xp;
    private int xpTotalForLevelUp;
    private Prestige prestige;
    private Rank rank;
    
    public final static int MAX_LEVEL = 80;
    
    @JsonCreator
    protected Progress(@JsonProperty("level") int level, 
            @JsonProperty("xp") int xp, 
            @JsonProperty("xpTotalForLevelUp") int xpTotalForLevelUp, 
            @JsonProperty("prestige") Prestige prestige,
            @JsonProperty("rank") Rank rank) {
        this.level = level;
        this.xp = xp;
        this.xpTotalForLevelUp = xpTotalForLevelUp;
        this.prestige = prestige;
        this.rank = rank;
    }
    
    protected Progress() {
        level = 1;
        xp = 0;
        genXPTotalForLevelUp();
        prestige = new Prestige(0);
        rank = RankManager.getRankForLevel(level);
    }
    
    //getters
    
    public int getLevel() {
        return level;
    }
    
    public int getXP() {
        return xp;
    }
    
    public int getXpTotalForLevelUp() {
        return xpTotalForLevelUp;
    }
    
    public Prestige getPrestige() {
        return prestige;
    }
    
    public Rank getRank() {
        return rank;
    }
    
    //TEMPORARY?
    
    @JsonIgnore
    public void setUser(User user) {
        this.user = user;
    }   
    
    public void setRank(Rank rank) {
        this.rank = rank;
    }
    
    @JsonIgnore
    public int getTotalLevels() {
        return prestige.getNumber() * MAX_LEVEL + level;
    }
    
    @JsonIgnore
    public boolean isMaxLevel() {
        return level == MAX_LEVEL;
    }
    
    public void addXP(int xp, IGuild guild) {
        if (level < MAX_LEVEL) {
            this.xp += xp;
            checkXP(guild);
        }
    }
    
    private void checkXP(IGuild guild) {
        if (xp >= xpTotalForLevelUp || xp < 0) {
            if (xp >= xpTotalForLevelUp) {
                levelUp(guild);
                checkUnlocksForUser(guild);              
            } else if (xp < 0) {
                if (level > 1) { //prevent negative levels
                    levelDown(guild);
                } else {
                    xp = 0;
                }
            }
            if (prestige.getNumber() < Prestige.MAX_PRESTIGE) {
                RankManager.setRankOfUser(guild, user);
                if (level < MAX_LEVEL) {
                    checkXP(guild);
                }
            }
        }
    }
    
    //Only handles leveling up, logic and xp handling handled elsewhere
    private void levelUp(IGuild guild) {
        xp -= xpTotalForLevelUp; //carry over xp to next level by subtracting from level xp
        level++;
        genXPTotalForLevelUp();
        BotUtils.sendMessage(guild.getChannelsByName("securitycam").get(0), "Level up!",
                String.format("%s\n**%d → %d**", BotUtils.getMention(user), 
                        level - 1, level), guild.getUserByID(user.getID()).getColorForGuild(guild));
    }
    
    //same as leveling up method
    private void levelDown(IGuild guild) {
        level--;
        genXPTotalForLevelUp();
        xp += xpTotalForLevelUp; //add negative xp to new level xp
        BotUtils.sendMessage(guild.getChannelsByName("securitycam").get(0), "Level down!",
                String.format("%s\n**%d → %d**", BotUtils.getMention(user), 
                        level, level - 1), guild.getUserByID(user.getID()).getColorForGuild(guild));
    }
    
    private void checkUnlocksForUser(IGuild guild) {
        IChannel pmChannel = guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getID()));      
        if (level == MAX_LEVEL && prestige.getNumber() < Prestige.MAX_PRESTIGE)
            maxOutUser(pmChannel);
        if (level % 20 == 0) 
            notifyUnlocksForUser(pmChannel, guild);
    }
    
    private void maxOutUser(IChannel channel) {
        xp = 0;
        xpTotalForLevelUp = 0;
        BotUtils.sendMessage(channel, "Congratulations!", "You have reached the max level. "
                + "\n\nYou can now prestige and carry over back to level one with `!prestige`"
                + "\n\nYou will keep all perks, and gain additional unlocks as you level again."
                + "\nPrestiging is **permanent.** Only do so if you are ready.", Color.CYAN);
    }
    
    //BAD
    public void prestige(IGuild guild) {
        prestige = prestige.prestige();
        level = 1;
        xp = 0;
        genXPTotalForLevelUp();
        if (prestige.getNumber() < Prestige.MAX_PRESTIGE) RankManager.setRankOfUser(guild, user); //keep top rank when max prestige
        user.getName().verify(guild);
        BotUtils.sendMessage(guild.getChannelsByName("securitycam").get(0), BotUtils.getMention(user), "PRESTIGE UP!", 
                String.format("**%d → %d**", prestige.getNumber() - 1, prestige.getNumber()), Color.BLACK);
        
        if (prestige.getNumber() == 1) { //messy to put these here?
            BotUtils.sendMessage(guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getID())), 
                    "Congratulations!", "You have unlocked the ability to **change your name color** on " + guild.getName() + "!"
                    + "\n\n*You can type* `!color` *on the server get started.*", Color.PINK);
        } else if (prestige.isMax()) {
            BotUtils.sendMessage(guild.getClient().getOrCreatePMChannel(guild.getUserByID(user.getID())), 
                    "Well done.", "**You have reached the maximum prestige.**" 
                    + "\n\nYour everlasting hard work has earned you the final badge, the trident."
                    + "\nThis is the end of the road. "
                    + "However, you can now level **infinitely** for fun, but won't earn any new unlocks or ranks."
                    + "\n\nIt's an incredibly long journey to have gotten here, and I "
                    + "thank you for your dedication to The Realm.", Color.BLACK);
        }
    }
    
    //Moved here until theres a solution/ unlock manager?
    //All of this is hardcoded, clean it up eventually
    private void notifyUnlocksForUser(IChannel pmChannel, IGuild guild) {
        String message = "";
        int totalLevels = getTotalLevels();
        Color colorToUse = Color.ORANGE;
        if (totalLevels == 40) {
            message = "You have unlocked the ability to **set an emoji** in your name on " + guild.getName() + "!"
                    + "\n\n*You can type* `!emoji` *on the server to get started.*";
        } else if (totalLevels == 60) {
            message = "You have unlocked the ability to **change your nickname** on " + guild.getName() + "!"
                    + "\n\n*You can type* `!name` *on the server to get started.*";
        } else if (totalLevels > 80 && totalLevels % 20 == 0) { //already prestiged, unlock color every 20 levels
            Unlockable color = ColorManager.getUnlockedColor(totalLevels);
            System.out.println(color);
            if (color != null) {
                message = "You have unlocked the name color **" + color.toString() + "** on " + guild.getName() + "!"
                        + "\n\n*You can type* `!color list` *on the server to view your unlocked colors.*";
                colorToUse = guild.getRolesByName(color.toString()).get(0).getColor();
            }
        }
        BotUtils.sendMessage(pmChannel, "Congratulations!", message, colorToUse);
    }
    
    private void genXPTotalForLevelUp() {
        xpTotalForLevelUp = level * 10 + 50;
    } 
    
}
