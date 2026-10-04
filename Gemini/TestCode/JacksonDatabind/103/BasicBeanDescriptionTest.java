package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class BasicBeanDescriptionTest {

    static class DummyTarget {
        public String propA;
        public String propB;

        public DummyTarget() {}

        public DummyTarget(String val) {}

        public DummyTarget(int count) {}

        public static DummyTarget valueOf(String text) {
            return new DummyTarget(text);
        }

        public static DummyTarget valueOf(String a, String b) {
            return new DummyTarget(a);
        }

        public static DummyTarget fromString(CharSequence cs) {
            return new DummyTarget(cs.toString());
        }

        public static DummyTarget fromString(int val) {
            return new DummyTarget(val);
        }

        public static DummyTarget customCreator(String s) {
            return new DummyTarget(s);
        }

        public static String notAFactory(String s) {
            return s;
        }

        public void anySetter(String key, Object value) {}

        public void badAnySetter(int key, Object value) {}

        public Map<String, Object> anyMap;

        public String badAnyField;
    }

    static class DummyConverter extends StdConverter<String, Integer> {
        @Override
        public Integer convert(String value) {
            return Integer.parseInt(value);
        }
    }

    static class UninstantiableBean {
        public UninstantiableBean() {
            throw new RuntimeException("Instantiation failed");
        }
    }

    static class AbstractBean {
        protected AbstractBean() {}
    }

    private ObjectMapper _mapper;
    private SerializationConfig _config;
    private JavaType _type;
    private AnnotatedClass _classDef;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getSerializationConfig();
        _type = _mapper.constructType(DummyTarget.class);
        _classDef = AnnotatedClassResolver.resolve(_config, _type, _config);
    }

    @Test
    public void testFactoryMethodsConstructors() {
        POJOPropertiesCollector coll = new POJOPropertiesCollector(_config, true, _type, _classDef, null);
        BasicBeanDescription desc1 = BasicBeanDescription.forDeserialization(coll);
        BasicBeanDescription desc2 = BasicBeanDescription.forSerialization(coll);
        BasicBeanDescription desc3 = BasicBeanDescription.forOtherUse(_config, _type, _classDef);

        Assert.assertNotNull(desc1);
        Assert.assertNotNull(desc2);
        Assert.assertNotNull(desc3);
        Assert.assertEquals(DummyTarget.class, desc1.getBeanClass());
        Assert.assertEquals(DummyTarget.class, desc3.getBeanClass());
    }

    @Test
    public void testNullConfigConstructor() {
        BasicBeanDescription desc = new BasicBeanDescription(null, _type, _classDef, new ArrayList<BeanPropertyDefinition>());
        Assert.assertNull(desc._annotationIntrospector);
        Assert.assertNull(desc.findSerializationConverter());
        Assert.assertNull(desc.findDeserializationConverter());
        Assert.assertNull(desc.findPOJOBuilder());
        Assert.assertNull(desc.findPOJOBuilderConfig());
        Assert.assertNull(desc.findClassDescription());
    }

    @Test
    public void testBasicPropertiesAndAccessors() {
        POJOPropertiesCollector coll = new POJOPropertiesCollector(_config, true, _type, _classDef, null);
        coll.collect();
        BasicBeanDescription desc = BasicBeanDescription.forDeserialization(coll);

        Assert.assertEquals(_classDef, desc.getClassInfo());
        Assert.assertNull(desc.getObjectIdInfo());
        Assert.assertNotNull(desc.findProperties());
        Assert.assertNotNull(desc.getConstructors());
        Assert.assertNotNull(desc.getIgnoredPropertyNames());
        Assert.assertEquals(desc.hasKnownClassAnnotations(), _classDef.hasAnnotations());
        Assert.assertNotNull(desc.getClassAnnotations());
        Assert.assertNotNull(desc.bindingsForBeanType());
        Assert.assertNull(desc.resolveType(null));
        Assert.assertNotNull(desc.resolveType(String.class));
        Assert.assertNotNull(desc.findDefaultConstructor());
        Assert.assertNotNull(desc.findMethod("valueOf", new Class<?>[]{String.class}));
    }

    @Test
    public void testAddRemoveFindProperty() {
        List<BeanPropertyDefinition> props = new ArrayList<BeanPropertyDefinition>();
        BasicBeanDescription desc = new BasicBeanDescription(_config, _type, _classDef, props);

        AnnotatedField fieldA = null;
        for (AnnotatedField f : _classDef.fields()) {
            if ("propA".equals(f.getName())) {
                fieldA = f;
                break;
            }
        }
        POJOPropertyBuilder propA = new POJOPropertyBuilder(_config, _config.getAnnotationIntrospector(), true, PropertyName.construct("propA"));
        propA.addField(fieldA, PropertyName.construct("propA"), false, true, false);

        Assert.assertTrue(desc.addProperty(propA));
        Assert.assertFalse(desc.addProperty(propA));
        Assert.assertTrue(desc.hasProperty(PropertyName.construct("propA")));
        Assert.assertNotNull(desc.findProperty(PropertyName.construct("propA")));
        Assert.assertNull(desc.findProperty(PropertyName.construct("nonExistent")));

        Assert.assertTrue(desc.removeProperty("propA"));
        Assert.assertFalse(desc.removeProperty("propA"));
        Assert.assertFalse(desc.hasProperty(PropertyName.construct("propA")));
    }

    @Test
    public void testJsonValueAndAccessorsNullCollector() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        Assert.assertNull(desc.findJsonValueMethod());
        Assert.assertNull(desc.findJsonValueAccessor());
        Assert.assertEquals(0, desc.getIgnoredPropertyNames().size());
        Assert.assertNull(desc.findAnySetterAccessor());
        Assert.assertEquals(0, desc.findInjectables().size());
        Assert.assertNull(desc.findAnyGetter());
    }

    @Test
    public void testFindSingleArgConstructor() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        Constructor<?> ctorString = desc.findSingleArgConstructor(String.class);
        Assert.assertNotNull(ctorString);
        Constructor<?> ctorInt = desc.findSingleArgConstructor(int.class);
        Assert.assertNotNull(ctorInt);
        Constructor<?> ctorNone = desc.findSingleArgConstructor(Double.class);
        Assert.assertNull(ctorNone);
    }

    @Test
    public void testFactoryMethodResolution() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        List<AnnotatedMethod> factories = desc.getFactoryMethods();
        Assert.assertNotNull(factories);
        Assert.assertTrue(factories.size() >= 2);

        Method mString = desc.findFactoryMethod(String.class);
        Assert.assertNotNull(mString);
        Assert.assertEquals("valueOf", mString.getName());

        Method mCharSequence = desc.findFactoryMethod(CharSequence.class);
        Assert.assertNotNull(mCharSequence);
        Assert.assertEquals("fromString", mCharSequence.getName());

        Method mDouble = desc.findFactoryMethod(Double.class);
        Assert.assertNull(mDouble);
    }

    @Test
    public void testIsFactoryMethodBranches() throws Exception {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);

        Method mNotFactory = DummyTarget.class.getDeclaredMethod("notAFactory", String.class);
        AnnotatedMethod amNotFactory = new AnnotatedMethod(null, mNotFactory, null, null);
        Assert.assertFalse(desc.isFactoryMethod(amNotFactory));

        Method mMultiValueOf = DummyTarget.class.getDeclaredMethod("valueOf", String.class, String.class);
        AnnotatedMethod amMultiValueOf = new AnnotatedMethod(null, mMultiValueOf, null, null);
        Assert.assertFalse(desc.isFactoryMethod(amMultiValueOf));

        Method mIntFromString = DummyTarget.class.getDeclaredMethod("fromString", int.class);
        AnnotatedMethod amIntFromString = new AnnotatedMethod(null, mIntFromString, null, null);
        Assert.assertFalse(desc.isFactoryMethod(amIntFromString));

        Method mValidValueOf = DummyTarget.class.getDeclaredMethod("valueOf", String.class);
        AnnotatedMethod amValidValueOf = new AnnotatedMethod(null, mValidValueOf, null, null);
        Assert.assertTrue(desc.isFactoryMethod(amValidValueOf));

        Method mValidFromString = DummyTarget.class.getDeclaredMethod("fromString", CharSequence.class);
        AnnotatedMethod amValidFromString = new AnnotatedMethod(null, mValidFromString, null, null);
        Assert.assertTrue(desc.isFactoryMethod(amValidFromString));
    }

    @Test
    public void testInstantiateBeanSuccess() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        Object obj = desc.instantiateBean(true);
        Assert.assertNotNull(obj);
        Assert.assertTrue(obj instanceof DummyTarget);
    }

    @Test
    public void testInstantiateBeanNoDefaultConstructor() {
        JavaType jt = _mapper.constructType(String.class);
        AnnotatedClass ac = AnnotatedClassResolver.resolve(_config, jt, _config);
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, jt, ac);
        Assert.assertNull(desc.instantiateBean(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiateBeanException() {
        JavaType jt = _mapper.constructType(UninstantiableBean.class);
        AnnotatedClass ac = AnnotatedClassResolver.resolve(_config, jt, _config);
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, jt, ac);
        desc.instantiateBean(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInstantiateBeanAbstract() {
        JavaType jt = _mapper.constructType(AbstractBean.class);
        AnnotatedClass ac = AnnotatedClassResolver.resolve(_config, jt, _config);
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, jt, ac);
        desc.instantiateBean(true);
    }

    @Test
    public void testFindExpectedFormatAndPropertyInclusion() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        JsonFormat.Value format = desc.findExpectedFormat(JsonFormat.Value.empty());
        Assert.assertNotNull(format);

        JsonFormat.Value formatNull = desc.findExpectedFormat(null);
        Assert.assertNotNull(formatNull);

        JsonInclude.Value incl = desc.findPropertyInclusion(JsonInclude.Value.empty());
        Assert.assertNotNull(incl);
    }

    @Test
    public void testFindDefaultViews() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        Class<?>[] views = desc.findDefaultViews();
        Assert.assertNull(views);

        SerializationConfig disabledViewConfig = _config.without(MapperFeature.DEFAULT_VIEW_INCLUSION);
        BasicBeanDescription descNoViews = BasicBeanDescription.forOtherUse(disabledViewConfig, _type, _classDef);
        Class<?>[] noViews = descNoViews.findDefaultViews();
        Assert.assertNotNull(noViews);
        Assert.assertEquals(0, noViews.length);
    }

    @Test
    public void testFindBackReferences() {
        List<BeanPropertyDefinition> props = new ArrayList<BeanPropertyDefinition>();
        POJOPropertyBuilder propA = new POJOPropertyBuilder(_config, _config.getAnnotationIntrospector(), true, PropertyName.construct("propA"));
        props.add(propA);
        BasicBeanDescription desc = new BasicBeanDescription(_config, _type, _classDef, props);

        Assert.assertNull(desc.findBackReferences());
        Assert.assertNull(desc.findBackReferenceProperties());
    }

    @Test
    public void testFindPropertyFields() {
        POJOPropertiesCollector coll = new POJOPropertiesCollector(_config, true, _type, _classDef, null);
        coll.collect();
        BasicBeanDescription desc = BasicBeanDescription.forDeserialization(coll);

        Set<String> ignored = new HashSet<String>();
        ignored.add("propB");

        LinkedHashMap<String, AnnotatedField> fields = desc._findPropertyFields(ignored, true);
        Assert.assertNotNull(fields);
        Assert.assertFalse(fields.containsKey("propB"));
    }

    @Test
    public void testCreateConverter() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        Assert.assertNull(desc._createConverter(null));
        Assert.assertNull(desc._createConverter(Converter.None.class));

        DummyConverter convInstance = new DummyConverter();
        Assert.assertSame(convInstance, desc._createConverter(convInstance));

        Converter<Object, Object> created = desc._createConverter(DummyConverter.class);
        Assert.assertNotNull(created);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateConverterInvalidDef() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        desc._createConverter("invalidStringConverter");
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateConverterInvalidClass() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        desc._createConverter(String.class);
    }

    @Test
    public void testFindCreatorPropertyName() {
        BasicBeanDescription desc = BasicBeanDescription.forOtherUse(_config, _type, _classDef);
        AnnotatedConstructor ctor = _classDef.getConstructors().get(0);
        if (ctor.getParameterCount() > 0) {
            AnnotatedParameter param = ctor.getParameter(0);
            PropertyName pn = desc._findCreatorPropertyName(param);
            Assert.assertNull(pn);
        }
    }
}
