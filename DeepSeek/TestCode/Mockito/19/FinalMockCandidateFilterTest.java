package org.mockito.internal.configuration.injection.filter;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.mockito.exceptions.base.MockitoException;

public class FinalMockCandidateFilterTest {

    private FinalMockCandidateFilter filter;

    @Before
    public void setUp() {
        filter = new FinalMockCandidateFilter();
    }

    public static class BeanWithSetter {
        private String value;
        public void setValue(String value) { this.value = value; }
        public String getValue() { return value; }
    }

    public static class BeanWithFieldOnly {
        public String value;
    }

    public static class BeanWithThrowingSetter {
        private String value;
        public void setValue(String value) { throw new RuntimeException("setter fails"); }
        public String getValue() { return value; }
    }

    public static class BeanWithFinalField {
        public final String value = "original";
    }

    @Test
    public void should_inject_via_property_setter_when_one_mock_present_and_setter_wrks() throws Exception {
        BeanWithSetter bean = new BeanWithSetter();
        Field field = BeanWithSetter.class.getDeclaredField("value");
        Collection<Object> mocks = Collections.singletonList("injectedValue");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, bean);
        Object result = injecter.thenInject();

        assertEquals("injectedValue", result);
        assertEquals("injectedValue", bean.getValue());
    }

    @Test
    public void should_fallback_to_field_setter_when_property_setter_fails() throws Exception {
        BeanWithFieldOnly bean = new BeanWithFieldOnly();
        Field field = BeanWithFieldOnly.class.getDeclaredField("value");
        Collection<Object> mocks = Collections.singletonList("fieldValue");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, bean);
        Object result = injecter.thenInject();

        assertEquals("fieldValue", result);
        assertEquals("fieldValue", bean.value);
    }

    @Test(expected = MockitoException.class)
    public void should_throw_exception_via_reporter_when_property_setter_throws_runtime_exception() throws Exception {
        BeanWithThrowingSetter bean = new BeanWithThrowingSetter();
        Field field = BeanWithThrowingSetter.class.getDeclaredField("value");
        Collection<Object> mocks = Collections.singletonList("dummy");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, bean);
        injecter.thenInject(); // should throw MockitoException
    }

    @Test(expected = MockitoException.class)
    public void should_throw_exception_via_reporter_when_field_setter_throws_runtime_exception() throws Exception {
        BeanWithFinalField bean = new BeanWithFinalField();
        Field field = BeanWithFinalField.class.getDeclaredField("value");
        Collection<Object> mocks = Collections.singletonList("newValue");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, bean);
        injecter.thenInject(); // should throw MockitoException
    }

    @Test
    public void should_return_injecter_that_returns_null_wen_mocks_empty() throws Exception {
        Field field = BeanWithSetter.class.getDeclaredField("value");
        Object instance = new BeanWithSetter();

        OngoingInjecter injecter = filter.filterCandidate(Collections.emptyList(), field, instance);
        assertNull(injecter.thenInject());
    }

    @Test
    public void should_return_injecter_that_returns_null_wen_mocks_has_more_than_one() throws Exception {
        Field field = BeanWithSetter.class.getDeclaredField("value");
        Object instance = new BeanWithSetter();
        List<Object> mocks = new ArrayList<Object>();
        mocks.add("mock1");
        mocks.add("mock2");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, instance);
        assertNull(injecter.thenInject());
    }

    @Test(expected = NullPointerException.class)
    public void should_throw_npe_wen_mocks_is_null() throws Exception {
        Field field = BeanWithSetter.class.getDeclaredField("value");
        Object instance = new BeanWithSetter();
        filter.filterCandidate(null, field, instance);
    }

    @Test(expected = NullPointerException.class)
    public void should_throw_npe_wen_thenInject_called_with_null_field() throws Exception {
        Collection<Object> mocks = Collections.singletonList("x");
        Object instance = new BeanWithSetter();

        OngoingInjecter injecter = filter.filterCandidate(mocks, null, instance);
        injecter.thenInject(); // NPE inside BeanPropertySetter or FieldSetter
    }

    @Test(expected = NullPointerException.class)
    public void should_throw_npe_wen_thenInject_called_with_null_instance() throws Exception {
        Field field = BeanWithSetter.class.getDeclaredField("value");
        Collection<Object> mocks = Collections.singletonList("x");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, null);
        injecter.thenInject(); // NPE inside BeanPropertySetter
    }
}
