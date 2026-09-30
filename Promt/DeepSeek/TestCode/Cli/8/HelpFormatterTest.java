package org.apache.commons.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    // Test constants
    @Test
    public void testConstants() {
        Assert.assertEquals(74, HelpFormatter.DEFAULT_WIDTH);
        Assert.assertEquals(1, HelpFormatter.DEFAULT_LEFT_PAD);
        Assert.assertEquals(3, HelpFormatter.DEFAULT_DESC_PAD);
        Assert.assertEquals("usage: ", HelpFormatter.DEFAULT_SYNTAX_PREFIX);
        Assert.assertEquals("-", HelpFormatter.DEFAULT_OPT_PREFIX);
        Assert.assertEquals("--", HelpFormatter.DEFAULT_LONG_OPT_PREFIX);
        Assert.assertEquals("arg", HelpFormatter.DEFAULT_ARG_NAME);
    }

    // Test getters and setters
    @Test
    public void testGetSetWidth() {
        formatter.setWidth(100);
        Assert.assertEquals(100, formatter.getWidth());
    }

    @Test
    public void testGetSetLeftPadding() {
        formatter.setLeftPadding(5);
        Assert.assertEquals(5, formatter.getLeftPadding());
    }

    @Test
    public void testGetSetDescPadding() {
        formatter.setDescPadding(7);
        Assert.assertEquals(7, formatter.getDescPadding());
    }

    @Test
    public void testGetSetSyntaxPrefix() {
        formatter.setSyntaxPrefix("cmd> ");
        Assert.assertEquals("cmd> ", formatter.getSyntaxPrefix());
    }

    @Test
    public void testGetSetNewLine() {
        formatter.setNewLine("\r\n");
        Assert.assertEquals("\r\n", formatter.getNewLine());
    }

    @Test
    public void testGetSetOptPrefix() {
        formatter.setOptPrefix("+");
        Assert.assertEquals("+", formatter.getOptPrefix());
    }

    @Test
    public void testGetSetLongOptPrefix() {
        formatter.setLongOptPrefix("---");
        Assert.assertEquals("---", formatter.getLongOptPrefix());
    }

    @Test
    public void testGetSetArgName() {
        formatter.setArgName("FILE");
        Assert.assertEquals("FILE", formatter.getArgName());
    }

    // Test printHelp overloads

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        formatter.printHelp(null, new Options());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        formatter.printHelp("", new Options());
    }

    @Test
    public void testPrintHelpSimple() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        formatter.printHelp(printWriter, 80, "myapp", "Header", options, 2, 4, "Footer", false);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("usage: myapp"));
        Assert.assertTrue(output.contains("Header"));
        Assert.assertTrue(output.contains("-a,--alpha"));
        Assert.assertTrue(output.contains("Alpha option"));
        Assert.assertTrue(output.contains("Footer"));
    }

    @Test
    public void testPrintHelpAutoUsage() {
        Options options = new Options();
        options.addOption("b", false, "Beta");
        formatter.printHelp(printWriter, 80, "myapp", "Header", options, 2, 4, "Footer", true);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("usage: myapp"));
        Assert.assertTrue(output.contains("-b"));
        Assert.assertTrue(output.contains("Header"));
        Assert.assertTrue(output.contains("Footer"));
    }

    @Test
    public void testPrintHelpNoHeaderFooter() {
        Options options = new Options();
        options.addOption("c", "gamma", true, "Gamma option");
        formatter.printHelp(printWriter, 80, "myapp", null, options, 2, 4, null, false);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertFalse(output.contains("Header"));
        Assert.assertFalse(output.contains("Footer"));
        Assert.assertTrue(output.contains("usage: myapp"));
    }

    @Test
    public void testPrintHelpHeaderFooterEmpty() {
        Options options = new Options();
        options.addOption("d", false, "Delta");
        formatter.printHelp(printWriter, 80, "myapp", "", options, 2, 4, "", false);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertFalse(output.contains("Header"));
        Assert.assertFalse(output.contains("Footer"));
    }

    @Test
    public void testPrintHelpHeaderFooterWhitespace() {
        Options options = new Options();
        options.addOption("e", false, "Epsilon");
        formatter.printHelp(printWriter, 80, "myapp", "   ", options, 2, 4, "   ", false);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertFalse(output.contains("Header"));
        Assert.assertFalse(output.contains("Footer"));
    }

    // Test printUsage with options
    @Test
    public void testPrintUsageWithOptions() {
        Options options = new Options();
        options.addOption("f", "foxtrot", false, "Foxtrot");
        options.addOption("g", false, "Golf");
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("usage: myapp"));
        Assert.assertTrue(output.contains("-f"));
        Assert.assertTrue(output.contains("--foxtrot"));
        Assert.assertTrue(output.contains("-g"));
    }

    @Test
    public void testPrintUsageWithRequiredOption() {
        Options options = new Options();
        Option requiredOpt = new Option("r", "required", true, "Required option");
        requiredOpt.setRequired(true);
        options.addOption(requiredOpt);
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("-r <arg>"));
        Assert.assertFalse(output.contains("["));
    }

    @Test
    public void testPrintUsageWithOptionalOption() {
        Options options = new Options();
        options.addOption("o", "optional", false, "Optional option");
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("[-o]"));
    }

    @Test
    public void testPrintUsageWithOptionGroup() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", "xray", false, "Xray"));
        group.addOption(new Option("y", "yankee", false, "Yankee"));
        options.addOptionGroup(group);
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("[-x | -y]"));
    }

    @Test
    public void testPrintUsageWithRequiredOptionGroup() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("x", "xray", false, "Xray"));
        group.addOption(new Option("y", "yankee", false, "Yankee"));
        options.addOptionGroup(group);
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("-x | -y"));
        Assert.assertFalse(output.contains("["));
    }

    @Test
    public void testPrintUsageWithLongOptOnly() {
        Options options = new Options();
        Option longOnly = new Option(null, "longonly", false, "Long only");
        options.addOption(longOnly);
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("--longonly"));
    }

    @Test
    public void testPrintUsageWithArgAndArgName() {
        Options options = new Options();
        Option argOpt = new Option("a", "arg", true, "With arg");
        argOpt.setArgName("FILE");
        options.addOption(argOpt);
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("-a <FILE>"));
    }

    @Test
    public void testPrintUsageWithArgNoArgName() {
        Options options = new Options();
        Option argOpt = new Option("b", "beta", true, "Beta");
        // argName is null by default
        options.addOption(argOpt);
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("-b"));
        Assert.assertFalse(output.contains("<"));
    }

    // Test printUsage without options
    @Test
    public void testPrintUsageWithoutOptions() {
        formatter.printUsage(printWriter, 80, "myapp");
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("usage: myapp"));
    }

    // Test printOptions
    @Test
    public void testPrintOptions() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha description");
        options.addOption("b", "beta", true, "Beta description");
        formatter.printOptions(printWriter, 80, options, 2, 4);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("-a,--alpha"));
        Assert.assertTrue(output.contains("Alpha description"));
        Assert.assertTrue(output.contains("-b,--beta <arg>"));
        Assert.assertTrue(output.contains("Beta description"));
    }

    @Test
    public void testPrintOptionsWithLongOptOnly() {
        Options options = new Options();
        Option longOnly = new Option(null, "longonly", false, "Long only desc");
        options.addOption(longOnly);
        formatter.printOptions(printWriter, 80, options, 2, 4);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("--longonly"));
        Assert.assertTrue(output.contains("Long only desc"));
    }

    @Test
    public void testPrintOptionsWithArgAndArgName() {
        Options options = new Options();
        Option argOpt = new Option("c", "charlie", true, "Charlie desc");
        argOpt.setArgName("INPUT");
        options.addOption(argOpt);
        formatter.printOptions(printWriter, 80, options, 2, 4);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("-c,--charlie <INPUT>"));
    }

    @Test
    public void testPrintOptionsWithArgNoArgName() {
        Options options = new Options();
        Option argOpt = new Option("d", "delta", true, "Delta desc");
        // argName null
        options.addOption(argOpt);
        formatter.printOptions(printWriter, 80, options, 2, 4);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("-d,--delta  ")); // space appended
    }

    @Test
    public void testPrintOptionsNoDescription() {
        Options options = new Options();
        options.addOption("e", "echo", false, null);
        formatter.printOptions(printWriter, 80, options, 2, 4);
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("-e,--echo"));
        // no description appended
    }

    // Test printWrapped
    @Test
    public void testPrintWrappedSimple() {
        formatter.printWrapped(printWriter, 20, "Hello world");
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("Hello world"));
    }

    @Test
    public void testPrintWrappedWithWrap() {
        formatter.printWrapped(printWriter, 10, "Hello world this is a test");
        printWriter.flush();
        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("Hello"));
        Assert.assertTrue(output.contains("world"));
    }

    @Test
    public void testPrintWrappedWithNextLineTabStop() {
        formatter.printWrapped(printWriter, 10, 5, "Hello world this is a test");
        printWriter.flush();
        String output = stringWriter.toString();
        // second line should be indented by 5 spaces
        Assert.assertTrue(output.contains("Hello"));
        Assert.assertTrue(output.contains("     world"));
    }

    // Test protected methods via subclass
    @Test
    public void testCreatePadding() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Assert.assertEquals("", thf.createPadding(0));
        Assert.assertEquals("   ", thf.createPadding(3));
    }

    @Test
    public void testRtrim() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Assert.assertNull(thf.rtrim(null));
        Assert.assertEquals("", thf.rtrim(""));
        Assert.assertEquals("abc", thf.rtrim("abc   "));
        Assert.assertEquals("abc", thf.rtrim("abc"));
        Assert.assertEquals("", thf.rtrim("   "));
    }

    @Test
    public void testFindWrapPos() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        // text shorter than width
        Assert.assertEquals(-1, thf.findWrapPos("short", 10, 0));
        // newline before width
        Assert.assertEquals(4, thf.findWrapPos("abc\ndef", 10, 0));
        // tab before width
        Assert.assertEquals(4, thf.findWrapPos("abc\tdef", 10, 0));
        // startPos+width >= text.length()
        Assert.assertEquals(-1, thf.findWrapPos("abc", 5, 0));
        // whitespace exactly at width
        Assert.assertEquals(5, thf.findWrapPos("hello world", 5, 0));
        // no whitespace before width, find first after
        Assert.assertEquals(11, thf.findWrapPos("helloworld test", 10, 0));
        // no whitespace at all after width
        Assert.assertEquals(-1, thf.findWrapPos("helloworld", 5, 0));
        // whitespace at startPos+width-1
        Assert.assertEquals(9, thf.findWrapPos("hello world", 10, 0));
        // newline exactly at width
        Assert.assertEquals(6, thf.findWrapPos("hello\nworld", 5, 0));
        // tab exactly at width
        Assert.assertEquals(6, thf.findWrapPos("hello\tworld", 5, 0));
    }

    @Test
    public void testRenderWrappedText() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 10, 0, "Hello world");
        Assert.assertEquals("Hello\nworld", sb.toString());
    }

    @Test
    public void testRenderWrappedTextWithNextLineTabStop() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 10, 5, "Hello world this is a test");
        // first line: "Hello" (5 chars) then newline, then padding 5 spaces + "world" etc.
        Assert.assertTrue(sb.toString().startsWith("Hello\n     world"));
    }

    @Test
    public void testRenderOptions() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha desc");
        options.addOption("b", "beta", true, "Beta desc");
        StringBuffer sb = new StringBuffer();
        thf.renderOptions(sb, 80, options, 2, 4);
        String output = sb.toString();
        Assert.assertTrue(output.contains("-a,--alpha"));
        Assert.assertTrue(output.contains("Alpha desc"));
        Assert.assertTrue(output.contains("-b,--beta <arg>"));
        Assert.assertTrue(output.contains("Beta desc"));
    }

    // Test OptionComparator indirectly via sorting
    @Test
    public void testOptionComparatorSorting() {
        Options options = new Options();
        options.addOption("c", "charlie", false, "C");
        options.addOption("a", "alpha", false, "A");
        options.addOption("b", "beta", false, "B");
        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();
        int posA = output.indexOf("-a");
        int posB = output.indexOf("-b");
        int posC = output.indexOf("-c");
        Assert.assertTrue(posA < posB);
        Assert.assertTrue(posB < posC);
    }

    // Helper class to expose protected methods
    private static class TestableHelpFormatter extends HelpFormatter {
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
