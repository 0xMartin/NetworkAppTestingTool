package utb.fai.Keyword.General;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje nahradit specificky text v promenne. Je mozne v listu specifikovat
 * vice textu pro nahrazeni
 */
@NATTAnnotation.Keyword(name = "replace")
public class ReplaceKw extends Keyword {

    protected String toVar;
    protected String fromVar;
    protected List<String> strFrom;
    protected List<String> strTo;

    private boolean status;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
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
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // to_var (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("to_var", Keyword.ParameterValueType.STRING,
                true);
        toVar = (String) val.getValue();

        // from_var (string) [je vyzadovany]
        val = this.getParameterValue("from_var", Keyword.ParameterValueType.STRING,
                true);
        fromVar = (String) val.getValue();

        // str_from (string) [je vyzadovany]
        val = this.getParameterValue("str_from", Keyword.ParameterValueType.LIST,
                true);
        strFrom = (List<String>) val.getValue();

        // str_to (string) [je vyzadovany]
        val = this.getParameterValue("str_to", Keyword.ParameterValueType.LIST,
                true);
        strTo = (List<String>) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
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
