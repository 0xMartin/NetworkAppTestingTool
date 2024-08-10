package utb.fai.natt.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utb.fai.natt.spi.NATTKeyword;

/**
 * Obsahuje staticke funkce pro zpracovani textoveho retezce ze ucelem vlozeni
 * hodnoty promennych za jejich zastupne symboly
 */
public class VariableProcessor {

    /**
     * Zpracuje retezec a pripadne promenne nahradi jejich skutecnym obsahem.
     * Vsechny promenne jsou ulozeny v NATTContext. Promenna zacina timto specialnim
     * znakem #. Priklad promenne: #var-1. Jsou podporovany tyto typy nazvu: name,
     * name-name, name_name, name-1 (slova je mozne spojovat pouze znaky: '-' a '_')
     * 
     * @param input Vstupni retezec
     * @return Vystupni reteze po vlozeni obsahu promennych
     */
    public static String processVariables(String input) {
        if (input == null) {
            return null;
        }
        if(input.isEmpty()) {
            return "";
        }

        Pattern pattern = Pattern.compile("\\$\\$|\\$([a-zA-Z0-9_-]+)");
        Matcher matcher = pattern.matcher(input);

        StringBuffer output = new StringBuffer();
        while (matcher.find()) {
            if (matcher.group(0).equals("$$")) {
                matcher.appendReplacement(output, "\\$");
            } else {
                String varName = matcher.group(1);
                String varValue = NATTContext.instance().getVariable(varName);
                if (varValue != null) {
                    matcher.appendReplacement(output, varValue);
                } else {
                    matcher.appendReplacement(output, "");
                }
            }
        }
        matcher.appendTail(output);

        return output.toString();
    }

    /**
     * Zpracuje retezec a pripadne promenne nahradi jejich skutecnym obsahem.
     * Vsechny promenne jsou ulozeny v NATTContext
     * 
     * @param param Hodnota parametru keywordy (ParameterValue)
     */
    public static void processVariablesInKeywordParameter(NATTKeyword.ParameterValue param) {
        if (param.getType() == NATTKeyword.ParameterValueType.STRING) {
            // jedna se o typ string
            param.setValue((String) VariableProcessor.processVariables((String) param.getValue()));
        } else if (param.getType() == NATTKeyword.ParameterValueType.LIST) {
            // jedna se o typ list<string>
            List<?> list = (List<?>) param.getValue();
            if (!list.isEmpty()) {
                if (list.get(0) instanceof String) {
                    List<String> finalList = new ArrayList<String>();
                    for (Object val : list) {
                        finalList.add((String) VariableProcessor.processVariables((String) val));
                    }
                    param.setValue(finalList);
                }
            }
        }
    }

}
