package com.joyent.manta.vfs;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

/**
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 */
public class MantaFileProvider extends AbstractOriginatingFileProvider {
    public final static Collection<Capability> CAPABILITIES = unmodifiableCollection(asList(
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
