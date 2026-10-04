package org.apache.commons.cli;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PosixParserTest {
    private PosixParser parser;
    private Options options;

    @Before
    public void setUp() {
        parser = new PosixParser();
        options = new Options();
    }

    @After
    public void tearDown() {
        parser = null;
    }

    // Helper to add an option with a short opt and hasArg flag
    private void addOption(String opt, boolean hasArg) {
        options.addOption(new Option(opt, null, hasArg, ""));
    }

    @Test
    public void testFlattenEmptyArguments() {
        String[] result = parser.flatten(options, new String[0], false);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testFlattenSingleOptionExisting() {
        addOption("h", false);
        String[] result = parser.flatten(options, new String[]{"- h"}, false);
        assertEquals(1, result.length);
        assertEquals("-h", result[0]];
    }

    @Test
    public void testFlattenSingleOptionNotExistingNoStop() {
        String[] result = parser.flatten(options, new String[]{"- x"}, false);
        // When stopAtNonOption is false, unknown short option is ignored
        assertEquals(0, result.length);
    }

    @Test
    public void testFlattenSingleOptionNotExistingStopAtNonOption() {
        String[] result = parser.flatten(options, new String[]{"- x"}, true);
        // stopAtNonOption -> eatTheRest set, but no more tokens, so nothing added
        assertEquals(0, result.length);
    }

    @Test
    public void testFlattenLongOptionNoEq() {
        String[] result = parser.flatten(options, new String[]{"-- verbose"}, false);
        assertEquals(1, result.length);
        assertEquals("--verbose", result[0]];
    }

    @Test
    public void testFlattenLongOptionWithEq() {
        String[] result = parser.flatten(options, new String[]{"-- verbose= true"}, false);
        assertEquals(2, result.length);
        assertEquals("--verbose", result[0]);
        assertEquals("true", result[1]);
    }

    @Test
    public void testFlattenSingleHyphen() {
        String[] result = parser.flatten(options, new String[]{"- "}, false);
        assertEquals(1, result.length);
        assertEquals("-", result[0]);
    }

    @Test
    public void testFlattenOptionWithArgument() {
        addOption("f", true);
        String[] result = parser.flatten(options, new String[]{"- f", "file.txt"}, false);
        assertEquals(2, result.length);
        assertEquals("-f", result[0]);
        assertEquals("file.txt", result[1]);
    }

    @Test
    public void testFlattenOptionWithArgumentButNextIsOption() {
        addOption("f", true);
        addOption("v", false);
        String[] result = parser.flatten(options, new String[]{"- f", "-v"}, false);
        // Argument for -f is lost because next token is an option
        assertEquals(2, result.length);
        assertEquals("-f", result[0]);
        assertEquals("-v", result[1]);
    }

    @Test
    public void testProcessWithCurrentOptionHasArgAndStopAtNonOption() {
        addOption("f", true);
        String[] result = parser.flatten(options, new String[]{"- f", "val"}, true);
        // process method adds value and nullifies currentOption
        assertEquals(2, result.length);
        assertEquals("-f", result[0]);
        assertEquals("val", result[1]);
    }

    @Test
    public void testStopAtNonOptionFirstNonOption() {
        String[] result = parser.flatten(options, new String[]{"val", "other"}, true);
        // first non-option triggers eatTheRest, adds -- val, then gobble adds rest
        assertEquals(3, result.length);
        assertEquals("--", result[0]);
        assertEquals("val", result[1]);
        assertEquals("other", result[2]);
    }

    @Test
    public void testBurstTokenAllOptionsExist() {
        addOption("a", false);
        addOption("b", false);
        String[] result = parser.flatten(options, new String[]{"- ab"}, false);
        // a and b are recognized, token is bursted correctly (no remaining unknown chars)
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
    }

    @Test
    public void testBurstTokenHasArgWithRemaining() {
        addOption("a", true);
        String[] result = parser.flatten(options, new String[]{"- a12"}, false);
        // -a has arg, remaining chars are added as separate token
        assertEquals(2, result.length);
        assertEquals("-a", result[0]);
        assertEquals("12", result[1]);
    }

    @Test
    public void testBurstTokenHasArgButNoRemaining() {
        addOption("a", true);
        String[] result = parser.flatten(options, new String[]{"- a"}, true);
        // no remaining chars, only -a added
        assertEquals(1, result.length);
        assertEquals("-a", result[0]);
    }

    @Test
    public void testBurstTokenNonOptionCharNoStop() {
        addOption("a", false);
        addOption("b", false);
        // c is not an option, stopAtNonOption false -> add original token and break
        String[] result = parser.flatten(options, new String[]{"- abc"}, false);
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("-b", result[1]);
        assertEquals("-abc", result[2]);
    }

    @Test
    public void testBurstTokenNonOptionCharWithStop() {
        addOption("a", false);
        String[] result = parser.flatten(options, new String[]{"- abc"}, true);
        // a known, b unknown -> process("bc") called, then gobble adds rest (none)
        assertEquals(3, result.length);
        assertEquals("-a", result[0]);
        assertEquals("--", result[1]);
        assertEquals("bc", result[2]);
    }

    @Test
    public void testGobbleAfterProcessEatTheRest() {
        // stopAtNonOption true, first token unknown option sets eatTheRest, gobble consumes the rest
        String[] result = parser.flatten(options, new String[]{"- x", "file1", "file2"}, true);
        // -x not added, eatTheRest=true, gobble adds file1 and file2
        assertEquals(2, result.length);
        assertEquals("file1", result[0]);
        assertEquals("file2", result[1]);
    }

    @Test
    public void testLongOptionWithEqAndSplitting() {
        String[] result = parser.flatten(options, new String[]{"-- key= value"}, false);
        assertEquals(2, result.length);
        assertEquals("--key", result[0]);
        assertEquals("value", result[1]);
    }

    @Test
    public void testMultipleArgsWithStopAtNonOptionAfterOptionWithArg() {
        addOption("f", true);
        String[] result = parser.flatten(options, new String[]{"- f", "file", "-v", "extra"}, true);
        // -f added (currentOption set), then "file" is processed via stopAtNonOption path (process)
        // which adds file and nullifies currentOption. Then -v is a short option, recognized,
        // and "extra" is processed similarly.
        assertEquals(4, result.length);
        assertEquals("-f", result[0]);
        assertEquals("file", result[1]);
        assertEquals("-v", result[2]);
        assertEquals("extra", result[3]);
    }
}
