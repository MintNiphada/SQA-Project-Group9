package com.fasterxml.jackson.core.util;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.FixedSpaceIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.Indenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter.NopIndenter;

@RunWith(MockitoJUnitRunner.class)
public class DefaultPrettyPrinterTest {

    @Mock
    private JsonGenerator g;

    @Mock
    private Indenter mockIndenter;

    private DefaultPrettyPrinter pp;

    @Before
    public void setUp() {
        pp = new DefaultPrettyPrinter();
    }

    @Test
    public void testDefaultConstructor() throws IOException {
        pp.writeRootValueSeparator(g);
        verify(g).writeRaw(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
        pp.writeObjectFieldValueSeparator(g);
        verify(g).writeRaw(" : ");
    }

    @Test
    public void testStringRootSeparatorConstructorNull() throws IOException {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter((String) null);
        p.writeRootValueSeparator(g);
        verify(g, never()).writeRaw(any(SerializableString.class));
    }

    @Test
    public void testStringRootSeparatorConstructorNonNull() throws IOException {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter("|");
        p.writeRootValueSeparator(g);
        verify(g).writeRaw(new SerializedString("|"));
    }

    @Test
    public void testSerializableStringRootSeparatorConstructorNull() throws IOException {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter((SerializableString) null);
        p.writeRootValueSeparator(g);
        verify(g, never()).writeRaw(any(SerializableString.class));
    }

    @Test
    public void testSerializableStringRootSeparatorConstructorNonNull() throws IOException {
        SerializedString sep = new SerializedString("|");
        DefaultPrettyPrinter p = new DefaultPrettyPrinter(sep);
        p.writeRootValueSeparator(g);
        verify(g).writeRaw(sep);
    }

    @Test
    public void testCopyConstructor() throws IOException {
        pp.writeStartObject(g);
        DefaultPrettyPrinter copy = new DefaultPrettyPrinter(pp);
        copy.writeEndObject(g, 1);
        verify(g, times(2)).writeRaw('}');
    }

    @Test
    public void testWithRootSeparatorSameInstance() {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter("|");
        assertSame(p, p.withRootSeparator("|"));
    }

    @Test
    public void testWithRootSeparatorNewInstance() {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter("|");
        DefaultPrettyPrinter p2 = p.withRootSeparator(",");
        assertNotSame(p, p2);
    }

    @Test
    public void testWithRootSeparatorSerializableSame() {
        SerializedString sep = new SerializedString("|");
        DefaultPrettyPrinter p = new DefaultPrettyPrinter(sep);
        assertSame(p, p.withRootSeparator(sep));
    }

    @Test
    public void testWithRootSeparatorSerializableNew() {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter("|");
        DefaultPrettyPrinter p2 = p.withRootSeparator(new SerializedString(","));
        assertNotSame(p, p2);
    }

    @Test
    public void testIndentArraysWithNull() throws IOException {
        pp.indentArraysWith(null);
        pp.writeStartArray(g);
        pp.writeArrayValueSeparator(g);
        verify(g, atLeastOnce()).writeRaw(',');
    }

    @Test
    public void testIndentArraysWithCustom() throws IOException {
        pp.indentArraysWith(mockIndenter);
        when(mockIndenter.isInline()).thenReturn(false);
        pp.writeStartArray(g);
        verify(mockIndenter).isInline();
    }

    @Test
    public void testIndentObjectsWithNull() throws IOException {
        pp.indentObjectsWith(null);
        pp.writeStartObject(g);
        pp.writeObjectEntrySeparator(g);
        verify(g, atLeastOnce()).writeRaw(',');
    }

    @Test
    public void testIndentObjectsWithCustom() throws IOException {
        pp.indentObjectsWith(mockIndenter);
        when(mockIndenter.isInline()).thenReturn(false);
        pp.writeStartObject(g);
        verify(mockIndenter).isInline();
    }

    @Test
    public void testWithArrayIndenterSame() {
        Indenter i = NopIndenter.instance;
        pp.indentArraysWith(i);
        assertSame(pp, pp.withArrayIndenter(i));
    }

    @Test
    public void testWithArrayIndenterNew() {
        Indenter i = mock(Indenter.class);
        DefaultPrettyPrinter p2 = pp.withArrayIndenter(i);
        assertNotSame(pp, p2);
    }

    @Test
    public void testWithArrayIndenterNull() {
        DefaultPrettyPrinter p2 = pp.withArrayIndenter(null);
        assertNotSame(pp, p2);
    }

    @Test
    public void testWithObjectIndenterSame() {
        Indenter i = NopIndenter.instance;
        pp.indentObjectsWith(i);
        assertSame(pp, pp.withObjectIndenter(i));
    }

    @Test
    public void testWithObjectIndenterNew() {
        Indenter i = mock(Indenter.class);
        DefaultPrettyPrinter p2 = pp.withObjectIndenter(i);
        assertNotSame(pp, p2);
    }

    @Test
    public void testWithObjectIndenterNull() {
        DefaultPrettyPrinter p2 = pp.withObjectIndenter(null);
        assertNotSame(pp, p2);
    }

    @Test
    public void testWithSpacesInObjectEntriesAlreadyTrue() {
        assertSame(pp, pp.withSpacesInObjectEntries());
    }

    @Test
    public void testWithSpacesInObjectEntriesFalseToTrue() {
        DefaultPrettyPrinter p = pp.withoutSpacesInObjectEntries();
        assertNotSame(p, p.withSpacesInObjectEntries());
    }

    @Test
    public void testWithoutSpacesInObjectEntriesAlreadyFalse() {
        DefaultPrettyPrinter p = pp.withoutSpacesInObjectEntries();
        assertSame(p, p.withoutSpacesInObjectEntries());
    }

    @Test
    public void testWithoutSpacesInObjectEntriesTrueToFalse() {
        assertNotSame(pp, pp.withoutSpacesInObjectEntries());
    }

    @Test
    public void testWithSeparators() throws IOException {
        Separators separators = new Separators();
        pp.withSeparators(separators);
        pp.writeObjectFieldValueSeparator(g);
        verify(g).writeRaw(" : ");
    }

    @Test
    public void testCreateInstance() {
        DefaultPrettyPrinter copy = pp.createInstance();
        assertNotSame(pp, copy);
    }

    @Test
    public void testWriteRootValueSeparatorNull() throws IOException {
        DefaultPrettyPrinter p = new DefaultPrettyPrinter((SerializableString) null);
        p.writeRootValueSeparator(g);
        verify(g, never()).writeRaw(any(SerializableString.class));
    }

    @Test
    public void testWriteRootValueSeparatorNonNull() throws IOException {
        pp.writeRootValueSeparator(g);
        verify(g).writeRaw(DefaultPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
    }

    @Test
    public void testWriteStartObjectInline() throws IOException {
        pp.indentObjectsWith(NopIndenter.instance);
        pp.writeStartObject(g);
        verify(g).writeRaw('{');
    }

    @Test
    public void testWriteStartObjectNonInline() throws IOException {
        pp.indentObjectsWith(mockIndenter);
        when(mockIndenter.isInline()).thenReturn(false);
        pp.writeStartObject(g);
        verify(mockIndenter).isInline();
    }

    @Test
    public void testBeforeObjectEntries() throws IOException {
        pp.indentObjectsWith(mockIndenter);
        pp.beforeObjectEntries(g);
        verify(mockIndenter).writeIndentation(g, 0);
    }

    @Test
    public void testWriteObjectFieldValueSeparatorWithSpaces() throws IOException {
        pp.writeObjectFieldValueSeparator(g);
        verify(g).writeRaw(" : ");
    }

    @Test
    public void testWriteObjectFieldValueSeparatorWithoutSpaces() throws IOException {
        DefaultPrettyPrinter p = pp.withoutSpacesInObjectEntries();
        p.writeObjectFieldValueSeparator(g);
        verify(g).writeRaw(":");
    }

    @Test
    public void testWriteObjectEntrySeparator() throws IOException {
        pp.indentObjectsWith(mockIndenter);
        pp.writeObjectEntrySeparator(g);
        verify(g).writeRaw(",");
        verify(mockIndenter).writeIndentation(g, 0);
    }

    @Test
    public void testWriteEndObjectNonInlineWithEntries() throws IOException {
        pp.indentObjectsWith(mockIndenter);
        when(mockIndenter.isInline()).thenReturn(false);
        pp.writeStartObject(g);
        pp.writeEndObject(g, 1);
        verify(mockIndenter).writeIndentation(g, 0);
        verify(g).writeRaw('}');
    }

    @Test
    public void testWriteEndObjectNonInlineNoEntries() throws IOException {
        pp.indentObjectsWith(mockIndenter);
        when(mockIndenter.isInline()).thenReturn(false);
        pp.writeStartObject(g);
        pp.writeEndObject(g, 0);
        verify(g).writeRaw(' ');
        verify(g).writeRaw('}');
    }

    @Test
    public void testWriteEndObjectInlineWithEntries() throws IOException {
        pp.indentObjectsWith(NopIndenter.instance);
        pp.writeStartObject(g);
        pp.writeEndObject(g, 1);
        verify(g).writeRaw('}');
    }

    @Test
    public void testWriteEndObjectInlineNoEntries() throws IOException {
        pp.indentObjectsWith(NopIndenter.instance);
        pp.writeStartObject(g);
        pp.writeEndObject(g, 0);
        verify(g).writeRaw(' ');
        verify(g).writeRaw('}');
    }

    @Test
    public void testWriteStartArrayInline() throws IOException {
        pp.indentArraysWith(NopIndenter.instance);
        pp.writeStartArray(g);
        verify(g).writeRaw('[');
    }

    @Test
    public void testWriteStartArrayNonInline() throws IOException {
        pp.indentArraysWith(mockIndenter);
        when(mockIndenter.isInline()).thenReturn(false);
        pp.writeStartArray(g);
        verify(mockIndenter).isInline();
    }

    @Test
    public void testBeforeArrayValues() throws IOException {
        pp.indentArraysWith(mockIndenter);
        pp.beforeArrayValues(g);
        verify(mockIndenter).writeIndentation(g, 0);
    }

    @Test
    public void testWriteArrayValueSeparator() throws IOException {
        pp.indentArraysWith(mockIndenter);
        pp.writeArrayValueSeparator(g);
        verify(g).writeRaw(",");
        verify(mockIndenter).writeIndentation(g, 0);
    }

    @Test
    public void testWriteEndArrayNonInlineWithValues() throws IOException {
        pp.indentArraysWith(mockIndenter);
        when(mockIndenter.isInline()).thenReturn(false);
        pp.writeStartArray(g);
        pp.writeEndArray(g, 1);
        verify(mockIndenter).writeIndentation(g, 0);
        verify(g).writeRaw(']');
    }

    @Test
    public void testWriteEndArrayNonInlineNoValues() throws IOException {
        pp.indentArraysWith(mockIndenter);
        when(mockIndenter.isInline()).thenReturn(false);
        pp.writeStartArray(g);
        pp.writeEndArray(g, 0);
        verify(g).writeRaw(' ');
        verify(g).writeRaw(']');
    }

    @Test
    public void testWriteEndArrayInlineWithValues() throws IOException {
        pp.indentArraysWith(NopIndenter.instance);
        pp.writeStartArray(g);
        pp.writeEndArray(g, 1);
        verify(g).writeRaw(']');
    }

    @Test
    public void testWriteEndArrayInlineNoValues() throws IOException {
        pp.indentArraysWith(NopIndenter.instance);
        pp.writeStartArray(g);
        pp.writeEndArray(g, 0);
        verify(g).writeRaw(' ');
        verify(g).writeRaw(']');
    }

    @Test
    public void testNopIndenterIsInline() {
        assertTrue(NopIndenter.instance.isInline());
    }

    @Test
    public void testNopIndenterWriteIndentation() throws IOException {
        NopIndenter.instance.writeIndentation(g, 5);
        verify(g, never()).writeRaw(any(char.class));
    }

    @Test
    public void testFixedSpaceIndenterIsInline() {
        assertTrue(FixedSpaceIndenter.instance.isInline());
    }

    @Test
    public void testFixedSpaceIndenterWriteIndentation() throws IOException {
        FixedSpaceIndenter.instance.writeIndentation(g, 5);
        verify(g).writeRaw(' ');
    }
}
