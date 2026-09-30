package com.fasterxml.jackson.databind.ser;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.Assert;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;

import static org.mockito.Mockito.*;

public class AnyGetterWriterTest {

    @Test
    public void testConstructor() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        Assert.assertNotNull(writer);
    }

    @Test
    public void testGetAndSerializeNullValue() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        
        Object bean = new Object();
        when(accessor.getValue(bean)).thenReturn(null);
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        
        writer.getAndSerialize(bean, gen, provider);
        verify(accessor).getValue(bean);
        verifyNoInteractions(gen);
        verifyNoInteractions(serializer);
    }

    @Test
    public void testGetAndSerializeNonMapValue() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        
        Object bean = new Object();
        when(accessor.getValue(bean)).thenReturn("not a map");
        when(accessor.getName()).thenReturn("getValues");
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        
        try {
            writer.getAndSerialize(bean, gen, provider);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("not java.util.Map"));
            Assert.assertTrue(e.getMessage().contains("getValues()"));
        }
    }

    @Test
    public void testGetAndSerializeMapWithNullSerializer() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, null);
        
        Object bean = new Object();
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        when(accessor.getValue(bean)).thenReturn(map);
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        
        writer.getAndSerialize(bean, gen, provider);
        verify(accessor).getValue(bean);
        verifyNoInteractions(gen);
    }

    @Test
    public void testGetAndSerializeMapWithSerializer() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        
        Object bean = new Object();
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        when(accessor.getValue(bean)).thenReturn(map);
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        
        writer.getAndSerialize(bean, gen, provider);
        verify(serializer).serializeFields(map, gen, provider);
    }

    @Test
    public void testGetAndFilterNullValue() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        
        Object bean = new Object();
        when(accessor.getValue(bean)).thenReturn(null);
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        PropertyFilter filter = mock(PropertyFilter.class);
        
        writer.getAndFilter(bean, gen, provider, filter);
        verify(accessor).getValue(bean);
        verifyNoInteractions(gen);
        verifyNoInteractions(serializer);
    }

    @Test
    public void testGetAndFilterNonMapValue() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        
        Object bean = new Object();
        when(accessor.getValue(bean)).thenReturn(123);
        when(accessor.getName()).thenReturn("getValues");
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        PropertyFilter filter = mock(PropertyFilter.class);
        
        try {
            writer.getAndFilter(bean, gen, provider, filter);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("not java.util.Map"));
            Assert.assertTrue(e.getMessage().contains("getValues()"));
        }
    }

    @Test
    public void testGetAndFilterMapWithNullSerializer() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, null);
        
        Object bean = new Object();
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        when(accessor.getValue(bean)).thenReturn(map);
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        PropertyFilter filter = mock(PropertyFilter.class);
        
        writer.getAndFilter(bean, gen, provider, filter);
        verify(accessor).getValue(bean);
        verifyNoInteractions(gen);
    }

    @Test
    public void testGetAndFilterMapWithSerializer() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        
        Object bean = new Object();
        Map<String, String> map = new HashMap<String, String>();
        map.put("key", "value");
        when(accessor.getValue(bean)).thenReturn(map);
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        PropertyFilter filter = mock(PropertyFilter.class);
        
        writer.getAndFilter(bean, gen, provider, filter);
        verify(serializer).serializeFilteredFields(map, gen, provider, filter, null);
    }

    @Test
    public void testResolve() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        
        SerializerProvider provider = mock(SerializerProvider.class);
        MapSerializer resolvedSerializer = mock(MapSerializer.class);
        when(provider.handlePrimaryContextualization(serializer, property)).thenReturn(resolvedSerializer);
        
        writer.resolve(provider);
        verify(provider).handlePrimaryContextualization(serializer, property);
    }

    @Test
    public void testGetAndSerializeEmptyMap() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        
        Object bean = new Object();
        Map<String, String> map = new HashMap<String, String>();
        when(accessor.getValue(bean)).thenReturn(map);
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        
        writer.getAndSerialize(bean, gen, provider);
        verify(serializer).serializeFields(map, gen, provider);
    }

    @Test
    public void testGetAndFilterEmptyMap() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        AnnotatedMember accessor = mock(AnnotatedMember.class);
        MapSerializer serializer = mock(MapSerializer.class);
        AnyGetterWriter writer = new AnyGetterWriter(property, accessor, serializer);
        
        Object bean = new Object();
        Map<String, String> map = new HashMap<String, String>();
        when(accessor.getValue(bean)).thenReturn(map);
        
        JsonGenerator gen = mock(JsonGenerator.class);
        SerializerProvider provider = mock(SerializerProvider.class);
        PropertyFilter filter = mock(PropertyFilter.class);
        
        writer.getAndFilter(bean, gen, provider, filter);
        verify(serializer).serializeFilteredFields(map, gen, provider, filter, null);
    }
}
