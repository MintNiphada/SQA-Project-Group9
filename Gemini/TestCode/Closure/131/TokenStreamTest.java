package com.google.javascript.rhino;

import org.junit.Assert;
import org.junit.Test;

public class TokenStreamTest {

    @Test
    public void testConstructor() {
        TokenStream stream = new TokenStream();
        Assert.assertNotNull(stream);
    }

    @Test
    public void testIsKeywordLength2() {
        // Valid length 2 keywords
        Assert.assertTrue(TokenStream.isKeyword("if"));
        Assert.assertTrue(TokenStream.isKeyword("in"));
        Assert.assertTrue(TokenStream.isKeyword("do"));

        // Invalid length 2 matching charAt(1)
        Assert.assertFalse(TokenStream.isKeyword("af"));
        Assert.assertFalse(TokenStream.isKeyword("of"));
        Assert.assertFalse(TokenStream.isKeyword("an"));
        Assert.assertFalse(TokenStream.isKeyword("on"));
        Assert.assertFalse(TokenStream.isKeyword("to"));
        Assert.assertFalse(TokenStream.isKeyword("go"));

        // Invalid length 2 non-matching charAt(1)
        Assert.assertFalse(TokenStream.isKeyword("at"));
        Assert.assertFalse(TokenStream.isKeyword("is"));
        Assert.assertFalse(TokenStream.isKeyword("as"));
        Assert.assertFalse(TokenStream.isKeyword("by"));
    }

    @Test
    public void testIsKeywordLength3() {
        // Valid length 3 keywords
        Assert.assertTrue(TokenStream.isKeyword("for"));
        Assert.assertTrue(TokenStream.isKeyword("int"));
        Assert.assertTrue(TokenStream.isKeyword("new"));
        Assert.assertTrue(TokenStream.isKeyword("try"));
        Assert.assertTrue(TokenStream.isKeyword("var"));

        // Partial match with 'f'
        Assert.assertFalse(TokenStream.isKeyword("far"));
        Assert.assertFalse(TokenStream.isKeyword("fox"));
        Assert.assertFalse(TokenStream.isKeyword("foo"));

        // Partial match with 'i'
        Assert.assertFalse(TokenStream.isKeyword("inn"));
        Assert.assertFalse(TokenStream.isKeyword("ist"));
        Assert.assertFalse(TokenStream.isKeyword("ice"));

        // Partial match with 'n': new
        Assert.assertFalse(TokenStream.isKeyword("net"));
        Assert.assertFalse(TokenStream.isKeyword("now"));
        Assert.assertFalse(TokenStream.isKeyword("not"));

        // Partial match with 't': try
        Assert.assertFalse(TokenStream.isKeyword("toy"));
        Assert.assertFalse(TokenStream.isKeyword("tra"));
        Assert.assertFalse(TokenStream.isKeyword("two"));

        // Partial match with 'v': var
        Assert.assertFalse(TokenStream.isKeyword("van"));
        Assert.assertFalse(TokenStream.isKeyword("vor"));
        Assert.assertFalse(TokenStream.isKeyword("via"));

        // Other chars
        Assert.assertFalse(TokenStream.isKeyword("abc"));
        Assert.assertFalse(TokenStream.isKeyword("xyz"));
    }

    @Test
    public void testIsKeywordLength4() {
        // Valid keywords
        Assert.assertTrue(TokenStream.isKeyword("byte"));
        Assert.assertTrue(TokenStream.isKeyword(new String("byte")));
        Assert.assertTrue(TokenStream.isKeyword("case"));
        Assert.assertTrue(TokenStream.isKeyword("char"));
        Assert.assertTrue(TokenStream.isKeyword("else"));
        Assert.assertTrue(TokenStream.isKeyword("enum"));
        Assert.assertTrue(TokenStream.isKeyword("goto"));
        Assert.assertTrue(TokenStream.isKeyword(new String("goto")));
        Assert.assertTrue(TokenStream.isKeyword("long"));
        Assert.assertTrue(TokenStream.isKeyword(new String("long")));
        Assert.assertTrue(TokenStream.isKeyword("null"));
        Assert.assertTrue(TokenStream.isKeyword(new String("null")));
        Assert.assertTrue(TokenStream.isKeyword("true"));
        Assert.assertTrue(TokenStream.isKeyword("this"));
        Assert.assertTrue(TokenStream.isKeyword("void"));
        Assert.assertTrue(TokenStream.isKeyword(new String("void")));
        Assert.assertTrue(TokenStream.isKeyword("with"));
        Assert.assertTrue(TokenStream.isKeyword(new String("with")));

        // Partial matches for 'b'
        Assert.assertFalse(TokenStream.isKeyword("bake"));
        Assert.assertFalse(TokenStream.isKeyword("best"));

        // Partial matches for 'c'
        Assert.assertFalse(TokenStream.isKeyword("care"));
        Assert.assertFalse(TokenStream.isKeyword("cone"));
        Assert.assertFalse(TokenStream.isKeyword("cher"));
        Assert.assertFalse(TokenStream.isKeyword("chat"));
        Assert.assertFalse(TokenStream.isKeyword("city"));
        Assert.assertFalse(TokenStream.isKeyword("camp"));

        // Partial matches for 'e'
        Assert.assertFalse(TokenStream.isKeyword("ease"));
        Assert.assertFalse(TokenStream.isKeyword("elre"));
        Assert.assertFalse(TokenStream.isKeyword("exam"));
        Assert.assertFalse(TokenStream.isKeyword("enim"));
        Assert.assertFalse(TokenStream.isKeyword("exit"));
        Assert.assertFalse(TokenStream.isKeyword("edge"));

        // Partial matches for 'g', 'l', 'n'
        Assert.assertFalse(TokenStream.isKeyword("game"));
        Assert.assertFalse(TokenStream.isKeyword("lock"));
        Assert.assertFalse(TokenStream.isKeyword("nine"));

        // Partial matches for 't'
        Assert.assertFalse(TokenStream.isKeyword("tree"));
        Assert.assertFalse(TokenStream.isKeyword("take"));
        Assert.assertFalse(TokenStream.isKeyword("thus"));
        Assert.assertFalse(TokenStream.isKeyword("tabs"));
        Assert.assertFalse(TokenStream.isKeyword("that"));
        Assert.assertFalse(TokenStream.isKeyword("test"));

        // Partial matches for 'v', 'w'
        Assert.assertFalse(TokenStream.isKeyword("view"));
        Assert.assertFalse(TokenStream.isKeyword("wash"));

        // Other char start
        Assert.assertFalse(TokenStream.isKeyword("zoom"));
        Assert.assertFalse(TokenStream.isKeyword("park"));
    }

    @Test
    public void testIsKeywordLength5() {
        // Valid keywords
        Assert.assertTrue(TokenStream.isKeyword("class"));
        Assert.assertTrue(TokenStream.isKeyword(new String("class")));
        Assert.assertTrue(TokenStream.isKeyword("break"));
        Assert.assertTrue(TokenStream.isKeyword(new String("break")));
        Assert.assertTrue(TokenStream.isKeyword("while"));
        Assert.assertTrue(TokenStream.isKeyword(new String("while")));
        Assert.assertTrue(TokenStream.isKeyword("false"));
        Assert.assertTrue(TokenStream.isKeyword(new String("false")));
        Assert.assertTrue(TokenStream.isKeyword("const"));
        Assert.assertTrue(TokenStream.isKeyword(new String("const")));
        Assert.assertTrue(TokenStream.isKeyword("final"));
        Assert.assertTrue(TokenStream.isKeyword(new String("final")));
        Assert.assertTrue(TokenStream.isKeyword("float"));
        Assert.assertTrue(TokenStream.isKeyword(new String("float")));
        Assert.assertTrue(TokenStream.isKeyword("short"));
        Assert.assertTrue(TokenStream.isKeyword(new String("short")));
        Assert.assertTrue(TokenStream.isKeyword("super"));
        Assert.assertTrue(TokenStream.isKeyword(new String("super")));
        Assert.assertTrue(TokenStream.isKeyword("throw"));
        Assert.assertTrue(TokenStream.isKeyword(new String("throw")));
        Assert.assertTrue(TokenStream.isKeyword("catch"));
        Assert.assertTrue(TokenStream.isKeyword(new String("catch")));

        // Partial matching failures for charAt(2)
        Assert.assertFalse(TokenStream.isKeyword("start")); // 'a'
        Assert.assertFalse(TokenStream.isKeyword("cheat")); // 'e'
        Assert.assertFalse(TokenStream.isKeyword("point")); // 'i'
        Assert.assertFalse(TokenStream.isKeyword("hello")); // 'l'

        // 'n' at index 2
        Assert.assertFalse(TokenStream.isKeyword("candy")); // 'c'
        Assert.assertFalse(TokenStream.isKeyword("fancy")); // 'f'
        Assert.assertFalse(TokenStream.isKeyword("panda")); // other

        // 'o' at index 2
        Assert.assertFalse(TokenStream.isKeyword("flock")); // 'f'
        Assert.assertFalse(TokenStream.isKeyword("shock")); // 's'
        Assert.assertFalse(TokenStream.isKeyword("block")); // other

        Assert.assertFalse(TokenStream.isKeyword("apple")); // 'p'
        Assert.assertFalse(TokenStream.isKeyword("agree")); // 'r'
        Assert.assertFalse(TokenStream.isKeyword("water")); // 't'

        // other char at index 2
        Assert.assertFalse(TokenStream.isKeyword("world"));
        Assert.assertFalse(TokenStream.isKeyword("quick"));
    }

    @Test
    public void testIsKeywordLength6() {
        // Valid keywords
        Assert.assertTrue(TokenStream.isKeyword("native"));
        Assert.assertTrue(TokenStream.isKeyword(new String("native")));
        Assert.assertTrue(TokenStream.isKeyword("delete"));
        Assert.assertTrue(TokenStream.isKeyword(new String("delete")));
        Assert.assertTrue(TokenStream.isKeyword("return"));
        Assert.assertTrue(TokenStream.isKeyword(new String("return")));
        Assert.assertTrue(TokenStream.isKeyword("throws"));
        Assert.assertTrue(TokenStream.isKeyword(new String("throws")));
        Assert.assertTrue(TokenStream.isKeyword("import"));
        Assert.assertTrue(TokenStream.isKeyword(new String("import")));
        Assert.assertTrue(TokenStream.isKeyword("double"));
        Assert.assertTrue(TokenStream.isKeyword(new String("double")));
        Assert.assertTrue(TokenStream.isKeyword("static"));
        Assert.assertTrue(TokenStream.isKeyword(new String("static")));
        Assert.assertTrue(TokenStream.isKeyword("public"));
        Assert.assertTrue(TokenStream.isKeyword(new String("public")));
        Assert.assertTrue(TokenStream.isKeyword("switch"));
        Assert.assertTrue(TokenStream.isKeyword(new String("switch")));
        Assert.assertTrue(TokenStream.isKeyword("export"));
        Assert.assertTrue(TokenStream.isKeyword(new String("export")));
        Assert.assertTrue(TokenStream.isKeyword("typeof"));
        Assert.assertTrue(TokenStream.isKeyword(new String("typeof")));

        // Partial matching failures for charAt(1)
        Assert.assertFalse(TokenStream.isKeyword("banner")); // 'a'
        Assert.assertFalse(TokenStream.isKeyword("decide")); // 'e' -> 'd'
        Assert.assertFalse(TokenStream.isKeyword("record")); // 'e' -> 'r'
        Assert.assertFalse(TokenStream.isKeyword("people")); // 'e' -> other
        Assert.assertFalse(TokenStream.isKeyword("choose")); // 'h'
        Assert.assertFalse(TokenStream.isKeyword("smooth")); // 'm'
        Assert.assertFalse(TokenStream.isKeyword("format")); // 'o'
        Assert.assertFalse(TokenStream.isKeyword("street")); // 't'
        Assert.assertFalse(TokenStream.isKeyword("summer")); // 'u'
        Assert.assertFalse(TokenStream.isKeyword("twenty")); // 'w'
        Assert.assertFalse(TokenStream.isKeyword("expand")); // 'x'
        Assert.assertFalse(TokenStream.isKeyword("system")); // 'y'

        // other char at index 1
        Assert.assertFalse(TokenStream.isKeyword("banana"));
        Assert.assertFalse(TokenStream.isKeyword("coffee"));
    }

    @Test
    public void testIsKeywordLength7() {
        // Valid keywords
        Assert.assertTrue(TokenStream.isKeyword("package"));
        Assert.assertTrue(TokenStream.isKeyword(new String("package")));
        Assert.assertTrue(TokenStream.isKeyword("default"));
        Assert.assertTrue(TokenStream.isKeyword(new String("default")));
        Assert.assertTrue(TokenStream.isKeyword("finally"));
        Assert.assertTrue(TokenStream.isKeyword(new String("finally")));
        Assert.assertTrue(TokenStream.isKeyword("boolean"));
        Assert.assertTrue(TokenStream.isKeyword(new String("boolean")));
        Assert.assertTrue(TokenStream.isKeyword("private"));
        Assert.assertTrue(TokenStream.isKeyword(new String("private")));
        Assert.assertTrue(TokenStream.isKeyword("extends"));
        Assert.assertTrue(TokenStream.isKeyword(new String("extends")));

        // Partial matching failures
        Assert.assertFalse(TokenStream.isKeyword("gallery")); // 'a'
        Assert.assertFalse(TokenStream.isKeyword("general")); // 'e'
        Assert.assertFalse(TokenStream.isKeyword("picture")); // 'i'
        Assert.assertFalse(TokenStream.isKeyword("counter")); // 'o'
        Assert.assertFalse(TokenStream.isKeyword("program")); // 'r'
        Assert.assertFalse(TokenStream.isKeyword("example")); // 'x'

        // other char at index 1
        Assert.assertFalse(TokenStream.isKeyword("student"));
        Assert.assertFalse(TokenStream.isKeyword("teacher"));
    }

    @Test
    public void testIsKeywordLength8() {
        // Valid keywords
        Assert.assertTrue(TokenStream.isKeyword("abstract"));
        Assert.assertTrue(TokenStream.isKeyword(new String("abstract")));
        Assert.assertTrue(TokenStream.isKeyword("continue"));
        Assert.assertTrue(TokenStream.isKeyword(new String("continue")));
        Assert.assertTrue(TokenStream.isKeyword("debugger"));
        Assert.assertTrue(TokenStream.isKeyword(new String("debugger")));
        Assert.assertTrue(TokenStream.isKeyword("function"));
        Assert.assertTrue(TokenStream.isKeyword(new String("function")));
        Assert.assertTrue(TokenStream.isKeyword("volatile"));
        Assert.assertTrue(TokenStream.isKeyword(new String("volatile")));

        // Partial matching failures
        Assert.assertFalse(TokenStream.isKeyword("activity")); // 'a'
        Assert.assertFalse(TokenStream.isKeyword("constant")); // 'c'
        Assert.assertFalse(TokenStream.isKeyword("database")); // 'd'
        Assert.assertFalse(TokenStream.isKeyword("fraction")); // 'f'
        Assert.assertFalse(TokenStream.isKeyword("valuable")); // 'v'

        // other char at index 0
        Assert.assertFalse(TokenStream.isKeyword("hospital"));
        Assert.assertFalse(TokenStream.isKeyword("standard"));
    }

    @Test
    public void testIsKeywordLength9() {
        // Valid keywords
        Assert.assertTrue(TokenStream.isKeyword("interface"));
        Assert.assertTrue(TokenStream.isKeyword(new String("interface")));
        Assert.assertTrue(TokenStream.isKeyword("protected"));
        Assert.assertTrue(TokenStream.isKeyword(new String("protected")));
        Assert.assertTrue(TokenStream.isKeyword("transient"));
        Assert.assertTrue(TokenStream.isKeyword(new String("transient")));

        // Partial matching failures
        Assert.assertFalse(TokenStream.isKeyword("important")); // 'i'
        Assert.assertFalse(TokenStream.isKeyword("president")); // 'p'
        Assert.assertFalse(TokenStream.isKeyword("treatment")); // 't'

        // other char at index 0
        Assert.assertFalse(TokenStream.isKeyword("community"));
        Assert.assertFalse(TokenStream.isKeyword("knowledge"));
    }

    @Test
    public void testIsKeywordLength10() {
        // Valid keywords
        Assert.assertTrue(TokenStream.isKeyword("implements"));
        Assert.assertTrue(TokenStream.isKeyword(new String("implements")));
        Assert.assertTrue(TokenStream.isKeyword("instanceof"));
        Assert.assertTrue(TokenStream.isKeyword(new String("instanceof")));

        // Partial matching failures
        Assert.assertFalse(TokenStream.isKeyword("employment")); // 'm'
        Assert.assertFalse(TokenStream.isKeyword("university")); // 'n'

        // other char at index 1
        Assert.assertFalse(TokenStream.isKeyword("technology"));
        Assert.assertFalse(TokenStream.isKeyword("government"));
    }

    @Test
    public void testIsKeywordLength12() {
        // Valid keywords
        Assert.assertTrue(TokenStream.isKeyword("synchronized"));
        Assert.assertTrue(TokenStream.isKeyword(new String("synchronized")));

        // Partial matching failure
        Assert.assertFalse(TokenStream.isKeyword("organization"));
    }

    @Test
    public void testIsKeywordOtherLengths() {
        Assert.assertFalse(TokenStream.isKeyword(""));
        Assert.assertFalse(TokenStream.isKeyword("a"));
        Assert.assertFalse(TokenStream.isKeyword("i"));
        Assert.assertFalse(TokenStream.isKeyword("information")); // length 11
        Assert.assertFalse(TokenStream.isKeyword("advertisement")); // length 13
        Assert.assertFalse(TokenStream.isKeyword("unquestionable")); // length 14
    }

    @Test(expected = NullPointerException.class)
    public void testIsKeywordNull() {
        TokenStream.isKeyword(null);
    }

    @Test
    public void testIsJSIdentifier() {
        // Empty string
        Assert.assertFalse(TokenStream.isJSIdentifier(""));

        // Invalid first character
        Assert.assertFalse(TokenStream.isJSIdentifier("1abc"));
        Assert.assertFalse(TokenStream.isJSIdentifier("-abc"));
        Assert.assertFalse(TokenStream.isJSIdentifier(" abc"));
        Assert.assertFalse(TokenStream.isJSIdentifier("+xyz"));

        // Single valid character
        Assert.assertTrue(TokenStream.isJSIdentifier("a"));
        Assert.assertTrue(TokenStream.isJSIdentifier("Z"));
        Assert.assertTrue(TokenStream.isJSIdentifier("$"));
        Assert.assertTrue(TokenStream.isJSIdentifier("_"));

        // Multi-character valid identifiers
        Assert.assertTrue(TokenStream.isJSIdentifier("foo"));
        Assert.assertTrue(TokenStream.isJSIdentifier("_bar"));
        Assert.assertTrue(TokenStream.isJSIdentifier("$baz"));
        Assert.assertTrue(TokenStream.isJSIdentifier("camelCaseVar"));
        Assert.assertTrue(TokenStream.isJSIdentifier("var123_456$"));

        // Multi-character invalid identifiers (invalid middle or end character)
        Assert.assertFalse(TokenStream.isJSIdentifier("foo-bar"));
        Assert.assertFalse(TokenStream.isJSIdentifier("foo bar"));
        Assert.assertFalse(TokenStream.isJSIdentifier("foo.bar"));
        Assert.assertFalse(TokenStream.isJSIdentifier("foo@bar"));
        Assert.assertFalse(TokenStream.isJSIdentifier("foo+bar"));
        Assert.assertFalse(TokenStream.isJSIdentifier("foo#"));
    }

    @Test(expected = NullPointerException.class)
    public void testIsJSIdentifierNull() {
        TokenStream.isJSIdentifier(null);
    }
}