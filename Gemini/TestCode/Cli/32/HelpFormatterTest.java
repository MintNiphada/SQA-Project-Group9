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
    private StringWriter out;
    private PrintWriter pw;
    private String EOL;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        out = new StringWriter();
        pw = new PrintWriter(out);
        EOL = formatter.getNewLine();
    }

    @Test
    public void testGettersAndSetters() {
        formatter.setWidth(80);
        assertEquals(80, formatter.getWidth());
        assertEquals(80, formatter.defaultWidth);

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());
        assertEquals(5, formatter.defaultLeftPad);

        formatter.setDescPadding(7);
        assertEquals(7, formatter.getDescPadding());
        assertEquals(7, formatter.defaultDescPad);

        formatter.setSyntaxPrefix("syn: ");
        assertEquals("syn: ", formatter.getSyntaxPrefix());
        assertEquals("syn: ", formatter.defaultSyntaxPrefix);

        formatter.setNewLine("\n");
        assertEquals("\n", formatter.getNewLine());
        assertEquals("\n", formatter.defaultNewLine);

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());
        assertEquals("/", formatter.defaultOptPrefix);

        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());
        assertEquals("//", formatter.defaultLongOptPrefix);

        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());

        formatter.setArgName("val");
        assertEquals("val", formatter.getArgName());
        assertEquals("val", formatter.defaultArgName);

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
    public void testCreatePadding() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
    }

    @Test
    public void testRtrim() {
        assertEquals(null, formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("  abc", formatter.rtrim("  abc \t\r\n "));
    }

    @Test
    public void testFindWrapPos() {
        String text = "hello world of options";
        // text fits within width
        assertEquals(-1, formatter.findWrapPos(text, 50, 0));

        // text contains newline before width
        assertEquals(6, formatter.findWrapPos("hello\nworld", 10, 0));

        // text contains tab before width
        assertEquals(6, formatter.findWrapPos("hello\tworld", 10, 0));

        // break at whitespace
        assertEquals(5, formatter.findWrapPos(text, 10, 0));

        // startPos offset
        assertEquals(11, formatter.findWrapPos(text, 10, 6));

        // break with \r
        assertEquals(5, formatter.findWrapPos("hello\rworld", 10, 0));

        // long word that cannot be split normally before width
        assertEquals(15, formatter.findWrapPos("supercalifragilistic expialidocious", 10, 0));

        // long word extending to end of string
        assertEquals(-1, formatter.findWrapPos("supercalifragilistic", 10, 0));
    }

    @Test
    public void testRenderWrappedTextBasic() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 20, 0, "short text");
        assertEquals("short text", sb.toString());

        sb = new StringBuffer();
        formatter.renderWrappedText(sb, 20, 4, "this is a longer text that definitely needs to wrap across multiple lines");
        String expected = "this is a longer" + EOL +
                "    text that" + EOL +
                "    definitely" + EOL +
                "    needs to wrap" + EOL +
                "    across multiple" + EOL +
                "    lines";
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testRenderWrappedTextTabStopGreaterThanWidth() {
        StringBuffer sb = new StringBuffer();
        // nextLineTabStop (15) >= width (10), resets tab stop to 1
        formatter.renderWrappedText(sb, 10, 15, "first line wrap text");
        String expected = "first line" + EOL + " wrap text";
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testRenderWrappedTextLongWordWrap() {
        StringBuffer sb = new StringBuffer();
        formatter.renderWrappedText(sb, 10, 2, "123456789012345 67890");
        String expected = "123456789012345" + EOL + "  67890";
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testPrintWrapped() {
        formatter.printWrapped(pw, 20, "first line second line");
        pw.flush();
        assertEquals("first line second" + EOL + "line" + EOL, out.toString());

        out.getBuffer().setLength(0);
        formatter.printWrapped(pw, 20, 2, "first line second line");
        pw.flush();
        assertEquals("first line second" + EOL + "  line" + EOL, out.toString());
    }

    @Test
    public void testPrintUsageSimple() {
        formatter.printUsage(pw, 80, "myapp -a -b");
        pw.flush();
        assertEquals("usage: myapp -a -b" + EOL, out.toString());
    }

    @Test
    public void testPrintUsageWithOptions() {
        Options options = new Options();
        Option a = OptionBuilder.withLongOpt("alpha").withDescription("desc a").create('a');
        Option b = OptionBuilder.hasArg().withArgName("file").isRequired().create('b');
        Option c = OptionBuilder.withLongOpt("charlie").hasArg().create(); // long only
        Option d = OptionBuilder.hasArg().withArgName("").create('d'); // empty arg name
        Option e = OptionBuilder.hasArg().create('e'); // default arg name

        options.addOption(a);
        options.addOption(b);
        options.addOption(c);
        options.addOption(d);
        options.addOption(e);

        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        assertEquals("usage: myapp [-a] -b <file> [--charlie <arg>] [-d] [-e <arg>]" + EOL, out.toString());
    }

    @Test
    public void testPrintUsageWithOptionGroups() {
        Options options = new Options();
        OptionGroup group1 = new OptionGroup();
        group1.setRequired(false);
        group1.addOption(new Option("a", "alpha"));
        group1.addOption(new Option("b", "beta"));

        OptionGroup group2 = new OptionGroup();
        group2.setRequired(true);
        group2.addOption(new Option("c", "gamma"));
        group2.addOption(new Option("d", "delta"));

        options.addOptionGroup(group1);
        options.addOptionGroup(group2);

        formatter.printUsage(pw, 80, "myapp", options);
        pw.flush();
        assertEquals("usage: myapp [-a | -b] -c | -d" + EOL, out.toString());
    }

    @Test
    public void testPrintOptions() {
        Options options = new Options();
        Option a = OptionBuilder.withLongOpt("alpha").withDescription("Option Alpha").hasArg().withArgName("VAL").create('a');
        Option b = OptionBuilder.withDescription("Option Beta").create('b');
        Option c = OptionBuilder.withLongOpt("charlie").withDescription("Option Charlie without short opt").create();
        Option d = OptionBuilder.hasArg().withArgName("").create('d'); // no description, blank argName

        options.addOption(a);
        options.addOption(b);
        options.addOption(c);
        options.addOption(d);

        formatter.setLongOptSeparator("=");
        formatter.printOptions(pw, 80, options, 2, 4);
        pw.flush();

        String expected = "  -a,--alpha=<VAL>    Option Alpha" + EOL +
                "  -b                  Option Beta" + EOL +
                "  -d                  " + EOL +
                "     --charlie        Option Charlie without short opt" + EOL;
        assertEquals(expected, out.toString());
    }

    @Test
    public void testPrintHelpNullCmdLineSyntax() {
        try {
            formatter.printHelp(pw, 80, null, "header", new Options(), 2, 4, "footer");
            fail("Should throw IllegalArgumentException when cmdLineSyntax is null");
        } catch (IllegalArgumentException e) {
            assertEquals("cmdLineSyntax not provided", e.getMessage());
        }

        try {
            formatter.printHelp(pw, 80, "", "header", new Options(), 2, 4, "footer");
            fail("Should throw IllegalArgumentException when cmdLineSyntax is empty");
        } catch (IllegalArgumentException e) {
            assertEquals("cmdLineSyntax not provided", e.getMessage());
        }
    }

    @Test
    public void testPrintHelpFull() {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        formatter.printHelp(pw, 80, "myapp", "Header banner", options, 1, 3, "Footer banner", true);
        pw.flush();

        String expected = "usage: myapp [-h]" + EOL +
                "Header banner" + EOL +
                " -h,--help   display help" + EOL +
                "Footer banner" + EOL;
        assertEquals(expected, out.toString());
    }

    @Test
    public void testPrintHelpWithoutAutoUsage() {
        Options options = new Options();
        options.addOption("h", "help", false, "display help");

        formatter.printHelp(pw, 80, "myapp command", "Header banner", options, 1, 3, "Footer banner", false);
        pw.flush();

        String expected = "usage: myapp command" + EOL +
                "Header banner" + EOL +
                " -h,--help   display help" + EOL +
                "Footer banner" + EOL;
        assertEquals(expected, out.toString());
    }

    @Test
    public void testPrintHelpOverloads() {
        Options options = new Options();
        options.addOption("o", false, "opt");

        // printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer)
        formatter.printHelp(pw, 80, "app", "hdr", options, 1, 2, "ftr");
        pw.flush();
        assertTrue(out.toString().contains("usage: app"));
        assertTrue(out.toString().contains("hdr"));
        assertTrue(out.toString().contains("ftr"));

        // Test overloads that print to System.out by redirecting/verifying no exceptions
        formatter.printHelp("app", options);
        formatter.printHelp("app", options, true);
        formatter.printHelp("app", "hdr", options, "ftr");
        formatter.printHelp("app", "hdr", options, "ftr", true);
        formatter.printHelp(80, "app", "hdr", options, "ftr");
        formatter.printHelp(80, "app", "hdr", options, "ftr", true);
    }

    @Test
    public void testOptionComparatorSorting() {
        Options options = new Options();
        options.addOption("z", "zebra", false, "Zebra option");
        options.addOption("a", "apple", false, "Apple option");
        options.addOption("m", "mango", false, "Mango option");

        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        assertEquals("usage: app [-a] [-m] [-z]" + EOL, out.toString());

        // Custom reverse comparator
        formatter.setOptionComparator(new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Option) o2).getKey().compareToIgnoreCase(((Option) o1).getKey());
            }
        });
        out.getBuffer().setLength(0);
        formatter.printUsage(pw, 80, "app", options);
        pw.flush();
        assertEquals("usage: app [-z] [-m] [-a]" + EOL, out.toString());
    }

    @Test
    public void testEmptyHeaderAndFooter() {
        Options options = new Options();
        options.addOption("x", false, "description");

        formatter.printHelp(pw, 80, "app", "   ", options, 1, 2, null, false);
        pw.flush();
        String output = out.toString();
        // Should not have wrapped header or footer lines
        assertEquals("usage: app" + EOL + " -x   description" + EOL, output);
    }
}