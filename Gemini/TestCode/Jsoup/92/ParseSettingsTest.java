package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.junit.Assert;
import org.junit.Test;

public class ParseSettingsTest {

    @Test
    public void testHtmlDefaultConstants() {
        ParseSettings settings = ParseSettings.htmlDefault;
        Assert.assertFalse(settings.preserveTagCase());
        Assert.assertEquals("div", settings.normalizeTag("  DIV  "));
        Assert.assertEquals("href", settings.normalizeAttribute("  HREF  "));
    }

    @Test
    public void testPreserveCaseConstants() {
        ParseSettings settings = ParseSettings.preserveCase;
        Assert.assertTrue(settings.preserveTagCase());
        Assert.assertEquals("DIV", settings.normalizeTag("  DIV  "));
        Assert.assertEquals("HREF", settings.normalizeAttribute("  HREF  "));
    }

    @Test
    public void testConstructorCustomSettings() {
        ParseSettings settings = new ParseSettings(true, false);
        Assert.assertTrue(settings.preserveTagCase());
        Assert.assertEquals("MyTag", settings.normalizeTag("  MyTag  "));
        Assert.assertEquals("myattr", settings.normalizeAttribute("  MyAttr  "));

        ParseSettings inverted = new ParseSettings(false, true);
        Assert.assertFalse(inverted.preserveTagCase());
        Assert.assertEquals("mytag", inverted.normalizeTag("  MyTag  "));
        Assert.assertEquals("MyAttr", inverted.normalizeAttribute("  MyAttr  "));
    }

    @Test
    public void testNormalizeAttributesWhenPreserveFalse() {
        ParseSettings settings = new ParseSettings(true, false);
        Attributes attributes = new Attributes();
        attributes.put("KeyOne", "ValOne");
        attributes.put("KEYTWO", "ValTwo");

        Attributes result = settings.normalizeAttributes(attributes);
        Assert.assertSame(attributes, result);
        Assert.assertTrue(result.hasKey("keyone"));
        Assert.assertTrue(result.hasKey("keytwo"));
        Assert.assertFalse(result.hasKeyIgnoreCase("KeyOne") && result.hasKey("KeyOne"));
    }

    @Test
    public void testNormalizeAttributesWhenPreserveTrue() {
        ParseSettings settings = new ParseSettings(false, true);
        Attributes attributes = new Attributes();
        attributes.put("KeyOne", "ValOne");
        attributes.put("KEYTWO", "ValTwo");

        Attributes result = settings.normalizeAttributes(attributes);
        Assert.assertSame(attributes, result);
        Assert.assertTrue(result.hasKey("KeyOne"));
        Assert.assertTrue(result.hasKey("KEYTWO"));
    }

    @Test
    public void testNormalizeTagEmptyAndWhitespace() {
        ParseSettings preserve = ParseSettings.preserveCase;
        Assert.assertEquals("", preserve.normalizeTag(""));
        Assert.assertEquals("", preserve.normalizeTag("   "));

        ParseSettings lower = ParseSettings.htmlDefault;
        Assert.assertEquals("", lower.normalizeTag(""));
        Assert.assertEquals("", lower.normalizeTag("   "));
    }

    @Test
    public void testNormalizeAttributeEmptyAndWhitespace() {
        ParseSettings preserve = ParseSettings.preserveCase;
        Assert.assertEquals("", preserve.normalizeAttribute(""));
        Assert.assertEquals("", preserve.normalizeAttribute("   "));

        ParseSettings lower = ParseSettings.htmlDefault;
        Assert.assertEquals("", lower.normalizeAttribute(""));
        Assert.assertEquals("", lower.normalizeAttribute("   "));
    }

    @Test(expected = NullPointerException.class)
    public void testNormalizeTagNullThrowsException() {
        ParseSettings.htmlDefault.normalizeTag(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNormalizeAttributeNullThrowsException() {
        ParseSettings.htmlDefault.normalizeAttribute(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNormalizeAttributesNullThrowsException() {
        ParseSettings.htmlDefault.normalizeAttributes(null);
    }
}
