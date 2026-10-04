package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ObjectReaderTest {

    private ObjectMapper mapper;
    private ObjectReader reader;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        reader = mapper.reader();
    }

    @Test
    public void testVersion() {
        Version v = reader.version();
        Assert.assertNotNull(v);
        Assert.assertFalse(v.isUnknownVersion());
    }

    @Test
    public void testAccessorsAndInitialState() {
        Assert.assertNotNull(reader.getConfig());
        Assert.assertNotNull(reader.getFactory());
        Assert.assertNotNull(reader.getTypeFactory());
        Assert.assertNotNull(reader.getAttributes());
        Assert.assertNull(reader.getInjectableValues());
        Assert.assertTrue(reader.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assert.assertTrue(reader.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
        Assert.assertFalse(reader.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
    }

    @Test
    public void testWithAndWithoutDeserializationFeatures() {
        ObjectReader r = reader.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        Assert.assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        Assert.assertSame(r, r.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        r = r.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        Assert.assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        r = reader.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        Assert.assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));

        r = r.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        Assert.assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));

        r = reader.withFeatures(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        Assert.assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

        r = r.withoutFeatures(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        Assert.assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
    }

    @Test
    public void testWithAndWithoutJsonParserFeatures() {
        ObjectReader r = reader.with(JsonParser.Feature.ALLOW_COMMENTS);
        Assert.assertTrue(r.getConfig().isEnabled(JsonParser.Feature.ALLOW_COMMENTS, r.getFactory()));

        r = r.without(JsonParser.Feature.ALLOW_COMMENTS);
        Assert.assertFalse(r.getConfig().isEnabled(JsonParser.Feature.ALLOW_COMMENTS, r.getFactory()));

        r = reader.withFeatures(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_YAML_COMMENTS);
        Assert.assertTrue(r.getConfig().isEnabled(JsonParser.Feature.ALLOW_COMMENTS, r.getFactory()));
        Assert.assertTrue(r.getConfig().isEnabled(JsonParser.Feature.ALLOW_YAML_COMMENTS, r.getFactory()));

        r = r.withoutFeatures(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_YAML_COMMENTS);
        Assert.assertFalse(r.getConfig().isEnabled(JsonParser.Feature.ALLOW_COMMENTS, r.getFactory()));
        Assert.assertFalse(r.getConfig().isEnabled(JsonParser.Feature.ALLOW_YAML_COMMENTS, r.getFactory()));

        r = reader.without(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_YAML_COMMENTS);
        Assert.assertFalse(r.getConfig().isEnabled(JsonParser.Feature.ALLOW_COMMENTS, r.getFactory()));
    }

    @Test
    public void testWithAndWithoutFormatFeatures() {
        FormatFeature dummyFeature = new FormatFeature() {
            @Override
            public boolean enabledByDefault() { return false; }
            @Override
            public int getMask() { return 1; }
            @Override
            public boolean enabledIn(int flags) { return (flags & 1) != 0; }
        };
        ObjectReader r = reader.with(dummyFeature);
        Assert.assertNotNull(r);
        r = r.without(dummyFeature);
        Assert.assertNotNull(r);

        r = reader.withFeatures(dummyFeature);
        Assert.assertNotNull(r);
        r = r.withoutFeatures(dummyFeature);
        Assert.assertNotNull(r);
    }

    @Test
    public void testWithRootNameConfigurations() throws Exception {
        ObjectReader r = reader.forType(Map.class).withRootName("root");
        Assert.assertNotNull(r);
        r = reader.withRootName(PropertyName.construct("rootProp"));
        Assert.assertNotNull(r);
        r = reader.withoutRootName();
        Assert.assertNotNull(r);

        ObjectReader wrappedReader = reader.forType(Map.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName("data");
        Map<?, ?> result = wrappedReader.readValue("{\"data\":{\"a\":1}}");
        Assert.assertEquals(1, result.get("a"));
    }

    @Test(expected = JsonMappingException.class)
    public void testUnwrapRootFailureWrongToken() throws Exception {
        ObjectReader wrappedReader = reader.forType(Map.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName("data");
        wrappedReader.readValue("[1, 2]");
    }

    @Test(expected = JsonMappingException.class)
    public void testUnwrapRootFailureWrongName() throws Exception {
        ObjectReader wrappedReader = reader.forType(Map.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName("expectedRoot");
        wrappedReader.readValue("{\"actualRoot\":{}}");
    }

    @Test
    public void testTypeConfigurations() {
        JavaType mapType = mapper.getTypeFactory().constructType(Map.class);
        ObjectReader r = reader.forType(mapType);
        Assert.assertSame(r, r.forType(mapType));

        ObjectReader rClass = reader.forType(String.class);
        Assert.assertNotNull(rClass);

        ObjectReader rTypeRef = reader.forType(new TypeReference<List<String>>() {});
        Assert.assertNotNull(rTypeRef);

        ObjectReader dep1 = reader.withType(mapType);
        ObjectReader dep2 = reader.withType(String.class);
        ObjectReader dep3 = reader.withType((java.lang.reflect.Type) Integer.class);
        ObjectReader dep4 = reader.withType(new TypeReference<List<Integer>>() {});
        Assert.assertNotNull(dep1);
        Assert.assertNotNull(dep2);
        Assert.assertNotNull(dep3);
        Assert.assertNotNull(dep4);
    }

    @Test
    public void testAttributesAndContext() {
        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("k", "v");
        ObjectReader r = reader.with(attrs);
        Assert.assertEquals("v", r.getAttributes().getAttribute("k"));

        Map<String, Object> map = new HashMap<>();
        map.put("k2", "v2");
        r = r.withAttributes(map);
        Assert.assertEquals("v2", r.getAttributes().getAttribute("k2"));

        r = r.withAttribute("k3", "v3");
        Assert.assertEquals("v3", r.getAttributes().getAttribute("k3"));

        r = r.withoutAttribute("k3");
        Assert.assertNull(r.getAttributes().getAttribute("k3"));
    }

    @Test
    public void testInjectableValues() throws Exception {
        InjectableValues.Std iv = new InjectableValues.Std();
        iv.addValue("testInj", "hello");
        ObjectReader r = reader.with(iv);
        Assert.assertSame(iv, r.getInjectableValues());
        Assert.assertSame(r, r.with(iv));
    }

    @Test
    public void testMiscConfigurations() {
        JsonNodeFactory nf = new JsonNodeFactory(true);
        ObjectReader r = reader.with(nf);
        Assert.assertSame(nf, r.getConfig().getNodeFactory());

        JsonFactory jf = new JsonFactory();
        r = reader.with(jf);
        Assert.assertSame(jf, r.getFactory());
        Assert.assertSame(r, r.with(jf));

        DeserializationConfig cfg = reader.getConfig();
        Assert.assertSame(reader, reader.with(cfg));

        r = reader.withView(String.class);
        Assert.assertNotNull(r);

        r = reader.with(Locale.GERMANY);
        Assert.assertEquals(Locale.GERMANY, r.getConfig().getLocale());

        r = reader.with(TimeZone.getTimeZone("GMT"));
        Assert.assertEquals(TimeZone.getTimeZone("GMT"), r.getConfig().getTimeZone());

        r = reader.with(Base64Variants.MODIFIED_FOR_URL);
        Assert.assertEquals(Base64Variants.MODIFIED_FOR_URL, r.getConfig().getBase64Variant());

        DeserializationProblemHandler handler = new DeserializationProblemHandler() {};
        r = reader.withHandler(handler);
        Assert.assertNotNull(r);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSchemaTypeValidationFailure() {
        FormatSchema schema = new FormatSchema() {
            @Override
            public String getSchemaType() { return "custom"; }
        };
        reader.with(schema);
    }

    @Test
    public void testSchemaTypeSuccessWhenSame() {
        ObjectReader r = reader.with((FormatSchema) null);
        Assert.assertSame(r, r.with((FormatSchema) null));
    }

    @Test
    public void testWithValueToUpdate() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 1);
        ObjectReader r = reader.withValueToUpdate(map);
        Assert.assertSame(r, r.withValueToUpdate(map));

        Map<?, ?> res = r.readValue("{\"b\":2}");
        Assert.assertSame(map, res);
        Assert.assertEquals(1, map.get("a"));
        Assert.assertEquals(2, map.get("b"));

        ObjectReader cleared = r.withValueToUpdate(null);
        Assert.assertNotNull(cleared);

        ObjectReader typed = reader.forType(Map.class).withValueToUpdate(new HashMap<>());
        Assert.assertNotNull(typed);
    }

    @Test
    public void testAtJsonPointer() throws Exception {
        ObjectReader r = reader.at("/target");
        Assert.assertNotNull(r);
        JsonNode node = r.readTree("{\"target\":{\"value\":123}}");
        Assert.assertEquals(123, node.get("value").asInt());

        JsonPointer ptr = JsonPointer.compile("/target");
        r = reader.at(ptr);
        node = r.readTree("{\"target\":{\"value\":456}}");
        Assert.assertEquals(456, node.get("value").asInt());
    }

    @Test
    public void testTreeCodecOperations() throws Exception {
        ArrayNode arr = reader.createArrayNode();
        Assert.assertNotNull(arr);
        Assert.assertTrue(arr.isArray());

        ObjectNode obj = reader.createObjectNode();
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj.isObject());

        obj.put("num", 42);
        JsonParser p = reader.treeAsTokens(obj);
        Assert.assertNotNull(p);

        Integer val = reader.treeToValue(obj.get("num"), Integer.class);
        Assert.assertEquals(Integer.valueOf(42), val);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteTreeUnsupported() {
        reader.writeTree(null, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWriteValueUnsupported() throws Exception {
        reader.writeValue(null, new Object());
    }

    @Test
    public void testReadValueVariations() throws Exception {
        String json = "{\"key\":\"val\"}";
        JsonParser p = reader.getFactory().createParser(json);

        Map<?, ?> m1 = reader.readValue(p, Map.class);
        Assert.assertEquals("val", m1.get("key"));

        p = reader.getFactory().createParser(json);
        Map<?, ?> m2 = reader.readValue(p, new TypeReference<Map<String, String>>() {});
        Assert.assertEquals("val", m2.get("key"));

        p = reader.getFactory().createParser(json);
        Map<?, ?> m3 = reader.readValue(p, (ResolvedType) mapper.getTypeFactory().constructType(Map.class));
        Assert.assertEquals("val", m3.get("key"));

        p = reader.getFactory().createParser(json);
        Map<?, ?> m4 = reader.readValue(p, mapper.getTypeFactory().constructType(Map.class));
        Assert.assertEquals("val", m4.get("key"));

        p = reader.getFactory().createParser(json);
        JsonNode node = reader.readTree(p);
        Assert.assertEquals("val", node.get("key").asText());

        p = reader.getFactory().createParser("");
        JsonNode emptyNode = reader.readTree(p);
        Assert.assertNotNull(emptyNode);

        p = reader.getFactory().createParser("null");
        JsonNode nullNode = reader.readTree(p);
        Assert.assertTrue(nullNode.isNull());
    }

    @Test
    public void testReadValueFromVariousSources() throws Exception {
        String json = "{\"k\":\"v\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        Map<?, ?> r1 = reader.forType(Map.class).readValue(json);
        Assert.assertEquals("v", r1.get("k"));

        Map<?, ?> r2 = reader.forType(Map.class).readValue(new StringReader(json));
        Assert.assertEquals("v", r2.get("k"));

        Map<?, ?> r3 = reader.forType(Map.class).readValue(new ByteArrayInputStream(bytes));
        Assert.assertEquals("v", r3.get("k"));

        Map<?, ?> r4 = reader.forType(Map.class).readValue(bytes);
        Assert.assertEquals("v", r4.get("k"));

        Map<?, ?> r5 = reader.forType(Map.class).readValue(bytes, 0, bytes.length);
        Assert.assertEquals("v", r5.get("k"));

        JsonNode tree = mapper.readTree(json);
        Map<?, ?> r6 = reader.forType(Map.class).readValue(tree);
        Assert.assertEquals("v", r6.get("k"));

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        Map<?, ?> r7 = reader.forType(Map.class).readValue((DataInput) dis);
        Assert.assertEquals("v", r7.get("k"));

        File temp = File.createTempFile("jackson-test", ".json");
        temp.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write(bytes);
        }
        Map<?, ?> r8 = reader.forType(Map.class).readValue(temp);
        Assert.assertEquals("v", r8.get("k"));

        URL url = temp.toURI().toURL();
        Map<?, ?> r9 = reader.forType(Map.class).readValue(url);
        Assert.assertEquals("v", r9.get("k"));
    }

    @Test
    public void testReadTreeFromVariousSources() throws Exception {
        String json = "{\"a\":123}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        Assert.assertEquals(123, reader.readTree(json).get("a").asInt());
        Assert.assertEquals(123, reader.readTree(new StringReader(json)).get("a").asInt());
        Assert.assertEquals(123, reader.readTree(new ByteArrayInputStream(bytes)).get("a").asInt());
        Assert.assertEquals(123, reader.readTree(bytes).get("a").asInt());
        Assert.assertEquals(123, reader.readTree(bytes, 0, bytes.length).get("a").asInt());
        Assert.assertEquals(123, reader.readTree((DataInput) new DataInputStream(new ByteArrayInputStream(bytes))).get("a").asInt());
    }

    @Test
    public void testReadValuesSequence() throws Exception {
        String seq = "{\"a\":1} {\"a\":2}";
        byte[] bytes = seq.getBytes(StandardCharsets.UTF_8);

        MappingIterator<Map<String, Integer>> it = reader.forType(Map.class).readValues(seq);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(Integer.valueOf(1), it.next().get("a"));
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(Integer.valueOf(2), it.next().get("a"));
        Assert.assertFalse(it.hasNext());

        it = reader.forType(Map.class).readValues(new StringReader(seq));
        Assert.assertEquals(Integer.valueOf(1), it.next().get("a"));

        it = reader.forType(Map.class).readValues(new ByteArrayInputStream(bytes));
        Assert.assertEquals(Integer.valueOf(1), it.next().get("a"));

        it = reader.forType(Map.class).readValues(bytes);
        Assert.assertEquals(Integer.valueOf(1), it.next().get("a"));

        it = reader.forType(Map.class).readValues(bytes, 0, bytes.length);
        Assert.assertEquals(Integer.valueOf(1), it.next().get("a"));

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        it = reader.forType(Map.class).readValues((DataInput) dis);
        Assert.assertEquals(Integer.valueOf(1), it.next().get("a"));

        File temp = File.createTempFile("jackson-seq", ".json");
        temp.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write(bytes);
        }
        it = reader.forType(Map.class).readValues(temp);
        Assert.assertEquals(Integer.valueOf(1), it.next().get("a"));

        it = reader.forType(Map.class).readValues(temp.toURI().toURL());
        Assert.assertEquals(Integer.valueOf(1), it.next().get("a"));
    }

    @Test
    public void testReadValuesWithJsonParserVariants() throws Exception {
        String json = "1 2 3";
        JsonParser p = reader.getFactory().createParser(json);
        Iterator<Integer> it1 = reader.readValues(p, Integer.class);
        Assert.assertEquals(Integer.valueOf(1), it1.next());

        p = reader.getFactory().createParser(json);
        Iterator<Integer> it2 = reader.readValues(p, new TypeReference<Integer>() {});
        Assert.assertEquals(Integer.valueOf(1), it2.next());

        p = reader.getFactory().createParser(json);
        Iterator<Integer> it3 = reader.readValues(p, (ResolvedType) mapper.getTypeFactory().constructType(Integer.class));
        Assert.assertEquals(Integer.valueOf(1), it3.next());

        p = reader.getFactory().createParser(json);
        Iterator<Integer> it4 = reader.readValues(p, mapper.getTypeFactory().constructType(Integer.class));
        Assert.assertEquals(Integer.valueOf(1), it4.next());
    }

    @Test(expected = JsonParseException.class)
    public void testFormatDetectionCharSourceThrowsException() throws Exception {
        ObjectReader r = reader.withFormatDetection(reader.forType(Map.class));
        r.readValue("{\"a\":1}");
    }

    @Test(expected = JsonParseException.class)
    public void testFormatDetectionReaderSourceThrowsException() throws Exception {
        ObjectReader r = reader.withFormatDetection(reader.forType(Map.class));
        r.readValue(new StringReader("{\"a\":1}"));
    }

    @Test(expected = JsonParseException.class)
    public void testFormatDetectionDataInputThrowsException() throws Exception {
        ObjectReader r = reader.withFormatDetection(reader.forType(Map.class));
        r.readValue((DataInput) new DataInputStream(new ByteArrayInputStream(new byte[0])));
    }

    @Test
    public void testFormatDetectionWithByteSources() throws Exception {
        ObjectReader r1 = mapper.readerFor(Map.class);
        ObjectReader detector = reader.withFormatDetection(r1);

        byte[] json = "{\"x\":9}".getBytes(StandardCharsets.UTF_8);
        Map<?, ?> res = detector.readValue(json);
        Assert.assertEquals(9, res.get("x"));

        res = detector.readValue(new ByteArrayInputStream(json));
        Assert.assertEquals(9, res.get("x"));

        JsonNode node = detector.readTree(new ByteArrayInputStream(json));
        Assert.assertEquals(9, node.get("x").asInt());

        File temp = File.createTempFile("jackson-fd", ".json");
        temp.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(temp)) {
            fos.write(json);
        }
        res = detector.readValue(temp);
        Assert.assertEquals(9, res.get("x"));

        res = detector.readValue(temp.toURI().toURL());
        Assert.assertEquals(9, res.get("x"));

        detector = detector.forType(Map.class);
        MappingIterator<Map<String, Object>> it = detector.readValues(json);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(9, it.next().get("x"));

        it = detector.readValues(new ByteArrayInputStream(json));
        Assert.assertEquals(9, it.next().get("x"));

        it = detector.readValues(temp);
        Assert.assertEquals(9, it.next().get("x"));

        it = detector.readValues(temp.toURI().toURL());
        Assert.assertEquals(9, it.next().get("x"));
    }

    @Test(expected = JsonParseException.class)
    public void testFormatDetectionUnrecognizedFormat() throws Exception {
        DataFormatReaders dfr = new DataFormatReaders(reader.forType(Map.class)).withMinimalMatch(MatchStrength.FULL_MATCH);
        ObjectReader detector = reader.withFormatDetection(dfr);
        detector.readValue(new byte[]{0, 0, 0, 0});
    }

    @Test(expected = JsonParseException.class)
    public void testTrailingTokensDetection() throws Exception {
        ObjectReader r = reader.forType(Integer.class).with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        r.readValue("123 456");
    }

    @Test(expected = JsonMappingException.class)
    public void testNoContentThrowsMappingException() throws Exception {
        reader.forType(Map.class).readValue("");
    }

    @Test
    public void testNullTokenValueBinding() throws Exception {
        String res = reader.forType(String.class).readValue("null");
        Assert.assertNull(res);

        Map<String, Object> map = new HashMap<>();
        Map<?, ?> updated = reader.withValueToUpdate(map).readValue("null");
        Assert.assertSame(map, updated);
    }
}
