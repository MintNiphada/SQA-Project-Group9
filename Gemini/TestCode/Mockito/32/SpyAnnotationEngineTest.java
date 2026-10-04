package org.mockito.internal.configuration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SpyAnnotationEngineTest {

    private SpyAnnotationEngine engine;
    private MockUtil mockUtil;

    @Before
    public void setUp() {
        engine = new SpyAnnotationEngine();
        mockUtil = new MockUtil();
    }

    @Test
    public void testCreateMockForReturnsNull() {
        assertNull(engine.createMockFor(null, null));
    }

    private static class ClassWithoutAnnotations {
        List<String> list = new ArrayList<String>();
    }

    @Test
    public void testProcessWithoutSpyAnnotation() {
        ClassWithoutAnnotations target = new ClassWithoutAnnotations();
        engine.process(ClassWithoutAnnotations.class, target);
        assertFalse(mockUtil.isMock(target.list));
    }

    private static class ClassWithSpy {
        @Spy
        List<String> list = new ArrayList<String>();
    }

    @Test
    public void testProcessWithSpyAnnotation() {
        ClassWithSpy target = new ClassWithSpy();
        engine.process(ClassWithSpy.class, target);
        assertTrue(mockUtil.isMock(target.list));
    }

    private static class ClassWithPreExistingMockSpy {
        @Spy
        List<String> list = Mockito.spy(new ArrayList<String>());
    }

    @Test
    public void testProcessWithExistingMockSpy() {
        ClassWithPreExistingMockSpy target = new ClassWithPreExistingMockSpy();
        List<String> originalSpy = target.list;
        originalSpy.add("test");
        engine.process(ClassWithPreExistingMockSpy.class, target);
        assertTrue(mockUtil.isMock(target.list));
        assertEquals(originalSpy, target.list);
    }

    private static class ClassWithNullSpy {
        @Spy
        List<String> list;
    }

    @Test
    public void testProcessWithNullSpyFieldThrowsException() {
        ClassWithNullSpy target = new ClassWithNullSpy();
        try {
            engine.process(ClassWithNullSpy.class, target);
            fail("Expected MockitoException when @Spy field is null");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Cannot create a @Spy for 'list' field"));
        }
    }

    private static class ClassWithSpyAndMock {
        @Spy
        @Mock
        List<String> list = new ArrayList<String>();
    }

    @Test(expected = MockitoException.class)
    public void testProcessWithSpyAndMockAnnotations() {
        ClassWithSpyAndMock target = new ClassWithSpyAndMock();
        engine.process(ClassWithSpyAndMock.class, target);
    }

    private static class ClassWithSpyAndDeprecatedMock {
        @Spy
        @org.mockito.MockitoAnnotations.Mock
        List<String> list = new ArrayList<String>();
    }

    @Test(expected = MockitoException.class)
    @SuppressWarnings("deprecation")
    public void testProcessWithSpyAndDeprecatedMockAnnotations() {
        ClassWithSpyAndDeprecatedMock target = new ClassWithSpyAndDeprecatedMock();
        engine.process(ClassWithSpyAndDeprecatedMock.class, target);
    }

    private static class ClassWithSpyAndCaptor {
        @Spy
        @Captor
        ArgumentCaptor<String> captor;
    }

    @Test(expected = MockitoException.class)
    public void testProcessWithSpyAndCaptorAnnotations() {
        ClassWithSpyAndCaptor target = new ClassWithSpyAndCaptor();
        engine.process(ClassWithSpyAndCaptor.class, target);
    }

    private static class ClassWithPrivateSpy {
        @Spy
        private List<String> list = new ArrayList<String>();
    }

    @Test
    public void testAccessibilityRestored() throws Exception {
        ClassWithPrivateSpy target = new ClassWithPrivateSpy();
        Field field = ClassWithPrivateSpy.class.getDeclaredField("list");
        assertFalse(field.isAccessible());
        engine.process(ClassWithPrivateSpy.class, target);
        assertFalse(field.isAccessible());
        field.setAccessible(true);
        assertTrue(mockUtil.isMock(field.get(target)));
    }

    @Test
    public void testAssertNoAnnotationsWithNoConflicts() throws Exception {
        Field field = ClassWithoutAnnotations.class.getDeclaredField("list");
        engine.assertNoAnnotations(Spy.class, field, Mock.class, Captor.class);
    }

    @Test(expected = MockitoException.class)
    public void testAssertNoAnnotationsThrowsWhenConflict() throws Exception {
        Field field = ClassWithSpyAndMock.class.getDeclaredField("list");
        engine.assertNoAnnotations(Spy.class, field, Mock.class, Captor.class);
    }
}
