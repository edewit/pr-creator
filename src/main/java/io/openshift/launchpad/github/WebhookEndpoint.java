package io.openshift.launchpad.github;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import io.openshift.launchpad.catalog.Booster;
import io.openshift.launchpad.catalog.BoosterCatalogService;
import io.openshift.launchpad.github.model.PullRequest;

/**
 * Webhook endpoint that will create PR based on documentation updates.
 */
@Path("/")
public class WebhookEndpoint {

    @Inject
    private PullRequestService pullRequest;

    @Inject
    private BoosterCatalogService boosterCatalogService;

    @Inject
    private TemplateMergerService templateMergerService;

    private List<Booster> boosters;

    @PostConstruct
    void index() {
        this.boosters = boosterCatalogService.getBoosters();
    }

    @POST
    @Path("/hook")
    @Consumes(MediaType.APPLICATION_JSON)
    public void hook(PullRequest payload) throws IOException {
        String missionName = pullRequest.isDocumentationUpdated(payload.getRepository().getFullName(), payload.getNumber());
        if (missionName != null) {
            File location = pullRequest.fork(boosters, missionName);
            File documentationLocation = pullRequest.checkout(payload.getRepository().getFullName());
            String html = templateMergerService.convertToAsciidoc(new File(documentationLocation, pullRequest.getDoc(missionName)));
            templateMergerService.mergeTemplate(location, html);
        }
    }
}
