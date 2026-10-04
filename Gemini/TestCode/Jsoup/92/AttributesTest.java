package org.jsoup.nodes;

import org.jsoup.SerializationException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class AttributesTest {

    @Test
    public void testEmptyAttributes() {
        Attributes a = new Attributes();
        Assert.assertEquals(0, a.size());
        Assert.assertFalse(a.hasKey("key"));
        Assert.assertFalse(a.hasKeyIgnoreCase("key"));
        Assert.assertEquals("", a.get("key"));
        Assert.assertEquals("", a.getIgnoreCase("key"));
        Assert.assertEquals("", a.html());
        Assert.assertEquals("", a.toString());
        Assert.assertEquals(0, a.asList().size());
        Assert.assertEquals(0, a.dataset().size());
    }

    @Test
    public void testPutAndGet() {
        Attributes a = new Attributes();
        a.put("Key", "Value");
        Assert.assertEquals(1, a.size());
        Assert.assertTrue(a.hasKey("Key"));
        Assert.assertFalse(a.hasKey("key"));
        Assert.assertTrue(a.hasKeyIgnoreCase("key"));
        Assert.assertEquals("Value", a.get("Key"));
        Assert.assertEquals("", a.get("key"));
        Assert.assertEquals("Value", a.getIgnoreCase("key"));

        a.put("Key", "NewValue");
        Assert.assertEquals(1, a.size());
        Assert.assertEquals("NewValue", a.get("Key"));
    }

    @Test
    public void testPutBoolean() {
        Attributes a = new Attributes();
        a.put("required", true);
        Assert.assertEquals(1, a.size());
        Assert.assertTrue(a.hasKey("required"));
        Assert.assertEquals("", a.get("required"));

        a.put("required", false);
        Assert.assertEquals(0, a.size());
        Assert.assertFalse(a.hasKey("required"));

        a.put("REQUIRED", true);
        Assert.assertTrue(a.hasKeyIgnoreCase("required"));
        a.put("required", true);
        Assert.assertEquals(1, a.size());
        Assert.assertEquals("required", a.keys[0]);
    }

    @Test
    public void testPutAttribute() {
        Attributes a = new Attributes();
        Attribute attr = new Attribute("href", "http://example.com");
        a.put(attr);
        Assert.assertEquals("http://example.com", a.get("href"));
        Assert.assertEquals(1, a.size());
        Assert.assertSame(a, attr.parent);
    }

    @Test
    public void testPutIgnoreCase() {
        Attributes a = new Attributes();
        a.putIgnoreCase("KEY", "val1");
        Assert.assertEquals("KEY", a.keys[0]);
        Assert.assertEquals("val1", a.vals[0]);

        a.putIgnoreCase("key", "val2");
        Assert.assertEquals(1, a.size());
        Assert.assertEquals("key", a.keys[0]);
        Assert.assertEquals("val2", a.vals[0]);

        a.putIgnoreCase("key", "val3");
        Assert.assertEquals("key", a.keys[0]);
        Assert.assertEquals("val3", a.vals[0]);
    }

    @Test
    public void testCapacityExpansion() {
        Attributes a = new Attributes();
        for (int i = 0; i < 10; i++) {
            a.put("key" + i, "val" + i);
        }
        Assert.assertEquals(10, a.size());
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals("val" + i, a.get("key" + i));
        }
    }

    @Test
    public void testRemove() {
        Attributes a = new Attributes();
        a.put("k1", "v1");
        a.put("k2", "v2");
        a.put("k3", "v3");

        a.remove("k2");
        Assert.assertEquals(2, a.size());
        Assert.assertFalse(a.hasKey("k2"));
        Assert.assertTrue(a.hasKey("k1"));
        Assert.assertTrue(a.hasKey("k3"));
        Assert.assertNull(a.keys[2]);
        Assert.assertNull(a.vals[2]);

        a.remove("nonexistent");
        Assert.assertEquals(2, a.size());

        a.remove("k1");
        a.remove("k3");
        Assert.assertEquals(0, a.size());
    }

    @Test
    public void testRemoveIgnoreCase() {
        Attributes a = new Attributes();
        a.put("K1", "v1");
        a.put("k2", "v2");

        a.removeIgnoreCase("k1");
        Assert.assertEquals(1, a.size());
        Assert.assertFalse(a.hasKeyIgnoreCase("k1"));

        a.removeIgnoreCase("NONEXISTENT");
        Assert.assertEquals(1, a.size());
    }

    @Test
    public void testAddAll() {
        Attributes a = new Attributes();
        a.put("a", "1");

        Attributes empty = new Attributes();
        a.addAll(empty);
        Assert.assertEquals(1, a.size());

        Attributes b = new Attributes();
        b.put("b", "2");
        b.put("c", "3");
        a.addAll(b);
        Assert.assertEquals(3, a.size());
        Assert.assertEquals("1", a.get("a"));
        Assert.assertEquals("2", a.get("b"));
        Assert.assertEquals("3", a.get("c"));
    }

    @Test
    public void testIterator() {
        Attributes a = new Attributes();
        a.put("k1", "v1");
        a.put("k2", "v2");

        Iterator<Attribute> it = a.iterator();
        Assert.assertTrue(it.hasNext());
        Attribute a1 = it.next();
        Assert.assertEquals("k1", a1.getKey());
        Assert.assertEquals("v1", a1.getValue());

        it.remove();
        Assert.assertEquals(1, a.size());
        Assert.assertFalse(a.hasKey("k1"));
        Assert.assertTrue(a.hasKey("k2"));

        Assert.assertTrue(it.hasNext());
        Attribute a2 = it.next();
        Assert.assertEquals("k2", a2.getKey());
        Assert.assertEquals("v2", a2.getValue());

        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testAsList() {
        Attributes a = new Attributes();
        a.put("k1", "v1");
        a.put("k2", null);

        List<Attribute> list = a.asList();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("k1", list.get(0).getKey());
        Assert.assertEquals("v1", list.get(0).getValue());
        Assert.assertEquals("k2", list.get(1).getKey());
        Assert.assertEquals("", list.get(1).getValue());
        Assert.assertTrue(list.get(1) instanceof BooleanAttribute);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAsListUnmodifiable() {
        Attributes a = new Attributes();
        a.put("k1", "v1");
        List<Attribute> list = a.asList();
        list.remove(0);
    }

    @Test
    public void testDataset() {
        Attributes a = new Attributes();
        a.put("data-name", "Jsoup");
        a.put("other", "val");
        a.put("data-empty", null);

        Map<String, String> dataset = a.dataset();
        Assert.assertEquals(2, dataset.size());
        Assert.assertEquals("Jsoup", dataset.get("name"));
        Assert.assertEquals("", dataset.get("empty"));

        String old = dataset.put("name", "NewJsoup");
        Assert.assertEquals("Jsoup", old);
        Assert.assertEquals("NewJsoup", a.get("data-name"));

        String addedOld = dataset.put("new-key", "new-val");
        Assert.assertNull(addedOld);
        Assert.assertEquals("new-val", a.get("data-new-key"));

        Iterator<Map.Entry<String, String>> it = dataset.entrySet().iterator();
        Assert.assertTrue(it.hasNext());
        Map.Entry<String, String> entry = it.next();
        Assert.assertEquals("name", entry.getKey());
        Assert.assertEquals("NewJsoup", entry.getValue());
        it.remove();
        Assert.assertFalse(a.hasKey("data-name"));
    }

    @Test
    public void testHtml() {
        Attributes a = new Attributes();
        a.put("id", "test");
        a.put("class", "main & sub");
        a.put("disabled", null);

        String html = a.html();
        Assert.assertEquals(" id=\"test\" class=\"main &amp; sub\" disabled", html);
    }

    @Test
    public void testHtmlWithAppendableException() {
        Attributes a = new Attributes();
        a.put("k", "v");
        Appendable badAppendable = new Appendable() {
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
            a.html(badAppendable, new Document("").outputSettings());
            Assert.fail();
        } catch (IOException e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testEqualsAndHashCode() {
        Attributes a1 = new Attributes();
        Attributes a2 = new Attributes();
        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());
        Assert.assertEquals(a1, a1);
        Assert.assertFalse(a1.equals(null));
        Assert.assertFalse(a1.equals("not attributes"));

        a1.put("k1", "v1");
        Assert.assertNotEquals(a1, a2);
        Assert.assertNotEquals(a1.hashCode(), a2.hashCode());

        a2.put("k1", "v1");
        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());

        a2.put("k2", "v2");
        Assert.assertNotEquals(a1, a2);

        Attributes a3 = new Attributes();
        a3.put("k1", "different");
        Assert.assertNotEquals(a1, a3);

        Attributes a4 = new Attributes();
        a4.put("diff", "v1");
        Assert.assertNotEquals(a1, a4);
    }

    @Test
    public void testClone() {
        Attributes a = new Attributes();
        a.put("k1", "v1");
        a.put("k2", "v2");

        Attributes clone = a.clone();
        Assert.assertEquals(a, clone);
        Assert.assertNotSame(a, clone);

        clone.put("k3", "v3");
        Assert.assertFalse(a.hasKey("k3"));
        Assert.assertTrue(clone.hasKey("k3"));

        clone.put("k1", "modified");
        Assert.assertEquals("v1", a.get("k1"));
        Assert.assertEquals("modified", clone.get("k1"));
    }

    @Test
    public void testNormalize() {
        Attributes a = new Attributes();
        a.put("NAME", "val1");
        a.put("MixedCase", "val2");
        a.normalize();

        Assert.assertEquals("name", a.keys[0]);
        Assert.assertEquals("mixedcase", a.keys[1]);
        Assert.assertEquals("val1", a.get("name"));
        Assert.assertEquals("val2", a.get("mixedcase"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexOfKeyNull() {
        Attributes a = new Attributes();
        a.indexOfKey(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIndexOfKeyIgnoreCaseNull() {
        Attributes a = new Attributes();
        a.hasKeyIgnoreCase(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutNullAttribute() {
        Attributes a = new Attributes();
        a.put((Attribute) null);
    }

    @Test
    public void testCheckNotNull() {
        Assert.assertEquals("", Attributes.checkNotNull(null));
        Assert.assertEquals("abc", Attributes.checkNotNull("abc"));
    }
}
