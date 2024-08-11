package utb.fai.natt.keyword.General;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Umoznuje nahradit specificky text v promenne. Je mozne v listu specifikovat
 * vice textu pro nahrazeni
 */
@NATTAnnotation.Keyword(
    name = "replace", 
    description = "Retrieves the content of a specific variable, replacing all desired words with their replacements. The result is stored in another variable.", 
    parameters = { "to_var", "from_var", "str_from", "str_to" }, 
    types = { ParamValType.STRING, ParamValType.STRING, ParamValType.LIST, ParamValType.LIST }
    )
public class ReplaceKw extends NATTKeyword {

    protected String toVar;
    protected String fromVar;
    protected List<String> strFrom;
    protected List<String> strTo;

    private boolean status;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.fromVar = VariableProcessor.processVariables(this.fromVar);
        this.toVar = VariableProcessor.processVariables(this.toVar);
        Map<String, String> tmpReplace = new HashMap<String, String>();
        for (int i = 0; i < this.strFrom.size() && i < this.strTo.size(); ++i) {
            tmpReplace.put(VariableProcessor.processVariables(this.strFrom.get(i)),
                    VariableProcessor.processVariables(this.strTo.get(i)));
        }

        // nahrazeni textu
        String value = NATTContext.instance().getVariable(this.fromVar);
        if (value == null) {
            return false;
        }
        for (Map.Entry<String, String> entry : tmpReplace.entrySet()) {
            value = value.replaceAll(entry.getKey(), entry.getValue());
        }

        // zapis do promenne
        this.toVar = NATTContext.instance().storeValueToVariable(this.toVar, value);
        if (this.toVar != null) {
            status = true;
        }

        return this.toVar != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // to_var (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("to_var", NATTKeyword.ParamValType.STRING,
                true);
        toVar = (String) val.getValue();

        // from_var (string) [je vyzadovany]
        val = this.getParameterValue("from_var", NATTKeyword.ParamValType.STRING,
                true);
        fromVar = (String) val.getValue();

        // str_from (string) [je vyzadovany]
        val = this.getParameterValue("str_from", NATTKeyword.ParamValType.LIST,
                true);
        strFrom = (List<String>) val.getValue();

        // str_to (string) [je vyzadovany]
        val = this.getParameterValue("str_to", NATTKeyword.ParamValType.LIST,
                true);
        strTo = (List<String>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        if (this.toVar != null) {
            NATTContext.instance().getVariables().remove(this.toVar);
        }
    }

    @Override
    public String getDescription() {
        if (status == false) {
            return super.getDescription() + String
                    .format("<br><font color=\"red\">Failed to replace text in variable <b>'%s'</b>.</font>", this.fromVar);
        }
        return super.getDescription() + String.format(
                "<br><font color=\"green\">All strings <b>'%s'</b> in variable <b>'%s'</b> were successfully replaced with strings <b>'%s'</b>. Final string stored it to the varialbe <b>'%s'</b>.</font>",
                this.strFrom, this.fromVar, this.strTo, this.toVar);
    }

}
