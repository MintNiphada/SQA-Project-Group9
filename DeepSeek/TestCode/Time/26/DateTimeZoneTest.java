package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DateTimeZoneTest {

    @Before
    public void setUp() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeZone.setProvider(null);
        DateTimeZone.setNameProvider(null);
    }

    @After
    public void tearDown() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        DateTimeZone.setProvider(null);
        DateTimeZone.setNameProvider(null);
    }

    @Test
    public void testGetDefault() {
        assertNotNull(DateTimeZone.getDefault());
    }

    @Test
    public void testSetDefault() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultNull() {
        DateTimeZone.setDefault(null);
    }

    @Test
    public void testForIDNull() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forID(null));
    }

    @Test
    public void testForIDUTC() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test
    public void testForIDValid() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone);
        assertEquals("Europe/London", zone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForIDInvalid() {
        DateTimeZone.forID("Invalid/Zone");
    }

    @Test
    public void testForIDFixedOffsetPositive() {
        DateTimeZone zone = DateTimeZone.forID("+01:00");
        assertNotNull(zone);
        assertEquals(3600000, zone.getOffset(0));
    }

    @Test
    public void testForIDFixedOffsetNegative() {
        DateTimeZone zone = DateTimeZone.forID("-05:30");
        assertNotNull(zone);
        assertEquals(-19800000, zone.getOffset(0));
    }

    @Test
    public void testForIDFixedOffsetZero() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));
    }

    @Test
    public void testForOffsetHoursZero() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
    }

    @Test
    public void testForOffsetHoursPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals(7200000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-3);
        assertEquals(-10800000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursMinutesZero() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0, 0));
    }

    @Test
    public void testForOffsetHoursMinutesValid() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(1, 30);
        assertEquals(5400000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetHoursMinutesNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals(-9000000, zone.getOffset(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesInvalidMinutesLow() {
        DateTimeZone.forOffsetHoursMinutes(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesInvalidMinutesHigh() {
        DateTimeZone.forOffsetHoursMinutes(0, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesOverflow() {
        DateTimeZone.forOffsetHoursMinutes(Integer.MAX_VALUE, 0);
    }

    @Test
    public void testForOffsetMillisZero() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
    }

    @Test
    public void testForOffsetMillisPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(5000);
        assertEquals(5000, zone.getOffset(0));
    }

    @Test
    public void testForOffsetMillisNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-5000);
        assertEquals(-5000, zone.getOffset(0));
    }

    @Test
    public void testForTimeZoneNull() {
        assertEquals(DateTimeZone.getDefault(), DateTimeZone.forTimeZone(null));
    }

    @Test
    public void testForTimeZoneUTC() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
    }

    @Test
    public void testForTimeZoneValid() {
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        assertEquals("Europe/Paris", zone.getID());
    }

    @Test
    public void testForTimeZoneShortID() {
        DateTimeZone zone = DateTimeZone.forTimeZone(TimeZone.getTimeZone("EST"));
        assertEquals("America/New_York", zone.getID());
    }

    @Test
    public void testForTimeZoneGMTDisplay() {
        TimeZone jdkZone = TimeZone.getTimeZone("GMT+01:00");
        DateTimeZone zone = DateTimeZone.forTimeZone(jdkZone);
        assertEquals(3600000, zone.getOffset(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForTimeZoneInvalid() {
        TimeZone custom = TimeZone.getTimeZone("Invalid/Zone");
        DateTimeZone.forTimeZone(custom);
    }

    @Test
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.contains("UTC"));
    }

    @Test
    public void testGetProvider() {
        assertNotNull(DateTimeZone.getProvider());
    }

    @Test
    public void testSetProvider() {
        Provider custom = new UTCProvider();
        DateTimeZone.setProvider(custom);
        assertEquals(custom, DateTimeZone.getProvider());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetProviderInvalidNoUTC() {
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
    public void testSetProviderInvalidWrongUTC() {
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
        assertNotNull(DateTimeZone.getNameProvider());
    }

    @Test
    public void testSetNameProvider() {
        NameProvider custom = new DefaultNameProvider();
        DateTimeZone.setNameProvider(custom);
        assertEquals(custom, DateTimeZone.getNameProvider());
    }

    @Test
    public void testUTCGetID() {
        assertEquals("UTC", DateTimeZone.UTC.getID());
    }

    @Test
    public void testUTCGetShortName() {
        assertEquals("+00:00", DateTimeZone.UTC.getShortName(0));
    }

    @Test
    public void testUTCGetName() {
        assertEquals("+00:00", DateTimeZone.UTC.getName(0));
    }

    @Test
    public void testUTCGetOffset() {
        assertEquals(0, DateTimeZone.UTC.getOffset(0));
    }

    @Test
    public void testUTCGetOffsetReadableInstantNull() {
        assertEquals(0, DateTimeZone.UTC.getOffset((ReadableInstant) null));
    }

    @Test
    public void testUTCGetStandardOffset() {
        assertEquals(0, DateTimeZone.UTC.getStandardOffset(0));
    }

    @Test
    public void testUTCIsStandardOffset() {
        assertTrue(DateTimeZone.UTC.isStandardOffset(0));
    }

    @Test
    public void testUTCGetOffsetFromLocal() {
        assertEquals(0, DateTimeZone.UTC.getOffsetFromLocal(0));
    }

    @Test
    public void testUTCConvertUTCToLocal() {
        assertEquals(1000, DateTimeZone.UTC.convertUTCToLocal(1000));
    }

    @Test
    public void testUTCConvertLocalToUTC() {
        assertEquals(1000, DateTimeZone.UTC.convertLocalToUTC(1000, false));
        assertEquals(1000, DateTimeZone.UTC.convertLocalToUTC(1000, true));
    }

    @Test
    public void testUTCGetMillisKeepLocal() {
        assertEquals(1000, DateTimeZone.UTC.getMillisKeepLocal(DateTimeZone.UTC, 1000));
    }

    @Test
    public void testUTCIsLocalDateTimeGap() {
        assertFalse(DateTimeZone.UTC.isLocalDateTimeGap(new LocalDateTime(2000, 1, 1, 0, 0)));
    }

    @Test
    public void testUTCIsFixed() {
        assertTrue(DateTimeZone.UTC.isFixed());
    }

    @Test
    public void testUTCNextTransition() {
        assertEquals(0, DateTimeZone.UTC.nextTransition(0));
    }

    @Test
    public void testUTCPreviousTransition() {
        assertEquals(0, DateTimeZone.UTC.previousTransition(0));
    }

    @Test
    public void testUTCToTimeZone() {
        assertEquals("UTC", DateTimeZone.UTC.toTimeZone().getID());
    }

    @Test
    public void testUTCEquals() {
        assertTrue(DateTimeZone.UTC.equals(DateTimeZone.UTC));
        assertFalse(DateTimeZone.UTC.equals(DateTimeZone.forOffsetHours(1)));
    }

    @Test
    public void testUTCHashCode() {
        assertEquals(DateTimeZone.UTC.hashCode(), DateTimeZone.UTC.hashCode());
    }

    @Test
    public void testUTCToString() {
        assertEquals("UTC", DateTimeZone.UTC.toString());
    }

    @Test
    public void testUTCWriteReplace() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(DateTimeZone.UTC);
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = ois.readObject();
        assertEquals(DateTimeZone.UTC, obj);
    }

    @Test
    public void testLondonDSTSpringForward() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long instantBefore = new DateTime(2014, 3, 30, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long instantAfter = new DateTime(2014, 3, 30, 2, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(0, london.getOffset(instantBefore));
        assertEquals(3600000, london.getOffset(instantAfter));
        assertTrue(london.isStandardOffset(instantBefore));
        assertFalse(london.isStandardOffset(instantAfter));
    }

    @Test
    public void testLondonGetOffsetFromLocalGap() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long localGap = new DateTime(2014, 3, 30, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        int offset = london.getOffsetFromLocal(localGap);
        assertEquals(3600000, offset);
    }

    @Test
    public void testLondonConvertLocalToUTCStrictGap() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long localGap = new DateTime(2014, 3, 30, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        try {
            london.convertLocalToUTC(localGap, true);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testLondonConvertLocalToUTCNonStrictGap() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        long localGap = new DateTime(2014, 3, 30, 1, 30, 0, 0, DateTimeZone.UTC).getMillis();
        long utc = london.convertLocalToUTC(localGap, false);
        assertEquals(new DateTime(2014, 3, 30, 2, 30, 0, 0, DateTimeZone.UTC).getMillis(), utc);
    }

    @Test
    public void testLondonGetMillisKeepLocal() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        long instant = new DateTime(2014, 6, 1, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long converted = london.getMillisKeepLocal(paris, instant);
        assertEquals(new DateTime(2014, 6, 1, 12, 0, 0, 0, paris).getMillis(), converted);
    }

    @Test
    public void testConvertUTCToLocalOverflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        try {
            zone.convertUTCToLocal(Long.MAX_VALUE);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
        }
    }

    @Test
    public void testConvertLocalToUTCOverflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        try {
            zone.convertLocalToUTC(Long.MIN_VALUE, false);
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
        }
    }

    @Test
    public void testFixedOffsetZoneCaching() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(3600000);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(3600000);
        assertSame(zone1, zone2);
    }

    @Test
    public void testGetShortNameWithLocale() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        String name = london.getShortName(new DateTime(2014, 6, 1, 12, 0, 0, 0, DateTimeZone.UTC).getMillis(), Locale.UK);
        assertNotNull(name);
    }

    @Test
    public void testGetNameWithLocale() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        String name = london.getName(new DateTime(2014, 6, 1, 12, 0, 0, 0, DateTimeZone.UTC).getMillis(), Locale.UK);
        assertNotNull(name);
    }

    @Test
    public void testGetOffsetReadableInstant() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        Instant instant = new Instant(0);
        assertEquals(7200000, zone.getOffset(instant));
    }

    @Test
    public void testIsLocalDateTimeGapTrue() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        LocalDateTime gap = new LocalDateTime(2014, 3, 30, 1, 30);
        assertTrue(london.isLocalDateTimeGap(gap));
    }

    @Test
    public void testIsLocalDateTimeGapFalse() {
        DateTimeZone london = DateTimeZone.forID("Europe/London");
        LocalDateTime normal = new LocalDateTime(2014, 3, 30, 2, 30);
        assertFalse(london.isLocalDateTimeGap(normal));
    }

    @Test
    public void testConstructorNullID() {
        try {
            new FixedDateTimeZone(null, null, 0, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
}
