package com.google.javascript.rhino;

import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.SimpleSourceFile;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class NodeTest {

  @Test
  public void testConstructorsAndBasics() {
    Node n0 = new Node(Token.BLOCK);
    Assert.assertEquals(Token.BLOCK, n0.getType());
    Assert.assertNull(n0.getParent());
    Assert.assertFalse(n0.hasChildren());
    Assert.assertEquals(-1, n0.getLineno());
    Assert.assertEquals(-1, n0.getCharno());

    Node c1 = new Node(Token.NAME);
    Node n1 = new Node(Token.EXPR_RESULT, c1);
    Assert.assertEquals(Token.EXPR_RESULT, n1.getType());
    Assert.assertTrue(n1.hasChildren());
    Assert.assertTrue(n1.hasOneChild());
    Assert.assertFalse(n1.hasMoreThanOneChild());
    Assert.assertSame(c1, n1.getFirstChild());
    Assert.assertSame(c1, n1.getLastChild());
    Assert.assertSame(n1, c1.getParent());

    Node c2_1 = new Node(Token.NAME);
    Node c2_2 = new Node(Token.NAME);
    Node n2 = new Node(Token.ASSIGN, c2_1, c2_2);
    Assert.assertEquals(2, n2.getChildCount());
    Assert.assertTrue(n2.hasMoreThanOneChild());
    Assert.assertSame(c2_1, n2.getFirstChild());
    Assert.assertSame(c2_2, n2.getLastChild());
    Assert.assertSame(c2_2, c2_1.getNext());

    Node c3_1 = new Node(Token.NAME);
    Node c3_2 = new Node(Token.NAME);
    Node c3_3 = new Node(Token.NAME);
    Node n3 = new Node(Token.HOOK, c3_1, c3_2, c3_3);
    Assert.assertEquals(3, n3.getChildCount());
    Assert.assertSame(c3_1, n3.getFirstChild());
    Assert.assertSame(c3_2, c3_1.getNext());
    Assert.assertSame(c3_3, c3_2.getNext());
    Assert.assertSame(c3_3, n3.getLastChild());

    Node c4_1 = new Node(Token.NAME);
    Node c4_2 = new Node(Token.NAME);
    Node c4_3 = new Node(Token.NAME);
    Node c4_4 = new Node(Token.NAME);
    Node n4 = new Node(Token.FOR, c4_1, c4_2, c4_3, c4_4);
    Assert.assertEquals(4, n4.getChildCount());
    Assert.assertSame(c4_4, n4.getLastChild());

    Node nLine = new Node(Token.TRUE, 10, 20);
    Assert.assertEquals(10, nLine.getLineno());
    Assert.assertEquals(20, nLine.getCharno());

    Node nLine1 = new Node(Token.EXPR_RESULT, new Node(Token.FALSE), 5, 12);
    Assert.assertEquals(5, nLine1.getLineno());
    Assert.assertEquals(12, nLine1.getCharno());

    Node nLine2 = new Node(Token.ASSIGN, new Node(Token.NAME), new Node(Token.NAME), 6, 13);
    Assert.assertEquals(6, nLine2.getLineno());
    Assert.assertEquals(13, nLine2.getCharno());

    Node nLine3 = new Node(Token.HOOK, new Node(Token.NAME), new Node(Token.NAME), new Node(Token.NAME), 7, 14);
    Assert.assertEquals(7, nLine3.getLineno());
    Assert.assertEquals(14, nLine3.getCharno());

    Node nLine4 = new Node(Token.FOR, new Node(Token.NAME), new Node(Token.NAME), new Node(Token.NAME), new Node(Token.NAME), 8, 15);
    Assert.assertEquals(8, nLine4.getLineno());
    Assert.assertEquals(15, nLine4.getCharno());
  }

  @Test
  public void testArrayConstructors() {
    Node[] emptyArr = new Node[0];
    Node nEmpty = new Node(Token.BLOCK, emptyArr);
    Assert.assertEquals(0, nEmpty.getChildCount());

    Node c1 = new Node(Token.NAME);
    Node c2 = new Node(Token.NAME);
    Node c3 = new Node(Token.NAME);
    Node[] arr = new Node[]{c1, c2, c3};
    Node nArr = new Node(Token.BLOCK, arr);
    Assert.assertEquals(3, nArr.getChildCount());
    Assert.assertSame(c1, nArr.getFirstChild());
    Assert.assertSame(c3, nArr.getLastChild());
    Assert.assertSame(nArr, c1.getParent());
    Assert.assertSame(nArr, c2.getParent());
    Assert.assertSame(nArr, c3.getParent());

    Node nArrLine = new Node(Token.BLOCK, new Node[]{new Node(Token.NULL)}, 2, 4);
    Assert.assertEquals(2, nArrLine.getLineno());
    Assert.assertEquals(4, nArrLine.getCharno());
    Assert.assertEquals(1, nArrLine.getChildCount());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayConstructorDuplicateChild() {
    Node c1 = new Node(Token.NAME);
    c1.next = new Node(Token.NAME);
    Node[] arr = new Node[]{c1, new Node(Token.NAME)};
    new Node(Token.BLOCK, arr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayConstructorLoopLast() {
    Node c1 = new Node(Token.NAME);
    Node c2 = new Node(Token.NAME);
    c2.next = c1;
    Node[] arr = new Node[]{c1, c2};
    new Node(Token.BLOCK, arr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingleChildConstructorAlreadyHasParent() {
    Node parent = new Node(Token.BLOCK);
    Node child = new Node(Token.NAME);
    parent.addChildToBack(child);
    new Node(Token.EXPR_RESULT, child);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSingleChildConstructorAlreadyHasSibling() {
    Node c1 = new Node(Token.NAME);
    c1.next = new Node(Token.NAME);
    new Node(Token.EXPR_RESULT, c1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTwoChildConstructorLeftHasParent() {
    Node parent = new Node(Token.BLOCK);
    Node c1 = new Node(Token.NAME);
    parent.addChildToBack(c1);
    new Node(Token.ASSIGN, c1, new Node(Token.NAME));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTwoChildConstructorRightHasParent() {
    Node parent = new Node(Token.BLOCK);
    Node c2 = new Node(Token.NAME);
    parent.addChildToBack(c2);
    new Node(Token.ASSIGN, new Node(Token.NAME), c2);
  }

  @Test
  public void testNumberNode() {
    Node num1 = Node.newNumber(42.5);
    Assert.assertEquals(Token.NUMBER, num1.getType());
    Assert.assertEquals(42.5, num1.getDouble(), 0.0);
    num1.setDouble(100.25);
    Assert.assertEquals(100.25, num1.getDouble(), 0.0);

    Node num2 = Node.newNumber(10.0, 15, 30);
    Assert.assertEquals(15, num2.getLineno());
    Assert.assertEquals(30, num2.getCharno());
    Assert.assertEquals(10.0, num2.getDouble(), 0.0);

    Node numZero = Node.newNumber(0.0);
    Node numNegZero = Node.newNumber(-0.0);
    Assert.assertFalse(numZero.isEquivalentTo(numNegZero));
    Assert.assertTrue(numZero.isEquivalentTo(Node.newNumber(0.0)));
    Assert.assertTrue(numNegZero.isEquivalentTo(Node.newNumber(-0.0)));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetDoubleOnNonNumberNode() {
    Node n = new Node(Token.NAME);
    n.getDouble();
  }

  @Test(expected = IllegalStateException.class)
  public void testGetDoubleOnWrongNumberNode() {
    Node n = new Node(Token.NUMBER);
    n.getDouble();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetDoubleOnNonNumberNode() {
    Node n = new Node(Token.NAME);
    n.setDouble(1.0);
  }

  @Test(expected = IllegalStateException.class)
  public void testSetDoubleOnWrongNumberNode() {
    Node n = new Node(Token.NUMBER);
    n.setDouble(1.0);
  }

  @Test
  public void testStringNode() {
    Node str1 = Node.newString("hello");
    Assert.assertEquals(Token.STRING, str1.getType());
    Assert.assertEquals("hello", str1.getString());
    str1.setString("world");
    Assert.assertEquals("world", str1.getString());

    Node str2 = Node.newString(Token.NAME, "foo");
    Assert.assertEquals(Token.NAME, str2.getType());
    Assert.assertEquals("foo", str2.getString());

    Node str3 = Node.newString("bar", 3, 5);
    Assert.assertEquals(3, str3.getLineno());
    Assert.assertEquals(5, str3.getCharno());
    Assert.assertEquals("bar", str3.getString());

    Node str4 = Node.newString(Token.STRING_KEY, "key", 4, 6);
    Assert.assertEquals(Token.STRING_KEY, str4.getType());
    Assert.assertEquals("key", str4.getString());
    Assert.assertEquals(4, str4.getLineno());
    Assert.assertEquals(6, str4.getCharno());

    Assert.assertFalse(str4.isQuotedString());
    str4.setQuotedString();
    Assert.assertTrue(str4.isQuotedString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStringNodeNull1() {
    Node.newString(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStringNodeNull2() {
    Node.newString(Token.STRING, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStringNodeNull3() {
    Node.newString(null, 1, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStringNodeNull4() {
    Node.newString(Token.STRING, null, 1, 1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStringNodeSetNull() {
    Node s = Node.newString("abc");
    s.setString(null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetStringOnNonString() {
    Node n = new Node(Token.BLOCK);
    n.getString();
  }

  @Test(expected = IllegalStateException.class)
  public void testGetStringOnFakeString() {
    Node n = new Node(Token.STRING);
    n.getString();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetStringOnNonString() {
    Node n = new Node(Token.BLOCK);
    n.setString("abc");
  }

  @Test(expected = IllegalStateException.class)
  public void testSetStringOnFakeString() {
    Node n = new Node(Token.STRING);
    n.setString("abc");
  }

  @Test(expected = IllegalStateException.class)
  public void testSetQuotedStringOnNonStringNode() {
    Node n = new Node(Token.BLOCK);
    Assert.assertFalse(n.isQuotedString());
    n.setQuotedString();
  }

  @Test
  public void testTreeManipulation() {
    Node parent = new Node(Token.BLOCK);
    Node c1 = Node.newString("a");
    Node c2 = Node.newString("b");
    Node c3 = Node.newString("c");

    parent.addChildToBack(c1);
    Assert.assertSame(c1, parent.getFirstChild());
    Assert.assertSame(c1, parent.getLastChild());

    parent.addChildToBack(c3);
    Assert.assertSame(c1, parent.getFirstChild());
    Assert.assertSame(c3, parent.getLastChild());

    parent.addChildBefore(c2, c3);
    Assert.assertSame(c2, c1.getNext());
    Assert.assertSame(c3, c2.getNext());

    Assert.assertNull(parent.getChildBefore(c1));
    Assert.assertSame(c1, parent.getChildBefore(c2));
    Assert.assertSame(c2, parent.getChildBefore(c3));

    Assert.assertSame(c1, parent.getChildAtIndex(0));
    Assert.assertSame(c2, parent.getChildAtIndex(1));
    Assert.assertSame(c3, parent.getChildAtIndex(2));

    Assert.assertEquals(0, parent.getIndexOfChild(c1));
    Assert.assertEquals(1, parent.getIndexOfChild(c2));
    Assert.assertEquals(2, parent.getIndexOfChild(c3));
    Assert.assertEquals(-1, parent.getIndexOfChild(Node.newString("none")));

    Assert.assertTrue(parent.hasChild(c1));
    Assert.assertTrue(parent.hasChild(c2));
    Assert.assertTrue(parent.hasChild(c3));
    Assert.assertFalse(parent.hasChild(Node.newString("none")));

    Node c0 = Node.newString("first");
    parent.addChildToFront(c0);
    Assert.assertSame(c0, parent.getFirstChild());
    Assert.assertSame(c1, c0.getNext());

    parent.addChildBefore(Node.newString("beforeFirst"), c0);
    Assert.assertEquals(5, parent.getChildCount());

    Node cAfter = Node.newString("afterFirst");
    parent.addChildAfter(cAfter, c0);
    Assert.assertSame(cAfter, c0.getNext());

    // replaceChild
    Node cReplace = Node.newString("replacedC1");
    parent.replaceChild(c1, cReplace);
    Assert.assertTrue(parent.hasChild(cReplace));
    Assert.assertFalse(parent.hasChild(c1));

    Node cReplaceFirst = Node.newString("replacedHead");
    parent.replaceChild(parent.getFirstChild(), cReplaceFirst);
    Assert.assertSame(cReplaceFirst, parent.getFirstChild());

    Node cReplaceLast = Node.newString("replacedTail");
    parent.replaceChild(parent.getLastChild(), cReplaceLast);
    Assert.assertSame(cReplaceLast, parent.getLastChild());

    // replaceChildAfter
    Node afterTarget = Node.newString("afterNew");
    parent.replaceChildAfter(c0, afterTarget);
    Assert.assertSame(afterTarget, c0.getNext());

    // removeChild
    parent.removeChild(parent.getFirstChild());
    parent.removeChild(parent.getLastChild());
    Assert.assertFalse(parent.hasChild(cReplaceLast));

    // removeFirstChild
    Node removed = parent.removeFirstChild();
    Assert.assertNotNull(removed);
    Assert.assertNull(removed.getParent());

    // removeChildAfter
    Node head = parent.getFirstChild();
    Node removedAfter = parent.removeChildAfter(head);
    Assert.assertNotNull(removedAfter);
    Assert.assertNull(removedAfter.getParent());

    // detachFromParent
    Node detachedChild = parent.getFirstChild();
    Assert.assertNotNull(detachedChild);
    detachedChild.detachFromParent();
    Assert.assertNull(detachedChild.getParent());
    Assert.assertFalse(parent.hasChild(detachedChild));

    // detachChildren
    Node nodeToDetach = new Node(Token.BLOCK, Node.newString("1"), Node.newString("2"));
    nodeToDetach.detachChildren();
    Assert.assertEquals(0, nodeToDetach.getChildCount());
    Assert.assertFalse(nodeToDetach.hasChildren());

    // removeChildren
    Node nodeToRemove = new Node(Token.BLOCK, Node.newString("1"), Node.newString("2"));
    Node chain = nodeToRemove.removeChildren();
    Assert.assertNotNull(chain);
    Assert.assertEquals(0, nodeToRemove.getChildCount());
    Assert.assertNull(chain.getParent());
  }

  @Test
  public void testAddChildren() {
    Node parent = new Node(Token.BLOCK);
    Node list1 = Node.newString("1");
    list1.next = Node.newString("2");

    parent.addChildrenToFront(list1);
    Assert.assertEquals(2, parent.getChildCount());
    Assert.assertEquals("1", parent.getFirstChild().getString());
    Assert.assertEquals("2", parent.getLastChild().getString());

    Node list2 = Node.newString("3");
    list2.next = Node.newString("4");
    parent.addChildrenToBack(list2);
    Assert.assertEquals(4, parent.getChildCount());
    Assert.assertEquals("4", parent.getLastChild().getString());

    Node emptyParent = new Node(Token.BLOCK);
    Node list3 = Node.newString("a");
    list3.next = Node.newString("b");
    emptyParent.addChildrenAfter(list3, null);
    Assert.assertEquals(2, emptyParent.getChildCount());
    Assert.assertEquals("a", emptyParent.getFirstChild().getString());
    Assert.assertEquals("b", emptyParent.getLastChild().getString());
  }

  @Test(expected = RuntimeException.class)
  public void testGetChildBeforeNotChild() {
    Node p = new Node(Token.BLOCK, Node.newString("a"));
    p.getChildBefore(Node.newString("b"));
  }

  @Test(expected = IllegalStateException.class)
  public void testDetachFromParentWithoutParent() {
    Node n = new Node(Token.BLOCK);
    n.detachFromParent();
  }

  @Test
  public void testProperties() {
    Node n = new Node(Token.BLOCK);
    Assert.assertNull(n.getProp(Node.ORIGINALNAME_PROP));
    Assert.assertEquals(0, n.getIntProp(Node.LENGTH));
    Assert.assertFalse(n.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));

    n.putProp(Node.ORIGINALNAME_PROP, "original");
    Assert.assertEquals("original", n.getProp(Node.ORIGINALNAME_PROP));

    n.putIntProp(Node.LENGTH, 42);
    Assert.assertEquals(42, n.getIntProp(Node.LENGTH));
    Assert.assertEquals(42, n.getExistingIntProp(Node.LENGTH));

    n.putBooleanProp(Node.SYNTHETIC_BLOCK_PROP, true);
    Assert.assertTrue(n.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));

    n.putIntProp(Node.CHANGE_TIME, 100);
    Assert.assertEquals(100, n.getChangeTime());
    n.setChangeTime(200);
    Assert.assertEquals(200, n.getChangeTime());

    n.setVarArgs(true);
    Assert.assertTrue(n.isVarArgs());
    n.setVarArgs(false);
    Assert.assertFalse(n.isVarArgs());

    n.setOptionalArg(true);
    Assert.assertTrue(n.isOptionalArg());
    n.setOptionalArg(false);
    Assert.assertFalse(n.isOptionalArg());

    n.setIsSyntheticBlock(true);
    Assert.assertTrue(n.isSyntheticBlock());

    n.setWasEmptyNode(true);
    Assert.assertTrue(n.wasEmptyNode());

    n.setLength(15);
    Assert.assertEquals(15, n.getLength());

    Set<String> directives = new HashSet<String>();
    directives.add("use strict");
    n.setDirectives(directives);
    Assert.assertEquals(directives, n.getDirectives());

    InputId inputId = new InputId("file.js");
    n.setInputId(inputId);
    Assert.assertSame(inputId, n.getInputId());

    SimpleSourceFile srcFile = new SimpleSourceFile("test.js", true);
    n.setStaticSourceFile(srcFile);
    Assert.assertSame(srcFile, n.getStaticSourceFile());
    Assert.assertEquals("test.js", n.getSourceFileName());
    Assert.assertTrue(n.isFromExterns());

    n.setSourceFileForTesting("nonExtern.js");
    Assert.assertEquals("nonExtern.js", n.getSourceFileName());
    Assert.assertFalse(n.isFromExterns());

    // Test removing properties
    n.removeProp(Node.ORIGINALNAME_PROP);
    Assert.assertNull(n.getProp(Node.ORIGINALNAME_PROP));
    n.removeProp(Node.LENGTH);
    Assert.assertEquals(0, n.getIntProp(Node.LENGTH));

    // PropListItem chain coverage
    Node n2 = new Node(Token.BLOCK);
    n2.putProp(Node.ORIGINALNAME_PROP, "test1");
    n2.putProp(Node.IS_CONSTANT_NAME, 1);
    n2.putProp(Node.IS_NAMESPACE, 1);
    n2.removeProp(Node.ORIGINALNAME_PROP);
    Assert.assertNull(n2.getProp(Node.ORIGINALNAME_PROP));
    Assert.assertTrue(n2.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetExistingIntPropThrowsWhenMissing() {
    Node n = new Node(Token.BLOCK);
    n.getExistingIntProp(Node.LENGTH);
  }

  @Test
  public void testPropListItemOperations() {
    Node n = new Node(Token.BLOCK);
    n.putProp(Node.ORIGINALNAME_PROP, "objVal");
    n.putIntProp(Node.LENGTH, 10);

    Node.PropListItem itemObj = n.lookupProperty(Node.ORIGINALNAME_PROP);
    Assert.assertNotNull(itemObj);
    Assert.assertEquals("objVal", itemObj.getObjectValue());
    try {
      itemObj.getIntValue();
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected
    }

    Node.PropListItem itemInt = n.lookupProperty(Node.LENGTH);
    Assert.assertNotNull(itemInt);
    Assert.assertEquals(10, itemInt.getIntValue());
    try {
      itemInt.getObjectValue();
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected
    }
  }

  @Test
  public void testClonePropsFrom() {
    Node src = new Node(Token.BLOCK);
    src.putProp(Node.ORIGINALNAME_PROP, "abc");
    src.putIntProp(Node.LENGTH, 5);

    Node dst = new Node(Token.BLOCK);
    dst.clonePropsFrom(src);
    Assert.assertEquals("abc", dst.getProp(Node.ORIGINALNAME_PROP));
    Assert.assertEquals(5, dst.getIntProp(Node.LENGTH));
  }

  @Test(expected = IllegalStateException.class)
  public void testClonePropsFromFailsWhenDstNotEmpty() {
    Node src = new Node(Token.BLOCK);
    src.putProp(Node.ORIGINALNAME_PROP, "abc");

    Node dst = new Node(Token.BLOCK);
    dst.putIntProp(Node.LENGTH, 1);
    dst.clonePropsFrom(src);
  }

  @Test
  public void testCloning() {
    Node root = new Node(Token.BLOCK);
    Node c1 = Node.newString("child1", 1, 2);
    Node c2 = Node.newNumber(3.14, 3, 4);
    root.addChildToBack(c1);
    root.addChildToBack(c2);
    root.putProp(Node.ORIGINALNAME_PROP, "rootNode");

    Node shallowClone = root.cloneNode();
    Assert.assertEquals(Token.BLOCK, shallowClone.getType());
    Assert.assertFalse(shallowClone.hasChildren());
    Assert.assertEquals("rootNode", shallowClone.getProp(Node.ORIGINALNAME_PROP));

    Node deepClone = root.cloneTree();
    Assert.assertEquals(2, deepClone.getChildCount());
    Assert.assertEquals("child1", deepClone.getFirstChild().getString());
    Assert.assertEquals(3.14, deepClone.getLastChild().getDouble(), 0.0);
    Assert.assertSame(deepClone, deepClone.getFirstChild().getParent());
    Assert.assertSame(deepClone, deepClone.getLastChild().getParent());
  }

  @Test
  public void testSourcePositions() {
    int encoded = Node.mergeLineCharNo(10, 20);
    Assert.assertEquals(10, Node.extractLineno(encoded));
    Assert.assertEquals(20, Node.extractCharno(encoded));

    Assert.assertEquals(-1, Node.mergeLineCharNo(-1, 5));
    Assert.assertEquals(-1, Node.mergeLineCharNo(5, -1));
    Assert.assertEquals(-1, Node.extractLineno(-1));
    Assert.assertEquals(-1, Node.extractCharno(-1));

    int hugeChar = Node.mergeLineCharNo(10, Node.MAX_COLUMN_NUMBER + 100);
    Assert.assertEquals(Node.MAX_COLUMN_NUMBER, Node.extractCharno(hugeChar));

    Node n = new Node(Token.NAME);
    n.setLineno(15);
    Assert.assertEquals(15, n.getLineno());
    Assert.assertEquals(0, n.getCharno());

    n.setCharno(8);
    Assert.assertEquals(15, n.getLineno());
    Assert.assertEquals(8, n.getCharno());

    n.setSourceEncodedPosition(12345);
    Assert.assertEquals(12345, n.getSourcePosition());

    Node parent = new Node(Token.BLOCK);
    Node child = new Node(Token.NAME);
    parent.addChildToBack(child);
    parent.setSourceEncodedPositionForTree(54321);
    Assert.assertEquals(54321, parent.getSourcePosition());
    Assert.assertEquals(54321, child.getSourcePosition());

    Assert.assertEquals(-1, n.getSourceOffset());
  }

  @Test
  public void testSourceInfoCopying() {
    Node src = Node.newString("src", 10, 20);
    src.putProp(Node.ORIGINALNAME_PROP, "origName");
    src.setSourceFileForTesting("src.js");

    Node dst = Node.newString("dst");
    dst.copyInformationFrom(src);
    Assert.assertEquals("origName", dst.getProp(Node.ORIGINALNAME_PROP));
    Assert.assertEquals("src.js", dst.getSourceFileName());
    Assert.assertEquals(10, dst.getLineno());
    Assert.assertEquals(20, dst.getCharno());

    Node dstTree = new Node(Token.BLOCK, Node.newString("child"));
    dstTree.copyInformationFromForTree(src);
    Assert.assertEquals("src.js", dstTree.getFirstChild().getSourceFileName());

    Node dstUse = Node.newString("use");
    dstUse.useSourceInfoFrom(src);
    Assert.assertEquals("origName", dstUse.getProp(Node.ORIGINALNAME_PROP));

    Node dstSrcref = Node.newString("ref");
    dstSrcref.srcref(src);
    Assert.assertEquals("src.js", dstSrcref.getSourceFileName());

    Node dstUseTree = new Node(Token.BLOCK, Node.newString("c"));
    dstUseTree.useSourceInfoFromForTree(src);
    Assert.assertEquals("origName", dstUseTree.getFirstChild().getProp(Node.ORIGINALNAME_PROP));

    Node dstSrcrefTree = new Node(Token.BLOCK, Node.newString("c2"));
    dstSrcrefTree.srcrefTree(src);
    Assert.assertEquals("origName", dstSrcrefTree.getFirstChild().getProp(Node.ORIGINALNAME_PROP));

    Node dstMissing = Node.newString("missing");
    dstMissing.putProp(Node.ORIGINALNAME_PROP, "keepMe");
    dstMissing.useSourceInfoIfMissingFrom(src);
    Assert.assertEquals("keepMe", dstMissing.getProp(Node.ORIGINALNAME_PROP));
    Assert.assertEquals("src.js", dstMissing.getSourceFileName());

    Node dstMissingTree = new Node(Token.BLOCK, Node.newString("c3"));
    dstMissingTree.useSourceInfoIfMissingFromForTree(src);
    Assert.assertEquals("origName", dstMissingTree.getFirstChild().getProp(Node.ORIGINALNAME_PROP));
  }

  @Test
  public void testIterators() {
    Node parent = new Node(Token.BLOCK);
    Assert.assertFalse(parent.children().iterator().hasNext());

    Node c1 = Node.newString("c1");
    Node c2 = Node.newString("c2");
    parent.addChildToBack(c1);
    parent.addChildToBack(c2);

    int count = 0;
    for (Node c : parent.children()) {
      Assert.assertNotNull(c);
      count++;
    }
    Assert.assertEquals(2, count);

    // Reuse iterator instance
    Iterable<Node> childrenIterable = parent.children();
    Iterator<Node> it1 = childrenIterable.iterator();
    Assert.assertTrue(it1.hasNext());
    Assert.assertSame(c1, it1.next());
    Assert.assertSame(c2, it1.next());
    Assert.assertFalse(it1.hasNext());
    try {
      it1.next();
      Assert.fail("Expected NoSuchElementException");
    } catch (NoSuchElementException e) {
      // Expected
    }

    try {
      it1.remove();
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected
    }

    Iterator<Node> it2 = childrenIterable.iterator();
    Assert.assertTrue(it2.hasNext());

    // siblings
    int sibCount = 0;
    for (Node sib : c1.siblings()) {
      Assert.assertNotNull(sib);
      sibCount++;
    }
    Assert.assertEquals(2, sibCount);

    // Ancestors
    Assert.assertSame(parent, c1.getAncestor(1));
    Assert.assertSame(c1, c1.getAncestor(0));
    Assert.assertNull(c1.getAncestor(2));

    Iterator<Node> ancIt = c1.getAncestors().iterator();
    Assert.assertTrue(ancIt.hasNext());
    Assert.assertSame(parent, ancIt.next());
    Assert.assertFalse(ancIt.hasNext());
    try {
      ancIt.next();
      Assert.fail("Expected NoSuchElementException");
    } catch (NoSuchElementException e) {
      // Expected
    }
    try {
      ancIt.remove();
      Assert.fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException e) {
      // Expected
    }
  }

  @Test
  public void testEquivalenceAndDiff() {
    Node n1 = Node.newString("foo");
    Node n2 = Node.newString("foo");
    Node n3 = Node.newString("bar");

    Assert.assertTrue(n1.isEquivalentTo(n2));
    Assert.assertFalse(n1.isEquivalentTo(n3));
    Assert.assertTrue(n1.isEquivalentToShallow(n2));
    Assert.assertTrue(n1.isEquivalentToTyped(n2));

    Node inc1 = new Node(Token.INC);
    inc1.putIntProp(Node.INCRDECR_PROP, Node.POST_FLAG);
    Node inc2 = new Node(Token.INC);
    inc2.putIntProp(Node.INCRDECR_PROP, Node.DECR_FLAG);
    Assert.assertFalse(inc1.isEquivalentTo(inc2));

    Node strKey1 = Node.newString(Token.STRING_KEY, "k");
    strKey1.setQuotedString();
    Node strKey2 = Node.newString(Token.STRING_KEY, "k");
    Assert.assertFalse(strKey1.isEquivalentTo(strKey2));

    Node call1 = new Node(Token.CALL);
    call1.putBooleanProp(Node.FREE_CALL, true);
    Node call2 = new Node(Token.CALL);
    Assert.assertFalse(call1.isEquivalentTo(call2));

    Node tree1 = new Node(Token.BLOCK, Node.newString("a"), Node.newString("b"));
    Node tree2 = new Node(Token.BLOCK, Node.newString("a"), Node.newString("b"));
    Node tree3 = new Node(Token.BLOCK, Node.newString("a"), Node.newString("c"));

    Assert.assertNull(tree1.checkTreeEquals(tree2));
    String diff = tree1.checkTreeEquals(tree3);
    Assert.assertNotNull(diff);
    Assert.assertTrue(diff.contains("Node tree inequality"));

    Node.NodeMismatch mismatch = tree1.checkTreeEqualsImpl(tree3);
    Assert.assertNotNull(mismatch);
    Assert.assertEquals(mismatch, new Node.NodeMismatch(mismatch.nodeA, mismatch.nodeB));
    Assert.assertEquals(mismatch.hashCode(), new Node.NodeMismatch(mismatch.nodeA, mismatch.nodeB).hashCode());
    Assert.assertNotEquals(mismatch, "differentType");
    Assert.assertNull(tree1.checkTreeTypeAwareEqualsImpl(tree2));
  }

  @Test
  public void testQualifiedName() {
    Node nameNode = Node.newString(Token.NAME, "a");
    Assert.assertTrue(nameNode.isQualifiedName());
    Assert.assertTrue(nameNode.isUnscopedQualifiedName());
    Assert.assertEquals("a", nameNode.getQualifiedName());

    Node emptyName = Node.newString(Token.NAME, "");
    Assert.assertFalse(emptyName.isQualifiedName());
    Assert.assertFalse(emptyName.isUnscopedQualifiedName());
    Assert.assertNull(emptyName.getQualifiedName());

    Node thisNode = new Node(Token.THIS);
    Assert.assertTrue(thisNode.isQualifiedName());
    Assert.assertFalse(thisNode.isUnscopedQualifiedName());
    Assert.assertEquals("this", thisNode.getQualifiedName());

    Node getProp1 = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b"));
    Assert.assertTrue(getProp1.isQualifiedName());
    Assert.assertTrue(getProp1.isUnscopedQualifiedName());
    Assert.assertEquals("a.b", getProp1.getQualifiedName());

    Node getPropThis = new Node(Token.GETPROP, new Node(Token.THIS), Node.newString(Token.STRING, "b"));
    Assert.assertTrue(getPropThis.isQualifiedName());
    Assert.assertFalse(getPropThis.isUnscopedQualifiedName());
    Assert.assertEquals("this.b", getPropThis.getQualifiedName());

    Node notQualified = new Node(Token.ARRAYLIT);
    Assert.assertFalse(notQualified.isQualifiedName());
    Assert.assertFalse(notQualified.isUnscopedQualifiedName());
    Assert.assertNull(notQualified.getQualifiedName());
  }

  @Test
  public void testSideEffects() {
    Node call = new Node(Token.CALL);
    Node.SideEffectFlags flags = new Node.SideEffectFlags();
    Assert.assertTrue(flags.areAllFlagsSet());

    flags.clearAllFlags();
    Assert.assertFalse(flags.areAllFlagsSet());

    flags.setAllFlags();
    Assert.assertTrue(flags.areAllFlagsSet());

    flags.clearSideEffectFlags();
    flags.setMutatesGlobalState();
    flags.setThrows();
    flags.setMutatesThis();
    flags.setMutatesArguments();
    flags.setReturnsTainted();

    call.setSideEffectFlags(flags);
    Assert.assertEquals(flags.valueOf(), call.getSideEffectFlags());

    call.setSideEffectFlags(Node.NO_SIDE_EFFECTS);
    Assert.assertTrue(call.isNoSideEffectsCall());
    Assert.assertFalse(call.mayMutateArguments());
    Assert.assertFalse(call.mayMutateGlobalStateOrThrow());

    call.setSideEffectFlags(Node.FLAG_LOCAL_RESULTS);
    Assert.assertTrue(call.isLocalResultCall());

    call.setSideEffectFlags(Node.FLAG_GLOBAL_STATE_UNMODIFIED | Node.FLAG_ARGUMENTS_UNMODIFIED | Node.FLAG_NO_THROWS);
    Assert.assertTrue(call.isOnlyModifiesThisCall());

    call.setSideEffectFlags(Node.FLAG_GLOBAL_STATE_UNMODIFIED | Node.FLAG_THIS_UNMODIFIED | Node.FLAG_NO_THROWS);
    Assert.assertTrue(call.isOnlyModifiesArgumentsCall());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSideEffectFlagsOnInvalidNode() {
    Node n = new Node(Token.BLOCK);
    n.setSideEffectFlags(0);
  }

  @Test
  public void testJSDocInfoAndBuilder() {
    Node n = new Node(Token.FUNCTION);
    Assert.assertNull(n.getJSDocInfo());

    JSDocInfo info = new JSDocInfo(false);
    n.setJSDocInfo(info);
    Assert.assertSame(info, n.getJSDocInfo());

    Node.FileLevelJsDocBuilder builder = n.getJsDocBuilderForNode();
    builder.append("License text");
    Assert.assertNotNull(n.getJSDocInfo());
    Assert.assertEquals("License text", n.getJSDocInfo().getLicense());

    builder.append(" second line");
    Assert.assertEquals("License text second line", n.getJSDocInfo().getLicense());

    Node n2 = new Node(Token.BLOCK);
    n2.addSuppression("checkTypes");
    Assert.assertNotNull(n2.getJSDocInfo());
    Assert.assertTrue(n2.getJSDocInfo().getSuppressions().contains("checkTypes"));
  }

  @Test
  public void testToStringAndToStringTree() throws IOException {
    Node nStr = Node.newString("hello", 1, 2);
    String s1 = nStr.toString();
    Assert.assertTrue(s1.contains("STRING hello"));
    Assert.assertTrue(s1.contains("1"));

    Node nNum = Node.newNumber(12.34, 5, 6);
    Assert.assertTrue(nNum.toString().contains("NUMBER 12.34"));

    Node fnValid = new Node(Token.FUNCTION, Node.newString(Token.NAME, "myFunc"));
    Assert.assertTrue(fnValid.toString().contains("FUNCTION myFunc"));

    Node fnInvalid = new Node(Token.FUNCTION);
    Assert.assertTrue(fnInvalid.toString().contains("FUNCTION <invalid>"));

    Node tree = new Node(Token.BLOCK, nStr, nNum);
    String treeStr = tree.toStringTree();
    Assert.assertTrue(treeStr.contains("BLOCK"));
    Assert.assertTrue(treeStr.contains("STRING hello"));
    Assert.assertTrue(treeStr.contains("NUMBER 12.34"));

    StringWriter sw = new StringWriter();
    tree.appendStringTree(sw);
    Assert.assertEquals(treeStr, sw.toString());
  }

  @Test
  public void testAstTypeChecks() {
    Assert.assertTrue(new Node(Token.ADD).isAdd());
    Assert.assertTrue(new Node(Token.AND).isAnd());
    Assert.assertTrue(new Node(Token.ARRAYLIT).isArrayLit());
    Assert.assertTrue(new Node(Token.ASSIGN).isAssign());
    Assert.assertTrue(new Node(Token.ASSIGN_ADD).isAssignAdd());
    Assert.assertTrue(new Node(Token.BLOCK).isBlock());
    Assert.assertTrue(new Node(Token.BREAK).isBreak());
    Assert.assertTrue(new Node(Token.CALL).isCall());
    Assert.assertTrue(new Node(Token.CASE).isCase());
    Assert.assertTrue(new Node(Token.CAST).isCast());
    Assert.assertTrue(new Node(Token.CATCH).isCatch());
    Assert.assertTrue(new Node(Token.COMMA).isComma());
    Assert.assertTrue(new Node(Token.CONTINUE).isContinue());
    Assert.assertTrue(new Node(Token.DEBUGGER).isDebugger());
    Assert.assertTrue(new Node(Token.DEC).isDec());
    Assert.assertTrue(new Node(Token.DEFAULT_CASE).isDefaultCase());
    Assert.assertTrue(new Node(Token.DELPROP).isDelProp());
    Assert.assertTrue(new Node(Token.DO).isDo());
    Assert.assertTrue(new Node(Token.EMPTY).isEmpty());
    Assert.assertTrue(new Node(Token.EXPR_RESULT).isExprResult());
    Assert.assertTrue(new Node(Token.FALSE).isFalse());
    Assert.assertTrue(new Node(Token.FOR).isFor());
    Assert.assertTrue(new Node(Token.FUNCTION).isFunction());
    Assert.assertTrue(new Node(Token.GETTER_DEF).isGetterDef());
    Assert.assertTrue(new Node(Token.GETELEM).isGetElem());
    Assert.assertTrue(new Node(Token.GETPROP).isGetProp());
    Assert.assertTrue(new Node(Token.HOOK).isHook());
    Assert.assertTrue(new Node(Token.IF).isIf());
    Assert.assertTrue(new Node(Token.IN).isIn());
    Assert.assertTrue(new Node(Token.INC).isInc());
    Assert.assertTrue(new Node(Token.INSTANCEOF).isInstanceOf());
    Assert.assertTrue(new Node(Token.LABEL).isLabel());
    Assert.assertTrue(new Node(Token.LABEL_NAME).isLabelName());
    Assert.assertTrue(Node.newString(Token.NAME, "a").isName());
    Assert.assertTrue(new Node(Token.NE).isNE());
    Assert.assertTrue(new Node(Token.NEW).isNew());
    Assert.assertTrue(new Node(Token.NOT).isNot());
    Assert.assertTrue(new Node(Token.NULL).isNull());
    Assert.assertTrue(Node.newNumber(1.0).isNumber());
    Assert.assertTrue(new Node(Token.OBJECTLIT).isObjectLit());
    Assert.assertTrue(new Node(Token.OR).isOr());
    Assert.assertTrue(new Node(Token.PARAM_LIST).isParamList());
    Assert.assertTrue(new Node(Token.REGEXP).isRegExp());
    Assert.assertTrue(new Node(Token.RETURN).isReturn());
    Assert.assertTrue(new Node(Token.SCRIPT).isScript());
    Assert.assertTrue(new Node(Token.SETTER_DEF).isSetterDef());
    Assert.assertTrue(Node.newString("s").isString());
    Assert.assertTrue(Node.newString(Token.STRING_KEY, "k").isStringKey());
    Assert.assertTrue(new Node(Token.SWITCH).isSwitch());
    Assert.assertTrue(new Node(Token.THIS).isThis());
    Assert.assertTrue(new Node(Token.THROW).isThrow());
    Assert.assertTrue(new Node(Token.TRUE).isTrue());
    Assert.assertTrue(new Node(Token.TRY).isTry());
    Assert.assertTrue(new Node(Token.TYPEOF).isTypeOf());
    Assert.assertTrue(new Node(Token.VAR).isVar());
    Assert.assertTrue(new Node(Token.VOID).isVoid());
    Assert.assertTrue(new Node(Token.WHILE).isWhile());
    Assert.assertTrue(new Node(Token.WITH).isWith());

    Node n = new Node(Token.BLOCK);
    Assert.assertFalse(n.isAdd());
    Assert.assertFalse(n.isName());
    Assert.assertFalse(n.isNumber());
    Assert.assertFalse(n.isString());
    n.setType(Token.VAR);
    Assert.assertTrue(n.isVar());
  }

  @Test
  public void testJSType() {
    Node n = new Node(Token.BLOCK);
    Assert.assertNull(n.getJSType());
    n.setJSType(null);
    Assert.assertNull(n.getJSType());
  }
}