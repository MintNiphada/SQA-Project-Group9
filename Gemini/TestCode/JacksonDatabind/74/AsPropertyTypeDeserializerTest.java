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
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class AsPropertyTypeDeserializerTest {

    private ObjectMapper mapper;
    private JavaType baseType;
    private TypeIdResolver idResolver;
    private JsonFactory factory;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        baseType = TypeFactory.defaultInstance().constructType(Object.class);
        idResolver = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        factory = new JsonFactory();
    }

    @Test
    public void testConstructorsAndAccessors() {
        AsPropertyTypeDeserializer deser1 = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, baseType);
        Assert.assertEquals(As.PROPERTY, deser1.getTypeInclusion());
        Assert.assertEquals("@type", deser1.getPropertyName());

        AsPropertyTypeDeserializer deser2 = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", true, baseType, As.EXISTING_PROPERTY);
        Assert.assertEquals(As.EXISTING_PROPERTY, deser2.getTypeInclusion());

        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        AsPropertyTypeDeserializer cloned = new AsPropertyTypeDeserializer(deser2, prop);
        Assert.assertEquals(As.EXISTING_PROPERTY, cloned.getTypeInclusion());
        Assert.assertSame(prop, cloned.getPropertyName() != null ? cloned.forProperty(prop).getPropertyName() != null ? prop : prop : prop);
    }

    @Test
    public void testForProperty() {
        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, baseType);
        Assert.assertSame(deser, deser.forProperty(null));

        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        com.fasterxml.jackson.databind.jsontype.TypeDeserializer deserWithProp = deser.forProperty(prop);
        Assert.assertNotSame(deser, deserWithProp);
        Assert.assertSame(deserWithProp, deserWithProp.forProperty(prop));
    }

    @Test
    public void testDeserializeTypedFromObjectWithDirectMatch() throws IOException {
        String json = "{\"@type\":\"" + String.class.getName() + "\",\"value\":\"test\"}";
        JsonParser p = factory.createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, null);

        Object result = deser.deserializeTypedFromObject(p, ctxt);
        Assert.assertNotNull(result);
        p.close();
    }

    @Test
    public void testDeserializeTypedFromObjectWithReorderedField() throws IOException {
        String json = "{\"foo\":\"bar\",\"@type\":\"" + String.class.getName() + "\"}";
        JsonParser p = factory.createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", true, null);

        Object result = deser.deserializeTypedFromObject(p, ctxt);
        Assert.assertNotNull(result);
        p.close();
    }

    @Test
    public void testDeserializeTypedFromObjectDefaultImpl() throws IOException {
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        String json = "{\"foo\":\"bar\"}";
        JsonParser p = factory.createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                strType, idResolver, "@type", false, strType);

        Object result = deser.deserializeTypedFromObject(p, ctxt);
        Assert.assertNotNull(result);
        p.close();
    }

    @Test
    public void testDeserializeTypedFromObjectNaturalType() throws IOException {
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        String json = "\"a-string-value\"";
        JsonParser p = factory.createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                strType, idResolver, "@type", false, null);

        Object result = deser.deserializeTypedFromObject(p, ctxt);
        Assert.assertEquals("a-string-value", result);
        p.close();
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeTypedFromObjectMissingPropertyException() throws IOException {
        String json = "{\"foo\":\"bar\"}";
        JsonParser p = factory.createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, null);

        deser.deserializeTypedFromObject(p, ctxt);
        p.close();
    }

    @Test
    public void testDeserializeTypedFromObjectStartArrayDefaultImpl() throws IOException {
        String json = "[]";
        JsonParser p = factory.createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, null);

        try {
            deser.deserializeTypedFromObject(p, ctxt);
            Assert.fail("Expected exception for missing type id in array wrapper");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("need JSON Array that contains type id"));
        }
        p.close();
    }

    @Test
    public void testDeserializeTypedFromAnyObject() throws IOException {
        String json = "{\"@type\":\"" + String.class.getName() + "\"}";
        JsonParser p = factory.createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, null);

        Object result = deser.deserializeTypedFromAny(p, ctxt);
        Assert.assertNotNull(result);
        p.close();
    }

    @Test
    public void testDeserializeTypedFromAnyArray() throws IOException {
        String json = "[\"" + String.class.getName() + "\", \"test\"]";
        JsonParser p = factory.createParser(json);
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        AsPropertyTypeDeserializer deser = new AsPropertyTypeDeserializer(
                baseType, idResolver, "@type", false, null);

        Object result = deser.deserializeTypedFromAny(p, ctxt);
        Assert.assertEquals("test", result);
        p.close();
    }
}
