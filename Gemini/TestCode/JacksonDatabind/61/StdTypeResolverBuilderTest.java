package com.fasterxml.jackson.databind.jsontype.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdTypeResolverBuilderTest {

    private ObjectMapper mapper;
    private SerializationConfig serializationConfig;
    private DeserializationConfig deserializationConfig;
    private JavaType baseType;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        serializationConfig = mapper.getSerializationConfig();
        deserializationConfig = mapper.getDeserializationConfig();
        baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
    }

    @Test
    public void testNoTypeInfoBuilder() {
        StdTypeResolverBuilder builder = StdTypeResolverBuilder.noTypeInfoBuilder();
        Assert.assertNotNull(builder);
        Assert.assertNull(builder.buildTypeSerializer(serializationConfig, baseType, null));
        Assert.assertNull(builder.buildTypeDeserializer(deserializationConfig, baseType, null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInitWithNullId() {
        new StdTypeResolverBuilder().init(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInclusionNull() {
        new StdTypeResolverBuilder().inclusion(null);
    }

    @Test
    public void testTypePropertyDefaultsAndCustom() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        Assert.assertEquals("@class", builder.getTypeProperty());

        builder.typeProperty("customProp");
        Assert.assertEquals("customProp", builder.getTypeProperty());

        builder.typeProperty("");
        Assert.assertEquals("@class", builder.getTypeProperty());

        builder.typeProperty(null);
        Assert.assertEquals("@class", builder.getTypeProperty());
    }

    @Test
    public void testAccessorsAndMutators() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.NAME, null);
        Assert.assertFalse(builder.isTypeIdVisible());
        Assert.assertNull(builder.getDefaultImpl());

        builder.typeIdVisibility(true);
        Assert.assertTrue(builder.isTypeIdVisible());

        builder.defaultImpl(String.class);
        Assert.assertEquals(String.class, builder.getDefaultImpl());
    }

    @Test
    public void testBuildTypeSerializerAllInclusions() {
        JsonTypeInfo.As[] inclusions = new JsonTypeInfo.As[] {
            JsonTypeInfo.As.WRAPPER_ARRAY,
            JsonTypeInfo.As.PROPERTY,
            JsonTypeInfo.As.WRAPPER_OBJECT,
            JsonTypeInfo.As.EXTERNAL_PROPERTY,
            JsonTypeInfo.As.EXISTING_PROPERTY
        };

        for (JsonTypeInfo.As incl : inclusions) {
            StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
            builder.init(JsonTypeInfo.Id.CLASS, null);
            builder.inclusion(incl);
            TypeSerializer serializer = builder.buildTypeSerializer(serializationConfig, baseType, null);
            Assert.assertNotNull(serializer);
        }
    }

    @Test
    public void testBuildTypeDeserializerAllInclusions() {
        JsonTypeInfo.As[] inclusions = new JsonTypeInfo.As[] {
            JsonTypeInfo.As.WRAPPER_ARRAY,
            JsonTypeInfo.As.PROPERTY,
            JsonTypeInfo.As.WRAPPER_OBJECT,
            JsonTypeInfo.As.EXTERNAL_PROPERTY,
            JsonTypeInfo.As.EXISTING_PROPERTY
        };

        for (JsonTypeInfo.As incl : inclusions) {
            StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
            builder.init(JsonTypeInfo.Id.CLASS, null);
            builder.inclusion(incl);
            TypeDeserializer deserializer = builder.buildTypeDeserializer(deserializationConfig, baseType, null);
            Assert.assertNotNull(deserializer);
        }
    }

    @Test
    public void testBuildTypeDeserializerWithDefaultImplVoidAndNoClass() {
        StdTypeResolverBuilder builder1 = new StdTypeResolverBuilder();
        builder1.init(JsonTypeInfo.Id.CLASS, null);
        builder1.inclusion(JsonTypeInfo.As.PROPERTY);
        builder1.defaultImpl(Void.class);
        TypeDeserializer deser1 = builder1.buildTypeDeserializer(deserializationConfig, baseType, null);
        Assert.assertNotNull(deser1);
        Assert.assertEquals(Void.class, deser1.getDefaultImpl());

        StdTypeResolverBuilder builder2 = new StdTypeResolverBuilder();
        builder2.init(JsonTypeInfo.Id.CLASS, null);
        builder2.inclusion(JsonTypeInfo.As.PROPERTY);
        builder2.defaultImpl(NoClass.class);
        TypeDeserializer deser2 = builder2.buildTypeDeserializer(deserializationConfig, baseType, null);
        Assert.assertNotNull(deser2);
        Assert.assertEquals(NoClass.class, deser2.getDefaultImpl());

        StdTypeResolverBuilder builder3 = new StdTypeResolverBuilder();
        builder3.init(JsonTypeInfo.Id.CLASS, null);
        builder3.inclusion(JsonTypeInfo.As.PROPERTY);
        builder3.defaultImpl(String.class);
        TypeDeserializer deser3 = builder3.buildTypeDeserializer(deserializationConfig, baseType, null);
        Assert.assertNotNull(deser3);
        Assert.assertEquals(String.class, deser3.getDefaultImpl());
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildTypeSerializerWithoutInclusion() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.buildTypeSerializer(serializationConfig, baseType, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testBuildTypeDeserializerWithoutInclusion() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CLASS, null);
        builder.buildTypeDeserializer(deserializationConfig, baseType, null);
    }

    @Test(expected = IllegalStateException.class)
    public void testIdResolverWithoutInit() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.idResolver(serializationConfig, baseType, null, true, false);
    }

    @Test
    public void testIdResolverWithCustomResolver() {
        TypeIdResolver custom = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, custom);
        builder.inclusion(JsonTypeInfo.As.PROPERTY);
        TypeIdResolver resolved = builder.idResolver(serializationConfig, baseType, null, true, false);
        Assert.assertSame(custom, resolved);
    }

    @Test(expected = IllegalStateException.class)
    public void testIdResolverWithCustomWithoutInstance() {
        StdTypeResolverBuilder builder = new StdTypeResolverBuilder();
        builder.init(JsonTypeInfo.Id.CUSTOM, null);
        builder.idResolver(serializationConfig, baseType, null, true, false);
    }

    @Test
    public void testIdResolverStandardTypes() {
        List<NamedType> subtypes = new ArrayList<NamedType>();
        subtypes.add(new NamedType(String.class, "str"));

        StdTypeResolverBuilder bClass = new StdTypeResolverBuilder().init(JsonTypeInfo.Id.CLASS, null);
        Assert.assertTrue(bClass.idResolver(serializationConfig, baseType, subtypes, true, false) instanceof ClassNameIdResolver);

        StdTypeResolverBuilder bMinClass = new StdTypeResolverBuilder().init(JsonTypeInfo.Id.MINIMAL_CLASS, null);
        Assert.assertTrue(bMinClass.idResolver(serializationConfig, baseType, subtypes, true, false) instanceof MinimalClassNameIdResolver);

        StdTypeResolverBuilder bName = new StdTypeResolverBuilder().init(JsonTypeInfo.Id.NAME, null);
        Assert.assertTrue(bName.idResolver(serializationConfig, baseType, subtypes, true, false) instanceof TypeNameIdResolver);

        StdTypeResolverBuilder bNone = new StdTypeResolverBuilder().init(JsonTypeInfo.Id.NONE, null);
        Assert.assertNull(bNone.idResolver(serializationConfig, baseType, subtypes, true, false));
    }
}
