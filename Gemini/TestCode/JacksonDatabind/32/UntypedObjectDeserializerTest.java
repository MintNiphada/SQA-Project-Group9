package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Assert;
import org.junit.Test;

public class UntypedObjectDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    @Test
    public void testConstructorsAndCachable() {
        UntypedObjectDeserializer deser1 = new UntypedObjectDeserializer();
        Assert.assertTrue(deser1.isCachable());

        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Object.class);
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class);
        UntypedObjectDeserializer deser2 = new UntypedObjectDeserializer(listType, mapType);
        Assert.assertNotNull(deser2);

        UntypedObjectDeserializer deser3 = new UntypedObjectDeserializer(deser2, null, null, null, null);
        Assert.assertNotNull(deser3);

        JsonDeserializer<?> withResolved = deser2._withResolved(null, null, null, null);
        Assert.assertNotNull(withResolved);
        Assert.assertTrue(withResolved instanceof UntypedObjectDeserializer);
    }

    @Test
    public void testVanillaResolutionAndContextual() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) ctxt)
                    .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        }
        deser.resolve(ctxt);
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        Assert.assertSame(UntypedObjectDeserializer.Vanilla.std, contextual);
    }

    @Test
    public void testNonVanillaWithCustomTypes() throws Exception {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(LinkedList.class, Object.class);
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(TreeMap.class, String.class, Object.class);
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(listType, mapType);

        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) ctxt)
                    .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        }
        deser.resolve(ctxt);
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        Assert.assertSame(deser, contextual);
    }

    @Test
    public void testDeserializePrimitivesAndScalars() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);

        Assert.assertEquals("hello", deserializeToken(deser, "\"hello\""));
        Assert.assertEquals(123, deserializeToken(deser, "123"));
        Assert.assertEquals(12.5, (Double) deserializeToken(deser, "12.5"), 0.001);
        Assert.assertEquals(Boolean.TRUE, deserializeToken(deser, "true"));
        Assert.assertEquals(Boolean.FALSE, deserializeToken(deser, "false"));
        Assert.assertNull(deserializeToken(deser, "null"));
    }

    @Test
    public void testDeserializeIntAndFloatCoercions() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);

        ObjectMapper bigIntMapper = new ObjectMapper();
        bigIntMapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        JsonParser p = bigIntMapper.getFactory().createParser("12345678901234567890");
        p.nextToken();
        DeserializationContext ctxt = createCtxt(bigIntMapper, p);
        Object val = deser.deserialize(p, ctxt);
        Assert.assertEquals(new BigInteger("12345678901234567890"), val);

        ObjectMapper bigDecMapper = new ObjectMapper();
        bigDecMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        p = bigDecMapper.getFactory().createParser("123.456");
        p.nextToken();
        ctxt = createCtxt(bigDecMapper, p);
        val = deser.deserialize(p, ctxt);
        Assert.assertEquals(new BigDecimal("123.456"), val);
    }

    @Test
    public void testDeserializeMapsVariousSizes() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);

        Object empty = deserializeToken(deser, "{}");
        Assert.assertTrue(empty instanceof Map);
        Assert.assertTrue(((Map<?, ?>) empty).isEmpty());

        Object single = deserializeToken(deser, "{\"k1\":\"v1\"}");
        Assert.assertTrue(single instanceof Map);
        Map<?, ?> singleMap = (Map<?, ?>) single;
        Assert.assertEquals(1, singleMap.size());
        Assert.assertEquals("v1", singleMap.get("k1"));

        Object two = deserializeToken(deser, "{\"k1\":\"v1\", \"k2\":2}");
        Assert.assertTrue(two instanceof Map);
        Map<?, ?> twoMap = (Map<?, ?>) two;
        Assert.assertEquals(2, twoMap.size());
        Assert.assertEquals(2, twoMap.get("k2"));

        Object three = deserializeToken(deser, "{\"k1\":1, \"k2\":2, \"k3\":3, \"k4\":4}");
        Assert.assertTrue(three instanceof Map);
        Map<?, ?> threeMap = (Map<?, ?>) three;
        Assert.assertEquals(4, threeMap.size());
        Assert.assertEquals(4, threeMap.get("k4"));
    }

    @Test
    public void testDeserializeArraysVariousSizes() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);

        Object empty = deserializeToken(deser, "[]");
        Assert.assertTrue(empty instanceof List);
        Assert.assertTrue(((List<?>) empty).isEmpty());

        Object single = deserializeToken(deser, "[\"a\"]");
        Assert.assertTrue(single instanceof List);
        Assert.assertEquals(1, ((List<?>) single).size());

        Object two = deserializeToken(deser, "[\"a\", \"b\"]");
        Assert.assertTrue(two instanceof List);
        Assert.assertEquals(2, ((List<?>) two).size());

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        Object large = deserializeToken(deser, sb.toString());
        Assert.assertTrue(large instanceof List);
        Assert.assertEquals(20, ((List<?>) large).size());
    }

    @Test
    public void testDeserializeArrayToJavaArray() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);
        ObjectMapper arrMapper = new ObjectMapper();
        arrMapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);

        JsonParser pEmpty = arrMapper.getFactory().createParser("[]");
        pEmpty.nextToken();
        DeserializationContext ctxtEmpty = createCtxt(arrMapper, pEmpty);
        Object resEmpty = deser.deserialize(pEmpty, ctxtEmpty);
        Assert.assertTrue(resEmpty instanceof Object[]);
        Assert.assertEquals(0, ((Object[]) resEmpty).length);

        JsonParser p = arrMapper.getFactory().createParser("[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]");
        p.nextToken();
        DeserializationContext ctxt = createCtxt(arrMapper, p);
        Object res = deser.deserialize(p, ctxt);
        Assert.assertTrue(res instanceof Object[]);
        Assert.assertEquals(15, ((Object[]) res).length);
    }

    @Test
    public void testDeserializeEmbeddedObject() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);
        TokenBuffer tb = new TokenBuffer(null, false);
        Date date = new Date(123456789L);
        tb.writeObject(date);
        JsonParser p = tb.asParser();
        p.nextToken();
        DeserializationContext ctxt = createCtxt(mapper, p);
        Object result = deser.deserialize(p, ctxt);
        Assert.assertEquals(date, result);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeInvalidToken() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken();
        p.nextToken(); // points to END_ARRAY
        DeserializationContext ctxt = createCtxt(mapper, p);
        deser.deserialize(p, ctxt);
    }

    @Test
    public void testCustomDeserializerDelegation() throws Exception {
        final JsonDeserializer<Object> customMapDeser = new StdDeserializer<Object>(Object.class) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "custom_map";
            }
        };
        final JsonDeserializer<Object> customListDeser = new StdDeserializer<Object>(Object.class) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "custom_list";
            }
        };
        final JsonDeserializer<Object> customStringDeser = new StdDeserializer<Object>(Object.class) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "custom_string";
            }
        };
        final JsonDeserializer<Object> customNumDeser = new StdDeserializer<Object>(Object.class) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return 9999;
            }
        };

        UntypedObjectDeserializer base = new UntypedObjectDeserializer(null, null);
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(base, customMapDeser, customListDeser, customStringDeser, customNumDeser);

        Assert.assertEquals("custom_map", deserializeToken(deser, "{\"a\":1}"));
        Assert.assertEquals("custom_list", deserializeToken(deser, "[1,2]"));
        Assert.assertEquals("custom_string", deserializeToken(deser, "\"abc\""));
        Assert.assertEquals(9999, deserializeToken(deser, "123"));
        Assert.assertEquals(9999, deserializeToken(deser, "12.34"));
    }

    @Test
    public void testVanillaDeserializerDirect() throws Exception {
        UntypedObjectDeserializer.Vanilla vanilla = new UntypedObjectDeserializer.Vanilla();

        Assert.assertEquals("text", deserializeVanillaToken(vanilla, mapper, "\"text\""));
        Assert.assertEquals(10, deserializeVanillaToken(vanilla, mapper, "10"));
        Assert.assertEquals(10.5, (Double) deserializeVanillaToken(vanilla, mapper, "10.5"), 0.001);
        Assert.assertEquals(Boolean.TRUE, deserializeVanillaToken(vanilla, mapper, "true"));
        Assert.assertEquals(Boolean.FALSE, deserializeVanillaToken(vanilla, mapper, "false"));
        Assert.assertNull(deserializeVanillaToken(vanilla, mapper, "null"));

        Object map0 = deserializeVanillaToken(vanilla, mapper, "{}");
        Assert.assertTrue(map0 instanceof Map && ((Map<?, ?>) map0).isEmpty());

        Object map1 = deserializeVanillaToken(vanilla, mapper, "{\"k1\":1}");
        Assert.assertEquals(1, ((Map<?, ?>) map1).get("k1"));

        Object map2 = deserializeVanillaToken(vanilla, mapper, "{\"k1\":1, \"k2\":2}");
        Assert.assertEquals(2, ((Map<?, ?>) map2).get("k2"));

        Object map3 = deserializeVanillaToken(vanilla, mapper, "{\"k1\":1, \"k2\":2, \"k3\":3, \"k4\":4}");
        Assert.assertEquals(4, ((Map<?, ?>) map3).size());

        Object arr0 = deserializeVanillaToken(vanilla, mapper, "[]");
        Assert.assertTrue(arr0 instanceof List && ((List<?>) arr0).isEmpty());

        Object arr1 = deserializeVanillaToken(vanilla, mapper, "[100]");
        Assert.assertEquals(1, ((List<?>) arr1).size());

        Object arr2 = deserializeVanillaToken(vanilla, mapper, "[100, 200]");
        Assert.assertEquals(2, ((List<?>) arr2).size());

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 20; i++) {
            if (i > 0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        Object arrMany = deserializeVanillaToken(vanilla, mapper, sb.toString());
        Assert.assertEquals(20, ((List<?>) arrMany).size());

        ObjectMapper arrMapper = new ObjectMapper();
        arrMapper.enable(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
        Object emptyArr = deserializeVanillaToken(vanilla, arrMapper, "[]");
        Assert.assertTrue(emptyArr instanceof Object[] && ((Object[]) emptyArr).length == 0);

        Object fullArr = deserializeVanillaToken(vanilla, arrMapper, "[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]");
        Assert.assertTrue(fullArr instanceof Object[] && ((Object[]) fullArr).length == 15);

        ObjectMapper numMapper = new ObjectMapper();
        numMapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        numMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        Assert.assertEquals(new BigInteger("123"), deserializeVanillaToken(vanilla, numMapper, "123"));
        Assert.assertEquals(new BigDecimal("123.45"), deserializeVanillaToken(vanilla, numMapper, "123.45"));

        TokenBuffer tb = new TokenBuffer(null, false);
        Date d = new Date(123L);
        tb.writeObject(d);
        JsonParser p = tb.asParser();
        p.nextToken();
        Assert.assertEquals(d, vanilla.deserialize(p, createCtxt(mapper, p)));
    }

    @Test(expected = JsonMappingException.class)
    public void testVanillaInvalidToken() throws Exception {
        UntypedObjectDeserializer.Vanilla vanilla = new UntypedObjectDeserializer.Vanilla();
        JsonParser p = mapper.getFactory().createParser("[]");
        p.nextToken();
        p.nextToken();
        DeserializationContext ctxt = createCtxt(mapper, p);
        vanilla.deserialize(p, ctxt);
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        UntypedObjectDeserializer deser = new UntypedObjectDeserializer(null, null);
        UntypedObjectDeserializer.Vanilla vanilla = UntypedObjectDeserializer.Vanilla.std;

        TypeDeserializer typeDeser = new AsPropertyTypeDeserializer(
                TypeFactory.defaultInstance().constructType(Object.class),
                new ClassNameIdResolver(TypeFactory.defaultInstance().constructType(Object.class), TypeFactory.defaultInstance()),
                "@class", false, TypeFactory.defaultInstance().constructType(Object.class));

        JsonParser p = mapper.getFactory().createParser("\"some string\"");
        p.nextToken();
        Assert.assertEquals("some string", deser.deserializeWithType(p, createCtxt(mapper, p), typeDeser));

        p = mapper.getFactory().createParser("\"some string\"");
        p.nextToken();
        Assert.assertEquals("some string", vanilla.deserializeWithType(p, createCtxt(mapper, p), typeDeser));

        p = mapper.getFactory().createParser("123");
        p.nextToken();
        Assert.assertEquals(123, deser.deserializeWithType(p, createCtxt(mapper, p), typeDeser));

        p = mapper.getFactory().createParser("123");
        p.nextToken();
        Assert.assertEquals(123, vanilla.deserializeWithType(p, createCtxt(mapper, p), typeDeser));

        p = mapper.getFactory().createParser("12.5");
        p.nextToken();
        Assert.assertEquals(12.5, ((Double) deser.deserializeWithType(p, createCtxt(mapper, p), typeDeser)).doubleValue(), 0.001);

        p = mapper.getFactory().createParser("12.5");
        p.nextToken();
        Assert.assertEquals(12.5, ((Double) vanilla.deserializeWithType(p, createCtxt(mapper, p), typeDeser)).doubleValue(), 0.001);

        p = mapper.getFactory().createParser("true");
        p.nextToken();
        Assert.assertEquals(Boolean.TRUE, deser.deserializeWithType(p, createCtxt(mapper, p), typeDeser));

        p = mapper.getFactory().createParser("false");
        p.nextToken();
        Assert.assertEquals(Boolean.FALSE, vanilla.deserializeWithType(p, createCtxt(mapper, p), typeDeser));

        p = mapper.getFactory().createParser("null");
        p.nextToken();
        Assert.assertNull(deser.deserializeWithType(p, createCtxt(mapper, p), typeDeser));

        p = mapper.getFactory().createParser("null");
        p.nextToken();
        Assert.assertNull(vanilla.deserializeWithType(p, createCtxt(mapper, p), typeDeser));
    }

    private Object deserializeToken(JsonDeserializer<?> deser, String json) throws Exception {
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = createCtxt(mapper, p);
        return deser.deserialize(p, ctxt);
    }

    private Object deserializeVanillaToken(UntypedObjectDeserializer.Vanilla deser, ObjectMapper om, String json) throws Exception {
        JsonParser p = om.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = createCtxt(om, p);
        return deser.deserialize(p, ctxt);
    }

    private DeserializationContext createCtxt(ObjectMapper om, JsonParser p) {
        DeserializationContext ctxt = om.getDeserializationContext();
        if (ctxt instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            return ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) ctxt)
                    .createInstance(om.getDeserializationConfig(), p, null);
        }
        return ctxt;
    }
}
