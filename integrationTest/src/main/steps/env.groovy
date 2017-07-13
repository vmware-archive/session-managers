import cucumber.api.Scenario
import io.pivotal.appsuite.qa.Context
import io.pivotal.appsuite.qa.TomcatFixture

import static cucumber.api.groovy.Hooks.*

World {
    new Context()
}

Before() { Scenario scenario ->
    Context.log.info "scenario: ${scenario.name}"
    String sandboxes = System.properties['io.pivotal.appsuite.qa.cucumber.sandboxes']
    String sandboxName = scenario.name
            .replace('"', '')
            .replace('-', '')
            .split('\\s')
            .collect { it.capitalize() }
            .join('')
    sandbox = new File(sandboxes, sandboxName)
    Context.log.info "using sandbox $sandbox"
    if (sandbox.exists()) {
        sandbox.deleteDir()
    }
    sandbox.mkdirs()
    tomcat = new TomcatFixture(new File(sandbox, 'tomcat'))
}

After() { Scenario scenario ->
    tomcat?.stop()
    sessionStore?.stop()
    tomcat?.tearDown()
    sessionStore?.tearDown()
}
