package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SourceMapTest {

  private SourceMap sourceMap;

  @Before
  public void setUp() {
    sourceMap = new SourceMap();
  }

  private Node createNode(String file, int line, int col, String origName) {
    Node node = new Node(Token.NAME);
    if (file != null) {
      node.putProp(Node.SOURCEFILE_PROP, file);
    }
    node.setLineno(line);
    node.setCharno(col);
    if (origName != null) {
      node.putProp(Node.ORIGINALNAME_PROP, origName);
    }
    return node;
  }

  @Test
  public void testMappingAppendTo() throws IOException {
    SourceMap.Mapping mapping = new SourceMap.Mapping();
    mapping.sourceFile = "\"foo.js\"";
    mapping.originalPosition = new Position(10, 20);
    mapping.originalName = "\"bar\"";

    StringBuilder sb = new StringBuilder();
    mapping.appendTo(sb);
    Assert.assertEquals("[\"foo.js\",10,20,\"bar\"]", sb.toString());

    mapping.originalName = null;
    sb = new StringBuilder();
    mapping.appendTo(sb);
    Assert.assertEquals("[\"foo.js\",10,20]", sb.toString());
  }

  @Test
  public void testAddMappingIgnoredWhenNoSourceFile() throws IOException {
    Node node = createNode(null, 1, 0, null);
    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 5));

    StringBuilder sb = new StringBuilder();
    try {
      sourceMap.appendTo(sb, "out.js");
      Assert.fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testAddMappingIgnoredWhenLineNegative() {
    Node node = createNode("foo.js", -1, 0, null);
    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 5));

    StringBuilder sb = new StringBuilder();
    try {
      sourceMap.appendTo(sb, "out.js");
      Assert.fail();
    } catch (IllegalStateException expected) {
    }
  }

  @Test
  public void testBasicMappingOutput() throws IOException {
    Node node = createNode("test.js", 1, 2, "myVar");
    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 3));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("/** Begin line maps. **/{ \"file\" : \"out.js\", \"count\": 1 }\n"));
    Assert.assertTrue(output.contains("[0,0,0]\n"));
    Assert.assertTrue(output.contains("/** Begin file information. **/\n[]\n"));
    Assert.assertTrue(output.contains("/** Begin mapping definitions. **/\n"));
    Assert.assertTrue(output.contains("[\"test.js\",1,2,\"myVar\"]\n"));
  }

  @Test
  public void testMultipleSourceFilesCaching() throws IOException {
    Node node1 = createNode("a.js", 1, 0, null);
    Node node2 = createNode("a.js", 2, 0, null);
    Node node3 = createNode("b.js", 1, 0, null);

    sourceMap.addMapping(node1, new Position(0, 0), new Position(0, 2));
    sourceMap.addMapping(node2, new Position(0, 2), new Position(0, 4));
    sourceMap.addMapping(node3, new Position(0, 4), new Position(0, 6));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("[0,0,1,1,2,2]\n"));
    Assert.assertTrue(output.contains("[\"a.js\",1,0]\n"));
    Assert.assertTrue(output.contains("[\"a.js\",2,0]\n"));
    Assert.assertTrue(output.contains("[\"b.js\",1,0]\n"));
  }

  @Test
  public void testStartingPositionOffset() throws IOException {
    sourceMap.setStartingPosition(1, 5);

    Node node1 = createNode("test.js", 1, 0, null);
    sourceMap.addMapping(node1, new Position(0, 0), new Position(0, 2));

    Node node2 = createNode("test.js", 2, 0, null);
    sourceMap.addMapping(node2, new Position(1, 0), new Position(1, 3));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("count\": 3"));
    Assert.assertTrue(output.contains("[-1,-1,-1,-1,-1,0,0]\n"));
  }

  @Test
  public void testSetWrapperPrefixSingleLine() throws IOException {
    sourceMap.setWrapperPrefix("(function(){");
    Node node = createNode("test.js", 1, 0, null);
    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 3));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("[-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,0]\n"));
  }

  @Test
  public void testSetWrapperPrefixMultiLine() throws IOException {
    sourceMap.setWrapperPrefix("/* comment */\nvar x = ");
    Node node = createNode("test.js", 1, 0, null);
    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 3));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("count\": 2"));
  }

  @Test
  public void testReset() throws IOException {
    Node node = createNode("test.js", 1, 0, null);
    sourceMap.addMapping(node, new Position(0, 0), new Position(0, 3));
    sourceMap.setStartingPosition(2, 2);
    sourceMap.setWrapperPrefix("abc\n");

    sourceMap.reset();

    Node node2 = createNode("other.js", 1, 0, null);
    sourceMap.addMapping(node2, new Position(0, 0), new Position(0, 2));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("count\": 1"));
    Assert.assertTrue(output.contains("[0,0]\n"));
    Assert.assertFalse(output.contains("test.js"));
    Assert.assertTrue(output.contains("other.js"));
  }

  @Test
  public void testNestedMappings() throws IOException {
    Node parentNode = createNode("test.js", 1, 0, null);
    Node childNode = createNode("test.js", 1, 3, null);

    sourceMap.addMapping(parentNode, new Position(0, 0), new Position(0, 10));
    sourceMap.addMapping(childNode, new Position(0, 2), new Position(0, 6));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("[0,0,1,1,1,1,0,0,0,0]\n"));
  }

  @Test
  public void testGapsBetweenMappings() throws IOException {
    Node node1 = createNode("test.js", 1, 0, null);
    Node node2 = createNode("test.js", 1, 5, null);

    sourceMap.addMapping(node1, new Position(0, 0), new Position(0, 2));
    sourceMap.addMapping(node2, new Position(0, 5), new Position(0, 7));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("[0,0,-1,-1,-1,1,1]\n"));
  }

  @Test
  public void testMultiLineMappings() throws IOException {
    Node node1 = createNode("test.js", 1, 0, null);
    Node node2 = createNode("test.js", 2, 0, null);

    sourceMap.addMapping(node1, new Position(0, 0), new Position(1, 2));
    sourceMap.addMapping(node2, new Position(1, 2), new Position(1, 5));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("count\": 2"));
  }

  @Test
  public void testConsecutiveOverlappingAndClosedMappings() throws IOException {
    Node n1 = createNode("test.js", 1, 0, null);
    Node n2 = createNode("test.js", 1, 2, null);
    Node n3 = createNode("test.js", 1, 7, null);

    sourceMap.addMapping(n1, new Position(0, 0), new Position(0, 6));
    sourceMap.addMapping(n2, new Position(0, 2), new Position(0, 4));
    sourceMap.addMapping(n3, new Position(0, 7), new Position(0, 9));

    StringBuilder sb = new StringBuilder();
    sourceMap.appendTo(sb, "out.js");
    String output = sb.toString();

    Assert.assertTrue(output.contains("[0,0,1,1,0,0,-1,2,2]\n"));
  }
}
