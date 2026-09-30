package org.apache.commons.collections;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Vector;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ExtendedPropertiesTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    // Helper to create a temp file with given content
    private File createTempFile(String content) throws IOException {
        File file = tempFolder.newFile();
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print(content);
        }
        return file;
    }

    @Test
    public void testDefaultConstructor() {
        ExtendedProperties props = new ExtendedProperties();
        assertFalse(props.isInitialized());
        assertNull(props.getInclude());
        assertNotNull(props.getKeys());
        assertEquals(0, props.size());
    }

    @Test
    public void testFileConstructor() throws Exception {
        String content = "key=value\n";
        File file = createTempFile(content);
        ExtendedProperties props = new ExtendedProperties(file.getAbsolutePath());
        assertTrue(props.isInitialized());
        assertEquals("value", props.getProperty("key"));
    }

    @Test
    public void testFileConstructorWithDefaults() throws Exception {
        String defaultContent = "defaultKey=defaultValue\n";
        File defaultFile = createTempFile(defaultContent);
        String content = "key=value\n";
        File file = createTempFile(content);
        ExtendedProperties props = new ExtendedProperties(file.getAbsolutePath(), defaultFile.getAbsolutePath());
        // defaults are loaded after main file
        assertEquals("value", props.getProperty("key"));
        assertEquals("defaultValue", props.getProperty("defaultKey"));
    }

    @Test
    public void testLoadWithInputStream() throws Exception {
        String content = "a=1\nb=2\n";
        InputStream in = new ByteArrayInputStream(content.getBytes());
        ExtendedProperties props = new ExtendedProperties();
        props.load(in);
        assertEquals("1", props.getProperty("a"));
        assertEquals("2", props.getProperty("b"));
    }

    @Test
    public void testLoadWithEncoding() throws Exception {
        String content = "key=value\n";
        InputStream in = new ByteArrayInputStream(content.getBytes("UTF-8"));
        ExtendedProperties props = new ExtendedProperties();
        props.load(in, "UTF-8");
        assertEquals("value", props.getProperty("key"));
    }

    @Test
    public void testLoadWithUnsupportedEncodingFallsBack() throws Exception {
        String content = "key=value\n";
        InputStream in = new ByteArrayInputStream(content.getBytes());
        ExtendedProperties props = new ExtendedProperties();
        props.load(in, "INVALID_ENCODING");
        assertEquals("value", props.getProperty("key"));
    }

    @Test
    public void testLoadCommentsAndBlankLines() throws Exception {
        String content = "# comment\n\nkey = value\n";
        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(content.getBytes()));
        assertEquals("value", props.getProperty("key"));
    }

    @Test
    public void testLoadContinuationLines() throws Exception {
        String content = "longkey = first part \\\n second part\n";
        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(content.getBytes()));
        assertEquals("first part  second part", props.getProperty("longkey"));
    }

    @Test
    public void testLoadEmptyValueIgnored() throws Exception {
        String content = "emptykey=\nkey=value\n";
        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(content.getBytes()));
        assertFalse(props.containsKey("emptykey"));
        assertEquals("value", props.getProperty("key"));
    }

    @Test
    public void testLoadIncludeRelative() throws Exception {
        String includeContent = "included=includedValue\n";
        File includeFile = tempFolder.newFile("included.properties");
        try (PrintWriter writer = new PrintWriter(includeFile)) {
            writer.print(includeContent);
        }
        String mainContent = "include=included.properties\nmain=mainValue\n";
        File mainFile = createTempFile(mainContent);
        ExtendedProperties props = new ExtendedProperties();
        // basePath is set in file constructor; use that constructor to test include
        props = new ExtendedProperties(mainFile.getAbsolutePath());
        assertEquals("includedValue", props.getProperty("included"));
        assertEquals("mainValue", props.getProperty("main"));
    }

    @Test
    public void testLoadIncludeAbsolute() throws Exception {
        String includeContent = "abs=absValue\n";
        File includeFile = createTempFile(includeContent);
        String mainContent = "include=" + includeFile.getAbsolutePath() + "\nkey=value\n";
        File mainFile = createTempFile(mainContent);
        ExtendedProperties props = new ExtendedProperties(mainFile.getAbsolutePath());
        assertEquals("absValue", props.getProperty("abs"));
        assertEquals("value", props.getProperty("key"));
    }

    @Test
    public void testLoadIncludeNotExistsIgnored() throws Exception {
        String mainContent = "include=nonexistent.properties\nkey=value\n";
        File mainFile = createTempFile(mainContent);
        ExtendedProperties props = new ExtendedProperties(mainFile.getAbsolutePath());
        assertEquals("value", props.getProperty("key"));
        assertFalse(props.containsKey("include"));
    }

    @Test
    public void testSetAndGetInclude() {
        ExtendedProperties props = new ExtendedProperties();
        assertNull(props.getInclude()); // default static include? Actually static include might be "include", but instance is null so default is static "include". Test.
        props.setInclude(null);
        assertNull(props.getInclude());
        props.setInclude("");
        assertNull(props.getInclude());
        props.setInclude("import");
        assertEquals("import", props.getInclude());
    }

    @Test
    public void testAddPropertySimple() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        assertEquals("value", props.getProperty("key"));
        assertTrue(props.isInitialized());
    }

    @Test
    public void testAddPropertyWithCommas() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a,b,c");
        Object value = props.getProperty("key");
        assertTrue(value instanceof List);
        List list = (List) value;
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testAddPropertyWithEscapedCommas() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a\\,b,c");
        Object value = props.getProperty("key");
        assertTrue(value instanceof List);
        List list = (List) value;
        assertEquals(2, list.size());
        assertEquals("a,b", list.get(0));
        assertEquals("c", list.get(1));
    }

    @Test
    public void testAddPropertyMultipleTimesSameKey() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "first");
        props.addProperty("key", "second");
        Object value = props.getProperty("key");
        assertTrue(value instanceof List);
        List list = (List) value;
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
    }

    @Test
    public void testAddPropertyNonString() {
        ExtendedProperties props = new ExtendedProperties();
        Object obj = new Integer(42);
        props.addProperty("key", obj);
        assertSame(obj, props.getProperty("key"));
    }

    @Test
    public void testSetProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.setProperty("key", "value1");
        assertEquals("value1", props.getProperty("key"));
        props.setProperty("key", "value2");
        assertEquals("value2", props.getProperty("key"));
    }

    @Test
    public void testClearProperty() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "val1");
        props.addProperty("key2", "val2");
        props.clearProperty("key1");
        assertFalse(props.containsKey("key1"));
        assertTrue(props.containsKey("key2"));
        // also removes from keysAsListed
        Iterator keys = props.getKeys();
        assertTrue(keys.hasNext());
        assertEquals("key2", keys.next());
        assertFalse(keys.hasNext());
    }

    @Test
    public void testClearPropertyNonExistent() {
        ExtendedProperties props = new ExtendedProperties();
        props.clearProperty("nonexistent"); // should not throw
        assertTrue(props.isEmpty());
    }

    @Test
    public void testSaveNullOutputStream() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        props.save(null, null); // should just return
    }

    @Test
    public void testSaveWithHeaderAndValues() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key1", "value1");
        props.addProperty("key2", "a,b");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, "Header");
        String output = out.toString("UTF-8");
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("key1=value1"));
        // expect two lines for key2 because of comma split
        assertTrue(output.contains("key2=a,b"));
    }

    @Test
    public void testSaveWithListValue() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a,b");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, null);
        String output = out.toString("UTF-8");
        assertTrue(output.contains("key=a"));
        assertTrue(output.contains("key=b"));
    }

    @Test
    public void testSaveWithNullValueKey() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        props.put("nullKey", null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, null);
        String output = out.toString("UTF-8");
        assertFalse(output.contains("nullKey"));
    }

    @Test
    public void testCombine() {
        ExtendedProperties props1 = new ExtendedProperties();
        props1.addProperty("a", "1");
        props1.addProperty("b", "2");

        ExtendedProperties props2 = new ExtendedProperties();
        props2.addProperty("b", "3");
        props2.addProperty("c", "4");

        props1.combine(props2);
        assertEquals("1", props1.getProperty("a"));
        assertEquals("3", props1.getProperty("b")); // overwritten
        assertEquals("4", props1.getProperty("c"));
    }

    @Test
    public void testGetKeys() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("first", "1");
        props.addProperty("second", "2");
        Iterator keys = props.getKeys();
        assertEquals("first", keys.next());
        assertEquals("second", keys.next());
        assertFalse(keys.hasNext());
    }

    @Test
    public void testGetKeysWithPrefix() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("prefix.one", "1");
        props.addProperty("prefix.two", "2");
        props.addProperty("other", "3");
        Iterator keys = props.getKeys("prefix.");
        List<String> keyList = new ArrayList<>();
        while (keys.hasNext()) keyList.add((String) keys.next());
        assertEquals(2, keyList.size());
        assertTrue(keyList.contains("prefix.one"));
        assertTrue(keyList.contains("prefix.two"));
        // also test prefix match with exact key
        Iterator keys2 = props.getKeys("other");
        assertTrue(keys2.hasNext());
        assertEquals("other", keys2.next());
        assertFalse(keys2.hasNext());
    }

    @Test
    public void testSubset() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("app.name", "MyApp");
        props.addProperty("app.version", "1.0");
        props.addProperty("other", "value");

        ExtendedProperties subset = props.subset("app");
        assertNotNull(subset);
        assertEquals("MyApp", subset.getProperty("name"));
        assertEquals("1.0", subset.getProperty("version"));
        assertFalse(subset.containsKey("other"));
        // subset with no matching should return null
        assertNull(props.subset("nonexistent"));
    }

    @Test
    public void testGetString() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        assertEquals("value", props.getString("key"));
        // test default
        assertNull(props.getString("nonexistent"));
        assertEquals("default", props.getString("nonexistent", "default"));
        // test list first element
        props.addProperty("listKey", "a,b");
        assertEquals("a", props.getString("listKey"));
    }

    @Test
    public void testGetStringWithInterpolation() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("name", "World");
        props.addProperty("greeting", "Hello ${name}!");
        assertEquals("Hello World!", props.getString("greeting"));
    }

    @Test
    public void testGetStringInterpolationWithDefaults() {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defaultName", "Default");
        ExtendedProperties props = new ExtendedProperties();
        // set defaults via reflection? We can use constructor with default file, but simpler: create props with defaults.
        // Since defaults field is private, we can use a file constructor with default file.
        // Or we can use reflection to set defaults.
        try {
            Field defaultsField = ExtendedProperties.class.getDeclaredField("defaults");
            defaultsField.setAccessible(true);
            defaultsField.set(props, defaults);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
        props.addProperty("greeting", "Hello ${defaultName}");
        assertEquals("Hello Default", props.getString("greeting"));
    }

    @Test
    public void testGetStringInterpolationLoopThrows() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("a", "${b}");
        props.addProperty("b", "${a}");
        try {
            props.getString("a");
            fail("Should have thrown IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testGetStringWithMissingKey() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "Hello ${missing}");
        assertEquals("Hello ${missing}", props.getString("key"));
    }

    @Test
    public void testGetStringClassCastException() {
        ExtendedProperties props = new ExtendedProperties();
        props.put("key", new Integer(5));
        try {
            props.getString("key");
            fail("Should throw ClassCastException");
        } catch (ClassCastException e) {
            // expected
        }
    }

    @Test
    public void testGetProperties() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("propEntry", "name=John");
        Properties result = props.getProperties("propEntry");
        assertEquals("John", result.getProperty("name"));
        // with multiple tokens
        props.addProperty("multiProp", "n1=v1,n2=v2");
        Properties multi = props.getProperties("multiProp");
        assertEquals("v1", multi.getProperty("n1"));
        assertEquals("v2", multi.getProperty("n2"));
    }

    @Test
    public void testGetPropertiesMalformedToken() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("badProp", "novalue");
        try {
            props.getProperties("badProp");
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testGetPropertiesWithDefaults() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("propEntry", "name=John");
        Properties defaultProps = new Properties();
        defaultProps.setProperty("defaultKey", "defaultValue");
        Properties result = props.getProperties("propEntry", defaultProps);
        assertEquals("defaultValue", result.getProperty("defaultKey"));
        assertEquals("John", result.getProperty("name"));
    }

    @Test
    public void testGetStringArray() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("single", "value");
        String[] arr1 = props.getStringArray("single");
        assertEquals(1, arr1.length);
        assertEquals("value", arr1[0]);

        props.addProperty("multi", "a,b,c");
        String[] arr2 = props.getStringArray("multi");
        assertEquals(3, arr2.length);
        assertEquals("a", arr2[0]);
        assertEquals("b", arr2[1]);
        assertEquals("c", arr2[2]);

        // defaults version not directly testable without reflection, but tested via other methods probably
    }

    @Test
    public void testGetStringArrayEmpty() {
        ExtendedProperties props = new ExtendedProperties();
        String[] arr = props.getStringArray("nonexistent");
        assertEquals(0, arr.length);
    }

    @Test
    public void testGetVector() {
        ExtendedProperties props = new ExtendedProperties();
        Vector v = props.getVector("nonexistent");
        assertNotNull(v);
        assertTrue(v.isEmpty());
        // test default
        Vector defaultVec = new Vector();
        defaultVec.add("default");
        assertSame(defaultVec, props.getVector("nonexistent", defaultVec));

        // test string -> vector conversion
        props.addProperty("key", "value");
        Vector vec = props.getVector("key");
        assertEquals(1, vec.size());
        assertEquals("value", vec.get(0));

        // test list -> vector
        props.addProperty("listKey", "a,b");
        Vector listVec = props.getVector("listKey");
        assertEquals(2, listVec.size());
        assertEquals("a", listVec.get(0));
        assertEquals("b", listVec.get(1));
    }

    @Test
    public void testGetList() {
        ExtendedProperties props = new ExtendedProperties();
        List list = props.getList("nonexistent");
        assertNotNull(list);
        assertTrue(list.isEmpty());
        // test default
        List defaultList = new ArrayList();
        defaultList.add("default");
        assertSame(defaultList, props.getList("nonexistent", defaultList));

        // test string -> list conversion
        props.addProperty("key", "value");
        List list2 = props.getList("key");
        assertEquals(1, list2.size());
        assertEquals("value", list2.get(0));

        // test list -> list copy
        props.addProperty("listKey", "a,b");
        List list3 = props.getList("listKey");
        assertEquals(2, list3.size());
        assertEquals("a", list3.get(0));
        assertEquals("b", list3.get(1));
    }

    @Test
    public void testGetBoolean() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("trueKey", "true");
        assertTrue(props.getBoolean("trueKey"));
        props.addProperty("onKey", "on");
        assertTrue(props.getBoolean("onKey"));
        props.addProperty("yesKey", "yes");
        assertTrue(props.getBoolean("yesKey"));
        props.addProperty("falseKey", "false");
        assertFalse(props.getBoolean("falseKey"));
        props.addProperty("offKey", "off");
        assertFalse(props.getBoolean("offKey"));
        props.addProperty("noKey", "no");
        assertFalse(props.getBoolean("noKey"));
        // test non-boolean string
        props.addProperty("bad", "maybe");
        assertNull(props.getBoolean("bad", null));
        // test default
        assertTrue(props.getBoolean("nonexistent", true));
        // test no default throws
        try {
            props.getBoolean("nonexistent");
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testTestBoolean() {
        ExtendedProperties props = new ExtendedProperties();
        assertEquals("true", props.testBoolean("true"));
        assertEquals("true", props.testBoolean("ON"));
        assertEquals("true", props.testBoolean("Yes"));
        assertEquals("false", props.testBoolean("false"));
        assertEquals("false", props.testBoolean("off"));
        assertEquals("false", props.testBoolean("NO"));
        assertNull(props.testBoolean("maybe"));
    }

    @Test
    public void testGetByte() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("byteKey", "10");
        assertEquals(10, props.getByte("byteKey"));
        // default
        assertEquals((byte)20, props.getByte("nonexistent", (byte)20));
        // no default throws
        try {
            props.getByte("nonexistent");
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        // NumberFormatException
        props.addProperty("badByte", "notanumber");
        try {
            props.getByte("badByte");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testGetShort() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("shortKey", "100");
        assertEquals(100, props.getShort("shortKey"));
        assertEquals((short)200, props.getShort("nonexistent", (short)200));
        try {
            props.getShort("nonexistent");
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        props.addProperty("badShort", "abc");
        try {
            props.getShort("badShort");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testGetInteger() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("intKey", "123");
        assertEquals(123, props.getInt("intKey"));
        assertEquals(456, props.getInteger("nonexistent", 456));
        // overloaded
        assertEquals(123, props.getInteger("intKey").intValue());
        try {
            props.getInteger("nonexistent");
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        props.addProperty("badInt", "1.2");
        try {
            props.getInteger("badInt");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testGetLong() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("longKey", "123456789L");
        assertEquals(123456789L, props.getLong("longKey"));
        assertEquals(987654321L, props.getLong("nonexistent", 987654321L));
        try {
            props.getLong("nonexistent");
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        props.addProperty("badLong", "1e10");
        try {
            props.getLong("badLong");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testGetFloat() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("floatKey", "3.14");
        assertEquals(3.14f, props.getFloat("floatKey"), 0.001f);
        assertEquals(2.5f, props.getFloat("nonexistent", 2.5f), 0.001f);
        try {
            props.getFloat("nonexistent");
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        props.addProperty("badFloat", "a");
        try {
            props.getFloat("badFloat");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testGetDouble() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("doubleKey", "2.71828");
        assertEquals(2.71828, props.getDouble("doubleKey"), 0.00001);
        assertEquals(1.5, props.getDouble("nonexistent", 1.5), 0.00001);
        try {
            props.getDouble("nonexistent");
            fail("Should throw NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
        props.addProperty("badDouble", "x");
        try {
            props.getDouble("badDouble");
            fail("Should throw NumberFormatException");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test
    public void testConvertProperties() {
        Properties p = new Properties();
        p.setProperty("key1", "value1");
        p.setProperty("key2", "value2");
        ExtendedProperties ep = ExtendedProperties.convertProperties(p);
        assertEquals("value1", ep.getProperty("key1"));
        assertEquals("value2", ep.getProperty("key2"));
    }

    @Test
    public void testPutAllWithExtendedProperties() {
        ExtendedProperties source = new ExtendedProperties();
        source.addProperty("a", "1");
        source.addProperty("b", "2");
        ExtendedProperties dest = new ExtendedProperties();
        dest.putAll(source);
        assertEquals("1", dest.getProperty("a"));
        assertEquals("2", dest.getProperty("b"));
        // order should be preserved
        Iterator keys = dest.getKeys();
        assertEquals("a", keys.next());
        assertEquals("b", keys.next());
        assertFalse(keys.hasNext());
    }

    @Test
    public void testPutAllWithRegularMap() {
        Map<String, String> map = new Hashtable<>();
        map.put("x", "10");
        map.put("y", "20");
        ExtendedProperties props = new ExtendedProperties();
        props.putAll(map);
        assertEquals("10", props.getProperty("x"));
        assertEquals("20", props.getProperty("y"));
    }

    // Test inner PropertiesReader via load features
    @Test
    public void testPropertiesReaderContinuation() throws Exception {
        String content = "key = value1 \\\n value2\n";
        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(content.getBytes()));
        assertEquals("value1  value2", props.getProperty("key"));
    }

    @Test
    public void testPropertiesReaderCommentSkipped() throws Exception {
        String content = "# comment\nkey=value\n";
        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(content.getBytes()));
        assertEquals("value", props.getProperty("key"));
    }

    // Test PropertiesTokenizer via addProperty with commas
    @Test
    public void testTokenizerWithEscapedCommasAndBackslashes() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a\\,b,c\\\\d");
        List list = (List) props.getProperty("key");
        assertEquals(2, list.size());
        assertEquals("a,b", list.get(0));
        assertEquals("c\\d", list.get(1)); // backslash escapes backslash
    }

    @Test
    public void testTokenizerWithTrailingComma() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a,b,");
        List list = (List) props.getProperty("key");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("", list.get(2));
    }

    @Test
    public void testDisplay() {
        // Just ensure no exception; capture output maybe
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        props.display();
    }

    // Test interpolation more thoroughly
    @Test
    public void testInterpolationMultipleVariables() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("first", "Hello");
        props.addProperty("second", "World");
        props.addProperty("combined", "${first} ${second}!");
        assertEquals("Hello World!", props.getString("combined"));
    }

    @Test
    public void testInterpolationWithDefaultsChain() throws Exception {
        // Using reflection to set defaults for chain
        ExtendedProperties props = new ExtendedProperties();
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("name", "Default");
        Field f = ExtendedProperties.class.getDeclaredField("defaults");
        f.setAccessible(true);
        f.set(props, defaults);

        props.addProperty("greeting", "Hello ${name}");
        assertEquals("Hello Default", props.getString("greeting"));
    }

    @Test
    public void testInterpolationMissingVarInDefaultsReturnsOriginal() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "Hello ${missing}");
        assertEquals("Hello ${missing}", props.getString("key"));
    }

    @Test
    public void testInterpolationWithLoopThrows() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("a", "${b}");
        props.addProperty("b", "${a}");
        try {
            props.getString("a");
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("infinite loop"));
        }
    }

    @Test
    public void testInterpolationWithDirectReference() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("direct", "Direct");
        props.addProperty("key", "${direct}");
        assertEquals("Direct", props.getString("key"));
    }

    @Test
    public void testGetStringArrayWithDefaults() throws Exception {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("arrKey", "x,y,z");
        ExtendedProperties props = new ExtendedProperties();
        Field f = ExtendedProperties.class.getDeclaredField("defaults");
        f.setAccessible(true);
        f.set(props, defaults);
        String[] arr = props.getStringArray("arrKey");
        assertEquals(3, arr.length);
        assertEquals("x", arr[0]);
        assertEquals("y", arr[1]);
        assertEquals("z", arr[2]);
    }

    @Test
    public void testGetPropertiesWithDefaultsChain() throws Exception {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("prop", "a=1,b=2");
        ExtendedProperties props = new ExtendedProperties();
        Field f = ExtendedProperties.class.getDeclaredField("defaults");
        f.setAccessible(true);
        f.set(props, defaults);
        Properties p = props.getProperties("prop");
        assertEquals("1", p.getProperty("a"));
        assertEquals("2", p.getProperty("b"));
    }

    @Test
    public void testGetVectorWithDefaults() throws Exception {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("vecKey", "a,b");
        ExtendedProperties props = new ExtendedProperties();
        Field f = ExtendedProperties.class.getDeclaredField("defaults");
        f.setAccessible(true);
        f.set(props, defaults);
        Vector v = props.getVector("vecKey");
        assertEquals(2, v.size());
        assertEquals("a", v.get(0));
        assertEquals("b", v.get(1));
    }

    @Test
    public void testGetListWithDefaults() throws Exception {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("listKey", "a,b");
        ExtendedProperties props = new ExtendedProperties();
        Field f = ExtendedProperties.class.getDeclaredField("defaults");
        f.setAccessible(true);
        f.set(props, defaults);
        List l = props.getList("listKey");
        assertEquals(2, l.size());
        assertEquals("a", l.get(0));
        assertEquals("b", l.get(1));
    }

    @Test
    public void testGetBooleanWithDefaultsChain() throws Exception {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("boolKey", "true");
        ExtendedProperties props = new ExtendedProperties();
        Field f = ExtendedProperties.class.getDeclaredField("defaults");
        f.setAccessible(true);
        f.set(props, defaults);
        assertTrue(props.getBoolean("boolKey"));
    }

    @Test
    public void testGetNumericWithDefaultsChain() throws Exception {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("intKey", "42");
        defaults.addProperty("longKey", "123");
        defaults.addProperty("doubleKey", "2.5");
        defaults.addProperty("floatKey", "1.5");
        defaults.addProperty("shortKey", "5");
        defaults.addProperty("byteKey", "10");
        ExtendedProperties props = new ExtendedProperties();
        Field f = ExtendedProperties.class.getDeclaredField("defaults");
        f.setAccessible(true);
        f.set(props, defaults);

        assertEquals(42, props.getInteger("intKey").intValue());
        assertEquals(123L, props.getLong("longKey").longValue());
        assertEquals(2.5, props.getDouble("doubleKey").doubleValue(), 0.001);
        assertEquals(1.5f, props.getFloat("floatKey").floatValue(), 0.001f);
        assertEquals((short)5, props.getShort("shortKey").shortValue());
        assertEquals((byte)10, props.getByte("byteKey").byteValue());
    }

    @Test
    public void testGetPropertyWithDefaults() throws Exception {
        ExtendedProperties defaults = new ExtendedProperties();
        defaults.addProperty("defaultKey", "defaultValue");
        ExtendedProperties props = new ExtendedProperties();
        Field f = ExtendedProperties.class.getDeclaredField("defaults");
        f.setAccessible(true);
        f.set(props, defaults);
        assertEquals("defaultValue", props.getProperty("defaultKey"));
        props.addProperty("defaultKey", "override");
        assertEquals("override", props.getProperty("defaultKey"));
    }

    // Test save with escaped characters
    @Test
    public void testSaveEscapesCommaAndBackslash() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "a,b\\c");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, null);
        String output = out.toString("UTF-8");
        // The escape method adds backslash before comma and backslash; note: actual output may be "a\\,b\\\\c" due to Java string escaping.
        // But the escape method will produce "a\\,b\\\\c" in memory, which serialized becomes "a\\,b\\\\c".
        assertTrue(output.contains("key=a\\,b\\\\c"));
    }

    // Test that getInclude returns static include when instance null
    @Test
    public void testGetIncludeStaticDefault() {
        ExtendedProperties props = new ExtendedProperties();
        // static include is "include" by default, but instance is null, so getInclude returns static.
        // We cannot change static, but we can check it's "include".
        assertEquals("include", props.getInclude());
    }

    // Test save with no header
    @Test
    public void testSaveWithoutHeader() throws Exception {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("key", "value");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        props.save(out, null);
        String output = out.toString("UTF-8");
        assertTrue(output.contains("key=value"));
    }

    // Test load with include property disabled (setInclude null)
    @Test
    public void testLoadWithIncludeDisabled() throws Exception {
        String content = "include=included.properties\nkey=value\n";
        File mainFile = createTempFile(content);
        ExtendedProperties props = new ExtendedProperties();
        props.setInclude(null);
        props.load(new FileInputStream(mainFile));
        // include should not be loaded, but key=value should be
        assertEquals("value", props.getProperty("key"));
        assertFalse(props.containsKey("included"));
    }

    // Test load with invalid include path (should not throw)
    @Test
    public void testLoadWithInvalidIncludePath() throws Exception {
        String content = "include=/\0invalid\0path\nkey=value\n";
        File mainFile = createTempFile(content);
        ExtendedProperties props = new ExtendedProperties(mainFile.getAbsolutePath());
        // Should not throw; include ignored because file not exists
        assertEquals("value", props.getProperty("key"));
        assertFalse(props.containsKey("included"));
    }

    // Test load with encoding fallback when given enc is null
    @Test
    public void testLoadWithNullEncoding() throws Exception {
        String content = "key=value\n";
        InputStream in = new ByteArrayInputStream(content.getBytes());
        ExtendedProperties props = new ExtendedProperties();
        props.load(in, null);
        assertEquals("value", props.getProperty("key"));
    }

    // Test load with default ISO encoding fallback when unsupported encoding and system default
    // Already covered with invalid encoding.

    // Test handling of duplicate keys in load (should become list)
    @Test
    public void testLoadDuplicateKeys() throws Exception {
        String content = "key=first\nkey=second\n";
        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(content.getBytes()));
        Object value = props.getProperty("key");
        assertTrue(value instanceof List);
        List list = (List) value;
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
    }

    // Test load with comma in value
    @Test
    public void testLoadWithCommaValue() throws Exception {
        String content = "key=a,b,c\n";
        ExtendedProperties props = new ExtendedProperties();
        props.load(new ByteArrayInputStream(content.getBytes()));
        Object value = props.getProperty("key");
        assertTrue(value instanceof List);
        List list = (List) value;
        assertEquals(3, list.size());
    }

    // Test getKeys after clearProperty ensures order maintained
    @Test
    public void testKeysOrderAfterClear() {
        ExtendedProperties props = new ExtendedProperties();
        props.addProperty("a", "1");
        props.addProperty("b", "2");
        props.addProperty("c", "3");
        props.clearProperty("b");
        Iterator keys = props.getKeys();
        assertEquals("a", keys.next());
        assertEquals("c", keys.next());
        assertFalse(keys.hasNext());
    }
}
