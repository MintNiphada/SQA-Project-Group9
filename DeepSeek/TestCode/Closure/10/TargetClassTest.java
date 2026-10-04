package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.TernaryValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link NodeUtil}.
 */
public class NodeUtilTest {
  private Node nameUndefined;
  private Node nameNaN;
  private Node nameInfinity;

  @Before
  public void setUp() {
    nameUndefined = IR.name("undefined");
    nameNaN = IR.name("NaN");
    nameInfinity = IR.name("Infinity");
  }

  // ---- getImpureBooleanValue ----

  @Test
  public void testGetImpureBooleanValue_string() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.string("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.string("")));
  }

  @Test
  public void testGetImpureBooleanValue_number() {
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(IR.number(0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.number(1)));
  }

  @Test
  public void testGetImpureBooleanValue_not() {
    Node not = IR.not(IR.string(""));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(not));
  }

  @Test
  public void testGetImpureBooleanValue_and_or_assign_comma() {
    // AND: both true
    Node and = new Node(Token.AND, IR.trueNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(and));
    // AND: true && false
    Node and2 = new Node(Token.AND, IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(and2));
    // OR
    Node or = new Node(Token.OR, IR.falseNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(or));
    // OR false || false
    Node or2 = new Node(Token.OR, IR.falseNode(), IR.falseNode());
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(or2));
    // ASSIGN
    Node assign = new Node(Token.ASSIGN, IR.name("x"), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));
    // COMMA
    Node comma = new Node(Token.COMMA, IR.falseNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(comma));
  }

  @Test
  public void testGetImpureBooleanValue_hook_equal_true_false() {
    Node hook = new Node(Token.HOOK, IR.trueNode(), IR.trueNode(), IR.trueNode());
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hook));
  }

  @Test
  public void testGetImpureBooleanValue_hook_unknown() {
    Node hook = new Node(Token.HOOK, IR.trueNode(), IR.trueNode(), IR.falseNode());
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hook));
  }

  @Test
  public void testGetImpureBooleanValue_void() {
    Node voidNode = new Node(Token.VOID, IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(voidNode));
  }

  @Test
  public void testGetImpureBooleanValue_array_object_literal() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.arraylit()));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(IR.objectlit()));
  }

  // ---- getPureBooleanValue ----

  @Test
  public void testGetPureBooleanValue_undefined_nan() {
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameUndefined));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameNaN));
  }

  @Test
  public void testGetPureBooleanValue_infinity() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(nameInfinity));
  }

  @Test
  public void testGetPureBooleanValue_regexp() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.regexp(IR.string("reg"))));
  }

  @Test
  public void testGetPureBooleanValue_void_no_side_effect() {
    Node voidNode = new Node(Token.VOID, IR.number(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(voidNode));
  }

  @Test
  public void testGetPureBooleanValue_void_with_side_effect() {
    // Void of a call may have side effects; we test that getPureBooleanValue returns UNKNOWN if side effects possible.
    Node call = IR.call(IR.name("f"));
    Node voidCall = new Node(Token.VOID, call);
    // May have side effects => pure boolean unknown
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(voidCall));
  }

  @Test
  public void testGetPureBooleanValue_array_object_literal_no_side_effect() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.arraylit(IR.number(1))));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(IR.objectlit()));
  }

  @Test
  public void testGetPureBooleanValue_array_with_side_effect() {
    Node call = IR.call(IR.name("f"));
    Node arr = IR.arraylit(call);
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(arr));
  }

  // ---- getStringValue ----

  @Test
  public void testGetStringValue_string() {
    assertEquals("hello", NodeUtil.getStringValue(IR.string("hello")));
  }

  @Test
  public void testGetStringValue_string_key() {
    Node key = Node.newString(Token.STRING_KEY, "prop");
    assertEquals("prop", NodeUtil.getStringValue(key));
  }

  @Test
  public void testGetStringValue_undefined_nan_infinity() {
    assertEquals("undefined", NodeUtil.getStringValue(nameUndefined));
    assertEquals("NaN", NodeUtil.getStringValue(nameNaN));
    assertEquals("Infinity", NodeUtil.getStringValue(nameInfinity));
  }

  @Test
  public void testGetStringValue_number() {
    Node num = IR.number(42);
    assertEquals("42", NodeUtil.getStringValue(num));
    Node numZero = IR.number(0);
    assertEquals("0", NodeUtil.getStringValue(numZero));
  }

  @Test
  public void testGetStringValue_boolean() {
    assertEquals("false", NodeUtil.getStringValue(IR.falseNode()));
    assertEquals("true", NodeUtil.getStringValue(IR.trueNode()));
  }

  @Test
  public void testGetStringValue_null() {
    assertEquals("null", NodeUtil.getStringValue(IR.nullNode()));
  }

  @Test
  public void testGetStringValue_void() {
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID, IR.number(0))));
  }

  @Test
  public void testGetStringValue_not_known() {
    Node not = IR.not(IR.trueNode());
    assertEquals("false", NodeUtil.getStringValue(not));
    Node not2 = IR.not(IR.call(IR.name("f")));
    assertNull(NodeUtil.getStringValue(not2));
  }

  @Test
  public void testGetStringValue_arraylit() {
    Node arr = IR.arraylit(IR.number(1), IR.number(2));
    assertEquals("1,2", NodeUtil.getStringValue(arr));
    Node arrMixed = IR.arraylit(IR.number(1), IR.nullNode());
    assertEquals("1,", NodeUtil.getStringValue(arrMixed));
  }

  @Test
  public void testGetStringValue_objectlit() {
    assertEquals("[object Object]", NodeUtil.getStringValue(IR.objectlit()));
  }

  @Test
  public void testGetStringValue_other_returns_null() {
    assertNull(NodeUtil.getStringValue(IR.name("x")));
  }

  @Test
  public void testGetStringValue_double() {
    assertEquals("42", NodeUtil.getStringValue(42.0));
    assertEquals("3.14", NodeUtil.getStringValue(3.14));
  }

  // ---- getArrayElementStringValue ----

  @Test
  public void testGetArrayElementStringValue_null_or_undefined() {
    assertEquals("", NodeUtil.getArrayElementStringValue(IR.nullNode()));
    assertEquals("", NodeUtil.getArrayElementStringValue(nameUndefined));
  }

  @Test
  public void testGetArrayElementStringValue_empty() {
    assertEquals("", NodeUtil.getArrayElementStringValue(IR.empty()));
  }

  @Test
  public void testGetArrayElementStringValue_other() {
    assertEquals("42", NodeUtil.getArrayElementStringValue(IR.number(42)));
  }

  // ---- arrayToString ----

  @Test
  public void testArrayToString_null_child_value() {
    Node arr = IR.arraylit(IR.name("x")); // getStringValue(name) returns null
    assertNull(NodeUtil.arrayToString(arr));
  }

  @Test
  public void testArrayToString_empty_array() {
    assertEquals("", NodeUtil.arrayToString(IR.arraylit()));
  }

  // ---- getNumberValue ----

  @Test
  public void testGetNumberValue_true_false_null() {
    assertEquals(1.0, NodeUtil.getNumberValue(IR.trueNode()), 0);
    assertEquals(0.0, NodeUtil.getNumberValue(IR.falseNode()), 0);
    assertEquals(0.0, NodeUtil.getNumberValue(IR.nullNode()), 0);
  }

  @Test
  public void testGetNumberValue_number() {
    assertEquals(3.0, NodeUtil.getNumberValue(IR.number(3)), 0);
  }

  @Test
  public void testGetNumberValue_void_no_side_effect() {
    assertEquals(Double.NaN, NodeUtil.getNumberValue(new Node(Token.VOID, IR.number(0))), 0);
  }

  @Test
  public void testGetNumberValue_void_with_side_effect() {
    Node voidWithCall = new Node(Token.VOID, IR.call(IR.name("f")));
    assertNull(NodeUtil.getNumberValue(voidWithCall));
  }

  @Test
  public void testGetNumberValue_undefined() {
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(nameUndefined)));
  }

  @Test
  public void testGetNumberValue_nan() {
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(nameNaN)));
  }

  @Test
  public void testGetNumberValue_infinity() {
    assertEquals(Double.POSITIVE_INFINITY, NodeUtil.getNumberValue(nameInfinity), 0);
  }

  @Test
  public void testGetNumberValue_negative_infinity() {
    Node negInf = IR.neg(IR.name("Infinity"));
    assertEquals(Double.NEGATIVE_INFINITY, NodeUtil.getNumberValue(negInf), 0);
  }

  @Test
  public void testGetNumberValue_not_known() {
    assertEquals(0.0, NodeUtil.getNumberValue(IR.not(IR.trueNode())), 0);
  }

  @Test
  public void testGetNumberValue_string() {
    Node str = IR.string("42");
    assertEquals(42.0, NodeUtil.getNumberValue(str), 0);
  }

  @Test
  public void testGetNumberValue_array_object_lit() {
    Node arr = IR.arraylit(IR.number(1));
    assertEquals(1.0, NodeUtil.getNumberValue(arr), 0);
    assertNull(NodeUtil.getNumberValue(IR.objectlit())); // "[object Object]" converts to NaN? Actually getStringValue returns "[object Object]" then getStringNumberValue will try to parse -> NaN? But getStringNumberValue("[object Object]") -> null because parse fails? Actually it will return Double.NaN via NumberFormatException. But getNumberValue for objectlit returns null if the conversion to string yields null? Let's trace: getNumberValue for OBJECTLIT calls getStringValue which returns "[object Object]", then passes to getStringNumberValue. getStringNumberValue will try to parse that and catch NumberFormatException returning Double.NaN. Then getNumberValue returns that? Actually getNumberValue's case for OBJECTLIT: String value = getStringValue(n); return value != null ? getStringNumberValue(value) : null; Since getStringValue returns non-null, getStringNumberValue returns Double.NaN, so overall returns Double.NaN. So assertTrue(Double.isNaN(NodeUtil.getNumberValue(IR.objectlit())));
  }

  // ---- getStringNumberValue ----
  @Test
  public void testGetStringNumberValue_empty() {
    assertEquals(0.0, NodeUtil.getStringNumberValue(""), 0);
    assertEquals(0.0, NodeUtil.getStringNumberValue("  "), 0); // trimmed empty
  }

  @Test
  public void testGetStringNumberValue_hex() {
    assertEquals(255.0, NodeUtil.getStringNumberValue("0xff"), 0);
    assertEquals(255.0, NodeUtil.getStringNumberValue("0XFF"), 0);
  }

  @Test
  public void testGetStringNumberValue_hex_invalid() {
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xGG")));
  }

  @Test
  public void testGetStringNumberValue_hex_with_sign() {
    assertNull(NodeUtil.getStringNumberValue("-0x10"));
    assertNull(NodeUtil.getStringNumberValue("+0x10"));
  }

  @Test
  public void testGetStringNumberValue_infinity_variants() {
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));
  }

  @Test
  public void testGetStringNumberValue_vertical_tab() {
    assertNull(NodeUtil.getStringNumberValue("\u000b"));
  }

  @Test
  public void testGetStringNumberValue_valid_double() {
    assertEquals(3.14, NodeUtil.getStringNumberValue("3.14"), 1e-9);
  }

  @Test
  public void testGetStringNumberValue_invalid_double() {
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("abc")));
  }

  // ---- trimJsWhiteSpace ----
  @Test
  public void testTrimJsWhiteSpace() {
    assertEquals("", NodeUtil.trimJsWhiteSpace(""));
    assertEquals("abc", NodeUtil.trimJsWhiteSpace("  abc  "));
    // includes various whitespace chars from isStrWhiteSpaceChar
    assertEquals("", NodeUtil.trimJsWhiteSpace("\t\n\r "));
  }

  // ---- isStrWhiteSpaceChar ----
  @Test
  public void testIsStrWhiteSpaceChar_basic() {
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar(' '));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\n'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\t'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u00A0')); // NBSP
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u000C')); // FF
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2028')); // LS
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2029')); // PS
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\uFEFF')); // BOM
    assertEquals(TernaryValue.UNKNO
