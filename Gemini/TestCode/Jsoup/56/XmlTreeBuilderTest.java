package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class XmlTreeBuilderTest {

    @Test
    public void testDefaultSettings() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        ParseSettings settings = treeBuilder.defaultSettings();
        Assert.assertEquals("TagCase", settings.normalizeTag("TagCase"));
        Assert.assertEquals("AttributeCase", settings.normalizeAttribute("AttributeCase"));
    }

    @Test
    public void testSimpleParse() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<root><child attr=\"val\">Text</child></root>", "http://example.com/");
        Assert.assertNotNull(doc);
        Assert.assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Assert.assertEquals(1, doc.children().size());
        Element root = doc.child(0);
        Assert.assertEquals("root", root.tagName());
        Assert.assertEquals(1, root.children().size());
        Element child = root.child(0);
        Assert.assertEquals("child", child.tagName());
        Assert.assertEquals("val", child.attr("attr"));
        Assert.assertEquals("Text", child.text());
        Assert.assertEquals("http://example.com/", child.baseUri());
    }

    @Test
    public void testSelfClosingUnknownTag() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<custom-tag id=\"1\"/><other-tag/>", "");
        Assert.assertEquals(2, doc.children().size());
        Element el = doc.child(0);
        Assert.assertEquals("custom-tag", el.tagName());
        Assert.assertTrue(el.tag().isSelfClosing());
    }

    @Test
    public void testSelfClosingKnownTag() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<img src=\"test.png\"/><br/>", "");
        Assert.assertEquals(2, doc.children().size());
        Element img = doc.child(0);
        Assert.assertEquals("img", img.tagName());
        Assert.assertTrue(img.tag().isKnownTag());
    }

    @Test
    public void testCommentNormal() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<!-- This is a regular comment --><root/>", "http://example.com");
        Assert.assertTrue(doc.childNode(0) instanceof Comment);
        Comment comment = (Comment) doc.childNode(0);
        Assert.assertEquals(" This is a regular comment ", comment.getData());
        Assert.assertEquals("http://example.com", comment.baseUri());
    }

    @Test
    public void testXmlDeclarationBogusComment() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><root/>", "http://example.com");
        Assert.assertTrue(doc.childNode(0) instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) doc.childNode(0);
        Assert.assertEquals("xml", decl.name());
        Assert.assertEquals("1.0", decl.attr("version"));
        Assert.assertEquals("UTF-8", decl.attr("encoding"));
        Assert.assertFalse(decl.toString().startsWith("<!"));
    }

    @Test
    public void testDocTypeBogusDeclaration() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<!DOCTYPE html><root/>", "");
        Assert.assertTrue(doc.childNode(0) instanceof DocumentType);
    }

    @Test
    public void testExclamationBogusDeclaration() {
        Token.Comment commentToken = new Token.Comment();
        commentToken.bogus = true;
        commentToken.data.append("!TAG attr='value'");
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        treeBuilder.initialiseParse("<root/>", "http://example.com", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        treeBuilder.insert(commentToken);
        Element root = treeBuilder.doc;
        Node inserted = root.childNode(0);
        Assert.assertTrue(inserted instanceof XmlDeclaration);
        XmlDeclaration decl = (XmlDeclaration) inserted;
        Assert.assertEquals("TAG", decl.name());
        Assert.assertEquals("value", decl.attr("attr"));
    }

    @Test
    public void testBogusCommentShortData() {
        Token.Comment commentToken = new Token.Comment();
        commentToken.bogus = true;
        commentToken.data.append("?");
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        treeBuilder.initialiseParse("<root/>", "", ParseErrorList.noTracking(), ParseSettings.preserveCase);
        treeBuilder.insert(commentToken);
        Node inserted = treeBuilder.doc.childNode(0);
        Assert.assertTrue(inserted instanceof Comment);
        Assert.assertEquals("?", ((Comment) inserted).getData());

        Token.Comment emptyComment = new Token.Comment();
        emptyComment.bogus = true;
        treeBuilder.insert(emptyComment);
        Node insertedEmpty = treeBuilder.doc.childNode(1);
        Assert.assertTrue(insertedEmpty instanceof Comment);
        Assert.assertEquals("", ((Comment) insertedEmpty).getData());

        Token.Comment regularDataBogus = new Token.Comment();
        regularDataBogus.bogus = true;
        regularDataBogus.data.append("regular bogus comment");
        treeBuilder.insert(regularDataBogus);
        Node insertedReg = treeBuilder.doc.childNode(2);
        Assert.assertTrue(insertedReg instanceof Comment);
        Assert.assertEquals("regular bogus comment", ((Comment) insertedReg).getData());
    }

    @Test
    public void testDoctypeNode() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><root/>", "http://example.com");
        Assert.assertTrue(doc.childNode(0) instanceof DocumentType);
        DocumentType doctype = (DocumentType) doc.childNode(0);
        Assert.assertEquals("html", doctype.attr("name"));
        Assert.assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.attr("publicId"));
        Assert.assertEquals("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", doctype.attr("systemId"));
    }

    @Test
    public void testPopStackToClose() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<a><b><c>test</b></a>", "");
        Assert.assertEquals(1, doc.children().size());
        Element a = doc.child(0);
        Assert.assertEquals("a", a.tagName());
        Assert.assertEquals(1, a.children().size());
        Element b = a.child(0);
        Assert.assertEquals("b", b.tagName());
        Assert.assertEquals(1, b.children().size());
        Element c = b.child(0);
        Assert.assertEquals("c", c.tagName());
    }

    @Test
    public void testPopStackToCloseUnmatched() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        Document doc = treeBuilder.parse("<root></unmatched><child></child></root>", "");
        Assert.assertEquals(1, doc.children().size());
        Assert.assertEquals("root", doc.child(0).tagName());
        Assert.assertEquals(1, doc.child(0).children().size());
        Assert.assertEquals("child", doc.child(0).child(0).tagName());
    }

    @Test
    public void testParseFragment() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        List<Node> nodes = treeBuilder.parseFragment("<one>1</one><two>2</two>", "http://example.com", ParseErrorList.tracking(10), ParseSettings.preserveCase);
        Assert.assertEquals(2, nodes.size());
        Assert.assertTrue(nodes.get(0) instanceof Element);
        Assert.assertEquals("one", ((Element) nodes.get(0)).tagName());
        Assert.assertTrue(nodes.get(1) instanceof Element);
        Assert.assertEquals("two", ((Element) nodes.get(1)).tagName());
    }

    @Test
    public void testProcessAllTokenTypes() {
        XmlTreeBuilder treeBuilder = new XmlTreeBuilder();
        treeBuilder.initialiseParse("<root/>", "", ParseErrorList.noTracking(), ParseSettings.preserveCase);

        Token.StartTag start = new Token.StartTag();
        start.nameAttr("testNode", new Attributes());
        Assert.assertTrue(treeBuilder.process(start));

        Token.Character character = new Token.Character();
        character.data("text data");
        Assert.assertTrue(treeBuilder.process(character));

        Token.Comment comment = new Token.Comment();
        comment.data.append("comment data");
        Assert.assertTrue(treeBuilder.process(comment));

        Token.Doctype doctype = new Token.Doctype();
        doctype.name.append("html");
        Assert.assertTrue(treeBuilder.process(doctype));

        Token.EndTag endTag = new Token.EndTag();
        endTag.nameAttr("testNode", new Attributes());
        Assert.assertTrue(treeBuilder.process(endTag));

        Token.EOF eof = new Token.EOF();
        Assert.assertTrue(treeBuilder.process(eof));
    }

    @Test
    public void testJsoupXmlParserIntegration() {
        String xml = "<!DOCTYPE root><root attr=\"1\"><!-- c -->Text<self/></root>";
        Document doc = Jsoup.parse(xml, "", Parser.xmlParser());
        Assert.assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
        Assert.assertEquals("root", doc.selectFirst("root").tagName());
        Assert.assertEquals("1", doc.selectFirst("root").attr("attr"));
        Assert.assertEquals("Text", doc.selectFirst("root").ownText());
        Assert.assertNotNull(doc.selectFirst("self"));
    }
}
