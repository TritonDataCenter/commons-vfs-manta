package com.joyent.manta.vfs;

import com.joyent.manta.org.apache.commons.lang3.Validate;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;

/**
 * Manta specific implementation of {@link AbstractFileName}.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class MantaFileName extends AbstractFileName {
    /**
     * Hardcoded name of scheme that is made available when getScheme can't be accessed.
     */
    public static final String SCHEME = "manta";

    /**
     * Creates a new instance with the specified path and file type.
     *
     * @param absPath absolute path to file on Manta
     * @param type type of file (e.g. directory or file)
     */
    public MantaFileName(final String absPath, final FileType type) {
        this(SCHEME, absPath, type);
    }

    /**
     * Creates a new instance with the specified scheme, path and file type.
     *
     * @param scheme scheme for this file - this should always be "manta"
     * @param absPath absolute path to file on Manta
     * @param type type of file (e.g. directory or file)
     */
    public MantaFileName(final String scheme, final String absPath, final FileType type) {
        super(scheme, absPath, type);
    }

    @Override
    public FileName createName(final String absPath, final FileType type) {
        Validate.notBlank(absPath, "File path must not be blank or null");
        Validate.notNull(type, "File type must be specified");

        return new MantaFileName(getScheme(), absPath, type);
    }

    @Override
    protected void appendRootUri(final StringBuilder buffer, final boolean addPassword) {
        buffer.append(getScheme());
        buffer.append("://");
    }
}
