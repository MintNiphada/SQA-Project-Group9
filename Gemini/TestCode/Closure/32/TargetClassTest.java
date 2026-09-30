package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfo.Visibility;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.ast.Comment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsDocInfoParserTest {

  private List<String> warnings;
  private List<String> errors;
  private ErrorReporter errorReporter;
  private Map<String, Annotation> annotationNames;
  private Set<String> suppressionNames;

  @Before
  public void setUp() {
    warnings = new ArrayList<String>();
    errors = new ArrayList<String>();
    errorReporter = new ErrorReporter() {
      @Override
      public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
        warnings.add(message);
      }

      @Override
      public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
        errors.add(message);
      }

      @Override
      public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        errors.add(message);
        return new EvaluatorException(message);
      }
    };

    annotationNames = Maps.newHashMap(Annotation.recognizedAnnotations);
    suppressionNames = Sets.newHashSet("checkTypes", "accessControls", "missingProperties", "visibility");
  }

  private JsDocInfoParser createParser(String comment, boolean parseDocs) {
    Config config = new Config(
        annotationNames,
        suppressionNames,
        parseDocs,
        LanguageMode.ECMASCRIPT5,
        false);
    JsDocTokenStream stream = new JsDocTokenStream(comment);
    Comment commentNode = new Comment(0, comment.length(),
        com.google.javascript.rhino.head.Token.CommentType.JSDOC, comment);
    return new JsDocInfoParser(stream, commentNode, null, config, errorReporter);
  }

  private JSDocInfo parse(String comment) {
    return parse(comment, true);
  }

  private JSDocInfo parse(String comment, boolean parseDocs) {
    JsDocInfoParser parser = createParser(comment, parseDocs);
    boolean success = parser.parse();
    Assert.assertTrue("Parsing should succeed", success);
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  private JsDocInfoParser parseWithParser(String comment) {
    JsDocInfoParser parser = createParser(comment, true);
    parser.parse();
    return parser;
  }

  @Test
  public void testParseTypeStringPrimitives() {
    Node n = JsDocInfoParser.parseTypeString("number");
    Assert.assertNotNull(n);
    Assert.assertEquals(Token.NAME, n.getType());
    Assert.assertEquals("number", n.getString());

    n = JsDocInfoParser.parseTypeString("string");
    Assert.assertEquals("string", n.getString());

    n = JsDocInfoParser.parseTypeString("boolean");
    Assert.assertEquals("boolean", n.getString());

    n = JsDocInfoParser.parseTypeString("null");
    Assert.assertEquals(Token.NAME, n.getType());
    Assert.assertEquals("null", n.getString());

    n = JsDocInfoParser.parseTypeString("undefined");
    Assert.assertEquals(Token.NAME, n.getType());
    Assert.assertEquals("undefined", n.getString());

    n = JsDocInfoParser.parseTypeString("*");
    Assert.assertEquals(Token.STAR, n.getType());

    n = JsDocInfoParser.parseTypeString("?");
    Assert.assertEquals(Token.QMARK, n.getType());
  }

  @Test
  public void testParseTypeStringModifiers() {
    Node n = JsDocInfoParser.parseTypeString("?number");
    Assert.assertEquals(Token.QMARK, n.getType());
    Assert.assertEquals(Token.NAME, n.getFirstChild().getType());

    n = JsDocInfoParser.parseTypeString("!number");
    Assert.assertEquals(Token.BANG, n.getType());
    Assert.assertEquals(Token.NAME, n.getFirstChild().getType());

    n = JsDocInfoParser.parseTypeString("number?");
    Assert.assertEquals(Token.QMARK, n.getType());

    n = JsDocInfoParser.parseTypeString("number!");
    Assert.assertEquals(Token.BANG, n.getType());
  }

  @Test
  public void testParseTypeStringUnionAndList() {
    Node n = JsDocInfoParser.parseTypeString("(number|string)");
    Assert.assertEquals(Token.PIPE, n.getType());
    Assert.assertEquals(2, n.getChildCount());

    n = JsDocInfoParser.parseTypeString("number|string|boolean");
    Assert.assertEquals(Token.PIPE, n.getType());
    Assert.assertEquals(3, n.getChildCount());

    n = JsDocInfoParser.parseTypeString("(number,string)");
    Assert.assertEquals(Token.PIPE, n.getType());
  }

  @Test
  public void testParseTypeStringGenericsAndRecord() {
    Node n = JsDocInfoParser.parseTypeString("Array.<string>");
    Assert.assertEquals(Token.NAME, n.getType());
    Assert.assertEquals("Array", n.getString());
    Assert.assertTrue(n.hasChildren());

    n = JsDocInfoParser.parseTypeString("Object.<string, number>");
    Assert.assertEquals(Token.NAME, n.getType());

    n = JsDocInfoParser.parseTypeString("{a: number, b: string}");
    Assert.assertEquals(Token.LC, n.getType());

    n = JsDocInfoParser.parseTypeString("[number, string]");
    Assert.assertEquals(Token.LB, n.getType());

    n = JsDocInfoParser.parseTypeString("[...number]");
    Assert.assertEquals(Token.LB, n.getType());
  }

  @Test
  public void testParseTypeStringFunction() {
    Node n = JsDocInfoParser.parseTypeString("function(): void");
    Assert.assertEquals(Token.FUNCTION, n.getType());

    n = JsDocInfoParser.parseTypeString("function(string, number): boolean");
    Assert.assertEquals(Token.FUNCTION, n.getType());

    n = JsDocInfoParser.parseTypeString("function(this:Object, string=, ...[number]): string");
    Assert.assertEquals(Token.FUNCTION, n.getType());

    n = JsDocInfoParser.parseTypeString("function(new:Object)");
    Assert.assertEquals(Token.FUNCTION, n.getType());

    n = JsDocInfoParser.parseTypeString("function(...)");
    Assert.assertEquals(Token.FUNCTION, n.getType());
  }

  @Test
  public void testParseTypeStringInvalid() {
    Assert.assertNull(JsDocInfoParser.parseTypeString("{"));
    Assert.assertNull(JsDocInfoParser.parseTypeString("function("));
    Assert.assertNull(JsDocInfoParser.parseTypeString("Array.<"));
    Assert.assertNull(JsDocInfoParser.parseTypeString("["));
    Assert.assertNull(JsDocInfoParser.parseTypeString("(foo"));
    Assert.assertNull(JsDocInfoParser.parseTypeString("function(this)"));
  }

  @Test
  public void testBasicDescriptionsAndBlock() {
    JSDocInfo info = parse("/** Some block description\n * on multiple lines.\n */");
    Assert.assertEquals("Some block description\non multiple lines.", info.getBlockDescription());

    info = parse("/** @desc Hello world */");
    Assert.assertEquals("Hello world", info.getDescription());

    info = parse("/** @meaning Translatable meaning */");
    Assert.assertEquals("Translatable meaning", info.getMeaning());
  }

  @Test
  public void testParamTag() {
    JSDocInfo info = parse("/**\n * @param {string} name User name\n * @param {number=} opt_age Age\n * @param {...*} var_args Arguments\n */");
    Assert.assertTrue(info.hasParameter("name"));
    Assert.assertTrue(info.hasParameter("opt_age"));
    Assert.assertTrue(info.hasParameter("var_args"));
    Assert.assertEquals("User name", info.getParameterDescription("name"));
    Assert.assertEquals("Age", info.getParameterDescription("opt_age"));
    Assert.assertEquals("Arguments", info.getParameterDescription("var_args"));

    info = parse("/** @param [name=defaultVal] Optional param */");
    Assert.assertTrue(info.hasParameter("name"));
  }

  @Test
  public void testParamWithDot() {
    JSDocInfo info = parse("/** @param {string} user.name Subfield */");
    Assert.assertFalse(info.hasParameter("user.name"));
  }

  @Test
  public void testReturnAndThrows() {
    JSDocInfo info = parse("/**\n * @return {boolean} Result flag\n * @throws {Error} When failed\n */");
    Assert.assertNotNull(info.getReturnType());
    Assert.assertEquals("Result flag", info.getReturnDescription());
    Assert.assertEquals(1, info.getThrows().size());
  }

  @Test
  public void testVisibilityTags() {
    JSDocInfo info = parse("/** @private */");
    Assert.assertEquals(Visibility.PRIVATE, info.getVisibility());

    info = parse("/** @protected */");
    Assert.assertEquals(Visibility.PROTECTED, info.getVisibility());

    info = parse("/** @public */");
    Assert.assertEquals(Visibility.PUBLIC, info.getVisibility());
  }

  @Test
  public void testConstructorAndInterface() {
    JSDocInfo info = parse("/** @constructor */");
    Assert.assertTrue(info.isConstructor());

    info = parse("/** @interface */");
    Assert.assertTrue(info.isInterface());
  }

  @Test
  public void testExtendsAndImplements() {
    JSDocInfo info = parse("/** @extends {BaseClass} */");
    Assert.assertNotNull(info.getBaseType());

    info = parse("/** @implements {Interface1} \n * @implements {Interface2} */");
    Assert.assertEquals(2, info.getImplementedInterfaces().size());

    info = parse("/** @interface\n * @extends {I1}\n * @extends {I2} */");
    Assert.assertEquals(2, info.getExtendedInterfaces().size());
  }

  @Test
  public void testConstEnumTypedef() {
    JSDocInfo info = parse("/** @const */");
    Assert.assertTrue(info.isConstant());

    info = parse("/** @enum {string} */");
    Assert.assertNotNull(info.getEnumParameterType());

    info = parse("/** @enum */");
    Assert.assertNotNull(info.getEnumParameterType());

    info = parse("/** @typedef {Array.<string>} */");
    Assert.assertNotNull(info.getTypedefType());
  }

  @Test
  public void testTypeAndDefine() {
    JSDocInfo info = parse("/** @type {number|string} */");
    Assert.assertNotNull(info.getType());

    info = parse("/** @define {boolean} */");
    Assert.assertNotNull(info.getType());
  }

  @Test
  public void testThisTag() {
    JSDocInfo info = parse("/** @this {Array} */");
    Assert.assertNotNull(info.getThisType());

    info = parse("/** @this Array */");
    Assert.assertNotNull(info.getThisType());
  }

  @Test
  public void testSuppressTag() {
    JSDocInfo info = parse("/** @suppress {checkTypes|accessControls} */");
    Set<String> supp = info.getSuppressions();
    Assert.assertTrue(supp.contains("checkTypes"));
    Assert.assertTrue(supp.contains("accessControls"));
  }

  @Test
  public void testModifiesTag() {
    JSDocInfo info = parse("/**\n * @param {Object} obj\n * @modifies {this|arguments|obj}\n */");
    Set<String> mod = info.getModifies();
    Assert.assertTrue(mod.contains("this"));
    Assert.assertTrue(mod.contains("arguments"));
    Assert.assertTrue(mod.contains("obj"));
  }

  @Test
  public void testFlagTags() {
    JSDocInfo info = parse("/**\n"
        + " * @consistentIdGenerator\n"
        + " * @export\n"
        + " * @expose\n"
        + " * @externs\n"
        + " * @javadispatch\n"
        + " * @hidden\n"
        + " * @noalias\n"
        + " * @nocompile\n"
        + " * @nosideeffects\n"
        + " * @override\n"
        + " * @preserveTry\n"
        + " * @noshadow\n"
        + " * @implicitCast\n"
        + " * @idGenerator\n"
        + " */");
    Assert.assertTrue(info.isConsistentIdGenerator());
    Assert.assertTrue(info.isExport());
    Assert.assertTrue(info.isExpose());
    Assert.assertTrue(info.isExterns());
    Assert.assertTrue(info.isJavaDispatch());
    Assert.assertTrue(info.isHidden());
    Assert.assertTrue(info.isNoAlias());
    Assert.assertTrue(info.isNoCompile());
    Assert.assertTrue(info.isNoSideEffects());
    Assert.assertTrue(info.isOverride());
    Assert.assertTrue(info.isPreserveTry());
    Assert.assertTrue(info.isNoShadow());
    Assert.assertTrue(info.isImplicitCast());
    Assert.assertTrue(info.isIdGenerator());
  }

  @Test
  public void testTemplateAuthorSeeVersion() {
    JSDocInfo info = parse("/**\n"
        + " * @template T, U\n"
        + " * @author Alice\n"
        + " * @see http://example.com\n"
        + " * @version 1.2.3\n"
        + " */");
    Assert.assertTrue(info.getTemplateTypeNames().contains("T, U"));
    Assert.assertTrue(info.getAuthors().contains("Alice"));
    Assert.assertTrue(info.getReferences().contains("http://example.com"));
    Assert.assertEquals("1.2.3", info.getVersion());
  }

  @Test
  public void testDeprecatedTag() {
    JSDocInfo info = parse("/** @deprecated Use newFunction instead. */");
    Assert.assertTrue(info.isDeprecated());
    Assert.assertEquals("Use newFunction instead.", info.getDeprecationReason());
  }

  @Test
  public void testFileOverview() {
    JsDocInfoParser parser = parseWithParser("/** @fileoverview This is a file overview.\n * Details here.\n */");
    JSDocInfo fileInfo = parser.getFileOverviewJSDocInfo();
    Assert.assertNotNull(fileInfo);
    Assert.assertTrue(fileInfo.hasFileOverview());
    Assert.assertEquals("This is a file overview.\nDetails here.", fileInfo.getFileOverview());
  }

  @Test
  public void testLicenseAndPreserve() {
    Node.FileLevelJsDocBuilder builder = new Node.FileLevelJsDocBuilder();
    JsDocInfoParser parser = createParser("/**\n * @license MIT License\n * Copyright 2020\n */", true);
    parser.setFileLevelJsDocBuilder(builder);
    parser.parse();
    Assert.assertTrue(builder.toString().contains("MIT License"));
  }

  @Test
  public void testLendsTag() {
    JSDocInfo info = parse("/** @lends {MyClass.prototype} */");
    Assert.assertEquals("MyClass.prototype", info.getLendsName());

    info = parse("/** @lends MyClass */");
    Assert.assertEquals("MyClass", info.getLendsName());
  }

  @Test
  public void testDuplicateAndIncompatibleWarnings() {
    parse("/** @constructor\n * @interface */");
    Assert.assertFalse(warnings.isEmpty());

    warnings.clear();
    parse("/** @private\n * @public */");
    Assert.assertFalse(warnings.isEmpty());

    warnings.clear();
    parse("/** @desc first\n * @desc second */");
    Assert.assertFalse(warnings.isEmpty());

    warnings.clear();
    parse("/** @param {string} x\n * @param {number} x */");
    Assert.assertFalse(warnings.isEmpty());

    warnings.clear();
    parse("/** @unknownTag */");
    Assert.assertFalse(warnings.isEmpty());
  }

  @Test
  public void testUnexpectedEOF() {
    Config config = new Config(
        annotationNames,
        suppressionNames,
        true,
        LanguageMode.ECMASCRIPT5,
        false);
    JsDocTokenStream stream = new JsDocTokenStream("/** @type {string");
    JsDocInfoParser parser = new JsDocInfoParser(stream, null, null, config, errorReporter);
    boolean success = parser.parse();
    Assert.assertFalse(success);
    Assert.assertFalse(warnings.isEmpty());
  }

  @Test
  public void testNoDocumentationOption() {
    JSDocInfo info = parse("/** Description\n * @author Bob\n * @see ref\n * @param {string} x desc\n * @return {number} ret\n */", false);
    Assert.assertNotNull(info);
    Assert.assertNull(info.getAuthors());
    Assert.assertNull(info.getReferences());
    Assert.assertNull(info.getParameterDescription("x"));
    Assert.assertNull(info.getReturnDescription());
  }
}