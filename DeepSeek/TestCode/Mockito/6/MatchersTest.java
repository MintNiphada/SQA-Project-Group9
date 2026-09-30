package org.mockito;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.*;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.internal.progress.HandyReturnValues;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MatchersTest {

    private MockingProgress originalProgress;

    @Before
    public void setUp() throws Exception {
        originalProgress = getMockingProgress();
        setMockingProgress(new ThreadSafeMockingProgress());
    }

    @After
    public void tearDown() throws Exception {
        setMockingProgress(originalProgress);
    }

    private MockingProgress getMockingProgress() throws Exception {
        java.lang.reflect.Field field = Matchers.class.getDeclaredField("MOCKING_PROGRESS");
        field.setAccessible(true);
        return (MockingProgress) field.get(null);
    }

    private void setMockingProgress(MockingProgress progress) throws Exception {
        java.lang.reflect.Field field = Matchers.class.getDeclaredField("MOCKING_PROGRESS");
        field.setAccessible(true);
        field.set(null, progress);
    }

    @Test
    public void testAnyBoolean() {
        boolean result = Matchers.anyBoolean();
        assertFalse(result);
    }

    @Test
    public void testAnyByte() {
        byte result = Matchers.anyByte();
        assertEquals(0, result);
    }

    @Test
    public void testAnyChar() {
        char result = Matchers.anyChar();
        assertEquals(0, result);
    }

    @Test
    public void testAnyInt() {
        int result = Matchers.anyInt();
        assertEquals(0, result);
    }

    @Test
    public void testAnyLong() {
        long result = Matchers.anyLong();
        assertEquals(0L, result);
    }

    @Test
    public void testAnyFloat() {
        float result = Matchers.anyFloat();
        assertEquals(0.0f, result, 0.0f);
    }

    @Test
    public void testAnyDouble() {
        double result = Matchers.anyDouble();
        assertEquals(0.0d, result, 0.0d);
    }

    @Test
    public void testAnyShort() {
        short result = Matchers.anyShort();
        assertEquals(0, result);
    }

    @Test
    public void testAnyObject() {
        Object result = Matchers.anyObject();
        assertNull(result);
    }

    @Test
    public void testAnyVararg() {
        Object result = Matchers.anyVararg();
        assertNull(result);
    }

    @Test
    public void testAnyWithClass() {
        String result = Matchers.any(String.class);
        assertNull(result);
    }

    @Test
    public void testAny() {
        Object result = Matchers.any();
        assertNull(result);
    }

    @Test
    public void testAnyString() {
        String result = Matchers.anyString();
        assertEquals("", result);
    }

    @Test
    public void testAnyList() {
        List result = Matchers.anyList();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAnyListOf() {
        List<String> result = Matchers.anyListOf(String.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAnySet() {
        Set result = Matchers.anySet();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAnySetOf() {
        Set<String> result = Matchers.anySetOf(String.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAnyMap() {
        Map result = Matchers.anyMap();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAnyMapOf() {
        Map<String, Integer> result = Matchers.anyMapOf(String.class, Integer.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAnyCollection() {
        Collection result = Matchers.anyCollection();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAnyCollectionOf() {
        Collection<String> result = Matchers.anyCollectionOf(String.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testIsA() {
        String result = Matchers.isA(String.class);
        assertNull(result);
    }

    @Test
    public void testEqBoolean() {
        boolean result = Matchers.eq(true);
        assertFalse(result);
    }

    @Test
    public void testEqByte() {
        byte result = Matchers.eq((byte) 5);
        assertEquals(0, result);
    }

    @Test
    public void testEqChar() {
        char result = Matchers.eq('a');
        assertEquals(0, result);
    }

    @Test
    public void testEqDouble() {
        double result = Matchers.eq(3.14);
        assertEquals(0.0, result, 0.0);
    }

    @Test
    public void testEqFloat() {
        float result = Matchers.eq(2.71f);
        assertEquals(0.0f, result, 0.0f);
    }

    @Test
    public void testEqInt() {
        int result = Matchers.eq(42);
        assertEquals(0, result);
    }

    @Test
    public void testEqLong() {
        long result = Matchers.eq(100L);
        assertEquals(0L, result);
    }

    @Test
    public void testEqShort() {
        short result = Matchers.eq((short) 10);
        assertEquals(0, result);
    }

    @Test
    public void testEqObject() {
        String input = "test";
        String result = Matchers.eq(input);
        assertNull(result);
    }

    @Test
    public void testRefEq() {
        Object input = new Object();
        Object result = Matchers.refEq(input);
        assertNull(result);
    }

    @Test
    public void testRefEqWithExcludeFields() {
        Object input = new Object();
        Object result = Matchers.refEq(input, "field1", "field2");
        assertNull(result);
    }

    @Test
    public void testSame() {
        Object input = new Object();
        Object result = Matchers.same(input);
        assertNull(result);
    }

    @Test
    public void testIsNull() {
        Object result = Matchers.isNull();
        assertNull(result);
    }

    @Test
    public void testIsNullWithClass() {
        String result = Matchers.isNull(String.class);
        assertNull(result);
    }

    @Test
    public void testNotNull() {
        Object result = Matchers.notNull();
        assertNull(result);
    }

    @Test
    public void testNotNullWithClass() {
        String result = Matchers.notNull(String.class);
        assertNull(result);
    }

    @Test
    public void testIsNotNull() {
        Object result = Matchers.isNotNull();
        assertNull(result);
    }

    @Test
    public void testIsNotNullWithClass() {
        String result = Matchers.isNotNull(String.class);
        assertNull(result);
    }

    @Test
    public void testContains() {
        String result = Matchers.contains("substring");
        assertEquals("", result);
    }

    @Test
    public void testMatches() {
        String result = Matchers.matches("regex");
        assertEquals("", result);
    }

    @Test
    public void testEndsWith() {
        String result = Matchers.endsWith("suffix");
        assertEquals("", result);
    }

    @Test
    public void testStartsWith() {
        String result = Matchers.startsWith("prefix");
        assertEquals("", result);
    }

    @Test
    public void testArgThat() {
        Matcher<String> matcher = new org.mockito.ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof String;
            }
        };
        String result = Matchers.argThat(matcher);
        assertNull(result);
    }

    @Test
    public void testCharThat() {
        Matcher<Character> matcher = new org.mockito.ArgumentMatcher<Character>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof Character;
            }
        };
        char result = Matchers.charThat(matcher);
        assertEquals(0, result);
    }

    @Test
    public void testBooleanThat() {
        Matcher<Boolean> matcher = new org.mockito.ArgumentMatcher<Boolean>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof Boolean;
            }
        };
        boolean result = Matchers.booleanThat(matcher);
        assertFalse(result);
    }

    @Test
    public void testByteThat() {
        Matcher<Byte> matcher = new org.mockito.ArgumentMatcher<Byte>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof Byte;
            }
        };
        byte result = Matchers.byteThat(matcher);
        assertEquals(0, result);
    }

    @Test
    public void testShortThat() {
        Matcher<Short> matcher = new org.mockito.ArgumentMatcher<Short>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof Short;
            }
        };
        short result = Matchers.shortThat(matcher);
        assertEquals(0, result);
    }

    @Test
    public void testIntThat() {
        Matcher<Integer> matcher = new org.mockito.ArgumentMatcher<Integer>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof Integer;
            }
        };
        int result = Matchers.intThat(matcher);
        assertEquals(0, result);
    }

    @Test
    public void testLongThat() {
        Matcher<Long> matcher = new org.mockito.ArgumentMatcher<Long>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof Long;
            }
        };
        long result = Matchers.longThat(matcher);
        assertEquals(0L, result);
    }

    @Test
    public void testFloatThat() {
        Matcher<Float> matcher = new org.mockito.ArgumentMatcher<Float>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof Float;
            }
        };
        float result = Matchers.floatThat(matcher);
        assertEquals(0.0f, result, 0.0f);
    }

    @Test
    public void testDoubleThat() {
        Matcher<Double> matcher = new org.mockito.ArgumentMatcher<Double>() {
            @Override
            public boolean matches(Object argument) {
                return argument instanceof Double;
            }
        };
        double result = Matchers.doubleThat(matcher);
        assertEquals(0.0d, result, 0.0d);
    }
}
