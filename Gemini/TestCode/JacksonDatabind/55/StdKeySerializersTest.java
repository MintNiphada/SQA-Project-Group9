package com.fasterxml.jackson.databind.ser.std;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;

public class StdKeySerializersTest {

    private enum TestEnum {
        FOO {
            @Override
            public String toString() {
                return "custom_foo";
            }
        },
        BAR
    }

    private static class CustomObject {
        @Override
        public String toString() {
            return "custom_object";
        }
    }

    @Test
    public void testGetStdKeySerializerNullAndObject() {
        JsonSerializer<Object> ser1 = StdKeySerializers.getStdKeySerializer(null, null, false);
        Assert.assertNotNull(ser1);
        Assert.assertTrue(ser1 instanceof StdKeySerializers.Dynamic);

        JsonSerializer<Object> ser2 = StdKeySerializers.getStdKeySerializer(null, Object.class, false);
        Assert.assertNotNull(ser2);
        Assert.assertTrue(ser2 instanceof StdKeySerializers.Dynamic);
    }

    @Test
    public void testGetStdKeySerializerString() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, String.class, false);
        Assert.assertNotNull(ser);
        Assert.assertTrue(ser instanceof StdKeySerializers.StringKeySerializer);
    }

    @Test
    public void testGetStdKeySerializerPrimitivesAndNumbers() {
        JsonSerializer<Object> serPrim = StdKeySerializers.getStdKeySerializer(null, int.class, false);
        Assert.assertNotNull(serPrim);
        Assert.assertTrue(serPrim instanceof StdKeySerializer);

        JsonSerializer<Object> serNum = StdKeySerializers.getStdKeySerializer(null, Long.class, false);
        Assert.assertNotNull(serNum);
        Assert.assertTrue(serNum instanceof StdKeySerializer);
    }

    @Test
    public void testGetStdKeySerializerClassAndDateAndCalendarAndUUID() {
        JsonSerializer<Object> serClass = StdKeySerializers.getStdKeySerializer(null, Class.class, false);
        Assert.assertNotNull(serClass);
        Assert.assertTrue(serClass instanceof StdKeySerializers.Default);

        JsonSerializer<Object> serDate = StdKeySerializers.getStdKeySerializer(null, Date.class, false);
        Assert.assertNotNull(serDate);
        Assert.assertTrue(serDate instanceof StdKeySerializers.Default);

        JsonSerializer<Object> serCal = StdKeySerializers.getStdKeySerializer(null, Calendar.class, false);
        Assert.assertNotNull(serCal);
        Assert.assertTrue(serCal instanceof StdKeySerializers.Default);

        JsonSerializer<Object> serUUID = StdKeySerializers.getStdKeySerializer(null, UUID.class, false);
        Assert.assertNotNull(serUUID);
        Assert.assertTrue(serUUID instanceof StdKeySerializers.Default);
    }

    @Test
    public void testGetStdKeySerializerFallbackAndNull() {
        JsonSerializer<Object> serDefault = StdKeySerializers.getStdKeySerializer(null, CustomObject.class, true);
        Assert.assertNotNull(serDefault);
        Assert.assertTrue(serDefault instanceof StdKeySerializer);

        JsonSerializer<Object> serNull = StdKeySerializers.getStdKeySerializer(null, CustomObject.class, false);
        Assert.assertNull(serNull);
    }

    @Test
    public void testGetFallbackKeySerializer() {
        JsonSerializer<Object> serEnumClass = StdKeySerializers.getFallbackKeySerializer(null, Enum.class);
        Assert.assertNotNull(serEnumClass);
        Assert.assertTrue(serEnumClass instanceof StdKeySerializers.Dynamic);

        JsonSerializer<Object> serEnumType = StdKeySerializers.getFallbackKeySerializer(null, TestEnum.class);
        Assert.assertNotNull(serEnumType);
        Assert.assertTrue(serEnumType instanceof StdKeySerializers.Default);

        JsonSerializer<Object> serNull = StdKeySerializers.getFallbackKeySerializer(null, null);
        Assert.assertNotNull(serNull);
        Assert.assertTrue(serNull instanceof StdKeySerializer);

        JsonSerializer<Object> serOther = StdKeySerializers.getFallbackKeySerializer(null, CustomObject.class);
        Assert.assertNotNull(serOther);
        Assert.assertTrue(serOther instanceof StdKeySerializer);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testGetDefault() {
        JsonSerializer<Object> ser = StdKeySerializers.getDefault();
        Assert.assertNotNull(ser);
        Assert.assertTrue(ser instanceof StdKeySerializer);
    }

    @Test
    public void testStringKeySerializer() throws Exception {
        StdKeySerializers.StringKeySerializer ser = new StdKeySerializers.StringKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator g = new JsonFactory().createGenerator(sw);
        g.writeStartObject();
        ser.serialize("testKey", g, null);
        g.writeNumber(1);
        g.writeEndObject();
        g.close();
        Assert.assertEquals("{\"testKey\":1}", sw.toString());
    }

    @Test
    public void testDefaultSerializerDate() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_DATE, Date.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        g.writeStartObject();
        Date date = new Date(0L);
        ser.serialize(date, g, prov);
        g.writeNumber(1);
        g.writeEndObject();
        g.close();
        Assert.assertTrue(sw.toString().contains("1970"));
    }

    @Test
    public void testDefaultSerializerCalendar() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_CALENDAR, Calendar.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        g.writeStartObject();
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(0L);
        ser.serialize(cal, g, prov);
        g.writeNumber(1);
        g.writeEndObject();
        g.close();
        Assert.assertTrue(sw.toString().contains("1970"));
    }

    @Test
    public void testDefaultSerializerClass() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_CLASS, Class.class);
        StringWriter sw = new StringWriter();
        JsonGenerator g = new JsonFactory().createGenerator(sw);
        g.writeStartObject();
        ser.serialize(String.class, g, null);
        g.writeNumber(1);
        g.writeEndObject();
        g.close();
        Assert.assertEquals("{\"java.lang.String\":1}", sw.toString());
    }

    @Test
    public void testDefaultSerializerEnumName() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_ENUM, TestEnum.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        g.writeStartObject();
        ser.serialize(TestEnum.FOO, g, prov);
        g.writeNumber(1);
        g.writeEndObject();
        g.close();
        Assert.assertEquals("{\"FOO\":1}", sw.toString());
    }

    @Test
    public void testDefaultSerializerEnumToString() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_ENUM, TestEnum.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        g.writeStartObject();
        ser.serialize(TestEnum.FOO, g, prov);
        g.writeNumber(1);
        g.writeEndObject();
        g.close();
        Assert.assertEquals("{\"custom_foo\":1}", sw.toString());
    }

    @Test
    public void testDefaultSerializerToString() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_TO_STRING, UUID.class);
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
        StringWriter sw = new StringWriter();
        JsonGenerator g = new JsonFactory().createGenerator(sw);
        g.writeStartObject();
        ser.serialize(uuid, g, null);
        g.writeNumber(1);
        g.writeEndObject();
        g.close();
        Assert.assertEquals("{\"00000000-0000-0000-0000-000000000000\":1}", sw.toString());
    }

    @Test
    public void testDefaultSerializerUnknownTypeIdFallback() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(999, CustomObject.class);
        StringWriter sw = new StringWriter();
        JsonGenerator g = new JsonFactory().createGenerator(sw);
        g.writeStartObject();
        ser.serialize(new CustomObject(), g, null);
        g.writeNumber(1);
        g.writeEndObject();
        g.close();
        Assert.assertEquals("{\"custom_object\":1}", sw.toString());
    }

    @Test
    public void testDynamicSerializerCaching() throws Exception {
        StdKeySerializers.Dynamic ser = new StdKeySerializers.Dynamic();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        g.writeStartObject();
        ser.serialize("first", g, prov);
        g.writeNumber(1);
        ser.serialize("second", g, prov);
        g.writeNumber(2);
        ser.serialize(123, g, prov);
        g.writeNumber(3);
        g.writeEndObject();
        g.close();

        Assert.assertEquals("{\"first\":1,\"second\":2,\"123\":3}", sw.toString());
    }

    @Test
    public void testDynamicSerializationReadResolve() throws Exception {
        StdKeySerializers.Dynamic ser = new StdKeySerializers.Dynamic();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ser);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        Assert.assertNotNull(deserialized);
        Assert.assertTrue(deserialized instanceof StdKeySerializers.Dynamic);

        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator g = mapper.getFactory().createGenerator(sw);
        g.writeStartObject();
        ((StdKeySerializers.Dynamic) deserialized).serialize("keyAfterDeserialization", g, prov);
        g.writeNumber(10);
        g.writeEndObject();
        g.close();

        Assert.assertEquals("{\"keyAfterDeserialization\":10}", sw.toString());
    }
}
