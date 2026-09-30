package com.google.gson.internal;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ConstructorConstructorTest {

    @Test
    public void testGetWithTypeInstanceCreator() {
        Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
        final TypeToken<String> typeToken = TypeToken.get(String.class);
        creators.put(typeToken.getType(), new InstanceCreator<String>() {
            @Override
            public String createInstance(Type type) {
                return "created";
            }
        });
        ConstructorConstructor cc = new ConstructorConstructor(creators);
        ObjectConstructor<String> constructor = cc.get(typeToken);
        Assert.assertNotNull(constructor);
        Assert.assertEquals("created", constructor.construct());
    }

    @Test
    public void testGetWithRawTypeInstanceCreator() {
        Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
        final TypeToken<String> typeToken = TypeToken.get(String.class);
        creators.put(String.class, new InstanceCreator<String>() {
            @Override
            public String createInstance(Type type) {
                return "rawCreated";
            }
        });
        ConstructorConstructor cc = new ConstructorConstructor(creators);
        ObjectConstructor<String> constructor = cc.get(typeToken);
        Assert.assertNotNull(constructor);
        Assert.assertEquals("rawCreated", constructor.construct());
    }

    @Test
    public void testGetWithDefaultConstructor() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<ArrayList> constructor = cc.get(TypeToken.get(ArrayList.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof ArrayList);
    }

    @Test
    public void testGetWithNoDefaultConstructor() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<NoDefaultConstructor> constructor = cc.get(TypeToken.get(NoDefaultConstructor.class));
        Assert.assertNotNull(constructor);
        Assert.assertNotNull(constructor.construct());
    }

    @Test
    public void testGetWithCollectionSortedSet() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<SortedSet> constructor = cc.get(TypeToken.get(SortedSet.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof TreeSet);
    }

    @Test
    public void testGetWithCollectionEnumSet() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<EnumSet<SampleEnum>> typeToken = new TypeToken<EnumSet<SampleEnum>>() {};
        ObjectConstructor<EnumSet<SampleEnum>> constructor = cc.get(typeToken);
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof EnumSet);
    }

    @Test(expected = JsonIOException.class)
    public void testGetWithCollectionEnumSetInvalidType() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<EnumSet> typeToken = TypeToken.get(EnumSet.class);
        cc.get(typeToken).construct();
    }

    @Test(expected = JsonIOException.class)
    public void testGetWithCollectionEnumSetInvalidParameterizedType() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<EnumSet<Object>> typeToken = new TypeToken<EnumSet<Object>>() {};
        cc.get(typeToken).construct();
    }

    @Test
    public void testGetWithCollectionSet() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Set> constructor = cc.get(TypeToken.get(Set.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedHashSet);
    }

    @Test
    public void testGetWithCollectionQueue() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Queue> constructor = cc.get(TypeToken.get(Queue.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedList);
    }

    @Test
    public void testGetWithCollectionDefault() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Collection> constructor = cc.get(TypeToken.get(Collection.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof ArrayList);
    }

    @Test
    public void testGetWithMapSortedMap() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<SortedMap> constructor = cc.get(TypeToken.get(SortedMap.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof TreeMap);
    }

    @Test
    public void testGetWithMapParameterizedTypeNonStringKey() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Map<Integer, String>> typeToken = new TypeToken<Map<Integer, String>>() {};
        ObjectConstructor<Map<Integer, String>> constructor = cc.get(typeToken);
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedHashMap);
    }

    @Test
    public void testGetWithMapDefault() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Map> constructor = cc.get(TypeToken.get(Map.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedTreeMap);
    }

    @Test
    public void testGetWithMapStringKey() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Map<String, String>> typeToken = new TypeToken<Map<String, String>>() {};
        ObjectConstructor<Map<String, String>> constructor = cc.get(typeToken);
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedTreeMap);
    }

    @Test
    public void testGetWithUnsafeAllocator() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<PrivateConstructor> constructor = cc.get(TypeToken.get(PrivateConstructor.class));
        Assert.assertNotNull(constructor);
        Assert.assertNotNull(constructor.construct());
    }

    @Test
    public void testToString() {
        Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
        ConstructorConstructor cc = new ConstructorConstructor(creators);
        Assert.assertEquals(creators.toString(), cc.toString());
    }

    @Test
    public void testDefaultConstructorThrowsRuntimeExceptionOnInstantiationException() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<AbstractClass> constructor = cc.get(TypeToken.get(AbstractClass.class));
        try {
            constructor.construct();
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to invoke"));
        }
    }

    @Test
    public void testDefaultConstructorThrowsRuntimeExceptionOnInvocationTargetException() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<ThrowingConstructor> constructor = cc.get(TypeToken.get(ThrowingConstructor.class));
        try {
            constructor.construct();
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to invoke"));
        }
    }

    @Test
    public void testDefaultConstructorSetsAccessible() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<PackagePrivateConstructor> constructor = cc.get(TypeToken.get(PackagePrivateConstructor.class));
        Assert.assertNotNull(constructor.construct());
    }

    @Test
    public void testGetWithNullInstanceCreators() {
        ConstructorConstructor cc = new ConstructorConstructor(null);
        ObjectConstructor<String> constructor = cc.get(TypeToken.get(String.class));
        Assert.assertNotNull(constructor);
        Assert.assertNotNull(constructor.construct());
    }

    @Test
    public void testGetWithEmptyInstanceCreators() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<String> constructor = cc.get(TypeToken.get(String.class));
        Assert.assertNotNull(constructor);
        Assert.assertNotNull(constructor.construct());
    }

    @Test
    public void testGetWithCollectionConcreteType() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<ArrayList> constructor = cc.get(TypeToken.get(ArrayList.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof ArrayList);
    }

    @Test
    public void testGetWithMapConcreteType() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<HashMap> constructor = cc.get(TypeToken.get(HashMap.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof HashMap);
    }

    @Test
    public void testGetWithEnumSetRawType() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<EnumSet> typeToken = TypeToken.get(EnumSet.class);
        try {
            cc.get(typeToken).construct();
            Assert.fail("Expected JsonIOException");
        } catch (JsonIOException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid EnumSet type"));
        }
    }

    @Test
    public void testGetWithEnumSetNonClassParameterizedType() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<EnumSet<Object>> typeToken = new TypeToken<EnumSet<Object>>() {};
        try {
            cc.get(typeToken).construct();
            Assert.fail("Expected JsonIOException");
        } catch (JsonIOException e) {
            Assert.assertTrue(e.getMessage().contains("Invalid EnumSet type"));
        }
    }

    @Test
    public void testGetWithMapNonParameterizedType() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Map> constructor = cc.get(TypeToken.get(Map.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedTreeMap);
    }

    @Test
    public void testGetWithMapStringKeyParameterizedType() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Map<String, Object>> typeToken = new TypeToken<Map<String, Object>>() {};
        ObjectConstructor<Map<String, Object>> constructor = cc.get(typeToken);
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedTreeMap);
    }

    @Test
    public void testGetWithMapNonStringKeyParameterizedType() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Map<Integer, Object>> typeToken = new TypeToken<Map<Integer, Object>>() {};
        ObjectConstructor<Map<Integer, Object>> constructor = cc.get(typeToken);
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedHashMap);
    }

    @Test
    public void testGetWithSortedMap() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<SortedMap> constructor = cc.get(TypeToken.get(SortedMap.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof TreeMap);
    }

    @Test
    public void testGetWithSortedSet() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<SortedSet> constructor = cc.get(TypeToken.get(SortedSet.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof TreeSet);
    }

    @Test
    public void testGetWithQueue() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Queue> constructor = cc.get(TypeToken.get(Queue.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedList);
    }

    @Test
    public void testGetWithSet() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Set> constructor = cc.get(TypeToken.get(Set.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof LinkedHashSet);
    }

    @Test
    public void testGetWithCollection() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<Collection> constructor = cc.get(TypeToken.get(Collection.class));
        Assert.assertNotNull(constructor);
        Assert.assertTrue(constructor.construct() instanceof ArrayList);
    }

    @Test
    public void testGetWithUnsafeAllocatorException() {
        ConstructorConstructor cc = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
        ObjectConstructor<UnsafeAllocatorExceptionClass> constructor = cc.get(TypeToken.get(UnsafeAllocatorExceptionClass.class));
        try {
            constructor.construct();
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Unable to invoke no-args constructor"));
        }
    }

    // Helper classes for testing

    static class NoDefaultConstructor {
        public NoDefaultConstructor(String s) {}
    }

    static class PrivateConstructor {
        private PrivateConstructor() {}
    }

    static abstract class AbstractClass {
        public AbstractClass() {}
    }

    static class ThrowingConstructor {
        public ThrowingConstructor() {
            throw new UnsupportedOperationException();
        }
    }

    static class PackagePrivateConstructor {
        PackagePrivateConstructor() {}
    }

    static class UnsafeAllocatorExceptionClass {
        public UnsafeAllocatorExceptionClass() {
            throw new RuntimeException("Constructor failed");
        }
    }

    enum SampleEnum {
        A, B, C
    }
}
