package org.jsoup.nodes;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FormElementTest {

    @Test
    public void testElementsAndAddElement() {
        Tag tag = Tag.valueOf("form");
        Attributes attrs = new Attributes();
        FormElement form = new FormElement(tag, "http://example.com", attrs);
        Assert.assertNotNull(form.elements());
        Assert.assertEquals(0, form.elements().size());

        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        FormElement returnedForm = form.addElement(input);
        Assert.assertSame(form, returnedForm);
        Assert.assertEquals(1, form.elements().size());
        Assert.assertSame(input, form.elements().get(0));
    }

    @Test
    public void testSubmitDefaultGet() {
        Tag tag = Tag.valueOf("form");
        Attributes attrs = new Attributes();
        attrs.put("action", "http://example.com/search");
        FormElement form = new FormElement(tag, "http://example.com", attrs);

        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        input.attr("name", "q");
        input.val("jsoup");
        form.addElement(input);

        Connection connection = form.submit();
        Assert.assertNotNull(connection);
        Assert.assertEquals(Connection.Method.GET, connection.request().method());
        Assert.assertEquals("http://example.com/search", connection.request().url().toString());
        List<Connection.KeyVal> data = (List<Connection.KeyVal>) connection.request().data();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals("q", data.get(0).key());
        Assert.assertEquals("jsoup", data.get(0).value());
    }

    @Test
    public void testSubmitPostMethod() {
        Tag tag = Tag.valueOf("form");
        Attributes attrs = new Attributes();
        attrs.put("action", "http://example.com/login");
        attrs.put("method", "post");
        FormElement form = new FormElement(tag, "http://example.com", attrs);

        Connection connection = form.submit();
        Assert.assertNotNull(connection);
        Assert.assertEquals(Connection.Method.POST, connection.request().method());
        Assert.assertEquals("http://example.com/login", connection.request().url().toString());
    }

    @Test
    public void testSubmitBaseUriWhenNoAction() {
        Tag tag = Tag.valueOf("form");
        Attributes attrs = new Attributes();
        FormElement form = new FormElement(tag, "http://example.com/default", attrs);

        Connection connection = form.submit();
        Assert.assertNotNull(connection);
        Assert.assertEquals("http://example.com/default", connection.request().url().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmitThrowsWhenNoActionAndNoBaseUri() {
        Tag tag = Tag.valueOf("form");
        Attributes attrs = new Attributes();
        FormElement form = new FormElement(tag, "", attrs);
        form.submit();
    }

    @Test
    public void testFormDataWithVariousControls() {
        String html = "<form action='/submit' method='post'>" +
                "<input type='text' name='username' value='testuser'>" +
                "<input type='password' name='pass' value='secret'>" +
                "<input type='hidden' name='token' value='12345'>" +
                "<textarea name='comments'>Some comments</textarea>" +
                "<input type='checkbox' name='check1' value='on' checked>" +
                "<input type='checkbox' name='check2' value='off'>" +
                "<input type='radio' name='radio1' value='r1'>" +
                "<input type='radio' name='radio1' value='r2' checked>" +
                "<select name='select1'>" +
                "  <option value='opt1'>Option 1</option>" +
                "  <option value='opt2' selected>Option 2</option>" +
                "  <option value='opt3' selected>Option 3</option>" +
                "</select>" +
                "<select name='select2'>" +
                "  <option value='firstOpt'>First Option</option>" +
                "  <option value='secondOpt'>Second Option</option>" +
                "</select>" +
                "<select name='select3'></select>" +
                "<input type='text' name='' value='noname'>" +
                "<div name='invalid'>Not a form control</div>" +
                "</form>";

        Document doc = Jsoup.parse(html, "http://example.com");
        FormElement form = (FormElement) doc.select("form").first();
        List<Connection.KeyVal> data = form.formData();

        Assert.assertEquals(8, data.size());

        Assert.assertEquals("username", data.get(0).key());
        Assert.assertEquals("testuser", data.get(0).value());

        Assert.assertEquals("pass", data.get(1).key());
        Assert.assertEquals("secret", data.get(1).value());

        Assert.assertEquals("token", data.get(2).key());
        Assert.assertEquals("12345", data.get(2).value());

        Assert.assertEquals("comments", data.get(3).key());
        Assert.assertEquals("Some comments", data.get(3).value());

        Assert.assertEquals("check1", data.get(4).key());
        Assert.assertEquals("on", data.get(4).value());

        Assert.assertEquals("radio1", data.get(5).key());
        Assert.assertEquals("r2", data.get(5).value());

        Assert.assertEquals("select1", data.get(6).key());
        Assert.assertEquals("opt2", data.get(6).value());

        Assert.assertEquals("select1", data.get(7).key());
        Assert.assertEquals("opt3", data.get(7).value());
    }

    @Test
    public void testFormDataSelectDefaultFirstOption() {
        Tag tag = Tag.valueOf("form");
        FormElement form = new FormElement(tag, "http://example.com", new Attributes());

        Element select = new Element(Tag.valueOf("select"), "http://example.com");
        select.attr("name", "dropdown");
        Element opt1 = new Element(Tag.valueOf("option"), "http://example.com");
        opt1.val("defaultVal");
        Element opt2 = new Element(Tag.valueOf("option"), "http://example.com");
        opt2.val("otherVal");
        select.appendChild(opt1);
        select.appendChild(opt2);

        form.addElement(select);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals("dropdown", data.get(0).key());
        Assert.assertEquals("defaultVal", data.get(0).value());
    }

    @Test
    public void testFormDataSelectEmptyOptions() {
        Tag tag = Tag.valueOf("form");
        FormElement form = new FormElement(tag, "http://example.com", new Attributes());

        Element select = new Element(Tag.valueOf("select"), "http://example.com");
        select.attr("name", "emptyDropdown");
        form.addElement(select);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertTrue(data.isEmpty());
    }

    @Test
    public void testFormDataIgnoresDisabledOrNonSubmittableElements() {
        Tag tag = Tag.valueOf("form");
        FormElement form = new FormElement(tag, "http://example.com", new Attributes());

        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.attr("name", "ignoredDiv");
        form.addElement(div);

        Element emptyNameInput = new Element(Tag.valueOf("input"), "http://example.com");
        emptyNameInput.attr("name", "");
        emptyNameInput.val("val");
        form.addElement(emptyNameInput);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertTrue(data.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        Tag tag = Tag.valueOf("form");
        FormElement form1 = new FormElement(tag, "http://example.com", new Attributes());
        FormElement form2 = new FormElement(tag, "http://example.com", new Attributes());

        Assert.assertTrue(form1.equals(form1));
        Assert.assertFalse(form1.equals(null));
        Assert.assertFalse(form1.equals("string"));
        Assert.assertFalse(form1.equals(form2));
    }
}
