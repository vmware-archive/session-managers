# Pivotal Session Managers: redis-store SSL development

This document provides instructions for setting up an environment for developing `redis-store` SSL support.

## Overview

In this setup, the JVM (Tomcat) is the SSL client and Redis is the SSL server.  Both endpoints are configured to use a test certificate.

For the JVM endpoint, the JVM system property `javax.net.ssl.trustStore` is set the keystore containing the test certificate.

The Redis endpoint is a little more complex as it itself does not provide an SSL endpoint, but instead [recommends][r] using an SSL tunnel.  In this development environment, the utility `stunnel` is used to create the SSL tunnel to a Redis service.  The `stunnel` endpoint is configured with the test certificate and its private key.

Once the endpoints are configured, the final step is to configured the Tomcat instance's `redis-store`.

The procedures in this document require 3 tools:

* `keytool`
* `openssl`
* `stunnel`

`keytool` is provided with your JDK/JRE.

`openssl` and `stunnel` are generally available from OS distribution package maintainers.  Support on Windows is beyond the scope of this document, however binaries for both `openssl` and `stunnel` are available.

## Before You Start

The CLI exmaples in this document can be copy/pasted if you first set some environment variables.  This step is optional, just be aware the example commands will need to be modified to your environment before running.

```sh
% NAME=redis                        # aritrary name, used as the base file name for generated SSL artifacts
% INSTANCE_DIR=/path/to/instance    # set this to the path to the Tomcat instance
```

## Creating a Test Certificate

The general steps used:

* create a Java keystore with a test certificate
* extract test certifcate and private key from Java keystore

The specifics of the above steps are not too complicated, but they require specifying a lot of parameters.  A utility, [`genssl`](genssl), is provided in this directory which runs `keytool` and `openssl` commands to generate the needed SSL artifacts.  `genssl` is a Bash script and can be easily viewed to get a specific understanding of the command invocations.

The general steps for `genssl`:

* run `keystore` to create a Java keystore with a test certificate
* run `keystore` to extract the test certifcate
* run `keystore` and `openssl` to extract and reformat the test certifacate private key

`genssl` takes a name as an argument that it uses as the base file name for the artifacts it generates.

Run `genssl` to generate the SSL artifacts:

```sh
% ./genssl $NAME
--- creating Java keystore
--- extracting certificate from Java keystore
Certificate stored in file <redis.crt>
--- extracting certificate private key from Java keystore
Entry for alias redis successfully imported.
Import command completed:  1 entries successfully imported, 0 entries failed or cancelled
MAC verified OK
--- creating key/cert PEM

  keystore          redis.jks               # JVM endpoint (set javax.net.ssl.trustStore to this)
  certificate       redis.crt               # certficate extracted from keystore
  key               redis.key               # certifacte private key extracted from keystore
  certificate/key   redis.pem               # remote endpoint (per your endpoint documentation)
```

Next steps:

* configure the JVM endpoint
* configure the Redis endpoint


## Configure the JVM Endpoint

Set the JVM system property `javax.net.ssl.trustStore` using the instance's `setenv.sh`:

```sh
% cat << EOF >> $INSTANCE_DIR/bin/setenv.sh
JAVA_OPTS="-Djavax.net.ssl.trustStore=$(pwd)/$NAME.jks"
EOF
```


## Configure the Redis Endpoint

In this example, `stunnel` is used to create a local SSL tunnel to a locally running Redis service.
The local Redis is service is listening on port `6379` and the tunnel will listen on port `6390`.

Create an `stunnel` configuration:

```sh
% cat << EOF >! stunnel.conf
foreground = yes
[redis]
client = no
cert = $NAME.pem
accept = 127.0.0.1:6390
connect = 6379
EOF
% chmod go-rw stunnel.conf      # stunnel may complain if permissions lax
```

Start `stunnel`:

```sh
% stunnel stunnel.conf
  2017.03.15 16:34:27 LOG5[ui]: stunnel 5.40 on ...
  2017.03.15 16:34:27 LOG5[ui]: Compiled/running with OpenSSL ...
  2017.03.15 16:34:27 LOG5[ui]: Threading:PTHREAD Sockets:POLL,IPv6 TLS:ENGINE,FIPS,OCSP,PSK,SNI
  2017.03.15 16:34:27 LOG5[ui]: Reading configuration from file ...stunnel.conf
  2017.03.15 16:34:27 LOG5[ui]: UTF-8 byte order mark not detected
  2017.03.15 16:34:27 LOG5[ui]: FIPS mode disabled
  2017.03.15 16:34:27 LOG5[ui]: Configuration successful
```


## Configure the Tomcat Instance's `redis-store`

Configure the Tomcat instance's `context.xml` per the [`redis-store`](../../README.md) documentation.

[r]: https://redis.io/topics/encryption
