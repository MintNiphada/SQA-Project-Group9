package org.mockito.internal.configuration.injection.filter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TypeBasedCandidateFilterTest {

    private static class SampleTarget {
        private CharSequence charSequenceField;
        private String stringField;
        private Integer integerField;
        private List<?> listField;
    }

    private static class DummyOngoingInjecter implements OngoingInjecter {
        private final Object injected;

        public DummyOngoingInjecter(Object injected) {
            this.injected = injected;
        }

        public boolean thenInject() {
            return injected != null;
        }
    }

    private static class CapturingMockCandidateFilter implements MockCandidateFilter {
        Collection<Object> capturedMocks;
        Field capturedField;
        Object capturedFieldInstance;
        OngoingInjecter injecterToReturn = new DummyOngoingInjecter("test");

        public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
            this.capturedMocks = mocks;
            this.capturedField = field;
            this.capturedFieldInstance = fieldInstance;
            return injecterToReturn;
        }
    }

    private CapturingMockCandidateFilter nextFilter;
    private TypeBasedCandidateFilter filter;
    private SampleTarget sampleTarget;

    @Before
    public void setUp() {
        nextFilter = new CapturingMockCandidateFilter();
        filter = new TypeBasedCandidateFilter(nextFilter);
        sampleTarget = new SampleTarget();
    }

    @Test
    public void shouldPassMatchingTypesToNextFilter() throws NoSuchFieldException {
        Field field = SampleTarget.class.getDeclaredField("charSequenceField");
        String stringMock = "hello";
        StringBuilder sbMock = new StringBuilder("world");
        Integer intMock = 123;

        List<Object> mocks = Arrays.asList(stringMock, intMock, sbMock);

        OngoingInjecter result = filter.filterCandidate(mocks, field, sampleTarget);

        Assert.assertSame(nextFilter.injecterToReturn, result);
        Assert.assertSame(field, nextFilter.capturedField);
        Assert.assertSame(sampleTarget, nextFilter.capturedFieldInstance);
        Assert.assertNotNull(nextFilter.capturedMocks);
        Assert.assertEquals(2, nextFilter.capturedMocks.size());
        Assert.assertTrue(nextFilter.capturedMocks.contains(stringMock));
        Assert.assertTrue(nextFilter.capturedMocks.contains(sbMock));
        Assert.assertFalse(nextFilter.capturedMocks.contains(intMock));
    }

    @Test
    public void shouldHandleExactTypeMatch() throws NoSuchFieldException {
        Field field = SampleTarget.class.getDeclaredField("stringField");
        String stringMock1 = "abc";
        String stringMock2 = "def";
        Integer intMock = 456;

        List<Object> mocks = Arrays.asList(stringMock1, intMock, stringMock2);

        OngoingInjecter result = filter.filterCandidate(mocks, field, sampleTarget);

        Assert.assertSame(nextFilter.injecterToReturn, result);
        Assert.assertEquals(2, nextFilter.capturedMocks.size());
        Assert.assertTrue(nextFilter.capturedMocks.contains(stringMock1));
        Assert.assertTrue(nextFilter.capturedMocks.contains(stringMock2));
    }

    @Test
    public void shouldHandleSubtypeMatches() throws NoSuchFieldException {
        Field field = SampleTarget.class.getDeclaredField("listField");
        ArrayList<String> arrayListMock = new ArrayList<String>();
        String stringMock = "not a list";

        List<Object> mocks = Arrays.asList(arrayListMock, stringMock);

        filter.filterCandidate(mocks, field, sampleTarget);

        Assert.assertEquals(1, nextFilter.capturedMocks.size());
        Assert.assertTrue(nextFilter.capturedMocks.contains(arrayListMock));
    }

    @Test
    public void shouldPassEmptyListWhenNoTypesMatch() throws NoSuchFieldException {
        Field field = SampleTarget.class.getDeclaredField("integerField");
        String stringMock = "not an integer";
        Double doubleMock = 3.14;

        List<Object> mocks = Arrays.asList(stringMock, doubleMock);

        OngoingInjecter result = filter.filterCandidate(mocks, field, sampleTarget);

        Assert.assertSame(nextFilter.injecterToReturn, result);
        Assert.assertTrue(nextFilter.capturedMocks.isEmpty());
    }

    @Test
    public void shouldHandleEmptyMocksCollection() throws NoSuchFieldException {
        Field field = SampleTarget.class.getDeclaredField("integerField");
        Collection<Object> mocks = Collections.emptyList();

        OngoingInjecter result = filter.filterCandidate(mocks, field, sampleTarget);

        Assert.assertSame(nextFilter.injecterToReturn, result);
        Assert.assertTrue(nextFilter.capturedMocks.isEmpty());
    }

    @Test
    public void shouldPassNullFieldInstanceToNextFilter() throws NoSuchFieldException {
        Field field = SampleTarget.class.getDeclaredField("stringField");
        String stringMock = "test";

        filter.filterCandidate(Collections.singletonList((Object) stringMock), field, null);

        Assert.assertNull(nextFilter.capturedFieldInstance);
        Assert.assertEquals(1, nextFilter.capturedMocks.size());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenFieldIsNull() {
        List<Object> mocks = Collections.singletonList((Object) "test");
        filter.filterCandidate(mocks, null, sampleTarget);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenNextFilterIsNull() throws NoSuchFieldException {
        TypeBasedCandidateFilter filterWithNullNext = new TypeBasedCandidateFilter(null);
        Field field = SampleTarget.class.getDeclaredField("stringField");

        filterWithNullNext.filterCandidate(Collections.emptyList(), field, sampleTarget);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenMockListContainsNull() throws NoSuchFieldException {
        Field field = SampleTarget.class.getDeclaredField("stringField");
        List<Object> mocks = Collections.singletonList(null);

        filter.filterCandidate(mocks, field, sampleTarget);
    }
}
