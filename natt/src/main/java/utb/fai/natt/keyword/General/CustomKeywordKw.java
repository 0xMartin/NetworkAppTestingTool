package utb.fai.natt.keyword.General;

import java.util.*;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje definovat vlastni keywordu
 */
@NATTAnnotation.Keyword(
    name = "custom_keyword", 
    description = "Allows for the definition of a custom keyword within the system. The custom keyword can include a series of steps and input parameters.", 
    parameters = { "name", "params", "steps" }, 
    types = { ParamValType.STRING, ParamValType.LIST, ParamValType.LIST }
    )
public class CustomKeywordKw extends NATTKeyword {

    protected String kwName;
    public List<String> params;
    public List<NATTKeyword> steps;

    private boolean state;

    @Override
    public boolean execute(INATTContext ctx)
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
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", NATTKeyword.ParamValType.STRING,
                true);
        kwName = (String) val.getValue();

        // params (list) [je vyzadovany]
        val = this.getParameterValue("params", NATTKeyword.ParamValType.LIST,
                false);
        params = (List<String>) val.getValue();

        // steps (string) [je vyzadovany]
        val = this.getParameterValue("steps", NATTKeyword.ParamValType.LIST,
                true);
        steps = (List<NATTKeyword>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
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
