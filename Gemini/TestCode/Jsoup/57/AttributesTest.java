package org.jsoup.nodes;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AttributesTest {

    @Test
    public void testEmptyAttributes() {
        Attributes attrs = new Attributes();
        Assert.assertEquals(0, attrs.size());
        Assert.assertEquals("", attrs.get("key"));
        Assert.assertEquals("", attrs.getIgnoreCase("key"));
        Assert.assertFalse(attrs.hasKey("key"));
        Assert.assertFalse(attrs.hasKeyIgnoreCase("key"));
        Assert.assertFalse(attrs.iterator().hasNext());
        Assert.assertTrue(attrs.asList().isEmpty());
        Assert.assertEquals("", attrs.html());
        Assert.assertEquals("", attrs.toString());
        Assert.assertEquals(0, attrs.hashCode());
    }

    @Test
    public void testPutAndGet() {
        Attributes attrs = new Attributes();
        attrs.put("href", "http://example.com");
        attrs.put("class", "main");

        Assert.assertEquals(2, attrs.size());
        Assert.assertEquals("http://example.com", attrs.get("href"));
        Assert.assertEquals("main", attrs.get("class"));
        Assert.assertEquals("", attrs.get("nonexistent"));
        Assert.assertTrue(attrs.hasKey("href"));
        Assert.assertFalse(attrs.hasKey("HREF"));
        Assert.assertTrue(attrs.hasKeyIgnoreCase("HREF"));
        Assert.assertEquals("http://example.com", attrs.getIgnoreCase("HREF"));
        Assert.assertEquals("", attrs.getIgnoreCase("missing"));
    }

    @Test
    public void testPutBoolean() {
        Attributes attrs = new Attributes();
        attrs.put("required", true);
        Assert.assertTrue(attrs.hasKey("required"));
        Assert.assertEquals("", attrs.get("required"));

        attrs.put("required", false);
        Assert.assertFalse(attrs.hasKey("required"));
        Assert.assertEquals(0, attrs.size());

        attrs.put("disabled", false);
        Assert.assertFalse(attrs.hasKey("disabled"));
    }

    @Test
    public void testPutAttributeObject() {
        Attributes attrs = new Attributes();
        Attribute attr = new Attribute("id", "testId");
        attrs.put(attr);

        Assert.assertTrue(attrs.hasKey("id"));
        Assert.assertEquals("testId", attrs.get("id"));

        Attribute updated = new Attribute("id", "newId");
        attrs.put(updated);
        Assert.assertEquals(1, attrs.size());
        Assert.assertEquals("newId", attrs.get("id"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutNullAttribute() {
        Attributes attrs = new Attributes();
        attrs.put(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullKey() {
        Attributes attrs = new Attributes();
        attrs.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEmptyKey() {
        Attributes attrs = new Attributes();
        attrs.get("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIgnoreCaseNullKey() {
        Attributes attrs = new Attributes();
        attrs.getIgnoreCase(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIgnoreCaseEmptyKey() {
        Attributes attrs = new Attributes();
        attrs.getIgnoreCase("");
    }

    @Test
    public void testRemove() {
        Attributes attrs = new Attributes();
        attrs.remove("nonexistent");

        attrs.put("Key", "Value");
        attrs.remove("key");
        Assert.assertTrue(attrs.hasKey("Key"));

        attrs.remove("Key");
        Assert.assertFalse(attrs.hasKey("Key"));
        Assert.assertEquals(0, attrs.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullKey() {
        Attributes attrs = new Attributes();
        attrs.remove(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveEmptyKey() {
        Attributes attrs = new Attributes();
        attrs.remove("");
    }

    @Test
    public void testRemoveIgnoreCase() {
        Attributes attrs = new Attributes();
        attrs.removeIgnoreCase("key");

        attrs.put("Key", "Value");
        attrs.put("Other", "Val2");
        attrs.removeIgnoreCase("kEy");

        Assert.assertFalse(attrs.hasKeyIgnoreCase("Key"));
        Assert.assertTrue(attrs.hasKey("Other"));
        Assert.assertEquals(1, attrs.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveIgnoreCaseNullKey() {
        Attributes attrs = new Attributes();
        attrs.removeIgnoreCase(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveIgnoreCaseEmptyKey() {
        Attributes attrs = new Attributes();
        attrs.removeIgnoreCase("");
    }

    @Test
    public void testAddAll() {
        Attributes a1 = new Attributes();
        Attributes a2 = new Attributes();
        a1.addAll(a2);
        Assert.assertEquals(0, a1.size());

        a2.put("key1", "val1");
        a2.put("key2", "val2");
        a1.addAll(a2);
        Assert.assertEquals(2, a1.size());
        Assert.assertEquals("val1", a1.get("key1"));
        Assert.assertEquals("val2", a1.get("key2"));

        Attributes a3 = new Attributes();
        a3.put("key2", "val2-new");
        a3.put("key3", "val3");
        a1.addAll(a3);
        Assert.assertEquals(3, a1.size());
        Assert.assertEquals("val2-new", a1.get("key2"));
    }

    @Test
    public void testIterator() {
        Attributes attrs = new Attributes();
        attrs.put("k1", "v1");
        attrs.put("k2", "v2");

        Iterator<Attribute> it = attrs.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("k1", it.next().getKey());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("k2", it.next().getKey());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testAsList() {
        Attributes attrs = new Attributes();
        Assert.assertEquals(0, attrs.asList().size());

        attrs.put("k1", "v1");
        attrs.put("k2", "v2");
        List<Attribute> list = attrs.asList();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("k1", list.get(0).getKey());
        Assert.assertEquals("k2", list.get(1).getKey());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAsListIsUnmodifiable() {
        Attributes attrs = new Attributes();
        attrs.put("k1", "v1");
        List<Attribute> list = attrs.asList();
        list.add(new Attribute("k2", "v2"));
    }

    @Test
    public void testHtmlOutput() throws IOException {
        Attributes attrs = new Attributes();
        attrs.put("class", "test");
        attrs.put("disabled", true);
        attrs.put("title", "hello & world");

        String html = attrs.html();
        Assert.assertTrue(html.contains(" class=\"test\""));
        Assert.assertTrue(html.contains(" disabled"));
        Assert.assertTrue(html.contains(" title=\"hello &amp; world\""));

        StringBuilder sb = new StringBuilder();
        Document doc = new Document("");
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        attrs.html(sb, doc.outputSettings());
        Assert.assertTrue(sb.toString().contains("disabled=\"\""));
    }

    @Test
    public void testHtmlEmptyAppendable() throws IOException {
        Attributes attrs = new Attributes();
        StringBuilder sb = new StringBuilder();
        attrs.html(sb, new Document("").outputSettings());
        Assert.assertEquals(0, sb.length());
    }

    @Test
    public void testEqualsAndHashCode() {
        Attributes a1 = new Attributes();
        Attributes a2 = new Attributes();
        Assert.assertEquals(a1, a1);
        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());

        a1.put("k1", "v1");
        Assert.assertNotEquals(a1, a2);
        Assert.assertNotEquals(a2, a1);

        a2.put("k1", "v1");
        Assert.assertEquals(a1, a2);
        Assert.assertEquals(a1.hashCode(), a2.hashCode());

        a1.put("k2", "v2");
        a2.put("k2", "other");
        Assert.assertNotEquals(a1, a2);

        Assert.assertFalse(a1.equals(null));
        Assert.assertFalse(a1.equals("string"));
    }

    @Test
    public void testClone() {
        Attributes empty = new Attributes();
        Attributes emptyClone = empty.clone();
        Assert.assertEquals(0, emptyClone.size());
        Assert.assertEquals(empty, emptyClone);

        Attributes attrs = new Attributes();
        attrs.put("k1", "v1");
        attrs.put("k2", "v2");

        Attributes clone = attrs.clone();
        Assert.assertEquals(attrs, clone);
        Assert.assertEquals(attrs.size(), clone.size());

        clone.put("k1", "v1-mod");
        Assert.assertEquals("v1", attrs.get("k1"));
        Assert.assertEquals("v1-mod", clone.get("k1"));
    }

    @Test
    public void testDataset() {
        Attributes attrs = new Attributes();
        Map<String, String> dataset = attrs.dataset();
        Assert.assertEquals(0, dataset.size());

        dataset.put("name", "jsoup");
        Assert.assertEquals("jsoup", dataset.get("name"));
        Assert.assertEquals("jsoup", attrs.get("data-name"));
        Assert.assertTrue(attrs.hasKey("data-name"));
        Assert.assertEquals(1, dataset.size());

        String old = dataset.put("name", "jsoup-updated");
        Assert.assertEquals("jsoup", old);
        Assert.assertEquals("jsoup-updated", dataset.get("name"));
        Assert.assertEquals("jsoup-updated", attrs.get("data-name"));

        attrs.put("other", "regular-attr");
        Assert.assertEquals(1, dataset.size());

        attrs.put("data-title", "doc");
        Assert.assertEquals(2, dataset.size());

        Iterator<Map.Entry<String, String>> it = dataset.entrySet().iterator();
        Assert.assertTrue(it.hasNext());
        Map.Entry<String, String> entry1 = it.next();
        Assert.assertEquals("name", entry1.getKey());
        Assert.assertEquals("jsoup-updated", entry1.getValue());

        Assert.assertTrue(it.hasNext());
        Map.Entry<String, String> entry2 = it.next();
        Assert.assertEquals("title", entry2.getKey());
        Assert.assertEquals("doc", entry2.getValue());

        it.remove();
        Assert.assertFalse(attrs.hasKey("data-title"));
        Assert.assertEquals(1, dataset.size());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testDatasetEmptyIterate() {
        Attributes attrs = new Attributes();
        attrs.put("regular", "attr");
        Map<String, String> dataset = attrs.dataset();
        Assert.assertEquals(0, dataset.size());
        Assert.assertFalse(dataset.entrySet().iterator().hasNext());
    }
}
