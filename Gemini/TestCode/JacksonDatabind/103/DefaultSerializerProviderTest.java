package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultSerializerProviderTest {

    static class CustomProvider extends DefaultSerializerProvider {
        private static final long serialVersionUID = 1L;

        public CustomProvider() {
            super();
        }

        public CustomProvider(SerializerProvider src, SerializationConfig config, SerializerFactory f) {
            super(src, config, f);
        }

        public CustomProvider(CustomProvider src) {
            super(src);
        }

        @Override
        public DefaultSerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
            return new CustomProvider(this, config, jsf);
        }
    }

    static class DummyBean {
        public String name = "test";
    }

    static class BrokenFilter {
        @Override
        public boolean equals(Object obj) {
            throw new RuntimeException("Filter error");
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    @Test
    public void testCopyAndCreateInstance() {
        DefaultSerializerProvider.Impl impl = new DefaultSerializerProvider.Impl();
        DefaultSerializerProvider copy = impl.copy();
        Assert.assertNotNull(copy);
        Assert.assertEquals(DefaultSerializerProvider.Impl.class, copy.getClass());

        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider instance = impl.createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());
        Assert.assertNotNull(instance);
    }

    @Test(expected = IllegalStateException.class)
    public void testCustomProviderCopyThrowsException() {
        CustomProvider custom = new CustomProvider();
        custom.copy();
    }

    @Test
    public void testSerializerInstance() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();

        Assert.assertNull(prov.serializerInstance(null, null));
        Assert.assertNull(prov.serializerInstance(null, JsonSerializer.None.class));

        JsonSerializer<?> serObj = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {}
        };
        Assert.assertSame(serObj, prov.serializerInstance(null, serObj));

        JsonSerializer<?> resolved = prov.serializerInstance(null, serObj.getClass());
        Assert.assertNotNull(resolved);
    }

    @Test(expected = JsonMappingException.class)
    public void testSerializerInstanceInvalidType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        prov.serializerInstance(null, "invalidType");
    }

    @Test(expected = JsonMappingException.class)
    public void testSerializerInstanceNonSerializerClass() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        prov.serializerInstance(null, String.class);
    }

    @Test
    public void testIncludeFilterInstance() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        Assert.assertNull(prov.includeFilterInstance(null, null));
        Object filter = prov.includeFilterInstance(null, DummyBean.class);
        Assert.assertNotNull(filter);
        Assert.assertEquals(DummyBean.class, filter.getClass());
    }

    @Test
    public void testIncludeFilterSuppressNulls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        Assert.assertTrue(prov.includeFilterSuppressNulls(null));
        Assert.assertFalse(prov.includeFilterSuppressNulls("non-null"));
    }

    @Test
    public void testIncludeFilterSuppressNullsException() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        try {
            prov.includeFilterSuppressNulls(new BrokenFilter());
            Assert.fail("Should throw exception");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Problem determining whether filter"));
        }
    }

    @Test
    public void testFindObjectId() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        Object pojo = new Object();

        WritableObjectId wid1 = prov.findObjectId(pojo, gen);
        Assert.assertNotNull(wid1);
        WritableObjectId wid2 = prov.findObjectId(pojo, gen);
        Assert.assertSame(wid1, wid2);

        Object pojo2 = new Object();
        WritableObjectId wid3 = prov.findObjectId(pojo2, gen);
        Assert.assertNotSame(wid1, wid3);
    }

    @Test
    public void testFindObjectIdWithEquality() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID);
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();

        String pojo1 = new String("test");
        String pojo2 = new String("test");
        WritableObjectId wid1 = prov.findObjectId(pojo1, gen);
        WritableObjectId wid2 = prov.findObjectId(pojo2, gen);
        Assert.assertSame(wid1, wid2);
    }

    @Test
    public void testHasSerializerFor() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();

        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        Assert.assertTrue(prov.hasSerializerFor(String.class, cause));
        Assert.assertNull(cause.get());

        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        Assert.assertTrue(prov.hasSerializerFor(Object.class, cause));
    }

    @Test
    public void testSerializeValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);

        prov.serializeValue(gen, null);
        Assert.assertSame(gen, prov.getGenerator());
        Assert.assertEquals("null", sw.toString());

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        DummyBean bean = new DummyBean();
        prov.serializeValue(gen, bean);
        Assert.assertTrue(sw.toString().contains("test"));
    }

    @Test
    public void testSerializeValueWithRootType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);

        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        DummyBean bean = new DummyBean();
        prov.serializeValue(gen, bean, type);
        Assert.assertTrue(sw.toString().contains("test"));

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        prov.serializeValue(gen, null, type);
        Assert.assertEquals("null", sw.toString());
    }

    @Test(expected = JsonMappingException.class)
    public void testSerializeValueIncompatibleRootType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        prov.serializeValue(gen, "string value", type);
    }

    @Test
    public void testSerializeValueWithExplicitSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);

        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        DummyBean bean = new DummyBean();
        prov.serializeValue(gen, bean, type, null);
        Assert.assertTrue(sw.toString().contains("test"));

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        prov.serializeValue(gen, null, type, null);
        Assert.assertEquals("null", sw.toString());
    }

    @Test
    public void testSerializeValueWithWrapRootValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        DummyBean bean = new DummyBean();
        prov.serializeValue(gen, bean);
        Assert.assertTrue(sw.toString().contains("DummyBean"));

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        prov.serializeValue(gen, bean, type);
        Assert.assertTrue(sw.toString().contains("DummyBean"));

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        prov.serializeValue(gen, bean, type, null);
        Assert.assertTrue(sw.toString().contains("DummyBean"));
    }

    @Test
    public void testSerializePolymorphic() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);

        prov.serializePolymorphic(gen, null, null, null, null);
        Assert.assertEquals("null", sw.toString());

        Map<String, String> map = new HashMap<String, String>();
        map.put("k", "v");
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, String.class);
        TypeSerializer typeSer = mapper.getSerializationConfig().getDefaultTyper(mapType) == null ? null : null;

        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);
        prov.serializePolymorphic(gen, map, mapType, null, typeSer);
        Assert.assertTrue(sw.toString().contains("k"));
    }

    @Test
    public void testCacheOperations() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        prov.findValueSerializer(DummyBean.class, null);
        Assert.assertTrue(prov.cachedSerializersCount() > 0);
        prov.flushCachedSerializers();
        Assert.assertEquals(0, prov.cachedSerializersCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAcceptJsonFormatVisitorNullType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        prov.acceptJsonFormatVisitor(null, new JsonFormatVisitorWrapper.Base());
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        JavaType type = TypeFactory.defaultInstance().constructType(DummyBean.class);
        prov.acceptJsonFormatVisitor(type, new JsonFormatVisitorWrapper.Base());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGenerateJsonSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        com.fasterxml.jackson.databind.jsonschema.JsonSchema schema = prov.generateJsonSchema(DummyBean.class);
        Assert.assertNotNull(schema);
        Assert.assertNotNull(schema.getSchemaNode());
    }

    @SuppressWarnings("deprecation")
    @Test(expected = IllegalArgumentException.class)
    public void testGenerateJsonSchemaNonObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        prov.generateJsonSchema(String.class);
    }
}
