package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.ThrowableDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.*;

public class BeanDeserializerFactoryTest {

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

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredBean {
        public int id;
        public String ignoredField;
    }

    static class SetterlessBean {
        private final List<String> list = new ArrayList<String>();
        private final Map<String, String> map = new HashMap<String, String>();

        public List<String> getList() { return list; }
        public Map<String, String> getMap() { return map; }
    }

    static class AnySetterBean {
        public Map<String, Object> extra = new HashMap<String, Object>();

        @JsonAnySetter
        public void handleUnknown(String key, Object value) {
            extra.put(key, value);
        }
    }

    static class InjectBean {
        @JacksonInject("injectId")
        public String id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class ObjectIdPropBean {
        public int id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class ObjectIdSeqBean {
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nonExistent")
    static class BadObjectIdBean {
        public int id;
    }

    static class Parent {
        public String name;
        @JsonManagedReference
        public Child child;
    }

    static class Child {
        public int age;
        @JsonBackReference
        public Parent parent;
    }

    @JsonDeserialize(builder = ValueBuilder.class)
    static class ValueObject {
        final int value;
        ValueObject(int v) { this.value = v; }
    }

    @JsonPOJOBuilder(withPrefix = "set")
    static class ValueBuilder {
        private int value;
        public ValueBuilder setValue(int v) { this.value = v; return this; }
        public ValueObject build() { return new ValueObject(value); }
    }

    static class CustomException extends Throwable {
        private static final long serialVersionUID = 1L;
        public CustomException(String msg) { super(msg); }
    }

    interface AbstractType {
        String getValue();
    }

    static class AbstractTypeImpl implements AbstractType {
        private String value;
        public void setValue(String v) { this.value = v; }
        @Override
        public String getValue() { return value; }
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public int pub;
        @JsonView(Views.Internal.class)
        public int priv;
    }

    static class Views {
        static class Public {}
        static class Internal {}
    }

    @JsonIgnoreType
    static class IgnorablePayload {
        public String secret;
    }

    static class BeanWithIgnorableType {
        public int id;
        public IgnorablePayload payload;
    }

    static class CreatorBean {
        public int id;
        public String name;

        @JsonCreator
        public CreatorBean(@JsonProperty("id") int id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class SubclassedFactory extends BeanDeserializerFactory {
        private static final long serialVersionUID = 1L;
        public SubclassedFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    @Test
    public void testWithConfig() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        DeserializerFactoryConfig config = new DeserializerFactoryConfig();
        DeserializerFactory same = factory.withConfig(factory.getFactoryConfig());
        Assert.assertSame(factory, same);

        DeserializerFactory diff = factory.withConfig(config);
        Assert.assertNotNull(diff);
        Assert.assertNotSame(factory, diff);
        Assert.assertTrue(diff instanceof BeanDeserializerFactory);
    }

    @Test(expected = IllegalStateException.class)
    public void testSubclassWithConfigThrows() {
        SubclassedFactory factory = new SubclassedFactory(new DeserializerFactoryConfig());
        factory.withConfig(new DeserializerFactoryConfig());
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

        try {
            Object proxyInstance = Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class<?>[]{Comparable.class},
                    (p, m, args) -> null);
            factory.isPotentialBeanType(proxyInstance.getClass());
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Proxy"));
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
    public void testSimpleBeanDeserialization() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = mapper.readValue("{\"x\":10,\"y\":\"foo\"}", SimpleBean.class);
        Assert.assertEquals(10, bean.x);
        Assert.assertEquals("foo", bean.y);
    }

    @Test
    public void testFieldOnlyBeanDeserialization() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        FieldOnlyBean bean = mapper.readValue("{\"a\":5,\"b\":\"bar\"}", FieldOnlyBean.class);
        Assert.assertEquals(5, bean.a);
        Assert.assertEquals("bar", bean.b);
    }

    @Test
    public void testIgnoredProperties() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        IgnoredBean bean = mapper.readValue("{\"id\":1,\"ignoredField\":\"skip\"}", IgnoredBean.class);
        Assert.assertEquals(1, bean.id);
        Assert.assertNull(bean.ignoredField);
    }

    @Test
    public void testIgnorableType() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BeanWithIgnorableType bean = mapper.readValue("{\"id\":2,\"payload\":{\"secret\":\"hidden\"}}", BeanWithIgnorableType.class);
        Assert.assertEquals(2, bean.id);
        Assert.assertNull(bean.payload);
    }

    @Test
    public void testSetterlessProperties() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SetterlessBean bean = mapper.readValue("{\"list\":[\"a\",\"b\"],\"map\":{\"k\":\"v\"}}", SetterlessBean.class);
        Assert.assertEquals(2, bean.getList().size());
        Assert.assertEquals("v", bean.getMap().get("k"));
    }

    @Test
    public void testAnySetter() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AnySetterBean bean = mapper.readValue("{\"extra1\":\"val1\",\"extra2\":123}", AnySetterBean.class);
        Assert.assertEquals("val1", bean.extra.get("extra1"));
        Assert.assertEquals(123, bean.extra.get("extra2"));
    }

    @Test
    public void testInjectables() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InjectableValues.Std injectables = new InjectableValues.Std();
        injectables.addValue("injectId", "injected123");
        InjectBean bean = mapper.reader(injectables).forType(InjectBean.class).readValue("{\"name\":\"test\"}");
        Assert.assertEquals("injected123", bean.id);
        Assert.assertEquals("test", bean.name);
    }

    @Test
    public void testObjectIdPropertyBased() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectIdPropBean bean = mapper.readValue("{\"id\":100,\"name\":\"foo\"}", ObjectIdPropBean.class);
        Assert.assertEquals(100, bean.id);
        Assert.assertEquals("foo", bean.name);
    }

    @Test
    public void testObjectIdSequenceBased() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectIdSeqBean bean = mapper.readValue("{\"@id\":1,\"name\":\"seq\"}", ObjectIdSeqBean.class);
        Assert.assertEquals("seq", bean.name);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidObjectIdPropertyThrows() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.readValue("{\"id\":1}", BadObjectIdBean.class);
    }

    @Test
    public void testManagedAndBackReference() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Parent parent = mapper.readValue("{\"name\":\"p\",\"child\":{\"age\":5}}", Parent.class);
        Assert.assertEquals("p", parent.name);
        Assert.assertNotNull(parent.child);
        Assert.assertEquals(5, parent.child.age);
        Assert.assertSame(parent, parent.child.parent);
    }

    @Test
    public void testBuilderBasedDeserializer() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ValueObject vo = mapper.readValue("{\"value\":42}", ValueObject.class);
        Assert.assertEquals(42, vo.value);
    }

    @Test
    public void testThrowableDeserializer() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CustomException ex = mapper.readValue("{\"message\":\"msg\",\"cause\":null}", CustomException.class);
        Assert.assertEquals("msg", ex.getMessage());
        Assert.assertTrue(ex instanceof CustomException);
    }

    @Test
    public void testAbstractTypeMaterialization() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractType.class, AbstractTypeImpl.class);
        module.setAbstractTypes(resolver);
        mapper.registerModule(module);

        AbstractType result = mapper.readValue("{\"value\":\"concrete\"}", AbstractType.class);
        Assert.assertTrue(result instanceof AbstractTypeImpl);
        Assert.assertEquals("concrete", result.getValue());
    }

    @Test
    public void testCreatorProperties() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        CreatorBean bean = mapper.readValue("{\"id\":12,\"name\":\"creator\"}", CreatorBean.class);
        Assert.assertEquals(12, bean.id);
        Assert.assertEquals("creator", bean.name);
    }

    @Test
    public void testViewsInclusion() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        ViewBean bean = mapper.readerWithView(Views.Public.class).forType(ViewBean.class)
                .readValue("{\"pub\":1,\"priv\":2}");
        Assert.assertEquals(1, bean.pub);
        Assert.assertEquals(0, bean.priv);
    }

    @Test
    public void testDeserializerModifierHooks() throws IOException {
        final boolean[] updated = new boolean[3];
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
                updated[0] = true;
                return super.updateProperties(config, beanDesc, propDefs);
            }

            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
                updated[1] = true;
                return super.updateBuilder(config, beanDesc, builder);
            }

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
                updated[2] = true;
                return super.modifyDeserializer(config, beanDesc, deserializer);
            }
        });
        mapper.registerModule(module);
        SimpleBean bean = mapper.readValue("{\"x\":1}", SimpleBean.class);
        Assert.assertEquals(1, bean.x);
        Assert.assertTrue(updated[0]);
        Assert.assertTrue(updated[1]);
        Assert.assertTrue(updated[2]);
    }

    @Test
    public void testCustomBeanDeserializerPrecedence() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(SimpleBean.class, new StdDeserializer<SimpleBean>(SimpleBean.class) {
            @Override
            public SimpleBean deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
                SimpleBean bean = new SimpleBean();
                bean.x = 999;
                return bean;
            }
        });
        mapper.registerModule(module);
        SimpleBean bean = mapper.readValue("{\"x\":1}", SimpleBean.class);
        Assert.assertEquals(999, bean.x);
    }

    @Test
    public void testDirectFactoryMethods() throws Exception {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = mapper.getDeserializationConfig().introspect(type);

        JsonDeserializer<Object> deser = factory.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
        Assert.assertTrue(deser instanceof BeanDeserializer);

        JavaType exType = mapper.constructType(CustomException.class);
        BeanDescription exDesc = mapper.getDeserializationConfig().introspect(exType);
        JsonDeserializer<Object> exDeser = factory.buildThrowableDeserializer(ctxt, exType, exDesc);
        Assert.assertNotNull(exDeser);
        Assert.assertTrue(exDeser instanceof ThrowableDeserializer);
    }
}
