package org.apache.commons.codec.language.bm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhoneticEngineTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithRulesRuleTypeThrowsException() {
        new PhoneticEngine(NameType.GENERIC, RuleType.RULES, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFourArgConstructorWithRulesRuleTypeThrowsException() {
        new PhoneticEngine(NameType.ASHKENAZI, RuleType.RULES, false, 15);
    }

    @Test
    public void testGettersAndInitialState() {
        PhoneticEngine engine1 = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        Assert.assertEquals(NameType.GENERIC, engine1.getNameType());
        Assert.assertEquals(RuleType.APPROX, engine1.getRuleType());
        Assert.assertTrue(engine1.isConcat());
        Assert.assertEquals(20, engine1.getMaxPhonemes());
        Assert.assertNotNull(engine1.getLang());

        PhoneticEngine engine2 = new PhoneticEngine(NameType.ASHKENAZI, RuleType.EXACT, false, 5);
        Assert.assertEquals(NameType.ASHKENAZI, engine2.getNameType());
        Assert.assertEquals(RuleType.EXACT, engine2.getRuleType());
        Assert.assertFalse(engine2.isConcat());
        Assert.assertEquals(5, engine2.getMaxPhonemes());
        Assert.assertNotNull(engine2.getLang());

        PhoneticEngine engine3 = new PhoneticEngine(NameType.SEPHARDIC, RuleType.APPROX, false);
        Assert.assertEquals(NameType.SEPHARDIC, engine3.getNameType());
        Assert.assertEquals(RuleType.APPROX, engine3.getRuleType());
        Assert.assertFalse(engine3.isConcat());
    }

    @Test
    public void testEncodeGenericSimple() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String encoded = engine.encode("Smith");
        Assert.assertNotNull(encoded);
        Assert.assertFalse(encoded.isEmpty());
    }

    @Test
    public void testEncodeGenericWithDQuotePrefix() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.EXACT, true);
        String encoded = engine.encode("d'angelo");
        Assert.assertTrue(encoded.startsWith("("));
        Assert.assertTrue(encoded.contains(")-("));
        Assert.assertTrue(encoded.endsWith(")"));
    }

    @Test
    public void testEncodeGenericWithGenericPrefix() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String encoded = engine.encode("van Gogh");
        Assert.assertTrue(encoded.startsWith("("));
        Assert.assertTrue(encoded.contains(")-("));
        Assert.assertTrue(encoded.endsWith(")"));

        String encodedDeLa = engine.encode("de la Cruz");
        Assert.assertTrue(encodedDeLa.startsWith("("));
        Assert.assertTrue(encodedDeLa.contains(")-("));
    }

    @Test
    public void testEncodeSephardicWithPrefixesAndApostrophe() {
        PhoneticEngine engine = new PhoneticEngine(NameType.SEPHARDIC, RuleType.APPROX, true);
        String encoded = engine.encode("al d'avila");
        Assert.assertNotNull(encoded);
        Assert.assertFalse(encoded.isEmpty());

        PhoneticEngine engineNoConcat = new PhoneticEngine(NameType.SEPHARDIC, RuleType.APPROX, false);
        String encodedNoConcat = engineNoConcat.encode("del sol");
        Assert.assertNotNull(encodedNoConcat);
    }

    @Test
    public void testEncodeAshkenaziWithPrefixes() {
        PhoneticEngine engine = new PhoneticEngine(NameType.ASHKENAZI, RuleType.APPROX, true);
        String encoded = engine.encode("ben Gurion");
        Assert.assertNotNull(encoded);
        Assert.assertFalse(encoded.isEmpty());

        PhoneticEngine engineNoConcat = new PhoneticEngine(NameType.ASHKENAZI, RuleType.APPROX, false);
        String encodedNoConcat = engineNoConcat.encode("bar Kochba");
        Assert.assertNotNull(encodedNoConcat);
    }

    @Test
    public void testEncodeMultiWordConcatTrueVsFalse() {
        PhoneticEngine engineConcat = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        PhoneticEngine engineNoConcat = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, false);

        String resultConcat = engineConcat.encode("john smith");
        String resultNoConcat = engineNoConcat.encode("john smith");

        Assert.assertNotNull(resultConcat);
        Assert.assertNotNull(resultNoConcat);
        Assert.assertTrue(resultNoConcat.contains("-"));
    }

    @Test
    public void testEncodeWithDashesAndSpaces() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String res1 = engine.encode("Jean-Luc");
        String res2 = engine.encode("Jean Luc");
        Assert.assertEquals(res1, res2);
    }

    @Test
    public void testEncodeWithExplicitLanguageSet() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.EXACT, true);
        Set<String> langs = new HashSet<String>();
        langs.add("french");
        Languages.LanguageSet customSet = Languages.LanguageSet.from(langs);

        String encoded = engine.encode("Dupont", customSet);
        Assert.assertNotNull(encoded);
        Assert.assertFalse(encoded.isEmpty());
    }

    @Test
    public void testMaxPhonemesLimitation() {
        PhoneticEngine engineLowMax = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true, 1);
        PhoneticEngine engineHighMax = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true, 20);

        String encodedLow = engineLowMax.encode("Alexandrov");
        String encodedHigh = engineHighMax.encode("Alexandrov");

        Assert.assertNotNull(encodedLow);
        Assert.assertNotNull(encodedHigh);
        Assert.assertFalse(encodedLow.contains("|"));
    }

    @Test
    public void testPhonemeBuilderOperations() {
        Languages.LanguageSet langSet = Languages.NO_LANGUAGES;
        PhoneticEngine.PhonemeBuilder builder = PhoneticEngine.PhonemeBuilder.empty(langSet);

        Assert.assertNotNull(builder.getPhonemes());
        Assert.assertEquals(1, builder.getPhonemes().size());
        Assert.assertEquals("", builder.makeString());

        builder.append("abc");
        Assert.assertEquals("abc", builder.makeString());

        builder.append("def");
        Assert.assertEquals("abcdef", builder.makeString());

        Set<String> langs1 = new HashSet<String>();
        langs1.add("english");
        Languages.LanguageSet engSet = Languages.LanguageSet.from(langs1);

        PhoneticEngine.PhonemeBuilder engBuilder = PhoneticEngine.PhonemeBuilder.empty(engSet);
        engBuilder.append("test");

        Rule.Phoneme rightPhoneme = new Rule.Phoneme("ing", engSet);
        engBuilder.apply(rightPhoneme, 5);
        Assert.assertEquals("testing", engBuilder.makeString());

        Set<String> langs2 = new HashSet<String>();
        langs2.add("german");
        Languages.LanguageSet gerSet = Languages.LanguageSet.from(langs2);
        Rule.Phoneme incompatiblePhoneme = new Rule.Phoneme("isch", gerSet);

        engBuilder.apply(incompatiblePhoneme, 5);
        Assert.assertEquals("", engBuilder.makeString());
    }

    @Test
    public void testPhonemeBuilderMaxPhonemesTruncation() {
        Set<String> langs = new HashSet<String>();
        langs.add("english");
        Languages.LanguageSet engSet = Languages.LanguageSet.from(langs);

        PhoneticEngine.PhonemeBuilder builder = PhoneticEngine.PhonemeBuilder.empty(engSet);

        Rule.Phoneme p1 = new Rule.Phoneme("a", engSet);
        Rule.Phoneme p2 = new Rule.Phoneme("b", engSet);
        Rule.Phoneme p3 = new Rule.Phoneme("c", engSet);

        Rule.PhonemeList list = new Rule.PhonemeList(java.util.Arrays.asList(p1, p2, p3));
        builder.apply(list, 2);

        Assert.assertEquals(2, builder.getPhonemes().size());
    }

    @Test
    public void testEncodeSingleWordNoConcat() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, false);
        String encoded = engine.encode("Smith");
        Assert.assertNotNull(encoded);
        Assert.assertFalse(encoded.contains("-"));
    }

    @Test
    public void testEncodeMultipleWordsNoConcat() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.EXACT, false);
        String encoded = engine.encode("John Smith");
        Assert.assertNotNull(encoded);
        Assert.assertTrue(encoded.contains("-"));
    }

    @Test
    public void testEncodeWithEmptyOrBlankWords() {
        PhoneticEngine engine = new PhoneticEngine(NameType.GENERIC, RuleType.APPROX, true);
        String encoded = engine.encode("   ");
        Assert.assertEquals("", encoded);
    }
}
