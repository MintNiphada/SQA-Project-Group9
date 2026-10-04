package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class NullifyingDeserializerTest {

    @Test
    public void testInstanceNotNull() {
        Assert.assertNotNull(NullifyingDeserializer.instance);
    }

    @Test
    public void testConstructor() {
        NullifyingDeserializer deserializer = new NullifyingDeserializer();
        Assert.assertEquals(Object.class, deserializer.handledType());
    }

    @Test
    public void testDeserialize() throws IOException {
        NullifyingDeserializer deserializer = new NullifyingDeserializer();
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Object result = deserializer.deserialize(parser, ctxt);

        Assert.assertNull(result);
        Mockito.verify(parser, Mockito.times(1)).skipChildren();
    }

    @Test
    public void testDeserializeWithTypeStartArray() throws IOException {
        NullifyingDeserializer deserializer = new NullifyingDeserializer();
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        TypeDeserializer typeDeserializer = Mockito.mock(TypeDeserializer.class);

        Object expected = new Object();
        Mockito.when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_START_ARRAY);
        Mockito.when(typeDeserializer.deserializeTypedFromAny(parser, ctxt)).thenReturn(expected);

        Object result = deserializer.deserializeWithType(parser, ctxt, typeDeserializer);

        Assert.assertSame(expected, result);
        Mockito.verify(typeDeserializer, Mockito.times(1)).deserializeTypedFromAny(parser, ctxt);
    }

    @Test
    public void testDeserializeWithTypeStartObject() throws IOException {
        NullifyingDeserializer deserializer = new NullifyingDeserializer();
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        TypeDeserializer typeDeserializer = Mockito.mock(TypeDeserializer.class);

        Object expected = new Object();
        Mockito.when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_START_OBJECT);
        Mockito.when(typeDeserializer.deserializeTypedFromAny(parser, ctxt)).thenReturn(expected);

        Object result = deserializer.deserializeWithType(parser, ctxt, typeDeserializer);

        Assert.assertSame(expected, result);
        Mockito.verify(typeDeserializer, Mockito.times(1)).deserializeTypedFromAny(parser, ctxt);
    }

    @Test
    public void testDeserializeWithTypeFieldName() throws IOException {
        NullifyingDeserializer deserializer = new NullifyingDeserializer();
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        TypeDeserializer typeDeserializer = Mockito.mock(TypeDeserializer.class);

        Object expected = new Object();
        Mockito.when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_FIELD_NAME);
        Mockito.when(typeDeserializer.deserializeTypedFromAny(parser, ctxt)).thenReturn(expected);

        Object result = deserializer.deserializeWithType(parser, ctxt, typeDeserializer);

        Assert.assertSame(expected, result);
        Mockito.verify(typeDeserializer, Mockito.times(1)).deserializeTypedFromAny(parser, ctxt);
    }

    @Test
    public void testDeserializeWithTypeOtherTokens() throws IOException {
        NullifyingDeserializer deserializer = new NullifyingDeserializer();
        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        TypeDeserializer typeDeserializer = Mockito.mock(TypeDeserializer.class);

        Mockito.when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_STRING);
        Object resultString = deserializer.deserializeWithType(parser, ctxt, typeDeserializer);
        Assert.assertNull(resultString);

        Mockito.when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_NUMBER_INT);
        Object resultInt = deserializer.deserializeWithType(parser, ctxt, typeDeserializer);
        Assert.assertNull(resultInt);

        Mockito.when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_NULL);
        Object resultNull = deserializer.deserializeWithType(parser, ctxt, typeDeserializer);
        Assert.assertNull(resultNull);

        Mockito.when(parser.getCurrentTokenId()).thenReturn(JsonTokenId.ID_NO_TOKEN);
        Object resultNoToken = deserializer.deserializeWithType(parser, ctxt, typeDeserializer);
        Assert.assertNull(resultNoToken);

        Mockito.verify(typeDeserializer, Mockito.never()).deserializeTypedFromAny(Mockito.any(JsonParser.class), Mockito.any(DeserializationContext.class));
    }
}
