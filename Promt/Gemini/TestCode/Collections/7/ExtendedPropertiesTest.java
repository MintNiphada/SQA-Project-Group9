package org.apache.commons.collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

public class ExtendedPropertiesTest {

    private File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = File.createTempFile("extprop_test", "");
        tempDir.delete();
        tempDir.mkdir();
    }

    @After
    public void tearDown() throws Exception {
        if (tempDir != null && tempDir.exists()) {
            File[] files = tempDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            tempDir.delete();
        }
    }

    @Test
    public void testEmptyConstructor() {
        ExtendedProperties props = new ExtendedProperties();
        Assert.assertFalse(props.isInitialized());
        Assert.assertNull(props.getProperty("nonExistent"));
    }

    @Test
    public void testFileConstructors() throws IOException {
        File propFile = new File(tempDir, "test.properties");
        FileOutputStream out = new FileOutputStream(propFile);
        out.write("prop1 = value1\nprop2 = value2\n".getBytes("ISO-8859-1"));
        out.close();

        File defaultFile = new File(tempDir, "default.properties");
        FileOutputStream defOut = new FileOutputStream(defaultFile);
        defOut.write("prop2 = defValue2\ndefProp = defValue\n".getBytes("ISO-8859-1"));
        defOut.close();

        ExtendedProperties props = new ExtendedProperties(propFile.getAbsolutePath());
        Assert.assertTrue(props.isInitialized());
        Assert.assertEquals("value1", props.getString("prop1"));
        Assert.assertEquals("value2", props.getString("prop2"));

        ExtendedProperties propsWithDefaults = new ExtendedProperties(propFile.getAbsolutePath(), defaultFile.getAbsolutePath());
        Assert.assertEquals("value1", propsWithDefaults.getString("prop1"));
        Assert.assertEquals("value2", propsWithDefaults.getString("prop2"));
        Assert.assertEquals("defValue", propsWithDefaults.getString("defProp"));
    }

    @Test
    public void testIncludeHandling() {
        ExtendedProperties props = new ExtendedProperties();
        Assert.assertEquals("include", props.getInclude());

        props.setInclude("custom_include");
        Assert.assertEquals("custom_include", props.getInclude());

        props.setInclude(null);
        Assert.assertNull(props.getInclude());

        props.setInclude("");
        Assert.assertNull(props.getInclude());
    }

    @Test
    public void testLoadWithIncludes() throws IOException {
        File includedFile = new File(tempDir, "sub.properties");
        FileOutputStream subOut = new FileOutputStream(includedFile);
        subOut.write("sub.key = sub.val\n".getBytes("ISO-8859-1"));
        subOut.close();

        File mainFile = new File(tempDir, "main.properties");
        FileOutputStream mainOut = new FileOutputStream(mainFile);
        String mainContent = "main.key = main.val\n" +
                "include = sub.properties\n" +
                "include = ./" + includedFile.getName() + "\n" +
                "include = " + includedFile.getAbsolutePath() + "\n" +
                "empty.key =\n";
        mainOut.write(mainContent.getBytes("ISO-8859-1"));
        mainOut.close();

        ExtendedProperties props = new ExtendedProperties(mainFile.getAbsolutePath());
        Assert.assertEquals("main.val", props.getString("main.key"));
        Assert.assertEquals("sub.val", props.getString("sub.key"));
        Assert.assertNull(props.getString("empty.key", null));
    }

    @Test
    public void testLoadEncodings() throws IOException {
        String data = "key = val\n";
        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(data.getBytes("UTF-8")), "UTF-8");
        Assert.assertEquals("val", props.getString("key"));

        ExtendedProperties props2 = new ExtendedProperties();
        props2.load(new ByteArrayInputStream(data.getBytes("ISO-8859-1")), "INVALID_ENCODING_NAME_123");
        Assert.assertEquals("val", props2.getString("key"));
    }

    @Test
    public void testMultilineAndCommentsAndEscapes() throws IOException {
        String data = "# This is a comment\n" +
                "   # Another indented comment\n" +
                "\n" +
                "multi = part1 \\\n" +
                "        part2 \\\n" +
                "        part3\n" +
                "escaped.comma = val1\\,val2,val3\n" +
                "escaped.slash = path\\\\dir\n" +
                "ends.with.even.slashes = test\\\\\n";

        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(data.getBytes("ISO-8859-1")));

        Assert.assertEquals("part1part2part3", props.getString("multi"));
        Assert.assertEquals("val1,val2", props.getStringArray("escaped.comma")[0]);
        Assert.assertEquals("val3", props.getStringArray("escaped.comma")[1]);
        Assert.assertEquals("path\\dir", props.getString("escaped.slash"));
    }

    @Test
    public void testAddAndSetProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "val1");
        Assert.assertEquals("val1", props.getString("key1"));

        // Adding second time becomes a list internally
        props.addProperty("key1", "val2");
        String[] arr = props.getStringArray("key1");
        Assert.assertEquals(2, arr.length);
        Assert.assertEquals("val1", arr[0]);
        Assert.assertEquals("val2", arr[1]);

        // Adding third time appends to the list
        props.addProperty("key1", "val3");
        Assert.assertEquals(3, props.getStringArray("key1").length);

        // Multiple values added as comma-separated string
        props.addProperty("multi", "a,b,c");
        Assert.assertEquals(3, props.getStringArray("multi").length);
        Assert.assertEquals("a", props.getStringArray("multi")[0]);

        // Non-string object addition
        props.addProperty("intKey", new Integer(42));
        Assert.assertEquals(42, props.getInt("intKey"));

        // setProperty clears previous values
        props.setProperty("key1", "resetVal");
        Assert.assertEquals("resetVal", props.getString("key1"));
        Assert.assertEquals(1, props.getStringArray("key1").length);
    }

    @Test
    public void testClearProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("a", "1");
        props.addProperty("b", "2");
        props.addProperty("c", "3");

        props.clearProperty("b");
        Assert.assertNull(props.getProperty("b"));

        Iterator it = props.getKeys();
        Assert.assertEquals("a", it.next());
        Assert.assertEquals("c", it.next());
        Assert.assertFalse(it.hasNext());

        // Clear non-existent
        props.clearProperty("non_existent");
    }

    @Test
    public void testCombineAndPutAll() {
        ExtendedProperties p1 = new ExtendedProperties();
        p1.addProperty("key1", "val1");
        p1.addProperty("key2", "val2");

        ExtendedProperties p2 = new ExtendedProperties();
        p2.addProperty("key2", "newVal2");
        p2.addProperty("key3", "val3");

        p1.combine(p2);
        Assert.assertEquals("val1", p1.getString("key1"));
        Assert.assertEquals("newVal2", p1.getString("key2"));
        Assert.assertEquals("val3", p1.getString("key3"));

        Map map = new HashMap();
        map.put("map1", "mVal1");
        map.put("map2", "mVal2");
        p1.putAll(map);
        Assert.assertEquals("mVal1", p1.getString("map1"));

        ExtendedProperties p3 = new ExtendedProperties();
        p3.addProperty("p3Key", "p3Val");
        p1.putAll(p3);
        Assert.assertEquals("p3Val", p1.getString("p3Key"));
    }

    @Test
    public void testGetKeysWithPrefix() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("db.host", "localhost");
        props.addProperty("db.port", "3306");
        props.addProperty("app.name", "test");

        Iterator it = props.getKeys("db.");
        List list = new ArrayList();
        while (it.hasNext()) {
            list.add(it.next());
        }
        Assert.assertEquals(2, list.size());
        Assert.assertTrue(list.contains("db.host"));
        Assert.assertTrue(list.contains("db.port"));
    }

    @Test
    public void testSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("prefix", "base");
        props.addProperty("prefix.key1", "val1");
        props.addProperty("prefix.key2", "val2");
        props.addProperty("other.key", "val3");

        ExtendedProperties sub = props.subset("prefix");
        Assert.assertNotNull(sub);
        Assert.assertEquals("base", sub.getString("prefix"));
        Assert.assertEquals("val1", sub.getString("key1"));
        Assert.assertEquals("val2", sub.getString("key2"));
        Assert.assertNull(sub.getString("other.key"));

        Assert.assertNull(props.subset("nonexistent"));
    }

    @Test
    public void testDisplay() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "val");
        PrintStream origOut = System.out;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            props.display();
            Assert.assertTrue(baos.toString().contains("key => val"));
        } finally {
            System.setOut(origOut);
        }
    }

    @Test
    public void testInterpolation() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("base", "http://localhost");
        props.addProperty("port", "8080");
        props.addProperty("url", "${base}:${port}/api");
        props.addProperty("undefined", "prefix_${nonexistent}_suffix");

        Assert.assertEquals("http://localhost:8080/api", props.getString("url"));
        Assert.assertEquals("prefix_${nonexistent}_suffix", props.getString("undefined"));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defVar", "fromDefault");
        props.defaults = defaults;
        props.addProperty("defTest", "val_${defVar}");
        Assert.assertEquals("val_fromDefault", props.getString("defTest"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInterpolationLoop() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("a", "${b}");
        props.addProperty("b", "${a}");
        props.getString("a");
    }

    @Test
    public void testGetString() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("str", "hello");
        props.addProperty("list", "item1");
        props.addProperty("list", "item2");

        Assert.assertEquals("hello", props.getString("str"));
        Assert.assertEquals("hello", props.getString("str", "def"));
        Assert.assertEquals("item1", props.getString("list"));
        Assert.assertEquals("default", props.getString("missing", "default"));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defOnly", "defVal");
        props.defaults = defaults;
        Assert.assertEquals("defVal", props.getString("defOnly"));

        props.put("object", new Object());
        try {
            props.getString("object");
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetProperties() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("propList", "k1=v1,k2=v2");

        Properties p = props.getProperties("propList");
        Assert.assertEquals("v1", p.getProperty("k1"));
        Assert.assertEquals("v2", p.getProperty("k2"));

        Properties defaults = new Properties();
        defaults.setProperty("defK", "defV");
        Properties pWithDef = props.getProperties("propList", defaults);
        Assert.assertEquals("defV", pWithDef.getProperty("defK"));

        props.addProperty("malformed", "noEqualsSign");
        try {
            props.getProperties("malformed");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetStringArray() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("arr", "v1,v2");
        props.addProperty("single", "v0");

        String[] arr = props.getStringArray("arr");
        Assert.assertEquals(2, arr.length);
        Assert.assertEquals("v1", arr[0]);
        Assert.assertEquals("v2", arr[1]);

        String[] single = props.getStringArray("single");
        Assert.assertEquals(1, single.length);
        Assert.assertEquals("v0", single[0]);

        Assert.assertEquals(0, props.getStringArray("missing").length);

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defArr", "d1,d2");
        props.defaults = defaults;
        Assert.assertEquals(2, props.getStringArray("defArr").length);

        props.put("invalid", new Integer(5));
        try {
            props.getStringArray("invalid");
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetVectorAndList() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("items", "a,b");
        props.addProperty("single", "singleVal");

        Vector v = props.getVector("items");
        Assert.assertEquals(2, v.size());

        Vector singleV = props.getVector("single");
        Assert.assertEquals(1, singleV.size());
        Assert.assertEquals("singleVal", singleV.get(0));

        Vector defV = new Vector();
        defV.add("default");
        Assert.assertEquals("default", props.getVector("missing", defV).get(0));
        Assert.assertEquals(0, props.getVector("missing_nodef").size());

        List l = props.getList("items");
        Assert.assertEquals(2, l.size());

        List singleL = props.getList("single");
        Assert.assertEquals(1, singleL.size());

        List defL = new ArrayList();
        defL.add("defaultList");
        Assert.assertEquals("defaultList", props.getList("missing", defL).get(0));
        Assert.assertEquals(0, props.getList("missing_nodef").size());

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defKey", "valDef");
        props.defaults = defaults;
        Assert.assertEquals(1, props.getVector("defKey").size());
        Assert.assertEquals(1, props.getList("defKey").size());

        props.put("invalid", new Integer(10));
        try {
            props.getVector("invalid");
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }

        try {
            props.getList("invalid");
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testTestBoolean() {
        ExtendedProperties props = new ExtendedProperties();
        Assert.assertEquals("true", props.testBoolean("true"));
        Assert.assertEquals("true", props.testBoolean("TRUE"));
        Assert.assertEquals("true", props.testBoolean("on"));
        Assert.assertEquals("true", props.testBoolean("yes"));

        Assert.assertEquals("false", props.testBoolean("false"));
        Assert.assertEquals("false", props.testBoolean("FALSE"));
        Assert.assertEquals("false", props.testBoolean("off"));
        Assert.assertEquals("false", props.testBoolean("no"));

        Assert.assertNull(props.testBoolean("invalid"));
    }

    @Test
    public void testGetBoolean() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("b1", "true");
        props.addProperty("b2", Boolean.FALSE);

        Assert.assertTrue(props.getBoolean("b1"));
        Assert.assertFalse(props.getBoolean("b2"));
        Assert.assertTrue(props.getBoolean("missing", true));
        Assert.assertEquals(Boolean.TRUE, props.getBoolean("missing", Boolean.TRUE));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defBool", "true");
        props.defaults = defaults;
        Assert.assertTrue(props.getBoolean("defBool"));

        try {
            props.getBoolean("missing_no_def");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        props.put("invalid", new Object());
        try {
            props.getBoolean("invalid", Boolean.TRUE);
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetByte() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("byteStr", "12");
        props.addProperty("byteObj", new Byte((byte) 34));

        Assert.assertEquals((byte) 12, props.getByte("byteStr"));
        Assert.assertEquals((byte) 34, props.getByte("byteObj"));
        Assert.assertEquals((byte) 56, props.getByte("missing", (byte) 56));
        Assert.assertEquals(new Byte((byte) 78), props.getByte("missing", new Byte((byte) 78)));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defByte", "90");
        props.defaults = defaults;
        Assert.assertEquals((byte) 90, props.getByte("defByte"));

        try {
            props.getByte("missing_no_def");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        props.put("invalid", new Object());
        try {
            props.getByte("invalid", (Byte) null);
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetShort() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("shortStr", "123");
        props.addProperty("shortObj", new Short((short) 456));

        Assert.assertEquals((short) 123, props.getShort("shortStr"));
        Assert.assertEquals((short) 456, props.getShort("shortObj"));
        Assert.assertEquals((short) 789, props.getShort("missing", (short) 789));
        Assert.assertEquals(new Short((short) 999), props.getShort("missing", new Short((short) 999)));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defShort", "111");
        props.defaults = defaults;
        Assert.assertEquals((short) 111, props.getShort("defShort"));

        try {
            props.getShort("missing_no_def");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        props.put("invalid", new Object());
        try {
            props.getShort("invalid", (Short) null);
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetInteger() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("intStr", "12345");
        props.addProperty("intObj", new Integer(67890));

        Assert.assertEquals(12345, props.getInt("intStr"));
        Assert.assertEquals(12345, props.getInteger("intStr"));
        Assert.assertEquals(67890, props.getInteger("intObj"));
        Assert.assertEquals(9999, props.getInt("missing", 9999));
        Assert.assertEquals(9999, props.getInteger("missing", 9999));
        Assert.assertEquals(new Integer(8888), props.getInteger("missing", new Integer(8888)));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defInt", "333");
        props.defaults = defaults;
        Assert.assertEquals(333, props.getInt("defInt"));

        try {
            props.getInteger("missing_no_def");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        props.put("invalid", new Object());
        try {
            props.getInteger("invalid", (Integer) null);
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetLong() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("longStr", "123456789");
        props.addProperty("longObj", new Long(987654321L));

        Assert.assertEquals(123456789L, props.getLong("longStr"));
        Assert.assertEquals(987654321L, props.getLong("longObj"));
        Assert.assertEquals(55555L, props.getLong("missing", 55555L));
        Assert.assertEquals(new Long(44444L), props.getLong("missing", new Long(44444L)));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defLong", "77777");
        props.defaults = defaults;
        Assert.assertEquals(77777L, props.getLong("defLong"));

        try {
            props.getLong("missing_no_def");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        props.put("invalid", new Object());
        try {
            props.getLong("invalid", (Long) null);
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetFloat() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("floatStr", "12.34");
        props.addProperty("floatObj", new Float(56.78f));

        Assert.assertEquals(12.34f, props.getFloat("floatStr"), 0.001f);
        Assert.assertEquals(56.78f, props.getFloat("floatObj"), 0.001f);
        Assert.assertEquals(90.12f, props.getFloat("missing", 90.12f), 0.001f);
        Assert.assertEquals(new Float(34.56f), props.getFloat("missing", new Float(34.56f)));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defFloat", "78.90");
        props.defaults = defaults;
        Assert.assertEquals(78.90f, props.getFloat("defFloat"), 0.001f);

        try {
            props.getFloat("missing_no_def");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        props.put("invalid", new Object());
        try {
            props.getFloat("invalid", (Float) null);
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetDouble() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("doubleStr", "123.456");
        props.addProperty("doubleObj", new Double(789.012));

        Assert.assertEquals(123.456, props.getDouble("doubleStr"), 0.0001);
        Assert.assertEquals(789.012, props.getDouble("doubleObj"), 0.0001);
        Assert.assertEquals(345.678, props.getDouble("missing", 345.678), 0.0001);
        Assert.assertEquals(new Double(901.234), props.getDouble("missing", new Double(901.234)));

        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defDouble", "555.666");
        props.defaults = defaults;
        Assert.assertEquals(555.666, props.getDouble("defDouble"), 0.0001);

        try {
            props.getDouble("missing_no_def");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        props.put("invalid", new Object());
        try {
            props.getDouble("invalid", (Double) null);
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testConvertProperties() {
        Properties p = new Properties();
        p.setProperty("prop1", "val1");
        p.setProperty("prop2", "val2");

        ExtendedProperties ep = ExtendedProperties.convertProperties(p);
        Assert.assertEquals("val1", ep.getString("prop1"));
        Assert.assertEquals("val2", ep.getString("prop2"));
    }

    @Test
    public void testSave() throws IOException {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("single", "val,ue");
        props.addProperty("multi", "val1");
        props.addProperty("multi", "val2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "# Header Comment");
        props.save(null, "ignored");

        String savedContent = new String(out.toByteArray());
        Assert.assertTrue(savedContent.contains("# Header Comment"));
        Assert.assertTrue(savedContent.contains("single=val\\,ue"));
        Assert.assertTrue(savedContent.contains("multi=val1"));
        Assert.assertTrue(savedContent.contains("multi=val2"));
    }

    @Test
    public void testPropertiesReaderAndTokenizer() throws IOException {
        String data = "line1 \\\n  continuation\nline2\n";
        ExtendedProperties.PropertiesReader reader = new ExtendedProperties.PropertiesReader(new StringReader(data));
        Assert.assertEquals("line1 continuation", reader.readProperty());
        Assert.assertEquals("line2", reader.readProperty());
        Assert.assertNull(reader.readProperty());

        ExtendedProperties.PropertiesTokenizer tokenizer = new ExtendedProperties.PropertiesTokenizer("a\\,b,c");
        Assert.assertTrue(tokenizer.hasMoreTokens());
        Assert.assertEquals("a,b", tokenizer.nextToken());
        Assert.assertTrue(tokenizer.hasMoreTokens());
        Assert.assertEquals("c", tokenizer.nextToken());
        Assert.assertFalse(tokenizer.hasMoreTokens());
    }
}
