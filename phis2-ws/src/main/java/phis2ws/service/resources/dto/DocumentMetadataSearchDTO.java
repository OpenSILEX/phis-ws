//**********************************************************************************************
//                                       DocumentMetadataSearchDTO.java 
//
// Author(s): Arnaud Charleroy, Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: March 2017
// Contact: arnaud.charleroy@inra.fr, morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  
// October, 12 2017 (Add documents status)
// March, 2018  (Add documents additionnalProperties and concernedItemType)
// Subject: Represents the submitted JSON to search the documents (POST)
//***********************************************************************************************
package phis2ws.service.resources.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import phis2ws.service.configuration.DefaultBrapiPaginationValues;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;

/**
 * Represents JSON send to post document search service
 *
 * @author Arnaud Charleroy<arnaud.charleroy@inra.fr>
 */
public class DocumentMetadataSearchDTO extends AbstractVerifiedClass {

    private String uri; // /!\ not use in with post service
    private String documentType;
    private String creator;
    private String language; //it's recommended to use RFC4646 specification
    private String title;
    private String creationDate;
    private String startDate;
    private String endDate;

    private String extension;
    private String comment;
    private List<String> concern; // document linked entities list
    private List<AdditionalDocumentPropertiesDTO> additionnalProperties; //not used by the client for now
    private Integer pageSize;
    private Integer page;
    private String concernedItemType;

    @Override
    public Map rules() {
        Map<String, Boolean> rules = new HashMap<>();
        rules.put("documentType", Boolean.FALSE);
        rules.put("checksum", Boolean.FALSE);
        rules.put("uri", Boolean.FALSE);
        rules.put("creator", Boolean.FALSE);
        rules.put("language", Boolean.FALSE);
        rules.put("title", Boolean.FALSE);
        rules.put("creationDate", Boolean.FALSE);
        rules.put("extension", Boolean.FALSE);
        rules.put("concern", Boolean.FALSE);
        rules.put("comment", Boolean.FALSE);
        rules.put("status", Boolean.TRUE);
//        rules.put("additionnalProperties", Boolean.FALSE);
        return rules;
    }

    @Override
    public Object createObjectFromDTO() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @ApiModelProperty(example = "http://www.phenome-fppn.fr/vocabulary/2015#ScientificDocument")
    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String mediaType) {
        this.documentType = mediaType;
    }

    @ApiModelProperty(example = "John Doe")
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    @ApiModelProperty(example = "fr")
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @ApiModelProperty(example = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @ApiModelProperty(example = "2017-01-01")
    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public List<String> getConcern() {
        return concern;
    }

    public void setConcern(List<String> concern) {
        this.concern = concern;
    }

    @ApiModelProperty(example = "jpg")
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<AdditionalDocumentPropertiesDTO> getAdditionnalProperties() {
        return additionnalProperties;
    }

    public void setAdditionnalProperties(List<AdditionalDocumentPropertiesDTO> additionnalProperties) {
        this.additionnalProperties = additionnalProperties;
    }

    @ApiModelProperty(example = DefaultBrapiPaginationValues.PAGE_SIZE)
    public Integer getPageSize() {
        if (pageSize == null) {
            this.pageSize = 20;
        }
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @ApiModelProperty(example = DefaultBrapiPaginationValues.PAGE)
    public Integer getPage() {
        if (page == null) {
            return 0;
        }
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getConcernedItemType() {
        return concernedItemType;
    }

    public void setConcernedItemType(String concernedItemType) {
        this.concernedItemType = concernedItemType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

}
