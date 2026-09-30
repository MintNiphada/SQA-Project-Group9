package com.google.gson.internal;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

public class ConstructorConstructorTest {

  private enum SampleEnum {
    FOO, BAR
  }

  private static class PrivateConstructorClass {
    private final int value;

    private PrivateConstructorClass() {
      this.value = 42;
    }
  }

  private static class ThrowingConstructorClass {
    public ThrowingConstructorClass() {
      throw new IllegalArgumentException("Constructor failure");
    }
  }

  private static abstract class AbstractClassWithConstructor {
    public AbstractClassWithConstructor() {}
  }

  private static class ClassWithoutNoArgConstructor {
    final int value;

    public ClassWithoutNoArgConstructor(int value) {
      this.value = value;
    }
  }

  @Test
  public void testExactTypeInstanceCreator() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    final String expected = "custom-string";
    creators.put(String.class, new InstanceCreator<String>() {
      @Override public String createInstance(Type type) {
        return expected;
      }
    });

    ConstructorConstructor constructor = new ConstructorConstructor(creators);
    ObjectConstructor<String> objectConstructor = constructor.get(TypeToken.get(String.class));
    Assert.assertNotNull(objectConstructor);
    Assert.assertEquals(expected, objectConstructor.construct());
  }

  @Test
  public void testRawTypeInstanceCreator() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    final List<String> customList = new LinkedList<String>();
    creators.put(List.class, new InstanceCreator<List<?>>() {
      @Override public List<?> createInstance(Type type) {
        return customList;
      }
    });

    ConstructorConstructor constructor = new ConstructorConstructor(creators);
    TypeToken<List<String>> typeToken = new TypeToken<List<String>>() {};
    ObjectConstructor<List<String>> objectConstructor = constructor.get(typeToken);
    Assert.assertNotNull(objectConstructor);
    Assert.assertSame(customList, objectConstructor.construct());
  }

  @Test
  public void testDefaultConstructor() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    ObjectConstructor<PrivateConstructorClass> objectConstructor =
        constructor.get(TypeToken.get(PrivateConstructorClass.class));
    Assert.assertNotNull(objectConstructor);
    PrivateConstructorClass instance = objectConstructor.construct();
    Assert.assertNotNull(instance);
    Assert.assertEquals(42, instance.value);
  }

  @Test
  public void testDefaultConstructorInstantiationException() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    ObjectConstructor<AbstractClassWithConstructor> objectConstructor =
        constructor.get(TypeToken.get(AbstractClassWithConstructor.class));
    Assert.assertNotNull(objectConstructor);
    try {
      objectConstructor.construct();
      Assert.fail("Expected RuntimeException on instantiating abstract class with default constructor");
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getMessage().contains("Failed to invoke"));
      Assert.assertTrue(e.getCause() instanceof InstantiationException);
    }
  }

  @Test
  public void testDefaultConstructorInvocationTargetException() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    ObjectConstructor<ThrowingConstructorClass> objectConstructor =
        constructor.get(TypeToken.get(ThrowingConstructorClass.class));
    Assert.assertNotNull(objectConstructor);
    try {
      objectConstructor.construct();
      Assert.fail("Expected RuntimeException when constructor throws");
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getMessage().contains("Failed to invoke"));
      Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
      Assert.assertEquals("Constructor failure", e.getCause().getMessage());
    }
  }

  @Test
  public void testDefaultImplementationSortedSet() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<SortedSet<String>> typeToken = new TypeToken<SortedSet<String>>() {};
    ObjectConstructor<SortedSet<String>> objectConstructor = constructor.get(typeToken);
    SortedSet<String> instance = objectConstructor.construct();
    Assert.assertTrue(instance instanceof TreeSet);
  }

  @Test
  public void testDefaultImplementationEnumSet() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<EnumSet<SampleEnum>> typeToken = new TypeToken<EnumSet<SampleEnum>>() {};
    ObjectConstructor<EnumSet<SampleEnum>> objectConstructor = constructor.get(typeToken);
    EnumSet<SampleEnum> instance = objectConstructor.construct();
    Assert.assertNotNull(instance);
    Assert.assertTrue(instance.isEmpty());
  }

  @Test
  public void testDefaultImplementationRawEnumSetThrowsJsonIOException() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    @SuppressWarnings("rawtypes")
    TypeToken<EnumSet> typeToken = TypeToken.get(EnumSet.class);
    @SuppressWarnings("unchecked")
    ObjectConstructor<EnumSet<?>> objectConstructor = (ObjectConstructor<EnumSet<?>>) constructor.get(typeToken);
    try {
      objectConstructor.construct();
      Assert.fail("Expected JsonIOException for raw EnumSet type");
    } catch (JsonIOException expected) {
      Assert.assertTrue(expected.getMessage().contains("Invalid EnumSet type"));
    }
  }

  @Test
  public void testDefaultImplementationEnumSetWithNonClassTypeArgumentThrowsJsonIOException() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<?> typeToken = new TypeToken<EnumSet<? extends SampleEnum>>() {};
    @SuppressWarnings("unchecked")
    ObjectConstructor<Object> objectConstructor = (ObjectConstructor<Object>) constructor.get(typeToken);
    try {
      objectConstructor.construct();
      Assert.fail("Expected JsonIOException for non-Class EnumSet type argument");
    } catch (JsonIOException expected) {
      Assert.assertTrue(expected.getMessage().contains("Invalid EnumSet type"));
    }
  }

  @Test
  public void testDefaultImplementationSet() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<Set<String>> typeToken = new TypeToken<Set<String>>() {};
    ObjectConstructor<Set<String>> objectConstructor = constructor.get(typeToken);
    Set<String> instance = objectConstructor.construct();
    Assert.assertTrue(instance instanceof LinkedHashSet);
  }

  @Test
  public void testDefaultImplementationQueue() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<Queue<String>> typeToken = new TypeToken<Queue<String>>() {};
    ObjectConstructor<Queue<String>> objectConstructor = constructor.get(typeToken);
    Queue<String> instance = objectConstructor.construct();
    Assert.assertTrue(instance instanceof LinkedList);
  }

  @Test
  public void testDefaultImplementationListAndCollection() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());

    TypeToken<List<String>> listToken = new TypeToken<List<String>>() {};
    ObjectConstructor<List<String>> listConstructor = constructor.get(listToken);
    Assert.assertTrue(listConstructor.construct() instanceof java.util.ArrayList);

    TypeToken<Collection<String>> collectionToken = new TypeToken<Collection<String>>() {};
    ObjectConstructor<Collection<String>> collectionConstructor = constructor.get(collectionToken);
    Assert.assertTrue(collectionConstructor.construct() instanceof java.util.ArrayList);
  }

  @Test
  public void testDefaultImplementationSortedMap() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<SortedMap<String, Object>> typeToken = new TypeToken<SortedMap<String, Object>>() {};
    ObjectConstructor<SortedMap<String, Object>> objectConstructor = constructor.get(typeToken);
    SortedMap<String, Object> instance = objectConstructor.construct();
    Assert.assertTrue(instance instanceof TreeMap);
  }

  @Test
  public void testDefaultImplementationMapNonStringKey() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<Map<Integer, Object>> typeToken = new TypeToken<Map<Integer, Object>>() {};
    ObjectConstructor<Map<Integer, Object>> objectConstructor = constructor.get(typeToken);
    Map<Integer, Object> instance = objectConstructor.construct();
    Assert.assertTrue(instance instanceof LinkedHashMap);
  }

  @Test
  public void testDefaultImplementationMapStringKey() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {};
    ObjectConstructor<Map<String, Object>> objectConstructor = constructor.get(typeToken);
    Map<String, Object> instance = objectConstructor.construct();
    Assert.assertTrue(instance instanceof LinkedTreeMap);
  }

  @Test
  public void testDefaultImplementationRawMap() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    @SuppressWarnings("rawtypes")
    TypeToken<Map> typeToken = TypeToken.get(Map.class);
    @SuppressWarnings("unchecked")
    ObjectConstructor<Map<?, ?>> objectConstructor = (ObjectConstructor<Map<?, ?>>) constructor.get(typeToken);
    Map<?, ?> instance = objectConstructor.construct();
    Assert.assertTrue(instance instanceof LinkedTreeMap);
  }

  @Test
  public void testUnsafeAllocatorSuccess() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<ClassWithoutNoArgConstructor> typeToken = TypeToken.get(ClassWithoutNoArgConstructor.class);
    ObjectConstructor<ClassWithoutNoArgConstructor> objectConstructor = constructor.get(typeToken);
    ClassWithoutNoArgConstructor instance = objectConstructor.construct();
    Assert.assertNotNull(instance);
    Assert.assertEquals(0, instance.value);
  }

  @Test
  public void testUnsafeAllocatorFailureOnInterface() {
    ConstructorConstructor constructor = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<CharSequence> typeToken = TypeToken.get(CharSequence.class);
    ObjectConstructor<CharSequence> objectConstructor = constructor.get(typeToken);
    try {
      objectConstructor.construct();
      Assert.fail("Expected RuntimeException when unsafe allocation fails on an interface");
    } catch (RuntimeException e) {
      Assert.assertTrue(e.getMessage().contains("Unable to invoke no-args constructor"));
    }
  }

  @Test
  public void testToString() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    creators.put(String.class, new InstanceCreator<String>() {
      @Override public String createInstance(Type type) {
        return "";
      }
    });
    ConstructorConstructor constructor = new ConstructorConstructor(creators);
    Assert.assertEquals(creators.toString(), constructor.toString());
  }
}
