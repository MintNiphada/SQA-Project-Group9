package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Type;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.util.Converter;

public class StdDelegatingSerializerTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testConstructorWithConverterOnly() {
        Converter<Object, Object> converter = mock(Converter.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter);
        assertSame(converter, ser.getConverter());
        assertNull(ser.getDelegatee());
        assertEquals(Object.class, ser.handledType());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConstructorWithClassAndConverter() {
        Converter<String, Integer> converter = mock(Converter.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(String.class, converter);
        assertSame(converter, ser.getConverter());        assertNull(ser.getDelegatee());    }

    @SuppressWarnings("unchecked")    @Test
    public void testConstructorWithAllArgs() {
        Converter<Object, Object> converter = mock(Converter.class);
        JavaType delegateType = mock(JavaType.class);
        JsonSerializer<Object> delegateSerializer = mock(JsonSerializer.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, delegateType, delegateSerializer);
        assertSame(converter, ser.getConverter());        assertSame(delegateSerializer, ser.getDelegatee());    }

    @Test    public void testResolveWithDelegateSerializerNull() throws JsonMappingException {
        StdDelegatingSerializer ser = new StdDelegatingSerializer(mock(Converter.class));
        SerializerProvider provider = mock(SerializerProvider.class);
        ser.resolve(provider);
        // no exception, didn't call anything on null        verifyNoMoreInteractions(provider);
    }

    @Test    public void testResolveWithDelegateSerializerNotResolvable() throws JsonMappingException {
        JsonSerializer<Object> delegate = mock(JsonSerializer.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(mock(Converter.class), mock(JavaType.class), delegate);
        SerializerProvider provider = mock(SerializerProvider.class);
        ser.resolve(provider);
        verify(delegate, never()).resolve(any(SerializerProvider.class));
    }

    @Test    public void testResolveWithDelegateSerializerResolvable() throws JsonMappingException {
        ResolvableSerializer delegate = mock(ResolvableSerializer.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(mock(Converter.class), mock(JavaType.class), (JsonSerializer<Object>) delegate);
        SerializerProvider provider = mock(SerialzerProvider.class);
        ser.resolve(provider);
        verify(delegate).resolve(provider);
    }

    @Test    public void testCreateContextualDelegateSerNotNullNotContextual() throws JsonMappingException {
        JsonSerializer<Object> delSer = mock(JsonSerializer.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(mock(Converter.class), mock(JavaType.class), delSer);
        SerializerProvider provider = mock(SerialzierProvider.class);
        BeanProperty prop = mock(BeanProperty.class);
        JsonSerializer<?> result = ser.createContextual(provider, prop);
        assertSame(ser, result);
    }

    @Test    public void testCreateContextualDelegateSerNotNullContextualSame() throws JsonMappingException {
        ContextualSerializer delSer = mock(ContextualSerializer.class, withSettings().extraInterfaces(JsonSerializer.class));
        JsonSerializer<?> delSerAsSer = (JsonSerializer<?>) delSer;
        StdDelegatingSerializer ser = new StdDelegatingSerializer(mock(Converter.class), mock(JavaType.class), delSerAsSer;
        SerialierProvider provider = mock(SerialierProvider.class);
        BeanProperty prop = mock(BeanProperty.class);
        when(provider.handleSecondaryContextualization(delSerAsSer, prop)).thenReturn(delSerAsSer);
        JsonSerializer<?> result = ser.createContextual(provider, prop);
        assertSame(ser, result);
    }

    @Test    public void testCreateContextualDelegateSerNotNullContextualDifferent() throws JsonMappingException {
        ContextualSerializer delSer = mock(ContextualSerializer.class, withSettings().extraInterfaces(JsonSerializer.class));
        JsonSerializer<?> delSerAsSer = (JsonSerializer<?>) delSer;
        StdDelegatingSerializer ser = new StdDelegatingSerializer(mock(Converter.class), mock(JavaType.class), delSerAsSer;
        SerialierProvider provider = mock(SerialierProvider.class);
        BeanProperty prop = mock(BeanProperty.class);
        JsonSerializer<?> newSer = mock(JsonSerializer.class);
        when(provider.handleSecondaryContextualization(delSerAsSer, prop)).thenReturn(newSer);
        JsonSerializer<?> result = ser.createContextual(provider, prop);
        assertNotSame(ser, result);
        assertTrue(result instanceof StdDelegatingSerializer);
        StdDelegatingSerializer newDeleg = (StdDelegatingSerializer) result;
        assertSame(ser.getConverter(), newDeleg.getConverter());
        assertSame(newSer, newDeleg.getDelegatee());
    }

    @Test    public void testCreateContextualDelegateSerNullDelegateTypeNull() throws JsonMappingException {
        Converter<Object, Object> converter = mock(Converter.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter);
        SerialierProvider provider = mock(SerialierProvider.class);
        TypeFactory typeFactory = mock(TypeFactory.class);
        when(provider.getTypeFactory()).thenReturn(typeFactory);
        JavaType outputType = mock(JavaType.class);
        when(converter.getOutputType(typeFactory)).thenReturn(outputType);
        JsonSerializer<Object> foundSer = mock(JsonSerializer.class);
        when(provider.findValueSerializer(outputType)).thenReturn(foundSer);
        BeanProperty prop = mock(BeanProperty.class);
        JsonSerializer<?> result = ser.createContextual(provider, prop);
        assertNotSame(ser, result);
        StdDelegatingSerializer newSer = (StdDelegatingSerializer) result;
        assertSame(foundSer, newSer.getDelegatee());
        assertNotNull(newSer.getDelegatee());
    }

    @Test    public void testCreateContextualDelegateSerNullDelegateTypeNotNull() throws JsonMappingException {
        Converter<Object, Object> converter = mock(Converter.class);
        JavaType delegateType = mock(JavaType.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, delegateType, null);
        SerialierProvider provider = mock(SerialierProvider.class);
        JsonSerializer<Object> foundSer = mock(JsonSerializer.class);
        when(provider.findValueSerializer(delegateType)).thenReturn(foundSer);
        BeanProperty prop = mock(BeanProperty.class);
        JsonSerializer<?> result = ser.createContextual(provider, prop);
        assertNotSame(ser, result);
        StdDelegatingSerializer newSer = (StdDelegatingSerializer) result;
        assertSame(foundSer, newSer.getDelegatee());
    }

    @Test    public void testCreateContextualDelegateSerNullFoundNull() throws JsonMappingException {
        Converter<Object, Object> converter = mock(Converter.class);
        JavaType delegateType = mock(JavaType.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, delegateType, null);
        SerialierProvider provider = mock(SerialierProvider.class);
        when(provider.findValueSerializer(delegateType)).thenReturn(null);
        BeanProperty prop = mock(BeanProperty.class);
        JsonSerializer<?> result = ser.createContextual(provider, prop);
        assertSame(ser, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testWithDelegateOnSubclassThrows() {
        Converter<Object, Object> converter = mock(Converter.class);
        StdDelegatingSerializer subclass = new StdDelegatingSerializer(converter) {
            // not overriding withDelegate
        };
        subclass.withDelegate(converter, mock(JavaType.class), mock(JsonSerializer.class));
    }

    @Test    public void testWithDelegateOnBaseClass() {
        Converter<Object, Object> converter = mock(Converter.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter);
        JavaType delegateType = mock(JavaType.class);
        JsonSerializer<Object> delegateSer = mock(JsonSerializer.class);
        StdDelegatingSerializer newSer = ser.withDelegate(converter, delegateType, delegateSer);
        assertNotNull(newSer);
        assertNotSame(ser, newSer);
        assertSame(converter, newSer.getConverter());
        assertSame(delegateSer, newSer.getDelegatee());
    }

    @Test    public void testSerializeNullDelegateValue() throws Exception {
        Converter<Object, Object> converter = mock(Converter.class);
        when(converter.convert(any())).thenReturn(null);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, mock(JavaType.class), mock(JsonSerializer.class));
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        ser.serialize(new Object(), gen, provider);
        verify(provider).defaultSerializeNull(gen);
        verifyNoMoreInteractions(gen);
    }

    @Test    public void testSerializeNonNullDelegateValue() throws Exception {
        Converter<Object, Object> converter = mock(Converter.class);
        Object delegateValue = new Object();
        when(converter.convert(any())).thenReturn(delegateValue);
        JsonSerializer<Object> delegateSer = mock(JsonSerializer.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, mock(JavaType.class), delegateSer);
        Object value = new Object();
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        ser.serialize(value, gen, provider);
        verify(delegateSer).serialize(delegateValue, gen, provider);
    }

    @Test    public void testSerializeWithType() throws Exception {
        Converter<Object, Object> converter = mock(Converter.class);
        Object delegateValue = new Object();
        when(converter.convert(any())).thenReturn(delegateValue);
        JsonSerializer<Object> delegateSer = mock(JsonSerializer.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, mock(JavaType.class), delegateSer);
        Object value = new Object();
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        TypeSerializer typeSer = mock(TypeSerializer.class);
        ser.serializeWithType(value, gen, provider, typeSer);
        verify(delegateSer).serializeWithType(delegateValue, gen, provider, typeSer);
    }

    @Test    public void testIsEmptyObject() {
        Converter<Object, Object> converter = mock(Converter.class);
        Object delegateValue = new Object();
        when(converter.convert(any())).thenReturn(delegateValue);
        JsonSerializer<Object> delegateSer = mock(JsonSerializer.class);
        when(delegateSer.isEmpty(delegateValue)).thenReturn(true);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, mock(JavaType.class), delegateSer);
        assertTrue(ser.isEmpty(new Object()));
        verify(delegateSer).isEmpty(delegateValue);
    }

    @Test    public void testIsEmptyWithProvider() {
        Converter<Object, Object> converter = mock(Converter.class);
        Object delegateValue = new Object();
        when(converter.convert(any())).thenReturn(delegateValue);
        JsonSerializer<Object> delegateSer = mock(JsonSerializer.class);
        SerializerProvider prov = mock(SerializerProvider.class);
        when(delegateSer.isEmpty(prov, delegateValue)).thenReturn(false);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, mock(JavaType.class), delegateSer);
        assertFalse(ser.isEmpty(prov, new Object()));
        verify(delegateSer).isEmpty(prov, delegateValue);
    }

    @Test    public void testConvertValue() {
        Converter<Object, Object> converter = mock(Converter.class);
        Object input = new Object();
        Object expected = new Object();
        when(converter.convert(input)).thenReturn(expected);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter);
        assertEquals(expected, ser.convertValue(input));
    }

    @Test    public void testGetSchemaSchemaAware() throws JsonMappingException {
        Converter<Object, Object> converter = mock(Converter.class);
        JsonSerializer<Object> delegateSer = mock(JsonSerializer.class);
        SchemaAware schemaAware = mock(SchemaAware.class);
        when(delegateSer).thenReturn(schemaAware); // trick: cast? We'll mock as SchemaAware
        // Simpler: use mock that implements both JsonSerializer and SchemaAware
        JsonSerializer<Object> multiMock = mock(JsonSerializer.class, withSettings().extraInterfaces(SchemaAware.class));
        SchemaAware schemaAwareDelegate = (SchemaAware) multiMock;
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, mock(JavaType.class), multiMock);
        SerializerProvider provider = mock(SerializerProvider.class);
        Type typeHint = Object.class;
        JsonNode expectedNode = mock(JsonNode.class);
        when(schemaAwareDelegate.getSchema(provider, typeHint)).thenReturn(expectedNode);
        JsonNode result = ser.getSchema(provider, typeHint);
        assertSame(expectedNode, result);
    }

    @Test    public void testGetSchemaNotSchemaAware() throws JsonMappingException {
        Converter<Object, Object> converter = mock(Converter.class);
        JsonSerializer<Object> delegateSer = mock(JsonSerializer.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, mock(JavaType.class), delegateSer);
        SerializerProvider provider = mock(SerializerProvider.class);
        Type typeHint = Object.class;
        // super.getSchema will return something, we just verify delegate not called
        JsonNode result = ser.getSchema(provider, typeHint);
        assertNotNull(result);
        verify(delegateSer, never()).getSchema(any(SerializerProvider.class), any(Type.class));
        verify(delegateSer, never()).getSchema(any(SerialierProvider.class), any(Type.class), anyBoolean());    }

    @Test    public void testGetSchemaWithOptionalSchemaAware() throws JsonMappingException {
        JsonSerializer<Object> multiMock = mock(JsonSerializer.class, withSettings().extraInterfaces(SchemaAware.class));
        SchemaAware schemaAwareDelegate = (SchemaAware) multiMock;
        StdDelegatingSerializer ser = new StdDelegatingSerializer(mock(Converter.class), mock(JavaType.class), multiMock);
        SerialierProvider provider = mock(SerialierProvider.class);
        Type typeHint = Object.class;
        JsonNode expectedNode = mock(JsonNode.class);
        when(schemaAwareDelegate.getSchema(provider, typeHint, true)).thenReturn(expectedNode);
        JsonNode result = ser.getSchema(provider, typeHint, true);
        assertSame(expectedNode, result);
    }

    @Test    public void testAcceptJsonFormatVisitor() throws JsonMappingException {
        Converter<Object, Object> converter = mock(Converter.class);
        JsonSerializer<Object> delegateSer = mock(JsonSerializer.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter, mock(JavaType.class), delegateSer);
        JsonFormatVisitorWrapper visitor = mock(JsonFormatVisitorWrapper.class);
        JavaType typeHint = mock(JavaType.class);
        ser.acceptJsonFormatVisitor(visitor, typeHint);
        verify(delegateSer).acceptJsonFormatVisitor(visitor, typeHint);
    }

    @Test    public void testGetConverter() {
        Converter<Object, Object> converter = mock(Converter.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(converter);
        assertSame(converter, ser.getConverter());
    }

    @Test    public void testGetDelegatee() {
        JsonSerializer<Object> delegateSer = mock(JsonSerializer.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(mock(Converter.class), mock(JavaType.class), delegateSer);
        assertSame(delegateSer, ser.getDelegatee());
    }
}
