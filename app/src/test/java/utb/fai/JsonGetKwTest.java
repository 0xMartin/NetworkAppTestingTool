package utb.fai;

import org.junit.Before;
import org.junit.Test;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTContext;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueModuleNamesException;
import utb.fai.Keyword.General.JsonGetKw;

import static org.junit.Assert.*;

public class JsonGetKwTest {

        private JsonGetKw jsonGetKw;

        @Before
        public void setUp() throws InvalidSyntaxInConfigurationException {
                jsonGetKw = new JsonGetKw();
                jsonGetKw.getParameters().put("to_var",
                                new Keyword.ParameterValue("newJsonVar", Keyword.ParameterValueType.STRING));
                jsonGetKw.getParameters().put("from_var",
                                new Keyword.ParameterValue("jsonVar", Keyword.ParameterValueType.STRING));
                jsonGetKw.getParameters().put("param_name",
                                new Keyword.ParameterValue("target_key", Keyword.ParameterValueType.STRING));
                jsonGetKw.keywordInit();
                NATTContext.instance().clearVariables();
        }

        @Test
        public void testExecute_WhenJsonVariableDoesNotExist() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar", null);
                boolean result = jsonGetKw.execute();
                assertTrue(result);
        }

        @Test
        public void testExecute_WhenJsonVariableIsNotValidJson() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar", "not_a_valid_json");
                boolean result = jsonGetKw.execute();
                assertTrue(result);
        }

        @Test
        public void testExecute_WhenParameterNotFound() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar", "{\"key\": \"value\"}");
                boolean result = jsonGetKw.execute();
                assertTrue(result);
        }

        @Test
        public void testExecute_WhenParameterIsFound() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar", "{\"target_key\": \"value11\"}");
                boolean result = jsonGetKw.execute();
                assertTrue(result);
                assertEquals("value11", NATTContext.instance().getVariable("newJsonVar"));
        }

        @Test
        public void testExecute_WhenParameterIsFoundAndIsObject() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar",
                                "{\"target_key\": {\"name\": \"Object Name\", \"value\": \"1234\"}}");
                boolean result = jsonGetKw.execute();
                assertTrue(result);
                assertEquals("{\"name\":\"Object Name\",\"value\":\"1234\"}",
                                NATTContext.instance().getVariable("newJsonVar"));
        }

        @Test
        public void testExecute_WhenParameterIsFoundAndIsList() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar",
                                "{\"target_key\": [{\"name\": \"Object Name\", \"value\": \"1234\"}, {\"name\": \"Object Name\", \"value\": \"1234\"}]}");
                boolean result = jsonGetKw.execute();
                assertTrue(result);
                assertEquals("[{\"name\":\"Object Name\",\"value\":\"1234\"},{\"name\":\"Object Name\",\"value\":\"1234\"}]",
                                NATTContext.instance().getVariable("newJsonVar"));
        }

        @Test
        public void testExecute_WhenParameterIsFoundAndIsList1() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar",
                                "[{\"name\": \"Object Name\", \"value\": \"1234\"}, {\"name\": \"Object Name\", \"value\": \"0\"}]");
                jsonGetKw.getParameters().put("param_name",
                                new Keyword.ParameterValue("1", Keyword.ParameterValueType.STRING));
                jsonGetKw.keywordInit();
                boolean result = jsonGetKw.execute();
                assertTrue(result);
                assertEquals("{\"name\":\"Object Name\",\"value\":\"0\"}",
                                NATTContext.instance().getVariable("newJsonVar"));
        }
}
