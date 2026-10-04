package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.NullsAsEmptyProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.deser.impl.NullsFailProvider;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.AccessPattern;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

public class StdDeserializerTest {

    @JacksonStdImpl
    static class StdImplDeser extends StdDeserializer<Object> {
        public StdImplDeser() { super(Object.class); }
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }
    }

    static class CustomDeser extends StdDeserializer<String> {
        public CustomDeser(Class<?> vc) { super(vc); }
        public CustomDeser(JavaType jt) { super(jt); }
        public CustomDeser(StdDeserializer<?> src) { super(src); }
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return _parseString(p, ctxt);
        }
    }

    @Test
    public void testConstructorsAndAccessors() {
        CustomDeser d1 = new CustomDeser(String.class);
        Assert.assertEquals(String.class, d1.handledType());
        Assert.assertEquals(String.class, d1.getValueClass());
        Assert.assertNull(d1.getValueType());

        CustomDeser d2 = new CustomDeser((JavaType) null);
        Assert.assertEquals(Object.class, d2.handledType());

        JavaType jt = TypeFactory.defaultInstance().constructType(Integer.class);
        CustomDeser d3 = new CustomDeser(jt);
        Assert.assertEquals(Integer.class, d3.handledType());

        CustomDeser d4 = new CustomDeser(d1);
        Assert.assertEquals(String.class, d4.handledType());
    }

    @Test
    public void testIsDefaultDeserializer() {
        CustomDeser deser = new CustomDeser(String.class);
        Assert.assertFalse(deser.isDefaultDeserializer(deser));
        Assert.assertTrue(deser.isDefaultDeserializer(new StdImplDeser()));
        Assert.assertFalse(deser.isDefaultKeyDeserializer(null));
    }

    @Test
    public void testArithmeticOverflowChecks() {
        CustomDeser d = new CustomDeser(String.class);
        Assert.assertFalse(d._byteOverflow(0));
        Assert.assertFalse(d._byteOverflow(255));
        Assert.assertFalse(d._byteOverflow(-128));
        Assert.assertTrue(d._byteOverflow(-129));
        Assert.assertTrue(d._byteOverflow(256));

        Assert.assertFalse(d._shortOverflow(0));
        Assert.assertFalse(d._shortOverflow(Short.MAX_VALUE));
        Assert.assertFalse(d._shortOverflow(Short.MIN_VALUE));
        Assert.assertTrue(d._shortOverflow(Short.MAX_VALUE + 1));
        Assert.assertTrue(d._shortOverflow(Short.MIN_VALUE - 1));

        Assert.assertFalse(d._intOverflow(0L));
        Assert.assertFalse(d._intOverflow((long) Integer.MAX_VALUE));
        Assert.assertFalse(d._intOverflow((long) Integer.MIN_VALUE));
        Assert.assertTrue(d._intOverflow((long) Integer.MAX_VALUE + 1L));
        Assert.assertTrue(d._intOverflow((long) Integer.MIN_VALUE - 1L));
    }

    @Test
    public void testHelperPredicates() {
        CustomDeser d = new CustomDeser(String.class);
        Assert.assertTrue(d._hasTextualNull("null"));
        Assert.assertFalse(d._hasTextualNull("NULL"));
        Assert.assertFalse(d._hasTextualNull(""));

        Assert.assertTrue(d._isEmptyOrTextualNull(""));
        Assert.assertTrue(d._isEmptyOrTextualNull("null"));
        Assert.assertFalse(d._isEmptyOrTextualNull("abc"));

        Assert.assertTrue(d._isNegInf("-Infinity"));
        Assert.assertTrue(d._isNegInf("-INF"));
        Assert.assertFalse(d._isNegInf("Infinity"));

        Assert.assertTrue(d._isPosInf("Infinity"));
        Assert.assertTrue(d._isPosInf("INF"));
        Assert.assertFalse(d._isPosInf("-INF"));

        Assert.assertTrue(d._isNaN("NaN"));
        Assert.assertFalse(d._isNaN("nan"));

        Assert.assertTrue(d._isIntNumber("12345"));
        Assert.assertTrue(d._isIntNumber("-12345"));
        Assert.assertTrue(d._isIntNumber("+12345"));
        Assert.assertFalse(d._isIntNumber("123a45"));
        Assert.assertFalse(d._isIntNumber(""));

        Assert.assertTrue(StdDeserializer._neitherNull("a", "b"));
        Assert.assertFalse(StdDeserializer._neitherNull(null, "b"));
        Assert.assertFalse(StdDeserializer._neitherNull("a", null));
        Assert.assertFalse(StdDeserializer._neitherNull(null, null));

        Assert.assertEquals(0, d._nonNullNumber(null).intValue());
        Assert.assertEquals(42, d._nonNullNumber(42).intValue());
    }

    @Test
    public void testParseDoubleHelper() {
        Assert.assertEquals(12.34, StdDeserializer.parseDouble("12.34"), 0.0001);
        Assert.assertEquals(Double.MIN_NORMAL, StdDeserializer.parseDouble(NumberInput.NASTY_SMALL_DOUBLE), 0.00000000000000001);
    }

    @Test
    public void testCoercedTypeDesc() {
        CustomDeser d1 = new CustomDeser(String.class);
        Assert.assertEquals("for type java.lang.String", d1._coercedTypeDesc());

        CustomDeser d2 = new CustomDeser(int[].class);
        Assert.assertEquals("as content of type int[]", d2._coercedTypeDesc());

        CustomDeser d3 = new CustomDeser(java.util.List.class);
        Assert.assertEquals("as content of type java.util.List", d3._coercedTypeDesc());
    }

    @Test
    public void testParseBooleanPrimitives() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CustomDeser d = new CustomDeser(Boolean.TYPE);

        JsonParser p1 = mapper.createParser("true");
        p1.nextToken();
        Assert.assertTrue(d._parseBooleanPrimitive(p1, ctxt));

        JsonParser p2 = mapper.createParser("false");
        p2.nextToken();
        Assert.assertFalse(d._parseBooleanPrimitive(p2, ctxt));

        JsonParser p3 = mapper.createParser("1");
        p3.nextToken();
        Assert.assertTrue(d._parseBooleanPrimitive(p3, ctxt));

        JsonParser p4 = mapper.createParser("0");
        p4.nextToken();
        Assert.assertFalse(d._parseBooleanPrimitive(p4, ctxt));

        JsonParser p5 = mapper.createParser("\"true\"");
        p5.nextToken();
        Assert.assertTrue(d._parseBooleanPrimitive(p5, ctxt));

        JsonParser p6 = mapper.createParser("\"False\"");
        p6.nextToken();
        Assert.assertFalse(d._parseBooleanPrimitive(p6, ctxt));

        JsonParser p7 = mapper.createParser("[true]");
        p7.nextToken();
        ObjectMapper mapperUnwrap = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        DeserializationContext ctxtUnwrap = mapperUnwrap.getDeserializationContext();
        Assert.assertTrue(d._parseBooleanPrimitive(p7, ctxtUnwrap));
    }

    @Test
    public void testNumericPrimitivesFromString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CustomDeser d = new CustomDeser(Integer.TYPE);

        Assert.assertEquals(123, d._parseIntPrimitive(ctxt, "123"));
        Assert.assertEquals(2147483647, d._parseIntPrimitive(ctxt, "2147483647"));

        Assert.assertEquals(1234567890123L, d._parseLongPrimitive(ctxt, "1234567890123"));

        Assert.assertEquals(Float.POSITIVE_INFINITY, d._parseFloatPrimitive(ctxt, "Infinity"), 0.0f);
        Assert.assertEquals(Float.NEGATIVE_INFINITY, d._parseFloatPrimitive(ctxt, "-Infinity"), 0.0f);
        Assert.assertTrue(Float.isNaN(d._parseFloatPrimitive(ctxt, "NaN")));
        Assert.assertEquals(12.5f, d._parseFloatPrimitive(ctxt, "12.5"), 0.001f);

        Assert.assertEquals(Double.POSITIVE_INFINITY, d._parseDoublePrimitive(ctxt, "Infinity"), 0.0);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, d._parseDoublePrimitive(ctxt, "-Infinity"), 0.0);
        Assert.assertTrue(Double.isNaN(d._parseDoublePrimitive(ctxt, "NaN")));
        Assert.assertEquals(12.5, d._parseDoublePrimitive(ctxt, "12.5"), 0.001);
    }

    @Test
    public void testParseNumericPrimitivesFromParser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CustomDeser d = new CustomDeser(Integer.TYPE);

        JsonParser pInt = mapper.createParser("42");
        pInt.nextToken();
        Assert.assertEquals(42, d._parseIntPrimitive(pInt, ctxt));

        JsonParser pByte = mapper.createParser("16");
        pByte.nextToken();
        Assert.assertEquals((byte) 16, d._parseBytePrimitive(pByte, ctxt));

        JsonParser pShort = mapper.createParser("300");
        pShort.nextToken();
        Assert.assertEquals((short) 300, d._parseShortPrimitive(pShort, ctxt));

        JsonParser pLong = mapper.createParser("10000000000");
        pLong.nextToken();
        Assert.assertEquals(10000000000L, d._parseLongPrimitive(pLong, ctxt));

        JsonParser pFloat = mapper.createParser("3.14");
        pFloat.nextToken();
        Assert.assertEquals(3.14f, d._parseFloatPrimitive(pFloat, ctxt), 0.001f);

        JsonParser pDouble = mapper.createParser("3.14159");
        pDouble.nextToken();
        Assert.assertEquals(3.14159, d._parseDoublePrimitive(pDouble, ctxt), 0.00001);
    }

    @Test
    public void testParseStringAndDate() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CustomDeser d = new CustomDeser(Date.class);

        JsonParser pStr = mapper.createParser("\"hello\"");
        pStr.nextToken();
        Assert.assertEquals("hello", d._parseString(pStr, ctxt));

        JsonParser pDateInt = mapper.createParser("0");
        pDateInt.nextToken();
        Date date = d._parseDate(pDateInt, ctxt);
        Assert.assertEquals(0L, date.getTime());

        JsonParser pDateNull = mapper.createParser("null");
        pDateNull.nextToken();
        Assert.assertNull(d._parseDate(pDateNull, ctxt));
    }

    @Test
    public void testCoerceIntegral() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CustomDeser d = new CustomDeser(Number.class);

        ObjectMapper mapperBigInt = new ObjectMapper().enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        JsonParser p1 = mapperBigInt.createParser("12345");
        p1.nextToken();
        Object o1 = d._coerceIntegral(p1, mapperBigInt.getDeserializationContext());
        Assert.assertTrue(o1 instanceof BigInteger);

        ObjectMapper mapperLong = new ObjectMapper().enable(DeserializationFeature.USE_LONG_FOR_INTS);
        JsonParser p2 = mapperLong.createParser("12345");
        p2.nextToken();
        Object o2 = d._coerceIntegral(p2, mapperLong.getDeserializationContext());
        Assert.assertTrue(o2 instanceof Long);
    }

    @Test
    public void testFindNullProvider() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CustomDeser d = new CustomDeser(String.class);

        NullValueProvider pSkip = d._findNullProvider(ctxt, null, Nulls.SKIP, d);
        Assert.assertSame(NullsConstantProvider.skipper(), pSkip);

        NullValueProvider pFail = d._findNullProvider(ctxt, null, Nulls.FAIL, d);
        Assert.assertTrue(pFail instanceof NullsFailProvider);

        NullValueProvider pEmpty = d._findNullProvider(ctxt, null, Nulls.AS_EMPTY, d);
        Assert.assertTrue(pEmpty instanceof NullsAsEmptyProvider || pEmpty instanceof NullsConstantProvider);
    }

    @Test
    public void testFindFormatOverrides() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CustomDeser d = new CustomDeser(String.class);

        JsonFormat.Value val = d.findFormatOverrides(ctxt, null, String.class);
        Assert.assertNotNull(val);

        Boolean feat = d.findFormatFeature(ctxt, null, String.class, JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        Assert.assertNull(feat);
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        CustomDeser d = new CustomDeser(String.class);

        TypeDeserializer td = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(BeanProperty prop) { return this; }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override
            public String getPropertyName() { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override
            public Class<?> getDefaultImpl() { return null; }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) { return "ok"; }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) { return "ok"; }
            @Override
            public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) { return "ok"; }
            @Override
            public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) { return "ok"; }
        };

        JsonParser p = mapper.createParser("\"test\"");
        p.nextToken();
        Assert.assertEquals("ok", d.deserializeWithType(p, ctxt, td));
    }
}
