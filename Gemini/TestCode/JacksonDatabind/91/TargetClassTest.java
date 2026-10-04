package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeserializerCacheTest {

    private DeserializerCache cache;
    private DeserializationContext ctxt;
    private DeserializerFactory factory;
    private DeserializationConfig config;
    private AnnotationIntrospector introspector;
    private TypeFactory typeFactory;

    @Before
    public void setUp() {
        cache = new DeserializerCache();
        ctxt = mock(DeserializationContext.class);
        factory = mock(DeserializerFactory.class);
        config = mock(DeserializationConfig.class);
        introspector = mock(AnnotationIntrospector.class);
        typeFactory = TypeFactory.defaultInstance();

        when(ctxt.getConfig()).thenReturn(config);
        when(ctxt.getAnnotationIntrospector()).thenReturn(introspector);
        when(ctxt.getTypeFactory()).thenReturn(typeFactory);
    }

    @Test
    public void testCacheLifecycleAndSerialization() throws Exception {
        Assert.assertEquals(0, cache.cachedDeserializersCount());
        JavaType type = typeFactory.constructType(String.class);

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) mock(JsonDeserializer.class);
        when(deser.isCachable()).thenReturn(true);

        cache._cachedDeserializers.put(type, deser);
        Assert.assertEquals(1, cache.cachedDeserializersCount());

        cache.flushCachedDeserializers();
        Assert.assertEquals(0, cache.cachedDeserializersCount());

        cache._incompleteDeserializers.put(type, deser);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(cache);
        oos.flush();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        DeserializerCache deserialized = (DeserializerCache) ois.readObject();
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(0, deserialized._incompleteDeserializers.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializerNull() {
        cache._findCachedDeserializer(null);
    }

    @Test
    public void testFindValueDeserializerCached() throws Exception {
        JavaType type = typeFactory.constructType(String.class);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) mock(JsonDeserializer.class);

        cache._cachedDeserializers.put(type, deser);
        JsonDeserializer<Object> result = cache.findValueDeserializer(ctxt, factory, type);
        Assert.assertSame(deser, result);
    }

    @Test
    public void testFindValueDeserializerFactoryConstructed() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        BeanDescription beanDesc = mock(BeanDescription.class);
        AnnotatedClass ac = mock(AnnotatedClass.class);
        when(beanDesc.getClassInfo()).thenReturn(ac);
        when(config.introspect(type)).thenReturn(beanDesc);
        when(introspector.refineDeserializationType(any(), any(), eq(type))).thenReturn(type);

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) mock(JsonDeserializer.class);
        when(deser.isCachable()).thenReturn(true);
        when(factory.createBeanDeserializer(ctxt, type, beanDesc)).thenReturn(deser);

        JsonDeserializer<Object> result = cache.findValueDeserializer(ctxt, factory, type);
        Assert.assertSame(deser, result);
        Assert.assertEquals(1, cache.cachedDeserializersCount());
        Assert.assertTrue(cache.hasValueDeserializerFor(ctxt, factory, type));
    }

    @Test
    public void testFindValueDeserializerUnknown() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        BeanDescription beanDesc = mock(BeanDescription.class);
        AnnotatedClass ac = mock(AnnotatedClass.class);
        when(beanDesc.getClassInfo()).thenReturn(ac);
        when(config.introspect(type)).thenReturn(beanDesc);
        when(introspector.refineDeserializationType(any(), any(), eq(type))).thenReturn(type);
        when(factory.createBeanDeserializer(ctxt, type, beanDesc)).thenReturn(null);

        JsonDeserializer<Object> result = cache.findValueDeserializer(ctxt, factory, type);
        Assert.assertNull(result);
        Mockito.verify(ctxt).reportMappingException(eq("Can not find a Value deserializer for type %s"), eq(type));
    }

    @Test
    public void testFindValueDeserializerUnknownAbstract() throws Exception {
        JavaType type = typeFactory.constructType(AbstractBean.class);
        BeanDescription beanDesc = mock(BeanDescription.class);
        AnnotatedClass ac = mock(AnnotatedClass.class);
        when(beanDesc.getClassInfo()).thenReturn(ac);
        when(factory.mapAbstractType(config, type)).thenReturn(type);
        when(config.introspect(type)).thenReturn(beanDesc);
        when(introspector.refineDeserializationType(any(), any(), eq(type))).thenReturn(type);
        when(factory.createBeanDeserializer(ctxt, type, beanDesc)).thenReturn(null);

        JsonDeserializer<Object> result = cache.findValueDeserializer(ctxt, factory, type);
        Assert.assertNull(result);
        Mockito.verify(ctxt).reportMappingException(eq("Can not find a Value deserializer for abstract type %s"), eq(type));
    }

    @Test(expected = JsonMappingException.class)
    public void testCreateAndCache2ThrowsJsonMappingExceptionOnIllegalArgument() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        when(config.introspect(type)).thenThrow(new IllegalArgumentException("Invalid type info"));
        cache._createAndCache2(ctxt, factory, type);
    }

    @Test
    public void testFindResolvableDeserializer() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        BeanDescription beanDesc = mock(BeanDescription.class);
        AnnotatedClass ac = mock(AnnotatedClass.class);
        when(beanDesc.getClassInfo()).thenReturn(ac);
        when(config.introspect(type)).thenReturn(beanDesc);
        when(introspector.refineDeserializationType(any(), any(), eq(type))).thenReturn(type);

        ResolvableBeanDeserializer deser = mock(ResolvableBeanDeserializer.class);
        when(deser.isCachable()).thenReturn(true);
        when(factory.createBeanDeserializer(ctxt, type, beanDesc)).thenReturn(deser);

        JsonDeserializer<Object> result = cache.findValueDeserializer(ctxt, factory, type);
        Assert.assertSame(deser, result);
        Mockito.verify(deser).resolve(ctxt);
        Assert.assertEquals(0, cache._incompleteDeserializers.size());
    }

    @Test
    public void testFindKeyDeserializer() throws Exception {
        JavaType type = typeFactory.constructType(String.class);
        ResolvableKeyDeser kd = mock(ResolvableKeyDeser.class);
        when(factory.createKeyDeserializer(ctxt, type)).thenReturn(kd);

        KeyDeserializer result = cache.findKeyDeserializer(ctxt, factory, type);
        Assert.assertSame(kd, result);
        Mockito.verify(kd).resolve(ctxt);
    }

    @Test
    public void testFindKeyDeserializerUnknown() throws Exception {
        JavaType type = typeFactory.constructType(String.class);
        when(factory.createKeyDeserializer(ctxt, type)).thenReturn(null);

        KeyDeserializer result = cache.findKeyDeserializer(ctxt, factory, type);
        Assert.assertNull(result);
        Mockito.verify(ctxt).reportMappingException(eq("Can not find a (Map) Key deserializer for type %s"), eq(type));
    }

    @Test
    public void testCreateDeserializerFromAnnotation() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        BeanDescription beanDesc = mock(BeanDescription.class);
        AnnotatedClass ac = mock(AnnotatedClass.class);
        when(beanDesc.getClassInfo()).thenReturn(ac);
        when(config.introspect(type)).thenReturn(beanDesc);

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) mock(JsonDeserializer.class);
        when(introspector.findDeserializer(ac)).thenReturn(SimpleBeanDeserializer.class);
        when(ctxt.deserializerInstance(ac, SimpleBeanDeserializer.class)).thenReturn(deser);
        when(introspector.findDeserializationConverter(ac)).thenReturn(null);

        JsonDeserializer<Object> result = cache._createDeserializer(ctxt, factory, type);
        Assert.assertSame(deser, result);
    }

    @Test
    public void testCreateDeserializerWithConverter() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        BeanDescription beanDesc = mock(BeanDescription.class);
        AnnotatedClass ac = mock(AnnotatedClass.class);
        when(beanDesc.getClassInfo()).thenReturn(ac);
        when(config.introspect(type)).thenReturn(beanDesc);
        when(introspector.refineDeserializationType(any(), any(), eq(type))).thenReturn(type);

        @SuppressWarnings("unchecked")
        Converter<Object, Object> conv = (Converter<Object, Object>) mock(Converter.class);
        JavaType delegateType = typeFactory.constructType(String.class);
        when(conv.getInputType(typeFactory)).thenReturn(delegateType);
        when(beanDesc.findDeserializationConverter()).thenReturn(conv);

        BeanDescription delegateBeanDesc = mock(BeanDescription.class);
        when(config.introspect(delegateType)).thenReturn(delegateBeanDesc);

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> delegateDeser = (JsonDeserializer<Object>) mock(JsonDeserializer.class);
        when(factory.createBeanDeserializer(ctxt, delegateType, delegateBeanDesc)).thenReturn(delegateDeser);

        JsonDeserializer<Object> result = cache._createDeserializer(ctxt, factory, type);
        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateDeserializerWithBuilder() throws Exception {
        JavaType type = typeFactory.constructType(SimpleBean.class);
        BeanDescription beanDesc = mock(BeanDescription.class);
        AnnotatedClass ac = mock(AnnotatedClass.class);
        when(beanDesc.getClassInfo()).thenReturn(ac);
        when(config.introspect(type)).thenReturn(beanDesc);
        when(introspector.refineDeserializationType(any(), any(), eq(type))).thenReturn(type);
        when(beanDesc.findPOJOBuilder()).thenReturn((Class) SimpleBuilder.class);

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) mock(JsonDeserializer.class);
        when(factory.createBuilderBasedDeserializer(ctxt, type, beanDesc, SimpleBuilder.class)).thenReturn(deser);

        JsonDeserializer<Object> result = cache._createDeserializer(ctxt, factory, type);
        Assert.assertSame(deser, result);
    }

    @Test
    public void testCreateDeserializer2Variations() throws Exception {
        BeanDescription beanDesc = mock(BeanDescription.class);

        JavaType enumType = typeFactory.constructType(SampleEnum.class);
        cache._createDeserializer2(ctxt, factory, enumType, beanDesc);
        Mockito.verify(factory).createEnumDeserializer(ctxt, enumType, beanDesc);

        ArrayType arrayType = typeFactory.constructArrayType(String.class);
        cache._createDeserializer2(ctxt, factory, arrayType, beanDesc);
        Mockito.verify(factory).createArrayDeserializer(ctxt, arrayType, beanDesc);

        MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, String.class);
        cache._createDeserializer2(ctxt, factory, mapType, beanDesc);
        Mockito.verify(factory).createMapDeserializer(ctxt, mapType, beanDesc);

        MapLikeType mapLikeType = typeFactory.constructMapLikeType(CustomMapLike.class, String.class, String.class);
        cache._createDeserializer2(ctxt, factory, mapLikeType, beanDesc);
        Mockito.verify(factory).createMapLikeDeserializer(ctxt, mapLikeType, beanDesc);

        CollectionType colType = typeFactory.constructCollectionType(ArrayList.class, String.class);
        cache._createDeserializer2(ctxt, factory, colType, beanDesc);
        Mockito.verify(factory).createCollectionDeserializer(ctxt, colType, beanDesc);

        when(beanDesc.findExpectedFormat(null)).thenReturn(new JsonFormat.Value().withShape(JsonFormat.Shape.OBJECT));
        cache._createDeserializer2(ctxt, factory, colType, beanDesc);
        Mockito.verify(factory).createBeanDeserializer(ctxt, colType, beanDesc);

        when(beanDesc.findExpectedFormat(null)).thenReturn(null);
        CollectionLikeType colLikeType = typeFactory.constructCollectionLikeType(CustomColLike.class, String.class);
        cache._createDeserializer2(ctxt, factory, colLikeType, beanDesc);
        Mockito.verify(factory).createCollectionLikeDeserializer(ctxt, colLikeType, beanDesc);

        ReferenceType refType = ReferenceType.upgradeFrom(typeFactory.constructType(AtomicReference.class), typeFactory.constructType(String.class));
        cache._createDeserializer2(ctxt, factory, refType, beanDesc);
        Mockito.verify(factory).createReferenceDeserializer(ctxt, refType, beanDesc);

        JavaType nodeType = typeFactory.constructType(JsonNode.class);
        cache._createDeserializer2(ctxt, factory, nodeType, beanDesc);
        Mockito.verify(factory).createTreeDeserializer(config, nodeType, beanDesc);
    }

    @Test
    public void testCustomHandlerBypassesCache() throws Exception {
        JavaType ct = typeFactory.constructType(String.class).withValueHandler("custom");
        CollectionType colType = typeFactory.constructCollectionType(ArrayList.class, ct);

        Assert.assertNull(cache._findCachedDeserializer(colType));

        BeanDescription beanDesc = mock(BeanDescription.class);
        AnnotatedClass ac = mock(AnnotatedClass.class);
        when(beanDesc.getClassInfo()).thenReturn(ac);
        when(factory.mapAbstractType(config, colType)).thenReturn(colType);
        when(config.introspect(colType)).thenReturn(beanDesc);
        when(introspector.refineDeserializationType(any(), any(), eq(colType))).thenReturn(colType);

        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> deser = (JsonDeserializer<Object>) mock(JsonDeserializer.class);
        when(deser.isCachable()).thenReturn(true);
        when(factory.createCollectionDeserializer(ctxt, colType, beanDesc)).thenReturn(deser);

        JsonDeserializer<Object> result = cache.findValueDeserializer(ctxt, factory, colType);
        Assert.assertSame(deser, result);
        Assert.assertEquals(0, cache.cachedDeserializersCount());
    }

    @Test
    public void testModifyTypeByAnnotationWithKeyAndContentHandlers() throws Exception {
        Annotated a = mock(Annotated.class);
        MapType mapType = typeFactory.constructMapType(HashMap.class, String.class, Integer.class);

        when(introspector.findKeyDeserializer(a)).thenReturn(KeyDeserializer.class);
        KeyDeserializer kd = mock(KeyDeserializer.class);
        when(ctxt.keyDeserializerInstance(a, KeyDeserializer.class)).thenReturn(kd);

        when(introspector.findContentDeserializer(a)).thenReturn(SimpleBeanDeserializer.class);
        @SuppressWarnings("unchecked")
        JsonDeserializer<Object> cd = (JsonDeserializer<Object>) mock(JsonDeserializer.class);
        when(ctxt.deserializerInstance(a, SimpleBeanDeserializer.class)).thenReturn(cd);

        when(introspector.refineDeserializationType(config, a, mapType)).thenAnswer(inv -> inv.getArgument(2));

        Method method = DeserializerCache.class.getDeclaredMethod("modifyTypeByAnnotation", DeserializationContext.class, Annotated.class, JavaType.class);
        method.setAccessible(true);
        JavaType modified = (JavaType) method.invoke(cache, ctxt, a, mapType);

        Assert.assertNotNull(modified.getKeyType().getValueHandler());
        Assert.assertNotNull(modified.getContentType().getValueHandler());
    }

    @Test
    public void testVerifyAsClass() throws Exception {
        Method method = DeserializerCache.class.getDeclaredMethod("_verifyAsClass", Object.class, String.class, Class.class);
        method.setAccessible(true);

        Assert.assertNull(method.invoke(cache, null, "test", JsonDeserializer.None.class));
        Assert.assertNull(method.invoke(cache, JsonDeserializer.None.class, "test", JsonDeserializer.None.class));
        Assert.assertNull(method.invoke(cache, com.fasterxml.jackson.databind.annotation.NoClass.class, "test", JsonDeserializer.None.class));
        Assert.assertEquals(String.class, method.invoke(cache, String.class, "test", JsonDeserializer.None.class));

        try {
            method.invoke(cache, "NotAClass", "test", JsonDeserializer.None.class);
            Assert.fail("Should have failed on invalid class object");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

    static class SimpleBean {}
    abstract static class AbstractBean {}
    static class SimpleBuilder {}
    enum SampleEnum { A, B }
    interface CustomMapLike extends Map<String, String> {}
    interface CustomColLike extends List<String> {}
    static abstract class SimpleBeanDeserializer extends JsonDeserializer<Object> {}
    static abstract class ResolvableBeanDeserializer extends JsonDeserializer<Object> implements ResolvableDeserializer {}
    static abstract class ResolvableKeyDeser extends KeyDeserializer implements ResolvableDeserializer {}
}
