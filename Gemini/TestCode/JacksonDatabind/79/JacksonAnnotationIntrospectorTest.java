package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.VirtualBeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.beans.ConstructorProperties;
import java.beans.Transient;
import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector ai;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        ai = new JacksonAnnotationIntrospector();
        mapper = new ObjectMapper();
    }

    private AnnotatedClass getAnnotatedClass(Class<?> cls) {
        return AnnotatedClass.constructWithoutSuperTypes(cls, mapper.getDeserializationConfig());
    }

    private AnnotatedField getAnnotatedField(Class<?> cls, String fieldName) throws NoSuchFieldException {
        Field f = cls.getDeclaredField(fieldName);
        return new AnnotatedField(null, f, new AnnotationMap());
    }

    private AnnotatedField getAnnotatedFieldWithAnn(Class<?> cls, String fieldName) throws NoSuchFieldException {
        Field f = cls.getDeclaredField(fieldName);
        AnnotationMap map = new AnnotationMap();
        for (java.lang.annotation.Annotation a : f.getDeclaredAnnotations()) {
            map.add(a);
        }
        return new AnnotatedField(null, f, map);
    }

    private AnnotatedMethod getAnnotatedMethod(Class<?> cls, String methodName, Class<?>... params) throws NoSuchMethodException {
        Method m = cls.getDeclaredMethod(methodName, params);
        AnnotationMap map = new AnnotationMap();
        for (java.lang.annotation.Annotation a : m.getDeclaredAnnotations()) {
            map.add(a);
        }
        return new AnnotatedMethod(null, m, map, null);
    }

    private AnnotatedConstructor getAnnotatedConstructor(Class<?> cls, Class<?>... params) throws NoSuchMethodException {
        Constructor<?> c = cls.getDeclaredConstructor(params);
        AnnotationMap map = new AnnotationMap();
        for (java.lang.annotation.Annotation a : c.getDeclaredAnnotations()) {
            map.add(a);
        }
        return new AnnotatedConstructor(null, c, map, null);
    }

    private AnnotatedParameter getAnnotatedParameter(Class<?> cls, int index, Class<?>... params) throws NoSuchMethodException {
        AnnotatedConstructor c = getAnnotatedConstructor(cls, params);
        return new AnnotatedParameter(c, params[index], new AnnotationMap(), index);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonIgnore
    @interface BundleAnn {}

    @Test
    public void testVersionAndSerialization() throws Exception {
        Version v = ai.version();
        Assert.assertNotNull(v);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ai);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        JacksonAnnotationIntrospector deser = (JacksonAnnotationIntrospector) ois.readObject();
        Assert.assertNotNull(deser);
    }

    @Test
    public void testIsAnnotationBundle() throws Exception {
        Field f = DummyBundleClass.class.getDeclaredField("field");
        java.lang.annotation.Annotation bundle = f.getAnnotation(BundleAnn.class);
        Assert.assertTrue(ai.isAnnotationBundle(bundle));

        java.lang.annotation.Annotation nonBundle = f.getAnnotation(JsonProperty.class);
        Assert.assertFalse(ai.isAnnotationBundle(nonBundle));
    }

    private static class DummyBundleClass {
        @BundleAnn
        @JsonProperty
        public String field;
    }

    enum TestEnum {
        @JsonProperty("alpha") A,
        @JsonProperty("") B,
        C
    }

    @Test
    public void testEnumValues() {
        Assert.assertEquals("alpha", ai.findEnumValue(TestEnum.A));
        Assert.assertEquals("B", ai.findEnumValue(TestEnum.B));
        Assert.assertEquals("C", ai.findEnumValue(TestEnum.C));

        String[] names = new String[] { "A", "B", "C" };
        String[] result = ai.findEnumValues(TestEnum.class, TestEnum.values(), names);
        Assert.assertEquals("alpha", result[0]);
        Assert.assertEquals("B", result[1]);
        Assert.assertEquals("C", result[2]);
    }

    @JsonRootName(value = "root", namespace = "http://example.com")
    private static class RootClass {}

    @JsonRootName(value = "root2", namespace = "")
    private static class RootClassEmptyNs {}

    @JsonIgnoreProperties(value = {"ignore1", "ignore2"}, allowGetters = true, allowSetters = false, ignoreUnknown = true)
    private static class IgnorePropsClass {}

    @JsonIgnoreType(true)
    private static class IgnoreTypeClass {}

    @JsonFilter("myFilter")
    private static class FilterClass {}

    @JsonFilter("")
    private static class EmptyFilterClass {}

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonClassDescription("class-desc")
    private static class NamingAndDescClass {}

    @Test
    public void testClassAnnotations() {
        AnnotatedClass ac = getAnnotatedClass(RootClass.class);
        PropertyName pn = ai.findRootName(ac);
        Assert.assertEquals("root", pn.getSimpleName());
        Assert.assertEquals("http://example.com", pn.getNamespace());

        AnnotatedClass ac2 = getAnnotatedClass(RootClassEmptyNs.class);
        PropertyName pn2 = ai.findRootName(ac2);
        Assert.assertEquals("root2", pn2.getSimpleName());
        Assert.assertNull(pn2.getNamespace());

        AnnotatedClass acEmpty = getAnnotatedClass(DummyBundleClass.class);
        Assert.assertNull(ai.findRootName(acEmpty));

        AnnotatedClass acIgnore = getAnnotatedClass(IgnorePropsClass.class);
        Assert.assertArrayEquals(new String[]{"ignore1", "ignore2"}, ai.findPropertiesToIgnore(acIgnore));
        Assert.assertNull(ai.findPropertiesToIgnore(acIgnore, true));
        Assert.assertArrayEquals(new String[]{"ignore1", "ignore2"}, ai.findPropertiesToIgnore(acIgnore, false));
        Assert.assertNull(ai.findPropertiesToIgnore(acEmpty, true));
        Assert.assertEquals(Boolean.TRUE, ai.findIgnoreUnknownProperties(acIgnore));
        Assert.assertNull(ai.findIgnoreUnknownProperties(acEmpty));

        Assert.assertEquals(Boolean.TRUE, ai.isIgnorableType(getAnnotatedClass(IgnoreTypeClass.class)));
        Assert.assertNull(ai.isIgnorableType(acEmpty));

        Assert.assertEquals("myFilter", ai.findFilterId(getAnnotatedClass(FilterClass.class)));
        Assert.assertNull(ai.findFilterId(getAnnotatedClass(EmptyFilterClass.class)));
        Assert.assertNull(ai.findFilterId(acEmpty));

        AnnotatedClass acNaming = getAnnotatedClass(NamingAndDescClass.class);
        Assert.assertEquals(PropertyNamingStrategy.SnakeCaseStrategy.class, ai.findNamingStrategy(acNaming));
        Assert.assertNull(ai.findNamingStrategy(acEmpty));
        Assert.assertEquals("class-desc", ai.findClassDescription(acNaming));
        Assert.assertNull(ai.findClassDescription(acEmpty));
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class AutoDetectClass {}

    @Test
    public void testAutoDetectVisibility() {
        AnnotatedClass ac = getAnnotatedClass(AutoDetectClass.class);
        VisibilityChecker<?> vc = ai.findAutoDetectVisibility(ac, VisibilityChecker.Std.defaultInstance());
        Assert.assertNotNull(vc);

        VisibilityChecker<?> vc2 = ai.findAutoDetectVisibility(getAnnotatedClass(DummyBundleClass.class), VisibilityChecker.Std.defaultInstance());
        Assert.assertNotNull(vc2);
    }

    private static class MemberClass {
        @JsonProperty(value = "prop1", required = true, index = 3, defaultValue = "def1", access = JsonProperty.Access.READ_ONLY)
        @JsonPropertyDescription("desc1")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @JsonManagedReference("ref-name")
        public String field1;

        @JsonBackReference("ref-name")
        @JsonUnwrapped(enabled = true, prefix = "pre_", suffix = "_post")
        @JacksonInject(value = "injectId")
        public String field2;

        @JsonUnwrapped(enabled = false)
        @JacksonInject("")
        public String field3;

        @JsonIgnore(true)
        public String ignored;

        @Transient
        public String transientField;

        @JsonView({Object.class, String.class})
        @JsonRawValue
        public String fieldWithView;

        public MemberClass(@ConstructorProperties({"param1"}) String param1) {}

        @JsonSetter("methodSetter")
        public void setCustom(String p) {}

        @JsonGetter("methodGetter")
        public String getCustom() { return null; }

        @JsonValue(true)
        public String asValue() { return null; }

        @JsonAnySetter
        public void anySetter(String k, Object v) {}

        @JsonAnyGetter
        public java.util.Map<String, Object> anyGetter() { return null; }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public static MemberClass create() { return null; }

        @JacksonInject("")
        public void setInjectWithoutName(String val) {}

        @JacksonInject("")
        public void setInjectNoParams() {}
    }

    @Test
    public void testMemberAnnotations() throws Exception {
        AnnotatedField af1 = getAnnotatedFieldWithAnn(MemberClass.class, "field1");
        Assert.assertEquals(Boolean.TRUE, ai.hasRequiredMarker(af1));
        Assert.assertEquals(JsonProperty.Access.READ_ONLY, ai.findPropertyAccess(af1));
        Assert.assertEquals("desc1", ai.findPropertyDescription(af1));
        Assert.assertEquals(Integer.valueOf(3), ai.findPropertyIndex(af1));
        Assert.assertEquals("def1", ai.findPropertyDefaultValue(af1));
        Assert.assertNotNull(ai.findFormat(af1));
        AnnotationIntrospector.ReferenceProperty ref = ai.findReferenceType(af1);
        Assert.assertTrue(ref.isManagedReference());
        Assert.assertEquals("ref-name", ref.getName());

        AnnotatedField af2 = getAnnotatedFieldWithAnn(MemberClass.class, "field2");
        AnnotationIntrospector.ReferenceProperty backRef = ai.findReferenceType(af2);
        Assert.assertTrue(backRef.isBackReference());
        NameTransformer tf = ai.findUnwrappingNameTransformer(af2);
        Assert.assertEquals("pre_var_post", tf.transform("var"));
        Assert.assertEquals("injectId", ai.findInjectableValueId(af2));

        AnnotatedField af3 = getAnnotatedFieldWithAnn(MemberClass.class, "field3");
        Assert.assertNull(ai.findUnwrappingNameTransformer(af3));
        Assert.assertEquals(String.class.getName(), ai.findInjectableValueId(af3));

        AnnotatedMethod amInject = getAnnotatedMethod(MemberClass.class, "setInjectWithoutName", String.class);
        Assert.assertEquals(String.class.getName(), ai.findInjectableValueId(amInject));

        AnnotatedMethod amInjectNoParam = getAnnotatedMethod(MemberClass.class, "setInjectNoParams");
        Assert.assertEquals(void.class.getName(), ai.findInjectableValueId(amInjectNoParam));

        AnnotatedField afIgnored = getAnnotatedFieldWithAnn(MemberClass.class, "ignored");
        Assert.assertTrue(ai.hasIgnoreMarker(afIgnored));

        AnnotatedField afTrans = getAnnotatedFieldWithAnn(MemberClass.class, "transientField");
        Assert.assertTrue(ai.hasIgnoreMarker(afTrans));

        AnnotatedField afView = getAnnotatedFieldWithAnn(MemberClass.class, "fieldWithView");
        Class<?>[] views = ai.findViews(afView);
        Assert.assertArrayEquals(new Class<?>[]{Object.class, String.class}, views);
        Assert.assertTrue(ai.findSerializer(afView) instanceof RawSerializer);

        AnnotatedMethod amGetter = getAnnotatedMethod(MemberClass.class, "getCustom");
        Assert.assertEquals("methodGetter", ai.findNameForSerialization(amGetter).getSimpleName());

        AnnotatedMethod amSetter = getAnnotatedMethod(MemberClass.class, "setCustom", String.class);
        Assert.assertEquals("methodSetter", ai.findNameForDeserialization(amSetter).getSimpleName());

        AnnotatedMethod amValue = getAnnotatedMethod(MemberClass.class, "asValue");
        Assert.assertTrue(ai.hasAsValueAnnotation(amValue));

        AnnotatedMethod amAnySet = getAnnotatedMethod(MemberClass.class, "anySetter", String.class, Object.class);
        Assert.assertTrue(ai.hasAnySetterAnnotation(amAnySet));

        AnnotatedMethod amAnyGet = getAnnotatedMethod(MemberClass.class, "anyGetter");
        Assert.assertTrue(ai.hasAnyGetterAnnotation(amAnyGet));

        AnnotatedMethod amCreator = getAnnotatedMethod(MemberClass.class, "create");
        Assert.assertTrue(ai.hasCreatorAnnotation(amCreator));
        Assert.assertEquals(JsonCreator.Mode.PROPERTIES, ai.findCreatorBinding(amCreator));

        AnnotatedParameter ap = getAnnotatedParameter(MemberClass.class, 0, String.class);
        Assert.assertEquals("param1", ai.findImplicitPropertyName(ap));

        AnnotatedConstructor ctor = getAnnotatedConstructor(MemberClass.class, String.class);
        Assert.assertTrue(ai.hasCreatorAnnotation(ctor));
        ai.setConstructorPropertiesImpliesCreator(false);
        Assert.assertFalse(ai.hasCreatorAnnotation(ctor));
        ai.setConstructorPropertiesImpliesCreator(true);
    }

    private static class SetterConflict {
        public void setPrim(int x) {}
        public void setObject(Integer x) {}
        public void setStr(String x) {}
        public void setObj(Object x) {}
    }

    @Test
    public void testResolveSetterConflict() throws Exception {
        AnnotatedMethod mPrim = getAnnotatedMethod(SetterConflict.class, "setPrim", int.class);
        AnnotatedMethod mObj = getAnnotatedMethod(SetterConflict.class, "setObject", Integer.class);
        AnnotatedMethod mStr = getAnnotatedMethod(SetterConflict.class, "setStr", String.class);
        AnnotatedMethod mGeneric = getAnnotatedMethod(SetterConflict.class, "setObj", Object.class);

        Assert.assertSame(mPrim, ai.resolveSetterConflict(null, mPrim, mObj));
        Assert.assertSame(mPrim, ai.resolveSetterConflict(null, mObj, mPrim));
        Assert.assertSame(mStr, ai.resolveSetterConflict(null, mStr, mGeneric));
        Assert.assertSame(mStr, ai.resolveSetterConflict(null, mGeneric, mStr));
        Assert.assertNull(ai.resolveSetterConflict(null, mObj, mGeneric));
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type", defaultImpl = PolySub.class, visible = true)
    @JsonSubTypes({ @JsonSubTypes.Type(value = PolySub.class, name = "sub") })
    @JsonTypeName("base")
    private static class PolyBase {}

    @JsonTypeIdResolver(DummyIdResolver.class)
    @JsonTypeResolver(DummyTypeResolverBuilder.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_ARRAY)
    private static class CustomPoly {}

    private static class PolySub extends PolyBase {}

    public static class DummyIdResolver implements TypeIdResolver {
        public void init(JavaType baseType) {}
        public String idFromValue(Object value) { return null; }
        public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
        public String idFromBaseType() { return null; }
        public JavaType typeFromId(DatabindContext context, String id) { return null; }
        public String getDescForKnownTypeIds() { return null; }
        public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
    }

    public static class DummyTypeResolverBuilder extends StdTypeResolverBuilder {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private static class NoTypeInfoClass {}

    @Test
    public void testPolymorphicTypeAnnotations() {
        JavaType jt = TypeFactory.defaultInstance().constructType(PolyBase.class);
        AnnotatedClass ac = getAnnotatedClass(PolyBase.class);
        TypeResolverBuilder<?> b = ai.findTypeResolver(mapper.getSerializationConfig(), ac, jt);
        Assert.assertNotNull(b);
        Assert.assertEquals(PolySub.class, b.getDefaultImpl());

        List<NamedType> subtypes = ai.findSubtypes(ac);
        Assert.assertEquals(1, subtypes.size());
        Assert.assertEquals("sub", subtypes.get(0).getName());
        Assert.assertEquals("base", ai.findTypeName(ac));

        AnnotatedClass acNone = getAnnotatedClass(NoTypeInfoClass.class);
        TypeResolverBuilder<?> bNone = ai.findTypeResolver(mapper.getSerializationConfig(), acNone, jt);
        Assert.assertNotNull(bNone);

        AnnotatedClass acCustom = getAnnotatedClass(CustomPoly.class);
        TypeResolverBuilder<?> bCustom = ai.findTypeResolver(mapper.getSerializationConfig(), acCustom, jt);
        Assert.assertNotNull(bCustom);

        JavaType listType = TypeFactory.defaultInstance().constructCollectionType(List.class, PolyBase.class);
        TypeResolverBuilder<?> bCont = ai.findPropertyContentTypeResolver(mapper.getSerializationConfig(), ac, listType);
        Assert.assertNotNull(bCont);

        try {
            ai.findPropertyContentTypeResolver(mapper.getSerializationConfig(), ac, jt);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ObjectIdClass.class)
    @JsonIdentityReference(alwaysAsId = true)
    private static class ObjectIdClass {
        @JsonTypeId
        public String id;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.None.class)
    private static class ObjectIdNoneClass {}

    @Test
    public void testObjectId() throws Exception {
        AnnotatedClass ac = getAnnotatedClass(ObjectIdClass.class);
        ObjectIdInfo info = ai.findObjectIdInfo(ac);
        Assert.assertNotNull(info);
        Assert.assertEquals("id", info.getPropertyName().getSimpleName());
        Assert.assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());

        ObjectIdInfo refInfo = ai.findObjectReferenceInfo(ac, info);
        Assert.assertTrue(refInfo.getAlwaysAsId());

        AnnotatedField af = getAnnotatedFieldWithAnn(ObjectIdClass.class, "id");
        Assert.assertEquals(Boolean.TRUE, ai.isTypeId(af));

        AnnotatedClass acNone = getAnnotatedClass(ObjectIdNoneClass.class);
        Assert.assertNull(ai.findObjectIdInfo(acNone));
    }

    private static class DummyConverter implements Converter<Object, Object> {
        public Object convert(Object value) { return value; }
        public JavaType getInputType(TypeFactory typeFactory) { return null; }
        public JavaType getOutputType(TypeFactory typeFactory) { return null; }
    }

    @JsonSerialize(
            using = JsonSerializer.None.class,
            keyUsing = JsonSerializer.None.class,
            contentUsing = JsonSerializer.None.class,
            nullsUsing = JsonSerializer.None.class,
            as = String.class,
            keyAs = Integer.class,
            contentAs = Object.class,
            typing = JsonSerialize.Typing.DYNAMIC,
            converter = DummyConverter.class,
            contentConverter = DummyConverter.class,
            include = JsonSerialize.Inclusion.NON_NULL
    )
    @JsonDeserialize(
            using = JsonDeserializer.None.class,
            keyUsing = KeyDeserializer.None.class,
            contentUsing = JsonDeserializer.None.class,
            as = String.class,
            keyAs = Integer.class,
            contentAs = Object.class,
            converter = DummyConverter.class,
            contentConverter = DummyConverter.class,
            builder = BuilderClass.class
    )
    @JsonPropertyOrder(value = {"p2", "p1"}, alphabetic = true)
    @JsonValueInstantiator(ValueInstantiator.class)
    private static class SerDeserClass {}

    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "with")
    private static class BuilderClass {}

    @Test
    public void testSerDeserAnnotations() {
        AnnotatedClass ac = getAnnotatedClass(SerDeserClass.class);

        Assert.assertNull(ai.findSerializer(ac));
        Assert.assertNull(ai.findKeySerializer(ac));
        Assert.assertNull(ai.findContentSerializer(ac));
        Assert.assertNull(ai.findNullSerializer(ac));

        Assert.assertEquals(String.class, ai.findSerializationType(ac));
        Assert.assertEquals(Integer.class, ai.findSerializationKeyType(ac, null));
        Assert.assertEquals(Object.class, ai.findSerializationContentType(ac, null));
        Assert.assertEquals(JsonSerialize.Typing.DYNAMIC, ai.findSerializationTyping(ac));
        Assert.assertEquals(DummyConverter.class, ai.findSerializationConverter(ac));
        Assert.assertEquals(DummyConverter.class, ai.findSerializationContentConverter(ac));

        Assert.assertNull(ai.findDeserializer(ac));
        Assert.assertNull(ai.findKeyDeserializer(ac));
        Assert.assertNull(ai.findContentDeserializer(ac));
        Assert.assertEquals(String.class, ai.findDeserializationType(ac, null));
        Assert.assertEquals(Integer.class, ai.findDeserializationKeyType(ac, null));
        Assert.assertEquals(Object.class, ai.findDeserializationContentType(ac, null));
        Assert.assertEquals(DummyConverter.class, ai.findDeserializationConverter(ac));
        Assert.assertEquals(DummyConverter.class, ai.findDeserializationContentConverter(ac));
        Assert.assertEquals(BuilderClass.class, ai.findPOJOBuilder(ac));

        Assert.assertEquals(ValueInstantiator.class, ai.findValueInstantiator(ac));

        String[] order = ai.findSerializationPropertyOrder(ac);
        Assert.assertArrayEquals(new String[]{"p2", "p1"}, order);
        Assert.assertEquals(Boolean.TRUE, ai.findSerializationSortAlphabetically(ac));

        AnnotatedClass bAc = getAnnotatedClass(BuilderClass.class);
        JsonPOJOBuilder.Value bVal = ai.findPOJOBuilderConfig(bAc);
        Assert.assertNotNull(bVal);
        Assert.assertEquals("create", bVal.buildMethodName);
        Assert.assertEquals("with", bVal.withPrefix);
    }

    @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_ABSENT)
    private static class InclusionClass {}

    @Test
    public void testInclusionAnnotations() {
        AnnotatedClass ac = getAnnotatedClass(InclusionClass.class);
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, ai.findSerializationInclusion(ac, JsonInclude.Include.ALWAYS));
        Assert.assertEquals(JsonInclude.Include.NON_ABSENT, ai.findSerializationInclusionForContent(ac, JsonInclude.Include.ALWAYS));
        JsonInclude.Value val = ai.findPropertyInclusion(ac);
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, val.getValueInclusion());
        Assert.assertEquals(JsonInclude.Include.NON_ABSENT, val.getContentInclusion());

        AnnotatedClass acLegacy = getAnnotatedClass(SerDeserClass.class);
        Assert.assertEquals(JsonInclude.Include.NON_NULL, ai.findSerializationInclusion(acLegacy, JsonInclude.Include.ALWAYS));
        JsonInclude.Value valLegacy = ai.findPropertyInclusion(acLegacy);
        Assert.assertEquals(JsonInclude.Include.NON_NULL, valLegacy.getValueInclusion());
    }

    public static class CustomVirtualProp extends VirtualBeanPropertyWriter {
        public CustomVirtualProp() {}
        public CustomVirtualProp(BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType) {
            super(propDef, contextAnnotations, declaredType);
        }
        @Override
        protected Object value(Object bean, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider prov) {
            return "virt";
        }
        @Override
        public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
            return new CustomVirtualProp(propDef, declaringClass.getAnnotations(), type);
        }
    }

    @JsonAppend(
            attrs = { @JsonAppend.Attr(value = "attr1", propName = "pAttr1", required = true) },
            props = { @JsonAppend.Prop(value = CustomVirtualProp.class, name = "virtProp", type = String.class) },
            prepend = true
    )
    private static class AppendClass {}

    @Test
    public void testFindAndAddVirtualProperties() {
        AnnotatedClass ac = getAnnotatedClass(AppendClass.class);
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();
        ai.findAndAddVirtualProperties(mapper.getSerializationConfig(), ac, props);
        Assert.assertEquals(2, props.size());
    }

    private static class InferredNamesClass {
        @JsonSerialize
        public String serField;

        @JsonDeserialize
        public String deserField;
    }

    @Test
    public void testInferredNames() throws Exception {
        AnnotatedField afSer = getAnnotatedFieldWithAnn(InferredNamesClass.class, "serField");
        Assert.assertEquals(PropertyName.USE_DEFAULT, ai.findNameForSerialization(afSer));

        AnnotatedField afDeser = getAnnotatedFieldWithAnn(InferredNamesClass.class, "deserField");
        Assert.assertEquals(PropertyName.USE_DEFAULT, ai.findNameForDeserialization(afDeser));
    }

    @Test
    public void testHelperMethods() {
        Assert.assertNull(ai._classIfExplicit(null));
        Assert.assertNull(ai._classIfExplicit(NoClass.class));
        Assert.assertEquals(String.class, ai._classIfExplicit(String.class));
        Assert.assertNull(ai._classIfExplicit(String.class, String.class));
        Assert.assertEquals(String.class, ai._classIfExplicit(String.class, Integer.class));

        PropertyName pn1 = ai._propertyName("", "");
        Assert.assertEquals(PropertyName.USE_DEFAULT, pn1);

        PropertyName pn2 = ai._propertyName("name", "");
        Assert.assertEquals("name", pn2.getSimpleName());
        Assert.assertNull(pn2.getNamespace());

        PropertyName pn3 = ai._propertyName("name", "ns");
        Assert.assertEquals("name", pn3.getSimpleName());
        Assert.assertEquals("ns", pn3.getNamespace());
    }
}
