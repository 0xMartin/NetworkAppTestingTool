package utb.fai.Keyword.AppControll;

import utb.fai.Core.ExternalProgramRunner;
import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje spustit externi testovanou aplikaci
 */
@NATTAnnotation.Keyword(name = "run_app")
public class RunAppKw extends Keyword {

    protected String command;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.command = VariableProcessor.processVariables(this.command);

        ExternalProgramRunner runner = (ExternalProgramRunner) NATTContext.instance()
                .getModule(ExternalProgramRunner.NAME);
        if (runner == null) {
            return false;
        }
        runner.runExternalProgram(command);

        return true;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(Keyword.DEFAULT_PARAMETER_NAME, Keyword.ParameterValueType.STRING,
                true);
        command = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        ExternalProgramRunner runner = (ExternalProgramRunner) NATTContext.instance()
                .getModule(ExternalProgramRunner.NAME);
        if (runner != null) {
            runner.stopExternalProgram();
        }
    }

}
