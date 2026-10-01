package com.google.gson.internal;

import org.junit.Assert;
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

public class $Gson$TypesTest {

  static class Outer {
    class Inner<T> {}
    static class StaticNested<T> {}
  }

  interface CustomInterface<T> {}
  interface SubInterface<T> extends CustomInterface<T> {}
  static class CustomBase<T> implements CustomInterface<T> {}
  static class CustomSub<E> extends CustomBase<E> {}
  static class StringSub extends CustomBase<String> {}

  static class SelfRef<T extends SelfRef<T>> {
    T field;
  }

  static class GenericHolder<A, B> {
    A fieldA;
    B fieldB;
    A[] arrayA;
    List<A> listA;
    Map<A, B> mapAB;
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPrivateConstructor() throws Throwable {
    Constructor<$Gson$Types> constructor = $Gson$Types.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  @Test
  public void testNewParameterizedTypeWithOwner() {
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Assert.assertNull(pt.getOwnerType());
    Assert.assertEquals(List.class, pt.getRawType());
    Assert.assertArrayEquals(new Type[] { String.class }, pt.getActualTypeArguments());
    Assert.assertEquals("java.util.List<java.lang.String>", pt.toString());

    ParameterizedType innerPt = $Gson$Types.newParameterizedTypeWithOwner(Outer.class, Outer.Inner.class, Integer.class);
    Assert.assertEquals(Outer.class, innerPt.getOwnerType());
    Assert.assertEquals(Outer.Inner.class, innerPt.getRawType());
    Assert.assertArrayEquals(new Type[] { Integer.class }, innerPt.getActualTypeArguments());

    ParameterizedType emptyArgs = $Gson$Types.newParameterizedTypeWithOwner(null, String.class);
    Assert.assertEquals(0, emptyArgs.getActualTypeArguments().length);
    Assert.assertEquals("java.lang.String", emptyArgs.toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeWithOwnerMissingOwnerForNonStaticInnerClass() {
    $Gson$Types.newParameterizedTypeWithOwner(null, Outer.Inner.class, String.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeWithOwnerPrimitiveTypeArgument() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, int.class);
  }

  @Test
  public void testArrayOf() {
    GenericArrayType arrayType = $Gson$Types.arrayOf(String.class);
    Assert.assertEquals(String.class, arrayType.getGenericComponentType());
    Assert.assertEquals("java.lang.String[]", arrayType.toString());

    GenericArrayType nestedArray = $Gson$Types.arrayOf(arrayType);
    Assert.assertEquals(arrayType, nestedArray.getGenericComponentType());
    Assert.assertEquals("java.lang.String[][]", nestedArray.toString());
  }

  @Test
  public void testSubtypeOf() {
    WildcardType subtype = $Gson$Types.subtypeOf(Number.class);
    Assert.assertArrayEquals(new Type[] { Number.class }, subtype.getUpperBounds());
    Assert.assertArrayEquals(new Type[0], subtype.getLowerBounds());
    Assert.assertEquals("? extends java.lang.Number", subtype.toString());

    WildcardType objectSubtype = $Gson$Types.subtypeOf(Object.class);
    Assert.assertEquals("?", objectSubtype.toString());

    WildcardType nestedSubtype = $Gson$Types.subtypeOf(subtype);
    Assert.assertArrayEquals(new Type[] { Number.class }, nestedSubtype.getUpperBounds());
  }

  @Test
  public void testSupertypeOf() {
    WildcardType supertype = $Gson$Types.supertypeOf(Number.class);
    Assert.assertArrayEquals(new Type[] { Object.class }, supertype.getUpperBounds());
    Assert.assertArrayEquals(new Type[] { Number.class }, supertype.getLowerBounds());
    Assert.assertEquals("? super java.lang.Number", supertype.toString());

    WildcardType nestedSupertype = $Gson$Types.supertypeOf(supertype);
    Assert.assertArrayEquals(new Type[] { Number.class }, nestedSupertype.getLowerBounds());
  }

  @Test
  public void testCanonicalize() {
    Assert.assertEquals(String.class, $Gson$Types.canonicalize(String.class));

    Type arrayCanonical = $Gson$Types.canonicalize(int[].class);
    Assert.assertTrue(arrayCanonical instanceof GenericArrayType);
    Assert.assertEquals(int.class, ((GenericArrayType) arrayCanonical).getGenericComponentType());

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type ptCanonical = $Gson$Types.canonicalize(pt);
    Assert.assertEquals(pt, ptCanonical);

    GenericArrayType gat = $Gson$Types.arrayOf(String.class);
    Type gatCanonical = $Gson$Types.canonicalize(gat);
    Assert.assertEquals(gat, gatCanonical);

    WildcardType wt = $Gson$Types.subtypeOf(String.class);
    Type wtCanonical = $Gson$Types.canonicalize(wt);
    Assert.assertEquals(wt, wtCanonical);

    Type customType = new Type() {};
    Assert.assertSame(customType, $Gson$Types.canonicalize(customType));
  }

  @Test
  public void testGetRawType() throws Exception {
    Assert.assertEquals(String.class, $Gson$Types.getRawType(String.class));

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Assert.assertEquals(List.class, $Gson$Types.getRawType(pt));

    GenericArrayType gat = $Gson$Types.arrayOf(String.class);
    Assert.assertEquals(String[].class, $Gson$Types.getRawType(gat));

    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    Assert.assertEquals(Number.class, $Gson$Types.getRawType(wt));

    TypeVariable<?> tv = GenericHolder.class.getTypeParameters()[0];
    Assert.assertEquals(Object.class, $Gson$Types.getRawType(tv));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRawTypeNullThrowsException() {
    $Gson$Types.getRawType(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRawTypeUnsupportedTypeThrowsException() {
    Type unsupported = new Type() {};
    $Gson$Types.getRawType(unsupported);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRawTypeParameterizedTypeWithNonClassRawType() {
    ParameterizedType invalidPt = new ParameterizedType() {
      public Type[] getActualTypeArguments() { return new Type[] { String.class }; }
      public Type getRawType() { return new Type() {}; }
      public Type getOwnerType() { return null; }
    };
    $Gson$Types.getRawType(invalidPt);
  }

  @Test
  public void testEqualsAndHashCode() throws Exception {
    Type stringList1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type stringList2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type intList = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);

    Assert.assertTrue($Gson$Types.equals(stringList1, stringList1));
    Assert.assertTrue($Gson$Types.equals(stringList1, stringList2));
    Assert.assertEquals(stringList1.hashCode(), stringList2.hashCode());
    Assert.assertFalse($Gson$Types.equals(stringList1, intList));
    Assert.assertFalse($Gson$Types.equals(stringList1, null));
    Assert.assertFalse($Gson$Types.equals(stringList1, String.class));
    Assert.assertTrue($Gson$Types.equals((Type) null, (Type) null));
    Assert.assertFalse($Gson$Types.equals(String.class, null));

    Type array1 = $Gson$Types.arrayOf(String.class);
    Type array2 = $Gson$Types.arrayOf(String.class);
    Type intArray = $Gson$Types.arrayOf(Integer.class);
    Assert.assertTrue($Gson$Types.equals(array1, array2));
    Assert.assertEquals(array1.hashCode(), array2.hashCode());
    Assert.assertFalse($Gson$Types.equals(array1, intArray));
    Assert.assertFalse($Gson$Types.equals(array1, stringList1));

    Type wildcardExtends1 = $Gson$Types.subtypeOf(Number.class);
    Type wildcardExtends2 = $Gson$Types.subtypeOf(Number.class);
    Type wildcardSuper = $Gson$Types.supertypeOf(Number.class);
    Assert.assertTrue($Gson$Types.equals(wildcardExtends1, wildcardExtends2));
    Assert.assertEquals(wildcardExtends1.hashCode(), wildcardExtends2.hashCode());
    Assert.assertFalse($Gson$Types.equals(wildcardExtends1, wildcardSuper));
    Assert.assertFalse($Gson$Types.equals(wildcardExtends1, String.class));

    TypeVariable<?> tvA1 = GenericHolder.class.getTypeParameters()[0];
    TypeVariable<?> tvA2 = GenericHolder.class.getTypeParameters()[0];
    TypeVariable<?> tvB = GenericHolder.class.getTypeParameters()[1];
    Assert.assertTrue($Gson$Types.equals(tvA1, tvA2));
    Assert.assertFalse($Gson$Types.equals(tvA1, tvB));
    Assert.assertFalse($Gson$Types.equals(tvA1, String.class));

    Type unsupported1 = new Type() {};
    Type unsupported2 = new Type() {};
    Assert.assertFalse($Gson$Types.equals(unsupported1, unsupported2));
  }

  @Test
  public void testGetArrayComponentType() {
    Assert.assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    GenericArrayType gat = $Gson$Types.arrayOf(Integer.class);
    Assert.assertEquals(Integer.class, $Gson$Types.getArrayComponentType(gat));
  }

  @Test
  public void testGetCollectionElementType() {
    ParameterizedType stringList = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Assert.assertEquals(String.class, $Gson$Types.getCollectionElementType(stringList, List.class));

    Assert.assertEquals(Object.class, $Gson$Types.getCollectionElementType(ArrayList.class, ArrayList.class));

    WildcardType wildcardList = $Gson$Types.subtypeOf(stringList);
    Assert.assertEquals(String.class, $Gson$Types.getCollectionElementType(wildcardList, List.class));
  }

  @Test
  public void testGetMapKeyAndValueTypes() {
    Type[] propTypes = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
    Assert.assertEquals(String.class, propTypes[0]);
    Assert.assertEquals(String.class, propTypes[1]);

    ParameterizedType mapType = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    Type[] resolved = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
    Assert.assertEquals(String.class, resolved[0]);
    Assert.assertEquals(Integer.class, resolved[1]);

    Type[] rawMapTypes = $Gson$Types.getMapKeyAndValueTypes(Map.class, Map.class);
    Assert.assertEquals(Object.class, rawMapTypes[0]);
    Assert.assertEquals(Object.class, rawMapTypes[1]);
  }

  @Test
  public void testGetGenericSupertype() {
    Type superInterface = $Gson$Types.getGenericSupertype(SubInterface.class, SubInterface.class, CustomInterface.class);
    Assert.assertTrue(superInterface instanceof ParameterizedType);
    Assert.assertEquals(CustomInterface.class, ((ParameterizedType) superInterface).getRawType());

    Type superclass = $Gson$Types.getGenericSupertype(StringSub.class, StringSub.class, CustomInterface.class);
    Assert.assertTrue(superclass instanceof ParameterizedType);
    Assert.assertEquals(CustomInterface.class, ((ParameterizedType) superclass).getRawType());

    Type unresolved = $Gson$Types.getGenericSupertype(String.class, String.class, Set.class);
    Assert.assertEquals(Set.class, unresolved);
  }

  @Test
  public void testResolve() throws Exception {
    Type stringListType = GenericHolder.class.getDeclaredField("listA").getGenericType();
    ParameterizedType holderType = $Gson$Types.newParameterizedTypeWithOwner(null, GenericHolder.class, String.class, Integer.class);
    Type resolvedList = $Gson$Types.resolve(holderType, GenericHolder.class, stringListType);
    Assert.assertTrue(resolvedList instanceof ParameterizedType);
    Assert.assertEquals(String.class, ((ParameterizedType) resolvedList).getActualTypeArguments()[0]);

    Type arrayType = GenericHolder.class.getDeclaredField("arrayA").getGenericType();
    Type resolvedArray = $Gson$Types.resolve(holderType, GenericHolder.class, arrayType);
    Assert.assertTrue(resolvedArray instanceof GenericArrayType);
    Assert.assertEquals(String.class, ((GenericArrayType) resolvedArray).getGenericComponentType());

    Type plainArray = String[].class;
    Type resolvedPlainArray = $Gson$Types.resolve(holderType, GenericHolder.class, plainArray);
    Assert.assertEquals(String[].class, resolvedPlainArray);

    Type selfRefType = SelfRef.class.getDeclaredField("field").getGenericType();
    Type resolvedSelfRef = $Gson$Types.resolve(SelfRef.class, SelfRef.class, selfRefType);
    Assert.assertEquals(selfRefType, resolvedSelfRef);

    WildcardType wildcardSub = $Gson$Types.subtypeOf(GenericHolder.class.getTypeParameters()[0]);
    Type resolvedWildcardSub = $Gson$Types.resolve(holderType, GenericHolder.class, wildcardSub);
    Assert.assertTrue(resolvedWildcardSub instanceof WildcardType);
    Assert.assertArrayEquals(new Type[] { String.class }, ((WildcardType) resolvedWildcardSub).getUpperBounds());

    WildcardType wildcardSuper = $Gson$Types.supertypeOf(GenericHolder.class.getTypeParameters()[0]);
    Type resolvedWildcardSuper = $Gson$Types.resolve(holderType, GenericHolder.class, wildcardSuper);
    Assert.assertTrue(resolvedWildcardSuper instanceof WildcardType);
    Assert.assertArrayEquals(new Type[] { String.class }, ((WildcardType) resolvedWildcardSuper).getLowerBounds());
  }

  @Test
  public void testResolveTypeVariableDeclaredByMethod() throws Exception {
    Method method = $Gson$TypesTest.class.getDeclaredMethod("dummyGenericMethod");
    TypeVariable<?> tv = method.getTypeParameters()[0];
    Type resolved = $Gson$Types.resolveTypeVariable(String.class, String.class, tv);
    Assert.assertEquals(tv, resolved);
  }

  <M> void dummyGenericMethod() {}

  @Test(expected = IllegalArgumentException.class)
  public void testWildcardTypeMultipleLowerBoundsThrows() {
    new $Gson$TypesTestProxy().createWildcard(new Type[] { Object.class }, new Type[] { String.class, Integer.class });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWildcardTypeMultipleUpperBoundsThrows() {
    new $Gson$TypesTestProxy().createWildcard(new Type[] { String.class, Integer.class }, new Type[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWildcardTypePrimitiveLowerBoundThrows() {
    $Gson$Types.supertypeOf(int.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWildcardTypePrimitiveUpperBoundThrows() {
    $Gson$Types.subtypeOf(int.class);
  }

  @Test
  public void testCheckNotPrimitive() {
    $Gson$Types.checkNotPrimitive(String.class);
    try {
      $Gson$Types.checkNotPrimitive(int.class);
      Assert.fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testTypeToString() {
    Assert.assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));
    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Assert.assertEquals("java.util.List<java.lang.String>", $Gson$Types.typeToString(pt));
  }

  private static class $Gson$TypesTestProxy {
    WildcardType createWildcard(Type[] upper, Type[] lower) {
      try {
        Class<?> clazz = Class.forName("com.google.gson.internal.$Gson$Types$WildcardTypeImpl");
        Constructor<?> ctor = clazz.getDeclaredConstructor(Type[].class, Type[].class);
        ctor.setAccessible(true);
        return (WildcardType) ctor.newInstance(upper, lower);
      } catch (InvocationTargetException e) {
        if (e.getCause() instanceof RuntimeException) {
          throw (RuntimeException) e.getCause();
        }
        throw new RuntimeException(e.getCause());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
