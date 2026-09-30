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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

public class ExtendedPropertiesTest {

    private ExtendedProperties ep;

    @Before
    public void setUp() {
        ep = new ExtendedProperties();
    }

    @Test
    public void testDefaultConstructorAndInitialization() {
        Assert.assertFalse(ep.isInitialized());
        ep.addProperty("key1", "val1");
        Assert.assertTrue(ep.isInitialized());
    }

    @Test
    public void testFileConstructors() throws Exception {
        File tempFile = File.createTempFile("extprop", ".properties");
        File tempDefaultFile = File.createTempFile("extprop_def", ".properties");
        tempFile.deleteOnExit();
        tempDefaultFile.deleteOnExit();

        try {
            FileOutputStream fos1 = new FileOutputStream(tempFile);
            fos1.write("file.key = file.val\n".getBytes("ISO-8859-1"));
            fos1.close();

            FileOutputStream fos2 = new FileOutputStream(tempDefaultFile);
            fos2.write("def.key = def.val\n".getBytes("ISO-8859-1"));
            fos2.close();

            ExtendedProperties ep1 = new ExtendedProperties(tempFile.getAbsolutePath());
            Assert.assertTrue(ep1.isInitialized());
            Assert.assertEquals("file.val", ep1.getString("file.key"));

            ExtendedProperties ep2 = new ExtendedProperties(tempFile.getAbsolutePath(), tempDefaultFile.getAbsolutePath());
            Assert.assertEquals("file.val", ep2.getString("file.key"));
            Assert.assertEquals("def.val", ep2.getString("def.key"));
        } finally {
            tempFile.delete();
            tempDefaultFile.delete();
        }
    }

    @Test
    public void testGetAndSetInclude() {
        Assert.assertEquals("include", ep.getInclude());
        ep.setInclude("import");
        Assert.assertEquals("import", ep.getInclude());
        ep.setInclude("include");
    }

    @Test
    public void testLoadSimpleAndMultiline() throws Exception {
        String data = "# This is a comment\n" +
                "key1 = val1\n" +
                "longKey = hello \\\n" +
                "world\n" +
                "emptyKey = \n" +
                "listKey = one, two, three\n" +
                "escapedComma = one\\,stillOne, two\n";
        ep.load(new ByteArrayInputStream(data.getBytes("ISO-8859-1")));

        Assert.assertEquals("val1", ep.getString("key1"));
        Assert.assertEquals("helloworld", ep.getString("longKey"));
        Assert.assertNull(ep.getProperty("emptyKey"));

        String[] list = ep.getStringArray("listKey");
        Assert.assertEquals(3, list.length);
        Assert.assertEquals("one", list[0]);
        Assert.assertEquals("two", list[1]);
        Assert.assertEquals("three", list[2]);

        String[] escList = ep.getStringArray("escapedComma");
        Assert.assertEquals(2, escList.length);
        Assert.assertEquals("one,stillOne", escList[0]);
        Assert.assertEquals("two", escList[1]);
    }

    @Test
    public void testLoadWithEncoding() throws Exception {
        String data = "testKey = testVal\n";
        ep.load(new ByteArrayInputStream(data.getBytes("UTF-8")), "UTF-8");
        Assert.assertEquals("testVal", ep.getString("testKey"));

        // Fallback on invalid encoding
        ExtendedProperties ep2 = new ExtendedProperties();
        ep2.load(new ByteArrayInputStream(data.getBytes("ISO-8859-1")), "INVALID_ENCODING_NAME");
        Assert.assertEquals("testVal", ep2.getString("testKey"));
    }

    @Test
    public void testIncludeDirective() throws Exception {
        File parentDir = File.createTempFile("extpropdir", "");
        parentDir.delete();
        parentDir.mkdir();

        File inc1 = new File(parentDir, "inc1.properties");
        File main = new File(parentDir, "main.properties");

        try {
            FileOutputStream fos1 = new FileOutputStream(inc1);
            fos1.write("inc.key = inc.value\n".getBytes("ISO-8859-1"));
            fos1.close();

            FileOutputStream fos2 = new FileOutputStream(main);
            fos2.write(("include = inc1.properties\nmain.key = main.value\n").getBytes("ISO-8859-1"));
            fos2.close();

            ExtendedProperties mainEp = new ExtendedProperties(main.getAbsolutePath());
            Assert.assertEquals("main.value", mainEp.getString("main.key"));
            Assert.assertEquals("inc.value", mainEp.getString("inc.key"));
        } finally {
            inc1.delete();
            main.delete();
            parentDir.delete();
        }
    }

    @Test
    public void testIncludeDirectiveRelativeAndAbsolute() throws Exception {
        File tempIncluded = File.createTempFile("included", ".properties");
        tempIncluded.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempIncluded);
        fos.write("abs.key = abs.val\n".getBytes("ISO-8859-1"));
        fos.close();

        File parentDir = File.createTempFile("pdir", "");
        parentDir.delete();
        parentDir.mkdir();
        File relIncluded = new File(parentDir, "rel.properties");
        FileOutputStream fosRel = new FileOutputStream(relIncluded);
        fosRel.write("rel.key = rel.val\n".getBytes("ISO-8859-1"));
        fosRel.close();

        File main = new File(parentDir, "main.properties");
        FileOutputStream fosMain = new FileOutputStream(main);
        String fileSep = System.getProperty("file.separator");
        String content = "include = ." + fileSep + "rel.properties\n" +
                "include = " + tempIncluded.getAbsolutePath() + "\n";
        fosMain.write(content.getBytes("ISO-8859-1"));
        fosMain.close();

        try {
            ExtendedProperties mainEp = new ExtendedProperties(main.getAbsolutePath());
            Assert.assertEquals("abs.val", mainEp.getString("abs.key"));
            Assert.assertEquals("rel.val", mainEp.getString("rel.key"));
        } finally {
            tempIncluded.delete();
            relIncluded.delete();
            main.delete();
            parentDir.delete();
        }
    }

    @Test
    public void testAddAndSetProperty() {
        ep.addProperty("prop", "a");
        ep.addProperty("prop", "b");
        ep.addProperty("prop", "c");

        List list = ep.getList("prop");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("a", list.get(0));
        Assert.assertEquals("b", list.get(1));
        Assert.assertEquals("c", list.get(2));

        ep.setProperty("prop", "single");
        Assert.assertEquals("single", ep.getString("prop"));

        ep.addProperty("nonString", Integer.valueOf(100));
        Assert.assertEquals(Integer.valueOf(100), ep.getProperty("nonString"));
    }

    @Test
    public void testClearProperty() {
        ep.addProperty("key1", "val1");
        ep.addProperty("key2", "val2");
        Assert.assertEquals("val1", ep.getString("key1"));

        ep.clearProperty("key1");
        Assert.assertNull(ep.getProperty("key1"));
        Assert.assertEquals("val2", ep.getString("key2"));

        // Clear non-existent
        ep.clearProperty("unknown");
    }

    @Test
    public void testKeysAndGetKeysWithPrefix() {
        ep.addProperty("server.host", "localhost");
        ep.addProperty("server.port", "8080");
        ep.addProperty("client.host", "remotehost");

        Iterator allKeys = ep.getKeys();
        List keyList = new ArrayList();
        while (allKeys.hasNext()) {
            keyList.add(allKeys.next());
        }
        Assert.assertEquals(3, keyList.size());
        Assert.assertEquals("server.host", keyList.get(0));
        Assert.assertEquals("server.port", keyList.get(1));
        Assert.assertEquals("client.host", keyList.get(2));

        Iterator serverKeys = ep.getKeys("server");
        List serverList = new ArrayList();
        while (serverKeys.hasNext()) {
            serverList.add(serverKeys.next());
        }
        Assert.assertEquals(2, serverList.size());
        Assert.assertTrue(serverList.contains("server.host"));
        Assert.assertTrue(serverList.contains("server.port"));
    }

    @Test
    public void testSubset() {
        ep.addProperty("db.user", "admin");
        ep.addProperty("db.password", "secret");
        ep.addProperty("db", "root");
        ep.addProperty("app.name", "testApp");

        ExtendedProperties subset = ep.subset("db");
        Assert.assertNotNull(subset);
        Assert.assertEquals("admin", subset.getString("user"));
        Assert.assertEquals("secret", subset.getString("password"));
        Assert.assertEquals("root", subset.getString("db"));
        Assert.assertNull(subset.getProperty("app.name"));

        ExtendedProperties emptySubset = ep.subset("nonexistent");
        Assert.assertNull(emptySubset);
    }

    @Test
    public void testCombine() {
        ExtendedProperties other = new ExtendedProperties();
        other.addProperty("key1", "otherVal1");
        other.addProperty("key2", "otherVal2");

        ep.addProperty("key1", "initialVal1");
        ep.addProperty("key3", "initialVal3");

        ep.combine(other);
        Assert.assertEquals("otherVal1", ep.getString("key1"));
        Assert.assertEquals("otherVal2", ep.getString("key2"));
        Assert.assertEquals("initialVal3", ep.getString("key3"));
    }

    @Test
    public void testSave() throws IOException {
        ep.addProperty("simple", "value");
        ep.addProperty("escaped", "val,ue\\test");
        ep.addProperty("multi", "val1");
        ep.addProperty("multi", "val2");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ep.save(baos, "Header Comment");
        String output = baos.toString();

        Assert.assertTrue(output.contains("Header Comment"));
        Assert.assertTrue(output.contains("simple=value"));
        Assert.assertTrue(output.contains("escaped=val\\,ue\\\\test"));
        Assert.assertTrue(output.contains("multi=val1"));
        Assert.assertTrue(output.contains("multi=val2"));

        // Save with null output
        ep.save(null, "Header");
    }

    @Test
    public void testDisplay() {
        ep.addProperty("key1", "val1");
        ep.display();
    }

    @Test
    public void testInterpolation() {
        ep.addProperty("base.url", "http://localhost");
        ep.addProperty("port", "8080");
        ep.addProperty("full.url", "${base.url}:${port}/api");

        Assert.assertEquals("http://localhost:8080/api", ep.getString("full.url"));

        // Missing variable remains untouched
        ep.addProperty("missing", "${undefined.var}/test");
        Assert.assertEquals("${undefined.var}/test", ep.getString("missing"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInterpolationLoop() {
        ep.addProperty("var1", "${var2}");
        ep.addProperty("var2", "${var3}");
        ep.addProperty("var3", "${var1}");
        ep.getString("var1");
    }

    @Test
    public void testGetStringWithDefaults() {
        ExtendedProperties def = new ExtendedProperties();
        def.addProperty("defKey", "defVal");
        def.addProperty("interpolatedDef", "${defKey}/sub");

        ExtendedProperties main = new ExtendedProperties();
        main.defaults = def;

        Assert.assertEquals("defVal", main.getString("defKey"));
        Assert.assertEquals("defVal/sub", main.getString("interpolatedDef"));
        Assert.assertEquals("fallback", main.getString("nonExistent", "fallback"));
    }

    @Test(expected = ClassCastException.class)
    public void testGetStringClassCastException() {
        ep.put("intKey", Integer.valueOf(42));
        ep.getString("intKey");
    }

    @Test
    public void testGetProperties() {
        ep.addProperty("props", "key1=val1,key2=val2");
        Properties p = ep.getProperties("props");
        Assert.assertEquals("val1", p.getProperty("key1"));
        Assert.assertEquals("val2", p.getProperty("key2"));

        Properties defaults = new Properties();
        defaults.setProperty("defKey", "defVal");
        Properties p2 = ep.getProperties("props", defaults);
        Assert.assertEquals("defVal", p2.getProperty("defKey"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertiesMalformed() {
        ep.addProperty("props", "invalidToken");
        ep.getProperties("props");
    }

    @Test
    public void testGetStringArray() {
        ep.addProperty("strArray", "a,b,c");
        String[] arr = ep.getStringArray("strArray");
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, arr);

        ep.setProperty("singleStr", "alone");
        String[] arr2 = ep.getStringArray("singleStr");
        Assert.assertArrayEquals(new String[]{"alone"}, arr2);

        String[] empty = ep.getStringArray("nonExistent");
        Assert.assertEquals(0, empty.length);
    }

    @Test(expected = ClassCastException.class)
    public void testGetStringArrayClassCastException() {
        ep.put("invalid", Integer.valueOf(1));
        ep.getStringArray("invalid");
    }

    @Test
    public void testGetVector() {
        ep.addProperty("vec", "a,b");
        Vector v = ep.getVector("vec");
        Assert.assertEquals(2, v.size());
        Assert.assertEquals("a", v.get(0));
        Assert.assertEquals("b", v.get(1));

        ep.setProperty("singleVec", "x");
        Vector v2 = ep.getVector("singleVec");
        Assert.assertEquals(1, v2.size());
        Assert.assertEquals("x", v2.get(0));

        Vector defaultVec = new Vector();
        defaultVec.add("def");
        Assert.assertEquals(defaultVec, ep.getVector("nonExistent", defaultVec));
        Assert.assertEquals(new Vector(), ep.getVector("nonExistent", null));
    }

    @Test(expected = ClassCastException.class)
    public void testGetVectorClassCastException() {
        ep.put("obj", Integer.valueOf(10));
        ep.getVector("obj");
    }

    @Test
    public void testGetList() {
        ep.addProperty("list", "a,b");
        List l = ep.getList("list");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("a", l.get(0));
        Assert.assertEquals("b", l.get(1));

        ep.setProperty("singleList", "x");
        List l2 = ep.getList("singleList");
        Assert.assertEquals(1, l2.size());
        Assert.assertEquals("x", l2.get(0));

        List defaultList = new ArrayList();
        defaultList.add("def");
        Assert.assertEquals(defaultList, ep.getList("nonExistent", defaultList));
        Assert.assertEquals(new ArrayList(), ep.getList("nonExistent", null));
    }

    @Test(expected = ClassCastException.class)
    public void testGetListClassCastException() {
        ep.put("obj", Integer.valueOf(10));
        ep.getList("obj");
    }

    @Test
    public void testBooleanConversions() {
        ep.addProperty("b1", "true");
        ep.addProperty("b2", "on");
        ep.addProperty("b3", "yes");
        ep.addProperty("b4", "false");
        ep.addProperty("b5", "off");
        ep.addProperty("b6", "no");
        ep.addProperty("b7", Boolean.TRUE);

        Assert.assertTrue(ep.getBoolean("b1"));
        Assert.assertTrue(ep.getBoolean("b2"));
        Assert.assertTrue(ep.getBoolean("b3"));
        Assert.assertFalse(ep.getBoolean("b4"));
        Assert.assertFalse(ep.getBoolean("b5"));
        Assert.assertFalse(ep.getBoolean("b6"));
        Assert.assertTrue(ep.getBoolean("b7"));

        Assert.assertTrue(ep.getBoolean("unknown", true));
        Assert.assertFalse(ep.getBoolean("unknown", false));
        Assert.assertEquals(Boolean.TRUE, ep.getBoolean("unknown", Boolean.TRUE));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetBooleanNotFound() {
        ep.getBoolean("not.found");
    }

    @Test(expected = ClassCastException.class)
    public void testGetBooleanClassCast() {
        ep.put("int", Integer.valueOf(1));
        ep.getBoolean("int");
    }

    @Test
    public void testTestBoolean() {
        Assert.assertEquals("true", ep.testBoolean("TRUE"));
        Assert.assertEquals("true", ep.testBoolean("On"));
        Assert.assertEquals("true", ep.testBoolean("YeS"));
        Assert.assertEquals("false", ep.testBoolean("FALSE"));
        Assert.assertEquals("false", ep.testBoolean("Off"));
        Assert.assertEquals("false", ep.testBoolean("No"));
        Assert.assertNull(ep.testBoolean("invalid"));
    }

    @Test
    public void testByteConversions() {
        ep.addProperty("byteStr", "12");
        ep.addProperty("byteObj", Byte.valueOf((byte) 34));

        Assert.assertEquals((byte) 12, ep.getByte("byteStr"));
        Assert.assertEquals((byte) 34, ep.getByte("byteObj"));
        Assert.assertEquals((byte) 56, ep.getByte("unknown", (byte) 56));
        Assert.assertEquals(Byte.valueOf((byte) 78), ep.getByte("unknown", Byte.valueOf((byte) 78)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetByteNotFound() {
        ep.getByte("unknown");
    }

    @Test(expected = ClassCastException.class)
    public void testGetByteClassCast() {
        ep.put("list", new ArrayList());
        ep.getByte("list");
    }

    @Test
    public void testShortConversions() {
        ep.addProperty("shortStr", "123");
        ep.addProperty("shortObj", Short.valueOf((short) 456));

        Assert.assertEquals((short) 123, ep.getShort("shortStr"));
        Assert.assertEquals((short) 456, ep.getShort("shortObj"));
        Assert.assertEquals((short) 789, ep.getShort("unknown", (short) 789));
        Assert.assertEquals(Short.valueOf((short) 999), ep.getShort("unknown", Short.valueOf((short) 999)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetShortNotFound() {
        ep.getShort("unknown");
    }

    @Test(expected = ClassCastException.class)
    public void testGetShortClassCast() {
        ep.put("list", new ArrayList());
        ep.getShort("list");
    }

    @Test
    public void testIntAndIntegerConversions() {
        ep.addProperty("intStr", "1234");
        ep.addProperty("intObj", Integer.valueOf(5678));

        Assert.assertEquals(1234, ep.getInt("intStr"));
        Assert.assertEquals(1234, ep.getInteger("intStr"));
        Assert.assertEquals(5678, ep.getInt("intObj"));
        Assert.assertEquals(5678, ep.getInteger("intObj"));
        Assert.assertEquals(9999, ep.getInt("unknown", 9999));
        Assert.assertEquals(9999, ep.getInteger("unknown", 9999));
        Assert.assertEquals(Integer.valueOf(8888), ep.getInteger("unknown", Integer.valueOf(8888)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetIntNotFound() {
        ep.getInteger("unknown");
    }

    @Test(expected = ClassCastException.class)
    public void testGetIntClassCast() {
        ep.put("list", new ArrayList());
        ep.getInteger("list");
    }

    @Test
    public void testLongConversions() {
        ep.addProperty("longStr", "123456789012");
        ep.addProperty("longObj", Long.valueOf(987654321098L));

        Assert.assertEquals(123456789012L, ep.getLong("longStr"));
        Assert.assertEquals(987654321098L, ep.getLong("longObj"));
        Assert.assertEquals(5555L, ep.getLong("unknown", 5555L));
        Assert.assertEquals(Long.valueOf(6666L), ep.getLong("unknown", Long.valueOf(6666L)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetLongNotFound() {
        ep.getLong("unknown");
    }

    @Test(expected = ClassCastException.class)
    public void testGetLongClassCast() {
        ep.put("list", new ArrayList());
        ep.getLong("list");
    }

    @Test
    public void testFloatConversions() {
        ep.addProperty("floatStr", "3.14");
        ep.addProperty("floatObj", Float.valueOf(2.718f));

        Assert.assertEquals(3.14f, ep.getFloat("floatStr"), 0.001f);
        Assert.assertEquals(2.718f, ep.getFloat("floatObj"), 0.001f);
        Assert.assertEquals(1.0f, ep.getFloat("unknown", 1.0f), 0.001f);
        Assert.assertEquals(Float.valueOf(2.0f), ep.getFloat("unknown", Float.valueOf(2.0f)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetFloatNotFound() {
        ep.getFloat("unknown");
    }

    @Test(expected = ClassCastException.class)
    public void testGetFloatClassCast() {
        ep.put("list", new ArrayList());
        ep.getFloat("list");
    }

    @Test
    public void testDoubleConversions() {
        ep.addProperty("doubleStr", "3.1415926535");
        ep.addProperty("doubleObj", Double.valueOf(2.7182818284));

        Assert.assertEquals(3.1415926535, ep.getDouble("doubleStr"), 0.000000001);
        Assert.assertEquals(2.7182818284, ep.getDouble("doubleObj"), 0.000000001);
        Assert.assertEquals(1.23, ep.getDouble("unknown", 1.23), 0.001);
        Assert.assertEquals(Double.valueOf(4.56), ep.getDouble("unknown", Double.valueOf(4.56)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetDoubleNotFound() {
        ep.getDouble("unknown");
    }

    @Test(expected = ClassCastException.class)
    public void testGetDoubleClassCast() {
        ep.put("list", new ArrayList());
        ep.getDouble("list");
    }

    @Test
    public void testConvertProperties() {
        Properties parentProps = new Properties();
        parentProps.setProperty("parent.key", "parentVal");

        Properties childProps = new Properties(parentProps);
        childProps.setProperty("child.key", "childVal");

        ExtendedProperties converted = ExtendedProperties.convertProperties(childProps);
        Assert.assertEquals("childVal", converted.getString("child.key"));
        Assert.assertEquals("parentVal", converted.getString("parent.key"));
    }

    @Test
    public void testPropertiesReaderAndTokenizerDirectly() throws IOException {
        String input = "# header\n" +
                "key = line1\\\n" +
                "line2\\\n" +
                "line3\n" +
                "empty = \n" +
                "normal = value\n";

        ExtendedProperties.PropertiesReader reader = new ExtendedProperties.PropertiesReader(new StringReader(input));
        String prop1 = reader.readProperty();
        Assert.assertEquals("key = line1line2line3", prop1);

        String prop2 = reader.readProperty();
        Assert.assertEquals("empty =", prop2);

        String prop3 = reader.readProperty();
        Assert.assertEquals("normal = value", prop3);

        String prop4 = reader.readProperty();
        Assert.assertNull(prop4);

        ExtendedProperties.PropertiesTokenizer tok = new ExtendedProperties.PropertiesTokenizer("a\\,b,c\\,d\\\\,e");
        Assert.assertTrue(tok.hasMoreTokens());
        Assert.assertEquals("a,b", tok.nextToken());
        Assert.assertEquals("c,d\\\\", tok.nextToken());
        Assert.assertEquals("e", tok.nextToken());
        Assert.assertFalse(tok.hasMoreTokens());
    }

    @Test
    public void testDefaultsFallbackForPrimitives() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("b", "true");
        defaults.addProperty("by", "10");
        defaults.addProperty("s", "20");
        defaults.addProperty("i", "30");
        defaults.addProperty("l", "40");
        defaults.addProperty("f", "50.5");
        defaults.addProperty("d", "60.6");
        defaults.addProperty("v", "one,two");
        defaults.addProperty("list", "three,four");
        defaults.addProperty("arr", "five,six");

        ExtendedProperties epWithDefaults = new ExtendedProperties();
        epWithDefaults.defaults = defaults;

        Assert.assertTrue(epWithDefaults.getBoolean("b"));
        Assert.assertEquals((byte) 10, epWithDefaults.getByte("by"));
        Assert.assertEquals((short) 20, epWithDefaults.getShort("s"));
        Assert.assertEquals(30, epWithDefaults.getInt("i"));
        Assert.assertEquals(40L, epWithDefaults.getLong("l"));
        Assert.assertEquals(50.5f, epWithDefaults.getFloat("f"), 0.01f);
        Assert.assertEquals(60.6, epWithDefaults.getDouble("d"), 0.01);

        Vector vec = epWithDefaults.getVector("v");
        Assert.assertEquals(2, vec.size());

        List list = epWithDefaults.getList("list");
        Assert.assertEquals(2, list.size());

        String[] arr = epWithDefaults.getStringArray("arr");
        Assert.assertEquals(2, arr.length);
    }
}
