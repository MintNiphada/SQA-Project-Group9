package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());
        assertEquals("\n", formatter.defaultNewLine);

        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());
        assertEquals("+", formatter.defaultOptPrefix);

        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());
        assertEquals("++", formatter.defaultLongOptPrefix);

        formatter.setArgName("parameter");
        assertEquals("parameter", formatter.getArgName());
        assertEquals("parameter", formatter.defaultArgName);

        Comparator defaultComp = formatter.getOptionComparator();
        assertNotNull(defaultComp);

        Comparator customComp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(customComp);
        assertSame(customComp, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    @Test
    public void testOptionComparatorSorting() {
        Comparator comp = formatter.getOptionComparator();
        Option o1 = new Option("a", "alpha", false, "desc1");
        Option o2 = new Option("B", "beta", false, "desc2");
        Option o3 = new Option("b", "bravo", false, "desc3");

        assertTrue(comp.compare(o1, o2) < 0);
        assertTrue(comp.compare(o2, o1) > 0);
        assertEquals(0, comp.compare(o2, o3));
    }

    @Test
    public void testRtrim() {
        assertEquals(null, formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("abc", formatter.rtrim("abc   \t\n\r"));
        assertEquals("   abc", formatter.rtrim("   abc   "));
        assertEquals("", formatter.rtrim("   \t  "));
    }

    @Test
    public void testCreatePadding() {
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("   ", formatter.createPadding(3));
    }

    @Test
    public void testFindWrapPos() {
        // Line ends before width with newline
        String textWithNewline = "abc\ndef";
        assertEquals(4, formatter.findWrapPos(textWithNewline, 10, 0));

        // Line ends before width with tab
        String textWithTab = "abc\tdef";
        assertEquals(4, formatter.findWrapPos(textWithTab, 10, 0));

        // startPos + width >= text.length()
        assertEquals(-1, formatter.findWrapPos("short", 10, 0));
        assertEquals(-1, formatter.findWrapPos("short", 5, 0));

        // Break on space before startPos + width
        String longText = "the quick brown fox";
        int pos = formatter.findWrapPos(longText, 12, 0);
        assertEquals(9, pos); // after 'brown' -> index of space before 'fox' is 9

        // Break when word exceeds width (look forward for next space)
        String unspacedWord = "supercalifragilisticexpialidocious and more";
        int forwardPos = formatter.findWrapPos(unspacedWord, 10, 0);
        assertEquals(34, forwardPos);

        // Entire string has no spaces and exceeds width
        String noSpaces = "supercalifragilisticexpialidocious";
        assertEquals(-1, formatter.findWrapPos(noSpaces, 10, 0));

        // Test with carriage return as delimiter
        String textWithCr = "abc\rdef";
        assertEquals(4, formatter.findWrapPos(textWithCr, 3, 0));
    }

    @Test
    public void testRenderWrappedText() {
        formatter.setNewLine("\n");
        StringBuffer sb = new StringBuffer();

        // Single line no wrap
        formatter.renderWrappedText(sb, 20, 4, "short text");
        assertEquals("short text", sb.toString());

        // Multi-line wrap
        sb = new StringBuffer();
        formatter.renderWrappedText(sb, 15, 2, "This is a long text that needs wrapping");
        String expected = "This is a long\n  text that\n  needs\n  wrapping";
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testPrintWrapped() {
        formatter.printWrapped(printWriter, 20, "Wrapped text here");
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Wrapped text here"));

        stringWriter.getBuffer().setLength(0);
        formatter.printWrapped(printWriter, 20, 4, "Wrapped text with next line tab stop");
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Wrapped text"));
    }

    @Test
    public void testPrintUsageSimple() {
        formatter.printUsage(printWriter, 80, "appname --arg1 -b");
        printWriter.flush();
        assertEquals("usage: appname --arg1 -b" + formatter.getNewLine(), stringWriter.toString());
    }

    @Test
    public void testPrintUsageWithOptions() {
        Options options = new Options();
        Option optA = new Option("a", "alpha", false, "Alpha description");
        Option optB = new Option("b", "beta", true, "Beta description");
        optB.setRequired(true);
        optB.setArgName("value");

        Option optC = new Option(null, "gamma", false, "Gamma description");

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);

        formatter.printUsage(printWriter, 80, "myApp", options);
        printWriter.flush();
        String usage = stringWriter.toString();
        assertTrue(usage.startsWith("usage: myApp"));
        assertTrue(usage.contains("[-a]"));
        assertTrue(usage.contains("-b <value>"));
        assertTrue(usage.contains("[--gamma]"));
    }

    @Test
    public void testPrintUsageWithOptionGroup() {
        Options options = new Options();

        OptionGroup optionalGroup = new OptionGroup();
        Option o1 = new Option("x", "Option X");
        Option o2 = new Option("y", "Option Y");
        optionalGroup.addOption(o1);
        optionalGroup.addOption(o2);
        options.addOptionGroup(optionalGroup);

        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        Option o3 = new Option("m", "Option M");
        Option o4 = new Option(null, "optN", false, "Option N");
        requiredGroup.addOption(o3);
        requiredGroup.addOption(o4);
        options.addOptionGroup(requiredGroup);

        formatter.printUsage(printWriter, 80, "testApp", options);
        printWriter.flush();
        String usage = stringWriter.toString();
        assertTrue(usage.contains("[-x | -y]"));
        assertTrue(usage.contains("-m | --optN"));
    }

    @Test
    public void testRenderOptions() {
        Options options = new Options();
        Option opt1 = new Option("a", "all", false, "Description for all");
        Option opt2 = new Option(null, "long-only", true, "Description for long only");
        opt2.setArgName("param");
        Option opt3 = new Option("c", null, false, null);
        Option opt4 = new Option("d", "dir", true, "No arg name option");
        opt4.setArgName(null);

        options.addOption(opt1);
        options.addOption(opt2);
        options.addOption(opt3);
        options.addOption(opt4);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 4);
        String result = sb.toString();

        assertTrue(result.contains("  -a,--all"));
        assertTrue(result.contains("  --long-only <param>"));
        assertTrue(result.contains("  -c"));
        assertTrue(result.contains("  -d,--dir"));
    }

    @Test
    public void testPrintOptions() {
        Options options = new Options();
        options.addOption("f", "file", true, "Input file to process");
        options.addOption("h", "help", false, "Print this help message");

        formatter.printOptions(printWriter, 80, options, 1, 3);
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("-f,--file"));
        assertTrue(output.contains("-h,--help"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullSyntax() {
        formatter.printHelp(printWriter, 80, null, "Header", new Options(), 1, 3, "Footer", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptySyntax() {
        formatter.printHelp(printWriter, 80, "", "Header", new Options(), 1, 3, "Footer", true);
    }

    @Test
    public void testPrintHelpFullFlow() {
        Options options = new Options();
        options.addOption("v", "version", false, "Display version");

        // With header, footer, autoUsage = true
        formatter.printHelp(printWriter, 80, "app", "=== Header ===", options, 2, 4, "=== Footer ===", true);
        printWriter.flush();
        String output = stringWriter.toString();

        assertTrue(output.contains("usage: app"));
        assertTrue(output.contains("=== Header ==="));
        assertTrue(output.contains("-v,--version"));
        assertTrue(output.contains("=== Footer ==="));

        // With autoUsage = false, empty header and null footer
        stringWriter.getBuffer().setLength(0);
        formatter.printHelp(printWriter, 80, "app", "   ", options, 2, 4, null, false);
        printWriter.flush();
        output = stringWriter.toString();
        assertTrue(output.startsWith("usage: app"));
        assertTrue(!output.contains("=== Footer ==="));
    }

    @Test
    public void testPrintHelpConvenienceMethods() {
        Options options = new Options();
        options.addOption("o", "output", true, "Output file");

        // printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer)
        formatter.printHelp(printWriter, 80, "cmd", "header", options, 1, 2, "footer");
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("usage: cmd"));

        // Capture System.out for stdout overloads
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));

            formatter.printHelp("syntax", options);
            formatter.printHelp("syntax", options, true);
            formatter.printHelp("syntax", "header", options, "footer");
            formatter.printHelp("syntax", "header", options, "footer", true);
            formatter.printHelp(80, "syntax", "header", options, "footer");
            formatter.printHelp(80, "syntax", "header", options, "footer", true);

            String sysOut = baos.toString();
            assertTrue(sysOut.contains("usage: syntax"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testWrapLongOptionDescription() {
        Options options = new Options();
        String longDesc = "This is a very long description intended to test whether line wrapping works properly across multiple lines when rendering options.";
        options.addOption("d", "detailed-option", false, longDesc);

        formatter.setNewLine("\n");
        formatter.printOptions(printWriter, 40, options, 1, 2);
        printWriter.flush();
        String output = stringWriter.toString();
        String[] lines = output.split("\n");
        assertTrue(lines.length > 1);
        for (String line : lines) {
            assertTrue(line.length() <= 40);
        }
    }

    @Test
    public void testOptionSortingWithCustomComparator() {
        Options options = new Options();
        options.addOption("z", "zebra", false, "Zebra option");
        options.addOption("a", "apple", false, "Apple option");

        // Custom comparator: Reverse alphabetical
        formatter.setOptionComparator(new Comparator() {
            public int compare(Object o1, Object o2) {
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt2.getKey().compareToIgnoreCase(opt1.getKey());
            }
        });

        formatter.printOptions(printWriter, 80, options, 1, 2);
        printWriter.flush();
        String output = stringWriter.toString();
        int zPos = output.indexOf("-z");
        int aPos = output.indexOf("-a");
        assertTrue(zPos >= 0 && aPos >= 0);
        assertTrue(zPos < aPos);
    }
}