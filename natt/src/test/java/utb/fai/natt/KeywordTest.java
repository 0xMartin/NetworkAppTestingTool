package utb.fai.natt;

import org.junit.Test;

import utb.fai.natt.spi.exception.InternalErrorException;
import utb.fai.natt.spi.exception.InvalidSyntaxInConfigurationException;
import utb.fai.natt.spi.NATTKeyword;
import utb.fai.natt.spi.INATTContext;
import utb.fai.natt.spi.NATTAnnotation;
import utb.fai.natt.core.NATTTestBuilder;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KeywordTest {

    @NATTAnnotation.Keyword(name = "ConcreteKeyword")
    private class ConcreteKeyword extends NATTKeyword {

        @Override
        public boolean execute(INATTContext ctx) throws InternalErrorException {
            return true;
        }

        @Override
        public void keywordInit(INATTContext ctx) throws InvalidSyntaxInConfigurationException {
        }

        @Override
        public void deleteAction(INATTContext ctx) throws InternalErrorException {
        }
    }

    @Test
    public void testKeywordName() {
        NATTKeyword keyword = new ConcreteKeyword();
        assertEquals("ConcreteKeyword", keyword.getKeywordName());
    }

    @Test
    public void testPutAndGetParameter() throws InvalidSyntaxInConfigurationException {
        NATTKeyword keyword = new ConcreteKeyword();
        keyword.putParameter("param1", new NATTKeyword.ParameterValue(10, NATTKeyword.ParameterValueType.LONG));
        keyword.putParameter("param2", new NATTKeyword.ParameterValue("test", NATTKeyword.ParameterValueType.STRING));

        assertEquals(10, keyword.getParameterValue("param1", NATTKeyword.ParameterValueType.LONG, true).getValue());
        assertEquals(NATTKeyword.ParameterValueType.LONG,
                keyword.getParameterValue("param1", NATTKeyword.ParameterValueType.LONG, true).getType());
        assertEquals("test", keyword.getParameterValue("param2", NATTKeyword.ParameterValueType.STRING, true)
                .getValue());
        assertEquals(NATTKeyword.ParameterValueType.STRING,
                keyword.getParameterValue("param2", NATTKeyword.ParameterValueType.STRING, true).getType());
    }

    @Test
    public void testBuildKeyword() throws InvalidSyntaxInConfigurationException {
        ConcreteKeyword keyword = new ConcreteKeyword();
        HashMap<String, Object> data = new HashMap<>();
        data.put("param1", 10);
        data.put("param2", "test");

        NATTTestBuilder.buildKeyword(keyword, data);

        assertEquals((Long) 10L, (Long) keyword
                .getParameterValue("param1", NATTKeyword.ParameterValueType.LONG, true).getValue());
        assertEquals(NATTKeyword.ParameterValueType.LONG,
                keyword.getParameterValue("param1", NATTKeyword.ParameterValueType.LONG, true).getType());
        assertEquals("test", keyword.getParameterValue("param2", NATTKeyword.ParameterValueType.STRING, true)
                .getValue());
        assertEquals(NATTKeyword.ParameterValueType.STRING,
                keyword.getParameterValue("param2", NATTKeyword.ParameterValueType.STRING, true).getType());
    }

    @Test
    public void testBuildKeywordWithMultipleValues() throws InvalidSyntaxInConfigurationException {
        ConcreteKeyword keyword = new ConcreteKeyword();
        HashMap<String, Object> data = new HashMap<>();
        data.put("param1", 10);
        data.put("param2", "test");
        data.put("param3", true);

        NATTTestBuilder.buildKeyword(keyword, data);

        assertEquals((Long) 10L, (Long) keyword
                .getParameterValue("param1", NATTKeyword.ParameterValueType.LONG, true).getValue());
        assertEquals(NATTKeyword.ParameterValueType.LONG,
                keyword.getParameterValue("param1", NATTKeyword.ParameterValueType.LONG, true).getType());
        assertEquals("test", keyword.getParameterValue("param2", NATTKeyword.ParameterValueType.STRING, true)
                .getValue());
        assertEquals(NATTKeyword.ParameterValueType.STRING,
                keyword.getParameterValue("param2", NATTKeyword.ParameterValueType.STRING, true).getType());
        assertEquals(true, keyword.getParameterValue("param3", NATTKeyword.ParameterValueType.BOOLEAN, true)
                .getValue());
        assertEquals(NATTKeyword.ParameterValueType.BOOLEAN,
                keyword.getParameterValue("param3", NATTKeyword.ParameterValueType.BOOLEAN, true)
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

        NATTTestBuilder.buildKeyword(keyword, data);

        assertEquals((Long) 10L, (Long) keyword
                .getParameterValue("param1", NATTKeyword.ParameterValueType.LONG, true).getValue());
        assertEquals(NATTKeyword.ParameterValueType.LONG,
                keyword.getParameterValue("param1", NATTKeyword.ParameterValueType.LONG, true).getType());
        assertEquals("test", keyword.getParameterValue("param2", NATTKeyword.ParameterValueType.STRING, true)
                .getValue());
        assertEquals(NATTKeyword.ParameterValueType.STRING,
                keyword.getParameterValue("param2", NATTKeyword.ParameterValueType.STRING, true).getType());
        assertEquals(3.14, keyword.getParameterValue("param3", NATTKeyword.ParameterValueType.DOUBLE, true)
                .getValue());
        assertEquals(NATTKeyword.ParameterValueType.DOUBLE,
                keyword.getParameterValue("param3", NATTKeyword.ParameterValueType.DOUBLE, true).getType());
        assertEquals(true, keyword.getParameterValue("param4", NATTKeyword.ParameterValueType.BOOLEAN, true)
                .getValue());
        assertEquals(NATTKeyword.ParameterValueType.BOOLEAN,
                keyword.getParameterValue("param4", NATTKeyword.ParameterValueType.BOOLEAN, true)
                        .getType());
    }

    @Test
    public void testBuildKeywordWithListParameter() throws InvalidSyntaxInConfigurationException {
        ConcreteKeyword keyword = new ConcreteKeyword();
        List<String> list = Arrays.asList("item1", "item2", "item3");
        HashMap<String, Object> data = new HashMap<>();
        data.put("param1", list);

        NATTTestBuilder.buildKeyword(keyword, data);

        assertEquals(list,
                keyword.getParameterValue("param1", NATTKeyword.ParameterValueType.LIST, true).getValue());
        assertEquals(NATTKeyword.ParameterValueType.LIST,
                keyword.getParameterValue("param1", NATTKeyword.ParameterValueType.LIST, true).getType());
    }

}
