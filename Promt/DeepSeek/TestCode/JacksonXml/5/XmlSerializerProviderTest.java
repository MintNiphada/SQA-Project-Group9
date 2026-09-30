package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.lang.reflect.Field;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlSerializerProviderTest {

    private final XmlMapper xmlMapper = new XmlMapper();
    private final XmlRootNameLookup rootNameLookup = new XmlRootNameLookup();

    @Test
    public void testConstructorWithRootNames() {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        Assert.assertNotNull(provider);
    }

    @Test
    public void testCopyConstructor() {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        XmlSerializerProvider copy = new XmlSerializerProvider(provider);
        Assert.assertNotNull(copy);
    }

    @Test
    public void testCopyMethod() {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        DefaultSerializerProvider copy = provider.copy();
        Assert.assertNotNull(copy);
        Assert.assertTrue(copy instanceof XmlSerializerProvider);
    }

    @Test
    public void testCreateInstance() {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        SerializationConfig config = xmlMapper.getSerializationConfig();
        SerializerFactory factory = xmlMapper.getSerializerProviderInstance().getFactory();
        DefaultSerializerProvider instance = provider.createInstance(config, factory);
        Assert.assertNotNull(instance);
        Assert.assertTrue(instance instanceof XmlSerializerProvider);
    }

    @Test
    public void testSerializeValueNull() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        provider.serializeValue(gen, null);
        gen.flush();
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeValueNullWithConfigRootName() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        SerializationConfig config = xmlMapper.getSerializationConfig().withRootName("customNull");
        XmlSerializerProvider customProvider = new XmlSerializerProvider(provider, config, provider.getFactory());
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        customProvider.serializeValue(gen, null);
        gen.flush();
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeValueWithRootNameFromConfig() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        SerializationConfig config = xmlMapper.getSerializationConfig().withRootName("configRoot");
        XmlSerializerProvider customProvider = new XmlSerializerProvider(provider, config, provider.getFactory());
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        customProvider.serializeValue(gen, "testValue");
        gen.flush();
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeValueWithIndexedType() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        provider.serializeValue(gen, new String[]{"a", "b"});
        gen.flush();
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeValueWithTokenBuffer() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        TokenBuffer buf = new TokenBuffer(null, false);
        provider.serializeValue(buf, "test");
        Assert.assertNotNull(buf);
    }

    @Test(expected = JsonMappingException.class)
    public void testSerializeValueWithNonXmlGenerator() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        JsonGenerator gen = new com.fasterxml.jackson.core.base.GeneratorBase(0, null) {
            @Override
            public void flush() throws IOException {}
            @Override
            protected void _releaseBuffers() {}
            @Override
            protected void _verifyValueWrite(String typeMsg) throws IOException {}
            @Override
            public void writeStartArray() throws IOException {}
            @Override
            public void writeEndArray() throws IOException {}
            @Override
            public void writeStartObject() throws IOException {}
            @Override
            public void writeEndObject() throws IOException {}
            @Override
            public void writeFieldName(String name) throws IOException {}
            @Override
            public void writeString(String text) throws IOException {}
            @Override
            public void writeString(char[] text, int offset, int len) throws IOException {}
            @Override
            public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override
            public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override
            public void writeRaw(String text) throws IOException {}
            @Override
            public void writeRaw(String text, int offset, int len) throws IOException {}
            @Override
            public void writeRaw(char[] text, int offset, int len) throws IOException {}
            @Override
            public void writeRaw(char c) throws IOException {}
            @Override
            public void writeBinary(com.fasterxml.jackson.core.Base64Variant bv, byte[] data, int offset, int len) throws IOException {}
            @Override
            public void writeNumber(int v) throws IOException {}
            @Override
            public void writeNumber(long v) throws IOException {}
            @Override
            public void writeNumber(java.math.BigInteger v) throws IOException {}
            @Override
            public void writeNumber(double v) throws IOException {}
            @Override
            public void writeNumber(float v) throws IOException {}
            @Override
            public void writeNumber(java.math.BigDecimal v) throws IOException {}
            @Override
            public void writeBoolean(boolean state) throws IOException {}
            @Override
            public void writeNull() throws IOException {}
        };
        provider.serializeValue(gen, "test");
    }

    @Test
    public void testSerializeValueWithJavaTypeAndSerializer() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonSerializer<Object> ser = provider.findTypedValueSerializer(type, true, null);
        provider.serializeValue(gen, "testValue", type, ser);
        gen.flush();
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeValueWithJavaTypeNullValue() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        provider.serializeValue(gen, null, type, null);
        gen.flush();
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeValueWithJavaTypeAndNullSerializer() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        provider.serializeValue(gen, "testValue", type, null);
        gen.flush();
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeValueWithJavaTypeIndexed() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        JavaType type = TypeFactory.defaultInstance().constructArrayType(String.class);
        provider.serializeValue(gen, new String[]{"a", "b"}, type, null);
        gen.flush();
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeValueWithJavaTypeAndTokenBuffer() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        TokenBuffer buf = new TokenBuffer(null, false);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        provider.serializeValue(buf, "test", type, null);
        Assert.assertNotNull(buf);
    }

    @Test
    public void testRootNameFromConfigNull() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        Field configField = DefaultSerializerProvider.class.getDeclaredField("_config");
        configField.setAccessible(true);
        SerializationConfig config = (SerializationConfig) configField.get(provider);
        Assert.assertNotNull(config);
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_rootNameFromConfig");
        method.setAccessible(true);
        QName result = (QName) method.invoke(provider);
        Assert.assertNull(result);
    }

    @Test
    public void testRootNameFromConfigWithSimpleName() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        SerializationConfig config = xmlMapper.getSerializationConfig().withRootName("simpleRoot");
        XmlSerializerProvider customProvider = new XmlSerializerProvider(provider, config, provider.getFactory());
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_rootNameFromConfig");
        method.setAccessible(true);
        QName result = (QName) method.invoke(customProvider);
        Assert.assertNotNull(result);
        Assert.assertEquals("simpleRoot", result.getLocalPart());
    }

    @Test
    public void testRootNameFromConfigWithNamespace() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        SerializationConfig config = xmlMapper.getSerializationConfig().withRootName(PropertyName.construct("nsRoot", "http://example.com"));
        XmlSerializerProvider customProvider = new XmlSerializerProvider(provider, config, provider.getFactory());
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_rootNameFromConfig");
        method.setAccessible(true);
        QName result = (QName) method.invoke(customProvider);
        Assert.assertNotNull(result);
        Assert.assertEquals("nsRoot", result.getLocalPart());
        Assert.assertEquals("http://example.com", result.getNamespaceURI());
    }

    @Test
    public void testAsXmlGeneratorWithToXmlGenerator() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_asXmlGenerator", JsonGenerator.class);
        method.setAccessible(true);
        Object result = method.invoke(provider, gen);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof ToXmlGenerator);
    }

    @Test
    public void testAsXmlGeneratorWithTokenBuffer() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        TokenBuffer buf = new TokenBuffer(null, false);
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_asXmlGenerator", JsonGenerator.class);
        method.setAccessible(true);
        Object result = method.invoke(provider, buf);
        Assert.assertNull(result);
    }

    @Test(expected = java.lang.reflect.InvocationTargetException.class)
    public void testAsXmlGeneratorWithInvalidGenerator() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        JsonGenerator gen = new com.fasterxml.jackson.core.base.GeneratorBase(0, null) {
            @Override
            public void flush() throws IOException {}
            @Override
            protected void _releaseBuffers() {}
            @Override
            protected void _verifyValueWrite(String typeMsg) throws IOException {}
            @Override
            public void writeStartArray() throws IOException {}
            @Override
            public void writeEndArray() throws IOException {}
            @Override
            public void writeStartObject() throws IOException {}
            @Override
            public void writeEndObject() throws IOException {}
            @Override
            public void writeFieldName(String name) throws IOException {}
            @Override
            public void writeString(String text) throws IOException {}
            @Override
            public void writeString(char[] text, int offset, int len) throws IOException {}
            @Override
            public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override
            public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override
            public void writeRaw(String text) throws IOException {}
            @Override
            public void writeRaw(String text, int offset, int len) throws IOException {}
            @Override
            public void writeRaw(char[] text, int offset, int len) throws IOException {}
            @Override
            public void writeRaw(char c) throws IOException {}
            @Override
            public void writeBinary(com.fasterxml.jackson.core.Base64Variant bv, byte[] data, int offset, int len) throws IOException {}
            @Override
            public void writeNumber(int v) throws IOException {}
            @Override
            public void writeNumber(long v) throws IOException {}
            @Override
            public void writeNumber(java.math.BigInteger v) throws IOException {}
            @Override
            public void writeNumber(double v) throws IOException {}
            @Override
            public void writeNumber(float v) throws IOException {}
            @Override
            public void writeNumber(java.math.BigDecimal v) throws IOException {}
            @Override
            public void writeBoolean(boolean state) throws IOException {}
            @Override
            public void writeNull() throws IOException {}
        };
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_asXmlGenerator", JsonGenerator.class);
        method.setAccessible(true);
        method.invoke(provider, gen);
    }

    @Test
    public void testWrapAsIOEWithIOException() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        IOException ioException = new IOException("test IO");
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_wrapAsIOE", JsonGenerator.class, Exception.class);
        method.setAccessible(true);
        IOException result = (IOException) method.invoke(provider, gen, ioException);
        Assert.assertSame(ioException, result);
    }

    @Test
    public void testWrapAsIOEWithRuntimeException() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        RuntimeException re = new RuntimeException("test runtime");
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_wrapAsIOE", JsonGenerator.class, Exception.class);
        method.setAccessible(true);
        IOException result = (IOException) method.invoke(provider, gen, re);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof JsonMappingException);
        Assert.assertEquals("test runtime", result.getMessage());
    }

    @Test
    public void testWrapAsIOEWithNullMessageException() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        Exception e = new Exception();
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_wrapAsIOE", JsonGenerator.class, Exception.class);
        method.setAccessible(true);
        IOException result = (IOException) method.invoke(provider, gen, e);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof JsonMappingException);
        Assert.assertTrue(result.getMessage().contains("no message for"));
    }

    @Test
    public void testInitWithRootNameMissingName() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        QName rootName = new QName("testRoot");
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_initWithRootName", ToXmlGenerator.class, QName.class);
        method.setAccessible(true);
        method.invoke(provider, gen, rootName);
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testInitWithRootNameWithNamespace() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        QName rootName = new QName("http://example.com", "testRoot");
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_initWithRootName", ToXmlGenerator.class, QName.class);
        method.setAccessible(true);
        method.invoke(provider, gen, rootName);
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testStartRootArray() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        QName rootName = new QName("arrayRoot");
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_startRootArray", ToXmlGenerator.class, QName.class);
        method.setAccessible(true);
        method.invoke(provider, gen, rootName);
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeXmlNullWithConfigRootName() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        SerializationConfig config = xmlMapper.getSerializationConfig().withRootName("customNullRoot");
        XmlSerializerProvider customProvider = new XmlSerializerProvider(provider, config, provider.getFactory());
        ToXmlGenerator gen = (ToXmlGenerator) xmlMapper.getFactory().createGenerator(new java.io.StringWriter());
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_serializeXmlNull", JsonGenerator.class);
        method.setAccessible(true);
        method.invoke(customProvider, gen);
        Assert.assertTrue(gen.getOutputContext().inRoot());
    }

    @Test
    public void testSerializeXmlNullWithTokenBuffer() throws Exception {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        TokenBuffer buf = new TokenBuffer(null, false);
        java.lang.reflect.Method method = XmlSerializerProvider.class.getDeclaredMethod("_serializeXmlNull", JsonGenerator.class);
        method.setAccessible(true);
        method.invoke(provider, buf);
        Assert.assertNotNull(buf);
    }

    @Test
    public void testSerializeValueWithRuntimeException() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = new ToXmlGenerator(0, null, null, null) {
            @Override
            public void writeStartObject() throws IOException {
                throw new RuntimeException("forced error");
            }
            @Override
            public void writeEndObject() throws IOException {}
            @Override
            public void writeFieldName(String name) throws IOException {}
            @Override
            public void writeString(String text) throws IOException {}
            @Override
            public void writeString(char[] text, int offset, int len) throws IOException {}
            @Override
            public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override
            public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override
            public void writeRaw(String text) throws IOException {}
            @Override
            public void writeRaw(String text, int offset, int len) throws IOException {}
            @Override
            public void writeRaw(char[] text, int offset, int len) throws IOException {}
            @Override
            public void writeRaw(char c) throws IOException {}
            @Override
            public void writeBinary(com.fasterxml.jackson.core.Base64Variant bv, byte[] data, int offset, int len) throws IOException {}
            @Override
            public void writeNumber(int v) throws IOException {}
            @Override
            public void writeNumber(long v) throws IOException {}
            @Override
            public void writeNumber(java.math.BigInteger v) throws IOException {}
            @Override
            public void writeNumber(double v) throws IOException {}
            @Override
            public void writeNumber(float v) throws IOException {}
            @Override
            public void writeNumber(java.math.BigDecimal v) throws IOException {}
            @Override
            public void writeBoolean(boolean state) throws IOException {}
            @Override
            public void writeNull() throws IOException {}
            @Override
            public void flush() throws IOException {}
            @Override
            protected void _releaseBuffers() {}
            @Override
            protected void _verifyValueWrite(String typeMsg) throws IOException {}
            @Override
            public void writeStartArray() throws IOException {}
            @Override
            public void writeEndArray() throws IOException {}
        };
        try {
            provider.serializeValue(gen, "test");
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertTrue(e instanceof JsonMappingException);
            Assert.assertTrue(e.getMessage().contains("forced error"));
        }
    }

    @Test
    public void testSerializeValueWithJavaTypeRuntimeException() throws IOException {
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNameLookup);
        ToXmlGenerator gen = new ToXmlGenerator(0, null, null, null) {
            @Override
            public void writeStartObject() throws IOException {
                throw new RuntimeException("forced error in type");
            }
            @Override
            public void writeEndObject() throws IOException {}
            @Override
            public void writeFieldName(String name) throws IOException {}
            @Override
            public void writeString(String text) throws IOException {}
            @Override
            public void writeString(char[] text, int offset, int len) throws IOException {}
            @Override
            public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override
            public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override
            public void writeRaw(String text) throws IOException {}
            @Override
            public void writeRaw(String text, int offset, int len) throws IOException {}
            @Override
            public void writeRaw(char[] text, int offset, int len) throws IOException {}
            @Override
            public void writeRaw(char c) throws IOException {}
            @Override
            public void writeBinary(com.fasterxml.jackson.core.Base64Variant bv, byte[] data, int offset, int len) throws IOException {}
            @Override
            public void writeNumber(int v) throws IOException {}
            @Override
            public void writeNumber(long v) throws IOException {}
            @Override
            public void writeNumber(java.math.BigInteger v) throws IOException {}
            @Override
            public void writeNumber(double v) throws IOException {}
            @Override
            public void writeNumber(float v) throws IOException {}
            @Override
            public void writeNumber(java.math.BigDecimal v) throws IOException {}
            @Override
            public void writeBoolean(boolean state) throws IOException {}
            @Override
            public void writeNull() throws IOException {}
            @Override
            public void flush() throws IOException {}
            @Override
            protected void _releaseBuffers() {}
            @Override
            protected void _verifyValueWrite(String typeMsg) throws IOException {}
            @Override
            public void writeStartArray() throws IOException {}
            @Override
            public void writeEndArray() throws IOException {}
        };
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        try {
            provider.serializeValue(gen, "test", type, null);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertTrue(e instanceof JsonMappingException);
            Assert.assertTrue(e.getMessage().contains("forced error in type"));
        }
    }
}
