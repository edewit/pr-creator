package io.openshift.launchpad.github;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import io.openshift.booster.catalog.Booster;
import io.openshift.booster.catalog.BoosterCatalogService;
import io.openshift.launchpad.github.model.Mapping;
import io.openshift.launchpad.github.model.PullRequest;

/**
 * Webhook endpoint that will create PR based on documentation updates.
 */
@Path("/")
@ApplicationScoped
public class WebhookEndpoint {

    @Inject
    private PullRequestService pullRequest;

    @Inject
    private TemplateMergerService templateMergerService;

    private List<Booster> boosters;

    @PostConstruct
    void index() {
        BoosterCatalogService boosterCatalogService = new BoosterCatalogService.Builder().build();
        boosterCatalogService.index();
        this.boosters = boosterCatalogService.getBoosters();
    }

    @POST
    @Path("/hook")
    @Consumes(MediaType.APPLICATION_JSON)
    public void hook(PullRequest payload) throws IOException {
        Mapping mapping = pullRequest.isDocumentationUpdated(payload.getRepository().getFullName(), payload.getNumber());
        if (mapping != null) {
            File location = pullRequest.fork(boosters, mapping.getMissionName());
            File documentationFolder = pullRequest.checkout(payload.getRepository().getFullName());
            String html = templateMergerService.convertToAsciidoc(new File(documentationFolder, mapping.getDocumentationLocation()));
            File file = templateMergerService.mergeTemplate(location, html);
            file.renameTo(location.listFiles(name -> name.getName().equals(file.getName()))[0]);
            pullRequest.createPullRequest(location);
        }
    }
}
