package io.openshift.launchpad.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Pull request data object.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PullRequest {
    private String action;
    private Integer number;
    private Repository repository;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    @Override
    public String toString() {
        return "PullRequest{" +
                "action='" + action + '\'' +
                ", number=" + number +
                ", repository=" + repository +
                '}';
    }
}
