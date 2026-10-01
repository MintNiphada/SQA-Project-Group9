package org.apache.commons.cli;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class PosixParserTest {

    private Options buildOptions() {
        return new Options();
    }

    @Test
    public void testFlatten_EmptyArguments() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[0], false);
        assertArrayEquals(new String[0], result);
    }

    @Test
    public void testFlatten_SingleHyphen() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"-"), false);
        assertArrayEquals(new String[]{"-"), result);
    }

    @Test
    public void testFlatten_DoubleHyphen() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"--"), false);
        assertArrayEquals(new String[]{"--"), result);
    }

    @Test
    public void testFlatten_LongOptionWithoutEquals() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"--foo"), false);
        assertArrayEquals(new String[]{"--foo"), result);
    }

    @Test
    public void testFlatten_LongOptionWithEquals() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"--foo=bar"), false);
        assertArrayEquals(new String[]{"--foo", "bar"), result);
    }

    @Test
    public void testFlatten_LongOptionWithMultipleEquals() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"--foo=bar=baz"), false);
        assertArrayEquals(new String[]{"--foo", "bar=baz"), result);
    }

    @Test
    public void testFlatten_ShortOption_Recognized_NoStop() {
        Options options = new Options();
        options.addOption("f", false, "file");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f"), false);
        assertArrayEquals(new String[]{"-f"), result);
    }

    @Test
    public void testFlatten_ShortOption_Recognized_Stop() {
        Options options = new Options();
        options.addOption("f", false, "file");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f"), true);
        assertArrayEquals(new String[]{"-f"), result);
    }

    @Test
    public void testFlatten_ShortOption_Unrecognized_NoStop() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-x"), false);
        assertArrayEquals(new String[]{"-x"), result);
    }

    @Test
    public void testFlatten_ShortOption_Unrecognized_Stop() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-x", "something"), true);
        // eatTheRest becomes true, gobble adds remaining
        assertArrayEquals(new String[]{"-x", "something"), result);
    }

    @Test
    public void testFlatten_ShortOption_Unrecognized_Stop_MoreTokens() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-x", "a", "b"), true);
        assertArrayEquals(new String[]{"-x", "a", "b"), result);
    }

    @Test
    public void testFlatten_NonOption_NoStop() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"foo"), false);
        assertArrayEquals(new String[]{"foo"), result);
    }

    @Test
    public void testFlatten_NonOption_Stop() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"foo"), true);
        // process sets eatTheRest, adds "--" and "foo"
        assertArrayEquals(new String[]{"--", "foo"), result);
    }

    @Test
    public void testFlatten_NonOption_Stop_WithExtra() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"foo", "bar"), true);
        assertArrayEquals(new String[]{"--", "foo", "bar"), result);
    }

    @Test
    public void testFlatten_OptionWithArg_Stop_Captured() {
        Options options = new Options();
        options.addOption("f", true, "file");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "value"), true);
        // first token "-f" processed, currentOption set; next token "value" triggers process, argument captured
        assertArrayEquals(new String[]{"-f", "value"), result);
    }

    @Test
    public void testFlatten_OptionWithArg_NoStop_NotCaptured() {
        Options options = new Options();
        options.addOption("f", true, "file");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "value"), false);
        // stopAtNonOption false, so "value" just added as token
        assertArrayEquals(new String[]{"-f", "value"), result);
    }

    @Test
    public void testFlatten_OptionWithArg_Stop_CurrentOptionNull_NonOption() {
        // no current option, non-option triggers process and eatTheRest
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"foo"), true);
        assertArrayEquals(new String[]{"--", "foo"), result);
    }

    @Test
    public void testFlatten_OptionWithArg_Stop_CurrentOptionNoArg() {
        Options options = new Options();
        options.addOption("f", false, "flag");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "value"), true);
        // first token sets currentOption (f with no arg); second triggers process but hasArg false -> else branch
        assertArrayEquals(new String[]{"-f", "--", "value"), result);
    }

    @Test
    public void testFlatten_MultiCharOption_AggedAsLongOption() {
        // option registered with opt "-foo" (including dash) to simulate direct match
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("foo").build()? Actually using Option API: new Option("foo", false, "") makes opt="foo". But hasOption expects token "-foo", which won't match. Instead, create Option with opt "-foo":
        options.addOption(new Option("-foo", false, "long"));
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-foo"), false);
        assertArrayEquals(new String[]{"-foo"), result);
    }

    @Test
    public void testFlatten_BurstToken_Normal() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        options.addOption("b", false, "beta");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-ab"), false);
        assertArrayEquals(new String[]{"-a", "-b"), result);
    }

    @Test
    public void testFlatten_BurstToken_Arg_Middle() {
        Options options = new Options();
        options.addOption("a", true, "alpha");
        options.addOption("b", false, "beta");
        PosixParser parser = new PosixParser();
        // token "-ab", length 3. 'a' has arg, rest exists, argument "b" added, stop
        String[] result = parser.flatten(options, new String[]{"-ab"), false);
        assertArrayEquals(new String[]{"-a", "b"), result);
    }

    @Test
    public void testFlatten_BurstToken_Arg_Middle_ExtraArg() {
        Options options = new Options();
        options.addOption("a", true, "alpha");
        options.addOption("b", false, "beta");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-abc"), false);
        // 'a' has arg, rest "bc" -> argument "bc"
        assertArrayEquals(new String[]{"-a", "bc"), result);
    }

    @Test
    public void testFlatten_BurstToken_Unrecognized_NoStop() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-ab"), false);
        // first char 'a' not found, stopAtNonOption false -> add whole token
        assertArrayEquals(new String[]{"-ab"), result);
    }

    @Test
    public void testFlatten_BurstToken_Unrecognized_Stop() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-ab"), true);
        // 'a' not found, stopAtNonOption true -> process("ab")
        assertArrayEquals(new String[]{"--", "ab"), result);
    }

    @Test
    public void testFlatten_BurstToken_Unrecognized_Stop_WithExtra() {
        Options options = new Options();
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-ab", "extra"), true);
        // burstToken processes "ab" -> adds "--", "ab"; eatTheRest = true; gobble adds "extra"
        assertArrayEquals(new String[]{"--", "ab", "extra"), result);
    }

    @Test
    public void testFlatten_BurstToken_OptionThenNonOption_Stop() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        PosixParser parser = new PosixParser();
        // token "-a*", a has no arg, i=1 adds "-a", i=2 '*' not found, stopAtNonOption true -> process("*")
        // currentOption is a (no arg) -> process else -> add "--", "*"
        String[] result = parser.flatten(options, new String[]{"-a*"), true);
        assertArrayEquals(new String[]{"-a", "--", "*"), result);
    }

    @Test
    public void testFlatten_BurstToken_OptionThenNonOption_Stop_WithExtra() {
        Options options = new Options();
        options.addOption("a", false, "alpha");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-a*", "more"), true);
        // process adds "--", "*", eatTheRest=true, gobble adds "more"
        assertArrayEquals(new String[]{"-a", "--", "*", "more"), result);
    }

    @Test
    public void testFlatten_MultipleInvocations_ResetState() {
        Options options = new Options();
        options.addOption("f", false, "file");
        PosixParser parser = new PosixParser();
        // first call
        String[] result1 = parser.flatten(options, new String[]{"-f"), true);
        assertArrayEquals(new String[]{"-f"), result1);
        // second call should be independent
        String[] result2 = parser.flatten(options, new String[]{"--bar"), false);
        assertArrayEquals(new String[]{"--bar"), result2);
    }

    @Test
    public void testFlatten_StopAtNonOption_FirstTokenNonOption() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"nonopt", "-f"), true);
        // nonopt triggers process, eatTheRest, gobble adds "-f"
        assertArrayEquals(new String[]{"--", "nonopt", "-f"), result);
    }

    @Test
    public void testFlatten_StopAtNonOption_FirstTokenNonOption_NoMore() {
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(buildOptions(), new String[]{"nonopt"), true);
        assertArrayEquals(new String[]{"--", "nonopt"), result);
    }

    @Test
    public void testFlatten_OptionWithNoArgThenNonOption_Stop() {
        Options options = new Options();
        options.addOption("f", false, "flag");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "nonopt"), true);
        // "-f" recognized, currentOption set, next "nonopt" triggers process with hasArg false -> else
        assertArrayEquals(new String[]{"-f", "--", "nonopt"), result);
    }

    @Test
    public void testFlatten_OptionWithArgThenNonOption_Stop() {
        Options options = new Options();
        options.addOption("f", true, "file");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-f", "value", "nonopt"), true);
        // "-f" sets currentOption, "value" captured, currentOption null, next "nonopt" triggers process with currentOption null -> else
        assertArrayEquals(new String[]{"-f", "value", "--", "nonopt"), result);
    }

    @Test
    public void testFlatten_BurstToken_ArgAtEnd_ByMultiChar() {
        // to cover arg deduction when token length equals i+2? For example, token "-ab" where a has arg, rest length 1, i=1, token.length()=3, i+1=2, so condition true, argument "b" added.
        Options options = new Options();
        options.addOption("a", true, "alpha");
        PosixParser parser = new PosixParser();
        String[] result = parser.flatten(options, new String[]{"-ab"), false);
        assertArrayEquals(new String[]{"-a", "b"), result);
    }
}
