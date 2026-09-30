package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());
        assertEquals(100, formatter.defaultWidth);

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
        assertEquals(5, formatter.defaultLeftPad);

        formatter.setDescPadding(8);
        assertEquals(8, formatter.getDescPadding());
        assertEquals(8, formatter.defaultDescPad);

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());
        assertEquals("Syntax: ", formatter.defaultSyntaxPrefix);

        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());
        assertEquals("\r\n", formatter.defaultNewLine);

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());
        assertEquals("/", formatter.defaultOptPrefix);

        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());
        assertEquals("//", formatter.defaultLongOptPrefix);

        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());

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
        assertTrue(formatter.getOptionComparator() != customComp);
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
    public void testPrintHelpBasic() {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");
        options.addOption(OptionBuilder.withLongOpt("version").withDescription("print version").create());

        formatter.printHelp(printWriter, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", false);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("Header banner"));
        assertTrue(output.contains("Footer banner"));
        assertTrue(output.contains("-h,--help"));
        assertTrue(output.contains("--version"));
        assertTrue(output.contains("display help"));
    }

    @Test
    public void testPrintHelpWithAutoUsage() {
        Options options = new Options();
        options.addOption("a", "all", false, "do all");
        options.addOption(OptionBuilder.isRequired().withLongOpt("req").withDescription("required option").create("r"));

        formatter.printHelp(printWriter, 80, "myapp", null, options, 1, 3, null, true);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: myapp [-a] -r"));
        assertTrue(output.contains("-a,--all"));
        assertTrue(output.contains("-r,--req"));
    }

    @Test
    public void testPrintHelpHeaderAndFooterEmptyOrWhitespace() {
        Options options = new Options();
        options.addOption("a", "apple", false, "desc a");

        formatter.printHelp(printWriter, 80, "myapp", "   ", options, 1, 3, "", false);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.startsWith("usage: myapp"));
        assertTrue(output.contains("-a,--apple"));
    }

    @Test
    public void testPrintHelpOverloads() {
        Options options = new Options();
        options.addOption("t", false, "test");

        formatter.printHelp(printWriter, 80, "app", "hdr", options, 1, 3, "ftr");
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("usage: app"));

        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "header", options, "footer");
        formatter.printHelp("app", "header", options, "footer", true);
        formatter.printHelp(80, "app", "header", options, "footer");
        formatter.printHelp(80, "app", "header", options, "footer", true);
    }

    @Test
    public void testPrintUsageSimple() {
        formatter.printUsage(printWriter, 80, "myapp arg1 arg2");
        printWriter.flush();
        assertEquals("usage: myapp arg1 arg2" + EOL, stringWriter.toString());

        stringWriter.getBuffer().setLength(0);
        formatter.printUsage(printWriter, 80, "singlecmd");
        printWriter.flush();
        assertEquals("usage: singlecmd" + EOL, stringWriter.toString());
    }

    @Test
    public void testPrintUsageWithOptions() {
        Options options = new Options();

        Option optA = new Option("a", "aaa", true, "Option A");
        optA.setArgName("VAL_A");
        options.addOption(optA);

        Option optB = new Option(null, "bbb", true, "Option B");
        optB.setArgName("VAL_B");
        optB.setRequired(true);
        options.addOption(optB);

        Option optC = new Option("c", false, "Option C");
        optC.setRequired(true);
        options.addOption(optC);

        Option optD = new Option("d", "ddd", true, "Option D");
        optD.setArgName("");
        options.addOption(optD);

        formatter.setLongOptSeparator("=");
        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("usage: app"));
        assertTrue(output.contains("[-a <VAL_A>]"));
        assertTrue(output.contains("--bbb=<VAL_B>"));
        assertTrue(output.contains("-c"));
        assertTrue(output.contains("[-d]"));
    }

    @Test
    public void testPrintUsageWithOptionGroup() {
        Options options = new Options();

        OptionGroup group1 = new OptionGroup();
        group1.setRequired(false);
        Option g1a = new Option("a", "alpha", false, "group1 opt a");
        Option g1b = new Option("b", null, true, "group1 opt b");
        g1b.setArgName("BARG");
        group1.addOption(g1a);
        group1.addOption(g1b);
        options.addOptionGroup(group1);

        OptionGroup group2 = new OptionGroup();
        group2.setRequired(true);
        Option g2a = new Option("x", false, "group2 opt x");
        Option g2b = new Option(null, "yaml", false, "group2 opt yaml");
        group2.addOption(g2a);
        group2.addOption(g2b);
        options.addOptionGroup(group2);

        formatter.printUsage(printWriter, 80, "app", options);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("[-a | -b <BARG>]"));
        assertTrue(output.contains("-x | --yaml"));
    }

    @Test
    public void testRenderOptionsFormatting() {
        Options options = new Options();
        Option optOnlyShort = new Option("s", "short only");
        Option optShortAndLong = new Option("m", "middle", false, "short and long");
        Option optOnlyLong = new Option(null, "longonly", true, "only long option");
        Option optBlankArgName = new Option("k", "key", true, "key option");
        optBlankArgName.setArgName("");
        Option optNullArgName = new Option("v", "val", true, "value option");
        optNullArgName.setArgName(null);
        Option optNoDesc = new Option("n", "nodesc", false, null);

        options.addOption(optOnlyShort);
        options.addOption(optShortAndLong);
        options.addOption(optOnlyLong);
        options.addOption(optBlankArgName);
        options.addOption(optNullArgName);
        options.addOption(optNoDesc);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 4);

        String rendered = sb.toString();
        assertTrue(rendered.contains("  -s"));
        assertTrue(rendered.contains("  -m,--middle"));
        assertTrue(rendered.contains("     --longonly <arg>"));
        assertTrue(rendered.contains("  -k,--key "));
        assertTrue(rendered.contains("  -v,--val <arg>"));
        assertTrue(rendered.contains("  -n,--nodesc"));
    }

    @Test
    public void testRenderWrappedTextSimple() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 20, 4, "Hello World this is a wrap test");
        String[] lines = sb.toString().split(EOL);
        for (String line : lines) {
            assertTrue(line.length() <= 20);
        }
        assertTrue(lines.length > 1);
        assertTrue(lines[1].startsWith("    "));
    }

    @Test
    public void testRenderWrappedTextSingleWordLongerThanWidth() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 8, 2, "Supercalifragilisticexpialidocious");
        String output = sb.toString();
        assertTrue(output.contains("Supercalifragilisticexpialidocious"));
    }

    @Test
    public void testRenderWrappedTextTabStopsExceedingWidth() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 15, "First line is short but then followed by multiple long words to wrap");
        String[] lines = sb.toString().split(EOL);
        assertTrue(lines.length > 1);
        assertTrue(lines[1].startsWith(" "));
    }

    @Test
    public void testRenderWrappedTextPosAtTabStopMinusOne() {
        StringBuffer sb = new StringBuffer();
        String text = "abc def\nghij klmnopqrstuvwxyz";
        formatter.renderWrappedText(sb, 10, 5, text);
        assertNotNull(sb.toString());
    }

    @Test
    public void testPrintWrapped() {
        formatter.printWrapped(printWriter, 40, "Line 1\nLine 2 is somewhat longer and needs wrapping.\nLine 3");
        printWriter.flush();
        String output = stringWriter.toString();
        assertTrue(output.contains("Line 1"));
        assertTrue(output.contains("Line 2"));
        assertTrue(output.contains("Line 3"));

        stringWriter.getBuffer().setLength(0);
        formatter.printWrapped(printWriter, 40, 5, "Line 1\tTabbed text that goes on for a bit");
        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Line 1"));
    }

    @Test
    public void testFindWrapPos() {
        assertEquals(5, formatter.findWrapPos("1234\n67890", 8, 0));
        assertEquals(5, formatter.findWrapPos("1234\t67890", 8, 0));
        assertEquals(-1, formatter.findWrapPos("12345", 10, 0));
        assertEquals(4, formatter.findWrapPos("1234 67890", 8, 0));
        assertEquals(10, formatter.findWrapPos("1234567890 123456", 8, 0));
        assertEquals(-1, formatter.findWrapPos("1234567890123456", 8, 0));
    }

    @Test
    public void testCreatePadding() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
        assertEquals("     ", formatter.createPadding(5));
    }

    @Test
    public void testRtrim() {
        assertNull(formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("abc", formatter.rtrim("abc   \t \n \r "));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("", formatter.rtrim("   \t "));
        assertEquals(" a b c", formatter.rtrim(" a b c  "));
    }

    @Test
    public void testOptionComparator() {
        Comparator comp = formatter.getOptionComparator();
        Option optA = new Option("a", "first");
        Option optB = new Option("b", "second");
        Option optUpperA = new Option("A", "upper first");

        assertTrue(comp.compare(optA, optB) < 0);
        assertTrue(comp.compare(optB, optA) > 0);
        assertEquals(0, comp.compare(optA, optUpperA));

        Option longOpt1 = new Option(null, "alpha", false, "alpha");
        Option longOpt2 = new Option(null, "beta", false, "beta");
        assertTrue(comp.compare(longOpt1, longOpt2) < 0);
    }

    @Test
    public void testPrintOptionsDirectly() {
        Options options = new Options();
        options.addOption("a", "all", false, "all files");
        options.addOption("b", "bytes", true, "number of bytes");

        formatter.printOptions(printWriter, 80, options, 1, 2);
        printWriter.flush();

        String output = stringWriter.toString();
        assertTrue(output.contains("-a,--all"));
        assertTrue(output.contains("-b,--bytes <arg>"));
    }

    @Test
    public void testDefaultWidthLeftPadDescPadValues() {
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_SEPARATOR, formatter.getLongOptSeparator());
    }
}