package fr.eris.eristrade.manager.commands.args.number;

import fr.eris.eristrade.manager.commands.args.CommandArgument;
import fr.eris.eristrade.utils.GetValue;
import fr.eris.eristrade.utils.StringUtils;

import java.util.List;

public class DoubleCommandArgument extends CommandArgument<Double> {
    protected DoubleCommandArgument(boolean canBeNull) {
        super(canBeNull);
    }

    protected DoubleCommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput) {
        super(customInput, onlyCustomInput);
    }

    protected DoubleCommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput, boolean canBeNull) {
        super(customInput, onlyCustomInput, canBeNull);
    }

    @Override
    public boolean isValid(String input) {
        return StringUtils.isDouble(input);
    }

    @Override
    public Double convertArgument(String input) {
        return Double.parseDouble(input);
    }
}
