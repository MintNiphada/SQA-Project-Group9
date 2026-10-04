package com.fasterxml.jackson.databind.ser;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanPropertyWriterTest {

    private static class TestBean {
        public String field;
        private String prop;
        public String getProp() { return prop; }
        public void setProp(String v) { prop = v; }
    }

    private BeanPropertyDefinition propDef;
    private AnnotatedMember member;
    private Annotations contextAnnotations;
    private JavaType declaredType;
    private JsonSerializer<Object> serializer;
    private TypeSerializer typeSerializer;
    private JavaType serType;
    private boolean suppressNulls;
    private Object suppressableValue;
    private BeanPropertyWriter writer;
    private ObjectMapper mapper;
    private SerializerProvider provider;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        provider = mapper.getSerializerProviderInstance();
        declaredType = TypeFactory.defaultInstance().constructType(String.class);
        serType = declaredType;
        serializer = new StringSerializer();
        typeSerializer = null;
        suppressNulls = false;
        suppressableValue = null;
        Field f = TestBean.class.getField("field");
        member = new AnnotatedField(null, f, null);
        contextAnnotations = new AnnotationMap();
        propDef = new com.fasterxml.jackson.databind.introspect.SimpleBeanPropertyDefinition(
                member, "field", null, null, null, null, null, null, null, null);
        writer = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, typeSerializer, serType,
                suppressNulls, suppressableValue);
    }

    @Test
    public void testPublicConstructor() throws Exception {
        assertNotNull(writer);
        assertEquals("field", writer.getName());
        assertNotNull(writer.getSerializedName());
        assertTrue(writer.hasSerializer());
        assertFalse(writer.hasNullSerializer());
        assertNull(writer.getTypeSerializer());
        assertFalse(writer.willSuppressNulls());
        assertNull(writer.getInternalSetting("key"));
    }

    @Test
    public void testPublicConstructorWithNullSerializer() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, null, null, serType, true, "default");
        assertFalse(w.hasSerializer());
        assertTrue(w.willSuppressNulls());
        assertEquals("default", w.getInternalSetting("suppressableValue") == null ? null : w.getInternalSetting("suppressableValue"));
    }

    @Test
    public void testProtectedDefaultConstructor() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter();
        assertNull(w.getName());
        assertNull(w.getSerializedName());
        assertFalse(w.hasSerializer());
        assertFalse(w.hasNullSerializer());
        assertNull(w.getType());
        assertNull(w.getWrapperName());
        assertFalse(w.isRequired());
        assertNull(w.getMetadata());
        assertNull(w.getMember());
        assertNull(w.getAnnotation(Deprecated.class));
        assertNull(w.getContextAnnotation(Deprecated.class));
        assertFalse(w.isVirtual());
        assertFalse(w.willSuppressNulls());
        assertNull(w.getSerializer());
        assertNull(w.getSerializationType());
        assertNull(w.getRawSerializationType());
        assertNull(w.getPropertyType());
        assertNull(w.getGenericPropertyType());
        assertNull(w.getViews());
    }

    @Test
    public void testCopyConstructorWithBase() throws Exception {
        BeanPropertyWriter copy = new BeanPropertyWriter(writer);
        assertEquals(writer.getName(), copy.getName());
        assertEquals(writer.getSerializedName(), copy.getSerializedName());
        assertEquals(writer.getWrapperName(), copy.getWrapperName());
        assertEquals(writer.getType(), copy.getType());
        assertEquals(writer.getMember(), copy.getMember());
        assertEquals(writer.hasSerializer(), copy.hasSerializer());
        assertEquals(writer.hasNullSerializer(), copy.hasNullSerializer());
        assertEquals(writer.willSuppressNulls(), copy.willSuppressNulls());
    }

    @Test
    public void testCopyConstructorWithPropertyName() throws Exception {
        PropertyName newName = PropertyName.construct("newField");
        BeanPropertyWriter copy = new BeanPropertyWriter(writer, newName);
        assertEquals("newField", copy.getName());
        assertEquals(writer.getWrapperName(), copy.getWrapperName());
        assertEquals(writer.getType(), copy.getType());
    }

    @Test
    public void testCopyConstructorWithSerializedString() throws Exception {
        SerializedString newName = new SerializedString("anotherField");
        BeanPropertyWriter copy = new BeanPropertyWriter(writer, newName);
        assertEquals("anotherField", copy.getName());
        assertEquals(writer.getWrapperName(), copy.getWrapperName());
    }

    @Test
    public void testRenameSameName() throws Exception {
        NameTransformer transformer = NameTransformer.simpleTransformer(null, null);
        BeanPropertyWriter result = writer.rename(transformer);
        assertSame(writer, result);
    }

    @Test
    public void testRenameDifferentName() throws Exception {
        NameTransformer transformer = new NameTransformer() {
            @Override
            public String transform(String name) {
                return "renamed_" + name;
            }
        };
        BeanPropertyWriter result = writer.rename(transformer);
        assertNotSame(writer, result);
        assertEquals("renamed_field", result.getName());
    }

    @Test
    public void testAssignTypeSerializer() throws Exception {
        TypeSerializer ts = mapper.getSerializerProviderInstance().findTypeSerializer(declaredType);
        writer.assignTypeSerializer(ts);
        assertSame(ts, writer.getTypeSerializer());
    }

    @Test
    public void testAssignSerializer() throws Exception {
        JsonSerializer<Object> newSer = new StringSerializer();
        writer.assignSerializer(newSer);
        assertSame(newSer, writer.getSerializer());
    }

    @Test(expected = IllegalStateException.class)
    public void testAssignSerializerThrowsOnOverride() throws Exception {
        JsonSerializer<Object> newSer = new StringSerializer();
        writer.assignSerializer(newSer);
        writer.assignSerializer(new StringSerializer());
    }

    @Test
    public void testAssignNullSerializer() throws Exception {
        JsonSerializer<Object> nullSer = new StringSerializer();
        writer.assignNullSerializer(nullSer);
        assertSame(nullSer, writer.getNullSerializer());
    }

    @Test(expected = IllegalStateException.class)
    public void testAssignNullSerializerThrowsOnOverride() throws Exception {
        JsonSerializer<Object> nullSer = new StringSerializer();
        writer.assignNullSerializer(nullSer);
        writer.assignNullSerializer(new StringSerializer());
    }

    @Test
    public void testUnwrappingWriter() throws Exception {
        NameTransformer unwrapper = NameTransformer.simpleTransformer("prefix-", null);
        BeanPropertyWriter unwrapped = writer.unwrappingWriter(unwrapper);
        assertTrue(unwrapped instanceof UnwrappingBeanPropertyWriter);
    }

    @Test
    public void testSetNonTrivialBaseType() throws Exception {
        JavaType t = TypeFactory.defaultInstance().constructType(String.class);
        writer.setNonTrivialBaseType(t);
        assertSame(t, writer.getNonTrivialBaseType());
    }

    @Test
    public void testReadResolveWithField() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, null, null, serType, false, null);
        w.readResolve();
        assertNotNull(w.getField());
        assertNull(w.getAccessorMethod());
        assertNotNull(w.getDynamicSerializers());
    }

    @Test
    public void testReadResolveWithMethod() throws Exception {
        Method m = TestBean.class.getMethod("getProp");
        AnnotatedMethod am = new AnnotatedMethod(null, m, null, null);
        BeanPropertyDefinition def = new com.fasterxml.jackson.databind.introspect.SimpleBeanPropertyDefinition(
                am, "prop", null, null, null, null, null, null, null, null);
        BeanPropertyWriter w = new BeanPropertyWriter(def, am, contextAnnotations,
                declaredType, serializer, null, serType, false, null);
        w.readResolve();
        assertNotNull(w.getAccessorMethod());
        assertNull(w.getField());
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals("field", writer.getName());
    }

    @Test
    public void testGetFullName() throws Exception {
        PropertyName full = writer.getFullName();
        assertEquals("field", full.getSimpleName());
    }

    @Test
    public void testGetType() throws Exception {
        assertSame(declaredType, writer.getType());
    }

    @Test
    public void testGetWrapperName() throws Exception {
        assertNull(writer.getWrapperName());
    }

    @Test
    public void testIsRequired() throws Exception {
        assertFalse(writer.isRequired());
    }

    @Test
    public void testGetMetadata() throws Exception {
        assertNotNull(writer.getMetadata());
    }

    @Test
    public void testGetAnnotationWhenMemberNull() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter();
        assertNull(w.getAnnotation(Deprecated.class));
    }

    @Test
    public void testGetAnnotationWhenMemberNotNull() throws Exception {
        assertNull(writer.getAnnotation(Deprecated.class));
    }

    @Test
    public void testGetContextAnnotationWhenNull() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter();
        assertNull(w.getContextAnnotation(Deprecated.class));
    }

    @Test
    public void testGetContextAnnotationWhenNotNull() throws Exception {
        assertNull(writer.getContextAnnotation(Deprecated.class));
    }

    @Test
    public void testFindFormatOverridesNoIntrospector() throws Exception {
        assertNull(writer.findFormatOverrides(null));
    }

    @Test
    public void testFindFormatOverridesWithIntrospector() throws Exception {
        AnnotationIntrospector intr = mapper.getSerializationConfig().getAnnotationIntrospector();
        assertNull(writer.findFormatOverrides(intr));
    }

    @Test
    public void testGetMember() throws Exception {
        assertSame(member, writer.getMember());
    }

    @Test
    public void testDepositSchemaProperty() throws Exception {
        ObjectNode node = mapper.createObjectNode();
        writer._depositSchemaProperty(node, mapper.createObjectNode().put("type", "string"));
        assertTrue(node.has("field"));
    }

    @Test
    public void testIsVirtual() throws Exception {
        assertFalse(writer.isVirtual());
    }

    @Test
    public void testInternalSettings() throws Exception {
        assertNull(writer.getInternalSetting("key"));
        assertNull(writer.setInternalSetting("key", "value"));
        assertEquals("value", writer.getInternalSetting("key"));
        assertEquals("value", writer.setInternalSetting("key", "newValue"));
        assertEquals("newValue", writer.getInternalSetting("key"));
        assertEquals("newValue", writer.removeInternalSetting("key"));
        assertNull(writer.getInternalSetting("key"));
        assertNull(writer.removeInternalSetting("key"));
    }

    @Test
    public void testRemoveInternalSettingClearsMap() throws Exception {
        writer.setInternalSetting("key", "value");
        writer.removeInternalSetting("key");
        assertNull(writer.getInternalSettingsMap());
    }

    @Test
    public void testGetSerializedName() throws Exception {
        SerializableString name = writer.getSerializedName();
        assertEquals("field", name.getValue());
    }

    @Test
    public void testHasSerializer() throws Exception {
        assertTrue(writer.hasSerializer());
    }

    @Test
    public void testHasNullSerializer() throws Exception {
        assertFalse(writer.hasNullSerializer());
    }

    @Test
    public void testGetTypeSerializer() throws Exception {
        assertNull(writer.getTypeSerializer());
    }

    @Test
    public void testIsUnwrapping() throws Exception {
        assertFalse(writer.isUnwrapping());
    }

    @Test
    public void testWillSuppressNulls() throws Exception {
        assertFalse(writer.willSuppressNulls());
    }

    @Test
    public void testWouldConflictWithNameWrapper() throws Exception {
        PropertyName wrapperName = PropertyName.construct("wrapper");
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, null);
        Field wrapperField = BeanPropertyWriter.class.getDeclaredField("_wrapperName");
        wrapperField.setAccessible(true);
        wrapperField.set(w, wrapperName);
        assertTrue(w.wouldConflictWithName(wrapperName));
        assertFalse(w.wouldConflictWithName(PropertyName.construct("other")));
    }

    @Test
    public void testWouldConflictWithNameNoWrapper() throws Exception {
        assertTrue(writer.wouldConflictWithName(PropertyName.construct("field")));
        assertFalse(writer.wouldConflictWithName(PropertyName.construct("field", "ns")));
        assertFalse(writer.wouldConflictWithName(PropertyName.construct("other")));
    }

    @Test
    public void testGetSerializer() throws Exception {
        assertSame(serializer, writer.getSerializer());
    }

    @Test
    public void testGetSerializationType() throws Exception {
        assertSame(serType, writer.getSerializationType());
    }

    @Test
    public void testGetRawSerializationType() throws Exception {
        assertEquals(String.class, writer.getRawSerializationType());
    }

    @Test
    public void testGetPropertyType() throws Exception {
        assertEquals(String.class, writer.getPropertyType());
    }

    @Test
    public void testGetGenericPropertyType() throws Exception {
        assertEquals(String.class, writer.getGenericPropertyType());
    }

    @Test
    public void testGetViews() throws Exception {
        assertNull(writer.getViews());
    }

    @Test
    public void testSerializeAsFieldNullWithNullSerializer() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, null);
        w.assignNullSerializer(new StringSerializer());
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = null;
        w.serializeAsField(bean, gen, provider);
        gen.close();
        assertEquals("{\"field\":\"\"}", sw.toString());
    }

    @Test
    public void testSerializeAsFieldNullWithoutNullSerializer() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = null;
        writer.serializeAsField(bean, gen, provider);
        gen.close();
        assertEquals("", sw.toString());
    }

    @Test
    public void testSerializeAsFieldNonNullStaticSerializer() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = "value";
        writer.serializeAsField(bean, gen, provider);
        gen.close();
        assertEquals("{\"field\":\"value\"}", sw.toString());
    }

    @Test
    public void testSerializeAsFieldSuppressDefaultValue() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, "default");
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = "default";
        w.serializeAsField(bean, gen, provider);
        gen.close();
        assertEquals("", sw.toString());
    }

    @Test
    public void testSerializeAsFieldSuppressEmpty() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, BeanPropertyWriter.MARKER_FOR_EMPTY);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = "";
        w.serializeAsField(bean, gen, provider);
        gen.close();
        assertEquals("", sw.toString());
    }

    @Test
    public void testSerializeAsFieldSelfReference() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = "self";
        w.serializeAsField(bean, gen, provider);
        gen.close();
        assertEquals("{\"field\":\"self\"}", sw.toString());
    }

    @Test(expected = JsonMappingException.class)
    public void testSerializeAsFieldSelfReferenceFail() throws Exception {
        mapper.enable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        provider = mapper.getSerializerProviderInstance();
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, new BeanSerializerBase(null) {
                    @Override
                    public void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) {}
                }, null, serType, false, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = "self";
        w.serializeAsField(bean, gen, provider);
    }

    @Test
    public void testSerializeAsOmittedFieldCanOmit() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        writer.serializeAsOmittedField(new TestBean(), gen, provider);
        gen.close();
        assertEquals("", sw.toString());
    }

    @Test
    public void testSerializeAsOmittedFieldCannotOmit() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw) {
            @Override
            public boolean canOmitFields() { return false; }
        };
        writer.serializeAsOmittedField(new TestBean(), gen, provider);
        gen.close();
        assertTrue(sw.toString().contains("field"));
    }

    @Test
    public void testSerializeAsElementNullWithNullSerializer() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, null);
        w.assignNullSerializer(new StringSerializer());
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = null;
        w.serializeAsElement(bean, gen, provider);
        gen.close();
        assertEquals("\"\"", sw.toString());
    }

    @Test
    public void testSerializeAsElementNullWithoutNullSerializer() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = null;
        writer.serializeAsElement(bean, gen, provider);
        gen.close();
        assertEquals("null", sw.toString());
    }

    @Test
    public void testSerializeAsElementNonNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = "value";
        writer.serializeAsElement(bean, gen, provider);
        gen.close();
        assertEquals("\"value\"", sw.toString());
    }

    @Test
    public void testSerializeAsElementSuppressDefault() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, "default");
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = "default";
        w.serializeAsElement(bean, gen, provider);
        gen.close();
        assertEquals("null", sw.toString());
    }

    @Test
    public void testSerializeAsElementSuppressEmpty() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, BeanPropertyWriter.MARKER_FOR_EMPTY);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        TestBean bean = new TestBean();
        bean.field = "";
        w.serializeAsElement(bean, gen, provider);
        gen.close();
        assertEquals("null", sw.toString());
    }

    @Test
    public void testSerializeAsPlaceholderWithNullSerializer() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, null);
        w.assignNullSerializer(new StringSerializer());
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        w.serializeAsPlaceholder(new TestBean(), gen, provider);
        gen.close();
        assertEquals("\"\"", sw.toString());
    }

    @Test
    public void testSerializeAsPlaceholderWithoutNullSerializer() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        writer.serializeAsPlaceholder(new TestBean(), gen, provider);
        gen.close();
        assertEquals("null", sw.toString());
    }

    @Test
    public void testDepositSchemaPropertyVisitorRequired() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, null);
        Field metadataField = BeanPropertyWriter.class.getDeclaredField("_metadata");
        metadataField.setAccessible(true);
        metadataField.set(w, PropertyMetadata.STD_REQUIRED);
        JsonObjectFormatVisitor visitor = new JsonObjectFormatVisitor() {
            public void property(BeanProperty prop) { fail("Should not be called"); }
            public void optionalProperty(BeanProperty prop) { fail("Should not be called"); }
            public void property(BeanProperty prop, boolean required) {
                assertTrue(required);
            }
            public void optionalProperty(BeanProperty prop, boolean required) {
                fail("Should not be called");
            }
        };
        w.depositSchemaProperty(visitor);
    }

    @Test
    public void testDepositSchemaPropertyVisitorOptional() throws Exception {
        JsonObjectFormatVisitor visitor = new JsonObjectFormatVisitor() {
            public void property(BeanProperty prop) { fail("Should not be called"); }
            public void optionalProperty(BeanProperty prop) { fail("Should not be called"); }
            public void property(BeanProperty prop, boolean required) {
                fail("Should not be called");
            }
            public void optionalProperty(BeanProperty prop, boolean required) {
                assertFalse(required);
            }
        };
        writer.depositSchemaProperty(visitor);
    }

    @Test
    public void testDepositSchemaPropertyObjectNode() throws Exception {
        ObjectNode node = mapper.createObjectNode();
        writer.depositSchemaProperty(node, provider);
        assertTrue(node.has("field"));
    }

    @Test
    public void testFindAndAddDynamic() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, null, null, serType, false, null);
        PropertySerializerMap map = PropertySerializerMap.emptyForProperties();
        JsonSerializer<Object> ser = w._findAndAddDynamic(map, String.class, provider);
        assertNotNull(ser);
        assertNotNull(w.getDynamicSerializers());
    }

    @Test
    public void testGet() throws Exception {
        TestBean bean = new TestBean();
        bean.field = "test";
        assertEquals("test", writer.get(bean));
    }

    @Test
    public void testHandleSelfReferenceFail() throws Exception {
        mapper.enable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        provider = mapper.getSerializerProviderInstance();
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, new BeanSerializerBase(null) {
                    @Override
                    public void serialize(Object bean, JsonGenerator gen, SerializerProvider provider) {}
                }, null, serType, false, null);
        try {
            w._handleSelfReference(new TestBean(), null, provider, w.getSerializer());
            fail("Expected exception");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Direct self-reference"));
        }
    }

    @Test
    public void testHandleSelfReferenceNotFail() throws Exception {
        mapper.disable(SerializationFeature.FAIL_ON_SELF_REFERENCES);
        provider = mapper.getSerializerProviderInstance();
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, serializer, null, serType, false, null);
        assertFalse(w._handleSelfReference(new TestBean(), null, provider, w.getSerializer()));
    }

    @Test
    public void testToString() throws Exception {
        String str = writer.toString();
        assertTrue(str.contains("field"));
        assertTrue(str.contains("static serializer"));
    }

    @Test
    public void testToStringVirtual() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter();
        String str = w.toString();
        assertTrue(str.contains("virtual"));
        assertTrue(str.contains("no static serializer"));
    }

    @Test
    public void testToStringField() throws Exception {
        BeanPropertyWriter w = new BeanPropertyWriter(propDef, member, contextAnnotations,
                declaredType, null, null, serType, false, null);
        String str = w.toString();
        assertTrue(str.contains("field"));
        assertTrue(str.contains("no static serializer"));
    }

    @Test
    public void testToStringMethod() throws Exception {
        Method m = TestBean.class.getMethod("getProp");
        AnnotatedMethod am = new AnnotatedMethod(null, m, null, null);
        BeanPropertyDefinition def = new com.fasterxml.jackson.databind.introspect.SimpleBeanPropertyDefinition(
                am, "prop", null, null, null, null, null, null, null, null);
        BeanPropertyWriter w = new BeanPropertyWriter(def, am, contextAnnotations,
                declaredType, serializer, null, serType, false, null);
        String str = w.toString();
        assertTrue(str.contains("via method"));
    }

    private static Field getField(Class<?> clazz, String name) throws Exception {
        Field f = clazz.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... params) throws Exception {
        Method m = clazz.getDeclaredMethod(name, params);
        m.setAccessible(true);
        return m;
    }
}
