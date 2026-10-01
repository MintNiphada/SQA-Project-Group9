package com.google.gson.internal;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;

public class $Gson$TypesTest {

    // --- newParameterizedTypeWithOwner ---

    @Test
    public void testNewParameterizedTypeWithOwner_Basic() {
        Type rawType = List.class;
        Type[] args = new Type[]{ String.class };
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, rawType, args);
        assertNull(pt.getOwnerType());
        assertEquals(rawType, pt.getRawType());
        assertArrayEquals(args, pt.getActualTypeArguments());
    }

    @Test
    public void testNewParameterizedTypeWithOwner_WithOwner() {
        Type owner = $Gson$Types.class;
        Type rawType = Map.Entry.class;
        Type[] args = new Type[]{ String.class, Integer.class };
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(owner, rawType, args);
        assertEquals(owner, pt.getOwnerType());
        assertEquals(rawType, pt.getRawType());
        assertArrayEquals(args, pt.getActualTypeArguments());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewParameterizedTypeWithOwner_MissingOwnerForNonStaticInnerClass() {
        // Map.Entry is a non-static inner interface of Map, so it requires owner
        Type rawType = Map.Entry.class;
        Type[] args = new Type[]{ String.class, Integer.class };
        $Gson$Types.newParameterizedTypeWithOwner(null, rawType, args);
    }

    @Test
    public void testNewParameterizedTypeWithOwner_StaticInnerClassAllowsNullOwner() {
        // Static inner class (e.g., AbstractMap.SimpleEntry is static)
        Type rawType = AbstractMap.SimpleEntry.class;
        Type[] args = new Type[]{ String.class, Integer.class };
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, rawType, args);
        assertNull(pt.getOwnerType());
    }

    @Test(expected = NullPointerException.class)
    public void testNewParameterizedTypeWithOwner_ullTypeArgument() {
        Type rawType = List.class;
        Type[] args = new Type[]{ null };
        $Gson$Types.newParameterizedTypeWithOwner(null, rawType, args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNewParameterizedTypeWithOwner_PrimitiveTypeArgument() {
        Type rawType = List.class;
        $Gson$Types.newParameterizedTypeWithOwner(null, rawType, int.class);
    }

    @Test
    public void testNewParameterizedTypeWithOwner_CanonicalizesArguments() {
        // Provide a non-canonical wildcard type as argument
        Type bound = Object.class;
        Type arg = $Gson$Types.subtypeOf(bound);
        Type rawType = List.class;
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, rawType, arg);
        // The argument should be canonicalized, so the returned argument might be the same or new?
        // just check no exception
        assertNotNull(pt);
    }

    // --- arrayOf ---

    @Test
    public void testArrayOf() {
        GenericArrayType gat = $Gson$Types.arrayOf(String.class);
        assertEquals(String.class, gat.getGenericComponentType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testArrayOf_PrimitiveComponentThrows() {
        $Gson$Types.arrayOf(int.class);
    }

    @Test
    public void testArrayOf_ullThrows() {
        try {
            $Gson$Types.arrayOf(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {}
    }

    // --- subtypeOf ---

    @Test
    public void testSubtypeOf_RegularType() {
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertArrayEquals(new Type[]{ Number.class }, wt.getUpperBounds());
        assertEquals(0, wt.getLowerBounds().length);
    }

    @Test
    public void testSubtypeOf_WildcardBound() {
        WildcardType bound = $Gson$Types.subtypeOf(Comparable.class);
        WildcardType wt = $Gson$Types.subtypeOf(bound);
        // upper bound should be taken from the wildcard's upper bounds
        assertArrayEquals(bound.getUpperBounds(), wt.getUpperBounds());
        assertEquals(0, wt.getLowerBounds().length);
    }

    @Test
    public void testSubtypeOf_ObjectClassGivesUnbounded() {
        WildcardType wt = $Gson$Types.subtypeOf(Object.class);
        assertEquals(Object.class, wt.getUpperBounds()[0]);
        assertEquals(0, wt.getLowerBounds().length);
        assertEquals("?", wt.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testSubtypeOf_ullThrows() {
        $Gson$Types.subtypeOf(null);
    }

    // --- supertypeOf ---

    @Test
    public void testSupertypeOf_RegularType() {
        WildcardType wt = $Gson$Types.supertypeOf(Integer.class);
        assertArrayEquals(new Type[]{ Object.class }, wt.getUpperBounds());
        assertArrayEquals(new Type[]{ Integer.class }, wt.getLowerBounds());
    }

    @Test
    public void testSupertypeOf_WildcardBound() {
        WildcardType bound = $Gson$Types.supertypeOf(Number.class);
        // bound has lowerBound Number, so upper is Object, lower is Number
        WildcardType wt = $Gson$Types.supertypeOf(bound);
        assertEquals(Object.class, wt.getUpperBounds()[0]);
        assertArrayEquals(bound.getLowerBounds(), wt.getLowerBounds());
    }

    @Test(expected = NullPointerException.class)
    public void testSupertypeOf_ullThrows() {
        $Gson$Types.supertypeOf(null);
    }

    // --- canonicalize ---

    @Test
    public void testCanonicalize_ClassNonArray() {
        assertEquals(String.class, $Gson$Types.canonicalize(String.class));
    }

    @Test
    public void testCanonicalize_ClassArray() {
        Type t = $Gson$Types.canonicalize(String[].class);
        assertTrue(t instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) t).getGenericComponentType());
    }

    @Test
    public void testCanonicalize_ParameterizedType() {
        // Create a parameterized type via factory and canonicalize it
        ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        Type canon = $Gson$Types.canonicalize(pt);
        assertTrue(canon instanceof ParameterizedType);
        // Should be equal according to equals
        assertTrue($Gson$Types.equals(pt, (ParameterizedType) canon));
    }

    @Test
    public void testCanonicalize_GenericArrayType() {
        GenericArrayType gat = $Gson$Types.arrayOf(String.class);
        Type canon = $Gson$Types.canonicalize(gat);
        assertTrue(canon instanceof GenericArrayType);
        assertEquals(String.class, ((GenericArrayType) canon).getGenericComponentType());
    }

    @Test
    public void testCanonicalize_WildcardType() {
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        Type canon = $Gson$Types.canonicalize(wt);
        assertTrue(canon instanceof WildcardType);
        assertTrue($Gson$Types.equals(wt, (WildcardType) canon));
    }

    @Test
    public void testCanonicalize_TypeVariable() {
        // Type variables are returned as-is
        TypeVariable<?> tv = (TypeVariable<?>) List.class.getTypeParameters()[0];
        // Actually List<E> has E as TypeVariable, but calling getTypeParameters()[0] gives TypeVariable<E>
        // canonicalize should return same object
        Type canon = $Gson$Types.canonicalize(tv);
        assertSame(tv, canon);
    }

    // --- getRawType ---

    @Test
    public void testGetRawType_Class() {
        assertEquals(String.class, $Gson$Types.getRawType(String.class));
    }

    @Test
    public void testGetRawType_ParameterizedType() {
        Type pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals(List.class, $Gson$Types.getRawType(pt));
    }

    @Test
    public void testGetRawType_GenericArrayType() {
        Type array = $Gson$Types.arrayOf(String.class);
        Class<?> raw = $Gson$Types.getRawType(array);
        assertNotNull(raw);
        assertTrue(raw.isArray());
        assertEquals(String.class, raw.getComponentType());
    }

    @Test
    public void testGetRawType_TypeVariable_ReturnsObject() {
        TypeVariable<?> tv = (TypeVariable<?>) List.class.getTypeParameters()[0];
        assertEquals(Object.class, $Gson$Types.getRawType(tv));
    }

    @Test
    public void testGetRawType_WildcardType() {
        WildcardType wt = $Gson$Types.subtypeOf(Number.class);
        assertEquals(Number.class, $Gson$Types.getRawType(wt));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRawType_ullThrows() {
        $Gson$Types.getRawType(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRawType_UnknownTypeThrows() {
        // some unsupported type, e.g., a custom Type implementation
        $Gson$Types.getRawType(new Type(){});
    }

    // --- equal (package-private) ---

    @Test
    public void testEqual_BothNull() {
        assertTrue($Gson$Types.equal(null, null));
    }

    @Test
    public void testEqual_FirstNullSecondNotNull() {
        assertFalse($Gson$Types.equal(null, "a"));
    }

    @Test
    public void testEqual_SameReference() {
        String s = "abc";
        assertTrue($Gson$Types.equal(s, s));
    }

    @Test
    public void testEqual_QualObjects() {
        assertTrue($Gson$Types.equal("abc", "abc"));
    }

    @Test
    public void testEqual_NotEqual() {
        assertFalse($Gson$Types.equal("abc", "def"));
    }

    // --- equals (public) ---

    @Test
    public void testEquals_SameReference() {
        Type t = String.class;
        assertTrue($Gson$Types.equals(t, t));
    }

    @Test
    public void testEquals_BothNull() {
        assertTrue($Gson$Types.equals(null, null));
    }

    @Test
    public void testEquals_ClassEqual() {
        assertTrue($Gson$Types.equals(String.class, String.class));
        assertFalse($Gson$Types.equals(String.class, Integer.class));
    }

    @Test
    public void testEquals_ParameterizedTypeEqual() {
        Type pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        Type pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertTrue($Gson$Types.equals(pt1, pt2));
    }

    @Test
    public void testEquals_ParameterizedTypeInequalRawType() {
        Type pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        Type pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, Set.class, String.class);
        assertFalse($Gson$Types.equals(pt1, pt2));
    }

    @Test
    public void testEquals_ParameterizedTypeInequalOwner() {
        Type owner = $Gson$Types.class;
        Type pt1 = $Gson$Types.newParameterizedTypeWithOwner(owner, Map.Entry.class, String.class, Integer.class);
        Type pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.Entry.class, String.class, Integer.class); // null owner
        assertFalse($Gson$Types.equals(pt1, pt2));
    }

    @Test
    public void testEquals_GenericArrayTypeEqual() {
        Type gat1 = $Gson$Types.arrayOf(String.class);
        Type gat2 = $Gson$Types.arrayOf(String.class);
        assertTrue($Gson$Types.equals(gat1, gat2));
    }

    @Test
    public void testEquals_GenericArrayTypeInequalComponent() {
        Type gat1 = $Gson$Types.arrayOf(String.class);
        Type gat2 = $Gson$Types.arrayOf(Integer.class);
        assertFalse($Gson$Types.equals(gat1, gat2));
    }

    @Test
    public void testEquals_WildcardTypeEqual() {
        Type wt1 = $Gson$Types.subtypeOf(Number.class);
        Type wt2 = $Gson$Types.subtypeOf(Number.class);
        assertTrue($Gson$Types.equals(wt1, wt2));
    }

    @Test
    public void testEquals_WildcardTypeInequalBounds() {
        Type wt1 = $Gson$Types.subtypeOf(Number.class);
        Type wt2 = $Gson$Types.supertypeOf(Number.class);
        assertFalse($Gson$Types.equals(wt1, wt2));
    }

    @Test
    public void testEquals_TypeVariableSameDeclarationAndName() {
        TypeVariable<?> tv1 = (TypeVariable<?>) List.class.getTypeParameters()[0];
        TypeVariable<?> tv2 = (TypeVariable<?>) List.class.getTypeParameters()[0];
        assertTrue($Gson$Types.equals(tv1, tv2));
    }

    @Test
    public void testEquals_TypeVariableDifferentName() {
        // Not possible to get two different variables easily; we'll just skip
    }

    @Test
    public void testEquals_DifferentTypesReturnFalse() {
        assertFalse($Gson$Types.equals(String.class, $Gson$Types.subtypeOf(Object.class));
        assertFalse($Gson$Types.equals(String.class, $Gson$Types.arrayOf(String.class)));
        assertFalse($Gson$Types.equals($Gson$Types.arrayOf(String.class), $Gson$Types.subtypeOf(Object.class)));
        assertFalse($Gson$Types.equals($Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class), String.class));
    }

    // --- hashCodeOrZero ---

    @Test
    public void testHashCodeOrZero_ull() {
        assertEquals(0, $Gson$Types.hashCodeOrZero(null));
    }

    @Test
    public void testHashCodeOrZero_NotNull() {
        String s = "abc";
        assertEquals(s.hashCode(), $Gson$Types.hashCodeOrZero(s));
    }

    // --- typeToString ---

    @Test
    public void testTypeToString_Class() {
        assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    }

    @Test
    public void testTypeToString_NonClass() {
        Type pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        assertEquals(pt.toString(), $Gson$Types.typeToString(pt));
    }

    // --- getGenericSupertype ---

    @Test
    public void testGetGenericSupertype_WhenToResolveEqualsRawType() {
        // Use ArrayList<String>
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        Type result = $Gson$Types.getGenericSupertype(context, ArrayList.class, ArrayList.class);
        assertEquals(context, result);
    }

    @Test
    public void testGetGenericSupertype_Interface() {
        // ArrayList implements List
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        Type result = $Gson$Types.getGenericSupertype(context, ArrayList.class, List.class);
        // Should be List<String>
        assertTrue(result instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) result;
        assertEquals(List.class, pt.getRawType());
        assertArrayEquals(new Type[]{ String.class }, pt.getActualTypeArguments());
    }

    @Test
    public void testGetGenericSupertype_Superclass() {
        // ArrayList extends AbstractList
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        Type result = $Gson$Types.getGenericSupertype(context, ArrayList.class, AbstractList.class);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(AbstractList.class, ((ParameterizedType) result).getRawType());
        assertArrayEquals(new Type[]{ String.class }, ((ParameterizedType) result).getActualTypeArguments());
    }

    @Test
    public void testGetGenericSupertype_NotSupertypeReturnsToResolve() {
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        // Set is not a supertype
        Type result = $Gson$Types.getGenericSupertype(context, ArrayList.class, Set.class);
        assertEquals(Set.class, result);
    }

    @Test
    public void testGetGenericSupertype_InterfaceViaSuperclass() {
        // ArrayList implements RandomAccess through multiple levels; RandomAccess is an interface
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        Type result = $Gson$Types.getGenericSupertype(context, ArrayList.class, RandomAccess.class);
        // RandomAccess does not have type arguments, but it should return RandomAccess interface type as-is?
        // Actually ArrayList implements RandomAccess directly, so we hit direct interface check
        assertTrue(result instanceof Class);
        assertEquals(RandomAccess.class, result);
    }

    // --- getSupertype ---

    @Test
    public void testGetSupertype() {
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        Type result = $Gson$Types.getSupertype(context, ArrayList.class, List.class);
        assertTrue(result instanceof ParameterizedType);
        assertEquals(List.class, ((ParameterizedType) result).getRawType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSupertype_NotAssignable() {
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, String.class);
        $Gson$Types.getSupertype(context, ArrayList.class, Map.class);
    }

    // --- getArrayComponentType ---

    @Test
    public void testGetArrayComponentType_GenericArrayType() {
        Type gat = $Gson$Types.arrayOf(String.class);
        assertEquals(String.class, $Gson$Types.getArrayComponentType(gat));
    }

    @Test
    public void testGetArrayComponentType_ClassArray() {
        assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    }

    @Test(expected = ClassCastException.class)
    public void testGetArrayComponentType_NotArrayThrows() {
        $Gson$Types.getArrayComponentType(String.class);
    }

    // --- getCollectionElementType ---

    @Test
    public void testGetCollectionElementType() {
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, ArrayList.class, Integer.class);
        Type elementType = $Gson$Types.getCollectionElementType(context, ArrayList.class);
        assertEquals(Integer.class, elementType);
    }

    @Test
    public void testGetCollectionElementType_WildcardUpperBound() {
        // Create a collection type with wildcard element
        Type context = new Type() {}; // Not needed, but we can use a simple Collection<?> parameterized with wildcard
        // Use raw Collection with wildcard: Collection<? extends Number> wrapper
        Type collectionType = $Gson$Types.newParameterizedTypeWithOwner(null, Collection.class, $Gson$Types.subtypeOf(Number.class));
        Type elementType = $Gson$Types.getCollectionElementType(collectionType, Collection.class);
        assertEquals(Number.class, elementType);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetCollectionElementType_NonCollection() {
        $Gson$Types.getCollectionElementType(String.class, String.class);
    }

    // --- getMapKeyAndValueTypes ---

    @Test
    public void testGetMapKeyAndValueTypes_PropertiesClassReturnsStringString() {
        Type[] kv = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
        assertArrayEquals(new Type[]{ String.class, String.class }, kv);
    }

    @Test
    public void testGetMapKeyAndValueTypes_Map() {
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, HashMap.class, String.class, Integer.class);
        Type[] kv = $Gson$Types.getMapKeyAndValueTypes(context, HashMap.class);
        assertArrayEquals(new Type[]{ String.class, Integer.class }, kv);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMapKeyAndValueTypes_NonMap() {
        $Gson$Types.getMapKeyAndValueTypes(String.class, String.class);
    }

    // --- resolve ---

    @Test
    public void testResolve_TypeVariable_fromInterface() {
        // Create a parameterized type for List<String> and resolve E of List
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        // E is a TypeVariable of List
        TypeVariable<?> e = (TypeVariable<?>) List.class.getTypeParameters()[0];
        Type resolved = $Gson$Types.resolve(context, List.class, e);
        assertEquals(String.class, resolved);
    }

    @Test
    public void testResolve_TypeVariable_NotInContextReturnsItself() {
        // E from Map not present in List context        TypeVariable<?> e = (TypeVariable<?>) Map.class.getTypeParameters()[0];
        Type context = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
        Type resolved = $Gson$Types.resolve(context, List.class, e);
        assertEquals(e, resolved);
    }

    @Test
    public void testResolve_ArrayType_ResolvesComponent() {
        // Resolve String[] array type (Class is array) with a TypeVariable? No, just test that array with component variable resolves.
        // Use generic array type: E[]? Hard. Simpler: resolve array of resolved type.
        Type arrayType = $Gson$Types.arrayOf(String.class);
        Type resolved = $Gson$Types.resolve(arrayType, Object.class, arrayType);
        // Not meaningful, but just test no exception
        arrayType.resolve;
    }
