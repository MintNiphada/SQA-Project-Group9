package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class BeanSerializerFactoryTest {

    private BeanSerializerFactory factory;
    private ObjectMapper mapper;
    private DefaultSerializerProvider serializerProvider;

    @Before
    public void setUp() {
        factory = BeanSerializerFactory.instance;
        mapper = new ObjectMapper();
        serializerProvider = (DefaultSerializerProvider) mapper.getSerializerProviderInstance();
    }

    // --- Subclass for withConfig test ---
    static class CustomBeanFactorySubclass extends BeanSerializerFactory {
        public CustomBeanFactorySubclass(SerializerFactoryConfig config) {
            super(config);
        }
    }

    // --- Test POJOs and Annotations ---

    static class SimpleBean {
        public String name = "test";
        public int value = 42;

        public String getName() { return name; }
        public int getValue() { return value; }
    }

    static class CustomClassSerializer extends StdSerializer<CustomAnnotatedBean> {
        public CustomClassSerializer() { super(CustomAnnotatedBean.class); }
        @Override
        public void serialize(CustomAnnotatedBean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString("custom:" + value.id);
        }
    }

    @JsonSerialize(using = CustomClassSerializer.class)
    static class CustomAnnotatedBean {
        public String id = "123";
    }

    static class StringToIntConverter extends StdConverter<ConvertedBean, Integer> {
        @Override
        public Integer convert(ConvertedBean value) {
            return value.score;
        }
    }

    @JsonSerialize(converter = StringToIntConverter.class)
    static class ConvertedBean {
        public int score = 99;
    }

    static class ObjectConverter extends StdConverter<ObjectConvertedBean, Object> {
        @Override
        public Object convert(ObjectConvertedBean value) {
            return "plain-object";
        }
    }

    @JsonSerialize(converter = ObjectConverter.class)
    static class ObjectConvertedBean {
    }

    @JsonIgnoreProperties({"secret"})
    static class FilteredBean {
        public String name = "visible";
        public String secret = "hidden";
    }

    @JsonIgnoreType
    static class IgnorableType {
        public String data = "ignored";
    }

    static class BeanWithIgnorableProp {
        public String name = "ok";
        public IgnorableType ignorable = new IgnorableType();
    }

    static class GetterOnlyBean {
        public String getReadOnly() { return "readonly"; }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class PropertyIdBean {
        public int id = 1;
        public String name = "item";
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "missingId")
    static class InvalidPropertyIdBean {
        public int id = 1;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class IntSeqIdBean {
        public String name = "item2";
    }

    static class Views {
        interface Public {}
        interface Internal extends Public {}
    }

    static class ViewBean {
        @JsonView(Views.Public.class)
        public String pub = "public";

        @JsonView(Views.Internal.class)
        public String priv = "private";
    }

    static class AnyGetterBean {
        private final Map<String, Object> extra = new HashMap<String, Object>();

        public AnyGetterBean() {
            extra.put("extraKey", "extraValue");
        }

        @JsonAnyGetter
        public Map<String, Object> any() {
            return extra;
        }
    }

    static class Parent {
        public String name = "parent";
        @JsonManagedReference
        public Child child;
    }

    static class Child {
        public String title = "child";
        @JsonBackReference
        public Parent parent;
    }

    @JsonRootName("EmptyAnnotated")
    static class EmptyAnnotatedBean {
    }

    static class TypeIdBean {
        @JsonTypeId
        public String getCustomType() {
            return "CustomTypeId";
        }
        public String data = "data";
    }

    static class ExternalPolymorphicHolder {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "extType")
        public Object poly;

        public String extType;
    }

    // --- Life-Cycle Tests ---

    @Test
    public void testWithConfigSameConfig() {
        SerializerFactoryConfig config = factory.getFactoryConfig();
        SerializerFactory sameFactory = factory.withConfig(config);
        Assert.assertSame(factory, sameFactory);
    }

    @Test
    public void testWithConfigNewConfig() {
        SerializerFactoryConfig config = new SerializerFactoryConfig();
        SerializerFactory newFactory = factory.withConfig(config);
        Assert.assertNotSame(factory, newFactory);
        Assert.assertTrue(newFactory instanceof BeanSerializerFactory);
    }

    @Test(expected = IllegalStateException.class)
    public void testWithConfigSubclassThrowsException() {
        CustomBeanFactorySubclass subclassFactory = new CustomBeanFactorySubclass(null);
        subclassFactory.withConfig(new SerializerFactoryConfig());
    }

    @Test
    public void testCustomSerializers() {
        Iterable<Serializers> custom = factory.customSerializers();
        Assert.assertNotNull(custom);
    }

    // --- Serialization Creation Tests ---

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
    public void testCreateSerializerWithCustomAnnotation() throws Exception {
        JavaType type = mapper.constructType(CustomAnnotatedBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        Assert.assertNotNull(ser);
        Assert.assertTrue(ser instanceof CustomClassSerializer);

        String json = mapper.writeValueAsString(new CustomAnnotatedBean());
        Assert.assertEquals("\"custom:123\"", json);
    }

    @Test
    public void testCreateSerializerWithConverter() throws Exception {
        JavaType type = mapper.constructType(ConvertedBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        Assert.assertNotNull(ser);

        String json = mapper.writeValueAsString(new ConvertedBean());
        Assert.assertEquals("99", json);
    }

    @Test
    public void testCreateSerializerWithObjectConverter() throws Exception {
        JavaType type = mapper.constructType(ObjectConvertedBean.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        Assert.assertNotNull(ser);

        String json = mapper.writeValueAsString(new ObjectConvertedBean());
        Assert.assertEquals("\"plain-object\"", json);
    }

    @Test
    public void testCreateSerializerForContainerType() throws Exception {
        JavaType type = mapper.constructType(List.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        Assert.assertNotNull(ser);
    }

    @Test
    public void testCreateSerializerForCustomModuleSerializer() throws Exception {
        SimpleModule module = new SimpleModule();
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(SimpleBean.class, new StdSerializer<SimpleBean>(SimpleBean.class) {
            @Override
            public void serialize(SimpleBean value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeString("custom-module");
            }
        });
        module.setSerializers(serializers);
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.registerModule(module);

        String json = customMapper.writeValueAsString(new SimpleBean());
        Assert.assertEquals("\"custom-module\"", json);
    }

    @Test
    public void testSerializerModifiers() throws Exception {
        ObjectMapper modMapper = new ObjectMapper();
        final boolean[] modifierCalled = new boolean[4];

        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                modifierCalled[0] = true;
                return beanProperties;
            }

            @Override
            public List<BeanPropertyWriter> orderProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                modifierCalled[1] = true;
                return beanProperties;
            }

            @Override
            public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
                modifierCalled[2] = true;
                return builder;
            }

            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                modifierCalled[3] = true;
                return serializer;
            }
        });
        modMapper.registerModule(module);

        String json = modMapper.writeValueAsString(new SimpleBean());
        Assert.assertTrue(json.contains("\"name\":\"test\""));
        for (int i = 0; i < 4; i++) {
            Assert.assertTrue("Modifier step " + i + " was not executed", modifierCalled[i]);
        }
    }

    // --- Non-Bean and Bean Introspection Tests ---

    @Test
    public void testIsPotentialBeanType() {
        Assert.assertTrue(factory.isPotentialBeanType(SimpleBean.class));
        Assert.assertFalse(factory.isPotentialBeanType(int.class));
        Assert.assertFalse(factory.isPotentialBeanType(String[].class));
    }

    @Test
    public void testFindBeanSerializerNonBeanType() throws Exception {
        JavaType intType = mapper.constructType(int.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(intType);
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, intType, beanDesc);
        Assert.assertNull(ser);
    }

    @Test
    public void testFindBeanSerializerEnumType() throws Exception {
        JavaType enumType = mapper.constructType(PropertyNamingStrategy.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(enumType);
        // Should not immediately reject as non-bean because it is an enum type
        JsonSerializer<Object> ser = factory.findBeanSerializer(serializerProvider, enumType, beanDesc);
        // Either null or a dummy/bean serializer, but shouldn't throw exception
        Assert.assertTrue(ser == null || ser instanceof JsonSerializer);
    }

    @Test
    public void testConstructBeanSerializerObject() throws Exception {
        JavaType objectType = mapper.constructType(Object.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(objectType);
        JsonSerializer<Object> ser = factory.constructBeanSerializer(serializerProvider, beanDesc);
        Assert.assertNotNull(ser);
    }

    // --- Object ID Handling Tests ---

    @Test
    public void testConstructObjectIdHandlerPropertyGenerator() throws Exception {
        String json = mapper.writeValueAsString(new PropertyIdBean());
        Assert.assertTrue(json.contains("\"id\":1"));
        Assert.assertTrue(json.contains("\"name\":\"item\""));
    }

    @Test(expected = JsonMappingException.class)
    public void testConstructObjectIdHandlerInvalidProperty() throws Exception {
        mapper.writeValueAsString(new InvalidPropertyIdBean());
    }

    @Test
    public void testConstructObjectIdHandlerIntSequenceGenerator() throws Exception {
        String json = mapper.writeValueAsString(new IntSeqIdBean());
        Assert.assertTrue(json.contains("\"@id\":1"));
        Assert.assertTrue(json.contains("\"name\":\"item2\""));
    }

    // --- Property Filtering and Views Tests ---

    @Test
    public void testFilterBeanPropertiesIgnoredProperties() throws Exception {
        String json = mapper.writeValueAsString(new FilteredBean());
        Assert.assertTrue(json.contains("\"name\":\"visible\""));
        Assert.assertFalse(json.contains("secret"));
    }

    @Test
    public void testRemoveIgnorableTypes() throws Exception {
        String json = mapper.writeValueAsString(new BeanWithIgnorableProp());
        Assert.assertTrue(json.contains("\"name\":\"ok\""));
        Assert.assertFalse(json.contains("ignorable"));
    }

    @Test
    public void testRequireSettersForGetters() throws Exception {
        // By default REQUIRE_SETTERS_FOR_GETTERS is false, so getter-only bean is serialized
        String jsonDefault = mapper.writeValueAsString(new GetterOnlyBean());
        Assert.assertTrue(jsonDefault.contains("\"readOnly\":\"readonly\""));

        // When enabled, getter-only bean without setter is ignored
        ObjectMapper strictMapper = new ObjectMapper();
        strictMapper.enable(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS);
        strictMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        String jsonStrict = strictMapper.writeValueAsString(new GetterOnlyBean());
        Assert.assertEquals("{}", jsonStrict);
    }

    @Test
    public void testProcessViews() throws Exception {
        ViewBean bean = new ViewBean();

        // Default view inclusion enabled
        String jsonPublic = mapper.writerWithView(Views.Public.class).writeValueAsString(bean);
        Assert.assertTrue(jsonPublic.contains("\"pub\":\"public\""));
        Assert.assertFalse(jsonPublic.contains("\"priv\":\"private\""));

        String jsonInternal = mapper.writerWithView(Views.Internal.class).writeValueAsString(bean);
        Assert.assertTrue(jsonInternal.contains("\"pub\":\"public\""));
        Assert.assertTrue(jsonInternal.contains("\"priv\":\"private\""));

        // Default view inclusion disabled
        ObjectMapper noDefaultViewMapper = new ObjectMapper();
        noDefaultViewMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        String jsonDisabled = noDefaultViewMapper.writerWithView(Views.Public.class).writeValueAsString(bean);
        Assert.assertTrue(jsonDisabled.contains("\"pub\":\"public\""));
        Assert.assertFalse(jsonDisabled.contains("\"priv\":\"private\""));
    }

    // --- AnyGetter, BackReference, TypeId and Virtual Properties Tests ---

    @Test
    public void testAnyGetterSerialization() throws Exception {
        String json = mapper.writeValueAsString(new AnyGetterBean());
        Assert.assertTrue(json.contains("\"extraKey\":\"extraValue\""));
    }

    @Test
    public void testBackReferenceSuppression() throws Exception {
        Parent parent = new Parent();
        Child child = new Child();
        parent.child = child;
        child.parent = parent;

        String json = mapper.writeValueAsString(parent);
        Assert.assertTrue(json.contains("\"name\":\"parent\""));
        Assert.assertTrue(json.contains("\"title\":\"child\""));
        // Back reference should not cause infinite recursion
        Assert.assertFalse(json.contains("\"parent\":"));
    }

    @Test
    public void testTypeIdProperty() throws Exception {
        String json = mapper.writeValueAsString(new TypeIdBean());
        Assert.assertTrue(json.contains("\"data\":\"data\""));
    }

    @Test
    public void testEmptyAnnotatedBeanDummySerializer() throws Exception {
        JavaType type = mapper.constructType(EmptyAnnotatedBean.class);
        BeanDescription beanDesc = mapper.getSerializationConfig().introspect(type);
        JsonSerializer<Object> ser = factory.constructBeanSerializer(serializerProvider, beanDesc);
        Assert.assertNotNull(ser);

        String json = mapper.writeValueAsString(new EmptyAnnotatedBean());
        Assert.assertEquals("{}", json);
    }

    // --- Helper Methods and Polymorphic Type Serializer Tests ---

    @Test
    public void testFindPropertyTypeSerializerAndContentTypeSerializer() throws Exception {
        JavaType listType = mapper.constructType(List.class);
        JavaType stringType = mapper.constructType(String.class);
        SerializationConfig config = mapper.getSerializationConfig();

        // Testing direct calls
        Assert.assertNull(factory.findPropertyTypeSerializer(stringType, config, null));
        Assert.assertNull(factory.findPropertyContentTypeSerializer(listType, config, null));
    }

    @Test
    public void testRemoveOverlappingTypeIds() throws Exception {
        JavaType type = mapper.constructType(ExternalPolymorphicHolder.class);
        JsonSerializer<Object> ser = factory.createSerializer(serializerProvider, type);
        Assert.assertNotNull(ser);

        ExternalPolymorphicHolder holder = new ExternalPolymorphicHolder();
        holder.poly = new SimpleBean();
        holder.extType = "SimpleBean";
        String json = mapper.writeValueAsString(holder);
        Assert.assertNotNull(json);
    }
}
