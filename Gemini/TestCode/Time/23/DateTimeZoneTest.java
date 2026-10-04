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
    public void testGetDefaultAndSetDefault() {
        DateTimeZone zone = DateTimeZone.getDefault();
        Assert.assertNotNull(zone);

        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        Assert.assertEquals(paris, DateTimeZone.getDefault());

        try {
            DateTimeZone.setDefault(null);
            Assert.fail("Expected IllegalArgumentException for null default");
        } catch (IllegalArgumentException e) {
            // expected
        }
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
        Assert.assertTrue(zone.isFixed());
        Assert.assertEquals(0, zone.getOffset(0L));
    }

    @Test
    public void testForID_validRegions() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        Assert.assertNotNull(london);
        Assert.assertEquals("Europe/London", london.getID());

        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        Assert.assertNotNull(ny);
        Assert.assertEquals("America/New_York", ny.getID());
    }

    @Test
    public void testForID_offsetStrings() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));

        DateTimeZone p1 = DateTimeZone.forID("+01:00");
        Assert.assertEquals("+01:00", p1.getID());
        Assert.assertEquals(3600000, p1.getOffset(0L));
        Assert.assertTrue(p1.isFixed());

        DateTimeZone m5 = DateTimeZone.forID("-05:00");
        Assert.assertEquals("-05:00", m5.getID());
        Assert.assertEquals(-5 * 3600000, m5.getOffset(0L));

        DateTimeZone pOffsetWithSeconds = DateTimeZone.forID("+01:30:15");
        Assert.assertEquals("+01:30:15", pOffsetWithSeconds.getID());
        Assert.assertEquals((1 * 3600 + 30 * 60 + 15) * 1000, pOffsetWithSeconds.getOffset(0L));

        DateTimeZone pOffsetWithMillis = DateTimeZone.forID("+01:30:15.123");
        Assert.assertEquals("+01:30:15.123", pOffsetWithMillis.getID());
        Assert.assertEquals((1 * 3600 + 30 * 60 + 15) * 1000 + 123, pOffsetWithMillis.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalid() {
        DateTimeZone.forID("Invalid/ID_String");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_invalidOffsetFormat() {
        DateTimeZone.forID("+25:00");
    }

    @Test
    public void testForOffsetHours() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        Assert.assertEquals("+02:00", zone.getID());
        Assert.assertEquals(2 * 3600000, zone.getOffset(0L));

        DateTimeZone negZone = DateTimeZone.forOffsetHours(-8);
        Assert.assertEquals("-08:00", negZone.getID());
        Assert.assertEquals(-8 * 3600000, negZone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));

        DateTimeZone p530 = DateTimeZone.forOffsetHoursMinutes(5, 30);
        Assert.assertEquals("+05:30", p530.getID());
        Assert.assertEquals((5 * 60 + 30) * 60000, p530.getOffset(0L));

        DateTimeZone m330 = DateTimeZone.forOffsetHoursMinutes(-3, 30);
        Assert.assertEquals("-03:30", m330.getID());
        Assert.assertEquals((-3 * 60 - 30) * 60000, m330.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_minutesTooLarge() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_overflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    @Test
    public void testForOffsetMillis() {
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));

        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(18000000); // +05:00
        Assert.assertEquals("+05:00", zone1.getID());
        Assert.assertEquals(18000000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(-18000000); // -05:00
        Assert.assertEquals("-05:00", zone2.getID());

        // Test cache reuse
        DateTimeZone zone1Cached = DateTimeZone.forOffsetMillis(18000000);
        Assert.assertSame(zone1, zone1Cached);

        // Sub-minute offset formatting
        DateTimeZone zoneSec = DateTimeZone.forOffsetMillis(12345000);
        Assert.assertEquals("+03:25:45", zoneSec.getID());

        DateTimeZone zoneMilli = DateTimeZone.forOffsetMillis(12345678);
        Assert.assertEquals("+03:25:45.678", zoneMilli.getID());
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
    public void testForTimeZone_convertedAliases() {
        TimeZone tzEst = TimeZone.getTimeZone("EST");
        DateTimeZone jodaEst = DateTimeZone.forTimeZone(tzEst);
        Assert.assertEquals("America/New_York", jodaEst.getID());

        TimeZone tzGmt = TimeZone.getTimeZone("GMT");
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(tzGmt));

        TimeZone tzCet = TimeZone.getTimeZone("ECT");
        Assert.assertEquals("Europe/Paris", DateTimeZone.forTimeZone(tzCet).getID());

        TimeZone tzPst = TimeZone.getTimeZone("PST");
        Assert.assertEquals("America/Los_Angeles", DateTimeZone.forTimeZone(tzPst).getID());
    }

    @Test
    public void testForTimeZone_customGmt() {
        TimeZone custom1 = TimeZone.getTimeZone("GMT+02:00");
        DateTimeZone dtz1 = DateTimeZone.forTimeZone(custom1);
        Assert.assertEquals("+02:00", dtz1.getID());

        TimeZone custom2 = TimeZone.getTimeZone("GMT-04:00");
        DateTimeZone dtz2 = DateTimeZone.forTimeZone(custom2);
        Assert.assertEquals("-04:00", dtz2.getID());

        TimeZone customZero = TimeZone.getTimeZone("GMT+00:00");
        Assert.assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(customZero));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTimeZone_unrecognised() {
        TimeZone custom = new TimeZone() {
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
                return "UnknownNonExistentZoneID";
            }
            @Override
            public String getDisplayName(boolean daylight, int style, Locale locale) {
                return "UnknownCustom";
            }
        };
        DateTimeZone.forTimeZone(custom);
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
    public void testSetProvider_valid() {
        Provider current = DateTimeZone.getProvider();
        Assert.assertNotNull(current);

        DateTimeZone.setProvider(new UTCProvider());
        Assert.assertEquals(1, DateTimeZone.getAvailableIDs().size());
        Assert.assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));

        // Reset with null uses default
        DateTimeZone.setProvider(null);
        Assert.assertNotNull(DateTimeZone.getProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProvider_emptyIDs() {
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
    public void testSetProvider_badUTCZone() {
        Provider badUtcProvider = new Provider() {
            public DateTimeZone getZone(String id) {
                if ("UTC".equals(id)) {
                    return DateTimeZone.forOffsetHours(1);
                }
                return null;
            }
            public Set<String> getAvailableIDs() {
                Set<String> set = new HashSet<String>();
                set.add("UTC");
                return set;
            }
        };
        DateTimeZone.setProvider(badUtcProvider);
    }

    @Test
    public void testSetNameProvider_valid() {
        NameProvider original = DateTimeZone.getNameProvider();
        Assert.assertNotNull(original);

        NameProvider custom = new DefaultNameProvider();
        DateTimeZone.setNameProvider(custom);
        Assert.assertSame(custom, DateTimeZone.getNameProvider());

        DateTimeZone.setNameProvider(null);
        Assert.assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testGetNameAndShortName() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2010, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2010, 7, 1, 12, 0, DateTimeZone.UTC).getMillis();

        Assert.assertNotNull(london.getName(winterInstant));
        Assert.assertNotNull(london.getName(winterInstant, Locale.UK));
        Assert.assertNotNull(london.getShortName(winterInstant));
        Assert.assertNotNull(london.getShortName(winterInstant, Locale.UK));

        Assert.assertNotNull(london.getName(summerInstant));
        Assert.assertNotNull(london.getShortName(summerInstant));

        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        Assert.assertEquals("+03:00", fixed.getName(winterInstant, Locale.ENGLISH));
        Assert.assertEquals("+03:00", fixed.getShortName(winterInstant, Locale.ENGLISH));

        // Fallback when nameProvider returns null
        DateTimeZone.setNameProvider(new NameProvider() {
            public String getShortName(Locale locale, String id, String nameKey) {
                return null;
            }
            public String getName(Locale locale, String id, String nameKey) {
                return null;
            }
        });
        Assert.assertEquals("+00:00", london.getShortName(winterInstant));
        Assert.assertEquals("+00:00", london.getName(winterInstant));
    }

    @Test
    public void testGetOffsetWithReadableInstant() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTime winter = new DateTime(2010, 1, 1, 12, 0, DateTimeZone.UTC);
        DateTime summer = new DateTime(2010, 7, 1, 12, 0, DateTimeZone.UTC);

        Assert.assertEquals(0, london.getOffset(winter));
        Assert.assertEquals(3600000, london.getOffset(summer));
        Assert.assertEquals(london.getOffset(DateTimeUtils.currentTimeMillis()), london.getOffset((ReadableInstant) null));
    }

    @Test
    public void testIsStandardOffset() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long winterInstant = new DateTime(2010, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        long summerInstant = new DateTime(2010, 7, 1, 12, 0, DateTimeZone.UTC).getMillis();

        Assert.assertTrue(london.isStandardOffset(winterInstant));
        Assert.assertFalse(london.isStandardOffset(summerInstant));

        Assert.assertTrue(DateTimeZone.UTC.isStandardOffset(winterInstant));
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Standard time: Jan 1
        long stdLocal = new DateTime(2010, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(-5 * 3600000, ny.getOffsetFromLocal(stdLocal));

        // Summer time: Jul 1
        long dstLocal = new DateTime(2010, 7, 1, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(-4 * 3600000, ny.getOffsetFromLocal(dstLocal));

        // During spring-forward gap in America/New_York (2010-03-14: 02:00 -> 03:00)
        // 02:30 is in the gap
        long gapLocal = new DateTime(2010, 3, 14, 2, 30, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(-4 * 3600000, ny.getOffsetFromLocal(gapLocal));

        // During fall-back overlap in America/New_York (2010-11-07: 01:00 -> 01:59 repeated)
        // 01:30 is in the overlap; offset should favour daylight savings (-4 hours)
        long overlapLocal = new DateTime(2010, 11, 7, 1, 30, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(-4 * 3600000, ny.getOffsetFromLocal(overlapLocal));

        // Test with positive offset zone (Europe/Paris)
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        // Overlap: 2010-10-31: 02:30 repeated
        long parisOverlapLocal = new DateTime(2010, 10, 31, 2, 30, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(2 * 3600000, paris.getOffsetFromLocal(parisOverlapLocal));
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone p2 = DateTimeZone.forOffsetHours(2);
        long utc = 1000000L;
        Assert.assertEquals(1000000L + 2 * 3600000, p2.convertUTCToLocal(utc));

        // Overflow test
        try {
            p2.convertUTCToLocal(Long.MAX_VALUE);
            Assert.fail("Expected ArithmeticException on overflow");
        } catch (ArithmeticException e) {
            // expected
        }

        DateTimeZone m2 = DateTimeZone.forOffsetHours(-2);
        try {
            m2.convertUTCToLocal(Long.MIN_VALUE);
            Assert.fail("Expected ArithmeticException on overflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testConvertLocalToUTC_strictAndNonStrict() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");

        // Non-gap regular time
        long regularLocal = new DateTime(2010, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(regularLocal + 5 * 3600000, ny.convertLocalToUTC(regularLocal, false));
        Assert.assertEquals(regularLocal + 5 * 3600000, ny.convertLocalToUTC(regularLocal, true));

        // Gap time (2010-03-14 02:30 local)
        long gapLocal = new DateTime(2010, 3, 14, 2, 30, DateTimeZone.UTC).getMillis();
        try {
            ny.convertLocalToUTC(gapLocal, true);
            Assert.fail("Expected IllegalArgumentException for gap when strict");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Non-strict in gap should not throw
        long nonStrictUTC = ny.convertLocalToUTC(gapLocal, false);
        Assert.assertTrue(nonStrictUTC > 0);

        // Strict in Eastern hemisphere gap (Europe/Berlin: 2010-03-28 02:30 local)
        DateTimeZone berlin = DateTimeZone.forID("Europe/Berlin");
        long berlinGapLocal = new DateTime(2010, 3, 28, 2, 30, DateTimeZone.UTC).getMillis();
        try {
            berlin.convertLocalToUTC(berlinGapLocal, true);
            Assert.fail("Expected IllegalArgumentException for berlin gap when strict");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // Non-strict in Eastern hemisphere gap
        long berlinUTC = berlin.convertLocalToUTC(berlinGapLocal, false);
        Assert.assertTrue(berlinUTC > 0);

        // Overflow check
        try {
            ny.convertLocalToUTC(Long.MAX_VALUE, false);
            Assert.fail("Expected ArithmeticException on overflow");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test
    public void testConvertLocalToUTC_withOriginalInstant() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        // Overlap: 2010-11-07 01:30 local occurs twice (EDT offset -4h, EST offset -5h)
        long overlapLocal = new DateTime(2010, 11, 7, 1, 30, DateTimeZone.UTC).getMillis();

        long originalEdt = overlapLocal + 4 * 3600000;
        long originalEst = overlapLocal + 5 * 3600000;

        long resEdt = ny.convertLocalToUTC(overlapLocal, false, originalEdt);
        long resEst = ny.convertLocalToUTC(overlapLocal, false, originalEst);

        Assert.assertEquals(originalEdt, resEdt);
        Assert.assertEquals(originalEst, resEst);
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone ny = DateTimeZone.forID("America/New_York");

        long instant = new DateTime(2010, 1, 1, 12, 0, london).getMillis();
        long nyInstant = london.getMillisKeepLocal(ny, instant);

        DateTime londonDt = new DateTime(instant, london);
        DateTime nyDt = new DateTime(nyInstant, ny);

        Assert.assertEquals(londonDt.getHourOfDay(), nyDt.getHourOfDay());
        Assert.assertEquals(londonDt.getMinuteOfHour(), nyDt.getMinuteOfHour());

        // Same zone
        Assert.assertEquals(instant, london.getMillisKeepLocal(london, instant));

        // null newZone uses default
        long defaultInstant = london.getMillisKeepLocal(null, instant);
        Assert.assertEquals(london.getMillisKeepLocal(DateTimeZone.getDefault(), instant), defaultInstant);
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        LocalDateTime gapTime = new LocalDateTime(2010, 3, 14, 2, 30);
        LocalDateTime nonGapTime = new LocalDateTime(2010, 3, 14, 3, 30);

        Assert.assertTrue(ny.isLocalDateTimeGap(gapTime));
        Assert.assertFalse(ny.isLocalDateTimeGap(nonGapTime));

        Assert.assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(gapTime));
    }

    @Test
    public void testAdjustOffset() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");

        // Overlap instant on 2010-11-07
        long overlapLocal = new DateTime(2010, 11, 7, 1, 30, DateTimeZone.UTC).getMillis();
        long instantEarlier = overlapLocal + 4 * 3600000; // EDT
        long instantLater = overlapLocal + 5 * 3600000;   // EST

        Assert.assertEquals(instantEarlier, ny.adjustOffset(instantLater, false));
        Assert.assertEquals(instantLater, ny.adjustOffset(instantEarlier, true));

        // Regular non-overlapping instant
        long regular = new DateTime(2010, 1, 1, 12, 0, DateTimeZone.UTC).getMillis();
        Assert.assertEquals(regular, ny.adjustOffset(regular, false));
        Assert.assertEquals(regular, ny.adjustOffset(regular, true));
    }

    @Test
    public void testTransitions() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        long instant = new DateTime(2010, 1, 1, 0, 0, DateTimeZone.UTC).getMillis();
        long next = ny.nextTransition(instant);
        long prev = ny.previousTransition(instant);

        Assert.assertTrue(next > instant);
        Assert.assertTrue(prev < instant);

        Assert.assertTrue(DateTimeZone.UTC.isFixed());
        Assert.assertEquals(instant, DateTimeZone.UTC.nextTransition(instant));
        Assert.assertEquals(instant, DateTimeZone.UTC.previousTransition(instant));
    }

    @Test
    public void testToTimeZone() {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");
        TimeZone tz = ny.toTimeZone();
        Assert.assertEquals("America/New_York", tz.getID());

        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        TimeZone tzFixed = fixed.toTimeZone();
        Assert.assertEquals("+03:00", tzFixed.getID());
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(2);
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(2);
        DateTimeZone zone3 = DateTimeZone.forOffsetHours(3);

        Assert.assertEquals(zone1, zone2);
        Assert.assertFalse(zone1.equals(zone3));
        Assert.assertFalse(zone1.equals("NotADateTimeZone"));
        Assert.assertFalse(zone1.equals(null));

        Assert.assertEquals(zone1.hashCode(), zone2.hashCode());
        Assert.assertEquals("+02:00", zone1.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        DateTimeZone ny = DateTimeZone.forID("America/New_York");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(ny);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DateTimeZone deserialized = (DateTimeZone) ois.readObject();
        ois.close();

        Assert.assertEquals(ny, deserialized);
        Assert.assertSame(ny, deserialized);
    }
}
