package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.impl.ValueInjector;
import com.fasterxml.jackson.databind.exc.IgnoredPropertyException;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

public class BeanDeserializerBaseTest {

    static class DummyBean {
        public String id;
        public String name;
        public int age;
    }

    static class ConcreteBeanDeserializer extends BeanDeserializerBase {
        private static final long serialVersionUID = 1L;

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
        public BeanDeserializerBase withBeanProperties(BeanPropertyMap props) {
            return new ConcreteBeanDeserializer(this, props);
        }

        @Override
        protected BeanDeserializerBase asArrayDeserializer() {
            return this;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return deserializeFromObject(p, ctxt);
        }

        @Override
        public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new DummyBean();
        }

        @Override
        protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new DummyBean();
        }
    }

    private ConcreteBeanDeserializer createTestDeserializer(ObjectMapper mapper, Class<?> beanClass) {
        JavaType type = mapper.constructType(beanClass);
        DeserializationConfig config = mapper.getDeserializationConfig();
        BasicBeanDescription beanDesc = (BasicBeanDescription) config.introspect(type);
        ValueInstantiator inst = new ValueInstantiator.Base(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(beanDesc, config);
        builder.setValueInstantiator(inst);
        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), true);
        return new ConcreteBeanDeserializer(builder, beanDesc, propMap,
                new HashMap<String, SettableBeanProperty>(),
                new HashSet<String>(), false, false);
    }

    @Test
    public void testBasicPropertiesAndAccessors() {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);

        Assert.assertTrue(deser.isCachable());
        Assert.assertEquals(DummyBean.class, deser.handledType());
        Assert.assertEquals(DummyBean.class, deser.getBeanClass());
        Assert.assertEquals(DummyBean.class, deser.getValueType().getRawClass());
        Assert.assertNull(deser.getObjectIdReader());
        Assert.assertEquals(0, deser.getPropertyCount());
        Assert.assertFalse(deser.hasViews());
        Assert.assertFalse(deser.hasProperty("missing"));
        Assert.assertNull(deser.findProperty("missing"));
        Assert.assertNull(deser.findProperty(new PropertyName("missing")));
        Assert.assertNull(deser.findProperty(0));
        Assert.assertNull(deser.findBackReference("backRef"));
        Assert.assertNotNull(deser.getValueInstantiator());
        Assert.assertNotNull(deser.getKnownPropertyNames());
        Assert.assertTrue(deser.getKnownPropertyNames().isEmpty());
        Assert.assertNotNull(deser.properties());
        Assert.assertNotNull(deser.creatorProperties());
        Assert.assertFalse(deser.creatorProperties().hasNext());
    }

    @Test
    public void testCopyConstructorsAndMutants() {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);

        ConcreteBeanDeserializer copy1 = new ConcreteBeanDeserializer(deser);
        Assert.assertEquals(deser.handledType(), copy1.handledType());

        ConcreteBeanDeserializer copy2 = new ConcreteBeanDeserializer(deser, true);
        Assert.assertNotNull(copy2);

        Set<String> ign = new HashSet<String>(Arrays.asList("secret"));
        BeanDeserializerBase deserWithIgn = deser.withIgnorableProperties(ign);
        Assert.assertNotNull(deserWithIgn);

        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        BeanDeserializerBase deserWithProps = deser.withBeanProperties(propMap);
        Assert.assertNotNull(deserWithProps);

        JavaType idType = mapper.constructType(String.class);
        ObjectIdReader oir = ObjectIdReader.construct(idType, new PropertyName("id"),
                new ObjectIdGenerators.StringIdGenerator(),
                null, null, new SimpleObjectIdResolver());
        BeanDeserializerBase deserWithOir = deser.withObjectIdReader(oir);
        Assert.assertEquals(oir, deserWithOir.getObjectIdReader());

        BeanDeserializerBase deserUnwrapped = (BeanDeserializerBase) deser.unwrappingDeserializer(NameTransformer.simpleTransformer("pre.", "post."));
        Assert.assertNotNull(deserUnwrapped);
    }

    @Test
    public void testUnsupportedWithBeanProperties() {
        class CustomDeserBase extends BeanDeserializerBase {
            private static final long serialVersionUID = 1L;
            protected CustomDeserBase(BeanDeserializerBuilder b, BeanDescription desc, BeanPropertyMap props) {
                super(b, desc, props, null, null, false, false);
            }
            @Override
            public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) { return this; }
            @Override
            public BeanDeserializerBase withObjectIdReader(ObjectIdReader oir) { return this; }
            @Override
            public BeanDeserializerBase withIgnorableProperties(Set<String> ignorableProps) { return this; }
            @Override
            protected BeanDeserializerBase asArrayDeserializer() { return this; }
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            public Object deserializeFromObject(JsonParser p, DeserializationContext ctxt) { return null; }
            @Override
            protected Object _deserializeUsingPropertyBased(JsonParser p, DeserializationContext ctxt) { return null; }
        }

        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(DummyBean.class);
        DeserializationConfig config = mapper.getDeserializationConfig();
        BasicBeanDescription desc = (BasicBeanDescription) config.introspect(type);
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(desc, config);
        builder.setValueInstantiator(new ValueInstantiator.Base(type));
        BeanPropertyMap propMap = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), true);
        CustomDeserBase cdb = new CustomDeserBase(builder, desc, propMap);

        try {
            cdb.withBeanProperties(propMap);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("does not override `withBeanProperties()`"));
        }
    }

    @Test
    public void testResolveAndContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);

        deser.resolve(ctxt);
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        Assert.assertSame(deser, contextual);
    }

    @Test
    public void testUnknownAndIgnoredPropertyHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser parser = mapper.getFactory().createParser("{\"foo\":\"bar\"}");
        parser.nextToken();
        parser.nextToken();

        try {
            deser.handleUnknownVanilla(parser, ctxt, new DummyBean(), "unknownProp");
            Assert.fail("Expected failure on unknown property");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }

        Set<String> ign = new HashSet<String>(Collections.singletonList("ignoredProp"));
        ConcreteBeanDeserializer deserWithIgn = (ConcreteBeanDeserializer) deser.withIgnorableProperties(ign);

        JsonParser parser2 = mapper.getFactory().createParser("{\"ignoredProp\":\"val\"}");
        parser2.nextToken();
        parser2.nextToken();

        deserWithIgn.handleUnknownVanilla(parser2, ctxt, new DummyBean(), "ignoredProp");
    }

    @Test(expected = IgnoredPropertyException.class)
    public void testIgnoredPropertyThrowsWhenConfigured() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);

        JsonParser parser = mapper.getFactory().createParser("{\"ignored\":\"val\"}");
        parser.nextToken();
        parser.nextToken();

        deser.handleIgnoredProperty(parser, ctxt, new DummyBean(), "ignored");
    }

    @Test
    public void testHandleUnknownPropertiesBuffer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);
        ConcreteBeanDeserializer ignoringDeser = new ConcreteBeanDeserializer(deser, true);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeFieldName("prop1");
        tb.writeString("val1");

        Object result = ignoringDeser.handleUnknownProperties(ctxt, new DummyBean(), tb);
        Assert.assertNotNull(result);
    }

    @Test
    public void testWrapAndThrowExceptions() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        DummyBean bean = new DummyBean();

        try {
            deser.wrapAndThrow(new NullPointerException("NPE"), bean, "name", ctxt);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("NPE"));
        }

        try {
            deser.wrapAndThrow(new NullPointerException("NPE2"), bean, 1, ctxt);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("NPE2"));
        }

        try {
            InvocationTargetException ite = new InvocationTargetException(new OutOfMemoryError("OOM"));
            deser.wrapAndThrow(ite, bean, "name", ctxt);
            Assert.fail("Expected Error to be rethrown");
        } catch (OutOfMemoryError e) {
            Assert.assertEquals("OOM", e.getMessage());
        }
    }

    @Test
    public void testWrapInstantiationProblem() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            deser.wrapInstantiationProblem(new IOException("Disk error"), ctxt);
            Assert.fail("Expected IOException to be thrown");
        } catch (IOException e) {
            Assert.assertEquals("Disk error", e.getMessage());
        }

        try {
            deser.wrapInstantiationProblem(new OutOfMemoryError("OOM"), ctxt);
            Assert.fail("Expected OutOfMemoryError");
        } catch (OutOfMemoryError e) {
            Assert.assertEquals("OOM", e.getMessage());
        }
    }

    @Test
    public void testDeserializeFromScalarTypesMissingInstantiators() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser intParser = mapper.getFactory().createParser("123");
        intParser.nextToken();
        try {
            deser.deserializeFromNumber(intParser, ctxt);
            Assert.fail("Expected failure for missing number instantiator");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }

        JsonParser strParser = mapper.getFactory().createParser("\"some text\"");
        strParser.nextToken();
        try {
            deser.deserializeFromString(strParser, ctxt);
            Assert.fail("Expected failure for missing string instantiator");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }

        JsonParser dblParser = mapper.getFactory().createParser("12.34");
        dblParser.nextToken();
        try {
            deser.deserializeFromDouble(dblParser, ctxt);
            Assert.fail("Expected failure for missing double instantiator");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }

        JsonParser boolParser = mapper.getFactory().createParser("true");
        boolParser.nextToken();
        try {
            deser.deserializeFromBoolean(boolParser, ctxt);
            Assert.fail("Expected failure for missing boolean instantiator");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }

        JsonParser arrParser = mapper.getFactory().createParser("[]");
        arrParser.nextToken();
        try {
            deser.deserializeFromArray(arrParser, ctxt);
            Assert.fail("Expected failure for unexpected token array");
        } catch (JsonMappingException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testDeserializeFromArrayEmptyAccepted() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT);
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser arrParser = mapper.getFactory().createParser("[]");
        arrParser.nextToken();
        Object result = deser.deserializeFromArray(arrParser, ctxt);
        Assert.assertNull(result);
    }

    @Test
    public void testDeserializeFromEmbedded() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        TokenBuffer buf = new TokenBuffer(mapper, false);
        DummyBean bean = new DummyBean();
        buf.writeObject(bean);
        JsonParser p = buf.asParser();
        p.nextToken();

        Object res = deser.deserializeFromEmbedded(p, ctxt);
        Assert.assertSame(bean, res);
    }

    @Test
    public void testDeserializeWithObjectIdDelegates() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonParser p = mapper.getFactory().createParser("{}");
        p.nextToken();
        Object res = deser.deserializeWithObjectId(p, ctxt);
        Assert.assertTrue(res instanceof DummyBean);
    }

    @Test
    public void testHandlePolymorphicNoSubDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteBeanDeserializer deser = createTestDeserializer(mapper, DummyBean.class);
        ConcreteBeanDeserializer ignoringDeser = new ConcreteBeanDeserializer(deser, true);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        DummyBean bean = new DummyBean();
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeFieldName("unknownField");
        tb.writeString("someValue");

        Object res = ignoringDeser.handlePolymorphic(null, ctxt, bean, tb);
        Assert.assertSame(bean, res);
    }
}
