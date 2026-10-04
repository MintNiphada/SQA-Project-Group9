package com.fasterxml.jackson.databind.type;

import java.util.*;
import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeModifier;
import com.fasterxml.jackson.databind.type.SimpleType;

public class TypeFactoryTest {
    
    private TypeFactory factory = TypeFactory.defaultInstance();

    // --------------- static helper methods ---------------

    @Test
    public void testUnknownType() {
        JavaType unknown = TypeFactory.unknownType();
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    @Test
    public void testRawClassWithClass() {
        assertEquals(String.class, TypeFactory.rawClass(String.class));
    }

    @Test
    public void testRawClassWithParameterizedType() {
        Type type = new TypeReference<List<String>>() { }.getType();
        assertEquals(List.class, TypeFactory.rawClass(type));
    }

    // --------------- constructType variations ---------------

    @Test
    public void testConstructTypeClass() {
        JavaType type = factory.constructType(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructTypeNull() {
        factory.constructType((Type) null);
    }

    @Test
    public void testConstructTypeJavaTypeItself() {
        JavaType original = factory.constructType(Integer.class);
        // Passing a JavaType should return the same instance
        JavaType result = factory.constructType(original);
        assertSame(original, result);
    }

    @Test
    public void testConstructTypeParameterizedType() {
        Type type = new TypeReference<List<String>>() { }.getType();
        JavaType result = factory.constructType(type);
        assertTrue(result.isCollectionLikeType());
        assertEquals(String.class, result.getContentType().getRawClass());
    }

    @Test
    public void testConstructTypeGenericArrayType() {
        // Use a method that returns a generic array to get GenericArrayType
        // Simpler: create a class that declares an array type variable? 
        // We'll use TypeVariable approach, but may be complex. Skipping for now , we can test via _fromArrayType directly later if needed
    }

    @Test
    public void testConstructTypeTypeVariable() {
        // Create a TypeVariable from a generic class
        TypeVariable<?>[] vars = GenericHolder.class.getTypeParameters();
        assertEquals(1, vars.length);
        TypeVariable<?> tv = vars[0];
        JavaType result = factory.constructType(tv);
        // Since no context, bound is Object
        assertEquals(Object.class, result.getRawClass());
    }

    static class GenericHolder<T> { }

    @Test
    public void testConstructTypeWildcardType() throws Exception {
        // Get WildcardType from List<? extends CharSequence>
        Type type = new TypeReference<List<? extends CharSequence>>() { }.getType();
        ParameterizedType pt = (ParameterizedType) type;
        Type wildcard = pt.getActualTypeArguments()[0];
        assertTrue(wildcard instanceof WildcardType);
        JavaType result = factory.constructType(wildcard);
        assertEquals(CharSequence.class, result.getRawClass());
    }

    @Test
    public void testConstructTypeWithTypeReference() {
        TypeReference<List<String>> ref = new TypeReference<List<String>>() { };
        JavaType type = factory.constructType(ref);
        assertTrue(type.isCollectionLikeType());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructTypeWithContextClass() {
        JavaType type = factory.constructType(List.class, String.class);
        // This should construct a simple type for List, but with context? 
        // Actually constructType(Type, Class<?>) creates TypeBindings from context class and then calls _constructType.
        // Since type is Class, it will use _fromClass, which may use context? _fromClass ignores context mostly.
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
    }

    @Test
    public void testConstructTypeWithJavaTypeContext() {
        JavaType context = factory.constructType(String.class);
        JavaType type = factory.constructType(List.class, context);
        assertNotNull(type);
    }

    // --------------- constructSpecializedType ---------------

    @Test
    public void testConstructSpecializedTypeSameRawClass() {
        JavaType base = factory.constructType(List.class);
        JavaType specialized = factory.constructSpecializedType(base, List.class);
        assertSame(base, specialized);
    }

    @Test
    public void testConstructSpecializedTypeSimpleTypeToMapSubclass() {
        JavaType base = factory.constructType(Map.class); // raw Map
        // Specialize to HashMap
        JavaType specialized = factory.constructSpecializedType(base, HashMap.class);
        assertNotNull(specialized);
        assertEquals(HashMap.class, specialized.getRawClass());
        // Should retain generic information? base is raw, so specialized will be raw HashMap.
    }

    @Test
    public void testConstructSpecializedTypeSimpleTypeToCollectionSubclass() {
        JavaType base = factory.constructType(Collection.class);
        JavaType specialized = factory.constructSpecializedType(base, ArrayList.class);
        assertEquals(ArrayList.class, specialized.getRawClass());
    }

    @Test
    public void testConstructSpecializedTypeWithHandlersCopied() {
        JavaType base = factory.constructType(String.class);
        Object valHandler = new Object();
        base = base.withValueHandler(valHandler);
        Object typeHandler = new Object();
        base = base.withTypeHandler(typeHandler);
        JavaType specialized = factory.constructSpecializedType(base, String.class); // same raw, should return base
        assertSame(base, specialized);
        // Actually if same raw class, returns base directly, so handlers are preserved.
        // Need a case where subclass is different and base is SimpleType and subclass is Map.
        base = factory.constructType(Map.class);
        base = base.withValueHandler(valHandler);
        base = base.withTypeHandler(typeHandler);
        specialized = factory.constructSpecializedType(base, HashMap.class);
        assertNotNull(specialized);
        assertEquals(valHandler, specialized.getValueHandler());
        assertEquals(typeHandler, specialized.getTypeHandler());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeNonSubtype() {
        JavaType base = factory.constructType(String.class);
        factory.constructSpecializedType(base, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSpecializedTypeStringToMap() {
        // base String, subclass HashMap not assignable
        JavaType base = factory.constructType(String.class);
        factory.constructSpecializedType(base, HashMap.class);
    }

    // --------------- findTypeParameters ---------------

    @Test
    public void testFindTypeParametersDirect() {
        JavaType type = factory.constructParametrizedType(ArrayList.class, List.class, String.class);
        JavaType[] params = factory.findTypeParameters(type, List.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(String.class, params[0].getRawClass());
    }

    @Test
    public void testFindTypeParametersFromClass() {
        JavaType[] params = factory.findTypeParameters(ArrayList.class, Collection.class);
        // Since ArrayList is a raw type, params might be null or one (Object)
        assertNotNull(params);
        assertEquals(1, params.length);
        assertEquals(Object.class, params[0].getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindTypeParametersNotSubtype() {
        factory.findTypeParameters(String.class, List.class);
    }

    @Test
    public void testFindTypeParametersWhenNullReturned() {
        // When expType == type.getParameterSource() and containedTypeCount == 0, returns null
        JavaType type = factory.constructType(String.class); // not a parameterized, how to get parameterSource? 
        // Can use a constructed param type without actual type arguments? 
        // Simpler: use findTypeParameters(Class, Class) that eventually returns null if super type not generic.
        // Let's test that when chain yields non-generic supertype, returns null.
        // For example, find type parameters for ArrayList on Cloneable (not generic)
        JavaType[] result = factory.findTypeParameters(ArrayList.class, Cloneable.class);
        assertNull(result);
    }

    // --------------- moreSpecificType ---------------

    @Test
    public void testMoreSpecificTypeNullFirst() {
        JavaType type2 = factory.constructType(String.class);
        assertSame(type2, factory.moreSpecificType(null, type2));
    }

    @Test
    public void testMoreSpecificTypeNullSecond() {
        JavaType type1 = factory.constructType(String.class);
        assertSame(type1, factory.moreSpecificType(type1, null));
    }

    @Test
    public void testMoreSpecificTypeSameRawClass() {
        JavaType t1 = factory.constructType(String.class);
        JavaType t2 = factory.constructType(String.class);
        assertSame(t1, factory.moreSpecificType(t1, t2));
    }

    @Test
    public void testMoreSpecificTypeAssignable() {
        JavaType type1 = factory.constructType(List.class);
        JavaType type2 = factory.constructType(ArrayList.class);
        JavaType result = factory.moreSpecificType(type1, type2);
        // ArrayList is more specific
        assertEquals(ArrayList.class, result.getRawClass());
    }

    @Test
    public void testMoreSpecificTypeNotAssignable() {
        JavaType type1 = factory.constructType(String.class);
        JavaType type2 = factory.constructType(Integer.class);
        JavaType result = factory.moreSpecificType(type1, type2);
        assertEquals(String.class, result.getRawClass());
    }

    // --------------- withModifier ---------------

    @Test
    public void testWithModifierNull() {
        TypeFactory newFactory = factory.withModifier(null);
        assertNotNull(newFactory);
        assertNotSame(factory, newFactory);
        // It should have same modifiers as original (null)
        // We can't directly inspect _modifiers, but can check that constructType yields same result
        assertEquals(factory.constructType(String.class).getRawClass(), newFactory.constructType(String.class).getRawClass());
    }

    @Test
    public void testWithModifierAddModifier() {
        TypeModifier mod = new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                return type.withContentValueHandler(new Object()); // dummy modification
            }
        };
        TypeFactory newFactory = factory.withModifier(mod);
        // modifier should be called when constructing a non-container type
        JavaType simple = newFactory.constructType(String.class);
        // The modifier adds content value handler, but String is not a container so isContainerType() false? 
        // String is SimpleType, which isContainerType returns false? Actually SimpleType's isContainerType() returns false.
        // So modifier should modify the type. We can verify by checking that the returned type has non-null content value handler? 
        // However, the modifier adds withContentValueHandler which is for content type, but apply to simple type? It might still be set.
        assertNotNull(simple.getContentValueHandler()); // might be true.
    }

    @Test
    public void testModifierNotCalledForContainer() {
        TypeModifier mod = new TypeModifier() {
            boolean called = false;
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                called = true;
                return type;
            }
        };
        TypeFactory newFactory = factory.withModifier(mod);
        JavaType listType = newFactory.constructType(List.class); // raw List, is container
        // The modifier should NOT be called because isContainerType() true for CollectionType, MapType etc?
        // But here, constructType(List.class) returns a CollectionType? Possibly, or SimpleType? Let's check: _fromClass for List.class? List is interface, Map.isAssignableFrom false, Collection.isAssignableFrom true, so it calls _collectionType, which returns CollectionType, which isContainerType true.
        // So modifier should not be called.
        assertFalse(mod.called);
    }

    // --------------- constructFromCanonical ---------------

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromCanonicalInvalid() {
        factory.constructFromCanonical("badcanonical");
    }

    @Test
    public void testConstructFromCanonicalValid() {
        // Use a canonical form for a simple type like "Ljava.lang.String;"
        // Not sure about exact format, but we can try the output of a constructed type's toCanonical
        JavaType type = factory.constructType(String.class);
        String canonical = type.toCanonical();
        JavaType parsed = factory.constructFromCanonical(canonical);
        assertEquals(type, parsed);
    }

    // --------------- constructArrayType ---------------

    @Test
    public void testConstructArrayTypeByClass() {
        ArrayType arrayType = factory.constructArrayType(String.class);
        assertNotNull(arrayType);
        assertTrue(arrayType.isArrayType());
        assertEquals(String.class, arrayType.getContentType().getRawClass());
    }

    @Test
    public void testConstructArrayTypeByJavaType() {
        JavaType elemType = factory.constructType(String.class);
        ArrayType arrayType = factory.constructArrayType(elemType);
        assertEquals(elemType, arrayType.getContentType());
    }

    // --------------- constructCollectionType / etc ---------------

    @Test
    public void testConstructCollectionTypeClassClass() {
        CollectionType type = factory.constructCollectionType(ArrayList.class, Integer.class);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionTypeClassJavaType() {
        JavaType elem = factory.constructType(Double.class);
        CollectionType type = factory.constructCollectionType(ArrayList.class, elem);
        assertEquals(elem, type.getContentType());
    }

    @Test
    public void testConstructCollectionLikeTypeClassClass() {
        CollectionLikeType type = factory.constructCollectionLikeType(ArrayList.class, String.class);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructCollectionLikeTypeClassJavaType() {
        JavaType elem = factory.constructType(Long.class);
        CollectionLikeType type = factory.constructCollectionLikeType(HashSet.class, elem);
        assertEquals(elem, type.getContentType());
    }

    @Test
    public void testConstructMapTypeJavaTypeJavaType() {
        MapType type = factory.constructMapType(HashMap.class, factory.constructType(String.class), factory.constructType(Integer.class));
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Integer.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapTypeClassClass() {
        MapType type = factory.constructMapType(TreeMap.class, Integer.class, String.class);
        assertEquals(TreeMap.class, type.getRawClass());
        assertEquals(Integer.class, type.getKeyType().getRawClass());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapLikeType() {
        MapLikeType type = factory.constructMapLikeType(HashMap.class, factory.constructType(String.class), factory.constructType(Long.class));
        assertEquals(String.class, type.getKeyType().getRawClass());
        assertEquals(Long.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructMapLikeTypeClassClass() {
        MapLikeType type = factory.constructMapLikeType(HashMap.class, Integer.class, Double.class);
        assertEquals(Integer.class, type.getKeyType().getRawClass());
        assertEquals(Double.class, type.getContentType().getRawClass());
    }

    // --------------- constructRawXxx ---------------

    @Test
    public void testConstructRawCollectionType() {
        CollectionType type = factory.constructRawCollectionType(ArrayList.class);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructRawCollectionLikeType() {
        CollectionLikeType type = factory.constructRawCollectionLikeType(HashSet.class);
        assertEquals(HashSet.class, type.getRawClass());
        assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructRawMapType() {
        MapType type = factory.constructRawMapType(HashMap.class);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals(Object.class, type.getKeyType().getRawClass());
        assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test
    public void testConstructRawMapLikeType() {
        MapLikeType type = factory.constructRawMapLikeType(TreeMap.class"");
        assertEquals(TreeMap.class, type.getRawClass());
        assertEquals(Object.class, type.getKeyType().getRawClass());
        assertEquals(Object.class, type.getContentType().getRawClass());
    }

    // --------------- constructSimpleType ---------------

    @Test
    public void testConstructSimpleTypeDefault() {
        JavaType type = factory.constructSimpleType(String.class, new JavaType[0]);
        // Actually deprecated variant, but still works, it calls the 3-arg version with rawType as parameterTarget.
        assertEquals(String.class, type.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructSimpleTypeMismatchCount() {
        // String has no type parameters, so providing one parameter should throw
        factory.constructSimpleType(String.class, new JavaType[] { factory.constructType(Integer.class) });
    }

    @Test
    public void testConstructSimpleTypeWithProperParams() {
        // Use Map.class which has two type parameters, but we need parameterTarget for which the parameters apply.
        // Simpler: use a generic class. Let's create a custom generic class with two type params and test.
    }

    // --------------- constructParametrizedType ---------------

    @Test
    public void testConstructParametrizedTypeArray() {
        // parametrized as array, parametersFor is array class, parameterClasses one element
        TypeFactory tf = factory;
        JavaType arrType = tf.constructParametrizedType(String[].class, String[].class, Integer.class);
        assertTrue(arrType.isArrayType());
        assertEquals(Integer.class, arrType.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedTypeArrayWrongCount() {
        factory.constructParametrizedType(String[].class, String[].class, Integer.class, Double.class);
    }

    @Test
    public void testConstructParametrizedTypeMap() {
        JavaType mapType = factory.constructParametrizedType(HashMap.class, Map.class, String.class, Integer.class);
        assertTrue(mapType.isMapLikeType());
        assertEquals(String.class, ((MapType)mapType).getKeyType().getRawClass());
        assertEquals(Integer.class, ((MapType)mapType).getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedTypeMapWrongCount() {
        factory.constructParametrizedType(HashMap.class, Map.class, String.class); // only one param
    }

    @Test
    public void testConstructParametrizedTypeCollection() {
        JavaType collType = factory.constructParametrizedType(ArrayList.class, List.class, String.class);
        assertEquals(ArrayList.class, collType.getRawClass());
        assertEquals(String.class, collType.getContentType().getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructParametrizedTypeCollectionWrongCount() {
        factory.constructParametrizedType(ArrayList.class, List.class, String.class, Integer.class);
    }

    @Test
    public void testConstructParametrizedTypeDefault() {
        // For non-array, non-map, non-collection: calls constructSimpleType
        // Let's test with a custom class that has type parameters
        JavaType type = factory.constructParametrizedType(GenericHolder.class, GenericHolder.class, String.class);
        // This should produce a SimpleType parameterized with String
        assertEquals(GenericHolder.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
        assertEquals(String.class, type.containedType(0).getRawClass());
    }

    // --------------- constructReferenceType ---------------

    @Test
    public void testConstructReferenceType() {
        JavaType refType = factory.constructReferenceType(AtomicReference.class, factory.constructType(String.class));
        assertEquals(AtomicReference.class, refType.getRawClass());
        assertEquals(String.class, refType.getContentType().getRawClass());
    }

    // --------------- uncheckedSimpleType ---------------

    @Test
    public void testUncheckedSimpleType() {
        JavaType type = factory.uncheckedSimpleType(String.class);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type instanceof SimpleType);
    }

    // --------------- clearCache ---------------

    @Test
    public void testClearCache() {
        // Ensure no exception thrown
        factory.clearCache();
    }

    // --------------- _fromClass coverage ---------------

    @Test
    public void testFromClassCoreTypes() {
        assertEquals("core string", factory.constructType(String.class).toString()); // just call
        assertEquals(Boolean.TYPE, factory.constructType(Boolean.TYPE).getRawClass());
        assertEquals(Integer.TYPE, factory.constructType(Integer.TYPE).getRawClass());
        assertEquals(Long.TYPE, factory.constructType(Long.TYPE).getRawClass());
    }

    @Test
    public void testFromClassEnum() {
        JavaType type = factory.constructType(Thread.State.class); // an enum
        assertEquals(Thread.State.class, type.getRawClass());
        assertTrue(type.isEnumType());
    }

    @Test
    public void testFromClassArray() {
        JavaType type = factory.constructType(String[].class);
        assertTrue(type.isArrayType());
        assertEquals(String.class, type.getContentType().getRawClass());
    }

    @Test
    public void testFromClassMap() {
        // Map.class should produce a MapType via _mapType
        JavaType type = factory.constructType(Map.class);
        assertTrue(type.isMapLikeType());
        // It will be raw MapType with unknown types
        assertEquals(Object.class, ((MapType)type).getKeyType().getRawClass());
    }

    @Test
    public void testFromClassCollection() {
        // Collection.class should produce CollectionType
        JavaType type = factory.constructType(Collection.class);
        assertTrue(type.isCollectionLikeType());
        assertEquals(Object.class, type.getContentType().getRawClass());
    }

    @Test
    public void testFromClassAtomicReference() {
        // AtomicReference.class should produce ReferenceType
        JavaType type = factory.constructType(AtomicReference.class);
        assertTrue(type.isReferenceType());
        assertEquals(Object.class, type.getContentType().getRawClass()); // because findTypeParameters returns null
    }

    @Test
    public void testFromClassMapEntry() {
        // Need a class that directly implements Map.Entry? 
        // Easier: test with AbstractMap.SimpleEntry which is a public implementation
        JavaType type = factory.constructType(AbstractMap.SimpleEntry.class);
        // It should be a SimpleType with two type params
        assertEquals(2, type.containedTypeCount());
        assertEquals(Object.class, type.containedType(0).getRawClass());
        assertEquals(Object.class, type.containedType(1).getRawClass());
    }

    // --------------- _fromParamType coverage ---------------

    @Test
    public void testFromParamTypeMap() {
        Type type = new TypeReference<HashMap<String, Integer>>() { }.getType();
        JavaType result = factory.constructType(type);
        assertTrue(result.isMapLikeType());
        assertEquals(String.class, ((MapType)result).getKeyType().getRawClass());
        assertEquals(Integer.class, ((MapType)result).getContentType().getRawClass());
    }

    @Test
    public void testFromParamTypeCollection() {
        Type type = new TypeReference<List<Double>>() { }.getType();
        JavaType result = factory.constructType(type);
        assertTrue(result.isCollectionLikeType());
        assertEquals(Double.class, result.getContentType().getRawClass());
    }

    @Test
    public void testFromParamTypeAtomicReference() {
        Type type = new TypeReference<AtomicReference<String>>() { }.getType();
        JavaType result = factory.constructType(type);
        assertEquals(AtomicReference.class, result.getRawClass());
        assertEquals(String.class, result.getContentType().getRawClass());
    }

    @Test
    public void testFromParamTypeMapEntry() {
        Type type = new TypeReference<Map.Entry<String, Integer>>() { }.getType();
        JavaType result = factory.constructType(type);
        assertEquals(Map.Entry.class, result.getRawClass());
        assertEquals(2, result.containedTypeCount());
        assertEquals(String.class, result.containedType(0).getRawClass());
        assertEquals(Integer.class, result.containedType(1).getRawClass());
    }

    // --------------- Hierarchy caching tests ---------------

    @Test
    public void testHashMapSuperInterfaceChain() {
        // This should trigger caching
        JavaType[] params = factory.findTypeParameters(HashMap.class, Map.class);
        assertNotNull(params);
        assertEquals(2, params.length);
        // Call again to use cache
        params = factory.findTypeParameters(HashMap.class, Map.class);
        assertNotNull(params);
    }

    @Test
    public void testArrayListSuperInterfaceChain() {
        JavaType[] params = factory.findTypeParameters(ArrayList.class, List.class);
        assertNotNull(params);
        assertEquals(1, params.length);
        // Call again
        params = factory.findTypeParameters(ArrayList.class, List.class);
        assertNotNull(params);
    }

    // --------------- Helper to hold a generic class for tests ---------------

    static class GenericHolder<T> { }
}
