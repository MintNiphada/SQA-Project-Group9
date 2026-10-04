package org.mockito.internal.verification.argumentmatching;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.ContainsExtraTypeInformation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ArgumentMatchingToolTest {

    private ArgumentMatchingTool tool;

    @Before
    public void setUp() {
        tool = new ArgumentMatchingTool();
    }

    @Test
    public void shouldReturnEmptyArrayWhenSizesDoNotMatch() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new CustomMatcher("1", false, false));
        Object[] args = new Object[]{"1", "2"};
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);
        assertEquals(0, result.length);
    }

    @Test
    public void shouldReturnEmptyArrayForEmptyInputs() {
        List<Matcher> matchers = Collections.emptyList();
        Object[] args = new Object[0];
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);
        assertEquals(0, result.length);
    }

    @Test
    public void shouldNotBeSuspiciousWhenMatcherDoesNotImplementExtraTypeInfo() {
        Matcher regularMatcher = new BaseMatcher<Object>() {
            public boolean matches(Object item) {
                return false;
            }
            public void describeTo(Description description) {
                description.appendText("test");
            }
        };
        List<Matcher> matchers = Arrays.asList(regularMatcher);
        Object[] args = new Object[]{"test"};
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);
        assertEquals(0, result.length);
    }

    @Test
    public void shouldNotBeSuspiciousWhenMatchesIsTrue() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new CustomMatcher("10", true, false));
        Object[] args = new Object[]{10};
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);
        assertEquals(0, result.length);
    }

    @Test
    public void shouldNotBeSuspiciousWhenToStringDoesNotMatch() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new CustomMatcher("10", false, false));
        Object[] args = new Object[]{20};
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);
        assertEquals(0, result.length);
    }

    @Test
    public void shouldNotBeSuspiciousWhenTypeMatchesIsTrue() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new CustomMatcher("10", false, true));
        Object[] args = new Object[]{10};
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);
        assertEquals(0, result.length);
    }

    @Test
    public void shouldBeSuspiciousWhenToStringMatchesButTypesDifferAndDoesNotMatch() {
        List<Matcher> matchers = Arrays.<Matcher>asList(new CustomMatcher("10", false, false));
        Object[] args = new Object[]{10L};
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);
        assertArrayEquals(new Integer[]{0}, result);
    }

    @Test
    public void shouldHandleMatchesThrowingExceptionGracefully() {
        Matcher throwingMatcher = new CustomMatcher("10", false, false) {
            @Override
            public boolean matches(Object item) {
                throw new RuntimeException("Error during match");
            }
        };
        List<Matcher> matchers = Arrays.asList(throwingMatcher);
        Object[] args = new Object[]{10};
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);
        assertArrayEquals(new Integer[]{0}, result);
    }

    @Test
    public void shouldFindMultipleSuspiciousIndexes() {
        List<Matcher> matchers = Arrays.<Matcher>asList(
                new CustomMatcher("1", false, false),
                new CustomMatcher("2", true, true),
                new CustomMatcher("3", false, false)
        );
        Object[] args = new Object[]{1L, 2, 3L};
        Integer[] result = tool.getSuspiciouslyNotMatchingArgsIndexes(matchers, args);
        assertArrayEquals(new Integer[]{0, 2}, result);
    }

    private static class CustomMatcher extends BaseMatcher<Object> implements ContainsExtraTypeInformation {
        private final String text;
        private final boolean matchesResult;
        private final boolean typeMatchesResult;

        public CustomMatcher(String text, boolean matchesResult, boolean typeMatchesResult) {
            this.text = text;
            this.matchesResult = matchesResult;
            this.typeMatchesResult = typeMatchesResult;
        }

        public boolean matches(Object item) {
            return matchesResult;
        }

        public void describeTo(Description description) {
            description.appendText(text);
        }

        public boolean typeMatches(Object target) {
            return typeMatchesResult;
        }

        public Object withExtraTypeInfo() {
            return this;
        }
    }
}
