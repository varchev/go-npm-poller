package net.varchev.go.plugin.npm.config;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;

public class NpmPackageConfig {
    public static final String PACKAGE_ID = "PACKAGE_ID";
    public static final String POLL_VERSION_FROM = "POLL_VERSION_FROM";
    public static final String POLL_VERSION_TO = "POLL_VERSION_TO";
    public static final String PACKAGE_LOCATION = "LOCATION";
    public static final String PACKAGE_VERSION = "VERSION";
    private final PackageConfiguration packageConfigs;
    private final Property packageIdConfig;

    public NpmPackageConfig(PackageConfiguration packageConfigs) {
        this.packageConfigs = packageConfigs;
        this.packageIdConfig = packageConfigs.get(PACKAGE_ID);
    }

    public boolean isPackageIdMissing() {
        return packageIdConfig == null;
    }

    public String getPackageId() {
        return packageIdConfig.getValue();
    }

    public static String[] getValidKeys() {
        return new String[]{PACKAGE_ID, POLL_VERSION_FROM, POLL_VERSION_TO};
    }

    public String getPollVersionFrom() {
        Property from = packageConfigs.get(POLL_VERSION_FROM);
        return (from == null) ? null : from.getValue();
    }

    public String getPollVersionTo() {
        Property to = packageConfigs.get(POLL_VERSION_TO);
        return (to == null) ? null : to.getValue();
    }

    public boolean hasBounds() {
        return getPollVersionFrom() != null || getPollVersionTo() != null;
    }
}
