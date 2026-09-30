package org.apache.commons.collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

public class ExtendedPropertiesTest {

    private ExtendedProperties props;

    @Before
    public void setUp() {
        props = new ExtendedProperties();
    }

    @Test
    public void testDefaultConstructorAndInitialization() {
        Assert.assertFalse(props.isInitialized());
        props.addProperty("test.init", "value");
        Assert.assertTrue(props.isInitialized());
    }

    @Test
    public void testFileConstructors() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File propFile = new File(tempDir, "test_ext_prop_" + System.currentTimeMillis() + ".properties");
        File defaultFile = new File(tempDir, "test_ext_prop_def_" + System.currentTimeMillis() + ".properties");

        try {
            FileOutputStream out = new FileOutputStream(propFile);
            out.write("prop.main=mainValue\n".getBytes(StandardCharsets.ISO_8859_1));
            out.close();

            FileOutputStream outDef = new FileOutputStream(defaultFile);
            outDef.write("prop.def=defaultValue\n".getBytes(StandardCharsets.ISO_8859_1));
            outDef.close();

            ExtendedProperties ep1 = new ExtendedProperties(propFile.getAbsolutePath());
            Assert.assertEquals("mainValue", ep1.getString("prop.main"));
            Assert.assertTrue(ep1.isInitialized());

            ExtendedProperties ep2 = new ExtendedProperties(propFile.getAbsolutePath(), defaultFile.getAbsolutePath());
            Assert.assertEquals("mainValue", ep2.getString("prop.main"));
            Assert.assertEquals("defaultValue", ep2.getString("prop.def"));
        } finally {
            if (propFile.exists()) {
                propFile.delete();
            }
            if (defaultFile.exists()) {
                defaultFile.delete();
            }
        }
    }

    @Test
    public void testGetAndSetInclude() {
        Assert.assertEquals("include", props.getInclude());
        props.setInclude("customInclude");
        Assert.assertEquals("customInclude", props.getInclude());
        props.setInclude("");
        Assert.assertNull(props.getInclude());
        props.setInclude(null);
        Assert.assertNull(props.getInclude());
    }

    @Test
    public void testPropertiesReaderAndLineContinuation() throws IOException {
        String data = "# Comment line\n" +
                "\n" +
                "key1 = value1\\\n" +
                "continued\n" +
                "key2 = normal\n";
        ExtendedProperties.PropertiesReader reader = new ExtendedProperties.PropertiesReader(new StringReader(data));
        String prop1 = reader.readProperty();
        Assert.assertEquals("key1 = value1continued", prop1);
        String prop2 = reader.readProperty();
        Assert.assertEquals("key2 = normal", prop2);
        String eof = reader.readProperty();
        Assert.assertNull(eof);
    }

    @Test
    public void testPropertiesTokenizer() {
        ExtendedProperties.PropertiesTokenizer tok = new ExtendedProperties.PropertiesTokenizer("a, b\\,c, d\\\\, e");
        Assert.assertTrue(tok.hasMoreTokens());
        Assert.assertEquals("a", tok.nextToken());
        Assert.assertEquals("b,c", tok.nextToken());
        Assert.assertEquals("d\\", tok.nextToken());
        Assert.assertEquals("e", tok.nextToken());
        Assert.assertFalse(tok.hasMoreTokens());
    }

    @Test
    public void testLoadSimple() throws IOException {
        String data = "app.name = TestApp\n" +
                "app.version = 1.0.0\n" +
                "app.items = item1, item2, item3\n";
        props.load(new ByteArrayInputStream(data.getBytes(StandardCharsets.ISO_8859_1)));

        Assert.assertEquals("TestApp", props.getString("app.name"));
        Assert.assertEquals("1.0.0", props.getString("app.version"));
        Vector items = props.getVector("app.items");
        Assert.assertEquals(3, items.size());
        Assert.assertEquals("item1", items.get(0));
        Assert.assertEquals("item2", items.get(1));
        Assert.assertEquals("item3", items.get(2));
    }

    @Test
    public void testLoadWithEncoding() throws IOException {
        String data = "encoded.key = Über\n";
        props.load(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)), "UTF-8");
        Assert.assertEquals("Über", props.getString("encoded.key"));

        ExtendedProperties fallbackProps = new ExtendedProperties();
        fallbackProps.load(new ByteArrayInputStream("foo=bar\n".getBytes(StandardCharsets.ISO_8859_1)), "UNSUPPORTED_ENC_12345");
        Assert.assertEquals("bar", fallbackProps.getString("foo"));
    }

    @Test
    public void testLoadWithInclude() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File incFile = new File(tempDir, "included_" + System.currentTimeMillis() + ".properties");
        try {
            FileOutputStream fos = new FileOutputStream(incFile);
            fos.write("included.key = includedValue\n".getBytes(StandardCharsets.ISO_8859_1));
            fos.close();

            String mainData = "main.key = mainVal\n" +
                    "include = " + incFile.getAbsolutePath().replace('\\', '/') + "\n";
            props.load(new ByteArrayInputStream(mainData.getBytes(StandardCharsets.ISO_8859_1)));

            Assert.assertEquals("mainVal", props.getString("main.key"));
            Assert.assertEquals("includedValue", props.getString("included.key"));
        } finally {
            if (incFile.exists()) {
                incFile.delete();
            }
        }
    }

    @Test
    public void testAddPropertyAndVectors() {
        props.addProperty("single", "val1");
        Assert.assertEquals("val1", props.getProperty("single"));

        props.addProperty("single", "val2");
        Object val = props.getProperty("single");
        Assert.assertTrue(val instanceof Vector || val instanceof List);
        List list = (List) val;
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("val1", list.get(0));
        Assert.assertEquals("val2", list.get(1));

        props.addProperty("single", "val3");
        Assert.assertEquals(3, ((List) props.getProperty("single")).size());

        props.addProperty("nonString", new Integer(42));
        Assert.assertEquals(new Integer(42), props.getProperty("nonString"));
    }

    @Test
    public void testSetProperty() {
        props.setProperty("item", "first");
        Assert.assertEquals("first", props.getString("item"));
        props.setProperty("item", "second");
        Assert.assertEquals("second", props.getString("item"));
    }

    @Test
    public void testInterpolation() {
        props.addProperty("base.url", "http://localhost");
        props.addProperty("base.port", "8080");
        props.addProperty("full.url", "${base.url}:${base.port}/api");
        props.addProperty("partial.url", "${base.url}/unknown/${undefined.var}");

        Assert.assertEquals("http://localhost:8080/api", props.getString("full.url"));
        Assert.assertEquals("http://localhost/unknown/${undefined.var}", props.getString("partial.url"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInterpolationLoop() {
        props.addProperty("loop1", "${loop2}");
        props.addProperty("loop2", "${loop1}");
        props.getString("loop1");
    }

    @Test
    public void testInterpolationWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("default.host", "example.com");

        ExtendedProperties child = new ExtendedProperties();
        child.defaults = defaults;
        child.addProperty("url", "http://${default.host}");

        Assert.assertEquals("http://example.com", child.getString("url"));
    }

    @Test
    public void testSave() throws IOException {
        props.setProperty("prop1", "value1");
        props.addProperty("list.prop", "item1, item2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header Comment");

        String savedContent = new String(out.toByteArray(), StandardCharsets.ISO_8859_1);
        Assert.assertTrue(savedContent.contains("Header Comment"));
        Assert.assertTrue(savedContent.contains("prop1=value1"));
        Assert.assertTrue(savedContent.contains("list.prop=item1"));
        Assert.assertTrue(savedContent.contains("list.prop=item2"));

        props.save(null, "ignored");
    }

    @Test
    public void testCombine() {
        ExtendedProperties p1 = new ExtendedProperties();
        p1.setProperty("key1", "val1");
        p1.setProperty("key2", "val2");

        ExtendedProperties p2 = new ExtendedProperties();
        p2.setProperty("key2", "newVal2");
        p2.setProperty("key3", "val3");

        p1.combine(p2);
        Assert.assertEquals("val1", p1.getString("key1"));
        Assert.assertEquals("newVal2", p1.getString("key2"));
        Assert.assertEquals("val3", p1.getString("key3"));
    }

    @Test
    public void testClearProperty() {
        props.setProperty("keyA", "valA");
        props.setProperty("keyB", "valB");

        Assert.assertTrue(props.containsKey("keyA"));
        props.clearProperty("keyA");
        Assert.assertFalse(props.containsKey("keyA"));
        Assert.assertNull(props.getProperty("keyA"));

        props.clearProperty("nonExistent");
    }

    @Test
    public void testGetKeys() {
        props.setProperty("app.name", "A");
        props.setProperty("app.desc", "B");
        props.setProperty("db.url", "C");

        Iterator allKeys = props.getKeys();
        List<Object> keyList = new ArrayList<Object>();
        while (allKeys.hasNext()) {
            keyList.add(allKeys.next());
        }
        Assert.assertEquals(3, keyList.size());
        Assert.assertEquals("app.name", keyList.get(0));
        Assert.assertEquals("app.desc", keyList.get(1));
        Assert.assertEquals("db.url", keyList.get(2));

        Iterator appKeys = props.getKeys("app.");
        List<Object> appList = new ArrayList<Object>();
        while (appKeys.hasNext()) {
            appList.add(appKeys.next());
        }
        Assert.assertEquals(2, appList.size());
        Assert.assertEquals("app.name", appList.get(0));
        Assert.assertEquals("app.desc", appList.get(1));
    }

    @Test
    public void testSubset() {
        props.setProperty("db.driver", "com.mysql.jdbc.Driver");
        props.setProperty("db.url", "jdbc:mysql://localhost/test");
        props.setProperty("db", "root");
        props.setProperty("other", "value");

        ExtendedProperties subset = props.subset("db");
        Assert.assertNotNull(subset);
        Assert.assertEquals("com.mysql.jdbc.Driver", subset.getString("driver"));
        Assert.assertEquals("jdbc:mysql://localhost/test", subset.getString("url"));
        Assert.assertEquals("root", subset.getString("db"));
        Assert.assertNull(subset.getString("other"));

        Assert.assertNull(props.subset("non.existing"));
    }

    @Test
    public void testDisplay() {
        props.setProperty("key1", "val1");
        props.display();
    }

    @Test
    public void testGetString() {
        props.setProperty("str", "hello");
        Assert.assertEquals("hello", props.getString("str"));
        Assert.assertEquals("hello", props.getString("str", "def"));
        Assert.assertEquals("def", props.getString("missing", "def"));
        Assert.assertNull(props.getString("missing"));

        props.addProperty("multiStr", "first");
        props.addProperty("multiStr", "second");
        Assert.assertEquals("first", props.getString("multiStr"));

        props.put("nonString", new Integer(100));
        try {
            props.getString("nonString");
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetProperties() {
        props.setProperty("props", "k1=v1, k2=v2");
        Properties p = props.getProperties("props");
        Assert.assertEquals("v1", p.getProperty("k1"));
        Assert.assertEquals("v2", p.getProperty("k2"));

        Properties defaultProps = new Properties();
        defaultProps.put("k3", "v3");
        Properties p2 = props.getProperties("props", defaultProps);
        Assert.assertEquals("v3", p2.getProperty("k3"));

        props.setProperty("badProps", "noEqualsHere");
        try {
            props.getProperties("badProps");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testGetStringArray() {
        props.addProperty("array", "one");
        props.addProperty("array", "two");

        String[] arr = props.getStringArray("array");
        Assert.assertEquals(2, arr.length);
        Assert.assertEquals("one", arr[0]);
        Assert.assertEquals("two", arr[1]);

        props.setProperty("singleArray", "solo");
        String[] arr2 = props.getStringArray("singleArray");
        Assert.assertEquals(1, arr2.length);
        Assert.assertEquals("solo", arr2[0]);

        String[] missingArr = props.getStringArray("missing");
        Assert.assertEquals(0, missingArr.length);

        props.put("nonStringArr", new Integer(5));
        try {
            props.getStringArray("nonStringArr");
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetVectorAndGetList() {
        props.addProperty("items", "a");
        props.addProperty("items", "b");

        Vector vec = props.getVector("items");
        Assert.assertEquals(2, vec.size());
        Assert.assertEquals("a", vec.get(0));

        props.setProperty("singleItem", "val");
        Vector vec2 = props.getVector("singleItem");
        Assert.assertEquals(1, vec2.size());
        Assert.assertEquals("val", vec2.get(0));

        Vector defaultVec = new Vector();
        defaultVec.add("def");
        Assert.assertEquals(defaultVec, props.getVector("missing", defaultVec));
        Assert.assertEquals(0, props.getVector("missing").size());

        List list = props.getList("items");
        Assert.assertEquals(2, list.size());

        props.setProperty("singleListItem", "valL");
        List list2 = props.getList("singleListItem");
        Assert.assertEquals(1, list2.size());
        Assert.assertEquals("valL", list2.get(0));

        List defaultList = new ArrayList();
        defaultList.add("defL");
        Assert.assertEquals(defaultList, props.getList("missingList", defaultList));
        Assert.assertEquals(0, props.getList("missingList").size());
    }

    @Test
    public void testGetBoolean() {
        props.setProperty("b1", "true");
        props.setProperty("b2", "on");
        props.setProperty("b3", "yes");
        props.setProperty("b4", "false");
        props.setProperty("b5", "off");
        props.setProperty("b6", "no");
        props.setProperty("b7", "unknown");

        Assert.assertTrue(props.getBoolean("b1"));
        Assert.assertTrue(props.getBoolean("b2"));
        Assert.assertTrue(props.getBoolean("b3"));
        Assert.assertFalse(props.getBoolean("b4"));
        Assert.assertFalse(props.getBoolean("b5"));
        Assert.assertFalse(props.getBoolean("b6"));
        Assert.assertFalse(props.getBoolean("b7", false));
        Assert.assertTrue(props.getBoolean("b7", true));

        Assert.assertTrue(props.getBoolean("missing", true));
        Assert.assertEquals(Boolean.TRUE, props.getBoolean("b1", Boolean.FALSE));

        props.put("boolObj", Boolean.TRUE);
        Assert.assertTrue(props.getBoolean("boolObj"));

        try {
            props.getBoolean("missing");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        props.put("nonBool", new Integer(10));
        try {
            props.getBoolean("nonBool", Boolean.FALSE);
            Assert.fail("Expected ClassCastException");
        } catch (ClassCastException expected) {
        }
    }

    @Test
    public void testGetByte() {
        props.setProperty("byteKey", "12");
        Assert.assertEquals((byte) 12, props.getByte("byteKey"));
        Assert.assertEquals((byte) 12, props.getByte("byteKey", (byte) 1));
        Assert.assertEquals(new Byte((byte) 12), props.getByte("byteKey", new Byte((byte) 1)));

        Assert.assertEquals((byte) 5, props.getByte("missingByte", (byte) 5));
        Assert.assertEquals(new Byte((byte) 5), props.getByte("missingByte", new Byte((byte) 5)));

        props.put("byteObj", new Byte((byte) 20));
        Assert.assertEquals((byte) 20, props.getByte("byteObj"));

        try {
            props.getByte("missingByte");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        props.put("nonByte", "invalidNumber");
        try {
            props.getByte("nonByte");
            Assert.fail("Expected NumberFormatException");
        } catch (NumberFormatException expected) {
        }
    }

    @Test
    public void testGetShort() {
        props.setProperty("shortKey", "123");
        Assert.assertEquals((short) 123, props.getShort("shortKey"));
        Assert.assertEquals((short) 123, props.getShort("shortKey", (short) 1));
        Assert.assertEquals(new Short((short) 123), props.getShort("shortKey", new Short((short) 1)));

        Assert.assertEquals((short) 50, props.getShort("missingShort", (short) 50));
        Assert.assertEquals(new Short((short) 50), props.getShort("missingShort", new Short((short) 50)));

        props.put("shortObj", new Short((short) 200));
        Assert.assertEquals((short) 200, props.getShort("shortObj"));

        try {
            props.getShort("missingShort");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }
    }

    @Test
    public void testGetIntegerAndInt() {
        props.setProperty("intKey", "12345");
        Assert.assertEquals(12345, props.getInt("intKey"));
        Assert.assertEquals(12345, props.getInt("intKey", 1));
        Assert.assertEquals(12345, props.getInteger("intKey"));
        Assert.assertEquals(12345, props.getInteger("intKey", 1));
        Assert.assertEquals(new Integer(12345), props.getInteger("intKey", new Integer(1)));

        Assert.assertEquals(500, props.getInt("missingInt", 500));
        Assert.assertEquals(500, props.getInteger("missingInt", 500));
        Assert.assertEquals(new Integer(500), props.getInteger("missingInt", new Integer(500)));

        props.put("intObj", new Integer(2000));
        Assert.assertEquals(2000, props.getInteger("intObj"));

        try {
            props.getInteger("missingInt");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }
    }

    @Test
    public void testGetLong() {
        props.setProperty("longKey", "1234567890123");
        Assert.assertEquals(1234567890123L, props.getLong("longKey"));
        Assert.assertEquals(1234567890123L, props.getLong("longKey", 1L));
        Assert.assertEquals(new Long(1234567890123L), props.getLong("longKey", new Long(1L)));

        Assert.assertEquals(5000L, props.getLong("missingLong", 5000L));
        Assert.assertEquals(new Long(5000L), props.getLong("missingLong", new Long(5000L)));

        props.put("longObj", new Long(20000L));
        Assert.assertEquals(20000L, props.getLong("longObj"));

        try {
            props.getLong("missingLong");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }
    }

    @Test
    public void testGetFloat() {
        props.setProperty("floatKey", "12.34");
        Assert.assertEquals(12.34f, props.getFloat("floatKey"), 0.0001f);
        Assert.assertEquals(12.34f, props.getFloat("floatKey", 1.0f), 0.0001f);
        Assert.assertEquals(new Float(12.34f), props.getFloat("floatKey", new Float(1.0f)));

        Assert.assertEquals(5.5f, props.getFloat("missingFloat", 5.5f), 0.0001f);
        Assert.assertEquals(new Float(5.5f), props.getFloat("missingFloat", new Float(5.5f)));

        props.put("floatObj", new Float(20.5f));
        Assert.assertEquals(20.5f, props.getFloat("floatObj"), 0.0001f);

        try {
            props.getFloat("missingFloat");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }
    }

    @Test
    public void testGetDouble() {
        props.setProperty("doubleKey", "123.456");
        Assert.assertEquals(123.456, props.getDouble("doubleKey"), 0.0001);
        Assert.assertEquals(123.456, props.getDouble("doubleKey", 1.0), 0.0001);
        Assert.assertEquals(new Double(123.456), props.getDouble("doubleKey", new Double(1.0)));

        Assert.assertEquals(50.5, props.getDouble("missingDouble", 50.5), 0.0001);
        Assert.assertEquals(new Double(50.5), props.getDouble("missingDouble", new Double(50.5)));

        props.put("doubleObj", new Double(200.5));
        Assert.assertEquals(200.5, props.getDouble("doubleObj"), 0.0001);

        try {
            props.getDouble("missingDouble");
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
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
    public void testPutPutAllRemove() {
        Object old = props.put("key1", "val1");
        Assert.assertNull(old);
        Assert.assertEquals("val1", props.get("key1"));

        old = props.put("key1", "val2");
        Assert.assertEquals("val1", old);

        Map<String, String> map = new HashMap<String, String>();
        map.put("map1", "mval1");
        map.put("map2", "mval2");
        props.putAll(map);
        Assert.assertEquals("mval1", props.getString("map1"));
        Assert.assertEquals("mval2", props.getString("map2"));

        ExtendedProperties otherEp = new ExtendedProperties();
        otherEp.setProperty("epKey", "epVal");
        props.putAll(otherEp);
        Assert.assertEquals("epVal", props.getString("epKey"));

        Object removed = props.remove("epKey");
        Assert.assertEquals("epVal", removed);
        Assert.assertFalse(props.containsKey("epKey"));
    }
}
