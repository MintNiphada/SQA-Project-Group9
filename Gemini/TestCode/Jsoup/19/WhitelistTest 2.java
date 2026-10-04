package org.jsoup.safety;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WhitelistTest {

    @Test
    public void testNoneWhitelist() {
        Whitelist whitelist = Whitelist.none();
        assertNotNull(whitelist);
        assertFalse(whitelist.isSafeTag("p"));
        assertFalse(whitelist.isSafeTag("b"));
        assertFalse(whitelist.isSafeTag("a"));
    }

    @Test
    public void testSimpleTextWhitelist() {
        Whitelist whitelist = Whitelist.simpleText();
        assertTrue(whitelist.isSafeTag("b"));
        assertTrue(whitelist.isSafeTag("em"));
        assertTrue(whitelist.isSafeTag("i"));
        assertTrue(whitelist.isSafeTag("strong"));
        assertTrue(whitelist.isSafeTag("u"));
        assertFalse(whitelist.isSafeTag("a"));
        assertFalse(whitelist.isSafeTag("img"));
    }

    @Test
    public void testBasicWhitelist() {
        Whitelist whitelist = Whitelist.basic();
        assertTrue(whitelist.isSafeTag("a"));
        assertTrue(whitelist.isSafeTag("blockquote"));
        assertTrue(whitelist.isSafeTag("p"));
        assertTrue(whitelist.isSafeTag("q"));
        assertFalse(whitelist.isSafeTag("img"));

        Attributes enforced = whitelist.getEnforcedAttributes("a");
        assertEquals("nofollow", enforced.get("rel"));

        Element elValid = new Element(Tag.valueOf("a"), "http://example.com/");
        elValid.attr("href", "http://example.com/page");
        Attribute attrValid = elValid.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("a", elValid, attrValid));

        Element elInvalid = new Element(Tag.valueOf("a"), "http://example.com/");
        elInvalid.attr("href", "javascript:alert(1)");
        Attribute attrInvalid = elInvalid.attributes().asList().get(0);
        assertFalse(whitelist.isSafeAttribute("a", elInvalid, attrInvalid));
    }

    @Test
    public void testBasicWithImagesWhitelist() {
        Whitelist whitelist = Whitelist.basicWithImages();
        assertTrue(whitelist.isSafeTag("a"));
        assertTrue(whitelist.isSafeTag("img"));

        Element imgValid = new Element(Tag.valueOf("img"), "http://example.com/");
        imgValid.attr("src", "https://example.com/test.png");
        Attribute srcAttr = imgValid.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("img", imgValid, srcAttr));

        Element imgInvalid = new Element(Tag.valueOf("img"), "http://example.com/");
        imgInvalid.attr("src", "ftp://example.com/test.png");
        Attribute srcInvalidAttr = imgInvalid.attributes().asList().get(0);
        assertFalse(whitelist.isSafeAttribute("img", imgInvalid, srcInvalidAttr));
    }

    @Test
    public void testRelaxedWhitelist() {
        Whitelist whitelist = Whitelist.relaxed();
        assertTrue(whitelist.isSafeTag("div"));
        assertTrue(whitelist.isSafeTag("h1"));
        assertTrue(whitelist.isSafeTag("table"));
        assertTrue(whitelist.isSafeTag("td"));
        assertTrue(whitelist.isSafeTag("img"));
        assertTrue(whitelist.isSafeTag("a"));

        Element colEl = new Element(Tag.valueOf("col"), "http://example.com/");
        colEl.attr("span", "2");
        Attribute spanAttr = colEl.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("col", colEl, spanAttr));

        Element thEl = new Element(Tag.valueOf("th"), "http://example.com/");
        thEl.attr("scope", "row");
        Attribute scopeAttr = thEl.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("th", thEl, scopeAttr));
    }

    @Test
    public void testAddTags() {
        Whitelist whitelist = new Whitelist();
        assertFalse(whitelist.isSafeTag("custom"));
        whitelist.addTags("custom", "other");
        assertTrue(whitelist.isSafeTag("custom"));
        assertTrue(whitelist.isSafeTag("other"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagsNull() {
        new Whitelist().addTags((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagsEmptyString() {
        new Whitelist().addTags("");
    }

    @Test
    public void testAddAttributes() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("p", "a");
        whitelist.addAttributes("p", "class", "style");
        whitelist.addAttributes("p", "id");

        Element pEl = new Element(Tag.valueOf("p"), "http://example.com/");
        pEl.attr("class", "my-class");
        Attribute classAttr = pEl.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("p", pEl, classAttr));

        pEl.attr("id", "my-id");
        Attribute idAttr = pEl.attributes().asList().get(1);
        assertTrue(whitelist.isSafeAttribute("p", pEl, idAttr));

        pEl.attr("title", "my-title");
        Attribute titleAttr = pEl.attributes().asList().get(2);
        assertFalse(whitelist.isSafeAttribute("p", pEl, titleAttr));
    }

    @Test
    public void testAllTagAttributes() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("div", "span");
        whitelist.addAttributes(":all", "class");

        Element divEl = new Element(Tag.valueOf("div"), "http://example.com/");
        divEl.attr("class", "container");
        Attribute classAttr = divEl.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("div", divEl, classAttr));

        Element spanEl = new Element(Tag.valueOf("span"), "http://example.com/");
        spanEl.attr("id", "span-id");
        Attribute idAttr = spanEl.attributes().asList().get(0);
        assertFalse(whitelist.isSafeAttribute("span", spanEl, idAttr));

        Element allEl = new Element(Tag.valueOf(":all"), "http://example.com/");
        allEl.attr("class", "container");
        Attribute allClassAttr = allEl.attributes().asList().get(0);
        assertFalse(whitelist.isSafeAttribute(":all", allEl, allClassAttr));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesNullTag() {
        new Whitelist().addAttributes(null, "class");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesEmptyTag() {
        new Whitelist().addAttributes("", "class");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesNullKeys() {
        new Whitelist().addAttributes("div", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesEmptyKey() {
        new Whitelist().addAttributes("div", "");
    }

    @Test
    public void testAddEnforcedAttribute() {
        Whitelist whitelist = new Whitelist();
        whitelist.addEnforcedAttribute("a", "rel", "nofollow");
        whitelist.addEnforcedAttribute("a", "target", "_blank");

        Attributes attrs = whitelist.getEnforcedAttributes("a");
        assertEquals("nofollow", attrs.get("rel"));
        assertEquals("_blank", attrs.get("target"));

        Attributes nonExistent = whitelist.getEnforcedAttributes("p");
        assertEquals(0, nonExistent.size());

        whitelist.addEnforcedAttribute("a", "rel", "noopener");
        assertEquals("noopener", whitelist.getEnforcedAttributes("a").get("rel"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullTag() {
        new Whitelist().addEnforcedAttribute(null, "rel", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeEmptyTag() {
        new Whitelist().addEnforcedAttribute("", "rel", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullKey() {
        new Whitelist().addEnforcedAttribute("a", null, "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeEmptyKey() {
        new Whitelist().addEnforcedAttribute("a", "", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullValue() {
        new Whitelist().addEnforcedAttribute("a", "rel", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeEmptyValue() {
        new Whitelist().addEnforcedAttribute("a", "rel", "");
    }

    @Test
    public void testAddProtocolsAndPreserveRelativeLinks() {
        Whitelist whitelist = new Whitelist();
        whitelist.addTags("a");
        whitelist.addAttributes("a", "href", "title");
        whitelist.addProtocols("a", "href", "http", "https");
        whitelist.addProtocols("a", "href", "mailto");

        Element aEl1 = new Element(Tag.valueOf("a"), "http://example.com/");
        aEl1.attr("href", "http://example.com/index.html");
        Attribute hrefAttr1 = aEl1.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("a", aEl1, hrefAttr1));

        Element aEl2 = new Element(Tag.valueOf("a"), "http://example.com/");
        aEl2.attr("href", "relative/path");
        Attribute hrefAttr2 = aEl2.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("a", aEl2, hrefAttr2));
        assertEquals("http://example.com/relative/path", hrefAttr2.getValue());

        Element aEl3 = new Element(Tag.valueOf("a"), "http://example.com/");
        aEl3.attr("title", "Some Title");
        Attribute titleAttr = aEl3.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("a", aEl3, titleAttr));

        whitelist.preserveRelativeLinks(true);
        Element aEl4 = new Element(Tag.valueOf("a"), "http://example.com/");
        aEl4.attr("href", "relative/path");
        Attribute hrefAttr4 = aEl4.attributes().asList().get(0);
        assertTrue(whitelist.isSafeAttribute("a", aEl4, hrefAttr4));
        assertEquals("relative/path", hrefAttr4.getValue());

        Element aEl5 = new Element(Tag.valueOf("a"), "");
        aEl5.attr("href", "relative/path");
        Attribute hrefAttr5 = aEl5.attributes().asList().get(0);
        assertFalse(whitelist.isSafeAttribute("a", aEl5, hrefAttr5));

        Element aEl6 = new Element(Tag.valueOf("a"), "http://example.com/");
        aEl6.attr("href", "javascript:void(0)");
        Attribute hrefAttr6 = aEl6.attributes().asList().get(0);
        assertFalse(whitelist.isSafeAttribute("a", aEl6, hrefAttr6));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullTag() {
        new Whitelist().addProtocols(null, "href", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyTag() {
        new Whitelist().addProtocols("", "href", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullKey() {
        new Whitelist().addProtocols("a", null, "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyKey() {
        new Whitelist().addProtocols("a", "", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullProtocols() {
        new Whitelist().addProtocols("a", "href", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyProtocolString() {
        new Whitelist().addProtocols("a", "href", "");
    }

    @Test
    public void testTypedValueEqualityAndHashCode() {
        Whitelist.TagName tag1 = Whitelist.TagName.valueOf("div");
        Whitelist.TagName tag2 = Whitelist.TagName.valueOf("div");
        Whitelist.TagName tag3 = Whitelist.TagName.valueOf("span");
        Whitelist.AttributeKey key1 = Whitelist.AttributeKey.valueOf("href");
        Whitelist.AttributeKey key2 = Whitelist.AttributeKey.valueOf("href");
        Whitelist.AttributeValue val1 = Whitelist.AttributeValue.valueOf("nofollow");
        Whitelist.AttributeValue val2 = Whitelist.AttributeValue.valueOf("nofollow");
        Whitelist.Protocol prot1 = Whitelist.Protocol.valueOf("http");
        Whitelist.Protocol prot2 = Whitelist.Protocol.valueOf("http");

        assertEquals(tag1, tag1);
        assertEquals(tag1, tag2);
        assertNotEquals(tag1, tag3);
        assertNotEquals(tag1, null);
        assertNotEquals(tag1, "div");
        assertNotEquals(tag1, key1);

        assertEquals(tag1.hashCode(), tag2.hashCode());
        assertNotEquals(tag1.hashCode(), tag3.hashCode());
        assertEquals("div", tag1.toString());

        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertEquals("href", key1.toString());

        assertEquals(val1, val2);
        assertEquals(val1.hashCode(), val2.hashCode());
        assertEquals("nofollow", val1.toString());

        assertEquals(prot1, prot2);
        assertEquals(prot1.hashCode(), prot2.hashCode());
        assertEquals("http", prot1.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTypedValueNullValue() {
        Whitelist.TagName.valueOf(null);
    }
}
