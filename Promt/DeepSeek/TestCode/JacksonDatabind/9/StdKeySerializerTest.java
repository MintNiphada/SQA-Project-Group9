package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;

@RunWith(MockitoJUnitRunner.class)
public class StdKeySerializerTest {

    @Mock
    private JsonGenerator jgen;
    @Mock
    private SerializerProvider provider;
    @Mock
    private JsonFormatVisitorWrapper visitor;
    @Mock
    private JavaType typeHint;

    private StdKeySerializer serializer;

    @Before
    public void setUp() {
        serializer = new StdKeySerializer();
    }

    @Test
    public void testConstructor() {
        assertNotNull(serializer);
        // The handled type is Object.class, but we can't easily verify without reflection.
        // Just ensure it's created.
    }

    @Test
    public void testSerializeWithDate() throws IOException {
        Date date = new Date();
        serializer.serialize(date, jgen, provider);
        verify(provider).defaultSerializeDateKey(date, jgen);
        verify(jgen, never()).writeFieldName(anyString());
    }

    @Test
    public void testSerializeWithNonDate() throws IOException {
        String value = "testKey";
        serializer.serialize(value, jgen, provider);
        verify(provider, never()).defaultSerializeDateKey(any(Date.class), eq(jgen));
        verify(jgen).writeFieldName(value);
    }

    @Test
    public void testSerializeWithInteger() throws IOException {
        Integer value = 123;
        serializer.serialize(value, jgen, provider);
        verify(provider, never()).defaultSerializeDateKey(any(Date.class), eq(jgen));
        verify(jgen).writeFieldName("123");
    }

    @Test(expected = NullPointerException.class)
    public void testSerializeWithNull() throws IOException {
        serializer.serialize(null, jgen, provider);
    }

    @Test
    public void testGetSchema() throws Exception {
        JsonNode schema = serializer.getSchema(provider, null);
        assertNotNull(schema);
        assertTrue(schema.has("type"));
        assertEquals("string", schema.get("type").asText());
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        serializer.acceptJsonFormatVisitor(visitor, typeHint);
        verify(visitor).expectStringFormat(typeHint);
    }
}
