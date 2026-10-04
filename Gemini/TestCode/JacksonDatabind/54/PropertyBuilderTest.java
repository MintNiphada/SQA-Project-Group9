package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class PropertyBuilderTest {

    static class SimpleBean {
        public int count = 5;
        public String text = "abc";
        public List<String> list = new ArrayList<String>();
        public int[] array = new int[]{1, 2};
        public AtomicReference<String> ref = new AtomicReference<String>("ref");

        public SimpleBean() {}
    }

    static class NoDefaultConstructorBean {
        public String name;
        public NoDefaultConstructorBean(String name) {
            this.name = name;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultClassBean {
        public int intProp = 10;
        public String strProp = "def";
        public int[] arrayProp = new int[]{1};
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    static class NonAbsentBean {
        public AtomicReference<String> opt;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class NonEmptyBean {
        public List<String> list;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class NonNullBean {
        public String str;
    }

    @JsonInclude(JsonInclude.Include.USE_DEFAULTS)
    static class UseDefaultsBean {
        public String str;
    }

    static class StaticTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
        public CharSequence charSeq = "text";

        @JsonSerialize(typing = JsonSerialize.Typing.DYNAMIC)
        public CharSequence dynSeq = "text2";
    }

    static class RefineTypeBean {
        @JsonSerialize(as = CharSequence.class)
        public String refined = "ref";
    }

    static class InvalidRefineBean {
        @JsonSerialize(as = Integer.class)
        public String invalid = "inv";
    }

    static class UnwrappedAndNullSerBean {
        @JsonUnwrapped
        public SimpleBean unwrapped;

        @JsonSerialize(nullsUsing = CustomNullSerializer.class)
        public String customNull;
    }

    static class CustomNullSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("CUSTOM_NULL");
        }
    }

    private PropertyBuilder createPropertyBuilder(Class<?> cls) {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(cls);
        BeanDescription beanDesc = config.introspect(type);
        return new PropertyBuilder(config, beanDesc);
    }

    private PropertyBuilder createPropertyBuilder(Class<?> cls, ObjectMapper mapper) {
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(cls);
        BeanDescription beanDesc = config.introspect(type);
        return new PropertyBuilder(config, beanDesc);
    }

    @Test
    public void testGetClassAnnotations() {
        PropertyBuilder pb = createPropertyBuilder(SimpleBean.class);
        Assert.assertNotNull(pb.getClassAnnotations());
    }

    @Test
    public void testGetDefaultValuePrimitivesAndObjects() {
        PropertyBuilder pb = createPropertyBuilder(SimpleBean.class);
        TypeFactory tf = TypeFactory.defaultInstance();

        Assert.assertEquals(0, pb.getDefaultValue(tf.constructType(int.class)));
        Assert.assertEquals(0L, pb.getDefaultValue(tf.constructType(Long.class)));
        Assert.assertEquals(false, pb.getDefaultValue(tf.constructType(boolean.class)));
        Assert.assertEquals("", pb.getDefaultValue(tf.constructType(String.class)));
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(tf.constructType(List.class)));
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(tf.constructType(int[].class)));
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(tf.constructType(AtomicReference.class)));
        Assert.assertNull(pb.getDefaultValue(tf.constructType(Object.class)));
    }

    @Test
    public void testGetDefaultBean() {
        PropertyBuilder pb = createPropertyBuilder(SimpleBean.class);
        Object bean1 = pb.getDefaultBean();
        Assert.assertNotNull(bean1);
        Assert.assertTrue(bean1 instanceof SimpleBean);
        Object bean2 = pb.getDefaultBean();
        Assert.assertSame(bean1, bean2);

        PropertyBuilder pbNoDef = createPropertyBuilder(NoDefaultConstructorBean.class);
        Assert.assertNull(pbNoDef.getDefaultBean());
    }

    @Test
    public void testGetPropertyDefaultValue() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(NonDefaultClassBean.class);
        JavaType type = TypeFactory.defaultInstance().constructType(int.class);
        AnnotatedClass ac = AnnotatedClass.construct(NonDefaultClassBean.class, pb._config.getAnnotationIntrospector(), null);
        for (AnnotatedField f : ac.fields()) {
            if ("intProp".equals(f.getName())) {
                Object val = pb.getPropertyDefaultValue("intProp", f, type);
                Assert.assertEquals(10, val);
            }
        }
    }

    @Test
    public void testGetPropertyDefaultValueNoDefaultBean() {
        PropertyBuilder pb = createPropertyBuilder(NoDefaultConstructorBean.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        AnnotatedClass ac = AnnotatedClass.construct(NoDefaultConstructorBean.class, pb._config.getAnnotationIntrospector(), null);
        AnnotatedField field = ac.fields().iterator().next();
        Object val = pb.getPropertyDefaultValue("name", field, type);
        Assert.assertEquals("", val);
    }

    @Test
    public void testBuildWriterAlwaysAndEmptyCollections() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        PropertyBuilder pb = createPropertyBuilder(SimpleBean.class, mapper);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        BeanPropertyDefinition propDef = pb._beanDesc.findProperties().stream()
                .filter(p -> "list".equals(p.getName())).findFirst().get();
        BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, propDef.getPrimaryType(), null, null, null, propDef.getField(), false);
        Assert.assertNotNull(bpw);
        Assert.assertSame(BeanPropertyWriter.MARKER_FOR_EMPTY, bpw.getInternalSetting(BeanPropertyWriter.class));
    }

    @Test
    public void testBuildWriterNonDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyBuilder pb = createPropertyBuilder(NonDefaultClassBean.class, mapper);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        BeanPropertyDefinition propDef = pb._beanDesc.findProperties().stream()
                .filter(p -> "arrayProp".equals(p.getName())).findFirst().get();
        BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, propDef.getPrimaryType(), null, null, null, propDef.getField(), false);
        Assert.assertNotNull(bpw);
    }

    @Test
    public void testBuildWriterNonAbsent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyBuilder pb = createPropertyBuilder(NonAbsentBean.class, mapper);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        BeanPropertyDefinition propDef = pb._beanDesc.findProperties().stream()
                .filter(p -> "opt".equals(p.getName())).findFirst().get();
        BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, propDef.getPrimaryType(), null, null, null, propDef.getField(), false);
        Assert.assertNotNull(bpw);
        Assert.assertTrue(bpw.willSuppressNulls());
    }

    @Test
    public void testBuildWriterNonEmpty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyBuilder pb = createPropertyBuilder(NonEmptyBean.class, mapper);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        BeanPropertyDefinition propDef = pb._beanDesc.findProperties().stream()
                .filter(p -> "list".equals(p.getName())).findFirst().get();
        BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, propDef.getPrimaryType(), null, null, null, propDef.getField(), false);
        Assert.assertNotNull(bpw);
        Assert.assertTrue(bpw.willSuppressNulls());
    }

    @Test
    public void testBuildWriterNonNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyBuilder pb = createPropertyBuilder(NonNullBean.class, mapper);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        BeanPropertyDefinition propDef = pb._beanDesc.findProperties().stream()
                .filter(p -> "str".equals(p.getName())).findFirst().get();
        BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, propDef.getPrimaryType(), null, null, null, propDef.getField(), false);
        Assert.assertNotNull(bpw);
        Assert.assertTrue(bpw.willSuppressNulls());
    }

    @Test
    public void testBuildWriterUseDefaults() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyBuilder pb = createPropertyBuilder(UseDefaultsBean.class, mapper);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        BeanPropertyDefinition propDef = pb._beanDesc.findProperties().stream()
                .filter(p -> "str".equals(p.getName())).findFirst().get();
        BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, propDef.getPrimaryType(), null, null, null, propDef.getField(), false);
        Assert.assertNotNull(bpw);
    }

    @Test
    public void testBuildWriterCustomNullAndUnwrapped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PropertyBuilder pb = createPropertyBuilder(UnwrappedAndNullSerBean.class, mapper);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        for (BeanPropertyDefinition propDef : pb._beanDesc.findProperties()) {
            BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, propDef.getPrimaryType(), null, null, null, propDef.getField(), false);
            Assert.assertNotNull(bpw);
            if ("customNull".equals(propDef.getName())) {
                Assert.assertNotNull(bpw.getNullSerializer());
            }
            if ("unwrapped".equals(propDef.getName())) {
                Assert.assertTrue(bpw.isUnwrapping());
            }
        }
    }

    @Test
    public void testFindSerializationType() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(StaticTypingBean.class);
        BeanPropertyDefinition propStatic = pb._beanDesc.findProperties().stream()
                .filter(p -> "charSeq".equals(p.getName())).findFirst().get();
        JavaType st = pb.findSerializationType(propStatic.getField(), false, propStatic.getPrimaryType());
        Assert.assertNotNull(st);
        Assert.assertTrue(st.useStaticType());

        BeanPropertyDefinition propDyn = pb._beanDesc.findProperties().stream()
                .filter(p -> "dynSeq".equals(p.getName())).findFirst().get();
        JavaType dyn = pb.findSerializationType(propDyn.getField(), true, propDyn.getPrimaryType());
        Assert.assertNull(dyn);
    }

    @Test
    public void testFindSerializationTypeRefined() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(RefineTypeBean.class);
        BeanPropertyDefinition prop = pb._beanDesc.findProperties().stream()
                .filter(p -> "refined".equals(p.getName())).findFirst().get();
        JavaType st = pb.findSerializationType(prop.getField(), false, prop.getPrimaryType());
        Assert.assertNotNull(st);
        Assert.assertEquals(CharSequence.class, st.getRawClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindSerializationTypeInvalidRefinement() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(InvalidRefineBean.class);
        BeanPropertyDefinition prop = pb._beanDesc.findProperties().stream()
                .filter(p -> "invalid".equals(p.getName())).findFirst().get();
        pb.findSerializationType(prop.getField(), false, prop.getPrimaryType());
    }

    @Test
    public void testThrowWrapped() {
        PropertyBuilder pb = createPropertyBuilder(SimpleBean.class);

        try {
            pb._throwWrapped(new RuntimeException("runtime"), "testProp", new SimpleBean());
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertEquals("runtime", e.getMessage());
        }

        try {
            pb._throwWrapped(new Error("error"), "testProp", new SimpleBean());
            Assert.fail();
        } catch (Error e) {
            Assert.assertEquals("error", e.getMessage());
        }

        try {
            pb._throwWrapped(new Exception("checked"), "testProp", new SimpleBean());
            Assert.fail();
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get property 'testProp'"));
        }
    }
}
