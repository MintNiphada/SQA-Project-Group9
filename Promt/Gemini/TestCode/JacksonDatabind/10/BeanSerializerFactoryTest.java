package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeId;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

public class BeanSerializerFactoryTest {

    // Helper classes for testing
    public static class SimpleBean {
        private String name;
        private int age;

        public SimpleBean() {}

        public SimpleBean(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
    }

    public static class EmptyBean {
    }

    @JsonRootName("annotatedEmpty")
    public static class AnnotatedEmptyBean {
    }

    public static class AnyGetterBean {
        private final Map<String, Object> map = new HashMap<String, Object>();

        public void add(String key, Object value) {
            map.put(key, value);
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return map;
        }
    }

    @JsonIgnoreProperties({"hidden"})
    public static class IgnoredPropBean {
        public String visible = "visible";
        public String hidden = "hidden";
    }

    @JsonIgnoreType
    public static class IgnoredType {
        public String data = "ignored";
    }

    public static class BeanWithIgnoredTypeField {
        public String name = "test";
        public IgnoredType ignored = new IgnoredType();
    }

    public static class SetterlessGetterBean {
        public String getName() { return "name"; }
        public String getReadOnly() { return "readOnly"; }
        public void setName(String name) {}
    }

    public static class ViewBean {
        public interface View1 {}
        public interface View2 {}

        @JsonView(View1.class)
        public String field1 = "v1";

        @JsonView(View2.class)
        public String field2 = "v2";

        public String field3 = "v3";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class PropertyIdBean {
        public int id;
        public String name;

        public PropertyIdBean() {}
        public PropertyIdBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    public static class IntSequenceIdBean {
        public String name;
        public IntSequenceIdBean(String name) {
            this.name = name;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "missingId")
    public static class InvalidPropertyIdBean {
        public String name = "bad";
    }

    public static class CustomFieldSerializer extends StdSerializer<String> implements ResolvableSerializer {
        private boolean resolved = false;

        public CustomFieldSerializer() {
            super(String.class);
        }

        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value + "_custom");
        }

        @Override
        public void resolve(SerializerProvider provider) throws JsonMappingException {
            this.resolved = true;
        }
    }

    public static class FieldAnnotatedBean {
        @JsonSerialize(using = CustomFieldSerializer.class)
        public String text = "hello";
    }

    @JsonSerialize(using = ClassSerializer.class)
    public static class ClassAnnotatedBean {
        public String text = "hello";
    }

    public static class ClassSerializer extends StdSerializer<ClassAnnotatedBean> {
        public ClassSerializer() {
            super(ClassAnnotatedBean.class);
        }

        @Override
        public void serialize(ClassAnnotatedBean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString("class_serialized");
        }
    }

    public static class ConverterBean {
        public String value;

        public ConverterBean(String value) {
            this.value = value;
        }
    }

    public static class TestConverter extends StdConverter<ConverterBean, String> {
        @Override
        public String convert(ConverterBean value) {
            return value.value + "_converted";
        }
    }

    @JsonSerialize(converter = TestConverter.class)
    public static class AnnotatedConverterBean {
        public String value = "raw";
    }

    public static class TypeIdBean {
        @JsonTypeId
        public String typeId = "customType";
        public String value = "val";
    }

    public static class ParentBean {
        public String name = "parent";
        @JsonManagedReference
        public ChildBean child;
    }

    public static class ChildBean {
        public String childName = "child";
        @JsonBackReference
        public ParentBean parent;
    }

    public static class PolymorphicContainerBean {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
        public List<Object> items = new ArrayList<Object>();
    }

    public static class SubtypeFactory extends BeanSerializerFactory {
        public SubtypeFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    // TESTS

    @Test
    public void testSingletonAndWithConfig() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        Assert.assertNotNull(factory);

        SerializerFactoryConfig config = new SerializerFactoryConfig();
        SerializerFactory f2 = factory.withConfig(config);
        Assert.assertNotNull(f2);
        Assert.assertNotSame(factory, f2);

        SerializerFactory f3 = f2.withConfig(config);
        Assert.assertSame(f2, f3);

        SubtypeFactory subFactory = new SubtypeFactory(null);
        try {
            subFactory.withConfig(new SerializerFactoryConfig());
            Assert.fail("Expected IllegalStateException for un-overridden withConfig in subclass");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("Subtype of BeanSerializerFactory"));
        }
    }

    @Test
    public void testIsPotentialBeanType() {
        BeanSerializerFactory factory = BeanSerializerFactory.instance;
        Assert.assertTrue(factory.isPotentialBeanType(SimpleBean.class));
        Assert.assertFalse(factory.isPotentialBeanType(int.class));
        Assert.assertFalse(factory.isPotentialBeanType(String[].class));
    }

    @Test
    public void testFindBeanSerializerNonBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType intType = mapper.constructType(int.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(intType);

        JsonSerializer<Object> ser = BeanSerializerFactory.instance.findBeanSerializer(prov, intType, desc);
        Assert.assertNull(ser);

        JavaType enumType = mapper.constructType(RetentionPolicy.class);
        BeanDescription enumDesc = mapper.getSerializationConfig().introspect(enumType);
        JsonSerializer<Object> enumSer = BeanSerializerFactory.instance.findBeanSerializer(prov, enumType, enumDesc);
        Assert.assertNotNull(enumSer);
    }

    @Test
    public void testConstructBeanSerializerForObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType objType = mapper.constructType(Object.class);
        BeanDescription desc = mapper.getSerializationConfig().introspect(objType);

        JsonSerializer<Object> ser = BeanSerializerFactory.instance.constructBeanSerializer(prov, desc);
        Assert.assertNotNull(ser);
        Assert.assertEquals(prov.getUnknownTypeSerializer(Object.class).getClass(), ser.getClass());
    }

    @Test
    public void testSimpleBeanSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleBean bean = new SimpleBean("Alice", 30);
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"name\":\"Alice\""));
        Assert.assertTrue(json.contains("\"age\":30"));
    }

    @Test
    public void testClassSerializerAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ClassAnnotatedBean bean = new ClassAnnotatedBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertEquals("\"class_serialized\"", json);
    }

    @Test
    public void testFieldCustomSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        FieldAnnotatedBean bean = new FieldAnnotatedBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertEquals("{\"text\":\"hello_custom\"}", json);
    }

    @Test
    public void testConverterSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedConverterBean bean = new AnnotatedConverterBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertEquals("\"raw_converted\"", json);
    }

    @Test
    public void testAnyGetterSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AnyGetterBean bean = new AnyGetterBean();
        bean.add("k1", "v1");
        bean.add("k2", 123);
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"k1\":\"v1\""));
        Assert.assertTrue(json.contains("\"k2\":123"));
    }

    @Test
    public void testIgnoredProperties() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IgnoredPropBean bean = new IgnoredPropBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"visible\":\"visible\""));
        Assert.assertFalse(json.contains("hidden"));
    }

    @Test
    public void testIgnoredType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanWithIgnoredTypeField bean = new BeanWithIgnoredTypeField();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"name\":\"test\""));
        Assert.assertFalse(json.contains("ignored"));
    }

    @Test
    public void testRequireSettersForGetters() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        SetterlessGetterBean bean = new SetterlessGetterBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"name\":\"name\""));
        Assert.assertFalse(json.contains("readOnly"));
    }

    @Test
    public void testViewsInclusion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ViewBean bean = new ViewBean();

        // Default view inclusion TRUE
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        String json1 = mapper.writerWithView(ViewBean.View1.class).writeValueAsString(bean);
        Assert.assertTrue(json1.contains("\"field1\":\"v1\""));
        Assert.assertFalse(json1.contains("\"field2\":\"v2\""));
        Assert.assertTrue(json1.contains("\"field3\":\"v3\""));

        // Default view inclusion FALSE
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        String json2 = mapper.writerWithView(ViewBean.View1.class).writeValueAsString(bean);
        Assert.assertTrue(json2.contains("\"field1\":\"v1\""));
        Assert.assertFalse(json2.contains("\"field2\":\"v2\""));
        Assert.assertFalse(json2.contains("\"field3\":\"v3\""));

        // No active view, view inclusion default true
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
        String json3 = mapper.writeValueAsString(bean);
        Assert.assertTrue(json3.contains("field1"));
        Assert.assertTrue(json3.contains("field2"));
        Assert.assertTrue(json3.contains("field3"));
    }

    @Test
    public void testPropertyBasedObjectId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyIdBean bean = new PropertyIdBean(101, "TestProp");
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"id\":101"));
        Assert.assertTrue(json.contains("\"name\":\"TestProp\""));
    }

    @Test
    public void testIntSequenceObjectId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        IntSequenceIdBean bean = new IntSequenceIdBean("SequenceTest");
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"@id\":1"));
        Assert.assertTrue(json.contains("\"name\":\"SequenceTest\""));
    }

    @Test
    public void testInvalidPropertyIdThrows() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValueAsString(new InvalidPropertyIdBean());
            Assert.fail("Expected exception for invalid object id property name");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("can not find property with name 'missingId'"));
        }
    }

    @Test
    public void testBackReferenceOmission() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ParentBean parent = new ParentBean();
        ChildBean child = new ChildBean();
        parent.child = child;
        child.parent = parent;

        String json = mapper.writeValueAsString(parent);
        Assert.assertTrue(json.contains("\"name\":\"parent\""));
        Assert.assertTrue(json.contains("\"child\":{"));
        Assert.assertTrue(json.contains("\"childName\":\"child\""));
        Assert.assertFalse(json.contains("\"parent\":"));
    }

    @Test
    public void testTypeIdAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeIdBean bean = new TypeIdBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"typeId\":\"customType\""));
        Assert.assertTrue(json.contains("\"value\":\"val\""));
    }

    @Test
    public void testEmptyBeanAndAnnotatedEmptyBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        String emptyJson = mapper.writeValueAsString(new EmptyBean());
        Assert.assertEquals("{}", emptyJson);

        String annotatedEmptyJson = mapper.writeValueAsString(new AnnotatedEmptyBean());
        Assert.assertEquals("{}", annotatedEmptyJson);
    }

    @Test
    public void testSerializerModifiers() throws Exception {
        BeanSerializerModifier modifier = new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                return beanProperties;
            }

            @Override
            public List<BeanPropertyWriter> orderProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                List<BeanPropertyWriter> reversed = new ArrayList<BeanPropertyWriter>(beanProperties);
                Collections.reverse(reversed);
                return reversed;
            }

            @Override
            public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
                return builder;
            }

            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                return serializer;
            }
        };

        SerializerFactoryConfig config = new SerializerFactoryConfig().withSerializerModifier(modifier);
        BeanSerializerFactory factory = (BeanSerializerFactory) BeanSerializerFactory.instance.withConfig(config);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializerFactory(factory);

        SimpleBean bean = new SimpleBean("Bob", 25);
        String json = mapper.writeValueAsString(bean);
        int ageIdx = json.indexOf("\"age\"");
        int nameIdx = json.indexOf("\"name\"");
        Assert.assertTrue(ageIdx < nameIdx);
    }

    @Test
    public void testCustomSerializersInFactory() throws Exception {
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(SimpleBean.class, new JsonSerializer<SimpleBean>() {
            @Override
            public void serialize(SimpleBean value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString("custom_simple_bean:" + value.getName());
            }
        });

        SerializerFactoryConfig config = new SerializerFactoryConfig().withAdditionalSerializers(serializers);
        BeanSerializerFactory factory = (BeanSerializerFactory) BeanSerializerFactory.instance.withConfig(config);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializerFactory(factory);

        SimpleBean bean = new SimpleBean("Charlie", 40);
        String json = mapper.writeValueAsString(bean);
        Assert.assertEquals("\"custom_simple_bean:Charlie\"", json);
    }

    @Test
    public void testContainerTypesStaticTyping() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_STATIC_TYPING);

        List<String> list = Arrays.asList("a", "b");
        String json = mapper.writeValueAsString(list);
        Assert.assertEquals("[\"a\",\"b\"]", json);
    }

    @Test
    public void testFindPropertyTypeSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType stringType = mapper.constructType(String.class);

        TypeSerializer typeSer = BeanSerializerFactory.instance.findPropertyTypeSerializer(stringType, config, null);
        Assert.assertNull(typeSer);
    }

    @Test
    public void testFindPropertyContentTypeSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        CollectionType colType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);

        TypeSerializer contentTypeSer = BeanSerializerFactory.instance.findPropertyContentTypeSerializer(colType, config, null);
        Assert.assertNull(contentTypeSer);
    }
}
