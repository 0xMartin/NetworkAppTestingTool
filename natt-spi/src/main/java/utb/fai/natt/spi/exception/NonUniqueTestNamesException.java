package utb.fai.natt.spi.exception;

import utb.fai.natt.spi.StatusCode;

/**
 * NonUniqueTestNamesException is a custom exception class that represents an error in the configuration file syntax.
 */
public class NonUniqueTestNamesException extends Exception {

    public NonUniqueTestNamesException(String duplicitName) {
        super("The names of the test cases/test suites are not unique. Name [" + duplicitName + "] is found in multiple elements.");
    }

    /**
     * Returns the error code associated with this exception.
     * @return The error code associated with this exception.
     */
    public int getErrorCode() {
        return StatusCode.NON_UNIQUE_TEST_NAMES;
    }

}
