package com.fasterxml.jackson.databind;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;

import org.junit.Assert;
import org.junit.Test;

public class JsonMappingExceptionTest {

    private final JsonFactory JSON_F = new JsonFactory();

    @Test
    public void testDeprecatedConstructors() {
        JsonMappingException e1 = new JsonMappingException("msg1");
        Assert.assertEquals("msg1", e1.getMessage());
        Assert.assertNull(e1.getCause());

        Throwable cause = new RuntimeException("cause");
        JsonMappingException e2 = new JsonMappingException("msg2", cause);
        Assert.assertEquals("msg2", e2.getMessage());
        Assert.assertSame(cause, e2.getCause());

        JsonLocation loc = new JsonLocation("src", 10L, 1, 2);
        JsonMappingException e3 = new JsonMappingException("msg3", loc);
        Assert.assertTrue(e3.getMessage().contains("msg3"));
        Assert.assertEquals(loc, e3.getLocation());

        JsonMappingException e4 = new JsonMappingException("msg4", loc, cause);
        Assert.assertTrue(e4.getMessage().contains("msg4"));
        Assert.assertEquals(loc, e4.getLocation());
        Assert.assertSame(cause, e4.getCause());
    }

    @Test
    public void testConstructorsWithProcessor() throws Exception {
        JsonParser p = JSON_F.createParser(new StringReader("123"));
        JsonMappingException ep1 = new JsonMappingException(p, "parse error");
        Assert.assertSame(p, ep1.getProcessor());
        Assert.assertNotNull(ep1.getLocation());

        Throwable t = new IOException("io");
        JsonMappingException ep2 = new JsonMappingException(p, "parse error 2", t);
        Assert.assertSame(p, ep2.getProcessor());
        Assert.assertSame(t, ep2.getCause());
        Assert.assertNotNull(ep2.getLocation());

        JsonLocation loc = new JsonLocation("src", 5L, 2, 3);
        JsonMappingException ep3 = new JsonMappingException(p, "parse error 3", loc);
        Assert.assertSame(p, ep3.getProcessor());
        Assert.assertEquals(loc, ep3.getLocation());

        Closeable nonParser = new StringWriter();
        JsonMappingException ep4 = new JsonMappingException(nonParser, "msg");
        Assert.assertSame(nonParser, ep4.getProcessor());
        Assert.assertNull(ep4.getLocation());

        JsonMappingException ep5 = new JsonMappingException(nonParser, "msg", t);
        Assert.assertSame(nonParser, ep5.getProcessor());
        Assert.assertSame(t, ep5.getCause());

        p.close();
    }

    @Test
    public void testFactoryMethodsFromParser() throws Exception {
        JsonParser p = JSON_F.createParser(new StringReader("{}"));
        JsonMappingException e1 = JsonMappingException.from(p, "m1");
        Assert.assertSame(p, e1.getProcessor());
        Assert.assertEquals("m1", e1.getOriginalMessage());

        Throwable cause = new IllegalStateException("bad");
        JsonMappingException e2 = JsonMappingException.from(p, "m2", cause);
        Assert.assertSame(p, e2.getProcessor());
        Assert.assertSame(cause, e2.getCause());
        Assert.assertEquals("m2", e2.getOriginalMessage());
        p.close();
    }

    @Test
    public void testFactoryMethodsFromGenerator() throws Exception {
        StringWriter sw = new StringWriter();
        JsonGenerator g = JSON_F.createGenerator(sw);
        JsonMappingException e1 = JsonMappingException.from(g, "g1");
        Assert.assertSame(g, e1.getProcessor());
        Assert.assertEquals("g1", e1.getOriginalMessage());

        Throwable cause = new IllegalStateException("bad gen");
        JsonMappingException e2 = JsonMappingException.from(g, "g2", cause);
        Assert.assertSame(g, e2.getProcessor());
        Assert.assertSame(cause, e2.getCause());
        Assert.assertEquals("g2", e2.getOriginalMessage());
        g.close();
    }

    @Test
    public void testFactoryFromUnexpectedIOE() {
        IOException src = new IOException("disk error");
        JsonMappingException jme = JsonMappingException.fromUnexpectedIOE(src);
        Assert.assertNull(jme.getProcessor());
        Assert.assertTrue(jme.getMessage().contains("Unexpected IOException (of type java.io.IOException): disk error"));
    }

    @Test
    public void testFactoryFromContexts() {
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext dctx = mapper.getDeserializationContext();
        SerializerProvider sctx = mapper.getSerializerProviderInstance();

        JsonMappingException e1 = JsonMappingException.from(dctx, "dctx error");
        Assert.assertNotNull(e1);
        Assert.assertEquals("dctx error", e1.getOriginalMessage());

        Throwable t = new RuntimeException("dctx cause");
        JsonMappingException e2 = JsonMappingException.from(dctx, "dctx error 2", t);
        Assert.assertSame(t, e2.getCause());

        JsonMappingException e3 = JsonMappingException.from(sctx, "sctx error");
        Assert.assertNotNull(e3);
        Assert.assertEquals("sctx error", e3.getOriginalMessage());

        JsonMappingException e4 = JsonMappingException.from(sctx, "sctx error 2", t);
        Assert.assertSame(t, e4.getCause());
    }

    @Test
    public void testReferenceBasics() {
        Reference refDef = new Reference();
        Assert.assertNull(refDef.getFrom());
        Assert.assertNull(refDef.getFieldName());
        Assert.assertEquals(-1, refDef.getIndex());
        Assert.assertEquals("UNKNOWN[?]", refDef.getDescription());

        refDef.setFieldName("customField");
        refDef.setIndex(5);
        refDef.setDescription("customDesc");
        Assert.assertEquals("customField", refDef.getFieldName());
        Assert.assertEquals(5, refDef.getIndex());
        Assert.assertEquals("customDesc", refDef.getDescription());
        Assert.assertEquals("customDesc", refDef.toString());

        Reference refFrom = new Reference("sourceObj");
        Assert.assertEquals("sourceObj", refFrom.getFrom());
        Assert.assertEquals("java.lang.String[?]", refFrom.getDescription());

        Reference refField = new Reference(String.class, "length");
        Assert.assertEquals(String.class, refField.getFrom());
        Assert.assertEquals("length", refField.getFieldName());
        Assert.assertEquals("java.lang.String[\"length\"]", refField.getDescription());

        Reference refIdx = new Reference(new int[0], 3);
        Assert.assertEquals(3, refIdx.getIndex());
        Assert.assertEquals("int[][3]", refIdx.getDescription());
    }

    @Test(expected = NullPointerException.class)
    public void testReferenceNullFieldName() {
        new Reference("source", null);
    }

    @Test
    public void testReferenceDescriptionBranches() {
        class LocalInner {}
        Reference rInner = new Reference(LocalInner.class, "innerField");
        Assert.assertTrue(rInner.getDescription().endsWith("LocalInner[\"innerField\"]"));

        Reference rNoPkg = new Reference(new Object() {}.getClass(), "anonField");
        Assert.assertNotNull(rNoPkg.getDescription());
    }

    @Test
    public void testReferenceSerialization() throws Exception {
        Reference ref = new Reference("mySource", "myField");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ref);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Reference deserialized = (Reference) ois.readObject();
        ois.close();

        Assert.assertNull(deserialized.getFrom());
        Assert.assertEquals("myField", deserialized.getFieldName());
        Assert.assertEquals("java.lang.String[\"myField\"]", deserialized.getDescription());
    }

    @Test
    public void testWrapWithPath() throws Exception {
        JsonParser p = JSON_F.createParser("123");
        JsonParseException jpe = new JsonParseException(p, "parse fail");
        JsonMappingException wrapped1 = JsonMappingException.wrapWithPath(jpe, "fromObj", "prop");
        Assert.assertSame(p, wrapped1.getProcessor());
        Assert.assertSame(jpe, wrapped1.getCause());
        Assert.assertEquals(1, wrapped1.getPath().size());

        JsonMappingException wrapped2 = JsonMappingException.wrapWithPath(wrapped1, "fromObj2", 42);
        Assert.assertSame(wrapped1, wrapped2);
        Assert.assertEquals(2, wrapped2.getPath().size());

        Throwable emptyMsgEx = new Exception("");
        JsonMappingException wrapped3 = JsonMappingException.wrapWithPath(emptyMsgEx, new Reference("obj"));
        Assert.assertTrue(wrapped3.getMessage().contains("(was java.lang.Exception)"));

        Throwable nullMsgEx = new Exception((String) null);
        JsonMappingException wrapped4 = JsonMappingException.wrapWithPath(nullMsgEx, "src", 0);
        Assert.assertTrue(wrapped4.getMessage().contains("(was java.lang.Exception)"));

        p.close();
    }

    @Test
    public void testPathAndMessageBuilding() {
        JsonMappingException ex = new JsonMappingException("Base message");
        Assert.assertTrue(ex.getPath().isEmpty());
        Assert.assertEquals("", ex.getPathReference());
        Assert.assertEquals("Base message", ex.getMessage());
        Assert.assertEquals("Base message", ex.getLocalizedMessage());
        Assert.assertTrue(ex.toString().contains("Base message"));

        ex.prependPath("root", "a");
        ex.prependPath("item", 2);
        Assert.assertEquals(2, ex.getPath().size());
        Assert.assertEquals("java.lang.String[2]->java.lang.String[\"a\"]", ex.getPathReference());
        Assert.assertEquals("Base message (through reference chain: java.lang.String[2]->java.lang.String[\"a\"])", ex.getMessage());
        Assert.assertEquals(ex.getMessage(), ex.getLocalizedMessage());

        StringBuilder sb = new StringBuilder("prefix: ");
        ex.getPathReference(sb);
        Assert.assertEquals("prefix: java.lang.String[2]->java.lang.String[\"a\"]", sb.toString());
    }

    @Test
    public void testNullMessageWithPath() {
        JsonMappingException ex = new JsonMappingException((String) null);
        ex.prependPath("item", 1);
        Assert.assertEquals(" (through reference chain: java.lang.String[1])", ex.getMessage());
    }

    @Test
    public void testPrependPathMaxLimit() {
        JsonMappingException ex = new JsonMappingException("limit test");
        for (int i = 0; i < JsonMappingException.MAX_REFS_TO_LIST + 50; i++) {
            ex.prependPath("item", i);
        }
        List<Reference> path = ex.getPath();
        Assert.assertEquals(JsonMappingException.MAX_REFS_TO_LIST, path.size());
    }
}
