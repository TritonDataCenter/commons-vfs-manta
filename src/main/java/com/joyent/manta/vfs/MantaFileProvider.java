package com.joyent.manta.vfs;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

/**
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class MantaFileProvider extends AbstractOriginatingFileProvider {
    /**
     * Collection of the capabilities available to the Manta VFS provider.
     */
    public static final Collection<Capability> CAPABILITIES = unmodifiableCollection(asList(
            Capability.ATTRIBUTES,
            Capability.CREATE,
            Capability.DELETE,
            Capability.GET_TYPE,
            Capability.GET_LAST_MODIFIED,
            Capability.LIST_CHILDREN,
            Capability.READ_CONTENT,
            Capability.RENAME,
            Capability.RANDOM_ACCESS_READ,
            Capability.URI,
            Capability.WRITE_CONTENT
    ));

    /**
     * Creates a new instance backed by the {@link MantaFileNameParser}.
     */
    public MantaFileProvider() {
        setFileNameParser(new MantaFileNameParser());
    }

    @Override
    protected FileSystem doCreateFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        final MantaFileSystem fs = new MantaFileSystem(rootName, fileSystemOptions);

        return fs;
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return CAPABILITIES;
    }
}
