package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionDeserializerTest {

    @Test
    public void testIsCachable() {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        CollectionDeserializer deser1 = new CollectionDeserializer(listType, null, null, null);
        Assert.assertTrue(deser1.isCachable());

        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        CollectionDeserializer deser2 = new CollectionDeserializer(listType, valDeser, null, null);
        Assert.assertFalse(deser2.isCachable());

        TypeDeserializer typeDeser = Mockito.mock(TypeDeserializer.class);
        CollectionDeserializer deser3 = new CollectionDeserializer(listType, null, typeDeser, null);
        Assert.assertFalse(deser3.isCachable());

        CollectionDeserializer deser4 = new CollectionDeserializer(listType, null, null, null, valDeser, Boolean.TRUE);
        Assert.assertFalse(deser4.isCachable());
    }

    @Test
    public void testGetters() {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, null);

        Assert.assertEquals(listType.getContentType(), deser.getContentType());
        Assert.assertSame(valDeser, deser.getContentDeserializer());
    }

    @Test
    public void testWithResolved() {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        TypeDeserializer typeDeser = Mockito.mock(TypeDeserializer.class);
        ValueInstantiator inst = Mockito.mock(ValueInstantiator.class);

        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, typeDeser, inst, null, Boolean.FALSE);
        CollectionDeserializer same = deser.withResolved(null, valDeser, typeDeser, Boolean.FALSE);
        Assert.assertSame(deser, same);

        CollectionDeserializer diff = deser.withResolved(null, valDeser, typeDeser, Boolean.TRUE);
        Assert.assertNotSame(deser, diff);

        CollectionDeserializer deprecatedResolved = deser.withResolved(null, valDeser, typeDeser);
        Assert.assertSame(deser, deprecatedResolved);

        CollectionDeserializer copied = new CollectionDeserializer(deser);
        Assert.assertEquals(deser.getContentType(), copied.getContentType());
    }

    @Test
    public void testDeserializeWithType() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, null, null, null);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        TypeDeserializer typeDeser = Mockito.mock(TypeDeserializer.class);

        List<Object> expected = new ArrayList<Object>();
        Mockito.when(typeDeser.deserializeTypedFromArray(parser, ctxt)).thenReturn(expected);

        Object result = deser.deserializeWithType(parser, ctxt, typeDeser);
        Assert.assertSame(expected, result);
    }

    @Test
    public void testDeserializeUsingDelegate() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> delegateDeser = Mockito.mock(JsonDeserializer.class);
        ValueInstantiator inst = Mockito.mock(ValueInstantiator.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, null, null, inst, delegateDeser, null);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Object delegateValue = "dummy";
        List<Object> expectedList = new ArrayList<Object>();
        Mockito.when(delegateDeser.deserialize(parser, ctxt)).thenReturn(delegateValue);
        Mockito.when(inst.createUsingDelegate(ctxt, delegateValue)).thenReturn(expectedList);

        Collection<Object> res = deser.deserialize(parser, ctxt);
        Assert.assertSame(expectedList, res);
    }

    @Test
    public void testDeserializeEmptyString() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        ValueInstantiator inst = Mockito.mock(ValueInstantiator.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, null, null, inst, null, null);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.hasToken(JsonToken.VALUE_STRING)).thenReturn(true);
        Mockito.when(parser.getText()).thenReturn("");
        List<Object> expectedList = new ArrayList<Object>();
        Mockito.when(inst.createFromString(ctxt, "")).thenReturn(expectedList);

        Collection<Object> res = deser.deserialize(parser, ctxt);
        Assert.assertSame(expectedList, res);
    }

    @Test
    public void testDeserializeNormalArray() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        ValueInstantiator inst = Mockito.mock(ValueInstantiator.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, inst, null, null);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.hasToken(JsonToken.VALUE_STRING)).thenReturn(false);
        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(true);
        Mockito.when(inst.createUsingDefault(ctxt)).thenReturn(new ArrayList<Object>());
        Mockito.when(parser.nextToken())
                .thenReturn(JsonToken.VALUE_STRING)
                .thenReturn(JsonToken.VALUE_NULL)
                .thenReturn(JsonToken.END_ARRAY);

        Mockito.when(valDeser.deserialize(parser, ctxt)).thenReturn("first");
        Mockito.when(valDeser.getNullValue(ctxt)).thenReturn(null);

        Collection<Object> result = deser.deserialize(parser, ctxt);
        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.contains("first"));
        Assert.assertTrue(result.contains(null));
    }

    @Test
    public void testDeserializeWithPolymorphicValues() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, Object.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        TypeDeserializer typeDeser = Mockito.mock(TypeDeserializer.class);
        ValueInstantiator inst = Mockito.mock(ValueInstantiator.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, typeDeser, inst, null, null);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(true);
        Mockito.when(parser.nextToken()).thenReturn(JsonToken.START_OBJECT).thenReturn(JsonToken.END_ARRAY);
        Mockito.when(valDeser.deserializeWithType(parser, ctxt, typeDeser)).thenReturn("typedVal");

        List<Object> target = new ArrayList<Object>();
        Collection<Object> res = deser.deserialize(parser, ctxt, target);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("typedVal", res.iterator().next());
    }

    @Test
    public void testHandleNonArrayAcceptSingleValueTrue() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        ValueInstantiator inst = Mockito.mock(ValueInstantiator.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, inst, null, Boolean.TRUE);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(false);
        Mockito.when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(valDeser.deserialize(parser, ctxt)).thenReturn("single");

        List<Object> target = new ArrayList<Object>();
        Collection<Object> result = deser.deserialize(parser, ctxt, target);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("single", result.iterator().next());
    }

    @Test
    public void testHandleNonArrayWithNullValue() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        ValueInstantiator inst = Mockito.mock(ValueInstantiator.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, inst, null, Boolean.TRUE);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(false);
        Mockito.when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_NULL);
        Mockito.when(valDeser.getNullValue(ctxt)).thenReturn(null);

        List<Object> target = new ArrayList<Object>();
        Collection<Object> result = deser.deserialize(parser, ctxt, target);
        Assert.assertEquals(1, result.size());
        Assert.assertNull(result.iterator().next());
    }

    @Test
    public void testHandleNonArrayWithTypeDeserializer() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, Object.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        TypeDeserializer typeDeser = Mockito.mock(TypeDeserializer.class);
        ValueInstantiator inst = Mockito.mock(ValueInstantiator.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, typeDeser, inst, null, Boolean.TRUE);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(false);
        Mockito.when(parser.getCurrentToken()).thenReturn(JsonToken.START_OBJECT);
        Mockito.when(valDeser.deserializeWithType(parser, ctxt, typeDeser)).thenReturn("polyVal");

        List<Object> target = new ArrayList<Object>();
        Collection<Object> result = deser.deserialize(parser, ctxt, target);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("polyVal", result.iterator().next());
    }

    @Test(expected = JsonMappingException.class)
    public void testHandleNonArrayThrowsExceptionWhenNotAllowed() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, null, null, Boolean.FALSE);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(false);
        Mockito.when(ctxt.mappingException(List.class)).thenReturn(new JsonMappingException(null, "Not allowed"));

        deser.deserialize(parser, ctxt, new ArrayList<Object>());
    }

    @Test(expected = JsonMappingException.class)
    public void testHandleNonArrayThrowsOnDeserializerException() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, null, null, Boolean.TRUE);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(false);
        Mockito.when(parser.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(valDeser.deserialize(parser, ctxt)).thenThrow(new RuntimeException("Boom"));

        deser.deserialize(parser, ctxt, new ArrayList<Object>());
    }

    @Test(expected = RuntimeException.class)
    public void testDeserializeArrayUnwrappedException() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, null, null, null);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(true);
        Mockito.when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS)).thenReturn(false);
        Mockito.when(valDeser.deserialize(parser, ctxt)).thenThrow(new RuntimeException("Unwrapped"));

        deser.deserialize(parser, ctxt, new ArrayList<Object>());
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeArrayWrappedException() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, null, null, null);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(true);
        Mockito.when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(ctxt.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS)).thenReturn(true);
        Mockito.when(valDeser.deserialize(parser, ctxt)).thenThrow(new IOException("Wrapped"));

        deser.deserialize(parser, ctxt, new ArrayList<Object>());
    }

    @Test
    public void testCollectionReferringAccumulator() throws IOException {
        List<Object> result = new ArrayList<Object>();
        CollectionDeserializer.CollectionReferringAccumulator acc =
                new CollectionDeserializer.CollectionReferringAccumulator(String.class, result);

        acc.add("item1");
        Assert.assertEquals(1, result.size());

        JsonParser parser = Mockito.mock(JsonParser.class);
        UnresolvedForwardReference ref1 = new UnresolvedForwardReference(parser, "msg", new JsonLocation(null, 0L, 0L, 0, 0), null);
        ReadableObjectId.Referring r1 = acc.handleUnresolvedReference(ref1);
        Assert.assertNotNull(r1);

        acc.add("item2");
        acc.resolveForwardReference(ref1.getUnresolvedId(), "resolvedRef1");

        Assert.assertEquals(3, result.size());
        Assert.assertEquals("item1", result.get(0));
        Assert.assertEquals("resolvedRef1", result.get(1));
        Assert.assertEquals("item2", result.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCollectionReferringAccumulatorNotFound() throws IOException {
        List<Object> result = new ArrayList<Object>();
        CollectionDeserializer.CollectionReferringAccumulator acc =
                new CollectionDeserializer.CollectionReferringAccumulator(String.class, result);
        acc.resolveForwardReference("unknownId", "val");
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeUnresolvedForwardReferenceWithoutObjectIdReader() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, null);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(true);
        Mockito.when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING);
        UnresolvedForwardReference ref = new UnresolvedForwardReference(parser, "unresolved", new JsonLocation(null, 0L, 0L, 0, 0), null);
        Mockito.when(valDeser.deserialize(parser, ctxt)).thenThrow(ref);

        deser.deserialize(parser, ctxt, new ArrayList<Object>());
    }

    @Test
    public void testDeserializeUnresolvedForwardReferenceWithObjectIdReader() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> valDeser = Mockito.mock(JsonDeserializer.class);
        ObjectIdReader oir = Mockito.mock(ObjectIdReader.class);
        Mockito.when(valDeser.getObjectIdReader()).thenReturn(oir);

        CollectionDeserializer deser = new CollectionDeserializer(listType, valDeser, null, null);

        JsonParser parser = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);

        Mockito.when(parser.isExpectedStartArrayToken()).thenReturn(true);
        Mockito.when(parser.nextToken()).thenReturn(JsonToken.VALUE_STRING).thenReturn(JsonToken.END_ARRAY);

        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);
        UnresolvedForwardReference ref = new UnresolvedForwardReference(parser, "ref", new JsonLocation(null, 0L, 0L, 0, 0), roid);
        Mockito.when(valDeser.deserialize(parser, ctxt)).thenThrow(ref);

        List<Object> list = new ArrayList<Object>();
        deser.deserialize(parser, ctxt, list);
        Mockito.verify(roid).appendReferring(Mockito.any(ReadableObjectId.Referring.class));
    }

    @Test
    public void testCreateContextual() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        CollectionDeserializer deser = new CollectionDeserializer(listType, null, null, null);

        CollectionDeserializer contextual = deser.createContextual(ctxt, null);
        Assert.assertNotNull(contextual);
        Assert.assertNotNull(contextual.getContentDeserializer());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateContextualInvalidDelegate() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);

        ValueInstantiator inst = Mockito.mock(ValueInstantiator.class);
        Mockito.when(inst.canCreateUsingDelegate()).thenReturn(true);
        Mockito.when(inst.getDelegateType(Mockito.any(DeserializationConfig.class))).thenReturn(null);

        CollectionDeserializer deser = new CollectionDeserializer(listType, null, null, inst);
        deser.createContextual(ctxt, null);
    }
}
