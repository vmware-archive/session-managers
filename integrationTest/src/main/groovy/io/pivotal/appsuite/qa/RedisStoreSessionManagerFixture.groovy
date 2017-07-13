package io.pivotal.appsuite.qa

import groovy.util.logging.Slf4j
import groovy.xml.XmlUtil

@Slf4j
class RedisStoreSessionManagerFixture extends SessionManagerFixture {

    RedisStoreSessionManagerFixture(TomcatFixture tomcat) {
        super('redis-store', tomcat)
        //config
        def contextXml = new XmlSlurper().parse(tomcat.contextFile)
        contextXml.appendNode {
            Valve(className: 'com.gopivotal.manager.SessionFlushValve')
        }
        contextXml.appendNode {
            Manager(className: 'org.apache.catalina.session.PersistentManager') {
                Store(className: 'com.gopivotal.manager.redis.RedisStore')
            }
        }
        tomcat.contextFile.withWriter { out ->
            XmlUtil.serialize(contextXml, out)
        }
    }

    def getAttribute(def name) {
        def contextXml = new XmlSlurper().parse(tomcat.contextFile)
        def storeNode = contextXml.Manager.Store.find { it.@className == 'com.gopivotal.manager.redis.RedisStore' }
        assert storeNode
        return storeNode["@${name}"]
    }

    void setAttribute(def name, def value) {
        log.info "setting ${name} -> ${value}"
        def contextXml = new XmlSlurper().parse(tomcat.contextFile)
        def storeNode = contextXml.Manager.Store.find { it.@className == 'com.gopivotal.manager.redis.RedisStore' }
        assert storeNode
        storeNode["@${name}"] = value as String
        tomcat.contextFile.withWriter { out ->
            XmlUtil.serialize(contextXml, out)
        }
    }

    void setHost(def host) {
        setAttribute('host', host)
    }

    def getHost() {
        getAttribute('host')
    }

    void setPort(def port) {
        setAttribute('port', port)
    }

    def getPort() {
        getAttribute('port')
    }

    void setDatabase(def database) {
        setAttribute('database', database)
    }

    void setPassword(def password) {
        setAttribute('password', password)
    }

    def getPassword() {
        getAttribute('password')
    }

    def getDatabase() {
        getAttribute('database')
    }

    void setTimeout(def timeout) {
        setAttribute('timeout', timeout)
    }

    def getTimeout() {
        getAttribute('timeout')
    }

    void setConnectionPoolSize(def size) {
        setAttribute('connectionPoolSize', size)
    }

    def getConnectionPoolSize() {
        getAttribute('connectionPoolSize')
    }

    void setUri(def uri) {
        setAttribute('uri', uri)
    }

    def getUri() {
        getAttribute('uri')
    }

}
