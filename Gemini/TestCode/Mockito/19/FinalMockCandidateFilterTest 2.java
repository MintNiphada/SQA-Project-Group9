package org.mockito.internal.configuration.injection.filter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FinalMockCandidateFilterTest {

    private FinalMockCandidateFilter filter;

    public static class SampleTargetWithSetter {
        private String value;

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static class SampleTargetWithoutSetter {
        private String value;

        public String getValue() {
            return value;
        }
    }

    public static class SampleTargetThrowingSetter {
        private String value;

        public void setValue(String value) {
            throw new RuntimeException("Setter failed intentionally");
        }

        public String getValue() {
            return value;
        }
    }

    public static class SampleTargetWithFinalField {
        private final int primitiveValue = 0;

        public int getPrimitiveValue() {
            return primitiveValue;
        }
    }

    @Before
    public void setUp() {
        filter = new FinalMockCandidateFilter();
    }

    @Test
    public void shouldReturnNullInjecterWhenMocksCollectionIsEmpty() throws Exception {
        Field field = SampleTargetWithSetter.class.getDeclaredField("value");
        SampleTargetWithSetter target = new SampleTargetWithSetter();

        OngoingInjecter injecter = filter.filterCandidate(Collections.emptyList(), field, target);
        assertNotNull(injecter);

        Object result = injecter.thenInject();
        assertNull(result);
        assertNull(target.getValue());
    }

    @Test
    public void shouldReturnNullInjecterWhenMultipleMocksProvided() throws Exception {
        Field field = SampleTargetWithSetter.class.getDeclaredField("value");
        SampleTargetWithSetter target = new SampleTargetWithSetter();
        List<Object> mocks = Arrays.asList("mock1", "mock2");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        Object result = injecter.thenInject();
        assertNull(result);
        assertNull(target.getValue());
    }

    @Test
    public void shouldInjectViaPropertySetterWhenSetterAvailable() throws Exception {
        Field field = SampleTargetWithSetter.class.getDeclaredField("value");
        SampleTargetWithSetter target = new SampleTargetWithSetter();
        String mockValue = "injectedViaSetter";
        List<Object> mocks = Collections.singletonList((Object) mockValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        Object injectedResult = injecter.thenInject();
        assertSame(mockValue, injectedResult);
        assertEquals(mockValue, target.getValue());
    }

    @Test
    public void shouldInjectViaFieldDirectlyWhenNoSetterAvailable() throws Exception {
        Field field = SampleTargetWithoutSetter.class.getDeclaredField("value");
        SampleTargetWithoutSetter target = new SampleTargetWithoutSetter();
        String mockValue = "injectedViaField";
        List<Object> mocks = Collections.singletonList((Object) mockValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        Object injectedResult = injecter.thenInject();
        assertSame(mockValue, injectedResult);
        assertEquals(mockValue, target.getValue());
    }

    @Test
    public void shouldHandleExceptionViaReporterWhenInjectionFails() throws Exception {
        Field field = SampleTargetThrowingSetter.class.getDeclaredField("value");
        SampleTargetThrowingSetter target = new SampleTargetThrowingSetter();
        String mockValue = "mockValue";
        List<Object> mocks = Collections.singletonList((Object) mockValue);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        try {
            injecter.thenInject();
            fail("Expected exception was not thrown");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot inject"));
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Cannot inject") || e.getMessage().contains("Setter failed"));
        }
    }

    @Test
    public void shouldHandleExceptionWhenIncompatibleTypeProvided() throws Exception {
        Field field = SampleTargetWithoutSetter.class.getDeclaredField("value");
        SampleTargetWithoutSetter target = new SampleTargetWithoutSetter();
        Integer incompatibleMock = 12345;
        List<Object> mocks = Collections.singletonList((Object) incompatibleMock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        try {
            injecter.thenInject();
            fail("Expected exception when injecting incompatible type");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot inject"));
        } catch (RuntimeException e) {
            // Success if caught and handled
            assertNotNull(e);
        }
    }
}
