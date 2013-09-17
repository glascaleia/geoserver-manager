/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.geosolutions.geoserver.rest.cas;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.HTTPUtils;
import it.geosolutions.geoserver.rest.cas.manager.GeoServerCASRESTStyleManager;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jasig.cas.client.validation.Assertion;

/**
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group
 * @email nazzareno.sileno@geosdi.org
 */
public class GeoServerCASRESTReader extends GeoServerRESTReader {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeoServerCASRESTReader.class);
    private Assertion casAssertion;

    public GeoServerCASRESTReader() {
    }

    public GeoServerCASRESTReader(URL restUrl) {
        super(restUrl);
    }

    public GeoServerCASRESTReader(String restUrl) throws MalformedURLException {
        super(restUrl);
    }

    public GeoServerCASRESTReader(String restUrl, String username, String password) throws MalformedURLException {
        super(restUrl, username, password);
    }

    public GeoServerCASRESTReader(URL restUrl, String username, String password) {
        super(restUrl, username, password);
    }

    @Override
    protected String init(URL gsUrl, String username, String password) {
        System.out.println("Executing init from children");
        baseurl = super.init(gsUrl, username, password);
        System.out.println("Base url calculated: " + baseurl);

        super.styleManager = new GeoServerCASRESTStyleManager(gsUrl, username, password);

        return baseurl;
    }

    @Override
    protected String load(String url) {
        url = this.appendProxyTicketToURL(super.baseurl + url);
        LOGGER.info("Loading from REST path " + url);
        try {
            String response = HTTPUtils.get(url);
            return response;
        } catch (MalformedURLException ex) {
            LOGGER.warn("Bad URL", ex);
        }

        return null;
    }

    @Override
    protected String loadFullURL(String url) {
        url = this.appendProxyTicketToURL(url);
        LOGGER.info("Loading from REST path " + url);
        try {
            String response = HTTPUtils.get(url);
            return response;
        } catch (MalformedURLException ex) {
            LOGGER.warn("Bad URL", ex);
        }
        return null;
    }

    /**
     * @see GeoServerRESTReader.existGeoserver()
     */
    @Override
    public boolean existGeoserver() {
        return CASHTTPUtils.httpPing(baseurl + "/rest/");
    }

    public Assertion getCasAssertion() {
        return casAssertion;
    }

    public void setCasAssertion(Assertion casAssertion) {
        this.casAssertion = casAssertion;
        CASHTTPUtils.setCasAssertion(casAssertion);
        ((GeoServerCASRESTStyleManager) super.styleManager).setCasAssertion(casAssertion);
    }

    private String appendProxyTicketToURL(String url) {
        String proxyTicket = casAssertion.getPrincipal().getProxyTicketFor(url);
        if (proxyTicket == null || proxyTicket.isEmpty()) {
            throw new IllegalArgumentException("*********************** "
                    + "Impossible to obtain proxy ticket for URL: " + url);
        }
        try {
            char parameterSeparator = '?';
            if (url.contains("?")) {
                parameterSeparator = '&';
            }
            url += parameterSeparator + "ticket=" + URLEncoder.
                    encode(proxyTicket, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Error on encoding proxy ticket", ex);
        }
        return url;
    }
}
