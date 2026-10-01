package org.apache.commons.cli2;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class WriteableCommandLineTest {

    private static class TestOption implements Option {
        private final String preferredName;
        private final Set<String> triggers;

        public TestOption(String preferredName) {
            this.preferredName = preferredName;
            this.triggers = new HashSet<String>();
            this.triggers.add(preferredName);
        }

        @Override
        public String getPreferredName() {
            return preferredName;
        }

        @Override
        public Set<String> getTriggers() {
            return triggers;
        }

        @Override
        public String getId() {
            return preferredName;
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public void defaults(WriteableCommandLine commandLine) {
        }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, String argument) {
            return false;
        }

        @Override
        public void process(WriteableCommandLine commandLine, ListIterator<String> arguments) {
        }

        @Override
        public void validate(WriteableCommandLine commandLine) {
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public int getMaximum() {
            return 0;
        }

        @Override
        public int getMinimum() {
            return 0;
        }

        @Override
        public boolean hasArgument() {
            return false;
        }

        @Override
        public boolean hasOptionalArgument() {
            return false;
        }

        @Override
        public ResourceBundle getResourceBundle() {
            return null;
        }

        @Override
        public String getKey() {
            return null;
        }

        @Override
        public boolean isHelpOption() {
            return false;
        }

        @Override
        public boolean isVersionOption() {
            return false;
        }

        @Override
        public boolean isPropertyOption() {
            return false;
        }

        @Override
        public boolean isSwitch() {
            return false;
        }

        @Override
        public boolean isGroup() {
            return false;
        }

        @Override
        public boolean isCommand() {
            return false;
        }

        @Override
        public boolean isArgument() {
            return false;
        }

        @Override
        public boolean isRequired(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public void setParent(Option parent) {
        }

        @Override
        public Option getParent() {
            return null;
        }

        @Override
        public List<Option> getChildren() {
            return null;
        }

        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public boolean isHidden() {
            return false;
        }

        @Override
        public boolean isAlias() {
            return false;
        }

        @Override
        public Option getPreferredAlias() {
            return null;
        }

        @Override
        public List<Option> getAliases() {
            return null;
        }

        @Override
        public boolean isDefault() {
            return false;
        }

        @Override
        public boolean isRequiredOption() {
            return false;
        }

        @Override
        public boolean isRequiredArgument() {
            return false;
        }

        @Override
        public boolean isOptionalArgument() {
            return false;
        }

        @Override
        public boolean isMultiValued() {
            return false;
        }

        @Override
        public boolean isSingleValued() {
            return false;
        }

        @Override
        public boolean isBoolean() {
            return false;
        }

        @Override
        public boolean isSwitchOption() {
            return false;
        }

        @Override
        public boolean isProperty() {
            return false;
        }

        @Override
        public boolean isCommandOption() {
            return false;
        }

        @Override
        public boolean isGroupOption() {
            return false;
        }

        @Override
        public boolean isArgumentOption() {
            return false;
        }

        @Override
        public boolean isHelp() {
            return false;
        }

        @Override
        public boolean isVersion() {
            return false;
        }

        @Override
        public boolean isPropertyOption(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isSwitch(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isGroup(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isCommand(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isArgument(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isRequired(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isOptional(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isHidden(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isAlias(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public Option getPreferredAlias(WriteableCommandLine commandLine) {
            return null;
        }

        @Override
        public List<Option> getAliases(WriteableCommandLine commandLine) {
            return null;
        }

        @Override
        public boolean isDefault(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isRequiredOption(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isRequiredArgument(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isOptionalArgument(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isMultiValued(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isSingleValued(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isBoolean(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isSwitchOption(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isProperty(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isCommandOption(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isGroupOption(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isArgumentOption(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isHelp(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean isVersion(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public String getKey(WriteableCommandLine commandLine) {
            return null;
        }

        @Override
        public String getDescription(WriteableCommandLine commandLine) {
            return null;
        }

        @Override
        public int getMaximum(WriteableCommandLine commandLine) {
            return 0;
        }

        @Override
        public int getMinimum(WriteableCommandLine commandLine) {
            return 0;
        }

        @Override
        public boolean hasArgument(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public boolean hasOptionalArgument(WriteableCommandLine commandLine) {
            return false;
        }

        @Override
        public ResourceBundle getResourceBundle(WriteableCommandLine commandLine) {
            return null;
        }

        @Override
        public String getId(WriteableCommandLine commandLine) {
            return preferredName;
        }

        @Override
        public Set<String> getTriggers(WriteableCommandLine commandLine) {
            return triggers;
        }

        @Override
        public String getPreferredName(WriteableCommandLine commandLine) {
            return preferredName;
        }

        @Override
        public void validate(WriteableCommandLine commandLine, Option option) {
        }

        @Override
        public void defaults(WriteableCommandLine commandLine, Option option) {
        }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, String argument, Option option) {
            return false;
        }

        @Override
        public void process(WriteableCommandLine commandLine, ListIterator<String> arguments, Option option) {
        }

        @Override
        public boolean isRequired(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isOptional(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isHidden(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isAlias(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public Option getPreferredAlias(WriteableCommandLine commandLine, Option option) {
            return null;
        }

        @Override
        public List<Option> getAliases(WriteableCommandLine commandLine, Option option) {
            return null;
        }

        @Override
        public boolean isDefault(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isRequiredOption(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isRequiredArgument(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isOptionalArgument(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isMultiValued(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isSingleValued(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isBoolean(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isSwitchOption(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isProperty(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isCommandOption(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isGroupOption(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isArgumentOption(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isHelp(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean isVersion(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public String getKey(WriteableCommandLine commandLine, Option option) {
            return null;
        }

        @Override
        public String getDescription(WriteableCommandLine commandLine, Option option) {
            return null;
        }

        @Override
        public int getMaximum(WriteableCommandLine commandLine, Option option) {
            return 0;
        }

        @Override
        public int getMinimum(WriteableCommandLine commandLine, Option option) {
            return 0;
        }

        @Override
        public boolean hasArgument(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public boolean hasOptionalArgument(WriteableCommandLine commandLine, Option option) {
            return false;
        }

        @Override
        public ResourceBundle getResourceBundle(WriteableCommandLine commandLine, Option option) {
            return null;
        }

        @Override
        public String getId(WriteableCommandLine commandLine, Option option) {
            return preferredName;
        }

        @Override
        public Set<String> getTriggers(WriteableCommandLine commandLine, Option option) {
            return triggers;
        }

        @Override
        public String getPreferredName(WriteableCommandLine commandLine, Option option) {
            return preferredName;
        }

        @Override
        public void validate(WriteableCommandLine commandLine, Option option, Option parent) {
        }

        @Override
        public void defaults(WriteableCommandLine commandLine, Option option, Option parent) {
        }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, String argument, Option option, Option parent) {
            return false;
        }

        @Override
        public void process(WriteableCommandLine commandLine, ListIterator<String> arguments, Option option, Option parent) {
        }

        @Override
        public boolean isRequired(WriteableCommandLine commandLine, Option option, Option parent, Option grandParent) {
            return false;
        }

        @Override
        public boolean isOptional(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isHidden(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isAlias(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public Option getPreferredAlias(WriteableCommandLine commandLine, Option option, Option parent) {
            return null;
        }

        @Override
        public List<Option> getAliases(WriteableCommandLine commandLine, Option option, Option parent) {
            return null;
        }

        @Override
        public boolean isDefault(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isRequiredOption(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isRequiredArgument(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isOptionalArgument(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isMultiValued(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isSingleValued(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isBoolean(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isSwitchOption(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isProperty(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isCommandOption(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isGroupOption(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isArgumentOption(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isHelp(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean isVersion(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public String getKey(WriteableCommandLine commandLine, Option option, Option parent) {
            return null;
        }

        @Override
        public String getDescription(WriteableCommandLine commandLine, Option option, Option parent) {
            return null;
        }

        @Override
        public int getMaximum(WriteableCommandLine commandLine, Option option, Option parent) {
            return 0;
        }

        @Override
        public int getMinimum(WriteableCommandLine commandLine, Option option, Option parent) {
            return 0;
        }

        @Override
        public boolean hasArgument(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public boolean hasOptionalArgument(WriteableCommandLine commandLine, Option option, Option parent) {
            return false;
        }

        @Override
        public ResourceBundle getResourceBundle(WriteableCommandLine commandLine, Option option, Option parent) {
            return null;
        }

        @Override
        public String getId(WriteableCommandLine commandLine, Option option, Option parent) {
            return preferredName;
        }

        @Override
        public Set<String> getTriggers(WriteableCommandLine commandLine, Option option, Option parent) {
            return triggers;
        }

        @Override
        public String getPreferredName(WriteableCommandLine commandLine, Option option, Option parent) {
            return preferredName;
        }

        @Override
        public void validate(WriteableCommandLine commandLine, Option option, Option parent, Option grandParent) {
        }

        @Override
        public void defaults(WriteableCommandLine commandLine, Option option, Option parent, Option grandParent) {
        }

        @Override
        public boolean canProcess(WriteableCommandLine commandLine, String argument, Option option, Option parent, Option grandParent) {
            return false;
        }

        @Override
        public void process(WriteableCommandLine commandLine, ListIterator<String> arguments, Option option, Option parent, Option grandParent) {
        }
    }

    private static class TestableCommandLine implements WriteableCommandLine {
        private final Map<Option, List<Object>> values = new HashMap<Option, List<Object>>();
        private final Map<Option, List<Object>> defaultValues = new HashMap<Option, List<Object>>();
        private final Map<Option, Boolean> switches = new HashMap<Option, Boolean>();
        private final Map<Option, Boolean> defaultSwitches = new HashMap<Option, Boolean>();
        private final Map<String, String> properties = new HashMap<String, String>();
        private final Map<String, String> defaultProperties = new HashMap<String, String>();
        private final Set<Option> options = new HashSet<Option>();
        private Option currentOption;

        @Override
        public void addOption(Option option) {
            options.add(option);
        }

        @Override
        public void addValue(Option option, Object value) {
            List<Object> vals = values.get(option);
            if (vals == null) {
                vals = new ArrayList<Object>();
                values.put(option, vals);
            }
            vals.add(value);
        }

        @Override
        public List getUndefaultedValues(Option option) {
            List<Object> vals = values.get(option);
            if (vals == null) {
                return Collections.emptyList();
            }
            return new ArrayList<Object>(vals);
        }

        @Override
        public void setDefaultValues(Option option, List defaultValuesList) {
            defaultValues.put(option, new ArrayList<Object>(defaultValuesList));
        }

        @Override
        public void addSwitch(Option option, boolean value) {
            if (switches.containsKey(option)) {
                throw new IllegalStateException("Switch already added for " + option);
            }
            switches.put(option, value);
        }

        @Override
        public void setDefaultSwitch(Option option, Boolean defaultSwitch) {
            defaultSwitches.put(option, defaultSwitch);
        }

        @Override
        public void addProperty(Option option, String property, String value) {
            properties.put(property, value);
        }

        @Override
        public void addProperty(String property, String value) {
            defaultProperties.put(property, value);
        }

        @Override
        public boolean looksLikeOption(String argument) {
            return argument != null && argument.startsWith("-");
        }

        // CommandLine interface methods
        @Override
        public boolean hasOption(Option option) {
            return options.contains(option);
        }

        @Override
        public boolean hasOption(String trigger) {
            for (Option opt : options) {
                if (opt.getTriggers().contains(trigger)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Option getOption(String trigger) {
            for (Option opt : options) {
                if (opt.getTriggers().contains(trigger)) {
                    return opt;
                }
            }
            return null;
        }

        @Override
        public List getValues(Option option) {
            List<Object> all = new ArrayList<Object>();
            List<Object> defs = defaultValues.get(option);
            if (defs != null) {
                all.addAll(defs);
            }
            List<Object> vals = values.get(option);
            if (vals != null) {
                all.addAll(vals);
            }
            return all;
        }

        @Override
        public List getValues(String trigger) {
            Option opt = getOption(trigger);
            if (opt != null) {
                return getValues(opt);
            }
            return Collections.emptyList();
        }

        @Override
        public List getValues(Option option, List defaultValues) {
            return getValues(option);
        }

        @Override
        public Boolean getSwitch(Option option, Boolean defaultSwitch) {
            if (switches.containsKey(option)) {
                return switches.get(option);
            }
            if (defaultSwitches.containsKey(option)) {
                return defaultSwitches.get(option);
            }
            return defaultSwitch;
        }

        @Override
        public Boolean getSwitch(String trigger, Boolean defaultSwitch) {
            Option opt = getOption(trigger);
            if (opt != null) {
                return getSwitch(opt, defaultSwitch);
            }
            return defaultSwitch;
        }

        @Override
        public String getProperty(String property) {
            if (properties.containsKey(property)) {
                return properties.get(property);
            }
            return defaultProperties.get(property);
        }

        @Override
        public Set getOptions() {
            return new HashSet(options);
        }

        @Override
        public Set getOptionTriggers() {
            Set<String> triggers = new HashSet<String>();
            for (Option opt : options) {
                triggers.addAll(opt.getTriggers());
            }
            return triggers;
        }

        @Override
        public boolean hasProperty(String property) {
            return properties.containsKey(property) || defaultProperties.containsKey(property);
        }

        @Override
        public boolean hasSwitch(Option option) {
            return switches.containsKey(option);
        }

        @Override
        public boolean hasSwitch(String trigger) {
            Option opt = getOption(trigger);
            return opt != null && hasSwitch(opt);
        }

        @Override
        public boolean hasValue(Option option) {
            return values.containsKey(option) && !values.get(option).isEmpty();
        }

        @Override
        public boolean hasValue(String trigger) {
            Option opt = getOption(trigger);
            return opt != null && hasValue(opt);
        }

        @Override
        public Option getCurrentOption() {
            return currentOption;
        }

        @Override
        public void setCurrentOption(Option currentOption) {
            this.currentOption = currentOption;
        }

        // Other methods from CommandLine that might exist, we provide minimal implementations
        @Override
        public List getUndefaultedValues(String trigger) {
            Option opt = getOption(trigger);
            if (opt != null) {
                return getUndefaultedValues(opt);
            }
            return Collections.emptyList();
        }

        @Override
        public Boolean getSwitch(Option option) {
            return switches.get(option);
        }

        @Override
        public Boolean getSwitch(String trigger) {
            Option opt = getOption(trigger);
            if (opt != null) {
                return getSwitch(opt);
            }
            return null;
        }

        @Override
        public String getProperty(Option option, String property) {
            return getProperty(property);
        }

        @Override
        public void addSwitch(Option option, Boolean value) {
            addSwitch(option, value.booleanValue());
        }

        @Override
        public void setDefaultSwitch(Option option, boolean defaultSwitch) {
            setDefaultSwitch(option, Boolean.valueOf(defaultSwitch));
        }

        @Override
        public void addProperty(Option option, String property, String value, boolean isDefault) {
            if (isDefault) {
                defaultProperties.put(property, value);
            } else {
                properties.put(property, value);
            }
        }

        @Override
        public void addProperty(String property, String value, boolean isDefault) {
            if (isDefault) {
                defaultProperties.put(property, value);
            } else {
                properties.put(property, value);
            }
        }

        @Override
        public List getValues() {
            List all = new ArrayList();
            for (Option opt : options) {
                all.addAll(getValues(opt));
            }
            return all;
        }

        @Override
        public List getUndefaultedValues() {
            List all = new ArrayList();
            for (Option opt : options) {
                all.addAll(getUndefaultedValues(opt));
            }
            return all;
        }

        @Override
        public Map getSwitches() {
            Map<Option, Boolean> map = new HashMap<Option, Boolean>();
            for (Option opt : options) {
                Boolean val = getSwitch(opt);
                if (val != null) {
                    map.put(opt, val);
                }
            }
            return map;
        }

        @Override
        public Map getProperties() {
            Map<String, String> map = new HashMap<String, String>(defaultProperties);
            map.putAll(properties);
            return map;
        }

        @Override
        public boolean hasOption() {
            return !options.isEmpty();
        }

        @Override
        public boolean hasValue() {
            for (Option opt : options) {
                if (hasValue(opt)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean hasSwitch() {
            for (Option opt : options) {
                if (hasSwitch(opt)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean hasProperty() {
            return !properties.isEmpty() || !defaultProperties.isEmpty();
        }

        @Override
        public String toString() {
            return "TestableCommandLine";
        }
    }

    @Test
    public void testAddOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("test");
        cl.addOption(opt);
        Assert.assertTrue(cl.hasOption(opt));
        Assert.assertTrue(cl.getOptions().contains(opt));
    }

    @Test
    public void testAddOptionNull() {
        TestableCommandLine cl = new TestableCommandLine();
        try {
            cl.addOption(null);
            // If no exception, options should not contain null
            Assert.assertFalse(cl.getOptions().contains(null));
        } catch (NullPointerException e) {
            // Expected if implementation forbids null
        }
    }

    @Test
    public void testAddValue() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        cl.addValue(opt, "value1");
        List undefaulted = cl.getUndefaultedValues(opt);
        Assert.assertEquals(1, undefaulted.size());
        Assert.assertEquals("value1", undefaulted.get(0));
        List allValues = cl.getValues(opt);
        Assert.assertEquals(1, allValues.size());
        Assert.assertEquals("value1", allValues.get(0));
    }

    @Test
    public void testAddMultipleValues() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        cl.addValue(opt, "v1");
        cl.addValue(opt, "v2");
        List undefaulted = cl.getUndefaultedValues(opt);
        Assert.assertEquals(2, undefaulted.size());
        Assert.assertEquals("v1", undefaulted.get(0));
        Assert.assertEquals("v2", undefaulted.get(1));
    }

    @Test
    public void testGetUndefaultedValuesNoValues() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        List undefaulted = cl.getUndefaultedValues(opt);
        Assert.assertTrue(undefaulted.isEmpty());
    }

    @Test
    public void testGetUndefaultedValuesWithDefaults() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        List defaults = Arrays.asList("default1", "default2");
        cl.setDefaultValues(opt, defaults);
        cl.addValue(opt, "user1");
        List undefaulted = cl.getUndefaultedValues(opt);
        Assert.assertEquals(1, undefaulted.size());
        Assert.assertEquals("user1", undefaulted.get(0));
        // getValues should include defaults and user values
        List all = cl.getValues(opt);
        Assert.assertEquals(3, all.size());
        Assert.assertTrue(all.contains("default1"));
        Assert.assertTrue(all.contains("default2"));
        Assert.assertTrue(all.contains("user1"));
    }

    @Test
    public void testSetDefaultValues() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        List defaults = Arrays.asList("a", "b");
        cl.setDefaultValues(opt, defaults);
        List all = cl.getValues(opt);
        Assert.assertEquals(2, all.size());
        Assert.assertEquals("a", all.get(0));
        Assert.assertEquals("b", all.get(1));
    }

    @Test
    public void testSetDefaultValuesNullList() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        cl.setDefaultValues(opt, null);
        // Should handle gracefully, maybe empty list
        List all = cl.getValues(opt);
        Assert.assertNotNull(all);
        Assert.assertTrue(all.isEmpty());
    }

    @Test
    public void testAddSwitch() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.addSwitch(opt, true);
        Assert.assertTrue(cl.getSwitch(opt, false));
        Assert.assertTrue(cl.hasSwitch(opt));
    }

    @Test
    public void testAddSwitchFalse() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.addSwitch(opt, false);
        Assert.assertFalse(cl.getSwitch(opt, true));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitchTwiceThrowsException() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.addSwitch(opt, true);
        cl.addSwitch(opt, false);
    }

    @Test
    public void testSetDefaultSwitch() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.setDefaultSwitch(opt, Boolean.TRUE);
        // No explicit addSwitch, so getSwitch should return default
        Assert.assertTrue(cl.getSwitch(opt, false));
        // After adding a switch, it should override default
        cl.addSwitch(opt, false);
        Assert.assertFalse(cl.getSwitch(opt, true));
    }

    @Test
    public void testSetDefaultSwitchNull() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.setDefaultSwitch(opt, null);
        // getSwitch with provided default should return that default
        Assert.assertFalse(cl.getSwitch(opt, false));
        Assert.assertTrue(cl.getSwitch(opt, true));
    }

    @Test
    public void testAddPropertyWithOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("prop");
        cl.addOption(opt);
        cl.addProperty(opt, "key", "value");
        Assert.assertEquals("value", cl.getProperty("key"));
    }

    @Test
    public void testAddPropertyDefault() {
        TestableCommandLine cl = new TestableCommandLine();
        cl.addProperty("defKey", "defValue");
        Assert.assertEquals("defValue", cl.getProperty("defKey"));
    }

    @Test
    public void testAddPropertyOverwrite() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("prop");
        cl.addOption(opt);
        cl.addProperty(opt, "key", "first");
        cl.addProperty(opt, "key", "second");
        Assert.assertEquals("second", cl.getProperty("key"));
    }

    @Test
    public void testAddPropertyDefaultOverwrite() {
        TestableCommandLine cl = new TestableCommandLine();
        cl.addProperty("key", "first");
        cl.addProperty("key", "second");
        Assert.assertEquals("second", cl.getProperty("key"));
    }

    @Test
    public void testLooksLikeOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Assert.assertTrue(cl.looksLikeOption("-f"));
        Assert.assertTrue(cl.looksLikeOption("--file"));
        Assert.assertFalse(cl.looksLikeOption("file"));
        Assert.assertFalse(cl.looksLikeOption(""));
        Assert.assertFalse(cl.looksLikeOption(null));
    }

    @Test
    public void testLooksLikeOptionEdgeCases() {
        TestableCommandLine cl = new TestableCommandLine();
        Assert.assertTrue(cl.looksLikeOption("-"));
        Assert.assertFalse(cl.looksLikeOption(" -f")); // leading space
        Assert.assertFalse(cl.looksLikeOption("f-"));
    }

    @Test
    public void testGetUndefaultedValuesForUnknownOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("unknown");
        List vals = cl.getUndefaultedValues(opt);
        Assert.assertTrue(vals.isEmpty());
    }

    @Test
    public void testAddValueToUnknownOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        // Adding value without adding option should still work (implementation dependent)
        cl.addValue(opt, "val");
        List undefaulted = cl.getUndefaultedValues(opt);
        Assert.assertEquals(1, undefaulted.size());
        Assert.assertEquals("val", undefaulted.get(0));
    }

    @Test
    public void testAddSwitchToUnknownOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addSwitch(opt, true);
        Assert.assertTrue(cl.getSwitch(opt, false));
    }

    @Test
    public void testSetDefaultValuesForUnknownOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        List defaults = Arrays.asList("d");
        cl.setDefaultValues(opt, defaults);
        List all = cl.getValues(opt);
        Assert.assertEquals(1, all.size());
        Assert.assertEquals("d", all.get(0));
    }

    @Test
    public void testSetDefaultSwitchForUnknownOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.setDefaultSwitch(opt, true);
        Assert.assertTrue(cl.getSwitch(opt, false));
    }

    @Test
    public void testAddPropertyWithOptionNullProperty() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("prop");
        cl.addOption(opt);
        cl.addProperty(opt, null, "value");
        Assert.assertEquals("value", cl.getProperty(null));
    }

    @Test
    public void testAddPropertyWithOptionNullValue() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("prop");
        cl.addOption(opt);
        cl.addProperty(opt, "key", null);
        Assert.assertNull(cl.getProperty("key"));
    }

    @Test
    public void testAddPropertyDefaultNullProperty() {
        TestableCommandLine cl = new TestableCommandLine();
        cl.addProperty(null, "value");
        Assert.assertEquals("value", cl.getProperty(null));
    }

    @Test
    public void testAddPropertyDefaultNullValue() {
        TestableCommandLine cl = new TestableCommandLine();
        cl.addProperty("key", null);
        Assert.assertNull(cl.getProperty("key"));
    }

    @Test
    public void testGetSwitchWithNoDefaultAndNoAdd() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        // No addSwitch, no defaultSwitch, so getSwitch with provided default should return that default
        Assert.assertNull(cl.getSwitch(opt, null));
        Assert.assertTrue(cl.getSwitch(opt, true));
        Assert.assertFalse(cl.getSwitch(opt, false));
    }

    @Test
    public void testGetPropertyNotSet() {
        TestableCommandLine cl = new TestableCommandLine();
        Assert.assertNull(cl.getProperty("nonexistent"));
    }

    @Test
    public void testHasOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("test");
        Assert.assertFalse(cl.hasOption(opt));
        cl.addOption(opt);
        Assert.assertTrue(cl.hasOption(opt));
    }

    @Test
    public void testHasOptionByTrigger() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("test");
        cl.addOption(opt);
        Assert.assertTrue(cl.hasOption("test"));
        Assert.assertFalse(cl.hasOption("other"));
    }

    @Test
    public void testGetOptionByTrigger() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("test");
        cl.addOption(opt);
        Assert.assertEquals(opt, cl.getOption("test"));
        Assert.assertNull(cl.getOption("other"));
    }

    @Test
    public void testGetValuesByTrigger() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        cl.addValue(opt, "val");
        List vals = cl.getValues("opt");
        Assert.assertEquals(1, vals.size());
        Assert.assertEquals("val", vals.get(0));
        List empty = cl.getValues("unknown");
        Assert.assertTrue(empty.isEmpty());
    }

    @Test
    public void testGetSwitchByTrigger() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.addSwitch(opt, true);
        Assert.assertTrue(cl.getSwitch("switch", false));
        Assert.assertFalse(cl.getSwitch("unknown", false));
    }

    @Test
    public void testHasSwitch() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        Assert.assertFalse(cl.hasSwitch(opt));
        cl.addSwitch(opt, true);
        Assert.assertTrue(cl.hasSwitch(opt));
    }

    @Test
    public void testHasSwitchByTrigger() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.addSwitch(opt, true);
        Assert.assertTrue(cl.hasSwitch("switch"));
        Assert.assertFalse(cl.hasSwitch("unknown"));
    }

    @Test
    public void testHasValue() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        Assert.assertFalse(cl.hasValue(opt));
        cl.addValue(opt, "val");
        Assert.assertTrue(cl.hasValue(opt));
    }

    @Test
    public void testHasValueByTrigger() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        cl.addValue(opt, "val");
        Assert.assertTrue(cl.hasValue("opt"));
        Assert.assertFalse(cl.hasValue("unknown"));
    }

    @Test
    public void testGetCurrentOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Assert.assertNull(cl.getCurrentOption());
        Option opt = new TestOption("current");
        cl.setCurrentOption(opt);
        Assert.assertEquals(opt, cl.getCurrentOption());
    }

    @Test
    public void testSetCurrentOption() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("current");
        cl.setCurrentOption(opt);
        Assert.assertEquals(opt, cl.getCurrentOption());
        cl.setCurrentOption(null);
        Assert.assertNull(cl.getCurrentOption());
    }

    @Test
    public void testGetOptionTriggers() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt1 = new TestOption("opt1");
        Option opt2 = new TestOption("opt2");
        cl.addOption(opt1);
        cl.addOption(opt2);
        Set triggers = cl.getOptionTriggers();
        Assert.assertEquals(2, triggers.size());
        Assert.assertTrue(triggers.contains("opt1"));
        Assert.assertTrue(triggers.contains("opt2"));
    }

    @Test
    public void testHasProperty() {
        TestableCommandLine cl = new TestableCommandLine();
        Assert.assertFalse(cl.hasProperty("key"));
        cl.addProperty("key", "val");
        Assert.assertTrue(cl.hasProperty("key"));
    }

    @Test
    public void testGetProperties() {
        TestableCommandLine cl = new TestableCommandLine();
        cl.addProperty("def", "defVal");
        Option opt = new TestOption("prop");
        cl.addOption(opt);
        cl.addProperty(opt, "key", "val");
        Map props = cl.getProperties();
        Assert.assertEquals(2, props.size());
        Assert.assertEquals("defVal", props.get("def"));
        Assert.assertEquals("val", props.get("key"));
    }

    @Test
    public void testGetSwitches() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt1 = new TestOption("s1");
        Option opt2 = new TestOption("s2");
        cl.addOption(opt1);
        cl.addOption(opt2);
        cl.addSwitch(opt1, true);
        cl.setDefaultSwitch(opt2, false);
        Map switches = cl.getSwitches();
        Assert.assertEquals(1, switches.size()); // only explicit switches
        Assert.assertTrue(switches.containsKey(opt1));
        Assert.assertTrue((Boolean) switches.get(opt1));
    }

    @Test
    public void testGetValuesAll() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt1 = new TestOption("o1");
        Option opt2 = new TestOption("o2");
        cl.addOption(opt1);
        cl.addOption(opt2);
        cl.addValue(opt1, "a");
        cl.addValue(opt2, "b");
        List all = cl.getValues();
        Assert.assertEquals(2, all.size());
        Assert.assertTrue(all.contains("a"));
        Assert.assertTrue(all.contains("b"));
    }

    @Test
    public void testGetUndefaultedValuesAll() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt1 = new TestOption("o1");
        Option opt2 = new TestOption("o2");
        cl.addOption(opt1);
        cl.addOption(opt2);
        cl.setDefaultValues(opt1, Arrays.asList("def"));
        cl.addValue(opt1, "user");
        cl.addValue(opt2, "val");
        List undefaulted = cl.getUndefaultedValues();
        Assert.assertEquals(2, undefaulted.size());
        Assert.assertTrue(undefaulted.contains("user"));
        Assert.assertTrue(undefaulted.contains("val"));
        Assert.assertFalse(undefaulted.contains("def"));
    }

    @Test
    public void testHasOptionEmpty() {
        TestableCommandLine cl = new TestableCommandLine();
        Assert.assertFalse(cl.hasOption());
        cl.addOption(new TestOption("opt"));
        Assert.assertTrue(cl.hasOption());
    }

    @Test
    public void testHasValueEmpty() {
        TestableCommandLine cl = new TestableCommandLine();
        Assert.assertFalse(cl.hasValue());
        Option opt = new TestOption("opt");
        cl.addOption(opt);
        cl.addValue(opt, "val");
        Assert.assertTrue(cl.hasValue());
    }

    @Test
    public void testHasSwitchEmpty() {
        TestableCommandLine cl = new TestableCommandLine();
        Assert.assertFalse(cl.hasSwitch());
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.addSwitch(opt, true);
        Assert.assertTrue(cl.hasSwitch());
    }

    @Test
    public void testHasPropertyEmpty() {
        TestableCommandLine cl = new TestableCommandLine();
        Assert.assertFalse(cl.hasProperty());
        cl.addProperty("key", "val");
        Assert.assertTrue(cl.hasProperty());
    }

    @Test
    public void testAddSwitchBooleanObject() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.addSwitch(opt, Boolean.TRUE);
        Assert.assertTrue(cl.getSwitch(opt, false));
    }

    @Test
    public void testSetDefaultSwitchPrimitive() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("switch");
        cl.addOption(opt);
        cl.setDefaultSwitch(opt, true);
        Assert.assertTrue(cl.getSwitch(opt, false));
    }

    @Test
    public void testAddPropertyWithOptionAndDefaultFlag() {
        TestableCommandLine cl = new TestableCommandLine();
        Option opt = new TestOption("prop");
        cl.addOption(opt);
        cl.addProperty(opt, "key", "val", false);
        Assert.assertEquals("val", cl.getProperty("key"));
        cl.addProperty(opt, "key2", "val2", true);
        Assert.assertEquals("val2", cl.getProperty("key2"));
        // default property should be overridable by non-default later? Implementation may vary.
    }

    @Test
    public void testAddPropertyDefaultWithFlag() {
        TestableCommandLine cl = new TestableCommandLine();
        cl.addProperty("key", "val", false);
        Assert.assertEquals("val", cl.getProperty("key"));
        cl.addProperty("key2", "val2", true);
        Assert.assertEquals("val2", cl.getProperty("key2"));
    }
}
