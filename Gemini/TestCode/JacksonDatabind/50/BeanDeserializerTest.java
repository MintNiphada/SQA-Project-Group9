package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BeanDeserializerTest {

    static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {
        }

        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    static class Views {
        static class Public {}
        static class Internal extends Public {}
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String pub;

        @JsonView(Views.Internal.class)
        public String priv;
    }

    static class IgnorableBean {
        public String a;
        public String b;
    }

    static class AnySetterBean {
        public String a;
        private Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public void setExtra(String key, Object value) {
            extra.put(key, value);
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    static class CreatorBean {
        public final String x;
        public final int y;
        public String z;

        @JsonCreator
        public CreatorBean(@JsonProperty("x") String x, @JsonProperty("y") int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class NullCreatorBean {
        @JsonCreator
        public static NullCreatorBean make(@JsonProperty("name") String name) {
            return null;
        }
    }

    static class UnwrappedLocation {
        public double lat;
        public double lon;
    }

    static class PlaceBean {
        public String name;
        @JsonUnwrapped
        public UnwrappedLocation location;
    }

    static class PlaceWithCreator {
        public String name;
        @JsonUnwrapped
        public UnwrappedLocation location;

        @JsonCreator
        public PlaceWithCreator(@JsonProperty("name") String name, @JsonProperty("location") UnwrappedLocation location) {
            this.name = name;
            this.location = location;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdBean {
        public int id;
        public String name;
        public IdBean next;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayFormatBean {
        public String a;
        public int b;

        public ArrayFormatBean() {}
        public ArrayFormatBean(String a, int b) {
            this.a = a;
            this.b = b;
        }
    }

    static class ExtTypeBean {
        public String type;
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Object payload;
    }

    static class ExtTypeCreatorBean {
        public String type;
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Object payload;

        @JsonCreator
        public ExtTypeCreatorBean(@JsonProperty("type") String type, @JsonProperty("payload") Object payload) {
            this.type = type;
            this.payload = payload;
        }
    }

    @Test
    public void testVanillaDeserialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", SimpleBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("Alice", bean.name);
        Assert.assertEquals(30, bean.age);
    }

    @Test
    public void testDeserializeIntoExistingBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean("Bob", 20);
        ObjectReader reader = mapper.readerForUpdating(bean);
        SimpleBean result = reader.readValue("{\"age\":25}");
        Assert.assertSame(bean, result);
        Assert.assertEquals("Bob", result.name);
        Assert.assertEquals(25, result.age);
    }

    @Test
    public void testDeserializeIntoExistingBeanEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean("Bob", 20);
        ObjectReader reader = mapper.readerForUpdating(bean);
        SimpleBean result = reader.readValue("{}");
        Assert.assertSame(bean, result);
        Assert.assertEquals("Bob", result.name);
        Assert.assertEquals(20, result.age);
    }

    @Test
    public void testDeserializeWithViews() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"pub\":\"publicVal\",\"priv\":\"privateVal\"}";

        ViewBean publicResult = mapper.readerWithView(Views.Public.class).forType(ViewBean.class).readValue(json);
        Assert.assertEquals("publicVal", publicResult.pub);
        Assert.assertNull(publicResult.priv);

        ViewBean internalResult = mapper.readerWithView(Views.Internal.class).forType(ViewBean.class).readValue(json);
        Assert.assertEquals("publicVal", internalResult.pub);
        Assert.assertEquals("privateVal", internalResult.priv);
    }

    @Test
    public void testDeserializeWithViewsOnExisting() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ViewBean bean = new ViewBean();
        String json = "{\"pub\":\"publicVal\",\"priv\":\"privateVal\"}";

        ViewBean result = mapper.readerWithView(Views.Public.class).forType(ViewBean.class).withValueToUpdate(bean).readValue(json);
        Assert.assertEquals("publicVal", result.pub);
        Assert.assertNull(result.priv);
    }

    @Test
    public void testDeserializePropertyBasedCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"y\":42,\"x\":\"hello\",\"z\":\"world\"}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);
        Assert.assertEquals("hello", bean.x);
        Assert.assertEquals(42, bean.y);
        Assert.assertEquals("world", bean.z);
    }

    @Test
    public void testDeserializePropertyBasedWithAnySetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"a\":\"valA\",\"other1\":123,\"other2\":\"text\"}";
        AnySetterBean bean = mapper.readValue(json, AnySetterBean.class);
        Assert.assertEquals("valA", bean.a);
        Assert.assertEquals(123, bean.getExtra().get("other1"));
        Assert.assertEquals("text", bean.getExtra().get("other2"));
    }

    @Test
    public void testDeserializeWithUnwrapped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"Tower\",\"lat\":51.5,\"lon\":-0.08}";
        PlaceBean bean = mapper.readValue(json, PlaceBean.class);
        Assert.assertEquals("Tower", bean.name);
        Assert.assertNotNull(bean.location);
        Assert.assertEquals(51.5, bean.location.lat, 0.001);
        Assert.assertEquals(-0.08, bean.location.lon, 0.001);
    }

    @Test
    public void testDeserializeWithUnwrappedExisting() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PlaceBean bean = new PlaceBean();
        String json = "{\"name\":\"Tower\",\"lat\":51.5,\"lon\":-0.08}";
        PlaceBean result = mapper.readerForUpdating(bean).readValue(json);
        Assert.assertSame(bean, result);
        Assert.assertEquals("Tower", result.name);
        Assert.assertNotNull(result.location);
        Assert.assertEquals(51.5, result.location.lat, 0.001);
        Assert.assertEquals(-0.08, result.location.lon, 0.001);
    }

    @Test
    public void testDeserializeWithObjectId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"name\":\"first\",\"next\":1}";
        IdBean bean = mapper.readValue(json, IdBean.class);
        Assert.assertEquals(1, bean.id);
        Assert.assertEquals("first", bean.name);
        Assert.assertSame(bean, bean.next);
    }

    @Test
    public void testAsArrayDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "[\"abc\",123]";
        ArrayFormatBean bean = mapper.readValue(json, ArrayFormatBean.class);
        Assert.assertEquals("abc", bean.a);
        Assert.assertEquals(123, bean.b);
    }

    @Test
    public void testDeserializeExternalTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SimpleBean.class);
        String json = "{\"type\":\"BeanDeserializerTest$SimpleBean\",\"payload\":{\"name\":\"Sub\",\"age\":10}}";
        ExtTypeBean bean = mapper.readValue(json, ExtTypeBean.class);
        Assert.assertEquals("BeanDeserializerTest$SimpleBean", bean.type);
        Assert.assertTrue(bean.payload instanceof SimpleBean);
        Assert.assertEquals("Sub", ((SimpleBean) bean.payload).name);
    }

    @Test
    public void testDeserializeExternalTypeIdCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerSubtypes(SimpleBean.class);
        String json = "{\"type\":\"BeanDeserializerTest$SimpleBean\",\"payload\":{\"name\":\"Sub2\",\"age\":11}}";
        ExtTypeCreatorBean bean = mapper.readValue(json, ExtTypeCreatorBean.class);
        Assert.assertEquals("BeanDeserializerTest$SimpleBean", bean.type);
        Assert.assertTrue(bean.payload instanceof SimpleBean);
        Assert.assertEquals("Sub2", ((SimpleBean) bean.payload).name);
    }

    @Test(expected = JsonMappingException.class)
    public void testNullCreatorThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"name\":\"test\"}", NullCreatorBean.class);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void testUnknownPropertyFail() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"unknown\":123}", SimpleBean.class);
    }

    @Test
    public void testUnknownPropertyIgnoredWhenConfigured() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleBean bean = mapper.readValue("{\"unknown\":123,\"name\":\"Bob\"}", SimpleBean.class);
        Assert.assertEquals("Bob", bean.name);
    }

    @Test
    public void testNullFromCreatorExceptionCreation() {
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(null, null);
        BeanDeserializer bd = new BeanDeserializer(builder, null, BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false), null, null, false, false);
        Exception e1 = bd._creatorReturnedNullException();
        Assert.assertNotNull(e1);
        Exception e2 = bd._creatorReturnedNullException();
        Assert.assertSame(e1, e2);
    }

    @Test
    public void testCopyMutators() {
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(null, null);
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        BeanDeserializer bd = new BeanDeserializer(builder, null, map, null, null, false, false);

        BeanDeserializer copy1 = bd.withBeanProperties(map);
        Assert.assertNotNull(copy1);
        Assert.assertNotSame(bd, copy1);

        Set<String> ign = new HashSet<String>();
        ign.add("test");
        BeanDeserializer copy2 = bd.withIgnorableProperties(ign);
        Assert.assertNotNull(copy2);
        Assert.assertNotSame(bd, copy2);

        JsonDeserializer<Object> unwrapped = bd.unwrappingDeserializer(NameTransformer.NOP);
        Assert.assertNotNull(unwrapped);
        Assert.assertTrue(unwrapped instanceof BeanDeserializer);
    }

    @Test
    public void testUnwrappingDeserializerSubclassReturnThis() {
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(null, null);
        BeanPropertyMap map = BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false);
        BeanDeserializer subclass = new BeanDeserializer(builder, null, map, null, null, false, false) {
            private static final long serialVersionUID = 1L;
        };
        JsonDeserializer<Object> result = subclass.unwrappingDeserializer(NameTransformer.NOP);
        Assert.assertSame(subclass, result);
    }

    @Test
    public void testDeserializeOtherTokensUnexpected() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("[1, 2, 3]", SimpleBean.class);
            Assert.fail("Should have failed on array token");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("START_ARRAY"));
        }
    }

    @Test
    public void testDeserializeStringTokenUnexpected() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("\"a string\"", SimpleBean.class);
            Assert.fail("Should have failed on string token");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("VALUE_STRING"));
        }
    }

    @Test
    public void testDeserializeNumberTokenUnexpected() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("123.45", SimpleBean.class);
            Assert.fail("Should have failed on float token");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("VALUE_NUMBER_FLOAT"));
        }
    }

    @Test
    public void testDeserializeBooleanTokenUnexpected() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("true", SimpleBean.class);
            Assert.fail("Should have failed on boolean token");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("VALUE_TRUE"));
        }
    }
}
