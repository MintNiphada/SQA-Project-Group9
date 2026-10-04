package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class AttributeTest {

    @Test
    public void testConstructorsAndGetters() {
        Attribute attr = new Attribute("href", "http://example.com");
        Assert.assertEquals("href", attr.getKey());
        Assert.assertEquals("http://example.com", attr.getValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKeyThrows() {
        new Attribute(null, "val");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyKeyThrows() {
        new Attribute("", "val");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWhitespaceOnlyKeyThrows() {
        new Attribute("   ", "val");
    }

    @Test
    public void testKeyTrimmed() {
        Attribute attr = new Attribute("  class  ", "test");
        Assert.assertEquals("class", attr.getKey());
    }

    @Test
    public void testSetKey() {
        Attribute attr = new Attribute("class", "test");
        attr.setKey("  id  ");
        Assert.assertEquals("id", attr.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyNull() {
        Attribute attr = new Attribute("class", "test");
        attr.setKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyEmpty() {
        Attribute attr = new Attribute("class", "test");
        attr.setKey("   ");
    }

    @Test
    public void testSetKeyWithParent() {
        Attributes attributes = new Attributes();
        attributes.put("class", "active");
        Attribute attr = attributes.attribute("class");
        attr.setKey("id");
        Assert.assertEquals("id", attr.getKey());
        Assert.assertTrue(attributes.hasKey("id"));
        Assert.assertFalse(attributes.hasKey("class"));
        Assert.assertEquals("active", attributes.get("id"));
    }

    @Test
    public void testSetKeyWithParentKeyNotFound() {
        Attributes attributes = new Attributes();
        Attribute attr = new Attribute("missing", "val", attributes);
        attr.setKey("updated");
        Assert.assertEquals("updated", attr.getKey());
        Assert.assertFalse(attributes.hasKey("updated"));
    }

    @Test
    public void testSetValueWithParent() {
        Attributes attributes = new Attributes();
        attributes.put("key", "val1");
        Attribute attr = attributes.attribute("key");
        String oldVal = attr.setValue("val2");
        Assert.assertEquals("val1", oldVal);
        Assert.assertEquals("val2", attr.getValue());
        Assert.assertEquals("val2", attributes.get("key"));
    }

    @Test
    public void testHtmlOutput() {
        Attribute attr = new Attribute("key", "value");
        Assert.assertEquals("key=\"value\"", attr.html());
        Assert.assertEquals("key=\"value\"", attr.toString());
    }

    @Test
    public void testHtmlEntitiesEscaped() {
        Attribute attr = new Attribute("key", "a&b\"<>'");
        Assert.assertEquals("key=\"a&amp;b&quot;&lt;&gt;'\"", attr.html());
    }

    @Test
    public void testHtmlBooleanAttributeCollapse() {
        Attribute attr1 = new Attribute("required", "");
        Assert.assertEquals("required", attr1.html());

        Attribute attr2 = new Attribute("required", "required");
        Assert.assertEquals("required", attr2.html());

        Attribute attr3 = new Attribute("required", "REQUIRED");
        Assert.assertEquals("required", attr3.html());

        Attribute attr4 = new Attribute("required", "false");
        Assert.assertEquals("required=\"false\"", attr4.html());
    }

    @Test
    public void testHtmlNullValueCollapse() {
        Attribute attr = new Attribute("readonly", null);
        Assert.assertEquals("readonly", attr.html());

        Attribute attrNonBool = new Attribute("class", null);
        Assert.assertEquals("class", attrNonBool.html());
    }

    @Test
    public void testHtmlXmlSyntaxNoCollapse() throws IOException {
        Attribute attr = new Attribute("checked", "checked");
        Document.OutputSettings settings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder sb = new StringBuilder();
        attr.html(sb, settings);
        Assert.assertEquals("checked=\"checked\"", sb.toString());
    }

    @Test
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("href", "&amp;&lt;&gt;&quot;");
        Assert.assertEquals("&<>\"", attr.getValue());
        Assert.assertEquals("href", attr.getKey());
    }

    @Test
    public void testIsDataAttribute() {
        Attribute dataAttr = new Attribute("data-name", "John");
        Assert.assertTrue(dataAttr.isDataAttribute());
        Assert.assertTrue(Attribute.isDataAttribute("data-src"));

        Attribute nonDataAttr1 = new Attribute("data-", "val");
        Assert.assertFalse(nonDataAttr1.isDataAttribute());
        Assert.assertFalse(Attribute.isDataAttribute("data-"));

        Attribute nonDataAttr2 = new Attribute("class", "val");
        Assert.assertFalse(nonDataAttr2.isDataAttribute());
        Assert.assertFalse(Attribute.isDataAttribute("class"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testIsBooleanAttribute() {
        Attribute disabled = new Attribute("disabled", "disabled");
        Assert.assertTrue(disabled.isBooleanAttribute());
        Assert.assertTrue(Attribute.isBooleanAttribute("disabled"));

        Attribute customNull = new Attribute("custom", null);
        Assert.assertTrue(customNull.isBooleanAttribute());
        Assert.assertFalse(Attribute.isBooleanAttribute("custom"));

        Attribute customVal = new Attribute("custom", "val");
        Assert.assertFalse(customVal.isBooleanAttribute());
    }

    @Test
    public void testEqualsAndHashCode() {
        Attribute a1 = new Attribute("key", "val");
        Attribute a2 = new Attribute("key", "val");
        Attribute a3 = new Attribute("key", "other");
        Attribute a4 = new Attribute("other", "val");
        Attribute a5 = new Attribute("key", null);
        Attribute a6 = new Attribute("key", null);

        Assert.assertEquals(a1, a1);
        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());

        Assert.assertNotEquals(a1, a3);
        Assert.assertNotEquals(a1, a4);
        Assert.assertNotEquals(a1, null);
        Assert.assertNotEquals(a1, "string");
        Assert.assertNotEquals(a1, a5);
        Assert.assertEquals(a5, a6);
        Assert.assertEquals(a5.hashCode(), a6.hashCode());
    }

    @Test
    public void testClone() {
        Attribute original = new Attribute("key", "val");
        Attribute clone = original.clone();

        Assert.assertNotSame(original, clone);
        Assert.assertEquals(original, clone);
        Assert.assertEquals(original.getKey(), clone.getKey());
        Assert.assertEquals(original.getValue(), clone.getValue());
    }

    @Test
    public void testStaticHtml() throws IOException {
        StringBuilder sb = new StringBuilder();
        Document.OutputSettings out = new Document.OutputSettings();
        Attribute.html("id", "main", sb, out);
        Assert.assertEquals("id=\"main\"", sb.toString());
    }

    @Test
    public void testShouldCollapseAttributeInstance() {
        Attribute attr = new Attribute("async", "async");
        Document.OutputSettings out = new Document.OutputSettings();
        Assert.assertTrue(attr.shouldCollapseAttribute(out));
    }
}
