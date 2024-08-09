package utb.fai.Keyword.Main;

import java.util.List;

import utb.fai.Core.Keyword;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.ReportGenerator.TestCaseResult;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;

/**
 * Umoznuje definovat testovaci sadu
 */
@NATTAnnotation.Keyword(name = "test_suite")
public class TestSuiteKw extends Keyword {

    protected NATTLogger logger = new NATTLogger(TestSuiteKw.class);

    protected Long delay;
    protected List<Keyword> initialSteps;
    protected List<Keyword> testCases;

    private TestCaseResult result;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        boolean suitePassed = true;

        // vykonani inicializacnich keyword
        if (this.initialSteps != null) {
            for (Keyword keyword : this.initialSteps) {
                if (keyword.isIgnored()) {
                    this.result.logInfo(keyword.getKeywordName() + " is ignored");
                    continue;
                }
                boolean pass = keyword.execute();
                // zapsani do reportu
                this.result.logAction(keyword.getDescription(), pass);
                if (!pass) {
                    suitePassed = false;
                }
            }
        }

        if (this.delay == null) {
            this.delay = 500L;
        }

        // vykonani vsechny test casu
        for (Keyword keyword : this.testCases) {
            if (keyword.isIgnored()) {
                this.result.logInfo("Test case '" + keyword.getName() + "' is ignored");
                continue;
            }
            this.logger.info("Test case '" + keyword.getName() + "' starts executing now");
            boolean pass = keyword.execute();
            // vykona akce
            if (!pass) {
                suitePassed = false;
                logger.warning(String.format("Test suite '%s' - Test case '%s' failed",
                        this.getName(), keyword.getName()));
            } else {
                logger.info(String.format("Test suite '%s' - Test case '%s' passed",
                        this.getName(), keyword.getName()));
            }

            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
            }

            // ukoncujici akce test case
            keyword.deleteAction();

            // delay
            if (this.delay > 0) {
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                }
            }
        }

        return suitePassed;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", Keyword.ParameterValueType.STRING, true);
        this.setName((String) val.getValue());

        // delay (long) [neni vyzadovany]
        val = this.getParameterValue("delay", Keyword.ParameterValueType.LONG, false);
        delay = (Long) val.getValue();

        // initial_steps (list<Keyword>) [neni vyzadovany]
        val = this.getParameterValue("initial_steps", Keyword.ParameterValueType.LIST, false);
        initialSteps = (List<Keyword>) val.getValue();

        // test_cases (list<Keyword>) [je vyzadovany]
        val = this.getParameterValue("test_cases", Keyword.ParameterValueType.LIST, true);
        testCases = (List<Keyword>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////

        // pro logovani vysledu inicializacnich akci
        this.result = new TestCaseResult("Test suite '" + this.getName() + "' initialization", this.getName(), "",
                NATTContext.instance().getReportExtent());
        this.result.includeInFinalScore(false);
        NATTContext.instance().bindTestCaseResult(this.result);
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        NATTContext.instance().getMessageBuffer().clearAll();
        if (this.initialSteps != null) {
            for (Keyword keyword : this.initialSteps) {
                if (keyword.isIgnored())
                    continue;
                keyword.deleteAction();
            }
        }
    }

}
