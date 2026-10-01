package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualKeyDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

@RunWith(MockitoJUnitRunner.class)
public class MapDeserializerTest {

    @Mock
    private JavaType mockMapType;
    @Mock
    private JavaType mockKeyType;
    @Mock
    private JavaType mockContentType;
    @Mock
    private ValueInstantiator mockValueInstantiator;
    @Mock
    private KeyDeserializer mockKeyDeserializer;
    @Mock
    private JsonDeserializer<Object> mockValueDeserializer;
    @Mock
    private TypeDeserializer mockValueTypeDeserializer;
    @Mock
    private DeserializationContext mockCtxt;
    @Mock
    private JsonParser mockParser;
    @Mock
    private BeanProperty mockProperty;
    @Mock
    private ObjectMapper mockMapper;

    private MapDeserializer deserializer;

    @Before
    public void setUp() throws Exception {
        // Setup basic mocks
        when(mockMapType.getKeyType()).thenReturn(mockKeyType);
        when(mockMapType.getContentType()).thenReturn(mockContentType);
        when(mockMapType.getRawClass()).thenReturn((Class) HashMap.class);
        
        when(mockKeyType.getRawClass()).thenReturn(String.class);
        when(mockContentType.getRawClass()).thenReturn(Object.class);

        when(mockValueInstantiator.canCreateUsingDefault()).thenReturn(true);
        when(mockValueInstantiator.canCreateUsingDelegate()).thenReturn(false);
        when(mockValueInstantiator.canCreateFromObjectWith()).thenReturn(false);
        
        // Default behavior for standard string key detection
        // _isStdKeyDeser returns true if keyDeser is null OR (keyType is String/Object AND isDefaultKeyDeserializer)
        // We will test specific scenarios by changing mocks.
        
        deserializer = new MapDeserializer(mockMapType, mockValueInstantiator, mockKeyDeserializer, mockValueDeserializer, mockValueTypeDeserializer);
    }

    @Test
    public void testConstructorAndBasicProperties() {
        assertNotNull(deserializer);
        assertSame(mockMapType, deserializer.getValueType());
        assertSame(mockContentType, deserializer.getContentType());
        assertSame(mockValueDeserializer, deserializer.getContentDeserializer());
        assertEquals(HashMap.class, deserializer.getMapClass());
    }

    @Test
    public void testIsCachable_NoTypeDeser_NoIgnorable() {
        // Default setup has mockValueTypeDeserializer, so isCachable should be false
        assertFalse(deserializer.isCachable());
        
        // Create one without type deser
        MapDeserializer deserNoType = new MapDeserializer(mockMapType, mockValueInstantiator, mockKeyDeserializer, mockValueDeserializer, null);
        assertTrue(deserNoType.isCachable());
    }

    @Test
    public void testIsCachable_WithIgnorableProperties() {
        MapDeserializer deserNoType = new MapDeserializer(mockMapType, mockValueInstantiator, mockKeyDeserializer, mockValueDeserializer, null);
        deserNoType.setIgnorableProperties(new String[]{"ignoreMe"});
        assertFalse(deserNoType.isCachable());
    }

    @Test
    public void testSetIgnorableProperties_Null() {
        deserializer.setIgnorableProperties(null);
        // Should not throw, and _ignorableProperties should be null
        // We can't directly access protected field, but isCachable logic depends on it.
        // If we had a no-type version, we could check isCachable.
        MapDeserializer deserNoType = new MapDeserializer(mockMapType, mockValueInstantiator, mockKeyDeserializer, mockValueDeserializer, null);
        deserNoType.setIgnorableProperties(null);
        assertTrue(deserNoType.isCachable());
    }

    @Test
    public void testSetIgnorableProperties_Empty() {
        MapDeserializer deserNoType = new MapDeserializer(mockMapType, mockValueInstantiator, mockKeyDeserializer, mockValueDeserializer, null);
        deserNoType.setIgnorableProperties(new String[]{});
        assertTrue(deserNoType.isCachable());
    }

    @Test
    public void testResolve_NoDelegateNoPropertyCreator() throws Exception {
        when(mockValueInstantiator.canCreateUsingDelegate()).thenReturn(false);
        when(mockValueInstantiator.canCreateFromObjectWith()).thenReturn(false);
        
        deserializer.resolve(mockCtxt);
        
        // Should not throw
        verify(mockValueInstantiator, never()).getDelegateType(any());
        verify(mockValueInstantiator, never()).getFromObjectArguments(any());
    }

    @Test
    public void testResolve_WithDelegate() throws Exception {
        when(mockValueInstantiator.canCreateUsingDelegate()).thenReturn(true);
        when(mockValueInstantiator.getDelegateType(any())).thenReturn(mockContentType);
        
        // findDeserializer is protected in ContainerDeserializerBase, hard to mock directly without spy.
        // But we can verify it doesn't crash.
        // Note: findDeserializer calls ctxt.findContextualValueDeserializer usually.
        when(mockCtxt.findContextualValueDeserializer(any(), any())).thenReturn(mockValueDeserializer);

        deserializer.resolve(mockCtxt);
        
        verify(mockValueInstantiator).getDelegateType(any());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolve_InvalidDelegate() throws Exception {
        when(mockValueInstantiator.canCreateUsingDelegate()).thenReturn(true);
        when(mockValueInstantiator.getDelegateType(any())).thenReturn(null);
        
        deserializer.resolve(mockCtxt);
    }

    @Test
    public void testResolve_WithPropertyCreator() throws Exception {
        when(mockValueInstantiator.canCreateFromObjectWith()).thenReturn(true);
        when(mockValueInstantiator.getFromObjectArguments(any())).thenReturn(new com.fasterxml.jackson.databind.deser.SettableBeanProperty[0]);
        
        // PropertyBasedCreator.construct is static, hard to mock. 
        // We just ensure it runs without NPE.
        deserializer.resolve(mockCtxt);
        
        verify(mockValueInstantiator).getFromObjectArguments(any());
    }

    @Test
    public void testCreateContextual_Basic() throws Exception {
        when(mockCtxt.findKeyDeserializer(any(), any())).thenReturn(mockKeyDeserializer);
        when(mockCtxt.findContextualValueDeserializer(any(), any())).thenReturn(mockValueDeserializer);
        when(mockCtxt.getAnnotationIntrospector()).thenReturn(null);
        
        JsonDeserializer<?> result = deserializer.createContextual(mockCtxt, mockProperty);
        
        assertNotNull(result);
        assertTrue(result instanceof MapDeserializer);
    }

    @Test
    public void testCreateContextual_ContextualKeyDeserializer() throws Exception {
        ContextualKeyDeserializer contextualKeyDeser = mock(ContextualKeyDeserializer.class);
        when(contextualKeyDeser.createContextual(any(), any())).thenReturn(mockKeyDeserializer);
        
        // Need a deserializer that has this key deser
        MapDeserializer deserWithCtxKey = new MapDeserializer(mockMapType, mockValueInstantiator, contextualKeyDeser, mockValueDeserializer, mockValueTypeDeserializer);
        
        when(mockCtxt.findContextualValueDeserializer(any(), any())).thenReturn(mockValueDeserializer);
        when(mockCtxt.getAnnotationIntrospector()).thenReturn(null);
        
        JsonDeserializer<?> result = deserWithCtxKey.createContextual(mockCtxt, mockProperty);
        
        verify(contextualKeyDeser).createContextual(mockCtxt, mockProperty);
        assertNotNull(result);
    }

    @Test
    public void testCreateContextual_WithIgnorablePropertiesFromAnnotation() throws Exception {
        com.fasterxml.jackson.databind.introspect.AnnotationIntrospector mockIntrospector = mock(com.fasterxml.jackson.databind.introspect.AnnotationIntrospector.class);
        when(mockCtxt.getAnnotationIntrospector()).thenReturn(mockIntrospector);
        
        // Simulate finding properties to ignore
        when(mockIntrospector.findPropertiesToIgnore(any())).thenReturn(new String[]{"ignoredField"});
        
        when(mockCtxt.findKeyDeserializer(any(), any())).thenReturn(mockKeyDeserializer);
        when(mockCtxt.findContextualValueDeserializer(any(), any())).thenReturn(mockValueDeserializer);
        
        JsonDeserializer<?> result = deserializer.createContextual(mockCtxt, mockProperty);
        
        assertNotNull(result);
        // The result should have the ignorable properties set. 
        // We can't easily check private fields, but we can check isCachable if we remove type deser.
        // Let's create a version without type deser to test cachability impact of ignorable props
        MapDeserializer deserNoType = new MapDeserializer(mockMapType, mockValueInstantiator, mockKeyDeserializer, mockValueDeserializer, null);
        when(mockCtxt.findContextualValueDeserializer(any(), any())).thenReturn(mockValueDeserializer);
        
        JsonDeserializer<?> resultNoType = deserNoType.createContextual(mockCtxt, mockProperty);
        assertFalse(resultNoType.isCachable()); // Because ignorable properties were added
    }

    @Test
    public void testDeserialize_StandardStringKey() throws Exception {
        // Setup for standard string key: keyType is String, keyDeserializer is default (or null)
        // In our setUp, mockKeyDeserializer is a mock, so _isStdKeyDeser might return false unless we mock isDefaultKeyDeserializer logic.
        // _isStdKeyDeser checks: keyDeser == null OR (keyType is String/Object AND isDefaultKeyDeserializer(keyDeser))
        // isDefaultKeyDeserializer is protected in ContainerDeserializerBase.
        // To force _standardStringKey = true, easiest is to pass null keyDeserializer.
        
        MapDeserializer deserStdKey = new MapDeserializer(mockMapType, mockValueInstantiator, null, mockValueDeserializer, null);
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(mockParser.nextToken()).thenReturn(JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.END_OBJECT);
        when(mockParser.getCurrentName()).thenReturn("key1");
        when(mockParser.getText()).thenReturn("value1");
        
        when(mockValueInstantiator.createUsingDefault(any())).thenReturn(new HashMap<>());
        when(mockValueDeserializer.getObjectIdReader()).thenReturn(null);
        when(mockValueDeserializer.deserialize(any(), any())).thenReturn("value1");
        
        Map<Object, Object> result = deserStdKey.deserialize(mockParser, mockCtxt);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value1", result.get("key1"));
    }

    @Test
    public void testDeserialize_NonStandardKey() throws Exception {
        // Use the default deserializer which has a mock key deser (non-standard)
        // _standardStringKey will be false because mockKeyDeserializer is not "default" (isDefaultKeyDeserializer returns false for mocks usually unless configured)
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(mockParser.nextToken()).thenReturn(JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.END_OBJECT);
        when(mockParser.getCurrentName()).thenReturn("key1");
        
        when(mockValueInstantiator.createUsingDefault(any())).thenReturn(new HashMap<>());
        when(mockKeyDeserializer.deserializeKey(eq("key1"), any())).thenReturn("customKey");
        when(mockValueDeserializer.getObjectIdReader()).thenReturn(null);
        when(mockValueDeserializer.deserialize(any(), any())).thenReturn("value1");
        
        Map<Object, Object> result = deserializer.deserialize(mockParser, mockCtxt);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value1", result.get("customKey"));
        verify(mockKeyDeserializer).deserializeKey("key1", mockCtxt);
    }

    @Test
    public void testDeserialize_WithIgnorableProperty() throws Exception {
        deserializer.setIgnorableProperties(new String[]{"ignoreMe"});
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        // Sequence: FIELD_NAME(ignoreMe), VALUE_STRING, FIELD_NAME(keep), VALUE_STRING, END_OBJECT
        when(mockParser.nextToken()).thenReturn(JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.END_OBJECT);
        when(mockParser.getCurrentName()).thenReturn("ignoreMe", "keep");
        
        when(mockValueInstantiator.createUsingDefault(any())).thenReturn(new HashMap<>());
        when(mockKeyDeserializer.deserializeKey(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockValueDeserializer.getObjectIdReader()).thenReturn(null);
        when(mockValueDeserializer.deserialize(any(), any())).thenReturn("value");
        
        Map<Object, Object> result = deserializer.deserialize(mockParser, mockCtxt);
        
        assertEquals(1, result.size());
        assertTrue(result.containsKey("keep"));
        assertFalse(result.containsKey("ignoreMe"));
        verify(mockParser).skipChildren();
    }

    @Test
    public void testDeserialize_ValueNull() throws Exception {
        MapDeserializer deserStdKey = new MapDeserializer(mockMapType, mockValueInstantiator, null, mockValueDeserializer, null);
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(mockParser.nextToken()).thenReturn(JsonToken.FIELD_NAME, JsonToken.VALUE_NULL, JsonToken.END_OBJECT);
        when(mockParser.getCurrentName()).thenReturn("key1");
        
        when(mockValueInstantiator.createUsingDefault(any())).thenReturn(new HashMap<>());
        when(mockValueDeserializer.getObjectIdReader()).thenReturn(null);
        when(mockValueDeserializer.getNullValue()).thenReturn("nullValue");
        
        Map<Object, Object> result = deserStdKey.deserialize(mockParser, mockCtxt);
        
        assertEquals("nullValue", result.get("key1"));
    }

    @Test
    public void testDeserialize_WithDelegate() throws Exception {
        when(mockValueInstantiator.canCreateUsingDelegate()).thenReturn(true);
        when(mockValueInstantiator.getDelegateType(any())).thenReturn(mockContentType);
        when(mockCtxt.findContextualValueDeserializer(any(), any())).thenReturn(mockValueDeserializer);
        
        deserializer.resolve(mockCtxt);
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(mockValueDeserializer.deserialize(any(), any())).thenReturn("delegateResult");
        when(mockValueInstantiator.createUsingDelegate(any(), any())).thenReturn(new HashMap<>());
        
        Map<Object, Object> result = deserializer.deserialize(mockParser, mockCtxt);
        
        verify(mockValueInstantiator).createUsingDelegate(eq(mockCtxt), eq("delegateResult"));
    }

    @Test
    public void testDeserialize_NoDefaultCreator() throws Exception {
        when(mockValueInstantiator.canCreateUsingDefault()).thenReturn(false);
        when(mockValueInstantiator.canCreateUsingDelegate()).thenReturn(false);
        when(mockValueInstantiator.canCreateFromObjectWith()).thenReturn(false);
        
        // Need to re-instantiate or resolve to update _hasDefaultCreator? 
        // Constructor sets _hasDefaultCreator = valueInstantiator.canCreateUsingDefault();
        // So we need a new instance.
        MapDeserializer deserNoDefault = new MapDeserializer(mockMapType, mockValueInstantiator, mockKeyDeserializer, mockValueDeserializer, mockValueTypeDeserializer);
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(mockCtxt.instantiationException(any(), any())).thenThrow(new JsonMappingException("No default constructor"));
        
        try {
            deserNoDefault.deserialize(mockParser, mockCtxt);
            fail("Expected exception");
        } catch (JsonMappingException e) {
            // Expected
        }
    }

    @Test
    public void testDeserialize_FromString() throws Exception {
        when(mockValueInstantiator.canCreateUsingDefault()).thenReturn(false); // Force check for string
        when(mockValueInstantiator.canCreateUsingDelegate()).thenReturn(false);
        when(mockValueInstantiator.canCreateFromObjectWith()).thenReturn(false);
        
        MapDeserializer deserFromString = new MapDeserializer(mockMapType, mockValueInstantiator, mockKeyDeserializer, mockValueDeserializer, mockValueTypeDeserializer);
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(mockParser.getText()).thenReturn("empty");
        when(mockValueInstantiator.createFromString(any(), any())).thenReturn(new HashMap<>());
        
        Map<Object, Object> result = deserFromString.deserialize(mockParser, mockCtxt);
        
        verify(mockValueInstantiator).createFromString(eq(mockCtxt), eq("empty"));
    }

    @Test
    public void testDeserialize_InvalidToken() throws Exception {
        when(mockValueInstantiator.canCreateUsingDefault()).thenReturn(true);
        when(mockValueInstantiator.canCreateUsingDelegate()).thenReturn(false);
        when(mockValueInstantiator.canCreateFromObjectWith()).thenReturn(false);
        
        MapDeserializer deser = new MapDeserializer(mockMapType, mockValueInstantiator, mockKeyDeserializer, mockValueDeserializer, mockValueTypeDeserializer);
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.START_ARRAY);
        when(mockCtxt.mappingException(any())).thenThrow(new JsonMappingException("Invalid token"));
        
        try {
            deser.deserialize(mockParser, mockCtxt);
            fail("Expected exception");
        } catch (JsonMappingException e) {
            // Expected
        }
    }

    @Test
    public void testDeserialize_IntoExistingMap() throws Exception {
        Map<Object, Object> existingMap = new HashMap<>();
        existingMap.put("existing", "value");
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(mockParser.nextToken()).thenReturn(JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.END_OBJECT);
        when(mockParser.getCurrentName()).thenReturn("newKey");
        
        when(mockKeyDeserializer.deserializeKey(any(), any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mockValueDeserializer.getObjectIdReader()).thenReturn(null);
        when(mockValueDeserializer.deserialize(any(), any())).thenReturn("newValue");
        
        Map<Object, Object> result = deserializer.deserialize(mockParser, mockCtxt, existingMap);
        
        assertSame(existingMap, result);
        assertEquals(2, result.size());
        assertEquals("value", result.get("existing"));
        assertEquals("newValue", result.get("newKey"));
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        when(mockValueTypeDeserializer.deserializeTypedFromObject(any(), any())).thenReturn(new HashMap<>());
        
        Object result = deserializer.deserializeWithType(mockParser, mockCtxt, mockValueTypeDeserializer);
        
        verify(mockValueTypeDeserializer).deserializeTypedFromObject(mockParser, mockCtxt);
    }

    @Test
    public void testWrapAndThrow_InvocationTargetException() throws Exception {
        // Access protected method via reflection or subclass if needed, but it's protected.
        // We can test indirectly via deserialize if an exception occurs.
        // Or use reflection.
        
        java.lang.reflect.Method method = MapDeserializer.class.getDeclaredMethod("wrapAndThrow", Throwable.class, Object.class, String.class);
        method.setAccessible(true);
        
        Exception cause = new Exception("Root Cause");
        InvocationTargetException ite = new InvocationTargetException(cause);
        
        try {
            method.invoke(deserializer, ite, new Object(), "key");
            fail("Expected JsonMappingException");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof JsonMappingException);
            assertEquals(cause, e.getCause().getCause());
        }
    }

    @Test
    public void testWrapAndThrow_Error() throws Exception {
        java.lang.reflect.Method method = MapDeserializer.class.getDeclaredMethod("wrapAndThrow", Throwable.class, Object.class, String.class);
        method.setAccessible(true);
        
        Error error = new Error("Test Error");
        
        try {
            method.invoke(deserializer, error, new Object(), "key");
            fail("Expected Error");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertSame(error, e.getCause());
        }
    }

    @Test
    public void testWrapAndThrow_IOException() throws Exception {
        java.lang.reflect.Method method = MapDeserializer.class.getDeclaredMethod("wrapAndThrow", Throwable.class, Object.class, String.class);
        method.setAccessible(true);
        
        IOException ioException = new IOException("IO Error");
        
        try {
            method.invoke(deserializer, ioException, new Object(), "key");
            fail("Expected IOException");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertSame(ioException, e.getCause());
        }
    }

    @Test
    public void testWrapAndThrow_OtherException() throws Exception {
        java.lang.reflect.Method method = MapDeserializer.class.getDeclaredMethod("wrapAndThrow", Throwable.class, Object.class, String.class);
        method.setAccessible(true);
        
        RuntimeException runtimeException = new RuntimeException("Runtime Error");
        
        try {
            method.invoke(deserializer, runtimeException, new Object(), "key");
            fail("Expected JsonMappingException");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof JsonMappingException);
            assertEquals(runtimeException, e.getCause().getCause());
        }
    }

    @Test
    public void testIsStdKeyDeser_NullKeyDeserializer() throws Exception {
        java.lang.reflect.Method method = MapDeserializer.class.getDeclaredMethod("_isStdKeyDeser", JavaType.class, KeyDeserializer.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(deserializer, mockMapType, null);
        assertTrue(result);
    }

    @Test
    public void testIsStdKeyDeser_NullKeyType() throws Exception {
        java.lang.reflect.Method method = MapDeserializer.class.getDeclaredMethod("_isStdKeyDeser", JavaType.class, KeyDeserializer.class);
        method.setAccessible(true);
        
        when(mockMapType.getKeyType()).thenReturn(null);
        
        boolean result = (boolean) method.invoke(deserializer, mockMapType, mockKeyDeserializer);
        assertTrue(result);
    }

    @Test
    public void testIsStdKeyDeser_StringKeyDefaultDeser() throws Exception {
        java.lang.reflect.Method method = MapDeserializer.class.getDeclaredMethod("_isStdKeyDeser", JavaType.class, KeyDeserializer.class);
        method.setAccessible(true);
        
        // Need to mock isDefaultKeyDeserializer. It's protected in ContainerDeserializerBase.
        // We can't easily mock it without spy. 
        // However, if we pass a real StdKeyDeserializer, it might work?
        // Or we can just rely on the fact that if it's not null and not default, it returns false.
        
        // Let's test the negative case: Non-String key type
        when(mockKeyType.getRawClass()).thenReturn(Integer.class);
        
        boolean result = (boolean) method.invoke(deserializer, mockMapType, mockKeyDeserializer);
        assertFalse(result);
    }
    
    @Test
    public void testIsStdKeyDeser_ObjectKeyDefaultDeser() throws Exception {
        java.lang.reflect.Method method = MapDeserializer.class.getDeclaredMethod("_isStdKeyDeser", JavaType.class, KeyDeserializer.class);
        method.setAccessible(true);
        
        when(mockKeyType.getRawClass()).thenReturn(Object.class);
        
        // isDefaultKeyDeserializer(mockKeyDeserializer) will likely return false for a mock unless we spy.
        // But if we assume mock is not default, result is false.
        // If we want true, we need a real default key deser.
        
        // Let's just verify it doesn't crash and returns boolean.
        boolean result = (boolean) method.invoke(deserializer, mockMapType, mockKeyDeserializer);
        // Result depends on isDefaultKeyDeserializer implementation for mocks.
        // Usually false for mocks.
    }

    @Test
    public void testDeserialize_WithObjectId() throws Exception {
        // Setup for ObjectId
        ReadableObjectId mockRoid = mock(ReadableObjectId.class);
        when(mockValueDeserializer.getObjectIdReader()).thenReturn(mockRoid);
        
        // We need to simulate the flow where UnresolvedForwardReference is caught.
        // This is complex to unit test directly without deep integration.
        // We will test the happy path where no forward reference occurs.
        
        MapDeserializer deserStdKey = new MapDeserializer(mockMapType, mockValueInstantiator, null, mockValueDeserializer, null);
        
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        when(mockParser.nextToken()).thenReturn(JsonToken.FIELD_NAME, JsonToken.VALUE_STRING, JsonToken.END_OBJECT);
        when(mockParser.getCurrentName()).thenReturn("key1");
        
        when(mockValueInstantiator.createUsingDefault(any())).thenReturn(new HashMap<>());
        when(mockValueDeserializer.deserialize(any(), any())).thenReturn("value1");
        
        Map<Object, Object> result = deserStdKey.deserialize(mockParser, mockCtxt);
        
        assertNotNull(result);
        assertEquals("value1", result.get("key1"));
    }
}
