package org.apache.commons.codec.language.bm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;

public class LangTest {

    private static final String LANGUAGE_RULES_RN = "org/apache/commons/codec/language/bm/lang.txt";

    @Test
    public void testInstanceNotNull() {
        for (final NameType nameType : NameType.values()) {
            final Lang lang = Lang.instance(nameType);
            Assert.assertNotNull("Lang instance should not be null for " + nameType, lang);
        }
    }

    @Test
    public void testInstanceSameObject() {
        for (final NameType nameType : NameType.values()) {
            final Lang lang1 = Lang.instance(nameType);
            final Lang lang2 = Lang.instance(nameType);
            Assert.assertSame("Lang instances should be cached and identical for " + nameType, lang1, lang2);
        }
    }

    @Test
    public void testLoadFromResourceValid() {
        final Languages languages = Languages.getInstance(NameType.GENERIC);
        final Lang lang = Lang.loadFromResource(LANGUAGE_RULES_RN, languages);
        Assert.assertNotNull("Loaded Lang instance should not be null", lang);
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadFromResourceNotFound() {
        final Languages languages = Languages.getInstance(NameType.GENERIC);
        Lang.loadFromResource("org/apache/commons/codec/language/bm/non_existent_file.txt", languages);
    }

    @Test
    public void testGuessLanguageSingleton() {
        final Lang lang = Lang.instance(NameType.GENERIC);
        // Polish distinctive word endings typically yield polish as singleton
        final String guessed = lang.guessLanguage("brz\u0119czyszczykiewicz");
        Assert.assertEquals("polish", guessed);
    }

    @Test
    public void testGuessLanguageMultipleYieldsAny() {
        final Lang lang = Lang.instance(NameType.GENERIC);
        // "anna" is common across many languages, so it should not resolve to a singleton
        final String guessed = lang.guessLanguage("anna");
        Assert.assertEquals(Languages.ANY, guessed);
    }

    @Test
    public void testGuessLanguagesCaseInsensitive() {
        final Lang lang = Lang.instance(NameType.GENERIC);
        final String textLower = "guzikowski";
        final String textUpper = "GUZIKOWSKI";
        final String textMixed = "GuziKowSki";

        final Languages.LanguageSet setLower = lang.guessLanguages(textLower);
        final Languages.LanguageSet setUpper = lang.guessLanguages(textUpper);
        final Languages.LanguageSet setMixed = lang.guessLanguages(textMixed);

        Assert.assertFalse("LanguageSet should not be empty", setLower.isEmpty());
        Assert.assertEquals("LanguageSet should match regardless of case", setLower, setUpper);
        Assert.assertEquals("LanguageSet should match regardless of case", setLower, setMixed);
    }

    @Test
    public void testGuessLanguagesGenericKnownWords() {
        final Lang lang = Lang.instance(NameType.GENERIC);

        final Languages.LanguageSet polishSet = lang.guessLanguages("czernik");
        Assert.assertTrue("Should contain polish", polishSet.contains("polish"));

        final Languages.LanguageSet germanSet = lang.guessLanguages("schmidt");
        Assert.assertTrue("Should contain german", germanSet.contains("german"));

        final Languages.LanguageSet spanishSet = lang.guessLanguages("trujillo");
        Assert.assertTrue("Should contain spanish", spanishSet.contains("spanish"));

        final Languages.LanguageSet frenchSet = lang.guessLanguages("bourcier");
        Assert.assertTrue("Should contain french", frenchSet.contains("french"));
    }

    @Test
    public void testGuessLanguagesAshkenazi() {
        final Lang lang = Lang.instance(NameType.ASHKENAZI);
        Assert.assertNotNull(lang);

        final Languages.LanguageSet set = lang.guessLanguages("shapiro");
        Assert.assertNotNull(set);
        Assert.assertFalse(set.isEmpty());

        final String singleLang = lang.guessLanguage("shapiro");
        Assert.assertNotNull(singleLang);
    }

    @Test
    public void testGuessLanguagesSephardic() {
        final Lang lang = Lang.instance(NameType.SEPHARDIC);
        Assert.assertNotNull(lang);

        final Languages.LanguageSet set = lang.guessLanguages("toledano");
        Assert.assertNotNull(set);
        Assert.assertFalse(set.isEmpty());

        final String singleLang = lang.guessLanguage("toledano");
        Assert.assertNotNull(singleLang);
    }

    @Test
    public void testGuessLanguageEmptyString() {
        final Lang lang = Lang.instance(NameType.GENERIC);
        final Languages.LanguageSet ls = lang.guessLanguages("");
        Assert.assertNotNull(ls);
        final String guessed = lang.guessLanguage("");
        Assert.assertNotNull(guessed);
    }

    @Test
    public void testGuessLanguagesNoMatchFallbackToAny() {
        final Lang lang = Lang.instance(NameType.GENERIC);
        // Arbitrary numbers/characters that rule out all languages or match nothing
        final Languages.LanguageSet ls = lang.guessLanguages("1234567890");
        Assert.assertNotNull(ls);
        Assert.assertFalse("Should fallback to ANY_LANGUAGE if all ruled out or default", ls.isEmpty());
    }

    @Test
    public void testGuessLanguagesReturnsAnyLanguageSet() {
        final Lang lang = Lang.instance(NameType.GENERIC);
        final Languages.LanguageSet ls = lang.guessLanguages("xxxx");
        Assert.assertNotNull(ls);
        Assert.assertFalse(ls.isEmpty());
    }
}
