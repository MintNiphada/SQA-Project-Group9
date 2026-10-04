package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class AtomicReferenceDeserializerTest {

    private JavaType javaType;
    private AtomicReferenceDeserializer deserializer;

    @Before
    public void setUp() {
        javaType = TypeFactory.defaultInstance().constructType(AtomicReference.class);
        deserializer = new AtomicReferenceDeserializer(javaType, null, null, null);
    }

    @Test
    public void testConstructorAndProperties() {
        Assert.assertNotNull(deserializer);
        Assert.assertEquals(javaType, deserializer.getValueType());
    }

    @Test
    public void testWithResolved() {
        AtomicReferenceDeserializer resolved = deserializer.withResolved(null, null);
        Assert.assertNotNull(resolved);
        Assert.assertNotSame(deserializer, resolved);
        Assert.assertEquals(javaType, resolved.getValueType());
    }

    @Test
    public void testGetNullValue() throws JsonMappingException {
        AtomicReference<Object> nullValue = deserializer.getNullValue((DeserializationContext) null);
        Assert.assertNotNull(nullValue);
        Assert.assertNull(nullValue.get());
    }

    @Test
    public void testGetEmptyValue() {
        Object emptyValue = deserializer.getEmptyValue((DeserializationContext) null);
        Assert.assertNotNull(emptyValue);
        Assert.assertTrue(emptyValue instanceof AtomicReference);
        Assert.assertNull(((AtomicReference<?>) emptyValue).get());
    }

    @Test
    public void testReferenceValue() {
        String content = "testContent";
        AtomicReference<Object> ref = deserializer.referenceValue(content);
        Assert.assertNotNull(ref);
        Assert.assertEquals(content, ref.get());

        AtomicReference<Object> nullRef = deserializer.referenceValue(null);
        Assert.assertNotNull(nullRef);
        Assert.assertNull(nullRef.get());
    }

    @Test
    public void testGetReferenced() {
        String content = "referencedValue";
        AtomicReference<Object> ref = new AtomicReference<Object>(content);
        Object extracted = deserializer.getReferenced(ref);
        Assert.assertEquals(content, extracted);

        AtomicReference<Object> emptyRef = new AtomicReference<Object>();
        Assert.assertNull(deserializer.getReferenced(emptyRef));
    }

    @Test
    public void testUpdateReference() {
        AtomicReference<Object> ref = new AtomicReference<Object>("initial");
        AtomicReference<Object> updated = deserializer.updateReference(ref, "updated");
        Assert.assertSame(ref, updated);
        Assert.assertEquals("updated", updated.get());

        deserializer.updateReference(ref, null);
        Assert.assertNull(ref.get());
    }

    @Test
    public void testSupportsUpdate() {
        Boolean supports = deserializer.supportsUpdate((DeserializationConfig) null);
        Assert.assertEquals(Boolean.TRUE, supports);
    }
}
