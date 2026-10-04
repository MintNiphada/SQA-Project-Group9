package com.fasterxml.jackson.databind.deser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospector;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.Converter;

public class DeserializerCacheTest {

    private DeserializerCache cache;
    private DeserializationContext ctxt;
    private DeserializerFactory factory;
    private JavaType type;
    private DeserializationConfig config;
    private AnnotationIntrospector introspector;
    private BeanDescription beanDesc;

    @Before
    public void setUp() {
        cache = new DeserializerCache();
        ctxt = mock(DeserializationContext.class);
        factory = mock(DeserializerFactory.class);
        type = mock(JavaType.class);
        config = mock(DeserializationConfig.class);
        introspector = mock(AnnotationIntrospector.class);
        beanDesc = mock(BeanDescription.class);

        when(ctxt.getConfig()).thenReturn(config);
        when(ctxt.getAnnotationIntrospector()).thenReturn(introspector);
        when(config.introspect(any(JavaType.class))).thenReturn(beanDesc);
        when(type.getRawClass()).thenReturn(String.class);
        when(type.isAbstract()).thenReturn(false);
        when(type.isMapLikeType()).thenReturn(false);
        when(type.isCollectionLikeType()).thenReturn(false);
        when(type.isContainerType()).thenReturn(false);
        when(type.isEnumType()).thenReturn(false);
        when(beanDesc.getClassInfo()).thenReturn(mock(Annotated.class));
    }

    @Test
    public void testCachedDeserializersCountInitiallyZero() {
        assertEquals(0, cache.cachedDeserializersCount());
    }

    @Test
    public void testFlushCachedDeserializersClearsCache() {
        ConcurrentHashMap<JavaType, JsonDeserializer<Object>> cached = getCachedDeserializers(cache);
        cached.put(type, mock(JsonDeserializer.class));
        assertEquals(1, cache.cachedDeserializersCount());
        cache.flushCachedDeserializers();
        assertEquals(0, cache.cachedDeserializersCount());
    }

    @Test
    public void testFindValueDeserializerReturnsCached() throws JsonMappingException {
        JsonDeserializer<Object> deser = mock(JsonDeserializer.class);
        getCachedDeserializers(cache).put(type, deser);
        JsonDeserializer<Object> result = cache.findValueDeserializer(ctxt, factory, type);
        assertSame(deser, result);
    }

    @Test
    public void testFindValueDeserializerCreatesAndCaches() throws JsonMappingException {
        JsonDeserializer<Object> deser = mock(JsonDeserializer.class);
        when(deser.isCachable()).thenReturn(true);
        when(factory.createBeanDeserializer(any(DeserializationContext.class), any(JavaType.class), any(BeanDescription.class))).thenReturn(deser);
        JsonDeserializer<Object> result = cache.findValueDeserializer(ctxt, factory, type);
        assertSame(deser, result);
        assertTrue(getCachedDeserializers(cache).containsKey(type));
    }

    @Test
    public void testFindValueDeserializerReturnsNullAndHandlesUnknown() throws JsonMappingException {
        when(factory.createBeanDeserializer(any(DeserializationContext.class), any(JavaType.class), any(BeanDescription.class))).thenReturn(null);
        try {
            cache.findValueDeserializer(ctxt, factory, type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a Value deserializer"));
        }
    }

    @Test
    public void testFindKeyDeserializerReturnsCreated() throws JsonMappingException {
        KeyDeserializer kd = mock(KeyDeserializer.class);
        when(factory.createKeyDeserializer(ctxt, type)).thenReturn(kd);
        KeyDeserializer result = cache.findKeyDeserializer(ctxt, factory, type);
        assertSame(kd, result);
    }

    @Test
    public void testFindKeyDeserializerResolvesIfResolvable() throws JsonMappingException {
        KeyDeserializer kd = mock(KeyDeserializer.class, withSettings().extraInterfaces(ResolvableDeserializer.class));
        when(factory.createKeyDeserializer(ctxt, type)).thenReturn(kd);
        KeyDeserializer result = cache.findKeyDeserializer(ctxt, factory, type);
        assertSame(kd, result);
        verify((ResolvableDeserializer) kd).resolve(ctxt);
    }

    @Test
    public void testFindKeyDeserializerReturnsUnknownHandlerWhenNull() throws JsonMappingException {
        when(factory.createKeyDeserializer(ctxt, type)).thenReturn(null);
        try {
            cache.findKeyDeserializer(ctxt, factory, type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a (Map) Key deserializer"));
        }
    }

    @Test
    public void testHasValueDeserializerForReturnsTrueWhenCached() throws JsonMappingException {
        JsonDeserializer<Object> deser = mock(JsonDeserializer.class);
        getCachedDeserializers(cache).put(type, deser);
        assertTrue(cache.hasValueDeserializerFor(ctxt, factory, type));
    }

    @Test
    public void testHasValueDeserializerForReturnsFalseWhenNull() throws JsonMappingException {
        when(factory.createBeanDeserializer(any(DeserializationContext.class), any(JavaType.class), any(BeanDescription.class))).thenReturn(null);
        assertFalse(cache.hasValueDeserializerFor(ctxt, factory, type));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindCachedDeserializerThrowsOnNullType() {
        cache._findCachedDeserializer(null);
    }

    @Test
    public void testFindCachedDeserializerReturnsNullForCustomValueHandler() {
        JavaType containerType = mock(JavaType.class);
        when(containerType.isContainerType()).thenReturn(true);
        JavaType contentType = mock(JavaType.class);
        when(containerType.getContentType()).thenReturn(contentType);
        when(contentType.getValueHandler()).thenReturn(mock(Object.class));
        assertNull(cache._findCachedDeserializer(containerType));
    }

    @Test
    public void testCreateAndCacheValueDeserializerUsesIncompleteDeserializers() throws JsonMappingException {
        JsonDeserializer<Object> deser = mock(JsonDeserializer.class);
        when(deser.isCachable()).thenReturn(true);
        when(factory.createBeanDeserializer(any(DeserializationContext.class), any(JavaType.class), any(BeanDescription.class))).thenReturn(deser);
        JsonDeserializer<Object> result = cache._createAndCacheValueDeserializer(ctxt, factory, type);
        assertSame(deser, result);
        assertTrue(getCachedDeserializers(cache).containsKey(type));
    }

    @Test
    public void testCreateAndCacheValueDeserializerReturnsFromIncomplete() throws JsonMappingException {
        HashMap<JavaType, JsonDeserializer<Object>> incomplete = getIncompleteDeserializers(cache);
        JsonDeserializer<Object> deser = mock(JsonDeserializer.class);
        incomplete.put(type, deser);
        JsonDeserializer<Object> result = cache._createAndCacheValueDeserializer(ctxt, factory, type);
        assertSame(deser, result);
    }

    @Test
    public void testCreateAndCache2WrapsIllegalArgumentException() throws Exception {
        when(factory.createBeanDeserializer(any(DeserializationContext.class), any(JavaType.class), any(BeanDescription.class))).thenThrow(new IllegalArgumentException("test"));
        try {
            cache._createAndCache2(ctxt, factory, type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("test"));
        }
    }

    @Test
    public void testCreateAndCache2ReturnsNullWhenDeserIsNull() throws JsonMappingException {
        when(factory.createBeanDeserializer(any(DeserializationContext.class), any(JavaType.class), any(BeanDescription.class))).thenReturn(null);
        assertNull(cache._createAndCache2(ctxt, factory, type));
    }

    @Test
    public void testCreateAndCache2ResolvesAndCaches() throws JsonMappingException {
        JsonDeserializer<Object> deser = mock(JsonDeserializer.class, withSettings().extraInterfaces(ResolvableDeserializer.class));
        when(deser.isCachable()).thenReturn(true);
        when(factory.createBeanDeserializer(any(DeserializationContext.class), any(JavaType.class), any(BeanDescription.class))).thenReturn(deser);
        JsonDeserializer<Object> result = cache._createAndCache2(ctxt, factory, type);
        assertSame(deser, result);
        verify((ResolvableDeserializer) deser).resolve(ctxt);
        assertTrue(getCachedDeserializers(cache).containsKey(type));
    }

    @Test
    public void testCreateDeserializerAbstractTypeMapping() throws JsonMappingException {
        when(type.isAbstract()).thenReturn(true);
        JavaType mappedType = mock(JavaType.class);
        when(factory.mapAbstractType(config, type)).thenReturn(mappedType);
        when(config.introspect(mappedType)).thenReturn(beanDesc);
        when(mappedType.getRawClass()).thenReturn(String.class);
        when(mappedType.isEnumType()).thenReturn(false);
        when(mappedType.isContainerType()).thenReturn(false);
        JsonDeserializer<Object> deser = mock(JsonDeserializer.class);
        when(factory.createBeanDeserializer(any(DeserializationContext.class), any(JavaType.class), any(BeanDescription.class))).thenReturn(deser);
        JsonDeserializer<Object> result = cache._createDeserializer(ctxt, factory, type);
        assertSame(deser, result);
    }

    @Test
    public void testCreateDeserializerFromAnnotation() throws JsonMappingException {
        JsonDeserializer<Object> deser = mock(JsonDeserializer.class);
        when(introspector.findDeserializer(any(Annotated.class))).thenReturn(deser);
        when(ctxt.deserializerInstance(any(Annotated.class), any())).thenReturn(deser);
        JsonDeserializer<Object> result = cache._createDeserializer(ctxt, factory, type);
        assertSame(deser, result);
    }

    @Test
    public void testCreateDeserializerWithConverter() throws JsonMappingException {
        Converter<Object, Object> conv = mock(Converter.class);
        when(beanDesc.findDeserializationConverter()).thenReturn(conv);
        JavaType delegateType = mock(JavaType.class);
        when(conv.getInputType(any(TypeFactory.class))).thenReturn(delegateType);
        when(delegateType.hasRawClass(any(Class.class))).thenReturn(true);
        when(delegateType.getRawClass()).thenReturn(String.class);
        when(delegateType.isEnumType()).thenReturn(false);
        when(delegateType.isContainerType()).thenReturn(false);
        JsonDeserializer<Object> innerDeser = mock(JsonDeserializer.class);
        when(factory.createBeanDeserializer(any(DeserializationContext.class), any(JavaType.class), any(BeanDescription.class))).thenReturn(innerDeser);
        JsonDeserializer<Object> result = cache._createDeserializer(ctxt, factory, type);
        assertTrue(result instanceof StdDelegatingDeserializer);
    }

    @Test
    public void testCreateDeserializer2EnumType() throws JsonMappingException {
        when(type.isEnumType()).thenReturn(true);
        JsonDeserializer<?> deser = mock(JsonDeserializer.class);
        when(factory.createEnumDeserializer(ctxt, type, beanDesc)).thenReturn(deser);
        JsonDeserializer<?> result = cache._createDeserializer2(ctxt, factory, type, beanDesc);
        assertSame(deser, result);
    }

    @Test
    public void testCreateDeserializer2ArrayType() throws JsonMappingException {
        when(type.isContainerType()).thenReturn(true);
        when(type.isArrayType()).thenReturn(true);
        ArrayType arrayType = mock(ArrayType.class);
        JsonDeserializer<?> deser = mock(JsonDeserializer.class);
        when(factory.createArrayDeserializer(ctxt, arrayType, beanDesc)).thenReturn(deser);
        JsonDeserializer<?> result = cache._createDeserializer2(ctxt, factory, arrayType, beanDesc);
        assertSame(deser, result);
    }

    @Test
    public void testCreateDeserializer2MapType() throws JsonMappingException {
        when(type.isContainerType()).thenReturn(true);
        when(type.isMapLikeType()).thenReturn(true);
        MapType mapType = mock(MapType.class);
        when(mapType.isTrueMapType()).thenReturn(true);
        JsonDeserializer<?> deser = mock(JsonDeserializer.class);
        when(factory.createMapDeserializer(ctxt, mapType, beanDesc)).thenReturn(deser);
        JsonDeserializer<?> result = cache._createDeserializer2(ctxt, factory, mapType, beanDesc);
        assertSame(deser, result);
    }

    @Test
    public void testCreateDeserializer2CollectionType() throws JsonMappingException {
        when(type.isContainerType()).thenReturn(true);
        when(type.isCollectionLikeType()).thenReturn(true);
        CollectionType colType = mock(CollectionType.class);
        when(colType.isTrueCollectionType()).thenReturn(true);
        JsonDeserializer<?> deser = mock(JsonDeserializer.class);
        when(factory.createCollectionDeserializer(ctxt, colType, beanDesc)).thenReturn(deser);
        JsonDeserializer<?> result = cache._createDeserializer2(ctxt, factory, colType, beanDesc);
        assertSame(deser, result);
    }

    @Test
    public void testCreateDeserializer2TreeType() throws JsonMappingException {
        when(type.getRawClass()).thenReturn(com.fasterxml.jackson.databind.JsonNode.class);
        JsonDeserializer<?> deser = mock(JsonDeserializer.class);
        when(factory.createTreeDeserializer(config, type, beanDesc)).thenReturn(deser);
        JsonDeserializer<?> result = cache._createDeserializer2(ctxt, factory, type, beanDesc);
        assertSame(deser, result);
    }

    @Test
    public void testFindDeserializerFromAnnotationReturnsNullWhenNoAnnotation() throws JsonMappingException {
        when(introspector.findDeserializer(any(Annotated.class))).thenReturn(null);
        assertNull(cache.findDeserializerFromAnnotation(ctxt, beanDesc.getClassInfo()));
    }

    @Test
    public void testFindConvertingDeserializerReturnsOriginalWhenNoConverter() throws JsonMappingException {
        JsonDeserializer<Object> deser = mock(JsonDeserializer.class);
        when(introspector.findDeserializationConverter(any(Annotated.class))).thenReturn(null);
        JsonDeserializer<Object> result = cache.findConvertingDeserializer(ctxt, beanDesc.getClassInfo(), deser);
        assertSame(deser, result);
    }

    @Test
    public void testModifyTypeByAnnotationNarrowBySubclass() throws JsonMappingException {
        when(introspector.findDeserializationType(any(Annotated.class), any(JavaType.class))).thenReturn(Integer.class);
        when(type.narrowBy(Integer.class)).thenReturn(type);
        JavaType result = cache.modifyTypeByAnnotation(ctxt, beanDesc.getClassInfo(), type);
        assertSame(type, result);
    }

    @Test
    public void testModifyTypeByAnnotationKeyType() throws JsonMappingException {
        when(type.isContainerType()).thenReturn(true);
        when(type.getKeyType()).thenReturn(type);
        when(introspector.findDeserializationKeyType(any(Annotated.class), any(JavaType.class))).thenReturn(String.class);
        when(type instanceof MapLikeType).thenReturn(true);
        MapLikeType mapLikeType = mock(MapLikeType.class);
        when(mapLikeType.narrowKey(String.class)).thenReturn(mapLikeType);
        when(mapLikeType.getKeyType()).thenReturn(type);
        when(mapLikeType.getContentType()).thenReturn(type);
        when(mapLikeType.isContainerType()).thenReturn(true);
        when(introspector.findDeserializationContentType(any(Annotated.class), any(JavaType.class))).thenReturn(null);
        when(introspector.findKeyDeserializer(any(Annotated.class))).thenReturn(null);
        when(introspector.findContentDeserializer(any(Annotated.class))).thenReturn(null);
        JavaType result = cache.modifyTypeByAnnotation(ctxt, beanDesc.getClassInfo(), mapLikeType);
        assertSame(mapLikeType, result);
    }

    @Test
    public void testHandleUnknownValueDeserializerAbstractType() throws JsonMappingException {
        when(type.getRawClass()).thenReturn(java.util.AbstractList.class);
        try {
            cache._handleUnknownValueDeserializer(type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("abstract type"));
        }
    }

    @Test
    public void testHandleUnknownValueDeserializerConcreteType() throws JsonMappingException {
        when(type.getRawClass()).thenReturn(String.class);
        try {
            cache._handleUnknownValueDeserializer(type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a Value deserializer"));
        }
    }

    @Test
    public void testHandleUnknownKeyDeserializer() throws JsonMappingException {
        try {
            cache._handleUnknownKeyDeserializer(type);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not find a (Map) Key deserializer"));
        }
    }

    @Test
    public void testWriteReplaceClearsIncompleteDeserializers() {
        HashMap<JavaType, JsonDeserializer<Object>> incomplete = getIncompleteDeserializers(cache);
        incomplete.put(type, mock(JsonDeserializer.class));
        assertFalse(incomplete.isEmpty());
        cache.writeReplace();
        assertTrue(incomplete.isEmpty());
    }

    private ConcurrentHashMap<JavaType, JsonDeserializer<Object>> getCachedDeserializers(DeserializerCache cache) {
        try {
            java.lang.reflect.Field field = DeserializerCache.class.getDeclaredField("_cachedDeserializers");
            field.setAccessible(true);
            return (ConcurrentHashMap<JavaType, JsonDeserializer<Object>>) field.get(cache);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<JavaType, JsonDeserializer<Object>> getIncompleteDeserializers(DeserializerCache cache) {
        try {
            java.lang.reflect.Field field = DeserializerCache.class.getDeclaredField("_incompleteDeserializers");
            field.setAccessible(true);
            return (HashMap<JavaType, JsonDeserializer<Object>>) field.get(cache);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
