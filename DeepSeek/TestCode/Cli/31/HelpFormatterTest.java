package org.apache.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import org.junit.Test;

public class HelpFormatterTest {

    private static class TestableHelpFormatter extends HelpFormatter {
        public StringBuffer renderOptions(StringBuffer sb, int width, Options options, int leftPad, int descPad) {
            return super.renderOptions(sb, width, options, leftPad, descPad);
        }
        public StringBuffer renderWrappedText(StringBuffer sb, int width, int nextLineTabStop, String text) {
            return super.renderWrappedText(sb, width, nextLineTabStop, text);
        }
        public int findWrapPos(String text, int width, int startPos) {
            return super.findWrapPos(text, width, startPos);
        }
        public String createPadding(int len) {
            return super.createPadding(len);
        }
        public String rtrim(String s) {
            return super.rtrim(s);
        }
    }

    @Test
    public void testDefaultConstants() {
        HelpFormatter formatter = new HelpFormatter();
        assertEquals(74, formatter.defaultWidth);
        assertEquals(1, formatter.defaultLeftPad);
        assertEquals(3, formatter.defaultDescPad);
        assertEquals("usage: ", formatter.defaultSyntaxPrefix);
        assertEquals("-", formatter.defaultOptPrefix);
        assertEquals("--", formatter.defaultLongOptPrefix);
        assertEquals(" ", formatter.getLongOptSeparator());
        assertEquals("arg", formatter.defaultArgName);
        assertNotNull(formatter.getOptionComparator());
    }

    @Test
    public void testSetWidth() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(80);
        assertEquals(80, formatter.getWidth());
    }

    @Test
    public void testSetLeftPadding() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
    }

    @Test
    public void testSetDescPadding() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setDescPadding(10);
        assertEquals(10, formatter.getDescPadding());
    }

    @Test
    public void testSetSyntaxPrefix() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("test: ");
        assertEquals("test: ", formatter.getSyntaxPrefix());
    }

    @Test
    public void testSetNewLine() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());
    }

    @Test
    public void testSetOptPrefix() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());
    }

    @Test
    public void testSetLongOptPrefix() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());
    }

    @Test
    public void testSetLongOptSeparator() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());
    }

    @Test
    public void testSetArgName() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setArgName("filename");
        assertEquals("filename", formatter.getArgName());
    }

    @Test
    public void testSetOptionComparatorNull() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        assertTrue(formatter.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }

    @Test
    public void testSetOptionComparatorCustom() {
        HelpFormatter formatter = new HelpFormatter();
        Comparator custom = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(custom);
        assertEquals(custom, formatter.getOptionComparator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp((String) null, new Options());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("", new Options());
    }

    @Test
    public void testPrintHelpAutoUsageTrue() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha option");
        formatter.printHelp(pw, 80, "test", "Header", options, 1, 3, "Footer", true);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: test"));
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("Alpha option"));
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("Footer"));
    }

    @Test
    public void testPrintHelpAutoUsageFalse() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "test", null, new Options(), 1, 3, null, false);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: test"));
    }

    @Test
    public void testPrintHelpWithHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        formatter.printHelp(pw, 80, "test", "Header", options, 1, 3, "Footer", false);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("Header"));
        assertTrue(output.contains("Footer"));
    }

    @Test
    public void testPrintHelpNullHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "test", null, new Options(), 1, 3, null, false);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: test"));
    }

    @Test
    public void testPrintHelpEmptyHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "test", "", new Options(), 1, 3, "", false);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: test"));
    }

    @Test
    public void testPrintHelpWhitespaceHeaderFooter() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "test", "   ", new Options(), 1, 3, "   ", false);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: test"));
    }

    @Test(expected = NullPointerException.class)
    public void testPrintHelpNullOptions() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "test", null, null, 1, 3, null, false);
    }

    @Test
    public void testPrintUsageWithOptions() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", false, "alpha");
        options.addOption("b", "beta", true, "beta option");
        formatter.printUsage(pw, 80, "test", options);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("usage: test"));
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("--beta <arg>"));
    }

    @Test
    public void testPrintUsageWithOptionGroup() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha"));
        group.addOption(new Option("b", "beta", false, "beta"));
        options.addOptionGroup(group);
        formatter.printUsage(pw, 80, "test", options);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("[-a | -b]"));
    }

    @Test
    public void testPrintUsageRequiredOptionGroup() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "alpha"));
        group.addOption(new Option("b", "beta", false, "beta"));
        options.addOptionGroup(group);
        formatter.printUsage(pw, 80, "test", options);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("-a | -b"));
    }

    @Test
    public void testPrintUsageWithoutOptions() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "test");
        pw.flush();
        String output = sw.toString();
        assertEquals("usage: test" + formatter.getNewLine(), output);
    }

    @Test(expected = NullPointerException.class)
    public void testPrintUsageNullApp() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, null, new Options());
    }

    @Test
    public void testPrintOptions() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha description");
        options.addOption("b", "beta", true, "Beta description");
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("Alpha description"));
        assertTrue(output.contains("-b,--beta <arg>"));
        assertTrue(output.contains("Beta description"));
    }

    @Test
    public void testPrintWrapped() {
        HelpFormatter formatter = new HelpFormatter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 20, 0, "This is a long text that should wrap");
        pw.flush();
        String output = sw.toString();
        assertTrue(output.contains("This is a long text"));
        assertTrue(output.contains("that should wrap"));
    }

    @Test
    public void testRenderOptions() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha");
        options.addOption("b", "beta", true, "Beta");
        formatter.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("Alpha"));
        assertTrue(output.contains("-b,--beta <arg>"));
        assertTrue(output.contains("Beta"));
    }

    @Test
    public void testRenderOptionsWithLongOptOnly() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        Options options = new Options();
        options.addOption(new Option(null, "alpha", false, "Alpha"));
        formatter.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        assertTrue(output.contains("--alpha"));
    }

    @Test
    public void testRenderOptionsWithArgAndBlankArgName() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "Alpha");
        opt.setArgName("");
        options.addOption(opt);
        formatter.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        assertTrue(output.contains("-a,--alpha"));
    }

    @Test
    public void testRenderWrappedTextNoWrap() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 80, 0, "Short text");
        assertEquals("Short text", sb.toString());
    }

    @Test
    public void testRenderWrappedTextWithWrap() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 0, "This is a long text");
        String output = sb.toString();
        assertTrue(output.contains("This is a"));
        assertTrue(output.contains("long text"));
    }

    @Test
    public void testRenderWrappedTextNextLineTabStopExceedsWidth() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 15, "This is a long text that should wrap");
        String output = sb.toString();
        assertTrue(output.contains("This is a"));
    }

    @Test
    public void testFindWrapPosNewline() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        int pos = formatter.findWrapPos("Hello\nWorld", 10, 0);
        assertEquals(6, pos);
    }

    @Test
    public void testFindWrapPosTab() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        int pos = formatter.findWrapPos("Hello\tWorld", 10, 0);
        assertEquals(6, pos);
    }

    @Test
    public void testFindWrapPosEndOfText() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        int pos = formatter.findWrapPos("Hello", 10, 0);
        assertEquals(-1, pos);
    }

    @Test
    public void testFindWrapPosNoWhitespace() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        int pos = formatter.findWrapPos("HelloWorld", 5, 0);
        assertEquals(5, pos);
    }

    @Test
    public void testFindWrapPosWhitespaceAtWidth() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        int pos = formatter.findWrapPos("Hello World", 6, 0);
        assertEquals(5, pos);
    }

    @Test
    public void testCreatePadding() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    @Test
    public void testRtrimNull() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        assertEquals(null, formatter.rtrim(null));
    }

    @Test
    public void testRtrimEmpty() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        assertEquals("", formatter.rtrim(""));
    }

    @Test
    public void testRtrimTrailingSpaces() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        assertEquals("hello", formatter.rtrim("hello   "));
    }

    @Test
    public void testRtrimNoTrailingSpaces() {
        TestableHelpFormatter formatter = new TestableHelpFormatter();
        assertEquals("hello", formatter.rtrim("hello"));
    }

    @Test
    public void testOptionComparator() {
        HelpFormatter formatter = new HelpFormatter();
        Comparator comp = formatter.getOptionComparator();
        Option opt1 = new Option("a", "alpha", false, "");
        Option opt2 = new Option("b", "beta", false, "");
        assertTrue(comp.compare(opt1, opt2) < 0);
        assertTrue(comp.compare(opt2, opt1) > 0);
        assertEquals(0, comp.compare(opt1, opt1));
    }
}
