package com.google.javascript.jscomp.parsing;

import com.google.common.collect.Sets;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Visibility;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JsDocInfoParserTest {

  private static class TestErrorReporter implements ErrorReporter {
    final List<String> warnings = new ArrayList<String>();
    final List<String> errors = new ArrayList<String>();

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
    }

    @Override
    public RuntimeException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
      return new RuntimeException(message);
    }

    public boolean hasWarnings() {
      return !warnings.isEmpty();
    }

    public boolean hasErrors() {
      return !errors.isEmpty();
    }
  }

  private JsDocInfoParser createParser(String comment, boolean parseDocumentation, TestErrorReporter errorReporter) {
    Config config = new Config(
        Sets.<String>newHashSet("customTag"),
        Sets.<String>newHashSet("checkVars", "duplicate"),
        parseDocumentation,
        LanguageMode.ECMASCRIPT3,
        false);
    JsDocTokenStream stream = new JsDocTokenStream(comment, 0);
    return new JsDocInfoParser(stream, null, "test.js", config, errorReporter);
  }

  private JSDocInfo parseDoc(String comment, TestErrorReporter errorReporter) {
    JsDocInfoParser parser = createParser(comment, true, errorReporter);
    boolean parsed = parser.parse();
    Assert.assertTrue("Parsing should succeed", parsed);
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  private JSDocInfo parseDocWithoutDoc(String comment, TestErrorReporter errorReporter) {
    JsDocInfoParser parser = createParser(comment, false, errorReporter);
    boolean parsed = parser.parse();
    Assert.assertTrue("Parsing should succeed", parsed);
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  @Test
  public void testParseTypeStringBasicTypes() {
    Node node = JsDocInfoParser.parseTypeString("number");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.STRING, node.getType());
    Assert.assertEquals("number", node.getString());

    node = JsDocInfoParser.parseTypeString("string");
    Assert.assertNotNull(node);
    Assert.assertEquals("string", node.getString());

    node = JsDocInfoParser.parseTypeString("boolean");
    Assert.assertNotNull(node);
    Assert.assertEquals("boolean", node.getString());

    node = JsDocInfoParser.parseTypeString("null");
    Assert.assertNotNull(node);
    Assert.assertEquals("null", node.getString());

    node = JsDocInfoParser.parseTypeString("undefined");
    Assert.assertNotNull(node);
    Assert.assertEquals("undefined", node.getString());

    node = JsDocInfoParser.parseTypeString("*");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.STAR, node.getType());

    node = JsDocInfoParser.parseTypeString("?");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.QMARK, node.getType());
  }

  @Test
  public void testParseTypeStringModifiers() {
    Node node = JsDocInfoParser.parseTypeString("?number");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.QMARK, node.getType());
    Assert.assertEquals("number", node.getFirstChild().getString());

    node = JsDocInfoParser.parseTypeString("!number");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.BANG, node.getType());
    Assert.assertEquals("number", node.getFirstChild().getString());

    node = JsDocInfoParser.parseTypeString("number?");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.QMARK, node.getType());
    Assert.assertEquals("number", node.getFirstChild().getString());

    node = JsDocInfoParser.parseTypeString("number!");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.BANG, node.getType());
    Assert.assertEquals("number", node.getFirstChild().getString());
  }

  @Test
  public void testParseTypeStringUnion() {
    Node node = JsDocInfoParser.parseTypeString("(number|string)");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.PIPE, node.getType());
    Assert.assertEquals(2, node.getChildCount());

    node = JsDocInfoParser.parseTypeString("number|string");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.PIPE, node.getType());

    node = JsDocInfoParser.parseTypeString("number||string");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.PIPE, node.getType());

    node = JsDocInfoParser.parseTypeString("(number,string)");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.PIPE, node.getType());
  }

  @Test
  public void testParseTypeStringGenerics() {
    Node node = JsDocInfoParser.parseTypeString("Array.<string>");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.STRING, node.getType());
    Assert.assertEquals("Array", node.getString());
    Assert.assertTrue(node.hasChildren());
    Assert.assertEquals(Token.BLOCK, node.getFirstChild().getType());

    node = JsDocInfoParser.parseTypeString("Object.<string, number>");
    Assert.assertNotNull(node);
    Assert.assertEquals("Object", node.getString());
    Assert.assertEquals(2, node.getFirstChild().getChildCount());
  }

  @Test
  public void testParseTypeStringArray() {
    Node node = JsDocInfoParser.parseTypeString("[number, string]");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.LB, node.getType());
    Assert.assertEquals(2, node.getChildCount());

    node = JsDocInfoParser.parseTypeString("[...number]");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.LB, node.getType());
    Assert.assertEquals(Token.ELLIPSIS, node.getFirstChild().getType());
  }

  @Test
  public void testParseTypeStringRecord() {
    Node node = JsDocInfoParser.parseTypeString("{a: number, b: string}");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.LC, node.getType());
    Assert.assertEquals(Token.LB, node.getFirstChild().getType());
    Assert.assertEquals(2, node.getFirstChild().getChildCount());

    node = JsDocInfoParser.parseTypeString("{a}");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.LC, node.getType());
  }

  @Test
  public void testParseTypeStringFunction() {
    Node node = JsDocInfoParser.parseTypeString("function(): void");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.FUNCTION, node.getType());

    node = JsDocInfoParser.parseTypeString("function(number, string): boolean");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.FUNCTION, node.getType());

    node = JsDocInfoParser.parseTypeString("function(this:Object, number): boolean");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.FUNCTION, node.getType());
    Assert.assertEquals(Token.THIS, node.getFirstChild().getType());

    node = JsDocInfoParser.parseTypeString("function(new:Object, number): boolean");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.FUNCTION, node.getType());
    Assert.assertEquals(Token.NEW, node.getFirstChild().getType());

    node = JsDocInfoParser.parseTypeString("function(this:Object)");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.FUNCTION, node.getType());

    node = JsDocInfoParser.parseTypeString("function(...number)");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.FUNCTION, node.getType());

    node = JsDocInfoParser.parseTypeString("function(...[number])");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.FUNCTION, node.getType());

    node = JsDocInfoParser.parseTypeString("function(number=)");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.FUNCTION, node.getType());
  }

  @Test
  public void testParseTypeStringInvalid() {
    Assert.assertNull(JsDocInfoParser.parseTypeString("Array.<"));
    Assert.assertNull(JsDocInfoParser.parseTypeString("{"));
    Assert.assertNull(JsDocInfoParser.parseTypeString("["));
    Assert.assertNull(JsDocInfoParser.parseTypeString("function("));
    Assert.assertNull(JsDocInfoParser.parseTypeString("function(this)"));
  }

  @Test
  public void testParseAuthorAndSee() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n * @author John Doe\n * @see http://example.com\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.getAuthors().contains("John Doe"));
    Assert.assertTrue(info.getReferences().contains("http://example.com"));
    Assert.assertFalse(reporter.hasWarnings());
  }

  @Test
  public void testParseConstConstructorInterface() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n * @const\n * @constructor\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isConstant());
    Assert.assertTrue(info.isConstructor());

    reporter = new TestErrorReporter();
    info = parseDoc("/**\n * @interface\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isInterface());
  }

  @Test
  public void testParseDeprecatedAndDesc() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n * @deprecated Use newMethod instead\n * @desc Description here\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isDeprecated());
    Assert.assertEquals("Use newMethod instead", info.getDeprecationReason());
    Assert.assertEquals("Description here", info.getDescription());
  }

  @Test
  public void testParseEnum() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n * @enum {string}\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getEnumParameterType());

    reporter = new TestErrorReporter();
    info = parseDoc("/**\n * @enum\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getEnumParameterType());
  }

  @Test
  public void testParseExtendsImplements() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n * @constructor\n * @extends {Base}\n * @implements {Interface1}\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getBaseType());
    Assert.assertEquals(1, info.getImplementedInterfacesCount());

    reporter = new TestErrorReporter();
    info = parseDoc("/**\n * @interface\n * @extends {BaseInterface}\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(1, info.getExtendedInterfacesCount());
  }

  @Test
  public void testParseLends() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n * @lends {MyClass.prototype}\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals("MyClass.prototype", info.getLendsName());

    reporter = new TestErrorReporter();
    info = parseDoc("/**\n * @lends MyClass.prototype\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals("MyClass.prototype", info.getLendsName());
  }

  @Test
  public void testParseParamAndReturn() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n * @param {string} p1 Description of p1\n * @param {[string]=} [p2] Optional p2\n * @return {boolean} Description of return\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.hasParameter("p1"));
    Assert.assertTrue(info.hasParameter("p2"));
    Assert.assertNotNull(info.getParameterType("p1"));
    Assert.assertEquals("Description of p1", info.getParameterDescription("p1"));
    Assert.assertEquals("Optional p2", info.getParameterDescription("p2"));
    Assert.assertNotNull(info.getReturnType());
    Assert.assertEquals("Description of return", info.getReturnDescription());
  }

  @Test
  public void testParseThrows() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n * @throws {Error} If something goes wrong\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(1, info.getThrownTypes().size());
    Assert.assertEquals("If something goes wrong", info.getThrowsDescription(info.getThrownTypes().get(0)));
  }

  @Test
  public void testParseVisibility() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/** @private */", reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(Visibility.PRIVATE, info.getVisibility());

    reporter = new TestErrorReporter();
    info = parseDoc("/** @protected */", reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(Visibility.PROTECTED, info.getVisibility());

    reporter = new TestErrorReporter();
    info = parseDoc("/** @public */", reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(Visibility.PUBLIC, info.getVisibility());
  }

  @Test
  public void testParseModifiersAndFlags() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n"
        + " * @export\n"
        + " * @externs\n"
        + " * @javadispatch\n"
        + " * @hidden\n"
        + " * @noalias\n"
        + " * @nocompile\n"
        + " * @nocheck\n"
        + " * @override\n"
        + " * @preservertry\n"
        + " * @nosideeffects\n"
        + " * @implicitCast\n"
        + " * @meaning Some special meaning\n"
        + " * @version 1.0.0\n"
        + " * @template T\n"
        + " */", reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isExport());
    Assert.assertTrue(info.isExterns());
    Assert.assertTrue(info.isJavaDispatch());
    Assert.assertTrue(info.isHidden());
    Assert.assertTrue(info.isNoAlias());
    Assert.assertTrue(info.isNoCompile());
    Assert.assertTrue(info.isNoTypeCheck());
    Assert.assertTrue(info.isOverride());
    Assert.assertTrue(info.shouldPreserveTry());
    Assert.assertTrue(info.hasNoSideEffects());
    Assert.assertTrue(info.isImplicitCast());
    Assert.assertEquals("Some special meaning", info.getMeaning());
    Assert.assertEquals("1.0.0", info.getVersion());
    Assert.assertTrue(info.getTemplateTypeNames().contains("T"));
  }

  @Test
  public void testParseSuppressAndModifies() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n"
        + " * @param {Object} x\n"
        + " * @suppress {checkVars|duplicate}\n"
        + " * @modifies {this|arguments|x}\n"
        + " */", reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.getSuppressions().contains("checkVars"));
    Assert.assertTrue(info.getSuppressions().contains("duplicate"));
    Assert.assertTrue(info.getModifies().contains("this"));
    Assert.assertTrue(info.getModifies().contains("arguments"));
    Assert.assertTrue(info.getModifies().contains("x"));
  }

  @Test
  public void testParseTypedefAndDefineAndThisAndType() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/** @typedef {string|number} */", reporter);
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getTypedefType());

    reporter = new TestErrorReporter();
    info = parseDoc("/** @define {boolean} */", reporter);
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getBaseType());

    reporter = new TestErrorReporter();
    info = parseDoc("/** @this {Object} */", reporter);
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getThisType());

    reporter = new TestErrorReporter();
    info = parseDoc("/** @type {string} */", reporter);
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getType());
  }

  @Test
  public void testParseFileOverviewAndLicense() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = createParser("/**\n * @fileoverview File description\n * @license MIT License\n */", true, reporter);
    Node.FileLevelJsDocBuilder fileLevelBuilder = new Node.FileLevelJsDocBuilder();
    parser.setFileLevelJsDocBuilder(fileLevelBuilder);
    boolean parsed = parser.parse();
    Assert.assertTrue(parsed);
    JSDocInfo fileInfo = parser.getFileOverviewJSDocInfo();
    Assert.assertNotNull(fileInfo);
    Assert.assertEquals("File description", fileInfo.getFileOverview());
  }

  @Test
  public void testParseBlockDescription() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDoc("/**\n * This is a block description.\n * Line 2 of description.\n * @type {number}\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.getBlockDescription().contains("This is a block description."));
  }

  @Test
  public void testParseWithoutDocumentationParsing() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parseDocWithoutDoc("/**\n * Description ignored\n * @author IgnoreMe\n * @see IgnoreMe\n * @return {number} Return desc\n */", reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.getAuthors().isEmpty());
    Assert.assertTrue(info.getReferences().isEmpty());
    Assert.assertNotNull(info.getReturnType());
    Assert.assertNull(info.getReturnDescription());
  }

  @Test
  public void testParseWarnings() {
    TestErrorReporter reporter = new TestErrorReporter();
    parseDoc("/**\n * @unknownTag\n * @author\n * @see\n * @template\n * @version\n * @version 2.0\n */", reporter);
    Assert.assertTrue(reporter.hasWarnings());

    TestErrorReporter reporter2 = new TestErrorReporter();
    parseDoc("/**\n * @constructor\n * @interface\n */", reporter2);
    Assert.assertTrue(reporter2.hasWarnings());

    TestErrorReporter reporter3 = new TestErrorReporter();
    parseDoc("/**\n * @param {string} [p=\n */", reporter3);
    Assert.assertTrue(reporter3.hasWarnings());
  }
}