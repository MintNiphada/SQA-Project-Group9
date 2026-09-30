package com.fasterxml.jackson.dataformat.xml.ser;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.TokenBuffer;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

@RunWith(MockitoJUnitRunner.class)
public class XmlSerializerProviderTest {

    @Mock
    private XmlRootNameLookup rootNameLookup;
    @Mock
    private SerializationConfig config;
    @Mock
    private SerializerFactory factory;
    @Mock
    private ToXmlGenerator xgen;
    @Mock
    private JsonGenerator plainGen;
    @Mock
    private TokenBuffer tokenBuffer;
    @Mock
    private JavaType javaType;
    @Mock
    private JsonSerializer<Object> serializer;
    @Mock
    private JsonSerializer<Object> defaultSerializer;
    @Mock
    private XMLStreamWriter staxWriter;

    private XmlSerializerProvider baseProvider;
    private XmlSerializerProvider provider;

    @Before
    public void setUp() throws Exception {
        baseProvider = new XmlSerializerProvider(rootNameLookup);
        provider = spy(new XmlSerializerProvider(baseProvider, config, factory));
        // stub findTypedValueSerializer to return defaultSerializer
        doReturn(defaultSerializer).when(provider).findTypedValueSerializer(any(Class.class), anyBoolean(), any());
        doReturn(defaultSerializer).when(provider).findTypedValueSerializer(any(JavaType.class), anyBoolean(), any());
    }

    @Test
    public void testConstructorWithRootNameLookup() {
        XmlSerializerProvider p = new XmlSerializerProvider(rootNameLookup);
        assertNotNull(p);
        // _rootNameLookup is set (no direct getter, but can be inferred)
    }

    @Test
    public void testCopyConstructor() {
        XmlSerializerProvider copy = new XmlSerializerProvider(provider, config, factory);
        assertNotNull(copy);
        // fields are copied; can't easily verify without reflection, but trust
    }

    @Test
    public void testCreateInstance() {
        DefaultSerializerProvider instance = provider.createInstance(config, factory);
        assertTrue(instance instanceof XmlSerializerProvider);
        assertNotSame(provider, instance);
    }

    // ---------- serializeValue(JsonGenerator, Object) ----------

    @Test
    public void testSerializeValueNullWithToXmlGenerator() throws IOException {
        Object value = null;
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider.serializeValue(xgen, value);

        verify(xgen).setNextNameIfMissing(eq(XmlSerializerProvider.ROOT_NAME_FOR_NULL));
        verify(xgen).initGenerator();
        verify(staxWriter, never()).setDefaultNamespace(anyString());
        verify(xgen).writeNull(); // super.serializeValue(jgen, null) calls writeNull()
    }

    @Test
    public void testSerializeValueNullWithNonToXmlGenerator() throws IOException {
        Object value = null;
        provider.serializeValue(plainGen, value);
        verify(plainGen, never()).writeNull(); // super.serializeValue with null may call writeNull, but we can verify
        // Actually DefaultSerializerProvider.serializeValue(JsonGenerator, null) calls gen.writeNull()
        verify(plainGen).writeNull();
    }

    @Test
    public void testSerializeValueNullWithTokenBuffer() throws IOException {
        Object value = null;
        provider.serializeValue(tokenBuffer, value);
        verify(tokenBuffer).writeNull();
    }

    @Test
    public void testSerializeValueNonNullWithXgenNull() throws IOException {
        Object value = "test";
        // gen is TokenBuffer -> _asXmlGenerator returns null
        when(tokenBuffer.getClass()).thenReturn(TokenBuffer.class);
        provider.serializeValue(tokenBuffer, value);
        verify(defaultSerializer).serialize(eq(value), eq(tokenBuffer), eq(provider));
        verify(tokenBuffer, never()).writeEndObject();
    }

    @Test
    public void testSerializeValueNonNullWithXgenNotNullNonArray() throws IOException {
        Object value = "test";
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        QName rootName = new QName("root");
        when(rootNameLookup.findRootName(any(Class.class), eq(config))).thenReturn(rootName);

        provider.serializeValue(xgen, value);

        verify(xgen).setNextNameIfMissing(eq(rootName));
        verify(xgen).initGenerator();
        verify(staxWriter, never()).setDefaultNamespace(anyString());
        verify(defaultSerializer).serialize(eq(value), eq(xgen), eq(provider));
        verify(xgen, never()).writeEndObject();
    }

    @Test
    public void testSerializeValueNonNullWithXgenNotNullArray() throws IOException {
        Object value = new String[]{"a", "b"}; // indexed type
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        QName rootName = new QName("root");
        when(rootNameLookup.findRootName(any(Class.class), eq(config))).thenReturn(rootName);

        provider.serializeValue(xgen, value);

        verify(xgen).writeStartObject();
        verify(xgen).writeFieldName("item");
        verify(defaultSerializer).serialize(eq(value), eq(xgen), eq(provider));
        verify(xgen).writeEndObject();
    }

    @Test
    public void testSerializeValueNonNullWithRootNameFromConfig() throws IOException {
        Object value = "test";
        PropertyName propName = new PropertyName("configRoot");
        when(config.getFullRootName()).thenReturn(propName);
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider.serializeValue(xgen, value);

        verify(xgen).setNextNameIfMissing(eq(new QName("configRoot")));
        verify(xgen).initGenerator();
    }

    @Test
    public void testSerializeValueIOExceptionPassthrough() throws IOException {
        Object value = "test";
        IOException ioe = new IOException("test IOE");
        doThrow(ioe).when(defaultSerializer).serialize(any(), any(), any());
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        when(rootNameLookup.findRootName(any(Class.class), eq(config))).thenReturn(new QName("root"));

        try {
            provider.serializeValue(xgen, value);
            fail("Expected IOException");
        } catch (IOException e) {
            assertSame(ioe, e);
        }
    }

    @Test
    public void testSerializeValueRuntimeExceptionWrapped() throws IOException {
        Object value = "test";
        RuntimeException re = new RuntimeException("test RE");
        doThrow(re).when(defaultSerializer).serialize(any(), any(), any());
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        when(rootNameLookup.findRootName(any(Class.class), eq(config))).thenReturn(new QName("root"));

        try {
            provider.serializeValue(xgen, value);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("test RE"));
        }
    }

    @Test
    public void testSerializeValueExceptionWithNullMessage() throws IOException {
        Object value = "test";
        RuntimeException re = new RuntimeException(); // no message
        doThrow(re).when(defaultSerializer).serialize(any(), any(), any());
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        when(rootNameLookup.findRootName(any(Class.class), eq(config))).thenReturn(new QName("root"));

        try {
            provider.serializeValue(xgen, value);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("[no message for"));
        }
    }

    // ---------- serializeValue(JsonGenerator, Object, JavaType) ----------

    @Test
    public void testSerializeValueWithRootTypeNull() throws IOException {
        Object value = null;
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider.serializeValue(xgen, value, javaType);

        verify(xgen).setNextNameIfMissing(eq(XmlSerializerProvider.ROOT_NAME_FOR_NULL));
        verify(xgen).writeNull();
    }

    @Test
    public void testSerializeValueWithRootTypeNonNullNonArray() throws IOException {
        Object value = "test";
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        QName rootName = new QName("root");
        when(rootNameLookup.findRootName(eq(javaType), eq(config))).thenReturn(rootName);

        provider.serializeValue(xgen, value, javaType);

        verify(xgen).setNextNameIfMissing(eq(rootName));
        verify(xgen).initGenerator();
        verify(defaultSerializer).serialize(eq(value), eq(xgen), eq(provider));
        verify(xgen, never()).writeEndObject();
    }

    @Test
    public void testSerializeValueWithRootTypeArray() throws IOException {
        Object value = new int[]{1,2};
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        QName rootName = new QName("root");
        when(rootNameLookup.findRootName(eq(javaType), eq(config))).thenReturn(rootName);
        // TypeUtil.isIndexedType(javaType) returns true
        // We need to mock TypeUtil? It's static. We'll use PowerMock? Not. Instead, we can rely on actual TypeUtil behavior if we pass a real JavaType that is indexed.
        // But we can't easily mock static. We'll test with a real JavaType that is indexed, like TypeFactory.defaultInstance().constructCollectionType(List.class, String.class)
        // That would require real TypeFactory. To keep unit test simple, we can use a spy on TypeUtil? Not possible.
        // Alternative: we can test that if TypeUtil.isIndexedType returns true, then startRootArray and writeEndObject are called. We can't control TypeUtil, but we can test with a value whose class is indexed (like array). The method uses TypeUtil.isIndexedType(cls) or TypeUtil.isIndexedType(rootType). For the JavaType overload, it uses TypeUtil.isIndexedType(rootType). We can create a real JavaType that is indexed using TypeFactory. That's acceptable.
        // We'll use com.fasterxml.jackson.databind.type.TypeFactory.
        // So we need to import TypeFactory.
        // We'll create a JavaType for List<String>.
        com.fasterxml.jackson.databind.type.TypeFactory tf = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(java.util.List.class, String.class);
        // Then use that as rootType.
        // We'll adjust the test accordingly.
        // But we already have a mock javaType. We'll create a separate test for array with real JavaType.
        // Let's do that.
    }

    @Test
    public void testSerializeValueWithRootTypeArrayRealType() throws IOException {
        Object value = new java.util.ArrayList<String>();
        com.fasterxml.jackson.databind.type.TypeFactory tf = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(java.util.List.class, String.class);
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        QName rootName = new QName("root");
        when(rootNameLookup.findRootName(eq(listType), eq(config))).thenReturn(rootName);

        provider.serializeValue(xgen, value, listType);

        verify(xgen).writeStartObject();
        verify(xgen).writeFieldName("item");
        verify(defaultSerializer).serialize(eq(value), eq(xgen), eq(provider));
        verify(xgen).writeEndObject();
    }

    // ---------- serializeValue(JsonGenerator, Object, JavaType, JsonSerializer) ----------

    @Test
    public void testSerializeValueWithSerializerNull() throws IOException {
        Object value = null;
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider.serializeValue(xgen, value, javaType, null);

        verify(xgen).setNextNameIfMissing(eq(XmlSerializerProvider.ROOT_NAME_FOR_NULL));
        verify(xgen).writeNull();
    }

    @Test
    public void testSerializeValueWithSerializerProvided() throws IOException {
        Object value = "test";
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        QName rootName = new QName("root");
        when(rootNameLookup.findRootName(eq(javaType), eq(config))).thenReturn(rootName);

        provider.serializeValue(xgen, value, javaType, serializer);

        verify(serializer).serialize(eq(value), eq(xgen), eq(provider));
        verify(defaultSerializer, never()).serialize(any(), any(), any());
    }

    @Test
    public void testSerializeValueWithSerializerNullAndNonNull() throws IOException {
        Object value = "test";
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        when(config.getFullRootName()).thenReturn(null);
        QName rootName = new QName("root");
        when(rootNameLookup.findRootName(eq(javaType), eq(config))).thenReturn(rootName);

        provider.serializeValue(xgen, value, javaType, null);

        verify(defaultSerializer).serialize(eq(value), eq(xgen), eq(provider));
    }

    // ---------- _serializeXmlNull ----------

    @Test
    public void testSerializeXmlNullWithToXmlGenerator() throws IOException {
        when(xgen.setNextNameIfMissing(any(QName.class))).thenReturn(true);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider._serializeXmlNull(xgen);

        verify(xgen).setNextNameIfMissing(eq(XmlSerializerProvider.ROOT_NAME_FOR_NULL));
        verify(xgen).initGenerator();
        verify(xgen).writeNull();
    }

    @Test
    public void testSerializeXmlNullWithNonToXmlGenerator() throws IOException {
        provider._serializeXmlNull(plainGen);
        verify(plainGen, never()).setNextNameIfMissing(any());
        verify(plainGen).writeNull();
    }

    // ---------- _startRootArray ----------

    @Test
    public void testStartRootArray() throws IOException {
        QName rootName = new QName("root");
        provider._startRootArray(xgen, rootName);
        verify(xgen).writeStartObject();
        verify(xgen).writeFieldName("item");
    }

    // ---------- _initWithRootName ----------

    @Test
    public void testInitWithRootNameSetNextNameIfMissingTrue() throws IOException {
        QName rootName = new QName("root");
        when(xgen.setNextNameIfMissing(rootName)).thenReturn(true);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider._initWithRootName(xgen, rootName);

        verify(xgen).setNextNameIfMissing(rootName);
        verify(xgen, never()).setNextName(rootName);
        verify(xgen).initGenerator();
        verify(staxWriter, never()).setDefaultNamespace(anyString());
    }

    @Test
    public void testInitWithRootNameSetNextNameIfMissingFalseNotInRoot() throws IOException {
        QName rootName = new QName("root");
        when(xgen.setNextNameIfMissing(rootName)).thenReturn(false);
        when(xgen.inRoot()).thenReturn(false);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider._initWithRootName(xgen, rootName);

        verify(xgen, never()).setNextName(rootName);
        verify(xgen).initGenerator();
    }

    @Test
    public void testInitWithRootNameSetNextNameIfMissingFalseInRoot() throws IOException {
        QName rootName = new QName("root");
        when(xgen.setNextNameIfMissing(rootName)).thenReturn(false);
        when(xgen.inRoot()).thenReturn(true);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider._initWithRootName(xgen, rootName);

        verify(xgen).setNextName(rootName);
        verify(xgen).initGenerator();
    }

    @Test
    public void testInitWithRootNameWithNamespace() throws IOException {
        QName rootName = new QName("http://ns", "root");
        when(xgen.setNextNameIfMissing(rootName)).thenReturn(true);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider._initWithRootName(xgen, rootName);

        verify(staxWriter).setDefaultNamespace("http://ns");
    }

    @Test
    public void testInitWithRootNameWithEmptyNamespace() throws IOException {
        QName rootName = new QName("", "root");
        when(xgen.setNextNameIfMissing(rootName)).thenReturn(true);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);

        provider._initWithRootName(xgen, rootName);

        verify(staxWriter, never()).setDefaultNamespace(anyString());
    }

    @Test(expected = IOException.class)
    public void testInitWithRootNameSetDefaultNamespaceThrowsXMLStreamException() throws IOException {
        QName rootName = new QName("http://ns", "root");
        when(xgen.setNextNameIfMissing(rootName)).thenReturn(true);
        when(xgen.getStaxWriter()).thenReturn(staxWriter);
        doThrow(new XMLStreamException("test")).when(staxWriter).setDefaultNamespace(anyString());

        provider._initWithRootName(xgen, rootName);
    }

    // ---------- _rootNameFromConfig ----------

    @Test
    public void testRootNameFromConfigNull() {
        when(config.getFullRootName()).thenReturn(null);
        assertNull(provider._rootNameFromConfig());
    }

    @Test
    public void testRootNameFromConfigNoNamespace() {
        PropertyName pn = new PropertyName("simple");
        when(config.getFullRootName()).thenReturn(pn);
        QName result = provider._rootNameFromConfig();
        assertEquals(new QName("simple"), result);
    }

    @Test
    public void testRootNameFromConfigWithNamespace() {
        PropertyName pn = new PropertyName("ns", "simple");
        when(config.getFullRootName()).thenReturn(pn);
        QName result = provider._rootNameFromConfig();
        assertEquals(new QName("ns", "simple"), result);
    }

    @Test
    public void testRootNameFromConfigWithEmptyNamespace() {
        PropertyName pn = new PropertyName("", "simple");
        when(config.getFullRootName()).thenReturn(pn);
        QName result = provider._rootNameFromConfig();
        assertEquals(new QName("simple"), result);
    }

    // ---------- _asXmlGenerator ----------

    @Test
    public void testAsXmlGeneratorWithToXmlGenerator() throws Exception {
        assertSame(xgen, provider._asXmlGenerator(xgen));
    }

    @Test
    public void testAsXmlGeneratorWithTokenBuffer() throws Exception {
        assertNull(provider._asXmlGenerator(tokenBuffer));
    }

    @Test(expected = JsonMappingException.class)
    public void testAsXmlGeneratorWithOtherGenerator() throws Exception {
        provider._asXmlGenerator(plainGen);
    }
}
