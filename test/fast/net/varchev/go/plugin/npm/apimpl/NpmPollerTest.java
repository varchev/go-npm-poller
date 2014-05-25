package net.varchev.go.plugin.npm.apimpl;

import com.thoughtworks.go.plugin.api.config.Property;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageConfiguration;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageMaterialProperty;
import com.thoughtworks.go.plugin.api.material.packagerepository.RepositoryConfiguration;
import net.varchev.go.plugin.npm.NpmParams;
import net.varchev.go.plugin.npm.config.NpmPackageConfig;
import com.tw.go.plugin.util.RepoUrl;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Date;

import static org.mockito.Mockito.*;

public class NpmPollerTest {
    @Test
    public void PollerShouldExcuteCorrectCmd(){
        NpmPoller poller = new NpmPoller();
        NpmPoller spy = spy(poller);
        RepositoryConfiguration repoCfgs = mock(RepositoryConfiguration.class);
        PackageConfiguration pkgCfgs = mock(PackageConfiguration.class);
        String repoUrlStr = "http://google.com";//something valid to satisfy connection check
        when(repoCfgs.get(RepoUrl.REPO_URL)).thenReturn(new PackageMaterialProperty(RepoUrl.REPO_URL, repoUrlStr));
        String user = "user";
        when(repoCfgs.get(RepoUrl.USERNAME)).thenReturn(new PackageMaterialProperty(RepoUrl.USERNAME, user));
        String password = "passwrod";
        when(repoCfgs.get(RepoUrl.PASSWORD)).thenReturn(new PackageMaterialProperty(RepoUrl.PASSWORD, password));
        String packageId = "express";
        Property property = new PackageMaterialProperty(NpmPackageConfig.PACKAGE_ID, packageId);
        when(pkgCfgs.get(NpmPackageConfig.PACKAGE_ID)).thenReturn(property);
        PackageRevision dummyResult = new PackageRevision("1.0", new Date(),"user");
        RepoUrl repoUrl = RepoUrl.create(repoUrlStr, user, password);
        final NpmParams params = new NpmParams(repoUrl, packageId, null, null, null);
        Matcher<NpmParams> npmParamsMatcher = new BaseMatcher<NpmParams>() {
            NpmParams expected = params;
            @Override
            public boolean matches(Object item) {
                NpmParams npmParams = (NpmParams) item;
                return expected.getPackageId().equals(npmParams.getPackageId()) &&
                        expected.getRepoUrl().equals(npmParams.getRepoUrl());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(expected.getPackageId());
            }
        };
        doReturn(dummyResult).when(spy).poll(argThat(npmParamsMatcher));
        //actual test
        spy.getLatestRevision(pkgCfgs, repoCfgs);
        verify(spy).poll(argThat(npmParamsMatcher));
    }

}
