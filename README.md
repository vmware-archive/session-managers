# Pivotal Session Managers
This project contains implementations of the [Tomcat `PersistentManager` Store][m].

## `RedisStore`
This implementation of the Tomcat `PersistentManager` `Store` persists data to [Redis][r].  Sessions are serialized from their Java representation and the resulting `byte[]` is stored, keyed by the `Session` id (i.e. `JSESSIONID`).

- v1.2.0.RELEASE - works with both Tomcat 7 and 8, and with Java 6, 7 and 8.
- v1.3.x.RELEASE - works with Tomcat 8.5, and with Java 7 and 8.

### Usage
To use the Store, edit either the Tomcat instance's or application's `context.xml`, adding the following, `<Valve />`, `<Manager />`, and `<Store />` definitions.

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

### Configuration
The Store has a number of configuration elements that dictate how the Redis connection will be created.

| Key | Default | Description
| --- | ------- | -----------
| `connectionPoolSize` | `-1` | The maximum number of concurrent connections
| `database` | `0` | The database to connect to
| `host` | `localhost` | The host to connect to
| `password` | `<none>` | The password to use to `AUTH`
| `port` | `6379` | The port to connect to
| `timeout` | `2000` | The connection timeout in milliseconds
| `uri` | `<none>` | A URI-style representation of the connection details, e.g. `redis://username:password@localhost:6370/0`

### Binaries
Version 1.3.x+ are available at:

* [Spring Artifactory Repository](https://repo.spring.io) 
   * group "com.gopivotal.manager"
   * name "redis-store" 
   * version "`<version>`"

Version 1.2.0 is available at:

* Maven repo 
  * Repo URL: http://maven.gopivotal.com.s3.amazonaws.com/release/ 
     * group "com.gopivotal.manager"
     * name "redis-store" 
     * version "1.2.0.RELEASE"

## Contributing
[Pull requests][p] are welcome; see the [contributor guidelines][c] for details.

## Builds
Each branch, release, and pull request kicks off builds on [Travis CI](https://travis-ci.org/pivotalsoftware/session-managers)

## License
This project is released under version 2.0 of the [Apache License][a].

[a]: http://www.apache.org/licenses/LICENSE-2.0
[c]: CONTRIBUTING.md
[m]: http://tomcat.apache.org/tomcat-8.5-doc/config/manager.html
[p]: https://help.github.com/categories/collaborating-with-issues-and-pull-requests/
[r]: http://redis.io
