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

        Attributes parent = new Attributes();
        Attribute attrWithParent = new Attribute("title", "tooltip", parent);
        Assert.assertEquals("title", attrWithParent.getKey());
        Assert.assertEquals("tooltip", attrWithParent.getValue());
        Assert.assertSame(parent, attrWithParent.parent);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKey() {
        new Attribute(null, "val");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyKey() {
        new Attribute("", "val");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWhitespaceKey() {
        new Attribute("   ", "val");
    }

    @Test
    public void testConstructorKeyTrimming() {
        Attribute attr = new Attribute("  data-name  ", "value");
        Assert.assertEquals("data-name", attr.getKey());
    }

    @Test
    public void testSetKey() {
        Attribute attr = new Attribute("oldKey", "val");
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
        attr.setKey("   ");
    }

    @Test
    public void testSetKeyWithParent() {
        Attributes parent = new Attributes();
        parent.put("k1", "v1");
        parent.put("k2", "v2");

        Attribute attr1 = new Attribute("k1", "v1", parent);
        attr1.setKey("k1_new");
        Assert.assertEquals("k1_new", attr1.getKey());
        Assert.assertTrue(parent.hasKey("k1_new"));
        Assert.assertFalse(parent.hasKey("k1"));

        Attribute attrNotFound = new Attribute("k3", "v3", parent);
        attrNotFound.setKey("k3_new");
        Assert.assertEquals("k3_new", attrNotFound.getKey());
        Assert.assertFalse(parent.hasKey("k3_new"));
    }

    @Test
    public void testSetValue() {
        Attribute attr = new Attribute("key", "val");
        String old = attr.setValue("newVal");
        Assert.assertNull(old);
        Assert.assertEquals("newVal", attr.getValue());
    }

    @Test
    public void testSetValueWithParent() {
        Attributes parent = new Attributes();
        parent.put("key", "oldVal");

        Attribute attr = new Attribute("key", "oldVal", parent);
        String old = attr.setValue("newVal");
        Assert.assertEquals("oldVal", old);
        Assert.assertEquals("newVal", attr.getValue());
        Assert.assertEquals("newVal", parent.get("key"));

        Attribute notInParent = new Attribute("otherKey", "otherVal", parent);
        String oldNotFound = notInParent.setValue("changedVal");
        Assert.assertEquals("", oldNotFound);
        Assert.assertEquals("changedVal", notInParent.getValue());
    }

    @Test
    public void testHtmlAndToString() {
        Attribute attr = new Attribute("class", "btn btn-primary");
        Assert.assertEquals("class=\"btn btn-primary\"", attr.html());
        Assert.assertEquals("class=\"btn btn-primary\"", attr.toString());

        Attribute booleanAttr = new Attribute("disabled", "");
        Assert.assertEquals("disabled", booleanAttr.html());
    }

    @Test
    public void testHtmlAppendable() throws IOException {
        Attribute attr = new Attribute("href", "http://example.com/test?a=1&b=2");
        Document doc = new Document("");
        StringBuilder sb = new StringBuilder();

        attr.html(sb, doc.outputSettings());
        Assert.assertEquals("href=\"http://example.com/test?a=1&amp;b=2\"", sb.toString());
    }

    @Test
    public void testHtmlXmlSyntax() {
        Attribute attr = new Attribute("disabled", "");
        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        StringBuilder sb = new StringBuilder();
        try {
            attr.html(sb, doc.outputSettings());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals("disabled=\"\"", sb.toString());
    }

    @Test(expected = SerializationException.class)
    public void testHtmlSerializationException() {
        Appendable throwingAppendable = new Appendable() {
            @Override
            public Appendable append(CharSequence csq) throws IOException {
                throw new IOException("error");
            }

            @Override
            public Appendable append(CharSequence csq, int start, int end) throws IOException {
                throw new IOException("error");
            }

            @Override
            public Appendable append(char c) throws IOException {
                throw new IOException("error");
            }
        };

        try {
            Attribute.html("key", "val", throwingAppendable, new Document.OutputSettings());
        } catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    @Test
    public void testCreateFromEncoded() {
        Attribute attr = Attribute.createFromEncoded("title", "&quot;Hello &amp; World&quot;");
        Assert.assertEquals("title", attr.getKey());
        Assert.assertEquals("\"Hello & World\"", attr.getValue());
        Assert.assertNull(attr.parent);
    }

    @Test
    public void testIsDataAttribute() {
        Attribute attr1 = new Attribute("data-name", "John");
        Attribute attr2 = new Attribute("data-", "empty-suffix");
        Attribute attr3 = new Attribute("dataset", "val");
        Attribute attr4 = new Attribute("class", "val");

        Assert.assertTrue(attr1.isDataAttribute());
        Assert.assertFalse(attr2.isDataAttribute());
        Assert.assertFalse(attr3.isDataAttribute());
        Assert.assertFalse(attr4.isDataAttribute());

        Assert.assertTrue(Attribute.isDataAttribute("data-custom-id"));
        Assert.assertFalse(Attribute.isDataAttribute("data-"));
        Assert.assertFalse(Attribute.isDataAttribute("href"));
    }

    @Test
    public void testShouldCollapseAttribute() {
        Document.OutputSettings htmlSettings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.html);
        Document.OutputSettings xmlSettings = new Document.OutputSettings().syntax(Document.OutputSettings.Syntax.xml);

        Attribute attr1 = new Attribute("required", "");
        Attribute attr2 = new Attribute("required", "required");
        Attribute attr3 = new Attribute("required", "REQUIRED");
        Attribute attr4 = new Attribute("required", "other");
        Attribute attr5 = new Attribute("class", "");
        Attribute attr6 = new Attribute("class", "class");
        Attribute attr7 = new Attribute("disabled", null);

        Assert.assertTrue(attr1.shouldCollapseAttribute(htmlSettings));
        Assert.assertTrue(attr2.shouldCollapseAttribute(htmlSettings));
        Assert.assertTrue(attr3.shouldCollapseAttribute(htmlSettings));
        Assert.assertFalse(attr4.shouldCollapseAttribute(htmlSettings));
        Assert.assertFalse(attr5.shouldCollapseAttribute(htmlSettings));
        Assert.assertFalse(attr6.shouldCollapseAttribute(htmlSettings));
        Assert.assertTrue(attr7.shouldCollapseAttribute(htmlSettings));

        Assert.assertFalse(attr1.shouldCollapseAttribute(xmlSettings));
        Assert.assertFalse(attr2.shouldCollapseAttribute(xmlSettings));
        Assert.assertFalse(attr7.shouldCollapseAttribute(xmlSettings));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testIsBooleanAttribute() {
        Assert.assertTrue(Attribute.isBooleanAttribute("allowfullscreen"));
        Assert.assertTrue(Attribute.isBooleanAttribute("async"));
        Assert.assertTrue(Attribute.isBooleanAttribute("autofocus"));
        Assert.assertTrue(Attribute.isBooleanAttribute("checked"));
        Assert.assertTrue(Attribute.isBooleanAttribute("compact"));
        Assert.assertTrue(Attribute.isBooleanAttribute("declare"));
        Assert.assertTrue(Attribute.isBooleanAttribute("default"));
        Assert.assertTrue(Attribute.isBooleanAttribute("defer"));
        Assert.assertTrue(Attribute.isBooleanAttribute("disabled"));
        Assert.assertTrue(Attribute.isBooleanAttribute("formnovalidate"));
        Assert.assertTrue(Attribute.isBooleanAttribute("hidden"));
        Assert.assertTrue(Attribute.isBooleanAttribute("inert"));
        Assert.assertTrue(Attribute.isBooleanAttribute("ismap"));
        Assert.assertTrue(Attribute.isBooleanAttribute("itemscope"));
        Assert.assertTrue(Attribute.isBooleanAttribute("multiple"));
        Assert.assertTrue(Attribute.isBooleanAttribute("muted"));
        Assert.assertTrue(Attribute.isBooleanAttribute("nohref"));
        Assert.assertTrue(Attribute.isBooleanAttribute("noresize"));
        Assert.assertTrue(Attribute.isBooleanAttribute("noshade"));
        Assert.assertTrue(Attribute.isBooleanAttribute("novalidate"));
        Assert.assertTrue(Attribute.isBooleanAttribute("nowrap"));
        Assert.assertTrue(Attribute.isBooleanAttribute("open"));
        Assert.assertTrue(Attribute.isBooleanAttribute("readonly"));
        Assert.assertTrue(Attribute.isBooleanAttribute("required"));
        Assert.assertTrue(Attribute.isBooleanAttribute("reversed"));
        Assert.assertTrue(Attribute.isBooleanAttribute("seamless"));
        Assert.assertTrue(Attribute.isBooleanAttribute("selected"));
        Assert.assertTrue(Attribute.isBooleanAttribute("sortable"));
        Assert.assertTrue(Attribute.isBooleanAttribute("truespeed"));
        Assert.assertTrue(Attribute.isBooleanAttribute("typemustmatch"));
        Assert.assertFalse(Attribute.isBooleanAttribute("href"));
        Assert.assertFalse(Attribute.isBooleanAttribute("src"));

        Attribute booleanInstance = new Attribute("disabled", "disabled");
        Assert.assertTrue(booleanInstance.isBooleanAttribute());

        Attribute nonBooleanInstance = new Attribute("href", "http://example.com");
        Assert.assertFalse(nonBooleanInstance.isBooleanAttribute());

        Attribute nullValInstance = new Attribute("custom", null);
        Assert.assertTrue(nullValInstance.isBooleanAttribute());
    }

    @Test
    public void testEqualsAndHashCode() {
        Attribute a1 = new Attribute("key", "val");
        Attribute a2 = new Attribute("key", "val");
        Attribute a3 = new Attribute("key2", "val");
        Attribute a4 = new Attribute("key", "val2");
        Attribute a5 = new Attribute("key", null);
        Attribute a6 = new Attribute("key", null);

        Assert.assertTrue(a1.equals(a1));
        Assert.assertTrue(a1.equals(a2));
        Assert.assertEquals(a1.hashCode(), a2.hashCode());

        Assert.assertFalse(a1.equals(null));
        Assert.assertFalse(a1.equals("some string"));
        Assert.assertFalse(a1.equals(a3));
        Assert.assertFalse(a1.equals(a4));
        Assert.assertFalse(a1.equals(a5));
        Assert.assertFalse(a5.equals(a1));

        Assert.assertTrue(a5.equals(a6));
        Assert.assertEquals(a5.hashCode(), a6.hashCode());

        Attribute aWithParent = new Attribute("key", "val", new Attributes());
        Assert.assertTrue(a1.equals(aWithParent));
        Assert.assertEquals(a1.hashCode(), aWithParent.hashCode());
    }

    @Test
    public void testClone() {
        Attribute attr = new Attribute("key", "value");
        Attribute clone = attr.clone();

        Assert.assertNotSame(attr, clone);
        Assert.assertEquals(attr.getKey(), clone.getKey());
        Assert.assertEquals(attr.getValue(), clone.getValue());
        Assert.assertEquals(attr, clone);

        clone.setKey("newKey");
        clone.setValue("newValue");
        Assert.assertEquals("key", attr.getKey());
        Assert.assertEquals("value", attr.getValue());
    }
}
