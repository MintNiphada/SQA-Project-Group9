package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.Base;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.DoubleSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.FloatSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.IntLikeSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.IntegerSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.LongSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.ShortSerializer;

@RunWith(MockitoJUnitRunner.class)
public class NumberSerializersTest {

    @Mock
    private JsonGenerator gen;
    @Mock
    private SerializerProvider provider;
    @Mock
    private TypeSerializer typeSer;
    @Mock
    private JsonFormatVisitorWrapper visitorWrapper;
    @Mock
    private JsonIntegerFormatVisitor intVisitor;
    @Mock
    private JsonNumberFormatVisitor numVisitor;
    @Mock
    private BeanProperty property;
    @Mock
    private AnnotatedMember member;
    @Mock
    private com.fasterxml.jackson.databind.AnnotationIntrospector introspector;

    @Before
    public void setUp() {
        when(provider.getAnnotationIntrospector()).thenReturn(introspector);
    }

    @Test
    public void testAddAll() {
        Map<String, JsonSerializer<?>> map = new HashMap<String, JsonSerializer<?>>();
        NumberSerializers.addAll(map);
        assertEquals(10, map.size());
        assertSame(IntegerSerializer.class, map.get(Integer.class.getName()).getClass());
        assertSame(IntegerSerializer.class, map.get(Integer.TYPE.getName()).getClass());
        assertSame(LongSerializer.class, map.get(Long.class.getName()).getClass());
        assertSame(LongSerializer.class, map.get(Long.TYPE.getName()).getClass());
        assertSame(IntLikeSerializer.class, map.get(Byte.class.getName()).getClass());
        assertSame(IntLikeSerializer.class, map.get(Byte.TYPE.getName()).getClass());
        assertSame(ShortSerializer.class, map.get(Short.class.getName()).getClass());
        assertSame(ShortSerializer.class, map.get(Short.TYPE.getName()).getClass());
        assertSame(FloatSerializer.class, map.get(Float.class.getName()).getClass());
        assertSame(FloatSerializer.class, map.get(Float.TYPE.getName()).getClass());
        assertSame(DoubleSerializer.class, map.get(Double.class.getName()).getClass());
        assertSame(DoubleSerializer.class, map.get(Double.TYPE.getName()).getClass());
    }

    @Test
    public void testShortSerializerSerialize() throws IOException {
        ShortSerializer serializer = new ShortSerializer();
        short value = 42;
        serializer.serialize(value, gen, provider);
        verify(gen).writeNumber(value);
    }

    @Test
    public void testIntegerSerializerSerialize() throws IOException {
        IntegerSerializer serializer = new IntegerSerializer();
        Integer value = 123;
        serializer.serialize(value, gen, provider);
        verify(gen).writeNumber(value.intValue());
    }

    @Test
    public void testIntegerSerializerSerializeWithType() throws IOException {
        IntegerSerializer serializer = new IntegerSerializer();
        Integer value = 456;
        serializer.serializeWithType(value, gen, provider, typeSer);
        verify(gen).writeNumber(value.intValue());
        verifyNoInteractions(typeSer);
    }

    @Test
    public void testIntLikeSerializerSerialize() throws IOException {
        IntLikeSerializer serializer = new IntLikeSerializer();
        Number value = Byte.valueOf((byte) 7);
        serializer.serialize(value, gen, provider);
        verify(gen).writeNumber(value.intValue());
    }

    @Test
    public void testLongSerializerSerialize() throws IOException {
        LongSerializer serializer = new LongSerializer();
        Long value = 999L;
        serializer.serialize(value, gen, provider);
        verify(gen).writeNumber(value.longValue());
    }

    @Test
    public void testFloatSerializerSerialize() throws IOException {
        FloatSerializer serializer = new FloatSerializer();
        Float value = 3.14f;
        serializer.serialize(value, gen, provider);
        verify(gen).writeNumber(value.floatValue());
    }

    @Test
    public void testDoubleSerializerSerialize() throws IOException {
        DoubleSerializer serializer = new DoubleSerializer();
        Double value = 2.718;
        serializer.serialize(value, gen, provider);
        verify(gen).writeNumber(value.doubleValue());
    }

    @Test
    public void testDoubleSerializerSerializeWithType() throws IOException {
        DoubleSerializer serializer = new DoubleSerializer();
        Double value = 1.618;
        serializer.serializeWithType(value, gen, provider, typeSer);
        verify(gen).writeNumber(value.doubleValue());
        verifyNoInteractions(typeSer);
    }

    @Test
    public void testBaseGetSchema() {
        Base<?> serializer = new ShortSerializer();
        JsonNode schema = serializer.getSchema(provider, null);
        assertNotNull(schema);
        assertTrue(schema.has("type"));
        assertEquals("number", schema.get("type").asText());
        assertTrue(schema.has("required"));
        assertTrue(schema.get("required").asBoolean());
    }

    @Test
    public void testBaseAcceptJsonFormatVisitorInt() throws JsonMappingException {
        Base<?> serializer = new ShortSerializer();
        when(visitorWrapper.expectIntegerFormat(null)).thenReturn(intVisitor);
        serializer.acceptJsonFormatVisitor(visitorWrapper, null);
        verify(visitorWrapper).expectIntegerFormat(null);
        verify(intVisitor).numberType(JsonParser.NumberType.INT);
    }

    @Test
    public void testBaseAcceptJsonFormatVisitorNonInt() throws JsonMappingException {
        Base<?> serializer = new FloatSerializer();
        when(visitorWrapper.expectNumberFormat(null)).thenReturn(numVisitor);
        serializer.acceptJsonFormatVisitor(visitorWrapper, null);
        verify(visitorWrapper).expectNumberFormat(null);
        verify(numVisitor).numberType(JsonParser.NumberType.FLOAT);
    }

    @Test
    public void testBaseCreateContextualPropertyNull() throws JsonMappingException {
        Base<?> serializer = new ShortSerializer();
        JsonSerializer<?> result = serializer.createContextual(provider, null);
        assertSame(serializer, result);
    }

    @Test
    public void testBaseCreateContextualMemberNull() throws JsonMappingException {
        Base<?> serializer = new ShortSerializer();
        when(property.getMember()).thenReturn(null);
        JsonSerializer<?> result = serializer.createContextual(provider, property);
        assertSame(serializer, result);
    }

    @Test
    public void testBaseCreateContextualFormatNull() throws JsonMappingException {
        Base<?> serializer = new ShortSerializer();
        when(property.getMember()).thenReturn(member);
        when(introspector.findFormat(member)).thenReturn(null);
        JsonSerializer<?> result = serializer.createContextual(provider, property);
        assertSame(serializer, result);
    }

    @Test
    public void testBaseCreateContextualShapeString() throws JsonMappingException {
        Base<?> serializer = new ShortSerializer();
        when(property.getMember()).thenReturn(member);
        JsonFormat.Value format = mock(JsonFormat.Value.class);
        when(format.getShape()).thenReturn(JsonFormat.Shape.STRING);
        when(introspector.findFormat(member)).thenReturn(format);
        JsonSerializer<?> result = serializer.createContextual(provider, property);
        assertSame(ToStringSerializer.instance, result);
    }

    @Test
    public void testBaseCreateContextualShapeOther() throws JsonMappingException {
        Base<?> serializer = new ShortSerializer();
        when(property.getMember()).thenReturn(member);
        JsonFormat.Value format = mock(JsonFormat.Value.class);
        when(format.getShape()).thenReturn(JsonFormat.Shape.NUMBER);
        when(introspector.findFormat(member)).thenReturn(format);
        JsonSerializer<?> result = serializer.createContextual(provider, property);
        assertSame(serializer, result);
    }
}
