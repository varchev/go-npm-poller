package net.varchev.go.plugin.npm;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Test;

import static net.varchev.go.plugin.npm.config.NpmPackageConfig.PACKAGE_VERSION;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NpmParamsTest {
    @Test
    public void shouldHandleUpperBound(){
        NpmParams params = new NpmParams(RepoUrl.create("http://registry.npmjs.org", null, null),
                "express", null, "1.2", null);
        assertThat(params.getQuery(),
                is("http://registry.npmjs.org/express"));
    }
    @Test
    public void shouldIgnoreLowerBoundDuringUpdate(){
        PackageRevision known = new PackageRevision("1.1.2",null,"abc");
        known.addData(PACKAGE_VERSION,"1.1.2");
        NpmParams params = new NpmParams(RepoUrl.create("http://registry.npmjs.org", null, null),
                "express", "1.0", null, known);
        assertThat(params.getQuery(),
                is("http://registry.npmjs.org/express"));
    }
}
