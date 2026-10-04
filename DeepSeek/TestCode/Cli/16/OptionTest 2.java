package org.apache.commons.cli2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Option interface using a concrete implementation.
 */
public class OptionTest {

    // Concrete implementation of WriteableCommandLine for testing.
    private static class MockWriteableCommandLine implements WriteableCommandLine {
        private final List<Option> options = new ArrayList<Option>();
        private boolean optionAdded;

        @Override
        public void addOption(Option option) {
            options.add(option);
            optionAdded = true;
        }

        @Override
        public void addValue(Option option, Object value) {
            // no-op for testing
        }

        @Override
        public void addSwitch(Option option, boolean value) {
            // no-op
        }

        @Override
        public boolean hasOption(Option option) {
            return options.contains(option);
        }

        @Override
        public Object getValue(Option option) {
            return null;
        }

        @Override
        public List getValues(Option option) {
            return Collections.emptyList();
        }

        @Override
        public Boolean getSwitch(Option option) {
            return null;
        }

        @Override
        public Set getOptions() {
            return new HashSet(options);
        }

        @Override
        public List getUndefaultedValues() {
            return Collections.emptyList();
        }

        public boolean isOptionAdded() {
            return optionAdded;
        }
    }

    // Concrete implementation of Option for testing.
    private static class TestOption implements Option {
        private final Set<String> triggers;
        private final Set<String> prefixes;
        private final String preferredName;
        private final String description;
        private final int id;
        private final boolean required;
        private final Option parent;
        private final Option findOptionReturn;
        private final boolean canProcessString;
        private final boolean canProcessIterator;
        private boolean processCalled;
        private int lastProcessedCount;

        public TestOption(Builder builder) {
            this.triggers = builder.triggers != null ? builder.triggers : new HashSet<String>();
            this.prefixes = builder.prefixes != null ? builder.prefixes : new HashSet<String>();
            this.preferredName = builder.preferredName;
            this.description = builder.description;
            this.id = builder.id;
            this.required = builder.required;
            this.parent = builder.parent;
            this.findOptionReturn = builder.findOptionReturn;
            this.canProcessString = builder.canProcessString;
            this.canProcessIterator = builder.canProcessIterator;
        }

        @Override
        public void process(WriteableCommandLine commandLine, ListIterator args) throws OptionException {
            processCalled = true;
            if (commandLine == null || args == null) {
                throw new NullPointerException("Arguments must not be null");
            }
            if (!args.hasNext()) {
                throw new OptionException("No arguments to process");
            }
            // consume at least one argument
            Object arg = args.next();
            lastProcessedCount = 1;
            // optionally consume more
        }

        @Override
        public void defaults(WriteableCommandLine commandLine) {
            // apply defaults if needed
        }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return canProcessString;
        }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, ListIterator arguments) {
            // return result and restore iterator position
            // For simplicity, return constant; test will verify iterator state.
            return canProcessIterator;
        }

        @Override
        public Set getTriggers() {
            return triggers;
        }

        @Override
        public Set getPrefixes() {
            return prefixes;
        }

        @Override
        public void validate(WriteableCommandLine commandLine) throws OptionException {
            if (required && (commandLine == null || !commandLine.hasOption(this))) {
                throw new OptionException("Required option missing");
            }
        }

        @Override
        public List helpLines(int depth, Set helpSettings, Comparator comp) {
            List<HelpLine> lines = new ArrayList<HelpLine>();
            lines.add(new HelpLineImpl(this, depth)); // simple line
            return lines;
        }

        @Override
        public void appendUsage(StringBuffer buffer, Set helpSettings, Comparator comp) {
            buffer.append(" [--option]");
        }

        @Override
        public String getPreferredName() {
            return preferredName;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public Option findOption(String trigger) {
            return findOptionReturn;
        }

        @Override
        public boolean isRequired() {
            return required;
        }

        // Builder pattern for easy creation.
        static class Builder {
            Set<String> triggers;
            Set<String> prefixes;
            String preferredName = "test";
            String description = "test option";
            int id = 1;
            boolean required = false;
            Option parent = null;
            Option findOptionReturn = null;
            boolean canProcessString = false;
            boolean canProcessIterator = false;

            public Builder withTriggers(String... triggers) {
                this.triggers = new HashSet<String>(Arrays.asList(triggers));
                return this;
            }

            public Builder withPrefixes(String... prefixes) {
                this.prefixes = new HashSet<String>(Arrays.asList(prefixes));
                return this;
            }

            public Builder withPreferredName(String name) {
                this.preferredName = name;
                return this;
            }

            public Builder withDescription(String desc) {
                this.description = desc;
                return this;
            }

            public Builder withId(int id) {
                this.id = id;
                return this;
            }

            public Builder withRequired(boolean required) {
                this.required = required;
                return this;
            }

            public Builder withFindOptionReturn(Option result) {
                this.findOptionReturn = result;
                return this;
            }

            public Builder withCanProcessString(boolean can) {
                this.canProcessString = can;
                return this;
            }

            public Builder withCanProcessIterator(boolean can) {
                this.canProcessIterator = can;
                return this;
            }

            public TestOption build() {
                return new TestOption(this);
            }
        }
    }

    // Tests for getTriggers()
    @Test
    public void testGetTriggersNonNull() {
        Option option = new TestOption.Builder().build();
        Assert.assertNotNull(option.getTriggers());
    }

    @Test
    public void testGetTriggersContainsExpected() {
        Option option = new TestOption.Builder().withTriggers("--test", "-t").build();
        Set triggers = option.getTriggers();
        Assert.assertTrue(triggers.contains("--test"));
        Assert.assertTrue(triggers.contains("-t"));
        Assert.assertEquals(2, triggers.size());
    }

    // Tests for getPrefixes()
    @Test
    public void testGetPrefixesNonNull() {
        Option option = new TestOption.Builder().build();
        Assert.assertNotNull(option.getPrefixes());
    }

    @Test
    public void testGetPrefixesCustom() {
        Option option = new TestOption.Builder().withPrefixes("--", "-").build();
        Set prefixes = option.getPrefixes();
        Assert.assertEquals(2, prefixes.size());
        Assert.assertTrue(prefixes.contains("--"));
        Assert.assertTrue(prefixes.contains("-"));
    }

    // Tests for getPreferredName()
    @Test
    public void testGetPreferredName() {
        Option option = new TestOption.Builder().withPreferredName("verbose").build();
        Assert.assertEquals("verbose", option.getPreferredName());
    }

    // Tests for getDescription()
    @Test
    public void testGetDescription() {
        Option option = new TestOption.Builder().withDescription("Enable verbose output").build();
        Assert.assertEquals("Enable verbose output", option.getDescription());
    }

    // Tests for getId()
    @Test
    public void testGetId() {
        Option option = new TestOption.Builder().withId(42).build();
        Assert.assertEquals(42, option.getId());
    }

    // Tests for isRequired()
    @Test
    public void testIsRequiredTrue() {
        Option option = new TestOption.Builder().withRequired(true).build();
        Assert.assertTrue(option.isRequired());
    }

    @Test
    public void testIsRequiredFalse() {
        Option option = new TestOption.Builder().withRequired(false).build();
        Assert.assertFalse(option.isRequired());
    }

    // Tests for findOption()
    @Test
    public void testFindOptionReturnsNull() {
        Option option = new TestOption.Builder().build();
        Assert.assertNull(option.findOption("--trigger"));
    }

    @Test
    public void testFindOptionReturnsGivenOption() {
        Option returned = new TestOption.Builder().withPreferredName("found").build();
        Option option = new TestOption.Builder().withFindOptionReturn(returned).build();
        Assert.assertSame(returned, option.findOption("any"));
    }

    // Tests for process()
    @Test
    public void testProcessConsumesArgument() throws OptionException {
        WriteableCommandLine commandLine = new MockWriteableCommandLine();
        List<String> argList = new ArrayList<String>(Arrays.asList("arg1", "arg2"));
        ListIterator<String> args = argList.listIterator();
        Option option = new TestOption.Builder().build();
        option.process(commandLine, args);
        // Should have moved forward by one
        Assert.assertEquals(1, args.nextIndex());
    }

    @Test(expected = OptionException.class)
    public void testProcessWithEmptyArgsThrowsOptionException() throws OptionException {
        WriteableCommandLine commandLine = new MockWriteableCommandLine();
        ListIterator<String> args = Collections.<String>emptyList().listIterator();
        Option option = new TestOption.Builder().build();
        option.process(commandLine, args);
    }

    @Test(expected = NullPointerException.class)
    public void testProcessWithNullCommandLine() throws OptionException {
        ListIterator<String> args = new ArrayList<String>(Arrays.asList("arg")).listIterator();
        Option option = new TestOption.Builder().build();
        option.process(null, args);
    }

    // Tests for defaults()
    @Test
    public void testDefaultsDoesNotThrow() {
        WriteableCommandLine commandLine = new MockWriteableCommandLine();
        Option option = new TestOption.Builder().build();
        option.defaults(commandLine); // should not throw
    }

    // Tests for canProcess(String)
    @Test
    public void testCanProcessStringTrue() {
        Option option = new TestOption.Builder().withCanProcessString(true).build();
        Assert.assertTrue(option.canProcess(new MockWriteableCommandLine(), "any"));
    }

    @Test
    public void testCanProcessStringFalse() {
        Option option = new TestOption.Builder().withCanProcessString(false).build();
        Assert.assertFalse(option.canProcess(new MockWriteableCommandLine(), "any"));
    }

    // Tests for canProcess(ListIterator)
    @Test
    public void testCanProcessIteratorReturnsTrue() {
        Option option = new TestOption.Builder().withCanProcessIterator(true).build();
        ListIterator<String> args = new ArrayList<String>(Arrays.asList("arg")).listIterator();
        Assert.assertTrue(option.canProcess(new MockWriteableCommandLine(), args));
    }

    @Test
    public void testCanProcessIteratorDoesNotMoveIterator() {
        Option option = new TestOption.Builder().withCanProcessIterator(true).build();
        List<String> argList = new ArrayList<String>(Arrays.asList("one", "two"));
        ListIterator<String> args = argList.listIterator();
        int initialNextIndex = args.nextIndex();
        option.canProcess(new MockWriteableCommandLine(), args);
        Assert.assertEquals("Iterator should not have moved", initialNextIndex, args.nextIndex());
        Assert.assertEquals("one", args.next()); // still at first element
    }

    // Tests for validate()
    @Test
    public void testValidateRequiredMissingOptionThrows() throws OptionException {
        Option option = new TestOption.Builder().withRequired(true).build();
        WriteableCommandLine commandLine = new MockWriteableCommandLine();
        try {
            option.validate(commandLine);
            Assert.fail("Expected OptionException for missing required option");
        } catch (OptionException e) {
            // expected
        }
    }

    @Test
    public void testValidateRequiredOptionPresentSucceeds() throws OptionException {
        Option option = new TestOption.Builder().withRequired(true).build();
        MockWriteableCommandLine commandLine = new MockWriteableCommandLine();
        commandLine.addOption(option); // make it present
        option.validate(commandLine); // should not throw
    }

    @Test
    public void testValidateNotRequiredSucceeds() throws OptionException {
        Option option = new TestOption.Builder().withRequired(false).build();
        WriteableCommandLine commandLine = new MockWriteableCommandLine();
        option.validate(commandLine); // no exception
    }

    // Tests for helpLines()
    @Test
    public void testHelpLinesReturnsList() {
        Option option = new TestOption.Builder().build();
        List helpLines = option.helpLines(0, new HashSet(), null);
        Assert.assertNotNull(helpLines);
        Assert.assertFalse(helpLines.isEmpty());
        Assert.assertTrue(helpLines.get(0) instanceof HelpLine);
    }

    // Tests for appendUsage()
    @Test
    public void testAppendUsageAppendsToStringBuffer() {
        Option option = new TestOption.Builder().build();
        StringBuffer buffer = new StringBuffer();
        option.appendUsage(buffer, new HashSet(), null);
        Assert.assertTrue(buffer.toString().contains("[--option]"));
    }

    // Test that triggers set is not null by default
    @Test
    public void testGetTriggersDefaultNotNull() {
        Option option = new TestOption.Builder().build();
        Assert.assertNotNull(option.getTriggers());
    }

    // Test that prefixes set is not null by default
    @Test
    public void testGetPrefixesDefaultNotNull() {
        Option option = new TestOption.Builder().build();
        Assert.assertNotNull(option.getPrefixes());
    }
}

// Additional dummy HelpLineImpl to make compilation succeed (not part of the interface)
class HelpLineImpl implements HelpLine {
    private final Option option;
    private final int depth;

    HelpLineImpl(Option option, int depth) {
        this.option = option;
        this.depth = depth;
    }

    // minimal implementation to satisfy the interface; not tested.
    public Option getOption() { return option; }
    public int getDepth() { return depth; }
    // The HelpLine interface has other methods; add stubs for compilation.
    // For brevity, we'll just add the necessary methods or leave it incomplete? 
    // To avoid errors, we'll implement as needed.
    public String usage() { return option.getPreferredName(); }
}
interface HelpLine {
    Option getOption();
    int getDepth();
    String usage();
}
