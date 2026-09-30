package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

public class MakeDeclaredNamesUniqueTest {

  @Test
  public void testContextualRenamerBasic() {
    MakeDeclaredNamesUnique.ContextualRenamer renamer =
        new MakeDeclaredNamesUnique.ContextualRenamer();
    Assert.assertFalse(renamer.stripConstIfReplaced());

    // In global scope, reserve names
    renamer.addDeclaredName("a");
    renamer.addDeclaredName("arguments"); // arguments should be ignored
    Assert.assertNull(renamer.getReplacementName("a"));
    Assert.assertNull(renamer.getReplacementName("arguments"));

    // Child scope
    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    Assert.assertFalse(child.stripConstIfReplaced());
    child.addDeclaredName("arguments"); // should be ignored
    Assert.assertNull(child.getReplacementName("arguments"));

    child.addDeclaredName("a");
    Assert.assertEquals("a$$1", child.getReplacementName("a"));

    // Adding same name again in child scope shouldn't change replacement
    child.addDeclaredName("a");
    Assert.assertEquals("a$$1", child.getReplacementName("a"));

    // Second child scope
    MakeDeclaredNamesUnique.Renamer child2 = renamer.forChildScope();
    child2.addDeclaredName("a");
    Assert.assertEquals("a$$2", child2.getReplacementName("a"));

    // A brand new name in child scope (not seen globally)
    child2.addDeclaredName("b");
    Assert.assertNull(child2.getReplacementName("b"));

    // Another child of child2 declaring 'b'
    MakeDeclaredNamesUnique.Renamer child3 = child2.forChildScope();
    child3.addDeclaredName("b");
    Assert.assertEquals("b$$1", child3.getReplacementName("b"));
  }

  @Test
  public void testInlineRenamer() {
    Supplier<String> supplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };

    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "inline_", true);
    Assert.assertTrue(renamer.stripConstIfReplaced());

    renamer.addDeclaredName("foo");
    Assert.assertEquals("foo$$inline_0", renamer.getReplacementName("foo"));

    // Adding again should not overwrite
    renamer.addDeclaredName("foo");
    Assert.assertEquals("foo$$inline_0", renamer.getReplacementName("foo"));

    // Name with existing separator
    renamer.addDeclaredName("bar$$old");
    Assert.assertEquals("bar$$inline_1", renamer.getReplacementName("bar$$old"));

    // Empty name
    renamer.addDeclaredName("");
    Assert.assertEquals("", renamer.getReplacementName(""));

    // Child scope
    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    Assert.assertTrue(child.stripConstIfReplaced());
    child.addDeclaredName("baz");
    Assert.assertEquals("baz$$inline_2", child.getReplacementName("baz"));
  }

  @Test(expected = IllegalStateException.class)
  public void testInlineRenamerArgumentsThrows() {
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return "1";
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "pre_", false);
    renamer.addDeclaredName("arguments");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInlineRenamerEmptyPrefixThrows() {
    Supplier<String> supplier = new Supplier<String>() {
      @Override
      public String get() {
        return "1";
      }
    };
    new MakeDeclaredNamesUnique.InlineRenamer(supplier, "", false);
  }

  @Test
  public void testBoilerplateRenamer() {
    Supplier<String> supplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };
    MakeDeclaredNamesUnique.BoilerplateRenamer renamer =
        new MakeDeclaredNamesUnique.BoilerplateRenamer(supplier, "bp_");

    // Root is global contextual renamer
    renamer.addDeclaredName("globalVar");
    Assert.assertNull(renamer.getReplacementName("globalVar"));

    MakeDeclaredNamesUnique.Renamer child = renamer.forChildScope();
    Assert.assertFalse(child.stripConstIfReplaced());
    child.addDeclaredName("localVar");
    Assert.assertEquals("localVar$$bp_0", child.getReplacementName("localVar"));
  }

  @Test
  public void testContextualRenameInverterStaticMethods() {
    Assert.assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo$$123"));
    Assert.assertEquals("foo", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo"));
    Assert.assertEquals("foo$$bar", MakeDeclaredNamesUnique.ContextualRenameInverter.getOrginalName("foo$$bar$$1"));
  }

  @Test
  public void testTreeTraversalWithContextualRenamer() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    // Create AST:
    // var a = 1;
    // function f(a) { var a = 2; return a; }
    // function g() { try {} catch (e) { var e = 3; } }
    // (function h() { return h; })();
    String js = "var a = 1; function f(a) { var a = 2; return a; } " +
                "function g() { try {} catch (e) { var e = 3; } } " +
                "(function h() { return h; })();";
    Node root = compiler.parseTestCode(js);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique();
    NodeTraversal.traverse(compiler, root, pass);

    // Verify pass executed without throwing exceptions
    Assert.assertNotNull(root);
  }

  @Test
  public void testTreeTraversalWithInlineRenamer() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "var CONST_VAL = 10; function f(x) { var y = CONST_VAL + x; return y; }";
    Node root = compiler.parseTestCode(js);

    Supplier<String> supplier = new Supplier<String>() {
      private int count = 0;
      @Override
      public String get() {
        return String.valueOf(count++);
      }
    };
    MakeDeclaredNamesUnique.InlineRenamer renamer =
        new MakeDeclaredNamesUnique.InlineRenamer(supplier, "unique_", true);

    MakeDeclaredNamesUnique pass = new MakeDeclaredNamesUnique(renamer);
    NodeTraversal.traverse(compiler, root, pass);

    Assert.assertNotNull(root);
  }

  @Test
  public void testContextualRenameInverterProcess() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    String js = "var a = 1; function f() { var a$$1 = 2; return a$$1; }";
    Node root = compiler.parseTestCode(js);

    CompilerPass inverter = MakeDeclaredNamesUnique.getContextualRenameInverter(compiler);
    inverter.process(null, root);

    Assert.assertNotNull(root);
  }

  @Test
  public void testArgumentsConstant() {
    Assert.assertEquals("arguments", MakeDeclaredNamesUnique.ARGUMENTS);
  }
}