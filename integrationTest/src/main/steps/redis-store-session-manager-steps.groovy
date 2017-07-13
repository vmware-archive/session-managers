import io.pivotal.appsuite.qa.RedisStoreSessionManagerFixture

import static cucumber.api.groovy.EN.*

Given(~/^a redis-store session manager$/) { ->
    sessionManager = new RedisStoreSessionManagerFixture(tomcat)
}

Given(~/^the redis-store session manager is configured with a custom host$/) { ->
    sessionManager.host = 'custom.host'
}

Given(~/^the redis-store session manager is configured with an unreachable host$/) { ->
    sessionManager.host = 'unreachable.host'
}

Given(~/^the redis-store session manager is configured with a custom port$/) { ->
    sessionManager.port = 1234
}

Given(~/^the redis-store session manager is configured with an invalid port$/) { ->
    sessionManager.port = 'nosuchport'
}

Given(~/^the redis-store session manager is configured with the redis port$/) { ->
    sessionManager.port = sessionStore.port
}

Given(~/^the redis-store session manager is configured with a custom password$/) { ->
    sessionManager.password = 'mysecret'
}

Given(~/^the redis-store session manager is configured with the redis password$/) { ->
    sessionManager.password = sessionStore.password
}

Given(~/^the redis-store session manager is configured with a custom database$/) { ->
    sessionManager.database = 2345
}

Given(~/^the redis-store session manager is configured with an invalid database$/) { ->
    sessionManager.database = 'nosuchdatabase'
}

Given(~/^the redis-store session manager is configured with a custom connection pool size$/) { ->
    sessionManager.connectionPoolSize = 4567
}

Given(~/^the redis-store session manager is configured with an invalid connection pool size$/) { ->
    sessionManager.connectionPoolSize = 'nusuchpoolsize'
}

Given(~/^the redis-store session manager is configured with a custom timeout$/) { ->
    sessionManager.timeout = 3456
}

Given(~/^the redis-store session manager is configured with an invalid timeout$/) { ->
    sessionManager.timeout = 'nosuchtimeout'
}

Given(~/^the redis-store session manager is configured with a custom URI$/) { ->
    sessionManager.uri = 'redis://localhost:1111/1'
}

Then(~/^the redis-store session manager should log "about" information$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*Pivotal redis-store, ${sessionManager.version}.*/
}

Then(~/^the redis-store session manager should log a host configuration message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*setting host=${sessionManager.host}.*/
}

Then(~/^the redis-store session manager should log a port configuration message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*setting port=${sessionManager.port}.*/
}

Then(~/^the redis-store session manager should log a port configuration failure message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*Setting property 'port' to '${sessionManager.port}' did not find a matching property..*/
}

Then(~/^the redis-store session manager should log a password configuration message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*setting password=\*.*/
}

Then(~/^the redis-store session manager should log a database configuration message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*setting database=${sessionManager.database}.*/
}

Then(~/the redis-store session manager should log a database configuration failure message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*Setting property 'database' to '${sessionManager.database}' did not find a matching property..*/
}

Then(~/^the redis-store session manager should log a connection pool size configuration message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*setting connectionPoolSize=${sessionManager.connectionPoolSize}.*/
}

Then(~/^the redis-store session manager should log a connection pool size configuration failure message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*Setting property 'connectionPoolSize' to '${sessionManager.connectionPoolSize}' did not find a matching property..*/
}

Then(~/^the redis-store session manager should log a timeout configuration message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*setting timeout=${sessionManager.timeout}.*/
}

Then(~/^the redis-store session manager should log a timeout configuration failure message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*Setting property 'timeout' to '${sessionManager.timeout}' did not find a matching property..*/
}

Then(~/^the redis-store session manager should log a URI configuration message$/) { ->
    assert tomcat.logFile.text ==~ /(?s).*setting uri=${sessionManager.uri}.*/
}

Then(~/^the redis-store session manager (should|should not) log a connection failure message$/) { should ->
    assert (tomcat.logFile.text ==~ /(?s).*Jedis.*Exception: Could not get a resource from the pool.*/) == (should == 'should')
}
