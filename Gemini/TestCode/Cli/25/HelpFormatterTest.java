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

/**
 * Test suite for {@link HelpFormatter}.
 */
public class HelpFormatterTest
{
    private HelpFormatter formatter;
    private StringWriter sw;
    private PrintWriter pw;
    private final String EOL = System.getProperty("line.separator");

    @Before
    public void setUp()
    {
        formatter = new HelpFormatter();
        sw = new StringWriter();
        pw = new PrintWriter(sw);
    }

    @Test
    public void testDefaultFieldValuesAndAccessors()
    {
        assertEquals(HelpFormatter.DEFAULT_WIDTH, formatter.getWidth());
        assertEquals(HelpFormatter.DEFAULT_LEFT_PAD, formatter.getLeftPadding());
        assertEquals(HelpFormatter.DEFAULT_DESC_PAD, formatter.getDescPadding());
        assertEquals(HelpFormatter.DEFAULT_SYNTAX_PREFIX, formatter.getSyntaxPrefix());
        assertEquals(System.getProperty("line.separator"), formatter.getNewLine());
        assertEquals(HelpFormatter.DEFAULT_OPT_PREFIX, formatter.getOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_LONG_OPT_PREFIX, formatter.getLongOptPrefix());
        assertEquals(HelpFormatter.DEFAULT_ARG_NAME, formatter.getArgName());
        assertNotNull(formatter.getOptionComparator());

        formatter.setWidth(120);
        assertEquals(120, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(8);
        assertEquals(8, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());

        formatter.setOptPrefix("+");
        assertEquals("+", formatter.getOptPrefix());

        formatter.setLongOptPrefix("++");
        assertEquals("++", formatter.getLongOptPrefix());

        formatter.setArgName("parameter");
        assertEquals("parameter", formatter.getArgName());

        Comparator customComp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return 0;
            }
        };
        formatter.setOptionComparator(customComp);
        assertSame(customComp, formatter.getOptionComparator());

        // Setting null comparator should reset to default OptionComparator
        formatter.setOptionComparator(null);
        assertNotNull(formatter.getOptionComparator());
        assertTrue(formatter.getOptionComparator().getClass().getName().contains("OptionComparator"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullCmdLineSyntaxThrowsException()
    {
        formatter.printHelp(pw, 80, null, "header", new Options(), 1, 3, "footer", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptyCmdLineSyntaxThrowsException()
    {
        formatter.printHelp(pw, 80, "", "header", new Options(), 1, 3, "footer", false);
    }

    @Test
    public void testPrintHelpBasic()
    {
        Options options = new Options();
        options.addOption("a", "alpha", false, "Alpha description");
        options.addOption("b", "beta", true, "Beta description");

        formatter.printHelp(pw, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", false);
        pw.flush();

        String expected = "usage: myapp" + EOL
                + "Header banner" + EOL
                + "  -a,--alpha        Alpha description" + EOL
                + "  -b,--beta <arg>   Beta description" + EOL
                + "Footer banner" + EOL;

        assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintHelpNullAndEmptyHeaderFooter()
    {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        formatter.printHelp(pw, 80, "myapp", null, options, 1, 3, null, false);
        pw.flush();
        String out1 = sw.toString();

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "myapp", "   ", options, 1, 3, "   ", false);
        pw.flush();
        String out2 = sw.toString();

        String expected = "usage: myapp" + EOL
                + " -h,--help   display help" + EOL;

        assertEquals(expected, out1);
        assertEquals(expected, out2);
    }

    @Test
    public void testPrintHelpWithAutoUsage()
    {
        Options options = new Options();
        options.addOption("f", "file", true, "the target file");

        formatter.printHelp(pw, 80, "myapp", "header", options, 1, 3, "footer", true);
        pw.flush();

        String expected = "usage: myapp [-f <file>]" + EOL
                + "header" + EOL
                + " -f,--file <file>   the target file" + EOL
                + "footer" + EOL;

        // since option arg is not named specifically, default arg name is "file" if we set it or "arg"
        Option opt = (Option) options.getOptions().iterator().next();
        opt.setArgName("FILE");

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        formatter.printHelp(pw, 80, "myapp", "header", options, 1, 3, "footer", true);
        pw.flush();

        expected = "usage: myapp [-f <FILE>]" + EOL
                + "header" + EOL
                + " -f,--file <FILE>   the target file" + EOL
                + "footer" + EOL;

        assertEquals(expected, sw.toString());
    }

    @Test
    public void testPrintHelpConvenienceMethods()
    {
        Options options = new Options();
        options.addOption("v", "version", false, "print version");

        // printHelp(cmdLineSyntax, options)
        formatter.printHelp("app", options);

        // printHelp(cmdLineSyntax, options, autoUsage)
        formatter.printHelp("app", options, true);

        // printHelp(cmdLineSyntax, header, options, footer)
        formatter.printHelp("app", "Header", options, "Footer");

        // printHelp(cmdLineSyntax, header, options, footer, autoUsage)
        formatter.printHelp("app", "Header", options, "Footer", true);

        // printHelp(width, cmdLineSyntax, header, options, footer)
        formatter.printHelp(80, "app", "Header", options, "Footer");

        // printHelp(width, cmdLineSyntax, header, options, footer, autoUsage)
        formatter.printHelp(80, "app", "Header", options, "Footer", true);

        // printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer)
        formatter.printHelp(pw, 80, "app", "Header", options, 2, 2, "Footer");
        pw.flush();
        assertTrue(sw.toString().contains("usage: app"));
        assertTrue(sw.toString().contains("Header"));
        assertTrue(sw.toString().contains("Footer"));
    }

    @Test
    public void testPrintUsageSimple()
    {
        formatter.printUsage(pw, 80, "myapp <file> [options]");
        pw.flush();
        assertEquals("usage: myapp <file> [options]" + EOL, sw.toString());
    }

    @Test
    public void testPrintUsageWithOptionsAndOptionGroup()
    {
        Options options = new Options();

        Option optA = new Option("a", "alpha", false, "Alpha option");
        Option optB = new Option("b", "beta", true, "Beta option");
        optB.setRequired(true);
        optB.setArgName("VAL");

        Option optLongOnly = new Option(null, "long-only", false, "Long opt only");

        OptionGroup group1 = new OptionGroup();
        group1.setRequired(false);
        Option optC = new Option("c", "cat", false, "Cat option");
        Option optD = new Option("d", "dog", false, "Dog option");
        group1.addOption(optC);
        group1.addOption(optD);

        OptionGroup group2 = new OptionGroup();
        group2.setRequired(true);
        Option optE = new Option("e", "elephant", true, "Elephant option");
        optE.setArgName("SIZE");
        Option optF = new Option(null, "fox", false, "Fox option");
        group2.addOption(optE);
        group2.addOption(optF);

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optLongOnly);
        options.addOptionGroup(group1);
        options.addOptionGroup(group2);

        formatter.printUsage(pw, 100, "mytool", options);
        pw.flush();

        String usage = sw.toString();
        assertTrue(usage.startsWith("usage: mytool "));
        assertTrue(usage.contains("[-a]"));
        assertTrue(usage.contains("-b <VAL>"));
        assertTrue(usage.contains("[--long-only]"));
        assertTrue(usage.contains("[-c | -d]"));
        assertTrue(usage.contains("(-e <SIZE> | --fox)") || usage.contains("-e <SIZE> | --fox"));
    }

    @Test
    public void testRenderOptionsFormatting()
    {
        Options options = new Options();
        Option opt1 = new Option("s", false, "Short only");
        Option opt2 = new Option(null, "long-only", true, "Long only with arg");
        opt2.setArgName(null); // hasArg=true, hasArgName=false (should append space)
        Option opt3 = new Option("b", "both", true, null); // description is null
        opt3.setArgName("VAL");

        options.addOption(opt1);
        options.addOption(opt2);
        options.addOption(opt3);

        StringBuffer sb = new StringBuffer();
        formatter.renderOptions(sb, 80, options, 2, 4);

        String rendered = sb.toString();
        assertTrue(rendered.contains("  -b,--both <VAL>"));
        assertTrue(rendered.contains("  -s"));
        assertTrue(rendered.contains("     --long-only "));
    }

    @Test
    public void testRenderWrappedTextBasicAndWrap()
    {
        StringBuffer sb = new StringBuffer();
        String text = "This is a simple text that should be wrapped across multiple lines nicely.";
        formatter.renderWrappedText(sb, 25, 4, text);

        String result = sb.toString();
        String[] lines = result.split(EOL);
        for (int i = 0; i < lines.length; i++)
        {
            assertTrue("Line " + i + " should be <= 25 chars but was " + lines[i].length(), lines[i].length() <= 25);
            if (i > 0)
            {
                assertTrue("Subsequent line should start with 4 padding spaces", lines[i].startsWith("    "));
            }
        }
    }

    @Test
    public void testRenderWrappedTextWithTabsAndNewLines()
    {
        StringBuffer sb = new StringBuffer();
        String text = "First line\nSecond line with\ttab\nThird line";
        formatter.renderWrappedText(sb, 80, 2, text);

        String result = sb.toString();
        assertTrue(result.contains("First line"));
        assertTrue(result.contains("Second line with"));
        assertTrue(result.contains("Third line"));
    }

    @Test
    public void testRenderWrappedTextNextLineTabStopGreaterThanWidth()
    {
        StringBuffer sb = new StringBuffer();
        String text = "Some very long text that must be wrapped when tab stop is larger than width.";
        formatter.renderWrappedText(sb, 10, 15, text);

        String result = sb.toString();
        String[] lines = result.split(EOL);
        assertTrue(lines.length > 1);
    }

    @Test
    public void testRenderWrappedTextLongUnbreakableWord()
    {
        StringBuffer sb = new StringBuffer();
        String text = "Prefix " + "VeryLongUnbreakableWordThatExceedsTheWidthLimitOfColumn" + " suffix";
        formatter.renderWrappedText(sb, 15, 2, text);

        String result = sb.toString();
        assertTrue(result.contains("VeryLongUnbreakableWordThatExceedsTheWidthLimitOfColumn"));
    }

    @Test
    public void testFindWrapPos()
    {
        // 1. Text with newline within width
        String text1 = "hello\nworld";
        assertEquals(6, formatter.findWrapPos(text1, 10, 0));

        // 2. Text with tab within width
        String text2 = "hello\tworld";
        assertEquals(6, formatter.findWrapPos(text2, 10, 0));

        // 3. startPos + width >= text.length()
        String text3 = "short text";
        assertEquals(-1, formatter.findWrapPos(text3, 20, 0));

        // 4. Space before startPos + width
        String text4 = "The quick brown fox jumps";
        int pos = formatter.findWrapPos(text4, 10, 0);
        assertEquals(9, pos); // 'brown' starts at 10, space at 9

        // 5. No space before startPos + width, but space after startPos + width
        String text5 = "Supercalifragilisticexpialidocious is awesome";
        pos = formatter.findWrapPos(text5, 10, 0);
        assertEquals(34, pos); // wraps at the first space after the long word

        // 6. Long word with no whitespace at all
        String text6 = "Supercalifragilisticexpialidocious";
        pos = formatter.findWrapPos(text6, 10, 0);
        assertEquals(-1, pos);
    }

    @Test
    public void testCreatePadding()
    {
        assertEquals("", formatter.createPadding(0));
        assertEquals(" ", formatter.createPadding(1));
        assertEquals("   ", formatter.createPadding(3));
        assertEquals("          ", formatter.createPadding(10));
    }

    @Test
    public void testRtrim()
    {
        assertEquals(null, formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("", formatter.rtrim("   "));
        assertEquals("\t\n\r", formatter.rtrim("\t\n\r")); // Character.isWhitespace check handles tabs, newlines, spaces
        assertEquals("", formatter.rtrim("  \t \n \r "));
        assertEquals("hello", formatter.rtrim("hello"));
        assertEquals("hello", formatter.rtrim("hello   "));
        assertEquals("  hello", formatter.rtrim("  hello  \t"));
    }

    @Test
    public void testOptionComparator()
    {
        Comparator comp = formatter.getOptionComparator();
        Option optA = new Option("a", "alpha", false, null);
        Option optB = new Option("b", "beta", false, null);
        Option optA2 = new Option("A", "ALPHA", false, null);

        assertTrue(comp.compare(optA, optB) < 0);
        assertTrue(comp.compare(optB, optA) > 0);
        assertEquals(0, comp.compare(optA, optA2)); // case-insensitive
    }

    @Test
    public void testCustomOptionComparatorSorting()
    {
        Options options = new Options();
        options.addOption("z", "zebra", false, "Zebra option");
        options.addOption("a", "apple", false, "Apple option");
        options.addOption("m", "monkey", false, "Monkey option");

        // Set reverse alphabetical comparator
        formatter.setOptionComparator(new Comparator() {
            public int compare(Object o1, Object o2) {
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt2.getKey().compareToIgnoreCase(opt1.getKey());
            }
        });

        formatter.printHelp(pw, 80, "testApp", null, options, 1, 3, null, false);
        pw.flush();

        String output = sw.toString();
        int posZ = output.indexOf("-z");
        int posM = output.indexOf("-m");
        int posA = output.indexOf("-a");

        assertTrue(posZ < posM);
        assertTrue(posM < posA);
    }

    @Test
    public void testPrintWrappedOverloads()
    {
        formatter.printWrapped(pw, 80, "First wrapped line without tab stop");
        pw.flush();
        assertTrue(sw.toString().contains("First wrapped line without tab stop"));

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 80, 5, "Second wrapped line with tab stop");
        pw.flush();
        assertTrue(sw.toString().contains("Second wrapped line with tab stop"));
    }
}