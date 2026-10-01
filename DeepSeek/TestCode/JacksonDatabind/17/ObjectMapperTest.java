package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.cfg.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.*;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.*;

public class ObjectMapperTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDefaultConstructor() {
        ObjectMapper m = new ObjectMapper();
        assertNotNull(m);
        assertNotNull(m.getFactory());
        assertTrue(m.getFactory() instanceof MappingJsonFactory);
    }

    @Test
    public void testConstructorWithJsonFactory() {
        JsonFactory jf = new JsonFactory();
        ObjectMapper m = new ObjectMapper(jf);
        assertSame(jf, m.getFactory());
    }

    @Test
    public void testConstructorWithNullJsonFactory() {
        ObjectMapper m = new ObjectMapper((JsonFactory) null);
        assertNotNull(m.getFactory());
        assertTrue(m.getFactory() instanceof MappingJsonFactory);
    }

    @Test
    public void testConstructorWithAllArgs() {
        ObjectMapper m = new ObjectMapper(null, null, null);
        assertNotNull(m);
    }

    @Test
    public void testCopy() {
        ObjectMapper m2 = mapper.copy();
        assertNotNull(m2);
        assertNotSame(mapper, m2);
        // copy should have same config but independent
        assertNotNull(m2.getSerializationConfig());
        assertNotNull(m2.getDeserializationConfig());
    }

    @Test(expected = IllegalStateException.class)
    public void testCopyMustBeOverridden() {
        new SubClassNoCopy().copy();
    }

    static class SubClassNoCopy extends ObjectMapper { }

    @Test
    public void testVersion() {
        assertNotNull(mapper.version());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterNullModule() {
        mapper.registerModule(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegisterNullVersionModule() {
        mapper.registerModule(new SimpleModule("test") {
            @Override
            public Version version() { return null; }
        });
    }

    @Test
    public void testRegisterModule() {
        SimpleModule mod = new SimpleModule("test", new Version(1,0,0,"","",""));
        ObjectMapper m = new ObjectMapper();
        assertSame(m, m.registerModule(mod));
    }

    @Test
    public void testRegisterModulesArray() {
        ObjectMapper m = new ObjectMapper();
        SimpleModule mod1 = new SimpleModule("test1", new Version(1,0,0,"","",""));
        SimpleModule mod2 = new SimpleModule("test2", new Version(1,0,0,"","",""));
        assertSame(m, m.registerModules(mod1, mod2));
    }

    @Test
    public void testRegisterModulesIterable() {
        ObjectMapper m = new ObjectMapper();
        SimpleModule mod = new SimpleModule("test", new Version(1,0,0,"","",""));
        List<Module> list = new ArrayList<Module>();
        list.add(mod);
        assertSame(m, m.registerModules(list));
    }

    @Test
    public void testFindModules() {
        List<Module> modules = ObjectMapper.findModules();
        assertNotNull(modules);
    }

    @Test
    public void testFindModulesWithClassLoader() {
        List<Module> modules = ObjectMapper.findModules(getClass().getClassLoader());
        assertNotNull(modules);
    }

    @Test
    public void testFindAndRegisterModules() {
        ObjectMapper m = new ObjectMapper();
        assertSame(m, m.findAndRegisterModules());
    }

    @Test
    public void testGetSerializationConfig() {
        assertNotNull(mapper.getSerializationConfig());
    }

    @Test
    public void testGetDeserializationConfig() {
        assertNotNull(mapper.getDeserializationConfig());
    }

    @Test
    public void testGetDeserializationContext() {
        assertNotNull(mapper.getDeserializationContext());
    }

    @Test
    public void testSetSerializerFactory() {
        SerializerFactory sf = BeanSerializerFactory.instance;
        assertSame(mapper, mapper.setSerializerFactory(sf));
    }

    @Test
    public void testGetSerializerFactory() {
        assertNotNull(mapper.getSerializerFactory());
    }

    @Test
    public void testSetSerializerProvider() {
        DefaultSerializerProvider sp = new DefaultSerializerProvider.Impl();
        assertSame(mapper, mapper.setSerializerProvider(sp));
    }

    @Test
    public void testGetSerializerProvider() {
        assertNotNull(mapper.getSerializerProvider());
    }

    @Test
    public void testSetMixInAnnotations() {
        Map<Class<?>, Class<?>> mixins = new HashMap<Class<?>, Class<?>>();
        mixins.put(String.class, Integer.class);
        mapper.setMixInAnnotations(mixins);
        assertEquals(1, mapper.mixInCount());
    }

    @Test
    public void testAddMixInAnnotations() {
        mapper.addMixInAnnotations(String.class, Integer.class);
        assertEquals(String.class, mapper.findMixInClassFor(String.class));
    }

    @Test
    public void testAddMixIn() {
        assertSame(mapper, mapper.addMixIn(String.class, Integer.class));
    }

    @Test
    public void testFindMixInClassFor() {
        mapper.addMixInAnnotations(Integer.class, String.class);
        assertEquals(String.class, mapper.findMixInClassFor(Integer.class));
    }

    @Test
    public void testMixInCount() {
        mapper.addMixInAnnotations(String.class, Integer.class);
        assertEquals(1, mapper.mixInCount());
        mapper.addMixInAnnotations(Boolean.class, Long.class);
        assertEquals(2, mapper.mixInCount());
    }

    @Test
    public void testGetVisibilityChecker() {
        assertNotNull(mapper.getVisibilityChecker());
    }

    @Test
    public void testSetVisibilityChecker() {
        VisibilityChecker<?> vc = VisibilityChecker.Std.defaultInstance();
        mapper.setVisibilityChecker(vc);
        // just check no exception
    }

    @Test
    public void testSetVisibility() {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        assertEquals(JsonAutoDetect.Visibility.ANY,
                mapper.getSerializationConfig().getDefaultVisibilityChecker().getFieldVisibility());
    }

    @Test
    public void testGetSubtypeResolver() {
        assertNotNull(mapper.getSubtypeResolver());
    }

    @Test
    public void testSetSubtypeResolver() {
        SubtypeResolver str = new StdSubtypeResolver();
        assertSame(mapper, mapper.setSubtypeResolver(str));
    }

    @Test
    public void testSetAnnotationIntrospector() {
        AnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        assertSame(mapper, mapper.setAnnotationIntrospector(ai));
    }

    @Test
    public void testSetAnnotationIntrospectors() {
        AnnotationIntrospector ai = new JacksonAnnotationIntrospector();
        assertSame(mapper, mapper.setAnnotationIntrospectors(ai, ai));
    }

    @Test
    public void testSetPropertyNamingStrategy() {
        PropertyNamingStrategy strategy = PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES;
        assertSame(mapper, mapper.setPropertyNamingStrategy(strategy));
    }

    @Test
    public void testSetSerializationInclusion() {
        assertSame(mapper, mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL));
    }

    @Test
    public void testEnableDefaultTypingNoArg() {
        assertSame(mapper, mapper.enableDefaultTyping());
    }

    @Test
    public void testEnableDefaultTypingDefaultTyping() {
        assertSame(mapper, mapper.enableDefaultTyping(DefaultTyping.NON_FINAL));
    }

    @Test
    public void testEnableDefaultTypingWithAs() {
        assertSame(mapper, mapper.enableDefaultTyping(DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY));
    }

    @Test
    public void testEnableDefaultTypingAsProperty() {
        assertSame(mapper, mapper.enableDefaultTypingAsProperty(DefaultTyping.JAVA_LANG_OBJECT, "t"));
    }

    @Test
    public void testDisableDefaultTyping() {
        assertSame(mapper, mapper.disableDefaultTyping());
    }

    @Test
    public void testSetDefaultTyping() {
        assertSame(mapper, mapper.setDefaultTyping(null));
    }

    @Test
    public void testRegisterSubtypesClasses() {
        mapper.registerSubtypes(String.class, Integer.class);
        // no exception
    }

    @Test
    public void testRegisterSubtypesNamedTypes() {
        mapper.registerSubtypes(new NamedType(String.class, "string"));
    }

    @Test
    public void testGetTypeFactory() {
        assertNotNull(mapper.getTypeFactory());
    }

    @Test
    public void testSetTypeFactory() {
        TypeFactory tf = TypeFactory.defaultInstance();
        assertSame(mapper, mapper.setTypeFactory(tf));
    }

    @Test
    public void testConstructType() {
        JavaType t = mapper.constructType(String.class);
        assertNotNull(t);
        assertEquals(String.class, t.getRawClass());
    }

    @Test
    public void testSetNodeFactory() {
        JsonNodeFactory nf = JsonNodeFactory.instance;
        assertSame(mapper, mapper.setNodeFactory(nf));
    }

    @Test
    public void testAddHandler() {
        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        assertSame(mapper, mapper.addHandler(handler));
    }

    @Test
    public void testClearProblemHandlers() {
        assertSame(mapper, mapper.clearProblemHandlers());
    }

    @Test
    public void testSetConfigDeserialization() {
        DeserializationConfig config = mapper.getDeserializationConfig();
        assertSame(mapper, mapper.setConfig(config));
    }

    @Test
    public void testSetFilters() {
        mapper.setFilters(null); // just call
    }

    @Test
    public void testSetBase64Variant() {
        assertSame(mapper, mapper.setBase64Variant(Base64Variants.getDefaultVariant()));
    }

    @Test
    public void testSetConfigSerialization() {
        SerializationConfig config = mapper.getSerializationConfig();
        assertSame(mapper, mapper.setConfig(config));
    }

    @Test
    public void testGetFactory() {
        assertNotNull(mapper.getFactory());
    }

    @Deprecated
    @Test
    public void testGetJsonFactory() {
        assertNotNull(mapper.getJsonFactory());
    }

    @Test
    public void testSetDateFormat() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        assertSame(mapper, mapper.setDateFormat(df));
    }

    @Test
    public void testSetHandlerInstantiator() {
        assertSame(mapper, mapper.setHandlerInstantiator(null));
    }

    @Test
    public void testSetInjectableValues() {
        assertSame(mapper, mapper.setInjectableValues(null));
    }

    @Test
    public void testSetLocale() {
        assertSame(mapper, mapper.setLocale(Locale.US));
    }

    @Test
    public void testSetTimeZone() {
        assertSame(mapper, mapper.setTimeZone(TimeZone.getDefault()));
    }

    @Test
    public void testConfigureMapperFeature() {
        assertSame(mapper, mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true));
    }

    @Test
    public void testConfigureSerializationFeature() {
        assertSame(mapper, mapper.configure(SerializationFeature.INDENT_OUTPUT, true));
    }

    @Test
    public void testConfigureDeserializationFeature() {
        assertSame(mapper, mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    }

    @Test
    public void testConfigureJsonParserFeature() {
        assertSame(mapper, mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true));
    }

    @Test
    public void testConfigureJsonGeneratorFeature() {
        assertSame(mapper, mapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true));
    }

    @Test
    public void testEnableMapperFeatures() {
        assertSame(mapper, mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
    }

    @Test
    public void testDisableMapperFeatures() {
        assertSame(mapper, mapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
    }

    @Test
    public void testEnableDeserializationFeatureSingle() {
        assertSame(mapper, mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
    }

    @Test
    public void testEnableDeserializationFeatureMulti() {
        assertSame(mapper, mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS,
                DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
    }

    @Test
    public void testDisableDeserializationFeatureSingle() {
        assertSame(mapper, mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
    }

    @Test
    public void testDisableDeserializationFeatureMulti() {
        assertSame(mapper, mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                DeserializationFeature.UNWRAP_ROOT_VALUE));
    }

    @Test
    public void testEnableSerializationFeatureSingle() {
        assertSame(mapper, mapper.enable(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testEnableSerializationFeatureMulti() {
        assertSame(mapper, mapper.enable(SerializationFeature.INDENT_OUTPUT,
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    public void testDisableSerializationFeatureSingle() {
        assertSame(mapper, mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    public void testDisableSerializationFeatureMulti() {
        assertSame(mapper, mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                SerializationFeature.FAIL_ON_EMPTY_BEANS));
    }

    @Test
    public void testIsEnabledMapperFeature() {
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, false);
        assertFalse(mapper.isEnabled(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
    }

    @Test
    public void testIsEnabledSerializationFeature() {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        assertFalse(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT));
    }

    @Test
    public void testIsEnabledDeserializationFeature() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        assertTrue(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }

    @Test
    public void testIsEnabledJsonFactoryFeature() {
        mapper.getFactory().disable(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES);
        assertFalse(mapper.isEnabled(JsonFactory.Feature.CANONICALIZE_FIELD_NAMES));
    }

    @Test
    public void testIsEnabledJsonParserFeature() {
        mapper.getFactory().disable(JsonParser.Feature.ALLOW_COMMENTS);
        assertFalse(mapper.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
    }

    @Test
    public void testIsEnabledJsonGeneratorFeature() {
        mapper.getFactory().disable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        assertFalse(mapper.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));
    }

    @Test
    public void testGetNodeFactory() {
        assertNotNull(mapper.getNodeFactory());
    }

    @Test
    public void testReadTreeJsonParser() throws IOException {
        String json = "{\"a\":1}";
        JsonParser jp = mapper.getFactory().createParser(json);
        JsonNode node = mapper.readTree(jp);
        assertNotNull(node);
        assertTrue(node.isObject());
        jp.close();
    }

    @Test
    public void testReadTreeJsonParserNull() throws IOException {
        JsonParser jp = mapper.getFactory().createParser("null");
        JsonNode node = mapper.readTree(jp);
        assertTrue(node.isNull());
        jp.close();
    }

    @Test
    public void testReadTreeInputStream() throws IOException {
        String json = "true";
        JsonNode node = mapper.readTree(new ByteArrayInputStream(json.getBytes()));
        assertTrue(node.isBoolean());
    }

    @Test
    public void testReadTreeReader() throws IOException {
        JsonNode node = mapper.readTree(new StringReader("[]"));
        assertTrue(node.isArray());
    }

    @Test
    public void testReadTreeString() throws IOException {
        JsonNode node = mapper.readTree("123");
        assertTrue(node.isInt());
    }

    @Test
    public void testReadTreeByteArray() throws IOException {
        JsonNode node = mapper.readTree("false".getBytes());
        assertTrue(node.isBoolean());
    }

    @Test
    public void testReadTreeFile() throws IOException {
        File tmp = File.createTempFile("test", ".json");
        tmp.deleteOnExit();
        FileWriter fw = new FileWriter(tmp);
        fw.write("\"text\"");
        fw.close();
        JsonNode node = mapper.readTree(tmp);
        assertTrue(node.isTextual());
    }

    @Test
    public void testReadTreeURL() throws IOException {
        URL url = new URL("http://localhost:1/"); // dummy, will throw, so use alternative
        // Actually, we can test with a data URL? Better not rely on network. Instead test exception path.
        try {
            mapper.readTree(new URL("file:///nonexistent"));
            fail("Should have thrown IOException");
        } catch (IOException e) {
            // expected
        }
    }

    @Test
    public void testReadValueFromFile() throws IOException {
        File tmp = File.createTempFile("test", ".json");
        tmp.deleteOnExit();
        FileWriter fw = new FileWriter(tmp);
        fw.write("42");
        fw.close();
        Integer val = mapper.readValue(tmp, Integer.class);
        assertEquals((Integer)42, val);
    }

    @Test
    public void testReadValueFromFileTypeRef() throws IOException {
        File tmp = File.createTempFile("test", ".json");
        tmp.deleteOnExit();
        FileWriter fw = new FileWriter(tmp);
        fw.write("[1,2]");
        fw.close();
        List<Integer> list = mapper.readValue(tmp, new TypeReference<List<Integer>>() {});
        assertArrayEquals(new Integer[]{1,2}, list.toArray(new Integer[0]));
    }

    @Test
    public void testReadValueFromURL() throws IOException {
        URL url = new URL("file:///nonexistent");
        try {
            mapper.readValue(url, Integer.class);
            fail();
        } catch (IOException e) {}
    }

    @Test
    public void testReadValueFromString() throws IOException {
        String json = "\"hello\"";
        String val = mapper.readValue(json, String.class);
        assertEquals("hello", val);
    }

    @Test
    public void testReadValueFromReader() throws IOException {
        Reader r = new StringReader("true");
        Boolean val = mapper.readValue(r, Boolean.class);
        assertTrue(val);
    }

    @Test
    public void testReadValueFromInputStream() throws IOException {
        InputStream in = new ByteArrayInputStream("123".getBytes());
        Integer val = mapper.readValue(in, Integer.class);
        assertEquals((Integer)123, val);
    }

    @Test
    public void testReadValueFromByteArray() throws IOException {
        byte[] bytes = "\"test\"".getBytes();
        String val = mapper.readValue(bytes, String.class);
        assertEquals("test", val);
    }

    @Test
    public void testReadValueFromByteArrayOffsetLen() throws IOException {
        byte[] bytes = "[[\"value\"]]".getBytes();
        String val = mapper.readValue(bytes, 1, bytes.length-2, String.class);
        assertEquals("value", val);
    }

    @Test
    public void testReadValues() throws IOException {
        String json = "1 2 3";
        JsonParser jp = mapper.getFactory().createParser(json);
        MappingIterator<Integer> it = mapper.readValues(jp, Integer.class);
        assertTrue(it.hasNext());
        assertEquals((Integer)1, it.next());
        assertEquals((Integer)2, it.next());
        assertEquals((Integer)3, it.next());
        assertFalse(it.hasNext());
        jp.close();
    }

    @Test
    public void testCanSerialize() {
        assertTrue(mapper.canSerialize(String.class));
    }

    @Test
    public void testCanSerializeAtomicRef() {
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canSerialize(Integer.class, cause));
    }

    @Test
    public void testCanDeserialize() {
        assertTrue(mapper.canDeserialize(mapper.constructType(String.class));
    }

    @Test
    public void testCanDeserializeAtomicRef() {
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        assertTrue(mapper.canDeserialize(mapper.constructType(Boolean.class), cause));    }

    @Test
    public void testWriteValueGenerator() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        mapper.writeValue(gen, "text");
        gen.close();
        assertEquals("\"text\"", sw.toString());
    }

    @Test
    public void testWriteTreeGenerator() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        mapper.writeTree(gen, mapper.getNodeFactory().numberNode(1));
        gen.close();
        assertEquals("1", sw.toString());
    }

    @Test
    public void testCreateObjectNode() {
        assertNotNull(mapper.createObjectNode());
    }

    @Test
    public void testCreateArrayNode() {
        assertNotNull(mapper.createArrayNode());
    }

    @Test
    public void testTreeAsTokens() throws IOException {
        JsonNode root = mapper.readTree("{\"a\":1}");
        JsonParser tp = mapper.treeAsTokens(root);
        assertNotNull(tp);
        tp.close();
    }

    @Test
    public void testTreeToValue() throws IOException {
        JsonNode node = mapper.readTree("123");
        Integer val = mapper.treeToValue(node, Integer.class);
        assertEquals((Integer)123, val);
    }

    @Test
    public void testValueToTree() {
        JsonNode node = mapper.valueToTree(42);
        assertNotNull(node);
        assertEquals(42, node.intValue());
    }

    @Test
    public void testConvertValue() {
        String val = mapper.convertValue("hello", String.class);
        assertEquals("hello", val);
    }

    @Test
    public void testConvertValueNull() {
        assertNull(mapper.convertValue(null, String.class));
    }

    @Test
    public void testConvertValueTypeRef() {
        List<Integer> list = mapper.convertValue(new Integer[]{1,2,3}, new TypeReference<List<Integer>>() {});
        assertEquals(3, list.size());
    }

    @Test
    public void testWriter() {
        assertNotNull(mapper.writer());
    }

    @Test
    public void testWriterSerializationFeature() {
        assertNotNull(mapper.writer(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    public void testWriterMultiFeatures() {
        assertNotNull(mapper.writer(SerializationFeature.INDENT_OUTPUT, SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    public void testWriterDateFormat() {
        assertNotNull(mapper.writer(new SimpleDateFormat()));
    }

    @Test
    public void testWriterWithView() {
        assertNotNull(mapper.writerWithView(String.class));
    }

    @Test
    public void testWriterWithTypeClass() {
        assertNotNull(mapper.writerWithType(String.class));
    }

    @Test
    public void testWriterWithTypeTypeRef() {
        assertNotNull(mapper.writerWithType(new TypeReference<List<String>>() {}));
    }

    @Test
    public void testWriterWithTypeJavaType() {
        assertNotNull(mapper.writerWithType(mapper.constructType(String.class)));
    }

    @Test
    public void testWriterPrettyPrinter() {
        assertNotNull(mapper.writer((PrettyPrinter) null));
    }

    @Test
    public void testWriterWithDefaultPrettyPrinter() {
        assertNotNull(mapper.writerWithDefaultPrettyPrinter());
    }

    @Test
    public void testWriterFilterProvider() {
        assertNotNull(mapper.writer((FilterProvider) null));
    }

    @Test
    public void testWriterFormatSchema() {
        // cannot easily instantiate; just verify it throws if schema type unsupported
        try {
            mapper.writer(new FormatSchema() {
                @Override public String getSchemaType() { return "INVALID"; }
            });
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testWriterBase64Variant() {
        assertNotNull(mapper.writer(Base64Variants.getDefaultVariant()));
    }

    @Test
    public void testWriterCharacterEscapes() {
        assertNotNull(mapper.writer(new CharacterEscapes() {
            @Override public int[] getEscapeCodesForAscii() { return new int[0]; }
            @Override public SerializableString getEscapeSequence(int ch) { return null; }
        }));
    }

    @Test
    public void testWriterContextAttributes() {
        assertNotNull(mapper.writer((ContextAttributes) ContextAttributes.getEmpty()));
    }

    @Test
    public void testReader() {
        assertNotNull(mapper.reader());
    }

    @Test
    public void testReaderDeserializationFeatureSingle() {
        assertNotNull(mapper.reader(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
    }

    @Test
    public void testReaderMultiFeatures() {
        assertNotNull(mapper.reader(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                DeserializationFeature.UNWRAP_ROOT_VALUE));
    }

    @Test
    public void testReaderForUpdating() {
        assertNotNull(mapper.readerForUpdating(new HashMap<String,Object>()));
    }

    @Test
    public void testReaderJavaType() {
        assertNotNull(mapper.reader(mapper.constructType(String.class)));
    }

    @Test
    public void testReaderClass() {
        assertNotNull(mapper.reader(Integer.class));
    }

    @Test
    public void testReaderTypeRef() {
        assertNotNull(mapper.reader(new TypeReference<List<String>>() {}));
    }

    @Test
    public void testReaderJsonNodeFactory() {
        assertNotNull(mapper.reader(JsonNodeFactory.instance));
    }

    @Test
    public void testReaderFormatSchema() {
        assertNotNull(mapper.reader(new FormatSchema() {
            @Override public String getSchemaType() { return "Dummy"; }
        }));
    }

    @Test
    public void testReaderWithView() {
        assertNotNull(mapper.readerWithView(Object.class));
    }

    @Test
    public void testReaderBase64Variant() {
        assertNotNull(mapper.reader(Base64Variants.getDefaultVariant()));
    }

    @Test
    public void testReaderContextAttributes() {
        assertNotNull(mapper.reader(ContextAttributes.getEmpty()));
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws JsonMappingException {
        mapper.acceptJsonFormatVisitor(String.class, new JsonFormatVisitorWrapper() {
            @Override public JsonObjectFormatVisitor expectObjectFormat(JavaType type) { return null; }
            @Override public JsonArrayFormatVisitor expectArrayFormat(JavaType type) { return null; }
            @Override public JsonStringFormatVisitor expectStringFormat(JavaType type) { return null; }
            @Override public JsonNumberFormatVisitor expectNumberFormat(JavaType type) { return null; }
            @Override public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) { return null; }
            @Override public JsonBooleanFormatVisitor expectBooleanFormat(JavaType type) { return null; }
            @Override public JsonNullFormatVisitor expectNullFormat(JavaType type) { return null; }
            @Override public JsonAnyFormatVisitor expectAnyFormat(JavaType type) { return null; }
            @Override public JsonMapFormatVisitor expectMapFormat(JavaType type) { return null; }
        });
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAcceptJsonFormatVisitorNullType() throws JsonMappingException {
        mapper.acceptJsonFormatVisitor((JavaType) null, new JsonFormatVisitorWrapper() { /* empty */ });
    }

    @Test
    public void testDefaultTypeResolverBuilderUseForType() {
        DefaultTypeResolverBuilder b = new DefaultTypeResolverBuilder(DefaultTyping.JAVA_LANG_OBJECT);
        assertTrue(b.useForType(mapper.constructType(Object.class));
        assertFalse(b.useForType(mapper.constructType(String.class));
    }

    @Test
    public void testDefaultTypeResolverBuilderObjectAndNonConcrete() {
        DefaultTypeResolverBuilder b = new DefaultTypeResolverBuilder(DefaultTyping.OBJECT_AND_NON_CONCRETE);
        assertTrue(b.useForType(mapper.constructType(Object.class));
        assertTrue(b.useForType(mapper.constructType(AbstractList.class)));
        assertFalse(b.useForType(mapper.constructType(String.class)));
    }

    @Test
    public void testDefaultTypeResolverBuilderNonConcreteAndArrays() {
        DefaultTypeResolverBuilder b = new DefaultTypeResolverBuilder(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
        assertTrue(b.useForType(mapper.constructType(Object[].class));
        assertTrue(b.useForType(mapper.constructType(Number.class));
        assertFalse(b.useForType(mapper.constructType(Integer.class));
    }

    @Test
    public void testDefaultTypeResolverBuilderNonFinal() {
        DefaultTypeResolverBuilder b = new DefaultTypeResolverBuilder(DefaultTyping.NON_FINAL);
        assertTrue(b.useForType(mapper.constructType(Number.class));
        assertFalse(b.useForType(mapper.constructType(Integer.class));
    }

    @Test
    public void testWriteValueAsString() throws JsonProcessingException {
        String result = mapper.writeValueAsString(123);
        assertEquals("123", result);
    }

    @Test
    public void testWriteValueAsBytes() throws JsonProcessingException {
        byte[] result = mapper.writeValueAsBytes(true);
        assertEquals("true", new String(result));
    }

    @Test(expected=JsonMappingException.class)
    public void testInitForReadingEndOfInput() throws IOException {
        JsonParser jp = mapper.getFactory().createParser("");
        jp.nextToken(); // no tokens
        // will trigger _initForReading -> end-of-input
        jp.clearCurrentToken();
        mapper._initForReading(jp); // protected, we can't access, but we can call readValue that uses it.
        // Actually we can test via readTree on empty string (no content)
        // Use reflection? Better test via readValue("").
        // readValue("", String.class) will throw JsonMappingException: "No content to map due to end-of-input"
        mapper.readValue("", String.class);
    }

    @Test
    public void testUnwrap() throws IOException {
        ObjectMapper m = new ObjectMapper();
        m.enable(SerializationFeature.WRAP_ROOT_VALUE);
        // write then read should work
        String json = m.writeValueAsString(new IntWrapper(5));
        IntWrapper obj = m.readValue(json, IntWrapper.class);
        assertEquals(5, obj.value);
    }

    static class IntWrapper {
        public int value;
        public IntWrapper() {}
        public IntWrapper(int v) { value = v; }        public int getValue() { return value; }        public void setValue(int v) { value = v; }
    }
}
