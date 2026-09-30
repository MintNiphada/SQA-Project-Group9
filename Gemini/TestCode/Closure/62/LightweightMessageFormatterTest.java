package com.google.javascript.jscomp;

import static com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt.LINE;
import static com.google.javascript.jscomp.SourceExcerptProvider.SourceExcerpt.REGION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class LightweightMessageFormatterTest {

  private static final DiagnosticType DUMMY_ERROR =
      DiagnosticType.error("JSC_DUMMY_ERROR", "error description: {0}");
  private static final DiagnosticType DUMMY_WARNING =
      DiagnosticType.warning("JSC_DUMMY_WARN", "warning description: {0}");

  @Test
  public void testWithoutSourceError() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("test.js", 10, 2, DUMMY_ERROR, "some message");
    String formatted = formatter.formatError(error);
    assertEquals("test.js:10: ERROR - error description: some message\n", formatted);
  }

  @Test
  public void testWithoutSourceWarning() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError warning = JSError.make("test.js", 5, 0, DUMMY_WARNING, "warn msg");
    String formatted = formatter.formatWarning(warning);
    assertEquals("test.js:5: WARNING - warning description: warn msg\n", formatted);
  }

  @Test
  public void testFormatWithNullSourceName() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make(DUMMY_ERROR, "msg without file");
    String formatted = formatter.formatError(error);
    assertEquals("ERROR - error description: msg without file\n", formatted);
  }

  @Test
  public void testFormatWithNegativeLineNumber() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("file.js", -1, -1, DUMMY_ERROR, "msg");
    String formatted = formatter.formatError(error);
    assertEquals("file.js: ERROR - error description: msg\n", formatted);
  }

  @Test
  public void testFormatWithZeroLineNumber() {
    LightweightMessageFormatter formatter = LightweightMessageFormatter.withoutSource();
    JSError error = JSError.make("file.js", 0, 0, DUMMY_ERROR, "msg");
    String formatted = formatter.formatError(error);
    assertEquals("file.js: ERROR - error description: msg\n", formatted);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullSource() {
    new LightweightMessageFormatter(null);
  }

  @Test(expected = NullPointerException.class)
  public void testConstructorNullSourceWithExcerpt() {
    new LightweightMessageFormatter(null, LINE);
  }

  @Test
  public void testFormatWithSourceLine() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      @Override
      public String getSourceLine(String sourceName, int lineNumber) {
        if ("foo.js".equals(sourceName) && lineNumber == 1) {
          return "var a = 1 + ;";
        }
        return null;
      }

      @Override
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return null;
      }
    };

    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("foo.js", 1, 12, DUMMY_ERROR, "unexpected token");
    String formatted = formatter.formatError(error);
    String expected = "foo.js:1: ERROR - error description: unexpected token\n"
        + "var a = 1 + ;\n"
        + "            ^\n";
    assertEquals(expected, formatted);
  }

  @Test
  public void testFormatWithSourceLineAndTabs() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      @Override
      public String getSourceLine(String sourceName, int lineNumber) {
        return "\t\tvar a = 1;";
      }

      @Override
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return null;
      }
    };

    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider, LINE);
    JSError error = JSError.make("tab.js", 1, 2, DUMMY_ERROR, "tab error");
    String formatted = formatter.formatError(error);
    String expected = "tab.js:1: ERROR - error description: tab error\n"
        + "\t\tvar a = 1;\n"
        + "\t\t^\n";
    assertEquals(expected, formatted);
  }

  @Test
  public void testFormatWithCharnoOutOfBounds() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      @Override
      public String getSourceLine(String sourceName, int lineNumber) {
        return "var a = 1;";
      }

      @Override
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return null;
      }
    };

    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    
    // charno is equal to length (out of bounds)
    JSError errorEqual = JSError.make("foo.js", 1, 10, DUMMY_ERROR, "eol error");
    String formattedEqual = formatter.formatError(errorEqual);
    String expectedEqual = "foo.js:1: ERROR - error description: eol error\n"
        + "var a = 1;\n";
    assertEquals(expectedEqual, formattedEqual);

    // charno is negative
    JSError errorNegative = JSError.make("foo.js", 1, -1, DUMMY_ERROR, "neg charno");
    String formattedNeg = formatter.formatError(errorNegative);
    String expectedNeg = "foo.js:1: ERROR - error description: neg charno\n"
        + "var a = 1;\n";
    assertEquals(expectedNeg, formattedNeg);
  }

  @Test
  public void testFormatWithNullSourceExcerpt() {
    SourceExcerptProvider provider = new SourceExcerptProvider() {
      @Override
      public String getSourceLine(String sourceName, int lineNumber) {
        return null;
      }

      @Override
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return null;
      }
    };

    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider);
    JSError error = JSError.make("foo.js", 1, 5, DUMMY_ERROR, "missing line");
    String formatted = formatter.formatError(error);
    assertEquals("foo.js:1: ERROR - error description: missing line\n", formatted);
  }

  @Test
  public void testFormatRegionExcerpt() {
    final Region region = new Region() {
      @Override
      public String getSourceExcerpt() {
        return "function foo() {\n  return 1;\n}";
      }

      @Override
      public int getBeginningLineNumber() {
        return 9;
      }

      @Override
      public int getEndingLineNumber() {
        return 11;
      }
    };

    SourceExcerptProvider provider = new SourceExcerptProvider() {
      @Override
      public String getSourceLine(String sourceName, int lineNumber) {
        return null;
      }

      @Override
      public Region getSourceRegion(String sourceName, int lineNumber) {
        return region;
      }
    };

    LightweightMessageFormatter formatter = new LightweightMessageFormatter(provider, REGION);
    JSError error = JSError.make("foo.js", 10, 2, DUMMY_ERROR, "region error");
    String formatted = formatter.formatError(error);

    String expected = "foo.js:10: ERROR - error description: region error\n"
        + "   9| function foo() {\n"
        + "  10|   return 1;\n"
        + "  11| }\n";
    assertEquals(expected, formatted);
  }

  @Test
  public void testLineNumberingFormatterFormatLine() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    assertEquals("var x = 1;", formatter.formatLine("var x = 1;", 1));
    assertEquals("", formatter.formatLine("", 10));
    assertNull(formatter.formatLine(null, 5));
  }

  @Test
  public void testLineNumberingFormatterFormatRegionNull() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    assertNull(formatter.formatRegion(null));
  }

  @Test
  public void testLineNumberingFormatterFormatRegionEmptyCode() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    Region region = new Region() {
      @Override
      public String getSourceExcerpt() {
        return "";
      }

      @Override
      public int getBeginningLineNumber() {
        return 1;
      }

      @Override
      public int getEndingLineNumber() {
        return 1;
      }
    };
    assertNull(formatter.formatRegion(region));
  }

  @Test
  public void testLineNumberingFormatterSingleLine() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    Region region = new Region() {
      @Override
      public String getSourceExcerpt() {
        return "single line code";
      }

      @Override
      public int getBeginningLineNumber() {
        return 1;
      }

      @Override
      public int getEndingLineNumber() {
        return 1;
      }
    };
    String result = formatter.formatRegion(region);
    assertEquals("  1| single line code", result);
  }

  @Test
  public void testLineNumberingFormatterTrailingNewline() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    Region region = new Region() {
      @Override
      public String getSourceExcerpt() {
        return "line 1\nline 2\n";
      }

      @Override
      public int getBeginningLineNumber() {
        return 1;
      }

      @Override
      public int getEndingLineNumber() {
        return 2;
      }
    };
    String result = formatter.formatRegion(region);
    assertEquals("  1| line 1\n  2| line 2", result);
  }

  @Test
  public void testLineNumberingFormatterMultipleLinesPadding() {
    LightweightMessageFormatter.LineNumberingFormatter formatter =
        new LightweightMessageFormatter.LineNumberingFormatter();
    Region region = new Region() {
      @Override
      public String getSourceExcerpt() {
        return "a\nb\nc";
      }

      @Override
      public int getBeginningLineNumber() {
        return 8;
      }

      @Override
      public int getEndingLineNumber() {
        return 10;
      }
    };
    String result = formatter.formatRegion(region);
    String expected = "   8| a\n   9| b\n  10| c";
    assertEquals(expected, result);
  }
}