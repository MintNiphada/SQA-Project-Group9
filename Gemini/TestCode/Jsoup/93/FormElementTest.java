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
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Assert.assertEquals(0, form.elements().size());

        Element input = new Element(Tag.valueOf("input"), "http://example.com");
        FormElement chained = form.addElement(input);
        Assert.assertSame(form, chained);
        Assert.assertEquals(1, form.elements().size());
        Assert.assertTrue(form.elements().contains(input));
    }

    @Test
    public void testRemoveChild() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());
        Element input1 = new Element(Tag.valueOf("input"), "http://example.com");
        Element input2 = new Element(Tag.valueOf("input"), "http://example.com");

        form.appendChild(input1);
        form.appendChild(input2);
        form.addElement(input1);
        form.addElement(input2);

        Assert.assertEquals(2, form.elements().size());
        form.removeChild(input1);
        Assert.assertEquals(1, form.elements().size());
        Assert.assertFalse(form.elements().contains(input1));
        Assert.assertTrue(form.elements().contains(input2));
    }

    @Test
    public void testFormDataFiltering() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());

        Element div = new Element(Tag.valueOf("div"), "http://example.com");
        div.attr("name", "notFormSubmittable");
        form.addElement(div);

        Element disabled = new Element(Tag.valueOf("input"), "http://example.com");
        disabled.attr("name", "disabledInput");
        disabled.attr("disabled", "disabled");
        form.addElement(disabled);

        Element noName = new Element(Tag.valueOf("input"), "http://example.com");
        noName.attr("value", "someValue");
        form.addElement(noName);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertEquals(0, data.size());
    }

    @Test
    public void testFormDataTextInput() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());

        Element text = new Element(Tag.valueOf("input"), "http://example.com");
        text.attr("name", "username");
        text.attr("type", "text");
        text.attr("value", "john_doe");
        form.addElement(text);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals("username", data.get(0).key());
        Assert.assertEquals("john_doe", data.get(0).value());
    }

    @Test
    public void testFormDataSelectWithOptionsSelected() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());

        Element select = new Element(Tag.valueOf("select"), "http://example.com");
        select.attr("name", "colors");

        Element opt1 = new Element(Tag.valueOf("option"), "http://example.com");
        opt1.attr("value", "red");
        opt1.attr("selected", "selected");

        Element opt2 = new Element(Tag.valueOf("option"), "http://example.com");
        opt2.attr("value", "blue");
        opt2.attr("selected", "selected");

        Element opt3 = new Element(Tag.valueOf("option"), "http://example.com");
        opt3.attr("value", "green");

        select.appendChild(opt1);
        select.appendChild(opt2);
        select.appendChild(opt3);
        form.addElement(select);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertEquals(2, data.size());
        Assert.assertEquals("colors", data.get(0).key());
        Assert.assertEquals("red", data.get(0).value());
        Assert.assertEquals("colors", data.get(1).key());
        Assert.assertEquals("blue", data.get(1).value());
    }

    @Test
    public void testFormDataSelectNoOptionSelected() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());

        Element select = new Element(Tag.valueOf("select"), "http://example.com");
        select.attr("name", "colors");

        Element opt1 = new Element(Tag.valueOf("option"), "http://example.com");
        opt1.attr("value", "firstOption");

        Element opt2 = new Element(Tag.valueOf("option"), "http://example.com");
        opt2.attr("value", "secondOption");

        select.appendChild(opt1);
        select.appendChild(opt2);
        form.addElement(select);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertEquals(1, data.size());
        Assert.assertEquals("colors", data.get(0).key());
        Assert.assertEquals("firstOption", data.get(0).value());
    }

    @Test
    public void testFormDataSelectEmptyOptions() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());

        Element select = new Element(Tag.valueOf("select"), "http://example.com");
        select.attr("name", "emptySelect");
        form.addElement(select);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertEquals(0, data.size());
    }

    @Test
    public void testFormDataCheckboxAndRadio() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com", new Attributes());

        Element checkboxUnchecked = new Element(Tag.valueOf("input"), "http://example.com");
        checkboxUnchecked.attr("type", "checkbox");
        checkboxUnchecked.attr("name", "cbUnchecked");
        checkboxUnchecked.attr("value", "val1");
        form.addElement(checkboxUnchecked);

        Element checkboxCheckedWithValue = new Element(Tag.valueOf("input"), "http://example.com");
        checkboxCheckedWithValue.attr("type", "checkbox");
        checkboxCheckedWithValue.attr("name", "cbVal");
        checkboxCheckedWithValue.attr("value", "val2");
        checkboxCheckedWithValue.attr("checked", "checked");
        form.addElement(checkboxCheckedWithValue);

        Element checkboxCheckedNoValue = new Element(Tag.valueOf("input"), "http://example.com");
        checkboxCheckedNoValue.attr("type", "CHECKBOX");
        checkboxCheckedNoValue.attr("name", "cbNoVal");
        checkboxCheckedNoValue.attr("checked", "checked");
        form.addElement(checkboxCheckedNoValue);

        Element radioUnchecked = new Element(Tag.valueOf("input"), "http://example.com");
        radioUnchecked.attr("type", "radio");
        radioUnchecked.attr("name", "radioGroup");
        radioUnchecked.attr("value", "r1");
        form.addElement(radioUnchecked);

        Element radioChecked = new Element(Tag.valueOf("input"), "http://example.com");
        radioChecked.attr("type", "RADIO");
        radioChecked.attr("name", "radioGroup");
        radioChecked.attr("value", "r2");
        radioChecked.attr("checked", "checked");
        form.addElement(radioChecked);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertEquals(3, data.size());

        Assert.assertEquals("cbVal", data.get(0).key());
        Assert.assertEquals("val2", data.get(0).value());

        Assert.assertEquals("cbNoVal", data.get(1).key());
        Assert.assertEquals("on", data.get(1).value());

        Assert.assertEquals("radioGroup", data.get(2).key());
        Assert.assertEquals("r2", data.get(2).value());
    }

    @Test
    public void testSubmitWithActionAndPostMethod() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com/base/", new Attributes());
        form.attr("action", "submit.php");
        form.attr("method", "post");

        Element input = new Element(Tag.valueOf("input"), "http://example.com/base/");
        input.attr("name", "q");
        input.attr("value", "jsoup");
        form.addElement(input);

        Connection conn = form.submit();
        Assert.assertEquals(Connection.Method.POST, conn.request().method());
        Assert.assertEquals("http://example.com/base/submit.php", conn.request().url().toExternalForm());
        Assert.assertEquals(1, conn.request().data().size());
        Assert.assertEquals("q", conn.request().data().iterator().next().key());
        Assert.assertEquals("jsoup", conn.request().data().iterator().next().value());
    }

    @Test
    public void testSubmitWithoutActionUsesBaseUriAndDefaultGet() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com/form", new Attributes());

        Connection conn = form.submit();
        Assert.assertEquals(Connection.Method.GET, conn.request().method());
        Assert.assertEquals("http://example.com/form", conn.request().url().toExternalForm());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubmitEmptyActionThrowsException() {
        FormElement form = new FormElement(Tag.valueOf("form"), "", new Attributes());
        form.submit();
    }

    @Test
    public void testSubmitExplicitGetMethod() {
        FormElement form = new FormElement(Tag.valueOf("form"), "http://example.com/form", new Attributes());
        form.attr("method", "GET");

        Connection conn = form.submit();
        Assert.assertEquals(Connection.Method.GET, conn.request().method());
    }

    @Test
    public void testParsedDocumentFormIntegration() {
        String html = "<form action='/submit' method='POST'>" +
                "<input name='username' value='admin'/>" +
                "<input name='disabledField' value='skip' disabled/>" +
                "<input type='checkbox' name='agree' checked/>" +
                "<select name='role'><option value='user'>User</option><option value='admin' selected>Admin</option></select>" +
                "</form>";
        Document doc = Jsoup.parse(html, "http://example.com/");
        FormElement form = (FormElement) doc.select("form").first();
        Assert.assertNotNull(form);

        List<Connection.KeyVal> data = form.formData();
        Assert.assertEquals(3, data.size());
        Assert.assertEquals("username", data.get(0).key());
        Assert.assertEquals("admin", data.get(0).value());
        Assert.assertEquals("agree", data.get(1).key());
        Assert.assertEquals("on", data.get(1).value());
        Assert.assertEquals("role", data.get(2).key());
        Assert.assertEquals("admin", data.get(2).value());

        Connection conn = form.submit();
        Assert.assertEquals(Connection.Method.POST, conn.request().method());
        Assert.assertEquals("http://example.com/submit", conn.request().url().toExternalForm());
    }
}
