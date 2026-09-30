package com.google.javascript.jscomp.parsing;

import com.google.common.collect.Sets;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.ast.Comment;
import com.google.javascript.rhino.jstype.SimpleSourceFile;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsDocInfoParserTest {

  private static class TestErrorReporter implements ErrorReporter {
    private final List<String> warnings = new ArrayList<String>();
    private final List<String> errors = new ArrayList<String>();

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
  }

  private JsDocInfoParser createParser(String comment, boolean parseDocs, TestErrorReporter reporter) {
    Config config = ParserRunner.createConfig(
        true,
        LanguageMode.ECMASCRIPT5,
        parseDocs,
        Sets.newHashSet("checkVars", "duplicate", "extra"));
    JsDocTokenStream stream = new JsDocTokenStream(comment);
    Comment commentNode = new Comment(0, comment.length(), com.google.javascript.rhino.head.Token.CommentType.JSDOC, comment);
    Node associatedNode = new Node(Token.SCRIPT);
    StaticSourceFile sourceFile = new SimpleSourceFile("testcode", false);
    associatedNode.setStaticSourceFile(sourceFile);

    return new JsDocInfoParser(stream, commentNode, associatedNode, config, reporter);
  }

  private JSDocInfo parse(String comment) {
    return parse(comment, true, new TestErrorReporter());
  }

  private JSDocInfo parse(String comment, boolean parseDocs, TestErrorReporter reporter) {
    JsDocInfoParser parser = createParser(comment, parseDocs, reporter);
    boolean success = parser.parse();
    Assert.assertTrue("Parser should succeed for: " + comment, success);
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  @Test
  public void testParseTypeString() {
    Node typeNode = JsDocInfoParser.parseTypeString("number");
    Assert.assertNotNull(typeNode);
    Assert.assertEquals(Token.STRING, typeNode.getType());
    Assert.assertEquals("number", typeNode.getString());

    Node unionNode = JsDocInfoParser.parseTypeString("(number|string)");
    Assert.assertNotNull(unionNode);
    Assert.assertEquals(Token.PIPE, unionNode.getType());

    Node nullNode = JsDocInfoParser.parseTypeString("null");
    Assert.assertNotNull(nullNode);
    Assert.assertEquals(Token.STRING, nullNode.getType());
    Assert.assertEquals("null", nullNode.getString());

    Node undefinedNode = JsDocInfoParser.parseTypeString("undefined");
    Assert.assertNotNull(undefinedNode);
    Assert.assertEquals(Token.STRING, undefinedNode.getString());

    Node starNode = JsDocInfoParser.parseTypeString("*");
    Assert.assertNotNull(starNode);
    Assert.assertEquals(Token.STAR, starNode.getType());

    Node invalid = JsDocInfoParser.parseTypeString("{bad");
    Assert.assertNull(invalid);
  }

  @Test
  public void testParseInlineTypeDoc() {
    Config config = ParserRunner.createConfig(
        true, LanguageMode.ECMASCRIPT5, false, Collections.<String>emptySet());
    JsDocTokenStream stream = new JsDocTokenStream("number */");
    JsDocInfoParser parser = new JsDocInfoParser(
        stream, null, null, config, NullErrorReporter.forNewRhino());
    JSDocInfo info = parser.parseInlineTypeDoc();
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getType());
  }

  @Test
  public void testBlockCommentsAndDescriptions() {
    JSDocInfo info = parse("/**\n * This is a block description.\n * @param {string} x\n */");
    Assert.assertNotNull(info);
    Assert.assertEquals("This is a block description.", info.getBlockDescription());
    Assert.assertTrue(info.hasParameter("x"));
  }

  @Test
  public void testNgInject() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @ngInject */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isNgInject());
    Assert.assertFalse(reporter.hasWarnings());

    parse("/** @ngInject\n * @ngInject */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testAuthor() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @author John Doe */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(1, info.getAuthors().size());
    Assert.assertTrue(info.getAuthors().contains("John Doe"));

    parse("/** @author */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testConsistentIdGenerator() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @consistentIdGenerator */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isConsistentIdGenerator());

    parse("/** @consistentIdGenerator\n * @consistentIdGenerator */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testStructAndDict() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @struct */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.makesStructs());

    JSDocInfo dictInfo = parse("/** @dict */", true, reporter);
    Assert.assertNotNull(dictInfo);
    Assert.assertTrue(dictInfo.makesDicts());

    parse("/** @struct\n * @dict */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testConstructorAndInterface() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @constructor */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isConstructor());

    JSDocInfo iface = parse("/** @interface */", true, reporter);
    Assert.assertNotNull(iface);
    Assert.assertTrue(iface.isInterface());

    parse("/** @constructor\n * @interface */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testDeprecated() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @deprecated Use newMethod instead. */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isDeprecated());
    Assert.assertEquals("Use newMethod instead.", info.getDeprecationReason());

    parse("/** @deprecated reason1\n * @deprecated reason2 */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testDescAndMeaning() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @desc Hello world\n * @meaning greeting */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals("Hello world", info.getDescription());
    Assert.assertEquals("greeting", info.getMeaning());

    parse("/** @desc first\n * @desc second */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());

    parse("/** @meaning first\n * @meaning second */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testFileOverview() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = createParser("/** @fileoverview Provides math utils. */", true, reporter);
    parser.parse();
    JSDocInfo info = parser.getFileOverviewJSDocInfo();
    Assert.assertNotNull(info);
    Assert.assertEquals("Provides math utils.", info.getFileOverview());

    parser = createParser("/** @fileoverview first\n * @fileoverview second */", true, reporter);
    parser.parse();
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testLicenseAndPreserve() {
    TestErrorReporter reporter = new TestErrorReporter();
    JsDocInfoParser parser = createParser("/** @license Copyright 2023 */", true, reporter);
    Node.FileLevelJsDocBuilder fileDocBuilder = new Node.FileLevelJsDocBuilder();
    parser.setFileLevelJsDocBuilder(fileDocBuilder);
    parser.parse();
    Assert.assertTrue(fileDocBuilder.toString().contains("Copyright 2023"));
  }

  @Test
  public void testEnum() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @enum {string} */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getEnumParameterType());

    JSDocInfo defaultEnum = parse("/** @enum */", true, reporter);
    Assert.assertNotNull(defaultEnum);
    Assert.assertNotNull(defaultEnum.getEnumParameterType());

    parse("/** @enum {string}\n * @enum {number} */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testExportExposeExternsJavaDispatch() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @export\n * @expose\n * @externs\n * @javadispatch */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isExport());
    Assert.assertTrue(info.isExpose());

    parse("/** @export\n * @export */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testExtendsAndImplements() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @extends {SuperClass}\n * @implements {InterfaceA}\n * @implements {InterfaceB} */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(1, info.getBaseTypeCount());
    Assert.assertEquals(2, info.getImplementedInterfaceCount());

    parse("/** @extends {SuperClass}\n * @extends {AnotherClass} */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());

    parse("/** @implements {InterfaceA}\n * @implements {InterfaceA} */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());

    parse("/** @extends */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testLends() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @lends {MyClass.prototype} */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals("MyClass.prototype", info.getLendsName());

    parse("/** @lends */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testDirectivesNoAliasNoCompileNoCheck() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @noalias\n * @nocompile\n * @nocheck\n * @override\n * @hidden */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isNoAlias());
    Assert.assertTrue(info.isNoCompile());
    Assert.assertTrue(info.isNoTypeCheck());
    Assert.assertTrue(info.isOverride());
    Assert.assertTrue(info.isHidden());
  }

  @Test
  public void testThrows() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @throws {Error} When bad input occurs. */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(1, info.getThrownTypes().size());
  }

  @Test
  public void testParamAnnotations() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/**\n * @param {string} a Description for a\n * @param {number=} opt_b\n * @param {...boolean} var_args\n * @param [c=defaultVal]\n */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.hasParameter("a"));
    Assert.assertTrue(info.hasParameter("opt_b"));
    Assert.assertTrue(info.hasParameter("var_args"));
    Assert.assertTrue(info.hasParameter("c"));
    Assert.assertEquals("Description for a", info.getParameterDescription("a"));

    parse("/** @param {string} a\n * @param {number} a */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());

    parse("/** @param */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testPreserveTryNoShadowNoSideEffectsImplicitCast() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @preserveTry\n * @noshadow\n * @nosideeffects\n * @implicitCast\n * @idGenerator\n * @stableIdGenerator */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.hasPreserveTry());
    Assert.assertTrue(info.isNoShadow());
    Assert.assertTrue(info.hasNoSideEffects());
    Assert.assertTrue(info.isImplicitCast());
    Assert.assertTrue(info.isIdGenerator());
    Assert.assertTrue(info.isStableIdGenerator());
  }

  @Test
  public void testModifiesAndSuppress() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @param {Object} obj\n * @modifies {this|arguments|obj}\n * @suppress {checkVars|duplicate} */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(info.getModifies().contains("this"));
    Assert.assertTrue(info.getModifies().contains("arguments"));
    Assert.assertTrue(info.getModifies().contains("obj"));
    Assert.assertTrue(info.getSuppressions().contains("checkVars"));
    Assert.assertTrue(info.getSuppressions().contains("duplicate"));

    parse("/** @modifies {unknown} */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());

    parse("/** @suppress {unknownWarning} */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testTemplateAndClassTemplate() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @template T, U\n * @classTemplate V, W */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(2, info.getTemplateTypeNames().size());
    Assert.assertEquals(2, info.getClassTemplateTypeNames().size());

    parse("/** @template\n * @template T */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());

    parse("/** @classTemplate\n * @classTemplate T */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testSeeAndVersion() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @see http://example.com\n * @version 1.0.0 */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(1, info.getReferences().size());
    Assert.assertEquals("1.0.0", info.getVersion());

    parse("/** @see */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());

    parse("/** @version */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testVisibilityAndConstAndDefine() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @private {string}\n * @const */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertEquals(JSDocInfo.Visibility.PRIVATE, info.getVisibility());
    Assert.assertTrue(info.isConstant());

    JSDocInfo pubInfo = parse("/** @public */", true, reporter);
    Assert.assertNotNull(pubInfo);
    Assert.assertEquals(JSDocInfo.Visibility.PUBLIC, pubInfo.getVisibility());

    JSDocInfo protInfo = parse("/** @protected */", true, reporter);
    Assert.assertNotNull(protInfo);
    Assert.assertEquals(JSDocInfo.Visibility.PROTECTED, protInfo.getVisibility());

    JSDocInfo defInfo = parse("/** @define {boolean} */", true, reporter);
    Assert.assertNotNull(defInfo);
    Assert.assertNotNull(defInfo.getType());
  }

  @Test
  public void testReturnThisTypedef() {
    TestErrorReporter reporter = new TestErrorReporter();
    JSDocInfo info = parse("/** @this {Object}\n * @return {number} Return count.\n * @typedef {string|number} */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getThisType());
    Assert.assertNotNull(info.getReturnType());
    Assert.assertEquals("Return count.", info.getReturnDescription());
    Assert.assertNotNull(info.getTypedefType());
  }

  @Test
  public void testComplexTypesParsing() {
    Node recordType = JsDocInfoParser.parseTypeString("{a: number, b: string}");
    Assert.assertNotNull(recordType);
    Assert.assertEquals(Token.LC, recordType.getType());

    Node arrayType = JsDocInfoParser.parseTypeString("[number, ...string]");
    Assert.assertNotNull(arrayType);
    Assert.assertEquals(Token.LB, arrayType.getType());

    Node funcType = JsDocInfoParser.parseTypeString("function(this:Object, string, ...[number]): void");
    Assert.assertNotNull(funcType);
    Assert.assertEquals(Token.FUNCTION, funcType.getType());

    Node newFuncType = JsDocInfoParser.parseTypeString("function(new:Array, number): boolean");
    Assert.assertNotNull(newFuncType);
    Assert.assertEquals(Token.FUNCTION, newFuncType.getType());

    Node typeApp = JsDocInfoParser.parseTypeString("Array.<string, number>");
    Assert.assertNotNull(typeApp);
    Assert.assertEquals("Array", typeApp.getString());

    Node qmarkType = JsDocInfoParser.parseTypeString("?number");
    Assert.assertNotNull(qmarkType);
    Assert.assertEquals(Token.QMARK, qmarkType.getType());

    Node bangType = JsDocInfoParser.parseTypeString("!number");
    Assert.assertNotNull(bangType);
    Assert.assertEquals(Token.BANG, bangType.getType());

    Node unknownType = JsDocInfoParser.parseTypeString("?");
    Assert.assertNotNull(unknownType);
    Assert.assertEquals(Token.QMARK, unknownType.getType());
  }

  @Test
  public void testBadJsdocTagWarning() {
    TestErrorReporter reporter = new TestErrorReporter();
    parse("/** @unknownTag */", true, reporter);
    Assert.assertTrue(reporter.hasWarnings());
  }

  @Test
  public void testUnexpectedEOF() {
    TestErrorReporter reporter = new TestErrorReporter();
    Config config = ParserRunner.createConfig(
        true, LanguageMode.ECMASCRIPT5, false, Collections.<String>emptySet());
    JsDocTokenStream stream = new JsDocTokenStream("/** @param {string} x");
    JsDocInfoParser parser = new JsDocInfoParser(
        stream, null, null, config, reporter);
    boolean success = parser.parse();
    Assert.assertFalse(success);
    Assert.assertTrue(reporter.hasWarnings());
  }
}