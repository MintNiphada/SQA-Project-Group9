package org.apache.commons.codec.language.bm;

import org.apache.commons.codec.language.bm.Languages.LanguageSet;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class RuleTest {

    @Test
    public void testPhonemeConstructorsAndBasicMethods() {
        LanguageSet langSet1 = LanguageSet.from(new HashSet<String>(Arrays.asList("english", "french")));
        Rule.Phoneme p1 = new Rule.Phoneme("test", langSet1);
        Assert.assertEquals("test", p1.getPhonemeText().toString());
        Assert.assertSame(langSet1, p1.getLanguages());
        Assert.assertEquals("test[[english, french]]", p1.toString());

        p1.append("ing");
        Assert.assertEquals("testing", p1.getPhonemeText().toString());

        Iterable<Rule.Phoneme> iterable = p1.getPhonemes();
        List<Rule.Phoneme> list = new ArrayList<Rule.Phoneme>();
        for (Rule.Phoneme p : iterable) {
            list.add(p);
        }
        Assert.assertEquals(1, list.size());
        Assert.assertSame(p1, list.get(0));

        Rule.Phoneme p2 = new Rule.Phoneme("123", langSet1);
        Rule.Phoneme combined1 = new Rule.Phoneme(p1, p2);
        Assert.assertEquals("testing123", combined1.getPhonemeText().toString());
        Assert.assertSame(langSet1, combined1.getLanguages());

        LanguageSet langSet2 = LanguageSet.from(new HashSet<String>(Arrays.asList("german")));
        Rule.Phoneme combined2 = new Rule.Phoneme(p1, p2, langSet2);
        Assert.assertEquals("testing123", combined2.getPhonemeText().toString());
        Assert.assertSame(langSet2, combined2.getLanguages());

        Rule.Phoneme joined = p1.join(p2);
        Assert.assertEquals("testing123", joined.getPhonemeText().toString());
    }

    @Test
    public void testPhonemeComparator() {
        LanguageSet any = Languages.ANY_LANGUAGE;
        Rule.Phoneme a = new Rule.Phoneme("a", any);
        Rule.Phoneme aa = new Rule.Phoneme("aa", any);
        Rule.Phoneme b = new Rule.Phoneme("b", any);
        Rule.Phoneme a2 = new Rule.Phoneme("a", any);

        Assert.assertTrue(Rule.Phoneme.COMPARATOR.compare(a, b) < 0);
        Assert.assertTrue(Rule.Phoneme.COMPARATOR.compare(b, a) > 0);
        Assert.assertTrue(Rule.Phoneme.COMPARATOR.compare(a, aa) < 0);
        Assert.assertTrue(Rule.Phoneme.COMPARATOR.compare(aa, a) > 0);
        Assert.assertEquals(0, Rule.Phoneme.COMPARATOR.compare(a, a2));
    }

    @Test
    public void testPhonemeList() {
        Rule.Phoneme p1 = new Rule.Phoneme("a", Languages.ANY_LANGUAGE);
        Rule.Phoneme p2 = new Rule.Phoneme("b", Languages.ANY_LANGUAGE);
        List<Rule.Phoneme> list = Arrays.asList(p1, p2);
        Rule.PhonemeList pList = new Rule.PhonemeList(list);

        Assert.assertEquals(list, pList.getPhonemes());
    }

    @Test
    public void testGetInstanceAndRules() {
        List<Rule> rulesList = Rule.getInstance(NameType.GENERIC, RuleType.APPROX, "english");
        Assert.assertNotNull(rulesList);
        Assert.assertFalse(rulesList.isEmpty());

        Map<String, List<Rule>> ruleMap = Rule.getInstanceMap(NameType.GENERIC, RuleType.APPROX, "english");
        Assert.assertNotNull(ruleMap);
        Assert.assertFalse(ruleMap.isEmpty());

        Rule firstRule = rulesList.get(0);
        Assert.assertNotNull(firstRule.getPattern());
        Assert.assertNotNull(firstRule.getLContext());
        Assert.assertNotNull(firstRule.getRContext());
        Assert.assertNotNull(firstRule.getPhoneme());
        Assert.assertTrue(firstRule.toString().contains("Rule{line="));

        LanguageSet singletonSet = LanguageSet.from(new HashSet<String>(Collections.singletonList("english")));
        List<Rule> rulesFromSet = Rule.getInstance(NameType.GENERIC, RuleType.APPROX, singletonSet);
        Assert.assertNotNull(rulesFromSet);
        Assert.assertFalse(rulesFromSet.isEmpty());

        LanguageSet multiSet = LanguageSet.from(new HashSet<String>(Arrays.asList("english", "french")));
        Map<String, List<Rule>> anyRuleMap = Rule.getInstanceMap(NameType.GENERIC, RuleType.APPROX, multiSet);
        Assert.assertNotNull(anyRuleMap);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetInstanceMapInvalidLang() {
        Rule.getInstanceMap(NameType.GENERIC, RuleType.APPROX, "nonexistent_lang");
    }

    @Test
    public void testPatternAndContextMatches() {
        Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        Rule rule = new Rule("test", "pre", "post", ph);

        Assert.assertTrue(rule.patternAndContextMatches("pretestpost", 3));
        Assert.assertFalse(rule.patternAndContextMatches("pretestpost", 0));
        Assert.assertFalse(rule.patternAndContextMatches("wrongtestpost", 5));
        Assert.assertFalse(rule.patternAndContextMatches("pretestwrong", 3));
        Assert.assertFalse(rule.patternAndContextMatches("prete", 3));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testPatternAndContextMatchesNegativeIndex() {
        Rule.Phoneme ph = new Rule.Phoneme("out", Languages.ANY_LANGUAGE);
        Rule rule = new Rule("test", "", "", ph);
        rule.patternAndContextMatches("test", -1);
    }

    @Test
    public void testPatternCompilationBranches() throws Exception {
        Method patternMethod = Rule.class.getDeclaredMethod("pattern", String.class);
        patternMethod.setAccessible(true);

        // 1. ALL_STRINGS_RMATCHER: startsWith or endsWith and content is empty
        Rule.RPattern allMatcher = (Rule.RPattern) patternMethod.invoke(null, "^");
        Assert.assertTrue(allMatcher.isMatch("anything"));
        allMatcher = (Rule.RPattern) patternMethod.invoke(null, "$");
        Assert.assertTrue(allMatcher.isMatch(""));

        // 2. Exact match empty: "^$"
        Rule.RPattern exactEmpty = (Rule.RPattern) patternMethod.invoke(null, "^$");
        Assert.assertTrue(exactEmpty.isMatch(""));
        Assert.assertFalse(exactEmpty.isMatch("a"));

        // 3. Exact match non-empty: "^abc$"
        Rule.RPattern exactNonEmpty = (Rule.RPattern) patternMethod.invoke(null, "^abc$");
        Assert.assertTrue(exactNonEmpty.isMatch("abc"));
        Assert.assertFalse(exactNonEmpty.isMatch("abcd"));
        Assert.assertFalse(exactNonEmpty.isMatch("ab"));

        // 4. Starts with: "^abc"
        Rule.RPattern startsWithMatcher = (Rule.RPattern) patternMethod.invoke(null, "^abc");
        Assert.assertTrue(startsWithMatcher.isMatch("abcdef"));
        Assert.assertFalse(startsWithMatcher.isMatch("ab"));
        Assert.assertFalse(startsWithMatcher.isMatch("bcdef"));

        // 5. Ends with: "abc$"
        Rule.RPattern endsWithMatcher = (Rule.RPattern) patternMethod.invoke(null, "abc$");
        Assert.assertTrue(endsWithMatcher.isMatch("xyzabc"));
        Assert.assertFalse(endsWithMatcher.isMatch("xyzab"));
        Assert.assertFalse(endsWithMatcher.isMatch("ab"));

        // 6. Box exact match: "^[abc]$"
        Rule.RPattern boxExact = (Rule.RPattern) patternMethod.invoke(null, "^[abc]$");
        Assert.assertTrue(boxExact.isMatch("a"));
        Assert.assertTrue(boxExact.isMatch("b"));
        Assert.assertFalse(boxExact.isMatch("d"));
        Assert.assertFalse(boxExact.isMatch("ab"));
        Assert.assertFalse(boxExact.isMatch(""));

        // 7. Box exact negated match: "^[^abc]$"
        Rule.RPattern boxExactNeg = (Rule.RPattern) patternMethod.invoke(null, "^[^abc]$");
        Assert.assertTrue(boxExactNeg.isMatch("d"));
        Assert.assertFalse(boxExactNeg.isMatch("a"));
        Assert.assertFalse(boxExactNeg.isMatch("ab"));

        // 8. Box startsWith match: "^[abc]"
        Rule.RPattern boxStarts = (Rule.RPattern) patternMethod.invoke(null, "^[abc]");
        Assert.assertTrue(boxStarts.isMatch("axyz"));
        Assert.assertFalse(boxStarts.isMatch("wxyz"));
        Assert.assertFalse(boxStarts.isMatch(""));

        // 9. Box startsWith negated match: "^[^abc]"
        Rule.RPattern boxStartsNeg = (Rule.RPattern) patternMethod.invoke(null, "^[^abc]");
        Assert.assertTrue(boxStartsNeg.isMatch("wxyz"));
        Assert.assertFalse(boxStartsNeg.isMatch("axyz"));
        Assert.assertFalse(boxStartsNeg.isMatch(""));

        // 10. Box endsWith match: "[abc]$"
        Rule.RPattern boxEnds = (Rule.RPattern) patternMethod.invoke(null, "[abc]$");
        Assert.assertTrue(boxEnds.isMatch("xyza"));
        Assert.assertFalse(boxEnds.isMatch("xyzw"));
        Assert.assertFalse(boxEnds.isMatch(""));

        // 11. Box endsWith negated match: "[^abc]$"
        Rule.RPattern boxEndsNeg = (Rule.RPattern) patternMethod.invoke(null, "[^abc]$");
        Assert.assertTrue(boxEndsNeg.isMatch("xyzw"));
        Assert.assertFalse(boxEndsNeg.isMatch("xyza"));
        Assert.assertFalse(boxEndsNeg.isMatch(""));

        // 12. Regex fallback: "(a|b)"
        Rule.RPattern regexFallback = (Rule.RPattern) patternMethod.invoke(null, "(a|b)");
        Assert.assertTrue(regexFallback.isMatch("cat"));
        Assert.assertFalse(regexFallback.isMatch("dog"));
    }

    @Test
    public void testParsePhonemeAndPhonemeExpr() throws Exception {
        Method parsePhonemeExpr = Rule.class.getDeclaredMethod("parsePhonemeExpr", String.class);
        parsePhonemeExpr.setAccessible(true);

        Rule.PhonemeExpr simple = (Rule.PhonemeExpr) parsePhonemeExpr.invoke(null, "\"test\"");
        List<Rule.Phoneme> phList = new ArrayList<Rule.Phoneme>();
        for (Rule.Phoneme p : simple.getPhonemes()) {
            phList.add(p);
        }
        Assert.assertEquals(1, phList.size());
        Assert.assertEquals("\"test\"", phList.get(0).getPhonemeText().toString());

        Rule.PhonemeExpr withLangs = (Rule.PhonemeExpr) parsePhonemeExpr.invoke(null, "phoneme[english+french]");
        phList.clear();
        for (Rule.Phoneme p : withLangs.getPhonemes()) {
            phList.add(p);
        }
        Assert.assertEquals(1, phList.size());
        Assert.assertEquals("phoneme", phList.get(0).getPhonemeText().toString());

        Rule.PhonemeExpr listPh = (Rule.PhonemeExpr) parsePhonemeExpr.invoke(null, "(a|b[english]|)");
        phList.clear();
        for (Rule.Phoneme p : listPh.getPhonemes()) {
            phList.add(p);
        }
        Assert.assertEquals(3, phList.size());
        Assert.assertEquals("a", phList.get(0).getPhonemeText().toString());
        Assert.assertEquals("b", phList.get(1).getPhonemeText().toString());
        Assert.assertEquals("", phList.get(2).getPhonemeText().toString());
    }

    @Test
    public void testParsePhonemeInvalidOpenBracket() throws Exception {
        Method parsePhoneme = Rule.class.getDeclaredMethod("parsePhoneme", String.class);
        parsePhoneme.setAccessible(true);
        try {
            parsePhoneme.invoke(null, "test[english");
            Assert.fail("Expected IllegalArgumentException");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testParsePhonemeExprInvalidParentheses() throws Exception {
        Method parsePhonemeExpr = Rule.class.getDeclaredMethod("parsePhonemeExpr", String.class);
        parsePhonemeExpr.setAccessible(true);
        try {
            parsePhonemeExpr.invoke(null, "(a|b");
            Assert.fail("Expected IllegalArgumentException");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testParseRulesScanner() throws Exception {
        Method parseRules = Rule.class.getDeclaredMethod("parseRules", Scanner.class, String.class);
        parseRules.setAccessible(true);

        String rulesData =
                "/* multi-line \n comment */\n" +
                "// single-line comment\n" +
                "   \n" +
                "\"pat1\" \"lcon1\" \"rcon1\" \"ph1\"\n" +
                "\"pat2\" \"\" \"\" \"ph2\" // inline comment\n";

        Scanner sc = new Scanner(new ByteArrayInputStream(rulesData.getBytes("UTF-8")), "UTF-8");
        @SuppressWarnings("unchecked")
        Map<String, List<Rule>> result = (Map<String, List<Rule>>) parseRules.invoke(null, sc, "testLocation");

        Assert.assertEquals(1, result.size());
        List<Rule> pRules = result.get("p");
        Assert.assertNotNull(pRules);
        Assert.assertEquals(2, pRules.size());
    }

    @Test
    public void testParseRulesMalformedLine() throws Exception {
        Method parseRules = Rule.class.getDeclaredMethod("parseRules", Scanner.class, String.class);
        parseRules.setAccessible(true);

        String malformed = "\"pat1\" \"lcon1\" \"rcon1\"\n";
        Scanner sc = new Scanner(new ByteArrayInputStream(malformed.getBytes("UTF-8")), "UTF-8");
        try {
            parseRules.invoke(null, sc, "testLocation");
            Assert.fail("Expected IllegalArgumentException wrapped in InvocationTargetException");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testParseRulesMalformedInclude() throws Exception {
        Method parseRules = Rule.class.getDeclaredMethod("parseRules", Scanner.class, String.class);
        parseRules.setAccessible(true);

        String malformedInclude = "#include file with spaces.txt\n";
        Scanner sc = new Scanner(new ByteArrayInputStream(malformedInclude.getBytes("UTF-8")), "UTF-8");
        try {
            parseRules.invoke(null, sc, "testLocation");
            Assert.fail("Expected IllegalArgumentException wrapped in InvocationTargetException");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testParseRulesProblemParsingLine() throws Exception {
        Method parseRules = Rule.class.getDeclaredMethod("parseRules", Scanner.class, String.class);
        parseRules.setAccessible(true);

        String badPhoneme = "\"pat\" \"\" \"\" \"[invalid\"\n";
        Scanner sc = new Scanner(new ByteArrayInputStream(badPhoneme.getBytes("UTF-8")), "UTF-8");
        try {
            parseRules.invoke(null, sc, "testLocation");
            Assert.fail("Expected IllegalStateException wrapped in InvocationTargetException");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }

    @Test
    public void testCreateScannerNotFound() throws Exception {
        Method createScanner = Rule.class.getDeclaredMethod("createScanner", String.class);
        createScanner.setAccessible(true);
        try {
            createScanner.invoke(null, "non_existent_resource_file");
            Assert.fail("Expected IllegalArgumentException");
        } catch (InvocationTargetException e) {
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testStripQuotes() throws Exception {
        Method stripQuotes = Rule.class.getDeclaredMethod("stripQuotes", String.class);
        stripQuotes.setAccessible(true);

        Assert.assertEquals("hello", stripQuotes.invoke(null, "\"hello\""));
        Assert.assertEquals("hello", stripQuotes.invoke(null, "\"hello"));
        Assert.assertEquals("hello", stripQuotes.invoke(null, "hello\""));
        Assert.assertEquals("hello", stripQuotes.invoke(null, "hello"));
        Assert.assertEquals("", stripQuotes.invoke(null, "\"\""));
    }
}
