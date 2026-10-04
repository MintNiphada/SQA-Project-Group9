package org.joda.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.tz.FixedDateTimeZone;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DateTimeZoneTest {

    private static TimeZone originalTimeZone;
    private static String originalUserTimezone;
    private static Provider originalProvider;
    private static NameProvider originalNameProvider;
    private static DateTimeZone originalDefault;

    @BeforeClass
    public static void saveState() {
        originalTimeZone = TimeZone.getDefault();
        originalUserTimezone = System.getProperty("user.timezone");
        originalProvider = DateTimeZone.getProvider();
        originalNameProvider = DateTimeZone.getNameProvider();
        originalDefault = DateTimeZone.getDefault();
    }

    @After
    public void restoreState() throws Exception {
        // Restore system properties
        if (originalUserTimezone != null) {
            System.setProperty("user.timezone", originalUserTimezone);
        } else {
            System.clearProperty("user.timezone");
        }
        TimeZone.setDefault(originalTimeZone);
        // Restore providers
        setProvider0(originalProvider);
        setNameProvider0(originalNameProvider);
        // Restore default time zone
        setDefaultField(originalDefault);
    }

    private void setDefaultField(DateTimeZone zone) throws Exception {
        Field field = DateTimeZone.class.getDeclaredField("cDefault");
        field.setAccessible(true);
        field.set(null, zone);
    }

    private void setProvider0(Provider provider) throws Exception {
        Field field = DateTimeZone.class.getDeclaredField("cProvider");
        field.setAccessible(true);
        field.set(null, provider);
        // Also update cAvailableIDs
        if (provider != null) {
            Field idsField = DateTimeZone.class.getDeclaredField("cAvailableIDs");
            idsField.setAccessible(true);
            idsField.set(null, provider.getAvailableIDs());
        }
    }

    private void setNameProvider0(NameProvider nameProvider) throws Exception {
        Field field = DateTimeZone.class.getDeclaredField("cNameProvider");
        field.setAccessible(true);
        field.set(null, nameProvider);
    }

    private void clearFixedOffsetCache() throws Exception {
        Field cacheField = DateTimeZone.class.getDeclaredField("iFixedOffsetCache");
        cacheField.setAccessible(true);
        cacheField.set(null, null);
    }

    // ==================== getDefault ====================
    @Test
    public void testGetDefault_initial() throws Exception {
        // Force cDefault to null to test initial computation
        setDefaultField(null);
        // Clear system property
        System.clearProperty("user.timezone");
        // Set TimeZone default to a known zone
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));
        DateTimeZone zone = DateTimeZone.getDefault();
        assertNotNull(zone);
        // Should be Europe/London
        assertEquals("Europe/London", zone.getID());
    }

    @Test
    public void testGetDefault_cached() throws Exception {
        DateTimeZone first = DateTimeZone.getDefault();
        DateTimeZone second = DateTimeZone.getDefault();
        assertSame(first, second);
    }

    @Test
    public void testGetDefault_systemPropertyValid() throws Exception {
        setDefaultField(null);
        System.setProperty("user.timezone", "UTC");
        DateTimeZone zone = DateTimeZone.getDefault();
        assertEquals(DateTimeZone.UTC, zone);
    }

    @Test
    public void testGetDefault_systemPropertyInvalid() throws Exception {
        setDefaultField(null);
        System.setProperty("user.timezone", "Invalid/Zone");
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
        DateTimeZone zone = DateTimeZone.getDefault();
        // Should fall back to TimeZone default
        assertEquals("Europe/Paris", zone.getID());
    }

    @Test
    public void testGetDefault_bothFail() throws Exception {
        setDefaultField(null);
        System.setProperty("user.timezone", "Invalid/Zone");
        // Set TimeZone default to something that forTimeZone will fail on? 
        // Actually forTimeZone will try to convert, but if it fails, it will throw IllegalArgumentException.
        // We need to make forTimeZone throw. We can set TimeZone default to a custom TimeZone with invalid ID?
        // Simpler: we can set TimeZone default to a zone with ID that is not recognized and not GMT+/-.
        // But forTimeZone will throw IllegalArgumentException, which is caught, then temp remains null, then UTC is used.
        TimeZone.setDefault(TimeZone.getTimeZone("SomeUnknownID"));
        DateTimeZone zone = DateTimeZone.getDefault();
        assertEquals(DateTimeZone.UTC, zone);
    }

    // ==================== setDefault ====================
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_null() {
        DateTimeZone.setDefault(null);
    }

    @Test
    public void testSetDefault_valid() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTimeZone.setDefault(zone);
        assertSame(zone, DateTimeZone.getDefault());
    }

    // ==================== forID ====================
    @Test
    public void testForID_null() {
        // Should return default
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        assertSame(defaultZone, DateTimeZone.forID(null));
    }

    @Test
    public void testForID_UTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test
    public void testForID_validLongId() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone);
        assertEquals("Europe/London", zone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidId() {
        DateTimeZone.forID("Invalid/Zone");
    }

    @Test
    public void testForID_positiveOffset() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        assertNotNull(zone);
        assertEquals(19800000, zone.getOffset(0)); // 5h30m in millis
    }

    @Test
    public void testForID_negativeOffset() {
        DateTimeZone zone = DateTimeZone.forID("-05:30");
        assertNotNull(zone);
        assertEquals(-19800000, zone.getOffset(0));
    }

    @Test
    public void testForID_zeroOffset() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));
    }

    @Test
    public void testForID_offsetWithSeconds() {
        DateTimeZone zone = DateTimeZone.forID("+05:30:15");
        assertNotNull(zone);
        assertEquals(5 * 3600000 + 30 * 60000 + 15 * 1000, zone.getOffset(0));
    }

    @Test
    public void testForID_offsetWithMillis() {
        DateTimeZone zone = DateTimeZone.forID("+05:30:15.500");
        assertNotNull(zone);
        assertEquals(5 * 3600000 + 30 * 60000 + 15 * 1000 + 500, zone.getOffset(0));
    }

    @Test
    public void testForID_fixedOffsetCaching() throws Exception {
        clearFixedOffsetCache();
        DateTimeZone zone1 = DateTimeZone.forID("+01:00");
        DateTimeZone zone2 = DateTimeZone.forID("+01:00");
        assertSame(zone1, zone2);
    }

    // ==================== forOffsetHours ====================
    @Test
    public void testForOffsetHours_zero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
    }

    @Test
    public void testForOffsetHours_positive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals(5 * 3600000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHours_negative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-5);
        assertEquals(-5 * 3600000, zone.getOffset(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_tooLarge() {
        DateTimeZone.forOffsetHours(24);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_tooSmall() {
        DateTimeZone.forOffsetHours(-24);
    }

    // ==================== forOffsetHoursMinutes ====================
    @Test
    public void testForOffsetHoursMinutes_zero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
    }

    @Test
    public void testForOffsetHoursMinutes_positiveHoursPositiveMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(2, 15);
        assertEquals(2 * 3600000 + 15 * 60000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursMinutes_positiveHoursZeroMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(2, 0);
        assertEquals(2 * 3600000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursMinutes_negativeHoursPositiveMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 15);
        assertEquals(-(2 * 3600000 + 15 * 60000), zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursMinutes_negativeHoursZeroMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 0);
        assertEquals(-2 * 3600000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursMinutes_negativeHoursNegativeMinutes() {
        // Negative minutes are not allowed, should throw
        try {
            DateTimeZone.forOffsetHoursMinutes(-2, -15);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testForOffsetHoursMinutes_positiveHoursNegativeMinutes() {
        // Negative minutes not allowed
        try {
            DateTimeZone.forOffsetHoursMinutes(2, -15);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testForOffsetHoursMinutes_hoursOutOfRange() {
        try {
            DateTimeZone.forOffsetHoursMinutes(24, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            DateTimeZone.forOffsetHoursMinutes(-24, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testForOffsetHoursMinutes_minutesOutOfRange() {
        try {
            DateTimeZone.forOffsetHoursMinutes(0, 60);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            DateTimeZone.forOffsetHoursMinutes(0, -1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testForOffsetHoursMinutes_maxValues() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(23, 59);
        assertEquals(23 * 3600000 + 59 * 60000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursMinutes_minValues() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-23, 59);
        assertEquals(-(23 * 3600000 + 59 * 60000), zone.getOffset(0));
    }

    // ==================== forOffsetMillis ====================
    @Test
    public void testForOffsetMillis_zero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
    }

    @Test
    public void testForOffsetMillis_positive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals(3600000, zone.getOffset(0));
        assertEquals("+01:00", zone.getID());
    }

    @Test
    public void testForOffsetMillis_negative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-3600000);
        assertEquals(-3600000, zone.getOffset(0));
        assertEquals("-01:00", zone.getID());
    }

    @Test
    public void testForOffsetMillis_withSeconds() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(1000);
        assertEquals(1000, zone.getOffset(0));
        assertEquals("+00:00:01", zone.getID());
    }

    @Test
    public void testForOffsetMillis_withMillis() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(1);
        assertEquals(1, zone.getOffset(0));
        assertEquals("+00:00:00.001", zone.getID());
    }

    @Test
    public void testForOffsetMillis_max() {
        int max = 86399999; // 23:59:59.999
        DateTimeZone zone = DateTimeZone.forOffsetMillis(max);
        assertEquals(max, zone.getOffset(0));
    }

    @Test
    public void testForOffsetMillis_min() {
        int min = -86399999;
        DateTimeZone zone = DateTimeZone.forOffsetMillis(min);
        assertEquals(min, zone.getOffset(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_tooLarge() {
        DateTimeZone.forOffsetMillis(86400000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_tooSmall() {
        DateTimeZone.forOffsetMillis(-86400000);
    }

    // ==================== forTimeZone ====================
    @Test
    public void testForTimeZone_null() {
        assertSame(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
    }

    @Test
    public void testForTimeZone_UTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
    }

    @Test
    public void testForTimeZone_validLongId() {
        TimeZone tz = TimeZone.getTimeZone("Europe/London");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertEquals("Europe/London", zone.getID());
    }

    @Test
    public void testForTimeZone_shortIdConverted() {
        TimeZone tz = TimeZone.getTimeZone("EST");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertEquals("America/New_York", zone.getID());
    }

    @Test
    public void testForTimeZone_GMTplusFormat() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertEquals(19800000, zone.getOffset(0));
    }

    @Test
    public void testForTimeZone_GMTminusFormat() {
        TimeZone tz = TimeZone.getTimeZone("GMT-05:30");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertEquals(-19800000, zone.getOffset(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTimeZone_invalid() {
        TimeZone tz = TimeZone.getTimeZone("SomeUnknownID");
        // This might actually return a GMT zone with that ID, but forTimeZone should fail because provider won't have it.
        // However, TimeZone.getTimeZone("SomeUnknownID") returns a GMT zone with ID "SomeUnknownID"? Actually it returns GMT with ID "GMT".
        // So we need a truly invalid ID. We can create a custom TimeZone subclass with an invalid ID.
        TimeZone custom = new TimeZone() {
            @Override
            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) { return 0; }
            @Override
            public void setRawOffset(int offsetMillis) {}
            @Override
            public int getRawOffset() { return 0; }
            @Override
            public boolean useDaylightTime() { return false; }
            @Override
            public boolean inDaylightTime(java.util.Date date) { return false; }
        };
        custom.setID("Invalid/Zone");
        DateTimeZone.forTimeZone(custom);
    }

    // ==================== getAvailableIDs ====================
    @Test
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
    }

    // ==================== getProvider / setProvider ====================
    @Test
    public void testGetProvider_default() {
        assertNotNull(DateTimeZone.getProvider());
    }

    @Test
    public void testSetProvider_valid() throws Exception {
        Provider custom = new UTCProvider();
        DateTimeZone.setProvider(custom);
        assertSame(custom, DateTimeZone.getProvider());
    }

    @Test
    public void testSetProvider_null() throws Exception {
        DateTimeZone.setProvider(null);
        // Should reset to default
        assertNotNull(DateTimeZone.getProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_invalidNoIds() throws Exception {
        Provider bad = new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() { return new HashMap<String, String>().keySet(); } // empty set
        };
        DateTimeZone.setProvider(bad);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_invalidNoUTC() throws Exception {
        Provider bad = new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() {
                Set<String> set = new java.util.HashSet<String>();
                set.add("SomeZone");
                return set;
            }
        };
        DateTimeZone.setProvider(bad);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_invalidUTCZone() throws Exception {
        Provider bad = new Provider() {
            public DateTimeZone getZone(String id) {
                if (id.equals("UTC")) return new FixedDateTimeZone("UTC", "UTC", 1000, 1000); // not UTC
                return null;
            }
            public Set<String> getAvailableIDs() {
                Set<String> set = new java.util.HashSet<String>();
                set.add("UTC");
                return set;
            }
        };
        DateTimeZone.setProvider(bad);
    }

    // ==================== getNameProvider / setNameProvider ====================
    @Test
    public void testGetNameProvider_default() {
        assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testSetNameProvider_valid() throws Exception {
        NameProvider custom = new NameProvider() {
            public String getShortName(Locale locale, String id, String nameKey) { return "short"; }
            public String getName(Locale locale, String id, String nameKey) { return "long"; }
        };
        DateTimeZone.setNameProvider(custom);
        assertSame(custom, DateTimeZone.getNameProvider());
    }

    @Test
    public void testSetNameProvider_null() throws Exception {
        DateTimeZone.setNameProvider(null);
        assertNotNull(DateTimeZone.getNameProvider());
    }

    // ==================== Instance methods using FixedDateTimeZone ====================
    private FixedDateTimeZone fixedZone = new FixedDateTimeZone("Test/Zone", "TZ", 3600000, 3600000);

    @Test
    public void testGetID() {
        assertEquals("Test/Zone", fixedZone.getID());
    }

    @Test
    public void testGetNameKey() {
        // FixedDateTimeZone returns null
        assertNull(fixedZone.getNameKey(0));
    }

    @Test
    public void testGetShortName_noLocale() {
        // nameKey null -> returns iID
        assertEquals("Test/Zone", fixedZone.getShortName(0));
    }

    @Test
    public void testGetShortName_withLocale() {
        assertEquals("Test/Zone", fixedZone.getShortName(0, Locale.US));
    }

    @Test
    public void testGetName_noLocale() {
        assertEquals("Test/Zone", fixedZone.getName(0));
    }

    @Test
    public void testGetName_withLocale() {
        assertEquals("Test/Zone", fixedZone.getName(0, Locale.US));
    }

    @Test
    public void testGetOffset_long() {
        assertEquals(3600000, fixedZone.getOffset(0));
    }

    @Test
    public void testGetOffset_ReadableInstant_null() {
        // Should use current time, but offset is constant
        assertEquals(3600000, fixedZone.getOffset((ReadableInstant) null));
    }

    @Test
    public void testGetOffset_ReadableInstant() {
        Instant instant = new Instant(0);
        assertEquals(3600000, fixedZone.getOffset(instant));
    }

    @Test
    public void testGetStandardOffset() {
        assertEquals(3600000, fixedZone.getStandardOffset(0));
    }

    @Test
    public void testIsStandardOffset() {
        assertTrue(fixedZone.isStandardOffset(0));
    }

    @Test
    public void testGetOffsetFromLocal_fixed() {
        assertEquals(3600000, fixedZone.getOffsetFromLocal(0));
    }

    @Test
    public void testConvertUTCToLocal_fixed() {
        long local = fixedZone.convertUTCToLocal(0);
        assertEquals(3600000, local);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_overflow() {
        fixedZone.convertUTCToLocal(Long.MAX_VALUE);
    }

    @Test
    public void testConvertLocalToUTC_fixed() {
        long utc = fixedZone.convertLocalToUTC(3600000, false);
        assertEquals(0, utc);
    }

    @Test
    public void testConvertLocalToUTC_fixed_strict() {
        long utc = fixedZone.convertLocalToUTC(3600000, true);
        assertEquals(0, utc);
    }

    @Test
    public void testConvertLocalToUTC_overflow() {
        try {
            fixedZone.convertLocalToUTC(Long.MIN_VALUE, false);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testConvertLocalToUTC_withOriginal() {
        long utc = fixedZone.convertLocalToUTC(3600000, false, 0);
        assertEquals(0, utc);
    }

    @Test
    public void testGetMillisKeepLocal_sameZone() {
        long instant = 0;
        assertEquals(instant, fixedZone.getMillisKeepLocal(fixedZone, instant));
    }

    @Test
    public void testGetMillisKeepLocal_differentZone() {
        DateTimeZone utc = DateTimeZone.UTC;
        long instant = 0;
        long result = fixedZone.getMillisKeepLocal(utc, instant);
        // local time in fixedZone at instant 0 is 3600000, convert to UTC gives 3600000? Actually convertLocalToUTC(3600000, false, 0) returns 0? Wait, fixedZone.convertLocalToUTC(3600000, false, 0) uses originalInstantUTC=0, offsetOriginal=3600000, instantUTC = 3600000 - 3600000 = 0, offsetLocalFromOriginal = getOffset(0)=3600000, equals offsetOriginal, so returns 0. So result should be 0.
        assertEquals(0, result);
    }

    @Test
    public void testGetMillisKeepLocal_nullZone() {
        long instant = 0;
        long result = fixedZone.getMillisKeepLocal(null, instant);
        // null zone means default, which may not be fixedZone, so result may differ.
        // We just check it doesn't throw.
        assertNotNull(result);
    }

    @Test
    public void testIsLocalDateTimeGap_fixed() {
        LocalDateTime ldt = new LocalDateTime(0);
        assertFalse(fixedZone.isLocalDateTimeGap(ldt));
    }

    @Test
    public void testAdjustOffset_fixed() {
        assertEquals(0, fixedZone.adjustOffset(0, false));
        assertEquals(0, fixedZone.adjustOffset(0, true));
    }

    @Test
    public void testIsFixed() {
        assertTrue(fixedZone.isFixed());
    }

    @Test
    public void testNextTransition_fixed() {
        assertEquals(0, fixedZone.nextTransition(0));
    }

    @Test
    public void testPreviousTransition_fixed() {
        assertEquals(0, fixedZone.previousTransition(0));
    }

    @Test
    public void testToTimeZone() {
        TimeZone tz = fixedZone.toTimeZone();
        assertEquals("Test/Zone", tz.getID());
    }

    @Test
    public void testEquals() {
        FixedDateTimeZone other = new FixedDateTimeZone("Test/Zone", "TZ", 3600000, 3600000);
        assertTrue(fixedZone.equals(other));
        assertFalse(fixedZone.equals(DateTimeZone.UTC));
    }

    @Test
    public void testHashCode() {
        assertEquals(fixedZone.hashCode(), fixedZone.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("Test/Zone", fixedZone.toString());
    }

    @Test
    public void testWriteReplace() throws Exception {
        Object stub = fixedZone.writeReplace();
        assertNotNull(stub);
        // Serialize and deserialize stub
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(stub);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object resolved = ois.readObject();
        assertTrue(resolved instanceof DateTimeZone);
        assertEquals("Test/Zone", ((DateTimeZone) resolved).getID());
    }

    // ==================== Real zone tests (America/New_York) ====================
    private DateTimeZone newYork;

    @Before
    public void setUp() throws Exception {
        newYork = DateTimeZone.forID("America/New_York");
    }

    @Test
    public void testRealZone_getShortName() {
        // Spring: EDT
        long instant = DateTime.parse("2023-06-01").getMillis();
        String name = newYork.getShortName(instant, Locale.US);
        assertEquals("EDT", name);
    }

    @Test
    public void testRealZone_getName() {
        long instant = DateTime.parse("2023-06-01").getMillis();
        String name = newYork.getName(instant, Locale.US);
        assertEquals("Eastern Daylight Time", name);
    }

    @Test
    public void testRealZone_getOffset() {
        long winter = DateTime.parse("2023-01-01").getMillis();
        long summer = DateTime.parse("2023-06-01").getMillis();
        assertEquals(-5 * 3600000, newYork.getOffset(winter));
        assertEquals(-4 * 3600000, newYork.getOffset(summer));
    }

    @Test
    public void testRealZone_isStandardOffset() {
        long winter = DateTime.parse("2023-01-01").getMillis();
        long summer = DateTime.parse("2023-06-01").getMillis();
        assertTrue(newYork.isStandardOffset(winter));
        assertFalse(newYork.isStandardOffset(summer));
    }

    @Test
    public void testRealZone_getOffsetFromLocal_gap() {
        // Spring forward 2023: March 12, 2:00 AM local -> 3:00 AM
        // Local time 2:30 AM is in gap
        long local = DateTime.parse("2023-03-12T02:30:00").getMillis(); // This is actually 2:30 AM EST? Wait, DateTime.parse assumes UTC? We need to construct local millis correctly.
        // Better: use LocalDateTime to get millis in UTC then convert? Actually getOffsetFromLocal expects a local millis (i.e., wall time). We can compute local millis as: UTC millis of 2023-03-12T07:30:00Z? No.
        // We'll use DateTimeZone.convertLocalToUTC to get a local millis? That's circular.
        // We can use: long local = newYork.convertUTCToLocal(DateTime.parse("2023-03-12T07:30:00").getMillis()); That gives local millis for 2:30 AM EST? Actually 2023-03-12T07:30:00Z is 2:30 AM EST, but that's before transition? Transition is at 2:00 AM EST (7:00 UTC). So 7:30 UTC is 2:30 AM EST, which is valid. The gap is from 2:00 AM to 3:00 AM local, which corresponds to UTC 7:00 to 8:00? Actually after transition, 2:00 AM becomes 3:00 AM EDT (UTC 7:00). So local times 2:00-2:59:59.999 are invalid. So local millis for 2:30 AM would be the same as 2:30 AM EST? But that time doesn't exist. We can compute local millis as: UTC millis of 2023-03-12T07:30:00Z is 2:30 AM EST, but that's before gap? No, 7:30 UTC is after transition? Transition is at 7:00 UTC. So 7:30 UTC is 3:30 AM EDT. So local time 2:30 AM doesn't exist. To get a local millis that represents 2:30 AM wall time, we can use: long local = newYork.convertLocalToUTC(DateTime.parse("2023-03-12T02:30:00").getMillis(), false) but that's what we're testing. We'll use a known local millis from a LocalDateTime.
        LocalDateTime ldt = new LocalDateTime(2023, 3, 12, 2, 30);
        long localMillis = ldt.toDateTime(DateTimeZone.UTC).getMillis(); // This gives UTC millis for that local time as if it were UTC, not correct.
        // Actually, to get local millis in the zone, we can do: long localMillis = newYork.convertUTCToLocal(DateTime.parse("2023-03-12T07:30:00").getMillis()); That gives the local millis for 3:30 AM EDT, not 2:30.
        // We need a way to get a local millis that represents 2:30 AM wall time. We can use: long local = DateTime.parse("2023-03-12T02:30:00").getMillis() + newYork.getOffset(DateTime.parse("2023-03-12T02:30:00").getMillis())? That's messy.
        // Simpler: Use the fact that getOffsetFromLocal for a local time in the gap should return the daylight offset (-4 hours). We can test by using a local millis that is just after the gap? Actually we can test with a local millis that is known to be in the gap by using the transition time.
        // We'll compute the transition instant: nextTransition of some time before.
        long beforeTransition = DateTime.parse("2023-03-12T06:00:00").getMillis(); // 1:00 AM EST
        long transition = newYork.nextTransition(beforeTransition);
        // transition is the UTC millis when offset changes. For spring forward, transition is 2023-03-12T07:00:00Z.
        // Local millis for a time in the gap: local = transition + offsetAfter? Actually local millis = transition + offsetAfter? No.
        // We can use: long localGap = transition + (-4 * 3600000) + 30 * 60000; // 2:30 AM EDT? That's after gap.
        // The gap is from local = transition + offsetBefore to local = transition + offsetAfter. offsetBefore = -5h, offsetAfter = -4h.
        // So local gap start = transition - 5h = 2023-03-12T02:00:00 local? Actually 7:00 UTC - 5h = 2:00 AM EST, which is the start of gap. The gap ends at 3:00 AM EDT = transition - 4h = 3:00 AM EDT? Wait, 7:00 UTC - 4h = 3:00 AM EDT. So local times from 2:00 to 3:00 are invalid. So a local millis of 2:30 AM would be transition - 5h + 30min = 7:00 UTC - 5h + 30min = 2:30 AM EST? But that time doesn't exist. However, the local millis value is just a number; we can compute it as: long local = transition + (-5 * 3600000) + 30 * 60000; // This is 2:30 AM EST, but that's before transition? Actually transition is at 7:00 UTC, so 2:30 AM EST is 7:30 UTC, which is after transition, so it's actually 3:30 AM EDT. So that local millis corresponds to 3:30 AM EDT, not 2:30 AM. So we need a local millis that is between transition + offsetBefore and transition + offsetAfter. That is: local = transition + offsetBefore + 1? That would be 2:00:00.001 AM EST, which is invalid. So we can use: long localGap = transition + (-5 * 3600000) + 1; // 2:00:00.001 AM EST (invalid). Then getOffsetFromLocal should return -4 hours (daylight offset) because it favors daylight after gap.
        long localGap = transition + (-5 * 3600000) + 1;
        int offset = newYork.getOffsetFromLocal(localGap);
        assertEquals(-4 * 3600000, offset);
    }

    @Test
    public void testRealZone_getOffsetFromLocal_overlap() {
        // Fall back 2023: Nov 5, 2:00 AM EDT -> 1:00 AM EST
        // Overlap: 1:00 AM to 2:00 AM occurs twice.
        long beforeTransition = DateTime.parse("2023-11-05T05:00:00").getMillis(); // 1:00 AM EDT? Actually 5:00 UTC is 1:00 AM EDT.
        long transition = newYork.nextTransition(beforeTransition);
        // transition is 2023-11-05T06:00:00Z (1:00 AM EST? Actually 6:00 UTC is 1:00 AM EST). So transition is when offset changes from -4 to -5.
        // Overlap start: transition + offsetAfter? Actually local times from 1:00 AM to 2:00 AM are repeated.
        // A local time in overlap, e.g., 1:30 AM, can be either EDT or EST. getOffsetFromLocal should return -4 (EDT) because it favors daylight (earlier).
        long localOverlap = transition + (-5 * 3600000) + 30 * 60000; // 1:30 AM EST? That's after transition? Actually transition is 6:00 UTC, offsetAfter = -5, so local = 6:00 UTC -5 = 1:00 AM EST. So 1:30 AM EST is transition + 30min = 6:30 UTC? That's local = 6:30 UTC -5 = 1:30 AM EST. That's the later occurrence. The earlier occurrence would be 1:30 AM EDT = 5:30 UTC. So local millis for 1:30 AM EDT is 5:30 UTC -4 = 1:30 AM EDT? Actually local millis = UTC + offset. For 5:30 UTC, offset -4, local = 5:30 -4 = 1:30 AM. So local millis value is the same for both? No, local millis is the wall time representation, which is the same number for both occurrences. So we can use any local millis that corresponds to 1:30 AM. We can compute local = transition + (-5 * 3600000) + 30 * 60000; // 1:30 AM EST, but that's after transition. The local millis value for 1:30 AM EDT would be: (transition - 1 hour) + (-4 * 3600000) + 30*60000? That's messy.
        // Simpler: use a local millis that is known to be in the overlap. We can use: long local = newYork.convertUTCToLocal(DateTime.parse("2023-11-05T05:30:00").getMillis()); // 5:30 UTC is 1:30 AM EDT. That gives local millis for 1:30 AM EDT. Then getOffsetFromLocal should return -4.
        long local = newYork.convertUTCToLocal(DateTime.parse("2023-11-05T05:30:00").getMillis());
        int offset = newYork.getOffsetFromLocal(local);
        assertEquals(-4 * 3600000, offset);
    }

    @Test
    public void testRealZone_convertLocalToUTC_gap_strict() {
        // Spring forward gap, strict=true should throw IllegalInstantException
        long transition = newYork.nextTransition(DateTime.parse("2023-03-12T06:00:00").getMillis());
        long localGap = transition + (-5 * 3600000) + 1; // invalid local time
        try {
            newYork.convertLocalToUTC(localGap, true);
            fail("Expected IllegalInstantException");
        } catch (IllegalInstantException e) {
            // expected
        }
    }

    @Test
    public void testRealZone_convertLocalToUTC_gap_nonStrict() {
        long transition = newYork.nextTransition(DateTime.parse("2023-03-12T06:00:00").getMillis());
        long localGap = transition + (-5 * 3600000) + 1;
        long utc = newYork.convertLocalToUTC(localGap, false);
        // Should adjust to after gap, i.e., UTC = localGap - (-4h) = localGap + 4h
        assertEquals(localGap + 4 * 3600000, utc);
    }

    @Test
    public void testRealZone_convertLocalToUTC_overlap() {
        // Overlap: local time 1:30 AM, should return earlier UTC (EDT)
        long local = newYork.convertUTCToLocal(DateTime.parse("2023-11-05T05:30:00").getMillis()); // 1:30 AM EDT
        long utc = newYork.convertLocalToUTC(local, false);
        assertEquals(DateTime.parse("2023-11-05T05:30:00").getMillis(), utc);
    }

    @Test
    public void testRealZone_convertLocalToUTC_withOriginal() {
        // Overlap: use original instant to prefer the same offset
        long originalUtc = DateTime.parse("2023-11-05T05:30:00").getMillis(); // EDT
        long local = newYork.convertUTCToLocal(originalUtc); // 1:30 AM EDT
        long utc = newYork.convertLocalToUTC(local, false, originalUtc);
        assertEquals(originalUtc, utc);
    }

    @Test
    public void testRealZone_isLocalDateTimeGap() {
        LocalDateTime ldtGap = new LocalDateTime(2023, 3, 12, 2, 30);
        assertTrue(newYork.isLocalDateTimeGap(ldtGap));
        LocalDateTime ldtNormal = new LocalDateTime(2023, 3, 12, 3, 30);
        assertFalse(newYork.isLocalDateTimeGap(ldtNormal));
    }

    @Test
    public void testRealZone_adjustOffset_overlap() {
        // Overlap: 1:30 AM, earlierOrLater false -> earlier (EDT), true -> later (EST)
        long local = newYork.convertUTCToLocal(DateTime.parse("2023-11-05T05:30:00").getMillis()); // 1:30 AM EDT
        long adjustedEarlier = newYork.adjustOffset(local, false);
        long adjustedLater = newYork.adjustOffset(local, true);
        // Earlier should be same as local? Actually adjustOffset returns the instant millis (UTC? No, it returns the adjusted instant millis, which is the same as input if not in overlap? The method returns the adjusted instant millis, which is the UTC millis? Let's check: adjustOffset takes an instant (UTC millis) and returns adjusted UTC millis. So we need to pass UTC millis, not local. The method is used to adjust an instant that might be in overlap. So we should pass a UTC instant that falls in the overlap. For example, UTC 5:30 (EDT) is in overlap? Actually overlap is from 5:00 UTC to 6:00 UTC? Let's compute: transition is 6:00 UTC. Overlap is from 5:00 UTC to 6:00 UTC? Actually local times 1:00-2:00 are repeated. The first occurrence is EDT (UTC 5:00-6:00), second is EST (UTC 6:00-7:00). So UTC 5:30 is in the overlap (first occurrence). adjustOffset with earlierOrLater false should return the same instant (5:30), true should return 6:30? Actually later occurrence would be UTC 6:30. So we can test.
        long utc = DateTime.parse("2023-11-05T05:30:00").getMillis();
        long earlier = newYork.adjustOffset(utc, false);
        long later = newYork.adjustOffset(utc, true);
        assertEquals(utc, earlier);
        assertEquals(DateTime.parse("2023-11-05T06:30:00").getMillis(), later);
    }

    @Test
    public void testRealZone_nextTransition() {
        long before = DateTime.parse("2023-03-12T06:00:00").getMillis();
        long next = newYork.nextTransition(before);
        assertTrue(next > before);
    }

    @Test
    public void testRealZone_previousTransition() {
        long after = DateTime.parse("2023-03-12T08:00:00").getMillis();
        long prev = newYork.previousTransition(after);
        assertTrue(prev < after);
    }

    @Test
    public void testRealZone_isFixed() {
        assertFalse(newYork.isFixed());
    }

    // Helper to assert null
    private void assertNull(Object obj) {
        org.junit.Assert.assertNull(obj);
    }
}
