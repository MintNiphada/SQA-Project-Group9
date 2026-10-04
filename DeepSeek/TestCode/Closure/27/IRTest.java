package com.google.javascript.rhino;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class IRTest {

  @Test
  public void testEmpty() {
    Node n = IR.empty();
    assertEquals(Token.EMPTY, n.getType());
    assertFalse(n.hasChildren());
  }

  @Test
  public void testFunctionValid() {
    Node name = IR.name("f");
    Node params = IR.paramList();
    Node body = IR.block();
    Node f = IR.function(name, params, body);
    assertEquals(Token.FUNCTION, f.getType());
    assertEquals(name, f.getFirstChild());
    assertEquals(params, name.getNext());
    assertEquals(body, params.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionNameNotName() {
    IR.function(IR.number(0), IR.paramList(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionParamsNotParamList() {
    IR.function(IR.name("f"), IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionBodyNotBlock() {
    IR.function(IR.name("f"), IR.paramList(), IR.number(0));
  }

  @Test
  public void testParamListNoArgs() {
    Node p = IR.paramList();
    assertEquals(Token.PARAM_LIST, p.getType());
    assertFalse(p.hasChildren());
  }

  @Test
  public void testParamListSingle() {
    Node name = IR.name("a");
    Node p = IR.paramList(name);
    assertEquals(Token.PARAM_LIST, p.getType());
    assertEquals(name, p.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testParamListSingleInvalid() {
    IR.paramList(IR.number(0));
  }

  @Test
  public void testParamListVarargs() {
    Node a = IR.name("a");
    Node b = IR.name("b");
    Node p = IR.paramList(a, b);
    assertEquals(Token.PARAM_LIST, p.getType());
    assertEquals(a, p.getFirstChild());
    assertEquals(b, a.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testParamListVarargsInvalid() {
    IR.paramList(IR.name("a"), IR.number(0));
  }

  @Test
  public void testParamListList() {
    Node a = IR.name("a");
    Node b = IR.name("b");
    Node p = IR.paramList(Arrays.asList(a, b));
    assertEquals(Token.PARAM_LIST, p.getType());
    assertEquals(a, p.getFirstChild());
    assertEquals(b, a.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testParamListListInvalid() {
    IR.paramList(Arrays.asList(IR.name("a"), IR.number(0)));
  }

  @Test
  public void testBlockNoArgs() {
    Node b = IR.block();
    assertEquals(Token.BLOCK, b.getType());
    assertFalse(b.hasChildren());
  }

  @Test
  public void testBlockSingleStmt() {
    Node stmt = IR.empty();
    Node b = IR.block(stmt);
    assertEquals(Token.BLOCK, b.getType());
    assertEquals(stmt, b.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testBlockSingleStmtInvalid() {
    IR.block(IR.number(0));
  }

  @Test
  public void testBlockVarargs() {
    Node s1 = IR.empty();
    Node s2 = IR.block();
    Node b = IR.block(s1, s2);
    assertEquals(Token.BLOCK, b.getType());
    assertEquals(s1, b.getFirstChild());
    assertEquals(s2, s1.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testBlockVarargsInvalid() {
    IR.block(IR.empty(), IR.number(0));
  }

  @Test
  public void testScript() {
    Node s1 = IR.empty();
    Node s2 = IR.block();
    Node s = IR.script(s1, s2);
    assertEquals(Token.SCRIPT, s.getType());
    assertEquals(s1, s.getFirstChild());
    assertEquals(s2, s1.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testScriptInvalid() {
    IR.script(IR.number(0));
  }

  @Test
  public void testVarNameValue() {
    Node name = IR.name("x");
    Node value = IR.number(1);
    Node v = IR.var(name, value);
    assertEquals(Token.VAR, v.getType());
    assertEquals(name, v.getFirstChild());
    assertEquals(value, name.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testVarNameValueNameNotName() {
    IR.var(IR.number(0), IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testVarNameValueNameHasChildren() {
    Node name = IR.name("x");
    name.addChildToBack(IR.number(1));
    IR.var(name, IR.number(2));
  }

  @Test(expected = IllegalStateException.class)
  public void testVarNameValueValueNotExpression() {
    IR.var(IR.name("x"), IR.block());
  }

  @Test
  public void testVarName() {
    Node name = IR.name("x");
    Node v = IR.var(name);
    assertEquals(Token.VAR, v.getType());
    assertEquals(name, v.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testVarNameInvalid() {
    IR.var(IR.number(0));
  }

  @Test
  public void testReturnNodeNoExpr() {
    Node r = IR.returnNode();
    assertEquals(Token.RETURN, r.getType());
    assertFalse(r.hasChildren());
  }

  @Test
  public void testReturnNodeExpr() {
    Node expr = IR.number(1);
    Node r = IR.returnNode(expr);
    assertEquals(Token.RETURN, r.getType());
    assertEquals(expr, r.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testReturnNodeExprInvalid() {
    IR.returnNode(IR.block());
  }

  @Test
  public void testThrowNode() {
    Node expr = IR.number(1);
    Node t = IR.throwNode(expr);
    assertEquals(Token.THROW, t.getType());
    assertEquals(expr, t.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowNodeInvalid() {
    IR.throwNode(IR.block());
  }

  @Test
  public void testExprResult() {
    Node expr = IR.number(1);
    Node e = IR.exprResult(expr);
    assertEquals(Token.EXPR_RESULT, e.getType());
    assertEquals(expr, e.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testExprResultInvalid() {
    IR.exprResult(IR.block());
  }

  @Test
  public void testIfNodeTwoArgs() {
    Node cond = IR.trueNode();
    Node then = IR.block();
    Node i = IR.ifNode(cond, then);
    assertEquals(Token.IF, i.getType());
    assertEquals(cond, i.getFirstChild());
    assertEquals(then, cond.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testIfNodeTwoArgsCondInvalid() {
    IR.ifNode(IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testIfNodeTwoArgsThenInvalid() {
    IR.ifNode(IR.trueNode(), IR.number(0));
  }

  @Test
  public void testIfNodeThreeArgs() {
    Node cond = IR.trueNode();
    Node then = IR.block();
    Node elseNode = IR.block();
    Node i = IR.ifNode(cond, then, elseNode);
    assertEquals(Token.IF, i.getType());
    assertEquals(cond, i.getFirstChild());
    assertEquals(then, cond.getNext());
    assertEquals(elseNode, then.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testIfNodeThreeArgsElseInvalid() {
    IR.ifNode(IR.trueNode(), IR.block(), IR.number(0));
  }

  @Test
  public void testDoNode() {
    Node body = IR.block();
    Node cond = IR.trueNode();
    Node d = IR.doNode(body, cond);
    assertEquals(Token.DO, d.getType());
    assertEquals(body, d.getFirstChild());
    assertEquals(cond, body.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testDoNodeBodyInvalid() {
    IR.doNode(IR.number(0), IR.trueNode());
  }

  @Test(expected = IllegalStateException.class)
  public void testDoNodeCondInvalid() {
    IR.doNode(IR.block(), IR.block());
  }

  @Test
  public void testForInTargetVar() {
    Node target = IR.var(IR.name("i"));
    Node cond = IR.name("obj");
    Node body = IR.block();
    Node f = IR.forIn(target, cond, body);
    assertEquals(Token.FOR, f.getType());
    assertEquals(target, f.getFirstChild());
    assertEquals(cond, target.getNext());
    assertEquals(body, cond.getNext());
  }

  @Test
  public void testForInTargetExpression() {
    Node target = IR.name("i");
    Node cond = IR.name("obj");
    Node body = IR.block();
    Node f = IR.forIn(target, cond, body);
    assertEquals(Token.FOR, f.getType());
    assertEquals(target, f.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testForInTargetInvalid() {
    IR.forIn(IR.block(), IR.name("obj"), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForInCondInvalid() {
    IR.forIn(IR.var(IR.name("i")), IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForInBodyInvalid() {
    IR.forIn(IR.var(IR.name("i")), IR.name("obj"), IR.number(0));
  }

  @Test
  public void testForNode() {
    Node init = IR.var(IR.name("i"));
    Node cond = IR.trueNode();
    Node incr = IR.number(1);
    Node body = IR.block();
    Node f = IR.forNode(init, cond, incr, body);
    assertEquals(Token.FOR, f.getType());
    assertEquals(init, f.getFirstChild());
    assertEquals(cond, init.getNext());
    assertEquals(incr, cond.getNext());
    assertEquals(body, incr.getNext());
  }

  @Test
  public void testForNodeEmptyInitCondIncr() {
    Node init = IR.empty();
    Node cond = IR.empty();
    Node incr = IR.empty();
    Node body = IR.block();
    Node f = IR.forNode(init, cond, incr, body);
    assertEquals(Token.FOR, f.getType());
    assertEquals(init, f.getFirstChild());
    assertEquals(cond, init.getNext());
    assertEquals(incr, cond.getNext());
    assertEquals(body, incr.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testForNodeInitInvalid() {
    IR.forNode(IR.block(), IR.empty(), IR.empty(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForNodeCondInvalid() {
    IR.forNode(IR.empty(), IR.block(), IR.empty(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForNodeIncrInvalid() {
    IR.forNode(IR.empty(), IR.empty(), IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForNodeBodyInvalid() {
    IR.forNode(IR.empty(), IR.empty(), IR.empty(), IR.number(0));
  }

  @Test
  public void testSwitchNode() {
    Node cond = IR.name("x");
    Node case1 = IR.caseNode(IR.number(1), IR.block());
    Node case2 = IR.defaultCase(IR.block());
    Node sw = IR.switchNode(cond, case1, case2);
    assertEquals(Token.SWITCH, sw.getType());
    assertEquals(cond, sw.getFirstChild());
    assertEquals(case1, cond.getNext());
    assertEquals(case2, case1.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testSwitchNodeCondInvalid() {
    IR.switchNode(IR.block(), IR.caseNode(IR.number(1), IR.block()));
  }

  @Test(expected = IllegalStateException.class)
  public void testSwitchNodeCaseInvalid() {
    IR.switchNode(IR.name("x"), IR.number(0));
  }

  @Test
  public void testCaseNode() {
    Node expr = IR.number(1);
    Node body = IR.block();
    Node c = IR.caseNode(expr, body);
    assertEquals(Token.CASE, c.getType());
    assertEquals(expr, c.getFirstChild());
    assertEquals(body, expr.getNext());
    assertTrue(body.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));
  }

  @Test(expected = IllegalStateException.class)
  public void testCaseNodeExprInvalid() {
    IR.caseNode(IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testCaseNodeBodyInvalid() {
    IR.caseNode(IR.number(1), IR.number(0));
  }

  @Test
  public void testDefaultCase() {
    Node body = IR.block();
    Node d = IR.defaultCase(body);
    assertEquals(Token.DEFAULT_CASE, d.getType());
    assertEquals(body, d.getFirstChild());
    assertTrue(body.getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));
  }

  @Test(expected = IllegalStateException.class)
  public void testDefaultCaseBodyInvalid() {
    IR.defaultCase(IR.number(0));
  }

  @Test
  public void testLabel() {
    Node name = IR.labelName("loop");
    Node stmt = IR.block();
    Node l = IR.label(name, stmt);
    assertEquals(Token.LABEL, l.getType());
    assertEquals(name, l.getFirstChild());
    assertEquals(stmt, name.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testLabelNameInvalid() {
    IR.label(IR.name("loop"), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testLabelStmtInvalid() {
    IR.label(IR.labelName("loop"), IR.number(0));
  }

  @Test
  public void testLabelName() {
    Node n = IR.labelName("loop");
    assertEquals(Token.LABEL_NAME, n.getType());
    assertEquals("loop", n.getString());
  }

  @Test(expected = IllegalStateException.class)
  public void testLabelNameEmpty() {
    IR.labelName("");
  }

  @Test
  public void testTryFinally() {
    Node tryBody = IR.labelName("try");
    Node finallyBody = IR.labelName("finally");
    Node t = IR.tryFinally(tryBody, finallyBody);
    assertEquals(Token.TRY, t.getType());
    assertEquals(tryBody, t.getFirstChild());
    Node catchBody = tryBody.getNext();
    assertEquals(Token.BLOCK, catchBody.getType());
    assertEquals(finallyBody, catchBody.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testTryFinallyTryBodyInvalid() {
    IR.tryFinally(IR.block(), IR.labelName("finally"));
  }

  @Test(expected = IllegalStateException.class)
  public void testTryFinallyFinallyBodyInvalid() {
    IR.tryFinally(IR.labelName("try"), IR.block());
  }

  @Test
  public void testTryCatch() {
    Node tryBody = IR.block();
    Node catchNode = IR.catchNode(IR.name("e"), IR.block());
    Node t = IR.tryCatch(tryBody, catchNode);
    assertEquals(Token.TRY, t.getType());
    assertEquals(tryBody, t.getFirstChild());
    Node catchBody = tryBody.getNext();
    assertEquals(Token.BLOCK, catchBody.getType());
    assertEquals(catchNode, catchBody.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testTryCatchTryBodyInvalid() {
    IR.tryCatch(IR.number(0), IR.catchNode(IR.name("e"), IR.block()));
  }

  @Test(expected = IllegalStateException.class)
  public void testTryCatchCatchNodeInvalid() {
    IR.tryCatch(IR.block(), IR.block());
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBody = IR.block();
    Node catchNode = IR.catchNode(IR.name("e"), IR.block());
    Node finallyBody = IR.block();
    Node t = IR.tryCatchFinally(tryBody, catchNode, finallyBody);
    assertEquals(Token.TRY, t.getType());
    assertEquals(tryBody, t.getFirstChild());
    Node catchBody = tryBody.getNext();
    assertEquals(Token.BLOCK, catchBody.getType());
    assertEquals(catchNode, catchBody.getFirstChild());
    assertEquals(finallyBody, catchBody.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testTryCatchFinallyFinallyBodyInvalid() {
    IR.tryCatchFinally(IR.block(), IR.catchNode(IR.name("e"), IR.block()), IR.number(0));
  }

  @Test
  public void testCatchNode() {
    Node expr = IR.name("e");
    Node body = IR.block();
    Node c = IR.catchNode(expr, body);
    assertEquals(Token.CATCH, c.getType());
    assertEquals(expr, c.getFirstChild());
    assertEquals(body, expr.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testCatchNodeExprInvalid() {
    IR.catchNode(IR.number(0), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testCatchNodeBodyInvalid() {
    IR.catchNode(IR.name("e"), IR.number(0));
  }

  @Test
  public void testBreakNodeNoName() {
    Node b = IR.breakNode();
    assertEquals(Token.BREAK, b.getType());
    assertFalse(b.hasChildren());
  }

  @Test
  public void testBreakNodeName() {
    Node name = IR.labelName("loop");
    Node b = IR.breakNode(name);
    assertEquals(Token.BREAK, b.getType());
    assertEquals(name, b.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testBreakNodeNameInvalid() {
    IR.breakNode(IR.name("loop"));
  }

  @Test
  public void testContinueNodeNoName() {
    Node c = IR.continueNode();
    assertEquals(Token.CONTINUE, c.getType());
    assertFalse(c.hasChildren());
  }

  @Test
  public void testContinueNodeName() {
    Node name = IR.labelName("loop");
    Node c = IR.continueNode(name);
    assertEquals(Token.CONTINUE, c.getType());
    assertEquals(name, c.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testContinueNodeNameInvalid() {
    IR.continueNode(IR.name("loop"));
  }

  @Test
  public void testCall() {
    Node target = IR.name("f");
    Node arg1 = IR.number(1);
    Node arg2 = IR.string("s");
    Node call = IR.call(target, arg1, arg2);
    assertEquals(Token.CALL, call.getType());
    assertEquals(target, call.getFirstChild());
    assertEquals(arg1, target.getNext());
    assertEquals(arg2, arg1.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testCallArgInvalid() {
    IR.call(IR.name("f"), IR.block());
  }

  @Test
  public void testNewNode() {
    Node target = IR.name("C");
    Node arg = IR.number(1);
    Node n = IR.newNode(target, arg);
    assertEquals(Token.NEW, n.getType());
    assertEquals(target, n.getFirstChild());
    assertEquals(arg, target.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testNewNodeArgInvalid() {
    IR.newNode(IR.name("C"), IR.block());
  }

  @Test
  public void testName() {
    Node n = IR.name("foo");
    assertEquals(Token.NAME, n.getType());
    assertEquals("foo", n.getString());
  }

  @Test
  public void testGetprop() {
    Node target = IR.name("obj");
    Node prop = IR.string("prop");
    Node g = IR.getprop(target, prop);
    assertEquals(Token.GETPROP, g.getType());
    assertEquals(target, g.getFirstChild());
    assertEquals(prop, target.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetpropTargetInvalid() {
    IR.getprop(IR.block(), IR.string("prop"));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetpropPropInvalid() {
    IR.getprop(IR.name("obj"), IR.number(0));
  }

  @Test
  public void testGetelem() {
    Node target = IR.name("arr");
    Node elem = IR.number(0);
    Node g = IR.getelem(target, elem);
    assertEquals(Token.GETELEM, g.getType());
    assertEquals(target, g.getFirstChild());
    assertEquals(elem, target.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetelemTargetInvalid() {
    IR.getelem(IR.block(), IR.number(0));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetelemElemInvalid() {
    IR.getelem(IR.name("arr"), IR.block());
  }

  @Test
  public void testAssign() {
    Node target = IR.name("x");
    Node expr = IR.number(1);
    Node a = IR.assign(target, expr);
    assertEquals(Token.ASSIGN, a.getType());
    assertEquals(target, a.getFirstChild());
    assertEquals(expr, target.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testAssignTargetInvalid() {
    IR.assign(IR.number(0), IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testAssignExprInvalid() {
    IR.assign(IR.name("x"), IR.block());
  }

  @Test
  public void testHook() {
    Node cond = IR.trueNode();
    Node trueval = IR.number(1);
    Node falseval = IR.number(0);
    Node h = IR.hook(cond, trueval, falseval);
    assertEquals(Token.HOOK, h.getType());
    assertEquals(cond, h.getFirstChild());
    assertEquals(trueval, cond.getNext());
    assertEquals(falseval, trueval.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testHookCondInvalid() {
    IR.hook(IR.block(), IR.number(1), IR.number(0));
  }

  @Test(expected = IllegalStateException.class)
  public void testHookTruevalInvalid() {
    IR.hook(IR.trueNode(), IR.block(), IR.number(0));
  }

  @Test(expected = IllegalStateException.class)
  public void testHookFalsevalInvalid() {
    IR.hook(IR.trueNode(), IR.number(1), IR.block());
  }

  @Test
  public void testComma() {
    Node e1 = IR.number(1);
    Node e2 = IR.number(2);
    Node c = IR.comma(e1, e2);
    assertEquals(Token.COMMA, c.getType());
    assertEquals(e1, c.getFirstChild());
    assertEquals(e2, e1.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testCommaExpr1Invalid() {
    IR.comma(IR.block(), IR.number(2));
  }

  @Test(expected = IllegalStateException.class)
  public void testCommaExpr2Invalid() {
    IR.comma(IR.number(1), IR.block());
  }

  @Test
  public void testAnd() {
    Node e1 = IR.trueNode();
    Node e2 = IR.falseNode();
    Node a = IR.and(e1, e2);
    assertEquals(Token.AND, a.getType());
    assertEquals(e1, a.getFirstChild());
    assertEquals(e2, e1.getNext());
  }

  @Test
  public void testOr() {
    Node e1 = IR.trueNode();
    Node e2 = IR.falseNode();
    Node o = IR.or(e1, e2);
    assertEquals(Token.OR, o.getType());
    assertEquals(e1, o.getFirstChild());
    assertEquals(e2, e1.getNext());
  }

  @Test
  public void testNot() {
    Node e = IR.trueNode();
    Node n = IR.not(e);
    assertEquals(Token.NOT, n.getType());
    assertEquals(e, n.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testNotInvalid() {
    IR.not(IR.block());
  }

  @Test
  public void testEq() {
    Node e1 = IR.number(1);
    Node e2 = IR.number(2);
    Node eq = IR.eq(e1, e2);
    assertEquals(Token.EQ, eq.getType());
    assertEquals(e1, eq.getFirstChild());
    assertEquals(e2, e1.getNext());
  }

  @Test
  public void testSheq() {
    Node e1 = IR.number(1);
    Node e2 = IR.number(1);
    Node sheq = IR.sheq(e1, e2);
    assertEquals(Token.SHEQ, sheq.getType());
    assertEquals(e1, sheq.getFirstChild());
    assertEquals(e2, e1.getNext());
  }

  @Test
  public void testVoidNode() {
    Node e = IR.number(0);
    Node v = IR.voidNode(e);
    assertEquals(Token.VOID, v.getType());
    assertEquals(e, v.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testVoidNodeInvalid() {
    IR.voidNode(IR.block());
  }

  @Test
  public void testNeg() {
    Node e = IR.number(1);
    Node n = IR.neg(e);
    assertEquals(Token.NEG, n.getType());
    assertEquals(e, n.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testNegInvalid() {
    IR.neg(IR.block());
  }

  @Test
  public void testPos() {
    Node e = IR.number(1);
    Node p = IR.pos(e);
    assertEquals(Token.POS, p.getType());
    assertEquals(e, p.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testPosInvalid() {
    IR.pos(IR.block());
  }

  @Test
  public void testAdd() {
    Node e1 = IR.number(1);
    Node e2 = IR.number(2);
    Node a = IR.add(e1, e2);
    assertEquals(Token.ADD, a.getType());
    assertEquals(e1, a.getFirstChild());
    assertEquals(e2, e1.getNext());
  }

  @Test
  public void testSub() {
    Node e1 = IR.number(5);
    Node e2 = IR.number(3);
    Node s = IR.sub(e1, e2);
    assertEquals(Token.SUB, s.getType());
    assertEquals(e1, s.getFirstChild());
    assertEquals(e2, e1.getNext());
  }

  @Test
  public void testObjectlit() {
    Node key1 = IR.stringKey("a");
    Node val1 = IR.number(1);
    Node prop1 = IR.propdef(key1, val1);
    Node key2 = IR.stringKey("b");
    Node val2 = IR.number(2);
    Node prop2 = IR.propdef(key2, val2);
    Node obj = IR.objectlit(prop1, prop2);
    assertEquals(Token.OBJECTLIT, obj.getType());
    assertEquals(prop1, obj.getFirstChild());
    assertEquals(prop2, prop1.getNext());
  }

  @Test
  public void testObjectlitGetterDef() {
    Node getter = new Node(Token.GETTER_DEF, IR.string("prop"));
    Node obj = IR.objectlit(getter);
    assertEquals(Token.OBJECTLIT, obj.getType());
    assertEquals(getter, obj.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testObjectlitInvalidPropdefType() {
    IR.objectlit(IR.number(0));
  }

  @Test(expected = IllegalStateException.class)
  public void testObjectlitPropdefNoChild() {
    Node key = IR.stringKey("a");
    IR.objectlit(key);
  }

  @Test(expected = IllegalStateException.class)
  public void testObjectlitPropdefTwoChildren() {
    Node key = IR.stringKey("a");
    key.addChildToBack(IR.number(1));
    key.addChildToBack(IR.number(2));
    IR.objectlit(key);
  }

  @Test
  public void testPropdef() {
    Node key = IR.stringKey("a");
    Node value = IR.number(1);
    Node p = IR.propdef(key, value);
    assertEquals(Token.STRING_KEY, p.getType());
    assertEquals(value, p.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testPropdefKeyNotStringKey() {
    IR.propdef(IR.name("a"), IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testPropdefKeyHasChildren() {
    Node key = IR.stringKey("a");
    key.addChildToBack(IR.number(1));
    IR.propdef(key, IR.number(2));
  }

  @Test(expected = IllegalStateException.class)
  public void testPropdefValueNotExpression() {
    IR.propdef(IR.stringKey("a"), IR.block());
  }

  @Test
  public void testArraylit() {
    Node e1 = IR.number(1);
    Node e2 = IR.empty();
    Node arr = IR.arraylit(e1, e2);
    assertEquals(Token.ARRAYLIT, arr.getType());
    assertEquals(e1, arr.getFirstChild());
    assertEquals(e2, e1.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testArraylitInvalidExpr() {
    IR.arraylit(IR.block());
  }

  @Test
  public void testRegexpOneArg() {
    Node expr = IR.string("abc");
    Node r = IR.regexp(expr);
    assertEquals(Token.REGEXP, r.getType());
    assertEquals(expr, r.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testRegexpOneArgInvalid() {
    IR.regexp(IR.number(0));
  }

  @Test
  public void testRegexpTwoArgs() {
    Node expr = IR.string("abc");
    Node flags = IR.string("gi");
    Node r = IR.regexp(expr, flags);
    assertEquals(Token.REGEXP, r.getType());
    assertEquals(expr, r.getFirstChild());
    assertEquals(flags, expr.getNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testRegexpTwoArgsExprInvalid() {
    IR.regexp(IR.number(0), IR.string("gi"));
  }

  @Test(expected = IllegalStateException.class)
  public void testRegexpTwoArgsFlagsInvalid() {
    IR.regexp(IR.string("abc"), IR.number(0));
  }

  @Test
  public void testString() {
    Node s = IR.string("hello");
    assertEquals(Token.STRING, s.getType());
    assertEquals("hello", s.getString());
  }

  @Test
  public void testStringKey() {
    Node s = IR.stringKey("key");
    assertEquals(Token.STRING_KEY, s.getType());
    assertEquals("key", s.getString());
  }

  @Test
  public void testNumber() {
    Node n = IR.number(3.14);
    assertEquals(Token.NUMBER, n.getType());
    assertEquals(3.14, n.getDouble(), 0.0);
  }

  @Test
  public void testThisNode() {
    Node t = IR.thisNode();
    assertEquals(Token.THIS, t.getType());
  }

  @Test
  public void testTrueNode() {
    Node t = IR.trueNode();
    assertEquals(Token.TRUE, t.getType());
  }

  @Test
  public void testFalseNode() {
    Node f = IR.falseNode();
    assertEquals(Token.FALSE, f.getType());
  }

  @Test
  public void testNullNode() {
    Node n = IR.nullNode();
    assertEquals(Token.NULL, n.getType());
  }
}
