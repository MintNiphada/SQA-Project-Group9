package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.joda.time.tz.DefaultNameProvider;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateTimeZoneTest {

    private DateTimeZone originalDefault;
    private Provider originalProvider;
    private NameProvider originalNameProvider;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
        originalProvider = DateTimeZone.getProvider();
        originalNameProvider = DateTimeZone.getNameProvider();
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefault);
        DateTimeZone.setProvider(originalProvider);
        DateTimeZone.setNameProvider(originalNameProvider);
    }

    @Test
    public void testGetAndSetDefault() {
        DateTimeZone defaultZone = DateTimeZone.getDefault();
        Assert.assertNotNull(defaultZone);

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        Assert.assertEquals(paris, DateTimeZone.getDefault());

        DateTimeZone.setDefault(DateTimeZone.UTC);
        Assert.assertEquals(DateTimeZone.UTC, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_Null() {
        DateTimeZone.setDefault(null);
    }

    @Test
    public void testForID_Null() {
        DateTimeZone.setDefault(DateTimeZone.forID("Europe/London"));
        Assert.assertEquals(DateTimeZone.forID("Europe/London"), DateTimeZone.forID((String) null));
    }

    @Test
    public void testForID_UTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        Assert.assertSame(DateTimeZone.UTC, zone);
        Assert.assertEquals("UTC", zone.getID());
    }

    @Test
    public void testForID_ProviderIDs() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        Assert.assertNotNull(zone);
        Assert.assertEquals("America/New_York", zone.getID());

        DateTimeZone tokyo = DateTimeZone.forID("Asia/Tokyo");
        Assert.assertNotNull(tokyo);
        Assert.assertEquals("Asia/Tokyo", tokyo.getID());
    }

    @Test
    public void testForID_Offsets() {
        DateTimeZone zoneZero = DateTimeZone.forID("+00:00");
        Assert.assertSame(DateTimeZone.UTC, zoneZero);

        DateTimeZone zoneZeroNeg = DateTimeZone.forID("-00:00");
        Assert.assertSame(DateTimeZone.UTC, zoneZeroNeg);

        DateTimeZone zonePlus = DateTimeZone.forID("+02:00");
        Assert.assertEquals("+02:00", zonePlus.getID());
        Assert.assertEquals(2 * 3600 * 1000, zonePlus.getOffset(0L));

        DateTimeZone zoneMinus = DateTimeZone.forID("-05:30");
        Assert.assertEquals("-05:30", zoneMinus.getID());
        Assert.assertEquals(-(5 * 3600 + 30 * 60) * 1000, zoneMinus.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidString() {
        DateTimeZone.forID("Invalid/Non_Existent_Zone");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidOffsetFormat() {
        DateTimeZone.forID("+25:00");
    }

    @Test
    public void testForOffsetHours() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));

        DateTimeZone zonePlus = DateTimeZone.forOffsetHours(5);
        Assert.assertEquals("+05:00", zonePlus.getID());
        Assert.assertEquals(5 * 3600 * 1000, zonePlus.getOffset(0L));

        DateTimeZone zoneMinus = DateTimeZone.forOffsetHours(-8);
        Assert.assertEquals("-08:00", zoneMinus.getID());
        Assert.assertEquals(-8 * 3600 * 1000, zoneMinus.getOffset(0L));
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
        Assert.assertEquals((2 * 60 + 30) * 60000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        Assert.assertEquals("-02:30", zone2.getID());
        Assert.assertEquals(-(2 * 60 + 30) * 60000, zone2.getOffset(0L));

        DateTimeZone zone3 = DateTimeZone.forOffsetHoursMinutes(0, 45);
        Assert.assertEquals("+00:45", zone3.getID());
        Assert.assertEquals(45 * 60000, zone3.getOffset(0L));

        DateTimeZone zone4 = DateTimeZone.forOffsetHoursMinutes(23, 59);
        Assert.assertEquals("+23:59", zone4.getID());

        DateTimeZone zone5 = DateTimeZone.forOffsetHoursMinutes(-23, 59);
        Assert.assertEquals("-23:59", zone5.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_HoursTooLarge() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_HoursTooSmall() {
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_MinutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(2, -15);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_MinutesTooLarge() {
        DateTimeZone.forOffsetHoursMinutes(2, 60);
    }

    @Test
    public void testForOffsetMillis() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        Assert.assertEquals("+01:00", zone.getID());
        Assert.assertEquals(3600000, zone.getOffset(0L));

        DateTimeZone zoneCached = DateTimeZone.forOffsetMillis(3600000);
        Assert.assertSame(zone, zoneCached);

        DateTimeZone zoneSec = DateTimeZone.forOffsetMillis(3661000);
        Assert.assertEquals("+01:01:01", zoneSec.getID());

        DateTimeZone zoneMillis = DateTimeZone.forOffsetMillis(3661001);
        Assert.assertEquals("+01:01:01.001", zoneMillis.getID());

        DateTimeZone zoneNegMillis = DateTimeZone.forOffsetMillis(-3661001);
        Assert.assertEquals("-01:01:01.001", zoneNegMillis.getID());

        int maxMillis = (86400 * 1000) - 1;
        DateTimeZone maxZone = DateTimeZone.forOffsetMillis(maxMillis);
        Assert.assertEquals("+23:59:59.999", maxZone.getID());

        DateTimeZone minZone = DateTimeZone.forOffsetMillis(-maxMillis);
        Assert.assertEquals("-23:59:59.999", minZone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_TooLarge() {
        DateTimeZone.forOffsetMillis(86400 * 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_TooSmall() {
        DateTimeZone.forOffsetMillis(-(86400 * 1000));
    }

    @Test
    public void testForTimeZone() {
        Assert.assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));

        DateTimeZone convertedEst = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        Assert.assertEquals("America/New_York", convertedEst.getID());

        DateTimeZone convertedPst = DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST"));
        Assert.assertEquals("America/Los_Angeles", convertedPst.getID());

        DateTimeZone convertedGmt = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT"));
        Assert.assertSame(DateTimeZone.UTC, convertedGmt);

        DateTimeZone gmtPlus = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+02:00"));
        Assert.assertEquals("+02:00", gmtPlus.getID());

        DateTimeZone gmtMinus = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT-05:00"));
        Assert.assertEquals("-05:00", gmtMinus.getID());

        DateTimeZone gmtZero = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        Assert.assertSame(DateTimeZone.UTC, gmtZero);

        DateTimeZone tokyo = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
        Assert.assertEquals("Asia/Tokyo", tokyo.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTimeZone_Unrecognised() {
        TimeZone custom = new SimpleTimeZone(0, "CustomUnknownZoneId12345");
        DateTimeZone.forTimeZone(custom);
    }

    @Test
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains("UTC"));
        Assert.assertTrue(ids.contains("America/New_York"));
        Assert.assertTrue(ids.contains("Europe/London"));
    }

    @Test
    public void testProviderManagement() {
        Provider current = DateTimeZone.getProvider();
        Assert.assertNotNull(current);

        DateTimeZone.setProvider(null);
        Assert.assertNotNull(DateTimeZone.getProvider());

        DateTimeZone.setProvider(new UTCProvider());
        Assert.assertTrue(DateTimeZone.getProvider() instanceof UTCProvider);
        Assert.assertEquals(1, DateTimeZone.getAvailableIDs().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_EmptyIDs() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() { return Collections.emptySet(); }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_NoUTC() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() {
                Set<String> s = new HashSet<String>();
                s.add("America/New_York");
                return s;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_InvalidUTCZone() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                if ("UTC".equals(id)) {
                    return DateTimeZone.forOffsetHours(1);
                }
                return null;
            }
            public Set<String> getAvailableIDs() {
                Set<String> s = new HashSet<String>();
                s.add("UTC");
                return s;
            }
        });
    }

    @Test
    public void testNameProviderManagement() {
        NameProvider current = DateTimeZone.getNameProvider();
        Assert.assertNotNull(current);

        DateTimeZone.setNameProvider(null);
        Assert.assertNotNull(DateTimeZone.getNameProvider());
        Assert.assertTrue(DateTimeZone.getNameProvider() instanceof DefaultNameProvider);
    }

    @Test
    public void testNames() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2021, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2021, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        Assert.assertNotNull(london.getNameKey(winterInstant));
        Assert.assertNotNull(london.getShortName(winterInstant));
        Assert.assertNotNull(london.getShortName(summerInstant, Locale.UK));
        Assert.assertNotNull(london.getName(winterInstant));
        Assert.assertNotNull(london.getName(summerInstant, Locale.UK));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        Assert.assertEquals("+03:00", fixed.getShortName(0L, Locale.ENGLISH));
        Assert.assertEquals("+03:00", fixed.getName(0L, Locale.ENGLISH));

        DateTimeZone mockZone = new DateTimeZone("MockZone") {
            public String getNameKey(long instant) { return null; }
            public int getOffset(long instant) { return 0; }
            public int getStandardOffset(long instant) { return 0; }
            public boolean isFixed() { return true; }
            public long nextTransition(long instant) { return instant; }
            public long previousTransition(long instant) { return instant; }
            public boolean equals(Object object) { return object == this; }
        };
        Assert.assertEquals("MockZone", mockZone.getName(0L));
        Assert.assertEquals("MockZone", mockZone.getShortName(0L));
    }

    @Test
    public void testGetOffsetAndStandardOffset() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long winter = new DateTime(2021, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        long summer = new DateTime(2021, 7, 1, 12, 0, DateTimeZone.UTC).getMillis();

        Assert.assertEquals(0, london.getOffset(winter));
        Assert.assertEquals(3600000, london.getOffset(summer));

        Assert.assertEquals(0, london.getStandardOffset(winter));
        Assert.assertEquals(0, london.getStandardOffset(summer));

        Assert.assertTrue(london.isStandardOffset(winter));
        Assert.assertFalse(london.isStandardOffset(summer));

        ReadableInstant instant = new Instant(winter);
        Assert.assertEquals(0, london.getOffset(instant));
        Assert.assertEquals(london.getOffset(DateTimeUtils.currentTimeMillis()), london.getOffset((ReadableInstant) null));
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long normalWinter = new DateTime(2021, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(0, london.getOffsetFromLocal(normalWinter));

        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long winterNY = new DateTime(2021, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(-5 * 3600000, ny.getOffsetFromLocal(winterNY));

        // Gap in New York: Spring transition forward 02:00 -> 03:00
        DateTime gapLocal = new DateTime(2021, 3, 14, 2, 30, DateTimeZone.UTC);
        int gapOffset = ny.getOffsetFromLocal(gapLocal.getMillis());
        Assert.assertTrue(gapOffset == -5 * 3600000 || gapOffset == -4 * 3600000);

        // Overlap in New York: Autumn transition back 02:00 -> 01:00
        DateTime overlapLocal = new DateTime(2021, 11, 7, 1, 30, DateTimeZone.UTC);
        int overlapOffset = ny.getOffsetFromLocal(overlapLocal.getMillis());
        Assert.assertEquals(-4 * 3600000, overlapOffset);
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long utc = new DateTime(2021, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        long local = ny.convertUTCToLocal(utc);
        Assert.assertEquals(utc - 5 * 3600000, local);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_Overflow() {
        DateTimeZone plusTwo = DateTimeZone.forOffsetHours(2);
        plusTwo.convertUTCToLocal(Long.MAX_VALUE - 100);
    }

    @Test
    public void testConvertLocalToUTC_StrictAndLenient() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long winterLocal = new DateTime(2021, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(winterLocal, london.convertLocalToUTC(winterLocal, false));
        Assert.assertEquals(winterLocal, london.convertLocalToUTC(winterLocal, true));

        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long localNY = new DateTime(2021, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        long utcExpected = localNY + 5 * 3600000;
        Assert.assertEquals(utcExpected, ny.convertLocalToUTC(localNY, false));
        Assert.assertEquals(utcExpected, ny.convertLocalToUTC(localNY, true));

        // Test with originalInstantUTC overload
        long converted = ny.convertLocalToUTC(localNY, false, utcExpected);
        Assert.assertEquals(utcExpected, converted);
    }

    @Test(expected = IllegalInstantException.class)
    public void testConvertLocalToUTC_GapStrictThrows() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long gapInstantLocal = new DateTime(2021, 3, 14, 2, 30, DateTimeZone.UTC).getMillis();
        ny.convertLocalToUTC(gapInstantLocal, true);
    }

    @Test
    public void testConvertLocalToUTC_GapLenient() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long gapInstantLocal = new DateTime(2021, 3, 14, 2, 30, DateTimeZone.UTC).getMillis();
        long converted = ny.convertLocalToUTC(gapInstantLocal, false);
        Assert.assertTrue(converted > 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTC_Overflow() {
        DateTimeZone plusTwo = DateTimeZone.forOffsetHours(2);
        plusTwo.convertLocalToUTC(Long.MIN_VALUE + 100, false);
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        long instant = new DateTime(2021, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();

        Assert.assertEquals(instant, london.getMillisKeepLocal(london, instant));

        long parisInstant = london.getMillisKeepLocal(paris, instant);
        Assert.assertEquals(instant - 3600000, parisInstant);

        DateTimeZone.setDefault(paris);
        Assert.assertEquals(parisInstant, london.getMillisKeepLocal(null, instant));
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        LocalDateTime ldt = new LocalDateTime(2021, 3, 14, 2, 30);
        Assert.assertFalse(fixed.isLocalDateTimeGap(ldt));

        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        LocalDateTime gapLdt = new LocalDateTime(2021, 3, 14, 2, 30);
        Assert.assertTrue(ny.isLocalDateTimeGap(gapLdt));

        LocalDateTime nonGapLdt = new LocalDateTime(2021, 3, 14, 4, 30);
        Assert.assertFalse(ny.isLocalDateTimeGap(nonGapLdt));
    }

    @Test
    public void testAdjustOffset() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Overlap occurred on 2021-11-07 from 01:00 to 02:00 EDT/EST
        DateTime overlapEarlier = new DateTime(2021, 11, 7, 1, 30, DateTimeZone.forOffsetHours(-4));
        long earlierMillis = overlapEarlier.getMillis();
        long laterMillis = earlierMillis + 3600000;

        Assert.assertEquals(laterMillis, ny.adjustOffset(earlierMillis, true));
        Assert.assertEquals(earlierMillis, ny.adjustOffset(earlierMillis, false));
        Assert.assertEquals(laterMillis, ny.adjustOffset(laterMillis, true));
        Assert.assertEquals(earlierMillis, ny.adjustOffset(laterMillis, false));

        long normalMillis = new DateTime(2021, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(normalMillis, ny.adjustOffset(normalMillis, true));
        Assert.assertEquals(normalMillis, ny.adjustOffset(normalMillis, false));
    }

    @Test
    public void testIsFixedAndTransitions() {
        DateTimeZone utc = DateTimeZone.UTC;
        Assert.assertTrue(utc.isFixed());
        Assert.assertEquals(100L, utc.nextTransition(100L));
        Assert.assertEquals(100L, utc.previousTransition(100L));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        Assert.assertTrue(fixed.isFixed());
        Assert.assertEquals(1000L, fixed.nextTransition(1000L));
        Assert.assertEquals(1000L, fixed.previousTransition(1000L));

        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        Assert.assertFalse(ny.isFixed());
        long instant = new DateTime(2021, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long next = ny.nextTransition(instant);
        long prev = ny.previousTransition(instant);
        Assert.assertTrue(next > instant);
        Assert.assertTrue(prev < instant);
    }

    @Test
    public void testToTimeZone() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        TimeZone tz = ny.toTimeZone();
        Assert.assertNotNull(tz);
        Assert.assertEquals("America/New_York", tz.getID());

        DateTimeZone utc = DateTimeZone.UTC;
        Assert.assertEquals("UTC", utc.toTimeZone().getID());
    }

    @Test
    public void testEqualsHashCodeToString() {
        DateTimeZone ny1 = DateTimeZone.forID("America/New_York");
        DateTimeZone ny2 = DateTimeZone.forID("America/New_York");
        DateTimeZone london = DateTimeZone.forID("Europe/London");

        Assert.assertEquals(ny1, ny2);
        Assert.assertNotEquals(ny1, london);
        Assert.assertNotEquals(ny1, null);
        Assert.assertNotEquals(ny1, "America/New_York");

        Assert.assertEquals(ny1.hashCode(), ny2.hashCode());
        Assert.assertEquals("America/New_York", ny1.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        DateTimeZone original = DateTimeZone.forID("America/Chicago");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        Assert.assertEquals(original, deserialized);
        Assert.assertEquals(original.getID(), deserialized.getID());
    }

    @Test
    public void testConstructor_NullID() {
        try {
            new DateTimeZone(null) {
                public String getNameKey(long instant) { return null; }
                public int getOffset(long instant) { return 0; }
                public int getStandardOffset(long instant) { return 0; }
                public boolean isFixed() { return true; }
                public long nextTransition(long instant) { return instant; }
                public long previousTransition(long instant) { return instant; }
                public boolean equals(Object object) { return false; }
            };
            Assert.fail("Expected IllegalArgumentException for null ID");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Id must not be null", ex.getMessage());
        }
    }

    @Test
    public void testConvertedIdsMap() {
        String[] oldIds = new String[] {
            "WET", "CET", "MET", "ECT", "EET", "MIT", "HST", "AST", "PST",
            "MST", "PNT", "CST", "EST", "IET", "PRT", "CNT", "AGT", "BET",
            "ART", "CAT", "EAT", "NET", "PLT", "IST", "BST", "VST", "CTT",
            "JST", "ACT", "AET", "SST", "NST"
        };
        for (String oldId : oldIds) {
            TimeZone tz = TimeZone.getTimeZone(oldId);
            DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
            Assert.assertNotNull("Failed conversion for: " + oldId, dtz);
        }
    }
}
