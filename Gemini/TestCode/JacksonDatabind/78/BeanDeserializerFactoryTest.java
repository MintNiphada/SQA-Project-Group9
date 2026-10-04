package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.cfg.ConfigOverride;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class BeanDeserializerFactoryTest {

    static class CustomFactorySubclass extends BeanDeserializerFactory {
        public CustomFactorySubclass(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    static class SimpleBean {
        public int x;
        public String y;

        public void setX(int x) { this.x = x; }
        public void setY(String y) { this.y = y; }
    }

    static class FieldOnlyBean {
        public int a;
        public String b;
    }

    static class GetterSetterlessBean {
        private List<String> list = new ArrayList<String>();
        private Map<String, String> map = new HashMap<String, String>();

        public List<String> getList() { return list; }
        public Map<String, String> getMap() { return map; }
    }

    static class CreatorBean {
        final int id;
        final String name;

        @JsonCreator
        public CreatorBean(@JsonProperty("id") int id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class AnySetterMethodBean {
        Map<String, Object> map = new HashMap<String, Object>();

        @JsonAnySetter
        public void setAny(String name, Object value) {
            map.put(name, value);
        }
    }

    static class AnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> map = new HashMap<String, Object>();
    }

    static class IgnoredTypeHolder {
        public IgnoredClass ignored;
        public int valid;
    }

    @JsonIgnoreType
    static class IgnoredClass {
        public String value;
    }

    @JsonIgnoreProperties({"hidden"})
    static class IgnoredPropsBean {
        public int id;
        public String hidden;
        @JsonIgnore
        public String ignoredField;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class ObjectIdPropertyBean {
        public int id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class ObjectIdSequenceBean {
        public int x;
    }

    static class ParentRef {
        public int id;
        @JsonManagedReference
        public ChildRef child;
    }

    static class ChildRef {
        public String name;
        @JsonBackReference
        public ParentRef parent;
    }

    static class InjectBean {
        @JacksonInject("injectedVal")
        public String val;
        public int num;
    }

    static class CustomThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        public int extraCode;

        public CustomThrowable() { super(); }
        public CustomThrowable(String msg) { super(msg); }
        public void setExtraCode(int code) { this.extraCode = code; }
    }

    @JsonDeserialize(builder = ValueClassBuilder.class)
    static class ValueClass {
        final int a;
        final String b;

        ValueClass(int a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    @JsonPOJOBuilder(withPrefix = "with", buildMethodName = "create")
    static class ValueClassBuilder {
        private int a;
        private String b;

        public ValueClassBuilder withA(int a) {
            this.a = a;
            return this;
        }

        public ValueClassBuilder withB(String b) {
            this.b = b;
            return this;
        }

        public ValueClass create() {
            return new ValueClass(a, b);
        }
    }

    interface AbstractInterface {
        int getValue();
    }

    static class ConcreteImpl implements AbstractInterface {
        public int value;
        public int getValue() { return value; }
        public void setValue(int v) { this.value = v; }
    }

    abstract static class NonInstantiableAbstract {
        public int x;
    }

    @Test
    public void testWithConfig() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        DeserializerFactory same = factory.withConfig(factory.getFactoryConfig());
        Assert.assertSame(factory, same);

        DeserializerFactory modified = factory.withConfig(config);
        Assert.assertNotSame(factory, modified);
        Assert.assertTrue(modified instanceof BeanDeserializerFactory);
    }

    @Test(expected = IllegalStateException.class)
    public void testWithConfigSubclassFails() {
        CustomFactorySubclass subclass = new CustomFactorySubclass(new DeserializerFactoryConfig());
        subclass.withConfig(new DeserializerFactoryConfig());
    }

    @Test
    public void testIsPotentialBeanType() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        Assert.assertTrue(factory.isPotentialBeanType(SimpleBean.class));

        try {
            factory.isPotentialBeanType(int.class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("primitive"));
        }

        try {
            factory.isPotentialBeanType(int[].class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("array"));
        }

        class LocalClass {}
        try {
            factory.isPotentialBeanType(LocalClass.class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("local"));
        }
    }

    @Test
    public void testCreateBeanDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);

        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);

        SimpleBean bean = mapper.readValue("{\"x\":10,\"y\":\"hello\"}", SimpleBean.class);
        Assert.assertEquals(10, bean.x);
        Assert.assertEquals("hello", bean.y);
    }

    @Test
    public void testCreateBeanDeserializerWithModifiers() throws Exception {
        final boolean[] modifierCalled = new boolean[3];
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        mod.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                modifierCalled[0] = true;
                return builder;
            }

            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
                modifierCalled[1] = true;
                return propDefs;
            }

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                modifierCalled[2] = true;
                return deserializer;
            }
        });
        mapper.registerModule(mod);

        SimpleBean bean = mapper.readValue("{\"x\":1,\"y\":\"test\"}", SimpleBean.class);
        Assert.assertEquals(1, bean.x);
        Assert.assertTrue(modifierCalled[0]);
        Assert.assertTrue(modifierCalled[1]);
        Assert.assertTrue(modifierCalled[2]);
    }

    @Test
    public void testBuildThrowableDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"message\":\"something wrong\",\"extraCode\":404}";
        CustomThrowable t = mapper.readValue(json, CustomThrowable.class);
        Assert.assertEquals("something wrong", t.getMessage());
        Assert.assertEquals(404, t.extraCode);
    }

    @Test
    public void testMaterializeAbstractType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractInterface.class, ConcreteImpl.class);
        mod.setAbstractTypes(resolver);
        mapper.registerModule(mod);

        AbstractInterface result = mapper.readValue("{\"value\":42}", AbstractInterface.class);
        Assert.assertTrue(result instanceof ConcreteImpl);
        Assert.assertEquals(42, result.getValue());
    }

    @Test
    public void testBuilderBasedDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ValueClass value = mapper.readValue("{\"a\":123,\"b\":\"abc\"}", ValueClass.class);
        Assert.assertEquals(123, value.a);
        Assert.assertEquals("abc", value.b);
    }

    @Test
    public void testAddBeanPropsSetterless() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_GETTERS_AS_SETTERS);
        GetterSetterlessBean bean = mapper.readValue("{\"list\":[\"a\",\"b\"],\"map\":{\"k\":\"v\"}}", GetterSetterlessBean.class);
        Assert.assertEquals(2, bean.getList().size());
        Assert.assertEquals("v", bean.getMap().get("k"));
    }

    @Test
    public void testCreatorProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"id\":7,\"name\":\"jack\"}", CreatorBean.class);
        Assert.assertEquals(7, bean.id);
        Assert.assertEquals("jack", bean.name);
    }

    @Test
    public void testAnySetterMethod() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterMethodBean bean = mapper.readValue("{\"foo\":\"bar\",\"num\":10}", AnySetterMethodBean.class);
        Assert.assertEquals("bar", bean.map.get("foo"));
        Assert.assertEquals(10, bean.map.get("num"));
    }

    @Test
    public void testAnySetterField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterFieldBean bean = mapper.readValue("{\"k1\":\"v1\",\"k2\":99}", AnySetterFieldBean.class);
        Assert.assertEquals("v1", bean.map.get("k1"));
    }

    @Test
    public void testIgnoredProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoredPropsBean bean = mapper.readValue("{\"id\":10,\"hidden\":\"secret\",\"ignoredField\":\"val\"}", IgnoredPropsBean.class);
        Assert.assertEquals(10, bean.id);
        Assert.assertNull(bean.hidden);
        Assert.assertNull(bean.ignoredField);
    }

    @Test
    public void testIsIgnorableType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoredTypeHolder holder = mapper.readValue("{\"valid\":1,\"ignored\":{\"value\":\"test\"}}", IgnoredTypeHolder.class);
        Assert.assertEquals(1, holder.valid);
        Assert.assertNull(holder.ignored);

        Map<Class<?>, Boolean> map = new HashMap<Class<?>, Boolean>();
        BeanDescription desc = mapper.getDeserializationConfig().introspect(mapper.constructType(IgnoredTypeHolder.class));
        boolean ignorable = BeanDeserializerFactory.instance.isIgnorableType(mapper.getDeserializationConfig(), desc, IgnoredClass.class, map);
        Assert.assertTrue(ignorable);
    }

    @Test
    public void testObjectIdProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectIdPropertyBean bean = mapper.readValue("{\"id\":100,\"name\":\"test\"}", ObjectIdPropertyBean.class);
        Assert.assertEquals(100, bean.id);
        Assert.assertEquals("test", bean.name);
    }

    @Test
    public void testObjectIdSequence() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectIdSequenceBean bean = mapper.readValue("{\"@id\":1,\"x\":5}", ObjectIdSequenceBean.class);
        Assert.assertEquals(5, bean.x);
    }

    @Test
    public void testBackReferenceProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"child\":{\"name\":\"c1\"}}";
        ParentRef parent = mapper.readValue(json, ParentRef.class);
        Assert.assertEquals(1, parent.id);
        Assert.assertNotNull(parent.child);
        Assert.assertSame(parent, parent.child.parent);
    }

    @Test
    public void testAddInjectables() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std inject = new InjectableValues.Std();
        inject.addValue("injectedVal", "injected_string");
        InjectBean bean = mapper.reader(inject).forType(InjectBean.class).readValue("{\"num\":42}");
        Assert.assertEquals(42, bean.num);
        Assert.assertEquals("injected_string", bean.val);
    }

    @Test
    public void testAbstractTypeBuilding() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), null);
        JavaType type = mapper.constructType(NonInstantiableAbstract.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCustomDeserializerOverride() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule();
        mod.addDeserializer(SimpleBean.class, new StdDeserializer<SimpleBean>(SimpleBean.class) {
            @Override
            public SimpleBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws IOException {
                SimpleBean bean = new SimpleBean();
                bean.x = 999;
                return bean;
            }
        });
        mapper.registerModule(mod);

        SimpleBean bean = mapper.readValue("{\"x\":1}", SimpleBean.class);
        Assert.assertEquals(999, bean.x);
    }

    @Test
    public void testFieldPropertyHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FieldOnlyBean bean = mapper.readValue("{\"a\":15,\"b\":\"testField\"}", FieldOnlyBean.class);
        Assert.assertEquals(15, bean.a);
        Assert.assertEquals("testField", bean.b);
    }

    @Test
    public void testViewInclusionDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        SimpleBean bean = mapper.readValue("{\"x\":20,\"y\":\"viewTest\"}", SimpleBean.class);
        Assert.assertEquals(20, bean.x);
        Assert.assertEquals("viewTest", bean.y);
    }
}
