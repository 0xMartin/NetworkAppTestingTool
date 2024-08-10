package utb.fai.natt.spi;

import java.util.Objects;

public class NATTAssert {

    /**
     * Verifies whether the content of the provided parameter "varValue" matches
     * the expected value in the parameter "expectedText".
     * 
     * @param varValue      The value of the variable being tested.
     * @param expectedText  The expected value of the variable.
     * @param mode          The mode for comparing the variable's value with the expected value
     *                      (equals, contains, startswith, endswith). Can be null (default is equals).
     * @param caseSensitive If true, the comparison is case-sensitive.
     * @return True if the condition for data matching between "varValue" and "expectedText" is met.
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
     * Verifies whether the mathematical condition between the values in the parameters "varValue"
     * and "expectedValue" holds true.
     * 
     * @param varValue      The value of the variable being tested.
     * @param expectedValue The expected value of the variable.
     * @param mode          The mode for comparing the variable's value with the expected value (=, >, <).
     *                      The mode is always interpreted in this context: [varValue @ expectedValue].
     * @return True if the condition is met.
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
     * Verifies whether the numerical value in the parameter "varValue" is equal to
     * the numerical value in the parameter "expectedNumber" within the given "tolerance".
     * 
     * @param varValue       The value of the variable being tested.
     * @param expectedNumber The expected value of the variable.
     * @param tolerance      Tolerance is an integer from 0 to 100 representing the percentage of
     *                       maximum allowable deviation. A value of 0 means the numbers must be exactly
     *                       identical, while 100 means the numbers can differ by up to the absolute value
     *                       in the parameter "expectedNumber".
     * @return True if the condition is met.
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