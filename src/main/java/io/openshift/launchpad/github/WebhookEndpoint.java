package io.openshift.launchpad.github;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

//import io.openshift.launchpad.catalog.Booster;
//import io.openshift.launchpad.catalog.BoosterCatalogService;
import io.openshift.launchpad.github.model.PullRequest;

/**
 * Webhook endpoint that will create PR based on documentation updates.
 */
@Path("/")
public class WebhookEndpoint {

    @Inject
    private PullRequestService pullRequest;

//    @Inject
//    private BoosterCatalogService boosterCatalogService;

//    @Inject
//    private TemplateMergerService templateMergerService;

    @POST
    @Path("/hook")
    @Consumes(MediaType.APPLICATION_JSON)
    public void hook(PullRequest payload) {
        System.out.println("pullRequest = " + pullRequest);
//        System.out.println("boosterCatalogService = " + boosterCatalogService);
        String missionName = pullRequest.isDocumentationUpdated(payload.getRepository().getFullName(), payload.getNumber());
//        if (missionName != null) {
//            List<Booster> boosters = boosterCatalogService.getBoosters();
////            pullRequest.createPrs(boosters, missionName);
//            System.out.println("boosters.get(0) = " + boosters.get(0));
//        }
    }

}
