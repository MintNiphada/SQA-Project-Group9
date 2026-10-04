package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

public class NumberSerializerTest {

    @Test
    public void testSerializeBigDecimal() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        BigDecimal value = new BigDecimal("123.456");

        serializer.serialize(value, gen, provider);
        Mockito.verify(gen).writeNumber(value);
    }

    @Test
    public void testSerializeBigInteger() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        BigInteger value = new BigInteger("12345678901234567890");

        serializer.serialize(value, gen, provider);
        Mockito.verify(gen).writeNumber(value);
    }

    @Test
    public void testSerializeInteger() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        Integer value = 42;

        serializer.serialize(value, gen, provider);
        Mockito.verify(gen).writeNumber(42);
    }

    @Test
    public void testSerializeLong() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        Long value = 9876543210L;

        serializer.serialize(value, gen, provider);
        Mockito.verify(gen).writeNumber(9876543210L);
    }

    @Test
    public void testSerializeDouble() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        Double value = 3.14159;

        serializer.serialize(value, gen, provider);
        Mockito.verify(gen).writeNumber(3.14159);
    }

    @Test
    public void testSerializeFloat() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        Float value = 1.23f;

        serializer.serialize(value, gen, provider);
        Mockito.verify(gen).writeNumber(1.23f);
    }

    @Test
    public void testSerializeByte() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        Byte value = (byte) 8;

        serializer.serialize(value, gen, provider);
        Mockito.verify(gen).writeNumber(8);
    }

    @Test
    public void testSerializeShort() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        Short value = (short) 16;

        serializer.serialize(value, gen, provider);
        Mockito.verify(gen).writeNumber(16);
    }

    @Test
    public void testSerializeCustomNumberFallback() throws IOException {
        NumberSerializer serializer = NumberSerializer.instance;
        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider provider = Mockito.mock(SerializerProvider.class);
        AtomicInteger value = new AtomicInteger(100);

        serializer.serialize(value, gen, provider);
        Mockito.verify(gen).writeNumber("100");
    }

    @Test
    public void testGetSchemaForNumber() {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        JsonNode schema = serializer.getSchema(null, null);
        Assert.assertNotNull(schema);
        Assert.assertEquals("number", schema.get("type").asText());
        Assert.assertTrue(schema.get("required").asBoolean());
    }

    @Test
    public void testGetSchemaForBigInteger() {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        JsonNode schema = serializer.getSchema(null, null);
        Assert.assertNotNull(schema);
        Assert.assertEquals("integer", schema.get("type").asText());
        Assert.assertTrue(schema.get("required").asBoolean());
    }

    @Test
    public void testAcceptJsonFormatVisitorForBigInteger() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JsonIntegerFormatVisitor intVisitor = Mockito.mock(JsonIntegerFormatVisitor.class);
        JavaType typeHint = TypeFactory.defaultInstance().constructType(BigInteger.class);

        Mockito.when(visitor.expectIntegerFormat(typeHint)).thenReturn(intVisitor);

        serializer.acceptJsonFormatVisitor(visitor, typeHint);
        Mockito.verify(visitor).expectIntegerFormat(typeHint);
        Mockito.verify(intVisitor).numberType(JsonParser.NumberType.BIG_INTEGER);
    }

    @Test
    public void testAcceptJsonFormatVisitorForBigDecimal() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JsonNumberFormatVisitor numVisitor = Mockito.mock(JsonNumberFormatVisitor.class);
        JavaType typeHint = TypeFactory.defaultInstance().constructType(BigDecimal.class);

        Mockito.when(visitor.expectNumberFormat(typeHint)).thenReturn(numVisitor);

        serializer.acceptJsonFormatVisitor(visitor, typeHint);
        Mockito.verify(visitor).expectNumberFormat(typeHint);
        Mockito.verify(numVisitor).numberType(JsonParser.NumberType.BIG_INTEGER);
    }

    @Test
    public void testAcceptJsonFormatVisitorForGenericNumber() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JavaType typeHint = TypeFactory.defaultInstance().constructType(Number.class);

        serializer.acceptJsonFormatVisitor(visitor, typeHint);
        Mockito.verify(visitor).expectNumberFormat(typeHint);
    }
}
