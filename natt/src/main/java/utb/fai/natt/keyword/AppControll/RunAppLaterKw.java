package utb.fai.natt.keyword.AppControll;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;

import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.module.ExternalProgramRunner;

/**
 * Umoznuje spustit externi testovanou aplikaci
 */
@NATTAnnotation.Keyword(name = "run_app_later", description = "Launches the application with a time delay. This operation is asynchronous. Again, only one external application can run at a time.", parameters = {
        "command", "delay",
        "name" }, types = { ParamValType.STRING, ParamValType.LONG, ParamValType.STRING }, kwGroup = "NATT AppControll")
public class RunAppLaterKw extends NATTKeyword {

    private NATTLogger logger = new NATTLogger(RunAppLaterKw.class);

    protected String command;
    protected Long delay;
    protected String moduleName;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.command = VariableProcessor.processVariables(this.command);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        if (this.delay <= 0) {
            throw new InternalErrorException("Delay must be higher than 0 ms!");
        }

        ExternalProgramRunner runner = new ExternalProgramRunner(
                this.moduleName == null ? "default" : this.moduleName, this.command);

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
            try {
                runner.runModule();
            } catch (Exception e) {
                logger.warning("Failed to run application later: " + e.getMessage());
            }
        });
        thread.start();

        return true;
    }

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // command (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("command", NATTKeyword.ParamValType.STRING,
                true);
        command = (String) val.getValue();

        // delay (string) [je vyzadovany]
        val = this.getParameterValue("delay", NATTKeyword.ParamValType.LONG,
                true);
        delay = (Long) val.getValue();

        // (string) [neni vyzadovany]
        val = this.getParameterValue("name", NATTKeyword.ParamValType.STRING,
                false);
        moduleName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        ExternalProgramRunner runner = (ExternalProgramRunner) NATTContext.instance()
                .getModule(this.moduleName == null ? "default" : this.moduleName);
        if (runner != null) {
            runner.terminateModule();
            NATTContext.instance().removeModule(this.moduleName == null ? "default" : this.moduleName);
        }
    }

}
