package org.apache.commons.csv;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CSVRecordTest {

    private String[] values;
    private Map<String, Integer> headerMap;
    private CSVRecord record;

    @Before
    public void setUp() {
        values = new String[]{"first", "second", "third"};
        headerMap = new HashMap<String, Integer>();
        headerMap.put("A", 0);
        headerMap.put("B", 1);
        headerMap.put("C", 2);
        record = new CSVRecord(values, headerMap, "comment text", 1L);
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
    public void testGetByName() {
        Assert.assertEquals("first", record.get("A"));
        Assert.assertEquals("second", record.get("B"));
        Assert.assertEquals("third", record.get("C"));
        Assert.assertNull(record.get("UNKNOWN"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetByNameNoMapping() {
        final CSVRecord recordNoMapping = new CSVRecord(values, null, "comment", 1L);
        recordNoMapping.get("A");
    }

    @Test
    public void testGetComment() {
        Assert.assertEquals("comment text", record.getComment());

        final CSVRecord recordNullComment = new CSVRecord(values, headerMap, null, 1L);
        Assert.assertNull(recordNullComment.getComment());
    }

    @Test
    public void testGetRecordNumber() {
        Assert.assertEquals(1L, record.getRecordNumber());

        final CSVRecord recordZero = new CSVRecord(values, headerMap, null, 0L);
        Assert.assertEquals(0L, recordZero.getRecordNumber());

        final CSVRecord recordNegative = new CSVRecord(values, headerMap, null, -5L);
        Assert.assertEquals(-5L, recordNegative.getRecordNumber());
    }

    @Test
    public void testSize() {
        Assert.assertEquals(3, record.size());

        final CSVRecord emptyRecord = new CSVRecord(new String[0], headerMap, null, 1L);
        Assert.assertEquals(0, emptyRecord.size());
    }

    @Test
    public void testNullValuesInConstructor() {
        final CSVRecord nullValuesRecord = new CSVRecord(null, headerMap, "comment", 1L);
        Assert.assertEquals(0, nullValuesRecord.size());
        Assert.assertNotNull(nullValuesRecord.values());
        Assert.assertEquals(0, nullValuesRecord.values().length);
    }

    @Test
    public void testValues() {
        final String[] returnedValues = record.values();
        Assert.assertArrayEquals(values, returnedValues);
    }

    @Test
    public void testIsConsistent() {
        Assert.assertTrue(record.isConsistent());

        final CSVRecord recordNoMapping = new CSVRecord(values, null, null, 1L);
        Assert.assertTrue(recordNoMapping.isConsistent());

        final Map<String, Integer> smallerMap = new HashMap<String, Integer>();
        smallerMap.put("A", 0);
        final CSVRecord inconsistentRecord = new CSVRecord(values, smallerMap, null, 1L);
        Assert.assertFalse(inconsistentRecord.isConsistent());

        final Map<String, Integer> largerMap = new HashMap<String, Integer>();
        largerMap.put("A", 0);
        largerMap.put("B", 1);
        largerMap.put("C", 2);
        largerMap.put("D", 3);
        final CSVRecord inconsistentRecord2 = new CSVRecord(values, largerMap, null, 1L);
        Assert.assertFalse(inconsistentRecord2.isConsistent());
    }

    @Test
    public void testIsMapped() {
        Assert.assertTrue(record.isMapped("A"));
        Assert.assertTrue(record.isMapped("B"));
        Assert.assertTrue(record.isMapped("C"));
        Assert.assertFalse(record.isMapped("D"));
        Assert.assertFalse(record.isMapped(null));

        final CSVRecord recordNoMapping = new CSVRecord(values, null, null, 1L);
        Assert.assertFalse(recordNoMapping.isMapped("A"));
        Assert.assertFalse(recordNoMapping.isMapped(null));
    }

    @Test
    public void testIsSet() {
        Assert.assertTrue(record.isSet("A"));
        Assert.assertTrue(record.isSet("B"));
        Assert.assertTrue(record.isSet("C"));
        Assert.assertFalse(record.isSet("D"));
        Assert.assertFalse(record.isSet(null));

        final Map<String, Integer> mapWithOutOfBounds = new HashMap<String, Integer>();
        mapWithOutOfBounds.put("A", 0);
        mapWithOutOfBounds.put("OutOfBounds", 5);
        mapWithOutOfBounds.put("ExactBoundary", 3);

        final CSVRecord outOfBoundsRecord = new CSVRecord(values, mapWithOutOfBounds, null, 1L);
        Assert.assertTrue(outOfBoundsRecord.isSet("A"));
        Assert.assertFalse(outOfBoundsRecord.isSet("OutOfBounds"));
        Assert.assertFalse(outOfBoundsRecord.isSet("ExactBoundary"));

        final CSVRecord recordNoMapping = new CSVRecord(values, null, null, 1L);
        Assert.assertFalse(recordNoMapping.isSet("A"));
    }

    @Test
    public void testIterator() {
        final List<String> list = new ArrayList<String>();
        for (final String val : record) {
            list.add(val);
        }
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("first", list.get(0));
        Assert.assertEquals("second", list.get(1));
        Assert.assertEquals("third", list.get(2));

        final Iterator<String> iterator = record.iterator();
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("first", iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("second", iterator.next());
        Assert.assertTrue(iterator.hasNext());
        Assert.assertEquals("third", iterator.next());
        Assert.assertFalse(iterator.hasNext());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("[first, second, third]", record.toString());

        final CSVRecord emptyRecord = new CSVRecord(new String[0], null, null, 0L);
        Assert.assertEquals("[]", emptyRecord.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(record);
        oos.close();

        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        final CSVRecord deserialized = (CSVRecord) ois.readObject();
        ois.close();

        Assert.assertEquals(record.getComment(), deserialized.getComment());
        Assert.assertEquals(record.getRecordNumber(), deserialized.getRecordNumber());
        Assert.assertEquals(record.size(), deserialized.size());
        Assert.assertArrayEquals(record.values(), deserialized.values());
        Assert.assertEquals(record.get("A"), deserialized.get("A"));
        Assert.assertEquals(record.get(1), deserialized.get(1));
        Assert.assertEquals(record.toString(), deserialized.toString());
    }
}
