package com.fasterxml.jackson.databind.deser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.ObjectIdGenerator.IdKey;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.util.ClassUtil;

@RunWith(MockitoJUnitRunner.class)
public class DefaultDeserializationContextTest {

    @Mock
    private DeserializerFactory factory;
    @Mock
    private DeserializerCache cache;
    @Mock
    private DeserializationConfig config;
    @Mock
    private JsonParser parser;
    @Mock
    private InjectableValues values;
    @Mock
    private Annotated annotated;
    @Mock
    private ObjectIdGenerator<?> idGen;
    @Mock
    private ObjectIdResolver resolverType;
    @Mock
    private IdKey idKey;
    @Mock
    private ReadableObjectId readableObjectId;
    @Mock
    private HandlerInstantiator handlerInstantiator;

    private DefaultDeserializationContext.Impl context;

    @Before
    public void setUp() throws Exception {
        context = new DefaultDeserializationContext.Impl(factory);
        // Set config for tests that need it
        setField(context, "_config", config);
    }

    // Helper to set private fields via reflection
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = DefaultDeserializationContext.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Object getField(Object target, String fieldName) throws Exception {
        Field field = DefaultDeserializationContext.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    // ==================== Impl constructors and factory methods ====================

    @Test
    public void testImplConstructorWithFactory() {
        DefaultDeserializationContext.Impl impl = new DefaultDeserializationContext.Impl(factory);
        assertNotNull(impl);
    }

    @Test
    public void testImplCopy() {
        DefaultDeserializationContext.Impl copy = context.copy();
        assertNotNull(copy);
        assertNotSame(context, copy);
        assertTrue(copy instanceof DefaultDeserializationContext.Impl);
    }

    @Test(expected = IllegalStateException.class)
    public void testImplCopyOnSubclass() {
        // Create anonymous subclass of Impl
        DefaultDeserializationContext.Impl subclass = new DefaultDeserializationContext.Impl(factory) {
            // anonymous subclass
        };
        subclass.copy(); // should call super.copy() which throws
    }

    @Test
    public void testImplCreateInstance() {
        DefaultDeserializationContext newCtx = context.createInstance(config, parser, values);
        assertNotNull(newCtx);
        assertTrue(newCtx instanceof DefaultDeserializationContext.Impl);
        assertNotSame(context, newCtx);
    }

    @Test
    public void testImplWith() {
        DefaultDeserializationContext newCtx = context.with(factory);
        assertNotNull(newCtx);
        assertTrue(newCtx instanceof DefaultDeserializationContext.Impl);
        assertNotSame(context, newCtx);
    }

    // ==================== findObjectId (3-arg) ====================

    @Test
    public void testFindObjectIdNullObjectIds() throws Exception {
        Object id = "testId";
        when(idGen.key(id)).thenReturn(idKey);
        when(resolverType.newForDeserialization(any(DeserializationContext.class))).thenReturn(resolverType);

        ReadableObjectId result = context.findObjectId(id, idGen, resolverType);

        assertNotNull(result);
        verify(idGen).key(id);
        verify(resolverType).newForDeserialization(context);
        // Verify that _objectIds map was created and contains the entry
        LinkedHashMap<IdKey, ReadableObjectId> objectIds = (LinkedHashMap<IdKey, ReadableObjectId>) getField(context, "_objectIds");
        assertNotNull(objectIds);
        assertTrue(objectIds.containsKey(idKey));
        assertSame(result, objectIds.get(idKey));
    }

    @Test
    public void testFindObjectIdExistingEntry() throws Exception {
        Object id = "existing";
        when(idGen.key(id)).thenReturn(idKey);
        // Pre-populate _objectIds
        LinkedHashMap<IdKey, ReadableObjectId> map = new LinkedHashMap<>();
        map.put(idKey, readableObjectId);
        setField(context, "_objectIds", map);

        ReadableObjectId result = context.findObjectId(id, idGen, resolverType);

        assertSame(readableObjectId, result);
        // Should not create new resolver
        verify(resolverType, never()).newForDeserialization(any());
    }

    @Test
    public void testFindObjectIdResolverCached() throws Exception {
        Object id = "cached";
        when(idGen.key(id)).thenReturn(idKey);
        when(resolverType.newForDeserialization(any())).thenReturn(resolverType);
        // Pre-populate _objectIdResolvers with a resolver that canUseFor returns true
        List<ObjectIdResolver> resolvers = new ArrayList<>();
        ObjectIdResolver cachedResolver = mock(ObjectIdResolver.class);
        when(cachedResolver.canUseFor(resolverType)).thenReturn(true);
        resolvers.add(cachedResolver);
        setField(context, "_objectIdResolvers", resolvers);

        ReadableObjectId result = context.findObjectId(id, idGen, resolverType);

        assertNotNull(result);
        // Should use cached resolver, not create new one
        verify(resolverType, never()).newForDeserialization(any());
        // The entry should have the cached resolver
        assertEquals(cachedResolver, result.getResolver());
    }

    @Test
    public void testFindObjectIdResolverNotCached() throws Exception {
        Object id = "notcached";
        when(idGen.key(id)).thenReturn(idKey);
        when(resolverType.newForDeserialization(any())).thenReturn(resolverType);
        // Pre-populate _objectIdResolvers with a resolver that cannot use
        List<ObjectIdResolver> resolvers = new ArrayList<>();
        ObjectIdResolver otherResolver = mock(ObjectIdResolver.class);
        when(otherResolver.canUseFor(resolverType)).thenReturn(false);
        resolvers.add(otherResolver);
        setField(context, "_objectIdResolvers", resolvers);

        ReadableObjectId result = context.findObjectId(id, idGen, resolverType);

        assertNotNull(result);
        verify(resolverType).newForDeserialization(context);
        // The new resolver should be added to list
        List<ObjectIdResolver> updatedResolvers = (List<ObjectIdResolver>) getField(context, "_objectIdResolvers");
        assertTrue(updatedResolvers.contains(resolverType));
    }

    @Test
    public void testFindObjectIdDeprecated() throws Exception {
        Object id = "deprecated";
        when(idGen.key(id)).thenReturn(idKey);
        // The deprecated method should delegate to the 3-arg version with SimpleObjectIdResolver
        // We can't easily verify the resolver type, but we can check that the map is populated.
        ReadableObjectId result = context.findObjectId(id, idGen);
        assertNotNull(result);
        LinkedHashMap<IdKey, ReadableObjectId> objectIds = (LinkedHashMap<IdKey, ReadableObjectId>) getField(context, "_objectIds");
        assertTrue(objectIds.containsKey(idKey));
    }

    // ==================== checkUnresolvedObjectId ====================

    @Test
    public void testCheckUnresolvedObjectIdNullObjectIds() throws Exception {
        setField(context, "_objectIds", null);
        // Should not throw
        context.checkUnresolvedObjectId();
    }

    @Test
    public void testCheckUnresolvedObjectIdFeatureDisabled() throws Exception {
        when(config.isEnabled(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS)).thenReturn(false);
        LinkedHashMap<IdKey, ReadableObjectId> map = new LinkedHashMap<>();
        map.put(idKey, readableObjectId);
        setField(context, "_objectIds", map);
        // Should not throw
        context.checkUnresolvedObjectId();
    }

    @Test
    public void testCheckUnresolvedObjectIdNoUnresolved() throws Exception {
        when(config.isEnabled(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS)).thenReturn(true);
        when(readableObjectId.hasReferringProperties()).thenReturn(false);
        LinkedHashMap<IdKey, ReadableObjectId> map = new LinkedHashMap<>();
        map.put(idKey, readableObjectId);
        setField(context, "_objectIds", map);
        // Should not throw
        context.checkUnresolvedObjectId();
    }

    @Test(expected = UnresolvedForwardReference.class)
    public void testCheckUnresolvedObjectIdWithUnresolved() throws Exception {
        when(config.isEnabled(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS)).thenReturn(true);
        when(readableObjectId.hasReferringProperties()).thenReturn(true);
        // Mock referring properties iterator
        Referring referring = mock(Referring.class);
        when(referring.getBeanType()).thenReturn(String.class);
        when(referring.getLocation()).thenReturn(new JsonLocation(null, 0, 0, 0, 0));
        Iterator<Referring> iterator = Collections.singletonList(referring).iterator();
        when(readableObjectId.referringProperties()).thenReturn(iterator);
        when(readableObjectId.getKey()).thenReturn(idKey);
        when(idKey.key).thenReturn("unresolvedId");

        LinkedHashMap<IdKey, ReadableObjectId> map = new LinkedHashMap<>();
        map.put(idKey, readableObjectId);
        setField(context, "_objectIds", map);

        context.checkUnresolvedObjectId();
    }

    // ==================== deserializerInstance ====================

    @Test
    public void testDeserializerInstanceNullDef() throws Exception {
        assertNull(context.deserializerInstance(annotated, null));
    }

    @Test
    public void testDeserializerInstanceJsonDeserializer() throws Exception {
        JsonDeserializer<?> deser = mock(JsonDeserializer.class);
        JsonDeserializer<Object> result = context.deserializerInstance(annotated, deser);
        assertSame(deser, result);
    }

    @Test
    public void testDeserializerInstanceResolvableDeserializer() throws Exception {
        ResolvableDeserializer resolvable = mock(ResolvableDeserializer.class);
        JsonDeserializer<Object> result = context.deserializerInstance(annotated, resolvable);
        assertSame(resolvable, result);
        verify(resolvable).resolve(context);
    }

    @Test
    public void testDeserializerInstanceClassValid() throws Exception {
        Class<?> deserClass = TestDeserializer.class;
        when(config.canOverrideAccessModifiers()).thenReturn(false);
        JsonDeserializer<Object> result = context.deserializerInstance(annotated, deserClass);
        assertNotNull(result);
        assertTrue(result instanceof TestDeserializer);
    }

    @Test
    public void testDeserializerInstanceClassNone() throws Exception {
        assertNull(context.deserializerInstance(annotated, JsonDeserializer.None.class));
    }

    @Test
    public void testDeserializerInstanceClassBogus() throws Exception {
        // ClassUtil.isBogusClass returns true for Void.class? Actually it checks for certain types.
        // We'll mock ClassUtil? Better to use a known bogus class like Void.TYPE? But isBogusClass checks for Void.class, Void.TYPE, etc.
        // We'll just use Void.class which is bogus.
        assertNull(context.deserializerInstance(annotated, Void.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testDeserializerInstanceClassNotDeserializer() throws Exception {
        context.deserializerInstance(annotated, String.class);
    }

    @Test
    public void testDeserializerInstanceHandlerInstantiator() throws Exception {
        Class<?> deserClass = TestDeserializer.class;
        when(config.getHandlerInstantiator()).thenReturn(handlerInstantiator);
        JsonDeserializer<?> customDeser = mock(JsonDeserializer.class);
        when(handlerInstantiator.deserializerInstance(config, annotated, deserClass)).thenReturn(customDeser);
        JsonDeserializer<Object> result = context.deserializerInstance(annotated, deserClass);
        assertSame(customDeser, result);
    }

    // ==================== keyDeserializerInstance ====================

    @Test
    public void testKeyDeserializerInstanceNullDef() throws Exception {
        assertNull(context.keyDeserializerInstance(annotated, null));
    }

    @Test
    public void testKeyDeserializerInstanceKeyDeserializer() throws Exception {
        KeyDeserializer deser = mock(KeyDeserializer.class);
        KeyDeserializer result = context.keyDeserializerInstance(annotated, deser);
        assertSame(deser, result);
    }

    @Test
    public void testKeyDeserializerInstanceResolvable() throws Exception {
        KeyDeserializer resolvable = mock(KeyDeserializer.class, withSettings().extraInterfaces(ResolvableDeserializer.class));
        KeyDeserializer result = context.keyDeserializerInstance(annotated, resolvable);
        assertSame(resolvable, result);
        verify((ResolvableDeserializer) resolvable).resolve(context);
    }

    @Test
    public void testKeyDeserializerInstanceClassValid() throws Exception {
        Class<?> deserClass = TestKeyDeserializer.class;
        when(config.canOverrideAccessModifiers()).thenReturn(false);
        KeyDeserializer result = context.keyDeserializerInstance(annotated, deserClass);
        assertNotNull(result);
        assertTrue(result instanceof TestKeyDeserializer);
    }

    @Test
    public void testKeyDeserializerInstanceClassNone() throws Exception {
        assertNull(context.keyDeserializerInstance(annotated, KeyDeserializer.None.class));
    }

    @Test
    public void testKeyDeserializerInstanceClassBogus() throws Exception {
        assertNull(context.keyDeserializerInstance(annotated, Void.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testKeyDeserializerInstanceClassNotKeyDeserializer() throws Exception {
        context.keyDeserializerInstance(annotated, String.class);
    }

    @Test
    public void testKeyDeserializerInstanceHandlerInstantiator() throws Exception {
        Class<?> deserClass = TestKeyDeserializer.class;
        when(config.getHandlerInstantiator()).thenReturn(handlerInstantiator);
        KeyDeserializer customDeser = mock(KeyDeserializer.class);
        when(handlerInstantiator.keyDeserializerInstance(config, annotated, deserClass)).thenReturn(customDeser);
        KeyDeserializer result = context.keyDeserializerInstance(annotated, deserClass);
        assertSame(customDeser, result);
    }

    // Helper concrete deserializer classes for testing
    public static class TestDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) { return null; }
    }

    public static class TestKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) { return null; }
    }
}
