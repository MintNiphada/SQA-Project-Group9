package org.jsoup.parser;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class TagTest {

    @Test
    public void valueOfNullThrows() {
        try {
            Tag.valueOf(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfEmptyThrows() {
        Tag.valueOf("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void valueOfWhitespaceThrows() {
        Tag.valueOf("   ");
    }

    @Test
    public void valueOfKnownTagCaseInsensitiveAndTrimmed() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("P");
        Tag p3 = Tag.valueOf("  p  ");
        assertSame(p1, p2);
        assertSame(p1, p3);
        assertEquals("p", p1.getName());
    }

    @Test
    public void valueOfUnknownTagDefaults() {
        Tag unknown = Tag.valueOf("foo");
        assertFalse(unknown.isBlock());
        assertTrue(unknown.formatAsBlock());
        assertTrue(unknown.canContainBlock());
        assertTrue(unknown.canContainInline());
        assertFalse(unknown.isEmpty());
        assertFalse(unknown.isSelfClosing());
        assertFalse(unknown.preserveWhitespace());
        assertFalse(unknown.isKnownTag());
        assertEquals("foo", unknown.getName());
    }

    @Test
    public void valueOfUnknownTagLowercasesAndTrims() {
        Tag unknown = Tag.valueOf("  Foo ");
        assertEquals("foo", unknown.getName());
    }

    @Test
    public void knownBlockTagProperties() {
        Tag p = Tag.valueOf("p");
        assertTrue(p.isBlock());
        assertFalse(p.isInline());
        assertTrue(p.canContainBlock());
        assertTrue(p.canContainInline());
        assertFalse(p.isEmpty());
        assertFalse(p.isSelfClosing());
        assertFalse(p.formatAsBlock());
        assertTrue(p.isKnownTag());
    }

    @Test
    public void knownInlineTagProperties() {
        Tag a = Tag.valueOf("a");
        assertFalse(a.isBlock());
        assertTrue(a.isInline());
        assertFalse(a.canContainBlock());
        assertTrue(a.canContainInline());
        assertFalse(a.isEmpty());
        assertFalse(a.formatAsBlock());
    }

    @Test
    public void knownEmptyTagProperties() {
        Tag img = Tag.valueOf("img");
        assertFalse(img.isBlock());
        assertTrue(img.isInline());
        assertFalse(img.canContainBlock());
        assertFalse(img.canContainInline());
        assertTrue(img.isEmpty());
        assertTrue(img.isSelfClosing());
        assertFalse(img.formatAsBlock());
    }

    @Test
    public void knownPreserveWhitespaceTags() {
        assertTrue(Tag.valueOf("pre").preserveWhitespace());
        assertTrue(Tag.valueOf("plaintext").preserveWhitespace());
        assertTrue(Tag.valueOf("title").preserveWhitespace());
        assertFalse(Tag.valueOf("div").preserveWhitespace());
    }

    @Test
    public void formatAsBlockForDivAndTitle() {
        assertTrue(Tag.valueOf("div").formatAsBlock());
        assertFalse(Tag.valueOf("title").formatAsBlock());
    }

    @Test
    public void isDataForDefaultAndEmpty() {
        assertFalse(Tag.valueOf("foo").isData());
        assertFalse(Tag.valueOf("p").isData());
        assertFalse(Tag.valueOf("img").isData());
    }

    @Test
    public void isDataTrueWhenCanContainInlineFalseAndNotEmpty() throws Exception {
        Tag tag = Tag.valueOf("foo");
        setField(tag, "canContainInline", false);
        setField(tag, "empty", false);
        assertTrue(tag.isData());
    }

    @Test
    public void setSelfClosingOnUnknown() {
        Tag tag = Tag.valueOf("bar");
        assertFalse(tag.isSelfClosing());
        tag.setSelfClosing();
        assertTrue(tag.isSelfClosing());
    }

    @Test
    public void knownTagStaticCheck() {
        assertTrue(Tag.isKnownTag("p"));
        assertTrue(Tag.isKnownTag("img"));
        assertFalse(Tag.isKnownTag("foo"));
        assertFalse(Tag.isKnownTag(null));
        assertFalse(Tag.isKnownTag("P"));
    }

    @Test
    public void equalsSameObject() {
        Tag tag = Tag.valueOf("foo");
        assertTrue(tag.equals(tag));
    }

    @Test
    public void equalsNotInstance() {
        Tag tag = Tag.valueOf("foo");
        assertFalse(tag.equals("foo"));
    }

    @Test
    public void equalsDifferentTagName() {
        Tag foo = Tag.valueOf("foo");
        Tag bar = Tag.valueOf("bar");
        assertFalse(foo.equals(bar));
    }

    @Test
    public void equalsSameStateDifferentInstances() {
        Tag foo1 = Tag.valueOf("foo");
        Tag foo2 = Tag.valueOf("foo");
        assertTrue(foo1.equals(foo2));
        assertEquals(foo1.hashCode(), foo2.hashCode());
    }

    @Test
    public void equalsFalseWhenCanContainBlockDiffers() throws Exception {
        Tag left = Tag.valueOf("same");
        Tag right = Tag.valueOf("same");
        setField(left, "canContainBlock", false);
        assertFalse(left.equals(right));
    }

    @Test
    public void equalsFalseWhenCanContainInlineDiffers() throws Exception {
        Tag left = Tag.valueOf("same");
        Tag right = Tag.valueOf("same");
        setField(left, "canContainInline", false);
        assertFalse(left.equals(right));
    }

    @Test
    public void equalsFalseWhenEmptyDiffers() throws Exception {
        Tag left = Tag.valueOf("same");
        Tag right = Tag.valueOf("same");
        setField(left, "empty", true);
        assertFalse(left.equals(right));
    }

    @Test
    public void equalsFalseWhenFormatAsBlockDiffers() throws Exception {
        Tag left = Tag.valueOf("same");
        Tag right = Tag.valueOf("same");
        setField(left, "formatAsBlock", false);
        assertFalse(left.equals(right));
    }

    @Test
    public void equalsFalseWhenIsBlockDiffers() throws Exception {
        Tag left = Tag.valueOf("same");
        Tag right = Tag.valueOf("same");
        setField(left, "isBlock", true);
        assertFalse(left.equals(right));
    }

    @Test
    public void equalsFalseWhenPreserveWhitespaceDiffers() throws Exception {
        Tag left = Tag.valueOf("same");
        Tag right = Tag.valueOf("same");
        setField(left, "preserveWhitespace", true);
        assertFalse(left.equals(right));
    }

    @Test
    public void equalsFalseWhenSelfClosingDiffers() throws Exception {
        Tag left = Tag.valueOf("same");
        Tag right = Tag.valueOf("same");
        setField(left, "selfClosing", true);
        assertFalse(left.equals(right));
    }

    @Test
    public void toStringReturnsName() {
        Tag tag = Tag.valueOf("Foo");
        assertEquals("foo", tag.toString());
    }

    private void setField(Tag tag, String name, boolean value) throws Exception {
        Field field = Tag.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setBoolean(tag, value);
    }
}
