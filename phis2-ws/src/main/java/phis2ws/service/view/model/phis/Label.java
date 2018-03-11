//**********************************************************************************************
//                                       Label.java 
//
// Author(s): Arnaud Charleroy
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2018
// Creation date: March, 01 2018
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date: March, 05 2018
// Subject: Represents the label view
//***********************************************************************************************
package phis2ws.service.view.model.phis;

/**
 * Represents a label
 * @author Arnaud Charleroy<arnaud.charleroy@inra.fr>
 */
public class Label {
    private String language = null; 
    private String value = null;

    public Label() {
    }
    public Label(String language, String value) {
        this.language = language;
        this.value = value;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    
    
}
