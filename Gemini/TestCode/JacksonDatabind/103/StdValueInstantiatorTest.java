package com.fasterxml.jackson.databind.deser.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class StdValueInstantiatorTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;
    private JavaType stringType;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        stringType = TypeFactory.defaultInstance().constructType(String.class);
    }

    @Test
    public void testConstructorsAndMetadata() {
        StdValueInstantiator vi1 = new StdValueInstantiator((DeserializationConfig) null, String.class);
        Assert.assertEquals("java.lang.String", vi1.getValueTypeDesc());
        Assert.assertEquals(String.class, vi1.getValueClass());

        StdValueInstantiator viNullClass = new StdValueInstantiator((DeserializationConfig) null, (Class<?>) null);
        Assert.assertEquals("UNKNOWN", viNullClass.getValueTypeDesc());
        Assert.assertEquals(Object.class, viNullClass.getValueClass());

        StdValueInstantiator vi2 = new StdValueInstantiator((DeserializationConfig) null, stringType);
        Assert.assertEquals(stringType.toString(), vi2.getValueTypeDesc());
        Assert.assertEquals(String.class, vi2.getValueClass());

        StdValueInstantiator viNullType = new StdValueInstantiator((DeserializationConfig) null, (JavaType) null);
        Assert.assertEquals("UNKNOWN TYPE", viNullType.getValueTypeDesc());
        Assert.assertEquals(Object.class, viNullType.getValueClass());
    }

    @Test
    public void testCopyConstructorAndConfiguration() {
        StdValueInstantiator src = new StdValueInstantiator((DeserializationConfig) null, stringType);
        AnnotatedWithParams defaultCreator = Mockito.mock(AnnotatedWithParams.class);
        AnnotatedWithParams delegateCreator = Mockito.mock(AnnotatedWithParams.class);
        AnnotatedWithParams withArgsCreator = Mockito.mock(AnnotatedWithParams.class);
        AnnotatedWithParams arrayDelegateCreator = Mockito.mock(AnnotatedWithParams.class);
        AnnotatedWithParams fromStringCreator = Mockito.mock(AnnotatedWithParams.class);
        AnnotatedWithParams fromIntCreator = Mockito.mock(AnnotatedWithParams.class);
        AnnotatedWithParams fromLongCreator = Mockito.mock(AnnotatedWithParams.class);
        AnnotatedWithParams fromDoubleCreator = Mockito.mock(AnnotatedWithParams.class);
        AnnotatedWithParams fromBooleanCreator = Mockito.mock(AnnotatedWithParams.class);
        AnnotatedParameter incompleteParam = Mockito.mock(AnnotatedParameter.class);
        SettableBeanProperty[] delegateArgs = new SettableBeanProperty[0];
        SettableBeanProperty[] constructorArgs = new SettableBeanProperty[0];
        SettableBeanProperty[] arrayDelegateArgs = new SettableBeanProperty[0];

        src.configureFromObjectSettings(defaultCreator, delegateCreator, stringType, delegateArgs, withArgsCreator, constructorArgs);
        src.configureFromArraySettings(arrayDelegateCreator, stringType, arrayDelegateArgs);
        src.configureFromStringCreator(fromStringCreator);
        src.configureFromIntCreator(fromIntCreator);
        src.configureFromLongCreator(fromLongCreator);
        src.configureFromDoubleCreator(fromDoubleCreator);
        src.configureFromBooleanCreator(fromBooleanCreator);
        src.configureIncompleteParameter(incompleteParam);

        Assert.assertTrue(src.canInstantiate());
        Assert.assertTrue(src.canCreateUsingDefault());
        Assert.assertTrue(src.canCreateUsingDelegate());
        Assert.assertTrue(src.canCreateUsingArrayDelegate());
        Assert.assertTrue(src.canCreateFromObjectWith());
        Assert.assertTrue(src.canCreateFromString());
        Assert.assertTrue(src.canCreateFromInt());
        Assert.assertTrue(src.canCreateFromLong());
        Assert.assertTrue(src.canCreateFromDouble());
        Assert.assertTrue(src.canCreateFromBoolean());

        Assert.assertSame(defaultCreator, src.getDefaultCreator());
        Assert.assertSame(delegateCreator, src.getDelegateCreator());
        Assert.assertSame(arrayDelegateCreator, src.getArrayDelegateCreator());
        Assert.assertSame(withArgsCreator, src.getWithArgsCreator());
        Assert.assertSame(incompleteParam, src.getIncompleteParameter());
        Assert.assertSame(stringType, src.getDelegateType(null));
        Assert.assertSame(stringType, src.getArrayDelegateType(null));
        Assert.assertSame(constructorArgs, src.getFromObjectArguments(null));

        StdValueInstantiator copy = new StdValueInstantiator(src);
        Assert.assertEquals(src.getValueTypeDesc(), copy.getValueTypeDesc());
        Assert.assertEquals(src.getValueClass(), copy.getValueClass());
        Assert.assertSame(src.getDefaultCreator(), copy.getDefaultCreator());
        Assert.assertSame(src.getDelegateCreator(), copy.getDelegateCreator());
        Assert.assertSame(src.getArrayDelegateCreator(), copy.getArrayDelegateCreator());
        Assert.assertSame(src.getWithArgsCreator(), copy.getWithArgsCreator());
        Assert.assertSame(src.getDelegateType(null), copy.getDelegateType(null));
        Assert.assertSame(src.getArrayDelegateType(null), copy.getArrayDelegateType(null));
        Assert.assertSame(src.getFromObjectArguments(null), copy.getFromObjectArguments(null));
        Assert.assertTrue(copy.canCreateFromString());
        Assert.assertTrue(copy.canCreateFromInt());
        Assert.assertTrue(copy.canCreateFromLong());
        Assert.assertTrue(copy.canCreateFromDouble());
        Assert.assertTrue(copy.canCreateFromBoolean());
    }

    @Test
    public void testCanInstantiateFalse() {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        Assert.assertFalse(vi.canInstantiate());
        Assert.assertFalse(vi.canCreateUsingDefault());
        Assert.assertFalse(vi.canCreateUsingDelegate());
        Assert.assertFalse(vi.canCreateUsingArrayDelegate());
        Assert.assertFalse(vi.canCreateFromObjectWith());
        Assert.assertFalse(vi.canCreateFromString());
        Assert.assertFalse(vi.canCreateFromInt());
        Assert.assertFalse(vi.canCreateFromLong());
        Assert.assertFalse(vi.canCreateFromDouble());
        Assert.assertFalse(vi.canCreateFromBoolean());
    }

    @Test
    public void testCreateUsingDefault() throws Exception {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        try {
            vi.createUsingDefault(ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {
        }

        AnnotatedWithParams creator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(creator.call()).thenReturn("defaultResult");
        vi.configureFromObjectSettings(creator, null, null, null, null, null);
        Assert.assertEquals("defaultResult", vi.createUsingDefault(ctxt));

        Mockito.when(creator.call()).thenThrow(new IllegalStateException("err"));
        try {
            vi.createUsingDefault(ctxt);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("err"));
        }
    }

    @Test
    public void testCreateFromObjectWith() throws Exception {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        Object[] args = new Object[]{"val"};
        try {
            vi.createFromObjectWith(ctxt, args);
            Assert.fail();
        } catch (JsonMappingException e) {
        }

        AnnotatedWithParams creator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(creator.call(args)).thenReturn("withArgsResult");
        vi.configureFromObjectSettings(null, null, null, null, creator, null);
        Assert.assertEquals("withArgsResult", vi.createFromObjectWith(ctxt, args));

        Mockito.when(creator.call(args)).thenThrow(new IllegalArgumentException("err"));
        try {
            vi.createFromObjectWith(ctxt, args);
            Assert.fail();
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("err"));
        }
    }

    @Test
    public void testCreateUsingDelegate() throws Exception {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        AnnotatedWithParams delegateCreator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(delegateCreator.call1("del")).thenReturn("delegatedResult");

        vi.configureFromObjectSettings(null, delegateCreator, stringType, null, null, null);
        Assert.assertEquals("delegatedResult", vi.createUsingDelegate(ctxt, "del"));

        SettableBeanProperty prop0 = null;
        SettableBeanProperty prop1 = Mockito.mock(SettableBeanProperty.class);
        Mockito.when(prop1.getInjectableValueId()).thenReturn("id1");
        DeserializationContext mockCtxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(mockCtxt.findInjectableValue("id1", prop1, null)).thenReturn("injected1");

        SettableBeanProperty[] delegateArgs = new SettableBeanProperty[]{prop0, prop1};
        Mockito.when(delegateCreator.call(Mockito.any(Object[].class))).thenReturn("delegatedWithInject");
        vi.configureFromObjectSettings(null, delegateCreator, stringType, delegateArgs, null, null);
        Assert.assertEquals("delegatedWithInject", vi.createUsingDelegate(mockCtxt, "delVal"));

        Mockito.when(delegateCreator.call(Mockito.any(Object[].class))).thenThrow(new RuntimeException("delErr"));
        try {
            vi.createUsingDelegate(mockCtxt, "delVal");
            Assert.fail();
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testCreateUsingArrayDelegateFallback() throws Exception {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        AnnotatedWithParams arrayCreator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(arrayCreator.call1("arr")).thenReturn("arrResult");
        vi.configureFromArraySettings(arrayCreator, stringType, null);

        Assert.assertEquals("arrResult", vi.createUsingDelegate(ctxt, "arr"));
        Assert.assertEquals("arrResult", vi.createUsingArrayDelegate(ctxt, "arr"));

        StdValueInstantiator vi2 = new StdValueInstantiator((DeserializationConfig) null, stringType);
        AnnotatedWithParams delegateCreator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(delegateCreator.call1("delOnly")).thenReturn("delOnlyResult");
        vi2.configureFromObjectSettings(null, delegateCreator, stringType, null, null, null);

        Assert.assertEquals("delOnlyResult", vi2.createUsingArrayDelegate(ctxt, "delOnly"));

        StdValueInstantiator vi3 = new StdValueInstantiator((DeserializationConfig) null, stringType);
        try {
            vi3.createUsingArrayDelegate(ctxt, "empty");
            Assert.fail();
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("No delegate constructor"));
        }
    }

    @Test
    public void testCreateFromString() throws Exception {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        try {
            vi.createFromString(ctxt, "hello");
            Assert.fail();
        } catch (JsonMappingException e) {
        }

        AnnotatedWithParams creator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(creator.call1("hello")).thenReturn("helloResult");
        vi.configureFromStringCreator(creator);
        Assert.assertEquals("helloResult", vi.createFromString(ctxt, "hello"));

        Mockito.doReturn(String.class).when(creator).getDeclaringClass();
        Mockito.when(creator.call1("throw")).thenThrow(new RuntimeException("strErr"));
        try {
            vi.createFromString(ctxt, "throw");
            Assert.fail();
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testCreateFromInt() throws Exception {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        try {
            vi.createFromInt(ctxt, 123);
            Assert.fail();
        } catch (JsonMappingException e) {
        }

        AnnotatedWithParams intCreator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(intCreator.call1(123)).thenReturn("intResult");
        vi.configureFromIntCreator(intCreator);
        Assert.assertEquals("intResult", vi.createFromInt(ctxt, 123));

        Mockito.doReturn(Integer.class).when(intCreator).getDeclaringClass();
        Mockito.when(intCreator.call1(999)).thenThrow(new RuntimeException("intErr"));
        try {
            vi.createFromInt(ctxt, 999);
            Assert.fail();
        } catch (JsonMappingException e) {
        }

        StdValueInstantiator viLongFallback = new StdValueInstantiator((DeserializationConfig) null, stringType);
        AnnotatedWithParams longCreator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(longCreator.call1(123L)).thenReturn("longFromIntResult");
        viLongFallback.configureFromLongCreator(longCreator);
        Assert.assertEquals("longFromIntResult", viLongFallback.createFromInt(ctxt, 123));

        Mockito.doReturn(Long.class).when(longCreator).getDeclaringClass();
        Mockito.when(longCreator.call1(999L)).thenThrow(new RuntimeException("longErr"));
        try {
            viLongFallback.createFromInt(ctxt, 999);
            Assert.fail();
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testCreateFromLong() throws Exception {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        try {
            vi.createFromLong(ctxt, 100L);
            Assert.fail();
        } catch (JsonMappingException e) {
        }

        AnnotatedWithParams longCreator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(longCreator.call1(100L)).thenReturn("longResult");
        vi.configureFromLongCreator(longCreator);
        Assert.assertEquals("longResult", vi.createFromLong(ctxt, 100L));

        Mockito.doReturn(Long.class).when(longCreator).getDeclaringClass();
        Mockito.when(longCreator.call1(200L)).thenThrow(new RuntimeException("longErr"));
        try {
            vi.createFromLong(ctxt, 200L);
            Assert.fail();
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testCreateFromDouble() throws Exception {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        try {
            vi.createFromDouble(ctxt, 3.14);
            Assert.fail();
        } catch (JsonMappingException e) {
        }

        AnnotatedWithParams doubleCreator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(doubleCreator.call1(3.14)).thenReturn("doubleResult");
        vi.configureFromDoubleCreator(doubleCreator);
        Assert.assertEquals("doubleResult", vi.createFromDouble(ctxt, 3.14));

        Mockito.doReturn(Double.class).when(doubleCreator).getDeclaringClass();
        Mockito.when(doubleCreator.call1(2.71)).thenThrow(new RuntimeException("doubleErr"));
        try {
            vi.createFromDouble(ctxt, 2.71);
            Assert.fail();
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testCreateFromBoolean() throws Exception {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);
        try {
            vi.createFromBoolean(ctxt, true);
            Assert.fail();
        } catch (JsonMappingException e) {
        }

        AnnotatedWithParams boolCreator = Mockito.mock(AnnotatedWithParams.class);
        Mockito.when(boolCreator.call1(Boolean.TRUE)).thenReturn("boolResult");
        vi.configureFromBooleanCreator(boolCreator);
        Assert.assertEquals("boolResult", vi.createFromBoolean(ctxt, true));

        Mockito.doReturn(Boolean.class).when(boolCreator).getDeclaringClass();
        Mockito.when(boolCreator.call1(Boolean.FALSE)).thenThrow(new RuntimeException("boolErr"));
        try {
            vi.createFromBoolean(ctxt, false);
            Assert.fail();
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testExceptionWrappers() {
        StdValueInstantiator vi = new StdValueInstantiator((DeserializationConfig) null, stringType);

        JsonMappingException jme = new JsonMappingException(null, "orig");
        Exception wrapped = new Exception(new Exception(jme));
        Assert.assertSame(jme, vi.wrapException(wrapped));

        Exception generic = new Exception("generic");
        JsonMappingException wrappedGeneric = vi.wrapException(generic);
        Assert.assertTrue(wrappedGeneric.getMessage().contains("generic"));

        Assert.assertSame(jme, vi.unwrapAndWrapException(ctxt, wrapped));
        JsonMappingException unwrappedGeneric = vi.unwrapAndWrapException(ctxt, generic);
        Assert.assertNotNull(unwrappedGeneric);

        Assert.assertSame(jme, vi.wrapAsJsonMappingException(ctxt, jme));
        JsonMappingException mappedGeneric = vi.wrapAsJsonMappingException(ctxt, generic);
        Assert.assertNotNull(mappedGeneric);

        InvocationTargetException ite = new InvocationTargetException(jme);
        Assert.assertSame(jme, vi.rewrapCtorProblem(ctxt, ite));

        ExceptionInInitializerError eiie = new ExceptionInInitializerError(jme);
        Assert.assertSame(jme, vi.rewrapCtorProblem(ctxt, eiie));

        InvocationTargetException iteNull = new InvocationTargetException(null);
        Assert.assertNotNull(vi.rewrapCtorProblem(ctxt, iteNull));
    }
}
