package org.mockito.internal.configuration.injection.filter;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MockCandidateFilterTest {

    private static class SampleTarget {
        private String injectedField;
    }

    @Test
    public void testInterfaceDefinition() {
        Assert.assertTrue("MockCandidateFilter should be an interface", MockCandidateFilter.class.isInterface());
    }

    @Test
    public void testMethodSignature() throws NoSuchMethodException {
        Method method = MockCandidateFilter.class.getMethod(
                "filterCandidate",
                Collection.class,
                Field.class,
                Object.class
        );

        Assert.assertNotNull("filterCandidate method should exist", method);
        Assert.assertEquals("Return type must be OngoingInjecter", OngoingInjecter.class, method.getReturnType());
    }

    @Test
    public void testImplementationContractWithNonNullReturns() throws Exception {
        final OngoingInjecter expectedInjecter = new OngoingInjecter() {
            @Override
            public Object thenInject() {
                return "injected";
            }
        };

        MockCandidateFilter filter = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (mocks != null && fieldToBeInjected != null && fieldInstance != null) {
                    return expectedInjecter;
                }
                return OngoingInjecter.stub;
            }
        };

        SampleTarget target = new SampleTarget();
        Field field = SampleTarget.class.getDeclaredField("injectedField");
        List<Object> mocks = new ArrayList<Object>();
        mocks.add("mockString");

        OngoingInjecter result = filter.filterCandidate(mocks, field, target);
        Assert.assertNotNull(result);
        Assert.assertSame(expectedInjecter, result);
        Assert.assertEquals("injected", result.thenInject());
    }

    @Test
    public void testImplementationContractWithNullArguments() throws Exception {
        MockCandidateFilter filter = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                if (mocks == null || fieldToBeInjected == null || fieldInstance == null) {
                    return OngoingInjecter.stub;
                }
                return null;
            }
        };

        OngoingInjecter resultNullMocks = filter.filterCandidate(null, SampleTarget.class.getDeclaredField("injectedField"), new SampleTarget());
        Assert.assertSame(OngoingInjecter.stub, resultNullMocks);
        Assert.assertNull(resultNullMocks.thenInject());

        OngoingInjecter resultNullField = filter.filterCandidate(Collections.emptyList(), null, new SampleTarget());
        Assert.assertSame(OngoingInjecter.stub, resultNullField);

        OngoingInjecter resultNullInstance = filter.filterCandidate(Collections.emptyList(), SampleTarget.class.getDeclaredField("injectedField"), null);
        Assert.assertSame(OngoingInjecter.stub, resultNullInstance);
    }
}
