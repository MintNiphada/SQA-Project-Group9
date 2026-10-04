package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateTimeSerializerBaseTest {

    static class DummyDateTimeSerializer extends DateTimeSerializerBase<Date> {
        public DummyDateTimeSerializer() {
            this(null, null);
        }

        public DummyDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new DummyDateTimeSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return (value == null) ? 0L : value.getTime();
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        }

        public Boolean getUseTimestamp() {
            return _useTimestamp;
        }

        public DateFormat getCustomFormat() {
            return _customFormat;
        }
    }

    private ObjectMapper mapper;
    private SerializerProvider provider;
    private DummyDateTimeSerializer defaultSerializer;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        provider = mapper.getSerializerProviderInstance();
        defaultSerializer = new DummyDateTimeSerializer();
    }

    @Test
    public void testIsEmptyDeprecated() {
        Assert.assertTrue(defaultSerializer.isEmpty(null));
        Assert.assertTrue(defaultSerializer.isEmpty(new Date(0L)));
        Assert.assertFalse(defaultSerializer.isEmpty(new Date(123456789L)));
    }

    @Test
    public void testIsEmptyWithSerializerProvider() {
        Assert.assertTrue(defaultSerializer.isEmpty(provider, null));
        Assert.assertTrue(defaultSerializer.isEmpty(provider, new Date(0L)));
        Assert.assertFalse(defaultSerializer.isEmpty(provider, new Date(123456789L)));
    }

    @Test
    public void testAsTimestampExplicitBoolean() {
        DummyDateTimeSerializer serTrue = new DummyDateTimeSerializer(Boolean.TRUE, null);
        Assert.assertTrue(serTrue._asTimestamp(null));
        Assert.assertTrue(serTrue._asTimestamp(provider));

        DummyDateTimeSerializer serFalse = new DummyDateTimeSerializer(Boolean.FALSE, null);
        Assert.assertFalse(serFalse._asTimestamp(null));
        Assert.assertFalse(serFalse._asTimestamp(provider));
    }

    @Test
    public void testAsTimestampWithCustomFormat() {
        DummyDateTimeSerializer serCustom = new DummyDateTimeSerializer(null, new SimpleDateFormat("yyyy-MM-dd"));
        Assert.assertFalse(serCustom._asTimestamp(null));
        Assert.assertFalse(serCustom._asTimestamp(provider));
    }

    @Test
    public void testAsTimestampDefaultsWithProvider() {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        SerializerProvider provTrue = mapper.getSerializerProviderInstance();
        Assert.assertTrue(defaultSerializer._asTimestamp(provTrue));

        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SerializerProvider provFalse = mapper.getSerializerProviderInstance();
        Assert.assertFalse(defaultSerializer._asTimestamp(provFalse));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAsTimestampNullProviderThrows() {
        defaultSerializer._asTimestamp(null);
    }

    @Test
    public void testGetSchema() {
        DummyDateTimeSerializer serNumber = new DummyDateTimeSerializer(Boolean.TRUE, null);
        JsonNode numberSchema = serNumber.getSchema(provider, Date.class);
        Assert.assertEquals("number", numberSchema.get("type").asText());

        DummyDateTimeSerializer serString = new DummyDateTimeSerializer(Boolean.FALSE, null);
        JsonNode stringSchema = serString.getSchema(provider, Date.class);
        Assert.assertEquals("string", stringSchema.get("type").asText());
    }

    @Test
    public void testCreateContextualNullProperty() throws JsonMappingException {
        JsonSerializer<?> result = defaultSerializer.createContextual(provider, null);
        Assert.assertSame(defaultSerializer, result);
    }

    @Test
    public void testCreateContextualNullFormat() throws JsonMappingException {
        BeanProperty prop = new BeanProperty.Std(
                PropertyName.construct("test"),
                TypeFactory.defaultInstance().constructType(Date.class),
                null,
                null,
                null,
                PropertyMetadata.STD_OPTIONAL
        ) {
            @Override
            public AnnotatedMember getMember() {
                return null;
            }
        };

        SerializerProvider customProvider = mapper.getSerializerProviderInstance();
        JsonSerializer<?> result = defaultSerializer.createContextual(customProvider, prop);
        Assert.assertSame(defaultSerializer, result);
    }

    @Test
    public void testCreateContextualNumericFormat() throws JsonMappingException {
        final JsonFormat.Value formatValue = new JsonFormat.Value().withShape(JsonFormat.Shape.NUMBER);
        AnnotationIntrospector intr = new AnnotationIntrospector() {
            @Override
            public com.fasterxml.jackson.core.Version version() {
                return com.fasterxml.jackson.core.Version.unknownVersion();
            }

            @Override
            public JsonFormat.Value findFormat(Annotated memberOrClass) {
                return formatValue;
            }
        };

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setAnnotationIntrospector(intr);
        SerializerProvider sp = customMapper.getSerializerProviderInstance();

        BeanProperty prop = new BeanProperty.Std(
                PropertyName.construct("test"),
                TypeFactory.defaultInstance().constructType(Date.class),
                null,
                null,
                null,
                PropertyMetadata.STD_OPTIONAL
        );

        JsonSerializer<?> result = defaultSerializer.createContextual(sp, prop);
        Assert.assertTrue(result instanceof DummyDateTimeSerializer);
        DummyDateTimeSerializer dummyResult = (DummyDateTimeSerializer) result;
        Assert.assertEquals(Boolean.TRUE, dummyResult.getUseTimestamp());
        Assert.assertNull(dummyResult.getCustomFormat());
    }

    @Test
    public void testCreateContextualStringFormatAllOptions() throws JsonMappingException {
        final TimeZone tz = TimeZone.getTimeZone("PST");
        final Locale loc = Locale.GERMANY;
        final String pattern = "yyyy/MM/dd HH:mm:ss";
        final JsonFormat.Value formatValue = new JsonFormat.Value(pattern, JsonFormat.Shape.STRING, loc, tz);

        AnnotationIntrospector intr = new AnnotationIntrospector() {
            @Override
            public com.fasterxml.jackson.core.Version version() {
                return com.fasterxml.jackson.core.Version.unknownVersion();
            }

            @Override
            public JsonFormat.Value findFormat(Annotated memberOrClass) {
                return formatValue;
            }
        };

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setAnnotationIntrospector(intr);
        SerializerProvider sp = customMapper.getSerializerProviderInstance();

        BeanProperty prop = new BeanProperty.Std(
                PropertyName.construct("test"),
                TypeFactory.defaultInstance().constructType(Date.class),
                null,
                null,
                null,
                PropertyMetadata.STD_OPTIONAL
        );

        JsonSerializer<?> result = defaultSerializer.createContextual(sp, prop);
        Assert.assertTrue(result instanceof DummyDateTimeSerializer);
        DummyDateTimeSerializer dummyResult = (DummyDateTimeSerializer) result;
        Assert.assertEquals(Boolean.FALSE, dummyResult.getUseTimestamp());
        Assert.assertTrue(dummyResult.getCustomFormat() instanceof SimpleDateFormat);

        SimpleDateFormat sdf = (SimpleDateFormat) dummyResult.getCustomFormat();
        Assert.assertEquals(pattern, sdf.toPattern());
        Assert.assertEquals(tz, sdf.getTimeZone());
    }

    @Test
    public void testCreateContextualStringFormatDefaultPatternLocaleTimezone() throws JsonMappingException {
        final JsonFormat.Value formatValue = new JsonFormat.Value().withShape(JsonFormat.Shape.STRING);

        AnnotationIntrospector intr = new AnnotationIntrospector() {
            @Override
            public com.fasterxml.jackson.core.Version version() {
                return com.fasterxml.jackson.core.Version.unknownVersion();
            }

            @Override
            public JsonFormat.Value findFormat(Annotated memberOrClass) {
                return formatValue;
            }
        };

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setAnnotationIntrospector(intr);
        SerializerProvider sp = customMapper.getSerializerProviderInstance();

        BeanProperty prop = new BeanProperty.Std(
                PropertyName.construct("test"),
                TypeFactory.defaultInstance().constructType(Date.class),
                null,
                null,
                null,
                PropertyMetadata.STD_OPTIONAL
        );

        JsonSerializer<?> result = defaultSerializer.createContextual(sp, prop);
        Assert.assertTrue(result instanceof DummyDateTimeSerializer);
        DummyDateTimeSerializer dummyResult = (DummyDateTimeSerializer) result;
        Assert.assertEquals(Boolean.FALSE, dummyResult.getUseTimestamp());
        Assert.assertTrue(dummyResult.getCustomFormat() instanceof SimpleDateFormat);

        SimpleDateFormat sdf = (SimpleDateFormat) dummyResult.getCustomFormat();
        Assert.assertEquals(StdDateFormat.DATE_FORMAT_STR_ISO8601, sdf.toPattern());
        Assert.assertEquals(sp.getTimeZone(), sdf.getTimeZone());
    }

    @Test
    public void testCreateContextualOtherShape() throws JsonMappingException {
        final JsonFormat.Value formatValue = new JsonFormat.Value().withShape(JsonFormat.Shape.ARRAY);

        AnnotationIntrospector intr = new AnnotationIntrospector() {
            @Override
            public com.fasterxml.jackson.core.Version version() {
                return com.fasterxml.jackson.core.Version.unknownVersion();
            }

            @Override
            public JsonFormat.Value findFormat(Annotated memberOrClass) {
                return formatValue;
            }
        };

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setAnnotationIntrospector(intr);
        SerializerProvider sp = customMapper.getSerializerProviderInstance();

        BeanProperty prop = new BeanProperty.Std(
                PropertyName.construct("test"),
                TypeFactory.defaultInstance().constructType(Date.class),
                null,
                null,
                null,
                PropertyMetadata.STD_OPTIONAL
        );

        JsonSerializer<?> result = defaultSerializer.createContextual(sp, prop);
        Assert.assertSame(defaultSerializer, result);
    }

    @Test
    public void testAcceptJsonFormatVisitorAsNumber() throws JsonMappingException {
        final boolean[] visitedInt = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(provider) {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        Assert.assertEquals(JsonParser.NumberType.LONG, type);
                    }

                    @Override
                    public void format(JsonValueFormat format) {
                        Assert.assertEquals(JsonValueFormat.UTC_MILLISEC, format);
                        visitedInt[0] = true;
                    }
                };
            }
        };

        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(Boolean.TRUE, null);
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Date.class));
        Assert.assertTrue(visitedInt[0]);
    }

    @Test
    public void testAcceptJsonFormatVisitorAsString() throws JsonMappingException {
        final boolean[] visitedString = new boolean[1];
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(provider) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void format(JsonValueFormat format) {
                        Assert.assertEquals(JsonValueFormat.DATE_TIME, format);
                        visitedString[0] = true;
                    }
                };
            }
        };

        DummyDateTimeSerializer ser = new DummyDateTimeSerializer(Boolean.FALSE, null);
        ser.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Date.class));
        Assert.assertTrue(visitedString[0]);
    }
}
