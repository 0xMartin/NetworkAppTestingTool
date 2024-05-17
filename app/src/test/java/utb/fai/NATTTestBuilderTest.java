package utb.fai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTTestBuilder;
import utb.fai.Core.Keyword.ParameterValue;
import utb.fai.Core.Keyword.ParameterValueType;
import utb.fai.Core.NATTContext;
import utb.fai.Core.NATTCore;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;
import utb.fai.Exception.NonUniqueTestNamesException;
import utb.fai.ReportGenerator.TestReportGenerator;

public class NATTTestBuilderTest {

    @Test
    public void buildTests_NullConfigurationData() {
        assertThrows(NullPointerException.class, () -> NATTTestBuilder.buildTests(null));
    }

    @Test
    public void buildTests_EmptyConfigurationData()
            throws InvalidSyntaxInConfigurationException, InternalErrorException {
        assertThrows(InvalidSyntaxInConfigurationException.class, () -> NATTTestBuilder.buildTests(new HashMap<>()));
    }

    @Test
    public void buildTests_ValidConfigurationData()
            throws InvalidSyntaxInConfigurationException, InternalErrorException, NonUniqueTestNamesException {
        // Staticky definovany obsah YAML konfigurace
        String yamlContent = "test_root:\n" +
                "  max_points: 5.0\n" +
                "  initial_commands:\n" +
                "    - run_app: \"java -jar app.jar --arg1 value1 --arg2 value2\"\n" +
                "  test_suites:\n" +
                "    - test_suite:\n" +
                "        name: \"Example suite\"\n" +
                "        initial_commands:\n" +
                "          - standard_stream_send: \"initialize\"\n" +
                "          - wait: 1000\n" +
                "        test_cases:\n" +
                "          - test_case:\n" +
                "              name: \"Example case 1\"\n" +
                "              description: \"An example test case 1 description\"\n" +
                "              steps:\n" +
                "                - run_app: \"java -jar app.jar --arg1 11 --arg2 22\"\n" +
                "                - wait_until:\n" +
                "                    module_name: \"module_1\"\n" +
                "                    time_out: 15000\n" +
                "                - assert_string:\n" +
                "                    var_name: \"var1\"\n" +
                "                    expected: \"expected text\"\n" +
                "                - clear_buffer: \"*\"\n" +
                "          - test_case:\n" +
                "              name: \"Example case 2\"\n" +
                "              description: \"An example test case 2 description\"\n" +
                "              steps:\n" +
                "                - run_app: \"app_name --arg1 value1 --arg2 value2\"\n" +
                "                - wait_until:\n" +
                "                    module_name: \"module_1\"\n" +
                "                    time_out: 15000\n" +
                "                - assert_string:\n" +
                "                    var_name: \"var88888\"\n" +
                "                    expected: \"expected text 88\"";

        Yaml yaml = new Yaml();
        Map<String, Object> configurationData = yaml.load(yamlContent);

        // root keyword
        Keyword rootKeyword = NATTTestBuilder.buildTests(configurationData);
        assertNotNull(rootKeyword);

        // inicializace testovaci struktury
        NATTContext.instance().setReportExtent(TestReportGenerator.generateReportExtent(NATTCore.REPORT_PATH,
                "Test report"));
        NATTTestBuilder.initTestScructure(rootKeyword);

        // parametr test_suites
        ParameterValue val = rootKeyword.getParameterValue("test_suites", ParameterValueType.LIST, true);
        assertNotNull(val.getValue());

        // overeni velikosti listu test_suites
        @SuppressWarnings("unchecked")
        List<Keyword> test_suites = (List<Keyword>) val.getValue();
        assertEquals(1, test_suites.size());

        // overeni prvniho prvku v listu test_suites
        assertEquals(test_suites.get(0).getParameters().get("name").getValue(), "Example suite");

        // ziskani listu test_cases z prvni test_suite
        val = test_suites.get(0).getParameterValue("test_cases", ParameterValueType.LIST, true);
        assertNotNull(val.getValue());

        // overeni velikosti listu test_cases
        @SuppressWarnings("unchecked")
        List<Keyword> test_cases = (List<Keyword>) val.getValue();
        assertEquals(2, test_cases.size());

        // overen jmen dvou test case v listu
        assertEquals(test_cases.get(0).getParameters().get("name").getValue(), "Example case 1");
        assertEquals(test_cases.get(1).getParameters().get("name").getValue(), "Example case 2");
    }

    @Test
    public void buildTests_InvalidConfigurationData()
            throws InvalidSyntaxInConfigurationException, InternalErrorException {
        // Staticky definovany obsah YAML konfigurace s neplatnou syntaxi
        String yamlContent = "test_root:\n" +
                "  max_points: 5.0\n" +
                "  initial_commands:\n" +
                "    - run_app: \"java -jar app.jar --arg1 value1 --arg2 value2\"\n" +
                "  test_suites:\n" +
                "    - test_suite:\n" +
                "        name: \"Example suite\"\n" +
                "        initial_commands:\n" +
                "          - standard_stream_send: \"initialize\"\n" +
                "          - wait: 1000\n" +
                "        test_cases:\n" +
                "          - test_case:\n" +
                "              name: \"Example case 1\"\n" +
                "              description: \"An example test case 1 description\"\n" +
                "              steps:\n" +
                "                - run_app: \"java -jar app.jar --arg1 11 --arg2 22\"\n" +
                "                - wait_until:\n" +
                "                    module_name: \"module_1\"\n" +
                "                    time_out: 15000\n" +
                "                - not_existing_keyword:\n" +
                "                    var_name: \"var1\"\n" +
                "                    expected_text: \"expected text\"\n" +
                "                - clear_buffer: \"*\"\n" +
                "          - test_case:\n" +
                "              name: \"Example case 2\"\n" +
                "              description: \"An example test case 2 description\"\n" +
                "              steps:\n" +
                "                - run_app: \"app_name --arg1 value1 --arg2 value2\"\n" +
                "                - wait_until:\n" +
                "                    module_name: \"module_1\"\n" +
                "                    time_out: 15000\n" +
                "                - assert_equal:\n" +
                "                    var_name: \"var88888\"\n" +
                "                    expected_text: \"expected text 88\"";

        Yaml yaml = new Yaml();
        Map<String, Object> configurationData = yaml.load(yamlContent);

        assertThrows(InvalidSyntaxInConfigurationException.class, () -> NATTTestBuilder.buildTests(configurationData));

    }

}
