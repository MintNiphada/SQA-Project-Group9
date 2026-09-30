package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrepareAstTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  @Test
  public void testFreeCallAnnotation() {
    Node nameNode = IR.name("foo");
    Node callNode = IR.call(nameNode);
    Node root = IR.root(IR.exprResult(callNode));

    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Assert.assertTrue(callNode.getBooleanProp(Node.FREE_CALL));
    Assert.assertFalse(nameNode.getBooleanProp(Node.DIRECT_EVAL));
  }

  @Test
  public void testDirectEvalAnnotation() {
    Node evalName = IR.name("eval");
    Node callNode = IR.call(evalName);
    Node root = IR.root(IR.exprResult(callNode));

    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Assert.assertTrue(callNode.getBooleanProp(Node.FREE_CALL));
    Assert.assertTrue(evalName.getBooleanProp(Node.DIRECT_EVAL));
  }

  @Test
  public void testNonFreeCallAnnotation() {
    Node getProp = IR.getprop(IR.name("obj"), IR.string("method"));
    Node callNode = IR.call(getProp);
    Node root = IR.root(IR.exprResult(callNode));

    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Assert.assertFalse(callNode.getBooleanProp(Node.FREE_CALL));
  }

  @Test
  public void testGetElemCallAnnotation() {
    Node getElem = IR.getelem(IR.name("obj"), IR.string("method"));
    Node callNode = IR.call(getElem);
    Node root = IR.root(IR.exprResult(callNode));

    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Assert.assertFalse(callNode.getBooleanProp(Node.FREE_CALL));
  }

  @Test
  public void testObjectLiteralKeyAnnotationPropagation() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordDescription("test doc");
    JSDocInfo jsDocInfo = docBuilder.build(null);

    Node fnNode = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node keyNode = IR.stringKey("myKey", fnNode);
    keyNode.setJSDocInfo(jsDocInfo);

    Node objLit = IR.objectlit(keyNode);
    Node root = IR.root(IR.exprResult(objLit));

    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Assert.assertEquals(jsDocInfo, fnNode.getJSDocInfo());
  }

  @Test
  public void testObjectLiteralKeyWithoutFunctionValue() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordDescription("test doc");
    JSDocInfo jsDocInfo = docBuilder.build(null);

    Node strVal = IR.string("val");
    Node keyNode = IR.stringKey("myKey", strVal);
    keyNode.setJSDocInfo(jsDocInfo);

    Node objLit = IR.objectlit(keyNode);
    Node root = IR.root(IR.exprResult(objLit));

    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Assert.assertNull(strVal.getJSDocInfo());
  }

  @Test
  public void testDispatcherAnnotation() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordJavaDispatch();
    JSDocInfo jsDocInfo = docBuilder.build(null);

    Node fnNode = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node assignNode = IR.assign(IR.name("target"), fnNode);
    assignNode.setJSDocInfo(jsDocInfo);

    Node root = IR.root(IR.exprResult(assignNode));

    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Assert.assertTrue(fnNode.getBooleanProp(Node.IS_DISPATCHER));
  }

  @Test
  public void testDispatcherAnnotationIgnoredWithoutJavaDispatch() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordDescription("normal doc");
    JSDocInfo jsDocInfo = docBuilder.build(null);

    Node fnNode = IR.function(IR.name(""), IR.paramList(), IR.block());
    Node assignNode = IR.assign(IR.name("target"), fnNode);
    assignNode.setJSDocInfo(jsDocInfo);

    Node root = IR.root(IR.exprResult(assignNode));

    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, root);

    Assert.assertFalse(fnNode.getBooleanProp(Node.IS_DISPATCHER));
  }

  @Test
  public void testNormalizeBlocksInIfStatement() {
    Node cond = IR.name("cond");
    Node thenStmt = IR.exprResult(IR.number(1));
    Node ifNode = IR.ifNode(cond, thenStmt);
    Node root = IR.root(ifNode);

    PrepareAst prepareAst = new PrepareAst(compiler, false);
    prepareAst.process(null, root);

    Node child2 = ifNode.getLastChild();
    Assert.assertTrue(child2.isBlock());
    Assert.assertEquals(thenStmt, child2.getFirstChild());
  }

  @Test
  public void testNormalizeBlocksWithEmptyNode() {
    Node cond = IR.name("cond");
    Node thenBlock = IR.block();
    Node emptyElse = IR.empty();
    Node ifNode = new Node(Token.IF, cond, thenBlock, emptyElse);
    Node root = IR.root(ifNode);

    PrepareAst prepareAst = new PrepareAst(compiler, false);
    prepareAst.process(null, root);

    Node elseChild = ifNode.getLastChild();
    Assert.assertTrue(elseChild.isBlock());
    Assert.assertTrue(elseChild.wasEmptyNode());
  }

  @Test
  public void testCheckOnlyModeThrowsOnUnnormalizedTree() {
    Node cond = IR.name("cond");
    Node thenStmt = IR.exprResult(IR.number(1));
    Node ifNode = IR.ifNode(cond, thenStmt);
    Node root = IR.root(ifNode);

    PrepareAst prepareAst = new PrepareAst(compiler, true);
    try {
      prepareAst.process(null, root);
      Assert.fail("Expected IllegalStateException due to normalizeNodeType constraints violation");
    } catch (IllegalStateException e) {
      Assert.assertTrue(e.getMessage().contains("normalizeNodeType constraints violated"));
    }
  }

  @Test
  public void testCheckOnlyModePassesOnNormalizedTree() {
    Node cond = IR.name("cond");
    Node thenBlock = IR.block(IR.exprResult(IR.number(1)));
    Node ifNode = IR.ifNode(cond, thenBlock);
    Node root = IR.root(ifNode);

    PrepareAst prepareAst = new PrepareAst(compiler, true);
    prepareAst.process(null, root);
  }

  @Test
  public void testProcessWithExternsAndRoot() {
    Node externCall = IR.call(IR.name("externFn"));
    Node externRoot = IR.root(IR.exprResult(externCall));

    Node mainCall = IR.call(IR.name("mainFn"));
    Node mainRoot = IR.root(IR.exprResult(mainCall));

    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(externRoot, mainRoot);

    Assert.assertTrue(externCall.getBooleanProp(Node.FREE_CALL));
    Assert.assertTrue(mainCall.getBooleanProp(Node.FREE_CALL));
  }

  @Test
  public void testProcessWithNullExternsAndRoot() {
    PrepareAst prepareAst = new PrepareAst(compiler);
    prepareAst.process(null, null);
  }
}