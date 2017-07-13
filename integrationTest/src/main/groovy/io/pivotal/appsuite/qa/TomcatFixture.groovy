package io.pivotal.appsuite.qa

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

@Slf4j
class TomcatFixture extends Fixture {

    final File dir
    final File distro = new File(System.properties['io.pivotal.appsuite.qa.cucumber.tomcat.distro'])
    final Controller = new Controller()
    final int port = 8080

    TomcatFixture(File dir) {
        log.info "setting up tomcat"
        this.dir = dir
        configDir.mkdirs()
        libDir.mkdirs()
        logDir.mkdirs()
        tempDir.mkdirs()
        applicationDir.mkdirs()
        FileUtils.copyDirectory(new File(distro, 'conf'), configDir)
        FileUtils.copyDirectory(new File(System.properties['io.pivotal.appsuite.qa.cucumber.webapp']), applicationDir)
    }

    @Override
    void tearDown() {
        log.info "tearing down tomcat"
    }

    @Override
    void start() {
        log.info "starting tomcat"
        controller.startInstance()
    }

    @Override
    void stop() {
        log.info "stopping tomcat"
        controller.stopInstance()
    }

    File resolve(String path) {
        new File(dir, path)
    }

    File getConfigDir() {
        resolve('conf')
    }

    File getLibDir() {
        resolve('lib')
    }

    File getLogDir() {
        resolve('logs')
    }

    File getApplicationDir() {
        resolve('webapps/ROOT')
    }

    File getTempDir() {
        resolve('temp')
    }

    File getLogFile() {
        new File(logDir, "catalina.${new Date().format('YYYY-MM-dd')}.log")
    }

    File getContextFile() {
        new File(configDir, 'context.xml')
    }

    int getHttpPort() {
        return 8080
    }

    int getShutdownPort() {
        return 8005
    }

    int getAjpPort() {
        return 8009
    }

    class Controller {

        final String dispatcher = new File(distro,'bin/catalina.sh')

        State state = new StoppedState()

        synchronized void startInstance() {
            state.startInstance()
        }

        synchronized void stopInstance() {
            state.stopInstance()
        }

        Process runProcess(def command) {
            ProcessBuilder pb = new ProcessBuilder(dispatcher, command)
            pb.environment()['CATALINA_BASE'] = dir.path
            pb.start()
        }

        abstract class State {

            void startInstance() {}

            void stopInstance() {}

        }

        class StoppedState extends State {

            void startInstance() {
                Process p = runProcess('start')
                if (p.waitFor() != 0) {
                    log.error "failed to start tomcat instance: ${p.errorStream.text}"
                    return
                }
                [httpPort, ajpPort, shutdownPort].each { port ->
                    while (true) {
                        log.debug "checking for port ${port} to be accepting"
                        try {
                            new Socket('127.0.0.1', port).close()
                            break
                        } catch (ConnectException e) {
                            sleep(100)
                        }
                    }
                }
                state = new StartedState()
                log.debug "started tomcat instance"
            }

        }

        class StartedState extends State {

            void stopInstance() {
                Process p = runProcess('stop')
                if (p.waitFor() != 0) {
                    log.error "failed to stop tomcat instance: ${p.errorStream.text}"
                    return
                }
                [httpPort, ajpPort, shutdownPort].each { port ->
                    while (true) {
                        log.debug "checking for port ${port} to no longer be accepting"
                        try {
                            new Socket('127.0.0.1', port).close()
                            sleep(100)
                        } catch (ConnectException e) {
                            break
                        }
                    }
                }
                state = new StoppedState()
                log.debug "stopped tomcat instance"
            }

        }

    }

}
