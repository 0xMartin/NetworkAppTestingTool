package utb.fai.natt.keyword.General;

import java.util.concurrent.CopyOnWriteArrayList;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.INATTMessage;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje spocitat pocet zprav v bufferu a ulozit toto cislo do definovane
 * promenne
 */
@NATTAnnotation.Keyword(
    name = "count_and_store", 
    description = "Counts the number of received messages during a single test case and saves this count into a variable.", 
    parameters = { "var_name", "module_name" }, 
    types = { ParamValType.STRING, ParamValType.STRING },
    kwGroup = "NATT General"
    )
public class CountAndStoreKw extends NATTKeyword {

    protected String varName;
    protected String moduleName;

    private boolean status;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        CopyOnWriteArrayList<INATTMessage> messages = NATTContext.instance().getMessageBuffer().getMessages(moduleName);
        this.varName = NATTContext.instance().storeValueToVariable(varName, String.valueOf(messages.size()));
        if (this.varName != null) {
            status = true;
        }
        
        return this.varName != null;
    }

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("var_name", NATTKeyword.ParamValType.STRING,
                true);
        varName = (String) val.getValue();

        // module_name (string) [je vyzadovany]
        val = this.getParameterValue("module_name", NATTKeyword.ParamValType.STRING,
                true);
        moduleName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
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
