package com.google.debugging.sourcemap;

import com.google.debugging.sourcemap.proto.Mapping.OriginalMapping;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SourceMapConsumerV3Test {

  private SourceMapConsumerV3 consumer;

  @Before
  public void setUp() {
    consumer = new SourceMapConsumerV3();
  }

  @Test
  public void testParseValidBasicMap() throws Exception {
    // "AAAA" = [0, 0, 0, 0], ";;;" = empty lines
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":2,\n"
        + "\"mappings\":\"AAAA;AACA\",\n"
        + "\"sources\":[\"input.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    Collection<String> sources = consumer.getOriginalSources();
    Assert.assertEquals(1, sources.size());
    Assert.assertTrue(sources.contains("input.js"));

    OriginalMapping mapping1 = consumer.getMappingForLine(1, 1);
    Assert.assertNotNull(mapping1);
    Assert.assertEquals("input.js", mapping1.getOriginalFile());
    Assert.assertEquals(0, mapping1.getLineNumber());
    Assert.assertEquals(0, mapping1.getColumnPosition());

    OriginalMapping mapping2 = consumer.getMappingForLine(2, 1);
    Assert.assertNotNull(mapping2);
    Assert.assertEquals("input.js", mapping2.getOriginalFile());
    Assert.assertEquals(1, mapping2.getLineNumber());
    Assert.assertEquals(0, mapping2.getColumnPosition());
  }

  @Test
  public void testParseNamedEntry() throws Exception {
    // "AAAAA" = [0, 0, 0, 0, 0]
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAAA\",\n"
        + "\"sources\":[\"foo.js\"],\n"
        + "\"names\":[\"bar\"]\n"
        + "}";

    consumer.parse(json);

    OriginalMapping mapping = consumer.getMappingForLine(1, 1);
    Assert.assertNotNull(mapping);
    Assert.assertEquals("foo.js", mapping.getOriginalFile());
    Assert.assertEquals(0, mapping.getLineNumber());
    Assert.assertEquals(0, mapping.getColumnPosition());
    Assert.assertEquals("bar", mapping.getIdentifier());
  }

  @Test
  public void testParseUnmappedEntry() throws Exception {
    // "A" = [0] (single value = unmapped generated column)
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"A\",\n"
        + "\"sources\":[],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    OriginalMapping mapping = consumer.getMappingForLine(1, 1);
    Assert.assertNull(mapping);
  }

  @Test
  public void testBinarySearchAndPreviousMapping() throws Exception {
    // Line 1: col 0->srcLine 0, col 4->srcLine 0 col 1, col 8->srcLine 0 col 2, col 12->srcLine 0 col 3
    // "AAAA,IAAC,IAAC,IAAC;"
    // AAAA = [0, 0, 0, 0]
    // IAAC = [4, 0, 0, 1]
    // IAAC = [4, 0, 0, 1] (rel => col 8, src 0, line 0, srcCol 2)
    // IAAC = [4, 0, 0, 1] (rel => col 12, src 0, line 0, srcCol 3)
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":3,\n"
        + "\"mappings\":\"AAAA,IAAC,IAAC,IAAC;;AAAA\",\n"
        + "\"sources\":[\"foo.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    // Exact matches
    Assert.assertEquals(0, consumer.getMappingForLine(1, 1).getColumnPosition());
    Assert.assertEquals(1, consumer.getMappingForLine(1, 5).getColumnPosition());
    Assert.assertEquals(2, consumer.getMappingForLine(1, 9).getColumnPosition());
    Assert.assertEquals(3, consumer.getMappingForLine(1, 13).getColumnPosition());

    // In-between / Binary search boundaries
    Assert.assertEquals(0, consumer.getMappingForLine(1, 3).getColumnPosition());
    Assert.assertEquals(1, consumer.getMappingForLine(1, 6).getColumnPosition());
    Assert.assertEquals(2, consumer.getMappingForLine(1, 10).getColumnPosition());
    Assert.assertEquals(3, consumer.getMappingForLine(1, 20).getColumnPosition());

    // Line 2 is empty -> should fallback to previous mapping from line 1 (last entry on line 1)
    OriginalMapping prev = consumer.getMappingForLine(2, 1);
    Assert.assertNotNull(prev);
    Assert.assertEquals(3, prev.getColumnPosition());

    // Out of bounds queries
    Assert.assertNull(consumer.getMappingForLine(0, 1));
    Assert.assertNull(consumer.getMappingForLine(4, 1));
  }

  @Test
  public void testGetMappingBeforeFirstColumn() throws Exception {
    // First entry at column 4: "IAAA" = [4, 0, 0, 0]
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":2,\n"
        + "\"mappings\":\"AAAA;IAAA\",\n"
        + "\"sources\":[\"foo.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    // On line 2, query column 1 (generated 0), before column 4 (generated 4) -> falls back to previous line
    OriginalMapping mapping = consumer.getMappingForLine(2, 1);
    Assert.assertNotNull(mapping);
    Assert.assertEquals(0, mapping.getLineNumber());
  }

  @Test
  public void testReverseMapping() throws Exception {
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":2,\n"
        + "\"mappings\":\"AAAA,IAAA;AACA\",\n"
        + "\"sources\":[\"foo.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    consumer.parse(json);

    // Reverse mapping for foo.js at line 0
    Collection<OriginalMapping> mappingsLine0 = consumer.getReverseMapping("foo.js", 0, 0);
    Assert.assertEquals(2, mappingsLine0.size());

    // Reverse mapping for foo.js at line 1
    Collection<OriginalMapping> mappingsLine1 = consumer.getReverseMapping("foo.js", 1, 0);
    Assert.assertEquals(1, mappingsLine1.size());

    // Non-existent file
    Collection<OriginalMapping> notFoundFile = consumer.getReverseMapping("bar.js", 0, 0);
    Assert.assertTrue(notFoundFile.isEmpty());

    // Non-existent line
    Collection<OriginalMapping> notFoundLine = consumer.getReverseMapping("foo.js", 99, 0);
    Assert.assertTrue(notFoundLine.isEmpty());
  }

  @Test
  public void testVisitMappings() throws Exception {
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAA,IAACA;\",\n"
        + "\"sources\":[\"foo.js\"],\n"
        + "\"names\":[\"sym\"]\n"
        + "}";

    consumer.parse(json);

    final List<String> visited = new ArrayList<String>();
    consumer.visitMappings(new SourceMapConsumerV3.EntryVisitor() {
      @Override
      public void visit(String sourceName, String symbolName,
                        FilePosition sourceStartPosition,
                        FilePosition startPosition,
                        FilePosition endPosition) {
        visited.add(sourceName + ":" + symbolName + ":"
            + sourceStartPosition.getLine() + "," + sourceStartPosition.getColumn()
            + "->" + startPosition.getLine() + "," + startPosition.getColumn()
            + " to " + endPosition.getLine() + "," + endPosition.getColumn());
      }
    });

    Assert.assertEquals(1, visited.size());
    Assert.assertEquals("foo.js:null:0,0->0,0 to 0,4", visited.get(0));
  }

  @Test
  public void testParseMetaMapWithSectionsMap() throws Exception {
    String sectionMap = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"section.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAA\",\n"
        + "\"sources\":[\"source1.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    JSONObject metaJson = new JSONObject();
    metaJson.put("version", 3);
    metaJson.put("file", "combined.js");
    org.json.JSONArray sections = new org.json.JSONArray();
    JSONObject section = new JSONObject();
    JSONObject offset = new JSONObject();
    offset.put("line", 0);
    offset.put("column", 0);
    section.put("offset", offset);
    section.put("map", sectionMap);
    sections.put(section);
    metaJson.put("sections", sections);

    consumer.parse(metaJson);

    Collection<String> sources = consumer.getOriginalSources();
    Assert.assertTrue(sources.contains("source1.js"));
  }

  @Test
  public void testParseMetaMapWithSectionsUrl() throws Exception {
    final String sectionMap = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"section.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"AAAA\",\n"
        + "\"sources\":[\"source2.js\"],\n"
        + "\"names\":[]\n"
        + "}";

    JSONObject metaJson = new JSONObject();
    metaJson.put("version", 3);
    metaJson.put("file", "combined.js");
    org.json.JSONArray sections = new org.json.JSONArray();
    JSONObject section = new JSONObject();
    JSONObject offset = new JSONObject();
    offset.put("line", 0);
    offset.put("column", 0);
    section.put("offset", offset);
    section.put("url", "http://example.com/map.json");
    sections.put(section);
    metaJson.put("sections", sections);

    SourceMapSupplier supplier = new SourceMapSupplier() {
      @Override
      public String getSourceMap(String url) {
        if ("http://example.com/map.json".equals(url)) {
          return sectionMap;
        }
        return null;
      }
    };

    consumer.parse(metaJson.toString(), supplier);
    Collection<String> sources = consumer.getOriginalSources();
    Assert.assertTrue(sources.contains("source2.js"));
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseInvalidJsonThrows() throws Exception {
    consumer.parse("{ invalid json ");
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseInvalidVersionThrows() throws Exception {
    String json = "{\n"
        + "\"version\":2,\n"
        + "\"file\":\"out.js\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"\",\n"
        + "\"sources\":[],\n"
        + "\"names\":[]\n"
        + "}";
    consumer.parse(json);
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseEmptyFileThrows() throws Exception {
    String json = "{\n"
        + "\"version\":3,\n"
        + "\"file\":\"\",\n"
        + "\"lineCount\":1,\n"
        + "\"mappings\":\"\",\n"
        + "\"sources\":[],\n"
        + "\"names\":[]\n"
        + "}";
    consumer.parse(json);
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseMetaMapInvalidBothUrlAndMap() throws Exception {
    JSONObject metaJson = new JSONObject();
    metaJson.put("version", 3);
    metaJson.put("file", "combined.js");
    org.json.JSONArray sections = new org.json.JSONArray();
    JSONObject section = new JSONObject();
    JSONObject offset = new JSONObject();
    offset.put("line", 0);
    offset.put("column", 0);
    section.put("offset", offset);
    section.put("map", "{}");
    section.put("url", "http://example.com/map.json");
    sections.put(section);
    metaJson.put("sections", sections);

    consumer.parse(metaJson);
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseMetaMapInvalidNeitherUrlNorMap() throws Exception {
    JSONObject metaJson = new JSONObject();
    metaJson.put("version", 3);
    metaJson.put("file", "combined.js");
    org.json.JSONArray sections = new org.json.JSONArray();
    JSONObject section = new JSONObject();
    JSONObject offset = new JSONObject();
    offset.put("line", 0);
    offset.put("column", 0);
    section.put("offset", offset);
    sections.put(section);
    metaJson.put("sections", sections);

    consumer.parse(metaJson);
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseMetaMapWithTopLevelMappingsThrows() throws Exception {
    JSONObject metaJson = new JSONObject();
    metaJson.put("version", 3);
    metaJson.put("file", "combined.js");
    metaJson.put("mappings", "AAAA");
    org.json.JSONArray sections = new org.json.JSONArray();
    metaJson.put("sections", sections);

    consumer.parse(metaJson);
  }

  @Test(expected = SourceMapParseException.class)
  public void testParseMetaMapUrlSupplierReturnsNull() throws Exception {
    JSONObject metaJson = new JSONObject();
    metaJson.put("version", 3);
    metaJson.put("file", "combined.js");
    org.json.JSONArray sections = new org.json.JSONArray();
    JSONObject section = new JSONObject();
    JSONObject offset = new JSONObject();
    offset.put("line", 0);
    offset.put("column", 0);
    section.put("offset", offset);
    section.put("url", "http://example.com/nonexistent.json");
    sections.put(section);
    metaJson.put("sections", sections);

    consumer.parse(metaJson);
  }

  @Test
  public void testDefaultSourceMapSupplier() {
    SourceMapConsumerV3.DefaultSourceMapSupplier supplier =
        new SourceMapConsumerV3.DefaultSourceMapSupplier();
    Assert.assertNull(supplier.getSourceMap("http://test.url"));
  }
}