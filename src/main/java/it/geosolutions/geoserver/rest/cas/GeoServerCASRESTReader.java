/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.geosolutions.geoserver.rest.cas;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.HTTPUtils;
import it.geosolutions.geoserver.rest.Util;
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

    /**
     * Checks if the selected DataStore is present. Parameter quietOnNotFound
     * can be used for controlling the logging when 404 is returned.
     *
     * @param workspace workspace of the datastore
     * @param dsName name of the datastore
     * @param quietOnNotFound if true, no exception is logged
     * @return boolean indicating if the datastore exists
     */
    @Override
    public boolean existsDatastore(String workspace, String dsName, boolean quietOnNotFound) {
        String url = baseurl + "/rest/workspaces/" + workspace + "/datastores/" + dsName + ".xml";
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, username, password);
    }

    /**
     * Checks if the selected FeatureType is present. Parameter quietOnNotFound
     * can be used for controlling the logging when 404 is returned.
     *
     * @param workspace workspace of the datastore
     * @param dsName name of the datastore
     * @param ftName name of the featuretype
     * @param quietOnNotFound if true, no exception is logged
     * @return boolean indicating if the featuretype exists
     */
    @Override
    public boolean existsFeatureType(String workspace, String dsName, String ftName, boolean quietOnNotFound) {
        String url = baseurl + "/rest/workspaces/" + workspace + "/datastores/" + dsName + "/featuretypes/" + ftName + ".xml";
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, username, password);
    }

    /**
     * Checks if the selected Coverage store is present. Parameter
     * quietOnNotFound can be used for controlling the logging when 404 is
     * returned.
     *
     * @param workspace workspace of the coveragestore
     * @param csName name of the coveragestore
     * @param quietOnNotFound if true, no exception is logged
     * @return boolean indicating if the coveragestore exists
     */
    @Override
    public boolean existsCoveragestore(String workspace, String csName, boolean quietOnNotFound) {
        String url = baseurl + "/rest/workspaces/" + workspace + "/coveragestores/" + csName + ".xml";
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, username, password);
    }

    /**
     * Checks if the selected Coverage is present. Parameter quietOnNotFound can
     * be used for controlling the logging when 404 is returned.
     *
     * @param workspace workspace of the coveragestore
     * @param store name of the coveragestore
     * @param name name of the coverage
     * @param quietOnNotFound if true, no exception is logged
     * @return boolean indicating if the coverage exists
     */
    @Override
    public boolean existsCoverage(String workspace, String store, String name, boolean quietOnNotFound) {
        String url = baseurl + "/rest/workspaces/" + workspace + "/coveragestores/" + store + "/coverages/" + name + ".xml";
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, username, password);
    }

    /**
     * Checks if the selected LayerGroup is present. Parameter quietOnNotFound
     * can be used for controlling the logging when 404 is returned.
     *
     * @param workspace workspace of the LayerGroup
     * @param name name of the LayerGroup
     * @param quietOnNotFound if true, no exception is logged
     * @return boolean indicating if the LayerGroup exists
     */
    @Override
    public boolean existsLayerGroup(String workspace, String name, boolean quietOnNotFound) {
        String url;
        if (workspace == null) {
            url = baseurl + "/rest/layergroups/" + name + ".xml";
        } else {
            url = baseurl + "/rest/workspaces/" + workspace + "/layergroups/" + name + ".xml";
        }
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, username, password);
    }

    /**
     * Checks if the selected Layer is present. Parameter quietOnNotFound can be
     * used for controlling the logging when 404 is returned.
     *
     * @param workspace workspace of the Layer
     * @param name name of the Layer
     * @param quietOnNotFound if true, no exception is logged
     * @return boolean indicating if the Layer exists
     */
    @Override
    public boolean existsLayer(String workspace, String name, boolean quietOnNotFound) {
        String url;
        if (workspace == null) {
            url = baseurl + "/rest/layers/" + name + ".xml";
        } else {
            url = baseurl + "/rest/layers/" + workspace + ":" + name + ".xml";
        }
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, username, password);
    }

    /**
     * Checks if the selected Namespace is present. Parameter quietOnNotFound
     * can be used for controlling the logging when 404 is returned.
     *
     * @param prefix namespace prefix.
     * @param quietOnNotFound if true, no exception is logged
     * @return boolean indicating if the Namespace exists
     */
    @Override
    public boolean existsNamespace(String prefix, boolean quietOnNotFound) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Namespace prefix cannot be null or empty");
        }
        String url = baseurl + "/rest/namespaces/" + prefix + ".xml";
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, username, password);
    }

    /**
     * Checks if the selected Workspace is present. Parameter quietOnNotFound
     * can be used for controlling the logging when 404 is returned.
     *
     * @param prefix Workspace prefix.
     * @param quietOnNotFound if true, no exception is logged
     * @return boolean indicating if the Workspace exists
     */
    @Override
    public boolean existsWorkspace(String prefix, boolean quietOnNotFound) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("Workspace prefix cannot be null or empty");
        }
        String url = baseurl + "/rest/workspaces/" + prefix + ".xml";
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, username, password);
    }

    /**
     * Checks if the selected Granule is present. Parameter quietOnNotFound can
     * be used for controlling the logging when 404 is returned.
     *
     * @param workspace workspace of the coveragestore
     * @param coverageStore name of the coveragestore
     * @param coverage name of the coverage
     * @param id id of the granule
     * @param quietOnNotFound if true, no exception is logged
     * @return boolean indicating if the Granule exists
     */
    @Override
    public boolean existsGranule(String workspace, String coverageStore, String coverage,
            String id, boolean quietOnNotFound) {
        String url = baseurl + "/rest/workspaces/" + workspace + "/coveragestores/" + coverageStore
                + "/coverages/" + coverage + "/index/granules/" + id + ".xml";
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, username, password);
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
