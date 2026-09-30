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

    private Set prefixes;
    private List arguments;
    private Option rootOption;
    private WriteableCommandLineImpl commandLine;

    @Before
    public void setUp() {
        prefixes = new HashSet();
        prefixes.add("-");
        prefixes.add("--");

        arguments = new ArrayList();
        arguments.add("--foo");
        arguments.add("bar");

        rootOption = createOption("root", Collections.emptySet(), prefixes);
        commandLine = new WriteableCommandLineImpl(rootOption, arguments);
    }

    private Option createOption(final String preferredName, final Set triggers, final Set prefixSet) {
        return (Option) Proxy.newProxyInstance(
            Option.class.getClassLoader(),
            new Class[] { Option.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String name = method.getName();
                    if ("getPreferredName".equals(name)) {
                        return preferredName;
                    }
                    if ("getTriggers".equals(name)) {
                        return triggers != null ? triggers : Collections.emptySet();
                    }
                    if ("getPrefixes".equals(name)) {
                        return prefixSet != null ? prefixSet : Collections.emptySet();
                    }
                    if ("equals".equals(name)) {
                        return proxy == args[0];
                    }
                    if ("hashCode".equals(name)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("toString".equals(name)) {
                        return preferredName != null ? preferredName : "MockOption";
                    }
                    return null;
                }
            }
        );
    }

    private Argument createArgument(final String preferredName, final Set triggers, final Set prefixSet) {
        return (Argument) Proxy.newProxyInstance(
            Argument.class.getClassLoader(),
            new Class[] { Argument.class },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String name = method.getName();
                    if ("getPreferredName".equals(name)) {
                        return preferredName;
                    }
                    if ("getTriggers".equals(name)) {
                        return triggers != null ? triggers : Collections.emptySet();
                    }
                    if ("getPrefixes".equals(name)) {
                        return prefixSet != null ? prefixSet : Collections.emptySet();
                    }
                    if ("equals".equals(name)) {
                        return proxy == args[0];
                    }
                    if ("hashCode".equals(name)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("toString".equals(name)) {
                        return preferredName != null ? preferredName : "MockArgument";
                    }
                    return null;
                }
            }
        );
    }

    @Test
    public void testAddOptionAndQueries() {
        Set triggers = new HashSet();
        triggers.add("-f");
        triggers.add("--file");

        Option option = createOption("--file", triggers, prefixes);

        Assert.assertFalse(commandLine.hasOption(option));
        Assert.assertNull(commandLine.getOption("-f"));
        Assert.assertNull(commandLine.getOption("--file"));

        commandLine.addOption(option);

        Assert.assertTrue(commandLine.hasOption(option));
        Assert.assertSame(option, commandLine.getOption("-f"));
        Assert.assertSame(option, commandLine.getOption("--file"));
        Assert.assertTrue(commandLine.getOptions().contains(option));
        Assert.assertEquals(1, commandLine.getOptions().size());

        Set recordedTriggers = commandLine.getOptionTriggers();
        Assert.assertTrue(recordedTriggers.contains("-f"));
        Assert.assertTrue(recordedTriggers.contains("--file"));
    }

    @Test
    public void testAddValueRegularOption() {
        Option option = createOption("--opt", Collections.emptySet(), prefixes);

        Assert.assertFalse(commandLine.hasOption(option));
        Assert.assertEquals(Collections.EMPTY_LIST, commandLine.getUndefaultedValues(option));

        commandLine.addValue(option, "val1");
        commandLine.addValue(option, "val2");

        // Regular option is not added to options list by addValue
        Assert.assertFalse(commandLine.hasOption(option));

        List values = commandLine.getValues(option, null);
        Assert.assertEquals(2, values.size());
        Assert.assertEquals("val1", values.get(0));
        Assert.assertEquals("val2", values.get(1));

        List undefaulted = commandLine.getUndefaultedValues(option);
        Assert.assertEquals(2, undefaulted.size());
        Assert.assertEquals("val1", undefaulted.get(0));
        Assert.assertEquals("val2", undefaulted.get(1));
    }

    @Test
    public void testAddValueArgumentOption() {
        Set triggers = new HashSet();
        triggers.add("argTrig");
        Argument argument = createArgument("myArg", triggers, prefixes);

        Assert.assertFalse(commandLine.hasOption(argument));

        commandLine.addValue(argument, "argVal");

        // Argument option is automatically added to options list by addValue
        Assert.assertTrue(commandLine.hasOption(argument));
        Assert.assertSame(argument, commandLine.getOption("myArg"));
        Assert.assertSame(argument, commandLine.getOption("argTrig"));

        List values = commandLine.getValues(argument, null);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("argVal", values.get(0));
    }

    @Test
    public void testGetValuesFallbacks() {
        Option option = createOption("--fallback", Collections.emptySet(), prefixes);

        // 1. When no values, no supplied defaults, no registered defaults
        List values = commandLine.getValues(option, null);
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());

        // 2. When empty list passed as defaultValues and no registered defaults
        values = commandLine.getValues(option, Collections.emptyList());
        Assert.assertNotNull(values);
        Assert.assertTrue(values.isEmpty());

        // 3. When registered defaults exist and no values / empty supplied defaults
        List registeredDefaults = Arrays.asList(new Object[] {"default1", "default2"});
        commandLine.setDefaultValues(option, registeredDefaults);

        values = commandLine.getValues(option, null);
        Assert.assertEquals(registeredDefaults, values);

        values = commandLine.getValues(option, Collections.emptyList());
        Assert.assertEquals(registeredDefaults, values);

        // 4. When supplied defaults are provided, they take precedence over registered defaults
        List suppliedDefaults = Arrays.asList(new Object[] {"supplied1"});
        values = commandLine.getValues(option, suppliedDefaults);
        Assert.assertEquals(suppliedDefaults, values);

        // 5. When actual values are added, they take precedence over all defaults
        commandLine.addValue(option, "actual");
        values = commandLine.getValues(option, suppliedDefaults);
        Assert.assertEquals(1, values.size());
        Assert.assertEquals("actual", values.get(0));

        // 6. Clearing registered defaults
        Option anotherOption = createOption("--other", Collections.emptySet(), prefixes);
        commandLine.setDefaultValues(anotherOption, registeredDefaults);
        Assert.assertEquals(registeredDefaults, commandLine.getValues(anotherOption, null));
        commandLine.setDefaultValues(anotherOption, null);
        Assert.assertEquals(Collections.EMPTY_LIST, commandLine.getValues(anotherOption, null));
    }

    @Test
    public void testAddSwitchAndGetSwitch() {
        Option option = createOption("--switch", Collections.emptySet(), prefixes);

        Assert.assertNull(commandLine.getSwitch(option, null));
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(option, Boolean.TRUE));
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch(option, Boolean.FALSE));

        // Set default switch
        commandLine.setDefaultSwitch(option, Boolean.TRUE);
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(option, null));
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch(option, Boolean.FALSE));

        // Add switch (true)
        commandLine.addSwitch(option, true);
        Assert.assertTrue(commandLine.hasOption(option));
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(option, null));
        Assert.assertEquals(Boolean.TRUE, commandLine.getSwitch(option, Boolean.FALSE));

        // Reset default switch to null
        Option option2 = createOption("--switch2", Collections.emptySet(), prefixes);
        commandLine.setDefaultSwitch(option2, Boolean.FALSE);
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch(option2, null));
        commandLine.setDefaultSwitch(option2, null);
        Assert.assertNull(commandLine.getSwitch(option2, null));
    }

    @Test
    public void testAddSwitchFalse() {
        Option option = createOption("--switchFalse", Collections.emptySet(), prefixes);
        commandLine.addSwitch(option, false);
        Assert.assertTrue(commandLine.hasOption(option));
        Assert.assertEquals(Boolean.FALSE, commandLine.getSwitch(option, Boolean.TRUE));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitchDuplicateThrowsException() {
        Option option = createOption("--dupSwitch", Collections.emptySet(), prefixes);
        commandLine.addSwitch(option, true);
        commandLine.addSwitch(option, false);
    }

    @Test
    public void testPropertiesWithOption() {
        Option option = createOption("--propOpt", Collections.emptySet(), prefixes);

        Assert.assertEquals(Collections.EMPTY_SET, commandLine.getProperties(option));
        Assert.assertNull(commandLine.getProperty(option, "key", null));
        Assert.assertEquals("defaultVal", commandLine.getProperty(option, "key", "defaultVal"));

        commandLine.addProperty(option, "key1", "val1");
        commandLine.addProperty(option, "key2", "val2");

        Set props = commandLine.getProperties(option);
        Assert.assertEquals(2, props.size());
        Assert.assertTrue(props.contains("key1"));
        Assert.assertTrue(props.contains("key2"));

        Assert.assertEquals("val1", commandLine.getProperty(option, "key1", "default"));
        Assert.assertEquals("val2", commandLine.getProperty(option, "key2", "default"));
        Assert.assertEquals("default", commandLine.getProperty(option, "unknownKey", "default"));
    }

    @Test
    public void testDefaultPropertyOption() {
        Assert.assertEquals(Collections.EMPTY_SET, commandLine.getProperties());
        Assert.assertNull(commandLine.getProperty("propName"));

        commandLine.addProperty("propName", "propVal");

        Set props = commandLine.getProperties();
        Assert.assertEquals(1, props.size());
        Assert.assertTrue(props.contains("propName"));
        Assert.assertEquals("propVal", commandLine.getProperty("propName"));
    }

    @Test
    public void testLooksLikeOption() {
        Assert.assertTrue(commandLine.looksLikeOption("-f"));
        Assert.assertTrue(commandLine.looksLikeOption("--file"));
        Assert.assertTrue(commandLine.looksLikeOption("-"));
        Assert.assertFalse(commandLine.looksLikeOption("file"));
        Assert.assertFalse(commandLine.looksLikeOption(""));

        Option emptyPrefixOption = createOption("rootEmpty", Collections.emptySet(), Collections.emptySet());
        WriteableCommandLineImpl emptyCmdLine = new WriteableCommandLineImpl(emptyPrefixOption, Collections.emptyList());
        Assert.assertFalse(emptyCmdLine.looksLikeOption("--file"));
    }

    @Test
    public void testToStringFormatting() {
        List args = new ArrayList();
        args.add("--file");
        args.add("my document.txt");
        args.add("-v");
        args.add("another space arg");

        WriteableCommandLineImpl cmd = new WriteableCommandLineImpl(rootOption, args);
        String formatted = cmd.toString();
        Assert.assertEquals("--file \"my document.txt\" -v \"another space arg\"", formatted);

        WriteableCommandLineImpl emptyCmd = new WriteableCommandLineImpl(rootOption, Collections.emptyList());
        Assert.assertEquals("", emptyCmd.toString());

        WriteableCommandLineImpl singleArgCmd = new WriteableCommandLineImpl(rootOption, Collections.singletonList("single"));
        Assert.assertEquals("single", singleArgCmd.toString());

        WriteableCommandLineImpl singleSpaceArgCmd = new WriteableCommandLineImpl(rootOption, Collections.singletonList("single space"));
        Assert.assertEquals("\"single space\"", singleSpaceArgCmd.toString());
    }

    @Test
    public void testGetNormalised() {
        List norm = commandLine.getNormalised();
        Assert.assertEquals(2, norm.size());
        Assert.assertEquals("--foo", norm.get(0));
        Assert.assertEquals("bar", norm.get(1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetOptionsUnmodifiable() {
        commandLine.getOptions().add(createOption("--new", Collections.emptySet(), prefixes));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetOptionTriggersUnmodifiable() {
        commandLine.getOptionTriggers().add("trigger");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetNormalisedUnmodifiable() {
        commandLine.getNormalised().add("extra");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetPropertiesUnmodifiable() {
        Option opt = new PropertyOption();
        commandLine.addProperty(opt, "k", "v");
        commandLine.getProperties(opt).add("newKey");
    }
}