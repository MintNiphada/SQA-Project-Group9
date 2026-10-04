package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;

public class W3CDomTest {

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoupNull() {
        W3CDom w3c = new W3CDom();
        w3c.fromJsoup(null);
    }

    @Test
    public void testSimpleDocumentConversion() {
        String html = "<!DOCTYPE html><html><head><title>Test</title></head><body><p class=\"intro\">Hello <b>World</b></p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
        
        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);
        
        Assert.assertNotNull(doc);
        Assert.assertEquals("html", doc.getDocumentElement().getTagName());
        Assert.assertEquals(2, doc.getDocumentElement().getChildNodes().getLength());
        
        String out = w3c.asString(doc);
        Assert.assertTrue(out.contains("<title>Test</title>"));
        Assert.assertTrue(out.contains("class=\"intro\""));
    }

    @Test
    public void testDocumentLocationAndUri() {
        String html = "<html><head></head><body><p>Text</p></body></html>";
        String baseUri = "https://example.com/test.html";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, baseUri);

        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);

        Assert.assertEquals(baseUri, doc.getDocumentURI());
    }

    @Test
    public void testDocumentBlankLocation() {
        String html = "<html><head></head><body><p>Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);

        Assert.assertNull(doc.getDocumentURI());
    }

    @Test
    public void testNamespacesAndAttributes() {
        String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">" +
                "<head><title>Namespace Test</title></head>" +
                "<body><epub:section id=\"sec1\" class=\"chapter\" 1invalid=\"val\" _valid:key=\"val2\">" +
                "<!-- comment node -->" +
                "<script>var x = 10;</script>" +
                "</epub:section></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);

        Element htmlEl = doc.getDocumentElement();
        Assert.assertEquals("http://www.w3.org/1999/xhtml", htmlEl.getNamespaceURI());

        Element sectionEl = (Element) doc.getElementsByTagName("epub:section").item(0);
        Assert.assertNotNull(sectionEl);
        Assert.assertEquals("http://www.idpf.org/2007/ops", sectionEl.getNamespaceURI());
        Assert.assertEquals("sec1", sectionEl.getAttribute("id"));
        Assert.assertEquals("chapter", sectionEl.getAttribute("class"));
        Assert.assertEquals("val2", sectionEl.getAttribute("_valid:key"));
        Assert.assertFalse(sectionEl.hasAttribute("1invalid"));

        Node commentNode = sectionEl.getChildNodes().item(0);
        Assert.assertEquals(Node.COMMENT_NODE, commentNode.getNodeType());
        Assert.assertEquals(" comment node ", commentNode.getNodeValue());

        Node scriptEl = sectionEl.getChildNodes().item(1);
        Assert.assertEquals(Node.ELEMENT_NODE, scriptEl.getNodeType());
        Node dataNode = scriptEl.getFirstChild();
        Assert.assertEquals(Node.TEXT_NODE, dataNode.getNodeType());
        Assert.assertEquals("var x = 10;", dataNode.getNodeValue());
    }

    @Test
    public void testNestedElementsAndTail() {
        String html = "<div><ul><li><span>Text 1</span></li><li>Text 2</li></ul><p>Paragraph</p></div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);

        Assert.assertEquals(1, doc.getElementsByTagName("ul").getLength());
        Assert.assertEquals(2, doc.getElementsByTagName("li").getLength());
        Assert.assertEquals(1, doc.getElementsByTagName("p").getLength());

        String xml = w3c.asString(doc);
        Assert.assertTrue(xml.contains("<span>Text 1</span>"));
        Assert.assertTrue(xml.contains("Text 2"));
    }

    @Test
    public void testAttributeFilteringSpecialCharacters() {
        String html = "<div valid-name=\"true\" @invalid=\"no\" ?also-bad=\"no\" data-num=\"123\"></div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);

        Element div = (Element) doc.getElementsByTagName("div").item(0);
        Assert.assertTrue(div.hasAttribute("valid-name"));
        Assert.assertTrue(div.hasAttribute("data-num"));
        Assert.assertFalse(div.hasAttribute("@invalid"));
        Assert.assertFalse(div.hasAttribute("?also-bad"));
    }

    @Test
    public void testDataNodeDirectly() {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<script></script>");
        jsoupDoc.head().getElementsByTag("script").first().appendChild(new DataNode("console.log('hi');"));

        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);

        Element script = (Element) doc.getElementsByTagName("script").item(0);
        Assert.assertNotNull(script);
        Assert.assertEquals("console.log('hi');", script.getTextContent());
    }

    @Test
    public void testConvertDirect() throws Exception {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<p>Direct Conversion</p>", "http://example.org");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();

        W3CDom w3c = new W3CDom();
        w3c.convert(jsoupDoc, doc);

        Assert.assertEquals("http://example.org", doc.getDocumentURI());
        Assert.assertEquals(1, doc.getElementsByTagName("p").getLength());
        Assert.assertEquals("Direct Conversion", doc.getElementsByTagName("p").item(0).getTextContent());
    }

    @Test
    public void testW3CBuilderNamespaceInheritance() {
        String html = "<div xmlns:ns1=\"urn:ns1\"><div xmlns:ns2=\"urn:ns2\"><ns1:elem><ns2:elem/></ns1:elem></div></div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);

        Element ns1Elem = (Element) doc.getElementsByTagName("ns1:elem").item(0);
        Assert.assertNotNull(ns1Elem);
        Assert.assertEquals("urn:ns1", ns1Elem.getNamespaceURI());

        Element ns2Elem = (Element) doc.getElementsByTagName("ns2:elem").item(0);
        Assert.assertNotNull(ns2Elem);
        Assert.assertEquals("urn:ns2", ns2Elem.getNamespaceURI());
    }

    @Test
    public void testDefaultNamespaceUnset() {
        String html = "<div><span>plain</span></div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);

        Element span = (Element) doc.getElementsByTagName("span").item(0);
        Assert.assertNotNull(span);
        Assert.assertNull(span.getNamespaceURI());
    }

    @Test
    public void testAsStringOutput() {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<html><head><title>Title</title></head><body><p>Hello</p></body></html>");
        W3CDom w3c = new W3CDom();
        Document doc = w3c.fromJsoup(jsoupDoc);
        String result = w3c.asString(doc);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.contains("<title>Title</title>"));
    }
}
