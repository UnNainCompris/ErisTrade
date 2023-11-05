package fr.eris.eristrade.utils.error.data;

import fr.eris.eristrade.utils.ColorUtils;
import lombok.Getter;

public enum ErrorType {
    DEVELOPERS("&D", "Developers", "&dDevelopers"),
    PLAYER("&P", "Player", "&6Player"),
    UNDEFINED("&U", "Undefined", "&4Undefined");

    @Getter private final String code;
    @Getter private final String rawMessage;
    @Getter private final String displayMessage;

    ErrorType(String code, String rawMessage, String displayMessage) {
        this.code = code;
        this.rawMessage = rawMessage;
        this.displayMessage = ColorUtils.translate(displayMessage);
    }
}
