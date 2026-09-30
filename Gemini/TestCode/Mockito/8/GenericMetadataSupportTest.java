package org.mockito.internal.util.reflection;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.*;

import static org.junit.Assert.*;

public class GenericMetadataSupportTest {

    interface SimpleInterface {
        String nonGenericMethod();
        List<String> parameterizedMethod();
        <T> T typeVariableMethod();
        <T extends Number & Comparable<T>> T boundedTypeVariableMethod();
    }

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
        Set<Number> remove(Object key);
        List<? super Integer> returning_wildcard_with_class_lower_bound();
        List<? super K> returning_wildcard_with_typeVar_lower_bound();
        List<? extends K> returning_wildcard_with_typeVar_upper_bound();
        List<? extends CharSequence> returning_wildcard_with_class_upper_bound();
        K returningK();
        <O extends K> List<O> paramType_with_type_params();
        <S, T extends S> T two_type_params();
        <O extends K> O typeVar_with_type_params();
        Number returningNonGeneric();
    }

    static class GenericSuperClass<A, B> {
        public A getA() { return null; }
        public B getB() { return null; }
    }

    static class GenericSubClass<C> extends GenericSuperClass<C, String> implements List<C> {
        // Dummy List implementation methods to satisfy compiler if needed (abstract if not implementing)
        public int size() { return 0; }
        public boolean isEmpty() { return false; }
        public boolean contains(Object o) { return false; }
        public Iterator<C> iterator() { return null; }
        public Object[] toArray() { return new Object[0]; }
        public <T> T[] toArray(T[] a) { return null; }
        public boolean add(C c) { return false; }
        public boolean remove(Object o) { return false; }
        public boolean containsAll(Collection<?> c) { return false; }
        public boolean addAll(Collection<? extends C> c) { return false; }
        public boolean addAll(int index, Collection<? extends C> c) { return false; }
        public boolean removeAll(Collection<?> c) { return false; }
        public boolean retainAll(Collection<?> c) { return false; }
        public void clear() {}
        public C get(int index) { return null; }
        public C set(int index, C element) { return null; }
        public void add(int index, C element) {}
        public C remove(int index) { return null; }
        public int indexOf(Object o) { return 0; }
        public int lastIndexOf(Object o) { return 0; }
        public ListIterator<C> listIterator() { return null; }
        public ListIterator<C> listIterator(int index) { return null; }
        public List<C> subList(int fromIndex, int toIndex) { return null; }
    }

    interface DeepNestedInterface<X extends Serializable, Y extends List<X>> {
        Y getY();
    }

    interface RecursiveTypeVar<T extends RecursiveTypeVar<T>> {
        T self();
    }

    interface MultipleInterfaceBounds<T extends Number & Comparable<T> & Serializable> {
        T getT();
    }

    @Test
    public void testInferFromClass() {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SimpleInterface.class);
        assertNotNull(metadata);
        assertEquals(SimpleInterface.class, metadata.rawType());
        assertEquals(0, metadata.actualTypeArguments().size());
        assertEquals(0, metadata.extraInterfaces().size());
        assertEquals(0, metadata.rawExtraInterfaces().length);
        assertFalse(metadata.hasRawExtraInterfaces());
    }

    @Test(expected = MockitoException.class)
    public void testInferFromNullThrowsException() {
        GenericMetadataSupport.inferFrom(null);
    }

    @Test(expected = MockitoException.class)
    public void testInferFromUnsupportedTypeThrowsException() {
        Type unsupportedType = new Type() {
            @Override
            public String getTypeName() {
                return "CustomType";
            }
        };
        GenericMetadataSupport.inferFrom(unsupportedType);
    }

    @Test
    public void testResolveNonGenericReturnType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SimpleInterface.class);
        Method method = SimpleInterface.class.getMethod("nonGenericMethod");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(String.class, returnMetadata.rawType());
        assertEquals(0, returnMetadata.actualTypeArguments().size());
    }

    @Test
    public void testResolveParameterizedReturnType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SimpleInterface.class);
        Method method = SimpleInterface.class.getMethod("parameterizedMethod");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(List.class, returnMetadata.rawType());
    }

    @Test
    public void testResolveTypeVariableReturnType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SimpleInterface.class);
        Method method = SimpleInterface.class.getMethod("typeVariableMethod");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Object.class, returnMetadata.rawType());
        assertFalse(returnMetadata.hasRawExtraInterfaces());
        assertEquals(0, returnMetadata.rawExtraInterfaces().length);
        assertEquals(0, returnMetadata.extraInterfaces().size());
    }

    @Test
    public void testResolveBoundedTypeVariableReturnType() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(SimpleInterface.class);
        Method method = SimpleInterface.class.getMethod("boundedTypeVariableMethod");
        GenericMetadataSupport returnMetadata = metadata.resolveGenericReturnType(method);

        assertEquals(Number.class, returnMetadata.rawType());
        assertTrue(returnMetadata.hasRawExtraInterfaces());
        assertEquals(1, returnMetadata.rawExtraInterfaces().length);
        assertEquals(Comparable.class, returnMetadata.rawExtraInterfaces()[0]);
        assertEquals(1, returnMetadata.extraInterfaces().size());
    }

    @Test
    public void testInferFromParameterizedType() throws Exception {
        Method method = GenericsNest.class.getMethod("returning_wildcard_with_class_upper_bound");
        Type returnType = method.getGenericReturnType();
        assertTrue(returnType instanceof ParameterizedType);

        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(returnType);
        assertEquals(List.class, metadata.rawType());
        Map<TypeVariable, Type> typeArgs = metadata.actualTypeArguments();
        assertEquals(1, typeArgs.size());
    }

    @Test
    public void testClassHierarchyResolution() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericSubClass.class);
        assertEquals(GenericSubClass.class, metadata.rawType());

        Method getB = GenericSuperClass.class.getMethod("getB");
        GenericMetadataSupport getBMetadata = metadata.resolveGenericReturnType(getB);
        assertEquals(String.class, getBMetadata.rawType());
    }

    @Test
    public void testGenericsNestMethods() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);

        Method returningK = GenericsNest.class.getMethod("returningK");
        GenericMetadataSupport returnKMeta = metadata.resolveGenericReturnType(returningK);
        assertEquals(Comparable.class, returnKMeta.rawType());
        assertTrue(returnKMeta.hasRawExtraInterfaces());
        assertEquals(Cloneable.class, returnKMeta.rawExtraInterfaces()[0]);

        Method removeMethod = GenericsNest.class.getMethod("remove", Object.class);
        GenericMetadataSupport removeMeta = metadata.resolveGenericReturnType(removeMethod);
        assertEquals(Set.class, removeMeta.rawType());

        Method returningNonGeneric = GenericsNest.class.getMethod("returningNonGeneric");
        GenericMetadataSupport nonGenericMeta = metadata.resolveGenericReturnType(returningNonGeneric);
        assertEquals(Number.class, nonGenericMeta.rawType());

        Method typeVarWithParams = GenericsNest.class.getMethod("typeVar_with_type_params");
        GenericMetadataSupport typeVarWithParamsMeta = metadata.resolveGenericReturnType(typeVarWithParams);
        assertEquals(Comparable.class, typeVarWithParamsMeta.rawType());
        assertEquals(1, typeVarWithParamsMeta.rawExtraInterfaces().length);
        assertEquals(Cloneable.class, typeVarWithParamsMeta.rawExtraInterfaces()[0]);

        Method paramTypeWithParams = GenericsNest.class.getMethod("paramType_with_type_params");
        GenericMetadataSupport paramTypeWithParamsMeta = metadata.resolveGenericReturnType(paramTypeWithParams);
        assertEquals(List.class, paramTypeWithParamsMeta.rawType());

        Method twoTypeParams = GenericsNest.class.getMethod("two_type_params");
        GenericMetadataSupport twoTypeParamsMeta = metadata.resolveGenericReturnType(twoTypeParams);
        assertEquals(Object.class, twoTypeParamsMeta.rawType());
    }

    @Test
    public void testWildcardsInGenericsNest() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(GenericsNest.class);

        Method lowerBoundMethod = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        GenericMetadataSupport lowerBoundMeta = metadata.resolveGenericReturnType(lowerBoundMethod);
        assertEquals(List.class, lowerBoundMeta.rawType());

        Method lowerBoundTypeVarMethod = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_lower_bound");
        GenericMetadataSupport lowerBoundTypeVarMeta = metadata.resolveGenericReturnType(lowerBoundTypeVarMethod);
        assertEquals(List.class, lowerBoundTypeVarMeta.rawType());

        Method upperBoundTypeVarMethod = GenericsNest.class.getMethod("returning_wildcard_with_typeVar_upper_bound");
        GenericMetadataSupport upperBoundTypeVarMeta = metadata.resolveGenericReturnType(upperBoundTypeVarMethod);
        assertEquals(List.class, upperBoundTypeVarMeta.rawType());
    }

    @Test
    public void testTypeVarBoundedTypeDirectly() throws Exception {
        Method method = MultipleInterfaceBounds.class.getMethod("getT");
        TypeVariable<?> tv = (TypeVariable<?>) method.getGenericReturnType();

        GenericMetadataSupport.TypeVarBoundedType boundedType = new GenericMetadataSupport.TypeVarBoundedType(tv);
        assertEquals(Number.class, boundedType.firstBound());
        Type[] interfaceBounds = boundedType.interfaceBounds();
        assertEquals(2, interfaceBounds.length);
        assertEquals(Comparable.class, ((ParameterizedType) interfaceBounds[0]).getRawType());
        assertEquals(Serializable.class, interfaceBounds[1]);
        assertEquals(tv, boundedType.typeVariable());

        GenericMetadataSupport.TypeVarBoundedType sameBoundedType = new GenericMetadataSupport.TypeVarBoundedType(tv);
        assertEquals(boundedType, sameBoundedType);
        assertEquals(boundedType.hashCode(), sameBoundedType.hashCode());
        assertEquals(boundedType, boundedType);
        assertFalse(boundedType.equals(null));
        assertFalse(boundedType.equals("NotABoundedType"));

        String str = boundedType.toString();
        assertTrue(str.contains("firstBound="));
        assertTrue(str.contains("interfaceBounds="));
    }

    @Test
    public void testWildCardBoundedTypeDirectly() throws Exception {
        Method method = GenericsNest.class.getMethod("returning_wildcard_with_class_lower_bound");
        ParameterizedType listType = (ParameterizedType) method.getGenericReturnType();
        WildcardType wildcard = (WildcardType) listType.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType wildCardBoundedType = new GenericMetadataSupport.WildCardBoundedType(wildcard);
        assertEquals(Integer.class, wildCardBoundedType.firstBound());
        assertEquals(0, wildCardBoundedType.interfaceBounds().length);
        assertEquals(wildcard, wildCardBoundedType.wildCard());

        GenericMetadataSupport.WildCardBoundedType sameWildCardBoundedType = new GenericMetadataSupport.WildCardBoundedType(wildcard);
        assertEquals(wildCardBoundedType.hashCode(), sameWildCardBoundedType.hashCode());
        assertEquals(wildCardBoundedType, wildCardBoundedType);
        assertFalse(wildCardBoundedType.equals(null));
        assertFalse(wildCardBoundedType.equals("NotAWildcard"));

        String str = wildCardBoundedType.toString();
        assertTrue(str.contains("firstBound="));
        assertTrue(str.contains("interfaceBounds=[]"));
    }

    @Test
    public void testDeepNestedAndRecursiveTypes() throws Exception {
        GenericMetadataSupport metadata = GenericMetadataSupport.inferFrom(DeepNestedInterface.class);
        Method getY = DeepNestedInterface.class.getMethod("getY");
        GenericMetadataSupport getYMeta = metadata.resolveGenericReturnType(getY);
        assertEquals(List.class, getYMeta.rawType());

        GenericMetadataSupport recursiveMeta = GenericMetadataSupport.inferFrom(RecursiveTypeVar.class);
        Method self = RecursiveTypeVar.class.getMethod("self");
        GenericMetadataSupport selfMeta = recursiveMeta.resolveGenericReturnType(self);
        assertEquals(RecursiveTypeVar.class, selfMeta.rawType());
    }
}
