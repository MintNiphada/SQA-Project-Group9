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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HelpFormatterTest {

    private HelpFormatter formatter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private final String EOL = System.getProperty("line.separator");

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Test
    public void testGettersAndSetters() {
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        formatter.setDescPadding(8);
        assertEquals(8, formatter.getDescPadding());

        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        formatter.setSyntaxPrefix("Usage: ");
        assertEquals("Usage: ", formatter.getSyntaxPrefix());

        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());

        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());

        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        formatter.setArgName("value");
        assertEquals("value", formatter.getArgName());

        Comparator originalComparator = formatter.getOptionComparator();
        assertNotNull(originalComparator);

        Comparator customComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(customComparator);
        assertEquals(customComparator, formatter.getOptionComparator());

        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntax() {
        formatter.printHelp(printWriter, 80, null, "header", new Options(), 2, 2, "footer", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntax() {
        formatter.printHelp(printWriter, 80, "", "header", new Options(), 2, 2, "footer", true);
    }

    @Test
    public void testPrintHelpSimple() {
        Options options = new Options();
        options.addOption("a", "all", false, "do not ignore entries starting with .");
        options.addOption("b", false, "description for b");

        formatter.printHelp(printWriter, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", true);
        printWriter.flush();
        String output = stringWriter.toString();

        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("Header banner"));
        assertTrue(output.contains("-a,--all"));
        assertTrue(output.contains("-b"));
        assertTrue(output.contains("Footer banner"));
    }

    @Test
    public void testPrintHelpWithoutAutoUsage() {
        Options options = new Options();
        options.addOption("h", "help", false, "print help");

        formatter.printHelp(printWriter, 80, "myapp --opt", null, options, 1, 3, null, false);
        printWriter.flush();
        String output = stringWriter.toString();

        assertTrue(output.startsWith("usage: myapp --opt"));
        assertTrue(output.contains("-h,--help"));
    }

    @Test
    public void testPrintHelpOverloads() {
        Options options = new Options();
        options.addOption("v", "version", false, "display version");

        formatter.printHelp(printWriter, 80, "app", "hdr", options, 1, 2, "ftr");
        printWriter.flush();
        String out1 = stringWriter.toString();
        assertTrue(out1.contains("usage: app"));
        assertTrue(out1.contains("-v,--version"));

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(baos));

            formatter.printHelp("app", options);
            formatter.printHelp("app", options, true);
            formatter.printHelp("app", "header", options, "footer");
            formatter.printHelp("app", "header", options, "footer", true);
            formatter.printHelp(80, "app", "header", options, "footer");
            formatter.printHelp(80, "app", "header", options, "footer", true);

            String consoleOutput = baos.toString();
            assertTrue(consoleOutput.contains("usage: app"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPrintUsageWithOptionGroups() {
        Options options = new Options();

        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        requiredGroup.addOption(new Option("r1", "req1", false, "required option 1"));
        requiredGroup.addOption(new Option("r2", "req2", true, "required option 2"));
        options.addOptionGroup(requiredGroup);

        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.setRequired(false);
        Option optGroupLongOnly = new Option(null, "longOnlyInGroup", true, "desc");
        optionalGroup.addOption(optGroupLongOnly);
        optionalGroup.addOption(new Option("o2", false, "desc"));
        options.addOptionGroup(optionalGroup);

        Option normalRequired = new Option("n", true, "normal required");
        normalRequired.setRequired(true);
        normalRequired.setArgName("num");
        options.addOption(normalRequired);

        Option normalOptionalLongOnly = new Option(null, "verbose", false, "verbose flag");
        options.addOption(normalOptionalLongOnly);

        formatter.printUsage(printWriter, 80, "complexApp", options);
        printWriter.flush();
        String output = stringWriter.toString();

        assertTrue(output.contains("usage: complexApp"));
        assertTrue(output.contains("-r1 | -r2 <arg>"));
        assertTrue(output.contains("[--longOnlyInGroup <arg> | -o2]"));
        assertTrue(output.contains("-n <num>"));
        assertTrue(output.contains("[--verbose]"));
    }

    @Test
    public void testPrintUsageSimple() {
        formatter.printUsage(printWriter, 80, "simpleApp [options] <files>");
        printWriter.flush();
        String output = stringWriter.toString();

        assertEquals("usage: simpleApp [options] <files>" + EOL, output);
    }

    @Test
    public void testPrintOptions() {
        Options options = new Options();
        Option longOnly = new Option(null, "longOnly", false, "only long opt");
        Option shortAndLong = new Option("s", "shortAndLong", true, "short and long opt with arg");
        shortAndLong.setArgName("file");
        Option shortOnly = new Option("x", "short only without long opt");
        Option optionNoArgName = new Option("z", true, "has arg without argName");
        optionNoArgName.setArgName(null);
        Option optionNoDesc = new Option("n", "no desc");
        optionNoDesc.setDescription(null);

        options.addOption(longOnly);
        options.addOption(shortAndLong);
        options.addOption(shortOnly);
        options.addOption(optionNoArgName);
        options.addOption(optionNoDesc);

        formatter.printOptions(printWriter, 80, options, 2, 4);
        printWriter.flush();
        String output = stringWriter.toString();

        assertTrue(output.contains("--longOnly"));
        assertTrue(output.contains("-s,--shortAndLong <file>"));
        assertTrue(output.contains("-x"));
        assertTrue(output.contains("-z"));
    }

    @Test
    public void testPrintWrapped() {
        formatter.printWrapped(printWriter, 20, "This is a short line that will be wrapped into multiple lines.");
        printWriter.flush();
        String output = stringWriter.toString();

        String[] lines = output.split(EOL);
        assertTrue(lines.length > 1);
        for (String line : lines) {
            assertTrue(line.length() <= 20);
        }
    }

    @Test
    public void testPrintWrappedWithIndent() {
        formatter.printWrapped(printWriter, 30, 4, "A start line with text that wraps onto another line nicely with tabstop.");
        printWriter.flush();
        String output = stringWriter.toString();

        String[] lines = output.split(EOL);
        assertTrue(lines.length > 1);
        assertTrue(lines[1].startsWith("    "));
    }

    @Test
    public void testFindWrapPos() {
        String text = "12345 67890 12345";
        int pos = formatter.findWrapPos(text, 8, 0);
        assertEquals(5, pos);

        text = "12345\n67890";
        pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(6, pos);

        text = "12345\t67890";
        pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(6, pos);

        text = "12345";
        pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(-1, pos);

        text = "123456789012345 67890";
        pos = formatter.findWrapPos(text, 5, 0);
        assertEquals(15, pos);

        text = "12345678901234567890";
        pos = formatter.findWrapPos(text, 5, 0);
        assertEquals(-1, pos);
    }

    @Test
    public void testCreatePadding() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
        assertEquals("      ", formatter.createPadding(6));
    }

    @Test
    public void testRtrim() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("text", formatter.rtrim("text   \t \n"));
        assertEquals("  text", formatter.rtrim("  text "));
        assertEquals("text", formatter.rtrim("text"));
    }

    @Test
    public void testRenderWrappedTextInfiniteLoopProtection() {
        StringBuffer sb = new StringBuffer();
        try {
            formatter.renderWrappedText(sb, 5, 5, "1234567890 12345");
            fail("Expected RuntimeException for infinite loop protection");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("CLI-162"));
        }
    }

    @Test
    public void testOptionComparator() {
        Comparator comparator = formatter.getOptionComparator();

        Option optA = new Option("a", "first");
        Option optB = new Option("b", "second");
        Option optAUpper = new Option("A", "first upper");

        assertTrue(comparator.compare(optA, optB) < 0);
        assertTrue(comparator.compare(optB, optA) > 0);
        assertEquals(0, comparator.compare(optA, optAUpper));

        Option longOptOnly1 = new Option(null, "alpha", false, "desc");
        Option longOptOnly2 = new Option(null, "beta", false, "desc");
        assertTrue(comparator.compare(longOptOnly1, longOptOnly2) < 0);
    }
}