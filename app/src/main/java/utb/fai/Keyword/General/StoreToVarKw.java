package utb.fai.Keyword.General;

import java.util.List;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Core.MessageBuffer.NATTMessage;
import utb.fai.Core.MessageBuffer.SearchType;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;

/**
 * Umoznuje definovat vyhledavaci prikaz. Ten se podle predanych vyhledavacich
 * kriterii pokusi najit zpravu v bufferu a jeji obsah ulozi do definovane
 * promenne.
 */
@NATTAnnotation.Keyword(name = "store_to_var")
public class StoreToVarKw extends Keyword {

    protected String varName;
    protected String moduleName;
    protected String text;
    protected String tag;
    protected String mode;
    protected Boolean caseSensitive;

    private boolean status;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.text = VariableProcessor.processVariables(this.text);
        this.tag = VariableProcessor.processVariables(this.tag);
        this.mode = VariableProcessor.processVariables(this.mode);

        // typ vyhledavani
        SearchType searchType;
        if (this.mode == null) {
            this.mode = "equals";
        }
        switch (this.mode.toLowerCase().trim()) {
            case "equals":
                searchType = SearchType.EQUALS;
                break;
            case "contains":
                searchType = SearchType.CONTAINS;
                break;
            case "startswith":
                searchType = SearchType.STARTSWITH;
                break;
            case "endswith":
                searchType = SearchType.ENDSWITH;
                break;
            default:
                searchType = SearchType.EQUALS;
        }

        if (caseSensitive == null) {
            caseSensitive = true;
        }

        // vyhleda v bufferu zpravy ty ktere spnuji danou podminku
        List<NATTMessage> messages = NATTContext.instance().getMessageBuffer().searchMessages(this.moduleName, this.tag,
                this.text, searchType, this.caseSensitive);
        if (messages.isEmpty()) {
            this.varName = NATTContext.instance().storeValueToVariable(this.varName, "");
            if (this.varName != null) {
                status = true;
            }
            return this.varName != null;
        }

        // prni zpravu ulozi do promenne
        String value = messages.get(0).getMessage();
        this.varName = NATTContext.instance().storeValueToVariable(this.varName, value);
        if (this.varName != null) {
            status = true;
        }
        return this.varName != null;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("var_name", Keyword.ParameterValueType.STRING,
                true);
        varName = (String) val.getValue();

        // module_name (string) [je vyzadovany]
        val = this.getParameterValue("module_name", Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // text (string) [neni vyzadovany]
        val = this.getParameterValue("text", Keyword.ParameterValueType.STRING,
                false);
        text = (String) val.getValue();

        // tag (string) [neni vyzadovany]
        val = this.getParameterValue("tag", Keyword.ParameterValueType.STRING,
                false);
        tag = (String) val.getValue();

        // mode (string) [neni vyzadovany]
        val = this.getParameterValue("mode", Keyword.ParameterValueType.STRING,
                false);
        mode = (String) val.getValue();

        // case_sensitive (string) [neni vyzadovany]
        val = this.getParameterValue("case_sensitive", Keyword.ParameterValueType.BOOLEAN,
                false);
        caseSensitive = (Boolean) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        if (this.varName != null) {
            NATTContext.instance().getVariables().remove(this.varName);
        }
    }

    @Override
    public String getDescription() {
        if (this.varName == null || status == false) {
            return super.getDescription() + "<br><font color=\"red\">Failed to store value to variable.</font>";
        }
        String data = NATTContext.instance().getVariable(this.varName);
        data = data.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        String message = String.format("The following value has been stored in a variable named <b>[%s]</b>: <b>'%s'</b>",
                this.varName, data);
        return super.getDescription() + "<br><font color=\"green\">" + message + "</font>";
    }

}
