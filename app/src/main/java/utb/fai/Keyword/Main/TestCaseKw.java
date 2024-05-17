package utb.fai.Keyword.Main;

import java.util.List;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;
import utb.fai.ReportGenerator.TestCaseResult;

/**
 * Umoznuje definovat testovaci pripad
 */
@NATTAnnotation.Keyword(name = "test_case")
public class TestCaseKw extends Keyword {

    protected String description;
    protected List<Keyword> steps;

    private TestCaseResult result;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {

        // zpracovani promennych v retezci
        this.description = VariableProcessor.processVariables(this.description);

        // vykonani vsechny kroku v testovacim pripadu
        for (Keyword keyword : this.steps) {
            if (keyword.isIgnored()) {
                this.result.logInfo(keyword.getKeywordName() + " is ignored");
                continue;
            }
            // vykonani akce
            boolean passed = keyword.execute();
            // zapsani do reportu
            this.result.logAction(keyword.getDescription(), passed);
        }

        return result.isPassed();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", Keyword.ParameterValueType.STRING, true);
        this.setName((String) val.getValue());

        // description (string) [je vyzadovany]
        val = this.getParameterValue("description", Keyword.ParameterValueType.STRING, true);
        description = (String) val.getValue();

        // steps (list<keyword>) [je vyzadovany]
        val = this.getParameterValue("steps", Keyword.ParameterValueType.LIST, true);
        steps = (List<Keyword>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////

        // Vytvori instacni vysledku testovaciho pripadu a jeji referenci slozi do
        // kontextu. Je specifikovan nazev, nazev testovaci sady a popis
        this.result = new TestCaseResult(this.getName(), this.getParent().getName(), this.description,
                NATTContext.instance().getReportExtent());
        this.result.includeInFinalScore(true);
        NATTContext.instance().bindTestCaseResult(this.result);
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        NATTContext.instance().getMessageBuffer().clearAll();
        for (Keyword keyword : this.steps) {
            if (keyword.isIgnored())
                continue;
            keyword.deleteAction();
        }
    }

}
