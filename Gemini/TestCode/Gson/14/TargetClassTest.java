package com.google.gson.internal;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class $Gson$TypesTest {

  private static class NonStaticInner<T> {
  }

  private static class GenericSuperclass<T, U> {
  }

  private static class SubclassWithGenerics<T> extends GenericSuperclass<T, Integer> {
  }

  private static class ConcreteSubclass extends SubclassWithGenerics<String> {
  }

  private interface InterfaceA<T> {
  }

  private interface InterfaceB<T> extends InterfaceA<List<T>> {
  }

  private static class ClassImplementingInterface implements InterfaceB<String> {
  }

  private static class CustomType implements Type {
    private final String typeName;

    public CustomType(String typeName) {
      this.typeName = typeName;
    }

    @Override
    public String toString() {
      return typeName;
    }
  }

  @Test(expected = InvocationTargetException.class)
  public void testPrivateConstructor() throws Exception {
    Constructor<$Gson$Types> constructor = $Gson$Types.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    constructor.newInstance();
  }

  @Test
  public void testNewParameterizedTypeWithOwner() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertNull(pt.getOwnerType());
    assertEquals(List.class, pt.getRawType());
    assertArrayEquals(new Type[] { String.class }, pt.getActualTypeArguments());
    assertEquals("java.util.List<java.lang.String>", pt.toString());

    ParameterizedType owner = $Gson$Types.newParameterizedTypeWithOwner(null, $Gson$TypesTest.class);
    ParameterizedType innerPt = $Gson$Types.newParameterizedTypeWithOwner($Gson$TypesTest.class, NonStaticInner.class, String.class);
    assertEquals($Gson$TypesTest.class, innerPt.getOwnerType());
    assertEquals(NonStaticInner.class, innerPt.getRawType());
    assertEquals(1, innerPt.getActualTypeArguments().length);
    assertEquals(String.class, innerPt.getActualTypeArguments()[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeWithOwnerMissingOwnerForNonStaticInner() {
    $Gson$Types.newParameterizedTypeWithOwner(null, NonStaticInner.class, String.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeWithPrimitive() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, int.class);
  }

  @Test(expected = NullPointerException.class)
  public void testNewParameterizedTypeWithNullArg() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, new Type[] { null });
  }

  @Test
  public void testParameterizedTypeEqualsAndHashCode() {
    ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, String.class);
    ParameterizedType pt4 = $Gson$Types.newParameterizedTypeWithOwner(String.class, Map.class, String.class, Integer.class);

    assertEquals(pt1, pt2);
    assertEquals(pt1.hashCode(), pt2.hashCode());
    assertFalse(pt1.equals(pt3));
    assertFalse(pt1.equals(pt4));
    assertFalse(pt1.equals(null));
    assertFalse(pt1.equals("not a parameterized type"));

    assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", pt1.toString());

    ParameterizedType emptyArgs = $Gson$Types.newParameterizedTypeWithOwner(null, String.class);
    assertEquals("java.lang.String", emptyArgs.toString());
  }

  @Test
  public void testArrayOf() {
    GenericArrayType arrayType = $Gson$Types.arrayOf(String.class);
    assertEquals(String.class, arrayType.getGenericComponentType());
    assertEquals("java.lang.String[]", arrayType.toString());

    GenericArrayType listOfStringArray = $Gson$Types.arrayOf($Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class));
    assertEquals("java.util.List<java.lang.String>[]", listOfStringArray.toString());

    GenericArrayType arrayType2 = $Gson$Types.arrayOf(String.class);
    assertEquals(arrayType, arrayType2);
    assertEquals(arrayType.hashCode(), arrayType2.hashCode());
    assertFalse(arrayType.equals(listOfStringArray));
    assertFalse(arrayType.equals(null));
    assertFalse(arrayType.equals(String[].class));
  }

  @Test
  public void testSubtypeOf() {
    WildcardType subtype = $Gson$Types.subtypeOf(CharSequence.class);
    assertArrayEquals(new Type[] { CharSequence.class }, subtype.getUpperBounds());
    assertArrayEquals($Gson$Types.EMPTY_TYPE_ARRAY, subtype.getLowerBounds());
    assertEquals("? extends java.lang.CharSequence", subtype.toString());

    WildcardType subtypeObject = $Gson$Types.subtypeOf(Object.class);
    assertEquals("?", subtypeObject.toString());
    assertArrayEquals(new Type[] { Object.class }, subtypeObject.getUpperBounds());
    assertArrayEquals($Gson$Types.EMPTY_TYPE_ARRAY, subtypeObject.getLowerBounds());
  }

  @Test
  public void testSupertypeOf() {
    WildcardType supertype = $Gson$Types.supertypeOf(String.class);
    assertArrayEquals(new Type[] { Object.class }, supertype.getUpperBounds());
    assertArrayEquals(new Type[] { String.class }, supertype.getLowerBounds());
    assertEquals("? super java.lang.String", supertype.toString());
  }

  @Test
  public void testWildcardTypeEqualsAndHashCode() {
    WildcardType w1 = $Gson$Types.subtypeOf(CharSequence.class);
    WildcardType w2 = $Gson$Types.subtypeOf(CharSequence.class);
    WildcardType w3 = $Gson$Types.supertypeOf(CharSequence.class);
    WildcardType w4 = $Gson$Types.supertypeOf(CharSequence.class);

    assertEquals(w1, w2);
    assertEquals(w1.hashCode(), w2.hashCode());
    assertEquals(w3, w4);
    assertEquals(w3.hashCode(), w4.hashCode());
    assertFalse(w1.equals(w3));
    assertFalse(w1.equals(null));
    assertFalse(w1.equals("? extends CharSequence"));
  }

  @Test
  public void testCanonicalize() {
    assertEquals(String.class, $Gson$Types.canonicalize(String.class));

    Type canonicalArray = $Gson$Types.canonicalize(String[].class);
    assertTrue(canonicalArray instanceof GenericArrayType);
    assertEquals(String.class, ((GenericArrayType) canonicalArray).getGenericComponentType());

    Type multiDimArray = $Gson$Types.canonicalize(int[][].class);
    assertTrue(multiDimArray instanceof GenericArrayType);

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type canonicalPt = $Gson$Types.canonicalize(pt);
    assertEquals(pt, canonicalPt);

    GenericArrayType ga = $Gson$Types.arrayOf(String.class);
    Type canonicalGa = $Gson$Types.canonicalize(ga);
    assertEquals(ga, canonicalGa);

    WildcardType wt = $Gson$Types.subtypeOf(String.class);
    Type canonicalWt = $Gson$Types.canonicalize(wt);
    assertEquals(wt, canonicalWt);

    CustomType custom = new CustomType("custom");
    assertEquals(custom, $Gson$Types.canonicalize(custom));
  }

  @Test
  public void testGetRawType() throws NoSuchFieldException {
    assertEquals(String.class, $Gson$Types.getRawType(String.class));

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals(List.class, $Gson$Types.getRawType(pt));

    GenericArrayType ga = $Gson$Types.arrayOf(String.class);
    assertEquals(String[].class, $Gson$Types.getRawType(ga));

    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    assertEquals(Number.class, $Gson$Types.getRawType(wt));

    class Holder<T extends CharSequence> {
      T field;
    }
    TypeVariable<?> typeVariable = (TypeVariable<?>) Holder.class.getDeclaredField("field").getGenericType();
    assertEquals(Object.class, $Gson$Types.getRawType(typeVariable));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRawTypeNull() {
    $Gson$Types.getRawType(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRawTypeUnsupported() {
    $Gson$Types.getRawType(new CustomType("unsupported"));
  }

  @Test
  public void testEqualsAndHashCodeOrZero() throws NoSuchFieldException {
    assertTrue($Gson$Types.equals(null, null));
    assertFalse($Gson$Types.equals(String.class, null));
    assertFalse($Gson$Types.equals(null, String.class));
    assertTrue($Gson$Types.equals(String.class, String.class));
    assertFalse($Gson$Types.equals(String.class, Integer.class));

    class TypeVarHolder<T, U> {
      T t;
      T t2;
      U u;
    }
    Type t1 = TypeVarHolder.class.getDeclaredField("t").getGenericType();
    Type t2 = TypeVarHolder.class.getDeclaredField("t2").getGenericType();
    Type u = TypeVarHolder.class.getDeclaredField("u").getGenericType();

    assertTrue($Gson$Types.equals(t1, t2));
    assertFalse($Gson$Types.equals(t1, u));
    assertFalse($Gson$Types.equals(t1, String.class));

    CustomType ct1 = new CustomType("type");
    CustomType ct2 = new CustomType("type");
    assertFalse($Gson$Types.equals(ct1, ct2));

    assertEquals(0, $Gson$Types.hashCodeOrZero(null));
    assertEquals("abc".hashCode(), $Gson$Types.hashCodeOrZero("abc"));
  }

  @Test
  public void testTypeToString() {
    assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals("java.util.List<java.lang.String>", $Gson$Types.typeToString(pt));
  }

  @Test
  public void testGetArrayComponentType() {
    assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    GenericArrayType ga = $Gson$Types.arrayOf(Integer.class);
    assertEquals(Integer.class, $Gson$Types.getArrayComponentType(ga));
  }

  @Test
  public void testGetCollectionElementType() {
    Type listOfStrings = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    assertEquals(String.class, $Gson$Types.getCollectionElementType(listOfStrings, List.class));

    assertEquals(Object.class, $Gson$Types.getCollectionElementType(Collection.class, Collection.class));

    Type wildcardCollection = $Gson$Types.subtypeOf(listOfStrings);
    assertEquals(String.class, $Gson$Types.getCollectionElementType(wildcardCollection, Collection.class));
  }

  @Test
  public void testGetMapKeyAndValueTypes() {
    Type[] propTypes = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
    assertArrayEquals(new Type[] { String.class, String.class }, propTypes);

    Type mapType = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    Type[] mapTypes = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
    assertArrayEquals(new Type[] { String.class, Integer.class }, mapTypes);

    Type[] rawMapTypes = $Gson$Types.getMapKeyAndValueTypes(Map.class, Map.class);
    assertArrayEquals(new Type[] { Object.class, Object.class }, rawMapTypes);
  }

  @Test
  public void testResolveTypeVariable() throws NoSuchFieldException {
    class GenericClass<A, B> {
      A a;
      B b;
      A[] arrayOfA;
      List<A> listOfA;
      WildcardType wildcard;
    }

    Type concreteType = $Gson$Types.newParameterizedTypeWithOwner(null, GenericClass.class, String.class, Integer.class);
    Type resolvedA = $Gson$Types.resolve(concreteType, GenericClass.class, GenericClass.class.getDeclaredField("a").getGenericType());
    assertEquals(String.class, resolvedA);

    Type resolvedB = $Gson$Types.resolve(concreteType, GenericClass.class, GenericClass.class.getDeclaredField("b").getGenericType());
    assertEquals(Integer.class, resolvedB);

    Type resolvedArrayOfA = $Gson$Types.resolve(concreteType, GenericClass.class, GenericClass.class.getDeclaredField("arrayOfA").getGenericType());
    assertTrue(resolvedArrayOfA instanceof GenericArrayType);
    assertEquals(String.class, ((GenericArrayType) resolvedArrayOfA).getGenericComponentType());

    Type resolvedListOfA = $Gson$Types.resolve(concreteType, GenericClass.class, GenericClass.class.getDeclaredField("listOfA").getGenericType());
    assertTrue(resolvedListOfA instanceof ParameterizedType);
    assertEquals(String.class, ((ParameterizedType) resolvedListOfA).getActualTypeArguments()[0]);
  }

  @Test
  public void testResolveWildcard() {
    class WildcardHolder<T> {
    }

    WildcardType unbounded = $Gson$Types.subtypeOf(Object.class);
    assertEquals(unbounded, $Gson$Types.resolve(Object.class, Object.class, unbounded));

    WildcardType subtype = $Gson$Types.subtypeOf(String.class);
    assertEquals(subtype, $Gson$Types.resolve(Object.class, Object.class, subtype));

    WildcardType supertype = $Gson$Types.supertypeOf(String.class);
    assertEquals(supertype, $Gson$Types.resolve(Object.class, Object.class, supertype));
  }

  @Test
  public void testResolveGenericSuperclassAndInterfaces() {
    Type resolvedInterfaceSupertype = $Gson$Types.getGenericSupertype(ClassImplementingInterface.class, ClassImplementingInterface.class, InterfaceA.class);
    assertTrue(resolvedInterfaceSupertype instanceof ParameterizedType);

    Type resolvedSuperclassSupertype = $Gson$Types.getGenericSupertype(ConcreteSubclass.class, ConcreteSubclass.class, GenericSuperclass.class);
    assertTrue(resolvedSuperclassSupertype instanceof ParameterizedType);

    Type notSupertype = $Gson$Types.getGenericSupertype(String.class, String.class, Integer.class);
    assertEquals(Integer.class, notSupertype);
  }

  @Test
  public void testResolveArrays() {
    assertEquals(String[].class, $Gson$Types.resolve(Object.class, Object.class, String[].class));
    GenericArrayType ga = $Gson$Types.arrayOf(String.class);
    assertEquals(ga, $Gson$Types.resolve(Object.class, Object.class, ga));
  }

  @Test
  public void testResolveTypeVariableWithMethodDeclaration() throws Exception {
    Method method = $Gson$TypesTest.class.getDeclaredMethod("methodWithTypeVar", Object.class);
    TypeVariable<?> typeVariable = (TypeVariable<?>) method.getGenericParameterTypes()[0];
    Type resolved = $Gson$Types.resolve(String.class, String.class, typeVariable);
    assertEquals(typeVariable, resolved);
  }

  private <V> void methodWithTypeVar(V val) {
  }

  @Test
  public void testCheckNotPrimitive() {
    $Gson$Types.checkNotPrimitive(String.class);
    try {
      $Gson$Types.checkNotPrimitive(int.class);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }
}
