package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class BeanDeserializerTest {

    static class SimpleBean {
        public int x;
        public String y;

        public SimpleBean() {}
        public SimpleBean(int x, String y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Views {
        static class Public {}
        static class Internal extends Views.Public {}
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public int pub;

        @JsonView(Views.Internal.class)
        public int priv;
    }

    static class AnySetterBean {
        public int id;
        private Map<String, Object> other = new HashMap<String, Object>();

        @JsonAnySetter
        public void setOther(String name, Object value) {
            other.put(name, value);
        }

        public Map<String, Object> getOther() {
            return other;
        }
    }

    static class IgnoredBean {
        public int id;
        @JsonIgnore
        public String secret;
    }

    static class CreatorBean {
        public final int a;
        public final String b;
        public int c;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") int a, @JsonProperty("b") String b) {
            this.a = a;
            this.b = b;
        }
    }

    static class NullCreatorBean {
        @JsonCreator
        public static NullCreatorBean create(@JsonProperty("val") String val) {
            return null;
        }
    }

    static class InjectBean {
        @JacksonInject("inj")
        public String injected;
        public int id;
    }

    static class UnwrappedLocation {
        public int x;
        public int y;
    }

    static class UnwrappedBean {
        public String name;
        @JsonUnwrapped
        public UnwrappedLocation loc;
    }

    static class UnwrappedCreatorBean {
        public final String name;
        @JsonUnwrapped
        public UnwrappedLocation loc;

        @JsonCreator
        public UnwrappedCreatorBean(@JsonProperty("name") String name) {
            this.name = name;
        }
    }

    static class ExternalTypeBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = StringValue.class, name = "str"),
            @JsonSubTypes.Type(value = IntValue.class, name = "num")
        })
        public Object value;
        public String type;
    }

    static class StringValue {
        public String data;
    }

    static class IntValue {
        public int data;
    }

    static class ExternalTypeCreatorBean {
        public final String extType;
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
        @JsonSubTypes({
            @JsonSubTypes.Type(value = StringValue.class, name = "str")
        })
        public final Object value;

        @JsonCreator
        public ExternalTypeCreatorBean(@JsonProperty("extType") String extType, @JsonProperty("value") Object value) {
            this.extType = extType;
            this.value = value;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdBean {
        public int id;
        public String name;
        public IdBean next;
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class ArrayBean {
        public int a;
        public String b;
    }

    @Test
    public void testVanillaDeserialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("{\"x\":123,\"y\":\"abc\"}", SimpleBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals(123, bean.x);
        Assert.assertEquals("abc", bean.y);
    }

    @Test
    public void testDeserializeUpdateObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean target = new SimpleBean(1, "orig");
        SimpleBean result = mapper.readerForUpdating(target).readValue("{\"x\":99,\"y\":\"updated\"}");
        Assert.assertSame(target, result);
        Assert.assertEquals(99, result.x);
        Assert.assertEquals("updated", result.y);
    }

    @Test
    public void testDeserializeUpdateEmptyObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean target = new SimpleBean(10, "init");
        SimpleBean result = mapper.readerForUpdating(target).readValue("{}");
        Assert.assertSame(target, result);
        Assert.assertEquals(10, result.x);
        Assert.assertEquals("init", result.y);
    }

    @Test
    public void testDeserializeWithViews() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"pub\":10,\"priv\":20}";

        ViewBean publicOnly = mapper.readerWithView(Views.Public.class).forType(ViewBean.class).readValue(json);
        Assert.assertEquals(10, publicOnly.pub);
        Assert.assertEquals(0, publicOnly.priv);

        ViewBean internal = mapper.readerWithView(Views.Internal.class).forType(ViewBean.class).readValue(json);
        Assert.assertEquals(10, internal.pub);
        Assert.assertEquals(20, internal.priv);
    }

    @Test
    public void testDeserializeWithAnySetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterBean bean = mapper.readValue("{\"id\":5,\"extra1\":\"v1\",\"extra2\":100}", AnySetterBean.class);
        Assert.assertEquals(5, bean.id);
        Assert.assertEquals("v1", bean.getOther().get("extra1"));
        Assert.assertEquals(100, bean.getOther().get("extra2"));
    }

    @Test
    public void testDeserializeIgnoredProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoredBean bean = mapper.readValue("{\"id\":7,\"secret\":\"classified\"}", IgnoredBean.class);
        Assert.assertEquals(7, bean.id);
        Assert.assertNull(bean.secret);
    }

    @Test(expected = UnrecognizedPropertyException.class)
    public void testDeserializeUnknownPropertyThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"id\":7,\"unknownProp\":\"val\"}", IgnoredBean.class);
    }

    @Test
    public void testDeserializeUnknownPropertyIgnoredWhenConfigured() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        IgnoredBean bean = mapper.readValue("{\"id\":7,\"unknownProp\":\"val\"}", IgnoredBean.class);
        Assert.assertEquals(7, bean.id);
    }

    @Test
    public void testPropertyBasedCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"a\":10,\"b\":\"foo\",\"c\":20}", CreatorBean.class);
        Assert.assertEquals(10, bean.a);
        Assert.assertEquals("foo", bean.b);
        Assert.assertEquals(20, bean.c);
    }

    @Test(expected = JsonMappingException.class)
    public void testNullFromCreatorThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"val\":\"test\"}", NullCreatorBean.class);
    }

    @Test
    public void testInjectables() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues inject = new InjectableValues.Std().addValue("inj", "injectedValue");
        InjectBean bean = mapper.reader(inject).forType(InjectBean.class).readValue("{\"id\":42}");
        Assert.assertEquals(42, bean.id);
        Assert.assertEquals("injectedValue", bean.injected);
    }

    @Test
    public void testUnwrapped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UnwrappedBean bean = mapper.readValue("{\"name\":\"center\",\"x\":3,\"y\":4}", UnwrappedBean.class);
        Assert.assertEquals("center", bean.name);
        Assert.assertNotNull(bean.loc);
        Assert.assertEquals(3, bean.loc.x);
        Assert.assertEquals(4, bean.loc.y);
    }

    @Test
    public void testUnwrappedWithCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        UnwrappedCreatorBean bean = mapper.readValue("{\"name\":\"orig\",\"x\":15,\"y\":25}", UnwrappedCreatorBean.class);
        Assert.assertEquals("orig", bean.name);
        Assert.assertNotNull(bean.loc);
        Assert.assertEquals(15, bean.loc.x);
        Assert.assertEquals(25, bean.loc.y);
    }

    @Test
    public void testExternalTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ExternalTypeBean bean = mapper.readValue("{\"type\":\"str\",\"value\":{\"data\":\"hello\"}}", ExternalTypeBean.class);
        Assert.assertEquals("str", bean.type);
        Assert.assertTrue(bean.value instanceof StringValue);
        Assert.assertEquals("hello", ((StringValue) bean.value).data);
    }

    @Test
    public void testExternalTypeIdWithCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ExternalTypeCreatorBean bean = mapper.readValue("{\"extType\":\"str\",\"value\":{\"data\":\"world\"}}", ExternalTypeCreatorBean.class);
        Assert.assertEquals("str", bean.extType);
        Assert.assertTrue(bean.value instanceof StringValue);
        Assert.assertEquals("world", ((StringValue) bean.value).data);
    }

    @Test
    public void testObjectIdHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"name\":\"first\",\"next\":{\"id\":2,\"name\":\"second\",\"next\":1}}";
        IdBean b1 = mapper.readValue(json, IdBean.class);
        Assert.assertEquals(1, b1.id);
        Assert.assertNotNull(b1.next);
        Assert.assertEquals(2, b1.next.id);
        Assert.assertSame(b1, b1.next.next);
    }

    @Test
    public void testForwardReferenceHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"next\":1,\"id\":1,\"name\":\"self\"}";
        IdBean b = mapper.readValue(json, IdBean.class);
        Assert.assertEquals(1, b.id);
        Assert.assertSame(b, b.next);
    }

    @Test
    public void testArrayFormat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayBean bean = mapper.readValue("[100,\"test\"]", ArrayBean.class);
        Assert.assertEquals(100, bean.a);
        Assert.assertEquals("test", bean.b);
    }

    @Test(expected = MismatchedInputException.class)
    public void testUnexpectedTokenThrows() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("[1, 2, 3]", SimpleBean.class);
    }

    @Test
    public void testCreatorReturnedNullException() {
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(
                null,
                new DeserializationConfig(
                        new com.fasterxml.jackson.databind.cfg.BaseSettings(null, null, null, null, null, null, null, null, null, null, null),
                        new com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver(),
                        new com.fasterxml.jackson.databind.introspect.SimpleMixInResolver(null),
                        new com.fasterxml.jackson.databind.cfg.ContextAttributes.Impl()
                )
        );
        BeanDescription desc = new com.fasterxml.jackson.databind.introspect.BasicBeanDescription(
                com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(SimpleBean.class)
        );
        BeanDeserializer deser = new BeanDeserializer(
                builder, desc, BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false),
                Collections.<String, SettableBeanProperty>emptyMap(),
                new HashSet<String>(), false, false
        );
        Exception ex = deser._creatorReturnedNullException();
        Assert.assertNotNull(ex);
        Assert.assertTrue(ex instanceof NullPointerException);
        Assert.assertSame(ex, deser._creatorReturnedNullException());
    }

    @Test
    public void testUnwrappingDeserializerRecursionSafety() {
        BeanDeserializerBuilder builder = new BeanDeserializerBuilder(
                null,
                new DeserializationConfig(
                        new com.fasterxml.jackson.databind.cfg.BaseSettings(null, null, null, null, null, null, null, null, null, null, null),
                        new com.fasterxml.jackson.databind.jsontype.impl.StdSubtypeResolver(),
                        new com.fasterxml.jackson.databind.introspect.SimpleMixInResolver(null),
                        new com.fasterxml.jackson.databind.cfg.ContextAttributes.Impl()
                )
        );
        BeanDescription desc = new com.fasterxml.jackson.databind.introspect.BasicBeanDescription(
                com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(SimpleBean.class)
        );
        BeanDeserializer deser = new BeanDeserializer(
                builder, desc, BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false),
                Collections.<String, SettableBeanProperty>emptyMap(),
                new HashSet<String>(), false, false
        );

        NameTransformer transformer = NameTransformer.NOP;
        JsonDeserializer<Object> unwrapped = deser.unwrappingDeserializer(transformer);
        Assert.assertNotNull(unwrapped);
        Assert.assertTrue(unwrapped instanceof BeanDeserializer);

        BeanDeserializer withIgnorable = deser.withIgnorableProperties(Collections.singleton("prop"));
        Assert.assertNotNull(withIgnorable);

        BeanDeserializerBase withProps = deser.withBeanProperties(BeanPropertyMap.construct(Collections.<SettableBeanProperty>emptyList(), false));
        Assert.assertNotNull(withProps);

        BeanDeserializerBase asArray = deser.asArrayDeserializer();
        Assert.assertNotNull(asArray);
    }
}
