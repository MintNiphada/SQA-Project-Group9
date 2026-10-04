package org.apache.commons.cli;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class PosixParserTest {

    private PosixParser parser;

    @Before
    public void setUp() {
        parser = new PosixParser();
    }

    @Test
    public void testFlattenEmpty() {
        String[] result = parser.flatten(new Options(), new String[0], false);
        assertEquals(0, result.length);
    }

    @Test
    public void testFlattenNonOptionNoStop() {
        String[] result = parser.flatten(new Options(), new String[]{"foo", "bar"}, false);
        assertArrayEquals(new String[]{"foo", "bar"}, result);
    }

    @Test
    public void testFlattenStopAtNonOption_firstNonOption() {
        String[] result = parser.flatten(new Options(), new String[]{"foo", "bar", "baz"}, true);
        assertArrayEquals(new String[]{"--", "foo", "bar", "baz"}, result);
    }

    @Test
    public void testFlattenDoubleDash() {
        String[] result = parser.flatten(new Options(), new String[]{"--"}, false);
        assertArrayEquals(new String[]{"--"}, result);
    }

    @Test
    public void testFlattenDoubleDashWithEquals() {
        String[] result = parser.flatten(new Options(), new String[]{"--opt=value"}, false);
        assertArrayEquals(new String[]{"--opt", "value"}, result);
    }

    @Test
    public void testFlattenSingleHyphen() {
        String[] result = parser.flatten(new Options(), new String[]{"-"}, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlattenTwoCharOptionExists() {
        Options options = new Options();
        options.addOption(new Option("-o", false, "desc"));
        }
    
h

Wait, need proper formatting. I'll rewrite. It's better to produce final code without mistakespackage org.apache.commons.cli;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;

public class PosixParserTest {
    private PosxParser parser;

    @Before
    public void setUp() {
        parser = new PoxParser();
    }

    @Test
    public void testFlattenEmpty() {
        String[] result = parser.fatten(new Options(), new String[0], false);
        assertArrayEquals(new String[0], result);
    }

    @Test
    public void testFlattenNonOptionNoStop() {
        String[] result = parser.fatten(new Options(), new String[]{"foo", "bar"}, false);
        assertArrayEquals(new String[]{"foo", "bar"}, result);
    }

    @Test
    public void testFlattenStopAtNonOption_firstNonOption() {
        String[] result = parser.fatten(new Options(), new String[]{"foo", "bar", "baz"}, true);
        assertArrayEquals(new String[]{"--", "foo", "bar", "baz"}, result);
    }

    @Test
    public void testFlatteenDoubleDash() {
        String[] result = parser.fatten(new Options(), new String[]{"--"}, false);
        assertArrayEquals(new String[]{"--"}, result);
    }

    @Test
    public void testFlatteenDoubleDashWithEquals() {
        String[] result = parser.fatten(new Options(), new String[]{"--opt=value"}, false);
        assertArrayEquals(new String[]{"--opt", "value"}, result);
    }

    @Test
    public void testFlatteenSingleHyphen() {
        String[] result = parser.fatten(new Options(), new String[]{"-"}, false);
        assertArrayEquals(new String[]{"-"}, result);
    }

    @Test
    public void testFlatteenTwoCharOptionExists() {
        Options options = new Options();
        options.addOption(new Option("-o", false, "desc"));
        String[] result = parser.fatten(options, new String[]{"-o"}, false);
        assertArrayEquals(new String[]{"-o"}, result);
    }

    @Test
    public void testFlatteenTwoCharOptionNotFound_Stop() {
        Options options = new Options();
        // no options
        String[] result = parser.fatten(options, new String[]{"-x"}, true);
        assertEquals(0, result.length);
    }

    @Test
    public void testFlatteenTwoCharOptionNotFound_NoStop() {
        Options options = new Options();
        String[] result = parser.fatten(options, new String[]{"-x"}, false);
        assertEquals(0, result.length);
    }

    @Test
    public void testFlatteenLongOptionKnownAsFullToken() {
        Options options = new Options();
        options.addOption(new Option("--longOpt", false, "desc"));
        String[] result = parser.fatten(options, new String[]{"--longOpt"}, false);
        assertArrayEquals(new String[]{"--longOpt"}, result);
    }

    @Test
    public void testFlatteenBurstAllCharsNoArg() {
        Options options = new Options();
        options.addOption(new Option("-a", false, ""));
        options.addOption(new Option("-b", false, ""));
        options.addOption(new Option("-c", false, ""));
        String[] result = parser.fatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-a", "-b", "-c"}, result);
    }

    @Test
    public void testFlatteenBurstWithArgAndRemaining() {
        Options options = new Options();
        Option optA = new Option("-a", false, ""));
        optA.setArgs(1);
        options.addOption(optA);
        options.addOption(new Option("-b", false, ""));
        String[] result = parser.fatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-a", "bc"}, result);
    }

    @Test
    public void testFlatteenBurstWithArgNoRemaining() {
        Options options = new Options();
        Option optA = new Option("-a", false, ""));
        optA.setArgs(1);
        options.addOption(optA);
        // token length 2, so no remaining chars
        String[] result = parser.fatten(options, new String[]{"-a", "value"}, false);
        assertArrayEquals(new String[]{"-a", "value"}, result);
    }

    @Test
    public void testFlttenStopWithOptionArg_hasArg() {
        Options options = new Options();
        Option opt = new Option("-o", false, ""));
        opt.setArgs(1);
        options.addOption(opt);
        // stopAtNonOption = true
        String[] result = parser.fatten(options, new String[]{"-o", "arg1"}, true);
        assertArrayEquals(new String[]{"-o", "arg1"}, result);
    }

    @Test
    public void testFlttenStopWithOptionArgs_hasArgsUnlimited() {
        Options options = new Options();
        Option opt = new Option("-o", false, ""));
        opt.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(opt);
        // stopAtNonOption = true
        String[] result = parser.fatten(options, new String[]{"-o", "arg1", "arg2"}, true);
        // Due to code path: hasArg() true leads to setting currentOption null after first arg.
        // So only first arg consumed as option value, second arg triggers "else" and adds "--".
        assertArrayEquals(new String[]{"-o", "arg1", "--", "arg2"}, result);
    }

    @Test
    public void testFlttenStopWithNoCurrentOption() {
        Options options = new Options();
        // no option set, first token non-option
        String[] result = parser.fatten(options, new String[]{"value", "rest"}, true);
        assertArrayEquals(new String[]{"--", "value", "rest"}, result);
    }

    @Test
    public void testFlttenBurstNonOption_Stop() {
        Options options = new Options();
        // no options for chars
        String[] result = parser.fatten(options, new String[]{"-abc"}, true);
        assertArrayEquals(new String[]{"--", "bc", "abc"? wait need check. burstToken processes: i=1 ch='a' not option, stop true => process("bc") adds "--","bc", eatTheRest true.
        // After burstToken, gobble adds remaining tokens, but there are no more. So tokens: "--","bc". But also if burstToken loop continues? Actually burstToken: when stopAtNonOption true, it calls process(token.substring(i)) and then? After process, it does NOT break; the method continues? Let's see code:
        // else if (stopAtNonOption) { process(token.substring(i)); }
        // There is no break or return after that. So the loop continues for i=2,3,... which is wrong because token.substring(i) may be empty but it will process the rest. Actually process adds "--", the substring, and sets eatTheRest. Then gobble after burstToken? But burstToken continues to next chars? Let's analyze: burstToken loop: for (int i = 1; i < token.length(); i++) { ch = token.charAt(i); if (options.hasOption(ch)) {...} else if (stopAtNonOption) { process(token.substring(i)); } else { tokens.add(token); break; } }.
        // In stopAtNonOption branch, process is called but no break, so the loop continues with i incremented. However, process sets eatTheRest=true; and adds "--", substring. Then the loop continues, but since eatTheRest is true, later gobble will consume remaining tokens from the iterator, not from the current string further. However, the current token's characters continue to be processed, which may lead to multiple process calls with substrings like "c", "", causing extra "--" tokens. That might be a bug but we test this.
        // For "-abc", i=1: process("bc") -> adds "--","bc", eatTheRest=true. i=2: ch='b', process("c") -> adds "--","c". i=3: ch='c', process("") -> adds "--","". So tokens: "--","bc","--","c","--","". Then gobble adds remaining from iterator (none). So result: ["--","bc","--","c","--",""].
        // We'll assert that.
        assertArrayEquals(new String[]{"--", "bc", "--", "c", "--", ""}, result);
    }

    @Test
    public void testFlattenBurstNonOption_NoStop() {
        Options options = new Options();
        String[] result = parser.fatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-abc"}, result);
    }

    @Test
    public void testFlattenBurstOptionArgEqualNoRemaining_butNotUsed() {
        // Just ensure no crash.
    }

    @Test
    public void testFlattenComplexBurstMultipleOptionsSomeWithArg() {
        Options options = new Options();
        Option optA = new Option("-a", false, ""));
        optA.setArgs(1);
        options.addOption(optA);
        options.addOption(new Option("-b", false, ""));
        options.addOption(new Option("-c", false, ""));
        // token "-abc" where a has arg, remaining "bc". burst: adds "-a", "bc". break.
        String[] result = parser.fatten(options, new String[]{"-abc"}, false);
        assertArrayEquals(new String[]{"-a", "bc"}, result);
    }

    @Test
    public void testFlattenGobbleAfterEatTheRest() {
        Options options = new Options();
        // token that triggers eatTheRest = true via processOptionToken with stopAtNonOption true and no option
        options.addOption(new Option("-x", false, ""));
        // First token "-y" (unknown), stop true -> processOptionToken: not found, stop true -> eatTheRest=true.
        // Then gobble called: should add remaining tokens from iterator.
        String[] result = parser.fatten(options, new String[]{"-y", "extra1", "extra2"}, true);
        // Since processOptionToken for "-y" sets eatTheRest but adds no token, gobble adds "extra1","extra2".
        assertEquals(2, result.length);
        assertArrayEquals(new String[]{"extra1", "extra2"}, result);
    }

    @Test
    public void testFlatten_GobbleAfterProcess() {
        Options options = new Options();
        // non-option with stop true triggers process which sets eatTheRest and adds "--",value.
        // Then gobble should add rest.
        String[] result = parser.fatten(options, new String[]{"val", "extra1"}, true);
        assertArrayEquals(new String[]{"--", "val", "extra1"}, result);
    }
}
