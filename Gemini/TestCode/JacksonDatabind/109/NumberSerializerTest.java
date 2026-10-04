package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberSerializerTest {

    private static class CustomNumber extends Number {
        private final String val;

        public CustomNumber(String val) {
            this.val = val;
        }

        @Override
        public int intValue() {
            return 0;
        }

        @Override
        public long longValue() {
            return 0;
        }

        @Override
        public float floatValue() {
            return 0;
        }

        @Override
        public double doubleValue() {
            return 0;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    @Test
    public void testInstanceNotNull() {
        Assert.assertNotNull(NumberSerializer.instance);
        Assert.assertEquals(Number.class, NumberSerializer.instance.handledType());
    }

    @Test
    public void testConstructor() {
        NumberSerializer intSerializer = new NumberSerializer(BigInteger.class);
        Assert.assertEquals(BigInteger.class, intSerializer.handledType());

        NumberSerializer decSerializer = new NumberSerializer(BigDecimal.class);
        Assert.assertEquals(BigDecimal.class, decSerializer.handledType());
    }

    @Test
    public void testCreateContextualWithoutFormat() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        Mockito.when(provider.getAnnotationIntrospector()).thenReturn(null);
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(null);

        JsonSerializer<?> result = serializer.createContextual(provider, prop);
        Assert.assertSame(serializer, result);
    }

    @Test
    public void testCreateContextualWithFormatString() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        JsonFormat.Value format = JsonFormat.Value.forShape(JsonFormat.Shape.STRING);
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(format);

        JsonSerializer<?> result = serializer.createContextual(provider, prop);
        Assert.assertSame(ToStringSerializer.instance, result);
    }

    @Test
    public void testCreateContextualWithFormatNumber() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        JsonFormat.Value format = JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER);
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(format);

        JsonSerializer<?> result = serializer.createContextual(provider, prop);
        Assert.assertSame(serializer, result);
    }

    @Test
    public void testSerializeBigDecimal() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator g = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        BigDecimal dec = new BigDecimal("123.456");
        serializer.serialize(dec, g, provider);
        Mockito.verify(g).writeNumber(dec);
    }

    @Test
    public void testSerializeBigInteger() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator g = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        BigInteger bigint = new BigInteger("987654321987654321");
        serializer.serialize(bigint, g, provider);
        Mockito.verify(g).writeNumber(bigint);
    }

    @Test
    public void testSerializeLong() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator g = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        Long val = 123456789L;
        serializer.serialize(val, g, provider);
        Mockito.verify(g).writeNumber(123456789L);
    }

    @Test
    public void testSerializeDouble() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator g = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        Double val = 3.14159;
        serializer.serialize(val, g, provider);
        Mockito.verify(g).writeNumber(3.14159);
    }

    @Test
    public void testSerializeFloat() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator g = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        Float val = 1.25f;
        serializer.serialize(val, g, provider);
        Mockito.verify(g).writeNumber(1.25f);
    }

    @Test
    public void testSerializeInteger() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator g = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        Integer val = 42;
        serializer.serialize(val, g, provider);
        Mockito.verify(g).writeNumber(42);
    }

    @Test
    public void testSerializeByte() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator g = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        Byte val = (byte) 7;
        serializer.serialize(val, g, provider);
        Mockito.verify(g).writeNumber(7);
    }

    @Test
    public void testSerializeShort() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator g = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        Short val = (short) 1024;
        serializer.serialize(val, g, provider);
        Mockito.verify(g).writeNumber(1024);
    }

    @Test
    public void testSerializeCustomNumber() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator g = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        CustomNumber custom = new CustomNumber("9999999999999999999999999");
        serializer.serialize(custom, g, provider);
        Mockito.verify(g).writeNumber("9999999999999999999999999");
    }

    @Test
    public void testGetSchemaForBigInteger() {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        JsonNode schema = serializer.getSchema(provider, BigInteger.class);
        Assert.assertNotNull(schema);
        Assert.assertEquals("integer", schema.get("type").asText());
    }

    @Test
    public void testGetSchemaForOtherNumbers() {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);

        JsonNode schema = serializer.getSchema(provider, BigDecimal.class);
        Assert.assertNotNull(schema);
        Assert.assertEquals("number", schema.get("type").asText());

        JsonNode schemaInst = NumberSerializer.instance.getSchema(provider, Number.class);
        Assert.assertNotNull(schemaInst);
        Assert.assertEquals("number", schemaInst.get("type").asText());
    }

    @Test
    public void testAcceptJsonFormatVisitorBigInteger() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JsonIntegerFormatVisitor intVisitor = Mockito.mock(JsonIntegerFormatVisitor.class);
        JavaType type = TypeFactory.defaultInstance().constructType(BigInteger.class);

        Mockito.when(visitor.expectIntegerFormat(type)).thenReturn(intVisitor);

        serializer.acceptJsonFormatVisitor(visitor, type);
        Mockito.verify(visitor).expectIntegerFormat(type);
        Mockito.verify(intVisitor).numberType(JsonParser.NumberType.BIG_INTEGER);
    }

    @Test
    public void testAcceptJsonFormatVisitorBigDecimal() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JsonNumberFormatVisitor numVisitor = Mockito.mock(JsonNumberFormatVisitor.class);
        JavaType type = TypeFactory.defaultInstance().constructType(BigDecimal.class);

        Mockito.when(visitor.expectNumberFormat(type)).thenReturn(numVisitor);

        serializer.acceptJsonFormatVisitor(visitor, type);
        Mockito.verify(visitor).expectNumberFormat(type);
        Mockito.verify(numVisitor).numberType(JsonParser.NumberType.BIG_DECIMAL);
    }

    @Test
    public void testAcceptJsonFormatVisitorGenericNumber() throws Exception {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JsonNumberFormatVisitor numVisitor = Mockito.mock(JsonNumberFormatVisitor.class);
        JavaType type = TypeFactory.defaultInstance().constructType(Number.class);

        Mockito.when(visitor.expectNumberFormat(type)).thenReturn(numVisitor);

        serializer.acceptJsonFormatVisitor(visitor, type);
        Mockito.verify(visitor).expectNumberFormat(type);
        Mockito.verify(numVisitor, Mockito.never()).numberType(Mockito.any());
    }
}
