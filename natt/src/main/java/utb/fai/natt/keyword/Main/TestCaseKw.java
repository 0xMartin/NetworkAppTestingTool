package utb.fai.natt.keyword.Main;

import java.util.List;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.reportGenerator.TestCaseResult;

/**
 * Umoznuje definovat testovaci pripad
 */
@NATTAnnotation.Keyword(name = "test_case")
public class TestCaseKw extends NATTKeyword {

    protected String description;
    protected List<NATTKeyword> steps;

    private TestCaseResult result;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {

        // zpracovani promennych v retezci
        this.description = VariableProcessor.processVariables(this.description);

        // vykonani vsechny kroku v testovacim pripadu
        for (NATTKeyword keyword : this.steps) {
            if (keyword.isIgnored()) {
                this.result.logInfo(keyword.getKeywordName() + " is ignored");
                continue;
            }
            // vykonani akce
            boolean passed = keyword.execute(ctx);
            // zapsani do reportu
            this.result.logAction(keyword.getDescription(), passed);
        }

        return result.isPassed();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", NATTKeyword.ParameterValueType.STRING, true);
        this.setName((String) val.getValue());

        // description (string) [je vyzadovany]
        val = this.getParameterValue("description", NATTKeyword.ParameterValueType.STRING, true);
        description = (String) val.getValue();

        // steps (list<NATTKeyword>) [je vyzadovany]
        val = this.getParameterValue("steps", NATTKeyword.ParameterValueType.LIST, true);
        steps = (List<NATTKeyword>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////

        // Vytvori instacni vysledku testovaciho pripadu a jeji referenci slozi do
        // kontextu. Je specifikovan nazev, nazev testovaci sady a popis
        this.result = new TestCaseResult(this.getName(), this.getParent().getName(), this.description,
                NATTContext.instance().getReportExtent());
        this.result.includeInFinalScore(true);
        NATTContext.instance().bindTestCaseResult(this.result);
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        NATTContext.instance().getMessageBuffer().clearAll();
        for (NATTKeyword keyword : this.steps) {
            if (keyword.isIgnored())
                continue;
            keyword.deleteAction(ctx);
        }
    }

}
