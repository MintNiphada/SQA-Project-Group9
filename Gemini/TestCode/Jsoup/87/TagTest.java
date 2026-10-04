package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

public class TagTest {

    @Test
    public void testKnownTagsProperties() {
        Tag p = Tag.valueOf("p");
        Assert.assertEquals("p", p.getName());
        Assert.assertEquals("p", p.toString());
        Assert.assertTrue(p.isBlock());
        Assert.assertTrue(p.canContainBlock());
        Assert.assertFalse(p.isInline());
        Assert.assertFalse(p.formatAsBlock());
        Assert.assertFalse(p.isEmpty());
        Assert.assertFalse(p.isSelfClosing());
        Assert.assertFalse(p.isData());
        Assert.assertTrue(p.isKnownTag());
        Assert.assertTrue(Tag.isKnownTag("p"));
        Assert.assertFalse(p.preserveWhitespace());
        Assert.assertFalse(p.isFormListed());
        Assert.assertFalse(p.isFormSubmittable());

        Tag div = Tag.valueOf("div");
        Assert.assertTrue(div.isBlock());
        Assert.assertTrue(div.formatAsBlock());

        Tag a = Tag.valueOf("a");
        Assert.assertFalse(a.isBlock());
        Assert.assertTrue(a.isInline());
        Assert.assertFalse(a.formatAsBlock());

        Tag img = Tag.valueOf("img");
        Assert.assertTrue(img.isEmpty());
        Assert.assertTrue(img.isSelfClosing());
        Assert.assertFalse(img.isData());

        Tag pre = Tag.valueOf("pre");
        Assert.assertTrue(pre.preserveWhitespace());

        Tag textarea = Tag.valueOf("textarea");
        Assert.assertTrue(textarea.isFormListed());
        Assert.assertTrue(textarea.isFormSubmittable());
        Assert.assertTrue(textarea.preserveWhitespace());

        Tag fieldset = Tag.valueOf("fieldset");
        Assert.assertTrue(fieldset.isFormListed());
        Assert.assertFalse(fieldset.isFormSubmittable());
    }

    @Test
    public void testUnknownTag() {
        Tag custom = Tag.valueOf("custom-tag");
        Assert.assertEquals("custom-tag", custom.getName());
        Assert.assertFalse(custom.isBlock());
        Assert.assertTrue(custom.isInline());
        Assert.assertTrue(custom.formatAsBlock());
        Assert.assertFalse(custom.isEmpty());
        Assert.assertFalse(custom.isSelfClosing());
        Assert.assertFalse(custom.isData());
        Assert.assertFalse(custom.isKnownTag());
        Assert.assertFalse(Tag.isKnownTag("custom-tag"));

        Tag customSelfClosing = Tag.valueOf("custom-self").setSelfClosing();
        Assert.assertTrue(customSelfClosing.isSelfClosing());
    }

    @Test
    public void testValueOfSettings() {
        Tag tagUpper = Tag.valueOf("DIV", ParseSettings.preserveCase);
        Assert.assertEquals("DIV", tagUpper.getName());
        Assert.assertFalse(tagUpper.isKnownTag());

        Tag tagLower = Tag.valueOf("DIV", ParseSettings.htmlDefault);
        Assert.assertEquals("div", tagLower.getName());
        Assert.assertTrue(tagLower.isKnownTag());
        Assert.assertSame(Tag.valueOf("div"), tagLower);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfNullThrowsException() {
        Tag.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfEmptyThrowsException() {
        Tag.valueOf("   ", ParseSettings.htmlDefault);
    }

    @Test
    public void testEqualsAndHashCode() {
        Tag p1 = Tag.valueOf("p");
        Tag p2 = Tag.valueOf("p");
        Tag div = Tag.valueOf("div");
        Tag custom1 = Tag.valueOf("custom");
        Tag custom2 = Tag.valueOf("custom");
        Tag customUpper = Tag.valueOf("CUSTOM");
        Tag customSelf1 = Tag.valueOf("custom").setSelfClosing();
        Tag customSelf2 = Tag.valueOf("custom").setSelfClosing();

        Assert.assertTrue(p1.equals(p1));
        Assert.assertTrue(p1.equals(p2));
        Assert.assertEquals(p1.hashCode(), p2.hashCode());

        Assert.assertFalse(p1.equals(null));
        Assert.assertFalse(p1.equals("p"));
        Assert.assertFalse(p1.equals(div));

        Assert.assertTrue(custom1.equals(custom2));
        Assert.assertEquals(custom1.hashCode(), custom2.hashCode());
        Assert.assertFalse(custom1.equals(customUpper));
        Assert.assertFalse(custom1.equals(customSelf1));
        Assert.assertTrue(customSelf1.equals(customSelf2));
        Assert.assertEquals(customSelf1.hashCode(), customSelf2.hashCode());

        Tag img = Tag.valueOf("img");
        Tag input = Tag.valueOf("input");
        Assert.assertFalse(img.equals(input));

        Tag pre = Tag.valueOf("pre");
        Tag title = Tag.valueOf("title");
        Assert.assertFalse(pre.equals(title));

        Tag button = Tag.valueOf("button");
        Assert.assertFalse(button.equals(input));

        Tag fieldset = Tag.valueOf("fieldset");
        Assert.assertFalse(fieldset.equals(button));

        Tag script = Tag.valueOf("script");
        Tag style = Tag.valueOf("style");
        Assert.assertFalse(script.equals(style));
    }

    @Test
    public void testIsKnownTagStatic() {
        Assert.assertTrue(Tag.isKnownTag("a"));
        Assert.assertTrue(Tag.isKnownTag("div"));
        Assert.assertFalse(Tag.isKnownTag("notarealtag"));
        Assert.assertFalse(Tag.isKnownTag("DIV"));
    }
}
