package com.google.gson.internal;

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

public class $Gson$TypesTest {

  private static class NonStaticInner<T> {}

  private static class ParameterizedHolder<T> {
    List<T> list;
    T item;
    T[] array;
    List<? extends T> extendsList;
    List<? super T> superList;
  }

  private static class SubParameterizedHolder extends ParameterizedHolder<String> {}

  private interface InterfaceA<T> {}
  private interface InterfaceB<T> extends InterfaceA<T> {}
  private static class ClassA<T> implements InterfaceB<T> {}
  private static class ClassB extends ClassA<Integer> {}

  private static class CustomTypeVariable implements TypeVariable<GenericDeclaration> {
    private final String name;
    private final GenericDeclaration declaration;

    CustomTypeVariable(String name, GenericDeclaration declaration) {
      this.name = name;
      this.declaration = declaration;
    }

    @Override
    public Type[] getBounds() {
      return new Type[] { Object.class };
    }

    @Override
    public GenericDeclaration getGenericDeclaration() {
      return declaration;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public java.lang.annotation.Annotation[] getAnnotations() {
      return new java.lang.annotation.Annotation[0];
    }

    @Override
    public java.lang.annotation.Annotation[] getDeclaredAnnotations() {
      return new java.lang.annotation.Annotation[0];
    }

    @Override
    public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationClass) {
      return null;
    }
  }

  private static class CustomUnknownType implements Type {
    @Override
    public String toString() {
      return "CustomUnknownType";
    }
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

    ParameterizedType innerPt = $Gson$Types.newParameterizedTypeWithOwner(
        $Gson$TypesTest.class, NonStaticInner.class, String.class);
    Assert.assertEquals($Gson$TypesTest.class, innerPt.getOwnerType());
    Assert.assertEquals(NonStaticInner.class, innerPt.getRawType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewParameterizedTypeWithOwnerNonStaticInnerRequiresOwner() {
    $Gson$Types.newParameterizedTypeWithOwner(null, NonStaticInner.class, String.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testParameterizedTypeRejectsPrimitives() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, int.class);
  }

  @Test(expected = NullPointerException.class)
  public void testParameterizedTypeRejectsNullArgument() {
    $Gson$Types.newParameterizedTypeWithOwner(null, List.class, new Type[] { null });
  }

  @Test
  public void testArrayOf() {
    GenericArrayType arrayType = $Gson$Types.arrayOf(String.class);
    Assert.assertEquals(String.class, arrayType.getGenericComponentType());
    Assert.assertEquals("java.lang.String[]", arrayType.toString());

    GenericArrayType listOfStringType = $Gson$Types.arrayOf(
        $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class));
    Assert.assertEquals("java.util.List<java.lang.String>[]", listOfStringType.toString());
  }

  @Test
  public void testSubtypeOf() {
    WildcardType subtype = $Gson$Types.subtypeOf(Number.class);
    Assert.assertArrayEquals(new Type[] { Number.class }, subtype.getUpperBounds());
    Assert.assertArrayEquals($Gson$Types.EMPTY_TYPE_ARRAY, subtype.getLowerBounds());
    Assert.assertEquals("? extends java.lang.Number", subtype.toString());

    WildcardType objSubtype = $Gson$Types.subtypeOf(Object.class);
    Assert.assertEquals("?", objSubtype.toString());

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

  @Test(expected = IllegalArgumentException.class)
  public void testWildcardTypeRejectsPrimitive() {
    $Gson$Types.subtypeOf(int.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWildcardTypeRejectsPrimitiveInSupertype() {
    $Gson$Types.supertypeOf(int.class);
  }

  @Test
  public void testCanonicalize() {
    Assert.assertEquals(String.class, $Gson$Types.canonicalize(String.class));

    Type arrayCanonical = $Gson$Types.canonicalize(String[].class);
    Assert.assertTrue(arrayCanonical instanceof GenericArrayType);
    Assert.assertEquals(String.class, ((GenericArrayType) arrayCanonical).getGenericComponentType());

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type ptCanonical = $Gson$Types.canonicalize(pt);
    Assert.assertTrue(ptCanonical instanceof ParameterizedType);
    Assert.assertEquals(pt, ptCanonical);

    GenericArrayType gat = $Gson$Types.arrayOf(String.class);
    Type gatCanonical = $Gson$Types.canonicalize(gat);
    Assert.assertTrue(gatCanonical instanceof GenericArrayType);
    Assert.assertEquals(gat, gatCanonical);

    WildcardType wt = $Gson$Types.subtypeOf(String.class);
    Type wtCanonical = $Gson$Types.canonicalize(wt);
    Assert.assertTrue(wtCanonical instanceof WildcardType);
    Assert.assertEquals(wt, wtCanonical);

    CustomUnknownType custom = new CustomUnknownType();
    Assert.assertSame(custom, $Gson$Types.canonicalize(custom));
  }

  @Test
  public void testGetRawType() throws NoSuchFieldException {
    Assert.assertEquals(String.class, $Gson$Types.getRawType(String.class));

    ParameterizedType pt = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Assert.assertEquals(List.class, $Gson$Types.getRawType(pt));

    GenericArrayType gat = $Gson$Types.arrayOf(String.class);
    Assert.assertEquals(String[].class, $Gson$Types.getRawType(gat));

    TypeVariable<?> tv = ParameterizedHolder.class.getTypeParameters()[0];
    Assert.assertEquals(Object.class, $Gson$Types.getRawType(tv));

    WildcardType wt = $Gson$Types.subtypeOf(Number.class);
    Assert.assertEquals(Number.class, $Gson$Types.getRawType(wt));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRawTypeNull() {
    $Gson$Types.getRawType(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRawTypeUnknown() {
    $Gson$Types.getRawType(new CustomUnknownType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetRawTypeParameterizedTypeNotClass() {
    ParameterizedType mockPt = new ParameterizedType() {
      @Override
      public Type[] getActualTypeArguments() { return new Type[0]; }
      @Override
      public Type getRawType() { return new CustomUnknownType(); }
      @Override
      public Type getOwnerType() { return null; }
    };
    $Gson$Types.getRawType(mockPt);
  }

  @Test
  public void testEqualsAndHashCode() {
    Assert.assertTrue($Gson$Types.equals(null, null));
    Assert.assertFalse($Gson$Types.equals(String.class, null));
    Assert.assertFalse($Gson$Types.equals(null, String.class));
    Assert.assertTrue($Gson$Types.equals(String.class, String.class));
    Assert.assertFalse($Gson$Types.equals(String.class, Integer.class));

    ParameterizedType pt1 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType pt2 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    ParameterizedType pt3 = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, Integer.class);
    ParameterizedType pt4 = $Gson$Types.newParameterizedTypeWithOwner(null, Set.class, String.class);
    ParameterizedType pt5 = $Gson$Types.newParameterizedTypeWithOwner($Gson$TypesTest.class, NonStaticInner.class, String.class);

    Assert.assertTrue($Gson$Types.equals(pt1, pt2));
    Assert.assertFalse($Gson$Types.equals(pt1, pt3));
    Assert.assertFalse($Gson$Types.equals(pt1, pt4));
    Assert.assertFalse($Gson$Types.equals(pt1, pt5));
    Assert.assertFalse($Gson$Types.equals(pt1, String.class));
    Assert.assertEquals(pt1.hashCode(), pt2.hashCode());
    Assert.assertEquals(pt1, pt2);
    Assert.assertFalse(pt1.equals(null));
    Assert.assertFalse(pt1.equals(new Object()));

    GenericArrayType ga1 = $Gson$Types.arrayOf(String.class);
    GenericArrayType ga2 = $Gson$Types.arrayOf(String.class);
    GenericArrayType ga3 = $Gson$Types.arrayOf(Integer.class);

    Assert.assertTrue($Gson$Types.equals(ga1, ga2));
    Assert.assertFalse($Gson$Types.equals(ga1, ga3));
    Assert.assertFalse($Gson$Types.equals(ga1, String.class));
    Assert.assertEquals(ga1.hashCode(), ga2.hashCode());
    Assert.assertEquals(ga1, ga2);
    Assert.assertFalse(ga1.equals(null));
    Assert.assertFalse(ga1.equals(new Object()));

    WildcardType w1 = $Gson$Types.subtypeOf(Number.class);
    WildcardType w2 = $Gson$Types.subtypeOf(Number.class);
    WildcardType w3 = $Gson$Types.supertypeOf(Number.class);
    WildcardType w4 = $Gson$Types.subtypeOf(Integer.class);

    Assert.assertTrue($Gson$Types.equals(w1, w2));
    Assert.assertFalse($Gson$Types.equals(w1, w3));
    Assert.assertFalse($Gson$Types.equals(w1, w4));
    Assert.assertFalse($Gson$Types.equals(w1, String.class));
    Assert.assertEquals(w1.hashCode(), w2.hashCode());
    Assert.assertEquals(w1, w2);
    Assert.assertFalse(w1.equals(null));
    Assert.assertFalse(w1.equals(new Object()));

    TypeVariable<?> tv1 = new CustomTypeVariable("T", $Gson$TypesTest.class);
    TypeVariable<?> tv2 = new CustomTypeVariable("T", $Gson$TypesTest.class);
    TypeVariable<?> tv3 = new CustomTypeVariable("E", $Gson$TypesTest.class);
    TypeVariable<?> tv4 = new CustomTypeVariable("T", String.class);

    Assert.assertTrue($Gson$Types.equals(tv1, tv2));
    Assert.assertFalse($Gson$Types.equals(tv1, tv3));
    Assert.assertFalse($Gson$Types.equals(tv1, tv4));
    Assert.assertFalse($Gson$Types.equals(tv1, String.class));

    CustomUnknownType custom1 = new CustomUnknownType();
    CustomUnknownType custom2 = new CustomUnknownType();
    Assert.assertFalse($Gson$Types.equals(custom1, custom2));
  }

  @Test
  public void testTypeToString() {
    Assert.assertEquals("java.lang.String", $Gson$Types.typeToString(String.class));

    ParameterizedType zeroArgPt = $Gson$Types.newParameterizedTypeWithOwner(null, String.class);
    Assert.assertEquals("java.lang.String", zeroArgPt.toString());

    ParameterizedType multiArgPt = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    Assert.assertEquals("java.util.Map<java.lang.String, java.lang.Integer>", multiArgPt.toString());
  }

  @Test
  public void testGetGenericSupertype() {
    Type supertype = $Gson$Types.getGenericSupertype(ClassB.class, ClassB.class, InterfaceA.class);
    Assert.assertTrue(supertype instanceof ParameterizedType);
    Assert.assertEquals(InterfaceA.class, ((ParameterizedType) supertype).getRawType());
    Assert.assertEquals(Integer.class, ((ParameterizedType) supertype).getActualTypeArguments()[0]);

    Type unresolved = $Gson$Types.getGenericSupertype(Object.class, Object.class, List.class);
    Assert.assertEquals(List.class, unresolved);

    Type same = $Gson$Types.getGenericSupertype(String.class, String.class, String.class);
    Assert.assertEquals(String.class, same);
  }

  @Test
  public void testGetArrayComponentType() {
    Assert.assertEquals(String.class, $Gson$Types.getArrayComponentType(String[].class));
    GenericArrayType gat = $Gson$Types.arrayOf(Integer.class);
    Assert.assertEquals(Integer.class, $Gson$Types.getArrayComponentType(gat));
  }

  @Test(expected = ClassCastException.class)
  public void testGetArrayComponentTypeInvalid() {
    $Gson$Types.getArrayComponentType(String.class);
  }

  @Test
  public void testGetCollectionElementType() {
    Type listOfStringType = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    Type elemType = $Gson$Types.getCollectionElementType(listOfStringType, List.class);
    Assert.assertEquals(String.class, elemType);

    Type rawCollectionElem = $Gson$Types.getCollectionElementType(ArrayList.class, ArrayList.class);
    Assert.assertEquals(Object.class, rawCollectionElem);

    WildcardType wildcardCollection = $Gson$Types.subtypeOf(listOfStringType);
    Type wildcardElem = $Gson$Types.getCollectionElementType(wildcardCollection, List.class);
    Assert.assertEquals(String.class, wildcardElem);
  }

  @Test
  public void testGetMapKeyAndValueTypes() {
    Type[] propTypes = $Gson$Types.getMapKeyAndValueTypes(Properties.class, Properties.class);
    Assert.assertArrayEquals(new Type[] { String.class, String.class }, propTypes);

    Type mapType = $Gson$Types.newParameterizedTypeWithOwner(null, Map.class, String.class, Integer.class);
    Type[] resolved = $Gson$Types.getMapKeyAndValueTypes(mapType, Map.class);
    Assert.assertArrayEquals(new Type[] { String.class, Integer.class }, resolved);

    Type[] rawResolved = $Gson$Types.getMapKeyAndValueTypes(HashMap.class, HashMap.class);
    Assert.assertArrayEquals(new Type[] { Object.class, Object.class }, rawResolved);
  }

  @Test
  public void testResolve() throws NoSuchFieldException {
    Type itemField = ParameterizedHolder.class.getDeclaredField("item").getGenericType();
    Type resolvedItem = $Gson$Types.resolve(SubParameterizedHolder.class, SubParameterizedHolder.class, itemField);
    Assert.assertEquals(String.class, resolvedItem);

    Type listField = ParameterizedHolder.class.getDeclaredField("list").getGenericType();
    Type resolvedList = $Gson$Types.resolve(SubParameterizedHolder.class, SubParameterizedHolder.class, listField);
    Assert.assertEquals($Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class), resolvedList);

    Type arrayField = ParameterizedHolder.class.getDeclaredField("array").getGenericType();
    Type resolvedArray = $Gson$Types.resolve(SubParameterizedHolder.class, SubParameterizedHolder.class, arrayField);
    Assert.assertEquals($Gson$Types.arrayOf(String.class), resolvedArray);

    Type extendsField = ParameterizedHolder.class.getDeclaredField("extendsList").getGenericType();
    Type resolvedExtends = $Gson$Types.resolve(SubParameterizedHolder.class, SubParameterizedHolder.class, extendsField);
    Assert.assertEquals($Gson$Types.newParameterizedTypeWithOwner(null, List.class, $Gson$Types.subtypeOf(String.class)), resolvedExtends);

    Type superField = ParameterizedHolder.class.getDeclaredField("superList").getGenericType();
    Type resolvedSuper = $Gson$Types.resolve(SubParameterizedHolder.class, SubParameterizedHolder.class, superField);
    Assert.assertEquals($Gson$Types.newParameterizedTypeWithOwner(null, List.class, $Gson$Types.supertypeOf(String.class)), resolvedSuper);

    Assert.assertEquals(String[].class, $Gson$Types.resolve(String[].class, String[].class, String[].class));
    Assert.assertEquals(String.class, $Gson$Types.resolve(String.class, String.class, String.class));

    CustomUnknownType custom = new CustomUnknownType();
    Assert.assertSame(custom, $Gson$Types.resolve(String.class, String.class, custom));
  }

  @Test
  public void testResolveTypeVariableUnresolvable() {
    GenericDeclaration decl = new GenericDeclaration() {
      @Override
      public TypeVariable<?>[] getTypeParameters() { return new TypeVariable<?>[0]; }
      @Override
      public java.lang.annotation.Annotation[] getAnnotations() { return new java.lang.annotation.Annotation[0]; }
      @Override
      public java.lang.annotation.Annotation[] getDeclaredAnnotations() { return new java.lang.annotation.Annotation[0]; }
      @Override
      public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationClass) { return null; }
    };
    TypeVariable<?> customTv = new CustomTypeVariable("T", decl);
    Type resolved = $Gson$Types.resolve(Object.class, Object.class, customTv);
    Assert.assertSame(customTv, resolved);

    TypeVariable<?> unresolvableTv = ParameterizedHolder.class.getTypeParameters()[0];
    Type notInContext = $Gson$Types.resolve(Object.class, Object.class, unresolvableTv);
    Assert.assertSame(unresolvableTv, notInContext);
  }

  @Test
  public void testCheckNotPrimitive() {
    $Gson$Types.checkNotPrimitive(String.class);
    $Gson$Types.checkNotPrimitive(List.class);
    try {
      $Gson$Types.checkNotPrimitive(int.class);
      Assert.fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void testEqualHelper() {
    Assert.assertTrue($Gson$Types.equal(null, null));
    Assert.assertFalse($Gson$Types.equal("a", null));
    Assert.assertFalse($Gson$Types.equal(null, "a"));
    Assert.assertTrue($Gson$Types.equal("a", "a"));
  }

  @Test
  public void testHashCodeOrZero() {
    Assert.assertEquals(0, $Gson$Types.hashCodeOrZero(null));
    Assert.assertEquals("test".hashCode(), $Gson$Types.hashCodeOrZero("test"));
  }
}
