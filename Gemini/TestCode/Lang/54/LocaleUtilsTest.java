package org.apache.commons.lang;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocaleUtilsTest {

    @Test
    public void testConstructor() {
        LocaleUtils utils = new LocaleUtils();
        Assert.assertNotNull(utils);
    }

    @Test
    public void testToLocaleValid() {
        Assert.assertNull(LocaleUtils.toLocale(null));
        Assert.assertEquals(new Locale("en", ""), LocaleUtils.toLocale("en"));
        Assert.assertEquals(new Locale("en", "GB"), LocaleUtils.toLocale("en_GB"));
        Assert.assertEquals(new Locale("en", "GB", "xxx"), LocaleUtils.toLocale("en_GB_xxx"));
        Assert.assertEquals(new Locale("fr", "CA", "POSIX"), LocaleUtils.toLocale("fr_CA_POSIX"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLengthShort() {
        LocaleUtils.toLocale("u");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLengthThree() {
        LocaleUtils.toLocale("und");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLengthFour() {
        LocaleUtils.toLocale("unde");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLengthSix() {
        LocaleUtils.toLocale("en_GB_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageCase1() {
        LocaleUtils.toLocale("En");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageCase2() {
        LocaleUtils.toLocale("eN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageNonAlpha1() {
        LocaleUtils.toLocale("1n");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidLanguageNonAlpha2() {
        LocaleUtils.toLocale("e1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidSeparatorPos2() {
        LocaleUtils.toLocale("en-GB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidCountryCase1() {
        LocaleUtils.toLocale("en_gB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidCountryCase2() {
        LocaleUtils.toLocale("en_Gb");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidCountryNonAlpha1() {
        LocaleUtils.toLocale("en_1B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidCountryNonAlpha2() {
        LocaleUtils.toLocale("en_G1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToLocaleInvalidSeparatorPos5() {
        LocaleUtils.toLocale("en_GB-xxx");
    }

    @Test
    public void testLocaleLookupListOneArg() {
        Locale locale = new Locale("en", "GB", "xxx");
        List list = LocaleUtils.localeLookupList(locale);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(new Locale("en", "GB", "xxx"), list.get(0));
        Assert.assertEquals(new Locale("en", "GB"), list.get(1));
        Assert.assertEquals(new Locale("en", ""), list.get(2));

        locale = new Locale("en", "GB");
        list = LocaleUtils.localeLookupList(locale);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(new Locale("en", "GB"), list.get(0));
        Assert.assertEquals(new Locale("en", ""), list.get(1));

        locale = new Locale("en", "");
        list = LocaleUtils.localeLookupList(locale);
        Assert.assertEquals(1, list.size());
        Assert.assertEquals(new Locale("en", ""), list.get(0));

        list = LocaleUtils.localeLookupList(null);
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testLocaleLookupListTwoArgs() {
        Locale defaultLocale = new Locale("fr", "FR");

        List list = LocaleUtils.localeLookupList(null, defaultLocale);
        Assert.assertEquals(0, list.size());

        Locale locale = new Locale("en", "GB", "xxx");
        list = LocaleUtils.localeLookupList(locale, defaultLocale);
        Assert.assertEquals(4, list.size());
        Assert.assertEquals(new Locale("en", "GB", "xxx"), list.get(0));
        Assert.assertEquals(new Locale("en", "GB"), list.get(1));
        Assert.assertEquals(new Locale("en", ""), list.get(2));
        Assert.assertEquals(defaultLocale, list.get(3));

        list = LocaleUtils.localeLookupList(locale, new Locale("en", "GB"));
        Assert.assertEquals(3, list.size());
        Assert.assertEquals(new Locale("en", "GB", "xxx"), list.get(0));
        Assert.assertEquals(new Locale("en", "GB"), list.get(1));
        Assert.assertEquals(new Locale("en", ""), list.get(2));
    }

    @Test
    public void testAvailableLocaleList() {
        List list = LocaleUtils.availableLocaleList();
        Assert.assertNotNull(list);
        Assert.assertEquals(Locale.getAvailableLocales().length, list.size());
        try {
            list.add(new Locale("xx", "XX"));
            Assert.fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    public void testAvailableLocaleSet() {
        Set set1 = LocaleUtils.availableLocaleSet();
        Assert.assertNotNull(set1);
        Assert.assertEquals(new HashSet(Arrays.asList(Locale.getAvailableLocales())), set1);

        Set set2 = LocaleUtils.availableLocaleSet();
        Assert.assertSame(set1, set2);

        try {
            set1.add(new Locale("xx", "XX"));
            Assert.fail();
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    public void testIsAvailableLocale() {
        Assert.assertTrue(LocaleUtils.isAvailableLocale(Locale.ENGLISH));
        Assert.assertTrue(LocaleUtils.isAvailableLocale(Locale.US));
        Assert.assertFalse(LocaleUtils.isAvailableLocale(new Locale("xyz", "XYZ")));
    }

    @Test
    public void testLanguagesByCountry() {
        List nullList = LocaleUtils.languagesByCountry(null);
        Assert.assertNotNull(nullList);
        Assert.assertEquals(Collections.EMPTY_LIST, nullList);

        List usList = LocaleUtils.languagesByCountry("US");
        Assert.assertNotNull(usList);
        Assert.assertTrue(usList.contains(new Locale("en", "US")));
        for (int i = 0; i < usList.size(); i++) {
            Locale loc = (Locale) usList.get(i);
            Assert.assertEquals("US", loc.getCountry());
            Assert.assertEquals("", loc.getVariant());
        }

        List usListCached = LocaleUtils.languagesByCountry("US");
        Assert.assertSame(usList, usListCached);

        List unknownList = LocaleUtils.languagesByCountry("ZZ");
        Assert.assertNotNull(unknownList);
        Assert.assertEquals(0, unknownList.size());
    }

    @Test
    public void testCountriesByLanguage() {
        List nullList = LocaleUtils.countriesByLanguage(null);
        Assert.assertNotNull(nullList);
        Assert.assertEquals(Collections.EMPTY_LIST, nullList);

        List enList = LocaleUtils.countriesByLanguage("en");
        Assert.assertNotNull(enList);
        Assert.assertTrue(enList.contains(new Locale("en", "US")));
        Assert.assertTrue(enList.contains(new Locale("en", "GB")));
        for (int i = 0; i < enList.size(); i++) {
            Locale loc = (Locale) enList.get(i);
            Assert.assertEquals("en", loc.getLanguage());
            Assert.assertTrue(loc.getCountry().length() > 0);
            Assert.assertEquals("", loc.getVariant());
        }

        List enListCached = LocaleUtils.countriesByLanguage("en");
        Assert.assertSame(enList, enListCached);

        List unknownList = LocaleUtils.countriesByLanguage("zz");
        Assert.assertNotNull(unknownList);
        Assert.assertEquals(0, unknownList.size());
    }
}
