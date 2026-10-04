package com.fasterxml.jackson.databind;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

public class AnnotationIntrospectorTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface SecondaryAnnotation {
    }

    private static class DummyIntrospector extends AnnotationIntrospector {
        private static final long serialVersionUID = 1L;

        @Override
        public Version version() {
            return Version.unknownVersion();
        }

        public <A extends Annotation> A publicFindAnnotation(Annotated a, Class<A> cls) {
            return _findAnnotation(a, cls);
        }

        public boolean publicHasAnnotation(Annotated a, Class<? extends Annotation> cls) {
            return _hasAnnotation(a, cls);
        }

        public boolean publicHasOneOf(Annotated a, Class<? extends Annotation>[] cls) {
            return _hasOneOf(a, cls);
        }
    }

    private enum TestEnum {
        A, B
    }

    @Test
    public void testReferenceProperty() {
        AnnotationIntrospector.ReferenceProperty managed = AnnotationIntrospector.ReferenceProperty.managed("mgr");
        Assert.assertEquals(AnnotationIntrospector.ReferenceProperty.Type.MANAGED_REFERENCE, managed.getType());
        Assert.assertEquals("mgr", managed.getName());
        Assert.assertTrue(managed.isManagedReference());
        Assert.assertFalse(managed.isBackReference());

        AnnotationIntrospector.ReferenceProperty back = AnnotationIntrospector.ReferenceProperty.back("bk");
        Assert.assertEquals(AnnotationIntrospector.ReferenceProperty.Type.BACK_REFERENCE, back.getType());
        Assert.assertEquals("bk", back.getName());
        Assert.assertFalse(back.isManagedReference());
        Assert.assertTrue(back.isBackReference());
    }

    @Test
    public void testFactoryMethodsAndCollections() {
        AnnotationIntrospector nop = AnnotationIntrospector.nopInstance();
        Assert.assertNotNull(nop);

        DummyIntrospector ai1 = new DummyIntrospector();
        DummyIntrospector ai2 = new DummyIntrospector();
        AnnotationIntrospector pair = AnnotationIntrospector.pair(ai1, ai2);
        Assert.assertNotNull(pair);

        Collection<AnnotationIntrospector> intros = ai1.allIntrospectors();
        Assert.assertEquals(1, intros.size());
        Assert.assertTrue(intros.contains(ai1));

        List<AnnotationIntrospector> list = new ArrayList<AnnotationIntrospector>();
        Collection<AnnotationIntrospector> res = ai1.allIntrospectors(list);
        Assert.assertSame(list, res);
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.contains(ai1));
    }

    @Test
    public void testDefaultImplementationsReturnNullOrDefaults() {
        DummyIntrospector ai = new DummyIntrospector();
        Assert.assertEquals(Version.unknownVersion(), ai.version());
        Assert.assertFalse(ai.isAnnotationBundle(null));
        Assert.assertNull(ai.findObjectIdInfo(null));

        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, null, null);
        Assert.assertSame(info, ai.findObjectReferenceInfo(null, info));

        Assert.assertNull(ai.findRootName(null));
        Assert.assertNull(ai.findPropertiesToIgnore(null, true));
        Assert.assertNull(ai.findPropertiesToIgnore(null));
        Assert.assertNull(ai.findIgnoreUnknownProperties(null));
        Assert.assertNull(ai.isIgnorableType(null));
        Assert.assertNull(ai.findFilterId(null));
        Assert.assertNull(ai.findNamingStrategy(null));
        Assert.assertNull(ai.findClassDescription(null));

        VisibilityChecker<?> checker = VisibilityChecker.Std.defaultInstance();
        Assert.assertSame(checker, ai.findAutoDetectVisibility(null, checker));

        Assert.assertNull(ai.findTypeResolver(null, null, null));
        Assert.assertNull(ai.findPropertyTypeResolver(null, null, null));
        Assert.assertNull(ai.findPropertyContentTypeResolver(null, null, null));
        Assert.assertNull(ai.findSubtypes(null));
        Assert.assertNull(ai.findTypeName(null));
        Assert.assertNull(ai.isTypeId(null));

        Assert.assertNull(ai.findReferenceType(null));
        Assert.assertNull(ai.findUnwrappingNameTransformer(null));
        Assert.assertFalse(ai.hasIgnoreMarker(null));
        Assert.assertNull(ai.findInjectableValueId(null));
        Assert.assertNull(ai.hasRequiredMarker(null));
        Assert.assertNull(ai.findViews(null));
        Assert.assertNull(ai.findFormat(null));
        Assert.assertNull(ai.findWrapperName(null));
        Assert.assertNull(ai.findPropertyDefaultValue(null));
        Assert.assertNull(ai.findPropertyDescription(null));
        Assert.assertNull(ai.findPropertyIndex(null));
        Assert.assertNull(ai.findImplicitPropertyName(null));
        Assert.assertNull(ai.findPropertyAccess(null));
        Assert.assertNull(ai.resolveSetterConflict(null, null, null));

        Assert.assertNull(ai.findSerializer(null));
        Assert.assertNull(ai.findKeySerializer(null));
        Assert.assertNull(ai.findContentSerializer(null));
        Assert.assertNull(ai.findNullSerializer(null));
        Assert.assertNull(ai.findSerializationTyping(null));
        Assert.assertNull(ai.findSerializationConverter(null));
        Assert.assertNull(ai.findSerializationContentConverter(null));
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, ai.findSerializationInclusion(null, JsonInclude.Include.NON_EMPTY));
        Assert.assertEquals(JsonInclude.Include.NON_EMPTY, ai.findSerializationInclusionForContent(null, JsonInclude.Include.NON_EMPTY));
        Assert.assertEquals(JsonInclude.Value.empty(), ai.findPropertyInclusion(null));

        Assert.assertNull(ai.findSerializationType(null));
        Assert.assertNull(ai.findSerializationKeyType(null, null));
        Assert.assertNull(ai.findSerializationContentType(null, null));

        Assert.assertNull(ai.findSerializationPropertyOrder(null));
        Assert.assertNull(ai.findSerializationSortAlphabetically(null));
        ai.findAndAddVirtualProperties(null, null, null);

        Assert.assertNull(ai.findNameForSerialization(null));
        Assert.assertFalse(ai.hasAsValueAnnotation(null));
        Assert.assertEquals("A", ai.findEnumValue(TestEnum.A));

        String[] names = new String[] { null, "custom" };
        String[] result = ai.findEnumValues(TestEnum.class, TestEnum.values(), names);
        Assert.assertEquals("A", result[0]);
        Assert.assertEquals("custom", result[1]);

        Assert.assertNull(ai.findDeserializer(null));
        Assert.assertNull(ai.findKeyDeserializer(null));
        Assert.assertNull(ai.findContentDeserializer(null));
        Assert.assertNull(ai.findDeserializationConverter(null));
        Assert.assertNull(ai.findDeserializationContentConverter(null));
        Assert.assertNull(ai.findDeserializationType(null, null));
        Assert.assertNull(ai.findDeserializationKeyType(null, null));
        Assert.assertNull(ai.findDeserializationContentType(null, null));

        Assert.assertNull(ai.findValueInstantiator(null));
        Assert.assertNull(ai.findPOJOBuilder(null));
        Assert.assertNull(ai.findPOJOBuilderConfig(null));
        Assert.assertNull(ai.findNameForDeserialization(null));
        Assert.assertFalse(ai.hasAnySetterAnnotation(null));
        Assert.assertFalse(ai.hasAnyGetterAnnotation(null));
        Assert.assertFalse(ai.hasCreatorAnnotation(null));
        Assert.assertNull(ai.findCreatorBinding(null));
    }

    @Test
    public void testProtectedAnnotationHelpers() {
        DummyIntrospector ai = new DummyIntrospector();
        ObjectMapper mapper = new ObjectMapper();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(AnnotatedTestClass.class, mapper.getDeserializationConfig());

        Assert.assertNotNull(ai.publicFindAnnotation(ac, TestAnnotation.class));
        Assert.assertNull(ai.publicFindAnnotation(ac, SecondaryAnnotation.class));
        Assert.assertTrue(ai.publicHasAnnotation(ac, TestAnnotation.class));
        Assert.assertFalse(ai.publicHasAnnotation(ac, SecondaryAnnotation.class));

        @SuppressWarnings("unchecked")
        Class<? extends Annotation>[] classesWithMatch = new Class[] { SecondaryAnnotation.class, TestAnnotation.class };
        Assert.assertTrue(ai.publicHasOneOf(ac, classesWithMatch));

        @SuppressWarnings("unchecked")
        Class<? extends Annotation>[] classesWithoutMatch = new Class[] { SecondaryAnnotation.class };
        Assert.assertFalse(ai.publicHasOneOf(ac, classesWithoutMatch));
    }

    @Test
    public void testRefineSerializationTypeUnmodified() throws Exception {
        DummyIntrospector ai = new DummyIntrospector();
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, mapper.getSerializationConfig());

        JavaType refined = ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
        Assert.assertSame(baseType, refined);
    }

    @Test
    public void testRefineSerializationTypeSameClass() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findSerializationType(Annotated a) {
                return String.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, mapper.getSerializationConfig());

        JavaType refined = ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
        Assert.assertTrue(refined.useStaticType());
    }

    @Test
    public void testRefineSerializationTypeGeneralization() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findSerializationType(Annotated a) {
                return Number.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Integer.class, mapper.getSerializationConfig());

        JavaType refined = ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
        Assert.assertEquals(Number.class, refined.getRawClass());
    }

    @Test(expected = JsonMappingException.class)
    public void testRefineSerializationTypeGeneralizationFailure() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findSerializationType(Annotated a) {
                return List.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Integer.class, mapper.getSerializationConfig());

        ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
    }

    @Test
    public void testRefineSerializationTypeMapKeyAndValue() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
                return CharSequence.class;
            }
            @Override
            public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
                return Number.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(HashMap.class, mapper.getSerializationConfig());

        JavaType refined = ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
        Assert.assertEquals(CharSequence.class, refined.getKeyType().getRawClass());
        Assert.assertEquals(Number.class, refined.getContentType().getRawClass());
    }

    @Test
    public void testRefineSerializationTypeMapKeySpecialization() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
                return String.class;
            }
            @Override
            public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
                return Integer.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructMapType(Map.class, Object.class, Number.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Map.class, mapper.getSerializationConfig());

        JavaType refined = ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
        Assert.assertEquals(String.class, refined.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, refined.getContentType().getRawClass());
    }

    @Test
    public void testRefineSerializationTypeMapStaticTyping() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
                return String.class;
            }
            @Override
            public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
                return Integer.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Map.class, mapper.getSerializationConfig());

        JavaType refined = ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
        Assert.assertTrue(refined.getKeyType().useStaticType());
        Assert.assertTrue(refined.getContentType().useStaticType());
    }

    @Test(expected = JsonMappingException.class)
    public void testRefineSerializationTypeUnrelatedKey() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findSerializationKeyType(Annotated am, JavaType baseType) {
                return List.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Map.class, mapper.getSerializationConfig());

        ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
    }

    @Test(expected = JsonMappingException.class)
    public void testRefineSerializationTypeUnrelatedContent() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
                return List.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Map.class, mapper.getSerializationConfig());

        ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
    }

    @Test
    public void testRefineDeserializationTypeUnmodified() throws Exception {
        DummyIntrospector ai = new DummyIntrospector();
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(CharSequence.class, mapper.getDeserializationConfig());

        JavaType refined = ai.refineDeserializationType(mapper.getDeserializationConfig(), ac, baseType);
        Assert.assertSame(baseType, refined);
    }

    @Test
    public void testRefineDeserializationTypeSpecialization() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findDeserializationType(Annotated am, JavaType baseType) {
                return String.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(CharSequence.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(CharSequence.class, mapper.getDeserializationConfig());

        JavaType refined = ai.refineDeserializationType(mapper.getDeserializationConfig(), ac, baseType);
        Assert.assertEquals(String.class, refined.getRawClass());
    }

    @Test(expected = JsonMappingException.class)
    public void testRefineDeserializationTypeSpecializationFailure() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findDeserializationType(Annotated am, JavaType baseType) {
                return Integer.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, mapper.getDeserializationConfig());

        ai.refineDeserializationType(mapper.getDeserializationConfig(), ac, baseType);
    }

    @Test
    public void testRefineDeserializationTypeMapKeyAndContent() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findDeserializationKeyType(Annotated am, JavaType baseKeyType) {
                return String.class;
            }
            @Override
            public Class<?> findDeserializationContentType(Annotated am, JavaType baseContentType) {
                return Integer.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructMapType(Map.class, Object.class, Number.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Map.class, mapper.getDeserializationConfig());

        JavaType refined = ai.refineDeserializationType(mapper.getDeserializationConfig(), ac, baseType);
        Assert.assertEquals(String.class, refined.getKeyType().getRawClass());
        Assert.assertEquals(Integer.class, refined.getContentType().getRawClass());
    }

    @Test(expected = JsonMappingException.class)
    public void testRefineDeserializationTypeMapKeyFailure() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findDeserializationKeyType(Annotated am, JavaType baseKeyType) {
                return List.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Number.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Map.class, mapper.getDeserializationConfig());

        ai.refineDeserializationType(mapper.getDeserializationConfig(), ac, baseType);
    }

    @Test(expected = JsonMappingException.class)
    public void testRefineDeserializationTypeMapContentFailure() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findDeserializationContentType(Annotated am, JavaType baseContentType) {
                return List.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Number.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(Map.class, mapper.getDeserializationConfig());

        ai.refineDeserializationType(mapper.getDeserializationConfig(), ac, baseType);
    }

    @Test
    public void testRefineDeserializationTypeSameRawTypeDoesNotSpecialize() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findDeserializationType(Annotated am, JavaType baseType) {
                return String.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, mapper.getDeserializationConfig());

        JavaType refined = ai.refineDeserializationType(mapper.getDeserializationConfig(), ac, baseType);
        Assert.assertSame(baseType, refined);
    }

    @Test
    public void testRefineSerializationCollectionContent() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findSerializationContentType(Annotated am, JavaType baseType) {
                return Number.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructCollectionType(List.class, Integer.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(List.class, mapper.getSerializationConfig());

        JavaType refined = ai.refineSerializationType(mapper.getSerializationConfig(), ac, baseType);
        Assert.assertEquals(Number.class, refined.getContentType().getRawClass());
    }

    @Test
    public void testRefineDeserializationCollectionContent() throws Exception {
        AnnotationIntrospector ai = new DummyIntrospector() {
            private static final long serialVersionUID = 1L;
            @Override
            public Class<?> findDeserializationContentType(Annotated am, JavaType baseContentType) {
                return Integer.class;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        JavaType baseType = TypeFactory.defaultInstance().constructCollectionType(List.class, Number.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(List.class, mapper.getDeserializationConfig());

        JavaType refined = ai.refineDeserializationType(mapper.getDeserializationConfig(), ac, baseType);
        Assert.assertEquals(Integer.class, refined.getContentType().getRawClass());
    }

    @TestAnnotation("testClass")
    private static class AnnotatedTestClass {
    }
}
