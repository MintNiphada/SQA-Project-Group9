package org.jsoup.safety;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WhitelistTest {

    @Mock
    private Element element;

    @Mock
    private Attribute attribute;

    @Before
    public void setUp() {
        // Common setup if needed
    }

    // Test static factory methods

    @Test
    public void testNone() {
        Whitelist whitelist = Whitelist.none();
        assertNotNull(whitelist);
        // Should be empty, no tags, attributes, etc.
        assertFalse(whitelist.isSafeTag("a"));
        assertFalse(whitelist.isSafeTag("div"));
    }

    @Test
    public void testSimpleText() {
        Whitelist whitelist = Whitelist.simpleText();
        assertTrue(whitelist.isSafeTag("b"));
        assertTrue(whitelist.isSafeTag("i"));
        assertFalse(whitelist.isSafeTag("a"));
        // No attributes allowed; simulate isSafeAttribute should return false
        when(element.absUrl(anyString())).thenReturn("");
        when(attribute.getKey()).thenReturn("class");
        assertFalse(whitelist.isSafeAttribute("b", element, attribute));
    }

    @Test
    public void testBasic() {
        Whitelist whitelist = Whitelist.basic();
        // Check some tags
        assertTrue(whitelist.isSafeTag("a"));
        assertTrue(whitelist.isSafeTag("p"));
        assertFalse(whitelist.isSafeTag("img"));

        // Check attribute allowed
        when(element.absUrl(anyString())).thenReturn("http://example.com");
        when(attribute.getKey()).thenReturn("href");
        when(attribute.getValue()).thenReturn("http://example.com");
        assertTrue(whitelist.isSafeAttribute("a", element, attribute));

        // Check enforced attribute
        Attributes enforced = whitelist.getEnforcedAttributes("a");
        assertTrue(enforced.hasKey("rel"));
        assertEquals("nofollow", enforced.get("rel"));
    }

    @Test
    public void testBasicWithImages() {
        Whitelist whitelist = Whitelist.basicWithImages();
        assertTrue(whitelist.isSafeTag("img"));
        assertTrue(whitelist.isSafeTag("a"));
        // Check img attributes
        when(element.absUrl(anyString())).thenReturn("http://example.com/image.jpg");
        when(attribute.getKey()).thenReturn("src");
        assertTrue(whitelist.isSafeAttribute("img", element, attribute));
    }

    @Test
    public void testRelaxed() {
        Whitelist whitelist = Whitelist.relaxed();
        assertTrue(whitelist.isSafeTag("table"));
        assertTrue(whitelist.isSafeTag("th"));
        assertTrue(whitelist.isSafeTag("a"));
        // No enforced rel for a
        Attributes enforced = whitelist.getEnforcedAttributes("a");
        assertEquals(0, enforced.size());
    }

    // Test addTags

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagsNullArray() {
        Whitelist.none().addTags((String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagsEmptyString() {
        Whitelist.none().addTags("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTagsNullElementInArray() {
        Whitelist.none().addTags("a", null, "b");
    }

    @Test
    public void testAddTagsValid() {
        Whitelist whitelist = Whitelist.none().addTags("p", "div");
        assertTrue(whitelist.isSafeTag("p"));
        assertTrue(whitelist.isSafeTag("div"));
        assertFalse(whitelist.isSafeTag("span"));
        // Adding duplicate should not cause issues
        whitelist.addTags("p");
        assertTrue(whitelist.isSafeTag("p"));
    }

    // Test addAttributes

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesNullTag() {
        Whitelist.none().addAttributes(null, "class");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesEmptyTag() {
        Whitelist.none().addAttributes("", "class");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesNullKeys() {
        Whitelist.none().addAttributes("a", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAttributesEmptyKey() {
        Whitelist.none().addAttributes("a", "href", "", "class");
    }

    @Test
    public void testAddAttributesValid() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href", "class");
        when(element.absUrl(anyString())).thenReturn("http://example.com");
        when(attribute.getKey()).thenReturn("href");
        assertTrue(whitelist.isSafeAttribute("a", element, attribute));
        when(attribute.getKey()).thenReturn("title");
        assertFalse(whitelist.isSafeAttribute("a", element, attribute));
    }

    @Test
    public void testAddAttributesAllTag() {
        Whitelist whitelist = Whitelist.none().addTags("div").addAttributes(":all", "class");
        // even though div has no explicit attributes, :all applies
        when(element.absUrl(anyString()).thenReturn("");
        when(attribute.getKey()).thenReturn("class");
        assertTrue(whitelist.isSafeAttribute("div", element, attribute));
        // for a tag not in tagNames, it still should work? isSafeTag first checks tagNames, so if tag not allowed, isSafeAttribute wouldn't be called? But we test isSafeAttribute directly with any tag
        assertTrue(whitelist.isSafeAttribute("span", element, attribute)); // :all applies
    }

    // Test addEnforcedAttribute

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullTag() {
        Whitelist.none().addEnforcedAttribute(null, "rel", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeEmptyTag() {
        Whitelist.none().addEnforcedAttribute("", "rel", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullKey() {
        Whitelist.none().addEnforcedAttribute("a", null, "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeEmptyKey() {
        Whitelist.none().addEnforcedAttribute("a", "", "nofollow");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeNullValue() {
        Whitelist.none().addEnforcedAttribute("a", "rel", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEnforcedAttributeEmptyValue() {
        Whitelist.none().addEnforcedAttribute("a", "rel", "");
    }

    @Test
    public void testAddEnforcedAttribute() {
        Whitelist whitelist = Whitelist.none().addEnforcedAttribute("a", "rel", "nofollow");
        Attributes enforced = whitelist.getEnforcedAttributes("a");
        assertEquals("nofollow", enforced.get("rel"));
        // Override value
        whitelist.addEnforcedAttribute("a", "rel", "ugc");
        enforced = whitelist.getEnforcedAttributes("a");
        assertEquals("ugc", enforced.get("rel));
    }

    // Test preserveRelativeLinks

    @Test
    public void testPreserveRelativeLinksFalse() {
        Whitelist whitelist = Whitelist.basic();
        whitelist.preserveRelativeLinks(false);
        // This flag affects testValidProtocol, we need to check attribute value after isSafeAttribute
        when(element.absUrl(anyString()).thenReturn("http://abs.com");
        Attribute attr = spy(new Attribute("href", "relative"));
        when(element.absUrl(attr.getKey())).thenReturn("http://abs.com");
        assertTrue(whitelist.isSafeAttribute("a", element, attr));
        // verify attr.setValue was called with absolute url because preserveRelativeLinks=false
        assertEquals("http://abs.com", attr.getValue());
    }

    @Test
    public void testPreserveRelativeLinksTrue() {
        Whitelist whitelist = Whitelist.basic();
        whitelist.preserveRelativeLinks(true);
        when(element.absUrl(anyString()).thenReturn("http://abs.com");
        Attribute attr = spy(new Attribute("href", "relative"));
        when(element.absUrl(attr.getKey())).thenReturn("http://abs.com");
        assertTrue(whitelist.isSafeAttribute("a", element, attr));
        // preserveRelativeLinks=true, so value should not be changed
        assertEquals("relative", attr.getValue());
    }

    // Test addProtocols

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullTag() {
        Whitelist.none().addProtocols(null, "href", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyTag() {
        Whitelist.none().addProtocols("", "href", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullKey() {
        Whitelist.none().addProtocols("a", null, "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyKey() {
        Whitelist.none().addProtocols("a", "", "http");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsNullProtocolsArray() {
        Whitelist.none().addProtocols("a", "href", (String[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddProtocolsEmptyProtocol() {
        Whitelist.basic().addProtocols("a", "href", "http", "");
    }

    @Test
    public void testAddProtocolsValid() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href")
                .addProtocols("a", "href", "ftp");
        when(element.absUrl(anyString())).thenReturn("ftp://example.com");
        when(attribute.getKey()).thenReturn("href");
        assertTrue(whitelist.isSafeAttribute("a", element, attribute));
        when(element.absUrl(anyString())).thenReturn("http://example.com");
        assertFalse(whitelist.isSafeAttribute("a", element, attribute));
    }

    // Test isSafeTag

    @Test
    public void testIsSafeTagCaseSensitivity() {
        Whitelist whitelist = Whitelist.simpleText();
        // tag names are stored and matched as-is; simpleText adds lowercase, so uppercase should not match
        assertFalse(whitelist.isSafeTag("B"));
        assertTrue(whitelist.isSafeTag("b"));
    }

    // Test isSafeAttribute branching

    @Test
    public void testIsSafeAttributeTagWithAttrsAndNoProtocols() {
        Whitelist whitelist = Whitelist.none().addTags("span").addAttributes("span", "style");
        when(element.absUrl(anyString())).thenReturn("");  // not used because no protocols
        when(attribute.getKey()).thenReturn("style");
        assertTrue(whitelist.isSafeAttribute("span", element, attribute));
    }

    @Test
    public void testIsSafeAttributeTagWithAttrsButKeyNotPresent() {
        Whitelist whitelist = Whitelist.none().addTags("span").addAttributes("span", "style");
        when(attribute.getKey()).thenReturn("class");
        assertFalse(whitelist.isSafeAttribute("span", element, attribute));
    }

    @Test
    public void testIsSafeAttributeTagWithProtocolsForOtherKey() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href", "class")
                .addProtocols("a", "href", "http");
        // test class attribute, no protocol defined, should return true
        when(element.absUrl(anyString())).thenReturn(""); // not used
        when(attribute.getKey()).thenReturn("class");
        assertTrue(whitelist.isSafeAttribute("a", element, attribute));
    }

    @Test
    public void testIsSafeAttributeTagWithProtocolsMatching() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href")
                .addProtocols("a", "href", "http");
        when(element.absUrl(anyString())).thenReturn("http://example.com");
        when(attribute.getKey()).thenReturn("href");
        assertTrue(whitelist.isSafeAttribute("a", element, attribute));
    }

    @Test
    public void testIsSafeAttributeTagWithProtocolsNotMatching() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href")
                .addProtocols("a", "href", "ftp");
        when(element.absUrl(anyString())).thenReturn("http://example.com");
        when(attribute.getKey()).thenReturn("href");
        assertFalse(whitelist.isSafeAttribute("a", element, attribute));
    }

    @Test
    public void testIsSafeAttributeFallbackToAllTag() {
        Whitelist whitelist = Whitelist.none().addTags("div").addAttributes(":all", "class");
        when(element.absUrl(anyString())).thenReturn("");
        when(attribute.getKey()).thenReturn("class");
        assertTrue(whitelist.isSafeAttribute("div", element, attribute));
        // also check that :all is not treated as a tag itself (to avoid recursion)
        assertTrue(whitelist.isSafeAttribute(":all", element, attribute)); // :all has attributes defined, so it should work
    }

    @Test
    public void testIsSafeAttributeFailWhenNoAllApply() {
        Whitelist whitelist = Whitelist.none().addTags("span");
        when(attribute.getKey()).thenReturn("title");
        // no attributes for span, and no :all defined, so false
        assertFalse(whitelist.isSafeAttribute("span", element, attribute));
    }

    // Test getEnforcedAttributes

    @Test
    public void testGetEnforcedAttributesNoTag() {
        Whitelist whitelist = Whitelist.none();
        Attributes attrs = whitelist.getEnforcedAttributes("a");
        assertEquals(0, attrs.size());
    }

    @Test
    public void testGetEnforcedAttributesMultiple() {
        Whitelist whitelist = Whitelist.none()
                .addEnforcedAttribute("a", "rel", "nofollow")
                .addEnforcedAttribute("a", "target", "_blank");
        Attributes attrs = whitelist.getEnforcedAttributes("a");
        assertEquals(2, attrs.size());
        assertEquals("nofollow", attrs.get("rel"));
        assertEquals("_blank", attrs.get("target"));
    }

    // Test TypedValue subclasses equality / hashCode / toString

    @Test
    public void testTagNameEquality() {
        Whitelist.TagName t1 = Whitelist.TagName.valueOf("div");
        Whitelist.TagName t2 = Whitelist.TagName.valueOf("div");
        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
        assertEquals("div", t1.toString());
    }

    @Test
    public void testAttributeKeyEquality() {
        Whitelist.AttributeKey key1 = Whitelist.AttributeKey.valueOf("href");
        Whitelist.AttributeKey key2 = Whitelist.AttributeKey.valueOf("href");
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertEquals("href", key1.toString());
    }

    @Test
    public void testAttributeValueEquality() {
        Whitelist.AttributeValue val1 = Whitelist.AttributeValue.valueOf("nofollow");
        Whitelist.AttributeValue val2 = Whitelist.AttributeValue.valueOf("nofollow");
        assertEquals(val1, val2);
        assertEquals(val1.hashCode(), val2.hashCode());
        assertEquals("nofollow", val1.toString());
    }

    @Test
    public void testProtocolEquality() {
        Whitelist.Protocol p1 = Whitelist.Protocol.valueOf("http");
        Whitelist.Protocol p2 = Whitelist.Protocol.valueOf("http");
        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertEquals("http", p1.toString());
    }

    @Test
    public void testTypedValueEqualsNull() {
        Whitelist.TagName tag = Whitelist.TagName.valueOf("a");
        assertFalse(tag.equals(null));
    }

    @Test
    public void testTypedValueEqualsDifferentClass() {
        Whitelist.TagName tag = Whitelist.TagName.valueOf("a");
        assertFalse(tag.equals("a"));
    }

    // Coverage for testValidProtocol case insensitivity and multiple protocols

    @Test
    public void testTestValidProtocolCaseInsensitive() {
        Whitelist whitelist = Whitelist.none().addTags("a").addAttributes("a", "href")
                .addProtocols("a", "href", "HTTP");
        when(element.absUrl(anyString())).thenReturn("HTTP://EXAMPLE.COM");
        when(attribute.getKey()).thenReturn("href");
        assertTrue(whitelist.isSafeAttribute("a", element, attribute));
    }

    // Enforce that chaining works
    @Test
    public void testChainingAllMethods() {
        Whitelist whitelist = Whitelist.none()
                .addTags("p")
                .addAttributes("p", "align")
                .addEnforcedAttribute("p", "align", "center")
                .addProtocols("a", "href", "http")
                .preserveRelativeLinks(true);
        // just test that it doesn't throw and some state is correct
        assertTrue(whitelist.isSafeTag("p"));
        Attributes enforced = whitelist.getEnforcedAttributes("p");
        assertEquals("center", enforced.get("align"));
    }
}
