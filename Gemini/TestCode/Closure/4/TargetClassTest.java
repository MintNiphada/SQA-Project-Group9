package com.google.javascript.rhino.jstype;

import com.google.common.base.Predicate;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class NamedTypeTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;
  private TestScope scope;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
    scope = new TestScope();
  }

  private static class TestSlot implements StaticSlot<JSType> {
    private final String name;
    private final JSType type;

    TestSlot(String name, JSType type) {
      this.name = name;
      this.type = type;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public JSType getType() {
      return type;
    }

    @Override
    public boolean isTypeInferred() {
      return false;
    }

    @Override
    public StaticReference<JSType> getDeclaration() {
      return null;
    }

    @Override
    public JSType getJSType() {
      return type;
    }
  }

  private static class TestScope implements StaticScope<JSType> {
    private final Map<String, StaticSlot<JSType>> slots = new HashMap<String, StaticSlot<JSType>>();
    private StaticScope<JSType> parent;

    void addSlot(String name, JSType type) {
      slots.put(name, new TestSlot(name, type));
    }

    @Override
    public Node getRootNode() {
      return null;
    }

    @Override
    public StaticScope<JSType> getParentScope() {
      return parent;
    }

    @Override
    public StaticSlot<JSType> getSlot(String name) {
      if (slots.containsKey(name)) {
        return slots.get(name);
      }
      if (parent != null) {
        return parent.getSlot(name);
      }
      return null;
    }

    @Override
    public StaticSlot<JSType> getOwnSlot(String name) {
      return slots.get(name);
    }

    @Override
    public StaticSlot<JSType> getSlot(String name, StaticScope<JSType> stopScope) {
      return getSlot(name);
    }

    @Override
    public StaticSlot<JSType> getOwnSlot(String name, StaticScope<JSType> stopScope) {
      return getOwnSlot(name);
    }

    @Override
    public StaticSlot<JSType> getParentSlot(String name) {
      return parent != null ? parent.getSlot(name) : null;
    }

    @Override
    public StaticSlot<JSType> getSlot(StaticSlot<JSType> slot) {
      return slot;
    }

    @Override
    public StaticSlot<JSType> getSlot(StaticSlot<JSType> slot, StaticScope<JSType> stopScope) {
      return slot;
    }

    @Override
    public JSType getTypeOfThis() {
      return null;
    }
  }

  private static class RecordingErrorReporter implements ErrorReporter {
    final List<String> warnings = new ArrayList<String>();
    final List<String> errors = new ArrayList<String>();

    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
      errors.add(message);
    }
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullReference() {
    new NamedType(registry, null, "source.js", 1, 0);
  }

  @Test
  public void testBasicProperties() {
    NamedType namedType = new NamedType(registry, "Foo", "source.js", 10, 5);
    assertEquals("Foo", namedType.getReferenceName());
    assertEquals("Foo", namedType.toStringHelper(true));
    assertEquals("Foo", namedType.toStringHelper(false));
    assertTrue(namedType.hasReferenceName());
    assertTrue(namedType.isNamedType());
    assertTrue(namedType.isNominalType());
    assertEquals("Foo".hashCode(), namedType.hashCode());
    assertNotNull(namedType.getReferencedType());
    assertTrue(namedType.getReferencedType().isUnknownType());
  }

  @Test
  public void testResolveViaRegistrySuccess() {
    ObjectType objectType = registry.createObjectType("MyClass", null, null);
    NamedType namedType = new NamedType(registry, "MyClass", "source.js", 1, 0);

    final boolean[] validated = new boolean[] {false};
    namedType.setValidator(new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        validated[0] = true;
        return true;
      }
    });

    JSType resolved = namedType.resolve(errorReporter, scope);
    assertTrue(validated[0]);
    assertTrue(namedType.isResolved());
    assertEquals(objectType, namedType.getReferencedType());
    assertEquals(objectType, resolved);
  }

  @Test
  public void testResolveViaRegistryNotLastGeneration() {
    ObjectType objectType = registry.createObjectType("MyClass", null, null);
    NamedType namedType = new NamedType(registry, "MyClass", "source.js", 1, 0);

    NamedType resolved = (NamedType) namedType.resolveInternal(errorReporter, scope);
    assertSame(namedType, resolved);
    assertEquals(objectType, namedType.getReferencedType());
  }

  @Test
  public void testDefinePropertyContinuations() {
    ObjectType objectType = registry.createObjectType("TargetType", null, null);
    NamedType namedType = new NamedType(registry, "TargetType", "source.js", 1, 0);

    Node propNode = new Node(0);
    assertTrue(namedType.defineProperty("prop1", registry.getNativeType(JSTypeNative.STRING_TYPE), false, propNode));
    assertTrue(namedType.defineProperty("prop2", registry.getNativeType(JSTypeNative.NUMBER_TYPE), true, propNode));

    namedType.resolve(errorReporter, scope);

    assertTrue(objectType.hasProperty("prop1"));
    assertTrue(objectType.hasProperty("prop2"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), objectType.getPropertyType("prop1"));
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), objectType.getPropertyType("prop2"));

    // Defining property on already resolved type delegates to super
    assertTrue(namedType.defineProperty("prop3", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), false, propNode));
    assertTrue(objectType.hasProperty("prop3"));
  }

  @Test
  public void testResolveViaPropertiesConstructor() {
    FunctionType ctor = registry.createConstructorType("ns.MyCtor", null, null, null);
    ObjectType nsObj = registry.createAnonymousObjectType();
    nsObj.defineProperty("MyCtor", ctor, false, null);
    scope.addSlot("ns", nsObj);

    NamedType namedType = new NamedType(registry, "ns.MyCtor", "source.js", 1, 0);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertEquals(ctor.getInstanceType(), resolved);
    assertEquals(ctor.getInstanceType(), namedType.getReferencedType());
  }

  @Test
  public void testResolveViaPropertiesInterface() {
    FunctionType iface = registry.createInterfaceType("ns.MyIface", null);
    ObjectType nsObj = registry.createAnonymousObjectType();
    nsObj.defineProperty("MyIface", iface, false, null);
    scope.addSlot("ns", nsObj);

    NamedType namedType = new NamedType(registry, "ns.MyIface", "source.js", 1, 0);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertEquals(iface.getInstanceType(), resolved);
  }

  @Test
  public void testResolveViaPropertiesNoObjectType() {
    FunctionType noObjFn = registry.getNativeFunctionType(JSTypeNative.NO_OBJECT_TYPE);
    scope.addSlot("noObj", noObjFn.getTypeOfThis());

    NamedType namedType = new NamedType(registry, "noObj", "source.js", 1, 0);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertEquals(noObjFn.getInstanceType(), resolved);
  }

  @Test
  public void testResolveViaPropertiesEnumType() {
    EnumType enumType = registry.createEnumType("MyEnum", null, registry.getNativeType(JSTypeNative.STRING_TYPE));
    scope.addSlot("MyEnum", enumType);

    NamedType namedType = new NamedType(registry, "MyEnum", "source.js", 1, 0);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertEquals(enumType.getElementsType(), resolved);
  }

  @Test
  public void testResolveViaPropertiesDeepChaining() {
    FunctionType ctor = registry.createConstructorType("A.B.C.Target", null, null, null);
    ObjectType cObj = registry.createAnonymousObjectType();
    cObj.defineProperty("Target", ctor, false, null);

    ObjectType bObj = registry.createAnonymousObjectType();
    bObj.defineProperty("C", cObj, false, null);

    ObjectType aObj = registry.createAnonymousObjectType();
    aObj.defineProperty("B", bObj, false, null);

    scope.addSlot("A", aObj);

    NamedType namedType = new NamedType(registry, "A.B.C.Target", "source.js", 1, 0);
    JSType resolved = namedType.resolve(errorReporter, scope);

    assertEquals(ctor.getInstanceType(), resolved);
  }

  @Test
  public void testLookupViaPropertiesFailsEmptyFirstComponent() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    NamedType namedType = new NamedType(registry, ".A.B", "source.js", 10, 2);
    namedType.resolve(reporter, scope);

    assertTrue(reporter.warnings.size() > 0);
    assertTrue(reporter.warnings.get(0).contains("Bad type annotation. Unknown type .A.B"));
  }

  @Test
  public void testLookupViaPropertiesFailsMissingSlot() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    NamedType namedType = new NamedType(registry, "NotFound.Prop", "source.js", 10, 2);
    namedType.resolve(reporter, scope);

    assertTrue(reporter.warnings.size() > 0);
    assertTrue(reporter.warnings.get(0).contains("Bad type annotation. Unknown type NotFound.Prop"));
  }

  @Test
  public void testLookupViaPropertiesSlotTypeAllOrNoType() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    scope.addSlot("allSlot", registry.getNativeType(JSTypeNative.ALL_TYPE));
    scope.addSlot("noTypeSlot", registry.getNativeType(JSTypeNative.NO_TYPE));
    scope.addSlot("nullTypeSlot", null);

    NamedType nt1 = new NamedType(registry, "allSlot.Prop", "source.js", 1, 0);
    nt1.resolve(reporter, scope);

    NamedType nt2 = new NamedType(registry, "noTypeSlot.Prop", "source.js", 1, 0);
    nt2.resolve(reporter, scope);

    NamedType nt3 = new NamedType(registry, "nullTypeSlot.Prop", "source.js", 1, 0);
    nt3.resolve(reporter, scope);

    assertEquals(3, reporter.warnings.size());
  }

  @Test
  public void testLookupViaPropertiesNonObjectIntermediate() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    scope.addSlot("numSlot", registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    NamedType namedType = new NamedType(registry, "numSlot.sub.Target", "source.js", 1, 0);
    namedType.resolve(reporter, scope);

    assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testLookupViaPropertiesEmptyIntermediateComponent() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    ObjectType ns = registry.createAnonymousObjectType();
    scope.addSlot("ns", ns);

    NamedType namedType = new NamedType(registry, "ns..Target", "source.js", 1, 0);
    namedType.resolve(reporter, scope);

    assertTrue(reporter.warnings.size() > 0);
  }

  @Test
  public void testForwardDeclaredTypeNoWarning() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    registry.forwardDeclareType("ForwardDeclared.Type");

    NamedType namedType = new NamedType(registry, "ForwardDeclared.Type", "source.js", 1, 0);

    final boolean[] validated = new boolean[] {false};
    namedType.setValidator(new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        validated[0] = true;
        return true;
      }
    });

    JSType resolved = namedType.resolve(reporter, scope);

    assertEquals(0, reporter.warnings.size());
    assertTrue(validated[0]);
    assertEquals(registry.getNativeObjectType(JSTypeNative.NO_RESOLVED_TYPE), resolved);
  }

  @Test
  public void testGetTypedefTypeNullSlotType() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    NamedType namedType = new NamedType(registry, "Foo", "source.js", 1, 0);
    StaticSlot<JSType> nullSlot = new TestSlot("Foo", null);

    JSType res = namedType.getTypedefType(reporter, nullSlot, "Foo");
    assertNull(res);
    assertEquals(1, reporter.warnings.size());
  }

  @Test
  public void testGetTypedefTypeNonNullSlotType() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    NamedType namedType = new NamedType(registry, "Foo", "source.js", 1, 0);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    StaticSlot<JSType> slot = new TestSlot("Foo", stringType);

    JSType res = namedType.getTypedefType(reporter, slot, "Foo");
    assertEquals(stringType, res);
    assertEquals(0, reporter.warnings.size());
  }

  @Test
  public void testSetValidatorAfterResolved() {
    ObjectType objectType = registry.createObjectType("MyClass", null, null);
    NamedType namedType = new NamedType(registry, "MyClass", "source.js", 1, 0);
    namedType.resolve(errorReporter, scope);

    assertTrue(namedType.isResolved());
    boolean res = namedType.setValidator(new Predicate<JSType>() {
      @Override
      public boolean apply(JSType input) {
        return true;
      }
    });
    assertTrue(res);
  }

  @Test
  public void testEnumElementCycleDetection() {
    RecordingErrorReporter reporter = new RecordingErrorReporter();
    NamedType namedType = new NamedType(registry, "CycleEnum", "source.js", 5, 10);
    EnumElementType enumElementType = new EnumElementType(registry, namedType, "CycleEnum");
    scope.addSlot("CycleEnum", enumElementType);

    namedType.resolve(reporter, scope);

    assertTrue(reporter.warnings.size() > 0);
    assertTrue(reporter.warnings.get(0).contains("Cycle detected in inheritance chain of type CycleEnum"));
    assertTrue(namedType.getReferencedType().isUnknownType());
  }
}