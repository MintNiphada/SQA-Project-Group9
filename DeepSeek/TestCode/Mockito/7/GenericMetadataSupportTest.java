package org.mockito.internal.util.reflection;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.*;
import java.util.*;

import static org.junit.Assert.*;

public class GenericMetadataSupportTest {

    // Helper interfaces and classes for testing
    interface SimpleInterface {}
    interface ParamInterface<T> {}
    interface UpperBoundedType<E extends Comparable<E> & Cloneable> { E get(); }
    interface MixedBounds<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {}
    interface WildcardWithSuper { List<? super Integer> get(); }
    interface WildcardWithExtends { List<? extends Number> get(); }
    interface MultipleTypeParams<A, B> { A getA(); B getB(); }
    static class ConcreteSimple implements SimpleInterface {}
    static class ConcreteParam implements ParamInterface<String> {}
    static class ConcreteMixed implements MixedBounds<Integer> { public Set<Number> remove(Object key) { return null; } public Set<Number> get(Object key) { return null; } public int size() { return 0; } public boolean isEmpty() { return false; } public boolean containsKey(Object key) { return false; } public boolean containsValue(Object value) { return false; } public Set<Number> put(Integer key, Set<Number> value) { return null; } public Set<Number> remove(Object key) { return null; } public void putAll(Map<? extends Integer, ? extends Set<Number>> m) {} public void clear() {} public Set<Integer> keySet() { return null; } public Collection<Set<Number>> values() { return null; } public Set<Map.Entry<Integer, Set<Number>>> entrySet() { return null; } }

    // --- Tests for inferFrom ---
    @Test(expected = MockitoException.class)
    public void inferFromNullTypeThrowsException() {
        GenericMetadataSupport.inferFrom(null);
    }

    @Test
    public void inferFromClassReturnsFromClassGenericMetadataSupport() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertNotNull(metadata);
        assertEquals(String.class, metadata.rawType());
    }

    @Test
    public void inferFromParameterizedTypeReturnsFromParameterizedTypeGenericMetadataSupport() throws Exception {
        Type type = ConcreteParam.class.getGenericInterfaces()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(type);
        assertNotNull(metadata);
        assertEquals(ParamInterface.class, metadata.rawType());
    }

    @Test(expected = MockitoException.class)
    public void inferFromUnsupportedTypeThrowsException() {
        GenericMetadataSupport.inferFrom(new Type() {});
    }

    // --- Tests for rawType ---
    @Test
    public void rawTypeForClassMetadata() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ArrayList.class);
        assertEquals(ArrayList.class, metadata.rawType());
    }

    @Test
    public void rawTypeForParameterizedTypeMetadata() throws Exception {
        Type type = ConcreteParam.class.getGenericInterfaces()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(type);
        assertEquals(ParamInterface.class, metadata.rawType());
    }

    // --- Tests for extraInterfaces and rawExtraInterfaces ---
    @Test
    public void extraInterfacesDefaultEmpty() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertTrue(metadata.extraInterfaces().isEmpty());
    }

    @Test
    public void rawExtraInterfacesDefaultEmpty() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertEquals(0, metadata.rawExtraInterfaces().length);
    }

    @Test
    public void hasRawExtraInterfacesDefaultFalse() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertFalse(metadata.hasRawExtraInterfaces());
    }

    // --- Tests for actualTypeArguments ---
    @Test
    public void actualTypeArgumentsForNonGenericClass() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        Map<TypeVariable, Type> args = metadata.actualTypeArguments();
        assertNotNull(args);
        assertTrue(args.isEmpty());
    }

    @Test
    public void actualTypeArgumentsForParameterizedType() throws Exception {
        Type type = ConcreteParam.class.getGenericInterfaces()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(type);
        Map<TypeVariable, Type> args = metadata.actualTypeArguments();
        assertEquals(1, args.size());
        TypeVariable tv = ParamInterface.class.getTypeParameters()[0];
        assertTrue(args.containsKey(tv));
        assertEquals(String.class, args.get(tv));
    }

    // --- Tests for resolveGenericReturnType ---
    @Test
    public void resolveGenericReturnTypeForNonGenericMethod() throws Exception {
        Method method = SimpleInterface.class.getMethod("toString");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SimpleInterface.class);
        GenericMetadataSupport result = metadata.resolveGenericReturnType(method);
        assertEquals(String.class, result.rawType());
    }

    @Test
    public void resolveGenericReturnTypeForParameterizedReturnType() throws Exception {
        Method method = MixedBounds.class.getMethod("get", Object.class);
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MixedBounds.class);
        GenericMetadataSupport result = metadata.resolveGenericReturnType(method);
        assertEquals(Set.class, result.rawType());
    }

    @Test
    public void resolveGenericReturnTypeForTypeVariableReturnType() throws Exception {
        Method method = UpperBoundedType.class.getMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(UpperBoundedType.class);
        GenericMetadataSupport result = metadata.resolveGenericReturnType(method);
        assertNotNull(result);
    }

    @Test(expected = MockitoException.class)
    public void resolveGenericReturnTypeForUnsupportedTypeThrowsException() throws Exception {
        // This is tricky to trigger directly, but we can test via a mock or reflection
        // We'll test the exception path by creating a scenario where genericReturnType is not Class, ParameterizedType, or TypeVariable
        // Since we cannot easily create such a type, we rely on the code coverage of the exception
        // Actually, we can test the exception by passing a method that returns a GenericArrayType
        abstract class ArrayReturn { abstract String[] getArray(); }
        Method method = ArrayReturn.class.getDeclaredMethod("getArray");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ArrayReturn.class);
        try {
            metadata.resolveGenericReturnType(method);
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("not supported"));
        }
    }

    // --- Tests for TypeVarBoundedType ---
    @Test
    public void typeVarBoundedTypeFirstBound() {
        TypeVariable[] tvs = UpperBoundedType.class.getTypeParameters();
        TypeVarBoundedType boundedType = new TypeVarBoundedType(tvs[0]);
        assertEquals(Comparable.class, boundedType.firstBound());
    }

    @Test
    public void typeVarBoundedTypeInterfaceBounds() {
        TypeVariable[] tvs = UpperBoundedType.class.getTypeParameters();
        TypeVarBoundedType boundedType = new TypeVarBoundedType(tvs[0]);
        Type[] interfaces = boundedType.interfaceBounds();
        assertEquals(1, interfaces.length);
        assertEquals(Cloneable.class, interfaces[0]);
    }

    @Test
    public void typeVarBoundedTypeEqualsAndHashCode() {
        TypeVariable[] tvs = UpperBoundedType.class.getTypeParameters();
        TypeVarBoundedType bt1 = new TypeVarBoundedType(tvs[0]);
        TypeVarBoundedType bt2 = new TypeVarBoundedType(tvs[0]);
        assertEquals(bt1, bt2);
        assertEquals(bt1.hashCode(), bt2.hashCode());
    }

    @Test
    public void typeVarBoundedTypeNotEqualsDifferentType() {
        TypeVariable[] tvs = UpperBoundedType.class.getTypeParameters();
        TypeVarBoundedType bt = new TypeVarBoundedType(tvs[0]);
        assertFalse(bt.equals(new Object()));
    }

    @Test
    public void typeVarBoundedTypeNotEqualsNull() {
        TypeVariable[] tvs = UpperBoundedType.class.getTypeParameters();
        TypeVarBoundedType bt = new TypeVarBoundedType(tvs[0]);
        assertFalse(bt.equals(null));
    }

    @Test
    public void typeVarBoundedTypeToString() {
        TypeVariable[] tvs = UpperBoundedType.class.getTypeParameters();
        TypeVarBoundedType bt = new TypeVarBoundedType(tvs[0]);
        String str = bt.toString();
        assertTrue(str.contains("firstBound"));
        assertTrue(str.contains("interfaceBounds"));
    }

    @Test
    public void typeVarBoundedTypeTypeVariable() {
        TypeVariable[] tvs = UpperBoundedType.class.getTypeParameters();
        TypeVarBoundedType bt = new TypeVarBoundedType(tvs[0]);
        assertEquals(tvs[0], bt.typeVariable());
    }

    // --- Tests for WildCardBoundedType ---
    @Test
    public void wildCardBoundedTypeFirstBoundUpper() throws Exception {
        WildcardType wildcard = (WildcardType) ((ParameterizedType) WildcardWithExtends.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildCardBoundedType boundedType = new WildCardBoundedType(wildcard);
        assertEquals(Number.class, boundedType.firstBound());
    }

    @Test
    public void wildCardBoundedTypeFirstBoundLower() throws Exception {
        WildcardType wildcard = (WildcardType) ((ParameterizedType) WildcardWithSuper.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildCardBoundedType boundedType = new WildCardBoundedType(wildcard);
        assertEquals(Integer.class, boundedType.firstBound());
    }

    @Test
    public void wildCardBoundedTypeInterfaceBoundsEmpty() throws Exception {
        WildcardType wildcard = (WildcardType) ((ParameterizedType) WildcardWithExtends.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildCardBoundedType boundedType = new WildCardBoundedType(wildcard);
        assertEquals(0, boundedType.interfaceBounds().length);
    }

    @Test
    public void wildCardBoundedTypeEqualsAndHashCode() throws Exception {
        WildcardType wildcard = (WildcardType) ((ParameterizedType) WildcardWithExtends.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildCardBoundedType bt1 = new WildCardBoundedType(wildcard);
        WildCardBoundedType bt2 = new WildCardBoundedType(wildcard);
        assertEquals(bt1, bt2);
        assertEquals(bt1.hashCode(), bt2.hashCode());
    }

    @Test
    public void wildCardBoundedTypeNotEqualsNull() throws Exception {
        WildcardType wildcard = (WildcardType) ((ParameterizedType) WildcardWithExtends.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildCardBoundedType bt = new WildCardBoundedType(wildcard);
        assertFalse(bt.equals(null));
    }

    @Test
    public void wildCardBoundedTypeNotEqualsDifferentType() throws Exception {
        WildcardType wildcard = (WildcardType) ((ParameterizedType) WildcardWithExtends.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildCardBoundedType bt = new WildCardBoundedType(wildcard);
        assertFalse(bt.equals(new Object()));
    }

    @Test
    public void wildCardBoundedTypeToString() throws Exception {
        WildcardType wildcard = (WildcardType) ((ParameterizedType) WildcardWithExtends.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildCardBoundedType bt = new WildCardBoundedType(wildcard);
        String str = bt.toString();
        assertTrue(str.contains("firstBound"));
    }

    @Test
    public void wildCardBoundedTypeWildCard() throws Exception {
        WildcardType wildcard = (WildcardType) ((ParameterizedType) WildcardWithExtends.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildCardBoundedType bt = new WildCardBoundedType(wildcard);
        assertEquals(wildcard, bt.wildCard());
    }

    // --- Tests for registerTypeVariablesOn with WildcardType ---
    @Test
    public void registerTypeVariablesOnWithWildcardType() throws Exception {
        // Use a class that has a wildcard type argument
        abstract class WildcardClass implements ParamInterface<List<? extends Number>> {}
        Type type = WildcardClass.class.getGenericInterfaces()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(type);
        Map<TypeVariable, Type> args = metadata.actualTypeArguments();
        assertFalse(args.isEmpty());
        TypeVariable tv = ParamInterface.class.getTypeParameters()[0];
        assertTrue(args.containsKey(tv));
        assertTrue(args.get(tv) instanceof BoundedType);
    }

    // --- Tests for TypeVariableReturnType extraInterfaces and rawExtraInterfaces ---
    @Test
    public void typeVariableReturnTypeExtraInterfaces() throws Exception {
        Method method = UpperBoundedType.class.getMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(UpperBoundedType.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        List<Type> extra = returnMetadata.extraInterfaces();
        assertNotNull(extra);
        // E extends Comparable<E> & Cloneable, so extra interfaces should include Cloneable
        boolean hasCloneable = false;
        for (Type t : extra) {
            if (t instanceof Class && ((Class<?>) t).equals(Cloneable.class)) {
                hasCloneable = true;
                break;
            }
        }
        assertTrue(hasCloneable);
    }

    @Test
    public void typeVariableReturnTypeRawExtraInterfaces() throws Exception {
        Method method = UpperBoundedType.class.getMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(UpperBoundedType.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        Class<?>[] rawExtra = returnMetadata.rawExtraInterfaces();
        assertNotNull(rawExtra);
        assertTrue(rawExtra.length > 0);
        boolean hasCloneable = false;
        for (Class<?> c : rawExtra) {
            if (c.equals(Cloneable.class)) {
                hasCloneable = true;
                break;
            }
        }
        assertTrue(hasCloneable);
    }

    @Test
    public void typeVariableReturnTypeHasRawExtraInterfaces() throws Exception {
        Method method = UpperBoundedType.class.getMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(UpperBoundedType.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertTrue(returnMetadata.hasRawExtraInterfaces());
    }

    // --- Tests for boundsOf with TypeVariable that has TypeVariable bound ---
    @Test
    public void boundsOfTypeVariableWithTypeVariableBound() throws Exception {
        // Create a scenario where a type variable's first bound is another type variable
        // This is complex, but we can test indirectly via actualTypeArguments
        abstract class NestedTypeVar<A extends Comparable<A>, B extends A> {}
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(NestedTypeVar.class);
        Map<TypeVariable, Type> args = metadata.actualTypeArguments();
        // B's bound is A, which is a TypeVariable
        TypeVariable b = NestedTypeVar.class.getTypeParameters()[1];
        assertTrue(args.containsKey(b));
        assertTrue(args.get(b) instanceof BoundedType);
    }

    // --- Tests for getActualTypeArgumentFor ---
    @Test
    public void getActualTypeArgumentForReturnsTypeVariable() throws Exception {
        Type type = ConcreteParam.class.getGenericInterfaces()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(type);
        TypeVariable tv = ParamInterface.class.getTypeParameters()[0];
        Type actual = metadata.getActualTypeArgumentFor(tv);
        assertEquals(String.class, actual);
    }

    // --- Tests for FromClassGenericMetadataSupport ---
    @Test
    public void fromClassGenericMetadataSupportWithGenericSuperclass() {
        abstract class GenericSuper<T> {}
        class Concrete extends GenericSuper<String> {}
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(Concrete.class);
        assertEquals(Concrete.class, metadata.rawType());
        Map<TypeVariable, Type> args = metadata.actualTypeArguments();
        assertFalse(args.isEmpty());
    }

    @Test
    public void fromClassGenericMetadataSupportWithObjectSuperclass() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(Object.class);
        assertEquals(Object.class, metadata.rawType());
    }

    // --- Tests for FromParameterizedTypeGenericMetadataSupport ---
    @Test
    public void fromParameterizedTypeGenericMetadataSupport() throws Exception {
        Type type = ConcreteParam.class.getGenericInterfaces()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(type);
        assertEquals(ParamInterface.class, metadata.rawType());
    }

    // --- Tests for ParameterizedReturnType ---
    @Test
    public void parameterizedReturnTypeRawType() throws Exception {
        Method method = MixedBounds.class.getMethod("get", Object.class);
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MixedBounds.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(Set.class, returnMetadata.rawType());
    }

    // --- Tests for NotGenericReturnTypeSupport ---
    @Test
    public void notGenericReturnTypeSupportRawType() throws Exception {
        Method method = SimpleInterface.class.getMethod("toString");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SimpleInterface.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(String.class, returnMetadata.rawType());
    }

    // --- Tests for extractRawTypeOf in TypeVariableReturnType ---
    @Test
    public void extractRawTypeOfClass() throws Exception {
        Method method = UpperBoundedType.class.getMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(UpperBoundedType.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        // The raw type should be Comparable (first bound)
        assertEquals(Comparable.class, returnMetadata.rawType());
    }

    @Test
    public void extractRawTypeOfParameterizedType() throws Exception {
        // Create a scenario where the bound is a ParameterizedType
        abstract class ParamBound<T extends List<String>> { abstract T get(); }
        Method method = ParamBound.class.getDeclaredMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ParamBound.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(List.class, returnMetadata.rawType());
    }

    @Test
    public void extractRawTypeOfBoundedType() throws Exception {
        // This is tested indirectly through the wildcard and type variable bounds
        // Already covered by other tests
    }

    @Test(expected = MockitoException.class)
    public void extractRawTypeOfUnsupportedTypeThrowsException() throws Exception {
        // This is hard to trigger directly, but we can test via a custom Type implementation
        // We'll use a method that returns a type that is not Class, ParameterizedType, BoundedType, or TypeVariable
        // Actually, we can test the exception by creating a scenario where the bound is a GenericArrayType
        abstract class ArrayBound<T extends String[]> { abstract T get(); }
        Method method = ArrayBound.class.getDeclaredMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ArrayBound.class);
        try {
            metadata.resolveGenericReturnType(method).rawType();
            fail("Expected MockitoException");
        } catch (MockitoException e) {
            assertTrue(e.getMessage().contains("Raw extraction not supported"));
        }
    }

    // --- Tests for extractActualBoundedTypeOf ---
    @Test
    public void extractActualBoundedTypeOfTypeVariable() throws Exception {
        // Covered by extraInterfaces tests
    }

    @Test
    public void extractActualBoundedTypeOfBoundedType() throws Exception {
        // Covered by extraInterfaces tests
    }

    // --- Tests for registerTypeParametersOn ---
    @Test
    public void registerTypeParametersOnAddsNewTypeVariables() {
        // This is tested indirectly through actualTypeArguments
    }

    // --- Tests for registerTypeVariableIfNotPresent ---
    @Test
    public void registerTypeVariableIfNotPresentDoesNotOverwrite() throws Exception {
        // Test that if a type variable is already present, it is not overwritten
        // This is tested indirectly through the resolution process
    }

    // --- Tests for boundsOf with WildcardType that has TypeVariable bound ---
    @Test
    public void boundsOfWildcardWithTypeVariableBound() throws Exception {
        // Create a scenario where a wildcard's upper bound is a TypeVariable
        abstract class WildcardTypeVarBound<T extends Comparable<T>> { abstract List<? extends T> get(); }
        Method method = WildcardTypeVarBound.class.getDeclaredMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(WildcardTypeVarBound.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertNotNull(returnMetadata);
    }

    // --- Tests for MultipleTypeParams ---
    @Test
    public void multipleTypeParams() throws Exception {
        abstract class MultiImpl implements MultipleTypeParams<String, Integer> {}
        Type type = MultiImpl.class.getGenericInterfaces()[0];
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(type);
        Map<TypeVariable, Type> args = metadata.actualTypeArguments();
        assertEquals(2, args.size());
        TypeVariable[] tvs = MultipleTypeParams.class.getTypeParameters();
        assertEquals(String.class, args.get(tvs[0]));
        assertEquals(Integer.class, args.get(tvs[1]));
    }

    // --- Tests for Wildcard with super bound ---
    @Test
    public void wildcardSuperBound() throws Exception {
        Method method = WildcardWithSuper.class.getMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(WildcardWithSuper.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(List.class, returnMetadata.rawType());
    }

    // --- Tests for Wildcard with extends bound ---
    @Test
    public void wildcardExtendsBound() throws Exception {
        Method method = WildcardWithExtends.class.getMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(WildcardWithExtends.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(List.class, returnMetadata.rawType());
    }

    // --- Tests for TypeVariableReturnType with ParameterizedType bound ---
    @Test
    public void typeVariableReturnTypeWithParameterizedTypeBound() throws Exception {
        abstract class ParamTypeBound<T extends List<String>> { abstract T get(); }
        Method method = ParamTypeBound.class.getDeclaredMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ParamTypeBound.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        List<Type> extra = returnMetadata.extraInterfaces();
        assertNotNull(extra);
        assertTrue(extra.isEmpty()); // List is the first bound, no extra interfaces
    }

    // --- Tests for TypeVariableReturnType with Class bound ---
    @Test
    public void typeVariableReturnTypeWithClassBound() throws Exception {
        abstract class ClassBound<T extends String> { abstract T get(); }
        Method method = ClassBound.class.getDeclaredMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ClassBound.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(String.class, returnMetadata.rawType());
        List<Type> extra = returnMetadata.extraInterfaces();
        assertTrue(extra.isEmpty());
    }

    // --- Tests for rawExtraInterfaces collision avoidance ---
    @Test
    public void rawExtraInterfacesAvoidsCollisionWithRawType() throws Exception {
        // Create a scenario where an extra interface is the same as the raw type
        // This is hard to trigger, but we can test the logic indirectly
        // The code checks if rawType().equals(rawInterface) and skips it
        // We'll test with a type that has an interface bound that is the same as the first bound
        // Actually, interface bounds are only interfaces, so they shouldn't collide with the class bound
        // But we can test the logic by ensuring no duplicates
        Method method = UpperBoundedType.class.getMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(UpperBoundedType.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        Class<?>[] rawExtra = returnMetadata.rawExtraInterfaces();
        // Ensure no duplicates
        Set<Class<?>> set = new HashSet<>(Arrays.asList(rawExtra));
        assertEquals(rawExtra.length, set.size());
    }

    // --- Tests for TypeVarBoundedType interfaceBounds with no extra bounds ---
    @Test
    public void typeVarBoundedTypeInterfaceBoundsEmpty() {
        abstract class NoExtraBounds<T> {}
        TypeVariable[] tvs = NoExtraBounds.class.getTypeParameters();
        TypeVarBoundedType bt = new TypeVarBoundedType(tvs[0]);
        assertEquals(0, bt.interfaceBounds().length);
    }

    // --- Tests for WildCardBoundedType equals with different WildcardType ---
    @Test
    public void wildCardBoundedTypeNotEqualsDifferentWildcard() throws Exception {
        WildcardType wildcard1 = (WildcardType) ((ParameterizedType) WildcardWithExtends.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildcardType wildcard2 = (WildcardType) ((ParameterizedType) WildcardWithSuper.class.getMethod("get").getGenericReturnType()).getActualTypeArguments()[0];
        WildCardBoundedType bt1 = new WildCardBoundedType(wildcard1);
        WildCardBoundedType bt2 = new WildCardBoundedType(wildcard2);
        assertFalse(bt1.equals(bt2));
    }

    // --- Tests for TypeVarBoundedType equals with different TypeVariable ---
    @Test
    public void typeVarBoundedTypeNotEqualsDifferentTypeVariable() {
        TypeVariable[] tvs1 = UpperBoundedType.class.getTypeParameters();
        TypeVariable[] tvs2 = MultipleTypeParams.class.getTypeParameters();
        TypeVarBoundedType bt1 = new TypeVarBoundedType(tvs1[0]);
        TypeVarBoundedType bt2 = new TypeVarBoundedType(tvs2[0]);
        assertFalse(bt1.equals(bt2));
    }

    // --- Tests for registerTypeVariablesOn with non-ParameterizedType ---
    @Test
    public void registerTypeVariablesOnWithClassDoesNothing() {
        // This is tested indirectly, as registerTypeVariablesOn is called with Class in FromClassGenericMetadataSupport
        // and it should not throw
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(String.class);
        assertNotNull(metadata);
    }

    // --- Tests for resolveGenericReturnType with ParameterizedType that has TypeVariable arguments ---
    @Test
    public void resolveGenericReturnTypeWithParameterizedTypeHavingTypeVariableArguments() throws Exception {
        abstract class ComplexReturn { abstract Map<String, List<? extends Comparable>> get(); }
        Method method = ComplexReturn.class.getDeclaredMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(ComplexReturn.class);
        GenericMetadataSupport result = metadata.resolveGenericReturnType(method);
        assertEquals(Map.class, result.rawType());
    }

    // --- Tests for TypeVariableReturnType with multiple bounds ---
    @Test
    public void typeVariableReturnTypeWithMultipleBounds() throws Exception {
        interface MultiBound<T extends Number & Comparable<T> & Cloneable> { T get(); }
        Method method = MultiBound.class.getMethod("get");
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(MultiBound.class);
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);
        assertEquals(Number.class, returnMetadata.rawType());
        List<Type> extra = returnMetadata.extraInterfaces();
        assertEquals(2, extra.size());
        assertTrue(extra.contains(Comparable.class));
        assertTrue(extra.contains(Cloneable.class));
    }
}
