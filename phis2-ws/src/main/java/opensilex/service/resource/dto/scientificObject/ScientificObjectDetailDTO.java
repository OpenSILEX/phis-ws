//******************************************************************************
//                                ScientificObjectDetailDTO.java
// SILEX-PHIS
// Copyright © INRA 2019
// Creation date: 13 mai 2019
// Contact: morgane.vidal@inra.fr, anne.tireau@inra.fr, pascal.neveu@inra.fr
//******************************************************************************
package opensilex.service.resource.dto.scientificObject;

import java.util.ArrayList;
import java.util.List;
import opensilex.service.model.ScientificObjectByContext;
import opensilex.service.model.ScientificObjectMultipleContext;

/**
 * Scientific object detail DTO.
 * @author Morgane Vidal <morgane.vidal@inra.fr>
 */
public class ScientificObjectDetailDTO {
    //scientific object uri
    //@example http://www.opensilex.org/opensilex/2019/o19000019
    private String uri;
    //geometry of the scientific object
    //@example {\"type\":\"Polygon\",\"coordinates\":[[[3.97167246,43.61328981],
    //[3.97171243,43.61332417],[3.9717427,43.61330558],[3.97170272,43.61327122],
    //[3.97167246,43.61328981],[3.97167246,43.61328981]]]}
    private String geometry;
    //metadata of the scientific object by context
    private List<ScientificObjectByContextDTO> usedIn = new ArrayList<>();
    
    public ScientificObjectDetailDTO(ScientificObjectMultipleContext scientificObject) {
        setUri(scientificObject.getUri());
        setGeometry(scientificObject.getGeometry());
        
        for (ScientificObjectByContext scientificObjectByContext : scientificObject.getMetadataByContext()) {
            usedIn.add(new ScientificObjectByContextDTO(scientificObjectByContext));
        }
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public List<ScientificObjectByContextDTO> getUsedIn() {
        return usedIn;
    }

    public void setUsedIn(List<ScientificObjectByContextDTO> usedIn) {
        this.usedIn = usedIn;
    }
    
}
