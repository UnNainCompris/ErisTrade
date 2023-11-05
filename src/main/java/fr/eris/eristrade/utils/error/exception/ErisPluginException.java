package fr.eris.eristrade.utils.error.exception;

import fr.eris.eristrade.utils.error.data.ErrorCode;
import fr.eris.eristrade.utils.error.data.ErrorType;
import lombok.Getter;

/* This file is an experimental feature of eris development */
@Getter
public class ErisPluginException extends Exception {
    private final ErrorType errorType;
    private final ErrorCode errorCode;

    public ErisPluginException(ErrorType errorType, ErrorCode errorCode) {
        this(errorType, errorCode, "A error was catch in the Eris Plugin !");
    }

    public ErisPluginException(ErrorType errorType, ErrorCode errorCode, String additionalText) {
        super(additionalText + "\n" + errorCode.getRawMessage() + " Maybe caused by " + errorType.getRawMessage() +
                " \nDeveloper code [" + errorType.getCode() + errorCode.getCode());
        this.errorType = errorType;
        this.errorCode = errorCode;
    }
}
