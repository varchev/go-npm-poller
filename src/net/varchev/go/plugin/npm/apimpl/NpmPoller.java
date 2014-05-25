package net.varchev.go.plugin.npm.apimpl;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialPoller;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import com.thoughtworks.go.plugin.api.response.Result;
import com.thoughtworks.go.plugin.api.response.validation.ValidationError;
import com.thoughtworks.go.plugin.api.response.validation.ValidationResult;
import net.varchev.go.plugin.npm.Feed;
import net.varchev.go.plugin.npm.NpmFeedDocument;
import net.varchev.go.plugin.npm.NpmParams;
import net.varchev.go.plugin.npm.config.NpmPackageConfig;
import net.varchev.go.plugin.npm.config.NpmRepoConfig;
import com.tw.go.plugin.util.Credentials;
import com.tw.go.plugin.util.HttpRepoURL;
import com.tw.go.plugin.util.RepoUrl;

public class NpmPoller implements PackageMaterialPoller {
    private static Logger LOGGER = Logger.getLoggerFor(NpmPoller.class);

    public PackageRevision getLatestRevision(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig) {
        LOGGER.info(String.format("getLatestRevision called with packageId %s, for repo: %s",
                packageConfig.get(NpmPackageConfig.PACKAGE_ID).getValue(), repoConfig.get(RepoUrl.REPO_URL).getValue()));
        validateConfig(repoConfig, packageConfig);
        NpmPackageConfig npm = new NpmPackageConfig(packageConfig);
        NpmParams params = new NpmParams(
                new NpmRepoConfig(repoConfig).getRepoUrl(),
                npm.getPackageId(),
                npm.getPollVersionFrom(),
                npm.getPollVersionTo(), null);
        PackageRevision packageRevision = poll(params);
        LOGGER.info(String.format("getLatestRevision returning with %s, %s",
                packageRevision.getRevision(), packageRevision.getTimestamp()));
        return packageRevision;
    }

    public PackageRevision latestModificationSince(PackageConfiguration packageConfig, RepositoryConfiguration repoConfig, PackageRevision previouslyKnownRevision) {
        LOGGER.info(String.format("latestModificationSince called with packageId %s, for repo: %s",
                packageConfig.get(NpmPackageConfig.PACKAGE_ID).getValue(), repoConfig.get(RepoUrl.REPO_URL).getValue()));
        validateConfig(repoConfig, packageConfig);
        NpmPackageConfig npmPackageConfig = new NpmPackageConfig(packageConfig);
        NpmParams params = new NpmParams(
                new NpmRepoConfig(repoConfig).getRepoUrl(),
                npmPackageConfig.getPackageId(),
                npmPackageConfig.getPollVersionFrom(),
                npmPackageConfig.getPollVersionTo(),
                previouslyKnownRevision);
        PackageRevision updatedPackage = poll(params);
        if (updatedPackage == null) {
            LOGGER.info(String.format("no modification since %s", previouslyKnownRevision.getRevision()));
            return null;
        }
        LOGGER.info(String.format("latestModificationSince returning with %s, %s",
                updatedPackage.getRevision(), updatedPackage.getTimestamp()));
        if (updatedPackage.getTimestamp().getTime() < previouslyKnownRevision.getTimestamp().getTime())
            LOGGER.warn(String.format("Updated Package %s published earlier (%s) than previous (%s, %s)",
                    updatedPackage.getRevision(), updatedPackage.getTimestamp(), previouslyKnownRevision.getRevision(), previouslyKnownRevision.getTimestamp()));
        return updatedPackage;
    }

    @Override
    public Result checkConnectionToRepository(RepositoryConfiguration repoConfigs) {
        Result response = new Result();
        NpmRepoConfig npmRepoConfig = new NpmRepoConfig(repoConfigs);
        RepoUrl repoUrl = npmRepoConfig.getRepoUrl();
        if (repoUrl.isHttp()) {
            try {
                repoUrl.checkConnection(((HttpRepoURL) repoUrl).getUrlStrWithTrailingSlash());
            } catch (Exception e) {
                response.withErrorMessages(e.getMessage());
            }
        } else {
            repoUrl.checkConnection();
        }
        LOGGER.info(response.getMessagesForDisplay());
        return response;
    }

    @Override
    public Result checkConnectionToPackage(PackageConfiguration packageConfigs, RepositoryConfiguration repoConfigs) {
        Result response = checkConnectionToRepository(repoConfigs);
        if (!response.isSuccessful()) {
            LOGGER.info(response.getMessagesForDisplay());
            return response;
        }
        PackageRevision packageRevision = getLatestRevision(packageConfigs, repoConfigs);
        response.withSuccessMessages("Found " + packageRevision.getRevision());
        return response;
    }

    private void validateConfig(RepositoryConfiguration repoConfig, PackageConfiguration packageConfig) {
        ValidationResult errors = new PluginConfig().isRepositoryConfigurationValid(repoConfig);
        errors.addErrors(new PluginConfig().isPackageConfigurationValid(packageConfig, repoConfig).getErrors());
        if (!errors.isSuccessful()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ValidationError validationError : errors.getErrors()) {
                stringBuilder.append(validationError.getMessage()).append("; ");
            }
            String errorString = stringBuilder.toString();
            throw new RuntimeException(errorString.substring(0, errorString.length() - 2));
        }
    }

    public PackageRevision poll(NpmParams params) {
        String url = params.getQuery();
        LOGGER.info(url);
        PackageRevision packageRevision = new NpmFeedDocument(new Feed(url).download(), params.getPollVersionFrom(), params.getPollVersionTo(), params.getLastKnownVersion()).getPackageRevision();
        if(params.getRepoUrl().getCredentials().provided())
            addUserInfoToLocation(packageRevision, params.getRepoUrl().getCredentials());
        return packageRevision;
    }

    private void addUserInfoToLocation(PackageRevision packageRevision, Credentials credentials) {
        String location = packageRevision.getDataFor(NpmPackageConfig.PACKAGE_LOCATION);
        packageRevision.addData(NpmPackageConfig.PACKAGE_LOCATION, HttpRepoURL.getUrlWithCreds(location, credentials));
    }
}
