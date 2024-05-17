package utb.fai.Exception;

import utb.fai.Core.StatusCode;

public class TestedAppFailedToRunException extends Exception {

    public TestedAppFailedToRunException(String appArguments) {
        super("Failed to run the tested application. The application was started with the following arguments: "
                + appArguments);
    }

    public int getErrorCode() {
        return StatusCode.TESTED_APP_FAILED_TO_RUN;
    }

}
