package com.fasterxml.jackson.databind.ser.impl;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class WritableObjectIdTest {

    @Test
    public void testConstructorAndInitialState() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId oid = new WritableObjectId(gen);

        Assert.assertSame(gen, oid.generator);
        Assert.assertNull(oid.id);
        Assert.assertFalse(oid.idWritten);
    }

    @Test
    public void testGenerateId() {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId oid = new WritableObjectId(gen);

        Object id1 = oid.generateId(new Object());
        Assert.assertEquals(1, id1);
        Assert.assertEquals(1, oid.id);

        Object id2 = oid.generateId(new Object());
        Assert.assertEquals(2, id2);
        Assert.assertEquals(2, oid.id);
    }

    @Test
    public void testWriteAsIdWhenIdIsNull() throws IOException {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId oid = new WritableObjectId(gen);
        JsonGenerator jsonGen = Mockito.mock(JsonGenerator.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        ObjectIdWriter writer = ObjectIdWriter.construct(
                TypeFactory.defaultInstance().constructType(String.class),
                (PropertyName) null,
                gen,
                true
        );

        boolean written = oid.writeAsId(jsonGen, prov, writer);
        Assert.assertFalse(written);
        Mockito.verifyZeroInteractions(jsonGen);
    }

    @Test
    public void testWriteAsIdWhenIdNotNullButNotWrittenAndNotAlwaysAsId() throws IOException {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId oid = new WritableObjectId(gen);
        oid.id = 123;
        oid.idWritten = false;

        JsonGenerator jsonGen = Mockito.mock(JsonGenerator.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        ObjectIdWriter writer = ObjectIdWriter.construct(
                TypeFactory.defaultInstance().constructType(String.class),
                (PropertyName) null,
                gen,
                false
        );

        boolean written = oid.writeAsId(jsonGen, prov, writer);
        Assert.assertFalse(written);
        Mockito.verifyZeroInteractions(jsonGen);
    }

    @Test
    public void testWriteAsIdWithNativeObjectId() throws IOException {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId oid = new WritableObjectId(gen);
        oid.id = 123;
        oid.idWritten = true;

        JsonGenerator jsonGen = Mockito.mock(JsonGenerator.class);
        Mockito.when(jsonGen.canWriteObjectId()).thenReturn(true);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);

        ObjectIdWriter writer = ObjectIdWriter.construct(
                TypeFactory.defaultInstance().constructType(String.class),
                (PropertyName) null,
                gen,
                false
        );

        boolean written = oid.writeAsId(jsonGen, prov, writer);
        Assert.assertTrue(written);
        Mockito.verify(jsonGen).canWriteObjectId();
        Mockito.verify(jsonGen).writeObjectRef("123");
        Mockito.verifyNoMoreInteractions(jsonGen);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWriteAsIdWithSerializer() throws IOException {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId oid = new WritableObjectId(gen);
        oid.id = "myId";

        JsonGenerator jsonGen = Mockito.mock(JsonGenerator.class);
        Mockito.when(jsonGen.canWriteObjectId()).thenReturn(false);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);

        JsonSerializer<Object> serializer = Mockito.mock(JsonSerializer.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdWriter writer = new ObjectIdWriter(type, new SerializedString("id"), gen, serializer, true);

        boolean written = oid.writeAsId(jsonGen, prov, writer);
        Assert.assertTrue(written);
        Mockito.verify(jsonGen).canWriteObjectId();
        Mockito.verify(serializer).serialize("myId", jsonGen, prov);
    }

    @Test
    public void testWriteAsFieldNativeObjectId() throws IOException {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId oid = new WritableObjectId(gen);
        oid.id = 456;

        JsonGenerator jsonGen = Mockito.mock(JsonGenerator.class);
        Mockito.when(jsonGen.canWriteObjectId()).thenReturn(true);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);

        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        ObjectIdWriter writer = ObjectIdWriter.construct(type, new PropertyName("id"), gen, false);

        oid.writeAsField(jsonGen, prov, writer);

        Assert.assertTrue(oid.idWritten);
        Mockito.verify(jsonGen).canWriteObjectId();
        Mockito.verify(jsonGen).writeObjectId("456");
        Mockito.verifyNoMoreInteractions(jsonGen);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWriteAsFieldWithPropertyName() throws IOException {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId oid = new WritableObjectId(gen);
        oid.id = "fieldId";

        JsonGenerator jsonGen = Mockito.mock(JsonGenerator.class);
        Mockito.when(jsonGen.canWriteObjectId()).thenReturn(false);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);

        JsonSerializer<Object> serializer = Mockito.mock(JsonSerializer.class);
        SerializableString propName = new SerializedString("@id");
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdWriter writer = new ObjectIdWriter(type, propName, gen, serializer, false);

        oid.writeAsField(jsonGen, prov, writer);

        Assert.assertTrue(oid.idWritten);
        Mockito.verify(jsonGen).canWriteObjectId();
        Mockito.verify(jsonGen).writeFieldName(propName);
        Mockito.verify(serializer).serialize("fieldId", jsonGen, prov);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWriteAsFieldWithNullPropertyName() throws IOException {
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        WritableObjectId oid = new WritableObjectId(gen);
        oid.id = 789;

        JsonGenerator jsonGen = Mockito.mock(JsonGenerator.class);
        Mockito.when(jsonGen.canWriteObjectId()).thenReturn(false);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);

        JsonSerializer<Object> serializer = Mockito.mock(JsonSerializer.class);
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        ObjectIdWriter writer = new ObjectIdWriter(type, null, gen, serializer, false);

        oid.writeAsField(jsonGen, prov, writer);

        Assert.assertTrue(oid.idWritten);
        Mockito.verify(jsonGen).canWriteObjectId();
        Mockito.verifyNoMoreInteractions(jsonGen);
        Mockito.verifyZeroInteractions(serializer);
    }
}
