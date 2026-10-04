package org.apache.commons.compress.archivers.sevenz;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.Date;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SevenZOutputFileTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File output7zFile;

    @Before
    public void setUp() throws Exception {
        output7zFile = tempFolder.newFile("test_archive.7z");
    }

    @After
    public void tearDown() {
        if (output7zFile != null && output7zFile.exists()) {
            output7zFile.delete();
        }
    }

    @Test
    public void testEmptyArchiveCreation() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output7zFile);
        out.close();

        Assert.assertTrue(output7zFile.exists());
        Assert.assertTrue(output7zFile.length() > SevenZFile.SIGNATURE_HEADER_SIZE);

        SevenZFile in = new SevenZFile(output7zFile);
        SevenZArchiveEntry entry = in.getNextEntry();
        Assert.assertNull(entry);
        in.close();
    }

    @Test(expected = IOException.class)
    public void testDoubleFinishThrowsException() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output7zFile);
        try {
            out.finish();
            out.finish();
        } finally {
            out.close();
        }
    }

    @Test
    public void testCreateArchiveEntryFromFileAndDirectory() throws IOException {
        File dummyFile = tempFolder.newFile("dummy.txt");
        File dummyDir = tempFolder.newFolder("dummyDir");

        SevenZOutputFile out = new SevenZOutputFile(output7zFile);
        try {
            SevenZArchiveEntry entryFile = out.createArchiveEntry(dummyFile, "fileInArchive.txt");
            Assert.assertFalse(entryFile.isDirectory());
            Assert.assertEquals("fileInArchive.txt", entryFile.getName());
            Assert.assertEquals(dummyFile.lastModified(), entryFile.getLastModifiedDate().getTime());

            SevenZArchiveEntry entryDir = out.createArchiveEntry(dummyDir, "dirInArchive");
            Assert.assertTrue(entryDir.isDirectory());
            Assert.assertEquals("dirInArchive", entryDir.getName());
            Assert.assertEquals(dummyDir.lastModified(), entryDir.getLastModifiedDate().getTime());
        } finally {
            out.close();
        }
    }

    @Test
    public void testWriteSingleEntrySingleByteAndByteArray() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output7zFile);
        out.setContentCompression(SevenZMethod.COPY);

        SevenZArchiveEntry entry = new SevenZArchiveEntry();
        entry.setName("entry1.bin");
        out.putArchiveEntry(entry);

        out.write('A');
        out.write(new byte[] { 'B', 'C', 'D' });
        out.write(new byte[] { 'X', 'E', 'F', 'Y' }, 1, 2);
        out.write(new byte[] { 'Z' }, 0, 0); // test len = 0 branch

        out.closeArchiveEntry();
        out.close();

        SevenZFile in = new SevenZFile(output7zFile);
        SevenZArchiveEntry readEntry = in.getNextEntry();
        Assert.assertNotNull(readEntry);
        Assert.assertEquals("entry1.bin", readEntry.getName());
        Assert.assertEquals(6, readEntry.getSize());

        byte[] content = new byte[6];
        int read = in.read(content, 0, 6);
        Assert.assertEquals(6, read);
        Assert.assertEquals("ABCDEF", new String(content, "UTF-8"));
        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testDifferentCompressionMethods() throws IOException {
        SevenZMethod[] methods = new SevenZMethod[] {
            SevenZMethod.COPY,
            SevenZMethod.LZMA2,
            SevenZMethod.DEFLATE,
            SevenZMethod.BZIP2
        };

        for (int i = 0; i < methods.length; i++) {
            File currentArchive = tempFolder.newFile("archive_" + i + ".7z");
            SevenZOutputFile out = new SevenZOutputFile(currentArchive);
            out.setContentCompression(methods[i]);

            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("data_" + i + ".txt");
            out.putArchiveEntry(entry);

            byte[] data = ("Compressible text repeating content " + i + " ").getBytes("UTF-8");
            for (int j = 0; j < 50; j++) {
                out.write(data);
            }

            out.closeArchiveEntry();
            out.close();

            SevenZFile in = new SevenZFile(currentArchive);
            SevenZArchiveEntry readEntry = in.getNextEntry();
            Assert.assertNotNull(readEntry);
            Assert.assertEquals("data_" + i + ".txt", readEntry.getName());
            Assert.assertEquals(data.length * 50, readEntry.getSize());

            byte[] buf = new byte[data.length * 50];
            int totalRead = 0;
            int r;
            while ((r = in.read(buf, totalRead, buf.length - totalRead)) > 0) {
                totalRead += r;
            }
            Assert.assertEquals(data.length * 50, totalRead);
            Assert.assertNull(in.getNextEntry());
            in.close();
        }
    }

    @Test
    public void testEmptyEntriesAndDirectories() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output7zFile);

        SevenZArchiveEntry emptyFile = new SevenZArchiveEntry();
        emptyFile.setName("empty.txt");
        emptyFile.setDirectory(false);
        out.putArchiveEntry(emptyFile);
        out.closeArchiveEntry();

        SevenZArchiveEntry emptyDir = new SevenZArchiveEntry();
        emptyDir.setName("emptyDir/");
        emptyDir.setDirectory(true);
        out.putArchiveEntry(emptyDir);
        out.closeArchiveEntry();

        SevenZArchiveEntry antiItem = new SevenZArchiveEntry();
        antiItem.setName("antiItem.txt");
        antiItem.setDirectory(false);
        antiItem.setAntiItem(true);
        out.putArchiveEntry(antiItem);
        out.closeArchiveEntry();

        SevenZArchiveEntry regularFile = new SevenZArchiveEntry();
        regularFile.setName("regular.txt");
        out.putArchiveEntry(regularFile);
        out.write("Hello".getBytes("UTF-8"));
        out.closeArchiveEntry();

        out.close();

        SevenZFile in = new SevenZFile(output7zFile);
        SevenZArchiveEntry e1 = in.getNextEntry();
        Assert.assertNotNull(e1);
        Assert.assertEquals("empty.txt", e1.getName());
        Assert.assertFalse(e1.isDirectory());
        Assert.assertEquals(0, e1.getSize());

        SevenZArchiveEntry e2 = in.getNextEntry();
        Assert.assertNotNull(e2);
        Assert.assertEquals("emptyDir/", e2.getName());
        Assert.assertTrue(e2.isDirectory());

        SevenZArchiveEntry e3 = in.getNextEntry();
        Assert.assertNotNull(e3);
        Assert.assertEquals("antiItem.txt", e3.getName());
        Assert.assertTrue(e3.isAntiItem());

        SevenZArchiveEntry e4 = in.getNextEntry();
        Assert.assertNotNull(e4);
        Assert.assertEquals("regular.txt", e4.getName());
        Assert.assertEquals(5, e4.getSize());

        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testAllOrPartialFileDatesAndAttributes() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output7zFile);

        Date now = new Date(1600000000000L);
        Date access = new Date(1600000050000L);
        Date create = new Date(1600000010000L);

        // Entry 1 has all dates & attributes
        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("fullMetadata.txt");
        entry1.setLastModifiedDate(now);
        entry1.setAccessDate(access);
        entry1.setCreationDate(create);
        entry1.setWindowsAttributes(0x20); // Archive
        out.putArchiveEntry(entry1);
        out.write("data1".getBytes("UTF-8"));
        out.closeArchiveEntry();

        // Entry 2 has partial dates & attributes to trigger bitset paths
        SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("partialMetadata.txt");
        entry2.setHasLastModifiedDate(false);
        entry2.setHasAccessDate(false);
        entry2.setHasCreationDate(false);
        entry2.setHasWindowsAttributes(false);
        out.putArchiveEntry(entry2);
        out.write("data2".getBytes("UTF-8"));
        out.closeArchiveEntry();

        out.close();

        SevenZFile in = new SevenZFile(output7zFile);
        SevenZArchiveEntry r1 = in.getNextEntry();
        Assert.assertNotNull(r1);
        Assert.assertEquals("fullMetadata.txt", r1.getName());
        Assert.assertTrue(r1.getHasLastModifiedDate());
        Assert.assertEquals(now.getTime() / 1000, r1.getLastModifiedDate().getTime() / 1000);
        Assert.assertTrue(r1.getHasAccessDate());
        Assert.assertEquals(access.getTime() / 1000, r1.getAccessDate().getTime() / 1000);
        Assert.assertTrue(r1.getHasCreationDate());
        Assert.assertEquals(create.getTime() / 1000, r1.getCreationDate().getTime() / 1000);
        Assert.assertTrue(r1.getHasWindowsAttributes());
        Assert.assertEquals(0x20, r1.getWindowsAttributes());

        SevenZArchiveEntry r2 = in.getNextEntry();
        Assert.assertNotNull(r2);
        Assert.assertEquals("partialMetadata.txt", r2.getName());
        Assert.assertFalse(r2.getHasLastModifiedDate());
        Assert.assertFalse(r2.getHasAccessDate());
        Assert.assertFalse(r2.getHasCreationDate());
        Assert.assertFalse(r2.getHasWindowsAttributes());

        Assert.assertNull(in.getNextEntry());
        in.close();
    }

    @Test
    public void testAllEntriesHaveMetadata() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output7zFile);

        Date now = new Date(1600000000000L);

        SevenZArchiveEntry entry1 = new SevenZArchiveEntry();
        entry1.setName("e1.txt");
        entry1.setLastModifiedDate(now);
        entry1.setAccessDate(now);
        entry1.setCreationDate(now);
        entry1.setWindowsAttributes(0x01);
        out.putArchiveEntry(entry1);
        out.write("1".getBytes("UTF-8"));
        out.closeArchiveEntry();

        SevenZArchiveEntry entry2 = new SevenZArchiveEntry();
        entry2.setName("e2.txt");
        entry2.setLastModifiedDate(now);
        entry2.setAccessDate(now);
        entry2.setCreationDate(now);
        entry2.setWindowsAttributes(0x02);
        out.putArchiveEntry(entry2);
        out.write("2".getBytes("UTF-8"));
        out.closeArchiveEntry();

        out.close();

        SevenZFile in = new SevenZFile(output7zFile);
        SevenZArchiveEntry r1 = in.getNextEntry();
        Assert.assertNotNull(r1);
        Assert.assertTrue(r1.getHasLastModifiedDate());
        Assert.assertTrue(r1.getHasAccessDate());
        Assert.assertTrue(r1.getHasCreationDate());
        Assert.assertTrue(r1.getHasWindowsAttributes());
        Assert.assertEquals(0x01, r1.getWindowsAttributes());

        SevenZArchiveEntry r2 = in.getNextEntry();
        Assert.assertNotNull(r2);
        Assert.assertTrue(r2.getHasLastModifiedDate());
        Assert.assertTrue(r2.getHasAccessDate());
        Assert.assertTrue(r2.getHasCreationDate());
        Assert.assertTrue(r2.getHasWindowsAttributes());
        Assert.assertEquals(0x02, r2.getWindowsAttributes());

        in.close();
    }

    @Test
    public void testManyEntriesForBitSetLengthBoundaries() throws IOException {
        SevenZOutputFile out = new SevenZOutputFile(output7zFile);
        out.setContentCompression(SevenZMethod.COPY);

        // Writing 16 entries tests exact byte boundary (8 bits per byte * 2) and partial bit masks
        for (int i = 0; i < 16; i++) {
            SevenZArchiveEntry entry = new SevenZArchiveEntry();
            entry.setName("file_" + i + ".txt");
            if (i % 2 == 0) {
                entry.setLastModifiedDate(new Date(10000000L * i));
            } else {
                entry.setHasLastModifiedDate(false);
            }
            if (i % 3 == 0) {
                entry.setAccessDate(new Date(20000000L * i));
            } else {
                entry.setHasAccessDate(false);
            }
            if (i % 4 == 0) {
                entry.setCreationDate(new Date(30000000L * i));
            } else {
                entry.setHasCreationDate(false);
            }
            if (i % 5 == 0) {
                entry.setWindowsAttributes(i);
            } else {
                entry.setHasWindowsAttributes(false);
            }
            out.putArchiveEntry(entry);
            if (i % 2 == 0) {
                out.write(("Content " + i).getBytes("UTF-8"));
            }
            out.closeArchiveEntry();
        }

        out.close();

        SevenZFile in = new SevenZFile(output7zFile);
        for (int i = 0; i < 16; i++) {
            SevenZArchiveEntry readEntry = in.getNextEntry();
            Assert.assertNotNull(readEntry);
            Assert.assertEquals("file_" + i + ".txt", readEntry.getName());
            Assert.assertEquals(i % 2 == 0, readEntry.getHasLastModifiedDate());
            Assert.assertEquals(i % 3 == 0, readEntry.getHasAccessDate());
            Assert.assertEquals(i % 4 == 0, readEntry.getHasCreationDate());
            Assert.assertEquals(i % 5 == 0, readEntry.getHasWindowsAttributes());
        }
        Assert.assertNull(in.getNextEntry());
        in.close();
    }
}
