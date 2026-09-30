package org.apache.commons.cli2;

import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.CommandBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.builder.SwitchBuilder;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class OptionTest {

    private DefaultOptionBuilder dob;
    private ArgumentBuilder ab;
    private GroupBuilder gb;
    private CommandBuilder cb;
    private SwitchBuilder sb;

    @Before
    public void setUp() {
        dob = new DefaultOptionBuilder();
        ab = new ArgumentBuilder();
        gb = new GroupBuilder();
        cb = new CommandBuilder();
        sb = new SwitchBuilder();
    }

    @Test
    public void testCustomOptionImplementation() throws OptionException {
        Option customOption = new Option() {
            private int id = 42;
            private boolean required = true;

            public void process(WriteableCommandLine commandLine, ListIterator args) throws OptionException {
                if (!args.hasNext()) {
                    throw new OptionException(this, "No argument to process");
                }
                String arg = (String) args.next();
                commandLine.addOption(this);
                commandLine.addValue(this, arg);
            }

            public void defaults(WriteableCommandLine commandLine) {
                commandLine.addValue(this, "default-val");
            }

            public boolean canProcess(WriteableCommandLine commandLine, String argument) {
                return "--custom".equals(argument);
            }

            public boolean canProcess(WriteableCommandLine commandLine, ListIterator arguments) {
                if (arguments.hasNext()) {
                    String next = (String) arguments.next();
                    arguments.previous();
                    return canProcess(commandLine, next);
                }
                return false;
            }

            public Set getTriggers() {
                Set triggers = new HashSet();
                triggers.add("--custom");
                return triggers;
            }

            public Set getPrefixes() {
                Set prefixes = new HashSet();
                prefixes.add("--");
                return prefixes;
            }

            public void validate(WriteableCommandLine commandLine) throws OptionException {
                if (required && !commandLine.hasOption(this)) {
                    throw new OptionException(this, "Missing required option");
                }
            }

            public List helpLines(int depth, Set helpSettings, Comparator comp) {
                return Collections.emptyList();
            }

            public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
                buffer.append("--custom");
            }

            public String getPreferredName() {
                return "--custom";
            }

            public String getDescription() {
                return "Custom test option";
            }

            public int getId() {
                return id;
            }

            public Option findOption(String trigger) {
                return getTriggers().contains(trigger) ? this : null;
            }

            public boolean isRequired() {
                return required;
            }
        };

        Assert.assertEquals(42, customOption.getId());
        Assert.assertTrue(customOption.isRequired());
        Assert.assertEquals("--custom", customOption.getPreferredName());
        Assert.assertEquals("Custom test option", customOption.getDescription());
        Assert.assertTrue(customOption.getTriggers().contains("--custom"));
        Assert.assertTrue(customOption.getPrefixes().contains("--"));
        Assert.assertSame(customOption, customOption.findOption("--custom"));
        Assert.assertNull(customOption.findOption("--unknown"));

        StringBuffer buffer = new StringBuffer();
        customOption.appendUsage(buffer, Collections.emptySet(), null);
        Assert.assertEquals("--custom", buffer.toString());
        Assert.assertTrue(customOption.helpLines(0, Collections.emptySet(), null).isEmpty());

        WriteableCommandLine commandLine = new WriteableCommandLineImpl(customOption, new ArrayList());

        Assert.assertTrue(customOption.canProcess(commandLine, "--custom"));
        Assert.assertFalse(customOption.canProcess(commandLine, "--other"));

        List argsList = new ArrayList();
        argsList.add("--custom");
        argsList.add("value1");
        ListIterator it = argsList.listIterator();
        Assert.assertTrue(customOption.canProcess(commandLine, it));
        Assert.assertEquals(0, it.nextIndex());

        try {
            customOption.validate(commandLine);
            Assert.fail("Expected OptionException for missing option");
        } catch (OptionException e) {
            Assert.assertEquals(customOption, e.getOption());
        }

        customOption.defaults(commandLine);
        Assert.assertEquals("default-val", commandLine.getValue(customOption));

        it = argsList.listIterator();
        customOption.process(commandLine, it);
        Assert.assertTrue(commandLine.hasOption(customOption));
        Assert.assertEquals("--custom", commandLine.getValue(customOption));
        Assert.assertEquals(1, it.nextIndex());

        customOption.validate(commandLine);
    }

    @Test
    public void testDefaultOptionAsOption() throws OptionException {
        Option opt = dob
                .withShortName("o")
                .withLongName("output")
                .withDescription("Output file")
                .withRequired(true)
                .withId(101)
                .create();

        Assert.assertEquals(101, opt.getId());
        Assert.assertTrue(opt.isRequired());
        Assert.assertEquals("--output", opt.getPreferredName());
        Assert.assertEquals("Output file", opt.getDescription());
        Assert.assertTrue(opt.getTriggers().contains("-o"));
        Assert.assertTrue(opt.getTriggers().contains("--output"));
        Assert.assertSame(opt, opt.findOption("-o"));
        Assert.assertSame(opt, opt.findOption("--output"));
        Assert.assertNull(opt.findOption("-unknown"));

        WriteableCommandLine cl = new WriteableCommandLineImpl(opt, new ArrayList());
        Assert.assertTrue(opt.canProcess(cl, "-o"));
        Assert.assertTrue(opt.canProcess(cl, "--output"));
        Assert.assertFalse(opt.canProcess(cl, "-x"));

        List args = new ArrayList();
        args.add("-o");
        ListIterator it = args.listIterator();
        opt.process(cl, it);
        Assert.assertTrue(cl.hasOption(opt));
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testPropertyOptionAsOption() throws OptionException {
        Option opt = new PropertyOption();

        Assert.assertEquals(PropertyOption.DEFAULT_OPTION_STRING, opt.getPreferredName());
        Assert.assertFalse(opt.isRequired());
        Assert.assertTrue(opt.getTriggers().contains("-D"));
        Assert.assertSame(opt, opt.findOption("-D"));

        WriteableCommandLine cl = new WriteableCommandLineImpl(opt, new ArrayList());
        Assert.assertTrue(opt.canProcess(cl, "-Dkey=value"));

        List args = new ArrayList();
        args.add("-Dkey=value");
        ListIterator it = args.listIterator();
        opt.process(cl, it);
        Assert.assertTrue(cl.hasOption(opt));
        Assert.assertEquals("value", cl.getProperty("-Dkey"));
    }

    @Test
    public void testArgumentAsOption() throws OptionException {
        Option opt = ab
                .withName("target")
                .withMinimum(1)
                .withMaximum(2)
                .withId(200)
                .create();

        Assert.assertEquals(200, opt.getId());
        Assert.assertEquals("target", opt.getPreferredName());
        Assert.assertTrue(opt.getTriggers().isEmpty());

        WriteableCommandLine cl = new WriteableCommandLineImpl(opt, new ArrayList());
        Assert.assertTrue(opt.canProcess(cl, "file.txt"));

        List args = new ArrayList();
        args.add("file1.txt");
        args.add("file2.txt");
        ListIterator it = args.listIterator();
        opt.process(cl, it);
        Assert.assertTrue(cl.hasOption(opt));
        List values = cl.getValues(opt);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("file1.txt", values.get(0));
        Assert.assertEquals("file2.txt", values.get(1));
    }

    @Test
    public void testSwitchAsOption() throws OptionException {
        Option opt = sb
                .withName("display")
                .withPrefix("+")
                .withId(300)
                .create();

        Assert.assertEquals(300, opt.getId());
        Assert.assertEquals("+display", opt.getPreferredName());
        Assert.assertTrue(opt.getTriggers().contains("+display"));

        WriteableCommandLine cl = new WriteableCommandLineImpl(opt, new ArrayList());
        Assert.assertTrue(opt.canProcess(cl, "+display"));
        Assert.assertFalse(opt.canProcess(cl, "-display"));

        List args = new ArrayList();
        args.add("+display");
        ListIterator it = args.listIterator();
        opt.process(cl, it);
        Assert.assertTrue(cl.hasOption(opt));
        Assert.assertEquals(Boolean.TRUE, cl.getSwitch(opt));
    }

    @Test
    public void testCommandAsOption() throws OptionException {
        Option opt = cb
                .withName("commit")
                .withDescription("Commit changes")
                .withId(400)
                .create();

        Assert.assertEquals(400, opt.getId());
        Assert.assertEquals("commit", opt.getPreferredName());
        Assert.assertEquals("Commit changes", opt.getDescription());
        Assert.assertTrue(opt.getTriggers().contains("commit"));

        WriteableCommandLine cl = new WriteableCommandLineImpl(opt, new ArrayList());
        Assert.assertTrue(opt.canProcess(cl, "commit"));

        List args = new ArrayList();
        args.add("commit");
        ListIterator it = args.listIterator();
        opt.process(cl, it);
        Assert.assertTrue(cl.hasOption(opt));
    }

    @Test
    public void testGroupAsOption() throws OptionException {
        Option opt1 = dob.withShortName("a").create();
        Option opt2 = dob.withShortName("b").create();

        Group group = gb
                .withName("options")
                .withOption(opt1)
                .withOption(opt2)
                .withMinimum(1)
                .withMaximum(2)
                .withId(500)
                .create();

        Option groupOption = group;
        Assert.assertEquals(500, groupOption.getId());
        Assert.assertTrue(groupOption.isRequired());
        Assert.assertEquals("options", groupOption.getPreferredName());
        Assert.assertSame(opt1, groupOption.findOption("-a"));
        Assert.assertSame(opt2, groupOption.findOption("-b"));
        Assert.assertSame(groupOption, groupOption.findOption("options"));

        WriteableCommandLine cl = new WriteableCommandLineImpl(groupOption, new ArrayList());
        Assert.assertTrue(groupOption.canProcess(cl, "-a"));
        Assert.assertTrue(groupOption.canProcess(cl, "-b"));
        Assert.assertFalse(groupOption.canProcess(cl, "-c"));

        List args = new ArrayList();
        args.add("-a");
        ListIterator it = args.listIterator();
        groupOption.process(cl, it);
        Assert.assertTrue(cl.hasOption(opt1));
        groupOption.validate(cl);
    }
}