package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MappingIterator.
 */
public class MappingIteratorTest {

    private JsonParser mockParser;
    private DeserializationContext mockContext;
    private JsonDeserializer<Object> mockDeserializer;
    private JavaType mockType;

    @Before
    public void setUp() {
        mockParser = mock(JsonParser.class);
        mockContext = mock(DeserializationContext.class);
        mockDeserializer = mock(JsonDeserializer.class);
        mockType = mock(JavaType.class);
    }

    // Helper to create a MappingIterator with managed parser (closeParser=true)
    private MappingIterator<Object> createManagedIterator(JsonParser parser, JsonDeserializer<Object> deser, Object valueToUpdate) {
        return new MappingIterator<Object>(mockType, parser, mockContext, deser, true, valueToUpdate);
    }

    // Helper to create a MappingIterator with unmanaged parser (closeParser=false)
    private MappingIterator<Object> createUnmanagedIterator(JsonParser parser, JsonDeserializer<Object> deser, Object valueToUpdate) {
        return new MappingIterator<Object>(mockType, parser, mockContext, deser, false, valueToUpdate);
    }

    @Test
    public void testEmptyIterator() {
        MappingIterator<Object> empty = MappingIterator.emptyIterator();
        assertNotNull(empty);
        assertFalse(empty.hasNext());
        try {
            empty.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    @Test
    public void testConstructorWithNullParser() {
        MappingIterator<Object> iterator = createManagedIterator(null, mockDeserializer, null);
        assertNull(iterator.getParser());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testConstructorSkipsStartArrayWhenManaged() throws IOException {
        when(mockParser.isExpectedStartArrayToken()).thenReturn(true);
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        verify(mockParser).clearCurrentToken();
    }

    @Test
    public void testConstructorDoesNotSkipStartArrayWhenUnmanaged() throws IOException {
        when(mockParser.isExpectedStartArrayToken()).thenReturn(true);
        
        MappingIterator<Object> iterator = createUnmanagedIterator(mockParser, mockDeserializer, null);
        
        verify(mockParser, never()).clearCurrentToken();
    }

    @Test
    public void testHasNextValueWithNullParser() throws IOException {
        MappingIterator<Object> iterator = createManagedIterator(null, mockDeserializer, null);
        assertFalse(iterator.hasNextValue());
    }

    @Test
    public void testHasNextValueWithEOF() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken()).thenReturn(null); // EOF
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        assertFalse(iterator.hasNextValue());
        // Parser should be closed because it's managed and we hit EOF
        verify(mockParser).close();
    }

    @Test
    public void testHasNextValueWithEndArray() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken()).thenReturn(JsonToken.END_ARRAY);
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        assertFalse(iterator.hasNextValue());
        verify(mockParser).close();
    }

    @Test
    public void testHasNextValueWithValidToken() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        assertTrue(iterator.hasNextValue());
        // Should not close parser yet
        verify(mockParser, never()).close();
    }

    @Test
    public void testHasNextValueAlreadyChecked() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        // First call checks
        assertTrue(iterator.hasNextValue());
        
        // Second call should return true immediately without calling nextToken again
        // because _hasNextChecked is true
        assertTrue(iterator.hasNextValue());
        
        verify(mockParser, times(1)).nextToken();
    }

    @Test
    public void testNextValueWithNoParser() throws IOException {
        MappingIterator<Object> iterator = createManagedIterator(null, mockDeserializer, null);
        try {
            iterator.nextValue();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    @Test
    public void testNextValueWithNullDeserializerResult() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(mockDeserializer.deserialize(mockParser, mockContext)).thenReturn(null);
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        Object result = iterator.nextValue();
        assertNull(result);
        verify(mockParser).clearCurrentToken();
    }

    @Test
    public void testNextValueWithUpdatedValue() throws IOException {
        Object existingValue = new Object();
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, existingValue);
        
        Object result = iterator.nextValue();
        assertSame(existingValue, result);
        
        verify(mockDeserializer).deserialize(mockParser, mockContext, existingValue);
        verify(mockParser).clearCurrentToken();
    }

    @Test
    public void testNextValueThrowsNoSuchElementIfHasNextFalse() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken()).thenReturn(null); // EOF
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        try {
            iterator.nextValue();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // Expected
        }
    }

    @Test
    public void testHasNextHandlesJsonMappingException() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken()).thenThrow(new JsonMappingException(mockParser, "Test Error"));
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        try {
            iterator.hasNext();
            fail("Expected RuntimeJsonMappingException");
        } catch (RuntimeJsonMappingException e) {
            assertEquals("Test Error", e.getMessage());
        }
    }

    @Test
    public void testHasNextHandlesIOException() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken()).thenThrow(new IOException("IO Error"));
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        try {
            iterator.hasNext();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("IO Error", e.getMessage());
        }
    }

    @Test
    public void testNextHandlesJsonMappingException() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(mockDeserializer.deserialize(mockParser, mockContext)).thenThrow(new JsonMappingException(mockParser, "Deser Error"));
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        try {
            iterator.next();
            fail("Expected RuntimeJsonMappingException");
        } catch (RuntimeJsonMappingException e) {
            assertEquals("Deser Error", e.getMessage());
        }
    }

    @Test
    public void testNextHandlesIOException() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        when(mockDeserializer.deserialize(mockParser, mockContext)).thenThrow(new IOException("Deser IO Error"));
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        try {
            iterator.next();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("Deser IO Error", e.getMessage());
        }
    }

    @Test
    public void testRemoveThrowsUnsupported() {
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        try {
            iterator.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testCloseWithManagedParser() throws IOException {
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        iterator.close();
        verify(mockParser).close();
    }

    @Test
    public void testCloseWithNullParser() throws IOException {
        MappingIterator<Object> iterator = createManagedIterator(null, mockDeserializer, null);
        iterator.close();
        // No exception should be thrown
    }

    @Test
    public void testReadAllList() throws IOException {
        // Simulate reading 2 items then EOF
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken())
            .thenReturn(JsonToken.VALUE_STRING)
            .thenReturn(JsonToken.VALUE_STRING)
            .thenReturn(null); // EOF
        
        when(mockDeserializer.deserialize(mockParser, mockContext))
            .thenReturn("Item1")
            .thenReturn("Item2");
            
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        List<Object> result = iterator.readAll();
        
        assertEquals(2, result.size());
        assertEquals("Item1", result.get(0));
        assertEquals("Item2", result.get(1));
    }

    @Test
    public void testReadAllCollection() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken())
            .thenReturn(JsonToken.VALUE_STRING)
            .thenReturn(null); // EOF
        
        when(mockDeserializer.deserialize(mockParser, mockContext))
            .thenReturn("Item1");
            
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        Set<Object> result = iterator.readAll(new HashSet<Object>());
        
        assertEquals(1, result.size());
        assertTrue(result.contains("Item1"));
    }

    @Test
    public void testReadAllEmpty() throws IOException {
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken()).thenReturn(null); // EOF immediately
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        List<Object> result = iterator.readAll();
        
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetParser() {
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        assertSame(mockParser, iterator.getParser());
    }

    @Test
    public void testGetParserSchema() throws IOException {
        FormatSchema mockSchema = mock(FormatSchema.class);
        when(mockParser.getSchema()).thenReturn(mockSchema);
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        assertSame(mockSchema, iterator.getParserSchema());
    }

    @Test
    public void testGetCurrentLocation() {
        JsonLocation mockLocation = mock(JsonLocation.class);
        when(mockParser.getCurrentLocation()).thenReturn(mockLocation);
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        assertSame(mockLocation, iterator.getCurrentLocation());
    }

    @Test
    public void testIteratorStateTransition() throws IOException {
        // Test that hasNextValue sets _hasNextChecked and nextValue resets it
        
        when(mockParser.getCurrentToken()).thenReturn(null);
        when(mockParser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        when(mockDeserializer.deserialize(mockParser, mockContext)).thenReturn("Val");
        
        MappingIterator<Object> iterator = createManagedIterator(mockParser, mockDeserializer, null);
        
        // Initially _hasNextChecked is false
        // hasNextValue() should call nextToken()
        assertTrue(iterator.hasNextValue());
        verify(mockParser, times(1)).nextToken();
        
        // nextValue() should use the checked state, not call nextToken() again for checking
        // but it will call deserialize
        Object val = iterator.nextValue();
        assertEquals("Val", val);
        
        // After nextValue, _hasNextChecked is false again
        // So next hasNextValue() should call nextToken() again
        when(mockParser.nextToken()).thenReturn(null); // EOF
        assertFalse(iterator.hasNextValue());
        verify(mockParser, times(2)).nextToken();
    }
}
