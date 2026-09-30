package org.apache.commons.cli2.builder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.option.DefaultOption;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PatternBuilderTest {

    private PatternBuilder builder;
    private Parser parser;
    private GroupBuilder groupBuilder;

    @Before
    public void setUp() {
        builder = new PatternBuilder();
        parser = new Parser();
        groupBuilder = new GroupBuilder();
    }

    @Test
    public void testCustomConstructor() {
        GroupBuilder gb = new GroupBuilder();
        DefaultOptionBuilder ob = new DefaultOptionBuilder();
        ArgumentBuilder ab = new ArgumentBuilder();
        PatternBuilder pb = new PatternBuilder(gb, ob, ab);

        pb.withPattern("a");
        Option opt = pb.create();
        Assert.assertNotNull(opt);
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertEquals("-a", dopt.getPreferredName());
    }

    @Test
    public void testEmptyPattern() {
        builder.withPattern("");
        Option opt = builder.create();
        Assert.assertNotNull(opt);
        Assert.assertTrue(opt instanceof Group);
    }

    @Test
    public void testReset() {
        builder.withPattern("a");
        PatternBuilder returned = builder.reset();
        Assert.assertSame(builder, returned);

        Option opt = builder.create();
        Assert.assertTrue(opt instanceof Group);
    }

    @Test
    public void testCreateResetsBuilder() {
        builder.withPattern("a");
        Option first = builder.create();
        Assert.assertTrue(first instanceof DefaultOption);

        Option second = builder.create();
        Assert.assertTrue(second instanceof Group);
    }

    @Test
    public void testSimpleOptionWithoutArgument() throws OptionException {
        builder.withPattern("a");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertEquals("-a", dopt.getPreferredName());
        Assert.assertFalse(dopt.isRequired());
        Assert.assertNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-a"});
        Assert.assertTrue(cl.hasOption("-a"));
    }

    @Test
    public void testRequiredOptionWithoutArgument() throws OptionException {
        builder.withPattern("a!");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertEquals("-a", dopt.getPreferredName());
        Assert.assertTrue(dopt.isRequired());
        Assert.assertNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-a"});
        Assert.assertTrue(cl.hasOption("-a"));

        try {
            parser.parse(new String[]{});
            Assert.fail("Expected OptionException for missing required option");
        } catch (OptionException e) {
            // expected
        }
    }

    @Test
    public void testPrefixRequiredOption() throws OptionException {
        builder.withPattern("!a");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertEquals("-a", dopt.getPreferredName());
        Assert.assertTrue(dopt.isRequired());
        Assert.assertNull(dopt.getArgument());
    }

    @Test
    public void testStringArgumentOption() throws OptionException {
        builder.withPattern("s:");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-s", "hello"});
        Assert.assertTrue(cl.hasOption("-s"));
        Assert.assertEquals("hello", cl.getValue("-s"));
    }

    @Test
    public void testClassInstanceValidatorOption() throws OptionException {
        builder.withPattern("c@");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-c", "java.util.Date"});
        Assert.assertTrue(cl.hasOption("-c"));
        Object value = cl.getValue("-c");
        Assert.assertTrue(value instanceof Date);

        try {
            parser.parse(new String[]{"-c", "org.apache.commons.cli2.NonExistentClass12345"});
            Assert.fail("Expected OptionException for non-existent class");
        } catch (OptionException e) {
            // expected
        }
    }

    @Test
    public void testClassValidatorOption() throws OptionException {
        builder.withPattern("c+");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-c", "java.util.Date"});
        Assert.assertTrue(cl.hasOption("-c"));
        Object value = cl.getValue("-c");
        Assert.assertEquals(Date.class, value);

        try {
            parser.parse(new String[]{"-c", "org.apache.commons.cli2.NonExistentClass12345"});
            Assert.fail("Expected OptionException for non-existent class");
        } catch (OptionException e) {
            // expected
        }
    }

    @Test
    public void testNumberValidatorOption() throws OptionException {
        builder.withPattern("n%");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-n", "12345"});
        Assert.assertTrue(cl.hasOption("-n"));
        Assert.assertEquals(new Long(12345), cl.getValue("-n"));

        try {
            parser.parse(new String[]{"-n", "not_a_number"});
            Assert.fail("Expected OptionException for invalid number");
        } catch (OptionException e) {
            // expected
        }
    }

    @Test
    public void testDateValidatorOption() {
        builder.withPattern("d#");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        try {
            parser.parse(new String[]{"-d", "invalid_date_format_value"});
            Assert.fail("Expected OptionException for invalid date format");
        } catch (OptionException e) {
            // expected
        }
    }

    @Test
    public void testExistingFileValidatorOption() throws IOException, OptionException {
        File tempFile = File.createTempFile("pattern_test", ".tmp");
        tempFile.deleteOnExit();

        builder.withPattern("f<");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-f", tempFile.getAbsolutePath()});
        Assert.assertTrue(cl.hasOption("-f"));
        Assert.assertEquals(tempFile, cl.getValue("-f"));

        try {
            parser.parse(new String[]{"-f", "/non/existent/file/" + System.currentTimeMillis() + ".txt"});
            Assert.fail("Expected OptionException for non-existing file");
        } catch (OptionException e) {
            // expected
        }
    }

    @Test
    public void testFileValidatorOption() throws OptionException {
        builder.withPattern("f>");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        String path = "/any/path/file.txt";
        CommandLine cl = parser.parse(new String[]{"-f", path});
        Assert.assertTrue(cl.hasOption("-f"));
        Assert.assertEquals(new File(path), cl.getValue("-f"));
    }

    @Test
    public void testMultipleFileValidatorOption() throws OptionException {
        builder.withPattern("m*");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-m", "file1.txt", "file2.txt"});
        Assert.assertTrue(cl.hasOption("-m"));
        Assert.assertEquals(2, cl.getValues("-m").size());
    }

    @Test
    public void testUrlValidatorOption() throws OptionException {
        builder.withPattern("u/");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-u", "http://commons.apache.org"});
        Assert.assertTrue(cl.hasOption("-u"));

        try {
            parser.parse(new String[]{"-u", "htp:/bad url %%%"});
            Assert.fail("Expected OptionException for invalid URL");
        } catch (OptionException e) {
            // expected
        }
    }

    @Test
    public void testComplexMultiOptionPattern() throws OptionException {
        builder.withPattern("a!b:c%d+e#f<g>h*i/j");
        Option groupOpt = builder.create();
        Assert.assertTrue(groupOpt instanceof Group);

        parser.setGroup((Group) groupOpt);

        CommandLine cl = parser.parse(new String[]{
            "-a",
            "-b", "stringVal",
            "-c", "999",
            "-d", "java.lang.String",
            "-g", "out.txt",
            "-j"
        });

        Assert.assertTrue(cl.hasOption("-a"));
        Assert.assertEquals("stringVal", cl.getValue("-b"));
        Assert.assertEquals(new Long(999), cl.getValue("-c"));
        Assert.assertEquals(String.class, cl.getValue("-d"));
        Assert.assertEquals(new File("out.txt"), cl.getValue("-g"));
        Assert.assertTrue(cl.hasOption("-j"));
    }

    @Test
    public void testRequiredArgumentOption() throws OptionException {
        builder.withPattern("r!:");
        Option opt = builder.create();
        Assert.assertTrue(opt instanceof DefaultOption);
        DefaultOption dopt = (DefaultOption) opt;
        Assert.assertTrue(dopt.isRequired());
        Assert.assertNotNull(dopt.getArgument());

        groupBuilder.reset();
        groupBuilder.withOption(opt);
        parser.setGroup(groupBuilder.create());

        CommandLine cl = parser.parse(new String[]{"-r", "val"});
        Assert.assertEquals("val", cl.getValue("-r"));

        try {
            parser.parse(new String[]{});
            Assert.fail("Expected OptionException for missing required option with arg");
        } catch (OptionException e) {
            // expected
        }
    }
}