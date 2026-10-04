package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CreatorPropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomAnnotation {
        String value() default "test";
    }

    private JavaType javaType;
    private PropertyName propName;
    private PropertyMetadata metadata;
    private AnnotatedParameter annotatedParameter;

    @Before
    public void setUp() {
        javaType = TypeFactory.defaultInstance().constructType(String.class);
        propName = new PropertyName("testProp");
        metadata = PropertyMetadata.STD_REQUIRED;

        AnnotationMap map = new AnnotationMap();
        CustomAnnotation ann = new CustomAnnotation() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return CustomAnnotation.class;
            }
            @Override
            public String value() {
                return "annotatedValue";
            }
        };
        map.add(ann);

        TypeResolutionContext typeRes = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), TypeFactory.defaultInstance().constructType(String.class).getBindings());
        annotatedParameter = new AnnotatedParameter((AnnotatedWithParams) null, javaType, typeRes, map, 0);
    }

    @Test
    public void testConstructorAndGetters() {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, new PropertyName("wrapper"),
                null, null, annotatedParameter, 2, "injectId", metadata);

        Assert.assertEquals("testProp", prop.getName());
        Assert.assertEquals(javaType, prop.getType());
        Assert.assertEquals(new PropertyName("wrapper"), prop.getWrapperName());
        Assert.assertEquals(2, prop.getCreatorIndex());
        Assert.assertEquals("injectId", prop.getInjectableValueId());
        Assert.assertEquals(annotatedParameter, prop.getMember());
        Assert.assertFalse(prop.isIgnorable());
        Assert.assertNotNull(prop.getAnnotation(CustomAnnotation.class));
        Assert.assertEquals("[creator property, name 'testProp'; inject id 'injectId']", prop.toString());
    }

    @Test
    public void testGetAnnotationWithNullAnnotated() {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        Assert.assertNull(prop.getAnnotation(CustomAnnotation.class));
        Assert.assertNull(prop.getMember());
    }

    @Test
    public void testMarkAsIgnorable() {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        Assert.assertFalse(prop.isIgnorable());
        prop.markAsIgnorable();
        Assert.assertTrue(prop.isIgnorable());
    }

    @Test
    public void testWithName() {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, annotatedParameter, 1, "id1", metadata);
        prop.markAsIgnorable();

        SettableBeanProperty renamed = prop.withName(new PropertyName("renamed"));
        Assert.assertEquals("renamed", renamed.getName());
        Assert.assertEquals(1, renamed.getCreatorIndex());
        Assert.assertEquals("id1", renamed.getInjectableValueId());
        Assert.assertTrue(renamed.isIgnorable());
    }

    @Test
    public void testWithValueDeserializer() {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        JsonDeserializer<Object> deser1 = Mockito.mock(JsonDeserializer.class);
        JsonDeserializer<Object> deser2 = Mockito.mock(JsonDeserializer.class);

        SettableBeanProperty propWithDeser = prop.withValueDeserializer(deser1);
        Assert.assertNotSame(prop, propWithDeser);
        Assert.assertSame(deser1, propWithDeser.getValueDeserializer());

        SettableBeanProperty sameDeser = propWithDeser.withValueDeserializer(deser1);
        Assert.assertSame(propWithDeser, sameDeser);

        SettableBeanProperty differentDeser = propWithDeser.withValueDeserializer(deser2);
        Assert.assertNotSame(propWithDeser, differentDeser);
        Assert.assertSame(deser2, differentDeser.getValueDeserializer());
    }

    @Test
    public void testWithNullProvider() {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        NullValueProvider nva = Mockito.mock(NullValueProvider.class);

        SettableBeanProperty propWithNva = prop.withNullProvider(nva);
        Assert.assertNotSame(prop, propWithNva);
        Assert.assertSame(nva, propWithNva.getNullValueProvider());
    }

    @Test
    public void testFixAccess() {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        DeserializationConfig config = Mockito.mock(DeserializationConfig.class);

        prop.fixAccess(config);

        SettableBeanProperty fallback = Mockito.mock(SettableBeanProperty.class);
        prop.setFallbackSetter(fallback);
        prop.fixAccess(config);

        Mockito.verify(fallback, Mockito.times(1)).fixAccess(config);
    }

    @Test
    public void testFindInjectableValueSuccess() throws JsonMappingException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, "inject123", metadata);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Object targetBean = new Object();
        Object expectedInjected = "resolvedValue";

        Mockito.when(ctxt.findInjectableValue("inject123", prop, targetBean)).thenReturn(expectedInjected);

        Object actual = prop.findInjectableValue(ctxt, targetBean);
        Assert.assertEquals(expectedInjected, actual);
    }

    @Test
    public void testFindInjectableValueMissingId() throws JsonMappingException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Object targetBean = new Object();

        prop.findInjectableValue(ctxt, targetBean);

        Mockito.verify(ctxt).reportBadDefinition(Mockito.eq(Object.class), Mockito.contains("has no injectable value id configured"));
    }

    @Test
    public void testInject() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, "inject123", metadata);
        SettableBeanProperty fallback = Mockito.mock(SettableBeanProperty.class);
        prop.setFallbackSetter(fallback);

        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Object targetBean = new Object();
        Mockito.when(ctxt.findInjectableValue("inject123", prop, targetBean)).thenReturn("injectedVal");

        prop.inject(ctxt, targetBean);

        Mockito.verify(fallback).set(targetBean, "injectedVal");
    }

    @Test(expected = InvalidDefinitionException.class)
    public void testSetWithoutFallbackThrowsException() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        prop.set(new Object(), "val");
    }

    @Test(expected = InvalidDefinitionException.class)
    public void testSetAndReturnWithoutFallbackThrowsException() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        prop.setAndReturn(new Object(), "val");
    }

    @Test(expected = InvalidDefinitionException.class)
    public void testDeserializeAndSetWithoutFallbackThrowsException() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        prop.deserializeAndSet(Mockito.mock(JsonParser.class), Mockito.mock(DeserializationContext.class), new Object());
    }

    @Test(expected = InvalidDefinitionException.class)
    public void testDeserializeSetAndReturnWithoutFallbackThrowsException() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        prop.deserializeSetAndReturn(Mockito.mock(JsonParser.class), Mockito.mock(DeserializationContext.class), new Object());
    }

    @Test
    public void testSetWithFallback() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        SettableBeanProperty fallback = Mockito.mock(SettableBeanProperty.class);
        prop.setFallbackSetter(fallback);

        Object bean = new Object();
        prop.set(bean, "value");
        Mockito.verify(fallback).set(bean, "value");
    }

    @Test
    public void testSetAndReturnWithFallback() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        SettableBeanProperty fallback = Mockito.mock(SettableBeanProperty.class);
        prop.setFallbackSetter(fallback);

        Object bean = new Object();
        Object expectedRet = new Object();
        Mockito.when(fallback.setAndReturn(bean, "value")).thenReturn(expectedRet);

        Object ret = prop.setAndReturn(bean, "value");
        Assert.assertSame(expectedRet, ret);
    }

    @Test
    public void testDeserializeAndSetWithFallback() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        SettableBeanProperty fallback = Mockito.mock(SettableBeanProperty.class);
        prop.setFallbackSetter(fallback);

        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        prop = (CreatorProperty) prop.withValueDeserializer(deser);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(deser.deserialize(parser, ctxt)).thenReturn("deserValue");

        Object bean = new Object();
        prop.deserializeAndSet(parser, ctxt, bean);

        Mockito.verify(fallback).set(bean, "deserValue");
    }

    @Test
    public void testDeserializeSetAndReturnWithFallback() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        SettableBeanProperty fallback = Mockito.mock(SettableBeanProperty.class);
        prop.setFallbackSetter(fallback);

        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        prop = (CreatorProperty) prop.withValueDeserializer(deser);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(deser.deserialize(parser, ctxt)).thenReturn("deserValue");

        Object bean = new Object();
        Object expectedRet = new Object();
        Mockito.when(fallback.setAndReturn(bean, "deserValue")).thenReturn(expectedRet);

        Object ret = prop.deserializeSetAndReturn(parser, ctxt, bean);
        Assert.assertSame(expectedRet, ret);
    }

    @Test
    public void testReportMissingSetterWithContext() throws IOException {
        CreatorProperty prop = new CreatorProperty(
                propName, javaType, null, null, null, null, 0, null, metadata);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);

        try {
            java.lang.reflect.Method method = CreatorProperty.class.getDeclaredMethod("_reportMissingSetter", JsonParser.class, DeserializationContext.class);
            method.setAccessible(true);
            method.invoke(prop, p, ctxt);
        } catch (Exception e) {
            Assert.fail("Reflection call failed: " + e.getMessage());
        }

        Mockito.verify(ctxt).reportBadDefinition(Mockito.eq(javaType), Mockito.contains("No fallback setter/field defined for creator property 'testProp'"));
    }
}
