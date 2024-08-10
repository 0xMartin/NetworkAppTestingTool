package utb.fai.natt.spi.exception;

import utb.fai.natt.spi.StatusCode;

public class NonUniqueTestNamesException extends Exception {

    public NonUniqueTestNamesException(String duplicitName) {
        super("The names of the test cases/test suites are not unique. Name [" + duplicitName + "] is found in multiple elements.");
    }

    public int getErrorCode() {
        return StatusCode.NON_UNIQUE_TEST_NAMES;
    }

}
