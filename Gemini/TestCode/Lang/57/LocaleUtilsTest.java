package org.apache.commons.lang;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocaleUtilsTest {

    @Before
    public void setUp() {
        LocaleUtils.availableLocaleSet();
    }

    @Test
    public void testConstructor() {
        Assert.assertNotNull(new LocaleUtils());
        Assert.assertTrue(Modifier.isPublic(LocaleUtils.class.getConstructors()[0].getModifiers()));
    }

    @Test
    public void testToLocaleValid() {
        Assert.assertNull(LocaleUtils.toLocale(null));
        
        Locale loc = LocaleUtils.toLocale("en");
        Assert.assertEquals(new Locale("en", ""), loc);
        Assert.assertEquals("en", loc.getLanguage());
        Assert.assertEquals("", loc.getCountry());
        Assert.assertEquals("", loc.getVariant());

        loc = LocaleUtils.toLocale("en_GB");
        Assert.assertEquals(new Locale("en", "GB"), loc);
        Assert.assertEquals("en", loc.getLanguage());
        Assert.assertEquals("GB", loc.getCountry());
        Assert.assertEquals("", loc.getVariant());

        loc = LocaleUtils.toLocale("en_GB_xxx");
        Assert.assertEquals(new Locale("en", "GB", "xxx"), loc);
        Assert.assertEquals("en", loc.getLanguage());
        Assert.assertEquals("GB", loc.getCountry());
        Assert.assertEquals("xxx", loc.getVariant());

        loc = LocaleUtils.toLocale("en_GB_");
        Assert.assertEquals(new Locale("en", "GB", ""), loc);
        Assert.assertEquals("en", loc.getLanguage());
        Assert.assertEquals("GB", loc.getCountry());
        Assert.assertEquals("", loc.getVariant());

        loc = LocaleUtils.toLocale("fr_FR_POSIX");
        Assert.assertEquals(new Locale("fr", "FR", "POSIX"), loc);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleEmpty() {
        LocaleUtils.toLocale("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleLength1() {
        LocaleUtils.toLocale("a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleLength3() {
        LocaleUtils.toLocale("eng");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleLength4() {
        LocaleUtils.toLocale("engl");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleLength6() {
        LocaleUtils.toLocale("en_USA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageChar0Low() {
        LocaleUtils.toLocale("`a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageChar0High() {
        LocaleUtils.toLocale("{a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageChar1Low() {
        LocaleUtils.toLocale("a`");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageChar1High() {
        LocaleUtils.toLocale("a{");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageUpper() {
        LocaleUtils.toLocale("EN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageMixed() {
        LocaleUtils.toLocale("En");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidSeparatorPos2() {
        LocaleUtils.toLocale("en-GB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidCountryChar3Low() {
        LocaleUtils.toLocale("en_@B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidCountryChar3High() {
        LocaleUtils.toLocale("en_[B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidCountryChar4Low() {
        LocaleUtils.toLocale("en_G@");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidCountryChar4High() {
        LocaleUtils.toLocale("en_G[");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidCountryLower() {
        LocaleUtils.toLocale("en_gb");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidSeparatorPos5() {
        LocaleUtils.toLocale("en_GB-xxx");
    }

    @Test
    public void testLocaleLookupListDefault() {
        List list = LocaleUtils.localeLookupList(null);
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());

        list = LocaleUtils.localeLookupList(new Locale("en", ""));
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(new Locale("en", ""), list.get(0));

        list = LocaleUtils.localeLookupList(new Locale("en", "GB"));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(new Locale("en", "GB"), list.get(0));
        Assert.assertEquals(new Locale("en", ""), list.get(1));

        list = LocaleUtils.localeLookupList(new Locale("en", "GB", "xxx"));
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(new Locale("en", "GB", "xxx"), list.get(0));
        Assert.assertEquals(new Locale("en", "GB"), list.get(1));
        Assert.assertEquals(new Locale("en", ""), list.get(2));
    }

    @Test
    public void testLocaleLookupListWithDefaultLocale() {
        List list = LocaleUtils.localeLookupList(null, Locale.ENGLISH);
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());

        list = LocaleUtils.localeLookupList(new Locale("en", "GB"), Locale.US);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(new Locale("en", "GB"), list.get(0));
        Assert.assertEquals(new Locale("en", ""), list.get(1));
        Assert.assertEquals(Locale.US, list.get(2));

        list = LocaleUtils.localeLookupList(new Locale("en", "GB"), new Locale("en", "GB"));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(new Locale("en", "GB"), list.get(0));
        Assert.assertEquals(new Locale("en", ""), list.get(1));

        list = LocaleUtils.localeLookupList(new Locale("en", "GB"), new Locale("en"));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(new Locale("en", "GB"), list.get(0));
        Assert.assertEquals(new Locale("en", ""), list.get(1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLocaleLookupListUnmodifiable() {
        List list = LocaleUtils.localeLookupList(Locale.ENGLISH);
        list.add(Locale.FRENCH);
    }

    @Test
    public void testAvailableLocaleList() {
        List list = LocaleUtils.availableLocaleList();
        Assert.assertNotNull(list);
        Assert.assertTrue(list.size() > 0);
        Assert.assertEquals(LocaleUtils.availableLocaleList(), list);

        Locale[] jdkLocales = Locale.getAvailableLocales();
        Assert.assertEquals(jdkLocales.length, list.size());
        for (Locale loc : jdkLocales) {
            Assert.assertTrue(list.contains(loc));
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAvailableLocaleListUnmodifiable() {
        LocaleUtils.availableLocaleList().add(Locale.ENGLISH);
    }

    @Test
    public void testAvailableLocaleSet() {
        Set set = LocaleUtils.availableLocaleSet();
        Assert.assertNotNull(set);
        Assert.assertTrue(set.size() > 0);
        Assert.assertSame(set, LocaleUtils.availableLocaleSet());

        Set expected = new HashSet(Arrays.asList(Locale.getAvailableLocales()));
        Assert.assertEquals(expected, set);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAvailableLocaleSetUnmodifiable() {
        LocaleUtils.availableLocaleSet().add(Locale.ENGLISH);
    }

    @Test
    public void testIsAvailableLocale() {
        Assert.assertTrue(LocaleUtils.isAvailableLocale(Locale.ENGLISH));
        Assert.assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        Assert.assertFalse(LocaleUtils.isAvailableLocale(new Locale("zz", "ZZ", "zzz")));
    }

    @Test
    public void testLanguagesByCountry() {
        List list = LocaleUtils.languagesByCountry(null);
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
        Assert.assertSame(list, LocaleUtils.languagesByCountry(null));

        list = LocaleUtils.languagesByCountry("ZZ");
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());

        List usList = LocaleUtils.languagesByCountry("US");
        Assert.assertNotNull(usList);
        Assert.assertTrue(usList.size() > 0);
        Assert.assertSame(usList, LocaleUtils.languagesByCountry("US"));

        for (int i = 0; i < usList.size(); i++) {
            Locale loc = (Locale) usList.get(i);
            Assert.assertEquals("US", loc.getCountry());
            Assert.assertEquals("", loc.getVariant());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLanguagesByCountryUnmodifiable() {
        List list = LocaleUtils.languagesByCountry("US");
        list.add(Locale.ENGLISH);
    }

    @Test
    public void testCountriesByLanguage() {
        List list = LocaleUtils.countriesByLanguage(null);
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());
        Assert.assertSame(list, LocaleUtils.countriesByLanguage(null));

        list = LocaleUtils.countriesByLanguage("zz");
        Assert.assertNotNull(list);
        Assert.assertEquals(0, list.size());

        List enList = LocaleUtils.countriesByLanguage("en");
        Assert.assertNotNull(enList);
        Assert.assertTrue(enList.size() > 0);
        Assert.assertSame(enList, LocaleUtils.countriesByLanguage("en"));

        for (int i = 0; i < enList.size(); i++) {
            Locale loc = (Locale) enList.get(i);
            Assert.assertEquals("en", loc.getLanguage());
            Assert.assertTrue(loc.getCountry().length() > 0);
            Assert.assertEquals("", loc.getVariant());
        }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCountriesByLanguageUnmodifiable() {
        List list = LocaleUtils.countriesByLanguage("en");
        list.add(Locale.US);
    }
}
