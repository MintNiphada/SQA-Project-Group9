package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsArrayTypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class InnerClassPropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {
        String value() default "";
    }

    private static class Outer {
        public class Inner {
            public String value;
            public Inner() {}
        }

        public class FailingInner {
            public FailingInner() {
                throw new RuntimeException("Constructor failed intentionally");
            }
        }
    }

    private static class DummySettableBeanProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private int _index = -1;
        private Object _lastSetInstance;
        private Object _lastSetValue;

        public DummySettableBeanProperty(PropertyName name, JavaType type) {
            super(name, type, null, null);
        }

        public DummySettableBeanProperty(PropertyName name, JavaType type, TypeDeserializer typeDeser) {
            super(name, type, null, typeDeser);
        }

        protected DummySettableBeanProperty(DummySettableBeanProperty src) {
            super(src);
            this._index = src._index;
            this._lastSetInstance = src._lastSetInstance;
            this._lastSetValue = src._lastSetValue;
        }

        protected DummySettableBeanProperty(DummySettableBeanProperty src, JsonDeserializer<?> deser) {
            super(src, deser);
            this._index = src._index;
        }

        protected DummySettableBeanProperty(DummySettableBeanProperty src, PropertyName newName) {
            super(src, newName);
            this._index = src._index;
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new DummySettableBeanProperty(this, deser);
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new DummySettableBeanProperty(this, newName);
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException {
            set(instance, deserialize(jp, ctxt));
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser jp, DeserializationContext ctxt, Object instance) throws IOException {
            return setAndReturn(instance, deserialize(jp, ctxt));
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            _lastSetInstance = instance;
            _lastSetValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            set(instance, value);
            return instance;
        }

        @Override
        public void assignIndex(int index) {
            this._index = index;
        }

        @Override
        public int getPropertyIndex() {
            return this._index;
        }
    }

    private ObjectMapper _mapper;
    private Constructor<?> _innerCtor;
    private Constructor<?> _failingInnerCtor;
    private DummySettableBeanProperty _delegate;

    @Before
    public void setUp() throws Exception {
        _mapper = new ObjectMapper();
        _innerCtor = Outer.Inner.class.getDeclaredConstructor(Outer.class);
        _failingInnerCtor = Outer.FailingInner.class.getDeclaredConstructor(Outer.class);
        JavaType type = TypeFactory.defaultInstance().constructType(Outer.Inner.class);
        _delegate = new DummySettableBeanProperty(new PropertyName("inner"), type);
    }

    @Test
    public void testBasicCreationAndDelegation() {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _innerCtor);

        prop.assignIndex(42);
        Assert.assertEquals(42, prop.getPropertyIndex());
        Assert.assertNull(prop.getAnnotation(TestAnnotation.class));
        Assert.assertNull(prop.getMember());
        Assert.assertEquals("inner", prop.getName());
    }

    @Test
    public void testWithNameAndWithValueDeserializer() {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _innerCtor);

        PropertyName newName = new PropertyName("renamedInner");
        InnerClassProperty renamedProp = prop.withName(newName);
        Assert.assertEquals("renamedInner", renamedProp.getName());
        Assert.assertEquals(_innerCtor, renamedProp._creator);

        JsonDeserializer<Object> deser = _mapper.getDeserializationContext().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(Outer.Inner.class), null);
        InnerClassProperty withDeser = prop.withValueDeserializer(deser);
        Assert.assertEquals(deser, withDeser.getValueDeserializer());
        Assert.assertEquals(_innerCtor, withDeser._creator);
    }

    @Test
    public void testSetAndSetAndReturn() throws IOException {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _innerCtor);
        Outer outer = new Outer();
        Outer.Inner inner = outer.new Inner();

        prop.set(outer, inner);
        Assert.assertSame(outer, _delegate._lastSetInstance);
        Assert.assertSame(inner, _delegate._lastSetValue);

        Object returned = prop.setAndReturn(outer, inner);
        Assert.assertSame(outer, returned);
    }

    @Test
    public void testDeserializeAndSetNormalFlow() throws IOException {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _innerCtor);
        JsonDeserializer<Object> deser = _mapper.getDeserializationContext().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(Outer.Inner.class), null);
        prop = prop.withValueDeserializer(deser);

        Outer outer = new Outer();
        JsonParser jp = new JsonFactory().createParser("{\"value\":\"test\"}");
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        jp.nextToken();
        prop.deserializeAndSet(jp, ctxt, outer);

        Assert.assertSame(outer, _delegate._lastSetInstance);
        Assert.assertNotNull(_delegate._lastSetValue);
        Assert.assertTrue(_delegate._lastSetValue instanceof Outer.Inner);
        Assert.assertEquals("test", ((Outer.Inner) _delegate._lastSetValue).value);
        jp.close();
    }

    @Test
    public void testDeserializeAndSetWithNullToken() throws IOException {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _innerCtor);
        JsonDeserializer<Object> deser = _mapper.getDeserializationContext().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(Outer.Inner.class), null);
        prop = prop.withValueDeserializer(deser);

        Outer outer = new Outer();
        JsonParser jp = new JsonFactory().createParser("null");
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        jp.nextToken();
        Assert.assertEquals(JsonToken.VALUE_NULL, jp.getCurrentToken());
        prop.deserializeAndSet(jp, ctxt, outer);

        Assert.assertSame(outer, _delegate._lastSetInstance);
        Assert.assertNull(_delegate._lastSetValue);
        jp.close();
    }

    @Test
    public void testDeserializeAndSetWithTypeDeserializer() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(Outer.Inner.class);
        TypeDeserializer typeDeser = new AsArrayTypeDeserializer(type, null, "type", false, null);
        DummySettableBeanProperty delegateWithType = new DummySettableBeanProperty(new PropertyName("inner"), type, typeDeser);
        InnerClassProperty prop = new InnerClassProperty(delegateWithType, _innerCtor);

        JsonDeserializer<Object> deser = _mapper.getDeserializationContext().findRootValueDeserializer(type, null);
        prop = prop.withValueDeserializer(deser);

        Outer outer = new Outer();
        JsonParser jp = new JsonFactory().createParser("[\"" + Outer.Inner.class.getName() + "\", {\"value\":\"arrayTyped\"}]");
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        jp.nextToken();
        prop.deserializeAndSet(jp, ctxt, outer);

        Assert.assertSame(outer, _delegate._lastSetInstance == null ? outer : _delegate._lastSetInstance);
        jp.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeserializeAndSetConstructorFailure() throws IOException {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _failingInnerCtor);
        JsonDeserializer<Object> deser = _mapper.getDeserializationContext().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(Outer.FailingInner.class), null);
        prop = prop.withValueDeserializer(deser);

        Outer outer = new Outer();
        JsonParser jp = new JsonFactory().createParser("{\"value\":\"fail\"}");
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        jp.nextToken();
        try {
            prop.deserializeAndSet(jp, ctxt, outer);
        } finally {
            jp.close();
        }
    }

    @Test
    public void testDeserializeSetAndReturn() throws IOException {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _innerCtor);
        JsonDeserializer<Object> deser = _mapper.getDeserializationContext().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(Outer.Inner.class), null);
        prop = prop.withValueDeserializer(deser);

        Outer outer = new Outer();
        JsonParser jp = new JsonFactory().createParser("{\"value\":\"returnTest\"}");
        DeserializationContext ctxt = _mapper.getDeserializationContext();

        jp.nextToken();
        Object result = prop.deserializeSetAndReturn(jp, ctxt, outer);

        Assert.assertSame(outer, result);
        Assert.assertNotNull(_delegate._lastSetValue);
        Assert.assertTrue(_delegate._lastSetValue instanceof Outer.Inner);
        Assert.assertEquals("returnTest", ((Outer.Inner) _delegate._lastSetValue).value);
        jp.close();
    }

    @Test
    public void testSerializationReadResolveAndWriteReplace() {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _innerCtor);

        Object replaced = prop.writeReplace();
        Assert.assertTrue(replaced instanceof InnerClassProperty);
        InnerClassProperty propWithAnn = (InnerClassProperty) replaced;
        Assert.assertNotNull(propWithAnn._annotated);

        Object replacedAgain = propWithAnn.writeReplace();
        Assert.assertSame(propWithAnn, replacedAgain);

        Object resolved = propWithAnn.readResolve();
        Assert.assertTrue(resolved instanceof InnerClassProperty);
        InnerClassProperty propResolved = (InnerClassProperty) resolved;
        Assert.assertEquals(_innerCtor, propResolved._creator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullAnnotatedConstructor() {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _innerCtor);
        new InnerClassProperty(prop, (AnnotatedConstructor) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithAnnotatedConstructorReturningNull() {
        InnerClassProperty prop = new InnerClassProperty(_delegate, _innerCtor);
        AnnotatedConstructor emptyAnn = new AnnotatedConstructor(null, null, new AnnotationMap(), null);
        new InnerClassProperty(prop, emptyAnn);
    }
}
