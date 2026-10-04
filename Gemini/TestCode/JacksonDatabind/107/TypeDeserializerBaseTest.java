package com.fasterxml.jackson.databind.jsontype.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

public class TypeDeserializerBaseTest {

    static class DummyTypeDeserializer extends TypeDeserializerBase {
        private static final long serialVersionUID = 1L;

        public DummyTypeDeserializer(JavaType baseType, TypeIdResolver idRes,
                                     String typePropertyName, boolean typeIdVisible, JavaType defaultImpl) {
            super(baseType, idRes, typePropertyName, typeIdVisible, defaultImpl);
        }

        public DummyTypeDeserializer(DummyTypeDeserializer src, BeanProperty prop) {
            super(src, prop);
        }

        @Override
        public TypeDeserializer forProperty(BeanProperty prop) {
            return new DummyTypeDeserializer(this, prop);
        }

        @Override
        public JsonTypeInfo.As getTypeInclusion() {
            return JsonTypeInfo.As.PROPERTY;
        }

        @Override
        public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromScalar(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        @Override
        public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
            return null;
        }

        public JsonDeserializer<Object> testFindDeserializer(DeserializationContext ctxt, String typeId) throws IOException {
            return _findDeserializer(ctxt, typeId);
        }

        public JsonDeserializer<Object> testFindDefaultImplDeserializer(DeserializationContext ctxt) throws IOException {
            return _findDefaultImplDeserializer(ctxt);
        }

        public Object testDeserializeWithNativeTypeId(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return _deserializeWithNativeTypeId(jp, ctxt);
        }

        public Object testDeserializeWithNativeTypeId(JsonParser jp, DeserializationContext ctxt, Object typeId) throws IOException {
            return _deserializeWithNativeTypeId(jp, ctxt, typeId);
        }

        public JavaType testHandleUnknownTypeId(DeserializationContext ctxt, String typeId) throws IOException {
            return _handleUnknownTypeId(ctxt, typeId);
        }

        public JavaType testHandleMissingTypeId(DeserializationContext ctxt, String extraDesc) throws IOException {
            return _handleMissingTypeId(ctxt, extraDesc);
        }
    }

    static class MockTypeIdResolver implements TypeIdResolver {
        private JavaType baseType;
        private String knownTypeIds;
        private JavaType typeToReturn;

        public MockTypeIdResolver(JavaType baseType) {
            this.baseType = baseType;
        }

        @Override
        public void init(JavaType bt) {
            this.baseType = bt;
        }

        @Override
        public String idFromValue(Object value) {
            return null;
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return null;
        }

        @Override
        public String idFromBaseType() {
            return null;
        }

        @Override
        public JavaType typeFromId(DeserializationContext context, String id) {
            if ("custom".equals(id)) {
                return typeToReturn;
            }
            if ("string".equals(id)) {
                return TypeFactory.defaultInstance().constructType(String.class);
            }
            if ("list".equals(id)) {
                return TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
            }
            if ("rawList".equals(id)) {
                return TypeFactory.defaultInstance().constructType(ArrayList.class);
            }
            return null;
        }

        @Override
        public String getDescForKnownTypeIds() {
            return knownTypeIds;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CUSTOM;
        }
    }

    @Test
    public void testGettersAndToString() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);

        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, null, true, defaultImpl);

        Assert.assertEquals(CharSequence.class.getName(), deser.baseTypeName());
        Assert.assertEquals("", deser.getPropertyName());
        Assert.assertSame(idRes, deser.getTypeIdResolver());
        Assert.assertEquals(String.class, deser.getDefaultImpl());
        Assert.assertSame(baseType, deser.baseType());
        Assert.assertEquals(JsonTypeInfo.As.PROPERTY, deser.getTypeInclusion());

        String str = deser.toString();
        Assert.assertTrue(str.contains(DummyTypeDeserializer.class.getName()));
        Assert.assertTrue(str.contains("base-type:"));
        Assert.assertTrue(str.contains("id-resolver:"));
    }

    @Test
    public void testGetDefaultImplNull() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        Assert.assertNull(deser.getDefaultImpl());
    }

    @Test
    public void testFindDeserializerResolvedAndCached() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<Object> d1 = deser.testFindDeserializer(ctxt, "string");
        Assert.assertNotNull(d1);

        JsonDeserializer<Object> d2 = deser.testFindDeserializer(ctxt, "string");
        Assert.assertSame(d1, d2);
    }

    @Test
    public void testFindDeserializerSpecializedType() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<Object> d = deser.testFindDeserializer(ctxt, "rawList");
        Assert.assertNotNull(d);
    }

    @Test
    public void testFindDeserializerGenericTypeRetained() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructCollectionType(List.class, Object.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        JavaType fullGeneric = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        idRes.typeToReturn = fullGeneric;
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<Object> d = deser.testFindDeserializer(ctxt, "custom");
        Assert.assertNotNull(d);
    }

    @Test
    public void testFindDefaultImplDeserializerNullDefaultImpl() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<Object> d = deser.testFindDefaultImplDeserializer(ctxt);
        Assert.assertSame(NullifyingDeserializer.instance, d);

        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true);
        ctxt = mapper.getDeserializationContext();
        JsonDeserializer<Object> d2 = deser.testFindDefaultImplDeserializer(ctxt);
        Assert.assertNull(d2);
    }

    @Test
    public void testFindDefaultImplDeserializerBogusClass() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        JavaType bogusType = TypeFactory.defaultInstance().constructType(Void.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, bogusType);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<Object> d = deser.testFindDefaultImplDeserializer(ctxt);
        Assert.assertSame(NullifyingDeserializer.instance, d);
    }

    @Test
    public void testFindDefaultImplDeserializerValid() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, defaultImpl);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JsonDeserializer<Object> d = deser.testFindDefaultImplDeserializer(ctxt);
        Assert.assertNotNull(d);
        JsonDeserializer<Object> d2 = deser.testFindDefaultImplDeserializer(ctxt);
        Assert.assertSame(d, d2);
    }

    @Test
    public void testCopyConstructor() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "typeProp", true, defaultImpl);

        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        DummyTypeDeserializer copy = (DummyTypeDeserializer) deser.forProperty(prop);

        Assert.assertSame(baseType, copy.baseType());
        Assert.assertSame(idRes, copy.getTypeIdResolver());
        Assert.assertEquals("typeProp", copy.getPropertyName());
        Assert.assertEquals(String.class, copy.getDefaultImpl());
    }

    @Test(expected = JsonMappingException.class)
    public void testHandleUnknownTypeIdWithoutProperty() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        idRes.knownTypeIds = null;
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        deser.testHandleUnknownTypeId(ctxt, "unknownId");
    }

    @Test(expected = JsonMappingException.class)
    public void testHandleUnknownTypeIdWithPropertyAndKnownIds() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        idRes.knownTypeIds = "a, b";
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);
        BeanProperty.Bogus prop = new BeanProperty.Bogus();
        DummyTypeDeserializer deserWithProp = (DummyTypeDeserializer) deser.forProperty(prop);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        deserWithProp.testHandleUnknownTypeId(ctxt, "unknownId");
    }

    @Test(expected = JsonMappingException.class)
    public void testHandleMissingTypeId() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        deser.testHandleMissingTypeId(ctxt, "extra");
    }

    @Test
    public void testDeserializeWithNativeTypeId() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.createParser("\"hello\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object res = deser.testDeserializeWithNativeTypeId(p, ctxt, "string");
        Assert.assertEquals("hello", res);
        p.close();
    }

    @Test
    public void testDeserializeWithNativeTypeIdNonString() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.createParser("\"hello\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object res = deser.testDeserializeWithNativeTypeId(p, ctxt, new StringBuilder("string"));
        Assert.assertEquals("hello", res);
        p.close();
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeWithNativeTypeIdNullNoDefaultImpl() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, null);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, true);
        JsonParser p = mapper.createParser("\"hello\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            deser.testDeserializeWithNativeTypeId(p, ctxt, null);
        } finally {
            p.close();
        }
    }

    @Test
    public void testDeserializeWithNativeTypeIdNullWithDefaultImpl() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, defaultImpl);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.createParser("\"hello\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object res = deser.testDeserializeWithNativeTypeId(p, ctxt, null);
        Assert.assertEquals("hello", res);
        p.close();
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDeserializeWithNativeTypeIdDeprecatedMethod() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType defaultImpl = TypeFactory.defaultInstance().constructType(String.class);
        MockTypeIdResolver idRes = new MockTypeIdResolver(baseType);
        DummyTypeDeserializer deser = new DummyTypeDeserializer(baseType, idRes, "type", false, defaultImpl);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.createParser("\"hello\"");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        Object res = deser.testDeserializeWithNativeTypeId(p, ctxt);
        Assert.assertEquals("hello", res);
        p.close();
    }
}
