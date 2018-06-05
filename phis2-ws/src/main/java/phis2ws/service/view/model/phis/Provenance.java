//**********************************************************************************************
//                                       Provenance.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: September 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  October, 4 2017 (ajout de l'attribut wasGeneratedBy)
// Subject: Represents the data provenance view
//***********************************************************************************************
package phis2ws.service.view.model.phis;

import java.util.ArrayList;

public class Provenance {
    private String uri;
    private String creationDate;
    private WasGeneratedBy wasGeneratedBy;
    private ArrayList<String> documentsUris = new ArrayList<>();
    
    public Provenance() {
        
    }
    
    public Provenance(Provenance prov) {
        uri = prov.getUri();
        creationDate = prov.getCreationDate();
        wasGeneratedBy = prov.getWasGeneratedBy();
        
        if (prov.getDocumentsUris() != null) {
            for (String docUri : prov.getDocumentsUris()) {
                documentsUris.add(docUri);
            }
        }
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public WasGeneratedBy getWasGeneratedBy() {
        return wasGeneratedBy;
    }

    public void setWasGeneratedBy(WasGeneratedBy wasGeneratedBy) {
        this.wasGeneratedBy = wasGeneratedBy;
    }

    public ArrayList<String> getDocumentsUris() {
        return documentsUris;
    }

    public void setDocumentsUris(ArrayList<String> documentsUris) {
        this.documentsUris = documentsUris;
    }
    
    public void addDocumentUri(String documentUri) {
        this.documentsUris.add(uri);
    }
}
