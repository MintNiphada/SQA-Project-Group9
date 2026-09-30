package org.apache.commons.lang3.text.translate;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class CharSequenceTranslatorTest {

    @Test
    public void testTranslateNullCharSequence() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        Assert.assertNull(translator.translate((CharSequence) null));
    }

    @Test
    public void testTranslateNullWriter() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        try {
            translator.translate("test", null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("The Writer must not be null", e.getMessage());
        }
    }

    @Test
    public void testTranslateNullInputWithWriter() throws IOException {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                out.write("invoked");
                return 1;
            }
        };

        StringWriter writer = new StringWriter();
        translator.translate(null, writer);
        Assert.assertEquals("", writer.toString());
    }

    @Test
    public void testTranslateEmptyString() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        Assert.assertEquals("", translator.translate(""));
    }

    @Test
    public void testTranslateConsumedZero() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        String input = "Hello World!";
        String result = translator.translate(input);
        Assert.assertEquals(input, result);
    }

    @Test
    public void testTranslateConsumedZeroWithSurrogatePairs() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                return 0;
            }
        };

        // Unicode supplementary character (e.g. U+1F600 😀: represented as surrogate pair in UTF-16)
        String input = "A" + new String(Character.toChars(0x1F600)) + "B";
        String result = translator.translate(input);
        Assert.assertEquals(input, result);
    }

    @Test
    public void testTranslateSingleCodepointConsumed() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                char c = input.charAt(index);
                if (c == 'a') {
                    out.write("X");
                    return 1;
                }
                return 0;
            }
        };

        Assert.assertEquals("XbcXdX", translator.translate("abcada"));
    }

    @Test
    public void testTranslateMultipleCodepointsConsumed() {
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (index <= input.length() - 3 && "abc".equals(input.subSequence(index, index + 3).toString())) {
                    out.write("XYZ");
                    return 3;
                }
                return 0;
            }
        };

        Assert.assertEquals("123XYZ456XYZ", translator.translate("123abc456abc"));
    }

    @Test
    public void testTranslateSurrogatePairConsumed() {
        final int codePoint = 0x1F600;
        final String emoji = new String(Character.toChars(codePoint));

        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                int cp = Character.codePointAt(input, index);
                if (cp == codePoint) {
                    out.write("[EMOJI]");
                    // Consumed 1 codepoint
                    return 1;
                }
                return 0;
            }
        };

        String input = "Prefix " + emoji + " Suffix";
        String result = translator.translate(input);
        Assert.assertEquals("Prefix [EMOJI] Suffix", result);
    }

    @Test
    public void testTranslateIOExceptionPropagated() {
        final IOException expectedException = new IOException("Custom IO failure");
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                throw expectedException;
            }
        };

        Writer failingWriter = new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
            }
        };

        try {
            translator.translate("test", failingWriter);
            Assert.fail("Expected IOException");
        } catch (IOException e) {
            Assert.assertSame(expectedException, e);
        }
    }

    @Test
    public void testTranslateStringWriterIOExceptionWrappedInRuntimeException() {
        final IOException expectedException = new IOException("Translator IO failure");
        CharSequenceTranslator translator = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                throw expectedException;
            }
        };

        try {
            translator.translate("test");
            Assert.fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            Assert.assertSame(expectedException, e.getCause());
        }
    }

    @Test
    public void testWithTranslators() {
        CharSequenceTranslator t1 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'a') {
                    out.write('1');
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator t2 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'b') {
                    out.write('2');
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator t3 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'c') {
                    out.write('3');
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator combined = t1.with(t2, t3);
        Assert.assertNotNull(combined);
        Assert.assertEquals("123d", combined.translate("abcd"));
    }

    @Test
    public void testWithZeroTranslators() {
        CharSequenceTranslator t1 = new CharSequenceTranslator() {
            @Override
            public int translate(CharSequence input, int index, Writer out) throws IOException {
                if (input.charAt(index) == 'a') {
                    out.write('1');
                    return 1;
                }
                return 0;
            }
        };

        CharSequenceTranslator combined = t1.with();
        Assert.assertNotNull(combined);
        Assert.assertEquals("1bc", combined.translate("abc"));
    }

    @Test
    public void testHex() {
        Assert.assertEquals("0", CharSequenceTranslator.hex(0));
        Assert.assertEquals("A", CharSequenceTranslator.hex(10));
        Assert.assertEquals("F", CharSequenceTranslator.hex(15));
        Assert.assertEquals("10", CharSequenceTranslator.hex(16));
        Assert.assertEquals("20", CharSequenceTranslator.hex(32));
        Assert.assertEquals("FF", CharSequenceTranslator.hex(255));
        Assert.assertEquals("ABCD", CharSequenceTranslator.hex(0xABCD));
        Assert.assertEquals("FFFF", CharSequenceTranslator.hex(65535));
        Assert.assertEquals("1F600", CharSequenceTranslator.hex(0x1F600));
        Assert.assertEquals("FFFFFFFF", CharSequenceTranslator.hex(-1));
    }
}
