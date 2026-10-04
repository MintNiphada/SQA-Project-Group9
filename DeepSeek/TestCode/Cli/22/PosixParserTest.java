package org.apache.commons.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class PosixParserTest {

    // Helper subclass to expose protected method
    private static class TestablePosixParser extends PosixParser {
        @Override
        public String[] flatten(Options ops, String[] args, boolean stop) {
            return super.flatten(ops, args, stop);
        }
    }

    @Test
    public void testEmptyFlatten() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args = {};
        String[] result = parser.flatten(opts, args, false);
        assertArrayEquals("Empty input should return empty array", new String[0], result);
        result = parser.flatten(opts, args, true);
        assertArrayEquals("Empty input with stop true should return empty array", new String[0], result);
    }

    @Test
    public void testSingleDash() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args = {"-"};
        String[] expected = {"-"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
        assertArrayEquals(expected, parser.flatten(opts, args, true));
    }

    @Test
    public void testSimpleShortOptionRecognized() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption("a", false, "desc");
        String[] args = {"-a"};
        // token length 2, processOptionToken adds the token regardless of hasOption
        String[] expected = {"-a"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
        assertArrayEquals(expected, parser.flatten(opts, args, true));
    }

    @Test
    public void testSimpleShortOptionUnrecognizedWithStopAtNonOption() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options(); // no option 'a'
        String[] args = {"-a"};
        // length 2 -> processOptionToken -> eatTheRest true because stopAtNonOption true and !hasOption
        // then token added, no gobbling because no more tokens
        String[] expected = {"-a"};
        assertArrayEquals(expected, parser.flatten(opts, args, true));
    }

    @Test
    public void testSimpleShortOptionUnrecognizedWithoutStopAtNonOption() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args = {"-a"};
        // length 2 -> processOptionToken -> stopAtNonOption false -> eatTheRest not set, token added
        String[] expected = {"-a"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testLongOptionRecognized() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption("foo", false, "desc");
        String[] args = {"--foo"};
        String[] expected = {"--foo"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testLongOptionUnrecognized() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args = {"--bar"};
        // unrecognized -> processNonOptionToken, which adds "--" + value
        String[] expected = {"--", "--bar"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
        assertArrayEquals(expected, parser.flatten(opts, args, true));
    }

    @Test
    public void testLongOptionWithEqual() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption("foo", false, "desc");
        String[] args = {"--foo=bar"};
        String[] expected = {"--foo", "bar"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testLongOptionWithEqualUnrecognized() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args = {"--unknown=value"};
        // unrecognized -> processNonOptionToken -> "--" + full token
        String[] expected = {"--", "--unknown=value"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testBurstTokenSimple() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption("a", false, "desc");
        opts.addOption("b", false, "desc");
        String[] args = {"-ab"};
        // burstToken should add "-a" and "-b"
        String[] expected = {"-a", "-b"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testBurstTokenWithArg() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption(OptionBuilder.withLongOpt("a").hasArg().create('a'));
        String[] args = {"-a123"};
        // burstToken: i=1 'a' recognized, hasArg + remaining chars -> add "-a", "123"
        String[] expected = {"-a", "123"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testBurstTokenWithArgAndTrailingChars() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption(OptionBuilder.withLongOpt("a").hasArg().create('a'));
        opts.addOption("b", false, "desc"); // this won't be processed because break occurs after arg
        String[] args = {"-ab"};
        // i=1 'a' hasArg + length != i+1 (4!=2?) wait token length=3? "-ab" length 3. i=1, token.length()=3, i+1=2 -> not equal => add substring(2) => "b", break.
        // So tokens: "-a", "b"
        String[] expected = {"-a", "b"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testBurstTokenUnrecognizedCharAndStopFalse() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption("a", false, "desc"); // 'a' recognized, 'x' not
        String[] args = {"-ax"};
        // burstToken: i=1 'a' added. i=2 'x' not recognized, stopAtNonOption false -> else: tokens.add(token) i.e., "-ax" and break.
        String[] expected = {"-a", "-ax"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testBurstTokenUnrecognizedCharAndStopTrue() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption("a", false, "desc");
        String[] args = {"-ax", "remaining"};
        // burstToken: i=1 'a' added. i=2 'x' not recognized, stop true -> processNonOptionToken("x") (substring(2) = "x")
        // sets eatTheRest, adds "--", "x". Then break. gobble adds "remaining".
        String[] expected = {"-a", "--", "x", "remaining"};
        assertArrayEquals(expected, parser.flatten(opts, args, true));
    }

    @Test
    public void testBurstTokenFirstCharUnrecognizedStopTrue() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options(); // no options
        String[] args = {"-xyz", "leftover"};
        // token length 3 >2, options.hasOption("-xyz") false -> burstToken.
        // i=1 'x' not recognized, stop true -> processNonOptionToken("yz") (substring(1) = "yz")
        // sets eatTheRest, adds "--", "yz". gobble adds "leftover".
        String[] expected = {"--", "yz", "leftover"};
        assertArrayEquals(expected, parser.flatten(opts, args, true));
    }

    @Test
    public void testNonOptionTokenWithoutStop() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args = {"plain", "text"};
        // stopAtNonOption false, non-option tokens just added.
        String[] expected = {"plain", "text"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testNonOptionTokenWithStop() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args = {"plain", "text"};
        // stopAtNonOption true -> processNonOptionToken("plain") -> eatTheRest, add "--", "plain". gobble adds "text".
        String[] expected = {"--", "plain", "text"};
        assertArrayEquals(expected, parser.flatten(opts, args, true));
    }

    @Test
    public void testEatTheRestMultipleTokens() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption("a", false, "desc");
        String[] args = {"--", "-a", "other"};
        // token "--" starts with "--" but hasOption("--") false -> processNonOptionToken("--") -> eatTheRest, add "--", "--". gobble adds remaining.
        String[] expected = {"--", "--", "-a", "other"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testMixWithEatTheRest() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args = {"--unknown", "-a", "value"};
        // --unknown unrecognized -> processNonOptionToken -> eatTheRest, add "--", "--unknown". gobble adds "-a", "value".
        String[] expected = {"--", "--unknown", "-a", "value"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testLongOptionWithEqualsAndEatTheRest() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption("user", true, "desc");
        String[] args = {"--user=John", "--unknown", "extra"};
        // --user=John recognized -> add "--user", "John"
        // --unknown unrecognized -> processNonOptionToken -> eatTheRest, add "--", "--unknown". gobble adds "extra".
        String[] expected = {"--user", "John", "--", "--unknown", "extra"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testBurstTokenWithArgAndStopFalse() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption(OptionBuilder.withLongOpt("f").hasArg().create('f'));
        String[] args = {"-fvalue", "other"};
        // burstToken i=1 'f' hasArg, remaining chars "value", add "-f", "value". break. No eatTheRest.
        // next token "other" is non-option, stop false -> added.
        String[] expected = {"-f", "value", "other"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testBurnTokenWithArgAndStopTrue() {
        // similar but stopAtNonOption true, "other" will be handled correctly because eatTheRest not set.
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        opts.addOption(OptionBuilder.withLongOpt("f").hasArg().create('f'));
        String[] args = {"-fvalue", "other"};
        String[] expected = {"-f", "value", "other"}; // "other" added normally? But stopAtNonOption true, so "other" is non-option -> processNonOptionToken, which would add "--" etc. Not expected. Let's review logic:
        // first token "-fvalue" processed by burstToken, no eatTheRest set. Second token "other" not starting with '-', 
        // then else if (stopAtNonOption) { processNonOptionToken(token); } else { tokens.add(token); }
        // Since stop true, processNonOptionToken("other") will be called, eatTheRest set, add "--", "other".
        // However, since there are no more tokens after "other", the gobble won't affect anything.
        // So expected: ["-f", "value", "--", "other"].
        expected = new String[]{"-f", "value", "--", "other"};
        assertArrayEquals(expected, parser.flatten(opts, args, true));
    }

    @Test
    public void testTokenJustTwoDashes() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        // If we define an option named "--", it would be recognized. But not.
        String[] args = {"--"};
        // starts with "--", indexOf('=') -1, opt="--". hasOption("--") false -> processNonOptionToken -> eatTheRest, add "--", "--".
        String[] expected = {"--", "--"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testTokenSingleCharacterStartsWithDashButNotDashAlone() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args = {"-@"};
        // starts with '-', length 2 -> processOptionToken -> token added regardless
        String[] expected = {"-@"};
        assertArrayEquals(expected, parser.flatten(opts, args, false));
    }

    @Test
    public void testMultipleCallToFlattenResetsState() {
        TestablePosixParser parser = new TestablePosixParser();
        Options opts = new Options();
        String[] args1 = {"--unknown", "a"};
        // first call sets eatTheRest and populates tokens
        parser.flatten(opts, args1, true);
        // second call should be fresh
        String[] args2 = {"-v"};
        opts.addOption("v", false, "verbose");
        String[] result = parser.flatten(opts, args2, false);
        String[] expected = {"-v"};
        assertArrayEquals("Second call should not be affected by first call state", expected, result);
    }
}
