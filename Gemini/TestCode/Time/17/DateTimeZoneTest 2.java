package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    private DateTimeZone originalDefault;
    private Provider originalProvider;
    private NameProvider originalNameProvider;
    private Locale originalLocale;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
        originalProvider = DateTimeZone.getProvider();
        originalNameProvider = DateTimeZone.getNameProvider();
        originalLocale = Locale.getDefault();
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(originalDefault);
        DateTimeZone.setProvider(originalProvider);
        DateTimeZone.setNameProvider(originalNameProvider);
        Locale.setDefault(originalLocale);
    }

    @Test
    public void testGetDefault() {
        DateTimeZone def = DateTimeZone.getDefault();
        Assert.assertNotNull(def);
        Assert.assertEquals(def, DateTimeZone.getDefault());
    }

    @Test
    public void testSetDefault() {
        DateTimeZone previous = DateTimeZone.getDefault();
        DateTimeZone toSet = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(toSet);
        Assert.assertEquals(toSet, DateTimeZone.getDefault());

        DateTimeZone.setDefault(previous);
        Assert.assertEquals(previous, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultNullThrows() {
        DateTimeZone.setDefault(null);
    }

    @Test
    public void testForID_null() {
        Assert.assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
    }

    @Test
    public void testForID_UTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        Assert.assertSame(DateTimeZone.UTC, zone);
        Assert.assertEquals("UTC", zone.getID());
    }

    @Test
    public void testForID_ValidKnownZones() {
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");
        Assert.assertEquals("Europe/Paris", zoneParis.getID());

        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");
        Assert.assertEquals("Europe/London", zoneLondon.getID());

        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        Assert.assertEquals("America/New_York", zoneNY.getID());
    }

    @Test
    public void testForID_OffsetStrings() {
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
    public void testForID_InvalidString() {
        DateTimeZone.forID("Invalid/NonExistentZone");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidOffsetString() {
        DateTimeZone.forID("+25:00");
    }

    @Test
    public void testForOffsetHours() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        Assert.assertEquals("+03:00", zone.getID());
        Assert.assertEquals(3 * 3600000, zone.getOffset(0L));

        DateTimeZone zoneNeg = DateTimeZone.forOffsetHours(-8);
        Assert.assertEquals("-08:00", zoneNeg.getID());
        Assert.assertEquals(-8 * 3600000, zoneNeg.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_TooLarge() {
        DateTimeZone.forOffsetHours(1000000);
    }

    @Test
    public void testForOffsetHoursMinutes() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone zonePos = DateTimeZone.forOffsetHoursMinutes(5, 30);
        Assert.assertEquals("+05:30", zonePos.getID());
        Assert.assertEquals(19800000, zonePos.getOffset(0L));

        DateTimeZone zoneNeg = DateTimeZone.forOffsetHoursMinutes(-5, 30);
        Assert.assertEquals("-05:30", zoneNeg.getID());
        Assert.assertEquals(-19800000, zoneNeg.getOffset(0L));

        DateTimeZone zoneZeroHourNegMinute = DateTimeZone.forOffsetHoursMinutes(0, 45);
        Assert.assertEquals("+00:45", zoneZeroHourNegMinute.getID());
        Assert.assertEquals(45 * 60000, zoneZeroHourNegMinute.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_NegativeMinutes() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_MinutesTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_Overflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 30);
    }

    @Test
    public void testForOffsetMillis() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        DateTimeZone zonePos = DateTimeZone.forOffsetMillis(3600000);
        Assert.assertEquals("+01:00", zonePos.getID());
        Assert.assertEquals(3600000, zonePos.getOffset(0L));

        DateTimeZone zoneNeg = DateTimeZone.forOffsetMillis(-3600000);
        Assert.assertEquals("-01:00", zoneNeg.getID());
        Assert.assertEquals(-3600000, zoneNeg.getOffset(0L));

        // Milliseconds with seconds
        DateTimeZone zoneWithSec = DateTimeZone.forOffsetMillis(3661000);
        Assert.assertEquals("+01:01:01", zoneWithSec.getID());

        // Milliseconds with subseconds
        DateTimeZone zoneWithMillis = DateTimeZone.forOffsetMillis(3661001);
        Assert.assertEquals("+01:01:01.001", zoneWithMillis.getID());

        DateTimeZone zoneNegWithMillis = DateTimeZone.forOffsetMillis(-3661001);
        Assert.assertEquals("-01:01:01.001", zoneNegWithMillis.getID());

        // Cache hit test
        DateTimeZone zoneCached = DateTimeZone.forOffsetMillis(3661001);
        Assert.assertSame(zoneWithMillis, zoneCached);
    }

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
    public void testForTimeZone_StandardJDKConversions() {
        String[] oldIds = {
            "GMT", "WET", "CET", "MET", "ECT", "EET", "MIT", "HST", "AST", "PST",
            "MST", "PNT", "CST", "EST", "IET", "PRT", "CNT", "AGT", "BET", "ART",
            "CAT", "EAT", "NET", "PLT", "IST", "BST", "VST", "CTT", "JST", "ACT",
            "AET", "SST", "NST"
        };
        for (String id : oldIds) {
            TimeZone tz = TimeZone.getTimeZone(id);
            DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
            Assert.assertNotNull(dtz);
        }
    }

    @Test
    public void testForTimeZone_DirectIdMatch() {
        TimeZone tz = TimeZone.getTimeZone("Europe/London");
        DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
        Assert.assertEquals("Europe/London", dtz.getID());
    }

    @Test
    public void testForTimeZone_CustomGMT() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
        Assert.assertEquals("+02:00", dtz.getID());

        TimeZone tzNeg = TimeZone.getTimeZone("GMT-05:00");
        DateTimeZone dtzNeg = DateTimeZone.forTimeZone(tzNeg);
        Assert.assertEquals("-05:00", dtzNeg.getID());

        TimeZone tzZero = TimeZone.getTimeZone("GMT+00:00");
        DateTimeZone dtzZero = DateTimeZone.forTimeZone(tzZero);
        Assert.assertSame(DateTimeZone.UTC, dtzZero);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTimeZone_Unrecognised() {
        TimeZone tz = new TimeZone() {
            private static final long serialVersionUID = 1L;
            @Override
            public int getOffset(int era, int year, int month, int day, int dayOfWeek, int milliseconds) {
                return 0;
            }
            @Override
            public void setRawOffset(int offsetMillis) {}
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
                return "UnknownZoneID_XYZ";
            }
            @Override
            public String getDisplayName() {
                return "UnknownZoneName_XYZ";
            }
        };
        DateTimeZone.forTimeZone(tz);
    }

    @Test
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains("UTC"));
        Assert.assertTrue(ids.contains("Europe/London"));
        Assert.assertTrue(ids.contains("America/New_York"));
    }

    @Test
    public void testSetProvider() {
        Provider current = DateTimeZone.getProvider();
        Assert.assertNotNull(current);

        UTCProvider utcProvider = new UTCProvider();
        DateTimeZone.setProvider(utcProvider);
        Assert.assertSame(utcProvider, DateTimeZone.getProvider());
        Assert.assertEquals(1, DateTimeZone.getAvailableIDs().size());
        Assert.assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));

        // Setting null resets to default provider
        DateTimeZone.setProvider(null);
        Assert.assertNotNull(DateTimeZone.getProvider());
        Assert.assertTrue(DateTimeZone.getAvailableIDs().size() > 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_EmptyIDs() {
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
    public void testSetProvider_NullIDs() {
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
    public void testSetProvider_NoUTC() {
        DateTimeZone.setProvider(new Provider() {
            public DateTimeZone getZone(String id) {
                return null;
            }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("America/New_York");
                return set;
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_InvalidUTCZone() {
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
    public void testSetNameProvider() {
        NameProvider current = DateTimeZone.getNameProvider();
        Assert.assertNotNull(current);

        NameProvider custom = new DefaultNameProvider();
        DateTimeZone.setNameProvider(custom);
        Assert.assertSame(custom, DateTimeZone.getNameProvider());

        DateTimeZone.setNameProvider(null);
        Assert.assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testNames() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winter = new DateTime(2010, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summer = new DateTime(2010, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        Assert.assertNotNull(zone.getName(winter));
        Assert.assertNotNull(zone.getName(winter, Locale.UK));
        Assert.assertNotNull(zone.getName(winter, null));

        Assert.assertNotNull(zone.getShortName(winter));
        Assert.assertNotNull(zone.getShortName(winter, Locale.UK));
        Assert.assertNotNull(zone.getShortName(winter, null));

        Assert.assertNotNull(zone.getName(summer));
        Assert.assertNotNull(zone.getShortName(summer));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals("+02:00", fixed.getName(winter));
        Assert.assertEquals("+02:00", fixed.getShortName(winter));

        // Subclass returning null nameKey
        DateTimeZone nullKeyZone = new MockDateTimeZone("MockNullKey", 0, 0, null);
        Assert.assertEquals("MockNullKey", nullKeyZone.getName(0L));
        Assert.assertEquals("MockNullKey", nullKeyZone.getShortName(0L));
    }

    @Test
    public void testNameProviderFallbackToPrintOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        NameProvider dummyProvider = new NameProvider() {
            public String getShortName(Locale locale, String id, String nameKey) {
                return null;
            }
            public String getName(Locale locale, String id, String nameKey) {
                return null;
            }
        };
        DateTimeZone.setNameProvider(dummyProvider);

        long winter = new DateTime(2010, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals("+00:00", zone.getName(winter));
        Assert.assertEquals("+00:00", zone.getShortName(winter));
    }

    @Test
    public void testGetOffsetAndStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long winter = new DateTime(2010, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long summer = new DateTime(2010, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();

        Assert.assertEquals(3600000, zone.getOffset(winter));
        Assert.assertEquals(7200000, zone.getOffset(summer));
        Assert.assertEquals(3600000, zone.getStandardOffset(winter));
        Assert.assertEquals(3600000, zone.getStandardOffset(summer));

        Assert.assertTrue(zone.isStandardOffset(winter));
        Assert.assertFalse(zone.isStandardOffset(summer));

        Assert.assertEquals(zone.getOffset(winter), zone.getOffset(new Instant(winter)));
        Assert.assertEquals(zone.getOffset(DateTimeUtils.currentTimeMillis()), zone.getOffset((ReadableInstant) null));
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        // Winter
        long winter = new DateTime(2010, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(0, zone.getOffsetFromLocal(winter));

        // Summer
        long summer = new DateTime(2010, 7, 1, 0, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(3600000, zone.getOffsetFromLocal(summer));

        // Fixed zone
        DateTimeZone fixed = DateTimeZone.forOffsetHours(-5);
        Assert.assertEquals(-18000000, fixed.getOffsetFromLocal(winter));
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals(2 * 3600000L, zone.convertUTCToLocal(0L));
        Assert.assertEquals(1000L + 2 * 3600000L, zone.convertUTCToLocal(1000L));
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_OverflowMax() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        zone.convertUTCToLocal(Long.MAX_VALUE);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_OverflowMin() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-2);
        zone.convertUTCToLocal(Long.MIN_VALUE);
    }

    @Test
    public void testConvertLocalToUTC_Basic() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals(0L, zone.convertLocalToUTC(2 * 3600000L, false));
        Assert.assertEquals(0L, zone.convertLocalToUTC(2 * 3600000L, true));
        Assert.assertEquals(0L, zone.convertLocalToUTC(2 * 3600000L, false, 0L));
    }

    @Test
    public void testConvertLocalToUTC_Transitions() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Spring forward gap 2010-03-14 02:00:00 -> 03:00:00
        // Local 02:30 does not exist
        LocalDateTime gapLocal = new LocalDateTime(2010, 3, 14, 2, 30, 0, 0);
        long gapMillis = gapLocal.toDateTime(DateTimeZone.UTC).getMillis();

        try {
            zone.convertLocalToUTC(gapMillis, true);
            Assert.fail("Expected IllegalArgumentException for strict DST gap");
        } catch (IllegalArgumentException ex) {
            // Expected
        }

        long convertedLenient = zone.convertLocalToUTC(gapMillis, false);
        Assert.assertTrue(convertedLenient > 0);

        // Fall back overlap 2010-11-07 01:00:00 occurs twice
        LocalDateTime overlapLocal = new LocalDateTime(2010, 11, 7, 1, 30, 0, 0);
        long overlapMillis = overlapLocal.toDateTime(DateTimeZone.UTC).getMillis();
        long convertedOverlap = zone.convertLocalToUTC(overlapMillis, false);
        Assert.assertTrue(convertedOverlap > 0);

        long originalInstant = overlapMillis + 4 * 3600000L;
        long convertedWithOriginal = zone.convertLocalToUTC(overlapMillis, false, originalInstant);
        Assert.assertTrue(convertedWithOriginal > 0);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTC_OverflowMax() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-2);
        zone.convertLocalToUTC(Long.MAX_VALUE, false);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTC_OverflowMin() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        zone.convertLocalToUTC(Long.MIN_VALUE, false);
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zoneLondon = DateTimeZone.forID("Europe/London");

        long instant = new DateTime(2010, 1, 1, 12, 0, 0, DateTimeZone.UTC).getMillis();
        long inLondon = zoneParis.getMillisKeepLocal(zoneLondon, instant);
        Assert.assertEquals(instant + 3600000L, inLondon);

        // Same zone
        Assert.assertEquals(instant, zoneParis.getMillisKeepLocal(zoneParis, instant));

        // Null zone (defaults to default)
        DateTimeZone.setDefault(zoneLondon);
        Assert.assertEquals(inLondon, zoneParis.getMillisKeepLocal(null, instant));
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone fixed = DateTimeZone.UTC;
        LocalDateTime ldt = new LocalDateTime(2010, 3, 14, 2, 30, 0, 0);
        Assert.assertFalse(fixed.isLocalDateTimeGap(ldt));

        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        Assert.assertTrue(zoneNY.isLocalDateTimeGap(ldt));
        Assert.assertFalse(zoneNY.isLocalDateTimeGap(new LocalDateTime(2010, 3, 14, 4, 30, 0, 0)));
    }

    @Test
    public void testAdjustOffset() {
        DateTimeZone zoneNY = DateTimeZone.forID("America/New_York");
        // No overlap
        long normal = new DateTime(2010, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(normal, zoneNY.adjustOffset(normal, true));
        Assert.assertEquals(normal, zoneNY.adjustOffset(normal, false));

        // Overlap: 2010-11-07 01:30:00 EDT vs EST
        // UTC 05:30:00 (EDT = UTC-4 -> 01:30) vs UTC 06:30:00 (EST = UTC-5 -> 01:30)
        long instantEarlier = new DateTime(2010, 11, 7, 5, 30, DateTimeZone.UTC).getMillis();
        long instantLater = new DateTime(2010, 11, 7, 6, 30, DateTimeZone.UTC).getMillis();

        Assert.assertEquals(instantEarlier, zoneNY.adjustOffset(instantEarlier, false));
        Assert.assertEquals(instantLater, zoneNY.adjustOffset(instantEarlier, true));
        Assert.assertEquals(instantEarlier, zoneNY.adjustOffset(instantLater, false));
        Assert.assertEquals(instantLater, zoneNY.adjustOffset(instantLater, true));
    }

    @Test
    public void testIsFixedAndTransitions() {
        DateTimeZone utc = DateTimeZone.UTC;
        Assert.assertTrue(utc.isFixed());
        Assert.assertEquals(0L, utc.nextTransition(0L));
        Assert.assertEquals(0L, utc.previousTransition(0L));

        DateTimeZone zoneParis = DateTimeZone.forID("Europe/Paris");
        Assert.assertFalse(zoneParis.isFixed());

        long winter = new DateTime(2010, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long next = zoneParis.nextTransition(winter);
        long prev = zoneParis.previousTransition(winter);
        Assert.assertTrue(next > winter);
        Assert.assertTrue(prev < winter);
    }

    @Test
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        TimeZone tz = zone.toTimeZone();
        Assert.assertNotNull(tz);
        Assert.assertEquals("Europe/Paris", tz.getID());

        DateTimeZone utc = DateTimeZone.UTC;
        Assert.assertEquals("UTC", utc.toTimeZone().getID());
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone z1 = DateTimeZone.forOffsetHours(2);
        DateTimeZone z2 = DateTimeZone.forOffsetHours(2);
        DateTimeZone z3 = DateTimeZone.forOffsetHours(3);

        Assert.assertEquals(z1, z2);
        Assert.assertEquals(z1.hashCode(), z2.hashCode());
        Assert.assertNotEquals(z1, z3);
        Assert.assertNotEquals(z1, null);
        Assert.assertNotEquals(z1, new Object());
    }

    @Test
    public void testToString() {
        Assert.assertEquals("UTC", DateTimeZone.UTC.toString());
        Assert.assertEquals("Europe/Paris", DateTimeZone.forID("Europe/Paris").toString());
        Assert.assertEquals("+03:00", DateTimeZone.forOffsetHours(3).toString());
    }

    @Test
    public void testSerialization() throws Exception {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");

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
    public void testSerializationFixedOffset() throws Exception {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 45);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(zone);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        Assert.assertEquals(zone, deserialized);
        Assert.assertEquals(zone.getID(), deserialized.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProtectedConstructorNullId() throws Throwable {
        Constructor<DateTimeZone> constructor = DateTimeZone.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        try {
            constructor.newInstance((String) null);
        } catch (InvocationTargetException ex) {
            throw ex.getCause();
        }
    }

    @Test
    public void testPrivateStubSerializationDirectly() throws Exception {
        Class<?> stubClass = Class.forName("org.joda.time.DateTimeZone$Stub");
        Constructor<?> stubConstructor = stubClass.getDeclaredConstructor(String.class);
        stubConstructor.setAccessible(true);
        Object stub = stubConstructor.newInstance("Europe/London");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(stub);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        Assert.assertTrue(deserialized instanceof DateTimeZone);
        Assert.assertEquals("Europe/London", ((DateTimeZone) deserialized).getID());
    }

    @Test
    public void testCoverageInternalMethodsViaReflection() throws Exception {
        Method printOffsetMethod = DateTimeZone.class.getDeclaredMethod("printOffset", int.class);
        printOffsetMethod.setAccessible(true);

        // Test printOffset variations
        Assert.assertEquals("+00:00", printOffsetMethod.invoke(null, 0));
        Assert.assertEquals("+01:00", printOffsetMethod.invoke(null, 3600000));
        Assert.assertEquals("-01:00", printOffsetMethod.invoke(null, -3600000));
        Assert.assertEquals("+01:02", printOffsetMethod.invoke(null, 3720000));
        Assert.assertEquals("+01:02:03", printOffsetMethod.invoke(null, 3723000));
        Assert.assertEquals("+01:02:03.004", printOffsetMethod.invoke(null, 3723004));
        Assert.assertEquals("-01:02:03.004", printOffsetMethod.invoke(null, -3723004));

        Method parseOffsetMethod = DateTimeZone.class.getDeclaredMethod("parseOffset", String.class);
        parseOffsetMethod.setAccessible(true);
        Assert.assertEquals(0, parseOffsetMethod.invoke(null, "+00:00"));
        Assert.assertEquals(3600000, parseOffsetMethod.invoke(null, "+01:00"));
        Assert.assertEquals(-18000000, parseOffsetMethod.invoke(null, "-05:00"));

        Method getConvertedIdMethod = DateTimeZone.class.getDeclaredMethod("getConvertedId", String.class);
        getConvertedIdMethod.setAccessible(true);
        Assert.assertEquals("UTC", getConvertedIdMethod.invoke(null, "GMT"));
        Assert.assertEquals("America/New_York", getConvertedIdMethod.invoke(null, "EST"));
        Assert.assertNull(getConvertedIdMethod.invoke(null, "NonExistentKeyId12345"));
    }

    private static class MockDateTimeZone extends DateTimeZone {
        private static final long serialVersionUID = 1L;
        private final int offset;
        private final int standardOffset;
        private final String nameKey;

        MockDateTimeZone(String id, int offset, int standardOffset, String nameKey) {
            super(id);
            this.offset = offset;
            this.standardOffset = standardOffset;
            this.nameKey = nameKey;
        }

        @Override
        public String getNameKey(long instant) {
            return nameKey;
        }

        @Override
        public int getOffset(long instant) {
            return offset;
        }

        @Override
        public int getStandardOffset(long instant) {
            return standardOffset;
        }

        @Override
        public boolean isFixed() {
            return true;
        }

        @Override
        public long nextTransition(long instant) {
            return instant;
        }

        @Override
        public long previousTransition(long instant) {
            return instant;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object instanceof MockDateTimeZone) {
                MockDateTimeZone other = (MockDateTimeZone) object;
                return offset == other.offset && standardOffset == other.standardOffset && getID().equals(other.getID());
            }
            return false;
        }
    }
}
