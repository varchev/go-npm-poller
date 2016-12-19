package net.varchev.go.plugin.npm.apimpl;

import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProvider;

public class NpmProvider implements PackageMaterialProvider {

    public PluginConfig getConfig() {
        return new PluginConfig();
    }

    public NpmPoller getPoller() {
        return new NpmPoller();
    }
}
