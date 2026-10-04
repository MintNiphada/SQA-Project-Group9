package com.fasterxml.jackson.databind.jsontype.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeDeserializerBase;
import com.fasterxml.jackson.databind.util.ClassUtil;

public class TypeDeserializerBaseTest {

    public static class TestableTypeDeserializerBase extends TypeDeserializerBase {
        public TestableTypeDeserializerBase(JavaType baseType, TypeIdResolver idRes,
                String typePropertyName, boolean typeIdVisible, Class<?> defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public TestableTypeDeserializerBase(TypeDeserializerBase src, BeanProperty property) {
            super(src, property);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new TestableTypeDeserializerBase(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }

        public JsonDeserializer<Object> findDeserializer(DeserializationContext ctxt, String typeId) throws IOException {
            return _findDeserializer(ctxt, typeId);
        }

        public JsonDeserializer<Object> findDefaultImplDeserializer(DeserializationContext ctxt) throws IOException {
            return _findDefaultImplDeserializer(ctxt);
        }

        public Object deserializeWithNativeTypeId(JsonParser jp, DeserializationContext ctxt, Object typeId) throws IOException {
            return _deserializeWithNativeTypeId(jp, ctxt, typeId);
        }

        public JsonDeserializer<Object> handleUnknownTypeId(DeserializationContext ctxt, String typeId,
                TypeIdResolver idResolver, JavaType baseType) throws IOException {
            return _handleUnknownTypeId(ctxt, typeId, idResolver, baseType);
        }
    }

    @Mock
    private JavaType baseType;
    @Mock
    private TypeIdResolver idResolver;
    @Mock
    private DeserializationContext ctxt;
    @Mock
    private JsonParser jp;
    @Mock
    private BeanProperty property;
    @Mock
    private JsonDeserializer<Object> deserializer;
    @Mock
    private JavaType defaultImplType;

    private TestableTypeDeserializerBase instance;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private TestableTypeDeserializerBase createInstance(Class<?> defaultImpl) {
        when(baseType.forcedNarrowBy(any(Class.class))).thenReturn(defaultImplType);
        return new TestableTypeDeserializerBase(baseType, idResolver, "typeProp", false, defaultImpl);
    }

    @Test
    public void testConstructorDefaultImplNull() {
        instance = new TestableTypeDeserializerBase(baseType, idResolver, "typeProp", true, null);
        assertNull(instance.getDefaultImpl());
        assertNotNull(instance.getTypeIdResolver());
        assertEquals("typeProp", instance.getPropertyName());
    }

    @Test
    public void testConstructorDefaultImplNotNull() {
        when(baseType.forcedNarrowBy(String.class)).thenReturn(defaultImplType);
        instance = new TestableTypeDeserializerBase(baseType, idResolver, "typeProp", false, String.class);
        assertNotNull(instance.getDefaultImpl());
        verify(baseType).forcedNarrowBy(String.class);
    }

    @Test
    public void testCopyConstructor() {
        instance = createInstance(null);
        TestableTypeDeserializerBase copy = new TestableTypeDeserializerBase(instance, property);
        assertEquals(instance.getPropertyName(), copy.getPropertyName());
        assertEquals(instance.getTypeIdResolver(), copy.getTypeIdResolver());
        assertSame(property, copy._property);
    }

    @Test
    public void testBaseTypeName() {
        when(baseType.getRawClass()).thenReturn((Class) String.class);
        instance = createInstance(null);
        assertEquals("java.lang.String", instance.baseTypeName());
    }

    @Test
    public void testGetPropertyName() {
        instance = createInstance(null);
        assertEquals("typeProp", instance.getPropertyName());
    }

    @Test
    public void testGetTypeIdResolver() {
        instance = createInstance(null);
        assertSame(idResolver, instance.getTypeIdResolver());
    }

    @Test
    public void testGetDefaultImplNull() {
        instance = createInstance(null);
        assertNull(instance.getDefaultImpl());
    }

    @Test
    public void testGetDefaultImplNotNull() {
        when(defaultImplType.getRawClass()).thenReturn((Class) Integer.class);
        instance = createInstance(Integer.class);
        assertEquals(Integer.class, instance.getDefaultImpl());
    }

    @Test
    public void testToString() {
        when(baseType.toString()).thenReturn("baseTypeStr");
        when(idResolver.toString()).thenReturn("idResStr");
        instance = createInstance(null);
        String str = instance.toString();
        assertTrue(str.contains("base-type:baseTypeStr"));
        assertTrue(str.contains("id-resolver: idResStr"));
    }

    @Test
    public void testFindDeserializerCached() throws Exception {
        instance = createInstance(null);
        instance._deserializers.put("type1", deserializer);
        JsonDeserializer<Object> result = instance.findDeserializer(ctxt, "type1");
        assertSame(deserializer, result);
    }

    @Test
    public void testFindDeserializerResolvedTypeNarrow() throws Exception {
        instance = createInstance(null);
        JavaType resolvedType = mock(JavaType.class);
        when(idResolver.typeFromId(ctxt, "type2")).thenReturn(resolvedType);
        when(baseType.getClass()).thenReturn((Class) JavaType.class);
        when(resolvedType.getClass()).thenReturn((Class) JavaType.class);
        when(baseType.narrowBy(any(Class.class))).thenReturn(resolvedType);
        when(ctxt.findContextualValueDeserializer(resolvedType, null)).thenReturn(deserializer);

        JsonDeserializer<Object> result = instance.findDeserializer(ctxt, "type2");
        assertSame(deserializer, result);
        verify(baseType).narrowBy(any(Class.class));
    }

    @Test
    public void testFindDeserializerResolvedTypeNoNarrow() throws Exception {
        instance = createInstance(null);
        JavaType resolvedType = mock(JavaType.class);
        when(idResolver.typeFromId(ctxt, "type3")).thenReturn(resolvedType);
        when(baseType.getClass()).thenReturn((Class) JavaType.class);
        when(resolvedType.getClass()).thenReturn((Class) Object.class);
        when(ctxt.findContextualValueDeserializer(resolvedType, null)).thenReturn(deserializer);

        JsonDeserializer<Object> result = instance.findDeserializer(ctxt, "type3");
        assertSame(deserializer, result);
        verify(baseType, never()).narrowBy(any(Class.class));
    }

    @Test
    public void testFindDeserializerNullTypeDefaultImplFound() throws Exception {
        instance = createInstance(null);
        when(idResolver.typeFromId(ctxt, "type4")).thenReturn(null);
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(false);

        JsonDeserializer<Object> result = instance.findDeserializer(ctxt, "type4");
        assertSame(NullifyingDeserializer.instance, result);
    }

    @Test
    public void testFindDeserializerNullTypeDefaultImplNullHandleUnknown() throws Exception {
        instance = createInstance(null);
        when(idResolver.typeFromId(ctxt, "type5")).thenReturn(null);
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        when(ctxt.unknownTypeException(any(JavaType.class), eq("type5"), anyString())).thenThrow(new JsonMappingException("test"));

        try {
            instance.findDeserializer(ctxt, "type5");
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testFindDefaultImplDeserializerNullDefaultImplFailDisabled() throws Exception {
        instance = createInstance(null);
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(false);
        JsonDeserializer<Object> result = instance.findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, result);
    }

    @Test
    public void testFindDefaultImplDeserializerNullDefaultImplFailEnabled() throws Exception {
        instance = createInstance(null);
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        JsonDeserializer<Object> result = instance.findDefaultImplDeserializer(ctxt);
        assertNull(result);
    }

    @Test
    public void testFindDefaultImplDeserializerBogusClass() throws Exception {
        when(defaultImplType.getRawClass()).thenReturn((Class) Void.class);
        instance = createInstance(Void.class);
        JsonDeserializer<Object> result = instance.findDefaultImplDeserializer(ctxt);
        assertSame(NullifyingDeserializer.instance, result);
    }

    @Test
    public void testFindDefaultImplDeserializerAlreadySet() throws Exception {
        when(defaultImplType.getRawClass()).thenReturn((Class) String.class);
        instance = createInstance(String.class);
        instance._defaultImplDeserializer = deserializer;
        JsonDeserializer<Object> result = instance.findDefaultImplDeserializer(ctxt);
        assertSame(deserializer, result);
    }

    @Test
    public void testFindDefaultImplDeserializerCreateNew() throws Exception {
        when(defaultImplType.getRawClass()).thenReturn((Class) String.class);
        instance = createInstance(String.class);
        when(ctxt.findContextualValueDeserializer(defaultImplType, null)).thenReturn(deserializer);
        JsonDeserializer<Object> result = instance.findDefaultImplDeserializer(ctxt);
        assertSame(deserializer, result);
        assertSame(deserializer, instance._defaultImplDeserializer);
    }

    @Test
    public void testDeserializeWithNativeTypeIdNullTypeIdDefaultFound() throws Exception {
        instance = createInstance(null);
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(false);
        when(deserializer.deserialize(jp, ctxt)).thenReturn("result");
        Object result = instance.deserializeWithNativeTypeId(jp, ctxt, null);
        assertEquals("result", result);
    }

    @Test
    public void testDeserializeWithNativeTypeIdNullTypeIdDefaultNull() throws Exception {
        instance = createInstance(null);
        when(ctxt.isEnabled(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)).thenReturn(true);
        when(ctxt.mappingException(anyString())).thenThrow(new JsonMappingException("test"));
        try {
            instance.deserializeWithNativeTypeId(jp, ctxt, null);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testDeserializeWithNativeTypeIdStringTypeId() throws Exception {
        instance = createInstance(null);
        instance._deserializers.put("typeStr", deserializer);
        when(deserializer.deserialize(jp, ctxt)).thenReturn("result");
        Object result = instance.deserializeWithNativeTypeId(jp, ctxt, "typeStr");
        assertEquals("result", result);
    }

    @Test
    public void testDeserializeWithNativeTypeIdNonStringTypeId() throws Exception {
        instance = createInstance(null);
        instance._deserializers.put("123", deserializer);
        when(deserializer.deserialize(jp, ctxt)).thenReturn("result");
        Object result = instance.deserializeWithNativeTypeId(jp, ctxt, 123);
        assertEquals("result", result);
    }

    @Test
    public void testHandleUnknownTypeIdResolverBaseWithDesc() throws Exception {
        TypeIdResolverBase resolverBase = mock(TypeIdResolverBase.class);
        when(resolverBase.getDescForKnownTypeIds()).thenReturn("typeA,typeB");
        when(ctxt.unknownTypeException(any(JavaType.class), eq("unknown"), eq("known type ids = typeA,typeB")))
                .thenThrow(new JsonMappingException("test"));
        instance = createInstance(null);
        try {
            instance.handleUnknownTypeId(ctxt, "unknown", resolverBase, baseType);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testHandleUnknownTypeIdResolverBaseNullDesc() throws Exception {
        TypeIdResolverBase resolverBase = mock(TypeIdResolverBase.class);
        when(resolverBase.getDescForKnownTypeIds()).thenReturn(null);
        when(ctxt.unknownTypeException(any(JavaType.class), eq("unknown"), eq("known type ids are not statically known")))
                .thenThrow(new JsonMappingException("test"));
        instance = createInstance(null);
        try {
            instance.handleUnknownTypeId(ctxt, "unknown", resolverBase, baseType);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testHandleUnknownTypeIdNotResolverBase() throws Exception {
        when(ctxt.unknownTypeException(any(JavaType.class), eq("unknown"), isNull()))
                .thenThrow(new JsonMappingException("test"));
        instance = createInstance(null);
        try {
            instance.handleUnknownTypeId(ctxt, "unknown", idResolver, baseType);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }
}
