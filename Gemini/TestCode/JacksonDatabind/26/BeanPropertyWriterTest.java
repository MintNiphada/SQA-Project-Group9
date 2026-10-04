package com.fasterxml.jackson.databind.ser;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanPropertyWriterTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestAnn {
        String value() default "";
    }

    public static class SampleBean {
        @TestAnn("fieldVal")
        public String textField = "fieldData";
        public Integer intField = 42;
        public SampleBean selfRef;
        public List<String> listField = new ArrayList<String>();

        public String getTextMethod() {
            return textField;
        }

        public void setTextMethod(String s) {
            this.textField = s;
        }
    }

    public static class SubBeanPropertyWriter extends BeanPropertyWriter {
        public SubBeanPropertyWriter() {
            super();
        }

        public SubBeanPropertyWriter(BeanPropertyWriter base) {
            super(base);
        }

        public SubBeanPropertyWriter(BeanPropertyWriter base, PropertyName name) {
            super(base, name);
        }

        public SubBeanPropertyWriter(BeanPropertyWriter base, SerializedString name) {
            super(base, name);
        }

        @Override
        public void _depositSchemaProperty(ObjectNode propertiesNode, JsonNode schemaNode) {
            super._depositSchemaProperty(propertiesNode, schemaNode);
        }
    }

    private ObjectMapper mapper;
    private JavaType stringType;
    private JavaType beanType;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        stringType = TypeFactory.defaultInstance().constructType(String.class);
        beanType = TypeFactory.defaultInstance().constructType(SampleBean.class);
    }

    private BeanPropertyWriter buildFieldWriter(String propName, String fieldName, JsonSerializer<?> ser,
                                                TypeSerializer typeSer, JavaType serType,
                                                boolean suppressNulls, Object suppressableValue) throws Exception {
        Field field = SampleBean.class.getField(fieldName);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SampleBean.class, mapper.getDeserializationConfig());
        AnnotationMap annMap = new AnnotationMap();
        for (Annotation a : field.getDeclaredAnnotations()) {
            annMap.add(a);
        }
        AnnotatedField annotatedField = new AnnotatedField(ac, field, annMap);

        PropertyMetadata md = PropertyMetadata.STD_OPTIONAL;
        BeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
                mapper.getSerializationConfig(), annotatedField, new PropertyName(propName), md, JsonInclude.Include.ALWAYS
        );

        return new BeanPropertyWriter(
                propDef, annotatedField, annMap,
                stringType, ser, typeSer, serType,
                suppressNulls, suppressableValue
        );
    }

    private BeanPropertyWriter buildMethodWriter(String propName, String methodName, JsonSerializer<?> ser,
                                                 TypeSerializer typeSer, JavaType serType,
                                                 boolean suppressNulls, Object suppressableValue) throws Exception {
        Method method = SampleBean.class.getMethod(methodName);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SampleBean.class, mapper.getDeserializationConfig());
        AnnotationMap annMap = new AnnotationMap();
        for (Annotation a : method.getDeclaredAnnotations()) {
            annMap.add(a);
        }
        AnnotatedMethod annotatedMethod = new AnnotatedMethod(ac, method, annMap, null);

        PropertyMetadata md = PropertyMetadata.STD_REQUIRED;
        BeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
                mapper.getSerializationConfig(), annotatedMethod, new PropertyName(propName), md, JsonInclude.Include.ALWAYS
        );

        return new BeanPropertyWriter(
                propDef, annotatedMethod, annMap,
                stringType, ser, typeSer, serType,
                suppressNulls, suppressableValue
        );
    }

    @Test
    public void testDefaultConstructorAndVirtualState() {
        SubBeanPropertyWriter writer = new SubBeanPropertyWriter();
        Assert.assertNull(writer.getName());
        Assert.assertNull(writer.getFullName());
        Assert.assertNull(writer.getType());
        Assert.assertNull(writer.getWrapperName());
        Assert.assertNull(writer.getMember());
        Assert.assertNull(writer.getAnnotation(TestAnn.class));
        Assert.assertNull(writer.getContextAnnotation(TestAnn.class));
        Assert.assertFalse(writer.hasSerializer());
        Assert.assertFalse(writer.hasNullSerializer());
        Assert.assertNull(writer.getTypeSerializer());
        Assert.assertFalse(writer.isUnwrapping());
        Assert.assertFalse(writer.willSuppressNulls());
        Assert.assertFalse(writer.isVirtual());
        Assert.assertNull(writer.getSerializationType());
        Assert.assertNull(writer.getRawSerializationType());
        Assert.assertNull(writer.getGenericPropertyType());
        Assert.assertNull(writer.getViews());
        Assert.assertTrue(writer.toString().contains("virtual"));
    }

    @Test
    public void testFieldAndMethodConstructors() throws Exception {
        BeanPropertyWriter fieldWriter = buildFieldWriter("textField", "textField", null, null, null, false, null);
        Assert.assertEquals("textField", fieldWriter.getName());
        Assert.assertEquals(new PropertyName("textField"), fieldWriter.getFullName());
        Assert.assertEquals(String.class, fieldWriter.getPropertyType());
        Assert.assertEquals(String.class, fieldWriter.getGenericPropertyType());
        Assert.assertNotNull(fieldWriter.getMember());
        Assert.assertNotNull(fieldWriter.getAnnotation(TestAnn.class));
        Assert.assertEquals("fieldVal", fieldWriter.getAnnotation(TestAnn.class).value());
        Assert.assertFalse(fieldWriter.isRequired());
        Assert.assertEquals(PropertyMetadata.STD_OPTIONAL, fieldWriter.getMetadata());
        Assert.assertTrue(fieldWriter.toString().contains("field"));

        BeanPropertyWriter methodWriter = buildMethodWriter("textMethod", "getTextMethod", null, null, stringType, true, null);
        Assert.assertEquals("textMethod", methodWriter.getName());
        Assert.assertEquals(String.class, methodWriter.getPropertyType());
        Assert.assertEquals(String.class, methodWriter.getGenericPropertyType());
        Assert.assertEquals(stringType, methodWriter.getSerializationType());
        Assert.assertEquals(String.class, methodWriter.getRawSerializationType());
        Assert.assertTrue(methodWriter.isRequired());
        Assert.assertTrue(methodWriter.willSuppressNulls());
        Assert.assertTrue(methodWriter.toString().contains("via method"));
    }

    @Test
    public void testCopyConstructors() throws Exception {
        BeanPropertyWriter base = buildFieldWriter("prop", "textField", null, null, stringType, false, null);
        base.setInternalSetting("key1", "val1");

        SubBeanPropertyWriter copy1 = new SubBeanPropertyWriter(base);
        Assert.assertEquals("prop", copy1.getName());
        Assert.assertEquals("val1", copy1.getInternalSetting("key1"));

        SubBeanPropertyWriter copy2 = new SubBeanPropertyWriter(base, new PropertyName("renamedProp"));
        Assert.assertEquals("renamedProp", copy2.getName());
        Assert.assertEquals("val1", copy2.getInternalSetting("key1"));

        SubBeanPropertyWriter copy3 = new SubBeanPropertyWriter(base, new SerializedString("serializedRenamed"));
        Assert.assertEquals("serializedRenamed", copy3.getName());
        Assert.assertEquals("val1", copy3.getInternalSetting("key1"));
    }

    @Test
    public void testInternalSettingsManagement() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("prop", "textField", null, null, null, false, null);
        Assert.assertNull(writer.getInternalSetting("nonExisting"));
        Assert.assertNull(writer.removeInternalSetting("nonExisting"));

        writer.setInternalSetting("k1", "v1");
        writer.setInternalSetting("k2", "v2");
        Assert.assertEquals("v1", writer.getInternalSetting("k1"));
        Assert.assertEquals("v2", writer.getInternalSetting("k2"));

        Object oldVal = writer.setInternalSetting("k1", "v1_updated");
        Assert.assertEquals("v1", oldVal);
        Assert.assertEquals("v1_updated", writer.getInternalSetting("k1"));

        Assert.assertEquals("v1_updated", writer.removeInternalSetting("k1"));
        Assert.assertNull(writer.getInternalSetting("k1"));
        Assert.assertEquals("v2", writer.removeInternalSetting("k2"));
        Assert.assertNull(writer.getInternalSetting("k2"));
        Assert.assertNull(writer.removeInternalSetting("k2"));
    }

    @Test
    public void testAssignSerializers() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("prop", "textField", null, null, null, false, null);
        Assert.assertFalse(writer.hasSerializer());
        Assert.assertFalse(writer.hasNullSerializer());

        JsonSerializer<Object> ser = NullSerializer.instance;
        writer.assignSerializer(ser);
        Assert.assertTrue(writer.hasSerializer());
        Assert.assertSame(ser, writer.getSerializer());

        // Assign same serializer is allowed
        writer.assignSerializer(ser);

        try {
            writer.assignSerializer(new JsonSerializer<Object>() {
                @Override
                public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}
            });
            Assert.fail("Expected IllegalStateException on overriding serializer");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("Can not override serializer"));
        }

        JsonSerializer<Object> nullSer = NullSerializer.instance;
        writer.assignNullSerializer(nullSer);
        Assert.assertTrue(writer.hasNullSerializer());
        writer.assignNullSerializer(nullSer);

        try {
            writer.assignNullSerializer(new JsonSerializer<Object>() {
                @Override
                public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}
            });
            Assert.fail("Expected IllegalStateException on overriding null serializer");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("Can not override null serializer"));
        }
    }

    @Test
    public void testAssignTypeSerializerAndNonTrivialBaseType() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("prop", "textField", null, null, null, false, null);
        Assert.assertNull(writer.getTypeSerializer());
        TypeSerializer mockTypeSer = mapper.getSerializationConfig().getDefaultTyper(stringType) != null ?
                mapper.getSerializationConfig().getDefaultTyper(stringType).buildTypeSerializer(mapper.getSerializationConfig(), stringType, Collections.emptyList()) : null;

        writer.assignTypeSerializer(mockTypeSer);
        Assert.assertSame(mockTypeSer, writer.getTypeSerializer());

        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        writer.setNonTrivialBaseType(listType);
    }

    @Test
    public void testRenameAndUnwrapping() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("prop", "textField", null, null, null, false, null);
        NameTransformer noOpTransformer = NameTransformer.NOP;
        BeanPropertyWriter same = writer.rename(noOpTransformer);
        Assert.assertSame(writer, same);

        NameTransformer prefixTransformer = NameTransformer.simpleTransformer("pre_", "");
        BeanPropertyWriter renamed = writer.rename(prefixTransformer);
        Assert.assertNotSame(writer, renamed);
        Assert.assertEquals("pre_prop", renamed.getName());

        BeanPropertyWriter unwrapping = writer.unwrappingWriter(prefixTransformer);
        Assert.assertNotNull(unwrapping);
        Assert.assertTrue(unwrapping.isUnwrapping());
    }

    @Test
    public void testReadResolve() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("textField", "textField", null, null, null, false, null);
        Object resolved = writer.readResolve();
        Assert.assertSame(writer, resolved);
        Assert.assertNotNull(writer.getPropertyType());

        BeanPropertyWriter methodWriter = buildMethodWriter("textMethod", "getTextMethod", null, null, null, false, null);
        Object resolvedMethod = methodWriter.readResolve();
        Assert.assertSame(methodWriter, resolvedMethod);
        Assert.assertNotNull(methodWriter.getPropertyType());
    }

    @Test
    public void testFindFormatOverrides() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("prop", "textField", null, null, null, false, null);
        AnnotationIntrospector intr = mapper.getSerializationConfig().getAnnotationIntrospector();
        JsonFormat.Value format = writer.findFormatOverrides(intr);
        Assert.assertNull(format);

        // Call again to verify cached NO_FORMAT path
        JsonFormat.Value format2 = writer.findFormatOverrides(intr);
        Assert.assertNull(format2);

        // Null introspector lookup
        BeanPropertyWriter writer2 = buildFieldWriter("prop2", "textField", null, null, null, false, null);
        Assert.assertNull(writer2.findFormatOverrides(null));
    }

    @Test
    public void testWouldConflictWithName() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("propName", "textField", null, null, null, false, null);
        Assert.assertTrue(writer.wouldConflictWithName(new PropertyName("propName")));
        Assert.assertFalse(writer.wouldConflictWithName(new PropertyName("otherName")));
        Assert.assertFalse(writer.wouldConflictWithName(new PropertyName("propName", "http://namespace")));

        // Wrapper name conflict check
        AnnotatedField field = new AnnotatedField(
                AnnotatedClass.constructWithoutSuperTypes(SampleBean.class, mapper.getDeserializationConfig()),
                SampleBean.class.getField("textField"), new AnnotationMap()
        );
        BeanPropertyDefinition propWithWrapper = SimpleBeanPropertyDefinition.construct(
                mapper.getSerializationConfig(), field, new PropertyName("propName"),
                PropertyMetadata.STD_OPTIONAL, JsonInclude.Include.ALWAYS
        ).withWrapperName(new PropertyName("wrapperProp", "ns"));
        BeanPropertyWriter wrapperWriter = new BeanPropertyWriter(
                propWithWrapper, field, new AnnotationMap(), stringType, null, null, null, false, null
        );
        Assert.assertEquals(new PropertyName("wrapperProp", "ns"), wrapperWriter.getWrapperName());
        Assert.assertTrue(wrapperWriter.wouldConflictWithName(new PropertyName("wrapperProp", "ns")));
        Assert.assertFalse(wrapperWriter.wouldConflictWithName(new PropertyName("propName")));
    }

    @Test
    public void testGetAndSerializeAsField() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("textField", "textField", null, null, null, false, null);
        SampleBean bean = new SampleBean();
        bean.textField = "helloWorld";
        Assert.assertEquals("helloWorld", writer.get(bean));

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        writer.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();

        Assert.assertEquals("{\"textField\":\"helloWorld\"}", sw.toString());
    }

    @Test
    public void testSerializeAsFieldWithNullValue() throws Exception {
        SampleBean bean = new SampleBean();
        bean.textField = null;

        // 1. Without null serializer: nothing written
        BeanPropertyWriter writerNoNullSer = buildFieldWriter("textField", "textField", null, null, null, false, null);
        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = mapper.getFactory().createGenerator(sw1);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        gen1.writeStartObject();
        writerNoNullSer.serializeAsField(bean, gen1, prov);
        gen1.writeEndObject();
        gen1.close();
        Assert.assertEquals("{}", sw1.toString());

        // 2. With null serializer: field with null written
        BeanPropertyWriter writerWithNullSer = buildFieldWriter("textField", "textField", null, null, null, false, null);
        writerWithNullSer.assignNullSerializer(NullSerializer.instance);
        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = mapper.getFactory().createGenerator(sw2);

        gen2.writeStartObject();
        writerWithNullSer.serializeAsField(bean, gen2, prov);
        gen2.writeEndObject();
        gen2.close();
        Assert.assertEquals("{\"textField\":null}", sw2.toString());
    }

    @Test
    public void testSerializeAsFieldSuppressEmptyAndDefault() throws Exception {
        SampleBean bean = new SampleBean();
        bean.textField = "";

        BeanPropertyWriter emptySuppressWriter = buildFieldWriter("textField", "textField", null, null, null, false, BeanPropertyWriter.MARKER_FOR_EMPTY);
        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = mapper.getFactory().createGenerator(sw1);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        gen1.writeStartObject();
        emptySuppressWriter.serializeAsField(bean, gen1, prov);
        gen1.writeEndObject();
        gen1.close();
        Assert.assertEquals("{}", sw1.toString());

        BeanPropertyWriter valSuppressWriter = buildFieldWriter("textField", "textField", null, null, null, false, "suppressedVal");
        bean.textField = "suppressedVal";
        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = mapper.getFactory().createGenerator(sw2);

        gen2.writeStartObject();
        valSuppressWriter.serializeAsField(bean, gen2, prov);
        gen2.writeEndObject();
        gen2.close();
        Assert.assertEquals("{}", sw2.toString());
    }

    @Test
    public void testSerializeAsOmittedField() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("textField", "textField", null, null, null, false, null);
        SampleBean bean = new SampleBean();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // Default generator can omit fields (returns true for canOmitFields)
        writer.serializeAsOmittedField(bean, gen, prov);
        gen.close();
        Assert.assertEquals("", sw.toString());
    }

    @Test
    public void testSerializeAsElement() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("textField", "textField", null, null, null, false, null);
        SampleBean bean = new SampleBean();
        bean.textField = "elemVal";

        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = mapper.getFactory().createGenerator(sw1);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        gen1.writeStartArray();
        writer.serializeAsElement(bean, gen1, prov);
        gen1.writeEndArray();
        gen1.close();
        Assert.assertEquals("[\"elemVal\"]", sw1.toString());

        // Null value without null serializer -> outputs null in array
        bean.textField = null;
        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = mapper.getFactory().createGenerator(sw2);
        gen2.writeStartArray();
        writer.serializeAsElement(bean, gen2, prov);
        gen2.writeEndArray();
        gen2.close();
        Assert.assertEquals("[null]", sw2.toString());

        // Null value with null serializer -> outputs null
        writer.assignNullSerializer(NullSerializer.instance);
        StringWriter sw3 = new StringWriter();
        JsonGenerator gen3 = mapper.getFactory().createGenerator(sw3);
        gen3.writeStartArray();
        writer.serializeAsElement(bean, gen3, prov);
        gen3.writeEndArray();
        gen3.close();
        Assert.assertEquals("[null]", sw3.toString());
    }

    @Test
    public void testSerializeAsElementSuppressions() throws Exception {
        SampleBean bean = new SampleBean();
        bean.textField = "";
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // Marker for empty
        BeanPropertyWriter emptySuppressWriter = buildFieldWriter("textField", "textField", null, null, null, false, BeanPropertyWriter.MARKER_FOR_EMPTY);
        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = mapper.getFactory().createGenerator(sw1);
        gen1.writeStartArray();
        emptySuppressWriter.serializeAsElement(bean, gen1, prov);
        gen1.writeEndArray();
        gen1.close();
        Assert.assertEquals("[null]", sw1.toString());

        // Specific suppressable value
        BeanPropertyWriter valSuppressWriter = buildFieldWriter("textField", "textField", null, null, null, false, "suppressMe");
        bean.textField = "suppressMe";
        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = mapper.getFactory().createGenerator(sw2);
        gen2.writeStartArray();
        valSuppressWriter.serializeAsElement(bean, gen2, prov);
        gen2.writeEndArray();
        gen2.close();
        Assert.assertEquals("[null]", sw2.toString());
    }

    @Test
    public void testSerializeAsPlaceholder() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("textField", "textField", null, null, null, false, null);
        SampleBean bean = new SampleBean();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = mapper.getFactory().createGenerator(sw1);
        gen1.writeStartArray();
        writer.serializeAsPlaceholder(bean, gen1, prov);
        gen1.writeEndArray();
        gen1.close();
        Assert.assertEquals("[null]", sw1.toString());

        writer.assignNullSerializer(NullSerializer.instance);
        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = mapper.getFactory().createGenerator(sw2);
        gen2.writeStartArray();
        writer.serializeAsPlaceholder(bean, gen2, prov);
        gen2.writeEndArray();
        gen2.close();
        Assert.assertEquals("[null]", sw2.toString());
    }

    @Test
    public void testDirectSelfReferenceCycleDetection() throws Exception {
        Field field = SampleBean.class.getField("selfRef");
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SampleBean.class, mapper.getDeserializationConfig());
        AnnotatedField annotatedField = new AnnotatedField(ac, field, new AnnotationMap());

        PropertyMetadata md = PropertyMetadata.STD_OPTIONAL;
        BeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
                mapper.getSerializationConfig(), annotatedField, new PropertyName("selfRef"), md, JsonInclude.Include.ALWAYS
        );

        BeanPropertyWriter writer = new BeanPropertyWriter(
                propDef, annotatedField, new AnnotationMap(),
                beanType, null, null, beanType, false, null
        );

        SampleBean bean = new SampleBean();
        bean.selfRef = bean;

        SerializerProvider prov = mapper.getSerializerProviderInstance();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        // By default FAIL_ON_SELF_REFERENCES is enabled in SerializerProvider
        try {
            gen.writeStartObject();
            writer.serializeAsField(bean, gen, prov);
            Assert.fail("Expected JsonMappingException for self-reference cycle");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Direct self-reference leading to cycle"));
        }

        try {
            gen.writeStartArray();
            writer.serializeAsElement(bean, gen, prov);
            Assert.fail("Expected JsonMappingException for self-reference cycle");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Direct self-reference leading to cycle"));
        }
    }

    @Test
    public void testDepositSchemaPropertyVisitors() throws Exception {
        BeanPropertyWriter requiredWriter = buildMethodWriter("textMethod", "getTextMethod", null, null, null, false, null);
        BeanPropertyWriter optionalWriter = buildFieldWriter("textField", "textField", null, null, null, false, null);

        final List<String> requiredVisited = new ArrayList<String>();
        final List<String> optionalVisited = new ArrayList<String>();

        JsonObjectFormatVisitor visitor = new JsonObjectFormatVisitor.Base() {
            @Override
            public void property(BeanProperty prop) {
                requiredVisited.add(prop.getName());
            }

            @Override
            public void optionalProperty(BeanProperty prop) {
                optionalVisited.add(prop.getName());
            }
        };

        requiredWriter.depositSchemaProperty(visitor);
        optionalWriter.depositSchemaProperty(visitor);
        requiredWriter.depositSchemaProperty((JsonObjectFormatVisitor) null);

        Assert.assertTrue(requiredVisited.contains("textMethod"));
        Assert.assertTrue(optionalVisited.contains("textField"));
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDepositSchemaPropertyObjectNode() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("textField", "textField", null, null, stringType, false, null);
        ObjectNode propertiesNode = JsonNodeFactory.instance.objectNode();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        writer.depositSchemaProperty(propertiesNode, prov);
        Assert.assertTrue(propertiesNode.has("textField"));

        // Test with custom SubBeanPropertyWriter schema deposition
        SubBeanPropertyWriter subWriter = new SubBeanPropertyWriter(writer);
        ObjectNode customNode = JsonNodeFactory.instance.objectNode();
        subWriter._depositSchemaProperty(customNode, JsonNodeFactory.instance.textNode("dummySchema"));
        Assert.assertEquals("dummySchema", customNode.get("textField").asText());

        // Test with non-schema-aware serializer fallback
        BeanPropertyWriter nonSchemaWriter = buildFieldWriter("textField", "textField", new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}
        }, null, null, false, null);

        ObjectNode nonSchemaProps = JsonNodeFactory.instance.objectNode();
        nonSchemaWriter.depositSchemaProperty(nonSchemaProps, prov);
        Assert.assertTrue(nonSchemaProps.has("textField"));
    }

    @Test
    public void testDynamicSerializerWithSpecializedNonTrivialBaseType() throws Exception {
        Field field = SampleBean.class.getField("listField");
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SampleBean.class, mapper.getDeserializationConfig());
        AnnotatedField annotatedField = new AnnotatedField(ac, field, new AnnotationMap());

        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        PropertyMetadata md = PropertyMetadata.STD_OPTIONAL;
        BeanPropertyDefinition propDef = SimpleBeanPropertyDefinition.construct(
                mapper.getSerializationConfig(), annotatedField, new PropertyName("listField"), md, JsonInclude.Include.ALWAYS
        );

        BeanPropertyWriter writer = new BeanPropertyWriter(
                propDef, annotatedField, new AnnotationMap(),
                listType, null, null, null, false, null
        );
        writer.setNonTrivialBaseType(listType);

        SampleBean bean = new SampleBean();
        bean.listField.add("item1");

        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        gen.writeStartObject();
        writer.serializeAsField(bean, gen, prov);
        gen.writeEndObject();
        gen.close();

        Assert.assertEquals("{\"listField\":[\"item1\"]}", sw.toString());
    }

    @Test
    public void testGetSerializedName() throws Exception {
        BeanPropertyWriter writer = buildFieldWriter("prop", "textField", null, null, null, false, null);
        SerializableString serName = writer.getSerializedName();
        Assert.assertNotNull(serName);
        Assert.assertEquals("prop", serName.getValue());
    }
}
