package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberDeserializersTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    @Test
    public void testFindPrimitives() {
        Assert.assertNotNull(NumberDeserializers.find(Integer.TYPE, Integer.TYPE.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Boolean.TYPE, Boolean.TYPE.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Long.TYPE, Long.TYPE.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Double.TYPE, Double.TYPE.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Character.TYPE, Character.TYPE.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Byte.TYPE, Byte.TYPE.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Short.TYPE, Short.TYPE.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Float.TYPE, Float.TYPE.getName()));
    }

    @Test
    public void testFindWrappersAndBigTypes() {
        Assert.assertNotNull(NumberDeserializers.find(Integer.class, Integer.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Boolean.class, Boolean.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Long.class, Long.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Double.class, Double.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Character.class, Character.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Byte.class, Byte.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Short.class, Short.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Float.class, Float.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(Number.class, Number.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(BigDecimal.class, BigDecimal.class.getName()));
        Assert.assertNotNull(NumberDeserializers.find(BigInteger.class, BigInteger.class.getName()));
        Assert.assertNull(NumberDeserializers.find(String.class, String.class.getName()));
    }

    @Test
    public void testInstantiateContainer() {
        Assert.assertNotNull(new NumberDeserializers());
    }

    @Test
    public void testNullValueHandling() throws Exception {
        DeserializationContext ctxtDefault = mapper.getDeserializationContext();
        NumberDeserializers.IntegerDeserializer intPrim = NumberDeserializers.IntegerDeserializer.primitiveInstance;
        Assert.assertEquals(Integer.valueOf(0), intPrim.getNullValue());
        Assert.assertEquals(Integer.valueOf(0), intPrim.getNullValue(ctxtDefault));

        ObjectMapper failOnNullMapper = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        try {
            failOnNullMapper.readValue("null", int.class);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("FAIL_ON_NULL_FOR_PRIMITIVES"));
        }

        NumberDeserializers.IntegerDeserializer intWrap = NumberDeserializers.IntegerDeserializer.wrapperInstance;
        Assert.assertNull(intWrap.getNullValue());
        Assert.assertNull(intWrap.getNullValue(ctxtDefault));
    }

    @Test
    public void testBooleanDeserializer() throws Exception {
        NumberDeserializers.BooleanDeserializer deser = NumberDeserializers.BooleanDeserializer.wrapperInstance;
        JsonParser p = jsonFactory.createParser("true");
        p.nextToken();
        Assert.assertEquals(Boolean.TRUE, deser.deserialize(p, mapper.getDeserializationContext()));

        p = jsonFactory.createParser("false");
        p.nextToken();
        Assert.assertEquals(Boolean.FALSE, deser.deserializeWithType(p, mapper.getDeserializationContext(), null));
    }

    @Test
    public void testByteAndShortDeserializers() throws Exception {
        NumberDeserializers.ByteDeserializer byteDeser = NumberDeserializers.ByteDeserializer.primitiveInstance;
        JsonParser p1 = jsonFactory.createParser("12");
        p1.nextToken();
        Assert.assertEquals(Byte.valueOf((byte) 12), byteDeser.deserialize(p1, mapper.getDeserializationContext()));

        NumberDeserializers.ShortDeserializer shortDeser = NumberDeserializers.ShortDeserializer.primitiveInstance;
        JsonParser p2 = jsonFactory.createParser("345");
        p2.nextToken();
        Assert.assertEquals(Short.valueOf((short) 345), shortDeser.deserialize(p2, mapper.getDeserializationContext()));
    }

    @Test
    public void testCharacterDeserializer() throws Exception {
        NumberDeserializers.CharacterDeserializer deser = NumberDeserializers.CharacterDeserializer.wrapperInstance;
        JsonParser p1 = jsonFactory.createParser("65");
        p1.nextToken();
        Assert.assertEquals(Character.valueOf('A'), deser.deserialize(p1, mapper.getDeserializationContext()));

        JsonParser p2 = jsonFactory.createParser("\"B\"");
        p2.nextToken();
        Assert.assertEquals(Character.valueOf('B'), deser.deserialize(p2, mapper.getDeserializationContext()));

        JsonParser p3 = jsonFactory.createParser("\"\"");
        p3.nextToken();
        Assert.assertNull(deser.deserialize(p3, mapper.getDeserializationContext()));

        ObjectMapper unwrapMapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        Character c = unwrapMapper.readValue("[\"C\"]", Character.class);
        Assert.assertEquals(Character.valueOf('C'), c);

        try {
            unwrapMapper.readValue("[\"A\", \"B\"]", Character.class);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Attempted to unwrap single value array"));
        }

        try {
            JsonParser pInvalid = jsonFactory.createParser("true");
            pInvalid.nextToken();
            deser.deserialize(pInvalid, mapper.getDeserializationContext());
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testIntegerDeserializer() throws Exception {
        NumberDeserializers.IntegerDeserializer deser = NumberDeserializers.IntegerDeserializer.primitiveInstance;
        Assert.assertTrue(deser.isCachable());

        JsonParser p1 = jsonFactory.createParser("123");
        p1.nextToken();
        Assert.assertEquals(Integer.valueOf(123), deser.deserialize(p1, mapper.getDeserializationContext()));

        JsonParser p2 = jsonFactory.createParser("456");
        p2.nextToken();
        Assert.assertEquals(Integer.valueOf(456), deser.deserializeWithType(p2, mapper.getDeserializationContext(), null));

        JsonParser p3 = jsonFactory.createParser("\"789\"");
        p3.nextToken();
        Assert.assertEquals(Integer.valueOf(789), deser.deserialize(p3, mapper.getDeserializationContext()));
        
        JsonParser p4 = jsonFactory.createParser("\"789\"");
        p4.nextToken();
        Assert.assertEquals(Integer.valueOf(789), deser.deserializeWithType(p4, mapper.getDeserializationContext(), null));
    }

    @Test
    public void testLongDeserializer() throws Exception {
        NumberDeserializers.LongDeserializer deser = NumberDeserializers.LongDeserializer.primitiveInstance;
        Assert.assertTrue(deser.isCachable());

        JsonParser p1 = jsonFactory.createParser("1234567890123");
        p1.nextToken();
        Assert.assertEquals(Long.valueOf(1234567890123L), deser.deserialize(p1, mapper.getDeserializationContext()));

        JsonParser p2 = jsonFactory.createParser("\"1234567890123\"");
        p2.nextToken();
        Assert.assertEquals(Long.valueOf(1234567890123L), deser.deserialize(p2, mapper.getDeserializationContext()));
    }

    @Test
    public void testFloatAndDoubleDeserializers() throws Exception {
        NumberDeserializers.FloatDeserializer floatDeser = NumberDeserializers.FloatDeserializer.primitiveInstance;
        JsonParser p1 = jsonFactory.createParser("1.25");
        p1.nextToken();
        Assert.assertEquals(Float.valueOf(1.25f), floatDeser.deserialize(p1, mapper.getDeserializationContext()));

        NumberDeserializers.DoubleDeserializer doubleDeser = NumberDeserializers.DoubleDeserializer.primitiveInstance;
        JsonParser p2 = jsonFactory.createParser("2.5");
        p2.nextToken();
        Assert.assertEquals(Double.valueOf(2.5d), doubleDeser.deserialize(p2, mapper.getDeserializationContext()));

        JsonParser p3 = jsonFactory.createParser("3.75");
        p3.nextToken();
        Assert.assertEquals(Double.valueOf(3.75d), doubleDeser.deserializeWithType(p3, mapper.getDeserializationContext(), null));
    }

    @Test
    public void testNumberDeserializerVariants() throws Exception {
        NumberDeserializers.NumberDeserializer deser = NumberDeserializers.NumberDeserializer.instance;
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser pInt = jsonFactory.createParser("123");
        pInt.nextToken();
        Assert.assertEquals(123, deser.deserialize(pInt, ctxt));

        JsonParser pFloat = jsonFactory.createParser("123.45");
        pFloat.nextToken();
        Assert.assertEquals(123.45d, deser.deserialize(pFloat, ctxt));

        ObjectMapper bdMapper = new ObjectMapper().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        Assert.assertEquals(new BigDecimal("123.45"), bdMapper.readValue("123.45", Number.class));

        Assert.assertNull(mapper.readValue("\"\"", Number.class));
        Assert.assertNull(mapper.readValue("\"null\"", Number.class));
        Assert.assertEquals(Double.POSITIVE_INFINITY, mapper.readValue("\"Infinity\"", Number.class));
        Assert.assertEquals(Double.POSITIVE_INFINITY, mapper.readValue("\"+Infinity\"", Number.class));
        Assert.assertEquals(Double.NEGATIVE_INFINITY, mapper.readValue("\"-Infinity\"", Number.class));
        Assert.assertEquals(Double.NaN, mapper.readValue("\"NaN\"", Number.class));

        Assert.assertEquals(new Double(12.34), mapper.readValue("\"12.34\"", Number.class));
        Assert.assertEquals(new BigDecimal("12.34"), bdMapper.readValue("\"12.34\"", Number.class));

        ObjectMapper biMapper = new ObjectMapper().enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        Assert.assertEquals(new BigInteger("123"), biMapper.readValue("\"123\"", Number.class));

        ObjectMapper longMapper = new ObjectMapper().enable(DeserializationFeature.USE_LONG_FOR_INTS);
        Assert.assertEquals(Long.valueOf(123L), longMapper.readValue("\"123\"", Number.class));
        Assert.assertEquals(Integer.valueOf(123), mapper.readValue("\"123\"", Number.class));
        Assert.assertEquals(Long.valueOf(5000000000L), mapper.readValue("\"5000000000\"", Number.class));

        try {
            mapper.readValue("\"not_a_number\"", Number.class);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("not a valid number"));
        }

        ObjectMapper unwrapMapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        Assert.assertEquals(42, unwrapMapper.readValue("[42]", Number.class));

        try {
            unwrapMapper.readValue("[42, 43]", Number.class);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Attempted to unwrap single value array"));
        }

        try {
            JsonParser pObj = jsonFactory.createParser("{}");
            pObj.nextToken();
            deser.deserialize(pObj, ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }

        Assert.assertEquals(123, deser.deserializeWithType(pInt, ctxt, null));
        Assert.assertEquals(123.45d, deser.deserializeWithType(pFloat, ctxt, null));
        JsonParser pStr = jsonFactory.createParser("\"123\"");
        pStr.nextToken();
        Assert.assertEquals(123, deser.deserializeWithType(pStr, ctxt, null));
    }

    @Test
    public void testBigIntegerDeserializer() throws Exception {
        NumberDeserializers.BigIntegerDeserializer deser = NumberDeserializers.BigIntegerDeserializer.instance;
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser pInt = jsonFactory.createParser("12345");
        pInt.nextToken();
        Assert.assertEquals(BigInteger.valueOf(12345), deser.deserialize(pInt, ctxt));

        JsonParser pStr = jsonFactory.createParser("\"98765432109876543210\"");
        pStr.nextToken();
        Assert.assertEquals(new BigInteger("98765432109876543210"), deser.deserialize(pStr, ctxt));

        JsonParser pEmpty = jsonFactory.createParser("\"\"");
        pEmpty.nextToken();
        Assert.assertNull(deser.deserialize(pEmpty, ctxt));

        JsonParser pFloat = jsonFactory.createParser("123.45");
        pFloat.nextToken();
        Assert.assertEquals(BigInteger.valueOf(123), deser.deserialize(pFloat, ctxt));

        ObjectMapper noFloatAsInt = new ObjectMapper().disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        try {
            noFloatAsInt.readValue("123.45", BigInteger.class);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("java.math.BigInteger"));
        }

        try {
            JsonParser pInvalidStr = jsonFactory.createParser("\"invalid\"");
            pInvalidStr.nextToken();
            deser.deserialize(pInvalidStr, ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("not a valid representation"));
        }

        ObjectMapper unwrapMapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        Assert.assertEquals(BigInteger.valueOf(99), unwrapMapper.readValue("[99]", BigInteger.class));

        try {
            unwrapMapper.readValue("[99, 100]", BigInteger.class);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Attempted to unwrap single value array"));
        }

        try {
            JsonParser pBool = jsonFactory.createParser("true");
            pBool.nextToken();
            deser.deserialize(pBool, ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testBigDecimalDeserializer() throws Exception {
        NumberDeserializers.BigDecimalDeserializer deser = NumberDeserializers.BigDecimalDeserializer.instance;
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser pInt = jsonFactory.createParser("123");
        pInt.nextToken();
        Assert.assertEquals(new BigDecimal("123"), deser.deserialize(pInt, ctxt));

        JsonParser pFloat = jsonFactory.createParser("123.45");
        pFloat.nextToken();
        Assert.assertEquals(new BigDecimal("123.45"), deser.deserialize(pFloat, ctxt));

        JsonParser pStr = jsonFactory.createParser("\"123.450\"");
        pStr.nextToken();
        Assert.assertEquals(new BigDecimal("123.450"), deser.deserialize(pStr, ctxt));

        JsonParser pEmpty = jsonFactory.createParser("\"\"");
        pEmpty.nextToken();
        Assert.assertNull(deser.deserialize(pEmpty, ctxt));

        try {
            JsonParser pInvalid = jsonFactory.createParser("\"not_a_dec\"");
            pInvalid.nextToken();
            deser.deserialize(pInvalid, ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("not a valid representation"));
        }

        ObjectMapper unwrapMapper = new ObjectMapper().enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        Assert.assertEquals(new BigDecimal("42.0"), unwrapMapper.readValue("[\"42.0\"]", BigDecimal.class));

        try {
            unwrapMapper.readValue("[\"42.0\", \"43.0\"]", BigDecimal.class);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Attempted to unwrap single value array"));
        }

        try {
            JsonParser pBool = jsonFactory.createParser("true");
            pBool.nextToken();
            deser.deserialize(pBool, ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }
}
