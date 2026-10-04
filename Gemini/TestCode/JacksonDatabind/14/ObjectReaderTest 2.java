package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.*;
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
import java.util.*;

public class ObjectReaderTest {

    private ObjectMapper mapper;
    private ObjectReader reader;

    public static class SimpleBean {
        public int a;
        public String b;

        public SimpleBean() {}
        public SimpleBean(int a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    public static class TargetBean {
        public int x;
        public int y;
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        reader = mapper.reader();
    }

    @Test
    public void testVersionAndAccessors() {
        Assert.assertNotNull(reader.version());
        Assert.assertNotNull(reader.getConfig());
        Assert.assertNotNull(reader.getFactory());
        Assert.assertNotNull(reader.getJsonFactory());
        Assert.assertNotNull(reader.getTypeFactory());
        Assert.assertNotNull(reader.getAttributes());
        Assert.assertTrue(reader.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE));
        Assert.assertTrue(reader.isEnabled(MapperFeature.USE_ANNOTATIONS));
        Assert.assertFalse(reader.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
    }

    @Test
    public void testWithAndWithoutDeserializationFeatures() {
        ObjectReader r = reader.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        Assert.assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        r = r.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        Assert.assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        r = reader.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        Assert.assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));

        r = r.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        Assert.assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));

        r = reader.withFeatures(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertTrue(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        r = r.withoutFeatures(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertFalse(r.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
    }

    @Test
    public void testWithAndWithoutJsonParserFeatures() {
        ObjectReader r = reader.with(JsonParser.Feature.ALLOW_COMMENTS);
        Assert.assertTrue(r.getConfig().getParserFeatures() != 0);

        r = reader.withFeatures(JsonParser.Feature.ALLOW_COMMENTS, JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        r = r.without(JsonParser.Feature.ALLOW_COMMENTS);
        r = r.withoutFeatures(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        Assert.assertNotNull(r);
    }

    @Test
    public void testWithConfigurationModifications() {
        DeserializationConfig cfg = reader.getConfig();
        Assert.assertSame(reader, reader.with(cfg));

        DeserializationConfig newCfg = cfg.with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ObjectReader r = reader.with(newCfg);
        Assert.assertNotSame(reader, r);

        InjectableValues inject = new InjectableValues.Std();
        r = reader.with(inject);
        Assert.assertSame(r, r.with(inject));

        JsonNodeFactory nodeFactory = new JsonNodeFactory(true);
        r = reader.with(nodeFactory);
        Assert.assertNotNull(r);

        JsonFactory jf = new JsonFactory();
        r = reader.with(jf);
        Assert.assertSame(r, r.with(jf));

        r = reader.withRootName("rootName");
        Assert.assertNotNull(r);

        r = reader.with(Locale.GERMANY);
        r = r.with(TimeZone.getTimeZone("GMT+1"));
        r = r.with(Base64Variants.MIME_NO_LINEFEEDS);
        r = r.withHandler(new DeserializationProblemHandler() {});
        r = r.withView(String.class);

        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("k", "v");
        r = r.with(attrs);
        r = r.withAttribute("k2", "v2");
        r = r.withoutAttribute("k2");

        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("k3", "v3");
        r = r.withAttributes(map);
        Assert.assertNotNull(r);
    }

    @Test
    public void testForTypeAndWithTypeVariations() throws Exception {
        JavaType javaType = mapper.constructType(SimpleBean.class);
        ObjectReader r1 = reader.forType(SimpleBean.class);
        Assert.assertSame(r1, r1.forType(javaType));
        Assert.assertSame(r1, r1.withType(SimpleBean.class));
        Assert.assertSame(r1, r1.withType(javaType));

        ObjectReader r2 = reader.forType(new TypeReference<SimpleBean>() {});
        Assert.assertNotNull(r2);
        ObjectReader r3 = reader.withType((java.lang.reflect.Type) SimpleBean.class);
        Assert.assertNotNull(r3);
        ObjectReader r4 = reader.withType(new TypeReference<SimpleBean>() {});
        Assert.assertNotNull(r4);
    }

    @Test
    public void testWithValueToUpdate() throws Exception {
        TargetBean target = new TargetBean();
        target.x = 10;
        target.y = 20;

        ObjectReader updatingReader = reader.withValueToUpdate(target);
        Assert.assertSame(updatingReader, updatingReader.withValueToUpdate(target));

        TargetBean updated = updatingReader.readValue("{\"y\":30}");
        Assert.assertSame(target, updated);
        Assert.assertEquals(10, target.x);
        Assert.assertEquals(30, target.y);

        try {
            reader.withValueToUpdate(null);
            Assert.fail("Expected IllegalArgumentException for null value");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("null"));
        }

        try {
            reader.forType(int[].class).withValueToUpdate(new int[]{1, 2});
            Assert.fail("Expected IllegalArgumentException for array type update");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("array"));
        }
    }

    @Test
    public void testTreeCodecOperations() throws Exception {
        ArrayNode arr = reader.createArrayNode();
        Assert.assertNotNull(arr);
        Assert.assertTrue(arr.isArray());

        ObjectNode obj = reader.createObjectNode();
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj.isObject());

        obj.put("a", 123);
        JsonParser p = reader.treeAsTokens(obj);
        Assert.assertNotNull(p);

        JsonNode readNode = reader.readTree(p);
        Assert.assertEquals(123, readNode.get("a").asInt());

        SimpleBean bean = reader.treeToValue(obj, SimpleBean.class);
        Assert.assertEquals(123, bean.a);

        try {
            reader.writeTree(null, obj);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }

        try {
            reader.writeValue((JsonGenerator) null, bean);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testReadValueFromVariousSources() throws Exception {
        String json = "{\"a\":42,\"b\":\"test\"}";
        byte[] bytes = json.getBytes("UTF-8");

        SimpleBean res1 = reader.forType(SimpleBean.class).readValue(json);
        Assert.assertEquals(42, res1.a);
        Assert.assertEquals("test", res1.b);

        SimpleBean res2 = reader.forType(SimpleBean.class).readValue(new StringReader(json));
        Assert.assertEquals(42, res2.a);

        SimpleBean res3 = reader.forType(SimpleBean.class).readValue(new ByteArrayInputStream(bytes));
        Assert.assertEquals(42, res3.a);

        SimpleBean res4 = reader.forType(SimpleBean.class).readValue(bytes);
        Assert.assertEquals(42, res4.a);

        SimpleBean res5 = reader.forType(SimpleBean.class).readValue(bytes, 0, bytes.length);
        Assert.assertEquals(42, res5.a);

        File tmpFile = File.createTempFile("jackson-test", ".json");
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(bytes);
        fos.close();

        SimpleBean res6 = reader.forType(SimpleBean.class).readValue(tmpFile);
        Assert.assertEquals(42, res6.a);

        URL url = tmpFile.toURI().toURL();
        SimpleBean res7 = reader.forType(SimpleBean.class).readValue(url);
        Assert.assertEquals(42, res7.a);

        JsonNode node = mapper.readTree(json);
        SimpleBean res8 = reader.forType(SimpleBean.class).readValue(node);
        Assert.assertEquals(42, res8.a);
    }

    @Test
    public void testReadValueWithTypeParameters() throws Exception {
        String json = "{\"a\":5,\"b\":\"xyz\"}";
        JsonParser p1 = reader.getFactory().createParser(json);
        SimpleBean b1 = reader.readValue(p1, SimpleBean.class);
        Assert.assertEquals(5, b1.a);

        JsonParser p2 = reader.getFactory().createParser(json);
        SimpleBean b2 = reader.readValue(p2, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(5, b2.a);

        JsonParser p3 = reader.getFactory().createParser(json);
        SimpleBean b3 = reader.readValue(p3, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(5, b3.a);

        JsonParser p4 = reader.getFactory().createParser(json);
        SimpleBean b4 = reader.readValue(p4, (com.fasterxml.jackson.core.type.ResolvedType) mapper.constructType(SimpleBean.class));
        Assert.assertEquals(5, b4.a);
    }

    @Test
    public void testReadTreeFromVariousSources() throws Exception {
        String json = "{\"key\":\"val\"}";
        byte[] bytes = json.getBytes("UTF-8");

        JsonNode n1 = reader.readTree(json);
        Assert.assertEquals("val", n1.get("key").asText());

        JsonNode n2 = reader.readTree(new StringReader(json));
        Assert.assertEquals("val", n2.get("key").asText());

        JsonNode n3 = reader.readTree(new ByteArrayInputStream(bytes));
        Assert.assertEquals("val", n3.get("key").asText());
    }

    @Test
    public void testReadValuesSequence() throws Exception {
        String json = "{\"a\":1,\"b\":\"1\"} {\"a\":2,\"b\":\"2\"}";
        byte[] bytes = json.getBytes("UTF-8");

        MappingIterator<SimpleBean> it1 = reader.forType(SimpleBean.class).readValues(json);
        Assert.assertTrue(it1.hasNext());
        Assert.assertEquals(1, it1.next().a);
        Assert.assertTrue(it1.hasNext());
        Assert.assertEquals(2, it1.next().a);
        Assert.assertFalse(it1.hasNext());

        MappingIterator<SimpleBean> it2 = reader.forType(SimpleBean.class).readValues(new StringReader(json));
        Assert.assertEquals(1, it2.next().a);

        MappingIterator<SimpleBean> it3 = reader.forType(SimpleBean.class).readValues(new ByteArrayInputStream(bytes));
        Assert.assertEquals(1, it3.next().a);

        MappingIterator<SimpleBean> it4 = reader.forType(SimpleBean.class).readValues(bytes);
        Assert.assertEquals(1, it4.next().a);

        MappingIterator<SimpleBean> it5 = reader.forType(SimpleBean.class).readValues(bytes, 0, bytes.length);
        Assert.assertEquals(1, it5.next().a);

        File tmpFile = File.createTempFile("jackson-seq", ".json");
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(bytes);
        fos.close();

        MappingIterator<SimpleBean> it6 = reader.forType(SimpleBean.class).readValues(tmpFile);
        Assert.assertEquals(1, it6.next().a);

        MappingIterator<SimpleBean> it7 = reader.forType(SimpleBean.class).readValues(tmpFile.toURI().toURL());
        Assert.assertEquals(1, it7.next().a);

        JsonParser p = reader.getFactory().createParser(json);
        Iterator<SimpleBean> it8 = reader.readValues(p, SimpleBean.class);
        Assert.assertEquals(1, it8.next().a);

        JsonParser p2 = reader.getFactory().createParser(json);
        Iterator<SimpleBean> it9 = reader.readValues(p2, new TypeReference<SimpleBean>() {});
        Assert.assertEquals(1, it9.next().a);

        JsonParser p3 = reader.getFactory().createParser(json);
        Iterator<SimpleBean> it10 = reader.readValues(p3, mapper.constructType(SimpleBean.class));
        Assert.assertEquals(1, it10.next().a);

        JsonParser p4 = reader.getFactory().createParser(json);
        Iterator<SimpleBean> it11 = reader.readValues(p4, (com.fasterxml.jackson.core.type.ResolvedType) mapper.constructType(SimpleBean.class));
        Assert.assertEquals(1, it11.next().a);
    }

    @Test
    public void testRootUnwrapping() throws Exception {
        ObjectReader unwrappingReader = reader.forType(SimpleBean.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE);

        String json = "{\"SimpleBean\":{\"a\":12,\"b\":\"unwrapped\"}}";
        SimpleBean bean = unwrappingReader.readValue(json);
        Assert.assertEquals(12, bean.a);
        Assert.assertEquals("unwrapped", bean.b);

        // Mismatched root name
        String wrongRoot = "{\"WrongName\":{\"a\":12,\"b\":\"unwrapped\"}}";
        try {
            unwrappingReader.readValue(wrongRoot);
            Assert.fail("Expected JsonMappingException for root name mismatch");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Root name 'WrongName' does not match expected"));
        }

        // Not START_OBJECT
        try {
            unwrappingReader.readValue("[1, 2]");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Current token not START_OBJECT"));
        }
    }

    @Test
    public void testFormatDetection() throws Exception {
        ObjectReader jsonReader = mapper.readerFor(SimpleBean.class);
        ObjectReader detector = jsonReader.withFormatDetection(jsonReader);

        String json = "{\"a\":99,\"b\":\"format\"}";
        byte[] bytes = json.getBytes("UTF-8");

        SimpleBean bean = detector.readValue(new ByteArrayInputStream(bytes));
        Assert.assertEquals(99, bean.a);

        SimpleBean bean2 = detector.readValue(bytes);
        Assert.assertEquals(99, bean2.a);

        JsonNode tree = detector.readTree(new ByteArrayInputStream(bytes));
        Assert.assertEquals(99, tree.get("a").asInt());

        File tmpFile = File.createTempFile("jackson-det", ".json");
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(bytes);
        fos.close();

        SimpleBean bean3 = detector.readValue(tmpFile);
        Assert.assertEquals(99, bean3.a);

        SimpleBean bean4 = detector.readValue(tmpFile.toURI().toURL());
        Assert.assertEquals(99, bean4.a);

        MappingIterator<SimpleBean> it = detector.readValues(new ByteArrayInputStream(bytes));
        Assert.assertEquals(99, it.next().a);

        MappingIterator<SimpleBean> it2 = detector.readValues(bytes, 0, bytes.length);
        Assert.assertEquals(99, it2.next().a);

        MappingIterator<SimpleBean> it3 = detector.readValues(tmpFile);
        Assert.assertEquals(99, it3.next().a);

        MappingIterator<SimpleBean> it4 = detector.readValues(tmpFile.toURI().toURL());
        Assert.assertEquals(99, it4.next().a);

        // Undetectable sources error handling
        try {
            detector.readValue(json);
            Assert.fail("Expected JsonParseException for String input with format detection");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readValue(new StringReader(json));
            Assert.fail("Expected JsonParseException for Reader input with format detection");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readValue(mapper.readTree(json));
            Assert.fail("Expected JsonParseException for JsonNode input with format detection");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readTree(json);
            Assert.fail("Expected JsonParseException for String readTree");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readTree(new StringReader(json));
            Assert.fail("Expected JsonParseException for Reader readTree");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readValues(json);
            Assert.fail("Expected JsonParseException for String readValues");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            detector.readValues(new StringReader(json));
            Assert.fail("Expected JsonParseException for Reader readValues");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }
    }

    @Test
    public void testFormatDetectionNoMatch() throws Exception {
        DataFormatReaders dfReaders = new DataFormatReaders(new ObjectReader[0]);
        ObjectReader r = reader.withFormatDetection(dfReaders);
        try {
            r.readValue(new byte[]{1, 2, 3});
            Assert.fail("Expected JsonParseException for unmatched format");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Can not detect format from input"));
        }
    }

    @Test
    public void testNullAndEmptyInputs() throws Exception {
        ObjectReader r = reader.forType(SimpleBean.class);

        SimpleBean nullResult = r.readValue("null");
        Assert.assertNull(nullResult);

        JsonNode nullNode = reader.readTree("null");
        Assert.assertTrue(nullNode.isNull());

        try {
            r.readValue("");
            Assert.fail("Expected JsonMappingException for empty content");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("No content to map due to end-of-input"));
        }
    }

    @Test
    public void testSchemaValidation() {
        FormatSchema schema = new FormatSchema() {
            @Override
            public String getSchemaType() {
                return "UNSUPPORTED";
            }
        };

        try {
            reader.with(schema);
            Assert.fail("Expected IllegalArgumentException for unsupported schema");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Can not use FormatSchema"));
        }

        ObjectReader rSame = reader.with((FormatSchema) null);
        Assert.assertSame(reader, rSame);
    }
}
