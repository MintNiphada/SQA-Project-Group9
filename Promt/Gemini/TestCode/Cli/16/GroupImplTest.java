package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class GroupImplTest {

    private DefaultOptionBuilder obuilder;
    private ArgumentBuilder abuilder;
    private GroupBuilder gbuilder;

    private Option optHelp;
    private Option optVerbose;
    private Option optFile;
    private Option argFile;

    @Before
    public void setUp() {
        obuilder = new DefaultOptionBuilder();
        abuilder = new ArgumentBuilder();
        gbuilder = new GroupBuilder();

        optHelp = obuilder
                .withShortName("h")
                .withLongName("help")
                .withDescription("print this message")
                .create();

        optVerbose = obuilder
                .withShortName("v")
                .withLongName("verbose")
                .withDescription("verbose output")
                .create();

        argFile = abuilder
                .withName("file")
                .withMinimum(0)
                .withMaximum(1)
                .create();

        optFile = obuilder
                .withShortName("f")
                .withLongName("file")
                .withDescription("specify file")
                .withArgument(argFile)
                .create();
    }

    @Test
    public void testConstructorAndGetters() {
        List options = new ArrayList();
        options.add(optHelp);
        options.add(optVerbose);

        GroupImpl group = new GroupImpl(options, "groupName", "groupDesc", 1, 2);

        Assert.assertEquals("groupName", group.getPreferredName());
        Assert.assertEquals("groupDesc", group.getDescription());
        Assert.assertEquals(1, group.getMinimum());
        Assert.assertEquals(2, group.getMaximum());
        Assert.assertTrue(group.isRequired());
        Assert.assertEquals(2, group.getOptions().size());
        Assert.assertTrue(group.getAnonymous().isEmpty());

        Set prefixes = group.getPrefixes();
        Assert.assertTrue(prefixes.contains("-"));
        Assert.assertTrue(prefixes.contains("--"));

        Set triggers = group.getTriggers();
        Assert.assertTrue(triggers.contains("-h"));
        Assert.assertTrue(triggers.contains("--help"));
        Assert.assertTrue(triggers.contains("-v"));
        Assert.assertTrue(triggers.contains("--verbose"));
    }

    @Test
    public void testConstructorWithAnonymousArguments() {
        Option anonArg = abuilder.withName("target").withMinimum(1).withMaximum(1).create();
        List options = new ArrayList();
        options.add(optHelp);
        options.add(anonArg);

        GroupImpl group = new GroupImpl(options, "myGroup", "desc", 0, 1);

        Assert.assertFalse(group.isRequired());
        Assert.assertEquals(1, group.getOptions().size());
        Assert.assertEquals(1, group.getAnonymous().size());
        Assert.assertSame(anonArg, group.getAnonymous().get(0));
    }

    @Test
    public void testFindOption() {
        List options = new ArrayList();
        options.add(optHelp);
        options.add(optVerbose);
        GroupImpl group = new GroupImpl(options, "test", "test", 0, 0);

        Assert.assertSame(optHelp, group.findOption("-h"));
        Assert.assertSame(optHelp, group.findOption("--help"));
        Assert.assertSame(optVerbose, group.findOption("-v"));
        Assert.assertSame(optVerbose, group.findOption("--verbose"));
        Assert.assertNull(group.findOption("-nonexistent"));
    }

    @Test
    public void testCanProcess() {
        Option anonArg = abuilder.withName("anon").create();
        List options = new ArrayList();
        options.add(optHelp);
        options.add(anonArg);

        GroupImpl group = new GroupImpl(options, "testGroup", "desc", 0, 2);
        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());

        Assert.assertFalse(group.canProcess(cl, (String) null));
        Assert.assertTrue(group.canProcess(cl, "-h"));
        Assert.assertTrue(group.canProcess(cl, "--help"));
        Assert.assertTrue(group.canProcess(cl, "randomArgument"));

        List noAnonOptions = new ArrayList();
        noAnonOptions.add(optHelp);
        GroupImpl groupNoAnon = new GroupImpl(noAnonOptions, "testGroup2", "desc", 0, 2);
        Assert.assertFalse(groupNoAnon.canProcess(cl, "randomArgument"));
        Assert.assertFalse(groupNoAnon.canProcess(cl, "-unknownOption"));
    }

    @Test
    public void testProcessOptions() throws OptionException {
        List options = new ArrayList();
        options.add(optHelp);
        options.add(optVerbose);
        GroupImpl group = new GroupImpl(options, "group", "desc", 0, 2);

        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());
        List args = new ArrayList();
        args.add("-h");
        args.add("-v");

        ListIterator it = args.listIterator();
        group.process(cl, it);

        Assert.assertTrue(cl.hasOption(optHelp));
        Assert.assertTrue(cl.hasOption(optVerbose));
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testProcessWithBurstingAndAnonymousArgs() throws OptionException {
        Option anonArg = abuilder.withName("anon").create();
        List options = new ArrayList();
        options.add(optHelp);
        options.add(optFile);
        options.add(anonArg);
        GroupImpl group = new GroupImpl(options, "group", "desc", 0, 3);

        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());
        List args = new ArrayList();
        args.add("-f");
        args.add("myfile.txt");
        args.add("anonValue");

        ListIterator it = args.listIterator();
        group.process(cl, it);

        Assert.assertTrue(cl.hasOption(optFile));
        Assert.assertTrue(cl.hasOption("anon"));
        Assert.assertEquals("myfile.txt", cl.getValue(optFile));
        Assert.assertEquals("anonValue", cl.getValue(anonArg));
    }

    @Test
    public void testProcessStopsOnUnknownOption() throws OptionException {
        List options = new ArrayList();
        options.add(optHelp);
        GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());
        List args = new ArrayList();
        args.add("-h");
        args.add("-unknown");

        ListIterator it = args.listIterator();
        group.process(cl, it);

        Assert.assertTrue(cl.hasOption(optHelp));
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("-unknown", it.next());
    }

    @Test
    public void testProcessLoopDetection() throws OptionException {
        List options = new ArrayList();
        options.add(optHelp);
        GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());
        String sharedToken = "-unknown";
        List args = new ArrayList();
        args.add(sharedToken);
        args.add(sharedToken);

        ListIterator it = args.listIterator();
        group.process(cl, it);
        Assert.assertEquals(sharedToken, it.next());
    }

    @Test
    public void testValidateSuccess() throws OptionException {
        List options = new ArrayList();
        options.add(optHelp);
        options.add(optVerbose);
        GroupImpl group = new GroupImpl(options, "group", "desc", 1, 2);

        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());
        cl.addOption(optHelp);

        group.validate(cl);
    }

    @Test(expected = OptionException.class)
    public void testValidateMissingOption() throws OptionException {
        List options = new ArrayList();
        options.add(optHelp);
        GroupImpl group = new GroupImpl(options, "group", "desc", 1, 1);

        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());
        group.validate(cl);
    }

    @Test(expected = OptionException.class)
    public void testValidateUnexpectedToken() throws OptionException {
        List options = new ArrayList();
        options.add(optHelp);
        options.add(optVerbose);
        GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());
        cl.addOption(optHelp);
        cl.addOption(optVerbose);

        group.validate(cl);
    }

    @Test
    public void testValidateAnonymousArgument() throws OptionException {
        Option anonArg = abuilder.withName("requiredArg").withMinimum(1).withMaximum(1).create();
        List options = new ArrayList();
        options.add(anonArg);
        GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());
        try {
            group.validate(cl);
            Assert.fail("Expected OptionException for missing anonymous required argument");
        } catch (OptionException e) {
            Assert.assertNotNull(e.getMessage());
        }

        cl.addValue(anonArg, "someValue");
        group.validate(cl);
    }

    @Test
    public void testAppendUsageSimple() {
        List options = new ArrayList();
        options.add(optHelp);
        options.add(optVerbose);
        GroupImpl group = new GroupImpl(options, "optionsGroup", "description", 0, 2);

        StringBuffer buffer = new StringBuffer();
        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        group.appendUsage(buffer, helpSettings, null);
        String usage = buffer.toString();
        Assert.assertTrue(usage.contains("optionsGroup"));
        Assert.assertTrue(usage.contains("-h"));
        Assert.assertTrue(usage.contains("-v"));
    }

    @Test
    public void testAppendUsageWithOuterAndAnonymous() {
        Option anonArg = abuilder.withName("file").create();
        List options = new ArrayList();
        options.add(optHelp);
        options.add(anonArg);
        GroupImpl group = new GroupImpl(options, null, "desc", 0, 2);

        StringBuffer buffer = new StringBuffer();
        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_OUTER);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        group.appendUsage(buffer, helpSettings, null, ",");
        String usage = buffer.toString();
        Assert.assertTrue(usage.startsWith("["));
        Assert.assertTrue(usage.endsWith("]"));
    }

    @Test
    public void testAppendUsageWithComparator() {
        List options = new ArrayList();
        options.add(optVerbose);
        options.add(optHelp);
        GroupImpl group = new GroupImpl(options, "group", "desc", 1, 2);

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt1.getPreferredName().compareTo(opt2.getPreferredName());
            }
        };

        StringBuffer buffer = new StringBuffer();
        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        group.appendUsage(buffer, helpSettings, comp, "|");

        String usage = buffer.toString();
        int idxHelp = usage.indexOf("-h");
        int idxVerbose = usage.indexOf("-v");
        Assert.assertTrue(idxHelp < idxVerbose);
    }

    @Test
    public void testHelpLines() {
        Option anonArg = abuilder.withName("arg").withDescription("an arg").create();
        List options = new ArrayList();
        options.add(optHelp);
        options.add(anonArg);
        GroupImpl group = new GroupImpl(options, "mainGroup", "main description", 1, 2);

        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        List helpLines = group.helpLines(0, helpSettings, null);
        Assert.assertFalse(helpLines.isEmpty());

        boolean foundGroupName = false;
        boolean foundOption = false;
        boolean foundArg = false;

        for (int i = 0; i < helpLines.size(); i++) {
            HelpLine line = (HelpLine) helpLines.get(i);
            if (line.getOption() == group) {
                foundGroupName = true;
            }
            if (line.getOption() == optHelp) {
                foundOption = true;
            }
            if (line.getOption() == anonArg) {
                foundArg = true;
            }
        }

        Assert.assertTrue(foundGroupName);
        Assert.assertTrue(foundOption);
        Assert.assertTrue(foundArg);
    }

    @Test
    public void testHelpLinesWithComparator() {
        List options = new ArrayList();
        options.add(optVerbose);
        options.add(optHelp);
        GroupImpl group = new GroupImpl(options, "group", "desc", 0, 2);

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Option opt1 = (Option) o1;
                Option opt2 = (Option) o2;
                return opt1.getPreferredName().compareTo(opt2.getPreferredName());
            }
        };

        Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        List helpLines = group.helpLines(0, helpSettings, comp);
        Assert.assertEquals(2, helpLines.size());
        HelpLine first = (HelpLine) helpLines.get(0);
        HelpLine second = (HelpLine) helpLines.get(1);
        Assert.assertEquals(optHelp, first.getOption());
        Assert.assertEquals(optVerbose, second.getOption());
    }

    @Test
    public void testDefaults() {
        Option anonArg = abuilder.withName("arg").withDefault("defVal").create();
        List options = new ArrayList();
        options.add(optHelp);
        options.add(anonArg);
        GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1);

        WriteableCommandLine cl = new WriteableCommandLineImpl(group, new ArrayList());
        group.defaults(cl);

        Assert.assertEquals("defVal", cl.getValue(anonArg));
    }

    @Test
    public void testReverseStringComparator() {
        Comparator comp = ReverseStringComparator.getInstance();
        Assert.assertNotNull(comp);
        Assert.assertTrue(comp.compare("a", "b") > 0);
        Assert.assertTrue(comp.compare("b", "a") < 0);
        Assert.assertEquals(0, comp.compare("same", "same"));
    }
}