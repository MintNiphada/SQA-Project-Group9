package com.fasterxml.jackson.databind.ser.std;

import java.io.StringWriter;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.EnumValues;

public class EnumSerializerTest {

    enum TestEnum {
        A, B {
            @Override
            public String toString() {
                return "custom_b";
            }
        };
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("deprecation")
    public void testDeprecatedConstructor() {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer ser = new EnumSerializer(values);
        Assert.assertNotNull(ser.getEnumValues());
        Assert.assertSame(values, ser.getEnumValues());
        Assert.assertEquals(TestEnum.class, ser.handledType());
    }

    @Test
    public void testConstructFactory() {
        SerializationConfig config = mapper.getSerializationConfig();
        BeanDescription beanDesc = config.introspectClassAnnotations(TestEnum.class);
        JsonFormat.Value format = JsonFormat.Value.empty().withShape(Shape.NUMBER_INT);
        EnumSerializer ser = EnumSerializer.construct(TestEnum.class, config, beanDesc, format);
        Assert.assertNotNull(ser);
        Assert.assertSame(TestEnum.class, ser.handledType());
    }

    @Test
    public void testIsShapeWrittenUsingIndexBranches() {
        Assert.assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, null, true));
        Assert.assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.ANY), true));
        Assert.assertNull(EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.SCALAR), true));

        Assert.assertEquals(Boolean.FALSE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.STRING), true));
        Assert.assertEquals(Boolean.FALSE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.NATURAL), true));

        Assert.assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.NUMBER), true));
        Assert.assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.NUMBER_INT), true));
        Assert.assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.NUMBER_FLOAT), true));
        Assert.assertEquals(Boolean.TRUE, EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.ARRAY), true));

        try {
            EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.OBJECT), true);
            Assert.fail("Should have failed on Shape.OBJECT");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("class annotation"));
        }

        try {
            EnumSerializer._isShapeWrittenUsingIndex(TestEnum.class, JsonFormat.Value.empty().withShape(Shape.OBJECT), false);
            Assert.fail("Should have failed on Shape.OBJECT");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("property annotation"));
        }
    }

    @Test
    public void testCreateContextual() throws JsonMappingException {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer ser = new EnumSerializer(values, null);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        Assert.assertSame(ser, ser.createContextual(prov, null));

        BeanProperty.Bogus bogusProp = new BeanProperty.Bogus() {
            private static final long serialVersionUID = 1L;
            @Override
            public JavaType getType() {
                return TypeFactory.defaultInstance().constructType(TestEnum.class);
            }
        };

        JsonSerializer<?> contextual = ser.createContextual(prov, bogusProp);
        Assert.assertSame(ser, contextual);
    }

    @Test
    public void testSerializeAsIndexConfigured() throws Exception {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer serIndex = new EnumSerializer(values, Boolean.TRUE);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serIndex.serialize(TestEnum.B, gen, prov);
        gen.flush();
        Assert.assertEquals("1", sw.toString());
    }

    @Test
    public void testSerializeAsIndexDynamic() throws Exception {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer ser = new EnumSerializer(values, null);

        ObjectMapper mapperIndex = new ObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        SerializerProvider prov = mapperIndex.getSerializerProviderInstance();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ser.serialize(TestEnum.A, gen, prov);
        gen.flush();
        Assert.assertEquals("0", sw.toString());
    }

    @Test
    public void testSerializeUsingToString() throws Exception {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer ser = new EnumSerializer(values, null);

        ObjectMapper mapperToString = new ObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        SerializerProvider prov = mapperToString.getSerializerProviderInstance();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ser.serialize(TestEnum.B, gen, prov);
        gen.flush();
        Assert.assertEquals("\"custom_b\"", sw.toString());
    }

    @Test
    public void testSerializeDefaultName() throws Exception {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);

        SerializerProvider prov = mapper.getSerializerProviderInstance();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ser.serialize(TestEnum.B, gen, prov);
        gen.flush();
        Assert.assertEquals("\"B\"", sw.toString());
    }

    @Test
    public void testGetSchema() {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer serIndex = new EnumSerializer(values, Boolean.TRUE);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonNode schemaIndex = serIndex.getSchema(prov, TestEnum.class);
        Assert.assertEquals("integer", schemaIndex.get("type").asText());

        EnumSerializer serString = new EnumSerializer(values, Boolean.FALSE);
        JsonNode schemaNullType = serString.getSchema(prov, null);
        Assert.assertEquals("string", schemaNullType.get("type").asText());
        Assert.assertNull(schemaNullType.get("enum"));

        JsonNode schemaNonEnum = serString.getSchema(prov, String.class);
        Assert.assertEquals("string", schemaNonEnum.get("type").asText());
        Assert.assertNull(schemaNonEnum.get("enum"));

        JsonNode schemaEnum = serString.getSchema(prov, TestEnum.class);
        Assert.assertEquals("string", schemaEnum.get("type").asText());
        ArrayNode enumNode = (ArrayNode) schemaEnum.get("enum");
        Assert.assertNotNull(enumNode);
        Assert.assertEquals(2, enumNode.size());
        Assert.assertEquals("A", enumNode.get(0).asText());
        Assert.assertEquals("B", enumNode.get(1).asText());
    }

    @Test
    public void testAcceptJsonFormatVisitorIndex() throws JsonMappingException {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.TRUE);
        final boolean[] visitedInt = new boolean[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        if (type == JsonParser.NumberType.INT) {
                            visitedInt[0] = true;
                        }
                    }
                };
            }
        };

        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(TestEnum.class));
        Assert.assertTrue(visitedInt[0]);
    }

    @Test
    public void testAcceptJsonFormatVisitorStringDefault() throws JsonMappingException {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);
        final Set[] visitedEnums = new Set[1];

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        visitedEnums[0] = enums;
                    }
                };
            }
        };

        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(TestEnum.class));
        Assert.assertNotNull(visitedEnums[0]);
        Assert.assertTrue(visitedEnums[0].contains("A"));
        Assert.assertTrue(visitedEnums[0].contains("B"));
    }

    @Test
    public void testAcceptJsonFormatVisitorStringToString() throws JsonMappingException {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);
        final Set[] visitedEnums = new Set[1];

        ObjectMapper mapperToString = new ObjectMapper().enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapperToString.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void enumTypes(Set<String> enums) {
                        visitedEnums[0] = enums;
                    }
                };
            }
        };

        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(TestEnum.class));
        Assert.assertNotNull(visitedEnums[0]);
        Assert.assertTrue(visitedEnums[0].contains("A"));
        Assert.assertTrue(visitedEnums[0].contains("custom_b"));
    }

    @Test
    public void testAcceptJsonFormatVisitorNullStringVisitor() throws JsonMappingException {
        EnumValues values = EnumValues.constructFromName(mapper.getSerializationConfig(), TestEnum.class);
        EnumSerializer ser = new EnumSerializer(values, Boolean.FALSE);

        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return null;
            }
        };

        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(TestEnum.class));
    }
}
