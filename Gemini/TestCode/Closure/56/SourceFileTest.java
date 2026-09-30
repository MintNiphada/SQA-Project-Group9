package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

public class SourceFileTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorNullFileName() {
    new SourceFile(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorEmptyFileName() {
    new SourceFile("");
  }

  @Test
  public void testBasicProperties() {
    SourceFile sf = SourceFile.fromCode("foo.js", "var x = 1;");
    Assert.assertEquals("foo.js", sf.getName());
    Assert.assertEquals("foo.js", sf.toString());
    Assert.assertEquals("foo.js", sf.getOriginalPath());

    sf.setOriginalPath("original/foo.js");
    Assert.assertEquals("original/foo.js", sf.getOriginalPath());

    Assert.assertFalse(sf.isExtern());
    sf.setIsExtern(true);
    Assert.assertTrue(sf.isExtern());
    sf.setIsExtern(false);
    Assert.assertFalse(sf.isExtern());
  }

  @Test
  public void testFromCodeWithOriginalPath() throws IOException {
    SourceFile sf = SourceFile.fromCode("bar.js", "orig/bar.js", "var a = 2;");
    Assert.assertEquals("bar.js", sf.getName());
    Assert.assertEquals("orig/bar.js", sf.getOriginalPath());
    Assert.assertEquals("var a = 2;", sf.getCode());
    Assert.assertTrue(sf.hasSourceInMemory());
    Assert.assertEquals("var a = 2;", sf.getCodeNoCache());
  }

  @Test
  public void testGetCodeReader() throws IOException {
    SourceFile sf = SourceFile.fromCode("test.js", "var z = 3;\nvar y = 4;");
    Reader reader = sf.getCodeReader();
    Assert.assertNotNull(reader);
    String content = CharStreams.toString(reader);
    Assert.assertEquals("var z = 3;\nvar y = 4;", content);
  }

  @Test
  public void testLineOffsetsAndNumLines() {
    String code = "line1\nline22\n\nline444";
    SourceFile sf = SourceFile.fromCode("test.js", code);

    Assert.assertEquals(4, sf.getNumLines());
    Assert.assertEquals(0, sf.getLineOffset(1));
    Assert.assertEquals(6, sf.getLineOffset(2));
    Assert.assertEquals(13, sf.getLineOffset(3));
    Assert.assertEquals(14, sf.getLineOffset(4));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetLineOffsetTooSmall() {
    SourceFile sf = SourceFile.fromCode("test.js", "a\nb\nc");
    sf.getLineOffset(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetLineOffsetTooLarge() {
    SourceFile sf = SourceFile.fromCode("test.js", "a\nb\nc");
    sf.getLineOffset(4);
  }

  @Test
  public void testGetLineSequentialAndNonSequential() {
    String code = "line1\nline2\nline3\nline4\n";
    SourceFile sf = SourceFile.fromCode("test.js", code);

    Assert.assertEquals("line1", sf.getLine(1));
    Assert.assertEquals("line2", sf.getLine(2));
    Assert.assertEquals("line3", sf.getLine(3));
    Assert.assertEquals("line4", sf.getLine(4));
    Assert.assertNull(sf.getLine(5));
    Assert.assertNull(sf.getLine(10));

    // Non-sequential line lookup (backwards)
    Assert.assertEquals("line2", sf.getLine(2));
    Assert.assertEquals("line1", sf.getLine(1));
    Assert.assertEquals("line4", sf.getLine(4));
  }

  @Test
  public void testGetLineNoTrailingNewline() {
    String code = "line1\nline2";
    SourceFile sf = SourceFile.fromCode("test.js", code);
    Assert.assertEquals("line1", sf.getLine(1));
  }

  @Test
  public void testGetRegionSmallFile() {
    String code = "line1\nline2\nline3\n";
    SourceFile sf = SourceFile.fromCode("test.js", code);

    Region r = sf.getRegion(1);
    Assert.assertNotNull(r);
    Assert.assertEquals(1, r.getBeginningLineNumber());
    Assert.assertEquals(4, r.getEndingLineNumber());
    Assert.assertEquals("line1\nline2\nline3", r.getSourceExcerpt());
  }

  @Test
  public void testGetRegionNoTrailingNewline() {
    String code = "line1\nline2\nline3";
    SourceFile sf = SourceFile.fromCode("test.js", code);

    Region r = sf.getRegion(2);
    Assert.assertNotNull(r);
    Assert.assertEquals(1, r.getBeginningLineNumber());
    Assert.assertEquals(4, r.getEndingLineNumber());
    Assert.assertEquals("line1\nline2\nline3", r.getSourceExcerpt());
  }

  @Test
  public void testGetRegionLargeFile() {
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= 20; i++) {
      sb.append("line").append(i).append("\n");
    }
    SourceFile sf = SourceFile.fromCode("test.js", sb.toString());

    Region r = sf.getRegion(10);
    Assert.assertNotNull(r);
    Assert.assertEquals(8, r.getBeginningLineNumber());
    Assert.assertEquals(13, r.getEndingLineNumber());
    Assert.assertEquals("line8\nline9\nline10\nline11\nline12", r.getSourceExcerpt());

    Assert.assertNull(sf.getRegion(30));
  }

  @Test
  public void testFromInputStream() throws IOException {
    String code = "var x = 'stream';";
    InputStream is1 = new ByteArrayInputStream(code.getBytes(Charsets.UTF_8));
    SourceFile sf1 = SourceFile.fromInputStream("stream1.js", is1);
    Assert.assertEquals("stream1.js", sf1.getName());
    Assert.assertEquals(code, sf1.getCode());

    InputStream is2 = new ByteArrayInputStream(code.getBytes(Charsets.UTF_8));
    SourceFile sf2 = SourceFile.fromInputStream("stream2.js", "orig/stream2.js", is2);
    Assert.assertEquals("stream2.js", sf2.getName());
    Assert.assertEquals("orig/stream2.js", sf2.getOriginalPath());
    Assert.assertEquals(code, sf2.getCode());
  }

  @Test
  public void testFromReader() throws IOException {
    String code = "var r = 'reader';";
    Reader reader = new StringReader(code);
    SourceFile sf = SourceFile.fromReader("reader.js", reader);
    Assert.assertEquals("reader.js", sf.getName());
    Assert.assertEquals(code, sf.getCode());
  }

  @Test
  public void testFromGenerator() throws IOException {
    final int[] callCount = new int[]{0};
    SourceFile.Generator generator = new SourceFile.Generator() {
      @Override
      public String getCode() {
        callCount[0]++;
        return "var gen = " + callCount[0] + ";";
      }
    };

    SourceFile sf = SourceFile.fromGenerator("gen.js", generator);
    Assert.assertEquals("gen.js", sf.getName());
    Assert.assertFalse(sf.hasSourceInMemory());

    String code1 = sf.getCode();
    Assert.assertEquals("var gen = 1;", code1);
    Assert.assertTrue(sf.hasSourceInMemory());

    // Second call should return cached code without calling generator
    String code2 = sf.getCode();
    Assert.assertEquals("var gen = 1;", code2);
    Assert.assertEquals(1, callCount[0]);

    // Clear cache and verify code regenerates
    sf.clearCachedSource();
    Assert.assertFalse(sf.hasSourceInMemory());
    String code3 = sf.getCode();
    Assert.assertEquals("var gen = 2;", code3);
    Assert.assertEquals(2, callCount[0]);
  }

  @Test
  public void testFromFileAndOnDisk() throws IOException {
    File tempFile = File.createTempFile("source_file_test", ".js");
    tempFile.deleteOnExit();

    Writer writer = null;
    try {
      writer = new OutputStreamWriter(new FileOutputStream(tempFile), Charsets.UTF_8);
      writer.write("var disk = true;\nvar disk2 = false;");
    } finally {
      if (writer != null) {
        writer.close();
      }
    }

    SourceFile sf = SourceFile.fromFile(tempFile.getAbsolutePath());
    Assert.assertEquals(tempFile.getPath(), sf.getName());
    Assert.assertFalse(sf.hasSourceInMemory());

    // Reader before loaded into memory uses FileReader
    Reader r1 = sf.getCodeReader();
    Assert.assertNotNull(r1);
    String fromR1 = CharStreams.toString(r1);
    r1.close();
    Assert.assertEquals("var disk = true;\nvar disk2 = false;", fromR1);
    Assert.assertFalse(sf.hasSourceInMemory());

    // Get code loads into memory
    String code = sf.getCode();
    Assert.assertEquals("var disk = true;\nvar disk2 = false;", code);
    Assert.assertTrue(sf.hasSourceInMemory());

    // Reader after loaded into memory uses StringReader
    Reader r2 = sf.getCodeReader();
    Assert.assertNotNull(r2);
    String fromR2 = CharStreams.toString(r2);
    r2.close();
    Assert.assertEquals("var disk = true;\nvar disk2 = false;", fromR2);

    // Test clearCachedSource on OnDisk
    sf.clearCachedSource();
    Assert.assertFalse(sf.hasSourceInMemory());

    // Test custom charset methods
    SourceFile sfCharset = SourceFile.fromFile(tempFile, Charsets.US_ASCII);
    Assert.assertTrue(sfCharset instanceof SourceFile.OnDisk);
    SourceFile.OnDisk onDisk = (SourceFile.OnDisk) sfCharset;
    Assert.assertEquals(Charsets.US_ASCII, onDisk.getCharset());
    onDisk.setCharset(Charsets.UTF_8);
    Assert.assertEquals(Charsets.UTF_8, onDisk.getCharset());

    // Test fromFile(String, Charset) and fromFile(File)
    SourceFile sfFile = SourceFile.fromFile(tempFile);
    Assert.assertEquals(tempFile.getPath(), sfFile.getName());

    SourceFile sfStringCharset = SourceFile.fromFile(tempFile.getAbsolutePath(), Charsets.UTF_8);
    Assert.assertEquals(tempFile.getPath(), sfStringCharset.getName());
  }

  @Test
  public void testClearCachedSourceDefaultDoesNothing() {
    SourceFile sf = SourceFile.fromCode("preloaded.js", "var pre = 1;");
    Assert.assertTrue(sf.hasSourceInMemory());
    sf.clearCachedSource();
    Assert.assertTrue(sf.hasSourceInMemory());
  }

  @Test
  public void testIOExceptionHandlingInSubclass() {
    SourceFile throwingSf = new SourceFile("error.js") {
      private static final long serialVersionUID = 1L;

      @Override
      public String getCode() throws IOException {
        throw new IOException("Simulated IO error");
      }
    };

    Assert.assertEquals(1, throwingSf.getNumLines());
    Assert.assertEquals(0, throwingSf.getLineOffset(1));
    Assert.assertNull(throwingSf.getLine(1));
    Assert.assertNull(throwingSf.getRegion(1));
  }
}