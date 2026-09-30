package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class CSVRecordTest {

    private static final String[] EMPTY_VALUES = new String[0];
    private static final Map<String, Integer> EMPTY_MAPPING = new HashMap<String, Integer>();
    private static final String COMMENT = "comment";
    private static final long RECORD_NUMBER = 1L;

    // Helper to create a CSVRecord
    private CSVRecord createRecord(String[] values, Map<String, Integer> mapping, String comment, long recordNumber) {
        return new CSVRecord(values, mapping, comment, recordNumber);
    }

    @Test
    public void testConstructorWithNullValues() {
        CSVRecord record = createRecord(null, null, null, 0L);
        assertNotNull(record);
        assertEquals(0, record.size());
        assertNull(record.getComment());
        assertEquals(0L, record.getRecordNumber());
    }

    @Test
    public void testConstructorWithValues() {
        String[] values = {"a", "b", "c"};
        CSVRecord record = createRecord(values, null, null, 0L);
        assertEquals(3, record.size());
        assertEquals("a", record.get(0));
        assertEquals("b", record.get(1));
        assertEquals("c", record.get(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0L);
        record.get(1);
    }

    @Test
    public void testGetByEnum() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        CSVRecord record = createRecord(new String[]{"valA", "valB"}, mapping, null, 0L);
        assertEquals("valA", record.get(TestEnum.A));
        assertEquals("valB", record.get(TestEnum.B));
    }

    enum TestEnum { A, B }

    @Test
    public void testGetByName() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 0);
        mapping.put("col2", 1);
        CSVRecord record = createRecord(new String[]{"val1", "val2"}, mapping, null, 0L);
        assertEquals("val1", record.get("col1"));
        assertEquals("val2", record.get("col2"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetByNameNoMapping() {
        CSVRecord record = createRecord(new String[]{"val1"}, null, null, 0L);
        record.get("col1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameMappingNotFound() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 0);
        CSVRecord record = createRecord(new String[]{"val1"}, mapping, null, 0L);
        record.get("col2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameIndexOutOfBounds() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 2); // index 2, but only 1 value
        CSVRecord record = createRecord(new String[]{"val1"}, mapping, null, 0L);
        record.get("col1");
    }

    @Test
    public void testGetComment() {
        CSVRecord record = createRecord(EMPTY_VALUES, null, COMMENT, RECORD_NUMBER);
        assertEquals(COMMENT, record.getComment());
        record = createRecord(EMPTY_VALUES, null, null, RECORD_NUMBER);
        assertNull(record.getComment());
    }

    @Test
    public void testGetRecordNumber() {
        CSVRecord record = createRecord(EMPTY_VALUES, null, null, 100L);
        assertEquals(100L, record.getRecordNumber());
        record = createRecord(EMPTY_VALUES, null, null, -1L);
        assertEquals(-1L, record.getRecordNumber());
    }

    @Test
    public void testIsConsistent() {
        // No mapping -> consistent
        CSVRecord record = createRecord(new String[]{"a", "b"}, null, null, 0L);
        assertTrue(record.isConsistent());

        // Mapping size equals values length -> consistent
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("a", 0);
        mapping.put("b", 1);
        record = createRecord(new String[]{"a", "b"}, mapping, null, 0L);
        assertTrue(record.isConsistent());

        // Mapping size not equal to values length -> inconsistent
        mapping = new HashMap<String, Integer>();
        mapping.put("a", 0);
        record = createRecord(new String[]{"a", "b"}, mapping, null, 0L);
        assertFalse(record.isConsistent());

        mapping = new HashMap<String, Integer>();
        mapping.put("a", 0);
        mapping.put("b", 1);
        mapping.put("c", 2);
        record = createRecord(new String[]{"a", "b"}, mapping, null, 0L);
        assertFalse(record.isConsistent());
    }

    @Test
    public void testIsMapped() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 0);
        CSVRecord record = createRecord(new String[]{"val1"}, mapping, null, 0L);
        assertTrue(record.isMapped("col1"));
        assertFalse(record.isMapped("col2"));

        record = createRecord(new String[]{"val1"}, null, null, 0L);
        assertFalse(record.isMapped("col1"));
    }

    @Test
    public void testIsSet() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 0);
        mapping.put("col2", 1);
        CSVRecord record = createRecord(new String[]{"val1"}, mapping, null, 0L);
        assertTrue(record.isSet("col1"));
        assertFalse(record.isSet("col2")); // index 1 >= length 1

        record = createRecord(new String[]{"val1"}, null, null, 0L);
        assertFalse(record.isSet("col1"));
    }

    @Test
    public void testIterator() {
        String[] values = {"a", "b", "c"};
        CSVRecord record = createRecord(values, null, null, 0L);
        Iterator<String> it = record.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testPutIn() {
        Map<String, Integer> mapping = new LinkedHashMap<String, Integer>();
        mapping.put("col1", 0);
        mapping.put("col2", 1);
        mapping.put("col3", 2);
        CSVRecord record = createRecord(new String[]{"val1", "val2"}, mapping, null, 0L);
        Map<String, String> map = new HashMap<String, String>();
        Map<String, String> result = record.putIn(map);
        assertSame(map, result);
        assertEquals(2, map.size());
        assertEquals("val1", map.get("col1"));
        assertEquals("val2", map.get("col2"));
        assertNull(map.get("col3"));
    }

    @Test
    public void testPutInWithEmptyMapping() {
        CSVRecord record = createRecord(new String[]{"val1"}, EMPTY_MAPPING, null, 0L);
        Map<String, String> map = new HashMap<String, String>();
        record.putIn(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testSize() {
        assertEquals(0, createRecord(EMPTY_VALUES, null, null, 0L).size());
        assertEquals(3, createRecord(new String[]{"a", "b", "c"}, null, null, 0L).size());
    }

    @Test
    public void testToList() throws Exception {
        String[] values = {"x", "y"};
        CSVRecord record = createRecord(values, null, null, 0L);
        // toList is private, but we can test via iterator or toString
        List<String> list = Arrays.asList(values);
        Iterator<String> it = record.iterator();
        for (String s : list) {
            assertEquals(s, it.next());
        }
    }

    @Test
    public void testToMap() {
        Map<String, Integer> mapping = new LinkedHashMap<String, Integer>();
        mapping.put("key1", 0);
        mapping.put("key2", 1);
        CSVRecord record = createRecord(new String[]{"val1", "val2"}, mapping, null, 0L);
        Map<String, String> map = record.toMap();
        assertEquals(2, map.size());
        assertEquals("val1", map.get("key1"));
        assertEquals("val2", map.get("key2"));
    }

    @Test
    public void testToMapEmptyMapping() {
        CSVRecord record = createRecord(new String[]{"val1"}, null, null, 0L);
        Map<String, String> map = record.toMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testToString() {
        CSVRecord record = createRecord(new String[]{"a", "b"}, null, null, 0L);
        assertEquals("[a, b]", record.toString());
        record = createRecord(EMPTY_VALUES, null, null, 0L);
        assertEquals("[]", record.toString());
    }

    @Test
    public void testValues() {
        String[] values = {"v1", "v2"};
        CSVRecord record = createRecord(values, null, null, 0L);
        assertArrayEquals(values, record.values());
    }

    @Test
    public void testSerializable() {
        // Just ensure it implements Serializable
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0L);
        assertTrue(record instanceof Serializable);
    }

    @Test
    public void testGetByNameWithNullMappingKeySet() {
        // mapping.get returns null, but mapping is not null
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 0);
        CSVRecord record = createRecord(new String[]{"val1"}, mapping, null, 0L);
        try {
            record.get("nonexistent");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Mapping for nonexistent not found"));
        }
    }

    @Test
    public void testIsConsistentWithNullMapping() {
        CSVRecord record = createRecord(new String[]{"a", "b"}, null, null, 0L);
        assertTrue(record.isConsistent());
    }

    @Test
    public void testIsMappedWithNullMapping() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0L);
        assertFalse(record.isMapped("a"));
    }

    @Test
    public void testIsSetWithNullMapping() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0L);
        assertFalse(record.isSet("a"));
    }

    @Test
    public void testPutInWithNullMapping() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0L);
        Map<String, String> map = new HashMap<String, String>();
        // putIn accesses mapping.entrySet() which would NPE if mapping is null.
        // But the constructor sets mapping to the provided value, which can be null.
        // The method putIn is package-private and called from toMap which is public.
        // toMap calls putIn with a new HashMap. If mapping is null, NPE.
        // We test that toMap handles it gracefully? Actually toMap doesn't check for null mapping.
        // Let's see: toMap calls putIn(new HashMap...). If mapping is null, putIn will throw NPE.
        // But the source code shows putIn iterates over mapping.entrySet() without null check.
        // So we expect NullPointerException.
        try {
            record.toMap();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testIteratorOnEmptyRecord() {
        CSVRecord record = createRecord(EMPTY_VALUES, null, null, 0L);
        Iterator<String> it = record.iterator();
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetByIndexNegative() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0L);
        try {
            record.get(-1);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetByEnumWithNullEnum() {
        // Enum.toString() would NPE if enum is null, but get(Enum) calls e.toString().
        // We can't pass null to get(Enum) because it's not nullable in signature? Actually Enum<?> e can be null.
        // But the method doesn't check for null, so NPE.
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0L);
        try {
            record.get((Enum<?>) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetByNameWithEmptyMapping() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 0L);
        try {
            record.get("any");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Mapping for any not found"));
        }
    }

    @Test
    public void testIsSetWithIndexEqualToLength() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col", 1);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 0L);
        assertFalse(record.isSet("col"));
    }

    @Test
    public void testIsSetWithIndexLessThanLength() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col", 0);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 0L);
        assertTrue(record.isSet("col"));
    }

    @Test
    public void testPutInWithIndexOutOfBounds() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col", 2); // index 2, values length 1
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 0L);
        Map<String, String> map = new HashMap<String, String>();
        record.putIn(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testToMapWithIndexOutOfBounds() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col", 2);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 0L);
        Map<String, String> map = record.toMap();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testToStringWithNullValues() {
        // Constructor converts null values to empty array, so toString should be "[]"
        CSVRecord record = createRecord(null, null, null, 0L);
        assertEquals("[]", record.toString());
    }

    @Test
    public void testValuesReturnsCopyOrSame() {
        String[] values = {"a", "b"};
        CSVRecord record = createRecord(values, null, null, 0L);
        String[] returned = record.values();
        assertArrayEquals(values, returned);
        // Modify returned array and check if internal state changed (it shouldn't if it's a copy, but it's the same reference)
        returned[0] = "modified";
        assertEquals("modified", record.get(0)); // This shows it's the same array, not a copy. That's fine.
    }

    @Test
    public void testIteratorRemove() {
        CSVRecord record = createRecord(new String[]{"a", "b"}, null, null, 0L);
        Iterator<String> it = record.iterator();
        it.next();
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected because Arrays.asList returns a fixed-size list
        }
    }

    @Test
    public void testGetByIndexMaxInt() {
        // Just ensure no unexpected behavior with large index, but it will throw ArrayIndexOutOfBoundsException
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0L);
        try {
            record.get(Integer.MAX_VALUE);
            fail("Expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testGetRecordNumberMaxLong() {
        CSVRecord record = createRecord(EMPTY_VALUES, null, null, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, record.getRecordNumber());
    }

    @Test
    public void testGetRecordNumberMinLong() {
        CSVRecord record = createRecord(EMPTY_VALUES, null, null, Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, record.getRecordNumber());
    }

    @Test
    public void testIsConsistentWithEmptyMappingAndValues() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        CSVRecord record = createRecord(EMPTY_VALUES, mapping, null, 0L);
        assertTrue(record.isConsistent());
    }

    @Test
    public void testIsConsistentWithNonEmptyMappingAndEmptyValues() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col", 0);
        CSVRecord record = createRecord(EMPTY_VALUES, mapping, null, 0L);
        assertFalse(record.isConsistent());
    }

    @Test
    public void testGetByNameWithMappingIndexNull() {
        // mapping.get returns null, covered in testGetByNameMappingNotFound
    }

    @Test
    public void testGetByNameWithValidIndexButValuesLengthZero() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col", 0);
        CSVRecord record = createRecord(EMPTY_VALUES, mapping, null, 0L);
        try {
            record.get("col");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Index for header 'col' is 0 but CSVRecord only has 0 values!"));
        }
    }

    @Test
    public void testPutInWithNullMap() {
        Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col", 0);
        CSVRecord record = createRecord(new String[]{"a"}, mapping, null, 0L);
        try {
            record.putIn(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testToMapWithNullMapping() {
        CSVRecord record = createRecord(new String[]{"a"}, null, null, 0L);
        try {
            record.toMap();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
}
