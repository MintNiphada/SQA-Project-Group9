package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.AbstractTypeResolver;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanDeserializerFactoryTest {

    static class SimpleBean {
        public String name;
        public int value;

        public void setName(String n) { this.name = n; }
        public String getName() { return this.name; }
    }

    static class ExceptionBean extends Throwable {
        private static final long serialVersionUID = 1L;
        public String extra;

        public ExceptionBean() { super(); }
        public ExceptionBean(String msg) { super(msg); }
        public void setExtra(String e) { this.extra = e; }
    }

    @JsonIgnoreType
    static class IgnoredType {
        public String value;
    }

    static class BeanWithIgnoredType {
        public IgnoredType ignored;
        public String valid;
    }

    @JsonIgnoreProperties({"skipMe"})
    static class IgnoralBean {
        public String keepMe;
        public String skipMe;
    }

    static class AnySetterMethodBean {
        private Map<String, Object> map = new HashMap<>();

        @com.fasterxml.jackson.annotation.JsonAnySetter
        public void add(String key, Object value) {
            map.put(key, value);
        }

        public Map<String, Object> getMap() { return map; }
    }

    static class AnySetterFieldBean {
        @com.fasterxml.jackson.annotation.JsonAnySetter
        public Map<String, Object> map = new HashMap<>();
    }

    static class GetterAsSetterBean {
        private List<String> list = new java.util.ArrayList<>();

        public List<String> getList() {
            return list;
        }
    }

    static class CreatorBean {
        private final String prop;

        @JsonCreator
        public CreatorBean(@JsonProperty("prop") String prop) {
            this.prop = prop;
        }

        public String getProp() { return prop; }
    }

    static class ParentRef {
        public String id;
        @JsonManagedReference
        public ChildRef child;
    }

    static class ChildRef {
        public String name;
        @JsonBackReference
        public ParentRef parent;
    }

    static class InjectableBean {
        @com.fasterxml.jackson.annotation.JacksonInject("testId")
        public String id;
    }

    @com.fasterxml.jackson.annotation.JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class ObjectIdPropertyBean {
        public int id;
    }

    @com.fasterxml.jackson.annotation.JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    static class ObjectIdSeqBean {
        public String name;
    }

    @JsonDeserialize(builder = ValueClassBuilder.class)
    static class ValueClass {
        final int val;
        ValueClass(int v) { this.val = v; }
    }

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "set")
    static class ValueClassBuilder {
        private int val;
        public ValueClassBuilder setVal(int v) { this.val = v; return this; }
        public ValueClass build() { return new ValueClass(val); }
    }

    public interface AnAbstractInterface {
        String getValue();
    }

    static class ConcreteImpl implements AnAbstractInterface {
        public String value;
        public ConcreteImpl() {}
        public ConcreteImpl(String v) { this.value = v; }
        @Override
        public String getValue() { return value; }
        public void setValue(String v) { this.value = v; }
    }

    @Test
    public void testFactoryInstanceAndWithConfig() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        Assert.assertNotNull(factory);
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        DeserializerFactory modified = factory.withConfig(config);
        Assert.assertNotNull(modified);
        Assert.assertSame(factory, factory.withConfig(factory.getFactoryConfig()));
    }

    @Test(expected = IllegalStateException.class)
    public void testSubtypeWithConfigCheck() {
        BeanDeserializerFactory customSubtype = new BeanDeserializerFactory(new DeserializerFactoryConfig()) {};
        customSubtype.withConfig(new DeserializerFactoryConfig());
    }

    @Test
    public void testCreateSimpleBeanDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateThrowableDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(ExceptionBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);

        ExceptionBean result = mapper.readValue("{\"message\":\"msg\",\"extra\":\"value\"}", ExceptionBean.class);
        Assert.assertEquals("msg", result.getMessage());
        Assert.assertEquals("value", result.extra);
    }

    @Test
    public void testAbstractTypeMaterialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializerFactoryConfig dfc = new DeserializerFactoryConfig().withAbstractTypeResolver(
                new AbstractTypeResolver() {
                    @Override
                    public JavaType resolveAbstractType(DeserializationConfig config, BeanDescription typeDesc) {
                        if (typeDesc.getBeanClass() == AnAbstractInterface.class) {
                            return config.constructType(ConcreteImpl.class);
                        }
                        return null;
                    }
                }
        );
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(dfc);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(AnAbstractInterface.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testIllegalTypesBlocked() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        try {
            JavaType type = mapper.constructType(Class.forName("org.apache.commons.collections.functors.InvokerTransformer"));
            BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
            BeanDeserializerFactory.instance.checkIllegalTypes(ctxt, type, desc);
            Assert.fail("Expected security exception");
        } catch (ClassNotFoundException e) {
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Illegal type"));
        }
    }

    @Test
    public void testIsPotentialBeanType() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        Assert.assertTrue(factory.isPotentialBeanType(SimpleBean.class));

        try {
            factory.isPotentialBeanType(int[].class);
            Assert.fail("Expected exception for array");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("as a Bean"));
        }

        try {
            Class<?> proxyClass = Proxy.getProxyClass(getClass().getClassLoader(), new Class<?>[]{Runnable.class});
            factory.isPotentialBeanType(proxyClass);
            Assert.fail("Expected exception for Proxy");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Proxy"));
        }
    }

    @Test
    public void testIgnorableTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanWithIgnoredType result = mapper.readValue("{\"valid\":\"ok\",\"ignored\":{\"value\":\"test\"}}", BeanWithIgnoredType.class);
        Assert.assertNotNull(result);
        Assert.assertEquals("ok", result.valid);
        Assert.assertNull(result.ignored);
    }

    @Test
    public void testBuilderBasedDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ValueClass vc = mapper.readValue("{\"val\":42}", ValueClass.class);
        Assert.assertNotNull(vc);
        Assert.assertEquals(42, vc.val);
    }

    @Test
    public void testPropertyAndSeqObjectId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectIdPropertyBean propBean = mapper.readValue("{\"id\":123}", ObjectIdPropertyBean.class);
        Assert.assertNotNull(propBean);
        Assert.assertEquals(123, propBean.id);

        ObjectIdSeqBean seqBean = mapper.readValue("{\"id\":1,\"name\":\"test\"}", ObjectIdSeqBean.class);
        Assert.assertNotNull(seqBean);
        Assert.assertEquals("test", seqBean.name);
    }

    @Test
    public void testAnySetterMethodAndField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterMethodBean bean1 = mapper.readValue("{\"key1\":\"val1\"}", AnySetterMethodBean.class);
        Assert.assertEquals("val1", bean1.getMap().get("key1"));

        AnySetterFieldBean bean2 = mapper.readValue("{\"key2\":\"val2\"}", AnySetterFieldBean.class);
        Assert.assertEquals("val2", bean2.map.get("key2"));
    }

    @Test
    public void testGetterAsSetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        GetterAsSetterBean bean = mapper.readValue("{\"list\":[\"a\",\"b\"]}", GetterAsSetterBean.class);
        Assert.assertEquals(2, bean.getList().size());
        Assert.assertEquals("a", bean.getList().get(0));
    }

    @Test
    public void testCreatorProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"prop\":\"testVal\"}", CreatorBean.class);
        Assert.assertEquals("testVal", bean.getProp());
    }

    @Test
    public void testBackAndForwardReferences() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ParentRef parent = mapper.readValue("{\"id\":\"p1\",\"child\":{\"name\":\"c1\"}}", ParentRef.class);
        Assert.assertNotNull(parent);
        Assert.assertNotNull(parent.child);
        Assert.assertEquals(parent, parent.child.parent);
    }

    @Test
    public void testInjectables() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        com.fasterxml.jackson.databind.InjectableValues inject = new com.fasterxml.jackson.databind.InjectableValues.Std()
                .addValue("testId", "injected-123");
        InjectableBean bean = mapper.reader(inject).forType(InjectableBean.class).readValue("{}");
        Assert.assertNotNull(bean);
        Assert.assertEquals("injected-123", bean.id);
    }

    @Test
    public void testDeserializerModifiersApplied() throws Exception {
        final boolean[] flag = new boolean[2];
        BeanDeserializerModifier mod = new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                flag[0] = true;
                return builder;
            }

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                flag[1] = true;
                return deserializer;
            }
        };

        DeserializerFactoryConfig dfc = new DeserializerFactoryConfig().withDeserializerModifier(mod);
        BeanDeserializerFactory customFactory = new BeanDeserializerFactory(dfc);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);
        JsonDeserializer<Object> deser = customFactory.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
        Assert.assertTrue(flag[0]);
        Assert.assertTrue(flag[1]);
    }
}
