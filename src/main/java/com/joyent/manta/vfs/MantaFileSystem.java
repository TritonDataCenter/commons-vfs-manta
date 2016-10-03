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
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 */
public class MantaFileSystem extends AbstractFileSystem {
    private static final FileObject DEFAULT_PARENT_LAYER = null;
    private static final MantaFileSystemConfigBuilder configBuilder =
            new MantaFileSystemConfigBuilder();

    protected final ConfigContext mantaConfig;
    protected final MantaClient client;

    public MantaFileSystem(final FileName rootName,
                           final ConfigContext config) {
        super(rootName, DEFAULT_PARENT_LAYER,
                configBuilder.importContext(config, new FileSystemOptions()));
        this.mantaConfig = config;
        this.client = createClient(this.mantaConfig);
    }

    public MantaFileSystem(final FileName rootName,
                           final FileSystemOptions fileSystemOptions) {
        super(rootName, DEFAULT_PARENT_LAYER, fileSystemOptions);

        /* When there was no file system options (native VFS configuration), then
         * we default to the Manta system settings based configuration (environment
         * variables, JVM system properties, etc). */
        if (fileSystemOptions == null) {
            this.mantaConfig = new SystemSettingsConfigContext();
        } else {
            this.mantaConfig = configBuilder.exportContext(fileSystemOptions);
        }

        this.client = createClient(this.mantaConfig);
    }

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

    public MantaClient getClient() {
        return client;
    }

    public ConfigContext getMantaConfig() {
        return mantaConfig;
    }

    @Override
    public void close() {
        super.close();
    }
}
