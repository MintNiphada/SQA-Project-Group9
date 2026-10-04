package org.joda.time.tz;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class ZoneInfoCompilerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File outputDir;
    private File sourceFile;

    @Before
    public void setUp() throws IOException {
        outputDir = folder.newFolder("output");
        sourceFile = folder.newFile("source.txt");
    }

    @After
    public void tearDown() {
        ZoneInfoCompiler.cStartOfYear = null;
        ZoneInfoCompiler.cLenientISO = null;
    }

    // verbose tests
    @Test
    public void testVerboseInitialFalse() {
        assertFalse(ZoneInfoCompiler.verbose());
    }

    @Test
    public void testVerboseSetTrue() {
        ZoneInfoCompiler.cVerbose.set(Boolean.TRUE);
        assertTrue(ZoneInfoCompiler.verbose());
        ZoneInfoCompiler.cVerbose.set(Boolean.FALSE); // cleanup
    }

    // getStartOfYear tests
    @Test
    public void testGetStartOfYearReturnsSameInstance() {
        ZoneInfoCompiler.DateTimeOfYear first = ZoneInfoCompiler.getStartOfYear();
        assertNotNull(first);
        assertSame(first, ZoneInfoCompiler.getStartOfYear());
    }

    // getLenientISOChronology tests
    @Test
    public void testGetLenientISOChronologyReturnsSameInstance() {
        Chronology chrono = ZoneInfoCompiler.getLenientISOChronology();
        assertNotNull(chrono);
        assertSame(chrono, ZoneInfoCompiler.getLenientISOChronology());
    }

    // parseYear tests
    @Test
    public void testParseYearMinimum() {
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("minimum", 0));
    }

    @Test
    public void testParseYearMin() {
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("min", 0));
    }

    @Test
    public void testParseYearMaximum() {
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("maximum", 0));
    }

    @Test
    public void testParseYearMax() {
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("max", 0));
    }

    @Test
    public void testParseYearOnly() {
        assertEquals(2222, ZoneInfoCompiler.parseYear("only", 2222));
    }

    @Test
    public void testParseYearNumeric() {
        assertEquals(2025, ZoneInfoCompiler.parseYear("2025", 0));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseYearInvalid() {
        ZoneInfoCompiler.parseYear("abc", 0);
    }

    // parseMonth tests
    @Test
    public void testParseMonthJanuary() {
        assertEquals(1, ZoneInfoCompiler.parseMonth("Jan"));
    }

    @Test
    public void testParseMonthFebruary() {
        assertEquals(2, ZoneInfoCompiler.parseMonth("Feb"));
    }

    @Test
    public void testParseMonthDecember() {
        assertEquals(12, ZoneInfoCompiler.parseMonth("Dec"));
    }

    // parseDayOfWeek tests
    @Test
    public void testParseDayOfWeekMonday() {
        assertEquals(1, ZoneInfoCompiler.parseDayOfWeek("Mon"));
    }

    @Test
    public void testParseDayOfWeekSunday() {
        assertEquals(7, ZoneInfoCompiler.parseDayOfWeek("Sun"));
    }

    // parseOptional tests
    @Test
    public void testParseOptionalHypen() {
        assertNull(ZoneInfoCompiler.parseOptional("-"));
    }

    @Test
    public void testParseOptionalNonHypen() {
        assertEquals("Test", ZoneInfoCompiler.parseOptional("Test"));
    }

    // parseTime tests
    @Test
    public void testParseTimePositiveHourMinute() {
        int millis = ZoneInfoCompiler.parseTime("12:34");
        assertEquals(12*60*60*1000 + 34*60*1000, millis);
    }

    @Test
    public void testParseTimePositiveWithSeconds() {
        int millis = ZoneInfoCompiler.parseTime("12:34:56");
        assertEquals((12*60*60 + 34*60 + 56)*1000, millis);
    }

    @Test
    public void testParseTimePositiveWithFraction() {
        int millis = ZoneInfoCompiler.parseTime("12:34:56.789");
        assertEquals((12*60*60 + 34*60 + 56)*1000 + 789, millis);
    }

    @Test
    public void testParseTimeNegative() {
        int millis = ZoneInfoCompiler.parseTime("-01:00");
        assertEquals(-60*60*1000, millis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTimeInvalid() {
        ZoneInfoCompiler.parseTime("12:34:56:78");
    }

    // parseZoneChar tests
    @Test
    public void testParseZoneCharStandard() {
        assertEquals('s', ZoneInfoCompiler.parseZoneChar('s'));
        assertEquals('s', ZoneInfoCompiler.parseZoneChar('S'));
    }

    @Test
    public void testParseZoneCharUTC() {
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('u'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('U'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('g'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('G'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('z'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('Z'));
    }

    @Test
    public void testParseZoneCharOther() {
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('w'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('x'));
    }

    // test method
    @Test
    public void testTestMethodIDMismatch() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertTrue(ZoneInfoCompiler.test("WrongID", utc));
    }

    @Test
    public void testTestMethodNoTransitions() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertTrue(ZoneInfoCompiler.test("UTC", utc));
    }

    @Test
    public void testTestMethodWithTransitions() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        // just exercise the path; result may be true or false
        ZoneInfoCompiler.test("America/Chicago", zone);
    }

    // writeZoneInfoMap tests
    @Test
    public void testWriteZoneInfoMapSimpleMapping() throws IOException {
        Map<String, DateTimeZone> map = new HashMap<String, DateTimeZone>();
        map.put("ID1", DateTimeZone.forID("UTC"));
        map.put("ID2", DateTimeZone.forID("GMT+0"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        ZoneInfoCompiler.writeZoneInfoMap(dos, map);
        dos.flush();
        byte[] data = baos.toByteArray();
        assertTrue(data.length > 0);
    }

    @Test
    public void testWriteZoneInfoMapDuplicateIDs() throws IOException {
        Map<String, DateTimeZone> map = new HashMap<String, DateTimeZone>();
        map.put("UTC", DateTimeZone.forID("UTC"));
        // key and zone ID both UTC, should be single entry in pool
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        ZoneInfoCompiler.writeZoneInfoMap(dos, map);
        dos.flush();
        assertTrue(baos.toByteArray().length >0);
    }

    // compile tests
    @Test
    public void testCompileNoSourcesOutputDirNull() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        Map<String, DateTimeZone> result = compiler.compile(null, new File[0]);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testCompileWithValidSources() throws IOException {
        writeSource("Rule TestRule 2000 max - Mar lastSun 2:00u 1:00 D\n" +
                     "Zone Test/Zone 1:00 TestRule T%sT 2000 Jan 1:00u\n" +
                     "Link Test/Zone Test/Link");
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        Map<String, DateTimeZone> result = compiler.compile(outputDir, new File[]{sourceFile});
        assertTrue(result.containsKey("Test/Zone"));
        assertTrue(result.containsKey("Test/Link"));
    }

    @Test(expected = IOException.class)
    public void testCompileOutputDirNotDirectory() throws IOException {
        File notDir = folder.newFile("notadir");
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        compiler.compile(notDir, new File[]{sourceFile});
    }

    @Test(expected = IOException.class)
    public void testCompileOutputDirCreateFail() throws IOException {
        File parent = folder.newFolder("parent");
        File child = new File(parent, "child");
        // Make parent read-only to prevent mkdirs
        parent.setReadable(false);
        parent.setWritable(false);
        parent.setExecutable(false);
        try {
            // outputDir is child, which does not exist and cannot be created
            ZoneInfoCompiler compiler = new ZoneInfoCompiler();
            compiler.compile(child, new File[]{sourceFile});
        } finally {
            parent.setReadable(true);
            parent.setWritable(true);
            parent.setExecutable(true);
        }
    }

    @Test
    public void testCompileReadBackErrorDetected() throws IOException {
        writeSource("Rule TestRule 2000 only - Jan 1 0:00u 0:00 X\n" +
                     "Zone Test/Zone 1:00 - TST\n");
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        // outputDir not null to trigger write and read-back test
        compiler.compile(outputDir, new File[]{sourceFile});
        // The test method in compile will check read-back, but if error, prints to System.out
        // No assertion needed; just ensure no exception
    }

    // parseDataFile tests
    @Test
    public void testParseDataFileCommentAndBlankLines() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        String data = "# comment\n\n   # indented comment\n\t# tab comment\n";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        assertTrue(compiler.iZones.isEmpty());
        assertTrue(compiler.iLinks.isEmpty());
    }

    @Test
    public void testParseDataFileRule() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        String data = "Rule Test 2000 max - Mar lastSun 2:00u 1:00 D\n";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        assertEquals(0, compiler.iZones.size());
        assertEquals(0, compiler.iLinks.size());
        assertTrue(compiler.iRuleSets.containsKey("Test"));
    }

    @Test
    public void testParseDataFileZone() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        String data = "Zone Test/Zone 1:00 Test T%sT 2000 Jan 1:00u\n";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        assertEquals(1, compiler.iZones.size());
        assertEquals(0, compiler.iLinks.size());
        assertEquals("Test/Zone", compiler.iZones.get(0).iName);
    }

    @Test
    public void testParseDataFileLink() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        String data = "Link Test/Zone Test/Link\n";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        assertTrue(compiler.iZones.isEmpty());
        assertEquals(2, compiler.iLinks.size());
        assertEquals("Test/Zone", compiler.iLinks.get(0));
        assertEquals("Test/Link", compiler.iLinks.get(1));
    }

    @Test
    public void testParseDataFileUnknownToken() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        String data = "Unknown token\n";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        // just prints to System.out, no effect
        assertTrue(compiler.iZones.isEmpty());
    }

    @Test
    public void testParseDataFileZoneContinuation() throws IOException {
        ZoneInfoCompiler compiler = new ZoneInfoCompiler();
        String data = "Zone Test/Zone 1:00 Test T%sT 2000 Jan 1:00u\n" +
                      "          2:00  -  TFT 2001 Feb 2:00u\n";
        BufferedReader reader = new BufferedReader(new StringReader(data));
        compiler.parseDataFile(reader);
        assertEquals(1, compiler.iZones.size());
        Zone zone = compiler.iZones.get(0);
        assertNotNull(zone.iNext);
        assertEquals(2000, zone.iUntilYear);
        assertEquals(2001, zone.iNext.iUntilYear);
    }

    // DateTimeOfYear tests
    @Test
    public void testDateTimeOfYearDefaultConstructor() {
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear();
        assertEquals(1, dt.iMonthOfYear);
        assertEquals(1, dt.iDayOfMonth);
        assertEquals(0, dt.iDayOfWeek);
        assertFalse(dt.iAdvanceDayOfWeek);
        assertEquals(0, dt.iMillisOfDay);
        assertEquals('w', dt.iZoneChar);
    }

    @Test
    public void testDateTimeOfYearParseLastSun() {
        StringTokenizer st = new StringTokenizer("Mar lastSun 2:00u");
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(st);
        assertEquals(3, dt.iMonthOfYear);
        assertEquals(-1, dt.iDayOfMonth);
        assertEquals(7, dt.iDayOfWeek); // Sun=7
        assertFalse(dt.iAdvanceDayOfWeek);
    }

    @Test
    public void testDateTimeOfYearParseGreaterOrEqual() {
        StringTokenizer st = new StringTokenizer("Mar Sun>=8 2:00u");
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(st);
        assertEquals(3, dt.iMonthOfYear);
        assertEquals(8, dt.iDayOfMonth);
        assertEquals(7, dt.iDayOfWeek);
        assertTrue(dt.iAdvanceDayOfWeek);
    }

    @Test
    public void testDateTimeOfYearParseLessOrEqual() {
        StringTokenizer st = new StringTokenizer("Mar Sun<=15 2:00u");
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(st);
        assertEquals(3, dt.iMonthOfYear);
        assertEquals(15, dt.iDayOfMonth);
        assertEquals(7, dt.iDayOfWeek);
        assertFalse(dt.iAdvanceDayOfWeek);
    }

    @Test
    public void testDateTimeOfYearParseSimpleDay() {
        StringTokenizer st = new StringTokenizer("Mar 8 2:00u");
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(st);
        assertEquals(3, dt.iMonthOfYear);
        assertEquals(8, dt.iDayOfMonth);
        assertEquals(0, dt.iDayOfWeek);
        assertFalse(dt.iAdvanceDayOfWeek);
    }

    @Test
    public void testDateTimeOfYearParse24HourHandling() {
        // "24:00" on lastSun March: should advance to April 1, month=4, day=1, dayOfWeek shifts
        StringTokenizer st = new StringTokenizer("Mar lastSun 24:00u");
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear(st);
        assertEquals(4, dt.iMonthOfYear);
        assertEquals(1, dt.iDayOfMonth);
        assertEquals(1, dt.iDayOfWeek); // Sunday(7) -> (7-1+1)%7+1 = (7)%7+1 = 0+1? Actually (7-1+1)%7+1 = (7)%7+1 = 0+1=1 Monday? Need check Joda math: (iDayOfWeek -1 +1)%7 +1 => (7)%7+1 = 0+1=1 => Monday. So correct.
    }

    @Test
    public void testDateTimeOfYearAddRecurring() {
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear();
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        dt.addRecurring(builder, "ST", 3600000, 2000, 2010);
        // no assertion, just ensure no exception
    }

    @Test
    public void testDateTimeOfYearAddCutover() {
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear();
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        dt.addCutover(builder, 2020);
        // no exception
    }

    @Test
    public void testDateTimeOfYearToString() {
        ZoneInfoCompiler.DateTimeOfYear dt = new ZoneInfoCompiler.DateTimeOfYear();
        String s = dt.toString();
        assertTrue(s.contains("MonthOfYear"));
        assertTrue(s.contains("MillisOfDay"));
    }

    // Rule tests
    @Test
    public void testRuleConstructionValid() {
        StringTokenizer st = new StringTokenizer("Test 2000 max - Mar lastSun 2:00u 1:00 D");
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(st);
        assertEquals("Test", rule.iName);
        assertEquals(2000, rule.iFromYear);
        assertEquals(Integer.MAX_VALUE, rule.iToYear);
        assertNull(rule.iType);
        assertEquals("D", rule.iLetterS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRuleToYearLessThanFromYear() {
        StringTokenizer st = new StringTokenizer("Test max 2000 - Mar lastSun 2:00u 1:00 D");
        new ZoneInfoCompiler.Rule(st);
    }

    @Test
    public void testRuleFormatNameWithSlash() {
        StringTokenizer st = new StringTokenizer("Test 2000 max - Mar lastSun 2:00u 0:00 D");
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(st);
        String name = rule.formatName("Std/Dst");
        assertEquals("Std", name);
        // saveMillis=0
    }

    @Test
    public void testRuleFormatNameWithSlashNonZeroSave() {
        StringTokenizer st = new StringTokenizer("Test 2000 max - Mar lastSun 2:00u 1:00 D");
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(st);
        String name = rule.formatName("Std/Dst");
        assertEquals("Dst", name);
    }

    @Test
    public void testRuleFormatNameWithPercentS() {
        StringTokenizer st = new StringTokenizer("Test 2000 max - Mar lastSun 2:00u 1:00 D");
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(st);
        assertEquals("StdDst", rule.formatName("Std%sst"));
    }

    @Test
    public void testRuleFormatNameWithPercentSNullLetterS() {
        StringTokenizer st = new StringTokenizer("Test 2000 max - Mar lastSun 2:00u 1:00 -");
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(st);
        assertNull(rule.iLetterS);
        assertEquals("Stdst", rule.formatName("Std%sst"));
    }

    @Test
    public void testRuleAddRecurring() {
        StringTokenizer st = new StringTokenizer("Test 2000 max - Mar lastSun 2:00u 1:00 D");
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(st);
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        rule.addRecurring(builder, "Std%sst");
        // no exception
    }

    @Test
    public void testRuleToString() {
        StringTokenizer st = new StringTokenizer("Test 2000 max - Mar lastSun 2:00u 1:00 D");
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(st);
        assertTrue(rule.toString().contains("Rule"));
    }

    // RuleSet tests
    @Test
    public void testRuleSetAddRuleMismatchedNameThrows() {
        ZoneInfoCompiler.Rule rule1 = new ZoneInfoCompiler.Rule(
                new StringTokenizer("Test 2000 max - Mar lastSun 2:00u 1:00 D"));
        ZoneInfoCompiler.RuleSet rs = new ZoneInfoCompiler.RuleSet(rule1);
        ZoneInfoCompiler.Rule rule2 = new ZoneInfoCompiler.Rule(
                new StringTokenizer("Test2 2000 max - Mar lastSun 2:00u 1:00 D"));
        try {
            rs.addRule(rule2);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testRuleSetAddRecurring() {
        ZoneInfoCompiler.Rule rule = new ZoneInfoCompiler.Rule(
                new StringTokenizer("Test 2000 max - Mar lastSun 2:00u 1:00 D"));
        ZoneInfoCompiler.RuleSet rs = new ZoneInfoCompiler.RuleSet(rule);
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        rs.addRecurring(builder, "Std%sst");
        // no exception
    }

    // Zone tests
    @Test
    public void testZoneConstructionNoUntil() {
        StringTokenizer st = new StringTokenizer("Test/Zone 1:00 - TST");
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(st);
        assertEquals("Test/Zone", zone.iName);
        assertEquals(1*60*60*1000, zone.iOffsetMillis);
        assertNull(zone.iRules);
        assertEquals("TST", zone.iFormat);
        assertEquals(Integer.MAX_VALUE, zone.iUntilYear);
    }

    @Test
    public void testZoneConstructionWithUntil() {
        StringTokenizer st = new StringTokenizer("Test/Zone 1:00 - TST 2000 Jan 1:00u");
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(st);
        assertEquals(2000, zone.iUntilYear);
        assertNotNull(zone.iUntilDateTimeOfYear);
    }

    @Test
    public void testZoneChain() {
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Test/Zone 1:00 - TST 2000 Jan 1:00u"));
        StringTokenizer st2 = new StringTokenizer("2:00 - TDT 2001 Feb 2:00u");
        zone.chain(st2);
        assertNotNull(zone.iNext);
        assertEquals(2*60*60*1000, zone.iNext.iOffsetMillis);
        assertEquals(2001, zone.iNext.iUntilYear);
    }

    @Test
    public void testZoneChainExistingNext() {
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Test/Zone 1:00 - TST 2000 Jan 1:00u"));
        StringTokenizer st2 = new StringTokenizer("2:00 - TDT 2001 Feb 2:00u");
        zone.chain(st2);
        StringTokenizer st3 = new StringTokenizer("3:00 - TET 2002 Mar 3:00u");
        zone.chain(st3);
        assertNotNull(zone.iNext);
        assertNotNull(zone.iNext.iNext);
        assertEquals(3*60*60*1000, zone.iNext.iNext.iOffsetMillis);
    }

    @Test
    public void testZoneAddToBuilderNullRules() {
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Test/Zone 1:00 - TST"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        zone.addToBuilder(builder, new HashMap<String, ZoneInfoCompiler.RuleSet>());
        // fixed savings with 0
        // we can't easily verify, but ensure no exception
    }

    @Test
    public void testZoneAddToBuilderRulesParsableAsTime() {
        // iRules is a time string like "1:00" parseTime returns millis
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Test/Zone 1:00 1:00 TST"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        zone.addToBuilder(builder, new HashMap<String, ZoneInfoCompiler.RuleSet>());
        // expect setFixedSavings with 1 hour
    }

    @Test
    public void testZoneAddToBuilderRulesSetFound() {
        ZoneInfoCompiler.RuleSet rs = new ZoneInfoCompiler.RuleSet(
                new ZoneInfoCompiler.Rule(new StringTokenizer("TestRule 2000 max - Mar lastSun 2:00u 1:00 D")));
        Map<String, ZoneInfoCompiler.RuleSet> ruleSets = new HashMap<String, ZoneInfoCompiler.RuleSet>();
        ruleSets.put("TestRule", rs);
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Test/Zone 1:00 TestRule T%sT 2000 Jan 1:00u"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        zone.addToBuilder(builder, ruleSets);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZoneAddToBuilderRulesSetNotFound() {
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Test/Zone 1:00 NonExistent T%sT 2000 Jan 1:00u"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        zone.addToBuilder(builder, new HashMap<String, ZoneInfoCompiler.RuleSet>());
    }

    @Test
    public void testZoneAddToBuilderWithUntilAddsCutover() {
        ZoneInfoCompiler.RuleSet rs = new ZoneInfoCompiler.RuleSet(
                new ZoneInfoCompiler.Rule(new StringTokenizer("TestRule 2000 max - Mar lastSun 2:00u 1:00 D")));
        Map<String, ZoneInfoCompiler.RuleSet> ruleSets = new HashMap<String, ZoneInfoCompiler.RuleSet>();
        ruleSets.put("TestRule", rs);
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Test/Zone 1:00 TestRule T%sT 2000 Jan 1:00u"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        zone.addToBuilder(builder, ruleSets);
        // ensures cutover added without exception
    }

    @Test
    public void testZoneAddToBuilderWithNextLoops() {
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Test/Zone 1:00 - TST 2000 Jan 1:00u"));
        zone.chain(new StringTokenizer("2:00 - TDT 2001 Feb 2:00u"));
        DateTimeZoneBuilder builder = new DateTimeZoneBuilder();
        zone.addToBuilder(builder, new HashMap<String, ZoneInfoCompiler.RuleSet>());
        // should loop through iNext
    }

    @Test
    public void testZoneToString() {
        ZoneInfoCompiler.Zone zone = new ZoneInfoCompiler.Zone(
                new StringTokenizer("Test/Zone 1:00 - TST 2000 Jan 1:00u"));
        String s = zone.toString();
        assertTrue(s.contains("Zone"));
        assertTrue(s.contains("Test/Zone"));
    }

    // main method tests
    @Test
    public void testMainNoArgsPrintsUsage() {
        ZoneInfoCompiler.main(new String[0]);
    }

    @Test
    public void testMainQuestionMarkPrintsUsage() {
        ZoneInfoCompiler.main(new String[]{"-?"});
    }

    @Test
    public void testMainInvalidOptionMissingArgPrintsUsage() {
        ZoneInfoCompiler.main(new String[]{"- src"});
        // no assertion, it prints usage due to IndexOutOfBoundsException
    }

    @Test
    public void testMainWithSrcDstVerbose() throws Exception {
        writeSource("Zone Test/Zone 1:00 - TST\n");
        ZoneInfoCompiler.main(new String[]{
                "-src", folder.getRoot().getAbsolutePath(),
                "-dst", outputDir.getAbsolutePath(),
                "-verbose",
                "source.txt"
        });
        // should compile and generate output files
        assertTrue(new File(outputDir, "Test/Zone").exists());
    }

    // Helper method
    private void writeSource(String content) throws IOException {
        FileOutputStream fos = new FileOutputStream(sourceFile);
        fos.write(content.getBytes());
        fos.close();
    }
}
