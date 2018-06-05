//**********************************************************************************************
//                                       ExperimentDTO.java 
//
// Author(s): Morgane Vidal
// PHIS-SILEX version 1.0
// Copyright © - INRA - 2017
// Creation date: January 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
// Last modification date:  October, 31 2017 : Passage de trial à experiment
// Subject: A class which contains methods to automatically check the attributes
//          of a class, from rules defined by user.
//          Contains the list of the elements which might be send by the client
//          to save the database.
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
import phis2ws.service.view.model.phis.Group;
import phis2ws.service.view.model.phis.Project;
import phis2ws.service.view.model.phis.Experiment;

public class ExperimentDTO extends AbstractVerifiedClass {

    final static Logger LOGGER = LoggerFactory.getLogger(ExperimentDTO.class);
    
    private String uri;
    private String startDate;
    private String endDate;
    private String field;
    private String campaign;
    private String place;
    private String alias;
    private String comment;
    private String keywords;
    private String objective;
    private String cropSpecies;
    private ArrayList<String> projectsUris;
    private ArrayList<String> groupsUris;
    private ArrayList<Contact> contacts;
    
    @Override
    public Map rules() {
        Map<String, Boolean> rules = new HashMap<>();
        rules.put(uri, Boolean.TRUE);
        rules.put(startDate, Boolean.TRUE);
        rules.put(endDate, Boolean.TRUE);
        rules.put(field, Boolean.FALSE);
        rules.put(campaign, Boolean.FALSE);
        rules.put(place, Boolean.FALSE);
        rules.put(alias, Boolean.FALSE);
        rules.put(comment, Boolean.FALSE);
        rules.put(keywords, Boolean.FALSE);
        rules.put(objective, Boolean.FALSE);
        rules.put(cropSpecies, Boolean.FALSE);
        
        return rules;
    }

    @Override
    public Experiment createObjectFromDTO() {
        Experiment experiment = new Experiment(uri);
        experiment.setStartDate(startDate);
        experiment.setEndDate(endDate);
        experiment.setField(field);
        experiment.setCampaign(campaign);
        experiment.setPlace(place);
        experiment.setAlias(alias);
        experiment.setComment(comment);
        experiment.setKeywords(keywords);
        experiment.setObjective(objective);
        experiment.setCropSpecies(cropSpecies);
        
        if (projectsUris != null) {
            for (String projectURI : projectsUris) {
                Project project = new Project(projectURI);
                experiment.addProject(project);
            }
        }
        
        if (groupsUris != null) {
            for (String groupURI : groupsUris) {
                Group group = new Group(groupURI);
                experiment.addGroup(group);
            }
        }
        
        if (contacts != null && !contacts.isEmpty()) {
            for (Contact contact : contacts) {
                experiment.addContact(contact);
            }
        }
        
        return experiment;
    }

    @ApiModelProperty(example = "http://www.phenome-fppn.fr/diaphen/drops")
    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @ApiModelProperty(example = "2015-07-07")
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @ApiModelProperty(example = "2015-08-07")
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    @ApiModelProperty(example = "field")
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @ApiModelProperty(example = "campaign")
    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    @ApiModelProperty(example = "place")
    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    @ApiModelProperty(example = "alias")
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @ApiModelProperty(example = "comment")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @ApiModelProperty(example = "keywords")
    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @ApiModelProperty(example = "objective")
    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }
    
    public ArrayList<String> getProjectsUris() {
        return projectsUris;
    }

    public void setProjectsUris(ArrayList<String> projectsUris) {
        this.projectsUris = projectsUris;
    }

    public ArrayList<String> getGroupsUris() {
        return groupsUris;
    }

    public void setGroupsUris(ArrayList<String> groupsUris) {
        this.groupsUris = groupsUris;
    }

    @ApiModelProperty(example = "maize")
    public String getCropSpecies() {
        return cropSpecies;
    }

    public void setCropSpecies(String cropSpecies) {
        this.cropSpecies = cropSpecies;
    }
    
    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }
}
