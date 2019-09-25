//******************************************************************************
//                                ProjectPostDTO.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 11 juil. 2019
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource.dto.project;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import opensilex.service.configuration.DateFormat;
import opensilex.service.documentation.DocumentationAnnotation;
import opensilex.service.model.Contact;
import opensilex.service.model.FinancialFunding;
import opensilex.service.model.Project;
import opensilex.service.model.RdfResourceDefinition;
import opensilex.service.resource.ProjectResourceService;
import opensilex.service.resource.dto.manager.AbstractVerifiedClass;
import opensilex.service.resource.validation.interfaces.Date;
import opensilex.service.resource.validation.interfaces.Required;
import opensilex.service.resource.validation.interfaces.URL;

/**
 * The DTO for the POST of projects.
 * @see ProjectResourceService#post(java.util.ArrayList, javax.servlet.http.HttpServletRequest) 
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ProjectPostDTO extends AbstractVerifiedClass {

    //The name of the project.
    private String name;
    //A shortname for the project
    private String shortname;
    //The list of projects URI related to the project.
    private List<String> relatedProjects = new ArrayList<>();
    //The URI of the financial funding type.
    private String financialFunding;
    private String financialReference;
    //The description of the project.
    private String description;
    //The start date of the project. The format should be YYYY-MM-JJ
    private String startDate;
    //The end date of the project. The format should be YYYY-MM-JJ
    private String endDate;
    //A list of keyword corresponding to the project.
    private List<String> keywords = new ArrayList<>();
    //The home page of the project.
    private String homePage;
    //The list of administrative contacts of the project.
    private List<String> administrativeContacts = new ArrayList<>();
    //The list of coordinators of the projects.
    private List<String> coordinators = new ArrayList<>();
    //The list of scientific contacts of the project.
    private List<String> scientificContacts = new ArrayList<>();
    //The objective of the project.
    private String objective;
    
    @Override
    public Project createObjectFromDTO() {
        Project project = new Project();
        project.setName(name);
        project.setShortname(shortname);
        
        for (String relatedProjectUri : relatedProjects) {
            Project relatedProject = new Project();
            relatedProject.setUri(relatedProjectUri);
            project.addRelatedProject(relatedProject);
        }
        
        if (financialFunding != null) {
            FinancialFunding financialFundingObject = new FinancialFunding();
            financialFundingObject.setUri(financialFunding);
            project.setFinancialFunding(financialFundingObject);
        }
        project.setFinancialReference(financialReference);
        
        project.setDescription(description);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setKeywords(keywords);
        project.setHomePage(homePage);
        
        for (String administrativeContactUri : administrativeContacts) {
            Contact contact = new Contact();
            contact.setUri(administrativeContactUri);
            project.addAdministrativeContact(contact);
        }
        
        for (String coordinatorUri : coordinators) {
            Contact contact = new Contact();
            contact.setUri(coordinatorUri);
            project.addCoordinator(contact);
        }
        
        for (String scientificContactUri : scientificContacts) {
            Contact contact = new Contact();
            contact.setUri(scientificContactUri);
            project.addScientificContact(contact);
        }
        
        project.setObjective(objective);
        
        return project;
    }

    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_PROJECT_FINANCIAL_REFERENCE)
    public String getFinancialReference() {
        return financialReference;
    }

    public void setFinancialReference(String financialReference) {
        this.financialReference = financialReference;
    }

    @Required
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_PROJECT_NAME)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Required
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_PROJECT_SHORTNAME)
    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public List<String> getRelatedProjects() {
        return relatedProjects;
    }

    public void setRelatedProjects(List<String> relatedProjects) {
        this.relatedProjects = relatedProjects;
    }
    
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_PROJECT_FINANCIAL_URI)
    public String getFinancialFunding() {
        return financialFunding;
    }

    public void setFinancialFunding(String financialFunding) {
        this.financialFunding = financialFunding;
    }
    
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_PROJECT_DESCRIPTION)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    @Date(DateFormat.YMD)
    @Required
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_PROJECT_DATE_START)
    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    @Date(DateFormat.YMD)
    @Required
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_PROJECT_DATE_END)
    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
    
    @URL
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_PROJECT_HOME_PAGE)
    public String getHomePage() {
        return homePage;
    }

    public void setHomePage(String homePage) {
        this.homePage = homePage;
    }

    public List<String> getAdministrativeContacts() {
        return administrativeContacts;
    }

    public void setAdministrativeContacts(List<String> administrativeContacts) {
        this.administrativeContacts = administrativeContacts;
    }

    public List<String> getCoordinators() {
        return coordinators;
    }

    public void setCoordinators(List<String> coordinators) {
        this.coordinators = coordinators;
    }

    public List<String> getScientificContacts() {
        return scientificContacts;
    }

    public void setScientificContacts(List<String> scientificContacts) {
        this.scientificContacts = scientificContacts;
    }

    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_PROJECT_OBJECTIVE)
    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }
}
