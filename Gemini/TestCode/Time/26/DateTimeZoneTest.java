package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

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

    // ---------------------------------------------------------
    // getDefault() and setDefault() tests
    // ---------------------------------------------------------

    @Test
    public void testGetDefault() {
        DateTimeZone zone = DateTimeZone.getDefault();
        Assert.assertNotNull(zone);
    }

    @Test
    public void testSetDefault() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone.setDefault(london);
        Assert.assertEquals(london, DateTimeZone.getDefault());

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        Assert.assertEquals(paris, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_null() {
        DateTimeZone.setDefault(null);
    }

    // ---------------------------------------------------------
    // forID() tests
    // ---------------------------------------------------------

    @Test
    public void testForID_null() {
        Assert.assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
    }

    @Test
    public void testForID_UTC() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test
    public void testForID_validIDs() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        Assert.assertEquals("Europe/London", zone.getID());

        zone = DateTimeZone.forID("America/New_York");
        Assert.assertEquals("America/New_York", zone.getID());
    }

    @Test
    public void testForID_offsetPatterns() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));

        DateTimeZone zone1 = DateTimeZone.forID("+01:00");
        Assert.assertEquals("+01:00", zone1.getID());
        Assert.assertEquals(3600000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forID("-05:00");
        Assert.assertEquals("-05:00", zone2.getID());
        Assert.assertEquals(-18000000, zone2.getOffset(0L));

        DateTimeZone zone3 = DateTimeZone.forID("+05:30");
        Assert.assertEquals("+05:30", zone3.getID());
        Assert.assertEquals(19800000, zone3.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidID() {
        DateTimeZone.forID("Not_A_Zone_ID");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidOffsetFormat() {
        DateTimeZone.forID("+invalid");
    }

    // ---------------------------------------------------------
    // forOffsetHours() and forOffsetHoursMinutes() tests
    // ---------------------------------------------------------

    @Test
    public void testForOffsetHours() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        DateTimeZone zonePlus2 = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals("+02:00", zonePlus2.getID());
        Assert.assertEquals(7200000, zonePlus2.getOffset(0L));

        DateTimeZone zoneMinus8 = DateTimeZone.forOffsetHours(-8);
        Assert.assertEquals("-08:00", zoneMinus8.getID());
        Assert.assertEquals(-28800000, zoneMinus8.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zone1 = DateTimeZone.forOffsetHoursMinutes(2, 30);
        Assert.assertEquals("+02:30", zone1.getID());
        Assert.assertEquals(9000000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        Assert.assertEquals("-02:30", zone2.getID());
        Assert.assertEquals(-9000000, zone2.getOffset(0L));

        DateTimeZone zone3 = DateTimeZone.forOffsetHoursMinutes(0, 45);
        Assert.assertEquals("+00:45", zone3.getID());
        Assert.assertEquals(2700000, zone3.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(2, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesTooLarge() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_overflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 30);
    }

    // ---------------------------------------------------------
    // forOffsetMillis() tests
    // ---------------------------------------------------------

    @Test
    public void testForOffsetMillis() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        DateTimeZone zonePositive = DateTimeZone.forOffsetMillis(3600000);
        Assert.assertEquals("+01:00", zonePositive.getID());
        Assert.assertEquals(3600000, zonePositive.getOffset(0L));

        DateTimeZone zoneNegative = DateTimeZone.forOffsetMillis(-3600000);
        Assert.assertEquals("-01:00", zoneNegative.getID());
        Assert.assertEquals(-3600000, zoneNegative.getOffset(0L));

        DateTimeZone zoneMillis = DateTimeZone.forOffsetMillis(3600000 + 120000 + 3000 + 4);
        Assert.assertEquals("+01:02:03.004", zoneMillis.getID());

        DateTimeZone zoneSeconds = DateTimeZone.forOffsetMillis(3600000 + 120000 + 3000);
        Assert.assertEquals("+01:02:03", zoneSeconds.getID());

        DateTimeZone cachedZone = DateTimeZone.forOffsetMillis(3600000);
        Assert.assertSame(zonePositive, cachedZone);
    }

    // ---------------------------------------------------------
    // forTimeZone() tests
    // ---------------------------------------------------------

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
    public void testForTimeZone_knownAliases() {
        Assert.assertEquals("America/Los_Angeles", DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST")).getID());
        Assert.assertEquals("America/New_York", DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")).getID());
        Assert.assertEquals("America/Chicago", DateTimeZone.forTimeZone(TimeZone.getTimeZone("CST")).getID());
        Assert.assertEquals("America/Denver", DateTimeZone.forTimeZone(TimeZone.getTimeZone("MST")).getID());
        Assert.assertEquals("Pacific/Honolulu", DateTimeZone.forTimeZone(TimeZone.getTimeZone("HST")).getID());
        Assert.assertEquals("Asia/Tokyo", DateTimeZone.forTimeZone(TimeZone.getTimeZone("JST")).getID());
        Assert.assertEquals("Europe/London", DateTimeZone.forTimeZone(TimeZone.getTimeZone("WET")).getID());
        Assert.assertEquals("Europe/Paris", DateTimeZone.forTimeZone(TimeZone.getTimeZone("ECT")).getID());
        Assert.assertEquals("Asia/Calcutta", DateTimeZone.forTimeZone(TimeZone.getTimeZone("IST")).getID());
    }

    @Test
    public void testForTimeZone_customGMT() {
        TimeZone tz1 = TimeZone.getTimeZone("GMT+02:00");
        DateTimeZone dtz1 = DateTimeZone.forTimeZone(tz1);
        Assert.assertEquals("+02:00", dtz1.getID());

        TimeZone tz2 = TimeZone.getTimeZone("GMT-05:00");
        DateTimeZone dtz2 = DateTimeZone.forTimeZone(tz2);
        Assert.assertEquals("-05:00", dtz2.getID());

        TimeZone tzZero = TimeZone.getTimeZone("GMT+00:00");
        DateTimeZone dtzZero = DateTimeZone.forTimeZone(tzZero);
        Assert.assertSame(DateTimeZone.UTC, dtzZero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTimeZone_unrecognised() {
        TimeZone custom = new TimeZone() {
            @Override
            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
                return 0;
            }

            @Override
            public void setRawOffset(int offsetMillis) {
            }

            @Override
            public int getRawOffset() {
                return 0;
            }

            @Override
            public boolean useDaylightTime() {
                return false;
            }

            @Override
            public boolean inDaylightTime(java.util.Date date) {
                return false;
            }

            @Override
            public String getID() {
                return "UnknownID";
            }

            @Override
            public String getDisplayName() {
                return "UnknownDisplayName";
            }
        };
        DateTimeZone.forTimeZone(custom);
    }

    // ---------------------------------------------------------
    // Provider & NameProvider management
    // ---------------------------------------------------------

    @Test
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains("UTC"));
        Assert.assertTrue(ids.contains("Europe/London"));
    }

    @Test
    public void testSetProvider_null() {
        DateTimeZone.setProvider(null);
        Assert.assertNotNull(DateTimeZone.getProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_emptyIds() {
        Provider emptyProvider = new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }

            public Set<String> getAvailableIDs() {
                return Collections.emptySet();
            }
        };
        DateTimeZone.setProvider(emptyProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_noUTC() {
        Provider noUtcProvider = new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }

            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("America/New_York");
                return set;
            }
        };
        DateTimeZone.setProvider(noUtcProvider);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_invalidUTC() {
        Provider invalidUtcProvider = new Provider() {
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
        };
        DateTimeZone.setProvider(invalidUtcProvider);
    }

    @Test
    public void testSetNameProvider_null() {
        DateTimeZone.setNameProvider(null);
        Assert.assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testSetNameProvider_custom() {
        NameProvider custom = new NameProvider() {
            public String getShortName(Locale locale, String id, String nameKey) {
                return "SHORT";
            }

            public String getName(Locale locale, String id, String nameKey) {
                return "LONG";
            }
        };
        DateTimeZone.setNameProvider(custom);
        Assert.assertSame(custom, DateTimeZone.getNameProvider());
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        Assert.assertEquals("SHORT", zone.getShortName(0L, Locale.ENGLISH));
        Assert.assertEquals("LONG", zone.getName(0L, Locale.ENGLISH));
    }

    // ---------------------------------------------------------
    // Names and display
    // ---------------------------------------------------------

    @Test
    public void testGetShortName_getName() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winter = 0L; // 1970-01-01
        long summer = 15000000000L; // Summer 1970

        Assert.assertNotNull(zone.getShortName(winter));
        Assert.assertNotNull(zone.getName(winter));
        Assert.assertNotNull(zone.getShortName(summer, Locale.UK));
        Assert.assertNotNull(zone.getName(summer, Locale.UK));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        Assert.assertNotNull(fixed.getShortName(winter, Locale.ENGLISH));
        Assert.assertNotNull(fixed.getName(winter, Locale.ENGLISH));
    }

    @Test
    public void testGetShortName_fallbackToOffset() {
        DateTimeZone.setNameProvider(new NameProvider() {
            public String getShortName(Locale locale, String id, String nameKey) {
                return null;
            }

            public String getName(Locale locale, String id, String nameKey) {
                return null;
            }
        });
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        Assert.assertEquals("+00:00", zone.getShortName(0L, Locale.ENGLISH));
        Assert.assertEquals("+00:00", zone.getName(0L, Locale.ENGLISH));
    }

    // ---------------------------------------------------------
    // getOffset and standardOffset tests
    // ---------------------------------------------------------

    @Test
    public void testGetOffset_ReadableInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        Assert.assertEquals(3600000, zone.getOffset(new Instant(0L)));
        Assert.assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset((ReadableInstant) null));
    }

    @Test
    public void testIsStandardOffset() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long winter = 0L;
        long summer = 15000000000L;
        Assert.assertTrue(london.isStandardOffset(winter));
        Assert.assertFalse(london.isStandardOffset(summer));
    }

    // ---------------------------------------------------------
    // Conversions: UTC <-> Local
    // ---------------------------------------------------------

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long utc = 10000L;
        long expectedLocal = 10000L + 7200000L;
        Assert.assertEquals(expectedLocal, zone.convertUTCToLocal(utc));
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        zone.convertUTCToLocal(Long.MAX_VALUE - 100);
    }

    @Test
    public void testConvertLocalToUTC_strict() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long local = 10000L + 7200000L;
        Assert.assertEquals(10000L, zone.convertLocalToUTC(local, true));
        Assert.assertEquals(10000L, zone.convertLocalToUTC(local, false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_dstGapStrict() {
        // America/New_York Spring forward gap: 2007-03-11 02:00:00 -> 03:00:00
        // 2007-03-11T02:30:00.000 in local millis (UTC representation: 1173580200000L)
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long gapLocalMillis = 1173580200000L;
        zone.convertLocalToUTC(gapLocalMillis, true);
    }

    @Test
    public void testConvertLocalToUTC_dstGapNonStrict() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long gapLocalMillis = 1173580200000L;
        long utcResult = zone.convertLocalToUTC(gapLocalMillis, false);
        Assert.assertTrue(utcResult > 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTC_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-2);
        zone.convertLocalToUTC(Long.MAX_VALUE - 100, false);
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Winter standard time
        Assert.assertEquals(-18000000, zone.getOffsetFromLocal(0L));
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");

        long instant = 0L; // 00:00 UTC
        long result = london.getMillisKeepLocal(paris, instant);
        // London is 00:00, so 00:00 in Paris is -1 hour in UTC (-3600000)
        Assert.assertEquals(-3600000L, result);

        Assert.assertEquals(instant, london.getMillisKeepLocal(london, instant));
        Assert.assertEquals(london.getMillisKeepLocal(DateTimeZone.getDefault(), instant),
                london.getMillisKeepLocal(null, instant));
    }

    // ---------------------------------------------------------
    // Gap and Transition checks
    // ---------------------------------------------------------

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone fixed = DateTimeZone.UTC;
        LocalDateTime ldt = new LocalDateTime(2007, 3, 11, 2, 30);
        Assert.assertFalse(fixed.isLocalDateTimeGap(ldt));

        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        Assert.assertTrue(ny.isLocalDateTimeGap(ldt));

        LocalDateTime standardTime = new LocalDateTime(2007, 1, 1, 12, 0);
        Assert.assertFalse(ny.isLocalDateTimeGap(standardTime));
    }

    // ---------------------------------------------------------
    // General Methods: toTimeZone, equals, hashCode, toString, Serialization
    // ---------------------------------------------------------

    @Test
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        TimeZone tz = zone.toTimeZone();
        Assert.assertEquals("Europe/London", tz.getID());
    }

    @Test
    public void testHashCodeAndToString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        Assert.assertEquals(57 + "Europe/London".hashCode(), zone.hashCode());
        Assert.assertEquals("Europe/London", zone.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
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
}
