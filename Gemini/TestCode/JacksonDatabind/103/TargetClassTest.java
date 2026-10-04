package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DeserializerCacheTest {

    static enum TestEnum { A, B }

    static class SimpleBean {
        public int x;
    }

    static abstract class AbstractBase {
        public int id;
    }

    static class SubClass extends AbstractBase {
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class ObjectMap extends HashMap<String, String> {
        public String extra;
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class ObjectList extends ArrayList<String> {
        public String extra;
    }

    static class CustomDeserializer extends StdDeserializer<SimpleBean> {
        public CustomDeserializer() {
            super(SimpleBean.class);
        }

        @Override
        public SimpleBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
            return null;
        }
    }

    @JsonDeserialize(using = CustomDeserializer.class)
    static class BeanWithCustomDeser {
        public int a;
    }

    static class StringToBeanConverter extends StdConverter<String, SimpleBean> {
        @Override
        public SimpleBean convert(String value) {
            SimpleBean bean = new SimpleBean();
            bean.x = 42;
            return bean;
        }
    }

    @JsonDeserialize(converter = StringToBeanConverter.class)
    static class BeanWithConverter {
    }

    @JsonDeserialize(builder = BeanWithBuilder.Builder.class)
    static class BeanWithBuilder {
        final int value;

        BeanWithBuilder(int v) {
            this.value = v;
        }

        @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
        static class Builder {
            int value;

            public Builder withValue(int v) {
                this.value = v;
                return this;
            }

            public BeanWithBuilder build() {
                return new BeanWithBuilder(value);
            }
        }
    }

    static class NodeHolder {
        public JsonNode node;
    }

    static class SelfReferential {
        public SelfReferential next;
    }

    static class BadBean {
        public BadBean(String a, int b) {
        }
    }

    @Test
    public void testCacheLifecycleAndBasicStats() {
        DeserializerCache cache = new DeserializerCache();
        Assert.assertEquals(0, cache.cachedDeserializersCount());

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.getTypeFactory().constructType(SimpleBean.class);

        JsonDeserializer<Object> deser1 = mapper.getDeserializationContext().findRootValueDeserializer(type);
        Assert.assertNotNull(deser1);

        cache._cachedDeserializers.put(type, deser1);
        Assert.assertEquals(1, cache.cachedDeserializersCount());

        cache.flushCachedDeserializers();
        Assert.assertEquals(0, cache.cachedDeserializersCount());
    }

    @Test
    public void testSerializationWriteReplace() throws Exception {
        DeserializerCache cache = new DeserializerCache();
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructType(SimpleBean.class);

        cache._incompleteDeserializers.put(type, null);
        Assert.assertEquals(1, cache._incompleteDeserializers.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cache);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DeserializerCache readCache = (DeserializerCache) ois.readObject();
        ois.close();

        Assert.assertNotNull(readCache);
        Assert.assertEquals(0, readCache._incompleteDeserializers.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializerNullType() {
        DeserializerCache cache = new DeserializerCache();
        cache._findCachedDeserializer(null);
    }

    @Test
    public void testFindValueDeserializerSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();
        JavaType type = mapper.getTypeFactory().constructType(SimpleBean.class);

        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, ctxt.getFactory(), type);
        Assert.assertNotNull(deser);
        Assert.assertEquals(1, cache.cachedDeserializersCount());

        JsonDeserializer<Object> deserCached = cache.findValueDeserializer(ctxt, ctxt.getFactory(), type);
        Assert.assertSame(deser, deserCached);
    }

    @Test
    public void testFindValueDeserializerEnum() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();
        JavaType type = mapper.getTypeFactory().constructType(TestEnum.class);

        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, ctxt.getFactory(), type);
        Assert.assertNotNull(deser);
        Assert.assertTrue(cache.hasValueDeserializerFor(ctxt, ctxt.getFactory(), type));
    }

    @Test
    public void testFindValueDeserializerArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();
        JavaType type = mapper.getTypeFactory().constructType(SimpleBean[].class);

        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, ctxt.getFactory(), type);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testFindValueDeserializerMapAndMapLike() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType mapType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Integer.class);
        JsonDeserializer<Object> deserMap = cache.findValueDeserializer(ctxt, ctxt.getFactory(), mapType);
        Assert.assertNotNull(deserMap);

        JavaType objMapType = mapper.getTypeFactory().constructType(ObjectMap.class);
        JsonDeserializer<Object> deserObjMap = cache.findValueDeserializer(ctxt, ctxt.getFactory(), objMapType);
        Assert.assertNotNull(deserObjMap);
    }

    @Test
    public void testFindValueDeserializerCollectionAndCollectionLike() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<Object> deserList = cache.findValueDeserializer(ctxt, ctxt.getFactory(), listType);
        Assert.assertNotNull(deserList);

        JavaType objListType = mapper.getTypeFactory().constructType(ObjectList.class);
        JsonDeserializer<Object> deserObjList = cache.findValueDeserializer(ctxt, ctxt.getFactory(), objListType);
        Assert.assertNotNull(deserObjList);
    }

    @Test
    public void testFindValueDeserializerReferenceType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType refType = mapper.getTypeFactory().constructType(AtomicReference.class);
        JsonDeserializer<Object> deserRef = cache.findValueDeserializer(ctxt, ctxt.getFactory(), refType);
        Assert.assertNotNull(deserRef);
    }

    @Test
    public void testFindValueDeserializerJsonNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType nodeType = mapper.getTypeFactory().constructType(JsonNode.class);
        JsonDeserializer<Object> deserNode = cache.findValueDeserializer(ctxt, ctxt.getFactory(), nodeType);
        Assert.assertNotNull(deserNode);
    }

    @Test
    public void testFindValueDeserializerBuilder() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType type = mapper.getTypeFactory().constructType(BeanWithBuilder.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, ctxt.getFactory(), type);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testFindValueDeserializerConverter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType type = mapper.getTypeFactory().constructType(BeanWithConverter.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, ctxt.getFactory(), type);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testFindValueDeserializerAnnotated() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType type = mapper.getTypeFactory().constructType(BeanWithCustomDeser.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, ctxt.getFactory(), type);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof CustomDeserializer);
    }

    @Test
    public void testCyclicDependencyResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType type = mapper.getTypeFactory().constructType(SelfReferential.class);
        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, ctxt.getFactory(), type);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testKeyDeserializerResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType stringType = mapper.getTypeFactory().constructType(String.class);
        KeyDeserializer kd = cache.findKeyDeserializer(ctxt, ctxt.getFactory(), stringType);
        Assert.assertNotNull(kd);
    }

    @Test
    public void testCustomHandlersBypassCaching() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();

        JavaType elemType = mapper.getTypeFactory().constructType(String.class).withValueHandler(new CustomDeserializer());
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, elemType);

        JsonDeserializer<Object> deser = cache.findValueDeserializer(ctxt, ctxt.getFactory(), listType);
        Assert.assertNotNull(deser);
        Assert.assertEquals(0, cache.cachedDeserializersCount());

        JavaType keyType = mapper.getTypeFactory().constructType(String.class).withValueHandler(new Object());
        JavaType mapType = mapper.getTypeFactory().constructMapType(Map.class, keyType, mapper.getTypeFactory().constructType(String.class));

        JsonDeserializer<Object> deserMap = cache.findValueDeserializer(ctxt, ctxt.getFactory(), mapType);
        Assert.assertNotNull(deserMap);
        Assert.assertEquals(0, cache.cachedDeserializersCount());
    }

    @Test
    public void testUnknownValueDeserializerAbstractType() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();
        JavaType type = mapper.getTypeFactory().constructType(AbstractBase.class);

        try {
            cache._handleUnknownValueDeserializer(ctxt, type);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot find a Value deserializer for abstract type"));
        }
    }

    @Test
    public void testUnknownKeyDeserializer() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DeserializerCache cache = new DeserializerCache();
        JavaType type = mapper.getTypeFactory().constructType(BadBean.class);

        try {
            cache.findKeyDeserializer(ctxt, ctxt.getFactory(), type);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot find a (Map) Key deserializer for type"));
        }
    }
}
