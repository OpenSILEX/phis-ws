//******************************************************************************
//                                ScientificObjectByContextDTO.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 13 mai 2019
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource.dto.scientificObject;

import java.util.ArrayList;
import java.util.List;
import opensilex.service.model.Property;
import opensilex.service.model.ScientificObjectByContext;
import opensilex.service.resource.dto.rdfResourceDefinition.PropertyLabelsDTO;

/**
 * Scientific object detail by context DTO.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ScientificObjectByContextDTO {
    //Context of the metadata of the scientific object
    //@example http://www.opensilex.org/demo/DMO2019-1
    private String context;
    //Labels of the scientific object in the given context
    private List<String> labels;
    //Properties of the scientific object in the given context
    private List<PropertyLabelsDTO> properties = new ArrayList<>();

    public ScientificObjectByContextDTO(ScientificObjectByContext scientificObjectByContext) {
        context = scientificObjectByContext.getExperiment();
        labels = scientificObjectByContext.getLabels();
        
        for (Property property : scientificObjectByContext.getProperties()) {
            properties.add(new PropertyLabelsDTO(property));
        }
    }
    
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<PropertyLabelsDTO> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyLabelsDTO> properties) {
        this.properties = properties;
    }
    
}
