package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class AnyGetterWriterTest {

    private static class DummyAnnotatedMember extends AnnotatedMember {
        private static final long serialVersionUID = 1L;
        private final String name;
        private Object valueToReturn;

        public DummyAnnotatedMember(String name, Object valueToReturn) {
            super((TypeResolutionContext) null, (AnnotationMap) null);
            this.name = name;
            this.valueToReturn = valueToReturn;
        }

        public void setValueToReturn(Object value) {
            this.valueToReturn = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Object getValue(Object pojo) throws UnsupportedOperationException, IllegalArgumentException {
            return valueToReturn;
        }

        @Override
        public void setValue(Object pojo, Object value) throws UnsupportedOperationException, IllegalArgumentException {
        }

        @Override
        public AnnotatedElement getAnnotated() {
            return null;
        }

        @Override
        public int getModifiers() {
            return 0;
        }

        @Override
        public Class<?> getRawType() {
            return Object.class;
        }

        @Override
        public JavaType getType() {
            return TypeFactory.defaultInstance().constructType(Object.class);
        }

        @Override
        public Class<?> getDeclaringClass() {
            return Object.class;
        }

        @Override
        public Member getMember() {
            return null;
        }

        @Override
        public Annotated withAnnotations(AnnotationMap fallback) {
            return this;
        }
    }

    private static class MockMapSerializer extends MapSerializer {
        private static final long serialVersionUID = 1L;
        boolean serializeFieldsCalled = false;
        boolean serializeFilteredFieldsCalled = false;
        Map<?, ?> passedMap = null;
        PropertyFilter passedFilter = null;

        public MockMapSerializer() {
            super((Set<String>) null,
                    TypeFactory.defaultInstance().constructType(String.class),
                    TypeFactory.defaultInstance().constructType(Object.class),
                    false, null, null, null);
        }

        @Override
        public void serializeFields(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            this.serializeFieldsCalled = true;
            this.passedMap = value;
        }

        @Override
        public void serializeFilteredFields(Map<?, ?> value, JsonGenerator gen, SerializerProvider provider,
                PropertyFilter filter, Object suppressableValue) throws Exception {
            this.serializeFilteredFieldsCalled = true;
            this.passedMap = value;
            this.passedFilter = filter;
        }
    }

    private static class DummyBeanProperty extends BeanProperty.Std {
        public DummyBeanProperty(String name) {
            super(new PropertyName(name), TypeFactory.defaultInstance().constructType(Map.class),
                    null, null, PropertyMetadata.STD_REQUIRED);
        }
    }

    private static class AnyGetterBean {
        private final Map<String, Object> properties = new HashMap<String, Object>();

        public AnyGetterBean(String key, Object value) {
            properties.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return properties;
        }
    }

    private static class NullAnyGetterBean {
        @JsonAnyGetter
        public Map<String, Object> any() {
            return null;
        }
    }

    private static class InvalidTypeAnyGetterBean {
        @JsonAnyGetter
        public String any() {
            return "not-a-map";
        }
    }

    @Test
    public void testGetAndSerializeWithNullValue() throws Exception {
        DummyAnnotatedMember member = new DummyAnnotatedMember("getProps", null);
        MockMapSerializer serializer = new MockMapSerializer();
        BeanProperty prop = new DummyBeanProperty("props");
        AnyGetterWriter writer = new AnyGetterWriter(prop, member, serializer);

        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.getAndSerialize(new Object(), gen, provider);
        Assert.assertFalse(serializer.serializeFieldsCalled);
        gen.close();
    }

    @Test
    public void testGetAndSerializeWithNonMapValueThrowsException() throws Exception {
        DummyAnnotatedMember member = new DummyAnnotatedMember("getProps", "InvalidStringValue");
        MockMapSerializer serializer = new MockMapSerializer();
        BeanProperty prop = new DummyBeanProperty("props");
        AnyGetterWriter writer = new AnyGetterWriter(prop, member, serializer);

        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        try {
            writer.getAndSerialize(new Object(), gen, provider);
            Assert.fail("Expected JsonMappingException to be thrown");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Value returned by 'any-getter' (getProps()) not java.util.Map but java.lang.String"));
        } finally {
            gen.close();
        }
    }

    @Test
    public void testGetAndSerializeWithValidMapAndSerializer() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key1", "val1");

        DummyAnnotatedMember member = new DummyAnnotatedMember("getProps", map);
        MockMapSerializer serializer = new MockMapSerializer();
        BeanProperty prop = new DummyBeanProperty("props");
        AnyGetterWriter writer = new AnyGetterWriter(prop, member, serializer);

        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.getAndSerialize(new Object(), gen, provider);
        Assert.assertTrue(serializer.serializeFieldsCalled);
        Assert.assertSame(map, serializer.passedMap);
        gen.close();
    }

    @Test
    public void testGetAndSerializeWithNullSerializer() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key1", "val1");

        DummyAnnotatedMember member = new DummyAnnotatedMember("getProps", map);
        BeanProperty prop = new DummyBeanProperty("props");
        AnyGetterWriter writer = new AnyGetterWriter(prop, member, null);

        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.getAndSerialize(new Object(), gen, provider);
        gen.close();
    }

    @Test
    public void testGetAndFilterWithNullValue() throws Exception {
        DummyAnnotatedMember member = new DummyAnnotatedMember("getProps", null);
        MockMapSerializer serializer = new MockMapSerializer();
        BeanProperty prop = new DummyBeanProperty("props");
        AnyGetterWriter writer = new AnyGetterWriter(prop, member, serializer);

        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.getAndFilter(new Object(), gen, provider, null);
        Assert.assertFalse(serializer.serializeFilteredFieldsCalled);
        gen.close();
    }

    @Test
    public void testGetAndFilterWithNonMapValueThrowsException() throws Exception {
        DummyAnnotatedMember member = new DummyAnnotatedMember("getProps", 12345);
        MockMapSerializer serializer = new MockMapSerializer();
        BeanProperty prop = new DummyBeanProperty("props");
        AnyGetterWriter writer = new AnyGetterWriter(prop, member, serializer);

        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        try {
            writer.getAndFilter(new Object(), gen, provider, null);
            Assert.fail("Expected JsonMappingException to be thrown");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Value returned by 'any-getter' (getProps()) not java.util.Map but java.lang.Integer"));
        } finally {
            gen.close();
        }
    }

    @Test
    public void testGetAndFilterWithValidMapAndSerializer() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key2", 42);

        DummyAnnotatedMember member = new DummyAnnotatedMember("getProps", map);
        MockMapSerializer serializer = new MockMapSerializer();
        BeanProperty prop = new DummyBeanProperty("props");
        AnyGetterWriter writer = new AnyGetterWriter(prop, member, serializer);

        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        PropertyFilter filter = new SimpleBeanPropertyFilter() {};
        writer.getAndFilter(new Object(), gen, provider, filter);

        Assert.assertTrue(serializer.serializeFilteredFieldsCalled);
        Assert.assertSame(map, serializer.passedMap);
        Assert.assertSame(filter, serializer.passedFilter);
        gen.close();
    }

    @Test
    public void testGetAndFilterWithNullSerializer() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key2", 42);

        DummyAnnotatedMember member = new DummyAnnotatedMember("getProps", map);
        BeanProperty prop = new DummyBeanProperty("props");
        AnyGetterWriter writer = new AnyGetterWriter(prop, member, null);

        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        writer.getAndFilter(new Object(), gen, provider, null);
        gen.close();
    }

    @Test
    public void testResolveContextualizesSerializer() throws Exception {
        DummyAnnotatedMember member = new DummyAnnotatedMember("getProps", Collections.emptyMap());
        MockMapSerializer initialSerializer = new MockMapSerializer();
        final MockMapSerializer contextualSerializer = new MockMapSerializer();
        BeanProperty prop = new DummyBeanProperty("props");

        AnyGetterWriter writer = new AnyGetterWriter(prop, member, initialSerializer);

        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl(
                (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance()) {
            private static final long serialVersionUID = 1L;

            @Override
            public JsonSerializer<?> handlePrimaryContextualization(JsonSerializer<?> ser, BeanProperty property)
                    throws JsonMappingException {
                return contextualSerializer;
            }

            @Override
            public DefaultSerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
                return this;
            }
        };

        writer.resolve(provider);

        JsonGenerator gen = new JsonFactory().createGenerator(new StringWriter());
        writer.getAndSerialize(new Object(), gen, provider);

        Assert.assertFalse(initialSerializer.serializeFieldsCalled);
        Assert.assertTrue(contextualSerializer.serializeFieldsCalled);
        gen.close();
    }

    @Test
    public void testFullSerializationWithAnyGetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnyGetterBean bean = new AnyGetterBean("foo", "bar");
        String json = mapper.writeValueAsString(bean);
        Assert.assertEquals("{\"foo\":\"bar\"}", json);
    }

    @Test
    public void testFullSerializationWithNullAnyGetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NullAnyGetterBean bean = new NullAnyGetterBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertEquals("{}", json);
    }

    @Test
    public void testFullSerializationWithInvalidAnyGetterType() {
        ObjectMapper mapper = new ObjectMapper();
        InvalidTypeAnyGetterBean bean = new InvalidTypeAnyGetterBean();
        try {
            mapper.writeValueAsString(bean);
            Assert.fail("Expected JsonMappingException for non-Map any getter");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Value returned by 'any-getter' (any()) not java.util.Map but java.lang.String"));
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e);
        }
    }
}
