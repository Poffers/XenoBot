package discord.command.perk;

import discord.util.BotUtils;
import discord.core.command.CommandHandler;
import discord.command.AbstractCommand;
import discord.command.CommandCategory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;

public class TagCommand extends AbstractCommand {
    
    private static final int LEVEL_REQUIRED = 20;
    
    private ArrayList<String> tags = new ArrayList<>();
    
    public TagCommand() {
        super(new String[] {"tag", "label"}, 1, LEVEL_REQUIRED, CommandCategory.PERK);
        try {
            Files.lines(Paths.get("tags.txt")).forEachOrdered(line -> {
                if (!line.trim().isEmpty()) {
                    tags.add(line);
                }
            });
            System.out.println(tags.size() + " tags loaded.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void execute(IMessage message, String[] args) {
        String operation = args[0].toLowerCase();
        String tag;
        
        if (operation.equals("list")) {
            BotUtils.sendMessage(message.getChannel(), "Available Tags", "`" + tags.toString() + "`");
            return;
        }

        if (operation.equals("create")) {
            if (!message.getAuthor().equals(message.getGuild().getOwner())) {
                BotUtils.sendErrorMessage(message.getChannel(), "You must be this guild's owner to create tags.");
                return;
            }
            if (args.length < 2) {
                BotUtils.sendErrorMessage(message.getChannel(), "Please provide a tag name to create.");
                return;
            }
            tag = CommandHandler.combineArgs(1, args);
            if (tagsContainsIgnoreCase(tag)) {
                BotUtils.sendErrorMessage(message.getChannel(), "Tag already exists.");
                return;
            }
            tags.add(tag);
            message.getGuild().createRole().changeName(tag);
            try {
                Files.write(Paths.get("tags.txt"), (tag + "\n").getBytes(), StandardOpenOption.APPEND);
                BotUtils.sendInfoMessage(message.getChannel(), "Tag created and saved.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        
        tag = CommandHandler.combineArgs(0, args);
        
        if (!tagsContainsIgnoreCase(tag)) {
            BotUtils.sendErrorMessage(message.getChannel(), "That tag doesn't exist. Use `!tag list` to view all tags.");
            return;
        }
        for (String currentTag : tags) { //get the proper case sensitive tag
            if (currentTag.equalsIgnoreCase(tag)) {
                tag = currentTag;
            }
        }
        IRole role = message.getGuild().getRolesByName(tag).get(0);
        if (role == null) {
            BotUtils.sendErrorMessage(message.getChannel(), 
                    "A role with that name doesn't exist on guild. Please create one.");
            return;
        }
        IUser dUser = message.getAuthor();
        if (!dUser.hasRole(role)) {
            dUser.addRole(role);
        } else {
            dUser.removeRole(role);
        }
        BotUtils.sendInfoMessage(message.getChannel(), "Tag toggled.");
    }
    
    @Override
    public String getUsage(String alias) {
        return BotUtils.buildUsage(alias, "(tag name)", "Toggle various tags for your profile." 
                + "Tags are essentially just no-permission Discord roles that act as labels."
                + "\n\n**Special Arguments**"
                + "\n`!tag list` - View all available tags.");
    }
    
    private boolean tagsContainsIgnoreCase(String tagToCheck) {
        return (tags.stream().anyMatch((tag) -> (tag.toLowerCase().equalsIgnoreCase(tagToCheck))));
    }
    
}
