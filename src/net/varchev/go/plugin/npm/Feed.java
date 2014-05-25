package net.varchev.go.plugin.npm;

import com.thoughtworks.go.plugin.api.logging.Logger;
import com.tw.go.plugin.util.HttpRepoURL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class Feed {
    private static Logger LOGGER = Logger.getLoggerFor(Feed.class);
    private final String url;
    public Feed(String url) {
        this.url = url;
    }

    public JSONObject download() {
        DefaultHttpClient client = HttpRepoURL.getHttpClient();
        HttpGet method = new HttpGet(url);
        method.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10 * 1000);
        try {
            HttpResponse response = client.execute(method);
            if(response.getStatusLine().getStatusCode() == 404) {
                throw new NpmException("No such package found");
            }
            else if(response.getStatusLine().getStatusCode() != 200){
                throw new RuntimeException(String.format("HTTP %s, %s",
                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }

            HttpEntity entity = response.getEntity();
            String responseBody = EntityUtils.toString(entity);

            LOGGER.info(responseBody);

            JSONObject result = new JSONObject(responseBody);



            return result;
        } catch (NpmException ex) {
            throw ex;
        } catch (Exception ex) {
            String message = String.format("%s (%s) while getting package feed for : %s ", ex.getClass().getSimpleName(), ex.getMessage(), url);
            LOGGER.error(message);
            throw new RuntimeException(message, ex);
        } finally {
            method.releaseConnection();
            client.getConnectionManager().shutdown();
        }
    }

}


