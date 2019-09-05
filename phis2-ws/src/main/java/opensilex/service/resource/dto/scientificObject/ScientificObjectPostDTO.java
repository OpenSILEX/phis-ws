//******************************************************************************
//                             ScientificObjectDTO.java 
// SILEX-PHIS
// Copyright © INRA 2017
// Creation date: Aug. 2017
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************

package opensilex.service.resource.dto.scientificObject;

import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import opensilex.service.configuration.DateFormats;
import opensilex.service.model.Accession;
import opensilex.service.resource.validation.interfaces.Required;
import opensilex.service.resource.dto.manager.AbstractVerifiedClass;
import opensilex.service.resource.dto.rdfResourceDefinition.PropertyPostDTO;
import opensilex.service.model.ScientificObject;
import opensilex.service.resource.validation.interfaces.URL;

/**
 * Scientific object DTO.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ScientificObjectPostDTO extends AbstractVerifiedClass {
    
    /**
     * Scientific object type.
     * @example http://www.opensilex.org/vocabulary/oeso#Plot)
     */
    private String rdfType;
    
    /**
     * The WKT geometry (WGS84 EPSG4326).
     * @example POLYGON(0 1, 1 2, 2 3, 3 0, 0 1)
     */
    private String geometry;
    
    /** 
     * Concerned experiment.
     * @example http://www.phenome-fppn.fr/diaphen/DIA2018-2)
     */
    private String experiment;
    
    /**
     * Object which the scientific object is part of.
     * @example http://www.phenome-fppn.fr/mtp/2017/o1032491)
     */
    private String isPartOf;
    
    /**
     * Year used to generated the scientific object. 
     * If it is not given, this is the actual year.
     * @example 2017
     */
    private String year;
    
    /**
     * Accession uri of the scientific object (contains species, variety, or accession information) 
 If it is not given, this is the actual year.
     * @example 2017
     */
    private Accession germplasm;   
    
    /**
     * Properties
     */
    private ArrayList<PropertyPostDTO> properties;

    @Override
    public ScientificObject createObjectFromDTO() {
        ScientificObject scientificObject = new ScientificObject();
        scientificObject.setRdfType(rdfType);
        scientificObject.setGeometry(geometry);
        scientificObject.setUriExperiment(experiment);
        scientificObject.setIsPartOf(isPartOf);
        scientificObject.setYear(year);
        scientificObject.setGermplasm(germplasm);
        
        if (properties != null) {
            properties.forEach((property) -> {
                scientificObject.addProperty(property.createObjectFromDTO());
            });
        }
        
        return scientificObject;
    }
    
    //SILEX:todo
    // Do the geometry validator (needs discussions about the 
    // allowed formats and geometry types)
    //\SILEX:todo
    
    @ApiModelProperty(example = "POLYGON((0 0, 10 0, 10 10, 0 10, 0 0))")
    public String getGeometry() {
        return geometry;
    }
    
    @Required
    @URL
    @ApiModelProperty(example = "http://www.opensilex.org/vocabulary/oeso#Plot")
    public String getRdfType() {
        return rdfType;
    }

    public void setRdfType(String rdfType) {
        this.rdfType = rdfType;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    @Pattern(regexp = DateFormats.YEAR_REGEX, message = "This is not a valid year. Excepted format : YYYY (e.g. 2017)")
    @ApiModelProperty(example = "2017")
    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
    
    @URL
    @ApiModelProperty(example = "http://www.phenome-fppn.fr/diaphen/DIA2017-1")
    public String getExperiment() {
        return experiment;
    }

    public void setExperiment(String experiment) {
        this.experiment = experiment;
    }

    @URL
    public String getIsPartOf() {
        return isPartOf;
    }

    public void setIsPartOf(String isPartOf) {
        this.isPartOf = isPartOf;
    }
    
    @Valid
    public ArrayList<PropertyPostDTO> getProperties() {
        return properties;
    }

    public void setProperties(ArrayList<PropertyPostDTO> properties) {
        this.properties = properties;
    }

    @URL
    public Accession getGermplasm() {
        return germplasm;
    }

    public void setGermplasm(Accession germplasm) {
        this.germplasm = germplasm;
    }
    
    
}
