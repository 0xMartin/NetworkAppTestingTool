package utb.fai.natt.spi.exception;

import utb.fai.natt.spi.StatusCode;

public class InvalidSyntaxInConfigurationException extends Exception {

    public InvalidSyntaxInConfigurationException(String message) {
        super("Invalid syntax in configuration file. Error: " + message);
    }

    public int getErrorCode() {
        return StatusCode.INVALID_SYNTAX_IN_CONFIGURATION;
    }

}
