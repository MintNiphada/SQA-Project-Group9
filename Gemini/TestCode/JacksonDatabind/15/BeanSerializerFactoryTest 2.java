package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeId;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class BeanSerializerFactoryTest {

    private BeanSerializerFactory factory;
    private ObjectMapper mapper;
    private SerializerProvider serializerProvider;

    @Before
    public void setUp() {
        factory = BeanSerializerFactory.instance;
        mapper = new ObjectMapper();
        serializerProvider = mapper.getSerializerProviderInstance();
    }

    // --- Subclass for withConfig test ---
    static class CustomSubFactory extends BeanSerializerFactory {
        public CustomSubFactory(SerializerFactoryConfig config) {
            super(config);
        }
    }

    // --- Test POJOs and Helpers ---

    public static class SimpleBean {
        public String name = "test";
        public int value = 42;

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    @JsonIgnoreProperties({"secret"})
    public static class IgnoredPropBean {
        public String visible = "ok";
        public String secret = "hidden";
    }

    @JsonIgnoreType
    public static class IgnoredType {
        public String text = "skip";
    }

    public static class BeanWithIgnoredTypeProp {
        public String name = "safe";
        public IgnoredType ignored = new IgnoredType();
    }

    public static class SetterlessBean {
        private String field1 = "readonly";
        public String getField1() { return field1; }
    }

    public static class SetterlessWithExplicit {
        private String explicit = "explicit";
        private String implicit = "implicit";

        @JsonProperty("explicit")
        public String getExplicit() { return explicit; }
        public String getImplicit() { return implicit; }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static class PolymorphicPropertyBean {
        public BasePoly child = new SubPoly();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    public static class PolymorphicContainerBean {
        public List<BasePoly> children = Collections.singletonList(new SubPoly());
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
    @JsonSubTypes({@JsonSubTypes.Type(value = SubPoly.class, name = "sub")})
    public static abstract class BasePoly {}
    public static class SubPoly extends BasePoly {
        public int num = 123;
    }

    public static class Views {
        public interface PublicView {}
        public interface InternalView {}
    }

    public static class ViewBean {
        @JsonView(Views.PublicView.class)
        public String pub = "public";

        @JsonView(Views.InternalView.class)
        public String priv = "internal";

        public String untyped = "untyped";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class PropertyIdBean {
        public int id = 100;
        public String name = "idBean";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "nonExistent")
    public static class InvalidPropertyIdBean {
        public int id = 100;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "seq")
    public static class IntSequenceIdBean {
        public String payload = "data";
    }

    public static class AnyGetterBean {
        private Map<String, Object> map = new HashMap<String, Object>();

        public AnyGetterBean() {
            map.put("k1", "v1");
            map.put("k2", 2);
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return map;
        }
    }

    public static class CustomAnyMapSerializer extends StdSerializer<Map<String, Object>> {
        public CustomAnyMapSerializer() { super(Map.class, false); }
        @Override
        public void serialize(Map<String, Object> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("customAny", "customValue");
            gen.writeEndObject();
        }
    }

    public static class AnnotatedAnyGetterBean {
        private Map<String, Object> map = new HashMap<String, Object>();
        public AnnotatedAnyGetterBean() { map.put("foo", "bar"); }

        @JsonAnyGetter
        @JsonSerialize(using = CustomAnyMapSerializer.class)
        public Map<String, Object> any() { return map; }
    }

    public static class TypeIdBean {
        @JsonTypeId
        public String getTypeId() {
            return "customType";
        }
        public String getName() {
            return "name";
        }
    }

    public static class BackRefParent {
        public String name = "parent";
        @JsonManagedReference
        public BackRefChild child = new BackRefChild(this);
    }

    public static class BackRefChild {
        @JsonBackReference
        public BackRefParent parent;
        public String value = "childVal";

        public BackRefChild(BackRefParent parent) {
            this.parent = parent;
        }
    }

    @JsonSerialize(using = CustomClassSerializer.class)
    public static class AnnotatedClassBean {
        public String text = "text";
    }

    public static class CustomClassSerializer extends StdSerializer<AnnotatedClassBean> {
        public CustomClassSerializer() { super(AnnotatedClassBean.class); }
        @Override
        public void serialize(AnnotatedClassBean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString("custom_serialized");
        }
    }

    public static class CustomFieldSerializer extends StdSerializer<String> {
        public CustomFieldSerializer() { super(String.class); }
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString("field:" + value);
        }
    }

    public static class AnnotatedFieldBean {
        @JsonSerialize(using = CustomFieldSerializer.class)
        public String data = "hello";
    }

    public static class ResolvableFieldSerializer extends StdSerializer<String> implements ResolvableSerializer {
        boolean resolved = false;
        public ResolvableFieldSerializer() { super(String.class); }
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString("resolved=" + resolved + ":" + value);
        }
        @Override
        public void resolve(SerializerProvider provider) {
            resolved = true;
        }
    }

    public static class ResolvablePropBean {
        @JsonSerialize(using = ResolvableFieldSerializer.class)
        public String field = "value";
    }

    @JsonSerialize(converter = StringLengthConverter.class)
    public static class ConvertedBean {
        public String text;
        public ConvertedBean(String text) { this.text = text; }
    }

    public static class StringLengthConverter extends StdConverter<ConvertedBean, Integer> {
        @Override
        public Integer convert(ConvertedBean value) {
            return value.text == null ? 0 : value.text.length();
        }
    }

    @JsonSerialize(converter = ConverterToAnnotated.class)
    public static class ConvertedToAnnotatedBean {
        public String msg = "msg";
    }

    public static class ConverterToAnnotated extends StdConverter<ConvertedToAnnotatedBean, AnnotatedClassBean> {
        @Override
        public AnnotatedClassBean convert(ConvertedToAnnotatedBean value) {
            return new AnnotatedClassBean();
        }
    }

    public enum TestEnum {
        A, B, C;
        public String getExtra() { return "extra_" + name(); }
    }

    @JsonSerialize
    public static class EmptyAnnotatedBean {}

    public static class EmptyBean {}

    @JsonFilter("customFilter")
    public static class FilteredBean {
        public String field = "filtered";
    }

    // --- Tests ---

    @Test
    public void testSingletonAndWithConfig() {
        Assert.assertNotNull(BeanSerializerFactory.instance);
        SerializerFactoryConfig config = new SerializerFactoryConfig();
        SerializerFactory newFactory = factory.withConfig(config);
        Assert.assertNotNull(newFactory);
        Assert.assertNotSame(factory, newFactory);
        Assert.assertSame(newFactory, newFactory.withConfig(config));

        CustomSubFactory subFactory = new CustomSubFactory(config);
        try {
            subFactory.withConfig(new SerializerFactoryConfig());
            Assert.fail("Expected IllegalStateException for un-overridden withConfig in subclass");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("has not properly overridden method"));
        }
    }

    @Test
    public void testCreateSerializerStandardBean() throws Exception {
        JavaType type = mapper.constructType(SimpleBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        Assert.assertNotNull(ser);

        String json = mapper.writeValueAsString(new SimpleBean());
        Assert.assertTrue(json.contains("\"name\":\"test\""));
        Assert.assertTrue(json.contains("\"value\":42"));
    }

    @Test
    public void testCreateSerializerObject() throws Exception {
        JavaType type = mapper.constructType(Object.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        Assert.assertNotNull(ser);
        Assert.assertTrue(ser.getClass().getName().contains("UnknownSerializer") 
                || ser.getClass().getName().contains("NullSerializer")
                || ser.getClass().getSimpleName().contains("StdDelegatingSerializer")
                || ser.toString().contains("Unknown"));
    }

    @Test
    public void testClassSerializerAnnotation() throws Exception {
        JavaType type = mapper.constructType(AnnotatedClassBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        Assert.assertNotNull(ser);
        Assert.assertTrue(ser instanceof CustomClassSerializer);

        String json = mapper.writeValueAsString(new AnnotatedClassBean());
        Assert.assertEquals("\"custom_serialized\"", json);
    }

    @Test
    public void testConverterAnnotation() throws Exception {
        ConvertedBean bean = new ConvertedBean("Hello world");
        String json = mapper.writeValueAsString(bean);
        Assert.assertEquals("11", json);

        ConvertedToAnnotatedBean bean2 = new ConvertedToAnnotatedBean();
        String json2 = mapper.writeValueAsString(bean2);
        Assert.assertEquals("\"custom_serialized\"", json2);
    }

    @Test
    public void testCustomModuleSerializer() throws Exception {
        SimpleSerializers serializers = new SimpleSerializers();
        JsonSerializer<SimpleBean> customSer = new StdSerializer<SimpleBean>(SimpleBean.class) {
            @Override
            public void serialize(SimpleBean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString("customSimpleBean");
            }
        };
        serializers.addSerializer(SimpleBean.class, customSer);
        SerializerFactory customFactory = factory.withAdditionalSerializers(serializers);

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setSerializerFactory(customFactory);

        String json = customMapper.writeValueAsString(new SimpleBean());
        Assert.assertEquals("\"customSimpleBean\"", json);
    }

    @Test
    public void testSerializerModifier() throws Exception {
        final boolean[] modifierFlags = new boolean[4];

        BeanSerializerModifier modifier = new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                modifierFlags[0] = true;
                return super.changeProperties(config, beanDesc, beanProperties);
            }

            @Override
            public List<BeanPropertyWriter> orderProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                modifierFlags[1] = true;
                return super.orderProperties(config, beanDesc, beanProperties);
            }

            @Override
            public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
                modifierFlags[2] = true;
                return super.updateBuilder(config, beanDesc, builder);
            }

            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                modifierFlags[3] = true;
                return super.modifySerializer(config, beanDesc, serializer);
            }
        };

        SerializerFactory customFactory = factory.withSerializerModifier(modifier);
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setSerializerFactory(customFactory);

        String json = customMapper.writeValueAsString(new SimpleBean());
        Assert.assertNotNull(json);
        Assert.assertTrue(modifierFlags[0]);
        Assert.assertTrue(modifierFlags[1]);
        Assert.assertTrue(modifierFlags[2]);
        Assert.assertTrue(modifierFlags[3]);
    }

    @Test
    public void testFilterBeanPropertiesIgnored() throws Exception {
        String json = mapper.writeValueAsString(new IgnoredPropBean());
        Assert.assertTrue(json.contains("\"visible\":\"ok\""));
        Assert.assertFalse(json.contains("secret"));
    }

    @Test
    public void testRemoveIgnorableTypes() throws Exception {
        String json = mapper.writeValueAsString(new BeanWithIgnoredTypeProp());
        Assert.assertTrue(json.contains("\"name\":\"safe\""));
        Assert.assertFalse(json.contains("ignored"));
    }

    @Test
    public void testRequireSettersForGetters() throws Exception {
        ObjectMapper requireSettersMapper = new ObjectMapper();
        requireSettersMapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);

        try {
            requireSettersMapper.writeValueAsString(new SetterlessBean());
            Assert.fail("Expected exception due to empty properties with REQUIRE_SETTERS_FOR_GETTERS");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("No serializer found for class"));
        }

        String json = requireSettersMapper.writeValueAsString(new SetterlessWithExplicit());
        Assert.assertTrue(json.contains("\"explicit\":\"explicit\""));
        Assert.assertFalse(json.contains("implicit"));
    }

    @Test
    public void testViewsProcessing() throws Exception {
        ViewBean bean = new ViewBean();

        // Default view inclusion = true (default)
        String pubJson = mapper.writerWithView(Views.PublicView.class).writeValueAsString(bean);
        Assert.assertTrue(pubJson.contains("\"pub\":\"public\""));
        Assert.assertTrue(pubJson.contains("\"untyped\":\"untyped\""));
        Assert.assertFalse(pubJson.contains("\"priv\":\"internal\""));

        // Default view inclusion = false
        ObjectMapper noDefaultViewMapper = new ObjectMapper();
        noDefaultViewMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

        String noDefaultJson = noDefaultViewMapper.writerWithView(Views.PublicView.class).writeValueAsString(bean);
        Assert.assertTrue(noDefaultJson.contains("\"pub\":\"public\""));
        Assert.assertFalse(noDefaultJson.contains("\"untyped\":\"untyped\""));
        Assert.assertFalse(noDefaultJson.contains("\"priv\":\"internal\""));
    }

    @Test
    public void testObjectIdPropertyGenerator() throws Exception {
        PropertyIdBean bean = new PropertyIdBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"id\":100"));
        Assert.assertTrue(json.contains("\"name\":\"idBean\""));
    }

    @Test
    public void testInvalidObjectIdPropertyGenerator() {
        try {
            mapper.writeValueAsString(new InvalidPropertyIdBean());
            Assert.fail("Expected IllegalArgumentException or JsonMappingException");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("Invalid Object Id definition"));
        }
    }

    @Test
    public void testObjectIdSequenceGenerator() throws Exception {
        IntSequenceIdBean bean = new IntSequenceIdBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"seq\":1"));
        Assert.assertTrue(json.contains("\"payload\":\"data\""));
    }

    @Test
    public void testAnyGetterSerialization() throws Exception {
        AnyGetterBean bean = new AnyGetterBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"k1\":\"v1\""));
        Assert.assertTrue(json.contains("\"k2\":2"));
    }

    @Test
    public void testAnnotatedAnyGetterSerialization() throws Exception {
        AnnotatedAnyGetterBean bean = new AnnotatedAnyGetterBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"customAny\":\"customValue\""));
    }

    @Test
    public void testTypeIdProperty() throws Exception {
        TypeIdBean bean = new TypeIdBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"name\":\"name\""));
    }

    @Test
    public void testBackReferenceOmission() throws Exception {
        BackRefParent parent = new BackRefParent();
        String json = mapper.writeValueAsString(parent);
        Assert.assertTrue(json.contains("\"name\":\"parent\""));
        Assert.assertTrue(json.contains("\"child\":{\"value\":\"childVal\"}"));
        Assert.assertFalse(json.contains("\"parent\":"));
    }

    @Test
    public void testResolvableFieldSerializer() throws Exception {
        ResolvablePropBean bean = new ResolvablePropBean();
        String json = mapper.writeValueAsString(bean);
        Assert.assertTrue(json.contains("\"field\":\"resolved=true:value\""));
    }

    @Test
    public void testPolymorphicPropertyAndContainer() throws Exception {
        PolymorphicPropertyBean polyProp = new PolymorphicPropertyBean();
        String json1 = mapper.writeValueAsString(polyProp);
        Assert.assertTrue(json1.contains("\"type\":\"sub\""));
        Assert.assertTrue(json1.contains("\"num\":123"));

        PolymorphicContainerBean polyCont = new PolymorphicContainerBean();
        String json2 = mapper.writeValueAsString(polyCont);
        Assert.assertTrue(json2.contains("\"type\":\"sub\""));
        Assert.assertTrue(json2.contains("\"num\":123"));
    }

    @Test
    public void testFindBeanSerializerForEnum() throws Exception {
        JavaType enumType = mapper.constructType(TestEnum.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(enumType);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, enumType, beanDesc);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testEmptyAnnotatedBeanProducesDummySerializer() throws Exception {
        JavaType emptyAnnotatedType = mapper.constructType(EmptyAnnotatedBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, emptyAnnotatedType);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testEmptyBeanThrowsMappingException() {
        try {
            mapper.writeValueAsString(new EmptyBean());
            Assert.fail("Expected JsonMappingException for EmptyBean with FAIL_ON_EMPTY_BEANS enabled");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("No serializer found for class"));
        }
    }

    @Test
    public void testIsPotentialBeanType() {
        Assert.assertTrue(factory.isPotentialBeanType(SimpleBean.class));
        Assert.assertFalse(factory.isPotentialBeanType(int.class));
        Assert.assertFalse(factory.isPotentialBeanType(int[].class));
        Assert.assertFalse(factory.isPotentialBeanType(String[].class));
    }

    @Test
    public void testFilterId() throws Exception {
        SimpleFilterProvider filters = new SimpleFilterProvider();
        filters.addFilter("customFilter", com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter.filterOutAllExcept("field"));
        ObjectMapper filterMapper = new ObjectMapper();
        filterMapper.setFilterProvider(filters);

        String json = filterMapper.writeValueAsString(new FilteredBean());
        Assert.assertTrue(json.contains("\"field\":\"filtered\""));
    }

    @Test
    public void testFindPropertyTypeSerializerAndContentTypeSerializer() throws Exception {
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, BasePoly.class);
        JavaType baseType = mapper.constructType(BasePoly.class);

        BeanDescription desc = mapper.getSerializationConfig().introspect(mapper.constructType(PolymorphicContainerBean.class));
        AnnotatedMember member = null;
        for (BeanPropertyDefinition prop : desc.findProperties()) {
            if ("children".equals(prop.getName())) {
                member = prop.getAccessor();
                break;
            }
        }
        Assert.assertNotNull(member);

        TypeSerializer propTypeSer = factory.findPropertyTypeSerializer(listType, mapper.getSerializationConfig(), member);
        TypeSerializer contentSer = factory.findPropertyContentTypeSerializer(listType, mapper.getSerializationConfig(), member);
        Assert.assertNotNull(contentSer);
    }

    @Test
    public void testCustomPropertyBuilderAndBeanSerializerBuilder() {
        BeanDescription desc = mapper.getSerializationConfig().introspect(mapper.constructType(SimpleBean.class));
        PropertyBuilder pb = factory.constructPropertyBuilder(mapper.getSerializationConfig(), desc);
        Assert.assertNotNull(pb);

        BeanSerializerBuilder bsb = factory.constructBeanSerializerBuilder(desc);
        Assert.assertNotNull(bsb);
        Assert.assertEquals(desc, bsb.getBeanDescription());
    }
}
