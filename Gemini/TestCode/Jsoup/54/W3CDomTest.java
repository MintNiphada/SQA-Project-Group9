package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.DocumentType;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class W3CDomTest {

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoupNull() {
        W3CDom w3c = new W3CDom();
        w3c.fromJsoup(null);
    }

    @Test
    public void testSimpleConversion() {
        String html = "<html><head><title>Test Title</title></head><body><p class=\"intro\">Hello World</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Assert.assertNotNull(w3cDoc);
        NodeList titleList = w3cDoc.getElementsByTagName("title");
        Assert.assertEquals(1, titleList.getLength());
        Assert.assertEquals("Test Title", titleList.item(0).getTextContent());

        NodeList pList = w3cDoc.getElementsByTagName("p");
        Assert.assertEquals(1, pList.getLength());
        Element pElement = (Element) pList.item(0);
        Assert.assertEquals("Hello World", pElement.getTextContent());
        Assert.assertEquals("intro", pElement.getAttribute("class"));
    }

    @Test
    public void testDocumentLocationAndUri() {
        String baseUri = "http://example.com/test.html";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<html><body><a href=\"/link\">Link</a></body></html>", baseUri);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Assert.assertEquals(baseUri, w3cDoc.getDocumentURI());
    }

    @Test
    public void testAsString() {
        String html = "<html><head></head><body><p>Hello</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);
        String xml = w3c.asString(w3cDoc);

        Assert.assertTrue(xml.contains("<p>Hello</p>"));
    }

    @Test
    public void testNamespacesDefaultAndPrefixed() {
        String xml = "<root xmlns=\"http://default.com\" xmlns:custom=\"http://custom.com\"><custom:child id=\"1\">Value</custom:child></root>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Element root = w3cDoc.getDocumentElement();
        Assert.assertEquals("root", root.getTagName());
        Assert.assertEquals("http://default.com", root.getNamespaceURI());

        NodeList children = w3cDoc.getElementsByTagName("custom:child");
        Assert.assertEquals(1, children.getLength());
        Element child = (Element) children.item(0);
        Assert.assertEquals("http://custom.com", child.getNamespaceURI());
        Assert.assertEquals("Value", child.getTextContent());
        Assert.assertEquals("1", child.getAttribute("id"));
    }

    @Test
    public void testCommentsAndDataNodes() {
        String html = "<html><head><script>var x = 10;</script></head><body><!-- a comment --><p>Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList scripts = w3cDoc.getElementsByTagName("script");
        Assert.assertEquals(1, scripts.getLength());
        Assert.assertEquals("var x = 10;", scripts.item(0).getTextContent());

        Node body = w3cDoc.getElementsByTagName("body").item(0);
        NodeList bodyChildren = body.getChildNodes();
        boolean foundComment = false;
        for (int i = 0; i < bodyChildren.getLength(); i++) {
            if (bodyChildren.item(i).getNodeType() == Node.COMMENT_NODE) {
                foundComment = true;
                Assert.assertEquals(" a comment ", bodyChildren.item(i).getNodeValue());
            }
        }
        Assert.assertTrue(foundComment);
    }

    @Test
    public void testAttributeSanitization() {
        String html = "<div data-test$invalid#name=\"valid-value\" @attr!=\"other-value\">Content</div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        NodeList divs = w3cDoc.getElementsByTagName("div");
        Assert.assertEquals(1, divs.getLength());
        Element div = (Element) divs.item(0);
        Assert.assertEquals("valid-value", div.getAttribute("data-testinvalidname"));
        Assert.assertEquals("other-value", div.getAttribute("attr"));
    }

    @Test
    public void testNestedElementsHierarchy() {
        String html = "<div><ul><li>1</li><li>2</li></ul><ol><li>A</li></ol></div>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);

        W3CDom w3c = new W3CDom();
        Document w3cDoc = w3c.fromJsoup(jsoupDoc);

        Element div = (Element) w3cDoc.getElementsByTagName("div").item(0);
        Assert.assertNotNull(div);
        Assert.assertEquals(2, div.getElementsByTagName("ul").getLength() + div.getElementsByTagName("ol").getLength());
        Assert.assertEquals(3, div.getElementsByTagName("li").getLength());
    }

    @Test
    public void testManualConvert() throws Exception {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<div><span>Test</span></div>");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document targetDoc = db.newDocument();

        W3CDom w3c = new W3CDom();
        w3c.convert(jsoupDoc, targetDoc);

        NodeList spans = targetDoc.getElementsByTagName("span");
        Assert.assertEquals(1, spans.getLength());
        Assert.assertEquals("Test", spans.item(0).getTextContent());
    }

    @Test
    public void testW3CBuilderUnhandledNodeType() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document targetDoc = dbf.newDocumentBuilder().newDocument();

        W3CDom.W3CBuilder builder = new W3CDom.W3CBuilder(targetDoc);
        DocumentType docType = new DocumentType("html", "", "", "");
        builder.head(docType, 0);
        builder.tail(docType, 0);

        Assert.assertNull(targetDoc.getDocumentElement());
    }

    @Test
    public void testDataNodeHandling() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document targetDoc = dbf.newDocumentBuilder().newDocument();

        W3CDom.W3CBuilder builder = new W3CDom.W3CBuilder(targetDoc);
        org.jsoup.nodes.Element root = new org.jsoup.nodes.Element("root");
        DataNode dataNode = new DataNode("custom data", "");
        root.appendChild(dataNode);

        builder.head(root, 0);
        builder.head(dataNode, 1);
        builder.tail(dataNode, 1);
        builder.tail(root, 0);

        Element docEl = targetDoc.getDocumentElement();
        Assert.assertNotNull(docEl);
        Assert.assertEquals("custom data", docEl.getTextContent());
    }
}
