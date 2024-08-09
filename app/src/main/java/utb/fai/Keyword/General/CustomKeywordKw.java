package utb.fai.Keyword.General;

import java.util.*;
import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;

/**
 * Umoznuje definovat vlastni keywordu
 */
@NATTAnnotation.Keyword(name = "custom_keyword")
public class CustomKeywordKw extends Keyword {

    protected String kwName;
    public List<String> params;
    public List<Keyword> steps;

    private boolean state;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        state = false;

        // zpracovani promennych v retezci
        this.kwName = VariableProcessor.processVariables(this.kwName);

        if (this.kwName.length() == 0) {
            return false;
        }

        // registruje custom keywordu
        NATTContext.instance().getCustomKeywords().put(this.kwName, this);

        state = true;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", Keyword.ParameterValueType.STRING,
                true);
        kwName = (String) val.getValue();

        // params (list) [je vyzadovany]
        val = this.getParameterValue("params", Keyword.ParameterValueType.LIST,
                false);
        params = (List<String>) val.getValue();

        // steps (string) [je vyzadovany]
        val = this.getParameterValue("steps", Keyword.ParameterValueType.LIST,
                true);
        steps = (List<Keyword>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        // odstrani registraci custom keywordy
        NATTContext.instance().getCustomKeywords().remove(this.kwName);
    }

    @Override
    public String getDescription() {
        if (!state) {
            return super.getDescription() + "<br><font color=\"red\">Failed to create custom keyword '" + this.kwName
                    + "'</font>";
        } else {
            return super.getDescription() + "<br><font color=\"green\">Custom keyword '" + this.kwName
                    + "' successfully created</font>";
        }
    }

}
