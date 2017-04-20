/*
 *  GeoServer-Manager - Simple Manager Library for GeoServer
 *
 *  Copyright (C) 2007,2013 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package it.geosolutions.geoserver.rest.cas.manager;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.Util;
import it.geosolutions.geoserver.rest.cas.CASHTTPUtils;
import it.geosolutions.geoserver.rest.decoder.RESTStyle;
import it.geosolutions.geoserver.rest.decoder.RESTStyleList;
import it.geosolutions.geoserver.rest.manager.GeoServerRESTStyleManager;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class GeoServerCASRESTStyleManager extends GeoServerRESTStyleManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeoServerRESTStyleManager.class);
    private Assertion casAssertion;

    /**
     * Default constructor.
     *
     * @param restURL GeoServer REST API endpoint
     * @param username GeoServer REST API authorized username
     * @param password GeoServer REST API password for the former username
     */
    public GeoServerCASRESTStyleManager(URL restURL, String username, String password)
            throws IllegalArgumentException {
        super(restURL, username, password);
    }

    /**
     * Check if a Style exists in the configured GeoServer instance. User can
     * choose if log a possible exception or not
     *
     * @param name the name of the style to check for.
     * @param quietOnNotFound if true, mute exception if false is returned
     * @return <TT>true</TT> on HTTP 200, <TT>false</TT> on HTTP 404
     * @throws RuntimeException if any other HTTP code than 200 or 404 was
     * retrieved.
     */
    @Override
    public boolean existsStyle(String name, Boolean quietOnNotFound) {
        String url = buildXmlUrl(null, name);
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, gsuser, gspass);
    }

    /**
     * Get summary info about all Styles.
     *
     * @return summary info about Styles as a {@link RESTStyleList}
     */
    @Override
    public RESTStyleList getStyles() {
        String url = "/rest/styles.xml";
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("### Retrieving Styles list from " + url);
        }

        String response = CASHTTPUtils.get(gsBaseUrl + url, gsuser, gspass);
        return RESTStyleList.build(response);
    }

    @Override
    public RESTStyle getStyle(String name) {
        String url = buildXmlUrl(null, name);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("### Retrieving Style " + name + " from " + url);
        }

        String response = CASHTTPUtils.get(url, gsuser, gspass);
        return RESTStyle.build(response);
    }

    /**
     * Get the SLD body of a Style.
     */
    @Override
    public String getSLD(String styleName) {
        String url = buildUrl(null, styleName, ".sld");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("### Retrieving SLD body from " + url);
        }
        return CASHTTPUtils.get(url, gsuser, gspass);
    }

    //=========================================================================
    // Workspaces
    //=========================================================================
    /**
     *
     * @since GeoServer 2.6
     */
    @Override
    public boolean existsStyle(String workspace, String name, boolean quietOnNotFound) {
        String url = buildXmlUrl(workspace, name);
        String composed = Util.appendQuietOnNotFound(quietOnNotFound, url);
        return CASHTTPUtils.exists(composed, gsuser, gspass);
    }

    /**
     * Get summary info about Styles in a workspace.
     *
     * @return summary info about Styles as a {@link RESTStyleList}
     * @since GeoServer 2.2
     */
    @Override
    public RESTStyleList getStyles(String workspace) {
        String url = "/rest/workspaces/" + workspace + "/styles.xml";
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("### Retrieving Styles list from " + url);
        }

        String response = CASHTTPUtils.get(gsBaseUrl + url, gsuser, gspass);
        return RESTStyleList.build(response);
    }

    /**
     *
     * @since GeoServer 2.2
     */
    @Override
    public RESTStyle getStyle(String workspace, String name) {
        String url = buildXmlUrl(workspace, name);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("### Retrieving Style " + name + " from " + url);
        }

        String response = CASHTTPUtils.get(url, gsuser, gspass);
        return RESTStyle.build(response);
    }

    /**
     * Get the SLD body of a Style.
     *
     * @since GeoServer 2.2
     */
    @Override
    public String getSLD(String workspace, String name) {
        String url = buildUrl(workspace, name, ".sld");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("### Retrieving SLD body from " + url);
        }
        return CASHTTPUtils.get(url, gsuser, gspass);
    }

    //=========================================================================
    // Publishing
    //=========================================================================
    /**
     * Store and publish a Style.
     *
     * @param sldBody the full SLD document as a String.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     */
    @Override
    public boolean publishStyle(String sldBody) {
        /*
         * This is the equivalent call with cUrl:
         *
         * {@code curl -u admin:geoserver -XPOST \ -H 'Content-type: application/vnd.ogc.sld+xml' \ -d @$FULLSLD \
         * http://$GSIP:$GSPORT/$SERVLET/rest/styles}
         */
        try {
            return publishStyle(sldBody, null);
        } catch (IllegalArgumentException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    /**
     * Store and publish a Style, assigning it a name.
     *
     * @param sldBody the full SLD document as a String.
     * @param name the Style name.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if the style body is null or empty.
     */
    @Override
    public boolean publishStyle(final String sldBody, final String name)
            throws IllegalArgumentException {
        /*
         * This is the equivalent call with cUrl:
         *
         * {@code curl -u admin:geoserver -XPOST \ -H 'Content-type: application/vnd.ogc.sld+xml' \ -d @$FULLSLD \
         * http://$GSIP:$GSPORT/$SERVLET/rest/styles?name=name}
         */
        if (sldBody == null || sldBody.isEmpty()) {
            throw new IllegalArgumentException("The style body may not be null or empty");
        }

        String sUrl = buildPostUrl(null, name);

        final String result = CASHTTPUtils.post(sUrl, sldBody, "application/vnd.ogc.sld+xml", gsuser, gspass);
        return result != null;
    }

    /**
     * Store and publish a Style.
     *
     * @param sldFile the File containing the SLD document.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     */
    @Override
    public boolean publishStyle(File sldFile) {
        return publishStyle(sldFile, null);
    }

    /**
     * Store and publish a Style, assigning it a name.
     *
     * @param sldFile the File containing the SLD document.
     * @param name the Style name.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     */
    @Override
    public boolean publishStyle(File sldFile, String name) {
        String sUrl = buildPostUrl(null, name);
        LOGGER.debug("POSTing new style " + name + " to " + sUrl);
        String result = CASHTTPUtils.post(sUrl, sldFile, GeoServerRESTPublisher.Format.SLD.getContentType(), gsuser, gspass);
        return result != null;
    }
    
    /**
     * Store and publish a Style, assigning it a name and choosing the raw
     * format.
     *
     * @param sldBody the full SLD document as a String.
     * @param name the Style name.
     * @param raw the raw format
     *
     * @return <TT>true</TT> if the operation completed successfully.
     */
    @Override
    public boolean publishStyle(final String sldBody, final String name, final boolean raw) {
        /*
         * This is the equivalent call with cUrl:
         *
         * {@code curl -u admin:geoserver -XPOST \ -H 'Content-type: application/vnd.ogc.sld+xml' \ -d @$FULLSLD \
         * http://$GSIP:$GSPORT/$SERVLET/rest/styles?name=$name&raw=$raw}
         */
        if (sldBody == null || sldBody.isEmpty()) {
            throw new IllegalArgumentException("The style body may not be null or empty");
        }
        
        StringBuilder sUrl = new StringBuilder(buildPostUrl(null, name));
        Util.appendParameter(sUrl, "raw", ""+raw);
        String contentType = GeoServerRESTPublisher.Format.SLD.getContentType();
        if(!super.checkSLD10Version(sldBody)){
            contentType = GeoServerRESTPublisher.Format.SLD_1_1_0.getContentType();
        }
        LOGGER.debug("POSTing new style " + name + " to " + sUrl + " using version: " + contentType);
        String result = CASHTTPUtils.post(sUrl.toString(), sldBody, contentType, gsuser, gspass);
        return result != null;
    }

     /**
     * Store and publish a Style, assigning it a name and choosing the raw
     * format.
     *
     * @param sldFile the File containing the SLD document.
     * @param name the Style name.
     * @param raw the raw format
     *
     * @return <TT>true</TT> if the operation completed successfully.
     */
    @Override
    public boolean publishStyle(final File sldFile, final String name, final boolean raw) {
        /*
         * This is the equivalent call with cUrl:
         *
         * {@code curl -u admin:geoserver -XPOST \ -H 'Content-type: application/vnd.ogc.sld+xml' \ -d @$FULLSLD \
         * http://$GSIP:$GSPORT/$SERVLET/rest/styles?name=$name&raw=$raw}
         */
        StringBuilder sUrl = new StringBuilder(buildPostUrl(null, name));
        Util.appendParameter(sUrl, "raw", ""+raw);
        String contentType = GeoServerRESTPublisher.Format.SLD.getContentType();
        if(!super.checkSLD10Version(sldFile)){
            contentType = GeoServerRESTPublisher.Format.SLD_1_1_0.getContentType();
        }
        LOGGER.debug("POSTing new style " + name + " to " + sUrl + " using version: " + contentType);
        String result = CASHTTPUtils.post(sUrl.toString(), sldFile, contentType, gsuser, gspass);
        return result != null;
    }
    
    /**
     * Update a Style.
     * 
     * @param sldFile the File containing the SLD document.
     * @param name the Style name.
     * @param raw the raw format
     * 
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if the sldFile file or name are null or name is empty.
     */
    @Override
    public boolean updateStyle(final File sldFile, final String name, final boolean raw) 
            throws IllegalArgumentException {
        /*
         * This is the equivalent call with cUrl:
         *
         * {@code curl -u admin:geoserver -XPUT \ -H 'Content-type: application/vnd.ogc.sld+xml' \ -d @$FULLSLD \
         * http://$GSIP:$GSPORT/$SERVLET/rest/styles?name=$name&raw=$raw}
         */
        if (sldFile == null) {
            throw new IllegalArgumentException("Unable to updateStyle using a null parameter file");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The style name may not be null or empty");
        }
        
        StringBuilder sUrl = new StringBuilder(buildUrl(null, name, null));
        Util.appendParameter(sUrl, "raw", ""+raw);
        String contentType = GeoServerRESTPublisher.Format.SLD.getContentType();
        if(!super.checkSLD10Version(sldFile)){
            contentType = GeoServerRESTPublisher.Format.SLD_1_1_0.getContentType();
        }
        LOGGER.debug("PUTting style " + name + " to " + sUrl + " using version: " + contentType);
        String result = CASHTTPUtils.put(sUrl.toString(), sldFile, contentType, gsuser, gspass);
        return result != null;
    }
    
    /**
     * Update a Style.
     * 
     * @param sldBody the new SLD document as a String.
     * @param name the Style name.
     * @param raw the raw format
     * 
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if the style body or name are null or empty.
     */
    @Override
    public boolean updateStyle(final String sldBody, final String name, final Boolean raw)
            throws IllegalArgumentException {
        /*
         * This is the equivalent call with cUrl:
         *
         * {@code curl -u admin:geoserver -XPUT \ -H 'Content-type: application/vnd.ogc.sld+xml' \ -d @$FULLSLD \
         * http://$GSIP:$GSPORT/$SERVLET/rest/styles?name=$name&raw=$raw}
         */
        if (sldBody == null || sldBody.isEmpty()) {
            throw new IllegalArgumentException("The style body may not be null or empty");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The style name may not be null or empty");
        }
        
        StringBuilder sUrl = new StringBuilder(buildUrl(null, name, null));
        Util.appendParameter(sUrl, "raw", ""+raw);
        String contentType = GeoServerRESTPublisher.Format.SLD.getContentType();
        if(!super.checkSLD10Version(sldBody)){
            contentType = GeoServerRESTPublisher.Format.SLD_1_1_0.getContentType();
        }
        LOGGER.debug("PUTting style " + name + " to " + sUrl + " using version: " + contentType);
        String result = CASHTTPUtils.put(sUrl.toString(), sldBody, contentType, gsuser, gspass);
        return result != null;
    }

    /**
     * Update a Style.
     *
     * @param sldBody the new SLD document as a String.
     * @param name the Style name to update.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if the style body or name are null or
     * empty.
     */
    @Override
    public boolean updateStyle(final String sldBody, final String name)
            throws IllegalArgumentException {
        /*
         * This is the equivalent call with cUrl:
         *
         * {@code curl -u admin:geoserver -XPUT \ -H 'Content-type: application/vnd.ogc.sld+xml' \ -d @$FULLSLD \
         * http://$GSIP:$GSPORT/$SERVLET/rest/styles/$NAME}
         */
        if (sldBody == null || sldBody.isEmpty()) {
            throw new IllegalArgumentException("The style body may not be null or empty");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The style name may not be null or empty");
        }

        final String sUrl = buildUrl(null, name, null);

        final String result = CASHTTPUtils.put(sUrl, sldBody, "application/vnd.ogc.sld+xml", gsuser, gspass);
        return result != null;
    }

    /**
     * Update a Style.
     *
     * @param sldFile the File containing the SLD document.
     * @param name the Style name.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if the sldFile file or name are null or
     * name is empty.
     */
    @Override
    public boolean updateStyle(final File sldFile, final String name)
            throws IllegalArgumentException {

        if (sldFile == null) {
            throw new IllegalArgumentException("Unable to updateStyle using a null parameter file");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The style name may not be null or empty");
        }

        final String sUrl = buildUrl(null, name, null);

        final String result = CASHTTPUtils.put(sUrl, sldFile,
                "application/vnd.ogc.sld+xml", gsuser, gspass);
        return result != null;

    }

    /**
     * Remove a Style.
     * <P>
     * The Style will be unpublished, and (optionally) the SLD file will be
     * removed.
     *
     * @param styleName the name of the Style to remove.
     * @param purge remove the related SLD file from disk.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if styleName is null or empty.
     */
    @Override
    public boolean removeStyle(String styleName, final boolean purge)
            throws IllegalArgumentException {
        if (styleName == null || styleName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Check styleName parameter, it may never be null or empty");
        }

        // check style name
        // TODO may we want to throw an exception instead of
        // change style name?
        if (styleName.contains(":")) {
            LOGGER.warn("Style name is going to be changed [" + styleName + "]");
        }
        styleName = styleName.replaceAll(":", "_");

        // currently REST interface does't support URLencoded URL 
//        styleName = URLEncoder.encode(styleName);
        String sUrl = buildUrl(null, styleName, null);
        if (purge) {
            sUrl += "?purge=true";
        }

        return CASHTTPUtils.delete(sUrl, gsuser, gspass);
    }

    /**
     * Remove a Style.
     * <P>
     * The Style will be unpublished and the related SLD file will be removed.
     *
     * @param styleName the name of the Style to remove.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     */
    @Override
    public boolean removeStyle(String styleName) {
        try {
            return removeStyle(styleName, true);
        } catch (IllegalArgumentException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    //=========================================================================
    // Publishing in workspace
    //=========================================================================
    /**
     * Store and publish a Style.
     *
     * @param sldBody the full SLD document as a String.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @since GeoServer 2.2
     */
    @Override
    public boolean publishStyleInWorkspace(final String workspace, String sldBody) {
        try {
            return publishStyleInWorkspace(workspace, sldBody);
        } catch (IllegalArgumentException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    /**
     * Store and publish a Style, assigning it a name.
     *
     * @param sldBody the full SLD document as a String.
     * @param name the Style name.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if the style body is null or empty.
     * @since GeoServer 2.2
     */
    @Override
    public boolean publishStyleInWorkspace(final String workspace, final String sldBody, final String name)
            throws IllegalArgumentException {

        if (sldBody == null || sldBody.isEmpty()) {
            throw new IllegalArgumentException("The style body may not be null or empty");
        }
        String sUrl = buildPostUrl(workspace, name);
        final String result = CASHTTPUtils.post(sUrl, sldBody, "application/vnd.ogc.sld+xml", gsuser, gspass);
        return result != null;
    }

    /**
     * Store and publish a Style.
     *
     * @param sldFile the File containing the SLD document.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @since GeoServer 2.2
     */
    @Override
    public boolean publishStyleInWorkspace(final String workspace, File sldFile) {
        return publishStyleInWorkspace(workspace, sldFile, null);
    }

    /**
     * Store and publish a Style, assigning it a name.
     *
     * @param sldFile the File containing the SLD document.
     * @param name the Style name.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @since GeoServer 2.2
     */
    @Override
    public boolean publishStyleInWorkspace(final String workspace, File sldFile, String name) {
        String sUrl = buildPostUrl(workspace, name);
        LOGGER.debug("POSTing new style " + name + " to " + sUrl);
        String result = CASHTTPUtils.post(sUrl, sldFile, GeoServerRESTPublisher.Format.SLD.getContentType(), gsuser, gspass);
        return result != null;
    }

    /**
     * Update a Style.
     *
     * @param sldBody the new SLD document as a String.
     * @param name the Style name to update.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if the style body or name are null or
     * empty.
     * @since GeoServer 2.2
     */
    @Override
    public boolean updateStyleInWorkspace(final String workspace, final String sldBody, final String name)
            throws IllegalArgumentException {
        if (sldBody == null || sldBody.isEmpty()) {
            throw new IllegalArgumentException("The style body may not be null or empty");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The style name may not be null or empty");
        }

        final String sUrl = buildUrl(workspace, name, null);

        final String result = CASHTTPUtils.put(sUrl, sldBody,
                "application/vnd.ogc.sld+xml", gsuser, gspass);
        return result != null;
    }

    /**
     * Update a Style.
     *
     * @param sldFile the File containing the SLD document.
     * @param name the Style name.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if the sldFile file or name are null or
     * name is empty.
     * @since GeoServer 2.2
     */
    @Override
    public boolean updateStyleInWorkspace(final String workspace, final File sldFile, final String name)
            throws IllegalArgumentException {

        if (sldFile == null) {
            throw new IllegalArgumentException("Unable to updateStyle using a null parameter file");
        } else if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("The style name may not be null or empty");
        }

        final String sUrl = buildUrl(workspace, name, null);

        final String result = CASHTTPUtils.put(sUrl, sldFile,
                "application/vnd.ogc.sld+xml", gsuser, gspass);
        return result != null;
    }

    /**
     * Remove a Style.
     * <P>
     * The Style will be unpublished, and (optionally) the SLD file will be
     * removed.
     *
     * @param styleName the name of the Style to remove.
     * @param purge remove the related SLD file from disk.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @throws IllegalArgumentException if styleName is null or empty.
     * @since GeoServer 2.2
     */
    @Override
    public boolean removeStyleInWorkspace(final String workspace, String styleName, final boolean purge)
            throws IllegalArgumentException {
        if (styleName == null || styleName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Check styleName parameter, it may never be null or empty");
        }

        // check style name
        // TODO may we want to throw an exception instead of change style name?
        if (styleName.contains(":")) {
            LOGGER.warn("Style name is going to be changed [" + styleName + "]");
        }
        styleName = styleName.replaceAll(":", "_");
        styleName = URLEncoder.encode(styleName);

        String sUrl = buildUrl(workspace, styleName, null);

        if (purge) {
            sUrl += "?purge=true";
        }

        return CASHTTPUtils.delete(sUrl, gsuser, gspass);
    }

    /**
     * Remove a Style.
     * <P>
     * The Style will be unpublished and the related SLD file will be removed.
     *
     * @param styleName the name of the Style to remove.
     *
     * @return <TT>true</TT> if the operation completed successfully.
     * @since GeoServer 2.2
     */
    @Override
    public boolean removeStyleInWorkspace(final String workspace, String styleName) {
        try {
            return removeStyleInWorkspace(workspace, styleName, true);
        } catch (IllegalArgumentException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
        return false;
    }

    public Assertion getCasAssertion() {
        return casAssertion;
    }

    public void setCasAssertion(Assertion casAssertion) {
        this.casAssertion = casAssertion;
        CASHTTPUtils.setCasAssertion(casAssertion);
    }
}
