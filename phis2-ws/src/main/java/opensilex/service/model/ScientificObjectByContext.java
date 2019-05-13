//******************************************************************************
//                               ScientificObject.java 
// SILEX-PHIS
// Copyright © INRA 2017
// Creation date: August 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************

package opensilex.service.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Scientific object model.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ScientificObjectByContext {
    
    /**
     * Scientific object URI.
     */
    private String uri;
    
    /**
     * Type.
     */
    private String rdfType;
    //Lables of the rdfType
    private List<String> rdfTypeLabels = new ArrayList<>();
    
    /**
     * Geometry of the scientific object.
     */
    private String geometry;
    
    /**
     * Experiment of the scientific object.
     */
    private String experiment;
    
    /**
     * Object which has part the scientific object
     */
    private String isPartOf;
    //year of the scientific object
    private String year;
    //label of the scientific object
    private String label;
    //list of labels of the scientific object
    private List<String> labels = new ArrayList<>();
    //The properties of the scientific object
    private ArrayList<Property> properties = new ArrayList<>();

    public ScientificObjectByContext(String uri) {
        this.uri = uri;
    }

    public ScientificObjectByContext() {
    }
    
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRdfType() {
        return rdfType;
    }

    public void setRdfType(String rdfType) {
        this.rdfType = rdfType;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public String getUriExperiment() {
        return experiment;
    }

    public void setUriExperiment(String uriExperiment) {
        this.experiment = uriExperiment;
    }
    
    public ArrayList<Property> getProperties() {
        return properties;
    }
    
    public void addProperty(Property property) {
        properties.add(property);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getExperiment() {
        return experiment;
    }

    public void setExperiment(String experiment) {
        this.experiment = experiment;
    }

    public String getIsPartOf() {
        return isPartOf;
    }

    public void setIsPartOf(String isPartOf) {
        this.isPartOf = isPartOf;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public List<String> getRdfTypeLabels() {
        return rdfTypeLabels;
    }
    
    public void addRdfTypeLabel(String rdfTypeLabel) {
        rdfTypeLabels.add(rdfTypeLabel);
    }

    public List<String> getLabels() {
        return labels;
    }
    
    public void addLabel(String scientificObjectLabel) {
        labels.add(scientificObjectLabel);
    }

    public void setProperties(ArrayList<Property> properties) {
        this.properties = properties;
    }
}
