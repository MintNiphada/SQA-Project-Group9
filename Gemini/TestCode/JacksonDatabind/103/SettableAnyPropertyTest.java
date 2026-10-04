package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SettableAnyPropertyTest {

    static class TargetBean {
        public Map<String, Object> map = new HashMap<String, Object>();
        public Map<String, Object> nullMap = null;

        public void anySetter(String name, Object value) {
            map.put(name, value);
        }

        public void throwingSetter(String name, Object value) {
            throw new IllegalArgumentException("custom error");
        }

        public void throwingSetterNoMsg(String name, Object value) {
            throw new IllegalArgumentException((String) null);
        }

        public void throwingCheckedSetter(String name, Object value) throws Exception {
            throw new Exception("checked error");
        }

        public void throwingRuntimeSetter(String name, Object value) {
            throw new IllegalStateException("runtime error");
        }

        public void throwingIOESetter(String name, Object value) throws IOException {
            throw new IOException("ioe error");
        }
    }

    private AnnotatedMethod getAnnotatedMethod(String name, Class<?>... params) throws Exception {
        Method m = TargetBean.class.getDeclaredMethod(name, params);
        TypeResolutionContext typeContext = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), TypeFactory.defaultInstance().constructType(TargetBean.class).getBindings());
        return new AnnotatedMethod(typeContext, m, null, null);
    }

    private AnnotatedField getAnnotatedField(String name) throws Exception {
        Field f = TargetBean.class.getDeclaredField(name);
        TypeResolutionContext typeContext = new TypeResolutionContext.Basic(TypeFactory.defaultInstance(), TypeFactory.defaultInstance().constructType(TargetBean.class).getBindings());
        return new AnnotatedField(typeContext, f, null);
    }

    @Test
    public void testConstructorsAndAccessors() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        BeanProperty prop = new BeanProperty.Std(PropertyName.construct("test"), type, null, method, PropertyMetadata.STD_OPTIONAL);

        SettableAnyProperty anyPropDeprecated = new SettableAnyProperty(prop, method, type, null, null);
        Assert.assertNull(anyPropDeprecated._keyDeserializer);
        Assert.assertFalse(anyPropDeprecated.hasValueDeserializer());
        Assert.assertEquals(prop, anyPropDeprecated.getProperty());
        Assert.assertEquals(type, anyPropDeprecated.getType());
        Assert.assertEquals("[any property on class " + TargetBean.class.getName() + "]", anyPropDeprecated.toString());

        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        SettableAnyProperty withDeser = anyPropDeprecated.withValueDeserializer(deser);
        Assert.assertTrue(withDeser.hasValueDeserializer());
        Assert.assertNotSame(anyPropDeprecated, withDeser);
    }

    @Test
    public void testFixAccess() throws Exception {
        AnnotatedMethod method = Mockito.mock(AnnotatedMethod.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, null, null);

        DeserializationConfig config = Mockito.mock(DeserializationConfig.class);
        Mockito.when(config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS)).thenReturn(true);

        anyProp.fixAccess(config);
        Mockito.verify(method).fixAccess(true);
    }

    @Test
    public void testReadResolveSuccess() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, null, null);

        Object resolved = anyProp.readResolve();
        Assert.assertSame(anyProp, resolved);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadResolveNullSetter() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, null, type, null, null, null);
        anyProp.readResolve();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReadResolveNullAnnotated() {
        AnnotatedMember member = Mockito.mock(AnnotatedMember.class);
        Mockito.when(member.getAnnotated()).thenReturn(null);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, member, type, null, null, null);
        anyProp.readResolve();
    }

    @Test
    public void testDeserializeNullToken() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);

        Mockito.when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_NULL);
        Mockito.when(deser.getNullValue(ctxt)).thenReturn("NULL_VAL");

        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, deser, null);
        Object result = anyProp.deserialize(p, ctxt);
        Assert.assertEquals("NULL_VAL", result);
    }

    @Test
    public void testDeserializeWithType() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        TypeDeserializer typeDeser = Mockito.mock(TypeDeserializer.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);

        Mockito.when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(deser.deserializeWithType(p, ctxt, typeDeser)).thenReturn("TYPED_VAL");

        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, deser, typeDeser);
        Object result = anyProp.deserialize(p, ctxt);
        Assert.assertEquals("TYPED_VAL", result);
    }

    @Test
    public void testDeserializeStandard() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);

        Mockito.when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(deser.deserialize(p, ctxt)).thenReturn("PLAIN_VAL");

        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, deser, null);
        Object result = anyProp.deserialize(p, ctxt);
        Assert.assertEquals("PLAIN_VAL", result);
    }

    @Test
    public void testDeserializeAndSetMethodWithoutKeyDeser() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);

        Mockito.when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(deser.deserialize(p, ctxt)).thenReturn("val1");

        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, deser, null);
        TargetBean bean = new TargetBean();
        anyProp.deserializeAndSet(p, ctxt, bean, "key1");

        Assert.assertEquals("val1", bean.map.get("key1"));
    }

    @Test
    public void testDeserializeAndSetMethodWithKeyDeser() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        KeyDeserializer keyDeser = Mockito.mock(KeyDeserializer.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);

        Mockito.when(keyDeser.deserializeKey("rawKey", ctxt)).thenReturn("customKey");
        Mockito.when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(deser.deserialize(p, ctxt)).thenReturn("val2");

        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, keyDeser, deser, null);
        TargetBean bean = new TargetBean();
        anyProp.deserializeAndSet(p, ctxt, bean, "rawKey");

        Assert.assertEquals("val2", bean.map.get("customKey"));
    }

    @Test
    public void testSetFieldValidAndNull() throws Exception {
        AnnotatedField field = getAnnotatedField("map");
        JavaType type = TypeFactory.defaultInstance().constructType(Map.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, field, type, null, null, null);

        TargetBean bean = new TargetBean();
        anyProp.set(bean, "fieldKey", "fieldVal");
        Assert.assertEquals("fieldVal", bean.map.get("fieldKey"));

        AnnotatedField nullField = getAnnotatedField("nullMap");
        SettableAnyProperty anyPropNull = new SettableAnyProperty(null, nullField, type, null, null, null);
        anyPropNull.set(bean, "fieldKey", "fieldVal");
        Assert.assertNull(bean.nullMap);
    }

    @Test
    public void testUnresolvedForwardReferenceNoObjectIdReader() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);

        UnresolvedForwardReference ufr = new UnresolvedForwardReference(p, "Unresolved", new JsonLocation(null, 0, 0, 0), null);
        Mockito.when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(deser.deserialize(p, ctxt)).thenThrow(ufr);
        Mockito.when(deser.getObjectIdReader()).thenReturn(null);

        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, deser, null);
        TargetBean bean = new TargetBean();

        try {
            anyProp.deserializeAndSet(p, ctxt, bean, "key");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Unresolved forward reference but no identity info"));
        }
    }

    @Test
    public void testUnresolvedForwardReferenceSuccess() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        ObjectIdReader oir = Mockito.mock(ObjectIdReader.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        JsonParser p = Mockito.mock(JsonParser.class);

        UnresolvedForwardReference ufr = Mockito.mock(UnresolvedForwardReference.class);
        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);
        Mockito.when(ufr.getRoid()).thenReturn(roid);
        Mockito.when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(deser.deserialize(p, ctxt)).thenThrow(ufr);
        Mockito.when(deser.getObjectIdReader()).thenReturn(oir);

        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, deser, null);
        TargetBean bean = new TargetBean();
        anyProp.deserializeAndSet(p, ctxt, bean, "key");

        Mockito.verify(roid).appendReferring(Mockito.any(ReadableObjectId.Referring.class));
    }

    @Test
    public void testAnySetterReferringHandleResolved() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("anySetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, null, null);

        UnresolvedForwardReference ufr = Mockito.mock(UnresolvedForwardReference.class);
        ReadableObjectId.Referring referring = null;

        ReadableObjectId roid = Mockito.mock(ReadableObjectId.class);
        Mockito.when(ufr.getRoid()).thenReturn(roid);
        JsonDeserializer<Object> deser = Mockito.mock(JsonDeserializer.class);
        ObjectIdReader oir = Mockito.mock(ObjectIdReader.class);
        Mockito.when(deser.getObjectIdReader()).thenReturn(oir);
        JsonParser p = Mockito.mock(JsonParser.class);
        DeserializationContext ctxt = Mockito.mock(DeserializationContext.class);
        Mockito.when(p.getCurrentToken()).thenReturn(JsonToken.VALUE_STRING);
        Mockito.when(deser.deserialize(p, ctxt)).thenThrow(ufr);

        SettableAnyProperty propWithDeser = new SettableAnyProperty(null, method, type, null, deser, null);
        TargetBean bean = new TargetBean();

        final ReadableObjectId.Referring[] captured = new ReadableObjectId.Referring[1];
        Mockito.doAnswer(invocation -> {
            captured[0] = invocation.getArgument(0);
            return null;
        }).when(roid).appendReferring(Mockito.any(ReadableObjectId.Referring.class));

        propWithDeser.deserializeAndSet(p, ctxt, bean, "refKey");

        ReadableObjectId.Referring ref = captured[0];
        Assert.assertNotNull(ref);

        Mockito.when(ufr.getUnresolvedId()).thenReturn("id1");
        ref.handleResolvedForwardReference("id1", "resolvedVal");
        Assert.assertEquals("resolvedVal", bean.map.get("refKey"));

        try {
            ref.handleResolvedForwardReference("wrongId", "val");
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("wasn't previously registered"));
        }
    }

    @Test
    public void testThrowAsIOEIllegalArgumentException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("throwingSetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, null, null);
        TargetBean bean = new TargetBean();

        try {
            anyProp.set(bean, "prop", "val");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Problem deserializing \"any\" property 'prop'"));
            Assert.assertTrue(e.getMessage().contains("custom error"));
        }
    }

    @Test
    public void testThrowAsIOEIllegalArgumentExceptionNoMessage() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("throwingSetterNoMsg", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, null, null);
        TargetBean bean = new TargetBean();

        try {
            anyProp.set(bean, "prop", "val");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("(no error message provided)"));
        }
    }

    @Test(expected = IOException.class)
    public void testThrowAsIOEIOException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("throwingIOESetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, null, null);
        TargetBean bean = new TargetBean();
        anyProp.set(bean, "prop", "val");
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowAsIOERuntimeException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("throwingRuntimeSetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, null, null);
        TargetBean bean = new TargetBean();
        anyProp.set(bean, "prop", "val");
    }

    @Test
    public void testThrowAsIOECheckedException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod("throwingCheckedSetter", String.class, Object.class);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        SettableAnyProperty anyProp = new SettableAnyProperty(null, method, type, null, null, null);
        TargetBean bean = new TargetBean();

        try {
            anyProp.set(bean, "prop", "val");
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertEquals("checked error", e.getMessage());
        }
    }
}
