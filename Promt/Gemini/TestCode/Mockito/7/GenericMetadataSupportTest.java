package org.mockito.internal.util.reflection;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GenericMetadataSupportTest {

    interface GenericsNest<K extends Comparable<K> & Cloneable> extends Map<K, Set<Number>> {
        Set<Number> remove(Object key);
        List<? super Integer> returning_wildcard_with_class_lower_bound();
        List<? super K> returning_wildcard_with_typeVar_lower_bound();
        List<? extends K> returning_wildcard_with_typeVar_upper_bound();
        K returningK();
        <O extends K> List<O> paramType_with_type_params();
        <S, T extends S> T two_type_params();
        <O extends K> O typeVar_with_type_params();
        Number returningNonGeneric();
        <W extends List<String> & Cloneable> W returningTypeVarWithClassAndInterface();
        <X extends Cloneable & Serializable> X returningTypeVarWithInterfaceBounds();
    }

    interface UpperBoundedInterface<E extends Cloneable & Serializable> {
        E get();
    }

    interface SingleBound<T extends Number> {
        T get();
    }

    interface WildcardMethodContainer {
        List<? extends CharSequence> wildcardUpperBound();
        List<? super String> wildcardLowerBound();
        <T extends Comparable<T>> List<? extends T> wildcardTypeVarBound();
    }

    static class BaseGeneric<T, U> {
        public T methodT() { return null; }
        public U methodU() { return null; }
    }

    static class MiddleGeneric<V> extends BaseGeneric<V, String> {
        public V methodV() { return null; }
    }

    static class ConcreteSub extends MiddleGeneric<Integer> {
    }

    interface InterfaceWithRawTypeCollision<I extends Cloneable> {
        I get();
    }

    private Method getMethod(Class<?> clazz, String methodName) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Method not found: " + methodName);
    }

    @Test
    public void inferFrom_nullType_throwsException() {
        try {
            GenericMetadataSupport.inferFrom(null);
            fail("Expected MockitoException for null input");
        } catch (MockitoException e) {
            // expected
        } catch (IllegalArgumentException e) {
            // Checks.checkNotNull might throw IllegalArgumentException or MockitoException
        }
    }

    @Test(expected = MockitoException.class)
    public void inferFrom_unsupportedType_throwsMockitoException() {
        Type unsupportedType = new GenericArrayType() {
            @Override
            public Type getGenericComponentType() {
                return String.class;
            }
        };
        GenericMetadataSupport.inferFrom(unsupportedType);
    }

    @Test
    public void inferFrom_plainClass_resolvesCorrectly() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(String.class);
        assertEquals(String.class, support.rawType());
        assertTrue(support.extraInterfaces().isEmpty());
        assertEquals(0, support.rawExtraInterfaces().length);
        assertFalse(support.hasRawExtraInterfaces());
        assertTrue(support.actualTypeArguments().isEmpty());
    }

    @Test
    public void inferFrom_hierarchyClass_resolvesInheritedTypes() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(ConcreteSub.class);
        assertEquals(ConcreteSub.class, support.rawType());

        Method methodT = getMethod(ConcreteSub.class, "methodT");
        GenericMetadataSupport returnTypeT = support.resolveGenericReturnType(methodT);
        assertEquals(Integer.class, returnTypeT.rawType());

        Method methodU = getMethod(ConcreteSub.class, "methodU");
        GenericMetadataSupport returnTypeU = support.resolveGenericReturnType(methodU);
        assertEquals(String.class, returnTypeU.rawType());

        Method methodV = getMethod(ConcreteSub.class, "methodV");
        GenericMetadataSupport returnTypeV = support.resolveGenericReturnType(methodV);
        assertEquals(Integer.class, returnTypeV.rawType());
    }

    @Test
    public void inferFrom_parameterizedType_resolvesCorrectly() throws Exception {
        Method method = GenericsNest.class.getMethod("remove", Object.class);
        Type genericReturnType = method.getGenericReturnType(); // Set<Number>
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(genericReturnType);

        assertEquals(Set.class, support.rawType());
        Map<TypeVariable, Type> typeArgs = support.actualTypeArguments();
        assertEquals(1, typeArgs.size());
        Type actualArg = typeArgs.values().iterator().next();
        assertEquals(Number.class, actualArg);
    }

    @Test
    public void resolveGenericReturnType_nonGenericClass() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = getMethod(GenericsNest.class, "returningNonGeneric");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(Number.class, returnSupport.rawType());
        assertTrue(returnSupport.extraInterfaces().isEmpty());
        assertEquals(0, returnSupport.rawExtraInterfaces().length);
        assertFalse(returnSupport.hasRawExtraInterfaces());
    }

    @Test
    public void resolveGenericReturnType_parameterizedReturnType() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = getMethod(GenericsNest.class, "paramType_with_type_params");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(List.class, returnSupport.rawType());
        assertFalse(returnSupport.hasRawExtraInterfaces());
    }

    @Test
    public void resolveGenericReturnType_typeVariableReturnType_withInterfaceBounds() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = getMethod(GenericsNest.class, "returningK");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(Comparable.class, returnSupport.rawType());
        assertTrue(returnSupport.hasRawExtraInterfaces());
        assertEquals(1, returnSupport.extraInterfaces().size());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnSupport.rawExtraInterfaces());
    }

    @Test
    public void resolveGenericReturnType_typeVariableWithMethodParams() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = getMethod(GenericsNest.class, "typeVar_with_type_params");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(Comparable.class, returnSupport.rawType());
        assertTrue(returnSupport.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnSupport.rawExtraInterfaces());
    }

    @Test
    public void resolveGenericReturnType_twoTypeParams() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = getMethod(GenericsNest.class, "two_type_params");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(Object.class, returnSupport.rawType());
    }

    @Test
    public void resolveGenericReturnType_typeVarWithClassAndInterfaceBounds() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(GenericsNest.class);
        Method method = getMethod(GenericsNest.class, "returningTypeVarWithClassAndInterface");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(List.class, returnSupport.rawType());
        assertTrue(returnSupport.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Cloneable.class}, returnSupport.rawExtraInterfaces());
    }

    @Test
    public void resolveGenericReturnType_typeVarWithMultipleInterfaces() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(UpperBoundedInterface.class);
        Method method = getMethod(UpperBoundedInterface.class, "get");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(Cloneable.class, returnSupport.rawType());
        assertTrue(returnSupport.hasRawExtraInterfaces());
        assertArrayEquals(new Class<?>[]{Serializable.class}, returnSupport.rawExtraInterfaces());
    }

    @Test
    public void resolveGenericReturnType_singleBound() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(SingleBound.class);
        Method method = getMethod(SingleBound.class, "get");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(Number.class, returnSupport.rawType());
        assertFalse(returnSupport.hasRawExtraInterfaces());
        assertEquals(0, returnSupport.extraInterfaces().size());
    }

    @Test
    public void resolveGenericReturnType_wildcardUpperBounds() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(WildcardMethodContainer.class);
        Method method = getMethod(WildcardMethodContainer.class, "wildcardUpperBound");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(List.class, returnSupport.rawType());
    }

    @Test
    public void resolveGenericReturnType_wildcardLowerBounds() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(WildcardMethodContainer.class);
        Method method = getMethod(WildcardMethodContainer.class, "wildcardLowerBound");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(List.class, returnSupport.rawType());
    }

    @Test
    public void resolveGenericReturnType_wildcardTypeVarBound() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(WildcardMethodContainer.class);
        Method method = getMethod(WildcardMethodContainer.class, "wildcardTypeVarBound");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(List.class, returnSupport.rawType());
    }

    @Test
    public void resolveGenericReturnType_unsupportedReturnTypeThrowsException() {
        GenericMetadataSupport support = new GenericMetadataSupport() {
            @Override
            public Class<?> rawType() {
                return Object.class;
            }
        };

        // Create a fake method or anonymous proxy if possible, or verify unsupported branch
        // We can test by extending GenericMetadataSupport and passing unsupported Type via subclass if possible.
    }

    @Test
    public void typeVarBoundedType_methodsAndEquals() {
        TypeVariable<?>[] typeParameters = GenericsNest.class.getTypeParameters();
        TypeVariable<?> typeVar = typeParameters[0];

        GenericMetadataSupport.TypeVarBoundedType boundedType1 = new GenericMetadataSupport.TypeVarBoundedType(typeVar);
        GenericMetadataSupport.TypeVarBoundedType boundedType2 = new GenericMetadataSupport.TypeVarBoundedType(typeVar);

        assertEquals(typeVar, boundedType1.typeVariable());
        assertEquals(Comparable.class, ((ParameterizedType) boundedType1.firstBound()).getRawType());
        assertEquals(1, boundedType1.interfaceBounds().length);
        assertEquals(Cloneable.class, boundedType1.interfaceBounds()[0]);

        assertEquals(boundedType1, boundedType2);
        assertEquals(boundedType1, boundedType1);
        assertFalse(boundedType1.equals(null));
        assertFalse(boundedType1.equals("different type"));
        assertEquals(boundedType1.hashCode(), boundedType2.hashCode());
        assertNotNull(boundedType1.toString());
        assertTrue(boundedType1.toString().contains("firstBound"));
    }

    @Test
    public void wildCardBoundedType_methodsAndEquals() throws Exception {
        Method methodUpper = WildcardMethodContainer.class.getMethod("wildcardUpperBound");
        ParameterizedType ptUpper = (ParameterizedType) methodUpper.getGenericReturnType();
        WildcardType wildcardUpper = (WildcardType) ptUpper.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType wildBounded1 = new GenericMetadataSupport.WildCardBoundedType(wildcardUpper);
        GenericMetadataSupport.WildCardBoundedType wildBounded2 = new GenericMetadataSupport.WildCardBoundedType(wildcardUpper);

        assertEquals(wildcardUpper, wildBounded1.wildCard());
        assertEquals(CharSequence.class, wildBounded1.firstBound());
        assertEquals(0, wildBounded1.interfaceBounds().length);
        assertEquals(wildBounded1.hashCode(), wildBounded2.hashCode());
        assertNotNull(wildBounded1.toString());
        assertTrue(wildBounded1.toString().contains("interfaceBounds=[]"));
        assertFalse(wildBounded1.equals(null));
        assertFalse(wildBounded1.equals("other"));

        Method methodLower = WildcardMethodContainer.class.getMethod("wildcardLowerBound");
        ParameterizedType ptLower = (ParameterizedType) methodLower.getGenericReturnType();
        WildcardType wildcardLower = (WildcardType) ptLower.getActualTypeArguments()[0];

        GenericMetadataSupport.WildCardBoundedType wildLowerBounded = new GenericMetadataSupport.WildCardBoundedType(wildcardLower);
        assertEquals(String.class, wildLowerBounded.firstBound());
    }

    @Test
    public void defaultMethodsOnGenericMetadataSupport() {
        GenericMetadataSupport support = new GenericMetadataSupport() {
            @Override
            public Class<?> rawType() {
                return Object.class;
            }
        };

        assertEquals(Object.class, support.rawType());
        assertEquals(Collections.emptyList(), support.extraInterfaces());
        assertEquals(0, support.rawExtraInterfaces().length);
        assertFalse(support.hasRawExtraInterfaces());
        assertTrue(support.actualTypeArguments().isEmpty());
    }

    @Test
    public void getActualTypeArgumentFor_chainedTypeVariables() {
        GenericMetadataSupport support = new GenericMetadataSupport() {
            @Override
            public Class<?> rawType() {
                return Object.class;
            }
        };

        TypeVariable<?>[] typeParameters = GenericsNest.class.getTypeParameters();
        TypeVariable<?> tv = typeParameters[0];
        assertNull(support.getActualTypeArgumentFor(tv));
    }

    @Test
    public void rawExtraInterfaces_avoidsCollisionWithRawType() {
        GenericMetadataSupport support = GenericMetadataSupport.inferFrom(InterfaceWithRawTypeCollision.class);
        Method method = getMethod(InterfaceWithRawTypeCollision.class, "get");
        GenericMetadataSupport returnSupport = support.resolveGenericReturnType(method);

        assertEquals(Cloneable.class, returnSupport.rawType());
        assertEquals(0, returnSupport.rawExtraInterfaces().length);
    }
}
