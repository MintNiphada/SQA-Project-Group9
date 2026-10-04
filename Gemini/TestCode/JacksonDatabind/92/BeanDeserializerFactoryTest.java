package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanDeserializerFactoryTest {

    static class SimpleBean {
        public String name;
        public int age;
    }

    static class GetterSetterBean {
        private String value;
        public String getValue() { return value; }
        public void setValue(String v) { this.value = v; }
    }

    static class CollectionGetterBean {
        private List<String> items = new ArrayList<String>();
        public List<String> getItems() { return items; }
    }

    static class MapGetterBean {
        private Map<String, Object> map = new HashMap<String, Object>();
        public Map<String, Object> getMap() { return map; }
    }

    @JsonIgnoreProperties({"ignoredField"})
    static class IgnoredPropsBean {
        public String normalField;
        public String ignoredField;
        @JsonIgnore
        public String explicitIgnoreField;
    }

    @JsonIgnoreType
    static class IgnoredType {
        public String value;
    }

    static class ContainerWithIgnoredType {
        public IgnoredType ignored;
        public String name;
    }

    static class AnySetterBean {
        private Map<String, Object> extra = new HashMap<String, Object>();
        @JsonAnySetter
        public void setExtra(String key, Object value) {
            extra.put(key, value);
        }
    }

    static class CreatorBean {
        public String name;
        public int id;

        @JsonCreator
        public CreatorBean(@JsonProperty("name") String name, @JsonProperty("id") int id) {
            this.name = name;
            this.id = id;
        }
    }

    static class InjectedBean {
        @JacksonInject("injectedName")
        public String name;
    }

    static class ParentRef {
        public String name;
        public ChildRef child;
    }

    static class ChildRef {
        public String info;
        @com.fasterxml.jackson.annotation.JsonBackReference
        public ParentRef parent;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdPropBean {
        public int id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    static class IdIntSeqBean {
        public int id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "missingId")
    static class MissingIdPropBean {
        public int id;
    }

    @JsonDeserialize(builder = SimpleBuilderBean.Builder.class)
    static class SimpleBuilderBean {
        final String x;
        SimpleBuilderBean(String x) { this.x = x; }

        @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "with")
        static class Builder {
            private String x;
            public Builder withX(String x) { this.x = x; return this; }
            public SimpleBuilderBean create() { return new SimpleBuilderBean(x); }
        }
    }

    static class CustomException extends Throwable {
        private static final long serialVersionUID = 1L;
        public CustomException(String msg) { super(msg); }
        public CustomException() { super(); }
    }

    static abstract class AbstractType {
        public String value;
    }

    static class CustomSubFactory extends BeanDeserializerFactory {
        public CustomSubFactory(DeserializerFactoryConfig config) {
            super(config);
        }
    }

    @Test
    public void testInstanceAndWithConfig() {
        BeanDeserializerFactory factory = BeanDeserializerFactory.instance;
        Assert.assertNotNull(factory);
        Assert.assertSame(factory, factory.withConfig(factory.getFactoryConfig()));

        DeserializerFactoryConfig newConfig = new DeserializerFactoryConfig();
        DeserializerFactory newFactory = factory.withConfig(newConfig);
        Assert.assertNotSame(factory, newFactory);
        Assert.assertTrue(newFactory instanceof BeanDeserializerFactory);
    }

    @Test(expected = IllegalStateException.class)
    public void testSubtypeWithConfigFails() {
        CustomSubFactory subFactory = new CustomSubFactory(new DeserializerFactoryConfig());
        subFactory.withConfig(new DeserializerFactoryConfig());
    }

    @Test
    public void testCreateBeanDeserializerSimple() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateBeanDeserializerThrowable() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(CustomException.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateBeanDeserializerAbstract() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(AbstractType.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateBeanDeserializerWithGettersAsSetters() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JavaType typeCol = mapper.constructType(CollectionGetterBean.class);
        BeanDescription descCol = ctxt.getConfig().introspect(typeCol);
        JsonDeserializer<Object> deserCol = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, typeCol, descCol);
        Assert.assertNotNull(deserCol);

        JavaType typeMap = mapper.constructType(MapGetterBean.class);
        BeanDescription descMap = ctxt.getConfig().introspect(typeMap);
        JsonDeserializer<Object> deserMap = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, typeMap, descMap);
        Assert.assertNotNull(deserMap);
    }

    @Test
    public void testCreateBeanDeserializerWithIgnoredPropsAndTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JavaType typeIgn = mapper.constructType(IgnoredPropsBean.class);
        BeanDescription descIgn = ctxt.getConfig().introspect(typeIgn);
        JsonDeserializer<Object> deserIgn = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, typeIgn, descIgn);
        Assert.assertNotNull(deserIgn);

        JavaType typeCont = mapper.constructType(ContainerWithIgnoredType.class);
        BeanDescription descCont = ctxt.getConfig().introspect(typeCont);
        JsonDeserializer<Object> deserCont = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, typeCont, descCont);
        Assert.assertNotNull(deserCont);
    }

    @Test
    public void testCreateBeanDeserializerWithAnySetter() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(AnySetterBean.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateBeanDeserializerWithCreator() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(CreatorBean.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateBeanDeserializerWithInjectables() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(InjectedBean.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateBeanDeserializerWithBackReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(ChildRef.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testCreateBeanDeserializerWithObjectId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        JavaType typeProp = mapper.constructType(IdPropBean.class);
        BeanDescription descProp = ctxt.getConfig().introspect(typeProp);
        JsonDeserializer<Object> deserProp = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, typeProp, descProp);
        Assert.assertNotNull(deserProp);

        JavaType typeSeq = mapper.constructType(IdIntSeqBean.class);
        BeanDescription descSeq = ctxt.getConfig().introspect(typeSeq);
        JsonDeserializer<Object> deserSeq = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, typeSeq, descSeq);
        Assert.assertNotNull(deserSeq);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateBeanDeserializerMissingObjectIdProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(MissingIdPropBean.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
    }

    @Test
    public void testCreateBuilderBasedDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBuilderBean.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBuilderBasedDeserializer(
                ctxt, type, desc, SimpleBuilderBean.Builder.class);
        Assert.assertNotNull(deser);
    }

    @Test
    public void testWithDeserializerModifier() throws Exception {
        final boolean[] updated = new boolean[1];
        BeanDeserializerModifier modifier = new BeanDeserializerModifier() {
            @Override
            public BeanDeserializerBuilder updateBuilder(DeserializationConfig config,
                                                         BeanDescription beanDesc,
                                                         BeanDeserializerBuilder builder) {
                updated[0] = true;
                return builder;
            }
        };

        DeserializerFactoryConfig config = new DeserializerFactoryConfig().withDeserializerModifier(modifier);
        BeanDeserializerFactory factory = (BeanDeserializerFactory) BeanDeserializerFactory.instance.withConfig(config);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        factory.createBeanDeserializer(ctxt, type, desc);
        Assert.assertTrue(updated[0]);
    }

    @Test(expected = JsonMappingException.class)
    public void testCheckIllegalTypes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(com.sun.rowset.JdbcRowSetImpl.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        BeanDeserializerFactory.instance.checkIllegalTypes(ctxt, type, desc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypePrimitive() {
        BeanDeserializerFactory.instance.isPotentialBeanType(int.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsPotentialBeanTypeArray() {
        BeanDeserializerFactory.instance.isPotentialBeanType(String[].class);
    }

    @Test
    public void testIsPotentialBeanTypeValid() {
        Assert.assertTrue(BeanDeserializerFactory.instance.isPotentialBeanType(SimpleBean.class));
    }

    @Test
    public void testDisabledViewInclusion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        JavaType type = mapper.constructType(GetterSetterBean.class);
        BeanDescription desc = ctxt.getConfig().introspect(type);

        JsonDeserializer<Object> deser = BeanDeserializerFactory.instance.createBeanDeserializer(ctxt, type, desc);
        Assert.assertNotNull(deser);
    }
}
