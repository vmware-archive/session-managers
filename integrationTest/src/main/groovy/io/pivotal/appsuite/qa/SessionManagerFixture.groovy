package io.pivotal.appsuite.qa

import groovy.util.logging.Slf4j

import java.nio.file.Files
import java.nio.file.Paths

@Slf4j
abstract class SessionManagerFixture extends Fixture{

    final String module
    final TomcatFixture tomcat

    SessionManagerFixture(String module, TomcatFixture tomcat) {
        this.module = module
        this.tomcat = tomcat
        // library
        def jar = "${module}-${version}-all.jar"
        def src = Paths.get("${moduleDir}/build/libs/${jar}")
        def dst = Paths.get(tomcat.libDir.path).resolve(jar)
        Files.copy(src, dst)
    }

    String getVersion() {
        System.properties['io.pivotal.appsuite.qa.project.version']
    }

    File getModuleDir() {
        new File(System.properties['io.pivotal.appsuite.qa.project.rootdir'], module)
    }

}
