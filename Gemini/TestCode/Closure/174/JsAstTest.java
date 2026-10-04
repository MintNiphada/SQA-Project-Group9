package com.google.javascript.jscomp;

import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class JsAstTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  @Test
  public void testConstructorAndBasicGetters() {
    SourceFile file = SourceFile.fromCode("test.js", "var a = 1;");
    JsAst ast = new JsAst(file);

    Assert.assertEquals("test.js", ast.getInputId().getIdName());
    Assert.assertSame(file, ast.getSourceFile());
  }

  @Test
  public void testGetAstRootValidCode() {
    SourceFile file = SourceFile.fromCode("test.js", "var x = 10;");
    JsAst ast = new JsAst(file);

    Node root = ast.getAstRoot(compiler);
    Assert.assertNotNull(root);
    Assert.assertTrue(root.isScript());
    Assert.assertEquals(new InputId("test.js"), root.getInputId());
    Assert.assertSame(file, root.getStaticSourceFile());
    Assert.assertSame(root, ast.getAstRoot(compiler));
  }

  @Test
  public void testClearAst() {
    SourceFile file = SourceFile.fromCode("test.js", "var x = 10;");
    JsAst ast = new JsAst(file);

    Node root1 = ast.getAstRoot(compiler);
    Assert.assertNotNull(root1);

    ast.clearAst();

    Node root2 = ast.getAstRoot(compiler);
    Assert.assertNotNull(root2);
    Assert.assertNotSame(root1, root2);
  }

  @Test
  public void testSetSourceFileSuccess() {
    SourceFile file1 = SourceFile.fromCode("test.js", "var x = 1;");
    SourceFile file2 = SourceFile.fromCode("test.js", "var x = 2;");
    JsAst ast = new JsAst(file1);

    ast.setSourceFile(file2);
    Assert.assertSame(file2, ast.getSourceFile());
  }

  @Test(expected = IllegalStateException.class)
  public void testSetSourceFileDifferentName() {
    SourceFile file1 = SourceFile.fromCode("test1.js", "var x = 1;");
    SourceFile file2 = SourceFile.fromCode("test2.js", "var x = 2;");
    JsAst ast = new JsAst(file1);

    ast.setSourceFile(file2);
  }

  @Test
  public void testParseSyntaxErrorCreatesDummyBlock() {
    SourceFile file = SourceFile.fromCode("bad.js", "var = ;");
    JsAst ast = new JsAst(file);

    Node root = ast.getAstRoot(compiler);
    Assert.assertNotNull(root);
    Assert.assertTrue(root.isScript());
    Assert.assertEquals(0, root.getChildCount());
    Assert.assertEquals(new InputId("bad.js"), root.getInputId());
    Assert.assertSame(file, root.getStaticSourceFile());
  }

  @Test
  public void testParseIOExceptionHandling() {
    SourceFile errorFile = new SourceFile("io_error.js") {
      @Override
      public String getCode() throws IOException {
        throw new IOException("Simulated IO Error");
      }
    };
    JsAst ast = new JsAst(errorFile);

    Node root = ast.getAstRoot(compiler);
    Assert.assertNotNull(root);
    Assert.assertTrue(root.isScript());
    Assert.assertEquals(0, root.getChildCount());
    Assert.assertEquals(new InputId("io_error.js"), root.getInputId());
    Assert.assertSame(errorFile, root.getStaticSourceFile());
    Assert.assertEquals(1, compiler.getErrorCount());
  }

  @Test
  public void testParseWithHaltingErrors() {
    SourceFile file = SourceFile.fromCode("test.js", "var x = 1;");
    JsAst ast = new JsAst(file);

    compiler.report(JSError.make(Compiler.OPTIMIZE_LOOP_ERROR, "fatal"));
    Node root = ast.getAstRoot(compiler);

    Assert.assertNotNull(root);
    Assert.assertTrue(root.isScript());
    Assert.assertEquals(new InputId("test.js"), root.getInputId());
    Assert.assertSame(file, root.getStaticSourceFile());
  }
}
