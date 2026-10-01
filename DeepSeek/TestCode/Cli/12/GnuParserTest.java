package org.apache.commons.cli;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class GnuParserTest {

    private GnuParser parser;

    @Before
    public void setUp() {
        parser = new GnuParser();
    }

    private Options createOptions(String... opts) {
        Options options = new Options();
        for (String opt : opts) {
            if (opt.startsWith("--")) {
                String longOpt = opt.substring(2);
                options.addOption(OptionBuilder.withLongOpt(longOpt).create());
            } else if (opt.startsWith("-")) {
                String shortOpt = opt.substring(1);
                options.addOption(OptionBuilder.create(shortOpt));
            } else {
                options.addOption(OptionBuilder.create(opt));
            }
        }
        return options;
    }

    @Test
    public void testFlattenEmptyArguments() {
        String[] result = parser.flatten(new Options(), new String[0], false);
        assertArrayEquals(new String[0], result);
    }

    @Test
    public void testFlattenNullArguments() {
        try {
            parser.flatten(new Options(), null, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFlattenNullOptions() {
        try {
            parser.flatten(null, new String[]{"-a"}, false);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFlattenSingleDash() {
        String[] result = parser.flatten(new Options(), new String[]{"-"}, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlattenDoubleDash() {
        Options options = createOptions("-a");
        String[] args = {"--", "-a", "value"};
        String[] expected = {"--", "-a", "value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenDoubleDashWithStopAtNonOption() {
        Options options = createOptions("-a");
        String[] args = {"--", "-a", "value"};
        String[] expected = {"--", "-a", "value"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenKnownShortOption() {
        Options options = createOptions("-a");
        String[] args = {"-a", "value"};
        String[] expected = {"-a", "value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenKnownLongOption() {
        Options options = createOptions("--foo");
        String[] args = {"--foo", "bar"};
        String[] expected = {"--foo", "bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenKnownLongOptionWithEquals() {
        Options options = createOptions("--foo");
        String[] args = {"--foo=bar"};
        String[] expected = {"--foo=bar"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenUnknownShortOptionStopAtNonOptionFalse() {
        Options options = createOptions("-a");
        String[] args = {"-x", "value"};
        String[] expected = {"-x", "value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenUnknownShortOptionStopAtNonOptionTrue() {
        Options options = createOptions("-a");
        String[] args = {"-x", "value", "-a"};
        String[] expected = {"-x", "value", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenUnknownLongOptionStopAtNonOptionFalse() {
        Options options = createOptions("--foo");
        String[] args = {"--bar", "value"};
        String[] expected = {"--bar", "value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenUnknownLongOptionStopAtNonOptionTrue() {
        Options options = createOptions("--foo");
        String[] args = {"--bar", "value", "--foo"};
        String[] expected = {"--bar", "value", "--foo"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenSpecialPropertyOption() {
        Options options = createOptions("-D");
        String[] args = {"-Dproperty=value"};
        String[] expected = {"-D", "property=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenSpecialPropertyOptionWithStopAtNonOption() {
        Options options = createOptions("-D");
        String[] args = {"-Dproperty=value", "-a"};
        String[] expected = {"-D", "property=value", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenSpecialPropertyOptionUnknownOption() {
        Options options = new Options(); // no -D
        String[] args = {"-Dproperty=value"};
        String[] expected = {"-Dproperty=value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenSpecialPropertyOptionUnknownOptionStopAtNonOption() {
        Options options = new Options(); // no -D
        String[] args = {"-Dproperty=value", "-a"};
        String[] expected = {"-Dproperty=value", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenNonOption() {
        Options options = createOptions("-a");
        String[] args = {"file", "-a"};
        String[] expected = {"file", "-a"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenNonOptionWithStopAtNonOption() {
        Options options = createOptions("-a");
        String[] args = {"file", "-a"};
        String[] expected = {"file", "-a"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenMixedOptions() {
        Options options = createOptions("-a", "-b", "--foo");
        String[] args = {"-a", "value", "-b", "--foo", "bar", "--", "-c"};
        String[] expected = {"-a", "value", "-b", "--foo", "bar", "--", "-c"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenStopAtNonOptionWithMultipleUnknown() {
        Options options = createOptions("-a");
        String[] args = {"-x", "-y", "value"};
        String[] expected = {"-x", "-y", "value"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenStopAtNonOptionFalseWithMultipleUnknown() {
        Options options = createOptions("-a");
        String[] args = {"-x", "-y", "value"};
        String[] expected = {"-x", "-y", "value"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenOptionWithArgumentStartingWithDash() {
        Options options = createOptions("-a");
        String[] args = {"-a", "-b"};
        String[] expected = {"-a", "-b"};
        String[] result = parser.flatten(options, args, false);
        assertArrayEquals(expected, result);
    }

    @Test
    public void testFlattenOptionWithArgumentStartingWithDashStopAtNonOption() {
        Options options = createOptions("-a");
        String[] args = {"-a", "-b", "value"};
        String[] expected = {"-a", "-b", "value"};
        String[] result = parser.flatten(options, args, true);
        assertArrayEquals(expected, result);
    }
}
