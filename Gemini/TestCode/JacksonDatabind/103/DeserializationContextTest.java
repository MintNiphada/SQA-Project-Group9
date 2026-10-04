package com.fasterxml.jackson.databind;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ObjectBuffer;

public class DeserializationContextTest {

    private static class TestDeserializationContext extends DeserializationContext {
        private static final long serialVersionUID = 1L;

        public TestDeserializationContext(DeserializerFactory df) {
            super(df);
        }

        public TestDeserializationContext(DeserializerFactory df, DeserializerCache cache) {
            super(df, cache);
        }

        public TestDeserializationContext(TestDeserializationContext src, DeserializerFactory factory) {
            super(src, factory);
        }

        public TestDeserializationContext(TestDeserializationContext src, DeserializationConfig config,
                                          JsonParser p, InjectableValues injectableValues) {
            super(src, config, p, injectableValues);
        }

        public TestDeserializationContext(TestDeserializationContext src) {
            super(src);
        }

        @Override
        public ReadableObjectId findObjectId(Object id, ObjectIdGenerator<?> generator, ObjectIdResolver resolver) {
            return null;
        }

        @Override
        public void checkUnresolvedObjectId() throws UnresolvedForwardReference {
        }

        @Override
        public JsonDeserializer<Object> deserializerInstance(Annotated annotated, Object deserDef) {
            return null;
        }

        @Override
        public KeyDeserializer keyDeserializerInstance(Annotated annotated, Object deserDef) {
            return null;
        }
    }

    private ObjectMapper _mapper;
    private DeserializationConfig _config;
    private JsonParser _parser;
    private TestDeserializationContext _context;
    private DeserializerFactory _factory;

    @Before
    public void setUp() throws Exception {
        _mapper = new ObjectMapper();
        _config = _mapper.getDeserializationConfig();
        _parser = _mapper.getFactory().createParser("{\"a\":123}");
        _factory = new BeanDeserializerFactory(new DeserializerFactoryConfig());
        TestDeserializationContext blueprint = new TestDeserializationContext(_factory, new DeserializerCache());
        _context = new TestDeserializationContext(blueprint, _config, _parser, new InjectableValues.Std());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullFactory() {
        new TestDeserializationContext(null, null);
    }

    @Test
    public void testConstructorsAndCopies() {
        TestDeserializationContext blueprint1 = new TestDeserializationContext(_factory);
        Assert.assertNotNull(blueprint1.getFactory());

        TestDeserializationContext blueprint2 = new TestDeserializationContext(blueprint1, _factory);
        Assert.assertSame(_factory, blueprint2.getFactory());

        TestDeserializationContext copy = new TestDeserializationContext(_context);
        Assert.assertSame(_factory, copy.getFactory());
        Assert.assertNull(copy.getParser());
    }

    @Test
    public void testDatabindContextMethods() throws Exception {
        Assert.assertSame(_config, _context.getConfig());
        Assert.assertNull(_context.getActiveView());
        Assert.assertTrue(_context.canOverrideAccessModifiers());
        Assert.assertTrue(_context.isEnabled(MapperFeature.AUTO_DETECT_FIELDS));
        Assert.assertNotNull(_context.getDefaultPropertyFormat(String.class));
        Assert.assertNotNull(_context.getAnnotationIntrospector());
        Assert.assertSame(_config.getTypeFactory(), _context.getTypeFactory());
        Assert.assertEquals(Locale.getDefault(), _context.getLocale());
        Assert.assertNotNull(_context.getTimeZone());
        Assert.assertNotNull(_context.getBase64Variant());
        Assert.assertSame(JsonNodeFactory.instance, _context.getNodeFactory());

        Class<?> cls = _context.findClass("java.lang.String");
        Assert.assertEquals(String.class, cls);

        JavaType jt = _context.constructType(String.class);
        Assert.assertNotNull(jt);
        Assert.assertNull(_context.constructType(null));
    }

    @Test
    public void testAttributes() {
        Assert.assertNull(_context.getAttribute("key1"));
        _context.setAttribute("key1", "val1");
        Assert.assertEquals("val1", _context.getAttribute("key1"));
    }

    @Test
    public void testFeatures() {
        int mask = _context.getDeserializationFeatures();
        Assert.assertTrue(_context.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        Assert.assertTrue(_context.hasDeserializationFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask()));
        Assert.assertTrue(_context.hasSomeOfFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask()));
        Assert.assertFalse(_context.hasSomeOfFeatures(0));
    }

    @Test
    public void testInjectableValues() throws Exception {
        InjectableValues.Std std = new InjectableValues.Std();
        std.addValue("id1", "injectedVal");
        TestDeserializationContext ctxt = new TestDeserializationContext(_context, _config, _parser, std);
        Object val = ctxt.findInjectableValue("id1", null, null);
        Assert.assertEquals("injectedVal", val);

        TestDeserializationContext noInject = new TestDeserializationContext(_context, _config, _parser, null);
        try {
            noInject.findInjectableValue("id1", null, null);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("No 'injectableValues' configured"));
        }
    }

    @Test
    public void testDeserializerCacheLookups() throws Exception {
        JavaType strType = _context.constructType(String.class);
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();
        boolean hasDeser = _context.hasValueDeserializerFor(strType, cause);
        Assert.assertTrue(hasDeser);
        Assert.assertNull(cause.get());

        JsonDeserializer<Object> d1 = _context.findContextualValueDeserializer(strType, null);
        Assert.assertNotNull(d1);

        JsonDeserializer<Object> d2 = _context.findNonContextualValueDeserializer(strType);
        Assert.assertNotNull(d2);

        JsonDeserializer<Object> d3 = _context.findRootValueDeserializer(strType);
        Assert.assertNotNull(d3);

        KeyDeserializer kd = _context.findKeyDeserializer(strType, null);
        Assert.assertNotNull(kd);
    }

    @Test
    public void testBufferAndArrayBuilders() {
        ObjectBuffer buf1 = _context.leaseObjectBuffer();
        Assert.assertNotNull(buf1);
        _context.returnObjectBuffer(buf1);

        ObjectBuffer buf2 = _context.leaseObjectBuffer();
        Assert.assertSame(buf1, buf2);

        ObjectBuffer largerBuf = new ObjectBuffer();
        largerBuf.resetAndStart();
        largerBuf.appendCompletedChunk(new Object[100]);
        _context.returnObjectBuffer(largerBuf);

        ArrayBuilders ab = _context.getArrayBuilders();
        Assert.assertNotNull(ab);
        Assert.assertSame(ab, _context.getArrayBuilders());
    }

    @Test
    public void testDateAndCalendar() {
        Date d = _context.parseDate("2020-01-01T00:00:00.000+0000");
        Assert.assertNotNull(d);
        Calendar cal = _context.constructCalendar(d);
        Assert.assertEquals(d.getTime(), cal.getTimeInMillis());

        try {
            _context.parseDate("not-a-date");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to parse Date value"));
        }
    }

    @Test
    public void testReadValues() throws Exception {
        JsonParser p = _mapper.getFactory().createParser("\"hello\"");
        p.nextToken();
        String val = _context.readValue(p, String.class);
        Assert.assertEquals("hello", val);

        JsonParser p2 = _mapper.getFactory().createParser("\"world\"");
        p2.nextToken();
        String val2 = _context.readPropertyValue(p2, null, String.class);
        Assert.assertEquals("world", val2);
    }

    @Test
    public void testProblemHandlingUnknownProperty() throws Exception {
        JsonParser p = _mapper.getFactory().createParser("{\"prop\":1}");
        p.nextToken();
        p.nextToken();

        try {
            _context.handleUnknownProperty(p, null, String.class, "prop");
            Assert.fail();
        } catch (UnrecognizedPropertyException e) {
            Assert.assertEquals("prop", e.getPropertyName());
        }

        DeserializationConfig cfgNoFail = _config.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        TestDeserializationContext ctxtNoFail = new TestDeserializationContext(_context, cfgNoFail, p, null);
        boolean handled = ctxtNoFail.handleUnknownProperty(p, null, String.class, "prop");
        Assert.assertTrue(handled);

        DeserializationProblemHandler handler = new DeserializationProblemHandler() {
            @Override
            public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p2,
                                                 JsonDeserializer<?> deser, Object inst, String prop) {
                return true;
            }
        };
        DeserializationConfig cfgHandler = _config.withHandler(handler);
        TestDeserializationContext ctxtHandler = new TestDeserializationContext(_context, cfgHandler, p, null);
        Assert.assertTrue(ctxtHandler.handleUnknownProperty(p, null, String.class, "prop"));
    }

    @Test
    public void testProblemHandlingWeirdValues() throws Exception {
        try {
            _context.handleWeirdKey(Integer.class, "abc", "Custom message %s", "arg");
            Assert.fail();
        } catch (InvalidFormatException e) {
            Assert.assertEquals(Integer.class, e.getTargetType());
        }

        try {
            _context.handleWeirdStringValue(Integer.class, "abc", "Custom string %s", "arg");
            Assert.fail();
        } catch (InvalidFormatException e) {
            Assert.assertEquals(Integer.class, e.getTargetType());
        }

        try {
            _context.handleWeirdNumberValue(Integer.class, 123L, "Custom num %s", "arg");
            Assert.fail();
        } catch (InvalidFormatException e) {
            Assert.assertEquals(Integer.class, e.getTargetType());
        }

        try {
            _context.handleWeirdNativeValue(_context.constructType(Integer.class), "obj", _parser);
            Assert.fail();
        } catch (InvalidFormatException e) {
            Assert.assertEquals(Integer.class, e.getTargetType());
        }

        DeserializationProblemHandler handler = new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdKey(DeserializationContext ctxt, Class<?> rawKeyType, String keyValue, String msg) {
                return 42;
            }
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String msg) {
                return 43;
            }
            @Override
            public Object handleWeirdNumberValue(DeserializationContext ctxt, Class<?> targetType, Number valueToConvert, String msg) {
                return 44;
            }
            @Override
            public Object handleWeirdNativeValue(DeserializationContext ctxt, JavaType targetType, Object badValue, JsonParser p) {
                return 45;
            }
        };
        DeserializationConfig cfgHandler = _config.withHandler(handler);
        TestDeserializationContext ctxtHandler = new TestDeserializationContext(_context, cfgHandler, _parser, null);

        Assert.assertEquals(42, ctxtHandler.handleWeirdKey(Integer.class, "abc", "msg"));
        Assert.assertEquals(43, ctxtHandler.handleWeirdStringValue(Integer.class, "abc", "msg"));
        Assert.assertEquals(44, ctxtHandler.handleWeirdNumberValue(Integer.class, 123L, "msg"));
        Assert.assertEquals(45, ctxtHandler.handleWeirdNativeValue(_context.constructType(Integer.class), "obj", _parser));
    }

    @Test
    public void testProblemHandlingUnexpectedTokenAndMissingInstantiator() throws Exception {
        try {
            _context.handleUnexpectedToken(Integer.class, _parser);
            Assert.fail();
        } catch (MismatchedInputException e) {
            Assert.assertEquals(Integer.class, e.getTargetType());
        }

        try {
            _context.handleMissingInstantiator(Integer.class, null, _parser, "missing");
            Assert.fail();
        } catch (MismatchedInputException e) {
            Assert.assertEquals(Integer.class, e.getTargetType());
        }

        try {
            _context.handleInstantiationProblem(Integer.class, "arg", new RuntimeException("error"));
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot construct instance of"));
        }
    }

    @Test
    public void testProblemHandlingTypeIds() throws Exception {
        JavaType baseType = _context.constructType(CharSequence.class);
        TypeIdResolver resolver = ClassNameIdResolver.construct(baseType, _config.getTypeFactory(), _config.getPolymorphicTypeValidator());

        try {
            _context.handleUnknownTypeId(baseType, "invalidType", resolver, "extra");
            Assert.fail();
        } catch (InvalidTypeIdException e) {
            Assert.assertEquals("invalidType", e.getTypeId());
        }

        try {
            _context.handleMissingTypeId(baseType, resolver, "extra");
            Assert.fail();
        } catch (InvalidTypeIdException e) {
            Assert.assertNull(e.getTypeId());
        }

        DeserializationConfig cfgNoFail = _config.without(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        TestDeserializationContext ctxtNoFail = new TestDeserializationContext(_context, cfgNoFail, _parser, null);
        Assert.assertNull(ctxtNoFail.handleUnknownTypeId(baseType, "invalidType", resolver, "extra"));
    }

    @Test
    public void testReportMethods() throws Exception {
        try {
            _context.reportWrongTokenException(String.class, JsonToken.START_OBJECT, "expected obj");
            Assert.fail();
        } catch (MismatchedInputException e) {
            Assert.assertEquals(String.class, e.getTargetType());
        }

        try {
            _context.reportWrongTokenException(_context.constructType(String.class), JsonToken.START_OBJECT, "expected obj");
            Assert.fail();
        } catch (MismatchedInputException e) {
            Assert.assertEquals(String.class, e.getTargetType());
        }

        try {
            _context.reportInputMismatch(String.class, "msg %s", "arg");
            Assert.fail();
        } catch (MismatchedInputException e) {
            Assert.assertTrue(e.getMessage().contains("msg arg"));
        }

        try {
            _context.reportInputMismatch(_context.constructType(String.class), "msg");
            Assert.fail();
        } catch (MismatchedInputException e) {
            Assert.assertEquals(String.class, e.getTargetType());
        }

        try {
            _context.reportTrailingTokens(String.class, _parser, JsonToken.END_OBJECT);
            Assert.fail();
        } catch (MismatchedInputException e) {
            Assert.assertTrue(e.getMessage().contains("Trailing token"));
        }

        try {
            _context.reportBadDefinition(_context.constructType(String.class), "bad def");
            Assert.fail();
        } catch (InvalidDefinitionException e) {
            Assert.assertTrue(e.getMessage().contains("bad def"));
        }
    }

    @Test
    public void testDeprecatedAndHelperMethods() throws Exception {
        JsonMappingException e1 = _context.weirdKeyException(String.class, "key", "msg");
        Assert.assertNotNull(e1);

        JsonMappingException e2 = _context.weirdStringException("val", String.class, "msg");
        Assert.assertNotNull(e2);

        JsonMappingException e3 = _context.weirdNumberException(10, Integer.class, "msg");
        Assert.assertNotNull(e3);

        JsonMappingException e4 = _context.weirdNativeValueException("val", String.class);
        Assert.assertNotNull(e4);

        JsonMappingException e5 = _context.instantiationException(String.class, new RuntimeException("cause"));
        Assert.assertNotNull(e5);

        JsonMappingException e6 = _context.instantiationException(String.class, "msg");
        Assert.assertNotNull(e6);

        JsonMappingException e7 = _context.invalidTypeIdException(_context.constructType(String.class), "id", "extra");
        Assert.assertNotNull(e7);

        JsonMappingException e8 = _context.missingTypeIdException(_context.constructType(String.class), "extra");
        Assert.assertNotNull(e8);

        JsonMappingException e9 = _context.unknownTypeException(_context.constructType(String.class), "id", "extra");
        Assert.assertNotNull(e9);

        JsonMappingException e10 = _context.endOfInputException(String.class);
        Assert.assertNotNull(e10);

        JsonMappingException e11 = _context.mappingException("msg");
        Assert.assertNotNull(e11);

        JsonMappingException e12 = _context.mappingException("msg %s", "arg");
        Assert.assertNotNull(e12);

        JsonMappingException e13 = _context.wrongTokenException(_parser, JsonToken.START_ARRAY, "msg");
        Assert.assertNotNull(e13);

        Assert.assertTrue(_context._isCompatible(int.class, 5));
        Assert.assertTrue(_context._isCompatible(String.class, "abc"));
        Assert.assertTrue(_context._isCompatible(String.class, null));
        Assert.assertFalse(_context._isCompatible(int.class, "abc"));
    }
}
