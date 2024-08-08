package utb.fai.Keyword.AppControll;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Module.ExternalProgramRunner;

/**
 * Umoznuje spustit externi testovanou aplikaci
 */
@NATTAnnotation.Keyword(name = "run_app_later")
public class RunAppLaterKw extends Keyword {

    private NATTLogger logger = new NATTLogger(RunAppLaterKw.class);

    protected String command;
    protected Long delay;
    protected String moduleName;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.command = VariableProcessor.processVariables(this.command);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        if (this.delay <= 0) {
            throw new InternalErrorException("Delay must be higher than 0 ms!");
        }

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
            }
            try {
                ExternalProgramRunner runner = new ExternalProgramRunner(
                        this.moduleName == null ? "default" : this.moduleName, this.command);
                runner.runModule();
            } catch (Exception e) {
                logger.warning("Failed to run application later: " + e.getMessage());
            }
        });
        thread.start();

        return true;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // command (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("command", Keyword.ParameterValueType.STRING,
                true);
        command = (String) val.getValue();

        // delay (string) [je vyzadovany]
        val = this.getParameterValue("delay", Keyword.ParameterValueType.LONG,
                true);
        delay = (Long) val.getValue();

        // (string) [neni vyzadovany]
        val = this.getParameterValue("name", Keyword.ParameterValueType.STRING,
                false);
        moduleName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        ExternalProgramRunner runner = (ExternalProgramRunner) NATTContext.instance()
                .getModule(this.moduleName == null ? "default" : this.moduleName);
        if (runner != null) {
            runner.terminateModule();
        }
    }

}
