package net.varchev.go.plugin.npm;

import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import net.varchev.go.plugin.npm.config.NpmPackageConfig;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class NpmFeedDocumentTest {

    @Test
    public void shouldCreatePackageRevision() throws Exception {
        String fileContent = FileUtils.readFileToString(new File("test" + File.separator + "fast" + File.separator + "npm-good-feed.json"));
        JSONObject doc = new JSONObject(fileContent);
        PackageRevision result = new NpmFeedDocument(doc,null,null,null).getPackageRevision();
        assertThat(result.getUser(), is("Jeremy Ashkenas"));
        assertThat(result.getRevision(), is("underscore-1.6.0"));

        assertThat(result.getTimestamp(), is(javax.xml.bind.DatatypeConverter.parseDateTime(("2014-02-10T21:14:55.838Z")).getTime()));
        assertThat(result.getDataFor(NpmPackageConfig.PACKAGE_LOCATION), is("http://registry.npmjs.org/underscore/-/underscore-1.6.0.tgz"));
        assertThat(result.getDataFor(NpmPackageConfig.PACKAGE_VERSION), is("1.6.0"));
    }
}
