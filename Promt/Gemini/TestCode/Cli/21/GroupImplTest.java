package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.HelpLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.junit.Assert;
import org.junit.Test;

public class GroupImplTest {

    private static class DummyCommandLine implements WriteableCommandLine {
        private final Set presentOptions = new HashSet();
        private boolean looksLikeOption = false;

        public void addOption(Option option) {
            presentOptions.add(option);
        }

        public void setLooksLikeOption(boolean looksLikeOption) {
            this.looksLikeOption = looksLikeOption;
        }

        public boolean hasOption(Option option) {
            return presentOptions.contains(option);
        }

        public boolean hasOption(String trigger) {
            return false;
        }

        public Option getOption(String trigger) {
            return null;
        }

        public List getValues(Option option) {
            return Collections.EMPTY_LIST;
        }

        public List getValues(Option option, List defaultValues) {
            return defaultValues;
        }

        public List getValues(String trigger) {
            return Collections.EMPTY_LIST;
        }

        public List getValues(String trigger, List defaultValues) {
            return defaultValues;
        }

        public Object getValue(Option option) {
            return null;
        }

        public Object getValue(Option option, Object defaultValue) {
            return defaultValue;
        }

        public Object getValue(String trigger) {
            return null;
        }

        public Object getValue(String trigger, Object defaultValue) {
            return defaultValue;
        }

        public Boolean getSwitch(Option option) {
            return null;
        }

        public Boolean getSwitch(Option option, Boolean defaultValue) {
            return defaultValue;
        }

        public Boolean getSwitch(String trigger) {
            return null;
        }

        public Boolean getSwitch(String trigger, Boolean defaultValue) {
            return defaultValue;
        }

        public int getOptionCount(Option option) {
            return presentOptions.contains(option) ? 1 : 0;
        }

        public int getOptionCount(String trigger) {
            return 0;
        }

        public List getOptions() {
            return new ArrayList(presentOptions);
        }

        public Set getOptionTriggers() {
            return Collections.EMPTY_SET;
        }

        public void addValue(Option option, Object value) {}

        public void addSwitch(Option option, boolean value) {}

        public void setDefaultValues(Option option, List defaultValues) {}

        public void setDefaultSwitch(Option option, Boolean defaultSwitch) {}

        public boolean looksLikeOption(String trigger) {
            return looksLikeOption || (trigger != null && trigger.startsWith("-"));
        }

        public List getUndeterminedOptions() {
            return Collections.EMPTY_LIST;
        }
    }

    private static class DummyOption extends OptionImpl {
        private final String preferredName;
        private final String description;
        private final Set triggers = new HashSet();
        private final Set prefixes = new HashSet();
        private boolean canProcess = true;
        private boolean processed = false;
        private boolean validated = false;
        private boolean defaulted = false;

        public DummyOption(String preferredName, String description, boolean required) {
            super(0, required);
            this.preferredName = preferredName;
            this.description = description;
            if (preferredName != null) {
                triggers.add(preferredName);
                prefixes.add(preferredName.substring(0, Math.min(1, preferredName.length())));
            }
        }

        public void addTrigger(String trigger) {
            triggers.add(trigger);
        }

        public void addPrefix(String prefix) {
            prefixes.add(prefix);
        }

        public void setCanProcess(boolean canProcess) {
            this.canProcess = canProcess;
        }

        public boolean isProcessed() {
            return processed;
        }

        public boolean isValidated() {
            return validated;
        }

        public boolean isDefaulted() {
            return defaulted;
        }

        public boolean canProcess(WriteableCommandLine commandLine, String arg) {
            return canProcess;
        }

        public Set getTriggers() {
            return triggers;
        }

        public Set getPrefixes() {
            return prefixes;
        }

        public void validate(WriteableCommandLine commandLine) throws OptionException {
            this.validated = true;
        }

        public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
            this.processed = true;
            if (arguments.hasNext()) {
                arguments.next();
            }
        }

        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
            buffer.append(preferredName);
        }

        public String getPreferredName() {
            return preferredName;
        }

        public String getDescription() {
            return description;
        }

        public List helpLines(int depth, Set helpSettings, Comparator comp) {
            List list = new ArrayList();
            list.add(new HelpLineImpl(this, depth));
            return list;
        }

        public void defaults(WriteableCommandLine commandLine) {
            super.defaults(commandLine);
            this.defaulted = true;
        }

        public Option findOption(String trigger) {
            if (triggers.contains(trigger)) {
                return this;
            }
            return null;
        }
    }

    private static class DummyArgument extends DummyOption implements Argument {
        public DummyArgument(String preferredName, String description, boolean required) {
            super(preferredName, description, required);
        }

        public boolean canProcess(WriteableCommandLine commandLine, ListIterator arguments) {
            return super.canProcess;
        }

        public void processValues(WriteableCommandLine commandLine, ListIterator arguments, Option option)
                throws OptionException {
            super.processed = true;
        }

        public String getInitialSeparator() {
            return "=";
        }

        public char getInitialSeparatorChar() {
            return '=';
        }

        public int getMaximum() {
            return 1;
        }

        public int getMinimum() {
            return 0;
        }

        public String stripInitialSeparator(String token) {
            return token;
        }

        public List defaultValues(Option option) {
            return Collections.EMPTY_LIST;
        }
    }

    @Test
    public void testConstructorAndGetters() {
        DummyOption opt1 = new DummyOption("--opt1", "Option 1", false);
        DummyOption opt2 = new DummyOption("--opt2", "Option 2", false);
        DummyArgument arg1 = new DummyArgument("arg1", "Argument 1", false);

        List options = new ArrayList();
        options.add(opt1);
        options.add(opt2);
        options.add(arg1);

        GroupImpl group = new GroupImpl(options, "group1", "Group Description", 1, 2, true);

        Assert.assertEquals("group1", group.getPreferredName());
        Assert.assertEquals("Group Description", group.getDescription());
        Assert.assertEquals(1, group.getMinimum());
        Assert.assertEquals(2, group.getMaximum());
        Assert.assertEquals(group, opt1.getParent());
        Assert.assertEquals(group, opt2.getParent());
        Assert.assertEquals(group, arg1.getParent());

        Assert.assertEquals(2, group.getOptions().size());
        Assert.assertTrue(group.getOptions().contains(opt1));
        Assert.assertTrue(group.getOptions().contains(opt2));
        Assert.assertEquals(1, group.getAnonymous().size());
        Assert.assertTrue(group.getAnonymous().contains(arg1));

        Set triggers = group.getTriggers();
        Assert.assertTrue(triggers.contains("--opt1"));
        Assert.assertTrue(triggers.contains("--opt2"));

        Set prefixes = group.getPrefixes();
        Assert.assertTrue(prefixes.contains("-"));
    }

    @Test
    public void testIsRequired() {
        DummyOption opt = new DummyOption("-a", "desc", false);
        List options = new ArrayList();
        options.add(opt);

        GroupImpl groupMin0 = new GroupImpl(new ArrayList(options), "g0", "d", 0, 1, false);
        Assert.assertFalse(groupMin0.isRequired());

        GroupImpl groupMin1 = new GroupImpl(new ArrayList(options), "g1", "d", 1, 1, false);
        Assert.assertTrue(groupMin1.isRequired());

        GroupImpl parentGroup = new GroupImpl(new ArrayList(), "parent", "d", 0, 1, false);
        groupMin1.setParent(parentGroup);
        Assert.assertFalse(groupMin1.isRequired());

        GroupImpl groupReqTrue = new GroupImpl(new ArrayList(options), "gReq", "d", 1, 1, true);
        groupReqTrue.setParent(parentGroup);
        Assert.assertTrue(groupReqTrue.isRequired());
    }

    @Test
    public void testCanProcess() {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption optA = new DummyOption("-a", "Option A", false);
        optA.setCanProcess(true);

        List options = new ArrayList();
        options.add(optA);

        GroupImpl group = new GroupImpl(options, "group", "desc", 0, 1, false);

        Assert.assertFalse(group.canProcess(cl, null));
        Assert.assertTrue(group.canProcess(cl, "-a"));

        optA.setCanProcess(false);
        Assert.assertTrue(group.canProcess(cl, "-a"));

        cl.setLooksLikeOption(true);
        Assert.assertFalse(group.canProcess(cl, "-unknown"));

        cl.setLooksLikeOption(false);
        Assert.assertFalse(group.canProcess(cl, "plainValue"));

        DummyArgument arg = new DummyArgument("file", "arg desc", false);
        List optionsWithArg = new ArrayList();
        optionsWithArg.add(arg);
        GroupImpl groupWithArg = new GroupImpl(optionsWithArg, "groupArg", "desc", 0, 1, false);

        Assert.assertTrue(groupWithArg.canProcess(cl, "plainValue"));
        cl.setLooksLikeOption(true);
        Assert.assertFalse(groupWithArg.canProcess(cl, "-something"));
    }

    @Test
    public void testProcessDirectOption() throws OptionException {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption optA = new DummyOption("-a", "Option A", false);
        List options = new ArrayList();
        options.add(optA);
        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1, false);

        List args = new ArrayList();
        args.add("-a");
        ListIterator it = args.listIterator();

        group.process(cl, it);
        Assert.assertTrue(optA.isProcessed());
        Assert.assertFalse(it.hasNext());
    }

    @Test
    public void testProcessRepeatedTokenAborts() throws OptionException {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption optA = new DummyOption("-a", "Option A", false) {
            public void process(WriteableCommandLine commandLine, ListIterator arguments) {
                // intentionally do not advance arguments
            }
        };
        List options = new ArrayList();
        options.add(optA);
        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1, false);

        List args = new ArrayList();
        args.add("-a");
        ListIterator it = args.listIterator();

        group.process(cl, it);
        Assert.assertEquals(0, it.nextIndex());
    }

    @Test
    public void testProcessBurstOptionFound() throws OptionException {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption optAbc = new DummyOption("-a", "Option A", false) {
            public boolean canProcess(WriteableCommandLine commandLine, String arg) {
                return "-abc".startsWith(arg) || arg.startsWith("-a");
            }
        };
        List options = new ArrayList();
        options.add(optAbc);
        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1, false);

        List args = new ArrayList();
        args.add("-abc");
        ListIterator it = args.listIterator();

        group.process(cl, it);
        Assert.assertTrue(optAbc.isProcessed());
    }

    @Test
    public void testProcessBurstOptionNotFound() throws OptionException {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption optA = new DummyOption("-a", "Option A", false);
        optA.setCanProcess(false);
        List options = new ArrayList();
        options.add(optA);
        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1, false);

        List args = new ArrayList();
        args.add("-unknown");
        ListIterator it = args.listIterator();

        group.process(cl, it);
        Assert.assertEquals(0, it.nextIndex());
        Assert.assertFalse(optA.isProcessed());
    }

    @Test
    public void testProcessNonOptionNoAnonymous() throws OptionException {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption optA = new DummyOption("-a", "Option A", false);
        List options = new ArrayList();
        options.add(optA);
        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1, false);

        List args = new ArrayList();
        args.add("nonOptionValue");
        ListIterator it = args.listIterator();

        group.process(cl, it);
        Assert.assertEquals(0, it.nextIndex());
    }

    @Test
    public void testProcessNonOptionWithAnonymous() throws OptionException {
        DummyCommandLine cl = new DummyCommandLine();
        DummyArgument arg = new DummyArgument("file", "File Arg", false);
        List options = new ArrayList();
        options.add(arg);
        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1, false);

        List args = new ArrayList();
        args.add("test.txt");
        ListIterator it = args.listIterator();

        group.process(cl, it);
        Assert.assertTrue(arg.isProcessed());
    }

    @Test
    public void testValidateSuccess() throws OptionException {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption opt1 = new DummyOption("-a", "A", true);
        DummyOption opt2 = new DummyOption("-b", "B", false);
        DummyArgument arg1 = new DummyArgument("arg", "Arg", false);

        List options = new ArrayList();
        options.add(opt1);
        options.add(opt2);
        options.add(arg1);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 1, 2, false);
        cl.addOption(opt1);

        group.validate(cl);
        Assert.assertTrue(opt1.isValidated());
        Assert.assertFalse(opt2.isValidated());
        Assert.assertTrue(arg1.isValidated());
    }

    @Test(expected = OptionException.class)
    public void testValidateTooManyOptions() throws OptionException {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption opt1 = new DummyOption("-a", "A", false);
        DummyOption opt2 = new DummyOption("-b", "B", false);

        List options = new ArrayList();
        options.add(opt1);
        options.add(opt2);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 1, false);
        cl.addOption(opt1);
        cl.addOption(opt2);

        group.validate(cl);
    }

    @Test(expected = OptionException.class)
    public void testValidateMissingOption() throws OptionException {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption opt1 = new DummyOption("-a", "A", false);

        List options = new ArrayList();
        options.add(opt1);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 1, 2, false);
        group.validate(cl);
    }

    @Test
    public void testFindOption() {
        DummyOption opt1 = new DummyOption("-a", "A", false);
        opt1.addTrigger("--all");
        DummyOption opt2 = new DummyOption("-b", "B", false);

        List options = new ArrayList();
        options.add(opt1);
        options.add(opt2);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2, false);

        Assert.assertSame(opt1, group.findOption("-a"));
        Assert.assertSame(opt1, group.findOption("--all"));
        Assert.assertSame(opt2, group.findOption("-b"));
        Assert.assertNull(group.findOption("-c"));
    }

    @Test
    public void testDefaults() {
        DummyCommandLine cl = new DummyCommandLine();
        DummyOption opt1 = new DummyOption("-a", "A", false);
        DummyArgument arg1 = new DummyArgument("arg", "Arg", false);

        List options = new ArrayList();
        options.add(opt1);
        options.add(arg1);

        GroupImpl group = new GroupImpl(options, "grp", "desc", 0, 2, false);
        group.defaults(cl);

        Assert.assertTrue(opt1.isDefaulted());
        Assert.assertTrue(arg1.isDefaulted());
    }

    @Test
    public void testAppendUsage() {
        DummyOption opt1 = new DummyOption("-a", "A", false);
        DummyOption opt2 = new DummyOption("-b", "B", false);
        DummyArgument arg1 = new DummyArgument("<file>", "File", false);

        List options = new ArrayList();
        options.add(opt1);
        options.add(opt2);
        options.add(arg1);

        GroupImpl group = new GroupImpl(options, "groupName", "Group Description", 0, 2, false);

        StringBuffer buffer = new StringBuffer();
        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_OPTIONAL);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        group.appendUsage(buffer, settings, null);
        Assert.assertEquals("[groupName (-a|-b) <file>]", buffer.toString());

        buffer = new StringBuffer();
        settings.add(DisplaySetting.DISPLAY_GROUP_OUTER);
        group.appendUsage(buffer, settings, null, ", ");
        Assert.assertEquals("[groupName (-a, -b)] <file>", buffer.toString());

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                Option optA = (Option) o1;
                Option optB = (Option) o2;
                return optB.getPreferredName().compareTo(optA.getPreferredName());
            }
        };
        buffer = new StringBuffer();
        Set simpleSettings = new HashSet();
        simpleSettings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        group.appendUsage(buffer, simpleSettings, comp);
        Assert.assertEquals("-b|-a", buffer.toString());

        GroupImpl noNameGroup = new GroupImpl(new ArrayList(Arrays.asList(new Option[]{opt1, opt2})), null, "desc", 1, 2, true);
        buffer = new StringBuffer();
        noNameGroup.appendUsage(buffer, Collections.EMPTY_SET, null);
        Assert.assertEquals("-a|-b", buffer.toString());
    }

    @Test
    public void testHelpLines() {
        DummyOption opt1 = new DummyOption("-a", "Option A description", false);
        DummyArgument arg1 = new DummyArgument("<file>", "File argument", false);

        List options = new ArrayList();
        options.add(opt1);
        options.add(arg1);

        GroupImpl group = new GroupImpl(options, "myGroup", "group desc", 0, 1, false);

        Set settings = new HashSet();
        settings.add(DisplaySetting.DISPLAY_GROUP_NAME);
        settings.add(DisplaySetting.DISPLAY_GROUP_EXPANDED);
        settings.add(DisplaySetting.DISPLAY_GROUP_ARGUMENT);

        List lines = group.helpLines(0, settings, null);
        Assert.assertEquals(3, lines.size());
        HelpLine line0 = (HelpLine) lines.get(0);
        Assert.assertEquals(0, line0.getIndent());
        Assert.assertSame(group, line0.getOption());

        Comparator comp = new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Option) o1).getPreferredName().compareTo(((Option) o2).getPreferredName());
            }
        };
        List sortedLines = group.helpLines(1, settings, comp);
        Assert.assertEquals(3, sortedLines.size());

        List emptyLines = group.helpLines(0, Collections.EMPTY_SET, null);
        Assert.assertEquals(0, emptyLines.size());
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