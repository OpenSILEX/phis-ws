//**********************************************************************************************
//                                       RDFClass.java 
//
// Author(s): Arnaud Charleroy
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2018
// Creation date: December, 31 2017
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date: March, 05 2018
// Subject: Represents a triplestore concept with his possible metadata
//***********************************************************************************************
package phis2ws.service.view.model.phis;

import java.util.ArrayList;

/**
 *
 * @author Arnaud Charleroy<arnaud.charleroy@inra.fr>
 */
public class RDFClass {

    private String uri = null; // concept uri
    private String type = null; // concept type
    private ArrayList<Label> labels = null; // concept languages
    private ArrayList<RDFClass> subRanges = null; //concept sub ranges languages
    private ArrayList<String> instances = null; // concept instances

    public RDFClass() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public ArrayList<Label> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<Label> Labels) {
        this.labels = Labels;
    }

    public void addLabel(Label label) {
        if (this.labels == null) {
            this.labels = new ArrayList<>();
        }
        this.labels.add(label);
    }

    public ArrayList<RDFClass> getSubRanges() {
        if (subRanges == null || subRanges.isEmpty()) {
            return null;
        }
        return subRanges;
    }

    public void setSubRanges(ArrayList<RDFClass> subRanges) {
        this.subRanges = subRanges;
    }

    public ArrayList<String> getInstances() {
        return instances;
    }

    public void addInstances(String instance) {
        if (this.instances == null) {
            this.instances = new ArrayList<>();
        }
        this.instances.add(instance);
    }

    public void setInstances(ArrayList<String> instances) {
        this.instances = instances;
    }

}
