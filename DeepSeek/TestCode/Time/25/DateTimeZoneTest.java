package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import org.joda.time.tz.FixedDateTimeZone;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;
import org.joda.time.tz.ZoneInfoProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DateTimeZoneTest {

    private static DateTimeZone originalDefault;
    private static Provider originalProvider;
    private static String originalUserTimezone;

    private DateTimeZone london;
    private DateTimeZone newYork;
    private DateTimeZone fixedPlusOne;

    @BeforeClass
    public static void setUpClass() {
        originalDefault = DateTimeZone.getDefault();
        originalProvider = DateTimeZone.getProvider();
        originalUserTimezone = System.getProperty("user.timezone");
    }

    @AfterClass
    public static void tearDownClass() {
        DateTimeZone.setDefault(originalDefault);
        DateTimeZone.setProvider(originalProvider);
        if (originalUserTimezone != null) {
            System.setProperty("user.timezone", originalUserTimezone);
        } else {
            System.clearProperty("user.timezone");
        }
    }

    @Before
    public void setUp() {
        london = DateTimeZone.forID("Europe/London");
        newYork = DateTimeZone.forID("America/New_York");
        fixedPlusOne = DateTimeZone.forOffsetHours(1);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefault);
        DateTimeZone.setProvider(originalProvider);
    }

    @Test
    public void testGetDefault() {
        DateTimeZone zone = DateTimeZone.getDefault();
        Assert.assertNotNull(zone);
        DateTimeZone zone2 = DateTimeZone.getDefault();
        Assert.assertSame(zone, zone2);
    }

    @Test
    public void testGetDefaultWithUserTimezoneProperty() {
        System.setProperty("user.timezone", "Europe/London");
        try {
            DateTimeZone zone = DateTimeZone.getDefault();
            Assert.assertEquals("Europe/London", zone.getID());
        } finally {
            System.clearProperty("user.timezone");
        }
    }

    @Test
    public void testGetDefaultWithInvalidUserTimezoneProperty() {
        System.setProperty("user.timezone", "Invalid/Zone");
        try {
            DateTimeZone zone = DateTimeZone.getDefault();
            Assert.assertNotNull(zone);
        } finally {
            System.clearProperty("user.timezone");
        }
    }

    @Test
    public void testSetDefault() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        Assert.assertEquals(DateTimeZone.UTC, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultNull() {
        DateTimeZone.setDefault(null);
    }

    @Test
    public void testForIDNull() {
        DateTimeZone zone = DateTimeZone.forID(null);
        Assert.assertEquals(DateTimeZone.getDefault(), zone);
    }

    @Test
    public void testForIDUTC() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test
    public void testForIDValid() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        Assert.assertNotNull(zone);
        Assert.assertEquals("Europe/London", zone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForIDInvalid() {
        DateTimeZone.forID("Invalid/Zone");
    }

    @Test
    public void testForIDOffsetPositive() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        Assert.assertNotNull(zone);
        Assert.assertEquals(5 * 3600000 + 30 * 60000, zone.getOffset(0));
    }

    @Test
    public void testForIDOffsetNegative() {
        DateTimeZone zone = DateTimeZone.forID("-05:30");
        Assert.assertNotNull(zone);
        Assert.assertEquals(-(5 * 3600000 + 30 * 60000), zone.getOffset(0));
    }

    @Test
    public void testForIDOffsetZero() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));
    }

    @Test
    public void testForIDOffsetWithSeconds() {
        DateTimeZone zone = DateTimeZone.forID("+01:02:03");
        Assert.assertNotNull(zone);
        int expected = 1 * 3600000 + 2 * 60000 + 3 * 1000;
        Assert.assertEquals(expected, zone.getOffset(0));
    }

    @Test
    public void testForIDOffsetWithMillis() {
        DateTimeZone zone = DateTimeZone.forID("+01:02:03.004");
        Assert.assertNotNull(zone);
        int expected = 1 * 3600000 + 2 * 60000 + 3 * 1000 + 4;
        Assert.assertEquals(expected, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursZero() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
    }

    @Test
    public void testForOffsetHoursPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        Assert.assertEquals(5 * 3600000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-5);
        Assert.assertEquals(-5 * 3600000, zone.getOffset(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursOverflow() {
        DateTimeZone.forOffsetHours(Integer.MAX_VALUE);
    }

    @Test
    public void testForOffsetHoursMinutesZero() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
    }

    @Test
    public void testForOffsetHoursMinutesPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        Assert.assertEquals(5 * 3600000 + 30 * 60000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursMinutesNegativeHoursPositiveMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        Assert.assertEquals(-(2 * 3600000) + 30 * 60000, zone.getOffset(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesInvalidMinutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesInvalidMinutesTooLarge() {
        DateTimeZone.forOffsetHoursMinutes(0, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesOverflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    @Test
    public void testForOffsetMillisZero() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
    }

    @Test
    public void testForOffsetMillisPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        Assert.assertEquals(3600000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetMillisNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-3600000);
        Assert.assertEquals(-3600000, zone.getOffset(0));
    }

    @Test
    public void testForTimeZoneNull() {
        Assert.assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
    }

    @Test
    public void testForTimeZoneUTC() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
    }

    @Test
    public void testForTimeZoneValid() {
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("America/New_York"));
        Assert.assertEquals("America/New_York", zone.getID());
    }

    @Test
    public void testForTimeZoneShortID() {
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        Assert.assertEquals("America/New_York", zone.getID());
    }

    @Test
    public void testForTimeZoneGMTPlusFormat() {
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT+05:30"));
        Assert.assertNotNull(zone);
        Assert.assertEquals(5 * 3600000 + 30 * 60000, zone.getOffset(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTimeZoneInvalid() {
        DateTimeZone.forTimeZone(TimeZone.getTimeZone("Invalid/Zone"));
    }

    @Test
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains("UTC"));
    }

    @Test
    public void testGetProvider() {
        Assert.assertNotNull(DateTimeZone.getProvider());
    }

    @Test
    public void testSetProvider() {
        Provider custom = new UTCProvider();
        DateTimeZone.setProvider(custom);
        Assert.assertSame(custom, DateTimeZone.getProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProviderNullIDs() {
        Provider bad = new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() { return null; }
        };
        DateTimeZone.setProvider(bad);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProviderNoUTC() {
        Provider bad = new Provider() {
            public DateTimeZone getZone(String id) { return null; }
            public Set<String> getAvailableIDs() {
                Set<String> set = new java.util.HashSet<String>();
                set.add("Europe/London");
                return set;
            }
        };
        DateTimeZone.setProvider(bad);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProviderInvalidUTC() {
        Provider bad = new Provider() {
            public DateTimeZone getZone(String id) {
                if ("UTC".equals(id)) return DateTimeZone.forOffsetHours(1);
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

    @Test
    public void testGetNameProvider() {
        Assert.assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testSetNameProvider() {
        DateTimeZone.setNameProvider(null);
        Assert.assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testGetID() {
        Assert.assertEquals("UTC", DateTimeZone.UTC.getID());
        Assert.assertEquals("Europe/London", london.getID());
    }

    @Test
    public void testGetShortNameUTC() {
        Assert.assertEquals("UTC", DateTimeZone.UTC.getShortName(0));
    }

    @Test
    public void testGetShortNameLondon() {
        String name = london.getShortName(0, Locale.ENGLISH);
        Assert.assertNotNull(name);
    }

    @Test
    public void testGetShortNameNullLocale() {
        String name = london.getShortName(0);
        Assert.assertNotNull(name);
    }

    @Test
    public void testGetNameUTC() {
        Assert.assertEquals("UTC", DateTimeZone.UTC.getName(0));
    }

    @Test
    public void testGetNameLondon() {
        String name = london.getName(0, Locale.ENGLISH);
        Assert.assertNotNull(name);
    }

    @Test
    public void testGetNameNullLocale() {
        String name = london.getName(0);
        Assert.assertNotNull(name);
    }

    @Test
    public void testGetOffsetUTC() {
        Assert.assertEquals(0, DateTimeZone.UTC.getOffset(0));
    }

    @Test
    public void testGetOffsetLondon() {
        int offset = london.getOffset(0);
        Assert.assertEquals(0, offset);
    }

    @Test
    public void testGetOffsetReadableInstantNull() {
        Assert.assertEquals(DateTimeZone.UTC.getOffset(System.currentTimeMillis()), DateTimeZone.UTC.getOffset((ReadableInstant) null));
    }

    @Test
    public void testGetOffsetReadableInstant() {
        Instant instant = new Instant(0);
        Assert.assertEquals(0, DateTimeZone.UTC.getOffset(instant));
    }

    @Test
    public void testGetStandardOffsetUTC() {
        Assert.assertEquals(0, DateTimeZone.UTC.getStandardOffset(0));
    }

    @Test
    public void testIsStandardOffsetUTC() {
        Assert.assertTrue(DateTimeZone.UTC.isStandardOffset(0));
    }

    @Test
    public void testIsStandardOffsetLondon() {
        Assert.assertTrue(london.isStandardOffset(0));
        long summer = new DateTime(2023, 7, 1, 0, 0, london).getMillis();
        Assert.assertFalse(london.isStandardOffset(summer));
    }

    @Test
    public void testGetOffsetFromLocalUTC() {
        Assert.assertEquals(0, DateTimeZone.UTC.getOffsetFromLocal(0));
    }

    @Test
    public void testGetOffsetFromLocalLondonGap() {
        long localMillis = new DateTime(2023, 3, 26, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        int offset = london.getOffsetFromLocal(localMillis);
        Assert.assertEquals(3600000, offset);
    }

    @Test
    public void testConvertUTCToLocalUTC() {
        Assert.assertEquals(0, DateTimeZone.UTC.convertUTCToLocal(0));
    }

    @Test
    public void testConvertUTCToLocalLondon() {
        long utc = 0;
        long local = london.convertUTCToLocal(utc);
        Assert.assertEquals(utc + london.getOffset(utc), local);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocalOverflow() {
        DateTimeZone.UTC.convertUTCToLocal(Long.MAX_VALUE);
    }

    @Test
    public void testConvertLocalToUTCUTC() {
        Assert.assertEquals(0, DateTimeZone.UTC.convertLocalToUTC(0, false));
    }

    @Test
    public void testConvertLocalToUTCLondon() {
        long local = new DateTime(2023, 1, 1, 0, 0, london).getMillis();
        long utc = london.convertLocalToUTC(local, false);
        Assert.assertEquals(local - london.getOffset(local), utc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTCStrictGap() {
        long localMillis = new DateTime(2023, 3, 26, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        london.convertLocalToUTC(localMillis, true);
    }

    @Test
    public void testConvertLocalToUTCStrictNoGap() {
        long localMillis = new DateTime(2023, 3, 26, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();
        long utc = london.convertLocalToUTC(localMillis, true);
        Assert.assertNotNull(utc);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTCOverflow() {
        DateTimeZone.UTC.convertLocalToUTC(Long.MIN_VALUE, false);
    }

    @Test
    public void testConvertLocalToUTCOverloadWithOriginal() {
        long originalUTC = 0;
        long local = london.convertUTCToLocal(originalUTC);
        long utc = london.convertLocalToUTC(local, false, originalUTC);
        Assert.assertEquals(originalUTC, utc);
    }

    @Test
    public void testGetMillisKeepLocalSameZone() {
        Assert.assertEquals(0, DateTimeZone.UTC.getMillisKeepLocal(DateTimeZone.UTC, 0));
    }

    @Test
    public void testGetMillisKeepLocalDifferentZone() {
        long utc = 0;
        long result = london.getMillisKeepLocal(newYork, utc);
        Assert.assertNotNull(result);
    }

    @Test
    public void testIsLocalDateTimeGapFixed() {
        LocalDateTime ldt = new LocalDateTime(2023, 3, 26, 1, 30);
        Assert.assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(ldt));
    }

    @Test
    public void testIsLocalDateTimeGapLondon() {
        LocalDateTime ldt = new LocalDateTime(2023, 3, 26, 1, 30);
        Assert.assertTrue(london.isLocalDateTimeGap(ldt));
    }

    @Test
    public void testIsLocalDateTimeGapLondonNormal() {
        LocalDateTime ldt = new LocalDateTime(2023, 3, 26, 2, 30);
        Assert.assertFalse(london.isLocalDateTimeGap(ldt));
    }

    @Test
    public void testIsFixedUTC() {
        Assert.assertTrue(DateTimeZone.UTC.isFixed());
    }

    @Test
    public void testIsFixedLondon() {
        Assert.assertFalse(london.isFixed());
    }

    @Test
    public void testNextTransitionUTC() {
        Assert.assertEquals(0, DateTimeZone.UTC.nextTransition(0));
    }

    @Test
    public void testNextTransitionLondon() {
        long next = london.nextTransition(0);
        Assert.assertTrue(next > 0);
    }

    @Test
    public void testPreviousTransitionUTC() {
        Assert.assertEquals(0, DateTimeZone.UTC.previousTransition(0));
    }

    @Test
    public void testPreviousTransitionLondon() {
        long prev = london.previousTransition(Long.MAX_VALUE);
        Assert.assertTrue(prev < Long.MAX_VALUE);
    }

    @Test
    public void testToTimeZone() {
        TimeZone tz = DateTimeZone.UTC.toTimeZone();
        Assert.assertEquals("UTC", tz.getID());
    }

    @Test
    public void testEquals() {
        Assert.assertTrue(DateTimeZone.UTC.equals(DateTimeZone.UTC));
        Assert.assertFalse(DateTimeZone.UTC.equals(london));
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(DateTimeZone.UTC.hashCode(), DateTimeZone.UTC.hashCode());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("UTC", DateTimeZone.UTC.toString());
    }

    @Test
    public void testWriteReplace() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(DateTimeZone.UTC);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();
        Assert.assertEquals(DateTimeZone.UTC, obj);
    }
}
