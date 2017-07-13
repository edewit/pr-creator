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
import io.openshift.launchpad.github.model.PullRequest;
import io.openshift.launchpad.github.model.Repository;

import org.junit.Test;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GitHub;

import static org.junit.Assert.*;

/**
 * Test for the PullRequestService.
 */
public class PullRequestServiceTest {

    private static final String GITHUB_REPO = "edewit/doc-sync-test";

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
        try (PrintWriter printWriter = new PrintWriter(new File(fork, "README.md"))) {
            printWriter.write("changed");
        }

        PullRequest pr = createPullRequest(GITHUB_REPO, 1);
        
        //when
        pullRequestService.createPullRequest(fork, pr);

        //then
        GitHub gitHub = PullRequestService.getGitHub();
        Map<String, GHBranch> branches = gitHub.getMyself().getRepository(GITHUB_REPO).getBranches();
        assertTrue(branches.containsKey("documentation-update"));
    }

    @Test
    public void shouldCheckout() throws IOException {
    	//given
    	PullRequest pr = createPullRequest("openshiftio/appdev-documentation", 342);
    	
        //when
        File checkout = pullRequestService.checkout(pr);

        //then
        assertTrue(checkout.exists());
        assertNotEquals(new String[0], checkout.list());
    }
    
	private PullRequest createPullRequest(String repo, Integer prNumber) {
		PullRequest pr = new PullRequest();
    	pr.setNumber(prNumber);
    	Repository repository = new Repository();
    	repository.setFullName(repo);
		pr.setRepository(repository);
		return pr;
	}
}