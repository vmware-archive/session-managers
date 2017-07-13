package io.pivotal.appsuite.qa

import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.ExposedPort
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.api.model.Ports
import groovy.util.logging.Slf4j

@Slf4j
class RedisFixture extends SessionStoreFixture {

    static final def DEFAULT_PORT = 6379

    static final def REDIS_VERSION = new File(System.properties['io.pivotal.appsuite.qa.cucumber.redis.version'])

    final File path
    final int port
    final def password

    final Docker docker = new Docker()
    def containerId
    State state

    RedisFixture(Map args=[:], File path) {
        this.path = path
        this.port = args.port ?: DEFAULT_PORT
        this.password = args.password
        log.info "setting up redis"
        this.path.mkdirs()
        def redisImage = "redis:${REDIS_VERSION}"
        if (port)
            configFile << "port ${port}\n"
        if (password)
            configFile << "requirepass ${password}\n"
        configFile << "logfile /redis/redis.log\n"
        containerId = docker.createContainer(redisImage) { cmd ->
            // paths
            cmd.withBinds(Bind.parse("${path}:/redis"))
            // ports
            if (port) {
                Ports ports = new Ports()
                def exposed = ExposedPort.tcp(port)
                cmd.withPortBindings(new PortBinding(Ports.Binding.bindPort(port), exposed))
                cmd.withExposedPorts(exposed)
            }
            // command
            cmd.withCmd("redis-server", "/redis/redis.conf")
        }
        state = new StoppedState()
    }

    File getConfigFile() {
        new File(path, 'redis.conf')
    }

    def getPort() {
        port
    }

    @Override
    void tearDown() {
        log.info "tearing down redis"
        docker.destroyContainer(containerId)
    }

    @Override
    synchronized void start() {
        log.info "starting redis"
        state.start()
    }

    @Override
    synchronized void stop() {
        log.info "stopping redis"
        state.stop()
    }

    abstract class State {

        void start() {}

        void stop() {}
    }

    class StoppedState extends State {

        void start() {
            docker.startContainer(containerId)
            state = new StartedState()
        }

    }

    class StartedState extends State {

        void stop() {
            docker.stopContainer(containerId)
            state = new StoppedState()
        }

    }

}
