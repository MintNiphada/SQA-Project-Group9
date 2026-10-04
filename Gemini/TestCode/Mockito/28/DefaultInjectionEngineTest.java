package org.mockito.internal.configuration;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefaultInjectionEngineTest {

    private final DefaultInjectionEngine injectionEngine = new DefaultInjectionEngine();

    static class SuperDependency {}
    static class SubDependency extends SuperDependency {}
    static class UnrelatedDependency {}

    static class TargetWithHierarchy {
        SuperDependency superDep;
        SubDependency subDep;
        UnrelatedDependency unrelatedDep;
    }

    static class TestClassHierarchy {
        TargetWithHierarchy target;
    }

    static class ParentTarget {
        SuperDependency parentDep;
    }

    static class ChildTarget extends ParentTarget {
        SubDependency childDep;
    }

    static class TestClassInheritance {
        ChildTarget target;
    }

    static class UninstantiableTarget {
        public UninstantiableTarget(String requiredArg) {}
    }

    static class TestClassUninstantiable {
        UninstantiableTarget target;
    }

    static class SameTypeTarget {
        UnrelatedDependency first;
        UnrelatedDependency second;
    }

    static class TestClassSameType {
        SameTypeTarget target;
    }

    @Test
    public void shouldInjectMocksWithSubtypesPrioritizedOverSupertypes() throws Exception {
        TestClassHierarchy testInstance = new TestClassHierarchy();
        testInstance.target = new TargetWithHierarchy();

        SuperDependency superInstance = new SuperDependency();
        SubDependency subInstance = new SubDependency();
        UnrelatedDependency unrelatedInstance = new UnrelatedDependency();

        Set<Object> mocks = new HashSet<Object>();
        mocks.add(superInstance);
        mocks.add(subInstance);
        mocks.add(unrelatedInstance);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(TestClassHierarchy.class.getDeclaredField("target"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        Assert.assertSame(subInstance, testInstance.target.subDep);
        Assert.assertSame(superInstance, testInstance.target.superDep);
        Assert.assertSame(unrelatedInstance, testInstance.target.unrelatedDep);
    }

    @Test
    public void shouldInjectIntoSuperClassFields() throws Exception {
        TestClassInheritance testInstance = new TestClassInheritance();
        testInstance.target = new ChildTarget();

        SuperDependency superInstance = new SuperDependency();
        SubDependency subInstance = new SubDependency();

        Set<Object> mocks = new HashSet<Object>();
        mocks.add(superInstance);
        mocks.add(subInstance);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(TestClassInheritance.class.getDeclaredField("target"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        Assert.assertSame(subInstance, testInstance.target.childDep);
        Assert.assertSame(superInstance, testInstance.target.parentDep);
    }

    @Test
    public void shouldInitializeTargetFieldIfNull() throws Exception {
        TestClassHierarchy testInstance = new TestClassHierarchy();
        Assert.assertNull(testInstance.target);

        SuperDependency superInstance = new SuperDependency();
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(superInstance);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(TestClassHierarchy.class.getDeclaredField("target"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        Assert.assertNotNull(testInstance.target);
        Assert.assertSame(superInstance, testInstance.target.superDep);
    }

    @Test(expected = MockitoException.class)
    public void shouldReportExceptionWhenTargetCannotBeInitialized() throws Exception {
        TestClassUninstantiable testInstance = new TestClassUninstantiable();
        Set<Object> mocks = new HashSet<Object>();
        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(TestClassUninstantiable.class.getDeclaredField("target"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);
    }

    @Test
    public void shouldHandleEmptyInjectMocksFields() {
        TestClassHierarchy testInstance = new TestClassHierarchy();
        Set<Object> mocks = new HashSet<Object>();
        Set<Field> injectMocksFields = Collections.emptySet();

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);
        Assert.assertNull(testInstance.target);
    }

    @Test
    public void shouldHandleEmptyMocksSet() throws Exception {
        TestClassHierarchy testInstance = new TestClassHierarchy();
        testInstance.target = new TargetWithHierarchy();

        Set<Object> mocks = Collections.emptySet();
        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(TestClassHierarchy.class.getDeclaredField("target"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        Assert.assertNull(testInstance.target.superDep);
        Assert.assertNull(testInstance.target.subDep);
        Assert.assertNull(testInstance.target.unrelatedDep);
    }

    @Test
    public void shouldSortFieldsWithSameTypeCorrectly() throws Exception {
        TestClassSameType testInstance = new TestClassSameType();
        testInstance.target = new SameTypeTarget();

        UnrelatedDependency firstDep = new UnrelatedDependency();
        Set<Object> mocks = new HashSet<Object>();
        mocks.add(firstDep);

        Set<Field> injectMocksFields = new HashSet<Field>();
        injectMocksFields.add(TestClassSameType.class.getDeclaredField("target"));

        injectionEngine.injectMocksOnFields(injectMocksFields, mocks, testInstance);

        boolean injected = testInstance.target.first != null || testInstance.target.second != null;
        Assert.assertTrue(injected);
    }
}
