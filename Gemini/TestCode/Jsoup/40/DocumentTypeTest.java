package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

public class DocumentTypeTest {

    @Test
    public void testConstructorAndNodeName() {
        DocumentType docType = new DocumentType("html", "publicIdVal", "systemIdVal", "http://example.com/");
        Assert.assertEquals("#doctype", docType.nodeName());
        Assert.assertEquals("html", docType.attr("name"));
        Assert.assertEquals("publicIdVal", docType.attr("publicId"));
        Assert.assertEquals("systemIdVal", docType.attr("systemId"));
        Assert.assertEquals("http://example.com/", docType.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullName() {
        new DocumentType(null, "publicId", "systemId", "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyName() {
        new DocumentType("", "publicId", "systemId", "http://example.com/");
    }

    @Test
    public void testOuterHtmlSimple() {
        DocumentType docType = new DocumentType("html", "", "", "");
        Assert.assertEquals("<!DOCTYPE html>", docType.outerHtml());
    }

    @Test
    public void testOuterHtmlPublicAndSystemIds() {
        DocumentType docType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "http://www.w3.org/TR/html4/strict.dtd", "");
        Assert.assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">", docType.outerHtml());
    }

    @Test
    public void testOuterHtmlPublicIdOnly() {
        DocumentType docType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        Assert.assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", docType.outerHtml());
    }

    @Test
    public void testOuterHtmlSystemIdOnly() {
        DocumentType docType = new DocumentType("html", "", "http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd", "");
        Assert.assertEquals("<!DOCTYPE html \"http://www.ibm.com/data/dtd/v11/ibmxhtml1-transitional.dtd\">", docType.outerHtml());
    }

    @Test
    public void testOuterHtmlWithBlankName() {
        DocumentType docType = new DocumentType("temp", "", "", "");
        docType.attr("name", "");
        Assert.assertEquals("<!DOCTYPE>", docType.outerHtml());
    }

    @Test
    public void testOuterHtmlTailDoesNothing() {
        DocumentType docType = new DocumentType("html", "", "", "");
        StringBuilder accum = new StringBuilder();
        docType.outerHtmlTail(accum, 0, new Document("").outputSettings());
        Assert.assertEquals("", accum.toString());
    }
}
