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

/**
 * Umozuje odstranit vsechny filtri akci pro dany modul
 */
@NATTAnnotation.Keyword(
    name = "clear_filter_actions",
    description = "Removes all action filters for a specific module.",
    parameters = { "module_name" },
    types = { ParamValType.STRING }
    )
public class ClearFilterActionsKw extends NATTKeyword {

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
            return false;
        }

        module.getActionFilterList().clear();

        return true;
    }

    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(new String[] { "module_name", 
            NATTKeyword.DEFAULT_PARAMETER_NAME }, NATTKeyword.ParamValType.STRING, true);
        moduleName = (String) val.getValue();

        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
    }

}
