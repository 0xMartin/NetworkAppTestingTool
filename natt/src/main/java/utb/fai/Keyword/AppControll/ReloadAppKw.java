package utb.fai.Keyword.AppControll;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Module.ExternalProgramRunner;

/**
 * Umoznuje znovu spustit externi testovanou aplikaci
 */
@NATTAnnotation.Keyword(name = "reload_app")
public class ReloadAppKw extends Keyword {

    protected String command;
    protected String moduleName;

    @Override
    public boolean execute()
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
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(new String[] { "command", Keyword.DEFAULT_PARAMETER_NAME },
                Keyword.ParameterValueType.STRING,
                true);
        command = (String) val.getValue();

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
