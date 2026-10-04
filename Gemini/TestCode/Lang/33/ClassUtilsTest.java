package org.apache.commons.lang3;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ClassUtilsTest {

    private static class InnerTestClass {
    }

    @Test
    public void testConstructor() {
        ClassUtils utils = new ClassUtils();
        Assert.assertNotNull(utils);
    }

    @Test
    public void testGetShortClassName_Object() {
        Assert.assertEquals("ClassUtilsTest", ClassUtils.getShortClassName(this, "default"));
        Assert.assertEquals("default", ClassUtils.getShortClassName((Object) null, "default"));
    }

    @Test
    public void testGetShortClassName_Class() {
        Assert.assertEquals("ClassUtilsTest", ClassUtils.getShortClassName(ClassUtilsTest.class));
        Assert.assertEquals("ClassUtilsTest.InnerTestClass", ClassUtils.getShortClassName(InnerTestClass.class));
        Assert.assertEquals("String[]", ClassUtils.getShortClassName(String[].class));
        Assert.assertEquals("int[][]", ClassUtils.getShortClassName(int[][].class));
        Assert.assertEquals("", ClassUtils.getShortClassName((Class<?>) null));
    }

    @Test
    public void testGetShortClassName_String() {
        Assert.assertEquals("", ClassUtils.getShortClassName((String) null));
        Assert.assertEquals("", ClassUtils.getShortClassName(""));
        Assert.assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
        Assert.assertEquals("Map.Entry", ClassUtils.getShortClassName("java.util.Map$Entry"));
        Assert.assertEquals("Entry", ClassUtils.getShortClassName("Map$Entry"));
        Assert.assertEquals("int[]", ClassUtils.getShortClassName("[I"));
        Assert.assertEquals("double[][]", ClassUtils.getShortClassName("[[D"));
        Assert.assertEquals("String[]", ClassUtils.getShortClassName("[Ljava.lang.String;"));
        Assert.assertEquals("String[][]", ClassUtils.getShortClassName("[[Ljava.lang.String;"));
        Assert.assertEquals("String", ClassUtils.getShortClassName("Ljava.lang.String;"));
    }

    @Test
    public void testGetPackageName_Object() {
        Assert.assertEquals("org.apache.commons.lang3", ClassUtils.getPackageName(this, "default"));
        Assert.assertEquals("default", ClassUtils.getPackageName((Object) null, "default"));
    }

    @Test
    public void testGetPackageName_Class() {
        Assert.assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        Assert.assertEquals("java.lang", ClassUtils.getPackageName(String[].class));
        Assert.assertEquals("", ClassUtils.getPackageName((Class<?>) null));
        Assert.assertEquals("", ClassUtils.getPackageName(int.class));
    }

    @Test
    public void testGetPackageName_String() {
        Assert.assertEquals("", ClassUtils.getPackageName((String) null));
        Assert.assertEquals("", ClassUtils.getPackageName(""));
        Assert.assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String"));
        Assert.assertEquals("", ClassUtils.getPackageName("String"));
        Assert.assertEquals("java.lang", ClassUtils.getPackageName("[Ljava.lang.String;"));
        Assert.assertEquals("java.lang", ClassUtils.getPackageName("[[Ljava.lang.String;"));
        Assert.assertEquals("", ClassUtils.getPackageName("[I"));
    }

    @Test
    public void testGetAllSuperclasses() {
        Assert.assertNull(ClassUtils.getAllSuperclasses(null));
        List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(ArrayList.class);
        Assert.assertTrue(superclasses.contains(java.util.AbstractList.class));
        Assert.assertTrue(superclasses.contains(java.util.AbstractCollection.class));
        Assert.assertTrue(superclasses.contains(Object.class));
        Assert.assertEquals(0, ClassUtils.getAllSuperclasses(Object.class).size());
    }

    @Test
    public void testGetAllInterfaces() {
        Assert.assertNull(ClassUtils.getAllInterfaces(null));
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        Assert.assertTrue(interfaces.contains(java.util.List.class));
        Assert.assertTrue(interfaces.contains(java.util.Collection.class));
        Assert.assertTrue(interfaces.contains(Iterable.class));
        Assert.assertTrue(interfaces.contains(java.util.RandomAccess.class));
        Assert.assertTrue(interfaces.contains(Cloneable.class));
        Assert.assertTrue(interfaces.contains(java.io.Serializable.class));

        List<Class<?>> interfaceOnlyList = ClassUtils.getAllInterfaces(List.class);
        Assert.assertTrue(interfaceOnlyList.contains(java.util.Collection.class));
        Assert.assertTrue(interfaceOnlyList.contains(Iterable.class));
    }

    @Test
    public void testConvertClassNamesToClasses() {
        Assert.assertNull(ClassUtils.convertClassNamesToClasses(null));
        List<String> names = Arrays.asList("java.lang.String", "non.existing.ClassName", null);
        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(names);
        Assert.assertEquals(3, classes.size());
        Assert.assertEquals(String.class, classes.get(0));
        Assert.assertNull(classes.get(1));
        Assert.assertNull(classes.get(2));
    }

    @Test
    public void testConvertClassesToClassNames() {
        Assert.assertNull(ClassUtils.convertClassesToClassNames(null));
        List<Class<?>> classes = Arrays.asList(String.class, null, Integer.class);
        List<String> names = ClassUtils.convertClassesToClassNames(classes);
        Assert.assertEquals(3, names.size());
        Assert.assertEquals("java.lang.String", names.get(0));
        Assert.assertNull(names.get(1));
        Assert.assertEquals("java.lang.Integer", names.get(2));
    }

    @Test
    public void testIsAssignable_Array() {
        Assert.assertFalse(ClassUtils.isAssignable(new Class<?>[]{String.class}, new Class<?>[]{String.class, Integer.class}));
        Assert.assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
        Assert.assertTrue(ClassUtils.isAssignable(new Class<?>[]{String.class}, new Class<?>[]{Object.class}));
        Assert.assertTrue(ClassUtils.isAssignable(new Class<?>[]{Integer.TYPE}, new Class<?>[]{Integer.class}, true));
        Assert.assertFalse(ClassUtils.isAssignable(new Class<?>[]{Integer.TYPE}, new Class<?>[]{Integer.class}, false));
    }

    @Test
    public void testIsAssignable_Class() {
        Assert.assertFalse(ClassUtils.isAssignable(String.class, null));
        Assert.assertTrue(ClassUtils.isAssignable(null, Object.class));
        Assert.assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(String.class, String.class));
        Assert.assertTrue(ClassUtils.isAssignable(Integer.class, Number.class));

        Assert.assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        Assert.assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        Assert.assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        Assert.assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));

        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Integer.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Long.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Byte.TYPE, Character.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Byte.TYPE, Boolean.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Short.TYPE, Long.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Short.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Short.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Short.TYPE, Byte.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Character.TYPE, Long.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Character.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Character.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Character.TYPE, Short.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Integer.TYPE, Short.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Long.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Long.TYPE, Integer.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Float.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Float.TYPE, Long.TYPE));

        Assert.assertFalse(ClassUtils.isAssignable(Double.TYPE, Float.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));

        Assert.assertFalse(ClassUtils.isAssignable(Integer.TYPE, String.class, false));
        Assert.assertFalse(ClassUtils.isAssignable(String.class, Integer.TYPE, false));
    }

    @Test
    public void testPrimitiveToWrapper() {
        Assert.assertNull(ClassUtils.primitiveToWrapper(null));
        Assert.assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        Assert.assertEquals(Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        Assert.assertEquals(Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        Assert.assertEquals(Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));
        Assert.assertEquals(Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        Assert.assertEquals(Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        Assert.assertEquals(Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        Assert.assertEquals(Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));
        Assert.assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
        Assert.assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));
    }

    @Test
    public void testPrimitivesToWrappers() {
        Assert.assertNull(ClassUtils.primitivesToWrappers(null));
        Assert.assertArrayEquals(new Class<?>[0], ClassUtils.primitivesToWrappers(new Class<?>[0]));
        Class<?>[] primitives = new Class<?>[]{Integer.TYPE, String.class};
        Class<?>[] wrappers = ClassUtils.primitivesToWrappers(primitives);
        Assert.assertArrayEquals(new Class<?>[]{Integer.class, String.class}, wrappers);
    }

    @Test
    public void testWrapperToPrimitive() {
        Assert.assertNull(ClassUtils.wrapperToPrimitive(null));
        Assert.assertNull(ClassUtils.wrapperToPrimitive(String.class));
        Assert.assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
        Assert.assertEquals(Long.TYPE, ClassUtils.wrapperToPrimitive(Long.class));
        Assert.assertEquals(Double.TYPE, ClassUtils.wrapperToPrimitive(Double.class));
        Assert.assertEquals(Float.TYPE, ClassUtils.wrapperToPrimitive(Float.class));
        Assert.assertEquals(Short.TYPE, ClassUtils.wrapperToPrimitive(Short.class));
        Assert.assertEquals(Byte.TYPE, ClassUtils.wrapperToPrimitive(Byte.class));
        Assert.assertEquals(Character.TYPE, ClassUtils.wrapperToPrimitive(Character.class));
        Assert.assertEquals(Boolean.TYPE, ClassUtils.wrapperToPrimitive(Boolean.class));
    }

    @Test
    public void testWrappersToPrimitives() {
        Assert.assertNull(ClassUtils.wrappersToPrimitives(null));
        Assert.assertArrayEquals(new Class<?>[0], ClassUtils.wrappersToPrimitives(new Class<?>[0]));
        Class<?>[] wrappers = new Class<?>[]{Integer.class, String.class};
        Class<?>[] primitives = ClassUtils.wrappersToPrimitives(wrappers);
        Assert.assertArrayEquals(new Class<?>[]{Integer.TYPE, null}, primitives);
    }

    @Test
    public void testIsInnerClass() {
        Assert.assertFalse(ClassUtils.isInnerClass(null));
        Assert.assertFalse(ClassUtils.isInnerClass(String.class));
        Assert.assertTrue(ClassUtils.isInnerClass(InnerTestClass.class));
        Assert.assertTrue(ClassUtils.isInnerClass(Map.Entry.class));
    }

    @Test
    public void testGetClass() throws ClassNotFoundException {
        Assert.assertEquals(int.class, ClassUtils.getClass("int"));
        Assert.assertEquals(int[].class, ClassUtils.getClass("int[]"));
        Assert.assertEquals(String.class, ClassUtils.getClass("java.lang.String"));
        Assert.assertEquals(String[].class, ClassUtils.getClass("java.lang.String[]"));
        Assert.assertEquals(String[].class, ClassUtils.getClass("[Ljava.lang.String;"));
        Assert.assertEquals(int[].class, ClassUtils.getClass("[I"));
        Assert.assertEquals(String.class, ClassUtils.getClass(ClassUtils.class.getClassLoader(), "java.lang.String"));
        Assert.assertEquals(int.class, ClassUtils.getClass(ClassUtils.class.getClassLoader(), "int", false));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testGetClassNotFound() throws ClassNotFoundException {
        ClassUtils.getClass("non.existing.ClassName");
    }

    @Test(expected = NullPointerException.class)
    public void testGetClassNull() throws ClassNotFoundException {
        ClassUtils.getClass((String) null);
    }

    @Test
    public void testGetPublicMethod() throws Exception {
        Method m1 = ClassUtils.getPublicMethod(String.class, "length", new Class<?>[0]);
        Assert.assertEquals("length", m1.getName());

        Method m2 = ClassUtils.getPublicMethod(ArrayList.class, "size", new Class<?>[0]);
        Assert.assertEquals("size", m2.getName());

        List<?> list = Collections.unmodifiableList(new ArrayList<Object>());
        Method m3 = ClassUtils.getPublicMethod(list.getClass(), "isEmpty", new Class<?>[0]);
        Assert.assertTrue(Modifier.isPublic(m3.getDeclaringClass().getModifiers()));
    }

    @Test(expected = NoSuchMethodException.class)
    public void testGetPublicMethodNotFound() throws Exception {
        ClassUtils.getPublicMethod(String.class, "nonExistentMethod", new Class<?>[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testGetPublicMethodNullClass() throws Exception {
        ClassUtils.getPublicMethod(null, "length", new Class<?>[0]);
    }

    @Test
    public void testToClass() {
        Assert.assertNull(ClassUtils.toClass(null));
        Assert.assertArrayEquals(new Class<?>[0], ClassUtils.toClass(new Object[0]));
        Object[] array = new Object[]{"test", Integer.valueOf(1), null};
        Class<?>[] classes = ClassUtils.toClass(array);
        Assert.assertEquals(3, classes.length);
        Assert.assertEquals(String.class, classes[0]);
        Assert.assertEquals(Integer.class, classes[1]);
        Assert.assertNull(classes[2]);
    }

    @Test
    public void testGetShortCanonicalName() {
        Assert.assertEquals("default", ClassUtils.getShortCanonicalName((Object) null, "default"));
        Assert.assertEquals("ClassUtilsTest", ClassUtils.getShortCanonicalName(this, "default"));
        Assert.assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
        Assert.assertEquals("String", ClassUtils.getShortCanonicalName(String.class));
        Assert.assertEquals("String[]", ClassUtils.getShortCanonicalName(String[].class));
        Assert.assertEquals("", ClassUtils.getShortCanonicalName((String) null));
        Assert.assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        Assert.assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        Assert.assertEquals("Map.Entry", ClassUtils.getShortCanonicalName("java.util.Map$Entry"));
    }

    @Test
    public void testGetPackageCanonicalName() {
        Assert.assertEquals("default", ClassUtils.getPackageCanonicalName((Object) null, "default"));
        Assert.assertEquals("org.apache.commons.lang3", ClassUtils.getPackageCanonicalName(this, "default"));
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
        Assert.assertEquals("java.lang", ClassUtils.getPackageCanonicalName(String.class));
        Assert.assertEquals("java.lang", ClassUtils.getPackageCanonicalName(String[].class));
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName((String) null));
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
        Assert.assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        Assert.assertEquals("java.util", ClassUtils.getPackageCanonicalName("java.util.Map$Entry"));
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName("["));
    }
}
