package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.joda.time.chrono.ISOChronology;
import org.joda.time.tz.DefaultNameProvider;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateTimeZoneTest {

    private DateTimeZone originalDefaultZone;
    private Provider originalProvider;
    private NameProvider originalNameProvider;
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalDefaultZone = DateTimeZone.getDefault();
        originalProvider = DateTimeZone.getProvider();
        originalNameProvider = DateTimeZone.getNameProvider();
        originalLocale = Locale.getDefault();
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefaultZone);
        DateTimeZone.setProvider(originalProvider);
        DateTimeZone.setNameProvider(originalNameProvider);
        Locale.setDefault(originalLocale);
    }

    @Test
    public void testUTCConstant() {
        DateTimeZone zone = DateTimeZone.UTC;
        Assert.assertNotNull(zone);
        Assert.assertEquals("UTC", zone.getID());
        Assert.assertTrue(zone.isFixed());
        Assert.assertEquals(0, zone.getOffset(0L));
        Assert.assertEquals(0, zone.getStandardOffset(0L));
        Assert.assertEquals(0L, zone.nextTransition(0L));
        Assert.assertEquals(0L, zone.previousTransition(0L));
    }

    @Test
    public void testGetAndSetDefault() {
        DateTimeZone initial = DateTimeZone.getDefault();
        Assert.assertNotNull(initial);

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        Assert.assertEquals(paris, DateTimeZone.getDefault());

        DateTimeZone.setDefault(DateTimeZone.UTC);
        Assert.assertEquals(DateTimeZone.UTC, DateTimeZone.getDefault());

        try {
            DateTimeZone.setDefault(null);
            Assert.fail("Expected IllegalArgumentException for null default zone");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testForID_NullReturnsDefault() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        Assert.assertEquals(DateTimeZone.UTC, DateTimeZone.forID(null));

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone.setDefault(london);
        Assert.assertEquals(london, DateTimeZone.forID(null));
    }

    @Test
    public void testForID_UTC() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test
    public void testForID_ValidZoneIDs() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Assert.assertEquals("America/New_York", zone.getID());
        Assert.assertFalse(zone.isFixed());

        zone = DateTimeZone.forID("Europe/London");
        Assert.assertEquals("Europe/London", zone.getID());

        zone = DateTimeZone.forID("Asia/Tokyo");
        Assert.assertEquals("Asia/Tokyo", zone.getID());
    }

    @Test
    public void testForID_Offsets() {
        DateTimeZone zone = DateTimeZone.forID("+00:00");
        Assert.assertSame(DateTimeZone.UTC, zone);

        zone = DateTimeZone.forID("-00:00");
        Assert.assertSame(DateTimeZone.UTC, zone);

        zone = DateTimeZone.forID("+01:00");
        Assert.assertEquals("+01:00", zone.getID());
        Assert.assertEquals(3600000, zone.getOffset(0L));

        zone = DateTimeZone.forID("-08:00");
        Assert.assertEquals("-08:00", zone.getID());
        Assert.assertEquals(-28800000, zone.getOffset(0L));

        zone = DateTimeZone.forID("+05:30");
        Assert.assertEquals("+05:30", zone.getID());
        Assert.assertEquals(19800000, zone.getOffset(0L));

        zone = DateTimeZone.forID("-03:30");
        Assert.assertEquals("-03:30", zone.getID());
        Assert.assertEquals(-12600000, zone.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidID() {
        DateTimeZone.forID("Invalid/TimeZone_ID_That_Does_Not_Exist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidOffsetFormat() {
        DateTimeZone.forID("+25:00");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidOffsetNonDigits() {
        DateTimeZone.forID("+AB:CD");
    }

    @Test
    public void testForOffsetHours() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(5);
        Assert.assertEquals("+05:00", zone1.getID());
        Assert.assertEquals(5 * 3600000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHours(-8);
        Assert.assertEquals("-08:00", zone2.getID());
        Assert.assertEquals(-8 * 3600000, zone2.getOffset(0L));

        DateTimeZone zoneMax = DateTimeZone.forOffsetHours(23);
        Assert.assertEquals("+23:00", zoneMax.getID());

        DateTimeZone zoneMin = DateTimeZone.forOffsetHours(-23);
        Assert.assertEquals("-23:00", zoneMin.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_TooLarge() {
        DateTimeZone.forOffsetHours(24);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_TooSmall() {
        DateTimeZone.forOffsetHours(-24);
    }

    @Test
    public void testForOffsetHoursMinutes() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zone1 = DateTimeZone.forOffsetHoursMinutes(2, 30);
        Assert.assertEquals("+02:30", zone1.getID());
        Assert.assertEquals(2 * 3600000 + 30 * 60000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        Assert.assertEquals("-02:30", zone2.getID());
        Assert.assertEquals(-(2 * 3600000 + 30 * 60000), zone2.getOffset(0L));

        DateTimeZone zone3 = DateTimeZone.forOffsetHoursMinutes(0, 45);
        Assert.assertEquals("+00:45", zone3.getID());
        Assert.assertEquals(45 * 60000, zone3.getOffset(0L));

        DateTimeZone zone4 = DateTimeZone.forOffsetHoursMinutes(-0, 45);
        Assert.assertEquals("+00:45", zone4.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_MinutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(2, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_MinutesTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_HoursTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_HoursTooLow() {
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    @Test
    public void testForOffsetMillis() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        DateTimeZone zonePositive = DateTimeZone.forOffsetMillis(3600000);
        Assert.assertEquals("+01:00", zonePositive.getID());

        DateTimeZone zoneNegative = DateTimeZone.forOffsetMillis(-3600000);
        Assert.assertEquals("-01:00", zoneNegative.getID());

        // Offset with seconds and milliseconds
        DateTimeZone zoneDetailed = DateTimeZone.forOffsetMillis(3661001);
        Assert.assertEquals("+01:01:01.001", zoneDetailed.getID());
        Assert.assertEquals(3661001, zoneDetailed.getOffset(0L));

        DateTimeZone zoneDetailedNegative = DateTimeZone.forOffsetMillis(-3661001);
        Assert.assertEquals("-01:01:01.001", zoneDetailedNegative.getID());
        Assert.assertEquals(-3661001, zoneDetailedNegative.getOffset(0L));

        DateTimeZone zoneSecondsOnly = DateTimeZone.forOffsetMillis(3605000);
        Assert.assertEquals("+01:00:05", zoneSecondsOnly.getID());

        DateTimeZone zoneSecondsOnlyNeg = DateTimeZone.forOffsetMillis(-3605000);
        Assert.assertEquals("-01:00:05", zoneSecondsOnlyNeg.getID());

        // SoftReference cache test: requesting again should return cached instance
        DateTimeZone zoneCached = DateTimeZone.forOffsetMillis(3661001);
        Assert.assertSame(zoneDetailed, zoneCached);
    }

    @Test
    public void testForTimeZone() {
        Assert.assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));

        TimeZone tzUtc = TimeZone.getTimeZone("UTC");
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(tzUtc));

        TimeZone tzPst = TimeZone.getTimeZone("PST");
        DateTimeZone zonePst = DateTimeZone.forTimeZone(tzPst);
        Assert.assertEquals("America/Los_Angeles", zonePst.getID());

        TimeZone tzEst = TimeZone.getTimeZone("EST");
        DateTimeZone zoneEst = DateTimeZone.forTimeZone(tzEst);
        Assert.assertEquals("America/New_York", zoneEst.getID());

        TimeZone tzGmt = TimeZone.getTimeZone("GMT");
        DateTimeZone zoneGmt = DateTimeZone.forTimeZone(tzGmt);
        Assert.assertSame(DateTimeZone.UTC, zoneGmt);

        TimeZone tzGmtPlus = TimeZone.getTimeZone("GMT+02:00");
        DateTimeZone zoneGmtPlus = DateTimeZone.forTimeZone(tzGmtPlus);
        Assert.assertEquals("+02:00", zoneGmtPlus.getID());

        TimeZone tzGmtMinus = TimeZone.getTimeZone("GMT-05:00");
        DateTimeZone zoneGmtMinus = DateTimeZone.forTimeZone(tzGmtMinus);
        Assert.assertEquals("-05:00", zoneGmtMinus.getID());

        TimeZone tzGmtZero = TimeZone.getTimeZone("GMT+00:00");
        DateTimeZone zoneGmtZero = DateTimeZone.forTimeZone(tzGmtZero);
        Assert.assertSame(DateTimeZone.UTC, zoneGmtZero);

        TimeZone customTz = new SimpleTimeZone(0, "Custom_Unknown_Zone");
        try {
            DateTimeZone.forTimeZone(customTz);
            Assert.fail("Expected IllegalArgumentException for unknown TimeZone");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains("UTC"));
        Assert.assertTrue(ids.contains("America/New_York"));
        Assert.assertTrue(ids.contains("Europe/London"));
        Assert.assertTrue(ids.contains("Asia/Tokyo"));

        try {
            ids.add("Some/New/Zone");
            Assert.fail("Available IDs set should be unmodifiable");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    @Test
    public void testSetProvider() {
        Provider current = DateTimeZone.getProvider();
        Assert.assertNotNull(current);

        // Reset with null should restore default provider
        DateTimeZone.setProvider(null);
        Assert.assertNotNull(DateTimeZone.getProvider());

        // Test custom valid provider
        Provider customProvider = new UTCProvider();
        DateTimeZone.setProvider(customProvider);
        Assert.assertSame(customProvider, DateTimeZone.getProvider());

        // Provider with empty IDs
        try {
            DateTimeZone.setProvider(new Provider() {
                public DateTimeZone getZone(String name) { return null; }
                public Set<String> getAvailableIDs() { return Collections.emptySet(); }
            });
            Assert.fail("Expected IllegalArgumentException for empty IDs");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        // Provider without UTC
        try {
            DateTimeZone.setProvider(new Provider() {
                public DateTimeZone getZone(String name) { return null; }
                public Set<String> getAvailableIDs() {
                    Set<String> set = new HashSet<String>();
                    set.add("America/New_York");
                    return set;
                }
            });
            Assert.fail("Expected IllegalArgumentException for provider without UTC");
        } catch (IllegalArgumentException ex) {
            // expected
        }

        // Provider with invalid UTC zone object
        try {
            DateTimeZone.setProvider(new Provider() {
                public DateTimeZone getZone(String name) {
                    if ("UTC".equals(name)) {
                        return DateTimeZone.forOffsetHours(1);
                    }
                    return null;
                }
                public Set<String> getAvailableIDs() {
                    Set<String> set = new HashSet<String>();
                    set.add("UTC");
                    return set;
                }
            });
            Assert.fail("Expected IllegalArgumentException for invalid UTC zone returned");
        } catch (IllegalArgumentException ex) {
            // expected
        }
    }

    @Test
    public void testSetNameProvider() {
        NameProvider current = DateTimeZone.getNameProvider();
        Assert.assertNotNull(current);

        DateTimeZone.setNameProvider(null);
        Assert.assertNotNull(DateTimeZone.getNameProvider());

        NameProvider custom = new DefaultNameProvider();
        DateTimeZone.setNameProvider(custom);
        Assert.assertSame(custom, DateTimeZone.getNameProvider());
    }

    @Test
    public void testNamesAndShortNames() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long winterInstant = new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2020, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        String shortNameWinter = zone.getShortName(winterInstant);
        String shortNameSummer = zone.getShortName(summerInstant);
        Assert.assertNotNull(shortNameWinter);
        Assert.assertNotNull(shortNameSummer);

        String longNameWinter = zone.getName(winterInstant);
        String longNameSummer = zone.getName(summerInstant);
        Assert.assertNotNull(longNameWinter);
        Assert.assertNotNull(longNameSummer);

        // With locale
        Assert.assertNotNull(zone.getShortName(winterInstant, Locale.ENGLISH));
        Assert.assertNotNull(zone.getName(winterInstant, Locale.ENGLISH));

        // Fixed offset zone name handling
        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(3);
        Assert.assertEquals("+03:00", fixedZone.getName(winterInstant, Locale.ENGLISH));
        Assert.assertEquals("+03:00", fixedZone.getShortName(winterInstant, Locale.ENGLISH));
        Assert.assertEquals("+03:00", fixedZone.getName(winterInstant));
        Assert.assertEquals("+03:00", fixedZone.getShortName(winterInstant));

        // Zone with no name provider match falls back to offset print
        NameProvider emptyProvider = new NameProvider() {
            public String getShortName(Locale locale, String id, String nameKey) { return null; }
            public String getName(Locale locale, String id, String nameKey) { return null; }
        };
        DateTimeZone.setNameProvider(emptyProvider);
        Assert.assertEquals("-05:00", zone.getShortName(winterInstant, Locale.ENGLISH));
        Assert.assertEquals("-05:00", zone.getName(winterInstant, Locale.ENGLISH));
    }

    @Test
    public void testGetOffsetWithReadableInstant() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        DateTime dt = new DateTime(2020, 1, 1, 12, 0, DateTimeZone.UTC);
        Assert.assertEquals(-5 * 3600000, zone.getOffset(dt));

        // null instant means now
        int currentOffset = zone.getOffset((ReadableInstant) null);
        Assert.assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), currentOffset);
    }

    @Test
    public void testIsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long winterInstant = new DateTime(2020, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2020, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        Assert.assertTrue(zone.isStandardOffset(winterInstant));
        Assert.assertFalse(zone.isStandardOffset(summerInstant));
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");

        // Normal standard time: Jan 15, 2020
        long localNormal = new DateTime(2020, 1, 15, 12, 0, ISOChronology.getInstanceUTC()).getMillis();
        Assert.assertEquals(-5 * 3600000, zone.getOffsetFromLocal(localNormal));

        // Normal daylight time: July 15, 2020
        long localSummer = new DateTime(2020, 7, 15, 12, 0, ISOChronology.getInstanceUTC()).getMillis();
        Assert.assertEquals(-4 * 3600000, zone.getOffsetFromLocal(localSummer));

        // Gap in Spring (March 8, 2020, 2:30 AM does not exist, jump from 2:00 to 3:00)
        long localGap = new DateTime(2020, 3, 8, 2, 30, ISOChronology.getInstanceUTC()).getMillis();
        int offsetGap = zone.getOffsetFromLocal(localGap);
        Assert.assertEquals(-5 * 3600000, offsetGap);

        // Overlap in Autumn (Nov 1, 2020, 1:30 AM repeats, from -4 to -5)
        long localOverlap = new DateTime(2020, 11, 1, 1, 30, ISOChronology.getInstanceUTC()).getMillis();
        int offsetOverlap = zone.getOffsetFromLocal(localOverlap);
        Assert.assertEquals(-4 * 3600000, offsetOverlap);

        // Fixed zone offset from local
        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals(2 * 3600000, fixed.getOffsetFromLocal(localNormal));
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        long utc = 1000000L;
        Assert.assertEquals(1000000L + 3 * 3600000, zone.convertUTCToLocal(utc));

        // Overflow checks
        try {
            zone.convertUTCToLocal(Long.MAX_VALUE);
            Assert.fail("Expected ArithmeticException on overflow");
        } catch (ArithmeticException ex) {
            // expected
        }

        DateTimeZone zoneNegative = DateTimeZone.forOffsetHours(-3);
        try {
            zoneNegative.convertUTCToLocal(Long.MIN_VALUE);
            Assert.fail("Expected ArithmeticException on underflow");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    @Test
    public void testConvertLocalToUTC_StrictAndLenient() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");

        // Normal conversion
        long localSummer = new DateTime(2020, 7, 15, 12, 0, ISOChronology.getInstanceUTC()).getMillis();
        long utcSummer = zone.convertLocalToUTC(localSummer, true);
        Assert.assertEquals(localSummer - (-4 * 3600000), utcSummer);

        // DST Gap (March 8, 2020: 02:30 doesn't exist)
        long localGap = new DateTime(2020, 3, 8, 2, 30, ISOChronology.getInstanceUTC()).getMillis();

        try {
            zone.convertLocalToUTC(localGap, true);
            Assert.fail("Expected IllegalInstantException when converting non-existent gap strictly");
        } catch (IllegalInstantException ex) {
            // expected
        }

        // Lenient conversion during DST gap
        long utcGapLenient = zone.convertLocalToUTC(localGap, false);
        Assert.assertEquals(localGap - (-5 * 3600000), utcGapLenient);

        // DST Overlap (Nov 1, 2020: 01:30 repeats)
        long localOverlap = new DateTime(2020, 11, 1, 1, 30, ISOChronology.getInstanceUTC()).getMillis();
        long utcOverlapStrict = zone.convertLocalToUTC(localOverlap, true);
        Assert.assertEquals(localOverlap - (-4 * 3600000), utcOverlapStrict);

        // convertLocalToUTC with originalInstantUTC
        long originalInstant = localOverlap - (-4 * 3600000);
        long converted = zone.convertLocalToUTC(localOverlap, false, originalInstant);
        Assert.assertEquals(originalInstant, converted);

        long originalInstantWinter = localOverlap - (-5 * 3600000);
        long convertedWinter = zone.convertLocalToUTC(localOverlap, false, originalInstantWinter);
        Assert.assertEquals(originalInstantWinter, convertedWinter);

        // Overflow
        try {
            zone.convertLocalToUTC(Long.MIN_VALUE, true);
            Assert.fail("Expected ArithmeticException on underflow");
        } catch (ArithmeticException ex) {
            // expected
        }
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone ny = DateTimeZone.forID("America/New_York");

        long instant = new DateTime(2020, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();

        // Same zone
        Assert.assertEquals(instant, london.getMillisKeepLocal(london, instant));

        // null newZone defaults to default DateTimeZone
        DateTimeZone.setDefault(DateTimeZone.UTC);
        Assert.assertEquals(instant, london.getMillisKeepLocal(null, instant));

        // London (UTC+0 in winter) to NY (UTC-5 in winter)
        // 12:00 in London represents 12:00 in NY which is 17:00 UTC
        long inNy = london.getMillisKeepLocal(ny, instant);
        Assert.assertEquals(instant + 5 * 3600000, inNy);
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");

        LocalDateTime normal = new LocalDateTime(2020, 1, 15, 12, 0);
        Assert.assertFalse(zone.isLocalDateTimeGap(normal));

        LocalDateTime gapTime = new LocalDateTime(2020, 3, 8, 2, 30);
        Assert.assertTrue(zone.isLocalDateTimeGap(gapTime));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        Assert.assertFalse(fixed.isLocalDateTimeGap(gapTime));
    }

    @Test
    public void testAdjustOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");

        // Normal instant (no overlap)
        long normalInstant = new DateTime(2020, 1, 15, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(normalInstant, zone.adjustOffset(normalInstant, true));
        Assert.assertEquals(normalInstant, zone.adjustOffset(normalInstant, false));

        // DST Overlap: Nov 1, 2020. Transition occurs at 02:00 EDT (06:00 UTC) -> 01:00 EST.
        // Overlap range is between 05:00 UTC and 07:00 UTC.
        // 05:30 UTC is in earlier offset (-4 hrs -> 01:30 EDT)
        long instantEarlier = new DateTime(2020, 11, 1, 1, 30, DateTimeZone.forOffsetHours(-4)).getMillis();
        long instantLater = new DateTime(2020, 11, 1, 1, 30, DateTimeZone.forOffsetHours(-5)).getMillis();

        long adjustedLater = zone.adjustOffset(instantEarlier, true);
        Assert.assertEquals(instantLater, adjustedLater);

        long adjustedEarlier = zone.adjustOffset(instantLater, false);
        Assert.assertEquals(instantEarlier, adjustedEarlier);

        // Same offset adjustment returns original
        Assert.assertEquals(instantEarlier, zone.adjustOffset(instantEarlier, false));
        Assert.assertEquals(instantLater, zone.adjustOffset(instantLater, true));
    }

    @Test
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        TimeZone tz = zone.toTimeZone();
        Assert.assertEquals("America/New_York", tz.getID());

        DateTimeZone fixed = DateTimeZone.forOffsetHours(5);
        TimeZone tzFixed = fixed.toTimeZone();
        Assert.assertEquals("+05:00", tzFixed.getID());
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("America/New_York");
        DateTimeZone zone2 = DateTimeZone.forID("America/New_York");
        DateTimeZone zone3 = DateTimeZone.forID("Europe/London");

        Assert.assertTrue(zone1.equals(zone1));
        Assert.assertTrue(zone1.equals(zone2));
        Assert.assertFalse(zone1.equals(zone3));
        Assert.assertFalse(zone1.equals(null));
        Assert.assertFalse(zone1.equals("America/New_York"));

        Assert.assertEquals(zone1.hashCode(), zone2.hashCode());
        Assert.assertEquals(57 + zone1.getID().hashCode(), zone1.hashCode());
    }

    @Test
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Assert.assertEquals("America/New_York", zone.toString());

        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        Assert.assertEquals("+03:00", fixed.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        Assert.assertEquals(zone, deserialized);
        Assert.assertSame(zone, deserialized);
    }

    @Test
    public void testSerializationFixedZone() throws Exception {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        Assert.assertEquals(zone, deserialized);
    }

    @Test
    public void testConstructorNullCheck() throws Exception {
        Constructor<DateTimeZone> constructor = DateTimeZone.class.getDeclaredConstructor(String.class);
        Assert.assertTrue(Modifier.isProtected(constructor.getModifiers()));
        constructor.setAccessible(true);
        try {
            constructor.newInstance((String) null);
            Assert.fail("Expected exception when constructing DateTimeZone with null ID");
        } catch (Exception ex) {
            Assert.assertTrue(ex.getCause() instanceof IllegalArgumentException);
        }
    }
}
