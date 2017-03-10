# Pivotal Session Managers: redis-store
This sub-project contains the `redis-store` implementation, a [Redis][r]-backended Tomcat `PersistentManager`.

The implementation serializes sessions from their Java representation, storing the resulting `byte[]` in the Redis store, keyed by the `Session` id (i.e. `JSESSIONID`).

## Support Matrix

Supported Tomcat and Java versions:

| Version | Tomcat | Java |
| --- | --- | --- |
| 1.3.x.RELEASE | 8.5 | 8, 7 |
| 1.2.x.RELEASE | 8.0, 7.0 | 8, 7, 6 |

## Usage

* Obtain or build a `redis-store` jar
* Place the jar in the classpath for your Tomcat instance, e.g. the instance's `lib` directory
* Configure the Tomcat instance to use `redis-store`
* Configure `redis-store`

## Downloads

### Maven/Gradle

| Version 1.3.x+ | |
| --- | --- |
| repository | `https://repo.spring.io/release/` |
| group | `com.gopivotal.manager` |
| name | `redis-store` |
| version | `1.3.1.RELEASE` _(as an example)_ |


| Version 1.2.0 | |
| --- | --- |
| repository | `http://maven.gopivotal.com.s3.amazonaws.com/release/` |
| group | `com.gopivotal.manager` |
| name | `redis-store` |
| version | `1.2.0.RELEASE` |

### Links

* [1.3.1.RELEASE](https://repo.spring.io/libs-release-local/com/gopivotal/manager/redis-store/1.3.1.RELEASE/redis-store-1.3.1.RELEASE.jar)
* [1.3.0.RELEASE](https://repo.spring.io/libs-release-local/com/gopivotal/manager/redis-store/1.3.0.RELEASE/redis-store-1.3.0.RELEASE.jar)
* [1.2.0.RELEASE](http://maven.gopivotal.com.s3.amazonaws.com/release/com/gopivotal/manager/redis-store/1.2.0.RELEASE/redis-store-1.2.0.RELEASE.jar)

## Building from Source

```sh
# clone repository
% git clone https://github.com/pivotalsoftware/session-managers
% cd session-managers

# build
% mvn package

# redis-store jar will be in redis-store/target
% ls -1 redis-store/target/*jar
redis-store/target/redis-store-1.3.2.BUILD-SNAPSHOT-sources.jar
redis-store/target/redis-store-1.3.2.BUILD-SNAPSHOT.jar
```

## Configuring Tomcat
To use the Store, edit either the Tomcat instance's or application's `context.xml`, adding the following `<Valve />` and `<Manager />` definitions:

```xml
<Context>
  ...
  <Valve className="com.gopivotal.manager.SessionFlushValve" />
  <Manager className="org.apache.catalina.session.PersistentManager">
    <Store className="com.gopivotal.manager.redis.RedisStore" />
  </Manager>
  ...
</Context>
```

## Configuring `redis-store`
Configure `redis-store` using `<Store />` attributes:

| Attribute | Default | Description
| --- | ------- | -----------
| `connectionPoolSize` | `-1` | Maximum number of concurrent connections
| `database` | `0` | Redis database
| `host` | `localhost` | Redis host
| `password` | `<none>` | Redis AUTH password
| `port` | `6379` | Redis port
| `timeout` | `2000` | Connection timeout (in milliseconds)
| `uri` | `<none>` | Connection URI, e.g. `redis://username:password@localhost:6370/0`

Example: set the maximum number of concurrent connections to 20:
```xml
<Context>
  ...
    <Store
      className="com.gopivotal.manager.redis.RedisStore"
      connectionPoolSize="20"
    />
  ...
</Context>
```

[r]: http://redis.io
