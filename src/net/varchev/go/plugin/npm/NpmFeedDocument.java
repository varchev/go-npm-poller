package net.varchev.go.plugin.npm;

import com.github.zafarkhaja.semver.Version;
import com.thoughtworks.go.plugin.api.material.packagerepository.PackageRevision;
import net.varchev.go.plugin.npm.config.NpmPackageConfig;
import org.json.JSONObject;

import java.util.*;

public class NpmFeedDocument {

    private final JSONObject feedObject;
    private Version lowerBoundVersion;
    private Version upperBoundVersion;
    private Version lastKnownVersion;
    private HashMap<Version, String> versionStringMap;
    private ArrayList<Version> versionsList;


    public NpmFeedDocument(JSONObject feedObject, String lowerBoundVersion, String upperBoundVersion, String lastKnownVersion) {
        this.feedObject = feedObject;
        if(lowerBoundVersion != null) {
            this.lowerBoundVersion = Version.valueOf(lowerBoundVersion);
        }

        if(upperBoundVersion != null) {
            this.upperBoundVersion = Version.valueOf(upperBoundVersion);
        }

        if(lastKnownVersion != null) {
            this.lastKnownVersion = Version.valueOf(lastKnownVersion);
        }
        populateVersionsList();
    }

    private String getPackageLocation() {
        Version latestVersion = getLatestVersion();
        JSONObject versionDetails = getVersionDetails(latestVersion);
        if(!versionDetails.has("dist")){
            return "";
        }

        JSONObject dist = versionDetails.getJSONObject("dist");
        if(!dist.has("tarball")){
            return "";
        }
        return dist.getString("tarball");
    }

    private String getAuthor() {
        Version latestVersion = getLatestVersion();
        JSONObject versionDetails = getVersionDetails(latestVersion);
        if(!versionDetails.has("author")){
            return "";
        }
        Object authorObject = versionDetails.get("author");
        if(authorObject instanceof String){
            return authorObject.toString();
        }
        if(authorObject instanceof JSONObject){
            JSONObject author = (JSONObject) authorObject;
            if(author.has("name")) {
                return author.getString("name");
            }
        }

        return "";
    }

    private JSONObject getVersionDetails(Version version) {
        String versionString = getVersionString(version);
        return feedObject.getJSONObject("versions").getJSONObject(versionString);
    }

    private Date getVersionModifiedDate(Version version){
        String versionString = getVersionString(version);
        JSONObject time = feedObject.getJSONObject("time");
        String versionModifiedString = time.getString(versionString);

        return javax.xml.bind.DatatypeConverter.parseDateTime(versionModifiedString).getTime();
    }

    private Date getPublishedDate() {
        Version latestVersion = getLatestVersion();
        return getVersionModifiedDate(latestVersion);
    }

    private String getEntryTitle() {
        return feedObject.get("name").toString();
    }

    private String getPackageVersion() {
        return getVersionString(getLatestVersion());
    }

    private Version getLatestVersion(){
        return versionsList.get(0);
    }

    private String getVersionString(Version version){
        return versionStringMap.get(version);
    }

    private void populateVersionsList(){

        JSONObject versions = feedObject.getJSONObject("versions");
        Iterator keysIterator = versions.keys();
        versionsList = new ArrayList<Version>();

        versionStringMap = new HashMap<Version, String>() ;
        while (keysIterator.hasNext()) {
            String versionString = (String)keysIterator.next();
            Version currentVersion = Version.valueOf(versionString);
            if(isWithinBounds(currentVersion)) {
                versionStringMap.put(currentVersion, versionString);
                versionsList.add(currentVersion);
            }
        }
        Collections.sort(versionsList, Collections.reverseOrder());
    }

    private boolean isWithinBounds(Version currentVersion) {
        if(lowerBoundVersion != null && lowerBoundVersion.compareTo(currentVersion) > 0 ){
            return false;
        }
        if(upperBoundVersion != null && upperBoundVersion.compareTo(currentVersion) <= 0 ){
            return false;
        }

        if(lastKnownVersion != null && lastKnownVersion.compareTo(currentVersion) >= 0 ){
            return false;
        }
        return true;
    }

    public PackageRevision getPackageRevision() {

        if(versionsList.isEmpty()){
            if(lastKnownVersion != null) return null;
            else throw new NpmException("No such package found");
        }
        PackageRevision result = new PackageRevision(getPackageVersion(), getPublishedDate(), getAuthor());
        result.addData(NpmPackageConfig.PACKAGE_LOCATION, getPackageLocation());
        result.addData(NpmPackageConfig.PACKAGE_VERSION, getPackageVersion());
        return result;
    }

    private String getPackageLabel() {
        return getEntryTitle() + "-" + getPackageVersion();
    }
}
