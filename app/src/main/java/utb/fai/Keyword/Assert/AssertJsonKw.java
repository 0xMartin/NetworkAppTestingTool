package utb.fai.Keyword.Assert;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTLogger;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje definovat trzeni ze json objekt v promenne je stejny jako ten
 * ocekavany
 */
@NATTAnnotation.Keyword(name = "assert_json")
public class AssertJsonKw extends Keyword {

    private NATTLogger logger = new NATTLogger(AssertJsonKw.class);

    protected String varName;
    protected String expected;
    protected Boolean exactMode;
    protected Boolean result;

    // finalni stav tvrzeni (bude pouzito pri informativni zprave v reportu)
    private boolean finalStatus;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        this.finalStatus = false;

        // zpracovani promennych v retezci
        this.varName = VariableProcessor.processVariables(this.varName);
        this.expected = VariableProcessor.processVariables(this.expected);

        if (result == null) {
            this.result = true;
        }
        if (exactMode == null) {
            exactMode = false;
        }

        String varValue = NATTContext.instance().getVariable(varName);
        if (varValue == null) {
            logger.warning(String.format("Assertion failed. Variable '%s' not found!", varName));
            return false;
        }

        // porovnani json
        try {
            // hodnota ocekavana
            ObjectMapper mapper = new ObjectMapper();
            JsonNode expectedJson = mapper.readTree(expected);
            // hodnota promenne
            JsonNode actualJson = mapper.readTree(varValue);

            // porovnani JSON objektu
            if (this.exactMode) {
                if (expectedJson.equals(actualJson)) {
                    this.finalStatus = true;
                }
            } else {
                if (compareCommonProperties(expectedJson, actualJson)) {
                    this.finalStatus = true;
                }
            }
        } catch (JsonProcessingException e) {
            logger.warning("Error parsing JSON: " + e.getMessage());
        }

        // normalizace na podle ocekavaneho vysledku
        this.finalStatus = (this.finalStatus == this.result);

        if (!this.finalStatus) {
            logger.warning(String.format(
                    "Assertion failed. %s was expected as the result. Condition: (Expected JSON '%s' must be same as the JSON in the variable '%s')",
                    this.result ? "True" : "False", this.expected, varValue));
        }

        return this.finalStatus;
    }

    /**
     * Porovnava jen parametry, ktere jsou v ocekavanem "expected" json objektu.
     * Pokud tento parametr neni obsazen v aktualnim objektu, je to povazovano za
     * neshodu.
     * 
     * @param expected Ocekavany json
     * @param actual   Aktualni json (promenna)
     * @return True v pripade shody
     */
    private boolean compareCommonProperties(JsonNode expected, JsonNode actual) {
        Iterator<String> expectedKeys = expected.fieldNames();

        Set<String> commonKeys = new HashSet<>();

        while (expectedKeys.hasNext()) {
            String key = expectedKeys.next();
            if (actual.has(key)) {
                commonKeys.add(key);
            } else {
                return false;
            }
        }

        for (String key : commonKeys) {
            if (expected.get(key).isObject()) {
                if (!compareCommonProperties(expected.get(key), actual.get(key))) {
                    return false;
                }
            } else {
                if (!expected.get(key).equals(actual.get(key))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    protected void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // var_name (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("var_name", Keyword.ParameterValueType.STRING,
                true);
        varName = (String) val.getValue();

        // expected (double) [je vyzadovany]
        val = this.getParameterValue("expected", Keyword.ParameterValueType.STRING,
                true);
        expected = (String) val.getValue();

        // exact_mode (boolean) [neni vyzadovany]
        val = this.getParameterValue("exact_mode", Keyword.ParameterValueType.BOOLEAN,
                false);
        exactMode = (Boolean) val.getValue();

        // result (boolean) [neni vyzadovany]
        val = this.getParameterValue("result", Keyword.ParameterValueType.BOOLEAN,
                false);
        result = (Boolean) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
    }

    @Override
    public String getDescription() {
        String varValue = NATTContext.instance().getVariable(varName);
        if (varValue == null) {
            varValue = "";
        }
        String data = varValue.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

        String message;
        if (this.finalStatus) {
            message = String.format(
                    "<font color=\"green\">Assertion succeeded. <b>%s</b> was expected as the result. Condition: (Expected JSON <b>'%s'</b> must be same as the JSON in the variable <b>'%s'</b>)</font>",
                    this.result ? "True" : "False", this.expected, data);
        } else {
            message = String.format(
                    "<font color=\"red\">Assertion failed. <b>%s</b> was expected as the result. Condition: (Expected JSON <b>'%s'</b> must be same as the JSON in the variable <b>'%s'</b>)</font>",
                    this.result ? "True" : "False", this.expected, data);
        }

        return super.getDescription() + "<br>" + message;
    }

}
