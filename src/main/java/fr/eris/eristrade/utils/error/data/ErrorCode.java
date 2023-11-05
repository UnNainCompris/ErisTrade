package fr.eris.eristrade.utils.error.data;

import lombok.Getter;

public enum ErrorCode {
    VITAL_SETUP_FAILED("#0001", "Contact plugin developer.", ""),
    INVENTORY_INVALID_ITEM("#1001", "An item was considered as invalid while creating an ErisInventoryItem instance.", ""),
    INVENTORY_INVALID_INVENTORY_SIZE("#1002", "The plugin try to generate a inventory with an invalid amount of row", ""),
    INVENTORY_INVALID_INVENTORY_ITEM_SLOT("#1003", "The plugin try to generate a inventory with an invalid content (Item at wrong slot)", ""),
    COMMAND_TWO_NULLABLE_ARGUMENT_IN_ROW("#2001", "The plugin try te register a command with two nullable argument in a row", ""),
    COMMAND_NULLABLE_ARGUMENT_NOT_AT_END("#2002", "The plugin try te register a command with a nullable argument but not at the argument is not the last one", ""),
    COMMAND_FOR_CONSOLE_AND_PLAYER("#2003", "The plugin try te register a command that only for player and only for console at the same time", ""),
    COMMAND_MULTIPLE_SAME_ALIASES_OR_COMMAND("#2004", "The plugin try te register a command with the same name that an another one", ""),
    COMMAND_MULTIPLE_SAME_CUSTOM_INPUT("#2005", "The plugin try te register a command with multiple same custom input", ""),
    SUBCOMMAND_INVALID_ARGUMENT_WITH_SUBCOMMAND("#3001", "The plugin try te register a sub command that register subcommand but the first argument are not a String / none argument type", ""),
    UNDEFINED("#FUCK", "Developer error", "&4&cDeveloper error");

    @Getter private final String code;
    @Getter private final String rawMessage;
    @Getter private final String displayMessage;

    ErrorCode(String code, String rawMessage, String displayMessage) {
        this.code = code;
        this.rawMessage = (rawMessage == null || rawMessage.isEmpty()) ? displayMessage : rawMessage;
        this.displayMessage = (displayMessage == null || displayMessage.isEmpty()) ? rawMessage : displayMessage;
    }
}
