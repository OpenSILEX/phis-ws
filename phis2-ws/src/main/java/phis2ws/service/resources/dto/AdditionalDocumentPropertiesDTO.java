//**********************************************************************************************
//                                       AdditionalDocumentPropertiesDTO.java 
//
// Author(s): Arnaud Charleroy
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2018
// Creation date: March 2018
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  March 12 2018
// Subject: Represents AdditionalDocumentProperties
//***********************************************************************************************
package phis2ws.service.resources.dto;

import java.util.HashMap;
import java.util.Map;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;

/**
 * Example : wasGeneratedBy
 *
 * @author Arnaud Charleroy<arnaud.charleroy@inra.fr>
 */
public class AdditionalDocumentPropertiesDTO extends AbstractVerifiedClass {

    private String property = null;
    private String value = null;

    @Override
    public Map rules() {
        Map<String, Boolean> rules = new HashMap<>();
        rules.put("property", Boolean.TRUE);
        rules.put("value", Boolean.TRUE);
        return rules;
    }

    @Override
    public Object createObjectFromDTO() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
