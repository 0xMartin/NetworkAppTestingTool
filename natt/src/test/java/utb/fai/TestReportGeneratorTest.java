package utb.fai;

import org.junit.Test;

import utb.fai.ReportGenerator.TestCaseResult;
import utb.fai.ReportGenerator.TestReportGenerator;
import com.aventstack.extentreports.ExtentReports;

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