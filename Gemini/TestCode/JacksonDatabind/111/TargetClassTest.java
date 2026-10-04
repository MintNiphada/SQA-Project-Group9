package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;

public class FieldPropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomAnnotation {
        String value() default "test";
    }

    public static class SampleBean {
        @CustomAnnotation("fieldVal")
        public String name;
        public Integer age;
        final public String readonly = "const";
    }

    public static class FakePropDef extends BeanPropertyDefinition {
        private final PropertyName _name;

        public FakePropDef(String name) {
            _name = PropertyName.construct(name);
        }

        @Override public PropertyName getFullName() { return _name; }
        @Override public String getName() { return _name.getSimpleName(); }
        @Override public PropertyName getWrapperName() { return null; }
        @Override public boolean isExplicitlyIncluded() { return true; }
        @Override public boolean hasGetter() { return false; }
        @Override public boolean hasSetter() { return false; }
        @Override public boolean hasField() { return true; }
        @Override public boolean hasConstructorParameter() { return false; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMethod getGetter() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMethod getSetter() { return null; }
        @Override public AnnotatedField getField() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedParameter getConstructorParameter() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedMember getPrimaryMember() { return null; }
    }

    private ObjectMapper _mapper;
    private JavaType _stringType;
    private AnnotatedField _annotatedNameField;
    private FieldProperty _fieldProp;

    @Before
    public void setUp() throws Exception {
        _mapper = new ObjectMapper();
        _stringType = TypeFactory.defaultInstance().constructType(String.class);

        Field f = SampleBean.class.getField("name");
        AnnotatedClass ac = AnnotatedClassResolver.resolve(_mapper.getDeserializationConfig(), _stringType, null);
        TypeResolutionContext typeContext = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), ac.getBindings());
        AnnotationMap annMap = new AnnotationMap();
        for (java.lang.annotation.Annotation a : f.getAnnotations()) {
            annMap.add(a);
        }
        _annotatedNameField = new AnnotatedField(typeContext, f, annMap);

        FakePropDef propDef = new FakePropDef("name");
        _fieldProp = new FieldProperty(propDef, _stringType, null, annMap, _annotatedNameField);
    }

    @Test
    public void testBasicProperties() {
        Assert.assertEquals("name", _fieldProp.getName());
        Assert.assertSame(_annotatedNameField, _fieldProp.getMember());
        CustomAnnotation ann = _fieldProp.getAnnotation(CustomAnnotation.class);
        Assert.assertNotNull(ann);
        Assert.assertEquals("fieldVal", ann.value());
        Assert.assertNull(_fieldProp.getAnnotation(Retention.class));
    }

    @Test
    public void testWithName() {
        SettableBeanProperty renamed = _fieldProp.withName(new PropertyName("newName"));
        Assert.assertEquals("newName", renamed.getName());
        Assert.assertSame(_annotatedNameField, renamed.getMember());
    }

    @Test
    public void testWithValueDeserializer() {
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "custom";
            }
        };
        SettableBeanProperty prop1 = _fieldProp.withValueDeserializer(deser);
        Assert.assertNotSame(_fieldProp, prop1);
        Assert.assertSame(deser, prop1.getValueDeserializer());

        SettableBeanProperty prop2 = prop1.withValueDeserializer(deser);
        Assert.assertSame(prop1, prop2);
    }

    @Test
    public void testWithNullProvider() {
        NullValueProvider nva = NullsConstantProvider.skipper();
        SettableBeanProperty prop = _fieldProp.withNullProvider(nva);
        Assert.assertNotSame(_fieldProp, prop);
        Assert.assertSame(nva, prop.getNullValueProvider());
    }

    @Test
    public void testFixAccess() {
        DeserializationConfig config = _mapper.getDeserializationConfig().with(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        _fieldProp.fixAccess(config);
    }

    @Test
    public void testSetAndReturn() throws IOException {
        SampleBean bean = new SampleBean();
        Object res = _fieldProp.setAndReturn(bean, "updatedName");
        Assert.assertSame(bean, res);
        Assert.assertEquals("updatedName", bean.name);

        _fieldProp.set(bean, "updatedAgain");
        Assert.assertEquals("updatedAgain", bean.name);
    }

    @Test(expected = IOException.class)
    public void testSetFailure() throws IOException {
        _fieldProp.set(new Object(), "val");
    }

    @Test(expected = IOException.class)
    public void testSetAndReturnFailure() throws IOException {
        _fieldProp.setAndReturn(new Object(), "val");
    }

    @Test
    public void testDeserializeAndSetNormal() throws Exception {
        SampleBean bean = new SampleBean();
        JsonParser p = _mapper.getFactory().createParser("\"parsedValue\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "parsedValue";
            }
        };
        SettableBeanProperty prop = _fieldProp.withValueDeserializer(deser);
        prop.deserializeAndSet(p, ctxt, bean);
        Assert.assertEquals("parsedValue", bean.name);
    }

    @Test
    public void testDeserializeSetAndReturnNormal() throws Exception {
        SampleBean bean = new SampleBean();
        JsonParser p = _mapper.getFactory().createParser("\"parsedValue2\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "parsedValue2";
            }
        };
        SettableBeanProperty prop = _fieldProp.withValueDeserializer(deser);
        Object res = prop.deserializeSetAndReturn(p, ctxt, bean);
        Assert.assertSame(bean, res);
        Assert.assertEquals("parsedValue2", bean.name);
    }

    @Test
    public void testDeserializeAndSetNullTokenWithNullProvider() throws Exception {
        SampleBean bean = new SampleBean();
        bean.name = "initial";
        JsonParser p = _mapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        NullValueProvider nva = NullsConstantProvider.forValue("fallbackNull");
        SettableBeanProperty prop = _fieldProp.withNullProvider(nva);
        prop.deserializeAndSet(p, ctxt, bean);
        Assert.assertEquals("fallbackNull", bean.name);
    }

    @Test
    public void testDeserializeAndSetNullTokenSkipped() throws Exception {
        SampleBean bean = new SampleBean();
        bean.name = "preserve";
        JsonParser p = _mapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        NullValueProvider nva = NullsConstantProvider.skipper();
        SettableBeanProperty prop = _fieldProp.withNullProvider(nva);
        prop.deserializeAndSet(p, ctxt, bean);
        Assert.assertEquals("preserve", bean.name);
    }

    @Test
    public void testDeserializeSetAndReturnNullTokenSkipped() throws Exception {
        SampleBean bean = new SampleBean();
        bean.name = "preserve";
        JsonParser p = _mapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        NullValueProvider nva = NullsConstantProvider.skipper();
        SettableBeanProperty prop = _fieldProp.withNullProvider(nva);
        Object res = prop.deserializeSetAndReturn(p, ctxt, bean);
        Assert.assertSame(bean, res);
        Assert.assertEquals("preserve", bean.name);
    }

    @Test
    public void testDeserializeSetAndReturnNullTokenWithNullProvider() throws Exception {
        SampleBean bean = new SampleBean();
        bean.name = "initial";
        JsonParser p = _mapper.getFactory().createParser("null");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        NullValueProvider nva = NullsConstantProvider.forValue("fallbackNull");
        SettableBeanProperty prop = _fieldProp.withNullProvider(nva);
        Object res = prop.deserializeSetAndReturn(p, ctxt, bean);
        Assert.assertSame(bean, res);
        Assert.assertEquals("fallbackNull", bean.name);
    }

    @Test
    public void testDeserializeAndSetCoercedNull() throws Exception {
        SampleBean bean = new SampleBean();
        bean.name = "initial";
        JsonParser p = _mapper.getFactory().createParser("\"\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };
        NullValueProvider nva = NullsConstantProvider.forValue("nullProviderValue");
        SettableBeanProperty prop = _fieldProp.withValueDeserializer(deser).withNullProvider(nva);
        prop.deserializeAndSet(p, ctxt, bean);
        Assert.assertEquals("nullProviderValue", bean.name);
    }

    @Test
    public void testDeserializeAndSetCoercedNullSkipped() throws Exception {
        SampleBean bean = new SampleBean();
        bean.name = "initial";
        JsonParser p = _mapper.getFactory().createParser("\"\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };
        NullValueProvider nva = NullsConstantProvider.skipper();
        SettableBeanProperty prop = _fieldProp.withValueDeserializer(deser).withNullProvider(nva);
        prop.deserializeAndSet(p, ctxt, bean);
        Assert.assertEquals("initial", bean.name);
    }

    @Test
    public void testDeserializeSetAndReturnCoercedNullSkipped() throws Exception {
        SampleBean bean = new SampleBean();
        bean.name = "initial";
        JsonParser p = _mapper.getFactory().createParser("\"\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };
        NullValueProvider nva = NullsConstantProvider.skipper();
        SettableBeanProperty prop = _fieldProp.withValueDeserializer(deser).withNullProvider(nva);
        Object res = prop.deserializeSetAndReturn(p, ctxt, bean);
        Assert.assertSame(bean, res);
        Assert.assertEquals("initial", bean.name);
    }

    @Test
    public void testDeserializeSetAndReturnCoercedNullWithNullProvider() throws Exception {
        SampleBean bean = new SampleBean();
        bean.name = "initial";
        JsonParser p = _mapper.getFactory().createParser("\"\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };
        NullValueProvider nva = NullsConstantProvider.forValue("coercedNull");
        SettableBeanProperty prop = _fieldProp.withValueDeserializer(deser).withNullProvider(nva);
        Object res = prop.deserializeSetAndReturn(p, ctxt, bean);
        Assert.assertSame(bean, res);
        Assert.assertEquals("coercedNull", bean.name);
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        SampleBean bean = new SampleBean();
        JsonParser p = _mapper.getFactory().createParser("[\"type\", \"typedVal\"]");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }

            @Override
            public Object deserializeWithType(JsonParser p, DeserializationContext ctxt, TypeDeserializer typeDeserializer) {
                return "typedValue";
            }
        };

        TypeDeserializer typeDeser = _mapper.getDeserializationConfig().getDefaultTyper(_stringType) != null
                ? _mapper.getDeserializationConfig().getDefaultTyper(_stringType).buildTypeDeserializer(_mapper.getDeserializationConfig(), _stringType, null)
                : null;

        FakePropDef propDef = new FakePropDef("name");
        FieldProperty typedProp = new FieldProperty(propDef, _stringType, typeDeser, null, _annotatedNameField);
        SettableBeanProperty prop = typedProp.withValueDeserializer(deser);

        if (typeDeser != null) {
            prop.deserializeAndSet(p, ctxt, bean);
            Assert.assertEquals("typedValue", bean.name);
            Object res = prop.deserializeSetAndReturn(p, ctxt, bean);
            Assert.assertSame(bean, res);
        }
    }

    @Test(expected = IOException.class)
    public void testDeserializeAndSetExceptionOnField() throws Exception {
        JsonParser p = _mapper.getFactory().createParser("\"value\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "value";
            }
        };
        SettableBeanProperty prop = _fieldProp.withValueDeserializer(deser);
        prop.deserializeAndSet(p, ctxt, new Object());
    }

    @Test(expected = IOException.class)
    public void testDeserializeSetAndReturnExceptionOnField() throws Exception {
        JsonParser p = _mapper.getFactory().createParser("\"value\"");
        p.nextToken();
        DeserializationContext ctxt = _mapper.getDeserializationContext();
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "value";
            }
        };
        SettableBeanProperty prop = _fieldProp.withValueDeserializer(deser);
        prop.deserializeSetAndReturn(p, ctxt, new Object());
    }

    @Test
    public void testReadResolve() {
        Object resolved = _fieldProp.readResolve();
        Assert.assertNotNull(resolved);
        Assert.assertTrue(resolved instanceof FieldProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadResolveBrokenField() throws Exception {
        AnnotatedField brokenField = new AnnotatedField(null, null, null);
        FakePropDef propDef = new FakePropDef("broken");
        try {
            FieldProperty brokenProp = new FieldProperty(propDef, _stringType, null, null, brokenField);
            brokenProp.readResolve();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
