package utb.fai.Keyword.General;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje odstrani vsechny zpravy v bufferu pro zvoleny modul nebo vsechny
 * moduly
 */
@NATTAnnotation.Keyword(name = "clear_buffer")
public class ClearBufferKw extends Keyword {

    // obsahuje jmeno modulum, pro ktery bude buffer mazan. * je symbol zastupujici
    // vsechny moduly
    protected String moduleName;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
                
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);

        if (moduleName.equals("*")) {
            NATTContext.instance().getMessageBuffer().clearAll();
            return true;
        } else {
            return NATTContext.instance().getMessageBuffer().clearOneMessageBuffer(moduleName);
        }
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(Keyword.DEFAULT_PARAMETER_NAME, Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

}
