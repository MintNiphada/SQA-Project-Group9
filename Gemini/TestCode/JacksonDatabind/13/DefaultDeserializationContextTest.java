package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerator.IdKey;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;
import com.fasterxml.jackson.databind.introspect.Annotated;

public class DefaultDeserializationContextTest {

    private ObjectMapper mapper;
    private DeserializationConfig config;
    private DefaultDeserializationContext.Impl context;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        config = mapper.getDeserializationConfig();
        context = new DefaultDeserializationContext.Impl(BeanDeserializerFactory.instance);
    }

    // --- Subclass and copy() tests ---

    private static class CustomContextSubclass extends DefaultDeserializationContext {
        private static final long serialVersionUID = 1L;

        public CustomContextSubclass(DeserializerFactory df) {
            super(df, null);
        }

        public CustomContextSubclass(CustomContextSubclass src, DeserializationConfig cfg, JsonParser jp, InjectableValues iv) {
            super(src, cfg, jp, iv);
        }

        public CustomContextSubclass(CustomContextSubclass src, DeserializerFactory df) {
            super(src, df);
        }

        public CustomContextSubclass(CustomContextSubclass src) {
            super(src);
        }

        @Override
        public DefaultDeserializationContext with(DeserializerFactory factory) {
            return new CustomContextSubclass(this, factory);
        }

        @Override
        public DefaultDeserializationContext createInstance(DeserializationConfig cfg, JsonParser jp, InjectableValues values) {
            return new CustomContextSubclass(this, cfg, jp, values);
        }
    }

    private static class CustomImplSubclass extends DefaultDeserializationContext.Impl {
        private static final long serialVersionUID = 1L;

        public CustomImplSubclass(DeserializerFactory df) {
            super(df);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testBaseCopyThrowsIllegalStateException() {
        CustomContextSubclass custom = new CustomContextSubclass(BeanDeserializerFactory.instance);
        custom.copy();
    }

    @Test(expected = IllegalStateException.class)
    public void testImplSubclassCopyThrowsIllegalStateException() {
        CustomImplSubclass customImpl = new CustomImplSubclass(BeanDeserializerFactory.instance);
        customImpl.copy();
    }

    @Test
    public void testImplCopyAndWithAndCreateInstance() {
        DefaultDeserializationContext copy = context.copy();
        Assert.assertNotNull(copy);
        Assert.assertTrue(copy instanceof DefaultDeserializationContext.Impl);

        DefaultDeserializationContext withFactory = context.with(BeanDeserializerFactory.instance);
        Assert.assertNotNull(withFactory);
        Assert.assertTrue(withFactory instanceof DefaultDeserializationContext.Impl);

        DefaultDeserializationContext inst = context.createInstance(config, null, null);
        Assert.assertNotNull(inst);
        Assert.assertTrue(inst instanceof DefaultDeserializationContext.Impl);
        Assert.assertSame(config, inst.getConfig());
    }

    @Test
    public void testCustomSubclassConstructors() {
        CustomContextSubclass c1 = new CustomContextSubclass(BeanDeserializerFactory.instance);
        CustomContextSubclass c2 = (CustomContextSubclass) c1.with(BeanDeserializerFactory.instance);
        Assert.assertNotNull(c2);
        CustomContextSubclass c3 = (CustomContextSubclass) c1.createInstance(config, null, null);
        Assert.assertNotNull(c3);
        CustomContextSubclass c4 = new CustomContextSubclass(c1);
        Assert.assertNotNull(c4);
    }

    // --- Object ID resolution tests ---

    @Test
    public void testFindObjectIdAndCaching() {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        ObjectIdGenerator<Integer> gen1 = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ReadableObjectId roid1 = ctxt.findObjectId(123, gen1, resolver);
        Assert.assertNotNull(roid1);
        Assert.assertEquals(123, roid1.getKey().key);

        // Same key should return identical cached instance
        ReadableObjectId roid2 = ctxt.findObjectId(123, gen1, resolver);
        Assert.assertSame(roid1, roid2);

        // Different ID should return a new instance
        ReadableObjectId roid3 = ctxt.findObjectId(456, gen1, resolver);
        Assert.assertNotSame(roid1, roid3);
    }

    @Test
    public void testFindObjectIdDeprecatedMethod() {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        ObjectIdGenerator<Integer> gen = new ObjectIdGenerators.IntSequenceGenerator();

        @SuppressWarnings("deprecation")
        ReadableObjectId roid1 = ctxt.findObjectId(99, gen);
        Assert.assertNotNull(roid1);

        @SuppressWarnings("deprecation")
        ReadableObjectId roid2 = ctxt.findObjectId(99, gen);
        Assert.assertSame(roid1, roid2);
    }

    @Test
    public void testFindObjectIdResolverReuse() {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);

        ObjectIdResolver customResolver1 = new ObjectIdResolver() {
            @Override
            public void bindItem(IdKey id, Object pojo) {}

            @Override
            public Object resolveId(IdKey id) { return null; }

            @Override
            public ObjectIdResolver newForDeserialization(Object context) { return this; }

            @Override
            public boolean canUseFor(ObjectIdResolver resolverType) {
                return resolverType.getClass() == this.getClass();
            }
        };

        ObjectIdGenerator<Integer> gen1 = new ObjectIdGenerators.IntSequenceGenerator();
        ReadableObjectId roid1 = ctxt.findObjectId(1, gen1, customResolver1);
        ReadableObjectId roid2 = ctxt.findObjectId(2, gen1, customResolver1);

        Assert.assertNotSame(roid1, roid2);
    }

    @Test
    public void testCheckUnresolvedObjectIdWithNoObjectIds() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        ctxt.checkUnresolvedObjectId();
    }

    @Test
    public void testCheckUnresolvedObjectIdDisabled() throws Exception {
        DeserializationConfig cfg = config.without(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        DefaultDeserializationContext ctxt = context.createInstance(cfg, null, null);

        ObjectIdGenerator<Integer> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ReadableObjectId roid = ctxt.findObjectId(1, gen, new SimpleObjectIdResolver());
        roid.appendReferring(new Referring(null, String.class) {
            @Override
            public void handleResolvedForwardReference(Object id, Object value) throws IOException {}
        });

        // Feature is disabled, should not throw
        ctxt.checkUnresolvedObjectId();
    }

    @Test
    public void testCheckUnresolvedObjectIdNoReferringProperties() throws Exception {
        DeserializationConfig cfg = config.with(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        DefaultDeserializationContext ctxt = context.createInstance(cfg, null, null);

        ObjectIdGenerator<Integer> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ctxt.findObjectId(1, gen, new SimpleObjectIdResolver());

        // Has entries in _objectIds but no referring properties
        ctxt.checkUnresolvedObjectId();
    }

    @Test
    public void testCheckUnresolvedObjectIdThrowsException() {
        DeserializationConfig cfg = config.with(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        DefaultDeserializationContext ctxt = context.createInstance(cfg, null, null);

        ObjectIdGenerator<Integer> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ReadableObjectId roid = ctxt.findObjectId(42, gen, new SimpleObjectIdResolver());

        final JsonLocation loc = new JsonLocation("src", 100L, 1, 10);
        roid.appendReferring(new Referring(null, String.class) {
            @Override
            public JsonLocation getLocation() {
                return loc;
            }

            @Override
            public void handleResolvedForwardReference(Object id, Object value) throws IOException {}
        });

        try {
            ctxt.checkUnresolvedObjectId();
            Assert.fail("Expected UnresolvedForwardReference to be thrown");
        } catch (UnresolvedForwardReference ufr) {
            Assert.assertTrue(ufr.getMessage().contains("Unresolved forward references for:"));
            Assert.assertNotNull(ufr.getUnresolvedIds());
            Assert.assertEquals(1, ufr.getUnresolvedIds().size());
            UnresolvedId uid = ufr.getUnresolvedIds().iterator().next();
            Assert.assertEquals(42, uid.getId());
            Assert.assertEquals(String.class, uid.getType());
            Assert.assertEquals(loc, uid.getLocation());
        }
    }

    // --- deserializerInstance tests ---

    public static class DummyResolvableDeserializer extends JsonDeserializer<String> implements ResolvableDeserializer {
        public boolean resolved = false;

        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return "dummy";
        }

        @Override
        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
            resolved = true;
        }
    }

    public static class DummyPlainDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return "plain";
        }
    }

    @Test
    public void testDeserializerInstanceNull() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        Assert.assertNull(ctxt.deserializerInstance(null, null));
    }

    @Test
    public void testDeserializerInstanceDirectInstance() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        DummyResolvableDeserializer deser = new DummyResolvableDeserializer();
        JsonDeserializer<Object> result = ctxt.deserializerInstance(null, deser);

        Assert.assertSame(deser, result);
        Assert.assertTrue(deser.resolved);
    }

    @Test(expected = IllegalStateException.class)
    public void testDeserializerInstanceInvalidType() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        ctxt.deserializerInstance(null, "NotAClassOrDeserializer");
    }

    @Test
    public void testDeserializerInstanceNoneClass() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        Assert.assertNull(ctxt.deserializerInstance(null, JsonDeserializer.None.class));
    }

    @Test
    public void testDeserializerInstanceBogusClass() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        Assert.assertNull(ctxt.deserializerInstance(null, NoClass.class));
        Assert.assertNull(ctxt.deserializerInstance(null, Void.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testDeserializerInstanceNotAssignableClass() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        ctxt.deserializerInstance(null, String.class);
    }

    @Test
    public void testDeserializerInstanceFromClass() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        JsonDeserializer<Object> deser = ctxt.deserializerInstance(null, DummyResolvableDeserializer.class);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof DummyResolvableDeserializer);
        Assert.assertTrue(((DummyResolvableDeserializer) deser).resolved);
    }

    @Test
    public void testDeserializerInstanceWithHandlerInstantiator() throws Exception {
        final DummyPlainDeserializer customInst = new DummyPlainDeserializer();
        HandlerInstantiator hi = new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
                if (deserClass == DummyPlainDeserializer.class) {
                    return customInst;
                }
                return null;
            }

            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.JsonSerializer<?> serializerInstance(com.fasterxml.jackson.databind.SerializationConfig config, Annotated annotated, Class<?> serClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
                return null;
            }
        };

        DeserializationConfig cfgWithHi = config.with(hi);
        DefaultDeserializationContext ctxt = context.createInstance(cfgWithHi, null, null);

        JsonDeserializer<Object> deser = ctxt.deserializerInstance(null, DummyPlainDeserializer.class);
        Assert.assertSame(customInst, deser);

        // Class returning null from handler instantiator falls back to ClassUtil.createInstance
        JsonDeserializer<Object> deserFallback = ctxt.deserializerInstance(null, DummyResolvableDeserializer.class);
        Assert.assertNotNull(deserFallback);
        Assert.assertTrue(deserFallback instanceof DummyResolvableDeserializer);
    }

    // --- keyDeserializerInstance tests ---

    public static class DummyResolvableKeyDeserializer extends KeyDeserializer implements ResolvableDeserializer {
        public boolean resolved = false;

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return key;
        }

        @Override
        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
            resolved = true;
        }
    }

    public static class DummyPlainKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return key;
        }
    }

    @Test
    public void testKeyDeserializerInstanceNull() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        Assert.assertNull(ctxt.keyDeserializerInstance(null, null));
    }

    @Test
    public void testKeyDeserializerInstanceDirectInstance() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        DummyResolvableKeyDeserializer deser = new DummyResolvableKeyDeserializer();
        KeyDeserializer result = ctxt.keyDeserializerInstance(null, deser);

        Assert.assertSame(deser, result);
        Assert.assertTrue(deser.resolved);
    }

    @Test(expected = IllegalStateException.class)
    public void testKeyDeserializerInstanceInvalidType() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        ctxt.keyDeserializerInstance(null, 12345);
    }

    @Test
    public void testKeyDeserializerInstanceNoneClass() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        Assert.assertNull(ctxt.keyDeserializerInstance(null, KeyDeserializer.None.class));
    }

    @Test
    public void testKeyDeserializerInstanceBogusClass() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        Assert.assertNull(ctxt.keyDeserializerInstance(null, Void.class));
        Assert.assertNull(ctxt.keyDeserializerInstance(null, NoClass.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testKeyDeserializerInstanceNotAssignableClass() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        ctxt.keyDeserializerInstance(null, Integer.class);
    }

    @Test
    public void testKeyDeserializerInstanceFromClass() throws Exception {
        DefaultDeserializationContext ctxt = context.createInstance(config, null, null);
        KeyDeserializer deser = ctxt.keyDeserializerInstance(null, DummyResolvableKeyDeserializer.class);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof DummyResolvableKeyDeserializer);
        Assert.assertTrue(((DummyResolvableKeyDeserializer) deser).resolved);
    }

    @Test
    public void testKeyDeserializerInstanceWithHandlerInstantiator() throws Exception {
        final DummyPlainKeyDeserializer customInst = new DummyPlainKeyDeserializer();
        HandlerInstantiator hi = new HandlerInstantiator() {
            @Override
            public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
                return null;
            }

            @Override
            public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
                if (keyDeserClass == DummyPlainKeyDeserializer.class) {
                    return customInst;
                }
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.JsonSerializer<?> serializerInstance(com.fasterxml.jackson.databind.SerializationConfig config, Annotated annotated, Class<?> serClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
                return null;
            }
        };

        DeserializationConfig cfgWithHi = config.with(hi);
        DefaultDeserializationContext ctxt = context.createInstance(cfgWithHi, null, null);

        KeyDeserializer deser = ctxt.keyDeserializerInstance(null, DummyPlainKeyDeserializer.class);
        Assert.assertSame(customInst, deser);

        // Fallback when HandlerInstantiator returns null
        KeyDeserializer deserFallback = ctxt.keyDeserializerInstance(null, DummyResolvableKeyDeserializer.class);
        Assert.assertNotNull(deserFallback);
        Assert.assertTrue(deserFallback instanceof DummyResolvableKeyDeserializer);
    }
}
