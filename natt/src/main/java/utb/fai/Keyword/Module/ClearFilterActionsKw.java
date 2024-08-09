package utb.fai.Keyword.Module;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTModule;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;

/**
 * Umozuje odstranit vsechny filtri akci pro dany modul
 */
@NATTAnnotation.Keyword(name = "clear_filter_actions")
public class ClearFilterActionsKw extends Keyword {

    protected String moduleName;

    @Override
    public boolean execute()
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
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(Keyword.DEFAULT_PARAMETER_NAME, Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

}
