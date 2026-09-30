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
    private StringWriter sw;
    private PrintWriter pw;

    @Before
    public void setUp() {
        formatter = new HelpFormatter();
        sw = new StringWriter();
        pw = new PrintWriter(sw);
    }

    @Test
    public void testGettersAndSetters() {
        formatter.setWidth(100);
        assertEquals(100, formatter.getWidth());

        formatter.setLeftPadding(5);
        assertEquals(5, formatter.getLeftPadding());

        formatter.setDescPadding(8);
        assertEquals(8, formatter.getDescPadding());

        formatter.setSyntaxPrefix("Syntax: ");
        assertEquals("Syntax: ", formatter.getSyntaxPrefix());

        formatter.setNewLine("\r\n");
        assertEquals("\r\n", formatter.getNewLine());

        formatter.setOptPrefix("/");
        assertEquals("/", formatter.getOptPrefix());

        formatter.setLongOptPrefix("//");
        assertEquals("//", formatter.getLongOptPrefix());

        formatter.setLongOptSeparator("=");
        assertEquals("=", formatter.getLongOptSeparator());

        formatter.setArgName("val");
        assertEquals("val", formatter.getArgName());

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

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpNullSyntax() {
        formatter.printHelp(pw, 80, null, "header", new Options(), 1, 3, "footer", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrintHelpEmptySyntax() {
        formatter.printHelp(pw, 80, "", "header", new Options(), 1, 3, "footer", true);
    }

    @Test
    public void testPrintHelpBasic() {
        Options options = new Options();
        options.addOption("a", "all", false, "do not ignore entries starting with .");
        options.addOption("b", "block-size", true, "use SIZE-byte blocks");

        formatter.printHelp(pw, 80, "myapp", "Header banner", options, 2, 4, "Footer banner", true);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("usage: myapp"));
        assertTrue(output.contains("Header banner"));
        assertTrue(output.contains("-a,--all"));
        assertTrue(output.contains("-b,--block-size <arg>"));
        assertTrue(output.contains("Footer banner"));
    }

    @Test
    public void testPrintHelpOverloads() {
        Options options = new Options();
        options.addOption("h", "help", false, "print help");

        formatter.printHelp(pw, 80, "cmd", "header", options, 1, 3, "footer");
        pw.flush();
        assertTrue(sw.toString().contains("usage: cmd"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        try {
            System.setOut(new PrintStream(baos));

            formatter.printHelp("app", options);
            formatter.printHelp("app", options, true);
            formatter.printHelp("app", "header", options, "footer");
            formatter.printHelp("app", "header", options, "footer", true);
            formatter.printHelp(80, "app", "header", options, "footer");
            formatter.printHelp(80, "app", "header", options, "footer", true);

            String sysOut = baos.toString();
            assertTrue(sysOut.contains("usage: app"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testPrintHelpWithoutHeaderAndFooter() {
        Options options = new Options();
        options.addOption("f", "file", true, "target file");

        formatter.printHelp(pw, 80, "run", null, options, 1, 3, null, false);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.startsWith("usage: run" + formatter.getNewLine()));
        assertTrue(output.contains("-f,--file <arg>"));
    }

    @Test
    public void testPrintHelpWithBlankHeaderAndFooter() {
        Options options = new Options();
        options.addOption("f", "file", true, "target file");

        formatter.printHelp(pw, 80, "run", "   ", options, 1, 3, "   ", false);
        pw.flush();
        String output = sw.toString();
        assertTrue(output.startsWith("usage: run" + formatter.getNewLine()));
    }

    @Test
    public void testPrintUsageSimple() {
        formatter.printUsage(pw, 80, "myapp --input <file> [options]");
        pw.flush();
        String output = sw.toString().trim();
        assertEquals("usage: myapp --input <file> [options]", output);
    }

    @Test
    public void testPrintUsageWithOptionsAndOptionGroups() {
        Options options = new Options();

        Option optA = new Option("a", "enable a");
        Option optB = new Option("b", "size", true, "set size");
        optB.setRequired(true);
        optB.setArgName("SIZE");

        Option optLongOnly = new Option(null, "long-only", true, "long only option");
        optLongOnly.setArgName("");

        Option optLongOnlyVal = new Option(null, "config", true, "config path");

        options.addOption(optA);
        options.addOption(optB);
        options.addOption(optLongOnly);
        options.addOption(optLongOnlyVal);

        OptionGroup requiredGroup = new OptionGroup();
        requiredGroup.setRequired(true);
        requiredGroup.addOption(new Option("r1", "req 1"));
        requiredGroup.addOption(new Option("r2", "req 2"));
        options.addOptionGroup(requiredGroup);

        OptionGroup optionalGroup = new OptionGroup();
        optionalGroup.setRequired(false);
        optionalGroup.addOption(new Option("o1", "opt 1"));
        optionalGroup.addOption(new Option("o2", "opt 2"));
        options.addOptionGroup(optionalGroup);

        formatter.printUsage(pw, 120, "app", options);
        pw.flush();
        String output = sw.toString();

        assertTrue(output.contains("[-a]"));
        assertTrue(output.contains("-b <SIZE>"));
        assertTrue(output.contains("[--long-only]"));
        assertTrue(output.contains("[--config <arg>]"));
        assertTrue(output.contains("-r1 | -r2"));
        assertTrue(output.contains("[-o1 | -o2]"));
    }

    @Test
    public void testRenderOptionsFormatting() {
        Options options = new Options();

        Option optShortOnly = new Option("s", "short only");
        Option optShortAndLong = new Option("b", "both", true, "both options");
        optShortAndLong.setArgName("PARAM");
        Option optLongOnly = new Option(null, "long-only", false, "long only option");
        Option optBlankArg = new Option("k", "key", true, "blank arg description");
        optBlankArg.setArgName("");
        Option optNoDesc = new Option("n", "nodesc", false, null);

        options.addOption(optShortOnly);
        options.addOption(optShortAndLong);
        options.addOption(optLongOnly);
        options.addOption(optBlankArg);
        options.addOption(optNoDesc);

        formatter.setLongOptSeparator("=");
        formatter.printOptions(pw, 80, options, 2, 4);
        pw.flush();

        String output = sw.toString();
        assertTrue(output.contains("  -s"));
        assertTrue(output.contains("  -b,--both=<PARAM>"));
        assertTrue(output.contains("     --long-only"));
        assertTrue(output.contains("  -k,--key "));
        assertTrue(output.contains("  -n,--nodesc"));
    }

    @Test
    public void testFindWrapPos() {
        String text = "The quick brown fox jumps over the lazy dog";
        int pos = formatter.findWrapPos(text, 10, 0);
        assertEquals(9, pos);

        String textWithNewline = "The quick\nbrown fox";
        pos = formatter.findWrapPos(textWithNewline, 15, 0);
        assertEquals(10, pos);

        String textWithTab = "The quick\tbrown fox";
        pos = formatter.findWrapPos(textWithTab, 15, 0);
        assertEquals(10, pos);

        pos = formatter.findWrapPos(text, 100, 0);
        assertEquals(-1, pos);

        String longWord = "Supercalifragilisticexpialidocious";
        pos = formatter.findWrapPos(longWord, 10, 0);
        assertEquals(10, pos);

        pos = formatter.findWrapPos(longWord, longWord.length(), 0);
        assertEquals(-1, pos);
    }

    @Test
    public void testRenderWrappedText() {
        StringBuffer sb = new StringBuffer();
        String text = "This is a short line.";
        formatter.renderWrappedText(sb, 80, 0, text);
        assertEquals("This is a short line.", sb.toString());

        sb = new StringBuffer();
        String multiLine = "This is a longer line of text that needs to be wrapped properly across multiple lines.";
        formatter.renderWrappedText(sb, 20, 4, multiLine);
        String[] lines = sb.toString().split(formatter.getNewLine());
        assertTrue(lines.length > 1);
        for (int i = 1; i < lines.length; i++) {
            assertTrue(lines[i].startsWith("    "));
        }

        sb = new StringBuffer();
        String forcedWrap = "12345678901234567890 1234567890";
        formatter.renderWrappedText(sb, 10, 5, forcedWrap);
        assertTrue(sb.toString().contains(formatter.getNewLine()));

        sb = new StringBuffer();
        String tabStopOverflow = "Short words in a sentence that must wrap.";
        formatter.renderWrappedText(sb, 10, 15, tabStopOverflow);
        assertTrue(sb.toString().contains(formatter.getNewLine()));
    }

    @Test
    public void testPrintWrapped() {
        formatter.printWrapped(pw, 40, "First line of text that should wrap onto another line.");
        pw.flush();
        assertTrue(sw.toString().contains(formatter.getNewLine()));

        sw = new StringWriter();
        pw = new PrintWriter(sw);
        formatter.printWrapped(pw, 40, 5, "Indented text that should wrap onto another line with tab stop.");
        pw.flush();
        assertTrue(sw.toString().contains(formatter.getNewLine()));
    }

    @Test
    public void testCreatePadding() {
        assertEquals("", formatter.createPadding(0));
        assertEquals("   ", formatter.createPadding(3));
        assertEquals(10, formatter.createPadding(10).length());
    }

    @Test
    public void testRtrim() {
        assertEquals(null, formatter.rtrim(null));
        assertEquals("", formatter.rtrim(""));
        assertEquals("", formatter.rtrim("   \t  \n "));
        assertEquals("abc", formatter.rtrim("abc"));
        assertEquals("abc", formatter.rtrim("abc   "));
        assertEquals("  abc", formatter.rtrim("  abc   "));
    }

    @Test
    public void testOptionComparator() {
        Comparator comp = formatter.getOptionComparator();
        Option optA = new Option("a", "first");
        Option optB = new Option("b", "second");
        Option optA2 = new Option("A", "first upper");

        assertTrue(comp.compare(optA, optB) < 0);
        assertTrue(comp.compare(optB, optA) > 0);
        assertEquals(0, comp.compare(optA, optA2));

        Option optLongOnlyA = new Option(null, "alpha", false, "desc");
        Option optLongOnlyB = new Option(null, "beta", false, "desc");
        assertTrue(comp.compare(optLongOnlyA, optLongOnlyB) < 0);
    }
}