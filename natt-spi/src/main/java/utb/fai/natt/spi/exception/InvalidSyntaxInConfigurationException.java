package utb.fai.natt.spi.exception;

import utb.fai.natt.spi.StatusCode;

/**
 * InvalidSyntaxInConfigurationException is a custom exception class that represents an error in the configuration file syntax.
 */
public class InvalidSyntaxInConfigurationException extends Exception {

    public InvalidSyntaxInConfigurationException(String message) {
        super("Invalid syntax in configuration file. Error: " + message);
    }

    /**
     * Returns the error code associated with this exception.
     * @return The error code associated with this exception.
     */
    public int getErrorCode() {
        return StatusCode.INVALID_SYNTAX_IN_CONFIGURATION;
    }

}
