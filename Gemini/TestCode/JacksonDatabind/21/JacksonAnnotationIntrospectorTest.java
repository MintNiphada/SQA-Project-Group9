package com.fasterxml.jackson.databind.introspect;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
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

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector introspector;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        introspector = new JacksonAnnotationIntrospector();
        mapper = new ObjectMapper();
    }

    private AnnotatedClass getAnnotatedClass(Class<?> cls) {
        return mapper.getSerializationConfig().introspectClassAnnotations(cls);
    }

    private AnnotatedMethod getAnnotatedMethod(Class<?> cls, String methodName, Class<?>... paramTypes) throws Exception {
        AnnotatedClass ac = getAnnotatedClass(cls);
        return ac.findMethod(methodName, paramTypes);
    }

    private AnnotatedField getAnnotatedField(Class<?> cls, String fieldName) {
        AnnotatedClass ac = getAnnotatedClass(cls);
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        return null;
    }

    // --- Version and Annotation Bundle Tests ---

    @Test
    public void testVersion() {
        Version v = introspector.version();
        Assert.assertNotNull(v);
        Assert.assertFalse(v.isUnknownVersion());
    }

    @JacksonAnnotationsInside
    @Retention(RetentionPolicy.RUNTIME)
    @interface CustomBundle {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface NotBundle {
    }

    @CustomBundle
    @NotBundle
    static class BundleTarget {
    }

    @Test
    public void testIsAnnotationBundle() {
        CustomBundle bundleAnn = BundleTarget.class.getAnnotation(CustomBundle.class);
        NotBundle notBundleAnn = BundleTarget.class.getAnnotation(NotBundle.class);

        Assert.assertTrue(introspector.isAnnotationBundle(bundleAnn));
        Assert.assertFalse(introspector.isAnnotationBundle(notBundleAnn));
    }

    // --- Root Name and Class-Level Annotations ---

    @JsonRootName(value = "root", namespace = "http://example.com")
    static class RootWithNamespace {
    }

    @JsonRootName(value = "rootEmptyNs", namespace = "")
    static class RootWithEmptyNamespace {
    }

    static class NoRootName {
    }

    @Test
    public void testFindRootName() {
        PropertyName pn = introspector.findRootName(getAnnotatedClass(RootWithNamespace.class));
        Assert.assertNotNull(pn);
        Assert.assertEquals("root", pn.getSimpleName());
        Assert.assertEquals("http://example.com", pn.getNamespace());

        PropertyName pn2 = introspector.findRootName(getAnnotatedClass(RootWithEmptyNamespace.class));
        Assert.assertNotNull(pn2);
        Assert.assertEquals("rootEmptyNs", pn2.getSimpleName());
        Assert.assertNull(pn2.getNamespace());

        Assert.assertNull(introspector.findRootName(getAnnotatedClass(NoRootName.class)));
    }

    @JsonIgnoreProperties(value = {"a", "b"}, allowGetters = true, allowSetters = false, ignoreUnknown = true)
    static class IgnoreClassSerialization {
    }

    @JsonIgnoreProperties(value = {"c", "d"}, allowGetters = false, allowSetters = true, ignoreUnknown = false)
    static class IgnoreClassDeserialization {
    }

    @Test
    public void testFindPropertiesToIgnore() {
        AnnotatedClass acSer = getAnnotatedClass(IgnoreClassSerialization.class);
        AnnotatedClass acDeser = getAnnotatedClass(IgnoreClassDeserialization.class);

        @SuppressWarnings("deprecation")
        String[] deprecatedProps = introspector.findPropertiesToIgnore(acSer);
        Assert.assertArrayEquals(new String[]{"a", "b"}, deprecatedProps);

        Assert.assertNull(introspector.findPropertiesToIgnore(acSer, true)); // allowGetters == true -> null for ser
        Assert.assertArrayEquals(new String[]{"a", "b"}, introspector.findPropertiesToIgnore(acSer, false)); // allowSetters == false

        Assert.assertArrayEquals(new String[]{"c", "d"}, introspector.findPropertiesToIgnore(acDeser, true));
        Assert.assertNull(introspector.findPropertiesToIgnore(acDeser, false)); // allowSetters == true -> null for deser

        Assert.assertNull(introspector.findPropertiesToIgnore(getAnnotatedClass(NoRootName.class)));
        Assert.assertNull(introspector.findPropertiesToIgnore(getAnnotatedClass(NoRootName.class), true));

        Assert.assertEquals(Boolean.TRUE, introspector.findIgnoreUnknownProperties(acSer));
        Assert.assertEquals(Boolean.FALSE, introspector.findIgnoreUnknownProperties(acDeser));
        Assert.assertNull(introspector.findIgnoreUnknownProperties(getAnnotatedClass(NoRootName.class)));
    }

    @JsonIgnoreType(true)
    static class IgnoredType {
    }

    static class NotIgnoredType {
    }

    @Test
    public void testIsIgnorableType() {
        Assert.assertEquals(Boolean.TRUE, introspector.isIgnorableType(getAnnotatedClass(IgnoredType.class)));
        Assert.assertNull(introspector.isIgnorableType(getAnnotatedClass(NotIgnoredType.class)));
    }

    @JsonFilter("myFilter")
    static class FilteredClass {
    }

    @JsonFilter("")
    static class EmptyFilterClass {
    }

    @Test
    public void testFindFilterId() {
        AnnotatedClass ac = getAnnotatedClass(FilteredClass.class);
        Assert.assertEquals("myFilter", introspector.findFilterId(ac));
        @SuppressWarnings("deprecation")
        Object deprecatedId = introspector.findFilterId((Annotated) ac);
        Assert.assertEquals("myFilter", deprecatedId);

        AnnotatedClass acEmpty = getAnnotatedClass(EmptyFilterClass.class);
        Assert.assertNull(introspector.findFilterId(acEmpty));
        Assert.assertNull(introspector.findFilterId(getAnnotatedClass(NoRootName.class)));
    }

    static class DummyNamingStrategy extends PropertyNamingStrategy {
    }

    @JsonNaming(DummyNamingStrategy.class)
    static class NamingClass {
    }

    @Test
    public void testFindNamingStrategy() {
        Assert.assertEquals(DummyNamingStrategy.class, introspector.findNamingStrategy(getAnnotatedClass(NamingClass.class)));
        Assert.assertNull(introspector.findNamingStrategy(getAnnotatedClass(NoRootName.class)));
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    static class AutoDetectClass {
    }

    @Test
    public void testFindAutoDetectVisibility() {
        VisibilityChecker<?> checker = mapper.getSerializationConfig().getDefaultVisibilityChecker();
        VisibilityChecker<?> modified = introspector.findAutoDetectVisibility(getAnnotatedClass(AutoDetectClass.class), checker);
        Assert.assertNotNull(modified);
        Assert.assertNotSame(checker, modified);

        VisibilityChecker<?> untouched = introspector.findAutoDetectVisibility(getAnnotatedClass(NoRootName.class), checker);
        Assert.assertSame(checker, untouched);
    }

    // --- Property Markers, Names, Modifiers ---

    static class MemberTestClass {
        @JsonProperty(value = "prop1", required = true, index = 3, defaultValue = "defVal", access = JsonProperty.Access.READ_ONLY)
        @JsonPropertyDescription("A description")
        public String prop1;

        @JsonProperty(value = "", index = JsonProperty.INDEX_UNKNOWN, defaultValue = "")
        public String prop2;

        @JsonIgnore
        public String ignoredProp;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public Date formattedDate;

        @JsonManagedReference("parent-child")
        public Object managedRef;

        @JsonBackReference("parent-child")
        public Object backRef;

        @JsonUnwrapped(prefix = "pre_", suffix = "_post", enabled = true)
        public Object unwrappedProp;

        @JsonUnwrapped(enabled = false)
        public Object disabledUnwrappedProp;

        @JacksonInject("injectId")
        public String injectedNamed;

        @JacksonInject
        public Integer injectedImplicit;

        @JsonView({String.class, Integer.class})
        public String viewedProp;

        @JsonGetter("customGetter")
        public String getFoo() { return "foo"; }

        @JsonSetter("customSetter")
        public void setFoo(String f) { }

        @JsonValue
        public String asValueMethod() { return "val"; }

        @JacksonInject
        public void injectMethodNoArg() { }

        @JacksonInject
        public void injectMethodWithArg(Double d) { }

        @JsonAnySetter
        public void anySetter(String k, Object v) { }

        @JsonAnyGetter
        public Map<String, Object> anyGetter() { return Collections.emptyMap(); }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public MemberTestClass(@JsonProperty("prop1") String prop1) { this.prop1 = prop1; }

        @JsonCreator(mode = JsonCreator.Mode.DISABLED)
        public static MemberTestClass disabledCreator() { return null; }
    }

    @Test
    public void testMemberAnnotations() throws Exception {
        AnnotatedField f1 = getAnnotatedField(MemberTestClass.class, "prop1");
        AnnotatedField f2 = getAnnotatedField(MemberTestClass.class, "prop2");
        AnnotatedField fIgnored = getAnnotatedField(MemberTestClass.class, "ignoredProp");
        AnnotatedField fDate = getAnnotatedField(MemberTestClass.class, "formattedDate");

        Assert.assertNull(introspector.findImplicitPropertyName(f1));
        Assert.assertEquals(Boolean.TRUE, introspector.hasRequiredMarker(f1));
        Assert.assertEquals(JsonProperty.Access.READ_ONLY, introspector.findPropertyAccess(f1));
        Assert.assertEquals("A description", introspector.findPropertyDescription(f1));
        Assert.assertEquals(Integer.valueOf(3), introspector.findPropertyIndex(f1));
        Assert.assertEquals("defVal", introspector.findPropertyDefaultValue(f1));

        Assert.assertNull(introspector.findPropertyIndex(f2));
        Assert.assertNull(introspector.findPropertyDefaultValue(f2));

        Assert.assertTrue(introspector.hasIgnoreMarker(fIgnored));
        Assert.assertFalse(introspector.hasIgnoreMarker(f1));

        JsonFormat.Value fmt = introspector.findFormat(fDate);
        Assert.assertNotNull(fmt);
        Assert.assertEquals("yyyy-MM-dd", fmt.getPattern());
        Assert.assertNull(introspector.findFormat(f1));
    }

    @Test
    public void testReferenceProperties() {
        AnnotatedField fManaged = getAnnotatedField(MemberTestClass.class, "managedRef");
        AnnotatedField fBack = getAnnotatedField(MemberTestClass.class, "backRef");
        AnnotatedField f1 = getAnnotatedField(MemberTestClass.class, "prop1");

        AnnotationIntrospector.ReferenceProperty refManaged = introspector.findReferenceType(fManaged);
        Assert.assertNotNull(refManaged);
        Assert.assertTrue(refManaged.isManagedReference());
        Assert.assertEquals("parent-child", refManaged.getName());

        AnnotationIntrospector.ReferenceProperty refBack = introspector.findReferenceType(fBack);
        Assert.assertNotNull(refBack);
        Assert.assertTrue(refBack.isBackReference());
        Assert.assertEquals("parent-child", refBack.getName());

        Assert.assertNull(introspector.findReferenceType(f1));
    }

    @Test
    public void testUnwrappingNameTransformer() {
        AnnotatedField fUnwrapped = getAnnotatedField(MemberTestClass.class, "unwrappedProp");
        AnnotatedField fDisabled = getAnnotatedField(MemberTestClass.class, "disabledUnwrappedProp");
        AnnotatedField f1 = getAnnotatedField(MemberTestClass.class, "prop1");

        NameTransformer transformer = introspector.findUnwrappingNameTransformer(fUnwrapped);
        Assert.assertNotNull(transformer);
        Assert.assertEquals("pre_name_post", transformer.transform("name"));

        Assert.assertNull(introspector.findUnwrappingNameTransformer(fDisabled));
        Assert.assertNull(introspector.findUnwrappingNameTransformer(f1));
    }

    @Test
    public void testInjectableValueId() throws Exception {
        AnnotatedField fNamed = getAnnotatedField(MemberTestClass.class, "injectedNamed");
        AnnotatedField fImplicit = getAnnotatedField(MemberTestClass.class, "injectedImplicit");
        AnnotatedMethod mNoArg = getAnnotatedMethod(MemberTestClass.class, "injectMethodNoArg");
        AnnotatedMethod mWithArg = getAnnotatedMethod(MemberTestClass.class, "injectMethodWithArg", Double.class);
        AnnotatedField f1 = getAnnotatedField(MemberTestClass.class, "prop1");

        Assert.assertEquals("injectId", introspector.findInjectableValueId(fNamed));
        Assert.assertEquals(Integer.class.getName(), introspector.findInjectableValueId(fImplicit));
        Assert.assertEquals(void.class.getName(), introspector.findInjectableValueId(mNoArg));
        Assert.assertEquals(Double.class.getName(), introspector.findInjectableValueId(mWithArg));
        Assert.assertNull(introspector.findInjectableValueId(f1));
    }

    @Test
    public void testViews() {
        AnnotatedField fView = getAnnotatedField(MemberTestClass.class, "viewedProp");
        Class<?>[] views = introspector.findViews(fView);
        Assert.assertNotNull(views);
        Assert.assertEquals(2, views.length);
        Assert.assertEquals(String.class, views[0]);

        Assert.assertNull(introspector.findViews(getAnnotatedField(MemberTestClass.class, "prop1")));
    }

    @Test
    public void testGettersSettersAndMiscMethodAnnotations() throws Exception {
        AnnotatedMethod mGet = getAnnotatedMethod(MemberTestClass.class, "getFoo");
        AnnotatedMethod mSet = getAnnotatedMethod(MemberTestClass.class, "setFoo", String.class);
        AnnotatedMethod mVal = getAnnotatedMethod(MemberTestClass.class, "asValueMethod");
        AnnotatedMethod mAnySet = getAnnotatedMethod(MemberTestClass.class, "anySetter", String.class, Object.class);
        AnnotatedMethod mAnyGet = getAnnotatedMethod(MemberTestClass.class, "anyGetter");

        Assert.assertEquals("customGetter", introspector.findNameForSerialization(mGet).getSimpleName());
        Assert.assertEquals("customSetter", introspector.findNameForDeserialization(mSet).getSimpleName());
        Assert.assertTrue(introspector.hasAsValueAnnotation(mVal));
        Assert.assertFalse(introspector.hasAsValueAnnotation(mGet));

        Assert.assertTrue(introspector.hasAnySetterAnnotation(mAnySet));
        Assert.assertFalse(introspector.hasAnySetterAnnotation(mGet));
        Assert.assertTrue(introspector.hasAnyGetterAnnotation(mAnyGet));
        Assert.assertFalse(introspector.hasAnyGetterAnnotation(mGet));
    }

    @Test
    public void testCreatorAnnotations() throws Exception {
        AnnotatedClass ac = getAnnotatedClass(MemberTestClass.class);
        AnnotatedConstructor ctor = null;
        for (AnnotatedConstructor c : ac.getConstructors()) {
            if (c.getParameterCount() == 1) {
                ctor = c;
                break;
            }
        }
        Assert.assertNotNull(ctor);
        Assert.assertTrue(introspector.hasCreatorAnnotation(ctor));
        Assert.assertEquals(JsonCreator.Mode.PROPERTIES, introspector.findCreatorBinding(ctor));

        AnnotatedMethod disabledMethod = ac.findMethod("disabledCreator", new Class<?>[0]);
        Assert.assertFalse(introspector.hasCreatorAnnotation(disabledMethod));
        Assert.assertEquals(JsonCreator.Mode.DISABLED, introspector.findCreatorBinding(disabledMethod));

        AnnotatedMethod mGet = ac.findMethod("getFoo", new Class<?>[0]);
        Assert.assertFalse(introspector.hasCreatorAnnotation(mGet));
        Assert.assertNull(introspector.findCreatorBinding(mGet));
    }

    // --- Polymorphic Typing & Subtypes ---

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SubTypeA.class, name = "subA"),
            @JsonSubTypes.Type(value = SubTypeB.class, name = "subB")
    })
    @JsonTypeName("baseType")
    static class BasePoly {
    }

    static class SubTypeA extends BasePoly {
    }

    static class SubTypeB extends BasePoly {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    static class NoTypeInfoClass {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
    static class ExternalPropOnClass {
    }

    @Test
    public void testPolymorphicTypeResolver() {
        AnnotatedClass ac = getAnnotatedClass(BasePoly.class);
        JavaType baseType = TypeFactory.defaultInstance().constructType(BasePoly.class);

        TypeResolverBuilder<?> b = introspector.findTypeResolver(mapper.getSerializationConfig(), ac, baseType);
        Assert.assertNotNull(b);

        List<NamedType> subtypes = introspector.findSubtypes(ac);
        Assert.assertNotNull(subtypes);
        Assert.assertEquals(2, subtypes.size());
        Assert.assertEquals("subA", subtypes.get(0).getName());
        Assert.assertEquals(SubTypeA.class, subtypes.get(0).getType());

        Assert.assertEquals("baseType", introspector.findTypeName(ac));
        Assert.assertNull(introspector.findTypeName(getAnnotatedClass(SubTypeA.class)));

        TypeResolverBuilder<?> noTypeB = introspector.findTypeResolver(mapper.getSerializationConfig(),
                getAnnotatedClass(NoTypeInfoClass.class), baseType);
        Assert.assertNotNull(noTypeB);

        // Test External Property mapped to Property when applied to AnnotatedClass
        AnnotatedClass acExt = getAnnotatedClass(ExternalPropOnClass.class);
        TypeResolverBuilder<?> bExt = introspector.findTypeResolver(mapper.getSerializationConfig(), acExt, baseType);
        Assert.assertNotNull(bExt);
    }

    static class ContainerHolder {
        public List<String> list;
        public String nonContainer;
        @JsonTypeId
        public String typeIdField;
    }

    @Test
    public void testPropertyTypeResolvers() {
        AnnotatedField fList = getAnnotatedField(ContainerHolder.class, "list");
        AnnotatedField fNonCont = getAnnotatedField(ContainerHolder.class, "nonContainer");
        AnnotatedField fTypeId = getAnnotatedField(ContainerHolder.class, "typeIdField");

        JavaType containerType = TypeFactory.defaultInstance().constructType(List.class);
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);

        Assert.assertNull(introspector.findPropertyTypeResolver(mapper.getSerializationConfig(), fList, containerType));
        Assert.assertNull(introspector.findPropertyTypeResolver(mapper.getSerializationConfig(), fNonCont, stringType));

        Assert.assertNull(introspector.findPropertyContentTypeResolver(mapper.getSerializationConfig(), fList, containerType));
        try {
            introspector.findPropertyContentTypeResolver(mapper.getSerializationConfig(), fNonCont, stringType);
            Assert.fail("Should have thrown IllegalArgumentException for non-container type");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Must call method with a container type"));
        }

        Assert.assertTrue(introspector.isTypeId(fTypeId));
        Assert.assertFalse(introspector.isTypeId(fList));
    }

    // --- Object Identity ---

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ObjectIdentityClass.class)
    @JsonIdentityReference(alwaysAsId = true)
    static class ObjectIdentityClass {
        public int id;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.None.class, property = "none")
    static class NoneGeneratorClass {
    }

    @Test
    public void testObjectIdHandling() {
        AnnotatedClass ac = getAnnotatedClass(ObjectIdentityClass.class);
        ObjectIdInfo info = introspector.findObjectIdInfo(ac);
        Assert.assertNotNull(info);
        Assert.assertEquals("id", info.getPropertyName().getSimpleName());
        Assert.assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());
        Assert.assertEquals(ObjectIdentityClass.class, info.getScope());

        ObjectIdInfo refInfo = introspector.findObjectReferenceInfo(ac, info);
        Assert.assertTrue(refInfo.getAlwaysAsId());

        Assert.assertNull(introspector.findObjectIdInfo(getAnnotatedClass(NoneGeneratorClass.class)));
        Assert.assertNull(introspector.findObjectIdInfo(getAnnotatedClass(NoRootName.class)));
    }

    // --- Serialization Details ---

    static class DummySerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider serializers) {}
    }

    static class DummyKeySerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider serializers) {}
    }

    static class DummyConverter implements Converter<String, String> {
        @Override public String convert(String value) { return value; }
        @Override public JavaType getInputType(TypeFactory typeFactory) { return typeFactory.constructType(String.class); }
        @Override public JavaType getOutputType(TypeFactory typeFactory) { return typeFactory.constructType(String.class); }
    }

    @JsonSerialize(
            using = DummySerializer.class,
            keyUsing = DummyKeySerializer.class,
            contentUsing = DummySerializer.class,
            nullsUsing = DummySerializer.class,
            as = String.class,
            keyAs = String.class,
            contentAs = String.class,
            typing = JsonSerialize.Typing.STATIC,
            converter = DummyConverter.class,
            contentConverter = DummyConverter.class,
            include = JsonSerialize.Inclusion.NON_NULL
    )
    static class FullSerializeTarget {
        @JsonRawValue
        public String rawVal;
    }

    @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_DEFAULT)
    static class FullIncludeTarget {
    }

    @JsonPropertyOrder(value = {"prop2", "prop1"}, alphabetic = true)
    static class OrderedClass {
    }

    @Test
    public void testSerializationAnnotations() {
        AnnotatedClass ac = getAnnotatedClass(FullSerializeTarget.class);
        AnnotatedField fRaw = getAnnotatedField(FullSerializeTarget.class, "rawVal");

        Assert.assertEquals(DummySerializer.class, introspector.findSerializer(ac));
        Object rawSer = introspector.findSerializer(fRaw);
        Assert.assertTrue(rawSer instanceof RawSerializer<?>);

        Assert.assertEquals(DummyKeySerializer.class, introspector.findKeySerializer(ac));
        Assert.assertEquals(DummySerializer.class, introspector.findContentSerializer(ac));
        Assert.assertEquals(DummySerializer.class, introspector.findNullSerializer(ac));

        Assert.assertEquals(String.class, introspector.findSerializationType(ac));
        JavaType jt = TypeFactory.defaultInstance().constructType(String.class);
        Assert.assertEquals(String.class, introspector.findSerializationKeyType(ac, jt));
        Assert.assertEquals(String.class, introspector.findSerializationContentType(ac, jt));
        Assert.assertEquals(JsonSerialize.Typing.STATIC, introspector.findSerializationTyping(ac));
        Assert.assertEquals(DummyConverter.class, introspector.findSerializationConverter(ac));
        Assert.assertEquals(DummyConverter.class, introspector.findSerializationContentConverter(fRaw));

        Assert.assertEquals(JsonInclude.Include.NON_NULL, introspector.findSerializationInclusion(ac, JsonInclude.Include.ALWAYS));

        AnnotatedClass acInc = getAnnotatedClass(FullIncludeTarget.class);
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, introspector.findSerializationInclusion(acInc, JsonInclude.Include.ALWAYS));
        Assert.assertEquals(JsonInclude.Include.NON_DEFAULT, introspector.findSerializationInclusionForContent(acInc, JsonInclude.Include.ALWAYS));
        Assert.assertEquals(JsonInclude.Include.ALWAYS, introspector.findSerializationInclusionForContent(ac, JsonInclude.Include.ALWAYS));

        AnnotatedClass acOrder = getAnnotatedClass(OrderedClass.class);
        Assert.assertArrayEquals(new String[]{"prop2", "prop1"}, introspector.findSerializationPropertyOrder(acOrder));
        Assert.assertEquals(Boolean.TRUE, introspector.findSerializationSortAlphabetically(acOrder));
        @SuppressWarnings("deprecation")
        Boolean deprecatedAlpha = introspector.findSerializationSortAlphabetically((Annotated) acOrder);
        Assert.assertEquals(Boolean.TRUE, deprecatedAlpha);
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.ALWAYS)
    static class IncAlways {}
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
    static class IncNonDefault {}
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
    static class IncNonEmpty {}
    @JsonSerialize(include = JsonSerialize.Inclusion.DEFAULT_INCLUSION)
    static class IncDefaultInclusion {}

    @Test
    public void testJsonSerializeInclusionBranches() {
        Assert.assertEquals(JsonInclude.Include.ALWAYS, introspector.findSerializationInclusion(getAnnotatedClass(IncAlways.class), JsonInclude.Include.USE_DEFAULTS));
        Assert.assertEquals(JsonInclude.Include.NON_DEFAULT, introspector.findSerializationInclusion(getAnnotatedClass(IncNonDefault.class), JsonInclude.Include.USE_DEFAULTS));
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, introspector.findSerializationInclusion(getAnnotatedClass(IncNonEmpty.class), JsonInclude.Include.USE_DEFAULTS));
        Assert.assertEquals(JsonInclude.Include.USE_DEFAULTS, introspector.findSerializationInclusion(getAnnotatedClass(IncDefaultInclusion.class), JsonInclude.Include.USE_DEFAULTS));
    }

    // --- Deserialization Details ---

    static class DummyDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) { return null; }
    }

    static class DummyKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) { return key; }
    }

    static class DummyValueInstantiator extends com.fasterxml.jackson.databind.deser.ValueInstantiator {
        @Override public String getValueTypeDesc() { return "dummy"; }
    }

    static class DummyBuilder {
        public DummyBuilder withX(String x) { return this; }
        public String build() { return ""; }
    }

    @JsonDeserialize(
            using = DummyDeserializer.class,
            keyUsing = DummyKeyDeserializer.class,
            contentUsing = DummyDeserializer.class,
            as = String.class,
            keyAs = String.class,
            contentAs = String.class,
            converter = DummyConverter.class,
            contentConverter = DummyConverter.class,
            builder = DummyBuilder.class
    )
    @JsonValueInstantiator(DummyValueInstantiator.class)
    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    static class FullDeserializeTarget {
        @JsonDeserialize
        public String deserProp;

        @JsonView(String.class)
        public String viewOnlyDeser;

        @JsonUnwrapped
        public String unwrappedDeser;

        @JsonBackReference
        public String backRefDeser;

        @JsonManagedReference
        public String managedRefDeser;
    }

    @Test
    public void testDeserializationAnnotations() {
        AnnotatedClass ac = getAnnotatedClass(FullDeserializeTarget.class);
        AnnotatedField fDeser = getAnnotatedField(FullDeserializeTarget.class, "deserProp");

        Assert.assertEquals(DummyDeserializer.class, introspector.findDeserializer(ac));
        Assert.assertEquals(DummyKeyDeserializer.class, introspector.findKeyDeserializer(ac));
        Assert.assertEquals(DummyDeserializer.class, introspector.findContentDeserializer(ac));

        JavaType jt = TypeFactory.defaultInstance().constructType(String.class);
        Assert.assertEquals(String.class, introspector.findDeserializationType(ac, jt));
        Assert.assertEquals(String.class, introspector.findDeserializationKeyType(ac, jt));
        Assert.assertEquals(String.class, introspector.findDeserializationContentType(ac, jt));
        Assert.assertEquals(DummyConverter.class, introspector.findDeserializationConverter(ac));
        Assert.assertEquals(DummyConverter.class, introspector.findDeserializationContentConverter(fDeser));

        Assert.assertEquals(DummyValueInstantiator.class, introspector.findValueInstantiator(ac));
        Assert.assertEquals(DummyBuilder.class, introspector.findPOJOBuilder(ac));

        JsonPOJOBuilder.Value bConfig = introspector.findPOJOBuilderConfig(ac);
        Assert.assertNotNull(bConfig);
        Assert.assertEquals("build", bConfig.buildMethodName);
        Assert.assertEquals("with", bConfig.withPrefix);

        // Deserialization implicit names
        Assert.assertEquals("", introspector.findNameForDeserialization(fDeser).getSimpleName());
        Assert.assertEquals("", introspector.findNameForDeserialization(getAnnotatedField(FullDeserializeTarget.class, "viewOnlyDeser")).getSimpleName());
        Assert.assertEquals("", introspector.findNameForDeserialization(getAnnotatedField(FullDeserializeTarget.class, "unwrappedDeser")).getSimpleName());
        Assert.assertEquals("", introspector.findNameForDeserialization(getAnnotatedField(FullDeserializeTarget.class, "backRefDeser")).getSimpleName());
        Assert.assertEquals("", introspector.findNameForDeserialization(getAnnotatedField(FullDeserializeTarget.class, "managedRefDeser")).getSimpleName());
        Assert.assertNull(introspector.findNameForDeserialization(getAnnotatedClass(NoRootName.class)));
    }

    // --- Virtual Properties (JsonAppend) ---

    public static class CustomVirtualPropWriter extends VirtualBeanPropertyWriter implements Serializable {
        private static final long serialVersionUID = 1L;
        public CustomVirtualPropWriter() {}
        public CustomVirtualPropWriter(BeanPropertyDefinition propDef, Annotations contextAnnotations, JavaType declaredType) {
            super(propDef, contextAnnotations, declaredType);
        }
        @Override
        protected Object value(Object bean, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider prov) {
            return "virt";
        }
        @Override
        public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass,
                                                    BeanPropertyDefinition propDef, JavaType type) {
            return new CustomVirtualPropWriter(propDef, declaringClass.getAnnotations(), type);
        }
    }

    @JsonAppend(
            prepend = true,
            attrs = {
                    @JsonAppend.Attr(value = "attr1", propName = "propAttr1", required = true, include = JsonInclude.Include.NON_NULL)
            },
            props = {
                    @JsonAppend.Prop(value = CustomVirtualPropWriter.class, name = "virtProp", required = false)
            }
    )
    static class VirtualTarget {
    }

    @Test
    public void testFindAndAddVirtualProperties() {
        AnnotatedClass ac = getAnnotatedClass(VirtualTarget.class);
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();

        introspector.findAndAddVirtualProperties(mapper.getSerializationConfig(), ac, props);
        Assert.assertEquals(2, props.size());
        // Since prepend = true, props are inserted at beginning
        Assert.assertNotNull(props.get(0));
        Assert.assertNotNull(props.get(1));

        List<BeanPropertyWriter> emptyProps = new ArrayList<BeanPropertyWriter>();
        introspector.findAndAddVirtualProperties(mapper.getSerializationConfig(), getAnnotatedClass(NoRootName.class), emptyProps);
        Assert.assertTrue(emptyProps.isEmpty());
    }

    @Test
    public void testVirtualPropertiesWithHandlerInstantiator() {
        final CustomVirtualPropWriter customWriter = new CustomVirtualPropWriter();
        HandlerInstantiator hi = new HandlerInstantiator() {
            @Override public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) { return null; }
            @Override public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) { return null; }
            @Override public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) { return null; }
            @Override public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) { return null; }
            @Override public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) { return null; }
            @Override
            public VirtualBeanPropertyWriter virtualPropertyWriterInstance(MapperConfig<?> config, Class<?> implClass) {
                if (implClass == CustomVirtualPropWriter.class) {
                    return customWriter;
                }
                return null;
            }
        };

        ObjectMapper customMapper = new ObjectMapper();
        customMapper.setHandlerInstantiator(hi);

        AnnotatedClass ac = customMapper.getSerializationConfig().introspectClassAnnotations(VirtualTarget.class);
        List<BeanPropertyWriter> props = new ArrayList<BeanPropertyWriter>();
        introspector.findAndAddVirtualProperties(customMapper.getSerializationConfig(), ac, props);
        Assert.assertEquals(2, props.size());
    }

    // --- Helper & Utility Method Tests ---

    @Test
    public void testPropertyNameHelper() {
        PropertyName p1 = introspector._propertyName("", null);
        Assert.assertSame(PropertyName.USE_DEFAULT, p1);

        PropertyName p2 = introspector._propertyName("local", "");
        Assert.assertEquals("local", p2.getSimpleName());
        Assert.assertNull(p2.getNamespace());

        PropertyName p3 = introspector._propertyName("local", "ns");
        Assert.assertEquals("local", p3.getSimpleName());
        Assert.assertEquals("ns", p3.getNamespace());
    }

    @Test
    public void testClassIfExplicitHelper() {
        Assert.assertNull(introspector._classIfExplicit(null));
        Assert.assertNull(introspector._classIfExplicit(NoClass.class));
        Assert.assertEquals(String.class, introspector._classIfExplicit(String.class));

        Assert.assertNull(introspector._classIfExplicit(Converter.None.class, Converter.None.class));
        Assert.assertEquals(DummyConverter.class, introspector._classIfExplicit(DummyConverter.class, Converter.None.class));
    }
}
