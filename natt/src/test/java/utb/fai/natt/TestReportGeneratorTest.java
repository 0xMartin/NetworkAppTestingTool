package utb.fai.natt;

import org.junit.Test;

import com.aventstack.extentreports.ExtentReports;

import utb.fai.natt.reportGenerator.TestCaseResult;
import utb.fai.natt.reportGenerator.TestReportGenerator;

import static org.junit.Assert.assertTrue;

import java.io.File;

public class TestReportGeneratorTest {

    private final String fileName = "test-report.html";

    @Test
    public void testGenerateReportExtent() throws Exception {

        ExtentReports extent = TestReportGenerator.generateReportExtent(fileName, "Network App Testing");

        assertTrue(extent != null);

        TestCaseResult res = new TestCaseResult("Test case 1", "Suit 1", "Test case 1 description", extent);
        res.logAction("Action 1", true);
        res.logAction("Action 2", true);
        res = new TestCaseResult("Test case 2", "Suit 1", "Test case 2 description", extent);
        res.logAction("Action 1", false);

        TestReportGenerator.exportReportToFile(extent);

        File file = new File(fileName);
        assertTrue(file.exists());

        file.delete();
    }

}