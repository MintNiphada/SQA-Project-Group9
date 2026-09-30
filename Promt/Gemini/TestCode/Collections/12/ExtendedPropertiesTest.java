package org.apache.commons.collections;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

public class ExtendedPropertiesTest {

    @Test
    public void testDefaultConstructorAndInitialization() {
        ExtendedProperties ep = new ExtendedProperties();
        Assert.assertFalse(ep.isInitialized());
        ep.addProperty("key1", "val1");
        Assert.assertTrue(ep.isInitialized());
        Assert.assertEquals("val1", ep.getProperty("key1"));
        Assert.assertEquals("val1", ep.getString("key1"));
    }

    @Test
    public void testFileConstructors() throws IOException {
        File tempFile = File.createTempFile("test_props", ".properties");
        File defaultFile = File.createTempFile("test_default_props", ".properties");
        tempFile.deleteOnExit();
        defaultFile.deleteOnExit();

        try (FileOutputStream out1 = new FileOutputStream(tempFile);
             FileOutputStream out2 = new FileOutputStream(defaultFile)) {
            out1.write("file.key = mainValue\n".getBytes());
            out2.write("file.key = defaultValue\ndefault.only = exists\n".getBytes());
        }

        ExtendedProperties ep1 = new ExtendedProperties(tempFile.getAbsolutePath());
        Assert.assertEquals("mainValue", ep1.getString("file.key"));
        Assert.assertTrue(ep1.isInitialized());

        ExtendedProperties ep2 = new ExtendedProperties(tempFile.getAbsolutePath(), defaultFile.getAbsolutePath());
        Assert.assertEquals("mainValue", ep2.getString("file.key"));
        Assert.assertEquals("exists", ep2.getString("default.only"));
    }

    @Test
    public void testGetAndSetInclude() {
        ExtendedProperties ep = new ExtendedProperties();
        Assert.assertEquals("include", ep.getInclude());

        ep.setInclude("customInclude");
        Assert.assertEquals("customInclude", ep.getInclude());

        ep.setInclude(null);
        Assert.assertNull(ep.getInclude());

        ep.setInclude("");
        Assert.assertNull(ep.getInclude());
    }

    @Test
    public void testLoadWithEncodingAndComments() throws IOException {
        String data = "# This is a comment\n" +
                      "   # Another comment with spaces\n" +
                      "\n" +
                      "multiline = line1 \\\n" +
                      "    line2 \\\n" +
                      "    line3\n" +
                      "single = value\n";

        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(data.getBytes("ISO-8859-1")), "ISO-8859-1");

        Assert.assertEquals("value", ep.getString("single"));
        Assert.assertEquals("line1line2line3", ep.getString("multiline"));
    }

    @Test
    public void testLoadWithInvalidEncodingFallback() throws IOException {
        String data = "key=val\n";
        ExtendedProperties ep = new ExtendedProperties();
        ep.load(new ByteArrayInputStream(data.getBytes()), "INVALID_CHARSET_NAME_123");
        Assert.assertEquals("val", ep.getString("key"));
    }

    @Test
    public void testLoadIncludeFiles() throws IOException {
        File dir = new File(System.getProperty("java.io.tmpdir"));
        File childFile = new File(dir, "child_test_" + System.currentTimeMillis() + ".properties");
        File parentFile = new File(dir, "parent_test_" + System.currentTimeMillis() + ".properties");

        try {
            try (FileOutputStream out = new FileOutputStream(childFile)) {
                out.write("child.key = fromChild\n".getBytes());
            }

            try (FileOutputStream out = new FileOutputStream(parentFile)) {
                String parentData = "parent.key = fromParent\n" +
                        "include = " + childFile.getName() + "\n";
                out.write(parentData.getBytes());
            }

            ExtendedProperties ep = new ExtendedProperties(parentFile.getAbsolutePath());
            Assert.assertEquals("fromParent", ep.getString("parent.key"));
            Assert.assertEquals("fromChild", ep.getString("child.key"));

            // Test absolute path include and ./ relative path include
            File parentFile2 = new File(dir, "parent2_test_" + System.currentTimeMillis() + ".properties");
            try (FileOutputStream out = new FileOutputStream(parentFile2)) {
                String parentData = "include = ." + File.separator + childFile.getName() + "\n";
                out.write(parentData.getBytes());
            }
            ExtendedProperties ep2 = new ExtendedProperties(parentFile2.getAbsolutePath());
            Assert.assertEquals("fromChild", ep2.getString("child.key"));
            parentFile2.delete();
        } finally {
            childFile.delete();
            parentFile.delete();
        }
    }

    @Test
    public void testListAndVectorPropertyHandling() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("letters", "a, b, c");
        ep.addProperty("letters", "d");

        List list = ep.getList("letters");
        Assert.assertEquals(4, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));
        Assert.assertEquals("d", list.get(3));

        Vector vec = ep.getVector("letters");
        Assert.assertEquals(4, vec.size());

        String[] arr = ep.getStringArray("letters");
        Assert.assertEquals(4, arr.length);
        Assert.assertEquals("a", arr[0]);
        Assert.assertEquals("d", arr[3]);
    }

    @Test
    public void testAddPropertyNonStringAndEscapes() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("escaped", "a\\,b, c\\\\d");
        List list = ep.getList("escaped");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("a,b", list.get(0));
        Assert.assertEquals("c\\d", list.get(1));

        ep.addProperty("intProp", Integer.valueOf(100));
        Assert.assertEquals(Integer.valueOf(100), ep.getProperty("intProp"));

        ep.setProperty("single", "first");
        Assert.assertEquals("first", ep.getString("single"));
        ep.setProperty("single", "second");
        Assert.assertEquals("second", ep.getString("single"));
    }

    @Test
    public void testInterpolation() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("base.url", "http://localhost");
        ep.addProperty("port", "8080");
        ep.addProperty("full.url", "${base.url}:${port}/api");
        ep.addProperty("unresolved", "${missing.var}/test");

        Assert.assertEquals("http://localhost:8080/api", ep.getString("full.url"));
        Assert.assertEquals("${missing.var}/test", ep.getString("unresolved"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInterpolationLoop() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("loopA", "${loopB}");
        ep.addProperty("loopB", "${loopA}");
        ep.getString("loopA");
    }

    @Test
    public void testInterpolationWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defVar", "fromDefault");

        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;
        ep.addProperty("myVar", "Value: ${defVar}");

        Assert.assertEquals("Value: fromDefault", ep.getString("myVar"));
    }

    @Test
    public void testInterpolateHelperEdgeCases() {
        ExtendedProperties ep = new ExtendedProperties();
        Assert.assertNull(ep.interpolate(null));
        Assert.assertEquals("simpleText", ep.interpolate("simpleText"));
    }

    @Test
    public void testSave() throws IOException {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("singleKey", "value1");
        ep.addProperty("listKey", "elem1, elem2\\,escaped");
        ep.addProperty("nullValueKey", null);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ep.save(out, "Header Comment");
        String content = out.toString();

        Assert.assertTrue(content.contains("Header Comment"));
        Assert.assertTrue(content.contains("singleKey=value1"));
        Assert.assertTrue(content.contains("listKey=elem1"));
        Assert.assertTrue(content.contains("listKey=elem2\\,escaped"));

        // Save with null stream
        ep.save(null, "Header");
    }

    @Test
    public void testCombineClearAndKeys() {
        ExtendedProperties ep1 = new ExtendedProperties();
        ep1.addProperty("a.1", "val1");
        ep1.addProperty("a.2", "val2");
        ep1.addProperty("b.1", "val3");

        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.addProperty("a.1", "newVal1");
        ep2.addProperty("c.1", "val4");

        ep1.combine(ep2);
        Assert.assertEquals("newVal1", ep1.getString("a.1"));
        Assert.assertEquals("val4", ep1.getString("c.1"));

        ep1.clearProperty("a.1");
        Assert.assertNull(ep1.getProperty("a.1"));

        Iterator iter = ep1.getKeys("a.");
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("a.2", iter.next());
        Assert.assertFalse(iter.hasNext());

        ExtendedProperties subset = ep1.subset("b");
        Assert.assertNotNull(subset);
        Assert.assertEquals("val3", subset.getString("1"));

        ExtendedProperties subsetExact = ep1.subset("b.1");
        Assert.assertNotNull(subsetExact);
        Assert.assertEquals("val3", subsetExact.getString("b.1"));

        Assert.assertNull(ep1.subset("nonexistent"));
    }

    @Test
    public void testDisplay() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("testKey", "testValue");
        ep.display();
    }

    @Test
    public void testGetProperties() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("propGroup", "key1=val1, key2=val2");

        Properties props = ep.getProperties("propGroup");
        Assert.assertEquals("val1", props.getProperty("key1"));
        Assert.assertEquals("val2", props.getProperty("key2"));

        ep.addProperty("badProp", "onlyValueNoEquals");
        try {
            ep.getProperties("badProp");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetVectorAndListDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defList", "item1, item2");

        ExtendedProperties ep = new ExtendedProperties();
        ep.defaults = defaults;

        Assert.assertEquals(2, ep.getList("defList").size());
        Assert.assertEquals(2, ep.getVector("defList").size());
        Assert.assertEquals(2, ep.getStringArray("defList").length);

        Assert.assertEquals(0, ep.getList("missing").size());
        Assert.assertEquals(0, ep.getVector("missing").size());
        Assert.assertEquals(0, ep.getStringArray("missing").length);

        List defL = Arrays.asList("x", "y");
        Vector defV = new Vector(defL);
        Assert.assertEquals(2, ep.getList("missing", defL).size());
        Assert.assertEquals(2, ep.getVector("missing", defV).size());

        ep.addProperty("singleStr", "justOne");
        Assert.assertEquals(1, ep.getList("singleStr").size());
        Assert.assertEquals(1, ep.getVector("singleStr").size());
        Assert.assertEquals(1, ep.getStringArray("singleStr").length);
    }

    @Test
    public void testGetBoolean() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("b.true", "true");
        ep.addProperty("b.on", "on");
        ep.addProperty("b.yes", "yes");
        ep.addProperty("b.false", "false");
        ep.addProperty("b.off", "off");
        ep.addProperty("b.no", "no");
        ep.addProperty("b.boolObj", Boolean.TRUE);

        Assert.assertTrue(ep.getBoolean("b.true"));
        Assert.assertTrue(ep.getBoolean("b.on"));
        Assert.assertTrue(ep.getBoolean("b.yes"));
        Assert.assertFalse(ep.getBoolean("b.false"));
        Assert.assertFalse(ep.getBoolean("b.off"));
        Assert.assertFalse(ep.getBoolean("b.no"));
        Assert.assertTrue(ep.getBoolean("b.boolObj"));

        Assert.assertTrue(ep.getBoolean("missing", true));
        Assert.assertEquals(Boolean.FALSE, ep.getBoolean("missing", Boolean.FALSE));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defBool", "true");
        ep.defaults = defaults;
        Assert.assertTrue(ep.getBoolean("defBool"));

        try {
            ep.getBoolean("totallyMissing");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }
    }

    @Test
    public void testGetNumericTypes() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.addProperty("num.byte", "10");
        ep.addProperty("num.byteObj", Byte.valueOf((byte) 10));
        ep.addProperty("num.short", "20");
        ep.addProperty("num.shortObj", Short.valueOf((short) 20));
        ep.addProperty("num.int", "30");
        ep.addProperty("num.intObj", Integer.valueOf(30));
        ep.addProperty("num.long", "40");
        ep.addProperty("num.longObj", Long.valueOf(40L));
        ep.addProperty("num.float", "50.5");
        ep.addProperty("num.floatObj", Float.valueOf(50.5f));
        ep.addProperty("num.double", "60.6");
        ep.addProperty("num.doubleObj", Double.valueOf(60.6d));

        Assert.assertEquals((byte) 10, ep.getByte("num.byte"));
        Assert.assertEquals((byte) 10, ep.getByte("num.byteObj"));
        Assert.assertEquals((byte) 1, ep.getByte("missing", (byte) 1));
        Assert.assertEquals(Byte.valueOf((byte) 2), ep.getByte("missing", Byte.valueOf((byte) 2)));

        Assert.assertEquals((short) 20, ep.getShort("num.short"));
        Assert.assertEquals((short) 20, ep.getShort("num.shortObj"));
        Assert.assertEquals((short) 2, ep.getShort("missing", (short) 2));
        Assert.assertEquals(Short.valueOf((short) 3), ep.getShort("missing", Short.valueOf((short) 3)));

        Assert.assertEquals(30, ep.getInt("num.int"));
        Assert.assertEquals(30, ep.getInt("num.intObj"));
        Assert.assertEquals(30, ep.getInteger("num.int"));
        Assert.assertEquals(3, ep.getInt("missing", 3));
        Assert.assertEquals(3, ep.getInteger("missing", 3));
        Assert.assertEquals(Integer.valueOf(4), ep.getInteger("missing", Integer.valueOf(4)));

        Assert.assertEquals(40L, ep.getLong("num.long"));
        Assert.assertEquals(40L, ep.getLong("num.longObj"));
        Assert.assertEquals(4L, ep.getLong("missing", 4L));
        Assert.assertEquals(Long.valueOf(5L), ep.getLong("missing", Long.valueOf(5L)));

        Assert.assertEquals(50.5f, ep.getFloat("num.float"), 0.0001f);
        Assert.assertEquals(50.5f, ep.getFloat("num.floatObj"), 0.0001f);
        Assert.assertEquals(5.5f, ep.getFloat("missing", 5.5f), 0.0001f);
        Assert.assertEquals(Float.valueOf(6.5f), ep.getFloat("missing", Float.valueOf(6.5f)));

        Assert.assertEquals(60.6d, ep.getDouble("num.double"), 0.0001d);
        Assert.assertEquals(60.6d, ep.getDouble("num.doubleObj"), 0.0001d);
        Assert.assertEquals(6.6d, ep.getDouble("missing", 6.6d), 0.0001d);
        Assert.assertEquals(Double.valueOf(7.6d), ep.getDouble("missing", Double.valueOf(7.6d)));

        ExtendedProperties defs = new ExtendedProperties();
        defs.addProperty("num.byte", "11");
        defs.addProperty("num.short", "21");
        defs.addProperty("num.int", "31");
        defs.addProperty("num.long", "41");
        defs.addProperty("num.float", "51.5");
        defs.addProperty("num.double", "61.6");
        ExtendedProperties epDefaults = new ExtendedProperties();
        epDefaults.defaults = defs;

        Assert.assertEquals((byte) 11, epDefaults.getByte("num.byte"));
        Assert.assertEquals((short) 21, epDefaults.getShort("num.short"));
        Assert.assertEquals(31, epDefaults.getInteger("num.int"));
        Assert.assertEquals(41L, epDefaults.getLong("num.long"));
        Assert.assertEquals(51.5f, epDefaults.getFloat("num.float"), 0.0001f);
        Assert.assertEquals(61.6d, epDefaults.getDouble("num.double"), 0.0001d);
    }

    @Test
    public void testNumericNoSuchElementException() {
        ExtendedProperties ep = new ExtendedProperties();
        try {
            ep.getByte("missing");
            Assert.fail();
        } catch (NoSuchElementException e) {}

        try {
            ep.getShort("missing");
            Assert.fail();
        } catch (NoSuchElementException e) {}

        try {
            ep.getInteger("missing");
            Assert.fail();
        } catch (NoSuchElementException e) {}

        try {
            ep.getLong("missing");
            Assert.fail();
        } catch (NoSuchElementException e) {}

        try {
            ep.getFloat("missing");
            Assert.fail();
        } catch (NoSuchElementException e) {}

        try {
            ep.getDouble("missing");
            Assert.fail();
        } catch (NoSuchElementException e) {}
    }

    @Test
    public void testClassCastExceptions() {
        ExtendedProperties ep = new ExtendedProperties();
        ep.put("objKey", new Object());

        try {
            ep.getString("objKey");
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getStringArray("objKey");
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getVector("objKey");
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getList("objKey");
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getBoolean("objKey", Boolean.TRUE);
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getByte("objKey", (Byte) null);
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getShort("objKey", (Short) null);
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getInteger("objKey", (Integer) null);
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getLong("objKey", (Long) null);
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getFloat("objKey", (Float) null);
            Assert.fail();
        } catch (ClassCastException e) {}

        try {
            ep.getDouble("objKey", (Double) null);
            Assert.fail();
        } catch (ClassCastException e) {}
    }

    @Test
    public void testConvertProperties() {
        Properties parentProps = new Properties();
        parentProps.setProperty("parentKey", "parentVal");

        Properties props = new Properties(parentProps);
        props.setProperty("childKey", "childVal");

        ExtendedProperties ep = ExtendedProperties.convertProperties(props);
        Assert.assertEquals("childVal", ep.getString("childKey"));
        Assert.assertEquals("parentVal", ep.getString("parentKey"));
    }

    @Test
    public void testMapOperationsPutPutAllRemove() {
        ExtendedProperties ep = new ExtendedProperties();
        Object old = ep.put("key1", "val1");
        Assert.assertNull(old);
        Assert.assertEquals("val1", ep.get("key1"));

        old = ep.put("key1", "val2");
        Assert.assertNotNull(old);

        Map map = new HashMap();
        map.put("k1", "v1");
        map.put("k2", "v2");
        ep.putAll(map);
        Assert.assertEquals("v1", ep.getString("k1"));
        Assert.assertEquals("v2", ep.getString("k2"));

        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.addProperty("k3", "v3");
        ep.putAll(ep2);
        Assert.assertEquals("v3", ep.getString("k3"));

        Object removed = ep.remove("k3");
        Assert.assertEquals("v3", removed);
        Assert.assertNull(ep.getProperty("k3"));
    }

    @Test
    public void testPropertiesReaderAndTokenizer() throws IOException {
        String data = "key1 = val1, val2\\\n, val3\n" +
                      "key2 = val4\\\\ \n" +
                      "# comment\n" +
                      "key3 = line1\\\n" +
                      "line2\n";
        ExtendedProperties.PropertiesReader reader = new ExtendedProperties.PropertiesReader(new StringReader(data));
        String prop1 = reader.readProperty();
        Assert.assertEquals("key1 = val1, val2, val3", prop1);

        String prop2 = reader.readProperty();
        Assert.assertEquals("key2 = val4\\\\", prop2);

        String prop3 = reader.readProperty();
        Assert.assertEquals("key3 = line1line2", prop3);

        Assert.assertNull(reader.readProperty());

        ExtendedProperties.PropertiesTokenizer tok = new ExtendedProperties.PropertiesTokenizer("a\\,b,c");
        Assert.assertTrue(tok.hasMoreTokens());
        Assert.assertEquals("a\\,b", tok.nextToken());
        Assert.assertEquals("c", tok.nextToken());
        Assert.assertFalse(tok.hasMoreTokens());
    }

    @Test
    public void testTestBooleanInvalid() {
        ExtendedProperties ep = new ExtendedProperties();
        Assert.assertNull(ep.testBoolean("invalid_bool"));
    }
}
