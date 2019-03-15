//******************************************************************************
//                          AnnotationPostDTO.java
// SILEX-PHIS
// Copyright © INRA 2018
// Creation date: 06 March, 2019
// Contact: arnaud.charleroy@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package phis2ws.service.resources.dto.annotation;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.joda.time.DateTime;
import phis2ws.service.documentation.DocumentationAnnotation;
import phis2ws.service.resources.dto.manager.AbstractVerifiedClass;
import phis2ws.service.resources.validation.interfaces.URL;
import phis2ws.service.view.model.phis.Annotation;

/**
 * DTO representing an annotation for a POST
 * @author Arnaud Charleroy <arnaud.charleroy@inra.fr>
 */
public class AnnotationPostDTO extends AbstractVerifiedClass {

    /** 
     * URI that represents the author 
     * @example http://www.phenome-fppn.fr/diaphen/id/agent/arnaud_charleroy
     */
    private String creator;

    /** 
     * Motivation instance URI that describe the purpose of this annotation 
     * @example http://www.w3.org/ns/oa#commenting
     */ 
    private String motivatedBy;

    /**
     * Represents the annotation's body values
     * @link https://www.w3.org/TR/annotation-model/#cardinality-of-bodies-and-targets
     */
    private ArrayList<String> bodyValues;

    /**
     * URIs concerned by this annotation 
     * @example http://www.phenome-fppn.fr/diaphen/2017/o1032481
     * @link https://www.w3.org/TR/annotation-model/#cardinality-of-bodies-and-targets
     */
    private ArrayList<String> targets;
    
    /**
     * Constructor to create a DTO from an annotation model
     * @param annotation 
     */
    public AnnotationPostDTO(Annotation annotation) {        
        this.creator = annotation.getCreator();
        this.bodyValues = annotation.getBodyValues();
        this.motivatedBy = annotation.getMotivatedBy();
        this.targets = annotation.getTargets();
    }

    @Override
    public Annotation createObjectFromDTO() {
        return new Annotation(null, DateTime.now(), creator, bodyValues, motivatedBy, targets);
    }

    @URL
    @NotNull
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_ANNOTATION_MOTIVATED_BY)
    public String getMotivatedBy() {
        return motivatedBy;
    }

    public void setMotivatedBy(String motivatedBy) {
        this.motivatedBy = motivatedBy;
    }

    @URL
    @NotNull
    @ApiModelProperty(example = DocumentationAnnotation.EXAMPLE_ANNOTATION_CREATOR)
    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    @ApiModelProperty(notes = "Need to be an array of text")
    public ArrayList<String> getBodyValues() {
        return bodyValues;
    }

    public void setBodyValues(ArrayList<String> bodyValues) {
        this.bodyValues = bodyValues;
    }

    @URL
    @NotEmpty
    @NotNull
    @ApiModelProperty(notes = "Need to be an array of URI")
    public ArrayList<String> getTargets() {
        return targets;
    }
    public void setTargets(ArrayList<String> targets) {
        this.targets = targets;
    }
}
