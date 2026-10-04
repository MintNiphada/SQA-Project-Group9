package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.*;

public class BeanDeserializerFactoryTest {

    static class SimpleBean {
        public String name;
        public int age;
    }

    static class CustomDeserBean {
        public String value;
    }

    static class CustomBeanDeserializer extends StdDeserializer<CustomDeserBean> {
        public CustomBeanDeserializer() {
            super(CustomDeserBean.class);
        }
        @Override
        public CustomDeserBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
            CustomDeserBean b = new CustomDeserBean();
            b.value = "custom";
            return b;
        }
    }

    static class DummyException extends Exception {
        private static final long serialVersionUID = 1L;
        public DummyException() { super(); }
        public DummyException(String msg) { super(msg); }
    }

    static abstract class AbstractTypeTarget {
        public int x;
    }

    static class ConcreteTarget extends AbstractTypeTarget {
        public ConcreteTarget() {}
    }

    @JsonDeserialize(builder = SimplePOJOBuilder.class)
    static class BuiltBean {
        private final int x;
        public BuiltBean(int x) { this.x = x; }
        public int getX() { return x; }
    }

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "with")
    static class SimplePOJOBuilder {
        private int x;
        public SimplePOJOBuilder withX(int x) { this.x = x; return this; }
        public BuiltBean create() { return new BuiltBean(x); }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdPropBean {
        public int id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class IdGenBean {
        public int value;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "missingId")
    static class BadIdBean {
        public int realId;
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredBean {
        public String ignoredField;
        public String keptField;
    }

    static class AnySetterBean {
        private Map<String, Object> map = new HashMap<String, Object>();
        @JsonAnySetter
        public void setAny(String key, Object value) {
            map.put(key, value);
        }
        public Map<String, Object> getMap() { return map; }
    }

    static class GetterSetterlessBean {
        private List<String> list = new ArrayList<String>();
        public List<String> getList() { return list; }
    }

    static class CreatorBean {
        private final String name;
        private final int age;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("age") int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }
    }

    static class ParentNode {
        @JsonManagedReference
        public ChildNode child;
    }

    static class ChildNode {
        @JsonBackReference
        public ParentNode parent;
    }

    static class InjectBean {
        @JacksonInject("injectedVal")
        public String val;
    }

    @JsonIgnoreType
    static class IgnorablePayload {
        public String dummy;
    }

    static class BeanWithIgnorableType {
        public IgnorablePayload ignorable;
        public String normal;
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String publicField;
        public String defaultField;
    }

    interface Views {
        interface Public {}
    }

    static class CustomFactorySubclass extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;
        public CustomFactorySubclass(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    @Test
    public void testSingletonAndWithConfig() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        Assert.assertNotNull(factory);
        DeserializerFactoryConfig config = factory.getFactoryConfig();
        Assert.assertSame(factory, factory.withConfig(config));

        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(newConfig);
        Assert.assertNotSame(factory, newFactory);
        Assert.assertSame(newConfig, newFactory.getFactoryConfig());
    }

    @Test(expected = IllegalStateException.class)
    public void testSubclassWithConfigThrows() {
        CustomFactorySubclass custom = new CustomFactorySubclass(new DeserializerFactoryConfig());
        custom.withConfig(new DeserializerFactoryConfig());
    }

    @Test
    public void testCreateSimpleBeanDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof BeanDeserializer);

        SimpleBean bean = mapper.readValue("{\"name\":\"Alice\",\"age\":30}", SimpleBean.class);
        Assert.assertEquals("Alice", bean.name);
        Assert.assertEquals(30, bean.age);
    }

    @Test
    public void testCustomBeanDeserializerModifier() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(CustomDeserBean.class, new CustomBeanDeserializer());
        mapper.registerModule(mod);

        CustomDeserBean bean = mapper.readValue("{\"value\":\"something\"}", CustomDeserBean.class);
        Assert.assertEquals("custom", bean.value);
    }

    @Test
    public void testThrowableDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(DummyException.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof ThrowableDeserializer);

        DummyException ex = mapper.readValue("{\"message\":\"boom!\"}", DummyException.class);
        Assert.assertEquals("boom!", ex.getMessage());
    }

    @Test
    public void testMaterializeAbstractType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        mod.addAbstractTypeMapping(AbstractTypeTarget.class, ConcreteTarget.class);
        mapper.registerModule(mod);

        AbstractTypeTarget obj = mapper.readValue("{\"x\":42}", AbstractTypeTarget.class);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof ConcreteTarget);
        Assert.assertEquals(42, obj.x);
    }

    @Test
    public void testBuilderBasedDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BuiltBean bean = mapper.readValue("{\"x\":99}", BuiltBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals(99, bean.getX());
    }

    @Test
    public void testObjectIdReader() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IdPropBean bean = mapper.readValue("{\"id\":123,\"name\":\"Test\"}", IdPropBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals(123, bean.id);
        Assert.assertEquals("Test", bean.name);

        IdGenBean idGen = mapper.readValue("{\"@id\":1,\"value\":50}", IdGenBean.class);
        Assert.assertNotNull(idGen);
        Assert.assertEquals(50, idGen.value);
    }

    @Test(expected = JsonMappingException.class)
    public void testInvalidObjectIdDefinition() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"realId\":10}", BadIdBean.class);
    }

    @Test
    public void testIgnoredAndAnySetterProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoredBean ignored = mapper.readValue("{\"ignoredField\":\"foo\",\"keptField\":\"bar\"}", IgnoredBean.class);
        Assert.assertNull(ignored.ignoredField);
        Assert.assertEquals("bar", ignored.keptField);

        AnySetterBean anyBean = mapper.readValue("{\"propA\":\"valA\",\"propB\":123}", AnySetterBean.class);
        Assert.assertNotNull(anyBean.getMap());
        Assert.assertEquals("valA", anyBean.getMap().get("propA"));
        Assert.assertEquals(123, anyBean.getMap().get("propB"));
    }

    @Test
    public void testSetterlessCollectionProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        GetterSetterlessBean bean = mapper.readValue("{\"list\":[\"a\",\"b\",\"c\"]}", GetterSetterlessBean.class);
        Assert.assertNotNull(bean.getList());
        Assert.assertEquals(3, bean.getList().size());
        Assert.assertTrue(bean.getList().contains("b"));
    }

    @Test
    public void testCreatorPropertyDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"name\":\"Bob\",\"age\":25}", CreatorBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("Bob", bean.getName());
        Assert.assertEquals(25, bean.getAge());
    }

    @Test
    public void testManagedAndBackReferences() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"child\":{}}";
        ParentNode parent = mapper.readValue(json, ParentNode.class);
        Assert.assertNotNull(parent);
        Assert.assertNotNull(parent.child);
        Assert.assertSame(parent, parent.child.parent);
    }

    @Test
    public void testInjectableProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std inject = new InjectableValues.Std();
        inject.addValue("injectedVal", "injectedResult");
        InjectBean bean = mapper.reader(inject).forType(InjectBean.class).readValue("{}");
        Assert.assertNotNull(bean);
        Assert.assertEquals("injectedResult", bean.val);
    }

    @Test
    public void testIgnorableType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanWithIgnorableType bean = mapper.readValue("{\"ignorable\":{\"dummy\":\"ignored\"},\"normal\":\"keep\"}", BeanWithIgnorableType.class);
        Assert.assertNotNull(bean);
        Assert.assertNull(bean.ignorable);
        Assert.assertEquals("keep", bean.normal);
    }

    @Test
    public void testViewsConfiguration() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        String json = "{\"publicField\":\"pub\",\"defaultField\":\"def\"}";
        ViewBean bean = mapper.readerWithView(Views.Public.class).forType(ViewBean.class).readValue(json);
        Assert.assertNotNull(bean);
        Assert.assertEquals("pub", bean.publicField);
        Assert.assertNull(bean.defaultField);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypePrimitive() {
        BeanDeserializerFactory.instance.isPotentialBeanType(int.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeArray() {
        BeanDeserializerFactory.instance.isPotentialBeanType(String[].class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeProxy() {
        Class<?> proxyClass = Proxy.getProxyClass(getClass().getClassLoader(), Runnable.class);
        BeanDeserializerFactory.instance.isPotentialBeanType(proxyClass);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeLocalClass() {
        class LocalInnerClass {}
        BeanDeserializerFactory.instance.isPotentialBeanType(LocalInnerClass.class);
    }

    @Test
    public void testDeserializerModifierLifecycle() throws Exception {
        final boolean[] flags = new boolean[2];
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        mod.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                flags[0] = true;
                return builder;
            }
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                flags[1] = true;
                return deserializer;
            }
        });
        mapper.registerModule(mod);
        SimpleBean bean = mapper.readValue("{\"name\":\"test\",\"age\":1}", SimpleBean.class);
        Assert.assertNotNull(bean);
        Assert.assertTrue(flags[0]);
        Assert.assertTrue(flags[1]);
    }
}
