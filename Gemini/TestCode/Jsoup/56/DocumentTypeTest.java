package org.jsoup.nodes;

import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class DocumentTypeTest {

    @Test
    public void testConstructorAndNodeName() {
        DocumentType docType = new DocumentType("html", "pub", "sys", "http://example.com");
        Assert.assertEquals("#doctype", docType.nodeName());
        Assert.assertEquals("html", docType.attr("name"));
        Assert.assertEquals("pub", docType.attr("publicId"));
        Assert.assertEquals("sys", docType.attr("systemId"));
        Assert.assertEquals("http://example.com", docType.baseUri());
    }

    @Test
    public void testHtml5DoctypeOuterHtml() {
        DocumentType docType = new DocumentType("html", "", "", "");
        Assert.assertEquals("<!doctype html>", docType.outerHtml());
    }

    @Test
    public void testHtml5DoctypeWithNullIds() {
        DocumentType docType = new DocumentType("html", null, null, "");
        Assert.assertEquals("<!doctype html>", docType.outerHtml());
    }

    @Test
    public void testHtml5DoctypeWithBlankIds() {
        DocumentType docType = new DocumentType("html", "   ", "\t", "");
        Assert.assertEquals("<!doctype html>", docType.outerHtml());
    }

    @Test
    public void testPublicAndSystemDoctype() {
        DocumentType docType = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "");
        Assert.assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", docType.outerHtml());
    }

    @Test
    public void testPublicOnlyDoctype() {
        DocumentType docType = new DocumentType("html", "-//W3C//DTD XHTML 1.0 Strict//EN", "", "");
        Assert.assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">", docType.outerHtml());
    }

    @Test
    public void testSystemOnlyDoctype() {
        DocumentType docType = new DocumentType("html", "", "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd", "");
        Assert.assertEquals("<!DOCTYPE html \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", docType.outerHtml());
    }

    @Test
    public void testXmlSyntaxDoctype() {
        DocumentType docType = new DocumentType("html", "", "", "");
        Document doc = new Document("");
        doc.outputSettings().syntax(Syntax.xml);
        doc.appendChild(docType);
        Assert.assertEquals("<!DOCTYPE html>", docType.outerHtml());
    }

    @Test
    public void testXmlSyntaxWithPublicAndSystem() {
        DocumentType docType = new DocumentType("html", "pub", "sys", "");
        Document doc = new Document("");
        doc.outputSettings().syntax(Syntax.xml);
        doc.appendChild(docType);
        Assert.assertEquals("<!DOCTYPE html PUBLIC \"pub\" \"sys\">", docType.outerHtml());
    }

    @Test
    public void testNoNameDoctype() {
        DocumentType docType = new DocumentType("", "", "", "");
        Assert.assertEquals("<!doctype>", docType.outerHtml());
    }

    @Test
    public void testNoNameWithPublicAndSystem() {
        DocumentType docType = new DocumentType("", "pub", "sys", "");
        Assert.assertEquals("<!DOCTYPE PUBLIC \"pub\" \"sys\">", docType.outerHtml());
    }

    @Test
    public void testBlankNameDoctype() {
        DocumentType docType = new DocumentType("   ", null, null, "");
        Assert.assertEquals("<!doctype>", docType.outerHtml());
    }

    @Test
    public void testOuterHtmlTail() throws IOException {
        DocumentType docType = new DocumentType("html", "", "", "");
        StringBuilder sb = new StringBuilder();
        docType.outerHtmlTail(sb, 0, new Document.OutputSettings());
        Assert.assertEquals("", sb.toString());
    }

    @Test
    public void testConstants() {
        Assert.assertEquals("PUBLIC", DocumentType.PUBLIC_KEY);
        Assert.assertEquals("SYSTEM", DocumentType.SYSTEM_KEY);
    }
}
