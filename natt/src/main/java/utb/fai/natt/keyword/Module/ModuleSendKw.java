package utb.fai.natt.keyword.Module;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.NATTLogger;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje odeslat libovolnou textovou zpravu testovane aplikace skrz libovolny
 * modul
 */
@NATTAnnotation.Keyword(name = "module_send")
public class ModuleSendKw extends NATTKeyword {

    private NATTLogger logger = new NATTLogger(ModuleSendKw.class);

    protected String moduleName;
    protected String message;
    protected Long delay;

    private boolean status;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
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
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", NATTKeyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // message (string) [je vyzadovany]
        val = this.getParameterValue("message", NATTKeyword.ParameterValueType.STRING,
                true);
        message = (String) val.getValue();

        // delay (long) [je vyzadovany]
        val = this.getParameterValue("delay", NATTKeyword.ParameterValueType.LONG,
                false);
        delay = (Long) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String msg;
        if (this.status) {
            msg = String.format(
                    "<font color=\"green\">Message was successfully sent by module <b>'%s'</b>. Message content: <b>%s</b></font>",
                    this.moduleName, this.message.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
        } else {
            msg = String.format("<font color=\"red\">Failed to send message via module <b>'%s'</b>.</font>",
                    this.moduleName);
        }
        return super.getDescription() + "<br>" + msg;
    }

}
