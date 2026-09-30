package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.resource.ResourceConstants;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GroupImplTest {

    private Option optA;
    private Option optB;
    private Option optC;
    private Argument arg1;
    private Argument arg2;
    private GroupImpl group;

    @Before
    public void setUp() {
        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final ArgumentBuilder abuilder = new ArgumentBuilder();

        optA = obuilder.withShortName("a").withLongName("all").withDescription("all options").create();
        optB = obuilder.withShortName("b").withLongName("batch").withDescription("batch mode").create();
        optC = obuilder.withShortName("c").withLongName("check").withDescription("check mode").withRequired(true).create();

        arg1 = abuilder.withName("file").withMinimum(0).withMaximum(1).create();
        arg2 = abuilder.withName("output").withMinimum(1).withMaximum(1).create();

        final List options = new ArrayList();
        options.add(optA);
        options.add(optB);
        options.add(arg1);

        group = new GroupImpl(options, "testGroup", "A test group", 1, 2);
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals("testGroup", group.getPreferredName());
        assertEquals("A test group", group.getDescription());
        assertEquals(1, group.getMinimum());
        assertEquals(2, group.getMaximum());
        assertTrue(group.isRequired());

        final GroupImpl optionalGroup = new GroupImpl(new ArrayList(), "optGroup", "optional", 0, 1);
        assertFalse(optionalGroup.isRequired());

        final List memberOptions = group.getOptions();
        assertEquals(2, memberOptions.size());
        assertTrue(memberOptions.contains(optA));
        assertTrue(memberOptions.contains(optB));

        final List anonArgs = group.getAnonymous();
        assertEquals(1, anonArgs.size());
        assertTrue(anonArgs.contains(arg1));

        final Set triggers = group.getTriggers();
        assertTrue(triggers.contains("-a"));
        assertTrue(triggers.contains("--all"));
        assertTrue(triggers.contains("-b"));
        assertTrue(triggers.contains("--batch"));

        final Set prefixes = group.getPrefixes();
        assertTrue(prefixes.contains("-"));
        assertTrue(prefixes.contains("--"));
    }

    @Test
    public void testCanProcess() {
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        // Null argument
        assertFalse(group.canProcess(commandLine, null));

        // Direct option match
        assertTrue(group.canProcess(commandLine, "-a"));
        assertTrue(group.canProcess(commandLine, "--all"));
        assertTrue(group.canProcess(commandLine, "-b"));
        assertTrue(group.canProcess(commandLine, "--batch"));

        // Bursting / tailMap match
        assertTrue(group.canProcess(commandLine, "-aBurst"));

        // Looks like option but does not match any member option
        assertFalse(group.canProcess(commandLine, "-z"));
        assertFalse(group.canProcess(commandLine, "--unknown"));

        // Non-option argument when anonymous arguments exist
        assertTrue(group.canProcess(commandLine, "someValue"));

        // Non-option argument when no anonymous arguments exist
        final List noAnonOptions = new ArrayList();
        noAnonOptions.add(optA);
        final GroupImpl noAnonGroup = new GroupImpl(noAnonOptions, "noAnon", "no anon", 0, 1);
        final WriteableCommandLine cmdNoAnon = new WriteableCommandLineImpl(noAnonGroup, new ArrayList());
        assertFalse(noAnonGroup.canProcess(cmdNoAnon, "someValue"));
    }

    @Test
    public void testProcessDirectOption() throws OptionException {
        final List args = new ArrayList();
        args.add("-a");
        args.add("-b");

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);

        assertTrue(commandLine.hasOption(optA));
        assertTrue(commandLine.hasOption(optB));
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testProcessAnonymousArgument() throws OptionException {
        final List args = new ArrayList();
        args.add("filename.txt");

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);

        assertTrue(commandLine.hasOption(arg1));
        assertEquals("filename.txt", commandLine.getValue(arg1));
    }

    @Test
    public void testProcessNonOptionWithoutAnonymous() throws OptionException {
        final List options = new ArrayList();
        options.add(optA);
        final GroupImpl noAnonGroup = new GroupImpl(options, "noAnon", "no anon", 0, 1);

        final List args = new ArrayList();
        args.add("filename.txt");

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(noAnonGroup, new ArrayList());
        final ListIterator iterator = args.listIterator();

        noAnonGroup.process(commandLine, iterator);

        assertFalse(commandLine.hasOption(optA));
        assertTrue(iterator.hasNext());
        assertEquals("filename.txt", iterator.next());
    }

    @Test
    public void testProcessUnknownOptionAbort() throws OptionException {
        final List args = new ArrayList();
        args.add("-unknown");

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);

        assertFalse(commandLine.hasOption(optA));
        assertFalse(commandLine.hasOption(optB));
        assertTrue(iterator.hasNext());
        assertEquals("-unknown", iterator.next());
    }

    @Test
    public void testProcessBurstingOption() throws OptionException {
        final List args = new ArrayList();
        args.add("-ab");

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        group.process(commandLine, iterator);

        assertTrue(commandLine.hasOption(optA));
        assertTrue(commandLine.hasOption(optB));
    }

    @Test
    public void testProcessRepeatedArgumentLoopProtection() throws OptionException {
        final List args = new ArrayList();
        args.add("-a");

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        final ListIterator iterator = args.listIterator();

        iterator.next();
        iterator.previous();

        group.process(commandLine, iterator);
        assertTrue(commandLine.hasOption(optA));
    }

    @Test
    public void testValidateSuccess() throws OptionException {
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optA);

        group.validate(commandLine);
    }

    @Test
    public void testValidateMissingOption() {
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());

        try {
            group.validate(commandLine);
            fail("Expected OptionException for missing option");
        } catch (final OptionException e) {
            assertEquals(ResourceConstants.MISSING_OPTION, e.getMessageKey());
            assertSame(group, e.getOption());
        }
    }

    @Test
    public void testValidateUnexpectedTokenTooManyOptions() {
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(group, new ArrayList());
        commandLine.addOption(optA);
        commandLine.addOption(optB);

        final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
        final Option optExtra = obuilder.withShortName("e").withLongName("extra").create();
        final List options = new ArrayList();
        options.add(optA);
        options.add(optB);
        options.add(optExtra);

        final GroupImpl strictGroup = new GroupImpl(options, "strictGroup", "strict", 0, 1);
        try {
            strictGroup.validate(commandLine);
            fail("Expected OptionException for unexpected token");
        } catch (final OptionException e) {
            assertEquals(ResourceConstants.UNEXPECTED_TOKEN, e.getMessageKey());
            assertSame(strictGroup, e.getOption());
        }
    }

    @Test
    public void testValidateChildRequiredOption() {
        final List options = new ArrayList();
        options.add(optC);

        final GroupImpl reqGroup = new GroupImpl(options, "reqGroup", "required child", 0, 2);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(reqGroup, new ArrayList());

        try {
            reqGroup.validate(commandLine);
            fail("Expected OptionException because child option is required");
        } catch (final OptionException e) {
            assertEquals(ResourceConstants.MISSING_OPTION, e.getMessageKey());
        }
    }

    @Test
    public void testValidateChildGroup() throws OptionException {
        final List childOptions = new ArrayList();
        childOptions.add(optA);
        final GroupImpl childGroup = new GroupImpl(childOptions, "childGroup", "child group", 1, 1);

        final List parentOptions = new ArrayList();
        parentOptions.add(childGroup);
        final GroupImpl parentGroup = new GroupImpl(parentOptions, "parentGroup", "parent group", 0, 1);

        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(parentGroup, new ArrayList());

        try {
            parentGroup.validate(commandLine);
            fail("Expected validation to fail due to child group missing required options");
        } catch (final OptionException e) {
            assertEquals(ResourceConstants.MISSING_OPTION, e.getMessageKey());
        }

        commandLine.addOption(optA);
        parentGroup.validate(commandLine);
    }

    @Test
    public void testValidateAnonymousArgument() {
        final List options = new ArrayList();
        options.add(arg2); // arg2 has minimum 1

        final GroupImpl anonGroup = new GroupImpl(options, "anonGroup", "anon group", 0, 2);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(anonGroup, new ArrayList());

        try {
            anonGroup.validate(commandLine);
            fail("Expected OptionException because anonymous argument is missing values");
        } catch (final OptionException e) {
            assertEquals(ResourceConstants.MISSING_OPTION, e.getMessageKey());
        }

        commandLine.addValue(arg2, "output.txt");
        try {
            anonGroup.validate(commandLine);
        } catch (final OptionException e) {
            fail("Should validate successfully after providing value to anonymous argument");
        }
    }

    @Test
    public void testFindOption() {
        assertNotNull(group.findOption("-a"));
        assertEquals(optA, group.findOption("-a"));
        assertEquals(optA, group.findOption("--all"));
        assertEquals(optB, group.findOption("-b"));
        assertEquals(optB, group.findOption("--batch"));
        assertNull(group.findOption("-c"));
        assertNull(group.findOption(null));
    }

    @Test
    public void testFindOptionNested() {
        final List childOptions = new ArrayList();
        childOptions.add(optA);
        final GroupImpl childGroup = new GroupImpl(childOptions, "childGroup", "child", 0, 1);

        final List parentOptions = new ArrayList();
        parentOptions.add(childGroup);
        parentOptions.add(optB);
        final GroupImpl parentGroup = new GroupImpl(parentOptions, "parentGroup", "parent", 0, 2);

        assertEquals(optA, parentGroup.findOption("-a"));
        assertEquals(optB, parentGroup.findOption("-b"));
        assertNull(parentGroup.findOption("-z"));
    }

    @Test
    public void testAppendUsageDefault() {
        final StringBuffer buffer = new StringBuffer();
        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        group.appendUsage(buffer, helpSettings, null);

        final String usage = buffer.toString();
        assertTrue(usage.startsWith("testGroup ("));
        assertTrue(usage.contains("-a|--all"));
        assertTrue(usage.contains("|"));
        assertTrue(usage.contains("-b|--batch"));
        assertTrue(usage.endsWith(")"));
    }

    @Test
    public void testAppendUsageOptionalAndOuter() {
        final List options = new ArrayList();
        options.add(optA);
        options.add(optB);
        options.add(arg1);
        final GroupImpl optionalGroup = new GroupImpl(options, "optGroup", "desc", 0, 2);

        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_OUTER);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final StringBuffer buffer = new StringBuffer();
        optionalGroup.appendUsage(buffer, helpSettings, null, " or ");

        final String usage = buffer.toString();
        assertTrue(usage.startsWith("["));
        assertTrue(usage.contains(" or "));
        assertTrue(usage.contains("[file]"));
        assertTrue(usage.endsWith("]"));
    }

    @Test
    public void testAppendUsageOptionalAndInner() {
        final List options = new ArrayList();
        options.add(optA);
        options.add(arg1);
        final GroupImpl optionalGroup = new GroupImpl(options, "optGroup", "desc", 0, 2);

        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_OPTIONAL);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final StringBuffer buffer = new StringBuffer();
        optionalGroup.appendUsage(buffer, helpSettings, null);

        final String usage = buffer.toString();
        assertTrue(usage.startsWith("["));
        assertTrue(usage.endsWith("]"));
    }

    @Test
    public void testAppendUsageWithComparator() {
        final StringBuffer buffer = new StringBuffer();
        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final Comparator reverseComp = new Comparator() {
            public int compare(final Object o1, final Object o2) {
                final Option op1 = (Option) o1;
                final Option op2 = (Option) o2;
                return op2.getPreferredName().compareTo(op1.getPreferredName());
            }
        };

        group.appendUsage(buffer, helpSettings, reverseComp);
        final String usage = buffer.toString();
        assertTrue(usage.indexOf("-b") < usage.indexOf("-a"));
    }

    @Test
    public void testAppendUsageCollapsed() {
        final StringBuffer buffer = new StringBuffer();
        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_NAME);

        group.appendUsage(buffer, helpSettings, null);
        assertEquals("testGroup", buffer.toString());
    }

    @Test
    public void testHelpLines() {
        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        final List lines = group.helpLines(0, helpSettings, null);
        assertNotNull(lines);
        assertFalse(lines.isEmpty());

        boolean hasGroupName = false;
        for (final Object lineObj : lines) {
            final HelpLine helpLine = (HelpLine) lineObj;
            if (helpLine.getOption().equals(group)) {
                hasGroupName = true;
                assertEquals(0, helpLine.getIndent());
            }
        }
        assertTrue(hasGroupName);
    }

    @Test
    public void testHelpLinesWithComparator() {
        final Set helpSettings = new HashSet();
        helpSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);

        final Comparator comp = new Comparator() {
            public int compare(final Object o1, final Object o2) {
                final Option op1 = (Option) o1;
                final Option op2 = (Option) o2;
                return op2.getPreferredName().compareTo(op1.getPreferredName());
            }
        };

        final List lines = group.helpLines(1, helpSettings, comp);
        assertNotNull(lines);
        assertTrue(lines.size() >= 2);
    }

    @Test
    public void testDefaults() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final Argument defaultArg = abuilder.withName("def").withDefault("defaultValue").create();

        final List options = new ArrayList();
        options.add(optA);
        options.add(defaultArg);

        final GroupImpl groupWithDefaults = new GroupImpl(options, "groupWithDefaults", "desc", 0, 1);
        final WriteableCommandLine commandLine = new WriteableCommandLineImpl(groupWithDefaults, new ArrayList());

        groupWithDefaults.defaults(commandLine);

        assertTrue(commandLine.hasOption(defaultArg));
        assertEquals("defaultValue", commandLine.getValue(defaultArg));
    }

    @Test
    public void testReverseStringComparator() {
        final Comparator comp = ReverseStringComparator.getInstance();
        assertNotNull(comp);
        assertSame(comp, ReverseStringComparator.getInstance());

        assertTrue(comp.compare("a", "b") > 0);
        assertTrue(comp.compare("b", "a") < 0);
        assertEquals(0, comp.compare("same", "same"));
    }

    @Test
    public void testGroupBuilderCompatibility() {
        final GroupBuilder gbuilder = new GroupBuilder();
        final Group built = gbuilder.withName("built")
                                    .withDescription("built group")
                                    .withMinimum(1)
                                    .withMaximum(2)
                                    .withOption(optA)
                                    .withOption(optB)
                                    .withOption(arg1)
                                    .create();

        assertTrue(built instanceof GroupImpl);
        final GroupImpl builtGroup = (GroupImpl) built;
        assertEquals("built", builtGroup.getPreferredName());
        assertEquals(2, builtGroup.getOptions().size());
        assertEquals(1, builtGroup.getAnonymous().size());
    }
}