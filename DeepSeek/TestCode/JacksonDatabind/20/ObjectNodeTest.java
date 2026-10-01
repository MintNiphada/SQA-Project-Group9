package com.fasterxml.jackson.databind.node;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;

public class ObjectNodeTest
{
    private ObjectNode node;
    private JsonNodeFactory factory;

    @Before
    public void setUp() {
        factory = JsonNodeFactory.instance;
        node = new ObjectNode(factory);
    }

    @Test
    public void testConstructorEmpty() {
        assertEquals(0, node.size());
        assertTrue(node._children.isEmpty());
    }

    @Test
    public void testConstructorWithExistingMap() {
        Map<String, JsonNode> map = new LinkedHashMap<>();
        TextNode child = new TextNode("value");
        map.put("key", child);
        ObjectNode fromMap = new ObjectNode(factory, map);
        assertEquals(1, fromMap.size());
        assertSame(child, fromMap.get("key"));
    }

    @Test
    public void testDeepCopy() {
        node.put("a", "1");
        ObjectNode copy = node.deepCopy();
        assertEquals(node, copy);
        assertNotSame(node, copy);
        assertNotSame(node.get("a"), copy.get("a"));
    }

    @Test
    public void testGetNodeType() {
        assertEquals(JsonNodeType.OBJECT, node.getNodeType());
    }

    @Test
    public void testAsToken() {
        assertEquals(JsonToken.START_OBJECT, node.asToken());
    }

    @Test
    public void testSize() {
        assertEquals(0, node.size());
        node.put("key", 1);
        assertEquals(1, node.size());
    }

    @Test
    public void testElements() {
        node.put("first", 1).put("second", 2);
        Iterator<JsonNode> it = node.elements();
        assertTrue(it.hasNext());
        assertEquals(IntNode.valueOf(1), it.next());
        assertEquals(IntNode.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetByIndex() {
        assertNull(node.get(0));
    }

    @Test
    public void testGetByFieldName() {
        JsonNode val = new IntNode(42);
        node.set("exist", val);
        assertSame(val, node.get("exist"));
        assertNull(node.get("nonexistent"));
    }

    @Test
    public void testFieldNames() {
        node.put("first", 1).put("second", 2);
        Iterator<String> it = node.fieldNames();
        assertEquals("first", it.next());
        assertEquals("second", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testPathInt() {
        assertSame(MissingNode.getInstance(), node.path(0));
    }

    @Test
    public void testPathString() {
        node.put("foo", "bar");
        assertEquals(TextNode.valueOf("bar"), node.path("foo"));
        assertSame(MissingNode.getInstance(), node.path("missing"));
    }

    @Test
    public void testFields() {
        node.put("key", 10);
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        assertTrue(it.hasNext());
        Map.Entry<String, JsonNode> entry = it.next();
        assertEquals("key", entry.getKey());
        assertEquals(IntNode.valueOf(10), entry.getValue());
        assertFalse(it.hasNext());
    }

    @Test
    public void testWithExistingObjectNode() {
        ObjectNode child = node.putObject("child");
        child.put("x", 1);
        ObjectNode result = node.with("child");
        assertSame(child, result);
        assertEquals(1, result.get("x").asInt());
    }

    @Test
    public void testWithExistingNonObjectNodeShouldThrow() {
        node.put("bad", 42);
        try {
            node.with("bad");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("ObjectNode"));
        }
    }

    @Test
    public void testWithNonexistentCreatesNew() {
        ObjectNode created = node.with("new");
        assertNotNull(created);
        assertSame(created, node.get("new"));
        assertTrue(created instanceof ObjectNode);
        assertEquals(0, created.size());
    }

    @Test
    public void testWithArrayExistingArrayNode() {
        ArrayNode child = node.putArray("arr");
        child.add(5);
        ArrayNode result = node.withArray("arr");
        assertSame(child, result);
        assertEquals(1, result.size());
    }

    @Test
    public void testWithArrayExistingNonArrayNodeShouldThrow() {
        node.put("bad", "text");
        try {
            node.withArray("bad");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("ArrayNode"));
        }
    }

    @Test
    public void testWithArrayNonexistentCreatesNew() {
        ArrayNode created = node.withArray("newArr");
        assertNotNull(created);
        assertSame(created, node.get("newArr"));
        assertTrue(created instanceof ArrayNode);
        assertEquals(0, created.size());
    }

    @Test
    public void testFindValueDirect() {
        node.put("a", 1);
        JsonNode found = node.findValue("a");
        assertEquals(IntNode.valueOf(1), found);
    }

    @Test
    public void testFindValueNested() {
        ObjectNode child = node.putObject("child");
        child.put("target", "val");
        JsonNode found = node.findValue("target");
        assertEquals(TextNode.valueOf("val"), found);
    }

    @Test
    public void testFindValueNonexistent() {
        assertNull(node.findValue("none"));
    }

    @Test
    public void testFindValuesDirect() {
        node.put("a", 1).put("b", 2);
        List<JsonNode> results = node.findValues("a", null);
        assertEquals(1, results.size());
        assertEquals(IntNode.valueOf(1), results.get(0));
    }

    @Test
    public void testFindValuesNested() {
        ObjectNode child = node.putObject("child");
        child.put("x", "childVal");
        node.put("x", "rootVal");
        List<JsonNode> results = node.findValues("x", new ArrayList<JsonNode>());
        assertEquals(2, results.size());
        // Order: first root direct, then nested
        assertEquals(TextNode.valueOf("rootVal"), results.get(0));
        assertEquals(TextNode.valueOf("childVal"), results.get(1));
    }

    @Test
    public void testFindValuesAsText() {
        node.put("a", BooleanNode.TRUE);
        List<String> list = node.findValuesAsText("a", null);
        assertEquals(1, list.size());
        assertEquals("true", list.get(0));
    }

    @Test
    public void testFindValuesAsTextNested() {
        ObjectNode child = node.putObject("child");
        child.put("a", 123);
        node.put("a", "root");
        List<String> list = node.findValuesAsText("a", new ArrayList<String>());
        assertEquals(2, list.size());
        assertEquals("root", list.get(0));
        assertEquals("123", list.get(1));
    }

    @Test
    public void testFindParentDirect() {
        node.put("direct", true);
        JsonNode parent = node.findParent("direct");
        assertSame(node, parent);
    }

    @Test
    public void testFindParentNested() {
        ObjectNode child = node.putObject("child");
        child.put("deep", 1);
        JsonNode parent = node.findParent("deep");
        assertSame(child, parent);
    }

    @Test
    public void testFindParentNonexistent() {
        assertNull(node.findParent("none"));
    }

    @Test
    public void testFindParentsDirect() {
        node.put("x", 1).put("y", 2);
        List<JsonNode> parents = node.findParents("x", null);
        assertEquals(1, parents.size());
        assertSame(node, parents.get(0));
    }

    @Test
    public void testFindParentsNested() {
        ObjectNode child = node.putObject("child");
        child.put("deep", 0);
        node.put("deep", "top");
        List<JsonNode> parents = node.findParents("deep", new ArrayList<JsonNode>());
        assertEquals(2, parents.size());
        assertSame(node, parents.get(0));
        assertSame(child, parents.get(1));
    }

    @Test
    public void testSerializeEmpty() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        node.serialize(gen, null);
        gen.close();
        assertEquals("{}", sw.toString());
    }

    @Test
    public void testSerializeWithContent() throws IOException {
        node.put("str", "text").put("num", 5);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        node.serialize(gen, null);
        gen.close();
        assertTrue(sw.toString().contains("\"str\""));
        assertTrue(sw.toString().contains("\"num\""));
    }

    @Test
    public void testSerializeWithTypeEmpty() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TypeSerializer typeSer = new StubTypeSerializer();
        node.serializeWithType(gen, null, typeSer);
        gen.close();
        // expect wrapping markers
        assertTrue(sw.toString().contains("startObject"));
        assertTrue(sw.toString().contains("endObject"));
    }

    @Test
    public void testSerializeWithTypeContent() throws IOException {
        node.put("a", 1);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TypeSerializer typeSer = new StubTypeSerializer();
        node.serializeWithType(gen, null, typeSer);
        gen.close();
        String output = sw.toString();
        assertTrue(output.contains("a"));
        assertTrue(output.contains("1"));
        assertTrue(output.startsWith("startObject"));
    }

    @Test
    public void testSetValid() {
        IntNode val = IntNode.valueOf(99);
        JsonNode result = node.set("key", val);
        assertSame(node, result);
        assertSame(val, node.get("key"));
    }

    @Test
    public void testSetNull() {
        node.set("nullKey", null);
        assertTrue(node.get("nullKey") instanceof NullNode);
    }

    @Test
    public void testSetAllMap() {
        Map<String, JsonNode> props = new HashMap<>();
        props.put("a", IntNode.valueOf(1));
        props.put("b", null);  // should become NullNode
        node.setAll(props);
        assertEquals(IntNode.valueOf(1), node.get("a"));
        assertTrue(node.get("b") instanceof NullNode);
    }

    @Test
    public void testSetAllObjectNode() {
        ObjectNode other = new ObjectNode(factory);
        other.put("x", 10).put("y", 20);
        node.setAll(other);
        assertEquals(10, node.get("x").asInt());
        assertEquals(20, node.get("y").asInt());
    }

    @Test
    public void testReplace() {
        node.put("initial", "first");
        TextNode newVal = TextNode.valueOf("second");
        JsonNode old = node.replace("initial", newVal);
        assertEquals(TextNode.valueOf("first"), old);
        assertSame(newVal, node.get("initial"));
    }

    @Test
    public void testReplaceNull() {
        node.put("initial", "existing");
        JsonNode old = node.replace("initial", null);
        assertEquals(TextNode.valueOf("existing"), old);
        assertTrue(node.get("initial") instanceof NullNode);
    }

    @Test
    public void testReplaceNonexistent() {
        // put(null) would return null
        assertNull(node.replace("new", IntNode.valueOf(1)));
    }

    @Test
    public void testWithoutSingleField() {
        node.put("remove", "value");
        node.without("remove");
        assertNull(node.get("remove"));
        assertEquals(0, node.size());
    }

    @Test
    public void testWithoutCollection() {
        node.put("a", 1).put("b", 2).put("c", 3);
        node.without(Arrays.asList("a", "c"));
        assertNull(node.get("a"));
        assertNotNull(node.get("b"));
        assertNull(node.get("c"));
    }

    @Test
    @Deprecated
    public void testDeprecatedPut() {
        JsonNode old = node.put("old", IntNode.valueOf(10));
        assertNull(old);
        // put returns old value; set returns this
        IntNode newVal = IntNode.valueOf(20);
        ObjectNode returned = node.set("old", newVal);
        assertSame(node, returned);
        assertSame(newVal, node.get("old"));
    }

    @Test
    public void testRemoveField() {
        node.put("del", "bye");
        JsonNode removed = node.remove("del");
        assertEquals(TextNode.valueOf("bye"), removed);
        assertNull(node.get("del"));
        assertNull(node.remove("none"));
    }

    @Test
    public void testRemoveCollection() {
        node.put("r1", 1).put("r2", 2).put("keep", 3);
        node.remove(Arrays.asList("r1", "r2"));
        assertNull(node.get("r1"));
        assertNull(node.get("r2"));
        assertEquals(3, node.get("keep").asInt());
    }

    @Test
    public void testRemoveAll() {
        node.put("a", 1).put("b", 2);
        node.removeAll();
        assertEquals(0, node.size());
        assertTrue(node._children.isEmpty());
    }

    @Test
    public void testRetainCollection() {
        node.put("a", 1).put("b", 2).put("c", 3);
        node.retain(Arrays.asList("a", "c"));
        assertEquals(2, node.size());
        assertNotNull(node.get("a"));
        assertNull(node.get("b"));
        assertNotNull(node.get("c"));
    }

    @Test
    public void testRetainVarargs() {
        node.put("x", 1).put("y", 2);
        node.retain("x");
        assertNotNull(node.get("x"));
        assertNull(node.get("y"));
    }

    @Test
    public void testPutArray() {
        ArrayNode arr = node.putArray("arr");
        assertNotNull(arr);
        assertTrue(arr instanceof ArrayNode);
        assertSame(arr, node.get("arr"));
    }

    @Test
    public void testPutObject() {
        ObjectNode obj = node.putObject("obj");
        assertNotNull(obj);
        assertSame(obj, node.get("obj"));
    }

    @Test
    public void testPutPOJO() {
        // pojoNode returns a node (likely POJONode or TextNode for String)
        JsonNode n = node.putPOJO("pojo", "test");
        assertSame(node, n);
        assertNotNull(node.get("pojo"));
    }

    @Test
    public void testPutNull() {
        node.putNull("nullField");
        assertTrue(node.get("nullField") instanceof NullNode);
    }

    @Test
    public void testPutShort() {
        node.put("short", (short) 5);
        assertEquals(ShortNode.valueOf((short) 5), node.get("short"));
    }

    @Test
    public void testPutShortObject() {
        node.put("shortObj", Short.valueOf((short) 42));
        assertEquals(ShortNode.valueOf((short) 42), node.get("shortObj"));
        node.put("nullShort", (Short) null);
        assertTrue(node.get("nullShort") instanceof NullNode);
    }

    @Test
    public void testPutInt() {
        node.put("int", 77);
        assertEquals(IntNode.valueOf(77), node.get("int"));
    }

    @Test
    public void testPutIntegerObject() {
        node.put("integer", Integer.valueOf(33));
        assertEquals(IntNode.valueOf(33), node.get("integer"));
        node.put("nullInt", (Integer) null);
        assertTrue(node.get("nullInt") instanceof NullNode);
    }

    @Test
    public void testPutLong() {
        node.put("long", 100L);
        assertEquals(LongNode.valueOf(100L), node.get("long"));
    }

    @Test
    public void testPutLongObject() {
        node.put("longObj", Long.valueOf(200L));
        assertEquals(LongNode.valueOf(200L), node.get("longObj"));
        node.put("nullLong", (Long) null);
        assertTrue(node.get("nullLong") instanceof NullNode);
    }

    @Test
    public void testPutFloat() {
        node.put("float", 1.5f);
        assertEquals(FloatNode.valueOf(1.5f), node.get("float"));
    }

    @Test
    public void testPutFloatObject() {
        node.put("floatObj", Float.valueOf(3.14f));
        assertEquals(FloatNode.valueOf(3.14f), node.get("floatObj"));
        node.put("nullFloat", (Float) null);
        assertTrue(node.get("nullFloat") instanceof NullNode);
    }

    @Test
    public void testPutDouble() {
        node.put("double", 2.5);
        assertEquals(DoubleNode.valueOf(2.5), node.get("double"));
    }

    @Test
    public void testPutDoubleObject() {
        node.put("doubleObj", Double.valueOf(1.1));
        assertEquals(DoubleNode.valueOf(1.1), node.get("doubleObj"));
        node.put("nullDouble", (Double) null);
        assertTrue(node.get("nullDouble") instanceof NullNode);
    }

    @Test
    public void testPutBigDecimal() {
        BigDecimal bd = new BigDecimal("10.5");
        node.put("bd", bd);
        assertEquals(DecimalNode.valueOf(bd), node.get("bd"));
        node.put("nullBd", (BigDecimal) null);
        assertTrue(node.get("nullBd") instanceof NullNode);
    }

    @Test
    public void testPutString() {
        node.put("str", "hello");
        assertEquals(TextNode.valueOf("hello"), node.get("str"));
        node.put("nullStr", (String) null);
        assertTrue(node.get("nullStr") instanceof NullNode);
    }

    @Test
    public void testPutBoolean() {
        node.put("bool", true);
        assertEquals(BooleanNode.TRUE, node.get("bool"));
    }

    @Test
    public void testPutBooleanObject() {
        node.put("boolObj", Boolean.FALSE);
        assertEquals(BooleanNode.FALSE, node.get("boolObj"));
        node.put("nullBool", (Boolean) null);
        assertTrue(node.get("nullBool") instanceof NullNode);
    }

    @Test
    public void testPutByteArray() {
        byte[] bytes = {1,2,3};
        node.put("bytes", bytes);
        assertEquals(BinaryNode.valueOf(bytes), node.get("bytes"));
        node.put("nullBytes", (byte[]) null);
        assertTrue(node.get("nullBytes") instanceof NullNode);
    }

    @Test
    public void testEqualsAndHashCode() {
        ObjectNode other = new ObjectNode(factory);
        other.put("a", 1);
        node.put("a", 1);
        assertEquals(node, other);
        assertEquals(node.hashCode(), other.hashCode());
        other.put("b", 2);
        assertNotEquals(node, other);
        assertNotEquals(node, null);
        assertEquals(node, node);
        assertNotEquals(node, "string");
    }

    @Test
    public void testToString() {
        assertEquals("{}", node.toString());
        node.put("key", "value");
        assertTrue(node.toString().contains("\"key\""));
        assertTrue(node.toString().contains("\"value\""));
        node.put("num", 7);
        String str = node.toString();
        assertTrue(str.startsWith("{"));
        assertTrue(str.endsWith("}"));
    }

    @Test
    public void testAtMethod() {
        ObjectNode inner = node.putObject("inner");
        inner.put("prop", "yes");
        JsonNode byAt = node.at(JsonPointer.compile("/inner/prop"));
        assertEquals(TextNode.valueOf("yes"), byAt);
        JsonNode missing = node.at(JsonPointer.compile("/missing"));
        assertTrue(missing.isMissingNode());
    }

    // Helper class for testing serialization with type
    private static class StubTypeSerializer extends TypeSerializer {
        public StubTypeSerializer() { super(null, null); }
        @Override
        public void writeTypePrefixForObject(Object value, JsonGenerator gen) throws IOException {
            gen.writeRaw("startObject");
        }
        @Override
        public void writeTypePrefixForArray(Object value, JsonGenerator gen) throws IOException {}
        @Override
        public void writeTypePrefixForScalar(Object value, JsonGenerator gen) throws IOException {}
        @Override
        public void writeTypePrefixForScalar(Object value, JsonGenerator gen, Class<?> type) throws IOException {}
        @Override
        public void writeTypeSuffixForObject(Object value, JsonGenerator gen) throws IOException {
            gen.writeRaw("endObject");
        }
        @Override
        public void writeTypeSuffixForArray(Object value, JsonGenerator gen) throws IOException {}
        @Override
        public void writeTypeSuffixForScalar(Object value, JsonGenerator gen) throws IOException {}
        @Override
        public void writeCustomTypePrefixForArray(Object value, JsonGenerator gen, String typeId) throws IOException {}
        @Override
        public void writeCustomTypePrefixForObject(Object value, JsonGenerator gen, String typeId) throws IOException {}
        @Override
        public void writeCustomTypePrefixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
        @Override
        public void writeCustomTypeSuffixForArray(Object value, JsonGenerator gen, String typeId) throws IOException {}
        @Override
        public void writeCustomTypeSuffixForObject(Object value, JsonGenerator gen, String typeId) throws IOException {}
        @Override
        public void writeCustomTypeSuffixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
        @Override
        public TypeSerializer forProperty(Object prop) { return this; }
        @Override
        public JsonTypeInfoAs getTypeInclusion() { return JsonTypeInfoAs.PROPERTY; }
        @Override
        public String getPropertyName() { return null; }
        @Override
        public TypeIdResolver getTypeIdResolver() { return null; }
        @Override
        public void writeTypePrefix(Object value, JsonGenerator gen) throws IOException {}
        @Override
        public void writeTypeSuffix(Object value, JsonGenerator gen) throws IOException {}
    }
}
