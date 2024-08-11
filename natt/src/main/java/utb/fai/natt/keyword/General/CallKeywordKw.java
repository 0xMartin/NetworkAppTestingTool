package utb.fai.natt.keyword.General;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;

import utb.fai.natt.core.NATTContext;
import utb.fai.natt.spi.NATTLogger;
import utb.fai.natt.core.VariableProcessor;

/**
 * Umoznuje zavolat vlastni keyword
 */
@NATTAnnotation.Keyword(name = "call_keyword")
public class CallKeywordKw extends NATTKeyword {

    protected NATTLogger logger = new NATTLogger(CallKeywordKw.class);

    protected String kwName;

    private CustomKeywordKw kw;
    private String log;
    private boolean error;

    @Override
    public boolean execute(INATTContext ctx)
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
                    ParameterValue paramValue = this.getParameterValue(paramName, NATTKeyword.ParameterValueType.STRING,
                            false);
                    NATTContext.instance().storeValueToVariable(paramName, paramValue.getValue().toString());
                } catch (Exception e) {
                    this.logger.warning("Failed to set parameter " + paramName + " for keyword " + this.kwName);
                }
            }
        }

        // vykona keywordu
        for (int index = 0; index < this.kw.steps.size(); ++index) {
            NATTKeyword step = this.kw.steps.get(index);
            if (step.isIgnored()) {
                continue;
            }
            if(step instanceof CallKeywordKw) {
                this.logger.warning("Nested call of keyword is not allowed!");
                continue;
            }
            if (!step.execute(ctx)) {
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
    public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue(new String[] { "name", NATTKeyword.DEFAULT_PARAMETER_NAME },
                NATTKeyword.ParameterValueType.STRING,
                true);
        kwName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction(INATTContext ctx) throws InternalErrorException {
        if (this.kw != null) {
            for (NATTKeyword step : this.kw.steps) {
                step.deleteAction(ctx);
            }
        }
    }

    @Override
    public String getDescription() {
        return super.getDescription() + "<br><b>Steps:</b><br>" + this.log;
    }

}