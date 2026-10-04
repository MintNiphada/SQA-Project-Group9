package org.apache.commons.lang3.builder;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyStyleTest {

    private static class TestStyle extends ToStringStyle {
        private static final long serialVersionUID = 1L;

        public TestStyle() {
            super();
        }

        public void publicAppendDetail(StringBuffer buffer, String fieldName, Object value) {
            super.appendDetail(buffer, fieldName, value);
        }

        public void publicAppendDetail(StringBuffer buffer, String fieldName, Collection<?> coll) {
            super.appendDetail(buffer, fieldName, coll);
        }

        public void publicAppendDetail(StringBuffer buffer, String fieldName, Map<?, ?> map) {
            super.appendDetail(buffer, fieldName, map);
        }

        public void publicAppendSummary(StringBuffer buffer, String fieldName, Object value) {
            super.appendSummary(buffer, fieldName, value);
        }

        public void publicReflectionAppendArrayDetail(StringBuffer buffer, String fieldName, Object array) {
            super.reflectionAppendArrayDetail(buffer, fieldName, array);
        }

        public void publicRemoveLastFieldSeparator(StringBuffer buffer) {
            super.removeLastFieldSeparator(buffer);
        }

        public boolean publicIsFullDetail(Boolean fullDetailRequest) {
            return super.isFullDetail(fullDetailRequest);
        }

        public String publicGetShortClassName(Class<?> cls) {
            return super.getShortClassName(cls);
        }
    }

    private static class Person {
        String name = "John";
        int age = 30;
    }

    private static class SelfRef {
        SelfRef child;
    }

    @Test
    public void testDefaultStyle() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer sb = new StringBuffer();
        Person p = new Person();
        style.appendStart(sb, p);
        style.append(sb, "name", p.name, null);
        style.append(sb, "age", p.age);
        style.appendEnd(sb, p);

        String hexId = Integer.toHexString(System.identityHashCode(p));
        String expected = "org.apache.commons.lang3.builder.MyStyleTest$Person@" + hexId + "[name=John,age=30]";
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void testNoFieldNamesStyle() {
        ToStringStyle style = ToStringStyle.NO_FIELD_NAMES_STYLE;
        StringBuffer sb = new StringBuffer();
        Person p = new Person();
        style.appendStart(sb, p);
        style.append(sb, "name", p.name, null);
        style.append(sb, "age", p.age);
        style.appendEnd(sb, p);

        String hexId = Integer.toHexString(System.identityHashCode(p));
        String expected = "org.apache.commons.lang3.builder.MyStyleTest$Person@" + hexId + "[John,30]";
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void testShortPrefixStyle() {
        ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;
        StringBuffer sb = new StringBuffer();
        Person p = new Person();
        style.appendStart(sb, p);
        style.append(sb, "name", p.name, null);
        style.append(sb, "age", p.age);
        style.appendEnd(sb, p);

        String expected = "MyStyleTest.Person[name=John,age=30]";
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void testSimpleStyle() {
        ToStringStyle style = ToStringStyle.SIMPLE_STYLE;
        StringBuffer sb = new StringBuffer();
        Person p = new Person();
        style.appendStart(sb, p);
        style.append(sb, "name", p.name, null);
        style.append(sb, "age", p.age);
        style.appendEnd(sb, p);

        String expected = "John,30";
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void testMultiLineStyle() {
        ToStringStyle style = ToStringStyle.MULTI_LINE_STYLE;
        StringBuffer sb = new StringBuffer();
        Person p = new Person();
        style.appendStart(sb, p);
        style.append(sb, "name", p.name, null);
        style.append(sb, "age", p.age);
        style.appendEnd(sb, p);

        String hexId = Integer.toHexString(System.identityHashCode(p));
        String ls = SystemUtils.LINE_SEPARATOR;
        String expected = "org.apache.commons.lang3.builder.MyStyleTest$Person@" + hexId + "[" + ls + "  name=John" + ls + "  age=30" + ls + "]";
        Assert.assertEquals(expected, sb.toString());
    }

    @Test
    public void testAppendNullValues() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer sb = new StringBuffer();
        style.append(sb, "field", (Object) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "field", (long[]) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "field", (int[]) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "field", (short[]) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "field", (byte[]) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "field", (char[]) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "field", (double[]) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "field", (float[]) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "field", (boolean[]) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "field", (Object[]) null, null);
        Assert.assertEquals("field=<null>,", sb.toString());
    }

    @Test
    public void testAppendPrimitives() {
        ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;
        StringBuffer sb = new StringBuffer();
        style.append(sb, "long", 123L);
        style.append(sb, "int", 456);
        style.append(sb, "short", (short) 7);
        style.append(sb, "byte", (byte) 8);
        style.append(sb, "char", 'a');
        style.append(sb, "double", 1.5d);
        style.append(sb, "float", 2.5f);
        style.append(sb, "boolean", true);

        Assert.assertEquals("long=123,int=456,short=7,byte=8,char=a,double=1.5,float=2.5,boolean=true,", sb.toString());
    }

    @Test
    public void testAppendPrimitiveArraysDetail() {
        ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;

        StringBuffer sb = new StringBuffer();
        style.append(sb, "arr", new long[]{1L, 2L}, Boolean.TRUE);
        Assert.assertEquals("arr={1,2},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new int[]{1, 2}, Boolean.TRUE);
        Assert.assertEquals("arr={1,2},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new short[]{1, 2}, Boolean.TRUE);
        Assert.assertEquals("arr={1,2},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new byte[]{1, 2}, Boolean.TRUE);
        Assert.assertEquals("arr={1,2},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new char[]{'a', 'b'}, Boolean.TRUE);
        Assert.assertEquals("arr={a,b},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new double[]{1.1, 2.2}, Boolean.TRUE);
        Assert.assertEquals("arr={1.1,2.2},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new float[]{1.1f, 2.2f}, Boolean.TRUE);
        Assert.assertEquals("arr={1.1,2.2},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new boolean[]{true, false}, Boolean.TRUE);
        Assert.assertEquals("arr={true,false},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new Object[]{"val1", null}, Boolean.TRUE);
        Assert.assertEquals("arr={val1,<null>},", sb.toString());
    }

    @Test
    public void testAppendPrimitiveArraysSummary() {
        ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;

        StringBuffer sb = new StringBuffer();
        style.append(sb, "arr", new long[]{1L, 2L}, Boolean.FALSE);
        Assert.assertEquals("arr=<size=2>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new int[]{1, 2}, Boolean.FALSE);
        Assert.assertEquals("arr=<size=2>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new short[]{1, 2}, Boolean.FALSE);
        Assert.assertEquals("arr=<size=2>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new byte[]{1, 2}, Boolean.FALSE);
        Assert.assertEquals("arr=<size=2>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new char[]{'a', 'b'}, Boolean.FALSE);
        Assert.assertEquals("arr=<size=2>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new double[]{1.1, 2.2}, Boolean.FALSE);
        Assert.assertEquals("arr=<size=2>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new float[]{1.1f, 2.2f}, Boolean.FALSE);
        Assert.assertEquals("arr=<size=2>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new boolean[]{true, false}, Boolean.FALSE);
        Assert.assertEquals("arr=<size=2>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "arr", new Object[]{"val1", "val2"}, Boolean.FALSE);
        Assert.assertEquals("arr=<size=2>,", sb.toString());
    }

    @Test
    public void testAppendInternalTypesDetailAndSummary() {
        ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;

        List<String> list = Arrays.asList("a", "b");
        StringBuffer sb = new StringBuffer();
        style.append(sb, "list", list, Boolean.TRUE);
        Assert.assertEquals("list=[a, b],", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "list", list, Boolean.FALSE);
        Assert.assertEquals("list=<size=2>,", sb.toString());

        Map<String, String> map = new HashMap<String, String>();
        map.put("k", "v");
        sb = new StringBuffer();
        style.append(sb, "map", map, Boolean.TRUE);
        Assert.assertEquals("map={k=v},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "map", map, Boolean.FALSE);
        Assert.assertEquals("map=<size=1>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new int[]{1, 2, 3}, Boolean.TRUE);
        Assert.assertEquals("obj={1,2,3},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new int[]{1, 2, 3}, Boolean.FALSE);
        Assert.assertEquals("obj=<size=3>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new long[]{1L}, Boolean.TRUE);
        Assert.assertEquals("obj={1},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new long[]{1L}, Boolean.FALSE);
        Assert.assertEquals("obj=<size=1>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new short[]{1}, Boolean.TRUE);
        Assert.assertEquals("obj={1},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new short[]{1}, Boolean.FALSE);
        Assert.assertEquals("obj=<size=1>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new byte[]{1}, Boolean.TRUE);
        Assert.assertEquals("obj={1},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new byte[]{1}, Boolean.FALSE);
        Assert.assertEquals("obj=<size=1>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new char[]{'z'}, Boolean.TRUE);
        Assert.assertEquals("obj={z},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new char[]{'z'}, Boolean.FALSE);
        Assert.assertEquals("obj=<size=1>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new double[]{1.0}, Boolean.TRUE);
        Assert.assertEquals("obj={1.0},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new double[]{1.0}, Boolean.FALSE);
        Assert.assertEquals("obj=<size=1>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new float[]{1.0f}, Boolean.TRUE);
        Assert.assertEquals("obj={1.0},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new float[]{1.0f}, Boolean.FALSE);
        Assert.assertEquals("obj=<size=1>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new boolean[]{true}, Boolean.TRUE);
        Assert.assertEquals("obj={true},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new boolean[]{true}, Boolean.FALSE);
        Assert.assertEquals("obj=<size=1>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new String[]{"x"}, Boolean.TRUE);
        Assert.assertEquals("obj={x},", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new String[]{"x"}, Boolean.FALSE);
        Assert.assertEquals("obj=<size=1>,", sb.toString());

        sb = new StringBuffer();
        style.append(sb, "obj", new Person(), Boolean.FALSE);
        Assert.assertEquals("obj=<MyStyleTest.Person>,", sb.toString());
    }

    @Test
    public void testAppendToStringAndSuper() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer sb = new StringBuffer();
        style.appendToString(sb, null);
        Assert.assertEquals("", sb.toString());

        style.appendToString(sb, "Person@123[]");
        Assert.assertEquals("", sb.toString());

        style.appendToString(sb, "Person@123[a=1,b=2]");
        Assert.assertEquals("a=1,b=2,", sb.toString());

        sb = new StringBuffer();
        style.appendSuper(sb, null);
        Assert.assertEquals("", sb.toString());

        style.appendSuper(sb, "Person@123[a=1]");
        Assert.assertEquals("a=1,", sb.toString());

        ToStringStyle multiStyle = ToStringStyle.MULTI_LINE_STYLE;
        sb = new StringBuffer();
        multiStyle.appendToString(sb, "Person@123[\n  a=1\n]");
        Assert.assertEquals("\n  a=1" + SystemUtils.LINE_SEPARATOR + "  ", sb.toString());
    }

    @Test
    public void testAppendStartAndEndNull() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer sb = new StringBuffer();
        style.appendStart(sb, null);
        Assert.assertEquals("", sb.toString());

        style.appendEnd(sb, null);
        Assert.assertEquals("]", sb.toString());
    }

    @Test
    public void testRegistryAndCyclicReference() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        SelfRef ref = new SelfRef();
        ref.child = ref;

        StringBuffer sb = new StringBuffer();
        style.append(sb, "self", ref, Boolean.TRUE);
        String hex = Integer.toHexString(System.identityHashCode(ref));
        Assert.assertTrue(sb.toString().contains("self=org.apache.commons.lang3.builder.MyStyleTest$SelfRef@" + hex));
        Assert.assertFalse(ToStringStyle.isRegistered(ref));

        ToStringStyle.register(null);
        ToStringStyle.unregister(null);

        Object temp = new Object();
        Assert.assertFalse(ToStringStyle.isRegistered(temp));
        ToStringStyle.register(temp);
        Assert.assertTrue(ToStringStyle.isRegistered(temp));
        ToStringStyle.unregister(temp);
        Assert.assertFalse(ToStringStyle.isRegistered(temp));
        Assert.assertTrue(ToStringStyle.getRegistry().isEmpty());
    }

    @Test
    public void testReflectionAppendArrayDetail() {
        TestStyle style = new TestStyle();
        StringBuffer sb = new StringBuffer();
        style.publicReflectionAppendArrayDetail(sb, "arr", new String[]{"item1", null});
        Assert.assertEquals("{item1,<null>}", sb.toString());
    }

    @Test
    public void testSettersAndGetters() {
        TestStyle style = new TestStyle();

        style.setUseClassName(false);
        Assert.assertFalse(style.isUseClassName());
        style.setUseClassName(true);
        Assert.assertTrue(style.isUseClassName());

        style.setUseShortClassName(true);
        Assert.assertTrue(style.isUseShortClassName());
        style.setUseShortClassName(false);
        Assert.assertFalse(style.isUseShortClassName());

        style.setUseIdentityHashCode(false);
        Assert.assertFalse(style.isUseIdentityHashCode());
        style.setUseIdentityHashCode(true);
        Assert.assertTrue(style.isUseIdentityHashCode());

        style.setUseFieldNames(false);
        Assert.assertFalse(style.isUseFieldNames());
        style.setUseFieldNames(true);
        Assert.assertTrue(style.isUseFieldNames());

        style.setDefaultFullDetail(false);
        Assert.assertFalse(style.isDefaultFullDetail());
        style.setDefaultFullDetail(true);
        Assert.assertTrue(style.isDefaultFullDetail());

        style.setArrayContentDetail(false);
        Assert.assertFalse(style.isArrayContentDetail());
        style.setArrayContentDetail(true);
        Assert.assertTrue(style.isArrayContentDetail());

        style.setArrayStart(null);
        Assert.assertEquals("", style.getArrayStart());
        style.setArrayStart("[[");
        Assert.assertEquals("[[", style.getArrayStart());

        style.setArrayEnd(null);
        Assert.assertEquals("", style.getArrayEnd());
        style.setArrayEnd("]]");
        Assert.assertEquals("]]", style.getArrayEnd());

        style.setArraySeparator(null);
        Assert.assertEquals("", style.getArraySeparator());
        style.setArraySeparator(";");
        Assert.assertEquals(";", style.getArraySeparator());

        style.setContentStart(null);
        Assert.assertEquals("", style.getContentStart());
        style.setContentStart("<start>");
        Assert.assertEquals("<start>", style.getContentStart());

        style.setContentEnd(null);
        Assert.assertEquals("", style.getContentEnd());
        style.setContentEnd("<end>");
        Assert.assertEquals("<end>", style.getContentEnd());

        style.setFieldNameValueSeparator(null);
        Assert.assertEquals("", style.getFieldNameValueSeparator());
        style.setFieldNameValueSeparator("->");
        Assert.assertEquals("->", style.getFieldNameValueSeparator());

        style.setFieldSeparator(null);
        Assert.assertEquals("", style.getFieldSeparator());
        style.setFieldSeparator("|");
        Assert.assertEquals("|", style.getFieldSeparator());

        style.setFieldSeparatorAtStart(true);
        Assert.assertTrue(style.isFieldSeparatorAtStart());
        style.setFieldSeparatorAtStart(false);
        Assert.assertFalse(style.isFieldSeparatorAtStart());

        style.setFieldSeparatorAtEnd(true);
        Assert.assertTrue(style.isFieldSeparatorAtEnd());
        style.setFieldSeparatorAtEnd(false);
        Assert.assertFalse(style.isFieldSeparatorAtEnd());

        style.setNullText(null);
        Assert.assertEquals("", style.getNullText());
        style.setNullText("NULL");
        Assert.assertEquals("NULL", style.getNullText());

        style.setSizeStartText(null);
        Assert.assertEquals("", style.getSizeStartText());
        style.setSizeStartText("(size=");
        Assert.assertEquals("(size=", style.getSizeStartText());

        style.setSizeEndText(null);
        Assert.assertEquals("", style.getSizeEndText());
        style.setSizeEndText(")");
        Assert.assertEquals(")", style.getSizeEndText());

        style.setSummaryObjectStartText(null);
        Assert.assertEquals("", style.getSummaryObjectStartText());
        style.setSummaryObjectStartText("[obj=");
        Assert.assertEquals("[obj=", style.getSummaryObjectStartText());

        style.setSummaryObjectEndText(null);
        Assert.assertEquals("", style.getSummaryObjectEndText());
        style.setSummaryObjectEndText("]");
        Assert.assertEquals("]", style.getSummaryObjectEndText());
    }

    @Test
    public void testRemoveLastFieldSeparator() {
        TestStyle style = new TestStyle();
        style.setFieldSeparator(",");

        StringBuffer sb = new StringBuffer("a,b,");
        style.publicRemoveLastFieldSeparator(sb);
        Assert.assertEquals("a,b", sb.toString());

        sb = new StringBuffer("a,b");
        style.publicRemoveLastFieldSeparator(sb);
        Assert.assertEquals("a,b", sb.toString());

        sb = new StringBuffer();
        style.publicRemoveLastFieldSeparator(sb);
        Assert.assertEquals("", sb.toString());

        style.setFieldSeparator("");
        sb = new StringBuffer("a,b,");
        style.publicRemoveLastFieldSeparator(sb);
        Assert.assertEquals("a,b,", sb.toString());
    }

    @Test
    public void testProtectedHelperMethods() {
        TestStyle style = new TestStyle();

        Assert.assertTrue(style.publicIsFullDetail(null));
        Assert.assertTrue(style.publicIsFullDetail(Boolean.TRUE));
        Assert.assertFalse(style.publicIsFullDetail(Boolean.FALSE));

        Assert.assertEquals("String", style.publicGetShortClassName(String.class));

        StringBuffer sb = new StringBuffer();
        style.publicAppendDetail(sb, "f", "val");
        Assert.assertEquals("val", sb.toString());

        sb = new StringBuffer();
        style.publicAppendDetail(sb, "f", Arrays.asList("1", "2"));
        Assert.assertEquals("[1, 2]", sb.toString());

        sb = new StringBuffer();
        Map<String, String> m = new HashMap<String, String>();
        m.put("k", "v");
        style.publicAppendDetail(sb, "f", m);
        Assert.assertEquals("{k=v}", sb.toString());

        sb = new StringBuffer();
        style.publicAppendSummary(sb, "f", new Person());
        Assert.assertEquals("<MyStyleTest.Person>", sb.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        ToStringStyle[] styles = new ToStringStyle[]{
                ToStringStyle.DEFAULT_STYLE,
                ToStringStyle.NO_FIELD_NAMES_STYLE,
                ToStringStyle.SHORT_PREFIX_STYLE,
                ToStringStyle.SIMPLE_STYLE,
                ToStringStyle.MULTI_LINE_STYLE
        };

        for (ToStringStyle style : styles) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(style);
            oos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object obj = ois.readObject();
            ois.close();

            Assert.assertSame(style, obj);
        }
    }
}
