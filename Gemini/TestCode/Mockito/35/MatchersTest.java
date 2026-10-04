package org.mockito;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.progress.MockingProgress;
import org.mockito.internal.progress.ThreadSafeMockingProgress;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchersTest {

    private MockingProgress mockingProgress;

    private static class DummyMatcher<T> extends BaseMatcher<T> {
        public boolean matches(Object item) {
            return true;
        }

        public void describeTo(Description description) {
            description.appendText("dummy");
        }
    }

    @Before
    public void setUp() {
        mockingProgress = new ThreadSafeMockingProgress();
        mockingProgress.reset();
    }

    @After
    public void tearDown() {
        mockingProgress.reset();
    }

    @Test
    public void testInstantiation() {
        Matchers matchers = new Matchers();
        Assert.assertNotNull(matchers);
    }

    @Test
    public void testAnyBoolean() {
        boolean result = Matchers.anyBoolean();
        Assert.assertFalse(result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyByte() {
        byte result = Matchers.anyByte();
        Assert.assertEquals((byte) 0, result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyChar() {
        char result = Matchers.anyChar();
        Assert.assertEquals((char) 0, result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyInt() {
        int result = Matchers.anyInt();
        Assert.assertEquals(0, result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyLong() {
        long result = Matchers.anyLong();
        Assert.assertEquals(0L, result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyFloat() {
        float result = Matchers.anyFloat();
        Assert.assertEquals(0.0f, result, 0.0001f);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyDouble() {
        double result = Matchers.anyDouble();
        Assert.assertEquals(0.0, result, 0.0001);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyShort() {
        short result = Matchers.anyShort();
        Assert.assertEquals((short) 0, result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyObject() {
        Object result = Matchers.anyObject();
        Assert.assertNull(result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyVararg() {
        Object result = Matchers.anyVararg();
        Assert.assertNull(result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyClass() {
        String result = Matchers.any(String.class);
        Assert.assertNull(result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAny() {
        Object result = Matchers.any();
        Assert.assertNull(result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyString() {
        String result = Matchers.anyString();
        Assert.assertEquals("", result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyList() {
        List result = Matchers.anyList();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyListOf() {
        List<String> result = Matchers.anyListOf(String.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnySet() {
        Set result = Matchers.anySet();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnySetOf() {
        Set<Integer> result = Matchers.anySetOf(Integer.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyMap() {
        Map result = Matchers.anyMap();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyCollection() {
        Collection result = Matchers.anyCollection();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testAnyCollectionOf() {
        Collection<Double> result = Matchers.anyCollectionOf(Double.class);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isEmpty());
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testIsA() {
        String result = Matchers.isA(String.class);
        Assert.assertNull(result);
        Assert.assertNotNull(mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers());
    }

    @Test
    public void testEqBoolean() {
        boolean resultTrue = Matchers.eq(true);
        Assert.assertFalse(resultTrue);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());

        boolean resultFalse = Matchers.eq(false);
        Assert.assertFalse(resultFalse);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testEqByte() {
        byte result = Matchers.eq((byte) 123);
        Assert.assertEquals((byte) 0, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testEqChar() {
        char result = Matchers.eq('x');
        Assert.assertEquals((char) 0, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testEqDouble() {
        double result = Matchers.eq(3.14159);
        Assert.assertEquals(0.0, result, 0.0001);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testEqFloat() {
        float result = Matchers.eq(2.718f);
        Assert.assertEquals(0.0f, result, 0.0001f);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testEqInt() {
        int result = Matchers.eq(42);
        Assert.assertEquals(0, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testEqLong() {
        long result = Matchers.eq(999999L);
        Assert.assertEquals(0L, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testEqShort() {
        short result = Matchers.eq((short) 10);
        Assert.assertEquals((short) 0, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testEqObject() {
        String result = Matchers.eq("target");
        Assert.assertNull(result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());

        Object nullObj = Matchers.eq((Object) null);
        Assert.assertNull(nullObj);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testRefEq() {
        String result = Matchers.refEq("target", "field1", "field2");
        Assert.assertNull(result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testSame() {
        Object obj = new Object();
        Object result = Matchers.same(obj);
        Assert.assertNull(result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testIsNull() {
        Object result = Matchers.isNull();
        Assert.assertNull(result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testNotNull() {
        Object result = Matchers.notNull();
        Assert.assertNull(result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testIsNotNull() {
        Object result = Matchers.isNotNull();
        Assert.assertNull(result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testContains() {
        String result = Matchers.contains("sub");
        Assert.assertEquals("", result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testMatches() {
        String result = Matchers.matches(".*regex.*");
        Assert.assertEquals("", result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testEndsWith() {
        String result = Matchers.endsWith("suffix");
        Assert.assertEquals("", result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testStartsWith() {
        String result = Matchers.startsWith("prefix");
        Assert.assertEquals("", result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testArgThat() {
        Matcher<String> matcher = new DummyMatcher<String>();
        String result = Matchers.argThat(matcher);
        Assert.assertNull(result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testCharThat() {
        Matcher<Character> matcher = new DummyMatcher<Character>();
        char result = Matchers.charThat(matcher);
        Assert.assertEquals((char) 0, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testBooleanThat() {
        Matcher<Boolean> matcher = new DummyMatcher<Boolean>();
        boolean result = Matchers.booleanThat(matcher);
        Assert.assertFalse(result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testByteThat() {
        Matcher<Byte> matcher = new DummyMatcher<Byte>();
        byte result = Matchers.byteThat(matcher);
        Assert.assertEquals((byte) 0, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testShortThat() {
        Matcher<Short> matcher = new DummyMatcher<Short>();
        short result = Matchers.shortThat(matcher);
        Assert.assertEquals((short) 0, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testIntThat() {
        Matcher<Integer> matcher = new DummyMatcher<Integer>();
        int result = Matchers.intThat(matcher);
        Assert.assertEquals(0, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testLongThat() {
        Matcher<Long> matcher = new DummyMatcher<Long>();
        long result = Matchers.longThat(matcher);
        Assert.assertEquals(0L, result);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testFloatThat() {
        Matcher<Float> matcher = new DummyMatcher<Float>();
        float result = Matchers.floatThat(matcher);
        Assert.assertEquals(0.0f, result, 0.0001f);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }

    @Test
    public void testDoubleThat() {
        Matcher<Double> matcher = new DummyMatcher<Double>();
        double result = Matchers.doubleThat(matcher);
        Assert.assertEquals(0.0, result, 0.0001);
        Assert.assertEquals(1, mockingProgress.getArgumentMatcherStorage().pullLocalizedMatchers().size());
    }
}
