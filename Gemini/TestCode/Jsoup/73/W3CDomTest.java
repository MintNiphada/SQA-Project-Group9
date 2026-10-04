package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.DocumentType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class W3CDomTest {
    private W3CDom w3cDom;

    @Before
    public void setUp() {
        w3cDom = new W3CDom();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromJsoupNull() {
        w3cDom.fromJsoup(null);
    }

    @Test
    public void testSimpleConversion() {
        String html = "<html><head><title>Test Title</title></head><body><p class=\"intro\">Hello World!</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Assert.assertNotNull(w3cDoc);
        Assert.assertEquals("html", w3cDoc.getDocumentElement().getTagName());
        Assert.assertEquals(1, w3cDoc.getElementsByTagName("title").getLength());
        Assert.assertEquals("Test Title", w3cDoc.getElementsByTagName("title").item(0).getTextContent());
        Assert.assertEquals(1, w3cDoc.getElementsByTagName("p").getLength());

        Element p = (Element) w3cDoc.getElementsByTagName("p").item(0);
        Assert.assertEquals("intro", p.getAttribute("class"));
        Assert.assertEquals("Hello World!", p.getTextContent());

        String out = w3cDom.asString(w3cDoc);
        Assert.assertTrue(out.contains("<title>Test Title</title>"));
        Assert.assertTrue(out.contains("class=\"intro\""));
    }

    @Test
    public void testDocumentLocationAndUri() {
        String html = "<html><head></head><body></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html, "http://example.com/page.html");
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Assert.assertEquals("http://example.com/page.html", w3cDoc.getDocumentURI());
    }

    @Test
    public void testNamespaces() {
        String html = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\">" +
                "<head></head><body><epub:section>Content</epub:section></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Element root = w3cDoc.getDocumentElement();
        Assert.assertEquals("http://www.w3.org/1999/xhtml", root.getNamespaceURI());

        Element section = (Element) w3cDoc.getElementsByTagName("epub:section").item(0);
        Assert.assertNotNull(section);
        Assert.assertEquals("http://www.idpf.org/2007/ops", section.getNamespaceURI());
        Assert.assertEquals("Content", section.getTextContent());
    }

    @Test
    public void testCommentsAndDataNodes() {
        String html = "<html><head><script>var x = 10;</script></head><body><!-- This is a comment --><p>Text</p></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Element script = (Element) w3cDoc.getElementsByTagName("script").item(0);
        Assert.assertNotNull(script);
        Assert.assertEquals("var x = 10;", script.getTextContent());

        Element body = (Element) w3cDoc.getElementsByTagName("body").item(0);
        boolean commentFound = false;
        for (int i = 0; i < body.getChildNodes().getLength(); i++) {
            Node child = body.getChildNodes().item(i);
            if (child.getNodeType() == Node.COMMENT_NODE) {
                Assert.assertEquals(" This is a comment ", child.getTextContent());
                commentFound = true;
            }
        }
        Assert.assertTrue(commentFound);
    }

    @Test
    public void testAttributeFiltering() {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<div></div>");
        org.jsoup.nodes.Element div = jsoupDoc.selectFirst("div");
        div.attr("valid-name_1:test.ok", "value1");
        div.attr("123invalid", "value2");
        div.attr("invalid^char", "value3");

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Element w3cDiv = (Element) w3cDoc.getElementsByTagName("div").item(0);

        Assert.assertEquals("value1", w3cDiv.getAttribute("valid-name_1:test.ok"));
        Assert.assertEquals("value3", w3cDiv.getAttribute("invalidchar"));
        Assert.assertFalse(w3cDiv.hasAttribute("123invalid"));
    }

    @Test
    public void testUnhandledNode() {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<html><head></head><body><div>Test</div></body></html>");
        jsoupDoc.child(0).appendChild(new DocumentType("html", "", ""));
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Assert.assertNotNull(w3cDoc);
    }

    @Test
    public void testNestedElementsTailTraversal() {
        String html = "<html><body><div><ul><li><span>Text</span></li></ul></div><div><p>Other</p></div></body></html>";
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(html);
        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);

        Assert.assertEquals(2, w3cDoc.getElementsByTagName("div").getLength());
        Assert.assertEquals(1, w3cDoc.getElementsByTagName("span").getLength());
        Assert.assertEquals("Text", w3cDoc.getElementsByTagName("span").item(0).getTextContent());
        Assert.assertEquals(1, w3cDoc.getElementsByTagName("p").getLength());
        Assert.assertEquals("Other", w3cDoc.getElementsByTagName("p").item(0).getTextContent());
    }

    @Test
    public void testExplicitDataNodeConversion() {
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parse("<div></div>");
        org.jsoup.nodes.Element div = jsoupDoc.selectFirst("div");
        div.appendChild(new DataNode("custom data"));

        Document w3cDoc = w3cDom.fromJsoup(jsoupDoc);
        Element w3cDiv = (Element) w3cDoc.getElementsByTagName("div").item(0);
        Assert.assertEquals("custom data", w3cDiv.getTextContent());
    }

    @Test(expected = IllegalStateException.class)
    public void testParserConfigurationExceptionHandling() {
        W3CDom customDom = new W3CDom() {
            {
                this.factory = new DocumentBuilderFactory() {
                    @Override
                    public javax.xml.parsers.DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
                        throw new ParserConfigurationException("Simulated error");
                    }
                    @Override
                    public void setAttribute(String name, Object value) {}
                    @Override
                    public Object getAttribute(String name) { return null; }
                    @Override
                    public void setFeature(String name, boolean value) {}
                    @Override
                    public boolean getFeature(String name) { return false; }
                };
            }
        };
        customDom.fromJsoup(Jsoup.parse("<html></html>"));
    }
}
