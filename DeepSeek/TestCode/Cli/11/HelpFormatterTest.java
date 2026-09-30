package org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.*;
import java.io.*;
import java.util.*;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private ByteArrayOutputStream outContent;
    private PrintWriter printWriter;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        outContent = new ByteArrayOutputStream();
        printWriter = new PrintWriter(outContent);
    }

    private String getOutput() {
        printWriter.flush();
        return outContent.toString();
    }

    @Test
    public void testDefaultValues() {
        assertEquals(74, formatter.defaultWidth);
        assertEquals(1, formatter.defaultLeftPad);
        assertEquals(3, formatter.defaultDescPad);
        assertEquals("usage: ", formatter.defaultSyntaxPrefix);
        assertEquals(System.getProperty("line.separator"), formatter.defaultNewLine);
        assertEquals("-", formatter.defaultOptPrefix);
        assertEquals("--", formatter.defaultLongOptPrefix);
        assertEquals("arg", formatter.defaultArgName);
        assertNotNull(formatter.getOptionComparator());
    }

    @Test
    public void testSetGetWidth() {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());
    }

    @Test
    public void testSetGetLeftPadding() {
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
    }

    @Test
    public void testSetGetDescPadding() {
        formatter.setDescPadding(10);
        assertEquals(10, formatter.getDescPadding());
    }

    @Test
    public void testSetGetSyntaxPrefix() {
        formatter.setSyntaxPrefix("cmd> ");
        assertEquals("cmd> ", formatter.getSyntaxPrefix());
    }

    @Test
    public void testSetGetNewLine() {
        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());
    }

    @Test
    public void testSetGetOptPrefix() {
        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());
    }

    @Test
    public void testSetGetLongOptPrefix() {
        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());
    }

    @Test
    public void testSetGetArgName() {
        formatter.setArgName("FILE");
        assertEquals("FILE", formatter.getArgName());
    }

    @Test
    public void testSetOptionComparatorNull() {
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        // OptionComparator is private, but we can verify it's the default by checking class name
        assertTrue(formatter.getOptionComparator().getClass().getName().contains("OptionComparator"));
    }

    @Test
    public void testSetOptionComparatorCustom() {
        Comparator<Option> custom = new Comparator<Option>() {
            public int compare(Option o1, Option o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        };
        formatter.setOptionComparator(custom);
        assertSame(custom, formatter.getOptionComparator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        formatter.printHelp(printWriter, 80, null, "header", new Options(), 1, 3, "footer", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        formatter.printHelp(printWriter, 80, "", "header", new Options(), 1, 3, "footer", false);
    }

    @Test
    public void testPrintHelpAutoUsage() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        formatter.printHelp(printWriter, 80, "myapp", "header", options, 1, 3, "footer", true);
        String output = getOutput();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("footer"));
    }

    @Test
    public void testPrintHelpNoAutoUsage() {
        Options options = new Options();
        options.addOption("b", "beta", true, "Beta option");
        formatter.printHelp(printWriter, 80, "myapp", "header", options, 1, 3, "footer", false);
        String output = getOutput();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-b"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("footer"));
    }

    @Test
    public void testPrintHelpNullHeaderFooter() {
        Options options = new Options();
        options.addOption("c", false, "Gamma");
        formatter.printHelp(printWriter, 80, "myapp", null, options, 1, 3, null, false);
        String output = getOutput();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-c"));
        assertFalse(output.contains("null"));
    }

    @Test
    public void testPrintHelpEmptyHeaderFooter() {
        Options options = new Options();
        options.addOption("d", false, "Delta");
        formatter.printHelp(printWriter, 80, "myapp", "", options, 1, 3, "   ", false);
        String output = getOutput();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-d"));
    }

    @Test
    public void testPrintHelpOverloads() {
        Options options = new Options();
        options.addOption("e", false, "Epsilon");
        // These just call the main method with System.out, we can't assert output but ensure no exception.
        formatter.printHelp("myapp", options);
        formatter.printHelp("myapp", options, true);
        formatter.printHelp("myapp", "header", options, "footer");
        formatter.printHelp("myapp", "header", options, "footer", true);
        formatter.printHelp(80, "myapp", "header", options, "footer");
        formatter.printHelp(80, "myapp", "header", options, "footer", true);
    }

    @Test
    public void testPrintUsageWithOptions() {
        Options options = new Options();
        options.addOption("f", "foo", false, "Foo");
        options.addOption("g", "goo", true, "Goo");
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", "xray", false, "Xray"));
        group.addOption(new Option("y", "yankee", false, "Yankee"));
        options.addOptionGroup(group);
        formatter.printUsage(printWriter, 80, "myapp", options);
        String output = getOutput();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-f"));
        assertTrue(output.contains("--foo"));
        assertTrue(output.contains("-g <arg>"));
        assertTrue(output.contains("[-x | -y]"));
    }

    @Test
    public void testPrintUsageNoOptions() {
        formatter.printUsage(printWriter, 80, "myapp");
        String output = getOutput();
        assertTrue(output.contains("usage: myapp"));
    }

    @Test
    public void testPrintOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Help");
        options.addOption("v", "version", false, "Version");
        formatter.printOptions(printWriter, 80, options, 2, 4);
        String output = getOutput();
        assertTrue(output.contains("-h"));
        assertTrue(output.contains("--help"));
        assertTrue(output.contains("-v"));
        assertTrue(output.contains("--version"));
        assertTrue(output.contains("Help"));
        assertTrue(output.contains("Version"));
    }

    @Test
    public void testPrintWrapped() {
        formatter.printWrapped(printWriter, 20, 0, "This is a long text that should wrap.");
        String output = getOutput();
        assertTrue(output.contains(System.getProperty("line.separator")));
    }

    @Test
    public void testRenderOptions() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Options options = new Options();
        options.addOption("i", "input", true, "Input file");
        options.addOption("o", "output", true, "Output file");
        StringBuffer sb = new StringBuffer();
        thf.renderOptions(sb, 80, options, 2, 4);
        String result = sb.toString();
        assertTrue(result.contains("-i"));
        assertTrue(result.contains("--input"));
        assertTrue(result.contains("<arg>"));
        assertTrue(result.contains("Input file"));
    }

    @Test
    public void testRenderWrappedText() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 10, 0, "Hello world this is a test");
        String result = sb.toString();
        assertTrue(result.contains(System.getProperty("line.separator")));
    }

    @Test
    public void testFindWrapPos() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertEquals(5, thf.findWrapPos("1234\n5678", 10, 0));
        assertEquals(5, thf.findWrapPos("1234\t5678", 10, 0));
        assertEquals(-1, thf.findWrapPos("short", 10, 0));
        assertEquals(5, thf.findWrapPos("12345 7890", 6, 0));
        assertEquals(10, thf.findWrapPos("1234567890 123", 5, 0));
        assertEquals(-1, thf.findWrapPos("12345678901234567890", 5, 0));
        assertEquals(7, thf.findWrapPos("1234567 90", 5, 3));
    }

    @Test
    public void testCreatePadding() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertEquals("   ", thf.createPadding(3));
        assertEquals("", thf.createPadding(0));
    }

    @Test
    public void testRtrim() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertEquals("abc", thf.rtrim("abc   "));
        assertEquals("abc", thf.rtrim("abc"));
        assertEquals("", thf.rtrim("   "));
        assertNull(thf.rtrim(null));
        assertEquals("", thf.rtrim(""));
    }

    @Test
    public void testOptionComparator() {
        Option opt1 = new Option("a", "alpha", false, "desc");
        Option opt2 = new Option("b", "beta", false, "desc");
        Option opt3 = new Option("A", "Alpha", false, "desc");
        Comparator comp = new HelpFormatter().getOptionComparator();
        assertTrue(comp.compare(opt1, opt2) < 0);
        assertTrue(comp.compare(opt2, opt1) > 0);
        assertEquals(0, comp.compare(opt1, opt3));
    }

    @Test
    public void testOptionGroupRequired() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", false, "A"));
        group.addOption(new Option("b", false, "B"));
        options.addOptionGroup(group);
        formatter.printUsage(printWriter, 80, "myapp", options);
        String output = getOutput();
        assertTrue(output.contains("-a | -b"));
        assertFalse(output.contains("["));
    }

    @Test
    public void testOptionGroupOptional() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(false);
        group.addOption(new Option("a", false, "A"));
        group.addOption(new Option("b", false, "B"));
        options.addOptionGroup(group);
        formatter.printUsage(printWriter, 80, "myapp", options);
        String output = getOutput();
        assertTrue(output.contains("[-a | -b]"));
    }

    @Test
    public void testAppendOption() {
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("test").withDescription("desc").create("t"));
        formatter.printUsage(printWriter, 80, "myapp", options);
        String output = getOutput();
        assertTrue(output.contains("-t"));
        assertTrue(output.contains("--test"));
    }

    @Test
    public void testOptionWithArg() {
        Options options = new Options();
        options.addOption(OptionBuilder.hasArg().withArgName("FILE").withDescription("desc").create("f"));
        formatter.printUsage(printWriter, 80, "myapp", options);
        String output = getOutput();
        assertTrue(output.contains("-f <FILE>"));
    }

    @Test
    public void testOptionWithArgNoArgName() {
        Options options = new Options();
        Option opt = new Option("g", "goo", true, "desc");
        options.addOption(opt);
        formatter.printUsage(printWriter, 80, "myapp", options);
        String output = getOutput();
        assertTrue(output.contains("-g "));
        assertFalse(output.contains("<>"));
    }

    @Test
    public void testRenderOptionsLongOptOnly() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("longonly").withDescription("desc").create());
        StringBuffer sb = new StringBuffer();
        thf.renderOptions(sb, 80, options, 2, 4);
        String result = sb.toString();
        assertTrue(result.contains("--longonly"));
        assertFalse(result.contains("-"));
    }

    @Test
    public void testRenderOptionsWithArgName() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Options options = new Options();
        options.addOption(OptionBuilder.hasArg().withArgName("FILE").withDescription("desc").create("f"));
        StringBuffer sb = new StringBuffer();
        thf.renderOptions(sb, 80, options, 2, 4);
        String result = sb.toString();
        assertTrue(result.contains("<FILE>"));
    }

    @Test
    public void testRenderOptionsWithArgNoArgName() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Options options = new Options();
        Option opt = new Option("g", "goo", true, "desc");
        options.addOption(opt);
        StringBuffer sb = new StringBuffer();
        thf.renderOptions(sb, 80, options, 2, 4);
        String result = sb.toString();
        assertTrue(result.contains("-g "));
    }

    @Test
    public void testRenderWrappedTextNoWrap() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 80, 0, "short");
        assertEquals("short", sb.toString());
    }

    @Test
    public void testRenderWrappedTextExactFit() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 5, 0, "12345");
        assertEquals("12345", sb.toString());
    }

    @Test
    public void testRenderWrappedTextMultipleWraps() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 10, 0, "This is a long text that wraps multiple times.");
        String result = sb.toString();
        String[] lines = result.split(System.getProperty("line.separator"));
        assertTrue(lines.length > 1);
    }

    @Test
    public void testFindWrapPosEdgeCases() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertEquals(5, thf.findWrapPos("1234\n5678", 4, 0));
        assertEquals(5, thf.findWrapPos("1234\t5678", 4, 0));
        assertEquals(5, thf.findWrapPos("12345 678", 5, 0));
        assertEquals(-1, thf.findWrapPos("1234567890", 20, 0));
        assertEquals(-1, thf.findWrapPos("1234567890", 5, 0));
        assertEquals(10, thf.findWrapPos("1234567890 123", 5, 0));
        assertEquals(7, thf.findWrapPos("1234567 90", 5, 3));
    }

    @Test
    public void testPrintHelpAutoUsageFalseCallsPrintUsageWithoutOptions() {
        Options options = new Options();
        options.addOption("z", false, "Z");
        formatter.printHelp(printWriter, 80, "myapp", "header", options, 1, 3, "footer", false);
        String output = getOutput();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-z"));
    }

    @Test(expected = NullPointerException.class)
    public void testPrintHelpNullOptions() {
        formatter.printHelp(printWriter, 80, "myapp", "header", null, 1, 3, "footer", false);
    }

    @Test(expected = NullPointerException.class)
    public void testPrintUsageNullOptions() {
        formatter.printUsage(printWriter, 80, "myapp", null);
    }

    @Test(expected = NullPointerException.class)
    public void testPrintOptionsNullOptions() {
        formatter.printOptions(printWriter, 80, null, 1, 3);
    }

    @Test
    public void testPrintWrappedNullText() {
        formatter.printWrapped(printWriter, 80, 0, null);
        String output = getOutput();
        assertEquals("null" + System.getProperty("line.separator"), output);
    }

    @Test
    public void testRenderWrappedTextNullText() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 80, 0, null);
        assertEquals("null", sb.toString());
    }

    @Test(expected = NegativeArraySizeException.class)
    public void testCreatePaddingNegative() {
        new TestableHelpFormatter().createPadding(-1);
    }

    @Test
    public void testRtrimOnlyWhitespace() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertEquals("", thf.rtrim("   "));
    }

    @Test
    public void testRtrimMixed() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertEquals("hello", thf.rtrim("hello   "));
    }

    @Test
    public void testPrintHelpSmallWidth() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "This is a very long description that should wrap");
        formatter.printHelp(printWriter, 20, "myapp", "header", options, 1, 3, "footer", false);
        String output = getOutput();
        assertTrue(output.contains(System.getProperty("line.separator")));
    }

    @Test
    public void testCustomNewLine() {
        HelpFormatter hf = new HelpFormatter();
        hf.setNewLine("\r\n");
        TestableHelpFormatter thf = new TestableHelpFormatter(hf);
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 10, 0, "Hello world this is a test");
        String result = sb.toString();
        assertTrue(result.contains("\r\n"));
    }

    // Helper class to expose protected methods
    private static class TestableHelpFormatter extends HelpFormatter {
        public TestableHelpFormatter() {
            super();
        }

        public TestableHelpFormatter(HelpFormatter formatter) {
            // copy settings
            this.defaultWidth = formatter.defaultWidth;
            this.defaultLeftPad = formatter.defaultLeftPad;
            this.defaultDescPad = formatter.defaultDescPad;
            this.defaultSyntaxPrefix = formatter.defaultSyntaxPrefix;
            this.defaultNewLine = formatter.defaultNewLine;
            this.defaultOptPrefix = formatter.defaultOptPrefix;
            this.defaultLongOptPrefix = formatter.defaultLongOptPrefix;
            this.defaultArgName = formatter.defaultArgName;
            this.optionComparator = formatter.optionComparator;
        }

        @Override
        public StringBuffer renderOptions(StringBuffer sb, int width, Options options, int leftPad, int descPad) {
            return super.renderOptions(sb, width, options, leftPad, descPad);
        }

        @Override
        public StringBuffer renderWrappedText(StringBuffer sb, int width, int nextLineTabStop, String text) {
            return super.renderWrappedText(sb, width, nextLineTabStop, text);
        }

        @Override
        public int findWrapPos(String text, int width, int startPos) {
            return super.findWrapPos(text, width, startPos);
        }

        @Override
        public String createPadding(int len) {
            return super.createPadding(len);
        }

        @Override
        public String rtrim(String s) {
            return super.rtrim(s);
        }
    }
}
