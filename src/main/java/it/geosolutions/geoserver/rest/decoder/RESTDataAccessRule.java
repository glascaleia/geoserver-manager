/*
 *  geo-platform
 *  Rich webgis framework
 *  http://geo-platform.org
 * ====================================================================
 *
 * Copyright (C) 2008-2013 geoSDI Group (CNR IMAA - Potenza - ITALY).
 *
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. This program is distributed in the 
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR 
 * A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. You should have received a copy of the GNU General 
 * Public License along with this program. If not, see http://www.gnu.org/licenses/ 
 *
 * ====================================================================
 *
 * Linking this library statically or dynamically with other modules is 
 * making a combined work based on this library. Thus, the terms and 
 * conditions of the GNU General Public License cover the whole combination. 
 * 
 * As a special exception, the copyright holders of this library give you permission 
 * to link this library with independent modules to produce an executable, regardless 
 * of the license terms of these independent modules, and to copy and distribute 
 * the resulting executable under terms of your choice, provided that you also meet, 
 * for each linked independent module, the terms and conditions of the license of 
 * that module. An independent module is a module which is not derived from or 
 * based on this library. If you modify this library, you may extend this exception 
 * to your version of the library, but you are not obligated to do so. If you do not 
 * wish to do so, delete this exception statement from your version. 
 *
 */
package it.geosolutions.geoserver.rest.decoder;

import java.io.Serializable;
import java.util.Set;

/**
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group
 * @email nazzareno.sileno@geosdi.org
 */
public class RESTDataAccessRule implements Serializable {

    private static final long serialVersionUID = 5217813425844748166L;
    private String workspace;
    private String layer;
    private String rulePath;
    private String accessMode;
//    private GSAccessMode accessMode;
    private Set<String> roles;

    public String getWorkspace() {
        return this.workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getRulePath() {
        return this.rulePath;
    }

//    public String generateRulePath() {
//        assert (this.getWorkspace() != null && this.getLayer() != null
//                && super.get(GSDataAccessRuleKeyValue.ACCESS_MODE.name()) != null) : "Impossibile to  generate Rule Path because some properties are null";
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(this.getWorkspace());
//        stringBuilder.append(".");
//        stringBuilder.append(this.getLayer());
//        stringBuilder.append(".");
//        stringBuilder.append(super.get(GSDataAccessRuleKeyValue.ACCESS_MODE.name()));
//        return stringBuilder.toString();
//    }
    public void setRulePath(String rulePath) {
        this.rulePath = rulePath;
    }

    public String getLayer() {
        return this.layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public String getAccessMode() {
        return this.accessMode;
    }

    public void setAccessMode(String accessMode) {
        this.accessMode = accessMode;
    }

    public Set<String> getRoles() {
        return this.roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "GSDataAccessRule{" + "workspace=" + this.getWorkspace() + ", layer="
                + this.getLayer() + ", accessMode=" + this.getAccessMode()
                + ", roles=" + this.getRoles() + ", rulePath=" + this.getRulePath()
                + '}';
    }
}
