package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    @Test
    public void testGettersAndSetters() {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());
        assertEquals(100, formatter.defaultWidth);

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
        assertEquals(5, formatter.defaultLeftPad);

        formatter.setDescPadding(7);
        assertEquals(7, formatter.getDescPadding());
        assertEquals(7, formatter.defaultDescPad);

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());
        assertEquals("Syntax: ", formatter.defaultSyntaxPrefix);

        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());
        assertEquals("\r\n", formatter.defaultNewLine);

        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());
        assertEquals("+", formatter.defaultOptPrefix);

        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());
        assertEquals("++", formatter.defaultLongOptPrefix);

        formatter.setArgName("value");
        assertEquals("value", formatter.getArgName());
        assertEquals("value", formatter.defaultArgName);
    }

    @Test
    public void testOptionComparator() {
        assertNotNull(formatter.getOptionComparator());

        Comparator customComp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };

        formatter.setOptionComparator(customComp);
        assertSame(customComp, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());

        Option optA = new Option("a", "Alpha option");
        Option optB = new Option("b", "Beta option");
        Option optC = new Option(null, "charlie", false, "Charlie option");

        Comparator comparator = formatter.getOptionComparator();
        assertTrue(comparator.compare(optA, optB) < 0);
        assertTrue(comparator.compare(optB, optA) > 0);
        assertEquals(0, comparator.compare(optA, optA));
        assertTrue(comparator.compare(optA, optC) < 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        formatter.printHelp(printWriter, 80, null, "Header", new Options(), 1, 3, "Footer");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        formatter.printHelp(printWriter, 80, "", "Header", new Options(), 1, 3, "Footer");
    }

    @Test
    public void testPrintHelpBasic() {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");
        options.addOption("v", "version", false, "display version");

        formatter.printHelp(printWriter, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", false);
        printWriter.flush();
        String output = stringWriter.toString();

        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("Header banner"));
        assertTrue(output.contains("-h,--help"));
        assertTrue(output.contains("-v,--version"));
        assertTrue(output.contains("display help"));
        assertTrue(output.contains("display version"));
        assertTrue(output.contains("Footer banner"));
    }

    @Test
    public void testPrintHelpWithAutoUsage() {
        Options options = new Options();
        Option optFile = new Option("f", "file", true, "input file");
        optFile.setRequired(true);
        optFile.setArgName("FILE");
        options.addOption(optFile);

        formatter.printHelp(printWriter, 80, "myapp", null, options, 2, 4, null, true);
        printWriter.flush();
        String output = stringWriter.toString();

        assertTrue(output.contains("usage: myapp -f <FILE>"));
    }

    @Test
    public void testPrintHelpSystemOutOverloads() {
        Options options = new Options();
        options.addOption("a", "alpha", false, "alpha test");

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(80, "app", "header", options, "footer");
        formatter.printHelp(80, "app", "header", options, "footer", true);
    }

    @Test
    public void testPrintUsageSimple() {
        formatter.printUsage(printWriter, 80, "myapp <arg1> <arg2>");
        printWriter.flush();
        String output = stringWriter.toString();
        assertEquals("usage: myapp <arg1> <arg2>" + formatter.getNewLine(), output);
    }

    @Test
    public void testPrintUsageWithOptionsAndOptionGroup() {
        Options options = new Options();
        Option optA = new Option("a", "apple", false, "apple desc");
        Option optB = new Option("b", "banana", true, "banana desc");
        optB.setRequired(true);
        optB.setArgName("QTY");
        Option optLongOnly = new Option(null, "cherry", false, "cherry desc");

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optLongOnly);

        OptionGroup group = new OptionGroup();
        Option optX = new Option("x", "x-ray", false, "x-ray desc");
        Option optY = new Option("y", "yellow", true, "yellow desc");
        optY.setArgName("COLOR");
        group.addOption(optX);
        group.addOption(optY);
        group.setRequired(false);

        options.addOptionGroup(group);

        OptionGroup reqGroup = new OptionGroup();
        Option opt1 = new Option("1", "one", false, "one desc");
        Option opt2 = new Option("2", "two", false, "two desc");
        reqGroup.addOption(opt1);
        reqGroup.addOption(opt2);
        reqGroup.setRequired(true);

        options.addOptionGroup(reqGroup);

        formatter.printUsage(printWriter, 80, "myapp", options);
        printWriter.flush();
        String output = stringWriter.toString();

        assertTrue(output.startsWith("usage: myapp"));
        assertTrue(output.contains("[-a]"));
        assertTrue(output.contains("-b <QTY>"));
        assertTrue(output.contains("[--cherry]"));
        assertTrue(output.contains("[-x | -y <COLOR>]"));
        assertTrue(output.contains("-1 | -2"));
    }

    @Test
    public void testRenderOptionsFormatting() {
        Options options = new Options();
        Option shortOnly = new Option("s", false, "short only description");
        Option longOnly = new Option(null, "long-only", true, "long only with arg");
        longOnly.setArgName("VALUE");
        Option both = new Option("b", "both", true, "both options");
        both.setArgName(null);
        Option noDesc = new Option("n", "nodesc", false, null);

        options.addOption(shortOnly);
        options.addOption(longOnly);
        options.addOption(both);
        options.addOption(noDesc);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 4);
        String rendered = sb.toString();

        assertTrue(rendered.contains("  -s"));
        assertTrue(rendered.contains("     --long-only <VALUE>"));
        assertTrue(rendered.contains("  -b,--both"));
        assertTrue(rendered.contains("  -n,--nodesc"));
        assertTrue(rendered.contains("short only description"));
        assertTrue(rendered.contains("long only with arg"));
    }

    @Test
    public void testPrintWrapped() {
        formatter.printWrapped(printWriter, 20, "This is a long text that definitely needs to be wrapped multiple times to verify correctly.");
        printWriter.flush();
        String[] lines = stringWriter.toString().split(System.getProperty("line.separator"));
        for (String line : lines) {
            assertTrue(line.length() <= 20);
        }
    }

    @Test
    public void testPrintWrappedWithTabAndNewline() {
        String text = "First line\nSecond\tline with tab and further long content that exceeds line width";
        formatter.printWrapped(printWriter, 25, 4, text);
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("First line"));
        assertTrue(output.contains("Second"));
    }

    @Test(expected = IllegalStateException.class)
    public void testRenderWrappedTextThrowsExceptionWhenTabStopTooLarge() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 10, "This text will wrap and throw exception due to tab stop width");
    }

    @Test
    public void testRenderWrappedTextEdgeBreakCondition() {
        StringBuffer sb = new StringBuffer();
        String longWord = "12345678901234567890";
        formatter.renderWrappedText(sb, 10, 3, "abc " + longWord);
        String result = sb.toString();
        assertTrue(result.contains("abc"));
        assertTrue(result.contains("12345678901234567890"));
    }

    @Test
    public void testFindWrapPos() {
        String text = "hello world from java junit test";
        assertEquals(5, formatter.findWrapPos(text, 10, 0));
        assertEquals(-1, formatter.findWrapPos(text, 50, 0));

        String textWithNewline = "hello\nworld";
        assertEquals(6, formatter.findWrapPos(textWithNewline, 10, 0));

        String textWithTab = "hello\tworld";
        assertEquals(6, formatter.findWrapPos(textWithTab, 10, 0));

        String noSpace = "veryverylongsinglewordwithnospacesatall";
        assertEquals(10, formatter.findWrapPos(noSpace, 10, 0));

        String trailingWord = "word " + noSpace;
        assertEquals(4, formatter.findWrapPos(trailingWord, 10, 0));
    }

    @Test
    public void testCreatePadding() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
        assertEquals("     ", formatter.createPadding(5));
    }

    @Test
    public void testRtrim() {
        assertEquals(null, formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("", formatter.rtrim("   \t\n\r"));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("  abc", formatter.rtrim("  abc   \t"));
    }

    @Test
    public void testPrintOptionsDirectly() {
        Options options = new Options();
        options.addOption("o", "option", false, "desc");
        formatter.printOptions(printWriter, 80, options, 1, 3);
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("-o,--option"));
    }
}