package fr.eris.eristrade.manager.commands.args.number;

import fr.eris.eristrade.manager.commands.args.CommandArgument;
import fr.eris.eristrade.utils.GetValue;
import fr.eris.eristrade.utils.StringUtils;

import java.util.List;

public class IntegerCommandArgument extends CommandArgument<Integer> {
    protected IntegerCommandArgument(boolean canBeNull) {
        super(canBeNull);
    }

    protected IntegerCommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput) {
        super(customInput, onlyCustomInput);
    }

    protected IntegerCommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput, boolean canBeNull) {
        super(customInput, onlyCustomInput, canBeNull);
    }

    @Override
    public boolean isValid(String input) {
        return StringUtils.isInteger(input);
    }

    @Override
    public Integer convertArgument(String input) {
        return Integer.parseInt(input);
    }
}
