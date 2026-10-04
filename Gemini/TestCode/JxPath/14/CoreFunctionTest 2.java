package org.apache.commons.jxpath.ri.compiler;

import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.BasicNodeSet;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.JXPathInvalidSyntaxException;
import org.apache.commons.jxpath.NodeSet;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.axes.InitialContext;
import org.apache.commons.jxpath.ri.axes.NodeSetContext;
import org.apache.commons.jxpath.ri.axes.RootContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.junit.Assert;
import org.junit.Test;

public class CoreFunctionTest {

    private static class MockContext extends EvalContext {
        private final List<Object> list;
        private int pos = 0;
        private JXPathContext jxpathContext;
        private NodePointer singleNodePointer;

        public MockContext(EvalContext parent, List<Object> list) {
            super(parent);
            this.list = list == null ? new ArrayList<Object>() : list;
        }

        public void setJXPathContext(JXPathContext context) {
            this.jxpathContext = context;
        }

        public void setSingleNodePointer(NodePointer pointer) {
            this.singleNodePointer = pointer;
        }

        @Override
        public JXPathContext getJXPathContext() {
            if (jxpathContext != null) {
                return jxpathContext;
            }
            return super.getJXPathContext();
        }

        @Override
        public NodePointer getSingleNodePointer() {
            if (singleNodePointer != null) {
                return singleNodePointer;
            }
            return super.getSingleNodePointer();
        }

        @Override
        public NodePointer getCurrentNodePointer() {
            if (pos >= 1 && pos <= list.size()) {
                Object obj = list.get(pos - 1);
                if (obj instanceof NodePointer) {
                    return (NodePointer) obj;
                }
                return NodePointer.newNodePointer(new QName("test"), obj, Locale.ENGLISH);
            }
            return null;
        }

        @Override
        public int getCurrentPosition() {
            return pos;
        }

        @Override
        public boolean nextNode() {
            return setPosition(pos + 1);
        }

        @Override
        public boolean setPosition(int position) {
            this.pos = position;
            return pos >= 1 && pos <= list.size();
        }

        @Override
        public void reset() {
            this.pos = 0;
        }

        @Override
        public boolean hasNext() {
            return pos < list.size();
        }

        @Override
        public Object next() {
            pos++;
            Object obj = list.get(pos - 1);
            if (obj instanceof NodePointer) {
                return obj;
            }
            return NodePointer.newNodePointer(new QName("item"), obj, Locale.ENGLISH);
        }
    }

    private static class DummyNodePointer extends NodePointer {
        private String namespaceURI;
        private QName name;
        private Object value;
        private String lang;

        protected DummyNodePointer(QName name, Object value, String namespaceURI, String lang) {
            super(null, Locale.US);
            this.name = name;
            this.value = value;
            this.namespaceURI = namespaceURI;
            this.lang = lang;
        }

        @Override
        public QName getName() {
            return name;
        }

        @Override
        public Object getBaseValue() {
            return value;
        }

        @Override
        public Object getImmediateNode() {
            return value;
        }

        @Override
        public boolean isCollection() {
            return false;
        }

        @Override
        public int getLength() {
            return 1;
        }

        @Override
        public boolean isLeaf() {
            return true;
        }

        @Override
        public String getNamespaceURI() {
            return namespaceURI;
        }

        @Override
        public boolean isLanguage(String lang) {
            return this.lang != null && this.lang.equalsIgnoreCase(lang);
        }

        @Override
        public NodePointer getPointerByID(JXPathContext context, String id) {
            return new DummyNodePointer(new QName("idNode"), id, null, null);
        }

        @Override
        public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
            return 0;
        }

        @Override
        public void setValue(Object value) {
            this.value = value;
        }
    }

    private static class DummyJXPathContext extends JXPathContext {
        protected DummyJXPathContext() {
            super(null, null);
        }

        @Override
        public Object getValue(String xpath) { return null; }
        @Override
        public Object getValue(String xpath, Class requiredType) { return null; }
        @Override
        public void setValue(String xpath, Object value) {}
        @Override
        public org.apache.commons.jxpath.Pointer createPath(String xpath) { return null; }
        @Override
        public org.apache.commons.jxpath.Pointer createPathAndSetValue(String xpath, Object value) { return null; }
        @Override
        public void removePath(String xpath) {}
        @Override
        public void removeAll(String xpath) {}
        @Override
        public java.util.Iterator iterate(String xpath) { return null; }
        @Override
        public org.apache.commons.jxpath.Pointer getPointer(String xpath) { return null; }
        @Override
        public java.util.Iterator iteratePointers(String xpath) { return null; }
        @Override
        public JXPathContext getRelativeContext(org.apache.commons.jxpath.Pointer pointer) { return null; }
        @Override
        public org.apache.commons.jxpath.Pointer getContextPointer() {
            return new DummyNodePointer(new QName("root"), "rootVal", null, "en");
        }

        @Override
        public NodeSet getNodeSetByKey(String key, Object value) {
            BasicNodeSet bns = new BasicNodeSet();
            bns.add(new DummyNodePointer(new QName("keyNode"), key + ":" + value, null, "en"));
            return bns;
        }

        @Override
        public DecimalFormatSymbols getDecimalFormatSymbols(String name) {
            return new DecimalFormatSymbols(Locale.GERMAN);
        }

        @Override
        public Locale getLocale() {
            return Locale.US;
        }
    }

    @Test
    public void testGetFunctionCodeAndArgs() {
        Expression arg1 = new Constant("a");
        Expression arg2 = new Constant("b");
        Expression arg3 = new Constant("c");
        CoreFunction cf = new CoreFunction(Compiler.FUNCTION_CONCAT, new Expression[]{arg1, arg2, arg3});

        Assert.assertEquals(Compiler.FUNCTION_CONCAT, cf.getFunctionCode());
        Assert.assertEquals(3, cf.getArgumentCount());
        Assert.assertSame(arg1, cf.getArg1());
        Assert.assertSame(arg2, cf.getArg2());
        Assert.assertSame(arg3, cf.getArg3());

        CoreFunction noArgs = new CoreFunction(Compiler.FUNCTION_LAST, null);
        Assert.assertEquals(0, noArgs.getArgumentCount());
    }

    @Test
    public void testGetFunctionNameAndToString() {
        int[] codes = new int[]{
            Compiler.FUNCTION_LAST,
            Compiler.FUNCTION_POSITION,
            Compiler.FUNCTION_COUNT,
            Compiler.FUNCTION_ID,
            Compiler.FUNCTION_LOCAL_NAME,
            Compiler.FUNCTION_NAMESPACE_URI,
            Compiler.FUNCTION_NAME,
            Compiler.FUNCTION_STRING,
            Compiler.FUNCTION_CONCAT,
            Compiler.FUNCTION_STARTS_WITH,
            Compiler.FUNCTION_CONTAINS,
            Compiler.FUNCTION_SUBSTRING_BEFORE,
            Compiler.FUNCTION_SUBSTRING_AFTER,
            Compiler.FUNCTION_SUBSTRING,
            Compiler.FUNCTION_STRING_LENGTH,
            Compiler.FUNCTION_NORMALIZE_SPACE,
            Compiler.FUNCTION_TRANSLATE,
            Compiler.FUNCTION_BOOLEAN,
            Compiler.FUNCTION_NOT,
            Compiler.FUNCTION_TRUE,
            Compiler.FUNCTION_FALSE,
            Compiler.FUNCTION_LANG,
            Compiler.FUNCTION_NUMBER,
            Compiler.FUNCTION_SUM,
            Compiler.FUNCTION_FLOOR,
            Compiler.FUNCTION_CEILING,
            Compiler.FUNCTION_ROUND,
            Compiler.FUNCTION_KEY,
            Compiler.FUNCTION_FORMAT_NUMBER
        };

        String[] expectedNames = new String[]{
            "last", "position", "count", "id", "local-name", "namespace-uri", "name",
            "string", "concat", "starts-with", "contains", "substring-before",
            "substring-after", "substring", "string-length", "normalize-space",
            "translate", "boolean", "not", "true", "false", "lang", "number",
            "sum", "floor", "ceiling", "round", "key", "format-number"
        };

        for (int i = 0; i < codes.length; i++) {
            CoreFunction cf = new CoreFunction(codes[i], null);
            Assert.assertEquals(expectedNames[i], cf.getFunctionName());
            Assert.assertEquals(expectedNames[i] + "()", cf.toString());
        }

        CoreFunction unknown = new CoreFunction(9999, null);
        Assert.assertEquals("unknownFunction9999()", unknown.getFunctionName());

        CoreFunction withArgs = new CoreFunction(Compiler.FUNCTION_CONCAT, new Expression[]{
            new Constant("hello"), new Constant("world")
        });
        Assert.assertEquals("concat('hello', 'world')", withArgs.toString());
    }

    @Test
    public void testComputeContextDependent() {
        CoreFunction last = new CoreFunction(Compiler.FUNCTION_LAST, null);
        Assert.assertTrue(last.computeContextDependent());

        CoreFunction pos = new CoreFunction(Compiler.FUNCTION_POSITION, null);
        Assert.assertTrue(pos.computeContextDependent());

        int[] zeroArgContextDep = new int[]{
            Compiler.FUNCTION_BOOLEAN, Compiler.FUNCTION_LOCAL_NAME,
            Compiler.FUNCTION_NAME, Compiler.FUNCTION_NAMESPACE_URI,
            Compiler.FUNCTION_STRING, Compiler.FUNCTION_LANG, Compiler.FUNCTION_NUMBER
        };

        for (int code : zeroArgContextDep) {
            CoreFunction noArg = new CoreFunction(code, null);
            Assert.assertTrue(noArg.computeContextDependent());

            CoreFunction withArg = new CoreFunction(code, new Expression[]{new Constant("x")});
            Assert.assertFalse(withArg.computeContextDependent());
        }

        int[] nonContextDep = new int[]{
            Compiler.FUNCTION_COUNT, Compiler.FUNCTION_ID, Compiler.FUNCTION_CONCAT,
            Compiler.FUNCTION_STARTS_WITH, Compiler.FUNCTION_CONTAINS,
            Compiler.FUNCTION_SUBSTRING_BEFORE, Compiler.FUNCTION_SUBSTRING_AFTER,
            Compiler.FUNCTION_SUBSTRING, Compiler.FUNCTION_STRING_LENGTH,
            Compiler.FUNCTION_NORMALIZE_SPACE, Compiler.FUNCTION_TRANSLATE,
            Compiler.FUNCTION_NOT, Compiler.FUNCTION_TRUE, Compiler.FUNCTION_FALSE,
            Compiler.FUNCTION_SUM, Compiler.FUNCTION_FLOOR, Compiler.FUNCTION_CEILING,
            Compiler.FUNCTION_ROUND
        };

        for (int code : nonContextDep) {
            CoreFunction cf = new CoreFunction(code, new Expression[]{new Constant("x")});
            Assert.assertFalse(cf.computeContextDependent());
        }

        CoreFunction fn2 = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER, new Expression[]{
            new Constant(12), new Constant("#")
        });
        Assert.assertTrue(fn2.computeContextDependent());

        CoreFunction fn3 = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER, new Expression[]{
            new Constant(12), new Constant("#"), new Constant("de")
        });
        Assert.assertFalse(fn3.computeContextDependent());

        CoreFunction unknown = new CoreFunction(9999, null);
        Assert.assertFalse(unknown.computeContextDependent());
    }

    @Test
    public void testFunctionLastAndPosition() {
        MockContext ctx = new MockContext(null, Arrays.asList("A", "B", "C"));
        ctx.setPosition(2);

        CoreFunction last = new CoreFunction(Compiler.FUNCTION_LAST, null);
        Object lastVal = last.compute(ctx);
        Assert.assertEquals(new Double(3), lastVal);
        Assert.assertEquals(2, ctx.getCurrentPosition());

        MockContext ctxZero = new MockContext(null, Collections.emptyList());
        Object lastZero = last.compute(ctxZero);
        Assert.assertEquals(new Double(0), lastZero);

        CoreFunction pos = new CoreFunction(Compiler.FUNCTION_POSITION, null);
        ctx.setPosition(2);
        Assert.assertEquals(new Integer(2), pos.compute(ctx));
    }

    @Test
    public void testFunctionCount() {
        MockContext ctx = new MockContext(null, Arrays.asList("A", "B"));
        
        CoreFunction countColl = new CoreFunction(Compiler.FUNCTION_COUNT, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return Arrays.asList("X", "Y", "Z"); }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Assert.assertEquals(new Double(3), countColl.compute(ctx));

        CoreFunction countCtx = new CoreFunction(Compiler.FUNCTION_COUNT, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return new MockContext(null, Arrays.asList("1", "2")); }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Assert.assertEquals(new Double(2), countCtx.compute(ctx));

        CoreFunction countNull = new CoreFunction(Compiler.FUNCTION_COUNT, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return null; }
                @Override
                public Object computeValue(EvalContext c) { return null; }
            }
        });
        Assert.assertEquals(new Double(0), countNull.compute(ctx));

        CoreFunction countObj = new CoreFunction(Compiler.FUNCTION_COUNT, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return "SingleValue"; }
                @Override
                public Object computeValue(EvalContext c) { return "SingleValue"; }
            }
        });
        Assert.assertEquals(new Double(1), countObj.compute(ctx));

        final NodePointer np = NodePointer.newNodePointer(new QName("test"), "NodeVal", Locale.US);
        CoreFunction countNodePointer = new CoreFunction(Compiler.FUNCTION_COUNT, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return np; }
                @Override
                public Object computeValue(EvalContext c) { return np; }
            }
        });
        Assert.assertEquals(new Double(1), countNodePointer.compute(ctx));
    }

    @Test
    public void testFunctionLang() {
        MockContext ctx = new MockContext(null, Collections.emptyList());
        DummyNodePointer pointer = new DummyNodePointer(new QName("item"), "val", null, "en-US");
        ctx.setSingleNodePointer(pointer);

        CoreFunction lang = new CoreFunction(Compiler.FUNCTION_LANG, new Expression[]{
            new Constant("en-us")
        });
        Assert.assertEquals(Boolean.TRUE, lang.compute(ctx));

        CoreFunction langMismatch = new CoreFunction(Compiler.FUNCTION_LANG, new Expression[]{
            new Constant("fr")
        });
        Assert.assertEquals(Boolean.FALSE, langMismatch.compute(ctx));

        ctx.setSingleNodePointer(null);
        Assert.assertEquals(Boolean.FALSE, lang.compute(ctx));
    }

    @Test
    public void testFunctionID() {
        DummyJXPathContext jxContext = new DummyJXPathContext();
        MockContext ctx = new MockContext(null, Collections.emptyList());
        ctx.setJXPathContext(jxContext);

        CoreFunction idFunc = new CoreFunction(Compiler.FUNCTION_ID, new Expression[]{
            new Constant("targetID")
        });
        Object result = idFunc.compute(ctx);
        Assert.assertTrue(result instanceof DummyNodePointer);
        Assert.assertEquals("targetID", ((DummyNodePointer) result).getBaseValue());
    }

    @Test
    public void testFunctionKey() {
        DummyJXPathContext jxContext = new DummyJXPathContext();
        MockContext ctx = new MockContext(null, Collections.emptyList());
        ctx.setJXPathContext(jxContext);

        CoreFunction keySingle = new CoreFunction(Compiler.FUNCTION_KEY, new Expression[]{
            new Constant("myKey"), new Constant("val1")
        });
        Object res1 = keySingle.compute(ctx);
        Assert.assertTrue(res1 instanceof NodeSetContext);

        CoreFunction keyEmptyCtx = new CoreFunction(Compiler.FUNCTION_KEY, new Expression[]{
            new Constant("myKey"), new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return new MockContext(null, Collections.emptyList()); }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Object resEmpty = keyEmptyCtx.compute(ctx);
        Assert.assertTrue(resEmpty instanceof BasicNodeSet);

        CoreFunction keyMultiCtx = new CoreFunction(Compiler.FUNCTION_KEY, new Expression[]{
            new Constant("myKey"), new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) {
                    DummyNodePointer p1 = new DummyNodePointer(new QName("k1"), "val1", null, null);
                    DummyNodePointer p2 = new DummyNodePointer(new QName("k2"), "val2", null, null);
                    return new MockContext(null, Arrays.asList(p1, p2));
                }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Object resMulti = keyMultiCtx.compute(ctx);
        Assert.assertTrue(resMulti instanceof NodeSetContext);
    }

    @Test
    public void testFunctionNamesAndURIs() {
        DummyNodePointer p = new DummyNodePointer(new QName("ns", "local"), "val", "http://example.com", null);
        MockContext ctx = new MockContext(null, Collections.singletonList(p));
        ctx.setPosition(1);

        CoreFunction fnURI0 = new CoreFunction(Compiler.FUNCTION_NAMESPACE_URI, null);
        Assert.assertEquals("http://example.com", fnURI0.compute(ctx));

        CoreFunction fnLocal0 = new CoreFunction(Compiler.FUNCTION_LOCAL_NAME, null);
        Assert.assertEquals("local", fnLocal0.compute(ctx));

        CoreFunction fnName0 = new CoreFunction(Compiler.FUNCTION_NAME, null);
        Assert.assertEquals("ns:local", fnName0.compute(ctx));

        DummyNodePointer pNullURI = new DummyNodePointer(new QName("localOnly"), "val", null, null);
        MockContext ctxNullURI = new MockContext(null, Collections.singletonList(pNullURI));
        ctxNullURI.setPosition(1);
        Assert.assertEquals("", fnURI0.compute(ctxNullURI));

        CoreFunction fnURI1 = new CoreFunction(Compiler.FUNCTION_NAMESPACE_URI, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return new MockContext(null, Collections.singletonList(p)); }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Assert.assertEquals("http://example.com", fnURI1.compute(ctx));

        CoreFunction fnURI1Empty = new CoreFunction(Compiler.FUNCTION_NAMESPACE_URI, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return new MockContext(null, Collections.emptyList()); }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Assert.assertEquals("", fnURI1Empty.compute(ctx));

        CoreFunction fnURI1NonCtx = new CoreFunction(Compiler.FUNCTION_NAMESPACE_URI, new Expression[]{
            new Constant("not-a-context")
        });
        Assert.assertEquals("", fnURI1NonCtx.compute(ctx));

        CoreFunction fnLocal1 = new CoreFunction(Compiler.FUNCTION_LOCAL_NAME, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return new MockContext(null, Collections.singletonList(p)); }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Assert.assertEquals("local", fnLocal1.compute(ctx));

        CoreFunction fnLocal1Empty = new CoreFunction(Compiler.FUNCTION_LOCAL_NAME, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return new MockContext(null, Collections.emptyList()); }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Assert.assertEquals("", fnLocal1Empty.compute(ctx));

        CoreFunction fnName1 = new CoreFunction(Compiler.FUNCTION_NAME, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return new MockContext(null, Collections.singletonList(p)); }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Assert.assertEquals("ns:local", fnName1.compute(ctx));

        CoreFunction fnName1Empty = new CoreFunction(Compiler.FUNCTION_NAME, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return new MockContext(null, Collections.emptyList()); }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Assert.assertEquals("", fnName1Empty.compute(ctx));
    }

    @Test
    public void testFunctionString() {
        DummyNodePointer p = new DummyNodePointer(new QName("test"), "sampleText", null, null);
        MockContext ctx = new MockContext(null, Collections.singletonList(p));
        ctx.setPosition(1);

        CoreFunction str0 = new CoreFunction(Compiler.FUNCTION_STRING, null);
        Assert.assertEquals("sampleText", str0.compute(ctx));

        CoreFunction str1 = new CoreFunction(Compiler.FUNCTION_STRING, new Expression[]{
            new Constant(123)
        });
        Assert.assertEquals("123", str1.compute(ctx));
    }

    @Test
    public void testFunctionConcat() {
        MockContext ctx = new MockContext(null, Collections.emptyList());
        CoreFunction concat = new CoreFunction(Compiler.FUNCTION_CONCAT, new Expression[]{
            new Constant("a"), new Constant("b"), new Constant("c")
        });
        Assert.assertEquals("abc", concat.compute(ctx));
    }

    @Test
    public void testFunctionStartsWithAndContains() {
        MockContext ctx = new MockContext(null, Collections.emptyList());
        CoreFunction swTrue = new CoreFunction(Compiler.FUNCTION_STARTS_WITH, new Expression[]{
            new Constant("testing"), new Constant("test")
        });
        Assert.assertEquals(Boolean.TRUE, swTrue.compute(ctx));

        CoreFunction swFalse = new CoreFunction(Compiler.FUNCTION_STARTS_WITH, new Expression[]{
            new Constant("testing"), new Constant("ting")
        });
        Assert.assertEquals(Boolean.FALSE, swFalse.compute(ctx));

        CoreFunction contTrue = new CoreFunction(Compiler.FUNCTION_CONTAINS, new Expression[]{
            new Constant("testing"), new Constant("est")
        });
        Assert.assertEquals(Boolean.TRUE, contTrue.compute(ctx));

        CoreFunction contFalse = new CoreFunction(Compiler.FUNCTION_CONTAINS, new Expression[]{
            new Constant("testing"), new Constant("xyz")
        });
        Assert.assertEquals(Boolean.FALSE, contFalse.compute(ctx));
    }

    @Test
    public void testFunctionSubstringBeforeAndAfter() {
        MockContext ctx = new MockContext(null, Collections.emptyList());
        CoreFunction sbFound = new CoreFunction(Compiler.FUNCTION_SUBSTRING_BEFORE, new Expression[]{
            new Constant("1999/04/01"), new Constant("/")
        });
        Assert.assertEquals("1999", sbFound.compute(ctx));

        CoreFunction sbNotFound = new CoreFunction(Compiler.FUNCTION_SUBSTRING_BEFORE, new Expression[]{
            new Constant("1999/04/01"), new Constant(";")
        });
        Assert.assertEquals("", sbNotFound.compute(ctx));

        CoreFunction saFound = new CoreFunction(Compiler.FUNCTION_SUBSTRING_AFTER, new Expression[]{
            new Constant("1999/04/01"), new Constant("/")
        });
        Assert.assertEquals("04/01", saFound.compute(ctx));

        CoreFunction saNotFound = new CoreFunction(Compiler.FUNCTION_SUBSTRING_AFTER, new Expression[]{
            new Constant("1999/04/01"), new Constant(";")
        });
        Assert.assertEquals("", saNotFound.compute(ctx));
    }

    @Test
    public void testFunctionSubstring() {
        MockContext ctx = new MockContext(null, Collections.emptyList());

        CoreFunction sub2 = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("12345"), new Constant(2)
        });
        Assert.assertEquals("2345", sub2.compute(ctx));

        CoreFunction sub2Negative = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("12345"), new Constant(-2)
        });
        Assert.assertEquals("12345", sub2Negative.compute(ctx));

        CoreFunction sub3 = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("12345"), new Constant(2), new Constant(3)
        });
        Assert.assertEquals("234", sub3.compute(ctx));

        CoreFunction subNaN = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("12345"), new Constant(Double.NaN)
        });
        Assert.assertEquals("", subNaN.compute(ctx));

        CoreFunction subTooLargeFrom = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("12345"), new Constant(10)
        });
        Assert.assertEquals("", subTooLargeFrom.compute(ctx));

        CoreFunction subNegLen = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("12345"), new Constant(2), new Constant(-1)
        });
        Assert.assertEquals("", subNegLen.compute(ctx));

        CoreFunction subZeroTo = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("12345"), new Constant(-5), new Constant(2)
        });
        Assert.assertEquals("", subZeroTo.compute(ctx));

        CoreFunction subToExceeds = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("12345"), new Constant(-1), new Constant(10)
        });
        Assert.assertEquals("12345", subToExceeds.compute(ctx));

        CoreFunction subFromUnder1 = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("12345"), new Constant(0), new Constant(3)
        });
        Assert.assertEquals("12", subFromUnder1.compute(ctx));
    }

    @Test
    public void testFunctionStringLength() {
        DummyNodePointer p = new DummyNodePointer(new QName("test"), "abcde", null, null);
        MockContext ctx = new MockContext(null, Collections.singletonList(p));
        ctx.setPosition(1);

        CoreFunction len0 = new CoreFunction(Compiler.FUNCTION_STRING_LENGTH, null);
        Assert.assertEquals(new Double(5), len0.compute(ctx));

        CoreFunction len1 = new CoreFunction(Compiler.FUNCTION_STRING_LENGTH, new Expression[]{
            new Constant("hello")
        });
        Assert.assertEquals(new Double(5), len1.compute(ctx));
    }

    @Test
    public void testFunctionNormalizeSpace() {
        MockContext ctx = new MockContext(null, Collections.emptyList());
        CoreFunction norm = new CoreFunction(Compiler.FUNCTION_NORMALIZE_SPACE, new Expression[]{
            new Constant(" \t  hello \r\n  world   \t")
        });
        Assert.assertEquals("hello world", norm.compute(ctx));

        CoreFunction normEmpty = new CoreFunction(Compiler.FUNCTION_NORMALIZE_SPACE, new Expression[]{
            new Constant("   \t\r\n ")
        });
        Assert.assertEquals("", normEmpty.compute(ctx));
    }

    @Test
    public void testFunctionTranslate() {
        MockContext ctx = new MockContext(null, Collections.emptyList());
        CoreFunction tr = new CoreFunction(Compiler.FUNCTION_TRANSLATE, new Expression[]{
            new Constant("--abc--"), new Constant("abc-"), new Constant("ABC")
        });
        Assert.assertEquals("ABC", tr.compute(ctx));
    }

    @Test
    public void testFunctionBooleanAndNot() {
        MockContext ctx = new MockContext(null, Collections.emptyList());
        CoreFunction boolTrue = new CoreFunction(Compiler.FUNCTION_BOOLEAN, new Expression[]{
            new Constant("true")
        });
        Assert.assertEquals(Boolean.TRUE, boolTrue.compute(ctx));

        CoreFunction boolFalse = new CoreFunction(Compiler.FUNCTION_BOOLEAN, new Expression[]{
            new Constant("")
        });
        Assert.assertEquals(Boolean.FALSE, boolFalse.compute(ctx));

        CoreFunction notTrue = new CoreFunction(Compiler.FUNCTION_NOT, new Expression[]{
            new Constant("true")
        });
        Assert.assertEquals(Boolean.FALSE, notTrue.compute(ctx));

        CoreFunction notFalse = new CoreFunction(Compiler.FUNCTION_NOT, new Expression[]{
            new Constant("")
        });
        Assert.assertEquals(Boolean.TRUE, notFalse.compute(ctx));
    }

    @Test
    public void testFunctionTrueFalseNull() {
        MockContext ctx = new MockContext(null, Collections.emptyList());
        CoreFunction fnTrue = new CoreFunction(Compiler.FUNCTION_TRUE, null);
        Assert.assertEquals(Boolean.TRUE, fnTrue.compute(ctx));

        CoreFunction fnFalse = new CoreFunction(Compiler.FUNCTION_FALSE, null);
        Assert.assertEquals(Boolean.FALSE, fnFalse.compute(ctx));

        CoreFunction fnNull = new CoreFunction(Compiler.FUNCTION_NULL, null);
        Assert.assertNull(fnNull.compute(ctx));
    }

    @Test
    public void testFunctionNumber() {
        DummyNodePointer p = new DummyNodePointer(new QName("test"), "42.5", null, null);
        MockContext ctx = new MockContext(null, Collections.singletonList(p));
        ctx.setPosition(1);

        CoreFunction num0 = new CoreFunction(Compiler.FUNCTION_NUMBER, null);
        Assert.assertEquals(new Double(42.5), num0.compute(ctx));

        CoreFunction num1 = new CoreFunction(Compiler.FUNCTION_NUMBER, new Expression[]{
            new Constant("100.25")
        });
        Assert.assertEquals(new Double(100.25), num1.compute(ctx));
    }

    @Test
    public void testFunctionSum() {
        DummyNodePointer p1 = new DummyNodePointer(new QName("n1"), "10.5", null, null);
        DummyNodePointer p2 = new DummyNodePointer(new QName("n2"), "20.5", null, null);
        MockContext subCtx = new MockContext(null, Arrays.asList(p1, p2));
        MockContext ctx = new MockContext(null, Collections.emptyList());

        CoreFunction sumCtx = new CoreFunction(Compiler.FUNCTION_SUM, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return subCtx; }
                @Override
                public Object computeValue(EvalContext c) { return compute(c); }
            }
        });
        Assert.assertEquals(new Double(31.0), sumCtx.compute(ctx));

        CoreFunction sumNull = new CoreFunction(Compiler.FUNCTION_SUM, new Expression[]{
            new Expression(new Expression[0]) {
                @Override
                public Object compute(EvalContext c) { return null; }
                @Override
                public Object computeValue(EvalContext c) { return null; }
            }
        });
        Assert.assertEquals(new Double(0), sumNull.compute(ctx));

        CoreFunction sumInvalid = new CoreFunction(Compiler.FUNCTION_SUM, new Expression[]{
            new Constant("invalid")
        });
        try {
            sumInvalid.compute(ctx);
            Assert.fail("Expected JXPathException for invalid argument type in sum()");
        } catch (JXPathException expected) {
            // success
        }
    }

    @Test
    public void testFunctionMathRounding() {
        MockContext ctx = new MockContext(null, Collections.emptyList());

        CoreFunction floor = new CoreFunction(Compiler.FUNCTION_FLOOR, new Expression[]{
            new Constant(2.9)
        });
        Assert.assertEquals(new Double(2.0), floor.compute(ctx));

        CoreFunction ceil = new CoreFunction(Compiler.FUNCTION_CEILING, new Expression[]{
            new Constant(2.1)
        });
        Assert.assertEquals(new Double(3.0), ceil.compute(ctx));

        CoreFunction round = new CoreFunction(Compiler.FUNCTION_ROUND, new Expression[]{
            new Constant(2.5)
        });
        Assert.assertEquals(new Double(3.0), round.compute(ctx));
    }

    @Test
    public void testFunctionFormatNumber() {
        DummyJXPathContext jxContext = new DummyJXPathContext();
        MockContext ctx = new MockContext(null, Collections.emptyList());
        ctx.setJXPathContext(jxContext);

        CoreFunction fn3 = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER, new Expression[]{
            new Constant(1234.56), new Constant("#,###.00"), new Constant("de")
        });
        String formattedGerman = (String) fn3.compute(ctx);
        Assert.assertEquals("1.234,56", formattedGerman);

        CoreFunction fn2WithoutPtr = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER, new Expression[]{
            new Constant(1234.56), new Constant("#,###.00")
        });
        String formattedUS = (String) fn2WithoutPtr.compute(ctx);
        Assert.assertEquals("1,234.56", formattedUS);

        DummyNodePointer germanPtr = new DummyNodePointer(new QName("p"), "val", null, null) {
            @Override
            public Locale getLocale() {
                return Locale.GERMAN;
            }
        };
        MockContext ctxWithPtr = new MockContext(null, Collections.singletonList(germanPtr));
        ctxWithPtr.setPosition(1);
        ctxWithPtr.setJXPathContext(jxContext);

        String formattedWithPtr = (String) fn2WithoutPtr.compute(ctxWithPtr);
        Assert.assertEquals("1.234,56", formattedWithPtr);
    }

    @Test
    public void testUnknownFunction() {
        MockContext ctx = new MockContext(null, Collections.emptyList());
        CoreFunction unknown = new CoreFunction(99999, new Expression[0]);
        Assert.assertNull(unknown.compute(ctx));
    }

    @Test
    public void testArgumentCountValidation() {
        MockContext ctx = new MockContext(null, Collections.emptyList());

        CoreFunction lastWithArgs = new CoreFunction(Compiler.FUNCTION_LAST, new Expression[]{
            new Constant(1)
        });
        try {
            lastWithArgs.compute(ctx);
            Assert.fail("Expected JXPathInvalidSyntaxException");
        } catch (JXPathInvalidSyntaxException e) {
            // expected
        }

        CoreFunction concatWithOneArg = new CoreFunction(Compiler.FUNCTION_CONCAT, new Expression[]{
            new Constant("1")
        });
        try {
            concatWithOneArg.compute(ctx);
            Assert.fail("Expected JXPathInvalidSyntaxException");
        } catch (JXPathInvalidSyntaxException e) {
            // expected
        }

        CoreFunction subWithOneArg = new CoreFunction(Compiler.FUNCTION_SUBSTRING, new Expression[]{
            new Constant("1")
        });
        try {
            subWithOneArg.compute(ctx);
            Assert.fail("Expected JXPathInvalidSyntaxException");
        } catch (JXPathInvalidSyntaxException e) {
            // expected
        }

        CoreFunction fmtWithOneArg = new CoreFunction(Compiler.FUNCTION_FORMAT_NUMBER, new Expression[]{
            new Constant("1")
        });
        try {
            fmtWithOneArg.compute(ctx);
            Assert.fail("Expected JXPathInvalidSyntaxException");
        } catch (JXPathInvalidSyntaxException e) {
            // expected
        }
    }
}
