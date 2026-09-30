package org.apache.commons.compress.archivers.tar;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

public class TarArchiveInputStreamTest {

    private byte[] createTarArchive(byte[][] headers, byte[][] contents) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < headers.length; i++) {
            bos.write(headers[i]);
            if (contents != null && i < contents.length && contents[i] != null) {
                bos.write(contents[i]);
                int remainder = contents[i].length % TarBuffer.DEFAULT_RCDSIZE;
                if (remainder != 0) {
                    bos.write(new byte[TarBuffer.DEFAULT_RCDSIZE - remainder]);
                }
            }
        }
        bos.write(new byte[TarBuffer.DEFAULT_RCDSIZE]);
        bos.write(new byte[TarBuffer.DEFAULT_RCDSIZE]);
        return bos.toByteArray();
    }

    private byte[] createHeader(String name, long size, byte linkFlag) {
        TarArchiveEntry entry = new TarArchiveEntry(name, linkFlag);
        entry.setSize(size);
        entry.setModTime(1000L);
        entry.setMode(0644);
        entry.setUserId(100);
        entry.setGroupId(100);
        entry.setUserName("user");
        entry.setGroupName("group");
        byte[] buf = new byte[TarBuffer.DEFAULT_RCDSIZE];
        entry.writeEntryHeader(buf);
        return buf;
    }

    @Test
    public void testConstructorsAndGetRecordSize() throws IOException {
        ByteArrayInputStream bais1 = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais1 = new TarArchiveInputStream(bais1);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tais1.getRecordSize());
        tais1.close();

        ByteArrayInputStream bais2 = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais2 = new TarArchiveInputStream(bais2, 1024);
        Assert.assertEquals(TarBuffer.DEFAULT_RCDSIZE, tais2.getRecordSize());
        tais2.close();

        ByteArrayInputStream bais3 = new ByteArrayInputStream(new byte[0]);
        TarArchiveInputStream tais3 = new TarArchiveInputStream(bais3, 1024, 512);
        Assert.assertEquals(512, tais3.getRecordSize());
        tais3.close();
    }

    @Test
    public void testReadBasicArchive() throws IOException {
        byte[] content1 = "Hello World Tar Entry 1".getBytes("UTF-8");
        byte[] content2 = "Second File Content with slightly more bytes than first".getBytes("UTF-8");
        byte[] h1 = createHeader("file1.txt", content1.length, TarConstants.LF_NORMAL);
        byte[] h2 = createHeader("dir/file2.txt", content2.length, TarConstants.LF_NORMAL);

        byte[] tarData = createTarArchive(new byte[][]{h1, h2}, new byte[][]{content1, content2});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));

        TarArchiveEntry e1 = tais.getNextTarEntry();
        Assert.assertNotNull(e1);
        Assert.assertEquals("file1.txt", e1.getName());
        Assert.assertEquals(content1.length, tais.available());

        byte[] read1 = new byte[content1.length];
        int bytesRead = tais.read(read1, 0, read1.length);
        Assert.assertEquals(content1.length, bytesRead);
        Assert.assertArrayEquals(content1, read1);
        Assert.assertEquals(-1, tais.read(read1, 0, 1));
        Assert.assertEquals(0, tais.available());

        ArchiveEntry e2 = tais.getNextEntry();
        Assert.assertNotNull(e2);
        Assert.assertEquals("dir/file2.txt", e2.getName());

        byte[] read2 = new byte[content2.length];
        int offset = 0;
        int chunk;
        while ((chunk = tais.read(read2, offset, 10)) != -1) {
            offset += chunk;
        }
        Assert.assertEquals(content2.length, offset);
        Assert.assertArrayEquals(content2, read2);

        Assert.assertNull(tais.getNextTarEntry());
        Assert.assertNull(tais.getNextEntry());
        tais.close();
    }

    @Test
    public void testSkipAutoDrainOnNextEntry() throws IOException {
        byte[] content1 = new byte[1500];
        Arrays.fill(content1, (byte) 'A');
        byte[] content2 = "Final file".getBytes("UTF-8");

        byte[] h1 = createHeader("large.dat", content1.length, TarConstants.LF_NORMAL);
        byte[] h2 = createHeader("final.txt", content2.length, TarConstants.LF_NORMAL);

        byte[] tarData = createTarArchive(new byte[][]{h1, h2}, new byte[][]{content1, content2});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));

        TarArchiveEntry e1 = tais.getNextTarEntry();
        Assert.assertNotNull(e1);
        Assert.assertEquals("large.dat", e1.getName());

        byte[] smallRead = new byte[100];
        Assert.assertEquals(100, tais.read(smallRead, 0, 100));

        TarArchiveEntry e2 = tais.getNextTarEntry();
        Assert.assertNotNull(e2);
        Assert.assertEquals("final.txt", e2.getName());

        byte[] read2 = new byte[content2.length];
        Assert.assertEquals(content2.length, tais.read(read2, 0, content2.length));
        Assert.assertArrayEquals(content2, read2);
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testExplicitSkip() throws IOException {
        byte[] content = new byte[1000];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i % 128);
        }
        byte[] h1 = createHeader("skip.dat", content.length, TarConstants.LF_NORMAL);
        byte[] tarData = createTarArchive(new byte[][]{h1}, new byte[][]{content});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        Assert.assertNotNull(tais.getNextTarEntry());

        long skipped = tais.skip(400);
        Assert.assertEquals(400, skipped);
        Assert.assertEquals(600, tais.available());

        byte[] buf = new byte[100];
        tais.read(buf, 0, 100);
        Assert.assertEquals(content[400], buf[0]);

        long skippedMore = tais.skip(1000);
        Assert.assertEquals(500, skippedMore);
        Assert.assertEquals(0, tais.available());

        long skippedPast = tais.skip(10);
        Assert.assertEquals(0, skippedPast);
        tais.close();
    }

    @Test
    public void testReadBufSplit() throws IOException {
        byte[] content = new byte[1000];
        Arrays.fill(content, (byte) 'Z');
        byte[] h1 = createHeader("split.dat", content.length, TarConstants.LF_NORMAL);
        byte[] tarData = createTarArchive(new byte[][]{h1}, new byte[][]{content});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tais.getNextTarEntry();

        byte[] b1 = new byte[100];
        tais.read(b1, 0, 100);

        byte[] b2 = new byte[200];
        tais.read(b2, 0, 200);

        byte[] b3 = new byte[700];
        tais.read(b3, 0, 700);

        Assert.assertEquals(-1, tais.read(b1, 0, 10));
        tais.close();
    }

    @Test
    public void testGNULongNameEntry() throws IOException {
        String longFileName = "very/long/path/name/that/exceeds/the/standard/tar/header/name/limit/and/requires/gnu/long/name/entry/created/specifically/for/testing/purpose/file.txt";
        byte[] longNameBytes = (longFileName + "\0").getBytes("UTF-8");
        byte[] gnuHeader = createHeader("././@LongLink", longNameBytes.length, TarConstants.LF_GNUTYPE_LONGNAME);

        byte[] realContent = "Hello GNU Long Name".getBytes("UTF-8");
        byte[] normalHeader = createHeader("truncated_name", realContent.length, TarConstants.LF_NORMAL);

        byte[] tarData = createTarArchive(
                new byte[][]{gnuHeader, normalHeader},
                new byte[][]{longNameBytes, realContent}
        );

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertEquals(longFileName, entry.getName());

        byte[] readContent = new byte[realContent.length];
        tais.read(readContent, 0, readContent.length);
        Assert.assertArrayEquals(realContent, readContent);
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testGNULongNameTruncatedArchive() throws IOException {
        String longFileName = "long_file_name_without_next_entry";
        byte[] longNameBytes = (longFileName + "\0").getBytes("UTF-8");
        byte[] gnuHeader = createHeader("././@LongLink", longNameBytes.length, TarConstants.LF_GNUTYPE_LONGNAME);

        byte[] tarData = createTarArchive(new byte[][]{gnuHeader}, new byte[][]{longNameBytes});
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test
    public void testPaxHeaders() throws IOException {
        String paxContent = "30 path=new_pax_filename.txt\n"
                + "24 linkpath=target_link\n"
                + "13 gid=1234\n"
                + "18 gname=customgrp\n"
                + "13 uid=5678\n"
                + "18 uname=customusr\n"
                + "14 size=10\n";
        byte[] paxBytes = paxContent.getBytes("UTF-8");
        byte[] paxHeader = createHeader("PaxHeader/old_name.txt", paxBytes.length, TarConstants.LF_PAX_EXTENDED_HEADER_LC);

        byte[] content = "0123456789".getBytes("UTF-8");
        byte[] normalHeader = createHeader("old_name.txt", 100L, TarConstants.LF_NORMAL);

        byte[] tarData = createTarArchive(
                new byte[][]{paxHeader, normalHeader},
                new byte[][]{paxBytes, content}
        );

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tais.getNextTarEntry();

        Assert.assertNotNull(entry);
        Assert.assertEquals("new_pax_filename.txt", entry.getName());
        Assert.assertEquals("target_link", entry.getLinkName());
        Assert.assertEquals(1234, entry.getGroupId());
        Assert.assertEquals("customgrp", entry.getGroupName());
        Assert.assertEquals(5678, entry.getUserId());
        Assert.assertEquals("customusr", entry.getUserName());
        Assert.assertEquals(10L, entry.getSize());

        byte[] readBytes = new byte[10];
        int r = tais.read(readBytes, 0, 10);
        Assert.assertEquals(10, r);
        Assert.assertArrayEquals(content, readBytes);
        Assert.assertNull(tais.getNextTarEntry());
        tais.close();
    }

    @Test(expected = IOException.class)
    public void testParsePaxHeadersCorrupt() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        String corruptPax = "30 path=corrupt_because_shorter_than_len\n";
        StringReader reader = new StringReader(corruptPax.substring(0, 15));
        tais.parsePaxHeaders(reader);
    }

    @Test
    public void testParsePaxHeadersEmpty() throws IOException {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        StringReader reader = new StringReader("");
        Map<String, String> map = tais.parsePaxHeaders(reader);
        Assert.assertTrue(map.isEmpty());
        tais.close();
    }

    @Test
    public void testGNUSparseArchiveEntry() throws IOException {
        byte[] headerBuf = new byte[TarBuffer.DEFAULT_RCDSIZE];
        TarArchiveEntry sparseEntry = new TarArchiveEntry("sparse.bin", TarConstants.LF_GNUTYPE_SPARSE);
        sparseEntry.setSize(1024L);
        sparseEntry.writeEntryHeader(headerBuf);
        headerBuf[TarConstants.LF_GNUTYPE_SPARSE] = 'S';
        headerBuf[482] = 1; // isExtended = 1

        byte[] extHeaderBuf = new byte[TarBuffer.DEFAULT_RCDSIZE];
        extHeaderBuf[504] = 0; // isExtended = 0

        byte[] content = new byte[512];
        byte[] tarData = createTarArchive(new byte[][]{headerBuf, extHeaderBuf}, new byte[][]{null, content});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        TarArchiveEntry entry = tais.getNextTarEntry();
        Assert.assertNotNull(entry);
        Assert.assertFalse(tais.canReadEntryData(entry));
        tais.close();
    }

    @Test
    public void testMatches() {
        byte[] invalidShort = new byte[10];
        Assert.assertFalse(TarArchiveInputStream.matches(invalidShort, invalidShort.length));

        byte[] posixSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_POSIX.getBytes(), 0, posixSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_POSIX.getBytes(), 0, posixSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(posixSig, posixSig.length));

        byte[] gnuSpaceSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, gnuSpaceSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_SPACE.getBytes(), 0, gnuSpaceSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(gnuSpaceSig, gnuSpaceSig.length));

        byte[] gnuZeroSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_GNU.getBytes(), 0, gnuZeroSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_GNU_ZERO.getBytes(), 0, gnuZeroSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(gnuZeroSig, gnuZeroSig.length));

        byte[] antSig = new byte[512];
        System.arraycopy(TarConstants.MAGIC_ANT.getBytes(), 0, antSig, TarConstants.MAGIC_OFFSET, TarConstants.MAGICLEN);
        System.arraycopy(TarConstants.VERSION_ANT.getBytes(), 0, antSig, TarConstants.VERSION_OFFSET, TarConstants.VERSIONLEN);
        Assert.assertTrue(TarArchiveInputStream.matches(antSig, antSig.length));

        byte[] corruptedSig = new byte[512];
        System.arraycopy("unknown".getBytes(), 0, corruptedSig, TarConstants.MAGIC_OFFSET, 7);
        Assert.assertFalse(TarArchiveInputStream.matches(corruptedSig, corruptedSig.length));
    }

    @Test
    public void testCanReadEntryData() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        TarArchiveEntry regularEntry = new TarArchiveEntry("file.txt");
        Assert.assertTrue(tais.canReadEntryData(regularEntry));

        TarArchiveEntry sparseEntry = new TarArchiveEntry("file.sparse", TarConstants.LF_GNUTYPE_SPARSE);
        Assert.assertFalse(tais.canReadEntryData(sparseEntry));

        ArchiveEntry nonTarEntry = new ArchiveEntry() {
            @Override
            public String getName() { return "other"; }
            @Override
            public long getSize() { return 0; }
            @Override
            public boolean isDirectory() { return false; }
            @Override
            public java.util.Date getLastModifiedDate() { return null; }
        };
        Assert.assertFalse(tais.canReadEntryData(nonTarEntry));
    }

    @Test
    public void testAvailableOverflowHandling() throws Exception {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        Field entrySizeField = TarArchiveInputStream.class.getDeclaredField("entrySize");
        entrySizeField.setAccessible(true);
        entrySizeField.set(tais, (long) Integer.MAX_VALUE + 100L);

        Field entryOffsetField = TarArchiveInputStream.class.getDeclaredField("entryOffset");
        entryOffsetField.setAccessible(true);
        entryOffsetField.set(tais, 0L);

        Assert.assertEquals(Integer.MAX_VALUE, tais.available());
    }

    @Test(expected = IOException.class)
    public void testUnexpectedEOFDuringRead() throws IOException {
        byte[] content = new byte[100];
        byte[] h = createHeader("partial.txt", 1000L, TarConstants.LF_NORMAL);
        byte[] tarData = new byte[512 + 100];
        System.arraycopy(h, 0, tarData, 0, 512);
        System.arraycopy(content, 0, tarData, 512, 100);

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData));
        tais.getNextTarEntry();
        byte[] buf = new byte[1000];
        tais.read(buf, 0, 1000);
    }

    @Test
    public void testResetAndGettersSetters() {
        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(new byte[0]));
        tais.reset();

        Assert.assertNull(tais.getCurrentEntry());
        TarArchiveEntry entry = new TarArchiveEntry("test.txt");
        tais.setCurrentEntry(entry);
        Assert.assertSame(entry, tais.getCurrentEntry());

        Assert.assertFalse(tais.isAtEOF());
        tais.setAtEOF(true);
        Assert.assertTrue(tais.isAtEOF());
    }

    @Test(expected = RuntimeException.class)
    public void testFailedSkipThrowsRuntimeException() throws Exception {
        byte[] h1 = createHeader("f1.txt", 100L, TarConstants.LF_NORMAL);
        byte[] h2 = createHeader("f2.txt", 10L, TarConstants.LF_NORMAL);
        byte[] tarData = createTarArchive(new byte[][]{h1, h2}, new byte[][]{new byte[100], new byte[10]});

        TarArchiveInputStream tais = new TarArchiveInputStream(new ByteArrayInputStream(tarData)) {
            @Override
            public long skip(long numToSkip) throws IOException {
                return 0;
            }
        };

        tais.getNextTarEntry();
        tais.getNextTarEntry();
    }
}
