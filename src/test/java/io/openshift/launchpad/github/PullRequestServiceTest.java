package io.openshift.launchpad.github;

import java.util.Collections;

import io.openshift.launchpad.catalog.Booster;
import io.openshift.launchpad.catalog.Mission;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test for the PullRequestService.
 */
public class PullRequestServiceTest {

    @Test
    public void isDocumentationUpdated() throws Exception {
        //given
        PullRequestService pullRequestService = new PullRequestService();

        //when
        String boosterName = pullRequestService.isDocumentationUpdated("openshiftio/appdev-documentation/", 295);

        //then
        assertNull(boosterName);
    }

    @Test
    public void shouldCreatePrs() {
        //given
        PullRequestService pullRequestService = new PullRequestService();
        Booster booster = new Booster();
        booster.setGithubRepo("openshiftio-vertx-boosters/vertx-http-booster");
        String mission = "impossible";
        booster.setMission(new Mission(mission));

        //when
        pullRequestService.fork(Collections.singletonList(booster), mission);
    }

}