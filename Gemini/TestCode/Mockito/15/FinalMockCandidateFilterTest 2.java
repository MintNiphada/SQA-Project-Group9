package org.mockito.internal.configuration.injection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class FinalMockCandidateFilterTest {

    private FinalMockCandidateFilter filter;

    private static class SampleTarget {
        private String stringField;
        private List<?> listField;
    }

    @Before
    public void setUp() {
        filter = new FinalMockCandidateFilter();
    }

    @Test
    public void shouldReturnFalseWhenMocksCollectionIsEmpty() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Collections.emptyList();

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        assertNotNull(injecter);
        boolean result = injecter.thenInject();
        assertFalse(result);
        assertNull(target.stringField);
    }

    @Test
    public void shouldReturnFalseWhenMultipleMocksProvided() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Arrays.asList("mock1", "mock2");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        assertNotNull(injecter);
        boolean result = injecter.thenInject();
        assertFalse(result);
        assertNull(target.stringField);
    }

    @Test
    public void shouldInjectMockSuccessfullyWhenSingleMatchingMockProvided() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        String mockInstance = "injectedValue";
        List<Object> mocks = Collections.singletonList(mockInstance);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        assertNotNull(injecter);
        boolean result = injecter.thenInject();
        assertTrue(result);
        assertEquals("injectedValue", target.stringField);
    }

    @Test
    public void shouldInjectMockIntoListField() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("listField");
        List<String> mockList = new ArrayList<String>();
        mockList.add("element");
        List<Object> mocks = Collections.singletonList(mockList);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        assertNotNull(injecter);
        boolean result = injecter.thenInject();
        assertTrue(result);
        assertSame(mockList, target.listField);
    }

    @Test
    public void shouldThrowMockitoExceptionWhenInjectionFailsDueToTypeMismatch() throws Exception {
        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("stringField");
        Integer incompatibleMock = 12345;
        List<Object> mocks = Collections.singletonList(incompatibleMock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        try {
            injecter.thenInject();
            fail("Expected MockitoException to be thrown due to type mismatch");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Problems injecting dependency in stringField"));
            assertNotNull(e.getCause());
        }
    }

    @Test
    public void shouldThrowMockitoExceptionWhenTargetInstanceIsNull() throws Exception {
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Collections.singletonList("mockValue");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, null);
        assertNotNull(injecter);

        try {
            injecter.thenInject();
            fail("Expected MockitoException when target instance is null");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Problems injecting dependency in stringField"));
        }
    }
}
