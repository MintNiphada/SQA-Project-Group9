package com.google.javascript.rhino;

import com.google.javascript.rhino.JSDocInfo.Visibility;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class JSDocInfoBuilderTest {

  private JSDocInfoBuilder builder;

  @Before
  public void setUp() {
    builder = new JSDocInfoBuilder(true);
  }

  private JSTypeExpression createTypeExpression(String typeName) {
    Node node = Node.newString(Token.NAME, typeName);
    return new JSTypeExpression(node, "testSource", null);
  }

  @Test
  public void testInitialState() {
    assertFalse(builder.isPopulated());
    assertFalse(builder.isPopulatedWithFileOverview());
    assertFalse(builder.isDescriptionRecorded());
    assertFalse(builder.isConstructorRecorded());
    assertFalse(builder.isInterfaceRecorded());
    assertFalse(builder.hasParameter("param1"));
    assertNull(builder.build("source.js"));
  }

  @Test
  public void testBuildWhenPopulated() {
    assertTrue(builder.recordConstancy());
    assertTrue(builder.isPopulated());

    JSDocInfo info = builder.build("source.js");
    assertNotNull(info);
    assertEquals("source.js", info.getSourceName());
    assertTrue(info.isConstant());
    assertEquals(Visibility.INHERITED, info.getVisibility());

    assertFalse(builder.isPopulated());
    assertNull(builder.build("source.js"));
  }

  @Test
  public void testBuildWithCustomVisibility() {
    assertTrue(builder.recordVisibility(Visibility.PRIVATE));
    JSDocInfo info = builder.build("source.js");
    assertNotNull(info);
    assertEquals(Visibility.PRIVATE, info.getVisibility());
  }

  @Test
  public void testRecordVisibility() {
    assertTrue(builder.recordVisibility(Visibility.PUBLIC));
    assertFalse(builder.recordVisibility(Visibility.PRIVATE));
  }

  @Test
  public void testRecordBlockDescription() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    assertTrue(docBuilder.recordBlockDescription("A block description"));
    assertTrue(docBuilder.isPopulated());

    JSDocInfoBuilder noDocBuilder = new JSDocInfoBuilder(false);
    assertFalse(noDocBuilder.recordBlockDescription("A block description"));
    assertFalse(noDocBuilder.isPopulated());
  }

  @Test
  public void testMarkers() {
    builder.markAnnotation("param", 1, 5);
    builder.markName("arg0", 1, 12);
    Node typeNode = Node.newString(Token.NAME, "number");
    builder.markTypeNode(typeNode, 1, 18, 24, true);
    builder.markText("The number argument", 1, 26, 1, 45);

    JSDocInfoBuilder builderWithoutDoc = new JSDocInfoBuilder(false);
    builderWithoutDoc.markText("text", 0, 0, 0, 4);
    builderWithoutDoc.markTypeNode(typeNode, 0, 0, 4, false);
    builderWithoutDoc.markName("name", 0, 0);
    builderWithoutDoc.markAnnotation("type", 0, 0);
  }

  @Test
  public void testRecordDescription() {
    assertFalse(builder.isDescriptionRecorded());
    assertFalse(builder.recordDescription(null));
    assertTrue(builder.recordDescription("Description"));
    assertTrue(builder.isDescriptionRecorded());
    assertFalse(builder.recordDescription("Duplicate description"));
  }

  @Test
  public void testRecordFileOverview() {
    assertFalse(builder.isPopulatedWithFileOverview());
    assertTrue(builder.recordFileOverview("File overview text"));
    assertTrue(builder.isPopulatedWithFileOverview());
    assertFalse(builder.recordFileOverview("Duplicate file overview"));
  }

  @Test
  public void testRecordParameterAndDescription() {
    JSTypeExpression type = createTypeExpression("string");
    assertFalse(builder.hasParameter("arg0"));
    assertTrue(builder.recordParameter("arg0", type));
    assertTrue(builder.hasParameter("arg0"));
    assertFalse(builder.recordParameter("arg0", type));

    assertTrue(builder.recordParameterDescription("arg0", "arg0 description"));
    assertFalse(builder.recordParameterDescription("arg0", "arg0 description duplicate"));
  }

  @Test
  public void testRecordTemplateTypeName() {
    assertTrue(builder.recordTemplateTypeName("T"));
    assertFalse(builder.recordTemplateTypeName("T"));
  }

  @Test
  public void testRecordThrowTypeAndDescription() {
    JSTypeExpression type = createTypeExpression("Error");
    assertTrue(builder.recordThrowType(type));
    assertTrue(builder.recordThrowDescription(type, "Throws error description"));
    assertFalse(builder.recordThrowDescription(type, "Duplicate throw description"));
  }

  @Test
  public void testAddAuthorAndReference() {
    assertTrue(builder.addAuthor("Alice"));
    assertFalse(builder.addAuthor("Alice"));

    assertTrue(builder.addReference("http://example.com"));
    assertFalse(builder.addReference("http://example.com"));
  }

  @Test
  public void testRecordVersionAndDeprecation() {
    assertTrue(builder.recordVersion("1.0.0"));
    assertFalse(builder.recordVersion("2.0.0"));

    assertTrue(builder.recordDeprecationReason("Use newApi instead"));
    assertFalse(builder.recordDeprecationReason("Duplicate reason"));
  }

  @Test
  public void testRecordSuppressions() {
    Set<String> suppressions = new HashSet<String>();
    suppressions.add("checkTypes");
    assertTrue(builder.recordSuppressions(suppressions));
    assertFalse(builder.recordSuppressions(suppressions));
  }

  @Test
  public void testRecordType() {
    assertFalse(builder.recordType(null));
    JSTypeExpression type = createTypeExpression("number");
    assertTrue(builder.recordType(type));
    assertFalse(builder.recordType(type));

    JSTypeExpression otherType = createTypeExpression("string");
    assertFalse(builder.recordTypedef(otherType));
    assertFalse(builder.recordEnumParameterType(otherType));
    assertFalse(builder.recordReturnType(otherType));
    assertFalse(builder.recordParameter("p", otherType));
    assertFalse(builder.recordThrowType(otherType));
    assertFalse(builder.recordThisType(otherType));
    assertFalse(builder.recordBaseType(otherType));
    assertFalse(builder.recordConstructor());
    assertFalse(builder.recordInterface());
  }

  @Test
  public void testRecordTypedef() {
    assertFalse(builder.recordTypedef(null));
    JSTypeExpression type = createTypeExpression("Object");
    assertTrue(builder.recordTypedef(type));
    assertFalse(builder.recordTypedef(type));
    assertFalse(builder.recordType(type));
  }

  @Test
  public void testRecordReturnTypeAndDescription() {
    assertFalse(builder.recordReturnType(null));
    JSTypeExpression type = createTypeExpression("boolean");
    assertTrue(builder.recordReturnType(type));
    assertFalse(builder.recordReturnType(type));

    assertTrue(builder.recordReturnDescription("Returns true on success"));
    assertFalse(builder.recordReturnDescription("Duplicate return description"));
  }

  @Test
  public void testRecordDefineType() {
    assertFalse(builder.recordDefineType(null));

    JSDocInfoBuilder constBuilder = new JSDocInfoBuilder(true);
    assertTrue(constBuilder.recordConstancy());
    assertFalse(constBuilder.recordDefineType(createTypeExpression("boolean")));

    JSTypeExpression type = createTypeExpression("boolean");
    assertTrue(builder.recordDefineType(type));
    assertFalse(builder.recordDefineType(type));
  }

  @Test
  public void testRecordEnumParameterType() {
    assertFalse(builder.recordEnumParameterType(null));
    JSTypeExpression type = createTypeExpression("number");
    assertTrue(builder.recordEnumParameterType(type));
    assertFalse(builder.recordEnumParameterType(type));
  }

  @Test
  public void testRecordThisType() {
    assertFalse(builder.recordThisType(null));
    JSTypeExpression type = createTypeExpression("HTMLElement");
    assertTrue(builder.recordThisType(type));
    assertFalse(builder.recordThisType(type));
  }

  @Test
  public void testRecordBaseType() {
    assertFalse(builder.recordBaseType(null));
    JSTypeExpression type = createTypeExpression("BaseClass");
    assertTrue(builder.recordBaseType(type));
    assertFalse(builder.recordBaseType(type));
  }

  @Test
  public void testRecordImplementedInterface() {
    JSTypeExpression type = createTypeExpression("Disposable");
    assertTrue(builder.recordImplementedInterface(type));
    assertFalse(builder.recordImplementedInterface(type));
  }

  @Test
  public void testRecordConstructorAndInterfaceExclusivity() {
    assertFalse(builder.isConstructorRecorded());
    assertTrue(builder.recordConstructor());
    assertTrue(builder.isConstructorRecorded());
    assertFalse(builder.recordConstructor());
    assertFalse(builder.recordInterface());

    JSDocInfoBuilder interfaceBuilder = new JSDocInfoBuilder(true);
    assertFalse(interfaceBuilder.isInterfaceRecorded());
    assertTrue(interfaceBuilder.recordInterface());
    assertTrue(interfaceBuilder.isInterfaceRecorded());
    assertFalse(interfaceBuilder.recordInterface());
    assertFalse(interfaceBuilder.recordConstructor());
  }

  @Test
  public void testFlags() {
    assertTrue(builder.recordConstancy());
    assertFalse(builder.recordConstancy());

    assertTrue(builder.recordHiddenness());
    assertFalse(builder.recordHiddenness());

    assertTrue(builder.recordNoTypeCheck());
    assertFalse(builder.recordNoTypeCheck());

    assertTrue(builder.recordPreserveTry());
    assertFalse(builder.recordPreserveTry());

    assertTrue(builder.recordOverride());
    assertFalse(builder.recordOverride());

    assertTrue(builder.recordNoAlias());
    assertFalse(builder.recordNoAlias());

    assertTrue(builder.recordDeprecated());
    assertFalse(builder.recordDeprecated());

    assertTrue(builder.recordExport());
    assertFalse(builder.recordExport());

    assertTrue(builder.recordNoShadow());
    assertFalse(builder.recordNoShadow());

    assertTrue(builder.recordImplicitCast());
    assertFalse(builder.recordImplicitCast());

    assertTrue(builder.recordNoSideEffects());
    assertFalse(builder.recordNoSideEffects());
  }

  @Test
  public void testTypeRelatedTagsConflictWithType() {
    JSTypeExpression type = createTypeExpression("number");

    JSDocInfoBuilder b1 = new JSDocInfoBuilder(true);
    assertTrue(b1.recordConstructor());
    assertFalse(b1.recordType(type));

    JSDocInfoBuilder b2 = new JSDocInfoBuilder(true);
    assertTrue(b2.recordInterface());
    assertFalse(b2.recordType(type));

    JSDocInfoBuilder b3 = new JSDocInfoBuilder(true);
    assertTrue(b3.recordParameter("param", type));
    assertFalse(b3.recordType(type));

    JSDocInfoBuilder b4 = new JSDocInfoBuilder(true);
    assertTrue(b4.recordReturnType(type));
    assertFalse(b4.recordType(type));

    JSDocInfoBuilder b5 = new JSDocInfoBuilder(true);
    assertTrue(b5.recordBaseType(type));
    assertFalse(b5.recordType(type));

    JSDocInfoBuilder b6 = new JSDocInfoBuilder(true);
    assertTrue(b6.recordThisType(type));
    assertFalse(b6.recordType(type));
  }
}