package fr.eris.eristrade.manager.commands;

import fr.eris.eristrade.manager.commands.args.CommandArgument;
import fr.eris.eristrade.manager.commands.args.StringCommandArgument;
import fr.eris.eristrade.utils.GetValue;
import fr.eris.eristrade.utils.error.data.ErrorCode;
import fr.eris.eristrade.utils.error.data.ErrorType;
import fr.eris.eristrade.utils.error.exception.ErisPluginException;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.*;

public abstract class ErisSubCommandExecutor extends ErisCommand {

    @Getter private final boolean noArgsExecution;
    @Getter private final List<ErisSubCommand> subCommands;

    public ErisSubCommandExecutor(String name, List<String> aliases, String permission, boolean forPlayerOnly, boolean forConsoleOnly, boolean noArgsExecution) {
        super(name, aliases, permission, forPlayerOnly, forConsoleOnly);
        this.noArgsExecution = noArgsExecution;
        this.subCommands = new ArrayList<>();
        try {
            validateCommandConfig();
            this.subCommands.addAll(registerSubCommands());
            registerSubCommandAsArgument();
        } catch (ErisPluginException exception) {
            exception.printStackTrace();
            isCommandRegister = false;
            this.subCommands.clear();
        }
    }

    private void registerSubCommandAsArgument() throws ErisPluginException {
        StringCommandArgument subCommandAsArgument =
                new StringCommandArgument(subCommandToStringGetter(), false, false);
        if(getCommandArguments().isEmpty())
            getCommandArguments().add(subCommandAsArgument);
        else {
            CommandArgument<?> firstCommandArgument = getCommandArguments().get(0);
            if(firstCommandArgument instanceof StringCommandArgument) {
                firstCommandArgument.addCustomInput(subCommandToStringGetter(), Bukkit.getConsoleSender());
            } else {
                throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.SUBCOMMAND_INVALID_ARGUMENT_WITH_SUBCOMMAND);
            }
        }
    }

    private GetValue<List<String>> subCommandToStringGetter() {
        return (args) -> {
            try {
                return subCommandAsCommandListString(true);
            } catch (ErisPluginException exception) {
                exception.printStackTrace();
                isCommandRegister = false;
                this.subCommands.clear();
            }
            return Collections.emptyList();
        };
    }

    private List<String> subCommandAsCommandListString(boolean withAliases) throws ErisPluginException {
        List<String> total = new ArrayList<>();
        for(ErisSubCommand subCommand : subCommands) {
            if(withAliases) {
                for(String commandAliases : subCommand.getAliases()) {
                    if(total.contains(commandAliases))
                        throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.COMMAND_MULTIPLE_SAME_ALIASES_OR_COMMAND);
                    total.add(commandAliases);
                }
            }
            if(total.contains(subCommand.getName()))
                throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.COMMAND_MULTIPLE_SAME_ALIASES_OR_COMMAND);
            total.add(subCommand.getName());
        }
        return total;
    }

    public void validateArguments() throws ErisPluginException {
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

    public void validateCommandConfig() throws ErisPluginException {
        validateArguments();
    }

    public abstract List<ErisSubCommand> registerSubCommands();

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] args) {
        if(noArgsExecution && (args == null || args.length == 0)) {
            defaultExecute(commandSender, new ArrayList<>());
            return true;
        }
        ErisSubCommand subCommand = getSubCommand(args[0]);
        if(subCommand == null) {
            if(validateExecutionWithArgs(commandSender, args, null) && validateSenderType(commandSender, args) && validateSenderPermission(commandSender, args)) {
                defaultExecute(commandSender, commandArguments);
                return true;
            }
            return true;
        }

        if(!validateExecutionWithArgs(commandSender, args, subCommand) || !validateSenderType(commandSender, args) || !validateSenderPermission(commandSender, args)) {
            return true;
        }
        execute(commandSender, commandArguments);
        return true;
    }

    @Override
    public void execute(CommandSender commandSender, List<CommandArgument<?>> args) {
        ErisSubCommand subCommand = getSubCommand(args.get(0).convert(String.class).getValue());
        if(subCommand == null) {
            error(commandSender, CommandExecutionError.NOT_FIND_SUBCOMMAND, null, args.get(0).convert(String.class).getValue(), args.get(0));
            return;
        }
        List<CommandArgument<?>> argsCopy = new ArrayList<>(args);
        argsCopy.remove(0);
        subCommand.execute(commandSender, argsCopy);
    }

    protected ErisSubCommand getSubCommand(String name) {
        for(ErisSubCommand subcommand : this.subCommands) {
            for(String aliases : subcommand.getAliases()) {
                if(aliases.equalsIgnoreCase(name))
                    return subcommand;
            }
            if(subcommand.getName().equalsIgnoreCase(name)) {
                return subcommand;
            }
        }
        return null;
    }

    // execute when no valid args are specified if allowed
    public abstract void defaultExecute(CommandSender sender, List<CommandArgument<?>> args);

    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if(args == null) return Collections.emptyList();
        if(args.length == 1) super.tabComplete(sender, alias, args);
        System.out.println(Arrays.toString(args));
        ErisSubCommand subCommand = getSubCommand(args[0]);
        int argsAsIndex = args.length - 1;
        if(subCommand == null)
            if(commandArguments.size() <= argsAsIndex || commandArguments.get(argsAsIndex) == null) return Collections.emptyList();
            else return commandArguments.get(argsAsIndex).matchingArgs(args[argsAsIndex], sender);
        if(subCommand.getCommandArguments().size() + 1 <= argsAsIndex) {
            return Collections.emptyList();
        }
        else {
            return subCommand.getCommandArguments().get(argsAsIndex - 1).matchingArgs(args[argsAsIndex], sender);
        }
    }
}
