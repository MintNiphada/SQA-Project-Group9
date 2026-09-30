package com.google.javascript.jscomp.parsing;

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
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TargetClassTest {

  private static class SimpleTestErrorReporter implements ErrorReporter {
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
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
      return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }
  }

  private JsDocInfo parse(String comment) {
    return parse(comment, true, new SimpleTestErrorReporter());
  }

  private JsDocInfo parse(String comment, boolean parseDocumentation, SimpleTestErrorReporter errorReporter) {
    Set<String> extraAnnotations = Sets.newHashSet("customTag");
    Set<String> suppressions = Sets.newHashSet("checkTypes", "visibility", "deprecated", "extraSuppression");
    Config config = new Config(
        extraAnnotations,
        suppressions,
        parseDocumentation,
        LanguageMode.ECMASCRIPT5,
        true);

    Comment commentNode = new Comment(0, comment.length(), null, comment);
    JsDocInfoParser parser = new JsDocInfoParser(
        new JsDocTokenStream(comment),
        commentNode,
        null,
        config,
        errorReporter);
    parser.parse();
    return parser.retrieveAndResetParsedJSDocInfo();
  }

  private JsDocInfoParser createParser(String comment, boolean parseDocumentation, SimpleTestErrorReporter errorReporter) {
    Set<String> extraAnnotations = Sets.newHashSet("customTag");
    Set<String> suppressions = Sets.newHashSet("checkTypes", "visibility", "deprecated");
    Config config = new Config(
        extraAnnotations,
        suppressions,
        parseDocumentation,
        LanguageMode.ECMASCRIPT5,
        true);

    return new JsDocInfoParser(
        new JsDocTokenStream(comment),
        null,
        null,
        config,
        errorReporter);
  }

  @Test
  public void testParseTypeString() {
    Node node = JsDocInfoParser.parseTypeString("number");
    Assert.assertNotNull(node);
    Assert.assertEquals(Token.STRING, node.getType());
    Assert.assertEquals("number", node.getString());

    Node unionNode = JsDocInfoParser.parseTypeString("number|string");
    Assert.assertNotNull(unionNode);
    Assert.assertEquals(Token.PIPE, unionNode.getType());

    Node unionDoublePipe = JsDocInfoParser.parseTypeString("number||string");
    Assert.assertNotNull(unionDoublePipe);

    Node qmark = JsDocInfoParser.parseTypeString("?");
    Assert.assertNotNull(qmark);
    Assert.assertEquals(Token.QMARK, qmark.getType());

    Node star = JsDocInfoParser.parseTypeString("*");
    Assert.assertNotNull(star);
    Assert.assertEquals(Token.STAR, star.getType());

    Node nullType = JsDocInfoParser.parseTypeString("null");
    Assert.assertNotNull(nullType);
    Assert.assertEquals("null", nullType.getString());

    Node undefinedType = JsDocInfoParser.parseTypeString("undefined");
    Assert.assertNotNull(undefinedType);
    Assert.assertEquals("undefined", undefinedType.getString());

    Node bangType = JsDocInfoParser.parseTypeString("!Object");
    Assert.assertNotNull(bangType);
    Assert.assertEquals(Token.BANG, bangType.getType());

    Node qmarkType = JsDocInfoParser.parseTypeString("?Object");
    Assert.assertNotNull(qmarkType);
    Assert.assertEquals(Token.QMARK, qmarkType.getType());

    Node suffixBang = JsDocInfoParser.parseTypeString("Object!");
    Assert.assertNotNull(suffixBang);
    Assert.assertEquals(Token.BANG, suffixBang.getType());

    Node suffixQmark = JsDocInfoParser.parseTypeString("Object?");
    Assert.assertNotNull(suffixQmark);
    Assert.assertEquals(Token.QMARK, suffixQmark.getType());

    Node arrayType = JsDocInfoParser.parseTypeString("[number, ...string]");
    Assert.assertNotNull(arrayType);
    Assert.assertEquals(Token.LB, arrayType.getType());

    Node recordType = JsDocInfoParser.parseTypeString("{a: number, b: string}");
    Assert.assertNotNull(recordType);
    Assert.assertEquals(Token.LC, recordType.getType());

    Node typeApp = JsDocInfoParser.parseTypeString("Array.<string, number>");
    Assert.assertNotNull(typeApp);

    Node funcType = JsDocInfoParser.parseTypeString("function(this:Object, string, ...[number]): boolean");
    Assert.assertNotNull(funcType);
    Assert.assertEquals(Token.FUNCTION, funcType.getType());

    Node funcTypeNew = JsDocInfoParser.parseTypeString("function(new:Object): void");
    Assert.assertNotNull(funcTypeNew);

    Node funcTypeEmpty = JsDocInfoParser.parseTypeString("function()");
    Assert.assertNotNull(funcTypeEmpty);

    Node invalid = JsDocInfoParser.parseTypeString("!@#$");
    Assert.assertNull(invalid);
  }

  @Test
  public void testParseInlineTypeDoc() {
    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    JsDocInfoParser parser = createParser("/** number */", false, reporter);
    JSDocInfo info = parser.parseInlineTypeDoc();
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getType());
  }

  @Test
  public void testParseBasicAnnotations() {
    JSDocInfo info = parse("/**\n * @constructor\n * @struct\n * @export\n * @expose\n * @externs\n * @javadispatch\n * @noalias\n * @nocompile\n * @nocheck\n * @notimplemented\n * @override\n * @preserveTry\n * @noshadow\n * @nosideeffects\n * @implicitCast\n * @wizaction\n */");
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isConstructor());
    Assert.assertTrue(info.makesDicts());
    Assert.assertFalse(info.makesDicts());
    Assert.assertTrue(info.isExport());
    Assert.assertTrue(info.isOverride());
  }

  @Test
  public void testConstructorAndInterfaceConflicts() {
    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    JSDocInfo info = parse("/** @constructor\n @interface */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testInterfaceAndConstructorConflicts() {
    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    JSDocInfo info = parse("/** @interface\n @constructor */", true, reporter);
    Assert.assertNotNull(info);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testDictAndStruct() {
    JSDocInfo infoDict = parse("/** @dict */");
    Assert.assertNotNull(infoDict);
    Assert.assertTrue(infoDict.makesDicts());

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @dict\n @struct */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testNgInjectAndJagger() {
    JSDocInfo info = parse("/**\n * @ngInject\n * @jaggerInject\n * @jaggerModule\n * @jaggerProvide\n */");
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isNgInject());

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @ngInject\n @ngInject\n @jaggerInject\n @jaggerInject\n @jaggerModule\n @jaggerModule\n @jaggerProvide\n @jaggerProvide */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() >= 4);
  }

  @Test
  public void testAuthorAndSee() {
    JSDocInfo info = parse("/**\n * @author Alice\n * @see http://example.com\n * @version 1.0\n */");
    Assert.assertNotNull(info);
    Assert.assertTrue(info.getAuthors().contains("Alice"));
    Assert.assertTrue(info.getReferences().contains("http://example.com"));
    Assert.assertEquals("1.0", info.getVersion());

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @author\n @see\n @version\n */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() >= 3);
  }

  @Test
  public void testDeprecatedAndDescAndMeaning() {
    JSDocInfo info = parse("/**\n * @deprecated Use newMethod instead.\n * @desc A description.\n * @meaning Some meaning.\n */");
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isDeprecated());
    Assert.assertEquals("Use newMethod instead.", info.getDeprecationReason());
    Assert.assertEquals("A description.", info.getDescription());
    Assert.assertEquals("Some meaning.", info.getMeaning());

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @desc first\n @desc second\n @meaning first\n @meaning second */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() >= 2);
  }

  @Test
  public void testFileOverviewAndPreserve() {
    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    JsDocInfoParser parser = createParser("/**\n * @fileoverview File overview text.\n * @preserve License header text.\n * @license Another license.\n */", true, reporter);
    Node.FileLevelJsDocBuilder fileLevelJsDocBuilder = new Node.FileLevelJsDocBuilder();
    parser.setFileLevelJsDocBuilder(fileLevelJsDocBuilder);
    parser.parse();
    JSDocInfo overview = parser.getFileOverviewJSDocInfo();
    Assert.assertNotNull(overview);
    Assert.assertEquals("File overview text.", overview.getFileOverview());
  }

  @Test
  public void testEnumTag() {
    JSDocInfo infoDefault = parse("/** @enum */");
    Assert.assertNotNull(infoDefault);
    Assert.assertNotNull(infoDefault.getEnumParameterType());

    JSDocInfo infoString = parse("/** @enum {string} */");
    Assert.assertNotNull(infoString);
    Assert.assertNotNull(infoString.getEnumParameterType());
  }

  @Test
  public void testExtendsAndImplements() {
    JSDocInfo info = parse("/**\n * @extends {BaseClass}\n * @implements {InterfaceA}\n * @implements {InterfaceB}\n */");
    Assert.assertNotNull(info);
    Assert.assertNotNull(info.getBaseType());
    Assert.assertEquals(2, info.getImplementedInterfaces().size());

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @extends {BaseClass}\n @extends {AnotherClass} */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testInterfaceExtendsMultiple() {
    JSDocInfo info = parse("/**\n * @interface\n * @extends {InterfaceA}\n * @extends {InterfaceB}\n */");
    Assert.assertNotNull(info);
    Assert.assertTrue(info.isInterface());
    Assert.assertEquals(2, info.getExtendedInterfaces().size());
  }

  @Test
  public void testLendsAndHidden() {
    JSDocInfo info = parse("/**\n * @lends {MyClass.prototype}\n * @hidden\n */");
    Assert.assertNotNull(info);
    Assert.assertEquals("MyClass.prototype", info.getLendsName());
    Assert.assertTrue(info.isHidden());

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @lends */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testThrows() {
    JSDocInfo info = parse("/**\n * @throws {Error} When something goes wrong.\n */");
    Assert.assertNotNull(info);
    Assert.assertEquals(1, info.getThrownTypes().size());

    JSDocInfo infoNoType = parse("/**\n * @throws When something goes wrong.\n */");
    Assert.assertNotNull(infoNoType);
    Assert.assertEquals(1, infoNoType.getThrownTypes().size());
  }

  @Test
  public void testParamTags() {
    JSDocInfo info = parse("/**\n * @param {string} a First param.\n * @param {number=} [opt_b] Optional param.\n * @param {boolean} [opt_c=true] Defaulted param.\n * @param ignored.property Ignored subproperty.\n */");
    Assert.assertNotNull(info);
    Assert.assertTrue(info.hasParameter("a"));
    Assert.assertTrue(info.hasParameter("opt_b"));
    Assert.assertTrue(info.hasParameter("opt_c"));
    Assert.assertEquals("First param.", info.getParameterDescription("a"));

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @param {string} a\n @param {number} a */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() > 0);

    SimpleTestErrorReporter reporterMissing = new SimpleTestErrorReporter();
    parse("/** @param */", true, reporterMissing);
    Assert.assertTrue(reporterMissing.warnings.size() > 0);
  }

  @Test
  public void testModifiesTag() {
    JSDocInfo info = parse("/**\n * @param {Object} x\n * @modifies {this|arguments|x}\n */");
    Assert.assertNotNull(info);
    Assert.assertTrue(info.getModifies().contains("this"));
    Assert.assertTrue(info.getModifies().contains("arguments"));
    Assert.assertTrue(info.getModifies().contains("x"));

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @modifies {unknownProp} */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testIdGeneratorTags() {
    JSDocInfo infoConsistent = parse("/** @consistentIdGenerator */");
    Assert.assertNotNull(infoConsistent);
    Assert.assertTrue(infoConsistent.isConsistentIdGenerator());

    JSDocInfo infoStable = parse("/** @stableIdGenerator */");
    Assert.assertNotNull(infoStable);
    Assert.assertTrue(infoStable.isStableIdGenerator());

    JSDocInfo infoIdGen1 = parse("/** @idgenerator */");
    Assert.assertNotNull(infoIdGen1);
    Assert.assertTrue(infoIdGen1.isIdGenerator());

    JSDocInfo infoIdGen2 = parse("/** @idgenerator {consistent} */");
    Assert.assertNotNull(infoIdGen2);
    Assert.assertTrue(infoIdGen2.isConsistentIdGenerator());

    JSDocInfo infoIdGen3 = parse("/** @idgenerator {stable} */");
    Assert.assertNotNull(infoIdGen3);
    Assert.assertTrue(infoIdGen3.isStableIdGenerator());

    JSDocInfo infoIdGen4 = parse("/** @idgenerator {mapped} */");
    Assert.assertNotNull(infoIdGen4);
    Assert.assertTrue(infoIdGen4.isMappedIdGenerator());

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @idgenerator {unknown} */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testSuppressTag() {
    JSDocInfo info = parse("/** @suppress {checkTypes, visibility|deprecated} */");
    Assert.assertNotNull(info);
    Assert.assertTrue(info.getSuppressions().contains("checkTypes"));
    Assert.assertTrue(info.getSuppressions().contains("visibility"));
    Assert.assertTrue(info.getSuppressions().contains("deprecated"));

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @suppress {unknownWarning} */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testTemplateAndDisposes() {
    JSDocInfo info = parse("/**\n * @template T, U\n * @disposes a, b\n */");
    Assert.assertNotNull(info);
    Assert.assertEquals(2, info.getTemplateTypeNames().size());
    Assert.assertTrue(info.getTemplateTypeNames().contains("T"));
    Assert.assertTrue(info.getTemplateTypeNames().contains("U"));
    Assert.assertEquals(2, info.getDisposesParameters().size());

    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @template\n @disposes */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() >= 2);
  }

  @Test
  public void testVisibilityAndTypeAnnotations() {
    JSDocInfo infoPrivate = parse("/** @private {string} Private property description. */");
    Assert.assertNotNull(infoPrivate);
    Assert.assertEquals(Visibility.PRIVATE, infoPrivate.getVisibility());
    Assert.assertNotNull(infoPrivate.getType());

    JSDocInfo infoProtected = parse("/** @protected {number} */");
    Assert.assertNotNull(infoProtected);
    Assert.assertEquals(Visibility.PROTECTED, infoProtected.getVisibility());

    JSDocInfo infoPublic = parse("/** @public {boolean} */");
    Assert.assertNotNull(infoPublic);
    Assert.assertEquals(Visibility.PUBLIC, infoPublic.getVisibility());

    JSDocInfo infoConst = parse("/** @const {string} */");
    Assert.assertNotNull(infoConst);
    Assert.assertTrue(infoConst.isConstant());

    JSDocInfo infoDefine = parse("/** @define {boolean} Description for define. */");
    Assert.assertNotNull(infoDefine);
    Assert.assertNotNull(infoDefine.getType());

    JSDocInfo infoReturn = parse("/** @return {number} Return description. */");
    Assert.assertNotNull(infoReturn);
    Assert.assertNotNull(infoReturn.getReturnType());
    Assert.assertEquals("Return description.", infoReturn.getReturnDescription());

    JSDocInfo infoThis = parse("/** @this {Object} */");
    Assert.assertNotNull(infoThis);
    Assert.assertNotNull(infoThis.getThisType());

    JSDocInfo infoTypedef = parse("/** @typedef {string|number} */");
    Assert.assertNotNull(infoTypedef);
    Assert.assertNotNull(infoTypedef.getTypedefType());
  }

  @Test
  public void testBlockCommentExtraction() {
    JSDocInfo info = parse("/**\n * Line one of block comment.\n * Line two.\n *\n * @constructor\n */");
    Assert.assertNotNull(info);
    Assert.assertTrue(info.getBlockDescription().contains("Line one of block comment."));
    Assert.assertTrue(info.getBlockDescription().contains("Line two."));
  }

  @Test
  public void testBadJsDocTagWarning() {
    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    parse("/** @invalidTagNameXYZ */", true, reporter);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testPrematureEOF() {
    SimpleTestErrorReporter reporter = new SimpleTestErrorReporter();
    JsDocInfoParser parser = createParser("/** @param {string} a", true, reporter);
    boolean result = parser.parse();
    Assert.assertFalse(result);
    Assert.assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testParamVariadicSyntax() {
    Node node = JsDocInfoParser.parseTypeString("function(...number): void");
    Assert.assertNotNull(node);

    Node nodeLegacy = JsDocInfoParser.parseTypeString("function(...[number]): void");
    Assert.assertNotNull(nodeLegacy);
  }
}

class JsDocInfoParserTest extends TargetClassTest {
}