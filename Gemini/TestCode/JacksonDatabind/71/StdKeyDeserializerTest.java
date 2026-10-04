package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.util.EnumResolver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class StdKeyDeserializerTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;

    private enum TestEnum {
        ALPHA,
        BETA;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public static class StringCtorClass {
        final String val;
        public StringCtorClass(String val) {
            if ("fail".equals(val)) {
                throw new IllegalArgumentException("forced fail");
            }
            this.val = val;
        }
    }

    public static class StringFactoryClass {
        final String val;
        private StringFactoryClass(String val) { this.val = val; }
        public static StringFactoryClass valueOf(String val) {
            if ("fail".equals(val)) {
                throw new IllegalArgumentException("forced fail");
            }
            return new StringFactoryClass(val);
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
    }

    @Test
    public void testNullKeyReturnsNull() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        Assert.assertNull(kd.deserializeKey(null, ctxt));
    }

    @Test
    public void testForTypeStringAndObject() throws Exception {
        StdKeyDeserializer kdStr = StdKeyDeserializer.forType(String.class);
        Assert.assertNotNull(kdStr);
        Assert.assertEquals("hello", kdStr.deserializeKey("hello", ctxt));
        Assert.assertEquals(String.class, kdStr.getKeyClass());

        StdKeyDeserializer kdObj = StdKeyDeserializer.forType(Object.class);
        Assert.assertNotNull(kdObj);
        Assert.assertEquals("objVal", kdObj.deserializeKey("objVal", ctxt));
        Assert.assertEquals(Object.class, kdObj.getKeyClass());

        StdKeyDeserializer.StringKD customKD = StdKeyDeserializer.StringKD.forType(StringBuilder.class);
        Assert.assertEquals("foo", customKD.deserializeKey("foo", ctxt));
    }

    @Test
    public void testForTypeUnrecognized() {
        Assert.assertNull(StdKeyDeserializer.forType(java.util.List.class));
    }

    @Test
    public void testBoolean() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        Assert.assertEquals(Boolean.TRUE, kd.deserializeKey("true", ctxt));
        Assert.assertEquals(Boolean.FALSE, kd.deserializeKey("false", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testBooleanInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        kd.deserializeKey("yes", ctxt);
    }

    @Test
    public void testByte() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        Assert.assertEquals(Byte.valueOf((byte) 0), kd.deserializeKey("0", ctxt));
        Assert.assertEquals(Byte.valueOf((byte) -128), kd.deserializeKey("-128", ctxt));
        Assert.assertEquals(Byte.valueOf((byte) 127), kd.deserializeKey("127", ctxt));
        Assert.assertEquals(Byte.valueOf((byte) 255), kd.deserializeKey("255", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testByteOverflowHigh() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        kd.deserializeKey("256", ctxt);
    }

    @Test(expected = JsonMappingException.class)
    public void testByteOverflowLow() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        kd.deserializeKey("-129", ctxt);
    }

    @Test
    public void testShort() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        Assert.assertEquals(Short.valueOf((short) 123), kd.deserializeKey("123", ctxt));
        Assert.assertEquals(Short.valueOf((short) -32768), kd.deserializeKey("-32768", ctxt));
        Assert.assertEquals(Short.valueOf((short) 32767), kd.deserializeKey("32767", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testShortOverflowHigh() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        kd.deserializeKey("32768", ctxt);
    }

    @Test(expected = JsonMappingException.class)
    public void testShortOverflowLow() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        kd.deserializeKey("-32769", ctxt);
    }

    @Test
    public void testChar() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        Assert.assertEquals(Character.valueOf('a'), kd.deserializeKey("a", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testCharInvalidLength() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        kd.deserializeKey("abc", ctxt);
    }

    @Test
    public void testInt() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        Assert.assertEquals(12345, kd.deserializeKey("12345", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testIntInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        kd.deserializeKey("invalid_int", ctxt);
    }

    @Test
    public void testLong() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Long.class);
        Assert.assertEquals(1234567890123L, kd.deserializeKey("1234567890123", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testLongInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Long.class);
        kd.deserializeKey("invalid_long", ctxt);
    }

    @Test
    public void testFloat() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Float.class);
        Assert.assertEquals(Float.valueOf(1.23f), kd.deserializeKey("1.23", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testFloatInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Float.class);
        kd.deserializeKey("invalid_float", ctxt);
    }

    @Test
    public void testDouble() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Double.class);
        Assert.assertEquals(Double.valueOf(1.234567), kd.deserializeKey("1.234567", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testDoubleInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Double.class);
        kd.deserializeKey("invalid_double", ctxt);
    }

    @Test
    public void testLocale() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Locale.class);
        Assert.assertEquals(Locale.US, kd.deserializeKey("en_US", ctxt));
    }

    @Test
    public void testCurrency() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Currency.class);
        Assert.assertEquals(Currency.getInstance("USD"), kd.deserializeKey("USD", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testCurrencyInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Currency.class);
        kd.deserializeKey("INVALID_CURR", ctxt);
    }

    @Test
    public void testUUID() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(UUID.class);
        UUID uuid = UUID.randomUUID();
        Assert.assertEquals(uuid, kd.deserializeKey(uuid.toString(), ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testUUIDInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(UUID.class);
        kd.deserializeKey("invalid-uuid", ctxt);
    }

    @Test
    public void testURI() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(URI.class);
        Assert.assertEquals(URI.create("http://localhost"), kd.deserializeKey("http://localhost", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testURIInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(URI.class);
        kd.deserializeKey("http://invalid uri", ctxt);
    }

    @Test
    public void testURL() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(URL.class);
        Assert.assertEquals(new URL("http://localhost"), kd.deserializeKey("http://localhost", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testURLInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(URL.class);
        kd.deserializeKey("invalid_url", ctxt);
    }

    @Test
    public void testClass() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Class.class);
        Assert.assertEquals(String.class, kd.deserializeKey("java.lang.String", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testClassInvalid() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Class.class);
        kd.deserializeKey("non.existent.Class", ctxt);
    }

    @Test
    public void testDateAndCalendar() throws Exception {
        StdKeyDeserializer kdDate = StdKeyDeserializer.forType(Date.class);
        Object dateRes = kdDate.deserializeKey("2020-01-01T00:00:00.000+0000", ctxt);
        Assert.assertTrue(dateRes instanceof Date);

        StdKeyDeserializer kdCal = StdKeyDeserializer.forType(Calendar.class);
        Object calRes = kdCal.deserializeKey("2020-01-01T00:00:00.000+0000", ctxt);
        Assert.assertTrue(calRes instanceof Calendar);
    }

    @Test
    public void testEnumKD() throws Exception {
        EnumResolver er = EnumResolver.constructUnsafe(TestEnum.class, mapper.getDeserializationConfig().getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD kd = new StdKeyDeserializer.EnumKD(er, null);

        Assert.assertEquals(TestEnum.ALPHA, kd.deserializeKey("ALPHA", ctxt));
        Assert.assertEquals(TestEnum.BETA, kd.deserializeKey("BETA", ctxt));

        ObjectMapper mToString = new ObjectMapper();
        mToString.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        DeserializationContext ctxtToString = mToString.getDeserializationContext();
        Assert.assertEquals(TestEnum.ALPHA, kd.deserializeKey("alpha", ctxtToString));
    }

    @Test(expected = JsonMappingException.class)
    public void testEnumKDInvalid() throws Exception {
        EnumResolver er = EnumResolver.constructUnsafe(TestEnum.class, mapper.getDeserializationConfig().getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD kd = new StdKeyDeserializer.EnumKD(er, null);
        kd.deserializeKey("GAMMA", ctxt);
    }

    @Test
    public void testEnumKDReadUnknownAsNull() throws Exception {
        EnumResolver er = EnumResolver.constructUnsafe(TestEnum.class, mapper.getDeserializationConfig().getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD kd = new StdKeyDeserializer.EnumKD(er, null);

        ObjectMapper mNull = new ObjectMapper();
        mNull.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        DeserializationContext ctxtNull = mNull.getDeserializationContext();
        Assert.assertNull(kd.deserializeKey("GAMMA", ctxtNull));
    }

    @Test
    public void testStringCtorKeyDeserializer() throws Exception {
        Constructor<?> ctor = StringCtorClass.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        Object result = kd.deserializeKey("val1", ctxt);
        Assert.assertTrue(result instanceof StringCtorClass);
        Assert.assertEquals("val1", ((StringCtorClass) result).val);
    }

    @Test(expected = JsonMappingException.class)
    public void testStringCtorKeyDeserializerException() throws Exception {
        Constructor<?> ctor = StringCtorClass.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        kd.deserializeKey("fail", ctxt);
    }

    @Test
    public void testStringFactoryKeyDeserializer() throws Exception {
        Method method = StringFactoryClass.class.getMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(method);
        Object result = kd.deserializeKey("val2", ctxt);
        Assert.assertTrue(result instanceof StringFactoryClass);
        Assert.assertEquals("val2", ((StringFactoryClass) result).val);
    }

    @Test(expected = JsonMappingException.class)
    public void testStringFactoryKeyDeserializerException() throws Exception {
        Method method = StringFactoryClass.class.getMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(method);
        kd.deserializeKey("fail", ctxt);
    }

    @Test
    public void testDelegatingKD() throws Exception {
        JsonDeserializer<String> mockDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return "delegatedValue";
            }
        };
        StdKeyDeserializer.DelegatingKD kd = new StdKeyDeserializer.DelegatingKD(String.class, mockDeser);
        Assert.assertEquals(String.class, kd.getKeyClass());
        Assert.assertNull(kd.deserializeKey(null, ctxt));
        Assert.assertEquals("delegatedValue", kd.deserializeKey("any", ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testDelegatingKDReturnsNullThrows() throws Exception {
        JsonDeserializer<String> mockDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return null;
            }
        };
        StdKeyDeserializer.DelegatingKD kd = new StdKeyDeserializer.DelegatingKD(String.class, mockDeser);
        kd.deserializeKey("any", ctxt);
    }

    @Test(expected = JsonMappingException.class)
    public void testDelegatingKDThrowsException() throws Exception {
        JsonDeserializer<String> mockDeser = new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw new IOException("error in delegate");
            }
        };
        StdKeyDeserializer.DelegatingKD kd = new StdKeyDeserializer.DelegatingKD(String.class, mockDeser);
        kd.deserializeKey("any", ctxt);
    }

    @Test
    public void testProtectedHelpers() throws Exception {
        StdKeyDeserializer kd = new StdKeyDeserializer(999, Object.class);
        Assert.assertEquals(123, kd._parseInt("123"));
        Assert.assertEquals(123456789L, kd._parseLong("123456789"));
        Assert.assertEquals(1.25, kd._parseDouble("1.25"), 0.0001);
        Assert.assertNull(kd._parse("dummy", ctxt));
    }
}
