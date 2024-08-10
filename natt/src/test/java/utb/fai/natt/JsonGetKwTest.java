package utb.fai.natt;

import org.junit.Before;
import org.junit.Test;

import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.exception.NonUniqueModuleNamesException;
import utb.fai.natt.core.NATTContext;
import utb.fai.natt.keyword.General.JsonGetKw;

import static org.junit.Assert.*;

public class JsonGetKwTest {

        private JsonGetKw jsonGetKw;

        @Before
        public void setUp() throws InvalidSyntaxInConfigurationException {
                jsonGetKw = new JsonGetKw();
                jsonGetKw.getParameters().put("to_var",
                                new NATTKeyword.ParameterValue("newJsonVar", NATTKeyword.ParameterValueType.STRING));
                jsonGetKw.getParameters().put("from_var",
                                new NATTKeyword.ParameterValue("jsonVar", NATTKeyword.ParameterValueType.STRING));
                jsonGetKw.getParameters().put("param_name",
                                new NATTKeyword.ParameterValue("target_key", NATTKeyword.ParameterValueType.STRING));
                jsonGetKw.keywordInit(NATTContext.instance());
                NATTContext.instance().clearVariables();
        }

        @Test
        public void testExecute_WhenJsonVariableDoesNotExist() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar", null);
                boolean result = jsonGetKw.execute(NATTContext.instance());
                assertTrue(result);
        }

        @Test
        public void testExecute_WhenJsonVariableIsNotValidJson() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar", "not_a_valid_json");
                boolean result = jsonGetKw.execute(NATTContext.instance());
                assertTrue(result);
        }

        @Test
        public void testExecute_WhenParameterNotFound() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar", "{\"key\": \"value\"}");
                boolean result = jsonGetKw.execute(NATTContext.instance());
                assertTrue(result);
        }

        @Test
        public void testExecute_WhenParameterIsFound() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar", "{\"target_key\": \"value11\"}");
                boolean result = jsonGetKw.execute(NATTContext.instance());
                assertTrue(result);
                assertEquals("value11", NATTContext.instance().getVariable("newJsonVar"));
        }

        @Test
        public void testExecute_WhenParameterIsFoundAndIsObject() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar",
                                "{\"target_key\": {\"name\": \"Object Name\", \"value\": \"1234\"}}");
                boolean result = jsonGetKw.execute(NATTContext.instance());
                assertTrue(result);
                assertEquals("{\"name\":\"Object Name\",\"value\":\"1234\"}",
                                NATTContext.instance().getVariable("newJsonVar"));
        }

        @Test
        public void testExecute_WhenParameterIsFoundAndIsList() throws InternalErrorException,
                        NonUniqueModuleNamesException, InvalidSyntaxInConfigurationException {
                NATTContext.instance().storeValueToVariable("jsonVar",
                                "{\"target_key\": [{\"name\": \"Object Name\", \"value\": \"1234\"}, {\"name\": \"Object Name\", \"value\": \"1234\"}]}");
                boolean result = jsonGetKw.execute(NATTContext.instance());
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
                                new NATTKeyword.ParameterValue("1", NATTKeyword.ParameterValueType.STRING));
                jsonGetKw.keywordInit(NATTContext.instance());
                boolean result = jsonGetKw.execute(NATTContext.instance());
                assertTrue(result);
                assertEquals("{\"name\":\"Object Name\",\"value\":\"0\"}",
                                NATTContext.instance().getVariable("newJsonVar"));
        }
}
