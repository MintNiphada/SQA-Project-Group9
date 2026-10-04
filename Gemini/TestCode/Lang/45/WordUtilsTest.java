package org.apache.commons.lang;

import org.junit.Assert;
import org.junit.Test;

public class WordUtilsTest {

    @Test
    public void testConstructor() {
        WordUtils utils = new WordUtils();
        Assert.assertNotNull(utils);
    }

    @Test
    public void testWrap_StringInt() {
        Assert.assertNull(WordUtils.wrap(null, 20));
        Assert.assertNull(WordUtils.wrap(null, -1));
        Assert.assertEquals("", WordUtils.wrap("", 20));
        Assert.assertEquals("", WordUtils.wrap("", -1));

        String systemNewLine = SystemUtils.LINE_SEPARATOR;
        Assert.assertEquals("Here is" + systemNewLine + "one line of" + systemNewLine + "text",
                WordUtils.wrap("Here is one line of text", 10));
        Assert.assertEquals("Here is one line of text", WordUtils.wrap("Here is one line of text", 50));
        Assert.assertEquals("Here is" + systemNewLine + "one line of" + systemNewLine + "text",
                WordUtils.wrap("Here is one line of text", -1));
    }

    @Test
    public void testWrap_StringIntStringBoolean() {
        Assert.assertNull(WordUtils.wrap(null, 20, "\n", false));
        Assert.assertNull(WordUtils.wrap(null, 20, "\n", true));
        Assert.assertEquals("", WordUtils.wrap("", 20, "\n", false));
        Assert.assertEquals("", WordUtils.wrap("", 20, "\n", true));

        String input = "Here is one line of text";
        Assert.assertEquals("Here is\none line of\ntext", WordUtils.wrap(input, 10, "\n", false));
        Assert.assertEquals("Here is\none line of\ntext", WordUtils.wrap(input, 10, "\n", true));
        Assert.assertEquals("Here is one line of text", WordUtils.wrap(input, 50, "\n", false));
        Assert.assertEquals("Here is one line of text", WordUtils.wrap(input, 50, "\n", true));

        String url = "flam mylongwordwithnospacesinithere flam";
        Assert.assertEquals("flam\nmylongwordwithnospacesinithere\nflam", WordUtils.wrap(url, 10, "\n", false));
        Assert.assertEquals("flam\nmylongword\nwithnospac\nesinithere\nflam", WordUtils.wrap(url, 10, "\n", true));

        String singleLongWord = "alongwordwithnospaces";
        Assert.assertEquals("alongwordwithnospaces", WordUtils.wrap(singleLongWord, 5, "\n", false));
        Assert.assertEquals("along\nwordw\nithno\nspace\ns", WordUtils.wrap(singleLongWord, 5, "\n", true));

        String leadingSpaces = "   Here is   one line of text";
        Assert.assertEquals("   Here\nis   one\nline of\ntext", WordUtils.wrap(leadingSpaces, 10, "\n", false));

        Assert.assertEquals("H\ne\nr\ne", WordUtils.wrap("Here", 0, "\n", true));
        Assert.assertEquals("Here", WordUtils.wrap("Here", 0, "\n", false));

        String systemNewLine = SystemUtils.LINE_SEPARATOR;
        Assert.assertEquals("Here is" + systemNewLine + "one line of" + systemNewLine + "text",
                WordUtils.wrap(input, 10, null, false));
    }

    @Test
    public void testCapitalize_String() {
        Assert.assertNull(WordUtils.capitalize(null));
        Assert.assertEquals("", WordUtils.capitalize(""));
        Assert.assertEquals("I", WordUtils.capitalize("i"));
        Assert.assertEquals("I Am Fine", WordUtils.capitalize("i am fine"));
        Assert.assertEquals("I Am FINE", WordUtils.capitalize("i am FINE"));
        Assert.assertEquals("I  Am   FINE", WordUtils.capitalize("i  am   FINE"));
        Assert.assertEquals("  I  Am   FINE", WordUtils.capitalize("  i  am   FINE"));
    }

    @Test
    public void testCapitalize_StringCharArray() {
        Assert.assertNull(WordUtils.capitalize(null, new char[0]));
        Assert.assertEquals("", WordUtils.capitalize("", new char[0]));
        Assert.assertEquals("i am fine", WordUtils.capitalize("i am fine", new char[0]));

        Assert.assertEquals("I Am Fine", WordUtils.capitalize("i am fine", null));
        Assert.assertEquals("I aM.Fine", WordUtils.capitalize("i aM.fine", new char[]{'.' }));
        Assert.assertEquals("I Am.Fine", WordUtils.capitalize("i am.fine", new char[]{'.', ' ' }));
        Assert.assertEquals("I;am;fine", WordUtils.capitalize("i;am;fine", new char[]{';' }));
    }

    @Test
    public void testCapitalizeFully_String() {
        Assert.assertNull(WordUtils.capitalizeFully(null));
        Assert.assertEquals("", WordUtils.capitalizeFully(""));
        Assert.assertEquals("I", WordUtils.capitalizeFully("i"));
        Assert.assertEquals("I Am Fine", WordUtils.capitalizeFully("i am fine"));
        Assert.assertEquals("I Am Fine", WordUtils.capitalizeFully("i am FINE"));
        Assert.assertEquals("I  Am   Fine", WordUtils.capitalizeFully("i  am   FINE"));
    }

    @Test
    public void testCapitalizeFully_StringCharArray() {
        Assert.assertNull(WordUtils.capitalizeFully(null, new char[0]));
        Assert.assertEquals("", WordUtils.capitalizeFully("", new char[0]));
        Assert.assertEquals("i am fine", WordUtils.capitalizeFully("i am fine", new char[0]));

        Assert.assertEquals("I Am Fine", WordUtils.capitalizeFully("i am fine", null));
        Assert.assertEquals("I am.Fine", WordUtils.capitalizeFully("i aM.fine", new char[]{'.' }));
        Assert.assertEquals("I Am.Fine", WordUtils.capitalizeFully("i aM.fine", new char[]{'.', ' ' }));
    }

    @Test
    public void testUncapitalize_String() {
        Assert.assertNull(WordUtils.uncapitalize(null));
        Assert.assertEquals("", WordUtils.uncapitalize(""));
        Assert.assertEquals("i", WordUtils.uncapitalize("I"));
        Assert.assertEquals("i am fine", WordUtils.uncapitalize("I Am Fine"));
        Assert.assertEquals("i am fINE", WordUtils.uncapitalize("I Am FINE"));
        Assert.assertEquals("i  am   fINE", WordUtils.uncapitalize("I  Am   FINE"));
    }

    @Test
    public void testUncapitalize_StringCharArray() {
        Assert.assertNull(WordUtils.uncapitalize(null, new char[0]));
        Assert.assertEquals("", WordUtils.uncapitalize("", new char[0]));
        Assert.assertEquals("I AM FINE", WordUtils.uncapitalize("I AM FINE", new char[0]));

        Assert.assertEquals("i aM FINE", WordUtils.uncapitalize("I AM FINE", null));
        Assert.assertEquals("i AM.fINE", WordUtils.uncapitalize("I AM.FINE", new char[]{'.' }));
        Assert.assertEquals("i aM.fINE", WordUtils.uncapitalize("I AM.FINE", new char[]{'.', ' ' }));
    }

    @Test
    public void testSwapCase_String() {
        Assert.assertNull(WordUtils.swapCase(null));
        Assert.assertEquals("", WordUtils.swapCase(""));
        Assert.assertEquals("tHE DOG HAS A bone", WordUtils.swapCase("The dog has a BONE"));
        Assert.assertEquals("tHE DOG HAS A BONE", WordUtils.swapCase("The dog has a bone"));
        Assert.assertEquals("tHe dOg HaS A bOnE", WordUtils.swapCase("ThE DoG hAs a BoNe"));
        Assert.assertEquals("1234\t@#$%\n", WordUtils.swapCase("1234\t@#$%\n"));

        char titleCaseChar = '\u01F2';
        Assert.assertEquals("\u01F3", WordUtils.swapCase(String.valueOf(titleCaseChar)));
    }

    @Test
    public void testInitials_String() {
        Assert.assertNull(WordUtils.initials(null));
        Assert.assertEquals("", WordUtils.initials(""));
        Assert.assertEquals("BJL", WordUtils.initials("Ben John Lee"));
        Assert.assertEquals("BJ", WordUtils.initials("Ben J.Lee"));
        Assert.assertEquals("BJL", WordUtils.initials("  Ben   John   Lee  "));
    }

    @Test
    public void testInitials_StringCharArray() {
        Assert.assertNull(WordUtils.initials(null, new char[0]));
        Assert.assertEquals("", WordUtils.initials("", new char[0]));
        Assert.assertEquals("", WordUtils.initials("Ben John Lee", new char[0]));
        Assert.assertEquals("BJL", WordUtils.initials("Ben John Lee", null));
        Assert.assertEquals("BJL", WordUtils.initials("Ben J.Lee", new char[]{' ', '.' }));
        Assert.assertEquals("B", WordUtils.initials("BenJLee", new char[]{' ', '.' }));
        Assert.assertEquals("BJL", WordUtils.initials("..Ben..J..Lee..", new char[]{' ', '.' }));
    }

    @Test
    public void testAbbreviate() {
        Assert.assertNull(WordUtils.abbreviate(null, 1, 10, "..."));
        Assert.assertEquals("", WordUtils.abbreviate("", 1, 10, "..."));

        Assert.assertEquals("0123456789", WordUtils.abbreviate("0123456789", 0, 10, null));
        Assert.assertEquals("01234", WordUtils.abbreviate("0123456789", 0, 5, null));
        Assert.assertEquals("01234...", WordUtils.abbreviate("0123456789", 0, 5, "..."));
        Assert.assertEquals("0123456789", WordUtils.abbreviate("0123456789", 5, 2, null));

        Assert.assertEquals("Now is the time", WordUtils.abbreviate("Now is the time for all good men", 0, 40, null));
        Assert.assertEquals("Now is the", WordUtils.abbreviate("Now is the time for all good men", 1, 10, null));
        Assert.assertEquals("Now is the...", WordUtils.abbreviate("Now is the time for all good men", 1, 10, "..."));
        Assert.assertEquals("Now is the time", WordUtils.abbreviate("Now is the time for all good men", 20, 40, null));
        Assert.assertEquals("Now is the time...", WordUtils.abbreviate("Now is the time for all good men", 20, 40, "..."));

        Assert.assertEquals("Now is the", WordUtils.abbreviate("Now is the time for all good men", 10, 10, null));
        Assert.assertEquals("Now is the...", WordUtils.abbreviate("Now is the time for all good men", 10, 10, "..."));

        Assert.assertEquals("Now is the time for all good men",
                WordUtils.abbreviate("Now is the time for all good men", 0, -1, null));
        Assert.assertEquals("Now is the time for all good men",
                WordUtils.abbreviate("Now is the time for all good men", 0, 100, null));

        Assert.assertEquals("Now is the time", WordUtils.abbreviate("Now is the time for all good men", 10, 15, null));
        Assert.assertEquals("Now is the time", WordUtils.abbreviate("Now is the time", 10, 20, "..."));
        Assert.assertEquals("Now is the", WordUtils.abbreviate("Now is the time", 5, 12, null));
        Assert.assertEquals("Now is", WordUtils.abbreviate("Now is", 10, 20, "..."));

        Assert.assertEquals("Now is...", WordUtils.abbreviate("Now is the time", 4, 8, "..."));
        Assert.assertEquals("Now is the time", WordUtils.abbreviate("Now is the time", 20, 20, "..."));
    }
}
