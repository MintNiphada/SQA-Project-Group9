package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlSerializerProviderTest {

    private XmlMapper _xmlMapper;
    private XmlSerializerProvider _provider;

    @JacksonXmlRootElement(localName = "sample", namespace = "http://example.com/ns")
    static class SampleBean {
        public String name = "test";
    }

    static class SimpleBean {
        public int id = 42;
    }

    static class FailingBean {
        public String getValue() {
            throw new IllegalStateException("Simulated getter failure");
        }
    }

    static class NoMsgFailingBean {
        public String getValue() {
            throw new RuntimeException((String) null);
        }
    }

    static class CustomIOExceptionBean {
        public String getValue() throws IOException {
            throw new IOException("Simulated IO failure");
        }
    }

    @Before
    public void setUp() {
        _xmlMapper = new XmlMapper();
        XmlRootNameLookup rootNames = new XmlRootNameLookup();
        _provider = new XmlSerializerProvider(rootNames);
    }

    private XmlSerializerProvider createConfiguredProvider(XmlMapper mapper) {
        SerializationConfig config = mapper.getSerializationConfig();
        return (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);
    }

    private ToXmlGenerator createToXmlGenerator(StringWriter sw) throws IOException {
        IOContext ioContext = new IOContext(new BufferRecycler(), sw, false);
        try {
            XMLOutputFactory staxFactory = XMLOutputFactory.newFactory();
            XMLStreamWriter swriter = staxFactory.createXMLStreamWriter(sw);
            return new ToXmlGenerator(ioContext, 0, 0, _xmlMapper.getXmlFactory().getCodec(), swriter);
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    @Test
    public void testConstructorsAndCreateInstance() {
        XmlRootNameLookup rootNames = new XmlRootNameLookup();
        XmlSerializerProvider provider = new XmlSerializerProvider(rootNames);
        Assert.assertNotNull(provider);

        SerializationConfig config = _xmlMapper.getSerializationConfig();
        DefaultSerializerProvider instance = provider.createInstance(config, BeanSerializerFactory.instance);
        Assert.assertTrue(instance instanceof XmlSerializerProvider);
        Assert.assertEquals(config, instance.getConfig());
    }

    @Test
    public void testSerializeNullValue() throws Exception {
        String xml = _xmlMapper.writeValueAsString(null);
        Assert.assertTrue(xml.contains("<null") || xml.contains("<null/>"));
    }

    @Test
    public void testSerializeNullWithTokenBuffer() throws Exception {
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);
        TokenBuffer tb = new TokenBuffer(_xmlMapper, false);
        prov.serializeValue(tb, null);
        tb.close();
        Assert.assertNull(tb.firstToken());
    }

    @Test
    public void testSerializeNullWithJavaTypeAndCustomSerializer() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = createToXmlGenerator(sw);
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);

        prov.serializeValue(xgen, null, type);
        xgen.close();
        Assert.assertTrue(sw.toString().contains("null"));

        sw = new StringWriter();
        xgen = createToXmlGenerator(sw);
        prov.serializeValue(xgen, null, type, null);
        xgen.close();
        Assert.assertTrue(sw.toString().contains("null"));
    }

    @Test
    public void testSerializeSimpleBean() throws Exception {
        SimpleBean bean = new SimpleBean();
        String xml = _xmlMapper.writeValueAsString(bean);
        Assert.assertTrue(xml.contains("<SimpleBean>"));
        Assert.assertTrue(xml.contains("<id>42</id>"));
        Assert.assertTrue(xml.contains("</SimpleBean>"));
    }

    @Test
    public void testSerializeNamespacedBean() throws Exception {
        SampleBean bean = new SampleBean();
        String xml = _xmlMapper.writeValueAsString(bean);
        Assert.assertTrue(xml.contains("xmlns=\"http://example.com/ns\""));
        Assert.assertTrue(xml.contains("<sample"));
        Assert.assertTrue(xml.contains("<name>test</name>"));
    }

    @Test
    public void testSerializeArrayAndCollection() throws Exception {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        String xmlList = _xmlMapper.writeValueAsString(list);
        Assert.assertTrue(xmlList.contains("<item>a</item>"));
        Assert.assertTrue(xmlList.contains("<item>b</item>"));

        String[] array = new String[] { "x", "y" };
        String xmlArray = _xmlMapper.writeValueAsString(array);
        Assert.assertTrue(xmlArray.contains("<item>x</item>"));
        Assert.assertTrue(xmlArray.contains("<item>y</item>"));
    }

    @Test
    public void testSerializeValueWithRootType() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = createToXmlGenerator(sw);
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(List.class, String.class);

        List<String> list = Collections.singletonList("itemValue");
        prov.serializeValue(xgen, list, type);
        xgen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<item>itemValue</item>"));
    }

    @Test
    public void testSerializeValueWithRootTypeAndCustomSerializer() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = createToXmlGenerator(sw);
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);

        JsonSerializer<Object> ser = prov.findTypedValueSerializer(type, true, null);
        SimpleBean bean = new SimpleBean();
        prov.serializeValue(xgen, bean, type, ser);
        xgen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<id>42</id>"));
    }

    @Test
    public void testSerializeValueWithTokenBuffer() throws Exception {
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);
        TokenBuffer tb = new TokenBuffer(_xmlMapper, false);
        SimpleBean bean = new SimpleBean();

        prov.serializeValue(tb, bean);
        tb.close();
        Assert.assertNotNull(tb.firstToken());

        TokenBuffer tb2 = new TokenBuffer(_xmlMapper, false);
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        prov.serializeValue(tb2, bean, type);
        tb2.close();
        Assert.assertNotNull(tb2.firstToken());

        TokenBuffer tb3 = new TokenBuffer(_xmlMapper, false);
        prov.serializeValue(tb3, bean, type, null);
        tb3.close();
        Assert.assertNotNull(tb3.firstToken());
    }

    @Test
    public void testAsXmlGeneratorThrowsOnInvalidGenerator() throws Exception {
        ObjectMapper standardJsonMapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator invalidGen = standardJsonMapper.getFactory().createGenerator(sw);
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);

        try {
            prov.serializeValue(invalidGen, new SimpleBean());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("XmlMapper does not with generators of type other than ToXmlGenerator"));
        } finally {
            invalidGen.close();
        }
    }

    @Test
    public void testRootNameFromConfigWithAndWithoutNamespace() throws Exception {
        XmlMapper mapperWithRoot = new XmlMapper();
        mapperWithRoot.setConfig(mapperWithRoot.getSerializationConfig().withRootName(PropertyName.construct("customRoot", "http://custom.org")));
        XmlSerializerProvider provWithRoot = createConfiguredProvider(mapperWithRoot);

        QName qname = provWithRoot._rootNameFromConfig();
        Assert.assertEquals("customRoot", qname.getLocalPart());
        Assert.assertEquals("http://custom.org", qname.getNamespaceURI());

        String xmlWithNs = mapperWithRoot.writeValueAsString(new SimpleBean());
        Assert.assertTrue(xmlWithNs.contains("<customRoot"));
        Assert.assertTrue(xmlWithNs.contains("xmlns=\"http://custom.org\""));

        XmlMapper mapperSimpleRoot = new XmlMapper();
        mapperSimpleRoot.setConfig(mapperSimpleRoot.getSerializationConfig().withRootName(PropertyName.construct("simpleRoot", null)));
        XmlSerializerProvider provSimpleRoot = createConfiguredProvider(mapperSimpleRoot);

        QName qnameSimple = provSimpleRoot._rootNameFromConfig();
        Assert.assertEquals("simpleRoot", qnameSimple.getLocalPart());
        Assert.assertEquals("", qnameSimple.getNamespaceURI());

        String xmlSimple = mapperSimpleRoot.writeValueAsString(new SimpleBean());
        Assert.assertTrue(xmlSimple.contains("<simpleRoot>"));
    }

    @Test
    public void testRootNameFromConfigNull() {
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);
        QName qname = prov._rootNameFromConfig();
        Assert.assertNull(qname);
    }

    @Test
    public void testExceptionHandlingWrapsRuntimeException() throws Exception {
        try {
            _xmlMapper.writeValueAsString(new FailingBean());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Simulated getter failure"));
        }

        try {
            _xmlMapper.writeValueAsString(new NoMsgFailingBean());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("[no message for java.lang.RuntimeException]"));
        }
    }

    @Test
    public void testExceptionHandlingPassesIOExceptionDirectly() {
        try {
            _xmlMapper.writeValueAsString(new CustomIOExceptionBean());
            Assert.fail("Expected IOException");
        } catch (JsonMappingException e) {
            // JsonMappingException extends IOException, verify root cause or exact type
            Assert.assertTrue(e.getCause() instanceof IOException || e.getMessage().contains("Simulated IO failure"));
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Simulated IO failure"));
        }
    }

    @Test
    public void testSerializeValueWithRootTypeExceptionWrapping() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = createToXmlGenerator(sw);
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);
        JavaType type = TypeFactory.defaultInstance().constructType(FailingBean.class);

        try {
            prov.serializeValue(xgen, new FailingBean(), type);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Simulated getter failure"));
        }

        try {
            prov.serializeValue(xgen, new NoMsgFailingBean(), type);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("[no message for java.lang.RuntimeException]"));
        }

        try {
            prov.serializeValue(xgen, new CustomIOExceptionBean(), type);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Simulated IO failure") || (e.getCause() != null && e.getCause().getMessage().contains("Simulated IO failure")));
        }
    }

    @Test
    public void testSerializeValueWithSerializerExceptionWrapping() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = createToXmlGenerator(sw);
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);

        JsonSerializer<Object> throwingSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                throw new IllegalStateException("Custom serializer failed");
            }
        };

        try {
            prov.serializeValue(xgen, "testValue", type, throwingSer);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Custom serializer failed"));
        }

        JsonSerializer<Object> noMsgThrowingSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                throw new RuntimeException((String) null);
            }
        };

        try {
            prov.serializeValue(xgen, "testValue", type, noMsgThrowingSer);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("[no message for java.lang.RuntimeException]"));
        }

        final IOException directIOException = new IOException("Direct IO failure in serializer");
        JsonSerializer<Object> ioThrowingSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                throw directIOException;
            }
        };

        try {
            prov.serializeValue(xgen, "testValue", type, ioThrowingSer);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertSame(directIOException, e);
        }
    }

    @Test
    public void testInitWithRootNameWhenNextNameAlreadySet() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = createToXmlGenerator(sw);
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);

        QName customRoot = new QName("http://test.org", "customName");
        xgen.setNextName(new QName("existingName"));
        prov._initWithRootName(xgen, customRoot);

        xgen.writeStartObject();
        xgen.writeEndObject();
        xgen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("existingName") || xml.contains("customName"));
    }

    @Test
    public void testInitWithRootNameThrowsXmlException() throws Exception {
        XMLStreamWriter throwingWriter = (XMLStreamWriter) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { XMLStreamWriter.class },
                new java.lang.reflect.InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        if ("setDefaultNamespace".equals(method.getName())) {
                            throw new XMLStreamException("Simulated XML stream fault");
                        }
                        if (method.getReturnType().equals(Void.TYPE)) {
                            return null;
                        }
                        if (method.getReturnType().equals(boolean.class)) {
                            return false;
                        }
                        if (method.getReturnType().equals(int.class)) {
                            return 0;
                        }
                        return null;
                    }
                }
        );

        IOContext ioContext = new IOContext(new BufferRecycler(), new StringWriter(), false);
        ToXmlGenerator customGen = new ToXmlGenerator(ioContext, 0, 0, _xmlMapper.getXmlFactory().getCodec(), throwingWriter);

        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);
        QName namespacedRoot = new QName("http://error.ns", "root");

        try {
            prov._initWithRootName(customGen, namespacedRoot);
            Assert.fail("Expected IOException due to XMLStreamException");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("Simulated XML stream fault") || (e.getCause() != null && e.getCause().getMessage().contains("Simulated XML stream fault")));
        }
    }

    @Test
    public void testStartRootArray() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xgen = createToXmlGenerator(sw);
        XmlSerializerProvider prov = createConfiguredProvider(_xmlMapper);

        QName rootName = new QName("rootList");
        prov._startRootArray(xgen, rootName);
        xgen.writeString("value");
        xgen.writeEndObject();
        xgen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("item"));
        Assert.assertTrue(xml.contains("value"));
    }

    @Test
    public void testWrapEmptyRootArray() throws Exception {
        List<Object> emptyList = Collections.emptyList();
        String xml = _xmlMapper.writeValueAsString(emptyList);
        Assert.assertNotNull(xml);
    }

    @Test
    public void testRootNameForNullConstant() {
        Assert.assertEquals("null", XmlSerializerProvider.ROOT_NAME_FOR_NULL.getLocalPart());
    }
}
