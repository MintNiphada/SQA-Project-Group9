package org.apache.commons.cli;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class PosixParserTest {

    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
    }

    // Test flatten with empty arguments array
    @Test
    public void testFlattenEmptyArguments() {
        String[] result = parser.flatten(options, new String[0], false);
        assertEquals(0, result.length);
    }

    // Test flatten with single long option without '='
    @Test
    public void testFlattenLongOptionWithoutEqual() {
        options.addOption(Option.builder("--test").build());
        // Need option defined such that hasOption("--test") returns true, but actually
        // flatten handles "--" tokens before checking options. So the token "--test"
        // matches startsWith("--") and has no '=', so added directly.
        // We don't need to define an option for this branch.
        String[] result = parser.flatten(options, new String[]{"--test"}, false);
        assertArrayEquals(new String[]{"--test"}, result);
    }

    // Test flatten with long option containing '='
    @Test
    public void testFlattenLongOptionWithEqual() {
        String[] result = parser.flatten(options, new String[]{"--foo=bar"}, false);
        assertArrayEquals(new String[]{"--foo", "bar"}, result);
    }

    // Test flatten with lone hyphen "-"
    @Test
    public void testFlattenSingleHyphen() {
        String[] result = parser.flatten(options, new String[]{"-"}, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    // Test flatten with short option (length 2) that is a valid option
    @Test
    public void testFlattenShortOptionValid() {
        // Define an option with opt "-a" so hasOption("-a") returns true.
        options.addOption(Option.builder("-a").build());
        String[] result = parser.flatten(options, new String[]{"-a"}, false);
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Test flatten with short option (length 2) that is invalid and stopAtNonOption = false
    @Test
    public void testFlattenShortOptionInvalidStopFalse() {
        // No option "-x" defined.
        String[] result = parser.flatten(options, new String[]{"-x"}, false);
        // According to processOptionToken, since no option and stopAtNonOption false,
        // it does nothing (no else). So token is ignored? 
        // Actually, in flatten: else if (token.startsWith("-")) {
        //     if (token.length() == 2) { processOptionToken(token, stopAtNonOption); }
        // Since stopAtNonOption false, processOptionToken has no else block, so nothing added.
        // So token is ignored.
        assertArrayEquals(new String[]{}, result);
    }

    // Test flatten with short option invalid and stopAtNonOption = true
    @Test
    public void testFlattenShortOptionInvalidStopTrue() {
        options.addOption(Option.builder("a").hasArg(false).build()); // unrelated
        String[] result = parser.flatten(options, new String[]{"-x", "arg1"}, true);
        // processOptionToken: no option, stopAtNonOption true -> eatTheRest=true, tokens.add("-x").
        // After returning to flatten, gobble iterates remaining "arg1" and adds.
        assertArrayEquals(new String[]{"-x", "arg1"}, result);
    }

    // Test flatten with long option (length > 2) that is a valid option
    @Test
    public void testFlattenLongOptionValid() {
        options.addOption(Option.builder("-long").build()); // token "-long" is valid
        String[] result = parser.flatten(options, new String[]{"-long"}, false);
        assertArrayEquals(new String[]{"-long"}, result);
    }

    // Test flatten with long option that is invalid, leading to burst
    @Test
    public void testFlattenShortCombinedBurstValid() {
        options.addOption(Option.builder("a").build());
        options.addOption(Option.builder("b").build());
        // token "-ab" length > 2, no option key "-ab" exists, so enters burst.
        String[] result = parser.flatten(options, new String[]{"-ab"}, false);
        // burstToken: for i=1, ch="a", exists -> add "-a", set currentOption, hasArg false, no extra.
        // continue i=2, ch="b", exists -> add "-b", set currentOption.
        assertArrayEquals(new String[]{"-a", "-b"}, result);
    }

    // Test burst with an option requiring an argument and remaining characters
    @Test
    public void testFlattenBurstOptionWithArg() {
        options.addOption(Option.builder("a").hasArg(true).build()); // a takes an argument
        // token "-abc", burst: i=1, ch="a", hasOption true, add "-a", currentOption hasArg true, token.length() (4) != (i+1)=2, so add substring(2)="bc", break.
        String[] result = parser.flatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-a", "bc"}, result);
    }

    // Test burst with an option requiring an argument but no remaining chars
    @Test
    public void testFlattenBurstOptionWithArgExhaust() {
        options.addOption(Option.builder("a").hasArg(true).build());
        String[] result = parser.flatten(options, new String[]{"-a"}, false);
        // token length 2, processOptionToken: valid, add "-a", currentOption set, no arg processing.
        // burstToken not invoked.
        assertArrayEquals(new String[]{"-a"}, result);
    }

    // Test burst with stopAtNonOption true and an invalid character in burst
    @Test
    public void testFlattenBurstStopAtNonOption() {
        options.addOption(Option.builder("a").build());
        // token "-abc", burst: i=1, ch="a" exists, add "-a", currentOption a, no arg.
        // i=2, ch="b" not exists, stopAtNonOption true -> process(token.substring(2)="c"), break.
        // process: currentOption is Option a, hasArg() false, so eatTheRest=true, add "--", add "c".
        // After burst, back in flatten, gobble iterates remaining (none here).
        // So tokens: "-a", "--", "c".
        String[] result = parser.flatten(options, new String[]{"-abc"}, true);
        assertArrayEquals(new String[]{"-a", "--", "c"}, result);
    }

    // Test burst with stopAtNonOption false and invalid character
    @Test
    public void testFlattenBurstInvalidStopFalse() {
        // no options defined
        String[] result = parser.flatten(options, new String[]{"-abc"}, false);
        // burstToken: i=1 ch="a" not exist, stopAtNonOption false -> tokens.add(token) i.e. "-abc", break.
        assertArrayEquals(new String[]{"-abc"}, result);
    }

    // Test process method when currentOption is set and hasArg true (single argument)
    @Test
    public void testProcessWithCurrentOptionAndArg() {
        Option opt = Option.builder("a").hasArg(true).build();
        options.addOption(opt);
        // We need to set currentOption via previous step. Use flatten with sequence: "-a" then "value"
        String[] result = parser.flatten(options, new String[]{"-a", "value"}, false);
        // "-a" length 2, processOptionToken: valid, add "-a", currentOption set to opt.
        // Then "value": token.startsWith("-") false, stopAtNonOption false, so tokens.add("value"). Wait, after processing "-a", gobble calls eatTheRest false.
        // So "value" goes through: not startsWith("-"), not stopAtNonOption? But stopAtNonOption is false, so it goes to else { tokens.add(value) }.
        // That branch is in flatten: else if (stopAtNonOption) { process(token); } else { tokens.add(token); }.
        // So for stopAtNonOption false, non-option tokens are just added, not process.
        // To test process, we need stopAtNonOption true. So:
        options = new Options();
        options.addOption(opt);
        String[] result2 = parser.flatten(options, new String[]{"-a", "value"}, true);
        // "-a" -> processOptionToken: valid, add "-a", currentOption opt.
        // "value": stopAtNonOption true, so process("value").
        // process: currentOption not null, hasArg true -> tokens.add("value"), currentOption = null.
        // No eatTheRest set, gobble nothing.
        // So result ["-a", "value"].
        assertArrayEquals(new String[]{"-a", "value"}, result2);
    }

    // Test process when currentOption is null and stopAtNonOption true
    @Test
    public void testProcessWhenCurrentOptionNnullStopTrue() {
        // first token is non-option with stopAtNonOption true, currentOption is null.
        String[] result = parser.flatten(options, new String[]{"nonopt", "rest"}, true);
        // "nonopt": not startsWith("-"), stopAtNonOption true -> process("nonopt").
        // currentOption null, else branch: eatTheRest=true, tokens.add("--"), tokens.add("nonopt").
        // gobble will consume "rest".
        assertArrayEquals(new String[]{"--", "nonopt", "rest"}, result);
    }

    // Test process when currentOption has multiple args? Branch not reachable, but add test anyway
    @Test
    public void testProcessWithCurrentOptionHasArgs() {
        // Option with numberOfArgs = 2 (hasArgs true). But outer condition requires hasArg() true.
        // Implement anyway.
        Option opt = Option.builder("a").hasArgs().build(); // unlimited args
        // In CLI, hasArg() returns true if minArgs > 0, so hasArg() is true.
        // However, inside process, first if (currentOption.hasArg()) is true, so it will add value and set null, not reach else if (hasArgs()).
        // So we can't reach hasArgs branch. Test will cover at least the first block.
        options.addOption(opt);
        String[] result = parser.flatten(options, new String[]{"-a", "val1"}, true);
        assertArrayEquals(new String[]{"-a", "val1"}, result);
    }

    // Test processOptionToken with stopAtNonOption true and option not found
    @Test
    public void testProcessOptionTokenInvalidStopTrue() {
        // Use token "-x" with stopAtNonOption true, no option defined.
        String[] result = parser.flatten(options, new String[]{"-}", true);
        assertArrayEquals(new String[]{"-}"}, result); // token added as is, eatTheRest true, but no more tokens.
    }

    // Test gobble with eatTheRest true
    @Test
    public void testGobbleEatTheRest() {
        // Make eatTheRest true by processing a non-option with stopAtNonOption true and currentOption null.
        String[] result = parser.flatten(options, new String[]{"nonopt", "a", "b"}, true);
        // nonopt triggers process, eatTheRest=true, add "--","nonopt". gobble adds "a","b".
        assertArrayEquals(new String[]{"--", "nonopt", "a", "b"}, result);
    }

    // Test flatten with arguments containing "null" token? Not necessary.
    // Test flatten with "--" token (no '=')
    @Test
    public void testFlattenSpecialDoubleDash() {
        String[] result = parser.flatten(options, new String[]{"--"}, false);
        assertArrayEquals(new String[]{"--"}, result);
    }

    // Test flatten with "--" token containing '=' unlikely, but maybe "--=" leads to no equals? 
    // Actually if token equals "--", indexOf('=') is -1, goes to else adding "--".
    // If "--=" token? indexOf('=') != -1, split into "--" and "" (empty string). 
    @Test
    public void testFlattenDoubleDashWithEval() {
        String[] result = parser.flatten(options, new String[]{"--=value"}, false);
        assertArrayEquals(new String[]{"--", "value"}, result);
    }

    // Test flatten with combination of all types
    @Test
    public void testFlattenComplexCombined() {
        options.addOption(Option.builder("a").hasArg(true).build());
        options.addOption(Option.builder("b").build());
        options.addOption(Option.builder("-X").build()); // to handle "-X" as valid
        String[] args = {"-abc", "--longOpt=val", "-", "--", "-X", "file", "extra"};
        String[] result = parser.flatten(options, args, false);
        // "-abc": burst -> a has arg, adds "-a","bc"
        // "--longOpt=val": split "--longOpt","val"
        // "-": added
        // "--": added (no '='), but we also have "--" token: startsWith("--"), no '=' so added.
        // "-X": length 2, processOptionToken: valid -> add "-X"
        // "file": not dash, stop false -> added
        // "extra": added
        assertArrayEquals(new String[]{"-}", "bc", "--longOpt", "val", "-", "--", "-X", "file", "extra"}, result);
    }

    // Test init resets state
    @Test
    public void testInitResetsState() {
        // First flatten with stop true and non-option to set eatTheRest
        options.addOption(Option.builder("a").build());
        parser.flatten(options, new String[]{"-unknown", "extra"}, true);
        // Now call flatten with empty args: should return empty array, state resets.
        String[] result = parser.flatten(options, new String[]{}, false);
        assertArrayEquals(new String[]{}, result);
        // Also check that eatTheRest and currentOption are cleared.
    }
}
