package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

public class CreatorCollectorTest {

    private static class DummyConfig extends ObjectMapper {
        private static final long serialVersionUID = 1L;
    }

    private static class SampleBean {
        public SampleBean() {}
        public SampleBean(String a) {}
        public SampleBean(CharSequence a) {}
        public SampleBean(int a) {}
        public SampleBean(long a) {}
        public SampleBean(double a) {}
        public SampleBean(boolean a) {}
        public SampleBean(List<?> a) {}
        public static SampleBean factory(String a) { return new SampleBean(a); }
        public static SampleBean factory(CharSequence a) { return new SampleBean(a.toString()); }
    }

    private AnnotatedConstructor makeAnnotatedConstructor(Class<?>... paramTypes) throws Exception {
        Constructor<?> ctor = SampleBean.class.getDeclaredConstructor(paramTypes);
        TypeResolutionContext typeRes = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), TypeFactory.defaultInstance().constructType(SampleBean.class).getBindings());
        AnnotationMap annMap = new AnnotationMap();
        AnnotationMap[] paramAnnMaps = new AnnotationMap[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramAnnMaps[i] = new AnnotationMap();
        }
        return new AnnotatedConstructor(typeRes, ctor, annMap, paramAnnMaps);
    }

    private AnnotatedMethod makeAnnotatedMethod(String name, Class<?>... paramTypes) throws Exception {
        Method m = SampleBean.class.getDeclaredMethod(name, paramTypes);
        TypeResolutionContext typeRes = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), TypeFactory.defaultInstance().constructType(SampleBean.class).getBindings());
        AnnotationMap annMap = new AnnotationMap();
        AnnotationMap[] paramAnnMaps = new AnnotationMap[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramAnnMaps[i] = new AnnotationMap();
        }
        return new AnnotatedMethod(typeRes, m, annMap, paramAnnMaps);
    }

    private CreatorCollector createCollector(Class<?> targetClass) {
        ObjectMapper mapper = new DummyConfig();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType type = mapper.getTypeFactory().constructType(targetClass);
        BeanDescription beanDesc = config.introspectClassAnnotations(type);
        return new CreatorCollector(beanDesc, config);
    }

    @Test
    public void testVanillaCollectionInstantiators() throws IOException {
        CreatorCollector coll1 = createCollector(ArrayList.class);
        ValueInstantiator vi1 = coll1.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertTrue(vi1 instanceof CreatorCollector.Vanilla);
        Assert.assertEquals(ArrayList.class.getName(), vi1.getValueTypeDesc());
        Assert.assertTrue(vi1.canInstantiate());
        Assert.assertTrue(vi1.canCreateUsingDefault());
        Object obj1 = vi1.createUsingDefault(null);
        Assert.assertTrue(obj1 instanceof ArrayList);

        CreatorCollector coll2 = createCollector(List.class);
        ValueInstantiator vi2 = coll2.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertTrue(vi2 instanceof CreatorCollector.Vanilla);

        CreatorCollector coll3 = createCollector(Collection.class);
        ValueInstantiator vi3 = coll3.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertTrue(vi3 instanceof CreatorCollector.Vanilla);
    }

    @Test
    public void testVanillaMapInstantiators() throws IOException {
        CreatorCollector coll1 = createCollector(LinkedHashMap.class);
        ValueInstantiator vi1 = coll1.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertTrue(vi1 instanceof CreatorCollector.Vanilla);
        Assert.assertEquals(LinkedHashMap.class.getName(), vi1.getValueTypeDesc());
        Object obj1 = vi1.createUsingDefault(null);
        Assert.assertTrue(obj1 instanceof LinkedHashMap);

        CreatorCollector coll2 = createCollector(Map.class);
        ValueInstantiator vi2 = coll2.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertTrue(vi2 instanceof CreatorCollector.Vanilla);

        CreatorCollector coll3 = createCollector(HashMap.class);
        ValueInstantiator vi3 = coll3.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertTrue(vi3 instanceof CreatorCollector.Vanilla);
        Assert.assertEquals(HashMap.class.getName(), vi3.getValueTypeDesc());
        Object obj3 = vi3.createUsingDefault(null);
        Assert.assertTrue(obj3 instanceof HashMap);
    }

    @Test
    public void testVanillaUnknownType() {
        CreatorCollector.Vanilla vanilla = new CreatorCollector.Vanilla(999);
        Assert.assertEquals(Object.class.getName(), vanilla.getValueTypeDesc());
        try {
            vanilla.createUsingDefault(null);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown type 999"));
        } catch (IOException e) {
            Assert.fail("Unexpected IOException");
        }
    }

    @Test
    public void testDefaultAndScalarCreators() throws Exception {
        CreatorCollector coll = createCollector(SampleBean.class);
        Assert.assertFalse(coll.hasDefaultCreator());
        Assert.assertFalse(coll.hasDelegatingCreator());
        Assert.assertFalse(coll.hasPropertyBasedCreator());

        AnnotatedConstructor ctorDef = makeAnnotatedConstructor();
        coll.setDefaultCreator(ctorDef);
        Assert.assertTrue(coll.hasDefaultCreator());

        AnnotatedConstructor ctorStr = makeAnnotatedConstructor(String.class);
        coll.addStringCreator(ctorStr, true);

        AnnotatedConstructor ctorInt = makeAnnotatedConstructor(int.class);
        coll.addIntCreator(ctorInt, true);

        AnnotatedConstructor ctorLong = makeAnnotatedConstructor(long.class);
        coll.addLongCreator(ctorLong, true);

        AnnotatedConstructor ctorDouble = makeAnnotatedConstructor(double.class);
        coll.addDoubleCreator(ctorDouble, true);

        AnnotatedConstructor ctorBool = makeAnnotatedConstructor(boolean.class);
        coll.addBooleanCreator(ctorBool, true);

        ValueInstantiator vi = coll.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertTrue(vi instanceof StdValueInstantiator);
        Assert.assertTrue(vi.canCreateFromString());
        Assert.assertTrue(vi.canCreateFromInt());
        Assert.assertTrue(vi.canCreateFromLong());
        Assert.assertTrue(vi.canCreateFromDouble());
        Assert.assertTrue(vi.canCreateFromBoolean());
    }

    @Test
    public void testDelegatingCreators() throws Exception {
        CreatorCollector coll = createCollector(SampleBean.class);
        AnnotatedConstructor ctorStr = makeAnnotatedConstructor(String.class);
        coll.addDelegatingCreator(ctorStr, true, null);
        Assert.assertTrue(coll.hasDelegatingCreator());

        ValueInstantiator vi = coll.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertTrue(vi.canCreateUsingDelegate());

        CreatorCollector collArr = createCollector(SampleBean.class);
        AnnotatedConstructor ctorArr = makeAnnotatedConstructor(List.class);
        collArr.addDelegatingCreator(ctorArr, true, new SettableBeanProperty[]{ null });
        ValueInstantiator viArr = collArr.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertTrue(viArr.canCreateUsingArrayDelegate());
    }

    @Test
    public void testPropertyCreatorAndDuplicates() throws Exception {
        CreatorCollector coll = createCollector(SampleBean.class);
        AnnotatedConstructor ctorDef = makeAnnotatedConstructor();
        
        CreatorProperty prop1 = CreatorProperty.construct(PropertyName.construct("prop1"), TypeFactory.defaultInstance().constructType(String.class), null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        CreatorProperty prop2 = CreatorProperty.construct(PropertyName.construct("prop2"), TypeFactory.defaultInstance().constructType(int.class), null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);
        
        coll.addPropertyCreator(ctorDef, true, new SettableBeanProperty[] { prop1, prop2 });
        Assert.assertTrue(coll.hasPropertyBasedCreator());

        CreatorCollector collDup = createCollector(SampleBean.class);
        CreatorProperty propDup1 = CreatorProperty.construct(PropertyName.construct("dup"), TypeFactory.defaultInstance().constructType(String.class), null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        CreatorProperty propDup2 = CreatorProperty.construct(PropertyName.construct("dup"), TypeFactory.defaultInstance().constructType(int.class), null, null, null, null, 1, null, PropertyMetadata.STD_REQUIRED);
        try {
            collDup.addPropertyCreator(ctorDef, true, new SettableBeanProperty[] { propDup1, propDup2 });
            Assert.fail("Expected duplicate property exception");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Duplicate creator property"));
        }

        CreatorCollector collInjectable = createCollector(SampleBean.class);
        CreatorProperty propEmptyInjectable = CreatorProperty.construct(PropertyName.construct(""), TypeFactory.defaultInstance().constructType(String.class), null, null, null, null, 0, "injectId", PropertyMetadata.STD_REQUIRED);
        collInjectable.addPropertyCreator(ctorDef, true, new SettableBeanProperty[] { propEmptyInjectable, prop1 });
        Assert.assertTrue(collInjectable.hasPropertyBasedCreator());
    }

    @Test
    public void testIncompleteParameter() throws Exception {
        CreatorCollector coll = createCollector(SampleBean.class);
        AnnotatedConstructor ctorStr = makeAnnotatedConstructor(String.class);
        AnnotatedParameter param = new AnnotatedParameter(ctorStr, TypeFactory.defaultInstance().constructType(String.class), null, null, 0);
        coll.addIncompeteParameter(param);
        AnnotatedParameter param2 = new AnnotatedParameter(ctorStr, TypeFactory.defaultInstance().constructType(String.class), null, null, 1);
        coll.addIncompeteParameter(param2);
        ValueInstantiator vi = coll.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertNotNull(vi);
    }

    @Test
    public void testDuplicateCreatorVerifications() throws Exception {
        CreatorCollector coll = createCollector(SampleBean.class);
        AnnotatedConstructor ctorStr = makeAnnotatedConstructor(String.class);
        AnnotatedConstructor ctorCharSequence = makeAnnotatedConstructor(CharSequence.class);

        coll.addStringCreator(ctorCharSequence, true);
        coll.addStringCreator(ctorStr, true);

        CreatorCollector coll2 = createCollector(SampleBean.class);
        coll2.addStringCreator(ctorStr, true);
        coll2.addStringCreator(ctorCharSequence, true);

        CreatorCollector coll3 = createCollector(SampleBean.class);
        coll3.addStringCreator(ctorStr, true);
        coll3.addStringCreator(ctorCharSequence, false);

        CreatorCollector coll4 = createCollector(SampleBean.class);
        coll4.addStringCreator(ctorStr, false);
        coll4.addStringCreator(ctorCharSequence, true);

        CreatorCollector collConflict = createCollector(SampleBean.class);
        collConflict.addStringCreator(ctorStr, true);
        try {
            collConflict.addStringCreator(ctorStr, true);
            Assert.fail("Expected Conflicting creators exception");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Conflicting String creators"));
        }

        CreatorCollector collDiffType = createCollector(SampleBean.class);
        AnnotatedMethod methStr = makeAnnotatedMethod("factory", String.class);
        collDiffType.addStringCreator(ctorStr, true);
        collDiffType.addStringCreator(methStr, true);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedMethods() throws Exception {
        CreatorCollector coll = createCollector(SampleBean.class);
        AnnotatedConstructor ctorStr = makeAnnotatedConstructor(String.class);
        AnnotatedConstructor ctorInt = makeAnnotatedConstructor(int.class);
        AnnotatedConstructor ctorLong = makeAnnotatedConstructor(long.class);
        AnnotatedConstructor ctorDouble = makeAnnotatedConstructor(double.class);
        AnnotatedConstructor ctorBool = makeAnnotatedConstructor(boolean.class);

        coll.addStringCreator(ctorStr);
        coll.addIntCreator(ctorInt);
        coll.addLongCreator(ctorLong);
        coll.addDoubleCreator(ctorDouble);
        coll.addBooleanCreator(ctorBool);

        CreatorProperty prop = CreatorProperty.construct(PropertyName.construct("p"), TypeFactory.defaultInstance().constructType(String.class), null, null, null, null, 0, null, PropertyMetadata.STD_REQUIRED);
        coll.addDelegatingCreator(ctorStr, new CreatorProperty[] { prop });
        coll.addPropertyCreator(ctorStr, new CreatorProperty[] { prop });

        ValueInstantiator vi = coll.constructValueInstantiator(new DummyConfig().getDeserializationConfig());
        Assert.assertNotNull(vi);
    }
}
