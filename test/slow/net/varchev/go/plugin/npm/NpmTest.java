package net.varchev.go.plugin.npm;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import net.varchev.go.plugin.npm.apimpl.NpmPoller;
import com.tw.go.plugin.util.RepoUrl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static net.varchev.go.plugin.npm.config.NpmPackageConfig.PACKAGE_LOCATION;
import static net.varchev.go.plugin.npm.config.NpmPackageConfig.PACKAGE_VERSION;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NpmTest {
    @Test
    public void shouldReportLocationCorrectly() {
        PackageRevision result = new NpmPoller().poll(new NpmParams(RepoUrl.create("http://registry.npmjs.org", null, null), "underscore", null, "1.6.1", null));
        assertThat(result.getDataFor(PACKAGE_LOCATION), is("http://registry.npmjs.org/underscore/-/underscore-1.6.0.tgz"));
    }


    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldFailIfNoPackagesFound() {
        expectedEx.expect(NpmException.class);
        expectedEx.expectMessage("No such package found");
        new NpmPoller().poll(new NpmParams(RepoUrl.create("http://registry.npmjs.org", null, null), "NoSuchPackageFound", null, null, null));
    }

    @Test
    public void shouldGetUpdateWhenLastVersionKnown() throws ParseException {
        PackageRevision lastKnownVersion = new PackageRevision("1Password-1.0.9.288", new SimpleDateFormat("yyyy-MM-dd").parse("2013-03-21"), "xyz");
        lastKnownVersion.addData(PACKAGE_VERSION, "1.5.2");
        PackageRevision result = new NpmPoller().poll(new NpmParams(RepoUrl.create("http://registry.npmjs.org", null, null), "underscore", null, null, lastKnownVersion));
        assertThat(result.getDataFor(PACKAGE_VERSION), is("1.6.0"));
    }

    @Test
    public void shouldReturnNullIfNoNewerRevision() throws ParseException {
        PackageRevision lastKnownVersion = new PackageRevision("underscore-10.0.9.332", new SimpleDateFormat("yyyy-MM-dd").parse("2014-03-21"), "xyz");
        lastKnownVersion.addData(PACKAGE_VERSION, "10.0.9.332");
        NpmParams params = new NpmParams(RepoUrl.create("http://registry.npmjs.org", null, null), "underscore", null, null, lastKnownVersion);
        assertNull(new NpmPoller().poll(params));

    }
}
