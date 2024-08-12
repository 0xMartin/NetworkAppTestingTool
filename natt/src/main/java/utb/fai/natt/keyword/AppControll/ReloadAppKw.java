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
 * Umoznuje znovu spustit externi testovanou aplikaci
 */
@NATTAnnotation.Keyword(
    name = "reload_app",
    description = "Stops the currently running application and launches the new application.",
    parameters = {"command", "name"},
    types = {ParamValType.STRING, ParamValType.STRING},
    kwGroup = "NATT AppControll"
    )
public class ReloadAppKw extends NATTKeyword {

    protected String command;
    protected String moduleName;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.command = VariableProcessor.processVariables(this.command);
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        ExternalProgramRunner runner = (ExternalProgramRunner) NATTContext.instance()
                .getModule(this.moduleName == null ? "default" : this.moduleName);
        if (runner == null) {
            return false;
        }
        runner.terminateModule();
        runner.runModule();
        return true;
    }

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
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
            NATTContext.instance().removeModule(this.moduleName == null ? "default" : this.moduleName);
        }
    }

}
