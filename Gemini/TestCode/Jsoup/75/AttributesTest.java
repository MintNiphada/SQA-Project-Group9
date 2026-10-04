package org.jsoup.nodes;

import org.jsoup.SerializationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class AttributesTest {

    @Test
    public void testPutAndGet() {
        Attributes attrs = new Attributes();
        Assert.assertEquals(0, attrs.size());
        attrs.put("href", "http://example.com");
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals("http://example.com", attrs.get("href"));
        Assert.assertEquals("http://example.com", attrs.getIgnoreCase("HREF"));
        Assert.assertEquals("", attrs.get("nonexistent"));
        Assert.assertEquals("", attrs.getIgnoreCase("nonexistent"));
    }

    @Test
    public void testPutOverwrite() {
        Attributes attrs = new Attributes();
        attrs.put("key", "val1");
        attrs.put("key", "val2");
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals("val2", attrs.get("key"));
    }

    @Test
    public void testPutIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.putIgnoreCase("KEY", "val1");
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals("val1", attrs.get("KEY"));
        
        attrs.putIgnoreCase("key", "val2");
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals("val2", attrs.get("key"));
        Assert.assertEquals("val2", attrs.getIgnoreCase("KEY"));

        attrs.putIgnoreCase("key", "val3");
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals("val3", attrs.get("key"));
    }

    @Test
    public void testPutBoolean() {
        Attributes attrs = new Attributes();
        attrs.put("required", true);
        Assert.assertTrue(attrs.hasKey("required"));
        Assert.assertTrue(attrs.hasKeyIgnoreCase("REQUIRED"));
        Assert.assertEquals("", attrs.get("required"));

        attrs.put("required", false);
        Assert.assertFalse(attrs.hasKey("required"));
        Assert.assertEquals(0, attrs.size());
    }

    @Test
    public void testPutAttributeObject() {
        Attributes attrs = new Attributes();
        Attribute attr = new Attribute("title", "hover");
        attrs.put(attr);
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals("hover", attrs.get("title"));
    }

    @Test
    public void testCapacityGrowth() {
        Attributes attrs = new Attributes();
        for (int i = 0; i < 10; i++) {
            attrs.put("key" + i, "val" + i);
        }
        Assert.assertEquals(10, attrs.size());
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("val" + i, attrs.get("key" + i));
        }
    }

    @Test
    public void testRemove() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");
        attrs.put("c", "3");

        attrs.remove("b");
        Assert.assertEquals(2, attrs.size());
        Assert.assertFalse(attrs.hasKey("b"));
        Assert.assertEquals("1", attrs.get("a"));
        Assert.assertEquals("3", attrs.get("c"));

        attrs.remove("nonexistent");
        Assert.assertEquals(2, attrs.size());

        attrs.remove("c");
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals("1", attrs.get("a"));

        attrs.remove("a");
        Assert.assertEquals(0, attrs.size());
    }

    @Test
    public void testRemoveIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("KeyOne", "val1");
        attrs.put("KeyTwo", "val2");

        attrs.removeIgnoreCase("keyone");
        Assert.assertEquals(1, attrs.size());
        Assert.assertFalse(attrs.hasKey("KeyOne"));

        attrs.removeIgnoreCase("nonexistent");
        Assert.assertEquals(1, attrs.size());
    }

    @Test
    public void testHasKeyAndHasKeyIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.put("MixedCase", "val");

        Assert.assertTrue(attrs.hasKey("MixedCase"));
        Assert.assertFalse(attrs.hasKey("mixedcase"));
        Assert.assertTrue(attrs.hasKeyIgnoreCase("mixedcase"));
        Assert.assertTrue(attrs.hasKeyIgnoreCase("MIXEDCASE"));
        Assert.assertFalse(attrs.hasKeyIgnoreCase("other"));
    }

    @Test
    public void testAddAll() {
        Attributes src = new Attributes();
        src.put("a", "1");
        src.put("b", "2");

        Attributes dst = new Attributes();
        dst.put("c", "3");
        dst.addAll(src);

        Assert.assertEquals(3, dst.size());
        Assert.assertEquals("1", dst.get("a"));
        Assert.assertEquals("2", dst.get("b"));
        Assert.assertEquals("3", dst.get("c"));

        Attributes empty = new Attributes();
        dst.addAll(empty);
        Assert.assertEquals(3, dst.size());
    }

    @Test
    public void testIterator() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("b", "2");

        Iterator<Attribute> iter = attrs.iterator();
        Assert.assertTrue(iter.hasNext());
        Attribute a1 = iter.next();
        Assert.assertEquals("a", a1.getKey());
        Assert.assertEquals("1", a1.getValue());

        iter.remove();
        Assert.assertEquals(1, attrs.size());
        Assert.assertFalse(attrs.hasKey("a"));

        Assert.assertTrue(iter.hasNext());
        Attribute a2 = iter.next();
        Assert.assertEquals("b", a2.getKey());
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void testAsList() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        attrs.put("checked", null);

        List<Attribute> list = attrs.asList();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("a", list.get(0).getKey());
        Assert.assertEquals("1", list.get(0).getValue());
        Assert.assertTrue(list.get(1) instanceof BooleanAttribute);
        Assert.assertEquals("checked", list.get(1).getKey());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAsListUnmodifiable() {
        Attributes attrs = new Attributes();
        attrs.put("a", "1");
        List<Attribute> list = attrs.asList();
        list.remove(0);
    }

    @Test
    public void testHtml() {
        Attributes attrs = new Attributes();
        attrs.put("id", "test");
        attrs.put("checked", null);
        attrs.put("data-name", "value");

        String html = attrs.html();
        Assert.assertEquals(" id=\"test\" checked data-name=\"value\"", html);
        Assert.assertEquals(html, attrs.toString());
    }

    @Test
    public void testHtmlXmlSyntax() {
        Attributes attrs = new Attributes();
        attrs.put("checked", null);
        attrs.put("disabled", "disabled");

        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        StringBuilder sb = new StringBuilder();
        try {
            attrs.html(sb, doc.outputSettings());
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(" checked=\"\" disabled=\"disabled\"", sb.toString());
    }

    @Test
    public void testHtmlEscapes() {
        Attributes attrs = new Attributes();
        attrs.put("title", "foo & \"bar\"");
        Assert.assertEquals(" title=\"foo &amp; &quot;bar&quot;\"", attrs.html());
    }

    @Test
    public void testHtmlExceptionHandling() {
        Attributes attrs = new Attributes();
        attrs.put("k", "v");
        Appendable failingAppendable = new Appendable() {
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

        try {
            attrs.html(failingAppendable, new Document("").outputSettings());
            Assert.fail("Expected IOException");
        } catch (IOException ignored) {
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        Attributes a1 = new Attributes();
        Attributes a2 = new Attributes();
        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());

        a1.put("k1", "v1");
        Assert.assertNotEquals(a1, a2);

        a2.put("k1", "v1");
        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());

        Assert.assertNotEquals(a1, null);
        Assert.assertNotEquals(a1, "other object");
        Assert.assertEquals(a1, a1);

        Attributes a3 = new Attributes();
        a3.put("k1", "v2");
        Assert.assertNotEquals(a1, a3);

        Attributes a4 = new Attributes();
        a4.put("k2", "v1");
        Assert.assertNotEquals(a1, a4);
    }

    @Test
    public void testClone() {
        Attributes original = new Attributes();
        original.put("k1", "v1");
        original.put("k2", "v2");

        Attributes clone = original.clone();
        Assert.assertEquals(original, clone);
        Assert.assertNotSame(original, clone);

        clone.put("k1", "changed");
        Assert.assertEquals("v1", original.get("k1"));
        Assert.assertEquals("changed", clone.get("k1"));
    }

    @Test
    public void testNormalize() {
        Attributes attrs = new Attributes();
        attrs.put("KEY_ONE", "val1");
        attrs.put("Key_Two", "val2");

        attrs.normalize();
        Assert.assertTrue(attrs.hasKey("key_one"));
        Assert.assertTrue(attrs.hasKey("key_two"));
        Assert.assertEquals("val1", attrs.get("key_one"));
        Assert.assertEquals("val2", attrs.get("key_two"));
    }

    @Test
    public void testDataset() {
        Attributes attrs = new Attributes();
        attrs.put("data-item", "1");
        attrs.put("data-type", "test");
        attrs.put("id", "main");

        Map<String, String> dataset = attrs.dataset();
        Assert.assertEquals(2, dataset.size());
        Assert.assertEquals("1", dataset.get("item"));
        Assert.assertEquals("test", dataset.get("type"));

        String oldVal = dataset.put("type", "updated");
        Assert.assertEquals("test", oldVal);
        Assert.assertEquals("updated", attrs.get("data-type"));

        String newOld = dataset.put("new-key", "new-val");
        Assert.assertNull(newOld);
        Assert.assertEquals("new-val", attrs.get("data-new-key"));

        Iterator<Map.Entry<String, String>> iter = dataset.entrySet().iterator();
        Assert.assertTrue(iter.hasNext());
        Map.Entry<String, String> entry = iter.next();
        Assert.assertEquals("item", entry.getKey());
        Assert.assertEquals("1", entry.getValue());
        iter.remove();
        Assert.assertFalse(attrs.hasKey("data-item"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexOfKeyNullThrows() {
        Attributes attrs = new Attributes();
        attrs.indexOfKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutNullAttributeThrows() {
        Attributes attrs = new Attributes();
        attrs.put((Attribute) null);
    }

    @Test
    public void testCheckNotNull() {
        Assert.assertEquals("", Attributes.checkNotNull(null));
        Assert.assertEquals("test", Attributes.checkNotNull("test"));
    }
}
