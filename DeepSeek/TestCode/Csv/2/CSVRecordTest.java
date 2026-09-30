package org.apache.commons.csv;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CSVRecordTest {

    @Test
    public void testConstructorWithNullValues() {
        CSVRecord record = new CSVRecord(null, null, null, 0);
        assertEquals(0, record.size());
        assertNotNull(record.values());
        assertEquals(0, record.values().length);
    }

    @Test
    public void testConstructorWithNonNullValues() {
        String[] values = {"a", "b", "c"};
        CSVRecord record = new CSVRecord(values, null, null, 1);
        assertEquals(3, record.size());
        assertArrayEquals(values, record.values());
    }

    @Test
    public void testGetByIndexValid() {
        String[] values = {"x", "y", "z"};
        CSVRecord record = new CSVRecord(values, null, null, 0);
        assertEquals("x", record.get(0));
        assertEquals("y", record.get(1));
        assertEquals("z", record.get(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexNegative() {
        CSVRecord record = new CSVRecord(new String[]{"a"}, null, null, 0);
        record.get(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        CSVRecord record = new CSVRecord(new String[]{"a"}, null, null, 0);
        record.get(1);
    }

    @Test(expected = IllegalStateException.class)
    public void testGetByNameWithNullMapping() {
        CSVRecord record = new CSVRecord(new String[]{"a"}, null, null, 0);
        record.get("col");
    }

    @Test
    public void testGetByNameMappingPresentNameNotFound() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = new CSVRecord(new String[]{"a"}, mapping, null, 0);
        assertNull(record.get("nonexistent"));
    }

    @Test
    public void testGetByNameMappingPresentNameFound() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        CSVRecord record = new CSVRecord(new String[]{"a", "b"}, mapping, null, 0);
        assertEquals("a", record.get("col0"));
        assertEquals("b", record.get("col1"));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByNameMappingPresentIndexOutOfBounds() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 5); // index 5, but values length is 2
        CSVRecord record = new CSVRecord(new String[]{"a", "b"}, mapping, null, 0);
        record.get("col");
    }

    @Test
    public void testIsConsistentWithNullMapping() {
        CSVRecord record = new CSVRecord(new String[]{"a", "b"}, null, null, 0);
        assertTrue(record.isConsistent());
    }

    @Test
    public void testIsConsistentWithMatchingSizes() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("a", 0);
        mapping.put("b", 1);
        CSVRecord record = new CSVRecord(new String[]{"x", "y"}, mapping, null, 0);
        assertTrue(record.isConsistent());
    }

    @Test
    public void testIsConsistentWithMismatchedSizes() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("a", 0);
        mapping.put("b", 1);
        mapping.put("c", 2); // 3 mappings, but values length 2
        CSVRecord record = new CSVRecord(new String[]{"x", "y"}, mapping, null, 0);
        assertFalse(record.isConsistent());
    }

    @Test
    public void testIsMappedWithNullMapping() {
        CSVRecord record = new CSVRecord(new String[]{"a"}, null, null, 0);
        assertFalse(record.isMapped("col"));
    }

    @Test
    public void testIsMappedWithMappingPresent() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord record = new CSVRecord(new String[]{"a"}, mapping, null, 0);
        assertTrue(record.isMapped("col"));
        assertFalse(record.isMapped("other"));
    }

    @Test
    public void testIsSetWithNullMapping() {
        CSVRecord record = new CSVRecord(new String[]{"a"}, null, null, 0);
        assertFalse(record.isSet("col"));
    }

    @Test
    public void testIsSetWithMappingNotMapped() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord record = new CSVRecord(new String[]{"a"}, mapping, null, 0);
        assertFalse(record.isSet("other"));
    }

    @Test
    public void testIsSetWithMappingMappedAndIndexInBounds() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 0);
        CSVRecord record = new CSVRecord(new String[]{"a", "b"}, mapping, null, 0);
        assertTrue(record.isSet("col"));
    }

    @Test
    public void testIsSetWithMappingMappedAndIndexOutOfBounds() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col", 5); // index 5, values length 2
        CSVRecord record = new CSVRecord(new String[]{"a", "b"}, mapping, null, 0);
        assertFalse(record.isSet("col"));
    }

    @Test
    public void testIterator() {
        String[] values = {"a", "b", "c"};
        CSVRecord record = new CSVRecord(values, null, null, 0);
        Iterator<String> it = record.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertTrue(it.hasNext());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testValuesMethod() {
        String[] values = {"x", "y"};
        CSVRecord record = new CSVRecord(values, null, null, 0);
        assertSame(values, record.values());
    }

    @Test
    public void testGetCommentNull() {
        CSVRecord record = new CSVRecord(new String[0], null, null, 0);
        assertNull(record.getComment());
    }

    @Test
    public void testGetCommentNonNull() {
        CSVRecord record = new CSVRecord(new String[0], null, "test comment", 0);
        assertEquals("test comment", record.getComment());
    }

    @Test
    public void testGetRecordNumber() {
        CSVRecord record = new CSVRecord(new String[0], null, null, 42L);
        assertEquals(42L, record.getRecordNumber());
    }

    @Test
    public void testSize() {
        CSVRecord record = new CSVRecord(new String[]{"a", "b", "c"}, null, null, 0);
        assertEquals(3, record.size());
        record = new CSVRecord(new String[0], null, null, 0);
        assertEquals(0, record.size());
    }

    @Test
    public void testToStringEmpty() {
        CSVRecord record = new CSVRecord(new String[0], null, null, 0);
        assertEquals("[]", record.toString());
    }

    @Test
    public void testToStringNonEmpty() {
        CSVRecord record = new CSVRecord(new String[]{"a", "b"}, null, null, 0);
        assertEquals("[a, b]", record.toString());
    }
}
