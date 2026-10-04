package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class DateTimeSerializerBaseTest {

    private static class ConcreteDateTimeSerializer extends DateTimeSerializerBase<Date> {
        private static final long serialVersionUID = 1L;

        public ConcreteDateTimeSerializer() {
            this(null, null);
        }

        public ConcreteDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        public Boolean getUseTimestamp() {
            return _useTimestamp;
        }

        public DateFormat getCustomFormat() {
            return _customFormat;
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new ConcreteDateTimeSerializer(timestamp, customFormat);
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
                gen.writeString(value.toString());
            }
        }
    }

    private ObjectMapper mapper;
    private SerializerProvider provider;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        provider = mapper.getSerializerProviderInstance();
    }

    @Test
    public void testIsEmptyWithNull() {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        Assert.assertTrue(serializer.isEmpty(null));
        Assert.assertTrue(serializer.isEmpty(provider, null));
    }

    @Test
    public void testIsEmptyWithZeroTimestamp() {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        Date date = new Date(0L);
        Assert.assertTrue(serializer.isEmpty(date));
        Assert.assertTrue(serializer.isEmpty(provider, date));
    }

    @Test
    public void testIsEmptyWithNonZeroTimestamp() {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        Date date = new Date(123456789L);
        Assert.assertFalse(serializer.isEmpty(date));
        Assert.assertFalse(serializer.isEmpty(provider, date));
    }

    @Test
    public void testAsTimestampExplicitBoolean() {
        ConcreteDateTimeSerializer serTrue = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        ConcreteDateTimeSerializer serFalse = new ConcreteDateTimeSerializer(Boolean.FALSE, null);

        Assert.assertTrue(serTrue._asTimestamp(null));
        Assert.assertTrue(serTrue._asTimestamp(provider));
        Assert.assertFalse(serFalse._asTimestamp(null));
        Assert.assertFalse(serFalse._asTimestamp(provider));
    }

    @Test
    public void testAsTimestampWithCustomFormat() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(null, df);

        Assert.assertFalse(serializer._asTimestamp(null));
        Assert.assertFalse(serializer._asTimestamp(provider));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAsTimestampNullProviderThrowsException() {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(null, null);
        serializer._asTimestamp(null);
    }

    @Test
    public void testAsTimestampFollowsProviderConfig() {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(null, null);

        ObjectMapper mapperEnabled = new ObjectMapper();
        mapperEnabled.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Assert.assertTrue(serializer._asTimestamp(mapperEnabled.getSerializerProviderInstance()));

        ObjectMapper mapperDisabled = new ObjectMapper();
        mapperDisabled.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Assert.assertFalse(serializer._asTimestamp(mapperDisabled.getSerializerProviderInstance()));
    }

    @Test
    public void testGetSchema() {
        ConcreteDateTimeSerializer serTimestamp = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        JsonNode numberNode = serTimestamp.getSchema(provider, (Type) null);
        Assert.assertEquals("number", numberNode.get("type").asText());

        ConcreteDateTimeSerializer serString = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        JsonNode stringNode = serString.getSchema(provider, (Type) null);
        Assert.assertEquals("string", stringNode.get("type").asText());
    }

    @Test
    public void testAcceptJsonFormatVisitorAsNumber() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        JavaType javaType = TypeFactory.defaultInstance().constructType(Date.class);

        final boolean[] visitedLong = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(provider) {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        if (type == JsonParser.NumberType.LONG) {
                            visitedLong[0] = true;
                        }
                    }

                    @Override
                    public void format(JsonValueFormat format) {
                        if (format == JsonValueFormat.UTC_MILLISEC) {
                            visitedLong[0] = true;
                        }
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, javaType);
        Assert.assertTrue(visitedLong[0]);
    }

    @Test
    public void testAcceptJsonFormatVisitorAsString() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        JavaType javaType = TypeFactory.defaultInstance().constructType(Date.class);

        final boolean[] visitedString = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(provider) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void format(JsonValueFormat format) {
                        if (format == JsonValueFormat.DATE_TIME) {
                            visitedString[0] = true;
                        }
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, javaType);
        Assert.assertTrue(visitedString[0]);
    }

    @Test
    public void testCreateContextualWithNullProperty() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        JsonSerializer<?> contextual = serializer.createContextual(provider, null);
        Assert.assertSame(serializer, contextual);
    }

    private static class DummyBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date numericDate;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "GMT+2", locale = "fr_FR")
        public Date fullStringDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        public Date patternOnlyDate;

        @JsonFormat(locale = "de_DE")
        public Date localeOnlyDate;

        @JsonFormat(timezone = "PST")
        public Date timezoneOnlyDate;

        @JsonFormat(shape = JsonFormat.Shape.OBJECT)
        public Date objectDate;

        public Date noFormatDate;
    }

    private BeanProperty getBeanProperty(String fieldName) {
        JavaType type = mapper.constructType(DummyBean.class);
        BeanProperty.Std prop = null;
        for (AnnotatedMember member : mapper.getSerializationConfig().introspect(type).findProperties()) {
            if (member.getName().equals(fieldName)) {
                return new BeanProperty.Std(
                        PropertyName.construct(fieldName),
                        TypeFactory.defaultInstance().constructType(Date.class),
                        null,
                        member,
                        PropertyMetadata.STD_OPTIONAL
                );
            }
        }
        return prop;
    }

    @Test
    public void testCreateContextualNumericShape() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        BeanProperty prop = getBeanProperty("numericDate");
        JsonSerializer<?> contextual = serializer.createContextual(provider, prop);

        Assert.assertTrue(contextual instanceof ConcreteDateTimeSerializer);
        ConcreteDateTimeSerializer result = (ConcreteDateTimeSerializer) contextual;
        Assert.assertEquals(Boolean.TRUE, result.getUseTimestamp());
        Assert.assertNull(result.getCustomFormat());
    }

    @Test
    public void testCreateContextualFullStringFormat() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        BeanProperty prop = getBeanProperty("fullStringDate");
        JsonSerializer<?> contextual = serializer.createContextual(provider, prop);

        Assert.assertTrue(contextual instanceof ConcreteDateTimeSerializer);
        ConcreteDateTimeSerializer result = (ConcreteDateTimeSerializer) contextual;
        Assert.assertEquals(Boolean.FALSE, result.getUseTimestamp());
        Assert.assertNotNull(result.getCustomFormat());
        SimpleDateFormat sdf = (SimpleDateFormat) result.getCustomFormat();
        Assert.assertEquals("yyyy/MM/dd", sdf.toPattern());
        Assert.assertEquals(TimeZone.getTimeZone("GMT+2"), sdf.getTimeZone());
    }

    @Test
    public void testCreateContextualPatternOnly() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        BeanProperty prop = getBeanProperty("patternOnlyDate");
        JsonSerializer<?> contextual = serializer.createContextual(provider, prop);

        Assert.assertTrue(contextual instanceof ConcreteDateTimeSerializer);
        ConcreteDateTimeSerializer result = (ConcreteDateTimeSerializer) contextual;
        Assert.assertEquals(Boolean.FALSE, result.getUseTimestamp());
        Assert.assertNotNull(result.getCustomFormat());
        SimpleDateFormat sdf = (SimpleDateFormat) result.getCustomFormat();
        Assert.assertEquals("yyyy-MM-dd", sdf.toPattern());
        Assert.assertEquals(provider.getTimeZone(), sdf.getTimeZone());
    }

    @Test
    public void testCreateContextualLocaleOnly() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        BeanProperty prop = getBeanProperty("localeOnlyDate");
        JsonSerializer<?> contextual = serializer.createContextual(provider, prop);

        Assert.assertTrue(contextual instanceof ConcreteDateTimeSerializer);
        ConcreteDateTimeSerializer result = (ConcreteDateTimeSerializer) contextual;
        Assert.assertEquals(Boolean.FALSE, result.getUseTimestamp());
        Assert.assertNotNull(result.getCustomFormat());
    }

    @Test
    public void testCreateContextualTimezoneOnly() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        BeanProperty prop = getBeanProperty("timezoneOnlyDate");
        JsonSerializer<?> contextual = serializer.createContextual(provider, prop);

        Assert.assertTrue(contextual instanceof ConcreteDateTimeSerializer);
        ConcreteDateTimeSerializer result = (ConcreteDateTimeSerializer) contextual;
        Assert.assertEquals(Boolean.FALSE, result.getUseTimestamp());
        Assert.assertNotNull(result.getCustomFormat());
        SimpleDateFormat sdf = (SimpleDateFormat) result.getCustomFormat();
        Assert.assertEquals(TimeZone.getTimeZone("PST"), sdf.getTimeZone());
    }

    @Test
    public void testCreateContextualObjectShape() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        BeanProperty prop = getBeanProperty("objectDate");
        JsonSerializer<?> contextual = serializer.createContextual(provider, prop);
        Assert.assertSame(serializer, contextual);
    }

    @Test
    public void testCreateContextualNoFormat() throws Exception {
        ConcreteDateTimeSerializer serializer = new ConcreteDateTimeSerializer();
        BeanProperty prop = getBeanProperty("noFormatDate");
        JsonSerializer<?> contextual = serializer.createContextual(provider, prop);
        Assert.assertSame(serializer, contextual);
    }
}
