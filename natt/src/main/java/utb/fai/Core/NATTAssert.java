package utb.fai.Core;

import java.util.Objects;

public class NATTAssert {

    /**
     * Overi zda obsah predaneho parametru "varValue" odpovida ocekavane hodnote
     * parametru "expectedText"
     * 
     * @param varValue      Hodnota testovane promenne
     * @param expectedText  Ocekavana hodnota promenne
     * @param mode          Mod porovnavani hodnoty promenne a ocekavana hodnoty
     *                      (equals, contains, startswith, endswith). Muze byt null
     *                      (defaultni hodnota je equals)
     * @param caseSensitive Pokud je true bude bran i ohled na velikosti pismen
     * @return True v pripade pokud je splnena podminka o shodnosti dat v parametru
     *         "varValue" a "expectedText"
     */
    public static boolean assertCondition(String varValue, String expectedText, String mode, boolean caseSensitive) {
        if(varValue == null) {
            varValue = "";
        }
        if(expectedText == null) {
            expectedText = "";
        }
        if (mode == null) {
            mode = "equals";
        }
        if (varValue.isEmpty() && expectedText.isEmpty()) {
            return true;
        }

        if (!caseSensitive) {
            varValue = varValue.toLowerCase();
            expectedText = expectedText.toLowerCase();
        }

        switch (mode.toLowerCase().trim()) {
            case "equals":
                return Objects.equals(varValue, expectedText);
            case "contains":
                return varValue.contains(expectedText);
            case "startswith":
                return varValue.startsWith(expectedText);
            case "endswith":
                return varValue.endsWith(expectedText);
            default:
                return Objects.equals(varValue, expectedText);
        }
    }

    /**
     * Overi zda plati matematicka podminka o hodnote cisel v parametrech "varValue"
     * a "expectedText"
     * 
     * @param varValue      Hodnota testovane promenne
     * @param expectedValue Ocekavana hodnota promenne
     * @param mode          Mod porovnavani hodnoty promenne a ocekavana hodnoty (=,
     *                      >, <). Mode je bran vzdy v tomto kontextu: [varValue @
     *                      expectedValue]
     * @return True v pripade pokud je splnena podminka
     */
    public static boolean assertNumberCondition(String varValue, double expectedNumber, String mode) {
        if(varValue == null) {
            varValue = "";
        }

        double varNumber = 0;
        try {
            varNumber = Double.parseDouble(varValue);
        } catch (NumberFormatException ex) {
            return false;
        }

        switch (mode) {
            case "=":
                return varNumber == expectedNumber;
            case ">":
                return varNumber > expectedNumber;
            case "<":
                return varNumber < expectedNumber;
            default:
                return varNumber == expectedNumber;
        }
    }

    /**
     * Overi zda cislna hodnota v parametru "varValue" se rovna ciselne hodnote v
     * parametru "expectedNumber" s toleranci "tolerance".
     * 
     * @param varValue       Hodnota testovane promenne
     * @param expectedNumber Ocekavana hodnota promenne
     * @param tolerance      Tolerance je cele cislo od 0 do 100 reprezentujici
     *                       pocet % maximalni tolerance odchylky. Hodnota 0 znamena
     *                       ze cisla musi byt uplne shodna, 100 znamena ze cisla se
     *                       mohou maximalne lisit od absolutni hodnotu cisla v
     *                       parametru "expectedNumber".
     * @return True v pripade splneni podminky
     */
    public static boolean assertNumberEqualWithTolerance(String varValue, double expectedNumber, double tolerance) {
        if(varValue == null) {
            varValue = "";
        }
        
        double varNumber;
        try {
            varNumber = Double.parseDouble(varValue);
        } catch (NumberFormatException ex) {
            return false;
        }

        double maxDeviation = Math.abs(expectedNumber * tolerance / 100.0);

        return Math.abs(varNumber - expectedNumber) <= maxDeviation;
    }

}
