//**********************************************************************************************
//                                       Document.java 
//
// Author(s): Arnaud Charleroy, Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: March 2017
// Contact: arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  March, 2018 (add additionalDocumentProperties attribute)
// Subject: Represents the Document
//***********************************************************************************************
package phis2ws.service.view.model.phis;

import java.util.ArrayList;
import phis2ws.service.resources.dto.AdditionalDocumentPropertiesDTO;
import phis2ws.service.resources.dto.ConcernItemDTO;

public class Document {
    private String uri;
    private String documentType;
    private String creator;
    private String language;
    private String title;
    private String creationDate;
    private String format;
    private String comment;
    private ArrayList<ConcernItemDTO> concernedItems = new ArrayList<>();
    private String status;
    private ArrayList<AdditionalDocumentPropertiesDTO> additionnalProperties = new ArrayList<>();


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public ArrayList<ConcernItemDTO> getConcernedItems() {
        return concernedItems;
    }

    public void setConcernedItems(ArrayList<ConcernItemDTO> concernedItems) {
        this.concernedItems = concernedItems;
    }
    
    public void addConcernedItem(ConcernItemDTO concernedItem) {
        this.concernedItems.add(concernedItem);
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public ArrayList<AdditionalDocumentPropertiesDTO> getAdditionnalProperties() {
        return additionnalProperties;
    }

    public void setAdditionnalProperties(ArrayList<AdditionalDocumentPropertiesDTO> additionnalProperties) {
        this.additionnalProperties = additionnalProperties;
    }
    
    public void addAdditionnalProperties(AdditionalDocumentPropertiesDTO additionnalProperties) {
        this.additionnalProperties.add(additionnalProperties);
    }
    //SILEX:conception
    //may be add additionnal properties comparison ?
    //\SILEX:conception
    /**
     * Compare two documents together (compare attributes one by one)
     * @todo add additionnal properties comparison
     * @param document Document to compare to this one
     * @return true if there are equals else false
     */
    public boolean equals(Document document) {
        return this.uri.equals(document.uri)
            && this.documentType.equals(document.documentType)
            && this.creator.equals(document.creator)
            && this.language.equals(document.language)
            && this.title.equals(document.title)
            && this.creationDate.equals(document.creationDate)
            && this.format.equals(document.format)
            && this.status.equals(document.status);
    }
}
