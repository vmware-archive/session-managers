package io.pivotal.appsuite.qa

import groovy.util.logging.Slf4j

@Slf4j
class Context {

    File sandbox
    TomcatFixture tomcat
    SessionStoreFixture sessionStore
    SessionManagerFixture sessionManager
    Session session

}
