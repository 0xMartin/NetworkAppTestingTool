package utb.fai.Keyword.General;

import java.util.concurrent.CopyOnWriteArrayList;

import utb.fai.Core.Keyword;
import utb.fai.Core.MessageBuffer.NATTMessage;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;

/**
 * Umoznuje spocitat pocet zprav v bufferu a ulozit toto cislo do definovane
 * promenne
 */
@NATTAnnotation.Keyword(name = "count_and_store")
public class CountAndStoreKw extends Keyword {

    protected String varName;
    protected String moduleName;

    private boolean status;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        CopyOnWriteArrayList<NATTMessage> messages = NATTContext.instance().getMessageBuffer().getMessages(moduleName);
        this.varName = NATTContext.instance().storeValueToVariable(varName, String.valueOf(messages.size()));
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
        String message = String.format("The following value has been stored in a variable named <b>[%s]<b>: <b>'%s'</b>",
                this.varName,
                NATTContext.instance().getVariable(this.varName));
        return super.getDescription() + "<br><font color=\"green\">" + message + "</font>";
    }

}
