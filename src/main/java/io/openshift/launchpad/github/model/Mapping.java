package io.openshift.launchpad.github.model;

/**
 * Mapping between documentation and missionName.
 */
public class Mapping {
    private String documentationLocation;
    private String missionName;

    public Mapping(String documentationLocation, String missionName) {
        this.documentationLocation = documentationLocation;
        this.missionName = missionName;
    }

    public String getDocumentationLocation() {
        return documentationLocation;
    }

    public void setDocumentationLocation(String documentationLocation) {
        this.documentationLocation = documentationLocation;
    }

    public String getMissionName() {
        return missionName;
    }

    public void setMissionName(String missionName) {
        this.missionName = missionName;
    }

    @Override
    public String toString() {
        return "Mapping{" +
                "documentationLocation='" + documentationLocation + '\'' +
                ", missionName='" + missionName + '\'' +
                '}';
    }
}
