package org.joda.time;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

import org.joda.time.chrono.BuddhistChronology;
import org.joda.time.chrono.CopticChronology;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PartialTest {

    private static final DateTimeZone PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone LONDON = DateTimeZone.forID("Europe/London");
    private static final Chronology ISO_UTC = ISOChronology.getInstanceUTC();
    private static final Chronology GREGORIAN_UTC = GregorianChronology.getInstanceUTC();
    private static final Chronology COPTIC_UTC = CopticChronology.getInstanceUTC();
    private static final Chronology BUDDHIST_UTC = BuddhistChronology.getInstanceUTC();

    private DateTimeZone defaultZone;
    private Locale defaultLocale;

    @Before
    public void setUp() throws Exception {
        defaultZone = DateTimeZone.getDefault();
        defaultLocale = Locale.getDefault();
        DateTimeZone.setDefault(LONDON);
        Locale.setDefault(Locale.UK);
    }

    @After
    public void tearDown() throws Exception {
        DateTimeZone.setDefault(defaultZone);
        Locale.setDefault(defaultLocale);
    }

    @Test
    public void testConstructor_Empty() {
        Partial test = new Partial();
        Assert.assertEquals(0, test.size());
        Assert.assertEquals(ISO_UTC, test.getChronology());
        Assert.assertEquals(0, test.getFieldTypes().length);
        Assert.assertEquals(0, test.getValues().length);
    }

    @Test
    public void testConstructor_Chronology() {
        Partial test = new Partial(GREGORIAN_UTC);
        Assert.assertEquals(0, test.size());
        Assert.assertEquals(GREGORIAN_UTC, test.getChronology());

        test = new Partial((Chronology) null);
        Assert.assertEquals(0, test.size());
        Assert.assertEquals(ISO_UTC, test.getChronology());
    }

    @Test
    public void testConstructor_Type_int() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        Assert.assertEquals(1, test.size());
        Assert.assertEquals(ISO_UTC, test.getChronology());
        Assert.assertEquals(DateTimeFieldType.year(), test.getFieldType(0));
        Assert.assertEquals(2005, test.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Type_int_nullType() {
        new Partial((DateTimeFieldType) null, 2005);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Type_int_invalidValue() {
        new Partial(DateTimeFieldType.dayOfMonth(), 32);
    }

    @Test
    public void testConstructor_Type_int_Chronology() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005, GREGORIAN_UTC);
        Assert.assertEquals(1, test.size());
        Assert.assertEquals(GREGORIAN_UTC, test.getChronology());
        Assert.assertEquals(DateTimeFieldType.year(), test.getFieldType(0));
        Assert.assertEquals(2005, test.getValue(0));

        test = new Partial(DateTimeFieldType.year(), 2005, null);
        Assert.assertEquals(ISO_UTC, test.getChronology());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Type_int_Chronology_nullType() {
        new Partial(null, 2005, GREGORIAN_UTC);
    }

    @Test
    public void testConstructor_Types_Values() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 2005, 6, 25 };
        Partial test = new Partial(types, values);
        Assert.assertEquals(3, test.size());
        Assert.assertEquals(ISO_UTC, test.getChronology());
        Assert.assertEquals(DateTimeFieldType.year(), test.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.monthOfYear(), test.getFieldType(1));
        Assert.assertEquals(DateTimeFieldType.dayOfMonth(), test.getFieldType(2));
        Assert.assertEquals(2005, test.getValue(0));
        Assert.assertEquals(6, test.getValue(1));
        Assert.assertEquals(25, test.getValue(2));
    }

    @Test
    public void testConstructor_EmptyArrays() {
        Partial test = new Partial(new DateTimeFieldType[0], new int[0]);
        Assert.assertEquals(0, test.size());
        Assert.assertEquals(ISO_UTC, test.getChronology());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Types_Values_nullTypes() {
        new Partial((DateTimeFieldType[]) null, new int[] { 2005 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Types_Values_nullValues() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.year() }, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Types_Values_mismatchedLengths() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.year() }, new int[] { 2005, 6 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Types_Values_nullElement() {
        new Partial(new DateTimeFieldType[] { DateTimeFieldType.year(), null }, new int[] { 2005, 6 });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Types_Values_wrongOrder() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.year()
        };
        int[] values = new int[] { 6, 2005 };
        new Partial(types, values);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Types_Values_duplicate() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.year()
        };
        int[] values = new int[] { 2005, 2005 };
        new Partial(types, values);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Types_Values_duplicateSameRange() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 5, 5 };
        new Partial(types, values);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Types_Values_sameUnitDifferentRangeWrongOrder() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfWeek(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 1, 15 };
        new Partial(types, values);
    }

    @Test
    public void testConstructor_Types_Values_sameUnitDifferentRangeCorrectOrder() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfYear(),
            DateTimeFieldType.dayOfMonth(),
            DateTimeFieldType.dayOfWeek()
        };
        int[] values = new int[] { 100, 10, 3 };
        Partial p = new Partial(types, values);
        Assert.assertEquals(3, p.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_Types_Values_sameUnitNullRange() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.yearOfEra()
        };
        int[] values = new int[] { 2005, 2005 };
        new Partial(types, values);
    }

    @Test
    public void testConstructor_ReadablePartial() {
        LocalDate date = new LocalDate(2005, 6, 25);
        Partial test = new Partial(date);
        Assert.assertEquals(3, test.size());
        Assert.assertEquals(DateTimeFieldType.year(), test.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.monthOfYear(), test.getFieldType(1));
        Assert.assertEquals(DateTimeFieldType.dayOfMonth(), test.getFieldType(2));
        Assert.assertEquals(2005, test.getValue(0));
        Assert.assertEquals(6, test.getValue(1));
        Assert.assertEquals(25, test.getValue(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_ReadablePartial_null() {
        new Partial((ReadablePartial) null);
    }

    @Test
    public void testPackagePrivateConstructors() {
        Partial base = new Partial(DateTimeFieldType.year(), 2005);
        Partial copy = new Partial(base, new int[] { 2010 });
        Assert.assertEquals(2010, copy.getValue(0));

        Partial copy2 = new Partial(GREGORIAN_UTC, new DateTimeFieldType[] { DateTimeFieldType.year() }, new int[] { 2015 });
        Assert.assertEquals(GREGORIAN_UTC, copy2.getChronology());
        Assert.assertEquals(2015, copy2.getValue(0));
    }

    @Test
    public void testGetField() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        Assert.assertEquals(ISO_UTC.year(), test.getField(0, ISO_UTC));
        Assert.assertEquals(GREGORIAN_UTC.year(), test.getField(0, GREGORIAN_UTC));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetField_outOfBounds() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        test.getField(1, ISO_UTC);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFieldType_outOfBounds() {
        Partial test = new Partial();
        test.getFieldType(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValue_outOfBounds() {
        Partial test = new Partial();
        test.getValue(0);
    }

    @Test
    public void testWithChronologyRetainFields() {
        Partial base = new Partial(DateTimeFieldType.year(), 2005, ISO_UTC);
        Partial same = base.withChronologyRetainFields(ISO_UTC);
        Assert.assertSame(base, same);

        Partial sameNull = base.withChronologyRetainFields(null);
        Assert.assertSame(base, sameNull);

        Partial diff = base.withChronologyRetainFields(BUDDHIST_UTC);
        Assert.assertEquals(BUDDHIST_UTC, diff.getChronology());
        Assert.assertEquals(2005, diff.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithChronologyRetainFields_invalid() {
        Partial base = new Partial(DateTimeFieldType.dayOfMonth(), 30, ISO_UTC);
        base.withChronologyRetainFields(CopticChronology.getInstanceUTC());
    }

    @Test
    public void testWith() {
        Partial p = new Partial();
        p = p.with(DateTimeFieldType.hourOfDay(), 12);
        Assert.assertEquals(1, p.size());
        Assert.assertEquals(12, p.getValue(0));

        p = p.with(DateTimeFieldType.minuteOfHour(), 30);
        Assert.assertEquals(2, p.size());
        Assert.assertEquals(DateTimeFieldType.hourOfDay(), p.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.minuteOfHour(), p.getFieldType(1));
        Assert.assertEquals(12, p.getValue(0));
        Assert.assertEquals(30, p.getValue(1));

        p = p.with(DateTimeFieldType.year(), 2020);
        Assert.assertEquals(3, p.size());
        Assert.assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.hourOfDay(), p.getFieldType(1));
        Assert.assertEquals(DateTimeFieldType.minuteOfHour(), p.getFieldType(2));
        Assert.assertEquals(2020, p.getValue(0));

        Partial same = p.with(DateTimeFieldType.year(), 2020);
        Assert.assertSame(p, same);

        Partial modified = p.with(DateTimeFieldType.year(), 2021);
        Assert.assertEquals(2021, modified.getValue(0));
        Assert.assertEquals(3, modified.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWith_nullType() {
        Partial p = new Partial();
        p.with(null, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWith_invalidValue() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.with(DateTimeFieldType.hourOfDay(), 25);
    }

    @Test
    public void testWithout() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        };
        int[] values = new int[] { 2020, 5, 10 };
        Partial p = new Partial(types, values);

        Partial p2 = p.without(DateTimeFieldType.monthOfYear());
        Assert.assertEquals(2, p2.size());
        Assert.assertEquals(DateTimeFieldType.year(), p2.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.dayOfMonth(), p2.getFieldType(1));
        Assert.assertEquals(2020, p2.getValue(0));
        Assert.assertEquals(10, p2.getValue(1));

        Partial p3 = p2.without(DateTimeFieldType.hourOfDay());
        Assert.assertSame(p2, p3);

        Partial p4 = p.without(DateTimeFieldType.year());
        Assert.assertEquals(2, p4.size());
        Assert.assertEquals(DateTimeFieldType.monthOfYear(), p4.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.dayOfMonth(), p4.getFieldType(1));

        Partial p5 = p.without(DateTimeFieldType.dayOfMonth());
        Assert.assertEquals(2, p5.size());
        Assert.assertEquals(DateTimeFieldType.year(), p5.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.monthOfYear(), p5.getFieldType(1));
    }

    @Test
    public void testWithField() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        Partial same = p.withField(DateTimeFieldType.year(), 2020);
        Assert.assertSame(p, same);

        Partial modified = p.withField(DateTimeFieldType.year(), 2021);
        Assert.assertEquals(2021, modified.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithField_unsupported() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.withField(DateTimeFieldType.dayOfMonth(), 5);
    }

    @Test
    public void testWithFieldAdded() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.minuteOfHour()
        };
        Partial p = new Partial(types, new int[] { 10, 20 });

        Partial same = p.withFieldAdded(DurationFieldType.minutes(), 0);
        Assert.assertSame(p, same);

        Partial added = p.withFieldAdded(DurationFieldType.minutes(), 50);
        Assert.assertEquals(11, added.getValue(0));
        Assert.assertEquals(10, added.getValue(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAdded_unsupported() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.withFieldAdded(DurationFieldType.days(), 1);
    }

    @Test
    public void testWithFieldAddWrapped() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.minuteOfHour()
        };
        Partial p = new Partial(types, new int[] { 10, 20 });

        Partial same = p.withFieldAddWrapped(DurationFieldType.minutes(), 0);
        Assert.assertSame(p, same);

        Partial wrapped = p.withFieldAddWrapped(DurationFieldType.minutes(), 50);
        Assert.assertEquals(10, wrapped.getValue(0));
        Assert.assertEquals(10, wrapped.getValue(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithFieldAddWrapped_unsupported() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        p.withFieldAddWrapped(DurationFieldType.days(), 1);
    }

    @Test
    public void testWithPeriodAdded() {
        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.hourOfDay(),
            DateTimeFieldType.minuteOfHour()
        };
        Partial p = new Partial(types, new int[] { 10, 20 });

        Assert.assertSame(p, p.withPeriodAdded(null, 1));
        Assert.assertSame(p, p.withPeriodAdded(Period.hours(1), 0));

        Period period = new Period(1, 15, 0, 0); // 1 hour, 15 minutes
        Partial result = p.withPeriodAdded(period, 1);
        Assert.assertEquals(11, result.getValue(0));
        Assert.assertEquals(35, result.getValue(1));

        Period periodWithUnusedFields = new Period().withDays(2).withHours(1).withMinutes(5);
        result = p.withPeriodAdded(periodWithUnusedFields, 2);
        Assert.assertEquals(12, result.getValue(0));
        Assert.assertEquals(30, result.getValue(1));
    }

    @Test
    public void testPlus_Minus() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        Partial plus = p.plus(Period.hours(2));
        Assert.assertEquals(12, plus.getValue(0));

        Partial minus = p.minus(Period.hours(3));
        Assert.assertEquals(7, minus.getValue(0));

        Assert.assertSame(p, p.plus(null));
        Assert.assertSame(p, p.minus(null));
    }

    @Test
    public void testProperty() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = p.property(DateTimeFieldType.monthOfYear());
        Assert.assertNotNull(prop);
        Assert.assertEquals(6, prop.get());
        Assert.assertSame(p, prop.getPartial());
        Assert.assertSame(p, prop.getReadablePartial());
        Assert.assertEquals(ISO_UTC.monthOfYear(), prop.getField());

        Partial pAdded = prop.addToCopy(2);
        Assert.assertEquals(8, pAdded.getValue(0));

        Partial pWrapped = prop.addWrapFieldToCopy(8);
        Assert.assertEquals(2, pWrapped.getValue(0));

        Partial pSetInt = prop.setCopy(12);
        Assert.assertEquals(12, pSetInt.getValue(0));

        Partial pSetTextLocale = prop.setCopy("December", Locale.UK);
        Assert.assertEquals(12, pSetTextLocale.getValue(0));

        Partial pSetText = prop.setCopy("11");
        Assert.assertEquals(11, pSetText.getValue(0));

        Partial pMax = prop.withMaximumValue();
        Assert.assertEquals(12, pMax.getValue(0));

        Partial pMin = prop.withMinimumValue();
        Assert.assertEquals(1, pMin.getValue(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testProperty_unsupported() {
        Partial p = new Partial(DateTimeFieldType.monthOfYear(), 6);
        p.property(DateTimeFieldType.year());
    }

    @Test
    public void testIsMatch_Instant() {
        DateTime dt = new DateTime(2020, 5, 12, 10, 30, 0, 0, PARIS);

        Partial pMatch = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 5)
                .with(DateTimeFieldType.dayOfMonth(), 12);
        Assert.assertTrue(pMatch.isMatch(dt));

        Partial pNoMatch = new Partial(DateTimeFieldType.year(), 2019);
        Assert.assertFalse(pNoMatch.isMatch(dt));

        Partial empty = new Partial();
        Assert.assertTrue(empty.isMatch((ReadableInstant) null));
    }

    @Test
    public void testIsMatch_Partial() {
        LocalDate date = new LocalDate(2020, 5, 12);

        Partial pMatch = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.monthOfYear(), 5);
        Assert.assertTrue(pMatch.isMatch(date));

        Partial pNoMatch = new Partial(DateTimeFieldType.year(), 2019);
        Assert.assertFalse(pNoMatch.isMatch(date));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsMatch_Partial_null() {
        Partial p = new Partial(DateTimeFieldType.year(), 2020);
        p.isMatch((ReadablePartial) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsMatch_Partial_missingFieldInTarget() {
        Partial p = new Partial(DateTimeFieldType.hourOfDay(), 10);
        LocalDate date = new LocalDate(2020, 5, 12);
        p.isMatch(date);
    }

    @Test
    public void testGetFormatter() {
        Partial empty = new Partial();
        Assert.assertNull(empty.getFormatter());

        Partial ymd = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        }, new int[] { 2020, 5, 12 });
        Assert.assertNotNull(ymd.getFormatter());
        // Call twice to test caching branch
        Assert.assertNotNull(ymd.getFormatter());

        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfWeek(),
            DateTimeFieldType.dayOfMonth()
        };
        Partial custom = new Partial(types, new int[] { 3, 12 });
        // dayOfWeek + dayOfMonth may have no exact single ISO pattern
        custom.getFormatter();
    }

    @Test
    public void testToString() {
        Partial empty = new Partial();
        Assert.assertEquals("[]", empty.toString());

        Partial ymd = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        }, new int[] { 2020, 5, 12 });
        Assert.assertEquals("2020-05-12", ymd.toString());

        DateTimeFieldType[] types = new DateTimeFieldType[] {
            DateTimeFieldType.dayOfWeek(),
            DateTimeFieldType.dayOfMonth()
        };
        Partial custom = new Partial(types, new int[] { 3, 12 });
        Assert.assertEquals("[dayOfWeek=3, dayOfMonth=12]", custom.toString());
    }

    @Test
    public void testToStringList() {
        Partial empty = new Partial();
        Assert.assertEquals("[]", empty.toStringList());

        Partial ymd = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear()
        }, new int[] { 2020, 5 });
        Assert.assertEquals("[year=2020, monthOfYear=5]", ymd.toStringList());
    }

    @Test
    public void testToString_Pattern() {
        Partial ymd = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        }, new int[] { 2020, 5, 12 });

        Assert.assertEquals("2020-05-12", ymd.toString((String) null));
        Assert.assertEquals("12/05/2020", ymd.toString("dd/MM/yyyy"));
    }

    @Test
    public void testToString_Pattern_Locale() {
        Partial ymd = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear(),
            DateTimeFieldType.dayOfMonth()
        }, new int[] { 2020, 5, 12 });

        Assert.assertEquals("2020-05-12", ymd.toString(null, Locale.FRENCH));
        Assert.assertEquals("12-mai-2020", ymd.toString("dd-MMM-yyyy", Locale.FRENCH));
        Assert.assertEquals("12-May-2020", ymd.toString("dd-MMM-yyyy", Locale.UK));
    }

    @Test
    public void testSerialization() throws Exception {
        Partial original = new Partial(new DateTimeFieldType[] {
            DateTimeFieldType.year(),
            DateTimeFieldType.monthOfYear()
        }, new int[] { 2020, 5 });

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Partial result = (Partial) ois.readObject();
        ois.close();

        Assert.assertEquals(original, result);
        Assert.assertEquals(original.getChronology(), result.getChronology());
        Assert.assertEquals(original.size(), result.size());
        Assert.assertEquals(original.getValue(0), result.getValue(0));
        Assert.assertEquals(original.getValue(1), result.getValue(1));
    }

    @Test
    public void testPropertySerialization() throws Exception {
        Partial partial = new Partial(DateTimeFieldType.monthOfYear(), 6);
        Partial.Property prop = partial.property(DateTimeFieldType.monthOfYear());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(prop);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Partial.Property result = (Partial.Property) ois.readObject();
        ois.close();

        Assert.assertEquals(prop.get(), result.get());
        Assert.assertEquals(prop.getField(), result.getField());
    }

    @Test
    public void testWithInsertionOrderingEdgeCases() {
        // Test inserting field between existing fields
        Partial p = new Partial(DateTimeFieldType.year(), 2020)
                .with(DateTimeFieldType.dayOfMonth(), 15);
        p = p.with(DateTimeFieldType.monthOfYear(), 6);
        Assert.assertEquals(3, p.size());
        Assert.assertEquals(DateTimeFieldType.year(), p.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.monthOfYear(), p.getFieldType(1));
        Assert.assertEquals(DateTimeFieldType.dayOfMonth(), p.getFieldType(2));

        // Test inserting field before all
        Partial p2 = new Partial(DateTimeFieldType.monthOfYear(), 6);
        p2 = p2.with(DateTimeFieldType.year(), 2020);
        Assert.assertEquals(DateTimeFieldType.year(), p2.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.monthOfYear(), p2.getFieldType(1));

        // Test inserting field after all
        Partial p3 = new Partial(DateTimeFieldType.year(), 2020);
        p3 = p3.with(DateTimeFieldType.monthOfYear(), 6);
        Assert.assertEquals(DateTimeFieldType.year(), p3.getFieldType(0));
        Assert.assertEquals(DateTimeFieldType.monthOfYear(), p3.getFieldType(1));
    }

    @Test
    public void testEqualsAndHashCode() {
        Partial p1 = new Partial(DateTimeFieldType.year(), 2020);
        Partial p2 = new Partial(DateTimeFieldType.year(), 2020);
        Partial p3 = new Partial(DateTimeFieldType.year(), 2021);
        Partial p4 = new Partial(DateTimeFieldType.monthOfYear(), 6);

        Assert.assertTrue(p1.equals(p1));
        Assert.assertTrue(p1.equals(p2));
        Assert.assertFalse(p1.equals(p3));
        Assert.assertFalse(p1.equals(p4));
        Assert.assertFalse(p1.equals(null));
        Assert.assertFalse(p1.equals("2020"));

        Assert.assertEquals(p1.hashCode(), p2.hashCode());
    }
}
