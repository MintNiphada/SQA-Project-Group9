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

public class PropertyBuilderTest {

    static class SimpleBean {
        public String strVal = "foo";
        public int intVal = 42;
        public int[] arrayVal = new int[]{1, 2};
        public List<String> listVal = new ArrayList<String>();
        public OptionalHolder optVal;

        public String getStrVal() { return strVal; }
        public int getIntVal() { return intVal; }
        public int[] getArrayVal() { return arrayVal; }
        public List<String> getListVal() { return listVal; }
    }

    static class OptionalHolder {
        public String value;
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultBean {
        public String text = "def";
        public int number = 10;
        public int[] numbers = new int[]{1, 2};
        public String getText() { return text; }
        public int getNumber() { return number; }
        public int[] getNumbers() { return numbers; }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class NonEmptyBean {
        public String emptyStr = "";
        public List<String> emptyList = Collections.emptyList();
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    static class NonAbsentBean {
        public String regularStr;
    }

    static class NoDefaultConstructorBean {
        public String prop;
        public NoDefaultConstructorBean(String p) { this.prop = p; }
        public String getProp() { return prop; }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultNoDefCtorBean {
        public String prop;
        public NonDefaultNoDefCtorBean(String p) { this.prop = p; }
        public String getProp() { return prop; }
    }

    static class StaticTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
        public CharSequence charSeq = "abc";
    }

    static class UnwrappedBean {
        @JsonUnwrapped
        public SimpleBean nested = new SimpleBean();
    }

    static class ThrowingBean {
        public String getExplosive() {
            throw new IllegalStateException("Boom");
        }
    }

    @Test
    public void testGetClassAnnotations() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = config.introspect(type);
        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);

        Assert.assertNotNull(pb.getClassAnnotations());
    }

    @Test
    public void testBuildWriterAlwaysInclusion() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig().without(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = config.introspect(type);
        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);

        for (BeanPropertyDefinition propDef : beanDesc.findProperties()) {
            AnnotatedMember am = propDef.getPrimaryMember();
            JavaType propType = am.getType();
            BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, propType, null, null, null, am, false);
            Assert.assertNotNull(bpw);
            if (propType.isContainerType()) {
                Assert.assertNotNull(bpw.getNullSerializer());
            }
        }
    }

    @Test
    public void testBuildWriterNonDefaultBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType type = mapper.constructType(NonDefaultBean.class);
        BeanDescription beanDesc = config.introspect(type);
        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);

        for (BeanPropertyDefinition propDef : beanDesc.findProperties()) {
            AnnotatedMember am = propDef.getPrimaryMember();
            JavaType propType = am.getType();
            BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, propType, null, null, null, am, false);
            Assert.assertNotNull(bpw);
        }
    }

    @Test
    public void testBuildWriterNonEmptyAndNonAbsent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JavaType typeEmpty = mapper.constructType(NonEmptyBean.class);
        BeanDescription beanDescEmpty = config.introspect(typeEmpty);
        PropertyBuilder pbEmpty = new PropertyBuilder(config, beanDescEmpty);
        for (BeanPropertyDefinition propDef : beanDescEmpty.findProperties()) {
            AnnotatedMember am = propDef.getPrimaryMember();
            BeanPropertyWriter bpw = pbEmpty.buildWriter(prov, propDef, am.getType(), null, null, null, am, false);
            Assert.assertNotNull(bpw);
        }

        JavaType typeAbsent = mapper.constructType(NonAbsentBean.class);
        BeanDescription beanDescAbsent = config.introspect(typeAbsent);
        PropertyBuilder pbAbsent = new PropertyBuilder(config, beanDescAbsent);
        for (BeanPropertyDefinition propDef : beanDescAbsent.findProperties()) {
            AnnotatedMember am = propDef.getPrimaryMember();
            BeanPropertyWriter bpw = pbAbsent.buildWriter(prov, propDef, am.getType(), null, null, null, am, false);
            Assert.assertNotNull(bpw);
        }
    }

    @Test
    public void testBuildWriterStaticTypingAndUnwrapped() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JavaType typeStatic = mapper.constructType(StaticTypingBean.class);
        BeanDescription descStatic = config.introspect(typeStatic);
        PropertyBuilder pbStatic = new PropertyBuilder(config, descStatic);
        for (BeanPropertyDefinition propDef : descStatic.findProperties()) {
            AnnotatedMember am = propDef.getPrimaryMember();
            BeanPropertyWriter bpw = pbStatic.buildWriter(prov, propDef, am.getType(), null, null, null, am, false);
            Assert.assertNotNull(bpw);
            Assert.assertNotNull(bpw.getSerializationType());
        }

        JavaType typeUnwrap = mapper.constructType(UnwrappedBean.class);
        BeanDescription descUnwrap = config.introspect(typeUnwrap);
        PropertyBuilder pbUnwrap = new PropertyBuilder(config, descUnwrap);
        for (BeanPropertyDefinition propDef : descUnwrap.findProperties()) {
            AnnotatedMember am = propDef.getPrimaryMember();
            BeanPropertyWriter bpw = pbUnwrap.buildWriter(prov, propDef, am.getType(), null, null, null, am, false);
            Assert.assertNotNull(bpw);
            Assert.assertTrue(bpw.isUnwrapping());
        }
    }

    @Test
    public void testNoDefaultConstructorHandling() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JavaType type = mapper.constructType(NonDefaultNoDefCtorBean.class);
        BeanDescription beanDesc = config.introspect(type);
        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);

        for (BeanPropertyDefinition propDef : beanDesc.findProperties()) {
            AnnotatedMember am = propDef.getPrimaryMember();
            BeanPropertyWriter bpw = pb.buildWriter(prov, propDef, am.getType(), null, null, null, am, false);
            Assert.assertNotNull(bpw);
        }
    }

    @Test
    public void testGetDefaultValueDirectly() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(SimpleBean.class);
        BeanDescription beanDesc = config.introspect(type);
        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);

        TypeFactory tf = mapper.getTypeFactory();
        Assert.assertEquals(0, pb.getDefaultValue(tf.constructType(int.class)));
        Assert.assertEquals(0, pb.getDefaultValue(tf.constructType(Integer.class)));
        Assert.assertEquals(false, pb.getDefaultValue(tf.constructType(boolean.class)));
        Assert.assertEquals("", pb.getDefaultValue(tf.constructType(String.class)));
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(tf.constructType(List.class)));
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, pb.getDefaultValue(tf.constructType(int[].class)));
        Assert.assertNull(pb.getDefaultValue(tf.constructType(Object.class)));
    }

    @Test
    public void testThrowWrappedExceptionHandling() {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType type = mapper.constructType(ThrowingBean.class);
        BeanDescription beanDesc = config.introspect(type);
        PropertyBuilder pb = new PropertyBuilder(config, beanDesc);

        try {
            pb._throwWrapped(new IllegalStateException("runtime issue"), "prop", new SimpleBean());
            Assert.fail("Expected exception");
        } catch (IllegalStateException e) {
            Assert.assertEquals("runtime issue", e.getMessage());
        }

        try {
            pb._throwWrapped(new AssertionError("error issue"), "prop", new SimpleBean());
            Assert.fail("Expected assertion error");
        } catch (AssertionError e) {
            Assert.assertEquals("error issue", e.getMessage());
        }

        try {
            pb._throwWrapped(new IOException("checked issue"), "prop", new SimpleBean());
            Assert.fail("Expected illegal argument exception");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Failed to get property 'prop'"));
        }
    }

    @Test
    public void testFindSerializationTypeMismatch() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        JavaType declaredType = mapper.constructType(String.class);
        BeanDescription beanDesc = config.introspect(declaredType);

        PropertyBuilder pb = new PropertyBuilder(config, beanDesc) {
            @Override
            protected JavaType findSerializationType(Annotated a, boolean useStaticTyping, JavaType declared)
                    throws JsonMappingException {
                return super.findSerializationType(a, useStaticTyping, declared);
            }
        };

        JavaType result = pb.findSerializationType(beanDesc.getClassInfo(), true, declaredType);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.useStaticType());

        result = pb.findSerializationType(beanDesc.getClassInfo(), false, declaredType);
        Assert.assertNull(result);
    }
}
