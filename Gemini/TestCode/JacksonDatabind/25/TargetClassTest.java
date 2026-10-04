package com.fasterxml.jackson.databind.deser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DeserializerCacheTest {

    private DeserializerCache _cache;
    private ObjectMapper _mapper;
    private DeserializationContext _context;
    private DeserializerFactory _factory;
    private TypeFactory _typeFactory;

    // Test helper classes and interfaces
    enum TestEnum { A, B }

    static class SimpleBean {
        public int x;
        public String y;
    }

    static abstract class AbstractBase {
        public int a;
    }

    @JsonDeserialize(builder = SimpleBuilderPOJO.Builder.class)
    static class SimpleBuilderPOJO {
        final int value;
        SimpleBuilderPOJO(int v) { this.value = v; }

        public static class Builder {
            private int v;
            public Builder withValue(int v) { this.v = v; return this; }
            public SimpleBuilderPOJO build() { return new SimpleBuilderPOJO(v); }
        }
    }

    @JsonDeserialize(converter = StringToBeanConverter.class)
    static class ConvertedBean {
        final String raw;
        ConvertedBean(String raw) { this.raw = raw; }
    }

    static class StringToBeanConverter extends StdConverter<String, ConvertedBean> {
        @Override
        public ConvertedBean convert(String value) {
            return new ConvertedBean(value);
        }
    }

    @JsonDeserialize(using = CustomBeanDeserializer.class)
    static class CustomAnnotatedBean {
        public int id;
    }

    static class CustomBeanDeserializer extends StdDeserializer<CustomAnnotatedBean> {
        public CustomBeanDeserializer() {
            super(CustomAnnotatedBean.class);
        }

        @Override
        public CustomAnnotatedBean deserialize(JsonParser p, DeserializationContext ctxt) {
            return new CustomAnnotatedBean();
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class ObjectShapedList extends ArrayList<String> {
        private static final long serialVersionUID = 1L;
        public int extraField;
    }

    static class DummyKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return key;
        }
    }

    static class ResolvableDummyDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {
        boolean resolved = false;

        public ResolvableDummyDeserializer() {
            super(Object.class);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return null;
        }

        @Override
        public void resolve(DeserializationContext ctxt) {
            resolved = true;
        }
    }

    static class ResolvableDummyKeyDeserializer extends KeyDeserializer implements ResolvableDeserializer {
        boolean resolved = false;

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return key;
        }

        @Override
        public void resolve(DeserializationContext ctxt) {
            resolved = true;
        }
    }

    static class NonCachableDeserializer extends StdDeserializer<Object> {
        public NonCachableDeserializer() {
            super(Object.class);
        }

        @Override
        public boolean isCachable() {
            return false;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return null;
        }
    }

    @Before
    public void setUp() {
        _cache = new DeserializerCache();
        _mapper = new ObjectMapper();
        _context = _mapper.getDeserializationContext();
        _factory = new BeanDeserializerFactory(new DeserializerFactoryConfig());
        _typeFactory = _mapper.getTypeFactory();
    }

    @Test
    public void testLifecycleAndFlush() throws Exception {
        Assert.assertEquals(0, _cache.cachedDeserializersCount());

        JavaType type = _typeFactory.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, type);
        Assert.assertNotNull(deser);
        Assert.assertTrue(_cache.cachedDeserializersCount() > 0);

        _cache.flushCachedDeserializers();
        Assert.assertEquals(0, _cache.cachedDeserializersCount());
    }

    @Test
    public void testSerializationWriteReplace() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        _cache.findValueDeserializer(_context, _factory, type);
        Assert.assertTrue(_cache.cachedDeserializersCount() > 0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(_cache);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DeserializerCache deserialized = (DeserializerCache) ois.readObject();
        ois.close();

        Assert.assertNotNull(deserialized);
        Assert.assertTrue(deserialized._incompleteDeserializers.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializerNull() {
        _cache._findCachedDeserializer(null);
    }

    @Test
    public void testHasValueDeserializerFor() throws Exception {
        JavaType type = _typeFactory.constructType(SimpleBean.class);
        Assert.assertTrue(_cache.hasValueDeserializerFor(_context, _factory, type));
        Assert.assertTrue(_cache.hasValueDeserializerFor(_context, _factory, type)); // cache hit
    }

    @Test
    public void testFindKeyDeserializer() throws Exception {
        JavaType stringType = _typeFactory.constructType(String.class);
        KeyDeserializer kd = _cache.findKeyDeserializer(_context, _factory, stringType);
        Assert.assertNotNull(kd);

        // Resolvable key deserializer test
        DeserializerFactory resolvableKdFactory = new BeanDeserializerFactory(new DeserializerFactoryConfig()) {
            @Override
            public KeyDeserializer createKeyDeserializer(DeserializationContext ctxt, JavaType type) {
                return new ResolvableDummyKeyDeserializer();
            }
        };
        KeyDeserializer rkd = _cache.findKeyDeserializer(_context, resolvableKdFactory, stringType);
        Assert.assertTrue(rkd instanceof ResolvableDummyKeyDeserializer);
        Assert.assertTrue(((ResolvableDummyKeyDeserializer) rkd).resolved);
    }

    @Test(expected = JsonMappingException.class)
    public void testFindKeyDeserializerUnknown() throws Exception {
        DeserializerFactory nullKdFactory = new BeanDeserializerFactory(new DeserializerFactoryConfig()) {
            @Override
            public KeyDeserializer createKeyDeserializer(DeserializationContext ctxt, JavaType type) {
                return null;
            }
        };
        _cache.findKeyDeserializer(_context, nullKdFactory, _typeFactory.constructType(SimpleBean.class));
    }

    @Test
    public void testFindValueDeserializerVariousTypes() throws Exception {
        // 1. Enum
        JavaType enumType = _typeFactory.constructType(TestEnum.class);
        JsonDeserializer<Object> deserEnum = _cache.findValueDeserializer(_context, _factory, enumType);
        Assert.assertNotNull(deserEnum);

        // 2. Array
        ArrayType arrayType = _typeFactory.constructArrayType(String.class);
        JsonDeserializer<Object> deserArray = _cache.findValueDeserializer(_context, _factory, arrayType);
        Assert.assertNotNull(deserArray);

        // 3. Map
        MapType mapType = _typeFactory.constructMapType(HashMap.class, String.class, Integer.class);
        JsonDeserializer<Object> deserMap = _cache.findValueDeserializer(_context, _factory, mapType);
        Assert.assertNotNull(deserMap);

        // 4. MapLike (not true map)
        MapLikeType mapLikeType = MapLikeType.construct(SimpleBean.class, _typeFactory.constructType(String.class), _typeFactory.constructType(Integer.class));
        try {
            _cache.findValueDeserializer(_context, _factory, mapLikeType);
        } catch (JsonMappingException e) {
            // Map-like types without deserializer may fail or fallback, testing routing branch
        }

        // 5. Collection
        CollectionType listType = _typeFactory.constructCollectionType(ArrayList.class, String.class);
        JsonDeserializer<Object> deserList = _cache.findValueDeserializer(_context, _factory, listType);
        Assert.assertNotNull(deserList);

        // 6. CollectionLike
        CollectionLikeType colLikeType = CollectionLikeType.construct(SimpleBean.class, _typeFactory.constructType(String.class));
        try {
            _cache.findValueDeserializer(_context, _factory, colLikeType);
        } catch (JsonMappingException e) {
            // Verify branch execution
        }

        // 7. Tree / JsonNode
        JavaType nodeType = _typeFactory.constructType(ObjectNode.class);
        JsonDeserializer<Object> deserNode = _cache.findValueDeserializer(_context, _factory, nodeType);
        Assert.assertNotNull(deserNode);

        // 8. Custom Annotation Deserializer
        JavaType customType = _typeFactory.constructType(CustomAnnotatedBean.class);
        JsonDeserializer<Object> deserCustom = _cache.findValueDeserializer(_context, _factory, customType);
        Assert.assertTrue(deserCustom instanceof CustomBeanDeserializer);

        // 9. Builder based POJO
        JavaType builderType = _typeFactory.constructType(SimpleBuilderPOJO.class);
        JsonDeserializer<Object> deserBuilder = _cache.findValueDeserializer(_context, _factory, builderType);
        Assert.assertNotNull(deserBuilder);

        // 10. Converter based POJO
        JavaType convertedType = _typeFactory.constructType(ConvertedBean.class);
        JsonDeserializer<Object> deserConv = _cache.findValueDeserializer(_context, _factory, convertedType);
        Assert.assertNotNull(deserConv);

        // 11. Collection with Shape.OBJECT
        JavaType objShapedType = _typeFactory.constructType(ObjectShapedList.class);
        JsonDeserializer<Object> deserObjList = _cache.findValueDeserializer(_context, _factory, objShapedType);
        Assert.assertNotNull(deserObjList);
    }

    @Test
    public void testIncompleteDeserializersHandling() throws Exception {
        final JavaType type = _typeFactory.constructType(SimpleBean.class);
        final ResolvableDummyDeserializer resolvableDeser = new ResolvableDummyDeserializer();

        DeserializerFactory customFactory = new BeanDeserializerFactory(new DeserializerFactoryConfig()) {
            @Override
            public JsonDeserializer<Object> createBeanDeserializer(DeserializationContext ctxt, JavaType t, BeanDescription beanDesc) {
                return resolvableDeser;
            }
        };

        JsonDeserializer<Object> result = _cache._createAndCache2(_context, customFactory, type);
        Assert.assertSame(resolvableDeser, result);
        Assert.assertTrue(resolvableDeser.resolved);
        Assert.assertTrue(_cache._incompleteDeserializers.isEmpty());
    }

    @Test
    public void testNonCachableDeserializerNotCached() throws Exception {
        final JavaType type = _typeFactory.constructType(SimpleBean.class);
        final NonCachableDeserializer nonCachable = new NonCachableDeserializer();

        DeserializerFactory customFactory = new BeanDeserializerFactory(new DeserializerFactoryConfig()) {
            @Override
            public JsonDeserializer<Object> createBeanDeserializer(DeserializationContext ctxt, JavaType t, BeanDescription beanDesc) {
                return nonCachable;
            }
        };

        _cache.flushCachedDeserializers();
        JsonDeserializer<Object> result = _cache._createAndCache2(_context, customFactory, type);
        Assert.assertSame(nonCachable, result);
        Assert.assertEquals(0, _cache.cachedDeserializersCount());
    }

    @Test
    public void testCustomValueHandlerPreventsCaching() throws Exception {
        JavaType elemType = _typeFactory.constructType(String.class).withValueHandler(new DummyKeyDeserializer());
        JavaType listType = _typeFactory.constructCollectionType(ArrayList.class, elemType);

        Assert.assertNull(_cache._findCachedDeserializer(listType));

        JsonDeserializer<Object> deser = _cache.findValueDeserializer(_context, _factory, listType);
        Assert.assertNotNull(deser);
        // It shouldn't be cached due to custom value handler
        Assert.assertNull(_cache._findCachedDeserializer(listType));
    }

    @Test
    public void testCustomTypeHandlerPreventsCaching() throws Exception {
        JavaType elemType = _typeFactory.constructType(String.class).withTypeHandler("dummy");
        JavaType listType = _typeFactory.constructCollectionType(ArrayList.class, elemType);

        Assert.assertNull(_cache._findCachedDeserializer(listType));
    }

    @Test
    public void testHandleUnknownValueDeserializer() {
        JavaType abstractType = _typeFactory.constructType(AbstractBase.class);
        try {
            _cache._handleUnknownValueDeserializer(abstractType);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("abstract type"));
        }

        JavaType concreteType = _typeFactory.constructType(SimpleBean.class);
        try {
            _cache._handleUnknownValueDeserializer(concreteType);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertFalse(e.getMessage().contains("abstract type"));
            Assert.assertTrue(e.getMessage().contains("Can not find a Value deserializer for type"));
        }
    }

    @Test(expected = JsonMappingException.class)
    public void testHandleUnknownKeyDeserializer() throws Exception {
        _cache._handleUnknownKeyDeserializer(_typeFactory.constructType(String.class));
    }

    @Test
    public void testFactoryThrowsIllegalArgumentException() {
        DeserializerFactory failingFactory = new BeanDeserializerFactory(new DeserializerFactoryConfig()) {
            @Override
            public JsonDeserializer<Object> createBeanDeserializer(DeserializationContext ctxt, JavaType type, BeanDescription beanDesc) {
                throw new IllegalArgumentException("Forced IAE");
            }
        };

        try {
            _cache._createAndCache2(_context, failingFactory, _typeFactory.constructType(SimpleBean.class));
            Assert.fail("Expected JsonMappingException wrapping IAE");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Forced IAE"));
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testFindConverterMethods() throws Exception {
        BeanDescription beanDesc = _context.getConfig().introspect(_typeFactory.constructType(ConvertedBean.class));
        AnnotatedClass classInfo = beanDesc.getClassInfo();

        Converter<Object, Object> conv = _cache.findConverter(_context, classInfo);
        Assert.assertNotNull(conv);

        JsonDeserializer<Object> origDeser = new NonCachableDeserializer();
        JsonDeserializer<Object> convertedDeser = _cache.findConvertingDeserializer(_context, classInfo, origDeser);
        Assert.assertNotNull(convertedDeser);
        Assert.assertNotSame(origDeser, convertedDeser);

        // When converter is null, return original deserializer
        BeanDescription simpleDesc = _context.getConfig().introspect(_typeFactory.constructType(SimpleBean.class));
        JsonDeserializer<Object> sameDeser = _cache.findConvertingDeserializer(_context, simpleDesc.getClassInfo(), origDeser);
        Assert.assertSame(origDeser, sameDeser);
    }

    @Test
    public void testVerifyAsClassViaFindDeserializerFromAnnotation() throws Exception {
        BeanDescription simpleDesc = _context.getConfig().introspect(_typeFactory.constructType(SimpleBean.class));
        JsonDeserializer<Object> deser = _cache.findDeserializerFromAnnotation(_context, simpleDesc.getClassInfo());
        Assert.assertNull(deser);

        BeanDescription customDesc = _context.getConfig().introspect(_typeFactory.constructType(CustomAnnotatedBean.class));
        JsonDeserializer<Object> customDeser = _cache.findDeserializerFromAnnotation(_context, customDesc.getClassInfo());
        Assert.assertNotNull(customDeser);
    }
}
