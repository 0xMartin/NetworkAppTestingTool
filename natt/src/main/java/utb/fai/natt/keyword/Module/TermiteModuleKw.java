package utb.fai.natt.keyword.Module;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;

@NATTAnnotation.Keyword(
    name = "termite_module",
    description = "Terminates a running module that is no longer needed.",
    parameters = { "module_name" },
    types = { ParamValType.STRING }
    )
public class TermiteModuleKw extends NATTKeyword {

    protected String moduleName;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        NATTModule module = NATTContext.instance().getModule(moduleName);
        if (module == null) {
            return false;
        }
        if (!module.isRunning()) {
            return true;
        }

        return module.terminateModule();
    }

    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(new String[] { "module_name", NATTKeyword.DEFAULT_PARAMETER_NAME}, 
            NATTKeyword.ParamValType.STRING, true);
        moduleName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
    }

}
