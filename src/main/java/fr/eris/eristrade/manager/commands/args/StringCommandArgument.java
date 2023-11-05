package fr.eris.eristrade.manager.commands.args;

import fr.eris.eristrade.utils.GetValue;

import java.util.List;

public class StringCommandArgument extends CommandArgument<String> {
    public StringCommandArgument(boolean canBeNull) {
        super(canBeNull);
    }

    public StringCommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput) {
        super(customInput, onlyCustomInput);
    }

    public StringCommandArgument(GetValue<List<String>> customInput, boolean onlyCustomInput, boolean canBeNull) {
        super(customInput, onlyCustomInput, canBeNull);
    }

    @Override
    public boolean isValid(String input) {
        return true;
    }

    @Override
    public String convertArgument(String input) {
        return input;
    }
}
