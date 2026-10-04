package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class JavaUtilCollectionsDeserializersTest {

    @Test
    public void testFindForCollectionArraysAsList() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Class<?> rawClass = Arrays.asList("a", "b").getClass();
        JavaType type = tf.constructType(rawClass);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof StdDelegatingDeserializer);

        StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
        Converter<Object, Object> conv = sdd.getConverter();
        Assert.assertNotNull(conv);

        Assert.assertNull(conv.convert(null));

        List<String> input = new ArrayList<String>(Arrays.asList("x", "y"));
        Object result = conv.convert(input);
        Assert.assertSame(input, result);

        Assert.assertEquals(type.findSuperType(List.class), conv.getInputType(tf));
        Assert.assertEquals(type.findSuperType(List.class), conv.getOutputType(tf));
    }

    @Test
    public void testFindForCollectionSingletonList() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Class<?> rawClass = Collections.singletonList("a").getClass();
        JavaType type = tf.constructType(rawClass);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Assert.assertNotNull(deser);
        StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
        Converter<Object, Object> conv = sdd.getConverter();

        Assert.assertNull(conv.convert(null));

        List<String> input = Arrays.asList("hello");
        Object result = conv.convert(input);
        Assert.assertEquals(Collections.singletonList("hello"), result);
        Assert.assertEquals(Collections.singletonList("hello").getClass(), result.getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingletonListInvalidSizeZero() {
        JavaType type = TypeFactory.defaultInstance().constructType(Collections.singletonList("a").getClass());
        Converter<Object, Object> conv = JavaUtilCollectionsDeserializers.converter(2, type, List.class);
        conv.convert(Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingletonListInvalidSizeTwo() {
        JavaType type = TypeFactory.defaultInstance().constructType(Collections.singletonList("a").getClass());
        Converter<Object, Object> conv = JavaUtilCollectionsDeserializers.converter(2, type, List.class);
        conv.convert(Arrays.asList("a", "b"));
    }

    @Test
    public void testFindForCollectionSingletonSet() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Class<?> rawClass = Collections.singleton("a").getClass();
        JavaType type = tf.constructType(rawClass);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Assert.assertNotNull(deser);
        StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
        Converter<Object, Object> conv = sdd.getConverter();

        Assert.assertNull(conv.convert(null));

        Set<String> input = new HashSet<String>(Collections.singletonList("world"));
        Object result = conv.convert(input);
        Assert.assertEquals(Collections.singleton("world"), result);
        Assert.assertEquals(Collections.singleton("world").getClass(), result.getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingletonSetInvalidSizeZero() {
        JavaType type = TypeFactory.defaultInstance().constructType(Collections.singleton("a").getClass());
        Converter<Object, Object> conv = JavaUtilCollectionsDeserializers.converter(1, type, Set.class);
        conv.convert(Collections.emptySet());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingletonSetInvalidSizeTwo() {
        JavaType type = TypeFactory.defaultInstance().constructType(Collections.singleton("a").getClass());
        Converter<Object, Object> conv = JavaUtilCollectionsDeserializers.converter(1, type, Set.class);
        Set<String> set = new HashSet<String>(Arrays.asList("1", "2"));
        conv.convert(set);
    }

    @Test
    public void testFindForCollectionUnmodifiableList() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Class<?> rawClass = Collections.unmodifiableList(new ArrayList<String>()).getClass();
        JavaType type = tf.constructType(rawClass);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Assert.assertNotNull(deser);
        StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
        Converter<Object, Object> conv = sdd.getConverter();

        Assert.assertNull(conv.convert(null));

        List<String> input = new ArrayList<String>(Arrays.asList("a", "b"));
        Object result = conv.convert(input);
        Assert.assertEquals(Collections.unmodifiableList(input), result);
        Assert.assertEquals(Collections.unmodifiableList(input).getClass(), result.getClass());
    }

    @Test
    public void testFindForCollectionUnmodifiableSet() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Class<?> rawClass = Collections.unmodifiableSet(new HashSet<String>()).getClass();
        JavaType type = tf.constructType(rawClass);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Assert.assertNotNull(deser);
        StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
        Converter<Object, Object> conv = sdd.getConverter();

        Assert.assertNull(conv.convert(null));

        Set<String> input = new HashSet<String>(Arrays.asList("a", "b"));
        Object result = conv.convert(input);
        Assert.assertEquals(Collections.unmodifiableSet(input), result);
        Assert.assertEquals(Collections.unmodifiableSet(input).getClass(), result.getClass());
    }

    @Test
    public void testFindForCollectionUnknown() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(ArrayList.class);
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForCollection(null, type);
        Assert.assertNull(deser);
    }

    @Test
    public void testFindForMapSingletonMap() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Class<?> rawClass = Collections.singletonMap("k", "v").getClass();
        JavaType type = tf.constructType(rawClass);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        Assert.assertNotNull(deser);
        StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
        Converter<Object, Object> conv = sdd.getConverter();

        Assert.assertNull(conv.convert(null));

        Map<String, String> input = new HashMap<String, String>();
        input.put("key1", "val1");
        Object result = conv.convert(input);
        Assert.assertEquals(Collections.singletonMap("key1", "val1"), result);
        Assert.assertEquals(Collections.singletonMap("key1", "val1").getClass(), result.getClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingletonMapInvalidSizeZero() {
        JavaType type = TypeFactory.defaultInstance().constructType(Collections.singletonMap("k", "v").getClass());
        Converter<Object, Object> conv = JavaUtilCollectionsDeserializers.converter(3, type, Map.class);
        conv.convert(Collections.emptyMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingletonMapInvalidSizeTwo() {
        JavaType type = TypeFactory.defaultInstance().constructType(Collections.singletonMap("k", "v").getClass());
        Converter<Object, Object> conv = JavaUtilCollectionsDeserializers.converter(3, type, Map.class);
        Map<String, String> map = new HashMap<String, String>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        conv.convert(map);
    }

    @Test
    public void testFindForMapUnmodifiableMap() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        Class<?> rawClass = Collections.unmodifiableMap(new HashMap<String, String>()).getClass();
        JavaType type = tf.constructType(rawClass);

        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        Assert.assertNotNull(deser);
        StdDelegatingDeserializer<?> sdd = (StdDelegatingDeserializer<?>) deser;
        Converter<Object, Object> conv = sdd.getConverter();

        Assert.assertNull(conv.convert(null));

        Map<String, String> input = new HashMap<String, String>();
        input.put("k1", "v1");
        input.put("k2", "v2");
        Object result = conv.convert(input);
        Assert.assertEquals(Collections.unmodifiableMap(input), result);
        Assert.assertEquals(Collections.unmodifiableMap(input).getClass(), result.getClass());
    }

    @Test
    public void testFindForMapUnknown() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(HashMap.class);
        JsonDeserializer<?> deser = JavaUtilCollectionsDeserializers.findForMap(null, type);
        Assert.assertNull(deser);
    }

    @Test
    public void testConverterDefaultKind() {
        JavaType type = TypeFactory.defaultInstance().constructType(ArrayList.class);
        Converter<Object, Object> conv = JavaUtilCollectionsDeserializers.converter(999, type, List.class);
        String val = "custom";
        Assert.assertSame(val, conv.convert(val));
    }
}
