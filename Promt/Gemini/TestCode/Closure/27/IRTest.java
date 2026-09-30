package com.google.javascript.rhino;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IRTest {

  @Test
  public void testEmpty() {
    Node n = IR.empty();
    Assert.assertEquals(Token.EMPTY, n.getType());
    Assert.assertFalse(n.hasChildren());
  }

  @Test
  public void testFunction() {
    Node name = IR.name("foo");
    Node params = IR.paramList(IR.name("a"));
    Node body = IR.block();
    Node fn = IR.function(name, params, body);

    Assert.assertEquals(Token.FUNCTION, fn.getType());
    Assert.assertEquals(3, fn.getChildCount());
    Assert.assertSame(name, fn.getFirstChild());
    Assert.assertSame(params, fn.getFirstChild().getNext());
    Assert.assertSame(body, fn.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionInvalidName() {
    IR.function(IR.string("foo"), IR.paramList(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionInvalidParams() {
    IR.function(IR.name("foo"), IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testFunctionInvalidBody() {
    IR.function(IR.name("foo"), IR.paramList(), IR.exprResult(IR.number(1)));
  }

  @Test
  public void testParamList() {
    Node pl0 = IR.paramList();
    Assert.assertEquals(Token.PARAM_LIST, pl0.getType());
    Assert.assertEquals(0, pl0.getChildCount());

    Node p1 = IR.name("p1");
    Node pl1 = IR.paramList(p1);
    Assert.assertEquals(Token.PARAM_LIST, pl1.getType());
    Assert.assertEquals(1, pl1.getChildCount());
    Assert.assertSame(p1, pl1.getFirstChild());

    Node p2 = IR.name("p2");
    Node p3 = IR.name("p3");
    Node plVar = IR.paramList(p2, p3);
    Assert.assertEquals(Token.PARAM_LIST, plVar.getType());
    Assert.assertEquals(2, plVar.getChildCount());
    Assert.assertSame(p2, plVar.getFirstChild());
    Assert.assertSame(p3, plVar.getLastChild());

    List<Node> list = Arrays.asList(IR.name("p4"), IR.name("p5"));
    Node plList = IR.paramList(list);
    Assert.assertEquals(Token.PARAM_LIST, plList.getType());
    Assert.assertEquals(2, plList.getChildCount());
  }

  @Test(expected = IllegalStateException.class)
  public void testParamListInvalidSingle() {
    IR.paramList(IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testParamListInvalidVarArgs() {
    IR.paramList(IR.name("a"), IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testParamListInvalidList() {
    IR.paramList(Collections.singletonList(IR.string("a")));
  }

  @Test
  public void testBlock() {
    Node b0 = IR.block();
    Assert.assertEquals(Token.BLOCK, b0.getType());
    Assert.assertEquals(0, b0.getChildCount());

    Node stmt1 = IR.exprResult(IR.number(1));
    Node b1 = IR.block(stmt1);
    Assert.assertEquals(Token.BLOCK, b1.getType());
    Assert.assertEquals(1, b1.getChildCount());
    Assert.assertSame(stmt1, b1.getFirstChild());

    Node stmt2 = IR.exprResult(IR.number(2));
    Node stmt3 = IR.returnNode();
    Node bVar = IR.block(stmt2, stmt3);
    Assert.assertEquals(Token.BLOCK, bVar.getType());
    Assert.assertEquals(2, bVar.getChildCount());
    Assert.assertSame(stmt2, bVar.getFirstChild());
    Assert.assertSame(stmt3, bVar.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testBlockInvalidSingle() {
    IR.block(IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testBlockInvalidVarArgs() {
    IR.block(IR.returnNode(), IR.string("notAStatement"));
  }

  @Test
  public void testScript() {
    Node s1 = IR.exprResult(IR.number(1));
    Node s2 = IR.var(IR.name("x"));
    Node script = IR.script(s1, s2);
    Assert.assertEquals(Token.SCRIPT, script.getType());
    Assert.assertEquals(2, script.getChildCount());
    Assert.assertSame(s1, script.getFirstChild());
    Assert.assertSame(s2, script.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testScriptInvalidChild() {
    IR.script(IR.add(IR.number(1), IR.number(2)));
  }

  @Test
  public void testVar() {
    Node name1 = IR.name("x");
    Node v1 = IR.var(name1);
    Assert.assertEquals(Token.VAR, v1.getType());
    Assert.assertEquals(1, v1.getChildCount());
    Assert.assertSame(name1, v1.getFirstChild());

    Node name2 = IR.name("y");
    Node val = IR.number(42);
    Node v2 = IR.var(name2, val);
    Assert.assertEquals(Token.VAR, v2.getType());
    Assert.assertEquals(1, v2.getChildCount());
    Assert.assertSame(name2, v2.getFirstChild());
    Assert.assertSame(val, name2.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testVarInvalidName() {
    IR.var(IR.string("x"));
  }

  @Test(expected = IllegalStateException.class)
  public void testVarNameWithExistingChildren() {
    Node name = IR.name("x");
    name.addChildToBack(IR.number(1));
    IR.var(name, IR.number(2));
  }

  @Test(expected = IllegalStateException.class)
  public void testVarInvalidValue() {
    IR.var(IR.name("x"), IR.block());
  }

  @Test
  public void testReturnNode() {
    Node ret0 = IR.returnNode();
    Assert.assertEquals(Token.RETURN, ret0.getType());
    Assert.assertEquals(0, ret0.getChildCount());

    Node expr = IR.number(1);
    Node ret1 = IR.returnNode(expr);
    Assert.assertEquals(Token.RETURN, ret1.getType());
    Assert.assertEquals(1, ret1.getChildCount());
    Assert.assertSame(expr, ret1.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testReturnInvalidExpr() {
    IR.returnNode(IR.block());
  }

  @Test
  public void testThrowNode() {
    Node expr = IR.name("e");
    Node th = IR.throwNode(expr);
    Assert.assertEquals(Token.THROW, th.getType());
    Assert.assertEquals(1, th.getChildCount());
    Assert.assertSame(expr, th.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testThrowInvalidExpr() {
    IR.throwNode(IR.block());
  }

  @Test
  public void testExprResult() {
    Node expr = IR.call(IR.name("fn"));
    Node er = IR.exprResult(expr);
    Assert.assertEquals(Token.EXPR_RESULT, er.getType());
    Assert.assertEquals(1, er.getChildCount());
    Assert.assertSame(expr, er.getFirstChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testExprResultInvalidExpr() {
    IR.exprResult(IR.block());
  }

  @Test
  public void testIfNode() {
    Node cond = IR.trueNode();
    Node thenBlock = IR.block();
    Node if2 = IR.ifNode(cond, thenBlock);
    Assert.assertEquals(Token.IF, if2.getType());
    Assert.assertEquals(2, if2.getChildCount());
    Assert.assertSame(cond, if2.getFirstChild());
    Assert.assertSame(thenBlock, if2.getLastChild());

    Node elseBlock = IR.block();
    Node if3 = IR.ifNode(cond, thenBlock, elseBlock);
    Assert.assertEquals(Token.IF, if3.getType());
    Assert.assertEquals(3, if3.getChildCount());
    Assert.assertSame(cond, if3.getFirstChild());
    Assert.assertSame(thenBlock, if3.getFirstChild().getNext());
    Assert.assertSame(elseBlock, if3.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testIfNodeInvalidCond() {
    IR.ifNode(IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testIfNodeInvalidThen() {
    IR.ifNode(IR.trueNode(), IR.exprResult(IR.number(1)));
  }

  @Test(expected = IllegalStateException.class)
  public void testIfNode3InvalidElse() {
    IR.ifNode(IR.trueNode(), IR.block(), IR.exprResult(IR.number(1)));
  }

  @Test
  public void testDoNode() {
    Node body = IR.block();
    Node cond = IR.falseNode();
    Node d = IR.doNode(body, cond);
    Assert.assertEquals(Token.DO, d.getType());
    Assert.assertEquals(2, d.getChildCount());
    Assert.assertSame(body, d.getFirstChild());
    Assert.assertSame(cond, d.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testDoNodeInvalidBody() {
    IR.doNode(IR.exprResult(IR.number(1)), IR.trueNode());
  }

  @Test(expected = IllegalStateException.class)
  public void testDoNodeInvalidCond() {
    IR.doNode(IR.block(), IR.block());
  }

  @Test
  public void testForIn() {
    Node varTarget = IR.var(IR.name("x"));
    Node cond = IR.name("obj");
    Node body = IR.block();
    Node forInVar = IR.forIn(varTarget, cond, body);
    Assert.assertEquals(Token.FOR, forInVar.getType());
    Assert.assertEquals(3, forInVar.getChildCount());
    Assert.assertSame(varTarget, forInVar.getFirstChild());
    Assert.assertSame(cond, forInVar.getFirstChild().getNext());
    Assert.assertSame(body, forInVar.getLastChild());

    Node exprTarget = IR.name("x");
    Node forInExpr = IR.forIn(exprTarget, cond, body);
    Assert.assertEquals(Token.FOR, forInExpr.getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testForInInvalidTarget() {
    IR.forIn(IR.block(), IR.name("obj"), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForInInvalidCond() {
    IR.forIn(IR.name("x"), IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForInInvalidBody() {
    IR.forIn(IR.name("x"), IR.name("obj"), IR.returnNode());
  }

  @Test
  public void testForNode() {
    Node init = IR.var(IR.name("i"), IR.number(0));
    Node cond = IR.eq(IR.name("i"), IR.number(10));
    Node incr = IR.assign(IR.name("i"), IR.add(IR.name("i"), IR.number(1)));
    Node body = IR.block();

    Node f = IR.forNode(init, cond, incr, body);
    Assert.assertEquals(Token.FOR, f.getType());
    Assert.assertEquals(4, f.getChildCount());
    Assert.assertSame(init, f.getFirstChild());
    Assert.assertSame(cond, f.getFirstChild().getNext());
    Assert.assertSame(incr, f.getFirstChild().getNext().getNext());
    Assert.assertSame(body, f.getLastChild());

    Node fEmpty = IR.forNode(IR.empty(), IR.empty(), IR.empty(), IR.block());
    Assert.assertEquals(Token.FOR, fEmpty.getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testForNodeInvalidInit() {
    IR.forNode(IR.returnNode(), IR.empty(), IR.empty(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForNodeInvalidCond() {
    IR.forNode(IR.empty(), IR.returnNode(), IR.empty(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForNodeInvalidIncr() {
    IR.forNode(IR.empty(), IR.empty(), IR.returnNode(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testForNodeInvalidBody() {
    IR.forNode(IR.empty(), IR.empty(), IR.empty(), IR.returnNode());
  }

  @Test
  public void testSwitchAndCases() {
    Node expr = IR.name("x");
    Node case1 = IR.caseNode(IR.number(1), IR.block());
    Node defCase = IR.defaultCase(IR.block());

    Assert.assertEquals(Token.CASE, case1.getType());
    Assert.assertTrue(case1.getLastChild().getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));

    Assert.assertEquals(Token.DEFAULT_CASE, defCase.getType());
    Assert.assertTrue(defCase.getFirstChild().getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));

    Node sw = IR.switchNode(expr, case1, defCase);
    Assert.assertEquals(Token.SWITCH, sw.getType());
    Assert.assertEquals(3, sw.getChildCount());
    Assert.assertSame(expr, sw.getFirstChild());
    Assert.assertSame(case1, sw.getFirstChild().getNext());
    Assert.assertSame(defCase, sw.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testSwitchInvalidCond() {
    IR.switchNode(IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testSwitchInvalidCase() {
    IR.switchNode(IR.name("x"), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testCaseInvalidExpr() {
    IR.caseNode(IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testCaseInvalidBody() {
    IR.caseNode(IR.number(1), IR.returnNode());
  }

  @Test(expected = IllegalStateException.class)
  public void testDefaultCaseInvalidBody() {
    IR.defaultCase(IR.returnNode());
  }

  @Test
  public void testLabelAndLabelName() {
    Node lblName = IR.labelName("myLabel");
    Assert.assertEquals(Token.LABEL_NAME, lblName.getType());
    Assert.assertEquals("myLabel", lblName.getString());

    Node stmt = IR.exprResult(IR.number(1));
    Node lbl = IR.label(lblName, stmt);
    Assert.assertEquals(Token.LABEL, lbl.getType());
    Assert.assertEquals(2, lbl.getChildCount());
    Assert.assertSame(lblName, lbl.getFirstChild());
    Assert.assertSame(stmt, lbl.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testEmptyLabelName() {
    IR.labelName("");
  }

  @Test(expected = IllegalStateException.class)
  public void testLabelInvalidName() {
    IR.label(IR.name("a"), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testLabelInvalidStmt() {
    IR.label(IR.labelName("lbl"), IR.number(1));
  }

  @Test
  public void testTryCatchFinally() {
    Node tryBody = IR.block();
    Node catchVar = IR.name("err");
    Node catchBody = IR.block();
    Node catchN = IR.catchNode(catchVar, catchBody);
    Assert.assertEquals(Token.CATCH, catchN.getType());
    Assert.assertSame(catchVar, catchN.getFirstChild());
    Assert.assertSame(catchBody, catchN.getLastChild());

    Node tryCatchN = IR.tryCatch(tryBody, catchN);
    Assert.assertEquals(Token.TRY, tryCatchN.getType());
    Assert.assertEquals(2, tryCatchN.getChildCount());
    Assert.assertSame(tryBody, tryCatchN.getFirstChild());
    Assert.assertEquals(Token.BLOCK, tryCatchN.getFirstChild().getNext().getType());
    Assert.assertSame(catchN, tryCatchN.getFirstChild().getNext().getFirstChild());

    Node finallyBody = IR.block();
    Node tryCatchFinallyN = IR.tryCatchFinally(tryBody, catchN, finallyBody);
    Assert.assertEquals(Token.TRY, tryCatchFinallyN.getType());
    Assert.assertEquals(3, tryCatchFinallyN.getChildCount());
    Assert.assertSame(finallyBody, tryCatchFinallyN.getLastChild());

    Node lblTry = IR.labelName("t");
    Node lblFin = IR.labelName("f");
    Node tryFin = IR.tryFinally(lblTry, lblFin);
    Assert.assertEquals(Token.TRY, tryFin.getType());
    Assert.assertEquals(3, tryFin.getChildCount());
    Assert.assertSame(lblTry, tryFin.getFirstChild());
    Assert.assertEquals(Token.BLOCK, tryFin.getFirstChild().getNext().getType());
    Assert.assertSame(lblFin, tryFin.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testTryFinallyInvalidTry() {
    IR.tryFinally(IR.block(), IR.labelName("f"));
  }

  @Test(expected = IllegalStateException.class)
  public void testTryFinallyInvalidFin() {
    IR.tryFinally(IR.labelName("t"), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testTryCatchInvalidTry() {
    IR.tryCatch(IR.number(1), IR.catchNode(IR.name("e"), IR.block()));
  }

  @Test(expected = IllegalStateException.class)
  public void testTryCatchInvalidCatch() {
    IR.tryCatch(IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testTryCatchFinallyInvalidFinally() {
    IR.tryCatchFinally(IR.block(), IR.catchNode(IR.name("e"), IR.block()), IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testCatchInvalidExpr() {
    IR.catchNode(IR.number(1), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testCatchInvalidBody() {
    IR.catchNode(IR.name("e"), IR.returnNode());
  }

  @Test
  public void testBreakAndContinue() {
    Node brk0 = IR.breakNode();
    Assert.assertEquals(Token.BREAK, brk0.getType());
    Assert.assertEquals(0, brk0.getChildCount());

    Node lblName = IR.labelName("lbl");
    Node brk1 = IR.breakNode(lblName);
    Assert.assertEquals(Token.BREAK, brk1.getType());
    Assert.assertEquals(1, brk1.getChildCount());
    Assert.assertSame(lblName, brk1.getFirstChild());

    Node cont0 = IR.continueNode();
    Assert.assertEquals(Token.CONTINUE, cont0.getType());
    Assert.assertEquals(0, cont0.getChildCount());

    Node cont1 = IR.continueNode(IR.labelName("lbl2"));
    Assert.assertEquals(Token.CONTINUE, cont1.getType());
    Assert.assertEquals(1, cont1.getChildCount());
  }

  @Test(expected = IllegalStateException.class)
  public void testBreakInvalidName() {
    IR.breakNode(IR.name("lbl"));
  }

  @Test(expected = IllegalStateException.class)
  public void testContinueInvalidName() {
    IR.continueNode(IR.name("lbl"));
  }

  @Test
  public void testCallAndNew() {
    Node target = IR.name("fn");
    Node arg1 = IR.number(1);
    Node arg2 = IR.string("str");

    Node call = IR.call(target, arg1, arg2);
    Assert.assertEquals(Token.CALL, call.getType());
    Assert.assertEquals(3, call.getChildCount());
    Assert.assertSame(target, call.getFirstChild());
    Assert.assertSame(arg1, call.getFirstChild().getNext());
    Assert.assertSame(arg2, call.getLastChild());

    Node newN = IR.newNode(target, arg1, arg2);
    Assert.assertEquals(Token.NEW, newN.getType());
    Assert.assertEquals(3, newN.getChildCount());
    Assert.assertSame(target, newN.getFirstChild());
    Assert.assertSame(arg1, newN.getFirstChild().getNext());
    Assert.assertSame(arg2, newN.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testCallInvalidArg() {
    IR.call(IR.name("fn"), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testNewInvalidArg() {
    IR.newNode(IR.name("Ctor"), IR.block());
  }

  @Test
  public void testGetPropAndGetElem() {
    Node obj = IR.name("obj");
    Node prop = IR.string("prop");
    Node gp = IR.getprop(obj, prop);
    Assert.assertEquals(Token.GETPROP, gp.getType());
    Assert.assertEquals(2, gp.getChildCount());
    Assert.assertSame(obj, gp.getFirstChild());
    Assert.assertSame(prop, gp.getLastChild());

    Node elem = IR.number(0);
    Node ge = IR.getelem(obj, elem);
    Assert.assertEquals(Token.GETELEM, ge.getType());
    Assert.assertEquals(2, ge.getChildCount());
    Assert.assertSame(obj, ge.getFirstChild());
    Assert.assertSame(elem, ge.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetPropInvalidTarget() {
    IR.getprop(IR.block(), IR.string("a"));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetPropInvalidProp() {
    IR.getprop(IR.name("obj"), IR.name("a"));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetElemInvalidTarget() {
    IR.getelem(IR.block(), IR.number(0));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetElemInvalidElem() {
    IR.getelem(IR.name("obj"), IR.block());
  }

  @Test
  public void testAssign() {
    Node targetName = IR.name("a");
    Node val = IR.number(1);
    Node as1 = IR.assign(targetName, val);
    Assert.assertEquals(Token.ASSIGN, as1.getType());
    Assert.assertSame(targetName, as1.getFirstChild());
    Assert.assertSame(val, as1.getLastChild());

    Node targetProp = IR.getprop(IR.name("obj"), IR.string("p"));
    Node as2 = IR.assign(targetProp, val);
    Assert.assertEquals(Token.ASSIGN, as2.getType());

    Node targetElem = IR.getelem(IR.name("arr"), IR.number(0));
    Node as3 = IR.assign(targetElem, val);
    Assert.assertEquals(Token.ASSIGN, as3.getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testAssignInvalidTarget() {
    IR.assign(IR.number(1), IR.number(2));
  }

  @Test(expected = IllegalStateException.class)
  public void testAssignInvalidValue() {
    IR.assign(IR.name("a"), IR.block());
  }

  @Test
  public void testHook() {
    Node cond = IR.trueNode();
    Node tVal = IR.number(1);
    Node fVal = IR.number(2);
    Node hook = IR.hook(cond, tVal, fVal);
    Assert.assertEquals(Token.HOOK, hook.getType());
    Assert.assertEquals(3, hook.getChildCount());
    Assert.assertSame(cond, hook.getFirstChild());
    Assert.assertSame(tVal, hook.getFirstChild().getNext());
    Assert.assertSame(fVal, hook.getLastChild());
  }

  @Test(expected = IllegalStateException.class)
  public void testHookInvalidCond() {
    IR.hook(IR.block(), IR.number(1), IR.number(2));
  }

  @Test(expected = IllegalStateException.class)
  public void testHookInvalidTrue() {
    IR.hook(IR.trueNode(), IR.block(), IR.number(2));
  }

  @Test(expected = IllegalStateException.class)
  public void testHookInvalidFalse() {
    IR.hook(IR.trueNode(), IR.number(1), IR.block());
  }

  @Test
  public void testBinaryAndUnaryOperators() {
    Node a = IR.number(1);
    Node b = IR.number(2);

    Assert.assertEquals(Token.COMMA, IR.comma(a, b).getType());
    Assert.assertEquals(Token.AND, IR.and(a, b).getType());
    Assert.assertEquals(Token.OR, IR.or(a, b).getType());
    Assert.assertEquals(Token.EQ, IR.eq(a, b).getType());
    Assert.assertEquals(Token.SHEQ, IR.sheq(a, b).getType());
    Assert.assertEquals(Token.ADD, IR.add(a, b).getType());
    Assert.assertEquals(Token.SUB, IR.sub(a, b).getType());

    Assert.assertEquals(Token.NOT, IR.not(a).getType());
    Assert.assertEquals(Token.VOID, IR.voidNode(a).getType());
    Assert.assertEquals(Token.NEG, IR.neg(a).getType());
    Assert.assertEquals(Token.POS, IR.pos(a).getType());
  }

  @Test(expected = IllegalStateException.class)
  public void testBinaryOpInvalidLeft() {
    IR.add(IR.block(), IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testBinaryOpInvalidRight() {
    IR.add(IR.number(1), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testUnaryOpInvalidExpr() {
    IR.not(IR.block());
  }

  @Test
  public void testObjectLitAndPropDef() {
    Node k1 = IR.stringKey("k1");
    Node v1 = IR.number(1);
    Node pd1 = IR.propdef(k1, v1);
    Assert.assertEquals(Token.STRING_KEY, pd1.getType());
    Assert.assertSame(v1, pd1.getFirstChild());

    Node getter = new Node(Token.GETTER_DEF, IR.function(IR.name(""), IR.paramList(), IR.block()));
    Node setter = new Node(Token.SETTER_DEF, IR.function(IR.name(""), IR.paramList(IR.name("v")), IR.block()));

    Node obj = IR.objectlit(pd1, getter, setter);
    Assert.assertEquals(Token.OBJECTLIT, obj.getType());
    Assert.assertEquals(3, obj.getChildCount());
  }

  @Test(expected = IllegalStateException.class)
  public void testPropDefInvalidKey() {
    IR.propdef(IR.string("k"), IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testPropDefKeyWithExistingChild() {
    Node key = IR.stringKey("k");
    key.addChildToBack(IR.number(1));
    IR.propdef(key, IR.number(2));
  }

  @Test(expected = IllegalStateException.class)
  public void testPropDefInvalidValue() {
    IR.propdef(IR.stringKey("k"), IR.block());
  }

  @Test(expected = IllegalStateException.class)
  public void testObjectLitInvalidDef() {
    IR.objectlit(IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testObjectLitKeyWithoutChild() {
    IR.objectlit(IR.stringKey("k"));
  }

  @Test
  public void testArrayLit() {
    Node arr = IR.arraylit(IR.number(1), IR.empty(), IR.string("a"));
    Assert.assertEquals(Token.ARRAYLIT, arr.getType());
    Assert.assertEquals(3, arr.getChildCount());
  }

  @Test(expected = IllegalStateException.class)
  public void testArrayLitInvalidChild() {
    IR.arraylit(IR.block());
  }

  @Test
  public void testRegexp() {
    Node r1 = IR.regexp(IR.string("abc"));
    Assert.assertEquals(Token.REGEXP, r1.getType());
    Assert.assertEquals(1, r1.getChildCount());

    Node r2 = IR.regexp(IR.string("abc"), IR.string("g"));
    Assert.assertEquals(Token.REGEXP, r2.getType());
    Assert.assertEquals(2, r2.getChildCount());
  }

  @Test(expected = IllegalStateException.class)
  public void testRegexp1InvalidExpr() {
    IR.regexp(IR.number(1));
  }

  @Test(expected = IllegalStateException.class)
  public void testRegexp2InvalidFlags() {
    IR.regexp(IR.string("abc"), IR.number(1));
  }

  @Test
  public void testLiteralsAndIdentifiers() {
    Node str = IR.string("hello");
    Assert.assertEquals(Token.STRING, str.getType());
    Assert.assertEquals("hello", str.getString());

    Node strKey = IR.stringKey("myKey");
    Assert.assertEquals(Token.STRING_KEY, strKey.getType());
    Assert.assertEquals("myKey", strKey.getString());

    Node num = IR.number(3.14);
    Assert.assertEquals(Token.NUMBER, num.getType());
    Assert.assertEquals(3.14, num.getDouble(), 1e-9);

    Assert.assertEquals(Token.THIS, IR.thisNode().getType());
    Assert.assertEquals(Token.TRUE, IR.trueNode().getType());
    Assert.assertEquals(Token.FALSE, IR.falseNode().getType());
    Assert.assertEquals(Token.NULL, IR.nullNode().getType());
  }

  @Test
  public void testMayBeExpressionBranchCoverage() {
    int[] expressionTokens = new int[] {
        Token.ASSIGN_BITOR, Token.ASSIGN_BITXOR, Token.ASSIGN_BITAND,
        Token.ASSIGN_LSH, Token.ASSIGN_RSH, Token.ASSIGN_URSH,
        Token.ASSIGN_ADD, Token.ASSIGN_SUB, Token.ASSIGN_MUL,
        Token.ASSIGN_DIV, Token.ASSIGN_MOD, Token.BITAND,
        Token.BITOR, Token.BITNOT, Token.BITXOR,
        Token.DEC, Token.DELPROP, Token.DIV,
        Token.GE, Token.GT, Token.IN,
        Token.INC, Token.INSTANCEOF, Token.LE,
        Token.LSH, Token.LT, Token.MOD,
        Token.MUL, Token.NE, Token.RSH,
        Token.SHNE, Token.TYPEOF, Token.URSH
    };

    for (int token : expressionTokens) {
      Node n = new Node(token);
      Node ret = IR.returnNode(n);
      Assert.assertSame(n, ret.getFirstChild());
    }
  }

  @Test
  public void testMayBeStatementBranchCoverage() {
    int[] statementTokens = new int[] {
        Token.CONST, Token.DEBUGGER, Token.WHILE, Token.WITH
    };

    for (int token : statementTokens) {
      Node n = new Node(token);
      Node blk = IR.block(n);
      Assert.assertSame(n, blk.getFirstChild());
    }
  }
}