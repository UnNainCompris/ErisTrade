package fr.eris.eristrade.manager.commands;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.utils.BukkitTasks;
import lombok.Setter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubCommandExecutor extends BukkitCommand {
    private final String commandName;
    @Setter private String prefix;

    private final List<String> usageMessage;
    private final List<SubCommand> subCommands;

    private SubCommand defaultSubcommand;

    public SubCommandExecutor(String commandName, List<String> usageMessage) {
        this(commandName, new ArrayList<>(), usageMessage);
    }

    public SubCommandExecutor(String commandName, List<String> aliases, List<String> usageMessage) {
        super(commandName);

        this.commandName = commandName;
        this.setAliases(aliases);

        this.subCommands = new ArrayList<>();
        this.usageMessage = usageMessage;

        ErisTrade.getCommandManager().registerCommand(this);
    }

    protected void addSubCommand(SubCommand command) {
        command.setPrefix(this.prefix);
        this.subCommands.add(command);
    }

    protected SubCommand getSubCommand(String name) {
        for(SubCommand sub : this.subCommands) {
            if(sub.getName().equalsIgnoreCase(name) || sub.getAliases().contains(name.toLowerCase())) {
                return sub;
            }
        }

        return null;
    }

    public void setDefaultSubCommand(SubCommand subCommand, boolean addToSubCommand) {
        this.defaultSubcommand = subCommand;
        if(addToSubCommand) {
            addSubCommand(subCommand);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(args == null || args.length == 0) {
            if(defaultSubcommand != null) {

                if(this.defaultSubcommand.forPlayersOnly && sender instanceof ConsoleCommandSender) {
                    return true;
                }

                if(this.defaultSubcommand.getPermission() != null && !sender.hasPermission(this.defaultSubcommand.getPermission())) {
                    return true;
                }

                if(this.defaultSubcommand.isExecuteAsync()) {
                    BukkitTasks.async(() -> this.defaultSubcommand.preExecute(sender, new String[]{}));
                } else {
                    this.defaultSubcommand.preExecute(sender, new String[]{});
                }
            }
            return true;
        }
        SubCommand sub = this.getSubCommand(args[0]);
        if(sub == null) {
            return true;
        }

        if(sub.forPlayersOnly && sender instanceof ConsoleCommandSender) {
            return true;
        }

        if(sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
            return true;
        }

        if(sub.isExecuteAsync()) {
            BukkitTasks.async(() -> sub.preExecute(sender, Arrays.copyOfRange(args, 1, args.length)));
        } else {
            sub.preExecute(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if(args.length != 1) {
            return super.tabComplete(sender, alias, args);
        }

        List<String> completions = new ArrayList<>();

        for(SubCommand subCommand : this.subCommands) {
            if(!subCommand.getName().startsWith(args[0].toLowerCase())) continue;
            if(subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) continue;

            completions.add(subCommand.getName());
        }

        return completions;
    }
}
