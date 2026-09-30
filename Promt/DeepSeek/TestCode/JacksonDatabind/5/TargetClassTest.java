package com.fasterxml.jackson.databind.introspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector.MixInResolver;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.ClassUtil;

/**
 * Unit tests for {@link AnnotatedClass}.
 */
public class AnnotatedClassTest {

    private AnnotationIntrospector mockIntrospector;
    private MixInResolver mockMixInResolver;

    // Test Classes
    public static class SimpleClass {
        public String publicField;
        private String privateField;
        public static String staticField;
        public transient String transientField;

        public SimpleClass() {}
        public SimpleClass(String arg) {}

        public void noArgsMethod() {}
        public void oneArgMethod(String arg) {}
        public void twoArgsMethod(String a, String b) {}
        public void threeArgsMethod(String a, String b, String c) {}
        public static void staticMethod() {}
        
        @Override
        public String toString() { return "SimpleClass"; }
    }

    public static class SubClass extends SimpleClass {
        public void subMethod() {}
    }

    public static class InterfaceImpl implements MyInterface {
        @Override
        public void interfaceMethod() {}
    }

    public interface MyInterface {
        void interfaceMethod();
    }

    @Before
    public void setUp() {
        mockIntrospector = Mockito.mock(AnnotationIntrospector.class);
        mockMixInResolver = Mockito.mock(MixInResolver.class);
        
        // Default behavior: no annotations, no mix-ins
        Mockito.when(mockIntrospector.isAnnotationBundle(Mockito.any(Annotation.class))).thenReturn(false);
        Mockito.when(mockIntrospector.hasIgnoreMarker(Mockito.any(Annotated.class))).thenReturn(false);
        Mockito.when(mockMixInResolver.findMixInClassFor(Mockito.any(Class.class))).thenReturn(null);
    }

    @Test
    public void testConstruct() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        assertNotNull(ac);
        assertEquals(SimpleClass.class, ac.getAnnotated());
        assertEquals(SimpleClass.class.getName(), ac.getName());
        assertEquals(SimpleClass.class.getModifiers(), ac.getModifiers());
        assertEquals(SimpleClass.class, ac.getGenericType());
        assertEquals(SimpleClass.class, ac.getRawType());
    }

    @Test
    public void testConstructWithoutSuperTypes() {
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SimpleClass.class, mockIntrospector, mockMixInResolver);
        assertNotNull(ac);
        assertEquals(SimpleClass.class, ac.getAnnotated());
    }

    @Test
    public void testWithAnnotations() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        AnnotationMap newAnnMap = new AnnotationMap();
        AnnotatedClass ac2 = ac.withAnnotations(newAnnMap);
        
        assertNotNull(ac2);
        assertSame(SimpleClass.class, ac2.getAnnotated());
        // Verify it's a new instance
        assertFalse(ac == ac2);
    }

    @Test
    public void testGetAnnotationsLazyInit() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        
        // First call triggers resolution
        Annotations anns = ac.getAnnotations();
        assertNotNull(anns);
        
        // Second call should use cached
        Annotations anns2 = ac.getAnnotations();
        assertSame(anns, anns2);
    }

    @Test
    public void testHasAnnotations() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        // SimpleClass has no Jackson annotations in this test setup
        assertFalse(ac.hasAnnotations());
    }

    @Test
    public void testGetDefaultConstructor() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        AnnotatedConstructor ctor = ac.getDefaultConstructor();
        assertNotNull(ctor);
        assertEquals(0, ctor.getParameterCount());
    }

    @Test
    public void testGetConstructors() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertNotNull(ctors);
        // SimpleClass has one non-default constructor
        assertEquals(1, ctors.size());
        assertEquals(1, ctors.get(0).getParameterCount());
    }

    @Test
    public void testGetStaticMethods() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        List<AnnotatedMethod> methods = ac.getStaticMethods();
        assertNotNull(methods);
        // SimpleClass has one static method
        assertEquals(1, methods.size());
        assertEquals("staticMethod", methods.get(0).getName());
    }

    @Test
    public void testMemberMethods() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        Iterable<AnnotatedMethod> methods = ac.memberMethods();
        assertNotNull(methods);
        
        int count = 0;
        for (AnnotatedMethod m : methods) {
            count++;
            // Verify only methods with <= 2 args are included
            assertTrue(m.getParameterCount() <= 2);
        }
        
        // SimpleClass has: noArgsMethod, oneArgMethod, twoArgsMethod, toString (from Object)
        // Note: Object methods might be filtered or included depending on implementation details of _isIncludableMemberMethod
        // _isIncludableMemberMethod excludes static, synthetic, bridge, and >2 args.
        // toString has 0 args.
        assertTrue(count >= 3); 
    }

    @Test
    public void testGetMemberMethodCount() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        int count = ac.getMemberMethodCount();
        assertTrue(count > 0);
    }

    @Test
    public void testFindMethod() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        
        AnnotatedMethod m = ac.findMethod("noArgsMethod", new Class<?>[]{});
        assertNotNull(m);
        assertEquals("noArgsMethod", m.getName());
        
        m = ac.findMethod("oneArgMethod", new Class<?>[]{String.class});
        assertNotNull(m);
        assertEquals("oneArgMethod", m.getName());
        
        m = ac.findMethod("nonExistent", new Class<?>[]{});
        assertNull(m);
    }

    @Test
    public void testFields() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        Iterable<AnnotatedField> fields = ac.fields();
        assertNotNull(fields);
        
        int count = 0;
        boolean foundPublic = false;
        boolean foundPrivate = false;
        boolean foundStatic = false;
        boolean foundTransient = false;
        
        for (AnnotatedField f : fields) {
            count++;
            if (f.getName().equals("publicField")) foundPublic = true;
            if (f.getName().equals("privateField")) foundPrivate = true;
            if (f.getName().equals("staticField")) foundStatic = true;
            if (f.getName().equals("transientField")) foundTransient = true;
        }
        
        assertTrue(foundPublic);
        assertTrue(foundPrivate);
        assertFalse("Static fields should be excluded", foundStatic);
        assertFalse("Transient fields should be excluded", foundTransient);
        assertEquals(2, count);
    }

    @Test
    public void testGetFieldCount() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        assertEquals(2, ac.getFieldCount());
    }

    @Test
    public void testAnnotationsWithNullIntrospector() {
        // When introspector is null, annotations should be empty but not crash
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, null, null);
        assertNotNull(ac.getAnnotations());
        assertFalse(ac.hasAnnotations());
        assertEquals(0, ac.getAnnotations().size());
    }

    @Test
    public void testCreatorsWithNullIntrospector() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, null, null);
        assertNotNull(ac.getDefaultConstructor());
        assertNotNull(ac.getConstructors());
        assertNotNull(ac.getStaticMethods());
    }

    @Test
    public void testMemberMethodsWithNullIntrospector() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, null, null);
        assertNotNull(ac.memberMethods());
        assertTrue(ac.getMemberMethodCount() > 0);
    }

    @Test
    public void testFieldsWithNullIntrospector() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, null, null);
        assertNotNull(ac.fields());
        assertEquals(2, ac.getFieldCount());
    }

    @Test
    public void testToString() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        String str = ac.toString();
        assertTrue(str.contains("AnnotedClass"));
        assertTrue(str.contains(SimpleClass.class.getName()));
    }

    @Test
    public void testGetAnnotation() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        // SimpleClass has no specific annotations in this test
        Annotation ann = ac.getAnnotation(Override.class);
        assertNull(ann);
    }

    @Test
    public void testAnnotationsIterable() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        Iterable<Annotation> it = ac.annotations();
        assertNotNull(it);
        // Should be empty for SimpleClass
        assertFalse(it.iterator().hasNext());
    }

    @Test
    public void testSuperTypesResolution() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, mockIntrospector, mockMixInResolver);
        // SubClass extends SimpleClass extends Object
        // Should find methods from SimpleClass and Object
        AnnotatedMethod m = ac.findMethod("noArgsMethod", new Class<?>[]{});
        assertNotNull("Should find inherited method", m);
        
        m = ac.findMethod("subMethod", new Class<?>[]{});
        assertNotNull("Should find own method", m);
    }

    @Test
    public void testInterfaceMethods() {
        AnnotatedClass ac = AnnotatedClass.construct(InterfaceImpl.class, mockIntrospector, mockMixInResolver);
        AnnotatedMethod m = ac.findMethod("interfaceMethod", new Class<?>[]{});
        assertNotNull("Should find interface method", m);
    }

    @Test
    public void testMixInResolverCalled() {
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        // Force resolution
        ac.getAnnotations();
        
        // Verify mix-in resolver was called for the class itself
        Mockito.verify(mockMixInResolver, Mockito.atLeastOnce()).findMixInClassFor(SimpleClass.class);
    }

    @Test
    public void testEmptyAnnotationMaps() {
        // Test internal helper indirectly via construction with null introspector
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, null, null);
        assertNotNull(ac.getAnnotations());
        assertEquals(0, ac.getAnnotations().size());
    }
    
    @Test
    public void testConstructorParameterMismatchHandling() {
        // This test is tricky to trigger directly without complex reflection setup 
        // involving inner classes or enums with specific JDK bugs.
        // We verify that standard constructors work fine.
        AnnotatedClass ac = AnnotatedClass.construct(SimpleClass.class, mockIntrospector, mockMixInResolver);
        List<AnnotatedConstructor> ctors = ac.getConstructors();
        assertEquals(1, ctors.size());
    }
}
