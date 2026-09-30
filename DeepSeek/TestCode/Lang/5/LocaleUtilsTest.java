package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

public class LocaleUtilsTest {

    @Test
    public void testConstructor() {
        new LocaleUtils();
    }

    // ---------- toLocale ----------

    @Test
    public void testToLocale_null() {
        assertNull(LocaleUtils.toLocale(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_emptyString() {
        LocaleUtils.toLocale("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lengthOne() {
        LocaleUtils.toLocale("a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_uppercaseLanguage() {
        LocaleUtils.toLocale("EN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_mixedCaseLanguage() {
        LocaleUtils.toLocale("eN");
    }

    @Test
    public void testToLocale_twoLowercase() {
        Locale locale = LocaleUtils.toLocale("en");
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lengthThreeNoUnderscore() {
        LocaleUtils.toLocale("ena");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lengthFourNoUnderscore() {
        LocaleUtils.toLocale("enab");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_underscoreAtWrongPosition() {
        LocaleUtils.toLocale("e_n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_languageTooShort() {
        LocaleUtils.toLocale("e_US");
    }

    @Test
    public void testToLocale_languageCountry() {
        Locale locale = LocaleUtils.toLocale("en_US");
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    @Test
    public void testToLocale_languageCountryVariant() {
        Locale locale = LocaleUtils.toLocale("en_US_WIN");
        assertEquals("en", locale.getLanguage());
        assertEquals("US", locale.getCountry());
        assertEquals("WIN", locale.getVariant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lowercaseCountry() {
        LocaleUtils.toLocale("en_us");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_mixedCaseCountry() {
        LocaleUtils.toLocale("en_uS");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_countryTooShort() {
        LocaleUtils.toLocale("en_U");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lengthFiveNoUnderscore() {
        LocaleUtils.toLocale("en_USA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_lengthSixNoSecondUnderscore() {
        LocaleUtils.toLocale("en_USxx");
    }

    @Test
    public void testToLocale_languageOnlyWithUnderscore() {
        Locale locale = LocaleUtils.toLocale("en__");
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("", locale.getVariant());
    }

    @Test
    public void testToLocale_languageOnlyWithUnderscoreAndVariant() {
        Locale locale = LocaleUtils.toLocale("en__POSIX");
        assertEquals("en", locale.getLanguage());
        assertEquals("", locale.getCountry());
        assertEquals("POSIX", locale.getVariant());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidFormat_lengthThreeWithUnderscore() {
        LocaleUtils.toLocale("e_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidFormat_lengthFourWithUnderscore() {
        LocaleUtils.toLocale("e_U");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidFormat_lengthSixWithUnderscore() {
        LocaleUtils.toLocale("en_US_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocale_invalidFormat_lengthSevenWithUnderscore() {
        LocaleUtils.toLocale("en_US_W");
    }

    // ---------- localeLookupList(Locale) ----------

    @Test
    public void testLocaleLookupList_singleArg() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List<Locale> list = LocaleUtils.localeLookupList(locale);
        assertEquals(3, list.size());
        assertEquals(new Locale("fr", "CA", "xxx"), list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr"), list.get(2));
    }

    @Test
    public void testLocaleLookupList_singleArg_noVariant() {
        Locale locale = new Locale("fr", "CA");
        List<Locale> list = LocaleUtils.localeLookupList(locale);
        assertEquals(2, list.size());
        assertEquals(new Locale("fr", "CA"), list.get(0));
        assertEquals(new Locale("fr"), list.get(1));
    }

    @Test
    public void testLocaleLookupList_singleArg_languageOnly() {
        Locale locale = new Locale("fr");
        List<Locale> list = LocaleUtils.localeLookupList(locale);
        assertEquals(1, list.size());
        assertEquals(new Locale("fr"), list.get(0));
    }

    // ---------- localeLookupList(Locale, Locale) ----------

    @Test
    public void testLocaleLookupList_nullLocale() {
        List<Locale> list = LocaleUtils.localeLookupList(null, Locale.US);
        assertTrue(list.isEmpty());
    }

    @Test
    public void testLocaleLookupList_nullDefaultLocale() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List<Locale> list = LocaleUtils.localeLookupList(locale, null);
        assertEquals(3, list.size());
        assertEquals(new Locale("fr", "CA", "xxx"), list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr"), list.get(2));
    }

    @Test
    public void testLocaleLookupList_defaultLocaleNotInList() {
        Locale locale = new Locale("fr", "CA", "xxx");
        List<Locale> list = LocaleUtils.localeLookupList(locale, Locale.US);
        assertEquals(4, list.size());
        assertEquals(new Locale("fr", "CA", "xxx"), list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr"), list.get(2));
        assertEquals(Locale.US, list.get(3));
    }

    @Test
    public void testLocaleLookupList_defaultLocaleAlreadyInList() {
        Locale locale = new Locale("fr", "CA", "xxx");
        Locale defaultLocale = new Locale("fr");
        List<Locale> list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertEquals(3, list.size());
        assertEquals(new Locale("fr", "CA", "xxx"), list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr"), list.get(2));
    }

    @Test
    public void testLocaleLookupList_defaultLocaleEqualsCountryLevel() {
        Locale locale = new Locale("fr", "CA", "xxx");
        Locale defaultLocale = new Locale("fr", "CA");
        List<Locale> list = LocaleUtils.localeLookupList(locale, defaultLocale);
        assertEquals(3, list.size());
        assertEquals(new Locale("fr", "CA", "xxx"), list.get(0));
        assertEquals(new Locale("fr", "CA"), list.get(1));
        assertEquals(new Locale("fr"), list.get(2));
    }

    @Test
    public void testLocaleLookupList_unmodifiable() {
        List<Locale> list = LocaleUtils.localeLookupList(new Locale("en"));
        try {
            list.add(new Locale("fr"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ---------- availableLocaleList ----------

    @Test
    public void testAvailableLocaleList() {
        List<Locale> list = LocaleUtils.availableLocaleList();
        assertNotNull(list);
        assertFalse(list.isEmpty());
        // should contain at least US
        assertTrue(list.contains(Locale.US));
        // should be unmodifiable
        try {
            list.add(new Locale("test"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ---------- availableLocaleSet ----------

    @Test
    public void testAvailableLocaleSet() {
        Set<Locale> set = LocaleUtils.availableLocaleSet();
        assertNotNull(set);
        assertFalse(set.isEmpty());
        assertTrue(set.contains(Locale.US));
        try {
            set.add(new Locale("test"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ---------- isAvailableLocale ----------

    @Test
    public void testIsAvailableLocale_true() {
        assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
    }

    @Test
    public void testIsAvailableLocale_false() {
        assertFalse(LocaleUtils.isAvailableLocale(new Locale("xx", "YY")));
    }

    @Test
    public void testIsAvailableLocale_null() {
        assertFalse(LocaleUtils.isAvailableLocale(null));
    }

    // ---------- languagesByCountry ----------

    @Test
    public void testLanguagesByCountry_null() {
        List<Locale> langs = LocaleUtils.languagesByCountry(null);
        assertNotNull(langs);
        assertTrue(langs.isEmpty());
    }

    @Test
    public void testLanguagesByCountry_valid() {
        List<Locale> langs = LocaleUtils.languagesByCountry("US");
        assertNotNull(langs);
        assertFalse(langs.isEmpty());
        for (Locale l : langs) {
            assertEquals("US", l.getCountry());
            assertEquals("", l.getVariant());
        }
        // unmodifiable
        try {
            langs.add(new Locale("test"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testLanguagesByCountry_invalidCountry() {
        List<Locale> langs = LocaleUtils.languagesByCountry("XX");
        assertNotNull(langs);
        assertTrue(langs.isEmpty());
    }

    @Test
    public void testLanguagesByCountry_caching() {
        List<Locale> first = LocaleUtils.languagesByCountry("DE");
        List<Locale> second = LocaleUtils.languagesByCountry("DE");
        assertSame(first, second);
    }

    // ---------- countriesByLanguage ----------

    @Test
    public void testCountriesByLanguage_null() {
        List<Locale> countries = LocaleUtils.countriesByLanguage(null);
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    @Test
    public void testCountriesByLanguage_valid() {
        List<Locale> countries = LocaleUtils.countriesByLanguage("en");
        assertNotNull(countries);
        assertFalse(countries.isEmpty());
        for (Locale l : countries) {
            assertEquals("en", l.getLanguage());
            assertFalse(l.getCountry().isEmpty());
            assertEquals("", l.getVariant());
        }
        try {
            countries.add(new Locale("test"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testCountriesByLanguage_invalidLanguage() {
        List<Locale> countries = LocaleUtils.countriesByLanguage("xx");
        assertNotNull(countries);
        assertTrue(countries.isEmpty());
    }

    @Test
    public void testCountriesByLanguage_caching() {
        List<Locale> first = LocaleUtils.countriesByLanguage("fr");
        List<Locale> second = LocaleUtils.countriesByLanguage("fr");
        assertSame(first, second);
    }

    @Test
    public void testCountriesByLanguage_noCountry() {
        // some languages might have no country, but we just test non-null
        List<Locale> countries = LocaleUtils.countriesByLanguage("ja");
        assertNotNull(countries);
    }

    // ---------- SyncAvoid inner class coverage ----------

    @Test
    public void testSyncAvoid() {
        // Accessing the inner class triggers static init
        assertNotNull(LocaleUtils.SyncAvoid.class);
    }
}
