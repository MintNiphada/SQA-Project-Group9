package org.joda.time;

import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.tz.FixedDateTimeZone;
import org.joda.time.tz.NameProvider;
import org.joda.time.tz.Provider;
import org.joda.time.tz.UTCProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DateTimeZoneTest {
    
    private static DateTimeZone originalDefault;
    private static Provider originalProvider;
    private static NameProvider originalNameProvider;
    
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
    public void testGetDefaultNotNull() {
        assertNotNull(DateTimeZone.getDefault());
    }
    
    @Test
    public void testSetDefaultValidZone() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        assertEquals(DateTimeZone.UTC, DateTimeZone.getDefault());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultNullThrows() {
        DateTimeZone.setDefault(null);
    }
    
    @Test
    public void testSetGetDefaultRoundTrip() {
        DateTimeZone original = DateTimeZone.getDefault();
        DateTimeZone.setDefault(DateTimeZone.forOffsetHours(1));
        assertNotSame(original, DateTimeZone.getDefault());
        DateTimeZone.setDefault(original);
    }
    
    @Test
    public void testForIDNullReturnsDefault() {
        assertEquals(originalDefault, DateTimeZone.forID(null));
    }
    
 @Test
    public void testForIDUTCReturnsUTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }
    
 @Test
    public void testForIDValidZoneId() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone);
        assertEquals("Europe/London", zone.getID());
    }
    
 @Test(expected = IllegalArgumentException.class)
    public void testForIDInvalidZoneId() {
        DateTimeZone.forID("Invalid/Zone");
    }
    
 @Test
    public void testForIDFixedOffsetPositive() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        assertTrue(zone.isFixed());
        offset(5*60*60*1000 + 30*60*1000, zone.getOffset(0));
    }
    
 @Test
    public void testForIDFixedOffsetNegative() {
        DateTimeZone zone = DateTimeZone.forID("-04:0");
        assertTrue(zone.isFixed());
        offset(-4*60*60*1000, zone.getOffset(0));
    }
    
 @Test
    public void testForIDFixedOffsetZeroReturnsUTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:0"));
    }
    
 @Test
    public void testForOffsetHoursZero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHours(0));
    }
    
 @Test
    public void testForOffsetHoursPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals(2*60*60*1000, zone.getOffset(0));
    }
    
 @Test
    public void testForOffsetHoursNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-5);
        assertEquals(-5*60*60*1000, zone.getOffset(0));
    }
    
 @Test
    public void testForOffsetHoursMinutesZeroBoth() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetHoursMinutes(0,0);
    }
    
 @Test
    public void testForOffsetHoursMinutesPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(1, 30);
        assertEquals((1*60+30)*60*1000, zone.getOffset(0));
    }
    
 @Test
    public void testForOffsetHoursMinutesNegativeHourWithPositiveMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertEquals((-2*60-30)*60*1000, zone.getOffset(0));
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
        DateTimeZone.forOffsetHoursMinutes(nteger.MAX_VALUE, 0);
    }
    
 @Test
    public void testForOffsetMillisZero() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
    }
    
 @Test
    public void testForOffsetMillisPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals(3600000, zone.getOffset(0));
    }
    
 @Test
    public void testForOffsetMillisNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-7200000);
        assertEquals(-7200000, zone.getOffset(0));
    }
    
 @Test
    public void testForTimeZoneNullReturnsDefault() {
        assertEquals(originalDefault, DateTimeZone.forTimeZone(null));
    }
    
 @Test
    public void testForTimeZoneUTC() {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(utc));
    }
    
 @Test
    public void testForTimeZoneValidZone() {
        TimeZone tz = TimeZone.getTimeZone("Europe/Paris");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertNotNull(zone);
        assertEquals("Europe/Paris", zone.getID());
    }
    
 @Test
    public void testForTimeZoneConvertedId() {
        TimeZone est = TimeZone.getTimeZone("EST");
        DateTimeZone zone = DateTimeZone.forTimeZone(est);
        assertNotNull(zone);
 }
    
 @Test
    public void testForTimeZoneGMTPlus() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:0");
        DateTimeZone zone = DateTimeZone.forTimeZone(tz);
        assertTrue(zone.isFixed());
        assertEquals(5*60*60*1000, zone.getOffset(0));
    }
    
 @Test
    public void testGetAvailableIDsNotNull() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertTrue(ids.size() > 0);
    }
    
 @Test
    public void testGetAvailableIDsContainsUTC() {
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
    }
    
 @Test
    public void testSetProviderValid() {
        Provider custom = new UTCProvider();
        DateTimeZone.setProvider(custom);
        assertSame(custom, DateTimeZone.getProvider());
    }
    
 @Test
    public void testSetProviderNullResetsToDefault() {
        DateTimeZone.setProvider(null);
        assertTrue(DateTimeZone.getProvider() instanceof Provider);
    }
    
 @Test
    public void testGetNameProviderDefault() {
        assertNotNull(DateTimeZone.getNameProvider());
    }
    
 @Test
    public void testSetNameProviderValid() {
        NameProvider custom = new DefaultNameProvider();
        DateTimeZone.setNameProvider(custom);
        assertSame(custom, DateTimeZone.getNameProvider());
    }
    
 @Test
    public void testSetNameProviderNullResetsToDefault() {
        DateTimeZone.setNameProvider(null);
        assertTrue(DateTimeZone.getNameProvider() instanceof DefaultNameProvider);
    }
    
 @Test
    public void testGetID() {
        assertEquals("UTC", DateTimeZone.UTC.getID());
    }
    
 @Test
    public void testGetShortNameFixedZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:0", zone.getShortName(0));
    }
    
 @Test
    public void testGetShortNameWithLocaleNull() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:0", zone.getShortName(0, (ocale) null));
    }
    
 @Test
    public void testGetShortNameRealZoneWithName() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String shortName = zone.getShortName(System.currentTimeMillis());
        assertNotNull(shortName);
    }
    
 @Test
    public void testGetNameFixedZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:0", zone.getName(0));
    }
    
 @Test
    public void testGetNameWithLocaleNull() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:0", zone.getName(0, (ocale) null));
    }
    
 @Test
    public void testGetNameRealZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String name = zone.getName(System.currentTimeMillis());
        assertNotNull(name);
    }
    
 @Test
    public void testGetOffsetWithInstantNull() {
        assertEquals(DateTimeZone.UTC.getOffset(0), DateTimeZone.UTC.getOffset((eableInstant) null));
    }
    
 @Test
    public void testGetOffsetWithInstant() {
        MutableDateTime instant = new MutableDateTime(2010, 6, 0, 0,0,0,0, DateTimeZone.UTC);
        assertEquals(0, DateTimeZone.UTC.getOffset(instant));
    }
    
 @Test
    public void testGetStandardOffsetFixed() {
        assertEquals(0, DateTimeZone.UTC.getStandardOffset(0));
    }
    
 @Test
    public void testIsStandardOffsetFixed() {
        assertTrue(DateTimeZone.UTC.isStandardOffset(0));
    }
    
 @Test
    public void testGetOffsetFromLocalFixed() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-3);
        assertEquals(-3*60*60*1000, zone.getOffsetFromLocal(1000000));
    }
    
 @Test
    public void testConvertUTCToLocalFixed() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals(7200000 + 1000000, zone.convertUTCTolocal(1000000));
    }
    
 @Test(expected = ArithmeticException.class)
    public void testConvertUTCToLocalOerflow() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        zone.convertUTCTolocal(long.MAX_VALUE);
    }
    
 @Test
    public void testConvertLocalToUTCStrictFalseFixed() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals(1000000, zone.convertLocalToUTC(1000000+7200000, false));
    }
    
 @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTCStrictTrueGap() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        zone.convertLocalToUtc(zone.nextTransition(0) + 1000, true);
    }
    
 @Test
    public void testConvertLocalToUTCWithOriginalInstant() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals(1000000, zone.convertLocalToUTC(1000000+7200000, false, 1000000));
    }
    
 @Test
    public void testGetMillisKeepLocalSameZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals(1000000, zone.getMillisKeepLoal(zone, 1000000));
    }
    
 @Test
    public void testGetMillisKeepLocalDifferentZone() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(2);
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(3);
        long result = zone1.getMillisKeepLoal(zone2, 1000000);
        assertTrue(result != 1000000);
    }
    
 @Test
    public void testIsLocalDateTimeGapFixed() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertFalse(zone.isLocalDateTimeGap(new LocalDateTime());
    }
    
 @Test
    public void testIsLocalDateTimeGapRealZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long transition = zone.nextTransition(0);
        LocalDateTime ldt = new LocalDateTime(transition, zone).plusMillis(1);
        assertTrue(zone.isLocalDateTimeGap(ldt));
    }
    
 @Test
    public void testAdjustOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        assertEquals(1000000, zone.djustOffset(1000000, false));
    }
    
 @Test
    public void testNextTransitionFixed() {
        assertEquals(0, DateTimeZone.UTC.nextTransition(0));
    }
    
 @Test
    public void testPreviousTransitionFixed() {
        assertEquals(0, DateTimeZone.UTC.previousTransition(0));
    }
    
 @Test
    public void testToTimeZone() {
        TimeZone tz = DateTimeZone.UTC.toTimeZone();
        assertNotNull(tz);
        assertEquals("UTC", tz.getID());
    }
    
 @Test
    public void testEqualsSame() {
        assertTrue(DateTimeZone.UTC.equals(DateTimeZone.UTC));
    }
    
 @Test
    public void testEqualsDifferent() {
        assertFalse(DateTimeZone.UTC.equals(DateTimeZone.forOffsetHours(1)));
    }
    
 @Test
    public void testHashCode() {
        assertEquals(DateTimeZone.UTC.hashCode(), DateTimeZone.UTC.hashCode());
    }
    
 @Test
    public void testToString() {
        assertEquals("UTC", DateTimeZone.UTC.toString());
    }
    
 @Test
    public void testConstructorNullIdThrows() {
        try {
            new TestZone(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }
    
 @Test
    public void testWriteReplace() throws Exception {
        Object stub = new DateTimeZone.Stub("test");
        assertNotNull(stub);
    }

    private static class TestZone extends DateTimeZone {
        public TestZone(String id) {
            super(id);
        }
        public String getNameKey(long instant) { return null; }
        public int getOffset(long instant) { return 0; }
        public int getStandardOffset(long instant) { return 0; }
        public boolean isFixed() { return true; }
        public long nextTransition(long instant) { return instant; }
        public long previousTransition(long instant) { return instant; }
        public boolean equals(Object object) { return object instanceof TestZone; }
    }
}
