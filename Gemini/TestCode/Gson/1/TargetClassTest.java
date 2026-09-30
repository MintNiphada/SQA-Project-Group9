package com.google.gson;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class TypeInfoFactoryTest {

  private static class SimpleClass {
    public int intField;
    public String stringField;
    public int[] intArrayField;
    public String[] stringArrayField;
  }

  private static class GenericClass<T, E> {
    public T fieldT;
    public E fieldE;
    public List<T> listT;
    public Map<T, E> mapTE;
    public T[] arrayT;
    public List<T>[] arrayOfListT;
  }

  private static class WildcardClass {
    public List<? extends Number> wildcardField;
  }

  private static class NestedGenericClass<T> {
    public GenericClass<T, String> nestedField;
  }

  private static class CustomType implements Type {
  }

  @Test
  public void testPrivateConstructor() throws Exception {
    Constructor<TypeInfoFactory> constructor = TypeInfoFactory.class.getDeclaredConstructor();
    Assert.assertTrue(!constructor.isAccessible());
    constructor.setAccessible(true);
    TypeInfoFactory factory = constructor.newInstance();
    Assert.assertNotNull(factory);
  }

  @Test
  public void testGetTypeInfoForArrayValid() {
    TypeInfoArray info1 = TypeInfoFactory.getTypeInfoForArray(int[].class);
    Assert.assertNotNull(info1);
    Assert.assertEquals(int[].class, info1.getType());

    Type genericArrayType = new TypeToken<List<String>[]>() {}.getType();
    TypeInfoArray info2 = TypeInfoFactory.getTypeInfoForArray(genericArrayType);
    Assert.assertNotNull(info2);
    Assert.assertEquals(genericArrayType, info2.getType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetTypeInfoForArrayInvalidNonArray() {
    TypeInfoFactory.getTypeInfoForArray(String.class);
  }

  @Test
  public void testFieldSimpleTypes() throws Exception {
    Field intField = SimpleClass.class.getField("intField");
    TypeInfo intInfo = TypeInfoFactory.getTypeInfoForField(intField, SimpleClass.class);
    Assert.assertEquals(int.class, intInfo.getActualType());

    Field stringField = SimpleClass.class.getField("stringField");
    TypeInfo stringInfo = TypeInfoFactory.getTypeInfoForField(stringField, SimpleClass.class);
    Assert.assertEquals(String.class, stringInfo.getActualType());

    Field intArrayField = SimpleClass.class.getField("intArrayField");
    TypeInfo intArrayInfo = TypeInfoFactory.getTypeInfoForField(intArrayField, SimpleClass.class);
    Assert.assertEquals(int[].class, intArrayInfo.getActualType());
  }

  @Test
  public void testFieldTypeVariables() throws Exception {
    Type parameterizedType = new TypeToken<GenericClass<Integer, String>>() {}.getType();

    Field fieldT = GenericClass.class.getField("fieldT");
    TypeInfo infoT = TypeInfoFactory.getTypeInfoForField(fieldT, parameterizedType);
    Assert.assertEquals(Integer.class, infoT.getActualType());

    Field fieldE = GenericClass.class.getField("fieldE");
    TypeInfo infoE = TypeInfoFactory.getTypeInfoForField(fieldE, parameterizedType);
    Assert.assertEquals(String.class, infoE.getActualType());
  }

  @Test
  public void testFieldParameterizedType() throws Exception {
    Type parameterizedType = new TypeToken<GenericClass<Integer, String>>() {}.getType();

    Field listField = GenericClass.class.getField("listT");
    TypeInfo listInfo = TypeInfoFactory.getTypeInfoForField(listField, parameterizedType);
    Assert.assertTrue(listInfo.getActualType() instanceof ParameterizedType);
    ParameterizedType ptList = (ParameterizedType) listInfo.getActualType();
    Assert.assertEquals(List.class, ptList.getRawType());
    Assert.assertEquals(1, ptList.getActualTypeArguments().length);
    Assert.assertEquals(Integer.class, ptList.getActualTypeArguments()[0]);

    Field mapField = GenericClass.class.getField("mapTE");
    TypeInfo mapInfo = TypeInfoFactory.getTypeInfoForField(mapField, parameterizedType);
    Assert.assertTrue(mapInfo.getActualType() instanceof ParameterizedType);
    ParameterizedType ptMap = (ParameterizedType) mapInfo.getActualType();
    Assert.assertEquals(Map.class, ptMap.getRawType());
    Assert.assertEquals(2, ptMap.getActualTypeArguments().length);
    Assert.assertEquals(Integer.class, ptMap.getActualTypeArguments()[0]);
    Assert.assertEquals(String.class, ptMap.getActualTypeArguments()[1]);
  }

  @Test
  public void testFieldGenericArrayType() throws Exception {
    Type parameterizedType = new TypeToken<GenericClass<Integer, String>>() {}.getType();

    Field arrayTField = GenericClass.class.getField("arrayT");
    TypeInfo arrayTInfo = TypeInfoFactory.getTypeInfoForField(arrayTField, parameterizedType);
    Assert.assertEquals(Integer[].class, arrayTInfo.getActualType());

    Field arrayOfListTField = GenericClass.class.getField("arrayOfListT");
    TypeInfo arrayOfListTInfo = TypeInfoFactory.getTypeInfoForField(arrayOfListTField, parameterizedType);
    Assert.assertTrue(arrayOfListTInfo.getActualType() instanceof GenericArrayType);
    GenericArrayType gat = (GenericArrayType) arrayOfListTInfo.getActualType();
    Assert.assertTrue(gat.getGenericComponentType() instanceof ParameterizedType);
  }

  @Test
  public void testFieldWildcardType() throws Exception {
    Field wildcardField = WildcardClass.class.getField("wildcardField");
    TypeInfo wildcardInfo = TypeInfoFactory.getTypeInfoForField(wildcardField, WildcardClass.class);
    Assert.assertTrue(wildcardInfo.getActualType() instanceof ParameterizedType);
    ParameterizedType pt = (ParameterizedType) wildcardInfo.getActualType();
    Assert.assertEquals(Number.class, pt.getActualTypeArguments()[0]);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testFieldTypeVariableWithoutParameterizedParent() throws Exception {
    Field fieldT = GenericClass.class.getField("fieldT");
    TypeInfoFactory.getTypeInfoForField(fieldT, GenericClass.class);
  }

  @Test
  public void testNestedGenericField() throws Exception {
    Type parameterizedType = new TypeToken<NestedGenericClass<Double>>() {}.getType();
    Field nestedField = NestedGenericClass.class.getField("nestedField");
    TypeInfo nestedInfo = TypeInfoFactory.getTypeInfoForField(nestedField, parameterizedType);
    Assert.assertTrue(nestedInfo.getActualType() instanceof ParameterizedType);
    ParameterizedType pt = (ParameterizedType) nestedInfo.getActualType();
    Assert.assertEquals(GenericClass.class, pt.getRawType());
    Assert.assertEquals(Double.class, pt.getActualTypeArguments()[0]);
    Assert.assertEquals(String.class, pt.getActualTypeArguments()[1]);
  }

  @Test
  public void testGenericArrayTypeComponentTypeMatchesActual() throws Exception {
    Type genericArrayType = new TypeToken<List<String>[]>() {}.getType();
    TypeInfoArray info = TypeInfoFactory.getTypeInfoForArray(genericArrayType);
    Assert.assertEquals(genericArrayType, info.getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testTypeVariableMismatchThrowsIllegalStateException() throws Exception {
    Field fieldT = GenericClass.class.getField("fieldT");
    ParameterizedType fakeParentType = new ParameterizedType() {
      public Type[] getActualTypeArguments() {
        return new Type[] { String.class };
      }

      public Type getRawType() {
        return SimpleClass.class;
      }

      public Type getOwnerType() {
        return null;
      }
    };
    TypeInfoFactory.getTypeInfoForField(fieldT, fakeParentType);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUnknownTypeThrowsIllegalArgumentException() throws Exception {
    Field intField = SimpleClass.class.getField("intField");
    ParameterizedType fakeParentType = new ParameterizedType() {
      public Type[] getActualTypeArguments() {
        return new Type[] { new CustomType() };
      }

      public Type getRawType() {
        return GenericClass.class;
      }

      public Type getOwnerType() {
        return null;
      }
    };
    Field fieldT = GenericClass.class.getField("fieldT");
    TypeInfoFactory.getTypeInfoForField(fieldT, fakeParentType);
  }
}