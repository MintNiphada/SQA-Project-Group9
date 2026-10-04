package org.apache.commons.lang3.text.translate;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class CharSequenceTranslatorTest {

    private static class DummyTranslator extends CharSequenceTranslator {
        private final char target;
        private final String replacement;
        private final int consumedCount;

        public DummyTranslator(char target, String replacement, int consumedCount) {
            this.target = target;
            this.replacement = replacement;
            this.consumedCount = consumedCount;
        }

        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            if (input.charAt(index) == target) {
                out.write(replacement);
                return consumedCount;
            }
            return 0;
        }
    }

    private static class ExceptionTranslator extends CharSequenceTranslator {
        @Override
        public int translate(CharSequence input, int index, Writer out) throws IOException {
            throw new IOException("Simulated IO exception");
        }
    }

    @Test
    public void testTranslateNullCharSequence() {
        CharSequenceTranslator translator = new DummyTranslator('a', "A", 1);
        Assert.assertNull(translator.translate(null));
    }

    @Test
    public void testTranslateNullWriter() throws IOException {
        CharSequenceTranslator translator = new DummyTranslator('a', "A", 1);
        try {
            translator.translate("test", (Writer) null);
            Assert.fail("Expected IllegalArgumentException when Writer is null");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("The Writer must not be null", e.getMessage());
        }
    }

    @Test
    public void testTranslateNullInputWithWriter() throws IOException {
        CharSequenceTranslator translator = new DummyTranslator('a', "A", 1);
        StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        Assert.assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateEmptyString() {
        CharSequenceTranslator translator = new DummyTranslator('a', "A", 1);
        Assert.assertEquals("", translator.translate(""));
    }

    @Test
    public void testTranslateUnchanged() {
        CharSequenceTranslator translator = new DummyTranslator('z', "Z", 1);
        Assert.assertEquals("hello world", translator.translate("hello world"));
    }

    @Test
    public void testTranslateSingleMatch() {
        CharSequenceTranslator translator = new DummyTranslator('a', "X", 1);
        Assert.assertEquals("Xbc", translator.translate("abc"));
    }

    @Test
    public void testTranslateMultipleMatches() {
        CharSequenceTranslator translator = new DummyTranslator('a', "XYZ", 1);
        Assert.assertEquals("XYZbXYZcXYZ", translator.translate("abaca"));
    }

    @Test
    public void testTranslateConsumedMultipleChars() {
        // Translate "abc" starting at 'a' consuming 3 characters
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index == 0 && input.subSequence(index, Math.min(index + 3, input.length())).toString().equals("abc")) {
                    out.write("123");
                    return 3;
                }
                return 0;
            }
        };

        Assert.assertEquals("123def", translator.translate("abcdef"));
    }

    @Test
    public void testTranslateConsumedNearEnd() {
        // Tests pos >= len - 2 branch inside the consumed loop
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index == 2) {
                    out.write("END");
                    return 2;
                }
                return 0;
            }
        };

        Assert.assertEquals("abEND", translator.translate("abcd"));
    }

    @Test
    public void testTranslateSupplementaryCharacters() {
        // Code point 0x1F600 (Grinning Face emoji, surrogate pair: \uD83D\uDE00)
        String emoji = "\uD83D\uDE00";
        String input = "Hi " + emoji + " Bye";

        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence in, int index, Writer out) throws IOException {
                int codePoint = Character.codePointAt(in, index);
                if (codePoint == 0x1F600) {
                    out.write(":smile:");
                    return 1;
                }
                return 0;
            }
        };

        Assert.assertEquals("Hi :smile: Bye", translator.translate(input));
    }

    @Test
    public void testTranslateSupplementaryCharactersUntranslated() {
        String emoji = "\uD83D\uDE00";
        String input = "A" + emoji + "B";

        CharSequenceTranslator translator = new DummyTranslator('z', "Z", 1);
        Assert.assertEquals(input, translator.translate(input));
    }

    @Test
    public void testTranslateThrowsRuntimeExceptionOnIOException() {
        CharSequenceTranslator translator = new ExceptionTranslator();
        try {
            translator.translate("trigger");
            Assert.fail("Expected RuntimeException when IOException occurs");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof IOException);
            Assert.assertEquals("Simulated IO exception", e.getCause().getMessage());
        }
    }

    @Test
    public void testTranslateDirectWriterIOException() {
        CharSequenceTranslator translator = new ExceptionTranslator();
        StringWriter writer = new StringWriter();
        try {
            translator.translate("trigger", writer);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertEquals("Simulated IO exception", e.getMessage());
        }
    }

    @Test
    public void testWithMergesTranslators() {
        CharSequenceTranslator t1 = new DummyTranslator('a', "1", 1);
        CharSequenceTranslator t2 = new DummyTranslator('b', "2", 1);
        CharSequenceTranslator t3 = new DummyTranslator('c', "3", 1);

        CharSequenceTranslator combined = t1.with(t2, t3);
        Assert.assertNotNull(combined);
        Assert.assertEquals("123d", combined.translate("abcd"));
    }

    @Test
    public void testWithEmptyTranslators() {
        CharSequenceTranslator t1 = new DummyTranslator('a', "1", 1);
        CharSequenceTranslator combined = t1.with();
        Assert.assertNotNull(combined);
        Assert.assertEquals("1bc", combined.translate("abc"));
    }

    @Test
    public void testHex() {
        Assert.assertEquals("0", CharSequenceTranslator.hex(0));
        Assert.assertEquals("A", CharSequenceTranslator.hex(10));
        Assert.assertEquals("20", CharSequenceTranslator.hex(' '));
        Assert.assertEquals("61", CharSequenceTranslator.hex('a'));
        Assert.assertEquals("41", CharSequenceTranslator.hex('A'));
        Assert.assertEquals("FFFF", CharSequenceTranslator.hex(0xFFFF));
        Assert.assertEquals("1F600", CharSequenceTranslator.hex(0x1F600));
    }
}
