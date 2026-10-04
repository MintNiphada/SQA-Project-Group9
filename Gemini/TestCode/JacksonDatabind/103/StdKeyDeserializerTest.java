package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
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

    private enum TestEnum {
        A, B
    }

    public static class SampleClassWithCtor {
        final String val;
        public SampleClassWithCtor(String v) { this.val = v; }
    }

    public static class SampleClassWithFactory {
        final String val;
        private SampleClassWithFactory(String v) { this.val = v; }
        public static SampleClassWithFactory valueOf(String v) { return new SampleClassWithFactory(v); }
    }

    private ObjectMapper mapper;
    private DeserializationContext ctxt;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
    }

    @Test
    public void testForTypeFactory() {
        Assert.assertTrue(StdKeyDeserializer.forType(String.class) instanceof StdKeyDeserializer.StringKD);
        Assert.assertTrue(StdKeyDeserializer.forType(Object.class) instanceof StdKeyDeserializer.StringKD);
        Assert.assertTrue(StdKeyDeserializer.forType(CharSequence.class) instanceof StdKeyDeserializer.StringKD);
        Assert.assertEquals(StdKeyDeserializer.TYPE_UUID, StdKeyDeserializer.forType(UUID.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_INT, StdKeyDeserializer.forType(Integer.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_LONG, StdKeyDeserializer.forType(Long.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_DATE, StdKeyDeserializer.forType(Date.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_CALENDAR, StdKeyDeserializer.forType(Calendar.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_BOOLEAN, StdKeyDeserializer.forType(Boolean.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_BYTE, StdKeyDeserializer.forType(Byte.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_CHAR, StdKeyDeserializer.forType(Character.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_SHORT, StdKeyDeserializer.forType(Short.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_FLOAT, StdKeyDeserializer.forType(Float.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_DOUBLE, StdKeyDeserializer.forType(Double.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_URI, StdKeyDeserializer.forType(URI.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_URL, StdKeyDeserializer.forType(URL.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_CLASS, StdKeyDeserializer.forType(Class.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_LOCALE, StdKeyDeserializer.forType(Locale.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_CURRENCY, StdKeyDeserializer.forType(Currency.class)._kind);
        Assert.assertEquals(StdKeyDeserializer.TYPE_BYTE_ARRAY, StdKeyDeserializer.forType(byte[].class)._kind);
        Assert.assertNull(StdKeyDeserializer.forType(SampleClassWithCtor.class));
    }

    @Test
    public void testDeserializeNullKey() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        Assert.assertNull(kd.deserializeKey(null, ctxt));
        Assert.assertEquals(Integer.class, kd.getKeyClass());
    }

    @Test
    public void testStringKD() throws Exception {
        StdKeyDeserializer.StringKD kd1 = StdKeyDeserializer.StringKD.forType(String.class);
        StdKeyDeserializer.StringKD kd2 = StdKeyDeserializer.StringKD.forType(Object.class);
        StdKeyDeserializer.StringKD kd3 = StdKeyDeserializer.StringKD.forType(CharSequence.class);
        Assert.assertEquals("foo", kd1.deserializeKey("foo", ctxt));
        Assert.assertEquals("bar", kd2.deserializeKey("bar", ctxt));
        Assert.assertEquals("baz", kd3.deserializeKey("baz", ctxt));
    }

    @Test
    public void testBooleans() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        Assert.assertEquals(Boolean.TRUE, kd.deserializeKey("true", ctxt));
        Assert.assertEquals(Boolean.FALSE, kd.deserializeKey("false", ctxt));
        try {
            kd.deserializeKey("not-bool", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public void testBytes() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        Assert.assertEquals((byte) 12, kd.deserializeKey("12", ctxt));
        Assert.assertEquals((byte) -128, kd.deserializeKey("-128", ctxt));
        Assert.assertEquals((byte) 255, kd.deserializeKey("255", ctxt));
        try {
            kd.deserializeKey("256", ctxt);
            Assert.fail();
        } catch (Exception e) {}
        try {
            kd.deserializeKey("-129", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public void testShorts() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        Assert.assertEquals((short) 1234, kd.deserializeKey("1234", ctxt));
        Assert.assertEquals(Short.MIN_VALUE, kd.deserializeKey(String.valueOf(Short.MIN_VALUE), ctxt));
        Assert.assertEquals(Short.MAX_VALUE, kd.deserializeKey(String.valueOf(Short.MAX_VALUE), ctxt));
        try {
            kd.deserializeKey("32768", ctxt);
            Assert.fail();
        } catch (Exception e) {}
        try {
            kd.deserializeKey("-32769", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public void testChars() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        Assert.assertEquals('a', kd.deserializeKey("a", ctxt));
        try {
            kd.deserializeKey("abc", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public void testIntsAndLongs() throws Exception {
        StdKeyDeserializer kdInt = StdKeyDeserializer.forType(Integer.class);
        Assert.assertEquals(42, kdInt.deserializeKey("42", ctxt));

        StdKeyDeserializer kdLong = StdKeyDeserializer.forType(Long.class);
        Assert.assertEquals(1234567890123L, kdLong.deserializeKey("1234567890123", ctxt));

        try {
            kdInt.deserializeKey("invalid", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public void testFloatsAndDoubles() throws Exception {
        StdKeyDeserializer kdFloat = StdKeyDeserializer.forType(Float.class);
        Assert.assertEquals(1.25f, ((Float) kdFloat.deserializeKey("1.25", ctxt)).floatValue(), 0.0001f);

        StdKeyDeserializer kdDouble = StdKeyDeserializer.forType(Double.class);
        Assert.assertEquals(1.25d, ((Double) kdDouble.deserializeKey("1.25", ctxt)).doubleValue(), 0.0001d);
    }

    @Test
    public void testLocaleAndCurrency() throws Exception {
        StdKeyDeserializer kdLocale = StdKeyDeserializer.forType(Locale.class);
        Assert.assertEquals(Locale.CHINA, kdLocale.deserializeKey("zh_CN", ctxt));

        StdKeyDeserializer kdCurrency = StdKeyDeserializer.forType(Currency.class);
        Assert.assertEquals(Currency.getInstance("USD"), kdCurrency.deserializeKey("USD", ctxt));
        try {
            kdCurrency.deserializeKey("INVALID_CURRENCY", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public void testDateAndCalendar() throws Exception {
        StdKeyDeserializer kdDate = StdKeyDeserializer.forType(Date.class);
        Object date = kdDate.deserializeKey("2020-01-01T00:00:00.000+00:00", ctxt);
        Assert.assertNotNull(date);
        Assert.assertTrue(date instanceof Date);

        StdKeyDeserializer kdCal = StdKeyDeserializer.forType(Calendar.class);
        Object cal = kdCal.deserializeKey("2020-01-01T00:00:00.000+00:00", ctxt);
        Assert.assertNotNull(cal);
        Assert.assertTrue(cal instanceof Calendar);
    }

    @Test
    public void testUuidUriUrlAndClass() throws Exception {
        StdKeyDeserializer kdUuid = StdKeyDeserializer.forType(UUID.class);
        UUID uuid = UUID.randomUUID();
        Assert.assertEquals(uuid, kdUuid.deserializeKey(uuid.toString(), ctxt));
        try {
            kdUuid.deserializeKey("not-a-uuid", ctxt);
            Assert.fail();
        } catch (Exception e) {}

        StdKeyDeserializer kdUri = StdKeyDeserializer.forType(URI.class);
        Assert.assertEquals(URI.create("http://localhost"), kdUri.deserializeKey("http://localhost", ctxt));
        try {
            kdUri.deserializeKey("://invalid", ctxt);
            Assert.fail();
        } catch (Exception e) {}

        StdKeyDeserializer kdUrl = StdKeyDeserializer.forType(URL.class);
        Assert.assertEquals(new URL("http://localhost"), kdUrl.deserializeKey("http://localhost", ctxt));
        try {
            kdUrl.deserializeKey("invalid-url", ctxt);
            Assert.fail();
        } catch (Exception e) {}

        StdKeyDeserializer kdClass = StdKeyDeserializer.forType(Class.class);
        Assert.assertEquals(String.class, kdClass.deserializeKey("java.lang.String", ctxt));
        try {
            kdClass.deserializeKey("com.nonexistent.Class", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public void testByteArray() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(byte[].class);
        byte[] original = new byte[]{1, 2, 3};
        String base64 = Base64Variants.getDefaultVariant().encode(original);
        byte[] result = (byte[]) kd.deserializeKey(base64, ctxt);
        Assert.assertArrayEquals(original, result);
        try {
            kd.deserializeKey("!@#$", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test(expected = IllegalStateException.class)
    public void testUnknownKind() throws Exception {
        StdKeyDeserializer kd = new StdKeyDeserializer(999, Object.class);
        kd._parse("key", ctxt);
    }

    @Test
    public void testStringCtorKeyDeserializer() throws Exception {
        Constructor<?> ctor = SampleClassWithCtor.class.getConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        SampleClassWithCtor result = (SampleClassWithCtor) kd.deserializeKey("hello", ctxt);
        Assert.assertNotNull(result);
        Assert.assertEquals("hello", result.val);
    }

    @Test
    public void testStringFactoryKeyDeserializer() throws Exception {
        Method method = SampleClassWithFactory.class.getMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(method);
        SampleClassWithFactory result = (SampleClassWithFactory) kd.deserializeKey("world", ctxt);
        Assert.assertNotNull(result);
        Assert.assertEquals("world", result.val);
    }

    @Test
    public void testDelegatingKD() throws Exception {
        JsonDeserializer<?> deser = ctxt.findRootValueDeserializer(ctxt.constructType(String.class));
        StdKeyDeserializer.DelegatingKD kd = new StdKeyDeserializer.DelegatingKD(String.class, deser);
        Assert.assertEquals(String.class, kd.getKeyClass());
        Assert.assertNull(kd.deserializeKey(null, ctxt));
        Assert.assertEquals("foo", kd.deserializeKey("foo", ctxt));
    }

    @Test
    public void testEnumKD() throws Exception {
        EnumResolver resolver = EnumResolver.constructUnsafe(TestEnum.class, mapper.getDeserializationConfig().getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD kd = new StdKeyDeserializer.EnumKD(resolver, null);

        Assert.assertEquals(TestEnum.A, kd.deserializeKey("A", ctxt));
        Assert.assertEquals(TestEnum.B, kd.deserializeKey("B", ctxt));

        ObjectMapper nullEnumMapper = new ObjectMapper();
        nullEnumMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        DeserializationContext nullCtxt = nullEnumMapper.getDeserializationContext();
        Assert.assertNull(kd.deserializeKey("C", nullCtxt));

        ObjectMapper toStringEnumMapper = new ObjectMapper();
        toStringEnumMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        DeserializationContext toStringCtxt = toStringEnumMapper.getDeserializationContext();
        Assert.assertEquals(TestEnum.A, kd.deserializeKey("A", toStringCtxt));

        try {
            kd.deserializeKey("UNKNOWN", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }
}
