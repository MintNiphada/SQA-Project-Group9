package com.google.javascript.jscomp;

import com.google.common.collect.Lists;
import com.google.debugging.sourcemap.FilePosition;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class SourceMapTest {

  @Test
  public void testFormatEnumsAndGetInstance() {
    for (SourceMap.Format format : SourceMap.Format.values()) {
      Assert.assertNotNull(format);
      SourceMap map = format.getInstance();
      Assert.assertNotNull(map);
    }
    Assert.assertEquals(SourceMap.Format.V1, SourceMap.Format.valueOf("V1"));
    Assert.assertEquals(SourceMap.Format.DEFAULT, SourceMap.Format.valueOf("DEFAULT"));
    Assert.assertEquals(SourceMap.Format.V2, SourceMap.Format.valueOf("V2"));
    Assert.assertEquals(SourceMap.Format.V3, SourceMap.Format.valueOf("V3"));
  }

  @Test
  public void testDetailLevelEnums() {
    for (SourceMap.DetailLevel level : SourceMap.DetailLevel.values()) {
      Assert.assertNotNull(level);
    }
    Assert.assertEquals(SourceMap.DetailLevel.ALL, SourceMap.DetailLevel.valueOf("ALL"));
    Assert.assertEquals(SourceMap.DetailLevel.SYMBOLS, SourceMap.DetailLevel.valueOf("SYMBOLS"));
  }

  @Test
  public void testDetailLevelAll() {
    SourceMap.DetailLevel all = SourceMap.DetailLevel.ALL;
    Assert.assertTrue(all.apply(new Node(Token.BLOCK)));
    Assert.assertTrue(all.apply(new Node(Token.CALL)));
    Assert.assertTrue(all.apply(new Node(Token.EMPTY)));
    Assert.assertTrue(all.apply(Node.newString("test")));
  }

  @Test
  public void testDetailLevelSymbols() {
    SourceMap.DetailLevel symbols = SourceMap.DetailLevel.SYMBOLS;

    // Node is Call
    Assert.assertTrue(symbols.apply(new Node(Token.CALL)));
    // Node is New
    Assert.assertTrue(symbols.apply(new Node(Token.NEW)));
    // Node is Function
    Assert.assertTrue(symbols.apply(new Node(Token.FUNCTION)));
    // Node is Name
    Assert.assertTrue(symbols.apply(Node.newString(Token.NAME, "foo")));
    // Node is GetProp / GetElem (NodeUtil.isGet)
    Assert.assertTrue(symbols.apply(new Node(Token.GETPROP)));
    Assert.assertTrue(symbols.apply(new Node(Token.GETELEM)));

    // Node is ObjectLitKey
    Node objLit = new Node(Token.OBJECTLIT);
    Node keyNode = Node.newString("key");
    objLit.addChildToBack(keyNode);
    Assert.assertTrue(symbols.apply(keyNode));

    // Node is String with parent GetProp (Node.isString() && NodeUtil.isGet(parent))
    Node getPropParent = new Node(Token.GETPROP);
    Node stringChild = Node.newString("child");
    getPropParent.addChildToBack(stringChild);
    Assert.assertTrue(symbols.apply(stringChild));

    // Negative cases
    Assert.assertFalse(symbols.apply(new Node(Token.BLOCK)));
    Assert.assertFalse(symbols.apply(new Node(Token.EMPTY)));
    Assert.assertFalse(symbols.apply(new Node(Token.VAR)));
    Assert.assertFalse(symbols.apply(new Node(Token.EXPR_RESULT)));
    Assert.assertFalse(symbols.apply(Node.newString("standalone_string")));
  }

  @Test
  public void testLocationMapping() {
    SourceMap.LocationMapping mapping = new SourceMap.LocationMapping("prefix/", "http://example.com/");
    Assert.assertEquals("prefix/", mapping.prefix);
    Assert.assertEquals("http://example.com/", mapping.replacement);
  }

  @Test
  public void testAddMappingWithNullSourceFileOrNegativeLineNumber() {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    FilePosition start = new FilePosition(1, 0);
    FilePosition end = new FilePosition(1, 5);

    // Node without source file
    Node nodeNoFile = new Node(Token.NAME, 1, 0);
    sourceMap.addMapping(nodeNoFile, start, end);

    // Node with source file but lineno < 0
    Node nodeNegLine = new Node(Token.NAME, -1, 0);
    nodeNegLine.putProp(Node.SOURCENAME_PROP, "test.js");
    sourceMap.addMapping(nodeNegLine, start, end);

    // Should not fail and should produce valid empty/initial source map output
    StringWriter sw = new StringWriter();
    try {
      sourceMap.appendTo(sw, "out.js");
      Assert.assertNotNull(sw.toString());
    } catch (IOException e) {
      Assert.fail("Unexpected IOException: " + e.getMessage());
    }
  }

  @Test
  public void testAddMappingAndOutput() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    Node node = Node.newString(Token.NAME, "varName", 1, 2);
    node.putProp(Node.SOURCENAME_PROP, "source.js");
    node.putProp(Node.ORIGINALNAME_PROP, "originalVarName");

    FilePosition start = new FilePosition(0, 0);
    FilePosition end = new FilePosition(0, 10);

    sourceMap.addMapping(node, start, end);

    StringWriter sw = new StringWriter();
    sourceMap.appendTo(sw, "out.js");
    String output = sw.toString();
    Assert.assertTrue(output.contains("source.js"));
  }

  @Test
  public void testFixupSourceLocationWithPrefixMappings() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();

    List<SourceMap.LocationMapping> mappings = Lists.newArrayList(
        new SourceMap.LocationMapping("src/js/", "http://example.com/js/"),
        new SourceMap.LocationMapping("lib/", "http://example.com/lib/")
    );
    sourceMap.setPrefixMappings(mappings);

    // 1. Matches first prefix
    Node node1 = Node.newString(Token.NAME, "a", 1, 0);
    node1.putProp(Node.SOURCENAME_PROP, "src/js/app.js");
    sourceMap.addMapping(node1, new FilePosition(0, 0), new FilePosition(0, 1));

    // 2. Matches same prefix again (covers cache hit path)
    Node node2 = Node.newString(Token.NAME, "b", 2, 0);
    node2.putProp(Node.SOURCENAME_PROP, "src/js/app.js");
    sourceMap.addMapping(node2, new FilePosition(1, 0), new FilePosition(1, 1));

    // 3. Matches second prefix
    Node node3 = Node.newString(Token.NAME, "c", 1, 0);
    node3.putProp(Node.SOURCENAME_PROP, "lib/utils.js");
    sourceMap.addMapping(node3, new FilePosition(2, 0), new FilePosition(2, 1));

    // 4. Matches no prefix (covers fixed == null branch and cache path)
    Node node4 = Node.newString(Token.NAME, "d", 1, 0);
    node4.putProp(Node.SOURCENAME_PROP, "other/unmatched.js");
    sourceMap.addMapping(node4, new FilePosition(3, 0), new FilePosition(3, 1));

    // 5. Matches no prefix again (cache hit for unmatched)
    Node node5 = Node.newString(Token.NAME, "e", 2, 0);
    node5.putProp(Node.SOURCENAME_PROP, "other/unmatched.js");
    sourceMap.addMapping(node5, new FilePosition(4, 0), new FilePosition(4, 1));

    StringWriter sw = new StringWriter();
    sourceMap.appendTo(sw, "out.js");
    String result = sw.toString();

    Assert.assertTrue(result.contains("http://example.com/js/app.js"));
    Assert.assertTrue(result.contains("http://example.com/lib/utils.js"));
    Assert.assertTrue(result.contains("other/unmatched.js"));
  }

  @Test
  public void testResetClearsCache() throws IOException {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    List<SourceMap.LocationMapping> mappings = Lists.newArrayList(
        new SourceMap.LocationMapping("prefix/", "newprefix/")
    );
    sourceMap.setPrefixMappings(mappings);

    Node node = Node.newString(Token.NAME, "foo", 1, 0);
    node.putProp(Node.SOURCENAME_PROP, "prefix/a.js");
    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 3));

    sourceMap.reset();

    // After reset, re-add mapping to verify state is clean
    sourceMap.setPrefixMappings(mappings);
    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 3));

    StringWriter sw = new StringWriter();
    sourceMap.appendTo(sw, "out.js");
    Assert.assertTrue(sw.toString().contains("newprefix/a.js"));
  }

  @Test
  public void testSetStartingPositionWrapperPrefixAndValidate() {
    SourceMap sourceMap = SourceMap.Format.V3.getInstance();
    sourceMap.setStartingPosition(10, 20);
    sourceMap.setWrapperPrefix("(function() {");
    sourceMap.validate(true);
    sourceMap.validate(false);

    Node node = Node.newString(Token.NAME, "x", 1, 0);
    node.putProp(Node.SOURCENAME_PROP, "test.js");
    sourceMap.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 1));

    StringWriter sw = new StringWriter();
    try {
      sourceMap.appendTo(sw, "out.js");
      Assert.assertNotNull(sw.toString());
    } catch (IOException e) {
      Assert.fail("Unexpected IOException: " + e.getMessage());
    }
  }

  @Test
  public void testAllFormatsSupportBasicOperations() throws IOException {
    SourceMap.Format[] formats = new SourceMap.Format[]{
        SourceMap.Format.V1,
        SourceMap.Format.V2,
        SourceMap.Format.V3,
        SourceMap.Format.DEFAULT
    };

    for (SourceMap.Format format : formats) {
      SourceMap map = format.getInstance();
      map.setStartingPosition(0, 0);
      map.setWrapperPrefix("");
      map.validate(true);

      Node node = Node.newString(Token.NAME, "symbol", 1, 1);
      node.putProp(Node.SOURCENAME_PROP, "file.js");
      node.putProp(Node.ORIGINALNAME_PROP, "origSymbol");

      map.addMapping(node, new FilePosition(0, 0), new FilePosition(0, 6));

      StringWriter out = new StringWriter();
      map.appendTo(out, "out.js");
      Assert.assertTrue(out.toString().length() > 0);

      map.reset();
    }
  }
}