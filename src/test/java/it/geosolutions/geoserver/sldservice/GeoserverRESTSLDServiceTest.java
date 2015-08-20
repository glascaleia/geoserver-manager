/*
 *  GeoServer-Manager - Simple Manager Library for GeoServer
 *  
 *  Copyright (C) 2007,2011 GeoSolutions S.A.S.
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
package it.geosolutions.geoserver.sldservice;

import it.geosolutions.geoserver.rest.*;
import it.geosolutions.geoserver.rest.sldservice.Ramp;
import it.geosolutions.geoserver.rest.sldservice.Type;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group
 * @email nazzareno.sileno@geosdi.org
 */
public class GeoserverRESTSLDServiceTest extends GeoserverRESTTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(GeoserverRESTTest.class);
    
    /**
     * Test of classifyVectorData method, of class GeoServerRESTReader.
     */
    @Test
    public void testClassifyVectorData() {
        LOGGER.info("--- Testing classify vector data ---");
        if (!enabled()) {
            return;
        }
        String result = reader.classifyVectorData("states", "LAND_KM", Ramp.red, 
                null, null, null, null, null, null, null, null);
        LOGGER.info("Result of test classify vector data: " + result);
        assertNotNull(result);
    }

    /**
     * Test of rasterizeData method, of class GeoServerRESTReader.
     */
    @Test
    public void testRasterizeData() {
        LOGGER.info("--- Testing rasterize data ---");
        if (!enabled()) {
            return;
        }
        String result = reader.rasterizeData("sfdem", Ramp.custom, 0d, 100d,
                5, 1, Type.RAMP, "0xFF0000", "0x0000FF", null);
        LOGGER.info("Result of test rasterize data: " + result);
        assertNotNull(result);
    }

}