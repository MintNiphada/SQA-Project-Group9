package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class StringArrayDeserializerTest {

    private ObjectMapper mapper;
    
    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }
    
    // -- constructor tests -- 
    @Test
    public void testDefaultConstructor() throws Exception {
        StringArrayDeserializer deser = new StringArrayDeserializer();
        assertNull(getElementDeserializer(deser));
    }

    @Test
    public void testConstructorWithCustomDeserializer() throws Exception {
        JsonDeserializer<String> custom = mock(JsonDeserializer.class);
        StringArrayDeserializer deser = new StringArrayDeserializer(custom);
        assertSame(custom, getElementDeserializer(deser));
    }

    // -- deserialize tests --

    @Test
    public void testDeserializeSimpleArray() throws Exception {
        String json = "[\"a\", \"b\", \"c\"]";
        String[] result = mapper.readValue(json, String[].class);
        assertArrayEquals(new String[]{"a","b","c"}, result);
    }

    @Test
    public void testDeserializeArrayWithNullAndNumbers() throws Exception {
        String json = "[\"a\", null, 123, true, \"\"]";
        String[] result = mapper.readValue(json, String[].class);
        // _parseString converts numbers/booleans to their string representations
        assertArrayEquals(new String[]{"a", null, "123", "true", ""}, result);
    }

    @Test
    public void testDeserializeLargeArrayChunkBoundary() throws Exception {
        int size = 20; // exceeds ObjectBuffer default chunk size
        StringBuilder sb = new StringBuilder("[");
        for (int i=0; i<size; i++) {
            sb.append("\"").append("val").append(i).append("\"");
            if (i < size-1) sb.append(",");
        }
        sb.append("]");
        String[] result = mapper.readValue(sb.toString(), String[].class);
        assertEquals(size, result.length);
        assertEquals("val0", result[0]);
        assertEquals("val"+(size-1), result[size-1]);
    }

    @Test
    public void testDeserializeWithAcceptSingleValueAsArray() throws Exception {
        ObjectMapper m = new ObjectMapper(); 
        m.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        String json = "\"single\"";
        String[] result = m.readValue(json, String[].class);
        assertArrayEquals(new String[]{"single"}, result);
    }

    @Test
    public void testDeserializeWithEmptyStringAsNullObject() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        m.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        String json = "\"\"";
        String[] result = m.readValue(json, String[].class);
        assertNull(result);
    }

    @Test
    public void testDeserializeNonArrayWithoutFeature() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        // ACCEPT_EMPTY_STRING_AS_NULL_OBJECT not enabled, so non-array throws
        String json = "123";
        try {
            m.readValue(json, String[].class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test
    public void testDeserializeNonArrayWithFeatureButNotEmptyString() throws Exception {
        ObjectMapper m = new ObjectMapper();
        m.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        m.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        // non-empty string not null, feature only affects empty string
        String json = "\"abc\"";
        try {
            m.readValue(json, String[].class);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    @Test
    public void testDeserializeWithCustomElementDeserializer() throws Exception {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "custom_" + p.getText();
            }
        });
        ObjectMapper m = new ObjectMapper().registerModule(module);
        String json = "[\"a\", null, \"b\"]";
        String[] result = m.readValue(json, String[].class);
        assertArrayEquals(new String[]{"custom_a", null, "custom_b"}, result);
    }

    // -- deserializeWithType -- 
    @Test
    public void testDeserializeWithType() throws Exception {
        StringArrayDeserializer deser = new StringArrayDeserializer();
        JsonParser jp = mock(JsonParser.class);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        TypeDeserializer typeDeser = mock(TypeDeserializer.class);
        
        deser.deserializeWithType(jp, ctxt, typeDeser);
        
        verify(typeDeser).deserializeTypedFromArray(jp, ctxt);
    }

    // -- createContextual tests (mocking) --

    @Test
    public void testCreateContextual_DefaultDeserializerReturnsSameInstance() throws Exception {
        StringArrayDeserializer original = new StringArrayDeserializer();
        DeserializationContext ctxt = mock(DeserializationContext.class);
        BeanProperty property = mock(BeanProperty.class);
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        when(ctxt.constructType(String.class)).thenReturn(stringType);
        // return a real default String deserializer so that isDefaultDeserializer returns true
        JsonDeserializer<?> defaultStringDeser = new com.fasterxml.jackson.databind.deser.std.StringDeserializer();
        when(ctxt.findContextualValueDeserializer(eq(stringType), eq(property)))
                .thenReturn(defaultStringDeser);
        
        JsonDeserializer<?> result = original.createContextual(ctxt, property);
        assertSame(original, result);
    }

    @Test
    public void testCreateContextual_CustomDeserializerReturnsNewInstance() throws Exception {
        StringArrayDeserializer original = new StringArrayDeserializer();
        DeserializationContext ctxt = mock(DeserializationContext.class);
        BeanProperty property = mock(BeanProperty.class);
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        when(ctxt.constructType(String.class)).thenReturn(stringType);
        JsonDeserializer<String> custom = mock(JsonDeserializer.class);
        when(ctxt.findContextualValueDeserializer(eq(stringType), eq(property)))
                .thenReturn(custom);
        
        JsonDeserializer<?> result = original.createContextual(ctxt, property);
        assertNotSame(original, result);
        assertTrue(result instanceof StringArrayDeserializer);
        assertSame(custom, getElementDeserializer((StringArrayDeserializer) result));
    }

    @Test
    public void testCreateContextual_ExistingDeserChanged() throws Exception {
        JsonDeserializer<String> origDeser = mock(JsonDeserializer.class);
        StringArrayDeserializer original = new StringArrayDeserializer(origDeser);
        // spy to control findConvertingContentDeserializer
        StringArrayDeserializer spy = spy(original);
        doReturn(null).when(spy).findConvertingContentDeserializer(any(DeserializationContext.class), any(BeanProperty.class), any(JsonDeserializer.class));
        DeserializationContext ctxt = mock(DeserializationContext.class);
        BeanProperty property = mock(BeanProperty.class);
        JsonDeserializer<String> newDeser = mock(JsonDeserializer.class);
        when(ctxt.handleSecondaryContextualization(same(origDeser), eq(property))).thenReturn(newDeser);
        
        JsonDeserializer<?> result = spy.createContextual(ctxt, property);
        assertNotSame(spy, result);
        assertSame(newDeser, getElementDeserializer((StringArrayDeserializer) result));
    }

    @Test
    public void testCreateContextual_ExistingDeserNoChange() throws Exception {
        JsonDeserializer<String> origDeser = mock(JsonDeserializer.class);
        StringArrayDeserializer original = new StringArrayDeserializer(origDeser);
        StringArrayDeserializer spy = spy(original);
        doReturn(null).when(spy).findConvertingContentDeserializer(any(DeserializationContext.class), any(BeanProperty.class), any(JsonDeserializer.class));
        DeserializationContext ctxt = mock(DeserializationContext.class);
        BeanProperty property = mock(BeanProperty.class);
        when(ctxt.handleSecondaryContextualization(same(origDeser), eq(property))).thenReturn(origDeser);
        
        JsonDeserializer<?> result = spy.createContextual(ctxt, property);
        assertSame(spy, result);
    }

    @Test
    public void testCreateContextual_ConverterReturnsDefault() throws Exception {
        // _elementDeserializer initially null, but converter returns a default deserializer
        StringArrayDeserializer original = new StringArrayDeserializer();
        StringArrayDeserializer spy = spy(original);
        DeserializationContext ctxt = mock(DeserializationContext.class);
        BeanProperty property = mock(BeanProperty.class);
        // Simulate a converter that returns a default String deserializer
        JsonDeserializer<?> converterDeser = new com.fasterxml.jackson.databind.deser.std.StringDeserializer();
        doReturn(converterDeser).when(spy).findConvertingContentDeserializer(eq(ctxt), eq(property), isNull());
        when(ctxt.handleSecondaryContextualization(same(converterDeser), eq(property))).thenReturn(converterDeser);
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        when(ctxt.constructType(String.class)).thenReturn(stringType);
        
        JsonDeserializer<?> result = spy.createContextual(ctxt, property);
        // The default deserializer will cause isDefaultDeserializer true -> set deser null
        // _elementDeserializer was null, so result should be same instance
        assertSame(spy, result);
    }

    // Helper to access _elementDeserializer via reflection
    private JsonDeserializer<?> getElementDeserializer(StringArrayDeserializer deser) throws Exception {
        Field f = StringArrayDeserializer.class.getDeclaredField("_elementDeserializer");
        f.setAccessible(true);
        return (JsonDeserializer<?>) f.get(deser);
    }
}
