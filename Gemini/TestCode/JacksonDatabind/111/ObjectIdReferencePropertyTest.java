package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.UnresolvedId;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReferenceProperty.PropertyReferring;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;

public class ObjectIdReferencePropertyTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {}

    private SettableBeanProperty mockForward;
    private ObjectIdInfo objectIdInfo;
    private JavaType javaType;
    private ObjectIdReferenceProperty prop;

    @Before
    public void setUp() {
        mockForward = Mockito.mock(SettableBeanProperty.class);
        javaType = TypeFactory.defaultInstance().constructType(String.class);
        Mockito.when(mockForward.getType()).thenReturn(javaType);
        Mockito.when(mockForward.getName()).thenReturn("testProp");
        Mockito.when(mockForward.getFullName()).thenReturn(new PropertyName("testProp"));
        Mockito.when(mockForward.getMetadata()).thenReturn(PropertyMetadata.STD_REQUIRED);
        
        objectIdInfo = new ObjectIdInfo(PropertyName.construct("id"), Object.class, null, null);
        prop = new ObjectIdReferenceProperty(mockForward, objectIdInfo);
    }

    @Test
    public void testWithName() {
        PropertyName newName = new PropertyName("newName");
        SettableBeanProperty renamed = prop.withName(newName);
        Assert.assertNotNull(renamed);
        Assert.assertNotSame(prop, renamed);
        Assert.assertEquals("newName", renamed.getName());
    }

    @Test
    public void testWithValueDeserializerSame() {
        SettableBeanProperty same = prop.withValueDeserializer(prop.getValueDeserializer());
        Assert.assertSame(prop, same);
    }

    @Test
    public void testWithValueDeserializerDifferent() {
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        SettableBeanProperty modified = prop.withValueDeserializer(deser);
        Assert.assertNotNull(modified);
        Assert.assertNotSame(prop, modified);
        Assert.assertSame(deser, modified.getValueDeserializer());
    }

    @Test
    public void testWithNullProvider() {
        NullValueProvider nva = Mockito.mock(NullValueProvider.class);
        SettableBeanProperty modified = prop.withNullProvider(nva);
        Assert.assertNotNull(modified);
        Assert.assertNotSame(prop, modified);
        Assert.assertSame(nva, modified.getNullValueProvider());
    }

    @Test
    public void testFixAccess() {
        DeserializationConfig config = Mockito.mock(DeserializationConfig.class);
        prop.fixAccess(config);
        Mockito.verify(mockForward).fixAccess(config);
    }

    @Test
    public void testGetAnnotation() {
        TestAnnotation mockAnnotation = Mockito.mock(TestAnnotation.class);
        Mockito.when(mockForward.getAnnotation(TestAnnotation.class)).thenReturn(mockAnnotation);
        TestAnnotation result = prop.getAnnotation(TestAnnotation.class);
        Assert.assertSame(mockAnnotation, result);
    }

    @Test
    public void testGetMember() {
        AnnotatedMember member = Mockito.mock(AnnotatedMember.class);
        Mockito.when(mockForward.getMember()).thenReturn(member);
        Assert.assertSame(member, prop.getMember());
    }

    @Test
    public void testGetCreatorIndex() {
        Mockito.when(mockForward.getCreatorIndex()).thenReturn(42);
        Assert.assertEquals(42, prop.getCreatorIndex());
    }

    @Test
    public void testSet() throws IOException {
        Object target = new Object();
        Object value = "value";
        prop.set(target, value);
        Mockito.verify(mockForward).set(target, value);
    }

    @Test
    public void testSetAndReturn() throws IOException {
        Object target = new Object();
        Object value = "value";
        Object returned = new Object();
        Mockito.when(mockForward.setAndReturn(target, value)).thenReturn(returned);
        Object actual = prop.setAndReturn(target, value);
        Assert.assertSame(returned, actual);
    }

    @Test
    public void testDeserializeAndSetSuccess() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(deser.deserialize(parser, ctxt)).thenReturn("deserialized");
        
        ObjectIdReferenceProperty propWithDeser = (ObjectIdReferenceProperty) prop.withValueDeserializer(deser);
        Object target = new Object();
        Mockito.when(mockForward.setAndReturn(target, "deserialized")).thenReturn(target);
        
        propWithDeser.deserializeAndSet(parser, ctxt, target);
        Mockito.verify(mockForward).setAndReturn(target, "deserialized");
    }

    @Test
    public void testDeserializeSetAndReturnWithForwardReferenceUsingObjectIdInfo() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        
        UnresolvedForwardReference ref = new UnresolvedForwardReference(parser, "Unresolved", new JsonLocation(null, 0, 0, 0), null);
        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);
        ref.getRoid();
        ReadableObjectId realRoid = new ReadableObjectId(new ObjectIdGenerator.IdKey(Object.class, Object.class, "id1"));
        
        UnresolvedForwardReference spyRef = Mockito.spy(ref);
        Mockito.when(spyRef.getRoid()).thenReturn(realRoid);
        Mockito.doThrow(spyRef).when(deser).deserialize(parser, ctxt);

        ObjectIdReferenceProperty propWithDeser = (ObjectIdReferenceProperty) prop.withValueDeserializer(deser);
        Object target = new Object();

        Object result = propWithDeser.deserializeSetAndReturn(parser, ctxt, target);
        Assert.assertNull(result);
        Assert.assertTrue(realRoid.hasReferringProperties());
    }

    @Test
    public void testDeserializeSetAndReturnWithForwardReferenceUsingObjectIdReader() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        ObjectIdReader objectIdReader = Mockito.mock(ObjectIdReader.class);
        Mockito.when(deser.getObjectIdReader()).thenReturn(objectIdReader);

        ObjectIdReferenceProperty propWithoutIdInfo = new ObjectIdReferenceProperty(mockForward, null);
        ObjectIdReferenceProperty propWithDeser = (ObjectIdReferenceProperty) propWithoutIdInfo.withValueDeserializer(deser);

        UnresolvedForwardReference ref = new UnresolvedForwardReference(parser, "Unresolved", new JsonLocation(null, 0, 0, 0), null);
        ReadableObjectId realRoid = new ReadableObjectId(new ObjectIdGenerator.IdKey(Object.class, Object.class, "id2"));
        UnresolvedForwardReference spyRef = Mockito.spy(ref);
        Mockito.when(spyRef.getRoid()).thenReturn(realRoid);
        Mockito.doThrow(spyRef).when(deser).deserialize(parser, ctxt);

        Object target = new Object();
        Object result = propWithDeser.deserializeSetAndReturn(parser, ctxt, target);
        Assert.assertNull(result);
        Assert.assertTrue(realRoid.hasReferringProperties());
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeSetAndReturnThrowsWhenNoIdentityInfo() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        Mockito.when(deser.getObjectIdReader()).thenReturn(null);

        ObjectIdReferenceProperty propWithoutIdInfo = new ObjectIdReferenceProperty(mockForward, null);
        ObjectIdReferenceProperty propWithDeser = (ObjectIdReferenceProperty) propWithoutIdInfo.withValueDeserializer(deser);

        UnresolvedForwardReference ref = new UnresolvedForwardReference(parser, "Unresolved", new JsonLocation(null, 0, 0, 0), null);
        Mockito.doThrow(ref).when(deser).deserialize(parser, ctxt);

        Object target = new Object();
        propWithDeser.deserializeSetAndReturn(parser, ctxt, target);
    }

    @Test
    public void testPropertyReferringHandleResolvedForwardReferenceSuccess() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        UnresolvedForwardReference ref = new UnresolvedForwardReference(parser, "test", new JsonLocation(null, 0, 0, 0), new UnresolvedId("id1", Object.class, new JsonLocation(null, 0, 0, 0)));
        Object pojo = new Object();
        PropertyReferring referring = new PropertyReferring(prop, ref, String.class, pojo);
        
        Assert.assertSame(pojo, referring._pojo);
        referring.handleResolvedForwardReference("id1", "resolvedValue");
        Mockito.verify(mockForward).set(pojo, "resolvedValue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPropertyReferringHandleResolvedForwardReferenceMismatch() throws IOException {
        JsonParser parser = Mockito.mock(JsonParser.class);
        UnresolvedForwardReference ref = new UnresolvedForwardReference(parser, "test", new JsonLocation(null, 0, 0, 0), new UnresolvedId("id1", Object.class, new JsonLocation(null, 0, 0, 0)));
        Object pojo = new Object();
        PropertyReferring referring = new PropertyReferring(prop, ref, String.class, pojo);

        referring.handleResolvedForwardReference("wrongId", "resolvedValue");
    }
}
