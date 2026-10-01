package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.util.Converter;

@RunWith(MockitoJUnitRunner.class)
public class StdSerializerTest {

    @Mock
    private SerializerProvider provider;
    @Mock
    private JsonGenerator jgen;
    @Mock
    private JsonFormatVisitorWrapper visitor;
    @Mock
    private JavaType typeHint;
    @Mock
    private AnnotationIntrospector introspector;
    @Mock
    private BeanProperty prop;
    @Mock
    private AnnotatedMember member;
    @Mock
    private Converter<Object, Object> converter;
    @Mock
    private JavaType delegateType;
    @Mock
    private JsonSerializer<Object> existingSerializer;
    @Mock
    private FilterProvider filterProvider;
    @Mock
    private PropertyFilter propertyFilter;

    private TestSerializer serializer;

    // Concrete implementation for testing
    @JacksonStdImpl
    private static class AnnotatedSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}
    }

    private static class NonAnnotatedSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}
    }

    private static class TestSerializer extends StdSerializer<Object> {
        public TestSerializer(Class<Object> t) {
            super(t);
        }
        public TestSerializer(JavaType type) {
            super(type);
        }
        public TestSerializer(Class<?> t, boolean dummy) {
            super(t, dummy);
        }
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) {}
    }

    @Before
    public void setUp() {
        serializer = new TestSerializer(Object.class);
    }

    // Constructor tests

    @Test
    public void testConstructorWithClass() {
        TestSerializer s = new TestSerializer(String.class);
        assertEquals(String.class, s.handledType());
    }

    @Test
    public void testConstructorWithJavaType() {
        when(typeHint.getRawClass()).thenReturn(Integer.class);
        TestSerializer s = new TestSerializer(typeHint);
        assertEquals(Integer.class, s.handledType());
    }

    @Test
    public void testConstructorWithClassAndDummy() {
        TestSerializer s = new TestSerializer(Boolean.class, true);
        assertEquals(Boolean.class, s.handledType());
    }

    // handledType

    @Test
    public void testHandledType() {
        assertEquals(Object.class, serializer.handledType());
    }

    // getSchema methods

    @Test
    public void testGetSchemaWithTypeHint() throws JsonMappingException {
        ObjectNode schema = (ObjectNode) serializer.getSchema(provider, String.class);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
    }

    @Test
    public void testGetSchemaWithTypeHintOptionalFalse() throws JsonMappingException {
        ObjectNode schema = (ObjectNode) serializer.getSchema(provider, String.class, false);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
        assertTrue(schema.get("required").asBoolean());
    }

    @Test
    public void testGetSchemaWithTypeHintOptionalTrue() throws JsonMappingException {
        ObjectNode schema = (ObjectNode) serializer.getSchema(provider, String.class, true);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
        assertNull(schema.get("required"));
    }

    // createObjectNode

    @Test
    public void testCreateObjectNode() {
        ObjectNode node = serializer.createObjectNode();
        assertNotNull(node);
        assertTrue(node.size() == 0);
    }

    // createSchemaNode

    @Test
    public void testCreateSchemaNodeType() {
        ObjectNode schema = serializer.createSchemaNode("array");
        assertEquals("array", schema.get("type").asText());
    }

    @Test
    public void testCreateSchemaNodeTypeOptionalFalse() {
        ObjectNode schema = serializer.createSchemaNode("array", false);
        assertEquals("array", schema.get("type").asText());
        assertTrue(schema.get("required").asBoolean());
    }

    @Test
    public void testCreateSchemaNodeTypeOptionalTrue() {
        ObjectNode schema = serializer.createSchemaNode("array", true);
        assertEquals("array", schema.get("type").asText());
        assertNull(schema.get("required"));
    }

    // acceptJsonFormatVisitor

    @Test
    public void testAcceptJsonFormatVisitor() throws JsonMappingException {
        serializer.acceptJsonFormatVisitor(visitor, typeHint);
        verify(visitor).expectAnyFormat(typeHint);
    }

    // wrapAndThrow tests

    @Test(expected = Error.class)
    public void testWrapAndThrowError() throws IOException {
        serializer.wrapAndThrow(provider, new Error("test"), "bean", "field");
    }

    @Test(expected = IOException.class)
    public void testWrapAndThrowPlainIOException() throws IOException {
        serializer.wrapAndThrow(provider, new IOException("plain"), "bean", "field");
    }

    @Test
    public void testWrapAndThrowJsonMappingExceptionWrapTrue() throws IOException {
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(true);
        try {
            serializer.wrapAndThrow(provider, new JsonMappingException("test"), "bean", "field");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test(expected = JsonMappingException.class)
    public void testWrapAndThrowJsonMappingExceptionWrapFalse() throws IOException {
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(false);
        serializer.wrapAndThrow(provider, new JsonMappingException("test"), "bean", "field");
    }

    @Test
    public void testWrapAndThrowRuntimeExceptionWrapTrue() throws IOException {
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(true);
        try {
            serializer.wrapAndThrow(provider, new RuntimeException("runtime"), "bean", "field");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test(expected = RuntimeException.class)
    public void testWrapAndThrowRuntimeExceptionWrapFalse() throws IOException {
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(false);
        serializer.wrapAndThrow(provider, new RuntimeException("runtime"), "bean", "field");
    }

    @Test
    public void testWrapAndThrowInvocationTargetExceptionUnwrap() throws IOException {
        IOException cause = new IOException("cause");
        InvocationTargetException ite = new InvocationTargetException(cause);
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(true);
        try {
            serializer.wrapAndThrow(provider, ite, "bean", "field");
            fail("Expected IOException");
        } catch (IOException e) {
            assertSame(cause, e);
        }
    }

    @Test
    public void testWrapAndThrowInvocationTargetExceptionNullCause() throws IOException {
        InvocationTargetException ite = new InvocationTargetException(null);
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(true);
        try {
            serializer.wrapAndThrow(provider, ite, "bean", "field");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test
    public void testWrapAndThrowProviderNull() throws IOException {
        // provider null => wrap = true
        try {
            serializer.wrapAndThrow(null, new RuntimeException("test"), "bean", "field");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    // wrapAndThrow with index

    @Test(expected = Error.class)
    public void testWrapAndThrowIndexError() throws IOException {
        serializer.wrapAndThrow(provider, new Error("test"), "bean", 1);
    }

    @Test(expected = IOException.class)
    public void testWrapAndThrowIndexPlainIOException() throws IOException {
        serializer.wrapAndThrow(provider, new IOException("plain"), "bean", 1);
    }

    @Test
    public void testWrapAndThrowIndexJsonMappingExceptionWrapTrue() throws IOException {
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(true);
        try {
            serializer.wrapAndThrow(provider, new JsonMappingException("test"), "bean", 1);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test(expected = JsonMappingException.class)
    public void testWrapAndThrowIndexJsonMappingExceptionWrapFalse() throws IOException {
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(false);
        serializer.wrapAndThrow(provider, new JsonMappingException("test"), "bean", 1);
    }

    @Test
    public void testWrapAndThrowIndexRuntimeExceptionWrapTrue() throws IOException {
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(true);
        try {
            serializer.wrapAndThrow(provider, new RuntimeException("runtime"), "bean", 1);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test(expected = RuntimeException.class)
    public void testWrapAndThrowIndexRuntimeExceptionWrapFalse() throws IOException {
        when(provider.isEnabled(SerializationFeature.WRAP_EXCEPTIONS)).thenReturn(false);
        serializer.wrapAndThrow(provider, new RuntimeException("runtime"), "bean", 1);
    }

    // isDefaultSerializer

    @Test
    public void testIsDefaultSerializerTrue() {
        assertTrue(serializer.isDefaultSerializer(new AnnotatedSerializer()));
    }

    @Test
    public void testIsDefaultSerializerFalse() {
        assertFalse(serializer.isDefaultSerializer(new NonAnnotatedSerializer()));
    }

    // findConvertingContentSerializer

    @Test
    public void testFindConvertingContentSerializerNullIntrospector() throws JsonMappingException {
        when(provider.getAnnotationIntrospector()).thenReturn(null);
        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, prop, existingSerializer);
        assertSame(existingSerializer, result);
    }

    @Test
    public void testFindConvertingContentSerializerNullProp() throws JsonMappingException {
        when(provider.getAnnotationIntrospector()).thenReturn(introspector);
        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, null, existingSerializer);
        assertSame(existingSerializer, result);
    }

    @Test
    public void testFindConvertingContentSerializerNullMember() throws JsonMappingException {
        when(provider.getAnnotationIntrospector()).thenReturn(introspector);
        when(prop.getMember()).thenReturn(null);
        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, prop, existingSerializer);
        assertSame(existingSerializer, result);
    }

    @Test
    public void testFindConvertingContentSerializerNoConverter() throws JsonMappingException {
        when(provider.getAnnotationIntrospector()).thenReturn(introspector);
        when(prop.getMember()).thenReturn(member);
        when(introspector.findSerializationContentConverter(member)).thenReturn(null);
        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, prop, existingSerializer);
        assertSame(existingSerializer, result);
    }

    @Test
    public void testFindConvertingContentSerializerWithConverterExistingNull() throws JsonMappingException {
        when(provider.getAnnotationIntrospector()).thenReturn(introspector);
        when(prop.getMember()).thenReturn(member);
        when(introspector.findSerializationContentConverter(member)).thenReturn(new Object()); // non-null convDef
        when(provider.converterInstance(member, any())).thenReturn(converter);
        when(converter.getOutputType(provider.getTypeFactory())).thenReturn(delegateType);
        when(provider.findValueSerializer(delegateType)).thenReturn(null); // existingSerializer is null

        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, prop, null);
        assertNotNull(result);
        assertTrue(result instanceof StdDelegatingSerializer);
    }

    @Test
    public void testFindConvertingContentSerializerWithConverterExistingNotNull() throws JsonMappingException {
        when(provider.getAnnotationIntrospector()).thenReturn(introspector);
        when(prop.getMember()).thenReturn(member);
        when(introspector.findSerializationContentConverter(member)).thenReturn(new Object());
        when(provider.converterInstance(member, any())).thenReturn(converter);
        when(converter.getOutputType(provider.getTypeFactory())).thenReturn(delegateType);
        // existingSerializer is not null, so findValueSerializer should not be called
        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, prop, existingSerializer);
        assertNotNull(result);
        assertTrue(result instanceof StdDelegatingSerializer);
        verify(provider, never()).findValueSerializer(any(JavaType.class));
    }

    // findPropertyFilter

    @Test(expected = JsonMappingException.class)
    public void testFindPropertyFilterNullFilterProvider() throws JsonMappingException {
        when(provider.getFilterProvider()).thenReturn(null);
        serializer.findPropertyFilter(provider, "id", "value");
    }

    @Test
    public void testFindPropertyFilterReturnsFilter() throws JsonMappingException {
        when(provider.getFilterProvider()).thenReturn(filterProvider);
        when(filterProvider.findPropertyFilter("id", "value")).thenReturn(propertyFilter);
        PropertyFilter result = serializer.findPropertyFilter(provider, "id", "value");
        assertSame(propertyFilter, result);
    }

    @Test
    public void testFindPropertyFilterReturnsNull() throws JsonMappingException {
        when(provider.getFilterProvider()).thenReturn(filterProvider);
        when(filterProvider.findPropertyFilter("id", "value")).thenReturn(null);
        PropertyFilter result = serializer.findPropertyFilter(provider, "id", "value");
        assertNull(result);
    }
}
