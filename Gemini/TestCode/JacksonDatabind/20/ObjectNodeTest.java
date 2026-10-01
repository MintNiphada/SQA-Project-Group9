package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

public class ObjectNodeTest {

    private JsonNodeFactory factory;
    private ObjectNode objectNode;

    @Before
    public void setUp() {
        factory = JsonNodeFactory.instance;
        objectNode = new ObjectNode(factory);
    }

    @Test
    public void testConstructors() {
        assertNotNull(objectNode);
        assertEquals(0, objectNode.size());

        Map<String, JsonNode> kids = new LinkedHashMap<String, JsonNode>();
        kids.put("k1", factory.textNode("v1"));
        ObjectNode customNode = new ObjectNode(factory, kids);
        assertEquals(1, customNode.size());
        assertEquals("v1", customNode.get("k1").asText());
    }

    @Test
    public void testNodeTypeAndToken() {
        assertEquals(JsonNodeType.OBJECT, objectNode.getNodeType());
        assertEquals(JsonToken.START_OBJECT, objectNode.asToken());
    }

    @Test
    public void testGetAndPath() {
        objectNode.put("name", "test");

        assertNull(objectNode.get(0));
        assertNull(objectNode.get(10));
        assertEquals("test", objectNode.get("name").asText());
        assertNull(objectNode.get("nonExisting"));

        assertTrue(objectNode.path(0).isMissingNode());
        assertTrue(objectNode.path(1).isMissingNode());
        assertEquals("test", objectNode.path("name").asText());
        assertTrue(objectNode.path("nonExisting").isMissingNode());
    }

    @Test
    public void testAtPointer() {
        objectNode.put("a", "b");
        JsonPointer ptr = JsonPointer.compile("/a");
        JsonNode result = objectNode._at(ptr);
        assertNotNull(result);
        assertEquals("b", result.asText());
    }

    @Test
    public void testDeepCopy() {
        objectNode.put("str", "value");
        ObjectNode child = objectNode.putObject("child");
        child.put("num", 100);

        ObjectNode copy = objectNode.deepCopy();
        assertNotSame(objectNode, copy);
        assertEquals(objectNode, copy);
        assertNotSame(objectNode.get("child"), copy.get("child"));

        copy.put("str", "changed");
        assertFalse(objectNode.get("str").asText().equals(copy.get("str").asText()));
    }

    @Test
    public void testElementsFieldNamesAndFields() {
        objectNode.put("k1", "v1");
        objectNode.put("k2", "v2");

        Iterator<JsonNode> elements = objectNode.elements();
        assertTrue(elements.hasNext());
        assertEquals("v1", elements.next().asText());
        assertTrue(elements.hasNext());
        assertEquals("v2", elements.next().asText());
        assertFalse(elements.hasNext());

        Iterator<String> fieldNames = objectNode.fieldNames();
        assertTrue(fieldNames.hasNext());
        assertEquals("k1", fieldNames.next());
        assertTrue(fieldNames.hasNext());
        assertEquals("k2", fieldNames.next());
        assertFalse(fieldNames.hasNext());

        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        assertTrue(fields.hasNext());
        Map.Entry<String, JsonNode> entry1 = fields.next();
        assertEquals("k1", entry1.getKey());
        assertEquals("v1", entry1.getValue().asText());
        assertTrue(fields.hasNext());
        Map.Entry<String, JsonNode> entry2 = fields.next();
        assertEquals("k2", entry2.getKey());
        assertEquals("v2", entry2.getValue().asText());
        assertFalse(fields.hasNext());
    }

    @Test
    public void testWith() {
        ObjectNode sub = objectNode.with("sub");
        assertNotNull(sub);
        assertSame(sub, objectNode.get("sub"));

        ObjectNode sameSub = objectNode.with("sub");
        assertSame(sub, sameSub);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWithNonObjectNodeThrows() {
        objectNode.put("num", 123);
        objectNode.with("num");
    }

    @Test
    public void testWithArray() {
        ArrayNode arr = objectNode.withArray("arr");
        assertNotNull(arr);
        assertSame(arr, objectNode.get("arr"));

        ArrayNode sameArr = objectNode.withArray("arr");
        assertSame(arr, sameArr);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testWithArrayNonArrayNodeThrows() {
        objectNode.put("text", "val");
        objectNode.withArray("text");
    }

    @Test
    public void testFindValue() {
        assertNull(objectNode.findValue("target"));

        objectNode.put("a", "1");
        ObjectNode child = objectNode.putObject("child");
        child.put("target", "found");

        JsonNode directMatch = objectNode.findValue("a");
        assertNotNull(directMatch);
        assertEquals("1", directMatch.asText());

        JsonNode deepMatch = objectNode.findValue("target");
        assertNotNull(deepMatch);
        assertEquals("found", deepMatch.asText());

        assertNull(objectNode.findValue("missing"));
    }

    @Test
    public void testFindValues() {
        objectNode.put("target", "val1");
        ObjectNode child = objectNode.putObject("child");
        child.put("target", "val2");
        child.put("other", "otherVal");

        List<JsonNode> result = objectNode.findValues("target", null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("val1", result.get(0).asText());

        List<JsonNode> existingList = new ArrayList<JsonNode>();
        List<JsonNode> deepResult = objectNode.findValues("other", existingList);
        assertSame(existingList, deepResult);
        assertEquals(1, deepResult.size());
        assertEquals("otherVal", deepResult.get(0).asText());
    }

    @Test
    public void testFindValuesAsText() {
        objectNode.put("target", "text1");
        ObjectNode child = objectNode.putObject("child");
        child.put("target", "text2");
        child.put("other", "textOther");

        List<String> result = objectNode.findValuesAsText("target", null);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("text1", result.get(0));

        List<String> existing = new ArrayList<String>();
        List<String> deepResult = objectNode.findValuesAsText("other", existing);
        assertSame(existing, deepResult);
        assertEquals(1, deepResult.size());
        assertEquals("textOther", deepResult.get(0));
    }

    @Test
    public void testFindParentAndFindParents() {
        assertNull(objectNode.findParent("key"));

        objectNode.put("topKey", "topVal");
        ObjectNode sub = objectNode.putObject("sub");
        sub.put("nestedKey", "nestedVal");

        assertSame(objectNode, objectNode.findParent("topKey"));
        assertSame(sub, objectNode.findParent("nestedKey"));
        assertNull(objectNode.findParent("nonExistent"));

        List<JsonNode> parents = objectNode.findParents("topKey", null);
        assertEquals(1, parents.size());
        assertSame(objectNode, parents.get(0));

        List<JsonNode> existing = new ArrayList<JsonNode>();
        List<JsonNode> nestedParents = objectNode.findParents("nestedKey", existing);
        assertSame(existing, nestedParents);
        assertEquals(1, nestedParents.size());
        assertSame(sub, nestedParents.get(0));
    }

    @Test
    public void testSetAndSetAll() {
        JsonNode returned = objectNode.set("field1", factory.textNode("value1"));
        assertSame(objectNode, returned);
        assertEquals("value1", objectNode.get("field1").asText());

        objectNode.set("nullField", null);
        assertTrue(objectNode.get("nullField").isNull());

        Map<String, JsonNode> map = new LinkedHashMap<String, JsonNode>();
        map.put("m1", factory.numberNode(10));
        map.put("m2", null);
        returned = objectNode.setAll(map);
        assertSame(objectNode, returned);
        assertEquals(10, objectNode.get("m1").asInt());
        assertTrue(objectNode.get("m2").isNull());

        ObjectNode other = factory.objectNode();
        other.put("o1", "otherVal");
        returned = objectNode.setAll(other);
        assertSame(objectNode, returned);
        assertEquals("otherVal", objectNode.get("o1").asText());
    }

    @Test
    public void testReplace() {
        assertNull(objectNode.replace("a", factory.textNode("initial")));
        assertEquals("initial", objectNode.get("a").asText());

        JsonNode old = objectNode.replace("a", factory.textNode("updated"));
        assertNotNull(old);
        assertEquals("initial", old.asText());
        assertEquals("updated", objectNode.get("a").asText());

        JsonNode oldNull = objectNode.replace("a", null);
        assertEquals("updated", oldNull.asText());
        assertTrue(objectNode.get("a").isNull());
    }

    @Test
    public void testWithoutAndRemove() {
        objectNode.put("k1", 1);
        objectNode.put("k2", 2);
        objectNode.put("k3", 3);
        objectNode.put("k4", 4);

        assertSame(objectNode, objectNode.without("k1"));
        assertNull(objectNode.get("k1"));

        assertSame(objectNode, objectNode.without(Arrays.asList("k2", "nonExistent")));
        assertNull(objectNode.get("k2"));
        assertEquals(2, objectNode.size());

        JsonNode removed = objectNode.remove("k3");
        assertNotNull(removed);
        assertEquals(3, removed.asInt());
        assertNull(objectNode.remove("k3"));

        assertSame(objectNode, objectNode.remove(Arrays.asList("k4")));
        assertNull(objectNode.get("k4"));
        assertEquals(0, objectNode.size());

        objectNode.put("k5", 5);
        assertSame(objectNode, objectNode.removeAll());
        assertEquals(0, objectNode.size());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedPutAndPutAll() {
        assertNull(objectNode.put("d1", factory.textNode("v1")));
        JsonNode prev = objectNode.put("d1", factory.textNode("v2"));
        assertEquals("v1", prev.asText());

        JsonNode prevNull = objectNode.put("d1", (JsonNode) null);
        assertEquals("v2", prevNull.asText());
        assertTrue(objectNode.get("d1").isNull());

        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        map.put("p1", factory.numberNode(123));
        objectNode.putAll(map);
        assertEquals(123, objectNode.get("p1").asInt());

        ObjectNode other = factory.objectNode();
        other.put("p2", "hello");
        objectNode.putAll(other);
        assertEquals("hello", objectNode.get("p2").asText());
    }

    @Test
    public void testRetain() {
        objectNode.put("k1", 1);
        objectNode.put("k2", 2);
        objectNode.put("k3", 3);

        assertSame(objectNode, objectNode.retain(Arrays.asList("k1", "k2")));
        assertEquals(2, objectNode.size());
        assertNotNull(objectNode.get("k1"));
        assertNotNull(objectNode.get("k2"));
        assertNull(objectNode.get("k3"));

        assertSame(objectNode, objectNode.retain("k1"));
        assertEquals(1, objectNode.size());
        assertNotNull(objectNode.get("k1"));
        assertNull(objectNode.get("k2"));
    }

    @Test
    public void testPutPrimitivesAndWrappers() {
        objectNode.put("shortPrimitive", (short) 1);
        assertEquals(1, objectNode.get("shortPrimitive").shortValue());

        objectNode.put("shortObject", Short.valueOf((short) 2));
        assertEquals(2, objectNode.get("shortObject").shortValue());
        objectNode.put("shortNull", (Short) null);
        assertTrue(objectNode.get("shortNull").isNull());

        objectNode.put("intPrimitive", 10);
        assertEquals(10, objectNode.get("intPrimitive").intValue());

        objectNode.put("intObject", Integer.valueOf(20));
        assertEquals(20, objectNode.get("intObject").intValue());
        objectNode.put("intNull", (Integer) null);
        assertTrue(objectNode.get("intNull").isNull());

        objectNode.put("longPrimitive", 100L);
        assertEquals(100L, objectNode.get("longPrimitive").longValue());

        objectNode.put("longObject", Long.valueOf(200L));
        assertEquals(200L, objectNode.get("longObject").longValue());
        objectNode.put("longNull", (Long) null);
        assertTrue(objectNode.get("longNull").isNull());

        objectNode.put("floatPrimitive", 1.5f);
        assertEquals(1.5f, objectNode.get("floatPrimitive").floatValue(), 0.0001f);

        objectNode.put("floatObject", Float.valueOf(2.5f));
        assertEquals(2.5f, objectNode.get("floatObject").floatValue(), 0.0001f);
        objectNode.put("floatNull", (Float) null);
        assertTrue(objectNode.get("floatNull").isNull());

        objectNode.put("doublePrimitive", 3.1415);
        assertEquals(3.1415, objectNode.get("doublePrimitive").doubleValue(), 0.00001);

        objectNode.put("doubleObject", Double.valueOf(6.283));
        assertEquals(6.283, objectNode.get("doubleObject").doubleValue(), 0.00001);
        objectNode.put("doubleNull", (Double) null);
        assertTrue(objectNode.get("doubleNull").isNull());

        BigDecimal bd = new BigDecimal("12345.6789");
        objectNode.put("bd", bd);
        assertEquals(bd, objectNode.get("bd").decimalValue());
        objectNode.put("bdNull", (BigDecimal) null);
        assertTrue(objectNode.get("bdNull").isNull());

        objectNode.put("str", "stringVal");
        assertEquals("stringVal", objectNode.get("str").textValue());
        objectNode.put("strNull", (String) null);
        assertTrue(objectNode.get("strNull").isNull());

        objectNode.put("boolPrimitive", true);
        assertTrue(objectNode.get("boolPrimitive").booleanValue());

        objectNode.put("boolObject", Boolean.FALSE);
        assertFalse(objectNode.get("boolObject").booleanValue());
        objectNode.put("boolNull", (Boolean) null);
        assertTrue(objectNode.get("boolNull").isNull());

        byte[] bytes = new byte[]{1, 2, 3};
        objectNode.put("binary", bytes);
        assertArrayEquals(bytes, objectNode.get("binary").binaryValue());
        objectNode.put("binaryNull", (byte[]) null);
        assertTrue(objectNode.get("binaryNull").isNull());

        objectNode.putNull("explicitNull");
        assertTrue(objectNode.get("explicitNull").isNull());

        Object pojo = new Date(0L);
        objectNode.putPOJO("pojo", pojo);
        assertEquals(pojo, ((POJONode) objectNode.get("pojo")).getPojo());

        ArrayNode arr = objectNode.putArray("arr");
        assertNotNull(arr);
        assertSame(arr, objectNode.get("arr"));

        ObjectNode obj = objectNode.putObject("obj");
        assertNotNull(obj);
        assertSame(obj, objectNode.get("obj"));
    }

    @Test
    public void testEqualsAndHashCode() {
        ObjectNode n1 = factory.objectNode();
        ObjectNode n2 = factory.objectNode();

        assertTrue(n1.equals(n1));
        assertFalse(n1.equals(null));
        assertFalse(n1.equals("string"));

        assertTrue(n1.equals(n2));
        assertEquals(n1.hashCode(), n2.hashCode());

        n1.put("a", 1);
        assertFalse(n1.equals(n2));
        assertFalse(n1.hashCode() == n2.hashCode());

        n2.put("a", 1);
        assertTrue(n1.equals(n2));
        assertEquals(n1.hashCode(), n2.hashCode());

        n1.put("b", 2);
        n2.put("b", 3);
        assertFalse(n1.equals(n2));
    }

    @Test
    public void testToString() {
        assertEquals("{}", objectNode.toString());

        objectNode.put("a", 1);
        assertEquals("{\"a\":1}", objectNode.toString());

        objectNode.put("b", "val");
        assertEquals("{\"a\":1,\"b\":\"val\"}", objectNode.toString());
    }

    @Test
    public void testSerialization() throws IOException {
        objectNode.put("k1", "v1");
        objectNode.put("k2", 2);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider provider = new ObjectMapper().getSerializerProvider();

        objectNode.serialize(gen, provider);
        gen.flush();

        assertEquals("{\"k1\":\"v1\",\"k2\":2}", sw.toString());
    }

    @Test
    public void testSerializeWithType() throws IOException {
        objectNode.put("x", 10);

        StringWriter sw = new StringWriter();
        final JsonGenerator gen = new JsonFactory().createGenerator(sw);
        final SerializerProvider provider = new ObjectMapper().getSerializerProvider();

        TypeSerializer typeSerializer = new TypeSerializer() {
            @Override
            public TypeSerializer forProperty(com.fasterxml.jackson.databind.BeanProperty prop) {
                return this;
            }

            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() {
                return com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_OBJECT;
            }

            @Override
            public String getPropertyName() {
                return "@type";
            }

            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() {
                return null;
            }

            @Override
            public void writeTypePrefixForObject(Object value, JsonGenerator jgen) throws IOException {
                jgen.writeStartObject();
                jgen.writeStringField("@type", "myType");
            }

            @Override
            public void writeTypePrefixForArray(Object value, JsonGenerator jgen) throws IOException {}

            @Override
            public void writeTypePrefixForScalar(Object value, JsonGenerator jgen) throws IOException {}

            @Override
            public void writeTypeSuffixForObject(Object value, JsonGenerator jgen) throws IOException {
                jgen.writeEndObject();
            }

            @Override
            public void writeTypeSuffixForArray(Object value, JsonGenerator jgen) throws IOException {}

            @Override
            public void writeTypeSuffixForScalar(Object value, JsonGenerator jgen) throws IOException {}

            @Override
            public void writeCustomTypePrefixForObject(Object value, JsonGenerator jgen, String typeId) throws IOException {}

            @Override
            public void writeCustomTypePrefixForArray(Object value, JsonGenerator jgen, String typeId) throws IOException {}

            @Override
            public void writeCustomTypePrefixForScalar(Object value, JsonGenerator jgen, String typeId) throws IOException {}

            @Override
            public void writeCustomTypeSuffixForObject(Object value, JsonGenerator jgen, String typeId) throws IOException {}

            @Override
            public void writeCustomTypeSuffixForArray(Object value, JsonGenerator jgen, String typeId) throws IOException {}

            @Override
            public void writeCustomTypeSuffixForScalar(Object value, JsonGenerator jgen, String typeId) throws IOException {}
        };

        objectNode.serializeWithType(gen, provider, typeSerializer);
        gen.flush();

        assertEquals("{\"@type\":\"myType\",\"x\":10}", sw.toString());
    }
}
