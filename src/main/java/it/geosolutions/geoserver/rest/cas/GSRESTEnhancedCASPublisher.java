/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.geosolutions.geoserver.rest.cas;

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
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group
 * @email nazzareno.sileno@geosdi.org
 */
public class GSRESTEnhancedCASPublisher {

    /**
     * The logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(GSRESTEnhancedCASPublisher.class);
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

    /**
     * Creates a <TT>GeoServerRESTPublisher</TT> to connect against a GeoServer
     * instance with the given URL and user credentials.
     *
     * @param restURL the base GeoServer URL (e.g.:
     * <TT>http://localhost:8080/geoserver</TT>)
     * @param username auth credential
     * @param password auth credential
     */
    public GSRESTEnhancedCASPublisher(String restURL, String username, String password) {
        this.restURL = CASHTTPUtils.decurtSlash(restURL);
        this.gsuser = username;
        this.gspass = password;
    }

    public GSRESTEnhancedCASPublisher() {
    }

    public String removeGSRule(String rulePathToRemove)
            throws MalformedURLException {
        String result = null;
        String urlToCall = this.restURL + "/rest/datasecurity/remove/" + rulePathToRemove;
        try {
            result = CASHTTPUtils.get(urlToCall, this.gsuser, this.gspass);
        } catch (MalformedURLException murle) {
            logger.error("Error on removing GSRule: " + murle.getMessage());
            throw new MalformedURLException("Remove GS Rule Error: " + murle);
        }
        return result;
    }

    public Boolean deleteGSServiceAccessRules(List<String> rulePathToRemove)
            throws IOException {
        StringBuilder urlToCall = new StringBuilder(restURL);
        urlToCall.append("/rest/servicesecurity/remove");
        for (String rulePath : rulePathToRemove) {
            urlToCall.append("&rulePath=");
            urlToCall.append(rulePath);
        }
        String result = CASHTTPUtils.post(urlToCall.toString(), urlToCall.toString(), "text/xml", gsuser, gspass);
        return result != null;
    }

    public Boolean deleteGSDataAccessRules(List<String> rulePathToRemove)
            throws IOException {
        StringBuilder urlToCall = new StringBuilder(restURL);
        urlToCall.append("/rest/datasecurity/remove");
        for (String rulePath : rulePathToRemove) {
            urlToCall.append("&rulePath=");
            urlToCall.append(rulePath);
        }
        String result = CASHTTPUtils.post(urlToCall.toString(), urlToCall.toString(), "text/xml", gsuser, gspass);
        return result != null;
    }

    public Boolean saveGSServiceAccessRules(List<RESTServiceAccessRule> accessRulesToSave,
            boolean override)
            throws IOException {
        StringBuilder urlToCall = new StringBuilder(restURL);
        urlToCall.append("/rest/servicesecurity/save");
        for (RESTServiceAccessRule accessRule : accessRulesToSave) {
            urlToCall.append("&service=");
            urlToCall.append(accessRule.getService());

            urlToCall.append("?method=");
            urlToCall.append(accessRule.getMethod());

            urlToCall.append("?override=");
            urlToCall.append(override);

            urlToCall.append("?previousRuleKey=");
            urlToCall.append(accessRule.getRulePath());

            StringBuilder roleStringBuilder = new StringBuilder();
            for (String role : accessRule.getRoles()) {
                roleStringBuilder.append(role);
                roleStringBuilder.append("%2c");//%2c == ,
            }
            urlToCall.append("?roles=");
            urlToCall.append(roleStringBuilder.toString());
        }
        String result = CASHTTPUtils.post(urlToCall.toString(), urlToCall.toString(), "text/xml", gsuser, gspass);
        return result != null;
    }

    public Boolean saveGSDataAccessRules(List<RESTDataAccessRule> accessRulesToSave,
            boolean override)
            throws IOException {
        StringBuilder urlToCall = new StringBuilder(restURL);
        urlToCall.append("/rest/datasecurity/save");
        for (RESTDataAccessRule accessRule : accessRulesToSave) {
            urlToCall.append("?workspace=");
            urlToCall.append(accessRule.getWorkspace());
            urlToCall.append("&layer=");
            urlToCall.append(accessRule.getLayer());
            urlToCall.append("&accessMode=");
            urlToCall.append(accessRule.getAccessMode());
            urlToCall.append("&override=");
            urlToCall.append(override);
            urlToCall.append("&previousRuleKey=");
            urlToCall.append(accessRule.getRulePath());

            StringBuilder roleStringBuilder = new StringBuilder();
            for (String role : accessRule.getRoles()) {
                roleStringBuilder.append(role);
                roleStringBuilder.append("%2c");//%2c == ,
            }

            urlToCall.append("&roles=");
            urlToCall.append(roleStringBuilder.toString());

        }
        String result = CASHTTPUtils.post(urlToCall.toString(), urlToCall.toString(), "text/xml", gsuser, gspass);
        return result != null;
    }

    private boolean manageHttpResponse(HttpResponse response) throws IOException {
        boolean result = false;
        StringBuilder stringBuilder = new StringBuilder();
        int statusCodeReceived = response.getStatusLine().getStatusCode();
        //TODO: Why we receive 405??
        logger.info("Status code received: " + statusCodeReceived);
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
                throw new IOException("Error Consuming HttpResponse on GSRESTEnhancedPublisher: "
                        + e + " - " + stringBuilder.toString());
            } finally {
                this.consumeResponseEntity(response.getEntity());
            }
            throw new IllegalArgumentException("Error Managing HttpResponse on GSRESTEnhancedPublisher: "
                    + stringBuilder.toString());
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
