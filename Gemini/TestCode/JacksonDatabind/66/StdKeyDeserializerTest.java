package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
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

    enum TestEnum {
        A, B;
        @Override
        public String toString() {
            return "custom_" + name();
        }
    }

    public static class StringCtorClass {
        final String val;
        public StringCtorClass(String v) { this.val = v; }
    }

    public static class StringFactoryClass {
        final String val;
        private StringFactoryClass(String v) { this.val = v; }
        public static StringFactoryClass valueOf(String v) {
            return new StringFactoryClass(v);
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
    }

    @Test
    public void testForTypeCreation() {
        Assert.assertNotNull(StdKeyDeserializer.forType(String.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Object.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(UUID.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Integer.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Long.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Date.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Calendar.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Boolean.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Byte.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Character.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Short.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Float.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Double.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(URI.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(URL.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Class.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Locale.class));
        Assert.assertNotNull(StdKeyDeserializer.forType(Currency.class));
        Assert.assertNull(StdKeyDeserializer.forType(TestEnum.class));
    }

    @Test
    public void testNullKey() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        Assert.assertNull(kd.deserializeKey(null, ctxt));
    }

    @Test
    public void testBooleans() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Boolean.class);
        Assert.assertEquals(Boolean.TRUE, kd.deserializeKey("true", ctxt));
        Assert.assertEquals(Boolean.FALSE, kd.deserializeKey("false", ctxt));
        try {
            kd.deserializeKey("invalid", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
    }

    @Test
    public void testBytes() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Byte.class);
        Assert.assertEquals((byte) 123, kd.deserializeKey("123", ctxt));
        Assert.assertEquals((byte) 255, kd.deserializeKey("255", ctxt));
        try {
            kd.deserializeKey("256", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
        try {
            kd.deserializeKey("-129", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
    }

    @Test
    public void testShorts() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Short.class);
        Assert.assertEquals((short) 1234, kd.deserializeKey("1234", ctxt));
        try {
            kd.deserializeKey("32768", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
        try {
            kd.deserializeKey("-32769", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
    }

    @Test
    public void testCharacters() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Character.class);
        Assert.assertEquals('a', kd.deserializeKey("a", ctxt));
        try {
            kd.deserializeKey("abc", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
    }

    @Test
    public void testIntegers() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        Assert.assertEquals(123456, kd.deserializeKey("123456", ctxt));
        try {
            kd.deserializeKey("not-an-int", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
    }

    @Test
    public void testLongs() throws Exception {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Long.class);
        Assert.assertEquals(123456789012L, kd.deserializeKey("123456789012", ctxt));
        try {
            kd.deserializeKey("not-a-long", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
    }

    @Test
    public void testFloatsAndDoubles() throws Exception {
        StdKeyDeserializer floatKd = StdKeyDeserializer.forType(Float.class);
        Assert.assertEquals(1.25f, (Float) floatKd.deserializeKey("1.25", ctxt), 0.0001f);

        StdKeyDeserializer doubleKd = StdKeyDeserializer.forType(Double.class);
        Assert.assertEquals(1.25d, (Double) doubleKd.deserializeKey("1.25", ctxt), 0.0001d);
    }

    @Test
    public void testLocaleAndCurrency() throws Exception {
        StdKeyDeserializer locKd = StdKeyDeserializer.forType(Locale.class);
        Assert.assertEquals(Locale.US, locKd.deserializeKey("en_US", ctxt));

        StdKeyDeserializer currKd = StdKeyDeserializer.forType(Currency.class);
        Assert.assertEquals(Currency.getInstance("USD"), currKd.deserializeKey("USD", ctxt));
    }

    @Test
    public void testDatesAndCalendar() throws Exception {
        StdKeyDeserializer dateKd = StdKeyDeserializer.forType(Date.class);
        Assert.assertNotNull(dateKd.deserializeKey("2020-01-01T00:00:00.000+0000", ctxt));

        StdKeyDeserializer calKd = StdKeyDeserializer.forType(Calendar.class);
        Assert.assertNotNull(calKd.deserializeKey("2020-01-01T00:00:00.000+0000", ctxt));
    }

    @Test
    public void testUUIDUriUrlAndClass() throws Exception {
        UUID uuid = UUID.randomUUID();
        StdKeyDeserializer uuidKd = StdKeyDeserializer.forType(UUID.class);
        Assert.assertEquals(uuid, uuidKd.deserializeKey(uuid.toString(), ctxt));

        StdKeyDeserializer uriKd = StdKeyDeserializer.forType(URI.class);
        Assert.assertEquals(URI.create("http://example.com"), uriKd.deserializeKey("http://example.com", ctxt));

        StdKeyDeserializer urlKd = StdKeyDeserializer.forType(URL.class);
        Assert.assertEquals(new URL("http://example.com"), urlKd.deserializeKey("http://example.com", ctxt));

        StdKeyDeserializer classKd = StdKeyDeserializer.forType(Class.class);
        Assert.assertEquals(String.class, classKd.deserializeKey("java.lang.String", ctxt));
    }

    @Test
    public void testUrlFailure() throws Exception {
        StdKeyDeserializer urlKd = StdKeyDeserializer.forType(URL.class);
        try {
            urlKd.deserializeKey("bad url text", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
    }

    @Test
    public void testClassFailure() throws Exception {
        StdKeyDeserializer classKd = StdKeyDeserializer.forType(Class.class);
        try {
            classKd.deserializeKey("non.existent.Class", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
    }

    @Test
    public void testStringKD() throws Exception {
        StdKeyDeserializer.StringKD kdStr = StdKeyDeserializer.StringKD.forType(String.class);
        Assert.assertEquals("test", kdStr.deserializeKey("test", ctxt));

        StdKeyDeserializer.StringKD kdObj = StdKeyDeserializer.StringKD.forType(Object.class);
        Assert.assertEquals("test", kdObj.deserializeKey("test", ctxt));

        StdKeyDeserializer.StringKD kdCustom = StdKeyDeserializer.StringKD.forType(CharSequence.class);
        Assert.assertEquals("test", kdCustom.deserializeKey("test", ctxt));
    }

    @Test
    public void testDelegatingKD() throws Exception {
        JsonDeserializer<Integer> deser = new JsonDeserializer<Integer>() {
            @Override
            public Integer deserialize(JsonParser p, DeserializationContext ctxt) {
                return 42;
            }
        };
        StdKeyDeserializer.DelegatingKD dkd = new StdKeyDeserializer.DelegatingKD(Integer.class, deser);
        Assert.assertEquals(Integer.class, dkd.getKeyClass());
        Assert.assertNull(dkd.deserializeKey(null, ctxt));
        Assert.assertEquals(42, dkd.deserializeKey("dummy", ctxt));

        JsonDeserializer<Integer> nullDeser = new JsonDeserializer<Integer>() {
            @Override
            public Integer deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };
        StdKeyDeserializer.DelegatingKD dkdNull = new StdKeyDeserializer.DelegatingKD(Integer.class, nullDeser);
        try {
            dkdNull.deserializeKey("dummy", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}

        JsonDeserializer<Integer> errDeser = new JsonDeserializer<Integer>() {
            @Override
            public Integer deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw new IOException("fail");
            }
        };
        StdKeyDeserializer.DelegatingKD dkdErr = new StdKeyDeserializer.DelegatingKD(Integer.class, errDeser);
        try {
            dkdErr.deserializeKey("dummy", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}
    }

    @Test
    public void testEnumKD() throws Exception {
        EnumResolver er = EnumResolver.constructUnsafe(TestEnum.class, ctxt.getAnnotationIntrospector());
        StdKeyDeserializer.EnumKD enumKd = new StdKeyDeserializer.EnumKD(er, null);
        Assert.assertEquals(TestEnum.A, enumKd.deserializeKey("A", ctxt));

        try {
            enumKd.deserializeKey("UNKNOWN", ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {}

        ObjectMapper mNull = new ObjectMapper();
        mNull.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL);
        DeserializationContext ctxtNull = mNull.getDeserializationContext();
        Assert.assertNull(enumKd.deserializeKey("UNKNOWN", ctxtNull));

        ObjectMapper mToString = new ObjectMapper();
        mToString.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        DeserializationContext ctxtToString = mToString.getDeserializationContext();
        Assert.assertEquals(TestEnum.A, enumKd.deserializeKey("custom_A", ctxtToString));
    }

    @Test
    public void testStringCtorKeyDeserializer() throws Exception {
        Constructor<?> ctor = StringCtorClass.class.getDeclaredConstructor(String.class);
        StdKeyDeserializer.StringCtorKeyDeserializer kd = new StdKeyDeserializer.StringCtorKeyDeserializer(ctor);
        Object res = kd.deserializeKey("foo", ctxt);
        Assert.assertTrue(res instanceof StringCtorClass);
        Assert.assertEquals("foo", ((StringCtorClass) res).val);
    }

    @Test
    public void testStringFactoryKeyDeserializer() throws Exception {
        Method method = StringFactoryClass.class.getDeclaredMethod("valueOf", String.class);
        StdKeyDeserializer.StringFactoryKeyDeserializer kd = new StdKeyDeserializer.StringFactoryKeyDeserializer(method);
        Object res = kd.deserializeKey("bar", ctxt);
        Assert.assertTrue(res instanceof StringFactoryClass);
        Assert.assertEquals("bar", ((StringFactoryClass) res).val);
    }

    @Test
    public void testUnknownKind() {
        StdKeyDeserializer kd = new StdKeyDeserializer(999, Object.class);
        try {
            kd.deserializeKey("test", ctxt);
            Assert.fail();
        } catch (Exception e) {}
    }

    @Test
    public void testGetKeyClass() {
        StdKeyDeserializer kd = StdKeyDeserializer.forType(Integer.class);
        Assert.assertEquals(Integer.class, kd.getKeyClass());
    }
}
