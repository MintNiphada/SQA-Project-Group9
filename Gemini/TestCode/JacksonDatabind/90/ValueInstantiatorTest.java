package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class ValueInstantiatorTest {

    private static class DummyInstantiator extends ValueInstantiator {
        private Class<?> _class = Object.class;
        private AnnotatedWithParams _defaultCreator;
        private boolean _canDelegate;
        private boolean _canObjectWith;
        private boolean _canString;
        private boolean _canInt;
        private boolean _canLong;
        private boolean _canDouble;
        private boolean _canBoolean;

        @Override
        public Class<?> getValueClass() {
            return _class;
        }

        @Override
        public AnnotatedWithParams getDefaultCreator() {
            return _defaultCreator;
        }

        @Override
        public boolean canCreateUsingDelegate() {
            return _canDelegate;
        }

        @Override
        public boolean canCreateFromObjectWith() {
            return _canObjectWith;
        }

        @Override
        public boolean canCreateFromString() {
            return _canString;
        }

        @Override
        public boolean canCreateFromInt() {
            return _canInt;
        }

        @Override
        public boolean canCreateFromLong() {
            return _canLong;
        }

        @Override
        public boolean canCreateFromDouble() {
            return _canDouble;
        }

        @Override
        public boolean canCreateFromBoolean() {
            return _canBoolean;
        }
    }

    private DeserializationContext createMockContext() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.getDeserializationContext();
    }

    @Test
    public void testDefaultMetadataAndDescriptions() {
        DummyInstantiator inst = new DummyInstantiator();
        Assert.assertEquals(Object.class, inst.getValueClass());
        Assert.assertEquals("java.lang.Object", inst.getValueTypeDesc());

        inst._class = null;
        Assert.assertEquals("UNKNOWN", inst.getValueTypeDesc());

        inst._class = String.class;
        Assert.assertEquals("java.lang.String", inst.getValueTypeDesc());
    }

    @Test
    public void testCanInstantiateCombinations() {
        DummyInstantiator inst = new DummyInstantiator();
        Assert.assertFalse(inst.canInstantiate());

        inst._defaultCreator = Mockito.mock(AnnotatedWithParams.class);
        Assert.assertTrue(inst.canCreateUsingDefault());
        Assert.assertTrue(inst.canInstantiate());

        inst._defaultCreator = null;
        inst._canDelegate = true;
        Assert.assertTrue(inst.canInstantiate());

        inst._canDelegate = false;
        inst._canObjectWith = true;
        Assert.assertTrue(inst.canInstantiate());

        inst._canObjectWith = false;
        inst._canString = true;
        Assert.assertTrue(inst.canInstantiate());

        inst._canString = false;
        inst._canInt = true;
        Assert.assertTrue(inst.canInstantiate());

        inst._canInt = false;
        inst._canLong = true;
        Assert.assertTrue(inst.canInstantiate());

        inst._canLong = false;
        inst._canDouble = true;
        Assert.assertTrue(inst.canInstantiate());

        inst._canDouble = false;
        inst._canBoolean = true;
        Assert.assertTrue(inst.canInstantiate());
    }

    @Test
    public void testDefaultImplementationsReturnNullOrFalse() {
        ValueInstantiator inst = new DummyInstantiator();
        Assert.assertFalse(inst.canCreateUsingArrayDelegate());
        Assert.assertNull(inst.getFromObjectArguments(null));
        Assert.assertNull(inst.getDelegateType(null));
        Assert.assertNull(inst.getArrayDelegateType(null));
        Assert.assertNull(inst.getDelegateCreator());
        Assert.assertNull(inst.getArrayDelegateCreator());
        Assert.assertNull(inst.getWithArgsCreator());
        Assert.assertNull(inst.getIncompleteParameter());
    }

    @Test
    public void testMissingInstantiatorHandlers() throws IOException {
        ValueInstantiator inst = new DummyInstantiator();
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);
        Mockito.when(ctxt.getParser()).thenReturn(p);

        inst.createUsingDefault(ctxt);
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no default no-arguments constructor found");

        inst.createFromObjectWith(ctxt, new Object[0]);
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no creator with arguments specified");

        inst.createUsingDelegate(ctxt, "del");
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no delegate creator specified");

        inst.createUsingArrayDelegate(ctxt, new Object[0]);
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no array delegate creator specified");

        inst.createFromInt(ctxt, 42);
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no int/Int-argument constructor/factory method to deserialize from Number value (%s)", 42);

        inst.createFromLong(ctxt, 42L);
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no long/Long-argument constructor/factory method to deserialize from Number value (%s)", 42L);

        inst.createFromDouble(ctxt, 42.5);
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no double/Double-argument constructor/factory method to deserialize from Number value (%s)", 42.5);

        inst.createFromBoolean(ctxt, true);
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no boolean/Boolean-argument constructor/factory method to deserialize from boolean value (%s)", true);
    }

    @Test
    public void testCreateFromObjectWithBufferDelegation() throws IOException {
        ValueInstantiator inst = Mockito.spy(new DummyInstantiator());
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        SettableBeanProperty[] props = new SettableBeanProperty[0];
        PropertyValueBuffer buffer = Mockito.mock(PropertyValueBuffer.class);
        Object[] args = new Object[]{"val"};
        Mockito.when(buffer.getParameters(props)).thenReturn(args);
        Mockito.doReturn("created").when(inst).createFromObjectWith(ctxt, args);

        Object result = inst.createFromObjectWith(ctxt, props, buffer);
        Assert.assertEquals("created", result);
        Mockito.verify(inst).createFromObjectWith(ctxt, args);
    }

    @Test
    public void testCreateFromStringFallbacks() throws IOException {
        DummyInstantiator inst = new DummyInstantiator();
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);
        Mockito.when(ctxt.getParser()).thenReturn(p);

        inst._canBoolean = true;
        ValueInstantiator spyInst = Mockito.spy(inst);
        Mockito.doReturn("bool-true").when(spyInst).createFromBoolean(ctxt, true);
        Mockito.doReturn("bool-false").when(spyInst).createFromBoolean(ctxt, false);

        Assert.assertEquals("bool-true", spyInst.createFromString(ctxt, "true"));
        Assert.assertEquals("bool-true", spyInst.createFromString(ctxt, "  true  "));
        Assert.assertEquals("bool-false", spyInst.createFromString(ctxt, "false"));
        Assert.assertEquals("bool-false", spyInst.createFromString(ctxt, " false "));

        Mockito.when(ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)).thenReturn(true);
        Assert.assertNull(spyInst.createFromString(ctxt, ""));

        Mockito.when(ctxt.isEnabled(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)).thenReturn(false);
        spyInst.createFromString(ctxt, "");
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no String-argument constructor/factory method to deserialize from String value ('%s')", "");

        spyInst.createFromString(ctxt, "not-a-bool");
        Mockito.verify(ctxt).handleMissingInstantiator(Object.class, p, "no String-argument constructor/factory method to deserialize from String value ('%s')", "not-a-bool");
    }

    @Test
    public void testBaseImplementation() {
        ValueInstantiator.Base baseClass = new ValueInstantiator.Base(String.class);
        Assert.assertEquals(String.class, baseClass.getValueClass());
        Assert.assertEquals(String.class.getName(), baseClass.getValueTypeDesc());

        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        ValueInstantiator.Base baseType = new ValueInstantiator.Base(type);
        Assert.assertEquals(Integer.class, baseType.getValueClass());
        Assert.assertEquals(Integer.class.getName(), baseType.getValueTypeDesc());
    }
}
