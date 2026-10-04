package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public class DateTimeSerializerBaseTest {

    static class TestDateTimeSerializer extends DateTimeSerializerBase<Date> {
        public TestDateTimeSerializer() {
            this(null, null);
        }

        public TestDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new TestDateTimeSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return value == null ? 0L : value.getTime();
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (_asTimestamp(serializers)) {
                gen.writeNumber(_timestamp(value));
            } else {
                _serializeAsString(value, gen, serializers);
            }
        }
    }

    @Test
    public void testIsEmpty() {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        Assert.assertFalse(ser.isEmpty(null, new Date(0L)));
        Assert.assertFalse(ser.isEmpty(null, new Date(1000L)));
        Assert.assertFalse(ser.isEmpty(null, null));
    }

    @Test
    public void testAsTimestampWithExplicitUseTimestamp() {
        TestDateTimeSerializer serTrue = new TestDateTimeSerializer(Boolean.TRUE, null);
        Assert.assertTrue(serTrue._asTimestamp(null));

        TestDateTimeSerializer serFalse = new TestDateTimeSerializer(Boolean.FALSE, null);
        Assert.assertFalse(serFalse._asTimestamp(null));
    }

    @Test
    public void testAsTimestampWithCustomFormat() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        TestDateTimeSerializer ser = new TestDateTimeSerializer(null, df);
        Assert.assertFalse(ser._asTimestamp(null));
    }

    @Test
    public void testAsTimestampWithProvider() {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Assert.assertTrue(ser._asTimestamp(prov));

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Assert.assertFalse(ser._asTimestamp(prov));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAsTimestampNullProviderThrows() {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ser._asTimestamp(null);
    }

    @Test
    public void testGetSchema() {
        TestDateTimeSerializer serNum = new TestDateTimeSerializer(Boolean.TRUE, null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonNode schemaNum = serNum.getSchema(prov, Date.class);
        Assert.assertEquals("number", schemaNum.get("type").asText());

        TestDateTimeSerializer serStr = new TestDateTimeSerializer(Boolean.FALSE, null);
        JsonNode schemaStr = serStr.getSchema(prov, Date.class);
        Assert.assertEquals("string", schemaStr.get("type").asText());
    }

    @Test
    public void testAcceptJsonFormatVisitor() throws Exception {
        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        Mockito.when(visitor.getProvider()).thenReturn(prov);
        Mockito.when(prov.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).thenReturn(true);

        JsonIntegerFormatVisitor intVisitor = Mockito.mock(JsonIntegerFormatVisitor.class);
        Mockito.when(visitor.expectIntegerFormat(Mockito.any(JavaType.class))).thenReturn(intVisitor);

        TestDateTimeSerializer ser = new TestDateTimeSerializer(Boolean.TRUE, null);
        ser.acceptJsonFormatVisitor(visitor, null);
        Mockito.verify(visitor).expectIntegerFormat(Mockito.any());
        Mockito.verify(intVisitor).numberType(JsonParser.NumberType.LONG);
        Mockito.verify(intVisitor).format(JsonValueFormat.UTC_MILLISEC);

        JsonStringFormatVisitor strVisitor = Mockito.mock(JsonStringFormatVisitor.class);
        Mockito.when(visitor.expectStringFormat(Mockito.any(JavaType.class))).thenReturn(strVisitor);

        TestDateTimeSerializer serStr = new TestDateTimeSerializer(Boolean.FALSE, null);
        serStr.acceptJsonFormatVisitor(visitor, null);
        Mockito.verify(visitor).expectStringFormat(Mockito.any());
        Mockito.verify(strVisitor).format(JsonValueFormat.DATE_TIME);
    }

    @Test
    public void testSerializeAsStringDefault() throws Exception {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writer(ser).writeValueAsString(new Date(0L));
        Assert.assertNotNull(json);
    }

    @Test
    public void testSerializeAsStringCustomFormatAndReuse() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        TestDateTimeSerializer ser = new TestDateTimeSerializer(null, df);

        ObjectMapper mapper = new ObjectMapper();
        Date d1 = new Date(0L);
        String json1 = mapper.writer(ser).writeValueAsString(d1);
        Assert.assertEquals("\"1970/01/01\"", json1);

        String json2 = mapper.writer(ser).writeValueAsString(d1);
        Assert.assertEquals("\"1970/01/01\"", json2);

        Field field = DateTimeSerializerBase.class.getDeclaredField("_reusedCustomFormat");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        AtomicReference<DateFormat> ref = (AtomicReference<DateFormat>) field.get(ser);
        ref.set(null);
        String json3 = mapper.writer(ser).writeValueAsString(d1);
        Assert.assertEquals("\"1970/01/01\"", json3);
    }

    @Test
    public void testCreateContextualNullProperty() throws Exception {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        JsonSerializer<?> result = ser.createContextual(null, null);
        Assert.assertSame(ser, result);
    }

    @Test
    public void testCreateContextualNoFormat() throws Exception {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(null);

        JsonSerializer<?> result = ser.createContextual(prov, prop);
        Assert.assertSame(ser, result);
    }

    @Test
    public void testCreateContextualShapeNumeric() throws Exception {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        JsonFormat.Value format = JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER);
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(format);

        DateTimeSerializerBase<?> result = (DateTimeSerializerBase<?>) ser.createContextual(prov, prop);
        Assert.assertNotSame(ser, result);
        Assert.assertEquals(Boolean.TRUE, result._useTimestamp);
        Assert.assertNull(result._customFormat);
    }

    @Test
    public void testCreateContextualPatternWithLocaleAndTZ() throws Exception {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        JsonFormat.Value format = new JsonFormat.Value()
                .withPattern("yyyy_MM_dd")
                .withLocale(Locale.GERMANY)
                .withTimeZone(TimeZone.getTimeZone("GMT+2"));
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(format);

        DateTimeSerializerBase<?> result = (DateTimeSerializerBase<?>) ser.createContextual(prov, prop);
        Assert.assertNotSame(ser, result);
        Assert.assertEquals(Boolean.FALSE, result._useTimestamp);
        Assert.assertTrue(result._customFormat instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat) result._customFormat;
        Assert.assertEquals("yyyy_MM_dd", sdf.toPattern());
        Assert.assertEquals(TimeZone.getTimeZone("GMT+2"), sdf.getTimeZone());
    }

    @Test
    public void testCreateContextualPatternWithoutLocaleAndTZ() throws Exception {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        JsonFormat.Value format = new JsonFormat.Value().withPattern("yyyy-MM-dd");
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(format);

        DateTimeSerializerBase<?> result = (DateTimeSerializerBase<?>) ser.createContextual(prov, prop);
        Assert.assertNotSame(ser, result);
        Assert.assertEquals(Boolean.FALSE, result._useTimestamp);
        Assert.assertTrue(result._customFormat instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat) result._customFormat;
        Assert.assertEquals("yyyy-MM-dd", sdf.toPattern());
        Assert.assertEquals(prov.getTimeZone(), sdf.getTimeZone());
    }

    @Test
    public void testCreateContextualNoLocaleNoTZNotString() throws Exception {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        JsonFormat.Value format = JsonFormat.Value.forShape(JsonFormat.Shape.ANY);
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(format);

        JsonSerializer<?> result = ser.createContextual(prov, prop);
        Assert.assertSame(ser, result);
    }

    @Test
    public void testCreateContextualStdDateFormat() throws Exception {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new StdDateFormat());
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        JsonFormat.Value format = new JsonFormat.Value()
                .withLocale(Locale.FRANCE)
                .withTimeZone(TimeZone.getTimeZone("PST"));
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(format);

        DateTimeSerializerBase<?> result = (DateTimeSerializerBase<?>) ser.createContextual(prov, prop);
        Assert.assertNotSame(ser, result);
        Assert.assertEquals(Boolean.FALSE, result._useTimestamp);
        Assert.assertTrue(result._customFormat instanceof StdDateFormat);
    }

    @Test
    public void testCreateContextualSimpleDateFormatBranch() throws Exception {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat origDf = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        origDf.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(origDf);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JsonFormat.Value format = new JsonFormat.Value()
                .withLocale(Locale.ITALY)
                .withTimeZone(TimeZone.getTimeZone("GMT+5"));
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(format);

        DateTimeSerializerBase<?> result = (DateTimeSerializerBase<?>) ser.createContextual(prov, prop);
        Assert.assertNotSame(ser, result);
        Assert.assertEquals(Boolean.FALSE, result._useTimestamp);
        SimpleDateFormat resDf = (SimpleDateFormat) result._customFormat;
        Assert.assertEquals(TimeZone.getTimeZone("GMT+5"), resDf.getTimeZone());

        JsonFormat.Value formatNoLocale = new JsonFormat.Value()
                .withTimeZone(TimeZone.getTimeZone("GMT+3"));
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(formatNoLocale);

        DateTimeSerializerBase<?> result2 = (DateTimeSerializerBase<?>) ser.createContextual(prov, prop);
        SimpleDateFormat resDf2 = (SimpleDateFormat) result2._customFormat;
        Assert.assertEquals(TimeZone.getTimeZone("GMT+3"), resDf2.getTimeZone());
    }

    @Test
    public void testCreateContextualBadDateFormat() {
        TestDateTimeSerializer ser = new TestDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        DateFormat badDf = new DateFormat() {
            @Override
            public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
                return toAppendTo;
            }
            @Override
            public Date parse(String source, ParsePosition pos) {
                return null;
            }
        };
        mapper.setDateFormat(badDf);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JsonFormat.Value format = new JsonFormat.Value().withLocale(Locale.ITALY);
        Mockito.when(prop.findPropertyFormat(Mockito.any(), Mockito.any())).thenReturn(format);

        try {
            ser.createContextual(prov, prop);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("not a `SimpleDateFormat`"));
        }
    }
}
