package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class CSVRecordTest {

    private enum Header {
        FIRST, SECOND, THIRD
    }

    private String[] values;
    private Map<String, Integer> headerMap;
    private CSVRecord record;
    private CSVRecord recordNoHeader;

    @Before
    public void setUp() {
        values = new String[]{"first", "second", "third"};
        headerMap = new HashMap<String, Integer>();
        headerMap.put("FIRST", 0);
        headerMap.put("SECOND", 1);
        headerMap.put("THIRD", 2);
        record = new CSVRecord(values, headerMap, "test comment", 1L);
        recordNoHeader = new CSVRecord(values, null, null, 0L);
    }

    @Test
    public void testConstructorWithNullValues() {
        final CSVRecord nullValuesRecord = new CSVRecord(null, headerMap, "comment", 5L);
        Assert.assertEquals(0, nullValuesRecord.size());
        Assert.assertNotNull(nullValuesRecord.values());
        Assert.assertEquals(0, nullValuesRecord.values().length);
        Assert.assertEquals("comment", nullValuesRecord.getComment());
        Assert.assertEquals(5L, nullValuesRecord.getRecordNumber());
    }

    @Test
    public void testGetByIndex() {
        Assert.assertEquals("first", record.get(0));
        Assert.assertEquals("second", record.get(1));
        Assert.assertEquals("third", record.get(2));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexNegative() {
        record.get(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        record.get(3);
    }

    @Test
    public void testGetByEnum() {
        Assert.assertEquals("first", record.get(Header.FIRST));
        Assert.assertEquals("second", record.get(Header.SECOND));
        Assert.assertEquals("third", record.get(Header.THIRD));
    }

    @Test
    public void testGetByName() {
        Assert.assertEquals("first", record.get("FIRST"));
        Assert.assertEquals("second", record.get("SECOND"));
        Assert.assertEquals("third", record.get("THIRD"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetByNameNoHeaderMapping() {
        recordNoHeader.get("FIRST");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameNotFound() {
        record.get("NON_EXISTENT");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameIndexOutOfBounds() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("OUT_OF_BOUNDS", 10);
        final CSVRecord rec = new CSVRecord(values, map, null, 1L);
        rec.get("OUT_OF_BOUNDS");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetByNameNegativeIndex() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("NEGATIVE", -1);
        final CSVRecord rec = new CSVRecord(values, map, null, 1L);
        rec.get("NEGATIVE");
    }

    @Test
    public void testGetComment() {
        Assert.assertEquals("test comment", record.getComment());
        Assert.assertNull(recordNoHeader.getComment());
    }

    @Test
    public void testGetRecordNumber() {
        Assert.assertEquals(1L, record.getRecordNumber());
        Assert.assertEquals(0L, recordNoHeader.getRecordNumber());
    }

    @Test
    public void testIsConsistent() {
        Assert.assertTrue(record.isConsistent());
        Assert.assertTrue(recordNoHeader.isConsistent());

        final Map<String, Integer> shortHeaderMap = new HashMap<String, Integer>();
        shortHeaderMap.put("FIRST", 0);
        final CSVRecord inconsistentRecord = new CSVRecord(values, shortHeaderMap, null, 1L);
        Assert.assertFalse(inconsistentRecord.isConsistent());

        final Map<String, Integer> longHeaderMap = new HashMap<String, Integer>();
        longHeaderMap.put("FIRST", 0);
        longHeaderMap.put("SECOND", 1);
        longHeaderMap.put("THIRD", 2);
        longHeaderMap.put("FOURTH", 3);
        final CSVRecord longInconsistentRecord = new CSVRecord(values, longHeaderMap, null, 1L);
        Assert.assertFalse(longInconsistentRecord.isConsistent());
    }

    @Test
    public void testIsMapped() {
        Assert.assertTrue(record.isMapped("FIRST"));
        Assert.assertTrue(record.isMapped("SECOND"));
        Assert.assertTrue(record.isMapped("THIRD"));
        Assert.assertFalse(record.isMapped("FOURTH"));
        Assert.assertFalse(record.isMapped(null));
        Assert.assertFalse(recordNoHeader.isMapped("FIRST"));
    }

    @Test
    public void testIsSet() {
        Assert.assertTrue(record.isSet("FIRST"));
        Assert.assertTrue(record.isSet("SECOND"));
        Assert.assertTrue(record.isSet("THIRD"));
        Assert.assertFalse(record.isSet("FOURTH"));
        Assert.assertFalse(record.isSet(null));
        Assert.assertFalse(recordNoHeader.isSet("FIRST"));

        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("IN_BOUNDS", 0);
        map.put("OUT_OF_BOUNDS", 10);
        final CSVRecord rec = new CSVRecord(new String[]{"val"}, map, null, 1L);
        Assert.assertTrue(rec.isSet("IN_BOUNDS"));
        Assert.assertFalse(rec.isSet("OUT_OF_BOUNDS"));
    }

    @Test
    public void testIterator() {
        final Iterator<String> it = record.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("first", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("second", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("third", it.next());
        Assert.assertFalse(it.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIteratorExhausted() {
        final Iterator<String> it = new CSVRecord(null, null, null, 0L).iterator();
        Assert.assertFalse(it.hasNext());
        it.next();
    }

    @Test
    public void testIterableForEach() {
        int count = 0;
        for (final String val : record) {
            Assert.assertEquals(values[count], val);
            count++;
        }
        Assert.assertEquals(3, count);
    }

    @Test
    public void testPutIn() {
        final Map<String, String> target = new HashMap<String, String>();
        final Map<String, String> result = record.putIn(target);
        Assert.assertSame(target, result);
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("first", result.get("FIRST"));
        Assert.assertEquals("second", result.get("SECOND"));
        Assert.assertEquals("third", result.get("THIRD"));
    }

    @Test(expected = NullPointerException.class)
    public void testPutInNullMappingThrowsNpe() {
        recordNoHeader.putIn(new HashMap<String, String>());
    }

    @Test
    public void testSize() {
        Assert.assertEquals(3, record.size());
        Assert.assertEquals(3, recordNoHeader.size());
        final CSVRecord emptyRecord = new CSVRecord(new String[0], null, null, 0L);
        Assert.assertEquals(0, emptyRecord.size());
    }

    @Test
    public void testToMap() {
        final Map<String, String> map = record.toMap();
        Assert.assertEquals(3, map.size());
        Assert.assertEquals("first", map.get("FIRST"));
        Assert.assertEquals("second", map.get("SECOND"));
        Assert.assertEquals("third", map.get("THIRD"));
    }

    @Test(expected = NullPointerException.class)
    public void testToMapNoHeaderThrowsNpe() {
        recordNoHeader.toMap();
    }

    @Test
    public void testToString() {
        Assert.assertEquals("[first, second, third]", record.toString());
        final CSVRecord empty = new CSVRecord(null, null, null, 0L);
        Assert.assertEquals("[]", empty.toString());
    }

    @Test
    public void testValues() {
        final String[] vals = record.values();
        Assert.assertNotNull(vals);
        Assert.assertArrayEquals(values, vals);
    }

    @Test
    public void testSerialization() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(record);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final CSVRecord deserialized = (CSVRecord) ois.readObject();

        Assert.assertEquals(record.getRecordNumber(), deserialized.getRecordNumber());
        Assert.assertEquals(record.getComment(), deserialized.getComment());
        Assert.assertEquals(record.size(), deserialized.size());
        Assert.assertEquals(record.get(0), deserialized.get(0));
        Assert.assertEquals(record.get("FIRST"), deserialized.get("FIRST"));
    }
}
