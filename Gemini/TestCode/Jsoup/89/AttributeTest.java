package org.jsoup.nodes;

import org.jsoup.SerializationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class AttributeTest {

    @Test
    public void testConstructorsAndGetters() {
        Attribute attr = new Attribute("href", "http://example.com");
        Assert.assertEquals("href", attr.getKey());
        Assert.assertEquals("http://example.com", attr.getValue());

        Attribute attrWithSpaces = new Attribute("  class  ", "btn");
        Assert.assertEquals("class", attrWithSpaces.getKey());
        Assert.assertEquals("btn", attrWithSpaces.getValue());
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
    public void testWhitespaceKeyThrows() {
        new Attribute("   ", "val");
    }

    @Test
    public void testSetKey() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey("newKey");
        Assert.assertEquals("newKey", attr.getKey());

        attr.setKey("  trimmedKey  ");
        Assert.assertEquals("trimmedKey", attr.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyNull() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyEmpty() {
        Attribute attr = new Attribute("key", "val");
        attr.setKey("  ");
    }

    @Test
    public void testSetKeyWithParent() {
        Attributes attributes = new Attributes();
        attributes.put("oldKey", "val");
        Attribute attr = attributes.attribute("oldKey");
        attr.setKey("newKey");

        Assert.assertEquals("newKey", attr.getKey());
        Assert.assertTrue(attributes.hasKey("newKey"));
        Assert.assertFalse(attributes.hasKey("oldKey"));
    }

    @Test
    public void testSetKeyWithParentKeyNotFound() {
        Attributes attributes = new Attributes();
        Attribute attr = new Attribute("key", "val", attributes);
        attr.setKey("newKey");
        Assert.assertEquals("newKey", attr.getKey());
    }

    @Test
    public void testSetValueWithParent() {
        Attributes attributes = new Attributes();
        attributes.put("key", "oldVal");
        Attribute attr = attributes.attribute("key");
        String oldVal = attr.setValue("newVal");

        Assert.assertEquals("oldVal", oldVal);
        Assert.assertEquals("newVal", attr.getValue());
        Assert.assertEquals("newVal", attributes.get("key"));
    }

    @Test
    public void testGetValueNullDefaultsToEmpty() {
        Attribute attr = new Attribute("key", null);
        Assert.assertEquals("", attr.getValue());
    }

    @Test
    public void testHtml() {
        Attribute attr = new Attribute("href", "http://example.com/test?a=1&b=2");
        Assert.assertEquals("href=\"http://example.com/test?a=1&amp;b=2\"", attr.html());
        Assert.assertEquals("href=\"http://example.com/test?a=1&amp;b=2\"", attr.toString());
    }

    @Test
    public void testHtmlBooleanAttribute() {
        Attribute attr = new Attribute("disabled", "");
        Assert.assertEquals("disabled", attr.html());

        Attribute attrSameVal = new Attribute("disabled", "disabled");
        Assert.assertEquals("disabled", attrSameVal.html());

        Attribute attrNullVal = new Attribute("disabled", null);
        Assert.assertEquals("disabled", attrNullVal.html());

        Attribute attrXml = new Attribute("disabled", "disabled");
        Document.OutputSettings xmlSettings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);
        StringBuilder sb = new StringBuilder();
        try {
            attrXml.html(sb, xmlSettings);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("disabled=\"disabled\"", sb.toString());
    }

    @Test
    public void testHtmlCustomAppendableIOException() {
        Appendable throwingAppendable = new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException {
                throw new IOException();
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                throw new IOException();
            }

            @Override
            public Appendable append(char c) throws IOException {
                throw new IOException();
            }
        };

        Attribute attr = new Attribute("key", "val");
        try {
            attr.html(throwingAppendable, new Document("").outputSettings());
            Assert.fail();
        } catch (IOException expected) {
            Assert.assertNotNull(expected);
        }
    }

    @Test
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("href", "http://example.com/test?a=1&amp;b=2");
        Assert.assertEquals("href", attr.getKey());
        Assert.assertEquals("http://example.com/test?a=1&b=2", attr.getValue());
    }

    @Test
    public void testIsDataAttribute() {
        Attribute dataAttr = new Attribute("data-name", "value");
        Assert.assertTrue(dataAttr.isDataAttribute());
        Assert.assertTrue(Attribute.isDataAttribute("data-custom"));

        Attribute prefixOnly = new Attribute("data-", "value");
        Assert.assertFalse(prefixOnly.isDataAttribute());
        Assert.assertFalse(Attribute.isDataAttribute("data-"));

        Attribute nonDataAttr = new Attribute("name", "value");
        Assert.assertFalse(nonDataAttr.isDataAttribute());
        Assert.assertFalse(Attribute.isDataAttribute("name"));
    }

    @Test
    public void testShouldCollapseAttribute() {
        Document.OutputSettings htmlSettings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.html);
        Document.OutputSettings xmlSettings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);

        Attribute attr = new Attribute("required", "");
        Assert.assertTrue(attr.shouldCollapseAttribute(htmlSettings));
        Assert.assertFalse(attr.shouldCollapseAttribute(xmlSettings));

        Attribute nonBool = new Attribute("class", "");
        Assert.assertFalse(nonBool.shouldCollapseAttribute(htmlSettings));
    }

    @Test
    public void testIsBooleanAttribute() {
        Assert.assertTrue(Attribute.isBooleanAttribute("checked"));
        Assert.assertTrue(Attribute.isBooleanAttribute("disabled"));
        Assert.assertFalse(Attribute.isBooleanAttribute("href"));

        Attribute boolAttr = new Attribute("checked", "checked");
        Assert.assertTrue(boolAttr.isBooleanAttribute());

        Attribute nullValAttr = new Attribute("nonbool", null);
        Assert.assertTrue(nullValAttr.isBooleanAttribute());

        Attribute normalAttr = new Attribute("class", "main");
        Assert.assertFalse(normalAttr.isBooleanAttribute());
    }

    @Test
    public void testEqualsAndHashCode() {
        Attribute a1 = new Attribute("key", "value");
        Attribute a2 = new Attribute("key", "value");
        Attribute a3 = new Attribute("key2", "value");
        Attribute a4 = new Attribute("key", "value2");
        Attribute a5 = new Attribute("key", null);
        Attribute a6 = new Attribute("key", null);

        Assert.assertTrue(a1.equals(a1));
        Assert.assertTrue(a1.equals(a2));
        Assert.assertFalse(a1.equals(null));
        Assert.assertFalse(a1.equals("key=value"));
        Assert.assertFalse(a1.equals(a3));
        Assert.assertFalse(a1.equals(a4));
        Assert.assertFalse(a1.equals(a5));
        Assert.assertTrue(a5.equals(a6));

        Assert.assertEquals(a1.hashCode(), a2.hashCode());
        Assert.assertNotEquals(a1.hashCode(), a3.hashCode());
        Assert.assertEquals(a5.hashCode(), a6.hashCode());
    }

    @Test
    public void testClone() {
        Attribute attr = new Attribute("key", "val");
        Attribute clone = attr.clone();

        Assert.assertNotSame(attr, clone);
        Assert.assertEquals(attr, clone);
        Assert.assertEquals(attr.getKey(), clone.getKey());
        Assert.assertEquals(attr.getValue(), clone.getValue());
    }
}
