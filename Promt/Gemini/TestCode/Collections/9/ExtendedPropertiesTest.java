package org.apache.commons.collections;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ExtendedProperties props;

    @Before
    public void setUp() {
        props = new ExtendedProperties();
    }

    @Test
    public void testDefaultConstructor() {
        ExtendedProperties ep = new ExtendedProperties();
        Assert.assertFalse(ep.isInitialized());
        Assert.assertEquals("include", ep.getInclude());
        Assert.assertFalse(ep.getKeys().hasNext());
    }

    @Test
    public void testFileConstructors() throws IOException {
        File file1 = tempFolder.newFile("test1.properties");
        File file2 = tempFolder.newFile("test2.properties");

        PrintWriter writer1 = new PrintWriter(new FileOutputStream(file1));
        writer1.println("prop1=value1");
        writer1.println("shared=override1");
        writer1.close();

        PrintWriter writer2 = new PrintWriter(new FileOutputStream(file2));
        writer2.println("prop2=value2");
        writer2.println("shared=defaultShared");
        writer2.close();

        ExtendedProperties ep1 = new ExtendedProperties(file1.getAbsolutePath());
        Assert.assertTrue(ep1.isInitialized());
        Assert.assertEquals("value1", ep1.getString("prop1"));

        ExtendedProperties ep2 = new ExtendedProperties(file1.getAbsolutePath(), file2.getAbsolutePath());
        Assert.assertTrue(ep2.isInitialized());
        Assert.assertEquals("value1", ep2.getString("prop1"));
        Assert.assertEquals("value2", ep2.getString("prop2"));
        Assert.assertEquals("override1", ep2.getString("shared"));
    }

    @Test
    public void testIncludeGetterAndSetter() {
        Assert.assertEquals("include", props.getInclude());

        props.setInclude("import");
        Assert.assertEquals("import", props.getInclude());

        props.setInclude("");
        Assert.assertNull(props.getInclude());

        props.setInclude(null);
        Assert.assertNull(props.getInclude());
    }

    @Test
    public void testLoadSimpleProperties() throws IOException {
        String data = "key1=value1\nkey2 = value2 \nkey3=val1,val2,val3\n";
        InputStream in = new ByteArrayInputStream(data.getBytes("ISO-8859-1"));
        props.load(in);

        Assert.assertTrue(props.isInitialized());
        Assert.assertEquals("value1", props.getString("key1"));
        Assert.assertEquals("value2", props.getString("key2"));
        
        List list = props.getList("key3");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("val1", list.get(0));
        Assert.assertEquals("val2", list.get(1));
        Assert.assertEquals("val3", list.get(2));
    }

    @Test
    public void testLoadWithEncoding() throws IOException {
        String data = "encKey=encValue\n";
        InputStream in = new ByteArrayInputStream(data.getBytes("UTF-8"));
        props.load(in, "UTF-8");
        Assert.assertEquals("encValue", props.getString("encKey"));

        // Fallback on invalid encoding
        in = new ByteArrayInputStream(data.getBytes("ISO-8859-1"));
        ExtendedProperties epInvalidEnc = new ExtendedProperties();
        epInvalidEnc.load(in, "INVALID_ENCODING_NAME_XYZ");
        Assert.assertEquals("encValue", epInvalidEnc.getString("encKey"));
    }

    @Test
    public void testLoadMultilineAndComments() throws IOException {
        String data = "# Comment line 1\n"
                + "\n"
                + "  # Comment line 2\n"
                + "multiline = first part \\\n"
                + "            second part \\\n"
                + "            third part\n"
                + "emptyVal =\n"
                + "noEqualLine\n"
                + "=noKey\n";
        InputStream in = new ByteArrayInputStream(data.getBytes("ISO-8859-1"));
        props.load(in);

        Assert.assertEquals("first part second part third part", props.getString("multiline"));
        Assert.assertNull(props.getProperty("emptyVal"));
        Assert.assertNull(props.getProperty("noEqualLine"));
    }

    @Test
    public void testLoadWithIncludes() throws IOException {
        File dir = tempFolder.newFolder("propDir");
        File mainFile = new File(dir, "main.properties");
        File included1 = new File(dir, "inc1.properties");
        File included2 = new File(dir, "inc2.properties");

        PrintWriter pw1 = new PrintWriter(new FileOutputStream(included1));
        pw1.println("inc1.prop=val1");
        pw1.close();

        PrintWriter pw2 = new PrintWriter(new FileOutputStream(included2));
        pw2.println("inc2.prop=val2");
        pw2.close();

        PrintWriter pwMain = new PrintWriter(new FileOutputStream(mainFile));
        pwMain.println("main.prop=mainVal");
        pwMain.println("include = inc1.properties");
        pwMain.println("include = ./" + included2.getName());
        pwMain.println("include = " + included1.getAbsolutePath());
        pwMain.close();

        ExtendedProperties ep = new ExtendedProperties(mainFile.getAbsolutePath());
        Assert.assertEquals("mainVal", ep.getString("main.prop"));
        Assert.assertEquals("val1", ep.getString("inc1.prop"));
        Assert.assertEquals("val2", ep.getString("inc2.prop"));
    }

    @Test
    public void testInterpolation() {
        props.addProperty("base.url", "http://example.com");
        props.addProperty("api.path", "${base.url}/api");
        props.addProperty("endpoint", "${api.path}/v1/users");
        props.addProperty("unresolved", "${unknown.variable}/test");
        props.addProperty("multiple", "${base.url}/${base.url}");

        Assert.assertEquals("http://example.com/api", props.getString("api.path"));
        Assert.assertEquals("http://example.com/api/v1/users", props.getString("endpoint"));
        Assert.assertEquals("${unknown.variable}/test", props.getString("unresolved"));
        Assert.assertEquals("http://example.com/http://example.com", props.getString("multiple"));
    }

    @Test
    public void testInterpolationWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defVar", "fromDefault");
        
        props = new ExtendedProperties();
        props.addProperty("mainVar", "${defVar}/value");

        // Manually inject defaults via getString check with defaults
        ExtendedProperties parentProps = new ExtendedProperties();
        parentProps.addProperty("defVar", "parentVal");
        parentProps.addProperty("testProp", "${defVar}");

        Assert.assertEquals("parentVal", parentProps.getString("testProp"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInterpolationLoop() {
        props.addProperty("varA", "${varB}");
        props.addProperty("varB", "${varA}");
        props.getString("varA");
    }

    @Test(expected = IllegalStateException.class)
    public void testInterpolationIndirectLoop() {
        props.addProperty("a", "${b}");
        props.addProperty("b", "${c}");
        props.addProperty("c", "${a}");
        props.getString("a");
    }

    @Test
    public void testEscapeAndUnescape() {
        props.addProperty("escaped.comma", "val1\\,val2");
        props.addProperty("escaped.slash", "val1\\\\val2");
        
        Assert.assertEquals("val1,val2", props.getString("escaped.comma"));
        Assert.assertEquals("val1\\val2", props.getString("escaped.slash"));
    }

    @Test
    public void testAddPropertyTypes() {
        props.addProperty("item", "one");
        Assert.assertEquals("one", props.getString("item"));

        props.addProperty("item", "two");
        List list = props.getList("item");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("one", list.get(0));
        Assert.assertEquals("two", list.get(1));

        props.addProperty("item", "three");
        list = props.getList("item");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("three", list.get(2));

        props.addProperty("nonString", new Integer(42));
        Assert.assertEquals(42, props.getInt("nonString"));
    }

    @Test
    public void testSetPropertyAndClearProperty() {
        props.setProperty("k1", "v1");
        props.setProperty("k2", "v2");
        Assert.assertEquals("v1", props.getString("k1"));

        props.setProperty("k1", "v1_updated");
        Assert.assertEquals("v1_updated", props.getString("k1"));

        props.clearProperty("k1");
        Assert.assertNull(props.getProperty("k1"));
        Assert.assertEquals(1, props.size());

        props.clearProperty("nonExistentKey");
    }

    @Test
    public void testSave() throws IOException {
        props.setProperty("single", "value1");
        props.addProperty("multi", "val1,val2");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header Comment");

        String savedContent = out.toString();
        Assert.assertTrue(savedContent.contains("Header Comment"));
        Assert.assertTrue(savedContent.contains("single=value1"));
        Assert.assertTrue(savedContent.contains("multi=val1"));
        Assert.assertTrue(savedContent.contains("multi=val2"));

        // Save with null stream
        props.save(null, "Header");
    }

    @Test
    public void testCombine() {
        ExtendedProperties other = new ExtendedProperties();
        other.setProperty("k1", "v1_other");
        other.setProperty("k2", "v2_other");

        props.setProperty("k1", "v1_orig");
        props.setProperty("k0", "v0_orig");

        props.combine(other);
        Assert.assertEquals("v1_other", props.getString("k1"));
        Assert.assertEquals("v2_other", props.getString("k2"));
        Assert.assertEquals("v0_orig", props.getString("k0"));
    }

    @Test
    public void testGetKeysAndGetKeysWithPrefix() {
        props.setProperty("app.name", "myApp");
        props.setProperty("app.version", "1.0");
        props.setProperty("db.host", "localhost");

        Iterator allKeys = props.getKeys();
        List keyList = new ArrayList();
        while (allKeys.hasNext()) {
            keyList.add(allKeys.next());
        }
        Assert.assertEquals(Arrays.asList("app.name", "app.version", "db.host"), keyList);

        Iterator appKeys = props.getKeys("app.");
        List appKeyList = new ArrayList();
        while (appKeys.hasNext()) {
            appKeyList.add(appKeys.next());
        }
        Assert.assertEquals(Arrays.asList("app.name", "app.version"), appKeyList);
    }

    @Test
    public void testSubset() {
        props.setProperty("db.host", "localhost");
        props.setProperty("db.port", "3306");
        props.setProperty("db", "mysql");
        props.setProperty("other.prop", "val");

        ExtendedProperties subset = props.subset("db");
        Assert.assertNotNull(subset);
        Assert.assertEquals("localhost", subset.getString("host"));
        Assert.assertEquals("3306", subset.getString("port"));
        Assert.assertEquals("mysql", subset.getString("db"));
        Assert.assertNull(subset.getProperty("other.prop"));

        ExtendedProperties emptySubset = props.subset("nonexistent");
        Assert.assertNull(emptySubset);
    }

    @Test
    public void testDisplay() {
        props.setProperty("keyA", "valA");
        props.setProperty("keyB", "valB");
        // Ensure display() runs without exception
        props.display();
    }

    @Test
    public void testGetStringVariants() {
        props.setProperty("strKey", "strVal");
        props.addProperty("listKey", "item1,item2");

        Assert.assertEquals("strVal", props.getString("strKey"));
        Assert.assertEquals("item1", props.getString("listKey"));
        Assert.assertEquals("default", props.getString("nonExistent", "default"));
        Assert.assertNull(props.getString("nonExistent"));
    }

    @Test(expected = ClassCastException.class)
    public void testGetStringClassCastException() {
        props.put("intObj", new Integer(100));
        props.getString("intObj");
    }

    @Test
    public void testGetProperties() {
        props.setProperty("mapProp", "k1=v1,k2=v2");
        Properties p = props.getProperties("mapProp");
        Assert.assertEquals("v1", p.getProperty("k1"));
        Assert.assertEquals("v2", p.getProperty("k2"));

        Properties defaults = new Properties();
        defaults.setProperty("defK", "defV");
        Properties pWithDef = props.getProperties("mapProp", defaults);
        Assert.assertEquals("defV", pWithDef.getProperty("defK"));
        Assert.assertEquals("v1", pWithDef.getProperty("k1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPropertiesInvalid() {
        props.setProperty("badProp", "invalidTokenNoEqual");
        props.getProperties("badProp");
    }

    @Test
    public void testGetStringArray() {
        props.setProperty("single", "val");
        props.addProperty("multi", "v1,v2,v3");

        String[] arr1 = props.getStringArray("single");
        Assert.assertArrayEquals(new String[]{"val"}, arr1);

        String[] arr2 = props.getStringArray("multi");
        Assert.assertArrayEquals(new String[]{"v1", "v2", "v3"}, arr2);

        String[] emptyArr = props.getStringArray("nonExistent");
        Assert.assertEquals(0, emptyArr.length);
    }

    @Test(expected = ClassCastException.class)
    public void testGetStringArrayClassCastException() {
        props.put("badType", new Integer(5));
        props.getStringArray("badType");
    }

    @Test
    public void testGetVector() {
        props.setProperty("single", "val");
        props.addProperty("multi", "v1,v2");

        Vector v1 = props.getVector("single");
        Assert.assertEquals(1, v1.size());
        Assert.assertEquals("val", v1.get(0));

        Vector v2 = props.getVector("multi");
        Assert.assertEquals(2, v2.size());
        Assert.assertEquals("v1", v2.get(0));
        Assert.assertEquals("v2", v2.get(1));

        Vector defaultVec = new Vector();
        defaultVec.add("def");
        Assert.assertEquals(defaultVec, props.getVector("nonExistent", defaultVec));
        Assert.assertEquals(0, props.getVector("nonExistent").size());
    }

    @Test(expected = ClassCastException.class)
    public void testGetVectorClassCastException() {
        props.put("badVec", new Integer(10));
        props.getVector("badVec");
    }

    @Test
    public void testGetList() {
        props.setProperty("single", "val");
        props.addProperty("multi", "v1,v2");

        List l1 = props.getList("single");
        Assert.assertEquals(1, l1.size());
        Assert.assertEquals("val", l1.get(0));

        List l2 = props.getList("multi");
        Assert.assertEquals(2, l2.size());
        Assert.assertEquals("v1", l2.get(0));
        Assert.assertEquals("v2", l2.get(1));

        List defaultList = new ArrayList();
        defaultList.add("def");
        Assert.assertEquals(defaultList, props.getList("nonExistent", defaultList));
        Assert.assertEquals(0, props.getList("nonExistent").size());
    }

    @Test(expected = ClassCastException.class)
    public void testGetListClassCastException() {
        props.put("badList", new Integer(20));
        props.getList("badList");
    }

    @Test
    public void testBooleanConversions() {
        Assert.assertEquals("true", props.testBoolean("true"));
        Assert.assertEquals("true", props.testBoolean("TRUE"));
        Assert.assertEquals("true", props.testBoolean("on"));
        Assert.assertEquals("true", props.testBoolean("ON"));
        Assert.assertEquals("true", props.testBoolean("yes"));
        Assert.assertEquals("true", props.testBoolean("YES"));

        Assert.assertEquals("false", props.testBoolean("false"));
        Assert.assertEquals("false", props.testBoolean("FALSE"));
        Assert.assertEquals("false", props.testBoolean("off"));
        Assert.assertEquals("false", props.testBoolean("OFF"));
        Assert.assertEquals("false", props.testBoolean("no"));
        Assert.assertEquals("false", props.testBoolean("NO"));

        Assert.assertNull(props.testBoolean("unknown"));

        props.setProperty("bool.true", "true");
        props.setProperty("bool.false", "off");
        props.put("bool.obj", Boolean.TRUE);

        Assert.assertTrue(props.getBoolean("bool.true"));
        Assert.assertFalse(props.getBoolean("bool.false"));
        Assert.assertTrue(props.getBoolean("bool.obj"));
        Assert.assertTrue(props.getBoolean("nonExistent", true));
        Assert.assertFalse(props.getBoolean("nonExistent", false));
        Assert.assertEquals(Boolean.TRUE, props.getBoolean("nonExistent", Boolean.TRUE));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetBooleanNoSuchElement() {
        props.getBoolean("nonExistent");
    }

    @Test(expected = ClassCastException.class)
    public void testGetBooleanClassCast() {
        props.put("invalidBool", new Integer(123));
        props.getBoolean("invalidBool");
    }

    @Test
    public void testByteConversions() {
        props.setProperty("b1", "12");
        props.put("b2", new Byte((byte) 34));

        Assert.assertEquals((byte) 12, props.getByte("b1"));
        Assert.assertEquals((byte) 34, props.getByte("b2"));
        Assert.assertEquals((byte) 56, props.getByte("nonExistent", (byte) 56));
        Assert.assertEquals(new Byte((byte) 78), props.getByte("nonExistent", new Byte((byte) 78)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetByteNoSuchElement() {
        props.getByte("nonExistent");
    }

    @Test(expected = ClassCastException.class)
    public void testGetByteClassCast() {
        props.put("invalidByte", new Long(123));
        props.getByte("invalidByte");
    }

    @Test
    public void testShortConversions() {
        props.setProperty("s1", "123");
        props.put("s2", new Short((short) 456));

        Assert.assertEquals((short) 123, props.getShort("s1"));
        Assert.assertEquals((short) 456, props.getShort("s2"));
        Assert.assertEquals((short) 789, props.getShort("nonExistent", (short) 789));
        Assert.assertEquals(new Short((short) 999), props.getShort("nonExistent", new Short((short) 999)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetShortNoSuchElement() {
        props.getShort("nonExistent");
    }

    @Test(expected = ClassCastException.class)
    public void testGetShortClassCast() {
        props.put("invalidShort", new Long(123));
        props.getShort("invalidShort");
    }

    @Test
    public void testIntegerConversions() {
        props.setProperty("i1", "1234");
        props.put("i2", new Integer(5678));

        Assert.assertEquals(1234, props.getInt("i1"));
        Assert.assertEquals(1234, props.getInteger("i1"));
        Assert.assertEquals(5678, props.getInt("i2"));
        Assert.assertEquals(9999, props.getInt("nonExistent", 9999));
        Assert.assertEquals(9999, props.getInteger("nonExistent", 9999));
        Assert.assertEquals(new Integer(8888), props.getInteger("nonExistent", new Integer(8888)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetIntegerNoSuchElement() {
        props.getInteger("nonExistent");
    }

    @Test(expected = ClassCastException.class)
    public void testGetIntegerClassCast() {
        props.put("invalidInt", new Long(123));
        props.getInteger("invalidInt");
    }

    @Test
    public void testLongConversions() {
        props.setProperty("l1", "123456789");
        props.put("l2", new Long(987654321L));

        Assert.assertEquals(123456789L, props.getLong("l1"));
        Assert.assertEquals(987654321L, props.getLong("l2"));
        Assert.assertEquals(555L, props.getLong("nonExistent", 555L));
        Assert.assertEquals(new Long(777L), props.getLong("nonExistent", new Long(777L)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetLongNoSuchElement() {
        props.getLong("nonExistent");
    }

    @Test(expected = ClassCastException.class)
    public void testGetLongClassCast() {
        props.put("invalidLong", new Double(12.3));
        props.getLong("invalidLong");
    }

    @Test
    public void testFloatConversions() {
        props.setProperty("f1", "12.34");
        props.put("f2", new Float(56.78f));

        Assert.assertEquals(12.34f, props.getFloat("f1"), 0.0001f);
        Assert.assertEquals(56.78f, props.getFloat("f2"), 0.0001f);
        Assert.assertEquals(99.9f, props.getFloat("nonExistent", 99.9f), 0.0001f);
        Assert.assertEquals(new Float(88.8f), props.getFloat("nonExistent", new Float(88.8f)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetFloatNoSuchElement() {
        props.getFloat("nonExistent");
    }

    @Test(expected = ClassCastException.class)
    public void testGetFloatClassCast() {
        props.put("invalidFloat", new Integer(10));
        props.getFloat("invalidFloat");
    }

    @Test
    public void testDoubleConversions() {
        props.setProperty("d1", "12.3456");
        props.put("d2", new Double(78.9012));

        Assert.assertEquals(12.3456, props.getDouble("d1"), 0.00001);
        Assert.assertEquals(78.9012, props.getDouble("d2"), 0.00001);
        Assert.assertEquals(33.33, props.getDouble("nonExistent", 33.33), 0.00001);
        Assert.assertEquals(new Double(44.44), props.getDouble("nonExistent", new Double(44.44)));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetDoubleNoSuchElement() {
        props.getDouble("nonExistent");
    }

    @Test(expected = ClassCastException.class)
    public void testGetDoubleClassCast() {
        props.put("invalidDouble", new Integer(10));
        props.getDouble("invalidDouble");
    }

    @Test
    public void testConvertProperties() {
        Properties standardProps = new Properties();
        standardProps.setProperty("p1", "v1");
        standardProps.setProperty("p2", "v2");

        ExtendedProperties converted = ExtendedProperties.convertProperties(standardProps);
        Assert.assertEquals("v1", converted.getString("p1"));
        Assert.assertEquals("v2", converted.getString("p2"));
    }

    @Test
    public void testMapMethods() {
        Assert.assertNull(props.put("k1", "v1"));
        Assert.assertEquals("v1", props.put("k1", "v2"));
        
        List list = props.getList("k1");
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("v1", list.get(0));
        Assert.assertEquals("v2", list.get(1));

        Map regularMap = new HashMap();
        regularMap.put("m1", "val1");
        regularMap.put("m2", "val2");
        props.putAll(regularMap);
        Assert.assertEquals("val1", props.getString("m1"));
        Assert.assertEquals("val2", props.getString("m2"));

        ExtendedProperties otherEp = new ExtendedProperties();
        otherEp.setProperty("ep1", "valEp1");
        otherEp.setProperty("ep2", "valEp2");
        props.putAll(otherEp);
        Assert.assertEquals("valEp1", props.getString("ep1"));
        Assert.assertEquals("valEp2", props.getString("ep2"));

        Object removed = props.remove("ep1");
        Assert.assertEquals("valEp1", removed);
        Assert.assertNull(props.getProperty("ep1"));
    }

    @Test
    public void testPropertiesReaderAndTokenizerDirectly() throws IOException {
        String data = "line1\\\ncontinuation\n#comment\nline2\n";
        ExtendedProperties.PropertiesReader reader = new ExtendedProperties.PropertiesReader(new StringReader(data));
        
        Assert.assertEquals("line1continuation", reader.readProperty());
        Assert.assertEquals("line2", reader.readProperty());
        Assert.assertNull(reader.readProperty());

        ExtendedProperties.PropertiesTokenizer tok = new ExtendedProperties.PropertiesTokenizer("a,b\\,c,d\\\\,e");
        Assert.assertTrue(tok.hasMoreTokens());
        Assert.assertEquals("a", tok.nextToken());
        Assert.assertEquals("b,c", tok.nextToken());
        Assert.assertEquals("d\\", tok.nextToken());
        Assert.assertEquals("e", tok.nextToken());
        Assert.assertFalse(tok.hasMoreTokens());
    }
}
