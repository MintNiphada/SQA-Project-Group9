package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FormElementTest {

    @Test
    public void testElementsAndAddElement() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Assert.assertTrue(form.elements().isEmpty());

        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        form.addElement(input);

        Assert.assertEquals(1, form.elements().size());
        Assert.assertSame(input, form.elements().get(0));
    }

    @Test
    public void testSubmitWithActionAndPost() {
        Document doc = Jsoup.parse("<form action='/submit' method='POST'><input name='q' value='jsoup'/></form>", "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();
        Assert.assertEquals("http://example.com/submit", con.request().url().toExternalForm());
        Assert.assertEquals(Connection.Method.POST, con.request().method());
        List<Connection.KeyVal> data = (List<Connection.KeyVal>) con.request().data();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals("q", data.get(0).key());
        Assert.assertEquals("jsoup", data.get(0).value());
    }

    @Test
    public void testSubmitWithoutActionUsesBaseUriAndDefaultGet() {
        Document doc = Jsoup.parse("<form method='get'><input name='test' value='val'/></form>", "http://example.com/page.html");
        FormElement form = (FormElement) doc.select("form").first();

        Connection con = form.submit();
        Assert.assertEquals("http://example.com/page.html", con.request().url().toExternalForm());
        Assert.assertEquals(Connection.Method.GET, con.request().method());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmitThrowsWhenNoActionAndNoBaseUri() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        form.submit();
    }

    @Test
    public void testFormDataIgnoresNonSubmittableOrDisabledOrNoName() {
        String html = "<form action='/'>" +
                "<div name='divname'>Text</div>" +
                "<input name='disabledField' value='val' disabled />" +
                "<input name='' value='noname' />" +
                "<input name='valid' value='ok' />" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        Assert.assertEquals(1, data.size());
        Assert.assertEquals("valid", data.get(0).key());
        Assert.assertEquals("ok", data.get(0).value());
    }

    @Test
    public void testFormDataSelectWithOptions() {
        String html = "<form action='/'>" +
                "<select name='multi' multiple>" +
                "<option value='one' selected>One</option>" +
                "<option value='two'>Two</option>" +
                "<option value='three' selected>Three</option>" +
                "</select>" +
                "<select name='single'>" +
                "<option value='first'>First</option>" +
                "<option value='second'>Second</option>" +
                "</select>" +
                "<select name='empty'></select>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        Assert.assertEquals(3, data.size());
        Assert.assertEquals("multi", data.get(0).key());
        Assert.assertEquals("one", data.get(0).value());
        Assert.assertEquals("multi", data.get(1).key());
        Assert.assertEquals("three", data.get(1).value());
        Assert.assertEquals("single", data.get(2).key());
        Assert.assertEquals("first", data.get(2).value());
    }

    @Test
    public void testFormDataCheckboxesAndRadios() {
        String html = "<form action='/'>" +
                "<input type='checkbox' name='c1' value='v1' checked />" +
                "<input type='checkbox' name='c2' checked />" +
                "<input type='checkbox' name='c3' value='v3' />" +
                "<input type='radio' name='r1' value='v4' checked />" +
                "<input type='radio' name='r2' checked />" +
                "<input type='radio' name='r3' value='v6' />" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        Assert.assertEquals(4, data.size());
        Assert.assertEquals("c1", data.get(0).key());
        Assert.assertEquals("v1", data.get(0).value());
        Assert.assertEquals("c2", data.get(1).key());
        Assert.assertEquals("on", data.get(1).value());
        Assert.assertEquals("r1", data.get(2).key());
        Assert.assertEquals("v4", data.get(2).value());
        Assert.assertEquals("r2", data.get(3).key());
        Assert.assertEquals("on", data.get(3).value());
    }

    @Test
    public void testFormDataOtherInputTypes() {
        String html = "<form action='/'>" +
                "<input type='text' name='username' value='john' />" +
                "<input type='hidden' name='token' value='12345' />" +
                "<textarea name='bio'>Hello World</textarea>" +
                "</form>";
        Document doc = Jsoup.parse(html);
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        Assert.assertEquals(3, data.size());
        Assert.assertEquals("username", data.get(0).key());
        Assert.assertEquals("john", data.get(0).value());
        Assert.assertEquals("token", data.get(1).key());
        Assert.assertEquals("12345", data.get(1).value());
        Assert.assertEquals("bio", data.get(2).key());
        Assert.assertEquals("Hello World", data.get(2).value());
    }
}
