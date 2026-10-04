package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class DocumentTypeTest {
    @Test
    public void testNodeName() {
        DocumentType dt = new DocumentType("html", null, null, "");
        assertEquals("#doctype", dt.nodeName());
    }

    @Test
    public void testOuterHtmlHeadBothNonBlank() {
        DocumentType dt = new DocumentType("html", "pub", "sys", "http://example.com");
        StringBuilder sb = new StringBuilder();
        dt.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html PUBLIC \"pub\" sys\">", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadOnlyPublicId() {
        DocumentType dt = new DocumentType("html", "pub", null, "");
        StringBuilder sb = new StringBuilder();
        dt.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html PUBLIC \"pub\">", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadOnlySystemId() {
        DocumentType dt = new DocumentType("html", null, "sys", "");
        StringBuilder sb = new StringBuilder();
        dt.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html sys\">", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadBothNull() {
        DocumentType dt = new DocumentType("html", null, null, "");
        StringBuilder sb = new StringBuilder();
        dt.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html>", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadBothEmpty() {
        DocumentType dt = new DocumentType("html", "", "", "");
        StringBuilder sb = new StringBuilder();
        dt.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html>", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadPublicIdWhitespaceOnly() {
        DocumentType dt = new DocumentType("html", "   ", "sys", "");
        StringBuilder sb = new StringBuilder();
        dt.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html sys\">", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadSystemIdWhitespaceOnly() {
        DocumentType dt = new DocumentType("html", "pub", "   ", "");
        StringBuilder sb = new StringBuilder();
        dt.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html PUBLIC \"pub\">", sb.toString());
    }

    @Test
    public void testOuterHtmlHeadAllWhitespace() {
        DocumentType dt = new DocumentType("html", "  ", "  ", "");
        StringBuilder sb = new StringBuilder();
        dt.outerHtmlHead(sb, 0, new Document.OutputSettings());
        assertEquals("<!DOCTYPE html>", sb.toString());
    }

    @Test
    public void testOuterHtmlTailDoesNothing() {
        DocumentType dt = new DocumentType("html", "pub", "sys", "");
        StringBuilder sb = new StringBuilder("content");
        dt.outerHtmlTail(sb, 0, new Document.OutputSettings());
        assertEquals("content", sb.toString());
    }
}
