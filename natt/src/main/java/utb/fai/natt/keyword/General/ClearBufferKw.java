package utb.fai.natt.keyword.General;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTKeyword.ParamValType;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje odstrani vsechny zpravy v bufferu pro zvoleny modul nebo vsechny
 * moduly
 */
@NATTAnnotation.Keyword(
    name = "clear_buffer", 
    description = "Clears the content of the message buffer. It's possible to clear the buffer content for all modules or for a specific one.", 
    parameters = {"module_name" }, 
    types = { ParamValType.STRING },
    kwGroup = "NATT General"
    )
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
            ctx.getMessageBuffer().clearAll();
            return true;
        } else {
            return ctx.getMessageBuffer().clearOneMessageBuffer(moduleName);
        }
    }

    @Override
    public void init(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(new String[] { "module_name", NATTKeyword.DEFAULT_PARAMETER_NAME },
                NATTKeyword.ParamValType.STRING,
                true);
        moduleName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
    }

}
