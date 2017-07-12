package io.openshift.launchpad.github;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.openshift.booster.catalog.Booster;
import io.openshift.booster.catalog.Mission;
import io.openshift.launchpad.github.model.Mapping;
import org.junit.Test;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GitHub;

import static org.junit.Assert.*;

/**
 * Test for the PullRequestService.
 */
public class PullRequestServiceTest {

    private static final String GITHUB_REPO = "openshiftio-vertx-boosters/vertx-http-booster";

    private PullRequestService pullRequestService = new PullRequestService();

    @Test
    public void isDocumentationUpdated() throws Exception {
        //given

        //when
        Mapping boosterName = pullRequestService.isDocumentationUpdated("openshiftio/appdev-documentation/", 295);

        //then
        assertNull(boosterName);
    }

    @Test
    public void shouldForkRepoAndCreatePR() throws IOException {
        /* ---shouldForkRepo --- */
        //given
        Booster booster = new Booster();
        booster.setGithubRepo(GITHUB_REPO);
        String mission = "impossible";
        booster.setMission(new Mission(mission));

        //when
        List<File> forks = pullRequestService.fork(Collections.singletonList(booster), mission);

        //then
        File fork = forks.get(0);
        assertTrue(fork.exists());

        /* ---shouldCreatePR--- */
        //given
        File file = fork.listFiles()[0];
        try (PrintWriter printWriter = new PrintWriter(file)) {
            printWriter.write("changed");
        }

        //when
        pullRequestService.createPullRequest(fork, null);

        //then
        GitHub gitHub = PullRequestService.getGitHub();
        Map<String, GHBranch> branches = gitHub.getMyself().getRepository("vertx-http-booster").getBranches();
        assertTrue(branches.containsKey("documentation-update"));
    }

    @Test
    public void shouldCheckout() throws IOException {
        //when
        File checkout = pullRequestService.checkout(GITHUB_REPO);

        //then
        assertTrue(checkout.exists());
        assertNotEquals(new String[0], checkout.list());
    }
}