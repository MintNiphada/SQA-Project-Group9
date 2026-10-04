package org.jsoup.parser;

import org.junit.Assert;
import org.junit.Test;

public class TokeniserStateTest {

    private Tokeniser createTokeniser(String input) {
        CharacterReader reader = new CharacterReader(input);
        return new Tokeniser(reader, ParseErrorList.tracking(100));
    }

    private void runState(TokeniserState state, String input) {
        CharacterReader r = new CharacterReader(input);
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(100));
        while (!r.isEmpty()) {
            state.read(t, r);
        }
    }

    @Test
    public void testDataState() {
        CharacterReader r = new CharacterReader("&<a\u0000");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.Data.read(t, r);
        Assert.assertEquals(TokeniserState.CharacterReferenceInData, t.getState());

        r = new CharacterReader("<a");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.Data.read(t, r);
        Assert.assertEquals(TokeniserState.TagOpen, t.getState());

        r = new CharacterReader("\u0000");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.Data.read(t, r);
        Assert.assertEquals(1, t.getErrors().size());

        r = new CharacterReader("");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.Data.read(t, r);
    }

    @Test
    public void testCharacterReferenceInData() {
        CharacterReader r = new CharacterReader("amp;");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.CharacterReferenceInData.read(t, r);
        Assert.assertEquals(TokeniserState.Data, t.getState());
    }

    @Test
    public void testRcdataStates() {
        CharacterReader r = new CharacterReader("&<\u0000abc");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.Rcdata.read(t, r);
        Assert.assertEquals(TokeniserState.CharacterReferenceInRcdata, t.getState());

        TokeniserState.CharacterReferenceInRcdata.read(t, r);

        TokeniserState.Rcdata.read(t, r);
        Assert.assertEquals(TokeniserState.RcdataLessthanSign, t.getState());

        TokeniserState.Rcdata.read(t, r);
        TokeniserState.Rcdata.read(t, r);

        r = new CharacterReader("");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.Rcdata.read(t, r);
    }

    @Test
    public void testRawtextAndScriptData() {
        CharacterReader r = new CharacterReader("<foo\u0000");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.Rawtext.read(t, r);
        Assert.assertEquals(TokeniserState.RawtextLessthanSign, t.getState());

        TokeniserState.Rawtext.read(t, r);
        TokeniserState.Rawtext.read(t, r);

        r = new CharacterReader("");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.Rawtext.read(t, r);

        r = new CharacterReader("<foo\u0000");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptData.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataLessthanSign, t.getState());
        TokeniserState.ScriptData.read(t, r);
        TokeniserState.ScriptData.read(t, r);

        r = new CharacterReader("");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptData.read(t, r);
    }

    @Test
    public void testPlaintext() {
        CharacterReader r = new CharacterReader("abc\u0000");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.PLAINTEXT.read(t, r);
        TokeniserState.PLAINTEXT.read(t, r);

        r = new CharacterReader("");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.PLAINTEXT.read(t, r);
    }

    @Test
    public void testTagOpen() {
        CharacterReader r = new CharacterReader("!");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.TagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.MarkupDeclarationOpen, t.getState());

        r = new CharacterReader("/");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.TagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.EndTagOpen, t.getState());

        r = new CharacterReader("?");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.TagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.BogusComment, t.getState());

        r = new CharacterReader("a");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.TagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.TagName, t.getState());

        r = new CharacterReader("1");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.TagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.Data, t.getState());
    }

    @Test
    public void testEndTagOpen() {
        CharacterReader r = new CharacterReader("");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.EndTagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.Data, t.getState());

        r = new CharacterReader("a");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.EndTagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.TagName, t.getState());

        r = new CharacterReader(">");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.EndTagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.Data, t.getState());

        r = new CharacterReader("?");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.EndTagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.BogusComment, t.getState());
    }

    @Test
    public void testTagName() {
        String[] inputs = {"div ", "div/", "div>", "div\u0000", "div"};
        for (String input : inputs) {
            CharacterReader r = new CharacterReader(input);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            TokeniserState.TagName.read(t, r);
        }
    }

    @Test
    public void testRcdataLessthanSignAndEndTags() {
        CharacterReader r = new CharacterReader("/title>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.RcdataLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.RCDATAEndTagOpen, t.getState());

        TokeniserState.RCDATAEndTagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.RCDATAEndTagName, t.getState());

        t.createTagPending(false);
        t.tagPending.name("title");
        TokeniserState.RCDATAEndTagName.read(t, r);

        r = new CharacterReader("123");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.RcdataLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.Rcdata, t.getState());

        r = new CharacterReader("1");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.RCDATAEndTagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.Rcdata, t.getState());

        String[] endTagChars = {" ", "/", ">", "z"};
        for (String s : endTagChars) {
            r = new CharacterReader(s);
            t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(false);
            t.tagPending.name("title");
            t.createTempBuffer();
            TokeniserState.RCDATAEndTagName.read(t, r);
        }
    }

    @Test
    public void testRawtextEndTags() {
        CharacterReader r = new CharacterReader("/style>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.RawtextLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.RawtextEndTagOpen, t.getState());

        TokeniserState.RawtextEndTagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.RawtextEndTagName, t.getState());

        t.createTagPending(false);
        t.tagPending.name("style");
        TokeniserState.RawtextEndTagName.read(t, r);

        r = new CharacterReader("abc");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.RawtextLessthanSign.read(t, r);

        r = new CharacterReader("1");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.RawtextEndTagOpen.read(t, r);
    }

    @Test
    public void testScriptDataTransitions() {
        CharacterReader r = new CharacterReader("/!x");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEndTagOpen, t.getState());

        TokeniserState.ScriptDataLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEscapeStart, t.getState());

        TokeniserState.ScriptDataLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptData, t.getState());

        r = new CharacterReader("script>");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataEndTagOpen.read(t, r);
        t.createTagPending(false);
        t.tagPending.name("script");
        TokeniserState.ScriptDataEndTagName.read(t, r);

        r = new CharacterReader("1");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataEndTagOpen.read(t, r);

        r = new CharacterReader("-x");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataEscapeStart.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEscapeStartDash, t.getState());
        TokeniserState.ScriptDataEscapeStart.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptData, t.getState());

        r = new CharacterReader("-x");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataEscapeStartDash.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEscapedDashDash, t.getState());
        TokeniserState.ScriptDataEscapeStartDash.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptData, t.getState());
    }

    @Test
    public void testScriptDataEscapedStates() {
        CharacterReader r = new CharacterReader("-<\u0000abc");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataEscaped.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEscapedDash, t.getState());

        TokeniserState.ScriptDataEscaped.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEscapedLessthanSign, t.getState());

        TokeniserState.ScriptDataEscaped.read(t, r);
        TokeniserState.ScriptDataEscaped.read(t, r);

        r = new CharacterReader("");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataEscaped.read(t, r);

        String[] dashInputs = {"-", "<", "\u0000", "x", ""};
        for (String in : dashInputs) {
            r = new CharacterReader(in);
            t = new Tokeniser(r, ParseErrorList.tracking(10));
            TokeniserState.ScriptDataEscapedDash.read(t, r);
        }

        String[] dashDashInputs = {"-", "<", ">", "\u0000", "x", ""};
        for (String in : dashDashInputs) {
            r = new CharacterReader(in);
            t = new Tokeniser(r, ParseErrorList.tracking(10));
            TokeniserState.ScriptDataEscapedDashDash.read(t, r);
        }
    }

    @Test
    public void testScriptDataEscapedLessthanAndEndTag() {
        CharacterReader r = new CharacterReader("s/1");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataEscapedLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataDoubleEscapeStart, t.getState());

        TokeniserState.ScriptDataEscapedLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEscapedEndTagOpen, t.getState());

        TokeniserState.ScriptDataEscapedLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEscaped, t.getState());

        r = new CharacterReader("s1");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataEscapedEndTagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEscapedEndTagName, t.getState());

        TokeniserState.ScriptDataEscapedEndTagOpen.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataEscaped, t.getState());

        r = new CharacterReader("cript ");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.createTagPending(false);
        t.createTempBuffer();
        TokeniserState.ScriptDataEscapedEndTagName.read(t, r);
    }

    @Test
    public void testScriptDataDoubleEscapedStates() {
        CharacterReader r = new CharacterReader("-<\u0000\uffffabc");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataDoubleEscaped.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataDoubleEscapedDash, t.getState());

        TokeniserState.ScriptDataDoubleEscaped.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataDoubleEscapedLessthanSign, t.getState());

        TokeniserState.ScriptDataDoubleEscaped.read(t, r);
        TokeniserState.ScriptDataDoubleEscaped.read(t, r);
        TokeniserState.ScriptDataDoubleEscaped.read(t, r);

        String[] dashInputs = {"-", "<", "\u0000", "\uffff", "x"};
        for (String in : dashInputs) {
            r = new CharacterReader(in);
            t = new Tokeniser(r, ParseErrorList.tracking(10));
            TokeniserState.ScriptDataDoubleEscapedDash.read(t, r);
        }

        String[] dashDashInputs = {"-", "<", ">", "\u0000", "\uffff", "x"};
        for (String in : dashDashInputs) {
            r = new CharacterReader(in);
            t = new Tokeniser(r, ParseErrorList.tracking(10));
            TokeniserState.ScriptDataDoubleEscapedDashDash.read(t, r);
        }

        r = new CharacterReader("/x");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.ScriptDataDoubleEscapedLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataDoubleEscapeEnd, t.getState());
        TokeniserState.ScriptDataDoubleEscapedLessthanSign.read(t, r);
        Assert.assertEquals(TokeniserState.ScriptDataDoubleEscaped, t.getState());

        r = new CharacterReader("script ");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.createTempBuffer();
        TokeniserState.ScriptDataDoubleEscapeStart.read(t, r);

        r = new CharacterReader("script>");
        t = new Tokeniser(r, ParseErrorList.tracking(10));
        t.createTempBuffer();
        TokeniserState.ScriptDataDoubleEscapeEnd.read(t, r);
    }

    @Test
    public void testBeforeAttributeName() {
        String[] inputs = {" ", "/", ">", "\u0000", "", "\"", "'", "<", "=", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            TokeniserState.BeforeAttributeName.read(t, r);
        }
    }

    @Test
    public void testAttributeName() {
        String[] inputs = {"id ", "id/", "id=", "id>", "id\u0000", "id", "id\"", "id'", "id<"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            t.tagPending.newAttribute();
            TokeniserState.AttributeName.read(t, r);
        }
    }

    @Test
    public void testAfterAttributeName() {
        String[] inputs = {" ", "/", "=", ">", "\u0000", "", "\"", "'", "<", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            t.tagPending.newAttribute();
            TokeniserState.AfterAttributeName.read(t, r);
        }
    }

    @Test
    public void testBeforeAttributeValue() {
        String[] inputs = {" ", "\"", "&", "'", "\u0000", "", ">", "<", "=", "`", "a"};
        for (String in : inputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            t.tagPending.newAttribute();
            TokeniserState.BeforeAttributeValue.read(t, r);
        }
    }

    @Test
    public void testAttributeValueQuoted() {
        String[] dInputs = {"val\"", "val&amp;", "\"", "&amp;", "\u0000", ""};
        for (String in : dInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            t.tagPending.newAttribute();
            TokeniserState.AttributeValue_doubleQuoted.read(t, r);
        }

        String[] sInputs = {"val'", "val&amp;", "'", "&amp;", "\u0000", ""};
        for (String in : sInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            t.tagPending.newAttribute();
            TokeniserState.AttributeValue_singleQuoted.read(t, r);
        }
    }

    @Test
    public void testAttributeValueUnquotedAndAfter() {
        String[] uInputs = {"val ", "val&amp;", "val>", "val\u0000", "val", "val\"", "val'", "val<", "val=", "val`"};
        for (String in : uInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            t.tagPending.newAttribute();
            TokeniserState.AttributeValue_unquoted.read(t, r);
        }

        String[] afterInputs = {" ", "/", ">", "", "x"};
        for (String in : afterInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            TokeniserState.AfterAttributeValue_quoted.read(t, r);
        }

        String[] selfClosing = {">", "", "x"};
        for (String in : selfClosing) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createTagPending(true);
            TokeniserState.SelfClosingStartTag.read(t, r);
        }
    }

    @Test
    public void testBogusCommentAndMarkupDeclaration() {
        CharacterReader r = new CharacterReader("comment>");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.BogusComment.read(t, r);
        Assert.assertEquals(TokeniserState.Data, t.getState());

        String[] markupInputs = {"--comment", "DOCTYPE html", "[CDATA[data]]>", "bogus"};
        for (String in : markupInputs) {
            r = new CharacterReader(in);
            t = new Tokeniser(r, ParseErrorList.tracking(10));
            TokeniserState.MarkupDeclarationOpen.read(t, r);
        }
    }

    @Test
    public void testComments() {
        String[] startInputs = {"-", "\u0000", ">", "", "a"};
        for (String in : startInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createCommentPending();
            TokeniserState.CommentStart.read(t, r);
        }

        for (String in : startInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createCommentPending();
            TokeniserState.CommentStartDash.read(t, r);
        }

        String[] commentInputs = {"-", "\u0000", "", "text"};
        for (String in : commentInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createCommentPending();
            TokeniserState.Comment.read(t, r);
        }

        String[] endDashInputs = {"-", "\u0000", "", "a"};
        for (String in : endDashInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createCommentPending();
            TokeniserState.CommentEndDash.read(t, r);
        }

        String[] endInputs = {">", "\u0000", "!", "-", "", "a"};
        for (String in : endInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createCommentPending();
            TokeniserState.CommentEnd.read(t, r);
        }

        String[] bangInputs = {"-", ">", "\u0000", "", "a"};
        for (String in : bangInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createCommentPending();
            TokeniserState.CommentEndBang.read(t, r);
        }
    }

    @Test
    public void testDoctypeStates() {
        String[] doctypeInputs = {" ", "", ">", "x"};
        for (String in : doctypeInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            TokeniserState.Doctype.read(t, r);
        }

        String[] beforeNameInputs = {"html", " ", "\u0000", "", "1"};
        for (String in : beforeNameInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            TokeniserState.BeforeDoctypeName.read(t, r);
        }

        String[] nameInputs = {"html", ">", " ", "\u0000", "", "1"};
        for (String in : nameInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.DoctypeName.read(t, r);
        }

        String[] afterNameInputs = {"", " ", ">", "PUBLIC", "SYSTEM", "bogus"};
        for (String in : afterNameInputs) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.AfterDoctypeName.read(t, r);
        }
    }

    @Test
    public void testDoctypePublicAndSystemIdentifiers() {
        String[] pubKeywords = {" ", "\"", "'", ">", "", "x"};
        for (String in : pubKeywords) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.AfterDoctypePublicKeyword.read(t, r);
        }

        String[] beforePub = {" ", "\"", "'", ">", "", "x"};
        for (String in : beforePub) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.BeforeDoctypePublicIdentifier.read(t, r);
        }

        String[] pubDouble = {"\"", "\u0000", ">", "", "a"};
        for (String in : pubDouble) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.DoctypePublicIdentifier_doubleQuoted.read(t, r);
        }

        String[] pubSingle = {"'", "\u0000", ">", "", "a"};
        for (String in : pubSingle) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.DoctypePublicIdentifier_singleQuoted.read(t, r);
        }

        String[] afterPub = {" ", ">", "\"", "'", "", "x"};
        for (String in : afterPub) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.AfterDoctypePublicIdentifier.read(t, r);
        }

        String[] betweenPubSys = {" ", ">", "\"", "'", "", "x"};
        for (String in : betweenPubSys) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.BetweenDoctypePublicAndSystemIdentifiers.read(t, r);
        }

        String[] sysKeywords = {" ", ">", "\"", "'", "", "x"};
        for (String in : sysKeywords) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.AfterDoctypeSystemKeyword.read(t, r);
        }

        String[] beforeSys = {" ", "\"", "'", ">", "", "x"};
        for (String in : beforeSys) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.BeforeDoctypeSystemIdentifier.read(t, r);
        }

        String[] sysDouble = {"\"", "\u0000", ">", "", "a"};
        for (String in : sysDouble) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.DoctypeSystemIdentifier_doubleQuoted.read(t, r);
        }

        String[] sysSingle = {"'", "\u0000", ">", "", "a"};
        for (String in : sysSingle) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.DoctypeSystemIdentifier_singleQuoted.read(t, r);
        }

        String[] afterSys = {" ", ">", "", "x"};
        for (String in : afterSys) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.AfterDoctypeSystemIdentifier.read(t, r);
        }

        String[] bogusDoctype = {">", "", "x"};
        for (String in : bogusDoctype) {
            CharacterReader r = new CharacterReader(in);
            Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
            t.createDoctypePending();
            TokeniserState.BogusDoctype.read(t, r);
        }
    }

    @Test
    public void testCdataSection() {
        CharacterReader r = new CharacterReader("some cdata content]]>trailing");
        Tokeniser t = new Tokeniser(r, ParseErrorList.tracking(10));
        TokeniserState.CdataSection.read(t, r);
        Assert.assertEquals(TokeniserState.Data, t.getState());
    }

    @Test
    public void testEnumValuesAndValueOf() {
        TokeniserState[] states = TokeniserState.values();
        Assert.assertTrue(states.length > 0);
        Assert.assertEquals(TokeniserState.Data, TokeniserState.valueOf("Data"));
    }
}
