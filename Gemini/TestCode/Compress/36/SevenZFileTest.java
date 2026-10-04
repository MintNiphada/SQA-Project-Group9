package org.apache.commons.compress.archivers.sevenz;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.zip.CRC32;

public class SevenZFileTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testMatches() {
        byte[] valid = new byte[] { (byte)'7', (byte)'z', (byte)0xBC, (byte)0xAF, (byte)0x27, (byte)0x1C };
        Assert.assertTrue(SevenZFile.matches(valid, 6));
        Assert.assertTrue(SevenZFile.matches(valid, 10));

        byte[] invalid = new byte[] { (byte)'7', (byte)'z', (byte)0xBC, (byte)0xAF, (byte)0x27, (byte)0x1D };
        Assert.assertFalse(SevenZFile.matches(invalid, 6));
        Assert.assertFalse(SevenZFile.matches(valid, 5));
    }

    @Test(expected = IOException.class)
    public void testBadSignature() throws IOException {
        File file = tempFolder.newFile("bad_sig.7z");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(new byte[32]);
        }
        new SevenZFile(file);
    }

    @Test(expected = IOException.class)
    public void testUnsupportedVersion() throws IOException {
        File file = tempFolder.newFile("bad_ver.7z");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(new byte[] { (byte)'7', (byte)'z', (byte)0xBC, (byte)0xAF, (byte)0x27, (byte)0x1C });
            fos.write(1);
            fos.write(0);
            fos.write(new byte[24]);
        }
        new SevenZFile(file);
    }

    @Test(expected = IOException.class)
    public void testStartHeaderCrcMismatch() throws IOException {
        File file = tempFolder.newFile("bad_sh_crc.7z");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(new byte[] { (byte)'7', (byte)'z', (byte)0xBC, (byte)0xAF, (byte)0x27, (byte)0x1C });
            fos.write(0);
            fos.write(2);
            fos.write(new byte[] { 1, 2, 3, 4 });
            fos.write(new byte[20]);
        }
        new SevenZFile(file);
    }

    @Test(expected = IOException.class)
    public void testNextHeaderCrcMismatch() throws IOException {
        byte[] nextHeader = new byte[] { (byte) NID.kHeader, (byte) NID.kEnd };
        File file = create7zFile(nextHeader, 12345678L, 0);
        new SevenZFile(file);
    }

    @Test(expected = IOException.class)
    public void testNoHeaderNid() throws IOException {
        byte[] nextHeader = new byte[] { (byte) NID.kEnd };
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);
        new SevenZFile(file);
    }

    @Test
    public void testMinimalEmptyArchive() throws IOException {
        byte[] nextHeader = new byte[] { (byte) NID.kHeader, (byte) NID.kEnd };
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);

        SevenZFile sevenZFile = new SevenZFile(file, new byte[] { 0, 0 });
        Assert.assertNotNull(sevenZFile.getEntries());
        Assert.assertNull(sevenZFile.getNextEntry());
        Assert.assertNotNull(sevenZFile.toString());
        sevenZFile.close();
    }

    @Test(expected = IllegalStateException.class)
    public void testReadWithoutEntryThrows() throws IOException {
        byte[] nextHeader = new byte[] { (byte) NID.kHeader, (byte) NID.kEnd };
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);

        try (SevenZFile sevenZFile = new SevenZFile(file)) {
            sevenZFile.read();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testReadBufferWithoutEntryThrows() throws IOException {
        byte[] nextHeader = new byte[] { (byte) NID.kHeader, (byte) NID.kEnd };
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);

        try (SevenZFile sevenZFile = new SevenZFile(file)) {
            sevenZFile.read(new byte[10]);
        }
    }

    @Test(expected = IOException.class)
    public void testArchiveWithAdditionalStreamsUnsupported() throws IOException {
        byte[] nextHeader = new byte[] { (byte) NID.kHeader, (byte) NID.kAdditionalStreamsInfo, (byte) NID.kEnd };
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);
        new SevenZFile(file);
    }

    @Test(expected = IOException.class)
    public void testBadlyTerminatedHeader() throws IOException {
        byte[] nextHeader = new byte[] { (byte) NID.kHeader, 0x7F };
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);
        new SevenZFile(file);
    }

    @Test
    public void testArchivePropertiesSkipped() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(NID.kHeader);
        baos.write(NID.kArchiveProperties);
        baos.write(0x01);
        baos.write(2);
        baos.write(new byte[] { 0x11, 0x22 });
        baos.write(NID.kEnd);
        baos.write(NID.kEnd);
        byte[] nextHeader = baos.toByteArray();

        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);

        try (SevenZFile sevenZFile = new SevenZFile(file)) {
            Assert.assertNull(sevenZFile.getNextEntry());
        }
    }

    @Test
    public void testEmptyFilesInfo() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(NID.kHeader);
        baos.write(NID.kFilesInfo);
        baos.write(0x00);
        baos.write(0x00);
        baos.write(NID.kEnd);
        byte[] nextHeader = baos.toByteArray();

        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);

        try (SevenZFile sevenZFile = new SevenZFile(file)) {
            Iterable<SevenZArchiveEntry> entries = sevenZFile.getEntries();
            Assert.assertFalse(entries.iterator().hasNext());
            Assert.assertNull(sevenZFile.getNextEntry());
        }
    }

    @Test
    public void testFilesInfoWithEmptyDirectories() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(NID.kHeader);
        baos.write(NID.kFilesInfo);
        baos.write(0x01);

        baos.write(NID.kEmptyStream);
        baos.write(0x80);

        baos.write(NID.kEmptyFile);
        baos.write(0x00);

        baos.write(NID.kAnti);
        baos.write(0x00);

        baos.write(NID.kName);
        baos.write(0x00);
        byte[] utf16Name = new byte[] { 'd', 0, 'i', 0, 'r', 0, 0, 0 };
        writeUint64(baos, utf16Name.length + 1);
        baos.write(utf16Name);

        baos.write(NID.kCTime);
        writeUint64(baos, 10);
        baos.write(1);
        baos.write(0);
        writeLongLE(baos, 1000L);

        baos.write(NID.kATime);
        writeUint64(baos, 10);
        baos.write(1);
        baos.write(0);
        writeLongLE(baos, 2000L);

        baos.write(NID.kMTime);
        writeUint64(baos, 10);
        baos.write(1);
        baos.write(0);
        writeLongLE(baos, 3000L);

        baos.write(NID.kWinAttributes);
        writeUint64(baos, 6);
        baos.write(1);
        baos.write(0);
        writeIntLE(baos, 16);

        baos.write(NID.kDummy);
        writeUint64(baos, 3);
        baos.write(new byte[] { 0, 0, 0 });

        baos.write(0x20);
        writeUint64(baos, 2);
        baos.write(new byte[] { 0, 0 });

        baos.write(0x00);
        baos.write(NID.kEnd);

        byte[] nextHeader = baos.toByteArray();
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);

        try (SevenZFile sevenZFile = new SevenZFile(file)) {
            Iterable<SevenZArchiveEntry> entries = sevenZFile.getEntries();
            Iterator<SevenZArchiveEntry> it = entries.iterator();
            Assert.assertTrue(it.hasNext());
            SevenZArchiveEntry entry = it.next();
            Assert.assertEquals("dir", entry.getName());
            Assert.assertTrue(entry.isDirectory());
            Assert.assertFalse(entry.isAntiItem());
            Assert.assertTrue(entry.getHasCreationDate());
            Assert.assertTrue(entry.getHasAccessDate());
            Assert.assertTrue(entry.getHasLastModifiedDate());
            Assert.assertTrue(entry.getHasWindowsAttributes());
            Assert.assertEquals(16, entry.getWindowsAttributes());

            SevenZArchiveEntry readEntry = sevenZFile.getNextEntry();
            Assert.assertNotNull(readEntry);
            Assert.assertEquals("dir", readEntry.getName());
            Assert.assertNull(sevenZFile.getNextEntry());
        }
    }

    @Test(expected = IOException.class)
    public void testEmptyFileBeforeEmptyStreamFails() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(NID.kHeader);
        baos.write(NID.kFilesInfo);
        baos.write(0x01);
        baos.write(NID.kEmptyFile);
        baos.write(0x80);
        baos.write(0x00);
        baos.write(NID.kEnd);

        byte[] nextHeader = baos.toByteArray();
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);
        new SevenZFile(file);
    }

    @Test(expected = IOException.class)
    public void testAntiBeforeEmptyStreamFails() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(NID.kHeader);
        baos.write(NID.kFilesInfo);
        baos.write(0x01);
        baos.write(NID.kAnti);
        baos.write(0x80);
        baos.write(0x00);
        baos.write(NID.kEnd);

        byte[] nextHeader = baos.toByteArray();
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);
        new SevenZFile(file);
    }

    @Test(expected = IOException.class)
    public void testStartPosPropertyThrows() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(NID.kHeader);
        baos.write(NID.kFilesInfo);
        baos.write(0x01);
        baos.write(NID.kStartPos);
        baos.write(0x01);
        baos.write(0x00);
        baos.write(NID.kEnd);

        byte[] nextHeader = baos.toByteArray();
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);
        new SevenZFile(file);
    }

    @Test
    public void testCloseMultipleTimes() throws IOException {
        byte[] nextHeader = new byte[] { (byte) NID.kHeader, (byte) NID.kEnd };
        CRC32 crc = new CRC32();
        crc.update(nextHeader);
        File file = create7zFile(nextHeader, crc.getValue(), 0);

        SevenZFile sevenZFile = new SevenZFile(file);
        sevenZFile.close();
        sevenZFile.close();
    }

    private File create7zFile(byte[] nextHeader, long nextHeaderCrc, long nextHeaderOffset) throws IOException {
        File file = tempFolder.newFile();
        try (FileOutputStream fos = new FileOutputStream(file);
             DataOutputStream dos = new DataOutputStream(fos)) {

            dos.write(new byte[] { (byte)'7', (byte)'z', (byte)0xBC, (byte)0xAF, (byte)0x27, (byte)0x1C });
            dos.writeByte(0);
            dos.writeByte(4);

            ByteArrayOutputStream shBaos = new ByteArrayOutputStream();
            DataOutputStream shDos = new DataOutputStream(shBaos);
            shDos.writeLong(Long.reverseBytes(nextHeaderOffset));
            shDos.writeLong(Long.reverseBytes(nextHeader.length));
            shDos.writeInt(Integer.reverseBytes((int) nextHeaderCrc));
            shDos.flush();
            byte[] startHeaderBytes = shBaos.toByteArray();

            CRC32 shCrc = new CRC32();
            shCrc.update(startHeaderBytes);
            dos.writeInt(Integer.reverseBytes((int) shCrc.getValue()));
            dos.write(startHeaderBytes);

            dos.write(nextHeader);
        }
        return file;
    }

    private static void writeUint64(ByteArrayOutputStream baos, long value) {
        if (value < 0x80) {
            baos.write((int) value);
        } else {
            baos.write(0x80 | 1);
            baos.write((int) (value & 0xFF));
            baos.write((int) ((value >> 8) & 0xFF));
        }
    }

    private static void writeLongLE(ByteArrayOutputStream baos, long val) {
        for (int i = 0; i < 8; i++) {
            baos.write((int) ((val >> (8 * i)) & 0xFF));
        }
    }

    private static void writeIntLE(ByteArrayOutputStream baos, int val) {
        for (int i = 0; i < 4; i++) {
            baos.write((val >> (8 * i)) & 0xFF);
        }
    }
}
