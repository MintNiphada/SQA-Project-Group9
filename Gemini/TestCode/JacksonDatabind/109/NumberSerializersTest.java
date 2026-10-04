package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class NumberSerializersTest {

    private final JsonFactory jsonFactory = new JsonFactory();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testConstructor() {
        NumberSerializers serializers = new NumberSerializers();
        Assert.assertNotNull(serializers);
    }

    @Test
    public void testAddAll() {
        Map<String, JsonSerializer<?>> map = new HashMap<String, JsonSerializer<?>>();
        NumberSerializers.addAll(map);

        Assert.assertEquals(12, map.size());
        Assert.assertTrue(map.get(Integer.class.getName()) instanceof NumberSerializers.IntegerSerializer);
        Assert.assertTrue(map.get(Integer.TYPE.getName()) instanceof NumberSerializers.IntegerSerializer);
        Assert.assertTrue(map.get(Long.class.getName()) instanceof NumberSerializers.LongSerializer);
        Assert.assertTrue(map.get(Long.TYPE.getName()) instanceof NumberSerializers.LongSerializer);
        Assert.assertTrue(map.get(Byte.class.getName()) instanceof NumberSerializers.IntLikeSerializer);
        Assert.assertTrue(map.get(Byte.TYPE.getName()) instanceof NumberSerializers.IntLikeSerializer);
        Assert.assertTrue(map.get(Short.class.getName()) instanceof NumberSerializers.ShortSerializer);
        Assert.assertTrue(map.get(Short.TYPE.getName()) instanceof NumberSerializers.ShortSerializer);
        Assert.assertTrue(map.get(Double.class.getName()) instanceof NumberSerializers.DoubleSerializer);
        Assert.assertTrue(map.get(Double.TYPE.getName()) instanceof NumberSerializers.DoubleSerializer);
        Assert.assertTrue(map.get(Float.class.getName()) instanceof NumberSerializers.FloatSerializer);
        Assert.assertTrue(map.get(Float.TYPE.getName()) instanceof NumberSerializers.FloatSerializer);
    }

    @Test
    public void testShortSerializer() throws IOException {
        NumberSerializers.ShortSerializer ser = NumberSerializers.ShortSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        ser.serialize((short) 123, gen, mapper.getSerializerProviderInstance());
        gen.flush();
        Assert.assertEquals("123", sw.toString());

        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), (Type) Short.class);
        Assert.assertEquals("number", schema.get("type").asText());

        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visited[0] = true;
                return new JsonIntegerFormatVisitor.Base();
            }
        };
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Short.class));
        Assert.assertTrue(visited[0]);
    }

    @Test
    public void testIntegerSerializer() throws IOException {
        NumberSerializers.IntegerSerializer ser = new NumberSerializers.IntegerSerializer(Integer.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        ser.serialize(456, gen, mapper.getSerializerProviderInstance());
        gen.flush();
        Assert.assertEquals("456", sw.toString());

        sw = new StringWriter();
        gen = jsonFactory.createGenerator(sw);
        ser.serializeWithType(789, gen, mapper.getSerializerProviderInstance(), null);
        gen.flush();
        Assert.assertEquals("789", sw.toString());

        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), (Type) Integer.class);
        Assert.assertEquals("integer", schema.get("type").asText());

        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visited[0] = true;
                return new JsonIntegerFormatVisitor.Base();
            }
        };
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Integer.class));
        Assert.assertTrue(visited[0]);
    }

    @Test
    public void testIntLikeSerializer() throws IOException {
        NumberSerializers.IntLikeSerializer ser = NumberSerializers.IntLikeSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        ser.serialize((byte) 42, gen, mapper.getSerializerProviderInstance());
        gen.flush();
        Assert.assertEquals("42", sw.toString());

        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), (Type) Byte.class);
        Assert.assertEquals("integer", schema.get("type").asText());

        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visited[0] = true;
                return new JsonIntegerFormatVisitor.Base();
            }
        };
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Byte.class));
        Assert.assertTrue(visited[0]);
    }

    @Test
    public void testLongSerializer() throws IOException {
        NumberSerializers.LongSerializer ser = new NumberSerializers.LongSerializer(Long.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        ser.serialize(1234567890123L, gen, mapper.getSerializerProviderInstance());
        gen.flush();
        Assert.assertEquals("1234567890123", sw.toString());

        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), (Type) Long.class);
        Assert.assertEquals("number", schema.get("type").asText());

        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visited[0] = true;
                return new JsonIntegerFormatVisitor.Base();
            }
        };
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Long.class));
        Assert.assertTrue(visited[0]);
    }

    @Test
    public void testFloatSerializer() throws IOException {
        NumberSerializers.FloatSerializer ser = NumberSerializers.FloatSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        ser.serialize(1.25f, gen, mapper.getSerializerProviderInstance());
        gen.flush();
        Assert.assertEquals("1.25", sw.toString());

        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), (Type) Float.class);
        Assert.assertEquals("number", schema.get("type").asText());

        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                visited[0] = true;
                return new JsonNumberFormatVisitor.Base();
            }
        };
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Float.class));
        Assert.assertTrue(visited[0]);
    }

    @Test
    public void testDoubleSerializer() throws IOException {
        NumberSerializers.DoubleSerializer ser = new NumberSerializers.DoubleSerializer(Double.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        ser.serialize(3.14159, gen, mapper.getSerializerProviderInstance());
        gen.flush();
        Assert.assertEquals("3.14159", sw.toString());

        sw = new StringWriter();
        gen = jsonFactory.createGenerator(sw);
        ser.serializeWithType(2.71828, gen, mapper.getSerializerProviderInstance(), null);
        gen.flush();
        Assert.assertEquals("2.71828", sw.toString());

        JsonNode schema = ser.getSchema(mapper.getSerializerProviderInstance(), (Type) Double.class);
        Assert.assertEquals("number", schema.get("type").asText());

        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                visited[0] = true;
                return new JsonNumberFormatVisitor.Base();
            }
        };
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Double.class));
        Assert.assertTrue(visited[0]);
    }

    @Test
    public void testBaseCustomImplementationForBigInteger() throws JsonMappingException {
        NumberSerializers.Base<Object> customBase = new NumberSerializers.Base<Object>(
                Number.class, JsonParser.NumberType.BIG_INTEGER, "integer") {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) {
            }
        };

        Assert.assertTrue(customBase._isInt);
        Assert.assertEquals("integer", customBase._schemaType);
        Assert.assertEquals(JsonParser.NumberType.BIG_INTEGER, customBase._numberType);

        final boolean[] visited = new boolean[1];
        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                visited[0] = true;
                return new JsonIntegerFormatVisitor.Base();
            }
        };
        customBase.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Number.class));
        Assert.assertTrue(visited[0]);
    }

    static class FormattedNumberBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int stringInt = 10;

        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public int normalInt = 20;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public double stringDouble = 3.5;
    }

    @Test
    public void testCreateContextualWithStringShape() throws Exception {
        FormattedNumberBean bean = new FormattedNumberBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"stringInt\":\"10\""));
        Assert.assertTrue(json.contains("\"normalInt\":20"));
        Assert.assertTrue(json.contains("\"stringDouble\":\"3.5\""));
    }

    @Test
    public void testCreateContextualDirect() throws Exception {
        NumberSerializers.IntegerSerializer ser = new NumberSerializers.IntegerSerializer(Integer.class);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        BeanProperty.Std propString = new BeanProperty.Std(
                PropertyName.construct("value"),
                TypeFactory.defaultInstance().constructType(Integer.class),
                null,
                null,
                PropertyMetadata.STD_REQUIRED) {
            @Override
            public JsonFormat.Value findPropertyFormat(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, Class<?> targetType) {
                return JsonFormat.Value.forShape(JsonFormat.Shape.STRING);
            }
        };

        JsonSerializer<?> contextualSer = ser.createContextual(prov, propString);
        Assert.assertTrue(contextualSer instanceof ToStringSerializer);

        BeanProperty.Std propNumber = new BeanProperty.Std(
                PropertyName.construct("value"),
                TypeFactory.defaultInstance().constructType(Integer.class),
                null,
                null,
                PropertyMetadata.STD_REQUIRED) {
            @Override
            public JsonFormat.Value findPropertyFormat(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, Class<?> targetType) {
                return JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER);
            }
        };

        JsonSerializer<?> sameSer = ser.createContextual(prov, propNumber);
        Assert.assertSame(ser, sameSer);

        JsonSerializer<?> nullPropSer = ser.createContextual(prov, null);
        Assert.assertSame(ser, nullPropSer);
    }
}
