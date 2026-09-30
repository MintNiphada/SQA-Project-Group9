package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InlineCostEstimatorTest {

  private static Node parse(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    return root.getFirstChild();
  }

  @Test
  public void testEstimatedIdentifierCostConstant() {
    assertEquals(2, InlineCostEstimator.ESTIMATED_IDENTIFIER_COST);
  }

  @Test
  public void testNumberCost() {
    Node node = Node.newNumber(5);
    assertEquals(1, InlineCostEstimator.getCost(node));

    Node nodeLarge = Node.newNumber(12345);
    assertEquals(5, InlineCostEstimator.getCost(nodeLarge));
  }

  @Test
  public void testIdentifierCost() {
    Node nodeShort = Node.newString(Token.NAME, "x");
    assertEquals(InlineCostEstimator.ESTIMATED_IDENTIFIER_COST, InlineCostEstimator.getCost(nodeShort));

    Node nodeLong = Node.newString(Token.NAME, "aVeryLongIdentifierName");
    assertEquals(InlineCostEstimator.ESTIMATED_IDENTIFIER_COST, InlineCostEstimator.getCost(nodeLong));
  }

  @Test
  public void testStringCost() {
    Node node = Node.newString("hello");
    // "hello" formatted as string literal: "\"hello\"" -> length 7
    assertEquals(7, InlineCostEstimator.getCost(node));

    Node emptyStr = Node.newString("");
    // "\"\"" -> length 2
    assertEquals(2, InlineCostEstimator.getCost(emptyStr));
  }

  @Test
  public void testBooleanAndNullCost() {
    Node trueNode = new Node(Token.TRUE);
    assertEquals(4, InlineCostEstimator.getCost(trueNode));

    Node falseNode = new Node(Token.FALSE);
    assertEquals(5, InlineCostEstimator.getCost(falseNode));

    Node nullNode = new Node(Token.NULL);
    assertEquals(4, InlineCostEstimator.getCost(nullNode));
  }

  @Test
  public void testBinaryExpressionCost() {
    Node expr = parse("a + b;");
    int cost = InlineCostEstimator.getCost(expr);
    assertTrue(cost > 0);
  }

  @Test
  public void testFunctionCost() {
    Node fn = parse("function foo(x) { return x + 1; }");
    int cost = InlineCostEstimator.getCost(fn);
    assertTrue(cost > 10);
  }

  @Test
  public void testCostWithThreshold() {
    Node expr = parse("var a = 1 + 2 + 3 + 4 + 5 + 6 + 7 + 8 + 9;");
    int fullCost = InlineCostEstimator.getCost(expr);

    int threshold = 5;
    int limitedCost = InlineCostEstimator.getCost(expr, threshold);

    assertTrue(limitedCost >= threshold);
    assertTrue(limitedCost <= fullCost);
  }

  @Test
  public void testCostWithZeroThreshold() {
    Node expr = parse("x = 1;");
    int limitedCost = InlineCostEstimator.getCost(expr, 0);
    assertTrue(limitedCost >= 0);
  }

  @Test
  public void testCostWithHighThreshold() {
    Node expr = parse("x = 1;");
    int normalCost = InlineCostEstimator.getCost(expr);
    int highCost = InlineCostEstimator.getCost(expr, Integer.MAX_VALUE);
    assertEquals(normalCost, highCost);
  }

  @Test
  public void testArrayLiteralCost() {
    Node arr = parse("[1, 2, 3];");
    int cost = InlineCostEstimator.getCost(arr);
    assertTrue(cost > 0);
  }

  @Test
  public void testObjectLiteralCost() {
    Node obj = parse("({a: 1, b: 2});");
    int cost = InlineCostEstimator.getCost(obj);
    assertTrue(cost > 0);
  }
}