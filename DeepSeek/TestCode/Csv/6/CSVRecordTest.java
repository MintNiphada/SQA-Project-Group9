package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

public class CSVRecordTest {

    private enum TestEnum {
        A, B, C
    }

    // Helper to create a CSVRecord with given values, mapping, comment, recordNumber
    private CSVRecord createRecord(String[] values, Map<String, Integer> mapping, String comment, long recordNumber) {
        return new CSVRecord(values, mapping, comment, recordNumber);
    }

    @Test
    public void testConstructorWithNullValues() {
        CSVRecord record = createRecord(null, null, null, 0);
        assertEquals(0, record.size());
        assertArrayEquals(new String[0], record.values());
    }

    @Test
    public void testConstructorWithNonNullValues() {
        String[] values = {"a", "b", "c"};
        CSVRecord record = createRecord(values, null, null, 1);
        assertEquals(3, record.size());
        assertArrayEquals(values, record.values());
    }

    @Test
    public void testGetByIndex() {
        String[] values = {"x", "y", "z"};
        CSVRecord record = createRecord(values, null, null, 1);
        assertEquals("x", record.get(0));
        assertEquals("y", record.get(1));
        assertEquals("z", record.get(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        String[] values = {"x"};
        CSVRecord record = createRecord(values, null, null, 1);
        record.get(1);
    }

    @Test
    public void testGetByEnum() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        mapping.put("C", 2);
        String[] values = {"valA", "valB", "valC"};
        CSVRecord record = createRecord(values, mapping, null, 1);
        assertEquals("valA", record.get(TestEnum.A));
        assertEquals("valB", record.get(TestEnum.B));
        assertEquals("valC", record.get(TestEnum.C));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByEnumMappingMissing() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        String[] values = {"valA"};
        CSVRecord record = createRecord(values, mapping, null, 1);
        record.get(TestEnum.B); // B not in mapping
    }

    @Test(expected = IllegalStateException.class)
    public void testGetByNameNoMapping() {
        String[] values = {"a"};
        CSVRecord record = createRecord(values, null, null, 1);
        record.get("any");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameMappingMissingKey() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("key1", 0);
        String[] values = {"val1"};
        CSVRecord record = createRecord(values, mapping, null, 1);
        record.get("key2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameInconsistentRecord() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("key1", 0);
        mapping.put("key2", 1); // index 1 but values length is 1
        String[] values = {"val1"};
        CSVRecord record = createRecord(values, mapping, null, 1);
        record.get("key2");
    }

    @Test
    public void testGetByNameSuccess() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("key1", 0);
        mapping.put("key2", 1);
        String[] values = {"val1", "val2"};
        CSVRecord record = createRecord(values, mapping, null, 1);
        assertEquals("val1", record.get("key1"));
        assertEquals("val2", record.get("key2"));
    }

    @Test
    public void testGetComment() {
        CSVRecord record = createRecord(new String[0], null, "comment", 0);
        assertEquals("comment", record.getComment());
        record = createRecord(new String[0], null, null, 0);
        assertNull(record.getComment());
    }

    @Test
    public void testGetRecordNumber() {
        CSVRecord record = createRecord(new String[0], null, null, 42);
        assertEquals(42, record.getRecordNumber());
    }

    @Test
    public void testIsConsistent() {
        // mapping null -> true
        CSVRecord record = createRecord(new String[]{"a", "b"}, null, null, 0);
        assertTrue(record.isConsistent());

        // mapping size equals values length -> true
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        record = createRecord(new String[]{"a", "b"}, mapping, null, 0);
        assertTrue(record.isConsistent());

        // mapping size not equal -> false
        mapping.put("C", 2);
        record = createRecord(new String[]{"a", "b"}, mapping, null, 0);
        assertFalse(record.isConsistent());
    }

    @Test
    public void testIsMapped() {
        // mapping null -> false
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0);
        assertFalse(record.isMapped("any"));

        // mapping not null, key present -> true
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        record = createRecord(new String[]{"a"}, mapping, null, 0);
        assertTrue(record.isMapped("A"));
        assertFalse(record.isMapped("B"));
    }

    @Test
    public void testIsSet() {
        // mapping null -> false
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0);
        assertFalse(record.isSet("any"));

        // mapping not null, key not mapped -> false
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        record = createRecord(new String[]{"a"}, mapping, null, 0);
        assertFalse(record.isSet("B"));

        // key mapped but index >= values.length -> false
        mapping.put("B", 1);
        record = createRecord(new String[]{"a"}, mapping, null, 0);
        assertFalse(record.isSet("B"));

        // key mapped and index < values.length -> true
        record = createRecord(new String[]{"a", "b"}, mapping, null, 0);
        assertTrue(record.isSet("A"));
        assertTrue(record.isSet("B"));
    }

    @Test
    public void testIterator() {
        String[] values = {"x", "y", "z"};
        CSVRecord record = createRecord(values, null, null, 0);
        Iterator<String> it = record.iterator();
        assertTrue(it.hasNext());
        assertEquals("x", it.next());
        assertEquals("y", it.next());
        assertEquals("z", it.next());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testSize() {
        assertEquals(0, createRecord(new String[0], null, null, 0).size());
        assertEquals(3, createRecord(new String[]{"a","b","c"}, null, null, 0).size());
    }

    @Test
    public void testToMapWithMapping() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("key1", 0);
        mapping.put("key2", 1);
        String[] values = {"val1", "val2"};
        CSVRecord record = createRecord(values, mapping, null, 0);
        Map<String, String> map = record.toMap();
        assertEquals(2, map.size());
        assertEquals("val1", map.get("key1"));
        assertEquals("val2", map.get("key2"));
    }

    @Test(expected = NullPointerException.class)
    public void testToMapWithNullMapping() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0);
        record.toMap(); // should throw NPE because mapping is null
    }

    @Test
    public void testToString() {
        String[] values = {"a", "b", "c"};
        CSVRecord record = createRecord(values, null, null, 0);
        assertEquals(Arrays.toString(values), record.toString());
    }

    @Test
    public void testValues() {
        String[] values = {"x", "y"};
        CSVRecord record = createRecord(values, null, null, 0);
        assertArrayEquals(values, record.values());
    }

    @Test
    public void testPutInWithInconsistentMapping() {
        // mapping has index beyond values length, putIn will throw ArrayIndexOutOfBoundsException
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("key1", 0);
        mapping.put("key2", 1);
        String[] values = {"val1"};
        CSVRecord record = createRecord(values, mapping, null, 0);
        try {
            record.toMap(); // calls putIn
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }
}
