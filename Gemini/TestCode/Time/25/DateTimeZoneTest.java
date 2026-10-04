package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.chrono.GJChronology;
import org.joda.time.tz.DefaultNameProvider;
import org.joda.time.tz.FixedDateTimeZone;
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

    // -----------------------------------------------------------------------
    // Constants and Basic Construction
    // -----------------------------------------------------------------------

    @Test
    public void testUTCConstant() {
        DateTimeZone utc = DateTimeZone.UTC;
        Assert.assertNotNull(utc);
        Assert.assertEquals("UTC", utc.getID());
        Assert.assertEquals(0, utc.getOffset(0L));
        Assert.assertEquals(0, utc.getStandardOffset(0L));
        Assert.assertTrue(utc.isFixed());
        Assert.assertEquals(0L, utc.nextTransition(0L));
        Assert.assertEquals(0L, utc.previousTransition(0L));
    }

    @Test
    public void testSubclassConstructorNullId() {
        try {
            new MockDateTimeZone(null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Id must not be null", ex.getMessage());
        }
    }

    private static class MockDateTimeZone extends DateTimeZone {
        private static final long serialVersionUID = 1L;

        public MockDateTimeZone(String id) {
            super(id);
        }

        public String getNameKey(long instant) {
            return "MOCK";
        }

        public int getOffset(long instant) {
            return 3600000;
        }

        public int getStandardOffset(long instant) {
            return 3600000;
        }

        public boolean isFixed() {
            return true;
        }

        public long nextTransition(long instant) {
            return instant;
        }

        public long previousTransition(long instant) {
            return instant;
        }

        public boolean equals(Object object) {
            if (object instanceof MockDateTimeZone) {
                return getID().equals(((MockDateTimeZone) object).getID());
            }
            return false;
        }
    }

    // -----------------------------------------------------------------------
    // getDefault and setDefault
    // -----------------------------------------------------------------------

    @Test
    public void testGetAndSetDefault() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        DateTimeZone.setDefault(zone);
        Assert.assertEquals(zone, DateTimeZone.getDefault());

        DateTimeZone.setDefault(DateTimeZone.UTC);
        Assert.assertEquals(DateTimeZone.UTC, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultNull() {
        DateTimeZone.setDefault(null);
    }

    // -----------------------------------------------------------------------
    // forID
    // -----------------------------------------------------------------------

    @Test
    public void testForID_null() {
        DateTimeZone def = DateTimeZone.getDefault();
        Assert.assertEquals(def, DateTimeZone.forID(null));
    }

    @Test
    public void testForID_UTC() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test
    public void testForID_knownZones() {
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        Assert.assertEquals("Europe/London", zoneLondon.getID());

        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        Assert.assertEquals("America/New_York", zoneNY.getID());
    }

    @Test
    public void testForID_offsetString() {
        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        Assert.assertSame(DateTimeZone.UTC, zoneZero);

        DateTimeZone zoneZeroMinus = DateTimeZone.forID("-00:00");
        Assert.assertSame(DateTimeZone.UTC, zoneZeroMinus);

        DateTimeZone zonePlus1 = DateTimeZone.forID("+01:00");
        Assert.assertEquals("+01:00", zonePlus1.getID());
        Assert.assertEquals(3600000, zonePlus1.getOffset(0L));

        DateTimeZone zoneMinus5 = DateTimeZone.forID("-05:00");
        Assert.assertEquals("-05:00", zoneMinus5.getID());
        Assert.assertEquals(-5 * 3600000, zoneMinus5.getOffset(0L));

        DateTimeZone zonePlusCache = DateTimeZone.forID("+01:00");
        Assert.assertSame(zonePlus1, zonePlusCache);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalid() {
        DateTimeZone.forID("InvalidZoneID");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidOffset() {
        DateTimeZone.forID("+99:99");
    }

    // -----------------------------------------------------------------------
    // forOffsetHours and forOffsetHoursMinutes
    // -----------------------------------------------------------------------

    @Test
    public void testForOffsetHours() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals("+02:00", zone.getID());
        Assert.assertEquals(2 * 3600000, zone.getOffset(0L));

        DateTimeZone zoneNeg = DateTimeZone.forOffsetHours(-8);
        Assert.assertEquals("-08:00", zoneNeg.getID());
        Assert.assertEquals(-8 * 3600000, zoneNeg.getOffset(0L));

        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
    }

    @Test
    public void testForOffsetHoursMinutes() {
        DateTimeZone zone0 = DateTimeZone.forOffsetHoursMinutes(0, 0);
        Assert.assertSame(DateTimeZone.UTC, zone0);

        DateTimeZone zonePos = DateTimeZone.forOffsetHoursMinutes(5, 30);
        Assert.assertEquals("+05:30", zonePos.getID());
        Assert.assertEquals((5 * 60 + 30) * 60000, zonePos.getOffset(0L));

        DateTimeZone zoneNeg = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        Assert.assertEquals("-02:30", zoneNeg.getID());
        Assert.assertEquals(-(2 * 60 + 30) * 60000, zoneNeg.getOffset(0L));

        DateTimeZone zoneNegZeroHour = DateTimeZone.forOffsetHoursMinutes(0, 45);
        Assert.assertEquals("+00:45", zoneNegZeroHour.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_overflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    // -----------------------------------------------------------------------
    // forOffsetMillis
    // -----------------------------------------------------------------------

    @Test
    public void testForOffsetMillis() {
        DateTimeZone zone0 = DateTimeZone.forOffsetMillis(0);
        Assert.assertSame(DateTimeZone.UTC, zone0);

        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(3600000);
        Assert.assertEquals("+01:00", zone1.getID());

        DateTimeZone zoneWithSeconds = DateTimeZone.forOffsetMillis(3661000);
        Assert.assertEquals("+01:01:01", zoneWithSeconds.getID());

        DateTimeZone zoneWithMillis = DateTimeZone.forOffsetMillis(3661123);
        Assert.assertEquals("+01:01:01.123", zoneWithMillis.getID());

        DateTimeZone zoneNegativeMillis = DateTimeZone.forOffsetMillis(-3661123);
        Assert.assertEquals("-01:01:01.123", zoneNegativeMillis.getID());

        DateTimeZone zoneCached = DateTimeZone.forOffsetMillis(3661123);
        Assert.assertSame(zoneWithMillis, zoneCached);
    }

    // -----------------------------------------------------------------------
    // forTimeZone
    // -----------------------------------------------------------------------

    @Test
    public void testForTimeZone_null() {
        Assert.assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
    }

    @Test
    public void testForTimeZone_UTC() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(tz));
    }

    @Test
    public void testForTimeZone_aliases() {
        DateTimeZone zonePST = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        Assert.assertEquals("America/Los_Angeles", zonePST.getID());

        DateTimeZone zoneEST = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        Assert.assertEquals("America/New_York", zoneEST.getID());

        DateTimeZone zoneGMT = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT"));
        Assert.assertSame(DateTimeZone.UTC, zoneGMT);

        DateTimeZone zoneMIT = DateTimeZone.forTimeZone(TimeZone.getTimeZone("MIT"));
        Assert.assertEquals("Pacific/Apia", zoneMIT.getID());
    }

    @Test
    public void testForTimeZone_gmtOffsets() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT+08:00");
        DateTimeZone zone1 = DateTimeZone.forTimeZone(tz1);
        Assert.assertEquals("+08:00", zone1.getID());

        TimeZone tz2 = TimeZone.getTimeZone("GMT-05:00");
        DateTimeZone zone2 = DateTimeZone.forTimeZone(tz2);
        Assert.assertEquals("-05:00", zone2.getID());

        TimeZone tzZero = TimeZone.getTimeZone("GMT+00:00");
        DateTimeZone zoneZero = DateTimeZone.forTimeZone(tzZero);
        Assert.assertSame(DateTimeZone.UTC, zoneZero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTimeZone_unrecognised() {
        TimeZone tz = new TimeZone() {
            private static final long serialVersionUID = 1L;

            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
                return 0;
            }

            public void setRawOffset(int offsetMillis) {}

            public int getRawOffset() {
                return 0;
            }

            public boolean useDaylightTime() {
                return false;
            }

            public boolean inDaylightTime(java.util.Date date) {
                return false;
            }

            public String getID() {
                return "NonExistentTimeZoneID";
            }

            public String getDisplayName() {
                return "NonExistentDisplayName";
            }
        };
        DateTimeZone.forTimeZone(tz);
    }

    // -----------------------------------------------------------------------
    // Providers and NameProviders
    // -----------------------------------------------------------------------

    @Test
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains("UTC"));
        Assert.assertTrue(ids.contains("Europe/London"));
        Assert.assertTrue(ids.contains("America/New_York"));
    }

    @Test
    public void testSetProvider_null() {
        DateTimeZone.setProvider(null);
        Assert.assertNotNull(DateTimeZone.getProvider());
        Assert.assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
    }

    @Test
    public void testSetProvider_custom() {
        Provider customProvider = new UTCProvider();
        DateTimeZone.setProvider(customProvider);
        Assert.assertSame(customProvider, DateTimeZone.getProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_emptyIds() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }

            public Set<String> getAvailableIDs() {
                return Collections.emptySet();
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_nullIds() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }

            public Set<String> getAvailableIDs() {
                return null;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_noUTC() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }

            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("Europe/London");
                return set;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_invalidUTCZone() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                if ("UTC".equals(id)) {
                    return new FixedDateTimeZone("UTC", "UTC", 3600000, 3600000);
                }
                return null;
            }

            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("UTC");
                return set;
            }
        });
    }

    @Test
    public void testSetNameProvider_null() {
        DateTimeZone.setNameProvider(null);
        Assert.assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testSetNameProvider_custom() {
        NameProvider customNameProvider = new DefaultNameProvider();
        DateTimeZone.setNameProvider(customNameProvider);
        Assert.assertSame(customNameProvider, DateTimeZone.getNameProvider());
    }

    // -----------------------------------------------------------------------
    // Names and Short Names
    // -----------------------------------------------------------------------

    @Test
    public void testGetNames() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // 1970-01-01 GMT (winter)
        long summerInstant = 10000000000L; // Summer 1970

        Assert.assertNotNull(zone.getName(winterInstant));
        Assert.assertNotNull(zone.getName(winterInstant, Locale.UK));
        Assert.assertNotNull(zone.getShortName(winterInstant));
        Assert.assertNotNull(zone.getShortName(winterInstant, Locale.UK));

        Assert.assertNotNull(zone.getName(summerInstant));
        Assert.assertNotNull(zone.getShortName(summerInstant));

        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(3);
        Assert.assertEquals("+03:00", fixedZone.getName(winterInstant));
        Assert.assertEquals("+03:00", fixedZone.getShortName(winterInstant));
    }

    @Test
    public void testGetName_providerReturnsNull() {
        NameProvider dummyProvider = new NameProvider() {
            public String getShortName(Locale locale, String id, String nameKey) {
                return null;
            }

            public String getName(Locale locale, String id, String nameKey) {
                return null;
            }
        };
        DateTimeZone.setNameProvider(dummyProvider);
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long instant = 0L;
        String name = zone.getName(instant, Locale.ENGLISH);
        String shortName = zone.getShortName(instant, Locale.ENGLISH);
        Assert.assertEquals("+00:00", name);
        Assert.assertEquals("+00:00", shortName);
    }

    @Test
    public void testGetName_nameKeyNull() {
        DateTimeZone zoneNullKey = new MockDateTimeZone("CustomNullKey") {
            private static final long serialVersionUID = 1L;

            public String getNameKey(long instant) {
                return null;
            }
        };
        Assert.assertEquals("CustomNullKey", zoneNullKey.getName(0L));
        Assert.assertEquals("CustomNullKey", zoneNullKey.getShortName(0L));
    }

    // -----------------------------------------------------------------------
    // Offsets and Calculations
    // -----------------------------------------------------------------------

    @Test
    public void testGetOffset_ReadableInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        Instant instant = new Instant(0L);
        Assert.assertEquals(0, zone.getOffset(instant));

        int offsetNow = zone.getOffset((ReadableInstant) null);
        Assert.assertTrue(offsetNow == 0 || offsetNow == 3600000);
    }

    @Test
    public void testIsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // 1970-01-01 (GMT, standard)
        long summerInstant = 10000000000L; // DST
        Assert.assertTrue(zone.isStandardOffset(winterInstant));
        Assert.assertFalse(zone.isStandardOffset(summerInstant));
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        Assert.assertEquals(0, zone.getOffsetFromLocal(0L));

        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        // Spring DST transition in 2007: 2007-03-11 02:00 -> 03:00
        // 2:30 AM local does not exist (gap)
        DateTime localGap = new DateTime(2007, 3, 11, 2, 30, 0, 0, DateTimeZone.UTC);
        int offset = zoneNY.getOffsetFromLocal(localGap.getMillis());
        Assert.assertEquals(-5 * 3600000, offset);

        // Fall DST transition in 2007: 2007-11-04 01:00 -> 02:00 repeats (overlap)
        // In overlap, getOffsetFromLocal favors daylight savings (earlier instant)
        DateTime localOverlap = new DateTime(2007, 11, 4, 1, 30, 0, 0, DateTimeZone.UTC);
        int overlapOffset = zoneNY.getOffsetFromLocal(localOverlap.getMillis());
        Assert.assertEquals(-4 * 3600000, overlapOffset);
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long local = zone.convertUTCToLocal(1000L);
        Assert.assertEquals(1000L + 2 * 3600000, local);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        zone.convertUTCToLocal(Long.MAX_VALUE);
    }

    @Test
    public void testConvertLocalToUTC_strict() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long utc = zone.convertLocalToUTC(1000L + 2 * 3600000, true);
        Assert.assertEquals(1000L, utc);
    }

    @Test
    public void testConvertLocalToUTC_gapStrict() {
        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        DateTime localGap = new DateTime(2007, 3, 11, 2, 30, 0, 0, DateTimeZone.UTC);
        try {
            zoneNY.convertLocalToUTC(localGap.getMillis(), true);
            Assert.fail("Expected IllegalArgumentException on strict conversion in gap");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Illegal instant due to time zone offset transition"));
        }
    }

    @Test
    public void testConvertLocalToUTC_gapNonStrict() {
        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        DateTime localGap = new DateTime(2007, 3, 11, 2, 30, 0, 0, DateTimeZone.UTC);
        long utc = zoneNY.convertLocalToUTC(localGap.getMillis(), false);
        Assert.assertEquals(localGap.getMillis() - (-5 * 3600000), utc);
    }

    @Test
    public void testConvertLocalToUTC_withOriginalInstant() {
        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        DateTime dt = new DateTime(2007, 1, 1, 0, 0, 0, 0, zoneNY);
        long local = dt.getMillis() + zoneNY.getOffset(dt.getMillis());
        long utc = zoneNY.convertLocalToUTC(local, false, dt.getMillis());
        Assert.assertEquals(dt.getMillis(), utc);

        long utcDifferentOriginal = zoneNY.convertLocalToUTC(local, false, 0L);
        Assert.assertEquals(dt.getMillis(), utcDifferentOriginal);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTC_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        zone.convertLocalToUTC(Long.MIN_VALUE, false);
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(1);
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(3);

        long instant = 1000000L;
        long keepSame = zone1.getMillisKeepLocal(zone1, instant);
        Assert.assertEquals(instant, keepSame);

        long keepNull = zone1.getMillisKeepLocal(null, instant);
        DateTimeZone def = DateTimeZone.getDefault();
        long expected = instant + zone1.getOffset(instant) - def.getOffsetFromLocal(instant + zone1.getOffset(instant));
        Assert.assertEquals(expected, keepNull);

        long converted = zone1.getMillisKeepLocal(zone2, instant);
        Assert.assertEquals(instant - 2 * 3600000, converted);
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        LocalDateTime gapTime = new LocalDateTime(2007, 3, 11, 2, 30, 0, 0);
        Assert.assertTrue(zoneNY.isLocalDateTimeGap(gapTime));

        LocalDateTime validTime = new LocalDateTime(2007, 3, 11, 1, 30, 0, 0);
        Assert.assertFalse(zoneNY.isLocalDateTimeGap(validTime));

        DateTimeZone fixedZone = DateTimeZone.forOffsetHours(1);
        Assert.assertFalse(fixedZone.isLocalDateTimeGap(gapTime));
    }

    // -----------------------------------------------------------------------
    // toTimeZone, equals, hashCode, toString
    // -----------------------------------------------------------------------

    @Test
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        TimeZone tz = zone.toTimeZone();
        Assert.assertEquals("Europe/London", tz.getID());

        DateTimeZone utc = DateTimeZone.UTC;
        Assert.assertEquals("UTC", utc.toTimeZone().getID());
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(1);
        DateTimeZone zone2 = DateTimeZone.forID("+01:00");
        DateTimeZone zone3 = DateTimeZone.forOffsetHours(2);

        Assert.assertEquals(zone1, zone2);
        Assert.assertEquals(zone1.hashCode(), zone2.hashCode());

        Assert.assertNotEquals(zone1, zone3);
        Assert.assertFalse(zone1.equals("NotADateTimeZone"));
        Assert.assertFalse(zone1.equals(null));
    }

    @Test
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        Assert.assertEquals("Europe/Paris", zone.toString());
        Assert.assertEquals("Europe/Paris", zone.getID());
    }

    // -----------------------------------------------------------------------
    // Serialization
    // -----------------------------------------------------------------------

    @Test
    public void testSerialization() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        Assert.assertSame(zone, deserialized);
    }

    @Test
    public void testSerializationUTC() throws Exception {
        DateTimeZone zone = DateTimeZone.UTC;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        Assert.assertSame(DateTimeZone.UTC, deserialized);
    }

    @Test
    public void testSerializationFixedOffset() throws Exception {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        Assert.assertSame(zone, deserialized);
    }

    // -----------------------------------------------------------------------
    // Private / Internal branch tests via Reflection
    // -----------------------------------------------------------------------

    @Test
    public void testParseOffsetHelper() throws Exception {
        Method method = DateTimeZone.class.getDeclaredMethod("parseOffset", String.class);
        method.setAccessible(true);

        int offset1 = (Integer) method.invoke(null, "+02:00");
        Assert.assertEquals(2 * 3600000, offset1);

        int offset2 = (Integer) method.invoke(null, "-05:30");
        Assert.assertEquals(-(5 * 60 + 30) * 60000, offset2);
    }

    @Test
    public void testPrintOffsetHelper() throws Exception {
        Method method = DateTimeZone.class.getDeclaredMethod("printOffset", int.class);
        method.setAccessible(true);

        String str0 = (String) method.invoke(null, 0);
        Assert.assertEquals("+00:00", str0);

        String strPos = (String) method.invoke(null, 3600000);
        Assert.assertEquals("+01:00", strPos);

        String strNeg = (String) method.invoke(null, -18000000);
        Assert.assertEquals("-05:00", strNeg);

        String strSec = (String) method.invoke(null, 3661000);
        Assert.assertEquals("+01:01:01", strSec);

        String strMilli = (String) method.invoke(null, -3661123);
        Assert.assertEquals("-01:01:01.123", strMilli);
    }

    @Test
    public void testGetConvertedId() throws Exception {
        Method method = DateTimeZone.class.getDeclaredMethod("getConvertedId", String.class);
        method.setAccessible(true);

        Assert.assertEquals("UTC", method.invoke(null, "GMT"));
        Assert.assertEquals("America/New_York", method.invoke(null, "EST"));
        Assert.assertEquals("Europe/London", method.invoke(null, "WET"));
        Assert.assertEquals("Asia/Tokyo", method.invoke(null, "JST"));
        Assert.assertEquals("Australia/Sydney", method.invoke(null, "AET"));
        Assert.assertNull(method.invoke(null, "UnknownOldID"));
    }
}
