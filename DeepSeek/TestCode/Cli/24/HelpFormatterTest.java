package org.apache.commons.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private StringWriter sw;
    private PrintWriter pw;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        sw = new StringWriter();
        pw = new PrintWriter(sw);
    }

    @Test
    public void testDefaultValues() {
        Assert.assertEquals(74, formatter.getWidth());
        Assert.assertEquals(1, formatter.getLeftPadding());
        Assert.assertEquals(3, formatter.getDescPadding());
        Assert.assertEquals("usage: ", formatter.getSyntaxPrefix());
        Assert.assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        Assert.assertEquals("-", formatter.getOptPrefix());
        Assert.assertEquals("--", formatter.getLongOptPrefix());
        Assert.assertEquals("arg", formatter.getArgName());
        Assert.assertNotNull(formatter.getOptionComparator());
    }

    @Test
    public void testSetWidth() {
        formatter.setWidth(80);
        Assert.assertEquals(80, formatter.getWidth());
    }

    @Test
    public void testSetLeftPadding() {
        formatter.setLeftPadding(5);
        Assert.assertEquals(5, formatter.getLeftPadding());
    }

    @Test
    public void testSetDescPadding() {
        formatter.setDescPadding(7);
        Assert.assertEquals(7, formatter.getDescPadding());
    }

    @Test
    public void testSetSyntaxPrefix() {
        formatter.setSyntaxPrefix("cmd: ");
        Assert.assertEquals("cmd: ", formatter.getSyntaxPrefix());
    }

    @Test
    public void testSetNewLine() {
        formatter.setNewLine("\r\n");
        Assert.assertEquals("\r\n", formatter.getNewLine());
    }

    @Test
    public void testSetOptPrefix() {
        formatter.setOptPrefix("/");
        Assert.assertEquals("/", formatter.getOptPrefix());
    }

    @Test
    public void testSetLongOptPrefix() {
        formatter.setLongOptPrefix("---");
        Assert.assertEquals("---", formatter.getLongOptPrefix());
    }

    @Test
    public void testSetArgName() {
        formatter.setArgName("FILE");
        Assert.assertEquals("FILE", formatter.getArgName());
    }

    @Test
    public void testSetOptionComparator() {
        Comparator<Option> comp = new Comparator<Option>() {
            public int compare(Option o1, Option o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        };
        formatter.setOptionComparator(comp);
        Assert.assertSame(comp, formatter.getOptionComparator());
    }

    @Test
    public void testSetOptionComparatorNullResetsToDefault() {
        formatter.setOptionComparator(null);
        Assert.assertTrue(formatter.getOptionComparator() instanceof HelpFormatter.OptionComparator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        formatter.printHelp(pw, 80, null, "header", new Options(), 1, 3, "footer", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        formatter.printHelp(pw, 80, "", "header", new Options(), 1, 3, "footer", false);
    }

    @Test
    public void testPrintHelpAutoUsageTrue() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "test", "header", options, 1, 3, "footer", true);
        String output = sw.toString();
        Assert.assertTrue(output.contains("usage: test"));
        Assert.assertTrue(output.contains("-a"));
        Assert.assertTrue(output.contains("header"));
        Assert.assertTrue(output.contains("footer"));
    }

    @Test
    public void testPrintHelpAutoUsageFalse() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "test", "header", options, 1, 3, "footer", false);
        String output = sw.toString();
        Assert.assertTrue(output.contains("usage: test"));
        Assert.assertFalse(output.contains("-a"));
        Assert.assertTrue(output.contains("header"));
        Assert.assertTrue(output.contains("footer"));
    }

    @Test
    public void testPrintHelpHeaderNull() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "test", null, options, 1, 3, "footer", false);
        String output = sw.toString();
        Assert.assertFalse(output.contains("header"));
    }

    @Test
    public void testPrintHelpHeaderEmpty() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "test", "", options, 1, 3, "footer", false);
        String output = sw.toString();
        Assert.assertFalse(output.contains("header"));
    }

    @Test
    public void testPrintHelpHeaderWhitespace() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "test", "   ", options, 1, 3, "footer", false);
        String output = sw.toString();
        Assert.assertFalse(output.contains("   "));
    }

    @Test
    public void testPrintHelpFooterNull() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "test", "header", options, 1, 3, null, false);
        String output = sw.toString();
        Assert.assertFalse(output.contains("footer"));
    }

    @Test
    public void testPrintHelpFooterEmpty() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "test", "header", options, 1, 3, "", false);
        String output = sw.toString();
        Assert.assertFalse(output.contains("footer"));
    }

    @Test
    public void testPrintHelpFooterWhitespace() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "test", "header", options, 1, 3, "   ", false);
        String output = sw.toString();
        Assert.assertFalse(output.contains("   "));
    }

    @Test
    public void testPrintUsageWithOptions() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        options.addOption("b", true, "beta");
        formatter.printUsage(pw, 80, "test", options);
        String output = sw.toString();
        Assert.assertTrue(output.contains("usage: test"));
        Assert.assertTrue(output.contains("-a"));
        Assert.assertTrue(output.contains("-b <arg>"));
    }

    @Test
    public void testPrintUsageWithOptionGroup() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha desc"));
        group.addOption(new Option("b", "beta", false, "beta desc"));
        options.addOptionGroup(group);
        formatter.printUsage(pw, 80, "test", options);
        String output = sw.toString();
        Assert.assertTrue(output.contains("[-a | -b]"));
    }

    @Test
    public void testPrintUsageWithRequiredOptionGroup() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "alpha desc"));
        group.addOption(new Option("b", "beta", false, "beta desc"));
        options.addOptionGroup(group);
        formatter.printUsage(pw, 80, "test", options);
        String output = sw.toString();
        Assert.assertTrue(output.contains("-a | -b"));
        Assert.assertFalse(output.contains("["));
    }

    @Test
    public void testPrintUsageWithoutOptions() {
        formatter.printUsage(pw, 80, "test");
        String output = sw.toString();
        Assert.assertTrue(output.contains("usage: test"));
    }

    @Test
    public void testPrintOptions() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha desc");
        options.addOption("b", "beta", true, "beta desc");
        formatter.printOptions(pw, 80, options, 1, 3);
        String output = sw.toString();
        Assert.assertTrue(output.contains("-a,--alpha"));
        Assert.assertTrue(output.contains("-b,--beta <arg>"));
        Assert.assertTrue(output.contains("alpha desc"));
        Assert.assertTrue(output.contains("beta desc"));
    }

    @Test
    public void testPrintWrapped() {
        formatter.printWrapped(pw, 10, "hello world");
        String output = sw.toString();
        Assert.assertTrue(output.contains("hello"));
        Assert.assertTrue(output.contains("world"));
    }

    @Test
    public void testPrintWrappedWithNextLineTabStop() {
        formatter.printWrapped(pw, 10, 5, "hello world");
        String output = sw.toString();
        Assert.assertTrue(output.contains("hello"));
        Assert.assertTrue(output.contains("world"));
    }

    @Test
    public void testRenderOptions() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha desc");
        options.addOption("b", "beta", true, "beta desc");
        StringBuffer sb = new StringBuffer();
        thf.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        Assert.assertTrue(output.contains("-a,--alpha"));
        Assert.assertTrue(output.contains("-b,--beta <arg>"));
    }

    @Test
    public void testRenderOptionsWithLongOptOnly() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Options options = new Options();
        options.addOption(new Option(null, "alpha", false, "alpha desc"));
        StringBuffer sb = new StringBuffer();
        thf.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        Assert.assertTrue(output.contains("--alpha"));
    }

    @Test
    public void testRenderOptionsWithArgNoArgName() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "alpha desc");
        opt.setArgName(null);
        options.addOption(opt);
        StringBuffer sb = new StringBuffer();
        thf.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        Assert.assertTrue(output.contains("-a,--alpha"));
        Assert.assertFalse(output.contains("<"));
    }

    @Test
    public void testRenderWrappedTextFits() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 20, 5, "short text");
        Assert.assertEquals("short text", sb.toString());
    }

    @Test
    public void testRenderWrappedTextWraps() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        thf.setNewLine("\n");
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 10, 5, "hello world foo bar");
        String output = sb.toString();
        Assert.assertTrue(output.contains("hello"));
        Assert.assertTrue(output.contains("world"));
        Assert.assertTrue(output.contains("foo"));
        Assert.assertTrue(output.contains("bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void testRenderWrappedTextNextLineTabStopGreaterThanWidth() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 5, 5, "hello world");
    }

    @Test
    public void testRenderWrappedTextWithNewlineInText() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        thf.setNewLine("\n");
        StringBuffer sb = new StringBuffer();
        thf.renderWrappedText(sb, 20, 5, "hello\nworld");
        String output = sb.toString();
        Assert.assertTrue(output.contains("hello"));
        Assert.assertTrue(output.contains("world"));
    }

    @Test
    public void testFindWrapPosNewline() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello\nworld", 10, 0);
        Assert.assertEquals(6, pos);
    }

    @Test
    public void testFindWrapPosTab() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello\tworld", 10, 0);
        Assert.assertEquals(6, pos);
    }

    @Test
    public void testFindWrapPosSpace() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello world", 10, 0);
        Assert.assertEquals(5, pos);
    }

    @Test
    public void testFindWrapPosNoSpaceWithinWidth() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("helloworld", 5, 0);
        Assert.assertEquals(5, pos);
    }

    @Test
    public void testFindWrapPosStartPosPlusWidthBeyondLength() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello", 10, 0);
        Assert.assertEquals(-1, pos);
    }

    @Test
    public void testFindWrapPosStartPosPlusWidthExactlyLength() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello", 5, 0);
        Assert.assertEquals(-1, pos);
    }

    @Test
    public void testFindWrapPosNoWhitespaceAfterWidth() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("helloworld", 5, 0);
        Assert.assertEquals(5, pos);
    }

    @Test
    public void testCreatePadding() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Assert.assertEquals("", thf.createPadding(0));
        Assert.assertEquals("   ", thf.createPadding(3));
    }

    @Test
    public void testRtrimNull() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Assert.assertNull(thf.rtrim(null));
    }

    @Test
    public void testRtrimEmpty() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Assert.assertEquals("", thf.rtrim(""));
    }

    @Test
    public void testRtrimTrailingSpaces() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Assert.assertEquals("hello", thf.rtrim("hello   "));
    }

    @Test
    public void testRtrimNoTrailingSpaces() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        Assert.assertEquals("hello", thf.rtrim("hello"));
    }

    @Test
    public void testOptionComparator() {
        Comparator comp = formatter.getOptionComparator();
        Option opt1 = new Option("a", "alpha", false, "desc");
        Option opt2 = new Option("b", "beta", false, "desc");
        Assert.assertTrue(comp.compare(opt1, opt2) < 0);
        Assert.assertTrue(comp.compare(opt2, opt1) > 0);
        Assert.assertEquals(0, comp.compare(opt1, opt1));
    }

    @Test
    public void testOptionComparatorCaseInsensitive() {
        Comparator comp = formatter.getOptionComparator();
        Option opt1 = new Option("a", "Alpha", false, "desc");
        Option opt2 = new Option("A", "alpha", false, "desc");
        Assert.assertEquals(0, comp.compare(opt1, opt2));
    }

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
}
