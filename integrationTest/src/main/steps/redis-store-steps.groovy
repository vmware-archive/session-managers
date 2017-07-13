import io.pivotal.appsuite.qa.RedisFixture

import static cucumber.api.groovy.EN.*

Given(~/^a redis instance$/) { ->
    sessionStore = new RedisFixture(new File(sandbox, 'redis'))
}

Given(~/^a redis instance with a custom port$/) { ->
    sessionStore = new RedisFixture(new File(sandbox, 'redis'), port:9999)
}

Given(~/^a redis instance with a password$/) { ->
    sessionStore = new RedisFixture(new File(sandbox, 'redis'), password:'mysecret')
}
