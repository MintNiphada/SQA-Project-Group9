package org.jsoup.nodes;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DocumentTypeTest {

    @Test
    public void testConstructorAndAttributes() {
        DocumentType documentType = new DocumentType("html", "publicIdVal", "systemIdVal", "http://example.com");

        assertEquals("html", documentType.attr("name"));
        assertEquals("publicIdVal", documentType.attr("publicId"));
        assertEquals("systemIdVal", documentType.attr("systemId"));
        assertEquals("http://example.com", documentType.baseUri());
        assertEquals("#doctype", documentType.nodeName());
    }

    @Test
    public void testOuterHtmlHtml5() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("<!DOCTYPE html>", documentType.outerHtml());
        assertEquals("<!DOCTYPE html>", documentType.toString());
    }

    @Test
    public void testOuterHtmlWithPublicAndSystemIds() {
        DocumentType documentType = new DocumentType(
                "html",
                "-//W3C//DTD HTML 4.01//EN",
                "http://www.w3.org/TR/html4/strict.dtd",
                "http://example.com"
        );
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" http://www.w3.org/TR/html4/strict.dtd\">", documentType.outerHtml());
    }

    @Test
    public void testOuterHtmlWithPublicIdOnly() {
        DocumentType documentType = new DocumentType("html", "-//W3C//DTD HTML 4.01//EN", "", "");
        assertEquals("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\">", documentType.outerHtml());
    }

    @Test
    public void testOuterHtmlWithSystemIdOnly() {
        DocumentType documentType = new DocumentType("html", "", "http://www.w3.org/TR/html4/strict.dtd", "");
        assertEquals("<!DOCTYPE html http://www.w3.org/TR/html4/strict.dtd\">", documentType.outerHtml());
    }

    @Test
    public void testOuterHtmlWithWhitespaceIds() {
        DocumentType documentType = new DocumentType("html", "   ", "   ", "");
        assertEquals("<!DOCTYPE html>", documentType.outerHtml());
    }

    @Test
    public void testOuterHtmlHeadAndTailDirectly() {
        DocumentType documentType = new DocumentType("html", "pub", "sys", "http://example.com");
        StringBuilder accum = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();

        documentType.outerHtmlHead(accum, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"pub\" sys\">", accum.toString());

        documentType.outerHtmlTail(accum, 0, out);
        assertEquals("<!DOCTYPE html PUBLIC \"pub\" sys\">", accum.toString());
    }

    @Test
    public void testNodeName() {
        DocumentType documentType = new DocumentType("html", "", "", "");
        assertEquals("#doctype", documentType.nodeName());
    }
}
