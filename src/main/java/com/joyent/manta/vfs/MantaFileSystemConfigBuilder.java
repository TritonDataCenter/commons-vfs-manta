package com.joyent.manta.vfs;

import com.joyent.manta.config.ChainedConfigContext;
import com.joyent.manta.config.ConfigContext;
import com.joyent.manta.config.DefaultsConfigContext;
import com.joyent.manta.config.MapConfigContext;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

import java.util.HashMap;
import java.util.Map;

import static com.joyent.manta.config.MapConfigContext.*;

/**
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 */
public class MantaFileSystemConfigBuilder extends FileSystemConfigBuilder {
    public MantaFileSystemConfigBuilder() {
    }

    /**
     * Sets the Manta service endpoint.
     * @param opts file system options object to populate with config
     * @param mantaURL Manta service endpoint
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setMantaURL(final FileSystemOptions opts,
                                                    final String mantaURL) {
        if (mantaURL != null) {
            setParam(opts, MANTA_URL_KEY, mantaURL);
        }

        return this;
    }

    /**
     * Sets the account associated with the Manta service.
     * @param opts file system options object to populate with config
     * @param mantaUser Manta user account
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setMantaUser(final FileSystemOptions opts,
                                                     final String mantaUser) {
        if (mantaUser != null) {
            setParam(opts, MANTA_USER_KEY, mantaUser);
        }

        return this;
    }

    /**
     * Sets the RSA key fingerprint of the private key used to access Manta.
     * @param opts file system options object to populate with config
     * @param mantaKeyId RSA key fingerprint
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setMantaKeyId(final FileSystemOptions opts,
                                                      final String mantaKeyId) {
        if (mantaKeyId != null) {
            setParam(opts, MANTA_KEY_ID_KEY, mantaKeyId);
        }

        return this;
    }

    /**
     * Sets the path on the filesystem to the private RSA key used to access Manta.
     * @param opts file system options object to populate with config
     * @param mantaKeyPath path on the filesystem
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setMantaKeyPath(final FileSystemOptions opts,
                                                        final String mantaKeyPath) {
        if (hasParam(opts, MANTA_PRIVATE_KEY_CONTENT_KEY) && mantaKeyPath != null) {
            String msg = "You can't set both a private key path and private key content";
            throw new IllegalArgumentException(msg);
        }

        if (mantaKeyPath != null) {
            setParam(opts, MANTA_KEY_PATH_KEY, mantaKeyPath);
        }

        return this;
    }

    /**
     * Sets the general connection timeout for the Manta service.
     * @param opts file system options object to populate with config
     * @param timeout timeout in milliseconds
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setTimeout(final FileSystemOptions opts,
                                                   final Integer timeout) {
        if (timeout != null) {
            setParam(opts, MANTA_TIMEOUT_KEY, timeout);
        }

        return this;
    }

    /**
     * Sets the number of times to retry failed HTTP requests.
     * @param opts file system options object to populate with config
     * @param retries number of times to retry
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setRetries(final FileSystemOptions opts,
                                                   final Integer retries) {
        if (retries != null) {
            if (retries < 0) {
                throw new IllegalArgumentException("Retries must be zero or greater");
            }
            setParam(opts, MANTA_RETRIES_KEY, retries);
        }

        return this;
    }

    /**
     * Sets the maximum number of open connections to the Manta API.
     * @param opts file system options object to populate with config
     * @param maxConns number of connections greater than zero
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setMaximumConnections(final FileSystemOptions opts,
                                                              final Integer maxConns) {
        if (maxConns != null) {
            if (maxConns < 1) {
                throw new IllegalArgumentException("Maximum number of connections must "
                        + "be 1 or greater");
            }
            setParam(opts, MANTA_MAX_CONNS_KEY, maxConns);
        }

        return this;
    }

    /**
     * Sets the private key content used to authenticate. This can't be set if
     * you already have a private key path specified.
     * @param opts file system options object to populate with config
     * @param privateKeyContent contents of private key in plain text
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setPrivateKeyContent(final FileSystemOptions opts,
                                                             final String privateKeyContent) {
        if (hasParam(opts, MANTA_KEY_PATH_KEY) && privateKeyContent != null) {
            String msg = "You can't set both a private key path and private key content";
            throw new IllegalArgumentException(msg);
        }

        if (privateKeyContent != null) {
            setParam(opts, MANTA_PRIVATE_KEY_CONTENT_KEY, privateKeyContent);
        }

        return this;
    }

    /**
     * Sets the password used for the private key. This is optional and not
     * typically used.
     * @param opts file system options object to populate with config
     * @param password password to set
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setPassword(final FileSystemOptions opts,
                                                    final String password) {
        if (password != null) {
            setParam(opts, MANTA_PASSWORD_KEY, password);
        }

        return this;
    }

    /**
     * Sets the class name of the HttpTransport implementation to use. Use the
     * strings ApacheHttpTransport, NetHttpTransport or MockHttpTransport to use the
     * included implementations. If the value is not one of those three - then we
     * default to the ApacheHttpTransport method.
     *
     * @param opts file system options object to populate with config
     * @param httpTransport Typically 'ApacheHttpTransport' or 'NetHttpTransport'
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setHttpTransport(final FileSystemOptions opts,
                                                         final String httpTransport) {
        if (httpTransport != null) {
            setParam(opts, MANTA_HTTP_TRANSPORT_KEY, httpTransport);
        }

        return this;
    }

    /**
     * Set the supported TLS protocols.
     *
     * @param opts file system options object to populate with config
     * @param httpsProtocols comma delimited list of TLS protocols
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setHttpsProtocols(final FileSystemOptions opts,
                                                          final String httpsProtocols) {
        if (httpsProtocols != null) {
            setParam(opts, MANTA_HTTPS_PROTOCOLS_KEY, httpsProtocols);
        }

        return this;
    }

    /**
     * Set the supported TLS ciphers.
     *
     * @param opts file system options object to populate with config
     * @param httpsCiphers comma delimited list of TLS ciphers
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setHttpsCiphers(final FileSystemOptions opts,
                                                        final String httpsCiphers) {
        if (httpsCiphers != null) {
            setParam(opts, MANTA_HTTPS_CIPHERS_KEY, httpsCiphers);
        }

        return this;
    }

    /**
     * Change the state of whether or not HTTP signatures are sent to the Manta API.
     *
     * @param opts file system options object to populate with config
     * @param noAuth true to disable HTTP signatures
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setNoAuth(final FileSystemOptions opts,
                                                  final Boolean noAuth) {
        if (noAuth != null) {
            setParam(opts, MapConfigContext.MANTA_NO_AUTH_KEY, noAuth);
        }

        return this;
    }

    /**
     * Change the state of whether or not HTTP signatures are using native code
     * to generate the cryptographic signatures.
     *
     * @param opts file system options object to populate with config
     * @param disableNativeSignatures true to disable
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setDisableNativeSignatures(final FileSystemOptions opts,
                                                                   final Boolean disableNativeSignatures) {
        if (disableNativeSignatures != null) {
            setParam(opts, MANTA_NO_NATIVE_SIGS_KEY, disableNativeSignatures);
        }

        return this;
    }

    /**
     * Sets the time in milliseconds to cache HTTP signature headers.
     *
     * @param opts file system options object to populate with config
     * @param signatureCacheTTL time in milliseconds to cache HTTP signature headers
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setSignatureCacheTTL(final FileSystemOptions opts,
                                                             final Integer signatureCacheTTL) {
        if (signatureCacheTTL != null) {
            setParam(opts, MANTA_SIGS_CACHE_TTL_KEY, signatureCacheTTL);
        }
        return this;
    }



    /**
     * Imports the provided {@link ConfigContext} object into the config builder's
     * settings.
     *
     * @param opts file system options object to populate with config
     * @param config Manta configuration context to import
     * @return reference to current instance
     */
    public FileSystemOptions importContext(final ConfigContext config,
                                           final FileSystemOptions opts) {
        setMantaURL(opts, config.getMantaURL());
        setMantaUser(opts, config.getMantaUser());
        setMantaKeyPath(opts, config.getMantaKeyPath());
        setTimeout(opts, config.getTimeout());
        setRetries(opts, config.getRetries());
        setMaximumConnections(opts, config.getMaximumConnections());
        setPrivateKeyContent(opts, config.getPrivateKeyContent());
        setPassword(opts, config.getPassword());
        setHttpTransport(opts, config.getHttpTransport());
        setHttpsProtocols(opts, config.getHttpsProtocols());
        setHttpsCiphers(opts, config.getHttpsCipherSuites());
        setNoAuth(opts, config.noAuth());
        setDisableNativeSignatures(opts, config.disableNativeSignatures());
        setSignatureCacheTTL(opts, config.getSignatureCacheTTL());

        return opts;
    }

    public ConfigContext exportContext(final FileSystemOptions opts) {
        final Map<String, Object> properties = new HashMap<>(ALL_PROPERTIES.length);

        for (String key : MapConfigContext.ALL_PROPERTIES) {
            final Object value = getParam(opts, key);

            if (value == null) {
                continue;
            }

            properties.put(key, value);
        }

        final ChainedConfigContext config = new ChainedConfigContext(
                new DefaultsConfigContext(),
                new MapConfigContext(properties)
        );

        return config;
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return MantaFileSystem.class;
    }
}
