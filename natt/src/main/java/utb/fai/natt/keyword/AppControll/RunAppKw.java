package utb.fai.natt.keyword.AppControll;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;
import utb.fai.natt.module.ExternalProgramRunner;

/**
 * Umoznuje spustit externi testovanou aplikaci
 */
@NATTAnnotation.Keyword(
    name = "run_app",
    description = "Launches the application. At any given time, only one external application can run! It allows the definition of arguments to be passed to the application upon its launch.",
    parameters = {"command", "name"},
    types = {ParamValType.STRING, ParamValType.STRING}
    )
public class RunAppKw extends NATTKeyword {

    protected String command;
    protected String moduleName;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.command = VariableProcessor.processVariables(this.command);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        ExternalProgramRunner runner = new ExternalProgramRunner(
                this.moduleName == null ? "default" : this.moduleName, this.command);
        runner.runModule();

        return true;
    }

    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(new String[] { "command", NATTKeyword.DEFAULT_PARAMETER_NAME },
                NATTKeyword.ParamValType.STRING,
                true);
        command = (String) val.getValue();

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
        }
    }

}
