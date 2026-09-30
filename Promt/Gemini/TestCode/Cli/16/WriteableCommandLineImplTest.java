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
import org.apache.commons.cli2.option.PropertyOption;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WriteableCommandLineImplTest {

    private Option rootOption;
    private List arguments;
    private WriteableCommandLineImpl commandLine;

    private Option createMockOption(final String preferredName, final Set triggers, final Set prefixes) {
        return (Option) Proxy.newProxyInstance(
            Option.class.getClassLoader(),
            new Class[] { Option.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String name = method.getName();
                    if ("getPreferredName".equals(name)) {
                        return preferredName;
                    } else if ("getTriggers".equals(name)) {
                        return triggers == null ? Collections.EMPTY_SET : triggers;
                    } else if ("getPrefixes".equals(name)) {
                        return prefixes == null ? Collections.EMPTY_SET : prefixes;
                    } else if ("toString".equals(name)) {
                        return "MockOption[" + preferredName + "]";
                    } else if ("hashCode".equals(name)) {
                        return Integer.valueOf(System.identityHashCode(proxy));
                    } else if ("equals".equals(name)) {
                        return Boolean.valueOf(proxy == (args != null && args.length > 0 ? args[0] : null));
                    }
                    return null;
                }
            }
        );
    }

    private Argument createMockArgument(final String preferredName, final Set triggers, final Set prefixes) {
        return (Argument) Proxy.newProxyInstance(
            Argument.class.getClassLoader(),
            new Class[] { Argument.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String name = method.getName();
                    if ("getPreferredName".equals(name)) {
                        return preferredName;
                    } else if ("getTriggers".equals(name)) {
                        return triggers == null ? Collections.EMPTY_SET : triggers;
                    } else if ("getPrefixes".equals(name)) {
                        return prefixes == null ? Collections.EMPTY_SET : prefixes;
                    } else if ("toString".equals(name)) {
                        return "MockArgument[" + preferredName + "]";
                    } else if ("hashCode".equals(name)) {
                        return Integer.valueOf(System.identityHashCode(proxy));
                    } else if ("equals".equals(name)) {
                        return Boolean.valueOf(proxy == (args != null && args.length > 0 ? args[0] : null));
                    }
                    return null;
                }
            }
        );
    }

    @Before
    public void setUp() {
        Set prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");
        prefixes.add("+");

        rootOption = createMockOption("root", Collections.EMPTY_SET, prefixes);
        arguments = new ArrayList();
        arguments.add("--file");
        arguments.add("test doc.txt");
        arguments.add("-v");

        commandLine = new WriteableCommandLineImpl(rootOption, arguments);
    }

    @Test
    public void testConstructorAndGetNormalised() {
        List normalised = commandLine.getNormalised();
        Assert.assertEquals(3, normalised.size());
        Assert.assertEquals("--file", normalised.get(0));
        Assert.assertEquals("test doc.txt", normalised.get(1));
        Assert.assertEquals("-v", normalised.get(2));

        try {
            normalised.add("extra");
            Assert.fail("getNormalised() should return an unmodifiable list");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testAddOptionAndHasOptionAndGetOption() {
        Set triggers = new HashSet();
        triggers.add("-f");
        triggers.add("--file");
        Option fileOpt = createMockOption("--file", triggers, Collections.EMPTY_SET);

        Assert.assertFalse(commandLine.hasOption(fileOpt));
        Assert.assertNull(commandLine.getOption("-f"));
        Assert.assertNull(commandLine.getOption("--file"));

        commandLine.addOption(fileOpt);

        Assert.assertTrue(commandLine.hasOption(fileOpt));
        Assert.assertSame(fileOpt, commandLine.getOption("-f"));
        Assert.assertSame(fileOpt, commandLine.getOption("--file"));
        Assert.assertNull(commandLine.getOption("--unknown"));

        List options = commandLine.getOptions();
        Assert.assertEquals(1, options.size());
        Assert.assertSame(fileOpt, options.get(0));

        try {
            options.add(fileOpt);
            Assert.fail("getOptions() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        Set optionTriggers = commandLine.getOptionTriggers();
        Assert.assertTrue(optionTriggers.contains("-f"));
        Assert.assertTrue(optionTriggers.contains("--file"));

        try {
            optionTriggers.add("-x");
            Assert.fail("getOptionTriggers() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testAddValueWithArgument() {
        Argument argOpt = createMockArgument("arg", Collections.singleton("arg"), Collections.EMPTY_SET);

        commandLine.addValue(argOpt, "val1");
        commandLine.addValue(argOpt, "val2");

        Assert.assertTrue(commandLine.hasOption(argOpt));

        List values = commandLine.getUndefaultedValues(argOpt);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("val1", values.get(0));
        Assert.assertEquals("val2", values.get(1));
    }

    @Test
    public void testAddValueWithNonArgument() {
        Option opt = createMockOption("opt", Collections.EMPTY_SET, Collections.EMPTY_SET);

        commandLine.addValue(opt, "val");

        Assert.assertFalse(commandLine.hasOption(opt));
        List values = commandLine.getUndefaultedValues(opt);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("val", values.get(0));
    }

    @Test
    public void testGetUndefaultedValuesEmpty() {
        Option opt = createMockOption("opt", Collections.EMPTY_SET, Collections.EMPTY_SET);
        List values = commandLine.getUndefaultedValues(opt);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());
    }

    @Test
    public void testAddSwitchSuccess() {
        Option switchOpt = createMockOption("--verbose", Collections.singleton("-v"), Collections.EMPTY_SET);

        commandLine.addSwitch(switchOpt, true);

        Assert.assertTrue(commandLine.hasOption(switchOpt));
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(switchOpt, Boolean.FALSE));
    }

    @Test
    public void testAddSwitchFalse() {
        Option switchOpt = createMockOption("+v", Collections.singleton("+v"), Collections.EMPTY_SET);

        commandLine.addSwitch(switchOpt, false);

        Assert.assertTrue(commandLine.hasOption(switchOpt));
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch(switchOpt, null));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitchDuplicateThrowsException() {
        Option switchOpt = createMockOption("--verbose", Collections.EMPTY_SET, Collections.EMPTY_SET);
        commandLine.addSwitch(switchOpt, true);
        commandLine.addSwitch(switchOpt, false);
    }

    @Test
    public void testGetSwitchFallbackChain() {
        Option switchOpt = createMockOption("--debug", Collections.EMPTY_SET, Collections.EMPTY_SET);

        // 1. Initially no switch set, no default set, defaultValue param null
        Assert.assertNull(commandLine.getSwitch(switchOpt, null));

        // 2. Default value parameter takes precedence over option default if present
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(switchOpt, Boolean.TRUE));

        // 3. Set defaultSwitch on commandLine
        commandLine.setDefaultSwitch(switchOpt, Boolean.TRUE);
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(switchOpt, null));

        // Reset defaultSwitch to null
        commandLine.setDefaultSwitch(switchOpt, null);
        Assert.assertNull(commandLine.getSwitch(switchOpt, null));

        // 4. Added switch takes precedence over all
        commandLine.setDefaultSwitch(switchOpt, Boolean.TRUE);
        commandLine.addSwitch(switchOpt, false);
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch(switchOpt, Boolean.TRUE));
    }

    @Test
    public void testGetValuesVariousScenarios() {
        Option opt = createMockOption("opt", Collections.EMPTY_SET, Collections.EMPTY_SET);

        // Case 1: No values, no defaultValues -> empty list
        List res = commandLine.getValues(opt, null);
        Assert.assertEquals(Collections.EMPTY_LIST, res);

        // Case 2: No values, commandLine defaultValues set
        List cmdDefaults = Arrays.asList(new Object[] { "def1", "def2" });
        commandLine.setDefaultValues(opt, cmdDefaults);
        res = commandLine.getValues(opt, null);
        Assert.assertEquals(cmdDefaults, res);

        // Clear commandLine defaultValues
        commandLine.setDefaultValues(opt, null);
        res = commandLine.getValues(opt, null);
        Assert.assertEquals(Collections.EMPTY_LIST, res);

        // Case 3: No values, explicit defaultValues argument supplied
        List paramDefaults = Arrays.asList(new Object[] { "paramDef1" });
        res = commandLine.getValues(opt, paramDefaults);
        Assert.assertEquals(paramDefaults, res);

        // Case 4: Values added, defaultValues null -> returns added values
        commandLine.addValue(opt, "val1");
        res = commandLine.getValues(opt, null);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("val1", res.get(0));

        // Case 5: Values added (size 1), defaults provided (size 3) -> augmented list
        List longerDefaults = Arrays.asList(new Object[] { "d1", "d2", "d3" });
        res = commandLine.getValues(opt, longerDefaults);
        Assert.assertEquals(3, res.size());
        Assert.assertEquals("val1", res.get(0));
        Assert.assertEquals("d2", res.get(1));
        Assert.assertEquals("d3", res.get(2));

        // Case 6: Values added (size 1), defaults provided (size 1) -> does not augment
        List sameSizeDefaults = Arrays.asList(new Object[] { "d1" });
        res = commandLine.getValues(opt, sameSizeDefaults);
        Assert.assertEquals(1, res.size());
        Assert.assertEquals("val1", res.get(0));
    }

    @Test
    public void testPropertiesMethodsWithOptions() {
        Option opt = createMockOption("propOpt", Collections.EMPTY_SET, Collections.EMPTY_SET);

        Assert.assertEquals(Collections.EMPTY_SET, commandLine.getProperties(opt));
        Assert.assertNull(commandLine.getProperty(opt, "key1", null));
        Assert.assertEquals("defaultVal", commandLine.getProperty(opt, "key1", "defaultVal"));

        commandLine.addProperty(opt, "key1", "value1");
        commandLine.addProperty(opt, "key2", "value2");

        Set keys = commandLine.getProperties(opt);
        Assert.assertEquals(2, keys.size());
        Assert.assertTrue(keys.contains("key1"));
        Assert.assertTrue(keys.contains("key2"));

        Assert.assertEquals("value1", commandLine.getProperty(opt, "key1", "defaultVal"));
        Assert.assertEquals("value2", commandLine.getProperty(opt, "key2", "defaultVal"));
        Assert.assertEquals("defaultVal", commandLine.getProperty(opt, "key3", "defaultVal"));
    }

    @Test
    public void testDefaultPropertyOptionMethods() {
        Assert.assertEquals(Collections.EMPTY_SET, commandLine.getProperties());
        Assert.assertNull(commandLine.getProperty("myProp"));

        commandLine.addProperty("myProp", "myVal");

        Assert.assertEquals("myVal", commandLine.getProperty("myProp"));
        Set propKeys = commandLine.getProperties();
        Assert.assertTrue(propKeys.contains("myProp"));
    }

    @Test
    public void testLooksLikeOption() {
        Assert.assertTrue(commandLine.looksLikeOption("-v"));
        Assert.assertTrue(commandLine.looksLikeOption("--help"));
        Assert.assertTrue(commandLine.looksLikeOption("+s"));
        Assert.assertFalse(commandLine.looksLikeOption("value"));
        Assert.assertFalse(commandLine.looksLikeOption(""));

        // Option with empty prefixes
        Option noPrefixRoot = createMockOption("noprefix", Collections.EMPTY_SET, Collections.EMPTY_SET);
        WriteableCommandLineImpl clNoPrefix = new WriteableCommandLineImpl(noPrefixRoot, Collections.EMPTY_LIST);
        Assert.assertFalse(clNoPrefix.looksLikeOption("-v"));
    }

    @Test
    public void testToStringFormatting() {
        // Normalised list: ["--file", "test doc.txt", "-v"]
        Assert.assertEquals("--file \"test doc.txt\" -v", commandLine.toString());

        // Empty normalised list
        WriteableCommandLineImpl emptyCl = new WriteableCommandLineImpl(rootOption, Collections.EMPTY_LIST);
        Assert.assertEquals("", emptyCl.toString());

        // Single argument without spaces
        List singleArg = Collections.singletonList("single");
        WriteableCommandLineImpl singleCl = new WriteableCommandLineImpl(rootOption, singleArg);
        Assert.assertEquals("single", singleCl.toString());

        // Single argument with spaces
        List spaceArg = Collections.singletonList("spaced value");
        WriteableCommandLineImpl spaceCl = new WriteableCommandLineImpl(rootOption, spaceArg);
        Assert.assertEquals("\"spaced value\"", spaceCl.toString());
    }
}