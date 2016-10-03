package com.joyent.manta.vfs;

import com.joyent.manta.client.MantaSeekableByteChannel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.provider.AbstractRandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.*;

/**
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 */
public class MantaRandomAccessContent extends AbstractRandomAccessContent
        implements AutoCloseable {
    public static int EOF = -1;

    private MantaSeekableByteChannel channel;

    public MantaRandomAccessContent(final MantaSeekableByteChannel channel) {
        super(RandomAccessMode.READ);

        this.channel = channel;
    }

    @Override
    public void close() throws IOException {
        this.channel.close();
    }

    @Override
    public long getFilePointer() throws IOException {
        return this.channel.position();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return this.channel;
    }

    @Override
    public long length() throws IOException {
        return this.channel.size();
    }

    @Override
    public void seek(final long pos) throws IOException {
        if (pos < 0) {
            throw new IOException("Attempt to position before the start of the file");
        }

        MantaSeekableByteChannel oldChannel = this.channel;

        @SuppressWarnings("unchecked")
        final MantaSeekableByteChannel newChannel =
                (MantaSeekableByteChannel)this.channel.position(pos);
        this.channel = newChannel;

        IOUtils.closeQuietly(oldChannel);
    }

    @Override
    public void setLength(final long newLength) throws IOException {
        throw new UnsupportedOperationException("Truncation is not supported");
    }

    @Override
    public void readFully(final byte[] b) throws IOException {
        if (b == null) {
            throw new NullPointerException("byte array value must not be null");
        }

        if (this.channel.read(b) == EOF) {
            throw new EOFException();
        }
    }

    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException("byte array value must not be null");
        }

        if (this.channel.read(b, off, len) == EOF) {
            throw new EOFException();
        }
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        if (n < 0) {
            throw new IndexOutOfBoundsException(
                    "The skip number can't be negative");
        }

        return (int)this.channel.skip(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        final int byteRead = this.channel.read();
        if (byteRead == EOF) {
            throw new EOFException();
        }

        return byteRead != 0;
    }

    @Override
    public byte readByte() throws IOException {
        return (byte)this.readUnsignedByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        final int byteRead = this.channel.read();
        if (byteRead == EOF) {
            throw new EOFException();
        }

        return byteRead;
    }

    @Override
    public short readShort() throws IOException {
        int ch1 = this.channel.read();
        int ch2 = this.channel.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    @Override
    public int readUnsignedShort() throws IOException {
        final int ch1 = this.channel.read();
        final int ch2 = this.channel.read();

        if ((ch1 | ch2) < 0) {
            throw new EOFException();
        }
        return (ch1 << 8) + (ch2 << 0);
    }

    @Override
    public char readChar() throws IOException {
        int ch1 = this.channel.read();
        int ch2 = this.channel.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

    @Override
    public int readInt() throws IOException {
        int ch1 = this.channel.read();
        int ch2 = this.channel.read();
        int ch3 = this.channel.read();
        int ch4 = this.channel.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    @Override
    public long readLong() throws IOException {
        return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }
}
