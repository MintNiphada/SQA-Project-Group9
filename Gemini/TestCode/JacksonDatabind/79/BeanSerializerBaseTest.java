package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Assert;
import org.junit.Test;

public class BeanSerializerBaseTest {

    static class DummyBeanSerializer extends BeanSerializerBase {
        public DummyBeanSerializer(JavaType type, BeanSerializerBuilder builder,
                                   BeanPropertyWriter[] properties, BeanPropertyWriter[] filteredProperties) {
            super(type, builder, properties, filteredProperties);
        }

        public DummyBeanSerializer(BeanSerializerBase src, BeanPropertyWriter[] properties,
                                   BeanPropertyWriter[] filteredProperties) {
            super(src, properties, filteredProperties);
        }

        public DummyBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter) {
            super(src, objectIdWriter);
        }

        public DummyBeanSerializer(BeanSerializerBase src, ObjectIdWriter objectIdWriter, Object filterId) {
            super(src, objectIdWriter, filterId);
        }

        public DummyBeanSerializer(BeanSerializerBase src, String[] toIgnore) {
            super(src, toIgnore);
        }

        public DummyBeanSerializer(BeanSerializerBase src) {
            super(src);
        }

        public DummyBeanSerializer(BeanSerializerBase src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }

        @Override
        public BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter) {
            return new DummyBeanSerializer(this, objectIdWriter);
        }

        @Override
        protected BeanSerializerBase withIgnorals(String[] toIgnore) {
            return new DummyBeanSerializer(this, toIgnore);
        }

        @Override
        protected BeanSerializerBase asArraySerializer() {
            return this;
        }

        @Override
        public BeanSerializerBase withFilterId(Object filterId) {
            return new DummyBeanSerializer(this, _objectIdWriter, filterId);
        }

        @Override
        public void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            if (_propertyFilterId != null) {
                serializeFieldsFiltered(bean, gen, provider);
            } else {
                serializeFields(bean, gen, provider);
            }
            gen.writeEndObject();
        }
    }

    @JsonSerializableSchema(id = "urn:test:beanserializer")
    static class SampleSchemaBean {
        public String propA;
        public int propB;
    }

    static class SamplePojo {
        public String name = "test";
        public int age = 30;
    }

    @Test
    public void testConstructorsAndProperties() {
        JavaType type = TypeFactory.defaultInstance().constructType(SamplePojo.class);
        DummyBeanSerializer ser = new DummyBeanSerializer(type, null, BeanSerializerBase.NO_PROPS, null);
        Assert.assertFalse(ser.usesObjectId());
        Assert.assertFalse(ser.properties().hasNext());

        DummyBeanSerializer copy = new DummyBeanSerializer(ser);
        Assert.assertFalse(copy.usesObjectId());

        DummyBeanSerializer renamed = new DummyBeanSerializer(ser, NameTransformer.NOP);
        Assert.assertNotNull(renamed);

        DummyBeanSerializer filtered = (DummyBeanSerializer) ser.withFilterId("myFilter");
        Assert.assertEquals("myFilter", filtered._propertyFilterId);
    }

    @Test
    public void testWithIgnorals() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SamplePojo.class);
        BasicBeanDescription desc = BasicClassIntrospector.STRING_DESC;
        BeanSerializerBuilder builder = new BeanSerializerBuilder(desc);

        BeanPropertyWriter[] props = new BeanPropertyWriter[0];
        DummyBeanSerializer ser = new DummyBeanSerializer(type, builder, props, null);

        BeanSerializerBase ignored = ser.withIgnorals(new String[]{"name"});
        Assert.assertNotNull(ignored);
    }

    @Test
    public void testSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SamplePojo pojo = new SamplePojo();
        String json = mapper.writeValueAsString(pojo);
        Assert.assertTrue(json.contains("name"));
        Assert.assertTrue(json.contains("age"));
    }

    @Test
    public void testGetSchema() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(SampleSchemaBean.class);

        DummyBeanSerializer ser = new DummyBeanSerializer(type, null, BeanSerializerBase.NO_PROPS, null);
        JsonNode schemaNode = ser.getSchema(prov, null);

        Assert.assertNotNull(schemaNode);
        Assert.assertEquals("object", schemaNode.get("type").asText());
        Assert.assertEquals("urn:test:beanserializer", schemaNode.get("id").asText());
        Assert.assertTrue(schemaNode.has("properties"));
    }

    @Test
    public void testGetSchemaWithFilter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filterProvider = new SimpleFilterProvider().addFilter("customFilter", SimpleBeanPropertyFilter.serializeAll());
        mapper.setFilterProvider(filterProvider);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JavaType type = mapper.constructType(SamplePojo.class);
        DummyBeanSerializer ser = new DummyBeanSerializer(type, null, BeanSerializerBase.NO_PROPS, null);
        BeanSerializerBase filteredSer = ser.withFilterId("customFilter");

        JsonNode schemaNode = filteredSer.getSchema(prov, null);
        Assert.assertNotNull(schemaNode);
    }

    @Test
    public void testAcceptJsonFormatVisitorNull() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(SamplePojo.class);
        DummyBeanSerializer ser = new DummyBeanSerializer(type, null, BeanSerializerBase.NO_PROPS, null);
        ser.acceptJsonFormatVisitor(null, type);
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(SamplePojo.class);
        DummyBeanSerializer ser = new DummyBeanSerializer(type, null, BeanSerializerBase.NO_PROPS, null);

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType type) {
                return new JsonObjectFormatVisitor.Base();
            }
        };

        ser.acceptJsonFormatVisitor(visitor, type);
    }

    @Test
    public void testResolveAndContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(SamplePojo.class);
        DummyBeanSerializer ser = new DummyBeanSerializer(type, null, BeanSerializerBase.NO_PROPS, null);

        ser.resolve(prov);
        JsonSerializer<?> contextual = ser.createContextual(prov, null);
        Assert.assertSame(ser, contextual);
    }

    @Test
    public void testSerializeWithTypeNullCustomId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(SamplePojo.class);
        DummyBeanSerializer ser = new DummyBeanSerializer(type, null, BeanSerializerBase.NO_PROPS, null);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        TypeSerializer typeSer = mapper.getSerializationConfig().getDefaultTyper(type) == null ?
                new com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeSerializer(
                        new com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver(type, mapper.getTypeFactory()),
                        null, "@class"
                ) : null;

        if (typeSer != null) {
            ser.serializeWithType(new SamplePojo(), gen, prov, typeSer);
            gen.flush();
            Assert.assertTrue(sw.toString().contains("@class"));
        }
    }
}
