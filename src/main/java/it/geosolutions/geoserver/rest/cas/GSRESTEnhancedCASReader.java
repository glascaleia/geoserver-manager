/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.geosolutions.geoserver.rest.cas;

import it.geosolutions.geoserver.rest.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group
 * @email nazzareno.sileno@geosdi.org
 */
public class GSRESTEnhancedCASReader {

    /**
     * The logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(GeoServerRESTPublisher.class);
    /**
     * GeoServer instance base URL. E.g.:
     * <TT>http://localhost:8080/geoserver</TT>.
     */
    private String restURL;
    /**
     * GeoServer instance privileged username, with read & write permission on
     * REST API
     */
    private String gsuser;
    /**
     * GeoServer instance password for privileged username with r&w permission
     * on REST API
     */
    private String gspass;
    //
    private DefaultHttpClient httpClient;
    private HttpContext localContext;

    /**
     * Creates a <TT>GeoServerRESTPublisher</TT> to connect against a GeoServer
     * instance with the given URL and user credentials.
     *
     * @param restURL the base GeoServer URL (e.g.:
     * <TT>http://localhost:8080/geoserver</TT>)
     * @param username auth credential
     * @param password auth credential
     */
    public GSRESTEnhancedCASReader(String restURL, String username, String password) {
        this.restURL = CASHTTPUtils.decurtSlash(restURL);
        this.gsuser = username;
        this.gspass = password;
        this.afterPropertiesSet();
    }

    public GSRESTEnhancedCASReader() {
    }

    private void afterPropertiesSet() {
        localContext = new BasicHttpContext();
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 60000);
        HttpConnectionParams.setSoTimeout(params, 60000);
        this.httpClient = new DefaultHttpClient(params);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(this.gsuser,
                this.gspass));
        httpClient.setCredentialsProvider(credsProvider);
    }

    /**
     *
     * @return it returns the Json array rappresenting the GS service list
     */
    public String loadGSServices() {
        String urlToCall = restURL + "/rest/services/load/all";
        String jsonToParse = null;
        jsonToParse = CASHTTPUtils.get(urlToCall, this.gsuser, this.gspass);
        logger.debug("Json received: " + jsonToParse);
        jsonToParse = jsonToParse.substring(jsonToParse.indexOf('[') + 1,
                jsonToParse.lastIndexOf(']'));
        return jsonToParse;
    }

    public String loadAllGSServiceAccessRules() {
        String urlToCall = restURL + "/rest/servicesecurity/load/all";
        String jsonToParse = null;
        jsonToParse = CASHTTPUtils.get(urlToCall, this.gsuser, this.gspass);
        logger.debug("Json received: " + jsonToParse);
        jsonToParse = jsonToParse.substring(jsonToParse.indexOf('[') + 1,
                jsonToParse.lastIndexOf(']'));
        return jsonToParse;
    }

    public String loadAllGSDataSecurityRules() {
        String urlToCall = restURL + "/rest/datasecurity/load/all";
        String jsonToParse = null;
        jsonToParse = CASHTTPUtils.get(urlToCall, this.gsuser, this.gspass);
        logger.debug("Json received: " + jsonToParse);
        jsonToParse = jsonToParse.substring(jsonToParse.indexOf('[') + 1,
                jsonToParse.lastIndexOf(']'));
        return jsonToParse;
    }

    /**
     *
     * @param serviceName
     * @return it returns the Json array rappresenting the GS service methods
     * list
     * @throws GeoPlatformException
     */
    public String loadGSServiceMethods(String serviceName) {
//        HttpPost post = new HttpPost(urlToCall);
//        List<NameValuePair> nameValuePairs = Lists.<NameValuePair>newArrayList();
//        nameValuePairs.add(new BasicNameValuePair("serviceName",
//                serviceName));
//        try {
//            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//            HttpResponse response = httpClient.execute(post, this.localContext);
//
//            System.out.println("Response: " + response);
//            
//            jsonToParse = this.generateStringFromResponse(response);
//
////            this.manageHttpResponse(response);
//        } catch (IOException e) {
//            logger.error("Error on loading service methods: " + e.getMessage());
//            throw new GeoPlatformException("Load GS Service Methods Error: " + e);
//        }
        String urlToCall = this.restURL + "/rest/services/load/methods?serviceName=" + serviceName.trim();
        String jsonToParse = null;
        jsonToParse = CASHTTPUtils.get(urlToCall, this.gsuser, this.gspass);
        logger.info("Json received: " + jsonToParse);
        jsonToParse = jsonToParse.substring(jsonToParse.indexOf('[') + 1,
                jsonToParse.lastIndexOf(']'));
        logger.debug("Json to parse: " + jsonToParse);
        return jsonToParse;
    }

    private String generateStringFromResponse(HttpResponse response) throws IOException {
        InputStream is = response.getEntity().getContent();
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(
                    new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }
}
