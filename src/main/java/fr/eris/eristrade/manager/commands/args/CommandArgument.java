package fr.eris.eristrade.manager.commands.args;

import fr.eris.eristrade.utils.GetValue;
import fr.eris.eristrade.utils.error.data.ErrorCode;
import fr.eris.eristrade.utils.error.data.ErrorType;
import fr.eris.eristrade.utils.error.exception.ErisPluginException;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CommandArgument<T> {

    @Getter private T value;
    @Setter @Getter private boolean canBeNull;
    @Getter private final boolean onlyCustomInput;
    private final List<GetValue<List<String>>> customInput;

    protected CommandArgument(boolean canBeNull) {
        this(null, false, canBeNull);
    }

    protected CommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput) {
        this(customInput, onlyCustomInput, false);
    }

    protected CommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput, boolean canBeNull) {
        this.customInput = new ArrayList<>();
        this.customInput.add(customInput);
        this.onlyCustomInput = onlyCustomInput;
        this.canBeNull = canBeNull;
    }

    public abstract boolean isValid(String input);

    /**
     * Call when is isValid return true value to convert the input in a usable value
     * @param input The player command input
     * @return The valid converted
     */
    public abstract T convertArgument(String input);

    public final boolean inputArgument(String input, CommandSender sender) {
        if(input == null || input.isEmpty()) {
            if(canBeNull) value = null;
            return canBeNull;
        }
        if(onlyCustomInput) {
            boolean flag = false;
            for(String currentCustomInput : convertCustomInput(sender)) {
                if(input.equalsIgnoreCase(currentCustomInput)) {
                    flag = true;
                    break;
                }
            }
            if(!flag) return false;
        }
        if(!isValid(input)) {
            value = null;
            return false;
        }
        value = convertArgument(input);
        return true;
    }

    public List<String> matchingArgs(String input, CommandSender sender) {
        List<String> matchingArgs = new ArrayList<>();
        if(customInput == null || customInput.isEmpty()) return matchingArgs;
        for(String currentArgs : convertCustomInput(sender)) {
            if(currentArgs.toLowerCase().startsWith(input.toLowerCase())) matchingArgs.add(currentArgs);
        }
        return matchingArgs;
    }

    public void validateCustomInput(CommandSender sender) throws ErisPluginException {
        List<String> flagList = new ArrayList<>();
        for(String currentCustomInput : convertCustomInput(sender)) {
            if(flagList.contains(currentCustomInput)) throw new ErisPluginException(ErrorType.DEVELOPERS, ErrorCode.COMMAND_MULTIPLE_SAME_CUSTOM_INPUT);
            flagList.add(currentCustomInput);
        }
    }

    public void addCustomInput(GetValue<List<String>> newCustomInput, CommandSender sender) {
        customInput.add(newCustomInput);
        try {
            validateCustomInput(sender);
        } catch (ErisPluginException erisPluginException) {
            erisPluginException.printStackTrace();
            customInput.clear();
            customInput.add((args) -> Collections.singletonList("error (check console)"));
        }
    }

    public List<String> convertCustomInput(CommandSender sender) {
        List<String> customInput = new ArrayList<>();
        for(GetValue<List<String>> currentCustomInputGetter : this.customInput) {
            if(currentCustomInputGetter == null) continue;
            customInput.addAll(currentCustomInputGetter.getValue(sender));
        }
        return customInput;
    }
    
    public <X> CommandArgument<X> convert(Class<X> to) {
        return (CommandArgument<X>) this;
    }
}
