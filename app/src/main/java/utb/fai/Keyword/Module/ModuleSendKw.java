package utb.fai.Keyword.Module;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.NATTModule;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje odeslat libovolnou textovou zpravu testovane aplikace skrz libovolny
 * modul
 */
@NATTAnnotation.Keyword(name = "module_send")
public class ModuleSendKw extends Keyword {

    private NATTLogger logger = new NATTLogger(ModuleSendKw.class);

    protected String moduleName;
    protected String message;
    protected Long delay;

    private boolean status;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        this.status = false;

        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.message = VariableProcessor.processVariables(this.message);

        if (delay == null) {
            // okamzite odeslani zpravy
            NATTModule module = NATTContext.instance().getModule(moduleName);
            if (module == null) {
                return false;
            }
            if (!module.isRunning()) {
                return false;
            }
            this.status = module.sendMessage(message);
        } else {
            // odeslani se zpozdenim
            Thread thread = new Thread(() -> {
                NATTModule module = NATTContext.instance().getModule(moduleName);
                if (module == null) {
                    return;
                }
                if (!module.isRunning()) {
                    return;
                }
                try {
                    module.sendMessage(message);
                } catch (InternalErrorException e) {
                    logger.warning("Failed to send message. Error: " + e.getMessage());  
                }
            });
            thread.start();
            this.status = true;
        }

        return this.status;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // message (string) [je vyzadovany]
        val = this.getParameterValue("message", Keyword.ParameterValueType.STRING,
                true);
        message = (String) val.getValue();

        // delay (long) [je vyzadovany]
        val = this.getParameterValue("delay", Keyword.ParameterValueType.LONG,
                false);
        delay = (Long) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String msg;
        if (this.status) {
            msg = String.format(
                    "<font color=\"green\">Message was successfully sent by module <b>'%s'</b>. Message content: <br><b>%s</b></font>",
                    this.moduleName, this.message.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        } else {
            msg = String.format("<font color=\"red\">Failed to send message via module <b>'%s'</b>.</font>",
                    this.moduleName);
        }
        return super.getDescription() + "<br>" + msg;
    }

}
