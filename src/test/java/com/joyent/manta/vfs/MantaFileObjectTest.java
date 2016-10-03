package com.joyent.manta.vfs;

import com.joyent.manta.client.*;
import com.joyent.manta.exception.MantaCryptoException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.vfs2.*;
import org.testng.annotations.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class MantaFileObjectTest {
    private FileSystemManager fsManager;
    private MantaFileSystem mantaFs;
    private MantaClient mantaClient;
    private String testPathPrefix;

    @BeforeSuite
    public void beforeSuite() throws Exception {
        this.fsManager = VFS.getManager();
        @SuppressWarnings("unchecked")
        MantaFileSystem fs = (MantaFileSystem)fsManager.resolveFile("manta:///").getFileSystem();
        this.mantaFs = fs;
    }

    @BeforeClass
    public void setup() throws Exception {
        this.mantaClient = this.mantaFs.getClient();
        testPathPrefix = String.format("%s/stor/vfs-test/%s/",
                this.mantaFs.getMantaConfig().getMantaHomeDirectory(), UUID.randomUUID());
        mantaClient.putDirectory(testPathPrefix, true);
    }

    @AfterClass
    public void afterClass() throws IOException, MantaCryptoException {
        if (mantaClient != null) {
            mantaClient.deleteRecursive(testPathPrefix);
        }
    }

    @AfterSuite
    public void cleanUpSuite() throws Exception {
        this.fsManager.closeFileSystem(this.mantaFs);
    }

    public void canUploadSmallFile() throws Exception {
        final Path file = tempFile("small-upload", ".txt");
        final String path = testPathPrefix + file.getFileName();
        final String contents = "Hello. I'm a file.";
        FileUtils.write(file.toFile(), contents, StandardCharsets.UTF_8.name());

        final FileObject localFile = fsManager.resolveFile(file.toString());
        final MantaFileObject remoteFile = testObject(path, FileType.IMAGINARY);
        remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);

        assertTrue(mantaClient.existsAndIsAccessible(path),
            String.format("File wasn't copied to expected location: %s", path));

        assertFalse(mantaClient.head(path).isDirectory(),
                String.format("Path should point to a file - not a directory: %s", path));

        assertEquals(mantaClient.getAsString(path),
                     contents,
                     "Actual uploaded contents differs from expectation");
    }

    public void canUploadFile() throws Exception {
        final Path file = tempFile("random-bytes-uploaded", ".random");
        final String path = testPathPrefix + file.getFileName();
        final byte[] contents = RandomUtils.nextBytes(1024 * 24);
        FileUtils.writeByteArrayToFile(file.toFile(), contents);

        final FileObject localFile = fsManager.resolveFile(file.toString());
        final MantaFileObject remoteFile = testObject(path, FileType.IMAGINARY);
        remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);

        final MantaObjectResponse head = remoteFile.head();

        assertFalse(head.isDirectory(),
                String.format("Path should point to a file - not a directory: %s", path));

        byte[] localMd5 = fileMd5(file);
        byte[] remoteMd5 = remoteFile.head().getMd5Bytes();

        assertTrue(Arrays.equals(localMd5, remoteMd5),
                   "MD5 of local and remote file didn't match");
    }

    @Test(expectedExceptions = { org.apache.commons.vfs2.FileSystemException.class })
    public void cantAppendFile() throws Exception {
        final String contents = "Go ahead and try to append.";
        final String path = String.format("%sfile-append-test-%s.txt", testPathPrefix,
                UUID.randomUUID());
        mantaClient.put(path, contents);
        FileObject object = fsManager.resolveFile(String.format("manta://%s", path));
        assertTrue(object.exists(), "File wasn't created successfully");

        object.getContent().getOutputStream(true);
    }

    public void canDeleteFile() throws Exception {
        final Path file = tempFile("file-to-delete", ".txt");
        final String path = testPathPrefix + file.getFileName();
        mantaClient.put(path, "Delete me");

        final MantaFileObject object = testObject(path, FileType.FILE);
        object.delete();

        assertFalse(mantaClient.existsAndIsAccessible(path),
                "File wasn't deleted successfully");
    }

    public void canListRootDir() throws FileSystemException {
        FileObject mantaFile = fsManager.resolveFile("manta:///");

        assertTrue(mantaFile.isFolder(), "Folder is not marked as a proper folder");
        FileObject[] children = mantaFile.getChildren();
        assertEquals(children.length, 1, "Directory missing single child");
        assertEquals(children[0].getName().getPath(), homeDir(),
                "Home directory not returned as child from root directory");
    }

    public void canListDir() throws FileSystemException {
        FileObject mantaFile = fsManager.resolveFile("manta://" + homeDir());

        assertTrue(mantaFile.isFolder(), "Folder is not marked as a proper folder");
        FileObject[] children = mantaFile.getChildren();
        assertTrue(children.length > 0, "Directory is not empty");
    }

    public void canGetFileLength() throws Exception {
        final String contents = "I'm a string";
        final long length = (long)contents.length();
        final String path = String.format("%sfile-length-test-%s.txt", testPathPrefix,
                UUID.randomUUID());
        mantaClient.put(path, contents);

        FileObject object = fsManager.resolveFile(String.format("manta://%s", path));
        final long remoteLength = object.getContent().getSize();
        assertEquals(remoteLength, length,
                "byte lengths should match");
    }

    public void canGetFileLastModifiedTime() throws Exception {
        final String contents = "I'm setting a time value";
        final String path = String.format("%sfile-lastmod-test-%s.txt", testPathPrefix,
                UUID.randomUUID());
        MantaObjectResponse response = mantaClient.put(path, contents);

        FileObject object = fsManager.resolveFile(String.format("manta://%s", path));
        final long uploadLastModified = response.getLastModifiedTime().getTime();
        final long remoteLastModified = object.getContent().getLastModifiedTime();
        assertEquals(remoteLastModified, uploadLastModified,
                "last modified times should match");
    }

    public void canRecognizeDirectory() throws Exception {
        final String path = homeDir();
        FileObject object = fsManager.resolveFile(String.format("manta://%s", path));

        assertEquals(object.getType(), FileType.FOLDER,
                "Incorrect file type returned");
    }

    public void canRecognizeFile() throws Exception {
        final String contents = "I'm a file";
        final String path = String.format("%sfile-type-test-%s.txt", testPathPrefix,
                UUID.randomUUID());
        mantaClient.put(path, contents);

        FileObject object = fsManager.resolveFile(String.format("manta://%s", path));

        assertEquals(object.getType(), FileType.FILE,
                "Incorrect file type returned:");
    }

    public void canGetAttributesForFile() throws Exception {
        final String contents = "I'm a file with attributes";
        final String path = String.format("%sfile-type-attr-test-%s.txt", testPathPrefix,
                UUID.randomUUID());
        MantaMetadata metadata = new MantaMetadata();
        metadata.put("m-my-header", "This is a value");
        mantaClient.put(path, contents, metadata);

        FileObject object = fsManager.resolveFile(String.format("manta://%s", path));
        Map<String, Object> attributes = object.getContent().getAttributes();
        assertEquals(attributes.get("my-header"),
                     metadata.get("m-my-header"),
                     "Custom header on write was not passed to attributes");
    }

    public void canSetAndGetAttributesForFile() throws Exception {
        final String contents = "I'm a file with attributes";
        final String path = String.format("%sfile-type-attr-test-%s.txt", testPathPrefix,
                UUID.randomUUID());
        mantaClient.put(path, contents);

        FileObject object = fsManager.resolveFile(String.format("manta://%s", path));
        object.getContent().setAttribute("some-key", "some-value");

        Map<String, Object> attributes = object.getContent().getAttributes();
        assertEquals(attributes.get("some-key"), "some-value",
                "Attribute wasn't properly set");
    }

    public void canRemoveAttributesForFile() throws Exception {
        final String contents = "I'm a file with attributes";
        final String path = String.format("%sfile-type-attr-test-%s.txt", testPathPrefix,
                UUID.randomUUID());
        MantaMetadata metadata = new MantaMetadata();
        metadata.put("m-delete-me", "hello!");
        mantaClient.put(path, contents, metadata);

        FileObject object = fsManager.resolveFile(String.format("manta://%s", path));
        object.getContent().removeAttribute("m-delete-me");

        assertFalse(object.getContent().hasAttribute("m-delete-me"),
                "Attribute wasn't properly deleted");
    }

    public void rootDirIsNotWritable() throws Exception {
        FileObject object = fsManager.resolveFile("manta:///testFile");
        assertFalse(object.isWriteable(), "Root directory is not writable");
    }

    public void wontModifyCorrectPath() {
        final String path = "/user/stor/directory";
        final MantaFileObject object = testObject(path, FileType.FOLDER);
        final String actual = object.path();

        assertEquals(actual, path, "Path was modified from original path");
    }

    public void willNormalizePath() {
        final String path = "/user//stor/directory";
        final MantaFileObject object = testObject(path, FileType.FOLDER);
        final String actual = object.path();
        final String expected = "/user/stor/directory";

        assertEquals(actual, expected, "Path was not normalized properly");
    }

    private MantaFileObject testObject(final String path, final FileType type) {
        final MantaFileName name = new MantaFileName("manta", path, type);
        final MantaFileObject object = new MantaFileObject(name, this.mantaFs);

        return object;
    }

    private String homeDir() {
        return mantaFs.getMantaConfig().getMantaHomeDirectory();
    }

    private byte[] fileMd5(final Path file) throws IOException, NoSuchAlgorithmException {
        byte[] b = Files.readAllBytes(file);
        return MessageDigest.getInstance("MD5").digest(b);
    }

    private Path tempFile(final String prefix, final String suffix) throws IOException {
        final String start = String.format("%s-%s", prefix, UUID.randomUUID());
        final Path file = Files.createTempFile(start, suffix);

        FileUtils.forceDeleteOnExit(file.toFile());

        return file;
    }
}
