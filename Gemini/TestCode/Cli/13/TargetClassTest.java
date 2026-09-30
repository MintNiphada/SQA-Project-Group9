package org.apache.commons.cli2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli2.commandline.WriteableCommandLineImpl;
import org.apache.commons.cli2.option.DefaultOption;
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

class WriteableCommandLineTest {

    private WriteableCommandLine commandLine;
    private Option rootOption;
    private Option helpOption;
    private Option fileOption;

    @Before
    public void setUp() {
        helpOption = new DefaultOption("-h", "--help", false, "Display help", null, null, true, 0, null, null);
        fileOption = new DefaultOption("-f", "--file", true, "Specify file", null, null, false, 0, null, null);
        List options = new ArrayList();
        options.add(helpOption);
        options.add(fileOption);
        rootOption = new DefaultOption("-r", "--root", false, "Root option", null, null, false, 0, null, null);
        commandLine = new WriteableCommandLineImpl(rootOption, options);
    }

    @Test
    public void testAddOptionAndHasOption() {
        Assert.assertFalse(commandLine.hasOption(helpOption));
        Assert.assertFalse(commandLine.hasOption("-h"));
        Assert.assertFalse(commandLine.hasOption("--help"));

        commandLine.addOption(helpOption);

        Assert.assertTrue(commandLine.hasOption(helpOption));
        Assert.assertTrue(commandLine.hasOption("-h"));
        Assert.assertTrue(commandLine.hasOption("--help"));
        Assert.assertEquals(helpOption, commandLine.getOption("-h"));
        Assert.assertEquals(helpOption, commandLine.getOption("--help"));

        Set options = commandLine.getOptions();
        Assert.assertTrue(options.contains(helpOption));
    }

    @Test
    public void testAddValueAndGetValues() {
        Assert.assertNull(commandLine.getValue(fileOption));
        Assert.assertTrue(commandLine.getValues(fileOption).isEmpty());

        commandLine.addValue(fileOption, "file1.txt");
        Assert.assertEquals("file1.txt", commandLine.getValue(fileOption));
        Assert.assertEquals("file1.txt", commandLine.getValue("-f"));
        Assert.assertEquals("file1.txt", commandLine.getValue("--file"));

        List values = commandLine.getValues(fileOption);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("file1.txt", values.get(0));

        commandLine.addValue(fileOption, "file2.txt");
        values = commandLine.getValues(fileOption);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("file1.txt", values.get(0));
        Assert.assertEquals("file2.txt", values.get(1));

        List undefaulted = commandLine.getUndefaultedValues(fileOption);
        Assert.assertEquals(2, undefaulted.size());
        Assert.assertEquals("file1.txt", undefaulted.get(0));
        Assert.assertEquals("file2.txt", undefaulted.get(1));
    }

    @Test
    public void testDefaultValues() {
        List defaults = Arrays.asList(new Object[]{"default.txt", "fallback.txt"});
        commandLine.setDefaultValues(fileOption, defaults);

        Assert.assertEquals("default.txt", commandLine.getValue(fileOption));
        Assert.assertEquals("default.txt", commandLine.getValue("-f"));
        Assert.assertEquals(defaults, commandLine.getValues(fileOption));

        List undefaulted = commandLine.getUndefaultedValues(fileOption);
        Assert.assertTrue(undefaulted.isEmpty());

        commandLine.addValue(fileOption, "actual.txt");
        Assert.assertEquals("actual.txt", commandLine.getValue(fileOption));
        Assert.assertEquals(Collections.singletonList("actual.txt"), commandLine.getValues(fileOption));
        Assert.assertEquals(Collections.singletonList("actual.txt"), commandLine.getUndefaultedValues(fileOption));
    }

    @Test
    public void testAddSwitchAndGetSwitch() {
        Assert.assertNull(commandLine.getSwitch(helpOption));
        Assert.assertNull(commandLine.getSwitch("-h"));

        commandLine.addSwitch(helpOption, true);
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(helpOption));
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch("-h"));
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch("--help"));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitchTwiceThrowsException() {
        commandLine.addSwitch(helpOption, true);
        commandLine.addSwitch(helpOption, false);
    }

    @Test
    public void testDefaultSwitch() {
        commandLine.setDefaultSwitch(helpOption, Boolean.FALSE);
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch(helpOption));
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch("-h"));

        commandLine.addSwitch(helpOption, true);
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(helpOption));
    }

    @Test
    public void testAddPropertyAndGetProperty() {
        PropertyOption propOption = new PropertyOption();
        commandLine.addOption(propOption);

        Assert.assertNull(commandLine.getProperty("key1"));
        Assert.assertEquals("defaultVal", commandLine.getProperty("key1", "defaultVal"));

        commandLine.addProperty("key1", "value1");
        Assert.assertEquals("value1", commandLine.getProperty("key1"));
        Assert.assertEquals("value1", commandLine.getProperty("key1", "defaultVal"));

        commandLine.addProperty("key1", "value2");
        Assert.assertEquals("value2", commandLine.getProperty("key1"));

        commandLine.addProperty("key2", "value3");
        Set properties = commandLine.getProperties();
        Assert.assertTrue(properties.contains("key1"));
        Assert.assertTrue(properties.contains("key2"));
    }

    @Test
    public void testLooksLikeOption() {
        Assert.assertTrue(commandLine.looksLikeOption("-h"));
        Assert.assertTrue(commandLine.looksLikeOption("--help"));
        Assert.assertTrue(commandLine.looksLikeOption("-f"));
        Assert.assertFalse(commandLine.looksLikeOption("randomArgument"));
        Assert.assertFalse(commandLine.looksLikeOption(""));
    }

    @Test
    public void testCustomImplementationContracts() {
        final Map options = new HashMap();
        final Map values = new HashMap();
        final Map defaultValuesMap = new HashMap();
        final Map switches = new HashMap();
        final Map defaultSwitches = new HashMap();
        final Map properties = new HashMap();

        WriteableCommandLine customImpl = new WriteableCommandLine() {
            public void addOption(Option option) {
                options.put(option.getPreferredName(), option);
            }

            public void addValue(Option option, Object value) {
                List list = (List) values.get(option);
                if (list == null) {
                    list = new ArrayList();
                    values.put(option, list);
                }
                list.add(value);
            }

            public void setDefaultValues(Option option, List defaultValues) {
                defaultValuesMap.put(option, defaultValues);
            }

            public void addSwitch(Option option, boolean value) throws IllegalStateException {
                if (switches.containsKey(option)) {
                    throw new IllegalStateException("Switch already set");
                }
                switches.put(option, Boolean.valueOf(value));
            }

            public void setDefaultSwitch(Option option, Boolean defaultSwitch) {
                defaultSwitches.put(option, defaultSwitch);
            }

            public void addProperty(String property, String value) {
                properties.put(property, value);
            }

            public boolean looksLikeOption(String argument) {
                return argument != null && argument.startsWith("-");
            }

            public boolean hasOption(Option option) {
                return options.containsValue(option);
            }

            public boolean hasOption(String trigger) {
                return options.containsKey(trigger);
            }

            public Option getOption(String trigger) {
                return (Option) options.get(trigger);
            }

            public List getValues(Option option) {
                List list = (List) values.get(option);
                return list != null ? list : (List) defaultValuesMap.get(option);
            }

            public List getValues(Option option, List defaultValuesList) {
                List list = getValues(option);
                return (list != null && !list.isEmpty()) ? list : defaultValuesList;
            }

            public List getValues(String trigger) {
                return getValues(getOption(trigger));
            }

            public List getValues(String trigger, List defaultValuesList) {
                return getValues(getOption(trigger), defaultValuesList);
            }

            public Object getValue(Option option) {
                List list = getValues(option);
                return (list != null && !list.isEmpty()) ? list.get(0) : null;
            }

            public Object getValue(Option option, Object defaultValue) {
                Object val = getValue(option);
                return val != null ? val : defaultValue;
            }

            public Object getValue(String trigger) {
                return getValue(getOption(trigger));
            }

            public Object getValue(String trigger, Object defaultValue) {
                return getValue(getOption(trigger), defaultValue);
            }

            public Boolean getSwitch(Option option) {
                Boolean s = (Boolean) switches.get(option);
                return s != null ? s : (Boolean) defaultSwitches.get(option);
            }

            public Boolean getSwitch(Option option, Boolean defaultValue) {
                Boolean s = getSwitch(option);
                return s != null ? s : defaultValue;
            }

            public Boolean getSwitch(String trigger) {
                return getSwitch(getOption(trigger));
            }

            public Boolean getSwitch(String trigger, Boolean defaultValue) {
                return getSwitch(getOption(trigger), defaultValue);
            }

            public String getProperty(String property) {
                return (String) properties.get(property);
            }

            public String getProperty(String property, String defaultValue) {
                String val = getProperty(property);
                return val != null ? val : defaultValue;
            }

            public Set getProperties() {
                return properties.keySet();
            }

            public Set getOptionTriggers() {
                return options.keySet();
            }

            public Set getOptions() {
                return new HashSet(options.values());
            }

            public List getUndefaultedValues(Option option) {
                List list = (List) values.get(option);
                return list != null ? list : Collections.EMPTY_LIST;
            }
        };

        customImpl.addOption(helpOption);
        Assert.assertTrue(customImpl.hasOption(helpOption));
        Assert.assertTrue(customImpl.hasOption(helpOption.getPreferredName()));
        Assert.assertEquals(helpOption, customImpl.getOption(helpOption.getPreferredName()));

        customImpl.addValue(helpOption, "v1");
        Assert.assertEquals("v1", customImpl.getValue(helpOption));
        Assert.assertEquals("v1", customImpl.getValue(helpOption.getPreferredName()));
        Assert.assertEquals(Collections.singletonList("v1"), customImpl.getValues(helpOption));
        Assert.assertEquals(Collections.singletonList("v1"), customImpl.getUndefaultedValues(helpOption));

        customImpl.addSwitch(helpOption, true);
        Assert.assertEquals(Boolean.TRUE, customImpl.getSwitch(helpOption));
        Assert.assertEquals(Boolean.TRUE, customImpl.getSwitch(helpOption.getPreferredName()));

        try {
            customImpl.addSwitch(helpOption, false);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            Assert.assertNotNull(e.getMessage());
        }

        customImpl.setDefaultSwitch(fileOption, Boolean.FALSE);
        Assert.assertEquals(Boolean.FALSE, customImpl.getSwitch(fileOption));

        customImpl.setDefaultValues(fileOption, Collections.singletonList("defFile"));
        Assert.assertEquals("defFile", customImpl.getValue(fileOption));

        customImpl.addProperty("foo", "bar");
        Assert.assertEquals("bar", customImpl.getProperty("foo"));
        Assert.assertEquals("default", customImpl.getProperty("nonexistent", "default"));

        Assert.assertTrue(customImpl.looksLikeOption("-test"));
        Assert.assertFalse(customImpl.looksLikeOption("test"));
    }
}