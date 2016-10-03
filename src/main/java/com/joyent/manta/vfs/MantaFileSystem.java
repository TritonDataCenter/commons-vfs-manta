package com.joyent.manta.vfs;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.config.ConfigContext;
import com.joyent.manta.config.SystemSettingsConfigContext;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;

/**
 * Manta specific implementation of {@link org.apache.commons.vfs2.FileSystem}.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class MantaFileSystem extends AbstractFileSystem {
    /**
     * Default parent layer for file system is null.
     */
    private static final FileObject DEFAULT_PARENT_LAYER = null;

    /**
     * Utility instance of config builder used to convert between configuration
     * formats.
     */
    private static final MantaFileSystemConfigBuilder CONFIG_BUILDER =
            new MantaFileSystemConfigBuilder();

    /**
     * Reference to Manta driver configuration object.
     */
    private final ConfigContext mantaConfig;

    /**
     * Reference to MAnta driver object.
     */
    private final MantaClient client;

    /**
     * Creates a new instance based on the root name and Manta configuration object.
     *
     * @param rootName Root file in the filesystem
     * @param config Manta configuration object
     */
    public MantaFileSystem(final FileName rootName,
                           final ConfigContext config) {
        super(rootName, DEFAULT_PARENT_LAYER,
                CONFIG_BUILDER.importContext(config, new FileSystemOptions()));
        this.mantaConfig = config;
        this.client = createClient(this.mantaConfig);
    }

    /**
     * Creates a new instance based on the root name and VFS configuration object.
     *
     * @param rootName Root file in the filesystem
     * @param fileSystemOptions VFS configuration object
     */
    public MantaFileSystem(final FileName rootName,
                           final FileSystemOptions fileSystemOptions) {
        super(rootName, DEFAULT_PARENT_LAYER, fileSystemOptions);

        /* When there was no file system options (native VFS configuration), then
         * we default to the Manta system settings based configuration (environment
         * variables, JVM system properties, etc). */
        if (fileSystemOptions == null) {
            this.mantaConfig = new SystemSettingsConfigContext();
        } else {
            this.mantaConfig = CONFIG_BUILDER.exportContext(fileSystemOptions);
        }

        this.client = createClient(this.mantaConfig);
    }

    /**
     * Creates new instance of the Manta driver object based off of
     * the passed Manta configuration object.
     *
     * @param config Manta configuration object
     * @return Manta driver instance
     */
    protected MantaClient createClient(final ConfigContext config) {
        try {
            return new MantaClient(config);
        } catch (IOException e) {
            final String msg = "Error creating Manta client";
            throw new UncheckedIOException(msg, e);
        }
    }

    @Override
    protected FileObject createFile(final AbstractFileName name) throws Exception {
        return new MantaFileObject(name, this);
    }

    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(MantaFileProvider.CAPABILITIES);
    }

    @Override
    protected void doCloseCommunicationLink() {
        client.closeWithWarning();
    }

    @Override
    public void close() {
        super.close();

        if (this.client != null) {
            this.client.closeWithWarning();
        }
    }

    /**
     * Gets the backing Manta driver object.
     *
     * @return Manta driver object
     */
    public MantaClient getClient() {
        return client;
    }

    /**
     * Gets the backing Manta configuration object.
     *
     * @return Manta configuration object
     */
    public ConfigContext getMantaConfig() {
        return mantaConfig;
    }
}
