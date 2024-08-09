package utb.fai.Exception;

import utb.fai.Core.StatusCode;

public class InternalErrorException extends Exception {

    public InternalErrorException(String message) {
        super("Internal NATT error. Error message: " + message);
    }

    public int getErrorCode() {
        return StatusCode.INTERNAL_ERROR;
    }

}
