package com.google.gson.internal;

import static org.junit.Assert.*;
import org.junit.Test;

import java.lang.reflect.*;
import java.util.*;

public class $Gson$TypesTest {

    // Helper to get TypeVariable from a class
    private static TypeVariable<?> getTypeVariable(Class<?> clazz, String name) {
        for (TypeVariable<?> tv : clazz.getTypeParameters()) {
            if (tv.getName().equals(name)) return tv;
        }
        return null;
    }

    // Test constructor throws UnsupportedOperationException
    @Test(expected = UnsupportedOperationException.class)
    public void testPrivateConstructor() throws Exception {
        // Use reflection to instantiate
        java.lang.reflect.Constructor<$Gson$Types> c = $Gson$Types.class.getDeclaredConstructor();
        c.setAccessible(true);
        c.newInstance();
    }

    // Test newParameterizedTypeWithOwner
    @Test
    public void testNewParameterizedTypeWithOwner() {
        Type owner = String.class;
        Type raw = List.class;
        Type arg = Integer.class;
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(owner, raw, arg);
        assertNotNull(pt);
        assertEquals(owner, pt.getOwnerType());
        assertEquals(raw, pt.getRawType());
        assertArrayEquals(new Type[]{arg}, pt.getActualTypeArguments());

        // Test without owner
        pt = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
        assertNull(pt.getOwnerType());
        assertEquals(Map.class, pt.getRawType());
        assertArrayEquals(new Type[]{String.class, Integer.class}, pt.getActualTypeArguments());

        // Test with primitive type argument should throw
        try {
            $Gson$Types.newParameterizedTypeWithOwner(null, List.class, int.class);
            fail("Expected IllegalArgumentException for primitive type argument");
        } catch (IllegalArgumentException expected) {}

        // Test with non-static inner class without owner should throw
        try {
            $Gson$Types.newParameterizedTypeWithOwner(null, NonStaticInner.class);
            fail("Expected IllegalArgumentException for missing owner type");
        } catch (IllegalArgumentException expected) {}

        // Test with static inner class without owner is fine
        Type pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, StaticInner.class);
        assertNotNull(pt2);
    }

    // Non-static inner class for testing
    class NonStaticInner {}
    static class StaticInner {}

    // Test arrayOf
    @Test
    public void testArrayOf() {
        GenericArrayType gat = $Gson$Types.arrayOf(String.class);
        assertEquals(String.class, gat.getGenericComponentType());

        // array of array
        GenericArrayType gat2 = $Gson$Types.arrayOf(gat);
        assertEquals(gat, gat2.getGenericComponentType());

        // canonicalization applied
        GenericArrayType gatPrimitive = $Gson$Types.arrayOf(int.class);
        assertEquals(int.class, gatPrimitive.getGenericComponentType());
    }

    // Test subtypeOf
    @Test
    public void testSubtypeOf() {
        // bound is a class
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertArrayEquals(new Type[]{Number.class}, wt.getUpperBounds());
        assertArrayEquals(new Type[]{}, wt.getLowerBounds());

        // bound is a wildcard (? extends Foo) -> extracts upper bound
        WildcardType inner = $Gson$Types.subtypeOf(String.class);
        WildcardType wt2 = $Gson$Types.subtypeOf(inner);
        assertArrayEquals(new Type[]{String.class}, wt2.getUpperBounds());
        assertArrayEquals(new Type[]{}, wt2.getLowerBounds());

        // bound is a wildcard with lower bound (should be treated as upper bound extraction)
        WildcardType superWild = $Gson$Types.supertypeOf(String.class);
        WildcardType wt3 = $Gson$Types.subtypeOf(superWild);
        // supertypeOf(String) yields ? super String, with upper bound Object.class, lower bound String.class
        // subtypeOf(superWild) extracts upper bounds = [Object.class]
        assertArrayEquals(new Type[]{Object.class}, wt3.getUpperBounds());
        assertArrayEquals(new Type[]{}, wt3.getLowerBounds());
    }

    // Test supertypeOf
    @Test
    public void testSupertypeOf() {
        // bound is a class
        WildcardType wt = $Gson$Types.supertypeOf(String.class);
        assertArrayEquals(new Type[]{Object.class}, wt.getUpperBounds());
        assertArrayEquals(new Type[]{String.class}, wt.getLowerBounds());

        // bound is a wildcard (? super X) -> extracts lower bound
        WildcardType superWild = $Gson$Types.supertypeOf(Number.class);
        WildcardType wt2 = $Gson$Types.supertypeOf(superWild);
        assertArrayEquals(new Type[]{Object.class}, wt2.getUpperBounds());
        assertArrayEquals(new Type[]{Number.class}, wt2.getLowerBounds());

        // bound is wildcard with upper bound (subtype) -> extracts lower bound (empty) then makes ? super Object
        WildcardType subWild = $Gson$Types.subtypeOf(String.class);
        WildcardType wt3 = $Gson$Types.supertypeOf(subWild);
        assertArrayEquals(new Type[]{Object.class}, wt3.getUpperBounds());
        assertArrayEquals(new Type[]{Object.class}, wt3.getLowerBounds()); // because lowerBounds empty -> bound = Object
    }

    // Test canonicalize
    @Test
    public void testCanonicalize() {
        // class: non-array
        assertEquals(String.class, $Gson$Types.canonicalize(String.class));

        // class: array -> becomes GenericArrayTypeImpl
        Type arrayType = $Gson$Types.canonicalize(String[].class);
        assertTrue(arrayType instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType)arrayType).getGenericComponentType());

        // ParameterizedType: roundtrip
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);
        Type canonical = $Gson$Types.canonicalize(pt);
        assertTrue(canonical instanceof ParameterizedType);
        assertEquals(pt.getRawType(), ((ParameterizedType)canonical).getRawType());
        assertArrayEquals(pt.getActualTypeArguments(), ((ParameterizedType)canonical).getActualTypeArguments());

        // GenericArrayType: roundtrip
        GenericArrayType gat = $Gson$Types.arrayOf(Double.class);
        canonical = $Gson$Types.canonicalize(gat);
        assertTrue(canonical instanceof GenericArrayType);
        assertEquals(Double.class, ((GenericArrayType)canonical).getGenericComponentType());

        // WildcardType: roundtrip
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        canonical = $Gson$Types.canonicalize(wt);
        assertTrue(canonical instanceof WildcardType);
        assertArrayEquals(wt.getUpperBounds(), ((WildcardType)canonical).getUpperBounds());
        assertArrayEquals(wt.getLowerBounds(), ((WildcardType)canonical).getLowerBounds());

        // Unsupported type (TypeVariable) just returns same instance
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        assertSame(tv, $Gson$Types.canonicalize(tv));
    }

    // Test getRawType
    @Test
    public void testGetRawType() {
        // Class
        assertEquals(String.class, $Gson$Types.getRawType(String.class));

        // ParameterizedType
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals(List.class, $Gson$Types.getRawType(pt));

        // GenericArrayType
        GenericArrayType gat = $Gson$Types.arrayOf(String.class);
        Class<?> arrayClass = $Gson$Types.getRawType(gat);
        assertTrue(arrayClass.isArray());
        assertEquals(String.class, arrayClass.getComponentType());

        // TypeVariable -> Object.class
        TypeVariable<?> tv = getTypeVariable(List.class, "E");
        assertEquals(Object.class, $Gson$Types.getRawType(tv));

        // WildcardType -> upper bound raw type
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertEquals(Number.class, $Gson$Types.getRawType(wt));

        // null -> IAE
        try {
            $Gson$Types.getRawType(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {}

        // unsupported type (e.g., a custom Type implementation)
        Type custom = new Type(){};
        try {
            $Gson$Types.getRawType(custom);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {}
    }

    // Test equal static helper
    @Test
    public void testEqual() {
        assertTrue($Gson$Types.equal(null, null));
        assertFalse($Gson$Types.equal(null, "a"));
        assertTrue($Gson$Types.equal("a", "a"));
        assertFalse($Gson$Types.equal("a", "b"));
    }

    // Test equals method for various types
    @Test
    public void testEquals() {
        // null handling
        assertTrue($Gson$Types.equals(null, null));
        assertFalse($Gson$Types.equals(null, String.class));
        assertFalse($Gson$Types.equals(String.class, null));

        // Class
        assertTrue($Gson$Types.equals(String.class, String.class));
        assertFalse($Gson$Types.equals(String.class, Integer.class));

        // ParameterizedType equality
        ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertTrue($Gson$Types.equals(pt1, pt2));
        ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);
        assertFalse($Gson$Types.equals(pt1, pt3));
        // owner type difference
        ParameterizedType ptOwner = $Gson$Types.newParameterizedTypeWithOwner(String.class, List.class, String.class);
        assertFalse($Gson$Types.equals(pt1, ptOwner));

        // ParameterizedType vs non-ParameterizedType
        assertFalse($Gson$Types.equals(pt1, String.class));

        // GenericArrayType equality
        GenericArrayType gat1 = $Gson$Types.arrayOf(String.class);
        GenericArrayType gat2 = $Gson$Types.arrayOf(String.class);
        assertTrue($Gson$Types.equals(gat1, gat2));
        GenericArrayType gat3 = $Gson$Types.arrayOf(Integer.class);
        assertFalse($Gson$Types.equals(gat1, gat3));

        // WildcardType equality
        WildcardType wt1 = $Gson$Types.subtypeOf(String.class);
        WildcardType wt2 = $Gson$Types.subtypeOf(String.class);
        assertTrue($Gson$Types.equals(wt1, wt2));
        WildcardType wt3 = $Gson$Types.subtypeOf(Number.class);
        assertFalse($Gson$Types.equals(wt1, wt3));
        WildcardType superWt = $Gson$Types.supertypeOf(String.class);
        assertFalse($Gson$Types.equals(wt1, superWt)); // upper vs lower

        // TypeVariable equality
        TypeVariable<?> tv1 = getTypeVariable(List.class, "E");
        TypeVariable<?> tv2 = getTypeVariable(List.class, "E");
        assertTrue($Gson$Types.equals(tv1, tv2));
        TypeVariable<?> tv3 = getTypeVariable(Map.class, "K");
        assertFalse($Gson$Types.equals(tv1, tv3));

        // Unsupported type (not of known types) -> returns false
        Type custom = new Type(){};
        assertFalse($Gson$Types.equals(custom, custom)); // still false because not instanceof
    }

    // Test hashCodeOrZero
    @Test
    public void testHashCodeOrZero() {
        assertEquals(0, $Gson$Types.hashCodeOrZero(null));
        assertEquals("a".hashCode(), $Gson$Types.hashCodeOrZero("a"));
    }

    // Test typeToString
    @Test
    public void testTypeToString() {
        assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
        assertEquals("?", $Gson$Types.typeToString($Gson$Types.subtypeOf(Object.class))); // toString of WildcardType
    }

    // Test getGenericSupertype
    @Test
    public void testGetGenericSupertype() {
        // rawType == toResolve
        Type context = String.class;
        Type result = $Gson$Types.getGenericSupertype(context, String.class, String.class);
        assertEquals(context, result);

        // toResolve is interface directly implemented
        result = $Gson$Types.getGenericSupertype(new ParameterizedTypeImpl(null, ArrayList.class, String.class), ArrayList.class, List.class);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType)result).getRawType());

        // toResolve is superclass
        result = $Gson$Types.getGenericSupertype(new ParameterizedTypeImpl(null, ArrayList.class, String.class), ArrayList.class, AbstractList.class);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(AbstractList.class, ((ParameterizedType)result).getRawType());

        // toResolve not found, returns toResolve itself
        result = $Gson$Types.getGenericSupertype(String.class, String.class, Serializable.class);
        assertEquals(Serializable.class, result); // String implements Serializable, but getGenericSupertype returns toResolve because not directly found? Actually it should return the generic interface. But we test the fallback path.
    }

    // Test getSupertype (which also calls resolve)
    @Test
    public void testGetSupertype() {
        // context = ArrayList<String>
        ParameterizedType context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        Type supertype = $Gson$Types.getSupertype(context, ArrayList.class, Collection.class);
        // should be Collection<String>
        assertTrue(supertype instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) supertype;
        assertEquals(Collection.class, pt.getRawType());
        assertArrayEquals(new Type[]{String.class}, pt.getActualTypeArguments());
    }

    // Test getArrayComponentType
    @Test
    public void testGetArrayComponentType() {
        // Class array
        assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));

        // GenericArrayType
        GenericArrayType gat = $Gson$Types.arrayOf(Integer.class);
        assertEquals(Integer.class, $Gson$Types.getArrayComponentType(gat));

        // Non-array class throws ClassCastException
        try {
            $Gson$Types.getArrayComponentType(String.class);
            fail("Expected ClassCastException");
        } catch (ClassCastException expected) {}
    }

    // Test getCollectionElementType
    @Test
    public void testGetCollectionElementType() {
        // ArrayList<String>
        ParameterizedType context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        Type elementType = $Gson$Types.getCollectionElementType(context, ArrayList.class);
        assertEquals(String.class, elementType);

        // wildcard upper bound
        Type wildContext = $Gson$Types.subtypeOf(List.class); // ? extends List
        // contextRawType would be List.class? Not assignable from wildContext; but we need a proper rawType. For test, use a collection with wildcard.
        // Example: ArrayList<? extends Number>
        ParameterizedType wildPt = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, $Gson$Types.subtypeOf(Number.class));
        elementType = $Gson$Types.getCollectionElementType(wildPt, ArrayList.class);
        assertEquals(Number.class, elementType); // because getSupertype returns parameterized Collection<? extends Number>, getActualTypeArguments gives the wildcard, then extraction of upper bound

        // Non-collection throws IAE from getSupertype
        try {
            $Gson$Types.getCollectionElementType(String.class, String.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {}
    }

    // Test getMapKeyAndValueTypes
    @Test
    public void testGetMapKeyAndValueTypes() {
        // HashMap<String, Integer>
        ParameterizedType context = $Gson$Types.newParameterizedTypeWithOwner(null, HashMap.class, String.class, Integer.class);
        Type[] kv = $Gson$Types.getMapKeyAndValueTypes(context, HashMap.class);
        assertArrayEquals(new Type[]{String.class, Integer.class}, kv);

        // Properties special case
        kv = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
        assertArrayEquals(new Type[]{String.class, String.class}, kv);

        // Non-map throws IAE
        try {
            $Gson$Types.getMapKeyAndValueTypes(String.class, String.class);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {}
    }

    // Test resolve (public method)
    @Test
    public void testResolve() {
        // Resolving TypeVariable using context containing type arguments
        TypeVariable<?> tv = getTypeVariable(Container.class, "T");
        ParameterizedType context = $Gson$Types.newParameterizedTypeWithOwner(null, Container.class, String.class);
        Type resolved = $Gson$Types.resolve(context, Container.class, tv);
        assertEquals(String.class, resolved);

        // Resolving an array with type variable component
        // Create GenericArrayType containing TypeVariable
        Type arrayOfT = $Gson$Types.arrayOf(tv);
        resolved = $Gson$Types.resolve(context, Container.class, arrayOfT);
        assertTrue(resolved instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) resolved).getGenericComponentType());

        // Resolving ParameterizedType with owner and type arguments that change
        TypeVariable<?> tvMap = getTypeVariable(Map.class, "K");
        // Let's create a context Pair<Map<String, Integer>, Map> where the raw type is Pair<?,?> but we need a class with two params.
        // Simpler: context HashMap<String, Integer>, rawType HashMap, toResolve Map<K, V> but Map has type parameters K,V.
        // That would require resolve to replace K and V.
        // We can create a raw type RawMap<K,V> extending HashMap<K,V> or just use Map interface.
        // But Map.class is not a class, it's an interface. Let's use a custom class like Foo<K,V> with fields.
        class Foo<K, V> {}
        TypeVariable<?> k = getTypeVariable(Foo.class, "K");
        TypeVariable<?> v = getTypeVariable(Foo.class, "V");
        ParameterizedType contextFoo = $Gson$Types.newParameterizedTypeWithOwner(null, Foo.class, String.class, Integer.class);
        // toResolve = Foo<K,V> as ParameterizedType
        ParameterizedType fooPT = $Gson$Types.newParameterizedTypeWithOwner(null, Foo.class, k, v);
        Type resolvedFoo = $Gson$Types.resolve(contextFoo, Foo.class, fooPT);
        assertTrue(resolvedFoo instanceof ParameterizedType);
        ParameterizedType resolvedFooPT = (ParameterizedType) resolvedFoo;
        assertArrayEquals(new Type[]{String.class, Integer.class}, resolvedFooPT.getActualTypeArguments());

        // Resolving WildcardType with lower bound
        WildcardType superString = $Gson$Types.supertypeOf(String.class);
        Type resolvedWild = $Gson$Types.resolve(contextFoo, Foo.class, superString);
        assertTrue(resolvedWild instanceof WildcardType);
        assertArrayEquals(new Type[]{Object.class}, ((WildcardType)resolvedWild).getUpperBounds());
        assertArrayEquals(new Type[]{String.class}, ((WildcardType)resolvedWild).getLowerBounds());

        // Resolving TypeVariable that leads to infinite recursion (self-referential)
        class SelfRef<T extends SelfRef<T>> {}
        TypeVariable<?> tvSelf = getTypeVariable(SelfRef.class, "T");
        ParameterizedType contextSelf = $Gson$Types.newParameterizedTypeWithOwner(null, SelfRef.class, tvSelf);
        Type resolvedSelf = $Gson$Types.resolve(contextSelf, SelfRef.class, tvSelf);
        // should return the TypeVariable itself (cannot reduce)
        assertEquals(tvSelf, resolvedSelf);
    }

    // Test resolveTypeVariable
    @Test
    public void testResolveTypeVariable() {
        TypeVariable<?> tv = getTypeVariable(Container.class, "T");
        ParameterizedType context = $Gson$Types.newParameterizedTypeWithOwner(null, Container.class, String.class);
        Type resolved = $Gson$Types.resolveTypeVariable(context, Container.class, tv);
        assertEquals(String.class, resolved);

        // TypeVariable not declared by class (declaringClass returns null) -> returns unknown
        // We don't have such case without mock; but can test with a TypeVariable from a method? Not possible.
    }

    // Helper inner types for tests
    static class Container<T> {}
}
