package com.fasterxml.jackson.databind;

import java.io.*;
import java.net.URL;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.SimpleType;

public class ObjectReaderTest {

    public static class SampleBean {
        public int a;
        public String b;

        public SampleBean() {}
        public SampleBean(int a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testVersionAndBasicAccessors() {
        ObjectReader reader = mapper.readerFor(SampleBean.class);
        Assert.assertNotNull(reader.version());
        Assert.assertNotNull(reader.getConfig());
        Assert.assertNotNull(reader.getFactory());
        Assert.assertNotNull(reader.getTypeFactory());
        Assert.assertNotNull(reader.getAttributes());
        Assert.assertNull(reader.getInjectableValues());

        Assert.assertTrue(reader.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assert.assertTrue(reader.isEnabled(MapperFeature.DEFAULT_VIEW_INCLUSION));
        Assert.assertFalse(reader.isEnabled(JsonParser.Feature.ALLOW_COMMENTS));
    }

    @Test
    public void testFluentWithWithoutDeserializationFeatures() {
        ObjectReader reader = mapper.reader();
        ObjectReader r2 = reader.with(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        Assert.assertTrue(r2.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));

        ObjectReader r3 = r2.with(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertTrue(r3.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));
        Assert.assertTrue(r3.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));

        ObjectReader r4 = r3.withFeatures(DeserializationFeature.UNWRAP_ROOT_VALUE);
        Assert.assertTrue(r4.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));

        ObjectReader r5 = r4.without(DeserializationFeature.UNWRAP_ROOT_VALUE);
        Assert.assertFalse(r5.isEnabled(DeserializationFeature.UNWRAP_ROOT_VALUE));

        ObjectReader r6 = r5.without(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        Assert.assertFalse(r6.isEnabled(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT));
        Assert.assertFalse(r6.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT));

        ObjectReader r7 = r6.withoutFeatures(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertFalse(r7.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY));
    }

    @Test
    public void testFluentWithWithoutJsonParserFeatures() {
        ObjectReader reader = mapper.reader();
        ObjectReader r2 = reader.with(JsonParser.Feature.ALLOW_COMMENTS);
        Assert.assertTrue(r2.getConfig().getParserFeatures() != 0);

        ObjectReader r3 = r2.withFeatures(JsonParser.Feature.ALLOW_YAML_COMMENTS, JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        ObjectReader r4 = r3.without(JsonParser.Feature.ALLOW_COMMENTS);
        ObjectReader r5 = r4.without(JsonParser.Feature.ALLOW_YAML_COMMENTS, JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        ObjectReader r6 = r5.withoutFeatures(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        Assert.assertNotNull(r6);
    }

    @Test
    public void testFluentWithWithoutFormatFeatures() {
        ObjectReader reader = mapper.reader();
        FormatFeature dummyFeature = new FormatFeature() {
            @Override
            public boolean enabledByDefault() { return false; }
            @Override
            public int getMask() { return 1; }
            @Override
            public boolean enabledIn(int flags) { return (flags & 1) != 0; }
        };
        ObjectReader r1 = reader.with(dummyFeature);
        ObjectReader r2 = r1.withFeatures(dummyFeature);
        ObjectReader r3 = r2.without(dummyFeature);
        ObjectReader r4 = r3.withoutFeatures(dummyFeature);
        Assert.assertNotNull(r4);
    }

    @Test
    public void testFluentConfigurationMutators() {
        ObjectReader reader = mapper.reader();
        DeserializationConfig cfg = reader.getConfig();
        Assert.assertSame(reader, reader.with(cfg));

        InjectableValues inject = new InjectableValues.Std();
        ObjectReader rInj = reader.with(inject);
        Assert.assertSame(inject, rInj.getInjectableValues());
        Assert.assertSame(rInj, rInj.with(inject));

        JsonNodeFactory nodeFactory = new JsonNodeFactory(true);
        ObjectReader rNode = reader.with(nodeFactory);
        Assert.assertNotNull(rNode);

        JsonFactory jf = new JsonFactory();
        ObjectReader rJf = reader.with(jf);
        Assert.assertSame(jf, rJf.getFactory());
        Assert.assertSame(rJf, rJf.with(jf));

        ObjectReader rRoot1 = reader.withRootName("customRoot");
        ObjectReader rRoot2 = reader.withRootName(PropertyName.construct("customRoot2"));
        ObjectReader rRoot3 = rRoot1.withoutRootName();
        Assert.assertNotNull(rRoot2);
        Assert.assertNotNull(rRoot3);

        ObjectReader rView = reader.withView(String.class);
        ObjectReader rLoc = reader.with(Locale.GERMANY);
        ObjectReader rTz = reader.with(TimeZone.getTimeZone("GMT"));
        ObjectReader rH = reader.withHandler(new DeserializationProblemHandler() {});
        ObjectReader rB64 = reader.with(Base64Variants.MODIFIED_FOR_URL);
        Assert.assertNotNull(rView);
        Assert.assertNotNull(rLoc);
        Assert.assertNotNull(rTz);
        Assert.assertNotNull(rH);
        Assert.assertNotNull(rB64);

        ContextAttributes attrs = ContextAttributes.getEmpty().withSharedAttribute("k", "v");
        ObjectReader rAttr = reader.with(attrs);
        Assert.assertEquals("v", rAttr.getAttributes().getAttribute("k"));

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("k2", "v2");
        ObjectReader rAttrMap = reader.withAttributes(map);
        Assert.assertEquals("v2", rAttrMap.getAttributes().getAttribute("k2"));

        ObjectReader rAttrOne = reader.withAttribute("k3", "v3");
        Assert.assertEquals("v3", rAttrOne.getAttributes().getAttribute("k3"));
        ObjectReader rAttrRem = rAttrOne.withoutAttribute("k3");
        Assert.assertNull(rAttrRem.getAttributes().getAttribute("k3"));
    }

    @Test
    public void testTypeConfigurations() throws Exception {
        ObjectReader reader = mapper.reader();
        JavaType javaType = mapper.constructType(SampleBean.class);

        ObjectReader r1 = reader.forType(javaType);
        Assert.assertSame(r1, r1.forType(javaType));
        ObjectReader r2 = reader.forType(SampleBean.class);
        ObjectReader r3 = reader.forType(new TypeReference<SampleBean>() {});

        Assert.assertNotNull(r1);
        Assert.assertNotNull(r2);
        Assert.assertNotNull(r3);

        @SuppressWarnings("deprecation")
        ObjectReader rDep1 = reader.withType(javaType);
        @SuppressWarnings("deprecation")
        ObjectReader rDep2 = reader.withType(SampleBean.class);
        @SuppressWarnings("deprecation")
        ObjectReader rDep3 = reader.withType((java.lang.reflect.Type) SampleBean.class);
        @SuppressWarnings("deprecation")
        ObjectReader rDep4 = reader.withType(new TypeReference<SampleBean>() {});

        Assert.assertNotNull(rDep1);
        Assert.assertNotNull(rDep2);
        Assert.assertNotNull(rDep3);
        Assert.assertNotNull(rDep4);
    }

    @Test
    public void testWithValueToUpdate() throws Exception {
        SampleBean target = new SampleBean(10, "foo");
        ObjectReader reader = mapper.readerForUpdating(target);
        Assert.assertSame(reader, reader.withValueToUpdate(target));

        String json = "{\"b\":\"bar\"}";
        SampleBean result = reader.readValue(json);
        Assert.assertSame(target, result);
        Assert.assertEquals(10, result.a);
        Assert.assertEquals("bar", result.b);

        SampleBean target2 = new SampleBean(20, "orig");
        ObjectReader reader2 = mapper.reader().forType(SampleBean.class).withValueToUpdate(target2);
        reader2.readValue("{\"a\":30}");
        Assert.assertEquals(30, target2.a);

        try {
            mapper.reader().withValueToUpdate(null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("cat not update null value"));
        }

        try {
            int[] arr = new int[]{1, 2};
            mapper.readerFor(int[].class).withValueToUpdate(arr);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Can not update an array value"));
        }
    }

    @Test
    public void testTreeCodecOperations() throws Exception {
        ObjectReader reader = mapper.reader();
        ArrayNode arr = reader.createArrayNode();
        Assert.assertNotNull(arr);
        Assert.assertTrue(arr.isArray());

        ObjectNode obj = reader.createObjectNode();
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj.isObject());

        obj.put("a", 123);
        obj.put("b", "test");
        JsonParser p = reader.treeAsTokens(obj);
        Assert.assertNotNull(p);

        SampleBean bean = reader.treeToValue(obj, SampleBean.class);
        Assert.assertEquals(123, bean.a);
        Assert.assertEquals("test", bean.b);

        JsonNode readNode = reader.readTree(reader.treeAsTokens(obj));
        Assert.assertEquals(123, readNode.get("a").asInt());

        try {
            reader.writeTree(null, obj);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }

        try {
            reader.writeValue(null, bean);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testReadValueVariations() throws Exception {
        String json = "{\"a\":5,\"b\":\"hello\"}";
        ObjectReader reader = mapper.readerFor(SampleBean.class);

        SampleBean b1 = reader.readValue(json);
        Assert.assertEquals(5, b1.a);

        SampleBean b2 = reader.readValue(new StringReader(json));
        Assert.assertEquals(5, b2.a);

        SampleBean b3 = reader.readValue(new ByteArrayInputStream(json.getBytes("UTF-8")));
        Assert.assertEquals(5, b3.a);

        SampleBean b4 = reader.readValue(json.getBytes("UTF-8"));
        Assert.assertEquals(5, b4.a);

        SampleBean b5 = reader.readValue(json.getBytes("UTF-8"), 0, json.getBytes("UTF-8").length);
        Assert.assertEquals(5, b5.a);

        JsonNode tree = mapper.readTree(json);
        SampleBean b6 = reader.readValue(tree);
        Assert.assertEquals(5, b6.a);

        File tmpFile = File.createTempFile("jackson_test", ".json");
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(json.getBytes("UTF-8"));
        fos.close();

        SampleBean b7 = reader.readValue(tmpFile);
        Assert.assertEquals(5, b7.a);

        URL url = tmpFile.toURI().toURL();
        SampleBean b8 = reader.readValue(url);
        Assert.assertEquals(5, b8.a);
    }

    @Test
    public void testReadValueParserVariations() throws Exception {
        String json = "{\"a\":7,\"b\":\"world\"}";
        ObjectReader reader = mapper.reader();
        JavaType type = mapper.constructType(SampleBean.class);

        JsonParser p1 = mapper.getFactory().createParser(json);
        SampleBean b1 = reader.readValue(p1, SampleBean.class);
        Assert.assertEquals(7, b1.a);

        JsonParser p2 = mapper.getFactory().createParser(json);
        SampleBean b2 = reader.readValue(p2, new TypeReference<SampleBean>() {});
        Assert.assertEquals(7, b2.a);

        JsonParser p3 = mapper.getFactory().createParser(json);
        SampleBean b3 = reader.readValue(p3, (com.fasterxml.jackson.core.type.ResolvedType) type);
        Assert.assertEquals(7, b3.a);

        JsonParser p4 = mapper.getFactory().createParser(json);
        SampleBean b4 = reader.readValue(p4, type);
        Assert.assertEquals(7, b4.a);
    }

    @Test
    public void testReadTreeInputs() throws Exception {
        String json = "{\"a\":99}";
        ObjectReader reader = mapper.reader();

        JsonNode n1 = reader.readTree(json);
        Assert.assertEquals(99, n1.get("a").asInt());

        JsonNode n2 = reader.readTree(new StringReader(json));
        Assert.assertEquals(99, n2.get("a").asInt());

        JsonNode n3 = reader.readTree(new ByteArrayInputStream(json.getBytes("UTF-8")));
        Assert.assertEquals(99, n3.get("a").asInt());

        JsonParser p = mapper.getFactory().createParser(json);
        JsonNode n4 = reader.readTree(p);
        Assert.assertEquals(99, n4.get("a").asInt());
    }

    @Test
    public void testReadValuesSequence() throws Exception {
        String json = "{\"a\":1,\"b\":\"1\"}\n{\"a\":2,\"b\":\"2\"}";
        ObjectReader reader = mapper.readerFor(SampleBean.class);

        MappingIterator<SampleBean> it1 = reader.readValues(json);
        List<SampleBean> list1 = it1.readAll();
        Assert.assertEquals(2, list1.size());
        Assert.assertEquals(1, list1.get(0).a);
        Assert.assertEquals(2, list1.get(1).a);

        MappingIterator<SampleBean> it2 = reader.readValues(new StringReader(json));
        Assert.assertEquals(2, it2.readAll().size());

        MappingIterator<SampleBean> it3 = reader.readValues(new ByteArrayInputStream(json.getBytes("UTF-8")));
        Assert.assertEquals(2, it3.readAll().size());

        MappingIterator<SampleBean> it4 = reader.readValues(json.getBytes("UTF-8"));
        Assert.assertEquals(2, it4.readAll().size());

        MappingIterator<SampleBean> it5 = reader.readValues(json.getBytes("UTF-8"), 0, json.getBytes("UTF-8").length);
        Assert.assertEquals(2, it5.readAll().size());

        File tmpFile = File.createTempFile("jackson_test_seq", ".json");
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(json.getBytes("UTF-8"));
        fos.close();

        MappingIterator<SampleBean> it6 = reader.readValues(tmpFile);
        Assert.assertEquals(2, it6.readAll().size());

        MappingIterator<SampleBean> it7 = reader.readValues(tmpFile.toURI().toURL());
        Assert.assertEquals(2, it7.readAll().size());
    }

    @Test
    public void testReadValuesWithParserTypes() throws Exception {
        String json = "[{\"a\":1},{\"a\":2}]";
        ObjectReader reader = mapper.reader();
        JavaType type = mapper.constructType(SampleBean.class);

        JsonParser p1 = mapper.getFactory().createParser(json);
        p1.nextToken(); // START_ARRAY
        p1.nextToken(); // first object
        Iterator<SampleBean> it1 = reader.readValues(p1, SampleBean.class);
        Assert.assertTrue(it1.hasNext());
        Assert.assertEquals(1, it1.next().a);

        JsonParser p2 = mapper.getFactory().createParser(json);
        p2.nextToken();
        p2.nextToken();
        Iterator<SampleBean> it2 = reader.readValues(p2, new TypeReference<SampleBean>() {});
        Assert.assertEquals(1, it2.next().a);

        JsonParser p3 = mapper.getFactory().createParser(json);
        p3.nextToken();
        p3.nextToken();
        Iterator<SampleBean> it3 = reader.readValues(p3, (com.fasterxml.jackson.core.type.ResolvedType) type);
        Assert.assertEquals(1, it3.next().a);

        JsonParser p4 = mapper.getFactory().createParser(json);
        p4.nextToken();
        p4.nextToken();
        Iterator<SampleBean> it4 = reader.readValues(p4, type);
        Assert.assertEquals(1, it4.next().a);
    }

    @Test
    public void testAtPointer() throws Exception {
        String json = "{\"outer\":{\"inner\":{\"a\":42,\"b\":\"target\"}}}";
        ObjectReader reader = mapper.readerFor(SampleBean.class).at("/outer/inner");
        SampleBean bean = reader.readValue(json);
        Assert.assertEquals(42, bean.a);
        Assert.assertEquals("target", bean.b);

        ObjectReader readerPtr = mapper.readerFor(SampleBean.class).at(JsonPointer.compile("/outer/inner"));
        SampleBean bean2 = readerPtr.readValue(json);
        Assert.assertEquals(42, bean2.a);
    }

    @Test
    public void testFormatAutoDetectionByteBased() throws Exception {
        ObjectReader rJson = mapper.readerFor(SampleBean.class);
        ObjectReader rWithDetect = mapper.readerFor(SampleBean.class).withFormatDetection(rJson);

        String json = "{\"a\":11,\"b\":\"detect\"}";
        SampleBean b1 = rWithDetect.readValue(json.getBytes("UTF-8"));
        Assert.assertEquals(11, b1.a);

        SampleBean b2 = rWithDetect.readValue(new ByteArrayInputStream(json.getBytes("UTF-8")));
        Assert.assertEquals(11, b2.a);

        File tmpFile = File.createTempFile("jackson_detect", ".json");
        tmpFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tmpFile);
        fos.write(json.getBytes("UTF-8"));
        fos.close();

        SampleBean b3 = rWithDetect.readValue(tmpFile);
        Assert.assertEquals(11, b3.a);

        SampleBean b4 = rWithDetect.readValue(tmpFile.toURI().toURL());
        Assert.assertEquals(11, b4.a);

        JsonNode tree = rWithDetect.readTree(new ByteArrayInputStream(json.getBytes("UTF-8")));
        Assert.assertEquals(11, tree.get("a").asInt());

        MappingIterator<SampleBean> it = rWithDetect.readValues(json.getBytes("UTF-8"));
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals(11, it.next().a);

        MappingIterator<SampleBean> it2 = rWithDetect.readValues(new ByteArrayInputStream(json.getBytes("UTF-8")));
        Assert.assertTrue(it2.hasNext());
        Assert.assertEquals(11, it2.next().a);

        MappingIterator<SampleBean> it3 = rWithDetect.readValues(tmpFile);
        Assert.assertTrue(it3.hasNext());
        Assert.assertEquals(11, it3.next().a);

        MappingIterator<SampleBean> it4 = rWithDetect.readValues(tmpFile.toURI().toURL());
        Assert.assertTrue(it4.hasNext());
        Assert.assertEquals(11, it4.next().a);
    }

    @Test
    public void testFormatDetectionCharSourceFails() throws Exception {
        ObjectReader rJson = mapper.readerFor(SampleBean.class);
        ObjectReader rWithDetect = mapper.readerFor(SampleBean.class).withFormatDetection(rJson);

        try {
            rWithDetect.readValue("{\"a\":1}");
            Assert.fail("Expected JsonParseException for String source");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            rWithDetect.readValue(new StringReader("{\"a\":1}"));
            Assert.fail("Expected JsonParseException for Reader source");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            rWithDetect.readTree("{\"a\":1}");
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            rWithDetect.readTree(new StringReader("{\"a\":1}"));
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            rWithDetect.readValues("{\"a\":1}");
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            rWithDetect.readValues(new StringReader("{\"a\":1}"));
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }

        try {
            rWithDetect.readValue(mapper.createObjectNode());
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("must be byte- not char-based"));
        }
    }

    @Test
    public void testRootUnwrapping() throws Exception {
        ObjectReader reader = mapper.readerFor(SampleBean.class)
                .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName("SampleBean");

        String json = "{\"SampleBean\":{\"a\":55,\"b\":\"unwrapped\"}}";
        SampleBean bean = reader.readValue(json);
        Assert.assertEquals(55, bean.a);
        Assert.assertEquals("unwrapped", bean.b);

        JsonNode tree = mapper.reader().with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .withRootName("Root")
                .readTree("{\"Root\":{\"k\":\"v\"}}");
        Assert.assertEquals("v", tree.get("k").asText());

        try {
            reader.readValue("[1,2,3]");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Current token not START_OBJECT"));
        }

        try {
            reader.readValue("{\"WrongRoot\":{\"a\":1}}");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("does not match expected"));
        }

        try {
            reader.readValue("{}");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Current token not FIELD_NAME"));
        }
    }

    @Test
    public void testFormatSchemaVerification() {
        FormatSchema unsupportedSchema = new FormatSchema() {
            @Override
            public String getSchemaType() { return "UNSUPPORTED"; }
        };
        try {
            mapper.reader().with(unsupportedSchema);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Can not use FormatSchema"));
        }

        ObjectReader reader = mapper.reader();
        Assert.assertSame(reader, reader.with((FormatSchema) null));
    }

    @Test
    public void testNullAndEmptyTokenHandling() throws Exception {
        ObjectReader reader = mapper.readerFor(SampleBean.class);

        SampleBean bean = reader.readValue("null");
        Assert.assertNull(bean);

        SampleBean target = new SampleBean(10, "init");
        SampleBean updated = reader.withValueToUpdate(target).readValue("null");
        Assert.assertSame(target, updated);

        JsonNode nullNode = mapper.reader().readTree("null");
        Assert.assertTrue(nullNode.isNull());

        try {
            mapper.readerFor(SampleBean.class).readValue("");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("No content to map due to end-of-input"));
        }

        try {
            mapper.reader().readValue("   ");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("No content to map due to end-of-input"));
        }
    }

    @Test
    public void testNoValueTypeFindRootDeserializerFails() throws Exception {
        ObjectReader reader = mapper.reader();
        JsonParser p = mapper.getFactory().createParser("{\"a\":1}");
        p.nextToken();
        try {
            reader.readValue(p);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("No value type configured"));
        }
    }

    @Test
    public void testFormatDetectionUnmatched() throws Exception {
        DataFormatReaders detector = new DataFormatReaders(new ObjectReader[0]);
        ObjectReader reader = mapper.readerFor(SampleBean.class).withFormatDetection(detector);
        try {
            reader.readValue(new byte[] { 1, 2, 3 });
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("Can not detect format"));
        }
    }
}
