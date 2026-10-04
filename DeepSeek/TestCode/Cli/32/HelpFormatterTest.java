package org.apache.commons.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Comparator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HelpFormatterTest {

    private HelpFormatter formatter;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        formatter.setNewLine("\n");
    }

    @Test
    public void testDefaultConstructor() {
        Assert.assertEquals(74, formatter.getWidth());
        Assert.assertEquals(1, formatter.getLeftPadding());
        Assert.assertEquals(3, formatter.getDescPadding());
        Assert.assertEquals("usage: ", formatter.getSyntaxPrefix());
        Assert.assertEquals("\n", formatter.getNewLine());
        Assert.assertEquals("-", formatter.getOptPrefix());
        Assert.assertEquals("--", formatter.getLongOptPrefix());
        Assert.assertEquals(" ", formatter.getLongOptSeparator());
        Assert.assertEquals("arg", formatter.getArgName());
        Assert.assertNotNull(formatter.getOptionComparator());
    }

    @Test
    public void testSetGetWidth() {
        formatter.setWidth(100);
        Assert.assertEquals(100, formatter.getWidth());
    }

    @Test
    public void testSetGetLeftPadding() {
        formatter.setLeftPadding(5);
        Assert.assertEquals(5, formatter.getLeftPadding());
    }

    @Test
    public void testSetGetDescPadding() {
        formatter.setDescPadding(7);
        Assert.assertEquals(7, formatter.getDescPadding());
    }

    @Test
    public void testSetGetSyntaxPrefix() {
        formatter.setSyntaxPrefix("cmd: ");
        Assert.assertEquals("cmd: ", formatter.getSyntaxPrefix());
    }

    @Test
    public void testSetGetNewLine() {
        formatter.setNewLine("\r\n");
        Assert.assertEquals("\r\n", formatter.getNewLine());
    }

    @Test
    public void testSetGetOptPrefix() {
        formatter.setOptPrefix("+");
        Assert.assertEquals("+", formatter.getOptPrefix());
    }

    @Test
    public void testSetGetLongOptPrefix() {
        formatter.setLongOptPrefix("---");
        Assert.assertEquals("---", formatter.getLongOptPrefix());
    }

    @Test
    public void testSetGetLongOptSeparator() {
        formatter.setLongOptSeparator("=");
        Assert.assertEquals("=", formatter.getLongOptSeparator());
    }

    @Test
    public void testSetGetArgName() {
        formatter.setArgName("filename");
        Assert.assertEquals("filename", formatter.getArgName());
    }

    @Test
    public void testGetOptionComparatorDefault() {
        Comparator comp = formatter.getOptionComparator();
        Assert.assertNotNull(comp);
        Assert.assertTrue(comp instanceof HelpFormatter.OptionComparator);
    }

    @Test
    public void testSetOptionComparatorNull() {
        formatter.setOptionComparator(null);
        Comparator comp = formatter.getOptionComparator();
        Assert.assertNotNull(comp);
        Assert.assertTrue(comp instanceof HelpFormatter.OptionComparator);
    }

    @Test
    public void testSetOptionComparatorCustom() {
        Comparator custom = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(custom);
        Assert.assertSame(custom, formatter.getOptionComparator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        formatter.printHelp((String) null, new Options());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        formatter.printHelp("", new Options());
    }

    @Test
    public void testPrintHelpAutoUsageFalse() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", false, "description");
        formatter.printHelp(pw, 80, "app", "header", options, 1, 3, "footer", false);
        pw.flush();
        String output = sw.toString();
        Assert.assertTrue(output.contains("usage: app"));
        Assert.assertTrue(output.contains("header"));
        Assert.assertTrue(output.contains("-a"));
        Assert.assertTrue(output.contains("description"));
        Assert.assertTrue(output.contains("footer"));
    }

    @Test
    public void testPrintHelpAutoUsageTrue() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", false, "description");
        formatter.printHelp(pw, 80, "app", "header", options, 1, 3, "footer", true);
        pw.flush();
        String output = sw.toString();
        Assert.assertTrue(output.contains("usage: app"));
        Assert.assertTrue(output.contains("-a"));
    }

    @Test
    public void testPrintHelpHeaderFooterEmpty() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", false, "desc");
        formatter.printHelp(pw, 80, "app", "", options, 1, 3, "   ", false);
        pw.flush();
        String output = sw.toString();
        Assert.assertFalse(output.contains("header"));
        Assert.assertFalse(output.contains("footer"));
    }

    @Test(expected = NullPointerException.class)
    public void testPrintHelpOptionsNull() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "app", "header", null, 1, 3, "footer", false);
    }

    @Test
    public void testPrintUsageWithOptions() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", false, "desc");
        options.addOption("b", true, "desc2");
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("c", "ccc", false, "group desc"));
        group.addOption(new Option("d", "ddd", false, "group desc2"));
        options.addOptionGroup(group);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        String output = sw.toString();
        Assert.assertTrue(output.contains("usage: app"));
        Assert.assertTrue(output.contains("-a"));
        Assert.assertTrue(output.contains("-b <arg>"));
        Assert.assertTrue(output.contains("[-c | -d]"));
    }

    @Test
    public void testPrintUsageSimple() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "app");
        pw.flush();
        String output = sw.toString();
        Assert.assertEquals("usage: app\n", output);
    }

    @Test
    public void testPrintOptions() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha description");
        options.addOption("b", "beta", true, "Beta description");
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();
        String output = sw.toString();
        Assert.assertTrue(output.contains("-a,--alpha"));
        Assert.assertTrue(output.contains("-b,--beta <arg>"));
        Assert.assertTrue(output.contains("Alpha description"));
        Assert.assertTrue(output.contains("Beta description"));
    }

    @Test
    public void testRenderOptions() {
        StringBuffer sb = new StringBuffer();
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha description");
        options.addOption("b", "beta", true, "Beta description");
        formatter.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        Assert.assertTrue(output.contains("-a,--alpha"));
        Assert.assertTrue(output.contains("-b,--beta <arg>"));
    }

    @Test
    public void testRenderOptionsNoShortOpt() {
        StringBuffer sb = new StringBuffer();
        Options options = new Options();
        options.addOption(new Option(null, "alpha", false, "desc"));
        formatter.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        Assert.assertTrue(output.contains("   --alpha"));
    }

    @Test
    public void testRenderOptionsBlankArgName() {
        StringBuffer sb = new StringBuffer();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "desc");
        opt.setArgName("");
        options.addOption(opt);
        formatter.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        Assert.assertTrue(output.contains("-a,--alpha "));
        Assert.assertFalse(output.contains("<"));
    }

    @Test
    public void testRenderOptionsNullArgName() {
        StringBuffer sb = new StringBuffer();
        Options options = new Options();
        Option opt = new Option("a", "alpha", true, "desc");
        opt.setArgName(null);
        options.addOption(opt);
        formatter.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        Assert.assertTrue(output.contains("-a,--alpha <arg>"));
    }

    @Test
    public void testRenderOptionsNoDescription() {
        StringBuffer sb = new StringBuffer();
        Options options = new Options();
        options.addOption("a", false, (String) null);
        formatter.renderOptions(sb, 80, options, 1, 3);
        String output = sb.toString();
        Assert.assertTrue(output.contains("-a"));
        Assert.assertFalse(output.contains("null"));
    }

    @Test
    public void testRenderWrappedTextSimple() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 0, "short");
        Assert.assertEquals("short", sb.toString());
    }

    @Test
    public void testRenderWrappedTextLong() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 0, "This is a long text that should wrap");
        Assert.assertTrue(sb.toString().contains("\n"));
    }

    @Test
    public void testRenderWrappedTextNextLineTabStopGreaterThanWidth() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 15, "This is a long text that should wrap");
        Assert.assertTrue(sb.toString().contains("\n"));
    }

    @Test
    public void testRenderWrappedTextPosEqualsNextLineTabStopMinusOne() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 5, "1234567890abc");
        Assert.assertTrue(sb.toString().contains("\n"));
    }

    @Test
    public void testFindWrapPosNewLineBeforeWidth() {
        int pos = formatter.findWrapPos("hello\nworld", 10, 0);
        Assert.assertEquals(6, pos);
    }

    @Test
    public void testFindWrapPosTabBeforeWidth() {
        int pos = formatter.findWrapPos("hello\tworld", 10, 0);
        Assert.assertEquals(6, pos);
    }

    @Test
    public void testFindWrapPosEndOfText() {
        int pos = formatter.findWrapPos("short", 10, 0);
        Assert.assertEquals(-1, pos);
    }

    @Test
    public void testFindWrapPosWhitespace() {
        int pos = formatter.findWrapPos("hello world", 10, 0);
        Assert.assertEquals(5, pos);
    }

    @Test
    public void testFindWrapPosNoWhitespace() {
        int pos = formatter.findWrapPos("helloworld", 5, 0);
        Assert.assertEquals(5, pos);
    }

    @Test
    public void testFindWrapPosNoWhitespaceAtEnd() {
        int pos = formatter.findWrapPos("helloworld", 5, 0);
        Assert.assertEquals(5, pos);
    }

    @Test
    public void testCreatePadding() {
        String pad = formatter.createPadding(5);
        Assert.assertEquals("     ", pad);
    }

    @Test
    public void testRtrimNull() {
        Assert.assertNull(formatter.rtrim(null));
    }

    @Test
    public void testRtrimEmpty() {
        Assert.assertEquals("", formatter.rtrim(""));
    }

    @Test
    public void testRtrimTrailingSpaces() {
        Assert.assertEquals("hello", formatter.rtrim("hello   "));
    }

    @Test
    public void testOptionComparator() throws Exception {
        Constructor<?> ctor = HelpFormatter.OptionComparator.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Comparator comp = (Comparator) ctor.newInstance();
        Option opt1 = new Option("a", "alpha", false, "desc");
        Option opt2 = new Option("b", "beta", false, "desc");
        Assert.assertTrue(comp.compare(opt1, opt2) < 0);
        Assert.assertTrue(comp.compare(opt2, opt1) > 0);
        Assert.assertEquals(0, comp.compare(opt1, opt1));
    }

    @Test
    public void testPrintHelpSystemOut() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        System.setOut(new java.io.PrintStream(baos));
        try {
            formatter.printHelp("app", new Options());
        } finally {
            System.setOut(System.out);
        }
        Assert.assertTrue(baos.toString().contains("usage: app"));
    }
}
