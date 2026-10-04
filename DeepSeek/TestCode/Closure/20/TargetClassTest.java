package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link PeepholeSubstituteAlternateSyntax}.
 */
public class PeepholeSubstituteAlternateSyntaxTest {

  private PeepholeSubstituteAlternateSyntax peepholeNotLate;
  private PeepholeSubstituteAlternateSyntax peepholeLate;

  @Before
  public void setUp() throws Exception {
    peepholeNotLate = new PeepholeSubstituteAlternateSyntax(false);
    peepholeLate = new PeepholeSubstituteAlternateSyntax(true);
    // Set a default coding convention; some tests override this if needed.
    peepholeNotLate.setCodingConvention(new DefaultCodingConvention());
    peepholeLate.setCodingConvention(new DefaultCodingConvention());
  }

  /**
   * Helper to apply optimizeSubtree and return the result; replaces the given node in its parent.
   */
  private Node optimize(Node n, boolean late) {
    PeepholeSubstituteAlternateSyntax instance = late ? peepholeLate : peepholeNotLate;
    // The optimizeSubtree method modifies the tree in place and returns the new root (or same).
    return instance.optimizeSubtree(n);
  }

  // ------------------------- Tests for tryReduceReturn ----------------

  @Test
  public void testReduceReturnVoid0() {
    // return void 0 -> return;
    Node script = IR.script();
    Node ret = IR.returnNode(IR.voidNode(IR.number(0)));
    script.addChildToBack(ret);
    optimize(ret, false);
    assertFalse("void 0 should be removed", ret.hasChildren());
    script = IR.script();
    ret = IR.returnNode(IR.voidNode(IR.number(0));
    script.addChildToBack(ret);
    optimize(ret, true);
    assertFalse(ret.hasChildren());
  }

  @Test
  public void testReduceReturnUndefined() {
    // return undefined -> return;
    Node script = IR.script();
    Node ret = IR.returnNode(IR.name("undefined"));
    script.addChildToBack(ret);
    optimize(ret, false);
    assertFalse(ret.hasChildren());
  }

  @Test
  public void testReduceReturnNotRemovedIfSideEffects() {
    // return void 0 where operand may have side effects (e.g., call) should not remove.
    Node script = IR.script();
    Node ret = IR.returnNode(IR.voidNode(IR.call(IR.name("foo"))));
    script.addChildToBack(ret);
    Node result = optimize(ret, false);
    assertTrue(result.hasChildren());
  }

  @Test
  public void testReduceReturnWithOtherOp() {
    // return 42 should stay unchanged.
    Node script = IR.script();
    Node ret = IR.returnNode(IR.number(42));
    script.addChildToBack(ret);
    Node result = optimize(ret, false);
    assertTrue(result.hasChildren());
    assertEquals(42.0, result.getFirstChild().getDouble(), 0.0);
  }

  // --------------- Tests for tryRemoveRedundantExit and tryReplaceExitWithBreak ------

  @Test
  public void testRemoveRedundantReturn() {
    // if (a) { return 1 } return 1  -> remove first return.
    Node script = IR.script();
    Node iff = IR.ifNode(IR.name("a"),
        IR.block(IR.returnNode(IR.number(1))));
    script.addChildToBack(iff);
    script.addChildToBack(IR.returnNode(IR.number(1)));

    // We apply optimization on the first return inside the if.
    Node firstRet = iff.getLastChild().getFirstChild();
    Node result = optimize(firstRet, false);
    // result should be null because it was removed.
    assertNull(result);
    // The script should now have: if(a) {} return 1
    assertTrue(script.hasChildren());
    assertEquals(2, script.getChildCount());
    Node ifBlock = iff.getLastChild();
    assertFalse(ifBlock.hasChildren()); // empty block
    assertNotNull(script.getLastChild());
    assertTrue(script.getLastChild().isReturn());
  }

  @Test
  public void testRemoveRedundantThrow() {
    // if (a) { throw 'e' } throw 'e' -> remove first throw.
    Node script = IR.script();
    Node iff = IR.ifNode(IR.name("a"),
        IR.block(IR.throwNode(IR.string("e"))));
    script.addChildToBack(iff);
    script.addChildToBack(IR.throwNode(IR.string("e")));
    Node firstThrow = iff.getLastChild().getFirstChild();
    Node result = optimize(firstThrow, false);
    assertNull(result);
    assertFalse(iff.getLastChild().hasChildren());
  }

  @Test
  public void testReplaceExitWithBreak() {
    // while (true) { return 1 } return 1 -> while(true) { break } return 1
    Node script = IR.script();
    Node whileNode = IR.whileNode(IR.trueNode(),
        IR.block(IR.returnNode(IR.number(1))));
    script.addChildToBack(whileNode);
    script.addChildToBack(IR.returnNode(IR.number(1)));

    Node retInside = whileNode.getLastChild().getFirstChild();
    Node result = optimize(retInside, false);
    // Should replace with break
    assertTrue(result.isBreak());
    assertEquals(Token.BREAK, result.getType());
    // The outer return remains.
    assertTrue(script.getLastChild().isReturn());
  }

  // ------ Tests for tryMinimizeNot -----

  @Test
  public void testMinimizeNotEq() {
    // !(x==y) -> x!=y
    Node script = IR.script();
    Node not = IR.not(IR.eq(IR.name("x"), IR.name("y")));
    script.addChildToBack(not);
    Node result = optimize(not, false);
    assertTrue(result.isNE());
    assertEquals(Token.NE, result.getType());
  }

  @Test
  public void testMinimizeNotNe() {
    // !(x!=y) -> x==y
    Node not = IR.not(IR.ne(IR.name("x"), IR.name("y")));
    Node result = optimize(not, false);
    assertTrue(result.isEQ());
  }

  @Test
  public void testMinimizeNotSheq() {
    // !(x===y) -> x!==y
    Node not = IR.not(IR.sheq(IR.name("x"), IR.name("y")));
    Node result = optimize(not, false);
    assertTrue(result.isSHNE());
  }

  @Test
  public void testMinimizeNotShne() {
    // !(x!==y) -> x===y
    Node not = IR.not(IR.shne(IR.name("x"), IR.name("y")));
    Node result = optimize(not, false);
    assertTrue(result.isSHEQ());
  }

  @Test
  public void testMinimizeNotIrreducible() {
    // !(a>b) stays unchanged.
    Node not = IR.not(IR.gt(IR.name("a"), IR.name("b")));
    Node result = optimize(not, false);
    assertSame(not, result);
  }

  // --------- Tests for tryMinimizeIf ----------

  @Test
  public void testIfWithBooleanLiteralCondition() {
    // if (true) foo() -> stays (handled by other passes? code doesn't change.
    Node ifNode = IR.ifNode(IR.trueNode(), IR.block(IR.exprResult(IR.call(IR.name("foo"))));
    // No change.
    Node result = optimize(ifNode, false);
    assertSame(ifNode, result);
  }

  @Test
  public void testIfToAnd() {
    // if(x) foo() -> x && foo()
    Node expr = IR.exprResult(IR.call(IR.name("foo")));
    Node ifNode = IR.ifNode(IR.name("x"), IR.block(expr));
    Node parent = IR.script();
    parent.addChildToBack(ifNode);
    Node result = optimize(ifNode, false);
    // result should be an EXPR_RESULT with AND inside
    assertTrue(result.isExprResult());
    Node and = result.getFirstChild();
    assertTrue(and.isAND());
    assertEquals("x", and.getFirstChild().getString());
    assertEquals("foo", and.getLastChild().getFirstChild().getString());
  }

  @Test
  public void testIfWithNotToOr() {
    // if(!x) bar() -> x || bar()
    Node not = IR.not(IR.name("x"));
    Node ifNode = IR.ifNode(not, IR.block(IR.exprResult(IR.call(IR.name("bar"))));
    Node parent = IR.script();
    parent.addChildToBack(ifNode);
    Node result = optimize(ifNode, false);
    assertTrue(result.isExprResult());
    Node or = result.getFirstChild();
    assertTrue(or.isOR());
    assertEquals("x", or.getFirstChild().getString());
    assertEquals("bar", or.getLastChild().getFirstChild().getString());
  }

  @Test
  public void testIfReturnToHook() {
    // if(x) return 1; else return 2 -> return x ? 1 : 2
    Node ifNode = IR.ifNode(
        IR.name("x"),
        IR.block(IR.returnNode(IR.number(1))),
        IR.block(IR.returnNode(IR.number(2))));
    Node parent = IR.script();
    parent.addChildToBack(ifNode);
    Node result = optimize(ifNode, false);
    assertTrue(result.isReturn());
    Node hook = result.getFirstChild();
    assertTrue(hook.isHOOK());
    assertEquals("x", hook.getFirstChild().getString());
    assertEquals(1.0, hook.getChildAtIndex(1).getDouble(), 0.0);
    assertEquals(2.0, hook.getLastChild().getDouble(), 0.0);
  }

  @Test
  public void testIfAssignToHook() {
    // if(x) a=1; else a=2 -> a = x ? 1 : 2
    Node assign1 = IR.assign(IR.name("a"), IR.number(1));
    Node assign2 = IR.assign(IR.name("a"), IR.number(2));
    Node ifNode = IR.ifNode(
        IR.name("x"),
        IR.block(IR.exprResult(assign1)),
        IR.block(IR.exprResult(assign2)));
    Node parent = IR.script();
    parent.addChildToBack(ifNode);
    Node result = optimize(ifNode, false);
    // result is an EXPR_RESULT with an ASSIGN containing a HOOK.
    assertTrue(result.isExprResult());
    Node assign = result.getFirstChild();
    assertTrue(assign.isAssign());
    assertEquals("a", assign.getFirstChild().getString());
    Node hook = assign.getLastChild();
    assertTrue(hook.isHOOK());
    assertEquals("x", hook.getFirstChild().getString());
    assertEquals(1.0, hook.getChildAtIndex(1).getDouble(), 0.0);
    assertEquals(2.0, hook.getLastChild().getDouble(), 0.0);
  }

  @Test
  public void testIfVarToHook() {
    // if(x) var y=1; else y=2 -> var y = x ? 1 : 2
    Node varDecl = IR.var(IR.name("y"), IR.number(1));
    Node assign = IR.assign(IR.name("y"), IR.number(2));
    Node ifNode = IR.ifNode(
        IR.name("x"),
        IR.block(varDecl),
        IR.block(IR.exprResult(assign)));
    Node parent = IR.script();
    parent.addChildToBack(ifNode);
    Node result = optimize(ifNode, false);
    assertTrue(result.isVar());
    Node nameNode = result.getFirstChild();
    assertTrue(nameNode.hasChildren());
    Node hook = nameNode.getFirstChild();
    assertTrue(hook.isHOOK());
  }

  @Test
  public void testIfVarToHookElseBranchIsVar() {
    // if(x) y=1; else var y=2 -> var y = x ? 1 : 2
    Node assign = IR.assign(IR.name("y"), IR.number(1));
    Node varDecl = IR.var(IR.name("y"), IR.number(2));
    Node ifNode = IR.ifNode(
        IR.name("x"),
        IR.block(IR.exprResult(assign)),
        IR.block(varDecl));
    Node parent = IR.script();
    parent.addChildToBack(ifNode);
    Node result = optimize(ifNode, false);
    assertTrue(result.isVar());
    Node nameNode = result.getFirstChild();
    assertTrue(nameNode.hasChildren());
    Node hook = nameNode.getFirstChild();
    assertTrue(hook.isHOOK());
  }

  @Test
  public void testRemoveRepeatedStatements() {
    // if (a) { x=1; return true } else { x=2; return true } -> return true moved outside.
    Node ifNode = IR.ifNode(
        IR.name("a"),
        IR.block(
            IR.exprResult(IR.assign(IR.name("x"), IR.number(1))),
            IR.returnNode(IR.trueNode())),
        IR.block(
            IR.exprResult(IR.assign(IR.name("x"), IR.number(2))),
            IR.returnNode(IR.trueNode())));
    Node parent = IR.block();
    parent.addChildToBack(ifNode);
    // optimize the if node
    Node result = optimize(ifNode, false);
    // if node remains, but the repeated return true should be appended after the if.
    assertSame(ifNode, result);
    assertTrue(parent.getLastChild().isReturn());
    assertTrue(parent.getLastChild().getFirstChild().isTrue());
  }

  @Test
  public void testCombineIfsWithOr() {
    // if (x) return 1; if (y) return 1; -> if (x||y) return 1;
    Node block = IR.block();
    block.addChildToBack(IR.ifNode(
        IR.name("x"),
        IR.block(IR.returnNode(IR.number(1))));
    block.addChildToBack(IR.ifNode(
        IR.name("y"),
        IR.block(IR.returnNode(IR.number(1))));
    Node parent = IR.script();
    parent.addChildToBack(block);
    // optimize the block
    Node result = optimize(block, false);
    // block now should have a single if with OR condition.
    assertEquals(1, block.getChildCount());
    Node ifSt = block.getFirstChild();
    assertTrue(ifSt.isIf());
    Node cond = ifSt.getFirstChild();
    assertTrue(cond.isOR());
    assertEquals("x", cond.getFirstChild().getString());
    assertEquals("y", cond.getLastChild().getString());
  }

  @Test
  public void testIfElseExpressionToHook() {
    // if(x) foo(); else bar() -> x ? foo() : bar()
    Node expr1 = IR.exprResult(IR.call(IR.name("foo")));
    Node expr2 = IR.exprResult(IR.call(IR.name("bar")));
    Node ifNode = IR.ifNode(IR.name("x"), IR.block(expr1), IR.block(expr2));
    Node parent = IR.script();
    parent.addChildToBack(ifNode);
    Node result = optimize(ifNode, false);
    assertTrue(result.isExprResult());
    Node hook = result.getFirstChild();
    assertTrue(hook.isHOOK());
    assertEquals("x", hook.getFirstChild().getString());
    assertTrue(hook.getChildAtIndex(1).isCall());
    assertTrue(hook.getChildAtIndex(2).isCall());
  }

  // ------- Tests for tryFoldStandardConstructors -----

  @Test
  public void testFoldNewObjectToCall() {
    // new Object() -> Object() (after normalizatoin)
    Node newObj = IR.newNode(IR.name("Object"));
    // AST normalized is true by default? We assume it is for test.
    Node result = optimize(newObj, false);
    assertTrue(result.isCall());
    assertTrue(result.getBooleanProp(Node.FREE_CALL));
  }

  @Test
  public void testFoldNewArrayToCall() {
    Node newArr = IR.newNode(IR.name("Array"));
    Node result = optimize(newArr, false);
    assertTrue(result.isCall());
  }

  // --------- Tests for tryFoldLiteralConstructor --------

  @Test
  public void testFoldObjectConstructorToObjectLit() {
    // Object() -> {}
    Node call = IR.call(IR.name("Object"));
    Node result = optimize(call, false);
    assertTrue(result.isObjectLit());
    assertFalse(result.hasChildren());
  }

  @Test
  public void testFoldArrayConstructorNoArgs() {
    // Array() -> []
    Node call = IR.call(IR.name("Array"));
    Node result = optimize(call, false);
    assertTrue(result.isArrayLit());
    assertFalse(result.hasChildren());
  }

  @Test
  public void testFoldArrayConstructorMultipleArgs() {
    // Array(1,2) -> [1,2]
    Node call = IR.call(IR.name("Array"), IR.number(1), IR.number(2));
    Node result = optimize(call, false);
    assertTrue(result.isArrayLit());
    assertEquals(2, result.getChildCount());
    assertEquals(1.0, result.getFirstChild().getDouble(), 0.0);
    assertEquals(2.0, result.getLastChild().getDouble(), 0.0);
  }

  @Test
  public void testFoldArrayConstructorStringArg() {
    // Array("a") -> ["a"]
    Node call = IR.call(IR.name("Array"), IR.string("a"));
    Node result = optimize(call, false);
    assertTrue(result.isArrayLit());
    assertEquals(1, result.getChildCount());
    assertEquals("a", result.getFirstChild().getString());
  }

  @Test
  public void testFoldArrayConstructorNumberZero() {
    // Array(0) -> []
    Node call = IR.call(IR.name("Array"), IR.number(0));
    Node result = optimize(call, false);
    assertTrue(result.isArrayLit());
    assertFalse(result.hasChildren());
  }

  @Test
  public void testFoldArrayConstructorNumberNonZeroNotFolded() {
    // Array(5) stays as call because unsafe.
    Node call = IR.call(IR.name("Array"), IR.number(5));
    Node result = optimize(call, false);
    assertTrue(result.isCall());
  }

  // ------ Tests for tryFoldRegularExpressionConstructor ------

  @Test
  public void testFoldRegExpSimple() {
    // new RegExp("abc") -> /abc/
    Node call = IR.call(IR.name("RegExp"), IR.string("abc"));
    Node result = optimize(call, false);
    assertTrue(result.isRegExp());
    assertEquals("abc", result.getFirstChild().getString());
    assertFalse(result.hasChildren()); // no flags
  }

  @Test
  public void testFoldRegExpWithFlags() {
    // new RegExp("abc","gi") -> /abc/gi (safe flags in ES5+)
    Node call = IR.call(IR.name("RegExp"), IR.string("abc"), IR.string("gi"));
    Node result = optimize(call, false);
    assertTrue(result.isRegExp());
    assertEquals("abc", result.getFirstChild().getString());
    assertEquals("gi", result.getChildAtIndex(1).getString());
  }

  @Test
  public void testFoldRegExpEmptyPatternNotFolded() {
    Node call = IR.call(IR.name("RegExp"), IR.string(""));
    Node result = optimize(call, false);
    assertTrue(result.isCall()); // not folded
  }

  @Test
  public void testFoldRegExpTooLongPatternNotFolded() {
    // pattern > 100 chars should not be folded.
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 101; i++) {
      sb.append('a');
    }
    Node call = IR.call(IR.name("RegExp"), IR.string(sb.toString()));
    Node result = optimize(call, false);
    assertTrue(result.isCall());
  }

  // -------- Tests for tryFoldSimpleFunctionCall ----

  @Test
  public void testFoldStringConstructor() {
    // String(42) -> ''+42
    Node call = IR.call(IR.name("String"), IR.number(42));
    Node result = optimize(call, false);
    assertTrue(result.isAdd());
    Node left = result.getFirstChild();
    assertTrue(left.isString());
    assertEquals("", left.getString());
    Node right = result.getLastChild();
    assertEquals(42.0, right.getDouble(), 0.0);
  }

  @Test
  public void testFoldStringConstructorNoArgsNotFolded() {
    Node call = IR.call(IR.name("String"));
    Node result = optimize(call, false);
    assertTrue(result.isCall()); // unchanged
  }

  // ------- Tests for tryFoldImmediateCallToBoundFunction -----

  @Test
  public void testFoldBindToCall() {
    // (fn.bind(a,b))() -> fn.call(a,b)
    Node bindCall = IR.call(
        IR.getprop(IR.name("fn"), IR.string("bind")),
        IR.name("a"), IR.name("b"));
    Node call = IR.call(bindCall);
    Node result = optimize(call, false);
    // Now it should be fn.call(a,b)
    assertTrue(result.isCall());
    Node target = result.getFirstChild();
    assertTrue(target.isGetProp());
    assertEquals("fn", target.getFirstChild().getString());
    assertEquals("call", target.getLastChild().getString());
    assertEquals("a", target.getNext().getString());
    assertEquals("b", target.getNext().getNext().getString());
  }

  @Test
  public void testFoldBindToCallUndefinedThis() {
    // (fn.bind(void 0))() -> fn() with FREE_CALL true
    Node bindCall = IR.call(
        IR.getprop(IR.name("fn"), IR.string("bind")),
        IR.voidNode(IR.number(0)));
    Node call = IR.call(bindCall);
    Node result = optimize(call, false);
    assertTrue(result.isCall());
    assertTrue(result.getBooleanProp(Node.FREE_CALL));
    // target unchanged
    Node target = result.getFirstChild();
    assertEquals("fn", target.getString());
    // no this argument added
    assertNull(target.getNext());
  }

  // ----- Tests for trySplitComma -------

  @Test
  public void testSplitCommaNotLate() {
    // a,b -> split into two statements when parent is EXPR_RESULT and not label.
    Node comma = IR.comma(IR.name("a"), IR.name("b"));
    Node exprResult = IR.exprResult(comma);
    Node block = IR.block();
    block.addChildToBack(exprResult);
    Node result = optimize(comma, false); // optimize the COMMA node
    // result should be the left node (a), and a new statement with b should be added after.
    assertEquals("a", result.getString());
    assertEquals(2, block.getChildCount());
    Node second = block.getLastChild();
    assertTrue(second.isExprResult());
    assertEquals("b", second.getFirstChild().getString());
  }

  @Test
  public void testSplitCommaLateNoSplit() {
    // late mode: no split.
    Node comma = IR.comma(IR.name("a"), IR.name("b"));
    Node exprResult = IR.exprResult(comma);
    Node block = IR.block();
    block.addChildToBack(exprResult);
    Node result = optimize(comma, true);
    assertSame(comma, result);
  }

  // ---- Tests for tryReplaceIf -------

  @Test
  public void testReplaceIfWithHokInBlock() {
    // { if(x) return 1; return 2; } -> { return x?1:2; }
    Node block = IR.block();
    block.addChildToBack(IR.ifNode(
        IR.name("x"),
        IR.block(IR.returnNode(IR.number(1))));
    block.addChildToBack(IR.returnNode(IR.number(2)));
    Node result = optimize(block, false);
    // The block should be replaced with a return containing a HOOK.
    assertEquals(1, block.getChildCount());
    Node ret = block.getFirstChild();
    assertTrue(ret.isReturn());
    assertTrue(ret.getFirstChild().isHOOK());
  }

  @Test
  public void testReplaceIfWithHokWhenNoElseExpression() {
    // { if(x) return; return 1; } -> { return x ? void 0 : 1 }
    Node block = IR.block();
    block.addChildToBack(IR.ifNode(
        IR.name("x"),
        IR.block(IR.returnNode())); // return without expr
    block.addChildToBack(IR.returnNode(IR.number(1)));
    optimize(block, false);
    assertEquals(1, block.getChildCount());
    Node ret = block.getFirstChild();
    assertTrue(ret.isReturn());
    Node hook = ret.getFirstChild();
    assertTrue(hook.isHOOK());
    // true branch is void 0
    Node trueExpr = hook.getChildAtIndex(1);
    assertTrue(trueExpr.isVoid());
    assertEquals(0.0, trueExpr.getFirstChild().getDouble(), 0.0);
  }

  // --------- Tests for tryReplaceUndefined -------

  @Test
  public void testReplaceUndefined() {
    // undefined used as value -> void 0
    Node name = IR.name("undefined");
    Node exprResult = IR.exprResult(name);
    Node block = IR.block();
    block.addChildToBack(exprResult);
    Node result = optimize(name, false);
    assertTrue(result.isVoid());
    assertEquals(0.0, result.getFirstChild().getDouble(), 0.0);
  }

  @Test
  public void testReplaceUndefinedAsLValueNotReplaced() {
    // undefined = 1; (lvalue) shouldn't be replaced.
    Node assign = IR.assign(IR.name("undefined"), IR.number(1));
    Node expr = IR.exprResult(assign);
    optimize(expr.getFirstChild().getFirstChild(), false); // the name "undefined" inside assign
    // Should not be replaced, so the name remains.
    assertTrue(assign.getFirstChild().isName());
    assertEquals("undefined", assign.getFirstChild().getString());
  }

  // -------- Tests for tryMinimizeStringArrayLiteral -------

  @Test
  public void testMinimizeStringArrayLiteralLate() {
    // late mode: ["a","b"] -> "a,b".split(",")
    Node arr = IR.arraylit(IR.string("a"), IR.string("b"));
    Node parent = IR.script();
    parent.addChildToBack(IR.exprResult(arr));
    Node result = optimize(arr, true);
    // Should become a call to "a,b".split(",")
    assertTrue(result.isCall());
    Node target = result.getFirstChild();
    assertTrue(target.isGetProp());
    assertEquals("split", target.getLastChild().getString());
    Node stringNode = target.getFirstChild();
    assertTrue(stringNode.isString());
    assertEquals("a,b", stringNode.getString());
    Node delimArg = target.getNext();
    assertTrue(delimArg.isString());
    assertEquals(",", delimArg.getString());
  }

  @Test
  public void testMinimizeStringArrayLiteralNotLateNoChange() {
    Node arr = IR.arraylit(IR.string("a"), IR.string("b"));
    Node result = optimize(arr, false);
    assertTrue(result.isArrayLit());
  }

  // -------- Tests for tryJoinForCondition -------

  @Test
  public void testJoinForConditionLate() {
    // for(;true;) if(x) break; -> for(;true && !x;)
    Node forNode = IR.forNode(
        IR.block(), // init
        IR.trueNode(), // condition
        IR.block(), // increment
        IR.block(
            IR.ifNode(IR.name("x"), IR.block(IR.breakNode())));
    Node result = optimize(forNode, true);
    // condition should become AND
    Node cond = NodeUtil.getConditionExpression(forNode);
    assertTrue(cond.isAND());
    // left is true, right is !x
    assertTrue(cond.getFirstChild().isTrue());
    assertTrue(cond.getLastChild().isNot());
    assertEquals("x", cond.getLastChild().getFirstChild().getString());
  }

  @Test
  public void testJoinForConditionEarlyNoChange() {
    Node forNode = IR.forNode(
        IR.block(),
        IR.trueNode(),
        IR.block(),
        IR.block(IR.ifNode(IR.name("x"), IR.block(IR.breakNode())));
    Node result = optimize(forNode, false);
    assertSame(forNode, result);
  }

  // ------- Tests for reduceTrueFalse -------

  @Test
  public void testReduceTrueFalseLate() {
    // true -> !0, false -> !1 (late mode)
    Node trueNode = IR.trueNode();
    Node script = IR.script();
    script.addChildToBack(IR.exprResult(trueNode));
    Node result = optimize(trueNode, true);
    assertTrue(result.isNot());
    Node num = result.getFirstChild();
    assertTrue(num.isNumber());
    assertEquals(0.0, num.getDouble(), 0.0);

    Node falseNode = IR.falseNode();
    script = IR.script();
    script.addChildToBack(IR.exprResult(falseNode));
    result = optimize(falseNode, true);
    assertTrue(result.isNot());
    num = result.getFirstChild();
    assertEquals(1.0, num.getDouble(), 0.0);
  }

  @Test
  public void testReduceTrueFalseNotLateNoChange() {
    Node trueNode = IR.trueNode();
    Node result = optimize(trueNode, false);
    assertSame(trueNode, result);
  }

  // -- Tests for tryMinimizeCondition on HOOK --

  @Test
  public void testHokConditionTrueFalse() {
    // x ? true : false -> x
    Node hook = IR.hook(IR.name("x"), IR.trueNode(), IR.falseNode());
    Node result = optimize(hook, false);
    assertEquals("x", result.getString());
  }

  @Test
  public void testHokConditionFalseTrue() {
    // x ? false : true -> !x
    Node hook = IR.hook(IR.name("x"), IR.falseNode(), IR.trueNode());
    Node result = optimize(hook, false);
    assertTrue(result.isNot());
    assertEquals("x", result.getFirstChild().getString());
  }

  @Test
  public void testHokConditionTrueOther() {
    // x ? true : y -> x || y
    Node hook = IR.hook(IR.name("x"), IR.trueNode(), IR.name("y"));
    Node result = optimize(hook, false);
    assertTrue(result.isOR());
    assertEquals("x", result.getFirstChild().getString());
    assertEquals("y", result.getLastChild().getString());
  }

  @Test
  public void testHokConditionOtherFalse() {
    // x ? y : false -> x && y
    Node hook = IR.hook(IR.name("x"), IR.name("y"), IR.falseNode());
    Node result = optimize(hook, false);
    assertTrue(result.isAND());
    assertEquals("x", result.getFirstChild().getString());
    assertEquals("y", result.getLastChild().getString());
  }

  // -- Tests for tryMinimizeCondition on OR/AND --

  @Test
  public void testOrWithFalse() {
    // x || false -> x
    Node or = IR.or(IR.name("x"), IR.falseNode());
    Node result = optimize(or, false);
    assertEquals("x", result.getString());
  }

  @Test
  public void testAndWithTrue() {
    // x && true -> x
    Node and = IR.and(IR.name("x"), IR.trueNode());
    Node result = optimize(and, false);
    assertEquals("x", result.getString());
  }

  @Test
  public void testOrWithTrue() {
    // x || true -> true (but only if left has no side-effects; we test with pure left)
    Node or = IR.or(IR.name("x"), IR.trueNode());
    Node result = optimize(or, false);
    assertTrue(result.isTrue());
  }

  @Test
  public void testAndWithFalse() {
    // x && false -> false (pure left)
    Node and = IR.and(IR.name("x"), IR.falseNode());
    Node result = optimize(and, false);
    assertTrue(result.isFalse());
  }

  @Test
  public void testDemorganLawsNotAnd() {
    // !(x && y) -> !x || !y
    Node notExpr = IR.not(IR.and(IR.name("x"), IR.name("y")));
    Node result = optimize(notExpr, false);
    assertTrue(result.isOR());
    Node left = result.getFirstChild();
    Node right = result.getLastChild();
    assertTrue(left.isNot());
    assertEquals("x", left.getFirstChild().getString());
    assertTrue(right.isNot());
    assertEquals("y", right.getFirstChild().getString());
  }

  @Test
  public void testDemorganLawsNotOr() {
    // !(x || y) -> !x && !y
    Node notExpr = IR.not(IR.or(IR.name("x"), IR.name("y")));
    Node result = optimize(notExpr, false);
    assertTrue(result.isAND());
    Node left = result.getFirstChild();
    Node right = result.getLastChild();
    assertTrue(left.isNot());
    assertTrue(right.isNot());
  }

  // -------- Tests for tryMinimizeArrayLiteral (all strings) ----
  // Already covered by string array literal test, but here we test that non-string arrays don't trigger.

  @Test
  public void testArrayLitWithNonStringNotFolded() {
    Node arr = IR.arraylit(IR.string("a"), IR.number(1));
    Node result = optimize(arr, false);
    assertTrue(result.isArrayLit());
  }

}
```
