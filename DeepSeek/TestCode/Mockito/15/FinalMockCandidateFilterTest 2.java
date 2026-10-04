package org.mockito.internal.configuration.injection;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FinalMockCandidateFilterTest {

    // Helper class with a non-final field for successful injection
    public static class InjectableClass {
        public Object myField;
    }

    // Helper class with a final field to force FieldSetter failure
    public static class FinalFieldClass {
        public final Object myField = null;
    }

    private final FinalMockCandidateFilter filter = new FinalMockCandidateFilter();

    @Test
    public void testFilterCandidateWithSingleMockInjectsSuccessfully() throws Exception {
        InjectableClass instance = new InjectableClass();
        Field field = InjectableClass.class.getDeclaredField("myField");
        Object mock = new Object();
        Collection<Object> mocks = Collections.singletonList(mock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, instance);
        boolean result = injecter.thenInject();

        Assert.assertTrue("thenInject should return true", result);
        Assert.assertSame("Field should be set to the mock", mock, instance.myField);
    }

    @Test
    public void testFilterCandidateWithSingleMockThrowsExceptionWhenFieldSetterFails() throws Exception {
        FinalFieldClass instance = new FinalFieldClass();
        Field field = FinalFieldClass.class.getDeclaredField("myField");
        Object mock = new Object();
        Collection<Object> mocks = Collections.singletonList(mock);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, instance);
        try {
            injecter.thenInject();
            Assert.fail("Expected MockitoException was not thrown");
        } catch (MockitoException e) {
            Assert.assertTrue("Exception message should contain field name",
                    e.getMessage().contains("myField"));
        }
    }

    @Test
    public void testFilterCandidateWithNoMocksReturnsFalse() throws Exception {
        InjectableClass instance = new InjectableClass();
        Field field = InjectableClass.class.getDeclaredField("myField");
        Collection<Object> mocks = new ArrayList<Object>();

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, instance);
        boolean result = injecter.thenInject();

        Assert.assertFalse("thenInject should return false when no mocks", result);
        Assert.assertNull("Field should remain null", instance.myField);
    }

    @Test
    public void testFilterCandidateWithMultipleMocksReturnsFalse() throws Exception {
        InjectableClass instance = new InjectableClass();
        Field field = InjectableClass.class.getDeclaredField("myField");
        Collection<Object> mocks = new ArrayList<Object>();
        mocks.add(new Object());
        mocks.add(new Object());

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, instance);
        boolean result = injecter.thenInject();

        Assert.assertFalse("thenInject should return false when multiple mocks", result);
        Assert.assertNull("Field should remain null", instance.myField);
    }
}
