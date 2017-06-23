package io.openshift.launchpad.github.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Repository
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Repository {
    @JsonProperty("full_name")
    private String fullName;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String toString() {
        return "Repository{" +
                "fullName='" + fullName + '\'' +
                '}';
    }
}
