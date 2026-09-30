package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CSVRecordTest {

    private enum Header {
        FIRST_NAME, LAST_NAME, EMAIL
    }

    private String[] values;
    private Map<String, Integer> headerMap;
    private CSVRecord record;
    private CSVRecord recordNoHeader;
    private CSVRecord recordNullValues;

    @Before
    public void setUp() {
        values = new String[]{"John", "Doe", "john.doe@example.com"};
        headerMap = new HashMap<String, Integer>();
        headerMap.put("FIRST_NAME", 0);
        headerMap.put("LAST_NAME", 1);
        headerMap.put("EMAIL", 2);

        record = new CSVRecord(values, headerMap, "test comment", 1L);
        recordNoHeader = new CSVRecord(values, null, null, 0L);
        recordNullValues = new CSVRecord(null, headerMap, "empty", 2L);
    }

    @Test
    public void testConstructorWithNullValues() {
        Assert.assertNotNull(recordNullValues.values());
        Assert.assertEquals(0, recordNullValues.size());
        Assert.assertEquals(0, recordNullValues.values().length);
    }

    @Test
    public void testGetByIndex() {
        Assert.assertEquals("John", record.get(0));
        Assert.assertEquals("Doe", record.get(1));
        Assert.assertEquals("john.doe@example.com", record.get(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        record.get(3);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexNegative() {
        record.get(-1);
    }

    @Test
    public void testGetByName() {
        Assert.assertEquals("John", record.get("FIRST_NAME"));
        Assert.assertEquals("Doe", record.get("LAST_NAME"));
        Assert.assertEquals("john.doe@example.com", record.get("EMAIL"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetByNameNoHeaderMap() {
        recordNoHeader.get("FIRST_NAME");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameHeaderNotFound() {
        record.get("NON_EXISTENT_COLUMN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameIndexOutOfBounds() {
        final Map<String, Integer> invalidMap = new HashMap<String, Integer>();
        invalidMap.put("FIRST_NAME", 0);
        invalidMap.put("OUT_OF_BOUNDS", 10);
        final CSVRecord invalidRecord = new CSVRecord(values, invalidMap, null, 1L);
        invalidRecord.get("OUT_OF_BOUNDS");
    }

    @Test
    public void testGetByEnum() {
        Assert.assertEquals("John", record.get(Header.FIRST_NAME));
        Assert.assertEquals("Doe", record.get(Header.LAST_NAME));
        Assert.assertEquals("john.doe@example.com", record.get(Header.EMAIL));
    }

    @Test
    public void testGetComment() {
        Assert.assertEquals("test comment", record.getComment());
        Assert.assertNull(recordNoHeader.getComment());
        Assert.assertEquals("empty", recordNullValues.getComment());
    }

    @Test
    public void testGetRecordNumber() {
        Assert.assertEquals(1L, record.getRecordNumber());
        Assert.assertEquals(0L, recordNoHeader.getRecordNumber());
        Assert.assertEquals(2L, recordNullValues.getRecordNumber());
    }

    @Test
    public void testIsConsistent() {
        Assert.assertTrue(record.isConsistent());
        Assert.assertTrue(recordNoHeader.isConsistent());

        final Map<String, Integer> partialMap = new HashMap<String, Integer>();
        partialMap.put("FIRST_NAME", 0);
        final CSVRecord inconsistentRecord = new CSVRecord(values, partialMap, null, 1L);
        Assert.assertFalse(inconsistentRecord.isConsistent());

        final Map<String, Integer> emptyMap = Collections.emptyMap();
        final CSVRecord emptyRecord = new CSVRecord(new String[0], emptyMap, null, 1L);
        Assert.assertTrue(emptyRecord.isConsistent());

        Assert.assertFalse(recordNullValues.isConsistent());
    }

    @Test
    public void testIsMapped() {
        Assert.assertTrue(record.isMapped("FIRST_NAME"));
        Assert.assertTrue(record.isMapped("LAST_NAME"));
        Assert.assertTrue(record.isMapped("EMAIL"));
        Assert.assertFalse(record.isMapped("UNKNOWN"));
        Assert.assertFalse(recordNoHeader.isMapped("FIRST_NAME"));
    }

    @Test
    public void testIsSet() {
        Assert.assertTrue(record.isSet("FIRST_NAME"));
        Assert.assertTrue(record.isSet("LAST_NAME"));
        Assert.assertTrue(record.isSet("EMAIL"));
        Assert.assertFalse(record.isSet("UNKNOWN"));
        Assert.assertFalse(recordNoHeader.isSet("FIRST_NAME"));

        final Map<String, Integer> extendedMap = new HashMap<String, Integer>();
        extendedMap.put("FIRST_NAME", 0);
        extendedMap.put("EXTRA", 5);
        final CSVRecord extraRecord = new CSVRecord(values, extendedMap, null, 1L);

        Assert.assertTrue(extraRecord.isSet("FIRST_NAME"));
        Assert.assertFalse(extraRecord.isSet("EXTRA"));
        Assert.assertTrue(extraRecord.isMapped("EXTRA"));
    }

    @Test
    public void testIterator() {
        final List<String> list = new ArrayList<String>();
        for (final String value : record) {
            list.add(value);
        }
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("John", list.get(0));
        Assert.assertEquals("Doe", list.get(1));
        Assert.assertEquals("john.doe@example.com", list.get(2));

        final Iterator<String> emptyIterator = recordNullValues.iterator();
        Assert.assertFalse(emptyIterator.hasNext());
    }

    @Test
    public void testSize() {
        Assert.assertEquals(3, record.size());
        Assert.assertEquals(3, recordNoHeader.size());
        Assert.assertEquals(0, recordNullValues.size());
    }

    @Test
    public void testToMap() {
        final Map<String, String> map = record.toMap();
        Assert.assertNotNull(map);
        Assert.assertEquals(3, map.size());
        Assert.assertEquals("John", map.get("FIRST_NAME"));
        Assert.assertEquals("Doe", map.get("LAST_NAME"));
        Assert.assertEquals("john.doe@example.com", map.get("EMAIL"));

        final Map<String, Integer> extendedMap = new HashMap<String, Integer>();
        extendedMap.put("FIRST_NAME", 0);
        extendedMap.put("NOT_PRESENT", 10);
        final CSVRecord recordWithExcessHeaders = new CSVRecord(values, extendedMap, null, 1L);
        final Map<String, String> mapExcess = recordWithExcessHeaders.toMap();
        Assert.assertEquals(1, mapExcess.size());
        Assert.assertEquals("John", mapExcess.get("FIRST_NAME"));
        Assert.assertFalse(mapExcess.containsKey("NOT_PRESENT"));
    }

    @Test
    public void testPutIn() {
        final Map<String, String> customMap = new TreeMap<String, String>();
        customMap.put("EXISTING", "value");
        final Map<String, String> result = record.putIn(customMap);
        Assert.assertSame(customMap, result);
        Assert.assertEquals(4, result.size());
        Assert.assertEquals("value", result.get("EXISTING"));
        Assert.assertEquals("John", result.get("FIRST_NAME"));
        Assert.assertEquals("Doe", result.get("LAST_NAME"));
        Assert.assertEquals("john.doe@example.com", result.get("EMAIL"));
    }

    @Test
    public void testToString() {
        Assert.assertEquals("[John, Doe, john.doe@example.com]", record.toString());
        Assert.assertEquals("[]", recordNullValues.toString());
    }

    @Test
    public void testValues() {
        final String[] actualValues = record.values();
        Assert.assertNotNull(actualValues);
        Assert.assertEquals(3, actualValues.length);
        Assert.assertArrayEquals(values, actualValues);
    }

    @Test
    public void testSerialization() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(record);
        oos.flush();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final CSVRecord deserialized = (CSVRecord) ois.readObject();

        Assert.assertEquals(record.getRecordNumber(), deserialized.getRecordNumber());
        Assert.assertEquals(record.getComment(), deserialized.getComment());
        Assert.assertEquals(record.size(), deserialized.size());
        Assert.assertEquals(record.get("FIRST_NAME"), deserialized.get("FIRST_NAME"));
        Assert.assertEquals(record.get("LAST_NAME"), deserialized.get("LAST_NAME"));
        Assert.assertEquals(record.get("EMAIL"), deserialized.get("EMAIL"));
        Assert.assertArrayEquals(record.values(), deserialized.values());
    }
}
