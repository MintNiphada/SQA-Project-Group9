package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PropertyBuilderTest {

    private ObjectMapper _mapper;
    private SerializationConfig _config;

    static class SimpleBean {
        public String strVal = "foo";
        public int intVal = 42;
        public int[] arrayVal = new int[]{1, 2};
        public List<String> listVal = Collections.emptyList();
        public Optional<String> optVal = Optional.empty();

        public String getStrVal() { return strVal; }
        public int getIntVal() { return intVal; }
        public int[] getArrayVal() { return arrayVal; }
        public List<String> getListVal() { return listVal; }
        public Optional<String> getOptVal() { return optVal; }
    }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    static class NonDefaultBean {
        public String text = "def";
        public int number = 10;
        public int[] ints = new int[]{1};
        public String getText() { return text; }
        public int getNumber() { return number; }
        public int[] getInts() { return ints; }
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class NonEmptyBean {
        public String emptyStr = "";
        public List<String> emptyList = Collections.emptyList();
        public String getEmptyStr() { return emptyStr; }
        public List<String> getEmptyList() { return emptyList; }
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    static class NonAbsentBean {
        public Optional<String> opt;
        public Optional<String> getOpt() { return opt; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class NonNullBean {
        public String val;
        public String getVal() { return val; }
    }

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = CustomFilter.class)
    static class CustomFilterBean {
        public String customVal = "test";
        public String getCustomVal() { return customVal; }
    }

    static class CustomFilter {
        @Override
        public boolean equals(Object obj) {
            return "test".equals(obj);
        }
    }

    static class ViewA {}

    static class ViewBean {
        @JsonView(ViewA.class)
        public String viewed;
        public String getViewed() { return viewed; }
    }

    static class UnwrappedChild {
        public String childField = "val";
        public String getChildField() { return childField; }
    }

    static class UnwrappedParent {
        @JsonUnwrapped(prefix = "u_")
        public UnwrappedChild child = new UnwrappedChild();
        public UnwrappedChild getChild() { return child; }
    }

    static class PrivateNoDefaultBean {
        private PrivateNoDefaultBean(String dummy) {}
        public String getVal() { return "a"; }
    }

    static class ExceptionBean {
        public String getErr() {
            throw new RuntimeException("Property evaluation error");
        }
    }

    static class StaticTypingBean {
        @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
        public CharSequence charSeq = "abc";
        public CharSequence getCharSeq() { return charSeq; }
    }

    static class BadSubtypeRefineBean {
        @JsonSerialize(as = Integer.class)
        public String strVal = "abc";
        public String getStrVal() { return strVal; }
    }

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getSerializationConfig();
    }

    private PropertyBuilder createPropertyBuilder(Class<?> cls) {
        JavaType type = _mapper.constructType(cls);
        BeanDescription beanDesc = _config.introspect(type);
        return new PropertyBuilder(_config, beanDesc);
    }

    private BeanPropertyDefinition findPropDef(BeanDescription beanDesc, String propName) {
        for (BeanPropertyDefinition def : beanDesc.findProperties()) {
            if (def.getName().equals(propName)) {
                return def;
            }
        }
        return null;
    }

    @Test
    public void testGetClassAnnotations() {
        PropertyBuilder pb = createPropertyBuilder(SimpleBean.class);
        Annotations ann = pb.getClassAnnotations();
        Assert.assertNotNull(ann);
    }

    @Test
    public void testBuildWriterAlwaysInclusion() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(SimpleBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "strVal");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertEquals("strVal", bpw.getName());
        Assert.assertFalse(bpw.willSuppressNulls());
        Assert.assertNull(bpw.getViews());
    }

    @Test
    public void testBuildWriterWriteEmptyJsonArraysDisabled() throws Exception {
        _config = _config.without(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS);
        PropertyBuilder pb = new PropertyBuilder(_config, _config.introspect(_mapper.constructType(SimpleBean.class)));
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "listVal");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertEquals(BeanPropertyWriter.MARKER_FOR_EMPTY, bpw._suppressableValue);
    }

    @Test
    public void testBuildWriterNonDefaultWithRealPropertyDefaults() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(NonDefaultBean.class);
        Assert.assertTrue(pb._useRealPropertyDefaults);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "text");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertEquals("def", bpw._suppressableValue);
    }

    @Test
    public void testBuildWriterNonDefaultArrayComparator() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(NonDefaultBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "ints");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertNotNull(bpw._suppressableValue);
        Assert.assertTrue(bpw._suppressableValue.getClass().getName().contains("ArrayBuilders"));
    }

    @Test
    public void testBuildWriterNonEmpty() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(NonEmptyBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "emptyStr");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertTrue(bpw.willSuppressNulls());
        Assert.assertEquals(BeanPropertyWriter.MARKER_FOR_EMPTY, bpw._suppressableValue);
    }

    @Test
    public void testBuildWriterNonAbsent() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(NonAbsentBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "opt");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertTrue(bpw.willSuppressNulls());
        Assert.assertEquals(BeanPropertyWriter.MARKER_FOR_EMPTY, bpw._suppressableValue);
    }

    @Test
    public void testBuildWriterNonNull() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(NonNullBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "val");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertTrue(bpw.willSuppressNulls());
    }

    @Test
    public void testBuildWriterCustomFilter() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(CustomFilterBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "customVal");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertNotNull(bpw._suppressableValue);
        Assert.assertTrue(bpw._suppressableValue instanceof CustomFilter);
    }

    @Test
    public void testBuildWriterWithViews() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(ViewBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "viewed");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertNotNull(bpw.getViews());
        Assert.assertEquals(1, bpw.getViews().length);
        Assert.assertEquals(ViewA.class, bpw.getViews()[0]);
    }

    @Test
    public void testBuildWriterUnwrapped() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(UnwrappedParent.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "child");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertTrue(bpw.isUnwrapping());
    }

    @Test
    public void testBuildWriterStaticTyping() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(StaticTypingBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "charSeq");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        BeanPropertyWriter bpw = pb.buildWriter(prov, prop, type, null, null, null, am, false);
        Assert.assertNotNull(bpw);
        Assert.assertNotNull(bpw.getSerializationType());
        Assert.assertTrue(bpw.getSerializationType().useStaticType());
    }

    @Test(expected = JsonMappingException.class)
    public void testBuildWriterIllegalRefinement() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(BadSubtypeRefineBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "strVal");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        pb.buildWriter(prov, prop, type, null, null, null, am, false);
    }

    @Test
    public void testGetDefaultBeanNoDefaultConstructor() {
        PropertyBuilder pb = createPropertyBuilder(PrivateNoDefaultBean.class);
        Object defaultBean = pb.getDefaultBean();
        Assert.assertNull(defaultBean);
    }

    @Test(expected = RuntimeException.class)
    public void testThrowWrappedException() throws Exception {
        PropertyBuilder pb = createPropertyBuilder(ExceptionBean.class);
        BeanDescription desc = pb._beanDesc;
        BeanPropertyDefinition prop = findPropDef(desc, "err");
        AnnotatedMember am = prop.getAccessor();
        JavaType type = am.getType();
        SerializerProvider prov = _mapper.getSerializerProviderInstance();

        pb.buildWriter(prov, prop, type, null, null, null, am, false);
    }

    @Test
    public void testDeprecatedGetPropertyDefaultValueAndGetDefaultValue() {
        PropertyBuilder pb = createPropertyBuilder(SimpleBean.class);
        JavaType intType = TypeFactory.defaultInstance().constructType(int.class);
        Object defVal = pb.getDefaultValue(intType);
        Assert.assertEquals(0, defVal);

        BeanPropertyDefinition prop = findPropDef(pb._beanDesc, "strVal");
        Object strDef = pb.getPropertyDefaultValue("strVal", prop.getAccessor(), prop.getAccessor().getType());
        Assert.assertEquals("foo", strDef);
    }
}
