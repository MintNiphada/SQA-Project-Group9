package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private String defaultNewLine;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        defaultNewLine = formatter.getNewLine();
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

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());
        assertEquals("/", formatter.defaultOptPrefix);

        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());
        assertEquals("//", formatter.defaultLongOptPrefix);

        formatter.setArgName("argument");
        assertEquals("argument", formatter.getArgName());
        assertEquals("argument", formatter.defaultArgName);
    }

    @Test
    public void testRtrim() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("abc", formatter.rtrim("abc\t\n\r"));
        assertEquals("  abc", formatter.rtrim("  abc  "));
        assertEquals("", formatter.rtrim("     "));
        assertEquals("abc", formatter.rtrim("abc"));
    }

    @Test
    public void testCreatePadding() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
        assertEquals("     ", formatter.createPadding(5));
    }

    @Test
    public void testFindWrapPos() {
        // Line ends before max width with newline or tab
        assertEquals(5, formatter.findWrapPos("test\nmore text", 10, 0));
        assertEquals(5, formatter.findWrapPos("test\tmore text", 10, 0));

        // startPos + width >= text.length()
        assertEquals(-1, formatter.findWrapPos("short text", 20, 0));
        assertEquals(-1, formatter.findWrapPos("short text", 10, 0));

        // last whitespace before startPos + width
        String text1 = "the quick brown fox jumps";
        int pos1 = formatter.findWrapPos(text1, 12, 0); // before 12 is 'brown' ending at index 9, space at 9
        assertEquals(9, pos1);

        // break with \r or \n
        String textCarriage = "the quick\rbrown fox";
        assertEquals(9, formatter.findWrapPos(textCarriage, 12, 0));

        // No whitespace before startPos+width, find whitespace after startPos+width
        String longWord = "supercalifragilisticexpialidocious and more";
        int pos2 = formatter.findWrapPos(longWord, 10, 0);
        assertEquals(34, pos2);

        // Entire string is one single word without any whitespace
        String singleWord = "supercalifragilisticexpialidocious";
        int pos3 = formatter.findWrapPos(singleWord, 10, 0);
        assertEquals(-1, pos3);
    }

    @Test
    public void testRenderWrappedTextBasic() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 20, 0, "short text");
        assertEquals("short text", sb.toString());

        sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 2, "12345 67890 12345");
        String expected = "12345" + defaultNewLine + "  67890" + defaultNewLine + "  12345";
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testPrintWrapped() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printWrapped(pw, 20, "this is a test text to wrap");
        pw.flush();
        String expected = "this is a test text" + defaultNewLine + "to wrap" + defaultNewLine;
        assertEquals(expected, sw.toString());

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 20, 4, "this is a test text to wrap");
        pw.flush();
        expected = "this is a test text" + defaultNewLine + "    to wrap" + defaultNewLine;
        assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintUsageSimple() {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 80, "myapp -a -b");
        pw.flush();
        assertEquals("usage: myapp -a -b" + defaultNewLine, sw.toString());
    }

    @Test
    public void testPrintUsageWithOptionsAndOptionGroups() {
        Options options = new Options();
        Option optA = new Option("a", "alpha", false, "Option A");
        Option optB = new Option("b", "beta", true, "Option B");
        optB.setRequired(true);
        optB.setArgName("paramB");
        Option optC = new Option(null, "gamma", false, "Option C");
        Option optD = new Option("d", false, "Option D");
        optD.setArgs(1); // hasArg = true
        optD.setArgName(null); // hasArg() true but argName is null

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);
        options.addOption(optD);

        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        Option optG1 = new Option("x", "xray", false, "Option X");
        Option optG2 = new Option("y", false, "Option Y");
        requiredGroup.addOption(optG1);
        requiredGroup.addOption(optG2);
        options.addOptionGroup(requiredGroup);

        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.setRequired(false);
        Option optG3 = new Option("u", false, "Option U");
        Option optG4 = new Option("v", true, "Option V");
        optG4.setArgName("vVal");
        optionalGroup.addOption(optG3);
        optionalGroup.addOption(optG4);
        options.addOptionGroup(optionalGroup);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printUsage(pw, 120, "app", options);
        pw.flush();

        String usage = sw.toString();
        assertTrue(usage.startsWith("usage: app "));
        assertTrue(usage.contains("[-a]"));
        assertTrue(usage.contains("-b <paramB>"));
        assertTrue(usage.contains("[--gamma]"));
        assertTrue(usage.contains("[-d]"));
        assertTrue(usage.contains("-x | -y"));
        assertTrue(usage.contains("[-u | -v <vVal>]"));
    }

    @Test
    public void testRenderOptions() {
        Options options = new Options();
        Option optA = new Option("a", "all", false, "do not ignore entries starting with .");
        Option optB = new Option("b", "block-size", true, "use SIZE-byte blocks");
        optB.setArgName("SIZE");
        Option optC = new Option(null, "color", false, "colorize the output");
        Option optD = new Option("d", false, "describe directory");
        optD.setArgs(1);
        optD.setArgName(null); // triggers option.hasArg() && !option.hasArgName() branch (append ' ')
        Option optE = new Option("e", "echo", false, null); // description is null

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optC);
        options.addOption(optD);
        options.addOption(optE);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 4);

        String result = sb.toString();
        assertTrue(result.contains("  -a,--all"));
        assertTrue(result.contains("  -b,--block-size <SIZE>"));
        assertTrue(result.contains("     --color"));
        assertTrue(result.contains("  -d "));
        assertTrue(result.contains("  -e,--echo"));
    }

    @Test
    public void testPrintOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "print help message");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 80, options, 1, 3);
        pw.flush();

        String result = sw.toString();
        assertTrue(result.contains("-h,--help   print help message"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        formatter.printHelp(new PrintWriter(new StringWriter()), 80, null, "header", new Options(), 1, 3, "footer");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        formatter.printHelp(new PrintWriter(new StringWriter()), 80, "", "header", new Options(), 1, 3, "footer");
    }

    @Test
    public void testPrintHelpFullFlow() {
        Options options = new Options();
        options.addOption("v", "version", false, "display version");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 80, "myApp", "Header info", options, 2, 4, "Footer info", false);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("usage: myApp"));
        assertTrue(output.contains("Header info"));
        assertTrue(output.contains("  -v,--version    display version"));
        assertTrue(output.contains("Footer info"));

        // Test with autoUsage = true and empty/null header and footer
        sw = new StringWriter();
        pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "myApp", "   ", options, 2, 4, null, true);
        pw.flush();

        output = sw.toString();
        assertTrue(output.contains("usage: myApp [-v]"));
        assertFalse(output.contains("Header info"));
    }

    @Test
    public void testPrintHelpOverloads() {
        Options options = new Options();
        options.addOption("f", "file", true, "input file");

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        formatter.printHelp(pw, 80, "app", "hdr", options, 1, 2, "ftr");
        pw.flush();
        assertTrue(sw.toString().contains("usage: app"));

        // Test methods that write to System.out by redirecting System.out temporarily
        PrintStream originalOut = System.out;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));

            formatter.printHelp("app", options);
            formatter.printHelp("app", options, true);
            formatter.printHelp("app", "header", options, "footer");
            formatter.printHelp("app", "header", options, "footer", true);
            formatter.printHelp(80, "app", "header", options, "footer");
            formatter.printHelp(80, "app", "header", options, "footer", true);

            String sysOutContent = baos.toString();
            assertTrue(sysOutContent.contains("usage: app"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testOptionComparatorCoverage() {
        Options options = new Options();
        Option optA = new Option("a", "alpha", false, "Option A");
        Option optB = new Option("B", "Beta", false, "Option B");
        Option optZ = new Option(null, "zeta", false, "Option Zeta");
        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optZ);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printOptions(pw, 80, options, 1, 2);
        pw.flush();

        String result = sw.toString();
        int idxA = result.indexOf("-a");
        int idxB = result.indexOf("-B");
        int idxZ = result.indexOf("--zeta");
        assertTrue(idxA != -1 && idxB != -1 && idxZ != -1);
        assertTrue(idxA < idxB);
        assertTrue(idxB < idxZ);
    }
}