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
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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

    @JacksonAnnotationsInside
    @Retention(RetentionPolicy.RUNTIME)
    private @interface BundleAnnotation {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface NonBundleAnnotation {}

    private enum TestEnum {
        @JsonProperty("customA")
        A,
        @JsonProperty("")
        B,
        C
    }

    @JsonRootName(value = "root", namespace = "http://example.com")
    private static class RootWithNs {}

    @JsonRootName(value = "rootEmptyNs", namespace = "")
    private static class RootWithEmptyNs {}

    private static class NoRoot {}

    @JsonIgnoreProperties(value = {"p1", "p2"}, allowGetters = true, allowSetters = false, ignoreUnknown = true)
    private static class IgnorePropClass1 {}

    @JsonIgnoreProperties(value = {"p3"}, allowGetters = false, allowSetters = true, ignoreUnknown = false)
    private static class IgnorePropClass2 {}

    @JsonIgnoreType(true)
    private static class IgnorableTypeClass {}

    @JsonIgnoreType(false)
    private static class NonIgnorableTypeClass {}

    @JsonFilter("filter123")
    private static class FilterClass {}

    @JsonFilter("")
    private static class EmptyFilterClass {}

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    private static class NamingClass {}

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    private static class AutoDetectClass {}

    @JsonPropertyOrder(value = {"b", "a"}, alphabetic = true)
    private static class PropOrderClass {}

    @JsonPropertyOrder(alphabetic = false)
    private static class PropOrderNonAlphaClass {}

    @JsonTypeName("myType")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SubTypeA.class, name = "subA"),
            @JsonSubTypes.Type(value = SubTypeB.class, name = "subB")
    })
    private static class BaseTypeClass {}

    private static class SubTypeA extends BaseTypeClass {}
    private static class SubTypeB extends BaseTypeClass {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type", defaultImpl = SubTypeA.class, visible = true)
    private static class TypeInfoClass {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
    private static class TypeInfoNoneClass {}

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "ext")
    private static class TypeInfoExternalClass {}

    public static class CustomTypeResolverBuilder extends StdTypeResolverBuilder {}

    public static class CustomTypeIdResolver implements TypeIdResolver {
        @Override
        public void init(JavaType baseType) {}
        @Override
        public String idFromValue(Object value) { return null; }
        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) { return null; }
        @Override
        public String idFromBaseType() { return null; }
        @Override
        public JavaType typeFromId(DatabindContext context, String id) { return null; }
        @Override
        public String getDescForKnownTypeIds() { return null; }
        @Override
        public JsonTypeInfo.Id getMechanism() { return JsonTypeInfo.Id.CUSTOM; }
    }

    @JsonTypeResolver(CustomTypeResolverBuilder.class)
    @JsonTypeIdResolver(CustomTypeIdResolver.class)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, defaultImpl = JsonTypeInfo.class)
    private static class CustomTypeResolverClass {}

    @JsonTypeResolver(CustomTypeResolverBuilder.class)
    private static class CustomTypeResolverNoInfoClass {}

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = EntityWithId.class, resolver = SimpleObjectIdResolver.class)
    @JsonIdentityReference(alwaysAsId = true)
    private static class EntityWithId {
        public int id;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.None.class)
    private static class EntityNoneId {}

    private static abstract class DummyConverter implements Converter<Object, Object> {}

    @JsonSerialize(
            using = ToStringSerializer.class,
            keyUsing = ToStringSerializer.class,
            contentUsing = ToStringSerializer.class,
            nullsUsing = NullSerializer.class,
            as = String.class,
            keyAs = Integer.class,
            contentAs = Long.class,
            typing = JsonSerialize.Typing.STATIC,
            converter = DummyConverter.class,
            contentConverter = DummyConverter.class
    )
    private static class SerializeFullClass {}

    @JsonSerialize(include = JsonSerialize.Inclusion.ALWAYS)
    private static class SerializeInclusionAlways {}

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private static class SerializeInclusionNonNull {}

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_DEFAULT)
    private static class SerializeInclusionNonDefault {}

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
    private static class SerializeInclusionNonEmpty {}

    @JsonSerialize(include = JsonSerialize.Inclusion.DEFAULT_INCLUSION)
    private static class SerializeInclusionDefault {}

    @JsonInclude(value = JsonInclude.Include.NON_EMPTY, content = JsonInclude.Include.NON_NULL)
    private static class IncludeValueClass {}

    @JsonDeserialize(
            using = JsonDeserializer.None.class,
            keyUsing = KeyDeserializer.None.class,
            contentUsing = JsonDeserializer.None.class,
            builder = Void.class
    )
    private static class DeserNoneClass {}

    public static class CustomDeser extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) {
            return null;
        }
    }

    public static class CustomKeyDeser extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return null;
        }
    }

    @JsonDeserialize(
            using = CustomDeser.class,
            keyUsing = CustomKeyDeser.class,
            contentUsing = CustomDeser.class,
            as = String.class,
            keyAs = Integer.class,
            contentAs = Long.class,
            converter = DummyConverter.class,
            contentConverter = DummyConverter.class,
            builder = StringBuilder.class
    )
    private static class DeserFullClass {}

    @JsonValueInstantiator(ValueInstantiator.class)
    @JsonPOJOBuilder(buildMethodName = "create", withPrefix = "set")
    private static class BuilderInstantiatorClass {}

    public static class DummyVirtualProp extends VirtualBeanPropertyWriter {
        public DummyVirtualProp() { super(); }
        @Override
        public VirtualBeanPropertyWriter withConfig(MapperConfig<?> config, AnnotatedClass declaringClass, BeanPropertyDefinition propDef, JavaType type) {
            return this;
        }
        @Override
        public Object value(Object bean, com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider prov) {
            return null;
        }
    }

    @JsonAppend(
            prepend = true,
            attrs = {
                    @JsonAppend.Attr(value = "attr1", propName = "propAttr1", propNamespace = "ns1", required = true, include = JsonInclude.Include.NON_NULL),
                    @JsonAppend.Attr(value = "attr2", propName = "", required = false)
            },
            props = {
                    @JsonAppend.Prop(value = DummyVirtualProp.class, name = "prop1", namespace = "ns2", type = String.class, required = true, include = JsonInclude.Include.NON_EMPTY),
                    @JsonAppend.Prop(value = DummyVirtualProp.class, name = "", type = int.class)
            }
    )
    private static class AppendPrependClass {}

    @JsonAppend(
            prepend = false,
            attrs = {
                    @JsonAppend.Attr(value = "attrAppend")
            },
            props = {
                    @JsonAppend.Prop(value = DummyVirtualProp.class, name = "propAppend")
            }
    )
    private static class AppendAppendClass {}

    private static class MemberTestBean {
        @JsonProperty(value = "customProp", required = true, index = 2, defaultValue = "defVal", access = JsonProperty.Access.READ_WRITE)
        @JsonPropertyDescription("A sample property description")
        public String prop;

        @JsonProperty(value = "", index = JsonProperty.INDEX_UNKNOWN, defaultValue = "")
        public String propEmpty;

        @JsonIgnore(true)
        public String ignored;

        @JsonIgnore(false)
        public String notIgnored;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        public String formatted;

        @JsonManagedReference("refName")
        public MemberTestBean managedRef;

        @JsonBackReference("refName")
        public MemberTestBean backRef;

        @JsonUnwrapped(prefix = "pre_", suffix = "_post", enabled = true)
        public MemberTestBean unwrapped;

        @JsonUnwrapped(enabled = false)
        public MemberTestBean unwrappedDisabled;

        @JacksonInject("injId")
        public String injected;

        @JacksonInject("")
        public String injectedDefault;

        @JsonView({String.class, Integer.class})
        public String viewed;

        @JsonTypeId
        public String typeIdField;

        @JsonRawValue(true)
        public String rawVal;

        @JsonRawValue(false)
        public String nonRawVal;

        @JsonGetter("getterMethod")
        public String getCustomGetter() { return prop; }

        @JsonSetter("setterMethod")
        public void setCustomSetter(String s) { prop = s; }

        @JsonValue(true)
        public String asValueMethod() { return prop; }

        @JsonValue(false)
        public String asValueDisabled() { return prop; }

        @JsonAnySetter
        public void anySetter(String k, Object v) {}

        @JsonAnyGetter
        public java.util.Map<String, Object> anyGetter() { return null; }

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public MemberTestBean(@JsonProperty("prop") String p) { this.prop = p; }

        public MemberTestBean() {}

        @JacksonInject("")
        public void setInjectMethod(String val) {}

        @JacksonInject("")
        public String getInjectMethodNoArgs() { return ""; }
    }

    private AnnotatedClass getAnnotatedClass(Class<?> cls) {
        return AnnotatedClass.constructWithoutSuperTypes(cls, mapper.getSerializationConfig());
    }

    @Test
    public void testVersion() {
        Version v = ai.version();
        Assert.assertFalse(v.isUnknownVersion());
    }

    @Test
    public void testIsAnnotationBundle() throws Exception {
        BundleAnnotation bundle = MemberTestBean.class.getMethod("getCustomGetter").getAnnotation(BundleAnnotation.class);
        Annotation[] ann = BundleHolder.class.getAnnotations();
        Assert.assertTrue(ai.isAnnotationBundle(ann[0]));
        Assert.assertFalse(ai.isAnnotationBundle(ann[1]));
    }

    @BundleAnnotation
    @NonBundleAnnotation
    private static class BundleHolder {}

    @Test
    public void testFindEnumValue() {
        Assert.assertEquals("customA", ai.findEnumValue(TestEnum.A));
        Assert.assertEquals("B", ai.findEnumValue(TestEnum.B));
        Assert.assertEquals("C", ai.findEnumValue(TestEnum.C));
    }

    @Test
    public void testFindRootName() {
        PropertyName rn1 = ai.findRootName(getAnnotatedClass(RootWithNs.class));
        Assert.assertEquals("root", rn1.getSimpleName());
        Assert.assertEquals("http://example.com", rn1.getNamespace());

        PropertyName rn2 = ai.findRootName(getAnnotatedClass(RootWithEmptyNs.class));
        Assert.assertEquals("rootEmptyNs", rn2.getSimpleName());
        Assert.assertNull(rn2.getNamespace());

        Assert.assertNull(ai.findRootName(getAnnotatedClass(NoRoot.class)));
    }

    @Test
    public void testFindPropertiesToIgnore() {
        AnnotatedClass ac1 = getAnnotatedClass(IgnorePropClass1.class);
        AnnotatedClass ac2 = getAnnotatedClass(IgnorePropClass2.class);
        AnnotatedClass acNone = getAnnotatedClass(NoRoot.class);

        Assert.assertArrayEquals(new String[]{"p1", "p2"}, ai.findPropertiesToIgnore(ac1));
        Assert.assertNull(ai.findPropertiesToIgnore(acNone));

        Assert.assertNull(ai.findPropertiesToIgnore(ac1, true));
        Assert.assertArrayEquals(new String[]{"p1", "p2"}, ai.findPropertiesToIgnore(ac1, false));

        Assert.assertArrayEquals(new String[]{"p3"}, ai.findPropertiesToIgnore(ac2, true));
        Assert.assertNull(ai.findPropertiesToIgnore(ac2, false));

        Assert.assertNull(ai.findPropertiesToIgnore(acNone, true));
    }

    @Test
    public void testFindIgnoreUnknownProperties() {
        Assert.assertEquals(Boolean.TRUE, ai.findIgnoreUnknownProperties(getAnnotatedClass(IgnorePropClass1.class)));
        Assert.assertEquals(Boolean.FALSE, ai.findIgnoreUnknownProperties(getAnnotatedClass(IgnorePropClass2.class)));
        Assert.assertNull(ai.findIgnoreUnknownProperties(getAnnotatedClass(NoRoot.class)));
    }

    @Test
    public void testIsIgnorableType() {
        Assert.assertEquals(Boolean.TRUE, ai.isIgnorableType(getAnnotatedClass(IgnorableTypeClass.class)));
        Assert.assertEquals(Boolean.FALSE, ai.isIgnorableType(getAnnotatedClass(NonIgnorableTypeClass.class)));
        Assert.assertNull(ai.isIgnorableType(getAnnotatedClass(NoRoot.class)));
    }

    @Test
    public void testFindFilterId() {
        AnnotatedClass ac = getAnnotatedClass(FilterClass.class);
        Assert.assertEquals("filter123", ai.findFilterId(ac));
        Assert.assertEquals("filter123", ai.findFilterId((Annotated) ac));
        Assert.assertNull(ai.findFilterId(getAnnotatedClass(EmptyFilterClass.class)));
        Assert.assertNull(ai.findFilterId(getAnnotatedClass(NoRoot.class)));
    }

    @Test
    public void testFindNamingStrategy() {
        Assert.assertEquals(PropertyNamingStrategy.SnakeCaseStrategy.class, ai.findNamingStrategy(getAnnotatedClass(NamingClass.class)));
        Assert.assertNull(ai.findNamingStrategy(getAnnotatedClass(NoRoot.class)));
    }

    @Test
    public void testFindAutoDetectVisibility() {
        VisibilityChecker<?> checker = mapper.getSerializationConfig().getDefaultVisibilityChecker();
        VisibilityChecker<?> res = ai.findAutoDetectVisibility(getAnnotatedClass(AutoDetectClass.class), checker);
        Assert.assertNotEquals(checker, res);
        Assert.assertSame(checker, ai.findAutoDetectVisibility(getAnnotatedClass(NoRoot.class), checker));
    }

    @Test
    public void testImplicitPropertyName() {
        Assert.assertNull(ai.findImplicitPropertyName(null));
    }

    @Test
    public void testMemberGeneralAnnotations() throws Exception {
        AnnotatedClass ac = getAnnotatedClass(MemberTestBean.class);
        AnnotatedField fieldProp = null;
        AnnotatedField fieldEmpty = null;
        AnnotatedField fieldIgnored = null;
        AnnotatedField fieldNotIgnored = null;
        AnnotatedField fieldFormat = null;
        AnnotatedField fieldManaged = null;
        AnnotatedField fieldBack = null;
        AnnotatedField fieldUnwrapped = null;
        AnnotatedField fieldUnwrappedDisabled = null;
        AnnotatedField fieldInjected = null;
        AnnotatedField fieldInjectedDef = null;
        AnnotatedField fieldViewed = null;
        AnnotatedField fieldTypeId = null;
        AnnotatedField fieldRaw = null;
        AnnotatedField fieldNonRaw = null;

        for (AnnotatedField f : ac.fields()) {
            if ("prop".equals(f.getName())) fieldProp = f;
            if ("propEmpty".equals(f.getName())) fieldEmpty = f;
            if ("ignored".equals(f.getName())) fieldIgnored = f;
            if ("notIgnored".equals(f.getName())) fieldNotIgnored = f;
            if ("formatted".equals(f.getName())) fieldFormat = f;
            if ("managedRef".equals(f.getName())) fieldManaged = f;
            if ("backRef".equals(f.getName())) fieldBack = f;
            if ("unwrapped".equals(f.getName())) fieldUnwrapped = f;
            if ("unwrappedDisabled".equals(f.getName())) fieldUnwrappedDisabled = f;
            if ("injected".equals(f.getName())) fieldInjected = f;
            if ("injectedDefault".equals(f.getName())) fieldInjectedDef = f;
            if ("viewed".equals(f.getName())) fieldViewed = f;
            if ("typeIdField".equals(f.getName())) fieldTypeId = f;
            if ("rawVal".equals(f.getName())) fieldRaw = f;
            if ("nonRawVal".equals(f.getName())) fieldNonRaw = f;
        }

        Assert.assertTrue(ai.hasIgnoreMarker(fieldIgnored));
        Assert.assertFalse(ai.hasIgnoreMarker(fieldNotIgnored));
        Assert.assertFalse(ai.hasIgnoreMarker(fieldProp));

        Assert.assertEquals(Boolean.TRUE, ai.hasRequiredMarker(fieldProp));
        Assert.assertEquals(Boolean.FALSE, ai.hasRequiredMarker(fieldEmpty));
        Assert.assertNull(ai.hasRequiredMarker(fieldIgnored));

        Assert.assertEquals(JsonProperty.Access.READ_WRITE, ai.findPropertyAccess(fieldProp));
        Assert.assertEquals(JsonProperty.Access.AUTO, ai.findPropertyAccess(fieldEmpty));
        Assert.assertNull(ai.findPropertyAccess(fieldIgnored));

        Assert.assertEquals("A sample property description", ai.findPropertyDescription(fieldProp));
        Assert.assertNull(ai.findPropertyDescription(fieldEmpty));

        Assert.assertEquals(Integer.valueOf(2), ai.findPropertyIndex(fieldProp));
        Assert.assertNull(ai.findPropertyIndex(fieldEmpty));
        Assert.assertNull(ai.findPropertyIndex(fieldIgnored));

        Assert.assertEquals("defVal", ai.findPropertyDefaultValue(fieldProp));
        Assert.assertNull(ai.findPropertyDefaultValue(fieldEmpty));
        Assert.assertNull(ai.findPropertyDefaultValue(fieldIgnored));

        JsonFormat.Value fmt = ai.findFormat(fieldFormat);
        Assert.assertNotNull(fmt);
        Assert.assertEquals("yyyy-MM-dd", fmt.getPattern());
        Assert.assertNull(ai.findFormat(fieldProp));

        AnnotationIntrospector.ReferenceProperty refManaged = ai.findReferenceType(fieldManaged);
        Assert.assertTrue(refManaged.isManagedReference());
        Assert.assertEquals("refName", refManaged.getName());

        AnnotationIntrospector.ReferenceProperty refBack = ai.findReferenceType(fieldBack);
        Assert.assertTrue(refBack.isBackReference());
        Assert.assertEquals("refName", refBack.getName());

        Assert.assertNull(ai.findReferenceType(fieldProp));

        NameTransformer nt = ai.findUnwrappingNameTransformer(fieldUnwrapped);
        Assert.assertNotNull(nt);
        Assert.assertEquals("pre_val_post", nt.transform("val"));
        Assert.assertNull(ai.findUnwrappingNameTransformer(fieldUnwrappedDisabled));
        Assert.assertNull(ai.findUnwrappingNameTransformer(fieldProp));

        Assert.assertEquals("injId", ai.findInjectableValueId(fieldInjected));
        Assert.assertEquals(String.class.getName(), ai.findInjectableValueId(fieldInjectedDef));
        Assert.assertNull(ai.findInjectableValueId(fieldProp));

        Class<?>[] views = ai.findViews(fieldViewed);
        Assert.assertArrayEquals(new Class<?>[]{String.class, Integer.class}, views);
        Assert.assertNull(ai.findViews(fieldProp));

        Assert.assertTrue(ai.isTypeId(fieldTypeId));
        Assert.assertFalse(ai.isTypeId(fieldProp));

        Object rawSer = ai.findSerializer(fieldRaw);
        Assert.assertTrue(rawSer instanceof RawSerializer);
        Assert.assertNull(ai.findSerializer(fieldNonRaw));
    }

    @Test
    public void testInjectableMethod() {
        AnnotatedClass ac = getAnnotatedClass(MemberTestBean.class);
        AnnotatedMethod setter = null;
        AnnotatedMethod getter = null;
        for (AnnotatedMethod m : ac.memberMethods()) {
            if ("setInjectMethod".equals(m.getName())) setter = m;
            if ("getInjectMethodNoArgs".equals(m.getName())) getter = m;
        }
        Assert.assertEquals(String.class.getName(), ai.findInjectableValueId(setter));
        Assert.assertEquals(String.class.getName(), ai.findInjectableValueId(getter));
    }

    @Test
    public void testPolymorphicTypeHandling() {
        AnnotatedClass ac = getAnnotatedClass(BaseTypeClass.class);
        List<NamedType> subtypes = ai.findSubtypes(ac);
        Assert.assertEquals(2, subtypes.size());
        Assert.assertEquals("subA", subtypes.get(0).getName());
        Assert.assertEquals(SubTypeA.class, subtypes.get(0).getType());
        Assert.assertEquals("subB", subtypes.get(1).getName());
        Assert.assertEquals(SubTypeB.class, subtypes.get(1).getType());

        Assert.assertNull(ai.findSubtypes(getAnnotatedClass(NoRoot.class)));
        Assert.assertEquals("myType", ai.findTypeName(ac));
        Assert.assertNull(ai.findTypeName(getAnnotatedClass(NoRoot.class)));

        JavaType baseType = TypeFactory.defaultInstance().constructType(BaseTypeClass.class);
        TypeResolverBuilder<?> b = ai.findTypeResolver(mapper.getSerializationConfig(), getAnnotatedClass(TypeInfoClass.class), baseType);
        Assert.assertNotNull(b);

        TypeResolverBuilder<?> bNone = ai.findTypeResolver(mapper.getSerializationConfig(), getAnnotatedClass(TypeInfoNoneClass.class), baseType);
        Assert.assertNotNull(bNone);

        TypeResolverBuilder<?> bExt = ai.findTypeResolver(mapper.getSerializationConfig(), getAnnotatedClass(TypeInfoExternalClass.class), baseType);
        Assert.assertNotNull(bExt);

        TypeResolverBuilder<?> bCustom = ai.findTypeResolver(mapper.getSerializationConfig(), getAnnotatedClass(CustomTypeResolverClass.class), baseType);
        Assert.assertNotNull(bCustom);

        Assert.assertNull(ai.findTypeResolver(mapper.getSerializationConfig(), getAnnotatedClass(CustomTypeResolverNoInfoClass.class), baseType));

        AnnotatedClass acBean = getAnnotatedClass(MemberTestBean.class);
        AnnotatedField fieldProp = acBean.fields().iterator().next();
        Assert.assertNull(ai.findPropertyTypeResolver(mapper.getSerializationConfig(), fieldProp, TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)));
        Assert.assertNull(ai.findPropertyTypeResolver(mapper.getSerializationConfig(), fieldProp, baseType));

        try {
            ai.findPropertyContentTypeResolver(mapper.getSerializationConfig(), fieldProp, baseType);
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        JavaType containerType = TypeFactory.defaultInstance().constructCollectionType(List.class, BaseTypeClass.class);
        Assert.assertNull(ai.findPropertyContentTypeResolver(mapper.getSerializationConfig(), fieldProp, containerType));
    }

    @Test
    public void testObjectIdHandling() {
        AnnotatedClass ac = getAnnotatedClass(EntityWithId.class);
        ObjectIdInfo info = ai.findObjectIdInfo(ac);
        Assert.assertNotNull(info);
        Assert.assertEquals(PropertyName.construct("id"), info.getPropertyName());
        Assert.assertEquals(EntityWithId.class, info.getScope());
        Assert.assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());
        Assert.assertEquals(SimpleObjectIdResolver.class, info.getResolverType());

        ObjectIdInfo refInfo = ai.findObjectReferenceInfo(ac, info);
        Assert.assertTrue(refInfo.getAlwaysAsId());

        Assert.assertNull(ai.findObjectIdInfo(getAnnotatedClass(EntityNoneId.class)));
        Assert.assertNull(ai.findObjectIdInfo(getAnnotatedClass(NoRoot.class)));
    }

    @Test
    public void testSerializationAnnotations() {
        AnnotatedClass acFull = getAnnotatedClass(SerializeFullClass.class);
        Assert.assertEquals(ToStringSerializer.class, ai.findSerializer(acFull));
        Assert.assertEquals(ToStringSerializer.class, ai.findKeySerializer(acFull));
        Assert.assertEquals(ToStringSerializer.class, ai.findContentSerializer(acFull));
        Assert.assertEquals(NullSerializer.class, ai.findNullSerializer(acFull));
        Assert.assertEquals(String.class, ai.findSerializationType(acFull));
        Assert.assertEquals(Integer.class, ai.findSerializationKeyType(acFull, null));
        Assert.assertEquals(Long.class, ai.findSerializationContentType(acFull, null));
        Assert.assertEquals(JsonSerialize.Typing.STATIC, ai.findSerializationTyping(acFull));
        Assert.assertEquals(DummyConverter.class, ai.findSerializationConverter(acFull));
        Assert.assertNull(ai.findSerializationContentConverter(acFull.fields().iterator().hasNext() ? acFull.fields().iterator().next() : null));

        AnnotatedClass acNone = getAnnotatedClass(NoRoot.class);
        Assert.assertNull(ai.findSerializer(acNone));
        Assert.assertNull(ai.findKeySerializer(acNone));
        Assert.assertNull(ai.findContentSerializer(acNone));
        Assert.assertNull(ai.findNullSerializer(acNone));
        Assert.assertNull(ai.findSerializationType(acNone));
        Assert.assertNull(ai.findSerializationKeyType(acNone, null));
        Assert.assertNull(ai.findSerializationContentType(acNone, null));
        Assert.assertNull(ai.findSerializationTyping(acNone));
        Assert.assertNull(ai.findSerializationConverter(acNone));
    }

    @Test
    public void testInclusionHandling() {
        Assert.assertEquals(JsonInclude.Include.ALWAYS, ai.findSerializationInclusion(getAnnotatedClass(SerializeInclusionAlways.class), JsonInclude.Include.NON_EMPTY));
        Assert.assertEquals(JsonInclude.Include.NON_NULL, ai.findSerializationInclusion(getAnnotatedClass(SerializeInclusionNonNull.class), JsonInclude.Include.ALWAYS));
        Assert.assertEquals(JsonInclude.Include.NON_DEFAULT, ai.findSerializationInclusion(getAnnotatedClass(SerializeInclusionNonDefault.class), JsonInclude.Include.ALWAYS));
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, ai.findSerializationInclusion(getAnnotatedClass(SerializeInclusionNonEmpty.class), JsonInclude.Include.ALWAYS));
        Assert.assertEquals(JsonInclude.Include.ALWAYS, ai.findSerializationInclusion(getAnnotatedClass(SerializeInclusionDefault.class), JsonInclude.Include.ALWAYS));

        AnnotatedClass incValClass = getAnnotatedClass(IncludeValueClass.class);
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, ai.findSerializationInclusion(incValClass, JsonInclude.Include.ALWAYS));
        Assert.assertEquals(JsonInclude.Include.NON_NULL, ai.findSerializationInclusionForContent(incValClass, JsonInclude.Include.ALWAYS));

        JsonInclude.Value inclValue = ai.findPropertyInclusion(incValClass);
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, inclValue.getValueInclusion());
        Assert.assertEquals(JsonInclude.Include.NON_NULL, inclValue.getContentInclusion());

        JsonInclude.Value inclSerNonNull = ai.findPropertyInclusion(getAnnotatedClass(SerializeInclusionNonNull.class));
        Assert.assertEquals(JsonInclude.Include.NON_NULL, inclSerNonNull.getValueInclusion());
    }

    @Test
    public void testOrderAndAlphabetic() {
        AnnotatedClass acOrder = getAnnotatedClass(PropOrderClass.class);
        Assert.assertArrayEquals(new String[]{"b", "a"}, ai.findSerializationPropertyOrder(acOrder));
        Assert.assertEquals(Boolean.TRUE, ai.findSerializationSortAlphabetically(acOrder));

        AnnotatedClass acNonAlpha = getAnnotatedClass(PropOrderNonAlphaClass.class);
        Assert.assertNull(ai.findSerializationSortAlphabetically(acNonAlpha));
        Assert.assertNull(ai.findSerializationPropertyOrder(getAnnotatedClass(NoRoot.class)));
        Assert.assertNull(ai.findSerializationSortAlphabetically(getAnnotatedClass(NoRoot.class)));
    }

    @Test
    public void testFindAndAddVirtualProperties() {
        AnnotatedClass acPrepend = getAnnotatedClass(AppendPrependClass.class);
        List<BeanPropertyWriter> listPrepend = new ArrayList<BeanPropertyWriter>();
        ai.findAndAddVirtualProperties(mapper.getSerializationConfig(), acPrepend, listPrepend);
        Assert.assertEquals(4, listPrepend.size());

        AnnotatedClass acAppend = getAnnotatedClass(AppendAppendClass.class);
        List<BeanPropertyWriter> listAppend = new ArrayList<BeanPropertyWriter>();
        ai.findAndAddVirtualProperties(mapper.getSerializationConfig(), acAppend, listAppend);
        Assert.assertEquals(2, listAppend.size());

        List<BeanPropertyWriter> listNone = new ArrayList<BeanPropertyWriter>();
        ai.findAndAddVirtualProperties(mapper.getSerializationConfig(), getAnnotatedClass(NoRoot.class), listNone);
        Assert.assertEquals(0, listNone.size());
    }

    @Test
    public void testSerializationMemberAnnotations() {
        AnnotatedClass ac = getAnnotatedClass(MemberTestBean.class);
        AnnotatedMethod getter = null;
        AnnotatedMethod asVal = null;
        AnnotatedMethod asValDis = null;
        for (AnnotatedMethod m : ac.memberMethods()) {
            if ("getCustomGetter".equals(m.getName())) getter = m;
            if ("asValueMethod".equals(m.getName())) asVal = m;
            if ("asValueDisabled".equals(m.getName())) asValDis = m;
        }

        PropertyName name = ai.findNameForSerialization(getter);
        Assert.assertNotNull(name);
        Assert.assertEquals("getterMethod", name.getSimpleName());

        AnnotatedField fieldProp = null;
        AnnotatedField fieldViewed = null;
        AnnotatedField fieldIgnored = null;
        for (AnnotatedField f : ac.fields()) {
            if ("prop".equals(f.getName())) fieldProp = f;
            if ("viewed".equals(f.getName())) fieldViewed = f;
            if ("ignored".equals(f.getName())) fieldIgnored = f;
        }

        Assert.assertEquals("customProp", ai.findNameForSerialization(fieldProp).getSimpleName());
        Assert.assertEquals("", ai.findNameForSerialization(fieldViewed).getSimpleName());
        Assert.assertNull(ai.findNameForSerialization(fieldIgnored));

        Assert.assertTrue(ai.hasAsValueAnnotation(asVal));
        Assert.assertFalse(ai.hasAsValueAnnotation(asValDis));
    }

    @Test
    public void testDeserializationAnnotations() {
        AnnotatedClass acNone = getAnnotatedClass(DeserNoneClass.class);
        Assert.assertNull(ai.findDeserializer(acNone));
        Assert.assertNull(ai.findKeyDeserializer(acNone));
        Assert.assertNull(ai.findContentDeserializer(acNone));
        Assert.assertNull(ai.findPOJOBuilder(acNone));

        AnnotatedClass acFull = getAnnotatedClass(DeserFullClass.class);
        Assert.assertEquals(CustomDeser.class, ai.findDeserializer(acFull));
        Assert.assertEquals(CustomKeyDeser.class, ai.findKeyDeserializer(acFull));
        Assert.assertEquals(CustomDeser.class, ai.findContentDeserializer(acFull));
        Assert.assertEquals(String.class, ai.findDeserializationType(acFull, null));
        Assert.assertEquals(Integer.class, ai.findDeserializationKeyType(acFull, null));
        Assert.assertEquals(Long.class, ai.findDeserializationContentType(acFull, null));
        Assert.assertEquals(DummyConverter.class, ai.findDeserializationConverter(acFull));
        Assert.assertEquals(StringBuilder.class, ai.findPOJOBuilder(acFull));

        AnnotatedClass acInst = getAnnotatedClass(BuilderInstantiatorClass.class);
        Assert.assertEquals(ValueInstantiator.class, ai.findValueInstantiator(acInst));
        JsonPOJOBuilder.Value bConfig = ai.findPOJOBuilderConfig(acInst);
        Assert.assertNotNull(bConfig);
        Assert.assertEquals("create", bConfig.buildMethodName);
        Assert.assertEquals("set", bConfig.withPrefix);

        Assert.assertNull(ai.findValueInstantiator(acNone));
        Assert.assertNull(ai.findPOJOBuilderConfig(acNone));
    }

    @Test
    public void testDeserializationMemberAnnotations() {
        AnnotatedClass ac = getAnnotatedClass(MemberTestBean.class);
        AnnotatedMethod setter = null;
        AnnotatedMethod anySetter = null;
        AnnotatedMethod anyGetter = null;
        AnnotatedConstructor creator = null;

        for (AnnotatedMethod m : ac.memberMethods()) {
            if ("setCustomSetter".equals(m.getName())) setter = m;
            if ("anySetter".equals(m.getName())) anySetter = m;
            if ("anyGetter".equals(m.getName())) anyGetter = m;
        }
        for (AnnotatedConstructor c : ac.getConstructors()) {
            if (c.getParameterCount() == 1) creator = c;
        }

        PropertyName nameSetter = ai.findNameForDeserialization(setter);
        Assert.assertNotNull(nameSetter);
        Assert.assertEquals("setterMethod", nameSetter.getSimpleName());

        AnnotatedField fieldProp = null;
        AnnotatedField fieldManaged = null;
        AnnotatedField fieldIgnored = null;
        for (AnnotatedField f : ac.fields()) {
            if ("prop".equals(f.getName())) fieldProp = f;
            if ("managedRef".equals(f.getName())) fieldManaged = f;
            if ("ignored".equals(f.getName())) fieldIgnored = f;
        }

        Assert.assertEquals("customProp", ai.findNameForDeserialization(fieldProp).getSimpleName());
        Assert.assertEquals("", ai.findNameForDeserialization(fieldManaged).getSimpleName());
        Assert.assertNull(ai.findNameForDeserialization(fieldIgnored));

        Assert.assertTrue(ai.hasAnySetterAnnotation(anySetter));
        Assert.assertFalse(ai.hasAnySetterAnnotation(setter));

        Assert.assertTrue(ai.hasAnyGetterAnnotation(anyGetter));
        Assert.assertFalse(ai.hasAnyGetterAnnotation(setter));

        Assert.assertTrue(ai.hasCreatorAnnotation(creator));
        Assert.assertEquals(JsonCreator.Mode.PROPERTIES, ai.findCreatorBinding(creator));

        AnnotatedConstructor defCtor = null;
        for (AnnotatedConstructor c : ac.getConstructors()) {
            if (c.getParameterCount() == 0) defCtor = c;
        }
        Assert.assertFalse(ai.hasCreatorAnnotation(defCtor));
        Assert.assertNull(ai.findCreatorBinding(defCtor));
    }

    @Test
    public void testPropertyNameHelper() {
        PropertyName pn1 = ai._propertyName("", "ns");
        Assert.assertSame(PropertyName.USE_DEFAULT, pn1);

        PropertyName pn2 = ai._propertyName("name", null);
        Assert.assertEquals("name", pn2.getSimpleName());
        Assert.assertNull(pn2.getNamespace());

        PropertyName pn3 = ai._propertyName("name", "");
        Assert.assertEquals("name", pn3.getSimpleName());
        Assert.assertNull(pn3.getNamespace());

        PropertyName pn4 = ai._propertyName("name", "ns");
        Assert.assertEquals("name", pn4.getSimpleName());
        Assert.assertEquals("ns", pn4.getNamespace());
    }

    @Test
    public void testClassIfExplicitHelper() {
        Assert.assertNull(ai._classIfExplicit(null));
        Assert.assertNull(ai._classIfExplicit(Void.class));
        Assert.assertNull(ai._classIfExplicit(NoClass.class));
        Assert.assertEquals(String.class, ai._classIfExplicit(String.class));
        Assert.assertNull(ai._classIfExplicit(String.class, String.class));
        Assert.assertEquals(String.class, ai._classIfExplicit(String.class, Integer.class));
    }
}
