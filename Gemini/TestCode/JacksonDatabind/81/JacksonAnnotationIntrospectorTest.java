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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class JacksonAnnotationIntrospectorTest {

    private JacksonAnnotationIntrospector ai;
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        ai = new JacksonAnnotationIntrospector();
        mapper = new ObjectMapper();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonIgnore
    private @interface BundleAnno {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface NonBundleAnno {}

    private enum TestEnum {
        @JsonProperty("a_val")
        A,
        @JsonProperty("")
        B,
        @JsonEnumDefaultValue
        DEFAULT_VAL,
        PLAIN
    }

    @JsonRootName(value = "root", namespace = "http://example.com")
    @JsonIgnoreProperties(value = {"ignored1"}, allowGetters = true)
    @JsonIgnoreType(true)
    @JsonFilter("filter123")
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    @JsonClassDescription("class description test")
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    @JsonPropertyOrder(value = {"propB", "propA"}, alphabetic = true)
    @JsonTypeName("CustomTypeName")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = String.class, name = "str"),
            @JsonSubTypes.Type(value = Integer.class, name = "int")
    })
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type", defaultImpl = String.class, visible = true)
    private static class AnnotatedRootClass {}

    @JsonRootName("")
    @JsonFilter("")
    private static class EmptyRootClass {}

    @JsonAppend(
            prepend = true,
            attrs = {
                    @JsonAppend.Attr(value = "attr1", propName = "prop1", propNamespace = "ns1", required = true, include = JsonInclude.Include.NON_EMPTY),
                    @JsonAppend.Attr(value = "attr2", propName = "", propNamespace = "", required = false, include = JsonInclude.Include.ALWAYS)
            },
            props = {
                    @JsonAppend.Prop(value = DummyVirtualPropWriter.class, name = "vp1", namespace = "ns2", type = String.class, required = true, include = JsonInclude.Include.NON_NULL)
            }
    )
    private static class AppendTargetClass {}

    public static class DummyVirtualPropWriter extends VirtualBeanPropertyWriter {
        public DummyVirtualPropWriter() { super(); }
        public DummyVirtualPropWriter(BeanPropertyDefinition propDef, com.fasterxml.jackson.databind.util.Annotations contextAnnotations, JavaType declaredType) {
            super(propDef, contextAnnotations, declaredType);
        }
        @Override
        protected Object value(Object bean, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider prov) {
            return "virt";
        }
        @Override
        public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
            return new DummyVirtualPropWriter(propDef, declaringClass.getAnnotations(), type);
        }
    }

    private static class DummyCustomResolverBuilder extends StdTypeResolverBuilder {}
    private static class DummyCustomIdResolver implements TypeIdResolver {
        @Override public void init(JavaType baseType) {}
        @Override public String idFromValue(Object value) { return null; }
        @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
        @Override public String idFromBaseType() { return null; }
        @Override public JavaType typeFromId(DatabindContext context, String id) { return null; }
        @Override public String getDescForKnownTypeIds() { return null; }
        @Override public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
    }

    @JsonTypeResolver(DummyCustomResolverBuilder.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.WRAPPER_OBJECT)
    @JsonTypeIdResolver(DummyCustomIdResolver.class)
    private static class CustomTypeResolvedClass {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private static class NoTypeResolvedClass {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY)
    private static class ExternalPropOnClass {}

    private static class MemberTargetClass {
        @JsonProperty(value = "fieldProp", required = true, index = 3, defaultValue = "defaultField")
        @JsonPropertyDescription("field description")
        @JsonAlias({"fAlias1", "fAlias2"})
        @JsonManagedReference("refName")
        @JsonUnwrapped(prefix = "pre_", suffix = "_suf", enabled = true)
        @JacksonInject("injName")
        @JsonView({Object.class})
        @JsonTypeId
        @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "fieldProp", scope = MemberTargetClass.class, resolver = SimpleObjectIdResolver.class)
        @JsonIdentityReference(alwaysAsId = true)
        @JsonSerialize(using = JsonSerializer.None.class, keyUsing = JsonSerializer.None.class, contentUsing = JsonSerializer.None.class, nullsUsing = JsonSerializer.None.class, as = CharSequence.class, keyAs = Object.class, contentAs = Object.class, typing = JsonSerialize.Typing.STATIC_TYPING, converter = Converter.None.class, contentConverter = Converter.None.class, include = JsonInclude.Include.NON_EMPTY)
        public String field1;

        @JsonGetter("getterProp")
        @JsonRawValue(true)
        public String getGetterProp() { return field1; }

        @JsonSetter("setterProp")
        @JsonMerge(com.fasterxml.jackson.annotation.OptBoolean.TRUE)
        public void setSetterProp(String s) { this.field1 = s; }

        @JsonBackReference("refName")
        @JsonValue(true)
        @JsonAnyGetter
        public String backRefGetter() { return field1; }

        @JsonAnySetter
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        public MemberTargetClass(String plain) {}

        @JsonCreator(mode = JsonCreator.Mode.DISABLED)
        public MemberTargetClass(int i) {}

        @JsonDeserialize(using = JsonDeserializer.None.class, keyUsing = KeyDeserializer.None.class, contentUsing = JsonDeserializer.None.class, as = String.class, keyAs = String.class, contentAs = String.class, builder = Void.class, converter = Converter.None.class, contentConverter = Converter.None.class)
        public MemberTargetClass() {}

        public void methodA(int a) {}
        public void methodB(Integer a) {}
        public void methodC(String s) {}
        public void methodD(Object o) {}
    }

    private AnnotatedClass getAnnotatedClass(Class<?> cls) {
        return AnnotatedClassResolver.resolve(mapper.getSerializationConfig(), mapper.constructType(cls), mapper.getSerializationConfig());
    }

    private AnnotatedMember getAnnotatedField(Class<?> cls, String fieldName) {
        AnnotatedClass ac = getAnnotatedClass(cls);
        for (AnnotatedField f : ac.fields()) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        return null;
    }

    private AnnotatedMethod getAnnotatedMethod(Class<?> cls, String methodName) {
        AnnotatedClass ac = getAnnotatedClass(cls);
        for (AnnotatedMethod m : ac.memberMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        return null;
    }

    private AnnotatedConstructor getAnnotatedConstructor(Class<?> cls, Class<?>... paramTypes) {
        AnnotatedClass ac = getAnnotatedClass(cls);
        for (AnnotatedConstructor c : ac.getConstructors()) {
            if (c.getParameterCount() == paramTypes.length) {
                boolean match = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (!c.getRawParameterType(i).equals(paramTypes[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) return c;
            }
        }
        return null;
    }

    @Test
    public void testVersionAndSerialization() throws Exception {
        Version v = ai.version();
        Assert.assertNotNull(v);
        Assert.assertFalse(v.isUnknownVersion());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ai);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        JacksonAnnotationIntrospector deserialized = (JacksonAnnotationIntrospector) ois.readObject();
        Assert.assertNotNull(deserialized);
    }

    @Test
    public void testIsAnnotationBundle() {
        BundleAnno bundleAnno = AnnotatedRootClass.class.getAnnotation(BundleAnno.class);
        Assert.assertTrue(ai.isAnnotationBundle(BundleHolder.class.getAnnotation(BundleAnno.class)));
        Assert.assertFalse(ai.isAnnotationBundle(BundleHolder.class.getAnnotation(NonBundleAnno.class)));
        Assert.assertFalse(ai.isAnnotationBundle(BundleHolder.class.getAnnotation(NonBundleAnno.class)));
    }

    @BundleAnno
    @NonBundleAnno
    private static class BundleHolder {}

    @Test
    public void testEnumIntrospection() {
        Assert.assertEquals("a_val", ai.findEnumValue(TestEnum.A));
        Assert.assertEquals("B", ai.findEnumValue(TestEnum.B));
        Assert.assertEquals("PLAIN", ai.findEnumValue(TestEnum.PLAIN));

        String[] names = new String[]{"A", "B", "DEFAULT_VAL", "PLAIN"};
        String[] result = ai.findEnumValues(TestEnum.class, TestEnum.values(), names);
        Assert.assertEquals("a_val", result[0]);

        Assert.assertEquals(TestEnum.DEFAULT_VAL, ai.findDefaultEnumValue(TestEnum.class));
    }

    @Test
    public void testClassAnnotations() {
        AnnotatedClass ac = getAnnotatedClass(AnnotatedRootClass.class);
        PropertyName rootName = ai.findRootName(ac);
        Assert.assertEquals("root", rootName.getSimpleName());
        Assert.assertEquals("http://example.com", rootName.getNamespace());

        AnnotatedClass acEmpty = getAnnotatedClass(EmptyRootClass.class);
        Assert.assertEquals("", ai.findRootName(acEmpty).getSimpleName());
        Assert.assertNull(ai.findFilterId(acEmpty));

        JsonIgnoreProperties.Value ign = ai.findPropertyIgnorals(ac);
        Assert.assertTrue(ign.findIgnoredForSerialization().contains("ignored1"));

        Assert.assertTrue(ai.isIgnorableType(ac));
        Assert.assertEquals("filter123", ai.findFilterId(ac));
        Assert.assertEquals(PropertyNamingStrategy.SnakeCaseStrategy.class, ai.findNamingStrategy(ac));
        Assert.assertEquals("class description test", ai.findClassDescription(ac));

        VisibilityChecker<?> checker = ai.findAutoDetectVisibility(ac, mapper.getSerializationConfig().getDefaultVisibilityChecker());
        Assert.assertNotNull(checker);

        String[] order = ai.findSerializationPropertyOrder(ac);
        Assert.assertArrayEquals(new String[]{"propB", "propA"}, order);
        Assert.assertTrue(ai.findSerializationSortAlphabetically(ac));

        Assert.assertEquals("CustomTypeName", ai.findTypeName(ac));
        List<NamedType> subtypes = ai.findSubtypes(ac);
        Assert.assertEquals(2, subtypes.size());
        Assert.assertEquals("str", subtypes.get(0).getName());
    }

    @Test
    public void testMemberAnnotations() {
        AnnotatedMember f1 = getAnnotatedField(MemberTargetClass.class, "field1");
        Assert.assertNotNull(f1);

        Assert.assertEquals("fieldProp", ai.findNameForSerialization(f1).getSimpleName());
        Assert.assertEquals("fieldProp", ai.findNameForDeserialization(f1).getSimpleName());
        Assert.assertTrue(ai.hasRequiredMarker(f1));
        Assert.assertEquals(Integer.valueOf(3), ai.findPropertyIndex(f1));
        Assert.assertEquals("defaultField", ai.findPropertyDefaultValue(f1));
        Assert.assertEquals("field description", ai.findPropertyDescription(f1));

        List<PropertyName> aliases = ai.findPropertyAliases(f1);
        Assert.assertEquals(2, aliases.size());
        Assert.assertEquals("fAlias1", aliases.get(0).getSimpleName());

        AnnotationIntrospector.ReferenceProperty ref = ai.findReferenceType(f1);
        Assert.assertTrue(ref.isManagedReference());
        Assert.assertEquals("refName", ref.getName());

        Assert.assertNotNull(ai.findUnwrappingNameTransformer(f1));
        Assert.assertNotNull(ai.findInjectableValue(f1));
        Assert.assertEquals("injName", ai.findInjectableValueId(f1));

        Class<?>[] views = ai.findViews(f1);
        Assert.assertEquals(1, views.length);
        Assert.assertEquals(Object.class, views[0]);

        Assert.assertTrue(ai.isTypeId(f1));

        ObjectIdInfo oi = ai.findObjectIdInfo(f1);
        Assert.assertNotNull(oi);
        Assert.assertEquals("fieldProp", oi.getPropertyName().getSimpleName());

        ObjectIdInfo refOi = ai.findObjectReferenceInfo(f1, oi);
        Assert.assertTrue(refOi.getAlwaysAsId());

        Assert.assertEquals(JsonSerialize.Typing.STATIC_TYPING, ai.findSerializationTyping(f1));
    }

    @Test
    public void testMethodAnnotations() {
        AnnotatedMethod getMethod = getAnnotatedMethod(MemberTargetClass.class, "getGetterProp");
        Assert.assertEquals("getterProp", ai.findNameForSerialization(getMethod).getSimpleName());
        Object rawSer = ai.findSerializer(getMethod);
        Assert.assertTrue(rawSer instanceof RawSerializer<?>);

        AnnotatedMethod setMethod = getAnnotatedMethod(MemberTargetClass.class, "setSetterProp");
        Assert.assertEquals("setterProp", ai.findNameForDeserialization(setMethod).getSimpleName());
        Assert.assertTrue(ai.findMergeInfo(setMethod));

        AnnotatedMethod backRefMethod = getAnnotatedMethod(MemberTargetClass.class, "backRefGetter");
        AnnotationIntrospector.ReferenceProperty backRef = ai.findReferenceType(backRefMethod);
        Assert.assertTrue(backRef.isBackReference());
        Assert.assertTrue(ai.hasAsValue(backRefMethod));
        Assert.assertTrue(ai.hasAsValueAnnotation(backRefMethod));
        Assert.assertTrue(ai.hasAnyGetter(backRefMethod));
        Assert.assertTrue(ai.hasAnyGetterAnnotation(backRefMethod));

        AnnotatedMethod mA = getAnnotatedMethod(MemberTargetClass.class, "methodA");
        AnnotatedMethod mB = getAnnotatedMethod(MemberTargetClass.class, "methodB");
        AnnotatedMethod mC = getAnnotatedMethod(MemberTargetClass.class, "methodC");
        AnnotatedMethod mD = getAnnotatedMethod(MemberTargetClass.class, "methodD");

        Assert.assertSame(mA, ai.resolveSetterConflict(mapper.getSerializationConfig(), mA, mB));
        Assert.assertSame(mA, ai.resolveSetterConflict(mapper.getSerializationConfig(), mB, mA));
        Assert.assertSame(mC, ai.resolveSetterConflict(mapper.getSerializationConfig(), mC, mD));
        Assert.assertSame(mC, ai.resolveSetterConflict(mapper.getSerializationConfig(), mD, mC));
        Assert.assertNull(ai.resolveSetterConflict(mapper.getSerializationConfig(), mB, mD));
    }

    @Test
    public void testCreatorAndConstructor() {
        AnnotatedConstructor ctorDel = getAnnotatedConstructor(MemberTargetClass.class, String.class);
        Assert.assertEquals(JsonCreator.Mode.DELEGATING, ai.findCreatorAnnotation(mapper.getDeserializationConfig(), ctorDel));
        Assert.assertEquals(JsonCreator.Mode.DELEGATING, ai.findCreatorBinding(ctorDel));
        Assert.assertTrue(ai.hasCreatorAnnotation(ctorDel));

        AnnotatedConstructor ctorDis = getAnnotatedConstructor(MemberTargetClass.class, int.class);
        Assert.assertEquals(JsonCreator.Mode.DISABLED, ai.findCreatorAnnotation(mapper.getDeserializationConfig(), ctorDis));
        Assert.assertFalse(ai.hasCreatorAnnotation(ctorDis));

        ai.setConstructorPropertiesImpliesCreator(false);
    }

    @Test
    public void testTypeResolvers() {
        AnnotatedClass acRoot = getAnnotatedClass(AnnotatedRootClass.class);
        TypeResolverBuilder<?> trb = ai.findTypeResolver(mapper.getSerializationConfig(), acRoot, mapper.constructType(AnnotatedRootClass.class));
        Assert.assertNotNull(trb);

        AnnotatedClass acExt = getAnnotatedClass(ExternalPropOnClass.class);
        TypeResolverBuilder<?> trbExt = ai.findTypeResolver(mapper.getSerializationConfig(), acExt, mapper.constructType(ExternalPropOnClass.class));
        Assert.assertNotNull(trbExt);

        AnnotatedClass acCust = getAnnotatedClass(CustomTypeResolvedClass.class);
        TypeResolverBuilder<?> trbCust = ai.findTypeResolver(mapper.getSerializationConfig(), acCust, mapper.constructType(CustomTypeResolvedClass.class));
        Assert.assertNotNull(trbCust);

        AnnotatedClass acNone = getAnnotatedClass(NoTypeResolvedClass.class);
        TypeResolverBuilder<?> trbNone = ai.findTypeResolver(mapper.getSerializationConfig(), acNone, mapper.constructType(NoTypeResolvedClass.class));
        Assert.assertNotNull(trbNone);

        AnnotatedMember f1 = getAnnotatedField(MemberTargetClass.class, "field1");
        JavaType strType = mapper.constructType(String.class);
        TypeResolverBuilder<?> ptrb = ai.findPropertyTypeResolver(mapper.getSerializationConfig(), f1, strType);
        Assert.assertNull(ptrb);

        JavaType listType = mapper.constructType(List.class);
        Assert.assertNull(ai.findPropertyTypeResolver(mapper.getSerializationConfig(), f1, listType));

        try {
            ai.findPropertyContentTypeResolver(mapper.getSerializationConfig(), f1, strType);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {}

        JavaType mapType = mapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);
        TypeResolverBuilder<?> crb = ai.findPropertyContentTypeResolver(mapper.getSerializationConfig(), f1, mapType);
        Assert.assertNull(crb);
    }

    @Test
    public void testTypeRefinements() throws Exception {
        AnnotatedMember f1 = getAnnotatedField(MemberTargetClass.class, "field1");

        JavaType stringType = mapper.constructType(String.class);
        JavaType refinedSer = ai.refineSerializationType(mapper.getSerializationConfig(), f1, stringType);
        Assert.assertNotNull(refinedSer);

        JavaType charSeqType = mapper.constructType(CharSequence.class);
        JavaType refinedDeser = ai.refineDeserializationType(mapper.getDeserializationConfig(), f1, charSeqType);
        Assert.assertEquals(String.class, refinedDeser.getRawClass());

        JavaType mapType = mapper.getTypeFactory().constructMapType(Map.class, Object.class, Object.class);
        JavaType refinedMapSer = ai.refineSerializationType(mapper.getSerializationConfig(), f1, mapType);
        Assert.assertNotNull(refinedMapSer);

        JavaType refinedMapDeser = ai.refineDeserializationType(mapper.getDeserializationConfig(), f1, mapType);
        Assert.assertEquals(String.class, refinedMapDeser.getKeyType().getRawClass());
        Assert.assertEquals(String.class, refinedMapDeser.getContentType().getRawClass());
    }

    @Test(expected = JsonMappingException.class)
    public void testRefineSerializationIncompatible() throws Exception {
        AnnotatedMember f1 = getAnnotatedField(MemberTargetClass.class, "field1");
        JavaType intType = mapper.constructType(Integer.class);
        ai.refineSerializationType(mapper.getSerializationConfig(), f1, intType);
    }

    @Test(expected = JsonMappingException.class)
    public void testRefineDeserializationIncompatible() throws Exception {
        AnnotatedMember f1 = getAnnotatedField(MemberTargetClass.class, "field1");
        JavaType intType = mapper.constructType(Integer.class);
        ai.refineDeserializationType(mapper.getDeserializationConfig(), f1, intType);
    }

    @Test
    public void testVirtualProperties() {
        AnnotatedClass ac = getAnnotatedClass(AppendTargetClass.class);
        List<BeanPropertyWriter> list = new ArrayList<>();
        ai.findAndAddVirtualProperties(mapper.getSerializationConfig(), ac, list);
        Assert.assertEquals(3, list.size());
    }

    @Test
    public void testFindNullAndDefaults() {
        AnnotatedClass ac = getAnnotatedClass(Object.class);
        Assert.assertNull(ai.findRootName(ac));
        Assert.assertNull(ai.findPropertyDescription(ac));
        Assert.assertNull(ai.findPropertyIndex(ac));
        Assert.assertNull(ai.findPropertyDefaultValue(ac));
        Assert.assertNull(ai.findFormat(ac));
        Assert.assertNull(ai.findReferenceType(getAnnotatedField(MemberTargetClass.class, "field1") != null ? (AnnotatedMember) ac : null));
        Assert.assertNull(ai.findViews(ac));
        Assert.assertNull(ai.findTypeName(ac));
        Assert.assertNull(ai.findSubtypes(ac));
        Assert.assertNull(ai.findObjectIdInfo(ac));
        Assert.assertNull(ai.findObjectReferenceInfo(ac, null));
        Assert.assertNull(ai.findSerializer(ac));
        Assert.assertNull(ai.findKeySerializer(ac));
        Assert.assertNull(ai.findContentSerializer(ac));
        Assert.assertNull(ai.findNullSerializer(ac));
        Assert.assertNull(ai.findSerializationTyping(ac));
        Assert.assertNull(ai.findSerializationConverter(ac));
        Assert.assertNull(ai.findValueInstantiator(ac));
        Assert.assertNull(ai.findPOJOBuilder(ac));
        Assert.assertNull(ai.findPOJOBuilderConfig(ac));
        Assert.assertNull(ai.findDeserializer(ac));
        Assert.assertNull(ai.findKeyDeserializer(ac));
        Assert.assertNull(ai.findContentDeserializer(ac));
        Assert.assertNull(ai.findDeserializationConverter(ac));
        Assert.assertNull(ai.hasAnySetter(ac));
        Assert.assertNull(ai.findMergeInfo(ac));
        Assert.assertNull(ai.findPropertyAccess(ac));
        Assert.assertNull(ai.findImplicitPropertyName(ac));
        Assert.assertNull(ai.findPropertyAliases(ac));
        Assert.assertNull(ai.findSerializationType(ac));
        Assert.assertNull(ai.findSerializationKeyType(ac, null));
        Assert.assertNull(ai.findSerializationContentType(ac, null));
        Assert.assertNull(ai.findDeserializationType(ac, null));
        Assert.assertNull(ai.findDeserializationKeyType(ac, null));
        Assert.assertNull(ai.findDeserializationContentType(ac, null));
    }
}
