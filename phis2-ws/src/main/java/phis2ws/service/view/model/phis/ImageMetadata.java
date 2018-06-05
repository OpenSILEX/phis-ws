//**********************************************************************************************
//                                       ImageMetadata.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: December, 11 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  January, 03 2018
// Subject: Represents the image metadata view
//***********************************************************************************************
package phis2ws.service.view.model.phis;

import java.util.ArrayList;

/**
 * Represents the image metadata view
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ImageMetadata {
    
    //image uri
    private String uri;
    //image type (in the ontology)
    private String rdfType;
    //elements concerned by the image
    private ArrayList<ConcernItem> concernedItems = new ArrayList<>();
    //shooting configuration
    private ShootingConfiguration configuration;
    //informations about the image file
    private FileInformations fileInformations;

    public ImageMetadata() {
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public ArrayList<ConcernItem> getConcernedItems() {
        return concernedItems;
    }

    public void setConcernedItems(ArrayList<ConcernItem> concernedItems) {
        this.concernedItems = concernedItems;
    }
    
    public void addConcernedItem(ConcernItem concernedItem) {
        this.concernedItems.add(concernedItem);
    }

    public ShootingConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ShootingConfiguration configuration) {
        this.configuration = configuration;
    }

    public FileInformations getFileInformations() {
        return fileInformations;
    }

    public void setFileInformations(FileInformations fileInformations) {
        this.fileInformations = fileInformations;
    }

    public String getRdfType() {
        return rdfType;
    }

    public void setRdfType(String rdfType) {
        this.rdfType = rdfType;
    }
}
