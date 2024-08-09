package utb.fai.Keyword.Main;

import java.util.List;
import java.util.stream.Collectors;

import utb.fai.Core.Keyword;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.ReportGenerator.TestCaseResult;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;

/**
 * Umoznuje definovat root struktur celeho testovani. Tato strukturma musi byt
 * vzdy jako hlavni a musi byt na zacatku konfigurace
 */
@NATTAnnotation.Keyword(name = "test_root")
public class TestRootKw extends Keyword {

    protected Double maxPoints;
    protected List<Keyword> initialSteps;
    protected List<Keyword> testSuites;

    private TestCaseResult result;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        boolean passed = true;

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
                    passed = false;
                }
            }
        }

        // vykonani vsechny test suits
        for (Keyword keyword : this.testSuites) {
            if (keyword.isIgnored()) {
                this.result.logInfo("Test suite '" + keyword.getName() + "' is ignored");
                continue;
            }
            // vykona akce
            if (!keyword.execute()) {
                passed = false;
            }
            
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
            }

            // vykona ukoncujici akce
            keyword.deleteAction();
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
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // max_points (double) [neni vyzadovany]
        ParameterValue val = this.getParameterValue("max_points", Keyword.ParameterValueType.DOUBLE, false);
        maxPoints = (Double) val.getValue();
        if (maxPoints != null) {
            NATTContext.instance().setMaxScore(maxPoints);
        } else {
            NATTContext.instance().setMaxScore(-1);
        }

        // initial_steps (list<keyword>) [neni vyzadovany]
        val = this.getParameterValue("initial_steps", Keyword.ParameterValueType.LIST, false);
        initialSteps = (List<Keyword>) val.getValue();

        // test_suites (list<keyword>) [je vyzadovany]
        val = this.getParameterValue("test_suites", Keyword.ParameterValueType.LIST, true);
        testSuites = (List<Keyword>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////

        // pro logovani vysledu inicializacnich akci
        this.result = new TestCaseResult("Test root initialization", null, "",
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
