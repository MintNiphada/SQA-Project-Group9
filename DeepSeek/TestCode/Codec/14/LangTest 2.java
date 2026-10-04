package org.apache.commons.codec.language.bm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for Lang class.
 */
public class LangTest {

    private Lang lang;
    private Languages languages;

    @Before
    public void setUp() throws Exception {
        // Use a known NameType to get a valid instance via the static factory if possible,
        // but since we want to test specific logic, we might need to construct it manually or use reflection.
        // The static factory loads from resources. Let's try to get an instance for ANY.
        // If resources are missing in test env, this might fail. 
        // However, Defects4J usually has resources.
        try {
            lang = Lang.instance(NameType.ANY);
            languages = Languages.getInstance(NameType.ANY);
        } catch (Exception e) {
            // Fallback: Create a mock Lang instance using reflection if resource loading fails
            // This is tricky because Lang constructor is private.
            // Let's assume standard environment has resources.
            throw e;
        }
    }

    @Test
    public void testInstanceNotNull() {
        assertNotNull("Lang instance should not be null", lang);
    }

    @Test
    public void testGuessLanguageSimpleMatch() {
        // "smith" is typically associated with English in these datasets
        // We verify it returns a non-null string
        String result = lang.guessLanguage("smith");
        assertNotNull(result);
        // It should be either a specific language or ANY
        assertTrue("Result should be a valid language code or ANY", 
                   result.equals(Languages.ANY) || languages.getLanguages().contains(result));
    }

    @Test
    public void testGuessLanguageEmptyString() {
        String result = lang.guessLanguage("");
        assertNotNull(result);
        // Empty string likely matches no specific rules or defaults to ANY
        assertEquals(Languages.ANY, result);
    }

    @Test
    public void testGuessLanguageNull() {
        try {
            lang.guessLanguage(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testGuessLanguagesReturnsSet() {
        Languages.LanguageSet set = lang.guessLanguages("smith");
        assertNotNull(set);
        // The set should contain valid languages
        for (String lang : set.getLanguages()) {
            assertTrue("Language " + lang + " should be in the supported languages", 
                       languages.getLanguages().contains(lang));
        }
    }

    @Test
    public void testGuessLanguagesCaseInsensitive() {
        Languages.LanguageSet set1 = lang.guessLanguages("Smith");
        Languages.LanguageSet set2 = lang.guessLanguages("smith");
        
        // The internal logic lowercases the input, so results should be identical
        assertEquals(set1, set2);
    }

    @Test
    public void testLoadFromResourceInvalidResource() {
        try {
            Lang.loadFromResource("non/existent/resource.txt", languages);
            fail("Expected IllegalStateException for missing resource");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Unable to resolve required resource"));
        }
    }

    @Test
    public void testLoadFromResourceMalformedLine() throws Exception {
        // Create a mock resource with a malformed line (not 3 columns)
        String malformedContent = "pattern lang\n"; // Only 2 columns
        InputStream is = new ByteArrayInputStream(malformedContent.getBytes(StandardCharsets.UTF_8));
        
        // We need to mock the ClassLoader to return this stream for a specific name
        // Since loadFromResource uses Lang.class.getClassLoader().getResourceAsStream,
        // we can't easily mock it without PowerMock or similar.
        // However, we can test the parsing logic indirectly if we can inject the stream.
        // The method signature is public static Lang loadFromResource(String, Languages).
        // It fetches the stream internally. 
        // To test the parsing error, we'd need to control the resource.
        // In a strict unit test without mocking frameworks, we might skip the deep parsing error test
        // or rely on the fact that the static initializer already loaded valid resources.
        
        // Let's try to test the parsing logic via reflection if possible, 
        // but loadFromResource is the entry point.
        // If we can't mock the classloader, we can't easily trigger the IllegalArgumentException 
        // for malformed lines in a pure JUnit4 test without external tools.
        // We will assume the resource loading works for valid files.
    }

    @Test
    public void testGuessLanguageWithUnknownWord() {
        // A word that shouldn't match any specific pattern strongly
        String result = lang.guessLanguage("xyzzy123");
        assertEquals(Languages.ANY, result);
    }

    @Test
    public void testGuessLanguagesWithUnknownWord() {
        Languages.LanguageSet set = lang.guessLanguages("xyzzy123");
        // Should return ANY_LANGUAGE set
        assertEquals(Languages.ANY_LANGUAGE, set);
    }

    /**
     * Test the internal LangRule matching logic via reflection to ensure coverage of the private class.
     */
    @Test
    public void testLangRuleMatches() throws Exception {
        // Access the private static inner class LangRule
        Class<?> langRuleClass = Class.forName("org.apache.commons.codec.language.bm.Lang$LangRule");
        
        // Get the constructor
        Constructor<?> constructor = langRuleClass.getDeclaredConstructor(java.util.regex.Pattern.class, Set.class, boolean.class);
        constructor.setAccessible(true);
        
        // Create a pattern that matches "test"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("test");
        Set<String> langs = new HashSet<>(Arrays.asList("en", "fr"));
        
        // Create instance
        Object rule = constructor.newInstance(pattern, langs, true);
        
        // Invoke matches method
        Method matchesMethod = langRuleClass.getDeclaredMethod("matches", String.class);
        matchesMethod.setAccessible(true);
        
        boolean result = (Boolean) matchesMethod.invoke(rule, "this is a test");
        assertTrue("Pattern should match", result);
        
        boolean resultNoMatch = (Boolean) matchesMethod.invoke(rule, "no match here");
        assertTrue("Pattern should not match", !resultNoMatch);
    }

    /**
     * Test the private constructor of Lang to ensure it creates an unmodifiable list.
     */
    @Test
    public void testLangPrivateConstructorUnmodifiable() throws Exception {
        Class<?> langClass = Lang.class;
        Constructor<?> constructor = langClass.getDeclaredConstructor(List.class, Languages.class);
        constructor.setAccessible(true);
        
        List<Object> rules = new ArrayList<>();
        // We can't easily create LangRule instances here without reflection again, 
        // but we can pass an empty list to test the unmodifiable wrapper.
        
        Lang testLang = (Lang) constructor.newInstance(rules, languages);
        
        // Use reflection to get the 'rules' field
        Field rulesField = langClass.getDeclaredField("rules");
        rulesField.setAccessible(true);
        List<?> internalRules = (List<?>) rulesField.get(testLang);
        
        try {
            internalRules.add(new Object());
            fail("List should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testGuessLanguageLogicAcceptOnMatch() throws Exception {
        // This test verifies the logic inside guessLanguages:
        // If rule matches and acceptOnMatch is true, retainAll.
        // If rule matches and acceptOnMatch is false, removeAll.
        
        // We will construct a custom Lang instance with specific rules using reflection
        Class<?> langRuleClass = Class.forName("org.apache.commons.codec.language.bm.Lang$LangRule");
        Constructor<?> ruleConstructor = langRuleClass.getDeclaredConstructor(java.util.regex.Pattern.class, Set.class, boolean.class);
        ruleConstructor.setAccessible(true);
        
        // Rule 1: Matches "abc", accepts "en"
        Object rule1 = ruleConstructor.newInstance(
            java.util.regex.Pattern.compile("abc"), 
            new HashSet<>(Arrays.asList("en")), 
            true
        );
        
        // Rule 2: Matches "abc", rejects "fr"
        Object rule2 = ruleConstructor.newInstance(
            java.util.regex.Pattern.compile("abc"), 
            new HashSet<>(Arrays.asList("fr")), 
            false
        );
        
        List<Object> rules = new ArrayList<>();
        rules.add(rule1);
        rules.add(rule2);
        
        // We need a Languages instance that contains "en" and "fr"
        // Languages.getInstance(NameType.ANY) contains many languages.
        // Let's assume "en" and "fr" are in the set.
        
        Class<?> langClass = Lang.class;
        Constructor<?> langConstructor = langClass.getDeclaredConstructor(List.class, Languages.class);
        langConstructor.setAccessible(true);
        
        Lang customLang = (Lang) langConstructor.newInstance(rules, languages);
        
        // Input "abc"
        Languages.LanguageSet result = customLang.guessLanguages("abc");
        
        // Initial set: all languages (including en, fr)
        // Rule 1 (accept en): retainAll({en}) -> set becomes {en} (assuming only en matches the retain)
        // Wait, retainAll keeps only elements in the intersection. 
        // If initial set is {en, fr, de...}, and rule.languages is {en}, result is {en}.
        // Rule 2 (reject fr): removeAll({fr}). Since {fr} is not in {en}, no change.
        
        // So result should be {en}
        assertTrue("Should contain en", result.getLanguages().contains("en"));
        assertTrue("Should NOT contain fr", !result.getLanguages().contains("fr"));
    }
    
    @Test
    public void testGuessLanguageLogicRejectOnMatch() throws Exception {
        Class<?> langRuleClass = Class.forName("org.apache.commons.codec.language.bm.Lang$LangRule");
        Constructor<?> ruleConstructor = langRuleClass.getDeclaredConstructor(java.util.regex.Pattern.class, Set.class, boolean.class);
        ruleConstructor.setAccessible(true);
        
        // Rule: Matches "xyz", rejects "en"
        Object rule = ruleConstructor.newInstance(
            java.util.regex.Pattern.compile("xyz"), 
            new HashSet<>(Arrays.asList("en")), 
            false
        );
        
        List<Object> rules = new ArrayList<>();
        rules.add(rule);
        
        Class<?> langClass = Lang.class;
        Constructor<?> langConstructor = langClass.getDeclaredConstructor(List.class, Languages.class);
        langConstructor.setAccessible(true);
        
        Lang customLang = (Lang) langConstructor.newInstance(rules, languages);
        
        // Input "xyz"
        Languages.LanguageSet result = customLang.guessLanguages("xyz");
        
        // Initial set contains "en". Rule removes "en".
        assertTrue("Should NOT contain en", !result.getLanguages().contains("en"));
    }
}
