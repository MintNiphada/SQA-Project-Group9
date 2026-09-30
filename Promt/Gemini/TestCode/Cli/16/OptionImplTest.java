package org.apache.commons.cli2.option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.DisplaySetting;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.WriteableCommandLine;
import org.junit.Assert;
import org.junit.Test;

public class OptionImplTest {

    private static class DummyOption extends OptionImpl {
        private String preferredName;
        private String description;
        private Set prefixes = new HashSet();
        private Set triggers = new HashSet();
        private boolean canProcessResult = true;

        public DummyOption(int id, boolean required) {
            super(id, required);
        }

        public void setPreferredName(String preferredName) {
            this.preferredName = preferredName;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setPrefixes(Set prefixes) {
            this.prefixes = prefixes;
        }

        public void setTriggers(Set triggers) {
            this.triggers = triggers;
        }

        public void setCanProcessResult(boolean canProcessResult) {
            this.canProcessResult = canProcessResult;
        }

        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return canProcessResult;
        }

        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
            if (preferredName != null) {
                buffer.append(preferredName);
            } else {
                buffer.append("dummy");
            }
        }

        public String getPreferredName() {
            return preferredName;
        }

        public String getDescription() {
            return description;
        }

        public Set getPrefixes() {
            return prefixes;
        }

        public Set getTriggers() {
            return triggers;
        }

        public void validate(WriteableCommandLine commandLine) throws OptionException {
        }

        public void process(WriteableCommandLine commandLine, ListIterator arguments) throws OptionException {
        }

        public List helpLines(int depth, Set helpSettings, Comparator comp) {
            return Collections.EMPTY_LIST;
        }

        public void testCheckPrefixes(Set prefixes) {
            super.checkPrefixes(prefixes);
        }
    }

    @Test
    public void testConstructorAndGetters() {
        DummyOption option = new DummyOption(42, true);
        Assert.assertEquals(42, option.getId());
        Assert.assertTrue(option.isRequired());

        DummyOption option2 = new DummyOption(0, false);
        Assert.assertEquals(0, option2.getId());
        Assert.assertFalse(option2.isRequired());
    }

    @Test
    public void testCanProcessWithListIterator() {
        DummyOption option = new DummyOption(1, false);
        List args = new ArrayList();
        args.add("--test");
        args.add("value");

        ListIterator iterator = args.listIterator();
        Assert.assertEquals(0, iterator.nextIndex());

        option.setCanProcessResult(true);
        Assert.assertTrue(option.canProcess(null, iterator));
        // Verify iterator cursor did not advance permanently
        Assert.assertEquals(0, iterator.nextIndex());

        option.setCanProcessResult(false);
        Assert.assertFalse(option.canProcess(null, iterator));
        Assert.assertEquals(0, iterator.nextIndex());

        // Exhaust iterator
        while (iterator.hasNext()) {
            iterator.next();
        }
        Assert.assertFalse(option.canProcess(null, iterator));
    }

    @Test
    public void testToString() {
        DummyOption option = new DummyOption(1, false);
        option.setPreferredName("--opt");
        Assert.assertEquals("--opt", option.toString());

        DummyOption optionNullName = new DummyOption(2, false);
        Assert.assertEquals("dummy", optionNullName.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        DummyOption opt1 = new DummyOption(1, false);
        DummyOption opt2 = new DummyOption(1, false);

        Set prefixes1 = new HashSet();
        prefixes1.add("-");
        prefixes1.add("--");
        Set triggers1 = new HashSet();
        triggers1.add("-o");
        triggers1.add("--option");

        opt1.setPreferredName("--option");
        opt1.setDescription("An option");
        opt1.setPrefixes(prefixes1);
        opt1.setTriggers(triggers1);

        Set prefixes2 = new HashSet();
        prefixes2.add("-");
        prefixes2.add("--");
        Set triggers2 = new HashSet();
        triggers2.add("-o");
        triggers2.add("--option");

        opt2.setPreferredName("--option");
        opt2.setDescription("An option");
        opt2.setPrefixes(prefixes2);
        opt2.setTriggers(triggers2);

        Assert.assertTrue(opt1.equals(opt1));
        Assert.assertTrue(opt1.equals(opt2));
        Assert.assertTrue(opt2.equals(opt1));
        Assert.assertEquals(opt1.hashCode(), opt2.hashCode());

        Assert.assertFalse(opt1.equals(null));
        Assert.assertFalse(opt1.equals("a string"));

        // Different ID
        DummyOption optDiffId = new DummyOption(2, false);
        optDiffId.setPreferredName("--option");
        optDiffId.setDescription("An option");
        optDiffId.setPrefixes(prefixes1);
        optDiffId.setTriggers(triggers1);
        Assert.assertFalse(opt1.equals(optDiffId));

        // Different Preferred Name
        DummyOption optDiffName = new DummyOption(1, false);
        optDiffName.setPreferredName("--other");
        optDiffName.setDescription("An option");
        optDiffName.setPrefixes(prefixes1);
        optDiffName.setTriggers(triggers1);
        Assert.assertFalse(opt1.equals(optDiffName));

        // Null Preferred Name in one
        DummyOption optNullName = new DummyOption(1, false);
        optNullName.setPreferredName(null);
        optNullName.setDescription("An option");
        optNullName.setPrefixes(prefixes1);
        optNullName.setTriggers(triggers1);
        Assert.assertFalse(opt1.equals(optNullName));
        Assert.assertFalse(optNullName.equals(opt1));

        // Different Description
        DummyOption optDiffDesc = new DummyOption(1, false);
        optDiffDesc.setPreferredName("--option");
        optDiffDesc.setDescription("Different description");
        optDiffDesc.setPrefixes(prefixes1);
        optDiffDesc.setTriggers(triggers1);
        Assert.assertFalse(opt1.equals(optDiffDesc));

        // Null Description in one
        DummyOption optNullDesc = new DummyOption(1, false);
        optNullDesc.setPreferredName("--option");
        optNullDesc.setDescription(null);
        optNullDesc.setPrefixes(prefixes1);
        optNullDesc.setTriggers(triggers1);
        Assert.assertFalse(opt1.equals(optNullDesc));
        Assert.assertFalse(optNullDesc.equals(opt1));

        // Different prefixes
        DummyOption optDiffPrefixes = new DummyOption(1, false);
        optDiffPrefixes.setPreferredName("--option");
        optDiffPrefixes.setDescription("An option");
        Set prefixesDiff = new HashSet();
        prefixesDiff.add("/");
        optDiffPrefixes.setPrefixes(prefixesDiff);
        optDiffPrefixes.setTriggers(triggers1);
        Assert.assertFalse(opt1.equals(optDiffPrefixes));

        // Different triggers
        DummyOption optDiffTriggers = new DummyOption(1, false);
        optDiffTriggers.setPreferredName("--option");
        optDiffTriggers.setDescription("An option");
        optDiffTriggers.setPrefixes(prefixes1);
        Set triggersDiff = new HashSet();
        triggersDiff.add("-x");
        optDiffTriggers.setTriggers(triggersDiff);
        Assert.assertFalse(opt1.equals(optDiffTriggers));

        // Both with null preferredName and description
        DummyOption optNullBoth1 = new DummyOption(1, false);
        optNullBoth1.setPreferredName(null);
        optNullBoth1.setDescription(null);
        optNullBoth1.setPrefixes(prefixes1);
        optNullBoth1.setTriggers(triggers1);

        DummyOption optNullBoth2 = new DummyOption(1, false);
        optNullBoth2.setPreferredName(null);
        optNullBoth2.setDescription(null);
        optNullBoth2.setPrefixes(prefixes2);
        optNullBoth2.setTriggers(triggers2);

        Assert.assertTrue(optNullBoth1.equals(optNullBoth2));
        Assert.assertEquals(optNullBoth1.hashCode(), optNullBoth2.hashCode());
    }

    @Test
    public void testFindOption() {
        DummyOption option = new DummyOption(1, false);
        Set triggers = new HashSet();
        triggers.add("-a");
        triggers.add("--all");
        option.setTriggers(triggers);

        Assert.assertSame(option, option.findOption("-a"));
        Assert.assertSame(option, option.findOption("--all"));
        Assert.assertNull(option.findOption("-b"));
        Assert.assertNull(option.findOption(null));
    }

    @Test
    public void testDefaults() {
        DummyOption option = new DummyOption(1, false);
        option.defaults((WriteableCommandLine) null);
    }

    @Test
    public void testCheckPrefixesEmptySet() {
        DummyOption option = new DummyOption(1, false);
        option.setPreferredName("invalid");
        Set emptyPrefixes = new HashSet();
        option.testCheckPrefixes(emptyPrefixes);
    }

    @Test
    public void testCheckPrefixesValid() {
        DummyOption option = new DummyOption(1, false);
        option.setPreferredName("--valid");
        Set triggers = new HashSet();
        triggers.add("--valid");
        triggers.add("-v");
        option.setTriggers(triggers);

        Set prefixes = new HashSet();
        prefixes.add("--");
        prefixes.add("-");

        option.testCheckPrefixes(prefixes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixesInvalidPreferredName() {
        DummyOption option = new DummyOption(1, false);
        option.setPreferredName("invalid");
        Set triggers = new HashSet();
        triggers.add("-v");
        option.setTriggers(triggers);

        Set prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");

        option.testCheckPrefixes(prefixes);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckPrefixesInvalidTrigger() {
        DummyOption option = new DummyOption(1, false);
        option.setPreferredName("--valid");
        Set triggers = new HashSet();
        triggers.add("--valid");
        triggers.add("invalidTrigger");
        option.setTriggers(triggers);

        Set prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");

        option.testCheckPrefixes(prefixes);
    }
}