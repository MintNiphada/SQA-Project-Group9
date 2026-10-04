package org.mockito.internal.configuration.injection;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class PropertyAndSetterInjectionTest {

    private PropertyAndSetterInjection injection;

    public interface DependencyService {
        void execute();
    }

    public interface OtherDependency {
        void run();
    }

    public static class SuperTarget {
        private DependencyService superService;

        public DependencyService getSuperService() {
            return superService;
        }
    }

    public static class SubTarget extends SuperTarget {
        public static String staticField = "static";
        public final String finalField = "final";

        private DependencyService subService;
        private OtherDependency otherDependency;
        private boolean setterCalled = false;

        public void setOtherDependency(OtherDependency otherDependency) {
            this.otherDependency = otherDependency;
            this.setterCalled = true;
        }

        public DependencyService getSubService() {
            return subService;
        }

        public OtherDependency getOtherDependency() {
            return otherDependency;
        }

        public boolean isSetterCalled() {
            return setterCalled;
        }
    }

    public static class SameTypeMultipleFieldsTarget {
        private DependencyService firstService;
        private DependencyService secondService;

        public DependencyService getFirstService() {
            return firstService;
        }

        public DependencyService getSecondService() {
            return secondService;
        }
    }

    public static class FailingConstructorTarget {
        public FailingConstructorTarget() {
            throw new RuntimeException("Constructor failed intentionally");
        }
    }

    public static class NoDefaultConstructorTarget {
        public NoDefaultConstructorTarget(String arg) {
        }
    }

    public static class ContainerWithPreInitializedField {
        private SubTarget target = new SubTarget();
    }

    public static class ContainerWithUninitializedField {
        private SubTarget target;
    }

    public static class ContainerWithMultipleSameTypeFields {
        private SameTypeMultipleFieldsTarget target = new SameTypeMultipleFieldsTarget();
    }

    public static class ContainerWithFailingConstructorField {
        private FailingConstructorTarget target;
    }

    public static class ContainerWithNoDefaultConstructorField {
        private NoDefaultConstructorTarget target;
    }

    public static class ContainerWithNoInjectableFields {
        private Object target = new Object();
    }

    @Before
    public void setUp() {
        injection = new PropertyAndSetterInjection();
    }

    @Test
    public void shouldInjectMocksIntoFieldsAndViaSetter() throws Exception {
        ContainerWithPreInitializedField container = new ContainerWithPreInitializedField();
        Field targetField = ContainerWithPreInitializedField.class.getDeclaredField("target");

        DependencyService mockService = Mockito.mock(DependencyService.class);
        OtherDependency mockOther = Mockito.mock(OtherDependency.class);

        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockService);
        mocks.add(mockOther);

        boolean result = injection.processInjection(targetField, container, mocks);

        assertTrue("Injection should return true when candidates are injected", result);
        assertNotNull(container.target.getSubService());
        assertNotNull(container.target.getOtherDependency());
        assertTrue("Setter should have been used for otherDependency", container.target.isSetterCalled());
        assertEquals(mockOther, container.target.getOtherDependency());
    }

    @Test
    public void shouldInjectIntoSuperClassFields() throws Exception {
        ContainerWithPreInitializedField container = new ContainerWithPreInitializedField();
        Field targetField = ContainerWithPreInitializedField.class.getDeclaredField("target");

        DependencyService mockService = Mockito.mock(DependencyService.class);
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockService);

        boolean result = injection.processInjection(targetField, container, mocks);

        assertTrue("Injection should occur in hierarchy", result);
        assertNotNull(container.target.getSubService());
    }

    @Test
    public void shouldInstantiateFieldIfNotInitialized() throws Exception {
        ContainerWithUninitializedField container = new ContainerWithUninitializedField();
        Field targetField = ContainerWithUninitializedField.class.getDeclaredField("target");

        DependencyService mockService = Mockito.mock(DependencyService.class);
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(mockService);

        assertNull(container.target);
        boolean result = injection.processInjection(targetField, container, mocks);

        assertTrue("Injection should succeed on instantiated field", result);
        assertNotNull("Target field should have been instantiated", container.target);
        assertNotNull("Dependency should be injected into new instance", container.target.getSubService());
    }

    @Test
    public void shouldReturnFalseWhenNoCandidatesMatch() throws Exception {
        ContainerWithPreInitializedField container = new ContainerWithPreInitializedField();
        Field targetField = ContainerWithPreInitializedField.class.getDeclaredField("target");

        Set<Object> mocks = new HashSet<Object>();
        mocks.add("A string that doesn't match any dependency type");

        boolean result = injection.processInjection(targetField, container, mocks);

        assertFalse("Injection should return false when no candidate matches", result);
        assertNull(container.target.getSubService());
        assertNull(container.target.getOtherDependency());
    }

    @Test
    public void shouldReturnFalseWhenCandidateSetIsEmpty() throws Exception {
        ContainerWithPreInitializedField container = new ContainerWithPreInitializedField();
        Field targetField = ContainerWithPreInitializedField.class.getDeclaredField("target");

        boolean result = injection.processInjection(targetField, container, Collections.emptySet());

        assertFalse("Injection should return false with empty candidates", result);
    }

    @Test
    public void shouldIgnoreStaticAndFinalFields() throws Exception {
        ContainerWithPreInitializedField container = new ContainerWithPreInitializedField();
        Field targetField = ContainerWithPreInitializedField.class.getDeclaredField("target");

        Set<Object> mocks = new HashSet<Object>();
        mocks.add("new_value");

        injection.processInjection(targetField, container, mocks);

        assertEquals("static", SubTarget.staticField);
        assertEquals("final", container.target.finalField);
    }

    @Test
    public void shouldInjectByNameWhenMultipleCandidatesHaveSameType() throws Exception {
        ContainerWithMultipleSameTypeFields container = new ContainerWithMultipleSameTypeFields();
        Field targetField = ContainerWithMultipleSameTypeFields.class.getDeclaredField("target");

        DependencyService firstMock = Mockito.mock(DependencyService.class, "firstService");
        DependencyService secondMock = Mockito.mock(DependencyService.class, "secondService");

        Set<Object> mocks = new HashSet<Object>();
        mocks.add(firstMock);
        mocks.add(secondMock);

        boolean result = injection.processInjection(targetField, container, mocks);

        assertTrue("Injection should succeed for multiple candidates", result);
        assertSame(firstMock, container.target.getFirstService());
        assertSame(secondMock, container.target.getSecondService());
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionWhenConstructorThrowsException() throws Exception {
        ContainerWithFailingConstructorField container = new ContainerWithFailingConstructorField();
        Field targetField = ContainerWithFailingConstructorField.class.getDeclaredField("target");

        injection.processInjection(targetField, container, Collections.emptySet());
    }

    @Test(expected = MockitoException.class)
    public void shouldThrowMockitoExceptionWhenNoDefaultConstructorAvailable() throws Exception {
        ContainerWithNoDefaultConstructorField container = new ContainerWithNoDefaultConstructorField();
        Field targetField = ContainerWithNoDefaultConstructorField.class.getDeclaredField("target");

        injection.processInjection(targetField, container, Collections.emptySet());
    }

    @Test
    public void shouldHandleObjectWithNoSuperClassHierarchyBeyondObject() throws Exception {
        ContainerWithNoInjectableFields container = new ContainerWithNoInjectableFields();
        Field targetField = ContainerWithNoInjectableFields.class.getDeclaredField("target");

        boolean result = injection.processInjection(targetField, container, Collections.emptySet());

        assertFalse("Should return false when Object has no injectable fields", result);
    }
}
