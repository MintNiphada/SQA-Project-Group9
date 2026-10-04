package com.fasterxml.jackson.databind;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class MappingIteratorTest {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testEmptyIterator() throws IOException {
        MappingIterator<Object> it = MappingIterator.emptyIterator();
        assertNotNull(it);
        assertSame(it, MappingIterator.emptyIterator());
        assertFalse(it.hasNext());
        assertFalse(it.hasNextValue());
        assertNull(it.getParser());

        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        try {
            it.nextValue();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        // Close should be safe on empty iterator
        it.close();

        List<Object> list = it.readAll();
        assertTrue(list.isEmpty());

        List<Object> customList = new LinkedList<Object>();
        List<Object> returnedList = it.readAll(customList);
        assertSame(customList, returnedList);
        assertTrue(returnedList.isEmpty());

        Set<Object> customSet = new HashSet<Object>();
        Set<Object> returnedSet = it.readAll(customSet);
        assertSame(customSet, returnedSet);
        assertTrue(returnedSet.isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveThrowsException() {
        MappingIterator<Object> it = MappingIterator.emptyIterator();
        it.remove();
    }

    @Test
    public void testConstructorWithManagedParserAndStartArray() throws IOException {
        JsonParser p = JSON_FACTORY.createParser("[1, 2]");
        p.nextToken(); // position on START_ARRAY
        assertEquals(JsonToken.START_ARRAY, p.getCurrentToken());

        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        JavaType type = MAPPER.constructType(Integer.class);
        JsonDeserializer<?> deser = MAPPER._findRootDeserializer(ctxt, type);

        MappingIterator<Integer> it = new MappingIterator<Integer>(type, p, ctxt, deser, true, null);
        // Managed parser should clear the START_ARRAY token
        assertNull(p.getCurrentToken());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(1), it.next());
        assertTrue(it.hasNextValue());
        assertEquals(Integer.valueOf(2), it.nextValue());
        assertFalse(it.hasNextValue());
        it.close();
    }

    @Test
    public void testConstructorWithUnmanagedParserAndStartArray() throws IOException {
        JsonParser p = JSON_FACTORY.createParser("[1, 2]");
        p.nextToken(); // position on START_ARRAY
        assertEquals(JsonToken.START_ARRAY, p.getCurrentToken());

        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        JavaType type = MAPPER.constructType(Integer.class);
        JsonDeserializer<?> deser = MAPPER._findRootDeserializer(ctxt, type);

        // unmanaged parser (managedParser = false) should NOT clear token
        MappingIterator<Integer> it = new MappingIterator<Integer>(type, p, ctxt, deser, false, null);
        assertEquals(JsonToken.START_ARRAY, p.getCurrentToken());
        p.close();
    }

    @Test
    public void testConstructorWithNullParser() {
        MappingIterator<Object> it = new MappingIterator<Object>(null, null, null, null, true, null);
        assertNull(it.getParser());
        assertFalse(it.hasNext());
    }

    @Test
    public void testIterationWithValuesWithoutUpdate() throws IOException {
        JsonParser p = JSON_FACTORY.createParser("10 20 30");
        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        JavaType type = MAPPER.constructType(Integer.class);
        JsonDeserializer<?> deser = MAPPER._findRootDeserializer(ctxt, type);

        MappingIterator<Integer> it = new MappingIterator<Integer>(type, p, ctxt, deser, true, null);
        assertNotNull(it.getParser());
        assertNull(it.getParserSchema());
        assertNotNull(it.getCurrentLocation());

        assertTrue(it.hasNext());
        assertTrue(it.hasNext()); // redundant check
        assertEquals(Integer.valueOf(10), it.next());

        assertTrue(it.hasNextValue());
        assertEquals(Integer.valueOf(20), it.nextValue());

        // Calling next directly without hasNext
        assertEquals(Integer.valueOf(30), it.next());

        assertFalse(it.hasNext());
        assertFalse(it.hasNextValue());
        assertNull(it.getParser()); // Parser is set to null after reaching EOF
    }

    @Test
    public void testIterationWithUpdatedValue() throws IOException {
        JsonParser p = JSON_FACTORY.createParser("{\"name\":\"Alice\"} {\"name\":\"Bob\"}");
        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        JavaType type = MAPPER.constructType(Map.class);
        JsonDeserializer<?> deser = MAPPER._findRootDeserializer(ctxt, type);

        Map<String, Object> updateTarget = new HashMap<String, Object>();
        MappingIterator<Map<String, Object>> it = new MappingIterator<Map<String, Object>>(
                type, p, ctxt, deser, true, updateTarget);

        assertTrue(it.hasNextValue());
        Map<String, Object> first = it.nextValue();
        assertSame(updateTarget, first);
        assertEquals("Alice", updateTarget.get("name"));

        assertTrue(it.hasNextValue());
        Map<String, Object> second = it.nextValue();
        assertSame(updateTarget, second);
        assertEquals("Bob", updateTarget.get("name"));

        assertFalse(it.hasNextValue());
        it.close();
    }

    @Test
    public void testReadAllMethods() throws IOException {
        JsonParser p = JSON_FACTORY.createParser("1 2 3");
        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        JavaType type = MAPPER.constructType(Integer.class);
        JsonDeserializer<?> deser = MAPPER._findRootDeserializer(ctxt, type);

        MappingIterator<Integer> it = new MappingIterator<Integer>(type, p, ctxt, deser, true, null);
        List<Integer> list = it.readAll();
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));

        // Testing readAll(List)
        p = JSON_FACTORY.createParser("4 5");
        it = new MappingIterator<Integer>(type, p, ctxt, deser, true, null);
        List<Integer> customList = new ArrayList<Integer>();
        customList.add(0);
        List<Integer> resList = it.readAll(customList);
        assertSame(customList, resList);
        assertEquals(Arrays.asList(0, 4, 5), resList);

        // Testing readAll(Collection)
        p = JSON_FACTORY.createParser("6 7");
        it = new MappingIterator<Integer>(type, p, ctxt, deser, true, null);
        Set<Integer> customSet = new LinkedHashSet<Integer>();
        Set<Integer> resSet = it.readAll(customSet);
        assertSame(customSet, resSet);
        assertEquals(2, resSet.size());
        assertTrue(resSet.contains(6));
        assertTrue(resSet.contains(7));
    }

    @Test
    public void testManagedVsUnmanagedParserClosingOnEof() throws IOException {
        final boolean[] managedParserClosed = new boolean[]{false};
        JsonParser p1 = new JsonParserDelegate(JSON_FACTORY.createParser("1")) {
            @Override
            public void close() throws IOException {
                managedParserClosed[0] = true;
                super.close();
            }
        };

        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        JavaType type = MAPPER.constructType(Integer.class);
        JsonDeserializer<?> deser = MAPPER._findRootDeserializer(ctxt, type);

        MappingIterator<Integer> it1 = new MappingIterator<Integer>(type, p1, ctxt, deser, true, null);
        assertTrue(it1.hasNext());
        it1.next();
        assertFalse(it1.hasNext());
        assertTrue("Managed parser should be closed on EOF", managedParserClosed[0]);

        final boolean[] unmanagedParserClosed = new boolean[]{false};
        JsonParser p2 = new JsonParserDelegate(JSON_FACTORY.createParser("1")) {
            @Override
            public void close() throws IOException {
                unmanagedParserClosed[0] = true;
                super.close();
            }
        };

        MappingIterator<Integer> it2 = new MappingIterator<Integer>(type, p2, ctxt, deser, false, null);
        assertTrue(it2.hasNext());
        it2.next();
        assertFalse(it2.hasNext());
        assertFalse("Unmanaged parser should not be closed on EOF", unmanagedParserClosed[0]);
        p2.close();
    }

    @Test
    public void testEndOfArrayTokenClosesAndEnds() throws IOException {
        JsonParser p = JSON_FACTORY.createParser("[1]");
        p.nextToken(); // START_ARRAY
        p.nextToken(); // 1

        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        JavaType type = MAPPER.constructType(Integer.class);
        JsonDeserializer<?> deser = MAPPER._findRootDeserializer(ctxt, type);

        MappingIterator<Integer> it = new MappingIterator<Integer>(type, p, ctxt, deser, true, null);
        assertTrue(it.hasNextValue());
        assertEquals(Integer.valueOf(1), it.nextValue());
        // Next token is END_ARRAY
        assertFalse(it.hasNextValue());
        assertNull(it.getParser());
    }

    @Test
    public void testCloseMethod() throws IOException {
        final boolean[] closed = new boolean[]{false};
        JsonParser p = new JsonParserDelegate(JSON_FACTORY.createParser("1 2")) {
            @Override
            public void close() throws IOException {
                closed[0] = true;
                super.close();
            }
        };

        DeserializationContext ctxt = MAPPER.getDeserializationContext();
        JavaType type = MAPPER.constructType(Integer.class);
        JsonDeserializer<?> deser = MAPPER._findRootDeserializer(ctxt, type);

        MappingIterator<Integer> it = new MappingIterator<Integer>(type, p, ctxt, deser, false, null);
        it.close();
        assertTrue(closed[0]);
    }

    @Test
    public void testHasNextCatchesJsonMappingException() {
        JsonParser mockParser = new JsonParserDelegate(null) {
            @Override
            public JsonToken getCurrentToken() {
                return null;
            }

            @Override
            public JsonToken nextToken() throws IOException {
                throw new JsonMappingException("Mapping fail");
            }
        };

        MappingIterator<Object> it = new MappingIterator<Object>(null, mockParser, null, null, false, null);
        try {
            it.hasNext();
            fail("Expected RuntimeJsonMappingException");
        } catch (RuntimeJsonMappingException e) {
            assertTrue(e.getMessage().contains("Mapping fail"));
            assertTrue(e.getCause() instanceof JsonMappingException);
        }
    }

    @Test
    public void testHasNextCatchesIOException() {
        JsonParser mockParser = new JsonParserDelegate(null) {
            @Override
            public JsonToken getCurrentToken() {
                return null;
            }

            @Override
            public JsonToken nextToken() throws IOException {
                throw new IOException("IO error");
            }
        };

        MappingIterator<Object> it = new MappingIterator<Object>(null, mockParser, null, null, false, null);
        try {
            it.hasNext();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("IO error"));
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testNextCatchesJsonMappingException() {
        JsonParser p = null;
        try {
            p = JSON_FACTORY.createParser("1");
        } catch (IOException e) {
            fail(e.getMessage());
        }

        JsonDeserializer<Object> deser = new StdDeserializer<Object>(Object.class) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw new JsonMappingException("Mapping failed in deser");
            }
        };

        MappingIterator<Object> it = new MappingIterator<Object>(null, p, null, deser, true, null);
        try {
            it.next();
            fail("Expected RuntimeJsonMappingException");
        } catch (RuntimeJsonMappingException e) {
            assertTrue(e.getMessage().contains("Mapping failed in deser"));
            assertTrue(e.getCause() instanceof JsonMappingException);
        }
    }

    @Test
    public void testNextCatchesIOException() {
        JsonParser p = null;
        try {
            p = JSON_FACTORY.createParser("1");
        } catch (IOException e) {
            fail(e.getMessage());
        }

        JsonDeserializer<Object> deser = new StdDeserializer<Object>(Object.class) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw new IOException("IO error in deser");
            }
        };

        MappingIterator<Object> it = new MappingIterator<Object>(null, p, null, deser, true, null);
        try {
            it.next();
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("IO error in deser"));
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testClearCurrentTokenCalledEvenOnDeserializerException() throws IOException {
        final boolean[] cleared = new boolean[]{false};
        JsonParser p = new JsonParserDelegate(JSON_FACTORY.createParser("1")) {
            @Override
            public void clearCurrentToken() {
                cleared[0] = true;
                super.clearCurrentToken();
            }
        };

        JsonDeserializer<Object> deser = new StdDeserializer<Object>(Object.class) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw new JsonParseException("Parsing failure", JsonLocation.NA);
            }
        };

        MappingIterator<Object> it = new MappingIterator<Object>(null, p, null, deser, true, null);
        try {
            it.nextValue();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            assertTrue(cleared[0]);
        }
    }

    @Test
    public void testProtectedHelperMethods() {
        MappingIterator<Object> it = MappingIterator.emptyIterator();

        try {
            it._throwNoSuchElement();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }

        JsonMappingException jme = new JsonMappingException("JME");
        try {
            it._handleMappingException(jme);
            fail("Expected RuntimeJsonMappingException");
        } catch (RuntimeJsonMappingException e) {
            assertSame(jme, e.getCause());
        }

        IOException ioe = new IOException("IOE");
        try {
            it._handleIOException(ioe);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertSame(ioe, e.getCause());
        }
    }
}
