package org.apache.commons.lang;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

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
        Assert.assertEquals("&#913;", Entities.HTML32.escape("\u0391"));
        Assert.assertEquals("&Alpha;", Entities.HTML32.unescape("&Alpha;"));
    }

    @Test
    public void testHtml40Entities() {
        Assert.assertEquals("&Alpha;", Entities.HTML40.escape("\u0391"));
        Assert.assertEquals("\u0391", Entities.HTML40.unescape("&Alpha;"));
        Assert.assertEquals("&euro;", Entities.HTML40.escape("\u20AC"));
        Assert.assertEquals("\u20AC", Entities.HTML40.unescape("&euro;"));
    }

    @Test
    public void testEscapeNonAsciiAndBasic() {
        Entities entities = new Entities();
        entities.addEntity("foo", 161);
        Assert.assertEquals("abc &foo; &#300; xyz", entities.escape("abc \u00A1 \u012C xyz"));
    }

    @Test
    public void testEscapeWriter() throws IOException {
        Entities entities = new Entities();
        entities.addEntity("foo", 161);
        StringWriter writer = new StringWriter();
        entities.escape(writer, "abc \u00A1 \u012C xyz");
        Assert.assertEquals("abc &foo; &#300; xyz", writer.toString());
    }

    @Test
    public void testUnescapeSimple() {
        Entities entities = new Entities();
        entities.addEntity("foo", 161);
        Assert.assertEquals("NoAmpersand", entities.unescape("NoAmpersand"));
        Assert.assertEquals("abc \u00A1 \u012C xyz", entities.unescape("abc &foo; &#300; xyz"));
        Assert.assertEquals("abc \u00A1 \u012C xyz", entities.unescape("abc &foo; &#x12C; xyz"));
        Assert.assertEquals("abc \u00A1 \u012C xyz", entities.unescape("abc &foo; &#X12C; xyz"));
    }

    @Test
    public void testUnescapeWriterSimple() throws IOException {
        Entities entities = new Entities();
        entities.addEntity("foo", 161);
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "NoAmpersand");
        Assert.assertEquals("NoAmpersand", writer.toString());

        writer = new StringWriter();
        entities.unescape(writer, "abc &foo; &#300; xyz");
        Assert.assertEquals("abc \u00A1 \u012C xyz", writer.toString());
    }

    @Test
    public void testUnescapeMalformedEntities() {
        Entities entities = new Entities();
        Assert.assertEquals("&", entities.unescape("&"));
        Assert.assertEquals(";&", entities.unescape(";&"));
        Assert.assertEquals("&;", entities.unescape("&;"));
        Assert.assertEquals("&#;", entities.unescape("&#;"));
        Assert.assertEquals("&#x;", entities.unescape("&#x;"));
        Assert.assertEquals("&#X;", entities.unescape("&#X;"));
        Assert.assertEquals("&#invalid;", entities.unescape("&#invalid;"));
        Assert.assertEquals("&#xinvalid;", entities.unescape("&#xinvalid;"));
        Assert.assertEquals("&unknown;", entities.unescape("&unknown;"));
        Assert.assertEquals("&a&b;", entities.unescape("&a&b;"));
    }

    @Test
    public void testUnescapeWriterMalformedEntities() throws IOException {
        Entities entities = new Entities();
        StringWriter writer = new StringWriter();
        entities.unescape(writer, "&");
        entities.unescape(writer, ";&");
        entities.unescape(writer, "&;");
        entities.unescape(writer, "&#;");
        entities.unescape(writer, "&#x;");
        entities.unescape(writer, "&#X;");
        entities.unescape(writer, "&#invalid;");
        entities.unescape(writer, "&unknown;");
        entities.unescape(writer, "&a&b;");
        Assert.assertEquals("&&;&;&#;&#x;&#X;&#invalid;&unknown;&a&b;", writer.toString());
    }

    @Test
    public void testPrimitiveEntityMap() {
        Entities.PrimitiveEntityMap map = new Entities.PrimitiveEntityMap();
        map.add("foo", 100);
        Assert.assertEquals("foo", map.name(100));
        Assert.assertNull(map.name(200));
        Assert.assertEquals(100, map.value("foo"));
        Assert.assertEquals(-1, map.value("bar"));
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
    public void testLookupEntityMap() {
        Entities.LookupEntityMap map = new Entities.LookupEntityMap();
        map.add("low", 50);
        map.add("high", 300);
        Assert.assertEquals("low", map.name(50));
        Assert.assertNull(map.name(60));
        Assert.assertEquals("high", map.name(300));
        Assert.assertNull(map.name(350));
        Assert.assertEquals(50, map.value("low"));
        Assert.assertEquals(300, map.value("high"));
        Assert.assertEquals(-1, map.value("unknown"));
    }

    @Test
    public void testArrayEntityMap() {
        Entities.ArrayEntityMap map = new Entities.ArrayEntityMap(2);
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
        Assert.assertNull(map.name(5));
        Assert.assertNull(map.name(15));
        Assert.assertNull(map.name(40));
        Assert.assertEquals(10, map.value("a"));
        Assert.assertEquals(20, map.value("b"));
        Assert.assertEquals(30, map.value("c"));
        Assert.assertEquals(-1, map.value("d"));

        Entities.BinaryEntityMap defaultMap = new Entities.BinaryEntityMap();
        defaultMap.add("x", 100);
        Assert.assertEquals("x", defaultMap.name(100));
    }

    @Test
    public void testAddEntitiesArray() {
        Entities entities = new Entities();
        String[][] array = {
            {"foo", "100"},
            {"bar", "200"}
        };
        entities.addEntities(array);
        Assert.assertEquals("foo", entities.entityName(100));
        Assert.assertEquals("bar", entities.entityName(200));
        Assert.assertEquals(100, entities.entityValue("foo"));
        Assert.assertEquals(200, entities.entityValue("bar"));
    }
}
