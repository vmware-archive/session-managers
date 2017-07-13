import io.pivotal.appsuite.qa.Session

import static cucumber.api.groovy.EN.*

When(~/^a user starts a session$/) { ->
    session = new Session("http://localhost:${tomcat.httpPort}/")
}

Then(~/^the user session should survive$/) { ->
    assert session == new Session("http://localhost:${tomcat.httpPort}/")
}