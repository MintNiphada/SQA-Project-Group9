package org.apache.commons.compress.changes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.junit.Assert;
import org.junit.Test;

public class ChangeSetPerformerTest {

    private static class DummyArchiveEntry implements ArchiveEntry {
        private final String name;
        private final long size;

        DummyArchiveEntry(String name) {
            this(name, 0);
        }

        DummyArchiveEntry(String name, long size) {
            this.name = name;
            this.size = size;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }

        public boolean isDirectory() {
            return name != null && name.endsWith("/");
        }

        public Date getLastModifiedDate() {
            return new Date();
        }
    }

    private static class DummyArchiveInputStream extends ArchiveInputStream {
        private final List<ArchiveEntry> entries;
        private final List<byte[]> contents;
        private int index = 0;
        private ByteArrayInputStream currentStream = null;

        DummyArchiveInputStream(List<ArchiveEntry> entries, List<byte[]> contents) {
            this.entries = entries;
            this.contents = contents;
        }

        public ArchiveEntry getNextEntry() throws IOException {
            if (index < entries.size()) {
                ArchiveEntry entry = entries.get(index);
                byte[] content = (contents != null && index < contents.size()) ? contents.get(index) : new byte[0];
                currentStream = new ByteArrayInputStream(content);
                index++;
                return entry;
            }
            return null;
        }

        public int read() throws IOException {
            if (currentStream != null) {
                return currentStream.read();
            }
            return -1;
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (currentStream != null) {
                return currentStream.read(b, off, len);
            }
            return -1;
        }
    }

    private static class DummyArchiveOutputStream extends ArchiveOutputStream {
        private final ByteArrayOutputStream underlying = new ByteArrayOutputStream();
        private final List<String> writtenEntryNames = new ArrayList<String>();
        private boolean entryOpen = false;

        public void putArchiveEntry(ArchiveEntry entry) throws IOException {
            if (entryOpen) {
                throw new IOException("Previous entry not closed");
            }
            entryOpen = true;
            writtenEntryNames.add(entry.getName());
        }

        public void closeArchiveEntry() throws IOException {
            if (!entryOpen) {
                throw new IOException("No entry to close");
            }
            entryOpen = false;
        }

        public void finish() throws IOException {
        }

        public ArchiveEntry createArchiveEntry(File inputFile, String entryName) throws IOException {
            return new DummyArchiveEntry(entryName);
        }

        public void write(int b) throws IOException {
            underlying.write(b);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            underlying.write(b, off, len);
        }

        public List<String> getWrittenEntryNames() {
            return writtenEntryNames;
        }

        public byte[] toByteArray() {
            return underlying.toByteArray();
        }
    }

    @Test
    public void testEmptyChangeSetAndEmptyStream() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        DummyArchiveInputStream in = new DummyArchiveInputStream(
                new ArrayList<ArchiveEntry>(),
                new ArrayList<byte[]>()
        );
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.getAddedFromChangeSet().isEmpty());
        Assert.assertTrue(results.getAddedFromStream().isEmpty());
        Assert.assertTrue(results.getDeleted().isEmpty());
        Assert.assertTrue(out.getWrittenEntryNames().isEmpty());
    }

    @Test
    public void testPassThroughStreamWithoutChanges() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(
                new DummyArchiveEntry("file1.txt"),
                new DummyArchiveEntry("file2.txt")
        );
        List<byte[]> contents = Arrays.asList("content1".getBytes(), "content2".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertEquals(2, results.getAddedFromStream().size());
        Assert.assertTrue(results.getAddedFromStream().contains("file1.txt"));
        Assert.assertTrue(results.getAddedFromStream().contains("file2.txt"));
        Assert.assertEquals(2, out.getWrittenEntryNames().size());
        Assert.assertEquals("file1.txt", out.getWrittenEntryNames().get(0));
        Assert.assertEquals("file2.txt", out.getWrittenEntryNames().get(1));
    }

    @Test
    public void testAddReplaceModeTrue() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        ArchiveEntry newEntry = new DummyArchiveEntry("added.txt");
        InputStream input = new ByteArrayInputStream("added-content".getBytes());
        changeSet.add(newEntry, input, true);

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(new DummyArchiveEntry("old.txt"));
        List<byte[]> contents = Arrays.asList("old-content".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertTrue(results.getAddedFromChangeSet().contains("added.txt"));
        Assert.assertTrue(results.getAddedFromStream().contains("old.txt"));
        Assert.assertEquals(2, out.getWrittenEntryNames().size());
        Assert.assertEquals("added.txt", out.getWrittenEntryNames().get(0));
        Assert.assertEquals("old.txt", out.getWrittenEntryNames().get(1));
    }

    @Test
    public void testAddReplaceModeFalseWhenEntryDoesNotExistInStream() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        ArchiveEntry newEntry = new DummyArchiveEntry("new_file.txt");
        InputStream input = new ByteArrayInputStream("new-content".getBytes());
        changeSet.add(newEntry, input, false);

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(new DummyArchiveEntry("stream_file.txt"));
        List<byte[]> contents = Arrays.asList("stream-content".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertTrue(results.getAddedFromStream().contains("stream_file.txt"));
        Assert.assertTrue(results.getAddedFromChangeSet().contains("new_file.txt"));
        Assert.assertEquals(2, out.getWrittenEntryNames().size());
        Assert.assertEquals("stream_file.txt", out.getWrittenEntryNames().get(0));
        Assert.assertEquals("new_file.txt", out.getWrittenEntryNames().get(1));
    }

    @Test
    public void testAddReplaceModeFalseWhenEntryExistsInStream() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        ArchiveEntry newEntry = new DummyArchiveEntry("common.txt");
        InputStream input = new ByteArrayInputStream("changeset-version".getBytes());
        changeSet.add(newEntry, input, false);

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(new DummyArchiveEntry("common.txt"));
        List<byte[]> contents = Arrays.asList("stream-version".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertTrue(results.getAddedFromStream().contains("common.txt"));
        Assert.assertFalse(results.getAddedFromChangeSet().contains("common.txt"));
        Assert.assertEquals(1, out.getWrittenEntryNames().size());
        Assert.assertEquals("common.txt", out.getWrittenEntryNames().get(0));
        Assert.assertEquals("stream-version", new String(out.toByteArray()));
    }

    @Test
    public void testDeleteFile() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        changeSet.delete("delete_me.txt");

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(
                new DummyArchiveEntry("delete_me.txt"),
                new DummyArchiveEntry("keep_me.txt")
        );
        List<byte[]> contents = Arrays.asList("delete-content".getBytes(), "keep-content".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertTrue(results.getDeleted().contains("delete_me.txt"));
        Assert.assertTrue(results.getAddedFromStream().contains("keep_me.txt"));
        Assert.assertEquals(1, out.getWrittenEntryNames().size());
        Assert.assertEquals("keep_me.txt", out.getWrittenEntryNames().get(0));
    }

    @Test
    public void testDeleteDir() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        changeSet.deleteDir("target_dir");

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(
                new DummyArchiveEntry("target_dir/subfile.txt"),
                new DummyArchiveEntry("target_dir/nested/file.txt"),
                new DummyArchiveEntry("other_dir/file.txt"),
                new DummyArchiveEntry("target_dir_prefix/file.txt")
        );
        List<byte[]> contents = Arrays.asList("1".getBytes(), "2".getBytes(), "3".getBytes(), "4".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertTrue(results.getDeleted().contains("target_dir/subfile.txt"));
        Assert.assertTrue(results.getDeleted().contains("target_dir/nested/file.txt"));
        Assert.assertEquals(2, results.getDeleted().size());
        Assert.assertTrue(results.getAddedFromStream().contains("other_dir/file.txt"));
        Assert.assertTrue(results.getAddedFromStream().contains("target_dir_prefix/file.txt"));
        Assert.assertEquals(2, out.getWrittenEntryNames().size());
    }

    @Test
    public void testNullEntryNameHandling() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        changeSet.delete("some_file.txt");
        changeSet.deleteDir("some_dir");

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(
                new DummyArchiveEntry(null),
                new DummyArchiveEntry("normal.txt")
        );
        List<byte[]> contents = Arrays.asList("null-name".getBytes(), "normal".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertEquals(2, out.getWrittenEntryNames().size());
    }

    @Test
    public void testMultipleOperationsOnSamePerformerInstance() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        changeSet.delete("delete.txt");
        ArchiveEntry addedEntry = new DummyArchiveEntry("added.txt");
        changeSet.add(addedEntry, new ByteArrayInputStream("added".getBytes()), true);

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        for (int i = 0; i < 2; i++) {
            List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(
                    new DummyArchiveEntry("delete.txt"),
                    new DummyArchiveEntry("stream.txt")
            );
            List<byte[]> contents = Arrays.asList("del".getBytes(), "str".getBytes());
            DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
            DummyArchiveOutputStream out = new DummyArchiveOutputStream();

            ChangeSetResults results = performer.perform(in, out);
            Assert.assertTrue(results.getDeleted().contains("delete.txt"));
            Assert.assertTrue(results.getAddedFromStream().contains("stream.txt"));
            Assert.assertTrue(results.getAddedFromChangeSet().contains("added.txt"));
            Assert.assertEquals(2, out.getWrittenEntryNames().size());
            Assert.assertEquals("added.txt", out.getWrittenEntryNames().get(0));
            Assert.assertEquals("stream.txt", out.getWrittenEntryNames().get(1));
        }
    }

    @Test
    public void testAddReplaceModeTrueShadowsStreamEntry() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        ArchiveEntry replaceEntry = new DummyArchiveEntry("replace.txt");
        changeSet.add(replaceEntry, new ByteArrayInputStream("new-content".getBytes()), true);

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(
                new DummyArchiveEntry("replace.txt")
        );
        List<byte[]> contents = Arrays.asList("old-content".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertTrue(results.getAddedFromChangeSet().contains("replace.txt"));
        Assert.assertFalse(results.getAddedFromStream().contains("replace.txt"));
        Assert.assertEquals(1, out.getWrittenEntryNames().size());
        Assert.assertEquals("replace.txt", out.getWrittenEntryNames().get(0));
        Assert.assertEquals("new-content", new String(out.toByteArray()));
    }

    @Test
    public void testDeleteDirDoesNotMatchWithoutTrailingSlash() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        changeSet.deleteDir("folder");

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(
                new DummyArchiveEntry("folder_suffix.txt"),
                new DummyArchiveEntry("folder/inside.txt")
        );
        List<byte[]> contents = Arrays.asList("1".getBytes(), "2".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertTrue(results.getAddedFromStream().contains("folder_suffix.txt"));
        Assert.assertTrue(results.getDeleted().contains("folder/inside.txt"));
        Assert.assertFalse(results.getDeleted().contains("folder_suffix.txt"));
    }

    @Test
    public void testDeleteExactFileAndDirCombined() throws IOException {
        ChangeSet changeSet = new ChangeSet();
        changeSet.delete("file.txt");
        changeSet.deleteDir("dir");

        ChangeSetPerformer performer = new ChangeSetPerformer(changeSet);

        List<ArchiveEntry> entries = Arrays.<ArchiveEntry>asList(
                new DummyArchiveEntry("file.txt"),
                new DummyArchiveEntry("dir/file.txt"),
                new DummyArchiveEntry("other.txt")
        );
        List<byte[]> contents = Arrays.asList("1".getBytes(), "2".getBytes(), "3".getBytes());
        DummyArchiveInputStream in = new DummyArchiveInputStream(entries, contents);
        DummyArchiveOutputStream out = new DummyArchiveOutputStream();

        ChangeSetResults results = performer.perform(in, out);
        Assert.assertEquals(2, results.getDeleted().size());
        Assert.assertEquals(1, results.getAddedFromStream().size());
        Assert.assertTrue(results.getAddedFromStream().contains("other.txt"));
    }
}
