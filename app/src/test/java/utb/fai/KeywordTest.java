package utb.fai;

import org.junit.Test;

import utb.fai.Core.Keyword;
import utb.fai.Core.NATTAnnotation;
import utb.fai.Exception.InternalErrorException;
import utb.fai.Exception.InvalidSyntaxInConfigurationException;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KeywordTest {

        @NATTAnnotation.Keyword(name = "ConcreteKeyword")
        private class ConcreteKeyword extends Keyword {

                @Override
                public boolean execute() throws InternalErrorException {
                        return true;
                }

                @Override
                protected void keywordInit() throws InvalidSyntaxInConfigurationException {
                }

                @Override
                public void deleteAction() throws InternalErrorException {
                }
        }

        @Test
        public void testKeywordName() {
                Keyword keyword = new ConcreteKeyword();
                assertEquals("ConcreteKeyword", keyword.getKeywordName());
        }

        @Test
        public void testPutAndGetParameter() throws InvalidSyntaxInConfigurationException {
                Keyword keyword = new ConcreteKeyword();
                keyword.putParameter("param1", new Keyword.ParameterValue(10, Keyword.ParameterValueType.LONG));
                keyword.putParameter("param2", new Keyword.ParameterValue("test", Keyword.ParameterValueType.STRING));

                assertEquals(10, keyword.getParameterValue("param1", Keyword.ParameterValueType.LONG, true).getValue());
                assertEquals(Keyword.ParameterValueType.LONG,
                                keyword.getParameterValue("param1", Keyword.ParameterValueType.LONG, true).getType());
                assertEquals("test", keyword.getParameterValue("param2", Keyword.ParameterValueType.STRING, true)
                                .getValue());
                assertEquals(Keyword.ParameterValueType.STRING,
                                keyword.getParameterValue("param2", Keyword.ParameterValueType.STRING, true).getType());
        }

        @Test
        public void testBuildKeyword() throws InvalidSyntaxInConfigurationException {
                ConcreteKeyword keyword = new ConcreteKeyword();
                HashMap<String, Object> data = new HashMap<>();
                data.put("param1", 10);
                data.put("param2", "test");

                keyword.build(data);

                assertEquals((Long) 10L, (Long) keyword
                                .getParameterValue("param1", Keyword.ParameterValueType.LONG, true).getValue());
                assertEquals(Keyword.ParameterValueType.LONG,
                                keyword.getParameterValue("param1", Keyword.ParameterValueType.LONG, true).getType());
                assertEquals("test", keyword.getParameterValue("param2", Keyword.ParameterValueType.STRING, true)
                                .getValue());
                assertEquals(Keyword.ParameterValueType.STRING,
                                keyword.getParameterValue("param2", Keyword.ParameterValueType.STRING, true).getType());
        }

        @Test
        public void testBuildKeywordWithMultipleValues() throws InvalidSyntaxInConfigurationException {
                ConcreteKeyword keyword = new ConcreteKeyword();
                HashMap<String, Object> data = new HashMap<>();
                data.put("param1", 10);
                data.put("param2", "test");
                data.put("param3", true);

                keyword.build(data);

                assertEquals((Long) 10L, (Long) keyword
                                .getParameterValue("param1", Keyword.ParameterValueType.LONG, true).getValue());
                assertEquals(Keyword.ParameterValueType.LONG,
                                keyword.getParameterValue("param1", Keyword.ParameterValueType.LONG, true).getType());
                assertEquals("test", keyword.getParameterValue("param2", Keyword.ParameterValueType.STRING, true)
                                .getValue());
                assertEquals(Keyword.ParameterValueType.STRING,
                                keyword.getParameterValue("param2", Keyword.ParameterValueType.STRING, true).getType());
                assertEquals(true, keyword.getParameterValue("param3", Keyword.ParameterValueType.BOOLEAN, true)
                                .getValue());
                assertEquals(Keyword.ParameterValueType.BOOLEAN,
                                keyword.getParameterValue("param3", Keyword.ParameterValueType.BOOLEAN, true)
                                                .getType());
        }

        @Test
        public void testBuildKeywordWithDifferentParameterTypes() throws InvalidSyntaxInConfigurationException {
                ConcreteKeyword keyword = new ConcreteKeyword();
                HashMap<String, Object> data = new HashMap<>();
                data.put("param1", 10);
                data.put("param2", "test");
                data.put("param3", 3.14);
                data.put("param4", true);

                keyword.build(data);

                assertEquals((Long) 10L, (Long) keyword
                                .getParameterValue("param1", Keyword.ParameterValueType.LONG, true).getValue());
                assertEquals(Keyword.ParameterValueType.LONG,
                                keyword.getParameterValue("param1", Keyword.ParameterValueType.LONG, true).getType());
                assertEquals("test", keyword.getParameterValue("param2", Keyword.ParameterValueType.STRING, true)
                                .getValue());
                assertEquals(Keyword.ParameterValueType.STRING,
                                keyword.getParameterValue("param2", Keyword.ParameterValueType.STRING, true).getType());
                assertEquals(3.14, keyword.getParameterValue("param3", Keyword.ParameterValueType.DOUBLE, true)
                                .getValue());
                assertEquals(Keyword.ParameterValueType.DOUBLE,
                                keyword.getParameterValue("param3", Keyword.ParameterValueType.DOUBLE, true).getType());
                assertEquals(true, keyword.getParameterValue("param4", Keyword.ParameterValueType.BOOLEAN, true)
                                .getValue());
                assertEquals(Keyword.ParameterValueType.BOOLEAN,
                                keyword.getParameterValue("param4", Keyword.ParameterValueType.BOOLEAN, true)
                                                .getType());
        }

        @Test
        public void testBuildKeywordWithListParameter() throws InvalidSyntaxInConfigurationException {
                ConcreteKeyword keyword = new ConcreteKeyword();
                List<String> list = Arrays.asList("item1", "item2", "item3");
                HashMap<String, Object> data = new HashMap<>();
                data.put("param1", list);

                keyword.build(data);

                assertEquals(list,
                                keyword.getParameterValue("param1", Keyword.ParameterValueType.LIST, true).getValue());
                assertEquals(Keyword.ParameterValueType.LIST,
                                keyword.getParameterValue("param1", Keyword.ParameterValueType.LIST, true).getType());
        }

}
