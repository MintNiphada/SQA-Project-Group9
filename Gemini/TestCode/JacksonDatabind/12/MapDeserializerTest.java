package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MapDeserializerTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    public static class CustomKey {
        private final String key;

        public CustomKey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomKey customKey = (CustomKey) o;
            return Objects.equals(key, customKey.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }

    public static class CustomKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return new CustomKey("custom:" + key);
        }
    }

    public static class ContextualCustomKeyDeserializer extends KeyDeserializer implements ContextualKeyDeserializer {
        private final String prefix;

        public ContextualCustomKeyDeserializer() {
            this("ctx:");
        }

        public ContextualCustomKeyDeserializer(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return prefix + key;
        }

        @Override
        public KeyDeserializer createContextual(DeserializationContext ctxt, BeanProperty property) {
            return new ContextualCustomKeyDeserializer("propCtx:");
        }
    }

    public static class BeanWithIgnoredMapProp {
        @JsonIgnoreProperties({"ignoredKey1", "ignoredKey2"})
        public Map<String, String> map;
    }

    public static class CustomMapWithCreator extends HashMap<String, Object> {
        private final String creatorParam;

        @JsonCreator
        public CustomMapWithCreator(@JsonProperty("creatorParam") String creatorParam) {
            this.creatorParam = creatorParam;
        }

        public String getCreatorParam() {
            return creatorParam;
        }
    }

    public static class CustomMapFromString extends HashMap<String, Object> {
        public CustomMapFromString(String value) {
            put("fromStr", value);
        }
    }

    public static class EntityWithId {
        public int id;
        public String name;

        public EntityWithId() {}

        public EntityWithId(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static class MapWithPolymorphicValues {
        public Map<String, Object> data;
    }

    @Test
    public void testBasicStringMapDeserialization() throws Exception {
        String json = "{\"a\":\"1\",\"b\":\"2\"}";
        JavaType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);
        Map<String, String> result = mapper.readValue(json, type);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("1", result.get("a"));
        Assert.assertEquals("2", result.get("b"));
    }

    @Test
    public void testBasicObjectMapDeserialization() throws Exception {
        String json = "{\"num\":123,\"flag\":true,\"nil\":null}";
        JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, Object.class, Object.class);
        Map<Object, Object> result = mapper.readValue(json, type);

        Assert.assertNotNull(result);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals(123, result.get("num"));
        Assert.assertEquals(Boolean.TRUE, result.get("flag"));
        Assert.assertNull(result.get("nil"));
    }

    @Test
    public void testCustomKeyDeserializer() throws Exception {
        com.fasterxml.jackson.databind.module.SimpleModule module =
                new com.fasterxml.jackson.databind.module.SimpleModule();
        module.addKeyDeserializer(CustomKey.class, new CustomKeyDeserializer());
        mapper.registerModule(module);

        String json = "{\"k1\":\"v1\",\"k2\":\"v2\"}";
        JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, CustomKey.class, String.class);
        Map<CustomKey, String> result = mapper.readValue(json, type);

        Assert.assertNotNull(result);
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("v1", result.get(new CustomKey("custom:k1")));
        Assert.assertEquals("v2", result.get(new CustomKey("custom:k2")));
    }

    @Test
    public void testContextualKeyDeserializer() throws Exception {
        com.fasterxml.jackson.databind.module.SimpleModule module =
                new com.fasterxml.jackson.databind.module.SimpleModule();
        module.addKeyDeserializer(String.class, new ContextualCustomKeyDeserializer());
        mapper.registerModule(module);

        BeanWithIgnoredMapProp bean = mapper.readValue("{\"map\":{\"a\":\"b\"}}", BeanWithIgnoredMapProp.class);
        Assert.assertNotNull(bean.map);
        Assert.assertEquals("b", bean.map.get("propCtx:a"));
    }

    @Test
    public void testIgnorablePropertiesViaAnnotation() throws Exception {
        String json = "{\"map\":{\"valid\":\"ok\",\"ignoredKey1\":\"skip1\",\"ignoredKey2\":\"skip2\"}}";
        BeanWithIgnoredMapProp result = mapper.readValue(json, BeanWithIgnoredMapProp.class);

        Assert.assertNotNull(result.map);
        Assert.assertEquals(1, result.map.size());
        Assert.assertEquals("ok", result.map.get("valid"));
        Assert.assertFalse(result.map.containsKey("ignoredKey1"));
        Assert.assertFalse(result.map.containsKey("ignoredKey2"));
    }

    @Test
    public void testSetIgnorablePropertiesDirectly() throws Exception {
        JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        ValueInstantiator vi = new ValueInstantiator.Base(type);
        
        MapDeserializer deser = new MapDeserializer(type, vi, null, null, null);
        deser.setIgnorableProperties(new String[]{"ignoreMe", "ignoreMeToo"});
        
        Assert.assertFalse(deser.isCachable());
        
        deser.setIgnorableProperties(new String[0]);
        Assert.assertTrue(deser.isCachable());

        deser.setIgnorableProperties(null);
        Assert.assertTrue(deser.isCachable());
    }

    @Test
    public void testIsCachable() {
        JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new ValueInstantiator.Base(type);
        
        MapDeserializer deser = new MapDeserializer(type, vi, null, null, null);
        Assert.assertTrue(deser.isCachable());

        MapDeserializer deserWithIgnored = new MapDeserializer(deser, null, null, null, new HashSet<String>(Collections.singletonList("a")));
        Assert.assertFalse(deserWithIgnored.isCachable());
    }

    @Test
    public void testGettersAndFluentCopy() {
        JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Integer.class);
        ValueInstantiator vi = new ValueInstantiator.Base(type);

        MapDeserializer deser = new MapDeserializer(type, vi, null, null, null);
        Assert.assertEquals(type, deser.getValueType());
        Assert.assertEquals(HashMap.class, deser.getMapClass());
        Assert.assertEquals(Integer.class, deser.getContentType().getRawClass());
        Assert.assertNull(deser.getContentDeserializer());

        MapDeserializer copy = new MapDeserializer(deser);
        Assert.assertEquals(deser.getValueType(), copy.getValueType());

        MapDeserializer resolvedSame = deser.withResolved(null, null, null, null);
        Assert.assertSame(deser, resolvedSame);

        HashSet<String> ignorable = new HashSet<String>(Collections.singletonList("test"));
        MapDeserializer resolvedDiff = deser.withResolved(null, null, null, ignorable);
        Assert.assertNotSame(deser, resolvedDiff);
        Assert.assertFalse(resolvedDiff.isCachable());
    }

    @Test
    public void testDeserializeUpdateExistingMap() throws Exception {
        JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        Map<Object, Object> existing = new HashMap<Object, Object>();
        existing.put("initial", "val");

        JsonParser parser = mapper.getFactory().createParser("{\"newKey\":\"newVal\"}");
        parser.nextToken(); // Move to START_OBJECT

        Map<Object, Object> updated = mapper.readerForUpdating(existing).forType(type).readValue(parser);
        Assert.assertEquals(2, updated.size());
        Assert.assertEquals("val", updated.get("initial"));
        Assert.assertEquals("newVal", updated.get("newKey"));
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeUpdateWithInvalidToken() throws Exception {
        JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        Map<Object, Object> existing = new HashMap<Object, Object>();

        JsonParser parser = mapper.getFactory().createParser("[\"array\"]");
        parser.nextToken(); // Move to START_ARRAY

        mapper.readerForUpdating(existing).forType(type).readValue(parser);
    }

    @Test
    public void testPropertyBasedCreatorDeserialization() throws Exception {
        String json = "{\"creatorParam\":\"paramVal\",\"extraKey\":\"extraVal\"}";
        CustomMapWithCreator result = mapper.readValue(json, CustomMapWithCreator.class);

        Assert.assertNotNull(result);
        Assert.assertEquals("paramVal", result.getCreatorParam());
        Assert.assertEquals("extraVal", result.get("extraKey"));
    }

    @Test
    public void testPropertyBasedCreatorWithIgnoredAndNulls() throws Exception {
        String json = "{\"creatorParam\":\"paramVal\",\"ignored\":\"skip\",\"extraNull\":null}";
        JavaType type = mapper.constructType(CustomMapWithCreator.class);
        
        CustomMapWithCreator result = mapper.readerFor(type).readValue(json);
        Assert.assertNotNull(result);
        Assert.assertEquals("paramVal", result.getCreatorParam());
        Assert.assertTrue(result.containsKey("extraNull"));
        Assert.assertNull(result.get("extraNull"));
    }

    @Test
    public void testDeserializeEmptyStringOrInvalidTokens() throws Exception {
        ObjectMapper stringMapper = new ObjectMapper();
        stringMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        JavaType type = stringMapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);

        Map<String, String> result = stringMapper.readValue("\"\"", type);
        Assert.assertNull(result);

        try {
            stringMapper.readValue("12345", type);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // Success
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveInvalidDelegateCreatorThrowsException() throws Exception {
        JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        
        ValueInstantiator invalidVi = new ValueInstantiator.Base(type) {
            @Override
            public boolean canCreateUsingDelegate() {
                return true;
            }

            @Override
            public JavaType getDelegateType(DeserializationConfig config) {
                return null;
            }
        };

        MapDeserializer deser = new MapDeserializer(type, invalidVi, null, null, null);
        DefaultDeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        deser.resolve(ctxt);
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        ObjectMapper typedMapper = new ObjectMapper();
        typedMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", "value");

        String json = typedMapper.writeValueAsString(map);
        Object deserialized = typedMapper.readValue(json, Object.class);

        Assert.assertTrue(deserialized instanceof Map);
        Map<?, ?> resMap = (Map<?, ?>) deserialized;
        Assert.assertEquals("value", resMap.get("key"));
    }

    @Test
    public void testWrapAndThrowMechanisms() throws Exception {
        JavaType type = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new ValueInstantiator.Base(type);
        MapDeserializer deser = new MapDeserializer(type, vi, null, null, null);

        // 1. Error re-thrown as Error
        try {
            deser.wrapAndThrow(new OutOfMemoryError("test OOM"), Collections.emptyMap(), "key");
            Assert.fail("Should throw OutOfMemoryError");
        } catch (OutOfMemoryError e) {
            Assert.assertEquals("test OOM", e.getMessage());
        }

        // 2. IOException non-JsonMappingException re-thrown as IOException
        try {
            deser.wrapAndThrow(new IOException("plain IO"), Collections.emptyMap(), "key");
            Assert.fail("Should throw IOException");
        } catch (IOException e) {
            Assert.assertFalse(e instanceof JsonMappingException);
            Assert.assertEquals("plain IO", e.getMessage());
        }

        // 3. InvocationTargetException unwrapped
        try {
            InvocationTargetException ite = new InvocationTargetException(new IllegalArgumentException("inner"));
            deser.wrapAndThrow(ite, Collections.emptyMap(), "key");
            Assert.fail("Should throw JsonMappingException wrapping inner");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
            Assert.assertEquals("inner", e.getCause().getMessage());
        }

        // 4. Deprecated wrapAndThrow 2-arg method
        try {
            deser.wrapAndThrow(new RuntimeException("runtime"), Collections.emptyMap());
            Assert.fail("Should throw JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getCause() instanceof RuntimeException);
        }
    }

    @Test
    public void testForwardReferenceHandlingInMap() throws Exception {
        String json = "{\"entities\":{\"e1\":{\"id\":1,\"name\":\"first\"},\"e2\":1}}";

        ObjectMapper om = new ObjectMapper();
        SimpleIdBean result = om.readValue(
                "{\"items\":{\"a\":1,\"b\":{\"@id\":1,\"name\":\"item1\"}}}",
                SimpleIdBean.class
        );

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.items);
        Assert.assertEquals(2, result.items.size());
        Assert.assertEquals("item1", result.items.get("a").name);
        Assert.assertEquals("item1", result.items.get("b").name);
    }

    public static class SimpleIdBean {
        public Map<String, IdItem> items;
    }

    @com.fasterxml.jackson.annotation.JsonIdentityInfo(
            generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "@id",
            scope = IdItem.class
    )
    public static class IdItem {
        public int id;
        @JsonProperty("@id")
        public int idProp;
        public String name;
    }

    @Test
    public void testNoDefaultConstructorThrowsException() throws Exception {
        JavaType type = mapper.getTypeFactory().constructMapType(AbstractMap.class, String.class, String.class);
        try {
            mapper.readValue("{\"k\":\"v\"}", type);
            Assert.fail("Expected JsonMappingException for abstract map without default instantiator");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("No default constructor found") || e.getMessage().contains("can not find"));
        }
    }

    @Test
    public void testIsStdKeyDeserLogic() {
        JavaType stringKeyType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        JavaType objectKeyType = mapper.getTypeFactory().constructMapType(Map.class, Object.class, Object.class);
        JavaType intKeyType = mapper.getTypeFactory().constructMapType(Map.class, Integer.class, Object.class);
        JavaType rawMapType = TypeFactory.unknownType();

        ValueInstantiator vi = new ValueInstantiator.Base(stringKeyType);
        MapDeserializer deser = new MapDeserializer(stringKeyType, vi, null, null, null);

        Assert.assertTrue(deser._isStdKeyDeser(stringKeyType, null));
        Assert.assertTrue(deser._isStdKeyDeser(rawMapType, null));
        Assert.assertTrue(deser._isStdKeyDeser(objectKeyType, null));
        Assert.assertTrue(deser._isStdKeyDeser(stringKeyType, null));

        KeyDeserializer customKeyDeser = new CustomKeyDeserializer();
        Assert.assertFalse(deser._isStdKeyDeser(intKeyType, customKeyDeser));
        Assert.assertFalse(deser._isStdKeyDeser(stringKeyType, customKeyDeser));
    }
}
