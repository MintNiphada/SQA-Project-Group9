package org.apache.commons.cli2.commandline;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.Option;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WriteableCommandLineImplTest {

    private Option rootOption;
    private List arguments;
    private WriteableCommandLineImpl commandLine;

    private Option createMockOption(final String name, final Set triggers, final Set prefixes, final Option parent) {
        return (Option) Proxy.newProxyInstance(
            Option.class.getClassLoader(),
            new Class<?>[] { Option.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) {
                    String mName = method.getName();
                    if ("getPreferredName".equals(mName)) {
                        return name;
                    }
                    if ("getTriggers".equals(mName)) {
                        return triggers != null ? triggers : Collections.emptySet();
                    }
                    if ("getPrefixes".equals(mName)) {
                        return prefixes != null ? prefixes : Collections.emptySet();
                    }
                    if ("getParent".equals(mName)) {
                        return parent;
                    }
                    if ("toString".equals(mName)) {
                        return name != null ? name : "MockOption";
                    }
                    if ("hashCode".equals(mName)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(mName)) {
                        return Boolean.valueOf(proxy == (args != null && args.length > 0 ? args[0] : null));
                    }
                    return null;
                }
            }
        );
    }

    private Argument createMockArgument(final String name, final Set triggers, final Set prefixes, final Option parent) {
        return (Argument) Proxy.newProxyInstance(
            Argument.class.getClassLoader(),
            new Class<?>[] { Argument.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) {
                    String mName = method.getName();
                    if ("getPreferredName".equals(mName)) {
                        return name;
                    }
                    if ("getTriggers".equals(mName)) {
                        return triggers != null ? triggers : Collections.emptySet();
                    }
                    if ("getPrefixes".equals(mName)) {
                        return prefixes != null ? prefixes : Collections.emptySet();
                    }
                    if ("getParent".equals(mName)) {
                        return parent;
                    }
                    if ("toString".equals(mName)) {
                        return name != null ? name : "MockArgument";
                    }
                    if ("hashCode".equals(mName)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(mName)) {
                        return Boolean.valueOf(proxy == (args != null && args.length > 0 ? args[0] : null));
                    }
                    return null;
                }
            }
        );
    }

    @Before
    public void setUp() {
        Set prefixes = new HashSet(Arrays.asList("--", "-", "+"));
        rootOption = createMockOption("root", Collections.singleton("root"), prefixes, null);
        arguments = new ArrayList();
        arguments.add("--opt");
        arguments.add("val with spaces");
        arguments.add("-s");
        commandLine = new WriteableCommandLineImpl(rootOption, arguments);
    }

    @Test
    public void testGetNormalised() {
        List normalised = commandLine.getNormalised();
        Assert.assertEquals(3, normalised.size());
        Assert.assertEquals("--opt", normalised.get(0));
        Assert.assertEquals("val with spaces", normalised.get(1));
        Assert.assertEquals("-s", normalised.get(2));

        try {
            normalised.add("new-arg");
            Assert.fail("Expected UnsupportedOperationException on unmodifiable list");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testToString() {
        String rendered = commandLine.toString();
        Assert.assertEquals("--opt \"val with spaces\" -s", rendered);

        WriteableCommandLineImpl emptyCmd = new WriteableCommandLineImpl(rootOption, Collections.emptyList());
        Assert.assertEquals("", emptyCmd.toString());

        List singleArg = Collections.singletonList("single");
        WriteableCommandLineImpl singleCmd = new WriteableCommandLineImpl(rootOption, singleArg);
        Assert.assertEquals("single", singleCmd.toString());
    }

    @Test
    public void testLooksLikeOption() {
        Assert.assertTrue(commandLine.looksLikeOption("--test"));
        Assert.assertTrue(commandLine.looksLikeOption("-t"));
        Assert.assertTrue(commandLine.looksLikeOption("+opt"));
        Assert.assertFalse(commandLine.looksLikeOption("nonOption"));
        Assert.assertFalse(commandLine.looksLikeOption(""));
    }

    @Test
    public void testAddOptionAndGetOption() {
        Set triggers = new HashSet(Arrays.asList("--verbose", "-v"));
        Option verboseOpt = createMockOption("--verbose", triggers, Collections.singleton("-"), null);

        Assert.assertFalse(commandLine.hasOption(verboseOpt));
        commandLine.addOption(verboseOpt);
        Assert.assertTrue(commandLine.hasOption(verboseOpt));

        Assert.assertSame(verboseOpt, commandLine.getOption("--verbose"));
        Assert.assertSame(verboseOpt, commandLine.getOption("-v"));
        Assert.assertNull(commandLine.getOption("--unknown"));

        List options = commandLine.getOptions();
        Assert.assertEquals(1, options.size());
        Assert.assertTrue(options.contains(verboseOpt));

        try {
            options.add(createMockOption("fail", null, null, null));
            Assert.fail("Expected UnsupportedOperationException on unmodifiable list");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        Set optionTriggers = commandLine.getOptionTriggers();
        Assert.assertTrue(optionTriggers.contains("--verbose"));
        Assert.assertTrue(optionTriggers.contains("-v"));

        try {
            optionTriggers.add("trigger");
            Assert.fail("Expected UnsupportedOperationException on unmodifiable set");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testAddOptionWithParentHierarchy() {
        Option grandParent = createMockOption("grandParent", Collections.singleton("gp"), null, null);
        Option parent = createMockOption("parent", Collections.singleton("p"), null, grandParent);
        Option child = createMockOption("child", Collections.singleton("c"), null, parent);

        commandLine.addOption(child);

        Assert.assertTrue(commandLine.hasOption(child));
        Assert.assertTrue(commandLine.hasOption(parent));
        Assert.assertTrue(commandLine.hasOption(grandParent));

        List options = commandLine.getOptions();
        Assert.assertEquals(3, options.size());
        Assert.assertSame(child, options.get(0));
        Assert.assertSame(parent, options.get(1));
        Assert.assertSame(grandParent, options.get(2));

        // Adding child again should not re-add parents if already present
        commandLine.addOption(child);
        Assert.assertEquals(4, commandLine.getOptions().size());
    }

    @Test
    public void testAddValueForNonArgument() {
        Option opt = createMockOption("--file", Collections.singleton("--file"), null, null);

        commandLine.addValue(opt, "file1.txt");
        commandLine.addValue(opt, "file2.txt");

        Assert.assertFalse(commandLine.hasOption(opt));

        List undefaulted = commandLine.getUndefaultedValues(opt);
        Assert.assertEquals(2, undefaulted.size());
        Assert.assertEquals("file1.txt", undefaulted.get(0));
        Assert.assertEquals("file2.txt", undefaulted.get(1));

        List values = commandLine.getValues(opt, null);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("file1.txt", values.get(0));
        Assert.assertEquals("file2.txt", values.get(1));
    }

    @Test
    public void testAddValueForArgument() {
        Argument arg = createMockArgument("arg", Collections.singleton("arg"), null, null);

        commandLine.addValue(arg, "argValue");

        Assert.assertTrue(commandLine.hasOption(arg));
        List values = commandLine.getValues(arg, null);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("argValue", values.get(0));
    }

    @Test
    public void testGetUndefaultedValuesWhenEmpty() {
        Option opt = createMockOption("--empty", Collections.singleton("--empty"), null, null);
        List undefaulted = commandLine.getUndefaultedValues(opt);
        Assert.assertNotNull(undefaulted);
        Assert.assertTrue(undefaulted.isEmpty());
    }

    @Test
    public void testGetValuesWithDefaults() {
        Option opt = createMockOption("--opt", Collections.singleton("--opt"), null, null);

        // Case 1: no values and no defaults
        List values = commandLine.getValues(opt, null);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());

        // Case 2: no values, method defaults provided
        List methodDefaults = Arrays.asList("d1", "d2");
        values = commandLine.getValues(opt, methodDefaults);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("d1", values.get(0));
        Assert.assertEquals("d2", values.get(1));

        // Case 3: no values, instance defaults configured
        List instanceDefaults = Arrays.asList("id1", "id2", "id3");
        commandLine.setDefaultValues(opt, instanceDefaults);
        values = commandLine.getValues(opt, null);
        Assert.assertEquals(3, values.size());
        Assert.assertEquals("id1", values.get(0));

        // Case 4: empty method defaults should fallback to instance defaults
        values = commandLine.getValues(opt, Collections.emptyList());
        Assert.assertEquals(3, values.size());
        Assert.assertEquals("id1", values.get(0));

        // Case 5: values present, default values size > values size (augmentation path)
        commandLine.addValue(opt, "val1");
        List augmentDefaults = Arrays.asList("def1", "def2", "def3");
        values = commandLine.getValues(opt, augmentDefaults);
        Assert.assertEquals(3, values.size());
        Assert.assertEquals("val1", values.get(0));
        Assert.assertEquals("def2", values.get(1));
        Assert.assertEquals("def3", values.get(2));

        // Case 6: values present, default values size <= values size
        List smallerDefaults = Collections.singletonList("def1");
        values = commandLine.getValues(opt, smallerDefaults);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("val1", values.get(0));

        // Case 7: remove default values
        commandLine.setDefaultValues(opt, null);
        values = commandLine.getValues(opt, null);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("val1", values.get(0));
    }

    @Test
    public void testAddSwitchSuccess() {
        Option optTrue = createMockOption("-t", Collections.singleton("-t"), null, null);
        Option optFalse = createMockOption("-f", Collections.singleton("-f"), null, null);

        commandLine.addSwitch(optTrue, true);
        commandLine.addSwitch(optFalse, false);

        Assert.assertTrue(commandLine.hasOption(optTrue));
        Assert.assertTrue(commandLine.hasOption(optFalse));

        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(optTrue, null));
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch(optFalse, null));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitchAlreadySetThrowsException() {
        Option opt = createMockOption("-s", Collections.singleton("-s"), null, null);
        commandLine.addSwitch(opt, true);
        commandLine.addSwitch(opt, false);
    }

    @Test
    public void testGetSwitchWithDefaults() {
        Option opt = createMockOption("-d", Collections.singleton("-d"), null, null);

        // Neither switch nor defaults set
        Assert.assertNull(commandLine.getSwitch(opt, null));

        // Method default used when switch not set
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, Boolean.TRUE));
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch(opt, Boolean.FALSE));

        // Instance default used when method default is null
        commandLine.setDefaultSwitch(opt, Boolean.TRUE);
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, null));

        // Reset instance default
        commandLine.setDefaultSwitch(opt, null);
        Assert.assertNull(commandLine.getSwitch(opt, null));

        // When switch is added, it overrides both
        commandLine.setDefaultSwitch(opt, Boolean.FALSE);
        commandLine.addSwitch(opt, true);
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(opt, Boolean.FALSE));
    }

    @Test
    public void testPropertiesWithDefaultPropertyOption() {
        Assert.assertNull(commandLine.getProperty("property.key"));
        Assert.assertTrue(commandLine.getProperties().isEmpty());

        commandLine.addProperty("prop1", "value1");
        commandLine.addProperty("prop2", "value2");

        Assert.assertEquals("value1", commandLine.getProperty("prop1"));
        Assert.assertEquals("value2", commandLine.getProperty("prop2"));
        Assert.assertNull(commandLine.getProperty("prop3"));

        Set properties = commandLine.getProperties();
        Assert.assertEquals(2, properties.size());
        Assert.assertTrue(properties.contains("prop1"));
        Assert.assertTrue(properties.contains("prop2"));

        try {
            properties.add("fail");
            Assert.fail("Expected UnsupportedOperationException on unmodifiable set");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testPropertiesWithCustomOption() {
        Option opt = createMockOption("-D", Collections.singleton("-D"), null, null);

        Assert.assertEquals("defaultVal", commandLine.getProperty(opt, "k1", "defaultVal"));
        Assert.assertTrue(commandLine.getProperties(opt).isEmpty());

        commandLine.addProperty(opt, "k1", "v1");
        commandLine.addProperty(opt, "k2", "v2");

        Assert.assertEquals("v1", commandLine.getProperty(opt, "k1", "defaultVal"));
        Assert.assertEquals("defaultVal", commandLine.getProperty(opt, "unknownKey", "defaultVal"));

        Set keys = commandLine.getProperties(opt);
        Assert.assertEquals(2, keys.size());
        Assert.assertTrue(keys.contains("k1"));
        Assert.assertTrue(keys.contains("k2"));

        try {
            keys.remove("k1");
            Assert.fail("Expected UnsupportedOperationException on unmodifiable set");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }
}