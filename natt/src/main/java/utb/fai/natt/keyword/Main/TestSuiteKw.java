package utb.fai.natt.keyword.Main;

import java.util.List;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.reportGenerator.TestCaseResult;

/**
 * Umoznuje definovat testovaci sadu
 */
@NATTAnnotation.Keyword(
    name = "test_suite", 
    description = "Used to define a testing suite.", 
    parameters = { "name", "delay", "initial_steps", "test_cases" }, 
    types = { ParamValType.STRING, ParamValType.LONG, ParamValType.LIST, ParamValType.LIST },
    kwGroup = "NATT Main"
    )
public class TestSuiteKw extends NATTKeyword {

    protected NATTLogger logger = new NATTLogger(TestSuiteKw.class);

    // zpracovane parametry
    protected Long delay;
    protected List<NATTKeyword> initialSteps;
    protected List<NATTKeyword> testCases;

    private TestCaseResult result;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        boolean suitePassed = true;

        // vykonani inicializacnich keyword
        if (this.initialSteps != null) {
            for (NATTKeyword keyword : this.initialSteps) {
                if (keyword.isIgnored()) {
                    this.result.logInfo(keyword.getKeywordName() + " is ignored");
                    continue;
                }
                boolean pass = keyword.execute(ctx);
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
        for (NATTKeyword keyword : this.testCases) {
            if (keyword.isIgnored()) {
                this.result.logInfo("Test case '" + keyword.getName() + "' is ignored");
                continue;
            }
            this.logger.info("Test case '" + keyword.getName() + "' starts executing now");
            boolean pass = keyword.execute(ctx);
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
            keyword.deleteAction(ctx);

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
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", NATTKeyword.ParamValType.STRING, true);
        this.setName((String) val.getValue());

        // delay (long) [neni vyzadovany]
        val = this.getParameterValue("delay", NATTKeyword.ParamValType.LONG, false);
        delay = (Long) val.getValue();

        // initial_steps (list<NATTKeyword>) [neni vyzadovany]
        val = this.getParameterValue("initial_steps", NATTKeyword.ParamValType.LIST, false);
        initialSteps = (List<NATTKeyword>) val.getValue();

        // test_cases (list<NATTKeyword>) [je vyzadovany]
        val = this.getParameterValue("test_cases", NATTKeyword.ParamValType.LIST, true);
        testCases = (List<NATTKeyword>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////

        // pro logovani vysledu inicializacnich akci
        this.result = new TestCaseResult("Test suite '" + this.getName() + "' initialization", this.getName(), "",
                NATTContext.instance().getReportExtent());
        this.result.includeInFinalScore(false);
        NATTContext.instance().bindTestCaseResult(this.result);
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        NATTContext.instance().getMessageBuffer().clearAll();
        if (this.initialSteps != null) {
            for (NATTKeyword keyword : this.initialSteps) {
                if (keyword.isIgnored())
                    continue;
                keyword.deleteAction(ctx);
            }
        }
    }

}
