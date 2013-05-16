/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.geosolutions.geoserver.rest;

import com.google.common.collect.Lists;
import it.geosolutions.geoserver.rest.decoder.RESTDataAccessRule;
import it.geosolutions.geoserver.rest.decoder.RESTServiceAccessRule;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.List;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group
 * @email nazzareno.sileno@geosdi.org
 */
public class GSRESTEnhancedPublisher {

    /**
     * The logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(GeoServerRESTPublisher.class);
    /**
     * GeoServer instance base URL. E.g.:
     * <TT>http://localhost:8080/geoserver</TT>.
     */
    private final String restURL;
    /**
     * GeoServer instance privileged username, with read & write permission on
     * REST API
     */
    private final String gsuser;
    /**
     * GeoServer instance password for privileged username with r&w permission
     * on REST API
     */
    private final String gspass;
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
    public GSRESTEnhancedPublisher(String restURL, String username, String password) {
        this.restURL = HTTPUtils.decurtSlash(restURL);
        this.gsuser = username;
        this.gspass = password;
        this.afterPropertiesSet();
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

    public String removeGSRule(String rulePathToRemove)
            throws MalformedURLException {
        String result = null;
        String urlToCall = this.restURL + "/rest/datasecurity/remove/" + rulePathToRemove;
        try {
            result = HTTPUtils.get(urlToCall, this.gsuser, this.gspass);
        } catch (MalformedURLException murle) {
            logger.error("Error on removing GSRule: " + murle.getMessage());
            throw new MalformedURLException("Remove GS Rule Error: " + murle);
        }
        return result;
    }

    public Boolean deleteGSServiceAccessRules(List<String> rulePathToRemove)
            throws IOException {
        boolean result = Boolean.FALSE;
        String urlToCall = this.restURL + "/rest/servicesecurity/remove";
        HttpPost post = new HttpPost(urlToCall);
        List<NameValuePair> nameValuePairs = Lists.<NameValuePair>newArrayList();
        for (String rulePath : rulePathToRemove) {
            nameValuePairs.add(new BasicNameValuePair("rulePath",
                    rulePath));
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpClient.execute(post, this.localContext);
            this.manageHttpResponse(response);
            result = Boolean.TRUE;
        } catch (IOException e) {
            logger.error("Error on deleting service access rules: " + e.getMessage());
            throw new IOException("Delete GS Service Access Rule Error: " + e);
        }
        return result;
    }

    public Boolean deleteGSDataAccessRules(List<String> rulePathToRemove)
            throws IOException {
        boolean result = Boolean.FALSE;
        String urlToCall = restURL + "/rest/datasecurity/remove";
        HttpPost post = new HttpPost(urlToCall);
        List<NameValuePair> nameValuePairs = Lists.<NameValuePair>newArrayList();
        for (String rulePath : rulePathToRemove) {
            nameValuePairs.add(new BasicNameValuePair("rulePath",
                    rulePath));
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpClient.execute(post, this.localContext);
            this.manageHttpResponse(response);
            result = Boolean.TRUE;
        } catch (IOException e) {
            logger.error("Error on deleting data access rules: " + e.getMessage());
            throw new IOException("Delete GS Data Access Rule Error: " + e);
        }
        return result;
    }

    public Boolean saveGSServiceAccessRules(List<RESTServiceAccessRule> accessRulesToSave,
            boolean override)
            throws IOException {
        boolean result = Boolean.FALSE;
        String urlToCall = restURL + "/rest/servicesecurity/save";
        HttpPost post = new HttpPost(urlToCall);
        List<NameValuePair> nameValuePairs = Lists.<NameValuePair>newArrayList();
        for (RESTServiceAccessRule accessRule : accessRulesToSave) {
            nameValuePairs.add(new BasicNameValuePair("service",
                    accessRule.getService()));
            nameValuePairs.add(new BasicNameValuePair("method",
                    accessRule.getMethod()));
            nameValuePairs.add(new BasicNameValuePair("override",
                    "" + override));
            nameValuePairs.add(new BasicNameValuePair("previousRuleKey",
                    accessRule.getRulePath()));
            StringBuilder stringBuilder = new StringBuilder();
            for (String role : accessRule.getRoles()) {
                stringBuilder.append(role);
                stringBuilder.append("%2c");//%2c == ,
            }
            nameValuePairs.add(new BasicNameValuePair("roles",
                    stringBuilder.toString()));
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpClient.execute(post, this.localContext);
            this.manageHttpResponse(response);
            result = Boolean.TRUE;
        } catch (IOException e) {
            logger.error("Error on saving Service access rules: " + e.getMessage());
            throw new IOException("Save GS Service Access Rule Error: " + e);
        }
        return result;
    }

    public Boolean saveGSDataAccessRules(List<RESTDataAccessRule> accessRulesToSave,
            boolean override)
            throws IOException {
        boolean result = Boolean.FALSE;
        String urlToCall = restURL + "/rest/datasecurity/save";
        HttpPost post = new HttpPost(urlToCall);
        List<NameValuePair> nameValuePairs = Lists.<NameValuePair>newArrayList();
        for (RESTDataAccessRule accessRule : accessRulesToSave) {
            nameValuePairs.add(new BasicNameValuePair("workspace",
                    accessRule.getWorkspace()));
            nameValuePairs.add(new BasicNameValuePair("layer",
                    accessRule.getLayer()));
            nameValuePairs.add(new BasicNameValuePair("accessMode",
                    accessRule.getAccessMode()));
            nameValuePairs.add(new BasicNameValuePair("override",
                    "" + override));
            nameValuePairs.add(new BasicNameValuePair("previousRuleKey",
                    accessRule.getRulePath()));
            StringBuilder stringBuilder = new StringBuilder();
            for (String role : accessRule.getRoles()) {
                stringBuilder.append(role);
                stringBuilder.append("%2c");//%2c == ,
            }
            nameValuePairs.add(new BasicNameValuePair("roles",
                    stringBuilder.toString()));
        }
        try {
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpClient.execute(post, this.localContext);
            this.manageHttpResponse(response);
            result = Boolean.TRUE;
        } catch (IOException e) {
            logger.error("Error on saving Data access rules: " + e.getMessage());
            throw new IOException("Save GS Data Access Rule Error: " + e);
        }
        return result;
    }

    private boolean manageHttpResponse(HttpResponse response) throws IOException {
        boolean result = false;
        StringBuilder stringBuilder = new StringBuilder();
        int statusCodeReceived = response.getStatusLine().getStatusCode();
        //TODO: Why we receive 405??
        if (statusCodeReceived == HttpStatus.SC_METHOD_NOT_ALLOWED
                || statusCodeReceived == HttpStatus.SC_OK
                || statusCodeReceived == HttpStatus.SC_CREATED
                || statusCodeReceived == HttpStatus.SC_ACCEPTED) {
            result = true;
        } else {
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                throw new IOException("Error: " + e + " - " + stringBuilder.toString());
            } finally {
                this.consumeResponseEntity(response.getEntity());
            }
            throw new IllegalArgumentException("Error: " + stringBuilder.toString());
        }
        this.consumeResponseEntity(response.getEntity());
        return result;
    }

    private void consumeResponseEntity(HttpEntity entity) {
        if (entity != null) {
            try {
                EntityUtils.consume(entity);
            } catch (IOException ex) {
                logger.error("Error on consuming data access rules: " + ex.getMessage());
            }
        }
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
