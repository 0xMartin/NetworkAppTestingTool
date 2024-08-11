package utb.fai.natt.spi.exception;

import utb.fai.natt.spi.StatusCode;

/**
 * InternalErrorException is a custom exception class that represents an internal error in the NATT system.
 */
public class InternalErrorException extends Exception {

    /**
     * Constructs a new InternalErrorException with the specified error message.
     * @param message The error message.
     */
    public InternalErrorException(String message) {
        super("Internal NATT error. Error message: " + message);
    }

    /**
     * Returns the error code associated with this exception.
     * @return The error code associated with this exception.
     */
    public int getErrorCode() {
        return StatusCode.INTERNAL_ERROR;
    }

}
