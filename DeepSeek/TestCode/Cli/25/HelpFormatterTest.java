package org.apache.commons.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private StringWriter sw;
    private PrintWriter pw;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        formatter.defaultNewLine = "\n";
        sw = new StringWriter();
        pw = new PrintWriter(sw);
    }

    @Test
    public void testDefaultValues() {
        assertEquals(74, formatter.defaultWidth);
        assertEquals(1, formatter.defaultLeftPad);
        assertEquals(3, formatter.defaultDescPad);
        assertEquals("usage: ", formatter.defaultSyntaxPrefix);
        assertEquals("\n", formatter.defaultNewLine);
        assertEquals("-", formatter.defaultOptPrefix);
        assertEquals("--", formatter.defaultLongOptPrefix);
        assertEquals("arg", formatter.defaultArgName);
        assertNotNull(formatter.getOptionComparator());
    }

    @Test
    public void testSetWidth() {
        formatter.setWidth(80);
        assertEquals(80, formatter.getWidth());
    }

    @Test
    public void testSetLeftPadding() {
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
    }

    @Test
    public void testSetDescPadding() {
        formatter.setDescPadding(7);
        assertEquals(7, formatter.getDescPadding());
    }

    @Test
    public void testSetSyntaxPrefix() {
        formatter.setSyntaxPrefix("cmd: ");
        assertEquals("cmd: ", formatter.getSyntaxPrefix());
    }

    @Test
    public void testSetNewLine() {
        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());
    }

    @Test
    public void testSetOptPrefix() {
        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());
    }

    @Test
    public void testSetLongOptPrefix() {
        formatter.setLongOptPrefix("---");
        assertEquals("---", formatter.getLongOptPrefix());
    }

    @Test
    public void testSetArgName() {
        formatter.setArgName("FILE");
        assertEquals("FILE", formatter.getArgName());
    }

    @Test
    public void testSetOptionComparator() {
        Comparator<Option> comp = new Comparator<Option>() {
            public int compare(Option o1, Option o2) {
                return o1.getOpt().compareTo(o2.getOpt());
            }
        };
        formatter.setOptionComparator(comp);
        assertSame(comp, formatter.getOptionComparator());
    }

    @Test
    public void testSetOptionComparatorNullResetsToDefault() {
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        assertTrue(formatter.getOptionComparator() instanceof HelpFormatter.OptionComparator);
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
        formatter.printHelp(pw, 80, "myapp", "header", options, 1, 3, "footer", true);
        String output = sw.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("footer"));
    }

    @Test
    public void testPrintHelpAutoUsageFalse() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "myapp", "header", options, 1, 3, "footer", false);
        String output = sw.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("header"));
        assertTrue(output.contains("footer"));
    }

    @Test
    public void testPrintHelpHeaderNull() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "myapp", null, options, 1, 3, "footer", false);
        String output = sw.toString();
        assertFalse(output.contains("header"));
        assertTrue(output.contains("footer"));
    }

    @Test
    public void testPrintHelpHeaderEmpty() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "myapp", "", options, 1, 3, "footer", false);
        String output = sw.toString();
        assertFalse(output.contains("header"));
    }

    @Test
    public void testPrintHelpHeaderWhitespace() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "myapp", "   ", options, 1, 3, "footer", false);
        String output = sw.toString();
        assertFalse(output.contains("   "));
    }

    @Test
    public void testPrintHelpFooterNull() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "myapp", "header", options, 1, 3, null, false);
        String output = sw.toString();
        assertTrue(output.contains("header"));
        assertFalse(output.contains("footer"));
    }

    @Test
    public void testPrintHelpFooterEmpty() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "myapp", "header", options, 1, 3, "", false);
        String output = sw.toString();
        assertTrue(output.contains("header"));
        assertFalse(output.contains("footer"));
    }

    @Test
    public void testPrintHelpFooterWhitespace() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        formatter.printHelp(pw, 80, "myapp", "header", options, 1, 3, "   ", false);
        String output = sw.toString();
        assertTrue(output.contains("header"));
        assertFalse(output.contains("   "));
    }

    @Test(expected = NullPointerException.class)
    public void testPrintHelpOptionsNull() {
        formatter.printHelp(pw, 80, "myapp", "header", null, 1, 3, "footer", false);
    }

    @Test
    public void testPrintUsageWithOptions() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        options.addOption("b", "beta", true, "beta desc");
        formatter.printUsage(pw, 80, "myapp", options);
        String output = sw.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("-a"));
        assertTrue(output.contains("-b <arg>"));
    }

    @Test
    public void testPrintUsageWithOptionGroup() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("a", "alpha", false, "alpha desc"));
        group.addOption(new Option("b", "beta", false, "beta desc"));
        options.addOptionGroup(group);
        formatter.printUsage(pw, 80, "myapp", options);
        String output = sw.toString();
        assertTrue(output.contains("[-a | -b]"));
    }

    @Test
    public void testPrintUsageWithRequiredOptionGroup() {
        Options options = new Options();
        OptionGroup group = new OptionGroup();
        group.setRequired(true);
        group.addOption(new Option("a", "alpha", false, "alpha desc"));
        group.addOption(new Option("b", "beta", false, "beta desc"));
        options.addOptionGroup(group);
        formatter.printUsage(pw, 80, "myapp", options);
        String output = sw.toString();
        assertTrue(output.contains("-a | -b"));
        assertFalse(output.contains("["));
    }

    @Test
    public void testPrintUsageWithoutOptions() {
        formatter.printUsage(pw, 80, "myapp");
        String output = sw.toString();
        assertTrue(output.contains("usage: myapp"));
    }

    @Test
    public void testPrintOptions() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha description");
        options.addOption("b", "beta", true, "beta description");
        formatter.printOptions(pw, 80, options, 1, 3);
        String output = sw.toString();
        assertTrue(output.contains("-a,--alpha"));
        assertTrue(output.contains("alpha description"));
        assertTrue(output.contains("-b,--beta <arg>"));
        assertTrue(output.contains("beta description"));
    }

    @Test
    public void testPrintWrappedSimple() {
        formatter.printWrapped(pw, 80, "Hello World");
        String output = sw.toString();
        assertTrue(output.contains("Hello World"));
    }

    @Test
    public void testPrintWrappedWithTabStop() {
        formatter.printWrapped(pw, 20, 5, "This is a long text that should wrap");
        String output = sw.toString();
        assertTrue(output.contains("This is a long"));
        assertTrue(output.contains("     text"));
    }

    @Test
    public void testRenderOptionsWithLongOptOnly() {
        Options options = new Options();
        options.addOption(new Option(null, "longonly", false, "long only desc"));
        formatter.printOptions(pw, 80, options, 1, 3);
        String output = sw.toString();
        assertTrue(output.contains("--longonly"));
    }

    @Test
    public void testRenderOptionsWithArgNoArgName() {
        Options options = new Options();
        Option opt = new Option("a", false, "desc");
        opt.setArgs(1);
        options.addOption(opt);
        formatter.printOptions(pw, 80, options, 1, 3);
        String output = sw.toString();
        assertTrue(output.contains("-a"));
    }

    @Test
    public void testRenderOptionsSorting() {
        Options options = new Options();
        options.addOption("c", false, "c desc");
        options.addOption("a", false, "a desc");
        options.addOption("b", false, "b desc");
        formatter.printOptions(pw, 80, options, 1, 3);
        String output = sw.toString();
        int posA = output.indexOf("-a");
        int posB = output.indexOf("-b");
        int posC = output.indexOf("-c");
        assertTrue(posA < posB);
        assertTrue(posB < posC);
    }

    @Test
    public void testFindWrapPosNewline() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello\nworld", 10, 0);
        assertEquals(6, pos);
    }

    @Test
    public void testFindWrapPosTab() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello\tworld", 10, 0);
        assertEquals(6, pos);
    }

    @Test
    public void testFindWrapPosSpaceWithinWidth() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello world", 10, 0);
        assertEquals(5, pos);
    }

    @Test
    public void testFindWrapPosNoWhitespace() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("helloworld", 5, 0);
        assertEquals(5, pos);
    }

    @Test
    public void testFindWrapPosEndOfText() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello", 10, 0);
        assertEquals(-1, pos);
    }

    @Test
    public void testFindWrapPosStartPosBeyondWidth() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        int pos = thf.findWrapPos("hello world", 5, 6);
        assertEquals(11, pos);
    }

    @Test
    public void testCreatePadding() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        String pad = thf.createPadding(5);
        assertEquals("     ", pad);
    }

    @Test
    public void testRtrimNull() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertNull(thf.rtrim(null));
    }

    @Test
    public void testRtrimEmpty() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertEquals("", thf.rtrim(""));
    }

    @Test
    public void testRtrimTrailingSpaces() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertEquals("hello", thf.rtrim("hello   "));
    }

    @Test
    public void testRtrimNoTrailingSpaces() {
        TestableHelpFormatter thf = new TestableHelpFormatter();
        assertEquals("hello", thf.rtrim("hello"));
    }

    @Test
    public void testOptionComparator() {
        Comparator comp = formatter.getOptionComparator();
        Option opt1 = new Option("a", false, "desc");
        Option opt2 = new Option("B", false, "desc");
        assertTrue(comp.compare(opt1, opt2) < 0);
    }

    @Test
    public void testRenderWrappedTextInfiniteLoopPrevention() {
        formatter.printWrapped(pw, 10, 10, "1234567890");
        String output = sw.toString();
        assertTrue(output.contains("1234567890"));
    }

    @Test
    public void testRenderWrappedTextLongWord() {
        formatter.printWrapped(pw, 5, 0, "abcdefghij");
        String output = sw.toString();
        assertTrue(output.contains("abcde"));
        assertTrue(output.contains("fghij"));
    }

    private static class TestableHelpFormatter extends HelpFormatter {
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
