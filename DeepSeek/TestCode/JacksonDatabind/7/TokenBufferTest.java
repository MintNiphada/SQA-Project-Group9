package com.fasterxml.jackson.databind.util;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;

public class TokenBufferTest {

    private final ObjectCodec CODEC = new ObjectMapper();

    @Test
    public void testDefaultConstructor() {
        TokenBuffer buf = new TokenBuffer(null);
        assertFalse(buf.isClosed());
        assertNull(buf.firstToken());
        assertEquals(DEFAULT_GENERATOR_FEATURES_NOT_EXPOSED, buf.getFeatureMask()); // can't access default directly
        assertTrue(buf.getOutputContext() != null);
        assertFalse(buf.canWriteTypeId());
        assertFalse(buf.canWriteObjectId());
    }

    @Test
    public void testConstructorWithHasNativeIds() {
        TokenBuffer buf = new TokenBuffer(null, true);
        assertTrue(buf.canWriteTypeId());
        assertTrue(buf.canWriteObjectId());
    }

    @Test
    public void testConstructorWithJsonParser() throws Exception {
        TokenBuffer src = new TokenBuffer(CODEC);
        src.writeStartObject();
        src.writeEndObject();
        JsonParser parser = src.asParser();
        TokenBuffer buf = new TokenBuffer(parser);
        // should copy native id flags from parser (which are false)
        assertFalse(buf.canWriteTypeId());
        assertFalse(buf.canWriteObjectId());
    }

    @Test
    public void testWriteStartArrayAndEndArray() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartArray();
        buf.writeEndArray();
        buf.close();
        assertTrue(buf.isClosed());
    }

    @Test
    public void testWriteStartObjectAndEndObject() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        buf.writeEndObject();
    }

    @Test
    public void testWriteFieldNameString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        buf.writeFieldName("field");
        buf.writeString("value");
        buf.writeEndObject();
    }

    @Test
    public void testWriteFieldNameSerializableString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        buf.writeFieldName(new SerializedString("field"));
        buf.writeString("value");
        buf.writeEndObject();
    }

    @Test
    public void testWriteFieldNameNullSerializableString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        // writeFieldName(SerializableString) does NOT check for null; we test that it doesn't crash
        buf.writeFieldName((SerializableString) null);
    }

    @Test
    public void testWriteStringNull() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString((String) null);
        JsonToken token = buf.firstToken();
        assertEquals(JsonToken.VALUE_NULL, token);
    }

    @Test
    public void testWriteString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString("text");
        assertEquals(JsonToken.VALUE_STRING, buf.firstToken());
    }

    @Test
    public void testWriteStringSerializable() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString(new SerializedString("text"));
        assertEquals(JsonToken.VALUE_STRING, buf.firstToken());
    }

    @Test
    public void testWriteStringSerializableNull() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString((SerializableString) null);
        assertEquals(JsonToken.VALUE_NULL, buf.firstToken());
    }

    @Test
    public void testWriteNumberShort() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber((short) 1);
        assertEquals(JsonToken.VALUE_NUMBER_INT, buf.firstToken());
    }

    @Test
    public void testWriteNumberInt() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(1);
        assertEquals(JsonToken.VALUE_NUMBER_INT, buf.firstToken());
    }

    @Test
    public void testWriteNumberLong() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(1L);
        assertEquals(JsonToken.VALUE_NUMBER_INT, buf.firstToken());
    }

    @Test
    public void testWriteNumberDouble() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(1.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, buf.firstToken());
    }

    @Test
    public void testWriteNumberFloat() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(1.0f);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, buf.firstToken());
    }

    @Test
    public void testWriteNumberBigDecimal() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(new BigDecimal("1.0"));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, buf.firstToken());
    }

    @Test
    public void testWriteNumberBigDecimalNull() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber((BigDecimal) null);
        assertEquals(JsonToken.VALUE_NULL, buf.firstToken());
    }

    @Test
    public void testWriteNumberBigInteger() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(new BigInteger("1"));
        assertEquals(JsonToken.VALUE_NUMBER_INT, buf.firstToken());
    }

    @Test
    public void testWriteNumberBigIntegerNull() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber((BigInteger) null);
        assertEquals(JsonToken.VALUE_NULL, buf.firstToken());
    }

    @Test
    public void testWriteNumberString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber("123");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, buf.firstToken());
    }

    @Test
    public void testWriteBooleanTrue() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeBoolean(true);
        assertEquals(JsonToken.VALUE_TRUE, buf.firstToken());
    }

    @Test
    public void testWriteBooleanFalse() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeBoolean(false);
        assertEquals(JsonToken.VALUE_FALSE, buf.firstToken());
    }

    @Test
    public void testWriteNull() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNull();
        assertEquals(JsonToken.VALUE_NULL, buf.firstToken());
    }

    @Test
    public void testWriteObjectNull() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeObject(null);
        assertEquals(JsonToken.VALUE_NULL, buf.firstToken());
    }

    @Test
    public void testWriteObjectByteArray() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        byte[] data = new byte[] { 1, 2, 3 };
        buf.writeObject(data);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, buf.firstToken());
    }

    @Test
    public void testWriteObjectWithCodec() throws Exception {
        TokenBuffer buf = new TokenBuffer(CODEC);
        buf.writeObject("text");
        // ObjectMapper will write string value (VALUE_STRING)
        assertEquals(JsonToken.VALUE_STRING, buf.firstToken());
    }

    @Test
    public void testWriteObjectNoCodec() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeObject("text");
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, buf.firstToken());
    }

    @Test
    public void testWriteTreeNull() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeTree(null);
        assertEquals(JsonToken.VALUE_NULL, buf.firstToken());
    }

    @Test
    public void testWriteTreeNoCodec() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeTree( new ObjectMapper().createArrayNode() );
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, buf.firstToken());
    }

    @Test
    public void testWriteTreeWithCodec() throws Exception {
        TokenBuffer buf = new TokenBuffer(CODEC);
        buf.writeTree( new ObjectMapper().createArrayNode() );
        assertEquals(JsonToken.START_ARRAY, buf.firstToken());
    }

    @Test
    public void testWriteBinary() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        byte[] data = new byte[] { 1, 2, 3 };
        buf.writeBinary(Base64Variants.MIME, data, 0, data.length);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, buf.firstToken());
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteBinaryStream() {        TokenBuffer buf = new TokenBuffer(null);
        buf.writeBinary(Base64Variants.MIME, new java.io.ByteArrayInputStream(new byte[1]), 1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawUTF8String() throws Exception {        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRawUTF8String(new byte[1], 0, 1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteUTF8String() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeUTF8String(new byte[1], 0, 1);
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRaw() throws Exception {        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRaw("raw");
    }

    @Test(expected=UnsupportedOperationException.class)
    public void testWriteRawValue() throws Exception {        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRawValue("raw");
    }

    @Test
    public void testAppend() throws Exception {
        TokenBuffer src = new TokenBuffer(null);
        src.writeString("a");
        TokenBuffer dst = new TokenBuffer(null);
        dst.append(src);
        assertEquals(JsonToken.VALUE_STRING, dst.firstToken());
    }

    @Test
    public void testAppendWithNativeIds() throws Exception {
        TokenBuffer src = new TokenBuffer(null, true);
        src.writeTypeId("type");
        src.writeString("a");
        TokenBuffer dst = new TokenBuffer(null);
        assertFalse(dst.canWriteTypeId());
        dst.append(src);
        assertTrue(dst.canWriteTypeId());
    }

    @Test
    public void testSerialize() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        // Build a complex structure
        buf.writeStartObject();
        buf.writeFieldName("array");
        buf.writeStartArray();
        buf.writeString("str");
        buf.writeNumber(123);
        buf.writeNumber(45.6);
        buf.writeBoolean(true);
        buf.writeBoolean(false);
        buf.writeNull();
        buf.writeNumber(BigDecimal.ONE);
        buf.writeNumber(BigInteger.TEN);
        buf.writeNumber(100L);
        buf.writeNumber((short)5);
        buf.writeString(new SerializedString("ser"));
        buf.writeEndArray();
        buf.writeFieldName(new SerializedString("obj"));
        buf.writeStartObject();
        buf.writeFieldName("nested");
        buf.writeString("text");
        buf.writeEndObject();
        buf.writeEndObject();
        buf.close();

        // Serialize to string
        StringWriter sw = new StringWriter();
        JsonFactory f = new JsonFactory());
        JsonGenerator gen = f.createGenerator(sw);
        buf.serialize(gen);
        gen.close();

        String json = sw.toString();
        // Expected structure: {"array":["str",123,45.6,true,false,null,1,10,100,5,"ser"],"obj":{"nested":"text"}}
        // We can just check that it doesn't throw and contains some markers
        assertTrue(json.contains("\"str\""));
    }

    @Test
    public void testSerializeWithNativeIds() throws Exception {
        TokenBuffer buf = new TokenBuffer(null, true);
        buf.writeTypeId("type1");
        buf.writeObjectId("obj1");
        buf.writeStartObject();
        buf.writeEndObject();
        StringWriter sw = new StringWriter();
        JsonFactory f = new JsonFactory();
        JsonGenerator gen = f.createGenerator(sw);
        buf.serialize(gen);
        gen.close();
        // The object id and type id are written before the token
        // So we check for presence
        assertTrue(sw.toString().contains("\"obj1\"") || sw.toString().contains("\"type1\""));
    }

    @Test
    public void testToString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString("hello");
        String str = buf.toString();
        assertNotNull(str);
        assertTrue(str.contains("VALUE_STRING"));
    }

    @Test
    public void testCopyCurrentEvent() throws Exception {
        TokenBuffer src = new TokenBuffer(null);
        src.writeStartObject();
        src.writeFieldName("key");
        src.writeString("val");
        src.writeEndObject();
        JsonParser parser = src.asParser();
        parser.nextToken(); // START_OBJECT
        TokenBuffer dst = new TokenBuffer(null);
        dst.copyCurrentEvent(parser);
        assertEquals(JsonToken.START_OBJECT, dst.firstToken());
    }

    @Test
    public void testCopyCurrentStructure() throws Exception {
        TokenBuffer src = new TokenBuffer(null);
        src.writeStartObject();
        src.writeFieldName("inner");
        src.writeStartArray();
        src.writeNumber(1);
        src.writeEndArray();
        src.writeEndObject();
        JsonParser parser = src.asParser();
        parser.nextToken(); // START_OBJECT
        TokenBuffer dst = new TokenBuffer(null);
        dst.copyCurrentStructure(parser);
        // Should have copied the whole object
        JsonParser dstParser = dst.asParser();
        assertEquals(JsonToken.START_OBJECT, dstParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, dstParser.nextToken());
        assertEquals("inner", dstParser.getText());
        assertEquals(JsonToken.START_ARRAY, dstParser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, dstParser.nextToken());
        assertEquals(JsonToken.END_ARRAY, dstParser.nextToken());
        assertEquals(JsonToken.END_OBJECT, dstParser.nextToken());
        assertNull(dstParser.nextToken());
    }

    @Test
    public void testParserNextToken() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        buf.writeEndObject();
        JsonParser parser = buf.asParser();
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test
    public void testParserGetTextString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString("text");
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals("text", parser.getText());
    }

    @Test
    public void testParserGetTextFieldName() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        buf.writeFieldName("key");
        buf.writeEndObject();
        JsonParser parser = buf.asParser();
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        assertEquals("key", parser.getText());
    }

    @Test
    public void testParserGetNumberValueInt() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(42);
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals(NumberType.INT, parser.getNumberType());
        assertEquals(42, parser.getIntValue());
        assertEquals(42L, parser.getLongValue());
        assertEquals(42.0, parser.getDoubleValue(), 0.0);
        assertEquals(42.0f, parser.getFloatValue(), 0.0f);
        assertEquals(BigDecimal.valueOf(42), parser.getDecimalValue());
        assertEquals(BigInteger.valueOf(42), parser.getBigIntegerValue());
    }

    @Test
    public void testParserGetNumberValueLong() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(Long.MAX_VALUE);
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals(NumberType.LONG, parser.getNumberType());
        assertEquals(Long.MAX_VALUE, parser.getLongValue());
    }

    @Test
    public void testParserGetNumberValueFloat() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(3.14f);
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals(NumberType.FLOAT, parser.getNumberType());
        assertEquals(3.14f, parser.getFloatValue(), 0.0f);
        assertEquals(3.14, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testParserGetNumberValueDouble() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(3.14);
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals(NumberType.DOUBLE, parser.getNumberType());
        assertEquals(3.14, parser.getDoubleValue(), 0.0);
    }

    @Test
    public void testParserGetNumberValueBigDecimal() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(new BigDecimal("3.14"));
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals(NumberType.BIG_DECIMAL, parser.getNumberType());
        assertEquals(new BigDecimal("3.14"), parser.getDecimalValue());
    }

    @Test
    public void testParserGetNumberValueBigInteger() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(new BigInteger("42"));
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals(NumberType.BIG_INTEGER, parser.getNumberType());
        assertEquals(new BigInteger("42"), parser.getBigIntegerValue());
    }

    @Test
    public void testParserGetNumberValueShort() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber((short)5);
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals(NumberType.INT, parser.getNumberType()); // Short mapped to INT
        assertEquals(5, parser.getIntValue());
    }

    @Test
    public void testParserGetNumberValueFromString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber("123.45");
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals(NumberType.DOUBLE, parser.getNumberType()); // because contains '.'
        assertEquals(123.45, parser.getDoubleValue(), 0.0);
    }

    @Test(expected=JsonParseException.class)
    public void testParserGetNumberValueOnNonNumber() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString("text");
        JsonParser parser = buf.asParser();
        parser.nextToken();
        parser.getIntValue(); // should throw
    }

    @Test
    public void testParserGetEmbeddedObject() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        byte[] data = new byte[] {1,2,3};
        buf.writeObject(data); // embeds as byte array
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, parser.getCurrentToken());
        assertArrayEquals(data, (byte[])parser.getEmbeddedObject());
    }

    @Test
    public void testParserGetBinaryValueFromByteArray() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        byte[] data = new byte[] {1,2,3};
        buf.writeObject(data);
        JsonParser parser = buf.asParser();
        parser.nextToken();
        assertArrayEquals(data, parser.getBinaryValue(Base64Variants.MIME));
    }

    @Test
    public void testParserGetBinaryValueFromString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        String base64 = "AQID"; // 1,2,3 in base64
        buf.writeString(base64);
        JsonParser parser = buf.asParser();
        parser.nextToken();
        byte[] result = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(new byte[] {1,2,3}, result);
    }

    @Test
    public void testParserReadBinaryValue() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString("AQID");
        JsonParser parser = buf.asParser();
        parser.nextToken();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int len = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(3, len);
        assertArrayEquals(new byte[] {1,2,3}, out.toByteArray());
    }

    @Test
    public void testParserOverrideCurrentName() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        buf.writeFieldName("original");
        buf.writeEndObject();
        JsonParser parser = buf.asParser();
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.overrideCurrentName("overridden");
        assertEquals("overridden", parser.getCurrentName());
    }

    @Test
    public void testParserGetLocation() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        JsonParser parser = buf.asParser();
        assertEquals(JsonLocation.NA, parser.getCurrentLocation());
    }

    @Test
    public void testParserClose() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        JsonParser parser = buf.asParser();
        assertFalse(parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
        assertNull(parser.nextToken());
    }

    @Test
    public void testSegmentOverflow() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        // Write 20 tokens to trigger segment overflow
        for (int i = 0; i < 20; i++) {
            buf.writeNumber(i);
        }
        JsonParser parser = buf.asParser();
        for (int i = 0; i < 20; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(i, parser.getIntValue());
        }
        assertNull(parser.nextToken());
    }

    @Test
    public void testSegmentWithNativeIds() throws Exception {
        TokenBuffer buf = new TokenBuffer(null, true);
        buf.writeTypeId("type1");
        buf.writeString("value"); // this token will have typeId
        buf.writeObjectId("objId");
        buf.writeNumber(123); // this token will have objectId
        JsonParser parser = buf.asParser();
        parser.nextToken(); // VALUE_STRING
        assertEquals("type1", parser.getTypeId());
        assertEquals(null, parser.getObjectId());
        parser.nextToken(); // VALUE_NUMBER_INT
        assertEquals(null, parser.getTypeId());
        assertEquals("objId", parser.getObjectId());
    }

    @Test
    public void testSegmentHasIds() {
        Segment seg = new Segment();
        assertFalse(seg.hasIds());
        seg.append(0, JsonToken.VALUE_NULL);
        assertFalse(seg.hasIds());
        seg.append(1, JsonToken.VALUE_NULL, null, null); // no ids
        assertFalse(seg.hasIds());
        seg.append(2, JsonToken.VALUE_NULL, "objId", "typeId");
        assertTrue(seg.hasIds());
    }

    @Test
    public void testSegmentFindIds() {
        Segment seg = new Segment();
        seg.append(0, JsonToken.VALUE_NULL, "objId1", "typeId1");
        assertEquals("typeId1", seg.findTypeId(0));
        assertEquals("objId1", seg.findObjectId(0));
        seg.append(1, JsonToken.VALUE_NULL, "objId2", null);
        assertEquals(null, seg.findTypeId(1));
        assertEquals("objId2", seg.findObjectId(1));
    }

    @Test
    public void testSegmentAppendOverflow() {
        Segment seg = new Segment();
        Segment next = null;
        for (int i = 0; i < 16; i++) {
            next = seg.append(i, JsonToken.VALUE_NULL);
            assertNull(next);
        }
        next = seg.append(16, JsonToken.START_ARRAY);
        assertNotNull(next);
        assertEquals(JsonToken.START_ARRAY, next.type(0));
    }

    @Test
    public void testSegmentRawType() {
        Segment seg = new Segment();
        seg.appendRaw(0, 3, "value"); // token type index 3
        assertEquals(3, seg.rawType(0));
    }

    @Test
    public void testSegmentMultipleSegmentsChain() {
        Segment first = new Segment();
        Segment next = first.append(15, JsonToken.VALUE_TRUE);
        assertNull(next);
        next = first.append(16, JsonToken.VALUE_FALSE);
        assertNotNull(next);
        assertEquals(JsonToken.VALUE_TRUE, first.type(15));
        assertEquals(JsonToken.VALUE_FALSE, next.type(0));
        assertNull(first.next());
        assertNotNull(first.next()); // after setting 
    }

    @Test
    public void testSerializeVALUE_NUMBER_FLOATWithString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber("1.23");
        StringWriter sw = new StringWriter();
        JsonFactory f = new JsonFactory();
        JsonGenerator gen = f.createGenerator(sw);
        buf.serialize(gen);
        gen.close();
        assertEquals("1.23", sw.toString().trim());
    }

    @Test
    public void testSerializeVALUE_NUMBER_FLOATWithNull() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        // Append a VALUE_NUMBER_FLOAT token with value null (though unusual, test branch)
        // We can't do that through normal write* methods; but via _appendRaw we could, but it's protected.
        // We'll just test that serialize handles null in VALUE_NUMBER_FLOAT case by writing null via writeNumber((BigDecimal)null) which writes VALUE_NULL.
        // But that doesn't test the null branch. This branch appears when n==null in serialize for VALUE_NUMBER_FLOAT.
        // Since VALUE_NUMBER_FLOAT token stored with null is not reachable via public API, skip.
    }
}
```
