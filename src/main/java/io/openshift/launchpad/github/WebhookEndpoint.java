package io.openshift.launchpad.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;

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
    private Logger logger;

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
        logger.info("Analising PR for booster documentation update ({})", payload);
        Mapping mapping = pullRequest.isDocumentationUpdated(payload.getRepository().getFullName(), payload.getNumber());
        
        if (mapping != null) {
            logger.info("This PR updates mission '{}'", mapping.getMissionName());
            List<File> locations = pullRequest.fork(boosters, mapping.getMissionName());
            for (File location : locations) {
                File documentationFolder = pullRequest.checkout(payload);
                String html = templateMergerService.convertToAsciidoc(new File(documentationFolder, mapping.getDocumentationLocation()));
                File file = templateMergerService.mergeTemplate(location, html);
                if (file != null) {
                    Optional<java.nio.file.Path> path = Files.walk(location.toPath()).filter(
                        name -> name.getFileName().toString().equals("index.html")).findFirst();
                    if (path.isPresent()) {
                        logger.info("Updated booster, creating PR on booster project");
                        file.renameTo(path.get().toFile());
                        pullRequest.createPullRequest(location, payload);
                    }
                }
           }
        } else {
            logger.info("No booster documentation changes found");
        }
    }
}
