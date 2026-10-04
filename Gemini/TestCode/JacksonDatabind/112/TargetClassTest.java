package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.impl.NullsConstantProvider;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringCollectionDeserializerTest {

    private static class CustomStringDeser extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return "CUSTOM:" + p.getText();
        }
    }

    private static class DelegatingCollectionHolder {
        public List<String> values;

        public DelegatingCollectionHolder(String single) {
            this.values = new ArrayList<String>();
            this.values.add(single);
        }

        public DelegatingCollectionHolder() {
            this.values = new ArrayList<String>();
        }
    }

    private static class NullAsSkipListHolder {
        @JsonSetter(nulls = Nulls.SKIP, contentNulls = Nulls.SKIP)
        public List<String> list = new ArrayList<String>();
    }

    private static class NullAsEmptyListHolder {
        @JsonSetter(contentNulls = Nulls.AS_EMPTY)
        public List<String> list = new ArrayList<String>();
    }

    @Test
    public void testIsCachable() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        ValueInstantiator vi = new ValueInstantiator.Base(type);
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, vi);
        Assert.assertTrue(deser.isCachable());

        StringCollectionDeserializer deserWithCustom = new StringCollectionDeserializer(type, vi, null, new CustomStringDeser(), null, null);
        Assert.assertFalse(deserWithCustom.isCachable());

        StringCollectionDeserializer deserWithDelegate = new StringCollectionDeserializer(type, vi, new CustomStringDeser(), null, null, null);
        Assert.assertFalse(deserWithDelegate.isCachable());
    }

    @Test
    public void testGetters() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        ValueInstantiator vi = new ValueInstantiator.Base(type);
        CustomStringDeser customDeser = new CustomStringDeser();
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, vi, null, customDeser, null, Boolean.TRUE);

        Assert.assertSame(vi, deser.getValueInstantiator());
        Assert.assertSame(customDeser, deser.getContentDeserializer());
    }

    @Test
    public void testWithResolvedSameInstance() {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        ValueInstantiator vi = new ValueInstantiator.Base(type);
        CustomStringDeser customDeser = new CustomStringDeser();
        NullValueProvider nuller = NullsConstantProvider.nuller();
        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, vi, null, customDeser, nuller, Boolean.TRUE);

        StringCollectionDeserializer resolved = deser.withResolved(null, customDeser, nuller, Boolean.TRUE);
        Assert.assertSame(deser, resolved);

        StringCollectionDeserializer resolvedDiff = deser.withResolved(null, null, nuller, Boolean.TRUE);
        Assert.assertNotSame(deser, resolvedDiff);
    }

    @Test
    public void testBasicDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> result = mapper.readValue("[\"a\", \"b\", null, \"c\"]", new TypeReference<List<String>>() {});
        Assert.assertEquals(4, result.size());
        Assert.assertEquals("a", result.get(0));
        Assert.assertEquals("b", result.get(1));
        Assert.assertNull(result.get(2));
        Assert.assertEquals("c", result.get(3));
    }

    @Test
    public void testBasicDeserializationNonStringTokens() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> result = mapper.readValue("[123, true, 45.6]", new TypeReference<List<String>>() {});
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("123", result.get(0));
        Assert.assertEquals("true", result.get(1));
        Assert.assertEquals("45.6", result.get(2));
    }

    @Test
    public void testSingleValueAsArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        List<String> result = mapper.readValue("\"standalone\"", new TypeReference<List<String>>() {});
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("standalone", result.get(0));

        List<String> nullResult = mapper.readValue("null", new TypeReference<List<String>>() {});
        Assert.assertNull(nullResult);
    }

    @Test(expected = JsonMappingException.class)
    public void testSingleValueAsArrayDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.readValue("\"standalone\"", new TypeReference<List<String>>() {});
    }

    @Test
    public void testSkipNullValuesInArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NullAsSkipListHolder holder = mapper.readValue("{\"list\": [\"a\", null, \"b\"]}", NullAsSkipListHolder.class);
        Assert.assertEquals(2, holder.list.size());
        Assert.assertEquals("a", holder.list.get(0));
        Assert.assertEquals("b", holder.list.get(1));
    }

    @Test
    public void testSkipNullValuesSingleValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        NullAsSkipListHolder holder = mapper.readValue("{\"list\": null}", NullAsSkipListHolder.class);
        Assert.assertEquals(0, holder.list.size());
    }

    @Test
    public void testEmptyNullValuesInArray() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        NullAsEmptyListHolder holder = mapper.readValue("{\"list\": [\"a\", null, \"b\"]}", NullAsEmptyListHolder.class);
        Assert.assertEquals(3, holder.list.size());
        Assert.assertEquals("a", holder.list.get(0));
        Assert.assertEquals("", holder.list.get(1));
        Assert.assertEquals("b", holder.list.get(2));
    }

    @Test
    public void testCustomDeserializerDirectly() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        ValueInstantiator vi = new ValueInstantiator.Base(type) {
            @Override
            public boolean canCreateUsingDefault() {
                return true;
            }
            @Override
            public Object createUsingDefault(DeserializationContext ctxt) {
                return new ArrayList<String>();
            }
        };

        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, vi, null, new CustomStringDeser(), null, Boolean.TRUE);

        JsonParser parser1 = mapper.getFactory().createParser("[\"hello\", 123, null]");
        parser1.nextToken();
        Collection<String> result1 = deser.deserialize(parser1, ctxt);
        Assert.assertEquals(3, result1.size());
        Assert.assertTrue(result1.contains("CUSTOM:hello"));
        Assert.assertTrue(result1.contains("CUSTOM:123"));
        Assert.assertTrue(result1.contains(null));

        JsonParser parser2 = mapper.getFactory().createParser("\"single\"");
        parser2.nextToken();
        Collection<String> result2 = deser.deserialize(parser2, ctxt);
        Assert.assertEquals(1, result2.size());
        Assert.assertTrue(result2.contains("CUSTOM:single"));
    }

    @Test
    public void testDeserializeUsingDelegate() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        ValueInstantiator vi = new ValueInstantiator.Base(type) {
            @Override
            public boolean canCreateUsingDelegate() {
                return true;
            }
            @Override
            public Object createUsingDelegate(DeserializationContext ctxt, Object delegate) {
                List<String> list = new ArrayList<String>();
                list.add("DELEGATED:" + delegate);
                return list;
            }
        };

        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, vi, new CustomStringDeser(), null, null, null);

        JsonParser parser = mapper.getFactory().createParser("\"input\"");
        parser.nextToken();
        Collection<String> result = deser.deserialize(parser, ctxt);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("DELEGATED:CUSTOM:input", result.iterator().next());
    }

    @Test
    public void testContextualResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(new TypeReference<Set<String>>() {});

        ValueInstantiator vi = new ValueInstantiator.Base(type) {
            @Override
            public boolean canCreateUsingDefault() {
                return true;
            }
            @Override
            public Object createUsingDefault(DeserializationContext ctxt) {
                return new HashSet<String>();
            }
        };

        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, vi);
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        Assert.assertNotNull(contextual);
        Assert.assertTrue(contextual instanceof StringCollectionDeserializer);
    }

    @Test
    public void testContextualResolutionWithCustomDeser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(new TypeReference<List<String>>() {});
        ValueInstantiator vi = new ValueInstantiator.Base(type);

        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, vi, null, new CustomStringDeser(), null, null);
        JsonDeserializer<?> contextual = deser.createContextual(ctxt, null);
        Assert.assertNotNull(contextual);
        Assert.assertFalse(contextual.isCachable());
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(new TypeReference<List<String>>() {});
        ValueInstantiator vi = new ValueInstantiator.Base(type) {
            @Override
            public boolean canCreateUsingDefault() {
                return true;
            }
            @Override
            public Object createUsingDefault(DeserializationContext ctxt) {
                return new ArrayList<String>();
            }
        };

        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, vi);
        TypeDeserializer typeDeser = new TypeDeserializer() {
            @Override
            public TypeDeserializer forProperty(BeanProperty prop) {
                return this;
            }
            @Override
            public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() {
                return com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_ARRAY;
            }
            @Override
            public String getPropertyName() {
                return null;
            }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() {
                return null;
            }
            @Override
            public Class<?> getDefaultImpl() {
                return null;
            }
            @Override
            public Object deserializeTypedFromArray(JsonParser p, DeserializationContext ctxt) throws IOException {
                return deser.deserialize(p, ctxt);
            }
            @Override
            public Object deserializeTypedFromObject(JsonParser p, DeserializationContext ctxt) throws IOException {
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
        };

        JsonParser parser = mapper.getFactory().createParser("[\"test\"]");
        parser.nextToken();
        Object obj = deser.deserializeWithType(parser, ctxt, typeDeser);
        Assert.assertTrue(obj instanceof List);
        Assert.assertEquals(1, ((List<?>) obj).size());
    }

    @Test(expected = JsonMappingException.class)
    public void testExceptionWrapping() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        ValueInstantiator vi = new ValueInstantiator.Base(type) {
            @Override
            public boolean canCreateUsingDefault() {
                return true;
            }
            @Override
            public Object createUsingDefault(DeserializationContext ctxt) {
                return new ArrayList<String>() {
                    @Override
                    public boolean add(String s) {
                        throw new RuntimeException("Simulated failure");
                    }
                };
            }
        };

        StringCollectionDeserializer deser = new StringCollectionDeserializer(type, null, vi);
        JsonParser parser = mapper.getFactory().createParser("[\"a\"]");
        parser.nextToken();
        deser.deserialize(parser, ctxt);
    }
}
