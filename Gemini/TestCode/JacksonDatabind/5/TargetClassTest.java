package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector.MixInResolver;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;
import java.util.*;

public class AnnotatedClassTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnA {
        String value() default "A";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface AnnB {
        String value() default "B";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @AnnA("bundle-A")
    @AnnB("bundle-B")
    public @interface BundleAnn {
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface ParamAnn {
        int index() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface IgnorableAnn {
    }

    public static class SimpleMixInResolver implements MixInResolver {
        private final Map<Class<?>, Class<?>> _mappings = new HashMap<Class<?>, Class<?>>();

        public SimpleMixInResolver add(Class<?> target, Class<?> mixin) {
            _mappings.put(target, mixin);
            return this;
        }

        @Override
        public Class<?> findMixInClassFor(Class<?> cls) {
            return _mappings.get(cls);
        }
    }

    public static class CustomAnnotationIntrospector extends AnnotationIntrospector {
        @Override
        public boolean isAnnotationBundle(Annotation ann) {
            return ann.annotationType() == BundleAnn.class;
        }

        @Override
        public boolean hasIgnoreMarker(AnnotatedMember m) {
            return m.hasAnnotation(IgnorableAnn.class);
        }

        @Override
        public com.fasterxml.jackson.core.Version version() {
            return com.fasterxml.jackson.core.Version.unknownVersion();
        }
    }

    public interface Interface1 {
        @AnnA("interfaceMethod")
        void doSomething(int x);
    }

    @AnnA("baseClass")
    public static class BaseClass implements Interface1 {
        @AnnA("baseField")
        public String field1;
        public transient String transientField;
        public static String staticField;

        public BaseClass() {}

        @Override
        public void doSomething(int x) {}

        public static BaseClass createBase() {
            return new BaseClass();
        }
    }

    @AnnB("subClass")
    public static class SubClass extends BaseClass {
        @AnnB("subField")
        public String field2;

        public SubClass() {}

        @IgnorableAnn
        public SubClass(int arg) {}

        public SubClass(String s, int i) {}

        @Override
        @AnnB("subDoSomething")
        public void doSomething(int x) {}

        public void methodWithTwoParams(String a, String b) {}

        public void methodWithThreeParams(String a, String b, String c) {}

        @AnnA("subFactory")
        public static SubClass create(String s) {
            return new SubClass();
        }

        @IgnorableAnn
        public static SubClass ignoredFactory(int i) {
            return new SubClass();
        }
    }

    @AnnA("mixInClass")
    public static class SubClassMixIn {
        @AnnA("mixinField2")
        public String field2;

        public SubClassMixIn(@ParamAnn(index = 1) String s, int i) {}

        @AnnA("mixInDoSomething")
        public void doSomething(int x) {}

        public static SubClass create(@ParamAnn(index = 2) String s) {
            return null;
        }
    }

    public enum TestEnum {
        A(10),
        B(20);

        private final int value;

        TestEnum(@ParamAnn(index = 99) int v) {
            this.value = v;
        }
    }

    public class NonStaticInner {
        public NonStaticInner(@ParamAnn(index = 5) String val) {}
    }

    public static class ObjectMixIn {
        @AnnA("objectHashCode")
        public int hashCode() { return 0; }
    }

    public static class ClassWithIgnoredDefaultCtor {
        @IgnorableAnn
        public ClassWithIgnoredDefaultCtor() {}
    }

    @Test
    public void testBasicPropertiesAndAnnotationsWithoutIntrospector() {
        AnnotatedClass ac = AnnotatedClass.construct(BaseClass.class, null, null);
        Assert.assertEquals(BaseClass.class, ac.getAnnotated());
        Assert.assertEquals(BaseClass.class.getModifiers(), ac.getModifiers());
        Assert.assertEquals(BaseClass.class.getName(), ac.getName());
        Assert.assertEquals(BaseClass.class, ac.getGenericType());
        Assert.assertEquals(BaseClass.class, ac.getRawType());
        Assert.assertNull(ac.getAnnotation(AnnA.class));
        Assert.assertFalse(ac.hasAnnotations());
        Assert.assertNotNull(ac.getAnnotations());
        Assert.assertNotNull(ac.getAllAnnotations());
        Assert.assertFalse(ac.annotations().iterator().hasNext());
        Assert.assertEquals("[AnnotedClass " + BaseClass.class.getName() + "]", ac.toString());
    }

    @Test
    public void testClassAnnotationsWithInheritanceAndIntrospector() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, ai, null);

        Assert.assertTrue(ac.hasAnnotations());
        Assert.assertNotNull(ac.getAnnotation(AnnB.class));
        Assert.assertEquals("subClass", ac.getAnnotation(AnnB.class).value());
        Assert.assertNotNull(ac.getAnnotation(AnnA.class));
        Assert.assertEquals("baseClass", ac.getAnnotation(AnnA.class).value());
    }

    @Test
    public void testConstructWithoutSuperTypes() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(SubClass.class, ai, null);

        Assert.assertNotNull(ac.getAnnotation(AnnB.class));
        Assert.assertNull(ac.getAnnotation(AnnA.class));
    }

    @Test
    public void testWithAnnotations() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(BaseClass.class, ai, null);
        AnnotationMap map = new AnnotationMap();
        AnnotatedClass ac2 = ac.withAnnotations(map);

        Assert.assertNotSame(ac, ac2);
        Assert.assertFalse(ac2.hasAnnotations());
    }

    @Test
    public void testClassMixIns() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        SimpleMixInResolver resolver = new SimpleMixInResolver();
        resolver.add(SubClass.class, SubClassMixIn.class);

        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, ai, resolver);
        Assert.assertNotNull(ac.getAnnotation(AnnA.class));
        Assert.assertEquals("mixInClass", ac.getAnnotation(AnnA.class).value());
    }

    @Test
    public void testAnnotationBundle() {
        @BundleAnn
        class BundledClass {}

        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(BundledClass.class, ai, null);

        Assert.assertTrue(ac.hasAnnotations());
        Assert.assertNotNull(ac.getAnnotation(AnnA.class));
        Assert.assertEquals("bundle-A", ac.getAnnotation(AnnA.class).value());
        Assert.assertNotNull(ac.getAnnotation(AnnB.class));
        Assert.assertEquals("bundle-B", ac.getAnnotation(AnnB.class).value());
    }

    @Test
    public void testCreatorsResolutionAndIgnoring() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        SimpleMixInResolver resolver = new SimpleMixInResolver();
        resolver.add(SubClass.class, SubClassMixIn.class);

        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, ai, resolver);

        AnnotatedConstructor defaultCtor = ac.getDefaultConstructor();
        Assert.assertNotNull(defaultCtor);

        List<AnnotatedConstructor> ctors = ac.getConstructors();
        Assert.assertEquals(1, ctors.size());
        AnnotatedConstructor ctor = ctors.get(0);
        Assert.assertEquals(2, ctor.getParameterCount());
        Assert.assertNotNull(ctor.getParameterAnnotation(0, ParamAnn.class));

        List<AnnotatedMethod> staticMethods = ac.getStaticMethods();
        Assert.assertEquals(1, staticMethods.size());
        AnnotatedMethod factory = staticMethods.get(0);
        Assert.assertEquals("create", factory.getName());
        Assert.assertNotNull(factory.getParameterAnnotation(0, ParamAnn.class));
    }

    @Test
    public void testIgnoredDefaultConstructor() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(ClassWithIgnoredDefaultCtor.class, ai, null);
        Assert.assertNull(ac.getDefaultConstructor());
    }

    @Test
    public void testMemberMethodsResolutionAndHierarchy() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        SimpleMixInResolver resolver = new SimpleMixInResolver();
        resolver.add(SubClass.class, SubClassMixIn.class);
        resolver.add(Object.class, ObjectMixIn.class);

        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, ai, resolver);

        Assert.assertTrue(ac.getMemberMethodCount() > 0);
        AnnotatedMethod method = ac.findMethod("doSomething", new Class<?>[]{int.class});
        Assert.assertNotNull(method);
        Assert.assertNotNull(method.getAnnotation(AnnA.class));
        Assert.assertNotNull(method.getAnnotation(AnnB.class));

        AnnotatedMethod twoParamMethod = ac.findMethod("methodWithTwoParams", new Class<?>[]{String.class, String.class});
        Assert.assertNotNull(twoParamMethod);

        AnnotatedMethod threeParamMethod = ac.findMethod("methodWithThreeParams", new Class<?>[]{String.class, String.class, String.class});
        Assert.assertNull(threeParamMethod);

        AnnotatedMethod hashCodeMethod = ac.findMethod("hashCode", new Class<?>[]{});
        Assert.assertNotNull(hashCodeMethod);
        Assert.assertNotNull(hashCodeMethod.getAnnotation(AnnA.class));

        int count = 0;
        for (AnnotatedMethod m : ac.memberMethods()) {
            if (m != null) {
                count++;
            }
        }
        Assert.assertEquals(ac.getMemberMethodCount(), count);
    }

    @Test
    public void testFieldsResolutionAndMixIns() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        SimpleMixInResolver resolver = new SimpleMixInResolver();
        resolver.add(SubClass.class, SubClassMixIn.class);

        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, ai, resolver);

        Assert.assertEquals(2, ac.getFieldCount());

        boolean foundField1 = false;
        boolean foundField2 = false;

        for (AnnotatedField f : ac.fields()) {
            if ("field1".equals(f.getName())) {
                foundField1 = true;
                Assert.assertNotNull(f.getAnnotation(AnnA.class));
            } else if ("field2".equals(f.getName())) {
                foundField2 = true;
                Assert.assertNotNull(f.getAnnotation(AnnB.class));
                Assert.assertNotNull(f.getAnnotation(AnnA.class));
                Assert.assertEquals("mixinField2", f.getAnnotation(AnnA.class).value());
            } else {
                Assert.fail("Unexpected field found: " + f.getName());
            }
        }

        Assert.assertTrue(foundField1);
        Assert.assertTrue(foundField2);
    }

    @Test
    public void testEnumConstructorResolution() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(TestEnum.class, ai, null);

        List<AnnotatedConstructor> ctors = ac.getConstructors();
        Assert.assertFalse(ctors.isEmpty());
        AnnotatedConstructor ctor = ctors.get(0);
        Assert.assertEquals(3, ctor.getParameterCount());
        Assert.assertNotNull(ctor.getParameterAnnotation(2, ParamAnn.class));
        Assert.assertEquals(99, ctor.getParameterAnnotation(2, ParamAnn.class).index());
    }

    @Test
    public void testNonStaticInnerClassConstructorResolution() {
        AnnotationIntrospector ai = new CustomAnnotationIntrospector();
        AnnotatedClass ac = AnnotatedClass.construct(NonStaticInner.class, ai, null);

        List<AnnotatedConstructor> ctors = ac.getConstructors();
        Assert.assertEquals(1, ctors.size());
        AnnotatedConstructor ctor = ctors.get(0);
        Assert.assertEquals(2, ctor.getParameterCount());
        Assert.assertNotNull(ctor.getParameterAnnotation(1, ParamAnn.class));
        Assert.assertEquals(5, ctor.getParameterAnnotation(1, ParamAnn.class).index());
    }

    @Test
    public void testNoCreatorsAndNoFieldsClass() {
        interface EmptyInterface {}
        AnnotatedClass ac = AnnotatedClass.construct(EmptyInterface.class, null, null);

        Assert.assertNull(ac.getDefaultConstructor());
        Assert.assertTrue(ac.getConstructors().isEmpty());
        Assert.assertTrue(ac.getStaticMethods().isEmpty());
        Assert.assertEquals(0, ac.getFieldCount());
        Assert.assertFalse(ac.fields().iterator().hasNext());
    }

    @Test
    public void testCreatorMethodsWithoutAnnotationIntrospector() {
        AnnotatedClass ac = AnnotatedClass.construct(SubClass.class, null, null);
        Assert.assertNotNull(ac.getDefaultConstructor());
        Assert.assertFalse(ac.getConstructors().isEmpty());
        Assert.assertFalse(ac.getStaticMethods().isEmpty());
        Assert.assertTrue(ac.getFieldCount() > 0);
    }
}
