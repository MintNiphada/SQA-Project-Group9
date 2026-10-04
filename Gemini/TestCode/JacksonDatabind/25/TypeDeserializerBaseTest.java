package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TypeDeserializerBaseTest {

    private static class ConcreteTypeDeserializer extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;
        private final JsonTypeInfo.As _inclusion;

        public ConcreteTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                                        String typePropertyName, boolean typeIdVisible,
                                        Class<?> defaultImpl, JsonTypeInfo.As inclusion) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
            _inclusion = inclusion;
        }

        public ConcreteTypeDeserializer(ConcreteTypeDeserializer src, BeanProperty prop) {
            super(src, prop);
            _inclusion = src._inclusion;
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new ConcreteTypeDeserializer(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return _inclusion;
        }
    }

    private static class DummyTypeIdResolver extends TypeIdResolverBase {
        private final String _knownIds;
        private JavaType _typeToReturn;

        public DummyTypeIdResolver(String knownIds) {
            this(knownIds, null);
        }

        public DummyTypeIdResolver(String knownIds, JavaType typeToReturn) {
            _knownIds = knownIds;
            _typeToReturn = typeToReturn;
        }

        @Override
        public String idFromValue(Object value) {
            return null;
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return null;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }

        @Override
        public String getDescForKnownTypeIds() {
            return _knownIds;
        }

        @Override
        public JavaType typeFromId(DeserializationContext context, String id) {
            return _typeToReturn;
        }
    }

    @Test
    public void testGettersAndToString() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver("testIds");
        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", true, String.class, JsonTypeInfo.As.PROPERTY);

        Assert.assertEquals("java.lang.CharSequence", deser.baseTypeName());
        Assert.assertEquals("@type", deser.getPropertyName());
        Assert.assertSame(idResolver, deser.getTypeIdResolver());
        Assert.assertEquals(String.class, deser.getDefaultImpl());
        Assert.assertEquals(JsonTypeInfo.As.PROPERTY, deser.getTypeInclusion());

        String str = deser.toString();
        Assert.assertTrue(str.contains(ConcreteTypeDeserializer.class.getName()));
        Assert.assertTrue(str.contains("base-type:"));
        Assert.assertTrue(str.contains("id-resolver:"));

        ConcreteTypeDeserializer nullDefaultDeser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", true, null, JsonTypeInfo.As.PROPERTY);
        Assert.assertNull(nullDefaultDeser.getDefaultImpl());

        BeanProperty prop = new BeanProperty.Std(PropertyName.construct("prop"), baseType, null, null, (AnnotatedMember) null, PropertyMetadata.STD_REQUIRED);
        TypeDeserializer copy = deser.forProperty(prop);
        Assert.assertTrue(copy instanceof ConcreteTypeDeserializer);
        Assert.assertEquals(String.class, copy.getDefaultImpl());
        Assert.assertEquals("@type", copy.getPropertyName());
    }

    @Test
    public void testFindDeserializerResolvedFromId() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Number.class);
        JavaType subType = SimpleType.constructUnsafe(Integer.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, subType);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, null, JsonTypeInfo.As.PROPERTY);

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> mockDeserializer = Mockito.mock(JsonDeserializer.class);

        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.any(JavaType.class), Mockito.nullable(BeanProperty.class)))
                .thenReturn(mockDeserializer);

        JsonDeserializer<Object> result1 = deser._findDeserializer(ctxt, "int");
        Assert.assertSame(mockDeserializer, result1);

        JsonDeserializer<Object> result2 = deser._findDeserializer(ctxt, "int");
        Assert.assertSame(mockDeserializer, result2);
        Mockito.verify(ctxt, Mockito.times(1)).findContextualValueDeserializer(Mockito.any(JavaType.class), Mockito.nullable(BeanProperty.class));
    }

    @Test
    public void testFindDeserializerResolvedWithBaseTypeNull() throws Exception {
        JavaType subType = SimpleType.constructUnsafe(Integer.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, subType);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                null, idResolver, "@type", false, null, JsonTypeInfo.As.PROPERTY);

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> mockDeserializer = Mockito.mock(JsonDeserializer.class);

        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.eq(subType), Mockito.nullable(BeanProperty.class)))
                .thenReturn(mockDeserializer);

        JsonDeserializer<Object> result = deser._findDeserializer(ctxt, "int");
        Assert.assertSame(mockDeserializer, result);
    }

    @Test
    public void testFindDefaultImplDeserializerWhenDefaultImplNull() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Number.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, null);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, null, JsonTypeInfo.As.PROPERTY);

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(false);

        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        Assert.assertSame(NullifyingDeserializer.instance, defaultDeser);

        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        Assert.assertNull(defaultDeser);
    }

    @Test
    public void testFindDefaultImplDeserializerWhenBogusClass() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Void.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, null);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, Void.class, JsonTypeInfo.As.PROPERTY);

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonDeserializer<Object> defaultDeser = deser._findDefaultImplDeserializer(ctxt);
        Assert.assertSame(NullifyingDeserializer.instance, defaultDeser);
    }

    @Test
    public void testFindDefaultImplDeserializerValidImpl() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(CharSequence.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, null);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, String.class, JsonTypeInfo.As.PROPERTY);

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> mockDeser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.any(JavaType.class), Mockito.nullable(BeanProperty.class)))
                .thenReturn(mockDeser);

        JsonDeserializer<Object> defaultDeser1 = deser._findDefaultImplDeserializer(ctxt);
        Assert.assertSame(mockDeser, defaultDeser1);

        JsonDeserializer<Object> defaultDeser2 = deser._findDefaultImplDeserializer(ctxt);
        Assert.assertSame(mockDeser, defaultDeser2);
        Mockito.verify(ctxt, Mockito.times(1)).findContextualValueDeserializer(Mockito.any(JavaType.class), Mockito.nullable(BeanProperty.class));
    }

    @Test
    public void testFindDeserializerUnknownTypeIdWithCustomResolver() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(CharSequence.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver("knownIdA, knownIdB", null);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, null, JsonTypeInfo.As.PROPERTY);

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        Mockito.when(ctxt.unknownTypeException(Mockito.eq(baseType), Mockito.eq("unknownId"), Mockito.eq("known type ids = knownIdA, knownIdB")))
                .thenReturn(new JsonMappingException("unknown type"));

        try {
            deser._findDeserializer(ctxt, "unknownId");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertEquals("unknown type", e.getMessage());
        }
    }

    @Test
    public void testFindDeserializerUnknownTypeIdWithNullKnownIds() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(CharSequence.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, null);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, null, JsonTypeInfo.As.PROPERTY);

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        Mockito.when(ctxt.unknownTypeException(Mockito.eq(baseType), Mockito.eq("unknownId"), Mockito.eq("known type ids are not statically known")))
                .thenReturn(new JsonMappingException("unknown type with no known ids"));

        try {
            deser._findDeserializer(ctxt, "unknownId");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertEquals("unknown type with no known ids", e.getMessage());
        }
    }

    @Test
    public void testFindDeserializerUnknownTypeIdWithNonBaseResolver() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(CharSequence.class);
        TypeIdResolver nonBaseResolver = Mockito.mock(TypeIdResolver.class);
        Mockito.when(nonBaseResolver.typeFromId(Mockito.any(DeserializationContext.class), Mockito.anyString()))
                .thenReturn(null);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, nonBaseResolver, "@type", false, null, JsonTypeInfo.As.PROPERTY);

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        Mockito.when(ctxt.unknownTypeException(Mockito.eq(baseType), Mockito.eq("unknownId"), Mockito.isNull(String.class)))
                .thenReturn(new JsonMappingException("unknown type for generic resolver"));

        try {
            deser._findDeserializer(ctxt, "unknownId");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertEquals("unknown type for generic resolver", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeserializeWithNativeTypeIdDeprecated() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Number.class);
        JavaType subType = SimpleType.constructUnsafe(Integer.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, subType);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, null, JsonTypeInfo.As.PROPERTY);

        JsonParser jp = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> mockDeser = Mockito.mock(JsonDeserializer.class);

        Mockito.when(jp.getTypeId()).thenReturn("int");
        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.any(JavaType.class), Mockito.nullable(BeanProperty.class)))
                .thenReturn(mockDeser);
        Mockito.when(mockDeser.deserialize(jp, ctxt)).thenReturn(Integer.valueOf(123));

        Object result = deser._deserializeWithNativeTypeId(jp, ctxt);
        Assert.assertEquals(Integer.valueOf(123), result);
    }

    @Test
    public void testDeserializeWithNativeTypeIdNonString() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(Number.class);
        JavaType subType = SimpleType.constructUnsafe(Integer.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, subType);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, null, JsonTypeInfo.As.PROPERTY);

        JsonParser jp = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> mockDeser = Mockito.mock(JsonDeserializer.class);

        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.any(JavaType.class), Mockito.nullable(BeanProperty.class)))
                .thenReturn(mockDeser);
        Mockito.when(mockDeser.deserialize(jp, ctxt)).thenReturn(Integer.valueOf(456));

        Object result = deser._deserializeWithNativeTypeId(jp, ctxt, 12345);
        Assert.assertEquals(Integer.valueOf(456), result);
    }

    @Test
    public void testDeserializeWithNativeTypeIdNullWithDefaultImpl() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(CharSequence.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, null);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, String.class, JsonTypeInfo.As.PROPERTY);

        JsonParser jp = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> mockDeser = Mockito.mock(JsonDeserializer.class);

        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.any(JavaType.class), Mockito.nullable(BeanProperty.class)))
                .thenReturn(mockDeser);
        Mockito.when(mockDeser.deserialize(jp, ctxt)).thenReturn("default-result");

        Object result = deser._deserializeWithNativeTypeId(jp, ctxt, null);
        Assert.assertEquals("default-result", result);
    }

    @Test
    public void testDeserializeWithNativeTypeIdNullWithoutDefaultImplThrows() throws Exception {
        JavaType baseType = SimpleType.constructUnsafe(CharSequence.class);
        TypeIdResolver idResolver = new DummyTypeIdResolver(null, null);

        ConcreteTypeDeserializer deser = new ConcreteTypeDeserializer(
                baseType, idResolver, "@type", false, null, JsonTypeInfo.As.PROPERTY);

        JsonParser jp = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        Mockito.when(ctxt.mappingException(Mockito.anyString())).thenReturn(new JsonMappingException("No native type id"));

        try {
            deser._deserializeWithNativeTypeId(jp, ctxt, null);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertEquals("No native type id", e.getMessage());
        }
    }
}
