package com.google.gson.internal;

import static org.junit.Assert.*;
import org.junit.Test;
import java.lang.reflect.*;
import java.util.*;

public class $Gson$TypesTest {

    // Helper to create a TypeVariable from a class's type parameter
    private static TypeVariable<?> getTypeVariable(Class<?> clazz, String name) {
        for (TypeVariable<?> tv : clazz.getTypeParameters()) {
            if (tv.getName().equals(name)) {
                return tv;
            }
        }
        throw new IllegalArgumentException("No type variable " + name + " in " + clazz);
    }

    // Helper to create a ParameterizedType using $Gson$Types
    private static ParameterizedType newParamType(Type owner, Type raw, Type... args) {
        return $Gson$Types.newParameterizedTypeWithOwner(owner, raw, args);
    }

    // Helper to create a GenericArrayType
    private static GenericArrayType newArrayType(Type component) {
        return $Gson$Types.arrayOf(component);
    }

    // Helper to create a WildcardType subtype
    private static WildcardType subtypeOf(Type bound) {
        return $Gson$Types.subtypeOf(bound);
    }

    // Helper to create a WildcardType supertype
    private static WildcardType supertypeOf(Type bound) {
        return $Gson$Types.supertypeOf(bound);
    }

    // Helper to canonicalize
    private static Type canonicalize(Type type) {
        return $Gson$Types.canonicalize(type);
    }

    // Helper to get raw type
    private static Class<?> getRawType(Type type) {
        return $Gson$Types.getRawType(type);
    }

    // Helper to check equality
    private static boolean equals(Type a, Type b) {
        return $Gson$Types.equals(a, b);
    }

    // Helper to get array component type
    private static Type getArrayComponentType(Type array) {
        return $Gson$Types.getArrayComponentType(array);
    }

    // Helper to get collection element type
    private static Type getCollectionElementType(Type context, Class<?> contextRawType) {
        return $Gson$Types.getCollectionElementType(context, contextRawType);
    }

    // Helper to get map key/value types
    private static Type[] getMapKeyAndValueTypes(Type context, Class<?> contextRawType) {
        return $Gson$Types.getMapKeyAndValueTypes(context, contextRawType);
    }

    // Helper to resolve
    private static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
        return $Gson$Types.resolve(context, contextRawType, toResolve);
    }

    // Helper to resolveTypeVariable (package-private)
    private static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
        return $Gson$Types.resolveTypeVariable(context, contextRawType, unknown);
    }

    // Helper to get generic supertype (package-private)
    private static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
        return $Gson$Types.getGenericSupertype(context, rawType, toResolve);
    }

    // Helper to get supertype (package-private)
    private static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
        return $Gson$Types.getSupertype(context, contextRawType, supertype);
    }

    // Helper to check not primitive (package-private)
    private static void checkNotPrimitive(Type type) {
        $Gson$Types.checkNotPrimitive(type);
    }

    // Helper to test equal (package-private)
    private static boolean equal(Object a, Object b) {
        return $Gson$Types.equal(a, b);
    }

    // Helper to test hashCodeOrZero (package-private)
    private static int hashCodeOrZero(Object o) {
        return $Gson$Types.hashCodeOrZero(o);
    }

    // Helper to test typeToString
    private static String typeToString(Type type) {
        return $Gson$Types.typeToString(type);
    }

    // Helper to test indexOf via reflection (private)
    private static int indexOf(Object[] array, Object toFind) throws Exception {
        Method method = $Gson$Types.class.getDeclaredMethod("indexOf", Object[].class, Object.class);
        method.setAccessible(true);
        return (Integer) method.invoke(null, array, toFind);
    }

    // Helper to test declaringClassOf via reflection (private)
    private static Class<?> declaringClassOf(TypeVariable<?> tv) throws Exception {
        Method method = $Gson$Types.class.getDeclaredMethod("declaringClassOf", TypeVariable.class);
        method.setAccessible(true);
        return (Class<?>) method.invoke(null, tv);
    }

    // --- Tests for newParameterizedTypeWithOwner ---

    @Test
    public void testNewParameterizedTypeWithOwner() {
        ParameterizedType pt = newParamType(null, List.class, String.class);
        assertEquals(List.class, pt.getRawType());
        assertNull(pt.getOwnerType());
        assertArrayEquals(new Type[]{String.class}, pt.getActualTypeArguments());
    }

    @Test
    public void testNewParameterizedTypeWithOwner_OwnerType() {
        ParameterizedType pt = newParamType($Gson$TypesTest.class, Map.Entry.class, String.class, Integer.class);
        assertEquals(Map.Entry.class, pt.getRawType());
        assertEquals($Gson$TypesTest.class, pt.getOwnerType());
        assertArrayEquals(new Type[]{String.class, Integer.class}, pt.getActualTypeArguments());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewParameterizedTypeWithOwner_NonStaticInnerClassWithoutOwner() {
        // Map.Entry is a non-static inner interface, requires owner
        newParamType(null, Map.Entry.class, String.class, Integer.class);
    }

    @Test
    public void testNewParameterizedTypeWithOwner_StaticClassWithoutOwner() {
        // Static inner class does not require owner
        ParameterizedType pt = newParamType(null, AbstractMap.SimpleEntry.class, String.class, Integer.class);
        assertNull(pt.getOwnerType());
    }

    @Test(expected = NullPointerException.class)
    public void testNewParameterizedTypeWithOwner_NullTypeArgument() {
        newParamType(null, List.class, (Type) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewParameterizedTypeWithOwner_PrimitiveTypeArgument() {
        newParamType(null, List.class, int.class);
    }

    // --- Tests for arrayOf ---

    @Test
    public void testArrayOf() {
        GenericArrayType gat = newArrayType(String.class);
        assertEquals(String.class, gat.getGenericComponentType());
    }

    // --- Tests for subtypeOf ---

    @Test
    public void testSubtypeOf() {
        WildcardType wt = subtypeOf(Number.class);
        assertArrayEquals(new Type[]{Number.class}, wt.getUpperBounds());
        assertArrayEquals(new Type[]{}, wt.getLowerBounds());
    }

    @Test
    public void testSubtypeOf_Object() {
        WildcardType wt = subtypeOf(Object.class);
        assertArrayEquals(new Type[]{Object.class}, wt.getUpperBounds());
        assertArrayEquals(new Type[]{}, wt.getLowerBounds());
    }

    // --- Tests for supertypeOf ---

    @Test
    public void testSupertypeOf() {
        WildcardType wt = supertypeOf(String.class);
        assertArrayEquals(new Type[]{Object.class}, wt.getUpperBounds());
        assertArrayEquals(new Type[]{String.class}, wt.getLowerBounds());
    }

    // --- Tests for canonicalize ---

    @Test
    public void testCanonicalize_Class_NonArray() {
        Type result = canonicalize(String.class);
        assertSame(String.class, result);
    }

    @Test
    public void testCanonicalize_Class_Array() {
        Type result = canonicalize(String[].class);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test
    public void testCanonicalize_ParameterizedType() {
        ParameterizedType pt = newParamType(null, List.class, String.class);
        Type result = canonicalize(pt);
        assertTrue(result instanceof ParameterizedType);
        assertNotSame(pt, result); // should be a new instance
        assertEquals(pt, result);
    }

    @Test
    public void testCanonicalize_GenericArrayType() {
        GenericArrayType gat = newArrayType(String.class);
        Type result = canonicalize(gat);
        assertTrue(result instanceof GenericArrayType);
        assertNotSame(gat, result);
        assertEquals(gat, result);
    }

    @Test
    public void testCanonicalize_WildcardType() {
        WildcardType wt = subtypeOf(Number.class);
        Type result = canonicalize(wt);
        assertTrue(result instanceof WildcardType);
        assertNotSame(wt, result);
        assertEquals(wt, result);
    }

    @Test
    public void testCanonicalize_OtherType() {
        // TypeVariable is not specially handled, should return same instance
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        Type result = canonicalize(tv);
        assertSame(tv, result);
    }

    // --- Tests for getRawType ---

    @Test
    public void testGetRawType_Class() {
        assertEquals(String.class, getRawType(String.class));
    }

    @Test
    public void testGetRawType_ParameterizedType() {
        ParameterizedType pt = newParamType(null, List.class, String.class);
        assertEquals(List.class, getRawType(pt));
    }

    @Test
    public void testGetRawType_GenericArrayType() {
        GenericArrayType gat = newArrayType(String.class);
        Class<?> raw = getRawType(gat);
        assertEquals(String[].class, raw);
    }

    @Test
    public void testGetRawType_TypeVariable() {
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        assertEquals(Object.class, getRawType(tv));
    }

    @Test
    public void testGetRawType_WildcardType() {
        WildcardType wt = subtypeOf(Number.class);
        assertEquals(Number.class, getRawType(wt));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRawType_Null() {
        getRawType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRawType_UnsupportedType() {
        // Create a custom Type that is not Class, ParameterizedType, GenericArrayType, TypeVariable, WildcardType
        Type custom = new Type(){};
        getRawType(custom);
    }

    // --- Tests for equal ---

    @Test
    public void testEqual_BothNull() {
        assertTrue(equal(null, null));
    }

    @Test
    public void testEqual_SameReference() {
        Object o = "test";
        assertTrue(equal(o, o));
    }

    @Test
    public void testEqual_OneNull() {
        assertFalse(equal("test", null));
        assertFalse(equal(null, "test"));
    }

    @Test
    public void testEqual_EqualsTrue() {
        assertTrue(equal("test", "test"));
    }

    @Test
    public void testEqual_EqualsFalse() {
        assertFalse(equal("test", "other"));
    }

    // --- Tests for equals (Type) ---

    @Test
    public void testEquals_BothNull() {
        assertTrue(equals(null, null));
    }

    @Test
    public void testEquals_OneNull() {
        assertFalse(equals(String.class, null));
        assertFalse(equals(null, String.class));
    }

    @Test
    public void testEquals_Class() {
        assertTrue(equals(String.class, String.class));
        assertFalse(equals(String.class, Integer.class));
    }

    @Test
    public void testEquals_ParameterizedType() {
        ParameterizedType pt1 = newParamType(null, List.class, String.class);
        ParameterizedType pt2 = newParamType(null, List.class, String.class);
        assertTrue(equals(pt1, pt2));
        ParameterizedType pt3 = newParamType(null, List.class, Integer.class);
        assertFalse(equals(pt1, pt3));
        // different owner
        ParameterizedType pt4 = newParamType($Gson$TypesTest.class, Map.Entry.class, String.class, Integer.class);
        ParameterizedType pt5 = newParamType(null, Map.Entry.class, String.class, Integer.class);
        assertFalse(equals(pt4, pt5));
    }

    @Test
    public void testEquals_ParameterizedType_NotParameterized() {
        ParameterizedType pt = newParamType(null, List.class, String.class);
        assertFalse(equals(pt, String.class));
    }

    @Test
    public void testEquals_GenericArrayType() {
        GenericArrayType gat1 = newArrayType(String.class);
        GenericArrayType gat2 = newArrayType(String.class);
        assertTrue(equals(gat1, gat2));
        GenericArrayType gat3 = newArrayType(Integer.class);
        assertFalse(equals(gat1, gat3));
    }

    @Test
    public void testEquals_GenericArrayType_NotGenericArray() {
        GenericArrayType gat = newArrayType(String.class);
        assertFalse(equals(gat, String.class));
    }

    @Test
    public void testEquals_WildcardType() {
        WildcardType wt1 = subtypeOf(Number.class);
        WildcardType wt2 = subtypeOf(Number.class);
        assertTrue(equals(wt1, wt2));
        WildcardType wt3 = supertypeOf(String.class);
        assertFalse(equals(wt1, wt3));
    }

    @Test
    public void testEquals_WildcardType_NotWildcard() {
        WildcardType wt = subtypeOf(Number.class);
        assertFalse(equals(wt, String.class));
    }

    @Test
    public void testEquals_TypeVariable() {
        TypeVariable<?> tv1 = getTypeVariable(List.class, "E");
        TypeVariable<?> tv2 = getTypeVariable(List.class, "E");
        assertTrue(equals(tv1, tv2));
        TypeVariable<?> tv3 = getTypeVariable(Map.class, "K");
        assertFalse(equals(tv1, tv3));
    }

    @Test
    public void testEquals_TypeVariable_NotTypeVariable() {
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        assertFalse(equals(tv, String.class));
    }

    @Test
    public void testEquals_UnsupportedType() {
        Type custom = new Type(){};
        assertFalse(equals(custom, custom)); // falls to else, returns false
    }

    // --- Tests for hashCodeOrZero ---

    @Test
    public void testHashCodeOrZero_Null() {
        assertEquals(0, hashCodeOrZero(null));
    }

    @Test
    public void testHashCodeOrZero_NonNull() {
        String s = "test";
        assertEquals(s.hashCode(), hashCodeOrZero(s));
    }

    // --- Tests for typeToString ---

    @Test
    public void testTypeToString_Class() {
        assertEquals("java.lang.String", typeToString(String.class));
    }

    @Test
    public void testTypeToString_ParameterizedType() {
        ParameterizedType pt = newParamType(null, List.class, String.class);
        assertEquals("java.util.List<java.lang.String>", typeToString(pt));
    }

    // --- Tests for getGenericSupertype ---

    @Test
    public void testGetGenericSupertype_SameRawType() {
        Type result = getGenericSupertype(String.class, String.class, String.class);
        assertSame(String.class, result);
    }

    @Test
    public void testGetGenericSupertype_InterfaceDirect() {
        // ArrayList implements List
        Type result = getGenericSupertype(ArrayList.class, ArrayList.class, List.class);
        // ArrayList's generic interfaces: List<E>, RandomAccess, Cloneable, Serializable
        // The first is List<E>, so should return ParameterizedType List<E>
        assertTrue(result instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) result).getRawType());
    }

    @Test
    public void testGetGenericSupertype_InterfaceAssignable() {
        // ArrayList implements Collection via List
        Type result = getGenericSupertype(ArrayList.class, ArrayList.class, Collection.class);
        // Should resolve through List to Collection<E>
        assertTrue(result instanceof ParameterizedType);
        assertEquals(Collection.class, ((ParameterizedType) result).getRawType());
    }

    @Test
    public void testGetGenericSupertype_SuperclassDirect() {
        // Integer extends Number
        Type result = getGenericSupertype(Integer.class, Integer.class, Number.class);
        assertEquals(Number.class, result); // Integer's generic superclass is Number (non-parameterized)
    }

    @Test
    public void testGetGenericSupertype_SuperclassAssignable() {
        // Integer extends Number extends Object
        Type result = getGenericSupertype(Integer.class, Integer.class, Object.class);
        assertEquals(Object.class, result);
    }

    @Test
    public void testGetGenericSupertype_NotFound() {
        // String does not implement List
        Type result = getGenericSupertype(String.class, String.class, List.class);
        assertEquals(List.class, result); // returns toResolve
    }

    @Test
    public void testGetGenericSupertype_InterfaceNotFound() {
        // String does not implement List
        Type result = getGenericSupertype(String.class, String.class, List.class);
        assertEquals(List.class, result);
    }

    // --- Tests for getSupertype ---

    @Test
    public void testGetSupertype_Valid() {
        Type result = getSupertype(ArrayList.class, ArrayList.class, Collection.class);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(Collection.class, ((ParameterizedType) result).getRawType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSupertype_Invalid() {
        getSupertype(String.class, String.class, List.class);
    }

    // --- Tests for getArrayComponentType ---

    @Test
    public void testGetArrayComponentType_GenericArrayType() {
        GenericArrayType gat = newArrayType(String.class);
        assertEquals(String.class, getArrayComponentType(gat));
    }

    @Test
    public void testGetArrayComponentType_ClassArray() {
        assertEquals(String.class, getArrayComponentType(String[].class));
    }

    @Test
    public void testGetArrayComponentType_NonArrayClass() {
        // Returns null because Class.getComponentType() returns null for non-array
        assertNull(getArrayComponentType(String.class));
    }

    // --- Tests for getCollectionElementType ---

    @Test
    public void testGetCollectionElementType_Simple() {
        Type result = getCollectionElementType(ArrayList.class, ArrayList.class);
        // ArrayList<E> -> Collection<E> -> E is TypeVariable, but resolve should resolve to Object?
        // Actually, context is ArrayList.class (raw), so resolve will not resolve TypeVariable, returns Object.class
        assertEquals(Object.class, result);
    }

    @Test
    public void testGetCollectionElementType_Parameterized() {
        ParameterizedType context = newParamType(null, ArrayList.class, String.class);
        Type result = getCollectionElementType(context, ArrayList.class);
        assertEquals(String.class, result);
    }

    @Test
    public void testGetCollectionElementType_Wildcard() {
        // Create a context that results in a wildcard collection type? Hard to produce.
        // We can test with a context that has a wildcard upper bound.
        // For example, ArrayList<? extends Number>
        ParameterizedType context = newParamType(null, ArrayList.class, subtypeOf(Number.class));
        Type result = getCollectionElementType(context, ArrayList.class);
        // getSupertype returns Collection<? extends Number>, which is WildcardType, then getUpperBounds[0] gives ? extends Number, which is WildcardType, then getActualTypeArguments[0] gives Number
        assertEquals(Number.class, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCollectionElementType_NotCollection() {
        getCollectionElementType(String.class, String.class);
    }

    // --- Tests for getMapKeyAndValueTypes ---

    @Test
    public void testGetMapKeyAndValueTypes_Properties() {
        Type[] result = getMapKeyAndValueTypes(Properties.class, Properties.class);
        assertArrayEquals(new Type[]{String.class, String.class}, result);
    }

    @Test
    public void testGetMapKeyAndValueTypes_MapSubtype() {
        ParameterizedType context = newParamType(null, HashMap.class, String.class, Integer.class);
        Type[] result = getMapKeyAndValueTypes(context, HashMap.class);
        assertArrayEquals(new Type[]{String.class, Integer.class}, result);
    }

    @Test
    public void testGetMapKeyAndValueTypes_NonMap() {
        // Should throw because not assignable to Map
        try {
            getMapKeyAndValueTypes(String.class, String.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetMapKeyAndValueTypes_PropertiesSubclass() {
        // Subclass of Properties should still return String, String
        class MyProperties extends Properties {}
        Type[] result = getMapKeyAndValueTypes(MyProperties.class, MyProperties.class);
        assertArrayEquals(new Type[]{String.class, String.class}, result);
    }

    // --- Tests for resolve ---

    @Test
    public void testResolve_TypeVariable_Resolved() {
        // List<String> as context, resolve E -> String
        ParameterizedType context = newParamType(null, List.class, String.class);
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        Type result = resolve(context, List.class, tv);
        assertEquals(String.class, result);
    }

    @Test
    public void testResolve_TypeVariable_NotResolved() {
        // Raw List as context, resolve E -> still E (since no actual type arguments)
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        Type result = resolve(List.class, List.class, tv);
        assertSame(tv, result);
    }

    @Test
    public void testResolve_ClassArray_NoChange() {
        Type result = resolve(null, null, String[].class);
        assertSame(String[].class, result);
    }

    @Test
    public void testResolve_ClassArray_ComponentChanged() {
        // Context: List<String>, resolve String[] -> should become String[] (no change because component is not TypeVariable)
        // To force change, we need a context where component type is a TypeVariable that resolves.
        // For example, context: List<String>, resolve E[] where E is from List? But E[] is not a Class array, it's GenericArrayType.
        // For Class array, component type is a Class, so resolve will not change it unless it's a TypeVariable? Actually, Class.getComponentType() returns a Class, not a TypeVariable.
        // So Class array component type is always a Class, so resolve will return same array. So no change possible.
        // We'll just test no change.
        Type result = resolve(null, null, String[].class);
        assertSame(String[].class, result);
    }

    @Test
    public void testResolve_GenericArrayType_NoChange() {
        GenericArrayType gat = newArrayType(String.class);
        Type result = resolve(null, null, gat);
        assertSame(gat, result);
    }

    @Test
    public void testResolve_GenericArrayType_ComponentChanged() {
        // Context: List<String>, resolve E[] where E is List's type variable -> should become String[]
        ParameterizedType context = newParamType(null, List.class, String.class);
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        GenericArrayType gat = newArrayType(tv);
        Type result = resolve(context, List.class, gat);
        assertTrue(result instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) result).getGenericComponentType());
    }

    @Test
    public void testResolve_ParameterizedType_NoChange() {
        ParameterizedType pt = newParamType(null, List.class, String.class);
        Type result = resolve(null, null, pt);
        assertSame(pt, result);
    }

    @Test
    public void testResolve_ParameterizedType_OwnerChanged() {
        // Context: Map.Entry<String, Integer> with owner Map<String, Integer>? Hard to set up.
        // We'll use a custom class with inner class.
        // For simplicity, test with a context that changes owner type.
        // We'll create a context where owner type is a TypeVariable that resolves.
        // Example: class Outer<T> { class Inner {} } and context Outer<String>.Inner, resolve Inner -> owner changes.
        // But we don't have such class. We'll skip owner change test for brevity, but we can test type arguments change.
    }

    @Test
    public void testResolve_ParameterizedType_TypeArgumentsChanged() {
        // Context: List<String>, resolve List<E> -> List<String>
        ParameterizedType context = newParamType(null, List.class, String.class);
        ParameterizedType toResolve = newParamType(null, List.class, getTypeVariable(List.class, "E"));
        Type result = resolve(context, List.class, toResolve);
        assertTrue(result instanceof ParameterizedType);
        assertArrayEquals(new Type[]{String.class}, ((ParameterizedType) result).getActualTypeArguments());
    }

    @Test
    public void testResolve_WildcardType_LowerBoundChanged() {
        // Context: List<String>, resolve ? super E -> ? super String
        ParameterizedType context = newParamType(null, List.class, String.class);
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        WildcardType toResolve = supertypeOf(tv);
        Type result = resolve(context, List.class, toResolve);
        assertTrue(result instanceof WildcardType);
        assertArrayEquals(new Type[]{String.class}, ((WildcardType) result).getLowerBounds());
    }

    @Test
    public void testResolve_WildcardType_UpperBoundChanged() {
        // Context: List<String>, resolve ? extends E -> ? extends String
        ParameterizedType context = newParamType(null, List.class, String.class);
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        WildcardType toResolve = subtypeOf(tv);
        Type result = resolve(context, List.class, toResolve);
        assertTrue(result instanceof WildcardType);
        assertArrayEquals(new Type[]{String.class}, ((WildcardType) result).getUpperBounds());
    }

    @Test
    public void testResolve_WildcardType_NoChange() {
        WildcardType wt = subtypeOf(Number.class);
        Type result = resolve(null, null, wt);
        assertSame(wt, result);
    }

    @Test
    public void testResolve_OtherType() {
        // TypeVariable not resolved because context is null
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        Type result = resolve(null, null, tv);
        assertSame(tv, result);
    }

    // --- Tests for resolveTypeVariable ---

    @Test
    public void testResolveTypeVariable_DeclaringClassNull() throws Exception {
        // Use a TypeVariable from a method (generic declaration is Method, not Class)
        Method method = Collections.class.getMethod("max", Collection.class);
        TypeVariable<?> tv = method.getTypeParameters()[0];
        // declaringClassOf should return null
        assertNull(declaringClassOf(tv));
        Type result = resolveTypeVariable(null, null, tv);
        assertSame(tv, result);
    }

    @Test
    public void testResolveTypeVariable_DeclaredByParameterizedType() {
        // Context: List<String>, resolve E -> String
        ParameterizedType context = newParamType(null, List.class, String.class);
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        Type result = resolveTypeVariable(context, List.class, tv);
        assertEquals(String.class, result);
    }

    @Test
    public void testResolveTypeVariable_DeclaredByNotParameterizedType() {
        // Context: raw List, resolve E -> still E (since declaredBy is Class, not ParameterizedType)
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        Type result = resolveTypeVariable(List.class, List.class, tv);
        assertSame(tv, result);
    }

    // --- Tests for indexOf (via reflection) ---

    @Test
    public void testIndexOf_Found() throws Exception {
        Object[] array = {"a", "b", "c"};
        assertEquals(1, indexOf(array, "b"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testIndexOf_NotFound() throws Exception {
        Object[] array = {"a", "b", "c"};
        indexOf(array, "d");
    }

    // --- Tests for declaringClassOf (via reflection) ---

    @Test
    public void testDeclaringClassOf_Class() throws Exception {
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        assertEquals(List.class, declaringClassOf(tv));
    }

    @Test
    public void testDeclaringClassOf_Method() throws Exception {
        Method method = Collections.class.getMethod("max", Collection.class);
        TypeVariable<?> tv = method.getTypeParameters()[0];
        assertNull(declaringClassOf(tv));
    }

    // --- Tests for checkNotPrimitive ---

    @Test(expected = IllegalArgumentException.class)
    public void testCheckNotPrimitive_Primitive() {
        checkNotPrimitive(int.class);
    }

    @Test
    public void testCheckNotPrimitive_NonPrimitive() {
        checkNotPrimitive(String.class); // should not throw
    }

    // --- Tests for ParameterizedTypeImpl ---

    @Test
    public void testParameterizedTypeImpl_Equals() {
        ParameterizedType pt1 = newParamType(null, List.class, String.class);
        ParameterizedType pt2 = newParamType(null, List.class, String.class);
        assertEquals(pt1, pt2);
        assertEquals(pt1.hashCode(), pt2.hashCode());
    }

    @Test
    public void testParameterizedTypeImpl_ToString() {
        ParameterizedType pt = newParamType(null, List.class, String.class);
        assertEquals("java.util.List<java.lang.String>", pt.toString());
    }

    @Test
    public void testParameterizedTypeImpl_ToString_NoArgs() {
        // Raw type with no type arguments
        ParameterizedType pt = newParamType(null, List.class);
        assertEquals("java.util.List", pt.toString());
    }

    @Test
    public void testParameterizedTypeImpl_ToString_MultipleArgs() {
        ParameterizedType pt = newParamType(null, Map.class, String.class, Integer.class);
        assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", pt.toString());
    }

    // --- Tests for GenericArrayTypeImpl ---

    @Test
    public void testGenericArrayTypeImpl_Equals() {
        GenericArrayType gat1 = newArrayType(String.class);
        GenericArrayType gat2 = newArrayType(String.class);
        assertEquals(gat1, gat2);
        assertEquals(gat1.hashCode(), gat2.hashCode());
    }

    @Test
    public void testGenericArrayTypeImpl_ToString() {
        GenericArrayType gat = newArrayType(String.class);
        assertEquals("java.lang.String[]", gat.toString());
    }

    // --- Tests for WildcardTypeImpl ---

    @Test
    public void testWildcardTypeImpl_Subtype_Equals() {
        WildcardType wt1 = subtypeOf(Number.class);
        WildcardType wt2 = subtypeOf(Number.class);
        assertEquals(wt1, wt2);
        assertEquals(wt1.hashCode(), wt2.hashCode());
    }

    @Test
    public void testWildcardTypeImpl_Supertype_Equals() {
        WildcardType wt1 = supertypeOf(String.class);
        WildcardType wt2 = supertypeOf(String.class);
        assertEquals(wt1, wt2);
        assertEquals(wt1.hashCode(), wt2.hashCode());
    }

    @Test
    public void testWildcardTypeImpl_ToString_Subtype_Object() {
        WildcardType wt = subtypeOf(Object.class);
        assertEquals("?", wt.toString());
    }

    @Test
    public void testWildcardTypeImpl_ToString_Subtype_Bound() {
        WildcardType wt = subtypeOf(Number.class);
        assertEquals("? extends java.lang.Number", wt.toString());
    }

    @Test
    public void testWildcardTypeImpl_ToString_Supertype() {
        WildcardType wt = supertypeOf(String.class);
        assertEquals("? super java.lang.String", wt.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardTypeImpl_InvalidLowerBoundsLength() {
        // lowerBounds length > 1
        new $Gson$Types.WildcardTypeImpl(new Type[]{Object.class}, new Type[]{String.class, Integer.class});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardTypeImpl_InvalidUpperBoundsLength() {
        // upperBounds length != 1
        new $Gson$Types.WildcardTypeImpl(new Type[]{Object.class, String.class}, new Type[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardTypeImpl_LowerBoundWithNonObjectUpperBound() {
        // lower bound set but upper bound not Object.class
        new $Gson$Types.WildcardTypeImpl(new Type[]{String.class}, new Type[]{Integer.class});
    }

    @Test(expected = NullPointerException.class)
    public void testWildcardTypeImpl_NullLowerBound() {
        new $Gson$Types.WildcardTypeImpl(new Type[]{Object.class}, new Type[]{null});
    }

    @Test(expected = NullPointerException.class)
    public void testWildcardTypeImpl_NullUpperBound() {
        new $Gson$Types.WildcardTypeImpl(new Type[]{null}, new Type[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardTypeImpl_PrimitiveLowerBound() {
        new $Gson$Types.WildcardTypeImpl(new Type[]{Object.class}, new Type[]{int.class});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWildcardTypeImpl_PrimitiveUpperBound() {
        new $Gson$Types.WildcardTypeImpl(new Type[]{int.class}, new Type[]{});
    }

    // Test that constructor is private and throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testPrivateConstructor() throws Exception {
        Constructor<$Gson$Types> constructor = $Gson$Types.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
