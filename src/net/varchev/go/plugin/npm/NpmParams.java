package net.varchev.go.plugin.npm;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;
import net.varchev.go.plugin.npm.config.NpmPackageConfig;

public class NpmParams {
    private final String packageId;
    private final RepoUrl repoUrl;
    private String pollVersionFrom;
    private String pollVersionTo;
    private PackageRevision lastKnownVersion = null;

    public NpmParams(RepoUrl repoUrl, String packageId, String pollVersionFrom, String pollVersionTo, PackageRevision previouslyKnownRevision) {
        this.repoUrl = repoUrl;
        this.packageId = packageId;
        if (pollVersionFrom != null && !pollVersionFrom.trim().isEmpty()) this.pollVersionFrom = pollVersionFrom;
        if (pollVersionTo != null && !pollVersionTo.trim().isEmpty()) this.pollVersionTo = pollVersionTo;
        this.lastKnownVersion = previouslyKnownRevision;
    }

    public String getPollVersionFrom() {

        return pollVersionFrom;
    }
    public String getPollVersionTo() {

        return pollVersionTo;
    }

    public String getPackageId() {
        return packageId;
    }

    public RepoUrl getRepoUrl() {
        return repoUrl;
    }

    public boolean isLastVersionKnown() {
        return lastKnownVersion != null;
    }

    public String getLastKnownVersion() {
        if (lastKnownVersion == null) return null;
        return lastKnownVersion.getDataFor(NpmPackageConfig.PACKAGE_VERSION);
    }

    public boolean lowerBoundGiven() {
        return pollVersionFrom != null;
    }

    public boolean upperBoundGiven() {
        return pollVersionTo != null;
    }

    public String getQuery() {
        StringBuilder query = new StringBuilder();
        String repoUrlString = ((HttpRepoURL) repoUrl).getUrlWithBasicAuth();
        query.append(repoUrlString);

        if (!repoUrlString.endsWith("/")) {
            query.append("/");
        }

        query.append(getPackageId());

        return query.toString();
    }

    private String getEffectiveLowerBound() {
        if (getLastKnownVersion() != null) return getLastKnownVersion();
        if (lowerBoundGiven()) return pollVersionFrom;
        return "0.0.1";
    }

}
