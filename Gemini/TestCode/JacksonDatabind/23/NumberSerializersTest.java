package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class NumberSerializersTest {

    private ObjectMapper mapper;
    private SerializerProvider serializerProvider;
    private JsonFactory jsonFactory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        serializerProvider = mapper.getSerializerProviderInstance();
        jsonFactory = new JsonFactory();
    }

    @Test
    public void testConstructor() {
        NumberSerializers serializers = new NumberSerializers();
        Assert.assertNotNull(serializers);
    }

    @Test
    public void testAddAll() {
        Map<String, JsonSerializer<?>> map = new HashMap<String, JsonSerializer<?>>();
        NumberSerializers.addAll(map);

        Assert.assertTrue(map.containsKey(Integer.class.getName()));
        Assert.assertTrue(map.containsKey(Integer.TYPE.getName()));
        Assert.assertTrue(map.containsKey(Long.class.getName()));
        Assert.assertTrue(map.containsKey(Long.TYPE.getName()));
        Assert.assertTrue(map.containsKey(Byte.class.getName()));
        Assert.assertTrue(map.containsKey(Byte.TYPE.getName()));
        Assert.assertTrue(map.containsKey(Short.class.getName()));
        Assert.assertTrue(map.containsKey(Short.TYPE.getName()));
        Assert.assertTrue(map.containsKey(Float.class.getName()));
        Assert.assertTrue(map.containsKey(Float.TYPE.getName()));
        Assert.assertTrue(map.containsKey(Double.class.getName()));
        Assert.assertTrue(map.containsKey(Double.TYPE.getName()));

        Assert.assertTrue(map.get(Integer.class.getName()) instanceof NumberSerializers.IntegerSerializer);
        Assert.assertSame(map.get(Integer.class.getName()), map.get(Integer.TYPE.getName()));
        Assert.assertSame(NumberSerializers.LongSerializer.instance, map.get(Long.class.getName()));
        Assert.assertSame(NumberSerializers.LongSerializer.instance, map.get(Long.TYPE.getName()));
        Assert.assertSame(NumberSerializers.IntLikeSerializer.instance, map.get(Byte.class.getName()));
        Assert.assertSame(NumberSerializers.IntLikeSerializer.instance, map.get(Byte.TYPE.getName()));
        Assert.assertSame(NumberSerializers.ShortSerializer.instance, map.get(Short.class.getName()));
        Assert.assertSame(NumberSerializers.ShortSerializer.instance, map.get(Short.TYPE.getName()));
        Assert.assertSame(NumberSerializers.FloatSerializer.instance, map.get(Float.class.getName()));
        Assert.assertSame(NumberSerializers.FloatSerializer.instance, map.get(Float.TYPE.getName()));
        Assert.assertSame(NumberSerializers.DoubleSerializer.instance, map.get(Double.class.getName()));
        Assert.assertSame(NumberSerializers.DoubleSerializer.instance, map.get(Double.TYPE.getName()));
    }

    @Test
    public void testShortSerializer() throws IOException {
        NumberSerializers.ShortSerializer serializer = new NumberSerializers.ShortSerializer();

        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(writer);
        serializer.serialize((short) 42, gen, serializerProvider);
        gen.close();
        Assert.assertEquals("42", writer.toString());

        JsonNode schema = serializer.getSchema(serializerProvider, Short.class);
        Assert.assertEquals("number", schema.get("type").asText());

        final AtomicReference<JsonParser.NumberType> typeRef = new AtomicReference<JsonParser.NumberType>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        typeRef.set(type);
                    }
                };
            }
        };
        JavaType javaType = TypeFactory.defaultInstance().constructType(Short.class);
        serializer.acceptJsonFormatVisitor(visitor, javaType);
        Assert.assertEquals(JsonParser.NumberType.INT, typeRef.get());

        // Test with visitor returning null
        JsonFormatVisitorWrapper nullVisitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return null;
            }
        };
        serializer.acceptJsonFormatVisitor(nullVisitor, javaType);
    }

    @Test
    public void testIntegerSerializer() throws IOException {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer();

        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(writer);
        serializer.serialize(12345, gen, serializerProvider);
        gen.close();
        Assert.assertEquals("12345", writer.toString());

        // Test serializeWithType delegates to serialize
        writer = new StringWriter();
        gen = jsonFactory.createGenerator(writer);
        serializer.serializeWithType(999, gen, serializerProvider, null);
        gen.close();
        Assert.assertEquals("999", writer.toString());

        JsonNode schema = serializer.getSchema(serializerProvider, Integer.class);
        Assert.assertEquals("integer", schema.get("type").asText());

        final AtomicReference<JsonParser.NumberType> typeRef = new AtomicReference<JsonParser.NumberType>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        typeRef.set(type);
                    }
                };
            }
        };
        JavaType javaType = TypeFactory.defaultInstance().constructType(Integer.class);
        serializer.acceptJsonFormatVisitor(visitor, javaType);
        Assert.assertEquals(JsonParser.NumberType.INT, typeRef.get());
    }

    @Test
    public void testIntLikeSerializer() throws IOException {
        NumberSerializers.IntLikeSerializer serializer = NumberSerializers.IntLikeSerializer.instance;

        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(writer);
        serializer.serialize((byte) 8, gen, serializerProvider);
        gen.close();
        Assert.assertEquals("8", writer.toString());

        writer = new StringWriter();
        gen = jsonFactory.createGenerator(writer);
        serializer.serialize(Long.valueOf(100L), gen, serializerProvider);
        gen.close();
        Assert.assertEquals("100", writer.toString());

        JsonNode schema = serializer.getSchema(serializerProvider, Byte.class);
        Assert.assertEquals("integer", schema.get("type").asText());

        final AtomicReference<JsonParser.NumberType> typeRef = new AtomicReference<JsonParser.NumberType>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        typeRef.set(type);
                    }
                };
            }
        };
        JavaType javaType = TypeFactory.defaultInstance().constructType(Byte.class);
        serializer.acceptJsonFormatVisitor(visitor, javaType);
        Assert.assertEquals(JsonParser.NumberType.INT, typeRef.get());
    }

    @Test
    public void testLongSerializer() throws IOException {
        NumberSerializers.LongSerializer serializer = NumberSerializers.LongSerializer.instance;

        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(writer);
        serializer.serialize(9876543210L, gen, serializerProvider);
        gen.close();
        Assert.assertEquals("9876543210", writer.toString());

        JsonNode schema = serializer.getSchema(serializerProvider, Long.class);
        Assert.assertEquals("number", schema.get("type").asText());

        final AtomicReference<JsonParser.NumberType> typeRef = new AtomicReference<JsonParser.NumberType>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        typeRef.set(type);
                    }
                };
            }
        };
        JavaType javaType = TypeFactory.defaultInstance().constructType(Long.class);
        serializer.acceptJsonFormatVisitor(visitor, javaType);
        Assert.assertEquals(JsonParser.NumberType.LONG, typeRef.get());
    }

    @Test
    public void testFloatSerializer() throws IOException {
        NumberSerializers.FloatSerializer serializer = NumberSerializers.FloatSerializer.instance;

        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(writer);
        serializer.serialize(3.25f, gen, serializerProvider);
        gen.close();
        Assert.assertEquals("3.25", writer.toString());

        JsonNode schema = serializer.getSchema(serializerProvider, Float.class);
        Assert.assertEquals("number", schema.get("type").asText());

        final AtomicReference<JsonParser.NumberType> typeRef = new AtomicReference<JsonParser.NumberType>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        typeRef.set(type);
                    }
                };
            }
        };
        JavaType javaType = TypeFactory.defaultInstance().constructType(Float.class);
        serializer.acceptJsonFormatVisitor(visitor, javaType);
        Assert.assertEquals(JsonParser.NumberType.FLOAT, typeRef.get());

        // Test with visitor returning null
        JsonFormatVisitorWrapper nullVisitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return null;
            }
        };
        serializer.acceptJsonFormatVisitor(nullVisitor, javaType);
    }

    @Test
    public void testDoubleSerializer() throws IOException {
        NumberSerializers.DoubleSerializer serializer = NumberSerializers.DoubleSerializer.instance;

        StringWriter writer = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(writer);
        serializer.serialize(12.345, gen, serializerProvider);
        gen.close();
        Assert.assertEquals("12.345", writer.toString());

        // Test serializeWithType delegates to serialize
        writer = new StringWriter();
        gen = jsonFactory.createGenerator(writer);
        serializer.serializeWithType(67.89, gen, serializerProvider, null);
        gen.close();
        Assert.assertEquals("67.89", writer.toString());

        JsonNode schema = serializer.getSchema(serializerProvider, Double.class);
        Assert.assertEquals("number", schema.get("type").asText());

        final AtomicReference<JsonParser.NumberType> typeRef = new AtomicReference<JsonParser.NumberType>();
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        typeRef.set(type);
                    }
                };
            }
        };
        JavaType javaType = TypeFactory.defaultInstance().constructType(Double.class);
        serializer.acceptJsonFormatVisitor(visitor, javaType);
        Assert.assertEquals(JsonParser.NumberType.DOUBLE, typeRef.get());

        // Test with visitor returning null
        JsonFormatVisitorWrapper nullVisitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return null;
            }
        };
        serializer.acceptJsonFormatVisitor(nullVisitor, javaType);
    }

    public static class FormattedIntBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int stringValue = 123;

        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public int numberValue = 456;

        public int defaultValue = 789;
    }

    @Test
    public void testContextualSerialization() throws Exception {
        FormattedIntBean bean = new FormattedIntBean();
        String json = mapper.writeValueAsString(bean);

        Assert.assertTrue(json.contains("\"stringValue\":\"123\""));
        Assert.assertTrue(json.contains("\"numberValue\":456"));
        Assert.assertTrue(json.contains("\"defaultValue\":789"));
    }

    @Test
    public void testCreateContextualDirectly() throws Exception {
        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer();

        // 1. null property
        JsonSerializer<?> resultNullProp = intSer.createContextual(serializerProvider, null);
        Assert.assertSame(intSer, resultNullProp);

        // 2. Property with null member
        BeanProperty.Std propWithoutMember = new BeanProperty.Std(
                new PropertyName("val"),
                TypeFactory.defaultInstance().constructType(Integer.class),
                null,
                null,
                null,
                PropertyMetadata.STD_REQUIRED
        );
        JsonSerializer<?> resultNullMember = intSer.createContextual(serializerProvider, propWithoutMember);
        Assert.assertSame(intSer, resultNullMember);

        // 3. Property with format shape STRING
        JavaType type = mapper.constructType(FormattedIntBean.class);
        BeanProperty stringProp = new BeanProperty.Std(
                new PropertyName("stringValue"),
                TypeFactory.defaultInstance().constructType(Integer.TYPE),
                null,
                null,
                mapper.getSerializationConfig().introspect(type).findProperties().get(0).getPrimaryMember(),
                PropertyMetadata.STD_REQUIRED
        );
        // Find member specifically for stringValue
        AnnotatedMember stringMember = null;
        AnnotatedMember numberMember = null;
        AnnotatedMember defaultMember = null;
        for (com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition p : mapper.getSerializationConfig().introspect(type).findProperties()) {
            if ("stringValue".equals(p.getName())) {
                stringMember = p.getPrimaryMember();
            } else if ("numberValue".equals(p.getName())) {
                numberMember = p.getPrimaryMember();
            } else if ("defaultValue".equals(p.getName())) {
                defaultMember = p.getPrimaryMember();
            }
        }

        BeanProperty propString = new BeanProperty.Std(
                new PropertyName("stringValue"),
                TypeFactory.defaultInstance().constructType(Integer.TYPE),
                null,
                null,
                stringMember,
                PropertyMetadata.STD_REQUIRED
        );
        JsonSerializer<?> resultString = intSer.createContextual(serializerProvider, propString);
        Assert.assertSame(ToStringSerializer.instance, resultString);

        // 4. Property with format shape NUMBER (default branch in switch)
        BeanProperty propNumber = new BeanProperty.Std(
                new PropertyName("numberValue"),
                TypeFactory.defaultInstance().constructType(Integer.TYPE),
                null,
                null,
                numberMember,
                PropertyMetadata.STD_REQUIRED
        );
        JsonSerializer<?> resultNumber = intSer.createContextual(serializerProvider, propNumber);
        Assert.assertSame(intSer, resultNumber);

        // 5. Property with no format annotation (findFormat returns null)
        BeanProperty propDefault = new BeanProperty.Std(
                new PropertyName("defaultValue"),
                TypeFactory.defaultInstance().constructType(Integer.TYPE),
                null,
                null,
                defaultMember,
                PropertyMetadata.STD_REQUIRED
        );
        JsonSerializer<?> resultDefault = intSer.createContextual(serializerProvider, propDefault);
        Assert.assertSame(intSer, resultDefault);
    }
}
