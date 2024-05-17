package utb.fai.Keyword.Module;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTModule;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umozuje vytvorit filtr pro generovani akci dany modulem. Tyto akce jsou
 * napriklad vyuzivany pri keyword ktere cekaji nez tato dana akce nastane.
 */
@NATTAnnotation.Keyword(name = "create_filter_action")
public class CreateFilterActionKw extends Keyword {

    protected String moduleName;
    protected String text;
    protected String tag;
    protected String mode;
    protected Boolean caseSensitive;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
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
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("name", Keyword.ParameterValueType.STRING,
                true);
        moduleName = (String) val.getValue();

        // text (string) [je vyzadovany]
        val = this.getParameterValue("text", Keyword.ParameterValueType.STRING,
                true);
        text = (String) val.getValue();

        // tag (string) [neni vyzadovany]
        val = this.getParameterValue("tag", Keyword.ParameterValueType.STRING,
                false);
        tag = (String) val.getValue();

        // mode (string) [neni vyzadovany]
        val = this.getParameterValue("mode", Keyword.ParameterValueType.STRING,
                false);
        mode = (String) val.getValue();

        // case_sensitive (string) [neni vyzadovany]
        val = this.getParameterValue("case_sensitive", Keyword.ParameterValueType.BOOLEAN,
                false);
        caseSensitive = (Boolean) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        NATTModule module = NATTContext.instance().getModule(moduleName);

        if (module != null) {
            module.getActionFilterList().clear();
        }
    }

}
