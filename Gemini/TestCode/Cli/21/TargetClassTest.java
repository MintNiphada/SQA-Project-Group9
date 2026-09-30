package org.apache.commons.cli2;

import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.builder.SwitchBuilder;
import org.apache.commons.cli2.commandline.CommandLineImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class WriteableCommandLineTest {

    private Option rootGroup;
    private Option helpOption;
    private Option fileOption;
    private Option verboseSwitch;
    private Option propertyOption;
    private WriteableCommandLine writeableCommandLine;

    @Before
    public void setUp() {
        DefaultOptionBuilder oBuilder = new DefaultOptionBuilder();
        ArgumentBuilder aBuilder = new ArgumentBuilder();
        GroupBuilder gBuilder = new GroupBuilder();
        SwitchBuilder sBuilder = new SwitchBuilder();

        helpOption = oBuilder
                .withShortName("h")
                .withLongName("help")
                .withDescription("print help")
                .create();

        fileOption = oBuilder
                .withShortName("f")
                .withLongName("file")
                .withArgument(aBuilder.withName("path").create())
                .create();

        verboseSwitch = sBuilder
                .withShortName("v")
                .withLongName("verbose")
                .create();

        propertyOption = oBuilder
                .withShortName("D")
                .withArgument(aBuilder.withName("property").create())
                .create();

        rootGroup = gBuilder
                .withOption(helpOption)
                .withOption(fileOption)
                .withOption(verboseSwitch)
                .withOption(propertyOption)
                .create();

        writeableCommandLine = new CommandLineImpl(rootGroup, new ArrayList());
    }

    @Test
    public void testAddAndHasOption() {
        assertFalse(writeableCommandLine.hasOption(helpOption));
        assertFalse(writeableCommandLine.hasOption("-h"));
        assertFalse(writeableCommandLine.hasOption("--help"));

        writeableCommandLine.addOption(helpOption);

        assertTrue(writeableCommandLine.hasOption(helpOption));
        assertTrue(writeableCommandLine.hasOption("-h"));
        assertTrue(writeableCommandLine.hasOption("--help"));

        List options = writeableCommandLine.getOptions();
        assertNotNull(options);
        assertTrue(options.contains(helpOption));
    }

    @Test
    public void testAddValueAndGetValues() {
        assertNull(writeableCommandLine.getValue(fileOption));
        assertTrue(writeableCommandLine.getValues(fileOption).isEmpty());
        assertTrue(writeableCommandLine.getUndefaultedValues(fileOption).isEmpty());

        writeableCommandLine.addOption(fileOption);
        writeableCommandLine.addValue(fileOption, "config.xml");

        assertEquals("config.xml", writeableCommandLine.getValue(fileOption));
        assertEquals(Collections.singletonList("config.xml"), writeableCommandLine.getValues(fileOption));
        assertEquals(Collections.singletonList("config.xml"), writeableCommandLine.getUndefaultedValues(fileOption));

        writeableCommandLine.addValue(fileOption, "data.xml");
        List expected = Arrays.asList(new Object[]{"config.xml", "data.xml"});
        assertEquals(expected, writeableCommandLine.getValues(fileOption));
        assertEquals(expected, writeableCommandLine.getUndefaultedValues(fileOption));
    }

    @Test
    public void testDefaultValues() {
        List defaults = Arrays.asList(new Object[]{"default.txt", "backup.txt"});
        writeableCommandLine.setDefaultValues(fileOption, defaults);

        assertTrue(writeableCommandLine.getUndefaultedValues(fileOption).isEmpty());
        assertEquals(defaults, writeableCommandLine.getValues(fileOption));
        assertEquals("default.txt", writeableCommandLine.getValue(fileOption));

        writeableCommandLine.addOption(fileOption);
        writeableCommandLine.addValue(fileOption, "actual.txt");

        assertEquals(Collections.singletonList("actual.txt"), writeableCommandLine.getUndefaultedValues(fileOption));
        assertEquals(Collections.singletonList("actual.txt"), writeableCommandLine.getValues(fileOption));
    }

    @Test
    public void testAddSwitchAndGetSwitch() {
        assertNull(writeableCommandLine.getSwitch(verboseSwitch));

        writeableCommandLine.addOption(verboseSwitch);
        writeableCommandLine.addSwitch(verboseSwitch, true);

        assertEquals(Boolean.TRUE, writeableCommandLine.getSwitch(verboseSwitch));
        assertEquals(Boolean.TRUE, writeableCommandLine.getSwitch("-v"));

        try {
            writeableCommandLine.addSwitch(verboseSwitch, false);
            fail("Expected IllegalStateException when adding switch twice");
        } catch (IllegalStateException expected) {
            // Success
        }
    }

    @Test
    public void testDefaultSwitch() {
        writeableCommandLine.setDefaultSwitch(verboseSwitch, Boolean.FALSE);

        assertEquals(Boolean.FALSE, writeableCommandLine.getSwitch(verboseSwitch));
        assertEquals(Boolean.FALSE, writeableCommandLine.getSwitch("-v"));

        writeableCommandLine.addOption(verboseSwitch);
        writeableCommandLine.addSwitch(verboseSwitch, true);

        assertEquals(Boolean.TRUE, writeableCommandLine.getSwitch(verboseSwitch));
    }

    @Test
    public void testAddProperty() {
        writeableCommandLine.addProperty("globalKey", "globalValue");
        assertEquals("globalValue", writeableCommandLine.getProperty("globalKey"));

        Set properties = writeableCommandLine.getProperties();
        assertNotNull(properties);
        assertTrue(properties.contains("globalKey"));

        writeableCommandLine.addProperty(propertyOption, "myKey", "myVal");
        assertEquals("myVal", writeableCommandLine.getProperty(propertyOption, "myKey"));

        Set optProps = writeableCommandLine.getProperties(propertyOption);
        assertNotNull(optProps);
        assertTrue(optProps.contains("myKey"));
    }

    @Test
    public void testPropertyOverwrite() {
        writeableCommandLine.addProperty("key1", "val1");
        assertEquals("val1", writeableCommandLine.getProperty("key1"));

        writeableCommandLine.addProperty("key1", "val2");
        assertEquals("val2", writeableCommandLine.getProperty("key1"));

        writeableCommandLine.addProperty(propertyOption, "key1", "optVal1");
        assertEquals("optVal1", writeableCommandLine.getProperty(propertyOption, "key1"));

        writeableCommandLine.addProperty(propertyOption, "key1", "optVal2");
        assertEquals("optVal2", writeableCommandLine.getProperty(propertyOption, "key1"));
    }

    @Test
    public void testLooksLikeOption() {
        assertTrue(writeableCommandLine.looksLikeOption("-h"));
        assertTrue(writeableCommandLine.looksLikeOption("--help"));
        assertTrue(writeableCommandLine.looksLikeOption("-f"));
        assertFalse(writeableCommandLine.looksLikeOption("regular_arg"));
        assertFalse(writeableCommandLine.looksLikeOption(""));
    }

    @Test
    public void testGetOptionWithTriggers() {
        writeableCommandLine.addOption(helpOption);

        Option foundByShort = writeableCommandLine.getOption("-h");
        assertEquals(helpOption, foundByShort);

        Option foundByLong = writeableCommandLine.getOption("--help");
        assertEquals(helpOption, foundByLong);

        assertNull(writeableCommandLine.getOption("--unknown"));
    }
}