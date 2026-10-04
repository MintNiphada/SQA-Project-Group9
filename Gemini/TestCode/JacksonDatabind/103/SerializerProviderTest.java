package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.ConfigOverrides;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.SerializerCache;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.impl.FailingSerializer;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.RootNameLookup;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SerializerProviderTest {

    private static class TestSerializerProvider extends SerializerProvider {
        public TestSerializerProvider() {
            super();
        }

        public TestSerializerProvider(SerializerProvider src, SerializationConfig config, SerializerFactory f) {
            super(src, config, f);
        }

        public TestSerializerProvider(SerializerProvider src) {
            super(src);
        }

        @Override
        public WritableObjectId findObjectId(Object forPojo, ObjectIdGenerator<?> generatorType) {
            return null;
        }

        @Override
        public JsonSerializer<Object> serializerInstance(Annotated annotated, Object serDef) {
            return null;
        }

        @Override
        public Object includeFilterInstance(BeanPropertyDefinition forProperty, Class<?> filterClass) {
            return null;
        }

        @Override
        public boolean includeFilterSuppressNulls(Object filter) {
            return false;
        }
    }

    private static class ResolvableContextualSerializer extends StdSerializer<Object>
            implements ResolvableSerializer, ContextualSerializer {
        boolean resolved = false;
        boolean contextualized = false;

        public ResolvableContextualSerializer() {
            super(Object.class);
        }

        @Override
        public void resolve(SerializerProvider provider) {
            resolved = true;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            contextualized = true;
            return this;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString("custom");
        }
    }

    private SerializationConfig config;
    private TestSerializerProvider blueprint;
    private TestSerializerProvider provider;

    @Before
    public void setUp() {
        BaseSettings base = new BaseSettings(null, new ObjectMapper().getAnnotationIntrospector(),
                null, TypeFactory.defaultInstance(), null, StdDateFormat.instance,
                null, Locale.getDefault(), TimeZone.getTimeZone("GMT"),
                com.fasterxml.jackson.core.Base64Variants.getDefaultVariant());
        config = new SerializationConfig(base, new StdSubtypeResolver(), new SimpleMixInResolver(null),
                new RootNameLookup(), new ConfigOverrides());
        blueprint = new TestSerializerProvider();
        provider = new TestSerializerProvider(blueprint, config, BeanSerializerFactory.instance);
    }

    @Test
    public void testConstructorsAndConfigAccessors() {
        TestSerializerProvider copyBlueprint = new TestSerializerProvider(blueprint);
        Assert.assertNull(copyBlueprint.getConfig());
        Assert.assertNull(copyBlueprint.getActiveView());
        Assert.assertNull(copyBlueprint.getGenerator());

        Assert.assertSame(config, provider.getConfig());
        Assert.assertNotNull(provider.getAnnotationIntrospector());
        Assert.assertNotNull(provider.getTypeFactory());
        Assert.assertNull(provider.getActiveView());
        Assert.assertNull(provider.getSerializationView());
        Assert.assertTrue(provider.canOverrideAccessModifiers());
        Assert.assertTrue(provider.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
        Assert.assertTrue(provider.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        Assert.assertFalse(provider.hasSerializationFeatures(1 << 30));
        Assert.assertNotNull(provider.getLocale());
        Assert.assertNotNull(provider.getTimeZone());
        Assert.assertNull(provider.getFilterProvider());
        Assert.assertNotNull(provider.getDefaultPropertyFormat(String.class));
        Assert.assertNotNull(provider.getDefaultPropertyInclusion(String.class));
    }

    @Test
    public void testDefaultSerializersAndSetters() {
        Assert.assertSame(NullSerializer.instance, provider.getDefaultNullValueSerializer());
        Assert.assertSame(SerializerProvider.DEFAULT_NULL_KEY_SERIALIZER, provider.getDefaultNullKeySerializer());

        JsonSerializer<Object> customNull = new NullSerializer();
        JsonSerializer<Object> customKey = new FailingSerializer("key");
        JsonSerializer<Object> customNullKey = new FailingSerializer("nullKey");

        provider.setNullValueSerializer(customNull);
        provider.setDefaultKeySerializer(customKey);
        provider.setNullKeySerializer(customNullKey);

        Assert.assertSame(customNull, provider.getDefaultNullValueSerializer());
        Assert.assertSame(customNullKey, provider.getDefaultNullKeySerializer());

        try {
            provider.setNullValueSerializer(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            provider.setDefaultKeySerializer(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            provider.setNullKeySerializer(null);
            Assert.fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testAttributes() {
        Assert.assertNull(provider.getAttribute("k1"));
        provider.setAttribute("k1", "v1");
        Assert.assertEquals("v1", provider.getAttribute("k1"));
    }

    @Test
    public void testFindSerializers() throws Exception {
        JavaType strType = config.constructType(String.class);

        JsonSerializer<Object> ser1 = provider.findValueSerializer(String.class, null);
        Assert.assertNotNull(ser1);
        JsonSerializer<Object> ser2 = provider.findValueSerializer(strType, null);
        Assert.assertSame(ser1, ser2);

        JsonSerializer<Object> ser3 = provider.findValueSerializer(String.class);
        Assert.assertSame(ser1, ser3);

        JsonSerializer<Object> ser4 = provider.findValueSerializer(strType);
        Assert.assertSame(ser1, ser4);

        JsonSerializer<Object> primSer = provider.findPrimaryPropertySerializer(strType, null);
        Assert.assertSame(ser1, primSer);

        JsonSerializer<Object> primSerCls = provider.findPrimaryPropertySerializer(String.class, null);
        Assert.assertSame(ser1, primSerCls);

        JsonSerializer<Object> typedSer1 = provider.findTypedValueSerializer(String.class, true, null);
        Assert.assertNotNull(typedSer1);
        JsonSerializer<Object> typedSer2 = provider.findTypedValueSerializer(strType, true, null);
        Assert.assertNotNull(typedSer2);

        JsonSerializer<Object> keySer = provider.findKeySerializer(strType, null);
        Assert.assertNotNull(keySer);
        JsonSerializer<Object> keySer2 = provider.findKeySerializer(String.class, null);
        Assert.assertSame(keySer, keySer2);

        Assert.assertNull(provider.findTypeSerializer(strType));
        Assert.assertSame(provider.getDefaultNullValueSerializer(), provider.findNullValueSerializer(null));
        Assert.assertSame(provider.getDefaultNullKeySerializer(), provider.findNullKeySerializer(strType, null));
    }

    @Test(expected = JsonMappingException.class)
    public void testFindValueSerializerNullType() throws Exception {
        provider.findValueSerializer((JavaType) null, null);
    }

    @Test
    public void testUnknownTypeSerializer() {
        JsonSerializer<Object> unknownObj = provider.getUnknownTypeSerializer(Object.class);
        Assert.assertTrue(provider.isUnknownTypeSerializer(unknownObj));
        Assert.assertTrue(provider.isUnknownTypeSerializer(null));

        JsonSerializer<Object> unknownCustom = provider.getUnknownTypeSerializer(Void.class);
        Assert.assertTrue(provider.isUnknownTypeSerializer(unknownCustom));

        SerializationConfig disabledConfig = config.without(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        TestSerializerProvider providerNoFail = new TestSerializerProvider(blueprint, disabledConfig, BeanSerializerFactory.instance);
        Assert.assertFalse(providerNoFail.isUnknownTypeSerializer(unknownCustom));
    }

    @Test
    public void testContextualAndResolvableHandling() throws Exception {
        ResolvableContextualSerializer ser = new ResolvableContextualSerializer();
        JsonSerializer<?> resolved = provider._handleResolvable(ser);
        Assert.assertSame(ser, resolved);
        Assert.assertTrue(ser.resolved);

        ResolvableContextualSerializer ser2 = new ResolvableContextualSerializer();
        JsonSerializer<?> contextual = provider._handleContextualResolvable(ser2, null);
        Assert.assertSame(ser2, contextual);
        Assert.assertTrue(ser2.resolved);
        Assert.assertTrue(ser2.contextualized);

        ResolvableContextualSerializer ser3 = new ResolvableContextualSerializer();
        JsonSerializer<?> primaryContextual = provider.handlePrimaryContextualization(ser3, null);
        Assert.assertSame(ser3, primaryContextual);
        Assert.assertTrue(ser3.contextualized);
    }

    @Test
    public void testDefaultSerializationMethods() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        provider.defaultSerializeNull(gen);
        provider.defaultSerializeValue(null, gen);
        provider.defaultSerializeField("field1", null, gen);
        provider.defaultSerializeValue("hello", gen);
        provider.defaultSerializeField("field2", "world", gen);

        Date d = new Date(1500000000000L);
        provider.defaultSerializeDateValue(1500000000000L, gen);
        provider.defaultSerializeDateValue(d, gen);
        provider.defaultSerializeDateKey(1500000000000L, gen);
        gen.writeString("val1");
        provider.defaultSerializeDateKey(d, gen);
        gen.writeString("val2");

        gen.flush();
        Assert.assertTrue(sw.toString().contains("hello"));
    }

    @Test
    public void testDefaultSerializationDatesFormatted() throws Exception {
        SerializationConfig dateConfig = config.without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .without(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
        TestSerializerProvider dateProvider = new TestSerializerProvider(blueprint, dateConfig, BeanSerializerFactory.instance);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(sw);

        Date d = new Date(1500000000000L);
        dateProvider.defaultSerializeDateValue(1500000000000L, gen);
        dateProvider.defaultSerializeDateValue(d, gen);
        dateProvider.defaultSerializeDateKey(1500000000000L, gen);
        gen.writeString("v1");
        dateProvider.defaultSerializeDateKey(d, gen);
        gen.writeString("v2");
        gen.flush();

        Assert.assertTrue(sw.toString().contains("2017"));
    }

    @Test
    public void testErrorReportingMethods() {
        try {
            provider.reportMappingProblem("Error message %s", "arg");
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Error message arg"));
        }

        try {
            provider.reportMappingProblem(new RuntimeException("cause"), "Error message %d", 123);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Error message 123"));
            Assert.assertEquals("cause", e.getCause().getMessage());
        }

        try {
            provider.reportBadTypeDefinition(null, "Bad type desc");
            Assert.fail();
        } catch (InvalidDefinitionException e) {
            Assert.assertTrue(e.getMessage().contains("Bad type desc"));
        }

        try {
            provider.reportBadPropertyDefinition(null, null, "Bad prop desc");
            Assert.fail();
        } catch (InvalidDefinitionException e) {
            Assert.assertTrue(e.getMessage().contains("Bad prop desc"));
        }

        try {
            provider.reportBadDefinition(config.constructType(String.class), "Bad def");
            Assert.fail();
        } catch (InvalidDefinitionException e) {
            Assert.assertTrue(e.getMessage().contains("Bad def"));
        }

        try {
            provider.reportBadDefinition(config.constructType(String.class), "Bad def with cause", new RuntimeException());
            Assert.fail();
        } catch (InvalidDefinitionException e) {
            Assert.assertTrue(e.getMessage().contains("Bad def with cause"));
        }

        try {
            provider.reportBadDefinition(String.class, "Bad raw def", new RuntimeException());
            Assert.fail();
        } catch (InvalidDefinitionException e) {
            Assert.assertTrue(e.getMessage().contains("Bad raw def"));
        }

        InvalidTypeIdException ite = provider.invalidTypeIdException(config.constructType(Object.class), "typeId", "extra");
        Assert.assertNotNull(ite);
        Assert.assertTrue(ite.getMessage().contains("typeId"));

        JsonMappingException me1 = provider.mappingException("test msg");
        Assert.assertTrue(me1.getMessage().contains("test msg"));

        JsonMappingException me2 = provider.mappingException(new RuntimeException(), "test msg with cause");
        Assert.assertTrue(me2.getMessage().contains("test msg with cause"));
    }

    @Test
    public void testIncompatibleRootType() throws Exception {
        JavaType intType = config.constructType(int.class);
        provider._reportIncompatibleRootType(Integer.valueOf(1), intType);

        try {
            provider._reportIncompatibleRootType("not an int", intType);
            Assert.fail();
        } catch (InvalidDefinitionException e) {
            Assert.assertTrue(e.getMessage().contains("Incompatible types"));
        }
    }

    @Test
    public void testFindExplicitUntypedSerializer() throws Exception {
        JsonSerializer<Object> ser = provider._findExplicitUntypedSerializer(String.class);
        Assert.assertNotNull(ser);

        JsonSerializer<Object> unknown = provider._findExplicitUntypedSerializer(Object.class);
        Assert.assertNull(unknown);
    }
}
