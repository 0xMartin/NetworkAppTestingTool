package utb.fai.Core;

public class StatusCode {

    // Stav uspesneho provedeni testu
    public static int TEST_PASSED = 0;

    // Stav selhani testu
    public static int TEST_FAILED = 1000;

    // chyba pri spusteni aplikace
    public static int TESTED_APP_FAILED_TO_RUN = 2000;

    // Stav, kdy se vyskytuji neunikatni nazvy modulu
    public static int NON_UNIQUE_MODULE_NAMES = 3000;

    // Stav, kdy se vyskytuji neunikatni nazvy testu
    public static int NON_UNIQUE_TEST_NAMES = 4000;

    // Stav, kdy je v konfiguraci chybna syntaxe
    public static int INVALID_SYNTAX_IN_CONFIGURATION = 5000;

    // Interni chyba
    public static int INTERNAL_ERROR = 6000;

}
