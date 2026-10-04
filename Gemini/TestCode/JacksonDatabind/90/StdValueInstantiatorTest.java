package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class StdValueInstantiatorTest {

    private ObjectMapper mapper;
    private DeserializationContext ctxt;
    private JavaType stringType;

    private static class DummyAnnotatedWithParams extends AnnotatedWithParams {
        private static final long serialVersionUID = 1L;
        private final Object returnValue;
        private final Throwable toThrow;
        private final Class<?> declaringClass;

        DummyAnnotatedWithParams(Object returnValue, Throwable toThrow, Class<?> declaringClass) {
            super(null, null, null);
            this.returnValue = returnValue;
            this.toThrow = toThrow;
            this.declaringClass = declaringClass;
        }

        @Override
        public int getParameterCount() {
            return 0;
        }

        @Override
        public Class<?> getRawParameterType(int index) {
            return Object.class;
        }

        @Override
        public JavaType getParameterType(int index) {
            return null;
        }

        @Override
        @Deprecated
        public JavaType resolveParameterType(int index, com.fasterxml.jackson.databind.type.TypeBindings bindings) {
            return null;
        }

        @Override
        public Object call() throws Exception {
            if (toThrow != null) {
                if (toThrow instanceof Exception) throw (Exception) toThrow;
                if (toThrow instanceof Error) throw (Error) toThrow;
                throw new RuntimeException(toThrow);
            }
            return returnValue;
        }

        @Override
        public Object call(Object[] args) throws Exception {
            if (toThrow != null) {
                if (toThrow instanceof Exception) throw (Exception) toThrow;
                if (toThrow instanceof Error) throw (Error) toThrow;
                throw new RuntimeException(toThrow);
            }
            return returnValue;
        }

        @Override
        public Object call1(Object arg) throws Exception {
            if (toThrow != null) {
                if (toThrow instanceof Exception) throw (Exception) toThrow;
                if (toThrow instanceof Error) throw (Error) toThrow;
                throw new RuntimeException(toThrow);
            }
            return returnValue;
        }

        @Override
        public java.lang.reflect.AnnotatedElement getAnnotated() {
            return null;
        }

        @Override
        public int getModifiers() {
            return 0;
        }

        @Override
        public String getName() {
            return "dummy";
        }

        @Override
        public Class<?> getRawType() {
            return declaringClass;
        }

        @Override
        public JavaType getType() {
            return null;
        }

        @Override
        public Class<?> getDeclaringClass() {
            return declaringClass;
        }

        @Override
        public com.fasterxml.jackson.databind.introspect.Annotated withAnnotations(com.fasterxml.jackson.databind.introspect.AnnotationMap fallback) {
            return this;
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "DummyAnnotatedWithParams";
        }
    }

    private static class SubStdValueInstantiator extends StdValueInstantiator {
        private static final long serialVersionUID = 1L;

        public SubStdValueInstantiator(StdValueInstantiator src) {
            super(src);
        }
    }

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
        ctxt = mapper.getDeserializationContext();
        stringType = TypeFactory.defaultInstance().constructType(String.class);
    }

    @Test
    public void testConstructorsNullArguments() {
        StdValueInstantiator inst1 = new StdValueInstantiator((DeserializationConfig) null, (Class<?>) null);
        Assert.assertEquals("UNKNOWN TYPE", inst1.getValueTypeDesc());
        Assert.assertEquals(Object.class, inst1.getValueClass());

        StdValueInstantiator inst2 = new StdValueInstantiator((DeserializationConfig) null, (JavaType) null);
        Assert.assertEquals("UNKNOWN TYPE", inst2.getValueTypeDesc());
        Assert.assertEquals(Object.class, inst2.getValueClass());
    }

    @Test
    public void testConstructorsValidArguments() {
        StdValueInstantiator inst1 = new StdValueInstantiator(null, String.class);
        Assert.assertEquals(String.class.getName(), inst1.getValueTypeDesc());
        Assert.assertEquals(String.class, inst1.getValueClass());

        StdValueInstantiator inst2 = new StdValueInstantiator(null, stringType);
        Assert.assertEquals(stringType.toString(), inst2.getValueTypeDesc());
        Assert.assertEquals(String.class, inst2.getValueClass());
    }

    @Test
    public void testCopyConstructor() {
        StdValueInstantiator src = new StdValueInstantiator(null, stringType);
        AnnotatedWithParams defaultCreator = new DummyAnnotatedWithParams("default", null, String.class);
        AnnotatedWithParams delegateCreator = new DummyAnnotatedWithParams("delegate", null, String.class);
        AnnotatedWithParams arrayCreator = new DummyAnnotatedWithParams("array", null, String.class);
        AnnotatedWithParams withArgsCreator = new DummyAnnotatedWithParams("withArgs", null, String.class);
        AnnotatedWithParams strCreator = new DummyAnnotatedWithParams("str", null, String.class);
        AnnotatedWithParams intCreator = new DummyAnnotatedWithParams("int", null, String.class);
        AnnotatedWithParams longCreator = new DummyAnnotatedWithParams("long", null, String.class);
        AnnotatedWithParams doubleCreator = new DummyAnnotatedWithParams("double", null, String.class);
        AnnotatedWithParams boolCreator = new DummyAnnotatedWithParams("bool", null, String.class);

        src.configureFromObjectSettings(defaultCreator, delegateCreator, stringType, null, withArgsCreator, null);
        src.configureFromArraySettings(arrayCreator, stringType, null);
        src.configureFromStringCreator(strCreator);
        src.configureFromIntCreator(intCreator);
        src.configureFromLongCreator(longCreator);
        src.configureFromDoubleCreator(doubleCreator);
        src.configureFromBooleanCreator(boolCreator);

        SubStdValueInstantiator copy = new SubStdValueInstantiator(src);
        Assert.assertEquals(src.getValueTypeDesc(), copy.getValueTypeDesc());
        Assert.assertEquals(src.getValueClass(), copy.getValueClass());
        Assert.assertTrue(copy.canCreateUsingDefault());
        Assert.assertTrue(copy.canCreateUsingDelegate());
        Assert.assertTrue(copy.canCreateUsingArrayDelegate());
        Assert.assertTrue(copy.canCreateFromObjectWith());
        Assert.assertTrue(copy.canCreateFromString());
        Assert.assertTrue(copy.canCreateFromInt());
        Assert.assertTrue(copy.canCreateFromLong());
        Assert.assertTrue(copy.canCreateFromDouble());
        Assert.assertTrue(copy.canCreateFromBoolean());
        Assert.assertSame(defaultCreator, copy.getDefaultCreator());
        Assert.assertSame(delegateCreator, copy.getDelegateCreator());
        Assert.assertSame(arrayCreator, copy.getArrayDelegateCreator());
        Assert.assertSame(withArgsCreator, copy.getWithArgsCreator());
    }

    @Test
    public void testConfigureIncompleteParameter() {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        Assert.assertNull(inst.getIncompleteParameter());
        AnnotatedParameter param = new AnnotatedParameter(null, stringType, null, null, 0);
        inst.configureIncompleteParameter(param);
        Assert.assertSame(param, inst.getIncompleteParameter());
    }

    @Test
    public void testGettersWhenEmpty() {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        Assert.assertFalse(inst.canCreateFromString());
        Assert.assertFalse(inst.canCreateFromInt());
        Assert.assertFalse(inst.canCreateFromLong());
        Assert.assertFalse(inst.canCreateFromDouble());
        Assert.assertFalse(inst.canCreateFromBoolean());
        Assert.assertFalse(inst.canCreateUsingDefault());
        Assert.assertFalse(inst.canCreateUsingDelegate());
        Assert.assertFalse(inst.canCreateUsingArrayDelegate());
        Assert.assertFalse(inst.canCreateFromObjectWith());
        Assert.assertNull(inst.getDelegateType(null));
        Assert.assertNull(inst.getArrayDelegateType(null));
        Assert.assertNull(inst.getFromObjectArguments(null));
        Assert.assertNull(inst.getDelegateCreator());
        Assert.assertNull(inst.getArrayDelegateCreator());
        Assert.assertNull(inst.getDefaultCreator());
        Assert.assertNull(inst.getWithArgsCreator());
    }

    @Test
    public void testCreateUsingDefaultSuccess() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams("createdDefault", null, String.class);
        inst.configureFromObjectSettings(creator, null, null, null, null, null);
        Assert.assertEquals("createdDefault", inst.createUsingDefault(ctxt));
    }

    @Test(expected = JsonMappingException.class)
    public void testCreateUsingDefaultNullThrows() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        inst.createUsingDefault(ctxt);
    }

    @Test(expected = JsonMappingException.class)
    public void testCreateUsingDefaultException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams(null, new RuntimeException("fail"), String.class);
        inst.configureFromObjectSettings(creator, null, null, null, null, null);
        inst.createUsingDefault(ctxt);
    }

    @Test
    public void testCreateFromObjectWithSuccess() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams("createdWithArgs", null, String.class);
        inst.configureFromObjectSettings(null, null, null, null, creator, null);
        Assert.assertEquals("createdWithArgs", inst.createFromObjectWith(ctxt, new Object[]{"a"}));
    }

    @Test(expected = JsonMappingException.class)
    public void testCreateFromObjectWithNullThrows() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        inst.createFromObjectWith(ctxt, new Object[0]);
    }

    @Test(expected = JsonMappingException.class)
    public void testCreateFromObjectWithException() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams(null, new IllegalStateException("fail"), String.class);
        inst.configureFromObjectSettings(null, null, null, null, creator, null);
        inst.createFromObjectWith(ctxt, new Object[]{"arg"});
    }

    @Test
    public void testCreateUsingDelegateSuccess() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams("delegated", null, String.class);
        inst.configureFromObjectSettings(null, creator, stringType, null, null, null);
        Assert.assertEquals("delegated", inst.createUsingDelegate(ctxt, "delegateVal"));
    }

    @Test
    public void testCreateUsingDelegateFallbackToArrayDelegate() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams arrayCreator = new DummyAnnotatedWithParams("arrayDelegated", null, String.class);
        inst.configureFromArraySettings(arrayCreator, stringType, null);
        Assert.assertEquals("arrayDelegated", inst.createUsingDelegate(ctxt, "delegateVal"));
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateUsingDelegateNoCreatorThrows() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        inst.createUsingDelegate(ctxt, "delegateVal");
    }

    @Test
    public void testCreateUsingArrayDelegateSuccess() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams arrayCreator = new DummyAnnotatedWithParams("arrayDelegated", null, String.class);
        inst.configureFromArraySettings(arrayCreator, stringType, null);
        Assert.assertEquals("arrayDelegated", inst.createUsingArrayDelegate(ctxt, "delegateVal"));
    }

    @Test
    public void testCreateUsingArrayDelegateFallbackToDelegate() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams("delegated", null, String.class);
        inst.configureFromObjectSettings(null, creator, stringType, null, null, null);
        Assert.assertEquals("delegated", inst.createUsingArrayDelegate(ctxt, "delegateVal"));
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateUsingArrayDelegateNoCreatorThrows() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        inst.createUsingArrayDelegate(ctxt, "delegateVal");
    }

    @Test
    public void testCreateUsingDelegateWithInjectables() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams("delegatedWithInjectables", null, String.class);
        SettableBeanProperty[] props = new SettableBeanProperty[]{null};
        inst.configureFromObjectSettings(null, creator, stringType, props, null, null);
        Assert.assertEquals("delegatedWithInjectables", inst.createUsingDelegate(ctxt, "delegateVal"));
    }

    @Test
    public void testCreateFromString() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams("strVal", null, String.class);
        inst.configureFromStringCreator(creator);
        Assert.assertEquals("strVal", inst.createFromString(ctxt, "input"));

        StdValueInstantiator inst2 = new StdValueInstantiator(null, String.class);
        Assert.assertEquals("direct", inst2.createFromString(ctxt, "direct"));

        DummyAnnotatedWithParams excCreator = new DummyAnnotatedWithParams(null, new RuntimeException("err"), String.class);
        inst.configureFromStringCreator(excCreator);
        try {
            inst.createFromString(ctxt, "test");
            Assert.fail();
        } catch (JsonMappingException ignored) {
        }
    }

    @Test
    public void testCreateFromIntNative() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, Integer.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams(123, null, Integer.class);
        inst.configureFromIntCreator(creator);
        Assert.assertEquals(123, inst.createFromInt(ctxt, 123));

        DummyAnnotatedWithParams excCreator = new DummyAnnotatedWithParams(null, new RuntimeException("err"), Integer.class);
        inst.configureFromIntCreator(excCreator);
        try {
            inst.createFromInt(ctxt, 123);
            Assert.fail();
        } catch (JsonMappingException ignored) {
        }
    }

    @Test
    public void testCreateFromIntWideningToLong() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, Long.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams(456L, null, Long.class);
        inst.configureFromLongCreator(creator);
        Assert.assertEquals(456L, inst.createFromInt(ctxt, 456));

        DummyAnnotatedWithParams excCreator = new DummyAnnotatedWithParams(null, new RuntimeException("err"), Long.class);
        inst.configureFromLongCreator(excCreator);
        try {
            inst.createFromInt(ctxt, 456);
            Assert.fail();
        } catch (JsonMappingException ignored) {
        }
    }

    @Test(expected = JsonMappingException.class)
    public void testCreateFromIntFallbackThrows() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        inst.createFromInt(ctxt, 100);
    }

    @Test
    public void testCreateFromLong() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, Long.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams(789L, null, Long.class);
        inst.configureFromLongCreator(creator);
        Assert.assertEquals(789L, inst.createFromLong(ctxt, 789L));

        DummyAnnotatedWithParams excCreator = new DummyAnnotatedWithParams(null, new RuntimeException("err"), Long.class);
        inst.configureFromLongCreator(excCreator);
        try {
            inst.createFromLong(ctxt, 789L);
            Assert.fail();
        } catch (JsonMappingException ignored) {
        }

        StdValueInstantiator inst2 = new StdValueInstantiator(null, String.class);
        try {
            inst2.createFromLong(ctxt, 789L);
            Assert.fail();
        } catch (JsonMappingException ignored) {
        }
    }

    @Test
    public void testCreateFromDouble() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, Double.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams(3.14, null, Double.class);
        inst.configureFromDoubleCreator(creator);
        Assert.assertEquals(3.14, inst.createFromDouble(ctxt, 3.14));

        DummyAnnotatedWithParams excCreator = new DummyAnnotatedWithParams(null, new RuntimeException("err"), Double.class);
        inst.configureFromDoubleCreator(excCreator);
        try {
            inst.createFromDouble(ctxt, 3.14);
            Assert.fail();
        } catch (JsonMappingException ignored) {
        }

        StdValueInstantiator inst2 = new StdValueInstantiator(null, String.class);
        try {
            inst2.createFromDouble(ctxt, 3.14);
            Assert.fail();
        } catch (JsonMappingException ignored) {
        }
    }

    @Test
    public void testCreateFromBoolean() throws IOException {
        StdValueInstantiator inst = new StdValueInstantiator(null, Boolean.class);
        DummyAnnotatedWithParams creator = new DummyAnnotatedWithParams(true, null, Boolean.class);
        inst.configureFromBooleanCreator(creator);
        Assert.assertEquals(true, inst.createFromBoolean(ctxt, true));

        DummyAnnotatedWithParams excCreator = new DummyAnnotatedWithParams(null, new RuntimeException("err"), Boolean.class);
        inst.configureFromBooleanCreator(excCreator);
        try {
            inst.createFromBoolean(ctxt, true);
            Assert.fail();
        } catch (JsonMappingException ignored) {
        }

        StdValueInstantiator inst2 = new StdValueInstantiator(null, String.class);
        try {
            inst2.createFromBoolean(ctxt, true);
            Assert.fail();
        } catch (JsonMappingException ignored) {
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testWrapException() {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        JsonMappingException jme = new JsonMappingException(null, "jme");
        Assert.assertSame(jme, inst.wrapException(jme));

        RuntimeException nested = new RuntimeException("wrapper", jme);
        Assert.assertSame(jme, inst.wrapException(nested));

        JsonMappingException wrapped = inst.wrapException(new RuntimeException("simple"));
        Assert.assertTrue(wrapped.getMessage().contains("simple"));
        Assert.assertTrue(wrapped.getMessage().contains(String.class.getName()));
    }

    @Test
    public void testUnwrapAndWrapException() {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        JsonMappingException jme = new JsonMappingException(null, "jme");
        Assert.assertSame(jme, inst.unwrapAndWrapException(ctxt, jme));

        RuntimeException nested = new RuntimeException("wrapper", jme);
        Assert.assertSame(jme, inst.unwrapAndWrapException(ctxt, nested));

        JsonMappingException wrapped = inst.unwrapAndWrapException(ctxt, new RuntimeException("simple"));
        Assert.assertNotNull(wrapped);
    }

    @Test
    public void testWrapAsJsonMappingException() {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        JsonMappingException jme = new JsonMappingException(null, "jme");
        Assert.assertSame(jme, inst.wrapAsJsonMappingException(ctxt, jme));

        JsonMappingException wrapped = inst.wrapAsJsonMappingException(ctxt, new RuntimeException("error"));
        Assert.assertNotNull(wrapped);
    }

    @Test
    public void testRewrapCtorProblem() {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        JsonMappingException jme = new JsonMappingException(null, "jme");
        Assert.assertSame(jme, inst.rewrapCtorProblem(ctxt, jme));

        InvocationTargetException ite = new InvocationTargetException(jme);
        Assert.assertSame(jme, inst.rewrapCtorProblem(ctxt, ite));

        ExceptionInInitializerError eiie = new ExceptionInInitializerError(jme);
        Assert.assertSame(jme, inst.rewrapCtorProblem(ctxt, eiie));

        InvocationTargetException iteNoCause = new InvocationTargetException(null);
        JsonMappingException wrappedIte = inst.rewrapCtorProblem(ctxt, iteNoCause);
        Assert.assertNotNull(wrappedIte);
    }

    @Test
    public void testGettersAndSettings() {
        StdValueInstantiator inst = new StdValueInstantiator(null, String.class);
        SettableBeanProperty[] ctorArgs = new SettableBeanProperty[0];
        inst.configureFromObjectSettings(null, null, stringType, null, null, ctorArgs);
        inst.configureFromArraySettings(null, stringType, null);

        Assert.assertSame(stringType, inst.getDelegateType(null));
        Assert.assertSame(stringType, inst.getArrayDelegateType(null));
        Assert.assertSame(ctorArgs, inst.getFromObjectArguments(null));
    }
}
