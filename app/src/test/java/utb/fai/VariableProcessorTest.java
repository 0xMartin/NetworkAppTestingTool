package utb.fai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;

import utb.fai.Core.Keyword.ParameterValue;
import utb.fai.Core.Keyword.ParameterValueType;
import utb.fai.Core.NATTContext;
import utb.fai.Core.VariableProcessor;

public class VariableProcessorTest {

    @After
    public void clean() {
        NATTContext.instance().getVariables().clear();   
    }

    @Test
    public void testProcessVariables_VariableExists() {
        NATTContext.instance().storeValueToVariable("name-2", "John");
        String input = "Hello, $name-2! How are you?";
        String expectedOutput = "Hello, John! How are you?";
        String result = VariableProcessor.processVariables(input);
        assertEquals(expectedOutput, result);
    }

    @Test
    public void testProcessVariables_VariableNotExists() {
        String input = "Hello, $name! How are you?";
        String expectedOutput = "Hello, ! How are you?";
        String result = VariableProcessor.processVariables(input);
        assertEquals(expectedOutput, result);
    }

    @Test
    public void testProcessVariables_DoubleDollarSign() {
        String input = "Hello, $name! How are you? $$var1";
        String expectedOutput = "Hello, John! How are you? $var1";
        NATTContext.instance().storeValueToVariable("name", "John");
        String result = VariableProcessor.processVariables(input);
        assertEquals(expectedOutput, result);
    }

    @Test
    public void testProcessVariables_MultipleVariables() {
        NATTContext.instance().storeValueToVariable("name", "John");
        NATTContext.instance().storeValueToVariable("age", "30");
        String input = "Hello, $name! You are $age years old.";
        String expectedOutput = "Hello, John! You are 30 years old.";
        String result = VariableProcessor.processVariables(input);
        assertEquals(expectedOutput, result);
    }

    @Test
    public void testProcessVariablesInKeywordParameter_StringParameter() {
        ParameterValue param = new ParameterValue("$name", ParameterValueType.STRING);
        NATTContext.instance().storeValueToVariable("name", "John");
        VariableProcessor.processVariablesInKeywordParameter(param);
        assertEquals("John", param.getValue());
    }

    @Test
    public void testProcessVariablesInKeywordParameter_ListParameter() {
        List<String> list = Arrays.asList("$name", "$age");
        ParameterValue param = new ParameterValue(list, ParameterValueType.LIST);
        NATTContext.instance().storeValueToVariable("name", "John");
        NATTContext.instance().storeValueToVariable("age", "30");
        VariableProcessor.processVariablesInKeywordParameter(param);
        assertEquals(Arrays.asList("John", "30"), param.getValue());
    }

    @Test
    public void testProcessVariablesInKeywordParameter_EmptyListParameter() {
        List<String> list = new ArrayList<>();
        ParameterValue param = new ParameterValue(list, ParameterValueType.LIST);
        VariableProcessor.processVariablesInKeywordParameter(param);
        assertTrue(((List<?>) param.getValue()).isEmpty());
    }

}
