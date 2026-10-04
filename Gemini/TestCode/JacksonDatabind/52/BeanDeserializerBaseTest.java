package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.impl.ValueInjector;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BeanDeserializerBaseTest {

    private ObjectMapper mapper;
    private JsonFactory factory;

    static class DummyBean {
        public String name;
        public int age;
    }

    static class ConcreteBeanDeserializer extends BeanDeserializerBase {
        public ConcreteBeanDeserializer(BeanDeserializerBuilder builder,
                                        BeanDescription beanDesc,
                                        BeanPropertyMap properties,
                                        Map<String, SettableBeanProperty> backRefs,
                                        Set<String> ignorableProps,
                                        boolean ignoreAllUnknown,
                                        boolean hasViews) {
            super(builder, beanDesc, properties, backRefs, ignorableProps, ignoreAllUnknown, hasViews);
        }

        public ConcreteBeanDeserializer(ConcreteBeanDeserializer src) {
            super(src);
        }

        public ConcreteBeanDeserializer(ConcreteBeanDeserializer src, boolean ignoreAllUnknown) {
            super(src, ignoreAllUnknown);
        }

        public ConcreteBeanDeserializer(ConcreteBeanDeserializer src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }

        public ConcreteBeanDeserializer(ConcreteBeanDeserializer src, ObjectIdReader oir) {
            super(src, oir);
        }

        public ConcreteBeanDeserializer(ConcreteBeanDeserializer src, Set<String> ignorableProps) {
            super(src, ignorableProps);
        }

        public ConcreteBeanDeserializer(ConcreteBeanDeserializer src, BeanPropertyMap props) {
            super(src, props);
        }

        @Override
        public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
            return new ConcreteBeanDeserializer(this, unwrapper);
        }

        @Override
        public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
            return new ConcreteBeanDeserializer(this, oir);
        }

        @Override
        public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) {
            return new ConcreteBeanDeserializer(this, ignorableProps);
        }

        @Override
        protected BeanDeserializerBase asArrayDeserializer() {
            return this;
        }

        @Override
        public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            DummyBean bean = new DummyBean();
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String name = p.getCurrentName();
                p.nextToken();
                if ("name".equals(name)) {
                    bean.name = p.getText();
                } else if ("age".equals(name)) {
                    bean.age = p.getIntValue();
                } else {
                    handleUnknownVanilla(p, ctxt, bean, name);
                }
            }
            return bean;
        }

        @Override
        protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new DummyBean();
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            if (p.getCurrentToken() == JsonToken.START_OBJECT) {
                return deserializeFromObject(p, ctxt);
            }
            return null;
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        factory = mapper.getFactory();
    }

    private ConcreteBeanDeserializer createTestDeserializer(Class<?> cls) throws Exception {
        JavaType type = mapper.constructType(cls);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, mapper.getDeserializationConfig());
        builder.setValueInstantiator(new ValueInstantiator.Base(type));
        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        return new ConcreteBeanDeserializer(builder, beanDesc, propMap, null, null, false, false);
    }

    @Test
    public void testBasicPropertiesAndAccessors() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        Assert.assertTrue(deser.isCachable());
        Assert.assertEquals(DummyBean.class, deser.handledType());
        Assert.assertEquals(DummyBean.class, deser.getBeanClass());
        Assert.assertEquals(DummyBean.class, deser.getValueType().getRawClass());
        Assert.assertNull(deser.getObjectIdReader());
        Assert.assertFalse(deser.hasViews());
        Assert.assertEquals(0, deser.getPropertyCount());
        Assert.assertFalse(deser.hasProperty("none"));
        Assert.assertNull(deser.findProperty("none"));
        Assert.assertNull(deser.findProperty(new PropertyName("none")));
        Assert.assertNull(deser.findProperty(0));
        Assert.assertNull(deser.findBackReference("back"));
        Assert.assertNotNull(deser.getValueInstantiator());
        Assert.assertNotNull(deser.properties());
        Assert.assertFalse(deser.properties().hasNext());
        Assert.assertNotNull(deser.creatorProperties());
        Assert.assertFalse(deser.creatorProperties().hasNext());
        Assert.assertTrue(deser.getKnownPropertyNames().isEmpty());
    }

    @Test
    public void testCopyConstructors() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        ConcreteBeanDeserializer copy1 = new ConcreteBeanDeserializer(deser);
        Assert.assertEquals(deser.handledType(), copy1.handledType());

        ConcreteBeanDeserializer copy2 = new ConcreteBeanDeserializer(deser, true);
        Assert.assertEquals(deser.handledType(), copy2.handledType());

        Set<String> ign = new HashSet<String>();
        ign.add("ignored");
        BeanDeserializerBase copy3 = deser.withIgnorableProperties(ign);
        Assert.assertNotNull(copy3);

        BeanDeserializerBase copy4 = (BeanDeserializerBase) deser.unwrappingDeserializer(NameTransformer.NOP);
        Assert.assertNotNull(copy4);
        BeanDeserializerBase copy4b = (BeanDeserializerBase) deser.unwrappingDeserializer(null);
        Assert.assertNotNull(copy4b);

        BeanDeserializerBase copy5 = deser.withObjectIdReader(null);
        Assert.assertNotNull(copy5);
        Assert.assertNull(copy5.getObjectIdReader());
    }

    @Test
    public void testWithBeanPropertiesDefaultThrows() {
        BeanDeserializerBase base = new BeanDeserializerBase(null,
                mapper.getDeserializationConfig().introspectClassAnnotations(DummyBean.class),
                BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false),
                null, null, false, false) {
            @Override
            public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
                return null;
            }

            @Override
            public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) {
                return null;
            }

            @Override
            public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) {
                return null;
            }

            @Override
            protected BeanDeserializerBase asArrayDeserializer() {
                return null;
            }

            @Override
            public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        try {
            base.withBeanProperties(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false));
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("does not override `withBeanProperties()`"));
        }
    }

    @Test
    public void testResolve() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deser.resolve(ctxt);
        Assert.assertEquals(0, deser.getPropertyCount());
    }

    @Test
    public void testCreateContextual() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        Assert.assertNotNull(contextual);
        Assert.assertSame(deser, contextual);
    }

    @Test
    public void testDeserializeFromNumber() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser pInt = factory.createParser("123");
        pInt.nextToken();
        try {
            deser.deserializeFromNumber(pInt, ctxt);
            Assert.fail("Should throw JsonMappingException for missing instantiator");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }

        JsonParser pLong = factory.createParser("1234567890123");
        pLong.nextToken();
        try {
            deser.deserializeFromNumber(pLong, ctxt);
            Assert.fail("Should throw JsonMappingException for missing instantiator");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testDeserializeFromString() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser p = factory.createParser("\"test string\"");
        p.nextToken();
        try {
            deser.deserializeFromString(p, ctxt);
            Assert.fail("Should throw JsonMappingException for missing instantiator");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testDeserializeFromDouble() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser p = factory.createParser("3.1415");
        p.nextToken();
        try {
            deser.deserializeFromDouble(p, ctxt);
            Assert.fail("Should throw JsonMappingException for missing instantiator");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testDeserializeFromBoolean() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser p = factory.createParser("true");
        p.nextToken();
        try {
            deser.deserializeFromBoolean(p, ctxt);
            Assert.fail("Should throw JsonMappingException for missing instantiator");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testDeserializeFromArray() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser p = factory.createParser("[1, 2]");
        p.nextToken();
        try {
            deser.deserializeFromArray(p, ctxt);
            Assert.fail("Should throw JsonMappingException for unexpected token");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testDeserializeFromArrayEmptyAsNull() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = localMapper.getDeserializationContext();
        JsonParser p = localMapper.getFactory().createParser("[]");
        p.nextToken();
        Object result = deser.deserializeFromArray(p, ctxt);
        Assert.assertNull(result);
    }

    @Test
    public void testDeserializeFromEmbedded() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser p = factory.createParser("\"test\"");
        p.nextToken();
        Object result = deser.deserializeFromEmbedded(p, ctxt);
        Assert.assertNull(result);
    }

    @Test
    public void testHandleUnknownPropertyIgnored() throws Exception {
        Set<String> ign = new HashSet<String>();
        ign.add("ignoredField");
        JavaType type = mapper.constructType(DummyBean.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, mapper.getDeserializationConfig());
        builder.setValueInstantiator(new ValueInstantiator.Base(type));
        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        ConcreteBeanDeserializer deser = new ConcreteBeanDeserializer(builder, beanDesc, propMap, null, ign, false, false);

        JsonParser p = factory.createParser("{\"ignoredField\":\"foo\"}");
        p.nextToken();
        p.nextToken();
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DummyBean bean = new DummyBean();
        deser.handleUnknownVanilla(p, ctxt, bean, "ignoredField");
    }

    @Test(expected = IgnoredPropertyException.class)
    public void testHandleIgnoredPropertyThrowsWhenConfigured() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        Set<String> ign = new HashSet<String>();
        ign.add("ignoredField");
        JavaType type = localMapper.constructType(DummyBean.class);
        BeanDescription beanDesc = localMapper.getDeserializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, localMapper.getDeserializationConfig());
        builder.setValueInstantiator(new ValueInstantiator.Base(type));
        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        ConcreteBeanDeserializer deser = new ConcreteBeanDeserializer(builder, beanDesc, propMap, null, ign, false, false);

        JsonParser p = localMapper.getFactory().createParser("\"foo\"");
        p.nextToken();
        DeserializationContext ctxt = localMapper.getDeserializationContext();
        deser.handleIgnoredProperty(p, ctxt, DummyBean.class, "ignoredField");
    }

    @Test
    public void testHandleUnknownPropertyIgnoreAll() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        ConcreteBeanDeserializer ignoreAllDeser = new ConcreteBeanDeserializer(deser, true);
        JsonParser p = factory.createParser("\"unknownValue\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        ignoreAllDeser.handleUnknownProperty(p, ctxt, new DummyBean(), "unknownProp");
    }

    @Test
    public void testWrapAndThrow() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DummyBean bean = new DummyBean();

        try {
            deser.wrapAndThrow(new IllegalArgumentException("test msg"), bean, "name", ctxt);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("test msg"));
        }

        try {
            deser.wrapAndThrow(new InvocationTargetException(new RuntimeException("wrapped msg")), bean, 0, ctxt);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("wrapped msg"));
        }

        try {
            deser.wrapAndThrow(new OutOfMemoryError("oom"), bean, "name", ctxt);
            Assert.fail("Expected Error");
        } catch (OutOfMemoryError e) {
            Assert.assertEquals("oom", e.getMessage());
        }

        try {
            deser.wrapAndThrow(new IOException("plain io"), bean, "name", ctxt);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertEquals("plain io", e.getMessage());
        }
    }

    @Test
    public void testWrapInstantiationProblem() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            deser.wrapInstantiationProblem(new OutOfMemoryError("oom"), ctxt);
            Assert.fail("Expected Error");
        } catch (OutOfMemoryError e) {
            Assert.assertEquals("oom", e.getMessage());
        }

        try {
            deser.wrapInstantiationProblem(new IOException("plain io"), ctxt);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertEquals("plain io", e.getMessage());
        }

        try {
            deser.wrapInstantiationProblem(new InvocationTargetException(new RuntimeException("runtime")), ctxt);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testHandleUnknownPropertiesBuffer() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        ConcreteBeanDeserializer ignoreAllDeser = new ConcreteBeanDeserializer(deser, true);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        TokenBuffer buf = new TokenBuffer(mapper, false);
        buf.writeFieldName("foo");
        buf.writeString("bar");
        buf.writeFieldName("count");
        buf.writeNumber(10);

        DummyBean bean = new DummyBean();
        Object result = ignoreAllDeser.handleUnknownProperties(ctxt, bean, buf);
        Assert.assertSame(bean, result);
    }

    @Test
    public void testHandlePolymorphic() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        ConcreteBeanDeserializer ignoreAllDeser = new ConcreteBeanDeserializer(deser, true);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        TokenBuffer buf = new TokenBuffer(mapper, false);
        buf.writeFieldName("foo");
        buf.writeString("bar");

        DummyBean bean = new DummyBean();
        Object result = ignoreAllDeser.handlePolymorphic(null, ctxt, bean, buf);
        Assert.assertSame(bean, result);
    }

    @Test
    public void testConvertObjectId() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JsonParser p = factory.createParser("{}");

        JsonDeserializer<Object> stringDeser = new StdDeserializer<Object>(String.class) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getText();
            }
        };

        Object convertedString = deser._convertObjectId(p, ctxt, "12345", stringDeser);
        Assert.assertEquals("12345", convertedString);

        JsonDeserializer<Object> longDeser = new StdDeserializer<Object>(Long.class) {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return p.getLongValue();
            }
        };

        Object convertedLong = deser._convertObjectId(p, ctxt, Long.valueOf(9876543210L), longDeser);
        Assert.assertEquals(9876543210L, convertedLong);

        Object convertedInt = deser._convertObjectId(p, ctxt, Integer.valueOf(42), longDeser);
        Assert.assertEquals(42L, convertedInt);
    }

    @Test
    public void testDeserializeFromObjectUsingNonDefaultAbstractThrows() throws Exception {
        JavaType type = mapper.constructType(CharSequence.class);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, mapper.getDeserializationConfig());
        builder.setValueInstantiator(new ValueInstantiator.Base(type));
        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        ConcreteBeanDeserializer deser = new ConcreteBeanDeserializer(builder, beanDesc, propMap, null, null, false, false);

        JsonParser p = factory.createParser("{}");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        try {
            deser.deserializeFromObjectUsingNonDefault(p, ctxt);
            Assert.fail("Expected JsonMappingException for abstract type");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("abstract type"));
        }
    }

    @Test
    public void testFindConvertingDeserializerNullWhenNoConverter() throws Exception {
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        SettableBeanProperty prop = deser.findProperty("name");
        Assert.assertNull(prop);
    }

    @Test
    public void testUnwrapSingleValueArray() throws Exception {
        ObjectMapper localMapper = new ObjectMapper();
        localMapper.enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
        ConcreteBeanDeserializer deser = createTestDeserializer(DummyBean.class);
        DeserializationContext ctxt = localMapper.getDeserializationContext();

        JsonParser p = localMapper.getFactory().createParser("[{\"name\":\"Alice\",\"age\":30}]");
        p.nextToken();
        Object result = deser.deserializeFromArray(p, ctxt);
        Assert.assertTrue(result instanceof DummyBean);
        DummyBean bean = (DummyBean) result;
        Assert.assertEquals("Alice", bean.name);
        Assert.assertEquals(30, bean.age);
    }
}
