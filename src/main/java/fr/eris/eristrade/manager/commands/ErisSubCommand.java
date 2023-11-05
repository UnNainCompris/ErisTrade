package fr.eris.eristrade.manager.commands;

import fr.eris.eristrade.manager.commands.args.CommandArgument;
import fr.eris.eristrade.utils.error.data.ErrorCode;
import fr.eris.eristrade.utils.error.data.ErrorType;
import fr.eris.eristrade.utils.error.exception.ErisPluginException;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ErisSubCommand {

    @Getter private final boolean forPlayerOnly;
    @Getter private final boolean forConsoleOnly;
    @Getter private final List<CommandArgument<?>> commandArguments;

    @Getter private final List<String> aliases;
    private final String permission;
    @Getter private final String name;

    @Getter protected boolean isCommandRegister = false;

    public ErisSubCommand(String name, List<String> aliases, String permission, boolean forPlayerOnly, boolean forConsoleOnly) {
        this.name = name;
        if(aliases == null) aliases = new ArrayList<>();
        this.aliases = aliases;
        this.permission = permission;
        this.forPlayerOnly = forPlayerOnly;
        this.forConsoleOnly = forConsoleOnly;
        this.commandArguments = registerCommandArgument();
        try {
            validateCommandConfig();
        } catch (ErisPluginException exception) {
            exception.printStackTrace();
            isCommandRegister = false;
        }
    }

    public void validateCommandConfig() throws ErisPluginException {
        validateArguments();
        validateExecutorType();
    }

    public void validateExecutorType() throws ErisPluginException {
        if(forPlayerOnly && forConsoleOnly)
            throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.COMMAND_FOR_CONSOLE_AND_PLAYER);
    }

    private void validateArguments() throws ErisPluginException {
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

    public abstract void error(CommandSender sender, ErisCommand.CommandExecutionError errorCode, String[] argsValue, String targetedArgs,
                      CommandArgument<?> targetedCommandArguments);

    public abstract @NonNull List<CommandArgument<?>> registerCommandArgument();

    public abstract void execute(CommandSender commandSender, List<CommandArgument<?>> args);
}
