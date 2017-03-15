# Pivotal Session Managers
This project contains implementations of the [Tomcat `PersistentManager` Store][m].

| Implementation | Description |
| --- | --- |
| [redis-store](redis-store) | Redis store backend |

## Contributing
[Pull requests][p] are welcome. See the [contributor guidelines][c] for details.

## Builds
Each branch, release, and pull request kicks off builds on [Travis CI](https://travis-ci.org/pivotalsoftware/session-managers)

## Logging
This project uses [SLF4J][s] and defaults to Java Utils Logging (JUL) binding

In case you want to use another one you must to package your binder and explicit remove the JUL dependency:

```xml
            <dependency>
                <groupId>com.gopivotal.manager</groupId>
                <artifactId>session-managers</artifactId>
                <version>${session-managers.version}</version>
                <exclusions>
                    <exclusion>
                        <groupId>org.slf4j</groupId>
                        <artifactId>slf4j-jdk14</artifactId>
                    </exclusion>
                </exclusions>
            </dependency>
```

## License
This project is released under the [Apache License, Version 2.0][a].

[a]: http://www.apache.org/licenses/LICENSE-2.0
[c]: CONTRIBUTING.md
[m]: http://tomcat.apache.org/tomcat-8.5-doc/config/manager.html
[p]: https://help.github.com/categories/collaborating-with-issues-and-pull-requests/
[s]: https://www.slf4j.org/manual.html
