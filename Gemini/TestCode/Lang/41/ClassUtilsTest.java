package org.apache.commons.lang;

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

    private static class InnerClass {
    }

    private static class InnerClassChild extends InnerClass {
    }

    private interface PublicInterface {
        void testMethod();
    }

    private static class PrivateClass implements PublicInterface {
        public void testMethod() {
        }
    }

    @Test
    public void testConstructor() {
        ClassUtils utils = new ClassUtils();
        Assert.assertNotNull(utils);
    }

    @Test
    public void testGetShortClassName_Object() {
        Assert.assertEquals("default", ClassUtils.getShortClassName((Object) null, "default"));
        Assert.assertEquals("String", ClassUtils.getShortClassName("hello", "default"));
        Assert.assertEquals("Integer", ClassUtils.getShortClassName(Integer.valueOf(1), null));
    }

    @Test
    public void testGetShortClassName_Class() {
        Assert.assertEquals("", ClassUtils.getShortClassName((Class<?>) null));
        Assert.assertEquals("String", ClassUtils.getShortClassName(String.class));
        Assert.assertEquals("ClassUtilsTest.InnerClass", ClassUtils.getShortClassName(InnerClass.class));
        Assert.assertEquals("int[]", ClassUtils.getShortClassName(int[].class));
    }

    @Test
    public void testGetShortClassName_String() {
        Assert.assertEquals("", ClassUtils.getShortClassName((String) null));
        Assert.assertEquals("", ClassUtils.getShortClassName(""));
        Assert.assertEquals("String", ClassUtils.getShortClassName("java.lang.String"));
        Assert.assertEquals("ClassUtilsTest.InnerClass", ClassUtils.getShortClassName("org.apache.commons.lang.ClassUtilsTest$InnerClass"));
        Assert.assertEquals("InnerClass", ClassUtils.getShortClassName("InnerClass"));
        Assert.assertEquals("InnerClass.Nested", ClassUtils.getShortClassName("InnerClass$Nested"));
    }

    @Test
    public void testGetPackageName_Object() {
        Assert.assertEquals("default", ClassUtils.getPackageName((Object) null, "default"));
        Assert.assertEquals("java.lang", ClassUtils.getPackageName("hello", "default"));
    }

    @Test
    public void testGetPackageName_Class() {
        Assert.assertEquals("", ClassUtils.getPackageName((Class<?>) null));
        Assert.assertEquals("java.lang", ClassUtils.getPackageName(String.class));
        Assert.assertEquals("org.apache.commons.lang", ClassUtils.getPackageName(ClassUtilsTest.class));
    }

    @Test
    public void testGetPackageName_String() {
        Assert.assertEquals("", ClassUtils.getPackageName((String) null));
        Assert.assertEquals("", ClassUtils.getPackageName(""));
        Assert.assertEquals("java.lang", ClassUtils.getPackageName("java.lang.String"));
        Assert.assertEquals("", ClassUtils.getPackageName("String"));
    }

    @Test
    public void testGetAllSuperclasses() {
        Assert.assertNull(ClassUtils.getAllSuperclasses(null));
        List<Class<?>> superclasses = ClassUtils.getAllSuperclasses(InnerClassChild.class);
        Assert.assertEquals(2, superclasses.size());
        Assert.assertEquals(InnerClass.class, superclasses.get(0));
        Assert.assertEquals(Object.class, superclasses.get(1));

        List<Class<?>> objSuper = ClassUtils.getAllSuperclasses(Object.class);
        Assert.assertTrue(objSuper.isEmpty());
    }

    @Test
    public void testGetAllInterfaces() {
        Assert.assertNull(ClassUtils.getAllInterfaces(null));
        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(ArrayList.class);
        Assert.assertTrue(interfaces.contains(List.class));
        Assert.assertTrue(interfaces.contains(Cloneable.class));
        Assert.assertTrue(interfaces.contains(java.io.Serializable.class));
    }

    @Test
    public void testConvertClassNamesToClasses() {
        Assert.assertNull(ClassUtils.convertClassNamesToClasses(null));
        List<String> list = Arrays.asList("java.lang.String", "non.existing.ClassName", null);
        List<Class<?>> classes = ClassUtils.convertClassNamesToClasses(list);
        Assert.assertEquals(3, classes.size());
        Assert.assertEquals(String.class, classes.get(0));
        Assert.assertNull(classes.get(1));
        Assert.assertNull(classes.get(2));
    }

    @Test
    public void testConvertClassesToClassNames() {
        Assert.assertNull(ClassUtils.convertClassesToClassNames(null));
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(String.class);
        list.add(null);
        List<String> names = ClassUtils.convertClassesToClassNames(list);
        Assert.assertEquals(2, names.size());
        Assert.assertEquals("java.lang.String", names.get(0));
        Assert.assertNull(names.get(1));
    }

    @Test
    public void testIsAssignable_Array() {
        Assert.assertFalse(ClassUtils.isAssignable(new Class<?>[]{String.class}, new Class<?>[]{String.class, Integer.class}));
        Assert.assertTrue(ClassUtils.isAssignable((Class<?>[]) null, (Class<?>[]) null));
        Assert.assertTrue(ClassUtils.isAssignable(new Class<?>[]{String.class}, new Class<?>[]{Object.class}));
        Assert.assertFalse(ClassUtils.isAssignable(new Class<?>[]{Object.class}, new Class<?>[]{String.class}));
        Assert.assertTrue(ClassUtils.isAssignable(new Class<?>[]{Integer.TYPE}, new Class<?>[]{Integer.class}, true));
    }

    @Test
    public void testIsAssignable_PrimitiveWidenings() {
        Assert.assertFalse(ClassUtils.isAssignable((Class<?>) null, (Class<?>) null));
        Assert.assertFalse(ClassUtils.isAssignable(String.class, null));
        Assert.assertTrue(ClassUtils.isAssignable(null, String.class));
        Assert.assertFalse(ClassUtils.isAssignable(null, Integer.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Integer.TYPE, Long.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Integer.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Integer.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Integer.TYPE, Short.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Long.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Long.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Long.TYPE, Integer.TYPE));

        Assert.assertFalse(ClassUtils.isAssignable(Boolean.TYPE, Integer.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Double.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Float.TYPE, Double.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Character.TYPE, Integer.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Character.TYPE, Long.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Character.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Character.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Character.TYPE, Short.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Short.TYPE, Integer.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Short.TYPE, Long.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Short.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Short.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Short.TYPE, Byte.TYPE));

        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Short.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Integer.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Long.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Float.TYPE));
        Assert.assertTrue(ClassUtils.isAssignable(Byte.TYPE, Double.TYPE));
        Assert.assertFalse(ClassUtils.isAssignable(Byte.TYPE, Character.TYPE));
    }

    @Test
    public void testIsAssignable_Autoboxing() {
        Assert.assertTrue(ClassUtils.isAssignable(Integer.TYPE, Integer.class, true));
        Assert.assertTrue(ClassUtils.isAssignable(Integer.class, Integer.TYPE, true));
        Assert.assertFalse(ClassUtils.isAssignable(Integer.TYPE, Integer.class, false));
        Assert.assertFalse(ClassUtils.isAssignable(Integer.class, Integer.TYPE, false));
        Assert.assertTrue(ClassUtils.isAssignable(Integer.class, Number.class, true));
        Assert.assertFalse(ClassUtils.isAssignable(String.class, Integer.TYPE, true));
    }

    @Test
    public void testPrimitiveToWrapper() {
        Assert.assertNull(ClassUtils.primitiveToWrapper(null));
        Assert.assertEquals(Integer.class, ClassUtils.primitiveToWrapper(Integer.TYPE));
        Assert.assertEquals(Boolean.class, ClassUtils.primitiveToWrapper(Boolean.TYPE));
        Assert.assertEquals(Byte.class, ClassUtils.primitiveToWrapper(Byte.TYPE));
        Assert.assertEquals(Character.class, ClassUtils.primitiveToWrapper(Character.TYPE));
        Assert.assertEquals(Short.class, ClassUtils.primitiveToWrapper(Short.TYPE));
        Assert.assertEquals(Long.class, ClassUtils.primitiveToWrapper(Long.TYPE));
        Assert.assertEquals(Double.class, ClassUtils.primitiveToWrapper(Double.TYPE));
        Assert.assertEquals(Float.class, ClassUtils.primitiveToWrapper(Float.TYPE));
        Assert.assertEquals(Void.TYPE, ClassUtils.primitiveToWrapper(Void.TYPE));
        Assert.assertEquals(String.class, ClassUtils.primitiveToWrapper(String.class));
    }

    @Test
    public void testPrimitivesToWrappers() {
        Assert.assertNull(ClassUtils.primitivesToWrappers(null));
        Assert.assertArrayEquals(new Class<?>[0], ClassUtils.primitivesToWrappers(new Class<?>[0]));
        Class<?>[] primitives = new Class<?>[]{Integer.TYPE, String.class, null};
        Class<?>[] wrappers = ClassUtils.primitivesToWrappers(primitives);
        Assert.assertEquals(Integer.class, wrappers[0]);
        Assert.assertEquals(String.class, wrappers[1]);
        Assert.assertNull(wrappers[2]);
    }

    @Test
    public void testWrapperToPrimitive() {
        Assert.assertNull(ClassUtils.wrapperToPrimitive(null));
        Assert.assertNull(ClassUtils.wrapperToPrimitive(String.class));
        Assert.assertEquals(Integer.TYPE, ClassUtils.wrapperToPrimitive(Integer.class));
        Assert.assertEquals(Boolean.TYPE, ClassUtils.wrapperToPrimitive(Boolean.class));
        Assert.assertEquals(Byte.TYPE, ClassUtils.wrapperToPrimitive(Byte.class));
        Assert.assertEquals(Character.TYPE, ClassUtils.wrapperToPrimitive(Character.class));
        Assert.assertEquals(Short.TYPE, ClassUtils.wrapperToPrimitive(Short.class));
        Assert.assertEquals(Long.TYPE, ClassUtils.wrapperToPrimitive(Long.class));
        Assert.assertEquals(Double.TYPE, ClassUtils.wrapperToPrimitive(Double.class));
        Assert.assertEquals(Float.TYPE, ClassUtils.wrapperToPrimitive(Float.class));
    }

    @Test
    public void testWrappersToPrimitives() {
        Assert.assertNull(ClassUtils.wrappersToPrimitives(null));
        Assert.assertArrayEquals(new Class<?>[0], ClassUtils.wrappersToPrimitives(new Class<?>[0]));
        Class<?>[] wrappers = new Class<?>[]{Integer.class, String.class, null};
        Class<?>[] primitives = ClassUtils.wrappersToPrimitives(wrappers);
        Assert.assertEquals(Integer.TYPE, primitives[0]);
        Assert.assertNull(primitives[1]);
        Assert.assertNull(primitives[2]);
    }

    @Test
    public void testIsInnerClass() {
        Assert.assertFalse(ClassUtils.isInnerClass(null));
        Assert.assertFalse(ClassUtils.isInnerClass(String.class));
        Assert.assertTrue(ClassUtils.isInnerClass(InnerClass.class));
    }

    @Test
    public void testGetClass() throws Exception {
        Assert.assertEquals(int.class, ClassUtils.getClass("int"));
        Assert.assertEquals(int[].class, ClassUtils.getClass("int[]"));
        Assert.assertEquals(String.class, ClassUtils.getClass("java.lang.String"));
        Assert.assertEquals(String[].class, ClassUtils.getClass("java.lang.String[]"));
        Assert.assertEquals(String.class, ClassUtils.getClass(ClassLoader.getSystemClassLoader(), "java.lang.String"));
        Assert.assertEquals(int.class, ClassUtils.getClass(ClassLoader.getSystemClassLoader(), "int", true));
    }

    @Test(expected = ClassNotFoundException.class)
    public void testGetClassNotFound() throws Exception {
        ClassUtils.getClass("non.existing.Class");
    }

    @Test
    public void testGetPublicMethod() throws Exception {
        Method method = ClassUtils.getPublicMethod(PrivateClass.class, "testMethod", new Class<?>[0]);
        Assert.assertNotNull(method);
        Assert.assertTrue(Modifier.isPublic(method.getDeclaringClass().getModifiers()));

        Method stringMethod = ClassUtils.getPublicMethod(String.class, "indexOf", new Class<?>[]{String.class});
        Assert.assertNotNull(stringMethod);
    }

    @Test(expected = NoSuchMethodException.class)
    public void testGetPublicMethodNotFound() throws Exception {
        ClassUtils.getPublicMethod(String.class, "nonExistentMethod", new Class<?>[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testGetPublicMethodNullClass() throws Exception {
        ClassUtils.getPublicMethod(null, "toString", new Class<?>[0]);
    }

    @Test
    public void testToClass() {
        Assert.assertNull(ClassUtils.toClass(null));
        Assert.assertArrayEquals(new Class<?>[0], ClassUtils.toClass(new Object[0]));
        Object[] objects = new Object[]{"hello", Integer.valueOf(10)};
        Class<?>[] expected = new Class<?>[]{String.class, Integer.class};
        Assert.assertArrayEquals(expected, ClassUtils.toClass(objects));
    }

    @Test
    public void testGetShortCanonicalName_Object() {
        Assert.assertEquals("default", ClassUtils.getShortCanonicalName((Object) null, "default"));
        Assert.assertEquals("String", ClassUtils.getShortCanonicalName("hello", "default"));
        Assert.assertEquals("int[]", ClassUtils.getShortCanonicalName(new int[0], "default"));
    }

    @Test
    public void testGetShortCanonicalName_Class() {
        Assert.assertEquals("", ClassUtils.getShortCanonicalName((Class<?>) null));
        Assert.assertEquals("String", ClassUtils.getShortCanonicalName(String.class));
        Assert.assertEquals("int[]", ClassUtils.getShortCanonicalName(int[].class));
        Assert.assertEquals("String[][]", ClassUtils.getShortCanonicalName(String[][].class));
    }

    @Test
    public void testGetShortCanonicalName_String() {
        Assert.assertEquals("", ClassUtils.getShortCanonicalName((String) null));
        Assert.assertEquals("", ClassUtils.getShortCanonicalName(""));
        Assert.assertEquals("String", ClassUtils.getShortCanonicalName("java.lang.String"));
        Assert.assertEquals("int[]", ClassUtils.getShortCanonicalName("[I"));
        Assert.assertEquals("String[]", ClassUtils.getShortCanonicalName("[Ljava.lang.String;"));
        Assert.assertEquals("Map.Entry", ClassUtils.getShortCanonicalName("java.util.Map$Entry"));
    }

    @Test
    public void testGetPackageCanonicalName_Object() {
        Assert.assertEquals("default", ClassUtils.getPackageCanonicalName((Object) null, "default"));
        Assert.assertEquals("java.lang", ClassUtils.getPackageCanonicalName("hello", "default"));
    }

    @Test
    public void testGetPackageCanonicalName_Class() {
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName((Class<?>) null));
        Assert.assertEquals("java.lang", ClassUtils.getPackageCanonicalName(String.class));
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName(int[].class));
    }

    @Test
    public void testGetPackageCanonicalName_String() {
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName((String) null));
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName(""));
        Assert.assertEquals("java.lang", ClassUtils.getPackageCanonicalName("java.lang.String"));
        Assert.assertEquals("java.lang", ClassUtils.getPackageCanonicalName("[Ljava.lang.String;"));
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName("[I"));
        Assert.assertEquals("", ClassUtils.getPackageCanonicalName("int[]"));
    }
}
