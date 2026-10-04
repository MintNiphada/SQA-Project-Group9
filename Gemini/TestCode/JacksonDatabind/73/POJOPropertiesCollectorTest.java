package com.fasterxml.jackson.databind.introspect;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class POJOPropertiesCollectorTest {

    static class SimpleBean {
        public int id;
        public String name;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    static class TransientBean {
        public transient int trans1;
        @JsonProperty("trans2")
        public transient int trans2;
        public int regular;
    }

    static class BooleanBean {
        private boolean active;
        private boolean enabled;

        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean getEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    static class MultipleJsonValueBean {
        @JsonValue
        public String getVal1() { return "v1"; }
        @JsonValue
        public String getVal2() { return "v2"; }
    }

    static class SingleJsonValueBean {
        @JsonValue
        public String getVal() { return "v"; }
    }

    static class MultipleAnyGetterBean {
        @JsonAnyGetter
        public Map<String, Object> any1() { return Collections.emptyMap(); }
        @JsonAnyGetter
        public Map<String, Object> any2() { return Collections.emptyMap(); }
    }

    static class SingleAnyGetterBean {
        @JsonAnyGetter
        public Map<String, Object> any() { return Collections.emptyMap(); }
    }

    static class MultipleAnySetterMethodBean {
        @JsonAnySetter
        public void set1(String k, Object v) {}
        @JsonAnySetter
        public void set2(String k, Object v) {}
    }

    static class SingleAnySetterMethodBean {
        @JsonAnySetter
        public void set(String k, Object v) {}
    }

    static class MultipleAnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> map1;
        @JsonAnySetter
        public Map<String, Object> map2;
    }

    static class SingleAnySetterFieldBean {
        @JsonAnySetter
        public Map<String, Object> map;
    }

    static class CreatorBean {
        private final int a;
        private final String b;

        @JsonCreator
        public CreatorBean(@JsonProperty("a") int a, @JsonProperty("b") String b) {
            this.a = a;
            this.b = b;
        }

        public int getA() { return a; }
        public String getB() { return b; }
    }

    static class FactoryCreatorBean {
        private final int x;

        private FactoryCreatorBean(int x) { this.x = x; }

        @JsonCreator
        public static FactoryCreatorBean create(@JsonProperty("x") int x) {
            return new FactoryCreatorBean(x);
        }

        public int getX() { return x; }
    }

    static class InjectBean {
        @JacksonInject("injectId")
        public String injectedField;

        @JacksonInject("injectMethodId")
        public void setInjected(String val) {}
    }

    static class DuplicateInjectBean {
        @JacksonInject("sameId")
        public String field1;

        @JacksonInject("sameId")
        public String field2;
    }

    @JsonPropertyOrder({"z", "a", "m"})
    static class OrderedBean {
        public int z;
        public int a;
        public int m;
    }

    static class AlphabeticBean {
        public int c;
        public int a;
        public int b;
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    static class SnakeNamingBean {
        public String firstName;
        public String getLastName() { return "last"; }
        public void setMiddleName(String m) {}
    }

    @JsonNaming(PropertyNamingStrategy.class)
    static class DefaultNamingStrategyBean {
        public String someValue;
    }

    static class InvalidNamingAnnotationIntrospector extends JacksonAnnotationIntrospector {
        private final Object namingDef;
        public InvalidNamingAnnotationIntrospector(Object namingDef) {
            this.namingDef = namingDef;
        }
        @Override
        public Object findNamingStrategy(AnnotatedClass ac) {
            return namingDef;
        }
    }

    static class RenamedExplicitBean {
        @JsonProperty("customName")
        public String field;
        @JsonProperty("")
        public String emptyNameField;

        @JsonProperty("customMethod")
        public String getRenamed() { return field; }
        @JsonProperty("")
        public String getEmptyRenamed() { return field; }

        @JsonProperty("customSetter")
        public void setRenamed(String f) { this.field = f; }
        @JsonProperty("")
        public void setEmptyRenamed(String f) { this.field = f; }
    }

    static class FinalFieldBean {
        public final int unmutedFinal = 10;
        public int normal = 20;
    }

    static class IgnoredBean {
        @JsonIgnore
        public int ignoredField;
        public int normalField;

        @JsonIgnore
        public int getIgnoredGetter() { return 1; }
        public void setIgnoredGetter(int val) {}

        public int getNormalGetter() { return 2; }
    }

    static class BuilderTestBean {
        private int x;
        public BuilderTestBean withX(int x) {
            this.x = x;
            return this;
        }
        public int getX() { return x; }
    }

    public class NonStaticInnerBean {
        public int innerField;
        public NonStaticInnerBean(int innerField) {
            this.innerField = innerField;
        }
    }

    private POJOPropertiesCollector collectorFor(Class<?> cls, boolean forSerialization) {
        return collectorFor(new ObjectMapper(), cls, forSerialization, null);
    }

    private POJOPropertiesCollector collectorFor(ObjectMapper mapper, Class<?> cls, boolean forSerialization, String mutatorPrefix) {
        JavaType type = mapper.getTypeFactory().constructType(cls);
        MapperConfig<?> config = forSerialization ? mapper.getSerializationConfig() : mapper.getDeserializationConfig();
        AnnotatedClass ac = AnnotatedClassResolver.resolve(config, type, config);
        return new POJOPropertiesCollector(config, forSerialization, type, ac, mutatorPrefix);
    }

    @Test
    public void testBasicPropertiesAndAccessors() {
        POJOPropertiesCollector coll = collectorFor(SimpleBean.class, true);
        Assert.assertNotNull(coll.getConfig());
        Assert.assertNotNull(coll.getType());
        Assert.assertNotNull(coll.getClassDef());
        Assert.assertNotNull(coll.getAnnotationIntrospector());
        Assert.assertSame(coll, coll.collect());

        List<BeanPropertyDefinition> props = coll.getProperties();
        Assert.assertEquals(2, props.size());

        Set<String> names = new HashSet<String>();
        for (BeanPropertyDefinition prop : props) {
            names.add(prop.getName());
        }
        Assert.assertTrue(names.contains("id"));
        Assert.assertTrue(names.contains("name"));
    }

    @Test
    public void testTransientHandling() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
        POJOPropertiesCollector coll = collectorFor(mapper, TransientBean.class, true, null);
        List<BeanPropertyDefinition> props = coll.getProperties();
        Set<String> names = new HashSet<String>();
        for (BeanPropertyDefinition prop : props) {
            names.add(prop.getName());
        }
        Assert.assertFalse(names.contains("trans1"));
        Assert.assertTrue(names.contains("trans2"));
        Assert.assertTrue(names.contains("regular"));
    }

    @Test
    public void testBooleanGetters() {
        POJOPropertiesCollector coll = collectorFor(BooleanBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        Set<String> names = new HashSet<String>();
        for (BeanPropertyDefinition prop : props) {
            names.add(prop.getName());
        }
        Assert.assertTrue(names.contains("active"));
        Assert.assertTrue(names.contains("enabled"));
    }

    @Test
    public void testSingleAndMultipleJsonValue() {
        POJOPropertiesCollector singleColl = collectorFor(SingleJsonValueBean.class, true);
        Assert.assertNotNull(singleColl.getJsonValueMethod());
        Assert.assertEquals("getVal", singleColl.getJsonValueMethod().getName());

        POJOPropertiesCollector multiColl = collectorFor(MultipleJsonValueBean.class, true);
        try {
            multiColl.getJsonValueMethod();
            Assert.fail("Expected IllegalArgumentException for multiple @JsonValue");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Multiple value properties defined"));
        }
    }

    @Test
    public void testSingleAndMultipleAnyGetter() {
        POJOPropertiesCollector singleColl = collectorFor(SingleAnyGetterBean.class, true);
        Assert.assertNotNull(singleColl.getAnyGetter());
        Assert.assertEquals("any", singleColl.getAnyGetter().getName());

        POJOPropertiesCollector multiColl = collectorFor(MultipleAnyGetterBean.class, true);
        try {
            multiColl.getAnyGetter();
            Assert.fail("Expected IllegalArgumentException for multiple @JsonAnyGetter");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Multiple 'any-getters' defined"));
        }
    }

    @Test
    public void testSingleAndMultipleAnySetterMethod() {
        POJOPropertiesCollector singleColl = collectorFor(SingleAnySetterMethodBean.class, false);
        Assert.assertNotNull(singleColl.getAnySetterMethod());
        Assert.assertEquals("set", singleColl.getAnySetterMethod().getName());

        POJOPropertiesCollector multiColl = collectorFor(MultipleAnySetterMethodBean.class, false);
        try {
            multiColl.getAnySetterMethod();
            Assert.fail("Expected IllegalArgumentException for multiple @JsonAnySetter methods");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Multiple 'any-setters' defined"));
        }
    }

    @Test
    public void testSingleAndMultipleAnySetterField() {
        POJOPropertiesCollector singleColl = collectorFor(SingleAnySetterFieldBean.class, false);
        Assert.assertNotNull(singleColl.getAnySetterField());
        Assert.assertEquals("map", singleColl.getAnySetterField().getName());

        POJOPropertiesCollector multiColl = collectorFor(MultipleAnySetterFieldBean.class, false);
        try {
            multiColl.getAnySetterField();
            Assert.fail("Expected IllegalArgumentException for multiple @JsonAnySetter fields");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Multiple 'any-Setters' defined"));
        }
    }

    @Test
    public void testConstructorsAndFactories() {
        POJOPropertiesCollector coll = collectorFor(CreatorBean.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        Assert.assertEquals(2, props.size());

        POJOPropertiesCollector factColl = collectorFor(FactoryCreatorBean.class, false);
        List<BeanPropertyDefinition> factProps = factColl.getProperties();
        Assert.assertEquals(1, factProps.size());
        Assert.assertEquals("x", factProps.get(0).getName());
    }

    @Test
    public void testInjectables() {
        POJOPropertiesCollector coll = collectorFor(InjectBean.class, false);
        Map<Object, AnnotatedMember> injectables = coll.getInjectables();
        Assert.assertNotNull(injectables);
        Assert.assertEquals(2, injectables.size());
        Assert.assertTrue(injectables.containsKey("injectId"));
        Assert.assertTrue(injectables.containsKey("injectMethodId"));
    }

    @Test
    public void testDuplicateInjectablesFail() {
        POJOPropertiesCollector coll = collectorFor(DuplicateInjectBean.class, false);
        try {
            coll.getInjectables();
            Assert.fail("Expected IllegalArgumentException for duplicate injectable id");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Duplicate injectable value with id"));
        }
    }

    @Test
    public void testPropertySortingExplicitAndAlphabetic() {
        POJOPropertiesCollector ordColl = collectorFor(OrderedBean.class, true);
        List<BeanPropertyDefinition> ordProps = ordColl.getProperties();
        Assert.assertEquals("z", ordProps.get(0).getName());
        Assert.assertEquals("a", ordProps.get(1).getName());
        Assert.assertEquals("m", ordProps.get(2).getName());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        POJOPropertiesCollector alphaColl = collectorFor(mapper, AlphabeticBean.class, true, null);
        List<BeanPropertyDefinition> alphaProps = alphaColl.getProperties();
        Assert.assertEquals("a", alphaProps.get(0).getName());
        Assert.assertEquals("b", alphaProps.get(1).getName());
        Assert.assertEquals("c", alphaProps.get(2).getName());
    }

    @Test
    public void testNamingStrategy() {
        POJOPropertiesCollector coll = collectorFor(SnakeNamingBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        Set<String> names = new HashSet<String>();
        for (BeanPropertyDefinition prop : props) {
            names.add(prop.getName());
        }
        Assert.assertTrue(names.contains("first_name"));
        Assert.assertTrue(names.contains("last_name"));

        POJOPropertiesCollector deserColl = collectorFor(SnakeNamingBean.class, false);
        List<BeanPropertyDefinition> deserProps = deserColl.getProperties();
        Set<String> deserNames = new HashSet<String>();
        for (BeanPropertyDefinition prop : deserProps) {
            deserNames.add(prop.getName());
        }
        Assert.assertTrue(deserNames.contains("middle_name"));
    }

    @Test
    public void testDefaultNamingStrategyIgnored() {
        POJOPropertiesCollector coll = collectorFor(DefaultNamingStrategyBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        Assert.assertEquals(1, props.size());
        Assert.assertEquals("someValue", props.get(0).getName());
    }

    @Test
    public void testInvalidNamingStrategyNonClass() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InvalidNamingAnnotationIntrospector(12345));
        POJOPropertiesCollector coll = collectorFor(mapper, SimpleBean.class, true, null);
        try {
            coll.getProperties();
            Assert.fail("Expected IllegalStateException for non-class naming strategy");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("expected type PropertyNamingStrategy or Class<PropertyNamingStrategy>"));
        }
    }

    @Test
    public void testInvalidNamingStrategyWrongClass() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InvalidNamingAnnotationIntrospector(String.class));
        POJOPropertiesCollector coll = collectorFor(mapper, SimpleBean.class, true, null);
        try {
            coll.getProperties();
            Assert.fail("Expected IllegalStateException for invalid naming strategy class");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("expected Class<PropertyNamingStrategy>"));
        }
    }

    @Test
    public void testHandlerInstantiatorNamingStrategy() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new InvalidNamingAnnotationIntrospector(PropertyNamingStrategy.SnakeCaseStrategy.class));
        final PropertyNamingStrategy customStrategy = new PropertyNamingStrategy.LowerCaseStrategy();
        mapper.setHandlerInstantiator(new HandlerInstantiator() {
            @Override
            public PropertyNamingStrategy namingStrategyInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
                return customStrategy;
            }
            @Override
            public com.fasterxml.jackson.databind.JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) { return null; }
            @Override
            public com.fasterxml.jackson.databind.JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) { return null; }
            @Override
            public com.fasterxml.jackson.databind.KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) { return null; }
            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) { return null; }
        });
        POJOPropertiesCollector coll = collectorFor(mapper, SimpleBean.class, true, null);
        List<BeanPropertyDefinition> props = coll.getProperties();
        Assert.assertFalse(props.isEmpty());
    }

    @Test
    public void testRenamedExplicitProperties() {
        POJOPropertiesCollector coll = collectorFor(RenamedExplicitBean.class, true);
        List<BeanPropertyDefinition> props = coll.getProperties();
        Set<String> names = new HashSet<String>();
        for (BeanPropertyDefinition prop : props) {
            names.add(prop.getName());
        }
        Assert.assertTrue(names.contains("customName"));
        Assert.assertTrue(names.contains("emptyNameField"));
        Assert.assertTrue(names.contains("customMethod"));
        Assert.assertTrue(names.contains("emptyRenamed"));
    }

    @Test
    public void testFinalFieldsPruningForDeserialization() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS);
        POJOPropertiesCollector deserColl = collectorFor(mapper, FinalFieldBean.class, false, null);
        List<BeanPropertyDefinition> deserProps = deserColl.getProperties();
        Assert.assertEquals(1, deserProps.size());
        Assert.assertEquals("normal", deserProps.get(0).getName());

        POJOPropertiesCollector serColl = collectorFor(mapper, FinalFieldBean.class, true, null);
        List<BeanPropertyDefinition> serProps = serColl.getProperties();
        Assert.assertEquals(2, serProps.size());
    }

    @Test
    public void testIgnoredProperties() {
        POJOPropertiesCollector deserColl = collectorFor(IgnoredBean.class, false);
        deserColl.getProperties();
        Set<String> ignored = deserColl.getIgnoredPropertyNames();
        Assert.assertNotNull(ignored);
        Assert.assertTrue(ignored.contains("ignoredField"));
        Assert.assertTrue(ignored.contains("ignoredGetter"));
    }

    @Test
    public void testMutatorPrefixCustomization() {
        POJOPropertiesCollector coll = collectorFor(new ObjectMapper(), BuilderTestBean.class, false, "with");
        List<BeanPropertyDefinition> props = coll.getProperties();
        Assert.assertEquals(1, props.size());
        Assert.assertEquals("x", props.get(0).getName());
    }

    @Test
    public void testNonStaticInnerClassCreatorsIgnored() {
        POJOPropertiesCollector coll = collectorFor(NonStaticInnerBean.class, false);
        List<BeanPropertyDefinition> props = coll.getProperties();
        Assert.assertTrue(props.isEmpty());
    }

    @Test
    public void testNullAnnotationIntrospector() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(null);
        POJOPropertiesCollector coll = collectorFor(mapper, SimpleBean.class, true, null);
        Assert.assertNull(coll.getAnnotationIntrospector());
        Assert.assertNull(coll.getObjectIdInfo());
        Assert.assertNull(coll.getAnyGetter());
        Assert.assertNull(coll.getAnySetterMethod());
        Assert.assertNull(coll.getAnySetterField());
        List<BeanPropertyDefinition> props = coll.getProperties();
        Assert.assertEquals(2, props.size());
    }

    @Test
    public void testEmptyWrapperRenaming() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME);
        POJOPropertiesCollector coll = collectorFor(mapper, SimpleBean.class, true, null);
        List<BeanPropertyDefinition> props = coll.getProperties();
        Assert.assertEquals(2, props.size());
    }
}
