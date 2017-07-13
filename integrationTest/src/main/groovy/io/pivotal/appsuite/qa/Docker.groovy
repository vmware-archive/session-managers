package io.pivotal.appsuite.qa

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.model.PortBinding
import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.core.DockerClientConfig
import com.github.dockerjava.core.command.PullImageResultCallback
import groovy.util.logging.Slf4j

@Slf4j
class Docker {

    final DockerClient client

    Docker() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
        client = DockerClientBuilder.getInstance(config).build()
    }

    String createContainer(def image, Closure cl=null) {
        def images = client.listImagesCmd().withImageNameFilter(image).exec()
        if (!images) {
            log.info "pulling docker image ${image}"
            client.pullImageCmd(image).exec(new PullImageResultCallback()).awaitSuccess()
        }
        log.info "creating docker container for ${image}"
        def cmd = client.createContainerCmd(image)
        if (cl) {
            cl(cmd)
        }
        def id = cmd.exec().id
        log.info "created docker container ${id}"
        return id
    }

    void destroyContainer(id) {
        log.info "removing docker container ${id}"
        client.removeContainerCmd(id).exec()
    }

    void startContainer(id) {
        log.info "starting docker container ${id}"
        client.startContainerCmd(id).exec()
    }

    void stopContainer(id) {
        log.info "stopping docker container ${id}"
        client.stopContainerCmd(id).exec()
        client.waitContainerCmd(id).exec()
    }

}
