package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Assert;
import org.junit.Test;

public class BeanDeserializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // -------------------------------------------------------------
    // Helper POJOs for various BeanDeserializer feature branches
    // -------------------------------------------------------------

    public static class SimpleBean {
        public String name;
        public int age;

        public SimpleBean() {}

        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static class ViewBean {
        public interface PublicView {}
        public interface PrivateView extends PublicView {}

        @JsonView(PublicView.class)
        public String publicField;

        @JsonView(PrivateView.class)
        public String privateField;
    }

    public static class CreatorBean {
        public final String name;
        public final int age;
        public String extra;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }
    }

    public static class AnySetterBean {
        public String name;
        private Map<String, Object> any = new HashMap<String, Object>();

        @JsonAnySetter
        public void setAny(String key, Object value) {
            any.put(key, value);
        }

        public Map<String, Object> getAny() {
            return any;
        }
    }

    public static class CreatorAnySetterBean {
        public final String id;
        public Map<String, Object> extra = new HashMap<String, Object>();

        @JsonCreator
        public CreatorAnySetterBean(@JsonProperty("id") String id) {
            this.id = id;
        }

        @JsonAnySetter
        public void addExtra(String key, Object value) {
            extra.put(key, value);
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class IdentifiedBean {
        public int id;
        public String name;
        public IdentifiedBean next;
    }

    public static class UnwrappedContainer {
        public String title;
        @JsonUnwrapped
        public SimpleBean inner;
    }

    public static class CreatorUnwrappedContainer {
        public final String title;
        @JsonUnwrapped
        public final SimpleBean inner;

        @JsonCreator
        public CreatorUnwrappedContainer(@JsonProperty("title") String title, @JsonUnwrapped SimpleBean inner) {
            this.title = title;
            this.inner = inner;
        }
    }

    public static class IgnorableBean {
        public String kept;
        @JsonIgnore
        public String ignored;
    }

    public static class CreatorWithIgnored {
        public final String a;
        @JsonIgnore
        public String ignored;

        @JsonCreator
        public CreatorWithIgnored(@JsonProperty("a") String a) {
            this.a = a;
        }
    }

    static class ExternalTypeContainer {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = StringValue.class, name = "str"),
            @JsonSubTypes.Type(value = IntValue.class, name = "int")
        })
        public Value value;
        public String type;
    }

    interface Value {}

    static class StringValue implements Value {
        public String text;
    }

    static class IntValue implements Value {
        public int number;
    }

    static class CreatorExternalTypeContainer {
        public final Value value;
        public final String type;

        @JsonCreator
        public CreatorExternalTypeContainer(
                @JsonProperty("value")
                @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
                @JsonSubTypes({
                    @JsonSubTypes.Type(value = StringValue.class, name = "str"),
                    @JsonSubTypes.Type(value = IntValue.class, name = "int")
                })
                Value value,
                @JsonProperty("type") String type) {
            this.value = value;
            this.type = type;
        }
    }

    public static class SingleStringCreatorBean {
        public String value;

        @JsonCreator
        public SingleStringCreatorBean(String v) {
            this.value = v;
        }
    }

    public static class SingleIntCreatorBean {
        public int value;

        @JsonCreator
        public SingleIntCreatorBean(int v) {
            this.value = v;
        }
    }

    public static class SingleDoubleCreatorBean {
        public double value;

        @JsonCreator
        public SingleDoubleCreatorBean(double v) {
            this.value = v;
        }
    }

    public static class SingleBooleanCreatorBean {
        public boolean value;

        @JsonCreator
        public SingleBooleanCreatorBean(boolean v) {
            this.value = v;
        }
    }

    public static class ArrayCreatorBean {
        public List<String> items;

        @JsonCreator
        public ArrayCreatorBean(List<String> items) {
            this.items = items;
        }
    }

    public static class SubclassedBeanDeserializer extends BeanDeserializer {
        private static final long serialVersionUID = 1L;

        public SubclassedBeanDeserializer(BeanDeserializerBase src) {
            super(src);
        }

        public SubclassedBeanDeserializer(BeanDeserializerBase src, boolean ignoreAllUnknown) {
            super(src, ignoreAllUnknown);
        }

        public SubclassedBeanDeserializer(BeanDeserializerBase src, NameTransformer unwrapper) {
            super(src, unwrapper);
        }

        public SubclassedBeanDeserializer(BeanDeserializerBase src, ObjectIdReader oir) {
            super(src, oir);
        }

        public SubclassedBeanDeserializer(BeanDeserializerBase src, HashSet<String> ignorableProps) {
            super(src, ignorableProps);
        }
    }

    // -------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------

    @Test
    public void testVanillaDeserialization() throws Exception {
        String json = "{\"name\":\"Bob\",\"age\":30}";
        SimpleBean bean = mapper.readValue(json, SimpleBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("Bob", bean.name);
        Assert.assertEquals(30, bean.age);
    }

    @Test
    public void testVanillaDeserializationEmptyObject() throws Exception {
        SimpleBean bean = mapper.readValue("{}", SimpleBean.class);
        Assert.assertNotNull(bean);
        Assert.assertNull(bean.name);
        Assert.assertEquals(0, bean.age);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void testVanillaUnknownPropertyThrows() throws Exception {
        mapper.readValue("{\"unknown\":123}", SimpleBean.class);
    }

    @Test
    public void testVanillaUnknownPropertyIgnoredWhenConfigured() throws Exception {
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SimpleBean bean = customMapper.readValue("{\"name\":\"Alice\",\"unknown\":123,\"age\":25}", SimpleBean.class);
        Assert.assertEquals("Alice", bean.name);
        Assert.assertEquals(25, bean.age);
    }

    @Test
    public void testUpdatingExistingBean() throws Exception {
        SimpleBean bean = new SimpleBean("Init", 10);
        ObjectReader reader = mapper.readerForUpdating(bean);
        SimpleBean updated = reader.readValue("{\"name\":\"Updated\"}");
        Assert.assertSame(bean, updated);
        Assert.assertEquals("Updated", bean.name);
        Assert.assertEquals(10, bean.age);
    }

    @Test
    public void testUpdatingExistingBeanWithViews() throws Exception {
        ViewBean bean = new ViewBean();
        bean.publicField = "initPublic";
        bean.privateField = "initPrivate";

        ObjectReader reader = mapper.readerForUpdating(bean).withView(ViewBean.PublicView.class);
        ViewBean updated = reader.readValue("{\"publicField\":\"newPublic\",\"privateField\":\"newPrivate\"}");
        Assert.assertSame(bean, updated);
        Assert.assertEquals("newPublic", bean.publicField);
        Assert.assertEquals("initPrivate", bean.privateField);
    }

    @Test
    public void testViewsDeserialization() throws Exception {
        String json = "{\"publicField\":\"pub\",\"privateField\":\"priv\"}";

        ViewBean pubOnly = mapper.readerWithView(ViewBean.PublicView.class)
                .forType(ViewBean.class)
                .readValue(json);
        Assert.assertEquals("pub", pubOnly.publicField);
        Assert.assertNull(pubOnly.privateField);

        ViewBean both = mapper.readerWithView(ViewBean.PrivateView.class)
                .forType(ViewBean.class)
                .readValue(json);
        Assert.assertEquals("pub", both.publicField);
        Assert.assertEquals("priv", both.privateField);
    }

    @Test
    public void testPropertyBasedCreatorDeserialization() throws Exception {
        String json = "{\"extra\":\"hello\",\"name\":\"Charlie\",\"age\":40}";
        CreatorBean bean = mapper.readValue(json, CreatorBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("Charlie", bean.name);
        Assert.assertEquals(40, bean.age);
        Assert.assertEquals("hello", bean.extra);
    }

    @Test
    public void testPropertyBasedCreatorWithIgnoredAndAnySetter() throws Exception {
        String json = "{\"id\":\"id123\",\"extraProp\":\"val\",\"ignored\":\"ignoredVal\"}";
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        CreatorAnySetterBean bean = customMapper.readValue(json, CreatorAnySetterBean.class);
        Assert.assertEquals("id123", bean.id);
        Assert.assertEquals("val", bean.extra.get("extraProp"));
    }

    @Test
    public void testCreatorWithIgnoredField() throws Exception {
        String json = "{\"a\":\"foo\",\"ignored\":\"bar\"}";
        CreatorWithIgnored bean = mapper.readValue(json, CreatorWithIgnored.class);
        Assert.assertEquals("foo", bean.a);
        Assert.assertNull(bean.ignored);
    }

    @Test
    public void testAnySetterDeserialization() throws Exception {
        String json = "{\"name\":\"David\",\"foo\":\"bar\",\"count\":5}";
        AnySetterBean bean = mapper.readValue(json, AnySetterBean.class);
        Assert.assertEquals("David", bean.name);
        Assert.assertEquals("bar", bean.getAny().get("foo"));
        Assert.assertEquals(5, bean.getAny().get("count"));
    }

    @Test
    public void testIdentityInfoDeserialization() throws Exception {
        String json = "{\"id\":1,\"name\":\"Parent\",\"next\":{\"id\":2,\"name\":\"Child\",\"next\":1}}";
        IdentifiedBean parent = mapper.readValue(json, IdentifiedBean.class);
        Assert.assertEquals(1, parent.id);
        Assert.assertEquals("Parent", parent.name);
        Assert.assertNotNull(parent.next);
        Assert.assertEquals(2, parent.next.id);
        Assert.assertSame(parent, parent.next.next);
    }

    @Test
    public void testUnwrappedDeserialization() throws Exception {
        String json = "{\"title\":\"Mr\",\"name\":\"Edward\",\"age\":55}";
        UnwrappedContainer container = mapper.readValue(json, UnwrappedContainer.class);
        Assert.assertEquals("Mr", container.title);
        Assert.assertNotNull(container.inner);
        Assert.assertEquals("Edward", container.inner.name);
        Assert.assertEquals(55, container.inner.age);
    }

    @Test
    public void testCreatorWithUnwrappedDeserialization() throws Exception {
        String json = "{\"title\":\"Dr\",\"name\":\"Frank\",\"age\":60}";
        CreatorUnwrappedContainer container = mapper.readValue(json, CreatorUnwrappedContainer.class);
        Assert.assertEquals("Dr", container.title);
        Assert.assertNotNull(container.inner);
        Assert.assertEquals("Frank", container.inner.name);
        Assert.assertEquals(60, container.inner.age);
    }

    @Test
    public void testExternalTypeIdDeserialization() throws Exception {
        String json = "{\"type\":\"str\",\"value\":{\"text\":\"Sample\"}}";
        ExternalTypeContainer container = mapper.readValue(json, ExternalTypeContainer.class);
        Assert.assertEquals("str", container.type);
        Assert.assertTrue(container.value instanceof StringValue);
        Assert.assertEquals("Sample", ((StringValue) container.value).text);

        String jsonInt = "{\"value\":{\"number\":42},\"type\":\"int\"}";
        ExternalTypeContainer containerInt = mapper.readValue(jsonInt, ExternalTypeContainer.class);
        Assert.assertEquals("int", containerInt.type);
        Assert.assertTrue(containerInt.value instanceof IntValue);
        Assert.assertEquals(42, ((IntValue) containerInt.value).number);
    }

    @Test
    public void testCreatorExternalTypeIdDeserialization() throws Exception {
        String json = "{\"type\":\"str\",\"value\":{\"text\":\"CreatorVal\"}}";
        CreatorExternalTypeContainer container = mapper.readValue(json, CreatorExternalTypeContainer.class);
        Assert.assertEquals("str", container.type);
        Assert.assertTrue(container.value instanceof StringValue);
        Assert.assertEquals("CreatorVal", ((StringValue) container.value).text);
    }

    @Test
    public void testScalarCreatorTypes() throws Exception {
        SingleStringCreatorBean strBean = mapper.readValue("\"hello\"", SingleStringCreatorBean.class);
        Assert.assertEquals("hello", strBean.value);

        SingleIntCreatorBean intBean = mapper.readValue("123", SingleIntCreatorBean.class);
        Assert.assertEquals(123, intBean.value);

        SingleDoubleCreatorBean dblBean = mapper.readValue("12.5", SingleDoubleCreatorBean.class);
        Assert.assertEquals(12.5, dblBean.value, 0.0001);

        SingleBooleanCreatorBean boolBean = mapper.readValue("true", SingleBooleanCreatorBean.class);
        Assert.assertTrue(boolBean.value);

        SingleBooleanCreatorBean boolBeanFalse = mapper.readValue("false", SingleBooleanCreatorBean.class);
        Assert.assertFalse(boolBeanFalse.value);

        ArrayCreatorBean arrBean = mapper.readValue("[\"a\",\"b\"]", ArrayCreatorBean.class);
        Assert.assertEquals(Arrays.asList("a", "b"), arrBean.items);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeOtherThrowsOnUnexpectedToken() throws Exception {
        mapper.readValue("[1,2,3]", SimpleBean.class);
    }

    @Test
    public void testBeanDeserializerMutationMethods() throws Exception {
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = mapper.getDeserializationConfig().findRootValueDeserializer(type);

        if (deser instanceof BeanDeserializer) {
            BeanDeserializer bd = (BeanDeserializer) deser;

            BeanDeserializer withIgnorable = bd.withIgnorableProperties(new HashSet<String>(Arrays.asList("dummy")));
            Assert.assertNotNull(withIgnorable);

            BeanDeserializer withOid = bd.withObjectIdReader(null);
            Assert.assertNotNull(withOid);

            JsonDeserializer<Object> unwrapped = bd.unwrappingDeserializer(NameTransformer.NOP);
            Assert.assertNotNull(unwrapped);

            BeanDeserializerBase asArray = bd.asArrayDeserializer();
            Assert.assertNotNull(asArray);
        }
    }

    @Test
    public void testSubclassedBeanDeserializerUnwrappingReturnsSelf() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = mapper.getDeserializationConfig().findRootValueDeserializer(type);
        if (deser instanceof BeanDeserializer) {
            SubclassedBeanDeserializer sub = new SubclassedBeanDeserializer((BeanDeserializer) deser);
            JsonDeserializer<Object> unwrapped = sub.unwrappingDeserializer(NameTransformer.NOP);
            Assert.assertSame(sub, unwrapped);

            SubclassedBeanDeserializer sub2 = new SubclassedBeanDeserializer((BeanDeserializer) deser, true);
            Assert.assertNotNull(sub2);

            SubclassedBeanDeserializer sub3 = new SubclassedBeanDeserializer((BeanDeserializer) deser, NameTransformer.NOP);
            Assert.assertNotNull(sub3);

            SubclassedBeanDeserializer sub4 = new SubclassedBeanDeserializer((BeanDeserializer) deser, (ObjectIdReader) null);
            Assert.assertNotNull(sub4);

            SubclassedBeanDeserializer sub5 = new SubclassedBeanDeserializer((BeanDeserializer) deser, new HashSet<String>());
            Assert.assertNotNull(sub5);
        }
    }

    @Test
    public void testMissingTokenMethod() {
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonDeserializer<Object> deser = mapper.getDeserializationConfig().findRootValueDeserializer(type);
        if (deser instanceof BeanDeserializer) {
            BeanDeserializer bd = (BeanDeserializer) deser;
            try {
                bd._missingToken(null, mapper.getDeserializationContext());
                Assert.fail("Expected exception");
            } catch (IOException e) {
                // Expected endOfInputException
                Assert.assertNotNull(e);
            }
        }
    }
}
