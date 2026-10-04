package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.impl.*;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BeanDeserializerTest {

    public static class TestBean {
        public int a;
        public String b;
    }

    private ObjectMapper mapper;
    private DeserializationContext ctxt;
    private BeanDeserializer deser;
    private JavaType type;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        type = mapper.constructType(TestBean.class);
        Field f = ObjectMapper.class.getDeclaredField("_deserializationContext");
        f.setAccessible(true);
        ctxt = (DeserializationContext) f.get(mapper);
        deser = (BeanDeserializer) ctxt.findRootValueDeserializer(type);
    }

    private JsonParser createParser(String json) throws IOException {
        return mapper.getFactory().createParser(json);
    }

    private JsonParser createParserFromTokens(TokenBuffer buf) throws IOException {
        return buf.asParser();
    }

    @Test
    public void testDeserializeStartObjectVanilla() throws Exception {
        Field vanillaField = BeanDeserializerBase.class.getDeclaredField("_vanillaProcessing");
        vanillaField.setAccessible(true);
        vanillaField.setBoolean(deser, true);
        JsonParser p = createParser("{\"a\":1}");
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertTrue(result instanceof TestBean);
        assertEquals(1, ((TestBean)result).a);
    }

    @Test
    public void testDeserializeStartObjectNonVanilla() throws Exception {
        Field vanillaField = BeanDeserializerBase.class.getDeclaredField("_vanillaProcessing");
        vanillaField.setAccessible(true);
        vanillaField.setBoolean(deser, false);
        JsonParser p = createParser("{\"a\":1}");
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertEquals(1, ((TestBean)result).a);
    }

    @Test
    public void testDeserializeStartObjectWithObjectId() throws Exception {
        Field oirField = BeanDeserializerBase.class.getDeclaredField("_objectIdReader");
        oirField.setAccessible(true);
        ObjectIdReader oir = ObjectIdReader.construct(type, "id", null, null, null);
        oirField.set(deser, oir);
        JsonParser p = createParser("{\"a\":1}");
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
    }

    @Test
    public void testDeserializeOtherString() throws Exception {
        JsonParser p = createParser("\"text\"");
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNull(result);
    }

    @Test
    public void testDeserializeOtherNumberInt() throws Exception {
        JsonParser p = createParser("123");
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNull(result);
    }

    @Test
    public void testDeserializeOtherNumberFloat() throws Exception {
        JsonParser p = createParser("1.5");
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNull(result);
    }

    @Test
    public void testDeserializeOtherBoolean() throws Exception {
        JsonParser p = createParser("true");
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNull(result);
    }

    @Test
    public void testDeserializeOtherArray() throws Exception {
        JsonParser p = createParser("[1]");
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNull(result);
    }

    @Test
    public void testDeserializeOtherFieldNameVanilla() throws Exception {
        Field vanillaField = BeanDeserializerBase.class.getDeclaredField("_vanillaProcessing");
        vanillaField.setAccessible(true);
        vanillaField.setBoolean(deser, true);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(1);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertEquals(1, ((TestBean)result).a);
    }

    @Test
    public void testDeserializeOtherFieldNameNonVanilla() throws Exception {
        Field vanillaField = BeanDeserializerBase.class.getDeclaredField("_vanillaProcessing");
        vanillaField.setAccessible(true);
        vanillaField.setBoolean(deser, false);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(1);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertEquals(1, ((TestBean)result).a);
    }

    @Test
    public void testDeserializeOtherEndObjectVanilla() throws Exception {
        Field vanillaField = BeanDeserializerBase.class.getDeclaredField("_vanillaProcessing");
        vanillaField.setAccessible(true);
        vanillaField.setBoolean(deser, true);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
    }

    @Test
    public void testDeserializeOtherEndObjectNonVanilla() throws Exception {
        Field vanillaField = BeanDeserializerBase.class.getDeclaredField("_vanillaProcessing");
        vanillaField.setAccessible(true);
        vanillaField.setBoolean(deser, false);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
    }

    @Test(expected = JsonMappingException.class)
    public void testDeserializeOtherInvalidToken() throws Exception {
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartArray();
        buf.writeEndArray();
        JsonParser p = buf.asParser();
        p.nextToken();
        p.nextToken();
        deser.deserialize(p, ctxt);
    }

    @Test
    public void testDeserializeWithBean() throws Exception {
        TestBean bean = new TestBean();
        JsonParser p = createParser("{\"a\":2}");
        Object result = deser.deserialize(p, ctxt, bean);
        assertSame(bean, result);
        assertEquals(2, bean.a);
    }

    @Test
    public void testDeserializeWithBeanEmptyObject() throws Exception {
        TestBean bean = new TestBean();
        JsonParser p = createParser("{}");
        Object result = deser.deserialize(p, ctxt, bean);
        assertSame(bean, result);
    }

    @Test
    public void testDeserializeWithBeanNoFieldName() throws Exception {
        TestBean bean = new TestBean();
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt, bean);
        assertSame(bean, result);
    }

    @Test
    public void testDeserializeWithBeanViewProcessing() throws Exception {
        Field viewField = BeanDeserializerBase.class.getDeclaredField("_needViewProcesing");
        viewField.setAccessible(true);
        viewField.setBoolean(deser, true);
        TestBean bean = new TestBean();
        JsonParser p = createParser("{\"a\":3}");
        Object result = deser.deserialize(p, ctxt, bean);
        assertSame(bean, result);
        assertEquals(3, bean.a);
    }

    @Test
    public void testUnwrappingDeserializer() throws Exception {
        NameTransformer unwrapper = NameTransformer.simpleTransformer("prefix");
        JsonDeserializer<?> result = deser.unwrappingDeserializer(unwrapper);
        assertNotNull(result);
        assertTrue(result instanceof BeanDeserializer);
    }

    @Test
    public void testWithObjectIdReader() throws Exception {
        ObjectIdReader oir = ObjectIdReader.construct(type, "id", null, null, null);
        BeanDeserializer newDeser = deser.withObjectIdReader(oir);
        assertNotNull(newDeser);
        assertNotSame(deser, newDeser);
    }

    @Test
    public void testWithIgnorableProperties() throws Exception {
        HashSet<String> ignorable = new HashSet<String>();
        ignorable.add("x");
        BeanDeserializer newDeser = deser.withIgnorableProperties(ignorable);
        assertNotNull(newDeser);
        assertNotSame(deser, newDeser);
    }

    @Test
    public void testAsArrayDeserializer() throws Exception {
        BeanDeserializerBase arrayDeser = deser.asArrayDeserializer();
        assertNotNull(arrayDeser);
        assertTrue(arrayDeser instanceof BeanAsArrayDeserializer);
    }

    @Test
    public void testVanillaDeserialize() throws Exception {
        Field vanillaField = BeanDeserializerBase.class.getDeclaredField("_vanillaProcessing");
        vanillaField.setAccessible(true);
        vanillaField.setBoolean(deser, true);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(5);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertEquals(5, ((TestBean)result).a);
    }

    @Test
    public void testDeserializeFromObjectWithObjectId() throws Exception {
        Field oirField = BeanDeserializerBase.class.getDeclaredField("_objectIdReader");
        oirField.setAccessible(true);
        ObjectIdReader oir = ObjectIdReader.construct(type, "id", null, null, null);
        oirField.set(deser, oir);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("id");
        buf.writeString("123");
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
    }

    @Test
    public void testDeserializeFromObjectNonStandardCreation() throws Exception {
        Field nonStdField = BeanDeserializerBase.class.getDeclaredField("_nonStandardCreation");
        nonStdField.setAccessible(true);
        nonStdField.setBoolean(deser, true);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(7);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertEquals(7, ((TestBean)result).a);
    }

    @Test
    public void testDeserializeFromObjectWithInjectables() throws Exception {
        Field injectField = BeanDeserializerBase.class.getDeclaredField("_injectables");
        injectField.setAccessible(true);
        injectField.set(deser, new InjectableValues.Std().addValue("x", "injected"));
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(8);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertEquals(8, ((TestBean)result).a);
    }

    @Test
    public void testDeserializeWithView() throws Exception {
        Field viewField = BeanDeserializerBase.class.getDeclaredField("_needViewProcesing");
        viewField.setAccessible(true);
        viewField.setBoolean(deser, true);
        TestBean bean = new TestBean();
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(9);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt, bean);
        assertSame(bean, result);
        assertEquals(9, bean.a);
    }

    @Test
    public void testDeserializeWithUnwrapped() throws Exception {
        Field unwrappedField = BeanDeserializerBase.class.getDeclaredField("_unwrappedPropertyHandler");
        unwrappedField.setAccessible(true);
        UnwrappedPropertyHandler handler = new UnwrappedPropertyHandler();
        unwrappedField.set(deser, handler);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(10);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertEquals(10, ((TestBean)result).a);
    }

    @Test
    public void testDeserializeWithExternalTypeId() throws Exception {
        Field extField = BeanDeserializerBase.class.getDeclaredField("_externalTypeIdHandler");
        extField.setAccessible(true);
        ExternalTypeHandler extHandler = ExternalTypeHandler.builder(null).build();
        extField.set(deser, extHandler);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(11);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt);
        assertNotNull(result);
        assertEquals(11, ((TestBean)result).a);
    }

    @Test
    public void testDeserializeUsingPropertyBased() throws Exception {
        Field propCreatorField = BeanDeserializerBase.class.getDeclaredField("_propertyBasedCreator");
        propCreatorField.setAccessible(true);
        PropertyBasedCreator creator = new PropertyBasedCreator(null, null, null, false, null);
        propCreatorField.set(deser, creator);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(12);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        try {
            deser.deserialize(p, ctxt);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testDeserializeWithUnwrappedBean() throws Exception {
        Field unwrappedField = BeanDeserializerBase.class.getDeclaredField("_unwrappedPropertyHandler");
        unwrappedField.setAccessible(true);
        UnwrappedPropertyHandler handler = new UnwrappedPropertyHandler();
        unwrappedField.set(deser, handler);
        TestBean bean = new TestBean();
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(13);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt, bean);
        assertSame(bean, result);
        assertEquals(13, bean.a);
    }

    @Test
    public void testDeserializeUsingPropertyBasedWithUnwrapped() throws Exception {
        Field propCreatorField = BeanDeserializerBase.class.getDeclaredField("_propertyBasedCreator");
        propCreatorField.setAccessible(true);
        PropertyBasedCreator creator = new PropertyBasedCreator(null, null, null, false, null);
        propCreatorField.set(deser, creator);
        Field unwrappedField = BeanDeserializerBase.class.getDeclaredField("_unwrappedPropertyHandler");
        unwrappedField.setAccessible(true);
        UnwrappedPropertyHandler handler = new UnwrappedPropertyHandler();
        unwrappedField.set(deser, handler);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(14);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        try {
            deser.deserialize(p, ctxt);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testDeserializeWithExternalTypeIdBean() throws Exception {
        Field extField = BeanDeserializerBase.class.getDeclaredField("_externalTypeIdHandler");
        extField.setAccessible(true);
        ExternalTypeHandler extHandler = ExternalTypeHandler.builder(null).build();
        extField.set(deser, extHandler);
        TestBean bean = new TestBean();
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(15);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        Object result = deser.deserialize(p, ctxt, bean);
        assertSame(bean, result);
        assertEquals(15, bean.a);
    }

    @Test
    public void testDeserializeUsingPropertyBasedWithExternalTypeId() throws Exception {
        Field propCreatorField = BeanDeserializerBase.class.getDeclaredField("_propertyBasedCreator");
        propCreatorField.setAccessible(true);
        PropertyBasedCreator creator = new PropertyBasedCreator(null, null, null, false, null);
        propCreatorField.set(deser, creator);
        Field extField = BeanDeserializerBase.class.getDeclaredField("_externalTypeIdHandler");
        extField.setAccessible(true);
        ExternalTypeHandler extHandler = ExternalTypeHandler.builder(null).build();
        extField.set(deser, extHandler);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeFieldName("a");
        buf.writeNumber(16);
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        try {
            deser.deserialize(p, ctxt);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    @Test
    public void testMissingToken() throws Exception {
        TestableBeanDeserializer testDeser = new TestableBeanDeserializer(deser);
        TokenBuffer buf = new TokenBuffer(null, false);
        buf.writeStartObject();
        buf.writeEndObject();
        JsonParser p = buf.asParser();
        p.nextToken();
        p.nextToken();
        try {
            testDeser.callMissingToken(p, ctxt);
            fail("Expected exception");
        } catch (JsonMappingException e) {
        }
    }

    static class TestableBeanDeserializer extends BeanDeserializer {
        public TestableBeanDeserializer(BeanDeserializerBase src) {
            super(src);
        }
        public Object callMissingToken(JsonParser p, DeserializationContext ctxt) throws IOException {
            return _missingToken(p, ctxt);
        }
    }
}
