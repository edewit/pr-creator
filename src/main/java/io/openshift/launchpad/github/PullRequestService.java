package io.openshift.launchpad.github;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import io.openshift.launchpad.catalog.Booster;
import io.openshift.launchpad.github.model.Mapping;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
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
    public Mapping isDocumentationUpdated(String repoName, Integer pr) {
        try {
            GitHub gitHub = GitHub.connectAnonymously();
            GHRepository repository = gitHub.getRepository(repoName);

            PagedIterable<GHPullRequestFileDetail> files = repository.getPullRequest(pr).listFiles();
            for (GHPullRequestFileDetail file : files) {
                for (Map.Entry<String, String> fileName : map.entrySet()) {
                    if (fileName.getKey().equals(file.getFilename())) {
                        return new Mapping(fileName.getKey(), fileName.getValue());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Create PRs for the boosters that have an index.html that needs updating.
     *
     * @param boosterList list of boosters all boosters
     * @param missionId the id of the mission to update.
     * @return the location of the fork.
     */
    public File fork(List<Booster> boosterList, String missionId) {
        try {
            for (Booster booster : boosterList) {
                if (missionId.equals(booster.getMission().getId())) {
                    String githubRepo = booster.getGithubRepo();
                    GitHub gitHub = getGitHub();

                    GHRepository fork = gitHub.getRepository(githubRepo).fork();
                    return checkout(fork).getRepository().getWorkTree();
                }
            }
        } catch (IOException | GitAPIException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    static GitHub getGitHub() throws IOException {
        return GitHub.connectUsingOAuth(System.getenv("GITHUB_TOKEN"));
    }

    public void createPullRequest(File repo) {
        try (Git gitRepo = Git.open(repo)) {
            createBranch(gitRepo);
            commit(gitRepo);
            push(gitRepo);
        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public File checkout(String repoName) throws IOException {
        GitHub gitHub = GitHub.connectAnonymously();
        try (Git git = checkout(gitHub.getRepository(repoName))) {
            return git.getRepository().getWorkTree();
        } catch (GitAPIException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Git checkout(GHRepository gitHubRepository) throws IOException, GitAPIException, URISyntaxException {
        File path = Files.createTempDirectory("checkout").toFile();
        return Git.cloneRepository().setDirectory(path)
                .setURI(gitHubRepository.gitHttpTransportUrl()).call();
    }

    private void createBranch(Git gitRepo) throws GitAPIException {
        String name = "documentation-update";
        gitRepo.branchCreate().setName(name).call();
        gitRepo.checkout().setName(name).call();
    }

    private void commit(Git repo) throws GitAPIException {
        repo.add().addFilepattern(".").call();
        repo.commit().setMessage("automatically updated").call();
    }

    private void push(Git repo) throws GitAPIException {
        UsernamePasswordCredentialsProvider credentialsProvider =
                new UsernamePasswordCredentialsProvider(System.getenv("GITHUB_TOKEN"), "");
        repo.push().setCredentialsProvider(credentialsProvider).call();
    }

}
