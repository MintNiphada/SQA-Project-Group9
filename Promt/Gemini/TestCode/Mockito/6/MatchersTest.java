package org.mockito;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Any;
import org.mockito.internal.matchers.AnyVararg;
import org.mockito.internal.matchers.Contains;
import org.mockito.internal.matchers.EndsWith;
import org.mockito.internal.matchers.Equals;
import org.mockito.internal.matchers.InstanceOf;
import org.mockito.internal.matchers.Matches;
import org.mockito.internal.matchers.NotNull;
import org.mockito.internal.matchers.Null;
import org.mockito.internal.matchers.Same;
import org.mockito.internal.matchers.StartsWith;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MatchersTest {

    private final MockingProgress mockingProgress = new ThreadSafeMockingProgress();

    @Before
    @After
    public void resetState() {
        mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers();
        mockingProgress.validateState();
    }

    private Matcher<?> lastReportedMatcher() {
        List<org.mockito.internal.matchers.LocalizedMatcher> matchers = mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers();
        assertNotNull("Matcher list should not be null", matchers);
        assertFalse("At least one matcher should be registered", matchers.isEmpty());
        return matchers.get(matchers.size() - 1).getMatcher();
    }

    @Test
    public void shouldInstantiateMatchers() {
        Matchers matchers = new Matchers();
        assertNotNull(matchers);
    }

    @Test
    public void shouldReportAnyBoolean() {
        boolean result = Matchers.anyBoolean();
        assertFalse(result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyByte() {
        byte result = Matchers.anyByte();
        assertEquals((byte) 0, result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyChar() {
        char result = Matchers.anyChar();
        assertEquals('\u0000', result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyInt() {
        int result = Matchers.anyInt();
        assertEquals(0, result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyLong() {
        long result = Matchers.anyLong();
        assertEquals(0L, result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyFloat() {
        float result = Matchers.anyFloat();
        assertEquals(0.0f, result, 0.0001f);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyDouble() {
        double result = Matchers.anyDouble();
        assertEquals(0.0d, result, 0.0001d);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyShort() {
        short result = Matchers.anyShort();
        assertEquals((short) 0, result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyObject() {
        Object result = Matchers.anyObject();
        assertNull(result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyVararg() {
        Object result = Matchers.anyVararg();
        assertNull(result);
        assertEquals(AnyVararg.ANY_VARARG, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyClass() {
        String result = Matchers.any(String.class);
        assertNull(result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAny() {
        Object result = Matchers.any();
        assertNull(result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyString() {
        String result = Matchers.anyString();
        assertEquals("", result);
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyList() {
        List<?> result = Matchers.anyList();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyListOf() {
        List<String> result = Matchers.anyListOf(String.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnySet() {
        Set<?> result = Matchers.anySet();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnySetOf() {
        Set<Integer> result = Matchers.anySetOf(Integer.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyMap() {
        Map<?, ?> result = Matchers.anyMap();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyMapOf() {
        Map<String, Integer> result = Matchers.anyMapOf(String.class, Integer.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyCollection() {
        Collection<?> result = Matchers.anyCollection();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportAnyCollectionOf() {
        Collection<Double> result = Matchers.anyCollectionOf(Double.class);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(Any.ANY, lastReportedMatcher());
    }

    @Test
    public void shouldReportIsA() {
        String result = Matchers.isA(String.class);
        assertNull(result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof InstanceOf);
        assertTrue(((InstanceOf) matcher).matches("test"));
        assertFalse(((InstanceOf) matcher).matches(123));
    }

    @Test
    public void shouldReportEqBoolean() {
        boolean result = Matchers.eq(true);
        assertFalse(result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Equals);
        assertTrue(((Equals) matcher).matches(true));
        assertFalse(((Equals) matcher).matches(false));
    }

    @Test
    public void shouldReportEqByte() {
        byte result = Matchers.eq((byte) 5);
        assertEquals((byte) 0, result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Equals);
        assertTrue(((Equals) matcher).matches((byte) 5));
        assertFalse(((Equals) matcher).matches((byte) 6));
    }

    @Test
    public void shouldReportEqChar() {
        char result = Matchers.eq('x');
        assertEquals('\u0000', result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Equals);
        assertTrue(((Equals) matcher).matches('x'));
        assertFalse(((Equals) matcher).matches('y'));
    }

    @Test
    public void shouldReportEqDouble() {
        double result = Matchers.eq(3.14d);
        assertEquals(0.0d, result, 0.0001d);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Equals);
        assertTrue(((Equals) matcher).matches(3.14d));
        assertFalse(((Equals) matcher).matches(2.71d));
    }

    @Test
    public void shouldReportEqFloat() {
        float result = Matchers.eq(2.5f);
        assertEquals(0.0f, result, 0.0001f);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Equals);
        assertTrue(((Equals) matcher).matches(2.5f));
        assertFalse(((Equals) matcher).matches(1.5f));
    }

    @Test
    public void shouldReportEqInt() {
        int result = Matchers.eq(42);
        assertEquals(0, result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Equals);
        assertTrue(((Equals) matcher).matches(42));
        assertFalse(((Equals) matcher).matches(43));
    }

    @Test
    public void shouldReportEqLong() {
        long result = Matchers.eq(100L);
        assertEquals(0L, result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Equals);
        assertTrue(((Equals) matcher).matches(100L));
        assertFalse(((Equals) matcher).matches(101L));
    }

    @Test
    public void shouldReportEqShort() {
        short result = Matchers.eq((short) 10);
        assertEquals((short) 0, result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Equals);
        assertTrue(((Equals) matcher).matches((short) 10));
        assertFalse(((Equals) matcher).matches((short) 11));
    }

    @Test
    public void shouldReportEqObject() {
        String input = "sample";
        String result = Matchers.eq(input);
        assertNull(result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Equals);
        assertTrue(((Equals) matcher).matches("sample"));
        assertFalse(((Equals) matcher).matches("other"));
    }

    @Test
    public void shouldReportRefEq() {
        class Dummy {
            final int id;
            final String name;
            Dummy(int id, String name) {
                this.id = id;
                this.name = name;
            }
        }

        Dummy dummy1 = new Dummy(1, "A");
        Dummy dummy2 = new Dummy(1, "B");
        Dummy dummy3 = new Dummy(2, "A");

        Dummy result = Matchers.refEq(dummy1, "name");
        assertNull(result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof ReflectionEquals);
        assertTrue(((ReflectionEquals) matcher).matches(dummy2));
        assertFalse(((ReflectionEquals) matcher).matches(dummy3));
    }

    @Test
    public void shouldReportSame() {
        String instance = "instance";
        String result = Matchers.same(instance);
        assertNull(result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Same);
        assertTrue(((Same) matcher).matches(instance));
        assertFalse(((Same) matcher).matches(new String("instance")));
    }

    @Test
    public void shouldReportIsNull() {
        Object result = Matchers.isNull();
        assertNull(result);
        assertEquals(Null.NULL, lastReportedMatcher());
    }

    @Test
    public void shouldReportIsNullClass() {
        String result = Matchers.isNull(String.class);
        assertNull(result);
        assertEquals(Null.NULL, lastReportedMatcher());
    }

    @Test
    public void shouldReportNotNull() {
        Object result = Matchers.notNull();
        assertNull(result);
        assertEquals(NotNull.NOT_NULL, lastReportedMatcher());
    }

    @Test
    public void shouldReportNotNullClass() {
        String result = Matchers.notNull(String.class);
        assertNull(result);
        assertEquals(NotNull.NOT_NULL, lastReportedMatcher());
    }

    @Test
    public void shouldReportIsNotNull() {
        Object result = Matchers.isNotNull();
        assertNull(result);
        assertEquals(NotNull.NOT_NULL, lastReportedMatcher());
    }

    @Test
    public void shouldReportIsNotNullClass() {
        String result = Matchers.isNotNull(String.class);
        assertNull(result);
        assertEquals(NotNull.NOT_NULL, lastReportedMatcher());
    }

    @Test
    public void shouldReportContains() {
        String result = Matchers.contains("needle");
        assertEquals("", result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Contains);
        assertTrue(((Contains) matcher).matches("haystack with needle in it"));
        assertFalse(((Contains) matcher).matches("haystack only"));
    }

    @Test
    public void shouldReportMatches() {
        String result = Matchers.matches("[0-9]+");
        assertEquals("", result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof Matches);
        assertTrue(((Matches) matcher).matches("12345"));
        assertFalse(((Matches) matcher).matches("123a"));
    }

    @Test
    public void shouldReportEndsWith() {
        String result = Matchers.endsWith("world");
        assertEquals("", result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof EndsWith);
        assertTrue(((EndsWith) matcher).matches("hello world"));
        assertFalse(((EndsWith) matcher).matches("hello world!"));
    }

    @Test
    public void shouldReportStartsWith() {
        String result = Matchers.startsWith("hello");
        assertEquals("", result);
        Matcher<?> matcher = lastReportedMatcher();
        assertTrue(matcher instanceof StartsWith);
        assertTrue(((StartsWith) matcher).matches("hello world"));
        assertFalse(((StartsWith) matcher).matches("world hello"));
    }

    @Test
    public void shouldReportArgThat() {
        Matcher<String> customMatcher = new BaseMatcher<String>() {
            @Override
            public boolean matches(Object item) {
                return "custom".equals(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is custom");
            }
        };

        String result = Matchers.argThat(customMatcher);
        assertNull(result);
        assertEquals(customMatcher, lastReportedMatcher());
    }

    @Test
    public void shouldReportCharThat() {
        Matcher<Character> customMatcher = new BaseMatcher<Character>() {
            @Override
            public boolean matches(Object item) {
                return Character.valueOf('c').equals(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is char c");
            }
        };

        char result = Matchers.charThat(customMatcher);
        assertEquals('\u0000', result);
        assertEquals(customMatcher, lastReportedMatcher());
    }

    @Test
    public void shouldReportBooleanThat() {
        Matcher<Boolean> customMatcher = new BaseMatcher<Boolean>() {
            @Override
            public boolean matches(Object item) {
                return Boolean.TRUE.equals(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is true");
            }
        };

        boolean result = Matchers.booleanThat(customMatcher);
        assertFalse(result);
        assertEquals(customMatcher, lastReportedMatcher());
    }

    @Test
    public void shouldReportByteThat() {
        Matcher<Byte> customMatcher = new BaseMatcher<Byte>() {
            @Override
            public boolean matches(Object item) {
                return Byte.valueOf((byte) 1).equals(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is byte 1");
            }
        };

        byte result = Matchers.byteThat(customMatcher);
        assertEquals((byte) 0, result);
        assertEquals(customMatcher, lastReportedMatcher());
    }

    @Test
    public void shouldReportShortThat() {
        Matcher<Short> customMatcher = new BaseMatcher<Short>() {
            @Override
            public boolean matches(Object item) {
                return Short.valueOf((short) 1).equals(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is short 1");
            }
        };

        short result = Matchers.shortThat(customMatcher);
        assertEquals((short) 0, result);
        assertEquals(customMatcher, lastReportedMatcher());
    }

    @Test
    public void shouldReportIntThat() {
        Matcher<Integer> customMatcher = new BaseMatcher<Integer>() {
            @Override
            public boolean matches(Object item) {
                return Integer.valueOf(1).equals(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is int 1");
            }
        };

        int result = Matchers.intThat(customMatcher);
        assertEquals(0, result);
        assertEquals(customMatcher, lastReportedMatcher());
    }

    @Test
    public void shouldReportLongThat() {
        Matcher<Long> customMatcher = new BaseMatcher<Long>() {
            @Override
            public boolean matches(Object item) {
                return Long.valueOf(1L).equals(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is long 1");
            }
        };

        long result = Matchers.longThat(customMatcher);
        assertEquals(0L, result);
        assertEquals(customMatcher, lastReportedMatcher());
    }

    @Test
    public void shouldReportFloatThat() {
        Matcher<Float> customMatcher = new BaseMatcher<Float>() {
            @Override
            public boolean matches(Object item) {
                return Float.valueOf(1.0f).equals(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is float 1.0");
            }
        };

        float result = Matchers.floatThat(customMatcher);
        assertEquals(0.0f, result, 0.0001f);
        assertEquals(customMatcher, lastReportedMatcher());
    }

    @Test
    public void shouldReportDoubleThat() {
        Matcher<Double> customMatcher = new BaseMatcher<Double>() {
            @Override
            public boolean matches(Object item) {
                return Double.valueOf(1.0d).equals(item);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("is double 1.0");
            }
        };

        double result = Matchers.doubleThat(customMatcher);
        assertEquals(0.0d, result, 0.0001d);
        assertEquals(customMatcher, lastReportedMatcher());
    }
}
