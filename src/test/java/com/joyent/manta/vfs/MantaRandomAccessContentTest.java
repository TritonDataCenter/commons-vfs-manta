package com.joyent.manta.vfs;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.client.MantaSeekableByteChannel;
import com.joyent.manta.exception.MantaCryptoException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.testng.annotations.*;

import java.io.*;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test
public class MantaRandomAccessContentTest {
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

    public void canReadOneByteAtATimeSequentially() throws IOException {
        final byte[] content = "Hello World\nIt is a nice world\0\0\r\t1919191"
                .getBytes(UTF_8);
        try (MantaRandomAccessContent randomAccess = writeBytesToMantaAndGetRandomAccess(content)) {
            assertEquals(randomAccess.length(), content.length,
                    "Lengths of binary data do not match");

            for (int i = 0; i < content.length; i++) {
                assertEquals(randomAccess.getFilePointer(), (long)i,
                        "Position not returned correctly");
                byte expected = content[i];
                byte actual = randomAccess.readByte();

                assertEquals(actual, expected, "Bytes didn't match");
            }

            assertEOFThrown(randomAccess::readByte);
        }
    }

    public void canReadUnsignedByte() throws IOException {
        final byte[] content = new byte[] { 2, 1, 0, -1, -2, -3, };
        final int[] expectedContent = new int[] { 2, 1, 0, 255, 254, 253 };

        try (MantaRandomAccessContent randomAccess = writeBytesToMantaAndGetRandomAccess(content)) {
            assertEquals(randomAccess.length(), content.length,
                    "Lengths of binary data do not match");

            for (int i = 0; i < expectedContent.length; i++) {
                assertEquals(randomAccess.getFilePointer(), (long)i,
                        "Position not returned correctly");
                int expected = expectedContent[i];
                int actual = randomAccess.readUnsignedByte();

                assertEquals(actual, expected, "Bytes didn't match");
            }

            assertEOFThrown(randomAccess::readByte);
        }
    }

    public void canReadUnsignedShort() throws IOException {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());

        short[] testValues = new short[] {
                Short.MAX_VALUE, 2, 1, 0, -1, -2, Short.MIN_VALUE
        };

        int[] expectedValues = new int[] {
                Short.MAX_VALUE, 2, 1, 0, 65535, 65534, 32768
        };

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            for (short s : testValues) {
                objectOut.writeShort(s);
            }
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);

            for (int i : expectedValues) {
                assertEquals(randomAccess.readUnsignedShort(), i, "Unsigned short value written didn't match value read");
            }

            assertEOFThrown(randomAccess::readUnsignedShort);
        }
    }

    public void canReadBytesAtATimeSequentially() throws IOException {
        final byte[] content = new byte[] { 0, 12, 23, 127, 33, 55, 9, 44, 22, 44, 55, 13, 44, 76, 66, 0 };
        MantaRandomAccessContent randomAccess = writeBytesToMantaAndGetRandomAccess(content);

        byte[] buff = new byte[2];

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            boolean finished = false;
            // this won't work for an odd-size content byte array
            while (!finished) {
                try {
                    randomAccess.readFully(buff);
                    out.write(buff);
                } catch (EOFException e) {
                    finished = true;
                }
            }

            byte[] written = out.toByteArray();
            assertEquals(content.length, written.length,
                    "Byte array lengths don't match");
            String expected = Hex.encodeHexString(content);
            String actual = Hex.encodeHexString(written);
            assertEquals(actual, expected, "Read bytes doesn't match actual");
        }
    }

    public void canSkipBytesSequentially() throws IOException {
        final byte[] content = new byte[] { 0, 12, 23, 127, 33, 55, 9, 44, 22, 44, 55, 13, 44, 76, 66, 0 };
        final int skipLength = 4;
        MantaRandomAccessContent randomAccess = writeBytesToMantaAndGetRandomAccess(content);

        randomAccess.skipBytes(skipLength);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // this won't work for an odd-size content byte array
            while (true) {
                try {
                    out.write(randomAccess.readByte());
                } catch (EOFException e) {
                    break;
                }
            }

            byte[] written = out.toByteArray();
            byte[] skipped = ArrayUtils.subarray(
                    content, skipLength, content.length);

            String expected = Hex.encodeHexString(skipped);
            String actual = Hex.encodeHexString(written);
            assertEquals(actual, expected, "Read bytes doesn't match actual");
        }
    }

    public void canSeekToAnyPosition() throws Exception {
        final byte[] content = new byte[] { 0, 12, 23, 127, 33, 55, 9, 44, 22, 44, 55, 13, 44, 76, 66, 0 };
        final int initialPosition = 4;
        try (MantaRandomAccessContent randomAccess = writeBytesToMantaAndGetRandomAccess(content)) {
            randomAccess.seek(initialPosition);
            long position = randomAccess.getFilePointer();
            assertEquals(position, initialPosition);

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                // this won't work for an odd-size content byte array
                int bytesWritten = 0;
                while (true) {
                    try {
                        out.write(randomAccess.readByte());
                        position = randomAccess.getFilePointer();
                        bytesWritten++;
                        assertEquals(position, initialPosition + bytesWritten,
                                "Position is inaccurate after write");
                    } catch (EOFException e) {
                        break;
                    }
                }

                byte[] written = out.toByteArray();
                byte[] skipped = ArrayUtils.subarray(
                        content, initialPosition, content.length);

                String expected = Hex.encodeHexString(skipped);
                String actual = Hex.encodeHexString(written);
                assertEquals(actual, expected, "Read bytes doesn't match actual");
                assertEquals(randomAccess.length(), content.length - initialPosition,
                        "Length of content wasn't updated after seek");
            }
        }
    }

    public void canReadAsciiCharsSequentially() throws Exception {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());
        final String source = "Hello I'm a string. I'm made up of chars.";

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            for (char c : source.toCharArray()) {
                objectOut.writeChar(c);
            }
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);

            for (char c : source.toCharArray()) {
                assertEquals(randomAccess.readChar(), c,
                        "Character written to Manta doesn't match expectation");
            }

            assertEOFThrown(randomAccess::readChar);
        }
    }

    public void canReadBoolean() throws IOException {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            objectOut.writeBoolean(false);
            objectOut.writeBoolean(true);
            objectOut.writeBoolean(false);
            objectOut.writeBoolean(true);
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);

            assertFalse(randomAccess.readBoolean(), "Couldn't read boolean from stream");
            assertTrue(randomAccess.readBoolean(), "Couldn't read boolean from stream");
            assertFalse(randomAccess.readBoolean(), "Couldn't read boolean from stream");
            assertTrue(randomAccess.readBoolean(), "Couldn't read boolean from stream");

            assertEOFThrown(randomAccess::readBoolean);
        }
    }

    public void canReadShort() throws IOException {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());

        short[] testValues = new short[] {
            7, 99, -2323, 0, Short.MIN_VALUE, Short.MAX_VALUE, -1, 44
        };

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            for (short s : testValues) {
                objectOut.writeShort(s);
            }
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);

            for (short s : testValues) {
                assertEquals(randomAccess.readShort(), s, "Short value written didn't match value read");
            }

            assertEOFThrown(randomAccess::readShort);
        }
    }

    public void canReadInteger() throws IOException {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());

        int[] testValues = new int[] {
                7, 99, -2323, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, 44
        };

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            for (int i : testValues) {
                objectOut.writeInt(i);
            }
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);

            for (int i : testValues) {
                assertEquals(randomAccess.readInt(), i, "Integer value written didn't match value read");
            }

            assertEOFThrown(randomAccess::readInt);
        }
    }

    public void canReadLong() throws IOException {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());

        long[] testValues = new long[] {
                7, 99, -2323, 0, Long.MIN_VALUE, Long.MAX_VALUE, -1, 44
        };

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            for (long l : testValues) {
                objectOut.writeLong(l);
            }
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);

            for (long l : testValues) {
                assertEquals(randomAccess.readLong(), l, "Long value written didn't match value read");
            }

            assertEOFThrown(randomAccess::readLong);
        }
    }

    public void canReadFloat() throws IOException {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());

        float[] testValues = new float[] {
                7.11f, 99.99f, -2323.33f, 0f, Float.MIN_VALUE, Float.MAX_VALUE, -1.0f, 44.9f
        };

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            for (float f : testValues) {
                objectOut.writeFloat(f);
            }
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);

            for (float f : testValues) {
                assertEquals(randomAccess.readFloat(), f, "Float value written didn't match value read");
            }

            assertEOFThrown(randomAccess::readFloat);
        }
    }

    public void canReadDouble() throws IOException {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());

        double[] testValues = new double[] {
                7.11d, 99.99d, -2323.33d, 0d, Double.MIN_VALUE, Double.MAX_VALUE, -1.0d, 44.9d
        };

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            for (double d : testValues) {
                objectOut.writeDouble(d);
            }
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);

            for (double d : testValues) {
                assertEquals(randomAccess.readDouble(), d, "Double value written didn't match value read");
            }

            assertEOFThrown(randomAccess::readDouble);
        }
    }

    public void canReadMultiByteCharsSequentially() throws Exception {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());
        final String source = "こんにちは！私はストリングです。キャラクタに基づいている。";

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            for (char c : source.toCharArray()) {
                objectOut.writeChar(c);
            }
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);

            for (char c : source.toCharArray()) {
                assertEquals(randomAccess.readChar(), c,
                        "Character written to Manta doesn't match expectation");
            }

            assertEOFThrown(randomAccess::readChar);
        }
    }

    public void canReadModifiedUTF8String() throws Exception {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());
        final String source = "こんにちは！私はストリングです。キャラクタに基づいている。";

        try (ObjectOutputStream objectOut = mantaOutputStream(path)) {
            objectOut.writeUTF(source);
        }

        try (MantaRandomAccessContent randomAccess = getRandomAccessContent(path)) {
            readHeaders(randomAccess);
            String actual = randomAccess.readUTF();
            assertEquals(actual, source, "UTF8 decoding didn't work properly");
        }
    }

    private MantaRandomAccessContent writeBytesToMantaAndGetRandomAccess(
            final byte[] bytes) throws IOException {
        final String path = String.format("%s/byte-content-%s.txt", testPathPrefix, UUID.randomUUID());
        mantaClient.put(path, bytes);
        MantaSeekableByteChannel channel = mantaClient.getSeekableByteChannel(path);
        return new MantaRandomAccessContent(channel);
    }

    private MantaRandomAccessContent getRandomAccessContent(
            final String path) throws IOException {
        MantaSeekableByteChannel channel = mantaClient.getSeekableByteChannel(path);
        return new MantaRandomAccessContent(channel);
    }

    private ObjectOutputStream mantaOutputStream(final String path) throws IOException {
        OutputStream mout = mantaClient.putAsOutputStream(path);
        return new ObjectOutputStream(mout);
    }

    private void readHeaders(final MantaRandomAccessContent randomAccess) throws IOException {
        assertEquals(randomAccess.readShort(), ObjectStreamConstants.STREAM_MAGIC,
                "Byte isn't magic header");
        assertEquals(randomAccess.readShort(), ObjectStreamConstants.STREAM_VERSION,
                "Byte isn't version header");
        randomAccess.readShort(); // I'm not sure what this byte is
    }

    private void assertEOFThrown(final EofFunction throwFunction) throws IOException {
        boolean finished = false;

        try {
            throwFunction.apply();
        } catch (EOFException e) {
            finished = true;
        }

        assertTrue(finished, "EOF exception not thrown");
    }

    interface EofFunction {
        void apply() throws EOFException, IOException;
    }
}
