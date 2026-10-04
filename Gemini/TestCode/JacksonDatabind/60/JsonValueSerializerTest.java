package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

public class JsonValueSerializerTest {

    static enum TestEnum {
        A, B;
        public String toVal() {
            return name().toLowerCase();
        }
    }

    static enum BrokenEnum {
        A;
        public String toVal() {
            throw new RuntimeException("enum error");
        }
    }

    static enum ErrorEnum {
        A;
        public String toVal() {
            throw new StackOverflowError("enum boom");
        }
    }

    static class SimpleBean {
        private final String value;
        public SimpleBean(String v) { this.value = v; }
        public String fetchValue() { return value; }
        public String errorValue() { throw new RuntimeException("custom boom"); }
        public String ioErrorValue() throws IOException { throw new IOException("io boom"); }
        public String errorErrorValue() { throw new StackOverflowError("fatal boom"); }
    }

    static class FinalBean {
        public final int val() { return 42; }
    }

    private AnnotatedMethod createAnnotatedMethod(Class<?> cls, String methodName) throws Exception {
        Method m = cls.getMethod(methodName);
        AnnotatedMethod am = Mockito.mock(AnnotatedMethod.class);
        Mockito.when(am.getMember()).thenReturn(m);
        Mockito.when(am.getName()).thenReturn(methodName);
        Mockito.when(am.getDeclaringClass()).thenReturn((Class) cls);
        Mockito.when(am.getType()).thenReturn(TypeFactory.defaultInstance().constructType(m.getGenericReturnType()));
        Mockito.when(am.getValue(Mockito.any())).thenAnswer(inv -> m.invoke(inv.getArgument(0)));
        Mockito.when(am.callOn(Mockito.any())).thenAnswer(inv -> m.invoke(inv.getArgument(0)));
        return am;
    }

    @Test
    public void testConstructorAndToString() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);
        Assert.assertNotNull(serializer.toString());
        Assert.assertTrue(serializer.toString().contains("fetchValue"));

        JsonValueSerializer copy = new JsonValueSerializer(serializer, null, null, false);
        Assert.assertEquals(Object.class, copy.handledType());

        JsonValueSerializer same = serializer.withResolved(null, null, true);
        Assert.assertSame(serializer, same);

        JsonValueSerializer different = serializer.withResolved(null, null, false);
        Assert.assertNotSame(serializer, different);
    }

    @Test
    public void testContextualWithPredefinedSerializer() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonSerializer<Object> mockSer = Mockito.mock(JsonSerializer.class);
        JsonSerializer<Object> contextualSer = Mockito.mock(JsonSerializer.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        BeanProperty prop = Mockito.mock(BeanProperty.class);

        Mockito.when(prov.handlePrimaryContextualization(mockSer, prop)).thenReturn((JsonSerializer) contextualSer);

        JsonValueSerializer serializer = new JsonValueSerializer(am, mockSer);
        JsonSerializer<?> result = serializer.createContextual(prov, prop);

        Assert.assertNotSame(serializer, result);
        Assert.assertTrue(result instanceof JsonValueSerializer);
    }

    @Test
    public void testContextualWithoutSerializerFinalType() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(FinalBean.class, "val");
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        BeanProperty prop = Mockito.mock(BeanProperty.class);
        JsonSerializer<Object> mockSer = Mockito.mock(JsonSerializer.class);

        Mockito.when(prov.isEnabled(MapperFeature.USE_STATIC_TYPING)).thenReturn(false);
        Mockito.when(prov.findPrimaryPropertySerializer(Mockito.any(JavaType.class), Mockito.eq(prop)))
                .thenReturn((JsonSerializer) mockSer);

        JsonValueSerializer serializer = new JsonValueSerializer(am, null);
        JsonSerializer<?> result = serializer.createContextual(prov, prop);

        Assert.assertNotSame(serializer, result);
    }

    @Test
    public void testContextualWithoutSerializerNonFinalDynamic() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JavaType nonFinalType = Mockito.mock(JavaType.class);
        Mockito.when(nonFinalType.isFinal()).thenReturn(false);
        Mockito.when(am.getType()).thenReturn(nonFinalType);

        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        Mockito.when(prov.isEnabled(MapperFeature.USE_STATIC_TYPING)).thenReturn(false);

        JsonValueSerializer serializer = new JsonValueSerializer(am, null);
        JsonSerializer<?> result = serializer.createContextual(prov, null);

        Assert.assertSame(serializer, result);
    }

    @Test
    public void testSerializeNullValue() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);

        SimpleBean bean = new SimpleBean(null);
        serializer.serialize(bean, gen, prov);

        Mockito.verify(prov).defaultSerializeNull(gen);
    }

    @Test
    public void testSerializeNonNullValueWithDynamicSerializer() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        JsonSerializer<Object> valueSer = Mockito.mock(JsonSerializer.class);

        Mockito.when(prov.findTypedValueSerializer(String.class, true, null)).thenReturn(valueSer);

        SimpleBean bean = new SimpleBean("testValue");
        serializer.serialize(bean, gen, prov);

        Mockito.verify(valueSer).serialize("testValue", gen, prov);
    }

    @Test(expected = IOException.class)
    public void testSerializeThrowsIOException() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "ioErrorValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        serializer.serialize(new SimpleBean("x"), Mockito.mock(JsonGenerator.class), Mockito.mock(SerializerProvider.class));
    }

    @Test(expected = JsonMappingException.class)
    public void testSerializeThrowsWrappedException() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "errorValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        serializer.serialize(new SimpleBean("x"), Mockito.mock(JsonGenerator.class), Mockito.mock(SerializerProvider.class));
    }

    @Test(expected = StackOverflowError.class)
    public void testSerializeThrowsError() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "errorErrorValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        serializer.serialize(new SimpleBean("x"), Mockito.mock(JsonGenerator.class), Mockito.mock(SerializerProvider.class));
    }

    @Test
    public void testSerializeWithTypeNullValue() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        TypeSerializer typeSer = Mockito.mock(TypeSerializer.class);

        serializer.serializeWithType(new SimpleBean(null), gen, prov, typeSer);
        Mockito.verify(prov).defaultSerializeNull(gen);
    }

    @Test
    public void testSerializeWithTypeDynamicSerializer() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        TypeSerializer typeSer = Mockito.mock(TypeSerializer.class);
        JsonSerializer<Object> valueSer = Mockito.mock(JsonSerializer.class);

        Mockito.when(prov.findValueSerializer(String.class, null)).thenReturn(valueSer);

        serializer.serializeWithType(new SimpleBean("abc"), gen, prov, typeSer);
        Mockito.verify(valueSer).serializeWithType("abc", gen, prov, typeSer);
    }

    @Test
    public void testSerializeWithTypeForcedTypeInformation() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonSerializer<Object> valueSer = Mockito.mock(JsonSerializer.class);
        JsonValueSerializer serializer = new JsonValueSerializer(am, valueSer);

        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        TypeSerializer typeSer = Mockito.mock(TypeSerializer.class);

        SimpleBean bean = new SimpleBean("abc");
        serializer.serializeWithType(bean, gen, prov, typeSer);

        Mockito.verify(typeSer).writeTypePrefixForScalar(bean, gen);
        Mockito.verify(valueSer).serialize("abc", gen, prov);
        Mockito.verify(typeSer).writeTypeSuffixForScalar(bean, gen);
    }

    @Test
    public void testSerializeWithTypeWithoutForcedTypeInformation() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonSerializer<Object> valueSer = Mockito.mock(JsonSerializer.class);
        JsonValueSerializer serializer = new JsonValueSerializer(new JsonValueSerializer(am, valueSer), null, valueSer, false);

        JsonGenerator gen = Mockito.mock(JsonGenerator.class);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        TypeSerializer typeSer = Mockito.mock(TypeSerializer.class);

        SimpleBean bean = new SimpleBean("abc");
        serializer.serializeWithType(bean, gen, prov, typeSer);

        Mockito.verify(valueSer).serializeWithType("abc", gen, prov, typeSer);
    }

    @Test(expected = IOException.class)
    public void testSerializeWithTypeThrowsIOException() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "ioErrorValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        serializer.serializeWithType(new SimpleBean("x"), Mockito.mock(JsonGenerator.class), Mockito.mock(SerializerProvider.class), Mockito.mock(TypeSerializer.class));
    }

    @Test(expected = JsonMappingException.class)
    public void testSerializeWithTypeThrowsWrappedException() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "errorValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        serializer.serializeWithType(new SimpleBean("x"), Mockito.mock(JsonGenerator.class), Mockito.mock(SerializerProvider.class), Mockito.mock(TypeSerializer.class));
    }

    @Test(expected = StackOverflowError.class)
    public void testSerializeWithTypeThrowsError() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "errorErrorValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        serializer.serializeWithType(new SimpleBean("x"), Mockito.mock(JsonGenerator.class), Mockito.mock(SerializerProvider.class), Mockito.mock(TypeSerializer.class));
    }

    interface SchemaAwareSerializer extends SchemaAware {}

    @Test
    public void testGetSchema() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        JsonNode schemaNode = serializer.getSchema(prov, null);
        Assert.assertNotNull(schemaNode);

        JsonSerializer<Object> schemaSer = (JsonSerializer) Mockito.mock(JsonSerializer.class, Mockito.withSettings().extraInterfaces(SchemaAware.class));
        JsonNode mockNode = Mockito.mock(JsonNode.class);
        Mockito.when(((SchemaAware) schemaSer).getSchema(prov, null)).thenReturn(mockNode);

        JsonValueSerializer schemaAwareSerializer = new JsonValueSerializer(am, schemaSer);
        Assert.assertSame(mockNode, schemaAwareSerializer.getSchema(prov, null));
    }

    @Test
    public void testAcceptJsonFormatVisitorNonEnum() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonSerializer<Object> valueSer = Mockito.mock(JsonSerializer.class);
        JsonValueSerializer serializer = new JsonValueSerializer(am, valueSer);

        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JavaType typeHint = TypeFactory.defaultInstance().constructType(SimpleBean.class);

        serializer.acceptJsonFormatVisitor(visitor, typeHint);
        Mockito.verify(valueSer).acceptJsonFormatVisitor(visitor, null);

        JsonValueSerializer dynamicSerializer = new JsonValueSerializer(am, null);
        SerializerProvider prov = Mockito.mock(SerializerProvider.class);
        Mockito.when(visitor.getProvider()).thenReturn(prov);
        Mockito.when(prov.findTypedValueSerializer(Mockito.any(JavaType.class), Mockito.eq(false), Mockito.isNull())).thenReturn(null);

        dynamicSerializer.acceptJsonFormatVisitor(visitor, typeHint);
        Mockito.verify(visitor).expectAnyFormat(typeHint);
    }

    @Test
    public void testAcceptJsonFormatVisitorForEnum() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(TestEnum.class, "toVal");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JsonStringFormatVisitor stringVisitor = Mockito.mock(JsonStringFormatVisitor.class);
        JavaType typeHint = TypeFactory.defaultInstance().constructType(TestEnum.class);

        Mockito.when(visitor.expectStringFormat(typeHint)).thenReturn(stringVisitor);

        serializer.acceptJsonFormatVisitor(visitor, typeHint);
        Mockito.verify(stringVisitor).enumTypes(Mockito.anySet());
    }

    @Test(expected = JsonMappingException.class)
    public void testAcceptJsonFormatVisitorForBrokenEnum() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(BrokenEnum.class, "toVal");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JsonStringFormatVisitor stringVisitor = Mockito.mock(JsonStringFormatVisitor.class);
        JavaType typeHint = TypeFactory.defaultInstance().constructType(BrokenEnum.class);

        Mockito.when(visitor.expectStringFormat(typeHint)).thenReturn(stringVisitor);

        serializer.acceptJsonFormatVisitor(visitor, typeHint);
    }

    @Test(expected = StackOverflowError.class)
    public void testAcceptJsonFormatVisitorForErrorEnum() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(ErrorEnum.class, "toVal");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        JsonFormatVisitorWrapper visitor = Mockito.mock(JsonFormatVisitorWrapper.class);
        JsonStringFormatVisitor stringVisitor = Mockito.mock(JsonStringFormatVisitor.class);
        JavaType typeHint = TypeFactory.defaultInstance().constructType(ErrorEnum.class);

        Mockito.when(visitor.expectStringFormat(typeHint)).thenReturn(stringVisitor);

        serializer.acceptJsonFormatVisitor(visitor, typeHint);
    }

    @Test
    public void testIsNaturalTypeWithStdHandling() throws Exception {
        AnnotatedMethod am = createAnnotatedMethod(SimpleBean.class, "fetchValue");
        JsonValueSerializer serializer = new JsonValueSerializer(am, null);

        JsonSerializer<Object> stdSer = new StdSerializer<Object>(String.class) {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) {}
        };

        Assert.assertTrue(serializer.isNaturalTypeWithStdHandling(String.class, stdSer));
        Assert.assertTrue(serializer.isNaturalTypeWithStdHandling(Integer.class, stdSer));
        Assert.assertTrue(serializer.isNaturalTypeWithStdHandling(Boolean.class, stdSer));
        Assert.assertTrue(serializer.isNaturalTypeWithStdHandling(Double.class, stdSer));
        Assert.assertTrue(serializer.isNaturalTypeWithStdHandling(Integer.TYPE, stdSer));
        Assert.assertTrue(serializer.isNaturalTypeWithStdHandling(Boolean.TYPE, stdSer));
        Assert.assertTrue(serializer.isNaturalTypeWithStdHandling(Double.TYPE, stdSer));

        Assert.assertFalse(serializer.isNaturalTypeWithStdHandling(Long.TYPE, stdSer));
        Assert.assertFalse(serializer.isNaturalTypeWithStdHandling(Object.class, stdSer));
        Assert.assertFalse(serializer.isNaturalTypeWithStdHandling(Long.class, stdSer));

        JsonSerializer<Object> nonStdSer = Mockito.mock(JsonSerializer.class);
        Assert.assertFalse(serializer.isNaturalTypeWithStdHandling(String.class, nonStdSer));
    }
}
