package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class JsonMappingExceptionTest {

    @Test
    public void testReferenceConstructorsAndGetters() {
        JsonMappingException.Reference ref1 = new JsonMappingException.Reference();
        Assert.assertNull(ref1.getFrom());
        Assert.assertNull(ref1.getFieldName());
        Assert.assertEquals(-1, ref1.getIndex());
        Assert.assertEquals("UNKNOWN[?]", ref1.getDescription());
        Assert.assertEquals("UNKNOWN[?]", ref1.toString());

        JsonMappingException.Reference ref2 = new JsonMappingException.Reference("sourceObj");
        Assert.assertEquals("sourceObj", ref2.getFrom());
        Assert.assertEquals("java.lang.String[?]", ref2.getDescription());

        JsonMappingException.Reference ref3 = new JsonMappingException.Reference(String.class, "myField");
        Assert.assertEquals(String.class, ref3.getFrom());
        Assert.assertEquals("myField", ref3.getFieldName());
        Assert.assertEquals(-1, ref3.getIndex());
        Assert.assertEquals("java.lang.String[\"myField\"]", ref3.getDescription());

        JsonMappingException.Reference ref4 = new JsonMappingException.Reference(new Integer[0][0], 5);
        Assert.assertEquals(5, ref4.getIndex());
        Assert.assertNull(ref4.getFieldName());
        Assert.assertEquals("java.lang.Integer[][][5]", ref4.getDescription());

        JsonMappingException.Reference ref5 = new JsonMappingException.Reference(int[][].class, 2);
        Assert.assertEquals("int[][][2]", ref5.getDescription());
    }

    @Test(expected = NullPointerException.class)
    public void testReferenceConstructorNullFieldName() {
        new JsonMappingException.Reference("src", (String) null);
    }

    @Test
    public void testReferencePackagePrivateSettersAndSerialization() {
        JsonMappingException.Reference ref = new JsonMappingException.Reference();
        ref.setFieldName("fieldA");
        ref.setIndex(3);
        ref.setDescription("customDesc");

        Assert.assertEquals("fieldA", ref.getFieldName());
        Assert.assertEquals(3, ref.getIndex());
        Assert.assertEquals("customDesc", ref.getDescription());
        Assert.assertSame(ref, ref.writeReplace());
    }

    @Test
    public void testDeprecatedConstructors() {
        JsonLocation loc = new JsonLocation("src", 10L, 1, 1);
        Throwable cause = new RuntimeException("cause");

        JsonMappingException ex1 = new JsonMappingException("msg1");
        Assert.assertEquals("msg1", ex1.getMessage());

        JsonMappingException ex2 = new JsonMappingException("msg2", cause);
        Assert.assertEquals("msg2", ex2.getMessage());
        Assert.assertSame(cause, ex2.getCause());

        JsonMappingException ex3 = new JsonMappingException("msg3", loc);
        Assert.assertEquals("msg3", ex3.getMessage());
        Assert.assertEquals(loc, ex3.getLocation());

        JsonMappingException ex4 = new JsonMappingException("msg4", loc, cause);
        Assert.assertEquals("msg4", ex4.getMessage());
        Assert.assertEquals(loc, ex4.getLocation());
        Assert.assertSame(cause, ex4.getCause());
    }

    @Test
    public void testConstructorsWithProcessor() throws Exception {
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(new StringReader("{}"));
        Throwable cause = new IOException("io-err");
        JsonLocation loc = new JsonLocation("src", 5L, 2, 3);

        JsonMappingException ex1 = new JsonMappingException(p, "parseErr");
        Assert.assertSame(p, ex1.getProcessor());
        Assert.assertEquals(p.getTokenLocation(), ex1.getLocation());

        JsonMappingException ex2 = new JsonMappingException(p, "parseErr2", cause);
        Assert.assertSame(p, ex2.getProcessor());
        Assert.assertSame(cause, ex2.getCause());
        Assert.assertEquals(p.getTokenLocation(), ex2.getLocation());

        Closeable customProc = new Closeable() {
            @Override
            public void close() {}
        };
        JsonMappingException ex3 = new JsonMappingException(customProc, "procErr", loc);
        Assert.assertSame(customProc, ex3.getProcessor());
        Assert.assertEquals(loc, ex3.getLocation());

        p.close();
    }

    @Test
    public void testFactoryMethodsFromParserAndGenerator() throws Exception {
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(new StringReader("[]"));
        JsonGenerator g = f.createGenerator(new ByteArrayOutputStream());
        Throwable cause = new RuntimeException("err");

        JsonMappingException ex1 = JsonMappingException.from(p, "pMsg");
        Assert.assertSame(p, ex1.getProcessor());
        Assert.assertEquals("pMsg", ex1.getMessage());

        JsonMappingException ex2 = JsonMappingException.from(p, "pMsg2", cause);
        Assert.assertSame(p, ex2.getProcessor());
        Assert.assertSame(cause, ex2.getCause());

        JsonMappingException ex3 = JsonMappingException.from(g, "gMsg");
        Assert.assertSame(g, ex3.getProcessor());
        Assert.assertEquals("gMsg", ex3.getMessage());

        JsonMappingException ex4 = JsonMappingException.from(g, "gMsg2", cause);
        Assert.assertSame(g, ex4.getProcessor());
        Assert.assertSame(cause, ex4.getCause());

        p.close();
        g.close();
    }

    @Test
    public void testFromUnexpectedIOE() {
        IOException ioe = new IOException("disk error");
        JsonMappingException jme = JsonMappingException.fromUnexpectedIOE(ioe);
        Assert.assertNull(jme.getProcessor());
        Assert.assertTrue(jme.getMessage().contains("Unexpected IOException"));
        Assert.assertTrue(jme.getMessage().contains("java.io.IOException"));
        Assert.assertTrue(jme.getMessage().contains("disk error"));
    }

    @Test
    public void testWrapWithPathVariations() {
        Throwable root = new IllegalArgumentException("bad arg");
        JsonMappingException ex1 = JsonMappingException.wrapWithPath(root, "myBean", "prop");
        Assert.assertSame(root, ex1.getCause());
        Assert.assertEquals(1, ex1.getPath().size());
        Assert.assertEquals("myBean", ex1.getPath().get(0).getFrom());
        Assert.assertEquals("prop", ex1.getPath().get(0).getFieldName());

        JsonMappingException ex2 = JsonMappingException.wrapWithPath(ex1, "myList", 3);
        Assert.assertSame(ex1, ex2);
        Assert.assertEquals(2, ex2.getPath().size());
        Assert.assertEquals("myList", ex2.getPath().get(0).getFrom());
        Assert.assertEquals(3, ex2.getPath().get(0).getIndex());

        Throwable noMsg = new NullPointerException();
        JsonMappingException ex3 = JsonMappingException.wrapWithPath(noMsg, new JsonMappingException.Reference("obj", "f"));
        Assert.assertTrue(ex3.getMessage().contains("(was java.lang.NullPointerException)"));

        Throwable emptyMsg = new RuntimeException("");
        JsonMappingException ex4 = JsonMappingException.wrapWithPath(emptyMsg, new JsonMappingException.Reference("obj", "f"));
        Assert.assertTrue(ex4.getMessage().contains("(was java.lang.RuntimeException)"));
    }

    @Test
    public void testWrapWithPathWithJsonProcessingExceptionProcessor() {
        JsonProcessingException jpe = new JsonProcessingException("proc-err") {
            private static final long serialVersionUID = 1L;
            @Override
            public Object getProcessor() {
                return new Closeable() {
                    @Override
                    public void close() {}
                };
            }
        };

        JsonMappingException jme = JsonMappingException.wrapWithPath(jpe, "from", "field");
        Assert.assertNotNull(jme.getProcessor());
        Assert.assertSame(jpe, jme.getCause());
    }

    @Test
    public void testPathAndMessageBuilding() {
        JsonMappingException ex = new JsonMappingException("Base issue");
        Assert.assertTrue(ex.getPath().isEmpty());
        Assert.assertEquals("", ex.getPathReference());
        Assert.assertEquals("Base issue", ex.getMessage());
        Assert.assertEquals("Base issue", ex.getLocalizedMessage());

        ex.prependPath("OuterClass", "items");
        ex.prependPath(new JsonMappingException.Reference(String.class, 0));

        List<JsonMappingException.Reference> path = ex.getPath();
        Assert.assertEquals(2, path.size());
        Assert.assertEquals("java.lang.String[0]->java.lang.String[\"items\"]", ex.getPathReference());
        Assert.assertEquals("Base issue (through reference chain: java.lang.String[0]->java.lang.String[\"items\"])", ex.getMessage());
        Assert.assertEquals(ex.getMessage(), ex.getLocalizedMessage());
        Assert.assertTrue(ex.toString().startsWith("com.fasterxml.jackson.databind.JsonMappingException: Base issue"));

        StringBuilder sb = new StringBuilder("prefix:");
        Assert.assertSame(sb, ex.getPathReference(sb));
        Assert.assertEquals("prefix:java.lang.String[0]->java.lang.String[\"items\"]", sb.toString());
    }

    @Test
    public void testBuildMessageWithNullSuperMessage() {
        JsonMappingException ex = new JsonMappingException((Closeable) null, null);
        ex.prependPath("Source", "f");
        Assert.assertEquals(" (through reference chain: java.lang.String[\"f\"])", ex.getMessage());
    }

    @Test
    public void testMaxRefsToListLimit() {
        JsonMappingException ex = new JsonMappingException("limit test");
        for (int i = 0; i < JsonMappingException.MAX_REFS_TO_LIST + 50; i++) {
            ex.prependPath("Obj", i);
        }
        Assert.assertEquals(JsonMappingException.MAX_REFS_TO_LIST, ex.getPath().size());
        Assert.assertEquals(JsonMappingException.MAX_REFS_TO_LIST - 1, ex.getPath().get(0).getIndex());
    }
}
