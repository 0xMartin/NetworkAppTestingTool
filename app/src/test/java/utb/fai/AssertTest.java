package utb.fai;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import utb.fai.Core.NATTAssert;

public class AssertTest {

    @Test
    public void testAssertConditionEquals() {
        assertTrue(NATTAssert.assertCondition("hello", "hello", "equals", true));
        assertFalse(NATTAssert.assertCondition("hello", "Hello", "equals", true));
    }

    @Test
    public void testAssertConditionContains() {
        assertTrue(NATTAssert.assertCondition("hello", "ell", "contains", false));
        assertFalse(NATTAssert.assertCondition("hello", "abc", "contains", false));
    }

    @Test
    public void testAssertConditionStartsWith() {
        assertTrue(NATTAssert.assertCondition("hello", "hel", "startswith", true));
        assertFalse(NATTAssert.assertCondition("hello", "abc", "startswith", true));
    }

    @Test
    public void testAssertConditionEndsWith() {
        assertTrue(NATTAssert.assertCondition("hello", "lo", "endswith", false));
        assertFalse(NATTAssert.assertCondition("hello", "abc", "endswith", false));
    }

    @Test
    public void testAssertNumberConditionEquals() {
        assertTrue(NATTAssert.assertNumberCondition("10", 10.0, "="));
        assertFalse(NATTAssert.assertNumberCondition("10", 5.0, "="));
    }

    @Test
    public void testAssertNumberConditionGreaterThan() {
        assertTrue(NATTAssert.assertNumberCondition("10", 5.0, ">"));
        assertFalse(NATTAssert.assertNumberCondition("10", 15.0, ">"));
    }

    @Test
    public void testAssertNumberConditionLessThan() {
        assertTrue(NATTAssert.assertNumberCondition("10", 15.0, "<"));
        assertFalse(NATTAssert.assertNumberCondition("10", 5.0, "<"));
    }

}
