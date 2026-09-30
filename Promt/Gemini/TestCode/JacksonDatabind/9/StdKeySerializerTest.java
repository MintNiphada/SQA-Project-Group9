package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class StdKeySerializerTest {

    private StdKeySerializer serializer;
    private SerializerProvider serializerProvider;

    @Before
    public void setUp() {
        serializer = new StdKeySerializer();
        ObjectMapper mapper = new ObjectMapper();
        serializerProvider = mapper.getSerializerProviderInstance();
    }

    @Test
    public void testHandledType() {
        assertEquals(Object.class, serializer.handledType());
    }

    @Test
    public void testSerializeDateKey() throws IOException {
        SerializerProvider mockProvider = mock(SerializerProvider.class);
        JsonGenerator mockGenerator = mock(JsonGenerator.class);
        Date testDate = new Date(123456789L);

        serializer.serialize(testDate, mockGenerator, mockProvider);

        verify(mockProvider).defaultSerializeDateKey(testDate, mockGenerator);
        verifyNoMoreInteractions(mockGenerator);
    }

    @Test
    public void testSerializeStringKey() throws IOException {
        SerializerProvider mockProvider = mock(SerializerProvider.class);
        JsonGenerator mockGenerator = mock(JsonGenerator.class);

        serializer.serialize("myKey", mockGenerator, mockProvider);

        verify(mockGenerator).writeFieldName("myKey");
        verifyNoMoreInteractions(mockProvider);
    }

    @Test
    public void testSerializeIntegerKey() throws IOException {
        SerializerProvider mockProvider = mock(SerializerProvider.class);
        JsonGenerator mockGenerator = mock(JsonGenerator.class);

        serializer.serialize(12345, mockGenerator, mockProvider);

        verify(mockGenerator).writeFieldName("12345");
        verifyNoMoreInteractions(mockProvider);
    }

    @Test
    public void testSerializeCustomObjectKey() throws IOException {
        SerializerProvider mockProvider = mock(SerializerProvider.class);
        JsonGenerator mockGenerator = mock(JsonGenerator.class);

        Object customObj = new Object() {
            @Override
            public String toString() {
                return "customObjectKey";
            }
        };

        serializer.serialize(customObj, mockGenerator, mockProvider);

        verify(mockGenerator).writeFieldName("customObjectKey");
        verifyNoMoreInteractions(mockProvider);
    }

    @Test
    public void testSerializeNullKeyThrowsNullPointerException() throws IOException {
        SerializerProvider mockProvider = mock(SerializerProvider.class);
        JsonGenerator mockGenerator = mock(JsonGenerator.class);

        try {
            serializer.serialize(null, mockGenerator, mockProvider);
            fail("Expected NullPointerException when serializing null key");
        } catch (NullPointerException e) {
            // Expected exception
        }
    }

    @Test
    public void testSerializeUsingRealJsonGenerator() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(writer);

        generator.writeStartObject();
        serializer.serialize("testField", generator, serializerProvider);
        generator.writeString("testValue");
        generator.writeEndObject();
        generator.close();

        assertEquals("{\"testField\":\"testValue\"}", writer.toString());
    }

    @Test
    public void testGetSchema() throws JsonMappingException {
        JsonNode schemaNode = serializer.getSchema(serializerProvider, (Type) String.class);
        assertNotNull(schemaNode);
        assertTrue(schemaNode.isObject());
        assertEquals("string", schemaNode.get("type").asText());
    }

    @Test
    public void testGetSchemaWithNullType() throws JsonMappingException {
        JsonNode schemaNode = serializer.getSchema(serializerProvider, (Type) null);
        assertNotNull(schemaNode);
        assertTrue(schemaNode.isObject());
        assertEquals("string", schemaNode.get("type").asText());
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws JsonMappingException {
        JsonFormatVisitorWrapper mockVisitor = mock(JsonFormatVisitorWrapper.class);
        JavaType javaType = TypeFactory.defaultInstance().constructType(String.class);

        serializer.acceptJsonFormatVisitor(mockVisitor, javaType);

        verify(mockVisitor).expectStringFormat(javaType);
    }

    @Test
    public void testAcceptJsonFormatVisitorWithNullType() throws JsonMappingException {
        JsonFormatVisitorWrapper mockVisitor = mock(JsonFormatVisitorWrapper.class);

        serializer.acceptJsonFormatVisitor(mockVisitor, null);

        verify(mockVisitor).expectStringFormat(null);
    }
}
