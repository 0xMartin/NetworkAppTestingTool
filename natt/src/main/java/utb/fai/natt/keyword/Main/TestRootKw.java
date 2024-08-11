package utb.fai.natt.keyword.Main;

import java.util.List;
import java.util.stream.Collectors;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.reportGenerator.TestCaseResult;

/**
 * Umoznuje definovat root struktur celeho testovani. Tato strukturma musi byt
 * vzdy jako hlavni a musi byt na zacatku konfigurace
 */
@NATTAnnotation.Keyword(
    name = "test_root",
    description = "Marks the root element of the test configuration. It must be located at the beginning of the testing configuration. Tests start executing from this point.",
    parameters = { "max_points", "initial_steps", "test_suites" },
    types = { ParamValType.DOUBLE, ParamValType.LIST, ParamValType.LIST }
    )
public class TestRootKw extends NATTKeyword {

    // zpracovane parametry
    protected Double maxPoints;
    protected List<NATTKeyword> initialSteps;
    protected List<NATTKeyword> testSuites;

    private TestCaseResult result;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        boolean passed = true;

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
                    passed = false;
                }
            }
        }

        // vykonani vsechny test suits
        for (NATTKeyword keyword : this.testSuites) {
            if (keyword.isIgnored()) {
                this.result.logInfo("Test suite '" + keyword.getName() + "' is ignored");
                continue;
            }
            // vykona akce
            if (!keyword.execute(ctx)) {
                passed = false;
            }
            
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
            }

            // vykona ukoncujici akce
            keyword.deleteAction(ctx);
        }

        // vypocet vysledneho bodoveho hodnoceni (zahrne jen testovaci pripady, budou
        // ignorovany vysledy incializacnich akci test suits, ktere jsou taky zahrnuty v
        // reportu)
        if (maxPoints != null) {
            List<TestCaseResult> testCaseResults = NATTContext.instance().getTestCaseResults().stream()
                    .filter(tc -> tc.isIncludeInFinalScore()).collect(Collectors.toList());
            if (testCaseResults.isEmpty()) {
                NATTContext.instance().setFinalScore(this.maxPoints);
            } else {
                long passedCount = testCaseResults.stream()
                        .filter(TestCaseResult::isPassed)
                        .count();
                NATTContext.instance().setFinalScore(((float) passedCount / testCaseResults.size()) * this.maxPoints);
            }
        }

        return passed;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // max_points (double) [neni vyzadovany]
        ParameterValue val = this.getParameterValue("max_points", NATTKeyword.ParamValType.DOUBLE, false);
        maxPoints = (Double) val.getValue();
        if (maxPoints != null) {
            NATTContext.instance().setMaxScore(maxPoints);
        } else {
            NATTContext.instance().setMaxScore(-1);
        }

        // initial_steps (list<NATTKeyword>) [neni vyzadovany]
        val = this.getParameterValue("initial_steps", NATTKeyword.ParamValType.LIST, false);
        initialSteps = (List<NATTKeyword>) val.getValue();

        // test_suites (list<NATTKeyword>) [je vyzadovany]
        val = this.getParameterValue("test_suites", NATTKeyword.ParamValType.LIST, true);
        testSuites = (List<NATTKeyword>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////

        // pro logovani vysledu inicializacnich akci
        this.result = new TestCaseResult("Test root initialization", null, "",
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
