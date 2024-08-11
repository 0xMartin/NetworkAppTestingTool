package utb.fai.natt.keyword.General;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje odstrani vsechny zpravy v bufferu pro zvoleny modul nebo vsechny
 * moduly
 */
@NATTAnnotation.Keyword(name = "clear_buffer")
public class ClearBufferKw extends NATTKeyword {

    // obsahuje jmeno modulum, pro ktery bude buffer mazan. * je symbol zastupujici
    // vsechny moduly
    protected String moduleName;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
                
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
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(NATTKeyword.DEFAULT_PARAMETER_NAME, NATTKeyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
    }

}