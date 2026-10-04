package com.fasterxml.jackson.databind.jsontype.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class AsWrapperTypeDeserializerTest {

    private ObjectMapper mapper;
    private JavaType baseType;
    private ClassNameIdResolver idResolver;
    private AsWrapperTypeDeserializer deserializer;

    static class DummyBase {
        public int x;
    }

    static class DummySub extends DummyBase {
        public int y;
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        baseType = TypeFactory.defaultInstance().constructType(DummyBase.class);
        idResolver = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        deserializer = new AsWrapperTypeDeserializer(baseType, idResolver, "type", false, DummyBase.class);
    }

    @Test
    public void testGetTypeInclusion() {
        Assert.assertEquals(As.WRAPPER_OBJECT, deserializer.getTypeInclusion());
    }

    @Test
    public void testForProperty() {
        TypeDeserializer same = deserializer.forProperty(null);
        Assert.assertSame(deserializer, same);

        BeanProperty prop = new BeanProperty.Std(
                new PropertyName("prop"),
                baseType,
                new PropertyName("prop"),
                null,
                null,
                PropertyMetadata.STD_OPTIONAL
        );
        TypeDeserializer distinct = deserializer.forProperty(prop);
        Assert.assertNotSame(deserializer, distinct);
        Assert.assertEquals(prop, distinct.getPropertyName() == null ? prop : prop);
        Assert.assertSame(distinct, distinct.forProperty(prop));
    }

    @Test
    public void testDeserializeTypedFromObject() throws IOException {
        String json = "{\"" + DummySub.class.getName() + "\":{\"x\":1,\"y\":2}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deserializer.deserializeTypedFromObject(p, ctxt);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof DummySub);
        DummySub sub = (DummySub) result;
        Assert.assertEquals(1, sub.x);
        Assert.assertEquals(2, sub.y);
        p.close();
    }

    @Test
    public void testDeserializeTypedFromArray() throws IOException {
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(java.util.List.class, String.class);
        ClassNameIdResolver listIdRes = new ClassNameIdResolver(listType, TypeFactory.defaultInstance());
        AsWrapperTypeDeserializer arrayDeser = new AsWrapperTypeDeserializer(listType, listIdRes, "type", false, java.util.ArrayList.class);

        String json = "{\"" + java.util.ArrayList.class.getName() + "\":[\"a\",\"b\"]}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = arrayDeser.deserializeTypedFromArray(p, ctxt);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof java.util.List);
        java.util.List<?> list = (java.util.List<?>) result;
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("a", list.get(0));
        p.close();
    }

    @Test
    public void testDeserializeTypedFromScalar() throws IOException {
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        ClassNameIdResolver strIdRes = new ClassNameIdResolver(strType, TypeFactory.defaultInstance());
        AsWrapperTypeDeserializer scalarDeser = new AsWrapperTypeDeserializer(strType, strIdRes, "type", false, String.class);

        String json = "{\"" + String.class.getName() + "\":\"hello\"}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = scalarDeser.deserializeTypedFromScalar(p, ctxt);
        Assert.assertEquals("hello", result);
        p.close();
    }

    @Test
    public void testDeserializeTypedFromAny() throws IOException {
        String json = "{\"" + DummySub.class.getName() + "\":{\"x\":5,\"y\":6}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deserializer.deserializeTypedFromAny(p, ctxt);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof DummySub);
        Assert.assertEquals(5, ((DummySub) result).x);
        Assert.assertEquals(6, ((DummySub) result).y);
        p.close();
    }

    @Test
    public void testDeserializeWithTypeIdVisible() throws IOException {
        AsWrapperTypeDeserializer deserVisible = new AsWrapperTypeDeserializer(baseType, idResolver, "type", true, DummyBase.class);
        String json = "{\"" + DummySub.class.getName() + "\":{\"x\":3,\"y\":4}}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deserVisible.deserializeTypedFromObject(p, ctxt);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof DummySub);
        DummySub sub = (DummySub) result;
        Assert.assertEquals(3, sub.x);
        Assert.assertEquals(4, sub.y);
        p.close();
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeWrongTokenNotStartObject() throws IOException {
        String json = "[\"not an object\"]";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deserializer.deserializeTypedFromObject(p, ctxt);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeWrongTokenNoFieldName() throws IOException {
        String json = "{}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deserializer.deserializeTypedFromObject(p, ctxt);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeMissingClosingEndObject() throws IOException {
        String json = "{\"" + DummySub.class.getName() + "\":{\"x\":1,\"y\":2}, \"extra\": 3}";
        JsonParser p = mapper.getFactory().createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        deserializer.deserializeTypedFromObject(p, ctxt);
    }

    @Test
    public void testDeserializeWithNativeTypeId() throws IOException {
        TokenBuffer tb = new TokenBuffer(mapper, false);
        tb.writeTypeId(DummySub.class.getName());
        tb.writeStartObject();
        tb.writeFieldName("x");
        tb.writeNumber(10);
        tb.writeFieldName("y");
        tb.writeNumber(20);
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = deserializer.deserializeTypedFromObject(p, ctxt);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof DummySub);
        DummySub sub = (DummySub) result;
        Assert.assertEquals(10, sub.x);
        Assert.assertEquals(20, sub.y);
        p.close();
        tb.close();
    }
}
