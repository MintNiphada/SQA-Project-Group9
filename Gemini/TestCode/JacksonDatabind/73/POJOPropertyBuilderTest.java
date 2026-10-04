package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class POJOPropertyBuilderTest {

    static class BaseClass {
        public int prop;
        public int getProp() { return prop; }
        public void setProp(int p) { this.prop = p; }
    }

    static class SubClass extends BaseClass {
        public int prop;
        public SubClass(int p) { this.prop = p; }
        public SubClass(int p, String other) { this.prop = p; }
        @Override
        public int getProp() { return prop; }
        public boolean isProp() { return true; }
        public int prop() { return prop; }
        @Override
        public void setProp(int p) { this.prop = p; }
        public void prop(int p) { this.prop = p; }
        public static SubClass factory(int p) { return new SubClass(p); }
    }

    static class UnrelatedClass {
        public int prop;
        public int getProp() { return prop; }
        public void setProp(int p) { this.prop = p; }
    }

    private AnnotatedClass _annotatedClass;
    private AnnotatedField _baseField;
    private AnnotatedField _subField;
    private AnnotatedField _unrelatedField;
    private AnnotatedMethod _baseGetter;
    private AnnotatedMethod _subGetter;
    private AnnotatedMethod _subIsGetter;
    private AnnotatedMethod _subImplicitGetter;
    private AnnotatedMethod _unrelatedGetter;
    private AnnotatedMethod _baseSetter;
    private AnnotatedMethod _subSetter;
    private AnnotatedMethod _subImplicitSetter;
    private AnnotatedMethod _unrelatedSetter;
    private AnnotatedParameter _subCtorParam;
    private AnnotatedParameter _factoryParam;

    @Before
    public void setUp() throws Exception {
        _annotatedClass = AnnotatedClass.constructWithoutSuperTypes(SubClass.class, null);

        Field bf = BaseClass.class.getDeclaredField("prop");
        Field sf = SubClass.class.getDeclaredField("prop");
        Field uf = UnrelatedClass.class.getDeclaredField("prop");
        _baseField = new AnnotatedField(null, bf, new AnnotationMap());
        _subField = new AnnotatedField(null, sf, new AnnotationMap());
        _unrelatedField = new AnnotatedField(null, uf, new AnnotationMap());

        Method bg = BaseClass.class.getDeclaredMethod("getProp");
        Method sg = SubClass.class.getDeclaredMethod("getProp");
        Method sig = SubClass.class.getDeclaredMethod("isProp");
        Method smg = SubClass.class.getDeclaredMethod("prop");
        Method ug = UnrelatedClass.class.getDeclaredMethod("getProp");
        _baseGetter = new AnnotatedMethod(null, bg, new AnnotationMap(), null);
        _subGetter = new AnnotatedMethod(null, sg, new AnnotationMap(), null);
        _subIsGetter = new AnnotatedMethod(null, sig, new AnnotationMap(), null);
        _subImplicitGetter = new AnnotatedMethod(null, smg, new AnnotationMap(), null);
        _unrelatedGetter = new AnnotatedMethod(null, ug, new AnnotationMap(), null);

        Method bs = BaseClass.class.getDeclaredMethod("setProp", int.class);
        Method ss = SubClass.class.getDeclaredMethod("setProp", int.class);
        Method sms = SubClass.class.getDeclaredMethod("prop", int.class);
        Method us = UnrelatedClass.class.getDeclaredMethod("setProp", int.class);
        _baseSetter = new AnnotatedMethod(null, bs, new AnnotationMap(), null);
        _subSetter = new AnnotatedMethod(null, ss, new AnnotationMap(), null);
        _subImplicitSetter = new AnnotatedMethod(null, sms, new AnnotationMap(), null);
        _unrelatedSetter = new AnnotatedMethod(null, us, new AnnotationMap(), null);

        Constructor<SubClass> ctor = SubClass.class.getDeclaredConstructor(int.class);
        AnnotatedConstructor aCtor = new AnnotatedConstructor(null, ctor, new AnnotationMap(), null);
        _subCtorParam = new AnnotatedParameter(aCtor, Integer.TYPE, new AnnotationMap(), 0);

        Method fm = SubClass.class.getDeclaredMethod("factory", int.class);
        AnnotatedMethod aFactory = new AnnotatedMethod(null, fm, new AnnotationMap(), null);
        _factoryParam = new AnnotatedParameter(aFactory, Integer.TYPE, new AnnotationMap(), 0);
    }

    @Test
    public void testBasicPropertiesAndNaming() {
        PropertyName propName = PropertyName.construct("testProp");
        POJOPropertyBuilder builder = new POJOPropertyBuilder(null, null, true, propName);

        Assert.assertEquals("testProp", builder.getName());
        Assert.assertEquals(propName, builder.getFullName());
        Assert.assertTrue(builder.hasName(propName));
        Assert.assertFalse(builder.hasName(PropertyName.construct("other")));
        Assert.assertEquals("testProp", builder.getInternalName());
        Assert.assertNull(builder.getWrapperName());

        POJOPropertyBuilder renamed = builder.withSimpleName("newProp");
        Assert.assertEquals("newProp", renamed.getName());
        Assert.assertSame(renamed, renamed.withSimpleName("newProp"));

        POJOPropertyBuilder renamed2 = builder.withName(PropertyName.construct("another"));
        Assert.assertEquals("another", renamed2.getName());

        POJOPropertyBuilder nullNameBuilder = new POJOPropertyBuilder(null, null, true, PropertyName.construct("internal"), null);
        Assert.assertNull(nullNameBuilder.getName());
        Assert.assertNull(nullNameBuilder.getFullName());
    }

    @Test
    public void testCompareTo() {
        PropertyName p1 = PropertyName.construct("a");
        PropertyName p2 = PropertyName.construct("b");

        POJOPropertyBuilder b1 = new POJOPropertyBuilder(null, null, true, p1);
        POJOPropertyBuilder b2 = new POJOPropertyBuilder(null, null, true, p2);
        Assert.assertTrue(b1.compareTo(b2) < 0);
        Assert.assertTrue(b2.compareTo(b1) > 0);
        Assert.assertEquals(0, b1.compareTo(b1));

        b2.addCtor(_subCtorParam, p2, true, true, false);
        Assert.assertTrue(b1.compareTo(b2) > 0);
        Assert.assertTrue(b2.compareTo(b1) < 0);

        b1.addCtor(_subCtorParam, p1, true, true, false);
        Assert.assertTrue(b1.compareTo(b2) < 0);
    }

    @Test
    public void testAccessorsAndPredicates() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        Assert.assertFalse(b.hasGetter());
        Assert.assertFalse(b.hasSetter());
        Assert.assertFalse(b.hasField());
        Assert.assertFalse(b.hasConstructorParameter());
        Assert.assertFalse(b.couldSerialize());
        Assert.assertFalse(b.couldDeserialize());

        b.addField(_subField, PropertyName.construct("prop"), false, true, false);
        Assert.assertTrue(b.hasField());
        Assert.assertTrue(b.couldSerialize());
        Assert.assertTrue(b.couldDeserialize());
        Assert.assertSame(_subField, b.getField());

        b.addGetter(_subGetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertTrue(b.hasGetter());
        Assert.assertSame(_subGetter, b.getGetter());

        b.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertTrue(b.hasSetter());
        Assert.assertSame(_subSetter, b.getSetter());

        b.addCtor(_subCtorParam, PropertyName.construct("prop"), false, true, false);
        Assert.assertTrue(b.hasConstructorParameter());
        Assert.assertSame(_subCtorParam, b.getConstructorParameter());

        Assert.assertSame(_subGetter, b.getAccessor());
        Assert.assertSame(_subCtorParam, b.getMutator());
        Assert.assertSame(_subSetter, b.getNonConstructorMutator());
        Assert.assertSame(_subGetter, b.getPrimaryMember());

        POJOPropertyBuilder bDeser = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        bDeser.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subSetter, bDeser.getPrimaryMember());
        Assert.assertSame(_subSetter, bDeser.getMutator());

        POJOPropertyBuilder bFieldOnly = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        bFieldOnly.addField(_subField, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subField, bFieldOnly.getAccessor());
        Assert.assertSame(_subField, bFieldOnly.getMutator());
        Assert.assertSame(_subField, bFieldOnly.getNonConstructorMutator());
    }

    @Test
    public void testGetterPrecedenceAndMasking() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b.addGetter(_baseGetter, PropertyName.construct("prop"), false, true, false);
        b.addGetter(_subGetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subGetter, b.getGetter());

        POJOPropertyBuilder bReverse = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        bReverse.addGetter(_subGetter, PropertyName.construct("prop"), false, true, false);
        bReverse.addGetter(_baseGetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subGetter, bReverse.getGetter());

        POJOPropertyBuilder bPriority = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        bPriority.addGetter(_subImplicitGetter, PropertyName.construct("prop"), false, true, false);
        bPriority.addGetter(_subIsGetter, PropertyName.construct("prop"), false, true, false);
        bPriority.addGetter(_subGetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subGetter, bPriority.getGetter());

        POJOPropertyBuilder bPriorityIs = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        bPriorityIs.addGetter(_subImplicitGetter, PropertyName.construct("prop"), false, true, false);
        bPriorityIs.addGetter(_subIsGetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subIsGetter, bPriorityIs.getGetter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConflictingGetters() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b.addGetter(_unrelatedGetter, PropertyName.construct("prop"), false, true, false);
        b.addGetter(_subGetter, PropertyName.construct("prop"), false, true, false);
        b.getGetter();
    }

    @Test
    public void testSetterPrecedenceAndMasking() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        b.addSetter(_baseSetter, PropertyName.construct("prop"), false, true, false);
        b.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subSetter, b.getSetter());

        POJOPropertyBuilder bReverse = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        bReverse.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        bReverse.addSetter(_baseSetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subSetter, bReverse.getSetter());

        POJOPropertyBuilder bPriority = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        bPriority.addSetter(_subImplicitSetter, PropertyName.construct("prop"), false, true, false);
        bPriority.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subSetter, bPriority.getSetter());
    }

    @Test
    public void testSetterConflictResolutionViaAnnotationIntrospector() {
        AnnotationIntrospector ai = new AnnotationIntrospector() {
            @Override
            public AnnotatedMethod resolveSetterConflict(MapperConfig<?> config, AnnotatedMethod setter1, AnnotatedMethod setter2) {
                return setter1;
            }
        };
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, ai, false, PropertyName.construct("prop"));
        b.addSetter(_unrelatedSetter, PropertyName.construct("prop"), false, true, false);
        b.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subSetter, b.getSetter());

        AnnotationIntrospector ai2 = new AnnotationIntrospector() {
            @Override
            public AnnotatedMethod resolveSetterConflict(MapperConfig<?> config, AnnotatedMethod setter1, AnnotatedMethod setter2) {
                return setter2;
            }
        };
        POJOPropertyBuilder b2 = new POJOPropertyBuilder(null, ai2, false, PropertyName.construct("prop"));
        b2.addSetter(_unrelatedSetter, PropertyName.construct("prop"), false, true, false);
        b2.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_unrelatedSetter, b2.getSetter());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConflictingSetters() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        b.addSetter(_unrelatedSetter, PropertyName.construct("prop"), false, true, false);
        b.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        b.getSetter();
    }

    @Test
    public void testFieldMaskingAndConflict() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b.addField(_baseField, PropertyName.construct("prop"), false, true, false);
        b.addField(_subField, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subField, b.getField());

        POJOPropertyBuilder bReverse = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        bReverse.addField(_subField, PropertyName.construct("prop"), false, true, false);
        bReverse.addField(_baseField, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subField, bReverse.getField());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConflictingFields() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b.addField(_unrelatedField, PropertyName.construct("prop"), false, true, false);
        b.addField(_subField, PropertyName.construct("prop"), false, true, false);
        b.getField();
    }

    @Test
    public void testConstructorParameters() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        Assert.assertFalse(b.getConstructorParameters().hasNext());
        Assert.assertNull(b.getConstructorParameter());

        b.addCtor(_factoryParam, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_factoryParam, b.getConstructorParameter());

        b.addCtor(_subCtorParam, PropertyName.construct("prop"), false, true, false);
        Assert.assertSame(_subCtorParam, b.getConstructorParameter());

        Iterator<AnnotatedParameter> it = b.getConstructorParameters();
        Assert.assertTrue(it.hasNext());
        Assert.assertSame(_subCtorParam, it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertSame(_factoryParam, it.next());
        Assert.assertFalse(it.hasNext());

        try {
            it.next();
            Assert.fail();
        } catch (NoSuchElementException ignored) {}

        try {
            it.remove();
            Assert.fail();
        } catch (UnsupportedOperationException ignored) {}

        b.removeConstructors();
        Assert.assertFalse(b.hasConstructorParameter());
    }

    @Test
    public void testExplicitsAndVisibility() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        Assert.assertFalse(b.isExplicitlyIncluded());
        Assert.assertFalse(b.isExplicitlyNamed());
        Assert.assertFalse(b.anyVisible());
        Assert.assertFalse(b.anyIgnorals());

        b.addField(_subField, PropertyName.construct("f"), true, true, false);
        Assert.assertTrue(b.isExplicitlyIncluded());
        Assert.assertTrue(b.isExplicitlyNamed());
        Assert.assertTrue(b.anyVisible());
        Assert.assertFalse(b.anyIgnorals());

        POJOPropertyBuilder b2 = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b2.addGetter(_subGetter, PropertyName.construct("g"), true, false, true);
        Assert.assertTrue(b2.anyIgnorals());
        Assert.assertFalse(b2.anyVisible());

        POJOPropertyBuilder b3 = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b3.addSetter(_subSetter, PropertyName.construct("s"), true, true, false);
        Assert.assertTrue(b3.isExplicitlyIncluded());
        Assert.assertTrue(b3.isExplicitlyNamed());

        POJOPropertyBuilder b4 = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b4.addCtor(_subCtorParam, PropertyName.construct("c"), true, true, false);
        Assert.assertTrue(b4.isExplicitlyIncluded());
        Assert.assertTrue(b4.isExplicitlyNamed());
    }

    @Test
    public void testRemoveIgnoredAndNonVisible() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b.addField(_subField, PropertyName.construct("f"), false, true, true);
        b.addGetter(_subGetter, PropertyName.construct("g"), false, true, true);
        b.addSetter(_subSetter, PropertyName.construct("s"), false, true, true);
        b.addCtor(_subCtorParam, PropertyName.construct("c"), false, true, true);
        b.removeIgnored();
        Assert.assertFalse(b.hasField());
        Assert.assertFalse(b.hasGetter());
        Assert.assertFalse(b.hasSetter());
        Assert.assertFalse(b.hasConstructorParameter());

        POJOPropertyBuilder b2 = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b2.addField(_subField, PropertyName.construct("f"), false, false, false);
        b2.addGetter(_subGetter, PropertyName.construct("g"), false, false, false);
        b2.addSetter(_subSetter, PropertyName.construct("s"), false, false, false);
        b2.addCtor(_subCtorParam, PropertyName.construct("c"), false, false, false);
        b2.removeNonVisible(false);
        Assert.assertFalse(b2.hasField());
        Assert.assertFalse(b2.hasGetter());
        Assert.assertFalse(b2.hasSetter());
        Assert.assertFalse(b2.hasConstructorParameter());
    }

    @Test
    public void testRemoveNonVisibleWithAccessTypes() {
        AnnotationIntrospector readOnlyAi = new AnnotationIntrospector() {
            @Override
            public JsonProperty.Access findPropertyAccess(Annotated ann) {
                return JsonProperty.Access.READ_ONLY;
            }
        };
        POJOPropertyBuilder bRead = new POJOPropertyBuilder(null, readOnlyAi, false, PropertyName.construct("prop"));
        bRead.addField(_subField, PropertyName.construct("f"), false, true, false);
        bRead.addSetter(_subSetter, PropertyName.construct("s"), false, true, false);
        bRead.addCtor(_subCtorParam, PropertyName.construct("c"), false, true, false);
        bRead.removeNonVisible(true);
        Assert.assertFalse(bRead.hasSetter());
        Assert.assertFalse(bRead.hasConstructorParameter());
        Assert.assertFalse(bRead.hasField());

        AnnotationIntrospector writeOnlyAi = new AnnotationIntrospector() {
            @Override
            public JsonProperty.Access findPropertyAccess(Annotated ann) {
                return JsonProperty.Access.WRITE_ONLY;
            }
        };
        POJOPropertyBuilder bWrite = new POJOPropertyBuilder(null, writeOnlyAi, true, PropertyName.construct("prop"));
        bWrite.addField(_subField, PropertyName.construct("f"), false, true, false);
        bWrite.addGetter(_subGetter, PropertyName.construct("g"), false, true, false);
        bWrite.removeNonVisible(true);
        Assert.assertFalse(bWrite.hasGetter());
        Assert.assertFalse(bWrite.hasField());

        AnnotationIntrospector rwAi = new AnnotationIntrospector() {
            @Override
            public JsonProperty.Access findPropertyAccess(Annotated ann) {
                return JsonProperty.Access.READ_WRITE;
            }
        };
        POJOPropertyBuilder bRW = new POJOPropertyBuilder(null, rwAi, true, PropertyName.construct("prop"));
        bRW.addField(_subField, PropertyName.construct("f"), false, true, false);
        bRW.removeNonVisible(true);
        Assert.assertTrue(bRW.hasField());
    }

    @Test
    public void testTrimByVisibility() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b.addField(_baseField, null, false, true, false);
        b.addField(_subField, PropertyName.construct("f"), true, true, false);
        b.trimByVisibility();
        Assert.assertSame(_subField, b.getField());

        POJOPropertyBuilder b2 = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b2.addGetter(_baseGetter, null, false, false, false);
        b2.addGetter(_subGetter, null, false, true, false);
        b2.trimByVisibility();
        Assert.assertSame(_subGetter, b2.getGetter());

        POJOPropertyBuilder b3 = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b3.addSetter(_baseSetter, null, false, true, false);
        b3.addSetter(_subSetter, null, false, false, false);
        b3.trimByVisibility();
        Assert.assertSame(_baseSetter, b3.getSetter());
    }

    @Test
    public void testAddAllAndMergeAnnotations() {
        POJOPropertyBuilder b1 = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b1.addGetter(_subGetter, PropertyName.construct("prop"), false, true, false);
        b1.addField(_subField, PropertyName.construct("prop"), false, true, false);

        POJOPropertyBuilder b2 = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b2.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        b2.addCtor(_subCtorParam, PropertyName.construct("prop"), false, true, false);

        b1.addAll(b2);
        Assert.assertTrue(b1.hasGetter());
        Assert.assertTrue(b1.hasField());
        Assert.assertTrue(b1.hasSetter());
        Assert.assertTrue(b1.hasConstructorParameter());

        b1.mergeAnnotations(true);
        b1.mergeAnnotations(false);

        POJOPropertyBuilder bDeser = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        bDeser.addCtor(_subCtorParam, PropertyName.construct("prop"), false, true, false);
        bDeser.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        bDeser.addField(_subField, PropertyName.construct("prop"), false, true, false);
        bDeser.addGetter(_subGetter, PropertyName.construct("prop"), false, true, false);
        bDeser.mergeAnnotations(false);

        POJOPropertyBuilder bDeserSetter = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        bDeserSetter.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        bDeserSetter.addField(_subField, PropertyName.construct("prop"), false, true, false);
        bDeserSetter.mergeAnnotations(false);

        POJOPropertyBuilder bDeserField = new POJOPropertyBuilder(null, null, false, PropertyName.construct("prop"));
        bDeserField.addField(_subField, PropertyName.construct("prop"), false, true, false);
        bDeserField.addGetter(_subGetter, PropertyName.construct("prop"), false, true, false);
        bDeserField.mergeAnnotations(false);

        POJOPropertyBuilder bSerField = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        bSerField.addField(_subField, PropertyName.construct("prop"), false, true, false);
        bSerField.addSetter(_subSetter, PropertyName.construct("prop"), false, true, false);
        bSerField.mergeAnnotations(true);
    }

    @Test
    public void testFindExplicitNamesAndExplode() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        Assert.assertTrue(b.findExplicitNames().isEmpty());

        PropertyName p1 = PropertyName.construct("name1");
        PropertyName p2 = PropertyName.construct("name2");
        b.addField(_subField, p1, true, true, false);
        b.addGetter(_subGetter, p2, true, true, false);

        Set<PropertyName> names = b.findExplicitNames();
        Assert.assertEquals(2, names.size());
        Assert.assertTrue(names.contains(p1));
        Assert.assertTrue(names.contains(p2));

        Collection<POJOPropertyBuilder> exploded = b.explode(names);
        Assert.assertEquals(2, exploded.size());

        POJOPropertyBuilder bConflict = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        bConflict.addField(_subField, null, false, true, false);
        bConflict.addGetter(_subGetter, p1, true, true, false);
        try {
            bConflict.explode(names);
            Assert.fail();
        } catch (IllegalStateException expected) {}
    }

    @Test
    public void testAnnotationIntrospectorDelegations() {
        final PropertyName wrapper = PropertyName.construct("wrap");
        final ObjectIdInfo objectId = new ObjectIdInfo(PropertyName.construct("id"), Object.class, null, null);
        final JsonInclude.Value incl = JsonInclude.Value.empty().withValueInclusion(JsonInclude.Include.NON_EMPTY);

        AnnotationIntrospector ai = new AnnotationIntrospector() {
            @Override
            public PropertyName findWrapperName(Annotated ann) { return wrapper; }
            @Override
            public Class<?>[] findViews(Annotated ann) { return new Class<?>[]{ String.class }; }
            @Override
            public ReferenceProperty findReferenceType(AnnotatedMember member) { return ReferenceProperty.managed("ref"); }
            @Override
            public Boolean isTypeId(AnnotatedMember member) { return Boolean.TRUE; }
            @Override
            public Boolean hasRequiredMarker(AnnotatedMember m) { return Boolean.TRUE; }
            @Override
            public String findPropertyDescription(AnnotatedMember m) { return "desc"; }
            @Override
            public Integer findPropertyIndex(AnnotatedMember m) { return 5; }
            @Override
            public String findPropertyDefaultValue(AnnotatedMember m) { return "def"; }
            @Override
            public ObjectIdInfo findObjectIdInfo(Annotated ann) { return objectId; }
            @Override
            public ObjectIdInfo findObjectReferenceInfo(Annotated ann, ObjectIdInfo info) { return info; }
            @Override
            public JsonInclude.Value findPropertyInclusion(Annotated a) { return incl; }
            @Override
            public JsonProperty.Access findPropertyAccess(Annotated ann) { return JsonProperty.Access.READ_ONLY; }
        };

        POJOPropertyBuilder b = new POJOPropertyBuilder(null, ai, true, PropertyName.construct("prop"));
        b.addGetter(_subGetter, PropertyName.construct("prop"), false, true, false);

        Assert.assertEquals(wrapper, b.getWrapperName());
        Assert.assertNotNull(b.findViews());
        Assert.assertEquals(1, b.findViews().length);
        Assert.assertNotNull(b.findReferenceType());
        Assert.assertTrue(b.isTypeId());
        Assert.assertEquals(objectId, b.findObjectIdInfo());
        Assert.assertEquals(incl, b.findInclusion());
        Assert.assertEquals(JsonProperty.Access.READ_ONLY, b.findAccess());

        PropertyMetadata md = b.getMetadata();
        Assert.assertEquals(Boolean.TRUE, md.getRequired());
        Assert.assertEquals("desc", md.getDescription());
        Assert.assertEquals(Integer.valueOf(5), md.getIndex());
        Assert.assertEquals("def", md.getDefaultValue());

        POJOPropertyBuilder bStdMeta = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        Assert.assertSame(PropertyMetadata.STD_REQUIRED_OR_OPTIONAL, bStdMeta.getMetadata());
    }

    @Test
    public void testToString() {
        POJOPropertyBuilder b = new POJOPropertyBuilder(null, null, true, PropertyName.construct("prop"));
        b.addField(_subField, PropertyName.construct("f"), false, true, false);
        String s = b.toString();
        Assert.assertNotNull(s);
        Assert.assertTrue(s.contains("prop"));
        Assert.assertTrue(s.contains("field(s):"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLinkedExplicitNameCheck() {
        new POJOPropertyBuilder.Linked<AnnotatedField>(_subField, null, null, true, true, false);
    }

    @Test
    public void testLinkedWithMethods() {
        POJOPropertyBuilder.Linked<AnnotatedField> node1 = new POJOPropertyBuilder.Linked<AnnotatedField>(
                _subField, null, PropertyName.construct("f1"), false, true, false);
        Assert.assertSame(node1, node1.withValue(_subField));
        Assert.assertSame(node1, node1.withNext(null));
        Assert.assertSame(node1, node1.withoutNext());

        POJOPropertyBuilder.Linked<AnnotatedField> node2 = new POJOPropertyBuilder.Linked<AnnotatedField>(
                _baseField, node1, PropertyName.construct("f2"), false, true, false);
        Assert.assertNotSame(node2, node2.withoutNext());
        Assert.assertNull(node2.withoutNext().next);

        POJOPropertyBuilder.Linked<AnnotatedField> nodeChangedVal = node1.withValue(_baseField);
        Assert.assertSame(_baseField, nodeChangedVal.value);

        POJOPropertyBuilder.Linked<AnnotatedField> nodeChangedNext = node1.withNext(node2);
        Assert.assertSame(node2, nodeChangedNext.next);
    }
}
