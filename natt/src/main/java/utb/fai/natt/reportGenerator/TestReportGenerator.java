package utb.fai.natt.reportGenerator;

import java.util.HashMap;
import java.util.Map;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;

/**
 * Trida pro generovani reportu o provedenem testovani.
 */
public class TestReportGenerator {

    private static NATTLogger logger = new NATTLogger(TestReportGenerator.class);

    /** Titulek testovacího reportu */
    public static String REPORT_TITLE = "Network App Testing Tool";

    /**
     * Metoda pro generovani instance ExtentReports pro tvorbu reportu.
     * 
     * @param fileName       Nazev vystupniho souboru (vcetne pripony .html)
     * @param testReportName Nazev testovaciho reportu
     * @return Instance ExtentReports
     */
    public static ExtentReports generateReportExtent(String fileName, String testReportName) {
        if (!fileName.endsWith(".html")) {
            fileName += ".html";
        }

        ExtentReports extent = new ExtentReports();
        extent.setReportUsesManualConfiguration(true);

        ExtentSparkReporter spark = new ExtentSparkReporter(fileName);
        spark.config().setDocumentTitle(TestReportGenerator.REPORT_TITLE);
        spark.config().setReportName(testReportName);
        extent.attachReporter(spark);

        TestReportGenerator.logger
                .info(String.format("Extenet reports generated. Final report will be saved to file: %s", fileName));

        return extent;
    }

    /**
     * Metoda pro exportovani reportu testovani do souboru.
     * 
     * @param extent Instance ExtentReports
     */
    public static void exportReportToFile(ExtentReports extent) throws Exception {

        // do reportu prida i informace ohledne hodnoceni (skore nebude zapisovano pokud
        // je maximalni skore nastaveno na 0 nebo zapornou hodnut)
        if (NATTContext.instance().getMaxScore() > 0) {
            ExtentTest scoreInfoExtent = extent.createTest("Result score",
                    "This is not a test case. This is an informative page containing the final score of the tested application.");

            // obsahuje pocty uspesnych testu v jednotlivych testovacich sadach
            HashMap<String, Long> passCount = new HashMap<String, Long>();
            // obsahuje celkovy pocet testovacich pripadu v jednotlivych testovacich sadach
            HashMap<String, Long> totalCount = new HashMap<String, Long>();
            // celkovy pocet testovacich pripadu
            long testCaseCnt = 0;

            for (TestCaseResult res : NATTContext.instance().getTestCaseResults()) {
                if (res.isIncludeInFinalScore()) {
                    String testSuiteName = res.getTestSuitName();
                    totalCount.put(testSuiteName, totalCount.getOrDefault(testSuiteName, 0L) + 1);
                    testCaseCnt++;
                    if (res.isPassed()) {
                        passCount.put(testSuiteName, passCount.getOrDefault(testSuiteName, 0L) + 1);
                    }
                }
            }

            // vypis informaci o vyslednem bodovem hodnoceni pro kazdou testovaci sadu
            for (Map.Entry<String, Long> res : totalCount.entrySet()) {
                Long passCnt = passCount.getOrDefault(res.getKey(), 0L);
                double max_score = ((double) res.getValue() / testCaseCnt) * NATTContext.instance().getMaxScore();
                double score = ((double) passCnt / res.getValue()) * max_score;
                if (passCnt >= res.getValue()) {
                    scoreInfoExtent.pass(String.format("Tested application for test suite '%s' get %.2f / %.2f points",
                            res.getKey(), score, max_score));
                } else {
                    scoreInfoExtent.fail(String.format("Tested application for test suite '%s' get %.2f / %.2f points",
                            res.getKey(), score, max_score));
                }
            }
            // vypis celkoveho hodnoceni
            scoreInfoExtent
                    .info(String.format("TOTAL SCORE: %.2f / %.2f points", NATTContext.instance().getFinalScore(),
                            NATTContext.instance().getMaxScore()));
        }

        // exportuve reporte celeho testovani do html souboru
        extent.flush();
        TestReportGenerator.logger.info("Report saved to file");
    }

}
