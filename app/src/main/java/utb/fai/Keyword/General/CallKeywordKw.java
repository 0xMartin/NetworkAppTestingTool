package utb.fai.Keyword.General;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;

/**
 * Umoznuje zavolat vlastni keyword
 */
@NATTAnnotation.Keyword(name = "call_keyword")
public class CallKeywordKw extends Keyword {

    protected NATTLogger logger = new NATTLogger(CallKeywordKw.class);

    protected String kwName;

    private CustomKeywordKw kw;
    private String log;
    private boolean error;

    @Override
    public boolean execute()
            throws InternalErrorException, NonUniqueModuleNamesException {
        this.log = "";

        // zpracovani promennych v retezci
        this.kwName = VariableProcessor.processVariables(this.kwName);

        // nacte pozadovanou keywordu
        this.kw = NATTContext.instance().getCustomKeywords().get(this.kwName);
        if (this.kw == null) {
            this.logger.warning("Keyword " + this.kwName + " is not defined");
            this.log = "<font color=\"red\">Keyword '" + this.kwName + "'' is not defined</font>";
            this.error = true;
            return false;
        }

        // ulozi aktualni stav promennych
        if (this.kw.params != null) {
            NATTContext.instance().saveVariablesState();
        }

        // predani vstupnich parametru
        if (this.kw.params != null) {
            for (String paramName : this.kw.params) {
                try {
                    ParameterValue paramValue = this.getParameterValue(paramName, Keyword.ParameterValueType.STRING,
                            false);
                    NATTContext.instance().storeValueToVariable(paramName, paramValue.getValue().toString());
                } catch (Exception e) {
                    this.logger.warning("Failed to set parameter " + paramName + " for keyword " + this.kwName);
                }
            }
        }

        // vykona keywordu
        for (int index = 0; index < this.kw.steps.size(); ++index) {
            Keyword step = this.kw.steps.get(index);
            if (step.isIgnored()) {
                continue;
            }
            if (!step.execute()) {
                this.error = true;
            }
            this.log += "[" + index + "]: " + step.getDescription() + "<br>";
        }

        // obnovy stav promennych
        if (this.kw.params != null) {
            NATTContext.instance().restoreVariablesState();
        }

        return !this.error;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(new String[] { "name", Keyword.DEFAULT_PARAMETER_NAME },
                Keyword.ParameterValueType.STRING,
                true);
        kwName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        if (this.kw != null) {
            for (Keyword step : this.kw.steps) {
                step.deleteAction();
            }
        }
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "<br><b>Steps:</b><br>" + this.log;
    }

}
