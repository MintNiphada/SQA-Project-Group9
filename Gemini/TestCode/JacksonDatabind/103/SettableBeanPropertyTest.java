package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;

public class SettableBeanPropertyTest {

    static class DummyProperty extends SettableBeanProperty {
        private Object value;
        private AnnotatedMember member;
        private Annotations annotations;

        public DummyProperty(PropertyName propName, JavaType type, PropertyName wrapper,
                             TypeDeserializer typeDeser, Annotations contextAnnotations,
                             PropertyMetadata metadata) {
            super(propName, type, wrapper, typeDeser, contextAnnotations, metadata);
            this.annotations = contextAnnotations;
        }

        public DummyProperty(BeanPropertyDefinition propDef, JavaType type,
                             TypeDeserializer typeDeser, Annotations contextAnnotations) {
            super(propDef, type, typeDeser, contextAnnotations);
            this.annotations = contextAnnotations;
        }

        public DummyProperty(PropertyName propName, JavaType type,
                             PropertyMetadata metadata, JsonDeserializer<Object> valueDeser) {
            super(propName, type, metadata, valueDeser);
        }

        public DummyProperty(DummyProperty src) {
            super(src);
            this.value = src.value;
            this.member = src.member;
            this.annotations = src.annotations;
        }

        public DummyProperty(DummyProperty src, JsonDeserializer<?> deser, NullValueProvider nuller) {
            super(src, deser, nuller);
            this.value = src.value;
            this.member = src.member;
            this.annotations = src.annotations;
        }

        public DummyProperty(DummyProperty src, PropertyName newName) {
            super(src, newName);
            this.value = src.value;
            this.member = src.member;
            this.annotations = src.annotations;
        }

        public void setMember(AnnotatedMember m) {
            this.member = m;
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new DummyProperty(this, deser, this._nullProvider);
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new DummyProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) {
            return new DummyProperty(this, this._valueDeserializer, nva);
        }

        @Override
        public AnnotatedMember getMember() {
            return member;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return annotations == null ? null : annotations.get(acls);
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            this.value = deserialize(p, ctxt);
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            this.value = deserialize(p, ctxt);
            return instance;
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            this.value = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            this.value = value;
            return instance;
        }

        public Object getLastSetValue() {
            return value;
        }
    }

    static class DummyDelegating extends SettableBeanProperty.Delegating {
        public DummyDelegating(SettableBeanProperty delegate) {
            super(delegate);
        }

        @Override
        protected SettableBeanProperty withDelegate(SettableBeanProperty d) {
            return new DummyDelegating(d);
        }
    }

    static class DummyBeanPropDef extends BeanPropertyDefinition {
        private final PropertyName name;
        private final PropertyMetadata metadata;

        public DummyBeanPropDef(String simpleName, PropertyMetadata metadata) {
            this.name = PropertyName.construct(simpleName);
            this.metadata = metadata;
        }

        @Override public PropertyName getFullName() { return name; }
        @Override public String getName() { return name.getSimpleName(); }
        @Override public PropertyName getWrapperName() { return PropertyName.construct("wrapper"); }
        @Override public PropertyMetadata getMetadata() { return metadata; }
        @Override public boolean isExplicitlyIncluded() { return true; }
        @Override public boolean hasGetter() { return false; }
        @Override public boolean hasSetter() { return false; }
        @Override public boolean hasField() { return false; }
        @Override public boolean hasConstructorParameter() { return false; }
        @Override public AnnotatedMethod getGetter() { return null; }
        @Override public AnnotatedMethod getSetter() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedField getField() { return null; }
        @Override public com.fasterxml.jackson.databind.introspect.AnnotatedParameter getConstructorParameter() { return null; }
        @Override public AnnotatedMember getAccessor() { return null; }
        @Override public AnnotatedMember getMutator() { return null; }
        @Override public AnnotatedMember getNonConstructorMutator() { return null; }
        @Override public AnnotatedMember getPrimaryMember() { return null; }
    }

    static class DummyVisitor extends JsonObjectFormatVisitor.Base {
        public BeanProperty seenProperty;
        public boolean seenRequired;

        @Override
        public void property(BeanProperty prop) {
            this.seenProperty = prop;
            this.seenRequired = true;
        }

        @Override
        public void optionalProperty(BeanProperty prop) {
            this.seenProperty = prop;
            this.seenRequired = false;
        }
    }

    @Test
    public void testConstructorsAndBasicGetters() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty prop = new DummyProperty(new PropertyName("prop1"), type, PropertyName.construct("wrap"),
                null, null, PropertyMetadata.STD_OPTIONAL);

        Assert.assertEquals("prop1", prop.getName());
        Assert.assertEquals("prop1", prop.getFullName().getSimpleName());
        Assert.assertEquals("wrap", prop.getWrapperName().getSimpleName());
        Assert.assertEquals(type, prop.getType());
        Assert.assertFalse(prop.hasValueDeserializer());
        Assert.assertNull(prop.getValueDeserializer());
        Assert.assertFalse(prop.hasValueTypeDeserializer());
        Assert.assertNull(prop.getValueTypeDeserializer());
        Assert.assertEquals(-1, prop.getPropertyIndex());
        Assert.assertNull(prop.getManagedReferenceName());
        Assert.assertNull(prop.getObjectIdInfo());
        Assert.assertNull(prop.getInjectableValueId());
        Assert.assertEquals("[property 'prop1']", prop.toString());
        Assert.assertFalse(prop.isIgnorable());

        prop.markAsIgnorable();
        Assert.assertFalse(prop.isIgnorable());
    }

    @Test
    public void testBeanPropertyDefinitionConstructor() {
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        DummyBeanPropDef def = new DummyBeanPropDef("numProp", PropertyMetadata.STD_REQUIRED);
        DummyProperty prop = new DummyProperty(def, type, null, null);

        Assert.assertEquals("numProp", prop.getName());
        Assert.assertEquals("wrapper", prop.getWrapperName().getSimpleName());
        Assert.assertTrue(prop.isRequired());
    }

    @Test
    public void testNullPropertyNameDefaultsToNoName() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty prop = new DummyProperty((PropertyName) null, type, null, null, null, PropertyMetadata.STD_OPTIONAL);
        Assert.assertEquals("", prop.getName());
        Assert.assertEquals(PropertyName.NO_NAME, prop.getFullName());

        DummyProperty prop2 = new DummyProperty((PropertyName) null, type, PropertyMetadata.STD_OPTIONAL, null);
        Assert.assertEquals("", prop2.getName());
        Assert.assertEquals(PropertyName.NO_NAME, prop2.getFullName());
    }

    @Test
    public void testObjectIdValuePropertyConstructor() {
        JavaType type = TypeFactory.defaultInstance().constructType(Long.class);
        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return 123L;
            }
        };
        DummyProperty prop = new DummyProperty(new PropertyName("oid"), type, PropertyMetadata.STD_OPTIONAL, deser);

        Assert.assertTrue(prop.hasValueDeserializer());
        Assert.assertSame(deser, prop.getValueDeserializer());
        Assert.assertSame(deser, prop.getNullValueProvider());
    }

    @Test
    public void testIndexAssignment() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty prop = new DummyProperty(new PropertyName("idxProp"), type, null, null, null, PropertyMetadata.STD_OPTIONAL);
        Assert.assertEquals(-1, prop.getPropertyIndex());
        prop.assignIndex(5);
        Assert.assertEquals(5, prop.getPropertyIndex());

        try {
            prop.assignIndex(10);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("already had index"));
        }
    }

    @Test
    public void testGetCreatorIndexThrowsException() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty prop = new DummyProperty(new PropertyName("creatorProp"), type, null, null, null, PropertyMetadata.STD_OPTIONAL);
        try {
            prop.getCreatorIndex();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("no creator index"));
        }
    }

    @Test
    public void testViews() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty prop = new DummyProperty(new PropertyName("vProp"), type, null, null, null, PropertyMetadata.STD_OPTIONAL);
        Assert.assertFalse(prop.hasViews());
        Assert.assertTrue(prop.visibleInView(Object.class));

        prop.setViews(new Class<?>[]{String.class});
        Assert.assertTrue(prop.hasViews());
        Assert.assertTrue(prop.visibleInView(String.class));
        Assert.assertFalse(prop.visibleInView(Integer.class));

        prop.setViews(null);
        Assert.assertFalse(prop.hasViews());
        Assert.assertTrue(prop.visibleInView(Integer.class));
    }

    @Test
    public void testManagedReferenceAndObjectId() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty prop = new DummyProperty(new PropertyName("refProp"), type, null, null, null, PropertyMetadata.STD_OPTIONAL);

        prop.setManagedReferenceName("myRef");
        Assert.assertEquals("myRef", prop.getManagedReferenceName());

        ObjectIdInfo oid = new ObjectIdInfo(PropertyName.construct("id"), Object.class, null, null);
        prop.setObjectIdInfo(oid);
        Assert.assertSame(oid, prop.getObjectIdInfo());
    }

    @Test
    public void testWithSimpleNameAndName() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty prop = new DummyProperty(new PropertyName("orig"), type, null, null, null, PropertyMetadata.STD_OPTIONAL);

        SettableBeanProperty same = prop.withSimpleName("orig");
        Assert.assertSame(prop, same);

        SettableBeanProperty renamed = prop.withSimpleName("renamed");
        Assert.assertNotSame(prop, renamed);
        Assert.assertEquals("renamed", renamed.getName());
    }

    @Test
    public void testWithValueDeserializerAndNullProvider() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty prop = new DummyProperty(new PropertyName("p"), type, null, null, null, PropertyMetadata.STD_OPTIONAL);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "deserialized";
            }
        };

        SettableBeanProperty p2 = prop.withValueDeserializer(deser);
        Assert.assertTrue(p2.hasValueDeserializer());
        Assert.assertSame(deser, p2.getValueDeserializer());

        SettableBeanProperty p3 = p2.withValueDeserializer(null);
        Assert.assertFalse(p3.hasValueDeserializer());
        Assert.assertNull(p3.getValueDeserializer());

        NullValueProvider nva = NullsConstantProvider.nuller();
        SettableBeanProperty p4 = p2.withNullProvider(nva);
        Assert.assertSame(nva, p4.getNullValueProvider());
    }

    @Test
    public void testDepositSchemaProperty() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty requiredProp = new DummyProperty(new PropertyName("req"), type, null, null, null, PropertyMetadata.STD_REQUIRED);
        DummyProperty optionalProp = new DummyProperty(new PropertyName("opt"), type, null, null, null, PropertyMetadata.STD_OPTIONAL);

        DummyVisitor visitor = new DummyVisitor();
        requiredProp.depositSchemaProperty(visitor, null);
        Assert.assertSame(requiredProp, visitor.seenProperty);
        Assert.assertTrue(visitor.seenRequired);

        optionalProp.depositSchemaProperty(visitor, null);
        Assert.assertSame(optionalProp, visitor.seenProperty);
        Assert.assertFalse(visitor.seenRequired);
    }

    @Test
    public void testDelegatingClass() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty delegate = new DummyProperty(new PropertyName("delProp"), type, PropertyName.construct("delWrap"),
                null, null, PropertyMetadata.STD_OPTIONAL);
        DummyDelegating delegating = new DummyDelegating(delegate);

        Assert.assertSame(delegate, delegating.getDelegate());
        Assert.assertEquals("delProp", delegating.getName());
        Assert.assertEquals("delProp", delegating.getFullName().getSimpleName());
        Assert.assertEquals("delWrap", delegating.getWrapperName().getSimpleName());
        Assert.assertFalse(delegating.hasValueDeserializer());
        Assert.assertNull(delegating.getValueDeserializer());
        Assert.assertFalse(delegating.hasValueTypeDeserializer());
        Assert.assertNull(delegating.getValueTypeDeserializer());
        Assert.assertNull(delegating.getManagedReferenceName());
        Assert.assertNull(delegating.getObjectIdInfo());
        Assert.assertNull(delegating.getInjectableValueId());
        Assert.assertFalse(delegating.hasViews());
        Assert.assertTrue(delegating.visibleInView(String.class));
        Assert.assertEquals(-1, delegating.getPropertyIndex());

        delegating.assignIndex(3);
        Assert.assertEquals(3, delegating.getPropertyIndex());
        Assert.assertEquals(3, delegate.getPropertyIndex());

        delegating.fixAccess(null);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "delegatedValue";
            }
        };

        SettableBeanProperty dWithDeser = delegating.withValueDeserializer(deser);
        Assert.assertTrue(dWithDeser.hasValueDeserializer());

        SettableBeanProperty dWithName = delegating.withName(PropertyName.construct("newName"));
        Assert.assertEquals("newName", dWithName.getName());

        SettableBeanProperty dWithNull = delegating.withNullProvider(NullsConstantProvider.nuller());
        Assert.assertSame(NullsConstantProvider.nuller(), dWithNull.getNullValueProvider());

        try {
            delegating.getCreatorIndex();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("no creator index"));
        }

        delegating.set(null, "val1");
        Assert.assertEquals("val1", delegate.getLastSetValue());

        Object ret = delegating.setAndReturn(new Object(), "val2");
        Assert.assertNotNull(ret);
        Assert.assertEquals("val2", delegate.getLastSetValue());

        delegating.deserializeAndSet(null, null, null);
        delegating.deserializeSetAndReturn(null, null, new Object());
    }

    @Test
    public void testExceptionHandling() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummyProperty prop = new DummyProperty(new PropertyName("errProp"), type, null, null, null, PropertyMetadata.STD_OPTIONAL);

        try {
            prop._throwAsIOE(new IllegalArgumentException("invalid argument"), 12345);
            Assert.fail("Expected JsonMappingException");
        } catch (IOException e) {
            Assert.assertTrue(e instanceof JsonMappingException);
            Assert.assertTrue(e.getMessage().contains("Problem deserializing property 'errProp'"));
            Assert.assertTrue(e.getMessage().contains("invalid argument"));
        }

        try {
            prop._throwAsIOE(new IllegalArgumentException((String) null), 12345);
            Assert.fail("Expected JsonMappingException");
        } catch (IOException e) {
            Assert.assertTrue(e instanceof JsonMappingException);
            Assert.assertTrue(e.getMessage().contains("no error message provided"));
        }

        try {
            prop._throwAsIOE(new RuntimeException("runtime problem"));
            Assert.fail("Expected RuntimeException");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof RuntimeException);
            Assert.assertEquals("runtime problem", e.getMessage());
        }

        try {
            prop._throwAsIOE(new IOException("direct IO error"));
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertEquals("direct IO error", e.getMessage());
        }

        try {
            prop._throwAsIOE((Exception) new Exception("generic checked exception"));
            Assert.fail("Expected JsonMappingException");
        } catch (IOException e) {
            Assert.assertTrue(e instanceof JsonMappingException);
            Assert.assertTrue(e.getMessage().contains("generic checked exception"));
        }
    }
}
