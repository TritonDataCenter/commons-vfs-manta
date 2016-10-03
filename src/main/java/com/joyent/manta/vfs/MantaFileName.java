package com.joyent.manta.vfs;

import com.joyent.manta.org.apache.commons.lang3.Validate;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;

/**
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 */
public class MantaFileName extends AbstractFileName {
    public static final String SCHEME = "manta";

    public MantaFileName(final String absPath, final FileType type) {
        this(SCHEME, absPath, type);
    }

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
