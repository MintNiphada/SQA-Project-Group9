package org.apache.commons.collections;

import static org.junit.Assert.*;
import org.junit.*;
import java.io.*;
import java.util.*;

public class ExtendedPropertiesTest {

    private ExtendedProperties props;
    private ExtendedProperties defaults;

    @Before
    public void setUp() {
        props = new ExtendedProperties();
        defaults = new ExtendedProperties();
        defaults.addProperty("default.key", "default.value");
        defaults.addProperty("interpolated.key", "interpolated.${default.key}");
        props.defaults = defaults;
    }

    @After
    public void tearDown() {
        // Reset static include to default
        ExtendedProperties.include = "include";
    }

    // Constructor tests
    @Test
    public void testDefaultConstructor() {
        ExtendedProperties ep = new ExtendedProperties();
        assertFalse(ep.isInitialized());
        assertTrue(ep.getKeys().hasNext() == false);
    }

    @Test
    public void testConstructorWithFile() throws IOException {
        File tempFile = File.createTempFile("test", ".properties");
        tempFile.deleteOnExit();
        PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
        pw.println("key1 = value1");
        pw.println("key2 = value2");
        pw.close();

        ExtendedProperties ep = new ExtendedProperties(tempFile.getAbsolutePath());
        assertTrue(ep.isInitialized());
        assertEquals("value1", ep.getString("key1"));
        assertEquals("value2", ep.getString("key2"));
    }

    @Test
    public void testConstructorWithFileAndDefault() throws IOException {
        File mainFile = File.createTempFile("main", ".properties");
        mainFile.deleteOnExit();
        PrintWriter pw = new PrintWriter(new FileWriter(mainFile));
        pw.println("key = mainValue");
        pw.close();

        File defaultFile = File.createTempFile("default", ".properties");
        defaultFile.deleteOnExit();
        pw = new PrintWriter(new FileWriter(defaultFile));
        pw.println("key = defaultValue");
        pw.println("defaultOnly = def");
        pw.close();

        ExtendedProperties ep = new ExtendedProperties(mainFile.getAbsolutePath(), defaultFile.getAbsolutePath());
        assertEquals("mainValue", ep.getString("key"));
        assertEquals("def", ep.getString("defaultOnly"));
    }

    // Load tests
    @Test
    public void testLoadInputStream() throws IOException {
        String input = "key1 = value1\nkey2 = value2\n";
        props.load(new ByteArrayInputStream(input.getBytes()));
        assertEquals("value1", props.getString("key1"));
        assertEquals("value2", props.getString("key2"));
    }

    @Test
    public void testLoadWithEncoding() throws IOException {
        String input = "key = value";
        props.load(new ByteArrayInputStream(input.getBytes("UTF-8")), "UTF-8");
        assertEquals("value", props.getString("key"));
    }

    @Test
    public void testLoadWithCommentsAndBlankLines() throws IOException {
        String input = "# comment\n\nkey = value\n  # another comment\n";
        props.load(new ByteArrayInputStream(input.getBytes()));
        assertEquals("value", props.getString("key"));
        assertEquals(1, sizeOfKeys(props));
    }

    @Test
    public void testLoadWithMultiline() throws IOException {
        String input = "longkey = line1\\\nline2\\\nline3\n";
        props.load(new ByteArrayInputStream(input.getBytes()));
        assertEquals("line1line2line3", props.getString("longkey"));
    }

    @Test
    public void testLoadWithEmptyValue() throws IOException {
        String input = "key = \nother = value\n";
        props.load(new ByteArrayInputStream(input.getBytes()));
        assertNull(props.getProperty("key"));
        assertEquals("value", props.getString("other"));
    }

    @Test
    public void testLoadWithNoEquals() throws IOException {
        String input = "keyvalue\nkey = value\n";
        props.load(new ByteArrayInputStream(input.getBytes()));
        assertNull(props.getProperty("keyvalue"));
        assertEquals("value", props.getString("key"));
    }

    @Test
    public void testLoadWithInclude() throws IOException {
        // Create a temp directory and files
        File dir = new File(System.getProperty("java.io.tmpdir"), "testInclude");
        dir.mkdirs();
        dir.deleteOnExit();
        File mainFile = new File(dir, "main.properties");
        mainFile.deleteOnExit();
        PrintWriter pw = new PrintWriter(new FileWriter(mainFile));
        pw.println("include = included.properties");
        pw.println("mainKey = mainValue");
        pw.close();

        File includedFile = new File(dir, "included.properties");
        includedFile.deleteOnExit();
        pw = new PrintWriter(new FileWriter(includedFile));
        pw.println("includedKey = includedValue");
        pw.close();

        ExtendedProperties ep = new ExtendedProperties();
        ep.setInclude("include");
        ep.basePath = dir.getAbsolutePath() + File.separator;
        ep.load(new FileInputStream(mainFile));
        assertEquals("mainValue", ep.getString("mainKey"));
        assertEquals("includedValue", ep.getString("includedKey"));
    }

    @Test
    public void testLoadWithIncludeAbsolutePath() throws IOException {
        File dir = new File(System.getProperty("java.io.tmpdir"), "testIncludeAbs");
        dir.mkdirs();
        dir.deleteOnExit();
        File mainFile = new File(dir, "main.properties");
        mainFile.deleteOnExit();
        PrintWriter pw = new PrintWriter(new FileWriter(mainFile));
        pw.println("include = " + dir.getAbsolutePath() + File.separator + "included.properties");
        pw.close();

        File includedFile = new File(dir, "included.properties");
        includedFile.deleteOnExit();
        pw = new PrintWriter(new FileWriter(includedFile));
        pw.println("absKey = absValue");
        pw.close();

        ExtendedProperties ep = new ExtendedProperties();
        ep.setInclude("include");
        ep.basePath = dir.getAbsolutePath() + File.separator;
        ep.load(new FileInputStream(mainFile));
        assertEquals("absValue", ep.getString("absKey"));
    }

    @Test
    public void testLoadWithIncludeDotSlash() throws IOException {
        File dir = new File(System.getProperty("java.io.tmpdir"), "testIncludeDot");
        dir.mkdirs();
        dir.deleteOnExit();
        File mainFile = new File(dir, "main.properties");
        mainFile.deleteOnExit();
        PrintWriter pw = new PrintWriter(new FileWriter(mainFile));
        pw.println("include = ./included.properties");
        pw.close();

        File includedFile = new File(dir, "included.properties");
        includedFile.deleteOnExit();
        pw = new PrintWriter(new FileWriter(includedFile));
        pw.println("dotKey = dotValue");
        pw.close();

        ExtendedProperties ep = new ExtendedProperties();
        ep.setInclude("include");
        ep.basePath = dir.getAbsolutePath() + File.separator;
        ep.load(new FileInputStream(mainFile));
        assertEquals("dotValue", ep.getString("dotKey"));
    }

    @Test
    public void testLoadWithIncludeFileNotFound() throws IOException {
        String input = "include = nonexistent.properties\nkey = value\n";
        props.setInclude("include");
        props.basePath = System.getProperty("java.io.tmpdir") + File.separator;
        props.load(new ByteArrayInputStream(input.getBytes()));
        assertEquals("value", props.getString("key"));
    }

    // addProperty tests
    @Test
    public void testAddPropertySimpleString() {
        props.addProperty("key", "value");
        assertEquals("value", props.getString("key"));
    }

    @Test
    public void testAddPropertyStringWithCommas() {
        props.addProperty("key", "a,b,c");
        List list = props.getList("key");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    @Test
    public void testAddPropertyStringWithEscapedCommas() {
        props.addProperty("key", "a\\,b,c");
        List list = props.getList("key");
        assertEquals(2, list.size());
        assertEquals("a,b", list.get(0));
        assertEquals("c", list.get(1));
    }

    @Test
    public void testAddPropertyMultipleCalls() {
        props.addProperty("key", "first");
        props.addProperty("key", "second");
        List list = props.getList("key");
        assertEquals(2, list.size());
        assertEquals("first", list.get(0));
        assertEquals("second", list.get(1));
    }

    @Test
    public void testAddPropertyNonString() {
        props.addProperty("key", new Integer(123));
        assertEquals(123, props.getInt("key"));
    }

    @Test
    public void testAddPropertyWithEscapedBackslash() {
        props.addProperty("key", "a\\\\b");
        assertEquals("a\\b", props.getString("key"));
    }

    // setProperty tests
    @Test
    public void testSetProperty() {
        props.addProperty("key", "old");
        props.setProperty("key", "new");
        assertEquals("new", props.getString("key"));
    }

    // clearProperty tests
    @Test
    public void testClearProperty() {
        props.addProperty("key1", "value1");
        props.addProperty("key2", "value2");
        props.clearProperty("key1");
        assertNull(props.getProperty("key1"));
        assertNotNull(props.getProperty("key2"));
        Iterator keys = props.getKeys();
        assertEquals("key2", keys.next());
        assertFalse(keys.hasNext());
    }

    // getProperty tests
    @Test
    public void testGetPropertyWithDefaults() {
        assertEquals("default.value", props.getString("default.key"));
    }

    @Test
    public void testGetPropertyWithoutDefaults() {
        props.defaults = null;
        assertNull(props.getProperty("nonexistent"));
    }

    // getKeys tests
    @Test
    public void testGetKeysOrder() {
        props.addProperty("z", "1");
        props.addProperty("a", "2");
        props.addProperty("m", "3");
        Iterator keys = props.getKeys();
        assertEquals("z", keys.next());
        assertEquals("a", keys.next());
        assertEquals("m", keys.next());
    }

    @Test
    public void testGetKeysWithPrefix() {
        props.addProperty("prefix.key1", "v1");
        props.addProperty("prefix.key2", "v2");
        props.addProperty("other", "v3");
        Iterator keys = props.getKeys("prefix");
        List<String> keyList = new ArrayList<String>();
        while (keys.hasNext()) keyList.add((String) keys.next());
        assertEquals(2, keyList.size());
        assertTrue(keyList.contains("prefix.key1"));
        assertTrue(keyList.contains("prefix.key2"));
    }

    // subset tests
    @Test
    public void testSubset() {
        props.addProperty("prefix.key1", "v1");
        props.addProperty("prefix.key2", "v2");
        props.addProperty("other", "v3");
        ExtendedProperties sub = props.subset("prefix");
        assertNotNull(sub);
        assertEquals("v1", sub.getString("key1"));
        assertEquals("v2", sub.getString("key2"));
        assertNull(sub.getProperty("other"));
    }

    @Test
    public void testSubsetNoMatch() {
        props.addProperty("key", "value");
        assertNull(props.subset("nonexistent"));
    }

    @Test
    public void testSubsetExactPrefix() {
        props.addProperty("prefix", "value");
        ExtendedProperties sub = props.subset("prefix");
        assertNotNull(sub);
        assertEquals("value", sub.getString("prefix"));
    }

    // combine tests
    @Test
    public void testCombine() {
        props.addProperty("key1", "v1");
        ExtendedProperties other = new ExtendedProperties();
        other.addProperty("key2", "v2");
        other.addProperty("key1", "overwrite");
        props.combine(other);
        assertEquals("overwrite", props.getString("key1"));
        assertEquals("v2", props.getString("key2"));
    }

    // save tests
    @Test
    public void testSave() throws IOException {
        props.addProperty("key", "value");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, "header");
        String output = baos.toString();
        assertTrue(output.contains("header"));
        assertTrue(output.contains("key=value"));
    }

    @Test
    public void testSaveNullOutput() throws IOException {
        props.save(null, "header"); // should not throw
    }

    @Test
    public void testSaveWithListValues() throws IOException {
        props.addProperty("key", "a,b");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, null);
        String output = baos.toString();
        assertTrue(output.contains("key=a"));
        assertTrue(output.contains("key=b"));
    }

    @Test
    public void testSaveEscapesCommasAndBackslashes() throws IOException {
        props.addProperty("key", "a\\,b\\\\c");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        props.save(baos, null);
        String output = baos.toString();
        assertTrue(output.contains("key=a\\\\,b\\\\\\\\c")); // escape doubles backslashes and commas
    }

    // getString tests
    @Test
    public void testGetStringSimple() {
        props.addProperty("key", "value");
        assertEquals("value", props.getString("key"));
    }

    @Test
    public void testGetStringInterpolation() {
        props.addProperty("key", "${default.key}");
        assertEquals("default.value", props.getString("key"));
    }

    @Test
    public void testGetStringWithDefault() {
        assertEquals("defaultVal", props.getString("missing", "defaultVal"));
    }

    @Test
    public void testGetStringWithList() {
        props.addProperty("key", "a,b");
        assertEquals("a", props.getString("key"));
    }

    @Test(expected = ClassCastException.class)
    public void testGetStringClassCastException() {
        props.addProperty("key", new Integer(1));
        props.getString("key");
    }

    // getStringArray tests
    @Test
    public void testGetStringArrayString() {
        props.addProperty("key", "value");
        String[] arr = props.getStringArray("key");
        assertEquals(1, arr.length);
        assertEquals("value", arr[0]);
    }

    @Test
    public void testGetStringArrayList() {
        props.addProperty("key", "a,b");
        String[] arr = props.getStringArray("key");
        assertEquals(2, arr.length);
        assertEquals("a", arr[0]);
        assertEquals("b", arr[1]);
    }

    @Test
    public void testGetStringArrayNull() {
        String[] arr = props.getStringArray("missing");
        assertEquals(0, arr.length);
    }

    @Test
    public void testGetStringArrayWithDefaults() {
        String[] arr = props.getStringArray("default.key");
        assertEquals(1, arr.length);
        assertEquals("default.value", arr[0]);
    }

    @Test(expected = ClassCastException.class)
    public void testGetStringArrayClassCastException() {
        props.addProperty("key", new Integer(1));
        props.getStringArray("key");
    }

    // getVector tests
    @Test
    public void testGetVectorString() {
        props.addProperty("key", "value");
        Vector v = props.getVector("key");
        assertEquals(1, v.size());
        assertEquals("value", v.get(0));
    }

    @Test
    public void testGetVectorList() {
        props.addProperty("key", "a,b");
        Vector v = props.getVector("key");
        assertEquals(2, v.size());
        assertEquals("a", v.get(0));
        assertEquals("b", v.get(1));
    }

    @Test
    public void testGetVectorNull() {
        Vector v = props.getVector("missing");
        assertTrue(v.isEmpty());
    }

    @Test
    public void testGetVectorWithDefault() {
        Vector def = new Vector();
        def.add("default");
        Vector v = props.getVector("missing", def);
        assertEquals(1, v.size());
        assertEquals("default", v.get(0));
    }

    @Test(expected = ClassCastException.class)
    public void testGetVectorClassCastException() {
        props.addProperty("key", new Integer(1));
        props.getVector("key");
    }

    // getList tests
    @Test
    public void testGetListString() {
        props.addProperty("key", "value");
        List list = props.getList("key");
        assertEquals(1, list.size());
        assertEquals("value", list.get(0));
    }

    @Test
    public void testGetListList() {
        props.addProperty("key", "a,b");
        List list = props.getList("key");
        assertEquals(2, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
    }

    @Test
    public void testGetListNull() {
        List list = props.getList("missing");
        assertTrue(list.isEmpty());
    }

    @Test
    public void testGetListWithDefault() {
        List def = new ArrayList();
        def.add("default");
        List list = props.getList("missing", def);
        assertEquals(1, list.size());
        assertEquals("default", list.get(0));
    }

    @Test(expected = ClassCastException.class)
    public void testGetListClassCastException() {
        props.addProperty("key", new Integer(1));
        props.getList("key");
    }

    // getBoolean tests
    @Test
    public void testGetBooleanTrueValues() {
        props.addProperty("key1", "true");
        props.addProperty("key2", "on");
        props.addProperty("key3", "yes");
        assertTrue(props.getBoolean("key1"));
        assertTrue(props.getBoolean("key2"));
        assertTrue(props.getBoolean("key3"));
    }

    @Test
    public void testGetBooleanFalseValues() {
        props.addProperty("key1", "false");
        props.addProperty("key2", "off");
        props.addProperty("key3", "no");
        assertFalse(props.getBoolean("key1"));
        assertFalse(props.getBoolean("key2"));
        assertFalse(props.getBoolean("key3"));
    }

    @Test
    public void testGetBooleanCaseInsensitive() {
        props.addProperty("key", "True");
        assertTrue(props.getBoolean("key"));
    }

    @Test
    public void testGetBooleanWithDefault() {
        assertTrue(props.getBoolean("missing", true));
        assertFalse(props.getBoolean("missing", false));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetBooleanNoSuchElement() {
        props.getBoolean("missing");
    }

    @Test(expected = ClassCastException.class)
    public void testGetBooleanClassCastException() {
        props.addProperty("key", new Integer(1));
        props.getBoolean("key");
    }

    // testBoolean method
    @Test
    public void testTestBoolean() {
        assertEquals("true", props.testBoolean("true"));
        assertEquals("true", props.testBoolean("on"));
        assertEquals("true", props.testBoolean("yes"));
        assertEquals("false", props.testBoolean("false"));
        assertEquals("false", props.testBoolean("off"));
        assertEquals("false", props.testBoolean("no"));
        assertNull(props.testBoolean("invalid"));
        assertNull(props.testBoolean(""));
    }

    // Numeric getters tests (Byte, Short, Int, Long, Float, Double)
    @Test
    public void testGetByte() {
        props.addProperty("key", "123");
        assertEquals(123, props.getByte("key"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetByteNoSuchElement() {
        props.getByte("missing");
    }

    @Test
    public void testGetByteWithDefault() {
        assertEquals(10, props.getByte("missing", (byte) 10));
    }

    @Test(expected = ClassCastException.class)
    public void testGetByteClassCastException() {
        props.addProperty("key", new Object());
        props.getByte("key");
    }

    @Test
    public void testGetShort() {
        props.addProperty("key", "123");
        assertEquals(123, props.getShort("key"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetShortNoSuchElement() {
        props.getShort("missing");
    }

    @Test
    public void testGetShortWithDefault() {
        assertEquals(10, props.getShort("missing", (short) 10));
    }

    @Test(expected = ClassCastException.class)
    public void testGetShortClassCastException() {
        props.addProperty("key", new Object());
        props.getShort("key");
    }

    @Test
    public void testGetInt() {
        props.addProperty("key", "123");
        assertEquals(123, props.getInt("key"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetIntNoSuchElement() {
        props.getInt("missing");
    }

    @Test
    public void testGetIntWithDefault() {
        assertEquals(10, props.getInt("missing", 10));
    }

    @Test(expected = ClassCastException.class)
    public void testGetIntClassCastException() {
        props.addProperty("key", new Object());
        props.getInt("key");
    }

    @Test
    public void testGetInteger() {
        props.addProperty("key", "123");
        assertEquals(123, props.getInteger("key"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetIntegerNoSuchElement() {
        props.getInteger("missing");
    }

    @Test
    public void testGetIntegerWithDefault() {
        assertEquals(10, props.getInteger("missing", 10));
    }

    @Test(expected = ClassCastException.class)
    public void testGetIntegerClassCastException() {
        props.addProperty("key", new Object());
        props.getInteger("key");
    }

    @Test
    public void testGetLong() {
        props.addProperty("key", "1234567890123");
        assertEquals(1234567890123L, props.getLong("key"));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetLongNoSuchElement() {
        props.getLong("missing");
    }

    @Test
    public void testGetLongWithDefault() {
        assertEquals(10L, props.getLong("missing", 10L));
    }

    @Test(expected = ClassCastException.class)
    public void testGetLongClassCastException() {
        props.addProperty("key", new Object());
        props.getLong("key");
    }

    @Test
    public void testGetFloat() {
        props.addProperty("key", "12.34");
        assertEquals(12.34f, props.getFloat("key"), 0.001);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetFloatNoSuchElement() {
        props.getFloat("missing");
    }

    @Test
    public void testGetFloatWithDefault() {
        assertEquals(1.23f, props.getFloat("missing", 1.23f), 0.001);
    }

    @Test(expected = ClassCastException.class)
    public void testGetFloatClassCastException() {
        props.addProperty("key", new Object());
        props.getFloat("key");
    }

    @Test
    public void testGetDouble() {
        props.addProperty("key", "12.34");
        assertEquals(12.34, props.getDouble("key"), 0.001);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetDoubleNoSuchElement() {
        props.getDouble("missing");
    }

    @Test
    public void testGetDoubleWithDefault() {
        assertEquals(1.23, props.getDouble("missing", 1.23), 0.001);
    }

    @Test(expected = ClassCastException.class)
    public void testGetDoubleClassCastException() {
        props.addProperty("key", new Object());
        props.getDouble("key");
    }

    // Interpolation tests
    @Test
    public void testInterpolateSimple() {
        props.addProperty("key", "${default.key}");
        assertEquals("default.value", props.getString("key"));
    }

    @Test
    public void testInterpolateNested() {
        props.addProperty("key1", "value1");
        props.addProperty("key2", "${key1}");
        props.addProperty("key3", "${key2}");
        assertEquals("value1", props.getString("key3"));
    }

    @Test
    public void testInterpolateMissingVariable() {
        props.addProperty("key", "${missing}");
        assertEquals("${missing}", props.getString("key"));
    }

    @Test
    public void testInterpolateWithDefaults() {
        props.addProperty("key", "${default.key}");
        assertEquals("default.value", props.getString("key"));
    }

    @Test(expected = IllegalStateException.class)
    public void testInterpolateLoop() {
        props.addProperty("key1", "${key2}");
        props.addProperty("key2", "${key1}");
        props.getString("key1");
    }

    @Test
    public void testInterpolateHelperNull() {
        assertNull(props.interpolateHelper(null, null));
    }

    // PropertiesReader tests
    @Test
    public void testPropertiesReaderSimple() throws IOException {
        String input = "key = value\n";
        ExtendedProperties.PropertiesReader reader = new ExtendedProperties.PropertiesReader(new StringReader(input));
        assertEquals("key = value", reader.readProperty());
        assertNull(reader.readProperty());
    }

    @Test
    public void testPropertiesReaderMultiline() throws IOException {
        String input = "key = line1\\\nline2\\\nline3\n";
        ExtendedProperties.PropertiesReader reader = new ExtendedProperties.PropertiesReader(new StringReader(input));
        assertEquals("key = line1line2line3", reader.readProperty());
    }

    @Test
    public void testPropertiesReaderCommentsAndBlankLines() throws IOException {
        String input = "# comment\n\nkey = value\n";
        ExtendedProperties.PropertiesReader reader = new ExtendedProperties.PropertiesReader(new StringReader(input));
        assertEquals("key = value", reader.readProperty());
    }

    @Test
    public void testPropertiesReaderEmptyFile() throws IOException {
        ExtendedProperties.PropertiesReader reader = new ExtendedProperties.PropertiesReader(new StringReader(""));
        assertNull(reader.readProperty());
    }

    // PropertiesTokenizer tests
    @Test
    public void testPropertiesTokenizerSimple() {
        ExtendedProperties.PropertiesTokenizer tokenizer = new ExtendedProperties.PropertiesTokenizer("a,b,c");
        assertTrue(tokenizer.hasMoreTokens());
        assertEquals("a", tokenizer.nextToken());
        assertEquals("b", tokenizer.nextToken());
        assertEquals("c", tokenizer.nextToken());
        assertFalse(tokenizer.hasMoreTokens());
    }

    @Test
    public void testPropertiesTokenizerEscapedCommas() {
        ExtendedProperties.PropertiesTokenizer tokenizer = new ExtendedProperties.PropertiesTokenizer("a\\,b,c");
        assertEquals("a,b", tokenizer.nextToken());
        assertEquals("c", tokenizer.nextToken());
    }

    @Test
    public void testPropertiesTokenizerEscapedBackslash() {
        ExtendedProperties.PropertiesTokenizer tokenizer = new ExtendedProperties.PropertiesTokenizer("a\\\\b");
        assertEquals("a\\b", tokenizer.nextToken());
    }

    @Test
    public void testPropertiesTokenizerEmptyString() {
        ExtendedProperties.PropertiesTokenizer tokenizer = new ExtendedProperties.PropertiesTokenizer("");
        assertFalse(tokenizer.hasMoreTokens());
    }

    // convertProperties test
    @Test
    public void testConvertProperties() {
        Properties p = new Properties();
        p.setProperty("key1", "value1");
        p.setProperty("key2", "value2");
        ExtendedProperties ep = ExtendedProperties.convertProperties(p);
        assertEquals("value1", ep.getString("key1"));
        assertEquals("value2", ep.getString("key2"));
    }

    @Test
    public void testConvertPropertiesWithDefaults() {
        Properties defaults = new Properties();
        defaults.setProperty("defaultKey", "defaultValue");
        Properties p = new Properties(defaults);
        p.setProperty("key", "value");
        ExtendedProperties ep = ExtendedProperties.convertProperties(p);
        assertEquals("value", ep.getString("key"));
        // defaults are not automatically loaded into ExtendedProperties, only the main properties
        assertNull(ep.getProperty("defaultKey"));
    }

    // isInitialized tests
    @Test
    public void testIsInitializedAfterLoad() throws IOException {
        assertFalse(props.isInitialized());
        props.load(new ByteArrayInputStream("key=value".getBytes()));
        assertTrue(props.isInitialized());
    }

    @Test
    public void testIsInitializedAfterAddProperty() {
        assertFalse(props.isInitialized());
        props.addProperty("key", "value");
        assertTrue(props.isInitialized());
    }

    // getInclude/setInclude tests
    @Test
    public void testGetSetInclude() {
        assertEquals("include", props.getInclude());
        props.setInclude("import");
        assertEquals("import", props.getInclude());
        // static variable is changed
        assertEquals("import", ExtendedProperties.include);
    }

    // Helper method to count keys
    private int sizeOfKeys(ExtendedProperties ep) {
        int count = 0;
        Iterator it = ep.getKeys();
        while (it.hasNext()) {
            it.next();
            count++;
        }
        return count;
    }
}
