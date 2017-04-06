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
 * Manta specific implementation of a VFS configuration builder.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class MantaFileSystemConfigBuilder extends FileSystemConfigBuilder {
    /**
     * Create new instance.
     */
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
     * Set the size of buffer in bytes to use to buffer streams of HTTP data.
     *
     * @param opts file system options object to populate with config
     * @param bufferSize HTTP buffer size
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setHttpBufferSize(final FileSystemOptions opts,
                                                          final Integer bufferSize) {
        if (bufferSize != null) {
            setParam(opts, MANTA_HTTP_BUFFER_SIZE_KEY, bufferSize);
        }

        return this;
    }

    /**
     * Set the in milliseconds to wait to see if a TCP socket has timed out.
     *
     * @param opts file system options object to populate with config
     * @param timeout socket timeout time in milliseconds
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setTcpSocketTimeout(final FileSystemOptions opts,
                                                            final Integer timeout) {
        if (timeout != null) {
            setParam(opts, MANTA_TCP_SOCKET_TIMEOUT_KEY, timeout);
        }

        return this;
    }

    /**
     * When set to true when we verify the uploaded file's checksum against the
     * server's checksum (MD5).
     *
     * @param opts file system options object to populate with config
     * @param verifyEnabled true if we verify object checksums against the server
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setVerifyUploads(final FileSystemOptions opts,
                                                         final Boolean verifyEnabled) {
        if (verifyEnabled != null) {
            setParam(opts, MANTA_VERIFY_UPLOADS_KEY, verifyEnabled);
        }

        return this;
    }

    /**
     * Sets the number of bytes to read into memory for a streaming upload before
     * deciding if we want to load it in memory before send it.
     *
     * @param opts file system options object to populate with config
     * @param bufferSize number of bytes in the buffer
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setUploadBufferSize(final FileSystemOptions opts,
                                                            final Integer bufferSize) {
        if (bufferSize != null) {
            setParam(opts, MANTA_UPLOAD_BUFFER_SIZE_KEY, bufferSize);
        }

        return this;
    }

    /**
     * When set to true when client-side encryption is enabled.
     *
     * @param opts file system options object to populate with config
     * @param encryptionEnabled flag enabling client-side encryption
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setClientEncryptionEnabled(final FileSystemOptions opts,
                                                                   final Boolean encryptionEnabled) {
        if (encryptionEnabled != null) {
            setParam(opts, MANTA_CLIENT_ENCRYPTION_ENABLED_KEY, encryptionEnabled);
        }

        return this;
    }

    /**
     * Sets a plain-text identifier for the encryption key used. It doesn't
     * contain whitespace and is encoded in US-ASCII.
     *
     * @see ConfigContext#getEncryptionKeyId()
     * @param opts file system options object to populate with config
     * @param keyId the unique identifier of the key used for encryption
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setEncryptionKeyId(final FileSystemOptions opts,
                                                           final String keyId) {
        if (keyId != null) {
            setParam(opts, MANTA_ENCRYPTION_KEY_ID_KEY, keyId);
        }

        return this;
    }

    /**
     * Sets the algorithm name in the format of <code>cipher/mode/padding state</code>.
     *
     * @see com.joyent.manta.client.crypto.SupportedCiphersLookupMap
     * @param opts file system options object to populate with config
     * @param algorithm algorithm name
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setEncryptionAlgorithm(final FileSystemOptions opts,
                                                               final String algorithm) {
        if (algorithm != null) {
            setParam(opts, MANTA_ENCRYPTION_ALGORITHM_KEY, algorithm);
        }

        return this;
    }

    /**
     * When set to true, downloading unencrypted files is allowed in encryption
     * mode.
     *
     * @param opts file system options object to populate with config
     * @param permitUnencryptedDownloads flag enabling unencrypted downloads
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setPermitUnencryptedDownloads(final FileSystemOptions opts,
                                                                      final Boolean permitUnencryptedDownloads) {
        if (permitUnencryptedDownloads != null) {
            setParam(opts, MANTA_PERMIT_UNENCRYPTED_DOWNLOADS_KEY, permitUnencryptedDownloads);
        }

        return this;
    }

    /**
     * Sets the authentication mode to use when doing client-side encryption.
     *
     * @see com.joyent.manta.config.EncryptionAuthenticationMode
     * @param opts file system options object to populate with config
     * @param authMode specifies if we are in strict ciphertext authentication mode or not
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setEncryptionAuthMode(final FileSystemOptions opts,
                                                              final String authMode) {
        if (authMode != null) {
            setParam(opts, MANTA_ENCRYPTION_AUTHENTICATION_MODE_KEY, authMode);
        }

        return this;
    }

    /**
     * Sets the path to the private encryption key on the filesystem (can't
     * be used if private key bytes is not null).
     *
     * @param opts file system options object to populate with config
     * @param keyPath path to encryption key file
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setEncryptionKeyPath(final FileSystemOptions opts,
                                                             final String keyPath) {
        if (keyPath != null) {
            setParam(opts, MANTA_ENCRYPTION_PRIVATE_KEY_PATH_KEY, keyPath);
        }

        return this;
    }

    /**
     * Sets the private encryption key data (can't be used if private key path
     * is not null).
     *
     * @param opts file system options object to populate with config
     * @param keyBytes private key as a byte array
     * @return the current instance of {@link MantaFileSystemConfigBuilder}
     */
    public MantaFileSystemConfigBuilder setEncryptionKeyBytes(final FileSystemOptions opts,
                                                              final byte[] keyBytes) {
        if (keyBytes != null) {
            setParam(opts, MANTA_ENCRYPTION_PRIVATE_KEY_BYTES_KEY, keyBytes);
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
        setMantaKeyId(opts, config.getMantaKeyId());
        setMantaKeyPath(opts, config.getMantaKeyPath());
        setPrivateKeyContent(opts, config.getPrivateKeyContent());
        setPassword(opts, config.getPassword());
        setTimeout(opts, config.getTimeout());
        setRetries(opts, config.getRetries());
        setMaximumConnections(opts, config.getMaximumConnections());
        setHttpBufferSize(opts, config.getHttpBufferSize());
        setHttpsProtocols(opts, config.getHttpsProtocols());
        setHttpsCiphers(opts, config.getHttpsCipherSuites());
        setNoAuth(opts, config.noAuth());
        setDisableNativeSignatures(opts, config.disableNativeSignatures());
        setTcpSocketTimeout(opts, config.getTcpSocketTimeout());
        setVerifyUploads(opts, config.verifyUploads());
        setUploadBufferSize(opts, config.getUploadBufferSize());

        if (config.isClientEncryptionEnabled()) {
            setClientEncryptionEnabled(opts, config.isClientEncryptionEnabled());
            setEncryptionKeyId(opts, config.getEncryptionKeyId());
            setEncryptionAlgorithm(opts, config.getEncryptionAlgorithm());
            setPermitUnencryptedDownloads(opts, config.permitUnencryptedDownloads());
            setEncryptionAuthMode(opts, config.getEncryptionAuthenticationMode().toString());
            setEncryptionKeyPath(opts, config.getEncryptionPrivateKeyPath());
            setEncryptionKeyBytes(opts, config.getEncryptionPrivateKeyBytes());
        }

        return opts;
    }

    /**
     * Converts a VFS configuration to a Manta configuration object.
     * @param opts VFS options to be converted
     * @return a Manta configuration object populated with the VFS options
     */
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
