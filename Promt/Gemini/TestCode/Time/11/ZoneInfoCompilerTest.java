package org.joda.time.tz;

import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ZoneInfoCompilerTest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent;

    @Before
    public void setUp() {
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @After
    public void tearDown() {
        System.setOut(originalOut);
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    @Test
    public void testVerbose() {
        ZoneInfoCompiler.cVerbose.set(Boolean.TRUE);
        assertTrue(ZoneInfoCompiler.verbose());
        ZoneInfoCompiler.cVerbose.set(Boolean.FALSE);
        assertFalse(ZoneInfoCompiler.verbose());
    }

    @Test
    public void testParseYear() {
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("min", 2000));
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("minimum", 2000));
        assertEquals(Integer.MIN_VALUE, ZoneInfoCompiler.parseYear("MINIMUM", 2000));

        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("max", 2000));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("maximum", 2000));
        assertEquals(Integer.MAX_VALUE, ZoneInfoCompiler.parseYear("MAX", 2000));

        assertEquals(1995, ZoneInfoCompiler.parseYear("only", 1995));
        assertEquals(2020, ZoneInfoCompiler.parseYear("ONLY", 2020));

        assertEquals(2012, ZoneInfoCompiler.parseYear("2012", 2000));
        assertEquals(-500, ZoneInfoCompiler.parseYear("-500", 2000));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseYearInvalid() {
        ZoneInfoCompiler.parseYear("invalid", 2000);
    }

    @Test
    public void testParseMonth() {
        assertEquals(1, ZoneInfoCompiler.parseMonth("Jan"));
        assertEquals(1, ZoneInfoCompiler.parseMonth("January"));
        assertEquals(2, ZoneInfoCompiler.parseMonth("Feb"));
        assertEquals(6, ZoneInfoCompiler.parseMonth("Jun"));
        assertEquals(12, ZoneInfoCompiler.parseMonth("Dec"));
        assertEquals(12, ZoneInfoCompiler.parseMonth("December"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMonthInvalid() {
        ZoneInfoCompiler.parseMonth("NotAMonth");
    }

    @Test
    public void testParseDayOfWeek() {
        assertEquals(DateTimeConstants.MONDAY, ZoneInfoCompiler.parseDayOfWeek("Mon"));
        assertEquals(DateTimeConstants.MONDAY, ZoneInfoCompiler.parseDayOfWeek("Monday"));
        assertEquals(DateTimeConstants.WEDNESDAY, ZoneInfoCompiler.parseDayOfWeek("Wed"));
        assertEquals(DateTimeConstants.SUNDAY, ZoneInfoCompiler.parseDayOfWeek("Sun"));
        assertEquals(DateTimeConstants.SUNDAY, ZoneInfoCompiler.parseDayOfWeek("Sunday"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDayOfWeekInvalid() {
        ZoneInfoCompiler.parseDayOfWeek("NotADay");
    }

    @Test
    public void testParseOptional() {
        assertNull(ZoneInfoCompiler.parseOptional("-"));
        assertEquals("EST", ZoneInfoCompiler.parseOptional("EST"));
        assertEquals("S", ZoneInfoCompiler.parseOptional("S"));
    }

    @Test
    public void testParseTime() {
        assertEquals(0, ZoneInfoCompiler.parseTime("0"));
        assertEquals(0, ZoneInfoCompiler.parseTime("00:00"));
        assertEquals(0, ZoneInfoCompiler.parseTime("00:00:00"));

        assertEquals(3600000, ZoneInfoCompiler.parseTime("1"));
        assertEquals(3600000, ZoneInfoCompiler.parseTime("01:00"));
        assertEquals(3661000, ZoneInfoCompiler.parseTime("01:01:01"));
        assertEquals(3661500, ZoneInfoCompiler.parseTime("01:01:01.5"));

        assertEquals(-3600000, ZoneInfoCompiler.parseTime("-1"));
        assertEquals(-3600000, ZoneInfoCompiler.parseTime("-01:00"));
        assertEquals(-3661000, ZoneInfoCompiler.parseTime("-01:01:01"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseTimeInvalid() {
        ZoneInfoCompiler.parseTime("abc");
    }

    @Test
    public void testParseZoneChar() {
        assertEquals('s', ZoneInfoCompiler.parseZoneChar('s'));
        assertEquals('s', ZoneInfoCompiler.parseZoneChar('S'));

        assertEquals('u', ZoneInfoCompiler.parseZoneChar('u'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('U'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('g'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('G'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('z'));
        assertEquals('u', ZoneInfoCompiler.parseZoneChar('Z'));

        assertEquals('w', ZoneInfoCompiler.parseZoneChar('w'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('W'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('a'));
        assertEquals('w', ZoneInfoCompiler.parseZoneChar('?'));
    }

    @Test
    public void testGetStartOfYearAndLenientChronology() {
        assertNotNull(ZoneInfoCompiler.getStartOfYear());
        assertNotNull(ZoneInfoCompiler.getLenientISOChronology());
    }

    @Test
    public void testTestReturnsTrueForDifferentId() {
        DateTimeZone zone = DateTimeZone.UTC;
        assertTrue(ZoneInfoCompiler.test("NonMatchingID", zone));
    }

    @Test
    public void testTestWithRealDateTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertTrue(ZoneInfoCompiler.test("America/New_York", zone));

        DateTimeZone utc = DateTimeZone.UTC;
        assertTrue(ZoneInfoCompiler.test("UTC", utc));
    }

    @Test
    public void testWriteZoneInfoMap() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        Map<String, DateTimeZone> zimap = new TreeMap<String, DateTimeZone>();
        zimap.put("UTC", DateTimeZone.UTC);
        zimap.put("GMT", DateTimeZone.UTC);
        zimap.put("America/New_York", DateTimeZone.forID("America/New_York"));

        ZoneInfoCompiler.writeZoneInfoMap(dos, zimap);
        byte[] bytes = baos.toByteArray();
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testMainNoArgsPrintsUsage() throws Exception {
        ZoneInfoCompiler.main(new String[0]);
        String output = outContent.toString();
        assertTrue(output.contains("Usage: java org.joda.time.tz.ZoneInfoCompiler <options> <source files>"));
    }

    @Test
    public void testMainHelpArgPrintsUsage() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-?"});
        String output = outContent.toString();
        assertTrue(output.contains("Usage: java org.joda.time.tz.ZoneInfoCompiler <options> <source files>"));
    }

    @Test
    public void testMainMissingArgValuePrintsUsage() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-src"});
        String output = outContent.toString();
        assertTrue(output.contains("Usage: java org.joda.time.tz.ZoneInfoCompiler <options> <source files>"));

        outContent.reset();
        ZoneInfoCompiler.main(new String[]{"-dst"});
        output = outContent.toString();
        assertTrue(output.contains("Usage: java org.joda.time.tz.ZoneInfoCompiler <options> <source files>"));
    }

    @Test
    public void testMainNoSourceFilesPrintsUsage() throws Exception {
        ZoneInfoCompiler.main(new String[]{"-verbose"});
        String output = outContent.toString();
        assertTrue(output.contains("Usage: java org.joda.time.tz.ZoneInfoCompiler <options> <source files>"));
    }

    @Test
    public void testCompileCompleteFlow() throws Exception {
        File tempDir = File.createTempFile("zic_test_src", "");
        tempDir.delete();
        tempDir.mkdirs();

        File outDir = File.createTempFile("zic_test_dst", "");
        outDir.delete();
        outDir.mkdirs();

        try {
            File sourceFile = new File(tempDir, "sample.tz");
            String tzData =
                    "# Sample rule data\n" +
                    "Rule\tUS\t1918\t1919\t-\tMar\tlastSun\t2:00\t1:00\tD\n" +
                    "Rule\tUS\t1918\t1919\t-\tOct\tlastSun\t2:00\t0\tS\n" +
                    "Rule\tUS\t1942\tonly\t-\tFeb\t9\t2:00\t1:00\tW\n" +
                    "Rule\tUS\t1945\tonly\t-\tAug\t14\t19:00u\t1:00\tP\n" +
                    "Rule\tUS\t1945\tonly\t-\tSep\t30\t2:00\t0\tS\n" +
                    "Rule\tUS\t1967\t2006\t-\tOct\tlastSun\t2:00\t0\tS\n" +
                    "Rule\tUS\t1967\t1973\t-\tApr\tlastSun\t2:00\t1:00\tD\n" +
                    "Rule\tUS\t1974\tonly\t-\tJan\t6\t2:00\t1:00\tD\n" +
                    "Rule\tUS\t1975\tonly\t-\tFeb\t23\t2:00\t1:00\tD\n" +
                    "Rule\tUS\t1976\t1986\t-\tApr\tlastSun\t2:00\t1:00\tD\n" +
                    "Rule\tUS\t1987\t2006\t-\tApr\tSun>=1\t2:00\t1:00\tD\n" +
                    "Rule\tUS\t2007\tmax\t-\tMar\tSun>=8\t2:00\t1:00\tD\n" +
                    "Rule\tUS\t2007\tmax\t-\tNov\tSun<=7\t2:00\t0\tS\n" +
                    "\n" +
                    "Zone\tAmerica/New_York\t-4:56:02\t-\tLMT\t1883 Nov 18 12:03:58\n" +
                    "\t-5:00\tUS\tE%sT\t1920\n" +
                    "\t-5:00\tNYC\tE%sT\t1942\n" +
                    "\t-5:00\tUS\tE%sT\n" +
                    "Zone\tSimple/Fixed\t1:00\t-\tSET\n" +
                    "Zone\tSimple/Savings\t1:00\t1:00\tSET\n" +
                    "Link\tAmerica/New_York\tEST5EDT\n" +
                    "Link\tNonExistent\tBrokenLink\n";

            FileOutputStream fos = new FileOutputStream(sourceFile);
            fos.write(tzData.getBytes("UTF-8"));
            fos.close();

            ZoneInfoCompiler.main(new String[]{
                    "-src", tempDir.getAbsolutePath(),
                    "-dst", outDir.getAbsolutePath(),
                    "-verbose",
                    "sample.tz"
            });

            File nyFile = new File(outDir, "America/New_York");
            assertTrue(nyFile.exists());

            File mapFile = new File(outDir, "ZoneInfoMap");
            assertTrue(mapFile.exists());

        } finally {
            deleteRecursively(tempDir);
            deleteRecursively(outDir);
        }
    }

    @Test
    public void testCompileDestinationCreationFailures() throws Exception {
        File notADir = File.createTempFile("zic_file_not_dir", "");
        try {
            ZoneInfoCompiler zic = new ZoneInfoCompiler();
            try {
                zic.compile(notADir, null);
                fail("Should fail when outputDir is not a directory");
            } catch (IOException expected) {
                assertTrue(expected.getMessage().contains("Destination is not a directory"));
            }
        } finally {
            notADir.delete();
        }
    }

    @Test
    public void testParseDataFileWithCommentsAndUnknownLines() throws Exception {
        String data =
                "# Just a comment\n" +
                "   # Indented comment\n" +
                "\n" +
                "UnknownCommand foo bar\n" +
                "Zone Test/Zone 0 - UTC\n";

        ZoneInfoCompiler zic = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        zic.parseDataFile(reader);

        Map<String, DateTimeZone> map = zic.compile(null, null);
        assertTrue(map.containsKey("Test/Zone"));
    }

    @Test
    public void testDateTimeOfYearVariousFormats() throws Exception {
        Class<?> dtoyClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$DateTimeOfYear");
        Constructor<?> ctor = dtoyClass.getDeclaredConstructor(StringTokenizer.class);
        ctor.setAccessible(true);

        Object dtoy1 = ctor.newInstance(new StringTokenizer("Jan 15 10:00s"));
        assertNotNull(dtoy1.toString());

        Object dtoy2 = ctor.newInstance(new StringTokenizer("Feb lastMon 12:00u"));
        assertNotNull(dtoy2.toString());

        Object dtoy3 = ctor.newInstance(new StringTokenizer("Mar Sun>=10 24:00"));
        assertNotNull(dtoy3.toString());

        Object dtoy4 = ctor.newInstance(new StringTokenizer("Apr Sun<=7 01:30w"));
        assertNotNull(dtoy4.toString());

        Object dtoy5 = ctor.newInstance(new StringTokenizer("May lastSun 24:00"));
        assertNotNull(dtoy5.toString());

        Object dtoyEmpty = dtoyClass.getDeclaredConstructor().newInstance();
        assertNotNull(dtoyEmpty.toString());
    }

    @Test
    public void testDateTimeOfYearInvalidDayPattern() throws Exception {
        Class<?> dtoyClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$DateTimeOfYear");
        Constructor<?> ctor = dtoyClass.getDeclaredConstructor(StringTokenizer.class);
        ctor.setAccessible(true);

        try {
            ctor.newInstance(new StringTokenizer("Jan InvalidPattern 10:00"));
            fail("Expected IllegalArgumentException for invalid pattern");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testRuleCreationAndFormatting() throws Exception {
        Class<?> ruleClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$Rule");
        Constructor<?> ctor = ruleClass.getDeclaredConstructor(StringTokenizer.class);
        ctor.setAccessible(true);

        Object ruleSlash = ctor.newInstance(new StringTokenizer("TestRule 2000 2005 - Mar 10 2:00 0:00 S"));
        assertNotNull(ruleSlash.toString());

        Method formatName = ruleClass.getDeclaredMethod("formatName", String.class);
        formatName.setAccessible(true);

        String name1 = (String) formatName.invoke(ruleSlash, "EST/EDT");
        assertEquals("EST", name1);

        Object ruleSlashDst = ctor.newInstance(new StringTokenizer("TestRule 2000 2005 - Mar 10 2:00 1:00 D"));
        String name2 = (String) formatName.invoke(ruleSlashDst, "EST/EDT");
        assertEquals("EDT", name2);

        String name3 = (String) formatName.invoke(ruleSlash, "E%sT");
        assertEquals("EST", name3);

        Object ruleNoLetter = ctor.newInstance(new StringTokenizer("TestRule 2000 2005 - Mar 10 2:00 0:00 -"));
        String name4 = (String) formatName.invoke(ruleNoLetter, "E%sT");
        assertEquals("ET", name4);

        String name5 = (String) formatName.invoke(ruleSlash, "STATIC");
        assertEquals("STATIC", name5);
    }

    @Test
    public void testRuleInvalidToYear() throws Exception {
        Class<?> ruleClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$Rule");
        Constructor<?> ctor = ruleClass.getDeclaredConstructor(StringTokenizer.class);
        ctor.setAccessible(true);

        try {
            ctor.newInstance(new StringTokenizer("TestRule 2005 2000 - Mar 10 2:00 0 S"));
            fail("Expected IllegalArgumentException when toYear < fromYear");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testRuleSetAddRuleMismatch() throws Exception {
        Class<?> ruleClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$Rule");
        Constructor<?> ruleCtor = ruleClass.getDeclaredConstructor(StringTokenizer.class);
        ruleCtor.setAccessible(true);

        Object rule1 = ruleCtor.newInstance(new StringTokenizer("Name1 2000 2005 - Mar 10 2:00 0 S"));
        Object rule2 = ruleCtor.newInstance(new StringTokenizer("Name2 2000 2005 - Mar 10 2:00 0 S"));

        Class<?> ruleSetClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$RuleSet");
        Constructor<?> ruleSetCtor = ruleSetClass.getDeclaredConstructor(ruleClass);
        ruleSetCtor.setAccessible(true);

        Object ruleSet = ruleSetCtor.newInstance(rule1);
        Method addRuleMethod = ruleSetClass.getDeclaredMethod("addRule", ruleClass);
        addRuleMethod.setAccessible(true);

        try {
            addRuleMethod.invoke(ruleSet, rule2);
            fail("Expected exception when rule name does not match");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testZoneToStringAndChaining() throws Exception {
        Class<?> zoneClass = Class.forName("org.joda.time.tz.ZoneInfoCompiler$Zone");
        Constructor<?> ctor = zoneClass.getDeclaredConstructor(StringTokenizer.class);
        ctor.setAccessible(true);

        Object zone = ctor.newInstance(new StringTokenizer("Test/Zone -5:00 - EST 1970"));
        assertNotNull(zone.toString());

        Method chainMethod = zoneClass.getDeclaredMethod("chain", StringTokenizer.class);
        chainMethod.setAccessible(true);
        chainMethod.invoke(zone, new StringTokenizer("-4:00 - EDT 1980"));
        chainMethod.invoke(zone, new StringTokenizer("-5:00 - EST"));

        String chainedStr = zone.toString();
        assertTrue(chainedStr.contains("Test/Zone"));
        assertTrue(chainedStr.contains("EDT"));
    }

    @Test
    public void testZoneMissingRuleSetThrowsException() throws Exception {
        String data =
                "Zone Broken/Zone -5:00 NonExistentRuleSet EST\n";

        ZoneInfoCompiler zic = new ZoneInfoCompiler();
        BufferedReader reader = new BufferedReader(new StringReader(data));
        zic.parseDataFile(reader);

        try {
            zic.compile(null, null);
            fail("Expected IllegalArgumentException for missing RuleSet");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Rules not found: NonExistentRuleSet"));
        }
    }
}
