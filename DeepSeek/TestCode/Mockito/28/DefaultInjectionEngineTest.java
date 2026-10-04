package org.mockito.internal.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.configuration.injection.MockCandidateFilter;
import org.mockito.internal.util.reflection.FieldInitializer;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultInjectionEngineTest {

    @Mock
    private MockCandidateFilter mockCandidateFilter;

    @Mock
    private MockCandidateFilter.MockInjection mockInjection;

    private DefaultInjectionEngine engine;

    @Before
    public void setUp() throws Exception {
        engine = new DefaultInjectionEngine();
        Field filterField = DefaultInjectionEngine.class.getDeclaredField("mockCandidateFilter");
        filterField.setAccessible(true);
        filterField.set(engine, mockCandidateFilter);
        when(mockCandidateFilter.filterCandidate(anySet(), any(Field.class), any())).thenReturn(mockInjection);
    }

    @Test
    public void testInjectMocksOnFields_EmptySet() {
        Set<Field> fields = new HashSet<Field>();
        Set<Object> mocks = new HashSet<Object>();
        engine.injectMocksOnFields(fields, mocks, new Object());
        verifyNoInteractions(mockCandidateFilter);
    }

    @Test
    public void testInjectMocksOnFields_SuccessfulInjectionWithHierarchy() throws Exception {
        Field field = TestClass.class.getDeclaredField("injectMocksField");
        Set<Field> fields = new HashSet<Field>(Arrays.asList(field));
        Set<Object> mocks = new HashSet<Object>(Arrays.asList("mock1"));
        TestClass testInstance = new TestClass();
        Bottom bottomInstance = new Bottom();

        try (MockedConstruction<FieldInitializer> mocked = mockConstruction(FieldInitializer.class,
                (mock, context) -> when(mock.initialize()).thenReturn(bottomInstance))) {
            engine.injectMocksOnFields(fields, mocks, testInstance);
        }

        verify(mockCandidateFilter, times(3)).filterCandidate(anySet(), any(Field.class), eq(bottomInstance));
        verify(mockInjection, times(3)).thenInject();

        InOrder inOrder = inOrder(mockCandidateFilter);
        inOrder.verify(mockCandidateFilter).filterCandidate(anySet(), argThat(f -> f.getName().equals("bottomField")), eq(bottomInstance));
        inOrder.verify(mockCandidateFilter).filterCandidate(anySet(), argThat(f -> f.getName().equals("middleField")), eq(bottomInstance));
        inOrder.verify(mockCandidateFilter).filterCandidate(anySet(), argThat(f -> f.getName().equals("topField")), eq(bottomInstance));
    }

    @Test(expected = NullPointerException.class)
    public void testInjectMocksOnFields_FieldInitializerThrowsException() throws Exception {
        Field field = TestClass.class.getDeclaredField("injectMocksField");
        Set<Field> fields = new HashSet<Field>(Arrays.asList(field));
        Set<Object> mocks = new HashSet<Object>(Arrays.asList("mock1"));
        TestClass testInstance = new TestClass();
        MockitoException mockitoException = new MockitoException("init failure");

        try (MockedConstruction<FieldInitializer> mockedFieldInit = mockConstruction(FieldInitializer.class,
                (mock, context) -> when(mock.initialize()).thenThrow(mockitoException));
             MockedConstruction<Reporter> mockedReporter = mockConstruction(Reporter.class)) {
            engine.injectMocksOnFields(fields, mocks, testInstance);
        }

        Reporter reporter = mockedReporter.constructed().get(0);
        verify(reporter).cannotInitializeForInjectMocksAnnotation(eq("injectMocksField"), eq(mockitoException));
    }

    @Test
    public void testComparator_SupertypesLast() throws Exception {
        Field comparatorField = DefaultInjectionEngine.class.getDeclaredField("supertypesLast");
        comparatorField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Comparator<Field> comparator = (Comparator<Field>) comparatorField.get(engine);

        Field bottomField = Bottom.class.getDeclaredField("bottomField");
        Field middleField = Bottom.class.getDeclaredField("middleField");
        Field topField = Bottom.class.getDeclaredField("topField");

        assertEquals(1, comparator.compare(topField, bottomField));
        assertEquals(-1, comparator.compare(bottomField, topField));
        assertEquals(0, comparator.compare(bottomField, bottomField));
        assertEquals(1, comparator.compare(middleField, bottomField));
        assertEquals(-1, comparator.compare(bottomField, middleField));
        assertEquals(1, comparator.compare(topField, middleField));
        assertEquals(-1, comparator.compare(middleField, topField));
    }

    static class Top {
        Object topField;
    }

    static class Middle extends Top {
        Object middleField;
    }

    static class Bottom extends Middle {
        Object bottomField;
    }

    static class TestClass {
        Bottom injectMocksField;
    }
}
