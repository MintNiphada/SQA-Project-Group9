package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.CreatorProperty;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CreatorCollectorTest {

    private ObjectMapper _mapper;
    private DeserializationConfig _config;

    @Before
    public void setUp() {
        _mapper = new ObjectMapper();
        _config = _mapper.getDeserializationConfig();
    }

    private static class DummyClass {
        public DummyClass() {}
        public DummyClass(String s) {}
        public DummyClass(int i) {}
        public DummyClass(long l) {}
        public DummyClass(double d) {}
        public DummyClass(boolean b) {}
        public DummyClass(String s, int i) {}

        public static DummyClass create(String s) { return new DummyClass(s); }
    }

    private AnnotatedConstructor makeAnnotatedConstructor(Class<?>... paramTypes) {
        try {
            Constructor<?> ctor = DummyClass.class.getDeclaredConstructor(paramTypes);
            AnnotationMap[] paramAnns = new AnnotationMap[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramAnns[i] = new AnnotationMap();
            }
            return new AnnotatedConstructor(ctor, new AnnotationMap(), paramAnns);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AnnotatedMethod makeAnnotatedMethod(String name, Class<?>... paramTypes) {
        try {
            Method m = DummyClass.class.getDeclaredMethod(name, paramTypes);
            AnnotationMap[] paramAnns = new AnnotationMap[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                paramAnns[i] = new AnnotationMap();
            }
            return new AnnotatedMethod(m, new AnnotationMap(), paramAnns);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private CreatorProperty makeCreatorProperty(String name, int index, Object injectableValueId) {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        PropertyName propName = new PropertyName(name);
        AnnotatedConstructor ctor = makeAnnotatedConstructor(String.class);
        AnnotatedParameter param = new AnnotatedParameter(ctor, String.class, new AnnotationMap(), index);
        return new CreatorProperty(propName, type, null, null, new AnnotationMap(), param, index, injectableValueId, true);
    }

    @Test
    public void testVanillaInstantiatorsForWellKnownTypes() {
        Class<?>[] collectionClasses = new Class<?>[] { Collection.class, List.class, ArrayList.class };
        for (Class<?> cls : collectionClasses) {
            JavaType type = _mapper.constructType(cls);
            BeanDescription beanDesc = _config.introspect(type);
            CreatorCollector coll = new CreatorCollector(beanDesc, true);
            ValueInstantiator vi = coll.constructValueInstantiator(_config);
            Assert.assertTrue(vi instanceof CreatorCollector.Vanilla);
            Assert.assertEquals(ArrayList.class.getName(), vi.getValueTypeDesc());
            Assert.assertTrue(vi.canInstantiate());
            Assert.assertTrue(vi.canCreateUsingDefault());
            try {
                Object obj = vi.createUsingDefault(null);
                Assert.assertTrue(obj instanceof ArrayList);
            } catch (IOException e) {
                Assert.fail("Unexpected exception: " + e.getMessage());
            }
        }

        Class<?>[] mapClasses = new Class<?>[] { Map.class, LinkedHashMap.class };
        for (Class<?> cls : mapClasses) {
            JavaType type = _mapper.constructType(cls);
            BeanDescription beanDesc = _config.introspect(type);
            CreatorCollector coll = new CreatorCollector(beanDesc, true);
            ValueInstantiator vi = coll.constructValueInstantiator(_config);
            Assert.assertTrue(vi instanceof CreatorCollector.Vanilla);
            Assert.assertEquals(LinkedHashMap.class.getName(), vi.getValueTypeDesc());
            try {
                Object obj = vi.createUsingDefault(null);
                Assert.assertTrue(obj instanceof LinkedHashMap);
            } catch (IOException e) {
                Assert.fail("Unexpected exception: " + e.getMessage());
            }
        }

        JavaType typeHashMap = _mapper.constructType(HashMap.class);
        BeanDescription descHashMap = _config.introspect(typeHashMap);
        CreatorCollector collHashMap = new CreatorCollector(descHashMap, true);
        ValueInstantiator viHashMap = collHashMap.constructValueInstantiator(_config);
        Assert.assertTrue(viHashMap instanceof CreatorCollector.Vanilla);
        Assert.assertEquals(HashMap.class.getName(), viHashMap.getValueTypeDesc());
        try {
            Object obj = viHashMap.createUsingDefault(null);
            Assert.assertTrue(obj instanceof HashMap);
        } catch (IOException e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testVanillaHelperClassDirectly() {
        CreatorCollector.Vanilla vColl = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_COLLECTION);
        Assert.assertEquals(ArrayList.class.getName(), vColl.getValueTypeDesc());

        CreatorCollector.Vanilla vMap = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_MAP);
        Assert.assertEquals(LinkedHashMap.class.getName(), vMap.getValueTypeDesc());

        CreatorCollector.Vanilla vHashMap = new CreatorCollector.Vanilla(CreatorCollector.Vanilla.TYPE_HASH_MAP);
        Assert.assertEquals(HashMap.class.getName(), vHashMap.getValueTypeDesc());

        CreatorCollector.Vanilla vUnknown = new CreatorCollector.Vanilla(999);
        Assert.assertEquals(Object.class.getName(), vUnknown.getValueTypeDesc());
        try {
            vUnknown.createUsingDefault(null);
            Assert.fail("Should throw IllegalStateException for unknown type");
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown type 999"));
        } catch (IOException e) {
            Assert.fail("Wrong exception type: " + e.getMessage());
        }
    }

    @Test
    public void testDefaultCreatorAndHasDefaultCreator() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        Assert.assertFalse(coll.hasDefaultCreator());

        AnnotatedConstructor ctor = makeAnnotatedConstructor();
        coll.setDefaultCreator(ctor);
        Assert.assertTrue(coll.hasDefaultCreator());

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        Assert.assertTrue(vi instanceof StdValueInstantiator);
        Assert.assertTrue(vi.canCreateUsingDefault());
    }

    @Test
    public void testScalarCreators() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, false);

        AnnotatedConstructor strCtor = makeAnnotatedConstructor(String.class);
        AnnotatedConstructor intCtor = makeAnnotatedConstructor(int.class);
        AnnotatedConstructor longCtor = makeAnnotatedConstructor(long.class);
        AnnotatedConstructor dblCtor = makeAnnotatedConstructor(double.class);
        AnnotatedConstructor boolCtor = makeAnnotatedConstructor(boolean.class);

        coll.addStringCreator(strCtor, true);
        coll.addIntCreator(intCtor, true);
        coll.addLongCreator(longCtor, true);
        coll.addDoubleCreator(dblCtor, true);
        coll.addBooleanCreator(boolCtor, true);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        Assert.assertTrue(vi instanceof StdValueInstantiator);
        Assert.assertTrue(vi.canCreateFromString());
        Assert.assertTrue(vi.canCreateFromInt());
        Assert.assertTrue(vi.canCreateFromLong());
        Assert.assertTrue(vi.canCreateFromDouble());
        Assert.assertTrue(vi.canCreateFromBoolean());
    }

    @Test
    public void testDelegatingCreatorWithArgs() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        AnnotatedConstructor ctor = makeAnnotatedConstructor(String.class, int.class);
        CreatorProperty prop0 = null;
        CreatorProperty prop1 = makeCreatorProperty("injectMe", 1, "id123");
        CreatorProperty[] delegates = new CreatorProperty[] { prop0, prop1 };

        coll.addDelegatingCreator(ctor, true, delegates);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        Assert.assertTrue(vi instanceof StdValueInstantiator);
        Assert.assertTrue(vi.canCreateUsingDelegate());
        Assert.assertNotNull(vi.getDelegateType(_config));
    }

    @Test
    public void testDelegatingCreatorWithoutArgs() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        AnnotatedConstructor ctor = makeAnnotatedConstructor(String.class);
        coll.addDelegatingCreator(ctor, true, null);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        Assert.assertTrue(vi instanceof StdValueInstantiator);
        Assert.assertTrue(vi.canCreateUsingDelegate());
        Assert.assertEquals(String.class, vi.getDelegateType(_config).getRawClass());
    }

    @Test
    public void testPropertyCreatorSuccess() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        AnnotatedConstructor ctor = makeAnnotatedConstructor(String.class, int.class);
        CreatorProperty prop0 = makeCreatorProperty("propA", 0, null);
        CreatorProperty prop1 = makeCreatorProperty("propB", 1, null);
        CreatorProperty[] props = new CreatorProperty[] { prop0, prop1 };

        coll.addPropertyCreator(ctor, true, props);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        Assert.assertTrue(vi instanceof StdValueInstantiator);
        Assert.assertTrue(vi.canCreateFromObjectWith());
        Assert.assertEquals(2, vi.getFromObjectArguments(_config).length);
    }

    @Test
    public void testPropertyCreatorWithInjectableEmptyName() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        AnnotatedConstructor ctor = makeAnnotatedConstructor(String.class, int.class);
        CreatorProperty prop0 = makeCreatorProperty("", 0, "injectId");
        CreatorProperty prop1 = makeCreatorProperty("propB", 1, null);
        CreatorProperty[] props = new CreatorProperty[] { prop0, prop1 };

        coll.addPropertyCreator(ctor, true, props);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        Assert.assertTrue(vi.canCreateFromObjectWith());
    }

    @Test
    public void testPropertyCreatorDuplicateNameThrowsException() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        AnnotatedConstructor ctor = makeAnnotatedConstructor(String.class, int.class);
        CreatorProperty prop0 = makeCreatorProperty("dupName", 0, null);
        CreatorProperty prop1 = makeCreatorProperty("dupName", 1, null);
        CreatorProperty[] props = new CreatorProperty[] { prop0, prop1 };

        try {
            coll.addPropertyCreator(ctor, true, props);
            Assert.fail("Should throw IllegalArgumentException on duplicate properties");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Duplicate creator property \"dupName\""));
        }
    }

    @Test
    public void testAddIncompleteParameter() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        AnnotatedConstructor ctor = makeAnnotatedConstructor(String.class);
        AnnotatedParameter param1 = new AnnotatedParameter(ctor, String.class, new AnnotationMap(), 0);
        AnnotatedParameter param2 = new AnnotatedParameter(ctor, String.class, new AnnotationMap(), 1);

        coll.addIncompeteParameter(param1);
        coll.addIncompeteParameter(param2); // should not overwrite existing _incompleteParameter

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        Assert.assertTrue(vi instanceof StdValueInstantiator);
        Assert.assertSame(param1, vi.getIncompleteParameter());
    }

    @Test
    public void testVerifyNonDupConflictingExplicitThrowsException() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        AnnotatedConstructor ctor1 = makeAnnotatedConstructor(String.class);
        AnnotatedConstructor ctor2 = makeAnnotatedConstructor(String.class);

        coll.addStringCreator(ctor1, true);
        try {
            coll.addStringCreator(ctor2, true);
            Assert.fail("Should throw IllegalArgumentException for conflicting explicit creators");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("Conflicting String creators"));
        }
    }

    @Test
    public void testVerifyNonDupExplicitPreservedWhenNewIsImplicit() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        AnnotatedConstructor ctorExplicit = makeAnnotatedConstructor(String.class);
        AnnotatedConstructor ctorImplicit = makeAnnotatedConstructor(String.class);

        coll.addStringCreator(ctorExplicit, true);
        // Adding non-explicit when explicit exists should be silently ignored (leaves explicit as-is)
        coll.addStringCreator(ctorImplicit, false);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        Assert.assertTrue(vi instanceof StdValueInstantiator);
    }

    @Test
    public void testVerifyNonDupDifferentClassesAllowsOverride() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        AnnotatedConstructor ctor = makeAnnotatedConstructor(String.class);
        AnnotatedMethod method = makeAnnotatedMethod("create", String.class);

        coll.addStringCreator(ctor, true);
        // ctor and method have different classes (AnnotatedConstructor vs AnnotatedMethod)
        coll.addStringCreator(method, true);

        ValueInstantiator vi = coll.constructValueInstantiator(_config);
        Assert.assertTrue(vi instanceof StdValueInstantiator);
    }

    @Test
    public void testDeprecatedMethods() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, false);

        AnnotatedConstructor strCtor = makeAnnotatedConstructor(String.class);
        AnnotatedConstructor intCtor = makeAnnotatedConstructor(int.class);
        AnnotatedConstructor longCtor = makeAnnotatedConstructor(long.class);
        AnnotatedConstructor dblCtor = makeAnnotatedConstructor(double.class);
        AnnotatedConstructor boolCtor = makeAnnotatedConstructor(boolean.class);

        coll.addStringCreator(strCtor);
        coll.addIntCreator(intCtor);
        coll.addLongCreator(longCtor);
        coll.addDoubleCreator(dblCtor);
        coll.addBooleanCreator(boolCtor);

        AnnotatedConstructor delCtor = makeAnnotatedConstructor(String.class);
        coll.addDelegatingCreator(delCtor, new CreatorProperty[0]);

        AnnotatedConstructor propCtor = makeAnnotatedConstructor(String.class);
        CreatorProperty prop = makeCreatorProperty("singleProp", 0, null);
        coll.addPropertyCreator(propCtor, new CreatorProperty[] { prop });

        AnnotatedWithParams old = coll.verifyNonDup(strCtor, CreatorCollector.C_STRING);
        Assert.assertNotNull(old);
    }

    @Test
    public void testFixAccessWithNullMember() {
        JavaType type = _mapper.constructType(DummyClass.class);
        BeanDescription beanDesc = _config.introspect(type);
        CreatorCollector coll = new CreatorCollector(beanDesc, true);

        coll.setDefaultCreator(null);
        Assert.assertFalse(coll.hasDefaultCreator());
    }
}
