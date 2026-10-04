package com.google.javascript.rhino.jstype;

import com.google.javascript.rhino.Node;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RecordTypeBuilderTest {
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(null);
  }

  @Test
  public void testBuildEmptyRecordReturnsNativeObjectType() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    JSType result = builder.build();
    Assert.assertNotNull(result);
    Assert.assertSame(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), result);
  }

  @Test
  public void testAddPropertyAndBuild() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    Node node = new Node(0);

    RecordTypeBuilder chained = builder.addProperty("foo", stringType, node);
    Assert.assertSame(builder, chained);

    JSType builtType = builder.build();
    Assert.assertNotNull(builtType);
    Assert.assertTrue(builtType.isRecordType());
    RecordType recordType = (RecordType) builtType;
    Assert.assertTrue(recordType.hasProperty("foo"));
    Assert.assertSame(stringType, recordType.getPropertyType("foo"));
    Assert.assertSame(node, recordType.getPropertyNode("foo"));
  }

  @Test
  public void testAddDuplicatePropertyReturnsNull() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    Node node1 = new Node(0);
    Node node2 = new Node(1);

    RecordTypeBuilder res1 = builder.addProperty("prop", stringType, node1);
    Assert.assertSame(builder, res1);

    RecordTypeBuilder res2 = builder.addProperty("prop", numberType, node2);
    Assert.assertNull(res2);

    JSType builtType = builder.build();
    Assert.assertTrue(builtType.isRecordType());
    RecordType recordType = (RecordType) builtType;
    Assert.assertSame(stringType, recordType.getPropertyType("prop"));
    Assert.assertSame(node1, recordType.getPropertyNode("prop"));
  }

  @Test
  public void testAddMultipleDistinctProperties() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    Node node1 = new Node(10);
    Node node2 = new Node(20);

    builder.addProperty("a", numType, node1);
    builder.addProperty("b", boolType, node2);

    RecordType recordType = (RecordType) builder.build();
    Assert.assertNotNull(recordType);
    Assert.assertTrue(recordType.hasProperty("a"));
    Assert.assertTrue(recordType.hasProperty("b"));
    Assert.assertSame(numType, recordType.getPropertyType("a"));
    Assert.assertSame(boolType, recordType.getPropertyType("b"));
  }

  @Test
  public void testAddPropertyWithNullTypeAndNode() {
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    RecordTypeBuilder chained = builder.addProperty("nullProp", null, null);
    Assert.assertSame(builder, chained);

    JSType builtType = builder.build();
    Assert.assertNotNull(builtType);
    Assert.assertTrue(builtType.isRecordType());
    RecordType recordType = (RecordType) builtType;
    Assert.assertTrue(recordType.hasProperty("nullProp"));
    Assert.assertNull(recordType.getPropertyNode("nullProp"));
  }

  @Test
  public void testRecordPropertyGetters() {
    JSType type = registry.getNativeType(JSTypeNative.ALL_TYPE);
    Node node = new Node(42);
    RecordTypeBuilder.RecordProperty prop = new RecordTypeBuilder.RecordProperty(type, node);

    Assert.assertSame(type, prop.getType());
    Assert.assertSame(node, prop.getPropertyNode());
  }

  @Test
  public void testRecordPropertyNullGetters() {
    RecordTypeBuilder.RecordProperty prop = new RecordTypeBuilder.RecordProperty(null, null);

    Assert.assertNull(prop.getType());
    Assert.assertNull(prop.getPropertyNode());
  }
}
