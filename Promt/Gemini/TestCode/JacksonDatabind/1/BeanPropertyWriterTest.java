package com.fasterxml.jackson.databind.ser;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.NameTransformer;

public class BeanPropertyWriterTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomAnnotation {
        String value() default "test";
    }

    public static class TestBean {
        @CustomAnnotation("fieldVal")
        public String fieldProp = "fieldValue";

        private int methodProp = 42;

        public TestBean selfRef;

        public String emptyString = "";

        public List<String> stringList = new ArrayList<String>();

        public int getMethodProp() {
            return methodProp;
        }

        public void setMethodProp(int methodProp) {
            this.methodProp = methodProp;
        }
    }

    public static class SimplePropDef extends BeanPropertyDefinition {
        private final String _name;
        private final PropertyName _wrapperName;
        private final boolean _isRequired;
        private final Class<?>[] _views;

        public SimplePropDef(String name) {
            this(name, null, false, null);
        }

        public SimplePropDef(String name, PropertyName wrapperName, boolean isRequired, Class<?>[] views) {
            _name = name;
            _wrapperName = wrapperName;
            _isRequired = isRequired;
            _views = views;
        }

        @Override public String getName() { return _name; }
        @Override public PropertyName getWrapperName() { return _wrapperName; }
        @Override public String getInternalName() { return _name; }
        @Override public boolean isExplicitlyIncluded() { return true; }
        @Override public boolean hasGetter() { return false; }
        @Override public boolean hasSetter() { return false; }
        @Override public boolean hasField() { return false; }
        @Override public boolean hasConstructorParameter() { return false; }
        @Override public AnnotatedMethod getGetter() { return null; }
        @Override public AnnotatedMethod getSetter() { return null; }
        @Override public AnnotatedField getField() { return null; }
        @Override public AnnotatedParameter getConstructorParameter() { return null; }
        @Override public AnnotatedMember getAccessor() { return null; }
        @Override public AnnotatedMember getMutator() { return null; }
        @Override public AnnotatedMember getNonConstructorMutator() { return null; }
        @Override public AnnotatedMember getPrimaryMember() { return null; }
        @Override public boolean couldDeserialize() { return false; }
        @Override public boolean isRequired() { return _isRequired; }
        @Override public Class<?>[] findViews() { return _views; }
    }

    private ObjectMapper _mapper;
    private DefaultSerializerProvider _provider;
    private AnnotatedField _annotatedField;
    private AnnotatedMethod _annotatedMethod;
    private Annotations _contextAnnotations;
    private JavaType _stringType;
    private JavaType _intType;

    @Before
    public void setUp() throws Exception {
        _mapper = new ObjectMapper();
        _provider = (DefaultSerializerProvider) _mapper.getSerializerProviderInstance();

        Field field = TestBean.class.getField("fieldProp");
        AnnotationMap fieldAnnMap = new AnnotationMap();
        fieldAnnMap.add(field.getAnnotation(CustomAnnotation.class));
        _annotatedField = new AnnotatedField(field, fieldAnnMap);

        Method getter = TestBean.class.getMethod("getMethodProp");
        AnnotationMap methodAnnMap = new AnnotationMap();
        _annotatedMethod = new AnnotatedMethod(getter, methodAnnMap, null);

        _contextAnnotations = new AnnotationMap();
        _stringType = TypeFactory.defaultInstance().constructType(String.class);
        _intType = TypeFactory.defaultInstance().constructType(int.class);
    }

    @Test
    public void testFieldPropertyWriterBasics() {
        SimplePropDef def = new SimplePropDef("fieldProp", new PropertyName("fieldWrapper"), true, new Class<?>[]{ String.class });
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        Assert.assertEquals("fieldProp", bpw.getName());
        Assert.assertEquals("fieldProp", bpw.getSerializedName().getValue());
        Assert.assertEquals(new PropertyName("fieldWrapper"), bpw.getWrapperName());
        Assert.assertTrue(bpw.isRequired());
        Assert.assertEquals(_stringType, bpw.getType());
        Assert.assertNull(bpw.getSerializationType());
        Assert.assertNull(bpw.getRawSerializationType());
        Assert.assertEquals(String.class, bpw.getPropertyType());
        Assert.assertEquals(String.class, bpw.getGenericPropertyType());
        Assert.assertSame(_annotatedField, bpw.getMember());
        Assert.assertArrayEquals(new Class<?>[]{ String.class }, bpw.getViews());
        Assert.assertFalse(bpw.hasSerializer());
        Assert.assertFalse(bpw.hasNullSerializer());
        Assert.assertFalse(bpw.willSuppressNulls());
        Assert.assertNotNull(bpw.getAnnotation(CustomAnnotation.class));
        Assert.assertNull(bpw.getContextAnnotation(CustomAnnotation.class));
        Assert.assertTrue(bpw.toString().contains("field \""));
    }

    @Test
    public void testMethodPropertyWriterBasics() {
        SimplePropDef def = new SimplePropDef("methodProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedMethod, _contextAnnotations,
                _intType, null, null, _intType, true, Integer.valueOf(0));

        Assert.assertEquals("methodProp", bpw.getName());
        Assert.assertEquals(Integer.TYPE, bpw.getPropertyType());
        Assert.assertEquals(Integer.TYPE, bpw.getGenericPropertyType());
        Assert.assertSame(_annotatedMethod, bpw.getMember());
        Assert.assertEquals(_intType, bpw.getSerializationType());
        Assert.assertEquals(int.class, bpw.getRawSerializationType());
        Assert.assertTrue(bpw.willSuppressNulls());
        Assert.assertFalse(bpw.isRequired());
        Assert.assertTrue(bpw.toString().contains("via method "));
        Assert.assertTrue(bpw.toString().contains("no static serializer"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithInvalidMember() {
        AnnotatedParameter param = new AnnotatedParameter(null, _stringType, null, 0);
        SimplePropDef def = new SimplePropDef("invalid");
        new BeanPropertyWriter(def, param, _contextAnnotations, _stringType, null, null, null, false, null);
    }

    @Test
    public void testCopyConstructorAndRename() {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        bpw.setInternalSetting("testKey", "testVal");
        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);
        bpw.setNonTrivialBaseType(listType);

        BeanPropertyWriter unchanged = bpw.rename(NameTransformer.NOP);
        Assert.assertSame(bpw, unchanged);

        BeanPropertyWriter renamed = bpw.rename(new NameTransformer() {
            @Override
            public String transform(String name) {
                return "prefix_" + name;
            }

            @Override
            public String reverse(String transformed) {
                return transformed.substring(7);
            }
        });

        Assert.assertEquals("prefix_fieldProp", renamed.getName());
        Assert.assertEquals("testVal", renamed.getInternalSetting("testKey"));
    }

    @Test
    public void testUnwrappingWriter() {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        BeanPropertyWriter unwrapping = bpw.unwrappingWriter(NameTransformer.NOP);
        Assert.assertNotNull(unwrapping);
    }

    @Test
    public void testInternalSettings() {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        Assert.assertNull(bpw.getInternalSetting("key1"));
        Assert.assertNull(bpw.removeInternalSetting("key1"));

        bpw.setInternalSetting("key1", "val1");
        bpw.setInternalSetting("key2", "val2");
        Assert.assertEquals("val1", bpw.getInternalSetting("key1"));
        Assert.assertEquals("val2", bpw.getInternalSetting("key2"));

        Assert.assertEquals("val1", bpw.removeInternalSetting("key1"));
        Assert.assertNull(bpw.getInternalSetting("key1"));

        Assert.assertEquals("val2", bpw.removeInternalSetting("key2"));
        Assert.assertNull(bpw.getInternalSetting("key2"));
        Assert.assertNull(bpw.removeInternalSetting("key2"));
    }

    @Test
    public void testAssignSerializerAndNullSerializer() throws Exception {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        JsonSerializer<Object> ser1 = _provider.findValueSerializer(String.class, bpw);
        bpw.assignSerializer(ser1);
        Assert.assertTrue(bpw.hasSerializer());
        Assert.assertSame(ser1, bpw.getSerializer());
        Assert.assertTrue(bpw.toString().contains("static serializer of type"));

        // Assigning the exact same serializer is allowed
        bpw.assignSerializer(ser1);

        try {
            JsonSerializer<Object> ser2 = _provider.findValueSerializer(Integer.class, bpw);
            bpw.assignSerializer(ser2);
            Assert.fail("Should throw IllegalStateException when overriding serializer");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("Can not override serializer"));
        }

        JsonSerializer<Object> nullSer1 = _provider.findNullValueSerializer(bpw);
        bpw.assignNullSerializer(nullSer1);
        Assert.assertTrue(bpw.hasNullSerializer());

        bpw.assignNullSerializer(nullSer1);

        try {
            JsonSerializer<Object> nullSer2 = _provider.findValueSerializer(Integer.class, bpw);
            bpw.assignNullSerializer(nullSer2);
            Assert.fail("Should throw IllegalStateException when overriding null serializer");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("Can not override null serializer"));
        }
    }

    @Test
    public void testGetFieldValueAndMethodValue() throws Exception {
        SimplePropDef def1 = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw1 = new BeanPropertyWriter(def1, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        TestBean bean = new TestBean();
        Assert.assertEquals("fieldValue", bpw1.get(bean));

        SimplePropDef def2 = new SimplePropDef("methodProp");
        BeanPropertyWriter bpw2 = new BeanPropertyWriter(def2, _annotatedMethod, _contextAnnotations,
                _intType, null, null, null, false, null);
        Assert.assertEquals(42, bpw2.get(bean));
    }

    @Test
    public void testSerializeAsField() throws Exception {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        TestBean bean = new TestBean();
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartObject();
        bpw.serializeAsField(bean, jgen, _provider);
        jgen.writeEndObject();
        jgen.close();

        Assert.assertEquals("{\"fieldProp\":\"fieldValue\"}", sw.toString());
    }

    @Test
    public void testSerializeAsFieldNullHandling() throws Exception {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        TestBean bean = new TestBean();
        bean.fieldProp = null;

        // Null without null serializer -> suppressed
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartObject();
        bpw.serializeAsField(bean, jgen, _provider);
        jgen.writeEndObject();
        jgen.close();
        Assert.assertEquals("{}", sw.toString());

        // Null with null serializer -> written
        bpw.assignNullSerializer(_provider.findNullValueSerializer(bpw));
        sw = new StringWriter();
        jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartObject();
        bpw.serializeAsField(bean, jgen, _provider);
        jgen.writeEndObject();
        jgen.close();
        Assert.assertEquals("{\"fieldProp\":null}", sw.toString());
    }

    @Test
    public void testSerializeAsFieldSuppressableValues() throws Exception {
        SimplePropDef def = new SimplePropDef("fieldProp");

        // Suppress specific value
        BeanPropertyWriter bpwSpecific = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, "fieldValue");
        TestBean bean = new TestBean();
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartObject();
        bpwSpecific.serializeAsField(bean, jgen, _provider);
        jgen.writeEndObject();
        jgen.close();
        Assert.assertEquals("{}", sw.toString());

        // Suppress empty marker
        BeanPropertyWriter bpwEmpty = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, BeanPropertyWriter.MARKER_FOR_EMPTY);
        bean.fieldProp = "";
        sw = new StringWriter();
        jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartObject();
        bpwEmpty.serializeAsField(bean, jgen, _provider);
        jgen.writeEndObject();
        jgen.close();
        Assert.assertEquals("{}", sw.toString());
    }

    @Test
    public void testSerializeAsFieldSelfReferenceCycle() throws Exception {
        Field field = TestBean.class.getField("selfRef");
        AnnotatedField af = new AnnotatedField(field, new AnnotationMap());
        JavaType beanType = TypeFactory.defaultInstance().constructType(TestBean.class);
        SimplePropDef def = new SimplePropDef("selfRef");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, af, _contextAnnotations,
                beanType, null, null, null, false, null);

        TestBean bean = new TestBean();
        bean.selfRef = bean;

        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartObject();
        try {
            bpw.serializeAsField(bean, jgen, _provider);
            Assert.fail("Should throw JsonMappingException for self reference cycle");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Direct self-reference leading to cycle"));
        }
    }

    @Test
    public void testSerializeAsColumn() throws Exception {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        TestBean bean = new TestBean();
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartArray();
        bpw.serializeAsColumn(bean, jgen, _provider);
        jgen.writeEndArray();
        jgen.close();

        Assert.assertEquals("[\"fieldValue\"]", sw.toString());
    }

    @Test
    public void testSerializeAsColumnNullsAndSuppressables() throws Exception {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        TestBean bean = new TestBean();
        bean.fieldProp = null;

        // Null without nullSerializer writes null in column
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartArray();
        bpw.serializeAsColumn(bean, jgen, _provider);
        jgen.writeEndArray();
        jgen.close();
        Assert.assertEquals("[null]", sw.toString());

        // Null with nullSerializer
        bpw.assignNullSerializer(_provider.findNullValueSerializer(bpw));
        sw = new StringWriter();
        jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartArray();
        bpw.serializeAsColumn(bean, jgen, _provider);
        jgen.writeEndArray();
        jgen.close();
        Assert.assertEquals("[null]", sw.toString());

        // Suppressed empty marker in column writes placeholder
        BeanPropertyWriter bpwEmpty = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, BeanPropertyWriter.MARKER_FOR_EMPTY);
        bean.fieldProp = "";
        sw = new StringWriter();
        jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartArray();
        bpwEmpty.serializeAsColumn(bean, jgen, _provider);
        jgen.writeEndArray();
        jgen.close();
        Assert.assertEquals("[null]", sw.toString());

        // Suppressed value in column writes placeholder
        BeanPropertyWriter bpwValue = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, "exactValue");
        bean.fieldProp = "exactValue";
        sw = new StringWriter();
        jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartArray();
        bpwValue.serializeAsColumn(bean, jgen, _provider);
        jgen.writeEndArray();
        jgen.close();
        Assert.assertEquals("[null]", sw.toString());
    }

    @Test
    public void testSerializeAsPlaceholder() throws Exception {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartArray();
        bpw.serializeAsPlaceholder(new TestBean(), jgen, _provider);
        jgen.writeEndArray();
        jgen.close();
        Assert.assertEquals("[null]", sw.toString());

        bpw.assignNullSerializer(_provider.findNullValueSerializer(bpw));
        sw = new StringWriter();
        jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartArray();
        bpw.serializeAsPlaceholder(new TestBean(), jgen, _provider);
        jgen.writeEndArray();
        jgen.close();
        Assert.assertEquals("[null]", sw.toString());
    }

    @Test
    public void testDepositSchemaPropertyWithVisitor() throws Exception {
        SimplePropDef reqDef = new SimplePropDef("fieldProp", null, true, null);
        BeanPropertyWriter bpwReq = new BeanPropertyWriter(reqDef, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        final List<String> props = new ArrayList<String>();
        JsonObjectFormatVisitor visitor = new JsonObjectFormatVisitor.Base() {
            @Override
            public void property(BeanProperty prop) {
                props.add("required:" + prop.getName());
            }

            @Override
            public void optionalProperty(BeanProperty prop) {
                props.add("optional:" + prop.getName());
            }
        };

        bpwReq.depositSchemaProperty(visitor);
        Assert.assertEquals(1, props.size());
        Assert.assertEquals("required:fieldProp", props.get(0));

        SimplePropDef optDef = new SimplePropDef("fieldProp", null, false, null);
        BeanPropertyWriter bpwOpt = new BeanPropertyWriter(optDef, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);
        bpwOpt.depositSchemaProperty(visitor);
        Assert.assertEquals(2, props.size());
        Assert.assertEquals("optional:fieldProp", props.get(1));

        // null visitor shouldn't throw exception
        bpwReq.depositSchemaProperty((JsonObjectFormatVisitor) null);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testDepositSchemaPropertyWithObjectNode() throws Exception {
        SimplePropDef def = new SimplePropDef("fieldProp");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, _annotatedField, _contextAnnotations,
                _stringType, null, null, null, false, null);

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        bpw.depositSchemaProperty(node, _provider);

        Assert.assertTrue(node.has("fieldProp"));
        Assert.assertNotNull(node.get("fieldProp"));
    }

    @Test
    public void testDynamicSerializationWithNonTrivialBaseType() throws Exception {
        Field field = TestBean.class.getField("stringList");
        AnnotatedField af = new AnnotatedField(field, new AnnotationMap());
        JavaType baseType = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);

        SimplePropDef def = new SimplePropDef("stringList");
        BeanPropertyWriter bpw = new BeanPropertyWriter(def, af, _contextAnnotations,
                baseType, null, null, null, false, null);
        bpw.setNonTrivialBaseType(baseType);

        TestBean bean = new TestBean();
        bean.stringList.add("item1");

        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        jgen.writeStartObject();
        bpw.serializeAsField(bean, jgen, _provider);
        jgen.writeEndObject();
        jgen.close();

        Assert.assertEquals("{\"stringList\":[\"item1\"]}", sw.toString());
    }
}