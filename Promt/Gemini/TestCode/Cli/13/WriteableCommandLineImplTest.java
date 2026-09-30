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

    private Option createMockOption(final String preferredName, final Set triggers, final Set prefixes) {
        return (Option) Proxy.newProxyInstance(
            Option.class.getClassLoader(),
            new Class<?>[] { Option.class },
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
                        return prefixes != null ? prefixes : Collections.emptySet();
                    }
                    if ("equals".equals(name)) {
                        return proxy == args[0];
                    }
                    if ("hashCode".equals(name)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("toString".equals(name)) {
                        return "MockOption[" + preferredName + "]";
                    }
                    return null;
                }
            }
        );
    }

    private Argument createMockArgument(final String preferredName, final Set triggers, final Set prefixes) {
        return (Argument) Proxy.newProxyInstance(
            Argument.class.getClassLoader(),
            new Class<?>[] { Argument.class },
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
                        return prefixes != null ? prefixes : Collections.emptySet();
                    }
                    if ("equals".equals(name)) {
                        return proxy == args[0];
                    }
                    if ("hashCode".equals(name)) {
                        return System.identityHashCode(proxy);
                    }
                    if ("toString".equals(name)) {
                        return "MockArgument[" + preferredName + "]";
                    }
                    return null;
                }
            }
        );
    }

    @Before
    public void setUp() {
        Set prefixes = new HashSet(Arrays.asList("--", "-", "+"));
        rootOption = createMockOption("root", Collections.singleton("root"), prefixes);
        arguments = new ArrayList(Arrays.asList("--file", "test.txt", "arg with space"));
        commandLine = new WriteableCommandLineImpl(rootOption, arguments);
    }

    @Test
    public void testConstructorAndGetNormalised() {
        List normalised = commandLine.getNormalised();
        Assert.assertEquals(3, normalised.size());
        Assert.assertEquals("--file", normalised.get(0));
        Assert.assertEquals("test.txt", normalised.get(1));
        Assert.assertEquals("arg with space", normalised.get(2));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetNormalisedIsUnmodifiable() {
        commandLine.getNormalised().add("new-arg");
    }

    @Test
    public void testAddOptionAndHasOption() {
        Set triggers = new HashSet(Arrays.asList("-h", "--help", "-?"));
        Option helpOpt = createMockOption("--help", triggers, Collections.singleton("-"));

        Assert.assertFalse(commandLine.hasOption(helpOpt));
        Assert.assertNull(commandLine.getOption("-h"));
        Assert.assertNull(commandLine.getOption("--help"));
        Assert.assertNull(commandLine.getOption("-?"));

        commandLine.addOption(helpOpt);

        Assert.assertTrue(commandLine.hasOption(helpOpt));
        Assert.assertSame(helpOpt, commandLine.getOption("-h"));
        Assert.assertSame(helpOpt, commandLine.getOption("--help"));
        Assert.assertSame(helpOpt, commandLine.getOption("-?"));
        Assert.assertSame(helpOpt, commandLine.getOption("--help"));

        List options = commandLine.getOptions();
        Assert.assertEquals(1, options.size());
        Assert.assertSame(helpOpt, options.get(0));

        Set allTriggers = commandLine.getOptionTriggers();
        Assert.assertTrue(allTriggers.contains("-h"));
        Assert.assertTrue(allTriggers.contains("--help"));
        Assert.assertTrue(allTriggers.contains("-?"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetOptionsIsUnmodifiable() {
        Option opt = createMockOption("-o", Collections.singleton("-o"), Collections.singleton("-"));
        commandLine.getOptions().add(opt);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetOptionTriggersIsUnmodifiable() {
        commandLine.getOptionTriggers().add("trigger");
    }

    @Test
    public void testAddValueForStandardOption() {
        Option opt = createMockOption("-v", Collections.singleton("-v"), Collections.singleton("-"));
        
        Assert.assertFalse(commandLine.hasOption(opt));
        commandLine.addValue(opt, "val1");
        commandLine.addValue(opt, "val2");

        Assert.assertFalse(commandLine.hasOption(opt)); // Not an argument, so addOption not called
        List vals = commandLine.getValues(opt, null);
        Assert.assertEquals(2, vals.size());
        Assert.assertEquals("val1", vals.get(0));
        Assert.assertEquals("val2", vals.get(1));
    }

    @Test
    public void testAddValueForArgument() {
        Argument arg = createMockArgument("arg", Collections.singleton("arg"), Collections.emptySet());
        
        Assert.assertFalse(commandLine.hasOption(arg));
        commandLine.addValue(arg, "argVal");
        
        Assert.assertTrue(commandLine.hasOption(arg)); // Argument triggers addOption
        List vals = commandLine.getValues(arg, null);
        Assert.assertEquals(1, vals.size());
        Assert.assertEquals("argVal", vals.get(0));
    }

    @Test
    public void testGetValuesFallbacks() {
        Option opt = createMockOption("-f", Collections.singleton("-f"), Collections.singleton("-"));
        
        // 1. All empty/null -> returns Collections.EMPTY_LIST
        List res1 = commandLine.getValues(opt, null);
        Assert.assertNotNull(res1);
        Assert.assertTrue(res1.isEmpty());

        // 2. Default values provided to method when no values present
        List methodDefaults = Arrays.asList("def1", "def2");
        List res2 = commandLine.getValues(opt, methodDefaults);
        Assert.assertEquals(methodDefaults, res2);

        // 3. Option's default values when method defaults are null or empty
        List optDefaults = Arrays.asList("optDef1");
        commandLine.setDefaultValues(opt, optDefaults);
        List res3 = commandLine.getValues(opt, null);
        Assert.assertEquals(optDefaults, res3);
        List res3EmptyParam = commandLine.getValues(opt, Collections.emptyList());
        Assert.assertEquals(optDefaults, res3EmptyParam);

        // 4. Actual command line values override both
        commandLine.addValue(opt, "actualVal");
        List res4 = commandLine.getValues(opt, methodDefaults);
        Assert.assertEquals(1, res4.size());
        Assert.assertEquals("actualVal", res4.get(0));

        // 5. Remove option defaults by passing null
        commandLine.setDefaultValues(opt, null);
        Option opt2 = createMockOption("-g", Collections.singleton("-g"), Collections.singleton("-"));
        commandLine.setDefaultValues(opt2, Arrays.asList("gDef"));
        commandLine.setDefaultValues(opt2, null);
        Assert.assertTrue(commandLine.getValues(opt2, null).isEmpty());
    }

    @Test
    public void testAddSwitchAndGetSwitch() {
        Option switchOpt = createMockOption("-s", Collections.singleton("-s"), Collections.singleton("-"));

        Assert.assertNull(commandLine.getSwitch(switchOpt, null));
        Assert.assertTrue(commandLine.getSwitch(switchOpt, Boolean.TRUE));
        Assert.assertFalse(commandLine.getSwitch(switchOpt, Boolean.FALSE));

        commandLine.setDefaultSwitch(switchOpt, Boolean.TRUE);
        Assert.assertTrue(commandLine.getSwitch(switchOpt, null));

        commandLine.setDefaultSwitch(switchOpt, null);
        Assert.assertNull(commandLine.getSwitch(switchOpt, null));

        commandLine.addSwitch(switchOpt, true);
        Assert.assertTrue(commandLine.hasOption(switchOpt));
        Assert.assertTrue(commandLine.getSwitch(switchOpt, null));
        Assert.assertTrue(commandLine.getSwitch(switchOpt, Boolean.FALSE)); // CLI value overrides defaultValue param
    }

    @Test
    public void testAddSwitchFalse() {
        Option switchOpt = createMockOption("-s", Collections.singleton("-s"), Collections.singleton("-"));
        commandLine.addSwitch(switchOpt, false);
        Assert.assertFalse(commandLine.getSwitch(switchOpt, null));
        Assert.assertFalse(commandLine.getSwitch(switchOpt, Boolean.TRUE));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitchTwiceThrowsException() {
        Option switchOpt = createMockOption("-s", Collections.singleton("-s"), Collections.singleton("-"));
        commandLine.addSwitch(switchOpt, true);
        commandLine.addSwitch(switchOpt, true);
    }

    @Test(expected = IllegalStateException.class)
    public void testAddSwitchTwiceDifferentValuesThrowsException() {
        Option switchOpt = createMockOption("-s", Collections.singleton("-s"), Collections.singleton("-"));
        commandLine.addSwitch(switchOpt, true);
        commandLine.addSwitch(switchOpt, false);
    }

    @Test
    public void testProperties() {
        Assert.assertNull(commandLine.getProperty("user.name", null));
        Assert.assertEquals("defaultVal", commandLine.getProperty("user.name", "defaultVal"));
        Assert.assertTrue(commandLine.getProperties().isEmpty());

        commandLine.addProperty("user.name", "john");
        commandLine.addProperty("timeout", "30");

        Assert.assertEquals("john", commandLine.getProperty("user.name", null));
        Assert.assertEquals("30", commandLine.getProperty("timeout", "10"));
        Assert.assertEquals(2, commandLine.getProperties().size());
        Assert.assertTrue(commandLine.getProperties().contains("user.name"));
        Assert.assertTrue(commandLine.getProperties().contains("timeout"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetPropertiesIsUnmodifiable() {
        commandLine.getProperties().add("key");
    }

    @Test
    public void testLooksLikeOption() {
        Assert.assertTrue(commandLine.looksLikeOption("--file"));
        Assert.assertTrue(commandLine.looksLikeOption("-f"));
        Assert.assertTrue(commandLine.looksLikeOption("+opt"));
        Assert.assertFalse(commandLine.looksLikeOption("file"));
        Assert.assertFalse(commandLine.looksLikeOption(""));
    }

    @Test
    public void testToString() {
        Assert.assertEquals("--file test.txt \"arg with space\"", commandLine.toString());

        WriteableCommandLineImpl emptyCmd = new WriteableCommandLineImpl(rootOption, Collections.emptyList());
        Assert.assertEquals("", emptyCmd.toString());

        WriteableCommandLineImpl singleArgCmd = new WriteableCommandLineImpl(rootOption, Collections.singletonList("single"));
        Assert.assertEquals("single", singleArgCmd.toString());

        WriteableCommandLineImpl singleSpacedCmd = new WriteableCommandLineImpl(rootOption, Collections.singletonList("has space"));
        Assert.assertEquals("\"has space\"", singleSpacedCmd.toString());
    }
}