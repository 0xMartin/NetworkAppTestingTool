package utb.fai.Keyword.General;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Exception.TestedAppFailedToRunException;

/**
 * Umoznuje ziskat urcity parametr z textu dane promenne, ktera je ve formatu
 * json
 */
@NATTAnnotation.Keyword(name = "json_get")
public class JsonGetKw extends Keyword {

    protected String toVar;
    protected String fromVar;
    protected String paramName;

    private boolean status;

    @Override
    public boolean execute()
            throws InternalErrorException, TestedAppFailedToRunException, NonUniqueModuleNamesException {
        status = false;

        // zpracovani promennych v retezci
        this.toVar = VariableProcessor.processVariables(this.toVar);
        this.fromVar = VariableProcessor.processVariables(this.fromVar);
        this.paramName = VariableProcessor.processVariables(this.paramName);

        // ziska path k promenne
        String[] paramPath = this.paramName.split(":");
        if (paramPath.length < 1) {
            this.toVar = NATTContext.instance().storeValueToVariable(this.toVar, "");
            if (this.toVar != null) {
                status = true;
            }
            return this.toVar != null;
        }

        // ziska zdrojovy text
        String jsonText = NATTContext.instance().getVariable(this.fromVar);
        if (jsonText == null) {
            this.toVar = NATTContext.instance().storeValueToVariable(this.toVar, "");
            if (this.toVar != null) {
                status = true;
            }
            return this.toVar != null;
        }

        // pro ukladani mezi vysledku
        String jsonBuffer = jsonText;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<Object> mapType = new TypeReference<Object>() {
            };

            for (String param : paramPath) {
                if (param.isEmpty() || jsonBuffer.isEmpty())
                    break;

                Object jsonMap = objectMapper.readValue(jsonBuffer, mapType);
                if (jsonMap instanceof List<?>) {
                    List<?> jsonList = (List<?>) jsonMap;
                    int index = Integer.parseInt(param);
                    if (index >= 0 && index < jsonList.size()) {
                        Object paramValue = jsonList.get(index);
                        if (paramValue != null) {
                            if (paramValue instanceof Map<?, ?> || paramValue instanceof List<?>) {
                                String jsonValue = objectMapper.writeValueAsString(paramValue);
                                jsonBuffer = jsonValue;
                            } else {
                                jsonBuffer = paramValue.toString();
                            }
                        } else {
                            jsonBuffer = "";
                        }
                    } else {
                        jsonBuffer = "";
                    }
                } else if (jsonMap instanceof Map<?, ?>) {
                    Map<?, ?> jsonObject = (Map<?, ?>) jsonMap;
                    if (jsonObject.containsKey(param)) {
                        Object paramValue = jsonObject.get(param);
                        if (paramValue != null) {
                            if (paramValue instanceof Map<?, ?> || paramValue instanceof List<?>) {
                                String jsonValue = objectMapper.writeValueAsString(paramValue);
                                jsonBuffer = jsonValue;
                            } else {
                                jsonBuffer = paramValue.toString();
                            }
                        } else {
                            jsonBuffer = "";
                        }
                    } else {
                        jsonBuffer = "";
                    }
                }
            }

            // uplny vysledek ulozi do promenne
            this.toVar = NATTContext.instance().storeValueToVariable(this.toVar, jsonBuffer);
            if (this.toVar != null) {
                status = true;
            }
            return this.toVar != null;
        } catch (Exception e) {
            this.toVar = NATTContext.instance().storeValueToVariable(this.toVar, "");
            if (this.toVar != null) {
                status = true;
            }
            return this.toVar != null;
        }
    }

    @Override
    public void keywordInit() throws InvalidSyntaxInConfigurationException {
        /// PARAMETRY
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
        // to_var (string) [je vyzadovany]
        ParameterValue val = this.getParameterValue("to_var", Keyword.ParameterValueType.STRING,
                true);
        toVar = (String) val.getValue();

        // from_var (string) [je vyzadovany]
        val = this.getParameterValue("from_var", Keyword.ParameterValueType.STRING,
                true);
        fromVar = (String) val.getValue();

        // paramName (string) [je vyzadovany]
        val = this.getParameterValue("param_name", Keyword.ParameterValueType.STRING,
                true);
        paramName = (String) val.getValue();
        /// //////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public void deleteAction() throws InternalErrorException {
        if (this.toVar != null) {
            NATTContext.instance().getVariables().remove(this.toVar);
        }
    }

    @Override
    public String getDescription() {
        if (this.toVar == null || status == false) {
            return super.getDescription() + "<br><font color=\"red\">Failed to store value to variable.</font>";
        }
        String data = NATTContext.instance().getVariable(this.toVar);
        data = data.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        String message = String.format("The following json content has been stored in a variable named <b>[%s]</b>: <b>'%s'</b>",
                this.toVar, data);
        return super.getDescription() + "<br><font color=\"green\">" + message + "</font>";
    }

}
