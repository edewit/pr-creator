package io.openshift.launchpad.github;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import io.openshift.launchpad.catalog.Booster;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.yaml.snakeyaml.Yaml;

/**
 * Service for pull requests.
 */
@ApplicationScoped
public class PullRequestService {

    private Map<String, String> map = (Map<String, String>) new Yaml().load(getClass().getResourceAsStream("/mapping.yml"));

    /**
     * Is this PR an update of the booster index.html file
     *
     * @param repoName the repo to check
     * @param pr       the PR number to check
     * @return the name of the mission to update or null if this is not a documentation update
     */
    public String isDocumentationUpdated(String repoName, Integer pr) {
        try {
            GitHub gitHub = GitHub.connectAnonymously();
            GHRepository repository = gitHub.getRepository(repoName);

            PagedIterable<GHPullRequestFileDetail> files = repository.getPullRequest(pr).listFiles();
            for (GHPullRequestFileDetail file : files) {
                for (Map.Entry<String, String> fileName : map.entrySet()) {
                    if (fileName.getKey().equals(file.getFilename())) {
                        return fileName.getValue();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getDoc(String missionName) {
        return map.entrySet().stream().filter(e -> e.getValue().equals(missionName)).findFirst()
                .orElseThrow(() -> new RuntimeException("key not found")).getValue();
    }

    /**
     * Create PRs for the boosters that have an index.html that needs updating.
     *
     * @param boosterList list of boosters all boosters
     * @param missionName the name of the mission to update.
     * @return the location of the fork.
     */
    public File fork(List<Booster> boosterList, String missionName) {
        try {
            for (Booster booster : boosterList) {
                if (missionName.equals(booster.getMission().getName())) {
                    String githubRepo = booster.getGithubRepo();
                    GitHub gitHub = GitHub.connectUsingOAuth(System.getenv("GITHUB_TOKEN"));

                    GHRepository fork = gitHub.getRepository(githubRepo).fork();
                    return checkout(fork).getRepository().getWorkTree();
                }
            }
        } catch (IOException | GitAPIException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void createPullRequest(Git gitRepo) throws GitAPIException {
        createBranch(gitRepo);
        commit(gitRepo);
        push(gitRepo);
    }

    public File checkout(String repoName) throws IOException {
        GitHub gitHub = GitHub.connectAnonymously();
        try (Git git = checkout(gitHub.getRepository(repoName))) {
            return git.getRepository().getWorkTree();
        } catch (GitAPIException | URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private Git checkout(GHRepository gitHubRepository) throws IOException, GitAPIException, URISyntaxException {
        File path = Files.createTempDirectory("checkout").toFile();
        return Git.cloneRepository().setDirectory(path)
                .setURI(gitHubRepository.gitHttpTransportUrl()).call();
    }

    private void createBranch(Git gitRepo) throws GitAPIException {
        gitRepo.branchCreate().setName("documentation-update").call();
    }

    private void commit(Git repo) throws GitAPIException {
        repo.add().addFilepattern(".").call();
        repo.commit().setMessage("automatically updated").call();
    }

    private void push(Git repo) throws GitAPIException {
        repo.push().call();
    }

}
