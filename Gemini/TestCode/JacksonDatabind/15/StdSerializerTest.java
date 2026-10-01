package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

import org.junit.Assert;
import org.junit.Test;

public class StdSerializerTest {

    private static class DummyStdSerializer<T> extends StdSerializer<T> {
        private static final long serialVersionUID = 1L;

        public DummyStdSerializer(Class<T> cls) {
            super(cls);
        }

        public DummyStdSerializer(JavaType type) {
            super(type);
        }

        public DummyStdSerializer(Class<?> cls, boolean dummy) {
            super(cls, dummy);
        }

        @Override
        public void serialize(T value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            // no-op for testing
        }

        @Override
        public ObjectNode createObjectNode() {
            return super.createObjectNode();
        }

        @Override
        public ObjectNode createSchemaNode(String type) {
            return super.createSchemaNode(type);
        }

        @Override
        public ObjectNode createSchemaNode(String type, boolean isOptional) {
            return super.createSchemaNode(type, isOptional);
        }

        @Override
        public boolean isDefaultSerializer(JsonSerializer<?> serializer) {
            return super.isDefaultSerializer(serializer);
        }

        @Override
        public JsonSerializer<?> findConvertingContentSerializer(SerializerProvider provider,
                BeanProperty prop, JsonSerializer<?> existingSerializer)
            throws JsonMappingException
        {
            return super.findConvertingContentSerializer(provider, prop, existingSerializer);
        }

        @Override
        public PropertyFilter findPropertyFilter(SerializerProvider provider,
                Object filterId, Object valueToFilter)
            throws JsonMappingException
        {
            return super.findPropertyFilter(provider, filterId, valueToFilter);
        }
    }

    @JacksonStdImpl
    private static class StdAnnotatedSerializer extends DummyStdSerializer<String> {
        private static final long serialVersionUID = 1L;

        public StdAnnotatedSerializer() {
            super(String.class);
        }
    }

    private static class DummyVisitor extends JsonFormatVisitorWrapper.Base {
        public JavaType visitedType;

        @Override
        public void expectAnyFormat(JavaType type) {
            this.visitedType = type;
        }
    }

    private static class StringToUpperConverter extends StdConverter<String, String> {
        @Override
        public String convert(String value) {
            return value == null ? null : value.toUpperCase();
        }
    }

    @Test
    public void testConstructorsAndHandledType() {
        DummyStdSerializer<String> ser1 = new DummyStdSerializer<>(String.class);
        Assert.assertEquals(String.class, ser1.handledType());

        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);
        DummyStdSerializer<Integer> ser2 = new DummyStdSerializer<>(intType);
        Assert.assertEquals(Integer.class, ser2.handledType());

        DummyStdSerializer<Object> ser3 = new DummyStdSerializer<>(Double.class, true);
        Assert.assertEquals(Double.class, ser3.handledType());
    }

    @Test
    public void testSchemaGeneration() throws Exception {
        DummyStdSerializer<String> ser = new DummyStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        JsonNode schemaNode = ser.getSchema(provider, (Type) String.class);
        Assert.assertNotNull(schemaNode);
        Assert.assertEquals("string", schemaNode.get("type").asText());
        Assert.assertNull(schemaNode.get("required"));

        JsonNode schemaOptional = ser.getSchema(provider, String.class, true);
        Assert.assertEquals("string", schemaOptional.get("type").asText());
        Assert.assertNull(schemaOptional.get("required"));

        JsonNode schemaRequired = ser.getSchema(provider, String.class, false);
        Assert.assertEquals("string", schemaRequired.get("type").asText());
        Assert.assertNotNull(schemaRequired.get("required"));
        Assert.assertTrue(schemaRequired.get("required").asBoolean());
    }

    @Test
    public void testCreateSchemaNode() {
        DummyStdSerializer<String> ser = new DummyStdSerializer<>(String.class);

        ObjectNode node1 = ser.createObjectNode();
        Assert.assertNotNull(node1);
        Assert.assertTrue(node1.isEmpty());

        ObjectNode node2 = ser.createSchemaNode("integer");
        Assert.assertEquals("integer", node2.get("type").asText());
        Assert.assertNull(node2.get("required"));

        ObjectNode node3 = ser.createSchemaNode("number", true);
        Assert.assertEquals("number", node3.get("type").asText());
        Assert.assertNull(node3.get("required"));

        ObjectNode node4 = ser.createSchemaNode("boolean", false);
        Assert.assertEquals("boolean", node4.get("type").asText());
        Assert.assertNotNull(node4.get("required"));
        Assert.assertTrue(node4.get("required").asBoolean());
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        DummyStdSerializer<String> ser = new DummyStdSerializer<>(String.class);
        DummyVisitor visitor = new DummyVisitor();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);

        ser.acceptJsonFormatVisitor(visitor, type);
        Assert.assertSame(type, visitor.visitedType);
    }

    @Test
    public void testWrapAndThrowWithFieldName() {
        DummyStdSerializer<String> ser = new DummyStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        // 1. Error should be rethrown as is
        try {
            ser.wrapAndThrow(provider, new StackOverflowError("test"), this, "field");
            Assert.fail("Expected StackOverflowError");
        } catch (Error e) {
            Assert.assertEquals("test", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e);
        }

        // 2. InvocationTargetException unwrapping to Error
        try {
            InvocationTargetException ite = new InvocationTargetException(new OutOfMemoryError("oom"));
            ser.wrapAndThrow(provider, ite, this, "field");
            Assert.fail("Expected OutOfMemoryError");
        } catch (Error e) {
            Assert.assertEquals("oom", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e);
        }

        // 3. Plain IOException should be rethrown as is
        try {
            ser.wrapAndThrow(provider, new IOException("io-err"), this, "field");
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertEquals("io-err", e.getMessage());
            Assert.assertFalse(e instanceof JsonMappingException);
        }

        // 4. JsonMappingException should be wrapped with path
        try {
            ser.wrapAndThrow(provider, new JsonMappingException("map-err"), this, "field");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getPath().size() > 0);
            Assert.assertEquals("field", e.getPath().get(0).getFieldName());
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }

        // 5. General Exception should be wrapped with path
        try {
            ser.wrapAndThrow(provider, new Exception("general-err"), this, "field");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getPath().size() > 0);
            Assert.assertEquals("field", e.getPath().get(0).getFieldName());
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }

        // 6. WRAP_EXCEPTIONS disabled with RuntimeException
        ObjectMapper mapperNoWrap = new ObjectMapper();
        mapperNoWrap.disable(SerializationFeature.WRAP_EXCEPTIONS);
        SerializerProvider providerNoWrap = mapperNoWrap.getSerializerProviderInstance();
        try {
            ser.wrapAndThrow(providerNoWrap, new IllegalArgumentException("arg-err"), this, "field");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("arg-err", e.getMessage());
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }

        // 7. WRAP_EXCEPTIONS disabled with JsonMappingException
        try {
            ser.wrapAndThrow(providerNoWrap, new JsonMappingException("map-err-nowrap"), this, "field");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertEquals("map-err-nowrap", e.getOriginalMessage());
            Assert.assertTrue(e.getPath().isEmpty());
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }

        // 8. Provider is null (WRAP_EXCEPTIONS defaults to true)
        try {
            ser.wrapAndThrow(null, new IllegalArgumentException("null-prov"), this, "field");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getPath().size() > 0);
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }
    }

    @Test
    public void testWrapAndThrowWithIndex() {
        DummyStdSerializer<String> ser = new DummyStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        // 1. Error
        try {
            ser.wrapAndThrow(provider, new UnknownError("unknown"), this, 2);
            Assert.fail("Expected UnknownError");
        } catch (Error e) {
            Assert.assertEquals("unknown", e.getMessage());
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e);
        }

        // 2. InvocationTargetException unwrapping
        try {
            InvocationTargetException ite = new InvocationTargetException(new IOException("nested-io"));
            ser.wrapAndThrow(provider, ite, this, 3);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertEquals("nested-io", e.getMessage());
            Assert.assertFalse(e instanceof JsonMappingException);
        }

        // 3. JsonMappingException wrapped with index
        try {
            ser.wrapAndThrow(provider, new JsonMappingException("indexed-err"), this, 5);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getPath().size() > 0);
            Assert.assertEquals(5, e.getPath().get(0).getIndex());
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }

        // 4. WRAP_EXCEPTIONS disabled with RuntimeException
        ObjectMapper mapperNoWrap = new ObjectMapper();
        mapperNoWrap.disable(SerializationFeature.WRAP_EXCEPTIONS);
        SerializerProvider providerNoWrap = mapperNoWrap.getSerializerProviderInstance();
        try {
            ser.wrapAndThrow(providerNoWrap, new IllegalStateException("state-err"), this, 1);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertEquals("state-err", e.getMessage());
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }

        // 5. WRAP_EXCEPTIONS disabled with JsonMappingException
        try {
            ser.wrapAndThrow(providerNoWrap, new JsonMappingException("jme-idx"), this, 1);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertEquals("jme-idx", e.getOriginalMessage());
            Assert.assertTrue(e.getPath().isEmpty());
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }

        // 6. Provider is null
        try {
            ser.wrapAndThrow(null, new Exception("null-prov-idx"), this, 7);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertEquals(7, e.getPath().get(0).getIndex());
        } catch (IOException e) {
            Assert.fail("Unexpected IOException: " + e);
        }
    }

    @Test
    public void testIsDefaultSerializer() {
        DummyStdSerializer<String> ser = new DummyStdSerializer<>(String.class);
        Assert.assertFalse(ser.isDefaultSerializer(ser));

        StdAnnotatedSerializer stdSer = new StdAnnotatedSerializer();
        Assert.assertTrue(ser.isDefaultSerializer(stdSer));
    }

    private static class BeanWithContentConverter {
        @com.fasterxml.jackson.databind.annotation.JsonSerialize(contentConverter = StringToUpperConverter.class)
        public String[] values;
    }

    @Test
    public void testFindConvertingContentSerializer() throws Exception {
        DummyStdSerializer<String> ser = new DummyStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        // 1. prop is null
        JsonSerializer<?> existing = ser.findConvertingContentSerializer(provider, null, null);
        Assert.assertNull(existing);

        // 2. property with content converter annotation
        JavaType beanType = mapper.constructType(BeanWithContentConverter.class);
        BeanProperty.Std prop = new BeanProperty.Std(
                PropertyName.construct("values"),
                beanType.containedType(0) != null ? beanType.containedType(0) : TypeFactory.unknownType(),
                null,
                null,
                mapper.getSerializationConfig().introspect(beanType).findProperties().get(0).getField(),
                PropertyMetadata.STD_OPTIONAL
        );

        JsonSerializer<?> result = ser.findConvertingContentSerializer(provider, prop, null);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof StdDelegatingSerializer);

        // 3. when existing serializer is supplied
        JsonSerializer<?> customExisting = new DummyStdSerializer<>(String.class);
        JsonSerializer<?> resultWithExisting = ser.findConvertingContentSerializer(provider, prop, customExisting);
        Assert.assertNotNull(resultWithExisting);
        Assert.assertTrue(resultWithExisting instanceof StdDelegatingSerializer);
        Assert.assertSame(customExisting, ((StdDelegatingSerializer) resultWithExisting).getDelegateSerializer());

        // 4. BeanProperty without converter
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        BeanProperty.Std propNoConv = new BeanProperty.Std(
                PropertyName.construct("name"),
                stringType,
                null,
                null,
                null,
                PropertyMetadata.STD_OPTIONAL
        );
        JsonSerializer<?> unchanged = ser.findConvertingContentSerializer(provider, propNoConv, customExisting);
        Assert.assertSame(customExisting, unchanged);
    }

    @Test
    public void testFindPropertyFilter() throws Exception {
        DummyStdSerializer<String> ser = new DummyStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();

        // 1. FilterProvider not configured -> throws JsonMappingException
        SerializerProvider providerNoFilter = mapper.getSerializerProviderInstance();
        try {
            ser.findPropertyFilter(providerNoFilter, "filterId", "testValue");
            Assert.fail("Expected JsonMappingException when FilterProvider is missing");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("no FilterProvider configured"));
        }

        // 2. FilterProvider configured
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.setFailOnUnknownId(false);
        ObjectMapper mapperWithFilters = new ObjectMapper();
        mapperWithFilters.setFilterProvider(filterProvider);

        SerializerProvider providerWithFilters = mapperWithFilters.getSerializerProviderInstance();
        PropertyFilter filter = ser.findPropertyFilter(providerWithFilters, "unknownFilter", "testValue");
        Assert.assertNull(filter);
    }
}
