package fr.eris.eristrade.manager.commands;

import fr.eris.eristrade.ErisTrade;
import fr.eris.eristrade.manager.commands.args.CommandArgument;
import fr.eris.eristrade.utils.error.data.ErrorCode;
import fr.eris.eristrade.utils.error.data.ErrorType;
import fr.eris.eristrade.utils.error.exception.ErisPluginException;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.HumanEntity;

import java.util.*;

public abstract class ErisCommand extends BukkitCommand {

    @Getter private final boolean forPlayerOnly;
    @Getter private final boolean forConsoleOnly;
    @Getter protected final List<CommandArgument<?>> commandArguments;
    @Getter protected boolean isCommandRegister = false;

    protected ErisCommand(String name, List<String> aliases, String permission, boolean forPlayerOnly, boolean forConsoleOnly) {
        super(name);
        if(aliases == null) aliases = new ArrayList<>();
        this.setAliases(aliases);
        this.setPermission(permission);
        this.forPlayerOnly = forPlayerOnly;
        this.forConsoleOnly = forConsoleOnly;
        this.commandArguments = new ArrayList<>(registerCommandArgument());
        try {
            validateCommandConfig();
        } catch (ErisPluginException exception) {
            exception.printStackTrace();
            isCommandRegister = false;
        }
        ErisTrade.getCommandManager().registerCommand(this);
    }

    public abstract @NonNull List<CommandArgument<?>> registerCommandArgument();

    public void validateCommandConfig() throws ErisPluginException {
        validateArguments();
        validateExecutorType();
    }

    public void validateExecutorType() throws ErisPluginException {
        if(forPlayerOnly && forConsoleOnly)
            throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.COMMAND_FOR_CONSOLE_AND_PLAYER);
    }

    protected void validateArguments() throws ErisPluginException {
        Iterator<CommandArgument<?>> commandArgumentIterator = commandArguments.iterator();
        boolean isPreviousArgumentCanBeNull = false;
        while(commandArgumentIterator.hasNext()) {
            if(commandArgumentIterator.next().isCanBeNull()) {
                if(isPreviousArgumentCanBeNull) {
                    throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.COMMAND_TWO_NULLABLE_ARGUMENT_IN_ROW);
                }
                isPreviousArgumentCanBeNull = true;
            }
        }
        // check if any argument are nullable and if one was found before and the last one is not nullable throw an error
        if(isPreviousArgumentCanBeNull && !commandArguments.get(commandArguments.size() - 1).isCanBeNull())
            throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.COMMAND_NULLABLE_ARGUMENT_NOT_AT_END);
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if(!validateExecutionWithArgs(commandSender, args, null) || !validateSenderType(commandSender, args) || !validateSenderPermission(commandSender, args)) {
            return true;
        }
        execute(commandSender, commandArguments);
        return true;
    }

    public abstract void error(CommandSender commandSender, CommandExecutionError errorCode, String[] argsValue, String targetedArgs, CommandArgument<?> targetedCommandArguments);

    protected boolean validateSenderType(CommandSender commandSender, String[] args) {
        if(!(commandSender instanceof HumanEntity) && forPlayerOnly) {
            error(commandSender, CommandExecutionError.NEEDED_PLAYER_EXECUTOR, args, null, null);
            return false;
        } else if(!(commandSender instanceof ConsoleCommandSender) && forConsoleOnly) {
            error(commandSender, CommandExecutionError.NEEDED_CONSOLE_EXECUTOR, args, null, null);
            return false;
        }
        return true;
    }

    protected boolean validateSenderPermission(CommandSender commandSender, String[] args) {
        if(commandSender instanceof ConsoleCommandSender || commandSender.hasPermission(getPermission())) {
            error(commandSender, CommandExecutionError.INSUFFICIENT_PERMISSION, args, null, null);
            return true;
        }
        return false;
    }

    protected boolean validateExecutionWithArgs(CommandSender commandSender, String[] args, ErisSubCommand targetSubCommand) {
        if(args == null || args.length == 0) {
            if(targetSubCommand != null)
                targetSubCommand.error(commandSender, CommandExecutionError.NOT_ENOUGH_ARGS, null, null, null);
            else error(commandSender, CommandExecutionError.NOT_ENOUGH_ARGS, null, null, null);
            return false;
        } if(args.length < commandArguments.size() ||  (args.length - 1 == commandArguments.size() && commandArguments.get(commandArguments.size() - 1).isCanBeNull())) {
            if(targetSubCommand != null)
                targetSubCommand.error(commandSender, CommandExecutionError.NOT_ENOUGH_ARGS, args, null, null);
            else error(commandSender, CommandExecutionError.NOT_ENOUGH_ARGS, args, null, null);
            return false;
        }
        List<CommandArgument<?>> allArgument = new ArrayList<>(commandArguments);
        if(targetSubCommand != null)
            allArgument.addAll(targetSubCommand.getCommandArguments());
        for(CommandArgument<?> currentCommandArgument : allArgument) {
            if(!currentCommandArgument.inputArgument(args[allArgument.indexOf(currentCommandArgument)], commandSender)) {
                if(targetSubCommand != null)
                    targetSubCommand.error(commandSender, CommandExecutionError.INVALID_ARGS, args, args[allArgument.indexOf(currentCommandArgument)], currentCommandArgument);
                else error(commandSender, CommandExecutionError.INVALID_ARGS, args, args[allArgument.indexOf(currentCommandArgument)], currentCommandArgument);
                return false;
            }
        }
        return true;
    }

    public enum CommandExecutionError {
        NOT_ENOUGH_ARGS,
        INVALID_ARGS,
        INSUFFICIENT_PERMISSION,
        NEEDED_PLAYER_EXECUTOR,
        NOT_FIND_SUBCOMMAND,
        NEEDED_CONSOLE_EXECUTOR; // use when the excepted executor is a player
    }

    public abstract void execute(CommandSender sender, List<CommandArgument<?>> args);

    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        super.tabComplete(sender, alias, args);
        if(args == null) return Collections.emptyList();
        int argsAsIndex = args.length - 1;
        if(argsAsIndex == -1) return Collections.emptyList();
        return commandArguments.get(argsAsIndex).matchingArgs(args[argsAsIndex], sender);
    }
}
