package utb.fai.natt.spi;

public class StatusCode {

    // Status for successful test execution
    public static int TEST_PASSED = 0;

    // Status for test failure
    public static int TEST_FAILED = 1000;

    // Status when there are non-unique module names
    public static int NON_UNIQUE_MODULE_NAMES = 2000;

    // Status when there are non-unique test names
    public static int NON_UNIQUE_TEST_NAMES = 3000;

    // Status when there is invalid syntax in the configuration
    public static int INVALID_SYNTAX_IN_CONFIGURATION = 4000;

    // Internal error status
    public static int INTERNAL_ERROR = 5000;
}