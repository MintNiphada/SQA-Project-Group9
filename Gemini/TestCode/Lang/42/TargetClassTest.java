package org.apache.commons.lang;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class EntitiesTest {

    @Test
    public void testXmlEntities() {
        Assert.assertEquals("&quot;", Entities.XML.escape("\""));
        Assert.assertEquals("&amp;", Entities.XML.escape("&"));
        Assert.assertEquals("&lt;", Entities.XML.escape("<"));
        Assert.assertEquals("&gt;", Entities.XML.escape(">"));
        Assert.assertEquals("&apos;", Entities.XML.escape("'"));
        Assert.assertEquals("\"", Entities.XML.unescape("&quot;"));
        Assert.assertEquals("&", Entities.XML.unescape("&amp;"));
        Assert.assertEquals("<", Entities.XML.unescape("&lt;"));
        Assert.assertEquals(">", Entities.XML.unescape("&gt;"));
        Assert.assertEquals("'", Entities.XML.unescape("&apos;"));
    }

    @Test
    public void testHtml32Entities() {
        Assert.assertEquals("&nbsp;", Entities.HTML32.escape("\u00A0"));
        Assert.assertEquals("\u00A0", Entities.HTML32.unescape("&nbsp;"));
        Assert.assertEquals("&copy;", Entities.HTML32.escape("\u00A9"));
        Assert.assertEquals("\u00A9", Entities.HTML32.unescape("&copy;"));
        Assert.assertEquals("&#913;", Entities.HTML32.escape("\u0391"));
    }

    @Test
    public void testHtml40Entities() {
        Assert.assertEquals("&Alpha;", Entities.HTML40.escape("\u0391"));
        Assert.assertEquals("\u0391", Entities.HTML40.unescape("&Alpha;"));
        Assert.assertEquals("&euro;", Entities.HTML40.escape("\u20AC"));
        Assert.assertEquals("\u20AC", Entities.HTML40.unescape("&euro;"));
    }

    @Test
    public void testPrimitiveEntityMap() {
        Entities.PrimitiveEntityMap map = new Entities.PrimitiveEntityMap();
        map.add("foo", 100);
        map.add("bar", 200);
        Assert.assertEquals("foo", map.name(100));
        Assert.assertEquals("bar", map.name(200));
        Assert.assertNull(map.name(300));
        Assert.assertEquals(100, map.value("foo"));
        Assert.assertEquals(200, map.value("bar"));
        Assert.assertEquals(-1, map.value("baz"));
    }

    @Test
    public void testHashEntityMap() {
        Entities.HashEntityMap map = new Entities.HashEntityMap();
        map.add("foo", 100);
        Assert.assertEquals("foo", map.name(100));
        Assert.assertNull(map.name(200));
        Assert.assertEquals(100, map.value("foo"));
        Assert.assertEquals(-1, map.value("bar"));
    }

    @Test
    public void testTreeEntityMap() {
        Entities.TreeEntityMap map = new Entities.TreeEntityMap();
        map.add("foo", 100);
        Assert.assertEquals("foo", map.name(100));
        Assert.assertNull(map.name(200));
        Assert.assertEquals(100, map.value("foo"));
        Assert.assertEquals(-1, map.value("bar"));
    }

    @Test
    public void testArrayEntityMap() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap(1);
        map.add("a", 1);
        map.add("b", 2);
        map.add("c", 3);
        Assert.assertEquals("a", map.name(1));
        Assert.assertEquals("b", map.name(2));
        Assert.assertEquals("c", map.name(3));
        Assert.assertNull(map.name(4));
        Assert.assertEquals(1, map.value("a"));
        Assert.assertEquals(2, map.value("b"));
        Assert.assertEquals(3, map.value("c"));
        Assert.assertEquals(-1, map.value("d"));

        Entities.ArrayEntityMap defaultMap = new Entities.ArrayEntityMap();
        defaultMap.add("x", 10);
        Assert.assertEquals("x", defaultMap.name(10));
    }

    @Test
    public void testBinaryEntityMap() {
        Entities.BinaryEntityMap map = new Entities.BinaryEntityMap(2);
        map.add("c", 30);
        map.add("a", 10);
        map.add("b", 20);
        map.add("dup", 20);
        Assert.assertEquals("a", map.name(10));
        Assert.assertEquals("b", map.name(20));
        Assert.assertEquals("c", map.name(30));
        Assert.assertNull(map.name(40));
        Assert.assertEquals(10, map.value("a"));
        Assert.assertEquals(20, map.value("b"));
        Assert.assertEquals(30, map.value("c"));
        Assert.assertEquals(-1, map.value("unknown"));

        Entities.BinaryEntityMap defaultMap = new Entities.BinaryEntityMap();
        defaultMap.add("k", 5);
        Assert.assertEquals("k", defaultMap.name(5));
    }

    @Test
    public void testLookupEntityMap() {
        Entities.LookupEntityMap map = new Entities.LookupEntityMap();
        map.add("small", 65);
        map.add("large", 300);
        Assert.assertEquals("small", map.name(65));
        Assert.assertNull(map.name(66));
        Assert.assertEquals("large", map.name(300));
        Assert.assertNull(map.name(301));
    }

    @Test
    public void testCustomEntitiesAndAddEntity() {
        Entities entities = new Entities();
        entities.addEntity("foo", 161);
        entities.addEntities(new String[][]{{"bar", "162"}, {"baz", "163"}});
        Assert.assertEquals("foo", entities.entityName(161));
        Assert.assertEquals(161, entities.entityValue("foo"));
        Assert.assertEquals("&foo;&bar;&baz;", entities.escape("\u00A1\u00A2\u00A3"));
        Assert.assertEquals("\u00A1\u00A2\u00A3", entities.unescape("&foo;&bar;&baz;"));
    }

    @Test
    public void testEscapeWithoutEntities() {
        Entities entities = new Entities();
        Assert.assertEquals("abc", entities.escape("abc"));
        Assert.assertEquals("&#256;", entities.escape("\u0100"));
    }

    @Test
    public void testUnescapeFormats() {
        Entities entities = new Entities();
        entities.addEntity("foo", 100);

        Assert.assertEquals("No entities here", entities.unescape("No entities here"));
        Assert.assertEquals("d", entities.unescape("&#100;"));
        Assert.assertEquals("d", entities.unescape("&#x64;"));
        Assert.assertEquals("d", entities.unescape("&#X64;"));
        Assert.assertEquals("d", entities.unescape("&foo;"));
        Assert.assertEquals("&#invalid;", entities.unescape("&#invalid;"));
        Assert.assertEquals("&#;", entities.unescape("&#;"));
        Assert.assertEquals("&;", entities.unescape("&;"));
        Assert.assertEquals("&unknown;", entities.unescape("&unknown;"));
        Assert.assertEquals("&a&b;", entities.unescape("&a&b;"));
        Assert.assertEquals("incomplete &amp", entities.unescape("incomplete &amp"));
        Assert.assertEquals("&#70000;", entities.unescape("&#70000;"));
    }

    @Test
    public void testWriterMethods() throws IOException {
        Entities entities = new Entities();
        entities.addEntity("gt", 62);

        StringWriter writer = new StringWriter();
        entities.escape(writer, "a > b");
        Assert.assertEquals("a &gt; b", writer.toString());

        writer = new StringWriter();
        entities.unescape(writer, "plain text");
        Assert.assertEquals("plain text", writer.toString());

        writer = new StringWriter();
        entities.unescape(writer, "a &gt; b");
        Assert.assertEquals("a > b", writer.toString());
    }

    @Test(expected = UnhandledException.class)
    public void testEscapeIOException() {
        Entities entities = new Entities();
        Writer mockWriter = new Writer() {
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException();
            }
            public void flush() throws IOException {}
            public void close() throws IOException {}
        };
        try {
            entities.escape(mockWriter, "test");
        } catch (IOException e) {
            throw new UnhandledException(e);
        }
    }

    @Test(expected = UnhandledException.class)
    public void testUnescapeIOException() {
        Entities entities = new Entities();
        Writer mockWriter = new Writer() {
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException();
            }
            public void flush() throws IOException {}
            public void close() throws IOException {}
        };
        try {
            entities.unescape(mockWriter, "test &amp;");
        } catch (IOException e) {
            throw new UnhandledException(e);
        }
    }
}
