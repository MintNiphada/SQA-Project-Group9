package com.fasterxml.jackson.core.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.io.SerializedString;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class DefaultPrettyPrinterTest {

    private final JsonFactory jsonFactory = new JsonFactory();

    static class CustomTestIndenter implements DefaultPrettyPrinter.Indenter, java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final boolean inline;
        int callCount = 0;
        int lastLevel = -1;

        CustomTestIndenter(boolean inline) {
            this.inline = inline;
        }

        @Override
        public void writeIndentation(JsonGenerator g, int level) throws IOException {
            callCount++;
            lastLevel = level;
            g.writeRaw("[indent:" + level + "]");
        }

        @Override
        public boolean isInline() {
            return inline;
        }
    }

    @Test
    public void testConstructorsAndDefaults() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertNotNull(pp._rootSeparator);
        assertEquals(" ", pp._rootSeparator.getValue());
        assertTrue(pp._spacesInObjectEntries);
        assertSame(DefaultPrettyPrinter.FixedSpaceIndenter.instance, pp._arrayIndenter);
        assertSame(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE, pp._objectIndenter);

        DefaultPrettyPrinter ppString = new DefaultPrettyPrinter("\n");
        assertEquals("\n", ppString._rootSeparator.getValue());

        DefaultPrettyPrinter ppNullString = new DefaultPrettyPrinter((String) null);
        assertNull(ppNullString._rootSeparator);

        SerializedString sString = new SerializedString("\t");
        DefaultPrettyPrinter ppSerializable = new DefaultPrettyPrinter(sString);
        assertSame(sString, ppSerializable._rootSeparator);

        DefaultPrettyPrinter ppNullSerializable = new DefaultPrettyPrinter((SerializedString) null);
        assertNull(ppNullSerializable._rootSeparator);

        DefaultPrettyPrinter ppCopy = new DefaultPrettyPrinter(pp);
        assertEquals(" ", ppCopy._rootSeparator.getValue());
        assertSame(pp._arrayIndenter, ppCopy._arrayIndenter);
        assertSame(pp._objectIndenter, ppCopy._objectIndenter);
        assertEquals(pp._spacesInObjectEntries, ppCopy._spacesInObjectEntries);

        SerializedString customRoot = new SerializedString("ROOT");
        DefaultPrettyPrinter ppCopyWithRoot = new DefaultPrettyPrinter(pp, customRoot);
        assertSame(customRoot, ppCopyWithRoot._rootSeparator);
    }

    @Test
    public void testWithRootSeparatorSerializableString() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertSame(pp, pp.withRootSeparator(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR));

        SerializedString equalRoot = new SerializedString(" ");
        assertSame(pp, pp.withRootSeparator(equalRoot));

        SerializedString newRoot = new SerializedString("\r\n");
        DefaultPrettyPrinter changed = pp.withRootSeparator(newRoot);
        assertNotSame(pp, changed);
        assertSame(newRoot, changed._rootSeparator);

        DefaultPrettyPrinter withNull = pp.withRootSeparator((SerializedString) null);
        assertNotSame(pp, withNull);
        assertNull(withNull._rootSeparator);
        assertSame(withNull, withNull.withRootSeparator((SerializedString) null));
    }

    @Test
    public void testWithRootSeparatorString() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter changed = pp.withRootSeparator("\n\n");
        assertNotSame(pp, changed);
        assertEquals("\n\n", changed._rootSeparator.getValue());

        DefaultPrettyPrinter withNull = pp.withRootSeparator((String) null);
        assertNotSame(pp, withNull);
        assertNull(withNull._rootSeparator);
    }

    @Test
    public void testIndentArraysWith() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        CustomTestIndenter custom = new CustomTestIndenter(false);
        pp.indentArraysWith(custom);
        assertSame(custom, pp._arrayIndenter);

        pp.indentArraysWith(null);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, pp._arrayIndenter);
    }

    @Test
    public void testIndentObjectsWith() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        CustomTestIndenter custom = new CustomTestIndenter(true);
        pp.indentObjectsWith(custom);
        assertSame(custom, pp._objectIndenter);

        pp.indentObjectsWith(null);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, pp._objectIndenter);
    }

    @Test
    public void testWithArrayIndenter() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertSame(pp, pp.withArrayIndenter(DefaultPrettyPrinter.FixedSpaceIndenter.instance));

        CustomTestIndenter custom = new CustomTestIndenter(false);
        DefaultPrettyPrinter changed = pp.withArrayIndenter(custom);
        assertNotSame(pp, changed);
        assertSame(custom, changed._arrayIndenter);
        assertSame(changed, changed.withArrayIndenter(custom));

        DefaultPrettyPrinter withNull = pp.withArrayIndenter(null);
        assertNotSame(pp, withNull);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, withNull._arrayIndenter);
        assertSame(withNull, withNull.withArrayIndenter(null));
    }

    @Test
    public void testWithObjectIndenter() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertSame(pp, pp.withObjectIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE));

        CustomTestIndenter custom = new CustomTestIndenter(false);
        DefaultPrettyPrinter changed = pp.withObjectIndenter(custom);
        assertNotSame(pp, changed);
        assertSame(custom, changed._objectIndenter);
        assertSame(changed, changed.withObjectIndenter(custom));

        DefaultPrettyPrinter withNull = pp.withObjectIndenter(null);
        assertNotSame(pp, withNull);
        assertSame(DefaultPrettyPrinter.NopIndenter.instance, withNull._objectIndenter);
        assertSame(withNull, withNull.withObjectIndenter(null));
    }

    @Test
    public void testWithSpacesInObjectEntries() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        assertTrue(pp._spacesInObjectEntries);
        assertSame(pp, pp.withSpacesInObjectEntries());

        DefaultPrettyPrinter without = pp.withoutSpacesInObjectEntries();
        assertNotSame(pp, without);
        assertFalse(without._spacesInObjectEntries);
        assertSame(without, without.withoutSpacesInObjectEntries());

        DefaultPrettyPrinter backWith = without.withSpacesInObjectEntries();
        assertNotSame(without, backWith);
        assertTrue(backWith._spacesInObjectEntries);
    }

    @Test
    public void testWithSeparators() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        Separators seps = Separators.createDefaultInstance().withObjectFieldValueSeparator('=');
        DefaultPrettyPrinter result = pp.withSeparators(seps);
        assertSame(pp, result);
        assertSame(seps, pp._separators);
        assertEquals(" = ", pp._objectFieldValueSeparatorWithSpaces);
    }

    @Test
    public void testCreateInstance() {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        DefaultPrettyPrinter copy = pp.createInstance();
        assertNotNull(copy);
        assertNotSame(pp, copy);
        assertEquals(pp._rootSeparator, copy._rootSeparator);
        assertSame(pp._arrayIndenter, copy._arrayIndenter);
        assertSame(pp._objectIndenter, copy._objectIndenter);
        assertEquals(pp._spacesInObjectEntries, copy._spacesInObjectEntries);
    }

    @Test
    public void testNopIndenter() throws IOException {
        DefaultPrettyPrinter.NopIndenter indenter = DefaultPrettyPrinter.NopIndenter.instance;
        assertTrue(indenter.isInline());
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        indenter.writeIndentation(g, 5);
        g.flush();
        assertEquals("", sw.toString());
    }

    @Test
    public void testFixedSpaceIndenter() throws IOException {
        DefaultPrettyPrinter.FixedSpaceIndenter indenter = DefaultPrettyPrinter.FixedSpaceIndenter.instance;
        assertTrue(indenter.isInline());
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        indenter.writeIndentation(g, 3);
        g.flush();
        assertEquals(" ", sw.toString());
    }

    @Test
    public void testWriteRootValueSeparator() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.writeRootValueSeparator(g);
        g.flush();
        assertEquals(" ", sw.toString());

        sw = new StringWriter();
        g = jsonFactory.createGenerator(sw);
        DefaultPrettyPrinter ppNull = pp.withRootSeparator((String) null);
        ppNull.writeRootValueSeparator(g);
        g.flush();
        assertEquals("", sw.toString());
    }

    @Test
    public void testObjectFormattingLifecycle() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();

        CustomTestIndenter objectIndenter = new CustomTestIndenter(false);
        pp.indentObjectsWith(objectIndenter);

        pp.writeStartObject(g);
        assertEquals(1, pp._nesting);

        pp.beforeObjectEntries(g);
        assertEquals(1, objectIndenter.lastLevel);

        pp.writeObjectFieldValueSeparator(g);
        pp.writeObjectEntrySeparator(g);

        pp.writeEndObject(g, 1);
        assertEquals(0, pp._nesting);
        assertEquals(0, objectIndenter.lastLevel);

        g.flush();
        String result = sw.toString();
        assertTrue(result.startsWith("{"));
        assertTrue(result.contains(" : "));
        assertTrue(result.endsWith("}"));

        // Empty object formatting (nrOfEntries = 0)
        sw = new StringWriter();
        g = jsonFactory.createGenerator(sw);
        pp.writeStartObject(g);
        pp.writeEndObject(g, 0);
        g.flush();
        assertEquals("{ }", sw.toString());
    }

    @Test
    public void testObjectFormattingWithInlineIndenter() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentObjectsWith(DefaultPrettyPrinter.NopIndenter.instance);

        pp.writeStartObject(g);
        assertEquals(0, pp._nesting);
        pp.writeEndObject(g, 1);
        assertEquals(0, pp._nesting);
        g.flush();
        assertEquals("{}", sw.toString());
    }

    @Test
    public void testObjectFieldValueSeparatorWithoutSpaces() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter().withoutSpacesInObjectEntries();

        pp.writeObjectFieldValueSeparator(g);
        g.flush();
        assertEquals(":", sw.toString());
    }

    @Test
    public void testArrayFormattingLifecycle() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();

        CustomTestIndenter arrayIndenter = new CustomTestIndenter(false);
        pp.indentArraysWith(arrayIndenter);

        pp.writeStartArray(g);
        assertEquals(1, pp._nesting);

        pp.beforeArrayValues(g);
        assertEquals(1, arrayIndenter.lastLevel);

        pp.writeArrayValueSeparator(g);
        assertEquals(2, arrayIndenter.callCount);

        pp.writeEndArray(g, 1);
        assertEquals(0, pp._nesting);
        assertEquals(0, arrayIndenter.lastLevel);

        g.flush();
        String result = sw.toString();
        assertTrue(result.startsWith("["));
        assertTrue(result.contains(","));
        assertTrue(result.endsWith("]"));

        // Empty array formatting (nrOfValues = 0)
        sw = new StringWriter();
        g = jsonFactory.createGenerator(sw);
        pp.writeStartArray(g);
        pp.writeEndArray(g, 0);
        g.flush();
        assertEquals("[ ]", sw.toString());
    }

    @Test
    public void testArrayFormattingWithInlineIndenter() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance);

        pp.writeStartArray(g);
        assertEquals(0, pp._nesting);
        pp.beforeArrayValues(g);
        pp.writeArrayValueSeparator(g);
        pp.writeEndArray(g, 1);
        assertEquals(0, pp._nesting);
        g.flush();
        assertEquals("[   ]", sw.toString());
    }

    @Test
    public void testFullJsonGenerationIntegration() throws IOException {
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter()
                .withRootSeparator("\n")
                .withoutSpacesInObjectEntries();
        g.setPrettyPrinter(pp);

        g.writeStartObject();
        g.writeFieldName("key");
        g.writeString("val");
        g.writeFieldName("arr");
        g.writeStartArray();
        g.writeNumber(1);
        g.writeNumber(2);
        g.writeEndArray();
        g.writeEndObject();
        g.writeStartArray();
        g.writeEndArray();
        g.close();

        String json = sw.toString();
        assertTrue(json.contains("\"key\":"));
        assertTrue(json.contains("\n[ ]"));
    }

    @Test
    public void testSerialization() throws Exception {
        DefaultPrettyPrinter pp = new DefaultPrettyPrinter();
        pp.indentArraysWith(DefaultPrettyPrinter.NopIndenter.instance);
        pp.indentObjectsWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(pp);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DefaultPrettyPrinter deserialized = (DefaultPrettyPrinter) ois.readObject();

        assertNotNull(deserialized);
        assertEquals(pp._rootSeparator.getValue(), deserialized._rootSeparator.getValue());
        assertTrue(deserialized._arrayIndenter.isInline());
        assertTrue(deserialized._objectIndenter.isInline());
        assertEquals(pp._spacesInObjectEntries, deserialized._spacesInObjectEntries);
    }
}
