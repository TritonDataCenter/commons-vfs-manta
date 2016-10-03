# Apache Commons VFS Manta Provider

## Introduction

This project provides a [VFS Provider](https://commons.apache.org/proper/commons-vfs/apidocs/org/apache/commons/vfs2/provider/FileProvider.html)
for the open source [Manta object store](https://github.com/joyent/manta). Unlike
other object stores, Manta is [strongly consistent](http://dtrace.org/blogs/dap/2013/07/03/fault-tolerance-in-manta/)
and uses a hierarchical file system (like a Unix filesystem) to organize file storage
whereas S3/Swift use a key/value system. This model aligns closely with the Apache Commons VFS
Filesystem model.

This project is licensed under the [Apache 2.0 license](LICENSE.txt).

## Capabilities
 * Configurable via native VFS configuration, environment variables or Java system properties.
 * VFS attributes / metadata is supported.
 * Create/delete/rename/copy/move/get are supported.
 * Directory listing is supported.
 * Random file reads are supported.
 * Public URIs via signed links or public URLs are supported.
 * Append is NOT supported.

## Run Requirements
 * Java 8
 * Java dependencies - see [pom.xml](pom.xml).
 * A running instance of the Manta object store ([available on the public cloud](https://www.joyent.com/object-storage))

## Build Requirements
 * Java 8
 * Maven 3.0+
 
## Configuration

You will need to have the public/private keys needed to access Manta on the machine
in which Hadoop is running. It is often best to verify that these keys are setup
correctly using the [Node.js Manta CLI](https://www.npmjs.com/package/manta).

Configuration will be done using the Hadoop configuration files or environment
variables. Refer to the table below for the available configuration options.

### Configuration Parameters

Configuration parameters take precedence from left to right - values on the
left are overridden by values on the right.

| Default                              | System Prop / VFS Prop    | Environment Variable      |
|--------------------------------------|---------------------------|---------------------------|
| https://us-east.manta.joyent.com:443 | manta.url                 | MANTA_URL                 |
|                                      | manta.user                | MANTA_USER                |
|                                      | manta.key_id              | MANTA_KEY_ID              |
| $HOME/.ssh/id_rsa                    | manta.key_path            | MANTA_KEY_PATH            |
|                                      | manta.key_content         | MANTA_KEY_CONTENT         |
|                                      | manta.password            | MANTA_PASSWORD            |
| 20000                                | manta.timeout             | MANTA_TIMEOUT             |
| 3                                    | manta.retries             | MANTA_HTTP_RETRIES        |
| 24                                   | manta.max_connections     | MANTA_MAX_CONNS           |
| ApacheHttpTransport                  | manta.http_transport      | MANTA_HTTP_TRANSPORT      |
| TLSv1.2                              | https.protocols           | MANTA_HTTPS_PROTOCOLS     |
| <value too big - see code>           | https.cipherSuites        | MANTA_HTTPS_CIPHERS       |
| false                                | manta.no_auth             | MANTA_NO_AUTH             |
| false                                | manta.disable_native_sigs | MANTA_NO_NATIVE_SIGS      |
| 0                                    | http.signature.cache.ttl  | MANTA_SIGS_CACHE_TTL      |

* `manta.url` ( **MANTA_URL** )
The URL of the manta service endpoint to test against
* `manta.user` ( **MANTA_USER** )
The account name used to access the manta service. If accessing via a [subuser](https://docs.joyent.com/public-cloud/rbac/users),
you will specify the account name as "user/subuser".
* `manta.key_id`: ( **MANTA_KEY_ID**)
The fingerprint for the public key used to access the manta service.
* `manta.key_path` ( **MANTA_KEY_PATH**)
The name of the file that will be loaded for the account used to access the manta service.
* `manta.key_content` ( **MANTA_KEY_CONTENT**)
The content of the private key as a string. This is an alternative to `manta.key_path`. Both
`manta.key_path` and can't be specified at the same time `manta.key_content`.
* `manta.password` ( **MANTA_PASSWORD**)
The password associated with the key specified. This is optional and not normally needed.
* `manta.timeout` ( **MANTA_TIMEOUT**)
The number of milliseconds to wait after a request was made to Manta before failing.
* `manta.retries` ( **MANTA_HTTP_RETRIES**)
The number of times to retry failed HTTP requests.
* `manta.max_connections` ( **MANTA_MAX_CONNS**)
The maximum number of open HTTP connections to the Manta API.
* `manta.http_transport` (**MANTA_HTTP_TRANSPORT**)
The HTTP transport library to use. Either the Apache HTTP Client (ApacheHttpTransport) or the native JDK HTTP library (NetHttpTransport).
* `https.protocols` (**MANTA_HTTPS_PROTOCOLS**)
A comma delimited list of TLS protocols.
* `https.cipherSuites` (**MANTA_HTTPS_CIPHERS**)
A comma delimited list of TLS cipher suites.
* `manta.no_auth` (**MANTA_NO_AUTH**)
When set to true, this disables HTTP Signature authentication entirely. This is
only really useful when you are running the library as part of a Manta job.
* `http.signature.native.rsa` (**MANTA_NO_NATIVE_SIGS**)
When set to true, this disables the use of native code libraries for cryptography.
* `http.signature.cache.ttl` (**MANTA_SIGS_CACHE_TTL**)
Time in milliseconds to cache the HTTP signature authorization header. A setting of
0ms disables the cache entirely.