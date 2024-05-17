package utb.fai;

import org.junit.Test;
import com.aventstack.extentreports.ExtentReports;

import utb.fai.ReportGenerator.TestCaseResult;

import static org.junit.Assert.assertTrue;

public class TestCaseResultTest {

    @Test
    public void testCreateTestCaseResult() {
        ExtentReports extent = new ExtentReports();

        TestCaseResult testCaseResult = new TestCaseResult("Test Case 1", "Test Suite 1", "Test Case 1 description",
                extent);

        assertTrue(testCaseResult != null);
    }

    @Test
    public void testLogAction() {
        ExtentReports extent = new ExtentReports();

        TestCaseResult testCaseResult = new TestCaseResult("Test Case 1", "Test Suite 1", "Test Case 1 description",
                extent);

        testCaseResult.logAction("Action description", true);

        assertTrue(testCaseResult.isPassed());

        testCaseResult.logAction("Failed action description", false);

        assertTrue(!testCaseResult.isPassed());
    }

}
