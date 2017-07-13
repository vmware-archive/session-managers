import static cucumber.api.groovy.EN.*

When(~/^the tomcat instance is started$/) { ->
    sessionStore?.start()
    tomcat.start()
}

When(~/^the tomcat instance is restarted$/) { ->
    tomcat.stop()
    tomcat.start()
}
