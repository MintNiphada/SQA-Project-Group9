package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class TypeDeserializerBaseTest {

    private static class DummyTypeDeserializer extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;

        public DummyTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                                    String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public DummyTypeDeserializer(DummyTypeDeserializer src, BeanProperty prop) {
            super(src, prop);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new DummyTypeDeserializer(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }
    }

    @Test
    public void testConstructorAndAccessors() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver resolver = Mockito.mock(TypeIdResolver.class);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, null, true, defaultImpl);

        Assert.assertEquals("", deser.getPropertyName());
        Assert.assertSame(resolver, deser.getTypeIdResolver());
        Assert.assertEquals(String.class, deser.getDefaultImpl());
        Assert.assertEquals(CharSequence.class.getName(), deser.baseTypeName());
        Assert.assertEquals(JsonTypeInfo.As.PROPERTY, deser.getTypeInclusion());

        String str = deser.toString();
        Assert.assertTrue(str.contains(DummyTypeDeserializer.class.getName()));
        Assert.assertTrue(str.contains("base-type:"));
        Assert.assertTrue(str.contains("id-resolver:"));
    }

    @Test
    public void testCopyConstructor() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver resolver = Mockito.mock(TypeIdResolver.class);
        DummyTypeDeserializer src = new DummyTypeDeserializer(baseType, resolver, "typeProp", false, null);

        BeanProperty prop = Mockito.mock(BeanProperty.class);
        DummyTypeDeserializer copy = (DummyTypeDeserializer) src.forProperty(prop);

        Assert.assertEquals("typeProp", copy.getPropertyName());
        Assert.assertSame(resolver, copy.getTypeIdResolver());
        Assert.assertNull(copy.getDefaultImpl());
        Assert.assertSame(prop, copy._property);
        Assert.assertFalse(copy._typeIdVisible);
    }

    @Test
    public void testFindDefaultImplDeserializer() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        DummyTypeDeserializer deserNoDefault = new DummyTypeDeserializer(baseType, null, "type", false, null);
        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(false);
        Assert.assertSame(NullifyingDeserializer.instance, deserNoDefault._findDefaultImplDeserializer(ctxt));

        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        Assert.assertNull(deserNoDefault._findDefaultImplDeserializer(ctxt));

        JavaType bogusType = TypeFactory.defaultInstance().constructType(Void.TYPE);
        DummyTypeDeserializer deserBogus = new DummyTypeDeserializer(baseType, null, "type", false, bogusType);
        Assert.assertSame(NullifyingDeserializer.instance, deserBogus._findDefaultImplDeserializer(ctxt));

        JavaType validDefault = TypeFactory.defaultInstance().constructType(String.class);
        DummyTypeDeserializer deserValid = new DummyTypeDeserializer(baseType, null, "type", false, validDefault);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.eq(validDefault), Mockito.isNull(BeanProperty.class)))
                .thenReturn(valDeser);

        JsonDeserializer<Object> result = deserValid._findDefaultImplDeserializer(ctxt);
        Assert.assertSame(valDeser, result);
        Assert.assertSame(valDeser, deserValid._findDefaultImplDeserializer(ctxt));
    }

    @Test
    public void testFindDeserializerResolved() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Number.class);
        JavaType resolvedType = TypeFactory.defaultInstance().constructType(Integer.class);
        TypeIdResolver resolver = Mockito.mock(TypeIdResolver.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(ctxt.getTypeFactory()).thenReturn(TypeFactory.defaultInstance());

        Mockito.when(resolver.typeFromId(ctxt, "int")).thenReturn(resolvedType);

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> intDeser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.any(JavaType.class), Mockito.isNull(BeanProperty.class)))
                .thenReturn(intDeser);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);
        JsonDeserializer<Object> res = deser._findDeserializer(ctxt, "int");
        Assert.assertSame(intDeser, res);

        JsonDeserializer<Object> cachedRes = deser._findDeserializer(ctxt, "int");
        Assert.assertSame(intDeser, cachedRes);
    }

    @Test
    public void testFindDeserializerResolvedDifferentBaseClass() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(List.class);
        JavaType resolvedType = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver resolver = Mockito.mock(TypeIdResolver.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(resolver.typeFromId(ctxt, "str")).thenReturn(resolvedType);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> strDeser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(ctxt.findContextualValueDeserializer(resolvedType, null)).thenReturn(strDeser);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);
        JsonDeserializer<Object> res = deser._findDeserializer(ctxt, "str");
        Assert.assertSame(strDeser, res);
    }

    @Test
    public void testFindDeserializerFallbackToDefaultImpl() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver resolver = Mockito.mock(TypeIdResolver.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(resolver.typeFromId(ctxt, "unknown")).thenReturn(null);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> defDeser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.eq(defaultImpl), Mockito.isNull(BeanProperty.class)))
                .thenReturn(defDeser);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, defaultImpl);
        JsonDeserializer<Object> res = deser._findDeserializer(ctxt, "unknown");
        Assert.assertSame(defDeser, res);
    }

    @Test
    public void testFindDeserializerFallbackToHandleUnknownTypeId() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver resolver = Mockito.mock(TypeIdResolver.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        Mockito.when(resolver.typeFromId(ctxt, "customUnknown")).thenReturn(null);

        JavaType handledType = TypeFactory.defaultInstance().constructType(Long.class);
        Mockito.when(ctxt.handleUnknownTypeId(Mockito.eq(baseType), Mockito.eq("customUnknown"), Mockito.eq(resolver), Mockito.isNull(String.class)))
                .thenReturn(handledType);

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> longDeser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.eq(handledType), Mockito.isNull(BeanProperty.class)))
                .thenReturn(longDeser);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);
        JsonDeserializer<Object> res = deser._findDeserializer(ctxt, "customUnknown");
        Assert.assertSame(longDeser, res);
    }

    @Test
    public void testFindDeserializerFallbackToHandleUnknownTypeIdReturnsNull() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver resolver = Mockito.mock(TypeIdResolver.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        Mockito.when(resolver.typeFromId(ctxt, "customUnknown")).thenReturn(null);
        Mockito.when(ctxt.handleUnknownTypeId(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(null);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);
        JsonDeserializer<Object> res = deser._findDeserializer(ctxt, "customUnknown");
        Assert.assertNull(res);
    }

    @Test
    public void testHandleUnknownTypeIdWithTypeIdResolverBase() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolverBase resolver = Mockito.mock(TypeIdResolverBase.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(resolver.getDescForKnownTypeIds()).thenReturn(null);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, null);
        deser._handleUnknownTypeId(ctxt, "unknown", resolver, baseType);
        Mockito.verify(ctxt).handleUnknownTypeId(baseType, "unknown", resolver, "known type ids are not statically known");

        Mockito.when(resolver.getDescForKnownTypeIds()).thenReturn("typeA, typeB");
        deser._handleUnknownTypeId(ctxt, "unknown2", resolver, baseType);
        Mockito.verify(ctxt).handleUnknownTypeId(baseType, "unknown2", resolver, "known type ids = typeA, typeB");
    }

    @Test
    public void testDeserializeWithNativeTypeId() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        TypeIdResolver resolver = Mockito.mock(TypeIdResolver.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> stringDeser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(stringDeser.deserialize(p, ctxt)).thenReturn("deserializedString");
        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.eq(defaultImpl), Mockito.isNull(BeanProperty.class)))
                .thenReturn(stringDeser);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, resolver, "type", false, defaultImpl);
        Object res1 = deser._deserializeWithNativeTypeId(p, ctxt, null);
        Assert.assertEquals("deserializedString", res1);

        Mockito.when(p.getTypeId()).thenReturn(null);
        Object res2 = deser._deserializeWithNativeTypeId(p, ctxt);
        Assert.assertEquals("deserializedString", res2);

        DummyTypeDeserializer deserNoDefault = new DummyTypeDeserializer(baseType, resolver, "type", false, null);
        Mockito.when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        Object resNull = deserNoDefault._deserializeWithNativeTypeId(p, ctxt, null);
        Assert.assertNull(resNull);
        Mockito.verify(ctxt).reportMappingException(Mockito.anyString());

        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);
        Mockito.when(resolver.typeFromId(ctxt, "123")).thenReturn(intType);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> intDeser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(intDeser.deserialize(p, ctxt)).thenReturn(123);
        Mockito.when(ctxt.findContextualValueDeserializer(Mockito.eq(intType), Mockito.isNull(BeanProperty.class)))
                .thenReturn(intDeser);

        Object res3 = deser._deserializeWithNativeTypeId(p, ctxt, 123);
        Assert.assertEquals(123, res3);
    }
}
