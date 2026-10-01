package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AllPermission;
import java.security.Permission;
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

    //-----------------------------------------------------------------------
    // Mock and helper classes
    //-----------------------------------------------------------------------
    private static class MockDateTimeZone extends DateTimeZone {
        private static final long serialVersionUID = 1L;
        private final int standardOffset;
        private final int offset;

        MockDateTimeZone(String id, int standardOffset, int offset) {
            super(id);
            this.standardOffset = standardOffset;
            this.offset = offset;
        }

        @Override
        public String getNameKey(long instant) {
            return "MOCK";
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
            if (this == object) {
                return true;
            }
            if (object instanceof MockDateTimeZone) {
                MockDateTimeZone other = (MockDateTimeZone) object;
                return getID().equals(other.getID()) && offset == other.offset && standardOffset == other.standardOffset;
            }
            return false;
        }
    }

    private static class MockNullKeyZone extends DateTimeZone {
        private static final long serialVersionUID = 1L;

        MockNullKeyZone(String id) {
            super(id);
        }

        @Override
        public String getNameKey(long instant) {
            return null;
        }

        @Override
        public int getOffset(long instant) {
            return 3600000;
        }

        @Override
        public int getStandardOffset(long instant) {
            return 3600000;
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
            return object instanceof MockNullKeyZone && getID().equals(((MockNullKeyZone) object).getID());
        }
    }

    //-----------------------------------------------------------------------
    // Default Zone Tests
    //-----------------------------------------------------------------------
    @Test
    public void testGetDefault() {
        DateTimeZone def = DateTimeZone.getDefault();
        Assert.assertNotNull(def);
    }

    @Test
    public void testSetDefault_valid() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        Assert.assertEquals(paris, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_null() {
        DateTimeZone.setDefault(null);
    }

    //-----------------------------------------------------------------------
    // forID Tests
    //-----------------------------------------------------------------------
    @Test
    public void testForID_null() {
        Assert.assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
    }

    @Test
    public void testForID_UTC() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test
    public void testForID_validProviderZone() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        Assert.assertEquals("Europe/London", london.getID());
    }

    @Test
    public void testForID_offsetZero() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));
    }

    @Test
    public void testForID_fixedPositiveOffset() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        Assert.assertEquals("+05:30", zone.getID());
        Assert.assertEquals(5 * 3600000 + 30 * 60000, zone.getOffset(0L));
    }

    @Test
    public void testForID_fixedNegativeOffset() {
        DateTimeZone zone = DateTimeZone.forID("-08:00");
        Assert.assertEquals("-08:00", zone.getID());
        Assert.assertEquals(-8 * 3600000, zone.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidString() {
        DateTimeZone.forID("Invalid/NonExistentZone");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidOffsetFormat() {
        DateTimeZone.forID("+25:00");
    }

    //-----------------------------------------------------------------------
    // forOffsetHours and forOffsetHoursMinutes Tests
    //-----------------------------------------------------------------------
    @Test
    public void testForOffsetHours_zero() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
    }

    @Test
    public void testForOffsetHours_positive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        Assert.assertEquals("+03:00", zone.getID());
        Assert.assertEquals(3 * 3600000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHours_negative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-5);
        Assert.assertEquals("-05:00", zone.getID());
        Assert.assertEquals(-5 * 3600000, zone.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_outOfRange() {
        DateTimeZone.forOffsetHours(24);
    }

    @Test
    public void testForOffsetHoursMinutes_zero() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
    }

    @Test
    public void testForOffsetHoursMinutes_positive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 45);
        Assert.assertEquals("+05:45", zone.getID());
        Assert.assertEquals((5 * 60 + 45) * 60000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_negative() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        Assert.assertEquals("-02:30", zone.getID());
        Assert.assertEquals(-(2 * 60 + 30) * 60000, zone.getOffset(0L));
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
    public void testForOffsetHoursMinutes_arithmeticOverflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    //-----------------------------------------------------------------------
    // forOffsetMillis and printOffset Tests
    //-----------------------------------------------------------------------
    @Test
    public void testForOffsetMillis_zero() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
    }

    @Test
    public void testForOffsetMillis_caching() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(3600000);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(3600000);
        Assert.assertSame(zone1, zone2);
    }

    @Test
    public void testForOffsetMillis_withSecondsAndMillis() {
        // Offset with hours, minutes, seconds and millis: 1h 2m 3s 4ms = 3723004
        DateTimeZone zonePos = DateTimeZone.forOffsetMillis(3723004);
        Assert.assertEquals("+01:02:03.004", zonePos.getID());
        Assert.assertEquals(3723004, zonePos.getOffset(0L));

        DateTimeZone zoneNeg = DateTimeZone.forOffsetMillis(-3723004);
        Assert.assertEquals("-01:02:03.004", zoneNeg.getID());
        Assert.assertEquals(-3723004, zoneNeg.getOffset(0L));

        // Offset with seconds only: 1h 2m 3s = 3723000
        DateTimeZone zoneSec = DateTimeZone.forOffsetMillis(3723000);
        Assert.assertEquals("+01:02:03", zoneSec.getID());

        DateTimeZone zoneSecNeg = DateTimeZone.forOffsetMillis(-3723000);
        Assert.assertEquals("-01:02:03", zoneSecNeg.getID());
    }

    //-----------------------------------------------------------------------
    // forTimeZone Tests
    //-----------------------------------------------------------------------
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
    public void testForTimeZone_convertedAliases() {
        Assert.assertEquals("America/New_York", DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST")).getID());
        Assert.assertEquals("America/Los_Angeles", DateTimeZone.forTimeZone(TimeZone.getTimeZone("PST")).getID());
        Assert.assertEquals("UTC", DateTimeZone.forTimeZone(TimeZone.getTimeZone("GMT")).getID());
        Assert.assertEquals("Pacific/Honolulu", DateTimeZone.forTimeZone(TimeZone.getTimeZone("HST")).getID());
        Assert.assertEquals("Asia/Tokyo", DateTimeZone.forTimeZone(TimeZone.getTimeZone("JST")).getID());
    }

    @Test
    public void testForTimeZone_standardIDs() {
        TimeZone tz = TimeZone.getTimeZone("Europe/Paris");
        Assert.assertEquals("Europe/Paris", DateTimeZone.forTimeZone(tz).getID());
    }

    @Test
    public void testForTimeZone_customGMT() {
        TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        Assert.assertEquals("+02:00", zone.getID());
        Assert.assertEquals(7200000, zone.getOffset(0L));

        TimeZone tzZero = TimeZone.getTimeZone("GMT+00:00");
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(tzZero));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTimeZone_unrecognised() {
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
                return "NonExistentTimeZoneID";
            }
            @Override
            public String getDisplayName() {
                return "NonExistentTimeZoneDisplayName";
            }
        };
        DateTimeZone.forTimeZone(tz);
    }

    //-----------------------------------------------------------------------
    // Provider & NameProvider Tests
    //-----------------------------------------------------------------------
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

    @Test
    public void testSetProvider_valid() {
        Provider custom = new UTCProvider();
        DateTimeZone.setProvider(custom);
        Assert.assertSame(custom, DateTimeZone.getProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_noAvailableIDs() {
        Provider invalid = new Provider() {
            public DateTimeZone getZone(String name) { return null; }
            public Set<String> getAvailableIDs() { return Collections.emptySet(); }
        };
        DateTimeZone.setProvider(invalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_missingUTC() {
        Provider invalid = new Provider() {
            public DateTimeZone getZone(String name) { return null; }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("America/New_York");
                return set;
            }
        };
        DateTimeZone.setProvider(invalid);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_invalidUTC() {
        Provider invalid = new Provider() {
            public DateTimeZone getZone(String name) {
                return name.equals("UTC") ? new FixedDateTimeZone("UTC", "UTC", 3600, 3600) : null;
            }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("UTC");
                return set;
            }
        };
        DateTimeZone.setProvider(invalid);
    }

    @Test
    public void testSetNameProvider_null() {
        DateTimeZone.setNameProvider(null);
        Assert.assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testSetNameProvider_custom() {
        NameProvider custom = new DefaultNameProvider();
        DateTimeZone.setNameProvider(custom);
        Assert.assertSame(custom, DateTimeZone.getNameProvider());
    }

    //-----------------------------------------------------------------------
    // Instance Methods: Names, Offsets, Transitions
    //-----------------------------------------------------------------------
    @Test
    public void testGetID() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        Assert.assertEquals("Europe/Paris", zone.getID());
    }

    @Test
    public void testGetNameAndShortName() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // 1970-01-01 (GMT)
        long summerInstant = 10000000000L; // 1970-04-26 (BST)

        Assert.assertNotNull(zone.getName(winterInstant));
        Assert.assertNotNull(zone.getName(winterInstant, Locale.ENGLISH));
        Assert.assertNotNull(zone.getShortName(winterInstant));
        Assert.assertNotNull(zone.getShortName(winterInstant, Locale.ENGLISH));
        Assert.assertNotNull(zone.getShortName(summerInstant, Locale.UK));
    }

    @Test
    public void testGetName_fallbackToPrintOffset() {
        NameProvider emptyProvider = new NameProvider() {
            public String getShortName(Locale locale, String id, String nameKey) { return null; }
            public String getName(Locale locale, String id, String nameKey) { return null; }
        };
        DateTimeZone.setNameProvider(emptyProvider);
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals("+02:00", zone.getName(0L, Locale.ENGLISH));
        Assert.assertEquals("+02:00", zone.getShortName(0L, Locale.ENGLISH));
    }

    @Test
    public void testGetName_nullKeyReturnsID() {
        DateTimeZone zone = new MockNullKeyZone("NullKeyZone");
        Assert.assertEquals("NullKeyZone", zone.getName(0L, Locale.ENGLISH));
        Assert.assertEquals("NullKeyZone", zone.getShortName(0L, Locale.ENGLISH));
    }

    @Test
    public void testGetOffset_ReadableInstant() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals(7200000, zone.getOffset(new Instant(10000L)));
        Assert.assertEquals(7200000, zone.getOffset((ReadableInstant) null));
    }

    @Test
    public void testIsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long winterInstant = 0L; // GMT is standard
        long summerInstant = 15000000000L; // BST is summer time
        Assert.assertTrue(zone.isStandardOffset(winterInstant));
        Assert.assertFalse(zone.isStandardOffset(summerInstant));
    }

    //-----------------------------------------------------------------------
    // Time Conversions & Arithmetic Boundaries
    //-----------------------------------------------------------------------
    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals(7200000L, zone.convertUTCToLocal(0L));
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_overflowPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        zone.convertUTCToLocal(Long.MAX_VALUE - 100);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocal_overflowNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-2);
        zone.convertUTCToLocal(Long.MIN_VALUE + 100);
    }

    @Test
    public void testConvertLocalToUTC_strictAndLenient() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long utc = zone.convertLocalToUTC(3600000L, false);
        Assert.assertEquals(0L, utc);

        long utcStrict = zone.convertLocalToUTC(3600000L, true);
        Assert.assertEquals(0L, utcStrict);
    }

    @Test(expected = ArithmeticException.class)
    public void testConvertLocalToUTC_overflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        zone.convertLocalToUTC(Long.MIN_VALUE + 100, false);
    }

    @Test
    public void testConvertLocalToUTC_withOriginalInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long original = 0L;
        long local = zone.convertUTCToLocal(original);
        long converted = zone.convertLocalToUTC(local, false, original);
        Assert.assertEquals(original, converted);
    }

    @Test
    public void testConvertLocalToUTC_gapLenientVsStrict() {
        // Spring forward in Europe/London on 2011-03-27: 01:00 -> 02:00
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long gapLocalMillis = new DateTime(2011, 3, 27, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();

        // Lenient should succeed and adjust
        long lenientUTC = zone.convertLocalToUTC(gapLocalMillis, false);
        Assert.assertTrue(lenientUTC > 0);

        // Strict should fail
        try {
            zone.convertLocalToUTC(gapLocalMillis, true);
            Assert.fail("Expected IllegalArgumentException for DST gap in strict mode");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Illegal instant due to time zone offset transition"));
        }
    }

    @Test
    public void testConvertLocalToUTC_westernHemisphereGapLenient() {
        // America/New_York DST gap on 2011-03-13: 02:00 -> 03:00
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long gapLocalMillis = new DateTime(2011, 3, 13, 2, 30, 0, 0, DateTimeZone.UTC).getMillis();

        long lenientUTC = zone.convertLocalToUTC(gapLocalMillis, false);
        Assert.assertTrue(lenientUTC > 0);
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        // Winter local time
        long winterLocal = new DateTime(2011, 1, 15, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(0, zone.getOffsetFromLocal(winterLocal));

        // Summer local time
        long summerLocal = new DateTime(2011, 7, 15, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(3600000, zone.getOffsetFromLocal(summerLocal));

        // Negative offset zone (e.g. America/New_York)
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long nyLocal = new DateTime(2011, 1, 15, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(-5 * 3600000, ny.getOffsetFromLocal(nyLocal));
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(1);
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(3);

        long instant = 10000000L;
        long converted = zone1.getMillisKeepLocal(zone2, instant);
        Assert.assertEquals(instant - 2 * 3600000L, converted);

        // Same zone optimization
        Assert.assertEquals(instant, zone1.getMillisKeepLocal(zone1, instant));

        // Null zone defaults to default zone
        long convertedDefault = zone1.getMillisKeepLocal(null, instant);
        Assert.assertEquals(zone1.getMillisKeepLocal(DateTimeZone.getDefault(), instant), convertedDefault);
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(2);
        LocalDateTime ldt = new LocalDateTime(2011, 3, 27, 1, 30);
        Assert.assertFalse(fixed.isLocalDateTimeGap(ldt));

        DateTimeZone london = DateTimeZone.forID("Europe/London");
        Assert.assertTrue(london.isLocalDateTimeGap(ldt));

        LocalDateTime nonGap = new LocalDateTime(2011, 3, 27, 0, 30);
        Assert.assertFalse(london.isLocalDateTimeGap(nonGap));
    }

    @Test
    public void testAdjustOffset() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long standardInstant = new DateTime(2011, 1, 1, 0, 0, london).getMillis();
        Assert.assertEquals(standardInstant, london.adjustOffset(standardInstant, true));
        Assert.assertEquals(standardInstant, london.adjustOffset(standardInstant, false));

        // Fall-back overlap in London: 2011-10-30 01:30 occurs twice (BST and GMT)
        long overlapBst = new DateTime(2011, 10, 30, 1, 30, 0, 0, DateTimeZone.forOffsetHours(1)).getMillis();
        long earlier = london.adjustOffset(overlapBst, false);
        long later = london.adjustOffset(overlapBst, true);
        Assert.assertTrue(earlier < later);
    }

    @Test
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        TimeZone tz = zone.toTimeZone();
        Assert.assertEquals("Europe/Paris", tz.getID());
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone z1 = DateTimeZone.forOffsetHours(2);
        DateTimeZone z2 = DateTimeZone.forOffsetHours(2);
        DateTimeZone z3 = DateTimeZone.forOffsetHours(3);

        Assert.assertTrue(z1.equals(z1));
        Assert.assertTrue(z1.equals(z2));
        Assert.assertFalse(z1.equals(z3));
        Assert.assertFalse(z1.equals(null));
        Assert.assertFalse(z1.equals("NotAZone"));
        Assert.assertEquals(z1.hashCode(), z2.hashCode());
    }

    @Test
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        Assert.assertEquals("Europe/Paris", zone.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullID() {
        new MockDateTimeZone(null, 0, 0);
    }

    //-----------------------------------------------------------------------
    // Serialization Tests
    //-----------------------------------------------------------------------
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
    public void testSerializationFixedOffset() throws Exception {
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

    //-----------------------------------------------------------------------
    // JodaTimePermission / SecurityManager Tests
    //-----------------------------------------------------------------------
    @Test
    public void testSetDefaultSecurityPermission() {
        SecurityManager originalSm = System.getSecurityManager();
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                    if (perm.getName().equals("DateTimeZone.setDefault")) {
                        throw new SecurityException("Permission denied for test");
                    }
                }
            });

            try {
                DateTimeZone.setDefault(DateTimeZone.UTC);
                Assert.fail("Expected SecurityException");
            } catch (SecurityException ex) {
                Assert.assertEquals("Permission denied for test", ex.getMessage());
            }
        } finally {
            System.setSecurityManager(originalSm);
        }
    }

    @Test
    public void testSetProviderSecurityPermission() {
        SecurityManager originalSm = System.getSecurityManager();
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                    if (perm.getName().equals("DateTimeZone.setProvider")) {
                        throw new SecurityException("Permission denied for test");
                    }
                }
            });

            try {
                DateTimeZone.setProvider(new UTCProvider());
                Assert.fail("Expected SecurityException");
            } catch (SecurityException ex) {
                Assert.assertEquals("Permission denied for test", ex.getMessage());
            }
        } finally {
            System.setSecurityManager(originalSm);
        }
    }

    @Test
    public void testSetNameProviderSecurityPermission() {
        SecurityManager originalSm = System.getSecurityManager();
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission perm) {
                    if (perm.getName().equals("DateTimeZone.setNameProvider")) {
                        throw new SecurityException("Permission denied for test");
                    }
                }
            });

            try {
                DateTimeZone.setNameProvider(new DefaultNameProvider());
                Assert.fail("Expected SecurityException");
            } catch (SecurityException ex) {
                Assert.assertEquals("Permission denied for test", ex.getMessage());
            }
        } finally {
            System.setSecurityManager(originalSm);
        }
    }

    //-----------------------------------------------------------------------
    // Reflection Tests for Private Methods
    //-----------------------------------------------------------------------
    @Test
    public void testPrivateParseOffsetAndPrintOffset() throws Exception {
        Method printOffsetMethod = DateTimeZone.class.getDeclaredMethod("printOffset", int.class);
        printOffsetMethod.setAccessible(true);

        Method parseOffsetMethod = DateTimeZone.class.getDeclaredMethod("parseOffset", String.class);
        parseOffsetMethod.setAccessible(true);

        String strPos = (String) printOffsetMethod.invoke(null, 3600000);
        Assert.assertEquals("+01:00", strPos);

        int parsedPos = (Integer) parseOffsetMethod.invoke(null, "+01:00");
        Assert.assertEquals(3600000, parsedPos);

        String strNeg = (String) printOffsetMethod.invoke(null, -3600000);
        Assert.assertEquals("-01:00", strNeg);

        int parsedNeg = (Integer) parseOffsetMethod.invoke(null, "-01:00");
        Assert.assertEquals(-3600000, parsedNeg);
    }
}
