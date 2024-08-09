package utb.fai.ReportGenerator;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

/**
 * Třída reprezentující výsledek testovacího případu. Implementuje zapis do
 * souboru, kde se bude nachazet vysledny report testovani.
 */
public class TestCaseResult {

    /** Nazev testovaciho pripadu */
    private String testCaseName;

    /** Nazev testovaciho pripadu */
    private String testSuitName;

    /** Popis testu casu */
    private String description;

    /** Stav testu casu (true = uspesny, false = neuspesny) */
    private boolean passed;

    /**
     * Obsahuje stav o tom, zde bude tento test case zahrnut ve finalnim bodovem
     * hodnoceni.
     */
    private boolean includeInFinalScore;

    /** Test extent */
    private ExtentTest extentTest;

    /**
     * Konstruktor pro vytvoření instance TestCaseResult.
     * 
     * @param testCaseName Nazev testovaciho pripadu
     * @param testSuitName Nazev testovaciho pripadu
     * @param description  Popis testovaciho pripadu
     * @param extent       Instance ExtentReports pro tvorbu reportu
     */
    public TestCaseResult(String testCaseName, String testSuitName, String description, ExtentReports extent) {
        this.testCaseName = testCaseName;
        this.testSuitName = testSuitName;
        this.description = description;
        this.passed = true;
        this.includeInFinalScore = true;
        this.extentTest = extent.createTest(this.testCaseName, this.description);
        if (this.testSuitName != null) {
            this.extentTest.assignCategory(this.testSuitName);
        }
    }

    /**
     * Navrati stav o tom vysledky toho test casu zahrnou pri vyslednem bodovem
     * hodnoceni
     * 
     * @return Stav
     */
    public boolean isIncludeInFinalScore() {
        return includeInFinalScore;
    }

    /**
     * Nastavi stav o tom, zde vysledky tohoto test casu zahrnou pri vyslednem
     * bodovem hodnoceni
     * 
     * @param isTestCase Stav
     */
    public void includeInFinalScore(boolean isTestCase) {
        this.includeInFinalScore = isTestCase;
    }

    /**
     * Metoda pro ziskani nazvu testovaciho pripadu.
     * 
     * @return Nazev testovaciho pripadu
     */
    public String getTestCaseName() {
        return testCaseName;
    }

    /**
     * Metoda pro ziskani nazvu testovaci sady.
     * 
     * @return Nazev testovaci sady
     */
    public String getTestSuitName() {
        return testSuitName;
    }

    /**
     * Navrati popis testovaciho pripadu
     * 
     * @return Popis
     */
    public String getDescription() {
        return description;
    }

    /**
     * Metoda pro ziskani stavu testovaciho pripadu.
     * 
     * @return Stav testu casu (true = uspesny, false = neuspesny)
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * Metoda pro zaznamenani akce provedené v ramci testovaciho pripadu.
     * 
     * @param actionDescription Popis provedene akce
     * @param passed            True, pokud akce byla uspesna, jinak false
     */
    public void logAction(String actionDescription, boolean passed) {
        if (passed) {
            // zapis vysledku provedene akce
            extentTest.pass(actionDescription);
        } else {
            // zapis vysledku provedene akce
            this.passed = false;
            extentTest.fail(actionDescription);
        }
    }

    /**
     * Zapise info text do reportu
     * 
     * @param info Info text
     */
    public void logInfo(String info) {
        extentTest.info(info);
    }

}
