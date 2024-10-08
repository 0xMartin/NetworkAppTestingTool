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
 * Umoznuje ziskat zpravu z bufferu zprav vybraneho modulu s konktretnim indexem
 */
@NATTAnnotation.Keyword(
    name = "buffer_get",
    description = "Retrieves the content of a single message from the message buffer of a specific module. The message is accessed using an index, and its content is stored in the defined variable.",
    parameters = {"var_name", "module_name", "index"},
    types = {ParamValType.STRING, ParamValType.STRING, ParamValType.LONG},
    kwGroup = "NATT General"
    )
public class BufferGetKw extends NATTKeyword {

    protected String varName;
    protected String moduleName;
    protected Long index;

    private boolean status;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        CopyOnWriteArrayList<INATTMessage> messages = ctx.getMessageBuffer().getMessages(moduleName);
        // pokud je index zaporny zacina od konce pole (zpravy prijate jako posledni)
        // ... posledni index pole = -1, predposledni = -2
        if (index < 0) {
            index += messages.size();
        }

        if (index < 0 || index >= messages.size()) {
            // hodnota neexistuje ... vlozi prazdnou hodnotu
            this.varName = ctx.storeValueToVariable(varName, "");
        } else {
            // hodnota existuje ... vlozi hodnotu na danem indexu
            this.varName = ctx.storeValueToVariable(varName,
                    messages.get(index.intValue()).getMessage());
        }

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

        // index (long) [je vyzadovany]
        val = this.getParameterValue("index", NATTKeyword.ParamValType.LONG,
                true);
        index = (Long) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        if (this.varName != null) {
            ctx.getVariables().remove(this.varName);
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
