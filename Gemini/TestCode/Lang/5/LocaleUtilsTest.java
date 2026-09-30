package org.apache.commons.lang3;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LocaleUtilsTest {

    private static final Locale LOCALE_EN = new Locale("en", "");
    private static final Locale LOCALE_EN_US = new Locale("en", "US");
    private static final Locale LOCALE_EN_GB = new Locale("en", "GB");
    private static final Locale LOCALE_FR = new Locale("fr", "");
    private static final Locale LOCALE_FR_CA = new Locale("fr", "CA");
    private static final Locale LOCALE_FR_CA_XXX = new Locale("fr", "CA", "xxx");

    @Test
    public void testConstructor() {
        assertNotNull(new LocaleUtils());
    }

    @Test
    public void testToLocale_Valid() {
        assertNull(LocaleUtils.toLocale(null));

        assertEquals(new Locale("en", ""), LocaleUtils.toLocale("en"));
        assertEquals(new Locale("fr", ""), LocaleUtils.toLocale("fr"));
        assertEquals(new Locale("de", ""), LocaleUtils.toLocale("de"));

        assertEquals(new Locale("en", "GB"), LocaleUtils.toLocale("en_GB"));
        assertEquals(new Locale("en", "US"), LocaleUtils.toLocale("en_US"));

        assertEquals(new Locale("en", "GB", "xxx"), LocaleUtils.toLocale("en_GB_xxx"));
        assertEquals(new Locale("en", "GB", "POSIX"), LocaleUtils.toLocale("en_GB_POSIX"));
        assertEquals(new Locale("en", "GB", "a_b_c"), LocaleUtils.toLocale("en_GB_a_b_c"));

        assertEquals(new Locale("en", "", "POSIX"), LocaleUtils.toLocale("en__POSIX"));
        assertEquals(new Locale("en", "", "xxx"), LocaleUtils.toLocale("en__xxx"));
        assertEquals(new Locale("en", "", ""), LocaleUtils.toLocale("en__"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidEmpty() {
        LocaleUtils.toLocale("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidLength1() {
        LocaleUtils.toLocale("a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidUppercaseLanguage1() {
        LocaleUtils.toLocale("En");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidUppercaseLanguage2() {
        LocaleUtils.toLocale("eN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidNumericLanguage() {
        LocaleUtils.toLocale("11");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidLength3() {
        LocaleUtils.toLocale("eng");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidLength4() {
        LocaleUtils.toLocale("en_g");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidSeparator1() {
        LocaleUtils.toLocale("en-GB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidCountryLowercase1() {
        LocaleUtils.toLocale("en_gb");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidCountryLowercase2() {
        LocaleUtils.toLocale("en_Gb");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidCountryLowercase3() {
        LocaleUtils.toLocale("en_gB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidCountryNumeric() {
        LocaleUtils.toLocale("en_G1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidLength6() {
        LocaleUtils.toLocale("en_GB_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_InvalidSeparator2() {
        LocaleUtils.toLocale("en_GB-xxx");
    }

    @Test
    public void testLocaleLookupList_Locale() {
        List<Locale> list = LocaleUtils.localeLookupList(null);
        assertNotNull(list);
        assertEquals(0, list.size());

        list = LocaleUtils.localeLookupList(LOCALE_EN);
        assertEquals(1, list.size());
        assertEquals(LOCALE_EN, list.get(0));

        list = LocaleUtils.localeLookupList(LOCALE_EN_US);
        assertEquals(2, list.size());
        assertEquals(LOCALE_EN_US, list.get(0));
        assertEquals(LOCALE_EN, list.get(1));

        list = LocaleUtils.localeLookupList(LOCALE_FR_CA_XXX);
        assertEquals(3, list.size());
        assertEquals(LOCALE_FR_CA_XXX, list.get(0));
        assertEquals(LOCALE_FR_CA, list.get(1));
        assertEquals(LOCALE_FR, list.get(2));

        Locale localeWithVariantNoCountry = new Locale("en", "", "POSIX");
        list = LocaleUtils.localeLookupList(localeWithVariantNoCountry);
        assertEquals(2, list.size());
        assertEquals(localeWithVariantNoCountry, list.get(0));
        assertEquals(LOCALE_EN, list.get(1));
    }

    @Test
    public void testLocaleLookupList_LocaleLocale() {
        List<Locale> list = LocaleUtils.localeLookupList(null, LOCALE_EN);
        assertNotNull(list);
        assertEquals(0, list.size());

        list = LocaleUtils.localeLookupList(LOCALE_EN, null);
        assertEquals(2, list.size());
        assertEquals(LOCALE_EN, list.get(0));
        assertNull(list.get(1));

        list = LocaleUtils.localeLookupList(LOCALE_EN, LOCALE_EN);
        assertEquals(1, list.size());
        assertEquals(LOCALE_EN, list.get(0));

        list = LocaleUtils.localeLookupList(LOCALE_EN_US, LOCALE_EN_US);
        assertEquals(2, list.size());
        assertEquals(LOCALE_EN_US, list.get(0));
        assertEquals(LOCALE_EN, list.get(1));

        list = LocaleUtils.localeLookupList(LOCALE_FR_CA_XXX, LOCALE_EN);
        assertEquals(4, list.size());
        assertEquals(LOCALE_FR_CA_XXX, list.get(0));
        assertEquals(LOCALE_FR_CA, list.get(1));
        assertEquals(LOCALE_FR, list.get(2));
        assertEquals(LOCALE_EN, list.get(3));

        list = LocaleUtils.localeLookupList(LOCALE_FR_CA_XXX, LOCALE_FR_CA);
        assertEquals(3, list.size());
        assertEquals(LOCALE_FR_CA_XXX, list.get(0));
        assertEquals(LOCALE_FR_CA, list.get(1));
        assertEquals(LOCALE_FR, list.get(2));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLocaleLookupList_Unmodifiable() {
        List<Locale> list = LocaleUtils.localeLookupList(LOCALE_EN);
        list.add(LOCALE_FR);
    }

    @Test
    public void testAvailableLocaleList() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertTrue(list.size() > 0);
        assertTrue(list.contains(Locale.US));

        List<Locale> list2 = LocaleUtils.availableLocaleList();
        assertSame(list, list2);

        try {
            list.add(new Locale("test"));
            fail("availableLocaleList should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testAvailableLocaleSet() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertTrue(set.size() > 0);
        assertTrue(set.contains(Locale.US));

        Set<Locale> set2 = LocaleUtils.availableLocaleSet();
        assertSame(set, set2);

        try {
            set.add(new Locale("test"));
            fail("availableLocaleSet should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testIsAvailableLocale() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        assertTrue(LocaleUtils.isAvailableLocale(Locale.GERMANY));
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("zzz", "ZZ")));
    }

    @Test
    public void testLanguagesByCountry() {
        List<Locale> listNull = LocaleUtils.languagesByCountry(null);
        assertNotNull(listNull);
        assertEquals(0, listNull.size());

        List<Locale> listUS = LocaleUtils.languagesByCountry("US");
        assertNotNull(listUS);
        assertTrue(listUS.contains(Locale.US));
        for (Locale locale : listUS) {
            assertEquals("US", locale.getCountry());
            assertTrue(locale.getVariant().isEmpty());
        }

        List<Locale> listUSCached = LocaleUtils.languagesByCountry("US");
        assertSame(listUS, listUSCached);

        List<Locale> listUnknown = LocaleUtils.languagesByCountry("ZZ");
        assertNotNull(listUnknown);
        assertEquals(0, listUnknown.size());

        try {
            listUS.add(Locale.UK);
            fail("languagesByCountry should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }

    @Test
    public void testCountriesByLanguage() {
        List<Locale> listNull = LocaleUtils.countriesByLanguage(null);
        assertNotNull(listNull);
        assertEquals(0, listNull.size());

        List<Locale> listEn = LocaleUtils.countriesByLanguage("en");
        assertNotNull(listEn);
        assertTrue(listEn.contains(Locale.US));
        assertTrue(listEn.contains(Locale.UK));
        for (Locale locale : listEn) {
            assertEquals("en", locale.getLanguage());
            assertFalse(locale.getCountry().isEmpty());
            assertTrue(locale.getVariant().isEmpty());
        }

        List<Locale> listEnCached = LocaleUtils.countriesByLanguage("en");
        assertSame(listEn, listEnCached);

        List<Locale> listUnknown = LocaleUtils.countriesByLanguage("zz");
        assertNotNull(listUnknown);
        assertEquals(0, listUnknown.size());

        try {
            listEn.add(Locale.GERMANY);
            fail("countriesByLanguage should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }
    }
}
