package com.fasterxml.jackson.dataformat.xml.ser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
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
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.util.XmlRootNameLookup;

public class XmlSerializerProviderTest {

    private XmlMapper _xmlMapper;
    private XmlSerializerProvider _provider;
    private XmlRootNameLookup _rootNameLookup;

    @JacksonXmlRootElement(localName = "customRoot", namespace = "http://example.com/ns")
    static class CustomAnnotatedBean {
        public String name = "test";
    }

    static class SimpleBean {
        public String value = "hello";
    }

    static class FailingBean {
        public String getThrowing() {
            throw new RuntimeException("Property read error");
        }
    }

    static class NullMessageExceptionBean {
        public String getThrowing() {
            throw new NullPointerException();
        }
    }

    @Before
    public void setUp() {
        _xmlMapper = new XmlMapper();
        _rootNameLookup = new XmlRootNameLookup();
        _provider = new XmlSerializerProvider(_rootNameLookup);
    }

    @Test
    public void testConstructorsAndCopy() {
        Assert.assertNotNull(_provider);
        Assert.assertNotNull(_provider._rootNameLookup);

        SerializationConfig config = _xmlMapper.getSerializationConfig();
        DefaultSerializerProvider copy1 = _provider.createInstance(config, BeanSerializerFactory.instance);
        Assert.assertTrue(copy1 instanceof XmlSerializerProvider);
        Assert.assertNotNull(((XmlSerializerProvider) copy1)._rootNameLookup);

        DefaultSerializerProvider copy2 = copy1.copy();
        Assert.assertTrue(copy2 instanceof XmlSerializerProvider);
        Assert.assertNotNull(((XmlSerializerProvider) copy2)._rootNameLookup);
    }

    @Test
    public void testRootNameFromConfigWithNull() {
        SerializationConfig config = _xmlMapper.getSerializationConfig().withRootName((PropertyName) null);
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);
        Assert.assertNull(prov._rootNameFromConfig());
    }

    @Test
    public void testRootNameFromConfigSimpleName() {
        SerializationConfig config = _xmlMapper.getSerializationConfig().withRootName(PropertyName.construct("myRoot", null));
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);
        QName qname = prov._rootNameFromConfig();
        Assert.assertNotNull(qname);
        Assert.assertEquals("myRoot", qname.getLocalPart());
        Assert.assertEquals("", qname.getNamespaceURI());
    }

    @Test
    public void testRootNameFromConfigWithNamespace() {
        SerializationConfig config = _xmlMapper.getSerializationConfig().withRootName(PropertyName.construct("myRoot", "http://ns.example.com"));
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);
        QName qname = prov._rootNameFromConfig();
        Assert.assertNotNull(qname);
        Assert.assertEquals("myRoot", qname.getLocalPart());
        Assert.assertEquals("http://ns.example.com", qname.getNamespaceURI());
    }

    @Test
    public void testAsXmlGenerator() throws Exception {
        StringWriter sw = new StringWriter();
        ToXmlGenerator xmlGen = _xmlMapper.getFactory().createGenerator(sw);
        Assert.assertSame(xmlGen, _provider._asXmlGenerator(xmlGen));
        xmlGen.close();

        TokenBuffer tb = new TokenBuffer((ObjectMapper) null, false);
        Assert.assertNull(_provider._asXmlGenerator(tb));
        tb.close();

        JsonGenerator jsonGen = new JsonFactory().createGenerator(new ByteArrayOutputStream());
        try {
            _provider._asXmlGenerator(jsonGen);
            Assert.fail("Expected JsonMappingException for non-XML generator");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("XmlMapper does not with generators of type other than ToXmlGenerator"));
        } finally {
            jsonGen.close();
        }
    }

    @Test
    public void testWrapAsIOE() {
        JsonGenerator dummyGen = new TokenBuffer(null, false);
        
        IOException ioe = new IOException("io failure");
        Assert.assertSame(ioe, _provider._wrapAsIOE(dummyGen, ioe));

        RuntimeException re = new RuntimeException("runtime failure");
        IOException wrappedRe = _provider._wrapAsIOE(dummyGen, re);
        Assert.assertTrue(wrappedRe instanceof JsonMappingException);
        Assert.assertTrue(wrappedRe.getMessage().contains("runtime failure"));
        Assert.assertSame(re, wrappedRe.getCause());

        NullPointerException npe = new NullPointerException();
        IOException wrappedNpe = _provider._wrapAsIOE(dummyGen, npe);
        Assert.assertTrue(wrappedNpe instanceof JsonMappingException);
        Assert.assertTrue(wrappedNpe.getMessage().contains("[no message for java.lang.NullPointerException]"));
    }

    @Test
    public void testSerializeNullValue() throws Exception {
        String xml = _xmlMapper.writeValueAsString(null);
        Assert.assertEquals("<null/>", xml);
    }

    @Test
    public void testSerializeNullValueWithConfiguredRoot() throws Exception {
        XmlMapper mapper = new XmlMapper();
        String xml = mapper.writer().withRootName("customNull").writeValueAsString(null);
        Assert.assertEquals("<customNull/>", xml);
    }

    @Test
    public void testSerializeNullValueWithTokenBuffer() throws Exception {
        TokenBuffer tb = new TokenBuffer(null, false);
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        DefaultSerializerProvider prov = _provider.createInstance(config, BeanSerializerFactory.instance);
        prov.serializeValue(tb, null);
        Assert.assertTrue(tb.asParser().nextToken() != null);
        tb.close();
    }

    @Test
    public void testSerializeSimpleBean() throws Exception {
        SimpleBean bean = new SimpleBean();
        String xml = _xmlMapper.writeValueAsString(bean);
        Assert.assertTrue(xml.contains("<SimpleBean>"));
        Assert.assertTrue(xml.contains("<value>hello</value>"));
        Assert.assertTrue(xml.contains("</SimpleBean>"));
    }

    @Test
    public void testSerializeAnnotatedBeanWithNamespace() throws Exception {
        CustomAnnotatedBean bean = new CustomAnnotatedBean();
        String xml = _xmlMapper.writeValueAsString(bean);
        Assert.assertTrue(xml.contains("xmlns=\"http://example.com/ns\""));
        Assert.assertTrue(xml.contains("customRoot"));
        Assert.assertTrue(xml.contains("<name>test</name>"));
    }

    @Test
    public void testSerializeArrayOrList() throws Exception {
        List<String> list = Arrays.asList("a", "b", "c");
        String xml = _xmlMapper.writeValueAsString(list);
        Assert.assertTrue(xml.contains("<ArrayList>"));
        Assert.assertTrue(xml.contains("<item>a</item>"));
        Assert.assertTrue(xml.contains("<item>b</item>"));
        Assert.assertTrue(xml.contains("<item>c</item>"));
        Assert.assertTrue(xml.contains("</ArrayList>"));

        String[] array = new String[] { "x", "y" };
        String arrayXml = _xmlMapper.writeValueAsString(array);
        Assert.assertTrue(arrayXml.contains("<item>x</item>"));
        Assert.assertTrue(arrayXml.contains("<item>y</item>"));
    }

    @Test
    public void testSerializeWithJavaTypeAndExplicitSerializer() throws Exception {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);

        StringWriter sw = new StringWriter();
        ToXmlGenerator xmlGen = _xmlMapper.getFactory().createGenerator(sw);
        
        prov.serializeValue(xmlGen, new SimpleBean(), type, null);
        xmlGen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<SimpleBean>"));
        Assert.assertTrue(xml.contains("<value>hello</value>"));
        Assert.assertTrue(xml.contains("</SimpleBean>"));
    }

    @Test
    public void testSerializeWithJavaTypeAndExplicitCustomSerializer() throws Exception {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);

        JsonSerializer<Object> customSer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartObject();
                gen.writeStringField("custom", "val");
                gen.writeEndObject();
            }
        };

        StringWriter sw = new StringWriter();
        ToXmlGenerator xmlGen = _xmlMapper.getFactory().createGenerator(sw);

        prov.serializeValue(xmlGen, new SimpleBean(), type, customSer);
        xmlGen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<SimpleBean>"));
        Assert.assertTrue(xml.contains("<custom>val</custom>"));
        Assert.assertTrue(xml.contains("</SimpleBean>"));
    }

    @Test
    public void testSerializeWithJavaTypeArray() throws Exception {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);
        JavaType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, String.class);

        StringWriter sw = new StringWriter();
        ToXmlGenerator xmlGen = _xmlMapper.getFactory().createGenerator(sw);

        prov.serializeValue(xmlGen, Arrays.asList("one", "two"), type, null);
        xmlGen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("<ArrayList>"));
        Assert.assertTrue(xml.contains("<item>one</item>"));
        Assert.assertTrue(xml.contains("<item>two</item>"));
        Assert.assertTrue(xml.contains("</ArrayList>"));
    }

    @Test
    public void testSerializeWithJavaTypeNullValue() throws Exception {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);

        StringWriter sw = new StringWriter();
        ToXmlGenerator xmlGen = _xmlMapper.getFactory().createGenerator(sw);

        prov.serializeValue(xmlGen, null, type, null);
        xmlGen.close();

        Assert.assertEquals("<null/>", sw.toString());
    }

    @Test
    public void testSerializeWithTokenBufferNonXmlGen() throws Exception {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);

        TokenBuffer tb = new TokenBuffer(null, false);
        prov.serializeValue(tb, new SimpleBean());
        Assert.assertNotNull(tb.asParser().nextToken());
        tb.close();

        TokenBuffer tb2 = new TokenBuffer(null, false);
        JavaType type = TypeFactory.defaultInstance().constructType(SimpleBean.class);
        prov.serializeValue(tb2, new SimpleBean(), type, null);
        Assert.assertNotNull(tb2.asParser().nextToken());
        tb2.close();
    }

    @Test
    public void testSerializeExceptionWrapping() {
        try {
            _xmlMapper.writeValueAsString(new FailingBean());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Property read error"));
        } catch (IOException e) {
            Assert.fail("Expected JsonMappingException, got " + e.getClass().getName());
        }

        try {
            _xmlMapper.writeValueAsString(new NullMessageExceptionBean());
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("NullPointerException"));
        } catch (IOException e) {
            Assert.fail("Expected JsonMappingException, got " + e.getClass().getName());
        }
    }

    @Test
    public void testSerializeExceptionWrappingWithJavaType() {
        JavaType type = TypeFactory.defaultInstance().constructType(FailingBean.class);
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);
        StringWriter sw = new StringWriter();
        try {
            ToXmlGenerator xmlGen = _xmlMapper.getFactory().createGenerator(sw);
            prov.serializeValue(xmlGen, new FailingBean(), type, null);
            Assert.fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Property read error"));
        } catch (IOException e) {
            Assert.fail("Expected JsonMappingException, got " + e.getClass().getName());
        }
    }

    @Test
    public void testInitWithRootNameWhenNextNameAlreadySet() throws Exception {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);

        StringWriter sw = new StringWriter();
        ToXmlGenerator xmlGen = _xmlMapper.getFactory().createGenerator(sw);
        xmlGen.setNextName(new QName("preSetRoot"));

        prov._initWithRootName(xmlGen, new QName("anotherRoot"));
        xmlGen.writeStartObject();
        xmlGen.writeStringField("k", "v");
        xmlGen.writeEndObject();
        xmlGen.close();

        String xml = sw.toString();
        Assert.assertTrue(xml.contains("anotherRoot") || xml.contains("preSetRoot"));
    }

    @Test
    public void testInitWithRootNameNamespaceExceptionHandling() throws Exception {
        SerializationConfig config = _xmlMapper.getSerializationConfig();
        XmlSerializerProvider prov = (XmlSerializerProvider) _provider.createInstance(config, BeanSerializerFactory.instance);

        XMLStreamWriter mockWriter = (XMLStreamWriter) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[] { XMLStreamWriter.class },
                (proxy, method, args) -> {
                    if ("setDefaultNamespace".equals(method.getName())) {
                        throw new XMLStreamException("Simulated namespace failure");
                    }
                    if ("getPrefix".equals(method.getName())) {
                        return "";
                    }
                    if ("getNamespaceContext".equals(method.getName())) {
                        return null;
                    }
                    return null;
                });

        XmlFactory xmlFactory = _xmlMapper.getFactory();
        ToXmlGenerator xmlGen = xmlFactory.createGenerator(mockWriter);

        try {
            prov._initWithRootName(xmlGen, new QName("http://example.com/fail", "root"));
            Assert.fail("Expected JsonMappingException due to XMLStreamException");
        } catch (JsonMappingException e) {
            Assert.assertTrue(e.getMessage().contains("Simulated namespace failure"));
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof XMLStreamException || e instanceof XMLStreamException);
        }
    }
}
