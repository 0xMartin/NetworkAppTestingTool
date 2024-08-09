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
 * Umoznuje ziskat zpravu z bufferu zprav vybraneho modulu s konktretnim indexem
 */
@NATTAnnotation.Keyword(name = "buffer_get")
public class BufferGetKw extends Keyword {

    protected String varName;
    protected String moduleName;
    protected Long index;

    private boolean status;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        CopyOnWriteArrayList<NATTMessage> messages = NATTContext.instance().getMessageBuffer().getMessages(moduleName);
        // pokud je index zaporny zacina od konce pole (zpravy prijate jako posledni)
        // ... posledni index pole = -1, predposledni = -2
        if (index < 0) {
            index += messages.size();
        }

        if (index < 0 || index >= messages.size()) {
            // hodnota neexistuje ... vlozi prazdnou hodnotu
            this.varName = NATTContext.instance().storeValueToVariable(varName, "");
        } else {
            // hodnota existuje ... vlozi hodnotu na danem indexu
            this.varName = NATTContext.instance().storeValueToVariable(varName,
                    messages.get(index.intValue()).getMessage());
        }

        if (this.varName != null) {
            status = true;
        }

        return this.varName != null;
    }

    @Override
    public void keywordInit() throws InvalidSyntaxInConfigurationException {
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

        // index (long) [je vyzadovany]
        val = this.getParameterValue("index", Keyword.ParameterValueType.LONG,
                true);
        index = (Long) val.getValue();
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
        String message = String.format("The following message has been stored in a variable named <b>[%s]</b>: <b>'%s'</b>",
                this.varName, data);
        return super.getDescription() + "<br><font color=\"green\">" + message + "</font>";
    }

}
