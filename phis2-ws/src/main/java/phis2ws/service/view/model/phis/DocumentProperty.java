//**********************************************************************************************
//                                       DocumentProperty.java 
//
// Author(s): Arnaud CHARLEROY
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: December 2017
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date: March 2018 
// Subject: Represents the document property view
//***********************************************************************************************
package phis2ws.service.view.model.phis;

import java.util.ArrayList;

/**
 * Represent a subclass document property (concepts in triplestore)
 * @author Arnaud Charleroy<arnaud.charleroy@inra.fr>
 */
public class DocumentProperty {

    private String propertyUri = null; // property uri
    private String propertyType = null; // property type
    private ArrayList<Label> labels = null; // property labels
    private ArrayList<RDFClass> domain = null; // property domains
    private ArrayList<RDFClass> ranges = null; // property ranges
    private ArrayList<String> instances = null; // property instances

    public DocumentProperty() {
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }

    public ArrayList<RDFClass> getRanges() {
        return ranges;
    }

    public void setRanges(ArrayList<RDFClass> ranges) {
        this.ranges = ranges;
    }

    public String getPropertyUri() {
        return propertyUri;
    }

    public void setPropertyUri(String propertyUri) {
        this.propertyUri = propertyUri;
    }

    public ArrayList<RDFClass> getDomains() {
        return domain;
    }

    public void addDomain(RDFClass domain) {
        if (this.domain == null) {
            this.domain = new ArrayList<>();
        }
        this.domain.add(domain);
    }

    public void setDomains(ArrayList<RDFClass> domains) {
        this.domain = domains;
    }

    public ArrayList<Label> getLabels() {
        return labels;
    }

    public void setLabels(ArrayList<Label> Labels) {
        this.labels = Labels;
    }

    public void addRange(RDFClass range) {
        if (this.ranges == null) {
            this.ranges = new ArrayList<>();
        }
        this.ranges.add(range);
    }

    public void addLabel(Label label) {
        if (this.labels == null) {
            this.labels = new ArrayList<>();
        }
        this.labels.add(label);
    }

    public void addInstance(String instance) {
        if (this.instances == null) {
            this.instances = new ArrayList<>();
        }
        this.instances.add(instance);
    }

    public void setInstances(ArrayList<String> instances) {
        this.instances = instances;
    }

}
