package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class MakeDeclaredNamesUniqueTest {

  @Test
  public void testContextualRenamerGlobalScope() {
    MakeDeclaredNamesUnique.ContextualRenamer renamer = new MakeDeclaredNamesUnique.ContextualRenamer();
    Assert.assertFalse(renamer.stripConstIfReplaced());

    renamer.addDeclaredName("a");
    Assert.assertNull(renamer.getReplacementName("a"));
    Assert.assertNull(renamer.getReplacementName("undeclared"));

    MakeDeclaredNamesUnique.Renamer childRenamer1 = renamer.forChildScope();
    Assert.assertFalse(childRenamer1.stripConstIfReplaced());

    // First local declaration of 'a' in child scope will get id 1 because 'a' was reserved in global
    childRenamer1.addDeclaredName("a");
    Assert.assertEquals("a$$1", childRenamer1.getReplacementName("a"));

    // Multiple calls with same name in same local scope should not increment id again
    childRenamer1.addDeclaredName("a");
    Assert.assertEquals("a$$1", childRenamer1.getReplacementName("a"));

    // First local declaration of 'b' (not reserved globally) gets id 0, so replacement is null
    childRenamer1.addDeclaredName("b");
    Assert.assertNull(childRenamer1.getReplacementName("b"));

    MakeDeclaredNamesUnique.Renamer childRenamer2 = childRenamer1.forChildScope();
    childRenamer2.addDeclaredName("a");
    Assert.assertEquals("a$$2", childRenamer2.getReplacementName("a"));

    childRenamer2.addDeclaredName("b");
    Assert.assertEquals("b$$1", childRenamer2.getReplacementName("b"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInlineRenamerEmptyPrefixThrows() {
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return "1";
      }
    };
    new MakeDeclaredNamesUnique.InlineRenamer(supplier, "", true);
  }

  @Test
  public void testInlineRenamer() {
    final int[] counter = new int[]{0};
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return String.valueOf(++counter[0]);
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "inline_", true);
    Assert.assertTrue(renamer.stripConstIfReplaced());

    renamer.addDeclaredName("x");
    Assert.assertEquals("x$$inline_1", renamer.getReplacementName("x"));
    // Calling addDeclaredName with the same name again should not re-generate
    renamer.addDeclaredName("x");
    Assert.assertEquals("x$$inline_1", renamer.getReplacementName("x"));

    // Empty name
    renamer.addDeclaredName("");
    Assert.assertEquals("", renamer.getReplacementName(""));

    // Name that already contains separator
    renamer.addDeclaredName("prev$$old");
    Assert.assertEquals("prev$$inline_2", renamer.getReplacementName("prev$$old"));

    // Child scope
    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    Assert.assertTrue(child.stripConstIfReplaced());
    child.addDeclaredName("childVar");
    Assert.assertEquals("childVar$$inline_3", child.getReplacementName("childVar"));

    MakeDeclaredNamesUnique.InlineRenamer nonConstStripping =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "p_", false);
    Assert.assertFalse(nonConstStripping.stripConstIfReplaced());
  }

  @Test
  public void testContextualRenameInverterGetOriginalName() {
    Assert.assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo"));
    Assert.assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo$$1"));
    Assert.assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo$$inline_123"));
    Assert.assertEquals("bar$$1", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("bar$$1$$2"));
  }

  @Test
  public void testMakeDeclaredNamesUniqueWithContextualRenamer() {
    Compiler compiler = new Compiler();
    String js = "var a = 1; function f(a) { var a = 2; return a; } function g() { try { var a = 3; } catch (a) { a = 4; } }";
    Node root = compiler.parseTestCode(js);
    NodeTraversal.traverse(compiler, root, new MakeDeclaredNamesUnique());

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(new Node(Token.BLOCK), root);
    Assert.assertNotNull(root);
  }

  @Test
  public void testMakeDeclaredNamesUniqueWithInlineRenamer() {
    Compiler compiler = new Compiler();
    String js = "var CONST_VAL = 10; function foo(a) { var b = function bar(c) { return CONST_VAL + a + b + c; }; return bar(1); }";
    Node root = compiler.parseTestCode(js);

    final int[] id = new int[]{0};
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return String.valueOf(++id[0]);
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "test_", true);
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(renamer);
    NodeTraversal.traverse(compiler, root, pass);

    Assert.assertNotNull(root);
  }

  @Test
  public void testContextualRenameInverterInversion() {
    Compiler compiler = new Compiler();
    String js = "function f() { var a$$1 = 1; return a$$1; }";
    Node root = compiler.parseTestCode(js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    Node fn = root.getFirstChild();
    Node fnBody = fn.getLastChild();
    Node varNode = fnBody.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Assert.assertEquals("a", nameNode.getString());
  }

  @Test
  public void testNamedFunctionExpressionRecursion() {
    Compiler compiler = new Compiler();
    String js = "var f = function rec(x) { if (x <= 0) return 1; return rec(x - 1); };";
    Node root = compiler.parseTestCode(js);

    final int[] id = new int[]{0};
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return String.valueOf(++id[0]);
      }
    };
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "rec_", false));
    NodeTraversal.traverse(compiler, root, pass);

    Assert.assertNotNull(root);
  }

  @Test
  public void testCatchBlockRenaming() {
    Compiler compiler = new Compiler();
    String js = "function test() { try {} catch (e) { var e = 1; } }";
    Node root = compiler.parseTestCode(js);

    final int[] id = new int[]{0};
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return String.valueOf(++id[0]);
      }
    };
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "catch_", false));
    NodeTraversal.traverse(compiler, root, pass);

    Assert.assertNotNull(root);
  }

  @Test
  public void testConstantRemovalWhenReplaced() {
    Compiler compiler = new Compiler();
    String js = "function test() { var CONST_A = 1; return CONST_A; }";
    Node root = compiler.parseTestCode(js);

    Node fn = root.getFirstChild();
    Node fnBody = fn.getLastChild();
    Node varNode = fnBody.getFirstChild();
    Node constNameNode = varNode.getFirstChild();
    constNameNode.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    Assert.assertTrue(constNameNode.getBooleanProp(Node.IS_CONSTANT_NAME));

    final int[] id = new int[]{0};
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return String.valueOf(++id[0]);
      }
    };
    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "const_", true));
    NodeTraversal.traverse(compiler, root, pass);

    Assert.assertFalse(constNameNode.getBooleanProp(Node.IS_CONSTANT_NAME));
  }

  @Test
  public void testContextualRenameInverterConflictPrevented() {
    Compiler compiler = new Compiler();
    String js = "var a = 1; function f() { var a$$1 = 2; return a + a$$1; }";
    Node root = compiler.parseTestCode(js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    Node fn = root.getFirstChild().getNext();
    Node fnBody = fn.getLastChild();
    Node varNode = fnBody.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    // Cannot invert 'a$$1' to 'a' because 'a' exists in the outer scope
    Assert.assertEquals("a$$1", nameNode.getString());
  }

  @Test
  public void testContextualRenameInverterNonNumericSuffix() {
    Compiler compiler = new Compiler();
    String js = "function f() { var a$$inline_1 = 1; return a$$inline_1; }";
    Node root = compiler.parseTestCode(js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    Node fn = root.getFirstChild();
    Node fnBody = fn.getLastChild();
    Node varNode = fnBody.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    Assert.assertEquals("a", nameNode.getString());
  }

  @Test
  public void testInvalidJsIdentifierNotInverted() {
    Compiler compiler = new Compiler();
    String js = "function f() { var var$$1 = 1; return var$$1; }";
    Node root = compiler.parseTestCode(js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    Node fn = root.getFirstChild();
    Node fnBody = fn.getLastChild();
    Node varNode = fnBody.getFirstChild();
    Node nameNode = varNode.getFirstChild();
    // "var" is a keyword and not a valid identifier
    Assert.assertEquals("var$$1", nameNode.getString());
  }
}