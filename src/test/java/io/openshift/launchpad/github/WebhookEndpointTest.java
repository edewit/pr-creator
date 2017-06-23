package io.openshift.launchpad.github;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import io.openshift.launchpad.github.model.PullRequest;
import io.openshift.launchpad.github.model.Repository;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

/**
 * Test for the WebhookEndpoint
 */
@RunWith(Arquillian.class)
@DefaultDeployment
public class WebhookEndpointTest {

    @Test
    @RunAsClient
    public void test_service_invocation() {
        //given
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080")
                .path("api").path("hook");

        //when
        PullRequest pullRequest = new PullRequest();
        pullRequest.setNumber(295);
        Repository repository = new Repository();
        repository.setFullName("openshiftio/appdev-documentation/");
        pullRequest.setRepository(repository);
        target.request().post(Entity.entity(pullRequest, MediaType.APPLICATION_JSON_TYPE));

    }
}
