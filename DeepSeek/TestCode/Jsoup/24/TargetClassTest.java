package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Doctype;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Entities;
import java.util.List;

public class TokeniserStateTest {

    @Test
    public void testDataWithText() {
        Document doc = Jsoup.parse("hello");
        assertEquals("hello", doc.text());
    }

    @Test
    public void testDataWithCharacterReference() {
        Document doc = Jsoup.parse("&amp;");
        assertEquals("&", doc.text());
    }

    @Test
    public void testDataWithTagOpen() {
        Document doc = Jsoup.parse("<p>text</p>");
        assertEquals("text", doc.select("p").text());
    }

    @Test
    public void testDataWithNullChar() {
        Document doc = Jsoup.parse("\u0000");
        assertEquals("\uFFFD", doc.text());
    }

    @Test
    public void testDataWithEof() {
        Document doc = Jsoup.parse("");
        assertEquals("", doc.text());
    }

    @Test
    public void testCharacterReferenceInDataNull() {
        Document doc = Jsoup.parse("&unknown;");
        assertTrue(doc.text().contains("&"));
    }

    @Test
    public void testRcdataNormalText() {
        Document doc = Jsoup.parse("<title>hello</title>");
        assertEquals("hello", doc.title());
    }

    @Test
    public void testRcdataWithCharacterReference() {
        Document doc = Jsoup.parse("<title>&amp;</title>");
        assertEquals("&", doc.title());
    }

    @Test
    public void testRcdataWithEndTag() {
        Document doc = Jsoup.parse("<title>ok</title>done");
        assertEquals("ok", doc.title());
        assertTrue(doc.text().contains("done"));
    }

    @Test
    public void testRcdataWithNullChar() {
        Document doc = Jsoup.parse("<title>a\u0000b</title>");
        assertTrue(doc.title().contains("\uFFFD"));
    }

    @Test
    public void testRcdataWithEof() {
        Document doc = Jsoup.parse("<title>unclosed");
        assertEquals("unclosed", doc.title());
    }

    @Test
    public void testCharacterReferenceInRcdataNull() {
        Document doc = Jsoup.parse("<title>&unknown;</title>");
        assertEquals("&unknown;", doc.title());
    }

    @Test
    public void testRawtextNormal() {
        Document doc = Jsoup.parse("<script>var x = 1;</script>");
        assertTrue(doc.select("script").html().contains("var x = 1;"));
    }

    @Test
    public void testRawtextWithNullChar() {
        Document doc = Jsoup.parse("<script>a\u0000b</script>");
        assertTrue(doc.select("script").html().contains("\uFFFD"));
    }

    @Test
    public void testRawtextWithEof() {
        Document doc = Jsoup.parse("<script>no close");
        assertEquals("no close", doc.select("script").html());
    }

    @Test
    public void testScriptDataNormal() {
        Document doc = Jsoup.parse("<script>var y = 2;</script>");
        assertTrue(doc.select("script").html().contains("var y = 2;"));
    }

    @Test
    public void testScriptDataNullChar() {
        Document doc = Jsoup.parse("<script>a\u0000b</script>");
        assertTrue(doc.select("script").html().contains("\uFFFD"));
    }

    @Test
    public void testPlaintext() {
        Document doc = Jsoup.parse("<plaintext>hello</plaintext>");
        assertTrue(doc.text().contains("hello</plaintext>"));
    }

    @Test
    public void testPlaintextNullChar() {
        Document doc = Jsoup.parse("<plaintext>a\u0000b</plaintext>");
        assertTrue(doc.text().contains("\uFFFD"));
    }

    @Test
    public void testPlaintextEof() {
        Document doc = Jsoup.parse("<plaintext>forever");
        assertEquals("forever", doc.text());
    }

    @Test
    public void testTagOpenExclamation() {
        Document doc = Jsoup.parse("<!-- comment -->");
        assertEquals(0, doc.select("comment").size());
    }

    @Test
    public void testTagOpenSlash() {
        Document doc = Jsoup.parse("<p>para</p>");
        assertEquals("para", doc.select("p").text());
    }

    @Test
    public void testTagOpenQuestion() {
        Document doc = Jsoup.parse("<?xml version=\"1.0\"?>");
        assertNotNull(doc.childNode(0));
    }

    @Test
    public void testTagOpenLetter() {
        Document doc = Jsoup.parse("<div>block</div>");
        assertEquals("block", doc.select("div").text());
    }

    @Test
    public void testTagOpenNonLetter() {
        Document doc = Jsoup.parse("<1notag>text</1notag>");
        assertEquals("<1notag>text</1notag>", doc.body().html());
    }

    @Test
    public void testEndTagOpenLetter() {
        Document doc = Jsoup.parse("<p>text</p>");
        assertEquals("text", doc.select("p").text());
    }

    @Test
    public void testEndTagOpenBracket() {
        Document doc = Jsoup.parse("<p>text</>");
        assertNotNull(doc.select("p").first());
    }

    @Test
    public void testEndTagOpenEof() {
        Document doc = Jsoup.parse("<p>text</");
        assertTrue(doc.select("p").text().contains("text"));
    }

    @Test
    public void testEndTagOpenNonLetter() {
        Document doc = Jsoup.parse("<p>text</1>");
        assertNotNull(doc.select("p").first());
    }

    @Test
    public void testTagNameWithSpaces() {
        Document doc = Jsoup.parse("<div   >text</div>");
        assertEquals("div", doc.select("div").first().tagName());
    }

    @Test
    public void testTagNameSelfClosing() {
        Document doc = Jsoup.parse("<br/>");
        assertEquals("br", doc.select("br").first().tagName());
    }

    @Test
    public void testTagNameClose() {
        Document doc = Jsoup.parse("<p>text</p>");
        assertEquals("p", doc.select("p").first().tagName());
    }

    @Test
    public void testTagNameNullChar() {
        Document doc = Jsoup.parse("<a\u0000>text</a\u0000>");
        assertNotNull(doc.select("a").first());
    }

    @Test
    public void testTagNameEof() {
        Document doc = Jsoup.parse("<unclosed");
        assertNotNull(doc.select("unclosed").first());
    }

    @Test
    public void testRcdataLessthanSignMatchingEndTag() {
        Document doc = Jsoup.parse("<title>text</title> post");
        assertEquals("text", doc.title());
        assertTrue(doc.text().contains("post"));
    }

    @Test
    public void testRcdataLessthanSignNoEndTag() {
        Document doc = Jsoup.parse("<title>text <span> etc");
        assertTrue(doc.title().contains("text"));
    }

    @Test
    public void testRCDATAEndTagOpenLetter() {
        Document doc = Jsoup.parse("<title>text</title> ok");
        assertEquals("text", doc.title());
        assertTrue(doc.text().contains("ok"));
    }

    @Test
    public void testRCDATAEndTagOpenNonLetter() {
        Document doc = Jsoup.parse("<title>text</ 123>");
        assertTrue(doc.title().contains("text"));
    }

    @Test
    public void testRCDATAEndTagNameWhitespace() {
        Document doc = Jsoup.parse("<title>text</title   >rest");
        assertEquals("text", doc.title());
        assertTrue(doc.text().contains("rest"));
    }

    @Test
    public void testRCDATAEndTagNameSlash() {
        Document doc = Jsoup.parse("<title>text</title/>rest");
        assertEquals("text", doc.title());
        assertTrue(doc.text().contains("rest"));
    }

    @Test
    public void testRCDATAEndTagNameGreater() {
        Document doc = Jsoup.parse("<title>text</title>rest");
        assertEquals("text", doc.title());
        assertTrue(doc.text().contains("rest"));
    }

    @Test
    public void testRCDATAEndTagNameDefault() {
        Document doc = Jsoup.parse("<title>text</titlez>");
        assertTrue(doc.title().contains("text"));
    }

    @Test
    public void testRawtextLessthanSignWithSlash() {
        Document doc = Jsoup.parse("<script>text</script>rest");
        assertEquals("text</script>rest", doc.select("script").html());
    }

    @Test
    public void testRawtextLessthanSignWithoutSlash() {
        Document doc = Jsoup.parse("<script>text< /script>");
        assertTrue(doc.select("script").html().contains("text<"));
    }

    @Test
    public void testRawtextEndTagOpenLetter() {
        Document doc = Jsoup.parse("<script>text</script>rest");
        assertEquals("text", doc.select("script").html());
    }

    @Test
    public void testRawtextEndTagOpenNonLetter() {
        Document doc = Jsoup.parse("<script>text</ 1>ignore");
        assertTrue(doc.select("script").html().contains("text"));
    }

    @Test
    public void testRawtextEndTagNameWhitespace() {
        Document doc = Jsoup.parse("<script>text</script  >rest");
        assertEquals("text", doc.select("script").html());
    }

    @Test
    public void testRawtextEndTagNameSlash() {
        Document doc = Jsoup.parse("<script>text</script/>rest");
        assertEquals("text", doc.select("script").html());
    }

    @Test
    public void testRawtextEndTagNameGreater() {
        Document doc = Jsoup.parse("<script>text</script>rest");
        assertEquals("text", doc.select("script").html());
    }

    @Test
    public void testRawtextEndTagNameDefault() {
        Document doc = Jsoup.parse("<script>text</scriptbad>");
        assertTrue(doc.select("script").html().contains("text"));
    }

    @Test
    public void testScriptDataLessthanSignSlash() {
        Document doc = Jsoup.parse("<script>text</script>rest");
        assertEquals("text", doc.select("script").html());
    }

    @Test
    public void testScriptDataLessthanSignExclamation() {
        Document doc = Jsoup.parse("<script>text<!-- comment</script>");
        assertTrue(doc.select("script").html().contains("text"));
    }

    @Test
    public void testScriptDataLessthanSignDefault() {
        Document doc = Jsoup.parse("<script>text<bad</script>");
        assertTrue(doc.select("script").html().contains("text<bad"));
    }

    @Test
    public void testScriptDataEndTagOpenLetter() {
        Document doc = Jsoup.parse("<script>text</script>rest");
        assertEquals("text", doc.select("script").html());
    }

    @Test
    public void testScriptDataEndTagOpenNonLetter() {
        Document doc = Jsoup.parse("<script>text</1>ignore");
        assertTrue(doc.select("script").html().contains("text"));
    }

    @Test
    public void testScriptDataEndTagNameWhitespace() {
        Document doc = Jsoup.parse("<script>text</script   >rest");
        assertEquals("text", doc.select("script").html());
    }

    @Test
    public void testScriptDataEndTagNameSlash() {
        Document doc = Jsoup.parse("<script>text</script/>rest");
        assertEquals("text", doc.select("script").html());
    }

    @Test
    public void testScriptDataEndTagNameGreater() {
        Document doc = Jsoup.parse("<script>text</script>rest");
        assertEquals("text", doc.select("script").html());
    }

    @Test
    public void testScriptDataEndTagNameDefault() {
        Document doc = Jsoup.parse("<script>text</scriptbad>");
        assertTrue(doc.select("script").html().contains("text"));
    }

    @Test
    public void testScriptDataEscapeStartMinus() {
        Document doc = Jsoup.parse("<script>text<!-- # --></script>");
        assertTrue(doc.select("script").html().contains("text"));
    }

    @Test
    public void testScriptDataEscapeStartNoMinus() {
        Document doc = Jsoup.parse("<script>text<!-</script>");
        assertTrue(doc.select("script").html().contains("text<!-"));
    }

    @Test
    public void testScriptDataEscapeStartDashMinus() {
        Document doc = Jsoup.parse("<script>text<!---</script>");
        assertTrue(doc.select("script").html().contains("text<!--"));
    }

    @Test
    public void testScriptDataEscapeStartDashNoMinus() {
        Document doc = Jsoup.parse("<script>text<!--bad</script>");
        assertTrue(doc.select("script").html().contains("text"));
    }

    @Test
    public void testScriptDataEscapedMinus() {
        Document doc = Jsoup.parse("<script><!-- text -- ></script>");
        assertTrue(doc.select("script").html().contains("text"));
    }

    @Test
    public void testScriptDataEscapedLessThan() {
        Document doc = Jsoup.parse("<script><!-- <tag --></script>");
        assertTrue(doc.select("script").html().contains("<tag"));
    }

    @Test
    public void testScriptDataEscapedNullChar() {
        Document doc = Jsoup.parse("<script><!-- \u0000 --></script>");
        assertTrue(doc.select("script").html().contains("\uFFFD"));
    }

    @Test
    public void testScriptDataEscapedEof() {
        Document doc = Jsoup.parse("<script><!-- unterminated");
        assertNotNull(doc.select("script").first());
    }

    @Test
    public void testScriptDataEscapedDashMinus() {
        Document doc = Jsoup.parse("<script><!-- -text--></script>");
        assertTrue(doc.select("script").html().contains("-text"));
    }

    @Test
    public void testScriptDataEscapedDashLessThan() {
        Document doc = Jsoup.parse("<script><!-- -<tag--></script>");
        assertTrue(doc.select("script").html().contains("<tag"));
    }

    @Test
    public void testScriptDataEscapedDashNullChar() {
        Document doc = Jsoup.parse("<script><!-- -\u0000--></script>");
        assertTrue(doc.select("script").html().contains("\uFFFD"));
    }

    @Test
    public void testScriptDataEscapedDashDefault() {
        Document doc = Jsoup.parse("<script><!-- -x--></script>");
        assertTrue(doc.select("script").html().contains("-x"));
    }

    @Test
    public void testScriptDataEscapedDashDashMinus() {
        Document doc = Jsoup.parse("<script><!-- --text--></script>");
        assertTrue(doc.select("script").html().contains("--text"));
    }

    @Test
    public void testScriptDataEscapedDashDashLessThan() {
        Document doc = Jsoup.parse("<script><!-- --<tag--></script>");
        assertTrue(doc.select("script").html().contains("<tag"));
    }

    @Test
    public void testScriptDataEscapedDashDashGreater() {
        Document doc = Jsoup.parse("<script><!-- -- >--></script>");
        assertTrue(doc.select("script").html().contains("-- >"));
    }

    @Test
    public void testScriptDataEscapedDashDashNullChar() {
        Document doc = Jsoup.parse("<script><!-- --\u0000--></script>");
        assertTrue(doc.select("script").html().contains("\uFFFD"));
    }

    @Test
    public void testScriptDataEscapedLessthanSignLetter() {
        Document doc = Jsoup.parse("<script><!--<script>inner</script>--></script>");
        assertTrue(doc.select("script").html().contains("inner"));
    }

    @Test
    public void testScriptDataEscapedLessthanSignSlash() {
        Document doc = Jsoup.parse("<script><!--</script>--></script>");
        assertEquals("<!--</script>-->", doc.select("script").html());
    }

    @Test
    public void testBeforeAttributeNameWhitespace() {
        Document doc = Jsoup.parse("<div  id=test>text</div>");
        assertEquals("test", doc.select("div").attr("id"));
    }

    @Test
    public void testBeforeAttributeNameSlash() {
        Document doc = Jsoup.parse("<br/>");
        assertEquals("br", doc.select("br").first().tagName());
    }

    @Test
    public void testBeforeAttributeNameGreater() {
        Document doc = Jsoup.parse("<div>text</div>");
        assertEquals("div", doc.select("div").first().tagName());
    }

    @Test
    public void testBeforeAttributeNameNullChar() {
        Document doc = Jsoup.parse("<div\u0000>text</div>");
        assertNotNull(doc.select("div").first());
    }

    @Test
    public void testBeforeAttributeNameQuote() {
        Document doc = Jsoup.parse("<div \">text</div>");
        assertTrue(doc.select("div").attr("\"").isEmpty());
    }

    @Test
    public void testBeforeAttributeNameEqual() {
        Document doc = Jsoup.parse("<div =>text</div>");
        assertTrue(doc.select("div").attr("=").isEmpty());
    }

    @Test
    public void testAttributeNameStandard() {
        Document doc = Jsoup.parse("<div id=\"test\">text</div>");
        assertEquals("test", doc.select("div").attr("id"));
    }

    @Test
    public void testAttributeNameSpecialChars() {
        Document doc = Jsoup.parse("<div a\"b=c>text</div>");
        assertTrue(doc.select("div").attributes().hasKey("a\"b"));
    }

    @Test
    public void testAfterAttributeNameSlash() {
        Document doc = Jsoup.parse("<div id=test />text");
        assertEquals("div", doc.select("div").first().tagName());
    }

    @Test
    public void testBeforeAttributeValueDoubleQuote() {
        Document doc = Jsoup.parse("<div id=\"test\">text</div>");
        assertEquals("test", doc.select("div").attr("id"));
    }

    @Test
    public void testBeforeAttributeValueAmpersand() {
        Document doc = Jsoup.parse("<div id=&amp;>text</div>");
        assertEquals("&", doc.select("div").attr("id"));
    }

    @Test
    public void testBeforeAttributeValueSingleQuote() {
        Document doc = Jsoup.parse("<div id='test'>text</div>");
        assertEquals("test", doc.select("div").attr("id"));
    }

    @Test
    public void testBeforeAttributeValueNullChar() {
        Document doc = Jsoup.parse("<div id=\u0000>text</div>");
        assertEquals("\uFFFD", doc.select("div").attr("id"));
    }

    @Test
    public void testBeforeAttributeValueGreater() {
        Document doc = Jsoup.parse("<div id>text</div>");
        assertTrue(doc.select("div").hasAttr("id"));
    }

    @Test
    public void testAttributeValueDoubleQuotedNormal() {
        Document doc = Jsoup.parse("<div id=\"test\">text</div>");
        assertEquals("test", doc.select("div").attr("id"));
    }

    @Test
    public void testAttributeValueDoubleQuotedAmpersand() {
        Document doc = Jsoup.parse("<div id=\"&amp;\">text</div>");
        assertEquals("&", doc.select("div").attr("id"));
    }

    @Test
    public void testAttributeValueDoubleQuotedNullChar() {
        Document doc = Jsoup.parse("<div id=\"a\u0000b\">text</div>");
        assertTrue(doc.select("div").attr("id").contains("\uFFFD"));
    }

    @Test
    public void testAttributeValueSingleQuotedAmpersand() {
        Document doc = Jsoup.parse("<div id='&amp;'>text</div>");
        assertEquals("&", doc.select("div").attr("id"));
    }

    @Test
    public void testAttributeValueUnquotedSpace() {
        Document doc = Jsoup.parse("<div id=test class=foo>text</div>");
        assertEquals("test", doc.select("div").attr("id"));
        assertEquals("foo", doc.select("div").attr("class"));
    }

    @Test
    public void testAttributeValueUnquotedAmpersand() {
        Document doc = Jsoup.parse("<div id=&amp;>text</div>");
        assertEquals("&", doc.select("div").attr("id"));
    }

    @Test
    public void testAttributeValueUnquotedGreater() {
        Document doc = Jsoup.parse("<div id=test>text</div>");
        assertEquals("test", doc.select("div").attr("id"));
    }

    @Test
    public void testAttributeValueUnquotedNullChar() {
        Document doc = Jsoup.parse("<div id=a\u0000b>text</div>");
        assertTrue(doc.select("div").attr("id").contains("\uFFFD"));
    }

    @Test
    public void testAfterAttributeValueQuotedSpace() {
        Document doc = Jsoup.parse("<div id=\"test\" class=foo>text</div>");
        assertEquals("test", doc.select("div").attr("id"));
    }

    @Test
    public void testAfterAttributeValueQuotedSlash() {
        Document doc = Jsoup.parse("<div id=\"test\"/>text");
        assertEquals("div", doc.select("div").first().tagName());
    }

    @Test
    public void testAfterAttributeValueQuotedGreater() {
        Document doc = Jsoup.parse("<div id=\"test\">text</div>");
        assertEquals("test", doc.select("div").attr("id"));
    }

    @Test
    public void testSelfClosingStartTagGreater() {
        Document doc = Jsoup.parse("<br/>");
        assertTrue(doc.select("br").first().isSelfClosing());
    }

    @Test
    public void testSelfClosingStartTagEof() {
        Document doc = Jsoup.parse("<br/");
        assertNotNull(doc.select("br").first());
    }

    @Test
    public void testSelfClosingStartTagDefault() {
        Document doc = Jsoup.parse("<br/ >text");
        assertNotNull(doc.select("br").first());
    }

    @Test
    public void testBogusComment() {
        Document doc = Jsoup.parse("<? comment -->");
        List<Comment> comments = doc.select("comment");
        assertTrue(comments.size() > 0);
    }

    @Test
    public void testMarkupDeclarationOpenComment() {
        Document doc = Jsoup.parse("<!-- comment -->");
        assertNotNull(doc.childNode(0));
    }

    @Test
    public void testMarkupDeclarationOpenDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        Doctype doctype = doc.childNode(0) instanceof Doctype ? (Doctype) doc.childNode(0) : null;
        assertNotNull(doctype);
    }

    @Test
    public void testMarkupDeclarationOpenCdata() {
        Document doc = Jsoup.parse("<![CDATA[ content ]]>");
        assertTrue(doc.text().contains("content"));
    }

    @Test
    public void testMarkupDeclarationOpenBogus() {
        Document doc = Jsoup.parse("<!bogus>");
        List<Comment> comments = doc.select("comment");
        assertTrue(comments.size() > 0);
    }

    @Test
    public void testCommentStartDash() {
        Document doc = Jsoup.parse("<!-- - comment -->");
        assertTrue(doc.childNode(0) instanceof Comment);
    }

    @Test
    public void testCommentStartNullChar() {
        Document doc = Jsoup.parse("<!-- \u0000 comment -->");
        Comment comment = (Comment) doc.childNode(0);
        assertTrue(comment.getData().contains("\uFFFD"));
    }

    @Test
    public void testCommentStartGreater() {
        Document doc = Jsoup.parse("<!-->");
        assertNotNull(doc.childNode(0));
    }

    @Test
    public void testCommentStartDashNullChar() {
        Document doc = Jsoup.parse("<!-- - \u0000 comment -->");
        assertTrue(doc.childNode(0) instanceof Comment);
    }

    @Test
    public void testCommentStartDashGreater() {
        Document doc = Jsoup.parse("<!-- - ->");
        assertNotNull(doc.childNode(0));
    }

    @Test
    public void testCommentDefault() {
        Document doc = Jsoup.parse("<!-- comment >");
        Comment comment = (Comment) doc.childNode(0);
        assertTrue(comment.getData().contains("comment"));
    }

    @Test
    public void testCommentEndDashMinus() {
        Document doc = Jsoup.parse("<!-- comment --!>");
        Comment comment = (Comment) doc.childNode(0);
        assertTrue(comment.getData().contains("comment"));
    }

    @Test
    public void testCommentEndDashNullChar() {
        Document doc = Jsoup.parse("<!-- comment - \u0000 >");
        assertNotNull(doc.childNode(0));
    }

    @Test
    public void testCommentEndDashEof() {
        Document doc = Jsoup.parse("<!-- comment -");
        assertNotNull(doc.childNode(0));
    }

    @Test
    public void testCommentEndGreater() {
        Document doc = Jsoup.parse("<!-- comment -->");
        assertTrue(doc.childNode(0) instanceof Comment);
    }

    @Test
    public void testCommentEndNullChar() {
        Document doc = Jsoup.parse("<!-- comment \u0000->");
        assertNotNull(doc.childNode(0));
    }

    @Test
    public void testCommentEndExclamation() {
        Document doc = Jsoup.parse("<!-- comment --!>");
        Comment comment = (Comment) doc.childNode(0);
        assertTrue(comment.getData().contains("comment"));
    }

    @Test
    public void testCommentEndBangDefault() {
        Document doc = Jsoup.parse("<!-- comment --!extra>");
        Comment comment = (Comment) doc.childNode(0);
        assertTrue(comment.getData().contains("extra"));
    }

    @Test
    public void testDoctypeWhitespace() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        Doctype doctype = (Doctype) doc.childNode(0);
        assertEquals("html", doctype.name());
    }

    @Test
    public void testDoctypeEof() {
        Document doc = Jsoup.parse("<!DOCTYPE");
        assertNotNull(doc.childNode(0));
    }

    @Test
    public void testBeforeDoctypeNameLetter() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        assertEquals("html", ((Doctype) doc.childNode(0)).name());
    }

    @Test
    public void testBeforeDoctypeNameNullChar() {
        Document doc = Jsoup.parse("<!DOCTYPE \u0000>");
        assertTrue(((Doctype) doc.childNode(0)).name().contains("\uFFFD"));
    }

    @Test
    public void testDoctypeNameLetter() {
        Document doc = Jsoup.parse("<!DOCTYPE HTML>");
        assertEquals("html", ((Doctype) doc.childNode(0)).name());
    }

    @Test
    public void testDoctypeNameGreater() {
        Document doc = Jsoup.parse("<!DOCTYPE html>");
        assertNotNull(doc.childNode(0));
    }

    @Test
    public void testAfterDoctypeNamePublic() {
        Document doc = Jsoup.parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">");
        Doctype doctype = (Doctype) doc.childNode(0);
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.publicIdentifier());
    }

    @Test
    public void testAfterDoctypePublicKeywordDoubleQuoted() {
        Document doc = Jsoup.parse("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">");
        Doctype doctype = (Doctype) doc.childNode(0);
        assertEquals("-//W3C//DTD XHTML 1.0 Strict//EN", doctype.publicIdentifier());
    }

    @Test
    public void testAfterDoctypePublicIdentifier() {
        Document doc = Jsoup.parse("<!DOCTYPE html PUBLIC id \"uri\">");
        Doctype doctype = (Doctype) doc.childNode(0);
        assertNotNull(doctype.getPublicIdentifier());
    }

    @Test
    public void testBogusDoctype() {
        Document doc = Jsoup.parse("<!DOCTYPE html∫>");
        assertTrue(((Doctype) doc.childNode(0)).isForceQuirks());
    }

    @Test
    public void testCdataSection() {
        Document doc = Jsoup.parse("<svg><![CDATA[ data ]]></svg>");
        assertTrue(doc.text().contains("data"));
    }
}
