package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.*;

public class BeanDeserializerFactoryTest {

    public static class SimpleBean {
        public String name;
        public int value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }
    }

    public static class SetterlessBean {
        private final List<String> list = new ArrayList<String>();
        private final Map<String, String> map = new HashMap<String, String>();

        public List<String> getList() { return list; }
        public Map<String, String> getMap() { return map; }
    }

    @JsonIgnoreProperties({"ignoredField"})
    public static class IgnoralBean {
        public String validField;
        public String ignoredField;
    }

    @JsonIgnoreType
    public static class IgnoredType {
        public String data;
    }

    public static class BeanWithIgnoredType {
        public IgnoredType ignored;
        public String name;
    }

    public static class AnySetterBean {
        private final Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public void setExtra(String key, Object value) {
            extra.put(key, value);
        }

        public Map<String, Object> getExtra() {
            return extra;
        }
    }

    public static class AnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> extra = new HashMap<String, Object>();
    }

    public static class CreatorBean {
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

    public static class CustomException extends Throwable {
        private static final long serialVersionUID = 1L;
        public CustomException(String msg) { super(msg); }
    }

    @JsonDeserialize(builder = ValueBuilder.class)
    public static class ValueClass {
        final int x;
        ValueClass(int x) { this.x = x; }
        public int getX() { return x; }
    }

    @JsonPOJOBuilder(withPrefix = "set")
    public static class ValueBuilder {
        private int x;
        public ValueBuilder setX(int x) { this.x = x; return this; }
        public ValueClass build() { return new ValueClass(x); }
    }

    public interface AbstractModel {
        String getValue();
    }

    public static class ConcreteModel implements AbstractModel {
        private String value;
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    public static class CustomDeserializerFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;
        public CustomDeserializerFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    @Test
    public void testFactoryInstanceAndWithConfig() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        Assert.assertNotNull(factory);
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(config);
        Assert.assertNotNull(newFactory);
        Assert.assertNotSame(factory, newFactory);
        Assert.assertSame(newFactory, newFactory.withConfig(config));
    }

    @Test(expected = IllegalStateException.class)
    public void testWithConfigSubtypeCheck() {
        CustomDeserializerFactory customFactory = new CustomDeserializerFactory(new DeserializerFactoryConfig());
        customFactory.withConfig(new DeserializerFactoryConfig());
    }

    @Test
    public void testSimpleBeanDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("{\"name\":\"test\",\"value\":123}", SimpleBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("test", bean.getName());
        Assert.assertEquals(123, bean.getValue());
    }

    @Test
    public void testSetterlessProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SetterlessBean bean = mapper.readValue("{\"list\":[\"a\",\"b\"],\"map\":{\"k\":\"v\"}}", SetterlessBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals(2, bean.getList().size());
        Assert.assertEquals("v", bean.getMap().get("k"));
    }

    @Test
    public void testIgnoralProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoralBean bean = mapper.readValue("{\"validField\":\"ok\",\"ignoredField\":\"skip\"}", IgnoralBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("ok", bean.validField);
        Assert.assertNull(bean.ignoredField);
    }

    @Test
    public void testIgnoredTypeProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanWithIgnoredType bean = mapper.readValue("{\"name\":\"ok\",\"ignored\":{\"data\":\"ignoreMe\"}}", BeanWithIgnoredType.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("ok", bean.name);
        Assert.assertNull(bean.ignored);
    }

    @Test
    public void testAnySetterMethod() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterBean bean = mapper.readValue("{\"foo\":\"bar\",\"num\":42}", AnySetterBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("bar", bean.getExtra().get("foo"));
        Assert.assertEquals(42, bean.getExtra().get("num"));
    }

    @Test
    public void testAnySetterField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterFieldBean bean = mapper.readValue("{\"foo\":\"bar\"}", AnySetterFieldBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("bar", bean.extra.get("foo"));
    }

    @Test
    public void testCreatorBeanDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"name\":\"John\",\"age\":30}", CreatorBean.class);
        Assert.assertNotNull(bean);
        Assert.assertEquals("John", bean.getName());
        Assert.assertEquals(30, bean.getAge());
    }

    @Test
    public void testThrowableDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CustomException ex = mapper.readValue("{\"message\":\"error message\",\"localizedMessage\":\"loc\"}", CustomException.class);
        Assert.assertNotNull(ex);
        Assert.assertEquals("error message", ex.getMessage());
    }

    @Test
    public void testBuilderBasedDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ValueClass val = mapper.readValue("{\"x\":99}", ValueClass.class);
        Assert.assertNotNull(val);
        Assert.assertEquals(99, val.getX());
    }

    @Test
    public void testMaterializeAbstractType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractModel.class, ConcreteModel.class);
        module.setAbstractTypes(resolver);
        mapper.registerModule(module);

        AbstractModel model = mapper.readValue("{\"value\":\"concrete\"}", AbstractModel.class);
        Assert.assertNotNull(model);
        Assert.assertEquals("concrete", model.getValue());
    }

    @Test
    public void testIsPotentialBeanType() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        Assert.assertTrue(factory.isPotentialBeanType(SimpleBean.class));

        try {
            factory.isPotentialBeanType(int.class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot deserialize Class"));
        }

        try {
            factory.isPotentialBeanType(int[].class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot deserialize Class"));
        }

        Object proxyInstance = Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{Serializable.class},
                (proxy, method, args) -> null
        );
        try {
            factory.isPotentialBeanType(proxyInstance.getClass());
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot deserialize Proxy"));
        }

        class LocalClass {}
        try {
            factory.isPotentialBeanType(LocalClass.class);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot deserialize Class"));
        }
    }

    @Test
    public void testModifierOnBuilder() throws Exception {
        final boolean[] updated = new boolean[]{false};
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                                                         BeanDescription beanDesc,
                                                         BeanDeserializerBuilder builder) {
                if (beanDesc.getBeanClass() == SimpleBean.class) {
                    updated[0] = true;
                }
                return builder;
            }
        });
        mapper.registerModule(module);
        SimpleBean bean = mapper.readValue("{\"name\":\"test\"}", SimpleBean.class);
        Assert.assertNotNull(bean);
        Assert.assertTrue(updated[0]);
    }

    @Test
    public void testFilterBeanProps() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(IgnoralBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        BeanDeserializerBuilder builder = factory.constructBeanDeserializerBuilder(ctxt, desc);

        Set<String> ignored = new HashSet<String>();
        ignored.add("ignoredField");

        List<BeanPropertyDefinition> props = factory.filterBeanProps(ctxt, desc, builder, desc.findProperties(), ignored);
        Assert.assertNotNull(props);
        for (BeanPropertyDefinition prop : props) {
            Assert.assertNotEquals("ignoredField", prop.getName());
        }
    }

    @Test
    public void testIsIgnorableType() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        Map<Class<?>, Boolean> cache = new HashMap<Class<?>, Boolean>();

        Assert.assertFalse(factory.isIgnorableType(config, null, String.class, cache));
        Assert.assertFalse(factory.isIgnorableType(config, null, int.class, cache));
        Assert.assertTrue(factory.isIgnorableType(config, null, IgnoredType.class, cache));
    }

    @Test
    public void testConstructSetterlessProperty() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SetterlessBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        BeanPropertyDefinition propDef = null;
        for (BeanPropertyDefinition pd : desc.findProperties()) {
            if ("list".equals(pd.getName())) {
                propDef = pd;
                break;
            }
        }
        Assert.assertNotNull(propDef);
        SettableBeanProperty prop = factory.constructSetterlessProperty(ctxt, desc, propDef);
        Assert.assertNotNull(prop);
        Assert.assertEquals("list", prop.getName());
    }

    @Test
    public void testConstructAnySetterError() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        AnnotatedMember fakeMember = desc.getClassInfo();
        try {
            factory.constructAnySetter(ctxt, desc, fakeMember);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Unrecognized mutator type for any setter"));
        }
    }

    @Test
    public void testAddReferencePropertiesDeprecated() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        BeanDeserializerBuilder builder = factory.constructBeanDeserializerBuilder(ctxt, desc);

        factory.addReferenceProperties(ctxt, desc, builder);
        Assert.assertNotNull(builder);
    }
}
