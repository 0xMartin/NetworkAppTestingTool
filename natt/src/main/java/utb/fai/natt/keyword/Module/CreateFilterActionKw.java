package utb.fai.natt.keyword.Module;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.NATTModule;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umozuje vytvorit filtr pro generovani akci dany modulem. Tyto akce jsou
 * napriklad vyuzivany pri keyword ktere cekaji nez tato dana akce nastane.
 */
@NATTAnnotation.Keyword(name = "create_filter_action")
public class CreateFilterActionKw extends NATTKeyword {

    protected String moduleName;
    protected String text;
    protected String tag;
    protected String mode;
    protected Boolean caseSensitive;

    @Override
    public boolean execute(INATTContext ctx)
            throws InternalErrorException, NonUniqueModuleNamesException {
        // zpracovani promennych v retezci
        this.moduleName = VariableProcessor.processVariables(this.moduleName);
        this.text = VariableProcessor.processVariables(this.text);
        this.tag = VariableProcessor.processVariables(this.tag);
        this.mode = VariableProcessor.processVariables(this.mode);

        NATTModule module = NATTContext.instance().getModule(moduleName);

        if (module == null) {
            return false;
        }
        if (!module.isRunning()) {
            return false;
        }

        module.getActionFilterList().add(new NATTModule.MessageFilter(
                this.text, this.tag, this.mode, this.caseSensitive));

        return true;
    }

    @Override
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", NATTKeyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // text (string) [je vyzadovany]
        val = this.getParameterValue("text", NATTKeyword.ParameterValueType.STRING,
                true);
        text = (String) val.getValue();

        // tag (string) [neni vyzadovany]
        val = this.getParameterValue("tag", NATTKeyword.ParameterValueType.STRING,
                false);
        tag = (String) val.getValue();

        // mode (string) [neni vyzadovany]
        val = this.getParameterValue("mode", NATTKeyword.ParameterValueType.STRING,
                false);
        mode = (String) val.getValue();

        // case_sensitive (string) [neni vyzadovany]
        val = this.getParameterValue("case_sensitive", NATTKeyword.ParameterValueType.BOOLEAN,
                false);
        caseSensitive = (Boolean) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        NATTModule module = NATTContext.instance().getModule(moduleName);

        if (module != null) {
            module.getActionFilterList().clear();
        }
    }

}