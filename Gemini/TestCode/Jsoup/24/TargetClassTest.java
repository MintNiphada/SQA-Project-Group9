package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

public class TokeniserStateTest {

    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        return new Tokeniser(reader, ParseErrorList.tracking(100));
    }

    private void runState(TokeniserState state, String input) {
        CharacterReader reader = new CharacterReader(input);
        Tokeniser tokeniser = new Tokeniser(reader, ParseErrorList.tracking(100));
        state.read(tokeniser, reader);
    }

    @Test
    public void testEnumValuesAndValueOf() {
        TokeniserState[] states = TokeniserState.values();
        Assert.assertTrue(states.length > 0);
        Assert.assertEquals(TokeniserState.Data, TokeniserState.valueOf("Data"));
    }

    @Test
    public void testDataState() {
        runState(TokeniserState.Data, "&amp;");
        runState(TokeniserState.Data, "<tag");
        runState(TokeniserState.Data, "\u0000abc");
        runState(TokeniserState.Data, "");
        runState(TokeniserState.Data, "plain text");
    }

    @Test
    public void testCharacterReferenceInData() {
        // Character reference resolving to a char
        CharacterReader r1 = new CharacterReader("lt;rest");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        TokeniserState.CharacterReferenceInData.read(t1, r1);

        // Character reference returning null
        CharacterReader r2 = new CharacterReader(" unknown;");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(100));
        TokeniserState.CharacterReferenceInData.read(t2, r2);
    }

    @Test
    public void testRcdata() {
        runState(TokeniserState.Rcdata, "&amp;");
        runState(TokeniserState.Rcdata, "<title>");
        runState(TokeniserState.Rcdata, "\u0000text");
        runState(TokeniserState.Rcdata, "");
        runState(TokeniserState.Rcdata, "some rcdata text");
    }

    @Test
    public void testCharacterReferenceInRcdata() {
        CharacterReader r1 = new CharacterReader("amp;");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        TokeniserState.CharacterReferenceInRcdata.read(t1, r1);

        CharacterReader r2 = new CharacterReader(" notaref");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(100));
        TokeniserState.CharacterReferenceInRcdata.read(t2, r2);
    }

    @Test
    public void testRawtext() {
        runState(TokeniserState.Rawtext, "<style>");
        runState(TokeniserState.Rawtext, "\u0000text");
        runState(TokeniserState.Rawtext, "");
        runState(TokeniserState.Rawtext, "rawtext data");
    }

    @Test
    public void testScriptData() {
        runState(TokeniserState.ScriptData, "<script>");
        runState(TokeniserState.ScriptData, "\u0000text");
        runState(TokeniserState.ScriptData, "");
        runState(TokeniserState.ScriptData, "var x = 1;");
    }

    @Test
    public void testPlaintext() {
        runState(TokeniserState.PLAINTEXT, "\u0000test");
        runState(TokeniserState.PLAINTEXT, "");
        runState(TokeniserState.PLAINTEXT, "plain text content");
    }

    @Test
    public void testTagOpen() {
        runState(TokeniserState.TagOpen, "!DOCTYPE html>");
        runState(TokeniserState.TagOpen, "/div>");
        runState(TokeniserState.TagOpen, "?xml version>");
        runState(TokeniserState.TagOpen, "div>");
        runState(TokeniserState.TagOpen, "123 invalid tag");
    }

    @Test
    public void testEndTagOpen() {
        runState(TokeniserState.EndTagOpen, "");
        runState(TokeniserState.EndTagOpen, "div>");
        runState(TokeniserState.EndTagOpen, ">");
        runState(TokeniserState.EndTagOpen, "123");
    }

    @Test
    public void testTagName() {
        String[] inputs = new String[] {
            "div\tattr", "div\nattr", "div\fattr", "div attr",
            "div/self", "div>rest", "div\u0000rest", "div"
        };
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            TokeniserState.TagName.read(t, r);
        }
    }

    @Test
    public void testRcdataLessthanSign() {
        // Case 1: matches '/'
        CharacterReader r1 = new CharacterReader("/title>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        TokeniserState.RcdataLessthanSign.read(t1, r1);

        // Case 2: matchesLetter && !containsIgnoreCase
        CharacterReader r2 = new CharacterReader("b>something");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(100));
        TokeniserState.RcdataLessthanSign.read(t2, r2);

        // Case 3: matchesLetter && containsIgnoreCase
        CharacterReader r3 = new CharacterReader("b>something</title>");
        Tokeniser t3 = new Tokeniser(r3, ParseErrorList.tracking(100));
        TokeniserState.RcdataLessthanSign.read(t3, r3);

        // Case 4: other
        CharacterReader r4 = new CharacterReader("!something");
        Tokeniser t4 = new Tokeniser(r4, ParseErrorList.tracking(100));
        TokeniserState.RcdataLessthanSign.read(t4, r4);
    }

    @Test
    public void testRCDATAEndTagOpen() {
        CharacterReader r1 = new CharacterReader("title>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        t1.createTempBuffer();
        TokeniserState.RCDATAEndTagOpen.read(t1, r1);

        CharacterReader r2 = new CharacterReader("123>");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(100));
        t2.createTempBuffer();
        TokeniserState.RCDATAEndTagOpen.read(t2, r2);
    }

    @Test
    public void testRCDATAEndTagName() {
        // letter sequence
        CharacterReader r1 = new CharacterReader("itle>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        t1.createTagPending(false);
        t1.createTempBuffer();
        TokeniserState.RCDATAEndTagName.read(t1, r1);

        // appropriate vs non-appropriate end tag
        String[] appropriateInputs = new String[] {"\t", "\n", "\f", " ", "/", ">", "z"};
        for (String in : appropriateInputs) {
            // Appropriate tag
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(false);
            t.createTempBuffer();
            TokeniserState.RCDATAEndTagName.read(t, r);

            // Inappropriate tag
            CharacterReader rInapp = new CharacterReader(in);
            Tokeniser tInapp = new Tokeniser(rInapp, ParseErrorList.tracking(100));
            tInapp.createTagPending(false);
            tInapp.tagPending.appendTagName("different");
            tInapp.createTempBuffer();
            TokeniserState.RCDATAEndTagName.read(tInapp, rInapp);
        }
    }

    @Test
    public void testRawtextLessthanSign() {
        CharacterReader r1 = new CharacterReader("/style>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        TokeniserState.RawtextLessthanSign.read(t1, r1);

        CharacterReader r2 = new CharacterReader("style>");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(100));
        TokeniserState.RawtextLessthanSign.read(t2, r2);
    }

    @Test
    public void testRawtextEndTagOpen() {
        CharacterReader r1 = new CharacterReader("style>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        TokeniserState.RawtextEndTagOpen.read(t1, r1);

        CharacterReader r2 = new CharacterReader("123>");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(100));
        TokeniserState.RawtextEndTagOpen.read(t2, r2);
    }

    @Test
    public void testRawtextEndTagName() {
        // Letter
        CharacterReader r1 = new CharacterReader("tyle>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        t1.createTagPending(false);
        t1.createTempBuffer();
        TokeniserState.RawtextEndTagName.read(t1, r1);

        // Switch branches when isAppropriateEndTagToken is true
        String[] branches = new String[] {"\t", "\n", "\f", " ", "/", ">", "x"};
        for (String b : branches) {
            CharacterReader r = new CharacterReader(b);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(false);
            t.createTempBuffer();
            TokeniserState.RawtextEndTagName.read(t, r);
        }

        // When not appropriate end tag token or empty
        CharacterReader rEmpty = new CharacterReader("");
        Tokeniser tEmpty = new Tokeniser(rEmpty, ParseErrorList.tracking(100));
        tEmpty.createTagPending(false);
        tEmpty.createTempBuffer();
        TokeniserState.RawtextEndTagName.read(tEmpty, rEmpty);
    }

    @Test
    public void testScriptDataLessthanSign() {
        runState(TokeniserState.ScriptDataLessthanSign, "/script>");
        runState(TokeniserState.ScriptDataLessthanSign, "!-- comment -->");
        runState(TokeniserState.ScriptDataLessthanSign, "other");
    }

    @Test
    public void testScriptDataEndTagOpen() {
        CharacterReader r1 = new CharacterReader("script>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        TokeniserState.ScriptDataEndTagOpen.read(t1, r1);

        CharacterReader r2 = new CharacterReader("123>");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(100));
        TokeniserState.ScriptDataEndTagOpen.read(t2, r2);
    }

    @Test
    public void testScriptDataEndTagName() {
        // Letter sequence
        CharacterReader r1 = new CharacterReader("cript>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        t1.createTagPending(false);
        t1.createTempBuffer();
        TokeniserState.ScriptDataEndTagName.read(t1, r1);

        // Appropriate token switch branches
        String[] branches = new String[] {"\t", "\n", "\f", " ", "/", ">", "x"};
        for (String b : branches) {
            CharacterReader r = new CharacterReader(b);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(false);
            t.createTempBuffer();
            TokeniserState.ScriptDataEndTagName.read(t, r);
        }

        // Not appropriate or empty
        CharacterReader rEmpty = new CharacterReader("");
        Tokeniser tEmpty = new Tokeniser(rEmpty, ParseErrorList.tracking(100));
        tEmpty.createTagPending(false);
        tEmpty.createTempBuffer();
        TokeniserState.ScriptDataEndTagName.read(tEmpty, rEmpty);
    }

    @Test
    public void testScriptDataEscapeStartAndDash() {
        runState(TokeniserState.ScriptDataEscapeStart, "-");
        runState(TokeniserState.ScriptDataEscapeStart, "x");

        runState(TokeniserState.ScriptDataEscapeStartDash, "-");
        runState(TokeniserState.ScriptDataEscapeStartDash, "x");
    }

    @Test
    public void testScriptDataEscaped() {
        runState(TokeniserState.ScriptDataEscaped, "");
        runState(TokeniserState.ScriptDataEscaped, "-");
        runState(TokeniserState.ScriptDataEscaped, "<");
        runState(TokeniserState.ScriptDataEscaped, "\u0000");
        runState(TokeniserState.ScriptDataEscaped, "abc");
    }

    @Test
    public void testScriptDataEscapedDash() {
        runState(TokeniserState.ScriptDataEscapedDash, "");
        runState(TokeniserState.ScriptDataEscapedDash, "-");
        runState(TokeniserState.ScriptDataEscapedDash, "<");
        runState(TokeniserState.ScriptDataEscapedDash, "\u0000");
        runState(TokeniserState.ScriptDataEscapedDash, "abc");
    }

    @Test
    public void testScriptDataEscapedDashDash() {
        runState(TokeniserState.ScriptDataEscapedDashDash, "");
        runState(TokeniserState.ScriptDataEscapedDashDash, "-");
        runState(TokeniserState.ScriptDataEscapedDashDash, "<");
        runState(TokeniserState.ScriptDataEscapedDashDash, ">");
        runState(TokeniserState.ScriptDataEscapedDashDash, "\u0000");
        runState(TokeniserState.ScriptDataEscapedDashDash, "abc");
    }

    @Test
    public void testScriptDataEscapedLessthanSign() {
        runState(TokeniserState.ScriptDataEscapedLessthanSign, "s");
        runState(TokeniserState.ScriptDataEscapedLessthanSign, "/script>");
        runState(TokeniserState.ScriptDataEscapedLessthanSign, "123");
    }

    @Test
    public void testScriptDataEscapedEndTagOpen() {
        CharacterReader r1 = new CharacterReader("script>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        TokeniserState.ScriptDataEscapedEndTagOpen.read(t1, r1);

        CharacterReader r2 = new CharacterReader("123>");
        Tokeniser t2 = new Tokeniser(r2, ParseErrorList.tracking(100));
        TokeniserState.ScriptDataEscapedEndTagOpen.read(t2, r2);
    }

    @Test
    public void testScriptDataEscapedEndTagName() {
        CharacterReader r1 = new CharacterReader("cript>");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        t1.createTagPending(false);
        t1.createTempBuffer();
        TokeniserState.ScriptDataEscapedEndTagName.read(t1, r1);

        String[] branches = new String[] {"\t", "\n", "\f", " ", "/", ">", "x"};
        for (String b : branches) {
            CharacterReader r = new CharacterReader(b);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(false);
            t.createTempBuffer();
            TokeniserState.ScriptDataEscapedEndTagName.read(t, r);
        }

        CharacterReader rEmpty = new CharacterReader("");
        Tokeniser tEmpty = new Tokeniser(rEmpty, ParseErrorList.tracking(100));
        tEmpty.createTagPending(false);
        tEmpty.createTempBuffer();
        TokeniserState.ScriptDataEscapedEndTagName.read(tEmpty, rEmpty);
    }

    @Test
    public void testScriptDataDoubleEscapeStart() {
        // Letters
        CharacterReader r1 = new CharacterReader("cript ");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        t1.createTempBuffer();
        TokeniserState.ScriptDataDoubleEscapeStart.read(t1, r1);

        // When dataBuffer is "script"
        String[] scriptBranches = new String[] {"\t", "\n", "\f", " ", "/", ">"};
        for (String b : scriptBranches) {
            CharacterReader r = new CharacterReader(b);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTempBuffer();
            t.dataBuffer.append("script");
            TokeniserState.ScriptDataDoubleEscapeStart.read(t, r);
        }

        // When dataBuffer is not "script"
        CharacterReader rOther = new CharacterReader(" ");
        Tokeniser tOther = new Tokeniser(rOther, ParseErrorList.tracking(100));
        tOther.createTempBuffer();
        tOther.dataBuffer.append("other");
        TokeniserState.ScriptDataDoubleEscapeStart.read(tOther, rOther);

        // Default branch
        CharacterReader rDef = new CharacterReader("z");
        Tokeniser tDef = new Tokeniser(rDef, ParseErrorList.tracking(100));
        tDef.createTempBuffer();
        TokeniserState.ScriptDataDoubleEscapeStart.read(tDef, rDef);
    }

    @Test
    public void testScriptDataDoubleEscaped() {
        runState(TokeniserState.ScriptDataDoubleEscaped, "-");
        runState(TokeniserState.ScriptDataDoubleEscaped, "<");
        runState(TokeniserState.ScriptDataDoubleEscaped, "\u0000");
        runState(TokeniserState.ScriptDataDoubleEscaped, "");
        runState(TokeniserState.ScriptDataDoubleEscaped, "abc");
    }

    @Test
    public void testScriptDataDoubleEscapedDash() {
        runState(TokeniserState.ScriptDataDoubleEscapedDash, "-");
        runState(TokeniserState.ScriptDataDoubleEscapedDash, "<");
        runState(TokeniserState.ScriptDataDoubleEscapedDash, "\u0000");
        runState(TokeniserState.ScriptDataDoubleEscapedDash, "");
        runState(TokeniserState.ScriptDataDoubleEscapedDash, "abc");
    }

    @Test
    public void testScriptDataDoubleEscapedDashDash() {
        runState(TokeniserState.ScriptDataDoubleEscapedDashDash, "-");
        runState(TokeniserState.ScriptDataDoubleEscapedDashDash, "<");
        runState(TokeniserState.ScriptDataDoubleEscapedDashDash, ">");
        runState(TokeniserState.ScriptDataDoubleEscapedDashDash, "\u0000");
        runState(TokeniserState.ScriptDataDoubleEscapedDashDash, "");
        runState(TokeniserState.ScriptDataDoubleEscapedDashDash, "abc");
    }

    @Test
    public void testScriptDataDoubleEscapedLessthanSign() {
        runState(TokeniserState.ScriptDataDoubleEscapedLessthanSign, "/script>");
        runState(TokeniserState.ScriptDataDoubleEscapedLessthanSign, "abc");
    }

    @Test
    public void testScriptDataDoubleEscapeEnd() {
        // Letter
        CharacterReader r1 = new CharacterReader("cript ");
        Tokeniser t1 = new Tokeniser(r1, ParseErrorList.tracking(100));
        t1.createTempBuffer();
        TokeniserState.ScriptDataDoubleEscapeEnd.read(t1, r1);

        // When dataBuffer is "script"
        String[] scriptBranches = new String[] {"\t", "\n", "\f", " ", "/", ">"};
        for (String b : scriptBranches) {
            CharacterReader r = new CharacterReader(b);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTempBuffer();
            t.dataBuffer.append("script");
            TokeniserState.ScriptDataDoubleEscapeEnd.read(t, r);
        }

        // When dataBuffer is not "script"
        CharacterReader rOther = new CharacterReader(" ");
        Tokeniser tOther = new Tokeniser(rOther, ParseErrorList.tracking(100));
        tOther.createTempBuffer();
        tOther.dataBuffer.append("style");
        TokeniserState.ScriptDataDoubleEscapeEnd.read(tOther, rOther);

        // Default
        CharacterReader rDef = new CharacterReader("z");
        Tokeniser tDef = new Tokeniser(rDef, ParseErrorList.tracking(100));
        tDef.createTempBuffer();
        TokeniserState.ScriptDataDoubleEscapeEnd.read(tDef, rDef);
    }

    @Test
    public void testBeforeAttributeName() {
        String[] inputs = new String[] {
            "\t", "\n", "\f", " ", "/", ">", "\u0000", "", "\"", "'", "<", "=", "a"
        };
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            TokeniserState.BeforeAttributeName.read(t, r);
        }
    }

    @Test
    public void testAttributeName() {
        String[] inputs = new String[] {
            "attr\t", "attr\n", "attr\f", "attr ", "attr/", "attr=", "attr>",
            "attr\u0000", "attr", "attr\"", "attr'", "attr<"
        };
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            t.tagPending.newAttribute();
            TokeniserState.AttributeName.read(t, r);
        }
    }

    @Test
    public void testAfterAttributeName() {
        String[] inputs = new String[] {
            "\t", "\n", "\f", " ", "/", "=", ">", "\u0000", "", "\"", "'", "<", "a"
        };
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            TokeniserState.AfterAttributeName.read(t, r);
        }
    }

    @Test
    public void testBeforeAttributeValue() {
        String[] inputs = new String[] {
            "\t", "\n", "\f", " ", "\"", "&", "'", "\u0000", "", ">", "<", "=", "`", "a"
        };
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            TokeniserState.BeforeAttributeValue.read(t, r);
        }
    }

    @Test
    public void testAttributeValueDoubleQuoted() {
        String[] inputs = new String[] {
            "val\"", "val&amp;rest\"", "val& notaref\"", "val\u0000\"", "val"
        };
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            TokeniserState.AttributeValue_doubleQuoted.read(t, r);
        }
    }

    @Test
    public void testAttributeValueSingleQuoted() {
        String[] inputs = new String[] {
            "val'", "val&amp;rest'", "val& notaref'", "val\u0000'", "val"
        };
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            TokeniserState.AttributeValue_singleQuoted.read(t, r);
        }
    }

    @Test
    public void testAttributeValueUnquoted() {
        String[] inputs = new String[] {
            "val\t", "val\n", "val\f", "val ", "val&amp;rest ", "val& notaref ",
            "val>", "val\u0000 ", "val", "val\" ", "val' ", "val< ", "val= ", "val` "
        };
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            TokeniserState.AttributeValue_unquoted.read(t, r);
        }
    }

    @Test
    public void testAfterAttributeValueQuoted() {
        String[] inputs = new String[] {"\t", "\n", "\f", " ", "/", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            TokeniserState.AfterAttributeValue_quoted.read(t, r);
        }
    }

    @Test
    public void testSelfClosingStartTag() {
        String[] inputs = new String[] {">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createTagPending(true);
            TokeniserState.SelfClosingStartTag.read(t, r);
        }
    }

    @Test
    public void testBogusComment() {
        CharacterReader r = new CharacterReader(" comment>");
        r.advance(); // simulate that we consumed a char
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
        TokeniserState.BogusComment.read(t, r);
    }

    @Test
    public void testMarkupDeclarationOpen() {
        runState(TokeniserState.MarkupDeclarationOpen, "-- comment -->");
        runState(TokeniserState.MarkupDeclarationOpen, "DOCTYPE html>");
        runState(TokeniserState.MarkupDeclarationOpen, "[CDATA[data]]>");
        runState(TokeniserState.MarkupDeclarationOpen, "invalid>");
    }

    @Test
    public void testCommentStart() {
        String[] inputs = new String[] {"-", "\u0000", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createCommentPending();
            TokeniserState.CommentStart.read(t, r);
        }
    }

    @Test
    public void testCommentStartDash() {
        String[] inputs = new String[] {"-", "\u0000", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createCommentPending();
            TokeniserState.CommentStartDash.read(t, r);
        }
    }

    @Test
    public void testComment() {
        String[] inputs = new String[] {"-", "\u0000", "", "some comment text-"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createCommentPending();
            TokeniserState.Comment.read(t, r);
        }
    }

    @Test
    public void testCommentEndDash() {
        String[] inputs = new String[] {"-", "\u0000", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createCommentPending();
            TokeniserState.CommentEndDash.read(t, r);
        }
    }

    @Test
    public void testCommentEnd() {
        String[] inputs = new String[] {">", "\u0000", "!", "-", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createCommentPending();
            TokeniserState.CommentEnd.read(t, r);
        }
    }

    @Test
    public void testCommentEndBang() {
        String[] inputs = new String[] {"-", ">", "\u0000", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createCommentPending();
            TokeniserState.CommentEndBang.read(t, r);
        }
    }

    @Test
    public void testDoctype() {
        String[] inputs = new String[] {"\t", "\n", "\f", " ", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            TokeniserState.Doctype.read(t, r);
        }
    }

    @Test
    public void testBeforeDoctypeName() {
        String[] inputs = new String[] {"html", "\t", "\n", "\f", " ", "\u0000", "", "1"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            TokeniserState.BeforeDoctypeName.read(t, r);
        }
    }

    @Test
    public void testDoctypeName() {
        String[] inputs = new String[] {"tml>", ">", "\t", "\n", "\f", " ", "\u0000", "", "1"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.DoctypeName.read(t, r);
        }
    }

    @Test
    public void testAfterDoctypeName() {
        String[] inputs = new String[] {"", "\t", "\n", "\f", " ", ">", "PUBLIC", "SYSTEM", "INVALID"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.AfterDoctypeName.read(t, r);
        }
    }

    @Test
    public void testAfterDoctypePublicKeyword() {
        String[] inputs = new String[] {"\t", "\n", "\f", " ", "\"", "'", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.AfterDoctypePublicKeyword.read(t, r);
        }
    }

    @Test
    public void testBeforeDoctypePublicIdentifier() {
        String[] inputs = new String[] {"\t", "\n", "\f", " ", "\"", "'", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.BeforeDoctypePublicIdentifier.read(t, r);
        }
    }

    @Test
    public void testDoctypePublicIdentifierDoubleQuoted() {
        String[] inputs = new String[] {"\"", "\u0000", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, r);
        }
    }

    @Test
    public void testDoctypePublicIdentifierSingleQuoted() {
        String[] inputs = new String[] {"'", "\u0000", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.DoctypePublicIdentifier_singleQuoted.read(t, r);
        }
    }

    @Test
    public void testAfterDoctypePublicIdentifier() {
        String[] inputs = new String[] {"\t", "\n", "\f", " ", ">", "\"", "'", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.AfterDoctypePublicIdentifier.read(t, r);
        }
    }

    @Test
    public void testBetweenDoctypePublicAndSystemIdentifiers() {
        String[] inputs = new String[] {"\t", "\n", "\f", " ", ">", "\"", "'", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, r);
        }
    }

    @Test
    public void testAfterDoctypeSystemKeyword() {
        String[] inputs = new String[] {"\t", "\n", "\f", " ", ">", "\"", "'", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.AfterDoctypeSystemKeyword.read(t, r);
        }
    }

    @Test
    public void testBeforeDoctypeSystemIdentifier() {
        String[] inputs = new String[] {"\t", "\n", "\f", " ", "\"", "'", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.BeforeDoctypeSystemIdentifier.read(t, r);
        }
    }

    @Test
    public void testDoctypeSystemIdentifierDoubleQuoted() {
        String[] inputs = new String[] {"\"", "\u0000", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, r);
        }
    }

    @Test
    public void testDoctypeSystemIdentifierSingleQuoted() {
        String[] inputs = new String[] {"'", "\u0000", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.DoctypeSystemIdentifier_singleQuoted.read(t, r);
        }
    }

    @Test
    public void testAfterDoctypeSystemIdentifier() {
        String[] inputs = new String[] {"\t", "\n", "\f", " ", ">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.AfterDoctypeSystemIdentifier.read(t, r);
        }
    }

    @Test
    public void testBogusDoctype() {
        String[] inputs = new String[] {">", "", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
            t.createDoctypePending();
            TokeniserState.BogusDoctype.read(t, r);
        }
    }

    @Test
    public void testCdataSection() {
        CharacterReader r = new CharacterReader("some cdata data]]>rest");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
        TokeniserState.CdataSection.read(t, r);
    }
}
