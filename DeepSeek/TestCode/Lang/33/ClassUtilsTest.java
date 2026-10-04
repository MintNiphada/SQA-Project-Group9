package org.apache.commons.lang3;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ClassUtilsTest {

    @Test
    public void testConstructor() {
        new ClassUtils();
    }

    @Test
    public void testGetShortClassName_Object() {
        assertEquals("String", ClassUtils.getShortClassName("hello", null));
        assertNull(ClassUtils.getShortClassName(null, null));
        assertEquals("default", ClassUtils.getShortClassName(null, "default"));
    }

    @Test
    public void testGetShortClassName_Class() {
        assertEquals("String", ClassUtils.getShortClassName(String.class));
        assertEquals("", ClassUtils.getShortClassName((Class<?>) null));
    }

    @Test
    public void testGetShortClassName_String() {
        assertEquals("", ClassUtils.getShortClassName((String) null));
        assertEquals("", ClassUtils.getShortClassName(""));
        assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
        assertEquals("Map.Entry", ClassUtils.getShortClassName("java.util.Map$Entry"));
        assertEquals("int", ClassUtils.getShortClassName("int"));
        assertEquals("int[]", ClassUtils.getShortClassName("[I"));
        assertEquals("int[][]", ClassUtils.getShortClassName("[[I"));
        assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
        assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
        assertEquals("Map.Entry", ClassUtils.getShortClassName("java.util.Map$Entry"));
        assertEquals("int[]", ClassUtils.getShortClassName("int[]"));
        assertEquals("String[]", ClassUtils.getShortClassName("java.lang.String[]"));
        assertEquals("Map.Entry[]", ClassUtils.getShortClassName("java.util.Map$Entry[]"));
    }

    @Test
    public void testGetPackageName_Object() {
        assertEquals("java.lang", ClassUtils.getPackageName("hello", null));
        assertNull(ClassUtils.getPackageName(null, null));
        assertEquals("default", ClassUtils.getPackageName(null, "default"));
    }

    @Test
    public void testGetPackageName_Class() {
        assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        assertEquals("", ClassUtils.getPackageName((Class<?>) null));
    }

    @Test
    public void testGetPackageName_String() {
        assertEquals("", ClassUtils.getPackageName((String) null));
        assertEquals("", ClassUtils.getPackageName(""));
        assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String"));
        assertEquals("", ClassUtils.getPackageName("String"));
        assertEquals("java.util", ClassUtils.getPackageName("java.util.Map$Entry"));
        assertEquals("", ClassUtils.getPackageName("[I"));
        assertEquals("java.lang", ClassUtils.getPackageName("[Ljava.lang.String;"));
        assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String[]"));
    }

    @Test
    public void testGetAllSuperclasses() {
        assertNull(ClassUtils.getAllSuperclasses(null));
        List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(ArrayList.class);
        assertEquals(3, superclasses.size());
        assertEquals(java.util.AbstractList.class, superclasses.get(0));
        assertEquals(java.util.AbstractCollection.class, superclasses.get(1));
        assertEquals(Object.class, superclasses.get(2));
        assertTrue(ClassUtils.getAllSuperclasses(Object.class).isEmpty());
    }

    @Test
    public void testGetAllInterfaces() {
        assertNull(ClassUtils.getAllInterfaces(null));
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        assertTrue(interfaces.contains(java.util.List.class));
        assertTrue(interfaces.contains(java.util.RandomAccess.class));
        assertTrue(interfaces.contains(java.lang.Cloneable.class));
        assertTrue(interfaces.contains(java.io.Serializable.class));
        assertTrue(interfaces.indexOf(java.util.List.class) < interfaces.indexOf(java.util.RandomAccess.class));
    }

    @Test
    public void testConvertClassNamesToClasses() {
        assertNull(ClassUtils.convertClassNamesToClasses(null));
        List<String> names = new ArrayList<String>();
        assertTrue(ClassUtils.convertClassNamesToClasses(names).isEmpty());
        names.add("java.lang.String");
        names.add("java.lang.Integer");
        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        assertEquals(String.class, classes.get(0));
        assertEquals(Integer.class, classes.get(1));
        names.add("invalid.ClassName");
        classes = ClassUtils.convertClassNamesToClasses(names);
        assertNull(classes.get(2));
        names.add(null);
        classes = ClassUtils.convertClassNamesToClasses(names);
        assertNull(classes.get(3));
    }

    @Test(expected = ClassCastException.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testConvertClassNamesToClasses_NonString() {
        List raw = new ArrayList();
        raw.add(Integer.valueOf(1));
        ClassUtils.convertClassNamesToClasses(raw);
    }

    @Test
    public void testConvertClassesToClassNames() {
        assertNull(ClassUtils.convertClassesToClassNames(null));
        List<Class<?>> classes = new ArrayList<Class<?>>();
        assertTrue(ClassUtils.convertClassesToClassNames(classes).isEmpty());
        classes.add(String.class);
        classes.add(Integer.class);
        List<String> names = ClassUtils.convertClassesToClassNames(classes);
        assertEquals("java.lang.String", names.get(0));
        assertEquals("java.lang.Integer", names.get(1));
        classes.add(null);
        names = ClassUtils.convertClassesToClassNames(classes);
        assertNull(names.get(2));
    }

    @Test(expected = ClassCastException.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void testConvertClassesToClassNames_NonClass() {
        List raw = new ArrayList();
        raw.add("not a class");
        ClassUtils.convertClassesToClassNames(raw);
    }

    @Test
    public void testIsAssignable_Class_Class() {
        assertFalse(ClassUtils.isAssignable((Class<?>) null, (Class<?>) null));
        assertFalse(ClassUtils.isAssignable(String.class, null));
        assertTrue(ClassUtils.isAssignable(null, String.class));
        assertTrue(ClassUtils.isAssignable(String.class, Object.class));
        assertFalse(ClassUtils.isAssignable(Object.class, String.class));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.TYPE));
        assertFalse(ClassUtils.isAssignable(Long.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Long.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
        assertFalse(ClassUtils.isAssignable(Double.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Float.TYPE, Double.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Character.TYPE, Double.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Short.TYPE, Double.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Long.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Float.TYPE));
        assertTrue(ClassUtils.isAssignable(Byte.TYPE, Double.TYPE));
        assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));
        assertTrue(ClassUtils.isAssignable(null, String.class));
    }

    @Test
    public void testIsAssignable_Class_Class_Autoboxing() {
        assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));
        assertTrue(ClassUtils.isAssignable(Integer.class, Number.class, true));
        assertTrue(ClassUtils.isAssignable(Integer.class, Number.class, false));
    }

    @Test
    public void testIsAssignable_Array() {
        assertTrue(ClassUtils.isAssignable(new Class[]{String.class}, new Class[]{Object.class}));
        assertFalse(ClassUtils.isAssignable(new Class[]{String.class}, new Class[]{Integer.class}));
        assertFalse(ClassUtils.isAssignable(new Class[]{String.class, Integer.class}, new Class[]{Object.class}));
        assertTrue(ClassUtils.isAssignable((Class[]) null, (Class[]) null));
        assertFalse(ClassUtils.isAssignable(new Class[]{String.class}, null));
        assertFalse(ClassUtils.isAssignable(null, new Class[]{String.class}));
        assertTrue(ClassUtils.isAssignable(new Class[0], new Class[0]));
    }

    @Test
    public void testIsAssignable_Array_Autoboxing() {
        assertTrue(ClassUtils.isAssignable(new Class[]{Integer.TYPE}, new Class[]{Integer.class}, true));
        assertFalse(ClassUtils.isAssignable(new Class[]{Integer.TYPE}, new Class[]{Integer.class}, false));
    }

    @Test
    public void testPrimitiveToWrapper() {
        assertNull(ClassUtils.primitiveToWrapper(null));
        assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));
        assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        assertEquals(Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        assertEquals(Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));
        assertEquals(Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        assertEquals(Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        assertEquals(Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        assertEquals(Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        assertEquals(Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));
        assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
    }

    @Test
    public void testPrimitivesToWrappers() {
        assertNull(ClassUtils.primitivesToWrappers(null));
        Class<?>[] empty = new Class[0];
        assertArrayEquals(empty, ClassUtils.primitivesToWrappers(empty));
        Class<?>[] primitives = new Class[]{Integer.TYPE, String.class, Void.TYPE};
        Class<?>[] wrappers = ClassUtils.primitivesToWrappers(primitives);
        assertEquals(Integer.class, wrappers[0]);
        assertEquals(String.class, wrappers[1]);
        assertEquals(Void.TYPE, wrappers[2]);
    }

    @Test
    public void testWrapperToPrimitive() {
        assertNull(ClassUtils.wrapperToPrimitive(null));
        assertNull(ClassUtils.wrapperToPrimitive(String.class));
        assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
        assertEquals(Long.TYPE, ClassUtils.wrapperToPrimitive(Long.class));
        assertEquals(Boolean.TYPE, ClassUtils.wrapperToPrimitive(Boolean.class));
        assertEquals(Byte.TYPE, ClassUtils.wrapperToPrimitive(Byte.class));
        assertEquals(Character.TYPE, ClassUtils.wrapperToPrimitive(Character.class));
        assertEquals(Short.TYPE, ClassUtils.wrapperToPrimitive(Short.class));
        assertEquals(Double.TYPE, ClassUtils.wrapperToPrimitive(Double.class));
        assertEquals(Float.TYPE, ClassUtils.wrapperToPrimitive(Float.class));
        assertNull(ClassUtils.wrapperToPrimitive(Void.TYPE));
    }

    @Test
    public void testWrappersToPrimitives() {
        assertNull(ClassUtils.wrappersToPrimitives(null));
        Class<?>[] empty = new Class[0];
        assertArrayEquals(empty, ClassUtils.wrappersToPrimitives(empty));
        Class<?>[] wrappers = new Class[]{Integer.class, String.class, Void.TYPE};
        Class<?>[] primitives = ClassUtils.wrappersToPrimitives(wrappers);
        assertEquals(Integer.TYPE, primitives[0]);
        assertNull(primitives[1]);
        assertNull(primitives[2]);
    }

    @Test
    public void testIsInnerClass() {
        assertFalse(ClassUtils.isInnerClass(null));
        assertFalse(ClassUtils.isInnerClass(String.class));
        assertTrue(ClassUtils.isInnerClass(Map.Entry.class));
        assertTrue(ClassUtils.isInnerClass(Thread.State.class));
    }

    @Test
    public void testGetClass_ClassLoader_String_boolean() throws Exception {
        assertEquals(String.class, ClassUtils.getClass(null, "java.lang.String", true));
        assertEquals(String.class, ClassUtils.getClass(ClassLoader.getSystemClassLoader(), "java.lang.String", false));
        assertEquals(int.class, ClassUtils.getClass(null, "int", true));
        assertEquals(int.class, ClassUtils.getClass(null, "[I", true));
        assertEquals(String[].class, ClassUtils.getClass(null, "[Ljava.lang.String;", true));
        assertEquals(String[].class, ClassUtils.getClass(null, "java.lang.String[]", true));
        assertEquals(int[].class, ClassUtils.getClass(null, "int[]", true));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testGetClass_ClassLoader_String_boolean_NotFound() throws Exception {
        ClassUtils.getClass(null, "invalid.Class", true);
    }

    @Test
    public void testGetClass_ClassLoader_String() throws Exception {
        assertEquals(String.class, ClassUtils.getClass(null, "java.lang.String"));
    }

    @Test
    public void testGetClass_String() throws Exception {
        assertEquals(String.class, ClassUtils.getClass("java.lang.String"));
    }

    @Test
    public void testGetClass_String_boolean() throws Exception {
        assertEquals(String.class, ClassUtils.getClass("java.lang.String", false));
    }

    @Test
    public void testGetPublicMethod() throws Exception {
        Method m = ClassUtils.getPublicMethod(String.class, "length", new Class[0]);
        assertNotNull(m);
        assertEquals("length", m.getName());
        assertTrue(java.lang.reflect.Modifier.isPublic(m.getDeclaringClass().getModifiers()));
    }

    @Test(expected = NoSuchMethodException.class)
    public void testGetPublicMethod_NotFound() throws Exception {
        ClassUtils.getPublicMethod(String.class, "nonexistent", new Class[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testGetPublicMethod_NullClass() throws Exception {
        ClassUtils.getPublicMethod(null, "toString", new Class[0]);
    }

    @Test(expected = NoSuchMethodException.class)
    public void testGetPublicMethod_NonPublicDeclaringClass() throws Exception {
        ClassUtils.getPublicMethod(PublicSubClass.class, "foo", new Class[0]);
    }

    @Test
    public void testGetPublicMethod_FromInterface() throws Exception {
        Method m = ClassUtils.getPublicMethod(ArrayList.class, "size", new Class[0]);
        assertNotNull(m);
        assertTrue(java.lang.reflect.Modifier.isPublic(m.getDeclaringClass().getModifiers()));
    }

    @Test
    public void testToClass() {
        assertNull(ClassUtils.toClass(null));
        assertArrayEquals(new Class[0], ClassUtils.toClass(new Object[0]));
        Object[] array = new Object[]{"hello", Integer.valueOf(1)};
        Class<?>[] classes = ClassUtils.toClass(array);
        assertEquals(String.class, classes[0]);
        assertEquals(Integer.class, classes[1]);
    }

    @Test(expected = NullPointerException.class)
    public void testToClass_NullElement() {
        ClassUtils.toClass(new Object[]{null});
    }

    @Test
    public void testGetShortCanonicalName_Object() {
        assertEquals("String", ClassUtils.getShortCanonicalName("hello", null));
        assertNull(ClassUtils.getShortCanonicalName(null, null));
        assertEquals("default", ClassUtils.getShortCanonicalName(null, "default"));
    }

    @Test
    public void testGetShortCanonicalName_Class() {
        assertEquals("String", ClassUtils.getShortCanonicalName(String.class));
        assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
    }

    @Test
    public void testGetShortCanonicalName_String() {
        assertEquals("", ClassUtils.getShortCanonicalName((String) null));
        assertEquals("", ClassUtils.getShortCanonicalName(""));
        assertEquals("String", ClassUtils.getShortCanonicalName("java.lang.String"));
        assertEquals("Map.Entry", ClassUtils.getShortCanonicalName("java.util.Map$Entry"));
        assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        assertEquals("int[][]", ClassUtils.getShortCanonicalName("[[I"));
        assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        assertEquals("String[]", ClassUtils.getShortCanonicalName("java.lang.String[]"));
    }

    @Test
    public void testGetPackageCanonicalName_Object() {
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("hello", null));
        assertNull(ClassUtils.getPackageCanonicalName(null, null));
        assertEquals("default", ClassUtils.getPackageCanonicalName(null, "default"));
    }

    @Test
    public void testGetPackageCanonicalName_Class() {
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName(String.class));
        assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
    }

    @Test
    public void testGetPackageCanonicalName_String() {
        assertEquals("", ClassUtils.getPackageCanonicalName((String) null));
        assertEquals("", ClassUtils.getPackageCanonicalName(""));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("java.lang.String"));
        assertEquals("", ClassUtils.getPackageCanonicalName("String"));
        assertEquals("java.util", ClassUtils.getPackageCanonicalName("java.util.Map$Entry"));
        assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        assertEquals("java.lang", ClassUtils.getPackageCanonicalName("java.lang.String[]"));
    }

    static class PublicSuperClass {
        public void foo() {}
    }

    public static class PublicSubClass extends PublicSuperClass {}
}
