//**********************************************************************************************
//                                       ProjectDTO.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: March 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  May, 2017
// Subject: A class which contains methods to automatically check the attributes
//          of a class, from rules defined by user.
//          Contains the list of the elements which might be send by the Client
//          to save the database
//***********************************************************************************************
package phis2ws.service.resources.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;
import phis2ws.service.view.model.phis.Contact;
import phis2ws.service.view.model.phis.Project;

public class ProjectDTO extends AbstractVerifiedClass {
    
    final static Logger LOGGER = LoggerFactory.getLogger(ProjectDTO.class);
    
    private String uri;
    private String name;
    private String acronyme;
    private String subprojectType;
    private String financialSupport;
    private String financialName;
    private String dateStart;
    private String dateEnd;
    private String keywords;
    private String description;
    private String objective;
    private String parentProject;
    private String website;
    private ArrayList<Contact> contacts;

    @Override
    public Map rules() {
        Map<String, Boolean> rules = new HashMap<>();
        rules.put(uri, Boolean.TRUE);
        rules.put(name, Boolean.TRUE);
        rules.put(acronyme, Boolean.FALSE);
        rules.put(subprojectType, Boolean.FALSE);
        rules.put(financialSupport, Boolean.FALSE);
        rules.put(financialName, Boolean.FALSE);
        rules.put(dateStart, Boolean.TRUE);
        rules.put(dateEnd, Boolean.FALSE);
        rules.put(keywords, Boolean.FALSE);
        rules.put(description, Boolean.FALSE);
        rules.put(objective, Boolean.FALSE);
        rules.put(parentProject, Boolean.FALSE);
        rules.put(website, Boolean.FALSE);
        
        return rules;
    }

    @Override
    public Project createObjectFromDTO() {
        Project project = new Project(uri);
        project.setName(name);
        project.setAcronyme(acronyme);
        project.setSubprojectType(subprojectType);
        project.setFinancialSupport(financialSupport);
        project.setFinancialName(financialName);
        project.setDateStart(dateStart);
        project.setDateEnd(dateEnd);
        project.setKeywords(keywords);
        project.setDescription(description);
        project.setObjective(objective);
        project.setParentProject(parentProject);
        project.setWebsite(website);
        
        if (contacts != null && !contacts.isEmpty()) {
            for (Contact contact : contacts) {
                project.addContact(contact);
            }
        }
        
        return project;
    }

    @ApiModelProperty(example = "http://phenome-fppn.fr/phis_field/projectTest")
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
    
    @ApiModelProperty(example = "projectTest")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ApiModelProperty(example = "P T")
    public String getAcronyme() {
        return acronyme;
    }

    public void setAcronyme(String acronyme) {
        this.acronyme = acronyme;
    }
    
    @ApiModelProperty(example = "subproject type")
    public String getSubprojectType() {
        return subprojectType;
    }

    public void setSubprojectType(String subprojectType) {
        this.subprojectType = subprojectType;
    }
    
    @ApiModelProperty(example = "financial support")
    public String getFinancialSupport() {
        return financialSupport;
    }

    public void setFinancialSupport(String financialSupport) {
        this.financialSupport = financialSupport;
    }
    
    @ApiModelProperty(example = "financial name")
    public String getFinancialName() {
        return financialName;
    }

    public void setFinancialName(String financialName) {
        this.financialName = financialName;
    }

    @ApiModelProperty(example = "2015-07-07")
    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }
    
    @ApiModelProperty(example = "2016-07-07")
    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    @ApiModelProperty(example = "keywords")
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @ApiModelProperty(example = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ApiModelProperty(example = "objective")
    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    @ApiModelProperty(example = "parent project")
    public String getParentProject() {
        return parentProject;
    }

    public void setParentProject(String parentProject) {
        this.parentProject = parentProject;
    }

    @ApiModelProperty(example = "http://example.com")
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
    
    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }
}
