package utb.fai.Keyword.Assert;

import utb.fai.Core.NATTAssert;
import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;

/**
 * Umoznuje definovat trzeni ze retezes splnuje definovanou podminku shody se
 * zadanym ocekavanym textem
 */
@NATTAnnotation.Keyword(name = "assert_string")
public class AssertStringKw extends Keyword {

    private NATTLogger logger = new NATTLogger(AssertEqualsKw.class);

    protected String varName;
    protected String expectedText;
    protected String mode;
    protected Boolean caseSensitive;
    protected Boolean result;

    // finalni stav tvrzeni (bude pouzito pri informativni zprave v reportu)
    private boolean finalStatus;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        this.finalStatus = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);
        this.expectedText = VariableProcessor.processVariables(this.expectedText);
        this.mode = VariableProcessor.processVariables(this.mode);

        if (result == null) {
            this.result = true;
        }

        if (caseSensitive == null) {
            caseSensitive = true;
        }

        String varValue = NATTContext.instance().getVariable(varName);
        if (varValue == null) {
            logger.warning(String.format("Assertion failed. Variable '%s' not found!", varName));
            return false;
        }

        this.finalStatus = NATTAssert.assertCondition(varValue, expectedText, mode, caseSensitive);

        // normalizace na podle ocekavaneho vysledku
        this.finalStatus = (this.finalStatus == this.result);
        
        if (!this.finalStatus) {
            logger.warning(String.format(
                    "Assertion failed. %s was expected as the result. Condition: (Value of variable '%s' must %s expected text '%s')",
                    this.result ? "True" : "False", varValue, mode, this.expectedText));
        }

        return this.finalStatus;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("var_name", Keyword.ParameterValueType.STRING,
                true);
        varName = (String) val.getValue();

        // expected (string) [je vyzadovany]
        val = this.getParameterValue("expected", Keyword.ParameterValueType.STRING,
                true);
        expectedText = (String) val.getValue();

        // mode (string) [neni vyzadovany]
        val = this.getParameterValue("mode", Keyword.ParameterValueType.STRING,
                false);
        mode = (String) val.getValue();

        // case_sensitive (string) [neni vyzadovany]
        val = this.getParameterValue("case_sensitive", Keyword.ParameterValueType.BOOLEAN,
                false);
        caseSensitive = (Boolean) val.getValue();

        // result (boolean) [neni vyzadovany]
        val = this.getParameterValue("result", Keyword.ParameterValueType.BOOLEAN,
                false);
        result = (Boolean) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String varValue = NATTContext.instance().getVariable(varName);
        if (varValue == null) {
            varValue = "";
        }
        String data = varValue.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

        String message;
        if (this.finalStatus) {
            message = String.format(
                    "<font color=\"green\">Assertion succeeded. <b>%s</b> was expected as the result. Condition: (Value of variable <b>'%s'</b> must %s expected text <b>'%s'</b>)</font>",
                    this.result ? "True" : "False", data, mode, this.expectedText);
        } else {
            message = String.format(
                    "<font color=\"red\">Assertion failed. <b>%s</b> was expected as the result. Condition: (Value of variable <b>'%s'</b> must %s expected text <b>'%s'</b>)</font>",
                    this.result ? "True" : "False", data, mode, this.expectedText);
        }

        return super.getDescription() + "<br>" + message;
    }

}
